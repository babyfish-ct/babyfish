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
package org.babyfish.model.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.babyfish.collection.MACollections;
import org.babyfish.model.data.entities.Company;
import org.babyfish.model.data.entities.FullName;
import org.babyfish.model.data.entities.Investor;
import org.junit.Test;

import junit.framework.Assert;

public class ObjectModelTest {
    
    @Test
    public void testUnstableElementsByChangingEmbeddedScalar() throws ClassNotFoundException, IOException {
        Investor steve = new Investor();
        Investor sculley = new Investor();
        Company apple = new Company();
        steve.setFullName(new FullName("Steve", "Jobs"));
        sculley.setFullName(new FullName("Sculley", null));
        apple.setName("Apple");
        apple.getInvestors().addAll(MACollections.wrap(steve, sculley));
        
        Object[] arr = recreate(steve, sculley, apple);
        steve = (Investor)arr[0];
        sculley = (Investor)arr[1];
        apple = (Company)arr[2];
        
        assertCollection(steve.getCompanies(), apple);
        assertCollection(sculley.getCompanies(), apple);
        assertCollection(apple.getInvestors(), steve, sculley);
        
        sculley.setFullName(new FullName("STEVE", "jobs"));
        
        assertCollection(steve.getCompanies());
        assertCollection(sculley.getCompanies(), apple);
        assertCollection(apple.getInvestors(), sculley);
    }
    
    @Test
    public void testUnstableElementsByChangingNestedScalar() throws ClassNotFoundException, IOException {
        
        Investor steve = new Investor();
        Investor sculley = new Investor();
        Company apple = new Company();
        steve.setFullName(new FullName("Steve", "Jobs"));
        sculley.setFullName(new FullName("Sculley", null));
        apple.setName("Apple");
        apple.getInvestors().addAll(MACollections.wrap(steve, sculley));
        
        Object[] arr = recreate(steve, sculley, apple);
        steve = (Investor)arr[0];
        sculley = (Investor)arr[1];
        apple = (Company)arr[2];
        
        assertCollection(steve.getCompanies(), apple);
        assertCollection(sculley.getCompanies(), apple);
        assertCollection(apple.getInvestors(), steve, sculley);
        
        sculley.getFullName().setFirstName("STEVE");
        sculley.getFullName().setLastName("jobs");
        assertCollection(steve.getCompanies());
        assertCollection(sculley.getCompanies(), apple);
        assertCollection(apple.getInvestors(), sculley);
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
