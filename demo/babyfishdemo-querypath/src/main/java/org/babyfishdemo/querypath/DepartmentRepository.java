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
package org.babyfishdemo.querypath;

import java.util.Collection;
import java.util.List;

import org.babyfish.persistence.XEntityManager;
import org.babyfishdemo.querypath.entities.Department;
import org.babyfishdemo.querypath.entities.Department__;

/**
 * @author Tao Chen
 */
public class DepartmentRepository {
    
    public List<Department> getDepartments(Department__ ... queryPaths) {
        
        // Close the entity manager soon, say goodbye for spring-open-session-in-view
        // (In real project, please close the entity manager in services, not repositories)
        try (XEntityManager em = JPAContext.createEntityManager()) {
            return 
                    em
                    .createQuery("select d from Department d", Department.class)
                    .setQueryPaths(queryPaths)
                    .getResultList();
        }
    }

    public Department getDepartmentByName(String name, Department__ ... queryPaths) {
        
        // Close the entity manager soon, say goodbye for spring-open-session-in-view
        // (In real project, please close the entity manager in services, not repositories)
        try (XEntityManager em = JPAContext.createEntityManager()) {
            return 
                    em
                    .createQuery("select d from Department d where d.name = :name", Department.class)
                    .setQueryPaths(queryPaths)
                    .setParameter("name", name)
                    .getSingleResult(true);
        }
    }
    
    public List<Department> getDepartmentsByEmployeeName(String employeeName, Department__ ... queryPaths) {
        
        // Close the entity manager soon, say goodbye for spring-open-session-in-view
        // (In real project, please close the entity manager in services, not repositories)
        try (XEntityManager em = JPAContext.createEntityManager()) {
            /*
             * When query parameter is applied on elements of a collection,
             * sub query is good choice,
             * but, in order to show our demo, I choice to use the bad choice: join.
             */
            return 
                    em
                    .createQuery(
                            "select d "
                            + "from Department d "
                            + "inner join d.employees e "
                            + "where e.name = :employeeName", 
                            Department.class
                    )
                    .setQueryPaths(queryPaths)
                    .setParameter("employeeName", employeeName)
                    .getResultList();
        }
    }
    
    public List<Department> getDepartmentsByEmployeeNames(Collection<String> employeeNames, Department__ ... queryPaths) {
        
        // Close the entity manager soon, say goodbye for spring-open-session-in-view
        // (In real project, please close the entity manager in services, not repositories)
        try (XEntityManager em = JPAContext.createEntityManager()) {
            /*
             * When query parameter is applied on elements of a collection,
             * sub query is good choice,
             * but, in order to show our demo, I choice to use the bad choice: join.
             */
            return 
                    em
                    .createQuery(
                            "select d "
                            + "from Department d "
                            + "inner join d.employees e "
                            + "where e.name in (:employeeNames)", 
                            Department.class
                    )
                    .setQueryPaths(queryPaths)
                    .setParameter("employeeNames", employeeNames)
                    .getResultList();
        }
    }
}
