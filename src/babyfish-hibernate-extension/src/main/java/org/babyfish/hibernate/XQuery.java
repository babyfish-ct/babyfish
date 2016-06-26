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
package org.babyfish.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.babyfish.model.jpa.path.QueryPath;
import org.babyfish.persistence.QueryType;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;

/**
 * @author Tao Chen
 */
public interface XQuery extends Query {
    
    long unlimitedCount();
    
    XQuery setQueryType(QueryType queryType);
    
    XQuery setQueryPaths(QueryPath ... queryPaths);

    XQuery setQueryPaths(String alias, QueryPath ... queryPaths);
    
    XQuery setQueryPaths(Collection<? extends QueryPath> queryPaths);

    XQuery setQueryPaths(String alias, Collection<? extends QueryPath> queryPaths);
    
    @Override
    XQuery setResultTransformer(ResultTransformer resultTransformer);
    
    @Override
    XQuery setFirstResult(int firstResult);
    
    @Override
    XQuery setMaxResults(int maxResults);
    
    @Override
    XQuery setComment(String comment);

    @Override
    XQuery setCacheable(boolean cacheable);

    @Override
    XQuery setCacheRegion(String cacheRegion);

    @Override
    XQuery setTimeout(int timeout);

    @Override
    XQuery setFetchSize(int fetchSize);

    @Override
    XQuery setReadOnly(boolean readOnly);

    @Override
    XQuery setLockOptions(LockOptions lockOption);

    @Override
    XQuery setLockMode(String alias, LockMode lockMode);

    @Override
    XQuery setFlushMode(FlushMode flushMode);

    @Override
    XQuery setCacheMode(CacheMode cacheMode);

    @Override
    XQuery setBoolean(int position, boolean val);

    @Override
    XQuery setBoolean(String name, boolean val);

    @Override
    XQuery setCharacter(int position, char val);

    @Override
    XQuery setCharacter(String name, char val);

    @Override
    XQuery setByte(int position, byte val);

    @Override
    XQuery setByte(String name, byte val);

    @Override
    XQuery setShort(int position, short val);

    @Override
    XQuery setShort(String name, short val);

    @Override
    XQuery setInteger(int position, int val);

    @Override
    XQuery setInteger(String name, int val);

    @Override
    XQuery setLong(int position, long val);

    @Override
    XQuery setLong(String name, long val);

    @Override
    XQuery setFloat(int position, float val);

    @Override
    XQuery setFloat(String name, float val);

    @Override
    XQuery setDouble(int position, double val);

    @Override
    XQuery setDouble(String name, double val);

    @Override
    XQuery setBigInteger(int position, BigInteger number);

    @Override
    XQuery setBigInteger(String name, BigInteger number);

    @Override
    XQuery setBigDecimal(int position, BigDecimal number);

    @Override
    XQuery setBigDecimal(String name, BigDecimal number);

    @Override
    XQuery setDate(int position, Date date);

    @Override
    XQuery setDate(String name, Date date);

    @Override
    XQuery setTime(String name, Date date);

    @Override
    XQuery setTime(int position, Date date);

    @Override
    XQuery setTimestamp(String name, Date date);

    @Override
    XQuery setTimestamp(int position, Date date);

    @Override
    XQuery setCalendar(int position, Calendar calendar);

    @Override
    XQuery setCalendar(String name, Calendar calendar);

    @Override
    XQuery setCalendarDate(int position, Calendar calendar);

    @Override
    XQuery setCalendarDate(String name, Calendar calendar);

    @Override
    XQuery setString(int position, String val);

    @Override
    XQuery setString(String name, String val);

    @Override
    XQuery setText(int position, String val);

    @Override
    XQuery setText(String name, String val);

    @Override
    XQuery setBinary(int position, byte[] val);

    @Override
    XQuery setBinary(String name, byte[] val);

    @Override
    XQuery setSerializable(String name, Serializable val);

    @Override
    XQuery setSerializable(int position, Serializable val);

    @Override
    XQuery setEntity(String name, Object val);

    @Override
    XQuery setEntity(int position, Object val);

    @Override
    XQuery setProperties(Object bean) throws HibernateException;

    @SuppressWarnings("rawtypes")
    @Override
    XQuery setProperties(Map map) throws HibernateException;

    @Override
    XQuery setLocale(String name, Locale locale);

    @Override
    XQuery setLocale(int position, Locale locale);

    @Override
    XQuery setParameter(int position, Object val) throws HibernateException;

    @Override
    XQuery setParameter(String name, Object val) throws HibernateException;

    @Override
    XQuery setParameter(int position, Object val, Type type);

    @Override
    XQuery setParameter(String name, Object val, Type type);

    @Override
    XQuery setParameterList(String name, Object[] vals) throws HibernateException;

    @SuppressWarnings("rawtypes")
    @Override
    XQuery setParameterList(String name, Collection vals) throws HibernateException;

    @Override
    XQuery setParameterList(String name, Object[] vals, Type type) throws HibernateException;

    @SuppressWarnings("rawtypes")
    @Override
    XQuery setParameterList(String name, Collection vals, Type type) throws HibernateException;

    @Override
    XQuery setParameters(Object[] values, Type[] types) throws HibernateException;
}
