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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * @author Tao Chen
 */
public final class FieldInfo extends MemberInfo {
    
    private Class<?> resolvedReturnType;
    
    private Type resolvedGenericReturnType;

    FieldInfo(Field field, ClassInfo<?> declaringType) {
        super (field, declaringType);
    }
    
    @Override
    void setResolvedType(Class<?> resolvedType, Type resolvedGenericType) {
        this.resolvedReturnType = resolvedType;
        this.resolvedGenericReturnType = resolvedGenericType;
    }

    public Field getRawField() {
        return (Field)this.member;
    }
    
    @Override
    public ModifierSet getModifiers() {
        return ModifierSet.forField(this.member.getModifiers());
    }
    
    public Class<?> getType() {
        return ((Field)this.member).getType();
    }
    
    public Type getGenericType() {
        return ((Field)this.member).getGenericType();
    }
    
    public Class<?> getResolvedType() {
        return this.resolvedReturnType;
    }
    
    public Type getResolvedGenericType() {
        return this.resolvedGenericReturnType;
    }
    
    @Override
    public String toGenericString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getModifiers().toString());
        builder.append(' ');
        builder.append(ClassInfo.toString(this.getGenericType()));
        builder.append(' ');
        builder.append(this.declaringClass.toGenericString());
        builder.append('.');
        builder.append(this.member.getName());
        return builder.toString();
    }
    
    @Override
    public String toResolvedString() {
        StringBuilder builder = new StringBuilder();
        if (this.member.getModifiers() != 0) {
            builder.append(Modifier.toString(this.member.getModifiers()));
            builder.append(' ');
        }
        builder.append(' ');
        builder.append(ClassInfo.toString(this.getResolvedType()));
        builder.append(' ');
        builder.append(this.declaringClass.toResolvedString());
        builder.append('.');
        builder.append(this.member.getName());
        return builder.toString();
    }

    @Override
    public String toResolvedGenericString() {
        StringBuilder builder = new StringBuilder();
        if (this.member.getModifiers() != 0) {
            builder.append(Modifier.toString(this.member.getModifiers()));
            builder.append(' ');
        }
        builder.append(' ');
        builder.append(ClassInfo.toString(this.getResolvedGenericType()));
        builder.append(' ');
        builder.append(this.declaringClass.toResolvedGenericString());
        builder.append('.');
        builder.append(this.member.getName());
        return builder.toString();
    }
    
}
