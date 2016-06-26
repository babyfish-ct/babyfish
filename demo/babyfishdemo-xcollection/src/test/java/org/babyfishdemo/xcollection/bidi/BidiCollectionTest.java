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
package org.babyfishdemo.xcollection.bidi;

import java.util.List;
import java.util.Map;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.BidiType;
import org.babyfish.collection.LinkedHashMap;
import org.junit.Test;

import junit.framework.Assert;

public class BidiCollectionTest {

    @Test
    public void testBidiMap() {
        
        Map<String, String> map = new LinkedHashMap<>(BidiType.ALL_VALUES);
        
        map.put("key1", "value");
        Assert.assertEquals("{key1=value}", map.toString());
        
        /*
         * bidi map does not allow duplicated values,
         * so "{key1=value}" will be removed automatically 
         * after "{key2=value}" is inserted
         */
        map.put("key2", "value");
        Assert.assertEquals("{key2=value}", map.toString());
    }
    
    @Test
    public void testBidiList() {
        
        List<String> list = new ArrayList<>(BidiType.ALL_VALUES);
        String a1 = "a";
        String a2 = new String("a");
        Assert.assertNotSame(a1, a2);
        
        list.add(a1);
        Assert.assertEquals("[a]", list.toString());
        Assert.assertSame(a1, list.get(0));
        
        /*
         * bidi list does not allow duplicated elements,
         * so a1 will be removed automatically after a2 is added
         */
        list.add(a2);
        Assert.assertEquals("[a]", list.toString());
        Assert.assertSame(a2, list.get(0));
    }
}
