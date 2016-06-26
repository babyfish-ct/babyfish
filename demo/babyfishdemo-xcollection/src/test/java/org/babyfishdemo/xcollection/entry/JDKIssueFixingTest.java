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
package org.babyfishdemo.xcollection.entry;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import junit.framework.Assert;
 
/**
 * @author Tao Chen
 */
public class JDKIssueFixingTest {

    // Right behavior of java.util.HashMap and org.babyfish.collection.TreeMap
    private static final String RIGHT_LOG = 
            "The middle entry is '2=one'.\n" +
            "map.remove(2); //remove the middleEntry\n" +
            "The middleEntry is '2=one' after it is removed.\n" +
            "middleEntry.setValue('x');\n" +
            "After middleEntry.setValue('x'), the map is '{1=one, 3=three}'.\n";
    
    
    // Wrong behavior of java.util.TreeMap
    private static final String WRONG_LOG = 
            "The middle entry is '2=one'.\n" +
            "map.remove(2); //remove the middleEntry\n" +
            "The middleEntry is '3=three' after it is removed.\n" +
            "middleEntry.setValue('x');\n" +
            "After middleEntry.setValue('x'), the map is '{1=one, 3=x}'.\n";
   
    @Test
    public void test() {
        /*
         * java.util.TreeMap returns the wrong result
         */
        Assert.assertEquals(
                WRONG_LOG, 
                getEntryBehaviorLog(new java.util.TreeMap<Integer, String>())
        );
        
        /*
         * java.util.HashMap
         * and
         * org.babyfish.collection.TreeMap return right result.
         */
        Assert.assertEquals(
                RIGHT_LOG, 
                getEntryBehaviorLog(new java.util.HashMap<Integer, String>())
        );
        Assert.assertEquals(
                RIGHT_LOG, 
                getEntryBehaviorLog(new org.babyfish.collection.TreeMap<Integer, String>())
        );
    }
 
    private static String getEntryBehaviorLog(Map<Integer, String> map) {
    
        StringBuilder builder = new StringBuilder();
        
        map.put(1, "one");
        map.put(2, "one");
        map.put(3, "three");
        Entry<Integer, String> middleEntry = null;
        for (Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getKey().intValue() == 2) {
                middleEntry = entry;
                break;
            }
        }
        builder.append("The middle entry is '" + middleEntry + "'.\n");
        
        builder.append("map.remove(2); //remove the middleEntry\n");
        map.remove(2);
        builder.append(
                "The middleEntry is '" + 
                middleEntry + 
                "' after it is removed.\n");
        
        builder.append("middleEntry.setValue('x');\n");
        middleEntry.setValue("x");
        builder.append(
            "After middleEntry.setValue('x'), the map is '" + 
            map + 
            "'.\n"
        );
        
        return builder.toString();
    }
}
