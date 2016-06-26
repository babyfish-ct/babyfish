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
package org.babyfish.collection.spi.wrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;

import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollection;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.spi.wrapper.event.AbstractElementEventDispatcher;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.data.AttributeContext;
import org.babyfish.data.DisablityManageable;
import org.babyfish.data.ModificationAware;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.StatefulObject;
import org.babyfish.validator.Validator;
import org.babyfish.validator.Validators;

/**
 * @author Tao Chen
 */
public abstract class AbstractWrapperXCollection<E> extends StatefulObject implements DisablityManageable, XCollection<E> {
    
    private RootData<E> rootData;
    
    private transient XCollection<E> base;
    
    private transient AbstractWrapperXCollection<E> parent;
    
    private transient int expectRootModCount;
    
    private transient ViewInfo viewInfo;
    
    protected AbstractWrapperXCollection(XCollection<E> base) {
        Arguments.mustNotBeInstanceOfValue("this", this, View.class);
        Arguments.mustNotBeInstanceOfValue("base", base, View.class);
        RootData<E> rootData = this.createRootData();
        if (rootData == null) {
            throw new IllegalProgramException(CommonMessages.createRootDataMustReturnNonNull(this.getClass()));
        }
        rootData.rootWrapper = this;
        rootData.setBase(base);
        rootData.onInitialize();
        this.rootData = rootData;
        if (base != null) {
            this.setBase(base);
        }
    }
    
    protected AbstractWrapperXCollection(
            AbstractWrapperXCollection<E> parent, 
            ViewInfo viewInfo) {
        Arguments.mustNotBeNull("parent", parent);
        Arguments.mustBeInstanceOfValue("this", this, View.class);
        Arguments.mustNotBeInstanceOfValue("this", this, Serializable.class);
        this.parent = parent;
        this.rootData = parent.rootData;
        this.viewInfo = viewInfo;
        XCollection<E> base = parent.getBase(true);
        if (base != null) {
            base = this.createBaseView(base, viewInfo);
            if (!(base instanceof View)) {
                throw new IllegalProgramException(CommonMessages.createBaseViewMustReturnView(this.getClass(), View.class));
            }
            this.setBase(base);
        }
    }
    
    /**
     * This method should not be invoked by the customer immediately.
     * 
     * <p>
     * It is used to create the instance during the when 
     * {@link java.io.ObjectInputStream} reads this object from a stream.
     * Although the derived classes of this class may implement {@link java.io.Serializable},
     * but this abstract super class does not implement {@link java.io.Serializable}
     * because it have some derived class that implements {@link View} which can 
     * not be implement {@link java.io.Serializable}
     * </p>
     * 
     * <p>
     * If the derived class is still a class does not implement {@link java.io.Serializable},
     * please support a no arguments constructor and mark it with {@link Deprecated}  too, 
     * like this method.
     * </p>
     */
    @Deprecated
    protected AbstractWrapperXCollection() {
        
    }
    
    protected final <T extends XCollection<E>> T getBase() {
        return this.getBase(false);
    }
    
    @SuppressWarnings("unchecked")
    protected final <T extends XCollection<E>> T getBase(boolean canReturnNull) {
        XCollection<E> base = this.base;
        RootData<E> rootData = this.rootData;
        if (base == null || this.expectRootModCount != rootData.modCount) {
            if (parent == null) {
                base = rootData.getBase(canReturnNull);
            } else {
                base = this.createBaseView(parent.getBase(false), this.viewInfo);
            }
            if (base != null) {
                this.setBase(base);
            }
        }
        return (T)base;
    }
    
    @SuppressWarnings("unchecked")
    protected final <T extends AbstractWrapperXCollection<E>> T getParent() {
        return (T)this.parent;
    }
    
    @SuppressWarnings("unchecked")
    protected final <T extends RootData<E>> T getRootData() {
        return (T)this.rootData;
    }
    
    protected RootData<E> createRootData() {
        return new RootData<E>();
    }
    
