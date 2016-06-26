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
package org.babyfish.persistence.criteria.spi;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.Expression;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.expression.PriorityConstants;

/**
 * @author Tao Chen
 */
public abstract class AbstractFunction<T> extends AbstractExpression<T> {
    
    private static final long serialVersionUID = -7912944348630654988L;

    private static final List<Expression<?>> EMPTY_ARGUMENT_EXPRESSIONS =
            MACollections.emptyList();
    
    private String functionName;
    
    private List<Expression<?>> argumentExpressions;

    protected AbstractFunction(
            XCriteriaBuilder criteriaBuilder, 
            String functionName, 
            Expression<?> ... argumentExpressions) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("functionName", functionName);
        Arguments.mustNotBeNull("argumentExpressions", argumentExpressions);
        this.functionName = functionName;
        this.initArgumentExpressions(argumentExpressions.clone());
    }

    protected AbstractFunction(
            XCriteriaBuilder criteriaBuilder, 
            String functionName, 
            Collection<Expression<?>> argumentExpressions) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("functionName", functionName);
        Arguments.mustNotBeNull("argumentExpressions", argumentExpressions);
        this.functionName = functionName;
        this.initArgumentExpressions(
                argumentExpressions.toArray(
                        new Expression[argumentExpressions.size()]));
    }
    
    private void initArgumentExpressions(Expression<?>[] argumentExpressions) {
        if (argumentExpressions.length == 0) {
            this.argumentExpressions = EMPTY_ARGUMENT_EXPRESSIONS;
        } else {
            for (int i = 0; i < argumentExpressions.length; i++) {
                if (argumentExpressions[i] == null) {
                    Arguments.mustNotBeNull("argumentExpressions[" + i + "]", argumentExpressions[i]);
                }
                this.mustUnderSameCriteriaBuilder(
                        "argumentExpressions[" + i + ']', 
                        argumentExpressions[i]);
            }
            this.argumentExpressions = MACollections.wrap(argumentExpressions);
        }
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Expression<?>> getArguments() {
        return argumentExpressions;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitFunction(this);
    }

    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }
}
