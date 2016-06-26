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

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.babyfish.collection.XOrderedSet;

/**
 * @author Tao Chen
 */
public interface XAbstractQuery<T> extends XCommonAbstractCriteria, AbstractQuery<T> {

    @Override
    <X> XRoot<X> from(Class<X> entityClass);

    @Override
    <X> XRoot<X> from(EntityType<X> entity);
    
    @Override
    XAbstractQuery<T> where(Expression<Boolean> restriction);

    @Override
    XAbstractQuery<T> where(Predicate... restrictions);

    @Override
    XAbstractQuery<T> groupBy(Expression<?>... grouping);

    @Override
    XAbstractQuery<T> groupBy(List<Expression<?>> grouping);

    @Override
    XAbstractQuery<T> having(Expression<Boolean> restriction);

    @Override
    XAbstractQuery<T> having(Predicate... restrictions);

    @Override
    XAbstractQuery<T> distinct(boolean distinct);
    
    @Override
    XOrderedSet<Root<?>> getRoots();
    
    XOrderedSet<XRoot<?>> getXRoots();
    
}
