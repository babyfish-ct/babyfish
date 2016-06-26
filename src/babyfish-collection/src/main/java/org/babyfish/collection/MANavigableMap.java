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

import org.babyfish.collection.MANavigableSet.MANavigableSetView;

/**
 * @author Tao Chen
 */
public interface MANavigableMap<K, V> extends XNavigableMap<K, V>, MASortedMap<K, V> {
    
    @Override
    MAEntry<K, V> firstEntry();

    @Override
    MAEntry<K, V> lastEntry();
    
    @Override
    MAEntry<K, V> floorEntry(K key);

    @Override
    MAEntry<K, V> ceilingEntry(K key);
    
    @Override
    MAEntry<K, V> lowerEntry(K key);

    @Override
    MAEntry<K, V> higherEntry(K key);

    @Override
    MANavigableMapView<K,V> descendingMap();
    
    @Override
    MANavigableKeySetView<K, V> keySet();

    @Override
    MANavigableKeySetView<K, V> navigableKeySet();

    @Override
    MANavigableKeySetView<K, V> descendingKeySet();
    
    @Override
    MANavigableMapView<K,V> headMap(K toKey);

    @Override
    MANavigableMapView<K,V> tailMap(K fromKey);

    @Override
    MANavigableMapView<K,V> subMap(K fromKey, K toKey);

    @Override
    MANavigableMapView<K,V> headMap(K toKey, boolean inclusive);

    @Override
    MANavigableMapView<K,V> tailMap(K fromKey, boolean inclusive);

    @Override
    MANavigableMapView<K,V> subMap(
            K fromKey, boolean fromInclusive,
            K toKey,   boolean toInclusive);

    interface MANavigableKeySetView<K, V> extends MANavigableSetView<K>, XNavigableKeySetView<K>, MASortedKeySetView<K, V> {
                                                
        @Override
        MANavigableKeySetView<K, V> descendingSet();
        
        @Override
        MAKeySetIterator<K, V> descendingIterator();
        
        @Override
        MANavigableKeySetView<K, V> headSet(K toElement);
        
        @Override
        MANavigableKeySetView<K, V> tailSet(K fromElement);
        
        @Override
        MANavigableKeySetView<K, V> subSet(K fromElement, K toElement);
        
        @Override
        MANavigableKeySetView<K, V> headSet(K toElement, boolean inclusive);
        
        @Override
        MANavigableKeySetView<K, V> tailSet(K fromElement, boolean inclusive);
        
        @Override
        MANavigableKeySetView<K, V> subSet(
                K fromElement, boolean fromInclusive, 
                K toElement, boolean toInclusive);
    }
    
    interface MANavigableMapView<K, V> extends MANavigableMap<K, V>, XNavigableMapView<K, V>, MASortedMapView<K, V> {
        
    }
    
}
