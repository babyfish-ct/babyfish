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

import org.babyfish.lang.Equality;
import org.babyfish.lang.OverrideEquality;

/**
 * @author Tao Chen
 */
public class Point2D {

    private int x;
    
    private int y;
    
    public Point2D() {}

    public Point2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int hashCode() {
        return this.x * 31 + this.y;
    }

    /*
     * The "@OverrideEquality" means this "equals" method 
     * overrides the real rule of equality.
     */
    @OverrideEquality
    @Override
    public boolean equals(Object obj) {
        
        Equality<Point2D> equality = Equality.of(Point2D.class, this, obj);
        Point2D other = equality.other();
        if (other == null) {
            return equality.returnValue();
        }
        
        /*
         * Notes: 
         * (1) Left side of "==" can use field to optimize the performance
         * (2) Right side of "==" must use getter because "other" may be proxy(For example: Point2DReadOnlyProxy)
         */
        return this.x == other.getX() && this.y == other.getY();
    }
}
