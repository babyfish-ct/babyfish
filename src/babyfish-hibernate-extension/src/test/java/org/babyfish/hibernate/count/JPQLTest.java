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
package org.babyfish.hibernate.count;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Tuple;

import junit.framework.Assert;

import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.hibernate.QueryException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class JPQLTest {
    
    private static XEntityManagerFactory entityManagerFactory;
    
    private XEntityManager entityManager;
    
    @BeforeClass
    public static void initEntityManagerFactory() {
        entityManagerFactory =
            new HibernatePersistenceProvider(
                    JPQLTest.class.getPackage().getName().replace('.', '/') + 
                    "/persistence.xml")
            .createEntityManagerFactory(null, null);
    }
    
    @AfterClass
    public static void diposeEntityManagerFactory() {
        EntityManagerFactory emf = entityManagerFactory;
        if (emf != null) {
            entityManagerFactory = null;
            emf.close();
        }
    }
    
    @Before
    public void initEntityManager() {
        this.entityManager = entityManagerFactory.createEntityManager();
        SqlAwareDriver.clearSqlList();
    }
    
    @After
    public void disposeEntityManager() {
        EntityManager em = this.entityManager;
        if (em != null) {
            this.entityManager = null;
            em.close();
        }
    }
    
    @Test
    public void testSimpleUnlimitedCount() {
        this.entityManager.createQuery("from Department d", Department.class).getUnlimitedCount();
        
        List<String> sqlList = SqlAwareDriver.getSqlList();
        Assert.assertEquals(1, sqlList.size());
        Assert.assertEquals(
                "select count(department0_.DEPARTMENT_ID) as col_0_0_ from DEPARTMENT department0_", 
                sqlList.get(0)
        );
    }
    
    @Test
    public void testUnlimitedCountWithUnusedNonNullReferenceInnerJoin() {
        this
        .entityManager
        .createQuery(
                "select e "
                + "from Employee e "
                + "inner join e.department d "
                + "order by e.name desc", 
                Employee.class
        )
        .getUnlimitedCount();
        
        List<String> sqlList = SqlAwareDriver.getSqlList();
        Assert.assertEquals(1, sqlList.size());
        Assert.assertEquals(
                "select count(employee0_.EMPLOYEE_ID) as col_0_0_ "
                + "from EMPLOYEE employee0_",
                sqlList.get(0)
        );
    }
    
    @Test
    public void testUnlimitedCountWithUnusedNulluableReferenceInnerJoin() {
        this
        .entityManager
        .createQuery(
                "select e "
                + "from Employee e "
                + "inner join e.supervisor s "
                + "order by e.name desc", 
                Employee.class
        )
        .getUnlimitedCount();
        
        List<String> sqlList = SqlAwareDriver.getSqlList();
        Assert.assertEquals(1, sqlList.size());
        Assert.assertEquals(
                "select count(employee0_.EMPLOYEE_ID) as col_0_0_ "
                + "from EMPLOYEE employee0_ "
                + "inner join EMPLOYEE employee1_ "
                +   "on employee0_.SUPERVSIOR_ID=employee1_.EMPLOYEE_ID",
                sqlList.get(0)
        );
    }
    
    @Test
    public void testUnlimitedCountWithUnusedReferenceLeftJoin() {
        this
        .entityManager
        .createQuery(
                "select e "
                + "from Employee e "
                + "left join e.department d "
                + "order by e.name desc", 
                Employee.class
        )
        .getUnlimitedCount();
        
        List<String> sqlList = SqlAwareDriver.getSqlList();
        Assert.assertEquals(1, sqlList.size());
        Assert.assertEquals(
                "select count(employee0_.EMPLOYEE_ID) as col_0_0_ from EMPLOYEE employee0_",
                sqlList.get(0)
        );
    }
    
    @Test
    public void testUnlimitedCountWithUnusedCollectionLeftJoin() {
        this
        .entityManager
        .createQuery(
                "select d from Department d left join d.employees e order by e.name asc", 
                Department.class
        )
        .getUnlimitedCount();
        
        List<String> sqlList = SqlAwareDriver.getSqlList();
        Assert.assertEquals(1, sqlList.size());
        Assert.assertEquals(
                "select count(department0_.DEPARTMENT_ID) as col_0_0_ from DEPARTMENT department0_", 
                sqlList.get(0)
        );
    }
    
    @Test
    public void testUnlimitedCountWithUsedCollectionLeftJoin() {
        this
        .entityManager
        .createQuery(
                "select d from Department d "
                + "left join d.employees e "
                + "where e.name like :name "
                + "order by e.name asc", 
                Department.class
        )
        .setParameter("name", "%i%")
        .getUnlimitedCount();
        
        List<String> sqlList = SqlAwareDriver.getSqlList();
        Assert.assertEquals(1, sqlList.size());
        Assert.assertEquals(
                "select count(distinct department0_.DEPARTMENT_ID) as col_0_0_ "
                + "from DEPARTMENT department0_ "
                + "left outer join EMPLOYEE employees1_ "
                +   "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + "where employees1_.NAME like ?", 
                sqlList.get(0)
        );
    }
    
    @Test
    public void testUnlimitedCountWithCollectionInnerJoin() {
        this
        .entityManager
        .createQuery("select d from Department d inner join d.employees", Department.class)
        .getUnlimitedCount();
        
        List<String> sqlList = SqlAwareDriver.getSqlList();
        Assert.assertEquals(1, sqlList.size());
        Assert.assertEquals(
                "select count(distinct department0_.DEPARTMENT_ID) as col_0_0_ "
                + "from DEPARTMENT department0_ "
                + "inner join EMPLOYEE employees1_ on "
                +   "department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID", 
                sqlList.get(0)
        );
    }
    
    @Test(expected = QueryException.class) 
    public void testUnlimitedCountWithGroupBy() {
        this
        .entityManager
        .createQuery("select d from Department d group by d", Department.class)
        .getUnlimitedCount();
    }
    
    @Test(expected = QueryException.class) 
    public void testUnlimitedCountWithTooManySelections() {
        this
        .entityManager
        .createQuery("select d, e from Department d, Employee e", Tuple.class)
        .getUnlimitedCount();
    }
    
    @Test(expected = QueryException.class) 
    public void testUnlimitedCountWithBadSelection() {
        this
        .entityManager
        .createQuery("select sum(d.id) from Department d", Long.class)
        .getUnlimitedCount();
    }
    
    @Test(expected = QueryException.class) 
    public void testUnlimitedCountWithImplictSelectionAndSeveralForms() {
        this
        .entityManager
        .createQuery("from Department, Employee", Tuple.class)
        .getUnlimitedCount();
    }
    
    @Test(expected = QueryException.class) 
    public void testUnlimitedCountWithImplicitSelectionAndNonFetchJoin() {
        this
        .entityManager
        .createQuery("from Department d inner join d.employees", Tuple.class)
        .getUnlimitedCount();
    }
}
