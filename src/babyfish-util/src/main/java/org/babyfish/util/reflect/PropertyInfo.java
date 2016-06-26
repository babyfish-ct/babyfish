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

import java.lang.reflect.Type;
import java.util.List;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Strings;

/**
 * @author Tao Chen
 */
public class PropertyInfo extends MemberInfo {
    
    final ModifierSet modifiers;
    
    final String name;
    
    final MethodInfo getter;
    
    final MethodInfo setter;
    
    final Class<?> returnType;
    
    final Type genericReturnType;
    
    final Class<?> resolvedReturnType;
    
    final Type resolvedGenericReturnType;
    
    final List<Class<?>> parameterTypes;
    
    final List<Type> genericParameterTypes;
    
    final List<Class<?>> resolvedParameterTypes;
    
    final List<Type> resolvedGenericParameterTypes;
    
    PropertyInfo(MethodInfo method) {
        super(method.getDeclaringClass());
        MethodInfo getter = getGetterByAnyMethod(method);
        MethodInfo setter = 
            method.equals(getter) ?
                    (getter != null ? getSetterByGetter(getter) : null) :
                    method;
        if (getter == null && setter == null) {
            //This is a private exception, catch it in this class so that it can not be thrown to other modules
            throw new InvalidPropertyException();
        }
        String name; 
        if (getter != null) {
            name = getter.getName();
            name = name.substring(name.startsWith("is") ? 2 : 3);
        } else {
            name = setter.getName().substring(3);
        }
        if (!name.isEmpty()) {
            name = Strings.toCamelCase(name);
        }
        this.name = name;
        this.getter = getter;
        this.setter = setter;
        if (getter != null) {
            this.member = getter.getRawMethod();
        } else {
            this.member = setter.getRawMethod();
        }
        if (this.getter == null) {
            this.modifiers = this.setter.getModifiers();
        } else if (this.setter == null) {
            this.modifiers = this.getter.getModifiers();
        } else {
            this.modifiers = mergeModifiers(
                    this.getter.getModifiers(), 
                    this.getter.getModifiers());
        }
        int parameterCount = 
            getter != null ? 
                    getter.getParameterTypes().size() : 
                    setter.getParameterTypes().size() - 1;
        this.returnType = 
            getter != null ? 
                    getter.getReturnType() : 
                    last(setter.getParameterTypes());
        this.genericReturnType =
            getter != null ?
                    getter.getGenericReturnType() :
                    last(setter.getGenericParameterTypes());
        this.resolvedReturnType =
            getter != null ?
                    getter.getResolvedReturnType() :
                    last(setter.getResolvedParameterTypes());
        this.resolvedGenericReturnType =
            getter != null ?
                    getter.getResolvedGenericReturnType() :
                    last(setter.getResolvedGenericParameterTypes());
        this.parameterTypes = 
            MACollections.wrap(
                    getter != null ? 
                            copyToArray(getter.getParameterTypes(), new Class[parameterCount]) : 
                            copyToArray(setter.getParameterTypes(), new Class[parameterCount]));
        this.genericParameterTypes =
            MACollections.wrap(
                    getter != null ?
                            copyToArray(getter.getGenericParameterTypes(), new Type[parameterCount]):
                            copyToArray(setter.getGenericParameterTypes(), new Type[parameterCount]));
        this.resolvedParameterTypes =
            MACollections.wrap(
                    getter != null ?
                            copyToArray(getter.getResolvedParameterTypes(), new Class[parameterCount]) :
                            copyToArray(setter.getResolvedParameterTypes(), new Class[parameterCount]));
        this.resolvedGenericParameterTypes =
            MACollections.wrap(
                    getter != null ?
                            copyToArray(getter.getResolvedGenericParameterTypes(), new Type[parameterCount]) :
                            copyToArray(setter.getResolvedGenericParameterTypes(), new Type[parameterCount]));
    }
    
    public MethodInfo getGetter() {
        return this.getter;
    }
    
    public MethodInfo getSetter() {
        return this.setter;
    }

