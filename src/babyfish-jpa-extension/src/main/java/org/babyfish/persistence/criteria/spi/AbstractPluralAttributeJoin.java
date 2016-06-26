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

import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;

import org.babyfish.persistence.criteria.JoinMode;
import org.babyfish.persistence.criteria.XPluralJoin;

/**
 * @author Tao Chen
 */
public abstract class AbstractPluralAttributeJoin<Z, C, E> 
extends AbstractJoin<Z, E> 
implements XPluralJoin<Z, C, E> {

    private static final long serialVersionUID = -4725033045017892893L;

    public AbstractPluralAttributeJoin(
            AbstractFrom<?, Z> parent,
            PluralAttribute<? super Z, C, ?> attribute, 
            JoinType joinType,
            JoinMode joinMode) {
        super(parent, attribute, joinType, joinMode);
    }

    public AbstractPluralAttributeJoin(AbstractPluralAttributeJoin<Z, C, E> correlationParent) {
        super(correlationParent);
    }

    public PluralAttribute<? super Z, C, E> getModel() {
        return this.getAttribute();
    }

    @SuppressWarnings("unchecked")
    public PluralAttribute<Z, C, E> getAttribute() {
        return (PluralAttribute<Z, C, E>)super.getAttribute();
    }
    
    @Override
    public boolean isBasicCollection() {
        return Type.PersistenceType.BASIC.equals(this.getAttribute().getElementType().getPersistenceType());
    }
    
    @Override
    protected boolean isJoinSource() {
        return !this.isBasicCollection();
    }
}
