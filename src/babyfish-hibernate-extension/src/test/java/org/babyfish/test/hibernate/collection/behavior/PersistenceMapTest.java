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
package org.babyfish.test.hibernate.collection.behavior;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.MAHashMap;
import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.XMap;
import org.babyfish.hibernate.collection.type.MANavigableMapType;
import org.babyfish.hibernate.collection.type.MAOrderedMapType;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class PersistenceMapTest {
    
    private static final TestSite.MapFactory[] MAP_FACTORIES =
        new TestSite.MapFactory[] {
            new TestSite.MapFactory() {
                @Override
                public XMap<String, String> createMap() {
                    return new org.babyfish.collection.HashMap<String, String>(
                            BidiType.NONNULL_VALUES
                    );
                }
            },
            new TestSite.MapFactory() {
                @Override
                public XMap<String, String> createMap() {
                    return new MAHashMap<String, String>(
                            BidiType.NONNULL_VALUES
                    );
                }
            },
            new TestSite.MapFactory() {
                @Override
                public XMap<String, String> createMap() {
                    return new org.babyfish.collection.LinkedHashMap<String, String>(
                            BidiType.NONNULL_VALUES
                    );
                }
            },
            new TestSite.MapFactory() {
                @Override
                public XMap<String, String> createMap() {
                    return new MALinkedHashMap<String, String>(
                            BidiType.NONNULL_VALUES
                    );
                }
            },
            new TestSite.MapFactory() {
                @Override
                public XMap<String, String> createMap() {
                    return new org.babyfish.collection.TreeMap<String, String>(
                            BidiType.NONNULL_VALUES
                    );
                }
            },
            new TestSite.MapFactory() {
                @Override
                public XMap<String, String> createMap() {
                    return new MATreeMap<String, String>(
                            BidiType.NONNULL_VALUES
                    );
                }
            },
            new TestSite.MapFactory() {
                @SuppressWarnings("unchecked")
                @Override
                public XMap<String, String> createMap() {
                    return (XMap<String, String>)new MAOrderedMapType().wrap(
                            null, 
                            new MALinkedHashMap<String, String>(
                                    BidiType.NONNULL_VALUES
                            )
                    );
                }
            },
            new TestSite.MapFactory() {
                @SuppressWarnings("unchecked")
                @Override
                public XMap<String, String> createMap() {
                    return (XMap<String, String>)new MANavigableMapType().wrap(
                            null, 
                            new MATreeMap<String, String>(
                                    BidiType.NONNULL_VALUES
                            )
                    );
                }
            },
        };
    
    private TestSite[] testSites;
    
    @Before
    public void initalize() {
        TestSite[] sites = new TestSite[MAP_FACTORIES.length];
        int index = 0;
        for (TestSite.MapFactory mapFactory : MAP_FACTORIES) {
            sites[index++] = new TestSite(mapFactory);
        }
        this.testSites = sites;
    }
    
    @Test
    public void testPut() {
        for (TestSite testSite : this.testSites) {
            testSite.testPut();
        }
    }
    
    @Test
    public void testPutAll() {
        for (TestSite testSite : this.testSites) {
            testSite.testPutAll();
        }
    }
    
    @Test
    public void testEntrySet() {
        for (TestSite testSite : this.testSites) {
            testSite.testEntrySet();
        }
    }
    
    private static class TestSite {
        
        private MapFactory mapFactory;
        
        public TestSite(MapFactory mapFactory) {
            this.mapFactory = mapFactory;
        }

        interface MapFactory {
            XMap<String, String> createMap();
        }

        void testPut() {
            Map<String, String> map = this.prepareMap("A", "a", "B", "b", "C", "c", "D", "d", "E", "e");
            assertMap(map, "A", "a", "B", "b", "C", "c", "D", "d", "E", "e");
            
            map.put("new-A", "a");
            assertMap(map, "new-A", "a", "B", "b", "C", "c", "D", "d", "E", "e");
            map.put("new-B", "b");
            assertMap(map, "new-A", "a", "new-B", "b", "C", "c", "D", "d", "E", "e");
            map.put("new-C", "c");
            assertMap(map, "new-A", "a", "new-B", "b", "new-C", "c", "D", "d", "E", "e");
            map.put("new-D", "d");
            assertMap(map, "new-A", "a", "new-B", "b", "new-C", "c", "new-D", "d", "E", "e");
            map.put("new-E", "e");
            assertMap(map, "new-A", "a", "new-B", "b", "new-C", "c", "new-D", "d", "new-E", "e");
            
            map.put("new-A", "b");
            assertMap(map, "new-A", "b", "new-C", "c", "new-D", "d", "new-E", "e");
            map.put("new-A", "c");
            assertMap(map, "new-A", "c", "new-D", "d", "new-E", "e");
            map.put("new-A", "d");
            assertMap(map, "new-A", "d", "new-E", "e");
            map.put("new-A", "e");
            assertMap(map, "new-A", "e");
        }
        
        void testPutAll() {
            Map<String, String> map = this.createMap();
            assertMap(map);
            
            map.putAll(
                    this.prepareMap(
                            "A4", "a", "B4", "b", "C4", "c", "D4", "d", "E4", "e",
                            "A3", "a", "B3", "b", "C3", "c", "D3", "d", "E3", "e",
                            "A2", "a", "B2", "b", "C2", "c", "D2", "d", "E2", "e",
                            "A", "a", "B", "b", "C", "c", "D", "d", "E", "e"));
            assertMap(map, "A", "a", "B", "b", "C", "c", "D", "d", "E", "e");
            
            map.putAll(this.prepareMap("new-A", "a", "new-B", "b"));
            assertMap(map, "new-A", "a", "new-B", "b", "C", "c", "D", "d", "E", "e");
            
            map.putAll(this.prepareMap("new-C", "c", "new-D", "d", "new-E", "e"));
            assertMap(map, "new-A", "a", "new-B", "b", "new-C", "c", "new-D", "d", "new-E", "e");
            
            map.putAll(prepareMap("new-A", "b"));
            assertMap(map, "new-A", "b", "new-C", "c", "new-D", "d", "new-E", "e");
            map.putAll(prepareMap("new-A", "c"));
            assertMap(map, "new-A", "c", "new-D", "d", "new-E", "e");
            map.putAll(prepareMap("new-A", "d"));
            assertMap(map, "new-A", "d", "new-E", "e");
            map.putAll(prepareMap("new-A", "e"));
            assertMap(map, "new-A", "e");
        }
        
        void testEntrySet() {
            Map<String, String> map = this.createMap();
            assertMap(map);
            
            map.putAll(this.prepareMap("A", "a", "B", "b", "C", "c", "D", "d", "E", "e"));
            assertMap(map, "A", "a", "B", "b", "C", "c", "D", "d", "E", "e");
            
            getEntry(map, "A").setValue("e");
            assertMap(map, "A", "e", "B", "b", "C", "c", "D", "d");
            getEntry(map, "A").setValue("d");
            assertMap(map, "A", "d", "B", "b", "C", "c");
            getEntry(map, "A").setValue("c");
            assertMap(map, "A", "c", "B", "b");
            getEntry(map, "A").setValue("b");
            assertMap(map, "A", "b");
            getEntry(map, "A").setValue("a");
            assertMap(map, "A", "a");
        }
        
        private Map<String, String> createMap() {
            XMap<String, String> map = this.mapFactory.createMap();
            return map;
        }
        
        private Map<String, String> prepareMap(String ... keysAndValues) {
            if (keysAndValues.length % 2 != 0) {
                throw new IllegalArgumentException();
            }
            Map<String, String> map = this.createMap();
            for (int i = 0; i < keysAndValues.length / 2; i++) {
                map.put(keysAndValues[2 * i], keysAndValues[2 * i + 1]);
            }
            return map;
        }
        
        private static void assertMap(Map<String, String> map, String ... keysAndValues) {
            if (keysAndValues.length % 2 != 0) {
                throw new IllegalArgumentException();
            }
            Assert.assertEquals(keysAndValues.length / 2, map.size());
            for (int i = 0; i < keysAndValues.length / 2; i++) {
                Assert.assertEquals(keysAndValues[2 * i + 1], map.get(keysAndValues[2 * i]));
            }
        }
        
        private static Entry<String, String> getEntry(
                Map<String, String> map, String key) {
            Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, String> entry = iterator.next();
                if (key.equals(entry.getKey())) {
                    return entry;
                }
            }
            return null;
        }
    }
    
}
