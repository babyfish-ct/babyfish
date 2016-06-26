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

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.SynchronizationType;

import org.babyfish.persistence.criteria.QueryTemplate;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaDelete;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XCriteriaUpdate;

/**
 * @author Tao Chen
 */
public interface XEntityManagerFactory extends EntityManagerFactory, AutoCloseable {

    @Override
    XCriteriaBuilder getCriteriaBuilder();

    @Override
    XEntityManager createEntityManager();
    
    @SuppressWarnings("rawtypes")
    @Override
    XEntityManager createEntityManager(Map map);
    
    @Override
    XEntityManager createEntityManager(SynchronizationType synchronizationType);
    
    @SuppressWarnings("rawtypes")
    @Override
    XEntityManager createEntityManager(SynchronizationType synchronizationType, Map map);
    
    <T> QueryTemplate<T> createQueryTemplate(XCriteriaQuery<T> criteriaQuery);
    
    <T> QueryTemplate<T> createQueryTemplate(XCriteriaUpdate<T> criteriaUpdate);
    
    <T> QueryTemplate<T> createQueryTemplate(XCriteriaDelete<T> criteriaDelete);
}
