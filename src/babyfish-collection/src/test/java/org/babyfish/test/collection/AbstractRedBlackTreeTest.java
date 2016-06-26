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
package org.babyfish.test.collection;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import org.babyfish.collection.XNavigableMap;
import org.babyfish.collection.spi.base.RedBlackTreeEntries;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public abstract class AbstractRedBlackTreeTest {
    
    protected abstract <K, V> XNavigableMap<K, V> createXNavigableMap(Map<K, V> map);

    @Test
    public void testAscendingWithJDKTreeMap() {
        this.testWithJDKTreeMap(false);
    }
    
    @Test
    public void testDescendingWithJDKTreeMap() {
        this.testWithJDKTreeMap(true);
    }
    
    @Test
    public void testSubTree() {
        
        NavigableMap<Character, Character> allCharMap = 
            new TreeMap<Character, Character>();
        NavigableMap<Character, Character> littleCharMap = 
            new TreeMap<Character, Character>();
        for (char c = 'A'; c <= 'Z'; c++) {
            allCharMap.put(c, Character.toLowerCase(c));
        }
        littleCharMap.put('A', 'a');
        littleCharMap.put('H', 'h');
        littleCharMap.put('O', 'o');
        littleCharMap.put('U', 'u');
        
        NavigableMap<Character, Character> xTreeMap = this.<Character, Character>createXNavigableMap(allCharMap);
        NavigableMap<Character, Character> a2n = xTreeMap.subMap('A', true, 'N', true);
        NavigableMap<Character, Character> o2z = xTreeMap.subMap('O', true, 'Z', true);
        NavigableMap<Character, Character> g2a = a2n.descendingMap().subMap('H', false, 'A', true);
        NavigableMap<Character, Character> n2h = a2n.descendingMap().subMap('N', true, 'G', false);
        NavigableMap<Character, Character> t2o = o2z.descendingMap().subMap('U', false, 'O', true);
        NavigableMap<Character, Character> z2u = o2z.descendingMap().subMap('Z', true, 'T', false);
        NavigableMap<Character, Character> a2g = g2a.descendingMap();
        NavigableMap<Character, Character> h2n = n2h.descendingMap();
        NavigableMap<Character, Character> o2t = t2o.descendingMap();
        NavigableMap<Character, Character> u2z = z2u.descendingMap();
        NavigableMap<Character, Character> a2c = a2g.subMap('A', true, 'C', true);
        NavigableMap<Character, Character> h2j = h2n.subMap('H', true, 'K', false);
        NavigableMap<Character, Character> o2q = o2t.subMap('O', true, 'Q', true);
        NavigableMap<Character, Character> u2w = u2z.subMap('U', true, 'X', false);
        NavigableMap<Character, Character> c2a = a2c.descendingMap();
        NavigableMap<Character, Character> j2h = h2j.descendingMap();
        NavigableMap<Character, Character> q2o = o2q.descendingMap();
        NavigableMap<Character, Character> w2u = u2w.descendingMap();
        
        Assert.assertTrue(a2g.containsKey('D'));
        Assert.assertTrue(g2a.containsKey('D'));
        Assert.assertFalse(a2c.containsKey('D'));
        Assert.assertFalse(c2a.containsKey('D'));
        Assert.assertTrue(h2n.containsKey('K'));
        Assert.assertTrue(n2h.containsKey('K'));
        Assert.assertFalse(h2j.containsKey('K'));
        Assert.assertFalse(j2h.containsKey('K'));
        Assert.assertTrue(o2t.containsKey('R'));
        Assert.assertTrue(t2o.containsKey('R'));
        Assert.assertFalse(o2q.containsKey('R'));
        Assert.assertFalse(q2o.containsKey('R'));
        Assert.assertTrue(u2z.containsKey('X'));
        Assert.assertTrue(z2u.containsKey('X'));
        Assert.assertFalse(u2w.containsKey('X'));
        Assert.assertFalse(w2u.containsKey('X'));
        
        Assert.assertTrue(a2g.containsValue('d'));
        Assert.assertTrue(g2a.containsValue('d'));
        Assert.assertFalse(a2c.containsValue('d'));
        Assert.assertFalse(c2a.containsValue('d'));
        Assert.assertTrue(h2n.containsValue('k'));
        Assert.assertTrue(n2h.containsValue('k'));
        Assert.assertFalse(h2j.containsValue('k'));
        Assert.assertFalse(j2h.containsValue('k'));
        Assert.assertTrue(o2t.containsValue('r'));
        Assert.assertTrue(t2o.containsValue('r'));
        Assert.assertFalse(o2q.containsValue('r'));
        Assert.assertFalse(q2o.containsValue('r'));
        Assert.assertTrue(u2z.containsValue('x'));
        Assert.assertTrue(z2u.containsValue('x'));
        Assert.assertFalse(u2w.containsValue('x'));
        Assert.assertFalse(w2u.containsValue('x'));
        
        Assert.assertEquals("ABCDEFGHIJKLMN", valuesToString(a2n.keySet()));
        Assert.assertEquals("OPQRSTUVWXYZ", valuesToString(o2z.keySet()));
        Assert.assertEquals("GFEDCBA", valuesToString(g2a.keySet()));
        Assert.assertEquals("NMLKJIH", valuesToString(n2h.keySet()));
        Assert.assertEquals("TSRQPO", valuesToString(t2o.keySet()));
        Assert.assertEquals("ZYXWVU", valuesToString(z2u.keySet()));
        Assert.assertEquals("ABCDEFG", valuesToString(a2g.keySet()));
        Assert.assertEquals("HIJKLMN", valuesToString(h2n.keySet()));
        Assert.assertEquals("OPQRST", valuesToString(o2t.keySet()));
        Assert.assertEquals("UVWXYZ", valuesToString(u2z.keySet()));
        Assert.assertEquals("ABC", valuesToString(a2c.keySet()));
        Assert.assertEquals("HIJ", valuesToString(h2j.keySet()));
        Assert.assertEquals("OPQ", valuesToString(o2q.keySet()));
        Assert.assertEquals("UVW", valuesToString(u2w.keySet()));
        Assert.assertEquals("CBA", valuesToString(c2a.keySet()));
        Assert.assertEquals("JIH", valuesToString(j2h.keySet()));
        Assert.assertEquals("QPO", valuesToString(q2o.keySet()));
        Assert.assertEquals("WVU", valuesToString(w2u.keySet()));
        
        Assert.assertEquals(14, a2n.size());
        Assert.assertEquals(12, o2z.size());
        Assert.assertEquals(7, g2a.size());
        Assert.assertEquals(7, n2h.size());
        Assert.assertEquals(6, t2o.size());
        Assert.assertEquals(6, z2u.size());
        Assert.assertEquals(7, a2g.size());
        Assert.assertEquals(7, h2n.size());
        Assert.assertEquals(6, o2t.size());
        Assert.assertEquals(6, u2z.size());
        Assert.assertEquals(3, a2c.size());
        Assert.assertEquals(3, h2j.size());
        Assert.assertEquals(3, o2q.size());
        Assert.assertEquals(3, u2w.size());
        Assert.assertEquals(3, c2a.size());
        Assert.assertEquals(3, j2h.size());
        Assert.assertEquals(3, q2o.size());
        Assert.assertEquals(3, w2u.size());
        
        Assert.assertFalse(a2n.isEmpty());
        Assert.assertFalse(o2z.isEmpty());
        Assert.assertFalse(g2a.isEmpty());
        Assert.assertFalse(n2h.isEmpty());
        Assert.assertFalse(t2o.isEmpty());
        Assert.assertFalse(z2u.isEmpty());
        Assert.assertFalse(a2g.isEmpty());
        Assert.assertFalse(h2n.isEmpty());
        Assert.assertFalse(o2t.isEmpty());
        Assert.assertFalse(u2z.isEmpty());
        Assert.assertFalse(a2c.isEmpty());
        Assert.assertFalse(h2j.isEmpty());
        Assert.assertFalse(o2q.isEmpty());
        Assert.assertFalse(u2w.isEmpty());
        Assert.assertFalse(c2a.isEmpty());
        Assert.assertFalse(j2h.isEmpty());
        Assert.assertFalse(q2o.isEmpty());
        Assert.assertFalse(w2u.isEmpty());
        
        Iterator<Entry<Character, Character>> iterator;
        iterator = a2c.entrySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        Assert.assertEquals("DEFGHIJKLMNOPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        Assert.assertEquals(23, xTreeMap.size());
        Assert.assertEquals(4, a2g.size());
        Assert.assertEquals(4, g2a.size());
        Assert.assertEquals(0, a2c.size());
        Assert.assertEquals(0, c2a.size());
        Assert.assertFalse(xTreeMap.isEmpty());
        Assert.assertFalse(a2g.isEmpty());
        Assert.assertFalse(g2a.isEmpty());
        Assert.assertTrue(a2c.isEmpty());
        Assert.assertTrue(c2a.isEmpty());
        
        iterator = j2h.entrySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        Assert.assertEquals("DEFGKLMNOPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        Assert.assertEquals(20, xTreeMap.size());
        Assert.assertEquals(4, h2n.size());
        Assert.assertEquals(4, n2h.size());
        Assert.assertEquals(0, h2j.size());
        Assert.assertEquals(0, j2h.size());
        Assert.assertFalse(xTreeMap.isEmpty());
        Assert.assertFalse(h2n.isEmpty());
        Assert.assertFalse(n2h.isEmpty());
        Assert.assertTrue(h2j.isEmpty());
        Assert.assertTrue(j2h.isEmpty());
        
        iterator = o2q.entrySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        Assert.assertEquals("DEFGKLMNRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        Assert.assertEquals(17, xTreeMap.size());
        Assert.assertEquals(3, o2t.size());
        Assert.assertEquals(3, t2o.size());
        Assert.assertEquals(0, o2q.size());
        Assert.assertEquals(0, q2o.size());
        Assert.assertEquals("DEFGKLMNRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        Assert.assertFalse(xTreeMap.isEmpty());
        Assert.assertFalse(o2t.isEmpty());
        Assert.assertFalse(t2o.isEmpty());
        Assert.assertTrue(o2q.isEmpty());
        Assert.assertTrue(q2o.isEmpty());
        
        iterator = w2u.entrySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        Assert.assertEquals("DEFGKLMNRSTXYZ", valuesToString(xTreeMap.keySet()));
        Assert.assertEquals(14, xTreeMap.size());
        Assert.assertEquals(3, u2z.size());
        Assert.assertEquals(3, z2u.size());
        Assert.assertEquals(0, u2w.size());
        Assert.assertEquals(0, w2u.size());
        Assert.assertFalse(xTreeMap.isEmpty());
        Assert.assertFalse(u2z.isEmpty());
        Assert.assertFalse(z2u.isEmpty());
        Assert.assertTrue(u2w.isEmpty());
        Assert.assertTrue(w2u.isEmpty());
        
        xTreeMap.clear();
        xTreeMap.putAll(allCharMap);
        Assert.assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        a2n.entrySet().removeAll(littleCharMap.entrySet());
        Assert.assertEquals("BCDEFGIJKLMNOPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        Assert.assertEquals("BCDEFGIJKLMN", valuesToString(a2n.keySet()));
        
        xTreeMap.clear();
        xTreeMap.putAll(allCharMap);
        Assert.assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        a2n.entrySet().removeAll(allCharMap.entrySet());
        Assert.assertEquals("OPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        Assert.assertEquals("", valuesToString(a2n.keySet()));
        
        xTreeMap.clear();
        xTreeMap.putAll(allCharMap);
        Assert.assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        a2n.keySet().removeAll(littleCharMap.keySet());
        Assert.assertEquals("BCDEFGIJKLMNOPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        Assert.assertEquals("BCDEFGIJKLMN", valuesToString(a2n.keySet()));
        
        xTreeMap.clear();
        xTreeMap.putAll(allCharMap);
        Assert.assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        a2n.keySet().removeAll(allCharMap.keySet());
        Assert.assertEquals("OPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        Assert.assertEquals("", valuesToString(a2n.keySet()));
        
        xTreeMap.clear();
        xTreeMap.putAll(allCharMap);
        Assert.assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        a2n.values().removeAll(littleCharMap.values());
        Assert.assertEquals("BCDEFGIJKLMNOPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        Assert.assertEquals("BCDEFGIJKLMN", valuesToString(a2n.keySet()));
        
        xTreeMap.clear();
        xTreeMap.putAll(allCharMap);
        Assert.assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        a2n.values().removeAll(allCharMap.values());
        Assert.assertEquals("OPQRSTUVWXYZ", valuesToString(xTreeMap.keySet()));
        Assert.assertEquals("", valuesToString(a2n.keySet()));
    }

    private void testWithJDKTreeMap(boolean descending) {
        NavigableMap<Integer, Object> jdkAscTreeMap = new TreeMap<>();
        NavigableMap<Integer, Object> jdkTreeMap = jdkAscTreeMap;
        XNavigableMap<Integer, Object> xAscTreeMap = this.<Integer, Object>createXNavigableMap(null);
        XNavigableMap<Integer, Object> xTreeMap = xAscTreeMap;
        
        if (descending) {
            jdkTreeMap = jdkAscTreeMap.descendingMap();
            xTreeMap = xAscTreeMap.descendingMap();
        }
        Random random = new Random(System.currentTimeMillis());
        final int maxKeyExclusive = 512;
        
        boolean strictCompare = !descending;
        
        /*
         * Test put & remove
         */
        int put = 0;
        for (int i = 0; i < maxKeyExclusive * 2; i++) {
            put = (put + 1) % 3;
            int key = random.nextInt(maxKeyExclusive);
            if (put != 0) {
                int value = random.nextInt();
                jdkTreeMap.put(key, value);
                xTreeMap.put(key, value);
            } else {
                jdkTreeMap.remove(key);
                xTreeMap.remove(key);
            }
            if (strictCompare) {
                compareTreeMap(jdkTreeMap, xTreeMap);
            } else {
                laxCompareTreeMap(jdkTreeMap, xTreeMap);
            }
        }
        
        /*
         * Test putAll
         */
        NavigableMap<Integer, Object> tmp = new TreeMap<Integer, Object>(jdkTreeMap);
        if (descending) {
            tmp = tmp.descendingMap();
        }
        jdkTreeMap.clear();
        xTreeMap.clear();
        if (descending) {
            //descendingMap has optimize the clear when it is unbounded, different with JDK
            setFieldValue(
                    getFieldValue(xAscTreeMap, "baseEntries"), 
                    "modCount", 
                    getFieldValue(jdkAscTreeMap, "modCount"));
        }
        if (strictCompare) {
            compareTreeMap(jdkTreeMap, xTreeMap);
        } else {
            laxCompareTreeMap(jdkTreeMap, xTreeMap);
        }
        
        jdkTreeMap.putAll(tmp);
        xTreeMap.putAll(tmp);
        if (strictCompare) {
            compareTreeMap(jdkTreeMap, xTreeMap);
        } else {
            laxCompareTreeMap(jdkTreeMap, xTreeMap);
        }
        
        /*
         * Test iterator and iterator.remove
         */
        Iterator<Entry<Integer, Object>> jdkIterator = jdkTreeMap.entrySet().iterator();
        Iterator<Entry<Integer, Object>> maIterator = xTreeMap.entrySet().iterator();
        boolean remove = false;
        while (jdkIterator.hasNext()) {
            Entry<Integer, Object> jdkEntry = jdkIterator.next();
            Entry<Integer, Object> maEntry = maIterator.next();
            Assert.assertEquals(jdkEntry.getKey(), maEntry.getKey());
            Assert.assertEquals(jdkEntry.getValue(), maEntry.getValue());
            if (remove) {
                jdkIterator.remove();
                maIterator.remove();
            }
            remove = !remove;
        }
        if (strictCompare) {
            compareTreeMap(jdkTreeMap, xTreeMap);
        } else {
            laxCompareTreeMap(jdkTreeMap, xTreeMap);
        }
        
        NavigableMap<Integer, Object> tmp2 = new TreeMap<Integer, Object>(jdkTreeMap);
        if (descending) {
            tmp2 = tmp2.descendingMap();
        }
        Assert.assertTrue(tmp.size() > tmp2.size());
        Assert.assertFalse(tmp2.isEmpty());
        
        /*
         * Test removeAll when size > c.size
         */
        jdkTreeMap.clear();
        xTreeMap.clear();
        if (descending) {
            //descendingMAMap has optimize the clear when it is unbounded, different with JDK
            setFieldValue(
                    getFieldValue(xAscTreeMap, "baseEntries"), 
                    "modCount", 
                    getFieldValue(jdkAscTreeMap, "modCount"));
        }
        jdkTreeMap.putAll(tmp);
        xTreeMap.putAll(tmp);
        if (strictCompare) {
            compareTreeMap(jdkTreeMap, xTreeMap);
        } else {
            laxCompareTreeMap(jdkTreeMap, xTreeMap);
        }
        
        jdkTreeMap.entrySet().removeAll(tmp2.entrySet());
        xTreeMap.entrySet().removeAll(tmp2.entrySet());
        Assert.assertEquals(tmp.size() - tmp2.size(), jdkTreeMap.size());
        Assert.assertEquals(tmp.size() - tmp2.size(), xTreeMap.size());
        if (strictCompare) {
            compareTreeMap(jdkTreeMap, xTreeMap);
        } else {
            laxCompareTreeMap(jdkTreeMap, xTreeMap);
        }
        
        /*
         * Test removeAll when size <= c.size
         */
        jdkTreeMap.clear();
        xTreeMap.clear();
        if (descending) {
            //descendingMAMap has optimize the clear when it is unbounded, different with JDK
            setFieldValue(
                    getFieldValue(xAscTreeMap, "baseEntries"), 
                    "modCount", 
                    getFieldValue(jdkAscTreeMap, "modCount"));
        }
        jdkTreeMap.putAll(tmp);
        xTreeMap.putAll(tmp);
        if (strictCompare) {
            compareTreeMap(jdkTreeMap, xTreeMap);
        } else {
            laxCompareTreeMap(jdkTreeMap, xTreeMap);
        }
        
        //TreeMap<Integer, Object> 
        NavigableMap<Integer, Object> tmp3 = new TreeMap<Integer, Object>();
        if (descending) {
            tmp3 = tmp3.descendingMap();
        }
        for (int i = maxKeyExclusive + 1 - 1; i >= 0; i--) {
            tmp3.put(2 * i, jdkTreeMap.get(2 * i));
        }
        jdkTreeMap.entrySet().removeAll(tmp3.entrySet());
        xTreeMap.entrySet().removeAll(tmp3.entrySet());
        Assert.assertTrue(tmp.size() > jdkTreeMap.size());
        Assert.assertTrue(tmp.size() > xTreeMap.size());
        if (strictCompare) {
            compareTreeMap(jdkTreeMap, xTreeMap);
        } else {
            laxCompareTreeMap(jdkTreeMap, xTreeMap);
        }
        
        /*
         * Test retainAll
         */
        jdkTreeMap.clear();
        xTreeMap.clear();
        jdkTreeMap.putAll(tmp);
        xTreeMap.putAll(tmp);
        if (strictCompare) {
            compareTreeMap(jdkTreeMap, xTreeMap);
        } else {
            laxCompareTreeMap(jdkTreeMap, xTreeMap);
        }
        
        jdkTreeMap.entrySet().retainAll(tmp2.entrySet());
        xTreeMap.entrySet().retainAll(tmp2.entrySet());
        Assert.assertEquals(tmp2.size(), jdkTreeMap.size());
        Assert.assertEquals(tmp2.size(), xTreeMap.size());
        if (strictCompare) {
            compareTreeMap(jdkTreeMap, xTreeMap);
        } else {
            laxCompareTreeMap(jdkTreeMap, xTreeMap);
        }
    }
    
    private static void compareTreeMap(NavigableMap<?, ?> jdkTreeMap, XNavigableMap<?, ?> xTreeMap) {
        Object jdkObject = jdkTreeMap;
        while (jdkObject.getClass().isMemberClass()) {
            jdkObject = getFieldValue(jdkObject, "m");
        }
        Object baseEntries = getFieldValue(xTreeMap, "baseEntries");
        Object redBlackTree;
        if (baseEntries instanceof RedBlackTreeEntries<?, ?>) {
            redBlackTree = baseEntries;
        } else {
            redBlackTree = getFieldValue(baseEntries, "this$0"); 
        }
        Object maRoot = getFieldValue(redBlackTree, "root");
        Assert.assertEquals(getFieldValue(jdkObject, "size"), maRoot == null ? 0 : getFieldValue(maRoot, "size"));
        Assert.assertEquals(getFieldValue(jdkObject, "modCount"), getFieldValue(redBlackTree, "modCount"));
        compareTreeMapEntry(getFieldValue(jdkObject, "root"), maRoot);
        Iterator<?> jdkIterator = jdkTreeMap.entrySet().iterator();
        Iterator<?> maIterator = xTreeMap.entrySet().iterator();
        while (jdkIterator.hasNext()) {
            Entry<?, ?> jdkEntry = (Entry<?, ?>)jdkIterator.next();
            Entry<?, ?> maEntry = (Entry<?, ?>)maIterator.next();
            Assert.assertEquals(jdkEntry.getKey(), maEntry.getKey());
            Assert.assertEquals(jdkEntry.getValue(), maEntry.getValue());
        }
    }
    
    private static void laxCompareTreeMap(NavigableMap<?, ?> jdkTreeMap, XNavigableMap<?, ?> xTreeMap) {
        Assert.assertEquals(jdkTreeMap.size(), xTreeMap.size());
        Iterator<? extends Entry<?, ?>> jdkItr = jdkTreeMap.entrySet().iterator();
        Iterator<? extends Entry<?, ?>> xItr = xTreeMap.entrySet().iterator();
        while (jdkItr.hasNext()) {
            Entry<?, ?> jdkEntry = jdkItr.next();
            Entry<?, ?> xEntry = xItr.next();
            Assert.assertEquals(jdkEntry.getKey(), xEntry.getKey());
            Assert.assertEquals(jdkEntry.getValue(), xEntry.getValue());
        }
        Assert.assertFalse(xItr.hasNext());
    }
    
    private static void compareTreeMapEntry(Object jdkEntry, Object xEntry) {
        if (jdkEntry == null) {
            Assert.assertNull(xEntry);
        } else if (xEntry == null) {
            Assert.assertNull(jdkEntry);
        } else {
            Assert.assertSame(RedBlackTreeEntries.class.getPackage(), xEntry.getClass().getDeclaringClass().getPackage());
            Assert.assertSame(Map.class.getPackage(), jdkEntry.getClass().getDeclaringClass().getPackage());
            
            Assert.assertEquals(getFieldValue(jdkEntry, "key"), getFieldValue(xEntry, "key"));
            Assert.assertEquals(getFieldValue(jdkEntry, "value"), getFieldValue(xEntry, "value"));
            Assert.assertEquals(!((Boolean)getFieldValue(jdkEntry, "color")).booleanValue(), getFieldValue(xEntry, "red"));
            
            Object maLeft = getFieldValue(xEntry, "left");
            Object jdkLeft = getFieldValue(jdkEntry, "left");
            Object maRight = getFieldValue(xEntry, "right");
            Object jdkRight = getFieldValue(jdkEntry, "right");
            
            int expectedMaEntrySize =
                (maLeft != null ? (Integer)getFieldValue(maLeft, "size") : 0) +
                (maRight != null ? (Integer)getFieldValue(maRight, "size") : 0) +
                1;
            Assert.assertEquals(expectedMaEntrySize, getFieldValue(xEntry, "size"));
            
            if (maLeft != null) {
                Assert.assertSame(xEntry, getFieldValue(maLeft, "parent"));
            }
            if (maRight != null) {
                Assert.assertSame(xEntry, getFieldValue(maRight, "parent"));
            }
            
            compareTreeMapEntry(jdkLeft, maLeft);
            compareTreeMapEntry(jdkRight, maRight);
        }
    }
    
    private static Object getFieldValue(Object obj, String fieldName) {
        for (Class<?> clazz = obj.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                try {
                    return field.get(obj);
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (SecurityException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchFieldException ex) {
                continue;
            }
        }
        throw new RuntimeException("NoSuchField: " + fieldName + " of " + obj.getClass().getName());
    }
    
    private static void setFieldValue(Object obj, String fieldName, Object value) {
        for (Class<?> clazz = obj.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                try {
                    field.set(obj, value);
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (SecurityException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchFieldException ex) {
                continue;
            }
        }
    }
    
    private static String valuesToString(Collection<?> c) {
        StringBuilder builder = new StringBuilder();
        for (Object o : c) {
            builder.append(o);
        }
        return builder.toString();
    }
    
}
