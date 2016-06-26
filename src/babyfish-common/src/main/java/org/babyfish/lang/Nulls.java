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

import java.util.Collection;
import java.util.Map;

/**
 * @author Tao Chen
 */
public class Nulls {

    @Deprecated
    protected Nulls() {
        throw new UnsupportedOperationException();
    }
    
    public static int hashCode(Object o) {
        return o == null ? 0 : o.hashCode();
    }
    
    public static boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }
    
    public static <T extends Comparable<T>> int compare(T o1, T o2) {
        Integer preCmp = preCompare(o1, o2);
        if (preCmp != null) {
            return preCmp;
        }
        return o1.compareTo(o2);
    }
    
    @SuppressWarnings("unchecked")
    public static int compare(Object o1, Object o2) {
        Integer preCmp = preCompare(o1, o2);
        if (preCmp != null) {
            return preCmp;
        }
        return ((Comparable<Object>)o1).compareTo(o2);
    }
    
    public static <T extends Comparable<T>> int compare(T o1, T o2, boolean nullsLast) {
        Integer preCmp = preCompare(o1, o2, nullsLast);
        if (preCmp != null) {
            return preCmp;
        }
        return o1.compareTo(o2);
    }
    
    @SuppressWarnings("unchecked")
    public static int compare(Object o1, Object o2, boolean nullsLast) {
        Integer preCmp = preCompare(o1, o2, nullsLast);
        if (preCmp != null) {
            return preCmp;
        }
        return ((Comparable<Object>)o1).compareTo(o2);
    }
    
    public static Integer preCompare(Object o1, Object o2) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return +1;
        }
        return null;
    }
    
    public static Integer preCompare(Object o1, Object o2, boolean nullsLast) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return nullsLast ? +1 : -1;
        }
        if (o2 == null) {
            return nullsLast ? -1 : +1;
        }
        return null;
    }
    
    public static String toString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString();
    }
    
    public static String toString(Object o, boolean nullsEmpty) {
        if (o == null) {
            return nullsEmpty ? "" : "null";
        }
        return o.toString();
    }
    
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
    
    public static boolean isNullOrEmpty(boolean[] a) {
        return a == null || a.length == 0; 
    }
    
    public static boolean isNullOrEmpty(char[] a) {
        return a == null || a.length == 0; 
    }
    
    public static boolean isNullOrEmpty(byte[] a) {
        return a == null || a.length == 0; 
    }
    
    public static boolean isNullOrEmpty(short[] a) {
        return a == null || a.length == 0; 
    }
    
    public static boolean isNullOrEmpty(int[] a) {
        return a == null || a.length == 0; 
    }
    
    public static boolean isNullOrEmpty(long[] a) {
        return a == null || a.length == 0; 
    }
    
    public static boolean isNullOrEmpty(float[] a) {
        return a == null || a.length == 0; 
    }
    
    public static boolean isNullOrEmpty(double[] a) {
        return a == null || a.length == 0; 
    }
    
    public static <T> boolean isNullOrEmpty(T[] a) {
        return a == null || a.length == 0; 
    }
    
    public static boolean isNullOrEmpty(Iterable<?> i) {
        if (i instanceof Collection<?>) {
            return isNullOrEmpty((Collection<?>)i);
        }
        return i == null || i.iterator().hasNext() == false;
    }
    
    public static boolean isNullOrEmpty(Collection<?> c) {
        return c == null || c.isEmpty(); 
    }
    
    public static boolean isNullOrEmpty(Map<?, ?> m) {
        return m == null || m.isEmpty(); 
    }
    
    public static String emptyToNull(String s) {
        return s != null && s.isEmpty() ? null : s;
    }
    
    public static boolean[] emptyToNull(boolean[] a) {
        return a != null && a.length == 0 ? null : a;
    }
    
    public static char[] emptyToNull(char[] a) {
        return a != null && a.length == 0 ? null : a;
    }
    
    public static byte[] emptyToNull(byte[] a) {
        return a != null && a.length == 0 ? null : a;
    }
    
    public static short[] emptyToNull(short[] a) {
        return a != null && a.length == 0 ? null : a;
    }
    
    public static int[] emptyToNull(int[] a) {
        return a != null && a.length == 0 ? null : a;
    }
    
    public static long[] emptyToNull(long[] a) {
        return a != null && a.length == 0 ? null : a;
    }
    
    public static float[] emptyToNull(float[] a) {
        return a != null && a.length == 0 ? null : a;
    }
    
    public static double[] emptyToNull(double[] a) {
        return a != null && a.length == 0 ? null : a;
    }
    
    public static <E, I extends Iterable<E>> I emptyToNull(I i) {
        if (i instanceof Collection<?>) {
            return ((Collection<?>)i).isEmpty() ? null : i;
        }
        return i != null && i.iterator().hasNext() == false ? null : i;
    }
    
    public static <E, C extends Collection<E>> C emptyToNull(C c) {
        return c != null && c.isEmpty() ? null : c;
    }
    
    public static <K, V, M extends Map<K, V>> M emptyToNull(M m) {
        return m != null && m.isEmpty() ? null : m;
    }
    
    public static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
