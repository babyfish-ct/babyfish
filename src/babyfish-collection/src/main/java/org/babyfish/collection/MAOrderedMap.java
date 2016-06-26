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

import org.babyfish.collection.MAOrderedSet.MAOrderedSetView;

/**
 * @author Tao Chen
 */
public interface MAOrderedMap<K, V> extends MAMap<K, V>, XOrderedMap<K, V> {

    @Override
    MAEntry<K, V> firstEntry();

    @Override
    MAEntry<K, V> lastEntry();
    
    @Override
    MAOrderedMapView<K, V> descendingMap();
    
    @Override
    MAOrderedKeySetView<K, V> keySet();
    
    @Override
    MAOrderedKeySetView<K, V> descendingKeySet();
    
    interface MAOrderedMapView<K, V> extends MAOrderedMap<K, V>, XOrderedMapView<K, V> {
        
    }
    
    interface MAOrderedKeySetView<K, V> extends MAKeySetView<K, V>, MAOrderedSetView<K>, XOrderedKeySetView<K> {
        
        @Override
        MAOrderedKeySetView<K, V> descendingSet();
        
        @Override
        MAKeySetIterator<K, V> descendingIterator();
    }
    
}
