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
package org.babyfish.test.collection.bidi;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.collection.event.modification.CollectionModifications;
import org.babyfish.collection.event.modification.MapModifications;
import org.babyfish.collection.spi.AbstractXMap;
import org.babyfish.collection.spi.base.AbstractBaseEntriesImpl;
import org.babyfish.collection.spi.base.BaseEntries;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.data.event.Modification;
import org.babyfish.data.event.ModificationType;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Test;

import junit.framework.Assert;

public abstract class AbstractBidiMapTest {
    
    private static final Field ABSTRACT_XMAP_BASE_ENTRIES;
    
    private static final Field ABSTRACT_BASE_ENTRIES_IMPL_ROOT_ENTRIES_OR_ROOT_DATA;
    
    private static final Field ROOT_DATA_INVERSED_ENTRIES;

    protected abstract Map<String, Unstable> createBidiMap();
    
    @Test
    public void testPut() {
        
        Map<String, Unstable> map = this.createBidiMap();
        EventCollector ec = new EventCollector(map);
        
        map.put("a", new Unstable("x"));
        assertBidiMap(map, "a", new Unstable("x"));
        ec.assertEvents(
                MapModifications.put("a", new Unstable("x")), 
                "adding(a, x)added(a, x)"
        );
        
        map.put("b", new Unstable("x"));
        assertBidiMap(map, "b", new Unstable("x"));
        ec.assertEvents(
                MapModifications.put("b", new Unstable("x")), 
                "removing(a, x)adding(b, x)removed(a, x)added(b, x)"
        );
    }
    
    @Test
    public void testPutAll() {
        
        Map<String, Unstable> map = this.createBidiMap();
        EventCollector ec = new EventCollector(map);
        
        Map<String, Unstable> tmp = new java.util.LinkedHashMap<>();
        tmp.put("a", new Unstable("B"));
        tmp.put("b", new Unstable("B"));
        tmp.put("c", new Unstable("C"));
        tmp.put("c", new Unstable("C2"));
        tmp.put("d", new Unstable("D-"));
        map.putAll(tmp);
        assertBidiMap(
                map, 
                "b", new Unstable("B"), 
                "c", new Unstable("C2"),
                "d", new Unstable("D-")
        );
        ec.assertEvents(
                MapModifications.putAll(tmp), 
                "adding(b, B)adding(c, C2)adding(d, D-)" +
                "added(b, B)added(c, C2)added(d, D-)"
        );
        
        tmp = new java.util.LinkedHashMap<>();
        tmp.put("b2", new Unstable("B"));
        tmp.put("c2", new Unstable("C2"));
        tmp.put("d", new Unstable("D"));
        map.putAll(tmp);
        assertBidiMap(
                map, 
                "d", new Unstable("D"),
                "b2", new Unstable("B"), 
                "c2", new Unstable("C2")
        );
        ec.assertEvents(
                MapModifications.putAll(tmp), 
                "removing(b, B)removing(c, C2)adding(b2, B)adding(c2, C2)changing(d, D-, d, D)" +
                "removed(b, B)removed(c, C2)added(b2, B)added(c2, C2)changed(d, D-, d, D)"
        );
    }
    
    @Test
    public void testRemoveByEntry() {
        Map<String, Unstable> map = this.createBidiMap();
        EventCollector ec = new EventCollector(map);
        map.put("a", new Unstable("A"));
        map.put("b", new Unstable("B"));
        map.put("c", new Unstable("C"));
        map.put("d", new Unstable("D"));
        ec.clear();
        
        assertBidiMap(
                map, 
                "a", new Unstable("A"),
                "b", new Unstable("B"),
                "c", new Unstable("C"),
                "d", new Unstable("D")
        );
        
        Entry<String, Unstable> entry = 
                Collections
                .singletonMap("b", new Unstable("B"))
                .entrySet()
                .iterator()
                .next();
        Assert.assertTrue(map.entrySet().contains(entry));
        map.entrySet().remove(entry);
        Assert.assertFalse(map.entrySet().contains(entry));
        
        assertBidiMap(
                map, 
                "a", new Unstable("A"),
                "c", new Unstable("C"),
                "d", new Unstable("D")
        );
        ec.assertEvents(
                CollectionModifications.remove(entry),
                "removing(b, B)removed(b, B)"
        );
    }
    
