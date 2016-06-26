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

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.expression.AsExpression;

/**
 * @author Tao Chen
 */
public abstract class AbstractExpression<T> extends AbstractSelection<T> implements Expression<T> {

    private static final long serialVersionUID = 1779620617070697476L;
    
    protected AbstractExpression(XCriteriaBuilder criteriaBuilder) {
        super(criteriaBuilder);
    }
    
    @Override
    public Predicate isNull() {
        return this.getCriteriaBuilder().isNull(this);
    }

    @Override
    public Predicate isNotNull() {
        return this.getCriteriaBuilder().isNotNull(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Predicate in(Object... values) {
        In<T> in = this.getCriteriaBuilder().in(this);
        for (Object value : values) {
            in.value((T)value);
        }
        return in;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Predicate in(Expression<?>... values) {
        In<T> in = this.getCriteriaBuilder().in(this);
        for (Object value : values) {
            in.value((T)value);
        }
        return in;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Predicate in(Collection<?> values) {
        In<T> in = this.getCriteriaBuilder().in(this);
        for (Object value : values) {
            in.value((T)value);
        }
        return in;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Predicate in(Expression<Collection<?>> values) {
        In<T> in = this.getCriteriaBuilder().in(this);
        in.value((Expression)values);
        return in;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> Expression<X> as(Class<X> type) {
        if (type == this.getJavaType()) {
            return (Expression<X>)this;
        }
        return new AsExpression<X>(this.getCriteriaBuilder(), this, type);
    }
    
    public abstract int getPriority();
    
}
