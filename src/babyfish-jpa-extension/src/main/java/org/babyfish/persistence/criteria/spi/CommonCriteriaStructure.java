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
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.persistence.criteria.XCommonAbstractCriteria;
import org.babyfish.persistence.criteria.XRoot;

/**
 * @author Tao Chen
 */
class CommonCriteriaStructure {

    private XCommonAbstractCriteria owner;
    
    private Predicate restriction;
    
    CommonCriteriaStructure(XCommonAbstractCriteria owner) {
        this.owner = owner;
    }
    
    public XCommonAbstractCriteria getOwner() {
        return this.owner;
    }

    public Predicate getRestriction() {
        return this.restriction;
    }

    public <X> XRoot<X> createRoot(Class<X> entityClass) {
        EntityType<X> entityType;
        try {
            entityType = 
                    this
                    .owner
                    .getCriteriaBuilder()
                    .getEntityManagerFactory()
                    .getMetamodel()
                    .entity(entityClass);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    invalidJPAEntityClass(entityClass),
                    ex
            );
        }
        return this.createRoot(entityType);
    }

    public <X> XRoot<X> createRoot(EntityType<X> entity) {
        return new RootImpl<X>(this.owner, Arguments.mustNotBeNull("entity", entity));
    }

    public void where(Expression<Boolean> restriction) {
        Predicate predicate = this.owner.getCriteriaBuilder().isTrue(restriction);
        this.restriction = AbstractPredicate.isNullOrEmpty(predicate) ? null : predicate;
    }

    public void where(Predicate... restrictions) {
        Predicate predicate = this.owner.getCriteriaBuilder().and(restrictions);
        this.restriction = AbstractPredicate.isNullOrEmpty(predicate) ? null : predicate;
    }
    
    @I18N
    private static native String invalidJPAEntityClass(Class<?> entityClass);
}
