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

import java.util.Comparator;

/**
 * @author Tao Chen
 */
public interface NavigableBaseEntries<K, V> extends DescendingBaseEntries<K, V> {
    
    Comparator<? super K> comparator();
    
    /**
     * @return The range of this object
     * If this {@link NavigableBaseEntries} is root entries, it always is null.
     * otherwise, this.comparator() == this.range().comparator()
     */
    NavigableRange<K> range();
    
    BaseEntry<K, V> first();
    
    BaseEntry<K, V> last();
    
    BaseEntry<K, V> floor(K key);
    
    BaseEntry<K, V> ceiling(K key);
    
    BaseEntry<K, V> lower(K key);
    
    BaseEntry<K, V> higher(K key);
    
    BaseEntry<K, V> pollFirst(BaseEntriesHandler<K, V> handler);
    
    BaseEntry<K, V> pollLast(BaseEntriesHandler<K, V> handler);

    @Override
    NavigableBaseEntries<K, V> descendingEntries();

    NavigableBaseEntries<K, V> subEntries(
            boolean hasFrom, K from, boolean fromInclusive, 
            boolean hasTo, K to, boolean toInclusive);
}
