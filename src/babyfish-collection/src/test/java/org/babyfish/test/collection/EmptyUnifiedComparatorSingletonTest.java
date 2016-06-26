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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Assert;

import org.babyfish.collection.UnifiedComparator;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class EmptyUnifiedComparatorSingletonTest {

    @Test
    public void test() throws IOException, ClassNotFoundException {
        UnifiedComparator<?> unifiedComparator = UnifiedComparator.empty();
        byte[] buffer;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeObject(unifiedComparator);
            oout.flush();
            buffer = bout.toByteArray();
        }
        UnifiedComparator<?> deserializedUnifiedComparator;
        try (ByteArrayInputStream bin = new ByteArrayInputStream(buffer);
                ObjectInputStream oin = new ObjectInputStream(bin)) {
            deserializedUnifiedComparator = (UnifiedComparator<?>)oin.readObject();
        }
        Assert.assertSame(unifiedComparator, deserializedUnifiedComparator);
    }
}
