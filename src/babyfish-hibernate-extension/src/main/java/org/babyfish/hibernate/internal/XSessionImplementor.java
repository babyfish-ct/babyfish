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
package org.babyfish.hibernate.internal;

import java.util.List;

import org.babyfish.hibernate.XQuery;
import org.babyfish.model.jpa.path.spi.PathPlanKey;
import org.babyfish.persistence.QueryType;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.SessionImpl;

/**
 * @author Tao Chen
 */
public interface XSessionImplementor extends SessionImplementor {

    @Override
    XSessionFactoryImplementor getFactory();
    
    SessionImpl getRawSessionImpl();
    
    XQuery createQuery(NamedQueryDefinition namedQueryDefinition);
    
    @SuppressWarnings("rawtypes")
    List list(
            String query, 
            QueryParameters queryParameters,
            QueryType queryType,
            PathPlanKey pathPlanKey) throws HibernateException;
    
    long unlimitedCount(
            String query, 
            QueryParameters queryParameters,
            QueryType queryType,
            PathPlanKey pathPlanKey) throws HibernateException;
}
