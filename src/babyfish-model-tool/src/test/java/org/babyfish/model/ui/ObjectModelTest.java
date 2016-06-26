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
package org.babyfish.model.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.babyfish.collection.MACollections;
import org.babyfish.model.ui.entities.Tab;
import org.babyfish.model.ui.entities.TabPage;
import org.junit.Test;

import junit.framework.Assert;

public class ObjectModelTest {

    @Test
    public void testListAndIndexReference() throws ClassNotFoundException, IOException {
        Tab tab1 = new Tab();
        Tab tab2 = new Tab();
        TabPage tabPage1 = new TabPage();
        TabPage tabPage2 = new TabPage();
        TabPage tabPage3 = new TabPage();
        TabPage tabPage4 = new TabPage();
        Object[] arr;
        
        assertCollection(tab1.getPages());
        assertCollection(tab2.getPages());
        Assert.assertEquals(-1, tabPage1.getIndex());
        Assert.assertSame(null, tabPage1.getParent());
        Assert.assertEquals(-1, tabPage2.getIndex());
        Assert.assertSame(null, tabPage2.getParent());
        Assert.assertEquals(-1, tabPage3.getIndex());
        Assert.assertSame(null, tabPage3.getParent());
        Assert.assertEquals(-1, tabPage4.getIndex());
        Assert.assertSame(null, tabPage4.getParent());
        
        arr = recreate(tab1, tab2, tabPage1, tabPage2, tabPage3, tabPage4);
        tab1 = (Tab)arr[0];
        tab2 = (Tab)arr[1];
        tabPage1 = (TabPage)arr[2];
        tabPage2 = (TabPage)arr[3];
        tabPage3 = (TabPage)arr[4];
        tabPage4 = (TabPage)arr[5];
        tab1.getPages().addAll(MACollections.wrap(tabPage1, tabPage2));
        tab2.getPages().addAll(MACollections.wrap(tabPage3, tabPage4));
        assertCollection(tab1.getPages(), tabPage1, tabPage2);
        assertCollection(tab2.getPages(), tabPage3, tabPage4);
        Assert.assertEquals(0, tabPage1.getIndex());
        Assert.assertSame(tab1, tabPage1.getParent());
        Assert.assertEquals(1, tabPage2.getIndex());
        Assert.assertSame(tab1, tabPage2.getParent());
        Assert.assertEquals(0, tabPage3.getIndex());
        Assert.assertSame(tab2, tabPage3.getParent());
        Assert.assertEquals(1, tabPage4.getIndex());
        Assert.assertSame(tab2, tabPage4.getParent());
        
        arr = recreate(tab1, tab2, tabPage1, tabPage2, tabPage3, tabPage4);
        tab1 = (Tab)arr[0];
        tab2 = (Tab)arr[1];
        tabPage1 = (TabPage)arr[2];
        tabPage2 = (TabPage)arr[3];
        tabPage3 = (TabPage)arr[4];
        tabPage4 = (TabPage)arr[5];
        tab1.getPages().add(0, tabPage3);
        assertCollection(tab1.getPages(), tabPage3, tabPage1, tabPage2);
        assertCollection(tab2.getPages(), tabPage4);
        Assert.assertEquals(1, tabPage1.getIndex());
        Assert.assertSame(tab1, tabPage1.getParent());
        Assert.assertEquals(2, tabPage2.getIndex());
        Assert.assertSame(tab1, tabPage2.getParent());
        Assert.assertEquals(0, tabPage3.getIndex());
        Assert.assertSame(tab1, tabPage3.getParent());
        Assert.assertEquals(0, tabPage4.getIndex());
        Assert.assertSame(tab2, tabPage4.getParent());
        
        arr = recreate(tab1, tab2, tabPage1, tabPage2, tabPage3, tabPage4);
        tab1 = (Tab)arr[0];
        tab2 = (Tab)arr[1];
        tabPage1 = (TabPage)arr[2];
        tabPage2 = (TabPage)arr[3];
        tabPage3 = (TabPage)arr[4];
        tabPage4 = (TabPage)arr[5];
        tabPage3.setParent(tab2);
        tabPage3.setIndex(1);
        assertCollection(tab1.getPages(), tabPage1, tabPage2);
        assertCollection(tab2.getPages(), tabPage4, tabPage3);
        Assert.assertEquals(0, tabPage1.getIndex());
        Assert.assertSame(tab1, tabPage1.getParent());
        Assert.assertEquals(1, tabPage2.getIndex());
        Assert.assertSame(tab1, tabPage2.getParent());
        Assert.assertEquals(1, tabPage3.getIndex());
        Assert.assertSame(tab2, tabPage3.getParent());
        Assert.assertEquals(0, tabPage4.getIndex());
        Assert.assertSame(tab2, tabPage4.getParent());
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertCollection(Collection<E> c, E ... elements) {
        Assert.assertEquals(elements.length, c.size());
        int index = 0;
        for (E e : c) {
            Assert.assertSame(elements[index++], e);
        }
    }
    
    private static Object[] recreate(Object ... args) throws IOException, ClassNotFoundException {
        byte[] buf;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
                for (Object o : args) {
                    out.writeObject(o);
                }
                out.flush();
            }
            buf = bout.toByteArray();
        }
        args = new Object[args.length];
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf))) {
            for (int i = 0; i < args.length; i++) {
                args[i] = in.readObject();
            }
        }
        return args;
    }
}
