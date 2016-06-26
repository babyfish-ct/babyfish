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

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.babyfish.collection.MACollections;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class PrimitiveListTest {

    @Test
    public void testPrimitiveList() {
        /*
         * You can use 
         *    <E> MACollections.wrap(E ...),
         *    MACollections.wrapBoolean(boolean ...),
         *    MACollections.wrapChar(char ...),
         *    MACollections.wrapByte(byte ...),
         *    MACollections.wrapShort(byte ...),
         *    MACollections.wrapInt(int ...),
         *    MACollections.wrapLong(int ...),
         *    MACollections.wrapFloat(float ...),
         *    MACollections.wrapDouble(double ...)
         * to cast array to list
         * 
         * What is is difference between "<T> java.util.Arrays.asList(T...)" and "MACollections.wrap?(? ...)" ?
         * 
         * Though "java.util.Arrays.asList" can create a list wrapper by an array,
         * but that array must be "Object[]", if your array is a primitive array,
         * you must cast it to "Object[]" by boxing all elements.
         * 
         * For example:
         *      List<Integer> list = java.util.Arrays.asList(1, 4, 9, 16, 25);
         * 
         * The memory structure is
         *         +-----------+-----------+-----------+-----------+-----------+
         * list--->|     0     |     1     |     2     |     3     |     4     |
         *         +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
         *               |           |           |           |           |
         *              \|/         \|/         \|/         \|/         \|/
         *          +---------+ +---------+ +---------+ +---------+ +---------+
         *          | Integer | | Integer | | Integer | | Integer | | Integer |
         *          |    1    | |    4    | |    9    | |   16    | |   25    |
         *          +---------+ +---------+ +---------+ +---------+ +---------+ 
         *          
         *      List<Integer> list = MACollections.wrapInt(1, 4, 9, 16, 25);
         * 
         * The memory struct is
         *         +---------+---------+---------+---------+---------+
         * list--->|  1: int |  4: int |  9: int | 16: int | 25: int |
         *         +---------+---------+---------+---------+---------+
         *         
         * Of course, the object boxing will be happen when you call some method of the returned list,
         * such as "Integer list.get(index)". But that boxed Integer object will be created on the
         * youngest generation/region of GC heap, so that it will be allocated/freed very fast. 
         * The list itself that may be in the old generation/region of CG heap does NOT retain any 
         * boxed objects. GC performance will be improved because reference graph become more simple.
         */
        
        int[] oldArr, newArr;
        oldArr = new int[] { 1, 4, 9, 16, 25 };
        List<Integer> list = MACollections.wrapInt(oldArr);
        
        try {
            list.subList(1,  4).clear();
            Assert.fail(UnsupportedOperationException.class.getName() + " is expected");
        } catch (UnsupportedOperationException ex) {
            // The list is read-only
        }
        
        Assert.assertEquals(1, list.get(0).intValue()); // The returned boxed object is created on youngest generation/region of GCHeap
        Assert.assertEquals(4, list.get(1).intValue()); // The returned boxed object is created on youngest generation/region of GCHeap
        Assert.assertEquals(9, list.get(2).intValue()); // The returned boxed object is created on youngest generation/region of GCHeap
        Assert.assertEquals(16, list.get(3).intValue()); // The returned boxed object is created on youngest generation/region of GCHeap
        Assert.assertEquals(25, list.get(4).intValue()); // The returned boxed object is created on youngest generation/region of GCHeap
        
        /*
         * list.toArray() returns "Object[]" with many boxed Integer objects.
         * MACollections.toIntArray() returns the primitive array "int[]",
         * but the returned array is a cloned new array, because the
         * read-only behavior must be guaranteed. 
         */
        newArr = MACollections.toIntArray(list);
        
        Assert.assertNotSame(oldArr, newArr); //Cloned array, not same.
        Assert.assertTrue(Arrays.equals(oldArr, newArr));
        
        /*
         * In order to optimize the performance,
         * you can clone a part of data from the list wrapper :)
         */
        Assert.assertTrue(
                Arrays.equals(
                        new int[] { 4, 9, 16 }, 
                        MACollections.toIntArray(list.subList(1, 4))
                )
        );
    }
}
