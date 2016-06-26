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
package org.babyfishdemo.om4java.dom;

import org.babyfish.data.ModificationException;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class ValidatorTest {

    @Test
    public void testValidatorByCollection() {
        try {
            new Element("div").getChildNodes().add(new Attribute("style", "margin:0px;padding:10px;"));
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(
                    "The child node of Element can only be Element, Text or Comment", 
                    ex.getMessage()
            );
            return;
        }
        Assert.fail("Cannot add attribute to the childNodes of element, an exception should be raised");
    }
    
    @Test
    public void testValidatorByReference() {
        try {
            Attribute attribute = new Attribute();
            attribute.setIndex(0);
            attribute.setParentNode(new Element("div"));
        } catch (ModificationException ex) {
            Throwable cause = ex.getCause();
            Assert.assertNotNull(cause);
            Assert.assertSame(IllegalArgumentException.class, cause.getClass());
            Assert.assertEquals(
                    "The child node of Element can only be Element, Text or Comment", 
                    cause.getMessage()
            );
            return;
        }
        Assert.fail("The Node.parentNode is not supported by Attribute, an exception should be raised");
    }
}
