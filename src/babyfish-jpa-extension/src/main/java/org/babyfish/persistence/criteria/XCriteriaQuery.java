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
package org.babyfish.persistence.criteria;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

import org.babyfish.collection.XOrderedSet;

/**
 * @author Tao Chen
 */
public interface XCriteriaQuery<T> extends CriteriaQuery<T>, XAbstractQuery<T> {
    
    @Override
    XOrderedSet<ParameterExpression<?>> getParameters();

    @Override
    XCriteriaQuery<T> select(Selection<? extends T> selection);

    @Override
    XCriteriaQuery<T> multiselect(Selection<?>... selections);

    @Override
    XCriteriaQuery<T> multiselect(List<Selection<?>> selectionList);

    @Override
    XCriteriaQuery<T> distinct(boolean distinct);

    @Override
    XCriteriaQuery<T> where(Expression<Boolean> restriction);

    @Override
    XCriteriaQuery<T> where(Predicate... restrictions);

    @Override
    XCriteriaQuery<T> groupBy(Expression<?>... grouping);

    @Override
    XCriteriaQuery<T> groupBy(List<Expression<?>> grouping);

    @Override
    XCriteriaQuery<T> having(Expression<Boolean> restriction);

    @Override
    XCriteriaQuery<T> having(Predicate... restrictions);

    @Override
    XCriteriaQuery<T> orderBy(Order... o);

    @Override
    XCriteriaQuery<T> orderBy(List<Order> o);
    
}
