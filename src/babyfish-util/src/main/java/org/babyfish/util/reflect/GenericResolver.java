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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.UncheckedException;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.util.reflect.runtime.ASM;

/**
 * @author Tao Chen
 */
final class GenericResolver {
    
    private static final TypeVariableWrapperFactory TYPE_VARIABLE_WRAPPER_FACTORY = wrapperFactory();
    
    private Type mappingType;
    
    public final Type rawTypeForClassInfo;
    
    public GenericResolver(
            Type type,
            GenericResolver derivedClassResolver) {
        if (type instanceof Class<?> && ClassInfo.isGenericClass((Class<?>)type)) {
            type = GenericTypes.cascadeMakeTypeOrParameterizedType((Class<?>)type);
        }
        if (derivedClassResolver != null) {
            type = derivedClassResolver.resolve0(type);
        }
        this.mappingType = transformType(type, true);
        this.rawTypeForClassInfo = transformType(type, false);
    }
    
    public Class<?> getRawClass() {
        return 
            this.mappingType instanceof Class<?> ?
                    (Class<?>)this.mappingType :
                    (Class<?>)((ParameterizedType)this.mappingType).getRawType();
    }
    
    public Type resolve(Type type) {
        type = this.resolve0(type);
        return transformType(type, false);
    }
    
