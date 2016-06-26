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
package org.babyfish.hibernate.hql;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.babyfish.hibernate.XQuery;
import org.babyfish.hibernate.internal.XSessionImplementor;
import org.babyfish.lang.Arguments;
import org.babyfish.model.jpa.path.QueryPath;
import org.babyfish.model.jpa.path.spi.PathPlanKeyBuilder;
import org.babyfish.persistence.QueryType;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.engine.query.spi.ParameterMetadata;
import org.hibernate.internal.QueryImpl;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;

/**
 * @author Tao Chen
 */
public class XQueryImpl extends QueryImpl implements XQuery {
    
    private QueryType queryType = QueryType.DISTINCT;
    
    private PathPlanKeyBuilder pathPlanKeyBuilder = new PathPlanKeyBuilder();

    public XQueryImpl(
            String queryString, 
            FlushMode flushMode,
            XSessionImplementor session, 
            ParameterMetadata parameterMetadata) {
        super(queryString, flushMode, session, parameterMetadata);
    }

    public XQueryImpl(
            String queryString, 
            XSessionImplementor session,
            ParameterMetadata parameterMetadata) {
        super(queryString, session, parameterMetadata);
    }

    @Override
    public XQuery setQueryType(QueryType queryType) {
        Arguments.mustNotBeNull("queryType", queryType);
        this.queryType = queryType;
        return this;
    }
    
    @Override
    public XQuery setQueryPaths(QueryPath ... queryPaths) {
        this.pathPlanKeyBuilder.setQueryPaths(queryPaths);
        return this;
    }

    @Override
    public XQuery setQueryPaths(String alias, QueryPath ... queryPaths) {
        this.pathPlanKeyBuilder.setQueryPaths(alias, queryPaths);
        return this;
    }
    
    @Override
    public XQuery setQueryPaths(Collection<? extends QueryPath> queryPaths) {
        this.pathPlanKeyBuilder.setQueryPaths(queryPaths);
        return this;
    }

    @Override
    public XQuery setQueryPaths(String alias, Collection<? extends QueryPath> queryPaths) {
        this.pathPlanKeyBuilder.setQueryPaths(alias, queryPaths);
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List list() throws HibernateException {
        this.verifyParameters();
        Map<?, ?> namedParams = getNamedParams();
        before();
        try {
            return ((XSessionImplementor)this.session).list(
                    expandParameterLists(namedParams),
                    getQueryParameters(namedParams),
                    this.queryType,
                    this.pathPlanKeyBuilder.build());
        }
        finally {
            after();
        }
    }
    
    @Override
    public long unlimitedCount() {
        this.verifyParameters();
        Map<?, ?> namedParams = getNamedParams();
        before();
        try {
            return ((XSessionImplementor)this.session).unlimitedCount(
                    expandParameterLists(namedParams),
                    getQueryParameters(namedParams),
                    this.queryType,
                    this.pathPlanKeyBuilder.build());
        }
        finally {
            after();
        }
    }
    
    @Override
    public final XQuery setResultTransformer(ResultTransformer resultTransformer) {
        super.setResultTransformer(resultTransformer);
        return this;
    } 
    
    @Override
    public XQuery setFirstResult(int firstResult) {
        super.setFirstResult(firstResult);
        return this;
    }
    
    @Override
    public XQuery setMaxResults(int maxResults) {
        super.setMaxResults(maxResults);
        return this;
    }
    
    //------------------------------
    @Override
    public XQuery setComment(String comment) {
        super.setComment(comment);
        return this;
    }

    @Override
    public XQuery setCacheable(boolean cacheable) {
        super.setCacheable(cacheable);
        return this;
    }

    @Override
    public XQuery setCacheRegion(String cacheRegion) {
        super.setCacheRegion(cacheRegion);
        return this;
    }

    @Override
    public XQuery setTimeout(int timeout) {
        super.setTimeout(timeout);
        return this;
    }

    @Override
    public XQuery setFetchSize(int fetchSize) {
        super.setFetchSize(fetchSize);
        return this;
    }

    @Override
    public XQuery setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        return this;
    }

    @Override
    public XQuery setLockOptions(LockOptions lockOption) {
        super.setLockOptions(lockOption);
        return this;
    }

    @Override
    public XQuery setLockMode(String alias, LockMode lockMode) {
        super.setLockMode(alias, lockMode);
        return this;
    }

    @Override
    public XQuery setFlushMode(FlushMode flushMode) {
        super.setFlushMode(flushMode);
        return this;
    }

    @Override
    public XQuery setCacheMode(CacheMode cacheMode) {
        super.setCacheMode(cacheMode);
        return this;
    }

    @Override
    public XQuery setBoolean(int position, boolean val) {
        super.setBoolean(position, val);
        return this;
    }

    @Override
    public XQuery setBoolean(String name, boolean val) {
        super.setBoolean(name, val);
        return this;
    }

    @Override
    public XQuery setCharacter(int position, char val) {
        super.setCharacter(position, val);
        return this;
    }

    @Override
    public XQuery setCharacter(String name, char val) {
        super.setCharacter(name, val);
        return this;
    }

    @Override
    public XQuery setByte(int position, byte val) {
        super.setByte(position, val);
        return this;
    }

    @Override
    public XQuery setByte(String name, byte val) {
        super.setByte(name, val);
        return this;
    }

    @Override
    public XQuery setShort(int position, short val) {
        super.setShort(position, val);
        return this;
    }

    @Override
    public XQuery setShort(String name, short val) {
        super.setShort(name, val);
        return this;
    }

