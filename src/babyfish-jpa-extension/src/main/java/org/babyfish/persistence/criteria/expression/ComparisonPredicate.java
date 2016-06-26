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

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractPredicate;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public abstract class ComparisonPredicate extends AbstractPredicate {

    private static final long serialVersionUID = 1729548889019479102L;
    
    private Expression<?> leftOperand;
    
    private Expression<?> rightOperand;

    private ComparisonPredicate(
            XCriteriaBuilder criteriaBuilder,
            Expression<?> leftOperand,
            Expression<?> rightOperand) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("leftOperand", leftOperand);
        Arguments.mustNotBeNull("rightOperand", rightOperand);
        this.mustUnderSameCriteriaBuilder("leftOperand", leftOperand);
        this.mustUnderSameCriteriaBuilder("rightOperand", rightOperand);
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    public final Expression<?> getLeftOperand() {
        return this.leftOperand;
    }

    public final Expression<?> getRightOperand() {
        return this.rightOperand;
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
    public final boolean isNegated() {
        return false;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitComparisonPredicate(this);
    }
    
    @Override
    public int getPriority() {
        return PriorityConstants.COMPARASION;
    }

    public static class Equal extends ComparisonPredicate {

        private static final long serialVersionUID = -4904876192581611289L;

        public Equal(
                XCriteriaBuilder criteriaBuilder,
                Expression<?> leftOperand, 
                Expression<?> rightOperand) {
            super(criteriaBuilder, leftOperand, rightOperand);
        }

        @Override
        protected AbstractPredicate createNot() {
            return new NotEqual(
                    this.getCriteriaBuilder(), 
                    this.getLeftOperand(), 
                    this.getRightOperand());
        }
        
    }
    
    public static class NotEqual extends ComparisonPredicate {

        private static final long serialVersionUID = 2092441513528960860L;

        public NotEqual(
                XCriteriaBuilder criteriaBuilder,
                Expression<?> leftOperand, 
                Expression<?> rightOperand) {
            super(criteriaBuilder, leftOperand, rightOperand);
        }
        
        @Override
        protected AbstractPredicate createNot() {
            return new Equal(
                    this.getCriteriaBuilder(), 
                    this.getLeftOperand(), 
                    this.getRightOperand());
        }
    }
    
    public static class LessThan extends ComparisonPredicate {

        private static final long serialVersionUID = 3353543931707946052L;

        public LessThan(
                XCriteriaBuilder criteriaBuilder,
                Expression<?> leftOperand, 
                Expression<?> rightOperand) {
            super(criteriaBuilder, leftOperand, rightOperand);
        }
        
        @Override
        protected AbstractPredicate createNot() {
            return new GreaterThanOrEqual(
                    this.getCriteriaBuilder(), 
                    this.getLeftOperand(), 
                    this.getRightOperand());
        }
    }
    
    public static class GreaterThan extends ComparisonPredicate {

        private static final long serialVersionUID = -7261950963710707767L;

        public GreaterThan(
                XCriteriaBuilder criteriaBuilder,
                Expression<?> leftOperand, 
                Expression<?> rightOperand) {
            super(criteriaBuilder, leftOperand, rightOperand);
        }
        
        @Override
        protected AbstractPredicate createNot() {
            return new LessThanOrEqual(
                    this.getCriteriaBuilder(), 
                    this.getLeftOperand(), 
                    this.getRightOperand());
        }
    }
    
    public static class LessThanOrEqual extends ComparisonPredicate {

        private static final long serialVersionUID = 5366125870381209968L;

        public LessThanOrEqual(
                XCriteriaBuilder criteriaBuilder,
                Expression<?> leftOperand, 
                Expression<?> rightOperand) {
            super(criteriaBuilder, leftOperand, rightOperand);
        }
        
        @Override
        protected AbstractPredicate createNot() {
            return new GreaterThan(
                    this.getCriteriaBuilder(), 
                    this.getLeftOperand(), 
                    this.getRightOperand());
        }
    }
    
    public static class GreaterThanOrEqual extends ComparisonPredicate {

        private static final long serialVersionUID = 4497492572920457355L;

        public GreaterThanOrEqual(
                XCriteriaBuilder criteriaBuilder,
                Expression<?> leftOperand, 
                Expression<?> rightOperand) {
            super(criteriaBuilder, leftOperand, rightOperand);
        }
        
        @Override
        protected AbstractPredicate createNot() {
            return new LessThan(
                    this.getCriteriaBuilder(), 
                    this.getLeftOperand(), 
                    this.getRightOperand());
        }
    }
}
