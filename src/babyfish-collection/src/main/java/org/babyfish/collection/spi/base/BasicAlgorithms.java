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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.babyfish.collection.UnifiedComparator;

/**
 * @author Tao Chen
 */
public class BasicAlgorithms {
    
    protected BasicAlgorithms() {
        throw new UnsupportedOperationException();
    }
    
    public static Object[] collectionToArray(Collection<?> collection) {
        Object[] a = new Object[collection.size()];
        int index = 0;
        for (Object o : collection) {
            a[index++] = o;
        }
        return a;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T[] collectionToArray(Collection<?> collection, T[] array) {
        int size = collection.size();
        Class<?> componentType = array.getClass().getComponentType();
        if (array.length < size) {
            array = componentType == Object.class ?
                    (T[])new Object[size] :
                    (T[])Array.newInstance(componentType, size);
        }
        int index = 0;
        for (Object o : collection) {
            array[index++] = (T)o;
        }
        if (array.length > size) {
            array[size] = null;
        }
        return array;
    }
    
    public static String collectionToString(Collection<?> this_) {
        Iterator<?> itr = this_.iterator();
        if (!itr.hasNext()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        while (true) {
            Object e = itr.next();
            sb.append(e == this_ ? "(this Collection)" : e);
            if (!itr.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }
    
    public static <E> int setHashCode(Set<E> this_) {
        int hash = 0;
        UnifiedComparator<? super E> unifiedComparator = UnifiedComparator.nullToEmpty(UnifiedComparator.of(this_));
        Iterator<E> beItr = this_.iterator();
        while (beItr.hasNext()) {
            E e = beItr.next();
            hash += unifiedComparator.hashCode(e);
        }
        return hash;
    }
    
    @SuppressWarnings("unchecked")
    public static <E> boolean setEquals(Set<E> this_, Object obj) {
        if (this_ == obj) {
            return true;
        }
        if (!(obj instanceof Set<?>)) {
            return false;
        }
        Collection<E> other = (Collection<E>)obj;
        if (this_.size() != other.size()) {
            return false;
        }
        UnifiedComparator<? super E> unifiedComparator = 
                UnifiedComparator.nullToEmpty(UnifiedComparator.of(this_));
        UnifiedComparator<? super E> otherUnifiedComparator = 
                UnifiedComparator.nullToEmpty(UnifiedComparator.of(other));
        try {
            if (unifiedComparator.equals(otherUnifiedComparator)) {
                return this_.containsAll(other);
            }
            return this_.containsAll(other) && other.containsAll(this_);
        } catch (ClassCastException unused)   {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    public static <E> int listHashCode(List<E> this_) {
        int hash = 1;
        UnifiedComparator<? super E> unifiedComparator = 
                UnifiedComparator.nullToEmpty(UnifiedComparator.of(this_));
        for (E e : this_) {
            hash = 31 * hash + (e == null ? 0 : unifiedComparator.hashCode(e));
        }
        return hash;
    }
    
    @SuppressWarnings("unchecked")
    public static <E> boolean listEquals(List<E> this_, Object obj) {
        if (this_ == obj) {
            return true;
        }
        if (!(obj instanceof List<?>)) {
            return false;
        }
        List<E> other = (List<E>)obj;
        UnifiedComparator<? super E> unifiedComparator = 
                UnifiedComparator.nullToEmpty(UnifiedComparator.of(this_));
        UnifiedComparator<? super E> otherUnifiedComparator = 
                UnifiedComparator.nullToEmpty(UnifiedComparator.of(other));
        Iterator<E> itr1 = this_.iterator();
        Iterator<E> itr2 = other.iterator();
        if (unifiedComparator.equals(otherUnifiedComparator)) {
            while(itr1.hasNext() && itr2.hasNext()) {
                if (!unifiedComparator.equals(itr1.next(), itr2.next())) {
                    return false;
                }
            }
        } else {
            while(itr1.hasNext() && itr2.hasNext()) {
                if (!unifiedComparator.equals(itr1.next(), itr2.next()) ||
                        !otherUnifiedComparator.equals(itr2.next(), itr1.next())) {
                    return false;
                }
            }
        }
        return !(itr1.hasNext() || itr2.hasNext());
    }
    
    public static <K, V> int mapHashCode(Map<K, V> this_) {
        int h = 0;
        for (Entry<K, V> e : this_.entrySet()) {
            h += e.hashCode();
        }
        return h;
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> boolean mapEquals(Map<K, V> this_, Object obj) {
        if (this_ == obj) {
            return true;
        }
        if (!(obj instanceof Map<?, ?>)) {
            return false;
        }
        Map<K,V> other = (Map<K,V>)obj;
        if (this_.size() != other.size()) {
            return false;
        }
        if (!containsAll(this_, other)) {
            return false;
        }
        UnifiedComparator<? super K> keyUnifiedComparator = UnifiedComparator.nullToEmpty(UnifiedComparator.of(this_.keySet()));
        UnifiedComparator<? super K> otherKeyUnifiedComparator = UnifiedComparator.nullToEmpty(UnifiedComparator.of(other.keySet()));
        UnifiedComparator<? super V> valueUnifiedComparator = UnifiedComparator.nullToEmpty(UnifiedComparator.of(this_.values()));
        UnifiedComparator<? super V> otherValueUnifiedComparator = UnifiedComparator.nullToEmpty(UnifiedComparator.of(other.values()));
        if (!keyUnifiedComparator.equals(otherKeyUnifiedComparator) ||
                !valueUnifiedComparator.equals(otherValueUnifiedComparator)) {
            if (!containsAll(other, this_)) {
                return false;
            }
        }
        return true;
    }
    
    public static String mapToString(Map<?, ?> this_) {
        Iterator<? extends Entry<?, ?>> i = this_.entrySet().iterator();
        if (!i.hasNext()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        while (true) {
            Entry<?, ?> e = i.next();
            Object key = e.getKey();
            Object value = e.getValue();
            sb.append(key == this_ ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this_ ? "(this Map)" : value);
            if (! i.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }
    
    private static <K, V> boolean containsAll(Map<K, V> a, Map<K, V> b) {
        try {
            for (Entry<K, V> e : a.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(b.get(key)==null && b.containsKey(key))) {
                        return false;
                    }
                } else {
                    if (!value.equals(b.get(key))) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
        return true;
    }
}
