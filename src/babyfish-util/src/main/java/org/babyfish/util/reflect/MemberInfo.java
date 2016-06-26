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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.Equality;
import org.babyfish.lang.OverrideEquality;

/**
 * @author Tao Chen
 */
public abstract class MemberInfo implements AnnotatedElement {
    
    Member member;
    
    ClassInfo<?> declaringClass;
    
    private transient int hash;
    
    MemberInfo(ClassInfo<?> declaringClass) {
        this.member = null;
        this.declaringClass = declaringClass;
    }
    
    MemberInfo(Member member, ClassInfo<?> declaringClass) {
        Arguments.mustBeInstanceOfValue("member", member, AnnotatedElement.class);
        this.member = member;
        this.declaringClass = declaringClass;
        
        Type[] genericParameterTypes = null;
        Type[] genericExceptionTypes = null;
        Type genericType = null;
        GenericResolver genericResolver = new GenericResolver(declaringClass.rawType, null);
        if (member instanceof Field) {
            genericType = ((Field)member).getGenericType();
        } else if (member instanceof Method) {
            genericParameterTypes = ((Method)member).getGenericParameterTypes();
            genericExceptionTypes = ((Method)member).getGenericExceptionTypes();
            genericType = ((Method)member).getGenericReturnType();
        } else if (member instanceof Constructor<?>) {
            genericParameterTypes = ((Constructor<?>)member).getGenericParameterTypes();
            genericExceptionTypes = ((Constructor<?>)member).getGenericExceptionTypes();
        }
        
        if (genericParameterTypes != null) {
            int index;
            Type[] resolvedGenericParameterTypes = new Type[genericParameterTypes.length];
            index = 0;
            for (Type genericParameterType : genericParameterTypes) {
                resolvedGenericParameterTypes[index++] = genericResolver.resolve(genericParameterType);
            }
            Class<?>[] resolvedParameterTypes = new Class[genericParameterTypes.length];
            index = 0;
            for (Type resolvedGenericParameterType : resolvedGenericParameterTypes) {
                resolvedParameterTypes[index++] = GenericTypes.eraseGenericType(resolvedGenericParameterType);
            }
            this.setResolvedParameterTypes(resolvedParameterTypes, resolvedGenericParameterTypes);
        }
        if (genericExceptionTypes != null) {
            int index;
            Type[] resolvedGenericExceptionTypes = new Type[genericExceptionTypes.length];
            index = 0;
            for (Type genericExceptionType : genericExceptionTypes) {
                resolvedGenericExceptionTypes[index++] = genericResolver.resolve(genericExceptionType);
            }
            Class<?>[] resolvedExceptionTypes = new Class[genericExceptionTypes.length];
            index = 0;
            for (Type resolvedGenericParameterType : resolvedGenericExceptionTypes) {
                resolvedExceptionTypes[index++] = GenericTypes.eraseGenericType(resolvedGenericParameterType);
            }
            this.setResolvedExceptionTypes(resolvedExceptionTypes, resolvedGenericExceptionTypes);
        }
        if (genericType != null) {
            Type resolvedGenericType = genericResolver.resolve(genericType);
            Class<?> resolvedType = GenericTypes.eraseGenericType(resolvedGenericType);
            this.setResolvedType(resolvedType, resolvedGenericType);
        }
    }
    
    void setResolvedType(Class<?> resolvedType, Type resolvedGenericType) {}
    
    void setResolvedExceptionTypes(Class<?>[] resolvedExceptionTypes, Type[] resolvedGenericExceptionTypes) {}
    
    void setResolvedParameterTypes(Class<?>[] resolvedParameterTypes, Type[] resolvedGenericParameterTypes) {}
    
    public ClassInfo<?> getDeclaringClass() {
        return this.declaringClass;
    }
    
    public abstract ModifierSet getModifiers();

    public String getName() {
        return this.member.getName();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return ((AnnotatedElement)this.member).getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return ((AnnotatedElement)this.member).getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return ((AnnotatedElement)this.member).getDeclaredAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return ((AnnotatedElement)this.member).isAnnotationPresent(annotationClass);
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            hash =
                this.declaringClass.hashCode() ^
                this.member.hashCode();
            if (hash == 0) {
                return -28574823;
            }
        }
        return hash;
    }

    @OverrideEquality
    @Override
    public boolean equals(Object obj) {
        Equality<MemberInfo> equality = Equality.of(MemberInfo.class, this, obj);
        MemberInfo other = equality.other();
        if (other == null) {
            return equality.returnValue();
        }
        return 
            this.declaringClass.equals(other.declaringClass) &&
            this.member.equals(other.member);
    }

    @Override
    public String toString() {
        return this.member.toString();
    }
    
    public abstract String toGenericString();
    
    public abstract String toResolvedString();
    
    public abstract String toResolvedGenericString();
    
}
