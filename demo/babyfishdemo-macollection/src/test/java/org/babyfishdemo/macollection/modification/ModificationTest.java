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
package org.babyfishdemo.macollection.modification;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MACollection;
import org.babyfish.collection.MACollection.MAIterator;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAHashMap;
import org.babyfish.collection.MAHashSet;
import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MAList;
import org.babyfish.collection.MAList.MAListIterator;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MAMap.MAEntry;
import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementEvent.MapModification;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.collection.event.modification.CollectionModifications;
import org.babyfish.collection.event.modification.CollectionModifications.AddAllByCollection;
import org.babyfish.collection.event.modification.CollectionModifications.AddByElement;
import org.babyfish.collection.event.modification.CollectionModifications.RemoveAllByCollection;
import org.babyfish.collection.event.modification.CollectionModifications.RemoveByElement;
import org.babyfish.collection.event.modification.CollectionModifications.RetainAllByCollection;
import org.babyfish.collection.event.modification.EntryModifications.SetByValue;
import org.babyfish.collection.event.modification.IteratorModifications;
import org.babyfish.collection.event.modification.ListIteratorModifications;
import org.babyfish.collection.event.modification.ListModifications.AddAllByIndexAndCollection;
import org.babyfish.collection.event.modification.ListModifications.AddByIndexAndElement;
import org.babyfish.collection.event.modification.ListModifications.RemoveByIndex;
import org.babyfish.collection.event.modification.ListModifications.SetByIndexAndElement;
import org.babyfish.collection.event.modification.MapModifications;
import org.babyfish.collection.event.modification.MapModifications.PutAllByMap;
import org.babyfish.collection.event.modification.MapModifications.PutByKeyAndValue;
import org.babyfish.collection.event.modification.MapModifications.RemoveByKey;
import org.babyfish.collection.event.modification.NavigableMapModifications;
import org.babyfish.collection.event.modification.NavigableSetModifications;
import org.babyfish.collection.event.modification.OrderedMapModifications;
import org.babyfish.collection.event.modification.OrderedSetModifications;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ModificationTest {
    
    @Test
    public void testCollectionModifications() {
        
        MACollection<String> collection = new MAHashSet<>();
        ElementListenerImpl<String> eli = new ElementListenerImpl<>();
        collection.addElementListener(eli);
        
        String e = "A";
        Collection<String> c = MACollections.wrap("B");
        
        {
            collection.add(e);
            Assert.assertTrue(eli.getModification() instanceof AddByElement<?>);
            AddByElement<String> addByElement = (AddByElement<String>)eli.getModification();
            Assert.assertSame(e, addByElement.getElement());
        }
        
        {
            collection.addAll(c);
            Assert.assertTrue(eli.getModification() instanceof AddAllByCollection<?>);
            AddAllByCollection<String> addAllByCollection = (AddAllByCollection<String>)eli.getModification();
            Assert.assertSame(c, addAllByCollection.getCollection());
        }
        
        {
            collection.remove(e);
            Assert.assertTrue(eli.getModification() instanceof RemoveByElement<?>);
            RemoveByElement<String> removeByElement = (RemoveByElement<String>)eli.getModification();
            Assert.assertSame(e, removeByElement.getElement());
        }
        
        {
            collection.removeAll(c);
            Assert.assertTrue(eli.getModification() instanceof RemoveAllByCollection<?>);
            RemoveAllByCollection<String> removeAllByCollection = (RemoveAllByCollection<String>)eli.getModification();
            Assert.assertSame(c, removeAllByCollection.getCollection());
        }
        
        collection.add("X");
        
        {
            collection.retainAll(c);
            Assert.assertTrue(eli.getModification() instanceof RetainAllByCollection<?>);
            RetainAllByCollection<String> retainAllByCollection = (RetainAllByCollection<String>)eli.getModification();
            Assert.assertSame(c, retainAllByCollection.getCollection());
        }
        
        collection.add("X");
        
        {
            collection.clear();
            Assert.assertTrue(eli.getModification() instanceof CollectionModifications.Clear<?>);
        }
    }
    
    @Test
    public void testOrderedSetModifications() {
        MAOrderedSet<String> set = new MALinkedHashSet<>();
        set.addAll(MACollections.wrap("First", "Last"));
        ElementListenerImpl<String> eli = new ElementListenerImpl<>();
        set.addElementListener(eli);
        
        {
            set.pollFirst();
            Assert.assertTrue(eli.getModification() instanceof OrderedSetModifications.PollFirst<?>);
        }
        
        {
            set.pollLast();
            Assert.assertTrue(eli.getModification() instanceof OrderedSetModifications.PollLast<?>);
        }
    }
    
    @Test
    public void testNavigableSetModifications() {
        MANavigableSet<String> set = new MATreeSet<>();
        set.addAll(MACollections.wrap("First", "Last"));
        ElementListenerImpl<String> eli = new ElementListenerImpl<>();
        set.addElementListener(eli);
        
        {
            set.pollFirst();
            Assert.assertTrue(eli.getModification() instanceof NavigableSetModifications.PollFirst<?>);
        }
        
        {
            set.pollLast();
            Assert.assertTrue(eli.getModification() instanceof NavigableSetModifications.PollLast<?>);
        }
    }
    
    @Test
    public void testListModifications() {
        MAList<String> list = new MAArrayList<>();
        ElementListenerImpl<String> eli = new ElementListenerImpl<>();
        list.addElementListener(eli);
        
        String e = "A";
        Collection<String> c = MACollections.wrap("B");

        {
            list.add(0, e);
            Assert.assertTrue(eli.getModification() instanceof AddByIndexAndElement<?>);
            AddByIndexAndElement<String> addByIndexAndElement = (AddByIndexAndElement<String>)eli.getModification();
            Assert.assertEquals(0, addByIndexAndElement.getIndex());
            Assert.assertSame(e, addByIndexAndElement.getElement());
        }
        
        {
            list.addAll(0, c);
            Assert.assertTrue(eli.getModification() instanceof AddAllByIndexAndCollection<?>);
            AddAllByIndexAndCollection<String> addByIndexAndElement = (AddAllByIndexAndCollection<String>)eli.getModification();
            Assert.assertEquals(0, addByIndexAndElement.getIndex());
            Assert.assertSame(c, addByIndexAndElement.getCollection());
        }
        
        {
            list.set(1, e);
            Assert.assertTrue(eli.getModification() instanceof SetByIndexAndElement<?>);
            SetByIndexAndElement<String> setByIndexAndElement = (SetByIndexAndElement<String>)eli.getModification();
            Assert.assertEquals(1, setByIndexAndElement.getIndex());
            Assert.assertSame(e, setByIndexAndElement.getElement());
        }
        
        {
            list.remove(1);
            Assert.assertTrue(eli.getModification() instanceof RemoveByIndex<?>);
            RemoveByIndex<String> removeByIndex = (RemoveByIndex<String>)eli.getModification();
            Assert.assertEquals(1, removeByIndex.getIndex());
        }
    }
    
    @Test
    public void testMapModification() {
        
        MAMap<String, String> map = new MAHashMap<>();
        MapElementListenerImpl<String, String> meli = new MapElementListenerImpl<>();
        map.addMapElementListener(meli);
        
        String key = "one";
        String value = "I";
        Map<String, String> m = Collections.singletonMap("two", "II");
        
        {
            map.put(key, value);
            Assert.assertTrue(meli.getModification() instanceof PutByKeyAndValue<?, ?>);
            PutByKeyAndValue<String, String> putByKeyAndValue = (PutByKeyAndValue<String, String>)meli.getModification();
            Assert.assertSame(key, putByKeyAndValue.getKey());
            Assert.assertSame(value, putByKeyAndValue.getValue());
        }
        
        {
            map.putAll(m);
            Assert.assertTrue(meli.getModification() instanceof PutAllByMap<?, ?>);
            PutAllByMap<String, String> putAllByMap = (PutAllByMap<String, String>)meli.getModification();
            Assert.assertSame(m, putAllByMap.getMap());
        }
        
        {
            map.remove(key);
            Assert.assertTrue(meli.getModification() instanceof RemoveByKey<?, ?>);
            RemoveByKey<String, String> removeByKey = (RemoveByKey<String, String>)meli.getModification();
            Assert.assertSame(key, removeByKey.getKey());
        }
        
        {
            map.clear();
            Assert.assertTrue(meli.getModification() instanceof MapModifications.Clear<?, ?>);
        }
    }
    
    @Test
    public void testOrderedMapModifications() {
        MAOrderedMap<String, String> map = new MALinkedHashMap<>();
        map.put("one", "I");
        map.put("two", "II");
        MapElementListenerImpl<String, String> eli = new MapElementListenerImpl<>();
        map.addMapElementListener(eli);
        
        {
            map.pollFirstEntry();
            Assert.assertTrue(eli.getModification() instanceof OrderedMapModifications.PollFirstEntry<?, ?>);
        }
        
        {
            map.pollLastEntry();
            Assert.assertTrue(eli.getModification() instanceof OrderedMapModifications.PollLastEntry<?, ?>);
        }
    }
    
    @Test
    public void testNavigableMapModifications() {
        MANavigableMap<String, String> map = new MATreeMap<>();
        map.put("one", "I");
        map.put("two", "II");
        MapElementListenerImpl<String, String> eli = new MapElementListenerImpl<>();
        map.addMapElementListener(eli);
        
        {
            map.pollFirstEntry();
            Assert.assertTrue(eli.getModification() instanceof NavigableMapModifications.PollFirstEntry<?, ?>);
        }
        
        {
            map.pollLastEntry();
            Assert.assertTrue(eli.getModification() instanceof NavigableMapModifications.PollLastEntry<?, ?>);
        }
    }
    
    @Test
    public void testEntryModifications() {
        MAMap<String, String> map = new MAHashMap<>();
        map.put("one", "I");
        MAEntry<String, String> entry = map.entrySet().iterator().next();
        ElementListenerImpl<String> eli = new ElementListenerImpl<>();
        entry.addElementListener(eli);
        
        String newValue = "Alpha";
        entry.setValue(newValue);
        
        Assert.assertTrue(eli.getModification() instanceof SetByValue<?>);
        SetByValue<String> setByValue = (SetByValue<String>)eli.getModification();
        Assert.assertSame(newValue, setByValue.getValue());
    }
    
    @Test
    public void testIteratorModifications() {
        MACollection<String> collection = new MAHashSet<>();
        collection.add("X");
        MAIterator<String> iterator = collection.iterator();
        ElementListenerImpl<String> eli = new ElementListenerImpl<>();
        iterator.addElementListener(eli);
        
        iterator.next();
        iterator.remove();
        
        Assert.assertTrue(eli.getModification() instanceof IteratorModifications.Remove<?>);
    }
    
    @Test
    public void testListIteratorModifications() {
        MAList<String> list = new MAArrayList<>();
        MAListIterator<String> iterator = list.iterator();
        ElementListenerImpl<String> eli = new ElementListenerImpl<>();
        iterator.addElementListener(eli);
        
        String element = "X";
        String newElement = "Y";
        
        {
            iterator.add(element);
            Assert.assertTrue(eli.getModification() instanceof ListIteratorModifications.AddByElement<?>);
            ListIteratorModifications.AddByElement<String> addByElement = 
                    (ListIteratorModifications.AddByElement<String>)eli.getModification();
            Assert.assertSame(element, addByElement.getElement());
        }
        
        {
            iterator.previous();
            iterator.next();
            iterator.set(newElement);
            Assert.assertTrue(eli.getModification() instanceof ListIteratorModifications.SetByElement<?>);
            ListIteratorModifications.SetByElement<String> setByElement = 
                    (ListIteratorModifications.SetByElement<String>)eli.getModification();
            Assert.assertSame(newElement, setByElement.getElement());
        }
    }
    
    private static class ElementListenerImpl<E> implements ElementListener<E> {
        
        private Modification<E> modification;

        @Override
        public void modified(ElementEvent<E> e) throws Throwable {
            this.modification = e.getModification();
        }
        
        public Modification<E> getModification() {
            return this.modification;
        }
    }
    
    private static class MapElementListenerImpl<K, V> implements MapElementListener<K, V> {

        private MapModification<K, V> modification;
        
        @Override
        public void modified(MapElementEvent<K, V> e) throws Throwable {
            this.modification = e.getModification();
        }
        
        public MapModification<K, V> getModification() {
            return this.modification;
        }
    }
}
