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
package org.babyfish.model.embedded;

import junit.framework.Assert;

import org.babyfish.model.embedded.entities.Color;
import org.babyfish.model.embedded.entities.ColorfulRectangle;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class InheritanceTest {

    @Test
    public void testEquals() {
        ColorfulRectangle colorfulRectangle1 = new ColorfulRectangle(100, 100, new Color(255, 255, 255, 255));
        ColorfulRectangle colorfulRectangle2 = new ColorfulRectangle(100, 100, new Color(255, 255, 255, 255));
        Assert.assertEquals(colorfulRectangle1.hashCode(), colorfulRectangle2.hashCode());
        Assert.assertEquals(colorfulRectangle1, colorfulRectangle2);
    }
    
    @Test
    public void testSuperDiff() {
        ColorfulRectangle colorfulRectangle1 = new ColorfulRectangle(100, 90, new Color(255, 255, 255, 255));
        ColorfulRectangle colorfulRectangle2 = new ColorfulRectangle(100, 100, new Color(255, 255, 255, 255));
        Assert.assertFalse(colorfulRectangle1.hashCode() == colorfulRectangle2.hashCode());
        Assert.assertFalse(colorfulRectangle1.equals(colorfulRectangle2));
    }
    
    @Test
    public void testDerivedDiff() {
        ColorfulRectangle colorfulRectangle1 = new ColorfulRectangle(100, 100, new Color(255, 0, 255, 255));
        ColorfulRectangle colorfulRectangle2 = new ColorfulRectangle(100, 100, new Color(255, 255, 255, 255));
        Assert.assertFalse(colorfulRectangle1.hashCode() == colorfulRectangle2.hashCode());
        Assert.assertFalse(colorfulRectangle1.equals(colorfulRectangle2));
    }
    
    @Test
    public void testBothDiff() {
        ColorfulRectangle colorfulRectangle1 = new ColorfulRectangle(100, 90, new Color(255, 0, 255, 255));
        ColorfulRectangle colorfulRectangle2 = new ColorfulRectangle(100, 100, new Color(255, 255, 255, 255));
        Assert.assertFalse(colorfulRectangle1.hashCode() == colorfulRectangle2.hashCode());
        Assert.assertFalse(colorfulRectangle1.equals(colorfulRectangle2));
    }
}
