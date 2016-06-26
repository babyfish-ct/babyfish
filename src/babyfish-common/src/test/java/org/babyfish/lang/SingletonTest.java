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
package org.babyfish.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import junit.framework.Assert;

import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.Singleton;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class SingletonTest {

    @Test(expected = IllegalProgramException.class)
    public void testInvokeConstructorImmediately() {
        new A();
    }
    
    @Test
    public void testInvokeGetInstance() {
        A a1 = A.getInstance();
        A a2 = A.getInstance();
        Assert.assertSame(a1, a2);
    }
    
    @Test(expected = IllegalProgramException.class)
    public void testInvokeConstructor() {
        B.getInstance();
    }
    
    @Test
    public void testInvokeNestedGetInstance() {
        C c1 = C.getInstance();
        C c2 = C.getInstance();
        Assert.assertSame(c1, c2);
    }
    
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        D d = D.getInstance();
        byte[] buffer;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeObject(d);
            oout.flush();
            buffer = bout.toByteArray();
        }
        D deserializedD;
        try (ByteArrayInputStream bin = new ByteArrayInputStream(buffer);
                ObjectInputStream oin = new ObjectInputStream(bin)) {
            deserializedD = (D)oin.readObject();
        }
        Assert.assertSame(d, deserializedD);
        Assert.assertSame(d, deserializedD.self);
    }
    
    @Test 
    public void testWrapperSerialization() throws IOException, ClassNotFoundException {
        D d = D.getInstance();
        byte[] buffer;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeObject(new DWrapper(d));
            oout.flush();
            buffer = bout.toByteArray();
        }
        D deserializedD;
        try (ByteArrayInputStream bin = new ByteArrayInputStream(buffer);
                ObjectInputStream oin = new ObjectInputStream(bin)) {
            deserializedD = ((DWrapper)oin.readObject()).getValue();
        }
        Assert.assertSame(d, deserializedD);
        Assert.assertSame(d, deserializedD.self);
    }
    
    static class A extends Singleton {
        
        static A getInstance() {
            return getInstance(A.class);
        }
    }
    
    static class B extends Singleton {
        
        A a = new A();
        
        static B getInstance() {
            return getInstance(B.class);
        }
    }
    
    static class C extends Singleton {
        
        A a = Singleton.getInstance(A.class);
        
        static C getInstance() {
            return getInstance(C.class);
        }
        
    }
    
    static class D extends Singleton implements Serializable {
        
        D self = this;
        
        static D getInstance() {
            return getInstance(D.class);
        }
    }
    
    static class DWrapper implements Serializable {

        private static final long serialVersionUID = 94767903590316967L;
        
        D value;

        public DWrapper(D value) {
            this.value = value;
        }

        public D getValue() {
            return value;
        }
        
    }
}
