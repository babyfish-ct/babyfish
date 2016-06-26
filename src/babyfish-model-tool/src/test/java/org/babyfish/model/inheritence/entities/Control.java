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
package org.babyfish.model.inheritence.entities;

import java.io.Serializable;
import java.util.List;

import org.babyfish.model.Association;
import org.babyfish.model.Model;
import org.babyfish.model.Scalar;

/**
 * @author Tao Chen
 */
@Model
public class Control implements Serializable {
    
    private static final long serialVersionUID = 5680087441645139218L;

    @Scalar
    private int width;
    
    @Scalar
    private int height;
    
    @Association(opposite = "parent")
    private List<Control> controls;
    
    @Association(opposite = "controls")
    private Control parent;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<Control> getControls() {
        return controls;
    }

    public void setControls(List<Control> controls) {
        this.controls = controls;
    }

    public Control getParent() {
        return parent;
    }

    public void setParent(Control parent) {
        this.parent = parent;
    }
}
