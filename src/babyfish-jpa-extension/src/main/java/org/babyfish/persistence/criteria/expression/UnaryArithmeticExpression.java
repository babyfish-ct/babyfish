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
public abstract class UnaryArithmeticExpression<N extends Number> extends AbstractExpression<N>{

    private static final long serialVersionUID = 1737023137827219801L;
    
    private Expression<? extends N> operand;

    private UnaryArithmeticExpression(
            XCriteriaBuilder criteriaBuilder,
            Expression<? extends N> operand) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("operand", operand);
        this.mustUnderSameCriteriaBuilder("operand", operand);
        this.operand = operand;
    }
    
    public Expression<? extends Number> getOperand() {
        return this.operand;
    }
    
    @Override
    public Class<? extends N> getJavaType() {
        return this.operand.getJavaType();
    }
    
    public abstract String getOperator();
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visitUnaryArithmeticExpression(this);
    }

    public static class Neg<N extends Number> extends UnaryArithmeticExpression<N> {
        
        private static final long serialVersionUID = -3826035740808870643L;

        public Neg(
                XCriteriaBuilder criteriaBuilder,
                Expression<? extends N> operand) {
            super(criteriaBuilder, operand);
        }
        
        @Override
        public String getOperator() {
            return "-";
        }
    }
    
    @Override
    public int getPriority() {
        return PriorityConstants.UNARY;
    }
}
