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
package org.babyfish.test.collection.equalitycomparator;

import java.lang.reflect.Field;
import java.util.List;

import junit.framework.Assert;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XSortedMap;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.collection.spi.AbstractXMap;
import org.babyfish.collection.spi.base.BaseEntries;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public abstract class AbstractXMapTest {
    
    private static final Field BASE_ENTRIES_FIELD;
    
    private XMap<Element, Element> xmap;
    
    private List<String> preEvents;
    
    private List<String> postEvents;
    
    private Integer modCount;
    
    protected abstract XMap<Element, Element> createXMap();
    
    protected abstract ReplacementRule keyReplacementRule();
    
    @Before
    public void initXMapToTestDetachBehaviors() {
        this.xmap = this.createXMap();
        this.modCount = null;
        Assert.assertEquals(this.keyReplacementRule(), this.xmap.keyReplacementRule());
        if (this.xmap.keyReplacementRule() == ReplacementRule.NEW_REFERENCE_WIN) {
            this.xmap.put(new Element("001", "Unknown"), null);
            this.xmap.put(new Element("002", "Unknown"), null);
            this.xmap.put(new Element("003", "Unknown"), null);
            this.xmap.put(new Element("004", "Unknown"), null);
            this.xmap.put(new Element("005", "Unknown"), null);
            this.xmap.put(new Element("006", "Unknown"), null);
            this.xmap.put(new Element("007", "Unknown"), null);
            this.xmap.put(new Element("008", "Unknown"), null);
        }
        this.xmap.put(new Element("001", "Jim"), new Element("Couple-I", "Jim|Kate"));
        this.xmap.put(new Element("002", "Kate"), new Element("Couple-I", "Jim|Kate"));
        this.xmap.put(new Element("003", "Smith"), new Element("Couple-II", "Smith|Linda"));
        this.xmap.put(new Element("004", "Linda"), new Element("Couple-II", "Smith|Linda"));
        this.xmap.put(new Element("005", "Sam"), new Element("Couple-III", "Sam|Lucy"));
        this.xmap.put(new Element("006", "Lucy"), new Element("Couple-III", "Sam|Lucy"));
        this.xmap.put(new Element("007", "Jhon"), new Element("Couple-IV", "Jhon|Mary"));
        this.xmap.put(new Element("008", "Mary"), new Element("Couple-IV", "Jhon|Mary"));
        
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "003", "004", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary", "Jhon|Mary");
        
        this.modCount = null;
        if (this.xmap instanceof MAMap<?, ?>) {
            this.preEvents = new ArrayList<String>();
            this.postEvents = new ArrayList<String>();
            MAMap<Element, Element> maMap = (MAMap<Element, Element>)this.xmap;
            maMap.addMapElementListener(
                    new MapElementListener<Element, Element>() {
                        @Override
                        public void modifying(MapElementEvent<Element, Element> e) throws Throwable {
                            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                                AbstractXMapTest.this.preEvents.add(
                                        "-DI[" +
                                        e.getKey(PropertyVersion.DETACH) +
                                        "]" +
                                        e.getValue(PropertyVersion.DETACH) +
                                        "");
                            }
                        }
                        @Override
                        public void modified(MapElementEvent<Element, Element> e) throws Throwable {
                            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                                AbstractXMapTest.this.postEvents.add(
                                        "-DE[" +
                                        e.getKey(PropertyVersion.DETACH) +
                                        "]" +
                                        e.getValue(PropertyVersion.DETACH) +
                                        "");
                            }
                        }
                        
                    });
        }
    }
    
    @Test
    public void testRemoveFirstSuccess() {
        this.synchronizeAndAssertModCount(false);
        Element detachedValue = this.xmap.remove(new Element("001", "invalidName"));
        Assert.assertNotNull(detachedValue);
        Assert.assertEquals("Couple-I", detachedValue.getCode());
        Assert.assertEquals("Jim|Kate", detachedValue.getName());
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Assert.assertEquals(
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}", 
                    this.preEvents.iterator().next());
            Assert.assertEquals(
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}", 
                    this.postEvents.iterator().next());
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "002", "003", "004", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary", "Jhon|Mary");
    }
    
    @Test
    public void testRemoveLastSuccess() {
        this.synchronizeAndAssertModCount(false);
        Element detachedValue = this.xmap.remove(new Element("008", "invalidName"));
        Assert.assertNotNull(detachedValue);
        Assert.assertEquals("Couple-IV", detachedValue.getCode());
        Assert.assertEquals("Jhon|Mary", detachedValue.getName());
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Assert.assertEquals(
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}", 
                    this.preEvents.iterator().next());
            Assert.assertEquals(
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}", 
                    this.postEvents.iterator().next());
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "003", "004", "005", "006", "007");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary");
    }

    @Test
    public void testRemoveMiddleSuccess() {
        this.synchronizeAndAssertModCount(false);
        Element detachedValue = this.xmap.remove(new Element("004", "invalidName"));
        Assert.assertNotNull(detachedValue);
        Assert.assertEquals("Couple-II", detachedValue.getCode());
        Assert.assertEquals("Smith|Linda", detachedValue.getName());
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Assert.assertEquals(
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}", 
                    this.preEvents.iterator().next());
            Assert.assertEquals(
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}", 
                    this.postEvents.iterator().next());
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "003", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Smith", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-II", "Couple-III", "Couple-III", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary", "Jhon|Mary");
    }
    
    @Test
    public void testRemoveFailed() {
        this.synchronizeAndAssertModCount(false);
        Element detachedValue = this.xmap.remove(new Element("invalidCode", "invalidName"));
        Assert.assertNull(detachedValue);
        this.synchronizeAndAssertModCount(false);
        if (this.preEvents != null) {
            Assert.assertTrue(this.preEvents.isEmpty());
            Assert.assertTrue(this.postEvents.isEmpty());
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "003", "004", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary", "Jhon|Mary");
    }
    
    @Test
    public void testRemoveFirstOfEntrySet() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .entrySet()
                .remove(
                        Helper.createEntry(
                                "001", "invalidName", "Couple-I", "invalidName")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Assert.assertEquals(
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}", 
                    this.preEvents.iterator().next());
            Assert.assertEquals(
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}", 
                    this.postEvents.iterator().next());
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "002", "003", "004", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary", "Jhon|Mary");
    }
    
    @Test
    public void testRemoveLastOfEntrySet() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .entrySet()
                .remove(
                        Helper.createEntry(
                                "008", "invalidName", "Couple-IV", "invalidName")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Assert.assertEquals(
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}", 
                    this.preEvents.iterator().next());
            Assert.assertEquals(
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}", 
                    this.postEvents.iterator().next());
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "003", "004", "005", "006", "007");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary");
    }
    
    @Test
    public void testRemoveMiddleOfEntrySet() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .entrySet()
                .remove(
                        Helper.createEntry(
                                "004", "invalidName", "Couple-II", "invalidName")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Assert.assertEquals(
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}", 
                    this.preEvents.iterator().next());
            Assert.assertEquals(
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}", 
                    this.postEvents.iterator().next());
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "003", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Smith", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-II", "Couple-III", "Couple-III", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary", "Jhon|Mary");
    }
    
    @Test
    public void testRemoveEntrySetFailed() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertFalse(
                this
                .xmap
                .entrySet()
                .remove(
                        Helper.createEntry(
                                "004", "invalidName", "invalidCode", "invalidName")));
        this.synchronizeAndAssertModCount(false);
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "003", "004", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary", "Jhon|Mary");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRemove7531OfEntrySetByCollectionWithSameComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .entrySet()
                .removeAll(
                        Helper.createList(
                                this.xmap.entryUnifiedComparator(), 
                                Helper.createEntry("007", "invalidName", "Couple-IV", "invalidName"),
                                Helper.createEntry("005", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("003", "invalidName", "Couple-II", "invalidName"),
                                Helper.createEntry("001", "invalidName", "Couple-I", "invalidName"),
                                Helper.createEntry("005", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("003", "invalidName", "Couple-II", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "002", "004", "006", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Kate", "Linda", "Lucy", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRemove7531OfEntrySetByCollectionWithDiffComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .entrySet()
                .removeAll(
                        Helper.createList(
                                null, 
                                Helper.createEntry("007", "invalidName", "Couple-IV", "invalidName"),
                                Helper.createEntry("005", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("003", "invalidName", "Couple-II", "invalidName"),
                                Helper.createEntry("001", "invalidName", "Couple-I", "invalidName"),
                                Helper.createEntry("005", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("003", "invalidName", "Couple-II", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "002", "004", "006", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Kate", "Linda", "Lucy", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRetain7531OfEntrySetByCollectionWithSameComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .entrySet()
                .retainAll(
                        Helper.createList(
                                this.xmap.entryUnifiedComparator(), 
                                Helper.createEntry("007", "invalidName", "Couple-IV", "invalidName"),
                                Helper.createEntry("005", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("003", "invalidName", "Couple-II", "invalidName"),
                                Helper.createEntry("001", "invalidName", "Couple-I", "invalidName"),
                                Helper.createEntry("005", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("003", "invalidName", "Couple-II", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "003", "005", "007");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Smith", "Sam", "Jhon");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRetain7531OfEntrySetByCollectionWithDiffComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .entrySet()
                .retainAll(
                        Helper.createList(
                                null, 
                                Helper.createEntry("007", "invalidName", "Couple-IV", "invalidName"),
                                Helper.createEntry("005", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("003", "invalidName", "Couple-II", "invalidName"),
                                Helper.createEntry("001", "invalidName", "Couple-I", "invalidName"),
                                Helper.createEntry("005", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("003", "invalidName", "Couple-II", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "003", "005", "007");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Smith", "Sam", "Jhon");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testReomve8462OfEntrySetByCollectionWithSameComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .entrySet()
                .removeAll(
                        Helper.createList(
                                this.xmap.entryUnifiedComparator(), 
                                Helper.createEntry("008", "invalidName", "Couple-IV", "invalidName"),
                                Helper.createEntry("006", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("004", "invalidName", "Couple-II", "invalidName"),
                                Helper.createEntry("002", "invalidName", "Couple-I", "invalidName"),
                                Helper.createEntry("006", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("002", "invalidName", "Couple-II", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "003", "005", "007");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Smith", "Sam", "Jhon");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testReomve8462OfEntrySetByCollectionWithDiffComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .entrySet()
                .removeAll(
                        Helper.createList(
                                null, 
                                Helper.createEntry("008", "invalidName", "Couple-IV", "invalidName"),
                                Helper.createEntry("006", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("004", "invalidName", "Couple-II", "invalidName"),
                                Helper.createEntry("002", "invalidName", "Couple-I", "invalidName"),
                                Helper.createEntry("006", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("002", "invalidName", "Couple-II", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "003", "005", "007");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Smith", "Sam", "Jhon");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRetain8642OfEntrySetByCollectionWithSameComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .entrySet()
                .retainAll(
                        Helper.createList(
                                this.xmap.entryUnifiedComparator(), 
                                Helper.createEntry("008", "invalidName", "Couple-IV", "invalidName"),
                                Helper.createEntry("006", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("004", "invalidName", "Couple-II", "invalidName"),
                                Helper.createEntry("002", "invalidName", "Couple-I", "invalidName"),
                                Helper.createEntry("006", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("002", "invalidName", "Couple-II", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "002", "004", "006", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Kate", "Linda", "Lucy", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRetain8642OfEntrySetByCollectionWithDiffComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .entrySet()
                .retainAll(
                        Helper.createList(
                                null, 
                                Helper.createEntry("008", "invalidName", "Couple-IV", "invalidName"),
                                Helper.createEntry("006", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("004", "invalidName", "Couple-II", "invalidName"),
                                Helper.createEntry("002", "invalidName", "Couple-I", "invalidName"),
                                Helper.createEntry("006", "invalidName", "Couple-III", "invalidName"),
                                Helper.createEntry("002", "invalidName", "Couple-II", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "002", "004", "006", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Kate", "Linda", "Lucy", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @Test
    public void testRemoveFirstOfKeySet() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .keySet()
                .remove(new Element("001", "invalidName")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Assert.assertEquals(
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}", 
                    this.preEvents.iterator().next());
            Assert.assertEquals(
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}", 
                    this.postEvents.iterator().next());
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "002", "003", "004", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary", "Jhon|Mary");
    }
    
    @Test
    public void testRemoveLastOfKeySet() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .keySet()
                .remove(new Element("008", "invalidName")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Assert.assertEquals(
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}", 
                    this.preEvents.iterator().next());
            Assert.assertEquals(
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}", 
                    this.postEvents.iterator().next());
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "003", "004", "005", "006", "007");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary");
    }
    
    @Test
    public void testRemoveMiddleOfKeySet() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .keySet()
                .remove(new Element("004", "invalidName")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Assert.assertEquals(
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}", 
                    this.preEvents.iterator().next());
            Assert.assertEquals(
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}", 
                    this.postEvents.iterator().next());
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "003", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Smith", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-II", "Couple-III", "Couple-III", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary", "Jhon|Mary");
    }
    
    @Test
    public void testRemoveKeySetFailed() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertFalse(
                this
                .xmap
                .keySet()
                .remove(new Element("invalidCode", "invalidName")));
        this.synchronizeAndAssertModCount(false);
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "003", "004", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary", "Jhon|Mary");
    }
    
    @Test
    public void testRemove7531OfKeySetByCollectionWithSameComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .keySet()
                .removeAll(
                        Helper.createList(
                                this.xmap.keyUnifiedComparator(), 
                                new Element("007", "invalidName"),
                                new Element("005", "invalidName"),
                                new Element("003", "invalidName"),
                                new Element("001", "invalidName"),
                                new Element("005", "invalidName"),
                                new Element("001", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "002", "004", "006", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Kate", "Linda", "Lucy", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @Test
    public void testRemove7531OfKeySetByCollectionWithDiffComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .keySet()
                .removeAll(
                        Helper.createList(
                                null, 
                                new Element("007", "invalidName"),
                                new Element("005", "invalidName"),
                                new Element("003", "invalidName"),
                                new Element("001", "invalidName"),
                                new Element("005", "invalidName"),
                                new Element("001", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "002", "004", "006", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Kate", "Linda", "Lucy", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @Test
    public void testRetain7531OfKeySetByCollectionWithSameComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .keySet()
                .retainAll(
                        Helper.createList(
                                this.xmap.keyUnifiedComparator(), 
                                new Element("007", "invalidName"),
                                new Element("005", "invalidName"),
                                new Element("003", "invalidName"),
                                new Element("001", "invalidName"),
                                new Element("005", "invalidName"),
                                new Element("001", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "003", "005", "007");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Smith", "Sam", "Jhon");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }

    @Test
    public void testRetain7531OfKeySetByCollectionWithDiffComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .keySet()
                .retainAll(
                        Helper.createList(
                                null, 
                                new Element("007", "invalidName"),
                                new Element("005", "invalidName"),
                                new Element("003", "invalidName"),
                                new Element("001", "invalidName"),
                                new Element("005", "invalidName"),
                                new Element("001", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "003", "005", "007");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Smith", "Sam", "Jhon");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @Test
    public void testReomve8462OfKeySetByCollectionWithSameComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .keySet()
                .removeAll(
                        Helper.createList(
                                this.xmap.keyUnifiedComparator(), 
                                new Element("008", "invalidName"),
                                new Element("006", "invalidName"),
                                new Element("004", "invalidName"),
                                new Element("002", "invalidName"),
                                new Element("006", "invalidName"),
                                new Element("002", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "003", "005", "007");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Smith", "Sam", "Jhon");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @Test
    public void testReomve8462OfKeySetByCollectionWithDiffComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .keySet()
                .removeAll(
                        Helper.createList(
                                null, 
                                new Element("008", "invalidName"),
                                new Element("006", "invalidName"),
                                new Element("004", "invalidName"),
                                new Element("002", "invalidName"),
                                new Element("006", "invalidName"),
                                new Element("002", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "003", "005", "007");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Smith", "Sam", "Jhon");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @Test
    public void testRetain8642OfKeySetByCollectionWithSameComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .keySet()
                .retainAll(
                        Helper.createList(
                                this.xmap.keyUnifiedComparator(), 
                                new Element("008", "invalidName"),
                                new Element("006", "invalidName"),
                                new Element("004", "invalidName"),
                                new Element("002", "invalidName"),
                                new Element("006", "invalidName"),
                                new Element("002", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "002", "004", "006", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Kate", "Linda", "Lucy", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @Test
    public void testRetain8642OfKeySetByCollectionWithDiffComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .keySet()
                .retainAll(
                        Helper.createList(
                                null, 
                                new Element("008", "invalidName"),
                                new Element("006", "invalidName"),
                                new Element("004", "invalidName"),
                                new Element("002", "invalidName"),
                                new Element("006", "invalidName"),
                                new Element("002", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "002", "004", "006", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Kate", "Linda", "Lucy", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-III", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Jhon|Mary");
    }
    
    @Test
    public void testRemoveFirstOfValues() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .values()
                .remove(new Element("Couple-I", "invalidName")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Assert.assertEquals(
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}", 
                    this.preEvents.iterator().next());
            Assert.assertEquals(
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}", 
                    this.postEvents.iterator().next());
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "002", "003", "004", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary", "Jhon|Mary");
    }
    
    @Test
    public void testRemoveElementBeforeLastOfValues() {
        if (this.xmap instanceof XOrderedMap<?, ?> ||
                this.xmap instanceof XSortedMap<?, ?>) {
            this.synchronizeAndAssertModCount(false);
            Assert.assertTrue(
                    this
                    .xmap
                    .values()
                    .remove(new Element("Couple-IV", "invalidName")));
            this.synchronizeAndAssertModCount(true);
            if (this.preEvents != null) {
                Assert.assertEquals(
                        "-DI[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}", 
                        this.preEvents.iterator().next());
                Assert.assertEquals(
                        "-DE[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}", 
                        this.postEvents.iterator().next());
            }
            Helper.assertElementCodes(
                    this.xmap.keySet(), 
                    "001", "002", "003", "004", "005", "006", "008");
            Helper.assertElementNames(
                    this.xmap.keySet(), 
                    "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Mary");
            Helper.assertElementCodes(
                    this.xmap.values(), 
                    "Couple-I", "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III", "Couple-IV");
            Helper.assertElementNames(
                    this.xmap.values(), 
                    "Jim|Kate", "Jim|Kate", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary");
        }
    }
    
    @Test
    public void testRemoveTheElementBeforeMiddleOfValues() {
        if (this.xmap instanceof XOrderedMap<?, ?> ||
                this.xmap instanceof XSortedMap<?, ?>) {
            this.synchronizeAndAssertModCount(false);
            Assert.assertTrue(
                    this
                    .xmap
                    .values()
                    .remove(new Element("Couple-II", "invalidName")));
            this.synchronizeAndAssertModCount(true);
            if (this.preEvents != null) {
                Assert.assertEquals(
                        "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}", 
                        this.preEvents.iterator().next());
                Assert.assertEquals(
                        "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}", 
                        this.postEvents.iterator().next());
            }
            Helper.assertElementCodes(
                    this.xmap.keySet(), 
                    "001", "002", "004", "005", "006", "007", "008");
            Helper.assertElementNames(
                    this.xmap.keySet(), 
                    "Jim", "Kate", "Linda", "Sam", "Lucy", "Jhon", "Mary");
            Helper.assertElementCodes(
                    this.xmap.values(), 
                    "Couple-I", "Couple-I", "Couple-II", "Couple-III", "Couple-III", "Couple-IV", "Couple-IV");
            Helper.assertElementNames(
                    this.xmap.values(), 
                    "Jim|Kate", "Jim|Kate", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary", "Jhon|Mary");
        }
    }
    
    @Test
    public void testRemoveValuesFailed() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertFalse(
                this
                .xmap
                .values()
                .remove(new Element("invalidCode", "invalidName")));
        this.synchronizeAndAssertModCount(false);
        if (this.preEvents != null) {
            Assert.assertTrue(this.preEvents.isEmpty());
            Assert.assertTrue(this.postEvents.isEmpty());
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "003", "004", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy", "Jhon|Mary", "Jhon|Mary");
    }
    
    @Test
    public void testRemove6521OfValuesByCollectionWithSameComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .values()
                .removeAll(
                        Helper.createList(
                                this.xmap.valueUnifiedComparator(), 
                                new Element("Couple-III", "invalidName"),
                                new Element("Couple-I", "invalidName"),
                                new Element("Couple-III", "invalidName"),
                                new Element("Couple-I", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "003", "004", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Smith", "Linda", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-II", "Couple-II", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Smith|Linda", "Smith|Linda", "Jhon|Mary", "Jhon|Mary");
    }
    
    @Test
    public void testRemove6521OfValuesByCollectionWithDiffComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .values()
                .removeAll(
                        Helper.createList(
                                null, 
                                new Element("Couple-III", "invalidName"),
                                new Element("Couple-I", "invalidName"),
                                new Element("Couple-III", "invalidName"),
                                new Element("Couple-I", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "003", "004", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Smith", "Linda", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-II", "Couple-II", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Smith|Linda", "Smith|Linda", "Jhon|Mary", "Jhon|Mary");
    }
    
    @Test
    public void testRetain6521OfValuesByCollectionWithSameComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .values()
                .retainAll(
                        Helper.createList(
                                this.xmap.valueUnifiedComparator(), 
                                new Element("Couple-III", "invalidName"),
                                new Element("Couple-I", "invalidName"),
                                new Element("Couple-III", "invalidName"),
                                new Element("Couple-I", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}",
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}",
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "005", "006");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Sam", "Lucy");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-III", "Couple-III");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Sam|Lucy", "Sam|Lucy");
    }
    
    @Test
    public void testRetain6521OfValuesByCollectionWithDiffComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .values()
                .retainAll(
                        Helper.createList(
                                null, 
                                new Element("Couple-III", "invalidName"),
                                new Element("Couple-I", "invalidName"),
                                new Element("Couple-III", "invalidName"),
                                new Element("Couple-I", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}",
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}",
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "005", "006");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Sam", "Lucy");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-III", "Couple-III");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Sam|Lucy", "Sam|Lucy");
    }
    
    @Test
    public void testRemove8743OfValuesByCollectionWithSameComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .values()
                .removeAll(
                        Helper.createList(
                                this.xmap.valueUnifiedComparator(), 
                                new Element("Couple-IV", "invalidName"),
                                new Element("Couple-II", "invalidName"),
                                new Element("Couple-IV", "invalidName"),
                                new Element("Couple-II", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}",
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}",
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "005", "006");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Sam", "Lucy");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-III", "Couple-III");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Sam|Lucy", "Sam|Lucy");
    }
    
    @Test
    public void testRemove8743OfValuesByCollectionWithDiffComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .values()
                .removeAll(
                        Helper.createList(
                                null, 
                                new Element("Couple-IV", "invalidName"),
                                new Element("Couple-II", "invalidName"),
                                new Element("Couple-IV", "invalidName"),
                                new Element("Couple-II", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DI[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}",
                    "-DI[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    "-DE[{code:007,name:Jhon}]{code:Couple-IV,name:Jhon|Mary}",
                    "-DE[{code:008,name:Mary}]{code:Couple-IV,name:Jhon|Mary}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "001", "002", "005", "006");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Jim", "Kate", "Sam", "Lucy");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-I", "Couple-I", "Couple-III", "Couple-III");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Jim|Kate", "Jim|Kate", "Sam|Lucy", "Sam|Lucy");
    }

    @Test
    public void testRetain8743OfValuesByCollectionWithSameComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .values()
                .retainAll(
                        Helper.createList(
                                this.xmap.valueUnifiedComparator(), 
                                new Element("Couple-IV", "invalidName"),
                                new Element("Couple-II", "invalidName"),
                                new Element("Couple-IV", "invalidName"),
                                new Element("Couple-II", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "003", "004", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Smith", "Linda", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-II", "Couple-II", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Smith|Linda", "Smith|Linda", "Jhon|Mary", "Jhon|Mary");
    }
    
    @Test
    public void testRetain8743OfValuesByCollectionWithDiffComparator() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this
                .xmap
                .values()
                .retainAll(
                        Helper.createList(
                                null, 
                                new Element("Couple-IV", "invalidName"),
                                new Element("Couple-II", "invalidName"),
                                new Element("Couple-IV", "invalidName"),
                                new Element("Couple-II", "invalidName"))));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}");
            Helper.assertCollection(
                    false, 
                    this.postEvents, 
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}",
                    "-DE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}",
                    "-DE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}");
        }
        Helper.assertElementCodes(
                this.xmap.keySet(), 
                "003", "004", "007", "008");
        Helper.assertElementNames(
                this.xmap.keySet(), 
                "Smith", "Linda", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xmap.values(), 
                "Couple-II", "Couple-II", "Couple-IV", "Couple-IV");
        Helper.assertElementNames(
                this.xmap.values(), 
                "Smith|Linda", "Smith|Linda", "Jhon|Mary", "Jhon|Mary");
    }
    
    private void synchronizeAndAssertModCount(boolean expectedChanged) {
        this.synchronizeAndAssertModCount(this.xmap, expectedChanged);
    }
    
    protected void synchronizeAndAssertModCount(XMap<Element, Element> xmap, boolean expectedChanged) {
        BaseEntries<?, ?> baseEntries = null;
        try {
            baseEntries = (BaseEntries<?, ?>)BASE_ENTRIES_FIELD.get(xmap);
        } catch (IllegalArgumentException ex) {
            Assert.fail("can not access the modCount");
        } catch (IllegalAccessException ex) {
            Assert.fail("can not access the modCount");
        }
        int modCount = baseEntries.modCount();
        if (this.modCount != null) {
            if (expectedChanged) {
                Assert.assertFalse(this.modCount.intValue() == modCount);
            } else {
                Assert.assertTrue(this.modCount.intValue() == modCount);
            }
        }
        this.modCount = modCount;
    }
    
    static {
        Field field = null;
        try {
            field = AbstractXMap.class.getDeclaredField("baseEntries");
        } catch (SecurityException ex) {
            Assert.fail("can not find baseElements");
        } catch (NoSuchFieldException ex) {
            Assert.fail("can not find baseElements");
        }
        field.setAccessible(true);
        BASE_ENTRIES_FIELD = field;
    }
    
}
