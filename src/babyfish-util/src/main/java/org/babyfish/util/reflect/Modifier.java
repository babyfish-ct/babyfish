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

/**
 * @author Tao Chen
 */
public enum Modifier {
    
    PUBLIC(
            "public", 
            java.lang.reflect.Modifier.PUBLIC,
            true,
            true,
            true,
            true),
            
    PROTECTED(
            "protected", 
            java.lang.reflect.Modifier.PROTECTED,
            true,
            true,
            true,
            true),
            
    PRIVATE(
            "private", 
            java.lang.reflect.Modifier.PRIVATE,
            true,
            true,
            true,
            true),
            
    STATIC(
            "static", 
            java.lang.reflect.Modifier.STATIC,
            true,
            true,
            true,
            true),
            
    FINAL(
            "final", 
            java.lang.reflect.Modifier.FINAL,
            true,
            true,
            true,
            true),
            
    ABSTRACT(
            "abstract", 
            java.lang.reflect.Modifier.ABSTRACT,
            true,
            true,
            false,
            true),
            
    SYNCHRONIZED(
            "synchronized", 
            java.lang.reflect.Modifier.SYNCHRONIZED,
            true,
            false,
            false,
            true),
            
    NATIVE(
            "native", 
            java.lang.reflect.Modifier.NATIVE,
            true,
            false,
            false,
            true),
            
    TRANSIENT(
            "transient", 
            java.lang.reflect.Modifier.TRANSIENT,
            true,
            false,
            true,
            false),
            
    VOLATILE(
            "volatile", 
            java.lang.reflect.Modifier.VOLATILE,
            true,
            false,
            true,
            false),
            
    STRICT(
            "strictfp", 
            java.lang.reflect.Modifier.STRICT,
            true,
            false,
            false,
            true),
            
    VARARGS(
            "varargs", 
            0x00000080,
            false,
            false,
            false,
            true),
            
    BRIDGE(
            "bridge", 
            0x00000040,
            false,
            false,
            false,
            true),
            
    SYNTHETIC(
            "synthetic" , 
            0x00001000,
            false,
            false,
            false,
            true),
            
    INTERFACE(
            "interface",
            java.lang.reflect.Modifier.INTERFACE,
            false,
            true,
            false,
            false),
            
    ANNOTATION(
            "annotation",
            0x00002000,
            false,
            true,
            false,
            false),
            
    ENUM(
            "enum",
            0x00004000,
            false,
            true,
            false,
            false),
    ;
    
    //Modifier.values() will execute clone every time, cache it.
    final static Modifier[] ALL_MODIFIERS = Modifier.values();

    String modifierName;
    
    int jdkModifier;
    
    boolean keyword;
    
    boolean forTypes;
    
    boolean forFields;
    
    boolean forMethods;
    
    private Modifier(
            String modifierName, 
            int jdkModifier,
            boolean keyword,
            boolean forTypes,
            boolean forFields,
            boolean forMethods) {
        this.modifierName = modifierName;
        this.jdkModifier = jdkModifier;
        this.keyword = keyword;
        this.forTypes = forTypes;
        this.forFields = forFields;
        this.forMethods = forMethods;
    }

    public String getModifierName() {
        return this.modifierName;
    }

    public boolean isKeyword() {
        return this.keyword;
    }
    
    public boolean isForTypes() {
        return this.forTypes;
    }

    public boolean isForFields() {
        return this.forFields;
    }

    public boolean isForMethods() {
        return this.forMethods;
    }
    
    public int toJdkModifier() {
        return this.jdkModifier;
    }
    
    public static Modifier fromJdkTypeModifier(int jdkModifier) {
        for (Modifier modifier : ALL_MODIFIERS) {
            if (modifier.jdkModifier == jdkModifier && modifier.forTypes) {
                return modifier;
            }
        }
        return null;
    }
    
    public static Modifier fromJdkFieldModifier(int jdkModifier) {
        for (Modifier modifier : ALL_MODIFIERS) {
            if (modifier.jdkModifier == jdkModifier && modifier.forFields) {
                return modifier;
            }
        }
        return null;
    }
    
    public static Modifier fromJdkMethodModifier(int jdkModifier) {
        for (Modifier modifier : ALL_MODIFIERS) {
            if (modifier.jdkModifier == jdkModifier && modifier.forMethods) {
                return modifier;
            }
        }
        return null;
    }
    
}
