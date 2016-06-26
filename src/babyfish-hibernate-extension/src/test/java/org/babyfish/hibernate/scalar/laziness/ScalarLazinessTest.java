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
package org.babyfish.hibernate.scalar.laziness;

import java.util.Arrays;

import javax.persistence.Persistence;

import junit.framework.Assert;

import org.babyfish.model.jpa.path.QueryPaths;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.hibernate.LazyInitializationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ScalarLazinessTest extends AbstractTest {
    
    private static XEntityManagerFactory entityManagerFactory;
    
    @BeforeClass
    public static void beforeClass() {
        entityManagerFactory = initEntityManagerFactory();
    }

    @AfterClass
    public static void afterClass() {
        XEntityManagerFactory emf = entityManagerFactory;
        if (emf != null) {
            entityManagerFactory = null;
            emf.close();
        }
    }
    
    @Test
    public void testNotLoaded() {
        Employee jim;
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            jim = 
                    em
                    .createQuery("select e from Employee e where e.name = :name", Employee.class)
                    .setParameter("name", "Jim")
                    .getSingleResult();
        }
        
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(jim, "image"));
        try {
            jim.getImage();
            Assert.fail();
        } catch (LazyInitializationException ex) {
            // Do nothing
        }
    }
    
    @Test
    public void testLoaded() {
        Employee jim;
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            jim = 
                    em
                    .createQuery("select e from Employee e where e.name = :name", Employee.class)
                    .setParameter("name", "Jim")
                    .getSingleResult();
            
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(jim, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 11, 22, 33, 44, 55, 66, 77, 88, 99 }, jim.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(jim, "image"));
        }
    }
    
    @Test
    public void testParentScalarNotLoadedByPreLoadParent() {
        Department market;
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            market = 
                    em
                    .createQuery("select e from Employee e where e.name = :name", Employee.class)
                    .setQueryPaths(QueryPaths.compile("department"))
                    .setParameter("name", "Jim")
                    .getSingleResult()
                    .getDepartment();
        }
        
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(market, "image"));
        try {
            market.getImage();
            Assert.fail();
        } catch (LazyInitializationException ex) {
            // Do nothing
        }
    }
    
    @Test
    public void testParentScalarLoadedByPreLoadParent() {
        Department market;
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            market = 
                    em
                    .createQuery("select e from Employee e where e.name = :name", Employee.class)
                    .setQueryPaths(QueryPaths.compile("department"))
                    .setParameter("name", "Jim")
                    .getSingleResult()
                    .getDepartment();
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(market, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 99, 88, 77, 66, 55, 44, 33, 22, 11 }, market.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(market, "image"));
        }
    }
    
    @Test
    public void testParentScalarNotLoadedByLazyLoadParent() {
        Department market;
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            market = 
                    em
                    .createQuery("select e from Employee e where e.name = :name", Employee.class)
                    .setParameter("name", "Jim")
                    .getSingleResult()
                    .getDepartment();
        }
        
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(market, "image"));
        try {
            market.getImage();
            Assert.fail();
        } catch (LazyInitializationException ex) {
            // Do nothing
        }
    }
    
    @Test
    public void testParentScalarLoadedByLazyLoadParent() {
        Department market;
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            market = 
                    em
                    .createQuery("select e from Employee e where e.name = :name", Employee.class)
                    .setParameter("name", "Jim")
                    .getSingleResult()
                    .getDepartment();
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(market, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 99, 88, 77, 66, 55, 44, 33, 22, 11 }, market.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(market, "image"));
        }
    }
}
