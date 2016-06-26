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

import java.util.Map.Entry;

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.MapAttributeJoin;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class MapEntryExpression<K, V> extends AbstractExpression<Entry<K, V>> {

    private static final long serialVersionUID = 4579678359801803949L;
    
    private MapAttributeJoin<?, K, V> mapAttributeJoin;

    public MapEntryExpression(
            XCriteriaBuilder criteriaBuilder,
            MapAttributeJoin<?, K, V> mapAttributeJoin) {
        super(criteriaBuilder);
        this.mapAttributeJoin = Arguments.mustNotBeNull("mapAttributeJoin", mapAttributeJoin);
        this.mustUnderSameCriteriaBuilder("mapAttributeJoin", mapAttributeJoin);
    }
    
    public MapAttributeJoin<?, K, V> getMapAttributeJoin() {
        return this.mapAttributeJoin;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Class<? extends Entry<K, V>> getJavaType() {
        return (Class)Entry.class;
    }

    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitMapEntryExpression(this);
    }

}
