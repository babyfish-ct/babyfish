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
package org.babyfish.hibernate.fetch;

import java.util.List;

import junit.framework.Assert;

import org.babyfish.hibernate.XQuery;
import org.babyfish.hibernate.XSession;
import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.cfg.Configuration;
import org.babyfish.persistence.QueryType;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class DistinctLimitHQLTest extends AbstractOracleTest {

    private static final String RESOURCE =
            DistinctLimitHQLTest.class.getPackage().getName().replace('.', '/') + 
            "/hibernate.cfg.xml";
    
    private static XSessionFactory sessionFactory;
    
    private XSession session;
    
    @SuppressWarnings("deprecation")
    @BeforeClass
    public static void initSessionFactory() {
        sessionFactory =
                new Configuration()
                .configure(RESOURCE)
                .addProperties(getOracleProperites())
                .addAnnotatedClass(Department.class)
                .addAnnotatedClass(Employee.class)
                .addAnnotatedClass(AnnualLeave.class)
                .buildSessionFactory();
        Session session = sessionFactory.openSession();
        try {
            Transaction tx = session.beginTransaction();
            try {
                Department market = new Department();
                Department development = new Department();
                Department sales = new Department();
                Department hr = new Department();
                Employee jim = new Employee();
                Employee kate = new Employee();
                Employee tom = new Employee();
                Employee linda = new Employee();
                Employee sam = new Employee();
                Employee alice = new Employee();
                Employee adam = new Employee();
                Employee sharon = new Employee();
                market.setName("market");
                development.setName("development");
                sales.setName("sales");
                hr.setName("hr");
                jim.setName(new Name("jim", "cotton"));
                kate.setName(new Name("kate", "hill"));
                tom.setName(new Name("tom", "george"));
                linda.setName(new Name("linda", "sharp"));
                sam.setName(new Name("Sam", "Carter"));
                alice.setName(new Name("alice", "london"));
                adam.setName(new Name("adam", "brook"));
                sharon.setName(new Name("sharon", "fox"));
                market.getEmployees().add(jim);
                market.getEmployees().add(kate);
                development.getEmployees().add(tom);
                development.getEmployees().add(linda);
                sales.getEmployees().add(sam);
                sales.getEmployees().add(alice);
                hr.getEmployees().add(adam);
                hr.getEmployees().add(sharon);
                session.persist(market);
                session.persist(development);
                session.persist(sales);
                session.persist(hr);
                session.persist(jim);
                session.persist(kate);
                session.persist(tom);
                session.persist(linda);
                session.persist(sam);
                session.persist(alice);
                session.persist(adam);
                session.persist(sharon);
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            } catch (Error err) {
                tx.rollback();
                throw err;
            }
            tx.commit();
        } finally {
            session.close();
        }
    }
    
    @AfterClass
    public static void disposeSessionFactory() {
        XSessionFactory emf = sessionFactory;
        if (emf != null) {
            sessionFactory = null;
            emf.close();
        }
    } 
    
    @Before
    public void initSession() {
        this.session = sessionFactory.openSession();
    }
    
    @After
    public void disposeSession() {
        XSession em = this.session;
        if (em != null) {
            this.session = null;
            em.close();
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testByDistinctMode() {
        XQuery query = this.createQuery(QueryType.DISTINCT, 1, 2);
        List<Department> departments = query.list();
        
        Assert.assertEquals(4, query.unlimitedCount());
        Assert.assertEquals(2, departments.size());
        Assert.assertEquals("hr", departments.get(0).getName());
        Assert.assertEquals("market", departments.get(1).getName());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testByResultMode() {
        XQuery query = this.createQuery(QueryType.RESULT, 2, 4);
        List<Department> departments = query.list();
        
        Assert.assertEquals(8, query.unlimitedCount());
        Assert.assertEquals(4, departments.size());
        Assert.assertSame(departments.get(0), departments.get(1));
        Assert.assertSame(departments.get(2), departments.get(3));
        Assert.assertEquals("hr", departments.get(0).getName());
        Assert.assertEquals("market", departments.get(3).getName());
    }
    
    private XQuery createQuery(
            QueryType queryType,
            int firstResult,
            int maxResults) {
        return 
                this.
                session
                .createQuery("from Department d order by d.name")
                .setQueryPaths(Department__.begin().employees().end())
                .setQueryType(queryType)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);
    }
}
