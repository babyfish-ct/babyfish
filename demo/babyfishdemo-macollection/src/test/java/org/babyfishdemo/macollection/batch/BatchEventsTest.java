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
package org.babyfishdemo.macollection.batch;

import org.babyfish.collection.MACollections;
import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MASet;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.data.ModificationException;
import org.babyfish.data.event.PropertyVersion;
import org.junit.Test;

import junit.framework.Assert;

public class BatchEventsTest {
    
    @Test
    public void testExceptionInPreEvents() {
        
        MASet<String> set = new MALinkedHashSet<>();
        set.addAll(MACollections.wrap("a", "b", "c", "d", "error", "e", "f", "g"));
        
        set.addElementListener(new PreErrorListenerImpl());
        
        try {
            set.removeAll(MACollections.wrap("a", "c", "error", "e", "g"));
            Assert.fail("Except Modifiation exception");
        } catch (ModificationException ex) {
            Assert.assertFalse(ex.isModified());
            Assert.assertEquals(
                    "A mocked exception before change the collection data", 
                    ex.getCause().getMessage()
            );
        }
        
        // The batch modification will be fully failed when error raised in pre-trigger event method "modifying"
        Assert.assertEquals("[a, b, c, d, error, e, f, g]", set.toString());
    }
    
    @Test
    public void testExceptionInPostEvents() {
        
        MASet<String> set = new MALinkedHashSet<>();
        set.addAll(MACollections.wrap("a", "b", "c", "d", "error", "e", "f", "g"));
        
        set.addElementListener(new PostErrorListenerImpl());
        
        try {
            set.removeAll(MACollections.wrap("a", "c", "error", "e", "g"));
            Assert.fail("Except Modifiation exception");
        } catch (ModificationException ex) {
            Assert.assertTrue(ex.isModified());
            Assert.assertEquals(
                    "A mocked exception after change the collection data", 
                    ex.getCause().getMessage()
            );
        }
        
        // The batch modification will be fully succeed when error raised in post-trigger event method "modified"
        Assert.assertEquals("[b, d, f]", set.toString());
    }

    private static class PreErrorListenerImpl implements ElementListener<String> {

        @Override
        public void modifying(ElementEvent<String> e) throws Throwable {
            if (e.getModificationType().contains(PropertyVersion.DETACH) && 
                    "error".equals(e.getElement(PropertyVersion.DETACH))) {
                throw new RuntimeException("A mocked exception before change the collection data");
            }
        }
    }
    
    private static class PostErrorListenerImpl implements ElementListener<String> {
        @Override
        public void modified(ElementEvent<String> e) throws Throwable {
            if (e.getModificationType().contains(PropertyVersion.DETACH) && 
                    "error".equals(e.getElement(PropertyVersion.DETACH))) {
                throw new RuntimeException("A mocked exception after change the collection data");
            }
        }
    }
}
