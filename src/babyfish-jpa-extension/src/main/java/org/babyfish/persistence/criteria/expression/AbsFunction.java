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

import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractFunction;

/**
 * @author Tao Chen
 */
public class AbsFunction<T extends Number> extends AbstractFunction<T> {
    
    private static final long serialVersionUID = 5527114451112290525L;
    
    private static final String FUNCTION_NAME = "abs";

    public AbsFunction(XCriteriaBuilder criteriaBuilder, Expression<? extends T> number) {
        super(criteriaBuilder, FUNCTION_NAME, number);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends T> getJavaType() {
        return ((Expression<? extends T>)this.getArguments().get(0)).getJavaType();
    }
    
}
