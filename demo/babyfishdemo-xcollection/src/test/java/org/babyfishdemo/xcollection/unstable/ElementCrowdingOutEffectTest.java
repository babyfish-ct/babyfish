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
package org.babyfishdemo.xcollection.unstable;

import java.util.Set;

import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.ReplacementRule;
import org.junit.Test;

import junit.framework.Assert;

public class ElementCrowdingOutEffectTest {

    @Test
    public void testByNewReferenceWinMode() {
        FullName fn1 = new FullName("james", "gosling");
        FullName fn2 = new FullName("josh", "bloch");
        FullName fn3 = new FullName("gavin", "king");
        Set<FullName> set = new LinkedHashSet<>(
                ReplacementRule.NEW_REFERENCE_WIN, 
                FullName.FULL_NAME_EQUALITY_COMPARATOR
        );
        set.addAll(MACollections.wrap(fn1, fn2, fn3));
        assertReferences(set, fn1, fn2, fn3);
        
        fn2.setFirstName("gavin");
        fn2.setLastName("king");
        assertReferences(set, fn1, fn2); //fn3 is removed automactially
    }
    
    @Test
    public void testByOldReferenceWinMode() {
        FullName fn1 = new FullName("james", "gosling");
        FullName fn2 = new FullName("josh", "bloch");
        FullName fn3 = new FullName("gavin", "king");
        Set<FullName> set = new LinkedHashSet<>(
                ReplacementRule.OLD_REFERENCE_WIN,
                FullName.FULL_NAME_EQUALITY_COMPARATOR
        );
        set.addAll(MACollections.wrap(fn1, fn2, fn3));
        assertReferences(set, fn1, fn2, fn3);
        
        fn2.setFirstName("gavin");
        fn2.setLastName("king");
        assertReferences(set, fn1, fn3); //fn2 is removed automactially
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertReferences(Set<E> set, E ... elements) {
        Assert.assertEquals(elements.length, set.size());
        int index = 0;
        for (E e : set) {
            // assertSame, not assertEquals
            Assert.assertSame(elements[index++], e);
        }
    }
}
