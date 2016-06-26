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

import javax.persistence.criteria.CriteriaBuilder.Coalesce;
import javax.persistence.criteria.Expression;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.I18N;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class CoalesceExpression<T> extends AbstractExpression<T> implements Coalesce<T> {
    
    private static final long serialVersionUID = 1956490783274191346L;

    private Class<? extends T> javaType;
    
    private List<Expression<? extends T>> values;

    public CoalesceExpression(XCriteriaBuilder criteriaBuilder) {
        super(criteriaBuilder);
        this.values = new ArrayList<>();
    }

    @Override
    public Class<? extends T> getJavaType() {
        if (this.javaType == null) {
            throw new IllegalStateException(isEmpty(CoalesceExpression.class));
        }
        return this.javaType;
    }
    
    public List<Expression<? extends T>> getExpressions() {
        return MACollections.unmodifiable(this.values);
    }

    @Override
    public Coalesce<T> value(T value) {
        if (value != null) {
            if (this.values.isEmpty()) {
                return this.value(this.getCriteriaBuilder().constant(value));
            }
            return this.value(this.getCriteriaBuilder().literal(value));
        }
        return this;
    }

    @Override
    public Coalesce<T> value(Expression<? extends T> value) {
        if (value != null) {
            if (this.values.isEmpty()) {
                mustHaveExplicitDataTypeWhen(
                        whenCoalesceIsEmpty(CoalesceExpression.class),
                        "value", 
                        value
                );
            }
            this.mustUnderSameCriteriaBuilder("value", value);
            if (this.javaType == null) {
                this.javaType = value.getJavaType();
            }
            this.values.add(value);
        }
        return this;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitCoalesceExpression(this);
    }
    
    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }

    @SuppressWarnings("rawtypes")
    @I18N
    private static native String whenCoalesceIsEmpty(Class<CoalesceExpression> coalesceExpressionType);
    
    @SuppressWarnings("rawtypes")
    @I18N
    private static native String isEmpty(Class<CoalesceExpression> coalesceExpressionType);
}
