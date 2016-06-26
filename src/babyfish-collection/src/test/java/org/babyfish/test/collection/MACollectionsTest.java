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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.LinkedList;
import org.babyfish.collection.LockMode;
import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MACollection;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAHashMap;
import org.babyfish.collection.MAHashSet;
import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MALinkedList;
import org.babyfish.collection.MAList;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.MASet;
import org.babyfish.collection.MASortedMap;
import org.babyfish.collection.MASortedSet;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XList;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XNavigableMap;
import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.collection.XSet;
import org.babyfish.collection.XSortedMap;
import org.babyfish.collection.XSortedSet;
import org.babyfish.lang.Ref;
import org.babyfish.lang.UncheckedException;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class MACollectionsTest {

    private static final Class<?> PROXY_CLASS;
    
    private static final Class<?> GATEWAY_PROXY_CLASS;
    
    private static final Class<?> LOCKED_PROXY_CLASS;
    
    private static final Field USING_INTERNAL_LOCK_FIELD;
    
    private static final Method GET_INNER_OBJECT_METHOD;

    private static final Class<?>[] ALL_CLASSES;
    
    @SuppressWarnings("unchecked")
    @Test
    public void testUnmodifiableEntry() {
        
        Map<String, String> modifiableMap;
        Map.Entry<String, String> e;
        
        modifiableMap = new java.util.HashMap<String, String>();
        modifiableMap.put("A", "a");
        e = MACollections.unmodifiable(modifiableMap).entrySet().iterator().next();
        assertUnmodifiableType(e, Map.Entry.class);
        e = (Entry<String, String>)MACollections.unmodifiable(modifiableMap).entrySet().toArray()[0];
        assertUnmodifiableType(e, Map.Entry.class);
        e = MACollections.unmodifiable(modifiableMap).entrySet().toArray(new Entry[2])[0];
        assertUnmodifiableType(e, Map.Entry.class);
        
        modifiableMap = new java.util.LinkedHashMap<String, String>();
        modifiableMap.put("A", "a");
        e = MACollections.unmodifiable(modifiableMap).entrySet().iterator().next();
        assertUnmodifiableType(e, Map.Entry.class);
        e = (Entry<String, String>)MACollections.unmodifiable(modifiableMap).entrySet().toArray()[0];
        assertUnmodifiableType(e, Map.Entry.class);
        e = MACollections.unmodifiable(modifiableMap).entrySet().toArray(new Entry[2])[0];
        assertUnmodifiableType(e, Map.Entry.class);
        
        modifiableMap = new java.util.TreeMap<String, String>().descendingMap();
        modifiableMap.put("A", "a");
        e = MACollections.unmodifiable(modifiableMap).entrySet().iterator().next();
        assertUnmodifiableType(e, Map.Entry.class);
        e = (Entry<String, String>)MACollections.unmodifiable(modifiableMap).entrySet().toArray()[0];
        assertUnmodifiableType(e, Map.Entry.class);
        e = MACollections.unmodifiable(modifiableMap).entrySet().toArray(new Entry[2])[0];
        assertUnmodifiableType(e, Map.Entry.class);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testUnmodifiableXEntry() {
        
        Map<String, String> modifiableMap;
        Map.Entry<String, String> e;
        
        modifiableMap = new HashMap<String, String>();
        modifiableMap.put("A", "a");
        e = MACollections.unmodifiable(modifiableMap).entrySet().iterator().next();
        assertUnmodifiableType(e, XMap.XEntry.class);
        e = (Entry<String, String>)MACollections.unmodifiable(modifiableMap).entrySet().toArray()[0];
        assertUnmodifiableType(e, XMap.XEntry.class);
        e = MACollections.unmodifiable(modifiableMap).entrySet().toArray(new Entry[2])[0];
        assertUnmodifiableType(e, XMap.XEntry.class);
        
        modifiableMap = new LinkedHashMap<String, String>();
        modifiableMap.put("A", "a");
        e = MACollections.unmodifiable(modifiableMap).entrySet().iterator().next();
        assertUnmodifiableType(e, XMap.XEntry.class);
        e = (Entry<String, String>)MACollections.unmodifiable(modifiableMap).entrySet().toArray()[0];
        assertUnmodifiableType(e, XMap.XEntry.class);
        e = MACollections.unmodifiable(modifiableMap).entrySet().toArray(new Entry[2])[0];
        assertUnmodifiableType(e, XMap.XEntry.class);
        
        modifiableMap = new TreeMap<String, String>().descendingMap();
        modifiableMap.put("A", "a");
        e = MACollections.unmodifiable(modifiableMap).entrySet().iterator().next();
        assertUnmodifiableType(e, XMap.XEntry.class);
        e = (Entry<String, String>)MACollections.unmodifiable(modifiableMap).entrySet().toArray()[0];
        assertUnmodifiableType(e, XMap.XEntry.class);
        e = MACollections.unmodifiable(modifiableMap).entrySet().toArray(new Entry[2])[0];
        assertUnmodifiableType(e, XMap.XEntry.class);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testMAUnmodifiableMAEntry() {
        
        Map<String, String> modifiableMap;
        Map.Entry<String, String> e;
        
        modifiableMap = new MAHashMap<String, String>();
        modifiableMap.put("A", "a");
        e = MACollections.unmodifiable(modifiableMap).entrySet().iterator().next();
        assertUnmodifiableType(e, MAMap.MAEntry.class);
        e = (Entry<String, String>)MACollections.unmodifiable(modifiableMap).entrySet().toArray()[0];
        assertUnmodifiableType(e, MAMap.MAEntry.class);
        e = MACollections.unmodifiable(modifiableMap).entrySet().toArray(new Entry[2])[0];
        assertUnmodifiableType(e, MAMap.MAEntry.class);
        
        modifiableMap = new MALinkedHashMap<String, String>();
        modifiableMap.put("A", "a");
        e = MACollections.unmodifiable(modifiableMap).entrySet().iterator().next();
        assertUnmodifiableType(e, MAMap.MAEntry.class);
        e = (Entry<String, String>)MACollections.unmodifiable(modifiableMap).entrySet().toArray()[0];
        assertUnmodifiableType(e, MAMap.MAEntry.class);
        e = MACollections.unmodifiable(modifiableMap).entrySet().toArray(new Entry[2])[0];
        assertUnmodifiableType(e, MAMap.MAEntry.class);
        
        modifiableMap = new MATreeMap<String, String>().descendingMap();
        modifiableMap.put("A", "a");
        e = MACollections.unmodifiable(modifiableMap).entrySet().iterator().next();
        assertUnmodifiableType(e, MAMap.MAEntry.class);
        e = (Entry<String, String>)MACollections.unmodifiable(modifiableMap).entrySet().toArray()[0];
        assertUnmodifiableType(e, MAMap.MAEntry.class);
        e = MACollections.unmodifiable(modifiableMap).entrySet().toArray(new Entry[2])[0];
        assertUnmodifiableType(e, MAMap.MAEntry.class);
    }
    
    @Test
    public void testUnmodifiableCollectionAndIterator() {
        
        Collection<String> modifiableCollection;
        Map<String, String> modifiableMap;
        Collection<String> c;
        Iterator<String> iterator;
        Collection<Entry<String, String>> ec;
        Iterator<Entry<String, String>> eiterator;
        
        c = MACollections.unmodifiable(new java.util.LinkedHashSet<String>());
        assertUnmodifiableType(c, Set.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, Iterator.class);
        
        modifiableCollection = new java.util.TreeSet<String>();
        modifiableCollection.add("A");
        c = MACollections.unmodifiable(modifiableCollection);
        assertUnmodifiableType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).descendingSet();
        assertUnmodifiableType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).headSet("A", true);
        assertUnmodifiableType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).tailSet("A", true);
        assertUnmodifiableType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).subSet("A", true, "A", true);
        assertUnmodifiableType(c, NavigableSet.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, Iterator.class);
        iterator = ((NavigableSet<String>)c).descendingIterator();
        assertUnmodifiableType(iterator, Iterator.class);
        
        c = MACollections.unmodifiable(new java.util.LinkedHashMap<String, String>().keySet());
        assertUnmodifiableType(c, Set.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, Iterator.class);
        
        modifiableMap = new java.util.TreeMap<String, String>();
        modifiableMap.put("A", "a");
        c = MACollections.unmodifiable(((NavigableMap<String, String>)modifiableMap).descendingMap().keySet());
        assertUnmodifiableType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).descendingSet();
        assertUnmodifiableType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).headSet("A", true);
        assertUnmodifiableType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).tailSet("A", true);
        assertUnmodifiableType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).subSet("A", true, "A", true);
        assertUnmodifiableType(c, NavigableSet.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, Iterator.class);
        iterator = ((NavigableSet<String>)c).descendingIterator();
        assertUnmodifiableType(iterator, Iterator.class);
        
        ec = MACollections.unmodifiable(new java.util.LinkedHashMap<String, String>().entrySet());
        assertUnmodifiableType(ec, Set.class);
        eiterator = ec.iterator();
        assertUnmodifiableType(eiterator, Iterator.class);
        
        ec = MACollections.unmodifiable(new java.util.TreeMap<String, String>().entrySet());
        assertUnmodifiableType(ec, Set.class);
        eiterator = ec.iterator();
        assertUnmodifiableType(eiterator, Iterator.class);
        
        c = MACollections.unmodifiable(new java.util.LinkedHashMap<String, String>().values());
        assertUnmodifiableType(c, Collection.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, Iterator.class);
        
        c = MACollections.unmodifiable(new java.util.TreeMap<String, String>().values());
        assertUnmodifiableType(c, Collection.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, Iterator.class);
        
        c = MACollections.unmodifiable(new java.util.ArrayList<String>());
        Assert.assertTrue(c instanceof RandomAccess);
        assertUnmodifiableType(c, List.class);
        c = ((List<String>)c).subList(0, 0);
        Assert.assertTrue(c instanceof RandomAccess);
        assertUnmodifiableType(c, List.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, ListIterator.class);
        
        c = MACollections.unmodifiable(new java.util.LinkedList<String>());
        Assert.assertFalse(c instanceof RandomAccess);
        assertUnmodifiableType(c, List.class);
        c = ((List<String>)c).subList(0, 0);
        Assert.assertFalse(c instanceof RandomAccess);
        assertUnmodifiableType(c, List.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, ListIterator.class);
    }
    
    @Test
    public void testUnmodifiableXCollectionAndIterator() {
        
        Map<String, String> modifiableMap;
        Collection<String> modifiableCollection;
        Collection<String> c;
        Iterator<String> iterator;
        Collection<Entry<String, String>> ec;
        Iterator<Entry<String, String>> eiterator;
        
        c = MACollections.unmodifiable(new HashSet<String>());
        assertUnmodifiableType(c, XSet.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, XCollection.XIterator.class);
        
        c = MACollections.unmodifiable(new LinkedHashSet<String>());
        assertUnmodifiableType(c, XOrderedSet.class);
        c = ((XOrderedSet<String>)c).descendingSet();
        assertUnmodifiableType(c, XOrderedSet.XOrderedSetView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, XCollection.XIterator.class);
        iterator = ((XOrderedSet<String>)c).descendingIterator();
        assertUnmodifiableType(iterator, XCollection.XIterator.class);
        
        modifiableCollection = new TreeSet<String>();
        modifiableCollection.add("A");
        c = MACollections.unmodifiable(modifiableCollection);
        assertUnmodifiableType(c, XNavigableSet.class);
        c = ((XNavigableSet<String>)c).descendingSet();
        assertUnmodifiableType(c, XNavigableSet.XNavigableSetView.class);
        c = ((XNavigableSet<String>)c).headSet("A", true);
        assertUnmodifiableType(c, XNavigableSet.XNavigableSetView.class);
        c = ((XNavigableSet<String>)c).tailSet("A", true);
        assertUnmodifiableType(c, XNavigableSet.XNavigableSetView.class);
        c = ((XNavigableSet<String>)c).subSet("A", true, "A", true);
        assertUnmodifiableType(c, XNavigableSet.XNavigableSetView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, XCollection.XIterator.class);
        iterator = ((XNavigableSet<String>)c).descendingIterator();
        assertUnmodifiableType(iterator, XCollection.XIterator.class);
        
        c = MACollections.unmodifiable(new HashMap<String, String>().keySet());
        assertUnmodifiableType(c, XMap.XKeySetView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, XCollection.XIterator.class);
        
        c = MACollections.unmodifiable(new LinkedHashMap<String, String>().keySet());
        assertUnmodifiableType(c, XOrderedMap.XOrderedKeySetView.class);
        c = ((XOrderedSet<String>)c).descendingSet();
        assertUnmodifiableType(c, XOrderedMap.XOrderedKeySetView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, XCollection.XIterator.class);
        iterator = ((XOrderedSet<String>)c).descendingIterator();
        assertUnmodifiableType(iterator, XCollection.XIterator.class);
        
        modifiableMap = new TreeMap<String, String>();
        modifiableMap.put("A", "a");
        c = MACollections.unmodifiable(((NavigableMap<String, String>)modifiableMap).descendingMap().keySet());
        assertUnmodifiableType(c, XNavigableMap.XNavigableKeySetView.class);
        c = ((NavigableSet<String>)c).descendingSet();
        assertUnmodifiableType(c, XNavigableMap.XNavigableKeySetView.class);
        c = ((NavigableSet<String>)c).headSet("A", true);
        assertUnmodifiableType(c, XNavigableMap.XNavigableKeySetView.class);
        c = ((NavigableSet<String>)c).tailSet("A", true);
        assertUnmodifiableType(c, XNavigableMap.XNavigableKeySetView.class);
        c = ((NavigableSet<String>)c).subSet("A", true, "A", true);
        assertUnmodifiableType(c, XNavigableMap.XNavigableKeySetView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, XCollection.XIterator.class);
        iterator = ((NavigableSet<String>)c).descendingIterator();
        assertUnmodifiableType(iterator, XCollection.XIterator.class);
        
        ec = MACollections.unmodifiable(new HashMap<String, String>().entrySet());
        assertUnmodifiableType(ec, XMap.XEntrySetView.class);
        eiterator = ec.iterator();
        assertUnmodifiableType(eiterator, XMap.XEntrySetView.XEntrySetIterator.class);
        
        ec = MACollections.unmodifiable(new LinkedHashMap<String, String>().entrySet());
        assertUnmodifiableType(ec, XMap.XEntrySetView.class);
        eiterator = ec.iterator();
        assertUnmodifiableType(eiterator, XMap.XEntrySetView.XEntrySetIterator.class);
        
        ec = MACollections.unmodifiable(new TreeMap<String, String>().entrySet());
        assertUnmodifiableType(ec, XMap.XEntrySetView.class);
        eiterator = ec.iterator();
        assertUnmodifiableType(eiterator, XMap.XEntrySetView.XEntrySetIterator.class);
        
        c = MACollections.unmodifiable(new HashMap<String, String>().values());
        assertUnmodifiableType(c, XMap.XValuesView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, XCollection.XIterator.class);
        
        c = MACollections.unmodifiable(new LinkedHashMap<String, String>().values());
        assertUnmodifiableType(c, XMap.XValuesView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, XCollection.XIterator.class);
        
        c = MACollections.unmodifiable(new TreeMap<String, String>().values());
        assertUnmodifiableType(c, XMap.XValuesView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, XCollection.XIterator.class);
        
        c = MACollections.unmodifiable(new ArrayList<String>());
        Assert.assertTrue(c instanceof RandomAccess);
        assertUnmodifiableType(c, XList.class);
        c = ((List<String>)c).subList(0, 0);
        Assert.assertTrue(c instanceof RandomAccess);
        assertUnmodifiableType(c, XList.XListView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, XList.XListIterator.class);
        
        c = MACollections.unmodifiable(new LinkedList<String>());
        Assert.assertFalse(c instanceof RandomAccess);
        assertUnmodifiableType(c, XList.class);
        c = ((List<String>)c).subList(0, 0);
        Assert.assertFalse(c instanceof RandomAccess);
        assertUnmodifiableType(c, XList.XListView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, XList.XListIterator.class);
    }
    
    @Test
    public void testUnmodifiableMACollectionAndIterator() {
        
        Map<String, String> modifiableMap;
        Collection<String> modifiableCollection;
        Collection<String> c;
        Iterator<String> iterator;
        Collection<Entry<String, String>> ec;
        Iterator<Entry<String, String>> eiterator;
        
        c = MACollections.unmodifiable(new MAHashSet<String>());
        assertUnmodifiableType(c, MASet.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, MACollection.MAIterator.class);
        
        c = MACollections.unmodifiable(new MALinkedHashSet<String>());
        assertUnmodifiableType(c, MAOrderedSet.class);
        c = ((XOrderedSet<String>)c).descendingSet();
        assertUnmodifiableType(c, MAOrderedSet.MAOrderedSetView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, MACollection.MAIterator.class);
        iterator = ((XOrderedSet<String>)c).descendingIterator();
        assertUnmodifiableType(iterator, MACollection.MAIterator.class);
        
        modifiableCollection = new MATreeSet<String>();
        modifiableCollection.add("A");
        c = MACollections.unmodifiable(modifiableCollection);
        assertUnmodifiableType(c, MANavigableSet.class);
        c = ((XNavigableSet<String>)c).descendingSet();
        assertUnmodifiableType(c, MANavigableSet.MANavigableSetView.class);
        c = ((XNavigableSet<String>)c).headSet("A", true);
        assertUnmodifiableType(c, MANavigableSet.MANavigableSetView.class);
        c = ((XNavigableSet<String>)c).tailSet("A", true);
        assertUnmodifiableType(c, MANavigableSet.MANavigableSetView.class);
        c = ((XNavigableSet<String>)c).subSet("A", true, "A", true);
        assertUnmodifiableType(c, MANavigableSet.MANavigableSetView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, MACollection.MAIterator.class);
        iterator = ((XNavigableSet<String>)c).descendingIterator();
        assertUnmodifiableType(iterator, MACollection.MAIterator.class);
        
        c = MACollections.unmodifiable(new MAHashMap<String, String>().keySet());
        assertUnmodifiableType(c, MAMap.MAKeySetView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, MAMap.MAKeySetView.MAKeySetIterator.class);
        
        c = MACollections.unmodifiable(new MALinkedHashMap<String, String>().keySet());
        assertUnmodifiableType(c, MAOrderedMap.MAOrderedKeySetView.class);
        c = ((XOrderedSet<String>)c).descendingSet();
        assertUnmodifiableType(c, MAOrderedMap.MAOrderedKeySetView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, MAMap.MAKeySetView.MAKeySetIterator.class);
        iterator = ((XOrderedSet<String>)c).descendingIterator();
        assertUnmodifiableType(iterator, MAMap.MAKeySetView.MAKeySetIterator.class);
        
        modifiableMap = new MATreeMap<String, String>();
        modifiableMap.put("A", "a");
        c = MACollections.unmodifiable(((NavigableMap<String, String>)modifiableMap).descendingMap().keySet());
        assertUnmodifiableType(c, MANavigableMap.MANavigableKeySetView.class);
        c = ((NavigableSet<String>)c).descendingSet();
        assertUnmodifiableType(c, MANavigableMap.MANavigableKeySetView.class);
        c = ((NavigableSet<String>)c).headSet("A", true);
        assertUnmodifiableType(c, MANavigableMap.MANavigableKeySetView.class);
        c = ((NavigableSet<String>)c).tailSet("A", true);
        assertUnmodifiableType(c, MANavigableMap.MANavigableKeySetView.class);
        c = ((NavigableSet<String>)c).subSet("A", true, "A", true);
        assertUnmodifiableType(c, MANavigableMap.MANavigableKeySetView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, MAMap.MAKeySetView.MAKeySetIterator.class);
        iterator = ((NavigableSet<String>)c).descendingIterator();
        assertUnmodifiableType(iterator, MAMap.MAKeySetView.MAKeySetIterator.class);
        
        ec = MACollections.unmodifiable(new MAHashMap<String, String>().entrySet());
        assertUnmodifiableType(ec, MAMap.MAEntrySetView.class);
        eiterator = ec.iterator();
        assertUnmodifiableType(eiterator, MAMap.MAEntrySetView.MAEntrySetIterator.class);
        
        ec = MACollections.unmodifiable(new MALinkedHashMap<String, String>().entrySet());
        assertUnmodifiableType(ec, MAMap.MAEntrySetView.class);
        eiterator = ec.iterator();
        assertUnmodifiableType(eiterator, MAMap.MAEntrySetView.MAEntrySetIterator.class);
        
        ec = MACollections.unmodifiable(new MATreeMap<String, String>().entrySet());
        assertUnmodifiableType(ec, MAMap.MAEntrySetView.class);
        eiterator = ec.iterator();
        assertUnmodifiableType(eiterator, MAMap.MAEntrySetView.MAEntrySetIterator.class);
        
        c = MACollections.unmodifiable(new MAHashMap<String, String>().values());
        assertUnmodifiableType(c, MAMap.MAValuesView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, MAMap.MAValuesView.MAValuesIterator.class);
        
        c = MACollections.unmodifiable(new MALinkedHashMap<String, String>().values());
        assertUnmodifiableType(c, MAMap.MAValuesView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, MAMap.MAValuesView.MAValuesIterator.class);
        
        c = MACollections.unmodifiable(new MATreeMap<String, String>().values());
        assertUnmodifiableType(c, MAMap.MAValuesView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, MAMap.MAValuesView.MAValuesIterator.class);
        
        c = MACollections.unmodifiable(new MAArrayList<String>());
        Assert.assertTrue(c instanceof RandomAccess);
        assertUnmodifiableType(c, MAList.class);
        c = ((List<String>)c).subList(0, 0);
        Assert.assertTrue(c instanceof RandomAccess);
        assertUnmodifiableType(c, MAList.MAListView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, MAList.MAListIterator.class);
        
        c = MACollections.unmodifiable(new MALinkedList<String>());
        Assert.assertFalse(c instanceof RandomAccess);
        assertUnmodifiableType(c, MAList.class);
        c = ((List<String>)c).subList(0, 0);
        Assert.assertFalse(c instanceof RandomAccess);
        assertUnmodifiableType(c, MAList.MAListView.class);
        iterator = c.iterator();
        assertUnmodifiableType(iterator, MAList.MAListIterator.class);
    }
    
    @Test
    public void testUnmodifiableMap() {
        
        Map<String, String> modifiableMap;
        Map<String, String> m;
        
        m = MACollections.unmodifiable(new java.util.LinkedHashMap<String, String>());
        assertUnmodifiableType(m, Map.class);
        
        modifiableMap = new java.util.TreeMap<String, String>();
        modifiableMap.put("A", "a");
        m = MACollections.unmodifiable(modifiableMap);
        assertUnmodifiableType(m, NavigableMap.class);
        m = ((NavigableMap<String, String>)m).descendingMap();
        assertUnmodifiableType(m, NavigableMap.class);
        m = ((NavigableMap<String, String>)m).headMap("A", true);
        assertUnmodifiableType(m, NavigableMap.class);
        m = ((NavigableMap<String, String>)m).tailMap("A", true);
        assertUnmodifiableType(m, NavigableMap.class);
        m = ((NavigableMap<String, String>)m).subMap("A", true, "A", true);
        assertUnmodifiableType(m, NavigableMap.class);
    }
    
    @Test
    public void testUnmodifiableXMap() {
        
        Map<String, String> modifiableMap;
        Map<String, String> m;
        
        m = MACollections.unmodifiable(new HashMap<String, String>());
        assertUnmodifiableType(m, XMap.class);
        
        m = MACollections.unmodifiable(new LinkedHashMap<String, String>());
        assertUnmodifiableType(m, XOrderedMap.class);
        m = ((XOrderedMap<String, String>)m).descendingMap();
        assertUnmodifiableType(m, XOrderedMap.XOrderedMapView.class);
        
        modifiableMap = new TreeMap<String, String>();
        modifiableMap.put("A", "a");
        m = MACollections.unmodifiable(modifiableMap);
        assertUnmodifiableType(m, XNavigableMap.class);
        m = ((NavigableMap<String, String>)m).descendingMap();
        assertUnmodifiableType(m, XNavigableMap.XNavigableMapView.class);
        m = ((NavigableMap<String, String>)m).headMap("A", true);
        assertUnmodifiableType(m, XNavigableMap.XNavigableMapView.class);
        m = ((NavigableMap<String, String>)m).tailMap("A", true);
        assertUnmodifiableType(m, XNavigableMap.XNavigableMapView.class);
        m = ((NavigableMap<String, String>)m).subMap("A", true, "A", true);
        assertUnmodifiableType(m, XNavigableMap.XNavigableMapView.class);
    }
    
    @Test
    public void testUnmodifiableMAMap() {
        
        Map<String, String> modifiableMap;
        Map<String, String> m;
        
        m = MACollections.unmodifiable(new MAHashMap<String, String>());
        assertUnmodifiableType(m, MAMap.class);
        
        m = MACollections.unmodifiable(new MALinkedHashMap<String, String>());
        assertUnmodifiableType(m, MAOrderedMap.class);
        m = ((XOrderedMap<String, String>)m).descendingMap();
        assertUnmodifiableType(m, MAOrderedMap.MAOrderedMapView.class);
        
        modifiableMap = new MATreeMap<String, String>();
        modifiableMap.put("A", "a");
        m = MACollections.unmodifiable(modifiableMap);
        assertUnmodifiableType(m, MANavigableMap.class);
        m = ((NavigableMap<String, String>)m).descendingMap();
        assertUnmodifiableType(m, MANavigableMap.MANavigableMapView.class);
        m = ((NavigableMap<String, String>)m).headMap("A", true);
        assertUnmodifiableType(m, MANavigableMap.MANavigableMapView.class);
        m = ((NavigableMap<String, String>)m).tailMap("A", true);
        assertUnmodifiableType(m, MANavigableMap.MANavigableMapView.class);
        m = ((NavigableMap<String, String>)m).subMap("A", true, "A", true);
        assertUnmodifiableType(m, MANavigableMap.MANavigableMapView.class);
    }
    
    @Test
    public void testLockedEntry() {
        
        Map<String, String> m;
        Map.Entry<String, String> e;
        Function<Iterator<Entry<String, String>>, Entry<String, String>> lockingFunction = 
            (Iterator<Entry<String, String>>iterator) -> iterator.next();
        
        m = new java.util.HashMap<String, String>();
        m.put("A", "a");
        m = MACollections.locked(m);
        e = MACollections.locking(m.entrySet().iterator(), LockMode.READ, lockingFunction);
        assertLockingType(e, Map.Entry.class);
        
        m = new java.util.LinkedHashMap<String, String>();
        m.put("A", "a");
        m = MACollections.locked(m);
        e = MACollections.locking(m.entrySet().iterator(), LockMode.READ, lockingFunction);
        assertLockingType(e, Map.Entry.class);
        
        m = new java.util.TreeMap<String, String>().descendingMap();
        m.put("A", "a");
        m = MACollections.locked(m);
        e = MACollections.locking(m.entrySet().iterator(), LockMode.READ, lockingFunction);
        assertLockingType(e, Map.Entry.class);
    }
    
    @Test
    public void testLockedXEntry() {
        
        Map<String, String> m;
        Map.Entry<String, String> e;
        Function<Iterator<Entry<String, String>>, Entry<String, String>> lockingFunction = 
            (Iterator<Entry<String, String>> iterator) -> iterator.next();
        
        m = new HashMap<String, String>();
        m.put("A", "a");
        m = MACollections.locked(m);
        e = MACollections.locking(m.entrySet().iterator(), LockMode.READ, lockingFunction);
        assertLockingType(e, XMap.XEntry.class);
        
        m = new LinkedHashMap<String, String>();
        m.put("A", "a");
        m = MACollections.locked(m);
        e = MACollections.locking(m.entrySet().iterator(), LockMode.READ, lockingFunction);
        assertLockingType(e, XMap.XEntry.class);
        
        m = new TreeMap<String, String>().descendingMap();
        m.put("A", "a");
        m = MACollections.locked(m);
        e = MACollections.locking(m.entrySet().iterator(), LockMode.READ, lockingFunction);
        assertLockingType(e, XMap.XEntry.class);
    }
    
    @Test
    public void testLockedMAEntry() {
        
        Map<String, String> m;
        Map.Entry<String, String> e;
        Function<Iterator<Entry<String, String>>, Entry<String, String>> lockingFunction = 
            (Iterator<Entry<String, String>> iterator) -> iterator.next();
        
        m = new HashMap<String, String>();
        m.put("A", "a");
        m = MACollections.locked(m);
        e = MACollections.locking(m.entrySet().iterator(), LockMode.READ, lockingFunction);
        assertLockingType(e, XMap.XEntry.class);
        
        m = new LinkedHashMap<String, String>();
        m.put("A", "a");
        m = MACollections.locked(m);
        e = MACollections.locking(m.entrySet().iterator(), LockMode.READ, lockingFunction);
        assertLockingType(e, XMap.XEntry.class);
        
        m = new TreeMap<String, String>().descendingMap();
        m.put("A", "a");
        m = MACollections.locked(m);
        e = MACollections.locking(m.entrySet().iterator(), LockMode.READ, lockingFunction);
        assertLockingType(e, XMap.XEntry.class);
    }
    
    @Test
    public void testLockedCollectionAndIterator() {
        
        Map<String, String> m;
        Collection<String> c;
        Iterator<String> iterator;
        Collection<Entry<String, String>> ec;
        Iterator<Entry<String, String>> eiterator;
        
        c = MACollections.locked(new java.util.LinkedHashSet<String>());
        assertLockedType(c, Set.class);
        iterator = c.iterator();
        assertLockedType(iterator, Iterator.class);
        
        c = new java.util.TreeSet<String>();
        c.add("A");
        c = MACollections.locked(c);
        assertLockedType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).descendingSet();
        assertLockedType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).headSet("A", true);
        assertLockedType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).tailSet("A", true);
        assertLockedType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).subSet("A", true, "A", true);
        assertLockedType(c, NavigableSet.class);
        iterator = c.iterator();
        assertLockedType(iterator, Iterator.class);
        iterator = ((NavigableSet<String>)c).descendingIterator();
        assertLockedType(iterator, Iterator.class);
        
        c = MACollections.locked(new java.util.LinkedHashMap<String, String>().keySet());
        assertLockedType(c, Set.class);
        iterator = c.iterator();
        assertLockedType(iterator, Iterator.class);
        
        m = new java.util.TreeMap<String, String>();
        m.put("A", "a");
        c = MACollections.locked(((NavigableMap<String, String>)m).descendingMap().keySet());
        assertLockedType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).descendingSet();
        assertLockedType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).headSet("A", true);
        assertLockedType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).tailSet("A", true);
        assertLockedType(c, NavigableSet.class);
        c = ((NavigableSet<String>)c).subSet("A", true, "A", true);
        assertLockedType(c, NavigableSet.class);
        iterator = c.iterator();
        assertLockedType(iterator, Iterator.class);
        iterator = ((NavigableSet<String>)c).descendingIterator();
        assertLockedType(iterator, Iterator.class);
        
        ec = MACollections.locked(new java.util.LinkedHashMap<String, String>().entrySet());
        assertLockedType(ec, Set.class);
        eiterator = ec.iterator();
        assertLockedType(eiterator, Iterator.class);
        
        ec = MACollections.locked(new java.util.TreeMap<String, String>().entrySet());
        assertLockedType(ec, Set.class);
        eiterator = ec.iterator();
        assertLockedType(eiterator, Iterator.class);
        
        c = MACollections.locked(new java.util.LinkedHashMap<String, String>().values());
        assertLockedType(c, Collection.class);
        iterator = c.iterator();
        assertLockedType(iterator, Iterator.class);
        
        c = MACollections.locked(new java.util.TreeMap<String, String>().values());
        assertLockedType(c, Collection.class);
        iterator = c.iterator();
        assertLockedType(iterator, Iterator.class);
        
        c = MACollections.locked(new java.util.ArrayList<String>());
        Assert.assertTrue(c instanceof RandomAccess);
        assertLockedType(c, List.class);
        c = ((List<String>)c).subList(0, 0);
        Assert.assertTrue(c instanceof RandomAccess);
        assertLockedType(c, List.class);
        iterator = c.iterator();
        assertLockedType(iterator, ListIterator.class);
        
        c = MACollections.locked(new java.util.LinkedList<String>());
        Assert.assertFalse(c instanceof RandomAccess);
        assertLockedType(c, List.class);
        c = ((List<String>)c).subList(0, 0);
        Assert.assertFalse(c instanceof RandomAccess);
        assertLockedType(c, List.class);
        iterator = c.iterator();
        assertLockedType(iterator, ListIterator.class);
    }
    
    @Test
    public void testLockedXCollectionAndIterator() {
        
        Map<String, String> m;
        Collection<String> c;
        Iterator<String> iterator;
        Collection<Entry<String, String>> ec;
        Iterator<Entry<String, String>> eiterator;
        
        c = MACollections.locked(new HashSet<String>());
        assertLockedType(c, XSet.class);
        iterator = c.iterator();
        assertLockedType(iterator, XCollection.XIterator.class);
        
        c = MACollections.locked(new LinkedHashSet<String>());
        assertLockedType(c, XOrderedSet.class);
        c = ((XOrderedSet<String>)c).descendingSet();
        assertLockedType(c, XOrderedSet.XOrderedSetView.class);
        iterator = c.iterator();
        assertLockedType(iterator, XCollection.XIterator.class);
        iterator = ((XOrderedSet<String>)c).descendingIterator();
        assertLockedType(iterator, XCollection.XIterator.class);
        
        c = new TreeSet<String>();
        c.add("A");
        c = MACollections.locked(c);
        assertLockedType(c, XNavigableSet.class);
        c = ((XNavigableSet<String>)c).descendingSet();
        assertLockedType(c, XNavigableSet.XNavigableSetView.class);
        c = ((XNavigableSet<String>)c).headSet("A", true);
        assertLockedType(c, XNavigableSet.XNavigableSetView.class);
        c = ((XNavigableSet<String>)c).tailSet("A", true);
        assertLockedType(c, XNavigableSet.XNavigableSetView.class);
        c = ((XNavigableSet<String>)c).subSet("A", true, "A", true);
        assertLockedType(c, XNavigableSet.XNavigableSetView.class);
        iterator = c.iterator();
        assertLockedType(iterator, XCollection.XIterator.class);
        iterator = ((XNavigableSet<String>)c).descendingIterator();
        assertLockedType(iterator, XCollection.XIterator.class);
        
        c = MACollections.locked(new HashMap<String, String>().keySet());
        assertLockedType(c, XMap.XKeySetView.class);
        iterator = c.iterator();
        assertLockedType(iterator, XCollection.XIterator.class);
        
        c = MACollections.locked(new LinkedHashMap<String, String>().keySet());
        assertLockedType(c, XOrderedMap.XOrderedKeySetView.class);
        c = ((XOrderedSet<String>)c).descendingSet();
        assertLockedType(c, XOrderedMap.XOrderedKeySetView.class);
        iterator = c.iterator();
        assertLockedType(iterator, XCollection.XIterator.class);
        iterator = ((XOrderedSet<String>)c).descendingIterator();
        assertLockedType(iterator, XCollection.XIterator.class);
        
        m = new TreeMap<String, String>();
        m.put("A", "a");
        c = MACollections.locked(((NavigableMap<String, String>)m).descendingMap().keySet());
        assertLockedType(c, XNavigableMap.XNavigableKeySetView.class);
        c = ((NavigableSet<String>)c).descendingSet();
        assertLockedType(c, XNavigableMap.XNavigableKeySetView.class);
        c = ((NavigableSet<String>)c).headSet("A", true);
        assertLockedType(c, XNavigableMap.XNavigableKeySetView.class);
        c = ((NavigableSet<String>)c).tailSet("A", true);
        assertLockedType(c, XNavigableMap.XNavigableKeySetView.class);
        c = ((NavigableSet<String>)c).subSet("A", true, "A", true);
        assertLockedType(c, XNavigableMap.XNavigableKeySetView.class);
        iterator = c.iterator();
        assertLockedType(iterator, XCollection.XIterator.class);
        iterator = ((NavigableSet<String>)c).descendingIterator();
        assertLockedType(iterator, XCollection.XIterator.class);
        
        ec = MACollections.locked(new HashMap<String, String>().entrySet());
        assertLockedType(ec, XMap.XEntrySetView.class);
        eiterator = ec.iterator();
        assertLockedType(eiterator, XMap.XEntrySetView.XEntrySetIterator.class);
        
        ec = MACollections.locked(new LinkedHashMap<String, String>().entrySet());
        assertLockedType(ec, XMap.XEntrySetView.class);
        eiterator = ec.iterator();
        assertLockedType(eiterator, XMap.XEntrySetView.XEntrySetIterator.class);
        
        ec = MACollections.locked(new TreeMap<String, String>().entrySet());
        assertLockedType(ec, XMap.XEntrySetView.class);
        eiterator = ec.iterator();
        assertLockedType(eiterator, XMap.XEntrySetView.XEntrySetIterator.class);
        
        c = MACollections.locked(new HashMap<String, String>().values());
        assertLockedType(c, XMap.XValuesView.class);
        iterator = c.iterator();
        assertLockedType(iterator, XCollection.XIterator.class);
        
        c = MACollections.locked(new LinkedHashMap<String, String>().values());
        assertLockedType(c, XMap.XValuesView.class);
        iterator = c.iterator();
        assertLockedType(iterator, XCollection.XIterator.class);
        
        c = MACollections.locked(new TreeMap<String, String>().values());
        assertLockedType(c, XMap.XValuesView.class);
        iterator = c.iterator();
        assertLockedType(iterator, XCollection.XIterator.class);
        
        c = MACollections.locked(new ArrayList<String>());
        Assert.assertTrue(c instanceof RandomAccess);
        assertLockedType(c, XList.class);
        c = ((List<String>)c).subList(0, 0);
        Assert.assertTrue(c instanceof RandomAccess);
        assertLockedType(c, XList.XListView.class);
        iterator = c.iterator();
        assertLockedType(iterator, XList.XListIterator.class);
        
        c = MACollections.locked(new LinkedList<String>());
        Assert.assertFalse(c instanceof RandomAccess);
        assertLockedType(c, XList.class);
        c = ((List<String>)c).subList(0, 0);
        Assert.assertFalse(c instanceof RandomAccess);
        assertLockedType(c, XList.XListView.class);
        iterator = c.iterator();
        assertLockedType(iterator, XList.XListIterator.class);
    }
    
    @Test
    public void testLockedMACollectionAndIterator() {
        
        Map<String, String> m;
        Collection<String> c;
        Iterator<String> iterator;
        Collection<Entry<String, String>> ec;
        Iterator<Entry<String, String>> eiterator;
        
        c = MACollections.locked(new MAHashSet<String>());
        assertLockedType(c, MASet.class);
        iterator = c.iterator();
        assertLockedType(iterator, MACollection.MAIterator.class);
        
        c = MACollections.locked(new MALinkedHashSet<String>());
        assertLockedType(c, MAOrderedSet.class);
        c = ((XOrderedSet<String>)c).descendingSet();
        assertLockedType(c, MAOrderedSet.MAOrderedSetView.class);
        iterator = c.iterator();
        assertLockedType(iterator, MACollection.MAIterator.class);
        iterator = ((XOrderedSet<String>)c).descendingIterator();
        assertLockedType(iterator, MACollection.MAIterator.class);
        
        c = new MATreeSet<String>();
        c.add("A");
        c = MACollections.locked(c);
        assertLockedType(c, MANavigableSet.class);
        c = ((XNavigableSet<String>)c).descendingSet();
        assertLockedType(c, MANavigableSet.MANavigableSetView.class);
        c = ((XNavigableSet<String>)c).headSet("A", true);
        assertLockedType(c, MANavigableSet.MANavigableSetView.class);
        c = ((XNavigableSet<String>)c).tailSet("A", true);
        assertLockedType(c, MANavigableSet.MANavigableSetView.class);
        c = ((XNavigableSet<String>)c).subSet("A", true, "A", true);
        assertLockedType(c, MANavigableSet.MANavigableSetView.class);
        iterator = c.iterator();
        assertLockedType(iterator, MACollection.MAIterator.class);
        iterator = ((XNavigableSet<String>)c).descendingIterator();
        assertLockedType(iterator, MACollection.MAIterator.class);
        
        c = MACollections.locked(new MAHashMap<String, String>().keySet());
        assertLockedType(c, MAMap.MAKeySetView.class);
        iterator = c.iterator();
        assertLockedType(iterator, MAMap.MAKeySetView.MAKeySetIterator.class);
        
        c = MACollections.locked(new MALinkedHashMap<String, String>().keySet());
        assertLockedType(c, MAOrderedMap.MAOrderedKeySetView.class);
        c = ((XOrderedSet<String>)c).descendingSet();
        assertLockedType(c, MAOrderedMap.MAOrderedKeySetView.class);
        iterator = c.iterator();
        assertLockedType(iterator, MAMap.MAKeySetView.MAKeySetIterator.class);
        iterator = ((XOrderedSet<String>)c).descendingIterator();
        assertLockedType(iterator, MAMap.MAKeySetView.MAKeySetIterator.class);
        
        m = new MATreeMap<String, String>();
        m.put("A", "a");
        c = MACollections.locked(((NavigableMap<String, String>)m).descendingMap().keySet());
        assertLockedType(c, MANavigableMap.MANavigableKeySetView.class);
        c = ((NavigableSet<String>)c).descendingSet();
        assertLockedType(c, MANavigableMap.MANavigableKeySetView.class);
        c = ((NavigableSet<String>)c).headSet("A", true);
        assertLockedType(c, MANavigableMap.MANavigableKeySetView.class);
        c = ((NavigableSet<String>)c).tailSet("A", true);
        assertLockedType(c, MANavigableMap.MANavigableKeySetView.class);
        c = ((NavigableSet<String>)c).subSet("A", true, "A", true);
        assertLockedType(c, MANavigableMap.MANavigableKeySetView.class);
        iterator = c.iterator();
        assertLockedType(iterator, MAMap.MAKeySetView.MAKeySetIterator.class);
        iterator = ((NavigableSet<String>)c).descendingIterator();
        assertLockedType(iterator, MAMap.MAKeySetView.MAKeySetIterator.class);
        
        ec = MACollections.locked(new MAHashMap<String, String>().entrySet());
        assertLockedType(ec, MAMap.MAEntrySetView.class);
        eiterator = ec.iterator();
        assertLockedType(eiterator, MAMap.MAEntrySetView.MAEntrySetIterator.class);
        
        ec = MACollections.locked(new MALinkedHashMap<String, String>().entrySet());
        assertLockedType(ec, MAMap.MAEntrySetView.class);
        eiterator = ec.iterator();
        assertLockedType(eiterator, MAMap.MAEntrySetView.MAEntrySetIterator.class);
        
        ec = MACollections.locked(new MATreeMap<String, String>().entrySet());
        assertLockedType(ec, MAMap.MAEntrySetView.class);
        eiterator = ec.iterator();
        assertLockedType(eiterator, MAMap.MAEntrySetView.MAEntrySetIterator.class);
        
        c = MACollections.locked(new MAHashMap<String, String>().values());
        assertLockedType(c, MAMap.MAValuesView.class);
        iterator = c.iterator();
        assertLockedType(iterator, MAMap.MAValuesView.MAValuesIterator.class);
        
        c = MACollections.locked(new MALinkedHashMap<String, String>().values());
        assertLockedType(c, MAMap.MAValuesView.class);
        iterator = c.iterator();
        assertLockedType(iterator, MAMap.MAValuesView.MAValuesIterator.class);
        
        c = MACollections.locked(new MATreeMap<String, String>().values());
        assertLockedType(c, MAMap.MAValuesView.class);
        iterator = c.iterator();
        assertLockedType(iterator, MAMap.MAValuesView.MAValuesIterator.class);
        
        c = MACollections.locked(new MAArrayList<String>());
        Assert.assertTrue(c instanceof RandomAccess);
        assertLockedType(c, MAList.class);
        c = ((List<String>)c).subList(0, 0);
        Assert.assertTrue(c instanceof RandomAccess);
        assertLockedType(c, MAList.MAListView.class);
        iterator = c.iterator();
        assertLockedType(iterator, MAList.MAListIterator.class);
        
        c = MACollections.locked(new MALinkedList<String>());
        Assert.assertFalse(c instanceof RandomAccess);
        assertLockedType(c, MAList.class);
        c = ((List<String>)c).subList(0, 0);
        Assert.assertFalse(c instanceof RandomAccess);
        assertLockedType(c, MAList.MAListView.class);
        iterator = c.iterator();
        assertLockedType(iterator, MAList.MAListIterator.class);
    }
    
    @Test
    public void testLockedMap() {
        
        Map<String, String> m;
        
        m = MACollections.locked(new java.util.LinkedHashMap<String, String>());
        assertLockedType(m, Map.class);
        
        m = new java.util.TreeMap<String, String>();
        m.put("A", "a");
        m = MACollections.locked(m);
        assertLockedType(m, NavigableMap.class);
        m = ((NavigableMap<String, String>)m).descendingMap();
        assertLockedType(m, NavigableMap.class);
        m = ((NavigableMap<String, String>)m).headMap("A", true);
        assertLockedType(m, NavigableMap.class);
        m = ((NavigableMap<String, String>)m).tailMap("A", true);
        assertLockedType(m, NavigableMap.class);
        m = ((NavigableMap<String, String>)m).subMap("A", true, "A", true);
        assertLockedType(m, NavigableMap.class);
    }
    
    @Test
    public void testLockedXMap() {
        
        Map<String, String> m;
        
        m = MACollections.locked(new HashMap<String, String>());
        assertLockedType(m, XMap.class);
        
        m = MACollections.locked(new LinkedHashMap<String, String>());
        assertLockedType(m, XOrderedMap.class);
        m = ((XOrderedMap<String, String>)m).descendingMap();
        assertLockedType(m, XOrderedMap.XOrderedMapView.class);
        
        m = new TreeMap<String, String>();
        m.put("A", "a");
        m = MACollections.locked(m);
        assertLockedType(m, XNavigableMap.class);
        m = ((NavigableMap<String, String>)m).descendingMap();
        assertLockedType(m, XNavigableMap.XNavigableMapView.class);
        m = ((NavigableMap<String, String>)m).headMap("A", true);
        assertLockedType(m, XNavigableMap.XNavigableMapView.class);
        m = ((NavigableMap<String, String>)m).tailMap("A", true);
        assertLockedType(m, XNavigableMap.XNavigableMapView.class);
        m = ((NavigableMap<String, String>)m).subMap("A", true, "A", true);
        assertLockedType(m, XNavigableMap.XNavigableMapView.class);
    }
    
    @Test
    public void testLockedMAMap() {
        
        Map<String, String> m;
        
        m = MACollections.locked(new MAHashMap<String, String>());
        assertLockedType(m, MAMap.class);
        
        m = MACollections.locked(new MALinkedHashMap<String, String>());
        assertLockedType(m, MAOrderedMap.class);
        m = ((XOrderedMap<String, String>)m).descendingMap();
        assertLockedType(m, MAOrderedMap.MAOrderedMapView.class);
        
        m = new MATreeMap<String, String>();
        m.put("A", "a");
        m = MACollections.locked(m);
        assertLockedType(m, MANavigableMap.class);
        m = ((NavigableMap<String, String>)m).descendingMap();
        assertLockedType(m, MANavigableMap.MANavigableMapView.class);
        m = ((NavigableMap<String, String>)m).headMap("A", true);
        assertLockedType(m, MANavigableMap.MANavigableMapView.class);
        m = ((NavigableMap<String, String>)m).tailMap("A", true);
        assertLockedType(m, MANavigableMap.MANavigableMapView.class);
        m = ((NavigableMap<String, String>)m).subMap("A", true, "A", true);
        assertLockedType(m, MANavigableMap.MANavigableMapView.class);
    }
    
    @Test
    public void testDuplicatedUnmodifiable() {
        
        Collection<String> c = new HashSet<String>();
        Collection<String> c1 = MACollections.unmodifiable(c);
        Collection<String> c2 = MACollections.unmodifiable(c1);
        
        Assert.assertNotSame(c1, c);
        Assert.assertSame(c, getInnerObject(c1));
        
        Assert.assertSame(c2, c1);
        Assert.assertSame(c, getInnerObject(c2));
    }
    
    @Test
    public void testDuplicatedInternalLocked() {
        
        Collection<String> c = new HashSet<String>();
        Collection<String> c1 = MACollections.locked(c);
        Collection<String> c2 = MACollections.locked(c1);
        
        Assert.assertTrue(c1 != c);
        Assert.assertSame(c, getInnerObject(c1));
        Assert.assertTrue(isUsingInternalLock(c1));
        Assert.assertTrue(isUsingInternalLock(c1.iterator()));
        
        Assert.assertTrue(c2 == c1);
        Assert.assertSame(c, getInnerObject(c2));
    }
    
    @Test
    public void testDuplicated3rdPartyLocked() {
        
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        Collection<String> c = new HashSet<String>();
        Collection<String> c1 = MACollections.locked(c, readWriteLock);
        Collection<String> c2 = MACollections.locked(c1, readWriteLock);
        
        Assert.assertNotSame(c1, c);
        Assert.assertSame(c, getInnerObject(c1));
        Assert.assertFalse(isUsingInternalLock(c1));
        Assert.assertFalse(isUsingInternalLock(c1.iterator()));
        
        Assert.assertSame(c2, c1);
        Assert.assertSame(c, getInnerObject(c2));
    }
    
    @Test
    public void testDuplicatedUnmodifiableAndLocked() {
        
        Collection<String> c = new HashSet<String>();
        Collection<String> c1 = MACollections.unmodifiable(c);
        Collection<String> c2 = MACollections.locked(c1);
        Collection<String> c3 = MACollections.unmodifiable(c2);
        Collection<String> c4 = MACollections.locked(c3);
        
        Assert.assertNotSame(c1, c);
        Assert.assertSame(c, getRealObject(c1));
        Assert.assertTrue(GATEWAY_PROXY_CLASS.isAssignableFrom(c1.getClass()));
        
        Assert.assertNotSame(c2, c1);
        Assert.assertSame(c, getRealObject(c2));
        Assert.assertTrue(GATEWAY_PROXY_CLASS.isAssignableFrom(c2.getClass()));
        Assert.assertFalse(GATEWAY_PROXY_CLASS.isAssignableFrom(getInnerObject(c2).getClass()));
        
        Assert.assertSame(c3, c2);
        Assert.assertSame(c4, c3);
    }
    
    @Test
    public void testDuplicatedLockedAndUnmodifiable() {
        
        Collection<String> c = new HashSet<String>();
        Collection<String> c1 = MACollections.locked(c);
        Collection<String> c2 = MACollections.unmodifiable(c1);
        Collection<String> c3 = MACollections.locked(c2);
        Collection<String> c4 = MACollections.unmodifiable(c3);
        
        Assert.assertNotSame(c1, c);
        Assert.assertSame(c, getRealObject(c1));
        Assert.assertFalse(GATEWAY_PROXY_CLASS.isAssignableFrom(c1.getClass()));
        
        Assert.assertNotSame(c2, c1);
        Assert.assertSame(c, getRealObject(c2));
        Assert.assertTrue(GATEWAY_PROXY_CLASS.isAssignableFrom(c2.getClass()));
        Assert.assertFalse(GATEWAY_PROXY_CLASS.isAssignableFrom(getInnerObject(c2).getClass()));
        
        Assert.assertSame(c3, c2);
        Assert.assertSame(c4, c3);
    }
    
    @Test
    public void testReturnableLocking() {
        StringBuilder builder = new StringBuilder();
        MockedReadWriteLock readWriteLock1 = new MockedReadWriteLock("A", builder);
        MockedReadWriteLock readWriteLock2 = new MockedReadWriteLock("B", builder);
        Collection<String> c = new LinkedHashSet<String>();
        c.add("A");
        c.add("B");
        c.add("C");
        c = MACollections.locked(c, readWriteLock1);
        c = MACollections.locked(c, readWriteLock2);
        Iterator<String> iterator = c.iterator();
        readWriteLock1.setName("I");
        readWriteLock2.setName("II");
        String value = MACollections.locking(
                iterator, 
                LockMode.READ, 
                (Iterator<String> itr) -> {
                    StringBuilder b = new StringBuilder();
                    while (itr.hasNext()) {
                        b
                        .append('-')
                        .append(itr.next());
                    }
                    return b.toString();
                });
        Assert.assertEquals("-LR:A-UR:A-LR:B-LR:A-UR:A-UR:B-LR:II-LR:I-UR:I-UR:II", builder.toString());
        Assert.assertEquals("-A-B-C", value);
    }
    
    @Test
    public void testLocking() {
        final StringBuilder builder = new StringBuilder();
        MockedReadWriteLock readWriteLock1 = new MockedReadWriteLock("A", builder);
        MockedReadWriteLock readWriteLock2 = new MockedReadWriteLock("B", builder);
        Collection<String> c = new LinkedHashSet<String>();
        c.add("A");
        c.add("B");
        c.add("C");
        c = MACollections.locked(c, readWriteLock1);
        c = MACollections.locked(c, readWriteLock2);
        Iterator<String> iterator = c.iterator();
        readWriteLock1.setName("I");
        readWriteLock2.setName("II");
        MACollections.locking(
                iterator, 
                LockMode.WRITE, 
                (Iterator<String> itr) -> {
                    while (itr.hasNext()) {
                        builder
                        .append('-')
                        .append(itr.next());
                    }
                }
        );
        Assert.assertEquals("-LR:A-UR:A-LR:B-LR:A-UR:A-UR:B-LW:II-LW:I-A-B-C-UW:I-UW:II", builder.toString());
    }
    
    @Test
    public void testLockingException() {
        final StringBuilder builder = new StringBuilder();
        MockedReadWriteLock readWriteLock1 = new MockedReadWriteLock("A", builder);
        MockedReadWriteLock readWriteLock2 = new MockedReadWriteLock("B", builder);
        Collection<String> c = new LinkedHashSet<String>();
        c.add("A");
        c.add("B");
        c.add("C");
        c = MACollections.locked(c, readWriteLock1);
        c = MACollections.locked(c, readWriteLock2);
        Iterator<String> iterator = c.iterator();
        readWriteLock1.setName("I");
        readWriteLock2.setName("II");
        try {
            MACollections.locking(
                    iterator, 
                    LockMode.WRITE, 
                    (Iterator<String> itr) -> {
                        while (itr.hasNext()) {
                            builder
                            .append('-')
                            .append(itr.next());
                            throw new RuntimeException();
                        }
                    });
            Assert.fail();
        } catch (RuntimeException ex) {
            
        }
        Assert.assertEquals("-LR:A-UR:A-LR:B-LR:A-UR:A-UR:B-LW:II-LW:I-A-UW:I-UW:II", builder.toString());
    }
    
    private static void assertUnmodifiableType(Object o, Class<?> expectedType) {
        Class<?> targetClass = o.getClass();
        Assert.assertTrue(targetClass.getSimpleName().startsWith("Unmodifiable"));
        Assert.assertSame(MACollections.class, targetClass.getDeclaringClass());
        for (Class<?> clazz : ALL_CLASSES) {
            if (clazz.isAssignableFrom(targetClass)) {
                Assert.assertSame(expectedType, clazz);
                return;
            }
        }
        Assert.fail();
    }
    
    @SuppressWarnings("unchecked")
    private static void assertLockedType(Object o, final Class<?> expectedType) {
        Class<?> targetClass = o.getClass();
        Assert.assertTrue(targetClass.getSimpleName().startsWith("Locked"));
        Assert.assertSame(MACollections.class, targetClass.getDeclaringClass());
        boolean matched = false;
        for (Class<?> clazz : ALL_CLASSES) {
            if (clazz.isAssignableFrom(targetClass)) {
                Assert.assertSame(expectedType, clazz);
                matched = true;
                break;
            }
        }
        Assert.assertTrue(matched);
        final Ref<Integer> matchRef = new Ref<Integer>(new Integer(0));
        if (o instanceof Map<?, ?>) {
            MACollections.locking(
                    (Map<Object, Object>)o, 
                    LockMode.READ, 
                    (Map<Object, Object> lockingObj) -> {
                        assertLockingType(lockingObj, expectedType);
                        matchRef.set(matchRef.get() + 1);
                    });
            MACollections.locking(
                    (Map<Object, Object>)o, 
                    LockMode.READ, 
                    (Map<Object, Object> lockingObj) -> {
                        assertLockingType(lockingObj, expectedType);
                        matchRef.set(matchRef.get() + 1);
                        return null;
                    });
        } else if (o instanceof Collection<?>) {
            MACollections.locking(
                    (Collection<Object>)o, 
                    LockMode.READ, 
                    (Collection<Object> lockingObj) -> {
                        assertLockingType(lockingObj, expectedType);
                        matchRef.set(matchRef.get() + 1);
                    });
            MACollections.locking(
                    (Collection<Object>)o, 
                    LockMode.READ, 
                    (Collection<Object> lockingObj) -> {
                        assertLockingType(lockingObj, expectedType);
                        matchRef.set(matchRef.get() + 1);
                        return null;
                    });
        } else if (o instanceof Iterator<?>) {
            MACollections.locking(
                    (Iterator<Object>)o, 
                    LockMode.READ, 
                    (Iterator<Object> lockingObj) -> {
                        assertLockingType(lockingObj, expectedType);
                        matchRef.set(matchRef.get() + 1);
                    }
            );
            MACollections.locking(
                    (Iterator<Object>)o, 
                    LockMode.READ, 
                    (Iterator<Object> lockingObj) -> {
                        assertLockingType(lockingObj, expectedType);
                        matchRef.set(matchRef.get() + 1);
                        return null;
                    });
        } else if (o instanceof Entry<?, ?>) {
            MACollections.locking(
                    (Entry<Object, Object>)o, 
                    LockMode.READ, 
                    (Entry<Object, Object> lockingObj) -> {
                        assertLockingType(lockingObj, expectedType);
                        matchRef.set(matchRef.get() + 1);
                    }
            );
            MACollections.locking(
                    (Entry<Object, Object>)o, 
                    LockMode.READ, 
                    (Entry<Object, Object> lockingObj) -> {
                        assertLockingType(lockingObj, expectedType);
                        matchRef.set(matchRef.get() + 1);
                        return null;
                    }
            );
        }
        Assert.assertEquals(2, matchRef.get().intValue());
    }
    
    private static void assertLockingType(Object o, Class<?> expectedType) {
        Class<?> targetClass = o.getClass();
        Assert.assertTrue(targetClass.getSimpleName().startsWith("Locking"));
        Assert.assertSame(MACollections.class, targetClass.getDeclaringClass());
        for (Class<?> clazz : ALL_CLASSES) {
            if (clazz.isAssignableFrom(targetClass)) {
                Assert.assertSame(expectedType, clazz);
                return;
            }
        }
        Assert.fail();
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T getInnerObject(T obj) {
        try {
            return (T)GET_INNER_OBJECT_METHOD.invoke(obj);
        } catch (IllegalAccessException ex) {
            throw UncheckedException.rethrow(ex);
        } catch (InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex);
        }
    }
    
    private static <T> T getRealObject(T obj) {
        if (!PROXY_CLASS.isAssignableFrom(obj.getClass())) {
            return (T)obj;
        }
        return getRealObject(getInnerObject(obj));
    }
    
    private static boolean isUsingInternalLock(Object o) {
        if (!LOCKED_PROXY_CLASS.isAssignableFrom(o.getClass())) {
            throw new IllegalArgumentException();
        }
        try {
            return USING_INTERNAL_LOCK_FIELD.getBoolean(o);
        } catch (IllegalAccessException ex) {
            throw UncheckedException.rethrow(ex);
        }
    }
    
    private static class MockedReadWriteLock implements ReadWriteLock {
        
        private String name;
        
        private StringBuilder builder;
        
        public MockedReadWriteLock(String name, StringBuilder builder) {
            this.name = name;
            this.builder = builder;
        }
        
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public Lock readLock() {
            
            return new Lock() {

                @Override
                public void lock() {
                    MockedReadWriteLock owner = MockedReadWriteLock.this;
                    owner.builder.append("-LR:" + owner.name);
                }

                @Override
                public void unlock() {
                    MockedReadWriteLock owner = MockedReadWriteLock.this;
                    owner.builder.append("-UR:" + owner.name);
                }

                @Override
                public void lockInterruptibly() throws InterruptedException {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean tryLock() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Condition newCondition() {
                    throw new UnsupportedOperationException();
                }
                
            };
        }

        @Override
        public Lock writeLock() {
            
            return new Lock() {

                @Override
                public void lock() {
                    MockedReadWriteLock owner = MockedReadWriteLock.this;
                    owner.builder.append("-LW:" + owner.name);
                }

                @Override
                public void unlock() {
                    MockedReadWriteLock owner = MockedReadWriteLock.this;
                    owner.builder.append("-UW:" + owner.name);
                }

                @Override
                public void lockInterruptibly() throws InterruptedException {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean tryLock() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Condition newCondition() {
                    throw new UnsupportedOperationException();
                }
                
            };
        }
        
    }
    
    static {
        ALL_CLASSES = new Class[] {
                MAOrderedMap.MAOrderedMapView.class,
                MAOrderedMap.class,
                MANavigableMap.MANavigableMapView.class,
                MANavigableMap.class,
                MASortedMap.MASortedMapView.class,
                MASortedMap.class,
                MAMap.class,
                XOrderedMap.XOrderedMapView.class,
                XOrderedMap.class,
                XNavigableMap.XNavigableMapView.class,
                XNavigableMap.class,
                XSortedMap.XSortedMapView.class,
                XSortedMap.class,
                XMap.class,
                NavigableMap.class,
                SortedMap.class,
                Map.class,
                MAList.MAListView.class,
                MAList.class,
                MAOrderedMap.MAOrderedKeySetView.class,
                MAOrderedSet.MAOrderedSetView.class,
                MAOrderedSet.class,
                MANavigableMap.MANavigableKeySetView.class,
                MANavigableSet.MANavigableSetView.class,
                MASortedMap.MASortedKeySetView.class,
                MANavigableSet.class,
                MASortedSet.MASortedSetView.class,
                MASortedSet.class,
                MAMap.MAEntrySetView.class,
                MAMap.MAKeySetView.class,
                MAMap.MAValuesView.class,
                MASet.class,
                MACollection.class,
                XList.XListView.class,
                XList.class,
                XOrderedMap.XOrderedKeySetView.class,
                XOrderedSet.XOrderedSetView.class,
                XOrderedSet.class,
                XNavigableMap.XNavigableKeySetView.class,
                XSortedMap.XSortedKeySetView.class,
                XNavigableSet.XNavigableSetView.class,
                XSortedSet.XSortedSetView.class,
                XNavigableSet.class,
                XSortedSet.class,
                XMap.XKeySetView.class,
                XMap.XValuesView.class,
                XMap.XEntrySetView.class,
                XSet.class,
                XCollection.class,
                List.class,
                NavigableSet.class,
                SortedSet.class,
                Set.class,
                Collection.class,
                MAMap.MAValuesView.MAValuesIterator.class,
                MAList.MAListIterator.class,
                MAMap.MAKeySetView.MAKeySetIterator.class,
                MAMap.MAEntrySetView.MAEntrySetIterator.class,
                MACollection.MAIterator.class,
                XList.XListIterator.class,
                XMap.XEntrySetView.XEntrySetIterator.class,
                XCollection.XIterator.class,
                ListIterator.class,
                Iterator.class,
                MAMap.MAEntry.class,
                XMap.XEntry.class,
                Map.Entry.class,
        };
        Class<?> proxyClass = null;
        Class<?> gatewayProxyClass = null;
        Class<?> lockedProxyClass = null;
        Method getInnerObjectMethod = null;
        Field usingInternalLockField = null;
        for (Class<?> clazz : MACollections.class.getDeclaredClasses()) {
            if (clazz.getSimpleName().equals("Proxy")) {
                proxyClass = clazz;
                try {
                    getInnerObjectMethod = clazz.getDeclaredMethod("getInnerObject");
                } catch (NoSuchMethodException ex) {
                    throw UncheckedException.rethrow(ex);
                }
            } else if (clazz.getSimpleName().equals("GatewayProxy")) {
                gatewayProxyClass = clazz;
            } else if (clazz.getSimpleName().equals("LockedProxy")) {
                lockedProxyClass = clazz;
                try {
                    usingInternalLockField = clazz.getDeclaredField("usingInternalLock");
                } catch (NoSuchFieldException ex) {
                    throw UncheckedException.rethrow(ex);
                }
            }
        }
        if (getInnerObjectMethod == null || gatewayProxyClass == null || usingInternalLockField == null) {
            throw new RuntimeException();
        }
        getInnerObjectMethod.setAccessible(true);
        usingInternalLockField.setAccessible(true);
        PROXY_CLASS = proxyClass;
        GATEWAY_PROXY_CLASS = gatewayProxyClass;
        LOCKED_PROXY_CLASS = lockedProxyClass;
        GET_INNER_OBJECT_METHOD = getInnerObjectMethod;
        USING_INTERNAL_LOCK_FIELD = usingInternalLockField;
    }
}
