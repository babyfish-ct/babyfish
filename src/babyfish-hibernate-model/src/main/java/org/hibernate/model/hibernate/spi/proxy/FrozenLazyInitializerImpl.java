/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2016, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.hibernate.model.hibernate.spi.proxy;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.babyfish.collection.FrozenContext;
import org.babyfish.data.ModificationException;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.EventAttributeContext;
import org.babyfish.data.event.PropertyVersion;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.UncheckedException;
import org.babyfish.model.event.ScalarEvent;
import org.babyfish.model.event.ScalarListener;
import org.babyfish.model.jpa.metadata.JPAModelClass;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelProvider;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.proxy.pojo.BasicLazyInitializer;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

class FrozenLazyInitializerImpl implements FrozenLazyInitializer, MethodHandler, Serializable {
    
    private static final long serialVersionUID = -3172051759799034702L;
    
    private static final Object AK_SCALAR_LISTENER = new Object();

    private static final Object[] EMPTY_ARGS = new Object[0];
    
    private static final Object AK_MODIFIYING_EXECUTED = new Object();
    
    private static final Object AK_FROZENCONTEXT = new Object();
    
    private static final Field GET_IDENTIFIER_METHOD_FIELD;
    
    private static final Field SET_IDENTIFIER_METHOD_FIELD;
    
    protected HibernateProxy owner;
    
    protected LazyInitializer lazyInitializer;
    
    protected transient FrozenContext<?> idFrozenContext;
    
    protected transient ScalarListener scalarListener;
    
    protected transient Method getIdentifierMethod;
    
    protected transient Method setIdentifierMethod;
    
    private transient Object oldTarget;
    
    private transient boolean disableSetTargetIdentifier;
    
    private transient boolean disableTargetListener;
    
    FrozenLazyInitializerImpl(HibernateProxy owner) {
        ProxyObject proxyObject = (ProxyObject)owner;
        MethodHandler handler = proxyObject.getHandler();
        if (!(handler instanceof LazyInitializer)) {
            Arguments.mustBeInstanceOfValue(
                    "((" +
                    ProxyObject.class.getName() +
                    ")owner).getHandler()", 
                    handler, 
                    LazyInitializer.class);
        }
        LazyInitializer lazyInitializer = owner.getHibernateLazyInitializer();
        if (lazyInitializer instanceof FrozenLazyInitializer) {
            throw new AssertionError("Internal bug");
        }
        this.owner = owner;
        this.lazyInitializer = lazyInitializer;
        this.initTransient();
        proxyObject.setHandler(this);
    }
    
    private void readObject(java.io.ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.initTransient();
    }
    
