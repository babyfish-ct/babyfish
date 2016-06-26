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
import java.util.Map;

import org.babyfish.data.LockDescriptor;
import org.babyfish.data.View;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public interface XMap<K, V> extends Map<K, V>, LockDescriptor {
    
    /**
     * <div>
     *    The bidiType of current map, it can never be changed after the map is created.
     * </div>
     * 
     * <div>
     *    The bidiType has three choices:
     *    <ul>
     *      <li>
     *          {@link BidiType#NONE}(Default):
     *          This is a simple map, like the map of Java Collection Framework.
     *      </li>
     *      <li>
     *          {@link BidiType#NONNULL_VALUES}:
     *          Except the null values, it looks like the "org.apache.commons.collections4.BidiMap&lt;K, V&gt;,
     *          each non-null value can be inversely mapped to an unique key. 
     *      </li>
     *      <li>
     *          {@link BidiType#ALL_VALUES}:
     *          it fully looks like the "org.apache.commons.collections4.BidiMap&lt;K, v&gt;,
     *          each value can be inversely mapped to an unique key. 
     *          This map enforces the restriction that there is a 1:1 relation between keys and values, 
     *          meaning that multiple keys cannot map to the same value
     *      </li>
     *    </ul>
     * </div>
     * 
     * @return The {@link BidiType} of current map
     */
    BidiType bidiType();
    
    /**
     * <div>
     *  The keyReplacementRule of current map, it can never be changed after the map is created.
     * </div>
     * 
     * <div>
     *  Put new key/value pair into the map, if an old key which equals the new key is already exists in the map,
     *  the value of the existing entry will be replaced absolutely, but should the key of the existing entry be replaced?
     *  In order to resolve this problem, the keyReplacementRule has two choices:
     *  <ul>
     *   <li>
     *    {@link ReplacementRule#NEW_REFERENCE_WIN}(Default):
     *    The key will be replaced, 
     *    <pre>
     *      Map&lt;String, Object&gt; map = new HashMap&lt;&gt;(ReplacementRule.NEW_REFERENCE_WIN);
     *      String a = "a";
     *      String b = new String(a); //a.equals(b), but a != b.
     *      map.put(a, null);
     *      map.put(b, null); //key will be replaced
     *      Assert.assertTrue(map.keySet().iterator().next() == b);
     *    </pre>
     *   </li>
     *   <li>
     *    {@link ReplacementRule#OLD_REFERENCE_WIN}:
     *    The key will never be replaced, like Java Collection Framework 
     *    <pre>
     *      Map&lt;String, Object&gt; map = new HashMap&lt;&gt;(ReplacementRule.NEW_REFERENCE_WIN);
     *      String a = "a";
     *      String b = new String(a); //a.equals(b), but a != b.
     *      map.put(a, null);
     *      map.put(b, null); //key will never be replaced
     *      Assert.assertTrue(map.keySet().iterator().next() == a);
     *    </pre>
     *   </li>
     *  </ul>
     * </div>
     * @return The {@link ReplacementRule} of the keySet of current map
     */
    ReplacementRule keyReplacementRule();
    
    /**
     * <div>
     *  The key UnifiedComparator of current map, it can never be changed after the map is created.
     * </div>
     * 
     * <div>
     *    This method will never return null, if no comparator is specified, it should return {@link UnifiedComparator#empty()}.
     * </div>
     * 
     * <div>
     *  <ul>
     *   <li>
     *    If the current map is {@link java.util.SortedMap}
     *    <ul>
     *      <li>
     *          If this method returns {@link UnifiedComparator#empty()}, that means the key of this element 
     *          must be instance of class {@link Comparable}
     *      </li>
     *      <li>
     *          If the method returns a wrapper of {@link java.util.Comparator}, that means the current map
     *          sorts its elements by that comparator.
     *      </li>
     *      <li>
     *          If the method returns a wrapper of {@link org.babyfish.collection.FrozenComparator}, that means
     *          the current map supports "Unstable Map Keys", the key object can still be changed even if it 
     *          has been added into map, the map will be adjusted automatically when the data of its key object(s) 
     *          is(are) changed.
     *      </li>
     *    </ul>
     *   </li>
     *   <li>
     *    Otherwise
     *    <ul>
     *      <li>
     *          If this method returns {@link UnifiedComparator#empty()}, that means the map uses
     *          the {Object{@link #hashCode()} and {Objet{@link #equals(Object)} of the key object itself,
     *          like Java Collection Framework
     *      </li>
     *      <li>
     *          If the method returns a wrapper of {@link org.babyfish.collection.EqualityComparator}, 
     *          that means the current map will never use the @link Object#hashCode()} and 
     *          {@link Object#equals(Object)} of the key object itself, but uses 
     *          {@link org.babyfish.collection.EqualityComparator#hashCode(Object)} and 
     *          {@link org.babyfish.collection.EqualityComparator#equals(Object, Object)}.
     *          This is very useful, let's see an example that cannot be implemented by Java Collection Framework
     *          <pre>
     *              // java.util.LinkedHashMap + java.util.IdentityHashMap
     *              Map&lt;K, V&gt; map = new {@link org.babyfish.collection.LinkedHashMap}&lt;&gt;(
     *                  {@link org.babyfish.collection.ReferenceEqualityComparator}.&lt;K&gt;getInstance(),
     *                  {@link org.babyfish.collection.ReferenceEqualityComparator}.&lt;V&gt;getInstance()
     *              );
     *          </pre>
     *      </li>
     *      <li>
     *          If the method returns a wrapper of {@link org.babyfish.collection.FrozenEqualityComparator}, 
     *          that means the current map supports "Unstable Map Keys", the key object can still be changed 
     *          even if it has been added into map, the map will be adjusted automatically when the data of 
     *          its key object(s) is changed.
     *      </li>
     *    </ul>
     *   </li>
     *  </ul>
     * </div>
     * 
     * @return The keyUnifiedComparator of the keySet of current map
     */
    UnifiedComparator<? super K> keyUnifiedComparator();
    
    /**
     * <div>
     *  The value UnifiedComparator of current map, it can never be changed after the map is created.
     * </div>
     * 
     * <div>
     *    This method will never return null, if no comparator is specified, it should return {@link UnifiedComparator#empty()}.
     * </div>
     * 
     * @return The keyUnifiedComparator of the values of current map
     */
    UnifiedComparator<? super V> valueUnifiedComparator();
    
    /**
     * <div>
     *    The entry UnifiedComparator of the current map, it isn't specified by the user,
     *  it's determined by {@link #keyUnifiedComparator()} and {@link #valueUnifiedComparator()}
     *  automatically
     * </div>
     * 
     * <div>
     *    This method will never return null, if no comparator is specified, it should return {@link UnifiedComparator#empty()}.
     * </div>
     * 
     * @return The entryUnifiedCoparator of the entrySet of current map
     */
    UnifiedComparator<? super Entry<K, V>> entryUnifiedComparator();
    
    void addKeyValidator(Validator<K> validator);
    
    void removeKeyValidator(Validator<K> validator);
    
    void validateKey(K key);
    
    void addValueValidator(Validator<V> validator);
    
    void removeValueValidator(Validator<V> validator);
    
    void validateValue(V value);
    
    XEntry<K, V> entryOfKey(K key);
    
    XEntry<K, V> entryOfValue(V value);
    
    @Override
    XEntrySetView<K, V> entrySet();
    
    @Override
    XKeySetView<K> keySet();
    
    @Override
    XValuesView<V> values();
    
    interface XEntrySetView<K, V> extends XSet<Entry<K, V>>, View {
        
        @Deprecated
        @Override
        boolean add(Entry<K, V> e) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        boolean addAll(Collection<? extends Entry<K, V>> c) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void addValidator(Validator<Entry<K, V>> validator) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void removeValidator(Validator<Entry<K, V>> validator) throws UnsupportedOperationException;
        
        @Override
        XEntrySetIterator<K, V> iterator();
        
        interface XEntrySetIterator<K, V> extends XIterator<Entry<K, V>> {
            
            @Override
            XEntry<K, V> next();
            
        }
    }
    
    interface XKeySetView<K> extends XSet<K>, View {
        
        @Deprecated
        @Override
        boolean add(K e) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        boolean addAll(Collection<? extends K> c) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void addValidator(Validator<K> validator) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void removeValidator(Validator<K> validator) throws UnsupportedOperationException;
    }
    
    interface XValuesView<V> extends XCollection<V>, View {
        
        @Deprecated
        @Override
        boolean add(V e) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        boolean addAll(Collection<? extends V> c) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void addValidator(Validator<V> validator) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void removeValidator(Validator<V> validator) throws UnsupportedOperationException;
    }
    
    interface XEntry<K, V> extends Entry<K, V>, LockDescriptor, View {
        
        UnifiedComparator<? super K> keyUnifiedComparator();
        
        UnifiedComparator<? super V> valueUnifiedComparator();
        
        UnifiedComparator<? super Entry<K, V>> unifiedComparator();
        
        boolean isAlive();
    }
}
