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

import javax.persistence.metamodel.EntityType;

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCommonAbstractCriteria;
import org.babyfish.persistence.criteria.XRoot;

/**
 * @author Tao Chen
 */
public class RootImpl<X> extends AbstractFrom<X, X> implements XRoot<X> {

    private static final long serialVersionUID = -8997495674215644487L;
    
    private EntityType<X> entityType;
    
    public RootImpl(XCommonAbstractCriteria commonAbstractCriteria, EntityType<X> entityType) {
        super(commonAbstractCriteria, null);
        this.entityType = Arguments.mustNotBeNull("entityType", entityType);
    }
    
    public RootImpl(RootImpl<X> correlationParent) {
        super(correlationParent);
        this.entityType = correlationParent.getModel();
    }
    
    @Override
    public XRoot<X> getCorrelationParent() {
        return (XRoot<X>)super.getCorrelationParent();
    }

    @Override
    public Class<? extends X> getJavaType() {
        return this.entityType.getJavaType();
    }
    
    @Override
    public EntityType<X> getModel() {
        return this.entityType;
    }

    @Override
    protected boolean isJoinSource() {
        return true;
    }

    @Override
    public String toString() {
        return "from [" + this.entityType.getJavaType().getName() + ']';
    }
    
    @Override
    public XRoot<X> alias(String alias) {
        return (XRoot<X>)super.alias(alias);
    }
    
    @Override
    public XRoot<? super X> getTreatedParent() {
        return null;
    }
}
