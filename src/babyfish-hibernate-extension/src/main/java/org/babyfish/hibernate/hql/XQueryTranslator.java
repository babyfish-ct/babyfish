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

import java.util.List;

import org.babyfish.model.jpa.path.spi.PathPlan;
import org.babyfish.persistence.QueryType;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.spi.QueryTranslator;

/**
 * @author Tao Chen
 */
public interface XQueryTranslator extends QueryTranslator {
    
    PathPlan getPathPlan();
    
    String getCountSQLString();
    
    String getDistinctCountSQLString();
    
    <T> List<T> list(
            SessionImplementor session,
            QueryParameters queryParameters, 
            QueryType queryType);
    
    long unlimitedCount(
            SessionImplementor session,
            QueryParameters queryParameters,
            QueryType queryType);
}
