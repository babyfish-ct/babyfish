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
import java.util.concurrent.locks.ReadWriteLock;

import junit.framework.Assert;

import org.babyfish.collection.MACollections;
import org.babyfishdemo.collections.mock.MockedReadWriteLock;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class OptimizationTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testIgnoreDuplicateUnmodifiableProxies() {
        
        Collection<String> c = new java.util.ArrayList<>();
        Collection<String>[] proxies = new Collection[10];
        
        proxies[0] = MACollections.unmodifiable(c);
        for (int i = 0; i < proxies.length - 1; i++) {
            proxies[i + 1] = MACollections.unmodifiable(proxies[i]);
        }
        
        // Proxy is NOT original collection
        Assert.assertNotSame(c, proxies[0]);
        
        // But, duplicated proxies will NOT be created
        for (int i = 0; i < proxies.length - 1; i++) {
            Assert.assertSame(proxies[i], proxies[i + 1]);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testIgnoreDuplicateLockedProxies() {
        
        Collection<String> c = new java.util.ArrayList<>();
        Collection<String>[] proxies = new Collection[10];
        
        proxies[0] = MACollections.locked(c);
        for (int i = 0; i < proxies.length - 1; i++) {
            proxies[i + 1] = MACollections.locked(proxies[i]);
        }
        
        // Proxy is NOT original collection
        Assert.assertNotSame(c, proxies[0]);
        
        // But, duplicated proxies will NOT be created
        for (int i = 0; i < proxies.length - 1; i++) {
            Assert.assertSame(proxies[i], proxies[i + 1]);
        }
    }

    @Test
    public void testIgnoreDuplicateMixedProxies() {
        Collection<String> c = new java.util.ArrayList<>();
        
        Collection<String> unmodifiable = MACollections.unmodifiable(c);
        Assert.assertNotSame(unmodifiable, c);
        
        Collection<String> locked = MACollections.locked(unmodifiable);
        Assert.assertNotSame(locked, unmodifiable);
        
        // "locked" is already unmodifiable
        // so the second "unmodifiable" operation will be ignore
        Collection<String> unmodifiable2 = MACollections.unmodifiable(locked);
        Assert.assertSame(unmodifiable2, locked);
    }
    
    @Test
    public void testGatewayPriority() {
        
        StringBuilder builder = new StringBuilder();
        ReadWriteLock readWriteLock = new MockedReadWriteLock("rwl", builder);
        
        Collection<String> c = new java.util.ArrayList<>();
        c = MACollections.unmodifiable(c); // First, create an "unmodifiable" proxy to wrap original collection
        c = MACollections.locked(c, readWriteLock); //Then, create a "locked" proxy to wrap "unmodifiable" collection
        
        /*
         * From the previous code, you can image that the proxy chain is
         * 
         *       +--------------+
         * c --->| locked proxy | 
         *       +-------+------+
         *               |    
         *               |    +--------------------+
         *               \--->| unmodifiable proxy |
         *                    +-------------+------+
         *                                  |
         *                                  |    +---------------------+
         *                                  \--->| Original collection |
         *                                       +---------------------+
         */
        
        /*
         * But, the actual proxy chain does NOT like that, because the 
         * unmodifiable proxy has high priority so that it is always
         * the outermost proxy whenever "MACollections.unmodifiable(...)" is called.
         * 
         *       +--------------------+
         * c --->| unmodifiable proxy | 
         *       +-------------+------+
         *                     |
         *                     |    +--------------+
         *                     \--->| locked proxy |
         *                          +-------+------+
         *                                  |
         *                                  |    +---------------------+
         *                                  \--->| Original collection |
         *                                       +---------------------+
         */
        
        c.size();
        Assert.assertEquals("rwl.readLock().lock();rwl.readLock().unlock();", builder.toString());
        
        // Clear the StringBuilder
        builder.setLength(0);
        
        try {
            c.clear();
            Assert.fail(UnsupportedOperationException.class.getName() + " is expected");
        } catch (UnsupportedOperationException ex) {
        }
        // "Unmodifable" proxy is the outermost proxy, 
        // so that the locked proxy has no chance to be executed
        Assert.assertEquals("", builder.toString());
    }
}
