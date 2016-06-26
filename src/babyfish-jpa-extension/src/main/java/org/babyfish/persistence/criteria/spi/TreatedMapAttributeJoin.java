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

import javax.persistence.metamodel.MapAttribute;

import org.babyfish.persistence.criteria.XMapJoin;

/**
 * @author Tao Chen
 */
public class TreatedMapAttributeJoin<Z, K, V> extends MapAttributeJoin<Z, K, V> {

    private static final long serialVersionUID = 7460030398239990507L;

    private MapAttributeJoin<Z, K, ? super V> join;
    
    private Class<V> type;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TreatedMapAttributeJoin(MapAttributeJoin<Z, K, ? super V> join, Class<V> type) {
        super(
                (AbstractFrom)join.getParent(), 
                (MapAttribute)join.getAttribute(), 
                join.getJoinType(), 
                join.getJoinMode());
        this.join = join;
        this.type = type;
    }

    @Override
    public Class<? extends V> getJavaType() {
        return this.type;
    }
    
    @Override
    public String getAlias() {
        return this.join.getAlias();
    }
    
    @Override
    public XMapJoin<Z, K, ? super V> getTreatedParent() {
        return this.join;
    }
}
