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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaQuery;

import org.babyfish.persistence.criteria.QueryTemplate;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaDelete;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XCriteriaUpdate;
import org.babyfish.model.jpa.path.TypedQueryPath;

/**
 * @author Tao Chen
 */
public interface XEntityManager extends EntityManager, AutoCloseable {
    
    @Override
    XEntityManagerFactory getEntityManagerFactory();
    
    <E> E find(Class<E> entityClass, Object primaryKey, String ... queryPaths);
    
    <E> E find(Class<E> entityClass, Object primaryKey, LockModeType lockModeType, String ... queryPaths);
    
    @SuppressWarnings("unchecked")
    <E> E find(Class<E> entityClass, Object primaryKey, TypedQueryPath<E> ... queryPaths);
    
    @SuppressWarnings("unchecked")
    <E> E find(Class<E> entityClass, Object primaryKey, LockModeType lockModeType, TypedQueryPath<E> ... queryPaths);
    
    <E> List<E> find(Class<E> entityClass, Iterable<?> primaryKeys, String ... queryPaths);
    
    <E> List<E> find(Class<E> entityClass, Iterable<?> primaryKeys, LockModeType lockModeType, String ... queryPaths);
    
    @SuppressWarnings("unchecked")
    <E> List<E> find(Class<E> entityClass, Iterable<?> primaryKeys, TypedQueryPath<E> ... queryPaths);
    
    @SuppressWarnings("unchecked")
    <E> List<E> find(Class<E> entityClass, Iterable<?> primaryKeys, LockModeType lockModeType, TypedQueryPath<E> ... queryPaths);
    
    @Override
    XCriteriaBuilder getCriteriaBuilder();
    
    @Override
    XQuery createQuery(String qlString);
    
    @Override
    XQuery createNamedQuery(String name);

    @Override
    <T> XTypedQuery<T> createQuery(String qlString, Class<T> resultClass);
    
    @Override
    <T> XTypedQuery<T> createNamedQuery(String name, Class<T> resultClass);
    
    @Override
    <T> XTypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery);
    
    <T> QueryTemplate<T> createQueryTemplate(XCriteriaQuery<T> criteriaQuery);
    
    <T> QueryTemplate<T> createQueryTemplate(XCriteriaUpdate<T> criteriaUpdate);
    
    <T> QueryTemplate<T> createQueryTemplate(XCriteriaDelete<T> criteriaDelete);
}
