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
package org.babyfish.test.util.reflect.runtime;

import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.util.reflect.MethodImplementation;
import org.babyfish.util.reflect.runtime.ASM;
import org.babyfish.util.reflect.runtime.ClassWrapper;
import org.babyfish.util.reflect.runtime.XMethodVisitor;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class ClassWrapperTest {

    @Test
    public void testGenerateMethod() {
        ConnectionImpl raw = new ConnectionImpl();
        Connection con = ConnectionFilter.wrap(raw);
        con.open();
        con.close();
        Assert.assertEquals(
                "Before invoke the raw method: void open()...\n" +
                "Connection is opened\n" +
                "Before invoke the raw method: void close()...\n" +
                "Connection is closed\n", 
                raw.getLog(true)
        );
    }
    
    static interface Connection {
        void open();
        void close();
    }
    
    static class ConnectionImpl implements Connection {
        
        private StringBuilder builder = new StringBuilder();

        @Override
        public void open() {
            this.log("Connection is opened");
        }

        @Override
        public void close() {
            this.log("Connection is closed");
        }
        
        public void log(String message) {
            this.builder.append(message).append('\n');
        }
        
        public String getLog(boolean clean) {
            String retval = this.builder.toString();
            if (clean) {
                this.builder = new StringBuilder();
            }
            return retval;
        }
    }
    
    static class ConnectionFilter extends ClassWrapper {
        
        private static final ConnectionFilter INSTANCE = getInstance(ConnectionFilter.class);

        public ConnectionFilter() {
            super(ConnectionImpl.class);
        }

        @Override
        protected void generateMethodCode(XMethodVisitor mv, MethodImplementation methodImplementation) {
            this.generateGetRaw(mv);
            mv.visitLdcInsn("Before invoke the raw method: " + methodImplementation + "...");
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ASM.getInternalName(this.getRawType()), 
                    "log", 
                    "(Ljava/lang/String;)V",
                    false);
            super.generateMethodCode(mv, methodImplementation);
        }

        public static Connection wrap(Connection con) {
            return (Connection)INSTANCE.createProxy(con);
        }
    }
}
