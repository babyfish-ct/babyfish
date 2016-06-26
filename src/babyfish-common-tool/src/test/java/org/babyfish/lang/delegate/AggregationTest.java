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
public class AggregationTest {

    @Test
    public void testNull() {
        
        Assert.assertNull(Aggregation.combine(null, null));
        Aggregation<Integer> comparator = new IntegerCount(null);
        Assert.assertSame(comparator, Aggregation.combine(comparator, null));
        Assert.assertSame(comparator, Aggregation.combine(null, comparator));
        
        Aggregation<Integer> combined = Aggregation.combine(comparator, comparator);
        Assert.assertTrue(combined instanceof AbstractCombinedDelegate);
        combined = Aggregation.remove(combined, comparator);
        Assert.assertSame(comparator, combined);
        combined = Aggregation.remove(combined, comparator);
        Assert.assertNull(combined);
        
        combined = Aggregation.combine(comparator, comparator);
        Assert.assertTrue(combined instanceof AbstractCombinedDelegate);
        Assert.assertNull(Aggregation.remove(combined, combined));
        
        Assert.assertNull(Aggregation.remove(comparator, combined));
    }
    
    @Test
    public void testChain() {
        StringBuilder builder = new StringBuilder();
        Aggregation<Integer> count = new IntegerCount(builder);
        Aggregation<Integer> sum = new IntegerSum(builder);
        Aggregation<Integer> avg = new IntegerAvg(builder);
        Aggregation<Integer> aggreation = Aggregation.combine(
                avg, 
                Aggregation.combine(count, sum)
        );
        
        Assert.assertEquals(15, aggreation.aggregate(1, 2, 3, 4, 5).intValue());
        Assert.assertEquals("avg(...)count(...)sum(...)", builder.toString());
        builder.setLength(0);
        
        try {
            aggreation.aggregate();
            Assert.fail("IllegalArgumentException is expected");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("Avg: args cannot be empty", ex.getMessage());
        }
        Assert.assertEquals("avg(...)count(...)sum(...)", builder.toString());
        builder.setLength(0);
        
        aggreation = Aggregation.remove(aggreation, avg);
        Assert.assertEquals(0, aggreation.aggregate().intValue());
        Assert.assertEquals("count(...)sum(...)", builder.toString());
        builder.setLength(0);
    }
    
    private static abstract class AbstractAggregation<T extends Number> implements Aggregation<T> {
        
        StringBuilder builder;
        
        public AbstractAggregation(StringBuilder builder) {
            this.builder = builder;
        }
    }
    
    private static class IntegerCount extends AbstractAggregation<Integer> {
    
        IntegerCount(StringBuilder builder) {
            super(builder);
        }
        
        @Override
        public Integer aggregate(Integer... args) {
            builder.append("count(...)");
            return args.length;
        }
    }

    private static class IntegerSum extends AbstractAggregation<Integer> {
    
        public IntegerSum(StringBuilder builder) {
            super(builder);
        }

        @Override
        public Integer aggregate(Integer... args) {
            builder.append("sum(...)");
            int sum = 0;
            for (Integer arg : args) {
                if (arg != null) {
                    sum += arg;
                }
            }
            return sum;
        }
    }

    private static class IntegerAvg extends AbstractAggregation<Integer> {

        IntegerAvg(StringBuilder builder) {
            super(builder);
        }

        @Override
        public Integer aggregate(Integer... args) {
            builder.append("avg(...)");
            if (args.length == 0) {
                throw new IllegalArgumentException("Avg: args cannot be empty");
            }
            int sum = 0;
            for (Integer arg : args) {
                if (arg != null) {
                    sum += arg;
                }
            }
            return sum / args.length;
        }
    }
}
