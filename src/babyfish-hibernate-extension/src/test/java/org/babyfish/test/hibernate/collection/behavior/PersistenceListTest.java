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

import java.util.List;
import java.util.ListIterator;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MALinkedList;
import org.babyfish.collection.XList;
import org.babyfish.collection.viewinfo.ListViewInfos.SubList;
import org.babyfish.hibernate.collection.type.MAListType;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;
/**
 * @author Tao Chen
 */
public class PersistenceListTest {
    
    private static final TestSite.ListFactory[] LIST_FACTORIES =
        new TestSite.ListFactory[] {
            new TestSite.ListFactory() {
                @Override
                public XList<String> createList() {
                    return new org.babyfish.collection.ArrayList<String>(BidiType.NONNULL_VALUES);
                }
            },
            new TestSite.ListFactory() {
                @Override
                public XList<String> createList() {
                    return new MAArrayList<String>(BidiType.NONNULL_VALUES);
                }
            },
            new TestSite.ListFactory() {
                @Override
                public XList<String> createList() {
                    return new org.babyfish.collection.LinkedList<String>(BidiType.NONNULL_VALUES);
                }
            },
            new TestSite.ListFactory() {
                @Override
                public XList<String> createList() {
                    return new MALinkedList<String>(BidiType.NONNULL_VALUES);
                }
            },
            new TestSite.ListFactory() {
                @SuppressWarnings("unchecked")
                @Override
                public XList<String> createList() {
                    return (XList<String>) new MAListType().wrap(null, new MAArrayList<String>(BidiType.NONNULL_VALUES));
                }
            },
            new TestSite.ListFactory() {
                @SuppressWarnings("unchecked")
                @Override
                public XList<String> createList() {
                    return (XList<String>) new MAListType().wrap(null, new MALinkedList<String>(BidiType.NONNULL_VALUES));
                }
            },
        };
    
    private TestSite[] testSites;
    
    @Before
    public void initalize() {
        TestSite[] sites = new TestSite[LIST_FACTORIES.length];
        int index = 0;
        for (TestSite.ListFactory listFactory : LIST_FACTORIES) {
            sites[index++] = new TestSite(listFactory);
        }
        this.testSites = sites;
    }
    
    @Test
    public void testAdd() {
        for (TestSite testSite : this.testSites) {
            testSite.testAdd();
        }
    }
    
    @Test
    public void testAddAll() {
        for (TestSite testSite : this.testSites) {
            testSite.testAddAll();
        }
    }
    
    @Test
    public void testSet() {
        for (TestSite testSite : this.testSites) {
            testSite.testSet();
        }
    }
    
    @Test
    public void testAddByListIteratorDuringNextConflictWithA() {
        for (TestSite testSite : this.testSites) {
            testSite.testAddByListIteratorDuringNextConflictWithA();
        }
    }
    
    @Test
    public void testAddByListIteratorDuringNextConflictWithB() {
        for (TestSite testSite : this.testSites) {
            testSite.testAddByListIteratorDuringNextConflictWithB();
        }
    }
    
    @Test
    public void testAddByListIteratorDuringPreviousConflictWithA() {
        for (TestSite testSite : this.testSites) {
            testSite.testAddByListIteratorDuringPreviousConflictWithB();
        }
    }
    
    @Test
    public void testAddByListIteratorDuringPreviousConflictWithB() {
        for (TestSite testSite : this.testSites) {
            testSite.testAddByListIteratorDuringPreviousConflictWithA();
        }
    }
    
    @Test
    public void testSetByListIteratoryDuringNextWithoutChange() {
        for (TestSite testSite : this.testSites) {
            testSite.testSetByListIteratoryDuringNextWithoutChange();
        }
    }
    
    @Test
    public void testSetByListIteratoryDuringNextWithChange() {
        for (TestSite testSite : this.testSites) {
            testSite.testSetByListIteratoryDuringNextWithChange();
        }
    }
    
    @Test
    public void testSetByListIteratoryDuringPreviousWithoutChange() {
        for (TestSite testSite : this.testSites) {
            testSite.testSetByListIteratoryDuringPreviousWithoutChange();
        }
    }
    
    @Test
    public void testSetByListIteratoryDuringPreviousWithChange() {
        for (TestSite testSite : this.testSites) {
            testSite.testSetByListIteratoryDuringPreviousWithChange();
        }
    }

    private static class TestSite {
        
        private ListFactory listFactory;
        
        public TestSite(ListFactory listFactory) {
            this.listFactory = listFactory;
        }

        interface ListFactory {
            XList<String> createList();
        }
        
        private List<String> createList() {
            XList<String> list = this.listFactory.createList();
            return list;
        }
        
