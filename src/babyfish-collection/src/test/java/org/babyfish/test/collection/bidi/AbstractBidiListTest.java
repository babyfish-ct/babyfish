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
import java.util.List;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAList;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.event.ListElementListener;
import org.babyfish.collection.event.modification.CollectionModifications;
import org.babyfish.collection.event.modification.ListModifications;
import org.babyfish.collection.spi.AbstractXList;
import org.babyfish.collection.spi.base.AbstractBaseElementsImpl;
import org.babyfish.collection.spi.base.BaseEntries;
import org.babyfish.data.event.Modification;
import org.babyfish.data.event.ModificationType;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractBidiListTest {

    private static final Field ABSTRACT_XLIST_BASE_ELEMENTS =
            fieldOf(AbstractXList.class, "baseElements");
    
    private static final Field BASE_ELEMENTS_INVERSED_ENTRIES =
            fieldOf(AbstractBaseElementsImpl.class, "inversedEntries");
    
    protected abstract List<Unstable> createBidiList();
    
    private List<Unstable> list;
    
    private EventCollector ec;
    
    @Before
    public void initData() {
        this.list = this.createBidiList();
        this.ec = new EventCollector(list);
        
        Collection<Unstable> c = MACollections.wrap(
                new Unstable("a"), 
                new Unstable("b"), 
                new Unstable("c")
        );
        this.list.addAll(c);
        assertBidiList(list, new Unstable("a"), new Unstable("b"), new Unstable("c"));
        this.ec.assertEvents(
                CollectionModifications.addAll(c),
                "adding(0, a)adding(1, b)adding(2, c)added(0, a)added(1, b)added(2, c)"
        );
    }
    
    @Test
    public void testAdd() {
        
        this.list.add(0, new Unstable("b"));
        assertBidiList(this.list, new Unstable("b"), new Unstable("a"), new Unstable("c"));
        this.ec.assertEvents(
                ListModifications.add(0, new Unstable("b")),
                "removing(1, b)adding(0, b)removed(1, b)added(0, b)"
        );
    }
    
    @Test
    public void testAddAll() {
        Collection<Unstable> c = MACollections.wrap(
                new Unstable("b"), 
                new Unstable("c"), 
                new Unstable("d")
        );
        this.list.addAll(0, c);
        assertBidiList(
                this.list, 
                new Unstable("b"), new Unstable("c"), new Unstable("d"), new Unstable("a") 
        );
        this.ec.assertEvents(
                ListModifications.addAll(0, c),
                "removing(1, b)removing(2, c)adding(0, b)adding(1, c)adding(2, d)" +
                "removed(1, b)removed(2, c)added(0, b)added(1, c)added(2, d)"
        );
    }
    
    @Test
    public void testSet() {
        Assert.assertEquals("c", this.list.set(2, new Unstable("a")).toString());
        assertBidiList(this.list, new Unstable("b"), new Unstable("a"));
        this.ec.assertEvents(
                ListModifications.set(2, new Unstable("a")),
                "removing(0, a)changing(2, c, 1, a)removed(0, a)changed(2, c, 1, a)"
        );
    }
    
    @Test
    public void testClear() {
        this.list.subList(1, 2).clear();
        assertBidiList(this.list, new Unstable("a"), new Unstable("c"));
        this.ec.assertEvents(
                ListModifications.clear(),
                "removing(1, b)removed(1, b)"
        );
    }
    
    @Test
    public void testRemoveAt() {
        this.list.remove(1);
        assertBidiList(this.list, new Unstable("a"), new Unstable("c"));
        this.ec.assertEvents(
                ListModifications.remove(1),
                "removing(1, b)removed(1, b)"
        );
    }
    
    @Test
    public void testRemove() {
        this.list.remove(new Unstable("b"));
        assertBidiList(this.list, new Unstable("a"), new Unstable("c"));
        this.ec.assertEvents(
                CollectionModifications.remove(new Unstable("b")),
                "removing(1, b)removed(1, b)"
        );
    }
    
    @Test
    public void testRemoveAll() {
        Collection<Unstable> c = MACollections.wrap(new Unstable("a"), new Unstable("c"));
        this.list.removeAll(c);
        assertBidiList(this.list, new Unstable("b"));
        this.ec.assertEvents(
                CollectionModifications.removeAll(c),
                "removing(0, a)removing(2, c)removed(0, a)removed(2, c)"
        );
    }
    
    @Test
    public void testRetainAll() {
        Collection<Unstable> c = MACollections.wrap(new Unstable("a"), new Unstable("c"));
        this.list.retainAll(c);
        assertBidiList(this.list, new Unstable("a"), new Unstable("c"));
        this.ec.assertEvents(
                CollectionModifications.retainAll(c),
                "removing(1, b)removed(1, b)"
        );
    }
    
    @Test
    public void testUnstableValue() {
        this.list.get(1).setVal("a");
        assertBidiList(this.list, new Unstable("a"), new Unstable("c"));
        this.ec.assertEvents(
                ListModifications.suspendViaInversedFrozenContext(new Unstable("b")),
                "removing(1, b)removed(1, b)",
                ListModifications.resumeViaInversedFrozenContext(),
                "removing(0, a)adding(0, a)removed(0, a)added(0, a)"
        );
    }
    
    private static void assertBidiList(List<Unstable> list, Unstable ... elements) {
        Assert.assertEquals(elements.length, list.size());
        int index = 0;
        for (Unstable e : list) {
            Assert.assertEquals(elements[index++].toString(), e.toString());
        }
        AbstractBaseElementsImpl<Unstable> baseElements = 
                valueOf(list, ABSTRACT_XLIST_BASE_ELEMENTS);
        BaseEntries<Unstable, Object> inversedEntries = 
                valueOf(baseElements, BASE_ELEMENTS_INVERSED_ENTRIES);
        Assert.assertEquals(elements.length, inversedEntries.size()); //very important
        for (Unstable e : elements) {
            Assert.assertTrue(inversedEntries.containsKey(e));
            Assert.assertTrue(baseElements.contains(0, 0, e));
        }
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
        EventCollector(List<?> list) {
            if (list instanceof MAList<?>) {
                this.modifications = new ArrayList<>();
                this.modificationTexts = new ArrayList<>();
                this.eventLogs = new ArrayList<>();
                MAList<Object> maList = (MAList<Object>)list;
                maList.addListElementListener(
                        new ListElementListener<Object>() {
                            @Override
                            public void modifying(ListElementEvent<Object> e) throws Throwable {
                                EventCollector.this.modifying(e);
                            }

                            @Override
                            public void modified(ListElementEvent<Object> e) throws Throwable {
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

        private void modifying(ListElementEvent<Object> e) {
            this.append(e, false);
        }
        
        private void modified(ListElementEvent<Object> e) {
            this.append(e, true);
        }
        
        private void append(ListElementEvent<Object> e, boolean post) {
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
                .append(e.getIndex(PropertyVersion.DETACH))
                .append(", ")
                .append(e.getElement(PropertyVersion.DETACH));
            }
            if (e.getModificationType() == ModificationType.REPLACE) {
                builder.append(", ");
            }
            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                builder
                .append(e.getIndex(PropertyVersion.ATTACH))
                .append(", ")
                .append(e.getElement(PropertyVersion.ATTACH));
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
}
