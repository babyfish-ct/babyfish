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
package org.babyfish.test.collection;

import java.util.Map.Entry;

import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.data.event.ModificationType;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class MALinkedHashMapAccessingTest {

    @Test
    public void testAccessing() {
        final StringBuilder builder = new StringBuilder();
        MAOrderedMap<String, String> map =
                new MALinkedHashMap<>(
                        ReplacementRule.NEW_REFERENCE_WIN, 
                        null, 
                        false, 
                        OrderAdjustMode.HEAD, 
                        OrderAdjustMode.NONE);
        map.put("A", "a");
        map.put("B", "b");
        map.put("C", "c");
        map.put("D", "d");
        map.put("E", "e");
        map.addMapElementListener(
                new MapElementListener<String, String>() {
                    @Override
                    public void modified(MapElementEvent<String, String> e) throws Throwable {
                        Assert.assertSame(ModificationType.REPLACE, e.getModificationType());
                        Assert.assertSame(e.getKey(PropertyVersion.DETACH), e.getKey(PropertyVersion.ATTACH));
                        Assert.assertSame(e.getValue(PropertyVersion.DETACH), e.getValue(PropertyVersion.ATTACH));
                        builder
                        .append('(')
                        .append(e.getKey(PropertyVersion.ATTACH))
                        .append(", ")
                        .append(e.getValue(PropertyVersion.ATTACH))
                        .append(')');
                    }
                });
        Assert.assertEquals("", builder.toString());
        assertMap(map, "A", "a", "B", "b", "C", "c", "D", "d", "E", "e");
        
        map.descendingMap().access("A");
        Assert.assertEquals("", builder.toString());
        assertMap(map, "A", "a", "B", "b", "C", "c", "D", "d", "E", "e");
        
        map.descendingMap().access("B");
        Assert.assertEquals("(B, b)", builder.toString());
        assertMap(map, "B", "b", "A", "a", "C", "c", "D", "d", "E", "e");
        
        map.descendingMap().access("C");
        Assert.assertEquals("(B, b)(C, c)", builder.toString());
        assertMap(map, "C", "c", "B", "b", "A", "a", "D", "d", "E", "e");
        
        map.descendingMap().access("D");
        Assert.assertEquals("(B, b)(C, c)(D, d)", builder.toString());
        assertMap(map, "D", "d", "C", "c", "B", "b", "A", "a", "E", "e");
        
        map.descendingMap().access("E");
        Assert.assertEquals("(B, b)(C, c)(D, d)(E, e)", builder.toString());
        assertMap(map, "E", "e", "D", "d", "C", "c", "B", "b", "A", "a");
    }
    
    private static void assertMap(XOrderedMap<String, String> map, String ... keyAndValues) {
        if (keyAndValues.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        Assert.assertEquals(keyAndValues.length / 2, map.size());
        int index = 0;
        for (Entry<String, String> entry : map.entrySet()) {
            Assert.assertEquals(entry.getKey(), keyAndValues[index++]);
            Assert.assertEquals(entry.getValue(), keyAndValues[index++]);
        }
        Assert.assertEquals(index, keyAndValues.length);
    }
}
