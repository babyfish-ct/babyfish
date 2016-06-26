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

import junit.framework.Assert;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.MAList;
import org.babyfish.collection.XList;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.event.ListElementListener;
import org.babyfish.collection.spi.AbstractXList;
import org.babyfish.collection.spi.base.BaseElements;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public abstract class AbstractXListTest {

    private static final Field BASE_ELEMENTS_FIELD;
    
    protected abstract XList<Element> createXList(
            EqualityComparator<Element> equalityComparator);
    
    private XList<Element> xList;
    
    private XList<Element> xSubList;
    
    private String eventLog;
    
    private Integer modCount;
    
    @Before
    public void init() {
        this.xList = this.createXList(Helper.ELEMENT_CODE_EQUALITY_COMPARATOR);
        xList.add(new Element("I", "Jim"));
        xList.add(new Element("II", "Kate"));
        xList.add(new Element("III", "Smith"));
        xList.add(new Element("IV", "Linda"));
        xList.add(new Element("V", "Sam"));
        xList.add(new Element("VI", "Lucy"));
        xList.add(new Element("VII", "Jhon"));
        xList.add(new Element("VIII", "Mary"));
        xList.add(new Element("I", "Jim"));
        xList.add(new Element("II", "Kate"));
        xList.add(new Element("III", "Smith"));
        xList.add(new Element("IV", "Linda"));
        xList.add(new Element("V", "Sam"));
        xList.add(new Element("VI", "Lucy"));
        xList.add(new Element("VII", "Jhon"));
        xList.add(new Element("VIII", "Mary"));
        xList.add(new Element("I", "Jim"));
        xList.add(new Element("II", "Kate"));
        xList.add(new Element("III", "Smith"));
        xList.add(new Element("IV", "Linda"));
        xList.add(new Element("V", "Sam"));
        xList.add(new Element("VI", "Lucy"));
        xList.add(new Element("VII", "Jhon"));
        xList.add(new Element("VIII", "Mary"));
        
        this.xSubList = 
            this.xList
            .subList(1, 23)
            .subList(1, 21)
            .subList(1, 19);
        
        Helper.assertElementCodes(
                this.xList, 
                "I", "II", "III", "IV", "V", "VI", "VII", "VIII",
                "I", "II", "III", "IV", "V", "VI", "VII", "VIII",
                "I", "II", "III", "IV", "V", "VI", "VII", "VIII");
        Helper.assertElementNames(
                this.xList, 
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary",
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary",
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xSubList, 
                "IV", "V", "VI", "VII", "VIII",
                "I", "II", "III", "IV", "V", "VI", "VII", "VIII",
                "I", "II", "III", "IV", "V");
        Helper.assertElementNames(
                this.xSubList, 
                "Linda", "Sam", "Lucy", "Jhon", "Mary",
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary",
                "Jim", "Kate", "Smith", "Linda", "Sam");
        
        if (this.xSubList instanceof MAList<?>) {
            this.eventLog = "";
            MAList<Element> maList = (MAList<Element>)this.xSubList;
            maList.addListElementListener(
                    new ListElementListener<Element>() {
                        @Override
                        public void modifying(ListElementEvent<Element> e) throws Throwable {
                            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                                AbstractXListTest.this.eventLog += 
                                    "-DI[" + 
                                    e.getIndex(PropertyVersion.DETACH) + 
                                    ']' + 
                                    e.getElement(PropertyVersion.DETACH);
                            }
                            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                                AbstractXListTest.this.eventLog += 
                                    "-AI[" + 
                                    e.getIndex(PropertyVersion.ATTACH) + 
                                    ']' + 
                                    e.getElement(PropertyVersion.ATTACH);
                            }
                        }

                        @Override
                        public void modified(ListElementEvent<Element> e) throws Throwable {
                            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                                AbstractXListTest.this.eventLog += 
                                    "-DE[" + 
                                    e.getIndex(PropertyVersion.DETACH) + 
                                    ']' + 
                                    e.getElement(PropertyVersion.DETACH);
                            }
                            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                                AbstractXListTest.this.eventLog += 
                                    "-AE[" + 
                                    e.getIndex(PropertyVersion.ATTACH) + 
                                    ']' + 
                                    e.getElement(PropertyVersion.ATTACH);
                            }
                        }
                    });
        } else {
            this.eventLog = null;
        }
    }
    
    @Test
    public void testRemoveSuccess() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(this.xSubList.remove(new Element("I", "invalidName")));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[5]{code:I,name:Jim}" +
                    "-DE[5]{code:I,name:Jim}", 
                    this.eventLog);
        }
        Helper.assertElementCodes(
                this.xList, 
                "I", "II", "III", "IV", "V", "VI", "VII", "VIII",
                "II", "III", "IV", "V", "VI", "VII", "VIII",
                "I", "II", "III", "IV", "V", "VI", "VII", "VIII");
        Helper.assertElementNames(
                this.xList, 
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary",
                "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary",
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xSubList, 
                "IV", "V", "VI", "VII", "VIII",
                "II", "III", "IV", "V", "VI", "VII", "VIII",
                "I", "II", "III", "IV", "V");
        Helper.assertElementNames(
                this.xSubList, 
                "Linda", "Sam", "Lucy", "Jhon", "Mary",
                "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary",
                "Jim", "Kate", "Smith", "Linda", "Sam");
    }
    
    @Test
    public void testRemoveFailed() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertFalse(this.xSubList.remove(new Element("invalidCode", "Jim")));
        this.synchronizeAndAssertModCount(false);
        if (this.eventLog != null) {
            Assert.assertEquals("", this.eventLog);
        }
        Helper.assertElementCodes(
                this.xList, 
                "I", "II", "III", "IV", "V", "VI", "VII", "VIII",
                "I", "II", "III", "IV", "V", "VI", "VII", "VIII",
                "I", "II", "III", "IV", "V", "VI", "VII", "VIII");
        Helper.assertElementNames(
                this.xList, 
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary",
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary",
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xSubList, 
                "IV", "V", "VI", "VII", "VIII",
                "I", "II", "III", "IV", "V", "VI", "VII", "VIII",
                "I", "II", "III", "IV", "V");
        Helper.assertElementNames(
                this.xSubList, 
                "Linda", "Sam", "Lucy", "Jhon", "Mary",
                "Jim", "Kate", "Smith", "Linda", "Sam", "Lucy", "Jhon", "Mary",
                "Jim", "Kate", "Smith", "Linda", "Sam");
    }
    
    @Test
    public void testRemove7531ByCollectionManagedByCode() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this.xSubList.removeAll(
                        Helper.createElementListManagedByCodeByCodes(
                                "VII", "V", "III", "I", "V", "I")));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[1]{code:V,name:Sam}" +
                    "-DI[3]{code:VII,name:Jhon}" +
                    "-DI[5]{code:I,name:Jim}" +
                    "-DI[7]{code:III,name:Smith}" +
                    "-DI[9]{code:V,name:Sam}" +
                    "-DI[11]{code:VII,name:Jhon}" +
                    "-DI[13]{code:I,name:Jim}" +
                    "-DI[15]{code:III,name:Smith}" +
                    "-DI[17]{code:V,name:Sam}" +
                    "-DE[1]{code:V,name:Sam}" +
                    "-DE[3]{code:VII,name:Jhon}" +
                    "-DE[5]{code:I,name:Jim}" +
                    "-DE[7]{code:III,name:Smith}" +
                    "-DE[9]{code:V,name:Sam}" +
                    "-DE[11]{code:VII,name:Jhon}" +
                    "-DE[13]{code:I,name:Jim}" +
                    "-DE[15]{code:III,name:Smith}" +
                    "-DE[17]{code:V,name:Sam}",
                    this.eventLog);
        }
        Helper.assertElementCodes(
                this.xList, 
                "I", "II", "III", "IV", "VI", "VIII",
                "II", "IV", "VI", "VIII",
                "II", "IV", "VI", "VII", "VIII");
        Helper.assertElementNames(
                this.xList, 
                "Jim", "Kate", "Smith", "Linda", "Lucy", "Mary",
                "Kate", "Linda", "Lucy", "Mary",
                "Kate", "Linda", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xSubList, 
                "IV", "VI", "VIII",
                "II", "IV", "VI", "VIII",
                "II", "IV");
        Helper.assertElementNames(
                this.xSubList, 
                "Linda", "Lucy", "Mary",
                "Kate", "Linda", "Lucy", "Mary",
                "Kate", "Linda");
    }
    
    @Test
    public void testRemove7531ByCollectionManagedByName() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this.xSubList.removeAll(
                        Helper.createElementListManagedByNameByCodes(
                                "VII", "V", "III", "I", "V", "I")));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[1]{code:V,name:Sam}" +
                    "-DI[3]{code:VII,name:Jhon}" +
                    "-DI[5]{code:I,name:Jim}" +
                    "-DI[7]{code:III,name:Smith}" +
                    "-DI[9]{code:V,name:Sam}" +
                    "-DI[11]{code:VII,name:Jhon}" +
                    "-DI[13]{code:I,name:Jim}" +
                    "-DI[15]{code:III,name:Smith}" +
                    "-DI[17]{code:V,name:Sam}" +
                    "-DE[1]{code:V,name:Sam}" +
                    "-DE[3]{code:VII,name:Jhon}" +
                    "-DE[5]{code:I,name:Jim}" +
                    "-DE[7]{code:III,name:Smith}" +
                    "-DE[9]{code:V,name:Sam}" +
                    "-DE[11]{code:VII,name:Jhon}" +
                    "-DE[13]{code:I,name:Jim}" +
                    "-DE[15]{code:III,name:Smith}" +
                    "-DE[17]{code:V,name:Sam}",
                    this.eventLog);
        }
        Helper.assertElementCodes(
                this.xList, 
                "I", "II", "III", "IV", "VI", "VIII",
                "II", "IV", "VI", "VIII",
                "II", "IV", "VI", "VII", "VIII");
        Helper.assertElementNames(
                this.xList, 
                "Jim", "Kate", "Smith", "Linda", "Lucy", "Mary",
                "Kate", "Linda", "Lucy", "Mary",
                "Kate", "Linda", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xSubList, 
                "IV", "VI", "VIII",
                "II", "IV", "VI", "VIII",
                "II", "IV");
        Helper.assertElementNames(
                this.xSubList, 
                "Linda", "Lucy", "Mary",
                "Kate", "Linda", "Lucy", "Mary",
                "Kate", "Linda");
    }
    
    @Test
    public void testRemove8462ByCollectionManagedByCode() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this.xSubList.removeAll(
                        Helper.createElementListManagedByCodeByCodes(
                                "VIII", "VI", "IV", "II", "VI", "II")));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[0]{code:IV,name:Linda}" +
                    "-DI[2]{code:VI,name:Lucy}" +
                    "-DI[4]{code:VIII,name:Mary}" +
                    "-DI[6]{code:II,name:Kate}" +
                    "-DI[8]{code:IV,name:Linda}" +
                    "-DI[10]{code:VI,name:Lucy}" +
                    "-DI[12]{code:VIII,name:Mary}" +
                    "-DI[14]{code:II,name:Kate}" +
                    "-DI[16]{code:IV,name:Linda}" +
                    "-DE[0]{code:IV,name:Linda}" +
                    "-DE[2]{code:VI,name:Lucy}" +
                    "-DE[4]{code:VIII,name:Mary}" +
                    "-DE[6]{code:II,name:Kate}" +
                    "-DE[8]{code:IV,name:Linda}" +
                    "-DE[10]{code:VI,name:Lucy}" +
                    "-DE[12]{code:VIII,name:Mary}" +
                    "-DE[14]{code:II,name:Kate}" +
                    "-DE[16]{code:IV,name:Linda}",
                    this.eventLog);
        }
        Helper.assertElementCodes(
                this.xList, 
                "I", "II", "III", "V", "VII",
                "I", "III", "V", "VII",
                "I", "III", "V", "VI", "VII", "VIII");
        Helper.assertElementNames(
                this.xList, 
                "Jim", "Kate", "Smith", "Sam", "Jhon",
                "Jim", "Smith", "Sam", "Jhon",
                "Jim", "Smith", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xSubList, 
                "V", "VII",
                "I", "III", "V", "VII",
                "I", "III", "V");
        Helper.assertElementNames(
                this.xSubList, 
                "Sam", "Jhon",
                "Jim", "Smith", "Sam", "Jhon",
                "Jim", "Smith", "Sam");
    }
    
    @Test
    public void testRemove8462ByCollectionManagedByName() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this.xSubList.removeAll(
                        Helper.createElementListManagedByNameByCodes(
                                "VIII", "VI", "IV", "II", "VI", "II")));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[0]{code:IV,name:Linda}" +
                    "-DI[2]{code:VI,name:Lucy}" +
                    "-DI[4]{code:VIII,name:Mary}" +
                    "-DI[6]{code:II,name:Kate}" +
                    "-DI[8]{code:IV,name:Linda}" +
                    "-DI[10]{code:VI,name:Lucy}" +
                    "-DI[12]{code:VIII,name:Mary}" +
                    "-DI[14]{code:II,name:Kate}" +
                    "-DI[16]{code:IV,name:Linda}" +
                    "-DE[0]{code:IV,name:Linda}" +
                    "-DE[2]{code:VI,name:Lucy}" +
                    "-DE[4]{code:VIII,name:Mary}" +
                    "-DE[6]{code:II,name:Kate}" +
                    "-DE[8]{code:IV,name:Linda}" +
                    "-DE[10]{code:VI,name:Lucy}" +
                    "-DE[12]{code:VIII,name:Mary}" +
                    "-DE[14]{code:II,name:Kate}" +
                    "-DE[16]{code:IV,name:Linda}",
                    this.eventLog);
        }
        Helper.assertElementCodes(
                this.xList, 
                "I", "II", "III", "V", "VII",
                "I", "III", "V", "VII",
                "I", "III", "V", "VI", "VII", "VIII");
        Helper.assertElementNames(
                this.xList, 
                "Jim", "Kate", "Smith", "Sam", "Jhon",
                "Jim", "Smith", "Sam", "Jhon",
                "Jim", "Smith", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xSubList, 
                "V", "VII",
                "I", "III", "V", "VII",
                "I", "III", "V");
        Helper.assertElementNames(
                this.xSubList, 
                "Sam", "Jhon",
                "Jim", "Smith", "Sam", "Jhon",
                "Jim", "Smith", "Sam");
    }
    
    @Test
    public void testRetain7531ByCollectionManagedByCode() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this.xSubList.retainAll(
                        Helper.createElementListManagedByCodeByCodes(
                                "VII", "V", "III", "I", "V", "I")));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[0]{code:IV,name:Linda}" +
                    "-DI[2]{code:VI,name:Lucy}" +
                    "-DI[4]{code:VIII,name:Mary}" +
                    "-DI[6]{code:II,name:Kate}" +
                    "-DI[8]{code:IV,name:Linda}" +
                    "-DI[10]{code:VI,name:Lucy}" +
                    "-DI[12]{code:VIII,name:Mary}" +
                    "-DI[14]{code:II,name:Kate}" +
                    "-DI[16]{code:IV,name:Linda}" +
                    "-DE[0]{code:IV,name:Linda}" +
                    "-DE[2]{code:VI,name:Lucy}" +
                    "-DE[4]{code:VIII,name:Mary}" +
                    "-DE[6]{code:II,name:Kate}" +
                    "-DE[8]{code:IV,name:Linda}" +
                    "-DE[10]{code:VI,name:Lucy}" +
                    "-DE[12]{code:VIII,name:Mary}" +
                    "-DE[14]{code:II,name:Kate}" +
                    "-DE[16]{code:IV,name:Linda}",
                    this.eventLog);
        }
        Helper.assertElementCodes(
                this.xList, 
                "I", "II", "III", "V", "VII",
                "I", "III", "V", "VII",
                "I", "III", "V", "VI", "VII", "VIII");
        Helper.assertElementNames(
                this.xList, 
                "Jim", "Kate", "Smith", "Sam", "Jhon",
                "Jim", "Smith", "Sam", "Jhon",
                "Jim", "Smith", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xSubList, 
                "V", "VII",
                "I", "III", "V", "VII",
                "I", "III", "V");
        Helper.assertElementNames(
                this.xSubList, 
                "Sam", "Jhon",
                "Jim", "Smith", "Sam", "Jhon",
                "Jim", "Smith", "Sam");
    }

    @Test
    public void testRetain7531ByCollectionManagedByName() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this.xSubList.retainAll(
                        Helper.createElementListManagedByNameByCodes(
                                "VII", "V", "III", "I", "V", "I")));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[0]{code:IV,name:Linda}" +
                    "-DI[2]{code:VI,name:Lucy}" +
                    "-DI[4]{code:VIII,name:Mary}" +
                    "-DI[6]{code:II,name:Kate}" +
                    "-DI[8]{code:IV,name:Linda}" +
                    "-DI[10]{code:VI,name:Lucy}" +
                    "-DI[12]{code:VIII,name:Mary}" +
                    "-DI[14]{code:II,name:Kate}" +
                    "-DI[16]{code:IV,name:Linda}" +
                    "-DE[0]{code:IV,name:Linda}" +
                    "-DE[2]{code:VI,name:Lucy}" +
                    "-DE[4]{code:VIII,name:Mary}" +
                    "-DE[6]{code:II,name:Kate}" +
                    "-DE[8]{code:IV,name:Linda}" +
                    "-DE[10]{code:VI,name:Lucy}" +
                    "-DE[12]{code:VIII,name:Mary}" +
                    "-DE[14]{code:II,name:Kate}" +
                    "-DE[16]{code:IV,name:Linda}",
                    this.eventLog);
        }
        Helper.assertElementCodes(
                this.xList, 
                "I", "II", "III", "V", "VII",
                "I", "III", "V", "VII",
                "I", "III", "V", "VI", "VII", "VIII");
        Helper.assertElementNames(
                this.xList, 
                "Jim", "Kate", "Smith", "Sam", "Jhon",
                "Jim", "Smith", "Sam", "Jhon",
                "Jim", "Smith", "Sam", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xSubList, 
                "V", "VII",
                "I", "III", "V", "VII",
                "I", "III", "V");
        Helper.assertElementNames(
                this.xSubList, 
                "Sam", "Jhon",
                "Jim", "Smith", "Sam", "Jhon",
                "Jim", "Smith", "Sam");
    }

    @Test
    public void testRetain8264ByCollectionManagedByCode() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this.xSubList.retainAll(
                        Helper.createElementListManagedByCodeByCodes(
                                "VIII", "VI", "IV", "II", "VI", "II")));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[1]{code:V,name:Sam}" +
                    "-DI[3]{code:VII,name:Jhon}" +
                    "-DI[5]{code:I,name:Jim}" +
                    "-DI[7]{code:III,name:Smith}" +
                    "-DI[9]{code:V,name:Sam}" +
                    "-DI[11]{code:VII,name:Jhon}" +
                    "-DI[13]{code:I,name:Jim}" +
                    "-DI[15]{code:III,name:Smith}" +
                    "-DI[17]{code:V,name:Sam}" +
                    "-DE[1]{code:V,name:Sam}" +
                    "-DE[3]{code:VII,name:Jhon}" +
                    "-DE[5]{code:I,name:Jim}" +
                    "-DE[7]{code:III,name:Smith}" +
                    "-DE[9]{code:V,name:Sam}" +
                    "-DE[11]{code:VII,name:Jhon}" +
                    "-DE[13]{code:I,name:Jim}" +
                    "-DE[15]{code:III,name:Smith}" +
                    "-DE[17]{code:V,name:Sam}",
                    this.eventLog);
        }
        Helper.assertElementCodes(
                this.xList, 
                "I", "II", "III", "IV", "VI", "VIII",
                "II", "IV", "VI", "VIII",
                "II", "IV", "VI", "VII", "VIII");
        Helper.assertElementNames(
                this.xList, 
                "Jim", "Kate", "Smith", "Linda", "Lucy", "Mary",
                "Kate", "Linda", "Lucy", "Mary",
                "Kate", "Linda", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xSubList, 
                "IV", "VI", "VIII",
                "II", "IV", "VI", "VIII",
                "II", "IV");
        Helper.assertElementNames(
                this.xSubList, 
                "Linda", "Lucy", "Mary",
                "Kate", "Linda", "Lucy", "Mary",
                "Kate", "Linda");
    }
    
    @Test
    public void testRetain8264ByCollectionManagedByName() {
        this.synchronizeAndAssertModCount(false);
        Assert.assertTrue(
                this.xSubList.retainAll(
                        Helper.createElementListManagedByNameByCodes(
                                "VIII", "VI", "IV", "II", "VI", "II")));
        this.synchronizeAndAssertModCount(true);
        if (this.eventLog != null) {
            Assert.assertEquals(
                    "-DI[1]{code:V,name:Sam}" +
                    "-DI[3]{code:VII,name:Jhon}" +
                    "-DI[5]{code:I,name:Jim}" +
                    "-DI[7]{code:III,name:Smith}" +
                    "-DI[9]{code:V,name:Sam}" +
                    "-DI[11]{code:VII,name:Jhon}" +
                    "-DI[13]{code:I,name:Jim}" +
                    "-DI[15]{code:III,name:Smith}" +
                    "-DI[17]{code:V,name:Sam}" +
                    "-DE[1]{code:V,name:Sam}" +
                    "-DE[3]{code:VII,name:Jhon}" +
                    "-DE[5]{code:I,name:Jim}" +
                    "-DE[7]{code:III,name:Smith}" +
                    "-DE[9]{code:V,name:Sam}" +
                    "-DE[11]{code:VII,name:Jhon}" +
                    "-DE[13]{code:I,name:Jim}" +
                    "-DE[15]{code:III,name:Smith}" +
                    "-DE[17]{code:V,name:Sam}",
                    this.eventLog);
        }
        Helper.assertElementCodes(
                this.xList, 
                "I", "II", "III", "IV", "VI", "VIII",
                "II", "IV", "VI", "VIII",
                "II", "IV", "VI", "VII", "VIII");
        Helper.assertElementNames(
                this.xList, 
                "Jim", "Kate", "Smith", "Linda", "Lucy", "Mary",
                "Kate", "Linda", "Lucy", "Mary",
                "Kate", "Linda", "Lucy", "Jhon", "Mary");
        Helper.assertElementCodes(
                this.xSubList, 
                "IV", "VI", "VIII",
                "II", "IV", "VI", "VIII",
                "II", "IV");
        Helper.assertElementNames(
                this.xSubList, 
                "Linda", "Lucy", "Mary",
                "Kate", "Linda", "Lucy", "Mary",
                "Kate", "Linda");
    }
    
    private void synchronizeAndAssertModCount(boolean expectedChanged) {
        BaseElements<?> baseElements = null;
        try {
            baseElements = (BaseElements<?>)BASE_ELEMENTS_FIELD.get(this.xSubList);
        } catch (IllegalArgumentException ex) {
            Assert.fail("can not access the modCount");
        } catch (IllegalAccessException ex) {
            Assert.fail("can not access the modCount");
        }
        int modCount = baseElements.modCount();
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
            field = AbstractXList.class.getDeclaredField("baseElements");
        } catch (SecurityException ex) {
            Assert.fail("can not find baseElements");
        } catch (NoSuchFieldException ex) {
            Assert.fail("can not find baseElements");
        }
        field.setAccessible(true);
        BASE_ELEMENTS_FIELD = field;
    }

}
