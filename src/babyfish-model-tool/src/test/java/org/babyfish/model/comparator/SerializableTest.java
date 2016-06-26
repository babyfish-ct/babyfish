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

import org.babyfish.model.comparator.entities.Node;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.metadata.ModelProperty;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class SerializableTest {
    
    @Test
    public void test() throws ClassNotFoundException, IOException {
        ModelClass modelClass = ModelClass.of(Node.class);
        ModelProperty name = modelClass.getProperties().get("name");
        ModelProperty childNodes = modelClass.getProperties().get("childNodes");
        ModelProperty parentReference = modelClass.getProperties().get("parent");
        Assert.assertSame(modelClass, serializingClone(modelClass));
        Assert.assertSame(name, serializingClone(name));
        Assert.assertSame(childNodes, serializingClone(childNodes));
        Assert.assertSame(parentReference, serializingClone(parentReference));
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T serializingClone(T obj) throws IOException, ClassNotFoundException {
        byte[] buf;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeObject(obj);
            oout.flush();
            buf = bout.toByteArray();
        }
        try (ByteArrayInputStream bin = new ByteArrayInputStream(buf);
                ObjectInputStream oin = new ObjectInputStream(bin)) {
            return (T)oin.readObject();
        }
    }
}
