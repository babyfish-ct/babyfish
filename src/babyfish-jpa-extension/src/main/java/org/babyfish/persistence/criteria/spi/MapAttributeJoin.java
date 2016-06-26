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

import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.MapAttribute;

import org.babyfish.persistence.criteria.JoinMode;
import org.babyfish.persistence.criteria.XMapJoin;
import org.babyfish.persistence.criteria.expression.MapEntryExpression;

/**
 * @author Tao Chen
 */
public class MapAttributeJoin<Z, K, V> 
extends AbstractPluralAttributeJoin<Z, Map<K, V>, V>
implements XMapJoin<Z, K, V> {

    private static final long serialVersionUID = -8892423089319639884L;

    public MapAttributeJoin(
            AbstractFrom<?, Z> parent,
            MapAttribute<? super Z, K, V> attribute,
            JoinType joinType,
            JoinMode joinMode) {
        super(parent, attribute, joinType, joinMode);
    }

    public MapAttributeJoin(
            MapAttributeJoin<Z, K, V> correlationParent) {
        super(correlationParent);
    }

    @Override
    public MapAttribute<Z, K, V> getAttribute() {
        return (MapAttribute<Z, K, V>)super.getAttribute();
    }
    
    @Override
    public MapAttribute<Z, K, V> getModel() {
        return this.getAttribute();
    }

    @Override
    public Expression<Entry<K, V>> entry() {
        return new MapEntryExpression<K, V>(this.getCriteriaBuilder(), this);
    }

    @Override
    public Path<K> key() {
        return new MapKeyPath<>(this);
    }

    @Override
    public Path<V> value() {
        return this;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public XMapJoin<Z, K, V> on(Expression<Boolean> restriction) {
        return (XMapJoin<Z, K, V>)super.on(restriction);
    }

    @SuppressWarnings("unchecked")
    @Override
    public XMapJoin<Z, K, V> on(Predicate... restrictions) {
        return (XMapJoin<Z, K, V>)super.on(restrictions);
    }
    
    @Override
    public XMapJoin<Z, K, ? super V> getTreatedParent() {
        return null;
    }
}
