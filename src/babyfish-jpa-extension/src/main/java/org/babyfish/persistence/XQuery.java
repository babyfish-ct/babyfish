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
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.babyfish.model.jpa.path.QueryPath;

/**
 * @author Tao Chen
 */
public interface XQuery extends Query {
    
    long getUnlimitedCount();

    Object getSingleResult(boolean returnNullWhenNothingFound);
    
    XQuery setQueryType(QueryType queryType);
    
    XQuery setQueryPaths(QueryPath ... queryPaths);

    XQuery setQueryPaths(String alias, QueryPath ... queryPaths);
    
    XQuery setQueryPaths(Collection<? extends QueryPath> queryPaths);

    XQuery setQueryPaths(String alias, Collection<? extends QueryPath> queryPaths);
    
    @Override
    XQuery setFirstResult(int startPosition);
    
    @Override
    XQuery setMaxResults(int maxResult);

    @Override
    XQuery setHint(String hintName, Object value);
    
    @Override
    XQuery setFlushMode(FlushModeType flushMode);
    
    @Override
    XQuery setLockMode(LockModeType lockMode);
    
    @Override
    XQuery setParameter(int position, Object value);

    @Override
    XQuery setParameter(String name, Object value);

    @Override
    XQuery setParameter(int position, Date value, TemporalType temporalType);
    
    @Override
    XQuery setParameter(String name, Date value, TemporalType temporalType);

    @Override
    XQuery setParameter(int position, Calendar value, TemporalType temporalType);

    @Override
    XQuery setParameter(String name, Calendar value, TemporalType temporalType);
    
    @Override
    XQuery setParameter(Parameter<Date> param, Date value, TemporalType temporalType);
    
    @Override
    XQuery setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType);
    
    @Override
    <T> XQuery setParameter(Parameter<T> param, T value);
}
