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
package org.babyfish.util.reflect;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Equality;
import org.babyfish.lang.Strings;
import org.babyfish.lang.OverrideEquality;

/**
 * @author Tao Chen
 */
public class MethodDescriptor implements Serializable {

    private static final long serialVersionUID = 7268940702524417064L;

    private Class<?> returnType;
    
    private String name;
    
    private List<Class<?>> parameterTypes;
    
    private transient int hash;
    
    private MethodDescriptor() {
        
    }
    
    public static MethodDescriptor erased(MethodInfo methodInfo) {
        MethodDescriptor descriptor = new MethodDescriptor();
        descriptor.returnType = methodInfo.getReturnType();
        descriptor.name = methodInfo.getName();
        descriptor.parameterTypes = methodInfo.getParameterTypes();
        return descriptor;
    }
    
    public static MethodDescriptor resolved(MethodInfo methodInfo) {
        MethodDescriptor descriptor = new MethodDescriptor();
        descriptor.returnType = methodInfo.getResolvedReturnType();
        descriptor.name = methodInfo.getName();
        descriptor.parameterTypes = methodInfo.getResolvedParameterTypes();
        return descriptor;
    }
    
    public MethodDescriptor(Class<?> returnType, String name, Class<?> ... parameterTypes) {
        Arguments.mustNotBeNull("returnType", returnType);
        Arguments.mustNotBeNull("name", name);
        this.returnType = returnType;
        this.name = name;
        if (parameterTypes != null) {
            for (Class<?> parameterType : parameterTypes) {
                if (parameterType == null) {
                    throw new IllegalArgumentException(
                            "When parameterTypes is not null, it should not contains null element.");
                }
            }
            this.parameterTypes = MACollections.wrap(parameterTypes.clone());
        } else {
            this.parameterTypes = MACollections.wrap((Class<?>[])null);
        }
    }
    
    public MethodDescriptor(Class<?> returnType, String name, List<Class<?>> parameterTypes) {
        Arguments.mustNotBeNull("returnType", returnType);
        Arguments.mustNotBeNull("name", name);
        this.returnType = returnType;
        this.name = name;
        if (parameterTypes != null) {
            for (Class<?> parameterType : parameterTypes) {
                if (parameterType == null) {
                    throw new IllegalArgumentException(
                            "When parameterTypes is not null, it should not contains null element.");
                }
            }
            this.parameterTypes = 
                MACollections.<Class<?>>wrap(
                        parameterTypes.toArray(
                                new Class[parameterTypes.size()]));
        } else {
            this.parameterTypes = MACollections.wrap((Class<?>[])null);
        }
    }

    public Class<?> getReturnType() {
        return this.returnType;
    }
    
    public String getName() {
        return this.name;
    }
    
    public List<Class<?>> getParameterTypes() {
        return this.parameterTypes;
    }
    
    public boolean isAssignableFrom(MethodDescriptor descriptor) {
        if (this == descriptor) {
            return true;
        }
        if (!this.name.equals(descriptor.name)) {
            return false;
        }
        if (this.parameterTypes.size() != descriptor.parameterTypes.size()) {
            return false;
        }
        if (!this.returnType.isAssignableFrom(descriptor.returnType)) {
            return false;
        }
        Iterator<Class<?>> parameterTypeIterator = this.parameterTypes.iterator();
        Iterator<Class<?>> descriptorParameterTypeIterator = descriptor.parameterTypes.iterator();
        while (parameterTypeIterator.hasNext()) {
            if (!parameterTypeIterator.next().isAssignableFrom(descriptorParameterTypeIterator.next())) {
                return false;
            }
        }
        return true;
    }
    
    public String toByteCodeDescriptor() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        for (Class<?> parameterType : this.parameterTypes) {
            appendByteCodeDescriptor(parameterType, builder);
        }
        builder.append(')');
        appendByteCodeDescriptor(this.returnType, builder);
        return builder.toString();
    }
    
    public boolean match(String name, Class<?> ... parameterTypes) {
        if (!this.name.equals(name)) {
            return false;
        }
        List<Class<?>> thisParameterTypes = this.parameterTypes;
        int size = thisParameterTypes.size();
        if (size != parameterTypes.length) {
            return false;
        }
        for (int i = size - 1; i >= 0; i--) {
            if (thisParameterTypes.get(i) != parameterTypes[i]) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            hash = hashCode0();
            if (hash == 0) {
                hash = 952307581;
            }
            this.hash = hash;
        }
        return hash;
    }

    @OverrideEquality
    @Override
    public boolean equals(Object obj) {
        Equality<MethodDescriptor> equality = Equality.of(MethodDescriptor.class, this, obj);
        MethodDescriptor other = equality.other();
        if (other == null) {
            return equality.returnValue();
        }
        return 
            this.returnType.equals(other.getReturnType()) &&
            this.name.equals(other.getName()) &&
            this.parameterTypes.equals(other.getParameterTypes());
    }

    @Override
    public String toString() {
        return 
            this.returnType.getName() + 
            " " + 
            this.name +
            "(" +
            Strings.join(
                    this.parameterTypes, 
                    ", ", 
                    cls -> cls.getName()
            ) +
            ")";
    }
    
    private int hashCode0() {
        int hash = this.getReturnType().hashCode();
        hash = hash * 31 + this.getName().hashCode();
        hash = hash * 31 + this.getParameterTypes().hashCode();
        return hash;
    }
    
    private static void appendByteCodeDescriptor(Class<?> clazz, StringBuilder builder) {
        if (clazz == void.class) {
            builder.append('V');
        } else if (clazz == boolean.class) {
            builder.append('Z');
        } else if (clazz == char.class) {
            builder.append('C');
        } else if (clazz == byte.class) {
            builder.append('B');
        } else if (clazz == short.class) {
            builder.append('S');
        } else if (clazz == int.class) {
            builder.append('I');
        } else if (clazz == long.class) {
            builder.append('J');
        } else if (clazz == float.class) {
            builder.append('F');
        } else if (clazz == double.class) {
            builder.append('D');
        } else if (clazz.isArray()) {
            builder.append('[');
            appendByteCodeDescriptor(clazz.getComponentType(), builder);
        } else {
            builder.append('L');
            builder.append(clazz.getName().replace('.', '/'));
            builder.append(';');
        }
    }
    
}
