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

import java.io.Serializable;

import org.babyfish.lang.ReferenceComparator;
import org.babyfish.validator.Validator;
import org.babyfish.validator.Validators;

/**
 * @author Tao Chen
 */
public class ReferenceImpl<T> implements Reference<T>, Serializable {
    
    private static final long serialVersionUID = 5629382981485975410L;

    protected T value;
    
    protected ReferenceComparator<? super T> comparator;
    
    protected Validator<? super T> validator;
    
    public ReferenceImpl() {
        
    }
    
    public ReferenceImpl(ReferenceComparator<? super T> comparator) {
        this(null, comparator);
    }
    
    public ReferenceImpl(T value) {
        this(value, null);
    }
    
    public ReferenceImpl(T value, ReferenceComparator<? super T> comparator) {
        this.value = value;
        this.comparator = ReferenceComparator.emptyToNull(comparator);
    }
    
    @Override
    public ReferenceComparator<? super T> comparator() {
        return ReferenceComparator.nullToEmpty(this.comparator);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void addValidator(Validator<? super T> validator) {
        this.validator = Validators.combine((Validator)this.validator, (Validator)validator);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void removeValidator(Validator<? super T> validator) {
        this.validator = Validators.remove((Validator)this.validator, (Validator)validator);
    }

    @Override
    public void validate(T value) {
        Validator<? super T> validator = this.validator;
        if (validator != null) {
            validator.validate(value);
        }
    }

    @Override
    public boolean contains(Object value) {
        return this.contains(value, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object value, boolean absolute) {
        ReferenceComparator<? super T> comparator = this.comparator;
        if (comparator == null) {
            return this.value == value;
        }
        return comparator.same(this.value, (T)value);
    }

    @Override
    public final T get() {
        return this.get(false);
    }
    
    @Override
    public T get(boolean absolute) {
        return this.value;
    }

    @Override
    public T set(T value) {
        T oldValue = this.value;
        if (oldValue != value) {
            if (this.validator != null) {
                this.validator.validate(value);
            }
            this.value = value;
        }
        return oldValue;
    }
    
    @Override
    public int hashCode() {
        T value = this.value;
        return value == null ? 0 : value.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Reference<?>)) {
            return false;
        }
        Reference<?> other = (Reference<?>)obj;
        return this.contains(other.get(true)) && other.contains(this.value);
    }

}
