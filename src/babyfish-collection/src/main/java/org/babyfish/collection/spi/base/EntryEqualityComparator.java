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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map.Entry;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.HashCalculator;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
public final class EntryEqualityComparator<K, V> implements EqualityComparator<Entry<K, V>>, Serializable {
    
    private static final EntryEqualityComparator<?, ?> EMPTY =
        new EntryEqualityComparator<Object, Object>();
    
    private static final long serialVersionUID = 837604906671932586L;

    private Object keyComparatorOrEqualityComparator;
    
    private Object valueComparatorOrEqualityComparator;
    
    private EntryEqualityComparator() {
        
    }

    private EntryEqualityComparator(
            Object keyComparatorOrEqualityComparator,
            Object valueComparatorOrEqualityComparator) {
        if (keyComparatorOrEqualityComparator == null && valueComparatorOrEqualityComparator == null) {
            throw new IllegalArgumentException(twoComparatorCanNotBeNullSimultaneously());
        }
        Arguments.mustBeInstanceOfAnyOfValue(
                "keyComparatorOrEqualityComparator", 
                keyComparatorOrEqualityComparator, 
                Comparator.class,
                EqualityComparator.class);
        Arguments.mustBeInstanceOfAnyOfValue(
                "valueComparatorOrEqualityComparator", 
                valueComparatorOrEqualityComparator, 
                Comparator.class,
                EqualityComparator.class);
        this.keyComparatorOrEqualityComparator = keyComparatorOrEqualityComparator;
        this.valueComparatorOrEqualityComparator = valueComparatorOrEqualityComparator;
    }
    
    public static <K, V> EntryEqualityComparator<K, V> of(
            Object keyComparatorOrEqualityComparatorOrUnifiedComparator,
            Object valueComparatorOrEqualityComparatorOrUnifiedComparator) {
        Object keyComparatorOrEqualityComparator =
            UnifiedComparator.unwrap(
                    keyComparatorOrEqualityComparatorOrUnifiedComparator);
        Object valueComparatorOrEqualityComparator =
            UnifiedComparator.unwrap(
                    valueComparatorOrEqualityComparatorOrUnifiedComparator);
        if (keyComparatorOrEqualityComparator != null || 
                valueComparatorOrEqualityComparator != null) {
            return new EntryEqualityComparator<K, V>(
                    keyComparatorOrEqualityComparator, 
                    valueComparatorOrEqualityComparator);
        }
        return null;
    }
 
    @SuppressWarnings("unchecked")
    public static <K, V> EntryEqualityComparator<K, V> nullToEmpty(
            EntryEqualityComparator<K, V> entryEqualityComparator) {
        if (entryEqualityComparator == null) {
            return (EntryEqualityComparator<K, V>)EMPTY;
        }
        return entryEqualityComparator;
    }
    
    public static <K, V> EntryEqualityComparator<K, V> emptyToNull(
            EntryEqualityComparator<K, V> entryEqualityComparator) {
        if (EMPTY.equals(entryEqualityComparator)) {
            return null;
        }
        return entryEqualityComparator;
    }

    @Override
    public int hashCode(Entry<K, V> o) {
        if (o == null) {
            return 0;
        }
        return 
            hashCode(this.keyComparatorOrEqualityComparator, o.getKey())
            ^
            hashCode(this.valueComparatorOrEqualityComparator, o.getValue());
    }
    
    @Override
    public boolean equals(Entry<K, V> o1, Entry<K, V> o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return 
            equals(this.keyComparatorOrEqualityComparator, o1.getKey(), o2.getKey())
            &&
            equals(this.valueComparatorOrEqualityComparator, o1.getValue(), o2.getValue());
    }

    @SuppressWarnings("unchecked")
    private static int hashCode(
            Object comparatorOrEqualityComparator, Object obj) {
        if (obj == null) {
            return 0;
        }
        if (comparatorOrEqualityComparator instanceof HashCalculator<?>) {
            return ((HashCalculator<Object>)comparatorOrEqualityComparator).hashCode(obj);
        }
        return obj.hashCode();
    }

    @SuppressWarnings("unchecked")
    private static boolean equals(
            Object comparatorOrEqualityComparator, Object obj1, Object obj2) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        if (comparatorOrEqualityComparator instanceof Comparator<?>) {
            return ((Comparator<Object>)comparatorOrEqualityComparator).compare(obj1, obj2) == 0;
        }
        if (comparatorOrEqualityComparator instanceof EqualityComparator<?>) {
            return ((EqualityComparator<Object>)comparatorOrEqualityComparator).equals(obj1, obj2);
        }
        return obj1.equals(obj2);
    }

    @Override
    public int hashCode() {
        Object keyUnifiedComparator = this.keyComparatorOrEqualityComparator;
        Object valueUnifiedComparator = this.valueComparatorOrEqualityComparator;
        return 
            (keyUnifiedComparator != null ? keyUnifiedComparator.hashCode() : 0)
            ^
            (valueUnifiedComparator != null ? valueUnifiedComparator.hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntryEqualityComparator<?, ?>)) {
            return false;
        }
        EntryEqualityComparator<?, ?> other = (EntryEqualityComparator<?, ?>)obj;
        Object keyComparatorOrEqualityComparator = this.keyComparatorOrEqualityComparator;
        Object valueComparatorOrEqualityComparator = this.valueComparatorOrEqualityComparator;
        return 
            (keyComparatorOrEqualityComparator != null ?
                    keyComparatorOrEqualityComparator.equals(other.keyComparatorOrEqualityComparator) :
                    other.keyComparatorOrEqualityComparator == null)
            &&
            (valueComparatorOrEqualityComparator != null ?
                    valueComparatorOrEqualityComparator.equals(other.valueComparatorOrEqualityComparator) :
                    other.valueComparatorOrEqualityComparator == null);
    }
    
    @I18N
    private static native String twoComparatorCanNotBeNullSimultaneously();   
}
