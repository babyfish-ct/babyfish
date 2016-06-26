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
package org.babyfish.lang.delegate;

import org.babyfish.lang.Delegate;

/**
 * @author Tao Chen
 */
@Delegate
public interface PrimitiveComparator {
    
    boolean equals(boolean a, boolean b);
    
    boolean equals(char a, char b);
    
    boolean equals(byte a, byte b);
    
    boolean equals(short a, short b);
    
    boolean equals(int a, int b);
    
    boolean equals(long a, long b);
    
    boolean equals(float a, float b);
    
    boolean equals(double a, double b);
    
    static PrimitiveComparator combine(PrimitiveComparator a, PrimitiveComparator b) {
        throw new UnsupportedOperationException("Instrument required");
    }
    
    static PrimitiveComparator remove(PrimitiveComparator a, PrimitiveComparator b) {
        throw new UnsupportedOperationException("Instrument required");
    }
}
