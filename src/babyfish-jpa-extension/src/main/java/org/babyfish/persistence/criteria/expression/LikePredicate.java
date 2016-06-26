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
import org.babyfish.persistence.criteria.spi.AbstractSimplePredicate;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class LikePredicate extends AbstractSimplePredicate {

    private static final long serialVersionUID = -6795508976761218934L;

    private Expression<String> matchExpression;
    
    private Expression<String> pattern;
    
    private Expression<Character> escapeCharacter;

    public LikePredicate(
            XCriteriaBuilder criteriaBuilder,
            Expression<String> matchExpression,
            Expression<String> pattern, 
            Expression<Character> escapeCharacter) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("matchExpression", matchExpression);
        this.mustUnderSameCriteriaBuilder("matchExpression", matchExpression);
        this.mustUnderSameCriteriaBuilder("pattern", pattern);
        this.mustUnderSameCriteriaBuilder("escapeCharacter", escapeCharacter);
        this.matchExpression = matchExpression;
        this.pattern = pattern;
        this.escapeCharacter = escapeCharacter;
    }

    public final Expression<String> getSource() {
        return this.matchExpression;
    }

    public final Expression<String> getPattern() {
        return this.pattern;
    }

    public final Expression<Character> getEscapeCharacter() {
        return this.escapeCharacter;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitLikePredicate(this);
    }
    
    @Override
    public int getPriority() {
        return PriorityConstants.COMPARASION;
    }
    
}