    private void initTransient() {
        
        LazyInitializer rawLazyInitializer = this.lazyInitializer;
        try {
            this.getIdentifierMethod = (Method)GET_IDENTIFIER_METHOD_FIELD.get(rawLazyInitializer);
            this.setIdentifierMethod = (Method)SET_IDENTIFIER_METHOD_FIELD.get(rawLazyInitializer);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        
        if (!rawLazyInitializer.isUninitialized()) {
            ObjectModel targetOM = ((ObjectModelProvider)rawLazyInitializer.getImplementation()).objectModel();
            ScalarListener listener = this.new TargetScalarListener();
            targetOM.removeScalarListener(listener);
            targetOM.addScalarListener(listener);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void freezeIdentifier(FrozenContext<?> ctx) {
        this.idFrozenContext = FrozenContext.combine(
                (FrozenContext)this.idFrozenContext, 
                (FrozenContext)ctx);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void unfreezeIdentifier(FrozenContext<?> ctx) {
        this.idFrozenContext = FrozenContext.remove(
                (FrozenContext)this.idFrozenContext, 
                (FrozenContext)ctx);
    }

    @Override
    public void addScalarListener(ScalarListener listener) {
        this.scalarListener = ScalarListener.combine(this.scalarListener, listener);
    }

    @Override
    public void removeScalarListener(ScalarListener listener) {
        this.scalarListener = ScalarListener.remove(this.scalarListener, listener);
    }

    @Override
    public Serializable getIdentifier() {
        if (!this.isUninitialized()) {
            try {
                return
                        (Serializable)
                        this.getIdentifierMethod
                        .invoke(this.lazyInitializer.getImplementation(), EMPTY_ARGS);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new AssertionError("Internal bug", ex);
            } catch (InvocationTargetException ex) {
                throw UncheckedException.rethrow(ex.getTargetException());
            }
        }
        return this.lazyInitializer.getIdentifier();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void setIdentifier(Serializable id) {
        FrozenContext<Serializable> ctx = (FrozenContext)this.idFrozenContext;
        FrozenContext.suspendFreezing(ctx, this.owner);
        try {
            this.setRawIdentifier(id);
        } finally {
            FrozenContext.resumeFreezing(ctx);
        }
    }
    
    private void setRawIdentifier(Serializable id) {
        this.lazyInitializer.setIdentifier(id);
        if (!this.isUninitialized() && !this.disableSetTargetIdentifier) {
            try {
                Method setIdentifierMethod = this.setIdentifierMethod;
                if (!setIdentifierMethod.isAccessible()) {
                    setIdentifierMethod.setAccessible(true);
                }
                this.disableTargetListener = true;
                try {
                    setIdentifierMethod.invoke(this.getImplementation(), new Object[] { id });
                } finally {
                    this.disableTargetListener = false;
                }
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new AssertionError("Internal bug", ex);
            } catch (InvocationTargetException ex) {
                UncheckedException.rethrow(ex.getTargetException());
            }
        }
    }

    @Override
    public void setImplementation(Object target) {
        LazyInitializer rawLazyInitializer = this.lazyInitializer;
        Object oldTarget = 
                rawLazyInitializer.isUninitialized() ? 
                        null : 
                        rawLazyInitializer.getImplementation();
        if (target != oldTarget) {
            ScalarListener listener = this.new TargetScalarListener();
            if (oldTarget != null) {
                ((ObjectModelProvider)oldTarget).objectModel().removeScalarListener(listener);
            }
            rawLazyInitializer.setImplementation(target);
            this.oldTarget = target;
            if (target != null) {
                ObjectModel targetOM = ((ObjectModelProvider)target).objectModel();
                targetOM.removeScalarListener(listener); //remove the duplicated listener.
                targetOM.addScalarListener(listener);
            }
        }
    }
    
    @Override
    public void initialize() throws HibernateException {
        LazyInitializer rawLazyInitializer = this.lazyInitializer;
        Object oldTarget = 
                rawLazyInitializer.isUninitialized() ? 
                        null : 
                        rawLazyInitializer.getImplementation();
        rawLazyInitializer.initialize();
        Object target = rawLazyInitializer.getImplementation();
        if (oldTarget != target) {
            ScalarListener listener = this.new TargetScalarListener();
            if (oldTarget != null) {
                ((ObjectModelProvider)oldTarget).objectModel().removeScalarListener(listener);
            }
            this.oldTarget = target;
            if (target != null) {
                ObjectModel targetOM = ((ObjectModelProvider)target).objectModel();
                targetOM.removeScalarListener(listener); //remove the duplicated listener.
                targetOM.addScalarListener(listener);
            }
        }
    }

    @Override
    public Object getImplementation() {
        Object oldTarget = this.oldTarget;
        Object target = this.lazyInitializer.getImplementation();
        if (oldTarget != target) {
            ScalarListener listener = this.new TargetScalarListener();
            if (oldTarget != null) {
                ((ObjectModelProvider)oldTarget).objectModel().removeScalarListener(listener);
            }
            this.oldTarget = target;
            if (target != null) {
                ObjectModel targetOM = ((ObjectModelProvider)target).objectModel();
                targetOM.removeScalarListener(listener); //remove the duplicated listener.
                targetOM.addScalarListener(listener);
            }
        }
        return target;
    }

    @Override
    public Object getImplementation(SessionImplementor session)
            throws HibernateException {
        Object oldTarget = this.oldTarget;
        Object target = this.lazyInitializer.getImplementation(session);
        if (oldTarget != target) {
            ScalarListener listener = this.new TargetScalarListener();
            if (oldTarget != null) {
                ((ObjectModelProvider)oldTarget).objectModel().removeScalarListener(listener);
            }
            this.oldTarget = target;
            if (target != null) {
                ObjectModel targetOM = ((ObjectModelProvider)target).objectModel();
                targetOM.removeScalarListener(listener); //remove the duplicated listener.
                targetOM.addScalarListener(listener);
            }
        }
        return target;
    }

    @Override
    public String getEntityName() {
        return this.lazyInitializer.getEntityName();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getPersistentClass() {
        return this.lazyInitializer.getPersistentClass();
    }

    @Override
    public boolean isUninitialized() {
        return this.lazyInitializer.isUninitialized();
    }

    @Override
    public boolean isReadOnlySettingAvailable() {
        return this.lazyInitializer.isReadOnlySettingAvailable();
    }

    @Override
    public boolean isReadOnly() {
        return this.lazyInitializer.isReadOnly();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.lazyInitializer.setReadOnly(readOnly);
    }

    @Override
    public SessionImplementor getSession() {
        return this.lazyInitializer.getSession();
    }

    @Override
    public void setSession(SessionImplementor session)
            throws HibernateException {
        this.lazyInitializer.setSession(session);
    }

    @Override
    public void unsetSession() {
        this.lazyInitializer.unsetSession();
    }

    @Override
    public void setUnwrap(boolean unwrap) {
        this.lazyInitializer.setUnwrap(unwrap);
    }

    @Override
    public boolean isUnwrap() {
        return this.lazyInitializer.isUnwrap();
    }
    
    @Override
    public Object invoke(
            Object self, 
            Method thisMethod, 
            Method proceed, 
            Object[] args) throws Throwable {
        if (thisMethod.getName().equals("getHibernateLazyInitializer")) {
            return this;
        } else if (thisMethod.equals(this.getIdentifierMethod)) {
            //Very important!!!
            //Hibernate invoke thisMethod.invoke(target, args) here if the proxy is loaded.
            //But babyfish do the same operation in this.getIdentifier(),
            //Becasue the behaviors of getIdentfier() and setIdentifier(Serializable)
            //must be same. (see the comment of next else if)
            return this.getIdentifier();
        } else if (thisMethod.equals(this.setIdentifierMethod)) {
            //Very important!!!
            //Hibernate invoke thisMethod.invoke(target, args) here.
            //But babyfish do the same operation in this.setIdentifier(Serializable),
            //this is very important, because the "ctx.resumeFreezing()" in this.setIdentifier()
            //need the id of the target if the proxy is loaded.
            this.setIdentifier((Serializable)args[0]);
        }
        return ((MethodHandler)this.lazyInitializer).invoke(
                self, 
                thisMethod, 
                proceed, 
                args);
    }
    
    protected void executeModifying(ScalarEvent e) {
        Throwable finalThrowable = null;
        try {
            this.onModifying(e);    
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            this.raiseModifying(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw new ModificationException(false, e, finalThrowable);
        }
    }

    protected void executeModified(ScalarEvent e) {
        Throwable finalThrowable = null;
        try {
            this.raiseModified(e);
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            this.onModified(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw new ModificationException(true, e, finalThrowable);
        }
    }

    protected void onModifying(ScalarEvent e) throws Throwable {
        
    }

    protected void onModified(ScalarEvent e) throws Throwable {
        
    }

    protected void raiseModifying(ScalarEvent e) throws Throwable {
        ScalarListener scalarListener = this.scalarListener;
        if (scalarListener != null) {
            e
            .getAttributeContext(AttributeScope.LOCAL)
            .addAttribute(AK_SCALAR_LISTENER, scalarListener);
            scalarListener.modifying(e);
        }
    }

    protected void raiseModified(ScalarEvent e) throws Throwable {
        ScalarListener scalarListener = 
            (ScalarListener)
            e
            .getAttributeContext(AttributeScope.LOCAL)
            .removeAttribute(AK_SCALAR_LISTENER);
        if (scalarListener != null) {
            scalarListener.modified(e);
        }
    }
    
    private final class TargetScalarListener implements ScalarListener {

        @SuppressWarnings("unchecked")
        @Override
        public void modifying(ScalarEvent e) {
            FrozenLazyInitializerImpl that = FrozenLazyInitializerImpl.this;
            if (!that.disableTargetListener) {
                LazyInitializer rawLazyInitializer = that.lazyInitializer;
                EventAttributeContext localAttributeContext = e.getAttributeContext(AttributeScope.LOCAL);
                if (rawLazyInitializer.isUninitialized() ||
                        ((ObjectModelProvider)rawLazyInitializer.getImplementation())
                        .objectModel() 
                        != 
                        e.getSource()) {
                    ((ObjectModel)e.getSource()).removeScalarListener(this);
                    return;
                }
                localAttributeContext.addAttribute(AK_MODIFIYING_EXECUTED, true);
                ObjectModel ownerObjectModel = ((ObjectModelProvider)that.owner).objectModel();
                JPAModelClass jpaModelClass = (JPAModelClass)ownerObjectModel.getModelClass();
                if (e.getScalarPropertyId() == jpaModelClass.getIdProperty().getId()) {
                    FrozenContext<Object> ctx = (FrozenContext<Object>)that.idFrozenContext;
                    localAttributeContext.addAttribute(AK_FROZENCONTEXT, ctx);
                    FrozenContext.suspendFreezing(ctx, that.owner);
                }
                ScalarListener scalarListener = that.scalarListener;
                if (scalarListener != null) {
                    that.executeModifying(e.dispatch(ownerObjectModel));
                }
            }
        }

        @Override
        public void modified(ScalarEvent e) {
            EventAttributeContext localAttributeContext = e.getAttributeContext(AttributeScope.LOCAL);
            if (localAttributeContext.getAttribute(AK_MODIFIYING_EXECUTED) == null) {
                return;
            }
            FrozenLazyInitializerImpl that = FrozenLazyInitializerImpl.this;
            ObjectModel ownerObjectModel = ((ObjectModelProvider)that.owner).objectModel();
            JPAModelClass jpaModelClass = (JPAModelClass)ownerObjectModel.getModelClass();
            if (e.getScalarPropertyId() == jpaModelClass.getIdProperty().getId()) {
                try {
                    that.disableSetTargetIdentifier = true;
                    try {
                        that.setIdentifier((Serializable)e.getValue(PropertyVersion.ATTACH));
                    } finally {
                        that.disableSetTargetIdentifier = false;
                    }
                } finally {
                    FrozenContext<?> ctx = (FrozenContext<?>)localAttributeContext.getAttribute(AK_FROZENCONTEXT);
                    FrozenContext.resumeFreezing(ctx);
                }
            }
            ScalarEvent dispatchedEvent = e.getDispatchedEvent(ownerObjectModel);
            if (dispatchedEvent != null) {
                that.executeModified(dispatchedEvent);
            }
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(FrozenLazyInitializerImpl.this);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TargetScalarListener)) {
                return false;
            }
            TargetScalarListener other = (TargetScalarListener)obj;
            return FrozenLazyInitializerImpl.this == other.owner();
        }
        
        private Object owner() {
            return FrozenLazyInitializerImpl.this;
        }
        
    }
    
    static {
        
        Field field;
        try {
            field = BasicLazyInitializer.class.getDeclaredField("getIdentifierMethod");
        } catch (NoSuchFieldException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        field.setAccessible(true);
        GET_IDENTIFIER_METHOD_FIELD = field;
        
        try {
            field = BasicLazyInitializer.class.getDeclaredField("setIdentifierMethod");
        } catch (NoSuchFieldException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        field.setAccessible(true);
        SET_IDENTIFIER_METHOD_FIELD = field;
    }
}

