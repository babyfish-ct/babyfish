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

import java.util.SortedMap;

import org.babyfish.collection.XSortedSet.XSortedSetView;
import org.babyfish.data.View;

/**
 * @author Tao Chen
 */
public interface XSortedMap<K, V> extends SortedMap<K, V>, XMap<K, V> {
    
    @Override
    XSortedKeySetView<K> keySet();
    
    @Override
    XSortedMapView<K, V> headMap(K toKey);
    
    @Override
    XSortedMapView<K, V> tailMap(K fromKey);
    
    @Override
    XSortedMapView<K, V> subMap(K fromKey, K toKey);

    interface XSortedKeySetView<K> extends XSortedSetView<K>, XKeySetView<K> {
        
        @Override
        XSortedKeySetView<K> headSet(K toElement);
        
        @Override
        XSortedKeySetView<K> tailSet(K fromElement);
        
        @Override
        XSortedKeySetView<K> subSet(K fromElement, K toElement);
    }
    
    interface XSortedMapView<K, V> extends XSortedMap<K, V>, View {
        
    }
}
