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
package org.babyfish.lang.internal;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Tao Chen
 */
public class I18NUtils {
    
    private static final String COMMA = ", ";
    
    private static final String COLON = " : ";
    
    private static final String LIST_BEGIN = "[ ";
    
    private static final String LIST_END = " ]";
    
    private static final String MAP_BEGIN = "{ ";
    
    private static final String MAP_END = " }";
    
    private static final String EMPTY_LIST = "[]";
    
    private static final String EMPTY_MAP = "{}";

    public static String toString(Object o) {
        if (o == null) {
            return "null";
        }
        if (o instanceof Class<?>) {
            return ((Class<?>)o).getName();
        }
        if (!(o instanceof Iterable<?>) && !(o instanceof Map<?, ?>) && !(o.getClass().isArray())) {
            return o.toString();
        }
        StringBuilder builder = new StringBuilder();
        appendTo(builder, o);
        return builder.toString();
    }
    
    private static void appendTo(StringBuilder builder, Object o) {
        if (o == null) {
            builder.append("null");
        } else if (o instanceof Class<?>) {
            builder.append(((Class<?>)o).getName());
        } else if (o instanceof Iterable<?>) {
            appendTo(builder, (Iterable<?>)o);
        } else if (o instanceof Map<?, ?>) {
            appendTo(builder, (Map<?, ?>)o);
        } else if (o.getClass().isArray()) {
            if (o instanceof boolean[]) {
                appendTo(builder, (boolean[])o);
            } else if (o instanceof char[]) {
                appendTo(builder, (char[])o);
            } else if (o instanceof byte[]) {
                appendTo(builder, (byte[])o);
            } else if (o instanceof short[]) {
                appendTo(builder, (short[])o);
            } else if (o instanceof int[]) {
                appendTo(builder, (int[])o);
            } else if (o instanceof long[]) {
                appendTo(builder, (long[])o);
            } else if (o instanceof float[]) {
                appendTo(builder, (float[])o);
            } else if (o instanceof double[]) {
                appendTo(builder, (double[])o);
            } else {
                appendObjectArrayTo(builder, o);
            }
        } else{
            builder.append(o.toString());
        }
    }
    
    private static void appendTo(StringBuilder builder, Iterable<?> iterable) {
        if (iterable instanceof Collection<?> && ((Collection<?>)iterable).isEmpty()) {
            builder.append(EMPTY_LIST);
        } else if (!iterable.iterator().hasNext()) {
            builder.append(EMPTY_LIST);
        } else {
            builder.append(LIST_BEGIN);
            boolean addComma = false;
            for (Object o : iterable) {
                if (addComma) {
                    builder.append(COMMA);
                } else {
                    addComma = true;
                }
                appendTo(builder, o);
            }
            builder.append(LIST_END);
        }
    }
    
    private static void appendTo(StringBuilder builder, Map<?, ?> map) {
        if (!map.isEmpty()) {
            builder.append(EMPTY_MAP);
        } else {
            builder.append(MAP_BEGIN);
            boolean addComma = false;
            for (Entry<?, ?> e : map.entrySet()) {
                if (addComma) {
                    builder.append(COMMA);
                } else {
                    addComma = true;
                }
                appendTo(builder, e.getKey());
                builder.append(COLON);
                appendTo(builder, e.getValue());
            }
            builder.append(MAP_END);
        }
    }
    
    private static void appendTo(StringBuilder builder, boolean[] arr) {
        if (arr.length == 0) {
            builder.append(EMPTY_LIST);
        } else {
            builder.append(LIST_BEGIN);
            boolean addComma = false;
            for (boolean e : arr) {
                if (addComma) {
                    builder.append(COMMA);
                } else {
                    addComma = true;
                }
                builder.append(e);
            }
            builder.append(LIST_END);
        }
    }
    
    private static void appendTo(StringBuilder builder, char[] arr) {
        if (arr.length == 0) {
            builder.append(EMPTY_LIST);
        } else {
            builder.append(LIST_BEGIN);
            boolean addComma = false;
            for (char e : arr) {
                if (addComma) {
                    builder.append(COMMA);
                } else {
                    addComma = true;
                }
                builder.append(e);
            }
            builder.append(LIST_END);
        }
    }
    
    private static void appendTo(StringBuilder builder, byte[] arr) {
        if (arr.length == 0) {
            builder.append(EMPTY_LIST);
        } else {
            builder.append(LIST_BEGIN);
            boolean addComma = false;
            for (byte e : arr) {
                if (addComma) {
                    builder.append(COMMA);
                } else {
                    addComma = true;
                }
                builder.append(e);
            }
            builder.append(LIST_END);
        }
    }
    
    private static void appendTo(StringBuilder builder, short[] arr) {
        if (arr.length == 0) {
            builder.append(EMPTY_LIST);
        } else {
            builder.append(LIST_BEGIN);
            boolean addComma = false;
            for (short e : arr) {
                if (addComma) {
                    builder.append(COMMA);
                } else {
                    addComma = true;
                }
                builder.append(e);
            }
            builder.append(LIST_END);
        }
    }
    
    private static void appendTo(StringBuilder builder, int[] arr) {
        if (arr.length == 0) {
            builder.append(EMPTY_LIST);
        } else {
            builder.append(LIST_BEGIN);
            boolean addComma = false;
            for (int e : arr) {
                if (addComma) {
                    builder.append(COMMA);
                } else {
                    addComma = true;
                }
                builder.append(e);
            }
            builder.append(LIST_END);
        }
    }
    
    private static void appendTo(StringBuilder builder, long[] arr) {
        if (arr.length == 0) {
            builder.append(EMPTY_LIST);
        } else {
            builder.append(LIST_BEGIN);
            boolean addComma = false;
            for (long e : arr) {
                if (addComma) {
                    builder.append(COMMA);
                } else {
                    addComma = true;
                }
                builder.append(e);
            }
            builder.append(LIST_END);
        }
    }
    
    private static void appendTo(StringBuilder builder, float[] arr) {
        if (arr.length == 0) {
            builder.append(EMPTY_LIST);
        } else {
            builder.append(LIST_BEGIN);
            boolean addComma = false;
            for (float e : arr) {
                if (addComma) {
                    builder.append(COMMA);
                } else {
                    addComma = true;
                }
                builder.append(e);
            }
            builder.append(LIST_END);
        }
    }
    
    private static void appendTo(StringBuilder builder, double[] arr) {
        if (arr.length == 0) {
            builder.append(EMPTY_LIST);
        } else {
            builder.append(LIST_BEGIN);
            boolean addComma = false;
            for (double e : arr) {
                if (addComma) {
                    builder.append(COMMA);
                } else {
                    addComma = true;
                }
                builder.append(e);
            }
            builder.append(LIST_END);
        }
    }
    
    private static void appendObjectArrayTo(StringBuilder builder, Object arr) {
        int len = Array.getLength(arr);
        if (len == 0) {
            builder.append(EMPTY_LIST);
        } else {
            builder.append(LIST_BEGIN);
            boolean addComma = false;
            for (int i = 0; i < len; i++) {
                if (addComma) {
                    builder.append(COMMA);
                } else {
                    addComma = true;
                }
                builder.append(Array.get(arr, i));
            }
            builder.append(LIST_END);
        }
    }
    
    private I18NUtils() {
        throw new UnsupportedOperationException("Don't invoke this method by java reflection");
    }
}
