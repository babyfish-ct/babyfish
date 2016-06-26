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
package org.babyfish.hibernate.context.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import org.babyfish.hibernate.XSession;
import org.babyfish.hibernate.context.spi.CurrentXSessionContext;
import org.babyfish.hibernate.internal.XSessionFactoryImplementor;
import org.babyfish.hibernate.internal.XSessionImplementor;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.UncheckedException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.hibernate.engine.jdbc.LobCreationContext;
import org.hibernate.engine.transaction.spi.TransactionContext;
import org.hibernate.event.spi.EventSource;

/**
 * @author Tao Chen
 */
public class ThreadLocalXSessionContext implements CurrentXSessionContext {
    
    private static final long serialVersionUID = 5126346856437291147L;
    
    private RawContext rawContext;
    
    public ThreadLocalXSessionContext(XSessionFactoryImplementor factory) {
        this(new RawContext(factory));
    }
    
    protected ThreadLocalXSessionContext(RawContext rawContext) {
        this.rawContext = Arguments.mustNotBeNull("rawContext", rawContext);
    }

    @Override
    public XSession currentSession() throws HibernateException {
        return (XSession)this.rawContext.currentSession();
    }
    
    protected static class RawContext extends ThreadLocalSessionContext {

        private static final long serialVersionUID = 7954381412144248963L;

        private static final Constructor<?> INVOCATION_HANDLER_CONSTRUCTOR;
        
        private static final Method INVOCATION_HANDLER_SET_WRAPPED;
        
        private static final Class<?>[] SESSION_PROXY_INTERFACES = new Class[] {
                XSession.class,
                XSessionImplementor.class,
                EventSource.class,
                TransactionContext.class,
                LobCreationContext.class
        };
        
        public RawContext(XSessionFactoryImplementor factory) {
            super(factory);
        }
        
        @Override
        protected Session wrap(Session session) {
            InvocationHandler invocationHandler = this.createInvocationHandler(session);
            Session wrapped = (Session)Proxy.newProxyInstance(
                    Session.class.getClassLoader(),
                    SESSION_PROXY_INTERFACES,
                    invocationHandler
                );
            this.setInvocationHandlerWrapped(invocationHandler, wrapped);
            return wrapped;
        }
        
        protected InvocationHandler createInvocationHandler(Session session) {
            try {
                if (INVOCATION_HANDLER_CONSTRUCTOR.getParameterTypes().length == 1) {
                    return (InvocationHandler)INVOCATION_HANDLER_CONSTRUCTOR.newInstance(session);
                }
                return (InvocationHandler)INVOCATION_HANDLER_CONSTRUCTOR.newInstance(this, session);
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new AssertionError(ex);
            } catch (InvocationTargetException ex) {
                throw UncheckedException.rethrow(ex.getTargetException());
            }
        }
        
        protected void setInvocationHandlerWrapped(InvocationHandler invocationHandler, Session session) {
            try {
                INVOCATION_HANDLER_SET_WRAPPED.invoke(invocationHandler, session);
            } catch (IllegalAccessException ex) {
                throw new AssertionError(ex);
            } catch (InvocationTargetException ex) {
                throw UncheckedException.rethrow(ex.getTargetException());
            }
        }

        static {
            Constructor<?> invocationHandlerConstructor = null;
            Method invocationHandlerSetWrapped = null;
            for (Class<?> clazz : ThreadLocalSessionContext.class.getDeclaredClasses()) {
                if (InvocationHandler.class.isAssignableFrom(clazz)) {
                    try {
                        if (Modifier.isStatic(clazz.getModifiers())) {
                            invocationHandlerConstructor = clazz.getDeclaredConstructor(Session.class);
                        } else {
                            invocationHandlerConstructor = clazz.getDeclaredConstructor(
                                    ThreadLocalSessionContext.class, 
                                    Session.class);
                        }
                    } catch (NoSuchMethodException | SecurityException ex) {
                        throw new AssertionError(ex);
                    }
                    try {
                        invocationHandlerSetWrapped = clazz.getDeclaredMethod("setWrapped", Session.class);
                    } catch (NoSuchMethodException | SecurityException ex) {
                        throw new AssertionError(ex);
                    }
                }
                break;
            }
            if (invocationHandlerConstructor == null) {
                throw new AssertionError();
            }
            if (invocationHandlerSetWrapped == null) {
                throw new AssertionError();
            }
            invocationHandlerConstructor.setAccessible(true);
            invocationHandlerSetWrapped.setAccessible(true);
            INVOCATION_HANDLER_CONSTRUCTOR = invocationHandlerConstructor;
            INVOCATION_HANDLER_SET_WRAPPED = invocationHandlerSetWrapped;
        }
    }
}
