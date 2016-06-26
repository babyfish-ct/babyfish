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
package org.babyfish.collection;

import java.util.NavigableMap;

import org.babyfish.collection.XNavigableSet.XNavigableSetView;

/**
 * @author Tao Chen
 */
public interface XNavigableMap<K, V> extends NavigableMap<K, V>, XSortedMap<K, V> {
    
    @Override
    XEntry<K, V> firstEntry();
    
    @Override
    XEntry<K, V> lastEntry();
    
    @Override
    XEntry<K, V> higherEntry(K key);
    
    @Override
    XEntry<K, V> lowerEntry(K key);
    
    @Override
    XEntry<K, V> floorEntry(K key);
    
    @Override
    XEntry<K, V> ceilingEntry(K key);
    
    @Override
    XNavigableMapView<K, V> descendingMap();
    
    @Override
    XNavigableMapView<K, V> headMap(K toKey);

    @Override
    XNavigableMapView<K, V> tailMap(K fromKey);

    @Override
    XNavigableMapView<K, V> subMap(K fromKey, K toKey);

    @Override
    XNavigableMapView<K, V> headMap(K toKey, boolean inclusive);

    @Override
    XNavigableMapView<K, V> tailMap(K fromKey, boolean inclusive);

    @Override
    XNavigableMapView<K, V> subMap(
            K fromKey, boolean fromInclusive, 
            K toKey, boolean toInclusive);

    @Override
    XNavigableKeySetView<K> keySet();

    @Override
    XNavigableKeySetView<K> navigableKeySet();

    @Override
    XNavigableKeySetView<K> descendingKeySet();

    interface XNavigableKeySetView<K> extends XSortedKeySetView<K>, XNavigableSetView<K> {
        
        @Override
        XNavigableKeySetView<K> descendingSet();
        
        @Override
        XNavigableKeySetView<K> headSet(K toElement);
        
        @Override
        XNavigableKeySetView<K> tailSet(K fromElement);
        
        @Override
        XNavigableKeySetView<K> subSet(K fromElement, K toElement);
        
        @Override
        XNavigableKeySetView<K> headSet(K toElement, boolean inclusive);
        
        @Override
        XNavigableKeySetView<K> tailSet(K fromElement, boolean inclusive);
        
        @Override
        XNavigableKeySetView<K> subSet( 
                K fromElement, boolean fromInclusive, 
                K toElement, boolean toInclusive);  
    }
    
    interface XNavigableMapView<K, V> extends XNavigableMap<K, V>, XSortedMapView<K, V> {
        
    }
}
