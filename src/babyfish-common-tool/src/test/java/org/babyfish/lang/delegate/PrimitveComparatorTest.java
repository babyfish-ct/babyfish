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

import org.babyfish.lang.internal.AbstractCombinedDelegate;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class PrimitveComparatorTest {

    @Test
    public void testNull() {
        
        Assert.assertNull(PrimitiveComparator.combine(null, null));
        PrimitiveComparator comparator = new Impl(null);
        Assert.assertSame(comparator, PrimitiveComparator.combine(comparator, null));
        Assert.assertSame(comparator, PrimitiveComparator.combine(null, comparator));
        
        PrimitiveComparator combined = PrimitiveComparator.combine(comparator, comparator);
        Assert.assertTrue(combined instanceof AbstractCombinedDelegate);
        combined = PrimitiveComparator.remove(combined, comparator);
        Assert.assertSame(comparator, combined);
        combined = PrimitiveComparator.remove(combined, comparator);
        Assert.assertNull(combined);
        
        combined = PrimitiveComparator.combine(comparator, comparator);
        Assert.assertTrue(combined instanceof AbstractCombinedDelegate);
        Assert.assertNull(PrimitiveComparator.remove(combined, combined));
        
        Assert.assertNull(PrimitiveComparator.remove(comparator, combined));
    }
    
    @Test
    public void testChain() {
        StringBuilder builder = new StringBuilder();
        PrimitiveComparator a = new Impl(builder);
        PrimitiveComparator b = new InversedImpl(builder);
        PrimitiveComparator c = new Impl(builder);
        PrimitiveComparator d = new InversedImpl(builder);
        PrimitiveComparator total = PrimitiveComparator.combine(
                PrimitiveComparator.combine(a, b),
                PrimitiveComparator.combine(c, d)
        );
        
        Assert.assertEquals(false, total.equals(3, 3));
        Assert.assertEquals(":int:~int:int:~int", builder.toString());
        builder.setLength(0);
        
        total = PrimitiveComparator.remove(total, PrimitiveComparator.combine(b, d));
        total.equals(3, 3);
        //Assert.assertEquals(true, total.equals(3, 3));
        Assert.assertEquals(":int:int", builder.toString());
        builder.setLength(0);
    }
    
    private static class Impl implements PrimitiveComparator {

        private StringBuilder builder;
        
        Impl(StringBuilder builder) {
            this.builder = builder;
        }
        
        @Override
        public boolean equals(boolean a, boolean b) {
            this.builder.append(":boolean");
            return a == b;
        }

        @Override
        public boolean equals(char a, char b) {
            this.builder.append(":char");
            return a == b;
        }

        @Override
        public boolean equals(byte a, byte b) {
            this.builder.append(":byte");
            return a == b;
        }

        @Override
        public boolean equals(short a, short b) {
            this.builder.append(":short");
            return a == b;
        }

        @Override
        public boolean equals(int a, int b) {
            this.builder.append(":int");
            return a == b;
        }

        @Override
        public boolean equals(long a, long b) {
            this.builder.append(":long");
            return a == b;
        }

        @Override
        public boolean equals(float a, float b) {
            this.builder.append(":float");
            return a == b;
        }

        @Override
        public boolean equals(double a, double b) {
            this.builder.append(":double");
            return a == b;
        }
    }
    
    private static class InversedImpl implements PrimitiveComparator {

        private StringBuilder builder;
        
        InversedImpl(StringBuilder builder) {
            this.builder = builder;
        }
        
        @Override
        public boolean equals(boolean a, boolean b) {
            this.builder.append(":~boolean");
            return a != b;
        }

        @Override
        public boolean equals(char a, char b) {
            this.builder.append(":~char");
            return a != b;
        }

        @Override
        public boolean equals(byte a, byte b) {
            this.builder.append(":~byte");
            return a != b;
        }

        @Override
        public boolean equals(short a, short b) {
            this.builder.append(":~short");
            return a != b;
        }

        @Override
        public boolean equals(int a, int b) {
            this.builder.append(":~int");
            return a != b;
        }

        @Override
        public boolean equals(long a, long b) {
            this.builder.append(":~long");
            return a != b;
        }

        @Override
        public boolean equals(float a, float b) {
            this.builder.append(":~float");
            return a != b;
        }

        @Override
        public boolean equals(double a, double b) {
            this.builder.append(":~double");
            return a != b;
        }
    }
}
