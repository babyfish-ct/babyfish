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
import java.util.NavigableSet;

import org.babyfish.collection.LockMode;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.collection.XSet;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public abstract class AbstractXSetTest extends AbstractTest {

    protected abstract XSet<String> onCreateSet();
    
    @Test
    public void testRawSet() throws IOException, ClassNotFoundException {
        this.testImpl(this.initSet());
    }
    
    @Test
    public void testUnmodifiableSet() throws IOException, ClassNotFoundException {
        this.testImpl(MACollections.unmodifiable(this.initSet()));
    }
    
    @Test
    public void testLockingInReadModeSet() throws IOException, ClassNotFoundException {
        MACollections.locking(
                MACollections.locked(this.initSet()), 
                LockMode.READ, 
                (XSet<String> x) -> {
                    try {
                        this.testImpl(x);
                    } catch (ClassNotFoundException | IOException ex) {
                        throw new AssertionError(ex);
                    }
                }
        );
    }
    
    @Test
    public void testLockingInWriteModeSet() throws IOException, ClassNotFoundException {
        MACollections.locking(
                MACollections.locked(this.initSet()), 
                LockMode.WRITE, 
                (XSet<String> x) -> {
                    try {
                        this.testImpl(x);
                    } catch (ClassNotFoundException | IOException ex) {
                        throw new AssertionError(ex);
                    }
                }
        );
    }
    
    private void testImpl(XSet<String> set) throws IOException, ClassNotFoundException {
        XSet<String> deserializableSet = serialzingClone(set);
        Assert.assertTrue(set != deserializableSet);
        Assert.assertEquals(set.size(), deserializableSet.size());
        Iterator<String> itr = set.iterator();
        Iterator<String> deserializableItr = deserializableSet.iterator();
        Assert.assertTrue(itr != deserializableItr);
        if (!(set instanceof XOrderedSet<?>) && !(set instanceof NavigableSet<?>)) {
            NavigableSet<String> navigableSet = new TreeSet<String>();
            NavigableSet<String> deserializedNavigableSet = new TreeSet<String>();
            navigableSet.addAll(set);
            deserializedNavigableSet.addAll(deserializableSet);
            itr = navigableSet.iterator();
            deserializableItr = deserializedNavigableSet.iterator();
        }
        while (itr.hasNext()) {
            String a = itr.next();
            String b = deserializableItr.next();
            Assert.assertTrue(a != b);
            Assert.assertEquals(a, b);
        }
        Assert.assertFalse(deserializableItr.hasNext());
        
        itr = descendingIterator(set);
        if (itr != null) {
            deserializableItr = descendingIterator(deserializableSet);
            Assert.assertTrue(itr != deserializableItr);
            while (itr.hasNext()) {
                String a = itr.next();
                String b = deserializableItr.next();
                Assert.assertTrue(a != b);
                Assert.assertEquals(a, b);
            }
            Assert.assertFalse(deserializableItr.hasNext());
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Iterator<String> descendingIterator(XSet<String> set) {
        if (set instanceof XOrderedSet<?>) {
            return ((XOrderedSet<String>)set).descendingIterator();
        }
        if (set instanceof NavigableSet<?>) {
            return ((NavigableSet<String>)set).descendingIterator();
        }
        return null;
    }
    
    private XSet<String> initSet() {
        XSet<String> set = this.onCreateSet();
        for (int i = 0; i < 100; i++) {
            set.add("element[" + i + "]");
        }
        return set;
    }
}
