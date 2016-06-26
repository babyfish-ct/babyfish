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

import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.persistence.criteria.Assignment;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaUpdate;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.XSubquery;

/**
 * @author Tao Chen
 */
public class CriteriaUpdateImpl<T> extends AbstractNode implements XCriteriaUpdate<T> {
    
    private static final long serialVersionUID = -779353911411874272L;
    
    private CommonCriteriaStructure commonCriteriaStructure;
    
    private XRoot<T> root;
    
    private List<Assignment> assignments;

    protected CriteriaUpdateImpl(XCriteriaBuilder criteriaBuilder) {
        super(criteriaBuilder);
        this.commonCriteriaStructure = new CommonCriteriaStructure(this);
        this.assignments = new ArrayList<>();
    }

    @Override
    public XRoot<T> getRoot() {
        return this.root;
    }

    @Override
    public List<Assignment> getAssignments() {
        return MACollections.unmodifiable(this.assignments);
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
    public <Y, X extends Y> XCriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, X value) {
        this.checkState();
        this.rootMustNotBeNull();
        Path<Y> path = this.root.get(attribute);
        Expression<? extends Y> expression;
        if (value == null) {
            expression = this.getCriteriaBuilder().nullLiteral(path.getJavaType());
        } else {
            expression = this.getCriteriaBuilder().literal(value);
        }
        this.assignments.add(new AssignmentImpl(this.getCriteriaBuilder(), path, expression));
        return this;
    }
    
    @Override
    public <Y> XCriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value) {
        this.checkState();
        this.rootMustNotBeNull();
        Path<Y> path = this.root.get(attribute);
        this.assignments.add(new AssignmentImpl(this.getCriteriaBuilder(), path, value));
        return this;
    }

    @Override
    public <Y, X extends Y> XCriteriaUpdate<T> set(Path<Y> attribute, X value) {
        this.checkState();
        this.rootMustNotBeNull();
        this.pathMustBeChildOfRoot(attribute);
        Expression<? extends Y> expression;
        if (value == null) {
            expression = this.getCriteriaBuilder().nullLiteral(attribute.getJavaType());
        } else {
            expression = this.getCriteriaBuilder().literal(value);
        }
        this.assignments.add(new AssignmentImpl(this.getCriteriaBuilder(), attribute, expression));
        return this;
    }

    @Override
    public <Y> XCriteriaUpdate<T> set(Path<Y> attribute, Expression<? extends Y> value) {
        this.checkState();
        this.rootMustNotBeNull();
        this.pathMustBeChildOfRoot(attribute);
        this.assignments.add(new AssignmentImpl(this.getCriteriaBuilder(), attribute, value));
        return this;
    }

    @Override
    public XCriteriaUpdate<T> set(String attributeName, Object value) {
        this.checkState();
        this.rootMustNotBeNull();
        Path<?> path = this.root.get(attributeName);
        Expression<?> expression;
        if (value == null) {
            expression = this.getCriteriaBuilder().nullLiteral(path.getJavaType());
        } else {
            expression = this.getCriteriaBuilder().literal(value);
        }
        this.assignments.add(new AssignmentImpl(this.getCriteriaBuilder(), path, expression));
        return this;
    }

    @Override
    public XCriteriaUpdate<T> where(Expression<Boolean> restriction) {
        this.checkState();
        this.commonCriteriaStructure.where(restriction);
        return this;
    }

    @Override
    public XCriteriaUpdate<T> where(Predicate... restrictions) {
        this.checkState();
        this.commonCriteriaStructure.where(restrictions);
        return this;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitCriteriaUpdate(this);
    }
    
    private void rootMustBeNull() {
        if (this.root != null) {
            throw new IllegalStateException(rootMustBeNull(this.getClass()));
        }
    }

    private void rootMustNotBeNull() {
        if (this.root == null) {
            throw new IllegalStateException(rootMustNotBeNull(this.getClass()));
        }
    }
    
    private void pathMustBeChildOfRoot(Path<?> attribute) {
        Arguments.mustBeInstanceOfValue("attribute.getModel()", attribute.getModel(), SingularAttribute.class);
        for (Path<?> parent = attribute.getParentPath(); parent != null; parent = parent.getParentPath()) {
            if (parent == this.root) {
                return;
            }
        }
        throw new IllegalArgumentException(pathMustBeChildOfRoot(this.getClass()));
    }
    
    @I18N
    private static native String rootMustBeNull(Class<?> thisType);
        
    @I18N
    private static native String rootMustNotBeNull(Class<?> thisType);
        
    @I18N
    private static native String pathMustBeChildOfRoot(Class<?> thisType);
}