    private Type resolve0(Type type) {
        if (type instanceof TypeVariable<?>) {
            final TypeVariable<?> typeVariable = (TypeVariable<?>)type;
            type = this.mapTypeVariable(typeVariable);
            if (type != typeVariable) {
                return this.resolve0(type);
            }
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            for (int i = typeArguments.length - 1; i >= 0; i--) {
                typeArguments[i] = this.resolve0(typeArguments[i]);
            }
            Type ownerType = parameterizedType.getOwnerType();
            if (ownerType != null) {
                ownerType = this.resolve0(ownerType);
            }
            return GenericTypes.makeParameterizedType(
                    false,
                    ownerType,
                    (Class<?>)parameterizedType.getRawType(), 
                    typeArguments);
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType)type).getGenericComponentType();
            componentType = this.resolve0(componentType);
            if (componentType instanceof Class<?>) {
                return Array.newInstance((Class<?>)componentType, 0).getClass();
            }
            return GenericTypes.makeGenericArrayType(componentType);
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType)type;
            Type[] lowerBounds = wildcardType.getLowerBounds();
            Type[] upperBounds = wildcardType.getUpperBounds();
            for (int i = lowerBounds.length - 1; i >= 0; i--) {
                lowerBounds[i] = this.resolve0(lowerBounds[i]);
            }
            for (int i = upperBounds.length - 1; i >= 0; i--) {
                upperBounds[i] = this.resolve0(upperBounds[i]);
            }
            return GenericTypes.makeWildcardType(lowerBounds, upperBounds);
        }
        return type;
    }

    @Override
    public int hashCode() {
        throw new AssertionError("internal bug: GenericResolver can not be storaged into HashMap or HashSet");
    }

    @Override
    public boolean equals(Object obj) {
        throw new AssertionError("internal bug: Can not compare GenericResolver to other");
    }
    
    static TypeVariable<?> normalTypeVariable(TypeVariable<?> typeVariable) {
        if (typeVariable instanceof TypeVariableWrapper) {
            return ((TypeVariableWrapper)typeVariable).unwrap();
        }
        return typeVariable;
    }
    
    private Type mapTypeVariable(TypeVariable<?> typeVariable) {
        Type type = this.mappingType;
        while (type instanceof ParameterizedType && 
                !(typeVariable instanceof TypeVariableWrapper) &&
                typeVariable.getGenericDeclaration() instanceof Class<?>) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            TypeVariable<?>[] typeParameters = ((Class<?>)parameterizedType.getRawType()).getTypeParameters();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length != 0) {
                for (int i = 0; i < typeParameters.length; i++) {
                    if (!(typeVariable instanceof TypeVariableWrapper) && typeParameters[i].equals(typeVariable)) {
                        if (!(actualTypeArguments[i] instanceof TypeVariable<?>)) {
                            return actualTypeArguments[i];
                        }
                        typeVariable = (TypeVariable<?>)actualTypeArguments[i];
                    }
                }
            }
            type = parameterizedType.getOwnerType();
        }
        return typeVariable;
    }
    
    private static Type transformType(Type type, boolean toReplacedTypeVariables) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            for (int i = 0; i < actualTypeArguments.length; i++) {
                actualTypeArguments[i] = transformType(actualTypeArguments[i], toReplacedTypeVariables);
            }
            Type ownerType = parameterizedType.getOwnerType();
            if (ownerType != null) {
                ownerType = transformType(ownerType, toReplacedTypeVariables);
            }
            return GenericTypes.makeParameterizedType(
                    ownerType, (Class<?>)parameterizedType.getRawType(), actualTypeArguments);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType)type;
            Type componentType = genericArrayType.getGenericComponentType();
            componentType = transformType(componentType, toReplacedTypeVariables);
            return GenericTypes.makeGenericArrayType(componentType);
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType)type;
            Type[] lowerBounds = wildcardType.getLowerBounds();
            Type[] upperBounds = wildcardType.getUpperBounds();
            for (int i = 0; i < lowerBounds.length; i++) {
                lowerBounds[i] = transformType(lowerBounds[i], toReplacedTypeVariables);
            }
            for (int i = 0; i < upperBounds.length; i++) {
                upperBounds[i] = transformType(upperBounds[i], toReplacedTypeVariables);
            }
            return GenericTypes.makeWildcardType(lowerBounds, upperBounds);
        } else if (type instanceof TypeVariable<?>) {
            if (toReplacedTypeVariables && !(type instanceof TypeVariableWrapper)) {
                return TYPE_VARIABLE_WRAPPER_FACTORY.create((TypeVariable<?>)type);
            }
            if (!toReplacedTypeVariables && type instanceof TypeVariableWrapper) {
                return ((TypeVariableWrapper)type).unwrap();
            }
        }
        return type;
    }
    
    /*
     * Java8 changed "java.lang.reflect.TypeVariable<?>" so that it impossible
     * to create a custom implementation of TypeVariable<?> that can be compiled
     * successfully under both JDK7 and JDK8. Use byte-code to do it.
     * 
     * This is the abstract class for the derived class that's generated dynamically
     */
    private static abstract class TypeVariableWrapper implements TypeVariable<GenericDeclaration> {
        
        private TypeVariable<?> target;
        
        @SuppressWarnings("unused") // Use the the byte-code that's generated dynamically
        protected TypeVariableWrapper(TypeVariable<?> target) {
            this.target = Arguments.mustNotBeInstanceOfValue(
                    "target",
                    Arguments.mustNotBeNull("target", target), 
                    TypeVariableWrapper.class
            );
        }
        
        public TypeVariable<?> unwrap() {
            return this.target;
        }

        @Override
        public int hashCode() {
            throw new UnsupportedOperationException("Internal bug, TypeVariableWrapper supports nothing");
        }

        @Override
        public boolean equals(Object obj) {
            throw new UnsupportedOperationException("Internal bug, TypeVariableWrapper supports nothing");
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException("Internal bug, TypeVariableWrapper supports nothing");
        }
    }
    
    private interface TypeVariableWrapperFactory {
        
        TypeVariableWrapper create(TypeVariable<?> typeVariable);
    }

    /*
     * Java8 changed "java.lang.reflect.TypeVariable<?>" so that it impossible
     * to create a custom implementation of TypeVariable<?> that can be compiled
     * successfully under both JDK7 and JDK8. Use byte-code to do it.
     */
    @SuppressWarnings("unchecked")
    private static TypeVariableWrapperFactory wrapperFactory() {
        String namespacePostfix = "92B8C17E_BF4E_4135_B596_5A76E0FEBF4E";
        final String typeVariableWrapperClassName = 
                TypeVariableWrapper.class.getName() +
                '{' + 
                namespacePostfix + 
                '}';
        final String typeVariableWrapperFactoryClassName = 
                TypeVariableWrapperFactory.class.getName() +
                '{' + 
                namespacePostfix + 
                '}';
        ASM.loadDynamicClass(
                TypeVariableWrapper.class.getClassLoader(), 
                typeVariableWrapperClassName, 
                TypeVariableWrapper.class.getProtectionDomain(), 
                (ClassVisitor cv) -> {
                    cv.visit(
                            Opcodes.V1_7, 
                            Opcodes.ACC_PUBLIC, 
                            typeVariableWrapperClassName.replace('.', '/'), 
                            null, 
                            ASM.getInternalName(TypeVariableWrapper.class), 
                            null
                    );
                    
                    MethodVisitor mv = cv.visitMethod(
                            Opcodes.ACC_PUBLIC, 
                            "<init>", 
                            '(' +
                            ASM.getDescriptor(TypeVariable.class) +
                            ")V", 
                            null, 
                            null
                    );
                    mv.visitCode();
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitMethodInsn(
                            Opcodes.INVOKESPECIAL, 
                            ASM.getInternalName(TypeVariableWrapper.class), 
                            "<init>", 
                            '(' + ASM.getDescriptor(TypeVariable.class) + ")V", 
                            false
                    );
                    mv.visitInsn(Opcodes.RETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                    
                    for (Method method : TypeVariable.class.getMethods()) {
                        mv = cv.visitMethod(
                                Opcodes.ALOAD, 
                                method.getName(), 
                                ASM.getDescriptor(method), 
                                null, 
                                ASM.getExceptionInternalNames(method)
                        );
                        mv.visitCode();
                        ASM.visitNewObjectWithConstant(
                                mv, 
                                ASM.getInternalName(UnsupportedOperationException.class), 
                                String.class, 
                                "Internal bug, TypeVariableWrapper supports nothing"
                        );
                        mv.visitInsn(Opcodes.ATHROW);
                        mv.visitMaxs(0, 0);
                        mv.visitEnd();
                    }
                    
                    cv.visitEnd();
                }
        );
        Class<TypeVariableWrapperFactory> clazz =
                (Class<TypeVariableWrapperFactory>)ASM.loadDynamicClass(
                        TypeVariableWrapperFactory.class.getClassLoader(), 
                        typeVariableWrapperFactoryClassName, 
                        TypeVariableWrapperFactory.class.getProtectionDomain(), 
                        (ClassVisitor cv) -> {
                            cv.visit(
                                    Opcodes.V1_7, 
                                    Opcodes.ACC_PUBLIC, 
                                    typeVariableWrapperFactoryClassName.replace('.', '/'), 
                                    null, 
                                    "java/lang/Object", 
                                    new String[] { ASM.getInternalName(TypeVariableWrapperFactory.class) }
                            );
                            
                            MethodVisitor mv = cv.visitMethod(
                                    Opcodes.ACC_PUBLIC, 
                                    "<init>", 
                                    "()V", 
                                    null, 
                                    null);
                            mv.visitCode();
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitMethodInsn(
                                    Opcodes.INVOKESPECIAL,
                                    "java/lang/Object",
                                    "<init>",
                                    "()V",
                                    false
                            );
                            mv.visitInsn(Opcodes.RETURN);
                            mv.visitMaxs(0, 0);
                            mv.visitEnd();
                            
                            mv = cv.visitMethod(
                                    Opcodes.ACC_PUBLIC, 
                                    "create", 
                                    '(' +
                                    ASM.getDescriptor(TypeVariable.class) +
                                    ')' +
                                    ASM.getDescriptor(TypeVariableWrapper.class), 
                                    null,
                                    null);
                            mv.visitCode();
                            mv.visitTypeInsn(Opcodes.NEW, typeVariableWrapperClassName.replace('.', '/'));
                            mv.visitInsn(Opcodes.DUP);
                            mv.visitVarInsn(Opcodes.ALOAD, 1);
                            mv.visitMethodInsn(
                                    Opcodes.INVOKESPECIAL, 
                                    typeVariableWrapperClassName.replace('.', '/'), 
                                    "<init>", 
                                    '(' + ASM.getDescriptor(TypeVariable.class) + ")V", 
                                    false
                            );
                            mv.visitInsn(Opcodes.ARETURN);
                            mv.visitMaxs(0, 0);
                            mv.visitEnd();
                            
                            cv.visitEnd();
                        }
                );
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw UncheckedException.rethrow(ex);
        }
    }
}
