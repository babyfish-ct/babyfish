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

import org.babyfish.lang.Equality;
import org.babyfish.lang.OverrideEquality;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class InterfaceEqualityTest {

    @Test
    public void testRectangeAndRectangle() {
        Assert.assertTrue(Rectangles.rectangle(1, 2).equals(Rectangles.rectangle(1, 2)));
        Assert.assertFalse(Rectangles.rectangle(1, 2).equals(Rectangles.rectangle(2, 2)));
    }
    
    @Test
    public void testDrawableRectangeAndDrawableRectangle() {
        Assert.assertTrue(Rectangles.drawableRectangle(1, 2, 0xffff0000).equals(Rectangles.drawableRectangle(1, 2, 0xffff0000)));
        Assert.assertFalse(Rectangles.drawableRectangle(1, 2, 0xffff0000).equals(Rectangles.drawableRectangle(1, 2, 0x0000ffff)));
    }
    
    @Test
    public void testRectangeProxyAndRectangle() {
        Assert.assertTrue(Rectangles.rectangleProxy(Rectangles.rectangle(1, 2)).equals(Rectangles.rectangle(1, 2)));
    }
    
    @Test
    public void testRectangeAndRectangleProxy() {
        Assert.assertTrue(Rectangles.rectangle(1, 2).equals(Rectangles.rectangleProxy(Rectangles.rectangle(1, 2))));
    }
    
    @Test
    public void testDrawableRectangeAndRectangle() {
        Assert.assertFalse(Rectangles.drawableRectangle(1, 2, 0xffff0000).equals(Rectangles.rectangle(1, 2)));
    }
    
    @Test
    public void testRectangeAndDrawableRectangle() {
        Assert.assertFalse(Rectangles.rectangle(1, 2).equals(Rectangles.drawableRectangle(1, 2, 0xffff0000)));
    }
    
    interface Rectangle {
        
        int getWidth();
        
        int getHeight();
        
        @OverrideEquality
        @Override
        boolean equals(Object obj);
        
    }
    
    interface DrawableRectangle extends Rectangle {
        
        int getA8r8g8b8();
        
        @OverrideEquality
        @Override
        boolean equals(Object obj);
        
    }
    
    static class Rectangles {
        
        private Rectangles() {
            
        }
        
        public static Rectangle rectangle(final int width, final int height) {
            return new Rectangle() {

                @Override
                public int getWidth() {
                    return width;
                }

                @Override
                public int getHeight() {
                    return height;
                }

                @Override
                public int hashCode() {
                    return width ^ height;
                }

                @Override
                public boolean equals(Object obj) {
                    Equality<Rectangle> equality = Equality.of(Rectangle.class, this, obj);
                    Rectangle other = equality.other();
                    if (other == null) {
                        return equality.returnValue();
                    }
                    return width == other.getWidth() && height == other.getHeight();
                }
                
            };
        }
        
        public static DrawableRectangle drawableRectangle(
                final int width, final int height, final int a8r8g8b8) {
            return new DrawableRectangle() {

                @Override
                public int getWidth() {
                    return width;
                }

                @Override
                public int getHeight() {
                    return height;
                }
                
                @Override
                public int getA8r8g8b8() {
                    return a8r8g8b8;
                }

                @Override
                public int hashCode() {
                    return width ^ height ^ a8r8g8b8;
                }

                @Override
                public boolean equals(Object obj) {
                    Equality<DrawableRectangle> equality = Equality.of(DrawableRectangle.class, this, obj);
                    DrawableRectangle other = equality.other();
                    if (other == null) {
                        return equality.returnValue();
                    }
                    return 
                            width == other.getWidth() && 
                            height == other.getHeight() &&
                            a8r8g8b8 == other.getA8r8g8b8();
                }
                
            };
        }
        
        public static Rectangle rectangleProxy(final Rectangle rectangle) {
            
            if (rectangle == null) {
                throw new NullPointerException();
            }
            return new Rectangle() {
                
                @Override
                public int getWidth() {
                    return rectangle.getWidth();
                }
                
                @Override
                public int getHeight() {
                    return rectangle.getHeight();
                }

                @Override
                public int hashCode() {
                    return rectangle.hashCode();
                }

                @Override
                public boolean equals(Object obj) {
                    return rectangle.equals(obj);
                }
                
            };
        } 
    }
    
}
