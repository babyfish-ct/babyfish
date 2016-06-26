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
package org.babyfishdemo.macollection.bubble;

import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MACollection.MAIterator;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAList;
import org.babyfish.collection.MAList.MAListIterator;
import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.event.KeySetElementEvent;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.event.ListElementListener;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.data.event.ModificationEvent;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Assert;
import org.junit.Test;

/*
 * Before learn this test, you must learn
 * org.babyfishdemo.macollection.basic.*
 * and
 * org.babyfishdemo.macollection.modification.*
 * at first
 */
/**
 * @author Tao Chen
 */
public class SimpleBubbleEventTest {

    /*
     * In java collection framework, collection can create views, 
     * view can create sub-views, sub-view can create more deep sub-views.
     * 
     * You can consider all the views of a collection as a tree which 
     * uses the original collection to be the root. 
     * (1) This tree has unlimited depth 
     * (2) You can use any node of this tree to change the data of original collection.
     * (3) All the data change change be observed by root node of this tree.
     * 
     * In java collection framework, these methods can be used to create views.
     * 
     * (1) java.util.Collection<E>.iterator()
     * (2) java.util.List<E>.subList(int, int)
     * (3) java.util.List<E>.listIterator()
     * (4) java.util.List<E>.listIterator(int)
     * (5) java.util.Map<K, V>.entrySet()
     * (6) java.util.Map<K, V>.keySet()
     * (7) java.util.Map<K, V>.values()
     * (8) java.util.SortedSet<K, V>.headSet(E)
     * (9) java.util.NavigableSet<E>.headSet(E, boolean)
     * (10) java.util.SortedSet<E>.tailSet(E)
     * (11) java.util.NavigableSet<E>.tailSet(E, boolean)
     * (12) java.util.SortedSet<E>.subSet(E, E)
     * (13) java.util.NavigableSet<E>.subSet(E, boolean, E, boolean)
     * (14) java.util.NavigableSet<E>.descendingSet()
     * (15) java.util.NavigableSet<E>.descendingIterator()
     * (16) java.util.SortedMap<K, V>.headMap(K)
     * (17) java.util.NavigableMap<K, V>.headMap(K, boolean)
     * (18) java.util.SortedMap<K, V>.tailMap(K)
     * (19) java.util.NavigableMap<K, V>.tailMap(K, boolean)
     * (20) java.util.SortedMap<K, V>.subMap(K, K)
     * (21) java.util.NavigableMap<K, V>.subMap(K, boolean, K, boolean)
     * (22) java.util.NavigableMap<K, V>.descendingMap()
     * (23) java.util.NavigableMap<K, V>.navigableKeySet()
     * (24) java.util.NavigableMap<K, V>.descendingKeySet()
     * (25) java.util.NavigableMap<K, V>.firstEntry()
     * (26) java.util.NavigableMap<K, V>.lastEntry()
     * (27) java.util.NavigableMap<K, V>.lowerEntry(K)
     * (28) java.util.NavigableMap<K, V>.higherEntry(K)
     * (29) java.util.NavigableMap<K, V>.floorEntry()
     * (30) java.util.NavigableMap<K, V>.ceilingEntry()
     * 
     * BabyFish Collection Framework added these methods that can create views
     * (31) org.babyfish.collection.XOrderedSet<E>.descendingSet()
     * (32) org.babyfish.collection.XOrderedSet<E>.descendingIterator()
     * (33) org.babyfish.collection.XOrderedMap<K, V>.descendingMap()
     * (34) org.babyfish.collection.XOrderedMap<K, V>.descendingKeySet()
     * (35) org.babyfish.collection.XOrderedMap<K, V>.firstEntry()
     * (36) org.babyfish.collection.XOrderedMap<K, V>.lastEntry()
     * 
     * Specially, the instance of java.util.Map.Entry<K, V> returned by 
     * java.util.Map<K, V>.entrySet().iterator() is implicit view too
     * because its method "setValue(V)" can change the original map.
     * 
     * BabyFish collection framework is very powerful, because if you change
     * the data of a view, the view will report an event, then this event
     * will be bubbled to the parent view and reported by parent view, then
     * this event will be bubbled to the parent of parent... Finally, this
     * event will be bubbled to the root(original collection) and reported 
     * by the root. If you handle the data change event of the root original collection,
     * you can feel all the data change on any node of this tree with unlimited depth
     * 
     * It is impossible to show all the view-branches the unlimited view tree 
     * in one test class, so we only show 2 cases
     */
    
    
    
    
    
