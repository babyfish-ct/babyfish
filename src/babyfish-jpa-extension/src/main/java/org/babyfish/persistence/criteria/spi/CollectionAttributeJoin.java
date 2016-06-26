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

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.CollectionAttribute;

import org.babyfish.persistence.criteria.JoinMode;
import org.babyfish.persistence.criteria.XCollectionJoin;

/**
 * @author Tao Chen
 */
public class CollectionAttributeJoin<Z, E> 
extends AbstractPluralAttributeJoin<Z, Collection<E>, E>
implements XCollectionJoin<Z, E> {

    private static final long serialVersionUID = 6573747466239523361L;

    public CollectionAttributeJoin(
            AbstractFrom<?, Z> parent,
            CollectionAttribute<? super Z, E> attribute, 
            JoinType joinType,
            JoinMode joinMode) {
        super(parent, attribute, joinType, joinMode);
    }

    public CollectionAttributeJoin(
            CollectionAttributeJoin<Z, E> correlationParent) {
        super(correlationParent);
    }
    
    @Override
    public CollectionAttribute<Z, E> getAttribute() {
        return (CollectionAttribute<Z, E>)super.getAttribute();
    }
    
    @Override
    public CollectionAttribute<Z, E> getModel() {
        return this.getAttribute();
    }
    
    @Override
    public XCollectionJoin<Z, E> on(Expression<Boolean> restriction) {
        return (XCollectionJoin<Z, E>)super.on(restriction);
    }

    @Override
    public XCollectionJoin<Z, E> on(Predicate... restrictions) {
        return (XCollectionJoin<Z, E>)super.on(restrictions);
    }
    
    @Override
    public XCollectionJoin<Z, ? super E> getTreatedParent() {
        return null;
    }
}
