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
package org.babyfishdemo.foundation.equality.clazz;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Tao Chen
 */
public class PointEqualityTest {

    /*
     * java.lang.Object supports "boolean equals(Object)" and you can
     * override it to check whether two objects are equal.
     * 
     * Unfortunately, in the strict sense of the word, it is impossible
     * to implement this method correctly!
     * 
     * There are 2 classic implementation styles:
     * 
     * (1) Too strict style
     *      @Override
     *      public boolean equals(Object obj) {
     *          if (this == obj) {
     *              return true;
     *          }
     *          if (obj == null || this.getClass() != obj.getClass()) {
     *              return false;
     *          }
     *          <<ThisClass>> other = (<<ThisClass>>)obj;
     *          return ...; 
     *      }
     * 
     * (2) Too lax style
     *      @Override
     *      public boolean equals(Object obj) {
     *          if (this == obj) {
     *              return true;
     *          }
     *          if (!(obj instanceof <<ThisClass>>) {
     *              return false;
     *          }
     *          <<ThisClass>> other = (<<ThisClass>>)obj;
     *          return ...; 
     *      }
     * 
     * Unfortunately, both of them are not perfect. 
     * (a) If you choose style-1, "testEquals()" of this test class can not be passed
     * (b) If you choose style-2, "testNotEquals()" of this test class can not be passed
     * 
     * So, babyfish supports a new style to implement the "equals" method,
     * it can make both all the methods of this test class can be passed.
     * 
     * Please find the difference between "Point3D.equals(Object)" and "Point2DProxy.equals(Object)"
     */
    
    
    
    @Test
    public void testNotEquals() {
        Point2D a = new Point2D(3, 4);
        Point3D b = new Point3D(3, 4, 5);
        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(b.equals(a));
    }
    
    @Test
    public void testEquals() {
        Point2D a = new Point2D(3, 4);
        Point2D b = new Point2DReadOnlyProxy(new Point2D(3, 4));
        Assert.assertTrue(a.equals(b));
        Assert.assertTrue(b.equals(a));
    }
}
