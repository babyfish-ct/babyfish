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
import java.util.LinkedHashMap;
import java.util.Map;

import org.babyfish.collection.MAHashMap;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class MapElementListenerTest {

    private MAMap<String, String> map;
    
    private StringBuilder mapEventHistory;
    
    @Before
    public void setUp() {
        this.map = new MAHashMap<>();
        map.addMapElementListener(new MapElementListener<String, String>() {
            @Override
            public void modifying(MapElementEvent<String, String> e) throws Throwable {
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    MapElementListenerTest
                    .this
                    .mapEventHistory
                    .append(":pre-detach(key=")
                    .append(e.getKey(PropertyVersion.DETACH))
                    .append(",value=")
                    .append(e.getValue(PropertyVersion.DETACH))
                    .append(')');
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    MapElementListenerTest
                    .this
                    .mapEventHistory
                    .append(":pre-attach(key=")
                    .append(e.getKey(PropertyVersion.ATTACH))
                    .append(",value=")
                    .append(e.getValue(PropertyVersion.ATTACH))
                    .append(')');
                }
            }

            @Override
            public void modified(MapElementEvent<String, String> e) throws Throwable {
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    MapElementListenerTest
                    .this
                    .mapEventHistory
                    .append(":post-detach(key=")
                    .append(e.getKey(PropertyVersion.DETACH))
                    .append(",value=")
                    .append(e.getValue(PropertyVersion.DETACH))
                    .append(')');
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    MapElementListenerTest
                    .this
                    .mapEventHistory
                    .append(":post-attach(key=")
                    .append(e.getKey(PropertyVersion.ATTACH))
                    .append(",value=")
                    .append(e.getValue(PropertyVersion.ATTACH))
                    .append(')');
                }
            }
        });
    }
    
    @Test
    public void test() {
        this.mapEventHistory = new StringBuilder();
        this.map.putAll(map("I", "One", "II", "Two", "III", "Three", "IV", "Four", "V", "Five"));
        Assert.assertEquals(
                ":pre-attach(key=I,value=One):pre-attach(key=II,value=Two):pre-attach(key=III,value=Three):pre-attach(key=IV,value=Four):pre-attach(key=V,value=Five)"
                + ":post-attach(key=I,value=One):post-attach(key=II,value=Two):post-attach(key=III,value=Three):post-attach(key=IV,value=Four):post-attach(key=V,value=Five)",
                this.mapEventHistory.toString()
        );
        
        // Remove key "II" and "IV"
        this.mapEventHistory = new StringBuilder();
        Iterator<String> keyIterator = this.map.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            if (key.length() % 2 == 0) {
                keyIterator.remove();
            }
        }
        Assert.assertEquals(
                ":pre-detach(key=II,value=Two):post-detach(key=II,value=Two)"
                + ":pre-detach(key=IV,value=Four):post-detach(key=IV,value=Four)",
                this.mapEventHistory.toString()
        );
    }
    
    private static Map<String, String> map(String ... keyAndValues) {
        if (keyAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("The length of keyAndValues must be even number");
        }
        Map<String, String> map = new LinkedHashMap<>((keyAndValues.length * 4 + 2) /3);
        int index = 0;
        while (index < keyAndValues.length) {
            map.put(keyAndValues[index++], keyAndValues[index++]);
        }
        return map;
    }
}
