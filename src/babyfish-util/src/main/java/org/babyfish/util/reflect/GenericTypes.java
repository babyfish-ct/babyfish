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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
public class GenericTypes {
    
    private static final Type[] EMPTY_TYPES = new Type[0];

    private GenericTypes() {
        
    }
    
    static ParameterizedType makeParameterizedType(
            boolean validateTypeArguments,
            Type ownerType,
            Class<?> rawType, 
            Type ... actualTypeArguments) {
        return new ParameterizedTypeImpl(
                validateTypeArguments, 
                ownerType, 
                rawType, 
                actualTypeArguments);
    }
    
    static Type cascadeMakeTypeOrParameterizedType(
            boolean validateTypeArguments, 
            Class<?> rawType, 
            Type ... actualTypeArguments) {
        Arguments.mustNotBeNull("rawType", rawType);
        boolean hasActualTypeArguments = actualTypeArguments != null && actualTypeArguments.length != 0;
        if (!ClassInfo.isGenericClass(rawType)) {
            if (hasActualTypeArguments) {
                Arguments.mustBeEmptyWhen(
                        whenTheTypeIsNotGeneric("rawType"), 
                        "actualTypeArguments", 
                        actualTypeArguments);
            }
            return rawType;
        }
        if (ClassInfo.isNonStaticMemberClass(rawType)) {
            Type[] ownerTypeArguments;
            Type[] thisTypeArguments;
            if (hasActualTypeArguments) {
                int typeParameterCount = rawType.getTypeParameters().length;
                if (typeParameterCount > actualTypeArguments.length) {
                    throw new IllegalArgumentException(
                            invalidTypeArgumentCount(
                                    rawType, 
                                    typeParameterCount, 
                                    actualTypeArguments.length)
                    );
                }
                ownerTypeArguments = new Type[actualTypeArguments.length - typeParameterCount];
                thisTypeArguments = new Type[typeParameterCount];
                System.arraycopy(actualTypeArguments, 0, ownerTypeArguments, 0, ownerTypeArguments.length);
                System.arraycopy(actualTypeArguments, ownerTypeArguments.length, thisTypeArguments, 0, thisTypeArguments.length);
            } else {
                ownerTypeArguments = null;
                thisTypeArguments = rawType.getTypeParameters();
            }
            Type ownerType = cascadeMakeTypeOrParameterizedType(
                    validateTypeArguments, 
                    rawType.getDeclaringClass(), 
                    ownerTypeArguments);
            return GenericTypes.makeParameterizedType(
                    validateTypeArguments, 
                    ownerType, 
                    rawType, 
                    thisTypeArguments);
        }
        return GenericTypes.makeParameterizedType(
                validateTypeArguments, 
                rawType.getDeclaringClass(),
                rawType, 
                hasActualTypeArguments ? actualTypeArguments : rawType.getTypeParameters());
    }
    
    public static Type cascadeMakeTypeOrParameterizedType(
            Class<?> rawType, 
            Type ... actualTypeArguments) {
        return cascadeMakeTypeOrParameterizedType(true, rawType, actualTypeArguments);
    }
    
    public static Type makeParameterizedType(
            Type ownerType,
            Class<?> rawType, 
            Type ... actualTypeArguments) {
        if (GenericTypes.eraseGenericType(ownerType) != rawType.getDeclaringClass()) {
            throw new IllegalArgumentException(
                    erasedOwnerTypeMustBeDeclaringOfRawType(
                            "ownerType",
                            ownerType,
                            "rawType",
                            rawType)
            );
        }
        if (!(ownerType instanceof ParameterizedTypeImpl) && rawType.getTypeParameters().length == 0) {
            if (actualTypeArguments != null && actualTypeArguments.length != 0) {
                Arguments.mustBeEmptyWhen(
                        whenOwnerTypeIsClassWithoutTypeParameter("ownerType"), 
                        "actualTypeArguments", 
                        actualTypeArguments
                );
            }
            return rawType;
        }
        return new ParameterizedTypeImpl(
                true, 
                ownerType, 
                rawType, 
                actualTypeArguments);
    }
    
    public static GenericArrayType makeGenericArrayType(Type genericComponentType) {
        return new GenericArrayTypeImpl(genericComponentType);
    }
    
