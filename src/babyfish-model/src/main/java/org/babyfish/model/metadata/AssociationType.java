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

/**
 * @author Tao Chen
 */
public enum AssociationType {
    NONE(false, false),
    REFERENCE(true, false),
    INDEXED_REFERENCE(true, false),
    KEYED_REFERENCE(true, false),
    COLLECTION(false, true),
    LIST(false, true),
    MAP(false, true);
    
    private boolean reference;
    
    private boolean collection;

    private AssociationType(boolean reference, boolean collection) {
        this.reference = reference;
        this.collection = collection;
    }

    public boolean isReference() {
        return this.reference;
    }

    public boolean isCollection() {
        return this.collection;
    }
}
