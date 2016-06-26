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
package org.babyfishdemo.foundation.equality.interfaze;

import org.babyfish.lang.Equality;

/**
 * @author Tao Chen
 */
public class Points {

    private Points() {}
    
    public static Point2D of(int x, int y) {
        return new P2D(x, y);
    }
    
    public static Point3D of(int x, int y, int z) {
        return new P3D(x, y, z);
    }
    
    public static Point2D readOnly(Point2D target) {
        return new P2DReadOnlyProxy(target);
    }
    
    private static class P2D implements Point2D {
        
        private int x;
        
        private int y;

        public P2D(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        @Override
        public int hashCode() {
            return this.x * 31 + this.y;
        }

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
    
    private static class P3D extends P2D implements Point3D {

        private int z;
        
        public P3D(int x, int y, int z) {
            super(x, y);
            this.z = z;
        }

        @Override
        public int getZ() {
            return this.z;
        }

        @Override
        public void setZ(int z) {
            this.z = z;
        }

        @Override
        public int hashCode() {
            return super.hashCode() * 31 + this.z;
        }

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
            return super.equals(obj) && this.z == other.getZ();
        }
    }
    
    private static class P2DReadOnlyProxy implements Point2D {
        
        private Point2D target;

        public P2DReadOnlyProxy(Point2D target) {
            this.target = target;
        }

        public int getX() {
            return target.getX();
        }

        public void setX(int x) {
            throw new UnsupportedOperationException();
        }

        public int getY() {
            return target.getY();
        }

        public void setY(int y) {
            throw new UnsupportedOperationException();
        }

        public int hashCode() {
            return target.hashCode();
        }

        public boolean equals(Object obj) {
            return target.equals(obj);
        }
    }
}
