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

import org.babyfish.collection.MASortedSet.MASortedSetView;


/**
 * @author Tao Chen
 */
public interface MASortedMap<K, V> extends XSortedMap<K, V>, MAMap<K, V> {
    
    @Override
    MASortedKeySetView<K, V> keySet();

    @Override
    MASortedMapView<K,V> subMap(K fromKey, K toKey);

    @Override
    MASortedMapView<K,V> headMap(K toKey);

    @Override
    MASortedMapView<K,V> tailMap(K fromKey);
    
    interface MASortedKeySetView<K, V> extends MAKeySetView<K, V>, XSortedKeySetView<K>, MASortedSetView<K> {
        
        @Override
        MASortedKeySetView<K, V> headSet(K toElement);
        
        @Override
        MASortedKeySetView<K, V> tailSet(K fromElement);
        
        @Override
        MASortedKeySetView<K, V> subSet(K fromElement, K toElement);
    }
    
    interface MASortedMapView<K, V> extends MASortedMap<K, V>, XSortedMapView<K, V> {
        
    }
    
}