    protected XCollection<E> createBaseView(
            XCollection<E> parentBase, 
            ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }
    
    protected void onLoadBase(BaseContext<E> ctx) {
        
    }

    protected void onUnloadBase(BaseContext<E> ctx) {
        
    }
    
    protected AbstractElementEventDispatcher<E> createEventDispatcher() {
        throw new IllegalProgramException(
                CommonMessages.mustOverrideCreateEventDispatcher(
                        this.getClass(), AbstractWrapperXCollection.class)
        );
    }

    @Override
    public boolean isReadWriteLockSupported() {
        return this.rootData instanceof ConstructOnlyRootData && this.rootData.base.isReadWriteLockSupported();
    }

    @Override
    public UnifiedComparator<? super E> unifiedComparator() {
        return this.rootData.unifiedComparator();
    }

    @Override
    public final void addValidator(Validator<E> validator) {
        this.rootData.addValidator(validator);
    }

    @Override
    public final void removeValidator(Validator<E> validator) {
        this.rootData.removeValidator(validator);
    }

    @Override
    public void validate(E e) {
        this.rootData.validate(e);
    }

    @Override
    public final boolean isDisabled() {
        return this.rootData.isDisabled();
    }

    @Override
    public final void disable() {
        this.rootData.setDisabled(true);
    }

    @Override
    public final void enable() {
        this.rootData.setDisabled(false);
    }

    @Override
    public int hashCode() {
        return this.getBase().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this.getBase().equals(o);
    }

    @Override
    public String toString() {
        return this.getBase().toString();
    }

    @Override
    public boolean isEmpty() {
        this.requiredEnabled();
        return this.getBase().isEmpty();
    }

    @Override
    public int size() {
        this.requiredEnabled();
        return this.getBase().size();
    }

    @Override
    public boolean contains(Object o) {
        this.requiredEnabled();
        return this.getBase().contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        this.requiredEnabled();
        return this.getBase().containsAll(c);
    }

    @Override
    public Object[] toArray() {
        this.requiredEnabled();
        return this.getBase().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        this.requiredEnabled();
        return this.getBase().toArray(a);
    }

    @Override
    public boolean add(E e) {
        this.enable();
        return this.getBase().add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        this.enable();
        return this.getBase().addAll(c);
    }

    @Override
    public void clear() {
        this.enable();
        this.getBase().clear();
    }

    @Override
    public boolean remove(Object o) {
        this.enable();
        return this.getBase().remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        this.enable();
        return this.getBase().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        this.enable();
        return this.getBase().retainAll(c);
    }

    @Override
    public XIterator<E> iterator() {
        return new IteratorImpl<E>(this);
    }

