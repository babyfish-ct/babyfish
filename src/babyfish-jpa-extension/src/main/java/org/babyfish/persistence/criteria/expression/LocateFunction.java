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

import java.util.List;

import javax.persistence.criteria.Expression;

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractFunction;

/**
 * @author Tao Chen
 */
public class LocateFunction extends AbstractFunction<Integer> {
    
    private static final long serialVersionUID = 9061090638186351417L;
    
    public static final String FUNCTION_NAME = "locate";
    
    public LocateFunction(
            XCriteriaBuilder criteriaBuilder,
            Expression<String> stringExpression,
            Expression<String> patternExpression,
            Expression<Integer> startExpression) {
        super(criteriaBuilder, 
                FUNCTION_NAME, 
                createArgumentExpressions(
                        stringExpression, 
                        patternExpression, 
                        startExpression));
    }

    @Override
    public Class<? extends Integer> getJavaType() {
        return Integer.class;
    }

    @SuppressWarnings("unchecked")
    public Expression<String> getString() {
        return (Expression<String>)this.getArguments().get(0);
    }

    @SuppressWarnings("unchecked")
    public Expression<String> getPattern() {
        return (Expression<String>)this.getArguments().get(1);
    }

    @SuppressWarnings("unchecked")
    public Expression<Integer> getStart() {
        List<Expression<?>> argumentExpressions = this.getArguments();
        if (argumentExpressions.size() > 2) {
            return (Expression<Integer>)argumentExpressions.get(2);
        }
        return null;
    }

    private static Expression<?>[] createArgumentExpressions(
            Expression<String> stringExpression,
            Expression<String> patternExpression,
            Expression<Integer> startExpression) {
        Arguments.mustNotBeNull("stringExpression", stringExpression);
        Arguments.mustNotBeNull("patternExpression", patternExpression);
        if (startExpression == null) {
            return new Expression[] { stringExpression, patternExpression };
        } else {
            return new Expression[] { stringExpression, patternExpression, startExpression };
        }
    }
}
