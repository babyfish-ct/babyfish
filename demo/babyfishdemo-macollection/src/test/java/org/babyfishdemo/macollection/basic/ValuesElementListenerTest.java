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
package org.babyfishdemo.macollection.basic;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MAMap.MAValuesView;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.event.ValuesElementEvent;
import org.babyfish.collection.event.ValuesElementListener;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ValuesElementListenerTest {

    private MAOrderedMap<Integer, String> map;
    
    private MAValuesView<Integer, String> values;
    
    private StringBuilder valuesEventHistory;
    
    @Before
    public void setUp() {
        this.map = new MALinkedHashMap<>();
        this.values = this.map.values();
        this.values.addValuesElementListener(new ValuesElementListener<Integer, String>() {
            @Override
            public void modifying(ValuesElementEvent<Integer, String> e) throws Throwable {
                ValuesElementListenerTest that = ValuesElementListenerTest.this;
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    that
                    .valuesEventHistory
                    .append(":pre-detach(key=")
                    .append(e.getKey())
                    .append(",value=")
                    .append(e.getElement(PropertyVersion.DETACH))
                    .append(")");
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    that
                    .valuesEventHistory
                    .append(":pre-detach(key=")
                    .append(e.getKey())
                    .append(",value=")
                    .append(e.getElement(PropertyVersion.ATTACH))
                    .append(")");
                }
            }
            @Override
            public void modified(ValuesElementEvent<Integer, String> e) throws Throwable {
                ValuesElementListenerTest that = ValuesElementListenerTest.this;
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    that
                    .valuesEventHistory
                    .append(":post-detach(key=")
                    .append(e.getKey())
                    .append(",value=")
                    .append(e.getElement(PropertyVersion.DETACH))
                    .append(")");
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    that
                    .valuesEventHistory
                    .append(":post-detach(key=")
                    .append(e.getKey())
                    .append(",value=")
                    .append(e.getElement(PropertyVersion.ATTACH))
                    .append(")");
                }
            }
        });
    }
    
    @Test
    public void test() {
        this.map.put(1, "I");
        this.map.put(2, "II");
        this.map.put(3, "III");
        this.map.put(4, "IV");
        this.map.put(5, "V");
        this.map.put(6, "VI");
        
        this.valuesEventHistory = new StringBuilder();
        Iterator<String> valueIterator = this.map.values().iterator();
        while (valueIterator.hasNext()) {
            String value = valueIterator.next();
            if (value.length() % 2 != 0) {
                valueIterator.remove();
            }
        }
        assertMap(this.map, 2, "II", 4, "IV", 6, "VI");
        Assert.assertEquals(
                ":pre-detach(key=1,value=I)" +
                ":post-detach(key=1,value=I)" +
                ":pre-detach(key=3,value=III)" +
                ":post-detach(key=3,value=III)" +
                ":pre-detach(key=5,value=V)" +
                ":post-detach(key=5,value=V)",
                this.valuesEventHistory.toString());
    }
    
    private static void assertMap(Map<Integer, String> map, Object ... keyAndValues) {
        if (keyAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("The length of \"keyAndValues\" must be even number");
        }
        Assert.assertEquals(keyAndValues.length / 2, map.size());
        int index = 0;
        for (Entry<Integer, String> entry : map.entrySet()) {
            Assert.assertEquals(keyAndValues[index++], entry.getKey());
            Assert.assertEquals(keyAndValues[index++], entry.getValue());
        }
    }
}
