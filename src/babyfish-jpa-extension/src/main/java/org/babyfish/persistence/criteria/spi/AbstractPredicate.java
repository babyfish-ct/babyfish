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

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.expression.CompoundPredicate;

/**
 * @author Tao Chen
 */
public abstract class AbstractPredicate 
extends AbstractExpression<Boolean> 
implements Predicate, Cloneable {

    private static final long serialVersionUID = 8450975090557585625L;
    
    private AbstractPredicate not;

    protected AbstractPredicate(XCriteriaBuilder criteriaBuilder) {
        super(criteriaBuilder);
    }

    @Override
    public final Class<? extends Boolean> getJavaType() {
        return Boolean.class;
    }

    @Override
    public final Predicate not() {
        AbstractPredicate not = this.not;
        if (not == null) {
            not = this.createNot();
            not.not = this;
            this.not = not;
        }
        return not;
    }
    
    protected abstract AbstractPredicate createNot();
    
    public static boolean isNullOrEmpty(Expression<Boolean> expression) {
        if (expression == null) {
            return true;
        }
        return 
                expression instanceof CompoundPredicate &&
                ((CompoundPredicate)expression).getExpressions().isEmpty();
    }

}
