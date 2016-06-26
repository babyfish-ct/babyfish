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
package org.babyfish.model.ui.entities;

import org.babyfish.model.Model;
import org.babyfish.model.ModelType;
import org.babyfish.model.Scalar;

/**
 * @author Tao Chen
 */
@Model(type = ModelType.EMBEDDABLE)
public class Color {

    @Scalar
    private float alpha;
    
    @Scalar
    private float red;
    
    @Scalar
    private float green;
    
    @Scalar
    private float blue;

    public Color(float red, float green, float blue) {
        this.alpha = 1.0F;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Color(float alpha, float red, float green, float blue) {
        this.alpha = alpha;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
    
    public Color(Color color) {
        if (color != null) {
            this.alpha = color.alpha;
            this.red = color.red;
            this.green = color.green;
            this.blue = color.blue;
        } else {
            this.alpha = 1.0F;
        }
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getRed() {
        return red;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public float getGreen() {
        return green;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public float getBlue() {
        return blue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }
}