    public static WildcardType makeWildcardType(Type[] lowerBounds, Type[] upperBounds) {
        return new WildcardTypeImpl(lowerBounds, upperBounds);
    }
    
    public static Class<?> eraseGenericType(Type type) {
        return privateEraseGenericType(type, false);
    }
    
    public static boolean isTypeArgumentsDefault(ParameterizedType parameterizedType) {
        TypeVariable<?>[] typeVariables = ((Class<?>)parameterizedType.getRawType()).getTypeParameters();
        Type[] actualTypeArguments;
        if (parameterizedType instanceof ParameterizedTypeImpl) {
            actualTypeArguments = ((ParameterizedTypeImpl)parameterizedType).actualTypeArguments;
            if (actualTypeArguments == null) {
                actualTypeArguments = EMPTY_TYPES;
            }
        } else {
            actualTypeArguments = parameterizedType.getActualTypeArguments();
        }
        for (int i = typeVariables.length - 1; i >= 0; i--) {
            if (!typeVariables[i].equals(actualTypeArguments[i])) {
                return false;
            }
        }
        Type ownerType = parameterizedType.getOwnerType();
        if (ownerType instanceof ParameterizedType) {
            return isTypeArgumentsDefault((ParameterizedType)ownerType);
        }
        return true;
    }
    
