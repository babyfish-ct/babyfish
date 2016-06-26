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
import org.babyfish.lang.I18N;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractSimplePredicate;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class BetweenPredicate<Y> extends AbstractSimplePredicate {

    private static final long serialVersionUID = 1753924293482192005L;
    
    private Expression<? extends Y> expression;
    
    private Expression<? extends Y> lowerBound;
    
    private Expression<? extends Y> upperBound;

    public BetweenPredicate(
            XCriteriaBuilder criteriaBuilder,
            Expression<? extends Y> expression,
            Expression<? extends Y> lowerBound,
            Expression<? extends Y> upperBound) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("expression", expression);
        if (lowerBound == null && upperBound == null) {
            throw new IllegalArgumentException(
                    bothAreNull("lowerBound", "upperBound")
            );
        }
        this.mustUnderSameCriteriaBuilder("expression", expression);
        this.mustUnderSameCriteriaBuilder("lowerBound", lowerBound);
        this.mustUnderSameCriteriaBuilder("upperBound", upperBound);
        this.expression = expression;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public Expression<? extends Y> getSource() {
        return this.expression;
    }

    public Expression<? extends Y> getLowerBound() {
        return this.lowerBound;
    }

    public Expression<? extends Y> getUpperBound() {
        return this.upperBound;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitBetweenPredicate(this);
    }

    @Override
    public int getPriority() {
        return PriorityConstants.COMPARASION;
    }
    
    @I18N
    private static native String bothAreNull(String firstParameterName, String secondParameterName);
}
