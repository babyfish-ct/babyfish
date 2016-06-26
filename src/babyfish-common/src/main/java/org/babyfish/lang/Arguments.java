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

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Tao Chen
 */
public class Arguments {
    
    public static <T> T mustBeNull(
            String parameterName, 
            T argument) {
        if (argument != null) {
            throw new IllegalArgumentException(
                    mustBeNullMessage(parameterName));
        }
        return argument;
    }
    
    public static <T> T mustNotBeNull(
            String parameterName, 
            T argument) {
        if (argument == null) {
            throw new NullArgumentException(
                    mustNotBeNullMessage(parameterName));
        }
        return argument;
    }
    
    public static boolean[] mustNotBeEmpty(
            String parameterName, 
            boolean[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyMessage(parameterName));
        }
        return argument;
    }
    
    public static char[] mustNotBeEmpty(
            String parameterName, 
            char[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyMessage(parameterName));
        }
        return argument;
    }
    
    public static byte[] mustNotBeEmpty(
            String parameterName, 
            byte[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyMessage(parameterName));
        }
        return argument;
    }
    
    public static short[] mustNotBeEmpty(
            String parameterName, 
            short[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyMessage(parameterName));
        }
        return argument;
    }
    
    public static int[] mustNotBeEmpty(
            String parameterName, 
            int[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyMessage(parameterName));
        }
        return argument;
    }
    
    public static long[] mustNotBeEmpty(
            String parameterName, 
            long[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyMessage(parameterName));
        }
        return argument;
    }
    
    public static float[] mustNotBeEmpty(
            String parameterName, 
            float[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyMessage(parameterName));
        }
        return argument;
    }
    
    public static double[] mustNotBeEmpty(
            String parameterName, 
            double[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyMessage(parameterName));
        }
        return argument;
    }
    
    public static <T> T[] mustNotBeEmpty(
            String parameterName, 
            T[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyMessage(parameterName));
        }
        return argument;
    }
    
    public static String mustNotBeEmpty(
            String parameterName, 
            String argument) {
        if (argument != null && argument.isEmpty()) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyMessage(parameterName));
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustNotBeEmpty(
            String parameterName, 
            I argument) {
        if (argument != null && isEmpty(argument)) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyMessage(parameterName));
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotBeEmpty(
            String parameterName, 
            M argument) {
        if (argument != null && argument.isEmpty()) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyMessage(parameterName));
        }
        return argument;
    }
    
    public static <E> E[] mustNotContainNullElements(
            String parameterName,
            E[] argument) {
        if (argument != null) {
            for (Object o : argument) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            mustNotContainNullElementsMessage(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static String[] mustNotContainEmptyElements(
            String parameterName,
            String[] argument) {
        if (argument != null) {
            for (String s : argument) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            mustNotContainEmptyElementsMessage(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <E> E[] mustNotContainSpecialElements(
            String parameterName,
            E[] argument,
            Class<?> classValue) {
        if (argument != null) {
            for (Object o : argument) {
                if (o != null && classValue.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            mustNotContainSpecialElementsMessage(parameterName, classValue));
                }
            }
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustNotContainNullElements(
            String parameterName,
            I argument) {
        if (argument != null) {
            for (Object o : argument) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            mustNotContainNullElementsMessage(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <I extends Iterable<String>> I mustNotContainEmptyElements(
            String parameterName,
            I argument) {
        if (argument != null) {
            for (String s : argument) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            mustNotContainEmptyElementsMessage(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustNotContainSpecialElements(
            String parameterName,
            I argument,
            Class<?> classValue) {
        if (argument != null) {
            for (Object o : argument) {
                if (o != null && classValue.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            mustNotContainSpecialElementsMessage(parameterName, classValue));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainNullKeys(
            String parameterName,
            M argument) {
        if (argument != null) {
            for (Object o : argument.keySet()) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            mustNotContainNullKeysMessage(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <V> Map<String, V> mustNotContainEmptyElements(
            String parameterName,
            Map<String, V> argument) {
        if (argument != null) {
            for (String s : argument.keySet()) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            mustNotContainEmptyElementsMessage(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainSpecialKeys(
            String parameterName,
            M argument,
            Class<?> classValue) {
        if (argument != null) {
            for (Object o : argument.keySet()) {
                if (o != null && classValue.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            mustNotContainSpecialKeysMessage(parameterName, classValue));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainNullValues(
            String parameterName,
            M argument) {
        if (argument != null) {
            for (Object o : argument.values()) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            mustNotContainNullValuesMessage(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <K> Map<K, String> mustNotContainEmptyValues(
            String parameterName,
            Map<K, String> argument) {
        if (argument != null) {
            for (String s : argument.values()) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            mustNotContainEmptyElementsMessage(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainSpecialValues(
            String parameterName,
            M argument,
            Class<?> classValue) {
        if (argument != null) {
            for (Object o : argument.values()) {
                if (o != null && classValue.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            mustNotContainSpecialValuesMessage(parameterName, classValue));
                }
            }
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeClass(String parameterName, Class<T> argument) {
        if (argument != null) {
            if (argument.isInterface() ||
                    argument.isEnum() ||
                    argument.isAnnotation() ||
                    argument.isArray() ||
                    argument.isPrimitive()) {
                throw new IllegalArgumentException(mustBeClassMessage(parameterName));
            }
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeClass(String parameterName, Class<T> argument) {
        if (argument != null) {
            if (!argument.isInterface() &&
                    !argument.isEnum() &&
                    !argument.isAnnotation() &&
                    !argument.isArray() &&
                    !argument.isPrimitive()) {
                throw new IllegalArgumentException(mustNotBeClassMessage(parameterName));
            }
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeInterface(String parameterName, Class<T> argument) {
        if (argument != null && !argument.isInterface()) {
            throw new IllegalArgumentException(mustBeInterfaceMessage(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeInterface(String parameterName, Class<T> argument) {
        if (argument != null && argument.isInterface()) {
            throw new IllegalArgumentException(mustNotBeInterfaceMessage(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeEnum(String parameterName, Class<T> argument) {
        if (argument != null && !argument.isEnum()) {
            throw new IllegalArgumentException(mustBeEnumMessage(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeEnum(String parameterName, Class<T> argument) {
        if (argument != null && argument.isEnum()) {
            throw new IllegalArgumentException(mustNotBeEnumMessage(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeAnnotation(String parameterName, Class<T> argument) {
        if (argument != null && !argument.isAnnotation()) {
            throw new IllegalArgumentException(mustBeAnnotationMessage(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeAnnotation(String parameterName, Class<T> argument) {
        if (argument != null && argument.isAnnotation()) {
            throw new IllegalArgumentException(mustNotBeAnnotationMessage(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeArray(String parameterName, Class<T> argument) {
        if (argument != null && !argument.isArray()) {
            throw new IllegalArgumentException(mustBeArrayMessage(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeArray(String parameterName, Class<T> argument) {
        if (argument != null && argument.isArray()) {
            throw new IllegalArgumentException(mustNotBeArrayMessage(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBePrimitive(String parameterName, Class<T> argument) {
        if (argument != null && !argument.isPrimitive()) {
            throw new IllegalArgumentException(mustBePrimitiveMessage(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBePrimitive(String parameterName, Class<T> argument) {
        if (argument != null && argument.isPrimitive()) {
            throw new IllegalArgumentException(mustNotBePrimitiveMessage(parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeAbstract(String parameterName, Class<T> argument) {
        if (argument != null && !Modifier.isAbstract(argument.getModifiers())) {
            throw new IllegalArgumentException(mustBeAbstractMessage(parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustNotBeAbstract(String parameterName, Class<T> argument) {
        if (argument != null && Modifier.isAbstract(argument.getModifiers())) {
            throw new IllegalArgumentException(mustNotBeAbstractMessage(parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeFinal(String parameterName, Class<T> argument) {
        if (argument != null && !Modifier.isFinal(argument.getModifiers())) {
            throw new IllegalArgumentException(mustBeFinalMessage(parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustNotBeFinal(String parameterName, Class<T> argument) {
        if (argument != null && Modifier.isFinal(argument.getModifiers())) {
            throw new IllegalArgumentException(mustNotBeFinalMessage(parameterName));
        }
        return argument;
    }
    
    public static char mustBeEqualToValue(
            String parameterName,
            char argument,
            char value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    mustBeEqualToValueMessage(parameterName, Character.toString(value)));
        }
        return argument;
    }
    
    public static char mustNotBeEqualToValue(
            String parameterName,
            char argument,
            char value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueMessage(parameterName, Character.toString(value)));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanValue(
            String parameterName,
            char argument,
            char minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueMessage(parameterName, Character.toString(minimumValue)));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanOrEqualToValue(
            String parameterName,
            char argument,
            char minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueMessage(parameterName, Character.toString(minimumValue)));
        }
        return argument;
    }
    
    public static char mustBeLessThanValue(
            String parameterName,
            char argument,
            char maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueMessage(parameterName, Character.toString(maximumValue)));
        }
        return argument;
    }
    
    public static char mustBeLessThanOrEqualToValue(
            String parameterName,
            char argument,
            char maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueMessage(parameterName, Character.toString(maximumValue)));
        }
        return argument;
    }
    
    public static char mustBetweenValue(
            String parameterName,
            char argument,
            char minimumValue,
            boolean minimumInclusive,
            char maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueMessage(
                            parameterName, 
                            minimumOp,
                            Character.toString(minimumValue),
                            maximumOp,
                            Character.toString(maximumValue)));
        }
        return argument;
    }
    
    public static byte mustBeEqualToValue(
            String parameterName,
            byte argument,
            byte value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    mustBeEqualToValueMessage(parameterName, Byte.toString(value)));
        }
        return argument;
    }

    public static byte mustNotBeEqualToValue(
            String parameterName,
            byte argument,
            byte value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueMessage(parameterName, Byte.toString(value)));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanValue(
            String parameterName,
            byte argument,
            byte minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueMessage(parameterName, Byte.toString(minimumValue)));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanOrEqualToValue(
            String parameterName,
            byte argument,
            byte minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueMessage(parameterName, Byte.toString(minimumValue)));
        }
        return argument;
    }
    
    public static byte mustBeLessThanValue(
            String parameterName,
            byte argument,
            byte maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueMessage(parameterName, Byte.toString(maximumValue)));
        }
        return argument;
    }
    
    public static byte mustBeLessThanOrEqualToValue(
            String parameterName,
            byte argument,
            byte maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueMessage(parameterName, Byte.toString(maximumValue)));
        }
        return argument;
    }
    
    public static byte mustBetweenValue(
            String parameterName,
            byte argument,
            byte minimumValue,
            boolean minimumInclusive,
            byte maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueMessage(
                            parameterName, 
                            minimumOp,
                            Byte.toString(minimumValue),
                            maximumOp,
                            Byte.toString(maximumValue)));
        }
        return argument;
    }
    
    public static short mustBeEqualToValue(
            String parameterName,
            short argument,
            short value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    mustBeEqualToValueMessage(parameterName, Short.toString(value)));
        }
        return argument;
    }
    
    public static short mustNotBeEqualToValue(
            String parameterName,
            short argument,
            short value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueMessage(parameterName, Short.toString(value)));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanValue(
            String parameterName,
            short argument,
            short minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueMessage(parameterName, Short.toString(minimumValue)));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanOrEqualToValue(
            String parameterName,
            short argument,
            short minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueMessage(parameterName, Short.toString(minimumValue)));
        }
        return argument;
    }
    
    public static short mustBeLessThanValue(
            String parameterName,
            short argument,
            short maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueMessage(parameterName, Short.toString(maximumValue)));
        }
        return argument;
    }
    
    public static short mustBeLessThanOrEqualToValue(
            String parameterName,
            short argument,
            short maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueMessage(parameterName, Short.toString(maximumValue)));
        }
        return argument;
    }
    
    public static short mustBetweenValue(
            String parameterName,
            short argument,
            short minimumValue,
            boolean minimumInclusive,
            short maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueMessage(
                            parameterName, 
                            minimumOp,
                            Short.toString(minimumValue),
                            maximumOp,
                            Short.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int mustBeEqualToValue(
            String parameterName,
            int argument,
            int value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    mustBeEqualToValueMessage(parameterName, Integer.toString(value)));
        }
        return argument;
    }
    
    public static int mustNotBeEqualToValue(
            String parameterName,
            int argument,
            int value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueMessage(parameterName, Integer.toString(value)));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanValue(
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueMessage(parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanOrEqualToValue(
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueMessage(parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int mustBeLessThanValue(
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueMessage(parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int mustBeLessThanOrEqualToValue(
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueMessage(parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int mustBetweenValue(
            String parameterName,
            int argument,
            int minimumValue,
            boolean minimumInclusive,
            int maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueMessage(
                            parameterName, 
                            minimumOp,
                            Integer.toString(minimumValue),
                            maximumOp,
                            Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanValue(
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument <= minimumValue) {
            throw new IndexOutOfBoundsException(
                    mustBeGreaterThanValueMessage(parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanOrEqualToValue(
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument < minimumValue) {
            throw new IndexOutOfBoundsException(
                    mustBeGreaterThanOrEqualToValueMessage(parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanValue(
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument >= maximumValue) {
            throw new IndexOutOfBoundsException(
                    mustBeLessThanValueMessage(parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanOrEqualToValue(
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument > maximumValue) {
            throw new IndexOutOfBoundsException(
                    mustBeLessThanOrEqualToValueMessage(parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int indexMustBetweenValue(
            String parameterName,
            int argument,
            int minimumValue,
            boolean minimumInclusive,
            int maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IndexOutOfBoundsException(
                    mustBetweenValueMessage(
                            parameterName, 
                            minimumOp,
                            Integer.toString(minimumValue),
                            maximumOp,
                            Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static long mustBeEqualToValue(
            String parameterName,
            long argument,
            long value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    mustBeEqualToValueMessage(parameterName, Long.toString(value)));
        }
        return argument;
    }
    
    public static long mustNotBeEqualToValue(
            String parameterName,
            long argument,
            long value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueMessage(parameterName, Long.toString(value)));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanValue(
            String parameterName,
            long argument,
            long minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueMessage(parameterName, Long.toString(minimumValue)));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanOrEqualToValue(
            String parameterName,
            long argument,
            long minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueMessage(parameterName, Long.toString(minimumValue)));
        }
        return argument;
    }
    
    public static long mustBeLessThanValue(
            String parameterName,
            long argument,
            long maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueMessage(parameterName, Long.toString(maximumValue)));
        }
        return argument;
    }
    
    public static long mustBeLessThanOrEqualToValue(
            String parameterName,
            long argument,
            long maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueMessage(parameterName, Long.toString(maximumValue)));
        }
        return argument;
    }
    
    public static long mustBetweenValue(
            String parameterName,
            long argument,
            long minimumValue,
            boolean minimumInclusive,
            long maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueMessage(
                            parameterName, 
                            minimumOp,
                            Long.toString(minimumValue),
                            maximumOp,
                            Long.toString(maximumValue)));
        }
        return argument;
    }
    
    public static float mustBeEqualToValue(
            String parameterName,
            float argument,
            float value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    mustBeEqualToValueMessage(parameterName, Float.toString(value)));
        }
        return argument;
    }
    
    public static float mustNotBeEqualToValue(
            String parameterName,
            float argument,
            float value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueMessage(parameterName, Float.toString(value)));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanValue(
            String parameterName,
            float argument,
            float minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueMessage(parameterName, Float.toString(minimumValue)));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanOrEqualToValue(
            String parameterName,
            float argument,
            float minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueMessage(parameterName, Float.toString(minimumValue)));
        }
        return argument;
    }
    
    public static float mustBeLessThanValue(
            String parameterName,
            float argument,
            float maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueMessage(parameterName, Float.toString(maximumValue)));
        }
        return argument;
    }
    
    public static float mustBeLessThanOrEqualToValue(
            String parameterName,
            float argument,
            float maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueMessage(parameterName, Float.toString(maximumValue)));
        }
        return argument;
    }
    
    public static float mustBetweenValue(
            String parameterName,
            float argument,
            float minimumValue,
            boolean minimumInclusive,
            float maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueMessage(
                            parameterName, 
                            minimumOp,
                            Float.toString(minimumValue),
                            maximumOp,
                            Float.toString(maximumValue)));
        }
        return argument;
    }
    
    public static double mustBeEqualToValue(
            String parameterName,
            double argument,
            double value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    mustBeEqualToValueMessage(parameterName, Double.toString(value)));
        }
        return argument;
    }
    
    public static double mustNotBeEqualToValue(
            String parameterName,
            double argument,
            double value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueMessage(parameterName, Double.toString(value)));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanValue(
            String parameterName,
            double argument,
            double minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueMessage(parameterName, Double.toString(minimumValue)));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanOrEqualToValue(
            String parameterName,
            double argument,
            double minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueMessage(parameterName, Double.toString(minimumValue)));
        }
        return argument;
    }
    
    public static double mustBeLessThanValue(
            String parameterName,
            double argument,
            double maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueMessage(parameterName, Double.toString(maximumValue)));
        }
        return argument;
    }
    
    public static double mustBeLessThanOrEqualToValue(
            String parameterName,
            double argument,
            double maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueMessage(parameterName, Double.toString(maximumValue)));
        }
        return argument;
    }
    
    public static double mustBetweenValue(
            String parameterName,
            double argument,
            double minimumValue,
            boolean minimumInclusive,
            double maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueMessage(
                            parameterName, 
                            minimumOp,
                            Double.toString(minimumValue),
                            maximumOp,
                            Double.toString(maximumValue)));
        }
        return argument;
    }
    
    public static <T> T mustBeEqualToValue(
            String parameterName,
            T argument,
            T value) {
        if (argument != null && value != null && !argument.equals(value)) {
            throw new IllegalArgumentException(
                    mustBeEqualToValueMessage(parameterName, value.toString()));
        }
        return argument;
    }
    
    public static <T> T mustNotBeEqualToValue(
            String parameterName,
            T argument,
            T value) {
        if (argument != null && value != null && argument.equals(value)) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueMessage(parameterName, value.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanValue(
            String parameterName,
            T argument,
            T minimumValue) {
        if (argument != null && minimumValue != null && argument.compareTo(minimumValue) <= 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueMessage(parameterName, minimumValue.toString()));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeGreaterThanValue(
            String parameterName,
            T argument,
            T minimumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeGreaterThanValue(parameterName, (Comparable)argument, (Comparable)minimumValue);
        }
        if (argument != null && minimumValue != null && comparator.compare(argument, minimumValue) <= 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueMessage(parameterName, minimumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanOrEqualToValue(
            String parameterName,
            T argument,
            T minimumValue) {
        if (argument != null && minimumValue != null && argument.compareTo(minimumValue) < 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueMessage(parameterName, minimumValue.toString()));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeGreaterThanOrEqualToValue(
            String parameterName,
            T argument,
            T minimumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeGreaterThanOrEqualToValue(parameterName, (Comparable)argument, (Comparable)minimumValue);
        }
        if (argument != null && minimumValue != null && comparator.compare(argument, minimumValue) < 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueMessage(parameterName, minimumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanValue(
            String parameterName,
            T argument,
            T maximumValue) {
        if (argument != null && maximumValue != null && argument.compareTo(maximumValue) >= 0) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueMessage(parameterName, maximumValue.toString()));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeLessThanValue(
            String parameterName,
            T argument,
            T maximumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeLessThanValue(parameterName, (Comparable)argument, (Comparable)maximumValue);
        }
        if (argument != null && maximumValue != null && comparator.compare(argument, maximumValue) >= 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueMessage(parameterName, maximumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanOrEqualToValue(
            String parameterName,
            T argument,
            T maximumValue) {
        if (argument != null && maximumValue != null && argument.compareTo(maximumValue) > 0) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueMessage(parameterName, maximumValue.toString()));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeLessThanOrEqualToValue(
            String parameterName,
            T argument,
            T maximumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeLessThanOrEqualToValue(parameterName, (Comparable)argument, (Comparable)maximumValue);
        }
        if (argument != null && maximumValue != null && comparator.compare(argument, maximumValue) > 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueMessage(parameterName, maximumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBetweenValue(
            String parameterName,
            T argument,
            T minimumValue,
            boolean minimumInclusive,
            T maximumValue,
            boolean maximumInclusive) {
        if (argument == null) {
            return null;
        }
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumValue != null) {
            if (minimumInclusive) {
                if (argument.compareTo(minimumValue) < 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(minimumValue) <= 0) {
                    throwable = true;
                }
            }
        }
        if (!throwable && maximumValue != null) {
            if (maximumInclusive) {
                if (argument.compareTo(maximumValue) > 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(maximumValue) >= 0) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueMessage(
                            parameterName, 
                            minimumOp,
                            minimumValue == null ? null : maximumValue.toString(),
                            maximumOp,
                            maximumValue == null ? null : maximumValue.toString()));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBetweenValue(
            String parameterName,
            T argument,
            T minimumValue,
            boolean minimumInclusive,
            T maximumValue,
            boolean maximumInclusive,
            Comparator<? super T> comparator) {
        if (argument == null) {
            return null;
        }
        if (comparator == null) {
            return (T)mustBetweenValue(
                    parameterName,
                    (Comparable)argument,
                    (Comparable)minimumValue,
                    minimumInclusive,
                    (Comparable)maximumValue,
                    maximumInclusive);
        }
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumValue != null) {
            if (minimumInclusive) {
                if (comparator.compare(argument, minimumValue) < 0) {
                    throwable = true;
                }
            } else {
                if (comparator.compare(argument, minimumValue) <= 0) {
                    throwable = true;
                }
            }
        }
        if (!throwable && maximumValue != null) {
            if (maximumInclusive) {
                if (comparator.compare(argument, maximumValue) > 0) {
                    throwable = true;
                }
            } else {
                if (comparator.compare(argument, maximumValue) >= 0) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueMessage(
                            parameterName, 
                            minimumOp,
                            minimumValue == null ? null : maximumValue.toString(),
                            maximumOp,
                            maximumValue == null ? null : maximumValue.toString()));
        }
        return argument;
    }
    
    public static <T> T mustBeInstanceOfValue(
            String parameterName,
            T argument,
            Class<?> classValue) {
        if (argument != null && !classValue.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    mustBeInstanceOfValueMessage(parameterName, classValue));
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAllOfValue(
            String parameterName,
            T argument,
            Class<?> ... classesValue) {
        if  (argument != null) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                mustBeInstanceOfAllOfValueMessage(parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAnyOfValue(
            String parameterName,
            T argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            boolean throwable = false;
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        mustBeInstanceOfAnyOfValueMessage(parameterName, classesValue)
                );
            }
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfValue(
            String parameterName,
            T argument,
            Class<?> classValue) {
        if (argument != null && classValue.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    mustNotBeInstanceOfValueMessage(parameterName, classValue));
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfAnyOfValue(
            String parameterName,
            T argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                mustNotBeInstanceOfAnyOfValueMessage(parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithValue(
            String parameterName,
            Class<T> argument,
            Class<?> classValue) {
        if (argument != null && !classValue.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    mustBeCompatibleWithValueMessage(parameterName, classValue));
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAllOfValue(
            String parameterName,
            Class<T> argument,
            Class<?> ... classesValue) {
        if  (argument != null) {
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                mustBeCompatibleWithAllOfValueMessage(parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAnyOfValue(
            String parameterName,
            Class<T> argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            boolean throwable = false;
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        mustBeCompatibleWithAnyOfValueMessage(parameterName, classesValue)
                );
            }
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithValue(
            String parameterName,
            Class<T> argument,
            Class<?> classValue) {
        if (argument != null && classValue.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    mustNotBeCompatibleWithValueMessage(parameterName, classValue));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithAnyOfValue(
            String parameterName,
            Class<T> argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                mustNotBeCompatibleWithAnyOfValueMessage(parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }
    
    public static char mustBeAnyOfValue(
            String parameterName, 
            char argument, 
            char ... charactersValue) {
        for (char c : charactersValue) {
            if (c == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueMessage(parameterName, Arrays.asList(charactersValue))
        );
    }
    
    public static char mustNotBeAnyOfValue(
            String parameterName, 
            char argument, 
            char ... charactersValue) {
        for (char c : charactersValue) {
            if (c == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueMessage(parameterName, Arrays.asList(charactersValue))
                );
            }
        }
        return argument;
    }
    
    public static byte mustBeAnyOfValue(
            String parameterName, 
            byte argument, 
            byte ... bytesValue) {
        for (byte b : bytesValue) {
            if (b == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueMessage(parameterName, Arrays.asList(bytesValue))
        );
    }
    
    public static byte mustNotBeAnyOfValue(
            String parameterName, 
            byte argument, 
            byte ... bytesValue) {
        for (byte b : bytesValue) {
            if (b == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueMessage(parameterName, Arrays.asList(bytesValue))
                );
            }
        }
        return argument;
    }
    
    public static short mustBeAnyOfValue(
            String parameterName, 
            short argument, 
            short ... shortsValue) {
        for (short s : shortsValue) {
            if (s == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueMessage(parameterName, Arrays.asList(shortsValue))
        );
    }
    
    public static short mustNotBeAnyOfValue(
            String parameterName, 
            short argument, 
            short ... shortsValue) {
        for (short s : shortsValue) {
            if (s == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueMessage(parameterName, Arrays.asList(shortsValue))
                );
            }
        }
        return argument;
    }
    
    public static int mustBeAnyOfValue(
            String parameterName, 
            int argument, 
            int ... intsValue) {
        for (int i : intsValue) {
            if (i == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueMessage(parameterName, Arrays.asList(intsValue))
        );
    }
    
    public static int mustNotBeAnyOfValue(
            String parameterName, 
            int argument, 
            int ... intsValue) {
        for (int i : intsValue) {
            if (i == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueMessage(parameterName, Arrays.asList(intsValue))
                );
            }
        }
        return argument;
    }
    
    public static long mustBeAnyOfValue(
            String parameterName, 
            long argument, 
            long ... longsValue) {
        for (long l : longsValue) {
            if (l == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueMessage(parameterName, Arrays.asList(longsValue))
        );
    }
    
    public static long mustNotBeAnyOfValue(
            String parameterName, 
            long argument, 
            long ... longsValue) {
        for (long l : longsValue) {
            if (l == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueMessage(parameterName, Arrays.asList(longsValue))
                );
            }
        }
        return argument;
    }
    
    public static float mustBeAnyOfValue(
            String parameterName, 
            float argument, 
            float ... floatsValue) {
        for (float f : floatsValue) {
            if (f == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueMessage(parameterName, Arrays.asList(floatsValue))
        );
    }
    
    public static float mustNotBeAnyOfValue(
            String parameterName, 
            float argument, 
            float ... floatsValue) {
        for (float f : floatsValue) {
            if (f == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueMessage(parameterName, Arrays.asList(floatsValue))
                );
            }
        }
        return argument;
    }
    
    public static double mustBeAnyOfValue(
            String parameterName, 
            double argument, 
            double ... doublesValue) {
        for (double d : doublesValue) {
            if (d == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueMessage(parameterName, Arrays.asList(doublesValue))
        );
    }

    public static double mustNotBeAnyOfValue(
            String parameterName, 
            double argument, 
            double ... doublesValue) {
        for (double d : doublesValue) {
            if (d == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueMessage(parameterName, Arrays.asList(doublesValue))
                );
            }
        }
        return argument;
    }

    @SafeVarargs
    public static <T> T mustBeAnyOfValue(
            String parameterName, 
            T argument, 
            T ... objectsValue) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValue) {
            if (argument.equals(o)) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueMessage(parameterName, Arrays.asList(objectsValue))
        );
    }
    
    @SafeVarargs
    public static <T> T mustNotBeAnyOfValue(
            String parameterName, 
            T argument, 
            T ... objectsValue) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValue) {
            if (argument.equals(o)) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueMessage(parameterName, Arrays.asList(objectsValue))
                );
            }
        }
        return argument;
    }
    
    public static char mustBeEqualToOther(
            String parameterName,
            char argument,
            String valueParameterName,
            char valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static char mustNotBeEqualToOther(
            String parameterName,
            char argument,
            String valueParameterName,
            char valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanOther(
            String parameterName,
            char argument,
            String minimumParameterName,
            char minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanOrEqualToOther(
            String parameterName,
            char argument,
            String minimumParameterName, 
            char minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static char mustBeLessThanOther(
            String parameterName,
            char argument,
            String maximumParameterName,
            char maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static char mustBeLessThanOrEqualToOther(
            String parameterName,
            char argument,
            String maximumParameterName,
            char maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static char mustBetweenOther(
            String parameterName,
            char argument,
            String minimumParameterName,
            char minimumArgument,
            String maximumParameterName,
            boolean minimumInclusive,
            char maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherMessage(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static byte mustBeEqualToOther(
            String parameterName,
            byte argument,
            String valueParameterName,
            byte valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }

    public static byte mustNotBeEqualToOther(
            String parameterName,
            byte argument,
            String valueParamterName,
            byte valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherMessage(parameterName, valueParamterName));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanOther(
            String parameterName,
            byte argument,
            String minimumArgumentName,
            byte minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherMessage(parameterName, minimumArgumentName));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanOrEqualToOther(
            String parameterName,
            byte argument,
            String minmumParameterName,
            byte minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherMessage(parameterName, minmumParameterName));
        }
        return argument;
    }
    
    public static byte mustBeLessThanOther(
            String parameterName,
            byte argument,
            String maximumParameterName,
            byte maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static byte mustBeLessThanOrEqualToOther(
            String parameterName,
            byte argument,
            String maximumParameterName,
            byte maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static byte mustBetweenOther(
            String parameterName,
            byte argument,
            String minimumParameterName,
            byte minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            byte maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherMessage(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static short mustBeEqualToOther(
            String parameterName,
            short argument,
            String valueParameterName,
            short valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static short mustNotBeEqualToOther(
            String parameterName,
            short argument,
            String valueParameterName,
            short valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanOther(
            String parameterName,
            short argument,
            String minimumParameterName,
            short minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanOrEqualToOther(
            String parameterName,
            short argument,
            String minimumParameterName,
            short minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static short mustBeLessThanOther(
            String parameterName,
            short argument,
            String maximumParameterName,
            short maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static short mustBeLessThanOrEqualToOther(
            String parameterName,
            short argument,
            String maximumParameterName,
            short maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static short mustBetweenOther(
            String parameterName,
            short argument,
            String minimumParameterName,
            short minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            short maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherMessage(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static int mustBeEqualToOther(
            String parameterName,
            int argument,
            String valueParameterName,
            int valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static int mustNotBeEqualToOther(
            String parameterName,
            int argument,
            String valueParameterName,
            int valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanOther(
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanOrEqualToOther(
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int mustBeLessThanOther(
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int mustBeLessThanOrEqualToOther(
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int mustBetweenOther(
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            int maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherMessage(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanOther(
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IndexOutOfBoundsException(
                    mustBeGreaterThanOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanOrEqualToOther(
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument < minimumArgument) {
            throw new IndexOutOfBoundsException(
                    mustBeGreaterThanOrEqualToOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanOther(
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IndexOutOfBoundsException(
                    mustBeLessThanOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanOrEqualToOther(
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument > maximumArgument) {
            throw new IndexOutOfBoundsException(
                    mustBeLessThanOrEqualToOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBetweenOther(
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            int maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IndexOutOfBoundsException(
                    mustBetweenOtherMessage(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static long mustBeEqualToOther(
            String parameterName,
            long argument,
            String valueParameterName,
            long valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static long mustNotBeEqualToOther(
            String parameterName,
            long argument,
            String valueParameterName,
            long valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanOther(
            String parameterName,
            long argument,
            String minimumParmeterName,
            long minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherMessage(parameterName, minimumParmeterName));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanOrEqualToOther(
            String parameterName,
            long argument,
            String minimumParameterName,
            long minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static long mustBeLessThanOther(
            String parameterName,
            long argument,
            String maximumParameterName,
            long maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static long mustBeLessThanOrEqualToOther(
            String parameterName,
            long argument,
            String maximumParameterName,
            long maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static long mustBetweenOther(
            String parameterName,
            long argument,
            String minimumParameterName,
            long minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            long maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherMessage(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static float mustBeEqualToOther(
            String parameterName,
            float argument,
            String valueParameterName,
            float valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static float mustNotBeEqualToOther(
            String parameterName,
            float argument,
            String valueParameterName,
            float valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanOther(
            String parameterName,
            float argument,
            String minimumParameterName,
            float minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanOrEqualToOther(
            String parameterName,
            float argument,
            String minimumParameterName,
            float minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static float mustBeLessThanOther(
            String parameterName,
            float argument,
            String maximumParameterName,
            float maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static float mustBeLessThanOrEqualToOther(
            String parameterName,
            float argument,
            String maximumParameterName,
            float maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static float mustBetweenOther(
            String parameterName,
            float argument,
            String minimumParameterName,
            float minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            float maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherMessage(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static double mustBeEqualToOther(
            String parameterName,
            double argument,
            String valueParameterName,
            double valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static double mustNotBeEqualToOther(
            String parameterName,
            double argument,
            String valueParameterName,
            double valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanOther(
            String parameterName,
            double argument,
            String minimumParameterName,
            double minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanOrEqualToOther(
            String parameterName,
            double argument,
            String minimumParameterName,
            double minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static double mustBeLessThanOther(
            String parameterName,
            double argument,
            String maximumParameterName,
            double maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static double mustBeLessThanOrEqualToOther(
            String parameterName,
            double argument,
            String maximumParameterName,
            double maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static double mustBetweenOther(
            String parameterName,
            double argument,
            String minimumParameterName,
            double minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            double maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherMessage(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static <T> T mustBeEqualToOther(
            String parameterName,
            T argument,
            String valueParameterName,
            T valueArgument) {
        if (argument != null && valueArgument != null && !argument.equals(valueArgument)) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static <T> T mustNotBeEqualToOther(
            String parameterName,
            T argument,
            String valueParameterName,
            T valueArgument) {
        if (argument != null && valueArgument != null && argument.equals(valueArgument)) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherMessage(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanOther(
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumArgument) {
        if (argument != null && minimumArgument != null && argument.compareTo(minimumArgument) <= 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeGreaterThanOther(
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeGreaterThanOther(
                    parameterName, 
                    (Comparable)argument, 
                    minimumParameterName, 
                    (Comparable)minimumValue);
        }
        if (argument != null && minimumValue != null && comparator.compare(argument, minimumValue) <= 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanOrEqualToOther(
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumArgument) {
        if (argument != null && minimumArgument != null && argument.compareTo(minimumArgument) < 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeGreaterThanOrEqualToOther(
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeGreaterThanOrEqualToOther(
                    parameterName, 
                    (Comparable)argument, 
                    minimumParameterName, 
                    (Comparable)minimumValue);
        }
        if (argument != null && minimumValue != null && comparator.compare(argument, minimumValue) < 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherMessage(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanOther(
            String parameterName,
            T argument,
            String maximumParameterName,
            T maximumArgument) {
        if (argument != null && maximumArgument != null && argument.compareTo(maximumArgument) >= 0) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeLessThanOther(
            String parameterName,
            T argument,
            String maximumParameterName,
            T maximumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeLessThanOther(
                    parameterName, 
                    (Comparable)argument, 
                    maximumParameterName, 
                    (Comparable)maximumValue);
        }
        if (argument != null && maximumValue != null && comparator.compare(argument, maximumValue) >= 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanOrEqualToOther(
            String parameterName,
            T argument,
            String maximumParameterName,
            T maximumArgument) {
        if (argument != null && maximumArgument != null && argument.compareTo(maximumArgument) > 0) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeLessThanOrEqualToOther(
            String parameterName,
            T argument,
            String maximumParameterName,
            T maximumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeLessThanOrEqualToOther(
                    parameterName, 
                    (Comparable)argument, 
                    maximumParameterName, 
                    (Comparable)maximumValue);
        }
        if (argument != null && maximumValue != null && comparator.compare(argument, maximumValue) > 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherMessage(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBetweenOther(
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            T maximumArgument,
            boolean maximumInclusive) {
        if (argument == null) {
            return null;
        }
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumArgument != null) {
            if (minimumInclusive) {
                if (argument.compareTo(minimumArgument) < 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(minimumArgument) <= 0) {
                    throwable = true;
                }
            }
        }
        if (!throwable && maximumArgument != null) {
            if (maximumInclusive) {
                if (argument.compareTo(maximumArgument) > 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(maximumArgument) >= 0) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherMessage(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBetweenOther(
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumValue,
            boolean minimumInclusive,
            String maximumParameterName,
            T maximumValue,
            boolean maximumInclusive,
            Comparator<? super T> comparator) {
        if (argument == null) {
            return null;
        }
        if (comparator == null) {
            return (T)mustBetweenOther(
                    parameterName,
                    (Comparable)argument,
                    minimumParameterName,
                    (Comparable)minimumValue,
                    minimumInclusive,
                    maximumParameterName,
                    (Comparable)maximumValue,
                    maximumInclusive);
        }
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumValue != null) {
            if (minimumInclusive) {
                if (comparator.compare(argument, minimumValue) < 0) {
                    throwable = true;
                }
            } else {
                if (comparator.compare(argument, minimumValue) <= 0) {
                    throwable = true;
                }
            }
        }
        if (!throwable && maximumValue != null) {
            if (maximumInclusive) {
                if (comparator.compare(argument, maximumValue) > 0) {
                    throwable = true;
                }
            } else {
                if (comparator.compare(argument, maximumValue) >= 0) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueMessage(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static <T> T mustBeInstanceOfOther(
            String parameterName,
            T argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && !classArgument.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    mustBeInstanceOfOtherMessage(parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAllOfOther(
            String parameterName,
            T argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                mustBeInstanceOfAllOfOtherMessage(parameterName, classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAnyOfOther(
            String parameterName,
            T argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            boolean throwable = false;
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        mustBeInstanceOfAnyOfOtherMessage(parameterName, classesParameterName)
                );
            }
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfOther(
            String parameterName,
            T argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && classArgument.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    mustNotBeInstanceOfOtherMessage(parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfAnyOfOther(
            String parameterName,
            T argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                mustNotBeInstanceOfAnyOfOtherMessage(parameterName, classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithOther(
            String parameterName,
            Class<T> argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && !classArgument.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    mustBeCompatibleWithOtherMessage(parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAllOfOther(
            String parameterName,
            Class<T> argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null) {
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                mustBeCompatibleWithAllOfOtherMessage(parameterName,classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAnyOfOther(
            String parameterName,
            Class<T> argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            boolean throwable = false;
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        mustBeCompatibleWithAnyOfOtherMessage(parameterName, classesParameterName)
                );
            }
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithOther(
            String parameterName,
            Class<T> argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && classArgument.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    mustNotBeCompatibleWithOtherMessage(parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithAnyOfOther(
            String parameterName,
            Class<T> argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                mustNotBeCompatibleWithAnyOfOtherMessage(parameterName, classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }
    
    public static char mustBeAnyOfOther(
            String parameterName, 
            char argument, 
            String valueParameterName,
            char ... charactersValueArgument) {
        for (char c : charactersValueArgument) {
            if (c == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherMessage(parameterName, valueParameterName)
        );
    }
    
    public static char mustNotBeAnyOfOther(
            String parameterName, 
            char argument, 
            String valueParameterName,
            char ... charactersValueArgument) {
        for (char c : charactersValueArgument) {
            if (c == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherMessage(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static byte mustBeAnyOfOther(
            String parameterName, 
            byte argument, 
            String valueParameterName,
            byte ... bytesValueArgument) {
        for (byte b : bytesValueArgument) {
            if (b == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherMessage(parameterName, valueParameterName)
        );
    }
    
    public static byte mustNotBeAnyOfOther(
            String parameterName, 
            byte argument, 
            String valueParameterName,
            byte ... bytesValueArgument) {
        for (byte b : bytesValueArgument) {
            if (b == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherMessage(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static short mustBeAnyOfOther(
            String parameterName, 
            short argument, 
            String valueParameterName,
            short ... shortsValueArgument) {
        for (short s : shortsValueArgument) {
            if (s == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherMessage(parameterName, valueParameterName)
        );
    }
    
    public static short mustNotBeAnyOfOther(
            String parameterName, 
            short argument, 
            String valueParameterName,
            short ... shortsValueArgument) {
        for (short s : shortsValueArgument) {
            if (s == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherMessage(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static int mustBeAnyOfOther(
            String parameterName, 
            int argument, 
            String valueParameterName,
            int ... intsValueArgument) {
        for (int i : intsValueArgument) {
            if (i == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherMessage(parameterName, valueParameterName)
        );
    }
    
    public static int mustNotBeAnyOfOther(
            String parameterName, 
            int argument, 
            String valueParameterName,
            int ... intsValueArgument) {
        for (int i : intsValueArgument) {
            if (i == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherMessage(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static long mustBeAnyOfOther(
            String parameterName, 
            long argument, 
            String valueParameterName,
            long ... longsValueArgument) {
        for (long l : longsValueArgument) {
            if (l == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherMessage(parameterName, valueParameterName)
        );
    }
    
    public static long mustNotBeAnyOfOther(
            String parameterName, 
            long argument, 
            String valueParameterName,
            long ... longsValueArgument) {
        for (long l : longsValueArgument) {
            if (l == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherMessage(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static float mustBeAnyOfOther(
            String parameterName, 
            float argument, 
            String valueParameterName,
            float ... floatsValueArgument) {
        for (float f : floatsValueArgument) {
            if (f == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherMessage(parameterName, valueParameterName)
        );
    }
    
    public static float mustNotBeAnyOfOther(
            String parameterName, 
            float argument, 
            String valueParameterName,
            float ... floatsValueArgument) {
        for (float f : floatsValueArgument) {
            if (f == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherMessage(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static double mustBeAnyOfOther(
            String parameterName, 
            double argument, 
            String valueParameterName,
            double ... doublesValueArgument) {
        for (double d : doublesValueArgument) {
            if (d == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherMessage(parameterName, valueParameterName)
        );
    }

    public static double mustNotBeAnyOfOther(
            String parameterName, 
            double argument, 
            String valueParameterName,
            double ... doublesValueArgument) {
        for (double d : doublesValueArgument) {
            if (d == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherMessage(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }

    @SafeVarargs
    public static <T> T mustBeAnyOfOther(
            String parameterName, 
            T argument, 
            String valueParameterName,
            T ... objectsValueArgument) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValueArgument) {
            if (argument.equals(o)) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherMessage(parameterName, valueParameterName)
        );
    }
    
    @SafeVarargs
    public static <T> T mustNotBeAnyOfOther(
            String parameterName, 
            T argument, 
            String valueParameterName,
            T ... objectsValueArgument) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValueArgument) {
            if (argument.equals(o)) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherMessage(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static <T> T mustBeNullWhen(
            String whenCondition, 
            String parameterName, 
            T argument) {
        if (argument != null) {
            throw new IllegalArgumentException(
                    mustBeNullWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> T mustNotBeNullWhen(
            String whenCondition, 
            String parameterName, 
            T argument) {
        if (argument == null) {
            throw new NullArgumentException(
                    mustNotBeNullWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static boolean[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            boolean[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static char[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            char[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static byte[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            byte[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static short[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            short[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static int[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            int[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static long[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            long[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static float[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            float[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static double[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            double[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> T[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            T[] argument) {
        if (argument != null && argument.length != 0) {
            throw new IllegalArgumentException(
                    mustBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }

    public static boolean[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            boolean[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static char[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            char[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static byte[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            byte[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static short[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            short[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static int[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            int[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static long[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            long[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static float[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            float[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static double[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            double[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> T[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            T[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static String mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            String argument) {
        if (argument != null && !argument.isEmpty()) {
            throw new IllegalArgumentException(
                    mustBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static String mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            String argument) {
        if (argument != null && argument.isEmpty()) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            I argument) {
        if (argument != null && !isEmpty(argument)) {
            throw new IllegalArgumentException(
                    mustBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            I argument) {
        if (argument != null && isEmpty(argument)) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            M argument) {
        if (argument != null && !argument.isEmpty()) {
            throw new IllegalArgumentException(
                    mustBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            M argument) {
        if (argument != null && argument.isEmpty()) {
            throw new IllegalArgumentException(
                    mustNotBeEmptyWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <E> E[] mustNotContainNullElementsWhen(
            String whenCondition, 
            String parameterName,
            E[] argument) {
        if (argument != null) {
            for (Object o : argument) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            mustNotContainNullElementsWhenMessage(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static String[] mustNotContainEmptyElementsWhen(
            String whenCondition, 
            String parameterName,
            String[] argument) {
        if (argument != null) {
            for (String s : argument) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            mustNotContainEmptyElementsWhenMessage(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <E> E[] mustNotContainSpecialElementsWhen(
            String whenCondition, 
            String parameterName,
            E[] argument,
            Class<?> clazz) {
        if (argument != null) {
            for (Object o : argument) {
                if (o != null && clazz.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            mustNotContainSpecialElementsWhenMessage(whenCondition, parameterName, clazz));
                }
            }
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustNotContainNullElementsWhen(
            String whenCondition, 
            String parameterName,
            I argument) {
        if (argument != null) {
            for (Object o : argument) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            mustNotContainNullElementsWhenMessage(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <I extends Iterable<String>> I mustNotContainEmptyElementsWhen(
            String whenCondition, 
            String parameterName,
            I argument) {
        if (argument != null) {
            for (String s : argument) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            mustNotContainEmptyElementsWhenMessage(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustNotContainSpecialElementsWhen(
            String whenCondition, 
            String parameterName,
            I argument,
            Class<?> clazz) {
        if (argument != null) {
            for (Object o : argument) {
                if (o != null && clazz.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            mustNotContainSpecialElementsWhenMessage(whenCondition, parameterName, clazz));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainNullKeysWhen(
            String whenCondition, 
            String parameterName,
            M argument) {
        if (argument != null) {
            for (Object o : argument.keySet()) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            mustNotContainNullKeysWhenMessage(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <V> Map<String, V> mustNotContainEmptyElementsWhen(
            String whenCondition, 
            String parameterName,
            Map<String, V> argument) {
        if (argument != null) {
            for (String s : argument.keySet()) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            mustNotContainEmptyElementsWhenMessage(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainSpecialKeysWhen(
            String whenCondition, 
            String parameterName,
            M argument,
            Class<?> clazz) {
        if (argument != null) {
            for (Object o : argument.keySet()) {
                if (o != null && clazz.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            mustNotContainSpecialKeysWhenMessage(whenCondition, parameterName, clazz));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainNullValuesWhen(
            String whenCondition, 
            String parameterName,
            M argument) {
        if (argument != null) {
            for (Object o : argument.values()) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            mustNotContainNullValuesWhenMessage(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <K> Map<K, String> mustNotContainEmptyValuesWhen(
            String whenCondition, 
            String parameterName,
            Map<K, String> argument) {
        if (argument != null) {
            for (String s : argument.values()) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            mustNotContainEmptyElementsWhenMessage(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainSpecialValuesWhen(
            String whenCondition, 
            String parameterName,
            M argument,
            Class<?> clazz) {
        if (argument != null) {
            for (Object o : argument.values()) {
                if (o != null && clazz.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            mustNotContainSpecialValuesWhenMessage(whenCondition, parameterName, clazz));
                }
            }
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeClassWhen(String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null) {
            if (argument.isInterface() ||
                    argument.isEnum() ||
                    argument.isAnnotation() ||
                    argument.isArray() ||
                    argument.isPrimitive()) {
                throw new IllegalArgumentException(mustBeClassWhenMessage(whenCondition, parameterName));
            }
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeClassWhen(String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null) {
            if (!argument.isInterface() &&
                    !argument.isEnum() &&
                    !argument.isAnnotation() &&
                    !argument.isArray() &&
                    !argument.isPrimitive()) {
                throw new IllegalArgumentException(mustNotBeClassWhenMessage(whenCondition, parameterName));
            }
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeInterfaceWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !argument.isInterface()) {
            throw new IllegalArgumentException(mustBeInterfaceWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeInterfaceWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && argument.isInterface()) {
            throw new IllegalArgumentException(mustNotBeInterfaceWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeEnumWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !argument.isEnum()) {
            throw new IllegalArgumentException(mustBeEnumWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeEnumWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && argument.isEnum()) {
            throw new IllegalArgumentException(mustNotBeEnumWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeAnnotationWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !argument.isAnnotation()) {
            throw new IllegalArgumentException(mustBeAnnotationWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeAnnotationWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && argument.isAnnotation()) {
            throw new IllegalArgumentException(mustNotBeAnnotationWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeArrayWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !argument.isArray()) {
            throw new IllegalArgumentException(mustBeArrayWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeArrayWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && argument.isArray()) {
            throw new IllegalArgumentException(mustNotBeArrayWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBePrimitiveWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !argument.isPrimitive()) {
            throw new IllegalArgumentException(mustBePrimitiveWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBePrimitiveWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && argument.isPrimitive()) {
            throw new IllegalArgumentException(mustNotBePrimitiveWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeAbstractWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !Modifier.isAbstract(argument.getModifiers())) {
            throw new IllegalArgumentException(mustBeAbstractWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustNotBeAbstractWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && Modifier.isAbstract(argument.getModifiers())) {
            throw new IllegalArgumentException(mustNotBeAbstractWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeFinalWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !Modifier.isFinal(argument.getModifiers())) {
            throw new IllegalArgumentException(mustBeFinalWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustNotBeFinalWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && Modifier.isFinal(argument.getModifiers())) {
            throw new IllegalArgumentException(mustNotBeFinalWhenMessage(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static char mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            char value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueWhenMessage(whenCondition, parameterName, Character.toString(value)));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            char minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueWhenMessage(whenCondition, parameterName, Character.toString(minimumValue)));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            char minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueWhenMessage(whenCondition, parameterName, Character.toString(minimumValue)));
        }
        return argument;
    }
    
    public static char mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            char maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueWhenMessage(whenCondition, parameterName, Character.toString(maximumValue)));
        }
        return argument;
    }
    
    public static char mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            char maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueWhenMessage(whenCondition, parameterName, Character.toString(maximumValue)));
        }
        return argument;
    }
    
    public static char mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            char minimumValue,
            boolean minimumInclusive,
            char maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Character.toString(minimumValue),
                            maximumOp,
                            Character.toString(maximumValue)));
        }
        return argument;
    }

    public static byte mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            byte value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueWhenMessage(whenCondition, parameterName, Byte.toString(value)));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            byte minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueWhenMessage(whenCondition, parameterName, Byte.toString(minimumValue)));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            byte minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueWhenMessage(whenCondition, parameterName, Byte.toString(minimumValue)));
        }
        return argument;
    }
    
    public static byte mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            byte maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueWhenMessage(whenCondition, parameterName, Byte.toString(maximumValue)));
        }
        return argument;
    }
    
    public static byte mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            byte maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueWhenMessage(whenCondition, parameterName, Byte.toString(maximumValue)));
        }
        return argument;
    }
    
    public static byte mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            byte minimumValue,
            boolean minimumInclusive,
            byte maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Byte.toString(minimumValue),
                            maximumOp,
                            Byte.toString(maximumValue)));
        }
        return argument;
    }
    
    public static short mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            short value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueWhenMessage(whenCondition, parameterName, Short.toString(value)));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            short minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueWhenMessage(whenCondition, parameterName, Short.toString(minimumValue)));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            short minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueWhenMessage(whenCondition, parameterName, Short.toString(minimumValue)));
        }
        return argument;
    }
    
    public static short mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            short maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueWhenMessage(whenCondition, parameterName, Short.toString(maximumValue)));
        }
        return argument;
    }
    
    public static short mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            short maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueWhenMessage(whenCondition, parameterName, Short.toString(maximumValue)));
        }
        return argument;
    }
    
    public static short mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            short minimumValue,
            boolean minimumInclusive,
            short maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Short.toString(minimumValue),
                            maximumOp,
                            Short.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueWhenMessage(whenCondition, parameterName, Integer.toString(value)));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueWhenMessage(whenCondition, parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueWhenMessage(whenCondition, parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueWhenMessage(whenCondition, parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueWhenMessage(whenCondition, parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int minimumValue,
            boolean minimumInclusive,
            int maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Integer.toString(minimumValue),
                            maximumOp,
                            Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument <= minimumValue) {
            throw new IndexOutOfBoundsException(
                    mustBeGreaterThanValueWhenMessage(whenCondition, parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument < minimumValue) {
            throw new IndexOutOfBoundsException(
                    mustBeGreaterThanOrEqualToValueWhenMessage(whenCondition, parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument >= maximumValue) {
            throw new IndexOutOfBoundsException(
                    mustBeLessThanValueWhenMessage(whenCondition, parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument > maximumValue) {
            throw new IndexOutOfBoundsException(
                    mustBeLessThanOrEqualToValueWhenMessage(whenCondition, parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int indexMustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int minimumValue,
            boolean minimumInclusive,
            int maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IndexOutOfBoundsException(
                    mustBetweenValueWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Integer.toString(minimumValue),
                            maximumOp,
                            Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static long mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            long value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueWhenMessage(whenCondition, parameterName, Long.toString(value)));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            long minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueWhenMessage(whenCondition, parameterName, Long.toString(minimumValue)));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            long minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueWhenMessage(whenCondition, parameterName, Long.toString(minimumValue)));
        }
        return argument;
    }
    
    public static long mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            long maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueWhenMessage(whenCondition, parameterName, Long.toString(maximumValue)));
        }
        return argument;
    }
    
    public static long mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            long maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueWhenMessage(whenCondition, parameterName, Long.toString(maximumValue)));
        }
        return argument;
    }
    
    public static long mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            long minimumValue,
            boolean minimumInclusive,
            long maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Long.toString(minimumValue),
                            maximumOp,
                            Long.toString(maximumValue)));
        }
        return argument;
    }
    
    public static float mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            float value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueWhenMessage(whenCondition, parameterName, Float.toString(value)));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            float minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueWhenMessage(whenCondition, parameterName, Float.toString(minimumValue)));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            float minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueWhenMessage(whenCondition, parameterName, Float.toString(minimumValue)));
        }
        return argument;
    }
    
    public static float mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            float maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueWhenMessage(whenCondition, parameterName, Float.toString(maximumValue)));
        }
        return argument;
    }
    
    public static float mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            float maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueWhenMessage(whenCondition, parameterName, Float.toString(maximumValue)));
        }
        return argument;
    }
    
    public static float mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            float minimumValue,
            boolean minimumInclusive,
            float maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Float.toString(minimumValue),
                            maximumOp,
                            Float.toString(maximumValue)));
        }
        return argument;
    }
    
    public static double mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            double value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueWhenMessage(whenCondition, parameterName, Double.toString(value)));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            double minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueWhenMessage(whenCondition, parameterName, Double.toString(minimumValue)));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            double minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueWhenMessage(whenCondition, parameterName, Double.toString(minimumValue)));
        }
        return argument;
    }
    
    public static double mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            double maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueWhenMessage(whenCondition, parameterName, Double.toString(maximumValue)));
        }
        return argument;
    }
    
    public static double mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            double maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueWhenMessage(whenCondition, parameterName, Double.toString(maximumValue)));
        }
        return argument;
    }
    
    public static double mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            double minimumValue,
            boolean minimumInclusive,
            double maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Double.toString(minimumValue),
                            maximumOp,
                            Double.toString(maximumValue)));
        }
        return argument;
    }
    
    public static <T> T mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            T value) {
        if (argument != null && value != null && argument.equals(value)) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToValueWhenMessage(whenCondition, parameterName, value.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            T minimumValue) {
        if (argument != null && minimumValue != null && argument.compareTo(minimumValue) <= 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanValueWhenMessage(whenCondition, parameterName, minimumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            T minimumValue) {
        if (argument != null && minimumValue != null && argument.compareTo(minimumValue) < 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToValueWhenMessage(whenCondition, parameterName, minimumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            T maximumValue) {
        if (argument != null && maximumValue != null && argument.compareTo(maximumValue) >= 0) {
            throw new IllegalArgumentException(
                    mustBeLessThanValueWhenMessage(whenCondition, parameterName, maximumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            T maximumValue) {
        if (argument != null && maximumValue != null && argument.compareTo(maximumValue) > 0) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToValueWhenMessage(whenCondition, parameterName, maximumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            T minimumValue,
            boolean minimumInclusive,
            T maximumValue,
            boolean maximumInclusive) {
        if (argument == null) {
            return null;
        }
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumValue != null) {
            if (minimumInclusive) {
                if (argument.compareTo(minimumValue) < 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(minimumValue) <= 0) {
                    throwable = true;
                }
            }
        }
        if (!throwable && maximumValue != null) {
            if (maximumInclusive) {
                if (argument.compareTo(maximumValue) > 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(maximumValue) >= 0) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenValueWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumValue == null ? null : maximumValue.toString(),
                            maximumOp,
                            maximumValue == null ? null : maximumValue.toString()));
        }
        return argument;
    }
    
    public static <T> T mustBeInstanceOfValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            Class<?> classValue) {
        if (argument != null && !classValue.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    mustBeInstanceOfValueWhenMessage(whenCondition, parameterName, classValue));
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAllOfValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            Class<?> ... classesValue) {
        if  (argument != null) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                mustBeInstanceOfAllOfValueWhenMessage(whenCondition, parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAnyOfValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            boolean throwable = false;
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        mustBeInstanceOfAnyOfValueWhenMessage(whenCondition, parameterName, classesValue)
                );
            }
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            Class<?> classValue) {
        if (argument != null && classValue.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    mustNotBeInstanceOfValueWhenMessage(whenCondition, parameterName, classValue));
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfAnyOfValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                mustNotBeInstanceOfAnyOfValueWhenMessage(whenCondition, parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithValueWhen(
            String whenCondition, 
            String parameterName,
            Class<T> argument,
            Class<?> classValue) {
        if (argument != null && !classValue.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    mustBeCompatibleWithValueWhenMessage(whenCondition, parameterName, classValue));
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAllOfValueWhen(
            String whenCondition, 
            String parameterName,
            Class<T> argument,
            Class<?> ... classesValue) {
        if  (argument != null) {
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                mustBeCompatibleWithAllOfValueWhenMessage(whenCondition, parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAnyOfValueWhen(
            String whenCondition, 
            String parameterName,
            Class<T> argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            boolean throwable = false;
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        mustBeCompatibleWithAnyOfValueWhenMessage(whenCondition, parameterName, classesValue)
                );
            }
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithValueWhen(
            String whenCondition, 
            String parameterName,
            Class<T> argument,
            Class<?> classValue) {
        if (argument != null && classValue.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    mustNotBeCompatibleWithValueWhenMessage(whenCondition, parameterName, classValue));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithAnyOfValueWhen(
            String whenCondition, 
            String parameterName,
            Class<T> argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                mustNotBeCompatibleWithAnyOfValueWhenMessage(whenCondition, parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }
    
    public static char mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            char argument, 
            char ... charactersValue) {
        for (char c : charactersValue) {
            if (c == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(charactersValue))
        );
    }
    
    public static char mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            char argument, 
            char ... charactersValue) {
        for (char c : charactersValue) {
            if (c == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(charactersValue))
                );
            }
        }
        return argument;
    }
    
    public static byte mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            byte argument, 
            byte ... bytesValue) {
        for (byte b : bytesValue) {
            if (b == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(bytesValue))
        );
    }
    
    public static byte mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            byte argument, 
            byte ... bytesValue) {
        for (byte b : bytesValue) {
            if (b == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(bytesValue))
                );
            }
        }
        return argument;
    }
    
    public static short mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            short argument, 
            short ... shortsValue) {
        for (short s : shortsValue) {
            if (s == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(shortsValue))
        );
    }
    
    public static short mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            short argument, 
            short ... shortsValue) {
        for (short s : shortsValue) {
            if (s == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(shortsValue))
                );
            }
        }
        return argument;
    }
    
    public static int mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            int argument, 
            int ... intsValue) {
        for (int i : intsValue) {
            if (i == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(intsValue))
        );
    }
    
    public static int mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            int argument, 
            int ... intsValue) {
        for (int i : intsValue) {
            if (i == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(intsValue))
                );
            }
        }
        return argument;
    }
    
    public static long mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            long argument, 
            long ... longsValue) {
        for (long l : longsValue) {
            if (l == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(longsValue))
        );
    }
    
    public static long mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            long argument, 
            long ... longsValue) {
        for (long l : longsValue) {
            if (l == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(longsValue))
                );
            }
        }
        return argument;
    }
    
    public static float mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            float argument, 
            float ... floatsValue) {
        for (float f : floatsValue) {
            if (f == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(floatsValue))
        );
    }
    
    public static float mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            float argument, 
            float ... floatsValue) {
        for (float f : floatsValue) {
            if (f == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(floatsValue))
                );
            }
        }
        return argument;
    }
    
    public static double mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            double argument, 
            double ... doublesValue) {
        for (double d : doublesValue) {
            if (d == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(doublesValue))
        );
    }

    public static double mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            double argument, 
            double ... doublesValue) {
        for (double d : doublesValue) {
            if (d == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(doublesValue))
                );
            }
        }
        return argument;
    }

    @SafeVarargs
    public static <T> T mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            T argument, 
            T ... objectsValue) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValue) {
            if (argument.equals(o)) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(objectsValue))
        );
    }
    
    @SafeVarargs
    public static <T> T mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            T argument, 
            T ... objectsValue) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValue) {
            if (argument.equals(o)) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfValueWhenMessage(whenCondition, parameterName, Arrays.asList(objectsValue))
                );
            }
        }
        return argument;
    }
    
    public static char mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String valueParameterName,
            char valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static char mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String valueParameterName,
            char valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String minimumParameterName,
            char minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String minimumParameterName, 
            char minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static char mustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String maximumParameterName,
            char maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static char mustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String maximumParameterName,
            char maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static char mustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String minimumParameterName,
            char minimumArgument,
            String maximumParameterName,
            boolean minimumInclusive,
            char maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static byte mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String valueParameterName,
            byte valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }

    public static byte mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String valueParamterName,
            byte valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParamterName));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String minimumArgumentName,
            byte minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherWhenMessage(whenCondition, parameterName, minimumArgumentName));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String minmumParameterName,
            byte minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherWhenMessage(whenCondition, parameterName, minmumParameterName));
        }
        return argument;
    }
    
    public static byte mustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String maximumParameterName,
            byte maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static byte mustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String maximumParameterName,
            byte maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static byte mustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String minimumParameterName,
            byte minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            byte maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static short mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String valueParameterName,
            short valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static short mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String valueParameterName,
            short valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String minimumParameterName,
            short minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String minimumParameterName,
            short minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static short mustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String maximumParameterName,
            short maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static short mustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String maximumParameterName,
            short maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static short mustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String minimumParameterName,
            short minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            short maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static int mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String valueParameterName,
            int valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static int mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String valueParameterName,
            int valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int mustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int mustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int mustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            int maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IndexOutOfBoundsException(
                    mustBeGreaterThanOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument < minimumArgument) {
            throw new IndexOutOfBoundsException(
                    mustBeGreaterThanOrEqualToOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IndexOutOfBoundsException(
                    mustBeLessThanOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument > maximumArgument) {
            throw new IndexOutOfBoundsException(
                    mustBeLessThanOrEqualToOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            int maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IndexOutOfBoundsException(
                    mustBetweenOtherWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static long mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String valueParameterName,
            long valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static long mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String valueParameterName,
            long valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String minimumParmeterName,
            long minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherWhenMessage(whenCondition, parameterName, minimumParmeterName));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String minimumParameterName,
            long minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherWhenMessage(
                            whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static long mustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String maximumParameterName,
            long maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static long mustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String maximumParameterName,
            long maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static long mustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String minimumParameterName,
            long minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            long maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static float mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String valueParameterName,
            float valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static float mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String valueParameterName,
            float valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String minimumParameterName,
            float minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String minimumParameterName,
            float minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static float mustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String maximumParameterName,
            float maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static float mustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String maximumParameterName,
            float maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static float mustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String minimumParameterName,
            float minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            float maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static double mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            String valueParameterName,
            double valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static double mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            String valueParameterName,
            double valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            String minimumParameterName,
            double minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            String minimumParameterName,
            double minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static double mustBeLessThanOtherWhen(String whenCondition, 
            String parameterName,
            double argument,
            String maximumParameterName,
            double maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static double mustBeLessThanOrEqualToOtherWhen(String whenCondition, 
            String parameterName,
            double argument,
            String maximumParameterName,
            double maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static double mustBetweenOtherWhen(String whenCondition, 
            String parameterName,
            double argument,
            String minimumParameterName,
            double minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            double maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static <T> T mustBeEqualToOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String valueParameterName,
            T valueArgument) {
        if (argument != null && valueArgument != null && !argument.equals(valueArgument)) {
            throw new IllegalArgumentException(
                    mustBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static <T> T mustNotBeEqualToOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String valueParameterName,
            T valueArgument) {
        if (argument != null && valueArgument != null && argument.equals(valueArgument)) {
            throw new IllegalArgumentException(
                    mustNotBeEqualToOtherWhenMessage(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumArgument) {
        if (argument != null && minimumArgument != null && argument.compareTo(minimumArgument) <= 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanOrEqualToOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumArgument) {
        if (argument != null && minimumArgument != null && argument.compareTo(minimumArgument) < 0) {
            throw new IllegalArgumentException(
                    mustBeGreaterThanOrEqualToOtherWhenMessage(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String maximumParameterName,
            T maximumArgument) {
        if (argument != null && maximumArgument != null && argument.compareTo(maximumArgument) >= 0) {
            throw new IllegalArgumentException(
                    mustBeLessThanOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanOrEqualToOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String maximumParameterName,
            T maximumArgument) {
        if (argument != null && maximumArgument != null && argument.compareTo(maximumArgument) > 0) {
            throw new IllegalArgumentException(
                    mustBeLessThanOrEqualToOtherWhenMessage(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBetweenOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            T maximumArgument,
            boolean maximumInclusive) {
        if (argument == null) {
            return null;
        }
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumArgument != null) {
            if (minimumInclusive) {
                if (argument.compareTo(minimumArgument) < 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(minimumArgument) <= 0) {
                    throwable = true;
                }
            }
        }
        if (!throwable && maximumArgument != null) {
            if (maximumInclusive) {
                if (argument.compareTo(maximumArgument) > 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(maximumArgument) >= 0) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    mustBetweenOtherWhenMessage(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static <T> T mustBeInstanceOfOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && !classArgument.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    mustBeInstanceOfOtherWhenMessage(whenCondition, parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAllOfOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                mustBeInstanceOfAllOfOtherWhenMessage(whenCondition, parameterName, classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAnyOfOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            boolean throwable = false;
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        mustBeInstanceOfAnyOfOtherWhenMessage(whenCondition, parameterName, classesParameterName)
                );
            }
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && classArgument.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    mustNotBeInstanceOfOtherWhenMessage(whenCondition, parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfAnyOfOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                mustNotBeInstanceOfAnyOfOtherWhenMessage(whenCondition, parameterName, classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithOtherWhen(String whenCondition, 
            String parameterName,
            Class<T> argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && !classArgument.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    mustBeCompatibleWithOtherWhenMessage(whenCondition, parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAllOfOtherWhen(String whenCondition, 
            String parameterName,
            Class<T> argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null) {
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                mustBeCompatibleWithAllOfOtherWhenMessage(whenCondition, parameterName,classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAnyOfOtherWhen(String whenCondition, 
            String parameterName,
            Class<T> argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            boolean throwable = false;
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        mustBeCompatibleWithAnyOfOtherWhenMessage(whenCondition, parameterName, classesParameterName)
                );
            }
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithOtherWhen(String whenCondition, 
            String parameterName,
            Class<T> argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && classArgument.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    mustNotBeCompatibleWithOtherWhenMessage(whenCondition, parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithAnyOfOtherWhen(String whenCondition, 
            String parameterName,
            Class<T> argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                mustNotBeCompatibleWithAnyOfOtherWhenMessage(whenCondition, parameterName, classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }
    
    public static char mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            char argument, 
            String valueParameterName,
            char ... charactersValueArgument) {
        for (char c : charactersValueArgument) {
            if (c == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
        );
    }
    
    public static char mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            char argument, 
            String valueParameterName,
            char ... charactersValueArgument) {
        for (char c : charactersValueArgument) {
            if (c == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static byte mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            byte argument, 
            String valueParameterName,
            byte ... bytesValueArgument) {
        for (byte b : bytesValueArgument) {
            if (b == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
        );
    }
    
    public static byte mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            byte argument, 
            String valueParameterName,
            byte ... bytesValueArgument) {
        for (byte b : bytesValueArgument) {
            if (b == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static short mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            short argument, 
            String valueParameterName,
            short ... shortsValueArgument) {
        for (short s : shortsValueArgument) {
            if (s == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
        );
    }
    
    public static short mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            short argument, 
            String valueParameterName,
            short ... shortsValueArgument) {
        for (short s : shortsValueArgument) {
            if (s == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static int mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            int argument, 
            String valueParameterName,
            int ... intsValueArgument) {
        for (int i : intsValueArgument) {
            if (i == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
        );
    }
    
    public static int mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            int argument, 
            String valueParameterName,
            int ... intsValueArgument) {
        for (int i : intsValueArgument) {
            if (i == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static long mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            long argument, 
            String valueParameterName,
            long ... longsValueArgument) {
        for (long l : longsValueArgument) {
            if (l == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
        );
    }
    
    public static long mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            long argument, 
            String valueParameterName,
            long ... longsValueArgument) {
        for (long l : longsValueArgument) {
            if (l == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static float mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            float argument, 
            String valueParameterName,
            float ... floatsValueArgument) {
        for (float f : floatsValueArgument) {
            if (f == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
        );
    }
    
    public static float mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            float argument, 
            String valueParameterName,
            float ... floatsValueArgument) {
        for (float f : floatsValueArgument) {
            if (f == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static double mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            double argument, 
            String valueParameterName,
            double ... doublesValueArgument) {
        for (double d : doublesValueArgument) {
            if (d == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
        );
    }

    public static double mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            double argument, 
            String valueParameterName,
            double ... doublesValueArgument) {
        for (double d : doublesValueArgument) {
            if (d == argument) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }

    @SafeVarargs
    public static <T> T mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            T argument, 
            String valueParameterName,
            T ... objectsValueArgument) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValueArgument) {
            if (argument.equals(o)) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                mustBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
        );
    }
    
    @SafeVarargs
    public static <T> T mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            T argument, 
            String valueParameterName,
            T ... objectsValueArgument) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValueArgument) {
            if (argument.equals(o)) {
                throw new IllegalArgumentException(
                        mustNotBeAnyOfOtherWhenMessage(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    private static boolean isEmpty(Iterable<?> iterable) {
        if (iterable instanceof Collection<?>) {
            return ((Collection<?>)iterable).isEmpty();
        }
        Iterator<?> itr = iterable.iterator();
        return !itr.hasNext();
    } 
    
    /*
     * I18N methods
     */
    @I18N
    private static native String mustBeNullMessage(String parameterName);
    
    @I18N
    private static native String mustNotBeNullMessage(String parameterName);
    
    @I18N
    private static native String mustNotBeEmptyMessage(String parameterName);
    
    @I18N
    private static native String mustNotContainNullElementsMessage(String parameterName);
    
    @I18N
    private static native String mustNotContainEmptyElementsMessage(String parameterName);
    
    @I18N
    private static native String mustNotContainSpecialElementsMessage(String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustNotContainNullKeysMessage(String parameterName);
    
    @I18N
    private static native String mustNotContainEmptyKeysMessage(String parameterName);
    
    @I18N
    private static native String mustNotContainSpecialKeysMessage(String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustNotContainNullValuesMessage(String parameterName);
    
    @I18N
    private static native String mustNotContainEmptyValuesMessage(String parameterName);
    
    @I18N
    private static native String mustNotContainSpecialValuesMessage(String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustBeClassMessage(String parameter);
    
    @I18N
    private static native String mustNotBeClassMessage(String parameter);
    
    @I18N
    private static native String mustBeInterfaceMessage(String parameter);
    
    @I18N
    private static native String mustNotBeInterfaceMessage(String parameter);
    
    @I18N
    private static native String mustBeEnumMessage(String parameter);
    
    @I18N
    private static native String mustNotBeEnumMessage(String parameter);
    
    @I18N
    private static native String mustBeAnnotationMessage(String parameter);
    
    @I18N
    private static native String mustNotBeAnnotationMessage(String parameter);
    
    @I18N
    private static native String mustBeArrayMessage(String parameter);
    
    @I18N
    private static native String mustNotBeArrayMessage(String parameter);
    
    @I18N
    private static native String mustBePrimitiveMessage(String parameter);
    
    @I18N
    private static native String mustNotBePrimitiveMessage(String parameter);
    
    @I18N
    private static native String mustBeAbstractMessage(String parameter);
    
    @I18N
    private static native String mustNotBeAbstractMessage(String parameter);
    
    @I18N
    private static native String mustBeFinalMessage(String parameter);
    
    @I18N
    private static native String mustNotBeFinalMessage(String parameter);
    
    @I18N
    private static native String mustBeEqualToValueMessage(String parameterName, String value);
    
    @I18N
    private static native String mustNotBeEqualToValueMessage(String parameterName, String value);
    
    @I18N
    private static native String mustBeGreaterThanValueMessage(String parameterName, String value);
    
    @I18N
    private static native String mustBeGreaterThanOrEqualToValueMessage(String parameterName, String value);
    
    @I18N
    private static native String mustBeLessThanValueMessage(String parameterName, String value);
    
    @I18N
    private static native String mustBeLessThanOrEqualToValueMessage(String parameterName, String value);
    
    @I18N
    private static native String mustBetweenValueMessage(
            String parameterName, 
            String minimumOp,
            String minimumValue,
            String maximumOp,
            String maximumValue);
    
    @I18N
    private static native String mustBeInstanceOfValueMessage(String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustBeInstanceOfAllOfValueMessage(String parameterName, Class<?>[] classesValue);
    
    @I18N
    private static native String mustBeInstanceOfAnyOfValueMessage(String parameterName, Class<?>[] classesValue);
    
    @I18N
    private static native String mustNotBeInstanceOfValueMessage(String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustNotBeInstanceOfAnyOfValueMessage(String parameterName, Class<?>[] classesValue);
    
    @I18N
    private static native String mustBeCompatibleWithValueMessage(String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustBeCompatibleWithAllOfValueMessage(String parameterName, Class<?>[] classesValue);
    
    @I18N
    private static native String mustBeCompatibleWithAnyOfValueMessage(String parameterName, Class<?>[] classesValue);
    
    @I18N
    private static native String mustNotBeCompatibleWithValueMessage(String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustNotBeCompatibleWithAnyOfValueMessage(String parameterName, Class<?>[] classesValue);
    
    @I18N
    private static native String mustBeAnyOfValueMessage(String parameterName, Collection<?> collectionValue);
    
    @I18N
    private static native String mustNotBeAnyOfValueMessage(String parameterName, Collection<?> collectionValue);
    
    @I18N
    private static native String mustBeEqualToOtherMessage(String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustNotBeEqualToOtherMessage(String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustBeGreaterThanOtherMessage(String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustBeGreaterThanOrEqualToOtherMessage(String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustBeLessThanOtherMessage(String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustBeLessThanOrEqualToOtherMessage(String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustBetweenOtherMessage(
            String parameterName, 
            String minimumOp,
            String minimumParameterName,
            String maximumOp,
            String maximumParameterName);
    
    @I18N
    private static native String mustBeInstanceOfOtherMessage(String parameterName, String classParameterName);
    
    @I18N
    private static native String mustBeInstanceOfAllOfOtherMessage(String parameterName, String classesParameterName);
    
    @I18N
    private static native String mustBeInstanceOfAnyOfOtherMessage(String parameterName, String classesParameterName);
    
    @I18N
    private static native String mustNotBeInstanceOfOtherMessage(String parameterName, String classParameterName);
    
    @I18N
    private static native String mustNotBeInstanceOfAnyOfOtherMessage(String parameterName, String classesParameterName);
    
    @I18N
    private static native String mustBeCompatibleWithOtherMessage(String parameterName, String classParameterName);
    
    @I18N
    private static native String mustBeCompatibleWithAllOfOtherMessage(String parameterName, String classesParameterName);
    
    @I18N
    private static native String mustBeCompatibleWithAnyOfOtherMessage(String parameterName, String classesParameterName);
    
    @I18N
    private static native String mustNotBeCompatibleWithOtherMessage(String parameterName, String classParameterName);
    
    @I18N
    private static native String mustNotBeCompatibleWithAnyOfOtherMessage(String parameterName, String classesParameterName);
    
    @I18N
    private static native String mustBeAnyOfOtherMessage(String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustNotBeAnyOfOtherMessage(String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustBeNullWhenMessage(String whenCondition, String parameterName);
    
    @I18N
    private static native String mustNotBeNullWhenMessage(String whenCondition, String parameterName);
    
    @I18N
    private static native String mustBeEmptyWhenMessage(String whenCondition, String parameterName);
    
    @I18N
    private static native String mustNotBeEmptyWhenMessage(String whenCondition, String parameterName);
    
    @I18N
    private static native String mustNotContainNullElementsWhenMessage(String whenCondition, String parameterName);
    
    @I18N
    private static native String mustNotContainEmptyElementsWhenMessage(String whenCondition, String parameterName);
    
    @I18N
    private static native String mustNotContainSpecialElementsWhenMessage(String whenCondition, String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustNotContainNullKeysWhenMessage(String whenCondition, String parameterName);
    
    @I18N
    private static native String mustNotContainEmptyKeysWhenMessage(String whenCondition, String parameterName);
    
    @I18N
    private static native String mustNotContainSpecialKeysWhenMessage(String whenCondition, String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustNotContainNullValuesWhenMessage(String whenCondition, String parameterName);
    
    @I18N
    private static native String mustNotContainEmptyValuesWhenMessage(String whenCondition, String parameterName);
    
    @I18N
    private static native String mustNotContainSpecialValuesWhenMessage(String whenCondition, String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustBeClassWhenMessage(String widthCondition, String parameter);
    
    @I18N
    private static native String mustNotBeClassWhenMessage(String widthCondition, String parameter);
    
    @I18N
    private static native String mustBeInterfaceWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustNotBeInterfaceWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustBeEnumWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustNotBeEnumWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustBeAnnotationWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustNotBeAnnotationWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustBeArrayWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustNotBeArrayWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustBePrimitiveWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustNotBePrimitiveWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustBeAbstractWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustNotBeAbstractWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustBeFinalWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustNotBeFinalWhenMessage(String whenCondition, String parameter);
    
    @I18N
    private static native String mustNotBeEqualToValueWhenMessage(String whenCondition, String parameterName, String value);
    
    @I18N
    private static native String mustBeGreaterThanValueWhenMessage(String whenCondition, String parameterName, String value);
    
    @I18N
    private static native String mustBeGreaterThanOrEqualToValueWhenMessage(String whenCondition, String parameterName, String value);
    
    @I18N
    private static native String mustBeLessThanValueWhenMessage(String whenCondition, String parameterName, String value);
    
    @I18N
    private static native String mustBeLessThanOrEqualToValueWhenMessage(String whenCondition, String parameterName, String value);
    
    @I18N
    private static native String mustBetweenValueWhenMessage(
            String whenCondition, 
            String parameterName, 
            String minimumOp,
            String minimumValue,
            String maximumOp,
            String maximumValue);
    
    @I18N
    private static native String mustBeInstanceOfValueWhenMessage(String whenCondition, String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustBeInstanceOfAllOfValueWhenMessage(String whenCondition, String parameterName, Class<?>[] classesValue);
    
    @I18N
    private static native String mustBeInstanceOfAnyOfValueWhenMessage(String whenCondition, String parameterName, Class<?>[] classesValue);
    
    @I18N
    private static native String mustNotBeInstanceOfValueWhenMessage(String whenCondition, String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustNotBeInstanceOfAnyOfValueWhenMessage(String whenCondition, String parameterName, Class<?>[] classesValue);
    
    @I18N
    private static native String mustBeCompatibleWithValueWhenMessage(String whenCondition, String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustBeCompatibleWithAllOfValueWhenMessage(String whenCondition, String parameterName, Class<?>[] classesValue);
    
    @I18N
    private static native String mustBeCompatibleWithAnyOfValueWhenMessage(String whenCondition, String parameterName, Class<?>[] classesValue);
    
    @I18N
    private static native String mustNotBeCompatibleWithValueWhenMessage(String whenCondition, String parameterName, Class<?> classValue);
    
    @I18N
    private static native String mustNotBeCompatibleWithAnyOfValueWhenMessage(String whenCondition, String parameterName, Class<?>[] classesValue);
    
    @I18N
    private static native String mustBeAnyOfValueWhenMessage(String whenCondition, String parameterName, Collection<?> collectionValue);
    
    @I18N
    private static native String mustNotBeAnyOfValueWhenMessage(String whenCondition, String parameterName, Collection<?> collectionValue);
    
    @I18N
    private static native String mustBeEqualToOtherWhenMessage(String whenCondition, String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustNotBeEqualToOtherWhenMessage(String whenCondition, String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustBeGreaterThanOtherWhenMessage(String whenCondition, String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustBeGreaterThanOrEqualToOtherWhenMessage(String whenCondition, String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustBeLessThanOtherWhenMessage(String whenCondition, String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustBeLessThanOrEqualToOtherWhenMessage(String whenCondition, String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustBetweenOtherWhenMessage(
            String whenCondition, 
            String parameterName, 
            String minimumOp,
            String minimumParameterName,
            String maximumOp,
            String maximumParameterName);
    
    @I18N
    private static native String mustBeInstanceOfOtherWhenMessage(String whenCondition, String parameterName, String classParameterName);
    
    @I18N
    private static native String mustBeInstanceOfAllOfOtherWhenMessage(String whenCondition, String parameterName, String classesParameterName);
    
    @I18N
    private static native String mustBeInstanceOfAnyOfOtherWhenMessage(String whenCondition, String parameterName, String classesParameterName);
    
    @I18N
    private static native String mustNotBeInstanceOfOtherWhenMessage(String whenCondition, String parameterName, String classParameterName);
    
    @I18N
    private static native String mustNotBeInstanceOfAnyOfOtherWhenMessage(String whenCondition, String parameterName, String classesParameterName);
    
    @I18N
    private static native String mustBeCompatibleWithOtherWhenMessage(String whenCondition, String parameterName, String classParameterName);
    
    @I18N
    private static native String mustBeCompatibleWithAllOfOtherWhenMessage(String whenCondition, String parameterName, String classesParameterName);
    
    @I18N
    private static native String mustBeCompatibleWithAnyOfOtherWhenMessage(String whenCondition, String parameterName, String classesParameterName);
    
    @I18N
    private static native String mustNotBeCompatibleWithOtherWhenMessage(String whenCondition, String parameterName, String classParameterName);
    
    @I18N
    private static native String mustNotBeCompatibleWithAnyOfOtherWhenMessage(String whenCondition, String parameterName, String classesParameterName);
    
    @I18N
    private static native String mustBeAnyOfOtherWhenMessage(String whenCondition, String parameterName, String valueParameterName);
    
    @I18N
    private static native String mustNotBeAnyOfOtherWhenMessage(String whenCondition, String parameterName, String valueParameterName);

    @Deprecated
    protected Arguments() {
        throw new UnsupportedOperationException();
    }
}
