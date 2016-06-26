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
package org.babyfishdemo.pagingquery.base;

import java.util.List;

import org.babyfish.persistence.QueryType;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XTypedQuery;
import org.babyfishdemo.pagingquery.entities.Department__;
import org.babyfishdemo.pagingquery.entities.Department;

/**
 * @author Tao Chen
 */
public class AbstractDAOTest extends AbstractTest {

    protected static List<Department> getAllDepartments(QueryType queryType, Department__ ... queryPaths) {
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            return 
                    em
                    .createQuery("select d from Department d", Department.class)
                    .setQueryType(queryType) //If delete this invocation, default mode is QueryType.DISTINCT
                    .setQueryPaths(queryPaths)
                    .getResultList();
        }
    }
    
    protected static LimitedResult<Department> getLimitedDepartments(
            QueryType queryType,
            int firstResult, 
            int maxResults, 
            Department__ ... queryPaths) {
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            XTypedQuery<Department> query =
                    em
                    .createQuery("select d from Department d", Department.class)
                    .setQueryType(queryType) //If delete this invocation, default mode is QueryType.DISTINCT
                    .setQueryPaths(queryPaths)
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResults);
            return new LimitedResult<>(query.getUnlimitedCount(), query.getResultList());
        }
    }
}
