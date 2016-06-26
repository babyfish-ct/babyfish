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
package org.babyfish.model.comparator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.model.comparator.entities.Book;
import org.babyfish.model.metadata.ModelClass;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class OwnerComparatorOrEqualityComparatorTest {
    
    @Test
    public void testHashCode() {
        EqualityComparator<Book> equalityComparator =
            ModelClass
            .of(Book.class)
            .getProperties()
            .get("analogousBooks")
            .<Book>getCollectionUnifiedComparator()
            .equalityComparator(true);
        Assert.assertNotNull(equalityComparator);
        
        Assert.assertEquals(
                (("0001".hashCode() * 31) + "Thinking in C++".hashCode()) * 31 + "969-9-678-54545-7".hashCode(), 
                equalityComparator.hashCode(new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7")));
        Assert.assertEquals(
                ((0 * 31) + "Thinking in C++".hashCode()) * 31 + "969-9-678-54545-7".hashCode(),  
                equalityComparator.hashCode(new Book(3, null, "Thinking in C++", "969-9-678-54545-7")));
        Assert.assertEquals(
                (("0001".hashCode() * 31) + 0) * 31 + "969-9-678-54545-7".hashCode(), 
                equalityComparator.hashCode(new Book(3, "0001", null, "969-9-678-54545-7")));
        Assert.assertEquals(
                (("0001".hashCode() * 31) + "Thinking in C++".hashCode()) * 31 + 0, 
                equalityComparator.hashCode(new Book(3, "0001", "Thinking in C++", null)));
        Assert.assertEquals(
                0, 
                equalityComparator.hashCode(new Book(3, null, null, null)));
    }
    
    @Test
    public void testEquals() {
        EqualityComparator<Book> equalityComparator =
                ModelClass
                .of(Book.class)
                .getProperties()
                .get("analogousBooks")
                .<Book>getCollectionUnifiedComparator()
                .equalityComparator(true);
        Assert.assertNotNull(equalityComparator);
        
        Assert.assertTrue(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(
                                3, 
                                new String("0001"), 
                                new String("Thinking in C++"), 
                                new String("969-9-678-54545-7"))));
        
        Assert.assertTrue(
                equalityComparator.equals(
                        new Book(3, null, null, null),
                        new Book(3, null, null, null)));
        
        Assert.assertFalse(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(3, "0002", "Thinking in C++", "969-9-678-54545-7")));
        Assert.assertFalse(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(3, "0001", "Thinking in Java", "969-9-678-54545-7")));
        Assert.assertFalse(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-8")));
        
        Assert.assertFalse(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(3, null, "Thinking in C++", "969-9-678-54545-7")));
        Assert.assertFalse(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(3, "0001", null, "969-9-678-54545-7")));
        Assert.assertFalse(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(3, "0001", "Thinking in C++", null)));
    }
    
    @Test
    public void testComparator() {
        Comparator<Book> comparator =
                ModelClass
                .of(Book.class)
                .getProperties()
                .get("sameSeriesBooks")
                .<Book>getCollectionUnifiedComparator()
                .comparator(true);
                Assert.assertNotNull(comparator);
        
        Assert.assertEquals(
                0, 
                comparator.compare(
                        new Book(3, "A", "A", "A"),
                        new Book(
                                3, 
                                new String("A"), 
                                new String("A"), 
                                new String("A"))));
        
        Assert.assertEquals(
                -1, 
                comparator.compare(
                        new Book(3, "A", "B", "B"),
                        new Book(
                                3, 
                                new String("B"), 
                                new String("A"), 
                                new String("A"))));
        Assert.assertEquals(
                -1, 
                comparator.compare(
                        new Book(3, "A", "A", "B"),
                        new Book(
                                3, 
                                new String("A"), 
                                new String("B"), 
                                new String("A"))));
        Assert.assertEquals(
                -1, 
                comparator.compare(
                        new Book(3, "A", "A", "A"),
                        new Book(
                                3, 
                                new String("A"), 
                                new String("A"), 
                                new String("B"))));
        
        Assert.assertEquals(
                +1, 
                comparator.compare(
                        new Book(3, "B", "A", "A"),
                        new Book(
                                3, 
                                new String("A"), 
                                new String("B"), 
                                new String("B"))));
        Assert.assertEquals(
                +1, 
                comparator.compare(
                        new Book(3, "A", "B", "A"),
                        new Book(
                                3, 
                                new String("A"), 
                                new String("A"), 
                                new String("B"))));
        Assert.assertEquals(
                +1, 
                comparator.compare(
                        new Book(3, "A", "A", "B"),
                        new Book(
                                3, 
                                new String("A"), 
                                new String("A"), 
                                new String("A"))));
        
        Assert.assertEquals(
                0, 
                comparator.compare(
                        new Book(3, null, null, null),
                        new Book(3, null, null, null)));
        
        Assert.assertEquals(
                -1, 
                comparator.compare(
                        new Book(3, null, "A", "A"),
                        new Book(3, "A", null, null)));
        Assert.assertEquals(
                -1, 
                comparator.compare(
                        new Book(3, "A", null, "A"),
                        new Book(3, "A", "A", null)));
        Assert.assertEquals(
                -1, 
                comparator.compare(
                        new Book(3, "A", "A", null),
                        new Book(3, "A", "A", "A")));
        
        Assert.assertEquals(
                +1, 
                comparator.compare(
                        new Book(3, "A", null, null),
                        new Book(3, null, "A", "A")));
        Assert.assertEquals(
                +1, 
                comparator.compare(
                        new Book(3, "A", "A", null),
                        new Book(3, "A", null, "A")));
        Assert.assertEquals(
                +1, 
                comparator.compare(
                        new Book(3, "A", "A", "A"),
                        new Book(3, "A", "A", null)));
    }
    
    @Test
    public void testIO() throws ClassNotFoundException, IOException {
        byte[] buf;
        ModelClass modelClass = ModelClass.of(Book.class);
        int codeId = modelClass.getProperties().get("code").getId();
        int nameId = modelClass.getProperties().get("name").getId();
        Object[] arr = new Object[] {
                modelClass.getDefaultEqualityComparator(),
                modelClass.getComparator("code"),
                modelClass.getEqualityComparator("code"),
                modelClass.getComparator(codeId),
                modelClass.getEqualityComparator(codeId),
                modelClass.getComparator("code", "name"),
                modelClass.getEqualityComparator("code", "name"),
                modelClass.getComparator(codeId, nameId),
                modelClass.getEqualityComparator(codeId, nameId),
        };
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeObject(arr);
            oout.flush();
            buf = bout.toByteArray();
        }
        Object[] deserializedArr;
        try (ByteArrayInputStream bin = new ByteArrayInputStream(buf);
                ObjectInputStream oin = new ObjectInputStream(bin)) {
            deserializedArr = (Object[])oin.readObject();
        }
        Assert.assertTrue(arr != deserializedArr);
        Assert.assertEquals(arr.length, deserializedArr.length);
        for (int i = arr.length - 1; i >= 0; i--) {
            Assert.assertEquals(arr[i], deserializedArr[i]);
        }
    }
}
