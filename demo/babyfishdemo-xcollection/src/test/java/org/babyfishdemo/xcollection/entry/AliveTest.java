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

import org.babyfish.collection.HashMap;
import org.babyfish.collection.XMap;
import org.junit.Test;

import junit.framework.Assert;

public class AliveTest {

    @Test
    public void testAlive() {
        
        XMap<String, String> map = new HashMap<>();
        map.put("One", "Alpha");
        XMap.XEntry<String, String> entry = map.entrySet().iterator().next();
        
        // The entry is alive before it's removed from map,
        // that means the entry is managed by the map, the map will be affected when this entry is changed
        Assert.assertTrue(entry.isAlive());
        
        // Remove this entry from this map
        map.entrySet().remove(entry);
        
        // The entry isn't alive after it's removed from map,
        // that means the entry is managed by the map, the map wont be affected when this entry is changed
        Assert.assertFalse(entry.isAlive());
    }
}
