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
package org.babyfish.collection.event;

import java.util.EventListener;

import org.babyfish.lang.Delegate;
import org.babyfish.lang.DelegateExceptionHandlingType;

/**
 * @author Tao Chen
 */
@Delegate(DelegateExceptionHandlingType.CONTINUE)
public interface EntryElementListener<K, V> extends EventListener {

    default void modifying(EntryElementEvent<K, V> e) throws Throwable {}
    
    default void modified(EntryElementEvent<K, V> e) throws Throwable {}
    
    static <K, V> EntryElementListener<K, V> combine(
            EntryElementListener<K, V> a, 
            EntryElementListener<K, V> b) {
        throw new UnsupportedOperationException("Instrument required");
    }
    
    static <K, V> EntryElementListener<K, V> remove(
            EntryElementListener<K, V> a, 
            EntryElementListener<K, V> b) {
        throw new UnsupportedOperationException("Instrument required");
    }
}
