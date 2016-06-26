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
package org.babyfish.persistence;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.babyfish.model.jpa.path.QueryPath;

/**
 * @author Tao Chen
 */
public interface XTypedQuery<X> extends TypedQuery<X>, XQuery {
    
    @Override
    X getSingleResult(boolean returnNullWhenNothingFound);
    
    @Override
    XTypedQuery<X> setQueryType(QueryType queryType);
    
    @Override
    XTypedQuery<X> setQueryPaths(QueryPath ... queryPaths);

    @Override
    XTypedQuery<X> setQueryPaths(String alias, QueryPath ... queryPaths);
    
    @Override
    XTypedQuery<X> setQueryPaths(Collection<? extends QueryPath> queryPaths);

    @Override
    XTypedQuery<X> setQueryPaths(String alias, Collection<? extends QueryPath> queryPaths);
    
    @Override
    XTypedQuery<X> setFirstResult(int startPosition);
    
    @Override
    XTypedQuery<X> setMaxResults(int maxResult);

    @Override
    XTypedQuery<X> setHint(String hintName, Object value);
    
    @Override
    XTypedQuery<X> setFlushMode(FlushModeType flushMode);
    
    @Override
    XTypedQuery<X> setLockMode(LockModeType lockMode);
    
    @Override
    XTypedQuery<X> setParameter(int position, Object value);

    @Override
    XTypedQuery<X> setParameter(String name, Object value);

    @Override
    XTypedQuery<X> setParameter(int position, Date value, TemporalType temporalType);
    
    @Override
    XTypedQuery<X> setParameter(String name, Date value, TemporalType temporalType);

    @Override
    XTypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType);

    @Override
    XTypedQuery<X> setParameter(String name, Calendar value, TemporalType temporalType);
    
    @Override
    XTypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType);
    
    @Override
    XTypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType);
    
    @Override
    <T> XTypedQuery<X> setParameter(Parameter<T> param, T value);
}
