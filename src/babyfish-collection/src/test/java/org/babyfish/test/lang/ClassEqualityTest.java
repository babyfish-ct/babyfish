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
package org.babyfish.test.lang;

import junit.framework.Assert;

import org.babyfish.lang.Equality;
import org.babyfish.lang.OverrideEquality;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ClassEqualityTest {
    
    @Test
    public void testRectangeAndRectangle() {
        Assert.assertTrue(new Rectangle(1, 2).equals(new Rectangle(1, 2)));
        Assert.assertFalse(new Rectangle(1, 2).equals(new Rectangle(2, 2)));
    }
    
    @Test
    public void testDrawableRectangeAndDrawableRectangle() {
        Assert.assertTrue(new DrawableRectangle(1, 2, 0xffff0000).equals(new DrawableRectangle(1, 2, 0xffff0000)));
        Assert.assertFalse(new DrawableRectangle(1, 2, 0xffff0000).equals(new DrawableRectangle(1, 2, 0x0000ffff)));
    }
    
    @Test
    public void testRectangeProxyAndRectangle() {
        Assert.assertTrue(new RectangleProxy(new Rectangle(1, 2)).equals(new Rectangle(1, 2)));
    }
    
    @Test
    public void testRectangeAndRectangleProxy() {
        Assert.assertTrue(new Rectangle(1, 2).equals(new RectangleProxy(new Rectangle(1, 2))));
    }
    
    @Test
    public void testDrawableRectangeAndRectangle() {
        Assert.assertFalse(new DrawableRectangle(1, 2, 0xffff0000).equals(new Rectangle(1, 2)));
    }
    
    @Test
    public void testRectangeAndDrawableRectangle() {
        Assert.assertFalse(new Rectangle(1, 2).equals(new DrawableRectangle(1, 2, 0xffff0000)));
    }

    static class Rectangle {
        
        int width;
        
        int height;

        public Rectangle(int width, int height) {
            super();
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
        public int hashCode() {
            return this.width ^ this.height;
        }

        @OverrideEquality
        @Override
        public boolean equals(Object obj) {
            Equality<Rectangle> equality = Equality.of(Rectangle.class, this, obj);
            Rectangle other = equality.other();
            if (other == null) {
                return equality.returnValue();
            }
            return 
                this.width == other.getWidth() &&
                this.height == other.getHeight();
        }
        
    }
    
    static class RectangleProxy extends Rectangle {
        
        private Rectangle rectangle;

        public RectangleProxy(Rectangle rectangle) {
            super(0, 0);
            if (rectangle == null) {
                throw new NullPointerException();
            }
            this.rectangle = rectangle;
        }

        @Override
        public int getWidth() {
            return this.rectangle.getWidth();
        }

        @Override
        public int getHeight() {
            return this.rectangle.getHeight();
        }

        @Override
        public int hashCode() {
            return this.rectangle.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.rectangle.equals(obj);
        }
        
    }
    
    static class DrawableRectangle extends Rectangle {
        
        private int a8r8g8b8;

        public DrawableRectangle(int width, int height, int a8r8g8b8) {
            super(width, height);
            this.a8r8g8b8 = a8r8g8b8;
        }

        public int getA8r8g8b8() {
            return this.a8r8g8b8;
        }

        @Override
        public int hashCode() {
            return super.hashCode() ^ this.a8r8g8b8;
        }

        @OverrideEquality
        @Override
        public boolean equals(Object obj) {
            Equality<DrawableRectangle> equality = Equality.of(DrawableRectangle.class, this, obj);
            DrawableRectangle other = equality.other();
            if (other == null) {
                return equality.returnValue();
            }
            return super.equals(obj) && this.a8r8g8b8 == other.getA8r8g8b8();
        }
        
    }
    
}
