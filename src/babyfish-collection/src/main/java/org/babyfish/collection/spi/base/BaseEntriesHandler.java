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
package org.babyfish.collection.spi.base;

/**
 * @author Tao Chen
 */
public interface BaseEntriesHandler<K, V> {
    
    default Object createAddingArgument(K key, V value) {
        throw new UnsupportedOperationException();
    }
    
    default void adding(K key, V value, Object argument) {
        throw new UnsupportedOperationException();
    }
    
    default void added(K key, V value, Object argument) {
        throw new UnsupportedOperationException();
    }
    
    default Object createChangingArgument(K oldKey, V oldValue, K newKey, V newValue) {
        throw new UnsupportedOperationException();
    }

    default void changing(K oldKey, V oldValue, K newKey, V newValue, Object argument) {
        throw new UnsupportedOperationException();
    }
    
    default void changed(K oldKey, V oldValue, K newKey, V newValue, Object argument) {
        throw new UnsupportedOperationException();
    }
    
    default Object createRemovingArgument(K oldKey, V oldValue) {
        throw new UnsupportedOperationException();
    }

    default void removing(K oldKey, V oldValue, Object argument) {
        throw new UnsupportedOperationException();
    }
    
    default void removed(K oldKey, V oldValue, Object argument) {
        throw new UnsupportedOperationException();
    }
    
    default void setPreThrowable(Object argument, Throwable throwable) {}
    
    default void setNullOrThrowable(Throwable nullOrThrowable) {}
}
