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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * @author Tao Chen
 */
public abstract class StatefulObject {
    
    private static final Map<Class<?>, Class<?>> CACHE = new WeakHashMap<>();
    
    private static final ReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();
    
    private static final Class<?>[] ON_WRITE_STATE_PARAMTER_TYPES = 
            new Class[] { Output.class };
    
    private static final Class<?>[] ON_READ_STATE_PARAMTER_TYPES = 
            new Class[] { Input.class };
    
    private static final Field OBJECT_OUTPUT_STREAM_CUR_CONTEXT =
            fieldOf(ObjectOutputStream.class, "curContext");
    
    private static final Field OBJECT_INPUT_STREAM_CUR_CONTEXT =
            fieldOf(ObjectInputStream.class, "curContext");
    
    private static final Field SERIAL_CALL_BACK_CONTEXT_OBJ =
            fieldOf(OBJECT_OUTPUT_STREAM_CUR_CONTEXT.getType(), "obj");
    
    private static final Field SERIAL_CALL_BACK_CONTEXT_DESC =
            fieldOf(OBJECT_OUTPUT_STREAM_CUR_CONTEXT.getType(), "desc");
    
    protected StatefulObject() {
        this.getToppestSerializableClass();
    }

    protected final void writeState(ObjectOutputStream out) throws IOException {
        this.validateStream(out, false);
        this.onWriteState(new Output(out));
    }
    
    protected final void readState(ObjectInputStream in) throws ClassNotFoundException, IOException {
        this.validateStream(in, true);
        this.onReadState(new Input(in));
    }
    
    protected void onWriteState(Output out) throws IOException {
        
    }
    
    protected void onReadState(Input in) throws ClassNotFoundException, IOException {
        
    }
    
    private void validateStream(Object stream, boolean in) {
        Class<?> toppestSerializableClass = this.getToppestSerializableClass();
        if (toppestSerializableClass == null) {
            throw new IllegalOperationException(ownerIsNotSerializable(
                    this.getClass(), 
                    String.format(
                            "%s(%s)", 
                            in ? "readState" : "writeState", 
                            in ? ObjectInputStream.class.getName() : ObjectOutputStream.class)
                    )
            );
        }
        Field curContextField = 
                in ? 
                OBJECT_INPUT_STREAM_CUR_CONTEXT :
                OBJECT_OUTPUT_STREAM_CUR_CONTEXT;
        Object curContext = valueOf(stream, curContextField);
        Object obj = valueOf(curContext, SERIAL_CALL_BACK_CONTEXT_OBJ);
        if (this != obj) {
            throw new IllegalOperationException(
                    invalidIOTarget(
                            this.getClass(),
                            String.format(
                                    "%s(%s)", 
                                    in ? "readState" : "writeState", 
                                    in ? ObjectInputStream.class.getName() : ObjectOutputStream.class),
                            String.format(
                                    "%s(%s)", 
                                    in ? "readObject" : "writeObject", 
                                    in ? ObjectInputStream.class.getName() : ObjectOutputStream.class)
                    )
            );
        }
        ObjectStreamClass desc = (ObjectStreamClass)valueOf(curContext, SERIAL_CALL_BACK_CONTEXT_DESC);     
        if (toppestSerializableClass != desc.forClass()) {
            throw new IllegalOperationException(
                    invalidIOOwner(
                            this.getClass(), 
                            String.format(
                                    "%s(%s)", 
                                    in ? "readState" : "writeState", 
                                    in ? ObjectInputStream.class.getName() : ObjectOutputStream.class),
                            String.format(
                                    "%s(%s)", 
                                    in ? "readObject" : "writeObject", 
                                    in ? ObjectInputStream.class.getName() : ObjectOutputStream.class), 
                            toppestSerializableClass,
                            desc.forClass()
                    )
            );
        }
    }
    
    private Class<?> getToppestSerializableClass() {
        if (!(this instanceof Serializable)) {
            return null;
        }
        Class<?> clazz = this.getClass();
        Object toppestSerializableClass;
        Lock lock;
        (lock = CACHE_LOCK.readLock()).lock();
        try {
            toppestSerializableClass = CACHE.get(clazz);
        } finally {
            lock.unlock();
        }
        if (toppestSerializableClass == null) {
            (lock = CACHE_LOCK.writeLock()).lock();
            try {
                //The private method getToppestSerializableClass(Class<?>)
                //will execute the CACHE.put(clazz, toppestSerializableClass);
                toppestSerializableClass = getToppestSerializableClass(clazz);
            } finally {
                lock.unlock();
            }
        }
        return (Class<?>)toppestSerializableClass;
    }
    
