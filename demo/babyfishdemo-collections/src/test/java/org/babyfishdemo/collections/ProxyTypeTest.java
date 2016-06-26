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
package org.babyfishdemo.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;

import junit.framework.Assert;

import org.babyfish.collection.FrozenComparator;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.HashCalculator;
import org.babyfish.collection.MACollection;
import org.babyfish.collection.MACollection.MAIterator;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAList;
import org.babyfish.collection.MAList.MAListIterator;
import org.babyfish.collection.MAList.MAListView;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MAMap.MAEntry;
import org.babyfish.collection.MAMap.MAEntrySetView;
import org.babyfish.collection.MAMap.MAEntrySetView.MAEntrySetIterator;
import org.babyfish.collection.MAMap.MAKeySetView;
import org.babyfish.collection.MAMap.MAKeySetView.MAKeySetIterator;
import org.babyfish.collection.MAMap.MAValuesView;
import org.babyfish.collection.MAMap.MAValuesView.MAValuesIterator;
import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.MANavigableMap.MANavigableKeySetView;
import org.babyfish.collection.MANavigableMap.MANavigableMapView;
import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.MANavigableSet.MANavigableSetView;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.MAOrderedMap.MAOrderedKeySetView;
import org.babyfish.collection.MAOrderedMap.MAOrderedMapView;
import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.MAOrderedSet.MAOrderedSetView;
import org.babyfish.collection.MASet;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XCollection.XIterator;
import org.babyfish.collection.XList;
import org.babyfish.collection.XList.XListIterator;
import org.babyfish.collection.XList.XListView;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XMap.XEntrySetView;
import org.babyfish.collection.XMap.XEntrySetView.XEntrySetIterator;
import org.babyfish.collection.XMap.XEntry;
import org.babyfish.collection.XMap.XKeySetView;
import org.babyfish.collection.XMap.XValuesView;
import org.babyfish.collection.XNavigableMap;
import org.babyfish.collection.XNavigableMap.XNavigableKeySetView;
import org.babyfish.collection.XNavigableMap.XNavigableMapView;
import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.XNavigableSet.XNavigableSetView;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedMap.XOrderedKeySetView;
import org.babyfish.collection.XOrderedMap.XOrderedMapView;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.collection.XOrderedSet.XOrderedSetView;
import org.babyfish.collection.XSet;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ProxyTypeTest {
    
    private static final Class<?>[] PRIMARY_COMPARATOR_TYPES;
    
    private static final Class<?>[] PRIMARY_COLLECTION_TYPES;
    
    private static final Class<?>[] PRIMARY_MAP_TYPES;
    
    private static final Class<?>[] PRIMARY_ITERATOR_TYPES;
    
    private static final Class<?>[] PRIMARY_ENTRY_TYPES;

    @Test
    public void testComparatorType() {
        
        Comparator<String> cmp, cmp2;
        
        cmp = new MockedComparator<>();
        cmp2 = MACollections.reverseOrder(cmp);
        Assert.assertNotSame(cmp, cmp2);
        Assert.assertSame(Comparator.class, getPrimaryType(cmp2));
        Assert.assertSame(cmp, MACollections.reverseOrder(cmp2));
        
        cmp = new MockedFrozenComparator<>();
        cmp2 = MACollections.reverseOrder(cmp);
        Assert.assertNotSame(cmp, cmp2);
        Assert.assertSame(FrozenComparator.class, getPrimaryType(cmp2));
        Assert.assertSame(cmp, MACollections.reverseOrder(cmp2));
        
        Assert.assertFalse(MACollections.reverseOrder(new MockedComparator<String>()) instanceof HashCalculator);
        Assert.assertFalse(MACollections.reverseOrder(new MockedFrozenComparator<String>()) instanceof HashCalculator);
        Assert.assertTrue(MACollections.reverseOrder(new MockedComparatorWithHash<String>()) instanceof HashCalculator);
        Assert.assertTrue(MACollections.reverseOrder(new MockedFrozenComparatorWithHash<String>()) instanceof HashCalculator);
    }
    
    @Test
    public void testCollectionType() {
        Collection<String> c, c2;
        Collection<Entry<String, String>> ec, ec2;
        
        // Collection, XValuesView, MAValuesView
        {
            c = new java.util.HashMap<String, String>().values();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(Collection.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.HashMap<String, String>().values();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(XValuesView.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.MAHashMap<String, String>().values();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(MAValuesView.class, getPrimaryType(c2));
        }
        
        // List, XList, XListView, MAList, MAListView
        {
            c = new java.util.ArrayList<>();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(List.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.ArrayList<>();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(XList.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.ArrayList<String>().subList(0, 0);
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(XListView.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.MAArrayList<>();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(MAList.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.MAArrayList<String>().subList(0, 0);
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(MAListView.class, getPrimaryType(c2));
        }
        
        // Set, XSet, XEntrySetView, XKeySetView, MASet, MAEntrySetView, MAKeySetView
        {
            c = new java.util.HashSet<>();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(Set.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.HashSet<>();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(XSet.class, getPrimaryType(c2));
            
            ec = new org.babyfish.collection.HashMap<String, String>().entrySet();
            ec2 = MACollections.locked(MACollections.unmodifiable(ec));
            Assert.assertNotSame(ec, ec2);
            Assert.assertSame(XEntrySetView.class, getPrimaryType(ec2));
            
            c = new org.babyfish.collection.HashMap<String, String>().keySet();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(XKeySetView.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.MAHashSet<>();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(MASet.class, getPrimaryType(c2));
            
            ec = new org.babyfish.collection.MAHashMap<String, String>().entrySet();
            ec2 = MACollections.locked(MACollections.unmodifiable(ec));
            Assert.assertNotSame(ec, ec2);
            Assert.assertSame(MAEntrySetView.class, getPrimaryType(ec2));
            
            c = new org.babyfish.collection.MAHashMap<String, String>().keySet();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(MAKeySetView.class, getPrimaryType(c2));
        }
        
        // XOrderedSet, XOrderSetView, XOrderedKeySetView, MAOrderedSet, MAOrderedSetView, MAOrderedKeySetView
        {
            c = new org.babyfish.collection.LinkedHashSet<>();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(XOrderedSet.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.LinkedHashSet<String>().descendingSet();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(XOrderedSetView.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.LinkedHashMap<String, String>().descendingKeySet();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(XOrderedKeySetView.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.LinkedHashSet<>();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(XOrderedSet.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.MALinkedHashSet<String>().descendingSet();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(MAOrderedSetView.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.MALinkedHashMap<String, String>().descendingKeySet();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(MAOrderedKeySetView.class, getPrimaryType(c2));
        }
        
        /*
         * NavigableSet, 
         * XNavigableSet, XNavigableSetView, XNavigableKeySetView, 
         * MANavigableSet, MANavigableSetView, MANavigableKeySetView
         */
        {
            c = new java.util.TreeSet<>();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(NavigableSet.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.TreeSet<>();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(XNavigableSet.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.TreeSet<String>().subSet("dog", "duck");
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(XNavigableSetView.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.TreeMap<String, String>().descendingKeySet();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(XNavigableKeySetView.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.MATreeSet<>();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(MANavigableSet.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.MATreeSet<String>().subSet("dog", "duck");
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(MANavigableSetView.class, getPrimaryType(c2));
            
            c = new org.babyfish.collection.MATreeMap<String, String>().descendingKeySet();
            c2 = MACollections.locked(MACollections.unmodifiable(c));
            Assert.assertNotSame(c, c2);
            Assert.assertSame(MANavigableKeySetView.class, getPrimaryType(c2));
        }
    }
    
    @Test
    public void testMapType() {
        Map<String, String> m, m2;
        
        // Map, XMap, MAMap
        {
            m = new java.util.HashMap<>();
            m2 = MACollections.locked(MACollections.unmodifiable(m));
            Assert.assertNotSame(m, m2);
            Assert.assertSame(Map.class, getPrimaryType(m2));
            
            m = new org.babyfish.collection.HashMap<>();
            m2 = MACollections.locked(MACollections.unmodifiable(m));
            Assert.assertNotSame(m, m2);
            Assert.assertSame(XMap.class, getPrimaryType(m2));
            
            m = new org.babyfish.collection.MAHashMap<>();
            m2 = MACollections.locked(MACollections.unmodifiable(m));
            Assert.assertNotSame(m, m2);
            Assert.assertSame(MAMap.class, getPrimaryType(m2));
        }
        
        // XOrderedMap, XOrderedMapView, XOrderedMap, MAOrderedMapView
        { 
            m = new org.babyfish.collection.LinkedHashMap<>();
            m2 = MACollections.locked(MACollections.unmodifiable(m));
            Assert.assertNotSame(m, m2);
            Assert.assertSame(XOrderedMap.class, getPrimaryType(m2));
            
            m = new org.babyfish.collection.LinkedHashMap<String, String>().descendingMap();
            m2 = MACollections.locked(MACollections.unmodifiable(m));
            Assert.assertNotSame(m, m2);
            Assert.assertSame(XOrderedMapView.class, getPrimaryType(m2));
            
            m = new org.babyfish.collection.MALinkedHashMap<>();
            m2 = MACollections.locked(MACollections.unmodifiable(m));
            Assert.assertNotSame(m, m2);
            Assert.assertSame(MAOrderedMap.class, getPrimaryType(m2));
            
            m = new org.babyfish.collection.MALinkedHashMap<String, String>().descendingMap();
            m2 = MACollections.locked(MACollections.unmodifiable(m));
            Assert.assertNotSame(m, m2);
            Assert.assertSame(MAOrderedMapView.class, getPrimaryType(m2));
        }
        
        // NavigableMap, XNavigableMap, XNavigableMapView, MANavigableMap, MANavigableMapView.
        { 
            m = new java.util.TreeMap<>();
            m2 = MACollections.locked(MACollections.unmodifiable(m));
            Assert.assertNotSame(m, m2);
            Assert.assertSame(NavigableMap.class, getPrimaryType(m2));
            
            m = new org.babyfish.collection.TreeMap<>();
            m2 = MACollections.locked(MACollections.unmodifiable(m));
            Assert.assertNotSame(m, m2);
            Assert.assertSame(XNavigableMap.class, getPrimaryType(m2));
            
            m = new org.babyfish.collection.TreeMap<String, String>().subMap("dog", "duck");
            m2 = MACollections.locked(MACollections.unmodifiable(m));
            Assert.assertNotSame(m, m2);
            Assert.assertSame(XNavigableMapView.class, getPrimaryType(m2));
            
            m = new org.babyfish.collection.MATreeMap<>();
            m2 = MACollections.locked(MACollections.unmodifiable(m));
            Assert.assertNotSame(m, m2);
            Assert.assertSame(MANavigableMap.class, getPrimaryType(m2));
            
            m = new org.babyfish.collection.MATreeMap<String, String>().subMap("dog", "duck");
            m2 = MACollections.locked(MACollections.unmodifiable(m));
            Assert.assertNotSame(m, m2);
            Assert.assertSame(MANavigableMapView.class, getPrimaryType(m2));
        }
    }
    
    @Test
    public void testIteratorType() {
        
        Iterator<String> i, i2;
        XEntrySetIterator<String, String> ei, ei2;
        
        // Iterator, XIterator, MAIterator
        {
            i = new java.util.HashSet<String>().iterator();
            i2 = MACollections.locked(MACollections.unmodifiable(i));
            Assert.assertNotSame(i, i2);
            Assert.assertSame(Iterator.class, getPrimaryType(i2));
            
            i = new org.babyfish.collection.HashSet<String>().iterator();
            i2 = MACollections.locked(MACollections.unmodifiable(i));
            Assert.assertNotSame(i, i2);
            Assert.assertSame(XIterator.class, getPrimaryType(i2));
            
            i = new org.babyfish.collection.MAHashSet<String>().iterator();
            i2 = MACollections.locked(MACollections.unmodifiable(i));
            Assert.assertNotSame(i, i2);
            Assert.assertSame(MAIterator.class, getPrimaryType(i2));
        }
        
        // ListIterator, XListerator, MAListIterator
        {
            i = new java.util.ArrayList<String>().listIterator();
            i2 = MACollections.locked(MACollections.unmodifiable(i));
            Assert.assertNotSame(i, i2);
            Assert.assertSame(ListIterator.class, getPrimaryType(i2));
            
            i = new org.babyfish.collection.ArrayList<String>().listIterator();
            i2 = MACollections.locked(MACollections.unmodifiable(i));
            Assert.assertNotSame(i, i2);
            Assert.assertSame(XListIterator.class, getPrimaryType(i2));
            
            i = new org.babyfish.collection.MAArrayList<String>().listIterator();
            i2 = MACollections.locked(MACollections.unmodifiable(i));
            Assert.assertNotSame(i, i2);
            Assert.assertSame(MAListIterator.class, getPrimaryType(i2));
        }
        
        // XEntrySetIterator, MAEntrySetIterator
        {
            ei = new org.babyfish.collection.HashMap<String, String>().entrySet().iterator();
            ei2 = MACollections.locked(MACollections.unmodifiable(ei));
            Assert.assertNotSame(ei, ei2);
            Assert.assertSame(XEntrySetIterator.class, getPrimaryType(ei2));
            
            ei = new org.babyfish.collection.MAHashMap<String, String>().entrySet().iterator();
            ei2 = MACollections.locked(MACollections.unmodifiable(ei));
            Assert.assertNotSame(ei, ei2);
            Assert.assertSame(MAEntrySetIterator.class, getPrimaryType(ei2));
        }
        
        // MAKeySetIterator
        {
            i = new org.babyfish.collection.MAHashMap<String, String>().keySet().iterator();
            i2 = MACollections.locked(MACollections.unmodifiable(i));
            Assert.assertNotSame(i, i2);
            Assert.assertSame(MAKeySetIterator.class, getPrimaryType(i2));
        }
        
        // MAValuesIterator
        {
            i = new org.babyfish.collection.MAHashMap<String, String>().values().iterator();
            i2 = MACollections.locked(MACollections.unmodifiable(i));
            Assert.assertNotSame(i, i2);
            Assert.assertSame(MAValuesIterator.class, getPrimaryType(i2));
        }
    }
    
    @Test
    public void testEntryType() {
        Map<String, String> m;
        Entry<String, String> e, e2;
        
        // Entry
        {
            m = new java.util.HashMap<>();
            m.put("I", "Aplpha");
            e = m.entrySet().iterator().next();
            e2 = MACollections.locked(MACollections.unmodifiable(e));
            Assert.assertNotSame(e, e2);
            Assert.assertSame(Entry.class, getPrimaryType(e2));
        }
        
        // XEntry
        {
            m = new org.babyfish.collection.HashMap<>();
            m.put("I", "Aplpha");
            e = m.entrySet().iterator().next();
            e2 = MACollections.locked(MACollections.unmodifiable(e));
            Assert.assertNotSame(e, e2);
            Assert.assertSame(XEntry.class, getPrimaryType(e2));
        }
        
        // MAEntry
        {
            m = new org.babyfish.collection.MAHashMap<>();
            m.put("I", "Aplpha");
            e = m.entrySet().iterator().next();
            e2 = MACollections.locked(MACollections.unmodifiable(e));
            Assert.assertNotSame(e, e2);
            Assert.assertSame(MAEntry.class, getPrimaryType(e2));
        }
    }
    
    private static Class<?> getPrimaryType(Comparator<?> c) {
        Class<?> type = c.getClass();
        for (Class<?> clazz : PRIMARY_COMPARATOR_TYPES) {
            if (clazz.isAssignableFrom(type)) {
                return clazz;
            }
        }
        throw new AssertionError("Impossible");
    }
    
    private static Class<?> getPrimaryType(Collection<?> c) {
        Class<?> type = c.getClass();
        for (Class<?> clazz : PRIMARY_COLLECTION_TYPES) {
            if (clazz.isAssignableFrom(type)) {
                return clazz;
            }
        }
        throw new AssertionError("Impossible");
    }
    
    private static Class<?> getPrimaryType(Map<?, ?> m) {
        Class<?> type = m.getClass();
        for (Class<?> clazz : PRIMARY_MAP_TYPES) {
            if (clazz.isAssignableFrom(type)) {
                return clazz;
            }
        }
        throw new AssertionError("Impossible");
    }
    
    private static Class<?> getPrimaryType(Iterator<?> i) {
        Class<?> type = i.getClass();
        for (Class<?> clazz : PRIMARY_ITERATOR_TYPES) {
            if (clazz.isAssignableFrom(type)) {
                return clazz;
            }
        }
        throw new AssertionError("Impossible");
    }
    
    private static Class<?> getPrimaryType(Entry<?, ?> e) {
        Class<?> type = e.getClass();
        for (Class<?> clazz : PRIMARY_ENTRY_TYPES) {
            if (clazz.isAssignableFrom(type)) {
                return clazz;
            }
        }
        throw new AssertionError("Impossible");
    }
    
    private static class MockedComparator<T> implements Comparator<T> {

        @Override
        public int compare(T o1, T o2) {
            throw new UnsupportedOperationException("Mocked, not real");
        }
    }
    
    private static class MockedComparatorWithHash<T> extends MockedComparator<T> implements HashCalculator<T> {

        @Override
        public int hashCode(T o) {
            throw new UnsupportedOperationException("Mocked, not real");
        }
    }
    
    private static class MockedFrozenComparator<T> extends MockedComparator<T> implements FrozenComparator<T> {

        @Override
        public void freeze(T obj, FrozenContext<T> ctx) {
            throw new UnsupportedOperationException("Mocked, not real");
        }

        @Override
        public void unfreeze(T obj, FrozenContext<T> ctx) {
            throw new UnsupportedOperationException("Mocked, not real");
        }
    }
    
    private static class MockedFrozenComparatorWithHash<T> extends MockedFrozenComparator<T> implements HashCalculator<T> {
        
        @Override
        public int hashCode(T o) {
            throw new UnsupportedOperationException("Mocked, not real");
        }
    }
    
    static {
        PRIMARY_COMPARATOR_TYPES = new Class[] {
                FrozenComparator.class,
                Comparator.class
        };
        PRIMARY_COLLECTION_TYPES = new Class[] {
                
                MANavigableKeySetView.class,
                MANavigableSetView.class,
                MANavigableSet.class,
                
                MAOrderedKeySetView.class,
                MAOrderedSetView.class,
                MAOrderedSet.class,
                
                MAEntrySetView.class,
                MAKeySetView.class,
                MASet.class,
                
                MAListView.class,
                MAList.class,
                
                MAValuesView.class,
                MACollection.class,
                
                XNavigableKeySetView.class,
                XNavigableSetView.class,
                XNavigableSet.class,
                
                XOrderedKeySetView.class,
                XOrderedSetView.class,
                XOrderedSet.class,
                
                XEntrySetView.class,
                XKeySetView.class,
                XSet.class,
                
                XListView.class,
                XList.class,
                
                XValuesView.class,
                XCollection.class,
                
                NavigableSet.class,
                Set.class,
                List.class,
                Collection.class,
        };
        PRIMARY_MAP_TYPES = new Class<?>[] {
                
                MANavigableMapView.class,
                MANavigableMap.class,
                MAOrderedMapView.class,
                MAOrderedMap.class,
                MAMap.class,
                
                XNavigableMapView.class,
                XNavigableMap.class,
                XOrderedMapView.class,
                XOrderedMap.class,
                XMap.class,
                
                NavigableMap.class,
                Map.class,
        };
        PRIMARY_ITERATOR_TYPES = new Class[] {
                
                MAEntrySetIterator.class,
                MAKeySetIterator.class,
                MAValuesIterator.class,
                
                MAListIterator.class,
                MAIterator.class,
                
                XEntrySetIterator.class,
                
                XListIterator.class,
                XIterator.class,
                
                ListIterator.class,
                Iterator.class
        };
        PRIMARY_ENTRY_TYPES = new Class[] {
                MAEntry.class,
                XEntry.class,
                Entry.class
        };
    }
}
