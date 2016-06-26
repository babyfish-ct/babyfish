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
package org.babyfish.lang;

import java.io.Serializable;

/**
 * @author Tao Chen
 */
public interface ReferenceComparator<T> {

    boolean same(T obj1, T obj2);
    
    @SuppressWarnings("unchecked")
    static <T> ReferenceComparator<T> empty() {
        return (ReferenceComparator<T>)EmptyReferenceComparator.INSTANCE;
    }
    
    static <T> ReferenceComparator<T> nullToEmpty(ReferenceComparator<T> referenceComparator) {
        if (referenceComparator == null) {
            return empty();
        }
        return referenceComparator;
    }
    
    static <T> ReferenceComparator<T> emptyToNull(ReferenceComparator<T> referenceComparator) {
        if (referenceComparator == EmptyReferenceComparator.INSTANCE) {
            return null;
        }
        return referenceComparator;
    }
}

class EmptyReferenceComparator<T> extends Singleton implements ReferenceComparator<T>, Serializable {

    static EmptyReferenceComparator<?> INSTANCE = 
            EmptyReferenceComparator.getInstance(EmptyReferenceComparator.class);
    
    @Override
    public boolean same(T obj1, T obj2) {
        return obj1 == obj2;
    }
}
