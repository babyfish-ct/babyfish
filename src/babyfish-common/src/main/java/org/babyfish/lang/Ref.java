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
public class Ref<T> implements Serializable {

    private static final long serialVersionUID = 8965283752972877830L;
    
    private T value;
    
    public Ref() {
        
    }
    
    public Ref(T value) {
        this.value = value;
    }
    
    public T get() {
        return this.value;
    }
    
    public void set(T value) {
        this.value = value;
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
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Ref<?> other = (Ref<?>)obj;
        return this.value == other.value;
    }

    @Override
    public String toString() {
        T value = this.value;
        return "Ref[" + (value == null ? "null" : value.toString()) + "]";
    }
    
}
