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

import java.lang.reflect.Field;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.XNavigableMap;
import org.babyfish.collection.XNavigableMap.XNavigableMapView;
import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.XNavigableSet.XNavigableSetView;
import org.babyfish.collection.spi.AbstractMANavigableMap;
import org.babyfish.collection.spi.AbstractMANavigableSet;
import org.babyfish.collection.spi.base.NavigableBaseEntries;
import org.babyfish.collection.spi.base.NavigableRange;
import org.babyfish.collection.spi.base.RedBlackTreeEntries;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class NavigableRangeTest {
    
    private static final Field PARENT_SET = 
            fieldOf(classOf(AbstractMANavigableSet.class, "AbstractSubSetImpl"), "parentSet");
    
    private static final Field PARENT_MAP = 
            fieldOf(classOf(AbstractMANavigableMap.class, "AbstractSubMapImpl"), "parentMap");
    
    @Test
    public void testDescendingRange() {
        NavigableBaseEntries<String, String> baseEntries = 
                new RedBlackTreeEntries<>(
                        BidiType.NONE,
                        ReplacementRule.OLD_REFERENCE_WIN, 
                        null, 
                        null
                );
        NavigableBaseEntries<String, String> baseEntries2 = baseEntries.descendingEntries();
        NavigableBaseEntries<String, String> baseEntries3 = baseEntries2.descendingEntries();
        NavigableBaseEntries<String, String> baseEntries4 = 
                baseEntries3
                .subEntries(true, "C", true, true, "F", false)
                .descendingEntries();
        
        Assert.assertNull(baseEntries.range());
        
        Assert.assertSame(MACollections.reverseOrder(), baseEntries2.range().comparator(false));
        Assert.assertNull(baseEntries2.range().comparator(true));
        Assert.assertTrue(baseEntries2.range().descending());
        Assert.assertFalse(baseEntries2.range().hasFrom(false));
        Assert.assertFalse(baseEntries2.range().hasTo(false));
        
        Assert.assertNull(baseEntries3.range().comparator(false));
        Assert.assertNull(baseEntries3.range().comparator(true));
        Assert.assertFalse(baseEntries3.range().descending());
        Assert.assertFalse(baseEntries3.range().hasFrom(false));
        Assert.assertFalse(baseEntries3.range().hasTo(false));
        
        Assert.assertSame(MACollections.reverseOrder(), baseEntries4.range().comparator(false));
        Assert.assertNull(baseEntries4.range().comparator(true));
        Assert.assertTrue(baseEntries4.range().descending());
        Assert.assertTrue(baseEntries4.range().hasFrom(false));
        Assert.assertEquals("F", baseEntries4.range().from(false));
        Assert.assertEquals("C", baseEntries4.range().from(true));
        Assert.assertFalse(baseEntries4.range().fromInclusive(false));
        Assert.assertTrue(baseEntries4.range().fromInclusive(true));
        Assert.assertEquals("C", baseEntries4.range().to(false));
        Assert.assertEquals("F", baseEntries4.range().to(true));
        Assert.assertTrue(baseEntries4.range().toInclusive(false));
        Assert.assertFalse(baseEntries4.range().toInclusive(true));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFromGreaterThanTo() {
        NavigableBaseEntries<String, String> baseEntries = 
                new RedBlackTreeEntries<>(
                        BidiType.NONE,
                        ReplacementRule.OLD_REFERENCE_WIN, 
                        null, 
                        null);
        baseEntries.descendingEntries().subEntries(true, "C", true, true, "F", false);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFromOutOfRange() {
        NavigableBaseEntries<String, String> baseEntries = 
                new RedBlackTreeEntries<>(
                        BidiType.NONE,
                        ReplacementRule.OLD_REFERENCE_WIN, 
                        null, 
                        null
                );
        baseEntries
        .descendingEntries()
        .subEntries(true, "F", false, true, "C", true)
        .descendingEntries()
        .subEntries(true, "B", false, false, null, false);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testToOutOfRange() {
        NavigableBaseEntries<String, String> baseEntries = 
                new RedBlackTreeEntries<>(
                        BidiType.NONE,
                        ReplacementRule.OLD_REFERENCE_WIN, 
                        null, 
                        null
                );
        baseEntries
        .descendingEntries()
        .subEntries(true, "F", false, true, "C", true)
        .descendingEntries()
        .subEntries(false, null, false, true, "F", false);
    }
    
    @Test
    public void testSmallerInclusive() {
        NavigableBaseEntries<String, String> baseEntries = 
                new RedBlackTreeEntries<>(
                        BidiType.NONE,
                        ReplacementRule.OLD_REFERENCE_WIN, 
                        null, 
                        null
                );
        baseEntries =
                baseEntries
                .descendingEntries()
                .subEntries(true, "F", true, true, "C", true)
                .descendingEntries()
                .subEntries(true, "C", false, true, "F", false)
                .descendingEntries();
        Assert.assertSame(MACollections.reverseOrder(), baseEntries.range().comparator(false));
        Assert.assertNull(baseEntries.range().comparator(true));
        Assert.assertTrue(baseEntries.range().hasFrom(false));
        Assert.assertTrue(baseEntries.range().hasFrom(true));
        Assert.assertEquals("F", baseEntries.range().from(false));
        Assert.assertEquals("C", baseEntries.range().from(true));
        Assert.assertFalse(baseEntries.range().fromInclusive(false));
        Assert.assertFalse(baseEntries.range().fromInclusive(true));
        Assert.assertTrue(baseEntries.range().hasTo(false));
        Assert.assertTrue(baseEntries.range().hasTo(true));
        Assert.assertEquals("C", baseEntries.range().to(false));
        Assert.assertEquals("F", baseEntries.range().to(true));
        Assert.assertFalse(baseEntries.range().toInclusive(false));
        Assert.assertFalse(baseEntries.range().toInclusive(true));
    }
    
    @Test
    public void testSmallerRange() {
        NavigableBaseEntries<String, String> baseEntries = 
                new RedBlackTreeEntries<>(
                        BidiType.NONE,
                        ReplacementRule.OLD_REFERENCE_WIN, 
                        null, 
                        null
                );
        baseEntries =
                baseEntries
                .descendingEntries()
                .subEntries(true, "F", false, true, "C", false)
                .descendingEntries()
                .subEntries(true, "D", true, true, "E", true)
                .descendingEntries();
        Assert.assertSame(MACollections.reverseOrder(), baseEntries.range().comparator(false));
        Assert.assertNull(baseEntries.range().comparator(true));
        Assert.assertTrue(baseEntries.range().hasFrom(false));
        Assert.assertTrue(baseEntries.range().hasFrom(true));
        Assert.assertEquals("E", baseEntries.range().from(false));
        Assert.assertEquals("D", baseEntries.range().from(true));
        Assert.assertTrue(baseEntries.range().fromInclusive(false));
        Assert.assertTrue(baseEntries.range().fromInclusive(true));
        Assert.assertTrue(baseEntries.range().hasTo(false));
        Assert.assertTrue(baseEntries.range().hasTo(true));
        Assert.assertEquals("D", baseEntries.range().to(false));
        Assert.assertEquals("E", baseEntries.range().to(true));
        Assert.assertTrue(baseEntries.range().toInclusive(false));
        Assert.assertTrue(baseEntries.range().toInclusive(true));
    }
    
    @Test
    public void rangeFormSetWithoutInclusive() {
        XNavigableSet<String> set = new MATreeSet<>();
        NavigableRange<String> range =
                new NavigableRange<String>(
                        set
                        .subSet("B", "Z")
                        .descendingSet()
                        .headSet("B")
                        .tailSet("Y")
                        .descendingSet()
                        .descendingSet(), 
                        (XNavigableSetView<String> x) -> get(x, PARENT_SET)
                );
        Assert.assertSame(MACollections.reverseOrder(), range.comparator(false));
        Assert.assertNull(range.comparator(true));
        Assert.assertTrue(range.hasFrom(false));
        Assert.assertTrue(range.hasFrom(true));
        Assert.assertEquals("Y", range.from(false));
        Assert.assertEquals("B", range.from(true));
        Assert.assertTrue(range.fromInclusive(false));
        Assert.assertFalse(range.fromInclusive(true));
        Assert.assertTrue(range.hasTo(false));
        Assert.assertTrue(range.hasTo(true));
        Assert.assertEquals("B", range.to(false));
        Assert.assertEquals("Y", range.to(true));
        Assert.assertFalse(range.toInclusive(false));
        Assert.assertTrue(range.toInclusive(true));
    }
    
    @Test
    public void rangeFormSetWithInclusive() {
        XNavigableSet<String> set = new MATreeSet<>();
        NavigableRange<String> range =
                new NavigableRange<String>(
                        set
                        .subSet("B", true, "Z", false)
                        .descendingSet()
                        .headSet("B", false)
                        .tailSet("Y", true)
                        .descendingSet()
                        .descendingSet(), 
                        (XNavigableSetView<String> x) -> get(x, PARENT_SET)
                );
        Assert.assertSame(MACollections.reverseOrder(), range.comparator(false));
        Assert.assertNull(range.comparator(true));
        Assert.assertTrue(range.hasFrom(false));
        Assert.assertTrue(range.hasFrom(true));
        Assert.assertEquals("Y", range.from(false));
        Assert.assertEquals("B", range.from(true));
        Assert.assertTrue(range.fromInclusive(false));
        Assert.assertFalse(range.fromInclusive(true));
        Assert.assertTrue(range.hasTo(false));
        Assert.assertTrue(range.hasTo(true));
        Assert.assertEquals("B", range.to(false));
        Assert.assertEquals("Y", range.to(true));
        Assert.assertFalse(range.toInclusive(false));
        Assert.assertTrue(range.toInclusive(true));
    }
    
    @Test
    public void rangeFormMapWithoutInclusive() {
        XNavigableMap<String, String> map = new MATreeMap<>();
        NavigableRange<String> range =
                new NavigableRange<String>(
                        map
                        .subMap("B", "Z")
                        .descendingMap()
                        .headMap("B")
                        .tailMap("Y")
                        .descendingMap()
                        .descendingMap(),
                        (XNavigableMapView<String, ?> x) -> get(x, PARENT_MAP)
                );
        Assert.assertSame(MACollections.reverseOrder(), range.comparator(false));
        Assert.assertNull(range.comparator(true));
        Assert.assertTrue(range.hasFrom(false));
        Assert.assertTrue(range.hasFrom(true));
        Assert.assertEquals("Y", range.from(false));
        Assert.assertEquals("B", range.from(true));
        Assert.assertTrue(range.fromInclusive(false));
        Assert.assertFalse(range.fromInclusive(true));
        Assert.assertTrue(range.hasTo(false));
        Assert.assertTrue(range.hasTo(true));
        Assert.assertEquals("B", range.to(false));
        Assert.assertEquals("Y", range.to(true));
        Assert.assertFalse(range.toInclusive(false));
        Assert.assertTrue(range.toInclusive(true));
    }
    
    @Test
    public void rangeFormMapWithInclusive() {
        XNavigableMap<String, String> map = new MATreeMap<>();
        NavigableRange<String> range =
                new NavigableRange<String>(
                        map
                        .subMap("B", true, "Z", false)
                        .descendingMap()
                        .headMap("B", false)
                        .tailMap("Y", true)
                        .descendingMap()
                        .descendingMap(),
                        (XNavigableMapView<String, ?> x) -> get(x, PARENT_MAP)
                );
        Assert.assertSame(MACollections.reverseOrder(), range.comparator(false));
        Assert.assertNull(range.comparator(true));
        Assert.assertTrue(range.hasFrom(false));
        Assert.assertTrue(range.hasFrom(true));
        Assert.assertEquals("Y", range.from(false));
        Assert.assertEquals("B", range.from(true));
        Assert.assertTrue(range.fromInclusive(false));
        Assert.assertFalse(range.fromInclusive(true));
        Assert.assertTrue(range.hasTo(false));
        Assert.assertTrue(range.hasTo(true));
        Assert.assertEquals("B", range.to(false));
        Assert.assertEquals("Y", range.to(true));
        Assert.assertFalse(range.toInclusive(false));
        Assert.assertTrue(range.toInclusive(true));
    }
    
    private static Class<?> classOf(Class<?> clazz, String name) {
        for (Class<?> nc : clazz.getDeclaredClasses()) {
            if (nc.getSimpleName().equals(name)) {
                return nc;
            }
        }
        throw new AssertionError();
    }
    
    private static Field fieldOf(Class<?> clazz, String name) {
        Field field;
        try {
            field = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new AssertionError(ex);
        }
        field.setAccessible(true);
        return field;
    }
    
    @SuppressWarnings("unchecked")
    private static <E> E get(Object obj, Field field) {
        try {
            return (E)field.get(obj);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
    }
}
