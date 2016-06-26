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
package org.babyfish.test.util.reflect.runtime.other;

/**
 * @author Tao Chen
 */
public class Rectangle {
    
    private Point leftTop = new Point();
    
    private Point rightBottom = new Point();
    
    public void setBound(int left, int top, int right, int bottom) {
        this.leftTop.x = left;
        this.leftTop.y = top;
        this.rightBottom.x = right;
        this.rightBottom.y = bottom;
    }
    
    public int getArea() {
        int width = Math.abs(this.leftTop.x - this.rightBottom.x);
        int height = Math.abs(this.leftTop.y - this.rightBottom.y);
        return width * height;
    }
    
    public int getPerimeter() {
        int width = Math.abs(this.leftTop.diffX(this.rightBottom));
        int height = Math.abs(this.leftTop.diffY(this.rightBottom));
        return 2 * (width + height);
    }
    
    public float getAspectRatio() {
        Int[] arr = new Int[4];
        this.leftTop.fill(arr, 0);
        this.rightBottom.fill(arr, 2);
        return Math.abs((float)(arr[0].i - arr[2].i) / (arr[1].i - arr[3].i));
    }
    
    public Size getSize() {
        return new Size(
                Math.abs(this.leftTop.x - this.rightBottom.x),
                Math.abs(this.leftTop.y - this.rightBottom.y));
    }
    
    public static class Size {
        
        private int width;
        
        private int height;
        
        private Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        @Override
        public String toString() {
            return "(" +
                    this.width +
                    ", " +
                    this.height +
                    ")";
        }
    }
    
    private static class Point {
        
        int x;
        
        int y;
        
        int diffX(Point point) {
            return this.x - point.x;
        }
        
        int diffY(Point point) {
            return this.y - point.y;
        }
        
        void fill(Int[] arr, int offset) {
            arr[offset] = new Int(this.x);
            arr[offset + 1] = new Int(this.y);
        }
    }
    
    private static class Int {
        
        int i;
        
        Int(int i) {
            this.i = i;
        }
    }
}
