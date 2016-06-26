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

import javax.persistence.criteria.Subquery;

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public abstract class SubqueryComparisonModifierExpression<T> extends AbstractExpression<T> {

    private static final long serialVersionUID = -4661526734771739235L;
    
    private Subquery<T> subquery;

    private SubqueryComparisonModifierExpression(
            XCriteriaBuilder criteriaBuilder, Subquery<T> subquery) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("subquery", subquery);
        this.mustUnderSameCriteriaBuilder("subquery", subquery);
        this.subquery = subquery;
    }

    @Override
    public Class<? extends T> getJavaType() {
        return this.subquery.getJavaType();
    }

    public Subquery<T> getSubquery() {
        return this.subquery;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visitSubqueryComparisonModifierExpression(this);
    }

    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }

    public static class All<T> extends SubqueryComparisonModifierExpression<T> {

        private static final long serialVersionUID = 7744931366646024959L;

        public All(XCriteriaBuilder criteriaBuilder, Subquery<T> subquery) {
            super(criteriaBuilder, subquery);
        }
        
    }
    
    public static class Any<T> extends SubqueryComparisonModifierExpression<T> {

        private static final long serialVersionUID = -4770201507471406568L;

        public Any(XCriteriaBuilder criteriaBuilder, Subquery<T> subquery) {
            super(criteriaBuilder, subquery);
        }
        
    }
    
    public static class Some<T> extends SubqueryComparisonModifierExpression<T> {

        private static final long serialVersionUID = -7425167103045967426L;

        public Some(XCriteriaBuilder criteriaBuilder, Subquery<T> subquery) {
            super(criteriaBuilder, subquery);
        }
        
    }
    
}
