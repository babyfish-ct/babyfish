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

import org.babyfish.collection.MAHashMap;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.event.EntryElementEvent;
import org.babyfish.collection.event.EntryElementListener;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class EntryElementListenerTest {

    private MAMap.MAEntry<String, String> entry;
    
    private StringBuilder entryEventHistory;
    
    @Before
    public void setUp() {
        MAMap<String, String> map = new MAHashMap<>();
        map.put("A", "Alpha");
        this.entry = map.entrySet().iterator().next();
        this.entry.addEntryElementListener(new EntryElementListener<String, String>() {

            @Override
            public void modifying(EntryElementEvent<String, String> e) throws Throwable {
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    EntryElementListenerTest
                    .this
                    .entryEventHistory
                    .append(":pre-detach(key=")
                    .append(e.getKey())
                    .append(",element=")
                    .append(e.getElement(PropertyVersion.DETACH))
                    .append(')');
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    EntryElementListenerTest
                    .this
                    .entryEventHistory
                    .append(":pre-attach(key=")
                    .append(e.getKey())
                    .append(",element=")
                    .append(e.getElement(PropertyVersion.ATTACH))
                    .append(')');
                }
            }

            @Override
            public void modified(EntryElementEvent<String, String> e) throws Throwable {
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    EntryElementListenerTest
                    .this
                    .entryEventHistory
                    .append(":post-detach(key=")
                    .append(e.getKey())
                    .append(",element=")
                    .append(e.getElement(PropertyVersion.DETACH))
                    .append(')');
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    EntryElementListenerTest
                    .this
                    .entryEventHistory
                    .append(":post-attach(key=")
                    .append(e.getKey())
                    .append(",element=")
                    .append(e.getElement(PropertyVersion.ATTACH))
                    .append(')');
                }
            }
        });
    }
    
    @Test
    public void test() {
        this.entryEventHistory = new StringBuilder();
        this.entry.setValue("First");
        Assert.assertEquals(
                ":pre-detach(key=A,element=Alpha):pre-attach(key=A,element=First)"
                + ":post-detach(key=A,element=Alpha):post-attach(key=A,element=First)", 
                this.entryEventHistory.toString());
    }
}
