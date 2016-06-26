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

import org.babyfish.lang.I18N;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaDelete;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.XSubquery;

/**
 * @author Tao Chen
 */
public class CriteriaDeleteImpl<T> extends AbstractNode implements XCriteriaDelete<T> {
    
    private static final long serialVersionUID = 6817648025819322797L;

    private CommonCriteriaStructure commonCriteriaStructure;
    
    private XRoot<T> root;
    
    protected CriteriaDeleteImpl(XCriteriaBuilder criteriaBuilder) {
        super(criteriaBuilder);
        this.commonCriteriaStructure = new CommonCriteriaStructure(this);
    }

    @Override
    public XRoot<T> getRoot() {
        return this.root;
    }

    @Override
    public Predicate getRestriction() {
        return this.commonCriteriaStructure.getRestriction();
    }

    @Override
    public <U> XSubquery<U> subquery(Class<U> type) {
        this.checkState();
        return new CriteriaSubqueryImpl<>(this, type);
    }

    @Override
    public XRoot<T> from(Class<T> entityClass) {
        this.checkState();
        this.rootMustBeNull();
        return this.root = this.commonCriteriaStructure.createRoot(entityClass);
    }

    @Override
    public XRoot<T> from(EntityType<T> entity) {
        this.checkState();
        this.rootMustBeNull();
        return this.root = this.commonCriteriaStructure.createRoot(entity);
    }

    @Override
    public XCriteriaDelete<T> where(Expression<Boolean> restriction) {
        this.checkState();
        this.commonCriteriaStructure.where(restriction);
        return this;
    }

    @Override
    public XCriteriaDelete<T> where(Predicate... restrictions) {
        this.checkState();
        this.commonCriteriaStructure.where(restrictions);
        return this;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitCriteriaDelete(this);
    }
    
    private void rootMustBeNull() {
        if (this.root != null) {
            throw new IllegalStateException(rootMustBeNull(this.getClass()));
        }
    }

    @I18N
    private static native String rootMustBeNull(Class<?> thisType);
}
