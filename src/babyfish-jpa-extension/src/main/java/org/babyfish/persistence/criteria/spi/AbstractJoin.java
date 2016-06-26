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

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.persistence.criteria.JoinMode;
import org.babyfish.persistence.criteria.XFrom;
import org.babyfish.persistence.criteria.XJoin;

/**
 * @author Tao Chen
 */
public abstract class AbstractJoin<Z, X> extends AbstractFrom<Z, X> implements XJoin<Z, X> {

    private static final long serialVersionUID = 1924213188607394413L;
    
    private AbstractFrom<?, Z> parent;
    
    private Attribute<? super Z, ?> attribute;
    
    private JoinType joinType;
    
    private JoinMode joinMode;
    
    private Predicate on;

    protected AbstractJoin(
            AbstractFrom<?, Z> parent,
            Attribute<? super Z, ?> attribute,
            JoinType joinType,
            JoinMode joinMode) {
        super(parent.getCommonAbstractCriteria(), Arguments.mustNotBeNull("parent", parent));
        this.parent = parent;
        this.attribute = Arguments.mustNotBeNull("attribute", attribute);
        this.joinType = Arguments.mustNotBeNull("joinType", joinType);
        this.joinMode = Arguments.mustNotBeNull("joinMode", joinMode);
    }

    protected AbstractJoin(AbstractJoin<Z, X> correlationParent) {
        super(correlationParent);
        this.parent = correlationParent.parent;
        this.attribute = correlationParent.getAttribute();
        this.joinType = correlationParent.getJoinType();
    }
    
    @Override
    public Attribute<? super Z, ?> getAttribute() {
        return this.attribute;
    }

    @Override
    public JoinType getJoinType() {
        return this.joinType;
    }
    
    @Override
    public JoinMode getJoinMode() {
        return this.joinMode;
    }

    @Override
    public XFrom<?, Z> getParent() {
        return this.parent;
    }
    
    @Override
    public XJoin<Z, X> getCorrelationParent() {
        return (XJoin<Z, X>)super.getCorrelationParent();
    }
    
    @Override
    public XJoin<Z, X> alias(String alias) {
        return (XJoin<Z, X>)super.alias(alias);
    }

    @Override
    public XJoin<Z, X> on(Expression<Boolean> restriction) {
        Predicate predicate = this.getCriteriaBuilder().isTrue(restriction);
        this.setOn(predicate);
        return this;
    }

    @Override
    public XJoin<Z, X> on(Predicate... restrictions) {
        Predicate predicate = this.getCriteriaBuilder().and(restrictions);
        this.setOn(predicate);
        return this;
    }
    
    @Override
    public Predicate getOn() {
        return this.on;
    }

    void mergeJoinType(JoinType joinType) {
        if (this.joinType != joinType) {
            this.joinType = JoinType.INNER;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends X> getJavaType() {
        return (Class<? extends X>)this.attribute.getJavaType();
    }
    
    @Override
    public String toString() {
        return this.getParent().toString() +
                ' ' +
                this.joinType.name().toLowerCase() + 
                " join(mode = " +
                this.joinMode.name().toLowerCase() +
                ") " +
                this.attribute.getName();
    }
    
    private void setOn(Predicate on) {
        if (AbstractPredicate.isNullOrEmpty(on)) {
            this.on = null;
        } else {
            if (!this.joinMode.isNew()) {
                throw new IllegalStateException(
                        mergeJoinDoesNotSupportOn(
                                JoinMode.OPTIONALLY_MERGE_EXISTS, 
                                JoinMode.REQUIRED_TO_MERGE_EXISTS)
                );
            }
            this.on = on;
        }
    }

    @I18N
    private static native String mergeJoinDoesNotSupportOn(JoinMode ... joinModes);
}
