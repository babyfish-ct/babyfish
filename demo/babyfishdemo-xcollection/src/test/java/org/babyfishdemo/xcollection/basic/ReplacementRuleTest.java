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

import java.util.Set;

import org.babyfish.collection.HashSet;
import org.babyfish.collection.ReplacementRule;
import org.junit.Test;

import junit.framework.Assert;

public class ReplacementRuleTest {

    @Test
    public void testNewReferenceWin() {
        
        Set<String> set = new HashSet<>(ReplacementRule.NEW_REFERENCE_WIN); // This argument can be deleted because it's default value
        
        String a1 = "a", a2 = new String(a1);
        Assert.assertNotSame(a1, a2);
        
        Assert.assertTrue(set.add(a1)); // "a" doesn't exists, return true
        Assert.assertFalse(set.add(a2)); // "a" doesn't exists, return false
        
        // Be different with Java collection, the collection element has been replaced after the second adding
        Assert.assertSame(a2, set.iterator().next()); 
    }
    
    @Test
    public void testOldReferenceWin() {
        
        Set<String> set = new HashSet<>(ReplacementRule.OLD_REFERENCE_WIN);
        
        String a1 = "a", a2 = new String(a1);
        Assert.assertNotSame(a1, a2);
        
        Assert.assertTrue(set.add(a1)); // "a" doesn't exists, return true
        Assert.assertFalse(set.add(a2)); // "a" doesn't exists, return false
        
        // Be same with Java collection, the collection element has not been replaced after the second adding
        Assert.assertSame(a1, set.iterator().next()); 
    }
}
