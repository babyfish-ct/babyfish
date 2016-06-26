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

import org.babyfish.collection.event.EntryElementModificationAware;
import org.babyfish.collection.event.KeySetElementModificationAware;
import org.babyfish.collection.event.MapElementModificationAware;
import org.babyfish.collection.event.ValuesElementModificationAware;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public interface MAMap<K, V> 
extends XMap<K, V>, MapElementModificationAware<K, V> {
    
    @Override
    MAEntrySetView<K, V> entrySet();
    
    @Override
    MAKeySetView<K, V> keySet();
    
    @Override
    MAValuesView<K, V> values();
    
    @Override
    MAEntry<K, V> entryOfKey(K key);
    
    @Override
    MAEntry<K, V> entryOfValue(V value);
    
    interface MAEntrySetView<K, V> extends MASet<Entry<K, V>>, XEntrySetView<K, V> {
        
        @Deprecated
        @Override
        boolean add(
                Entry<K, V> e) 
        throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        boolean addAll(
                Collection<? extends Entry<K, V>> c) 
        throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void addValidator(
                Validator<Entry<K, V>> validator)
        throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void removeValidator(
                Validator<Entry<K, V>> validator)
        throws UnsupportedOperationException;
        
        @Override
        MAEntrySetIterator<K, V> iterator();
        
        interface MAEntrySetIterator<K, V> extends XEntrySetIterator<K, V>, MAIterator<Entry<K, V>> {
            
            @Override
            MAEntry<K, V> next();
            
        }
        
    }
    
    interface MAKeySetView<K, V> extends 
    MASet<K>, 
    XKeySetView<K>, 
    KeySetElementModificationAware<K, V> {
        
        @Deprecated
        @Override
        boolean add(
                K element) 
        throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        boolean addAll(
                Collection<? extends K> c) 
        throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void addValidator(
                Validator<K> validator)
        throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void removeValidator(
                Validator<K> validator)
        throws UnsupportedOperationException;
        
        @Override
        MAKeySetIterator<K, V> iterator();
        
        @Override
        int hashCode();
        
        @Override
        boolean equals(Object obj);
        
        @Override
        String toString();
        
        interface MAKeySetIterator<K, V> extends MAIterator<K>, KeySetElementModificationAware<K, V> {
            
        }
        
    }
    
    interface MAValuesView<K, V> extends 
    MACollection<V>, 
    XValuesView<V>, 
    ValuesElementModificationAware<K, V> {

        @Deprecated
        @Override
        boolean add(V element) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        boolean addAll(Collection<? extends V> c) throws UnsupportedOperationException;
        
        @Override
        MAValuesIterator<K, V> iterator();
        
        @Override
        int hashCode();
        
        @Override
        boolean equals(Object obj);
        
        @Override
        String toString();
        
        interface MAValuesIterator<K, V> extends MAIterator<V>, ValuesElementModificationAware<K, V> {

        }
    }
    
    interface MAEntry<K, V> extends XEntry<K, V>, EntryElementModificationAware<K, V> {
        
    }
}
