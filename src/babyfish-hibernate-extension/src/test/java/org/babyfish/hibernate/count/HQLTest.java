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

import junit.framework.Assert;

import org.babyfish.hibernate.XSession;
import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.cfg.Configuration;
import org.hibernate.QueryException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class HQLTest {
    
    private static XSessionFactory sessionFactory;
    
    private XSession session;
    
    @SuppressWarnings("deprecation")
    @BeforeClass
    public static void initEntityManagerFactory() {
        sessionFactory =
                new Configuration()
                .configure(
                        HQLTest.class.getPackage().getName().replace('.', '/') + 
                        "/hibernate.cfg.xml"
                )
                .addAnnotatedClass(Department.class)
                .addAnnotatedClass(Employee.class)
                .buildSessionFactory();
    }
    
    @AfterClass
    public static void disposeSessionFactory() {
        XSessionFactory sf = sessionFactory;
        if (sf != null) {
            sessionFactory = null;
            sf.close();
        }
    } 
    
    @Before
    public void initSession() {
        this.session = sessionFactory.openSession();
        SqlAwareDriver.clearSqlList();
    }
    
    @After
    public void disposeSession() {
        XSession s = this.session;
        if (s != null) {
            this.session = null;
            s.close();
        }
    }
    
    @Test
    public void testSimpleUnlimitedCount() {
        this.session.createQuery("from Department d").unlimitedCount();
        
        List<String> sqlList = SqlAwareDriver.getSqlList();
        Assert.assertEquals(1, sqlList.size());
        Assert.assertEquals(
                "select count(department0_.DEPARTMENT_ID) as col_0_0_ from DEPARTMENT department0_", 
                sqlList.get(0)
        );
    }

    @Test
    public void testUnlimitedCountWithUnusedReferenceLeftJoin() {
        this
        .session
        .createQuery(
                "select e "
                + "from Employee e "
                + "left join e.department d "
                + "order by e.name desc"
        )
        .unlimitedCount();
        
        List<String> sqlList = SqlAwareDriver.getSqlList();
        Assert.assertEquals(1, sqlList.size());
        Assert.assertEquals(
                "select count(employee0_.EMPLOYEE_ID) as col_0_0_ from EMPLOYEE employee0_",
                sqlList.get(0)
        );
    }
    
    @Test
    public void testUnlimitedCountWithUnUsedNonNullReferenceInnerJoin() {
        this
        .session
        .createQuery(
                "select e "
                + "from Employee e "
                + "inner join e.department d "
                + "order by e.name desc"
        )
        .unlimitedCount();
        
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
        .session
        .createQuery(
                "select e "
                + "from Employee e "
                + "inner join e.supervisor s "
                + "order by e.name desc"
        )
        .unlimitedCount();
        
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
    public void testUnlimitedCountWithUnusedCollectionLeftJoin() {
        this
        .session
        .createQuery("select d from Department d left join d.employees e order by e.name asc")
        .unlimitedCount();
        
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
        .session
        .createQuery(
                "select d from Department d "
                + "left join d.employees e "
                + "where e.name like :name "
                + "order by e.name asc"
        )
        .setParameter("name", "%i%")
        .unlimitedCount();
        
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
        .session
        .createQuery("select d from Department d inner join d.employees")
        .unlimitedCount();
        
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
        .session
        .createQuery("select d from Department d group by d")
        .unlimitedCount();
    }
    
    @Test(expected = QueryException.class) 
    public void testUnlimitedCountWithTooManySelections() {
        this
        .session
        .createQuery("select d, e from Department d, Employee e")
        .unlimitedCount();
    }
    
    @Test(expected = QueryException.class) 
    public void testUnlimitedCountWithBadSelection() {
        this
        .session
        .createQuery("select sum(d.id) from Department d")
        .unlimitedCount();
    }
    
    @Test(expected = QueryException.class) 
    public void testUnlimitedCountWithImplictSelectionAndSeveralForms() {
        this
        .session
        .createQuery("from Department, Employee")
        .unlimitedCount();
    }
    
    @Test(expected = QueryException.class) 
    public void testUnlimitedCountWithImplicitSelectionAndNonFetchJoin() {
        this
        .session
        .createQuery("from Department d inner join d.employees")
        .unlimitedCount();
    }
}