    /*
     * (1) <root> : NavigableMap<K, V>
     *     |
     *     \--->descendingMap() : NavigableMap<K, V>
     *          |
     *          \--->subMap(K, boolean, K, boolean) : NavigableMap<K, V>
     *               |
     *               \--->descendingKeySet() : NavigableKeySet<E>
     *                    |
     *                    \--->headSet(K, boolean) : NavigableSet<E>
     *                         |
     *                         \--->descendingIterator() : Iterator<E>
     * 
     * Then we use the iterator to delete one element, the data change
     * event will be triggered by the iterator, then this event will be
     * bubbled step by step, finally, the event will be triggered by
     * the root NavigableMap.
     * 
     * In this test case, the type of event and bubble event are different.
     * (1) For the events triggered by descendingIterator, headSet and descedingKeySet,
     *      java class is "KeySetElementEvent<K, V>"
     * (2) For the events triggered by subMap, descendingMap and root NavigableMap,
     *      java class is "MapElementEvent<K, V>"
     */
    @Test
    public void testBubbleEventWithEventTypeDifference() {
        
        MANavigableMap<String, String> map = new MATreeMap<>();
        map.put("A", "Alpha");
        map.put("B", "Beta");
        map.put("C", "Gamma");
        map.put("D", "Delta");
        
        MANavigableMap<String, String> descendingMap = map.descendingMap();
        MANavigableMap<String, String> subMap = descendingMap.subMap("C", true, "B", true);
        MANavigableSet<String> descendingKeySet = subMap.descendingKeySet();
        MANavigableSet<String> headSet = descendingKeySet.headSet("C", false);
        MAIterator<String> descendingIterator = headSet.descendingIterator();
        
        MapBubbleRecorder<String, String>
            mapRecorder = new MapBubbleRecorder<>(),
            descendingMapRecorder = new MapBubbleRecorder<>(),
            subMapRecorder = new MapBubbleRecorder<>(),
            descendingKeySetRecorder = new MapBubbleRecorder<>(),
            headSetRecorder = new MapBubbleRecorder<>(),
            descendingIteratorRecorder = new MapBubbleRecorder<>();
        
        map.addMapElementListener(mapRecorder.mapElementListener());
        descendingMap.addMapElementListener(descendingMapRecorder.mapElementListener());
        subMap.addMapElementListener(subMapRecorder.mapElementListener());
        descendingKeySet.addElementListener(descendingKeySetRecorder.keyElementListener());
        headSet.addElementListener(headSetRecorder.keyElementListener());
        descendingIterator.addElementListener(descendingIteratorRecorder.keyElementListener());
        
        descendingIterator.next();
        descendingIterator.remove();
        
        /*
         * (1) If the event is not bubbled event: e.getModification() != null, but e.getCause() == null
         * (2) If the event is bubbled event: e.getModification() == null, but e.getCause() != null
         */
        Assert.assertEquals(
                "KeySetElementEvent {"
                +   "detachedElement: B,"
                +   "value: Beta,"
                +   "modification: org.babyfish.collection.event.modification.IteratorModifications$Remove{},"
                +   "cause: null"
                + "}",
                descendingIteratorRecorder.toString()
        );
        Assert.assertEquals(
                "KeySetElementEvent {"
                +   "detachedElement: B,"
                +   "value: Beta,"
                +   "modification: null,"
                +   "cause: {"
                +     "viewInfo: org.babyfish.collection.viewinfo.NavigableSetViewInfos$DescendingIterator{},"
                +     "event: KeySetElementEvent {"
                +       "detachedElement: B,"
                +       "value: Beta,"
                +       "modification: org.babyfish.collection.event.modification.IteratorModifications$Remove{},"
                +       "cause: null"
                +     "}"
                +   "}"
                + "}",
                headSetRecorder.toString()
        );
        Assert.assertEquals(
                "KeySetElementEvent {"
                +   "detachedElement: B,"
                +   "value: Beta,"
                +   "modification: null,"
                +   "cause: {"
                +     "viewInfo: org.babyfish.collection.viewinfo.NavigableSetViewInfos$HeadSet{ "
                +       "toElement: C, "
                +       "inclusive: false "
                +     "},"
                +     "event: KeySetElementEvent {"
                +       "detachedElement: B,"
                +       "value: Beta,"
                +       "modification: null,"
                +       "cause: {"
                +         "viewInfo: org.babyfish.collection.viewinfo.NavigableSetViewInfos$DescendingIterator{},"
                +         "event: KeySetElementEvent {"
                +           "detachedElement: B,"
                +           "value: Beta,"
                +           "modification: org.babyfish.collection.event.modification.IteratorModifications$Remove{},"
                +           "cause: null"
                +         "}"
                +       "}"
                +     "}"
                +   "}"
                + "}",
                descendingKeySetRecorder.toString()
        );
        Assert.assertEquals(
                "MapElementEvent {"
                +   "detachedKey: B,"
                +   "detachedValue: Beta,"
                +   "modification: null,"
                +   "cause: {"
                +     "viewInfo: org.babyfish.collection.viewinfo.NavigableMapViewInfos$DescendingKeySet{},"
                +     "event: KeySetElementEvent {"
                +       "detachedElement: B,"
                +       "value: Beta,"
                +       "modification: null,"
                +       "cause: {"
                +         "viewInfo: org.babyfish.collection.viewinfo.NavigableSetViewInfos$HeadSet{ "
                +           "toElement: C, "
                +           "inclusive: false "
                +         "},"
                +         "event: KeySetElementEvent {"
                +           "detachedElement: B,"
                +           "value: Beta,"
                +           "modification: null,"
                +           "cause: {"
                +             "viewInfo: org.babyfish.collection.viewinfo.NavigableSetViewInfos$DescendingIterator{},"
                +             "event: KeySetElementEvent {"
                +               "detachedElement: B,"
                +               "value: Beta,"
                +               "modification: org.babyfish.collection.event.modification.IteratorModifications$Remove{},"
                +               "cause: null"
                +             "}"
                +           "}"
                +         "}"
                +       "}"
                +     "}"
                +   "}"
                + "}",
                subMapRecorder.toString()
        );
        Assert.assertEquals(
                "MapElementEvent {"
                +   "detachedKey: B,"
                +   "detachedValue: Beta,"
                +   "modification: null,"
                +   "cause: {"
                +     "viewInfo: org.babyfish.collection.viewinfo.NavigableMapViewInfos$SubMap{ "
                +       "fromKey: C, "
                +       "fromInclusive: true, "
                +       "toKey: B, "
                +       "toInclusive: true "
                +     "},"
                +     "event: MapElementEvent {"
                +       "detachedKey: B,"
                +       "detachedValue: Beta,"
                +       "modification: null,"
                +       "cause: {"
                +         "viewInfo: org.babyfish.collection.viewinfo.NavigableMapViewInfos$DescendingKeySet{},"
                +         "event: KeySetElementEvent {"
                +           "detachedElement: B,"
                +           "value: Beta,"
                +           "modification: null,"
                +           "cause: {"
                +             "viewInfo: org.babyfish.collection.viewinfo.NavigableSetViewInfos$HeadSet{ "
                +               "toElement: C, "
                +               "inclusive: false "
                +             "},"
                +             "event: KeySetElementEvent {"
                +               "detachedElement: B,"
                +               "value: Beta,"
                +               "modification: null,"
                +               "cause: {"
                +                 "viewInfo: org.babyfish.collection.viewinfo.NavigableSetViewInfos$DescendingIterator{},"
                +                 "event: KeySetElementEvent {"
                +                   "detachedElement: B,"
                +                   "value: Beta,"
                +                   "modification: org.babyfish.collection.event.modification.IteratorModifications$Remove{},"
                +                   "cause: null"
                +                 "}"
                +               "}"
                +             "}"
                +           "}"
                +         "}"
                +       "}"
                +     "}"
                +   "}"
                + "}",
                descendingMapRecorder.toString()
        );
        Assert.assertEquals(
                "MapElementEvent {"
                +   "detachedKey: B,"
                +   "detachedValue: Beta,"
                +   "modification: null,"
                +   "cause: {"
                +     "viewInfo: org.babyfish.collection.viewinfo.NavigableMapViewInfos$DescendingMap{},"
                +     "event: MapElementEvent {"
                +       "detachedKey: B,"
                +       "detachedValue: Beta,"
                +       "modification: null,"
                +       "cause: {"
                +         "viewInfo: org.babyfish.collection.viewinfo.NavigableMapViewInfos$SubMap{ "
                +           "fromKey: C, "
                +           "fromInclusive: true, "
                +           "toKey: B, "
                +           "toInclusive: true "
                +         "},"
                +         "event: MapElementEvent {"
                +           "detachedKey: B,"
                +           "detachedValue: Beta,"
                +           "modification: null,"
                +           "cause: {"
                +             "viewInfo: org.babyfish.collection.viewinfo.NavigableMapViewInfos$DescendingKeySet{},"
                +             "event: KeySetElementEvent {"
                +               "detachedElement: B,"
                +               "value: Beta,"
                +               "modification: null,"
                +               "cause: {"
                +                 "viewInfo: org.babyfish.collection.viewinfo.NavigableSetViewInfos$HeadSet{ "
                +                   "toElement: C, "
                +                   "inclusive: false "
                +                 "},"
                +                 "event: KeySetElementEvent {"
                +                   "detachedElement: B,"
                +                   "value: Beta,"
                +                   "modification: null,"
                +                   "cause: {"
                +                     "viewInfo: org.babyfish.collection.viewinfo.NavigableSetViewInfos$DescendingIterator{},"
                +                     "event: KeySetElementEvent {"
                +                       "detachedElement: B,"
                +                       "value: Beta,"
                +                       "modification: org.babyfish.collection.event.modification.IteratorModifications$Remove{},"
                +                       "cause: null"
                +                     "}"
                +                   "}"
                +                 "}"
                +               "}"
                +             "}"
                +           "}"
                +         "}"
                +       "}"
                +     "}"
                +   "}"
                + "}",
                mapRecorder.toString()
        );
    }

