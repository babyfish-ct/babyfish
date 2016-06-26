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
package org.babyfish.model.event;

import org.babyfish.data.event.PropertyVersion;
import org.babyfish.model.event.entities.Name;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.metadata.ModelProperty;
import org.babyfish.model.spi.ObjectModelProvider;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class NameTest {

    @Test
    public void testScalarEvent() {
        Name name = new Name();
        ScalarListener listener = new ScalarListener() {

            private StringBuilder builder = new StringBuilder();
            
            @Override
            public void modifying(ScalarEvent e) {
                ModelProperty modelProperty = 
                        ModelClass.of(Name.class)
                        .getProperty(e.getScalarPropertyId());
                this
                .builder
                .append("pre(")
                .append(modelProperty.getName())
                .append(':')
                .append(e.getValue(PropertyVersion.DETACH))
                .append("->")
                .append(e.getValue(PropertyVersion.ATTACH))
                .append(");");
            }

            @Override
            public void modified(ScalarEvent e) {
                ModelProperty modelProperty = 
                        ModelClass.of(Name.class)
                        .getProperty(e.getScalarPropertyId());
                this
                .builder
                .append("post(")
                .append(modelProperty.getName())
                .append(':')
                .append(e.getValue(PropertyVersion.DETACH))
                .append("->")
                .append(e.getValue(PropertyVersion.ATTACH))
                .append(");");
            }
            
            @Override
            public String toString() {
                return this.builder.toString();
            }
            
        };
        
        name.setFirstName("Jim");
        Assert.assertEquals("", listener.toString());
        name.setLastName("Green");
        Assert.assertEquals("", listener.toString());
        
        (((ObjectModelProvider)name).objectModel()).addScalarListener(listener);
        name.setFirstName("Jim2");
        Assert.assertEquals(
                "pre(firstName:Jim->Jim2);" +
                "post(firstName:Jim->Jim2);", 
                listener.toString());
        name.setLastName("Green2");
        Assert.assertEquals(
                "pre(firstName:Jim->Jim2);" +
                "post(firstName:Jim->Jim2);" +
                "pre(lastName:Green->Green2);" +
                "post(lastName:Green->Green2);", 
                listener.toString());
        
        name.setFirstName("Jim3");
        Assert.assertEquals(
                "pre(firstName:Jim->Jim2);" +
                "post(firstName:Jim->Jim2);" +
                "pre(lastName:Green->Green2);" +
                "post(lastName:Green->Green2);" +
                "pre(firstName:Jim2->Jim3);" +
                "post(firstName:Jim2->Jim3);", 
                listener.toString());
        name.setLastName("Green3");
        Assert.assertEquals(
                "pre(firstName:Jim->Jim2);" +
                "post(firstName:Jim->Jim2);" +
                "pre(lastName:Green->Green2);" +
                "post(lastName:Green->Green2);" +
                "pre(firstName:Jim2->Jim3);" +
                "post(firstName:Jim2->Jim3);" +
                "pre(lastName:Green2->Green3);" +
                "post(lastName:Green2->Green3);", 
                listener.toString());
        
        (((ObjectModelProvider)name).objectModel()).removeScalarListener(listener);
        name.setFirstName("Jim4");
        Assert.assertEquals(
                "pre(firstName:Jim->Jim2);" +
                "post(firstName:Jim->Jim2);" +
                "pre(lastName:Green->Green2);" +
                "post(lastName:Green->Green2);" +
                "pre(firstName:Jim2->Jim3);" +
                "post(firstName:Jim2->Jim3);" +
                "pre(lastName:Green2->Green3);" +
                "post(lastName:Green2->Green3);", 
                listener.toString());
        name.setLastName("Green4");
        Assert.assertEquals(
                "pre(firstName:Jim->Jim2);" +
                "post(firstName:Jim->Jim2);" +
                "pre(lastName:Green->Green2);" +
                "post(lastName:Green->Green2);" +
                "pre(firstName:Jim2->Jim3);" +
                "post(firstName:Jim2->Jim3);" +
                "pre(lastName:Green2->Green3);" +
                "post(lastName:Green2->Green3);", 
                listener.toString());
    }
}
