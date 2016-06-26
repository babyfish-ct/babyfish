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

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.babyfish.collection.XOrderedSet;
import org.babyfish.model.jpa.path.CollectionFetchType;

/**
 * @author Tao Chen
 */
public interface XFetchParent<Z, X> extends FetchParent<Z, X> {
    
    @Override
    XOrderedSet<Fetch<X, ?>> getFetches();
    
    XOrderedSet<XFetch<X, ?>> getXFetches();

    @Override
    <Y> XFetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute);

    @Override
    <Y> XFetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute, JoinType jt);

    @Override
    <Y> XFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute);

    @Override
    <Y> XFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt);

    <Y> XFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, CollectionFetchType cft);

    <Y> XFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt, CollectionFetchType cft);

    @SuppressWarnings("hiding")
    @Override
    <X, Y> XFetch<X, Y> fetch(String attributeName);

    @SuppressWarnings("hiding")
    @Override
    <X, Y> XFetch<X, Y> fetch(String attributeName, JoinType jt);

    @SuppressWarnings("hiding")
    <X, Y> XFetch<X, Y> fetch(String attributeName, CollectionFetchType cft);

    @SuppressWarnings("hiding")
    <X, Y> XFetch<X, Y> fetch(String attributeName, JoinType jt, CollectionFetchType cft);
}
