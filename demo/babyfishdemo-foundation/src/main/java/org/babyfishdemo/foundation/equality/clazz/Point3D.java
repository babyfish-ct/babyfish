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
public class Point3D extends Point2D {
    
    private int z;

    public Point3D() {}

    public Point3D(int x, int y, int z) {
        super(x, y);
        this.z = z;
    }

    public int getZ() {
        return this.z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + this.z;
    }

    /*
     * The "@OverrideEquality" means this "equals" method 
     * overrides the real rule of equality.
     */
    @OverrideEquality
    @Override
    public boolean equals(Object obj) {
        Equality<Point3D> equality = Equality.of(Point3D.class, this, obj);
        Point3D other = equality.other();
        if (other == null) {
            return equality.returnValue();
        }
        
        /*
         * Notes: 
         * (1) Left side of "==" can use field to optimize the performance
         * (2) Right side of "==" must use getter because "other" may be proxy
         */
        return super.equals(obj) && 
                this.z == other.getZ();
    }
    
}