    protected final void requiredEnabled() {
        if (this.rootData.disabled) {
            throw new IllegalStateException(
                    CommonMessages.currentCollectionIsDisabled(this.getClass())
            );
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setBase(XCollection<E> base) {
        XCollection<E> oldBase = this.base;
        if (oldBase != base) {
            AbstractElementEventDispatcher<E> dispatcher = null;
            if (this instanceof ModificationAware) {
                dispatcher = this.eventDispatcher();
                Arguments.mustBeInstanceOfValueWhen(
                        CommonMessages.whenThisIsModificationAware(ModificationAware.class), 
                        "base", 
                        base, 
                        MACollection.class);
            }
            BaseContext<E> ctx = new BaseContext<E>(oldBase, base);
            this.onUnloadBase(ctx);
            if (oldBase != null && dispatcher != null) {
                ((MACollection<E>)oldBase).removeElementListener(dispatcher);
            }
            if (oldBase instanceof WrapperCollectionAware) {
                ((WrapperCollectionAware<E>)oldBase).setWrapperCollection(null);
            }
            this.base = base;
            this.expectRootModCount = this.rootData.modCount;
            if (base instanceof WrapperCollectionAware) {
                ((WrapperCollectionAware<E>)base).setWrapperCollection(this);
            }
            if (dispatcher != null) {
                ((MACollection<E>)base).addElementListener(dispatcher);
            }
            this.onLoadBase(ctx);
        }
    }
    
    private AbstractElementEventDispatcher<E> eventDispatcher() {
        AbstractElementEventDispatcher<E> dispatcher = this.createEventDispatcher();
        if (dispatcher == null) {
            throw new IllegalProgramException(
                    CommonMessages.createEventDispatcherMustReturnNonNull(this.getClass())
            );
        }
        if (dispatcher.getOwner() != this) {
            throw new IllegalProgramException(
                    CommonMessages.ownerOfReturnedValueOfCreateEventDispatcherMustBeThis(this.getClass()));
        }
        return dispatcher;
    }
    
    @SuppressWarnings("unchecked")
    protected final void replace(Object base) {
        Arguments.mustNotBeInstanceOfValue("this", this, View.class);
        Arguments.mustNotBeInstanceOfValue("base", base, View.class);
        Arguments.mustBeInstanceOfValue("base", base, XCollection.class);
        
        // Unnecessary but for optimization
        if (this instanceof ModificationAware) {
            MACollection<E> oldBase = (MACollection<E>)this.base;
            if (oldBase != null) {
                oldBase.removeElementListener(this.eventDispatcher());
            }
        }
        
        // Only need to change "root.base", because "this.base" will be 
        // refreshed automatically when "this.getBase()" is called later.
        this.getRootData().setBase((XCollection<E>)base);
    }
    
    @Override
    protected void onWriteState(Output out) throws IOException {
        out.writeObject(this.rootData);
    }

    @Override
    protected void onReadState(Input in) throws ClassNotFoundException, IOException {
        RootData<E> rootData = in.readObject();
        boolean oldDeserializing = rootData.deserializing;
        rootData.deserializing = true;
        try {
            this.rootData = rootData;
            XCollection<E> base = rootData.getBase(true);
            if (base != null) {
                this.setBase(base);
            }
        } finally {
            rootData.deserializing = oldDeserializing;
        }
    }

    protected static final class BaseContext<E> extends AttributeContext {
        
        private static final long serialVersionUID = -4360845189904601127L;

        private XCollection<E> oldBase;
        
        private XCollection<E> newBase;
        
        BaseContext(XCollection<E> oldBase, XCollection<E> newBase) {
            this.oldBase = oldBase;
            this.newBase = newBase;
        }
        
        public XCollection<E> getOldBase() {
            return this.oldBase;
        }
    
        public XCollection<E> getNewBase() {
            return this.newBase;
        }
    }

    protected static abstract class AbstractIteratorImpl<E> implements XIterator<E> {
        
        private AbstractWrapperXCollection<E> parent;
        
        private XIterator<E> base;
        
        private int expectedRootModCount;
        
        public AbstractIteratorImpl(
                AbstractWrapperXCollection<E> parent, 
                ViewInfo viewInfo) {
            Class<?> derivedClass = this.getClass();
            if (derivedClass.getDeclaringClass() != null && !Modifier.isStatic(derivedClass.getModifiers())) {
                throw new IllegalProgramException(
                        "The class of \"" +
                        derivedClass.getName() +
                        "\" must be toppest or static nested class");
            }
            Arguments.mustNotBeNull("parent", parent);
            this.parent = parent;
            XIterator<E> base = this.createBaseView(parent.getBase(), viewInfo);
            if (base == null) {
                throw new IllegalProgramException(
                        );
            }
            this.base = base;
            this.expectedRootModCount = parent.rootData.modCount;
            if (base instanceof MACollection.MAIterator<?>) {
                ((MACollection.MAIterator<E>)base).addElementListener(this.eventDispatcher());
            }
        }
        
        protected abstract XIterator<E> createBaseView(
                XCollection<E> baseParent, 
                ViewInfo viewInfo);
        
        protected AbstractElementEventDispatcher<E> createEventDispatcher() {
            throw new IllegalProgramException(
                    CommonMessages.mustOverrideCreateEventDispatcher(
                            this.getClass(), AbstractIteratorImpl.class)
            );
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractWrapperXCollection<E>> T getParent() {
            return (T)this.parent;
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends XIterator<E>> T getBase() {
            return (T)this.base;
        }

        @Override
        public boolean hasNext() {
            this.checkForComodification();
            this.parent.requiredEnabled();
            return this.base.hasNext();
        }

        @Override
        public E next() {
            this.checkForComodification();
            this.parent.requiredEnabled();
            return this.base.next();
        }

        @Override
        public void remove() {
            this.checkForComodification();
            this.parent.enable();
            this.base.remove();
        }

        @Override
        public boolean isReadWriteLockSupported() {
            return this.base.isReadWriteLockSupported();
        }

        @Override
        public UnifiedComparator<? super E> unifiedComparator() {
            return this.base.unifiedComparator();
        }

        @Override
        public ViewInfo viewInfo() {
            return this.base.viewInfo();
        }
        
        protected void checkForComodification() {
            if (this.expectedRootModCount != this.parent.rootData.modCount) {
                throw new ConcurrentModificationException(CommonMessages.concurrentModification());
            }
        }
        
        private AbstractElementEventDispatcher<E> eventDispatcher() {
            AbstractElementEventDispatcher<E> dispatcher = this.createEventDispatcher();
            if (dispatcher == null) {
                throw new IllegalProgramException(CommonMessages.createEventDispatcherMustReturnNonNull(this.getClass()));
            }
            if (dispatcher.getOwner() != this) {
                throw new IllegalProgramException(CommonMessages.ownerOfReturnedValueOfCreateEventDispatcherMustBeThis(this.getClass()));
            }
            return dispatcher;
        }
    }
    
    protected static class IteratorImpl<E> extends AbstractIteratorImpl<E> {

        public IteratorImpl(AbstractWrapperXCollection<E> parent) {
            super(parent, CollectionViewInfos.iterator());
        }

        @Override
        protected XIterator<E> createBaseView(
                XCollection<E> baseParent, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof CollectionViewInfos.Iterator) {
                return baseParent.iterator();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
    }
    
    protected static class RootData<E> implements Serializable {
        
        private static final long serialVersionUID = 3283383260703820980L;
        
        private AbstractWrapperXCollection<E> rootWrapper;
        
        private XCollection<E> base;
        
        private Validator<E> validator;
        
        private boolean disabled;
        
        private transient int modCount;
        
        private transient boolean deserializing;
        
        public final <T extends XCollection<E>> T getBase() {
            return this.getBase(false);
        }

        @SuppressWarnings("unchecked")
        public final <T extends XCollection<E>> T getBase(boolean canReturnNull) {
            XCollection<E> base = (T)this.base;
            if (base == null) {
                if (this instanceof ConstructOnlyRootData) {
                    throw new IllegalProgramException(
                            CommonMessages.mustSetTheBaseInContstructorWhenTheRootDataImplementsRootData(
                                    ConstructOnlyRootData.class)
                    );
                }
                if (canReturnNull) {
                    return null;
                }
                UnifiedComparator<? super E> defaultUnifiedComparator = 
                        this.getDefaultUnifiedComparator();
                base = this.createDefaultBase(
                        UnifiedComparator.nullToEmpty(
                                defaultUnifiedComparator));
                if (base == null) {
                    throw new IllegalProgramException(CommonMessages.createDefaultBaseMustReturnNonNull(this.getClass()));
                }
                this.setBase(base);
            }
            return (T)base;
        }

        protected void setBase(XCollection<E> base) {
            if (this.deserializing) {
                throw new IllegalStateException(CommonMessages.canNotSetBaseDuringSerializing());
            }
            XCollection<E> oldBase = this.base;
            if (oldBase != null && this instanceof SetOnceOnlyRootData) {
                throw new IllegalStateException(
                        CommonMessages.canNotSetBaseTwiceWhenTheRootDataIsSetOnceOnlyRootData(
                                SetOnceOnlyRootData.class));
            }
            if (base == null && this instanceof ConstructOnlyRootData) {
                throw new IllegalArgumentException(
                        CommonMessages.canNotSetNullBaseWhenTheRootDataIsContructorOnlyRootData(
                                ConstructOnlyRootData.class));
            }
            if (oldBase != base) {
                Arguments.mustNotBeInstanceOfValue("base", "base", View.class);
                if (base != null) {
                    UnifiedComparator<? super E> defaultUnifiedComparator =
                            this.getDefaultUnifiedComparator();
                    if (defaultUnifiedComparator != null) {
                        Arguments.mustBeEqualToOtherWhen(
                                CommonMessages.whenDefaultUnifiedComparatorIsNotNull(), 
                                "base.unifiedComparator()", 
                                base.unifiedComparator(), 
                                "this.getDefaultUnifiedComparator", 
                                defaultUnifiedComparator);
                    }
                }
                Validator<E> validator = this.validator;
                if (oldBase != null) {
                    this.onUnloadTranisentData();
                    if (validator != null) {
                        oldBase.removeValidator(validator);
                    }
                }
                this.base = base;
                this.modCount++;
                if (base != null) {
                    if (validator != null) {
                        base.addValidator(validator);
                        if (this instanceof SetOnceOnlyRootData) {
                            this.validator = null;
                        }
                    }
                    this.onLoadTransientData();
                }
            }
        }

        public final UnifiedComparator<? super E> unifiedComparator() {
            XCollection<E> base = this.base;
            if (base != null) {
                return base.unifiedComparator();
            }
            return UnifiedComparator.nullToEmpty(this.getDefaultUnifiedComparator());
        }
        
        public final void addValidator(Validator<E> validator) {
            XCollection<E> base = this.base;
            if (base == null || !(this instanceof SetOnceOnlyRootData)) {
                this.validator = Validators.combine(this.validator, validator);
            }
            if (base != null) {
                base.addValidator(validator);
            }
        }
        
        public final void removeValidator(Validator<E> validator) {
            XCollection<E> base = this.base;
            if (base == null || !(this instanceof SetOnceOnlyRootData)) {
                this.validator = Validators.remove(this.validator, validator);
            }
            if (base != null) {
                base.removeValidator(validator);
            }
        }
        
        public final void validate(E e) {
            XCollection<E> base = this.base;
            if (base == null) {
                Validator<E> validator = this.validator;
                if (validator != null) {
                    validator.validate(e);
                }
            }
            if (base != null) {
                base.validate(e);
            }
        }

        public final boolean isDisabled() {
            return this.disabled;
        }

        public final void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        @SuppressWarnings("unchecked")
        protected final <T extends AbstractWrapperXCollection<E>> T getRootWrapper() {
            AbstractWrapperXCollection<E> rootWrapper = this.rootWrapper;
            if (rootWrapper == null) {
                throw new IllegalStateException(
                        CommonMessages.invokeGetRootOwnerTooEarlySoThatTheRootOwnerIsNull()
                );
            }
            return (T)rootWrapper;
        }

        protected UnifiedComparator<? super E> getDefaultUnifiedComparator() {
            return null;
        }

        protected XCollection<E> createDefaultBase(UnifiedComparator<? super E> unifiedComparator) {
            Comparator<? super E> comparator = unifiedComparator.comparator();
            if (comparator != null) {
                return new TreeSet<E>(comparator);
            }
            return new HashSet<E>(unifiedComparator.equalityComparator());
        }
        
        public boolean isDispatchable() {
            return true;
        }

        /**
         * Like the constructor, it is used to initialize this object.
         * The {@link #getRootWrapper()} can not be invoked in the constructor,
         * but it can be invoked in this method.
         */
        protected void onInitialize() {
            
        }
        
        protected void onLoadTransientData() {
            
        }
        
        protected void onUnloadTranisentData() {
            
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            this.onLoadTransientData();
        }
    }
}
