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
package org.babyfish.model.nonready;

import org.babyfish.model.nonready.entities.ChildNode;
import org.babyfish.model.nonready.entities.ParentNode;
import org.babyfish.model.nonready.entities.Person;
import org.babyfish.model.nonready.entities.XChildNode;
import org.babyfish.model.nonready.entities.XParentNode;
import org.babyfish.model.spi.ObjectModelProvider;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class DisablityTest {
    
    @Test
    public void testImplicitlyEnable() {
        Person person = new Person();
        person.disableAddress();
        Assert.assertTrue(person.isAddressDisabled());
        person.setAddress("Unknown");
        Assert.assertFalse(person.isAddressDisabled());
        Assert.assertEquals("Unknown", person.getAddress());
    }
    
    @Test
    public void testExplicitlyEnable() {
        Person person = new Person();
        person.disableAddress();
        Assert.assertTrue(person.isAddressDisabled());
        person.enableAddress();
        Assert.assertFalse(person.isAddressDisabled());
        Assert.assertNull(person.getAddress());
    }
    
    @Test
    public void testImplicitlyEnableByOMAPI() {
        Person person = new Person();
        person.disableAddress();
        Assert.assertTrue(((ObjectModelProvider)person).objectModel().isDisabled(Person.ADDRESS_ID));
        ((ObjectModelProvider)person).objectModel().set(Person.ADDRESS_ID, "Unknown");
        Assert.assertFalse(((ObjectModelProvider)person).objectModel().isDisabled(Person.ADDRESS_ID));
        Assert.assertEquals("Unknown", ((ObjectModelProvider)person).objectModel().get(Person.ADDRESS_ID));
    }
    
    @Test
    public void testExplicitlyEnableByOMAPI() {
        Person person = new Person();
        person.disableAddress();
        Assert.assertTrue(((ObjectModelProvider)person).objectModel().isDisabled(Person.ADDRESS_ID));
        ((ObjectModelProvider)person).objectModel().enable(Person.ADDRESS_ID);
        Assert.assertFalse(((ObjectModelProvider)person).objectModel().isDisabled(Person.ADDRESS_ID));
        Assert.assertNull(((ObjectModelProvider)person).objectModel().get(Person.ADDRESS_ID));
    }
    
    @Test
    public void testContravariance() {
        Assert.assertTrue(XParentNode.CHILD_NODES_ID != XParentNode.X_CHILD_NODES_ID);
        Assert.assertTrue(XChildNode.PARENT_NODE_ID != XChildNode.X_PARENT_NODE_ID);
    }
    
    @Test
    public void testEnableAssociationByParentNode() {
        
        XParentNode parentNode = new XParentNode();
        XChildNode childNode = new XChildNode();
        ((ObjectModelProvider)parentNode).objectModel().disable(ParentNode.CHILD_NODES_ID);
        ((ObjectModelProvider)childNode).objectModel().disable(ChildNode.PARENT_NODE_ID);
        
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(XParentNode.X_CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(XChildNode.X_PARENT_NODE_ID));
        
        parentNode.getChildNodes().add(childNode);
        
        Assert.assertFalse(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertFalse(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertFalse(((ObjectModelProvider)parentNode).objectModel().isDisabled(XParentNode.X_CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(XChildNode.X_PARENT_NODE_ID));
    }
    
    @Test
    public void testEnableAssociationByXParentNode() {
        
        XParentNode parentNode = new XParentNode();
        XChildNode childNode = new XChildNode();
        ((ObjectModelProvider)parentNode).objectModel().disable(ParentNode.CHILD_NODES_ID);
        ((ObjectModelProvider)childNode).objectModel().disable(ChildNode.PARENT_NODE_ID);
        
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(XParentNode.X_CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(XChildNode.X_PARENT_NODE_ID));
        
        parentNode.getXChildNodes().add(childNode);
        
        Assert.assertFalse(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertFalse(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertFalse(((ObjectModelProvider)parentNode).objectModel().isDisabled(XParentNode.X_CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(XChildNode.X_PARENT_NODE_ID));
    }
    
    @Test
    public void testEnableAssociationByChildNode() {
        
        XParentNode parentNode = new XParentNode();
        XChildNode childNode = new XChildNode();
        ((ObjectModelProvider)parentNode).objectModel().disable(ParentNode.CHILD_NODES_ID);
        ((ObjectModelProvider)childNode).objectModel().disable(ChildNode.PARENT_NODE_ID);
        
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(XParentNode.X_CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(XChildNode.X_PARENT_NODE_ID));
        
        childNode.setParentNode(parentNode);
        
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(XParentNode.X_CHILD_NODES_ID));
        Assert.assertFalse(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertFalse(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertFalse(((ObjectModelProvider)childNode).objectModel().isDisabled(XChildNode.X_PARENT_NODE_ID));
    }
    
    @Test
    public void testEnableAssociationByXChildNode() {
        
        XParentNode parentNode = new XParentNode();
        XChildNode childNode = new XChildNode();
        ((ObjectModelProvider)parentNode).objectModel().disable(ParentNode.CHILD_NODES_ID);
        ((ObjectModelProvider)childNode).objectModel().disable(ChildNode.PARENT_NODE_ID);
        
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(XParentNode.X_CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertTrue(((ObjectModelProvider)childNode).objectModel().isDisabled(XChildNode.X_PARENT_NODE_ID));
        
        childNode.setXParentNode(parentNode);
        
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(ParentNode.CHILD_NODES_ID));
        Assert.assertTrue(((ObjectModelProvider)parentNode).objectModel().isDisabled(XParentNode.X_CHILD_NODES_ID));
        Assert.assertFalse(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertFalse(((ObjectModelProvider)childNode).objectModel().isDisabled(ChildNode.PARENT_NODE_ID));
        Assert.assertFalse(((ObjectModelProvider)childNode).objectModel().isDisabled(XChildNode.X_PARENT_NODE_ID));
    }
}
