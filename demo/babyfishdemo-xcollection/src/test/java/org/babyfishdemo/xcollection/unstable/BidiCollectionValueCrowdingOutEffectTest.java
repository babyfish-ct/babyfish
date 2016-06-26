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

import java.util.List;
import java.util.Map;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.BidiType;
import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.LinkedHashMap;
import org.junit.Test;

import junit.framework.Assert;

public class BidiCollectionValueCrowdingOutEffectTest {

    @Test
    public void testBidiMap() {
        Map<String, FullName> map = new LinkedHashMap<>(
                BidiType.ALL_VALUES, 
                (EqualityComparator<String>)null, //key comparator
                FullName.FIRST_NAME_EQUALITY_COMPARATOR
        );
        FullName jim = new FullName("Jim", null);
        FullName mary = new FullName("Mary", null);
        map.put("first", jim);
        map.put("second", mary);
        Assert.assertEquals(
                "{"
                + "first={ firstName: 'Jim', lastName: 'null' }, "
                + "second={ firstName: 'Mary', lastName: 'null' }"
                + "}", 
                map.toString()
        );
        
        /*
         * This map supports unstable values because
         * (1) It's is bidi map.
         * (2) Its value comparator is "FullName.FIRST_NAME_EQUALITY_COMPARATOR".
         */
        mary.setFirstName("Jim");
        
        Assert.assertEquals(
                "{"
                + "second={ firstName: 'Jim', lastName: 'null' }"
                + "}", 
                map.toString()
        );
        Assert.assertSame(mary, map.values().iterator().next());
    }
    
    @Test
    public void testBidiList() {
        List<FullName> list = new ArrayList<>(BidiType.ALL_VALUES, FullName.FIRST_NAME_EQUALITY_COMPARATOR);
        FullName jim = new FullName("Jim", null);
        FullName mary = new FullName("Mary", null);
        list.add(jim);
        list.add(mary);
        Assert.assertEquals(
                "["
                + "{ firstName: 'Jim', lastName: 'null' }, "
                + "{ firstName: 'Mary', lastName: 'null' }"
                + "]", 
                list.toString()
        );
        
        /*
         * This list supports unstable elements because
         * (1) It's is bidi list.
         * (2) Its comparator is "FullName.FIRST_NAME_EQUALITY_COMPARATOR".
         */
        mary.setFirstName("Jim");
        
        Assert.assertEquals(
                "["
                + "{ firstName: 'Jim', lastName: 'null' }"
                + "]", 
                list.toString()
        );
        Assert.assertSame(mary, list.iterator().next());
    }
}