    /*
     * (2) <root> : List<E>
     *     |
     *     \--->subList(1, 6) : List<E>
     *          |
     *          \--->subList(1, 4) : List<E>
     *               |
     *               \--->subList(1, 2) : List<E>
     *                    |
     *                    \--->iterator() : Iterator<E>
     * 
     * Then we use the iterator to delete one element, the data change
     * event will be triggered by the iterator, then this event will be
     * bubbled step by step, finally, the event will be triggered by
     * the root List.
     * 
     * In this test case, the data of event and bubble event are different.
     * (1) For the events triggered by iterator and subList3, index = 0
     * (2) For the event triggered by subList2, index = 1
     * (3) For the event triggered by subList1, index = 2
     * (4) For the event triggered by root List, index = 3
     */
    @Test
    public void testBubbleEventWithEventDataDifference() {
        MAList<String> list = new MAArrayList<>();
        list.addAll(MACollections.wrap("Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta"));
        
        MAList<String> subList1 = list.subList(1, 6);
        MAList<String> subList2 = subList1.subList(1, 4);
        MAList<String> subList3 = subList2.subList(1, 2);
        MAListIterator<String> iterator = subList3.iterator();
        
        ListBubbleRecorder<String>
            listRecorder = new ListBubbleRecorder<>(),
            subList1Recorder = new ListBubbleRecorder<>(),
            subList2Recorder = new ListBubbleRecorder<>(),
            subList3Recorder = new ListBubbleRecorder<>(),
            iteratorRecorder = new ListBubbleRecorder<>();
            
        list.addListElementListener(listRecorder.listElementListener());
        subList1.addListElementListener(subList1Recorder.listElementListener());
        subList2.addListElementListener(subList2Recorder.listElementListener());
        subList3.addListElementListener(subList3Recorder.listElementListener());
        iterator.addListElementListener(iteratorRecorder.listElementListener());
        
        iterator.next();
        iterator.remove();
        
        /*
         * (1) If the event is not bubbled event: e.getModification() != null, but e.getCause() == null
         * (2) If the event is bubbled event: e.getModification() == null, but e.getCause() != null
         */
        Assert.assertEquals(
                "ListElementEvent {"
                +   "detachedElement: Delta,"
                +   "detachedIndex: 0,"
                +   "modification: org.babyfish.collection.event.modification.IteratorModifications$Remove{},"
                +   "cause: null"
                + "}", 
                iteratorRecorder.toString()
        );
        Assert.assertEquals(
                "ListElementEvent {"
                +   "detachedElement: Delta,"
                +   "detachedIndex: 0,"
                +   "modification: null,"
                +   "cause: {"
                +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIterator{ "
                +       "index: 0 "
                +     "},"
                +     "event: ListElementEvent {"
                +       "detachedElement: Delta,"
                +       "detachedIndex: 0,"
                +       "modification: org.babyfish.collection.event.modification.IteratorModifications$Remove{},"
                +       "cause: null"
                +     "}"
                +   "}"
                + "}", 
                subList3Recorder.toString()
        );
        Assert.assertEquals(
                "ListElementEvent {"
                +   "detachedElement: Delta,"
                +   "detachedIndex: 1,"
                +   "modification: null,"
                +   "cause: {"
                +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                +       "fromIndex: 1, "
                +       "toIndex: 2 "
                +     "},"
                +     "event: ListElementEvent {"
                +       "detachedElement: Delta,"
                +       "detachedIndex: 0,"
                +       "modification: null,"
                +       "cause: {"
                +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIterator{ "
                +           "index: 0 "
                +         "},"
                +         "event: ListElementEvent {"
                +           "detachedElement: Delta,"
                +           "detachedIndex: 0,"
                +           "modification: org.babyfish.collection.event.modification.IteratorModifications$Remove{},"
                +           "cause: null"
                +         "}"
                +       "}"
                +     "}"
                +   "}"
                + "}", 
                subList2Recorder.toString()
        );
        Assert.assertEquals(
                "ListElementEvent {"
                +   "detachedElement: Delta,"
                +   "detachedIndex: 2,"
                +   "modification: null,"
                +   "cause: {"
                +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                +       "fromIndex: 1, "
                +       "toIndex: 4 "
                +     "},"
                +     "event: ListElementEvent {"
                +       "detachedElement: Delta,"
                +       "detachedIndex: 1,"
                +       "modification: null,"
                +       "cause: {"
                +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                +           "fromIndex: 1, "
                +           "toIndex: 2 "
                +         "},"
                +         "event: ListElementEvent {"
                +           "detachedElement: Delta,"
                +           "detachedIndex: 0,"
                +           "modification: null,"
                +           "cause: {"
                +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIterator{ "
                +               "index: 0 "
                +             "},"
                +             "event: ListElementEvent {"
                +               "detachedElement: Delta,"
                +               "detachedIndex: 0,"
                +               "modification: org.babyfish.collection.event.modification.IteratorModifications$Remove{},"
                +               "cause: null"
                +             "}"
                +           "}"
                +         "}"
                +       "}"
                +     "}"
                +   "}"
                + "}", 
                subList1Recorder.toString()
        );
        Assert.assertEquals(
                "ListElementEvent {"
                +   "detachedElement: Delta,"
                +   "detachedIndex: 3,"
                +   "modification: null,"
                +   "cause: {"
                +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                +       "fromIndex: 1, "
                +       "toIndex: 6 "
                +     "},"
                +     "event: ListElementEvent {"
                +       "detachedElement: Delta,"
                +       "detachedIndex: 2,"
                +       "modification: null,"
                +       "cause: {"
                +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                +           "fromIndex: 1, "
                +           "toIndex: 4 "
                +         "},"
                +         "event: ListElementEvent {"
                +           "detachedElement: Delta,"
                +           "detachedIndex: 1,"
                +           "modification: null,"
                +           "cause: {"
                +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                +               "fromIndex: 1, "
                +               "toIndex: 2 "
                +             "},"
                +               "event: ListElementEvent {"
                +               "detachedElement: Delta,"
                +               "detachedIndex: 0,"
                +               "modification: null,"
                +               "cause: {"
                +                 "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIterator{ "
                +                   "index: 0 "
                +                 "},"
                +                 "event: ListElementEvent {"
                +                   "detachedElement: Delta,"
                +                   "detachedIndex: 0,"
                +                   "modification: org.babyfish.collection.event.modification.IteratorModifications$Remove{},"
                +                   "cause: null"
                +                 "}"
                +               "}"
                +             "}"
                +           "}"
                +         "}"
                +       "}"
                +     "}"
                +   "}"
                + "}", 
                listRecorder.toString()
        );
    }
    
    private static class MapBubbleRecorder<K, V> {
        
        private StringBuilder builder = new StringBuilder();
        
        private ElementListener<K> keyElementListener = 
                new ElementListener<K>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void modified(ElementEvent<K> e) throws Throwable {
                        MapBubbleRecorder.this.append((KeySetElementEvent<K, V>)e);
                    }
                };
                
        private MapElementListener<K, V> mapElementListener =
                new MapElementListener<K, V>() {
                    @Override
                    public void modified(MapElementEvent<K, V> e) throws Throwable {
                        MapBubbleRecorder.this.append(e);
                    }
                };
        
        public ElementListener<K> keyElementListener() {
            return this.keyElementListener;
        }
        
        public MapElementListener<K, V> mapElementListener() {
            return this.mapElementListener;
        }
        
        @Override
        public String toString() {
            return this.builder.toString();
        }
        
        private void append(KeySetElementEvent<K, V> e) {
            this
            .builder
            .append("KeySetElementEvent {");
            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                this
                .builder
                .append("detachedElement: ")
                .append(e.getElement(PropertyVersion.DETACH));
            }
            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                this
                .builder
                .append(e.getModificationType().contains(PropertyVersion.DETACH) ? "," : "")
                .append("attachedElement: ")
                .append(e.getElement(PropertyVersion.ATTACH));
            }
            this
            .builder
            .append(",value: ")
            .append(e.getValue())
            .append(",modification: ")
            .append(e.getModification());
            if (e.getCause() == null) {
                this.builder.append(",cause: null");
            } else {
                this
                .builder
                .append(",cause: {")
                .append("viewInfo: ")
                .append(e.getCause().getViewInfo())
                .append(",event: ");
                this.append(e.getCause().<ModificationEvent>getViewEvent());
                this
                .builder
                .append("}");
            }
            this
            .builder
            .append("}");
        }
        
        private void append(MapElementEvent<K, V> e) {
            this
            .builder
            .append("MapElementEvent {");
            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                this
                .builder
                .append("detachedKey: ")
                .append(e.getKey(PropertyVersion.DETACH))
                .append(",detachedValue: ")
                .append(e.getValue(PropertyVersion.DETACH));
            }
            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                this
                .builder
                .append(e.getModificationType().contains(PropertyVersion.DETACH) ? "," : "")
                .append("attachedKey: ")
                .append(e.getKey(PropertyVersion.ATTACH))
                .append(",attachedValue: ")
                .append(e.getValue(PropertyVersion.ATTACH));
            }
            this
            .builder
            .append(",modification: ")
            .append(e.getModification());
            if (e.getCause() == null) {
                this.builder.append(",cause: null");
            } else {
                this
                .builder
                .append(",cause: {")
                .append("viewInfo: ")
                .append(e.getCause().getViewInfo())
                .append(",event: ");
                this.append(e.getCause().<ModificationEvent>getViewEvent());
                this.builder.append("}");
            }
            this
            .builder
            .append("}");
        }
        
        @SuppressWarnings("unchecked")
        private void append(ModificationEvent e) {
            if (e instanceof KeySetElementEvent<?, ?>) {
                this.append((KeySetElementEvent<K, V>)e);
            } else {
                this.append((MapElementEvent<K, V>)e);
            }
        }
    }

    private static class ListBubbleRecorder<E> {
        
        private StringBuilder builder = new StringBuilder();
        
        private ListElementListener<E> listElementListener =
                new ListElementListener<E>() {
                    @Override
                    public void modified(ListElementEvent<E> e) throws Throwable {
                        ListBubbleRecorder.this.append(e);
                    }
                };
                
        public ListElementListener<E> listElementListener() {
            return this.listElementListener;
        }
        
        @Override
        public String toString() {
            return this.builder.toString();
        }
        
        @SuppressWarnings("unchecked")
        private void append(ListElementEvent<E> e) {
            this
            .builder
            .append("ListElementEvent {");
            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                this
                .builder
                .append("detachedElement: ")
                .append(e.getElement(PropertyVersion.DETACH))
                .append(",detachedIndex: ")
                .append(e.getIndex(PropertyVersion.DETACH));
            }
            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                this
                .builder
                .append(e.getModificationType().contains(PropertyVersion.DETACH) ? "," : "")
                .append("attachedElement: ")
                .append(e.getElement(PropertyVersion.ATTACH))
                .append(",attachedIndex: ")
                .append(e.getIndex(PropertyVersion.ATTACH));
            }
            this
            .builder
            .append(",modification: ")
            .append(e.getModification());
            if (e.getCause() == null) {
                this.builder.append(",cause: null");
            } else {
                this
                .builder
                .append(",cause: {")
                .append("viewInfo: ")
                .append(e.getCause().getViewInfo())
                .append(",event: ");
                this.append((ListElementEvent<E>)e.getCause().getViewEvent());
                this
                .builder
                .append("}");
            }
            this
            .builder
            .append("}");
        }
    }
}
