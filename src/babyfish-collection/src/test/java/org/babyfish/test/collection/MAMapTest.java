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
package org.babyfish.test.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.babyfish.collection.MACollection;
import org.babyfish.collection.MACollection.MAIterator;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.MASet;
import org.babyfish.collection.MASortedMap;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementEvent.MapModification;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.collection.event.modification.CollectionModifications;
import org.babyfish.collection.event.modification.IteratorModifications;
import org.babyfish.collection.event.modification.MapModifications;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.collection.viewinfo.MapViewInfos.EntrySet;
import org.babyfish.collection.viewinfo.MapViewInfos.KeySet;
import org.babyfish.collection.viewinfo.MapViewInfos.Values;
import org.babyfish.data.event.Cause;
import org.babyfish.data.event.ModificationType;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public abstract class MAMapTest {
    
    private final static int EVENT_ALL = -1;
    
    private final static int EVENT_MAP = 1 << 0;
    
    private final static int EVENT_ENTRY_SET = 1 << 1;
    
    private final static int EVENT_KEY_SET = 1 << 2;
    
    private final static int EVENT_VALUES = 1 << 3;
    
    private final static int EVENT_ENTRY_SET_ITERATOR = 1 << 4;
    
    private final static int EVENT_KEY_SET_ITERATOR = 1 << 5;
    
    private final static int EVENT_VALUES_ITERATOR = 1 << 6;

    private MAMap<String, String> map;
    
    private MapModification<String, String> expectedModification;
    
    private Class<?> expectedViewType;
    
    private Modification<Entry<String, String>> expectedEntrySetModification;
    
    private Class<?> expectedEntrySetViewType;
    
    private Modification<String> expectedKeySetModification;
    
    private Class<?> expectedKeySetViewType;
    
    private Modification<String> expectedValuesModification;
    
    private Class<?> expectedValuesViewType;
    
    private Modification<Entry<String, String>> expectedEntrySetIteratorModification;
    
    private Modification<String> expectedKeySetIteratorModification;
    
    private Modification<String> expectedValuesIteratorModification;
    
    private String eventSequence;
    
    private String entrySetEventSequence;
    
    private String entrySetIteratorEventSequence;
    
    private String keySetEventSequence;
    
    private String keySetIteratorEventSequence;
    
    private String valuesEventSequence;
    
    private String valuesIteratorEventSequence;
    
    private Collection<MapElementEvent<String, String>> attachingEvents;
    
    private Collection<MapElementEvent<String, String>> attachedEvents;
    
    private Collection<MapElementEvent<String, String>> detachingEvents;
    
    private Collection<MapElementEvent<String, String>> detachedEvents;
    
    private Collection<ElementEvent<Map.Entry<String, String>>> entrySetDetachingEvents;
    
    private Collection<ElementEvent<Map.Entry<String, String>>> entrySetDetachedEvents;
    
    private Collection<ElementEvent<String>> keySetDetachingEvents;
    
    private Collection<ElementEvent<String>> keySetDetachedEvents;
    
    private Collection<ElementEvent<String>> valuesDetachingEvents;
    
    private Collection<ElementEvent<String>> valuesDetachedEvents;
    
    private Collection<ElementEvent<Map.Entry<String, String>>> entrySetIteratorDetachingEvents;
    
    private Collection<ElementEvent<Map.Entry<String, String>>> entrySetIteratorDetachedEvents;
    
    private Collection<ElementEvent<String>> keySetIteratorDetachingEvents;
    
    private Collection<ElementEvent<String>> keySetIteratorDetachedEvents;
    
    private Collection<ElementEvent<String>> valuesIteratorDetachingEvents;
    
    private Collection<ElementEvent<String>> valuesIteratorDetachedEvents;
    
    protected abstract MAMap<String, String> createMAMap();
    
    @Before
    public void initialize() {
        this.map = this.createMAMap();
        this.expectedEntrySetModification = null;
        this.expectedEntrySetViewType = null;
        this.expectedKeySetModification = null;
        this.expectedKeySetViewType = null;
        this.expectedValuesModification = null;
        this.expectedValuesViewType = null;
        this.expectedEntrySetIteratorModification = null;
        this.expectedKeySetIteratorModification = null;
        this.expectedValuesIteratorModification = null;
        this.eventSequence = "";
        this.entrySetEventSequence = "";
        this.entrySetIteratorEventSequence = "";
        this.keySetEventSequence = "";
        this.keySetIteratorEventSequence = "";
        this.valuesEventSequence = "";
        this.valuesIteratorEventSequence = "";
        this.attachingEvents = new ArrayList<MapElementEvent<String,String>>();
        this.attachedEvents = new ArrayList<MapElementEvent<String,String>>();
        this.detachingEvents = new ArrayList<MapElementEvent<String,String>>();
        this.detachedEvents = new ArrayList<MapElementEvent<String,String>>();
        this.entrySetDetachingEvents = new ArrayList<ElementEvent<Entry<String,String>>>();
        this.entrySetDetachedEvents = new ArrayList<ElementEvent<Entry<String,String>>>();
        this.keySetDetachingEvents = new ArrayList<ElementEvent<String>>();
        this.keySetDetachedEvents = new ArrayList<ElementEvent<String>>();
        this.valuesDetachingEvents = new ArrayList<ElementEvent<String>>();
        this.valuesDetachedEvents = new ArrayList<ElementEvent<String>>();
        this.entrySetIteratorDetachingEvents = new ArrayList<ElementEvent<Entry<String,String>>>();
        this.entrySetIteratorDetachedEvents = new ArrayList<ElementEvent<Entry<String,String>>>();
        this.keySetIteratorDetachingEvents = new ArrayList<ElementEvent<String>>();
        this.keySetIteratorDetachedEvents = new ArrayList<ElementEvent<String>>();
        this.valuesIteratorDetachingEvents = new ArrayList<ElementEvent<String>>();
        this.valuesIteratorDetachedEvents = new ArrayList<ElementEvent<String>>();
    }
    
    @Test
    public void testPut() {
        this.handleMapEvents();
        
        this.expectedModification = MapModifications.put("A", "a");
        this.map.put("A", "a");
        this.validateEventsEmpty(EVENT_ALL & ~EVENT_MAP);
        Assert.assertEquals("-Ai-Ae", this.eventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents, "A", "a");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents, "A", "a");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a");
        
        this.expectedModification = MapModifications.put("B", "b");
        this.map.put("B", "b");
        this.validateEventsEmpty(EVENT_ALL & ~EVENT_MAP);
        Assert.assertEquals("-Ai-Ae", this.eventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents, "B", "b");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents, "B", "b");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "B", "b");
        
        this.expectedModification = MapModifications.put("A", "a");
        this.map.put("A", "a");
        this.validateEventsEmpty(EVENT_ALL & ~EVENT_MAP);
        Assert.assertEquals("-Di-Ai-De-Ae", this.eventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "A", "*a=>a");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "A", "*a=>a");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents, "A", "a=>*a");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents, "A", "a=>*a");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "B", "b");
        
        this.expectedModification = MapModifications.put("B", "a+1");
        this.map.put("B", "a+1");
        this.validateEventsEmpty(EVENT_ALL & ~EVENT_MAP);
        Assert.assertEquals("-Di-Ai-De-Ae", this.eventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "B", "*b=>a+1");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "B", "*b=>a+1");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents, "B", "b=>*a+1");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents, "B", "b=>*a+1");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "B", "a+1");
    }
    
    @Test
    public void testPutAll() {
        
        Map<String, String> map;
        this.handleMapEvents();
        
        map = linkedHashMap();
        this.expectedModification = MapModifications.putAll(map);
        this.map.putAll(map);
        this.validateEventsEmpty(EVENT_ALL);
        
        map = linkedHashMap("A", "a", "B", "b");
        this.expectedModification = MapModifications.putAll(map);
        this.map.putAll(map);
        this.validateEventsEmpty(EVENT_ALL & ~EVENT_MAP);
        Assert.assertEquals("-Ai-Ai-Ae-Ae", this.eventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents, "A", "a", "B", "b");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents, "A", "a", "B", "b");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "B", "b");
        
        map = linkedHashMap("B", "a+1", "C", "c");
        this.expectedModification = MapModifications.putAll(map);
        this.map.putAll(map);
        this.validateEventsEmpty(EVENT_ALL & ~EVENT_MAP);
        Assert.assertEquals("-Di-Ai-Ai-De-Ae-Ae", this.eventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "B", "*b=>a+1");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "B", "*b=>a+1");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents, "B", "b=>*a+1", "C", "c");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents, "B", "b=>*a+1", "C", "c");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "B", "a+1", "C", "c");
    }
    
    @Test
    public void testClear() {
        
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "C", "c"));
        this.handleMapEvents();
        
        this.expectedModification = MapModifications.<String, String>clear();
        this.map.clear();
        this.validateEventsEmpty(EVENT_ALL & ~EVENT_MAP);
        Assert.assertEquals("-Di-Di-Di-De-De-De", this.eventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "A", "a", "B", "b", "C", "c");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "A", "a", "B", "b", "C", "c");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.clearEvents(EVENT_ALL);
        this.validateMap();
    }
    
    @Test
    public void testRemove() {
        
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "C", "c"));
        this.handleMapEvents();
        
        this.expectedModification = MapModifications.<String, String>remove("B");
        this.map.remove("B");
        this.validateEventsEmpty(EVENT_ALL & ~EVENT_MAP);
        Assert.assertEquals("-Di-De", this.eventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "B", "b");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "B", "b");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "C", "c");
        
        this.expectedModification = MapModifications.<String, String>remove("B");
        this.map.remove("B");
        this.validateEventsEmpty(EVENT_ALL);
        this.validateMap("A", "a", "C", "c");
    }
    
    @SuppressWarnings("deprecation")
    @Test(expected = UnsupportedOperationException.class)
    public void testEntrySetAdd() {
        this.handleMapEvents();
        try {
            this.map.entrySet().add(entry("A", "a"));
        } catch (RuntimeException ex) {
            validateEventsEmpty(EVENT_ALL);
            throw ex;
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test(expected = UnsupportedOperationException.class)
    public void testEntrySetAddAll() {
        this.handleMapEvents();
        try {
            this.map.entrySet().addAll(MACollections.<String, String>emptyMap().entrySet());
        } catch (RuntimeException ex) {
            validateEventsEmpty(EVENT_ALL);
            throw ex;
        }
    }
    
    @Test
    public void testEntrySetClear() {
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "C", "c"));
        MASet<Entry<String, String>> entrySet = this.map.entrySet();
        this.handleMapEvents();
        this.handleEntrySetEvents(entrySet);
        
        this.expectedViewType = EntrySet.class;
        this.expectedEntrySetModification = CollectionModifications.<Entry<String, String>>clear();
        entrySet.clear();
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_ENTRY_SET));
        Assert.assertEquals("-Di-Di-Di-De-De-De", this.eventSequence);
        Assert.assertEquals("-Di-Di-Di-De-De-De", this.entrySetEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "A", "a", "B", "b", "C", "c");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "A", "a", "B", "b", "C", "c");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateEntrySetEvents(PropertyVersion.DETACH, this.entrySetDetachingEvents, "A", "a", "B", "b", "C", "c");
        this.validateEntrySetEvents(PropertyVersion.DETACH, this.entrySetDetachedEvents, "A", "a", "B", "b", "C", "c");
        this.clearEvents(EVENT_ALL);
        this.validateMap();
    }
    
    @Test
    public void testEntrySetRemove() {
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "C", "c"));
        MASet<Entry<String, String>> entrySet = this.map.entrySet();
        this.handleMapEvents();
        this.handleEntrySetEvents(entrySet);
        
        this.expectedViewType = EntrySet.class;
        this.expectedEntrySetModification = 
            CollectionModifications.remove(entry("B", "b"));
        entrySet.remove(entry("B", "b"));
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_ENTRY_SET));
        Assert.assertEquals("-Di-De", this.eventSequence);
        Assert.assertEquals("-Di-De", this.entrySetEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "B", "b");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "B", "b");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateEntrySetEvents(PropertyVersion.DETACH, this.entrySetDetachingEvents, "B", "b");
        this.validateEntrySetEvents(PropertyVersion.DETACH, this.entrySetDetachedEvents, "B", "b");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "C", "c");
        
        this.expectedViewType = null;
        this.expectedEntrySetModification = null;
        entrySet.remove(entry("A", "A"));
        validateEventsEmpty(EVENT_ALL);
        this.validateMap("A", "a", "C", "c");
        
        this.expectedViewType = null;
        this.expectedEntrySetModification = null;
        entrySet.remove(entry("c", "c"));
        validateEventsEmpty(EVENT_ALL);
        this.validateMap("A", "a", "C", "c");
    }
    
    @Test
    public void testEntrySetRemoveAll() {
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "C", "c", "D", "d"));
        MASet<Entry<String, String>> entrySet = this.map.entrySet();
        this.handleMapEvents();
        this.handleEntrySetEvents(entrySet);
        
        Set<Entry<String, String>> toRemove = 
            linkedHashMap("A", "a", "B", "B", "c", "c", "D", "d", "E", "e").entrySet();
        this.expectedViewType = EntrySet.class;
        this.expectedEntrySetModification = CollectionModifications.<Entry<String, String>>removeAll(toRemove);
        entrySet.removeAll(toRemove);
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_ENTRY_SET));
        Assert.assertEquals("-Di-Di-De-De", this.eventSequence);
        Assert.assertEquals("-Di-Di-De-De", this.entrySetEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "A", "a", "D", "d");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "A", "a", "D", "d");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateEntrySetEvents(PropertyVersion.DETACH, this.entrySetDetachingEvents, "A", "a", "D", "d");
        this.validateEntrySetEvents(PropertyVersion.DETACH, this.entrySetDetachedEvents, "A", "a", "D", "d");
        this.clearEvents(EVENT_ALL);
        this.validateMap("B", "b", "C", "c");
    }
    
    @Test
    public void testEntrySetRetainAll() {
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "C", "c", "D", "d"));
        MASet<Entry<String, String>> entrySet = this.map.entrySet();
        this.handleMapEvents();
        this.handleEntrySetEvents(entrySet);
        
        Set<Entry<String, String>> toRetain = 
            linkedHashMap("A", "a", "B", "B", "c", "c", "D", "d", "E", "e").entrySet();
        this.expectedViewType = EntrySet.class;
        this.expectedEntrySetModification = CollectionModifications.<Entry<String, String>>retainAll(toRetain);
        entrySet.retainAll(toRetain);
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_ENTRY_SET));
        Assert.assertEquals("-Di-Di-De-De", this.eventSequence);
        Assert.assertEquals("-Di-Di-De-De", this.entrySetEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "B", "b", "C", "c");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "B", "b", "C", "c");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateEntrySetEvents(PropertyVersion.DETACH, this.entrySetDetachingEvents, "B", "b", "C", "c");
        this.validateEntrySetEvents(PropertyVersion.DETACH, this.entrySetDetachedEvents, "B", "b", "C", "c");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "D", "d");
    }
    
    @Test
    public void testEntrySetIteratorRemove() {
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "C", "c", "D", "d"));
        MASet<Entry<String, String>> entrySet = this.map.entrySet();
        MAIterator<Entry<String, String>> iterator = entrySet.iterator();
        this.handleMapEvents();
        this.handleEntrySetEvents(entrySet);
        this.handleEntrySetIteratorEvents(iterator);
        
        if (this.map instanceof MASortedMap<?, ?> || this.map instanceof MAOrderedMap<?, ?>) {
            boolean remove = false;
            while (iterator.hasNext()) {
                iterator.next();
                if (remove) {
                    this.expectedViewType = EntrySet.class;
                    this.expectedEntrySetViewType = CollectionViewInfos.Iterator.class;
                    this.expectedEntrySetIteratorModification = 
                        IteratorModifications.<Entry<String, String>>remove();
                    iterator.remove();
                }
                remove = !remove;
            }
        } else {
            while (iterator.hasNext()) {
                String key = iterator.next().getKey();
                if ("B".equals(key) || "D".equals(key)) {
                    this.expectedViewType = EntrySet.class;
                    this.expectedEntrySetViewType = CollectionViewInfos.Iterator.class;
                    this.expectedEntrySetIteratorModification = 
                        IteratorModifications.<Entry<String, String>>remove();
                    iterator.remove();
                }
            }
        }
        
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_ENTRY_SET | EVENT_ENTRY_SET_ITERATOR));
        Assert.assertEquals("-Di-De-Di-De", this.eventSequence);
        Assert.assertEquals("-Di-De-Di-De", this.entrySetEventSequence);
        Assert.assertEquals("-Di-De-Di-De", this.entrySetIteratorEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "B", "b", "D", "d");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "B", "b", "D", "d");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateEntrySetEvents(PropertyVersion.DETACH, this.entrySetDetachingEvents, "B", "b", "D", "d");
        this.validateEntrySetEvents(PropertyVersion.DETACH, this.entrySetDetachedEvents, "B", "b", "D", "d");
        this.validateEntrySetEvents(PropertyVersion.DETACH, this.entrySetIteratorDetachingEvents, "B", "b", "D", "d");
        this.validateEntrySetEvents(PropertyVersion.DETACH, this.entrySetIteratorDetachedEvents, "B", "b", "D", "d");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "C", "c");
    }
    
    @SuppressWarnings("deprecation")
    @Test(expected = UnsupportedOperationException.class)
    public void testKeySetAdd() {
        this.handleMapEvents();
        try {
            this.map.keySet().add("A");
        } catch (RuntimeException ex) {
            validateEventsEmpty(EVENT_ALL);
            throw ex;
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test(expected = UnsupportedOperationException.class)
    public void testKeySetAddAll() {
        this.handleMapEvents();
        try {
            this.map.keySet().addAll(arrayList("A", "B", "C"));
        } catch (RuntimeException ex) {
            validateEventsEmpty(EVENT_ALL);
            throw ex;
        }
    }
    
    @Test
    public void testKeySetClear() {
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "C", "c"));
        MASet<String> keySet = this.map.keySet();
        this.handleMapEvents();
        this.handleKeySetEvents(keySet);
        
        this.expectedViewType = KeySet.class;
        this.expectedKeySetModification = CollectionModifications.<String>clear();
        keySet.clear();
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_KEY_SET));
        Assert.assertEquals("-Di-Di-Di-De-De-De", this.eventSequence);
        Assert.assertEquals("-Di-Di-Di-De-De-De", this.keySetEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "A", "a", "B", "b", "C", "c");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "A", "a", "B", "b", "C", "c");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateKeyEvents(PropertyVersion.DETACH, this.keySetDetachingEvents, "A", "B", "C");
        this.validateKeyEvents(PropertyVersion.DETACH, this.keySetDetachedEvents, "A", "B", "C");
        this.clearEvents(EVENT_ALL);
        this.validateMap();
    }
    
    @Test
    public void testKeySetRemove() {
                
        this.map.putAll(linkedHashMap("A", "a", "B", "b"));
        MASet<String> keySet = this.map.keySet();
        this.handleMapEvents();
        this.handleKeySetEvents(keySet);
        
        this.expectedViewType = KeySet.class;
        this.expectedKeySetModification = CollectionModifications.<String>remove("A");
        keySet.remove("A");
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_KEY_SET));
        Assert.assertEquals("-Di-De", this.eventSequence);
        Assert.assertEquals("-Di-De", this.keySetEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "A", "a");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "A", "a");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateKeyEvents(PropertyVersion.DETACH, this.keySetDetachingEvents, "A");
        this.validateKeyEvents(PropertyVersion.DETACH, this.keySetDetachedEvents, "A");
        this.clearEvents(EVENT_ALL);
        this.validateMap("B", "b");
        
        this.expectedViewType = null;
        this.expectedKeySetModification = null;
        keySet.remove("C");
        this.validateEventsEmpty(EVENT_ALL);
        this.validateMap("B", "b");
    }
    
    @Test
    public void testKeySetRemoveAll() {
        
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "C", "c", "D", "d"));
        MASet<String> keySet = this.map.keySet();
        this.handleMapEvents();
        this.handleKeySetEvents(keySet);
        
        Collection<String> toRemove = arrayList("A", "b", "c", "D", "e");
        this.expectedViewType = KeySet.class;
        this.expectedKeySetModification = CollectionModifications.<String>removeAll(toRemove);
        keySet.removeAll(toRemove);
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_KEY_SET));
        Assert.assertEquals("-Di-Di-De-De", this.eventSequence);
        Assert.assertEquals("-Di-Di-De-De", this.keySetEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "A", "a", "D", "d");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "A", "a", "D", "d");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateKeyEvents(PropertyVersion.DETACH, this.keySetDetachingEvents, "A", "D");
        this.validateKeyEvents(PropertyVersion.DETACH, this.keySetDetachedEvents, "A", "D");
        this.clearEvents(EVENT_ALL);
        this.validateMap("B", "b", "C", "c");
    }
    
    @Test
    public void testKeySetRetainAll() {
        
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "C", "c", "D", "d"));
        MASet<String> keySet = this.map.keySet();
        this.handleMapEvents();
        this.handleKeySetEvents(keySet);
        
        List<String> toRetain = arrayList("A", "b", "c", "D", "e");
        this.expectedViewType = KeySet.class;
        this.expectedKeySetModification = CollectionModifications.<String>retainAll(toRetain);
        keySet.retainAll(toRetain);
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_KEY_SET));
        Assert.assertEquals("-Di-Di-De-De", this.eventSequence);
        Assert.assertEquals("-Di-Di-De-De", this.keySetEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "B", "b", "C", "c");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "B", "b", "C", "c");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateKeyEvents(PropertyVersion.DETACH, this.keySetDetachingEvents, "B", "C");
        this.validateKeyEvents(PropertyVersion.DETACH, this.keySetDetachedEvents, "B", "C");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "D", "d");
    }
    
    @Test
    public void testKeySetIteratorRemove() {
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "C", "c", "D", "d"));
        MASet<String> keySet = this.map.keySet();
        MAIterator<String> iterator = keySet.iterator();
        this.handleMapEvents();
        this.handleKeySetEvents(keySet);
        this.handleKeySetIteratorEvents(iterator);
        
        if (this.map instanceof MASortedMap<?, ?> || this.map instanceof MAOrderedMap<?, ?>) {
            boolean remove = false;
            while (iterator.hasNext()) {
                iterator.next();
                if (remove) {
                    this.expectedViewType = KeySet.class;
                    this.expectedKeySetViewType = CollectionViewInfos.Iterator.class;
                    this.expectedKeySetIteratorModification = IteratorModifications.remove();
                    iterator.remove();
                }
                remove = !remove;
            }
        } else {
            while (iterator.hasNext()) {
                String key = iterator.next();
                if ("B".equals(key) || "D".equals(key)) {
                    this.expectedViewType = KeySet.class;
                    this.expectedKeySetViewType = CollectionViewInfos.Iterator.class;
                    this.expectedKeySetIteratorModification = IteratorModifications.remove();
                    iterator.remove();
                }
            }
        }
        
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_KEY_SET | EVENT_KEY_SET_ITERATOR));
        Assert.assertEquals("-Di-De-Di-De", this.eventSequence);
        Assert.assertEquals("-Di-De-Di-De", this.keySetEventSequence);
        Assert.assertEquals("-Di-De-Di-De", this.keySetIteratorEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "B", "b", "D", "d");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "B", "b", "D", "d");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateKeyEvents(PropertyVersion.DETACH, this.keySetDetachingEvents, "B", "D");
        this.validateKeyEvents(PropertyVersion.DETACH, this.keySetDetachedEvents,  "B", "D");
        this.validateKeyEvents(PropertyVersion.DETACH, this.keySetIteratorDetachingEvents,  "B", "D");
        this.validateKeyEvents(PropertyVersion.DETACH, this.keySetIteratorDetachedEvents,  "B", "D");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "C", "c");
    }
    
    @SuppressWarnings("deprecation")
    @Test(expected = UnsupportedOperationException.class)
    public void testValuesAdd() {
        this.handleMapEvents();
        try {
            this.map.values().add("A");
        } catch (RuntimeException ex) {
            validateEventsEmpty(EVENT_ALL);
            throw ex;
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test(expected = UnsupportedOperationException.class)
    public void testValuesAddAll() {
        this.handleMapEvents();
        try {
            this.map.values().addAll(arrayList("A", "B", "C"));
        } catch (RuntimeException ex) {
            validateEventsEmpty(EVENT_ALL);
            throw ex;
        }
    }
    
    @Test
    public void testValuesClear() {
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "C", "c"));
        MACollection<String> values = this.map.values();
        this.handleMapEvents();
        this.handleValuesEvents(values);
        
        this.expectedViewType = Values.class;
        this.expectedValuesModification = CollectionModifications.<String>clear();
        values.clear();
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_VALUES));
        Assert.assertEquals("-Di-Di-Di-De-De-De", this.eventSequence);
        Assert.assertEquals("-Di-Di-Di-De-De-De", this.valuesEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "A", "a", "B", "b", "C", "c");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "A", "a", "B", "b", "C", "c");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachingEvents, "a", "b", "c");
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachedEvents, "a", "b", "c");
        this.clearEvents(EVENT_ALL);
        this.validateMap();
    }
    
    @Test
    public void testValuesRemove() {
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "B+", "b", "C", "c"));
        MACollection<String> values = this.map.values();
        this.handleMapEvents();
        this.handleValuesEvents(values);
        
        this.expectedViewType = Values.class;
        this.expectedValuesModification = CollectionModifications.<String>remove("b");
        values.remove("b");
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_VALUES));
        Assert.assertEquals("-Di-De", this.eventSequence);
        Assert.assertEquals("-Di-De", this.valuesEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "B", "b");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "B", "b");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachingEvents, "b");
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachedEvents, "b");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "B+", "b", "C", "c");
        
        this.expectedViewType = Values.class;
        this.expectedValuesModification = CollectionModifications.<String>remove("b");
        values.remove("b");
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_VALUES));
        Assert.assertEquals("-Di-De", this.eventSequence);
        Assert.assertEquals("-Di-De", this.valuesEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "B+", "b");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "B+", "b");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachingEvents, "b");
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachedEvents, "b");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "C", "c");
        
        this.expectedViewType = Values.class;
        this.expectedValuesModification = CollectionModifications.<String>remove("a");
        values.remove("a");
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_VALUES));
        Assert.assertEquals("-Di-De", this.eventSequence);
        Assert.assertEquals("-Di-De", this.valuesEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "A", "a");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "A", "a");
        this.validateMapEvents(PropertyVersion.DETACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.DETACH, this.attachedEvents);
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachingEvents, "a");
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachedEvents, "a");
        this.clearEvents(EVENT_ALL);
        this.validateMap("C", "c");
        
        this.expectedViewType = Values.class;
        this.expectedValuesModification = CollectionModifications.<String>remove("c");
        values.remove("c");
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_VALUES));
        Assert.assertEquals("-Di-De", this.eventSequence);
        Assert.assertEquals("-Di-De", this.valuesEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "C", "c");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "C", "c");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachingEvents, "c");
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachedEvents, "c");
        this.clearEvents(EVENT_ALL);
        this.validateMap();
        
        this.expectedViewType = null;
        this.expectedValuesModification = null;
        values.remove("d");
        this.validateEventsEmpty(EVENT_ALL);
        this.validateMap();
    }
    
    @Test
    public void testValeusRemoveAll() {
        this.map.putAll(linkedHashMap(
                "A-", "a", "A", "a", "A+", "a", 
                "B-", "b", "B", "b", "B+", "b",
                "C-", "c", "C", "c", "C+", "c",
                "D-", "d", "D", "d", "D+", "d"));
        MACollection<String> values = this.map.values();
        this.handleMapEvents();
        this.handleValuesEvents(values);
        
        List<String> toRemove = arrayList("b", "b", "d", "f", "f");
        this.expectedViewType = Values.class;
        this.expectedValuesModification = CollectionModifications.<String>removeAll(toRemove);
        values.removeAll(toRemove);
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_VALUES));
        Assert.assertEquals("-Di-Di-Di-Di-Di-Di-De-De-De-De-De-De", this.eventSequence);
        Assert.assertEquals("-Di-Di-Di-Di-Di-Di-De-De-De-De-De-De", this.valuesEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "B-", "b", "B", "b", "B+", "b", "D-", "d", "D", "d", "D+", "d");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "B-", "b", "B", "b", "B+", "b", "D-", "d", "D", "d", "D+", "d");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachingEvents, "b", "b", "b", "d", "d", "d");
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachedEvents, "b", "b", "b", "d", "d", "d");
        this.validateMap("A-", "a", "A", "a", "A+", "a", "C-", "c", "C", "c", "C+", "c");
    }
    
    @Test
    public void testValeusRetainAll() {
        this.map.putAll(linkedHashMap(
                "A-", "a", "A", "a", "A+", "a", 
                "B-", "b", "B", "b", "B+", "b",
                "C-", "c", "C", "c", "C+", "c",
                "D-", "d", "D", "d", "D+", "d"));
        MACollection<String> values = this.map.values();
        this.handleMapEvents();
        this.handleValuesEvents(values);
        
        List<String> toRetain = arrayList("b", "b", "d", "f", "f");
        this.expectedViewType = Values.class;
        this.expectedValuesModification = CollectionModifications.<String>retainAll(toRetain);
        values.retainAll(toRetain);
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_VALUES));
        Assert.assertEquals("-Di-Di-Di-Di-Di-Di-De-De-De-De-De-De", this.eventSequence);
        Assert.assertEquals("-Di-Di-Di-Di-Di-Di-De-De-De-De-De-De", this.valuesEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "A-", "a", "A", "a", "A+", "a", "C-", "c", "C", "c", "C+", "c");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "A-", "a", "A", "a", "A+", "a", "C-", "c", "C", "c", "C+", "c");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachingEvents, "a", "a", "a", "c", "c", "c");
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachedEvents, "a", "a", "a", "c", "c", "c");
        this.validateMap("B-", "b", "B", "b", "B+", "b", "D-", "d", "D", "d", "D+", "d");
    }
    
    @Test
    public void testValuesIteratorRemove() {
        this.map.putAll(linkedHashMap("A", "a", "B", "b", "C", "c", "D", "d"));
        MACollection<String> values = this.map.values();
        MAIterator<String> iterator = values.iterator();
        this.handleMapEvents();
        this.handleValuesEvents(values);
        this.handleValuesIteratorEvents(iterator);
        
        if (this.map instanceof MASortedMap<?, ?> || this.map instanceof MAOrderedMap<?, ?>) {
            boolean remove = false;
            while (iterator.hasNext()) {
                iterator.next();
                if (remove) {
                    this.expectedViewType = Values.class;
                    this.expectedValuesViewType = CollectionViewInfos.Iterator.class;
                    this.expectedValuesIteratorModification = IteratorModifications.remove();
                    iterator.remove();
                }
                remove = !remove;
            }
        } else {
            while (iterator.hasNext()) {
                String value = iterator.next();
                if ("b".equals(value) || "d".equals(value)) {
                    this.expectedViewType = Values.class;
                    this.expectedValuesViewType = CollectionViewInfos.Iterator.class;
                    this.expectedValuesIteratorModification = IteratorModifications.remove();
                    iterator.remove();
                }
            }
        }
        
        this.validateEventsEmpty(EVENT_ALL & ~(EVENT_MAP | EVENT_VALUES | EVENT_VALUES_ITERATOR));
        Assert.assertEquals("-Di-De-Di-De", this.eventSequence);
        Assert.assertEquals("-Di-De-Di-De", this.valuesEventSequence);
        Assert.assertEquals("-Di-De-Di-De", this.valuesIteratorEventSequence);
        this.validateMapEvents(PropertyVersion.DETACH, this.detachingEvents, "B", "b", "D", "d");
        this.validateMapEvents(PropertyVersion.DETACH, this.detachedEvents, "B", "b", "D", "d");
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachingEvents);
        this.validateMapEvents(PropertyVersion.ATTACH, this.attachedEvents);
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachingEvents, "b", "d");
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesDetachedEvents,  "b", "d");
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesIteratorDetachingEvents,  "b", "d");
        this.validateValueEvents(PropertyVersion.DETACH, this.valuesIteratorDetachedEvents,  "b", "d");
        this.clearEvents(EVENT_ALL);
        this.validateMap("A", "a", "C", "c");
    }
    
    private void validateEventsEmpty(int validateEmptyEventsFlags) {
        if ((EVENT_MAP & validateEmptyEventsFlags) != 0) {
            Assert.assertEquals(0, this.eventSequence.length());
            Assert.assertTrue(this.attachingEvents.isEmpty());
            Assert.assertTrue(this.attachedEvents.isEmpty());
            Assert.assertTrue(this.detachingEvents.isEmpty());
            Assert.assertTrue(this.detachedEvents.isEmpty());
        }
        if ((EVENT_ENTRY_SET & validateEmptyEventsFlags) != 0) {
            Assert.assertEquals(0, this.entrySetEventSequence.length());
            Assert.assertTrue(this.entrySetDetachingEvents.isEmpty());
            Assert.assertTrue(this.entrySetDetachedEvents.isEmpty());
        }
        if ((EVENT_KEY_SET & validateEmptyEventsFlags) != 0) {
            Assert.assertEquals(0, this.keySetEventSequence.length());
            Assert.assertTrue(this.keySetDetachingEvents.isEmpty());
            Assert.assertTrue(this.keySetDetachedEvents.isEmpty());
        }
        if ((EVENT_VALUES & validateEmptyEventsFlags) != 0) {
            Assert.assertEquals(0, this.valuesEventSequence.length());
            Assert.assertTrue(this.valuesDetachingEvents.isEmpty());
            Assert.assertTrue(this.valuesDetachedEvents.isEmpty());
        }
        if ((EVENT_ENTRY_SET_ITERATOR & validateEmptyEventsFlags) != 0) {
            Assert.assertEquals(0, this.entrySetIteratorEventSequence.length());
            Assert.assertTrue(this.entrySetIteratorDetachingEvents.isEmpty());
            Assert.assertTrue(this.entrySetIteratorDetachedEvents.isEmpty());
        }
        if ((EVENT_KEY_SET_ITERATOR & validateEmptyEventsFlags) != 0) {
            Assert.assertEquals(0, this.keySetIteratorEventSequence.length());
            Assert.assertTrue(this.keySetIteratorDetachingEvents.isEmpty());
            Assert.assertTrue(this.keySetIteratorDetachedEvents.isEmpty());
        }
        if ((EVENT_VALUES_ITERATOR & validateEmptyEventsFlags) != 0) {
            Assert.assertEquals(0, this.valuesIteratorEventSequence.length());
            Assert.assertTrue(this.valuesIteratorDetachingEvents.isEmpty());
            Assert.assertTrue(this.valuesIteratorDetachedEvents.isEmpty());
        }
    }
    
    private void clearEvents(int clearEventFlags) {
        if ((EVENT_MAP & clearEventFlags) != 0) {
            this.expectedModification = null;
            this.expectedViewType = null;
            this.eventSequence = "";
            this.attachingEvents.clear();
            this.attachedEvents.clear();
            this.detachingEvents.clear();
            this.detachedEvents.clear();
        }
        if ((EVENT_ENTRY_SET & clearEventFlags) != 0) {
            this.expectedEntrySetModification = null;
            this.expectedEntrySetViewType = null;
            this.entrySetEventSequence = "";
            this.entrySetDetachingEvents.clear();
            this.entrySetDetachedEvents.clear();
        }
        if ((EVENT_KEY_SET & clearEventFlags) != 0) {
            this.expectedKeySetModification = null;
            this.expectedKeySetViewType = null;
            this.keySetEventSequence = "";
            this.keySetDetachingEvents.clear();
            this.keySetDetachedEvents.clear();
        }
        if ((EVENT_VALUES & clearEventFlags) != 0) {
            this.expectedValuesModification = null;
            this.expectedValuesViewType = null;
            this.valuesEventSequence = "";
            this.valuesDetachingEvents.clear();
            this.valuesDetachedEvents.clear();
        }
        if ((EVENT_ENTRY_SET_ITERATOR & clearEventFlags) != 0) {
            this.expectedEntrySetIteratorModification = null;
            this.entrySetIteratorDetachingEvents.clear();
            this.entrySetIteratorDetachedEvents.clear();
        }
        if ((EVENT_KEY_SET_ITERATOR & clearEventFlags) != 0) {
            this.expectedKeySetIteratorModification = null;
            this.keySetIteratorDetachingEvents.clear();
            this.keySetIteratorDetachedEvents.clear();
        }
        if ((EVENT_VALUES_ITERATOR & clearEventFlags) != 0) {
            this.expectedValuesIteratorModification = null;
            this.valuesIteratorDetachingEvents.clear();
            this.valuesIteratorDetachedEvents.clear();
        }
    }
    
    private void validateMap(String ... keyValues) {
        Assert.assertEquals(0, keyValues.length % 2);
        int expectedEventCount = keyValues.length / 2;
        Assert.assertEquals(expectedEventCount, this.map.size());
        Iterator<Entry<String, String>> iterator = this.map.entrySet().iterator();
        Map<String, String> paramMap = 
            this.map instanceof SortedMap<?, ?> ?
                new TreeMap<String, String>(((MASortedMap<String, String>)this.map).comparator()) :
                new LinkedHashMap<String, String>();
        for (int i = 0; i < keyValues.length; i += 2) {
            paramMap.put(keyValues[i], keyValues[i + 1]);
        }
        if (this.map instanceof MASortedMap<?, ?> || this.map instanceof MAOrderedMap<?, ?>) {
            for (Entry<String, String> kvPair : paramMap.entrySet()) {
                Entry<String, String> entry = iterator.next();
                Assert.assertEquals(kvPair.getKey(), entry.getKey());
                Assert.assertEquals(kvPair.getValue(), entry.getValue());
            }
        } else {
            for (Entry<String, String> entry : this.map.entrySet()) {
                Assert.assertTrue(paramMap.entrySet().contains(entry));
            }
        }
    }
    
    private <K, V> void validateMapEvents(
            PropertyVersion version,
            Collection<MapElementEvent<K, V>> events, 
            String ... keyValues) {
        Assert.assertEquals(0, keyValues.length % 2);
        int expectedEventCount = keyValues.length / 2;
        Assert.assertEquals(expectedEventCount, events.size());
        Iterator<MapElementEvent<K, V>> iterator = 
            this.map instanceof MASortedMap<?, ?> || this.map instanceof MAOrderedMap<?, ?> ?
                    events.iterator() : null;
        Map<String, String> paramMap = 
            this.map instanceof SortedMap<?, ?> ?
                new TreeMap<String, String>(((MASortedMap<String, String>)this.map).comparator()) :
                new LinkedHashMap<String, String>();
        for (int i = 0; i < keyValues.length; i += 2) {
            paramMap.put(keyValues[i], keyValues[i + 1]);
        }
        for (Entry<String, String> kvPair : paramMap.entrySet()) {
            MapElementEvent<K, V> event = null;
            if (iterator != null) {
                event = iterator.next();
            } else {
                for (MapElementEvent<K, V> evt : events) {
                    if (evt.getKey(version) == null ? kvPair.getKey() == 
                        null : evt.getKey(version).equals(kvPair.getKey())) {
                        event = evt;
                        break;
                    }
                }
            }
            Assert.assertNotNull(event);
            Assert.assertEquals(kvPair.getKey(), event.getKey(version));
            String value = kvPair.getValue();
            if (value.contains("=>")) {
                String[] strs = value.split("=>");
                String detach = strs[0];
                Assert.assertEquals(2, strs.length);
                if ("null".equals(detach)) {
                    detach = null;
                }
                String attach = strs[1];
                if ("null".equals(attach)) {
                    attach = null;
                }
                boolean detachMode = detach != null && detach.startsWith("*");
                boolean attachMode = attach != null && attach.startsWith("*");
                Assert.assertFalse(attachMode == detachMode);
                if (detachMode) {
                    detach = detach.substring(1);
                }
                if (attachMode) {
                    attach = attach.substring(1);
                }
                Assert.assertEquals(detach, event.getValue(PropertyVersion.DETACH));
                Assert.assertEquals(attach, event.getValue(PropertyVersion.ATTACH));
            } else {
                if (event.getModificationType().contains(PropertyVersion.DETACH)) {
                    Assert.assertEquals(value, event.getValue(PropertyVersion.DETACH));
                }
                if (event.getModificationType().contains(PropertyVersion.ATTACH)) {
                    Assert.assertEquals(value, event.getValue(PropertyVersion.ATTACH));
                }
            }
        }
    }
    
    private <K, V> void validateEntrySetEvents(
            PropertyVersion version, 
            Collection<ElementEvent<Entry<String, String>>> events, 
            String ... keyValues) {
        Assert.assertEquals(0, keyValues.length % 2);
        int expectedEventCount = keyValues.length / 2;
        Assert.assertEquals(expectedEventCount, events.size());
        Iterator<ElementEvent<Entry<String, String>>> iterator = 
            this.map instanceof MASortedMap<?, ?> || this.map instanceof MAOrderedMap<?, ?> ?
                    events.iterator() : null;
        Map<String, String> paramMap = 
            this.map instanceof SortedMap<?, ?> ?
                new TreeMap<String, String>(((MASortedMap<String, String>)this.map).comparator()) :
                new LinkedHashMap<String, String>();
        for (int i = 0; i < keyValues.length; i += 2) {
            paramMap.put(keyValues[i], keyValues[i + 1]);
        }
        for (Entry<String, String> kvPair : paramMap.entrySet()) {
            ElementEvent<Entry<String, String>> event = null;
            if (iterator != null) {
                event = iterator.next();
            } else {
                for (ElementEvent<Entry<String, String>> evt : events) {
                    if (evt.getElement(version).getKey() == null ? 
                            kvPair.getKey() == null : 
                            evt.getElement(version).getKey().equals(kvPair.getKey())) {
                        event = evt;
                        break;
                    }
                }
            }
            Assert.assertNotNull(event);
            Assert.assertEquals(kvPair.getKey(), event.getElement(version).getKey());
            Assert.assertEquals(kvPair.getValue(), event.getElement(version).getValue());
        }
    }
    
    @SuppressWarnings("unchecked")
    private <K> void validateKeyEvents(
            PropertyVersion version, 
            Collection<ElementEvent<K>> events, K ... keys) {
        Assert.assertEquals(keys.length, events.size());
        Iterator<ElementEvent<K>> iterator = 
            this.map instanceof MASortedMap<?, ?> || this.map instanceof MAOrderedMap<?, ?> ?
                    events.iterator() : null;
        Set<K> paramSet = this.map instanceof MASortedMap<?, ?> ?
                new TreeSet<K>() : new LinkedHashSet<K>();
        for (K key : keys) {
            paramSet.add(key);
        }
        for (K key : paramSet) {
            if (iterator != null) {
                K key2 = iterator.next().getElement(version);
                Assert.assertEquals(key, key2);
            } else {
                boolean find = false;
                for (ElementEvent<K> evt : events) {
                    if (key == null ? evt.getElement(version) == null : key.equals(evt.getElement(version))) {
                        find = true;
                        break;
                    }
                }
                Assert.assertTrue(find);
            }
            
        }
    }
    
    @SuppressWarnings("unchecked")
    private <V> void validateValueEvents(
            PropertyVersion version,
            Collection<ElementEvent<V>> events, 
            V ... values) {
        Assert.assertEquals(values.length, events.size());
        Iterator<ElementEvent<V>> iterator = 
            this.map instanceof MASortedMap<?, ?> || this.map instanceof MAOrderedMap<?, ?> ?
                    events.iterator() : null;
        for (V value : values) {
            if (iterator != null) {
                V value2 = iterator.next().getElement(version);
                Assert.assertEquals(value, value2);
            } else {
                boolean find = false;
                for (ElementEvent<V> evt : events) {
                    if (value == null ? evt.getElement(version) == null : value.equals(evt.getElement(version))) {
                        find = true;
                        break;
                    }
                }
                Assert.assertTrue(find);
            }
            
        }
    }
    
    private static void validateCause(Cause cause, Class<?> causeType) {
        if (causeType != null) {
            Assert.assertTrue(causeType.isAssignableFrom(cause.getViewInfo().getClass()));
        } else {
            Assert.assertNull(cause);
        }
    }
    
    private static Map<String, String> linkedHashMap(String ... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (int i = 0; i < keyValues.length; i++) {
            map.put(keyValues[i], keyValues[++i]);
        }
        return map;
    }
    
    private static List<String> arrayList(String ... elements) {
        List<String> list = new ArrayList<String>();
        for (String element : elements) {
            list.add(element);
        }
        return list;
    }
    
    private void handleMapEvents() {
        this.map.addMapElementListener(new MapElementListener<String, String>() {
            
            @Override
            public void modifying(MapElementEvent<String, String> e) {
                Assert.assertSame(MAMapTest.this.map, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedModification, e.getModification());
                validateCause(e.getCause(), MAMapTest.this.expectedViewType);
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    MAMapTest.this.eventSequence += "-Di";
                    MAMapTest.this.detachingEvents.add(e);
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    MAMapTest.this.eventSequence += "-Ai";
                    MAMapTest.this.attachingEvents.add(e);
                }
            }
            
            @Override
            public void modified(MapElementEvent<String, String> e) {
                Assert.assertSame(MAMapTest.this.map, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedModification, e.getModification());
                validateCause(e.getCause(), MAMapTest.this.expectedViewType);
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    MAMapTest.this.eventSequence += "-De";
                    MAMapTest.this.detachedEvents.add(e);
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    MAMapTest.this.eventSequence += "-Ae";
                    MAMapTest.this.attachedEvents.add(e);
                }
            }
        });
    }
    
    private void handleEntrySetEvents(final MASet<Entry<String, String>> entrySet) {
        entrySet.addElementListener(new ElementListener<Entry<String, String>>() {
            @Override
            public void modifying(ElementEvent<Entry<String, String>> e) {
                Assert.assertSame(entrySet, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedEntrySetModification, e.getModification());
                validateCause(e.getCause(), MAMapTest.this.expectedEntrySetViewType);
                Assert.assertEquals(ModificationType.DETACH, e.getModificationType()); 
                MAMapTest.this.entrySetEventSequence += "-Di";
                MAMapTest.this.entrySetDetachingEvents.add(e);
            }
            @Override
            public void modified(ElementEvent<Entry<String, String>> e) {
                Assert.assertSame(entrySet, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedEntrySetModification, e.getModification());
                validateCause(e.getCause(), MAMapTest.this.expectedEntrySetViewType);
                Assert.assertEquals(ModificationType.DETACH, e.getModificationType()); 
                MAMapTest.this.entrySetEventSequence += "-De";
                MAMapTest.this.entrySetDetachedEvents.add(e);
            }
        });
    }
    
    private void handleEntrySetIteratorEvents(final MAIterator<Entry<String, String>> iterator) {
        iterator.addElementListener(new ElementListener<Entry<String, String>>() {
            @Override
            public void modifying(ElementEvent<Entry<String, String>> e) {
                Assert.assertSame(iterator, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedEntrySetIteratorModification, e.getModification());
                Assert.assertEquals(ModificationType.DETACH, e.getModificationType()); 
                MAMapTest.this.entrySetIteratorEventSequence += "-Di";
                MAMapTest.this.entrySetIteratorDetachingEvents.add(e);
            }
            @Override
            public void modified(ElementEvent<Entry<String, String>> e) {
                Assert.assertSame(iterator, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedEntrySetIteratorModification, e.getModification());
                Assert.assertEquals(ModificationType.DETACH, e.getModificationType()); 
                MAMapTest.this.entrySetIteratorEventSequence += "-De";
                MAMapTest.this.entrySetIteratorDetachedEvents.add(e);
            }
        });
    }
    
    private void handleKeySetEvents(final MASet<String> keySet) {
        keySet.addElementListener(new ElementListener<String>() {
            @Override
            public void modifying(ElementEvent<String> e) {
                Assert.assertSame(keySet, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedKeySetModification, e.getModification());
                validateCause(e.getCause(), MAMapTest.this.expectedKeySetViewType);
                Assert.assertEquals(ModificationType.DETACH, e.getModificationType()); 
                MAMapTest.this.keySetEventSequence += "-Di";
                MAMapTest.this.keySetDetachingEvents.add(e);
            }
            @Override
            public void modified(ElementEvent<String> e) {
                Assert.assertSame(keySet, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedKeySetModification, e.getModification());
                validateCause(e.getCause(), MAMapTest.this.expectedKeySetViewType);
                Assert.assertEquals(ModificationType.DETACH, e.getModificationType()); 
                MAMapTest.this.keySetEventSequence += "-De";
                MAMapTest.this.keySetDetachedEvents.add(e);
            }
        });
    }
    
    private void handleKeySetIteratorEvents(final MAIterator<String> iterator) {
        iterator.addElementListener(new ElementListener<String>() {
            @Override
            public void modifying(ElementEvent<String> e) {
                Assert.assertSame(iterator, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedKeySetIteratorModification, e.getModification());
                Assert.assertEquals(ModificationType.DETACH, e.getModificationType()); 
                MAMapTest.this.keySetIteratorEventSequence += "-Di";
                MAMapTest.this.keySetIteratorDetachingEvents.add(e);
            }
            @Override
            public void modified(ElementEvent<String> e) {
                Assert.assertSame(iterator, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedKeySetIteratorModification, e.getModification());
                Assert.assertEquals(ModificationType.DETACH, e.getModificationType()); 
                MAMapTest.this.keySetIteratorEventSequence += "-De";
                MAMapTest.this.keySetIteratorDetachedEvents.add(e);
            }
        });
    }
    
    private void handleValuesEvents(final MACollection<String> values) {
        values.addElementListener(new ElementListener<String>() {
            @Override
            public void modifying(ElementEvent<String> e) {
                Assert.assertSame(values, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedValuesModification, e.getModification());
                validateCause(e.getCause(), MAMapTest.this.expectedValuesViewType);
                Assert.assertEquals(ModificationType.DETACH, e.getModificationType()); 
                MAMapTest.this.valuesEventSequence += "-Di";
                MAMapTest.this.valuesDetachingEvents.add(e);
            }
            @Override
            public void modified(ElementEvent<String> e) {
                Assert.assertSame(values, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedValuesModification, e.getModification());
                validateCause(e.getCause(), MAMapTest.this.expectedValuesViewType);
                Assert.assertEquals(ModificationType.DETACH, e.getModificationType()); 
                MAMapTest.this.valuesEventSequence += "-De";
                MAMapTest.this.valuesDetachedEvents.add(e);
            }
        });
    }
    
    private void handleValuesIteratorEvents(final MAIterator<String> iterator) {
        iterator.addElementListener(new ElementListener<String>() {
            @Override
            public void modifying(ElementEvent<String> e) {
                Assert.assertSame(iterator, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedValuesIteratorModification, e.getModification());
                Assert.assertEquals(ModificationType.DETACH, e.getModificationType()); 
                MAMapTest.this.valuesIteratorEventSequence += "-Di";
                MAMapTest.this.valuesIteratorDetachingEvents.add((ElementEvent<String>)e);
            }
            @Override
            public void modified(ElementEvent<String> e) {
                Assert.assertSame(iterator, e.getSource());
                Assert.assertEquals(e.getModification() != null, e.getCause() == null);
                assertModificationEquals(MAMapTest.this.expectedValuesIteratorModification, e.getModification());
                Assert.assertEquals(ModificationType.DETACH, e.getModificationType()); 
                MAMapTest.this.valuesIteratorEventSequence += "-De";
                MAMapTest.this.valuesIteratorDetachedEvents.add((ElementEvent<String>)e);
            }
        });
    }
    
    private static Entry<String, String> entry(final String key, final String value) {
        return new Entry<String, String>() {
            String _key = key;
            String _value = value;
            @Override
            public String getKey() {
                return this._key;
            }
            @Override
            public String getValue() {
                return this._value;
            }
            @Override
            public String setValue(String value) {
                return this._value;
            }
            @SuppressWarnings("unchecked")
            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj instanceof Entry == false) {
                    return false;
                }
                Entry<String, String> other = (Entry<String, String>)obj;
                String key = this._key;
                String value = this._value;
                String oKey = other.getKey();
                String oValue = other.getValue();
                if (!(key == null ? oKey == null : key.equals(oKey))) {
                    return false;
                }
                return value == null ? oValue == null : value.equals(oValue);
            }
            @Override
            public int hashCode() {
                int hash = 0;
                String key = _key;
                String value = this._value;
                if (key != null) {
                    hash += 31 * key.hashCode(); 
                }
                if (value != null) {
                    hash += value.hashCode();
                }
                return hash;
            }
            
        };
        
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
