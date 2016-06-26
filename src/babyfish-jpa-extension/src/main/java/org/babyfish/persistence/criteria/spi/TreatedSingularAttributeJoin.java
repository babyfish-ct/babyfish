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

import javax.persistence.metamodel.Bindable;

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XJoin;

/**
 * @author Tao Chen
 */
public class TreatedSingularAttributeJoin<Z, X> extends SingularAttributeJoin<Z, X> {
    
    private static final long serialVersionUID = 4509259014720272748L;

    private SingularAttributeJoin<Z, ? super X> join;
    
    private Class<X> type;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TreatedSingularAttributeJoin(SingularAttributeJoin<Z, ? super X> join, Class<X> type) {
        super(
                (AbstractFrom)join.getParent(), 
                join.getAttribute(), 
                join.getJoinType(), 
                join.getJoinMode());
        Arguments.mustBeCompatibleWithOther("type", type, "join.getJavaType()", join.getJavaType());
        this.join = join;
        this.type = type;
    }
    
    @Override
    public Class<? extends X> getJavaType() {
        return this.type;
    }
    
    @Override
    public String getAlias() {
        return this.join.getAlias();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected Bindable<X> onGetModel() {
        return (Bindable<X>)this.getCriteriaBuilder().getEntityManagerFactory().getMetamodel().entity(this.getJavaType());
    }
    
    @Override
    public XJoin<Z, ? super X> getTreatedParent() {
        return this.join;
    }
}