    @Test
    public void testRemoveByKey() {
        Map<String, Unstable> map = this.createBidiMap();
        EventCollector ec = new EventCollector(map);
        map.put("a", new Unstable("A"));
        map.put("b", new Unstable("B"));
        map.put("c", new Unstable("C"));
        map.put("d", new Unstable("D"));
        ec.clear();
        
        assertBidiMap(
                map, 
                "a", new Unstable("A"),
                "b", new Unstable("B"),
                "c", new Unstable("C"),
                "d", new Unstable("D")
        );
        
        Assert.assertTrue(map.keySet().contains("b"));
        Assert.assertTrue(map.containsKey("b"));
        map.keySet().remove("b");
        Assert.assertFalse(map.keySet().contains("b"));
        Assert.assertFalse(map.containsKey("b"));
        
        assertBidiMap(
                map, 
                "a", new Unstable("A"),
                "c", new Unstable("C"),
                "d", new Unstable("D")
        );
        ec.assertEvents(
                CollectionModifications.remove("b"),
                "removing(b, B)removed(b, B)"
        );
    }
    
    @Test
    public void testRemoveByValue() {
        Map<String, Unstable> map = this.createBidiMap();
        EventCollector ec = new EventCollector(map);
        map.put("a", new Unstable("A"));
        map.put("b", new Unstable("B"));
        map.put("c", new Unstable("C"));
        map.put("d", new Unstable("D"));
        ec.clear();
        
        assertBidiMap(
                map, 
                "a", new Unstable("A"),
                "b", new Unstable("B"),
                "c", new Unstable("C"),
                "d", new Unstable("D")
        );
        
        Assert.assertTrue(map.values().contains(new Unstable("B")));
        Assert.assertTrue(map.containsValue(new Unstable("B")));
        map.values().remove(new Unstable("B"));
        Assert.assertFalse(map.values().contains(new Unstable("B")));
        Assert.assertFalse(map.containsValue(new Unstable("B")));
        
        assertBidiMap(
                map, 
                "a", new Unstable("A"),
                "c", new Unstable("C"),
                "d", new Unstable("D")
        );
        ec.assertEvents(
                CollectionModifications.remove(new Unstable("B")),
                "removing(b, B)removed(b, B)"
        );
    }
    
    @Test
    public void testRemoveByValueColletion() {
        Map<String, Unstable> map = this.createBidiMap();
        EventCollector ec = new EventCollector(map);
        map.put("a", new Unstable("A"));
        map.put("b", new Unstable("B"));
        map.put("c", new Unstable("C"));
        map.put("d", new Unstable("D"));
        ec.clear();
        
        assertBidiMap(
                map, 
                "a", new Unstable("A"),
                "b", new Unstable("B"),
                "c", new Unstable("C"),
                "d", new Unstable("D")
        );
        
        Collection<Unstable> vc = MACollections.wrap(new Unstable("B"), new Unstable("D"));
        Assert.assertTrue(map.values().containsAll(vc));
        Assert.assertTrue(map.values().contains(new Unstable("B")));
        Assert.assertTrue(map.values().contains(new Unstable("D")));
        Assert.assertTrue(map.containsValue(new Unstable("B")));
        Assert.assertTrue(map.containsValue(new Unstable("D")));
        map.values().removeAll(vc);
        Assert.assertFalse(map.values().containsAll(vc));
        Assert.assertFalse(map.values().contains(new Unstable("B")));
        Assert.assertFalse(map.values().contains(new Unstable("D")));
        Assert.assertFalse(map.containsValue(new Unstable("B")));
        Assert.assertFalse(map.containsValue(new Unstable("D")));
        
        assertBidiMap(
                map, 
                "a", new Unstable("A"),
                "c", new Unstable("C")
        );
        ec.assertEvents(
                CollectionModifications.removeAll(vc),
                "removing(b, B)removing(d, D)removed(b, B)removed(d, D)"
        );
    }
    
