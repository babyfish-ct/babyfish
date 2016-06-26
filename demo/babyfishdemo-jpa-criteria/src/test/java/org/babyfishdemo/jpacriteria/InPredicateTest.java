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
package org.babyfishdemo.jpacriteria;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.Root;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfishdemo.jpacriteria.base.BadHSQLDialect;
import org.babyfishdemo.jpacriteria.entities.Employee;
import org.babyfishdemo.jpacriteria.entities.Employee_;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class InPredicateTest {
    
    private static XEntityManagerFactory entityManagerFactory;

    @BeforeClass
    public static void initEntityManagerFactory() {
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.dialect", BadHSQLDialect.class.getName());
        entityManagerFactory = 
                new HibernatePersistenceProvider()
                .createEntityManagerFactory(null, properties);
    }
    
    @AfterClass
    public static void closeEntityManagerFactory() {
        EntityManagerFactory emf = entityManagerFactory;
        if (emf != null) {
            entityManagerFactory = null;
            emf.close();
        }
    }
    
    @Test
    public void testLimitedDialect() {
        
        /*
         * Some database has limitation for the max length of in predicate list,
         * For example, in Oracle, the "in(...)" can only contain 1000 elements at most.
         * babyfish-jpa-criteria can resolve this problem, but you need to
         * 
         * (1) Use babyfish-jpa-criteria API, not JPQL
         * (2) Use the dialect that implements "org.babyfish.hibernate.dialect.LimitedListDialect",
         *      for example, org.babyfish.hibernate.dialect.Oracle10gDialect
         * 
         * In this demo, we created a new dialect "org.babyfishdemo.jpacriteria.BadHSQLDialect", 
         * it only allows "in(...)" to contain 10 elements at most.
         */
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        /*
         * (1) Specify the in(...) with 10 elements.
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq.where(
                    cb.in(
                            employee.get(Employee_.id), 
                            MACollections.wrapLong(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)
                    )
            );
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where babyfish_shared_alias_0.id in(:babyfish_literal_0)", 
                    entityManagerFactory.createQueryTemplate(cq).toString()
            );
        }
        
        /*
         * (1) Specify the in(...) with 25 elements.
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq.where(
                    cb.in(
                            employee.get(Employee_.id), 
                            MACollections.wrapLong(
                                    1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
                                    11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L,
                                    21L, 22L, 23L, 24L, 25L
                            )
                    )
            );
            // "org.babyfishdemo.jpacriteria.BadHSQLDialect" allows "in(...)" 
            // to contain 10 element at most,
            // so babyfish-jpa-criteria has to split the "in(...25 elements...)"
            // into 3 new "in(...)" predicates and join them by " or ".
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where ("
                    +     "babyfish_shared_alias_0.id in(:babyfish_literal_0) " // 1 ... 10
                    +   "or "
                    +     "babyfish_shared_alias_0.id in(:babyfish_literal_1) " // 11 ... 20
                    +   "or "
                    +     "babyfish_shared_alias_0.id in(:babyfish_literal_2)" // 21-25
                    + ")", 
                    entityManagerFactory.createQueryTemplate(cq).toString()
            );
        }
        
        /*
         * (1) Specify the not in(...) with 25 elements.
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq.where(
                    cb.not(
                            cb.in(
                                    employee.get(Employee_.id), 
                                    MACollections.wrapLong(
                                            1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
                                            11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L,
                                            21L, 22L, 23L, 24L, 25L
                                    )
                            )
                    )
            );
            // "org.babyfishdemo.jpacriteria.BadHSQLDialect" allows "in(...)" 
            // to contain 10 element at most,
            // so babyfish-jpa-criteria has to split the "in(...25 elements...)"
            // into 3 new "in(...)" predicates and join them by " or ".
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where ("
                    +     "babyfish_shared_alias_0.id not in(:babyfish_literal_0) " // 1 ... 10
                    +   "and "
                    +     "babyfish_shared_alias_0.id not in(:babyfish_literal_1) " // 11 ... 20
                    +   "and "
                    +     "babyfish_shared_alias_0.id not in(:babyfish_literal_2)" // 21-25
                    + ")", 
                    entityManagerFactory.createQueryTemplate(cq).toString()
            );
        }
        
        /*
         * Note:
         *    org.babyfish.hibernate.dialect.Oracle8iDialect,
         *  org.babyfish.hibernate.dialect.Oracle9iDialect and
         *  org.babyfish.hibernate.dialect.Oracle10gDialect allows 
         *  "in(...)" predicate to contain 1000 elements at most.
         */
    }
    
    @Test
    public void testOnlyOneElementThatIsNotSubquery() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        /*
         * suppose if "onlyOneElement" is an single value that is NOT sub query,
         * "expression in (onlyOneElement)" will be changed to "expression = onlyOneElement"
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq.where(
                    cb.in(employee.get(Employee_.id), MACollections.wrapLong(10L)) //Only one element
            );
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where babyfish_shared_alias_0.id = :babyfish_literal_0", // "in(onlyOne)" is changed to "="
                    entityManagerFactory.createQueryTemplate(cq).toString()
            );
        }
        
        /*
         * suppose if "onlyOneElement" is an single value that is NOT sub query,
         * "expression not in (onlyOneElement)" will be changed to "expression != onlyOneElement"
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq.where(
                    cb.not(
                            cb.in(employee.get(Employee_.id), MACollections.wrapLong(10L)) //Only one element
                    )
            );
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where babyfish_shared_alias_0.id != :babyfish_literal_0", // "in(onlyOne)" is changed to "="
                    entityManagerFactory.createQueryTemplate(cq).toString()
            );
        }
    }
    
    @Test
    public void testNoElements() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        /*
         * "expression in ()" will be changed to "1 = 0"
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq.where(
                    cb.in(employee.get(Employee_.id)) //Nothing
            );
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where 1 = 0", // "in()" is changed to "1 = 1"
                    entityManagerFactory.createQueryTemplate(cq).toString()
            );
        }
        
        /*
         * "expression not in ()" will be changed to "1 = 1"
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq.where(
                    cb.not(
                            cb.in(employee.get(Employee_.id)) //Nothing
                    )
            );
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where 1 = 1", // "in(onlyOne)" is changed to "="
                    entityManagerFactory.createQueryTemplate(cq).toString()
            );
        }
    }
}
