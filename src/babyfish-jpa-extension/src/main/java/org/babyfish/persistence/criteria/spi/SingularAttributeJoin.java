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
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.SingularAttribute;

import org.babyfish.persistence.criteria.JoinMode;
import org.babyfish.persistence.criteria.XJoin;

/**
 * @author Tao Chen
 */
public class SingularAttributeJoin<Z, X> extends AbstractJoin<Z, X> implements XJoin<Z, X> {

    private static final long serialVersionUID = -5576057054752953370L;
    
    private Bindable<X> model;

    public SingularAttributeJoin(
            AbstractFrom<?, Z> parent,
            SingularAttribute<? super Z, ?> attribute, 
            JoinType joinType,
            JoinMode joinMode) {
        super(parent, attribute, joinType, joinMode);
    }

    public SingularAttributeJoin(SingularAttributeJoin<Z, X> correlationParent) {
        super(correlationParent);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SingularAttribute<? super Z, X> getAttribute() {
        return (SingularAttribute<Z, X>)super.getAttribute();
    }

    @Override
    protected boolean isJoinSource() {
        return true;
    }
    
    @Override
    public final Bindable<X> getModel() {
        Bindable<X> model = this.model;
        if (model == null) {
            this.model = model = this.onGetModel();
        }
        return model;
    }

    @SuppressWarnings("unchecked")
    protected Bindable<X> onGetModel() {
        SingularAttribute<? super Z, ?> attribute = this.getAttribute();
        if (Attribute.PersistentAttributeType.EMBEDDED == attribute.getPersistentAttributeType()) {
            return (Bindable<X>)attribute;
        }
        return (Bindable<X>)this.getCriteriaBuilder().getEntityManagerFactory().getMetamodel().managedType(this.getJavaType());
    }
    
    @Override
    public XJoin<Z, ? super X> getTreatedParent() {
        return null;
    }
}