        void testAdd() {
            List<String> list = this.prepareList("A", "B", "C", "D", "E");
            List<String> subList1 = list.subList(1, 4);
            List<String> subList2 = subList1.subList(1, 2);
            assertList(subList1, "B", "C", "D");
            assertList(subList2, "C");
            assertViewSupporter(subList1, 1, 4);
            assertViewSupporter(subList2, 1, 2);
            
            subList2.add("E");
            assertList(list, "A", "B", "C", "E", "D");
            assertList(subList1, "B", "C", "E", "D");
            assertList(subList2, "C", "E");
            assertViewSupporter(subList1, 1, 5);
            assertViewSupporter(subList2, 1, 3);
            
            subList2.add(0, "A");
            assertList(list, "B", "A", "C", "E", "D");
            assertList(subList1, "B", "A", "C", "E", "D");
            assertList(subList2, "A", "C", "E");
            assertViewSupporter(subList1, 0, 5);
            assertViewSupporter(subList2, 1, 4);
            
            list = this.prepareList("A", "B", "C", "D", "E");
            subList1 = list.subList(1, 4);
            subList2 = subList1.subList(1, 2);
            assertList(subList1, "B", "C", "D");
            assertList(subList2, "C");
            assertViewSupporter(subList1, 1, 4);
            assertViewSupporter(subList2, 1, 2);
            
            subList2.add("B");
            assertList(list, "A", "C", "B", "D", "E");
            assertList(subList1, "C", "B", "D");
            assertList(subList2, "C", "B");
            assertViewSupporter(subList1, 1, 4);
            assertViewSupporter(subList2, 0, 2);
            
            subList2.add(0, "D");
            assertList(list, "A", "D", "C", "B", "E");
            assertList(subList1, "D", "C", "B");
            assertList(subList2, "D", "C", "B");
            assertViewSupporter(subList1, 1, 4);
            assertViewSupporter(subList2, 0, 3);
        }
        
        void testAddAll() {
            List<String> list = this.prepareList("A", "B", "C", "D", "E", "F", "G");
            List<String> subList1 = list.subList(2, 5);
            List<String> subList2 = subList1.subList(1, 2);
            assertList(list, "A", "B", "C", "D", "E", "F", "G");
            assertList(subList1, "C", "D", "E");
            assertList(subList2, "D");
            assertViewSupporter(subList1, 2, 5);
            assertViewSupporter(subList2, 1, 2);
            
            subList2.addAll(0, this.prepareList("D", "A", "G"));
            assertList(list, "B", "C", "D", "A", "G", "E", "F");
            assertList(subList1, "C", "D", "A", "G", "E");
            assertList(subList2, "D", "A", "G");
            assertViewSupporter(subList1, 1, 6);
            assertViewSupporter(subList2, 1, 4);
            
            subList2.addAll(this.prepareList("C", "E"));
            assertList(list, "B", "D", "A", "G", "C", "E", "F");
            assertList(subList1, "D", "A", "G", "C", "E");
            assertList(subList2, "D", "A", "G", "C", "E");
            assertViewSupporter(subList1, 1, 6);
            assertViewSupporter(subList2, 0, 5);
            
            list = this.prepareList("A", "B", "C", "D", "E", "F", "G");
            subList1 = list.subList(2, 5);
            subList2 = subList1.subList(1, 2);
            assertList(list, "A", "B", "C", "D", "E", "F", "G");
            assertList(subList1, "C", "D", "E");
            assertList(subList2, "D");
            assertViewSupporter(subList1, 2, 5);
            assertViewSupporter(subList2, 1, 2);
            
            subList2.addAll(this.prepareList("G", "F", "E", "D", "C", "B", "A"));
            assertList(list, "G", "F", "E", "D", "C", "B", "A");
            assertList(subList1, "G", "F", "E", "D", "C", "B", "A");
            assertList(subList2, "G", "F", "E", "D", "C", "B", "A");
            assertViewSupporter(subList1, 0, 7);
            assertViewSupporter(subList2, 0, 7);
        }
        
        void testSet() {
            
            List<String> list = this.prepareList("A", "B", "C", "D", "E");
            List<String> subList1 = list.subList(1, 4);
            List<String> subList2 = subList1.subList(1, 2);
            assertList(subList1, "B", "C", "D");
            assertList(subList2, "C");
            assertViewSupporter(subList1, 1, 4);
            assertViewSupporter(subList2, 1, 2);
            
            subList2.set(0, "B");
            assertList(list, "A", "B", "D", "E");
            assertList(subList1, "B", "D");
            assertList(subList2, "B");
            assertViewSupporter(subList1, 1, 3);
            assertViewSupporter(subList2, 0, 1);
            
            subList2.set(0, "D");
            assertList(list, "A", "D", "E");
            assertList(subList1, "D");
            assertList(subList2, "D");
            assertViewSupporter(subList1, 1, 2);
            assertViewSupporter(subList2, 0, 1);
            
            subList2.set(0, "A");
            assertList(list, "A", "E");
            assertList(subList1, "A");
            assertList(subList2, "A");
            assertViewSupporter(subList1, 0, 1);
            assertViewSupporter(subList2, 0, 1);
            
            subList2.set(0, "E");
            assertList(list, "E");
            assertList(subList1, "E");
            assertList(subList2, "E");
            assertViewSupporter(subList1, 0, 1);
            assertViewSupporter(subList2, 0, 1);
        }
        
