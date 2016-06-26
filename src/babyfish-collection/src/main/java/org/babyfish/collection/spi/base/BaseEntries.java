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

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public interface BaseEntries<K, V> extends BaseContainer {
    
    Object PRESENT = new Object();
    
    void initSpecialHandlerFactory(BaseEntriesSpecialHandlerFactory<K, V> specialHandlerFactory);
    
    BidiType bidiType();
    
    boolean isRoot();
    
    ReplacementRule keyReplacementRule();
    
    UnifiedComparator<? super K> keyUnifiedComparator();
    
    UnifiedComparator<? super V> valueUnifiedComparator();
    
    UnifiedComparator<? super Entry<K, V>> entryUnifiedComparator();
    
    void combineKeyValidator(Validator<K> validator);
    
    void removeKeyValidator(Validator<K> validator);
    
    void validateKey(K key);
    
    void combineValueValidator(Validator<V> validator);
    
    void validateValue(V value);
    
    void removeValueValidator(Validator<V> validator);
    
    int size();
    
    boolean isEmpty();
    
    BaseEntry<K, V> getBaseEntry(Object key);
    
    BaseEntry<K, V> getBaseEntryByValue(V value);
    
    boolean containsEntry(Object e);
    
    boolean containsKey(Object key);
    
    boolean containsValue(Object value);
    
    V put(
            K key, 
            V value, 
            BaseEntriesHandler<K, V> handler);
    
    void putAll(
            Map<? extends K, ? extends V> m, 
            BaseEntriesHandler<K, V> handler);
    
    boolean addAll(
            Collection<? extends K> kc, 
            BaseEntriesHandler<K, V> handler);
    
    void clear(BaseEntriesHandler<K, V> handler);
    
    BaseEntry<K, V> removeByEntry(Object e, BaseEntriesHandler<K, V> handler);
    
    BaseEntry<K, V> removeByKey(Object key, BaseEntriesHandler<K, V> handler);
    
    BaseEntry<K, V> removeByValue(Object value, BaseEntriesHandler<K, V> handler);
    
    boolean removeAllByEntryCollection(Collection<?> ec, BaseEntriesHandler<K, V> handler);
    
    boolean removeAllByKeyCollection(Collection<?> kc, BaseEntriesHandler<K, V> handler);
    
    boolean removeAllByValueCollection(Collection<?> vc, BaseEntriesHandler<K, V> handler);
    
    boolean retainAllByEntryCollection(Collection<?> ec, BaseEntriesHandler<K, V> handler);
    
    boolean retainAllByKeyCollection(Collection<?> kc, BaseEntriesHandler<K, V> handler);
    
    boolean retainAllByValueCollection(Collection<?> vc, BaseEntriesHandler<K, V> handler);
    
    FrozenContextSuspending<K, V> suspendViaFrozenContext(K key);
    
    void resumeViaFronzeContext(FrozenContextSuspending<K, V> suspending);
    
    BaseEntryIterator<K, V> iterator();
}
