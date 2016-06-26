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
package org.babyfish.model.unidirectional;

import junit.framework.Assert;

import org.babyfish.model.unidirectional.entities.XManufacturer;
import org.babyfish.model.unidirectional.entities.XProduct;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class UnidirectionalAssociationTest {

    @Test
    public void testBaseSetter() {
        XProduct product = new XProduct();
        XManufacturer manufacturer = new XManufacturer();
        product.setManufacturer(manufacturer);
        Assert.assertSame(manufacturer, product.getManufacturer());
        Assert.assertSame(manufacturer, product.getXManufacturer());
    }
    
    @Test
    public void testExtensionSetter() {
        XProduct product = new XProduct();
        XManufacturer manufacturer = new XManufacturer();
        product.setXManufacturer(manufacturer);
        Assert.assertSame(manufacturer, product.getManufacturer());
        Assert.assertSame(manufacturer, product.getXManufacturer());
    }
}
