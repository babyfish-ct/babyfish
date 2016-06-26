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
package org.babyfishdemo.xcollection.basic;

import java.util.Map;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.junit.Test;

import junit.framework.Assert;

public class EqualityComparatorTest {

    @Test
    public void testBuiltInEqualityComaprator() {
        /*
         * java.util.LinkedHashMap can keep the order of keys
         * java.util.IdentityHashMap does not uses the methods "hashCode" and "equals" of keys, but uses "System.identityHashCode" and "=="
         * 
         * So, what is "java.util.LinkedHashMap" + "java.util.IdentityHashMap" ?
         * 
         * Haha, here it is
         */
        Map<String, String> map = new LinkedHashMap<>(
                ReferenceEqualityComparator.<String>getInstance(),
                ReferenceEqualityComparator.<String>getInstance()
        );
        
        String a1 = "a", a2 = new String(a1);
        Assert.assertNotSame(a1, a2);
        
        map.put(a1, a2);
        map.put(a2, a2);
        Assert.assertEquals("{a=a, a=a}", map.toString());
    }
    
    @Test
    public void testCustomEqualityComparator() {
        EqualityComparator<String> insensitiveEqualityComparator = 
                new EqualityComparator<String>() {
                    @Override
                    public int hashCode(String o) {
                        return o.toUpperCase().hashCode();
                    }
                    @Override
                    public boolean equals(String o1, String o2) {
                        return o1.equalsIgnoreCase(o2);
                    }
                };
        Map<String, String> map = new LinkedHashMap<>(insensitiveEqualityComparator, (EqualityComparator<String>)null);
        
        map.put("a", "a");
        Assert.assertEquals("a", map.put("A", "A")); // "A" is conflict with "a"
        Assert.assertEquals("{A=A}", map.toString());
    }
}
