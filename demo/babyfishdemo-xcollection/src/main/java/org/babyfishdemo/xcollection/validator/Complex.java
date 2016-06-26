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
package org.babyfishdemo.xcollection.validator;

/**
 * @author Tao Chen
 */
public class Complex {
    
    private double real;
    
    private double image;
    
    public Complex(double real, double image) {
        this.real = real;
        this.image = image;
    }
    
    public double getReal() {
        return this.real;
    }

    public double getImage() {
        return this.image;
    }
    
    public double getAbs() {
        return Math.sqrt(this.real * this.real + this.image * this.image);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.real);
        if (this.image < 0) {
            builder.append(" - ").append(-this.image);
        } else {
            builder.append(" + ").append(+this.image);
        }
        builder.append('i');
        return builder.toString();
    }
}
