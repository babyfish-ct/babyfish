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

import javax.persistence.metamodel.ListAttribute;

import org.babyfish.persistence.criteria.XListJoin;

/**
 * @author Tao Chen
 */
public class TreatedListAttributeJoin<Z, E> extends ListAttributeJoin<Z, E> {

    private static final long serialVersionUID = 7460030398239990507L;

    private ListAttributeJoin<Z, ? super E> join;
    
    private Class<E> type;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TreatedListAttributeJoin(ListAttributeJoin<Z, ? super E> join, Class<E> type) {
        super(
                (AbstractFrom)join.getParent(), 
                (ListAttribute)join.getAttribute(), 
                join.getJoinType(), 
                join.getJoinMode());
        this.join = join;
        this.type = type;
    }

    @Override
    public Class<? extends E> getJavaType() {
        return this.type;
    }
    
    @Override
    public String getAlias() {
        return this.join.getAlias();
    }
    
    @Override
    public XListJoin<Z, ? super E> getTreatedParent() {
        return this.join;
    }
}
