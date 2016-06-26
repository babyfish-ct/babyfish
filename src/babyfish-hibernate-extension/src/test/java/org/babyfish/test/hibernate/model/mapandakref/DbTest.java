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
package org.babyfish.test.hibernate.model.mapandakref;

import java.util.Map.Entry;

import javax.persistence.Persistence;

import org.babyfish.collection.XOrderedMap;
import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class DbTest {

    private static XEntityManagerFactory entityManagerFactory;
    
    @BeforeClass
    public static void initEntityManagerFactory() {
        if (entityManagerFactory == null) {
            entityManagerFactory =
                    new HibernatePersistenceProvider(
                            DbTest.class.getPackage().getName().replace('.', '/') + 
                            "/persistence.xml")
                    .createEntityManagerFactory(null, null);
        }
        XEntityManager em = entityManagerFactory.createEntityManager();
        try {
            Company oracle = new Company("oracle");
            Company ibm = new Company("ibm");
            Department sales = new Department("market");
            Department market = new Department("sales");
            em.getTransaction().begin();
            try {
                em.persist(oracle);
                em.persist(ibm);
                em.persist(sales);
                em.persist(market);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    @AfterClass
    public static void disposeEntityManagerFactory() {
        XEntityManagerFactory emf = entityManagerFactory;
        if (emf != null) {
            entityManagerFactory = null;
            emf.close();
        }
    }
    
    @Before
    public void initData() {
        XEntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            try {
                em.createNativeQuery("update omkr_DEPARTMENT d set d.COMPANY_ID = null").executeUpdate();
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    @Test
    public void testChangeMapOfCompany() {
        XEntityManager em = entityManagerFactory.createEntityManager();
        try {
            Company oracle = em.getReference(Company.class, 1L);
            Company ibm = em.getReference(Company.class, 2L);
            Department market = em.getReference(Department.class, 1L);
            Department sales = em.getReference(Department.class, 2L);
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(ibm.getDepartments()));
            
            em.getTransaction().begin();
            try {
                ((XOrderedMap<String, Department>)oracle.getDepartments()).descendingMap().put(market.getName(), market);
                ((XOrderedMap<String, Department>)oracle.getDepartments()).descendingMap().put(sales.getName(), sales);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
            
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(ibm.getDepartments()));
            
            Assert.assertTrue(oracle.getDepartments().containsKey(sales.getName()));
            Assert.assertTrue(oracle.getDepartments().containsValue(sales));
            Assert.assertTrue(oracle.getDepartments().containsKey(market.getName()));
            Assert.assertTrue(oracle.getDepartments().containsValue(market));
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Company oracle = em.getReference(Company.class, 1L);
            Company ibm = em.getReference(Company.class, 2L);
            Department market = em.getReference(Department.class, 1L);
            Department sales = em.getReference(Department.class, 2L);
            
            assertDepartments(oracle, "sales", "market");
            assertDepartments(ibm);
            Assert.assertSame(oracle, sales.getCompany());
            Assert.assertSame(oracle, market.getCompany());
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Company oracle = em.getReference(Company.class, 1L);
            Company ibm = em.getReference(Company.class, 2L);
            Department market = em.getReference(Department.class, 1L);
            Department sales = em.getReference(Department.class, 2L);
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(ibm.getDepartments()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
            
            em.getTransaction().begin();
            try {
                ((XOrderedMap<String, Department>)ibm.getDepartments()).descendingMap().put(sales.getName(), sales);
                ((XOrderedMap<String, Department>)ibm.getDepartments()).descendingMap().put(market.getName(), market);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
            
            Assert.assertFalse(oracle.getDepartments().containsKey(sales.getName()));
            Assert.assertFalse(oracle.getDepartments().containsKey(market.getName()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
    
            Assert.assertFalse(oracle.getDepartments().containsValue(sales));
            Assert.assertFalse(oracle.getDepartments().containsValue(market));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
            
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(ibm.getDepartments()));
            Assert.assertTrue(ibm.getDepartments().containsKey(sales.getName()));
            Assert.assertTrue(ibm.getDepartments().containsValue(sales));
            Assert.assertTrue(ibm.getDepartments().containsKey(market.getName()));
            Assert.assertTrue(ibm.getDepartments().containsValue(market));
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Company oracle = em.getReference(Company.class, 1L);
            Company ibm = em.getReference(Company.class, 2L);
            Department market = em.getReference(Department.class, 1L);
            Department sales = em.getReference(Department.class, 2L);
            
            assertDepartments(oracle);
            assertDepartments(ibm, "sales", "market");
            Assert.assertSame(ibm, sales.getCompany());
            Assert.assertSame(ibm, market.getCompany());
        } finally {
            em.close();
        }
    }
    
    @Test
    public void testChangeKeySetOfCompany() {
        XEntityManager em = entityManagerFactory.createEntityManager();
        try {
            Company oracle = em.getReference(Company.class, 1L);
            Company ibm = em.getReference(Company.class, 2L);
            Department market = em.getReference(Department.class, 1L);
            Department sales = em.getReference(Department.class, 2L);
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(ibm.getDepartments()));
            
            em.getTransaction().begin();
            try {
                ((XOrderedMap<String, Department>)oracle.getDepartments()).descendingMap().put(market.getName(), market);
                ((XOrderedMap<String, Department>)oracle.getDepartments()).descendingMap().put(sales.getName(), sales);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
            
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(ibm.getDepartments()));
            
            Assert.assertTrue(oracle.getDepartments().containsKey(sales.getName()));
            Assert.assertTrue(oracle.getDepartments().containsValue(sales));
            Assert.assertTrue(oracle.getDepartments().containsKey(market.getName()));
            Assert.assertTrue(oracle.getDepartments().containsValue(market));
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Company oracle = em.getReference(Company.class, 1L);
            Company ibm = em.getReference(Company.class, 2L);
            Department market = em.getReference(Department.class, 1L);
            Department sales = em.getReference(Department.class, 2L);
            
            assertDepartments(oracle, "sales", "market");
            assertDepartments(ibm);
            Assert.assertSame(oracle, sales.getCompany());
            Assert.assertSame(oracle, market.getCompany());
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Company oracle = em.getReference(Company.class, 1L);
            Company ibm = em.getReference(Company.class, 2L);
            Department market = em.getReference(Department.class, 1L);
            Department sales = em.getReference(Department.class, 2L);
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(ibm.getDepartments()));
            
            em.getTransaction().begin();
            try {
                ((XOrderedMap<String, Department>)oracle.getDepartments()).descendingMap().descendingKeySet().descendingSet().remove("market");
                ((XOrderedMap<String, Department>)oracle.getDepartments()).descendingMap().descendingKeySet().descendingSet().remove("sales");
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
            
            Assert.assertFalse(oracle.getDepartments().containsKey("sales"));
            Assert.assertFalse(oracle.getDepartments().containsKey("market"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
    
            Assert.assertFalse(oracle.getDepartments().containsValue(sales));
            Assert.assertFalse(oracle.getDepartments().containsValue(market));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
            
        } finally {
            em.close();
        }
    }
    
    @Test
    public void testChangeReferenceOfDepartment() {
        XEntityManager em = entityManagerFactory.createEntityManager();
        try {
            Company oracle = em.getReference(Company.class, 1L);
            Company ibm = em.getReference(Company.class, 2L);
            Department market = em.getReference(Department.class, 1L);
            Department sales = em.getReference(Department.class, 2L);
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(ibm.getDepartments()));
            
            em.getTransaction().begin();
            try {
                market.setCompany(oracle);
                sales.setCompany(oracle);
                ((XOrderedMap<String, Department>)oracle.getDepartments()).descendingMap().put(market.getName(), market);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
            
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(ibm.getDepartments()));
            
            Assert.assertTrue(oracle.getDepartments().containsKey(sales.getName()));
            Assert.assertTrue(oracle.getDepartments().containsValue(sales));
            Assert.assertTrue(oracle.getDepartments().containsKey(market.getName()));
            Assert.assertTrue(oracle.getDepartments().containsValue(market));
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Company oracle = em.getReference(Company.class, 1L);
            Company ibm = em.getReference(Company.class, 2L);
            Department market = em.getReference(Department.class, 1L);
            Department sales = em.getReference(Department.class, 2L);
            
            assertDepartments(oracle, "sales", "market");
            assertDepartments(ibm);
            Assert.assertSame(oracle, sales.getCompany());
            Assert.assertSame(oracle, market.getCompany());
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Company oracle = em.getReference(Company.class, 1L);
            Company ibm = em.getReference(Company.class, 2L);
            Department market = em.getReference(Department.class, 1L);
            Department sales = em.getReference(Department.class, 2L);
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(ibm.getDepartments()));
            
            em.getTransaction().begin();
            try {
                sales.setCompany(ibm);
                market.setCompany(ibm);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
            
            Assert.assertFalse(oracle.getDepartments().containsKey(sales.getName()));
            Assert.assertFalse(oracle.getDepartments().containsKey(market.getName()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
    
            Assert.assertFalse(oracle.getDepartments().containsValue(sales));
            Assert.assertFalse(oracle.getDepartments().containsValue(market));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(oracle.getDepartments()));
            
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(ibm.getDepartments()));
            Assert.assertTrue(ibm.getDepartments().containsKey(sales.getName()));
            Assert.assertTrue(ibm.getDepartments().containsValue(sales));
            Assert.assertTrue(ibm.getDepartments().containsKey(market.getName()));
            Assert.assertTrue(ibm.getDepartments().containsValue(market));
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Company oracle = em.getReference(Company.class, 1L);
            Company ibm = em.getReference(Company.class, 2L);
            Department market = em.getReference(Department.class, 1L);
            Department sales = em.getReference(Department.class, 2L);
            
            assertDepartments(oracle);
            assertDepartments(ibm, "sales", "market");
            Assert.assertSame(ibm, sales.getCompany());
            Assert.assertSame(ibm, market.getCompany());
        } finally {
            em.close();
        }
    }
    
    private static void assertDepartments(Company company, String ... departmentNames) {
        Assert.assertEquals(departmentNames.length, company.getDepartments().size());
        int index = 0;
        for (Entry<String, Department> entry : company.getDepartments().entrySet()) {
            Assert.assertEquals(departmentNames[index], entry.getKey());
            Assert.assertEquals(departmentNames[index++], entry.getValue().getName());
        }
    }
    
}
