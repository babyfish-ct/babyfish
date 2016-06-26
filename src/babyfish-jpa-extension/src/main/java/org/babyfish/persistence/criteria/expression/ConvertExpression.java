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

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.persistence.criteria.Expression;

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public abstract class ConvertExpression<T, S> extends AbstractExpression<T> {
    
    private static final long serialVersionUID = 7921636257524376073L;
    
    private Expression<? extends S> sourceExpression;

    private ConvertExpression(XCriteriaBuilder criteriaBuilder, Expression<? extends S> sourceExpression) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("sourceExpression", sourceExpression);
        this.mustUnderSameCriteriaBuilder("sourceExpression", sourceExpression);
        this.sourceExpression = sourceExpression;
    }
    
    public final Expression<? extends S> getOperand() {
        return this.sourceExpression;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visitConvertExpression(this);
    }
    
    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }

    public static class ToInteger extends ConvertExpression<Integer, Number> {

        private static final long serialVersionUID = 4032454325561756420L;

        public ToInteger(
                XCriteriaBuilder criteriaBuilder,
                Expression<? extends Number> sourceExpression) {
            super(criteriaBuilder, sourceExpression);
        }

        @Override
        public Class<? extends Integer> getJavaType() {
            return Integer.class;
        }
        
    }
    
    public static class ToLong extends ConvertExpression<Long, Number> {

        private static final long serialVersionUID = -6745954229033983646L;

        public ToLong(
                XCriteriaBuilder criteriaBuilder,
                Expression<? extends Number> sourceExpression) {
            super(criteriaBuilder, sourceExpression);
        }

        @Override
        public Class<? extends Long> getJavaType() {
            return Long.class;
        }
        
    }
    
    public static class ToFloat extends ConvertExpression<Float, Number> {

        private static final long serialVersionUID = 6509685845199854847L;

        public ToFloat(
                XCriteriaBuilder criteriaBuilder,
                Expression<? extends Number> sourceExpression) {
            super(criteriaBuilder, sourceExpression);
        }

        @Override
        public Class<? extends Float> getJavaType() {
            return Float.class;
        }
        
    }
    
    public static class ToDouble extends ConvertExpression<Double, Number> {

        private static final long serialVersionUID = 4177495894700969124L;

        public ToDouble(
                XCriteriaBuilder criteriaBuilder,
                Expression<? extends Number> sourceExpression) {
            super(criteriaBuilder, sourceExpression);
        }

        @Override
        public Class<? extends Double> getJavaType() {
            return Double.class;
        }
        
    }
    
    public static class ToBigInteger extends ConvertExpression<BigInteger, Number> {

        private static final long serialVersionUID = 823277187219071420L;

        public ToBigInteger(
                XCriteriaBuilder criteriaBuilder,
                Expression<? extends Number> sourceExpression) {
            super(criteriaBuilder, sourceExpression);
        }

        @Override
        public Class<? extends BigInteger> getJavaType() {
            return BigInteger.class;
        }
        
    }
    
    public static class ToBigDecimal extends ConvertExpression<BigDecimal, Number> {

        private static final long serialVersionUID = -7959620809746613213L;

        public ToBigDecimal(
                XCriteriaBuilder criteriaBuilder,
                Expression<? extends Number> sourceExpression) {
            super(criteriaBuilder, sourceExpression);
        }

        @Override
        public Class<? extends BigDecimal> getJavaType() {
            return BigDecimal.class;
        }
        
    }
    
    public static class ToString extends ConvertExpression<String, Character> {

        private static final long serialVersionUID = 4054864240386385739L;

        public ToString(
                XCriteriaBuilder criteriaBuilder,
                Expression<? extends Character> sourceExpression) {
            super(criteriaBuilder, sourceExpression);
        }

        @Override
        public Class<? extends String> getJavaType() {
            return String.class;
        }
        
    }
}
