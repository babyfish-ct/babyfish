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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Tao Chen
 */
public class Equality<T> {
    
    private static final Map<Class<?>, Class<?>[]> CACHE = new WeakHashMap<>();
    
    private static final ReadWriteLock CACHE_LOCK =
        new ReentrantReadWriteLock();
    
    private static final Class<?>[] EMPTY_OE_TYPES = new Class[0];
    
    private static final Class<?>[] ONE_OBJECT_TYPE_ARRAY = new Class[] { Object.class };
    
    private int returnValue;
    
    private T other;

    private Equality(boolean returnValue) {
        this.returnValue = returnValue ? +1 : -1;
    }
    
    private Equality(T other) {
        this.other = other;
    }
    
    public boolean returnValue() {
        if (this.returnValue == 0) {
            throw new IllegalStateException(
                    illegalStateToGetReturnValue(Equality.class)
            );
        }
        return this.returnValue > 0;
    }
    
    public T other() {
        return this.other;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Equality<T> of(Class<T> clazz, T thiz, Object obj) {
        if (Arguments.mustNotBeNull("thiz", thiz) == obj) {
            return new Equality<>(true);
        } 
        if (obj == null) {
            return new Equality<>(false);
        }
        Class<? extends Object> thisClass = thiz.getClass();
        Arguments.mustBeInstanceOfOther("thisObj", thiz, "clazz", clazz);
        Class<? extends Object> objectClass = obj.getClass();
        if (thisClass == objectClass) {
            return new Equality<>((T)obj);
        }
        if (!clazz.isAssignableFrom(objectClass)) {
            return new Equality<>(false);
        }
        Class<?>[] expectedOETypes = Equality.getOETypes(thisClass);
        Class<?>[] actualOETypes = Equality.getOETypes(objectClass);
        if (expectedOETypes.length != actualOETypes.length) {
            return new Equality<>(false);
        }
        for (int i = expectedOETypes.length - 1; i >= 0; i--) {
            if (expectedOETypes[i] != actualOETypes[i]) {
                return new Equality<>(false);
            }
        }
        return new Equality<>((T)obj);
    }
    
    /*
     * Note: 
     * Though this method is fast, but it still is slower than doing nothing.
     * 
     * In order to avoid this cache operation, maybe in next version, 
     * babyfish can support a hack tool to change the byte-code of java.lang.Class 
     * so that java.lang.Class can use a private field to storage the oeTypes directly
     * (Of course, this functionality can work normally either the JDK is hacked or not).
     */
    private static Class<?>[] getOETypes(Class<?> clazz) {
        Class<?>[] oeTypes;
        Lock lock;
        
        (lock = CACHE_LOCK.readLock()).lock();
        try {
            oeTypes = CACHE.get(clazz); //1st reading
        } finally {
            lock.unlock();
        }
        
        if (oeTypes == null) { //1st checking
            (lock = CACHE_LOCK.writeLock()).lock();
            try {
                oeTypes = CACHE.get(clazz); //2nd reading
                if (oeTypes == null) { //2nd checking
                    oeTypes = getOETypes0(clazz);
                    CACHE.put(clazz, oeTypes);
                }
            } finally {
                lock.unlock();
            }
        }
        return oeTypes;
    }
    
    private static Class<?>[] getOETypes0(Class<?> clazz) {
        Set<Class<?>> classes = new HashSet<Class<?>>(16);
        collectOverrideEqualityClasses(clazz, classes);
        if (classes.isEmpty()) {
            return EMPTY_OE_TYPES;
        }
        Class<?>[] arr = classes.toArray(new Class[classes.size()]);
        int len = arr.length;
        int fragmentCount = 0; 
        for (int i = len - 1; i >= 0; i--) {
            Class<?> c1 = arr[i];
            for (int ii = i - 1; ii >= 0; ii--) {
                Class<?> c2 = arr[ii];
                if (c1 != null && c2 != null) {
                    if (c1.isAssignableFrom(c2)) {
                        arr[i] = null;
                        fragmentCount++;
                    } else if (c2.isAssignableFrom(c1)) {
                        arr[ii] = null;
                        fragmentCount++;
                    }
                }
            }
        }
        if (fragmentCount != 0) {
            Class<?>[] compressedArr = new Class[len - fragmentCount];
            int index = 0;
            for (Class<?> cls : arr) {
                if (cls != null) {
                    compressedArr[index++] = cls;
                }
            }
            arr = compressedArr;
        }
        return arr.length == 0 ? EMPTY_OE_TYPES : arr;
    }
    
    private static void collectOverrideEqualityClasses(
            Class<?> clazz, Set<Class<?>> classes) {
        if (clazz != Object.class) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(OverrideEquality.class)) {
                    validateEqualsMethod(method);
                    classes.add(clazz);
                    return;
                }
            }
            if (!clazz.isInterface() && !clazz.isAnnotation()) {
                collectOverrideEqualityClasses(clazz.getSuperclass(), classes);
            }
            for (Class<?> interfacze : clazz.getInterfaces()) {
                collectOverrideEqualityClasses(interfacze, classes);
            }
        }
    }
    
    private static void validateEqualsMethod(Method method) {
        if (
                !Modifier.isPublic(method.getModifiers()) ||
                Modifier.isStatic(method.getModifiers()) ||
                !"equals".equals(method.getName()) ||
                boolean.class != method.getReturnType() ||
                !Arrays.equals(method.getParameterTypes(), ONE_OBJECT_TYPE_ARRAY)) {
            throw new IllegalProgramException(
                    invalidOverrideEqualityMethod(method, OverrideEquality.class)
            );
        }
    }
    
    @I18N
    private static native String invalidOverrideEqualityMethod(
                Method method,
                Class<OverrideEquality> overrideEqualtiyTypeConstant);
        
    @SuppressWarnings("rawtypes")
    @I18N
    private static native String illegalStateToGetReturnValue(
            Class<Equality> equalityTypeConstant);
}
