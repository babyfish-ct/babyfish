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
package org.babyfish.lang;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Tao Chen
 */
public abstract class Singleton {
    
    private static final Object[] EMPTY_ARGS = new Object[0];
    
    private static final Map<Class<?>, Singleton> CACHE = new WeakHashMap<>();
    
    private static boolean enableConstructor;
    
    private static final ReadWriteLock GLOBAL_LOCK = new ReentrantReadWriteLock();
    
    private Replacement replacment = new Replacement(this);
    
    /**
     * @exception IllegalProgramException The derived class of this class can not be used to
     * create instance immediately, only creating instance via {@link #getInstance(Class)} is 
     * allowed. This exception will be raised if any instance is created immediately.
     */
    protected Singleton() {
        Lock lock;
        (lock = GLOBAL_LOCK.writeLock()).lock();
        try {
            if (!enableConstructor) {
                throw new IllegalProgramException(
                        illegalConstructorInvocation(
                                this.getClass(), 
                                Singleton.class
                        )
                );
            }
            /*
             * Very important!!!
             * Forbid the constructor in the derived class of this singleton 
             * to create other singleton object by constructor immediately.
             */
            enableConstructor = false;
        } finally {
            lock.unlock();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Singleton> T getInstance(Class<T> clazz) {
        Singleton singleton;
        Lock lock;
        
        (lock = GLOBAL_LOCK.readLock()).lock(); //1st locking
        try {
            singleton = CACHE.get(clazz); //1st reading
        } finally {
            lock.unlock();
        }
        
        if (singleton == null) { //1st checking
            (lock = GLOBAL_LOCK.writeLock()).lock(); //2nd locking
            try {
                singleton = (T)CACHE.get(clazz); //2nd reading
                if (singleton == null) { //2nd checking
                    singleton = (T)createInstance(clazz);
                    CACHE.put(clazz, singleton);
                }
            } finally {
                lock.unlock();
            }
        }
        
        return (T)singleton;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object createInstance(Class<? extends Singleton> clazz) {
        Arguments.mustNotBeNull("clazz", clazz);
        Arguments.mustBeCompatibleWithValue("clazz", clazz, Singleton.class);
        Arguments.mustNotBeCompatibleWithValue("clazz", clazz, Externalizable.class);
        Arguments.mustNotBeAbstract("clazz", clazz);
        Constructor<?> constructor = null;
        for (Constructor<?> cons : clazz.getDeclaredConstructors()) {
            if (cons.getParameterTypes().length == 0) {
                constructor = cons;
                break;
            }
        }
        if (constructor == null) {
            throw new IllegalProgramException(noDefaultConstructor(clazz));
        }
        if (Modifier.isPublic(constructor.getModifiers())) {
            throw new IllegalProgramException(defaultConstructorMustNotBePublic(clazz));
        }
        for (Class<? extends Throwable> exceptionType : (Class[])constructor.getExceptionTypes()) {
            if (!UncheckedException.isRuntimeExceptionOrError(exceptionType)) {
                throw new IllegalProgramException(
                        defaultConstructorThrowsCheckedException(
                                clazz, 
                                (Class)exceptionType
                        )
                );
            }
        }
        try {
            constructor.setAccessible(true);
            enableConstructor = true;
            try {
                return constructor.newInstance(EMPTY_ARGS);
            } finally {
                enableConstructor = false;
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new AssertionError(ex);
        } catch (InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex.getTargetException());
        }
    }
    
    protected final Object writeReplace() throws ObjectStreamException {
        return this.replacment;
    }
    
    @I18N
    private static native String illegalConstructorInvocation(
            Class<? extends Singleton> implType,
            Class<Singleton> singleType);
    
    @I18N
    private static native String noDefaultConstructor(
            Class<? extends Singleton> implType);
    
    @I18N
    private static native String defaultConstructorMustNotBePublic(
            Class<? extends Singleton> implType);
    
    @I18N
    private static native String defaultConstructorThrowsCheckedException(
            Class<? extends Singleton> implType,
            Class<? extends Exception> checkedExceptionType);
    
    private static class Replacement implements Serializable {

        private static final long serialVersionUID = -7830755086568979711L;
        
        private Singleton target;

        Replacement(Singleton target) {
            this.target = target;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(this.target.getClass());
        }
        
        @SuppressWarnings("unchecked")
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            Class<? extends Singleton> clazz = (Class<? extends Singleton>)in.readObject();
            this.target = getInstance(clazz);
        }
        
        private Object readResolve() throws ObjectStreamException {
            return this.target;
        }
    }
}
