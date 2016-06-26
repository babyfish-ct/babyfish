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
import org.babyfish.lang.I18N;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public abstract class BinaryArithmeticExpression<T extends Number> extends AbstractExpression<T> {

    private static final long serialVersionUID = -7438031664893657594L;
    
    private Expression<? extends Number> leftOperand;
    
    private Expression<? extends Number> rightOperand;
    
    private Class<? extends T> javaType;

    @SuppressWarnings("unchecked")
    BinaryArithmeticExpression(
            XCriteriaBuilder criteriaBuilder,
            Expression<? extends Number> leftOperand,
            Expression<? extends Number> rightOperand) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("leftOperand", leftOperand);
        Arguments.mustNotBeNull("rightOperand", rightOperand);
        this.mustUnderSameCriteriaBuilder("leftOperand", leftOperand);
        this.mustUnderSameCriteriaBuilder("rightOperand", rightOperand);
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        Class<? extends Number> leftType = this.leftOperand.getJavaType();
        Class<? extends Number> rightType = this.rightOperand.getJavaType();
        if (leftType == Double.class || rightType == Double.class || 
                leftType == double.class || rightType == double.class || 
                leftType == Float.class || rightType == Float.class || 
                leftType == float.class || rightType == float.class) {
            this.javaType = (Class<? extends T>)Double.class;
        } else if (leftType == BigDecimal.class || rightType == BigDecimal.class) {
            this.javaType = (Class<? extends T>)BigDecimal.class;
        } else if (leftType == BigInteger.class || rightType == BigInteger.class) {
            this.javaType = (Class<? extends T>)BigInteger.class;
        } else if (leftType == Long.class || rightType == Long.class || leftType == long.class || rightType == long.class) {
            this.javaType = (Class<? extends T>)Long.class;
        } else if (leftType == Integer.class || rightType == Integer.class || leftType == int.class || rightType == int.class) {
            this.javaType = (Class<? extends T>)Integer.class;
        } else if (leftType == Short.class || rightType == Short.class || leftType == short.class || rightType == short.class) {
            this.javaType = (Class<? extends T>)Short.class;
        } else if (leftType == Byte.class || rightType == Byte.class || leftType == byte.class || rightType == byte.class) {
            this.javaType = (Class<? extends T>)Byte.class;
        } else {
            throw new IllegalArgumentException(
                    illegalBinaryArithmeticTypes(leftType, rightType)
            );
        }
    }
    
    public final Expression<? extends Number> getLeftOperand() {
        return this.leftOperand;
    }

    public final Expression<? extends Number> getRightOperand() {
        return this.rightOperand;
    }

    @Override
    public Class<? extends T> getJavaType() {
        return this.javaType;
    }
    
    public abstract String getOperator();
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visitBinaryArithmeticExpression(this);
    }

    public static class Sum<T extends Number> extends BinaryArithmeticExpression<T> {

        private static final long serialVersionUID = -5240749360311053658L;

        public Sum(
                XCriteriaBuilder criteriaBuilder,
                Expression<? extends Number> leftOperand,
                Expression<? extends Number> rightOperand) {
            super(criteriaBuilder, leftOperand, rightOperand);
        }

        @Override
        public String getOperator() {
            return "+";
        }
        
        @Override
        public int getPriority() {
            return PriorityConstants.SUM_DIFF;
        }
    }
    
    public static class Diff<T extends Number> extends BinaryArithmeticExpression<T> {

        private static final long serialVersionUID = 4167825183168659680L;

        public Diff(
                XCriteriaBuilder criteriaBuilder,
                Expression<? extends Number> leftOperand,
                Expression<? extends Number> rightOperand) {
            super(criteriaBuilder, leftOperand, rightOperand);
        }
        
        @Override
        public String getOperator() {
            return "-";
        }
        
        @Override
        public int getPriority() {
            return PriorityConstants.SUM_DIFF;
        }
    }
    
    public static class Prod<T extends Number> extends BinaryArithmeticExpression<T> {

        private static final long serialVersionUID = 8480989312311308037L;

        public Prod(
                XCriteriaBuilder criteriaBuilder,
                Expression<? extends Number> leftOperand,
                Expression<? extends Number> rightOperand) {
            super(criteriaBuilder, leftOperand, rightOperand);
        }
        
        @Override
        public String getOperator() {
            return "*";
        }
        
        @Override
        public int getPriority() {
            return PriorityConstants.PROD_QUOT_MOD;
        }
        
    }
    
    public static class Quot<T extends Number> extends BinaryArithmeticExpression<T> {

        private static final long serialVersionUID = 6538695478703163288L;

        public Quot(
                XCriteriaBuilder criteriaBuilder,
                Expression<? extends Number> leftOperand,
                Expression<? extends Number> rightOperand) {
            super(criteriaBuilder, leftOperand, rightOperand);
        }
        
        @Override
        public String getOperator() {
            return "/";
        }
        
        @Override
        public int getPriority() {
            return PriorityConstants.PROD_QUOT_MOD;
        }
    }
    
    public static class Mod extends BinaryArithmeticExpression<Integer> {

        private static final long serialVersionUID = 7267091696695325409L;

        public Mod(
                XCriteriaBuilder criteriaBuilder,
                Expression<? extends Integer> leftOperand,
                Expression<? extends Integer> rightOperand) {
            super(criteriaBuilder, leftOperand, rightOperand);
        }
        
        @Override
        public String getOperator() {
            return "mod";
        }
        
        @Override
        public int getPriority() {
            return PriorityConstants.PROD_QUOT_MOD;
        }
    }
    
    @I18N
    private static native String illegalBinaryArithmeticTypes(
                Class<? extends Number> leftType, 
                Class<? extends Number> rightType);
}
