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

import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public class FrozenContextSuspending<K, V> {
    
    private K key;
    
    private V value;
    
    private boolean resumed;
    
    protected FrozenContextSuspending(BaseEntry<K, V> be) {
        Arguments.mustNotBeNull("be", be);
        this.key = be.getKey();
        this.value = be.getValue();
    }

    public K getKey() {
        if (this.resumed) {
            throw new IllegalStateException(hasBeenResumed(FrozenContextSuspending.class));
        }
        return this.key;
    }

    public V getValue() {
        if (this.resumed) {
            throw new IllegalStateException(hasBeenResumed(FrozenContextSuspending.class));
        }
        return this.value;
    }
    
    protected void onConflictBaseEntryDeleted(BaseEntry<K, V> conflictBaseEntry) {
        
    }
    
    void resume() {
        this.resumed = true;
    }
    
    @SuppressWarnings("rawtypes")
    private static native String hasBeenResumed(Class<FrozenContextSuspending> frozenContextSuspendingType);
}
