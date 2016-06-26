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

import org.babyfish.persistence.XEntityManager;
import org.babyfishdemo.querypath.entities.Company;
import org.babyfishdemo.querypath.entities.Company__;

/**
 * @author Tao Chen
 */
public class CompanyRepository {

    public Company getCompanyByName(String name, Company__ ... queryPaths) {
        
        // Close the entity manager soon, say goodbye for spring-open-session-in-view
        // (In real project, please close the entity manager in services, not repositories)
        try (XEntityManager em = JPAContext.createEntityManager()) {
            return 
                    em
                    .createQuery("select c from Company c where c.name = :name", Company.class)
                    .setParameter("name", name)
                    .setQueryPaths(queryPaths)
                    .getSingleResult(true);
        }
    }
}