    private static Class<?> getToppestSerializableClass(Class<?> clazz) {
        if (!Serializable.class.isAssignableFrom(clazz)) {
            return null;
        }
        Class<?> toppestSerializableClass;
        toppestSerializableClass = CACHE.get(clazz);
        if (toppestSerializableClass == null) {
            Arguments.mustNotBeInstanceOfValue("clazz", clazz, Externalizable.class);
            toppestSerializableClass = getToppestSerializableClass(clazz.getSuperclass());
            if (toppestSerializableClass == null) {
                toppestSerializableClass = clazz;
            } else {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().equals("onWriteState") &&
                            Arrays.equals(ON_WRITE_STATE_PARAMTER_TYPES, method.getParameterTypes())) {
                        throw new IllegalProgramException(invalidOverriding(method, method.getDeclaringClass()));
                    } else if (method.getName().equals("onReadState") &&
                            Arrays.equals(ON_READ_STATE_PARAMTER_TYPES, method.getParameterTypes())) {
                        throw new IllegalProgramException(invalidOverriding(method, method.getDeclaringClass()));
                    }
                }
            }
            CACHE.put(clazz, toppestSerializableClass);
        }
        return toppestSerializableClass;
    }
    
    private static Object valueOf(Object o, Field field) {
        try {
            return field.get(o);
        } catch (IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
    }
    
    private static Field fieldOf(Class<?> clazz, String name) {
        Field field;
        try {
            field = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            throw new AssertionError(ex);
        }
        field.setAccessible(true);
        return field;
    }
    
    protected static final class Output implements DataOutput {
        
        private ObjectOutputStream raw;
        
        private Output(ObjectOutputStream raw) {
            this.raw = raw;
        }
        
        public void writeObject(Object obj) throws IOException {
            this.raw.writeObject(obj);
        }

        @Override
        public void write(int b) throws IOException {
            this.raw.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            this.raw.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            this.raw.write(b, off, len);
        }

        @Override
        public void writeBoolean(boolean v) throws IOException {
            this.raw.writeBoolean(v);
        }

        @Override
        public void writeByte(int v) throws IOException {
            this.raw.writeByte(v);
        }

        @Override
        public void writeShort(int v) throws IOException {
            this.raw.writeShort(v);
        }

        @Override
        public void writeChar(int v) throws IOException {
            this.raw.writeChar(v);
        }

        @Override
        public void writeInt(int v) throws IOException {
            this.raw.writeInt(v);
        }

        @Override
        public void writeLong(long v) throws IOException {
            this.raw.writeLong(v);
        }

        @Override
        public void writeFloat(float v) throws IOException {
            this.raw.writeFloat(v);
        }

        @Override
        public void writeDouble(double v) throws IOException {
            this.raw.writeDouble(v);
        }

        @Override
        public void writeBytes(String s) throws IOException {
            this.raw.writeBytes(s);
        }

        @Override
        public void writeChars(String s) throws IOException {
            this.raw.writeChars(s);
        }

        @Override
        public void writeUTF(String s) throws IOException {
            this.raw.writeUTF(s);
        }
        
    }
    
    protected static final class Input implements DataInput {
        
        private ObjectInputStream raw;
        
        private Input(ObjectInputStream raw) {
            this.raw = raw;
        }
        
        @SuppressWarnings("unchecked")
        public <T> T readObject() throws ClassNotFoundException, IOException {
            return (T)this.raw.readObject();
        }

        @Override
        public void readFully(byte[] b) throws IOException {
            this.raw.readFully(b);
        }

        @Override
        public void readFully(byte[] b, int off, int len) throws IOException {
            this.raw.readFully(b, off, len);
        }

        @Override
        public int skipBytes(int n) throws IOException {
            return this.raw.skipBytes(n);
        }

        @Override
        public boolean readBoolean() throws IOException {
            return this.raw.readBoolean();
        }

        @Override
        public byte readByte() throws IOException {
            return this.raw.readByte();
        }

        @Override
        public int readUnsignedByte() throws IOException {
            return this.raw.readUnsignedByte();
        }

        @Override
        public short readShort() throws IOException {
            return this.raw.readShort();
        }

        @Override
        public int readUnsignedShort() throws IOException {
            return this.raw.readUnsignedShort();
        }

        @Override
        public char readChar() throws IOException {
            return this.raw.readChar();
        }

        @Override
        public int readInt() throws IOException {
            return this.raw.readInt();
        }

        @Override
        public long readLong() throws IOException {
            return this.raw.readLong();
        }

        @Override
        public float readFloat() throws IOException {
            return this.raw.readFloat();
        }

        @Override
        public double readDouble() throws IOException {
            return this.raw.readDouble();
        }

        @Deprecated
        @Override
        public String readLine() throws IOException {
            return this.raw.readLine();
        }

        @Override
        public String readUTF() throws IOException {
            return this.raw.readUTF();
        }
    }
    
    @I18N
    private static native String ownerIsNotSerializable(
            Class<? extends StatefulObject> runtimeType,
            String stateMethodSignature);
    
    @I18N
    private static native String invalidIOTarget(
            Class<? extends StatefulObject> runtimeType,
            String stateMethodSignature,
            String jdkIOMethodSignature);
    
    @I18N
    private static native String invalidIOOwner(
            Class<? extends StatefulObject> runtimeType,
            String stateMethodSignature,
            String jdkIOMethodSignature,
            Class<?> expectedType,
            Class<?> actualType);
    
    @I18N
    private static native String invalidOverriding(
            Method method,
            Class<?> declaringClass);
}