    @Override
    public ModifierSet getModifiers() {
        return this.modifiers.clone();
    }

    @Override
    public String getName() {
        return this.name;
    }
    
    public Class<?> getReturnType() {
        return this.returnType;
    }
    
    public Type getGenericReturnType() {
        return this.genericReturnType;
    }
    
    public Class<?> getResolvedReturnType() {
        return this.resolvedReturnType;
    }
    
    public Type getResolvedGenericReturnType() {
        return this.resolvedGenericReturnType;
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

    @Override
    public String toString() {
        return this.toString(
                this.declaringClass.toString(), 
                this.returnType, 
                this.parameterTypes);
    }

    @Override
    public String toGenericString() {
        return this.toString(
                this.declaringClass.toGenericString(), 
                this.genericReturnType, 
                this.genericParameterTypes);
    }

    @Override
    public String toResolvedString() {
        return this.toString(
                this.declaringClass.toResolvedString(), 
                this.resolvedReturnType, 
                this.resolvedParameterTypes);
    }

    @Override
    public String toResolvedGenericString() {
        return this.toString(
                this.declaringClass.toResolvedGenericString(), 
                this.resolvedGenericReturnType, 
                this.resolvedGenericParameterTypes);
    }
    
    private <T extends Type> String toString(String declaringClass, T returnType, List<T> parameterTypes) {
        StringBuilder builder = new StringBuilder();
        ModifierSet modifiers = this.modifiers;
        if (!modifiers.isEmpty()) {
            builder
            .append(modifiers.toString())
            .append(' ');
        }
        builder
        .append(ClassInfo.toString(returnType))
        .append(' ')
        .append(declaringClass)
        .append('.')
        .append(this.name.isEmpty() ? "this" : this.name);
        
        if (!parameterTypes.isEmpty()) {
            builder.append('[');
            boolean isFirst = true;
            for (T parameterType : parameterTypes) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(", ");
                }
                builder.append(ClassInfo.toString(parameterType));
            }
            builder.append(']');
        }
        