    private static Class<?> privateEraseGenericType(Type type, boolean acceptReplacedTypeVariable) {
        if (type instanceof TypeVariable<?>) {
            if (acceptReplacedTypeVariable) {
                return privateEraseGenericType(
                        GenericResolver.normalTypeVariable(((TypeVariable<?>)type)).getBounds()[0],
                        acceptReplacedTypeVariable
                );
            }
            return privateEraseGenericType(
                    ((TypeVariable<?>)type).getBounds()[0], 
                    acceptReplacedTypeVariable
            );
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>)((ParameterizedType)type).getRawType();
        }
        if (type instanceof GenericArrayType) {
            Class<?> componentType = privateEraseGenericType(
                    ((GenericArrayType)type).getGenericComponentType(),
                    acceptReplacedTypeVariable
            );
            return Array.newInstance(componentType, 0).getClass();
        }
        if (type instanceof WildcardType) {
            Type[] upperBounds = 
                type instanceof WildcardTypeImpl ?
                        WildcardTypeImpl.fromObject(((WildcardTypeImpl)type).upperBounds, false) :
                        ((WildcardType)type).getLowerBounds();
            return upperBounds == null || upperBounds.length == 0 ? 
                    Object.class : 
                    privateEraseGenericType(upperBounds[0], acceptReplacedTypeVariable);
        }
        return (Class<?>)type;
    }
    
    private abstract static class TypeImpl implements Type {
        
        private transient Integer hashCode;
        
        private transient String toString;

        @Override
        public final int hashCode() {
            if (this.hashCode == null) {
                this.hashCode = this.hashCode0();
            }
            return this.hashCode;
        }

        @Override
        public final String toString() {
            if (this.toString == null) {
                this.toString = this.toString0();
            }
            return this.toString;
        }
        
        protected abstract int hashCode0();
        
        protected abstract String toString0();
    }
    
    private static class ParameterizedTypeImpl extends TypeImpl implements ParameterizedType {
        
        private final Type ownerType;
        
        private final Class<?> rawType;
        
        private final Type[] actualTypeArguments;
        
        public ParameterizedTypeImpl(
                boolean validateTypeArguments,
                Type ownerType,
                Class<?> rawType, 
                Type[] actualTypeArguments) {
            Arguments.mustNotBeNull("rawType", rawType);
            if (!(ownerType instanceof ParameterizedType)) {
                Arguments.mustNotBeEmptyWhen(
                        whenOwnerTypeIsNotParameterizedType("ownerType"),
                        "rawType.getTypeParameters()", 
                        rawType.getTypeParameters());
            }
            this.rawType = rawType;
            
            if (ownerType == null) {
                this.ownerType = rawType.getDeclaringClass();
            } else if (ownerType instanceof Class<?>) {
                this.ownerType = ownerType;
            } else if (ownerType instanceof ParameterizedType) {
                this.ownerType = ownerType;
            } else {
                Arguments.mustBeInstanceOfAnyOfValue("ownerType", ownerType, Class.class, ParameterizedType.class);
                throw new AssertionError("Internal bug: impossible");
            }
            
            if (actualTypeArguments == null || actualTypeArguments.length == 0) {
                TypeVariable<?>[] typeVariables = rawType.getTypeParameters();
                if (typeVariables.length != 0) {
                    this.actualTypeArguments = typeVariables;
                } else {
                    this.actualTypeArguments = null;
                }
            } else {
                this.actualTypeArguments = actualTypeArguments.clone();
            }
            
            if (validateTypeArguments) {
                this.validateTypeArguments();
            }
        }
        
        @Override
        public Type getOwnerType() {
            return this.ownerType;
        }

        @Override
        public Class<?> getRawType() {
            return this.rawType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return this.actualTypeArguments == null ? EMPTY_TYPES : this.actualTypeArguments.clone();
        }

        private void validateTypeArguments() {
            TypeVariable<?>[] typeVariables = this.rawType.getTypeParameters();
            if (typeVariables.length != (this.actualTypeArguments == null ? 0 : this.actualTypeArguments.length)) {
                throw new IllegalArgumentException(
                        invalidTypeArgumentCount(
                                this.rawType, 
                                typeVariables.length, 
                                this.actualTypeArguments.length
                        )
                );
            }
            for (int i = 0; i < typeVariables.length; i++) {
                Type actualTypeArgument = this.actualTypeArguments[i];
                if (actualTypeArgument instanceof WildcardType) {
                    continue;
                }
                Class<?> erasedActualTypeArgument = GenericTypes.privateEraseGenericType(actualTypeArgument, true);
                for (Type bound : typeVariables[i].getBounds()) {
                    if (!GenericTypes.privateEraseGenericType(bound, true).isAssignableFrom(erasedActualTypeArgument)) {
                        throw new IllegalArgumentException(
                                invalidTypeArgument(
                                        this.rawType,
                                        i,
                                        bound, 
                                        actualTypeArgument
                                )
                        );
                    }
                }
            }
        }

        @Override
        protected int hashCode0() {
            return 
                Arrays.hashCode(this.actualTypeArguments) ^ 
                (this.ownerType == null ? 0 : this.ownerType.hashCode()) ^
                this.rawType.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ParameterizedType)) {
                return false;
            }
            ParameterizedType other = (ParameterizedType)obj;
            Type[] otherTypeArguments = 
                obj instanceof ParameterizedTypeImpl ?
                        ((ParameterizedTypeImpl)other).actualTypeArguments :
                        other.getActualTypeArguments();
            return 
                (this.ownerType == null ? 
                        other.getOwnerType() == null : 
                        this.ownerType.equals(other.getOwnerType())) &&
                this.rawType.equals(other.getRawType()) &&
                Arrays.equals(this.actualTypeArguments, otherTypeArguments);
        }

        @Override
        protected String toString0() {
            StringBuilder sb = new StringBuilder();
            if (this.ownerType != null) {
                sb.append(
                        this.ownerType instanceof Class<?> ? 
                                ((Class<?>)this.ownerType).getName() : 
                                    this.ownerType.toString());
                sb.append('.');
                if (this.ownerType instanceof ParameterizedType) {
                    sb.append(
                            this.rawType.getName().replace(
                                    ((Class<?>)((ParameterizedType)this.ownerType).getRawType()).getName() + "$", 
                                    ""));
                } else {
                    sb.append(this.rawType.getName());
                }
            } else {
                sb.append(this.rawType.getName());
            }
            if (this.actualTypeArguments != null) {
                sb.append('<');
                boolean notFirst = false;
                for (Type actualTypeArgument : this.actualTypeArguments) {
                    if (notFirst) {
                        sb.append(", ");
                    }
                    sb.append(
                            actualTypeArgument instanceof Class<?> ? 
                                    ((Class<?>)actualTypeArgument).getName() : 
                                    actualTypeArgument.toString());
                    notFirst = true;
                }
                sb.append('>');
            }
            return sb.toString();
        }
        
    }
    
    private static class GenericArrayTypeImpl extends TypeImpl implements GenericArrayType {
        
        private Type genericComponentType;
        
        public GenericArrayTypeImpl(Type genericComponentType) {
            this.genericComponentType = Arguments.mustNotBeNull("genericComponentType", genericComponentType);
        }

        @Override
        public Type getGenericComponentType() {
            return this.genericComponentType;
        }

        @Override
        protected int hashCode0() {
            return this.genericComponentType.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof GenericArrayType)) {
                return false;
            }
            GenericArrayType other = (GenericArrayType)obj;
            return this.genericComponentType.equals(other.getGenericComponentType());
        }

        @Override
        protected String toString0() {
            return 
                (this.genericComponentType instanceof Class<?> ? 
                        ((Class<?>)this.genericComponentType).getName() : 
                            this.genericComponentType.toString()) +
                "[]";
        }
        
    }
    
    private static class WildcardTypeImpl extends TypeImpl implements WildcardType {
        
        private final Object lowerBounds;
        
        private final Object upperBounds;
        
        WildcardTypeImpl(Type[] lowerBounds, Type[] upperBounds) {
            this.lowerBounds = toObject(lowerBounds);
            this.upperBounds = toObject(upperBounds);
        }

        @Override
        public Type[] getLowerBounds() {
            return fromObject(this.lowerBounds, true);
        }

        @Override
        public Type[] getUpperBounds() {
            return fromObject(this.upperBounds, true);
        }
        
        @Override
        protected int hashCode0() {
            return 
                Arrays.hashCode(fromObject(this.lowerBounds, false)) ^ 
                Arrays.hashCode(fromObject(this.upperBounds, false));
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof WildcardType)) {
                return false;
            }
            if (obj instanceof WildcardTypeImpl) {
                WildcardTypeImpl other = (WildcardTypeImpl)obj;
                return 
                    Arrays.equals(fromObject(this.lowerBounds, false), fromObject(other.lowerBounds, false)) &&
                    Arrays.equals(fromObject(this.upperBounds, false), fromObject(other.upperBounds, false));
            }
            WildcardType other = (WildcardType)obj;
            return 
                Arrays.equals(fromObject(this.lowerBounds, false), other.getLowerBounds()) &&
                Arrays.equals(fromObject(this.upperBounds, false), other.getUpperBounds());
        }

        @Override
        protected String toString0() {
            Type[] lowerBounds = fromObject(this.lowerBounds, false);
            Type[] upperBounds = fromObject(this.upperBounds, false);
            Type[] bounds = lowerBounds;
            StringBuilder sb = new StringBuilder();

            if (lowerBounds.length != 0) {
                sb.append("? super ");
            } else {
                if ((upperBounds.length > 0) && (!(upperBounds[0].equals(Object.class)))) {
                    bounds = upperBounds;
                    sb.append("? extends ");
                } else {
                    return "?";
                }
            }
            assert bounds.length > 0;
            int len = bounds.length; 
            for (int i = 0; i < len; i++) { 
                Type bound = bounds[i];
                if (i != 0) {
                    sb.append(" & ");
                }
                if (bound instanceof Class<?>) {
                    sb.append(((Class<?>)bound).getName());
                }
                else {
                    sb.append(bound.toString());
                }
            }
            return ((String)sb.toString());
        }
        
        private static Object toObject(Type[] bounds) {
            if (bounds == null || bounds.length == 0) {
                return null;
            }
            if (bounds.length == 1) {
                return bounds[0];
            }
            return bounds;
        }
        
        private static Type[] fromObject(Object obj, boolean clone) {
            if (obj == null) {
                return EMPTY_TYPES;
            }
            if (obj instanceof Type) {
                return new Type[] { (Type)obj };
            }
            return clone ? ((Type[])obj).clone() : (Type[])obj;
        }
    }
    
    @I18N
    private static native String invalidTypeArgumentCount(Class<?> rawType, int expectCount, int actualActualType);
    
    @I18N
    private static native String invalidTypeArgument(Class<?> rawType, int typeParameterIndex, Type bound, Type actualTypeArgument);

    @I18N
    private static native String erasedOwnerTypeMustBeDeclaringOfRawType(
            String ownerTypeParameterName,
            Type ownerType, 
            String rawTypeParameterName, 
            Class<?> rawType);
    
    @I18N
    private static native String whenTheTypeIsNotGeneric(String parameterName);
    
    @I18N
    private static native String whenOwnerTypeIsClassWithoutTypeParameter(String parameterName);
    
    @I18N
    private static native String whenOwnerTypeIsNotParameterizedType(String ownerTypeParameterName);
}
