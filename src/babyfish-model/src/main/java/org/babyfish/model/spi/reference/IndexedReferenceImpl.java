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
package org.babyfish.model.spi.reference;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.ReferenceComparator;

/**
 * @author Tao Chen
 */
public class IndexedReferenceImpl<T> extends ReferenceImpl<T> implements IndexedReference<T> {
    
    private static final long serialVersionUID = 668682623359567724L;
    
    protected int index;
    
    public IndexedReferenceImpl() {
        this.index = INVALID_INDEX;
    }
    
    public IndexedReferenceImpl(T value) {
        super(value);
        this.index = INVALID_INDEX;
    }
    
    public IndexedReferenceImpl(int index, T value) {
        super(value);
        this.index = index;
    }
    
    public IndexedReferenceImpl(ReferenceComparator<? super T> comparator) {
        super(comparator);
        this.index = INVALID_INDEX;
    }
    
    public IndexedReferenceImpl(T value, ReferenceComparator<? super T> comparator) {
        super(value, comparator);
        this.index = INVALID_INDEX;
    }
    
    public IndexedReferenceImpl(int index, T value, ReferenceComparator<? super T> comparator) {
        super(value, comparator);
        this.index = index;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object value, boolean absolute) {
        ReferenceComparator<? super T> comparator = this.comparator();
        if (comparator == null) {
            return this.get(absolute) == value;
        }
        return comparator.same(this.get(absolute), (T)value);
    }

    @Override
    public int getIndex() {
        return this.getIndex(false);
    }

    @Override
    public int getIndex(boolean absolute) {
        if (absolute) {
            return this.index;
        }
        return this.get(true) == null ? INVALID_INDEX : this.index;
    }

    @Override
    public int setIndex(int index) {
        Arguments.mustBeGreaterThanOrEqualToValue("index", index, INVALID_INDEX);
        int oldIndex = this.index;
        this.index = index;
        return oldIndex;
    }
    
    @Override
    public T get(boolean absolute) {
        if (absolute) {
            return super.get(true);
        }
        return this.index == INVALID_INDEX ? null : super.get(true);
    }

    @Override
    public T set(int index, T value) {
        this.index = index;
        return super.set(value);
    }

    @Override
    public int hashCode() {
        return this.index + super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IndexedReference<?>)) {
            return false;
        }
        IndexedReference<?> other = (IndexedReference<?>)obj;
        return this.index == other.getIndex(true) && this.get(true) == other.get(true);
    }

}
