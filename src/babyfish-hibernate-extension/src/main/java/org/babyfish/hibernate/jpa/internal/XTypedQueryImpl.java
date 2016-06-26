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
package org.babyfish.hibernate.jpa.internal;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;

import org.babyfish.model.jpa.path.QueryPath;
import org.babyfish.persistence.QueryType;
import org.babyfish.persistence.XTypedQuery;
import org.hibernate.jpa.internal.QueryImpl;
import org.hibernate.jpa.spi.AbstractEntityManagerImpl;

/**
 * @author Tao Chen
 */
public class XTypedQueryImpl<X> extends QueryImpl<X> implements XTypedQuery<X> {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public XTypedQueryImpl(org.babyfish.hibernate.XQuery query, AbstractEntityManagerImpl em,
            Map<String, Class<?>> namedParameterTypeRedefinitions) {
        super(query, em, (Map)namedParameterTypeRedefinitions);
    }

    public XTypedQueryImpl(org.babyfish.hibernate.XQuery query, AbstractEntityManagerImpl em) {
        super(query, em);
    }

    @Override
    public long getUnlimitedCount() {
        return 
                this
                .unwrap(org.babyfish.hibernate.XQuery.class)
                .unlimitedCount();
    }

    @Override
    public X getSingleResult(boolean returnNullWhenNothingFound) {
        if (!returnNullWhenNothingFound) {
            return this.getSingleResult();
        }
        try {
            return this.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public XTypedQueryImpl<X> setQueryType(QueryType queryType) {
        this
        .unwrap(org.babyfish.hibernate.XQuery.class)
        .setQueryType(queryType);
        return this;
    }

    @Override
    public XTypedQueryImpl<X> setQueryPaths(QueryPath ... queryPaths) {
        this
        .unwrap(org.babyfish.hibernate.XQuery.class)
        .setQueryPaths(queryPaths);
        return this;
    }

    @Override
    public XTypedQueryImpl<X> setQueryPaths(String alias, QueryPath ... queryPaths) {
        this
        .unwrap(org.babyfish.hibernate.XQuery.class)
        .setQueryPaths(alias, queryPaths);
        return this;
    }
    
    @Override
    public XTypedQueryImpl<X> setQueryPaths(Collection<? extends QueryPath> queryPaths) {
        this
        .unwrap(org.babyfish.hibernate.XQuery.class)
        .setQueryPaths(queryPaths);
        return this;
    }

    @Override
    public XTypedQueryImpl<X> setQueryPaths(String alias, Collection<? extends QueryPath> queryPaths) {
        this
        .unwrap(org.babyfish.hibernate.XQuery.class)
        .setQueryPaths(alias, queryPaths);
        return this;
    }
    
    @Override
    public XTypedQueryImpl<X> setFirstResult(int startPosition) {
        super.setFirstResult(startPosition);
        return this;
    }
    
    @Override
    public XTypedQueryImpl<X> setMaxResults(int maxResult) {
        super.setMaxResults(maxResult);
        return this;
    }

    @Override
    public XTypedQueryImpl<X> setHint(String hintName, Object value) {
        super.setHint(hintName, value);
        return this;
    }
    
    @Override
    public XTypedQueryImpl<X> setFlushMode(FlushModeType flushMode) {
        super.setFlushMode(flushMode);
        return this;
    }

    @Override
    public XTypedQueryImpl<X> setLockMode(LockModeType lockModeType) {
        super.setLockMode(lockModeType);
        return this;
    }

    public <T> XTypedQueryImpl<X> setParameter(Parameter<T> param, T value) {
        super.setParameter(param, value);
        return this;
    }

    public XTypedQueryImpl<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }

    public XTypedQueryImpl<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }

    public XTypedQueryImpl<X> setParameter(String name, Object value) {
        super.setParameter(name, value);
        return this;
    }

    public XTypedQueryImpl<X> setParameter(String name, Date value, TemporalType temporalType) {
        super.setParameter(name, value, temporalType);
        return this;
    }

    public XTypedQueryImpl<X> setParameter(String name, Calendar value, TemporalType temporalType) {
        super.setParameter(name, value, temporalType);
        return this;
    }

    public XTypedQueryImpl<X> setParameter(int position, Object value) {
        super.setParameter(position, value);
        return this;
    }

    public XTypedQueryImpl<X> setParameter(int position, Date value, TemporalType temporalType) {
        this.setParameter(position, value, temporalType);
        return this;
    }

    public XTypedQueryImpl<X> setParameter(int position, Calendar value, TemporalType temporalType) {
        this.setParameter(position, value, temporalType);
        return this;
    }

    //Must override this method to avoid compile warnings
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Set getParameters() {
        return super.getParameters();
    }
}
