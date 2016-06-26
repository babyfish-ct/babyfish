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

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.ListAttributeJoin;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class ListIndexExpression extends AbstractExpression<Integer> {

    private static final long serialVersionUID = 1680783293985230099L;
    
    ListAttributeJoin<?, ?> listAttributeJoin;
    
    public ListIndexExpression(XCriteriaBuilder criteriaBuilder, ListAttributeJoin<?, ?> listAttributeJoin) {
        super(criteriaBuilder);
        this.listAttributeJoin = Arguments.mustNotBeNull("listAttributeJoin", listAttributeJoin);
        this.mustUnderSameCriteriaBuilder("listAttributeJoin", listAttributeJoin);
    }
    
    public ListAttributeJoin<?, ?> getListAttributeJoin() {
        return listAttributeJoin;
    }

    @Override
    public Class<? extends Integer> getJavaType() {
        return Integer.class;
    }

    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitListIndexExpression(this);
    }

}
