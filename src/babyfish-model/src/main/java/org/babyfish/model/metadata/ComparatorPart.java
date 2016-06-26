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
package org.babyfish.model.metadata;

import java.io.Serializable;

import org.babyfish.model.NullComparatorType;
import org.babyfish.model.StringComparatorType;

/**
 * @author Tao Chen
 */
public class ComparatorPart implements Serializable {

    private static final long serialVersionUID = 242131029654811788L;

    private int scalarPropertyId;
    
    private StringComparatorType stringComparatorType;

    private NullComparatorType nullComparatorType;
    
    private transient int hash;
    
    private transient String str;
    
    public ComparatorPart(int scalarPropertyId) {
        this.scalarPropertyId = scalarPropertyId;
        this.stringComparatorType = StringComparatorType.SENSITIVE;
        this.nullComparatorType = NullComparatorType.NULLS_FIRST;
    }
    
    public ComparatorPart(int scalarPropertyId, NullComparatorType nullComparatorType) {
        if (nullComparatorType == null) {
            nullComparatorType = NullComparatorType.NULLS_FIRST;
        }
        this.scalarPropertyId = scalarPropertyId;
        this.stringComparatorType = StringComparatorType.SENSITIVE;
        this.nullComparatorType = nullComparatorType;
    }
    
    public ComparatorPart(int scalarPropertyId, StringComparatorType stringComparatorType) {
        if (stringComparatorType == null) {
            stringComparatorType = StringComparatorType.SENSITIVE;
        }
        this.scalarPropertyId = scalarPropertyId;
        this.stringComparatorType = stringComparatorType;
        this.nullComparatorType = NullComparatorType.NULLS_FIRST;
    }
    
    public ComparatorPart(
            int scalarPropertyId, 
            StringComparatorType stringComparatorType,
            NullComparatorType nullComparatorType) {
        if (stringComparatorType == null) {
            stringComparatorType = StringComparatorType.SENSITIVE;
        }
        if (nullComparatorType == null) {
            nullComparatorType = NullComparatorType.NULLS_FIRST;
        }
        this.scalarPropertyId = scalarPropertyId;
        this.stringComparatorType = stringComparatorType;
        this.nullComparatorType = nullComparatorType;
    }

    public int getScalarPropertyId() {
        return this.scalarPropertyId;
    }

    public StringComparatorType getStringComparatorType() {
        return this.stringComparatorType;
    }
    
    public NullComparatorType getNullComparatorType() {
        return this.nullComparatorType;
    }
    
    @Override
    public int hashCode() {
        int h = this.hash;
        if (h == 0) {
            h = this.scalarPropertyId;
            h = 31 * h + this.stringComparatorType.hashCode();
            h = 31 * h + this.nullComparatorType.hashCode();
            if (h == 0) {
                h = -1;
            }
            this.hash = h;
        }
        return h;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != ComparatorPart.class) {
            return false;
        }
        ComparatorPart other = (ComparatorPart)obj;
        return 
                this.scalarPropertyId == other.scalarPropertyId &&
                this.stringComparatorType == other.stringComparatorType &&
                this.nullComparatorType == other.nullComparatorType;
    }
    
    @Override
    public String toString() {
        String s = this.str;
        if (s == null) {
            this.str = s =
                    "{ scalarPropertyId: "
                    + this.scalarPropertyId
                    + ", stringComparatorType: "
                    + this.stringComparatorType
                    + ", nullComparatorType: "
                    + this.nullComparatorType
                    + " }";
        }
        return s;
    }
}
