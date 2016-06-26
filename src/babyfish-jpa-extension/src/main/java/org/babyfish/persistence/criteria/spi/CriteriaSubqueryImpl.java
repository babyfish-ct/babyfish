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
import java.util.Set;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.EntityType;

import org.babyfish.collection.XOrderedSet;
import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XAbstractQuery;
import org.babyfish.persistence.criteria.XCollectionJoin;
import org.babyfish.persistence.criteria.XCommonAbstractCriteria;
import org.babyfish.persistence.criteria.XJoin;
import org.babyfish.persistence.criteria.XListJoin;
import org.babyfish.persistence.criteria.XMapJoin;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.XSetJoin;
import org.babyfish.persistence.criteria.XSubquery;
import org.babyfish.persistence.criteria.expression.PriorityConstants;

/**
 * @author Tao Chen
 */
public class CriteriaSubqueryImpl<T> extends AbstractExpression<T> implements XSubquery<T> {

    private static final long serialVersionUID = -1135488611686474655L;
    
    private CommonAbstractCriteria parent;
    
    private QueryStructure<T> queryStructure;

    public CriteriaSubqueryImpl(XCommonAbstractCriteria parent, Class<T> resultType) {
        super(Arguments.mustNotBeNull("parent", parent).getCriteriaBuilder());
        this.parent = parent;
        this.queryStructure = new QueryStructure<T>(
                this, 
                Arguments.mustNotBeNull("resultType", resultType));
    }

    @Override
    public CommonAbstractCriteria getContainingQuery() {
        return this.parent;
    }

    @Override
    public XAbstractQuery<?> getParent() {
        return (XAbstractQuery<?>)this.parent;
    }

    @Override
    public Class<? extends T> getJavaType() {
        return this.getResultType();
    }

    @Override
    public Class<T> getResultType() {
        return this.queryStructure.getResultType();
    }

    @Override
    public boolean isDistinct() {
        return this.queryStructure.isDistinct();
    }

    @Override
    public Expression<T> getSelection() {
        return this.queryStructure.getSelection();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public XOrderedSet<Root<?>> getRoots() {
        return (XOrderedSet)this.getXRoots();
    }

    @Override
    public XOrderedSet<XRoot<?>> getXRoots() {
        return this.queryStructure.getXRoots();
    }

    @Override
    public Predicate getRestriction() {
        return this.queryStructure.getRestriction();
    }

    @Override
    public List<Expression<?>> getGroupList() {
        return this.queryStructure.getGroupList();
    }

    @Override
    public Predicate getGroupRestriction() {
        return this.queryStructure.getGroupRestriction();
    }

    @Override
    public Set<Join<?, ?>> getCorrelatedJoins() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public XSubquery<T> distinct(boolean distinct) {
        this.queryStructure.distinct(distinct);
        return this;
    }

    @Override
    public XSubquery<T> select(Expression<T> expression) {
        this.queryStructure.select(expression);
        return this;
    }

    @Override
    public <X> XRoot<X> from(Class<X> entityClass) {
        return this.queryStructure.from(entityClass);
    }

    @Override
    public <X> XRoot<X> from(EntityType<X> entityType) {
        return this.queryStructure.from(entityType);
    }

    @Override
    public XSubquery<T> where(Expression<Boolean> restriction) {
        this.queryStructure.where(restriction);
        return this;
    }

    @Override
    public XSubquery<T> where(Predicate... restrictions) {
        this.queryStructure.where(restrictions);
        return this;
    }

    @Override
    public XSubquery<T> groupBy(Expression<?>... grouping) {
        this.queryStructure.groupBy(grouping);
        return this;
    }

    @Override
    public XSubquery<T> groupBy(List<Expression<?>> grouping) {
        this.queryStructure.groupBy(grouping);
        return this;
    }

    @Override
    public XSubquery<T> having(Expression<Boolean> restriction) {
        this.queryStructure.having(restriction);
        return this;
    }

    @Override
    public XSubquery<T> having(Predicate... restrictions) {
        this.queryStructure.having(restrictions);
        return this;
    }

    @Override
    public <Y> XRoot<Y> correlate(Root<Y> parentRoot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> XJoin<X, Y> correlate(Join<X, Y> parentJoin) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> XCollectionJoin<X, Y> correlate(
            CollectionJoin<X, Y> parentCollection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> XSetJoin<X, Y> correlate(SetJoin<X, Y> parentSet) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, Y> XListJoin<X, Y> correlate(ListJoin<X, Y> parentList) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X, K, V> XMapJoin<X, K, V> correlate(MapJoin<X, K, V> parentMap) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <U> XSubquery<U> subquery(Class<U> type) {
        return new CriteriaSubqueryImpl<U>(this, type);
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visitSubquery(this);
    }
    
    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }
}
