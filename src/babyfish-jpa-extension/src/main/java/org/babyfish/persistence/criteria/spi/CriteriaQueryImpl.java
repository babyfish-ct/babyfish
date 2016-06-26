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

import java.util.Collection;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.EntityType;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.XSubquery;

/**
 * @author Tao Chen
 */
public class CriteriaQueryImpl<T> extends AbstractNode implements XCriteriaQuery<T> {
    
    private static final long serialVersionUID = 5646747751090857605L;

    private QueryStructure<T> queryStructure;
    
    private List<Order> orderList;

    protected CriteriaQueryImpl(
            XCriteriaBuilder criteriaBuilder, 
            Class<T> resultType) {
        super(criteriaBuilder);
        this.queryStructure = new QueryStructure<T>(
                this, 
                Arguments.mustNotBeNull("resultType", resultType));
    }

    @Override
    public XOrderedSet<ParameterExpression<?>> getParameters() {
        XOrderedSet<ParameterExpression<?>> parameters = new LinkedHashSet<>();
        this.accept(new ParameterVisitor(parameters));
        return parameters;
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
    public Selection<T> getSelection() {
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
    public List<Order> getOrderList() {
        List<Order> orderList = this.orderList;
        if (orderList == null) {
            return MACollections.emptyList();
        }
        return orderList;
    }

    @Override
    public XCriteriaQuery<T> distinct(boolean distinct) {
        this.checkState();
        this.queryStructure.distinct(distinct);
        return this;
    }

    @Override
    public XCriteriaQuery<T> select(Selection<? extends T> selection) {
        this.checkState();
        this.queryStructure.select(selection);
        return this;
    }

    @Override
    public XCriteriaQuery<T> multiselect(Selection<?>... selections) {
        return this.multiselect(MACollections.wrap(selections));
    }

    @SuppressWarnings("unchecked")
    @Override
    public XCriteriaQuery<T> multiselect(List<Selection<?>> selectionList) {
        this.checkState();
        Selection<? extends T> selection;
        if (Tuple.class.isAssignableFrom(this.getResultType())) {
            selection = (Selection<? extends T>)this.getCriteriaBuilder().tuple(selectionList);
        } else if (getResultType().isArray()) {
            selection = (Selection<? extends T>)this.getCriteriaBuilder().array(selectionList);
        } else if (Object.class.equals(getResultType())) {
            switch (selectionList.size()) {
                case 0:
                    throw new IllegalArgumentException(
                            "empty selections passed to criteria query typed as Object");
                case 1:
                    selection = (Selection<? extends T>)selectionList.get(0);
                    break;
                default:
                    selection = (Selection<? extends T>)this.getCriteriaBuilder().array(selectionList);
            }
        }
        else {
            selection = this.getCriteriaBuilder().construct(getResultType(), selectionList);
        }
        return this.select(selection);
    }

    @Override
    public <X> XRoot<X> from(Class<X> entityClass) {
        this.checkState();
        return this.queryStructure.from(entityClass);
    }

    @Override
    public <X> XRoot<X> from(EntityType<X> entityType) {
        this.checkState();
        return this.queryStructure.from(entityType);
    }

    @Override
    public XCriteriaQuery<T> where(Expression<Boolean> restriction) {
        this.checkState();
        this.queryStructure.where(restriction);
        return this;
    }

    @Override
    public XCriteriaQuery<T> where(Predicate... restrictions) {
        this.checkState();
        this.queryStructure.where(restrictions);
        return this;
    }

    @Override
    public XCriteriaQuery<T> groupBy(Expression<?>... grouping) {
        this.checkState();
        this.queryStructure.groupBy(grouping);
        return this;
    }

    @Override
    public XCriteriaQuery<T> groupBy(List<Expression<?>> grouping) {
        this.checkState();
        this.queryStructure.groupBy(grouping);
        return this;
    }

    @Override
    public XCriteriaQuery<T> having(Expression<Boolean> restriction) {
        this.checkState();
        this.queryStructure.having(restriction);
        return this;
    }

    @Override
    public XCriteriaQuery<T> having(Predicate... restrictions) {
        this.checkState();
        this.queryStructure.having(restrictions);
        return this;
    }

    @Override
    public XCriteriaQuery<T> orderBy(Order... o) {
        return this.orderBy(MACollections.wrap(o));
    }

    @Override
    public XCriteriaQuery<T> orderBy(List<Order> o) {
        this.checkState();
        List<Order> orderList;
        if (o != null && !o.isEmpty()) {
            orderList = new ArrayList<Order>(o.size());
            for (Order order : o) {
                if (order != null) {
                    orderList.add(order);
                }
            }
            if (orderList.isEmpty()) {
                orderList = null;
            } else {
                orderList = MACollections.unmodifiable(orderList);
            }
        } else {
            orderList = null;
        }
        this.orderList = orderList;
        return this;
    }

    @Override
    public <U> XSubquery<U> subquery(Class<U> type) {
        this.checkState();
        return new CriteriaSubqueryImpl<U>(this, type);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitCriteriaQuery(this);
    }

    private static class ParameterVisitor extends AbstractVisitor {
        
        private Collection<ParameterExpression<?>> parameters;
        
        ParameterVisitor(XOrderedSet<ParameterExpression<?>> parameters) {
            this.parameters = parameters;
        }

        @Override
        public void visitParameterExpression(ParameterExpression<?> parameterExpression) {
            this.parameters.add(parameterExpression);
        }
    }
}
