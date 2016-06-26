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
import java.util.List;

import org.babyfish.model.jpa.path.QueryPath;
import org.babyfish.model.jpa.path.TypedQueryPath;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.Session;

/**
 * @author Tao Chen
 */
public interface XSession extends Session {
    
    @Override
    XSessionFactory getSessionFactory();
    
    Object get(String entityName, Serializable id, String ... queryPaths);
    
    Object get(String entityName, Serializable id, LockOptions lockOptions, String ... queryPaths);
    
    Object get(String entityName, Serializable id, QueryPath ... queryPaths);
    
    @SuppressWarnings("unchecked")
    <E> E get(Class<E> entityClass, Serializable id, TypedQueryPath<E> ... queryPaths);
    
    Object get(String entityName, Serializable id, LockOptions lockOptions, QueryPath ... queryPaths);
    
    @SuppressWarnings("unchecked")
    <E> E get(Class<E> entityClass, Serializable id, LockOptions lockOptions, TypedQueryPath<E> ... queryPaths);
    
    List<Object> get(String entityName, Iterable<? extends Serializable> ids, String ... queryPaths);
    
    List<Object> get(String entityName, Iterable<? extends Serializable> ids, LockOptions lockOptions, String ... queryPaths);
    
    List<Object> get(String entityName, Iterable<? extends Serializable> ids, QueryPath ... queryPaths);
    
    @SuppressWarnings("unchecked")
    <E> List<E> get(Class<E> entityClass, Iterable<? extends Serializable> ids, TypedQueryPath<E> ... queryPaths);
    
    List<Object> get(String entityName, Iterable<? extends Serializable> ids, LockOptions lockOptions, QueryPath ... queryPaths);
    
    @SuppressWarnings("unchecked")
    <E> List<E> get(Class<E> entityClass, Iterable<? extends Serializable> ids, LockOptions lockOptions, TypedQueryPath<E> ... queryPaths);

    @Override
    XSharedSessionBuilder sessionWithOptions();
    
    @Override
    XQuery createQuery(String queryString) throws HibernateException;
    
    @Override
    XQuery getNamedQuery(String queryName) throws HibernateException;
}
