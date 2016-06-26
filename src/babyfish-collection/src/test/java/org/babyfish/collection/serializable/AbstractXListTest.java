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
import java.util.List;
import java.util.ListIterator;

import org.babyfish.collection.LockMode;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XList;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public abstract class AbstractXListTest extends AbstractTest {

    protected abstract XList<String> onCreateList();
    
    @Test
    public void testRawList() throws IOException, ClassNotFoundException {
        this.testImpl(this.initList());
    }
    
    @Test
    public void testUnmodifiableList() throws IOException, ClassNotFoundException {
        this.testImpl(MACollections.unmodifiable(this.initList()));
    }
    
    @Test
    public void testLockingInReadModeList() throws IOException, ClassNotFoundException {
        MACollections.locking(
                MACollections.locked(this.initList()), 
                LockMode.READ, 
                (XList<String> x) -> {
                    try {
                        this.testImpl(x);
                    } catch (ClassNotFoundException | IOException ex) {
                        throw new AssertionError(ex);
                    }
                }
        );
    }
    
    @Test
    public void testLockingInWriteModeList() throws IOException, ClassNotFoundException {
        MACollections.locking(
                MACollections.locked(this.initList()), 
                LockMode.WRITE, 
                (XList<String> x) -> {
                    try {
                        this.testImpl(x);
                    } catch (ClassNotFoundException | IOException ex) {
                        Assert.fail(ex.getMessage());
                    }
                }
        );
    }
    
    private void testImpl(XList<String> list) throws IOException, ClassNotFoundException {
        List<String> deserializableList = serialzingClone(list);
        Assert.assertTrue(list != deserializableList);
        Assert.assertEquals(list.size(), deserializableList.size());
        ListIterator<String> itr = list.listIterator();
        ListIterator<String> deserializableItr = deserializableList.listIterator();
        Assert.assertTrue(itr != deserializableItr);
        while (itr.hasNext()) {
            String a = itr.next();
            String b = deserializableItr.next();
            Assert.assertTrue(a != b);
            Assert.assertEquals(a, b);
        }
        Assert.assertFalse(deserializableItr.hasNext());
        
        itr = list.listIterator(list.size());
        deserializableItr = deserializableList.listIterator(deserializableList.size());
        Assert.assertTrue(itr != deserializableItr);
        while (itr.hasPrevious()) {
            String a = itr.previous();
            String b = deserializableItr.previous();
            Assert.assertTrue(a != b);
            Assert.assertEquals(a, b);
        }
        Assert.assertFalse(deserializableItr.hasPrevious());
    }
    
    private XList<String> initList() {
        XList<String> list = this.onCreateList();
        for (int i = 0; i < 100; i++) {
            list.add("element[" + i + "]");
        }
        return list;
    }
}
