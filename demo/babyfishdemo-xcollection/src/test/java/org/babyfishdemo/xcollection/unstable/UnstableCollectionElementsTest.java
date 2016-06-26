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

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XSet;
import org.babyfishdemo.xcollection.unstable.FullName;
import org.junit.Assert;
import org.junit.Test;
 
/**
 * @author Tao Chen
 * 
 * This test case demonstrates the most powerful functionality of "Babyfish XCollection Framework":
 *    Unstable Collection Elements.
 * 
 * In Java Collection Framework, Hash Structure and red-black tree requires the stable elements,
 * that means the data object cannot be changed after it has been added into set or added into map as key.
 * 
 * In BabyFish Collection Framework, you can change the data object even if it has been 
 * added into set or added into map as key. When the data object is changed, all the related set and map
 * will be adjusted automatically!
 * 
 * After the automatic adjusting
 * <ul>
 *    <li>For org.babyfish.collection.HashSet and org.babyfish.collection.HashMap, the new order is unknown</li>
 *  <li>For org.babyfish.collection.LinkedHashSet and org.babyfish.collection.LinkedHashMap, the new order is same with the old order</li>
 *  <li>For org.babyfish.collection.TreeSet and org.babyfish.collection.LinkedTreeMap, the collection looks like it has been sorted again</li>
 * </ul>
 */
public class UnstableCollectionElementsTest {
 
