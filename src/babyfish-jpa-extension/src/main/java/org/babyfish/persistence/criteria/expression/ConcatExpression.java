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

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaBuilder.Concat;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class ConcatExpression extends AbstractExpression<String> implements Concat {

    private static final long serialVersionUID = 4969211180840889062L;
    
    private List<Expression<String>> values;

    public ConcatExpression(XCriteriaBuilder criteriaBuilder) {
        super(criteriaBuilder);
    }

    @Override
    public Class<? extends String> getJavaType() {
        return String.class;
    }

    @Override
    public Concat value(Expression<String> value) {
        this.checkState();
        Arguments.mustNotBeNull("value", value);
        this.mustUnderSameCriteriaBuilder("value", value);
        List<Expression<String>> values = this.values;
        if (values == null) {
            this.values = values = new ArrayList<>();
        }
        values.add(value);
        return this;
    }

    @Override
    public Concat value(String value) {
        return this.value(this.getCriteriaBuilder().literal(value));
    }

    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitConcatExpression(this);
    }

    public List<Expression<String>> getValues() {
        List<Expression<String>> values = this.values;
        if (values == null) {
            throw new IllegalStateException(isEmpty(ConcatExpression.class));
        }
        return MACollections.unmodifiable(values);
    }
    
    @I18N
    private static native String isEmpty(Class<ConcatExpression> concatExpressionType);
}
