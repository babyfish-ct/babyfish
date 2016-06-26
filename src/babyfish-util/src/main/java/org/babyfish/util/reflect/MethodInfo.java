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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

/**
 * @author Tao Chen
 */
public final class MethodInfo extends MethodBase {
    
    private MethodDescriptor descriptor;
    
    private MethodDescriptor resolvedDescriptor;
    
    private Class<?> resolvedReturnType;
    
    private Type resolvedGenericReturnType;
    
    MethodInfo(Method method, ClassInfo<?> declaringType) {
        super (method, declaringType);
    }
    
    @Override
    void setResolvedType(Class<?> resolvedType, Type resolvedGenericType) {
        this.resolvedReturnType = resolvedType;
        this.resolvedGenericReturnType = resolvedGenericType;
    }

    public Method getRawMethod() {
        return (Method)this.member;
    }

    public MethodDescriptor getDescriptor() {
        MethodDescriptor descriptor = this.descriptor;
        if (descriptor ==  null) {
            this.descriptor = descriptor = MethodDescriptor.erased(this);
        }
        return descriptor;
    }
    
    public MethodDescriptor getResolvedDescriptor() {
        MethodDescriptor resolvedDescriptor = this.resolvedDescriptor;
        if (resolvedDescriptor ==  null) {
            this.resolvedDescriptor = resolvedDescriptor = MethodDescriptor.resolved(this);
        }
        return resolvedDescriptor;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<TypeVariable<Method>> getTypeParameters() {
        return super.getTypeParameters();
    }
    
    public Class<?> getReturnType() {
        return ((Method)this.member).getReturnType();
    }
    
    public Type getGenericReturnType() {
        return ((Method)this.member).getGenericReturnType();
    }
    
    public Class<?> getResolvedReturnType() {
        return this.resolvedReturnType;
    }
    
    public Type getResolvedGenericReturnType() {
        return this.resolvedGenericReturnType;
    }
    
    public boolean isAnnotationPresentByInheritance(Class<? extends Annotation> annotationClass) {
        return this.isAnnotationPresentByInheritance(annotationClass, this.declaringClass);
    }
    
    private boolean isAnnotationPresentByInheritance(
            Class<? extends Annotation> annotationClass, ClassInfo<?> classInfo) {
        MethodInfo methodInfo;
        if (this.declaringClass.equals(classInfo)) {
            methodInfo = this;
        } else {
            methodInfo = classInfo.tryGetDeclaredResolvedMethod(
                    null,
                    this.getName(), 
                    this.getResolvedParameterTypes());
            if (methodInfo == null) {
                return false;
            }
        }
        if (methodInfo.isAnnotationPresent(annotationClass)) {
            return true;
        }
        ClassInfo<?> superClassInfo = classInfo.getSuperClass();
        if (superClassInfo != null && 
                this.isAnnotationPresentByInheritance(
                        annotationClass, superClassInfo)) {
            return true;
        }
        for (ClassInfo<?> superInterface : classInfo.getInterfaces()) {
            if (this.isAnnotationPresentByInheritance(annotationClass, superInterface)) {
                return true;
            }
        }
        return false;
    }

}
