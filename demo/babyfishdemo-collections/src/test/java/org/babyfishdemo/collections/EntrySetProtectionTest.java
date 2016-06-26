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
package org.babyfishdemo.collections;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.Assert;

import org.babyfish.collection.MACollections;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class EntrySetProtectionTest {

    @Test
    public void testProtectEntrySet() {
        
        
        /*
         * Test java.util.Collections.unmodifiableSet
         * and java.util.HashMap.entrySet
         */
        {
            Map<String, String> map = new java.util.HashMap<>();
            map.put("Man's best friend", "Dog");
            Set<Entry<String, String>> unmodifiableEntrySet = Collections.unmodifiableSet(map.entrySet());
            
            // The set is unmodifiable, but the entry can still be changed
            unmodifiableEntrySet.iterator().next().setValue("DOG");
            Assert.assertEquals("DOG", map.get("Man's best friend"));
        }
        
        /*
         * Test org.babyfish.collection.MACollections.unmodifiable
         * and org.babyfish.collection.HashMap.entrySet
         */
        {
            Map<String, String> map = new org.babyfish.collection.HashMap<>();
            map.put("Man's best friend", "Dog");
            Set<Entry<String, String>> unmodifiableEntrySet = MACollections.unmodifiable(map.entrySet());
            
            // The unmodifiable set is XMap.XEntrySetView, 
            // so its element can not be changed too
            try {
                unmodifiableEntrySet.iterator().next().setValue("DOG");
                Assert.fail(UnsupportedOperationException.class.getName() + " is expected");
            } catch (UnsupportedOperationException ex) {
                
            }
        }
    }
}
