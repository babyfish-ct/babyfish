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
package org.babyfish.model.spi.reference.event;

import org.babyfish.data.event.PropertyVersion;
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public class IndexedValueEvent<T> extends ValueEvent<T> {

    private static final long serialVersionUID = 6006846668575505847L;

    private int detachedIndex;
    
    private int attachedIndex;

    protected IndexedValueEvent(
            Object source, 
            Modification<T> modification, 
            T detachedValue, 
            T attachedValue,
            int detachedIndex,
            int attachedIndex) {
        super(source, modification, detachedValue, attachedValue);
        this.detachedIndex = detachedIndex;
        this.attachedIndex = attachedIndex;
    }

    protected IndexedValueEvent(Object source, IndexedValueEvent<T> target) {
        super(source, target);
        this.detachedIndex = target.detachedIndex;
        this.attachedIndex = target.attachedIndex;
    }
    
    public int getIndex(PropertyVersion version) {
        switch (Arguments.mustNotBeNull("version", version)) {
        case DETACH:
            return this.detachedIndex;
        case ATTACH:
            return this.attachedIndex;
        default:
            throw new AssertionError("Internal bug");
        }
    }
    
    @Override
    public IndexedValueEvent<T> dispatch(Object source) {
        return new IndexedValueEvent<>(source, this);
    }
    
    public static <T> IndexedValueEvent<T> createReplaceEvent(
            Object source, 
            Modification<T> modification, 
            T detachedValue, 
            T attachedValue,
            int detachedIndex,
            int attachedIndex) {
        return new IndexedValueEvent<T>(
                source, 
                modification, 
                detachedValue, 
                attachedValue, 
                detachedIndex, 
                attachedIndex
        );
    }
}