    @Override
    public XQuery setInteger(int position, int val) {
        super.setInteger(position, val);
        return this;
    }

    @Override
    public XQuery setInteger(String name, int val) {
        super.setInteger(name, val);
        return this;
    }

    @Override
    public XQuery setLong(int position, long val) {
        super.setLong(position, val);
        return this;
    }

    @Override
    public XQuery setLong(String name, long val) {
        super.setLong(name, val);
        return this;
    }

    @Override
    public XQuery setFloat(int position, float val) {
        super.setFloat(position, val);
        return this;
    }

    @Override
    public XQuery setFloat(String name, float val) {
        super.setFloat(name, val);
        return this;
    }

    @Override
    public XQuery setDouble(int position, double val) {
        super.setDouble(position, val);
        return this;
    }

    @Override
    public XQuery setDouble(String name, double val) {
        super.setDouble(name, val);
        return this;
    }

    @Override
    public XQuery setBigInteger(int position, BigInteger number) {
        super.setBigInteger(position, number);
        return this;
    }

    @Override
    public XQuery setBigInteger(String name, BigInteger number) {
        super.setBigInteger(name, number);
        return this;
    }

    @Override
    public XQuery setBigDecimal(int position, BigDecimal number) {
        super.setBigDecimal(position, number);
        return this;
    }

    @Override
    public XQuery setBigDecimal(String name, BigDecimal number) {
        super.setBigDecimal(name, number);
        return this;
    }

    @Override
    public XQuery setDate(int position, Date date) {
        super.setDate(position, date);
        return this;
    }

    @Override
    public XQuery setDate(String name, Date date) {
        super.setDate(name, date);
        return this;
    }

    @Override
    public XQuery setTime(int position, Date date) {
        super.setTime(position, date);
        return this;
    }

    @Override
    public XQuery setTime(String name, Date date) {
        super.setTime(name, date);
        return this;
    }

    @Override
    public XQuery setTimestamp(int position, Date date) {
        super.setTimestamp(position, date);
        return this;
    }

    @Override
    public XQuery setTimestamp(String name, Date date) {
        super.setTimestamp(name, date);
        return this;
    }

    @Override
    public XQuery setCalendar(int position, Calendar calendar) {
        super.setCalendar(position, calendar);
        return this;
    }

    @Override
    public XQuery setCalendar(String name, Calendar calendar) {
        super.setCalendar(name, calendar);
        return this;
    }

    @Override
    public XQuery setCalendarDate(int position, Calendar calendar) {
        super.setCalendarDate(position, calendar);
        return this;
    }

    @Override
    public XQuery setCalendarDate(String name, Calendar calendar) {
        super.setCalendarDate(name, calendar);
        return this;
    }

    @Override
    public XQuery setString(int position, String val) {
        super.setString(position, val);
        return this;
    }

    @Override
    public XQuery setString(String name, String val) {
        super.setString(name, val);
        return this;
    }

    @Override
    public XQuery setText(int position, String val) {
        super.setText(position, val);
        return this;
    }

    @Override
    public XQuery setText(String name, String val) {
        super.setText(name, val);
        return this;
    }

    @Override
    public XQuery setBinary(int position, byte[] val) {
        super.setBinary(position, val);
        return this;
    }

    @Override
    public XQuery setBinary(String name, byte[] val) {
        super.setBinary(name, val);
        return this;
    }

    @Override
    public XQuery setSerializable(int position, Serializable val) {
        super.setSerializable(position, val);
        return this;
    }

    @Override
    public XQuery setSerializable(String name, Serializable val) {
        super.setSerializable(name, val);
        return this;
    }

    @Override
    public XQuery setLocale(int position, Locale locale) {
        super.setLocale(position, locale);
        return this;
    }

    @Override
    public XQuery setLocale(String name, Locale locale) {
        super.setLocale(name, locale);
        return this;
    }

    @Override
    public XQuery setEntity(int position, Object val) {
        super.setEntity(position, val);
        return this;
    }

    @Override
    public XQuery setEntity(String name, Object val) {
        super.setEntity(name, val);
        return this;
    }

    @Override
    public XQuery setProperties(Object bean) throws HibernateException {
        super.setProperties(bean);
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public XQuery setProperties(Map map) throws HibernateException {
        super.setProperties(map);
        return this;
    }

    @Override
    public XQuery setParameter(int position, Object val) throws HibernateException {
        super.setParameter(position, val);
        return this;
    }

    @Override
    public XQuery setParameter(String name, Object val) throws HibernateException {
        super.setParameter(name, val);
        return this;
    }

    @Override
    public XQuery setParameter(int position, Object val, Type type) {
        super.setParameter(position, val, type);
        return this;
    }

    @Override
    public XQuery setParameter(String name, Object val, Type type) {
        super.setParameter(name, val, type);
        return this;
    }

    @Override
    public XQuery setParameterList(String name, Object[] vals) throws HibernateException {
        super.setParameterList(name, vals);
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public XQuery setParameterList(String name, Collection vals) throws HibernateException {
        super.setParameterList(name, vals);
        return this;
    }

    @Override
    public XQuery setParameterList(String name, Object[] vals, Type type) throws HibernateException {
        super.setParameterList(name, vals, type);
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public XQuery setParameterList(String name, Collection vals, Type type) throws HibernateException {
        super.setParameterList(name, vals, type);
        return this;
    }

    @Override
    public XQuery setParameters(Object[] values, Type[] types) throws HibernateException {
        super.setParameters(values, types);
        return this;
    }
}