    @Test
    public void test() {
    
        /*
         * Prepare data
         */
        FullName jamesGosling = new FullName("james", "gosling");
        FullName joshBloch = new FullName("josh", "bloch");
        FullName gavinKing = new FullName("gavin", "king");
        
        XSet<FullName> firstNameLinkedHashSet = new LinkedHashSet<>(FullName.FIRST_NAME_EQUALITY_COMPARATOR);
        XMap<FullName, Void> firstNameLinkedHashMap = new LinkedHashMap<>(FullName.FIRST_NAME_EQUALITY_COMPARATOR);
        XSet<FullName> firstNameTreeSet = new TreeSet<>(FullName.FIRST_NAME_COMPARATOR);
        XMap<FullName, Void> firstNameTreeMap = new TreeMap<>(FullName.FIRST_NAME_COMPARATOR);
            
        XSet<FullName> lastNameLinkedHashSet = new LinkedHashSet<>(FullName.LAST_NAME_EQUALITY_COMPARATOR);
        XMap<FullName, Void> lastNameLinkedHashMap = new LinkedHashMap<>(FullName.LAST_NAME_EQUALITY_COMPARATOR);
        XSet<FullName> lastNameTreeSet = new TreeSet<>(FullName.LAST_NAME_COMPARATOR);
        XMap<FullName, Void> lastNameTreeMap = new TreeMap<>(FullName.LAST_NAME_COMPARATOR);
            
        XSet<FullName> fullNameLinkedHashSet = new LinkedHashSet<>(FullName.FULL_NAME_EQUALITY_COMPARATOR);
        XMap<FullName, Void> fullNameLinkedHashMap = new LinkedHashMap<>(FullName.FULL_NAME_EQUALITY_COMPARATOR);
        XSet<FullName> fullNameTreeSet = new TreeSet<>(FullName.FULL_NAME_COMPARATOR);
        XMap<FullName, Void> fullNameTreeMap = new TreeMap<>(FullName.FULL_NAME_COMPARATOR);
            
        firstNameLinkedHashSet.add(jamesGosling);
        firstNameLinkedHashSet.add(joshBloch);
        firstNameLinkedHashSet.add(gavinKing);
        firstNameLinkedHashMap.put(jamesGosling, null);
        firstNameLinkedHashMap.put(joshBloch, null);
        firstNameLinkedHashMap.put(gavinKing, null);
        firstNameTreeSet.add(jamesGosling);
        firstNameTreeSet.add(joshBloch);
        firstNameTreeSet.add(gavinKing);
        firstNameTreeMap.put(jamesGosling, null);
        firstNameTreeMap.put(joshBloch, null);
        firstNameTreeMap.put(gavinKing, null);
        
        lastNameLinkedHashSet.add(jamesGosling);
        lastNameLinkedHashSet.add(joshBloch);
        lastNameLinkedHashSet.add(gavinKing);
        lastNameLinkedHashMap.put(jamesGosling, null);
        lastNameLinkedHashMap.put(joshBloch, null);
        lastNameLinkedHashMap.put(gavinKing, null);
        lastNameTreeSet.add(jamesGosling);
        lastNameTreeSet.add(joshBloch);
        lastNameTreeSet.add(gavinKing);
        lastNameTreeMap.put(jamesGosling, null);
        lastNameTreeMap.put(joshBloch, null);
        lastNameTreeMap.put(gavinKing, null);
        
        fullNameLinkedHashSet.add(jamesGosling);
        fullNameLinkedHashSet.add(joshBloch);
        fullNameLinkedHashSet.add(gavinKing);
        fullNameLinkedHashMap.put(jamesGosling, null);
        fullNameLinkedHashMap.put(joshBloch, null);
        fullNameLinkedHashMap.put(gavinKing, null);
        fullNameTreeSet.add(jamesGosling);
        fullNameTreeSet.add(joshBloch);
        fullNameTreeSet.add(gavinKing);
        fullNameTreeMap.put(jamesGosling, null);
        fullNameTreeMap.put(joshBloch, null);
        fullNameTreeMap.put(gavinKing, null);
        
        {
            /*
             * Assert initialized values
             */
            assertNames(firstNameLinkedHashSet, "james gosling", "josh bloch", "gavin king");
            assertNames(firstNameLinkedHashMap.keySet(), "james gosling", "josh bloch", "gavin king");
            assertNames(firstNameTreeSet, "gavin king", "james gosling", "josh bloch");
            assertNames(firstNameTreeMap.keySet(), "gavin king", "james gosling", "josh bloch");
            
            assertNames(lastNameLinkedHashSet, "james gosling", "josh bloch", "gavin king");
            assertNames(lastNameLinkedHashMap.keySet(), "james gosling", "josh bloch", "gavin king");
            assertNames(lastNameTreeSet, "josh bloch", "james gosling", "gavin king");
            assertNames(lastNameTreeMap.keySet(), "josh bloch", "james gosling", "gavin king");
            
            assertNames(fullNameLinkedHashSet, "james gosling", "josh bloch", "gavin king");
            assertNames(fullNameLinkedHashMap.keySet(), "james gosling", "josh bloch", "gavin king");
            assertNames(fullNameTreeSet, "gavin king", "james gosling", "josh bloch");
            assertNames(fullNameTreeMap.keySet(), "gavin king", "james gosling", "josh bloch");
        }
        
        {
            /*
             * (1) Change the first name of jamesGosling to be "JAMES", the 
             * firstNameHashSet, firstNameHashSet, firstNameTreeSet, firstNameTreeMap,
             * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
             * will be adjusted automatically. 
             * 
             * The order of treeSet/treeMap based on firstName or fullName will be changed
             */
            jamesGosling.setFirstName("JAMES");
            
            assertNames(firstNameLinkedHashSet, "JAMES gosling", "josh bloch", "gavin king");
            assertNames(firstNameLinkedHashMap.keySet(), "JAMES gosling", "josh bloch", "gavin king");
            assertNames(firstNameTreeSet, "JAMES gosling", "gavin king", "josh bloch"); // Order changed
            assertNames(firstNameTreeMap.keySet(), "JAMES gosling", "gavin king", "josh bloch"); // Order changed
            
            assertNames(lastNameLinkedHashSet, "JAMES gosling", "josh bloch", "gavin king");
            assertNames(lastNameLinkedHashMap.keySet(), "JAMES gosling", "josh bloch", "gavin king");
            assertNames(lastNameTreeSet, "josh bloch", "JAMES gosling", "gavin king");
            assertNames(lastNameTreeMap.keySet(), "josh bloch", "JAMES gosling", "gavin king");
            
            assertNames(fullNameLinkedHashSet, "JAMES gosling", "josh bloch", "gavin king");
            assertNames(fullNameLinkedHashMap.keySet(), "JAMES gosling", "josh bloch", "gavin king");
            assertNames(fullNameTreeSet, "JAMES gosling", "gavin king", "josh bloch"); // Order changed
            assertNames(fullNameTreeMap.keySet(), "JAMES gosling", "gavin king", "josh bloch"); // Order changed
        }
        
        {
            /*
             * (2) Change the first name of joshBloch to be "JOSH", the 
             * firstNameHashSet, firstNameHashSet, firstNameTreeSet, firstNameTreeMap,
             * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
             * will be adjusted automatically. 
             *
             * The order of treeSet/treeMap based on firstName or fullName will be changed
             */
            joshBloch.setFirstName("JOSH");
            
            assertNames(firstNameLinkedHashSet, "JAMES gosling", "JOSH bloch", "gavin king");
            assertNames(firstNameLinkedHashMap.keySet(), "JAMES gosling", "JOSH bloch", "gavin king");
            assertNames(firstNameTreeSet, "JAMES gosling", "JOSH bloch", "gavin king"); // Order changed
            assertNames(firstNameTreeMap.keySet(), "JAMES gosling", "JOSH bloch", "gavin king"); // Order changed
            
            assertNames(lastNameLinkedHashSet, "JAMES gosling", "JOSH bloch", "gavin king");
            assertNames(lastNameLinkedHashMap.keySet(), "JAMES gosling", "JOSH bloch", "gavin king");
            assertNames(lastNameTreeSet, "JOSH bloch", "JAMES gosling", "gavin king");
            assertNames(lastNameTreeMap.keySet(), "JOSH bloch", "JAMES gosling", "gavin king");
            
            assertNames(fullNameLinkedHashSet, "JAMES gosling", "JOSH bloch", "gavin king");
            assertNames(fullNameLinkedHashMap.keySet(), "JAMES gosling", "JOSH bloch", "gavin king");
            assertNames(fullNameTreeSet, "JAMES gosling", "JOSH bloch", "gavin king"); // Order changed
            assertNames(fullNameTreeMap.keySet(), "JAMES gosling", "JOSH bloch", "gavin king"); // Order changed
        }
        
        {
            /*
             * (3) Change the first name of gavinKing to be "GAVIN", the 
             * firstNameHashSet, firstNameHashSet, firstNameTreeSet, firstNameTreeMap,
             * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
             * will be adjusted automatically. 
             *
             * The order of treeSet/treeMap based on firstName or fullName will be changed
             */
            gavinKing.setFirstName("GAVIN");
            
            assertNames(firstNameLinkedHashSet, "JAMES gosling", "JOSH bloch", "GAVIN king");
            assertNames(firstNameLinkedHashMap.keySet(), "JAMES gosling", "JOSH bloch", "GAVIN king");
            assertNames(firstNameTreeSet, "GAVIN king", "JAMES gosling", "JOSH bloch"); // Order changed
            assertNames(firstNameTreeMap.keySet(), "GAVIN king", "JAMES gosling", "JOSH bloch"); // Order changed
            
            assertNames(lastNameLinkedHashSet, "JAMES gosling", "JOSH bloch", "GAVIN king");
            assertNames(lastNameLinkedHashMap.keySet(), "JAMES gosling", "JOSH bloch", "GAVIN king");
            assertNames(lastNameTreeSet, "JOSH bloch", "JAMES gosling", "GAVIN king");
            assertNames(lastNameTreeMap.keySet(), "JOSH bloch", "JAMES gosling", "GAVIN king");
            
            assertNames(fullNameLinkedHashSet, "JAMES gosling", "JOSH bloch", "GAVIN king");
            assertNames(fullNameLinkedHashMap.keySet(), "JAMES gosling", "JOSH bloch", "GAVIN king");
            assertNames(fullNameTreeSet, "GAVIN king", "JAMES gosling", "JOSH bloch"); // Order changed
            assertNames(fullNameTreeMap.keySet(), "GAVIN king", "JAMES gosling", "JOSH bloch"); // Order changed
        }
        
        {
            /*
             * (4) Change the last name of jamesGosling to be "GOSLING", the 
             * lastNameHashSet, lastNameHashSet, lastNameTreeSet, lastNameTreeMap,
             * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
             * will be adjusted automatically. 
             * 
             * The order of treeSet/treeMap based on lastName will be changed
             */
            jamesGosling.setLastName("GOSLING");
            
            assertNames(firstNameLinkedHashSet, "JAMES GOSLING", "JOSH bloch", "GAVIN king");
            assertNames(firstNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH bloch", "GAVIN king");
            assertNames(firstNameTreeSet, "GAVIN king", "JAMES GOSLING", "JOSH bloch");
            assertNames(firstNameTreeMap.keySet(), "GAVIN king", "JAMES GOSLING", "JOSH bloch");
            
            assertNames(lastNameLinkedHashSet, "JAMES GOSLING", "JOSH bloch", "GAVIN king");
            assertNames(lastNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH bloch", "GAVIN king");
            assertNames(lastNameTreeSet, "JAMES GOSLING", "JOSH bloch", "GAVIN king"); // Order changed
            assertNames(lastNameTreeMap.keySet(), "JAMES GOSLING", "JOSH bloch", "GAVIN king"); // Order changed
            
            assertNames(fullNameLinkedHashSet, "JAMES GOSLING", "JOSH bloch", "GAVIN king");
            assertNames(fullNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH bloch", "GAVIN king");
            assertNames(fullNameTreeSet, "GAVIN king", "JAMES GOSLING", "JOSH bloch");
            assertNames(fullNameTreeMap.keySet(), "GAVIN king", "JAMES GOSLING", "JOSH bloch");
        }
        
        {
            /*
             * (5) Change the last name of joshBloch to be "BLOCH", the 
             * lastNameHashSet, lastNameHashSet, lastNameTreeSet, lastNameTreeMap,
             * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
             * will be adjusted automatically. 
             * 
             * The order of treeSet/treeMap based on lastName will be changed
             */
            joshBloch.setLastName("BLOCH");
            
            assertNames(firstNameLinkedHashSet, "JAMES GOSLING", "JOSH BLOCH", "GAVIN king");
            assertNames(firstNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH BLOCH", "GAVIN king");
            assertNames(firstNameTreeSet, "GAVIN king", "JAMES GOSLING", "JOSH BLOCH");
            assertNames(firstNameTreeMap.keySet(), "GAVIN king", "JAMES GOSLING", "JOSH BLOCH");
            
            assertNames(lastNameLinkedHashSet, "JAMES GOSLING", "JOSH BLOCH", "GAVIN king");
            assertNames(lastNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH BLOCH", "GAVIN king");
            assertNames(lastNameTreeSet, "JOSH BLOCH", "JAMES GOSLING", "GAVIN king"); // Order changed
            assertNames(lastNameTreeMap.keySet(), "JOSH BLOCH", "JAMES GOSLING", "GAVIN king"); // Order changed
            
            assertNames(fullNameLinkedHashSet, "JAMES GOSLING", "JOSH BLOCH", "GAVIN king");
            assertNames(fullNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH BLOCH", "GAVIN king");
            assertNames(fullNameTreeSet, "GAVIN king", "JAMES GOSLING", "JOSH BLOCH");
            assertNames(fullNameTreeMap.keySet(), "GAVIN king", "JAMES GOSLING", "JOSH BLOCH");
        }
        
        {
            /*
             * (6) Change the last name of gavinKing to be "KING", the 
             * lastNameHashSet, lastNameHashSet, lastNameTreeSet, lastNameTreeMap,
             * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
             * will be adjusted automatically. 
             */
            gavinKing.setLastName("KING");
            
            assertNames(firstNameLinkedHashSet, "JAMES GOSLING", "JOSH BLOCH", "GAVIN KING");
            assertNames(firstNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH BLOCH", "GAVIN KING");
            assertNames(firstNameTreeSet, "GAVIN KING", "JAMES GOSLING", "JOSH BLOCH");
            assertNames(firstNameTreeMap.keySet(), "GAVIN KING", "JAMES GOSLING", "JOSH BLOCH");
            
            assertNames(lastNameLinkedHashSet, "JAMES GOSLING", "JOSH BLOCH", "GAVIN KING");
            assertNames(lastNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH BLOCH", "GAVIN KING");
            assertNames(lastNameTreeSet, "JOSH BLOCH", "JAMES GOSLING", "GAVIN KING");
            assertNames(lastNameTreeMap.keySet(), "JOSH BLOCH", "JAMES GOSLING", "GAVIN KING");
            
            assertNames(fullNameLinkedHashSet, "JAMES GOSLING", "JOSH BLOCH", "GAVIN KING");
            assertNames(fullNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH BLOCH", "GAVIN KING");
            assertNames(fullNameTreeSet, "GAVIN KING", "JAMES GOSLING", "JOSH BLOCH");
            assertNames(fullNameTreeMap.keySet(), "GAVIN KING", "JAMES GOSLING", "JOSH BLOCH");
        }
    }
    
    private static void assertNames(Set<FullName> fullNameSetWithParticularOrder, String ... names) {
        Assert.assertEquals(names.length, fullNameSetWithParticularOrder.size());
        int index = 0;
        for (FullName fullName : fullNameSetWithParticularOrder) {
            Assert.assertEquals(names[index++], fullName.getFirstName() + " " + fullName.getLastName());
        }
    }
}
