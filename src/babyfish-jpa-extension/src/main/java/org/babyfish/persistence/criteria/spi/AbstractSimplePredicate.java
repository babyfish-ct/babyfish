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

import java.util.List;

import javax.persistence.criteria.Expression;

import org.babyfish.collection.MACollections;
import org.babyfish.persistence.criteria.XCriteriaBuilder;

/**
 * @author Tao Chen
 */
public abstract class AbstractSimplePredicate extends AbstractPredicate {

    private static final long serialVersionUID = -8310021795946395041L;
    
    private boolean nagated;
    
    protected AbstractSimplePredicate(XCriteriaBuilder criteriaBuilder) {
        super(criteriaBuilder);
    }

    @Override
    public final BooleanOperator getOperator() {
        return BooleanOperator.AND;
    }

    @Override
    public final List<Expression<Boolean>> getExpressions() {
        return MACollections.emptyList();
    }

    @Override
    public boolean isNegated() {
        return this.nagated;
    }

    @Override
    protected AbstractSimplePredicate createNot() {
        AbstractSimplePredicate not;
        try {
            not = (AbstractSimplePredicate)this.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        not.nagated = !this.nagated;
        return not;
    }

}
