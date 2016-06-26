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
import java.util.Set;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

/**
 * @author Tao Chen
 */
public interface XSubquery<T> extends Subquery<T>, XAbstractQuery<T> {

    @Override
    XSubquery<T> select(Expression<T> expression);

    @Override
    XSubquery<T> where(Expression<Boolean> restriction);

    @Override
    XSubquery<T> where(Predicate... restrictions);

    @Override
    XSubquery<T> groupBy(Expression<?>... grouping);

    @Override
    XSubquery<T> groupBy(List<Expression<?>> grouping);

    @Override
    XSubquery<T> having(Expression<Boolean> restriction);

    @Override
    XSubquery<T> having(Predicate... restrictions);

    @Override
    XSubquery<T> distinct(boolean distinct);

    @Override
    <Y> XRoot<Y> correlate(Root<Y> parentRoot);

    @Override
    <X, Y> XJoin<X, Y> correlate(Join<X, Y> parentJoin);

    @Override
    <X, Y> XCollectionJoin<X, Y> correlate(CollectionJoin<X, Y> parentCollection);

    @Override
    <X, Y> XSetJoin<X, Y> correlate(SetJoin<X, Y> parentSet);

    @Override
    <X, Y> XListJoin<X, Y> correlate(ListJoin<X, Y> parentList);

    @Override
    <X, K, V> XMapJoin<X, K, V> correlate(MapJoin<X, K, V> parentMap);

    @Override
    XAbstractQuery<?> getParent();

    @Override
    Set<Join<?, ?>> getCorrelatedJoins();
    
}
