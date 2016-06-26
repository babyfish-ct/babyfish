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

import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractFunction;

/**
 * @author Tao Chen
 */
public class SubstringFunction extends AbstractFunction<String> {
    
    private static final long serialVersionUID = -8556031697688118536L;
    
    public static final String FUNCTION_NAME = "substring";
    
    public SubstringFunction(
            XCriteriaBuilder criteriaBuilder,
            Expression<String> valueExpression,
            Expression<Integer> startExpression,
            Expression<Integer> lengthExpression) {
        super(
                criteriaBuilder, 
                FUNCTION_NAME, 
                createArgumentExpressions(valueExpression, startExpression, lengthExpression));
    }
    
    private static Expression<?>[] createArgumentExpressions(
            Expression<String> valueExpression,
            Expression<Integer> startExpression,
            Expression<Integer> lengthExpression) {
        return lengthExpression == null ?
                new Expression[] { valueExpression, startExpression } :
                new Expression[] { valueExpression, startExpression, lengthExpression };
    }
    
    @SuppressWarnings("unchecked")
    public Expression<String> getValue() {
        return (Expression<String>)this.getArguments().get(0);
    }
    
    @SuppressWarnings("unchecked")
    public Expression<Integer> getStart() {
        return (Expression<Integer>)this.getArguments().get(1);
    }
    
    @SuppressWarnings("unchecked")
    public Expression<Integer> getLength() {
        List<Expression<?>> argumentExpressions = this.getArguments();
        if (argumentExpressions.size() > 2) {
            return (Expression<Integer>)argumentExpressions.get(2);
        }
        return null;
    }

    @Override
    public Class<? extends String> getJavaType() {
        return String.class;
    }
    
}