    @Test
    public void testUnstableValue1() {
        
        Map<String, Unstable> map = this.createBidiMap();
        EventCollector ec = new EventCollector(map);
        
        Unstable x = new Unstable("x");
        map.put("a", x);
        ec.assertEvents(
                MapModifications.put("a", x), 
                "adding(a, x)added(a, x)"
        );
        
        Unstable y = new Unstable("y");
        map.put("b", y);
        ec.assertEvents(
                MapModifications.put("b", y), 
                "adding(b, y)added(b, y)"
        );
        
        Unstable z = new Unstable("z");
        map.put("c", z);
        ec.assertEvents(
                MapModifications.put("c", z), 
                "adding(c, z)added(c, z)"
        );
        
        assertBidiMap(
                map, 
                "a", new Unstable("x"), 
                "b", new Unstable("y"), 
                "c", new Unstable("z")
        );
        
        x.setVal("y");
        assertBidiMap(map, "a", new Unstable("y"), "c", new Unstable("z"));
        ec.assertEvents(
                MapModifications.suspendViaInversedFrozenContext("x"),
                "removing(a, x)removed(a, x)",
                MapModifications.resumeViaInversedFrozenContext(),
                "removing(b, y)adding(a, y)removed(b, y)added(a, y)"
        );
    }
    
    @Test
    public void testUnstableValue2() {
        
        Map<String, Unstable> map = this.createBidiMap();
        EventCollector ec = new EventCollector(map);
        
        Unstable z = new Unstable("z");
        map.put("c", z);
        ec.assertEvents(
                MapModifications.put("c", z), 
                "adding(c, z)added(c, z)"
        );
        
        Unstable y = new Unstable("y");
        map.put("b", y);
        ec.assertEvents(
                MapModifications.put("b", y), 
                "adding(b, y)added(b, y)"
        );
        
        Unstable x = new Unstable("x");
        map.put("a", x);
        ec.assertEvents(
                MapModifications.put("a", x), 
                "adding(a, x)added(a, x)"
        );
        
        assertBidiMap(
                map, 
                "c", new Unstable("z"), 
                "b", new Unstable("y"), 
                "a", new Unstable("x")
        );
        
        x.setVal("y");
        assertBidiMap(map, "c", new Unstable("z"), "a", new Unstable("y"));
        ec.assertEvents(
                MapModifications.suspendViaInversedFrozenContext("x"),
                "removing(a, x)removed(a, x)",
                MapModifications.resumeViaInversedFrozenContext(),
                "removing(b, y)adding(a, y)removed(b, y)added(a, y)"
        );
    }
    
    private static void assertBidiMap(Map<?, ?> map, Object ... keyValuePairs) {
        
        Assert.assertTrue(map instanceof AbstractXMap<?, ?>);
        BaseEntries<?, ?> baseEntries = valueOf(map, ABSTRACT_XMAP_BASE_ENTRIES);
        Object rootData = valueOf(baseEntries, ABSTRACT_BASE_ENTRIES_IMPL_ROOT_ENTRIES_OR_ROOT_DATA);
        BaseEntries<?, ?> inversedEntries = valueOf(rootData, ROOT_DATA_INVERSED_ENTRIES);
        
        Assert.assertEquals(keyValuePairs.length, baseEntries.size() * 2);
        Assert.assertEquals(keyValuePairs.length, inversedEntries.size() * 2); //very important
        
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            
            Object key = keyValuePairs[i];
            Object value = keyValuePairs[i + 1];
            
            BaseEntry<?, ?> be = baseEntries.getBaseEntry(key);
            Assert.assertNotNull(be);
            Assert.assertEquals(value.toString(), be.getValue().toString());
            
            BaseEntry<?, ?> ibe = inversedEntries.getBaseEntry(value);
            Assert.assertNotNull(ibe);
            Assert.assertEquals(key.toString(), ibe.getValue().toString());
        }
        
