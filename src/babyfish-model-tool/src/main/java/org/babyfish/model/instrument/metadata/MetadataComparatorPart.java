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
package org.babyfish.model.instrument.metadata;

import org.babyfish.model.NullComparatorType;
import org.babyfish.model.StringComparatorType;

/**
 * @author Tao Chen
 */
public class MetadataComparatorPart {
    
    private MetadataProperty property;
    
    private StringComparatorType stringComparatorType;
    
    private NullComparatorType nullComparatorType;
    
    public MetadataComparatorPart(
            MetadataProperty property, 
            StringComparatorType stringComparatorType,
            NullComparatorType nullComparatorType) {
        this.property = property;
        this.stringComparatorType = stringComparatorType;
        this.nullComparatorType = nullComparatorType;
    }

    public MetadataProperty getProperty() {
        return this.property;
    }
    
    public StringComparatorType getStringComparatorType() {
        return this.stringComparatorType;
    }
    
    public NullComparatorType getNullComparatorType() {
        return this.nullComparatorType;
    }
}
