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

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.babyfish.collection.XOrderedSet;

/**
 * @author Tao Chen
 */
@SuppressWarnings("hiding")
public interface XFrom<Z, X> extends From<Z, X>, XFetchParent<Z, X> {
    
    @Override
    XFrom<Z, X> alias(String alias);
    
    @Override
    XOrderedSet<Join<X, ?>> getJoins();
    
    XOrderedSet<XJoin<X, ?>> getXJoins();
    
    @Override
    XFrom<Z, X> getCorrelationParent();
    
    @Override
    <Y> XJoin<X, Y> join(SingularAttribute<? super X, Y> attribute);

    @Override
    <Y> XJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType jt);

    <Y> XJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinMode jm);

    <Y> XJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType jt, JoinMode jm);

    @Override
    <Y> XCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection);

    @Override
    <Y> XCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType jt);

    <Y> XCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinMode jm);

    <Y> XCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType jt, JoinMode jm);

    @Override
    <Y> XSetJoin<X, Y> join(SetAttribute<? super X, Y> set);

    @Override
    <Y> XSetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt);

    <Y> XSetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinMode jm);

    <Y> XSetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt, JoinMode jm);

    @Override
    <Y> XListJoin<X, Y> join(ListAttribute<? super X, Y> list);

    @Override
    <Y> XListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType jt);

    <Y> XListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinMode jm);

    <Y> XListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType jt, JoinMode jm);

    @Override
    <K, V> XMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map);

    @Override
    <K, V> XMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType jt);

    <K, V> XMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinMode jm);

    <K, V> XMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType jt, JoinMode jm);

    @Override
    <X, Y> XJoin<X, Y> join(String attributeName);

    @Override
    <X, Y> XJoin<X, Y> join(String attributeName, JoinType jt);

    <X, Y> XJoin<X, Y> join(String attributeName, JoinMode jm);   

    <X, Y> XJoin<X, Y> join(String attributeName, JoinType jt, JoinMode jm);

    @Override
    <X, Y> XCollectionJoin<X, Y> joinCollection(String attributeName);

    @Override
    <X, Y> XCollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt);

    <X, Y> XCollectionJoin<X, Y> joinCollection(String attributeName, JoinMode jm);   

    <X, Y> XCollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt, JoinMode jm);

    @Override
    <X, Y> XSetJoin<X, Y> joinSet(String attributeName);

    @Override
    <X, Y> XSetJoin<X, Y> joinSet(String attributeName, JoinType jt);

    <X, Y> XSetJoin<X, Y> joinSet(String attributeName, JoinMode jm);

    <X, Y> XSetJoin<X, Y> joinSet(String attributeName, JoinType jt, JoinMode jm);

    @Override
    <X, Y> XListJoin<X, Y> joinList(String attributeName);

    @Override
    <X, Y> XListJoin<X, Y> joinList(String attributeName, JoinType jt);

    <X, Y> XListJoin<X, Y> joinList(String attributeName, JoinMode jm);

    <X, Y> XListJoin<X, Y> joinList(String attributeName, JoinType jt, JoinMode jm);

    @Override
    <X, K, V> XMapJoin<X, K, V> joinMap(String attributeName);

    @Override
    <X, K, V> XMapJoin<X, K, V> joinMap(String attributeName, JoinType jt);

    <X, K, V> XMapJoin<X, K, V> joinMap(String attributeName, JoinMode jm);   

    <X, K, V> XMapJoin<X, K, V> joinMap(String attributeName, JoinType jt, JoinMode jm);
    
}
