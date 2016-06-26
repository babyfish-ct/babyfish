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

import org.babyfish.collection.MASet;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XSet;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public abstract class AbstractStrictXSetTest extends AbstractXSetTest {

    private XSet<Element> xset;
    
    private String eventLog;

    @Override
    protected final ReplacementRule replacementRule() {
        return ReplacementRule.NEW_REFERENCE_WIN;
    }
    
    @Before
    public void initXSetToTestAttachBehaviors() {
        this.xset = this.createXSet();
        Assert.assertEquals(this.replacementRule(), this.xset.replacementRule());
        if (this.xset instanceof MASet<?>) {
            MASet<Element> maSet = (MASet<Element>)this.xset;
            this.eventLog = "";
            maSet.addElementListener(
                    new ElementListener<Element>() {

                        @Override
                        public void modifying(ElementEvent<Element> e) throws Throwable {
                            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                                AbstractStrictXSetTest.this.eventLog += "-DI" + e.getElement(PropertyVersion.DETACH);
                            }
                            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                                AbstractStrictXSetTest.this.eventLog += "-AI" + e.getElement(PropertyVersion.ATTACH);
                            }
                        }

                        @Override
                        public void modified(ElementEvent<Element> e) throws Throwable {
                            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                                AbstractStrictXSetTest.this.eventLog += "-DE" + e.getElement(PropertyVersion.DETACH);
                            }
                            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                                AbstractStrictXSetTest.this.eventLog += "-AE" + e.getElement(PropertyVersion.ATTACH);
                            }
                        }
                        
                    });
        } else {
            this.eventLog = null;
        }
    }

    @Test
    public void testAdd() {
        this.synchronizeAndAssertModCount(false);
        
        Assert.assertTrue(this.xset.add(new Element("001", "Jim")));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-AI{code:001,name:Jim}-AE{code:001,name:Jim}", 
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xset, "001");
        Helper.assertElementNames(this.xset, "Jim");
        
        Assert.assertFalse(this.xset.add(new Element("001", "Jim2")));
        this.synchronizeAndAssertModCount(false);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI{code:001,name:Jim}-AI{code:001,name:Jim2}" +
                    "-DE{code:001,name:Jim}-AE{code:001,name:Jim2}", 
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xset, "001");
        Helper.assertElementNames(this.xset, "Jim2");
        
        Assert.assertTrue(this.xset.add(new Element("002", "Kate")));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-AI{code:002,name:Kate}-AE{code:002,name:Kate}", 
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xset, "001", "002");
        Helper.assertElementNames(this.xset, "Jim2", "Kate");
        
        Assert.assertFalse(this.xset.add(new Element("002", "Kate2")));
        this.synchronizeAndAssertModCount(false);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI{code:002,name:Kate}-AI{code:002,name:Kate2}" +
                    "-DE{code:002,name:Kate}-AE{code:002,name:Kate2}", 
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xset, "001", "002");
        Helper.assertElementNames(this.xset, "Jim2", "Kate2");
    }
    
    @Test
    public void testAddAllByList() {
        this.testAddAllNotBySetWithDiffComparator0(
                new PrepareXCollectionHandler<Element>() {
                    @Override
                    public XCollection<Element> prepareXCollection(Element... elements) {
                        return Helper.prepareXList(null, elements);
                    }
                });
    }
    
    @Test
    public void testAddAllBySetWithSameComparator() {
        this.testAddAllNotBySetWithDiffComparator0(
                new PrepareXCollectionHandler<Element>() {
                    @Override
                    public XCollection<Element> prepareXCollection(Element... elements) {
                        return Helper.prepareXSet(AbstractStrictXSetTest.this.xset.unifiedComparator(), elements);
                    }
                });
    }
    
    @Test
    public void testAddAllBySetWithDiffComparator() {
        Assert.assertTrue(
                this.xset.addAll(
                        Helper.prepareXOrderedSet(
                                Helper.ELEMENT_NAME_EQUALITY_COMPARATOR,
                                new Element("InvalidCode-001", "InvalidName1"),
                                new Element("InvalidCode-001", "InvalidName2"),
                                new Element("InvalidCode-002", "InvalidName3"),
                                new Element("InvalidCode-002", "InvalidName4"),
                                new Element("InvalidCode-001", "InvalidName1"),
                                new Element("InvalidCode-001", "InvalidName2"),
                                new Element("InvalidCode-002", "InvalidName3"),
                                new Element("InvalidCode-002", "InvalidName4"))));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-AI{code:InvalidCode-001,name:InvalidName2}" +
                    "-AI{code:InvalidCode-002,name:InvalidName4}" +
                    "-AE{code:InvalidCode-001,name:InvalidName2}" +
                    "-AE{code:InvalidCode-002,name:InvalidName4}", 
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xset, "InvalidCode-001", "InvalidCode-002");
        Helper.assertElementNames(this.xset, "InvalidName2", "InvalidName4");
    }
    
    private void testAddAllNotBySetWithDiffComparator0(PrepareXCollectionHandler<Element> pxcHandler) {
        this.synchronizeAndAssertModCount(false);
        
        Assert.assertTrue(
                this.xset.addAll(
                        pxcHandler.prepareXCollection(
                                new Element("001", "Jim"),
                                new Element("002", "Kate"),
                                new Element("003", "Smith"),
                                new Element("004", "Linda"),
                                new Element("001", "Jim"),
                                new Element("002", "Kate"),
                                new Element("003", "Smith"),
                                new Element("004", "Linda"))));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-AI{code:001,name:Jim}-AI{code:002,name:Kate}-AI{code:003,name:Smith}-AI{code:004,name:Linda}" +
                    "-AE{code:001,name:Jim}-AE{code:002,name:Kate}-AE{code:003,name:Smith}-AE{code:004,name:Linda}", 
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xset, "001", "002", "003", "004");
        Helper.assertElementNames(this.xset, "Jim", "Kate", "Smith", "Linda");
        
        Assert.assertFalse(
                this.xset.addAll(
                        pxcHandler.prepareXCollection(
                                new Element("001", "Jim2"),
                                new Element("002", "Kate2"),
                                new Element("003", "Smith2"),
                                new Element("004", "Linda2"),
                                new Element("001", "Jim2"),
                                new Element("002", "Kate2"),
                                new Element("003", "Smith2"),
                                new Element("004", "Linda2"))));
        this.synchronizeAndAssertModCount(false);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI{code:001,name:Jim}-AI{code:001,name:Jim2}" +
                    "-DI{code:002,name:Kate}-AI{code:002,name:Kate2}" +
                    "-DI{code:003,name:Smith}-AI{code:003,name:Smith2}" +
                    "-DI{code:004,name:Linda}-AI{code:004,name:Linda2}" +
                    "-DE{code:001,name:Jim}-AE{code:001,name:Jim2}" +
                    "-DE{code:002,name:Kate}-AE{code:002,name:Kate2}" +
                    "-DE{code:003,name:Smith}-AE{code:003,name:Smith2}" +
                    "-DE{code:004,name:Linda}-AE{code:004,name:Linda2}", 
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xset, "001", "002", "003", "004");
        Helper.assertElementNames(this.xset, "Jim2", "Kate2", "Smith2", "Linda2");
        
        Assert.assertTrue(
                this.xset.addAll(
                        pxcHandler.prepareXCollection(
                                new Element("001", "Jim3"),
                                new Element("002", "Kate3"),
                                new Element("005", "Sam3"),
                                new Element("006", "Lucy3"),
                                new Element("001", "Jim3"),
                                new Element("002", "Kate3"),
                                new Element("005", "Sam3"),
                                new Element("006", "Lucy3"))));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI{code:001,name:Jim2}-AI{code:001,name:Jim3}-DI{code:002,name:Kate2}-AI{code:002,name:Kate3}" +
                    "-AI{code:005,name:Sam3}-AI{code:006,name:Lucy3}" +
                    "-DE{code:001,name:Jim2}-AE{code:001,name:Jim3}-DE{code:002,name:Kate2}-AE{code:002,name:Kate3}" +
                    "-AE{code:005,name:Sam3}-AE{code:006,name:Lucy3}", 
                    this.eventLog);
            this.resetEventLog();
        }
        Helper.assertElementCodes(this.xset, "001", "002", "003", "004", "005", "006");
        Helper.assertElementNames(this.xset, "Jim3", "Kate3", "Smith2", "Linda2", "Sam3", "Lucy3");     
    }
    
    private void synchronizeAndAssertModCount(boolean expectedChanged) {
        this.synchronizeAndAssertModCount(this.xset, expectedChanged);
    }
    
    private void resetEventLog() {
        if (this.eventLog != null) {
            this.eventLog = "";
        }
    }

}