        if (map instanceof XOrderedMap<?, ?>) {
            int index = 0;
            for (Object k : map.keySet()) {
                Assert.assertEquals(keyValuePairs[index], k);
                index += 2;
            }
        }
    }
    
    private static Class<?> classOf(Class<?> type, String simpleName) {
        for (Class<?> nestedClass : type.getDeclaredClasses()) {
            if (nestedClass.getSimpleName().equals(simpleName)) {
                return nestedClass;
            }
        }
        return null;
    }
    
    private static Field fieldOf(Class<?> type, String name) {
        Field field;
        try {
            field = type.getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            throw new AssertionError(ex);
        }
        field.setAccessible(true);
        return field;
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T valueOf(Object o, Field field) {
        try {
            return (T)field.get(o);
        } catch (IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
    }
    
    private static class EventCollector {
        
        private List<Modification> modifications;
        
        private List<String> modificationTexts;
        
        private List<StringBuilder> eventLogs;
        
        @SuppressWarnings("unchecked")
        EventCollector(Map<?, ?> map) {
            if (map instanceof MAMap<?, ?>) {
                this.modifications = new ArrayList<>();
                this.modificationTexts = new ArrayList<>();
                this.eventLogs = new ArrayList<>();
                MAMap<Object, Object> maMap = (MAMap<Object, Object>)map;
                maMap.addMapElementListener(
                        new MapElementListener<Object, Object>() {
                            @Override
                            public void modifying(MapElementEvent<Object, Object> e) throws Throwable {
                                EventCollector.this.modifying(e);
                            }

                            @Override
                            public void modified(MapElementEvent<Object, Object> e) throws Throwable {
                                EventCollector.this.modified(e);
                            }
                        }
                );
            }
        }
        
        public void assertEvents(Object ... args) {
            if (this.modifications == null) {
                return;
            }
            if (args.length % 2 != 0) {
                Assert.fail("args count must be even number");
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < args.length; i += 2) {
                Object modification = args[i];
                Assert.assertTrue(
                        "args[" + i + "] must be Modification",
                        modification instanceof Modification
                );
                Object eventLog = args[i + 1];
                Assert.assertTrue(
                        "args[" + (i + 1) + "] must be String",
                        eventLog instanceof String
                );
                builder.append(modification).append(eventLog);
            }
            Assert.assertEquals(builder.toString(), this.toString());
            this.clear();
        }
        
        public void clear() {
            if (this.modifications != null) {
                this.modifications.clear();
                this.modificationTexts.clear();
                this.eventLogs.clear();
            }
        }

        private void modifying(MapElementEvent<Object, Object> e) {
            this.append(e, false);
        }
        
        private void modified(MapElementEvent<Object, Object> e) {
            this.append(e, true);
        }
        
        private void append(MapElementEvent<Object, Object> e, boolean post) {
            StringBuilder builder;
            Modification modification = e.getFinalModification();
            if (this.modifications.isEmpty() || 
                    this
                    .modifications
                    .get(this.modifications.size() - 1) !=
                    modification) {
                this.modifications.add(modification);
                this.modificationTexts.add(modification.toString());
                this.eventLogs.add(builder = new StringBuilder());
            } else {
                builder = this.eventLogs.get(this.eventLogs.size() - 1);
            }
            switch (e.getModificationType()) {
            case DETACH:
                builder.append(post ? "removed(" : "removing(");
                break;
            case ATTACH:
                builder.append(post ? "added(" : "adding(");
                break;
            case REPLACE:
                builder.append(post ? "changed(" : "changing(");
                break;
            }
            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                builder
                .append(e.getKey(PropertyVersion.DETACH))
                .append(", ")
                .append(e.getValue(PropertyVersion.DETACH));
            }
            if (e.getModificationType() == ModificationType.REPLACE) {
                builder.append(", ");
            }
            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                builder
                .append(e.getKey(PropertyVersion.ATTACH))
                .append(", ")
                .append(e.getValue(PropertyVersion.ATTACH));
            }
            builder.append(')');
        }
        
        @Override
        public String toString() {
            if (this.modificationTexts == null) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < this.modificationTexts.size(); i++) {
                builder.append(this.modificationTexts.get(i));
                builder.append(this.eventLogs.get(i));
            }
            return builder.toString();
        }
    }
    
    static {
        ABSTRACT_XMAP_BASE_ENTRIES = fieldOf(AbstractXMap.class, "baseEntries");
        ABSTRACT_BASE_ENTRIES_IMPL_ROOT_ENTRIES_OR_ROOT_DATA = 
                fieldOf(AbstractBaseEntriesImpl.class, "rootEntriesOrRootData");
        ROOT_DATA_INVERSED_ENTRIES = fieldOf(
                classOf(AbstractBaseEntriesImpl.class, "RootData"),
                "inversedEntries"
        );
    }
}
