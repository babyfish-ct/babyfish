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

import org.babyfish.collection.BidiType;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.collection.spi.base.BaseEntryIterator;
import org.babyfish.collection.spi.base.LinkedHashEntries;
import org.babyfish.collection.spi.base.OrderedBaseEntries;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class LinkedHashEntriesTest {
    
    @Test
    public void testReplaceToHead() {
        OrderedBaseEntries<Character, Object> entries;
        
        entries = createEntriesWithAB(OrderAdjustMode.HEAD);
        entries.put('A', null, null);
        validateOrderedEntries(entries, 'A', 'B');
        validateOrderedEntries(entries.descendingEntries(), 'B', 'A');
        
        entries = createEntriesWithAB(OrderAdjustMode.HEAD);
        entries.put('B', null, null);
        validateOrderedEntries(entries, 'B', 'A');
        validateOrderedEntries(entries.descendingEntries(), 'A', 'B');
        
        entries = createEntriesWithABC(OrderAdjustMode.HEAD);
        entries.put('A', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'C');
        validateOrderedEntries(entries.descendingEntries(), 'C', 'B', 'A');
        
        entries = createEntriesWithABC(OrderAdjustMode.HEAD);
        entries.put('B', null, null);
        validateOrderedEntries(entries, 'B', 'A', 'C');
        validateOrderedEntries(entries.descendingEntries(), 'C', 'A', 'B');
        
        entries = createEntriesWithABC(OrderAdjustMode.HEAD);
        entries.put('C', null, null);
        validateOrderedEntries(entries, 'C', 'A', 'B');
        validateOrderedEntries(entries.descendingEntries(), 'B', 'A', 'C');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.HEAD);
        entries.put('A', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.HEAD);
        entries.put('B', null, null);
        validateOrderedEntries(entries, 'B', 'A', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'A', 'B');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.HEAD);
        entries.put('C', null, null);
        validateOrderedEntries(entries, 'C', 'A', 'B', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'B', 'A', 'C');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.HEAD);
        entries.put('D', null, null);
        validateOrderedEntries(entries, 'D', 'A', 'B', 'C', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'C', 'B', 'A', 'D');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.HEAD);
        entries.put('E', null, null);
        validateOrderedEntries(entries, 'E', 'A', 'B', 'C', 'D');
        validateOrderedEntries(entries.descendingEntries(), 'D', 'C', 'B', 'A', 'E');
    }
    
    @Test
    public void testReplaceToTail() {
        OrderedBaseEntries<Character, Object> entries;
        
        entries = createEntriesWithAB(OrderAdjustMode.TAIL);
        entries.put('A', null, null);
        validateOrderedEntries(entries, 'B', 'A');
        validateOrderedEntries(entries.descendingEntries(), 'A', 'B');
        
        entries = createEntriesWithAB(OrderAdjustMode.TAIL);
        entries.put('B', null, null);
        validateOrderedEntries(entries, 'A', 'B');
        validateOrderedEntries(entries.descendingEntries(), 'B', 'A');
        
        entries = createEntriesWithABC(OrderAdjustMode.TAIL);
        entries.put('A', null, null);
        validateOrderedEntries(entries, 'B', 'C', 'A');
        validateOrderedEntries(entries.descendingEntries(), 'A', 'C', 'B');
        
        entries = createEntriesWithABC(OrderAdjustMode.TAIL);
        entries.put('B', null, null);
        validateOrderedEntries(entries, 'A', 'C', 'B');
        validateOrderedEntries(entries.descendingEntries(), 'B', 'C', 'A');
        
        entries = createEntriesWithABC(OrderAdjustMode.TAIL);
        entries.put('C', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'C');
        validateOrderedEntries(entries.descendingEntries(), 'C', 'B', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.TAIL);
        entries.put('A', null, null);
        validateOrderedEntries(entries, 'B', 'C', 'D', 'E', 'A');
        validateOrderedEntries(entries.descendingEntries(), 'A', 'E', 'D', 'C', 'B');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.TAIL);
        entries.put('B', null, null);
        validateOrderedEntries(entries, 'A', 'C', 'D', 'E', 'B');
        validateOrderedEntries(entries.descendingEntries(), 'B', 'E', 'D', 'C', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.TAIL);
        entries.put('C', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'D', 'E', 'C');
        validateOrderedEntries(entries.descendingEntries(), 'C', 'E', 'D', 'B', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.TAIL);
        entries.put('D', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'E', 'D');
        validateOrderedEntries(entries.descendingEntries(), 'D', 'E', 'C', 'B', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.TAIL);
        entries.put('E', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
    }
    
    @Test
    public void testReplaceToPrev() {
        OrderedBaseEntries<Character, Object> entries;
        
        entries = createEntriesWithAB(OrderAdjustMode.PREV);
        entries.put('A', null, null);
        validateOrderedEntries(entries, 'A', 'B');
        validateOrderedEntries(entries.descendingEntries(), 'B', 'A');
        
        entries = createEntriesWithAB(OrderAdjustMode.PREV);
        entries.put('B', null, null);
        validateOrderedEntries(entries, 'B', 'A');
        validateOrderedEntries(entries.descendingEntries(), 'A', 'B');
        
        entries = createEntriesWithABC(OrderAdjustMode.PREV);
        entries.put('A', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'C');
        validateOrderedEntries(entries.descendingEntries(), 'C', 'B', 'A');
        
        entries = createEntriesWithABC(OrderAdjustMode.PREV);
        entries.put('B', null, null);
        validateOrderedEntries(entries, 'B', 'A', 'C');
        validateOrderedEntries(entries.descendingEntries(), 'C', 'A', 'B');
        
        entries = createEntriesWithABC(OrderAdjustMode.PREV);
        entries.put('C', null, null);
        validateOrderedEntries(entries, 'A', 'C', 'B');
        validateOrderedEntries(entries.descendingEntries(), 'B', 'C', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.PREV);
        entries.put('A', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.PREV);
        entries.put('B', null, null);
        validateOrderedEntries(entries, 'B', 'A', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'A', 'B');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.PREV);
        entries.put('C', null, null);
        validateOrderedEntries(entries, 'A', 'C', 'B', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'B', 'C', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.PREV);
        entries.put('D', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'D', 'C', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'C', 'D', 'B', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.PREV);
        entries.put('E', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'E', 'D');
        validateOrderedEntries(entries.descendingEntries(), 'D', 'E', 'C', 'B', 'A');
    }
    
    @Test
    public void testReplaceToNext() {
        OrderedBaseEntries<Character, Object> entries;
        
        entries = createEntriesWithAB(OrderAdjustMode.NEXT);
        entries.put('A', null, null);
        validateOrderedEntries(entries, 'B', 'A');
        validateOrderedEntries(entries.descendingEntries(), 'A', 'B');
        
        entries = createEntriesWithAB(OrderAdjustMode.NEXT);
        entries.put('B', null, null);
        validateOrderedEntries(entries, 'A', 'B');
        validateOrderedEntries(entries.descendingEntries(), 'B', 'A');
        
        entries = createEntriesWithABC(OrderAdjustMode.NEXT);
        entries.put('A', null, null);
        validateOrderedEntries(entries, 'B', 'A', 'C');
        validateOrderedEntries(entries.descendingEntries(), 'C', 'A', 'B');
        
        entries = createEntriesWithABC(OrderAdjustMode.NEXT);
        entries.put('B', null, null);
        validateOrderedEntries(entries, 'A', 'C', 'B');
        validateOrderedEntries(entries.descendingEntries(), 'B', 'C', 'A');
        
        entries = createEntriesWithABC(OrderAdjustMode.NEXT);
        entries.put('C', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'C');
        validateOrderedEntries(entries.descendingEntries(), 'C', 'B', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.NEXT);
        entries.put('A', null, null);
        validateOrderedEntries(entries, 'B', 'A', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'A', 'B');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.NEXT);
        entries.put('B', null, null);
        validateOrderedEntries(entries, 'A', 'C', 'B', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'B', 'C', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.NEXT);
        entries.put('C', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'D', 'C', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'C', 'D', 'B', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.NEXT);
        entries.put('D', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'E', 'D');
        validateOrderedEntries(entries.descendingEntries(), 'D', 'E', 'C', 'B', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.NEXT);
        entries.put('E', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
    }
    
    @Test
    public void testReplaceEachElement() {
        OrderedBaseEntries<Character, Object> entries;
        
        entries = createEntriesWithABCDE(OrderAdjustMode.HEAD);
        entries.put('A', null, null);
        entries.put('B', null, null);
        entries.put('C', null, null);
        entries.put('D', null, null);
        entries.put('E', null, null);
        validateOrderedEntries(entries, 'E', 'D', 'C', 'B', 'A');
        validateOrderedEntries(entries.descendingEntries(), 'A', 'B', 'C', 'D', 'E');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.TAIL);
        entries.put('A', null, null);
        entries.put('B', null, null);
        entries.put('C', null, null);
        entries.put('D', null, null);
        entries.put('E', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.PREV);
        entries.put('A', null, null);
        entries.put('B', null, null);
        entries.put('C', null, null);
        entries.put('D', null, null);
        entries.put('E', null, null);
        validateOrderedEntries(entries, 'B', 'C', 'D', 'E', 'A');
        validateOrderedEntries(entries.descendingEntries(), 'A', 'E', 'D', 'C', 'B');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.NEXT);
        entries.put('A', null, null);
        entries.put('B', null, null);
        entries.put('C', null, null);
        entries.put('D', null, null);
        entries.put('E', null, null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
    }
    
    @Test
    public void testClear() {
        OrderedBaseEntries<Character, Object> entries = createEntriesWithABCDE(OrderAdjustMode.NONE);
        entries.clear(null);
        validateOrderedEntries(entries);
        validateOrderedEntries(entries.descendingEntries());
    }
    
    @Test
    public void testRemove() {
        
        OrderedBaseEntries<Character, Object> entries;
        
        entries = createEntriesWithAB(OrderAdjustMode.NONE);
        entries.removeByKey('A', null);
        validateOrderedEntries(entries, 'B');
        validateOrderedEntries(entries.descendingEntries(), 'B');
        
        entries = createEntriesWithAB(OrderAdjustMode.NONE);
        entries.removeByKey('B', null);
        validateOrderedEntries(entries, 'A');
        validateOrderedEntries(entries.descendingEntries(), 'A');
        
        entries = createEntriesWithABC(OrderAdjustMode.NONE);
        entries.removeByKey('A', null);
        validateOrderedEntries(entries, 'B', 'C');
        validateOrderedEntries(entries.descendingEntries(), 'C', 'B');
        
        entries = createEntriesWithABC(OrderAdjustMode.NONE);
        entries.removeByKey('B', null);
        validateOrderedEntries(entries, 'A', 'C');
        validateOrderedEntries(entries.descendingEntries(), 'C', 'A');
        
        entries = createEntriesWithABC(OrderAdjustMode.NONE);
        entries.removeByKey('C', null);
        validateOrderedEntries(entries, 'A', 'B');
        validateOrderedEntries(entries.descendingEntries(), 'B', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.NONE);
        entries.removeByKey('A', null);
        validateOrderedEntries(entries, 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.NONE);
        entries.removeByKey('B', null);
        validateOrderedEntries(entries, 'A', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.NONE);
        entries.removeByKey('C', null);
        validateOrderedEntries(entries, 'A', 'B', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'B', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.NONE);
        entries.removeByKey('D', null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'C', 'B', 'A');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.NONE);
        entries.removeByKey('E', null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D');
        validateOrderedEntries(entries.descendingEntries(), 'D', 'C', 'B', 'A');
    }
    
    @Test
    public void testIteratorRemove() {
        OrderedBaseEntries<Character, Object> entries;
        BaseEntryIterator<Character, Object> iterator;
        
        entries = createEntriesWithABCDE(OrderAdjustMode.NONE);
        iterator = entries.iterator();
        while (iterator.hasNext()) {
            BaseEntry<Character, Object> be = iterator.next();
            if ((be.getKey().charValue() - 'A') % 2 == 0) {
                iterator.remove(null);
            }
        }
        validateOrderedEntries(entries, 'B', 'D');
        validateOrderedEntries(entries.descendingEntries(), 'D', 'B');
        
        entries = createEntriesWithABCDE(OrderAdjustMode.NONE);
        iterator = entries.iterator();
        while (iterator.hasNext()) {
            BaseEntry<Character, Object> be = iterator.next();
            if ((be.getKey().charValue() - 'A') % 2 != 0) {
                iterator.remove(null);
            }
        }
        validateOrderedEntries(entries, 'A', 'C', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'C', 'A');
    }
    
    @Test
    public void testAccessInNoneMode() {
        LinkedHashEntries<Character, Object> entries = 
                createEntriesByAccessMode(
                        OrderAdjustMode.NONE, 
                        'A', 
                        'B', 
                        'C', 
                        'D', 
                        'E');
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries.descendingEntries().access('A', null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries.descendingEntries().access('B', null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries.descendingEntries().access('C', null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries.descendingEntries().access('D', null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries.descendingEntries().access('E', null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
    }
    
    @Test
    public void testAccessInHeadMode() {
        LinkedHashEntries<Character, Object> entries = 
                createEntriesByAccessMode(
                        OrderAdjustMode.HEAD, 
                        'A', 
                        'B', 
                        'C', 
                        'D', 
                        'E');
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries.descendingEntries().access('A', null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries.descendingEntries().access('B', null);
        validateOrderedEntries(entries, 'B', 'A', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'A', 'B');
        
        entries.descendingEntries().access('C', null);
        validateOrderedEntries(entries, 'C', 'B', 'A', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'A', 'B', 'C');
        
        entries.descendingEntries().access('D', null);
        validateOrderedEntries(entries, 'D', 'C', 'B', 'A', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'A', 'B', 'C', 'D');
        
        entries.descendingEntries().access('E', null);
        validateOrderedEntries(entries, 'E', 'D', 'C', 'B', 'A');
        validateOrderedEntries(entries.descendingEntries(), 'A', 'B', 'C', 'D', 'E');
    }
    
    @Test
    public void testAccessInPrevMode() {
        LinkedHashEntries<Character, Object> entries = 
                createEntriesByAccessMode(
                        OrderAdjustMode.PREV, 
                        'A', 
                        'B', 
                        'C', 
                        'D', 
                        'E');
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries.descendingEntries().access('A', null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries.descendingEntries().access('B', null);
        validateOrderedEntries(entries, 'B', 'A', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'A', 'B');
        
        entries.descendingEntries().access('C', null);
        validateOrderedEntries(entries, 'B', 'C', 'A', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'A', 'C', 'B');
        
        entries.descendingEntries().access('D', null);
        validateOrderedEntries(entries, 'B', 'C', 'D', 'A', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'A', 'D', 'C', 'B');
        
        entries.descendingEntries().access('E', null);
        validateOrderedEntries(entries, 'B', 'C', 'D', 'E', 'A');
        validateOrderedEntries(entries.descendingEntries(), 'A', 'E', 'D', 'C', 'B');
    }
    
    @Test
    public void testAccessInNextMode() {
        LinkedHashEntries<Character, Object> entries = 
                createEntriesByAccessMode(
                        OrderAdjustMode.NEXT, 
                        'A', 
                        'B', 
                        'C', 
                        'D', 
                        'E');
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries.descendingEntries().access('A', null);
        validateOrderedEntries(entries, 'B', 'A', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'A', 'B');
        
        entries.descendingEntries().access('B', null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries.descendingEntries().access('C', null);
        validateOrderedEntries(entries, 'A', 'B', 'D', 'C', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'C', 'D', 'B', 'A');
        
        entries.descendingEntries().access('D', null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries.descendingEntries().access('E', null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
    }
    
    @Test
    public void testAccessInTailMode() {
        LinkedHashEntries<Character, Object> entries = 
                createEntriesByAccessMode(
                        OrderAdjustMode.TAIL, 
                        'A', 
                        'B', 
                        'C', 
                        'D', 
                        'E');
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
        
        entries.descendingEntries().access('A', null);
        validateOrderedEntries(entries, 'B', 'C', 'D', 'E', 'A');
        validateOrderedEntries(entries.descendingEntries(), 'A', 'E', 'D', 'C', 'B');
        
        entries.descendingEntries().access('B', null);
        validateOrderedEntries(entries, 'C', 'D', 'E', 'A', 'B');
        validateOrderedEntries(entries.descendingEntries(), 'B', 'A', 'E', 'D', 'C');
        
        entries.descendingEntries().access('C', null);
        validateOrderedEntries(entries, 'D', 'E', 'A', 'B', 'C');
        validateOrderedEntries(entries.descendingEntries(), 'C', 'B', 'A', 'E', 'D');
        
        entries.descendingEntries().access('D', null);
        validateOrderedEntries(entries, 'E', 'A', 'B', 'C', 'D');
        validateOrderedEntries(entries.descendingEntries(), 'D', 'C', 'B', 'A', 'E');
        
        entries.descendingEntries().access('E', null);
        validateOrderedEntries(entries, 'A', 'B', 'C', 'D', 'E');
        validateOrderedEntries(entries.descendingEntries(), 'E', 'D', 'C', 'B', 'A');
    }
    
    private static LinkedHashEntries<Character, Object> createEntriesWithAB(
            OrderAdjustMode replaceMode) {
        return createEntriesByReplaceMode(replaceMode, 'A', 'B');
    }
    
    private static LinkedHashEntries<Character, Object> createEntriesWithABC(
            OrderAdjustMode replaceMode) {
        return createEntriesByReplaceMode(replaceMode, 'A', 'B', 'C');
    }
    
    private static LinkedHashEntries<Character, Object> createEntriesWithABCDE(
            OrderAdjustMode replaceMode) {
        return createEntriesByReplaceMode(replaceMode, 'A', 'B', 'C', 'D', 'E');
    }

    private static LinkedHashEntries<Character, Object> createEntriesByReplaceMode(
            OrderAdjustMode replaceMode, char ... args) {
        LinkedHashEntries<Character, Object> buckets = 
            new LinkedHashEntries<Character, Object>(
                    BidiType.NONE,
                    ReplacementRule.NEW_REFERENCE_WIN, 
                    null, 
                    null, 
                    2, 
                    null, 
                    false, 
                    OrderAdjustMode.NONE,
                    replaceMode);
        for (char arg : args) {
            buckets.put(arg, null, null);
        }
        return buckets;
    }
    
    private static LinkedHashEntries<Character, Object> createEntriesByAccessMode(
            OrderAdjustMode accessMode, char ... args) {
        LinkedHashEntries<Character, Object> buckets = 
            new LinkedHashEntries<Character, Object>(
                    BidiType.NONE,
                    ReplacementRule.NEW_REFERENCE_WIN, 
                    null, 
                    null, 
                    2, 
                    null, 
                    false, 
                    accessMode,
                    OrderAdjustMode.NONE);
        for (char arg : args) {
            buckets.put(arg, null, null);
        }
        return buckets;
    }
    
    private static void validateOrderedEntries(
            OrderedBaseEntries<Character, Object> entries, char ... args) {
        Assert.assertEquals(args.length, entries.size());
        Assert.assertEquals(args.length == 0, entries.isEmpty());
        BaseEntryIterator<Character, Object> iterator = entries.iterator();
        int len = 0;
        while (iterator.hasNext()) {
            Assert.assertEquals(args[len++], iterator.next().getKey().charValue());
        }
        Assert.assertEquals(args.length, len);
        iterator = entries.descendingEntries().iterator();
        len = 0;
        while (iterator.hasNext()) {
            Assert.assertEquals(args[args.length - ++len], iterator.next().getKey().charValue());
        }
        Assert.assertEquals(args.length, len);
    }
    
}
