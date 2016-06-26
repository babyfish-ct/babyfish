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

import javax.persistence.Tuple;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfish.persistence.criteria.ext.DependencyPredicateBuilder;

/**
 * @author Tao Chen
 */
public interface XCriteriaBuilder extends CriteriaBuilder {
    
    XEntityManagerFactory getEntityManagerFactory();

    @Override
    XCriteriaQuery<Object> createQuery();

    @Override
    <T> XCriteriaQuery<T> createQuery(Class<T> resultClass);

    @Override
    XCriteriaQuery<Tuple> createTupleQuery();
    
    @Override
    <T> XCriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity);
    
    @Override
    <T> XCriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity);

    <Y> CompoundSelection<Y> construct(Class<Y> resultClass, Iterable<Selection<?>> selections);
    
    CompoundSelection<Tuple> tuple(Iterable<Selection<?>> selections);

    CompoundSelection<Object[]> array(Iterable<Selection<?>> selections);
    
    Concat concat();
    
    <T> Expression<T> constant(T value);
    
    @Override
    <T> Expression<T> literal(T value);
    
    Predicate like(Expression<String> x, String pattern, LikeMode likeMode);
    
    Predicate insensitivelyLike(Expression<String> x, String pattern);
    
    Predicate insensitivelyLike(Expression<String> x, String pattern, LikeMode likeMode);
    
    Predicate notLike(Expression<String> x, String pattern, LikeMode likeMode);
    
    Predicate notInsensitivelyLike(Expression<String> x, String pattern);
    
    Predicate notInsensitivelyLike(Expression<String> x, String pattern, LikeMode likeMode);
    
    <T> Predicate in(Expression<? extends T> x, Iterable<T> values);
    
    <T> Predicate in(Expression<? extends T> x, Subquery<T> subquery);
        
    @Override
    <X, T extends X> Root<T> treat(Root<X> root, Class<T> type);

    @Override
    <X, K, T, V extends T> XMapJoin<X, K, V> treat(MapJoin<X, K, T> join, Class<V> type);

    @Override
    <X, T, E extends T> XListJoin<X, E> treat(ListJoin<X, T> join, Class<E> type);

    @Override
    <X, T, E extends T> XSetJoin<X, E> treat(SetJoin<X, T> join, Class<E> type);

    @Override
    <X, T, E extends T> XCollectionJoin<X, E> treat(CollectionJoin<X, T> join, Class<E> type);

    @Override
    <X, T, V extends T> XJoin<X, V> treat(Join<X, T> join, Class<V> type);
    
    <X, Y> DependencyPredicateBuilder<X, Y> dependencyPredicateBuilder(From<?, X> from, Class<Y> targetType);

    public interface Concat extends Expression<String> {
        
        Concat value(Expression<String> value);
        
        Concat value(String value);
    }
    
}
