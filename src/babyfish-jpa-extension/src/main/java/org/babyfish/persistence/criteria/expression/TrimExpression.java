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

import javax.persistence.criteria.CriteriaBuilder.Trimspec;
import javax.persistence.criteria.Expression;

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class TrimExpression extends AbstractExpression<String> {

    private static final long serialVersionUID = 3466420601366631960L;

    public static final String FUNCTION_NAME = "trim";
    
    public static final Trimspec DEFAULT_TRIM_SPEC = Trimspec.BOTH;
    
    private Trimspec trimspec;
    
    private Expression<Character> trimCharacter; 
    
    private Expression<String> trimSource;
    
    public TrimExpression(
            XCriteriaBuilder criteriaBuilder, 
            Trimspec trimspec,
            Expression<Character> trimCharacter, 
            Expression<String> trimSource) {
        super(criteriaBuilder);
        this.trimspec = trimspec == null ? Trimspec.BOTH : trimspec;
        this.trimCharacter = this.mustUnderSameCriteriaBuilder("trimCharacter", Arguments.mustNotBeNull("trimCharacter", trimCharacter));
        this.trimSource = this.mustUnderSameCriteriaBuilder("trimSource", Arguments.mustNotBeNull("trimSource", trimSource));
    }
    
    public Trimspec getTrimspec() {
        return this.trimspec;
    }

    public Expression<Character> getTrimCharacter() {
        return trimCharacter;
    }

    public Expression<String> getTrimSource() {
        return trimSource;
    }

    @Override
    public Class<? extends String> getJavaType() {
        return String.class;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitTrimExpression(this);
    }
    
    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }
}
