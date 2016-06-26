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

import org.babyfish.collection.LockMode;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.ReaderOptimizationType;
import org.babyfish.collection.XList;
import org.babyfishdemo.collections.mock.MockedReadWriteLock;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Before you learn this test case,
 * you'd better learn other at first.
 * 
 * BabyFish supports an interface like this
 * 
 *    package org.babyfish.lang;
 * 
 *    public interface LockDescriptor {
 *      boolean isNonFairLockSupported();
 *    }
 * 
 * It will be implementation by the collections, maps, iterators 
 * and map-entries of BabyFish Collection Framework.
 * 
 * For most implementations, this method return true, that means
 * that collection(map, iterator or map-entry) supports non-fair-lock.
 * BabyFish will used the readLock if you only use the methods with
 * ONLY read-only behaviors of that collection.
 * 
 * For few implementations, this method return false, thet means
 * that collection(map, iterator or map-entry) dose NOT support non-fair-lock.
 * BabyFish will used the WRITELock even if you only use the methods with
 * read-only behaviors of that collection.
 * 
 * Specially, the collection frameworks that are not supported BabyFish
 * may not implement this interface.
 * (1) If one class is under the package "java.util", BabyFish consider
 * it supports the non-pair-lock
 * (2) Otherwise, consider it does NOT support non-pair-lock.
 * 
 * @author Tao Chen
 */
public class ReadWriteLockSupportingTest {

    @Test
    public void testNonFairLockBehaviors() {
        /*
         * org.babyfish.collection.LinkedList supports 2 mode
         * (1) OPTIMIZE_READING: In this mode, "get(int)" can be optimized
         * but the read-lock is not supported(This is default behavior).
         * (2) OPTIMIZE_READ_LOCK: In this mode, read-lock is supported
         * but the "get(int)" can not be optimized.
         */
        XList<String> listSupportsNonFairLock = 
                new org.babyfish.collection.LinkedList<>(ReaderOptimizationType.OPTIMIZE_READ_LOCK);
        XList<String> listDoesNotSupportNonFairLock = 
                new org.babyfish.collection.LinkedList<>(ReaderOptimizationType.OPTIMIZE_READING);
        Assert.assertTrue(listSupportsNonFairLock.isReadWriteLockSupported());
        Assert.assertFalse(listDoesNotSupportNonFairLock.isReadWriteLockSupported());
        
        /*
         * Use locked collection to test it.
         */
        {
            StringBuilder builder = new StringBuilder();
            MACollections.locked(listSupportsNonFairLock, new MockedReadWriteLock("a", builder)).size();
            MACollections.locked(listDoesNotSupportNonFairLock, new MockedReadWriteLock("b", builder)).size();
            Assert.assertEquals(
                    // If non-fiar-lock is supported, read-only method such as "size()" uses read-lock;
                    "a.readLock().lock();a.readLock().unlock();" +
                    // otherwise, uses writeLock-lock
                    "b.writeLock().lock();b.writeLock().unlock();", 
                    builder.toString()
            );
        }
        
        /*
         * Use locking collection to test it.
         */
        {
            StringBuilder builder = new StringBuilder();
            MACollections.locking(
                    MACollections.locked(listSupportsNonFairLock, new MockedReadWriteLock("a", builder)), 
                    LockMode.READ, // Expect = read lock, Actual = read lock
                    (XList<String> lockingList) -> lockingList.size()
            );
            MACollections.locking(
                    MACollections.locked(listDoesNotSupportNonFairLock, new MockedReadWriteLock("b", builder)), 
                    LockMode.READ, // Expect = read lock, Actual = write lock
                    (XList<String> lockingList) -> lockingList.size()
            );
            Assert.assertEquals(
                    // If non-fiar-lock is supported, read-only method such as "size()" uses read-lock;
                    "a.readLock().lock();a.readLock().unlock();" +
                    // otherwise, uses writeLock-lock
                    "b.writeLock().lock();b.writeLock().unlock();", 
                    builder.toString()
            );
        }
    }
}
