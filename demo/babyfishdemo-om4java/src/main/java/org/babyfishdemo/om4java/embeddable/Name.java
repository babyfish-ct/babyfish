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
package org.babyfishdemo.om4java.embeddable;

import org.babyfish.model.Model;
import org.babyfish.model.ModelType;
import org.babyfish.model.Scalar;

/**
 * @author Tao Chen
 */
/*
 * (1) Using ObjectModel4Java, requires compilation-time byte code instrument
 *
 * (2) This class is embeddable so that it cannot be used by other association property as target type, 
 *     it can only
 *     (a) be used by other scalar property
 *     (b) be used by other association property as map-key type
 */
@Model(type = ModelType.EMBEDDABLE)
public class Name {
    
    @Scalar
    private String first;
    
    @Scalar
    private String last;
    
    public Name(String first, String last) {
        this.first = first;
        this.last = last;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }
    
    @Override
    public String toString() {
        return String.format(
                "{ first: %s, last: %s }", 
                this.getFirst(),
                this.getLast()
        );
    }
}
