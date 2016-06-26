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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class ConstantExpression<T> extends AbstractExpression<T> {
    
    private static final long serialVersionUID = 6757283545751309755L;
    
    private T value;

    public ConstantExpression(XCriteriaBuilder criteriaBuilder, T value) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("value", value);
        Arguments.mustBeInstanceOfAnyOfValue(
                "value", 
                value, 
                Boolean.class,
                Character.class,
                Short.class,
                Integer.class,
                Long.class,
                Float.class,
                Double.class,
                BigInteger.class,
                BigDecimal.class,
                String.class);
        this.value = value;
    }
    
    public T getValue() {
        return this.value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends T> getJavaType() {
        return (Class<? extends T>)this.value.getClass();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitConstantExpression(this);
    }
    
    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }

}
