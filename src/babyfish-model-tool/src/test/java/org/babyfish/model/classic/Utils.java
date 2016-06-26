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
package org.babyfish.model.classic;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

public class Utils {

    @SuppressWarnings("unchecked")
    public static <E> void assertCollection(Collection<E> c, E ... elements) {
        Assert.assertEquals(elements.length, c.size());
        int index = 0;
        for (E e : elements) {
            Assert.assertEquals(elements[index++], e);
        }
    }
    
    public static <K, V> void assertMap(Map<K, V> m, K[] keys, V[] values) {
        Assert.assertEquals(keys.length, m.size());
        Assert.assertEquals(values.length, m.size());
        int index = 0;
        for (Entry<K, V> e : m.entrySet()) {
            Assert.assertEquals(keys[index], e.getKey());
            Assert.assertEquals(values[index], e.getValue());
            index++;
        }
    }
    
    public static <E> Iterator<E> locateIterator(Collection<E> c, E target) {
        Iterator<E> itr = c.iterator();
        while (itr.hasNext()) {
            if (itr.next() == target) {
                return itr;
            }
        }
        throw new IllegalArgumentException("\"target\" does not exist");
    }
    
    public static <K, V> Iterator<Entry<K, V>> locateIterator(Map<K, V> m, K targetKey) {
        Iterator<Entry<K, V>> itr = m.entrySet().iterator();
        while (itr.hasNext()) {
            if (itr.next().getKey() == targetKey) {
                return itr;
            }
        }
        throw new IllegalArgumentException("\"targetKey\" does not exist");
    }
    
    public static <K, V> Entry<K, V> locateEntry(Map<K, V> m, K targetKey) {
        Iterator<Entry<K, V>> itr = m.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<K, V> entry = itr.next();
            if (entry.getKey() == targetKey) {
                return entry;
            }
        }
        throw new IllegalArgumentException("\"targetKey\" does not exist");
    }
    
    private Utils() {}
}
