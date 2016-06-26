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

/**
 * @author Tao Chen
 */
import org.babyfish.collection.XOrderedMap;
import org.babyfish.model.Association;
import org.babyfish.model.Model;
import org.babyfish.model.Scalar;

@Model
public class Canvas extends Component {

    private static final long serialVersionUID = 584457444708145732L;

    @Scalar
    private long deviceNo;
    
    @Scalar
    private double width;
    
    @Scalar
    private double height;
    
    @Association(opposite = "canvas")
    private XOrderedMap<String, Shape> shapes;

    public long getDeviceNo() {
        return deviceNo;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public XOrderedMap<String, Shape> getShapes() {
        return shapes;
    }

    public void setShapes(XOrderedMap<String, Shape> shapes) {
        this.shapes = shapes;
    }
}
