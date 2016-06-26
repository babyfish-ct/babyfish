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
package org.babyfish.test.hibernate.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAList;
import org.babyfish.collection.MAList.MAListIterator;
import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.event.ListElementListener;
import org.babyfish.collection.event.ListElementModificationAware;
import org.babyfish.collection.event.modification.ListIteratorModifications;
import org.babyfish.collection.event.modification.ListModifications;
import org.babyfish.collection.viewinfo.ListViewInfos;
import org.babyfish.collection.viewinfo.ListViewInfos.SubList;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public abstract class MAListTest {
    
    private MAList<String> list;
    
    private Modification<String> expectedModification;
    
    private Modification<String> expectedIteratorModification;
    
    private String events;
    
    private String attachingIndexs;
    
    private String attachedIndexs;
    
    private String detachingIndexs;
    
    private String detachedIndexs;
    
    private String attachingElements;
    
    private String attachedElements;
    
    private String detachingElements;
    
    private String detachedElements;
    
    protected abstract MAList<String> createMAList();
    
    @Before
    public void initialize() {
        this.list = this.createMAList();
        this.expectedModification = null;
        this.expectedIteratorModification = null;
        this.events = "";
        this.attachingIndexs = "";
        this.attachedIndexs = "";
        this.detachingIndexs = "";
        this.detachedIndexs = "";
        this.attachingElements = "";
        this.attachedElements = "";
        this.detachingElements = "";
        this.detachedElements = "";
    }
    
    @Test
    public void testAdd() {
        
        this.handleEvents();
        
        this.expectedModification = ListModifications.add("B");
        this.list.add("B");
        this.expectedModification = ListModifications.add("D");
        this.list.add("D");
        this.expectedModification = ListModifications.add(1, "C");
        this.list.add(1, "C");
        this.expectedModification = ListModifications.add(0, "A");
        this.list.add(0, "A");
        this.expectedModification = ListModifications.add(4, "E");
        this.list.add(4, "E");
        
        Assert.assertEquals("-Ai-Ae-Ai-Ae-Ai-Ae-Ai-Ae-Ai-Ae", this.events);
        Assert.assertEquals("-0-1-1-0-4", this.attachingIndexs);
        Assert.assertEquals("-B-D-C-A-E", this.attachingElements);
        Assert.assertEquals("-0-1-1-0-4", this.attachedIndexs);
        Assert.assertEquals("-B-D-C-A-E", this.attachedElements);
        Assert.assertEquals("", this.detachingIndexs);
        Assert.assertEquals("", this.detachingElements);
        Assert.assertEquals("", this.detachedIndexs);
        Assert.assertEquals("", this.detachedElements);
        
        validateList(this.list, "A", "B", "C", "D", "E");
    }
    
    @Test
    public void testAddAll() {
        
        this.handleEvents();
        this.expectedModification = ListModifications.addAll(collection("C", "F"));
        this.list.addAll(collection("C", "F"));
        this.expectedModification = ListModifications.addAll(1, collection("D", "E"));
        this.list.addAll(1, collection("D", "E"));
        this.expectedModification = ListModifications.addAll(0, collection("A", "B"));
        this.list.addAll(0, collection("A", "B"));
        this.expectedModification = ListModifications.addAll(6, collection("G"));
        this.list.addAll(6, collection("G"));
        this.expectedModification = ListModifications.addAll(0, collection(new String[0]));
        this.list.addAll(0, collection(new String[0]));
        this.expectedModification = ListModifications.addAll(0, collection("X", "X"));
        this.list.addAll(0, collection("X", "X"));
        this.expectedModification = ListModifications.addAll(collection("X", "X"));
        this.list.addAll(collection("X", "X"));
        
        Assert.assertEquals("-Ai-Ai-Ae-Ae-Ai-Ai-Ae-Ae-Ai-Ai-Ae-Ae-Ai-Ae-Ai-Ai-Ae-Ae-Ai-Ai-Ae-Ae", this.events);
        Assert.assertEquals("-0-1-1-2-0-1-6-0-1-9-10", this.attachingIndexs);
        Assert.assertEquals("-C-F-D-E-A-B-G-X-X-X-X", this.attachingElements);
        Assert.assertEquals("-0-1-1-2-0-1-6-0-1-9-10", this.attachedIndexs);
        Assert.assertEquals("-C-F-D-E-A-B-G-X-X-X-X", this.attachedElements);
        Assert.assertEquals("", this.detachingIndexs);
        Assert.assertEquals("", this.detachingElements);
        Assert.assertEquals("", this.detachedIndexs);
        Assert.assertEquals("", this.detachedElements);
        
        validateList(this.list, "X", "X", "A", "B", "C", "D", "E", "F", "G", "X", "X");
    }
    
    @Test
    public void testClear() {
        
        this.list.addAll(collection("A", "B", "C"));
        this.handleEvents();
        
        this.expectedModification = ListModifications.<String>clear();
        this.list.clear();
        
        Assert.assertEquals("-Di-Di-Di-De-De-De", this.events);
        Assert.assertEquals("", this.attachingIndexs);
        Assert.assertEquals("", this.attachingElements);
        Assert.assertEquals("", this.attachedIndexs);
        Assert.assertEquals("", this.attachedElements);
        Assert.assertEquals("-0-1-2", this.detachingIndexs);
        Assert.assertEquals("-A-B-C", this.detachingElements);
        Assert.assertEquals("-0-1-2", this.detachedIndexs);
        Assert.assertEquals("-A-B-C", this.detachedElements);
        
        validateList(this.list);
    }
    
    @Test
    public void testRemoveByIndex() {
        
        this.list.addAll(collection("A", "B", "C", "D", "E"));
        this.handleEvents();
        
        this.expectedModification = ListModifications.remove(0);
        this.list.remove(0);
        this.expectedModification = ListModifications.remove(3);
        this.list.remove(3);
        this.expectedModification = ListModifications.remove(1);
        this.list.remove(1);
        
        Assert.assertEquals("-Di-De-Di-De-Di-De", this.events);
        Assert.assertEquals("", this.attachingIndexs);
        Assert.assertEquals("", this.attachingElements);
        Assert.assertEquals("", this.attachedIndexs);
        Assert.assertEquals("", this.attachedElements);
        Assert.assertEquals("-0-3-1", this.detachingIndexs);
        Assert.assertEquals("-A-E-C", this.detachingElements);
        Assert.assertEquals("-0-3-1", this.detachedIndexs);
        Assert.assertEquals("-A-E-C", this.detachedElements);
        
        validateList(this.list, "B", "D");
    }
    
    @Test
    public void testRemoveByObject() {
        this.list.addAll(collection("A", "B", "B", "B", "C", "C", "C", "D"));
        this.handleEvents();
        
        this.expectedModification = ListModifications.remove("A");
        Assert.assertTrue(this.list.remove("A"));
        this.expectedModification = ListModifications.remove("B");
        Assert.assertTrue(this.list.remove("B"));
        this.expectedModification = ListModifications.remove("C");
        Assert.assertTrue(this.list.remove("C"));
        this.expectedModification = ListModifications.remove("D");
        Assert.assertTrue(this.list.remove("D"));
        Assert.assertFalse(this.list.remove("X"));
        
        Assert.assertEquals("-Di-De-Di-De-Di-De-Di-De", this.events);
        Assert.assertEquals("", this.attachingIndexs);
        Assert.assertEquals("", this.attachingElements);
        Assert.assertEquals("", this.attachedIndexs);
        Assert.assertEquals("", this.attachedElements);
        Assert.assertEquals("-0-0-2-4", this.detachingIndexs);
        Assert.assertEquals("-A-B-C-D", this.detachingElements);
        Assert.assertEquals("-0-0-2-4", this.detachedIndexs);
        Assert.assertEquals("-A-B-C-D", this.detachedElements);
        
        validateList(this.list, "B", "B", "C", "C");
    }
    
    @Test
    public void testRemoveAll() {
        this.list.addAll(collection("A", "B", "C", "D", "A", "B", "C", "D"));
        this.handleEvents();
        
        this.expectedModification = ListModifications.removeAll(
                collection("E", "C", "B", "B"));
        this.list.removeAll(collection("E", "C", "B", "B"));
        
        Assert.assertEquals("-Di-Di-Di-Di-De-De-De-De", this.events);
        Assert.assertEquals("", this.attachingIndexs);
        Assert.assertEquals("", this.attachingElements);
        Assert.assertEquals("", this.attachedIndexs);
        Assert.assertEquals("", this.attachedElements);
        Assert.assertEquals("-1-2-5-6", this.detachingIndexs);
        Assert.assertEquals("-B-C-B-C", this.detachingElements);
        Assert.assertEquals("-1-2-5-6", this.detachedIndexs);
        Assert.assertEquals("-B-C-B-C", this.detachedElements);
        
        validateList(this.list, "A", "D", "A", "D");
    }
    
    @Test
    public void testRetainAll() {
        this.list.addAll(collection("A", "B", "C", "D", "A", "B", "C", "D"));
        this.handleEvents();
        
        this.expectedModification = ListModifications.retainAll(
                collection("E", "C", "B", "B"));
        this.list.retainAll(collection("E", "C", "B", "B"));
        
        Assert.assertEquals("-Di-Di-Di-Di-De-De-De-De", this.events);
        Assert.assertEquals("", this.attachingIndexs);
        Assert.assertEquals("", this.attachingElements);
        Assert.assertEquals("", this.attachedIndexs);
        Assert.assertEquals("", this.attachedElements);
        Assert.assertEquals("-0-3-4-7", this.detachingIndexs);
        Assert.assertEquals("-A-D-A-D", this.detachingElements);
        Assert.assertEquals("-0-3-4-7", this.detachedIndexs);
        Assert.assertEquals("-A-D-A-D", this.detachedElements);
        
        validateList(this.list, "B", "C", "B", "C");
    }
    
    @Test
    public void testSet() {
        this.list.addAll(collection("A", "B", "C", "D", "E", "F", "G"));
        this.handleEvents();
        handleChangeEvents(this.list);
        
        final int increase = 'a' - 'A';
        for (int i = 0; i < this.list.size(); i++) {
            char c = this.list.get(i).charAt(0);
            String newC = Character.toString((char)(c + increase));
            this.expectedModification = ListModifications.set(i, newC);
            this.list.set(i, newC);
        }
        
        Assert.assertEquals("-Di-Ai-De-Ae-Di-Ai-De-Ae-Di-Ai-De-Ae-Di-Ai-De-Ae-Di-Ai-De-Ae-Di-Ai-De-Ae-Di-Ai-De-Ae", this.events);
        Assert.assertEquals("-a-b-c-d-e-f-g", this.attachingElements);
        Assert.assertEquals("-0-1-2-3-4-5-6", this.attachingIndexs);
        Assert.assertEquals("-a-b-c-d-e-f-g", this.attachedElements);
        Assert.assertEquals("-0-1-2-3-4-5-6", this.attachedIndexs);
        Assert.assertEquals("-A-B-C-D-E-F-G", this.detachingElements);
        Assert.assertEquals("-0-1-2-3-4-5-6", this.detachingIndexs);
        Assert.assertEquals("-A-B-C-D-E-F-G", this.detachedElements);
        Assert.assertEquals("-0-1-2-3-4-5-6", this.detachedIndexs);
        validateList(this.list, "a", "b", "c", "d", "e", "f", "g");
    }
    
    @Test
    public void testIteratorNextAndRemove() {
        this.list.addAll(collection("A", "B", "C", "D", "E", "F", "G"));
        this.handleEvents();
        ListIterator<String> iterator = this.list.iterator();
        boolean remove = false;
        String elements = "";
        while (iterator.hasNext()) {
            String element = iterator.next();
            elements += '-';
            elements += element;
            if (remove) {
                this.expectedIteratorModification = ListIteratorModifications.remove();
                iterator.remove();
            }
            remove = !remove;
        }
        Assert.assertEquals("-A-B-C-D-E-F-G", elements);
        Assert.assertEquals("-Di-De-Di-De-Di-De", this.events);
        Assert.assertEquals("-B-D-F", this.detachingElements);
        Assert.assertEquals("-1-2-3", this.detachingIndexs);
        Assert.assertEquals("-B-D-F", this.detachedElements);
        Assert.assertEquals("-1-2-3", this.detachedIndexs);
        Assert.assertEquals("", this.attachingElements);
        Assert.assertEquals("", this.attachingIndexs);
        Assert.assertEquals("", this.attachedElements);
        Assert.assertEquals("", this.attachedIndexs);
        validateList(this.list, "A", "C", "E", "G");
    }
    
    @Test
    public void testListIteratorNextAndRemove() {
        this.list.addAll(collection("A", "B", "C", "D", "E", "F", "G"));
        this.handleEvents();
        ListIterator<String> iterator = this.list.listIterator();
        boolean remove = false;
        String elements = "";
        while (iterator.hasNext()) {
            String element = iterator.next();
            elements += '-';
            elements += element;
            if (remove) {
                this.expectedIteratorModification = ListIteratorModifications.remove();
                iterator.remove();
            }
            remove = !remove;
        }
        Assert.assertEquals("-A-B-C-D-E-F-G", elements);
        Assert.assertEquals("-Di-De-Di-De-Di-De", this.events);
        Assert.assertEquals("-B-D-F", this.detachingElements);
        Assert.assertEquals("-1-2-3", this.detachingIndexs);
        Assert.assertEquals("-B-D-F", this.detachedElements);
        Assert.assertEquals("-1-2-3", this.detachedIndexs);
        Assert.assertEquals("", this.attachingElements);
        Assert.assertEquals("", this.attachingIndexs);
        Assert.assertEquals("", this.attachedElements);
        Assert.assertEquals("", this.attachedIndexs);
        validateList(this.list, "A", "C", "E", "G");
    }
    
    @Test
    public void testListIteratorPreviousAndRemove() {
        this.list.addAll(collection("A", "B", "C", "D", "E", "F", "G"));
        this.handleEvents();
        ListIterator<String> iterator = this.list.listIterator(this.list.size());
        boolean remove = false;
        String elements = "";
        while (iterator.hasPrevious()) {
            String element = iterator.previous();
            elements += '-';
            elements += element;
            if (remove) {
                this.expectedIteratorModification = ListIteratorModifications.remove();
                iterator.remove();
            }
            remove = !remove;
        }
        Assert.assertEquals("-G-F-E-D-C-B-A", elements);
        Assert.assertEquals("-Di-De-Di-De-Di-De", this.events);
        Assert.assertEquals("-F-D-B", this.detachingElements);
        Assert.assertEquals("-5-3-1", this.detachingIndexs);
        Assert.assertEquals("-F-D-B", this.detachedElements);
        Assert.assertEquals("-5-3-1", this.detachedIndexs);
        Assert.assertEquals("", this.attachingElements);
        Assert.assertEquals("", this.attachingIndexs);
        Assert.assertEquals("", this.attachedElements);
        Assert.assertEquals("", this.attachedIndexs);
        validateList(this.list, "A", "C", "E", "G");
    }
    
    @Test
    public void testListIteratorAddToHead() {
        this.list.add("D");
        this.handleEvents();
        ListIterator<String> iterator = this.list.listIterator();
        this.expectedIteratorModification = ListIteratorModifications.add("A");
        iterator.add("A");
        this.expectedIteratorModification = ListIteratorModifications.add("B");
        iterator.add("B");
        this.expectedIteratorModification = ListIteratorModifications.add("C");
        iterator.add("C");
        Assert.assertEquals("-Ai-Ae-Ai-Ae-Ai-Ae", this.events);
        Assert.assertEquals("-A-B-C", this.attachingElements);
        Assert.assertEquals("-0-1-2", this.attachingIndexs);
        Assert.assertEquals("-A-B-C", this.attachedElements);
        Assert.assertEquals("-0-1-2", this.attachedIndexs);
        Assert.assertEquals("", this.detachingElements);
        Assert.assertEquals("", this.detachingIndexs);
        Assert.assertEquals("", this.detachedElements);
        Assert.assertEquals("", this.detachedIndexs);
        validateList(this.list, "A", "B", "C", "D");
    }
    
    @Test
    public void testListIteratorNextAndAdd() {
        this.list.addAll(collection("A", "C", "E", "G"));
        this.handleEvents();
        ListIterator<String> iterator = this.list.listIterator();
        while (iterator.hasNext()) {
            char element = iterator.next().charAt(0);
            String newElement = Character.toString((char)(element + 1));
            this.expectedIteratorModification = ListIteratorModifications.add(newElement);
            iterator.add(newElement);
        }
        Assert.assertEquals("-Ai-Ae-Ai-Ae-Ai-Ae-Ai-Ae", this.events);
        Assert.assertEquals("-B-D-F-H", this.attachingElements);
        Assert.assertEquals("-1-3-5-7", this.attachingIndexs);
        Assert.assertEquals("-B-D-F-H", this.attachedElements);
        Assert.assertEquals("-1-3-5-7", this.attachedIndexs);
        Assert.assertEquals("", this.detachingElements);
        Assert.assertEquals("", this.detachingIndexs);
        Assert.assertEquals("", this.detachedElements);
        Assert.assertEquals("", this.detachedIndexs);
        validateList(this.list, "A", "B", "C", "D", "E", "F", "G", "H");
    }
    
    @Test
    public void testListIteratorPreviousAndAdd() {
        this.list.addAll(collection("B", "D", "F", "H"));
        this.handleEvents();
        ListIterator<String> iterator = this.list.listIterator(this.list.size());
        while (iterator.hasPrevious()) {
            char element = iterator.previous().charAt(0);
            String newElement = Character.toString((char)(element - 1));
            this.expectedIteratorModification = ListIteratorModifications.add(newElement);
            iterator.add(newElement);
            iterator.previous();
        }
        Assert.assertEquals("-Ai-Ae-Ai-Ae-Ai-Ae-Ai-Ae", this.events);
        Assert.assertEquals("-G-E-C-A", this.attachingElements);
        Assert.assertEquals("-3-2-1-0", this.attachingIndexs);
        Assert.assertEquals("-G-E-C-A", this.attachedElements);
        Assert.assertEquals("-3-2-1-0", this.attachedIndexs);
        Assert.assertEquals("", this.detachingElements);
        Assert.assertEquals("", this.detachingIndexs);
        Assert.assertEquals("", this.detachedElements);
        Assert.assertEquals("", this.detachedIndexs);
        validateList(this.list, "A", "B", "C", "D", "E", "F", "G", "H");
    }
    
    @Test
    public void testListIteratorSet() {
        this.list.addAll(collection("A", "B", "C", "D", "E", "F", "G"));
        this.handleEvents();
        final int increase = 'a' - 'A';
        MAListIterator<String> iterator = this.list.listIterator();
        
        handleChangeEvents(this.list);
        handleChangeEvents(iterator);
        
        while (iterator.hasNext()) {
            char element = iterator.next().charAt(0);
            String newElement = Character.toString((char)(element + increase));
            this.expectedIteratorModification = ListIteratorModifications.set(newElement);
            iterator.set(newElement);
        }
        Assert.assertEquals("-Di-Ai-De-Ae-Di-Ai-De-Ae-Di-Ai-De-Ae-Di-Ai-De-Ae-Di-Ai-De-Ae-Di-Ai-De-Ae-Di-Ai-De-Ae", this.events);
        Assert.assertEquals("-a-b-c-d-e-f-g", this.attachingElements);
        Assert.assertEquals("-0-1-2-3-4-5-6", this.attachingIndexs);
        Assert.assertEquals("-a-b-c-d-e-f-g", this.attachedElements);
        Assert.assertEquals("-0-1-2-3-4-5-6", this.attachedIndexs);
        Assert.assertEquals("-A-B-C-D-E-F-G", this.detachingElements);
        Assert.assertEquals("-0-1-2-3-4-5-6", this.detachingIndexs);
        Assert.assertEquals("-A-B-C-D-E-F-G", this.detachedElements);
        Assert.assertEquals("-0-1-2-3-4-5-6", this.detachedIndexs);
        validateList(this.list, "a", "b", "c", "d", "e", "f", "g");
    }
    
    @Test
    public void testSubListChainByLastSubList() {
        /*
         * It's difference for others. It does not call handleEvents
         */
        this.list.addAll(collection("A", "B", "C", "D", "E", "F", "G"));
        @SuppressWarnings("unchecked")
        MAList<String>[] lists = new MAList[5];
        final String[] attachingIndexsGroups = new String[lists.length];
        final String[] detachingIndexsGroups = new String[lists.length];
        final String[] attachedIndexsGroups = new String[lists.length];
        final String[] detachedIndexsGroups = new String[lists.length];
        final ListElementEvent<?>[] eventsGroups = new ListElementEvent[lists.length];
        final Modification<?>[] expectedModifications = new Modification[lists.length];
        
        for (int i = 0; i < attachingIndexsGroups.length; i++) {
            attachingIndexsGroups[i] = "";
        }
        for (int i = 0; i < attachedIndexsGroups.length; i++) {
            attachedIndexsGroups[i] = "";
        }
        for (int i = 0; i < detachingIndexsGroups.length; i++) {
            detachingIndexsGroups[i] = "";
        }
        for (int i = 0; i < detachedIndexsGroups.length; i++) {
            detachedIndexsGroups[i] = "";
        }
        
        for (int i = 0; i < lists.length; i++) {
            if (i != 0) {
                lists[i] = lists[i - 1].subList(1, lists[i - 1].size());
            } else {
                lists[i] = this.list;
            }
            final int listIndex = i;
            handleChangeEvents(lists[i]);
            lists[i].addListElementListener(new ListElementListener<String>() {

                @Override
                public void modifying(ListElementEvent<String> e) {
                    assertModificationEquals(expectedModifications[listIndex], e.getModification());
                    if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                        detachingIndexsGroups[listIndex] += "-" + e.getIndex(PropertyVersion.DETACH);
                    }
                    if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                        attachingIndexsGroups[listIndex] += "-" + e.getIndex(PropertyVersion.ATTACH);
                    }
                    eventsGroups[listIndex] = e;
                    if (e.getCause() != null) {
                        Assert.assertTrue(e.getCause().getViewInfo() instanceof SubList);
                        Assert.assertSame(eventsGroups[listIndex + 1], e.getCause().getViewEvent());
                    }
                }
                
                @Override
                public void modified(ListElementEvent<String> e) {
                    assertModificationEquals(expectedModifications[listIndex], e.getModification());
                    if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                        detachedIndexsGroups[listIndex] += "-" + e.getIndex(PropertyVersion.DETACH);
                    }
                    if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                        attachedIndexsGroups[listIndex] += "-" + e.getIndex(PropertyVersion.ATTACH);
                    }
                    eventsGroups[listIndex] = e;
                    if (e.getCause() != null) {
                        Assert.assertTrue(e.getCause().getViewInfo() instanceof SubList);
                        Assert.assertSame(eventsGroups[listIndex + 1], e.getCause().getViewEvent());
                    }
                }
            });
        }
        
        MAList<String> listToModify = lists[lists.length - 1];
        expectedModifications[4] = ListModifications.set(0, "e");
        expectedModifications[3] = null;
        expectedModifications[2] = null;
        expectedModifications[1] = null;
        expectedModifications[0] = null;
        listToModify.set(0, "e");
        expectedModifications[4] = ListModifications.set(1, "f");
        expectedModifications[3] = null;
        expectedModifications[2] = null;
        expectedModifications[1] = null;
        expectedModifications[0] = null;
        listToModify.set(1, "f");
        expectedModifications[4] = ListModifications.set(2, "g");
        expectedModifications[3] = null;
        expectedModifications[2] = null;
        expectedModifications[1] = null;
        expectedModifications[0] = null;
        listToModify.set(2, "g");
        
        Assert.assertEquals("-0-1-2", attachingIndexsGroups[4]);
        Assert.assertEquals("-1-2-3", attachingIndexsGroups[3]);
        Assert.assertEquals("-2-3-4", attachingIndexsGroups[2]);
        Assert.assertEquals("-3-4-5", attachingIndexsGroups[1]);
        Assert.assertEquals("-4-5-6", attachingIndexsGroups[0]);
        
        Assert.assertEquals("-0-1-2", attachedIndexsGroups[4]);
        Assert.assertEquals("-1-2-3", attachedIndexsGroups[3]);
        Assert.assertEquals("-2-3-4", attachedIndexsGroups[2]);
        Assert.assertEquals("-3-4-5", attachedIndexsGroups[1]);
        Assert.assertEquals("-4-5-6", attachedIndexsGroups[0]);
        
        Assert.assertEquals("-0-1-2", detachingIndexsGroups[4]);
        Assert.assertEquals("-1-2-3", detachingIndexsGroups[3]);
        Assert.assertEquals("-2-3-4", detachingIndexsGroups[2]);
        Assert.assertEquals("-3-4-5", detachingIndexsGroups[1]);
        Assert.assertEquals("-4-5-6", detachingIndexsGroups[0]);

        Assert.assertEquals("-0-1-2", detachedIndexsGroups[4]);
        Assert.assertEquals("-1-2-3", detachedIndexsGroups[3]);
        Assert.assertEquals("-2-3-4", detachedIndexsGroups[2]);
        Assert.assertEquals("-3-4-5", detachedIndexsGroups[1]);
        Assert.assertEquals("-4-5-6", detachedIndexsGroups[0]);
        
        validateList(this.list, "A", "B", "C", "D", "e", "f", "g");
    }
    
    @Test
    public void testSubListChainByLastSubIterator() {
        /*
         * It's difference for others. It does not call handleEvents
         */
        this.list.addAll(collection("A", "B", "C", "D", "E", "F", "G"));
        @SuppressWarnings("unchecked")
        MAList<String>[] lists = new MAList[5];
        final String[] attachingIndexsGroups = new String[lists.length];
        final String[] detachingIndexsGroups = new String[lists.length];
        final String[] attachedIndexsGroups = new String[lists.length];
        final String[] detachedIndexsGroups = new String[lists.length];
        final ListElementEvent<?>[] eventsGroups = new ListElementEvent[lists.length];
        final Modification<?>[] expectedModifications = new Modification[lists.length];
        
        for (int i = 0; i < attachingIndexsGroups.length; i++) {
            attachingIndexsGroups[i] = "";
        }
        for (int i = 0; i < attachedIndexsGroups.length; i++) {
            attachedIndexsGroups[i] = "";
        }
        for (int i = 0; i < detachingIndexsGroups.length; i++) {
            detachingIndexsGroups[i] = "";
        }
        for (int i = 0; i < detachedIndexsGroups.length; i++) {
            detachedIndexsGroups[i] = "";
        }
        
        for (int i = 0; i < lists.length; i++) {
            if (i != 0) {
                lists[i] = lists[i - 1].subList(1, lists[i - 1].size());
            } else {
                lists[i] = this.list;
            }
            final int listIndex = i;
            lists[i].addListElementListener(new ListElementListener<String>() {
                
                @Override
                public void modifying(ListElementEvent<String> e)
                        throws Throwable {
                    Assert.assertEquals(expectedModifications[listIndex], e.getModification());
                    if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                        detachingIndexsGroups[listIndex] += "-" + e.getIndex(PropertyVersion.DETACH);
                    }
                    if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                        attachingIndexsGroups[listIndex] += "-" + e.getIndex(PropertyVersion.ATTACH);
                    }
                    eventsGroups[listIndex] = e;
                    if (e.getCause() != null) {
                        if (e.getCause().getViewEvent().getSource() instanceof ListIterator) {
                            Assert.assertTrue(e.getCause().getViewInfo() instanceof ListViewInfos.ListIterator);
                        }
                        else {
                            Assert.assertTrue(e.getCause().getViewInfo() instanceof SubList);
                            Assert.assertSame(eventsGroups[listIndex + 1], e.getCause().getViewEvent());
                        }
                    }
                }
                
                @Override
                public void modified(ListElementEvent<String> e)
                        throws Throwable {
                    Assert.assertEquals(expectedModifications[listIndex], e.getModification());
                    if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                        detachedIndexsGroups[listIndex] += "-" + e.getIndex(PropertyVersion.DETACH);
                    }
                    if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                        attachedIndexsGroups[listIndex] += "-" + e.getIndex(PropertyVersion.ATTACH);
                    }
                    eventsGroups[listIndex] = e;
                    if (e.getCause() != null) {
                        if (e.getCause().getViewEvent().getSource() instanceof ListIterator) {
                            Assert.assertTrue(e.getCause().getViewInfo() instanceof ListViewInfos.ListIterator);
                        }
                        else {
                            Assert.assertTrue(e.getCause().getViewInfo() instanceof SubList);
                            Assert.assertSame(eventsGroups[listIndex + 1], e.getCause().getViewEvent());
                        }
                    }
                }
            });
        }
        
        ListIterator<String> listToModify = lists[lists.length - 1].listIterator();
        listToModify.next();
        listToModify.set("e");
        listToModify.next();
        listToModify.set("f");
        listToModify.next();
        listToModify.set("g");
        
        Assert.assertEquals("-0-1-2", attachingIndexsGroups[4]);
        Assert.assertEquals("-1-2-3", attachingIndexsGroups[3]);
        Assert.assertEquals("-2-3-4", attachingIndexsGroups[2]);
        Assert.assertEquals("-3-4-5", attachingIndexsGroups[1]);
        Assert.assertEquals("-4-5-6", attachingIndexsGroups[0]);
        
        Assert.assertEquals("-0-1-2", attachedIndexsGroups[4]);
        Assert.assertEquals("-1-2-3", attachedIndexsGroups[3]);
        Assert.assertEquals("-2-3-4", attachedIndexsGroups[2]);
        Assert.assertEquals("-3-4-5", attachedIndexsGroups[1]);
        Assert.assertEquals("-4-5-6", attachedIndexsGroups[0]);
        
        Assert.assertEquals("-0-1-2", detachingIndexsGroups[4]);
        Assert.assertEquals("-1-2-3", detachingIndexsGroups[3]);
        Assert.assertEquals("-2-3-4", detachingIndexsGroups[2]);
        Assert.assertEquals("-3-4-5", detachingIndexsGroups[1]);
        Assert.assertEquals("-4-5-6", detachingIndexsGroups[0]);

        Assert.assertEquals("-0-1-2", detachedIndexsGroups[4]);
        Assert.assertEquals("-1-2-3", detachedIndexsGroups[3]);
        Assert.assertEquals("-2-3-4", detachedIndexsGroups[2]);
        Assert.assertEquals("-3-4-5", detachedIndexsGroups[1]);
        Assert.assertEquals("-4-5-6", detachedIndexsGroups[0]);
        
        validateList(this.list, "A", "B", "C", "D", "e", "f", "g");
    }
    
    private void handleEvents() {
        this.list.addListElementListener(new ListElementListener<String>() {
            
            @Override
            public void modifying(ListElementEvent<String> e) throws Throwable {
                assertModificationEquals(MAListTest.this.expectedModification, e.getModification());
                Assert.assertTrue((e.getModification() == null) != (e.getCause() == null));
                if (MAListTest.this.expectedIteratorModification != null) {
                    assertModificationEquals(
                            MAListTest.this.expectedIteratorModification, 
                            e.getCause().getViewEvent().getModification());
                }
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    MAListTest.this.events += "-Di";
                    MAListTest.this.detachingIndexs += "-" + e.getIndex(PropertyVersion.DETACH);
                    MAListTest.this.detachingElements += "-" + e.getElement(PropertyVersion.DETACH);
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    MAListTest.this.events += "-Ai";
                    MAListTest.this.attachingIndexs += "-" + e.getIndex(PropertyVersion.ATTACH);
                    MAListTest.this.attachingElements += "-" + e.getElement(PropertyVersion.ATTACH);
                }
            }
            
            @Override
            public void modified(ListElementEvent<String> e) throws Throwable {
                assertModificationEquals(MAListTest.this.expectedModification, e.getModification());
                Assert.assertTrue((e.getModification() == null) != (e.getCause() == null));
                if (MAListTest.this.expectedIteratorModification != null) {
                    assertModificationEquals(
                            MAListTest.this.expectedIteratorModification, 
                            e.getCause().getViewEvent().getModification());
                }
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    MAListTest.this.events += "-De";
                    MAListTest.this.detachedIndexs += "-" + e.getIndex(PropertyVersion.DETACH);
                    MAListTest.this.detachedElements += "-" + e.getElement(PropertyVersion.DETACH);
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    MAListTest.this.events += "-Ae";
                    MAListTest.this.attachedIndexs += "-" + e.getIndex(PropertyVersion.ATTACH);
                    MAListTest.this.attachedElements += "-" + e.getElement(PropertyVersion.ATTACH);
                }
            }
        });
    }
    
    private static void handleChangeEvents(ListElementModificationAware<String> list) {
        list.addListElementListener(new ListElementListener<String>() {
            
            @Override
            public void modifying(ListElementEvent<String> e) throws Throwable {
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    char oldC = e.getElement(PropertyVersion.DETACH).charAt(0);
                    char newC = e.getElement(PropertyVersion.ATTACH).charAt(0);
                    Assert.assertEquals(oldC + 'a' - 'A', newC);
                }
            }

            @Override
            public void modified(ListElementEvent<String> e) throws Throwable {
                char oldC = e.getElement(PropertyVersion.DETACH).charAt(0);
                char newC = e.getElement(PropertyVersion.ATTACH).charAt(0);
                Assert.assertEquals(oldC + 'a' - 'A', newC);
            }
            
        });
    }
    
    @SuppressWarnings("unchecked")
    private static <E> Collection<E> collection(E ... elements) {
        List<E> list = new ArrayList<E>();
        for (E element : elements) {
            list.add(element);
        }
        return MACollections.unmodifiable(list);
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void validateList(List<E> list, E ... elements) {
        Assert.assertEquals(elements.length, list.size());
        for (int i = elements.length - 1; i >= 0; i--) {
            Assert.assertEquals(elements[i], list.get(i));
        }
    }
    
    private static void assertModificationEquals(
            org.babyfish.data.event.Modification a, 
            org.babyfish.data.event.Modification b) {
        if (a == b) {
            return;
        }
        if (a == null || b == null) {
            Assert.fail("One modification is null but the other is not null");
        }
        Assert.assertEquals(a.toString(), b.toString());
    }
}