        void testAddByListIteratorDuringNextConflictWithA() {
            List<String> list = this.prepareList("A", "B");
            ListIterator<String> iterator = list.listIterator();
            iterator.next();
            iterator.add("A");
            Assert.assertEquals(1, iterator.nextIndex());
            Assert.assertEquals(0, iterator.previousIndex());
            Assert.assertEquals("B", iterator.next());
            Assert.assertFalse(iterator.hasNext());
            assertList(list, "A", "B");
        }
        
        void testAddByListIteratorDuringNextConflictWithB() {
            List<String> list = this.prepareList("A", "B");
            ListIterator<String> iterator = list.listIterator();
            iterator.next();
            iterator.add("B");
            Assert.assertEquals(1, iterator.nextIndex());
            Assert.assertEquals(0, iterator.previousIndex());
            Assert.assertEquals("B", iterator.next());
            Assert.assertFalse(iterator.hasNext());
            assertList(list, "A", "B");
        }
        
        void testAddByListIteratorDuringPreviousConflictWithA() {
            List<String> list = this.prepareList("A", "B");
            ListIterator<String> iterator = list.listIterator(2);
            iterator.previous();
            iterator.add("A");
            Assert.assertEquals(0, iterator.previousIndex());
            Assert.assertEquals(1, iterator.nextIndex());
            Assert.assertEquals("A", iterator.previous());
            Assert.assertFalse(iterator.hasPrevious());
            assertList(list, "A", "B");
        }
        
        void testAddByListIteratorDuringPreviousConflictWithB() {
            List<String> list = this.prepareList("A", "B");
            ListIterator<String> iterator = list.listIterator(2);
            iterator.previous();
            iterator.add("B");
            Assert.assertEquals(0, iterator.previousIndex());
            Assert.assertEquals(1, iterator.nextIndex());
            Assert.assertEquals("A", iterator.previous());
            Assert.assertFalse(iterator.hasPrevious());
            assertList(list, "A", "B");
        }
        
        void testSetByListIteratoryDuringNextWithoutChange() {
            List<String> list = this.prepareList("A", "B");
            ListIterator<String> iterator = list.listIterator();
            Assert.assertEquals("A", iterator.next());
            iterator.set("A");
            Assert.assertEquals(iterator.nextIndex(), 1);
            Assert.assertEquals(iterator.previousIndex(), 0);
            Assert.assertEquals("B", iterator.next());
            iterator.set("B");
            Assert.assertEquals(iterator.nextIndex(), 2);
            Assert.assertEquals(iterator.previousIndex(), 1);
            Assert.assertFalse(iterator.hasNext());
            assertList(list, "A", "B");
        }
        
        void testSetByListIteratoryDuringNextWithChange() {
            List<String> list = this.prepareList("A", "B");
            ListIterator<String> iterator = list.listIterator();
            Assert.assertEquals("A", iterator.next());
            iterator.set("B");
            Assert.assertFalse(iterator.hasNext());
            assertList(list, "B");
        }
        
        void testSetByListIteratoryDuringPreviousWithoutChange() {
            List<String> list = this.prepareList("A", "B");
            ListIterator<String> iterator = list.listIterator(2);
            Assert.assertEquals("B", iterator.previous());
            iterator.set("B");
            Assert.assertEquals(iterator.previousIndex(), 0);
            Assert.assertEquals(iterator.nextIndex(), 1);
            Assert.assertEquals("A", iterator.previous());
            iterator.set("A");
            Assert.assertEquals(iterator.nextIndex(), 0);
            Assert.assertEquals(iterator.previousIndex(), -1);
            Assert.assertFalse(iterator.hasPrevious());
            assertList(list, "A", "B");
        }
        
        void testSetByListIteratoryDuringPreviousWithChange() {
            List<String> list = this.prepareList("A", "B");
            ListIterator<String> iterator = list.listIterator(2);
            Assert.assertEquals("B", iterator.previous());
            iterator.set("A");
            Assert.assertFalse(iterator.hasPrevious());
            assertList(list, "A");
        }
        
        private List<String> prepareList(String ... elements) {
            List<String> list = this.createList();
            for (String element : elements) {
                list.add(element);
            }
            return list;
        }
        
        @SuppressWarnings("unchecked")
        private static <E> void assertList(List<E> list, E ... elements) {
            Assert.assertEquals(elements.length, list.size());
            for (int i = elements.length - 1; i >= 0; i--) {
                Assert.assertEquals(elements[i], list.get(i));
            }
        }
        
        @SuppressWarnings("unchecked")
        private static <E> void assertViewSupporter(List<?> list, int fromIndex, int toIndex) {
            XList.XListView<E> view = (XList.XListView<E>)list;
            SubList subList = (SubList)view.viewInfo();
            Assert.assertEquals(fromIndex, subList.getFromIndex());
            Assert.assertEquals(toIndex, subList.getToIndex());
        }
    }
}
