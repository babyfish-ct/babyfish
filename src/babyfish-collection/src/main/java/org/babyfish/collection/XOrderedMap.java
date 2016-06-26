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

import java.util.Collection;

import org.babyfish.collection.XOrderedSet.XOrderedSetView;
import org.babyfish.data.View;

/**
 * @author Tao Chen
 */
public interface XOrderedMap<K, V> extends XMap<K, V> {
    
    boolean headAppend();
    
    OrderAdjustMode accessMode();
    
    OrderAdjustMode replaceMode();
    
    V access(K key);
    
    XOrderedMapView<K, V> descendingMap();

    @Override
    XOrderedKeySetView<K> keySet();

    XOrderedKeySetView<K> descendingKeySet();

    K firstKey();
    
    K lastKey();
    
    XEntry<K, V> firstEntry();
    
    XEntry<K, V> lastEntry();
    
    Entry<K, V> pollFirstEntry();
    
    Entry<K, V> pollLastEntry();
    
    interface XOrderedKeySetView<K> extends XKeySetView<K>, XOrderedSetView<K> {
        
        @Deprecated
        @Override
        boolean add(K e) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        boolean addAll(Collection<? extends K> c) throws UnsupportedOperationException;
        
        @Override
        XOrderedKeySetView<K> descendingSet();
    }
    
    interface XOrderedMapView<K, V> extends XOrderedMap<K, V>, View {
        
    }
    
}
