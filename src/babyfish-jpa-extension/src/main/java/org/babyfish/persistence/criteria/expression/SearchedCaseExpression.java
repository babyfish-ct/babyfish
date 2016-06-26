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

import javax.persistence.criteria.CriteriaBuilder.Case;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class SearchedCaseExpression<R> extends AbstractExpression<R> implements Case<R> {

    private static final long serialVersionUID = 3677272923107225348L;
    
    private List<WhenClause<R>> whenClauses;

    private Expression<? extends R> otherwiseResult;

    public SearchedCaseExpression(XCriteriaBuilder criteriaBuilder) {
        super(criteriaBuilder);
        this.whenClauses = new ArrayList<>();
    }

    @Override
    public Class<? extends R> getJavaType() {
        if (whenClauses.isEmpty() || this.otherwiseResult == null) {
            throw new IllegalStateException(missWhenOrOtherwise(SearchedCaseExpression.class));
        }
        return whenClauses.get(0).getResult().getJavaType();
    }

    public List<WhenClause<R>> getWhenClauses() {
        return MACollections.unmodifiable(this.whenClauses);
    }

    public Expression<? extends R> getOtherwiseResult() {
        return this.otherwiseResult;
    }

    @Override
    public Case<R> when(Expression<Boolean> condition, Expression<? extends R> result) {
        this.checkState();
        Arguments.mustNotBeNull("condition", condition);
        Arguments.mustNotBeNull("result", result);
        this.mustUnderSameCriteriaBuilder("condition", condition);
        this.mustUnderSameCriteriaBuilder("result", result);
        this.whenClauses.add(new WhenClause<>(condition, result));
        return this;
    }

    @Override
    public Case<R> when(Expression<Boolean> condition, R result) {
        return this.when(condition, this.getCriteriaBuilder().constant(result));
    }

    @Override
    public Expression<R> otherwise(Expression<? extends R> result) {
        this.checkState();
        Arguments.mustNotBeInstanceOfAnyOfValue(
        "result", 
        this.mustUnderSameCriteriaBuilder(
                "result", 
                Arguments.mustNotBeNull("result", result)
        ), 
        ParameterExpression.class,
        LiteralExpression.class);
        this.otherwiseResult = result;
        return this;
    }

    @Override
    public Expression<R> otherwise(R result) {
        return this.otherwise(this.getCriteriaBuilder().constant(result));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitSearchedCaseExpression(this);
    }

    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }

    public static class WhenClause<R> {
        
        private Expression<Boolean> condition;
        
        private Expression<? extends R> result;

        public WhenClause(Expression<Boolean> condition, Expression<? extends R> result) {
            this.condition = Arguments.mustNotBeNull("condition", condition);
            this.result = Arguments.mustNotBeInstanceOfAnyOfValue(
                    "result", 
                    Arguments.mustNotBeNull("result", result), 
                    ParameterExpression.class,
                    LiteralExpression.class);
        }
        
        public Expression<Boolean> getCondition() {
            return this.condition;
        }

        public Expression<? extends R> getResult() {
            return this.result;
        }
    }
    
    @SuppressWarnings("rawtypes")
    @I18N
    private static native String missWhenOrOtherwise(Class<SearchedCaseExpression> searchedCaseExpressionType);
}
