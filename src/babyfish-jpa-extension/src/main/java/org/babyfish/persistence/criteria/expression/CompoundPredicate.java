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
import javax.persistence.criteria.Predicate;

import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractPredicate;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public final class CompoundPredicate extends AbstractPredicate {
    
    private static final long serialVersionUID = 3838567907423250345L;

    private BooleanOperator operator;
    
    private Predicate[] predicates;
    
    public static Predicate of(
            XCriteriaBuilder criteriaBuilder, 
            BooleanOperator operator,
            Predicate ... predicates) {
        XOrderedSet<Predicate> orderedSet = new LinkedHashSet<Predicate>((predicates.length * 4 + 2) / 3);
        int index = 0;
        for (Predicate predicate : predicates) {
            if (predicate != null) {
                mustUnderSameCriteriaBuilder(
                        criteriaBuilder, 
                        "predicates[" + index + ']', 
                        predicate
                );
                index++;
                orderedSet.add(predicate);
            }
        }
        if (orderedSet.isEmpty()) {
            return null;
        }
        if (orderedSet.size() == 1) {
            return orderedSet.iterator().next();
        }
        return new CompoundPredicate(
                criteriaBuilder, 
                operator, 
                orderedSet.toArray(new Predicate[orderedSet.size()])
        );
    }

    private CompoundPredicate(
            XCriteriaBuilder criteriaBuilder, 
            BooleanOperator operator,
            Predicate[] predicates) {
        super(criteriaBuilder);
        this.operator = Arguments.mustNotBeNull("operator", operator);
        this.predicates = predicates;
    }

    @Override
    public List<Expression<Boolean>> getExpressions() {
        return MACollections.wrap((Expression<Boolean>[])this.predicates);
    }

    @Override
    public BooleanOperator getOperator() {
        return this.operator;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitCompoundPredicate(this);
    }
    
    @Override
    public int getPriority() {
        return this.operator == BooleanOperator.AND ? 
                PriorityConstants.AND :
                PriorityConstants.OR;
    }

    @Override
    public final boolean isNegated() {
        return false;
    }

    @Override
    protected CompoundPredicate createNot() {
        BooleanOperator booleanOperator = 
                this.operator == BooleanOperator.AND ?
                        BooleanOperator.OR : 
                        BooleanOperator.AND;
        Predicate[] predicates = null;
        if (this.predicates != null) {
            predicates = this.predicates.clone();
            for (int i = predicates.length - 1; i >= 0; i--) {
                predicates[i] = predicates[i].not();
            }
        }
        return new CompoundPredicate(
                this.getCriteriaBuilder(),
                booleanOperator,
                predicates
        );
    }
}
