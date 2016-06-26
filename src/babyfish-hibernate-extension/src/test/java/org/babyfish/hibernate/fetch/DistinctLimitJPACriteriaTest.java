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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import junit.framework.Assert;

import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.persistence.QueryType;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfish.persistence.XTypedQuery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class DistinctLimitJPACriteriaTest extends AbstractOracleTest {

    private static XEntityManagerFactory entityManagerFactory;
    
    private XEntityManager entityManager;
    
    @BeforeClass
    public static void initEntityManagerFactory() {
        entityManagerFactory =
                new HibernatePersistenceProvider(
                        JPQLTest.class.getPackage().getName().replace('.', '/') + 
                        "/persistence.xml")
                .createEntityManagerFactory(
                        null, 
                        getOraclePropertyMap()
                );
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
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
                em.persist(market);
                em.persist(development);
                em.persist(sales);
                em.persist(hr);
                em.persist(jim);
                em.persist(kate);
                em.persist(tom);
                em.persist(linda);
                em.persist(sam);
                em.persist(alice);
                em.persist(adam);
                em.persist(sharon);
            } catch (RuntimeException ex) {
                em.getTransaction().rollback();
                throw ex;
            } catch (Error err) {
                em.getTransaction().rollback();
                throw err;
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    @AfterClass
    public static void disposeEntityManagerFactory() {
        EntityManagerFactory emf = entityManagerFactory;
        if (emf != null) {
            entityManagerFactory = null;
            emf.close();
        }
    } 
    
    @Before
    public void initEntityManager() {
        this.entityManager = entityManagerFactory.createEntityManager();
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
    public void testByDistinctMode() {
        XTypedQuery<Department> typedQuery = this.createQuery(QueryType.DISTINCT, 1, 2);
        List<Department> departments = typedQuery.getResultList();
        
        Assert.assertEquals(4, typedQuery.getUnlimitedCount());
        Assert.assertEquals(2, departments.size());
        Assert.assertEquals("hr", departments.get(0).getName());
        Assert.assertEquals("market", departments.get(1).getName());
    }
    
    @Test
    public void testByResultMode() {
        XTypedQuery<Department> typedQuery = this.createQuery(QueryType.RESULT, 2, 4);
        List<Department> departments = typedQuery.getResultList();
        
        Assert.assertEquals(8, typedQuery.getUnlimitedCount());
        Assert.assertEquals(4, departments.size());
        Assert.assertSame(departments.get(0), departments.get(1));
        Assert.assertSame(departments.get(2), departments.get(3));
        Assert.assertEquals("hr", departments.get(0).getName());
        Assert.assertEquals("market", departments.get(3).getName());
    }
    
    private XTypedQuery<Department> createQuery(
            QueryType queryType,
            int firstResult,
            int maxResults) {
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Department> criteria = cb.createQuery(Department.class);
        Root<Department> d = criteria.from(Department.class);
        criteria.orderBy(cb.asc(d.get(Department_.name)));
        return 
                this.
                entityManager
                .createQuery(criteria)
                .setQueryPaths(Department__.begin().employees().end())
                .setQueryType(queryType)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);
    }
}
