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
public class NullifExpression<T> extends AbstractExpression<T> {
    
    private static final long serialVersionUID = -5807900145438912707L;

    private Expression<? extends T> primaryExpression;
    
    private Expression<?> secondaryExpression;

    public NullifExpression(
            XCriteriaBuilder criteriaBuilder,
            Expression<? extends T> primaryExpression,
            Expression<?> secondaryExpression) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("primaryExpression", primaryExpression);
        Arguments.mustNotBeNull("secondaryExpression", secondaryExpression);
        mustHaveExplicitDataType("primaryExpression", primaryExpression);
        this.mustUnderSameCriteriaBuilder("primaryExpression", primaryExpression);
        this.mustUnderSameCriteriaBuilder("secondaryExpression", secondaryExpression);
        this.primaryExpression = primaryExpression;
        this.secondaryExpression = secondaryExpression;
    }

    public Expression<? extends T> getPrimaryOperand() {
        return this.primaryExpression;
    }

    public Expression<?> getSecondaryOperand() {
        return this.secondaryExpression;
    }

    @Override
    public Class<? extends T> getJavaType() {
        return this.primaryExpression.getJavaType();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNullifExpression(this);
    }
    
    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }
}