        builder.append(" { ");
        if (this.getter != null) {
            ModifierSet specialGetterModifiers = this.getSpecialModifiers(this.getter);
            if (!specialGetterModifiers.isEmpty()) {
                builder
                .append(specialGetterModifiers)
                .append(' ');
            }
            builder.append("get;");
        }
        if (this.setter != null) {
            ModifierSet specialSetterModifiers = this.getSpecialModifiers(this.setter);
            if (!specialSetterModifiers.isEmpty()) {
                builder
                .append(specialSetterModifiers)
                .append(' ');
            }
            builder.append("set;");
        }
        builder.append(" }");
        return builder.toString();
    }
    
    @Override
    public int hashCode() {
        return hashCode(this.getter) ^ hashCode(this.setter);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PropertyInfo)) {
            return false;
        }
        PropertyInfo other = (PropertyInfo)obj;
        return 
            equals(this.getter, other.getter) && 
            equals(this.setter, other.setter);
    }

    private static MethodInfo getGetterByAnyMethod(MethodInfo method) {
        if (!method.getTypeParameters().isEmpty()) {
            return null;
        }
        String name = method.getName();
        Class<?> returnType = method.getReturnType();
        if (name.startsWith("get")) {
            if (returnType == void.class) {
                return null;
            }
            return method;
        }
        if (name.startsWith("is")) {
            if (returnType != boolean.class && returnType != Boolean.class) {
                return null;
            }
            return method;
        }
        if (name.startsWith("set")) {
            List<Class<?>> parameterTypes = method.getParameterTypes();
            if (!parameterTypes.isEmpty()) {
                Class<?>[] getterParameterTypes = 
                    copyToArray(
                            parameterTypes, 
                            new Class[parameterTypes.size() - 1]);
                String getterName = "get" + name.substring(3);
                MethodInfo getter = null;
                try {
                    getter =
                        method
                        .getDeclaringClass()
                        .getDeclaredErasedMethod(
                                getterName,  getterParameterTypes);
                } catch (NoSuchMethodInfoException ex) {
                    if (parameterTypes.get(parameterTypes.size() - 1) == boolean.class ||
                            parameterTypes.get(parameterTypes.size() - 1) == Boolean.class) {
                        getterName = "is" + name.substring(3);
                        try {
                            getter = 
                                method
                                .getDeclaringClass()
                                .getDeclaredErasedMethod(
                                        getterName, getterParameterTypes);
                        } catch (NoSuchMethodInfoException exEx) {
                            // Ingore exception
                        }
                    }
                }
                if (getter != null &&
                        getter.getTypeParameters().isEmpty() &&
                        getter.getModifiers().isStatic() == method.getModifiers().isStatic()) {
                    return getter;
                }
            }
        }
        return null;
    }
    
    private static MethodInfo getSetterByGetter(MethodInfo getter) {
        List<Class<?>> parameterTypes = getter.getParameterTypes();
        Class<?>[] setterParameterTypes = new Class[parameterTypes.size() + 1];
        copyToArray(parameterTypes, setterParameterTypes);
        setterParameterTypes[setterParameterTypes.length - 1] = getter.getReturnType();
        String setterName = getter.getName();
        setterName = "set" + setterName.substring(setterName.startsWith("is") ? 2 : 3);
        try {
            MethodInfo setter = 
                getter
                .getDeclaringClass()
                .getDeclaredErasedMethod(
                        setterName, setterParameterTypes);
            if (setter.getTypeParameters().isEmpty() &&
                    setter.getModifiers().isStatic() == getter.getModifiers().isStatic()) {
                return setter;
            }
        } catch (NoSuchMethodInfoException e) {
            // Ignore exception
        }
        return null;
    }
    
    private static <T> T last(List<T> list) {
        return list.get(list.size() - 1);
    }
    
    private static <T> T[] copyToArray(List<T> list, T[] arr) {
        int lastIndex = list.size() < arr.length ? list.size() - 1 : arr.length - 1;
        for (int i = lastIndex; i >= 0; i--) {
            arr[i] = list.get(i);
        }
        return arr;
    }
    
    private static ModifierSet mergeModifiers(ModifierSet set1, ModifierSet set2) {
        ModifierSet modifiers = ModifierSet.forMethod();
        if (set1.isPublic() || set2.isPublic()) {
            modifiers.add(Modifier.PUBLIC);
        } else if (set1.isProtected() || set2.isProtected()) {
            modifiers.add(Modifier.PROTECTED);
        } else if (set1.isPrivate() && set2.isPrivate()) {
            modifiers.add(Modifier.PRIVATE);
        }
        if (set1.isStatic() && set2.isStatic()) {
            modifiers.add(Modifier.STATIC);
        }
        if (set1.isAbstract() && set2.isAbstract()) {
            modifiers.add(Modifier.ABSTRACT);
        }
        if (set1.isFinal() && set2.isFinal()) {
            modifiers.add(Modifier.FINAL);
        }
        if (set1.isSynchronzied() && set2.isSynchronzied()) {
            modifiers.add(Modifier.SYNCHRONIZED);
        }
        if (set1.isSynthetic() && set2.isSynthetic()) {
            modifiers.add(Modifier.SYNCHRONIZED);
        }
        if (set1.isBridge() && set2.isBridge()) {
            modifiers.add(Modifier.BRIDGE);
        }
        return modifiers;
    }
    
    private static int hashCode(MethodInfo mi) {
        return mi == null ? 0 : mi.hashCode();
    }
    
    private static boolean equals(MethodInfo mi1, MethodInfo mi2) {
        return mi1 == null ? mi2 == null : mi1.equals(mi2);
    }
    
    private ModifierSet getSpecialModifiers(MethodInfo method) {
        ModifierSet modifiers = method.getModifiers();
        modifiers.removeAll(this.getModifiers());
        return modifiers;
    }
    
    static class InvalidPropertyException extends RuntimeException {

        private static final long serialVersionUID = -7617335294430306954L;

        public InvalidPropertyException() {
            
        }
        
    }
    
}
