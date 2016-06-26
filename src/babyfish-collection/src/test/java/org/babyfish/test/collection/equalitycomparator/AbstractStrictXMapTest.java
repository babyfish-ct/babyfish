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

import org.babyfish.collection.MAMap;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XMap;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public abstract class AbstractStrictXMapTest extends AbstractXMapTest {

    private XMap<Element, Element> xmap;
    
    private String eventLog;

    @Override
    protected final ReplacementRule keyReplacementRule() {
        return ReplacementRule.NEW_REFERENCE_WIN;
    }
    
    @Before
    public void initXMapToTestAttachBehaviors() {
        this.xmap = this.createXMap();
        Assert.assertEquals(this.keyReplacementRule(), this.xmap.keyReplacementRule());
        if (this.xmap instanceof MAMap<?, ?>) {
            this.eventLog = "";
            MAMap<Element, Element> maMap = (MAMap<Element, Element>)this.xmap;
            maMap.addMapElementListener(
                    new MapElementListener<Element, Element>() {

                        @Override
                        public void modifying(MapElementEvent<Element, Element> e) throws Throwable {
                            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                                AbstractStrictXMapTest.this.eventLog +=
                                    "-DI[" +
                                    e.getKey(PropertyVersion.DETACH) +
                                    "]" +
                                    e.getValue(PropertyVersion.DETACH);
                            }
                            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                                AbstractStrictXMapTest.this.eventLog +=
                                    "-AI[" +
                                    e.getKey(PropertyVersion.ATTACH) +
                                    "]" +
                                    e.getValue(PropertyVersion.ATTACH);
                            }
                        }

                        @Override
                        public void modified(MapElementEvent<Element, Element> e) throws Throwable {
                            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                                AbstractStrictXMapTest.this.eventLog +=
                                    "-DE[" +
                                    e.getKey(PropertyVersion.DETACH) +
                                    "]" +
                                    e.getValue(PropertyVersion.DETACH);
                            }
                            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                                AbstractStrictXMapTest.this.eventLog +=
                                    "-AE[" +
                                    e.getKey(PropertyVersion.ATTACH) +
                                    "]" +
                                    e.getValue(PropertyVersion.ATTACH);
                            }
                        }
                    });
        } else {
            this.eventLog = null;
        }
    }
    
    @Test
    public void testPut() {
        this.synchronizeAndAssertModCount(false);
        
        Assert.assertNull(this.xmap.put(new Element("001", "Jim"), new Element("Couple-I", "Jim|Kate")));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-AI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}" +
                    "-AE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}", 
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xmap.keySet(), "001");
        Helper.assertElementNames(this.xmap.keySet(), "Jim");
        Helper.assertElementCodes(this.xmap.values(), "Couple-I");
        Helper.assertElementNames(this.xmap.values(), "Jim|Kate");
        
        Element detachedValue = 
            this.xmap.put(new Element("001", "Jim2"), new Element("Couple-I", "Jim2|Kate"));
        Assert.assertNotNull(detachedValue);
        Assert.assertEquals("Couple-I", detachedValue.getCode());
        Assert.assertEquals("Jim|Kate", detachedValue.getName());
        this.synchronizeAndAssertModCount(false);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}" +
                    "-AI[{code:001,name:Jim2}]{code:Couple-I,name:Jim2|Kate}" +
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}" +
                    "-AE[{code:001,name:Jim2}]{code:Couple-I,name:Jim2|Kate}", 
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xmap.keySet(), "001");
        Helper.assertElementNames(this.xmap.keySet(), "Jim2");
        Helper.assertElementCodes(this.xmap.values(), "Couple-I");
        Helper.assertElementNames(this.xmap.values(), "Jim2|Kate");
        
        Assert.assertNull(this.xmap.put(new Element("002", "Kate"), new Element("Couple-I", "Jim2|Kate")));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-AI[{code:002,name:Kate}]{code:Couple-I,name:Jim2|Kate}" +
                    "-AE[{code:002,name:Kate}]{code:Couple-I,name:Jim2|Kate}", 
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xmap.keySet(), "001", "002");
        Helper.assertElementNames(this.xmap.keySet(), "Jim2", "Kate");
        Helper.assertElementCodes(this.xmap.values(), "Couple-I", "Couple-I");
        Helper.assertElementNames(this.xmap.values(), "Jim2|Kate", "Jim2|Kate");
        
        detachedValue = 
            this.xmap.put(new Element("002", "Kate2"), new Element("Couple-I", "Jim2|Kate2"));
        Assert.assertNotNull(detachedValue);
        Assert.assertEquals("Couple-I", detachedValue.getCode());
        Assert.assertEquals("Jim2|Kate", detachedValue.getName());
        this.synchronizeAndAssertModCount(false);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim2|Kate}" +
                    "-AI[{code:002,name:Kate2}]{code:Couple-I,name:Jim2|Kate2}" +
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim2|Kate}" +
                    "-AE[{code:002,name:Kate2}]{code:Couple-I,name:Jim2|Kate2}", 
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xmap.keySet(), "001", "002");
        Helper.assertElementNames(this.xmap.keySet(), "Jim2", "Kate2");
        Helper.assertElementCodes(this.xmap.values(), "Couple-I", "Couple-I");
        Helper.assertElementNames(this.xmap.values(), "Jim2|Kate", "Jim2|Kate2");
    }
    
    @Test
    public void testPutAllByMapWithSameComparatorAndWithoutDuplicatedElementForThisMap() {
        this.testPutAllByMapWithoutDuplicatedElementForThisMap(this.xmap.keyUnifiedComparator());
    }
    
    @Test
    public void testPutAllByMapWithDiffComparatorAndWithoutDuplicatedElementForThisMap() {
        this.testPutAllByMapWithoutDuplicatedElementForThisMap(
                UnifiedComparator.of(Helper.ELEMENT_NAME_EQUALITY_COMPARATOR));
    }
    
    @Test
    public void testPutAllByMapWithDiffComparatorAndWithDuplicatedElementForThisMap() {
        this.synchronizeAndAssertModCount(false);
        
        this.xmap.putAll(
                Helper.prepareXMap(
                        UnifiedComparator.of(Helper.ELEMENT_NAME_EQUALITY_COMPARATOR), 
                        new Element[] {
                            new Element("001", "Jim"),
                            new Element("001", "Jim.1"),
                            new Element("002", "Kate"),
                            new Element("002", "Kate.1"),
                            new Element("003", "Smith"),
                            new Element("003", "Smith.1"),
                            new Element("004", "Linda"),
                            new Element("004", "Linda.1"),
                        }, 
                        new Element[] {
                            new Element("Couple-I", "Jim|Kate"),
                            new Element("Couple-I", "Jim.1|Kate.1"),
                            new Element("Couple-I", "Jim|Kate"),
                            new Element("Couple-I", "Jim.1|Kate.1"),
                            new Element("Couple-II", "Smith|Linda"),
                            new Element("Couple-II", "Smith.1|Linda.1"),
                            new Element("Couple-II", "Smith|Linda"),
                            new Element("Couple-II", "Smith.1|Linda.1"),
                        }));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-AI[{code:001,name:Jim.1}]{code:Couple-I,name:Jim.1|Kate.1}" +
                    "-AI[{code:002,name:Kate.1}]{code:Couple-I,name:Jim.1|Kate.1}" +
                    "-AI[{code:003,name:Smith.1}]{code:Couple-II,name:Smith.1|Linda.1}" +
                    "-AI[{code:004,name:Linda.1}]{code:Couple-II,name:Smith.1|Linda.1}" +
                    "-AE[{code:001,name:Jim.1}]{code:Couple-I,name:Jim.1|Kate.1}" +
                    "-AE[{code:002,name:Kate.1}]{code:Couple-I,name:Jim.1|Kate.1}" +
                    "-AE[{code:003,name:Smith.1}]{code:Couple-II,name:Smith.1|Linda.1}" +
                    "-AE[{code:004,name:Linda.1}]{code:Couple-II,name:Smith.1|Linda.1}",
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xmap.keySet(), "001", "002", "003", "004");
        Helper.assertElementNames(this.xmap.keySet(), "Jim.1", "Kate.1", "Smith.1", "Linda.1");
        Helper.assertElementCodes(this.xmap.values(), "Couple-I", "Couple-I", "Couple-II", "Couple-II");
        Helper.assertElementNames(this.xmap.values(), "Jim.1|Kate.1", "Jim.1|Kate.1", "Smith.1|Linda.1", "Smith.1|Linda.1");
        
        this.xmap.putAll(
                Helper.prepareXMap(
                        UnifiedComparator.of(Helper.ELEMENT_NAME_EQUALITY_COMPARATOR), 
                        new Element[] {
                            new Element("001", "Jim-2"),
                            new Element("001", "Jim.1-2"),
                            new Element("002", "Kate-2"),
                            new Element("002", "Kate.1-2"),
                            new Element("003", "Smith-2"),
                            new Element("003", "Smith.1-2"),
                            new Element("004", "Linda-2"),
                            new Element("004", "Linda.1-2"),
                        }, 
                        new Element[] {
                            new Element("Couple-I", "Jim-2|Kate-2"),
                            new Element("Couple-I", "Jim.1-2|Kate.1-2"),
                            new Element("Couple-I", "Jim-2|Kate-2"),
                            new Element("Couple-I", "Jim.1-2|Kate.1-2"),
                            new Element("Couple-II", "Smith-2|Linda-2"),
                            new Element("Couple-II", "Smith.1-2|Linda.1-2"),
                            new Element("Couple-II", "Smith-2|Linda-2"),
                            new Element("Couple-II", "Smith.1-2|Linda.1-2"),
                        }));
        this.synchronizeAndAssertModCount(false);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[{code:001,name:Jim.1}]{code:Couple-I,name:Jim.1|Kate.1}" +
                    "-AI[{code:001,name:Jim.1-2}]{code:Couple-I,name:Jim.1-2|Kate.1-2}" +
                    "-DI[{code:002,name:Kate.1}]{code:Couple-I,name:Jim.1|Kate.1}" +
                    "-AI[{code:002,name:Kate.1-2}]{code:Couple-I,name:Jim.1-2|Kate.1-2}" +
                    "-DI[{code:003,name:Smith.1}]{code:Couple-II,name:Smith.1|Linda.1}" +
                    "-AI[{code:003,name:Smith.1-2}]{code:Couple-II,name:Smith.1-2|Linda.1-2}" +
                    "-DI[{code:004,name:Linda.1}]{code:Couple-II,name:Smith.1|Linda.1}" +
                    "-AI[{code:004,name:Linda.1-2}]{code:Couple-II,name:Smith.1-2|Linda.1-2}" +
                    "-DE[{code:001,name:Jim.1}]{code:Couple-I,name:Jim.1|Kate.1}" +
                    "-AE[{code:001,name:Jim.1-2}]{code:Couple-I,name:Jim.1-2|Kate.1-2}" +
                    "-DE[{code:002,name:Kate.1}]{code:Couple-I,name:Jim.1|Kate.1}" +
                    "-AE[{code:002,name:Kate.1-2}]{code:Couple-I,name:Jim.1-2|Kate.1-2}" +
                    "-DE[{code:003,name:Smith.1}]{code:Couple-II,name:Smith.1|Linda.1}" +
                    "-AE[{code:003,name:Smith.1-2}]{code:Couple-II,name:Smith.1-2|Linda.1-2}" +
                    "-DE[{code:004,name:Linda.1}]{code:Couple-II,name:Smith.1|Linda.1}" +
                    "-AE[{code:004,name:Linda.1-2}]{code:Couple-II,name:Smith.1-2|Linda.1-2}",
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xmap.keySet(), "001", "002", "003", "004");
        Helper.assertElementNames(this.xmap.keySet(), "Jim.1-2", "Kate.1-2", "Smith.1-2", "Linda.1-2");
        Helper.assertElementCodes(this.xmap.values(), "Couple-I", "Couple-I", "Couple-II", "Couple-II");
        Helper.assertElementNames(this.xmap.values(), "Jim.1-2|Kate.1-2", "Jim.1-2|Kate.1-2", "Smith.1-2|Linda.1-2", "Smith.1-2|Linda.1-2");
    }
    
    private void testPutAllByMapWithoutDuplicatedElementForThisMap(UnifiedComparator<? super Element> keyUnifiedComparator) {
        this.synchronizeAndAssertModCount(false);
        
        this.xmap.putAll(Helper.prepareXMap(
                keyUnifiedComparator, 
                new Element[] {
                    new Element("001", "Jim"),
                    new Element("002", "Kate"),
                    new Element("003", "Smith"),
                    new Element("004", "Linda"),
                }, 
                new Element[] {
                    new Element("Couple-I", "Jim|Kate"),
                    new Element("Couple-I", "Jim|Kate"),
                    new Element("Couple-II", "Smith|Linda"),
                    new Element("Couple-II", "Smith|Linda"),
                }));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-AI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}" +
                    "-AI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}" +
                    "-AI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}" +
                    "-AI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}" + 
                    "-AE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}" +
                    "-AE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}" +
                    "-AE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}" +
                    "-AE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}",
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xmap.keySet(), "001", "002", "003", "004");
        Helper.assertElementNames(this.xmap.keySet(), "Jim", "Kate", "Smith", "Linda");
        Helper.assertElementCodes(this.xmap.values(), "Couple-I", "Couple-I", "Couple-II", "Couple-II");
        Helper.assertElementNames(this.xmap.values(), "Jim|Kate", "Jim|Kate", "Smith|Linda", "Smith|Linda");
        
        this.xmap.putAll(Helper.prepareXMap(
                keyUnifiedComparator, 
                new Element[] {
                    new Element("001", "Jim2"),
                    new Element("002", "Kate2"),
                    new Element("005", "Sam"),
                    new Element("006", "Lucy"),
                }, 
                new Element[] {
                    new Element("Couple-I", "Jim2|Kate2"),
                    new Element("Couple-I", "Jim2|Kate2"),
                    new Element("Couple-III", "Sam|Lucy"),
                    new Element("Couple-III", "Sam|Lucy"),
                }));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}" +
                    "-AI[{code:001,name:Jim2}]{code:Couple-I,name:Jim2|Kate2}" +
                    "-DI[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}" +
                    "-AI[{code:002,name:Kate2}]{code:Couple-I,name:Jim2|Kate2}" +
                    "-AI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}" +
                    "-AI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}" +
                    "-DE[{code:001,name:Jim}]{code:Couple-I,name:Jim|Kate}" +
                    "-AE[{code:001,name:Jim2}]{code:Couple-I,name:Jim2|Kate2}" +
                    "-DE[{code:002,name:Kate}]{code:Couple-I,name:Jim|Kate}" +
                    "-AE[{code:002,name:Kate2}]{code:Couple-I,name:Jim2|Kate2}" +
                    "-AE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}" +
                    "-AE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}",
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xmap.keySet(), "001", "002", "003", "004", "005", "006");
        Helper.assertElementNames(this.xmap.keySet(), "Jim2", "Kate2", "Smith", "Linda", "Sam", "Lucy");
        Helper.assertElementCodes(this.xmap.values(), "Couple-I", "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III");
        Helper.assertElementNames(this.xmap.values(), "Jim2|Kate2", "Jim2|Kate2", "Smith|Linda", "Smith|Linda", "Sam|Lucy", "Sam|Lucy");
        
        this.xmap.putAll(Helper.prepareXMap(
                keyUnifiedComparator, 
                new Element[] {
                    new Element("003", "Smith2"),
                    new Element("004", "Linda2"),
                    new Element("005", "Sam2"),
                    new Element("006", "Lucy2"),
                }, 
                new Element[] {
                    new Element("Couple-II", "Smith2|Linda2"),
                    new Element("Couple-II", "Smith2|Linda2"),
                    new Element("Couple-III", "Sam2|Lucy2"),
                    new Element("Couple-III", "Sam2|Lucy2"),
                }));
        this.synchronizeAndAssertModCount(false);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}" +
                    "-AI[{code:003,name:Smith2}]{code:Couple-II,name:Smith2|Linda2}" +
                    "-DI[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}" +
                    "-AI[{code:004,name:Linda2}]{code:Couple-II,name:Smith2|Linda2}" +
                    "-DI[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}" +
                    "-AI[{code:005,name:Sam2}]{code:Couple-III,name:Sam2|Lucy2}" +
                    "-DI[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}" +
                    "-AI[{code:006,name:Lucy2}]{code:Couple-III,name:Sam2|Lucy2}" +
                    "-DE[{code:003,name:Smith}]{code:Couple-II,name:Smith|Linda}" +
                    "-AE[{code:003,name:Smith2}]{code:Couple-II,name:Smith2|Linda2}" +
                    "-DE[{code:004,name:Linda}]{code:Couple-II,name:Smith|Linda}" +
                    "-AE[{code:004,name:Linda2}]{code:Couple-II,name:Smith2|Linda2}" +
                    "-DE[{code:005,name:Sam}]{code:Couple-III,name:Sam|Lucy}" +
                    "-AE[{code:005,name:Sam2}]{code:Couple-III,name:Sam2|Lucy2}" +
                    "-DE[{code:006,name:Lucy}]{code:Couple-III,name:Sam|Lucy}" +
                    "-AE[{code:006,name:Lucy2}]{code:Couple-III,name:Sam2|Lucy2}",
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xmap.keySet(), "001", "002", "003", "004", "005", "006");
        Helper.assertElementNames(this.xmap.keySet(), "Jim2", "Kate2", "Smith2", "Linda2", "Sam2", "Lucy2");
        Helper.assertElementCodes(this.xmap.values(), "Couple-I", "Couple-I", "Couple-II", "Couple-II", "Couple-III", "Couple-III");
        Helper.assertElementNames(this.xmap.values(), "Jim2|Kate2", "Jim2|Kate2", "Smith2|Linda2", "Smith2|Linda2", "Sam2|Lucy2", "Sam2|Lucy2");
    }
    
    private void synchronizeAndAssertModCount(boolean expectedChanged) {
        this.synchronizeAndAssertModCount(this.xmap, expectedChanged);
    }
    
    private void resetEventLog() {
        if (this.eventLog != null) {
            this.eventLog = "";
        }
    }

}
