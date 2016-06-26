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
package org.babyfish.collection.serializable;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.babyfish.collection.LockMode;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XOrderedMap;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public abstract class AbstractXMapTest extends AbstractTest {

    protected abstract XMap<String, String> onCreateMap();
    
    @Test
    public void testRawMap() throws IOException, ClassNotFoundException {
        this.testImpl(this.initMap());
    }
    
    @Test
    public void testUnmodifiableMap() throws IOException, ClassNotFoundException {
        this.testImpl(MACollections.unmodifiable(this.initMap()));
    }
    
    @Test
    public void testLockingInReadModeMap() throws IOException, ClassNotFoundException {
        MACollections.locking(
                MACollections.locked(this.initMap()), 
                LockMode.READ, 
                (XMap<String, String> x) -> {
                    try {
                        this.testImpl(x);
                    } catch (ClassNotFoundException | IOException ex) {
                        throw new AssertionError(ex);
                    }
                }
        );
    }
    
    @Test
    public void testLockingInWriteModeMap() throws IOException, ClassNotFoundException {
        MACollections.locking(
                MACollections.locked(this.initMap()), 
                LockMode.WRITE, 
                (XMap<String, String> x) -> {
                    try {
                        this.testImpl(x);
                    } catch (ClassNotFoundException | IOException ex) {
                        throw new AssertionError(ex);
                    }
                }
        );
    }
    
    private void testImpl(XMap<String, String> map) throws IOException, ClassNotFoundException {
        XMap<String, String> deserializableMap = serialzingClone(map);
        Assert.assertTrue(map != deserializableMap);
        Assert.assertEquals(map.size(), deserializableMap.size());
        Iterator<Entry<String, String>> itr = map.entrySet().iterator();
        Iterator<Entry<String, String>> deserializableItr = deserializableMap.entrySet().iterator();
        Assert.assertTrue(itr != deserializableItr);
        if (!(map instanceof XOrderedMap<?, ?>) && !(map instanceof NavigableMap<?, ?>)) {
            NavigableMap<String, String> navigableMap = new TreeMap<String, String>();
            NavigableMap<String, String> deserializedNavigableMap = new TreeMap<String, String>();
            navigableMap.putAll(map);
            deserializedNavigableMap.putAll(deserializableMap);
            itr = navigableMap.entrySet().iterator();
            deserializableItr = deserializedNavigableMap.entrySet().iterator();
        }
        while (itr.hasNext()) {
            Entry<String, String> a = itr.next();
            Entry<String, String> b = deserializableItr.next();
            Assert.assertTrue(a.getKey() != b.getKey());
            Assert.assertTrue(a.getKey().equals(b.getKey()));
            Assert.assertTrue(a.getValue() != b.getValue());
            Assert.assertTrue(a.getValue().equals(b.getValue()));
        }
        Assert.assertFalse(deserializableItr.hasNext());
        
        itr = descendingIterator(map);
        if (itr != null) {
            deserializableItr = descendingIterator(deserializableMap);
            Assert.assertTrue(itr != deserializableItr);
            while (itr.hasNext()) {
                Entry<String, String> a = itr.next();
                Entry<String, String> b = deserializableItr.next();
                Assert.assertTrue(a.getKey() != b.getKey());
                Assert.assertTrue(a.getKey().equals(b.getKey()));
                Assert.assertTrue(a.getValue() != b.getValue());
                Assert.assertTrue(a.getValue().equals(b.getValue()));
            }
            Assert.assertFalse(deserializableItr.hasNext());
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Iterator<Entry<String, String>> descendingIterator(XMap<String, String> map) {
        if (map instanceof XOrderedMap<?, ?>) {
            return ((XOrderedMap<String, String>)map).descendingMap().entrySet().iterator();
        }
        if (map instanceof NavigableMap<?, ?>) {
            return ((NavigableMap<String, String>)map).descendingMap().entrySet().iterator();
        }
        return null;
    }
    
    private XMap<String, String> initMap() {
        XMap<String, String> map = this.onCreateMap();
        for (int i = 0; i < 100; i++) {
            map.put("key[" + i + "]", "value[" + i + "]");
        }
        return map;
    }
}
