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
package org.babyfish.model.spi.reference;

import java.util.Comparator;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.ReferenceComparator;

/**
 * @author Tao Chen
 */
public class KeyedReferenceImpl<K, T> extends ReferenceImpl<T> implements KeyedReference<K, T> {

    private static final long serialVersionUID = -660165376235295118L;

    protected K key;
    
    private Object keyComparator;
    
    public KeyedReferenceImpl() {
        
    }
    
    public KeyedReferenceImpl(K key, T value) {
        super(value);
        this.key = key;
    }
    
    public KeyedReferenceImpl(
            Comparator<? super K> keyComparator, ReferenceComparator<? super T> comparator) {
        super(comparator);
        this.keyComparator = keyComparator;
    }
    
    public KeyedReferenceImpl(
            K key, T value, 
            Comparator<? super K> keyComparator, ReferenceComparator<? super T> comparator) {
        super(value, comparator);
        this.key = key;
        this.keyComparator = keyComparator;
    }
    
    public KeyedReferenceImpl(
            EqualityComparator<? super K> keyComparator, ReferenceComparator<? super T> comparator) {
        super(comparator);
        this.keyComparator = keyComparator;
    }
    
    public KeyedReferenceImpl(
            K key, T value, 
            EqualityComparator<? super K> keyComparator, ReferenceComparator<? super T> comparator) {
        super(value, comparator);
        this.key = key;
        this.keyComparator = keyComparator;
    }
    
    public KeyedReferenceImpl(
            UnifiedComparator<? super K> keyComparator, ReferenceComparator<? super T> comparator) {
        super(comparator);
        this.keyComparator = UnifiedComparator.unwrap(keyComparator);
    }
    
    public KeyedReferenceImpl(
            K key, T value, 
            UnifiedComparator<? super K> keyComparator, ReferenceComparator<? super T> comparator) {
        super(value, comparator);
        this.key = key;
        this.keyComparator = UnifiedComparator.unwrap(keyComparator);
    }

    @Override
    public UnifiedComparator<? super K> keyComparator() {
        return UnifiedComparator.nullToEmpty(UnifiedComparator.of(this.keyComparator));
    }
    
    @Override
    public boolean containsKey(Object key) {
        return this.containsKey(key, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key, boolean absolute) {
        K thisKey = this.getKey(absolute);
        if (thisKey == null) {
            return key == null;
        }
        if (thisKey == key) {
            return true;
        }
        if (this.keyComparator == null) {
            return thisKey.equals(key);
        }
        return this.keyComparator().equals(thisKey, (K)key);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object value, boolean absolute) {
        ReferenceComparator<? super T> comparator = this.comparator();
        if (comparator == null) {
            return this.get(absolute) == value;
        }
        return comparator.same(this.get(absolute), (T)value);
    }

    @Override
    public K getKey() {
        return this.getKey(false);
    }

    @Override
    public K getKey(boolean absolute) {
        if (absolute) {
            return this.key;
        }
        return this.get(true) == null ? null : this.key;
    }

    @Override
    public K setKey(K key) {
        K oldKey = this.key;
        this.key = key;
        return oldKey;
    }
    
    @Override
    public T get(boolean absolute) {
        if (absolute) {
            return super.get(true);
        }
        return this.key == null ? null : super.get(true);
    }
    
    @Override
    public T set(K key, T value) {
        this.key = key;
        return super.set(value);
    }

    @Override
    public int hashCode() {
        K key = this.key;
        return (key == null ? 0 : key.hashCode()) + super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KeyedReference<?, ?>)) {
            return false;
        }
        KeyedReference<?, ?> other = (KeyedReference<?, ?>)obj;
        return 
            Nulls.equals(this.key, other.getKey(true)) && 
            Nulls.equals(this.get(true), other.get(true));
    }

}
