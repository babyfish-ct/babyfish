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
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.babyfish.collection.MASet;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.XSet;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.spi.AbstractXSet;
import org.babyfish.collection.spi.base.BaseEntries;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public abstract class AbstractXSetTest {
    
    private static final Field BASE_ENTRIES_FIELD;
    
    private XSet<Element> xset;

    protected abstract XSet<Element> createXSet();
    
    protected abstract ReplacementRule replacementRule();
    
    private List<String> preEvents;
    
    private List<String> postEvents;
    
    private Integer modCount;
    
    @Before
    public void initXSetToTestDetachBehaviors() {
        this.xset = this.createXSet();
        this.modCount = null;
        Assert.assertEquals(this.replacementRule(), this.xset.replacementRule());
        if (this.xset.replacementRule() == ReplacementRule.NEW_REFERENCE_WIN) {
            this.xset.add(new Element("001", "Unknown"));
            this.xset.add(new Element("002", "Unknown"));
            this.xset.add(new Element("003", "Unknown"));
            this.xset.add(new Element("004", "Unknown"));
            this.xset.add(new Element("005", "Unknown"));
            this.xset.add(new Element("006", "Unknown"));
            this.xset.add(new Element("007", "Unknown"));
            this.xset.add(new Element("008", "Unknown"));
        }
        this.xset.addAll(
                Helper.prepareXList(
                        null, 
                        new Element("001", "Jim"),
                        new Element("002", "Kate"),
                        new Element("003", "Smith"),
                        new Element("004", "Linda"),
                        new Element("005", "Sam"),
                        new Element("006", "Lucy"),
                        new Element("007", "Jhon"),
                        new Element("008", "Mary")));
        Helper.assertElementCodes(
                this.xset, 
                "001", "002", "003", "004", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xset, 
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
        this.modCount = null;
        if (this.xset instanceof MASet<?>) {
            this.preEvents = new ArrayList<String>();
            this.postEvents = new ArrayList<String>();
            MASet<Element> maSet = (MASet<Element>)this.xset;
            maSet.addElementListener(
                    new ElementListener<Element>() {
                        @Override
                        public void modifying(ElementEvent<Element> e) throws Throwable {
                            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                                AbstractXSetTest.this.preEvents.add("-DI" + e.getElement(PropertyVersion.DETACH));
                            }
                        }
    
                        @Override
                        public void modified(ElementEvent<Element> e) throws Throwable {
                            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                                AbstractXSetTest.this.postEvents.add("-DE" + e.getElement(PropertyVersion.DETACH));
                            }
                        }
                    });
        }
    }

    @Test
    public void testRemoveFirstSuccess() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(this.xset.remove(new Element("001", "invalidName")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(false, this.preEvents, "-DI{code:001,name:Jim}");
            Helper.assertCollection(false, this.postEvents, "-DE{code:001,name:Jim}");
        }
        Helper.assertElementCodes(
                this.xset, 
                "002", "003", "004", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xset,              
                "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
    }
    
    @Test
    public void testRemoveLastSuccess() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(this.xset.remove(new Element("008", "invalidName")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(false, this.preEvents, "-DI{code:008,name:Mary}");
            Helper.assertCollection(false, this.postEvents, "-DE{code:008,name:Mary}");
        }
        Helper.assertElementCodes(
                this.xset, 
                "001", "002", "003", "004", "005", "006", "007");
        Helper.assertElementNames(
                this.xset,              
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon");
    }
    
    @Test
    public void testRemoveMiddleSuccess() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(this.xset.remove(new Element("004", "invalidName")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(false, this.preEvents, "-DI{code:004,name:Linda}");
            Helper.assertCollection(false, this.postEvents, "-DE{code:004,name:Linda}");
        }
        Helper.assertElementCodes(
                this.xset, 
                "001", "002", "003", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xset,              
                "Jim", "Kate", "Smith", "Sam", "Lucy", "Jhon", "Mary");
    }
    
    @Test
    public void testRemoveFailed() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertFalse(this.xset.remove(new Element("invalidCode", "Jim")));
        this.synchronizeAndAssertModCount(false);
        if (this.preEvents != null) {
            Assert.assertTrue(this.preEvents.isEmpty());
            Assert.assertTrue(this.postEvents.isEmpty());
        }
        Helper.assertElementCodes(
                this.xset, 
                "001", "002", "003", "004", "005", "006", "007", "008");
        Helper.assertElementNames(
                this.xset,              
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
    }
    
    @Test
    public void testRemove7531ByCollectionManagedByCode() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this.xset.removeAll(
                        Helper.createElementListManagedByCodeByCodes(
                                "007", "005", "003", "001", "005", "001")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI{code:001,name:Jim}",
                    "-DI{code:003,name:Smith}",
                    "-DI{code:005,name:Sam}",
                    "-DI{code:007,name:Jhon}");
        }
        Helper.assertElementCodes(
                this.xset, 
                "002", "004", "006", "008");
        Helper.assertElementNames(
                this.xset,              
                "Kate", "Linda", "Lucy", "Mary");
    }
    
    @Test
    public void testRemove7531ByCollectionManagedByName() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this.xset.removeAll(
                        Helper.createElementListManagedByNameByCodes(
                                "007", "005", "003", "001", "005", "001")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI{code:001,name:Jim}",
                    "-DI{code:003,name:Smith}",
                    "-DI{code:005,name:Sam}",
                    "-DI{code:007,name:Jhon}");
        }
        Helper.assertElementCodes(
                this.xset, 
                "002", "004", "006", "008");
        Helper.assertElementNames(
                this.xset,              
                "Kate", "Linda", "Lucy", "Mary");
    }
    
    @Test
    public void testRetain7531ByCollectionManagedByCode() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this.xset.retainAll(
                        Helper.createElementListManagedByCodeByCodes(
                                "007", "005", "003", "001", "005", "001")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI{code:002,name:Kate}",
                    "-DI{code:004,name:Linda}",
                    "-DI{code:006,name:Lucy}",
                    "-DI{code:008,name:Mary}");
        }
        Helper.assertElementCodes(
                this.xset, 
                "001", "003", "005", "007");
        Helper.assertElementNames(
                this.xset,              
                "Jim", "Smith", "Sam", "Jhon");
    }
    
    @Test
    public void testRetain7531ByCollectionManagedByName() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this.xset.retainAll(
                        Helper.createElementListManagedByNameByCodes(
                                "007", "005", "003", "001", "005", "001")));
        this.synchronizeAndAssertModCount(true);
        if (this.preEvents != null) {
            Helper.assertCollection(
                    false, 
                    this.preEvents, 
                    "-DI{code:002,name:Kate}",
                    "-DI{code:004,name:Linda}",
                    "-DI{code:006,name:Lucy}",
                    "-DI{code:008,name:Mary}");
        }
        Helper.assertElementCodes(
                this.xset, 
                "001", "003", "005", "007");
        Helper.assertElementNames(
                this.xset,              
                "Jim", "Smith", "Sam", "Jhon");
    }
    
    private void synchronizeAndAssertModCount(boolean expectedChanged) {
        this.synchronizeAndAssertModCount(this.xset, expectedChanged);
    }

    protected void synchronizeAndAssertModCount(XSet<Element> xset, boolean expectedChanged) {
        BaseEntries<?, ?> baseEntries = null;
        try {
            baseEntries = (BaseEntries<?, ?>)BASE_ENTRIES_FIELD.get(xset);
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
            field = AbstractXSet.class.getDeclaredField("baseEntries");
        } catch (SecurityException ex) {
            Assert.fail("can not find baseElements");
        } catch (NoSuchFieldException ex) {
            Assert.fail("can not find baseElements");
        }
        field.setAccessible(true);
        BASE_ENTRIES_FIELD = field;
    }
    
}
