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

import java.util.Collection;
import java.util.Comparator;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenComparator;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.FrozenEqualityComparator;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XNavigableMap;
import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.data.event.PropertyVersion;
import org.babyfish.lang.Arguments;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class FrozenContextTest {

    @Test
    public void testByNavigableSet() {
        XNavigableSet<Element> set = new TreeSet<Element>(Element.CODE_COMPARATOR);
        Element one = new Element("1", "One");
        Element two = new Element("2", "Two");
        Element three = new Element("3", "Three");
        Element four = new Element("4", "Four");
        set.add(one);
        set.add(two);
        set.add(three);
        set.add(four);
        
        assertElementCodeAndNames(set, "1", "One", "2", "Two", "3", "Three", "4", "Four");
        
        one.setCode("I");
        assertElementCodeAndNames(set, "2", "Two", "3", "Three", "4", "Four", "I", "One");
        
        two.setCode("II");
        assertElementCodeAndNames(set, "3", "Three", "4", "Four", "I", "One", "II", "Two");
        
        three.setCode("III");
        assertElementCodeAndNames(set, "4", "Four", "I", "One", "II", "Two", "III", "Three");
        
        four.setCode("IV");
        assertElementCodeAndNames(set, "I", "One", "II", "Two", "III", "Three", "IV", "Four");
        
        one.setCode("Unknown");
        assertElementCodeAndNames(set, "II", "Two", "III", "Three", "IV", "Four", "Unknown", "One");
        
        two.setCode("Unknown");
        assertElementCodeAndNames(set, "III", "Three", "IV", "Four", "Unknown", "Two");
        
        three.setCode("Unknown");
        assertElementCodeAndNames(set, "IV", "Four", "Unknown", "Three");
        
        four.setCode("Unknown");
        assertElementCodeAndNames(set, "Unknown", "Four");
    }
    
    @Test
    public void testByOrderedSet() {
        XOrderedSet<Element> set = new LinkedHashSet<Element>(Element.CODE_EQUALITY_COMPARATOR);
        Element one = new Element("1", "One");
        Element two = new Element("2", "Two");
        Element three = new Element("3", "Three");
        Element four = new Element("4", "Four");
        set.add(one);
        set.add(two);
        set.add(three);
        set.add(four);
        
        assertElementCodeAndNames(set, "1", "One", "2", "Two", "3", "Three", "4", "Four");
        
        one.setCode("I");
        assertElementCodeAndNames(set, "I", "One", "2", "Two", "3", "Three", "4", "Four");
        
        two.setCode("II");
        assertElementCodeAndNames(set, "I", "One", "II", "Two", "3", "Three", "4", "Four");
        
        three.setCode("III");
        assertElementCodeAndNames(set, "I", "One", "II", "Two", "III", "Three", "4", "Four");
        
        four.setCode("IV");
        assertElementCodeAndNames(set, "I", "One", "II", "Two", "III", "Three", "IV", "Four");
        
        one.setCode("Unknown");
        assertElementCodeAndNames(set, "Unknown", "One", "II", "Two", "III", "Three", "IV", "Four");
        
        two.setCode("Unknown");
        assertElementCodeAndNames(set, "Unknown", "Two", "III", "Three", "IV", "Four");
        
        three.setCode("Unknown");
        assertElementCodeAndNames(set, "Unknown", "Three", "IV", "Four");
        
        four.setCode("Unknown");
        assertElementCodeAndNames(set, "Unknown", "Four");
    }
    
    @Test
    public void testByMANavigableSet() {
        MANavigableSet<Element> set = new MATreeSet<Element>(Element.CODE_COMPARATOR);
        Element one = new Element("1", "One");
        Element two = new Element("2", "Two");
        Element three = new Element("3", "Three");
        Element four = new Element("4", "Four");
        set.add(one);
        set.add(two);
        set.add(three);
        set.add(four);
        final StringBuilder builder = new StringBuilder();
        set.addElementListener(
                new ElementListener<Element>() {
                    @Override
                    public void modified(ElementEvent<Element> e) throws Throwable {
                        if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                            builder
                            .append("-[")
                            .append(e.getElement(PropertyVersion.DETACH).getCode())
                            .append(", ")
                            .append(e.getElement(PropertyVersion.DETACH).getName())
                            .append(']');
                        }
                        if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                            builder
                            .append("+[")
                            .append(e.getElement(PropertyVersion.ATTACH).getCode())
                            .append(", ")
                            .append(e.getElement(PropertyVersion.ATTACH).getName())
                            .append(']');
                        }
                    }
                });
        
        assertElementCodeAndNames(set, "1", "One", "2", "Two", "3", "Three", "4", "Four");
        
        one.setCode("I");
        assertElementCodeAndNames(set, "2", "Two", "3", "Three", "4", "Four", "I", "One");
        Assert.assertEquals("-[1, One]+[I, One]", builder.toString());
        
        two.setCode("II");
        assertElementCodeAndNames(set, "3", "Three", "4", "Four", "I", "One", "II", "Two");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]", builder.toString());
        
        three.setCode("III");
        assertElementCodeAndNames(set, "4", "Four", "I", "One", "II", "Two", "III", "Three");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]", builder.toString());
        
        four.setCode("IV");
        assertElementCodeAndNames(set, "I", "One", "II", "Two", "III", "Three", "IV", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]", builder.toString());
        
        one.setCode("Unknown");
        assertElementCodeAndNames(set, "II", "Two", "III", "Three", "IV", "Four", "Unknown", "One");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]", builder.toString());
        
        two.setCode("Unknown");
        assertElementCodeAndNames(set, "III", "Three", "IV", "Four", "Unknown", "Two");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]-[II, Two]-[Unknown, One]+[Unknown, Two]", builder.toString());
        
        three.setCode("Unknown");
        assertElementCodeAndNames(set, "IV", "Four", "Unknown", "Three");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]-[II, Two]-[Unknown, One]+[Unknown, Two]-[III, Three]-[Unknown, Two]+[Unknown, Three]", builder.toString());
        
        four.setCode("Unknown");
        assertElementCodeAndNames(set, "Unknown", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]-[II, Two]-[Unknown, One]+[Unknown, Two]-[III, Three]-[Unknown, Two]+[Unknown, Three]-[IV, Four]-[Unknown, Three]+[Unknown, Four]", builder.toString());
    }
    
    @Test
    public void testByMAOrderedSet() {
        MAOrderedSet<Element> set = new MALinkedHashSet<Element>(Element.CODE_EQUALITY_COMPARATOR);
        Element one = new Element("1", "One");
        Element two = new Element("2", "Two");
        Element three = new Element("3", "Three");
        Element four = new Element("4", "Four");
        set.add(one);
        set.add(two);
        set.add(three);
        set.add(four);
        final StringBuilder builder = new StringBuilder();
        set.addElementListener(
                new ElementListener<Element>() {
                    @Override
                    public void modified(ElementEvent<Element> e) throws Throwable {
                        if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                            builder
                            .append("-[")
                            .append(e.getElement(PropertyVersion.DETACH).getCode())
                            .append(", ")
                            .append(e.getElement(PropertyVersion.DETACH).getName())
                            .append(']');
                        }
                        if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                            builder
                            .append("+[")
                            .append(e.getElement(PropertyVersion.ATTACH).getCode())
                            .append(", ")
                            .append(e.getElement(PropertyVersion.ATTACH).getName())
                            .append(']');
                        }
                    }
                });
        
        assertElementCodeAndNames(set, "1", "One", "2", "Two", "3", "Three", "4", "Four");
        
        one.setCode("I");
        assertElementCodeAndNames(set, "I", "One", "2", "Two", "3", "Three", "4", "Four");
        Assert.assertEquals("-[1, One]+[I, One]", builder.toString());
        
        two.setCode("II");
        assertElementCodeAndNames(set, "I", "One", "II", "Two", "3", "Three", "4", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]", builder.toString());
        
        three.setCode("III");
        assertElementCodeAndNames(set, "I", "One", "II", "Two", "III", "Three", "4", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]", builder.toString());
        
        four.setCode("IV");
        assertElementCodeAndNames(set, "I", "One", "II", "Two", "III", "Three", "IV", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]", builder.toString());
        
        one.setCode("Unknown");
        assertElementCodeAndNames(set, "Unknown", "One", "II", "Two", "III", "Three", "IV", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]", builder.toString());
        
        two.setCode("Unknown");
        assertElementCodeAndNames(set, "Unknown", "Two", "III", "Three", "IV", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]-[II, Two]-[Unknown, One]+[Unknown, Two]", builder.toString());
        
        three.setCode("Unknown");
        assertElementCodeAndNames(set, "Unknown", "Three", "IV", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]-[II, Two]-[Unknown, One]+[Unknown, Two]-[III, Three]-[Unknown, Two]+[Unknown, Three]", builder.toString());
        
        four.setCode("Unknown");
        assertElementCodeAndNames(set, "Unknown", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]-[II, Two]-[Unknown, One]+[Unknown, Two]-[III, Three]-[Unknown, Two]+[Unknown, Three]-[IV, Four]-[Unknown, Three]+[Unknown, Four]", builder.toString());
    }
    
    @Test
    public void testByNavigableMap() {
        XNavigableMap<Element, Object> map = new TreeMap<Element, Object>(
                Element.CODE_COMPARATOR, 
                UnifiedComparator.empty());
        Element one = new Element("1", "One");
        Element two = new Element("2", "Two");
        Element three = new Element("3", "Three");
        Element four = new Element("4", "Four");
        map.put(one, null);
        map.put(two, null);
        map.put(three, null);
        map.put(four, null);
        assertElementCodeAndNames(map.keySet(), "1", "One", "2", "Two", "3", "Three", "4", "Four");
        
        one.setCode("I");
        assertElementCodeAndNames(map.keySet(), "2", "Two", "3", "Three", "4", "Four", "I", "One");
        
        two.setCode("II");
        assertElementCodeAndNames(map.keySet(), "3", "Three", "4", "Four", "I", "One", "II", "Two");
        
        three.setCode("III");
        assertElementCodeAndNames(map.keySet(), "4", "Four", "I", "One", "II", "Two", "III", "Three");
        
        four.setCode("IV");
        assertElementCodeAndNames(map.keySet(), "I", "One", "II", "Two", "III", "Three", "IV", "Four");
        
        one.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "II", "Two", "III", "Three", "IV", "Four", "Unknown", "One");
        
        two.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "III", "Three", "IV", "Four", "Unknown", "Two");
        
        three.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "IV", "Four", "Unknown", "Three");
        
        four.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "Unknown", "Four");
    }
    
    @Test
    public void testByOrderedMap() {
        XOrderedMap<Element, Object> map = new LinkedHashMap<Element, Object>(
                Element.CODE_EQUALITY_COMPARATOR, 
                UnifiedComparator.empty());
        Element one = new Element("1", "One");
        Element two = new Element("2", "Two");
        Element three = new Element("3", "Three");
        Element four = new Element("4", "Four");
        map.put(one, null);
        map.put(two, null);
        map.put(three, null);
        map.put(four, null);
        assertElementCodeAndNames(map.keySet(), "1", "One", "2", "Two", "3", "Three", "4", "Four");
        
        one.setCode("I");
        assertElementCodeAndNames(map.keySet(), "I", "One", "2", "Two", "3", "Three", "4", "Four");
        
        two.setCode("II");
        assertElementCodeAndNames(map.keySet(), "I", "One", "II", "Two", "3", "Three", "4", "Four");
        
        three.setCode("III");
        assertElementCodeAndNames(map.keySet(), "I", "One", "II", "Two", "III", "Three", "4", "Four");
        
        four.setCode("IV");
        assertElementCodeAndNames(map.keySet(), "I", "One", "II", "Two", "III", "Three", "IV", "Four");
        
        one.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "Unknown", "One", "II", "Two", "III", "Three", "IV", "Four");
        
        two.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "Unknown", "Two", "III", "Three", "IV", "Four");
        
        three.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "Unknown", "Three", "IV", "Four");
        
        four.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "Unknown", "Four");
    }
    
    @Test
    public void testByMANavigableMap() {
        MANavigableMap<Element, Object> map = new MATreeMap<Element, Object>(
                Element.CODE_COMPARATOR, 
                UnifiedComparator.empty());
        Element one = new Element("1", "One");
        Element two = new Element("2", "Two");
        Element three = new Element("3", "Three");
        Element four = new Element("4", "Four");
        map.put(one, null);
        map.put(two, null);
        map.put(three, null);
        map.put(four, null);
        final StringBuilder builder = new StringBuilder();
        map.addMapElementListener(
                new MapElementListener<Element, Object>() {
                    @Override
                    public void modified(MapElementEvent<Element, Object> e) throws Throwable {
                        if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                            builder
                            .append("-[")
                            .append(e.getKey(PropertyVersion.DETACH).getCode())
                            .append(", ")
                            .append(e.getKey(PropertyVersion.DETACH).getName())
                            .append(']');
                        }
                        if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                            builder
                            .append("+[")
                            .append(e.getKey(PropertyVersion.ATTACH).getCode())
                            .append(", ")
                            .append(e.getKey(PropertyVersion.ATTACH).getName())
                            .append(']');
                        }
                    }
                });
        
        assertElementCodeAndNames(map.keySet(), "1", "One", "2", "Two", "3", "Three", "4", "Four");
        
        one.setCode("I");
        assertElementCodeAndNames(map.keySet(), "2", "Two", "3", "Three", "4", "Four", "I", "One");
        Assert.assertEquals("-[1, One]+[I, One]", builder.toString());
        
        two.setCode("II");
        assertElementCodeAndNames(map.keySet(), "3", "Three", "4", "Four", "I", "One", "II", "Two");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]", builder.toString());
        
        three.setCode("III");
        assertElementCodeAndNames(map.keySet(), "4", "Four", "I", "One", "II", "Two", "III", "Three");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]", builder.toString());
        
        four.setCode("IV");
        assertElementCodeAndNames(map.keySet(), "I", "One", "II", "Two", "III", "Three", "IV", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]", builder.toString());
        
        one.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "II", "Two", "III", "Three", "IV", "Four", "Unknown", "One");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]", builder.toString());
        
        two.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "III", "Three", "IV", "Four", "Unknown", "Two");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]-[II, Two]-[Unknown, One]+[Unknown, Two]", builder.toString());
        
        three.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "IV", "Four", "Unknown", "Three");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]-[II, Two]-[Unknown, One]+[Unknown, Two]-[III, Three]-[Unknown, Two]+[Unknown, Three]", builder.toString());
        
        four.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "Unknown", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]-[II, Two]-[Unknown, One]+[Unknown, Two]-[III, Three]-[Unknown, Two]+[Unknown, Three]-[IV, Four]-[Unknown, Three]+[Unknown, Four]", builder.toString());
    }
    
    @Test
    public void testByMAOrderedMap() {
        MAOrderedMap<Element, Object> map = new MALinkedHashMap<Element, Object>(
                Element.CODE_EQUALITY_COMPARATOR, 
                UnifiedComparator.empty());
        Element one = new Element("1", "One");
        Element two = new Element("2", "Two");
        Element three = new Element("3", "Three");
        Element four = new Element("4", "Four");
        map.put(one, null);
        map.put(two, null);
        map.put(three, null);
        map.put(four, null);
        final StringBuilder builder = new StringBuilder();
        map.addMapElementListener(
                new MapElementListener<Element, Object>() {
                    @Override
                    public void modified(MapElementEvent<Element, Object> e) throws Throwable {
                        if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                            builder
                            .append("-[")
                            .append(e.getKey(PropertyVersion.DETACH).getCode())
                            .append(", ")
                            .append(e.getKey(PropertyVersion.DETACH).getName())
                            .append(']');
                        }
                        if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                            builder
                            .append("+[")
                            .append(e.getKey(PropertyVersion.ATTACH).getCode())
                            .append(", ")
                            .append(e.getKey(PropertyVersion.ATTACH).getName())
                            .append(']');
                        }
                    }
                });
        
        assertElementCodeAndNames(map.keySet(), "1", "One", "2", "Two", "3", "Three", "4", "Four");
        
        one.setCode("I");
        assertElementCodeAndNames(map.keySet(), "I", "One", "2", "Two", "3", "Three", "4", "Four");
        Assert.assertEquals("-[1, One]+[I, One]", builder.toString());
        
        two.setCode("II");
        assertElementCodeAndNames(map.keySet(), "I", "One", "II", "Two", "3", "Three", "4", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]", builder.toString());
        
        three.setCode("III");
        assertElementCodeAndNames(map.keySet(), "I", "One", "II", "Two", "III", "Three", "4", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]", builder.toString());
        
        four.setCode("IV");
        assertElementCodeAndNames(map.keySet(), "I", "One", "II", "Two", "III", "Three", "IV", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]", builder.toString());
        
        one.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "Unknown", "One", "II", "Two", "III", "Three", "IV", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]", builder.toString());
        
        two.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "Unknown", "Two", "III", "Three", "IV", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]-[II, Two]-[Unknown, One]+[Unknown, Two]", builder.toString());
        
        three.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "Unknown", "Three", "IV", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]-[II, Two]-[Unknown, One]+[Unknown, Two]-[III, Three]-[Unknown, Two]+[Unknown, Three]", builder.toString());
        
        four.setCode("Unknown");
        assertElementCodeAndNames(map.keySet(), "Unknown", "Four");
        Assert.assertEquals("-[1, One]+[I, One]-[2, Two]+[II, Two]-[3, Three]+[III, Three]-[4, Four]+[IV, Four]-[I, One]+[Unknown, One]-[II, Two]-[Unknown, One]+[Unknown, Two]-[III, Three]-[Unknown, Two]+[Unknown, Three]-[IV, Four]-[Unknown, Three]+[Unknown, Four]", builder.toString());
    }
    
    private static void assertElementCodeAndNames(Collection<Element> elements, String ... elementCodeAndNames) {
        if (elementCodeAndNames.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        Assert.assertEquals(elementCodeAndNames.length / 2, elements.size());
        int index = 0;
        for (Element element : elements) {
            Assert.assertEquals(elementCodeAndNames[index++], element.getCode());
            Assert.assertEquals(elementCodeAndNames[index++], element.getName());
        }
    }
    
    static class Element {
        
        public static final Comparator<Element> CODE_COMPARATOR =
                new FrozenComparator<Element>() {
            
                    @Override
                    public int compare(Element element1, Element element2) {
                        return element1.code.compareTo(element2.code);
                    }

                    @Override
                    public void freeze(Element element, FrozenContext<Element> ctx) {
                        element.codeFrozenContext = 
                                FrozenContext.combine(
                                        element.codeFrozenContext,
                                        ctx);
                    }

                    @Override
                    public void unfreeze(Element element, FrozenContext<Element> ctx) {
                        element.codeFrozenContext = 
                                FrozenContext.remove(
                                        element.codeFrozenContext,
                                        ctx);
                    }
            
                };
                
        public static final EqualityComparator<Element> CODE_EQUALITY_COMPARATOR =
                new FrozenEqualityComparator<Element>() {
            
                    @Override
                    public int hashCode(Element element) {
                        return element.code.hashCode();
                    }
                    
                    @Override
                    public boolean equals(Element element1, Element element2) {
                        return element1.code.equals(element2.code);
                    }

                    @Override
                    public void freeze(Element element, FrozenContext<Element> ctx) {
                        element.codeFrozenContext = 
                                FrozenContext.combine(
                                        element.codeFrozenContext,
                                        ctx);
                    }

                    @Override
                    public void unfreeze(Element element, FrozenContext<Element> ctx) {
                        element.codeFrozenContext = 
                                FrozenContext.remove(
                                        element.codeFrozenContext,
                                        ctx);
                    }
            
                };
        
        private String code;
        
        private FrozenContext<Element> codeFrozenContext;
        
        private String name;
        
        public Element(String code, String name) {
            this.setCode(code);
            this.setName(name);
        }
        
        public String getCode() {
            return this.code;
        }

        public void setCode(String code) {
            Arguments.mustNotBeNull("code", code);
            if (code.equals(this.code)) {
                return;
            }
            FrozenContext<Element> ctx = this.codeFrozenContext;
            FrozenContext.suspendFreezing(ctx, this);
            try {
                this.code = code;
            } finally {
                FrozenContext.resumeFreezing(ctx);
            }
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    
    static class StringRef {
        
        public static final EqualityComparator<StringRef> VFE =
                new FrozenEqualityComparator<StringRef>() {
            
                    @Override
                    public int hashCode(StringRef stringRef) {
                        return stringRef.value.hashCode();
                    }
                    
                    @Override
                    public boolean equals(StringRef stringRef1, StringRef stringRef2) {
                        return stringRef1.value.equals(stringRef2.value);
                    }

                    @Override
                    public void freeze(StringRef stringRef, FrozenContext<StringRef> ctx) {
                        stringRef.valueFrozenContext = 
                                FrozenContext.combine(
                                        stringRef.valueFrozenContext,
                                        ctx);
                    }

                    @Override
                    public void unfreeze(StringRef stringRef, FrozenContext<StringRef> ctx) {
                        stringRef.valueFrozenContext = 
                                FrozenContext.remove(
                                        stringRef.valueFrozenContext,
                                        ctx);
                    }
            
                };
        
        private String value;
        
        private FrozenContext<StringRef> valueFrozenContext;
        
        public StringRef(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            FrozenContext<StringRef> ctx = this.valueFrozenContext;
            FrozenContext.suspendFreezing(ctx, this);
            try {
                this.value = value;
            } finally {
                FrozenContext.resumeFreezing(ctx);
            }
        }
    }
}
