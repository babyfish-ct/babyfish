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
package org.babyfishdemo.om4java.m2kr;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
 


import org.babyfish.collection.LinkedHashMap;
import org.babyfishdemo.om4java.m2kr.Attribute;
import org.babyfishdemo.om4java.m2kr.Element;
import org.junit.Assert;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class ObjectModelOfMapAndKeyedReferenceTest {
 
    @Test
    public void test() {
        Element element1 = new Element();
        Element element2 = new Element();
        Attribute attribute1 = new Attribute();
        Attribute attribute2 = new Attribute();
        Attribute attribute3 = new Attribute();
        Attribute attribute4 = new Attribute();
        Attribute attribute5 = new Attribute();
        
        {
            /*
             * Validate the initialized state of these objects
             */
            assertElement(element1);
            assertElement(element2);
            assertAttribute(attribute1, null, null);
            assertAttribute(attribute2, null, null);
            assertAttribute(attribute3, null, null);
            assertAttribute(attribute4, null, null);
            assertAttribute(attribute5, null, null);
        }
        
        {
            /*
             * Add attribute1 into element1 with the key "attr-1".
             * (1) The property "element" of attribute1 will be changed to be element1 automatically and implicitly.
             * (2) The property "name" of attribute1 will be changed to be "attr-1" automatically and implicitly.
             */
            element1.getAttributes().put("attr-1", attribute1);
            
            assertElement(element1, attribute1); // Changed by you
            assertElement(element2);
            assertAttribute(attribute1, "attr-1", element1); // Changed automatically
            assertAttribute(attribute2, null, null);
            assertAttribute(attribute3, null, null);
            assertAttribute(attribute4, null, null);
            assertAttribute(attribute5, null, null);
        }
        
        {
            /*
             * Set the property "element" of attribute2 to be element1 and 
             * set the property "name" of attribute2 to be "attr-2".
             * After both of them are changed, the property "attributes" of element1 will be changed, 
             * a new map entry { key: "attr2", value: attribute2 } will
             * be added automatically and implicitly.
             */
            attribute2.setName("attr-2");
            attribute2.setElement(element1);
            
            assertElement(element1, attribute1, attribute2); // Changed automatically
            assertElement(element2);
            assertAttribute(attribute1, "attr-1", element1);
            assertAttribute(attribute2, "attr-2", element1); // Changed by you
            assertAttribute(attribute3, null, null);
            assertAttribute(attribute4, null, null);
            assertAttribute(attribute5, null, null);
        }
        
        {
            /*
             * Add attribute3, attribute4 and attribute5 into element1 into element1 with key "attr-3", "attr-4" and "attr-5".
             * (1) The property "name" of attribute3, attribute4 and attribute5 will be changed to be 
             * "attr-3", "attr-4" and "attr-5" automatically and implicitly.
             * (2) The property "element" of attribute3, attribute4 and attribute5 will be changed to be 
             * element1 automatically and implicitly.
             */
            Map<String, Attribute> m = new LinkedHashMap<>();
            m.put("attr-3", attribute3);
            m.put("attr-4", attribute4);
            m.put("attr-5", attribute5);
            element1.getAttributes().putAll(m);
            
            assertElement(element1, attribute1, attribute2, attribute3, attribute4, attribute5); // Changed by you
            assertElement(element2);
            assertAttribute(attribute1, "attr-1", element1);
            assertAttribute(attribute2, "attr-2", element1);
            assertAttribute(attribute3, "attr-3", element1); // Changed automatically
            assertAttribute(attribute4, "attr-4", element1); // Changed automatically
            assertAttribute(attribute5, "attr-5", element1); // Changed automatically
        }
        
        {
            /*
             * Change the property "element" of attribute5 from element1 to element2.
             * (1) The property "attributes" of old parent object element1 will be changed, 
             * the map entry { key: "attr-5", value attribute5 } will be removed automatically and implicitly..
             * (2) The property "attributes" of new parent object element2 will be changed,
             * the map entry { key: "attr-5", value attribute5 } will be added automatically and implicitly.
             */
            attribute5.setElement(element2);
            
            assertElement(element1, attribute1, attribute2, attribute3, attribute4); // Changed automatically
            assertElement(element2, attribute5); // Changed automatically
            assertAttribute(attribute1, "attr-1", element1);
            assertAttribute(attribute2, "attr-2", element1);
            assertAttribute(attribute3, "attr-3", element1);
            assertAttribute(attribute4, "attr-4", element1);
            assertAttribute(attribute5, "attr-5", element2); // Changed by you
        }
        
        {
            /*
             * Let element2 seize all the attributes of element1
             * (1) The property "attributes" of the old parent object element1 will be changed, 
             * all the map entries will be removed automatically and implicitly.
             * (2) The property "element" of the attribute1, attribute2, attribute3 and attribute4 will be 
             * changed to be "element2" automatically and implicitly.
             */
            element2.getAttributes().putAll(element1.getAttributes());
            
            assertElement(element1); // Changed automatically
            assertElement(element2, attribute5, attribute1, attribute2, attribute3, attribute4); // Changed by you
            assertAttribute(attribute1, "attr-1", element2); // Changed automatically
            assertAttribute(attribute2, "attr-2", element2); // Changed automatically
            assertAttribute(attribute3, "attr-3", element2); // Changed automatically
            assertAttribute(attribute4, "attr-4", element2); // Changed automatically
            assertAttribute(attribute5, "attr-5", element2);
        }
        
        {
            /*
             * Advance functionality:
             * Remove all the attributes that end with even number from the 
             * KeySet view of the property "attributes" of "element2".
             * 
             * (1) The property "name" of attribute2 and attribute4 will be 
             * changed to be null automatically and implicitly.
             * (2) The property "element" of attribute2 and attribute4 will be
             * changed to be null automatically and implicitly.
             */
            Iterator<String> itr = element2.getAttributes().keySet().iterator();
            while(itr.hasNext()) {
                String name = itr.next();
                if (Integer.parseInt(name.substring(name.indexOf('-') + 1)) % 2 == 0) {
                    itr.remove(); //Can ONLY invoke remove of iterator, can NOT invoke remove of collection
                }
            }
            assertElement(element1);
            assertElement(element2, attribute5, attribute1, attribute3); // Changed by you
            assertAttribute(attribute1, "attr-1", element2);
            assertAttribute(attribute2, null, null); // Changed automatically
            assertAttribute(attribute3, "attr-3", element2);
            assertAttribute(attribute4, null, null); // Changed automatically
            assertAttribute(attribute5, "attr-5", element2);
        }
        
        {
            /*
             * Set the property "element" of attribute1 to be null.
             * The property "employees" of element2 will be changed,
             * the map entry { key: "attr-1", value: attribute1 } will be removed automatically and implicitly.
             */
            attribute1.setElement(null);
            
            assertElement(element1);
            assertElement(element2, attribute5, attribute3); // Changed automatically
            assertAttribute(attribute1, null, null); // parent is changed by you, key is changed automatically.
            assertAttribute(attribute2, null, null);
            assertAttribute(attribute3, "attr-3", element2);
            assertAttribute(attribute4, null, null);
            assertAttribute(attribute5, "attr-5", element2);
        }
        
        {
            /*
             * Advance functionality: Unstable Collection Elements.
             * change the property "name" of attribute5 from "attr-5" to "attr-X".
             *
             * The property "attributes" of the parent object element2 will be changed,
             * the old map entry { key: "attr-5", value: attribute5 } will be removed
             * and a new map entry { key: "attr-X", value: attribute5 } will be added
             * automatically and implicitly.
             * (This demo uses org.babyfish.collection.XOrderedMap, so the removed and re-added 
             * child object attribute5 will became this last child of the map) 
             */
            attribute5.setName("attr-X");
            
            assertElement(element1);
            
            /*
             * Changed automatically, but can't see the difference
             */
            assertElement(element2, attribute3, attribute5); 
            /*
             * But the difference can be seen from these three statments.
             */
            Assert.assertEquals("[attr-3, attr-X]", element2.getAttributes().keySet().toString()); 
            Assert.assertTrue(element2.getAttributes().containsKey("attr-X"));
            Assert.assertFalse(element2.getAttributes().containsKey("attr-5")); 
            
            assertAttribute(attribute1, null, null);
            assertAttribute(attribute2, null, null);
            assertAttribute(attribute3, "attr-3", element2);
            assertAttribute(attribute4, null, null);
            assertAttribute(attribute5, "attr-X", element2); // Changed by you
        }
        
        {
            /*
             * Advance functionality:
             * change the property "name" of attribute3 from "attr-3" to "attr-X"
             * which is already a key of the map "element2.getAttributes()".
             *
             * The property "attributes" of the parent object element2 will be changed 
             * automatically and implicitly.
             * 
             * (1) The old map entry { key: "attr-3", value: attribute3 } will be removed
             * and a new map entry { key: "attr-X", value: attribute3 } will be added
             * automatically and implicitly.
             * 
             * (2) The old map entry { key: "attr-X", value: attribute5 } will be removed
             * automatically and implicitly because its key is conflict with the newest
             * name of attribute3. Both the existing name of attribute5 and the newest name of
             * attribute3 are "attr-X", attribute3 is the current modified object but attribute5
             * is NOT, so ObjectModel keeps attribute3 and removes attribute5 to resolve the 
             * name conflict to guarantee the key unique constraint of java.util.Map<K, V>.
             * 
             * (3) The property "name" and "element" of attribute5 will be changed to be null
             * automatically and implicitly, because attribute3 will be removed, that has been
             * disuccssed in (2).
             */
            attribute3.setName("attr-X");
            
            assertElement(element1);
            
            /*
             * Changed automatically
             * (1) attribute5 is removed, this can be seen easily
             * (2) attribute3 is refreshed(removed and add again), but it's not easy to be seen. 
             */
            assertElement(element2, attribute3);
            /*
             * The automatical refreshing of attributes can be seen by these three statements
             */
            Assert.assertEquals("[attr-X]", element2.getAttributes().keySet().toString());
            Assert.assertTrue(element2.getAttributes().containsKey("attr-X"));
            Assert.assertFalse(element2.getAttributes().containsKey("attr-3")); 
            
            assertAttribute(attribute1, null, null);
            assertAttribute(attribute2, null, null);
            assertAttribute(attribute3, "attr-X", element2); // Changed by you
            assertAttribute(attribute4, null, null);
            assertAttribute(attribute5, null, null);
        }
        
        {
            /*
             * Set the property "name" of attribute3 to be null.
             * The property "employees" of element2 will be changed,
             * the map entry { key: "attr-X", value: attribute3 } will be removed automatically and implicitly.
             * (In ObjectModel, assocation map can not contain null keys, assgins the key to be null means 
             * you want to destroy the association)
             */
            attribute3.setName(null);
            
            assertElement(element1);
            assertElement(element2); // Changed automatically
            assertAttribute(attribute1, null, null);
            assertAttribute(attribute2, null, null);
            assertAttribute(attribute3, null, null); // key(name) is changed by you, parent(element) is changed automatically.
            assertAttribute(attribute4, null, null);
            assertAttribute(attribute5, null, null);
        }
    }
 
    //Parameter attributes can not contains duplicated attributes
    //This is test code, not main code, so I does not validate the parameter.
    private static void assertElement(Element element, Attribute ... attributes) {
        Assert.assertEquals(attributes.length, element.getAttributes().size());
        int index = 0;
        //The element.getAttributes() is an instanceof org.babyfish.collection.MALinkedHashMap<K, V>
        //that is a implementation of org.babyfish.collection.XOrderedMap<K, V>, 
        //so we can assert its key/value pair by strict order. 
        for (Entry<String, Attribute> entry : element.getAttributes().entrySet()) {
            Assert.assertEquals(entry.getKey(), entry.getValue().getName());
            Assert.assertSame(attributes[index++], entry.getValue());
        }
    }
    
    private static void assertAttribute(Attribute attribute, String name, Element element) {
        Assert.assertEquals(name, attribute.getName());
        Assert.assertSame(element, attribute.getElement());
    }
}
