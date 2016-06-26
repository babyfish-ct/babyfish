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

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

import org.babyfish.collection.MACollections;

/**
 * @author Tao Chen
 */
public abstract class MethodBase extends MemberInfo {
    
    private List<TypeVariable<?>> typeParameters;
    
    private List<Class<?>> parameterTypes;
    
    private List<Type> genericParameterTypes;
    
    private List<Class<?>> resolvedParameterTypes;
    
    private List<Type> resolvedGenericParameterTypes;
    
    private List<Class<?>> exceptionTypes;
    
    private List<Type> genericExceptionTypes;
    
    private List<Class<?>> resolvedExceptionTypes;
    
    private List<Type> resolvedGenericExceptionTypes;

    MethodBase(Method method, ClassInfo<?> declaringType) {
        super (method, declaringType);
        this.typeParameters = MACollections.wrap((TypeVariable<?>[])method.getTypeParameters());
        this.parameterTypes = MACollections.wrap(method.getParameterTypes());
        this.genericParameterTypes = MACollections.wrap(method.getGenericParameterTypes());
        this.exceptionTypes = MACollections.wrap(method.getExceptionTypes());
        this.genericExceptionTypes = MACollections.wrap(method.getGenericExceptionTypes());
    }
    
    MethodBase(Constructor<?> constructor, ClassInfo<?> declaringType) {
        super (constructor, declaringType);
        this.typeParameters = MACollections.wrap((TypeVariable<?>[])constructor.getTypeParameters());
        this.parameterTypes = MACollections.wrap(constructor.getParameterTypes());
        this.genericParameterTypes = MACollections.wrap(constructor.getGenericParameterTypes());
        this.exceptionTypes = MACollections.wrap(constructor.getExceptionTypes());
        this.genericExceptionTypes = MACollections.wrap(constructor.getGenericExceptionTypes());
    }
    
    @Override
    void setResolvedParameterTypes(Class<?>[] resolvedParameterTypes, Type[] resolvedGenericParameterTypes) {
        this.resolvedParameterTypes = MACollections.wrap(resolvedParameterTypes);
        this.resolvedGenericParameterTypes = MACollections.wrap(resolvedGenericParameterTypes);
    }
    
    @Override
    void setResolvedExceptionTypes(Class<?>[] resolvedExceptionTypes, Type[] resolvedGenericExceptionTypes) {
        this.resolvedExceptionTypes = MACollections.wrap(resolvedExceptionTypes);
        this.resolvedGenericExceptionTypes = MACollections.wrap(resolvedGenericExceptionTypes);
    }
    
    @Override
    public ModifierSet getModifiers() {
        return ModifierSet.forMethod(this.member.getModifiers());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <D extends GenericDeclaration> List<TypeVariable<D>> getTypeParameters() {
        return (List)this.typeParameters;
    }

    public List<Class<?>> getParameterTypes() {
        return this.parameterTypes;
    }
    
    public List<Type> getGenericParameterTypes() {
        return this.genericParameterTypes;
    }
    
    public List<Class<?>> getResolvedParameterTypes() {
        return this.resolvedParameterTypes;
    }
    
    public List<Type> getResolvedGenericParameterTypes() {
        return this.resolvedGenericParameterTypes;
    }
    
    public List<Class<?>> getExceptionTypes() {
        return this.exceptionTypes;
    }

    public List<Type> getGenericExceptionTypes() {
        return this.genericExceptionTypes;
    }

    public List<Class<?>> getResolvedExceptionTypes() {
        return this.resolvedExceptionTypes;
    }

    public List<Type> getResolvedGenericExceptionTypes() {
        return this.resolvedGenericExceptionTypes;
    }
    
    @Override
    public String toGenericString() {
        return this.toString(
                this.declaringClass.toGenericString(),
                this instanceof MethodInfo ? ((MethodInfo)this).getGenericReturnType() : null,
                this.genericParameterTypes,
                this.genericExceptionTypes);
    }
    
    @Override
    public String toResolvedString() {
        return this.toString(
                this.declaringClass.toResolvedString(),
                this instanceof MethodInfo ? ((MethodInfo)this).getResolvedReturnType() : null,
                this.resolvedParameterTypes,
                this.resolvedExceptionTypes);
    }

    @Override
    public String toResolvedGenericString() {
        return this.toString(
                this.declaringClass.toResolvedGenericString(),
                this instanceof MethodInfo ? ((MethodInfo)this).getResolvedGenericReturnType() : null,
                this.resolvedGenericParameterTypes,
                this.resolvedGenericExceptionTypes);
    }
    
    private String toString(
            String declaringTypeName,
            Type returnType, 
            List<? extends Type> parameterTypes,
            List<? extends Type> exceptionTypes) {
        StringBuilder builder = new StringBuilder();
        boolean addComma;
        if (this.member.getModifiers() != 0) {
            builder.append(this.getModifiers().toString());
            builder.append(' ');
        }
        if (!this.typeParameters.isEmpty()) {
            builder.append('<');
            addComma = false;
            for (TypeVariable<?> typeParameter : this.typeParameters) {
                if (addComma) {
                    builder.append(", ");
                } else {
                    addComma = true;
                }
                builder.append(typeParameter.toString());
            }
            builder.append("> ");
        }
        if (returnType != null) {
            builder.append(ClassInfo.toString(returnType));
            builder.append(' ');
        }
        builder.append(declaringTypeName);
        builder.append('.');
        builder.append(this.member.getName());
        builder.append('(');
        addComma = false;
        for (Type parameterType : parameterTypes) {
            if (addComma) {
                builder.append(", ");
            } else {
                addComma = true;
            }
            builder.append(ClassInfo.toString(parameterType));
        }
        builder.append(')');
        if (!exceptionTypes.isEmpty()) {
            builder.append(" throws ");
            addComma = false;
            for (Type exceptionType : this.exceptionTypes) {
                if (addComma) {
                    builder.append(", ");
                } else {
                    addComma = true;
                }
                builder.append(ClassInfo.toString(exceptionType));
            }
        }
        return builder.toString();
    }
    
}
