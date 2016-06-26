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

import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedList;
import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MALinkedList;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.XList;
import org.babyfish.collection.XNavigableMap;
import org.babyfish.collection.XNavigableSet;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class RangeCheckTest {

    @Test
    public void testArrayList() {
        this.testXList(new ArrayList<String>());
    }
    
    @Test
    public void testLinkedList() {
        this.testXList(new LinkedList<String>());
    }
    
    @Test
    public void testMAArrayList() {
        this.testXList(new MAArrayList<String>());
    }
    
    @Test
    public void testMALinkedList() {
        this.testXList(new MALinkedList<String>());
    }
    
    @Test
    public void testTreeSet() {
        this.testXNavigableSet(new TreeSet<String>());
    }
    
    @Test
    public void testMATreeSet() {
        this.testXNavigableSet(new TreeSet<String>());
    }
    
    @Test
    public void testTreeMap() {
        this.testXNavigableMap(new TreeMap<String, String>());
    }
    
    @Test
    public void testMATreeMap() {
        this.testXNavigableMap(new MATreeMap<String, String>());
    }
    
    private void testXList(XList<String> list) {
        for (int i = 0; i < 7; i++) {
            list.add("ABCDEFG".substring(i, i + 1));
        }
        List<String> subList = list.subList(2, 5);
        Assert.assertTrue(list.contains("A"));
        Assert.assertFalse(subList.contains("A"));
        Assert.assertEquals(0, list.indexOf("A"));
        Assert.assertEquals(-1, subList.indexOf("A"));
        Assert.assertFalse(subList.remove("A"));
        assertCollection(list, "A", "B", "C", "D", "E", "F", "G");
        assertCollection(subList, "C", "D", "E");
    }
    
    private void testXNavigableSet(XNavigableSet<String> set) {
        for (int i = 0; i < 7; i++) {
            set.add("ABCDEFG".substring(i, i + 1));
        }
        XNavigableSet<String> subSet = set.subSet("C", "F");
        Assert.assertTrue(set.contains("A"));
        Assert.assertFalse(subSet.contains("A"));
        Assert.assertFalse(subSet.remove("A"));
        assertCollection(set, "A", "B", "C", "D", "E", "F", "G");
        assertCollection(subSet, "C", "D", "E");
    }
    
    private void testXNavigableMap(XNavigableMap<String, String> map) {
        for (int i = 0; i < 7; i++) {
            map.put("ABCDEFG".substring(i, i + 1), "abcdefg".substring(i, i + 1));
        }
        XNavigableMap<String, String> subMap = map.subMap("C", "F");
        Assert.assertTrue(map.keySet().contains("A"));
        Assert.assertTrue(map.values().contains("a"));
        Assert.assertTrue(map.keySet().contains("G"));
        Assert.assertTrue(map.values().contains("g"));
        Assert.assertFalse(subMap.keySet().contains("A"));
        Assert.assertFalse(subMap.values().contains("a"));
        Assert.assertFalse(subMap.keySet().contains("G"));
        Assert.assertFalse(subMap.values().contains("g"));
        Assert.assertNull(subMap.remove("A"));
        Assert.assertFalse(subMap.keySet().remove("A"));
        Assert.assertFalse(subMap.values().remove("g"));
        assertCollection(map.keySet(), "A", "B", "C", "D", "E", "F", "G");
        assertCollection(subMap.keySet(), "C", "D", "E");
        assertCollection(map.values(), "a", "b", "c", "d", "e", "f", "g");
        assertCollection(subMap.values(), "c", "d", "e");
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertCollection(Collection<E> c, E ... elements) {
        Assert.assertEquals(elements.length, c.size());
        int index = 0;
        for (E e : c) {
            Assert.assertEquals(elements[index++], e);
        }
    }
}
