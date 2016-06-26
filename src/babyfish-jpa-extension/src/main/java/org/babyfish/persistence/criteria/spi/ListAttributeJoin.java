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

import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.ListAttribute;

import org.babyfish.persistence.criteria.JoinMode;
import org.babyfish.persistence.criteria.XListJoin;
import org.babyfish.persistence.criteria.expression.ListIndexExpression;

/**
 * @author Tao Chen
 */
public class ListAttributeJoin<Z, E> 
extends AbstractPluralAttributeJoin<Z, List<E>,E>
implements XListJoin<Z, E> {

    private static final long serialVersionUID = 6573747466239523361L;

    public ListAttributeJoin(
            AbstractFrom<?, Z> parent,
            ListAttribute<? super Z, E> attribute, 
            JoinType joinType,
            JoinMode joinMode) {
        super(parent, attribute, joinType, joinMode);
    }

    public ListAttributeJoin(
            ListAttributeJoin<Z, E> correlationParent) {
        super(correlationParent);
    }
    
    @Override
    public ListAttribute<Z, E> getAttribute() {
        return (ListAttribute<Z, E>)super.getAttribute();
    }
    
    @Override
    public ListAttribute<Z, E> getModel() {
        return this.getAttribute();
    }

    @Override
    public Expression<Integer> index() {
        return new ListIndexExpression(this.getCriteriaBuilder(), this);
    }
    
    @Override
    public XListJoin<Z, E> on(Expression<Boolean> restriction) {
        return (XListJoin<Z, E>)super.on(restriction);
    }

    @Override
    public XListJoin<Z, E> on(Predicate... restrictions) {
        return (XListJoin<Z, E>)super.on(restrictions);
    }
    
    @Override
    public XListJoin<Z, ? super E> getTreatedParent() {
        return null;
    }
}
