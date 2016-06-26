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
package org.babyfish.persistence.criteria.expression;

import javax.persistence.criteria.Expression;

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public abstract class Aggregation<T, A> extends AbstractExpression<T> {

    private static final long serialVersionUID = -6583220409742987647L;
    
    private Expression<A> argument;

    private Aggregation(
            XCriteriaBuilder criteriaBuilder, 
            Expression<A> argument) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("argument", argument);
        this.mustUnderSameCriteriaBuilder("argument", argument);
        this.argument = argument;
    }
    
    public Expression<A> getOperand() {
        return this.argument;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visitAggregation(this);
    }

    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }

    public static class Count extends Aggregation<Long, Object> {
        
        private static final long serialVersionUID = -1039456291613092964L;
        
        private boolean distinct;
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Count(
                XCriteriaBuilder criteriaBuilder, 
                Expression<?> argument,
                boolean distinct) {
            super(criteriaBuilder, (Expression)argument);
            //don't check the java type of argument expression
            //because count() can use entity to be its parameter, not only number
            this.distinct = distinct;
        }
        
        public boolean isDistinct() {
            return this.distinct;
        }

        @Override
        public Class<? extends Long> getJavaType() {
            return Long.class;
        }
        
    }
    
    public static class Avg<T extends Number> extends Aggregation<Double, T> {
        
        private static final long serialVersionUID = -1039456291613092964L;
        
        public Avg(
                XCriteriaBuilder criteriaBuilder, 
                Expression<T> argument) {
            super(criteriaBuilder, argument);
            Arguments.mustBeCompatibleWithAnyOfValue(
                    "argument.getJavaType()", 
                    argument.getJavaType(), 
                    Number.class,
                    byte.class,
                    short.class,
                    int.class,
                    long.class,
                    float.class,
                    double.class);
        }

        @Override
        public Class<? extends Double> getJavaType() {
            return Double.class;
        }
        
    }
    
    public static class Sum<T extends Number> extends Aggregation<T, T> {

        private static final long serialVersionUID = 1697085396369811386L;
        
        public Sum(XCriteriaBuilder criteriaBuilder, Expression<T> argument) {
            super(criteriaBuilder, argument);
            Arguments.mustBeCompatibleWithAnyOfValue(
                    "argument.getJavaType()", 
                    argument.getJavaType(), 
                    Number.class,
                    byte.class,
                    short.class,
                    int.class,
                    long.class,
                    float.class,
                    double.class);
        }

        @Override
        public Class<? extends T> getJavaType() {
            return this.getOperand().getJavaType();
        }

    }
    
    public static class SumAsLong extends Aggregation<Long, Integer> {

        private static final long serialVersionUID = 1697085396369811386L;
        
        public SumAsLong(XCriteriaBuilder criteriaBuilder, Expression<Integer> argument) {
            super(criteriaBuilder, argument);
            Arguments.mustBeCompatibleWithAnyOfValue(
                    "argument.getJavaType()", 
                    argument.getJavaType(), 
                    Number.class,
                    byte.class,
                    short.class,
                    int.class,
                    long.class,
                    float.class,
                    double.class);
        }

        @Override
        public Class<? extends Long> getJavaType() {
            return Long.class;
        }

    }
    
    public static class SumAsDouble extends Aggregation<Double, Float> {

        private static final long serialVersionUID = 1697085396369811386L;
        
        public SumAsDouble(XCriteriaBuilder criteriaBuilder, Expression<Float> argument) {
            super(criteriaBuilder, argument);
            Arguments.mustBeCompatibleWithAnyOfValue(
                    "argument.getJavaType()", 
                    argument.getJavaType(), 
                    Number.class,
                    byte.class,
                    short.class,
                    int.class,
                    long.class,
                    float.class,
                    double.class);
        }

        @Override
        public Class<? extends Double> getJavaType() {
            return Double.class;
        }

    }
    
    public static class Least<T extends Comparable<? super T>> extends Aggregation<T, T> {

        private static final long serialVersionUID = 4614029407318424180L;

        public Least(
                XCriteriaBuilder criteriaBuilder,
                Expression<T> argument) {
            super(criteriaBuilder, argument);
            Arguments.mustBeCompatibleWithValue("argument.getJavaType()", argument.getJavaType(), Comparable.class);
        }

        @Override
        public Class<? extends T> getJavaType() {
            return this.getOperand().getJavaType();
        }
        
    }
    
    public static class Greatest<T extends Comparable<? super T>> extends Aggregation<T, T> {

        private static final long serialVersionUID = 4614029407318424180L;

        public Greatest(
                XCriteriaBuilder criteriaBuilder,
                Expression<T> argument) {
            super(criteriaBuilder, argument);
            Arguments.mustBeCompatibleWithValue("argument.getJavaType()", argument.getJavaType(), Comparable.class);
        }

        @Override
        public Class<? extends T> getJavaType() {
            return this.getOperand().getJavaType();
        }
        
    }
    
    public static class Min<T extends Number> extends Aggregation<T, T> {

        private static final long serialVersionUID = 1697085396369811386L;
        
        public Min(XCriteriaBuilder criteriaBuilder, Expression<T> argument) {
            super(criteriaBuilder, argument);
            Arguments.mustBeCompatibleWithAnyOfValue(
                    "argument.getJavaType()", 
                    argument.getJavaType(), 
                    Number.class,
                    byte.class,
                    short.class,
                    int.class,
                    long.class,
                    float.class,
                    double.class);
        }

        @Override
        public Class<? extends T> getJavaType() {
            return this.getOperand().getJavaType();
        }

    }
    
    public static class Max<T extends Number> extends Aggregation<T, T> {

        private static final long serialVersionUID = 1730549662631621484L;

        public Max(XCriteriaBuilder criteriaBuilder, Expression<T> argument) {
            super(criteriaBuilder, argument);
            Arguments.mustBeCompatibleWithAnyOfValue(
                    "argument.getJavaType()", 
                    argument.getJavaType(), 
                    Number.class,
                    byte.class,
                    short.class,
                    int.class,
                    long.class,
                    float.class,
                    double.class);
        }

        @Override
        public Class<? extends T> getJavaType() {
            return this.getOperand().getJavaType();
        }

    }

}
