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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.Assert;

import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class OracleBlobTest extends AbstractOracleTest {

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
                Employee jim = new Employee();
                Employee kate = new Employee();
                Employee sam = new Employee();
                Employee tom = new Employee();
                Employee linda = new Employee();
                Employee adam = new Employee();
                market.setName("market");
                market.setImage(new byte[] { 11, 22, 33, 44, 55, 44, 33, 22, 11 });
                development.setName("development");
                development.setImage(new byte[] { 55, 44, 33, 22, 11, 22, 33, 44, 55 });
                jim.setName(new Name("jim", "cotton"));
                jim.setImage(new byte[] { 11, 22, 33, 44, 55, 66, 77, 88, 99 });
                kate.setName(new Name("kate", "hill"));
                kate.setImage(new byte[] { 12, 23, 34, 45, 56, 67, 78, 89, 90 });
                sam.setName(new Name("Sam", "Carter"));
                sam.setImage(new byte[] { 13, 24, 35, 46, 57, 68, 79, 80, 91 });
                tom.setName(new Name("tom", "george"));
                tom.setImage(new byte[] { 14, 25, 36, 47, 58, 69, 70, 81, 92 });
                linda.setName(new Name("Linda", "Sharp"));
                linda.setImage(new byte[] { 15, 26, 37, 48, 59, 60, 71, 82, 93 });
                adam.setName(new Name("adam", "brook"));
                adam.setImage(new byte[] { 16, 27, 38, 40, 50, 61, 72, 83, 94 });
                market.getEmployees().add(jim);
                market.getEmployees().add(kate);
                market.getEmployees().add(sam);
                development.getEmployees().add(tom);
                development.getEmployees().add(linda);
                development.getEmployees().add(adam);
                kate.setSupervisor(jim);
                sam.setSupervisor(jim);
                linda.setSupervisor(tom);
                adam.setSupervisor(tom);
                em.persist(market);
                em.persist(development);
                em.persist(jim);
                em.persist(kate);
                em.persist(sam);
                em.persist(tom);
                em.persist(linda);
                em.persist(adam);
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
    public void testBlobNotLoaded() {
        Department market = this.entityManager.find(Department.class, 1L);
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(market, "image"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBlobLoaded() {
        Department market = this.entityManager.find(
                Department.class, 
                1L, 
                Department__.begin().image().end());
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(market, "image"));
        Assert.assertTrue(
                Arrays.equals(
                        new byte[] { 11, 22, 33, 44, 55, 44, 33, 22, 11 }, 
                        market.getImage()
                )
        );
    }
    
    @Test
    public void testLoadDepartmentBlobWithSelection() {
        
        List<Department> departments = 
                this
                .entityManager
                .createQuery("select d from Department d", Department.class)
                .setQueryPaths(
                        Department__.begin().image().end(),
                        Department__.begin().employees().end(),
                        Department__.preOrderBy().id().asc(),
                        Department__.preOrderBy().employees().id().asc()
                )
                .getResultList();
        this.assertLoadBlob(departments, true, false);
    }
    
    @Test
    public void testLoadDepartmentBlobWithoutSelection() {
        
        List<Department> departments = 
                this
                .entityManager
                .createQuery("from Department", Department.class)
                .setQueryPaths(
                        Department__.begin().image().end(),
                        Department__.begin().employees().end(),
                        Department__.preOrderBy().id().asc(),
                        Department__.preOrderBy().employees().id().asc()
                )
                .getResultList();
        this.assertLoadBlob(departments, true, false);
    }
    
    @Test
    public void testLoadEmployeeBlobWithSelection() {
        
        List<Department> departments = 
                this
                .entityManager
                .createQuery("select d from Department d", Department.class)
                .setQueryPaths(
                        Department__.begin().employees().image().end(),
                        Department__.preOrderBy().id().asc(),
                        Department__.preOrderBy().employees().id().asc()
                )
                .getResultList();
        this.assertLoadBlob(departments, false, true);
    }
    
    @Test
    public void testLoadEmployeeBlobWithoutSelection() {
        
        List<Department> departments = 
                this
                .entityManager
                .createQuery("from Department", Department.class)
                .setQueryPaths(
                        Department__.begin().employees().image().end(),
                        Department__.preOrderBy().id().asc(),
                        Department__.preOrderBy().employees().id().asc()
                )
                .getResultList();
        this.assertLoadBlob(departments, false, true);
    }
    
    @Test
    public void testLoadAllBlobsWithSelection() {
        
        List<Department> departments = 
                this
                .entityManager
                .createQuery("select d from Department d", Department.class)
                .setQueryPaths(
                        Department__.begin().image().end(),
                        Department__.begin().employees().image().end(),
                        Department__.preOrderBy().id().asc(),
                        Department__.preOrderBy().employees().id().asc()
                )
                .getResultList();
        this.assertLoadBlob(departments, true, true);
    }
    
    @Test
    public void testLoadAllBlobsWithoutSelection() {
        
        List<Department> departments = 
                this
                .entityManager
                .createQuery("from Department", Department.class)
                .setQueryPaths(
                        Department__.begin().image().end(),
                        Department__.begin().employees().image().end(),
                        Department__.preOrderBy().id().asc(),
                        Department__.preOrderBy().employees().id().asc()
                )
                .getResultList();
        this.assertLoadBlob(departments, true, true);
    }
    
    @Test
    public void testLoadDepartmentBlobViaTuplesByMode1() {
        List<Object[]> tuples = 
                this
                .entityManager
                .createQuery("select d, e from Department d, Employee e where d = e.department", Object[].class)
                .setQueryPaths(
                        "d", 
                        Department__.begin().image().end(),
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.preOrderBy().id().asc()
                )
                .getResultList();
        this.assertLoadBlobViaTuples(tuples, true, false);
    }
    
    @Test
    public void testLoadDepartmentBlobViaTuplesByMode2() {
        List<Object[]> tuples = 
                this
                .entityManager
                .createQuery("from Department d, Employee e where d = e.department", Object[].class)
                .setQueryPaths(
                        "d", 
                        Department__.begin().image().end(),
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.preOrderBy().id().asc()
                )
                .getResultList();
        this.assertLoadBlobViaTuples(tuples, true, false);
    }
    
    @Test
    public void testLoadDepartmentBlobViaTuplesByMode3() {
        List<Object[]> tuples = 
                this
                .entityManager
                // Look carefully, this is "join", not "join fetch"!
                .createQuery("select d, e from Department d join d.employees e", Object[].class)
                .setQueryPaths(
                        "d", 
                        Department__.begin().image().end(),
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.preOrderBy().id().asc()
                )
                .getResultList();
        this.assertLoadBlobViaTuples(tuples, true, false);
    }
    
    @Test
    public void testLoadDepartmentBlobViaTuplesByMode4() {
        List<Object[]> tuples = 
                this
                .entityManager
                // Look carefully, this is "join", not "join fetch"!
                .createQuery("from Department d join d.employees e", Object[].class)
                .setQueryPaths(
                        "d", 
                        Department__.begin().image().end(),
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.preOrderBy().id().asc()
                )
                .getResultList();
        this.assertLoadBlobViaTuples(tuples, true, false);
    }
    
    @Test
    public void testLoadEmployeeBlobViaTuplesByMode1() {
        List<Object[]> tuples = 
                this
                .entityManager
                .createQuery("select d, e from Department d, Employee e where d = e.department", Object[].class)
                .setQueryPaths(
                        "d", 
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.begin().image().end(),
                        Employee__.preOrderBy().id().asc()
                )
                .getResultList();
        this.assertLoadBlobViaTuples(tuples, false, true);
    }
    
    @Test
    public void testLoadEmployeeBlobViaTuplesByMode2() {
        List<Object[]> tuples = 
                this
                .entityManager
                .createQuery("from Department d, Employee e where d = e.department", Object[].class)
                .setQueryPaths(
                        "d", 
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.begin().image().end(),
                        Employee__.preOrderBy().id().asc()
                )
                .getResultList();
        this.assertLoadBlobViaTuples(tuples, false, true);
    }
    
    @Test
    public void testLoadEmployeeBlobViaTuplesByMode3() {
        List<Object[]> tuples = 
                this
                .entityManager
                // Look carefully, this is "join", not "join fetch"!
                .createQuery("select d, e from Department d join d.employees e", Object[].class)
                .setQueryPaths(
                        "d", 
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.begin().image().end(),
                        Employee__.preOrderBy().id().asc()
                )
                .getResultList();
        this.assertLoadBlobViaTuples(tuples, false, true);
    }
    
    @Test
    public void testLoadEmployeeBlobViaTuplesByMode4() {
        List<Object[]> tuples = 
                this
                .entityManager
                // Look carefully, this is "join", not "join fetch"!
                .createQuery("from Department d join d.employees e", Object[].class)
                .setQueryPaths(
                        "d", 
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.begin().image().end(),
                        Employee__.preOrderBy().id().asc()
                )
                .getResultList();
        this.assertLoadBlobViaTuples(tuples, false, true);
    }
    
    @Test
    public void testLoadAllBlobsViaTuplesByMode1() {
        List<Object[]> tuples = 
                this
                .entityManager
                .createQuery("select d, e from Department d, Employee e where d = e.department", Object[].class)
                .setQueryPaths(
                        "d", 
                        Department__.begin().image().end(),
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.begin().image().end(),
                        Employee__.preOrderBy().id().asc()
                )
                .getResultList();
        this.assertLoadBlobViaTuples(tuples, true, true);
    }
    
    @Test
    public void testLoadAllBlobsViaTuplesByMode2() {
        List<Object[]> tuples = 
                this
                .entityManager
                .createQuery("from Department d, Employee e where d = e.department", Object[].class)
                .setQueryPaths(
                        "d", 
                        Department__.begin().image().end(),
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.begin().image().end(),
                        Employee__.preOrderBy().id().asc()
                )
                .getResultList();
        this.assertLoadBlobViaTuples(tuples, true, true);
    }
    
    @Test
    public void testLoadAllBlobsViaTuplesByMode3() {
        List<Object[]> tuples = 
                this
                .entityManager
                // Look carefully, this is "join", not "join fetch"!
                .createQuery("select d, e from Department d join d.employees e", Object[].class)
                .setQueryPaths(
                        "d", 
                        Department__.begin().image().end(),
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.begin().image().end(),
                        Employee__.preOrderBy().id().asc()
                )
                .getResultList();
        this.assertLoadBlobViaTuples(tuples, true, true);
    }
    
    @Test
    public void testLoadAllBlobsViaTuplesByMode4() {
        List<Object[]> tuples = 
                this
                .entityManager
                // Look carefully, this is "join", not "join fetch"!
                .createQuery("from Department d join d.employees e", Object[].class)
                .setQueryPaths(
                        "d", 
                        Department__.begin().image().end(),
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.begin().image().end(),
                        Employee__.preOrderBy().id().asc()
                )
                .getResultList();
        this.assertLoadBlobViaTuples(tuples, true, true);
    }
    
    private void assertLoadBlob(List<Department> departments, boolean departmentImageLoaded, boolean employeeImageLoaded) {
        
        Assert.assertEquals(2, departments.size());
        Department market = departments.get(0);
        Department development = departments.get(1);
        
        Assert.assertEquals("market", market.getName());
        Assert.assertEquals("development", development.getName());
        if (departmentImageLoaded) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(market, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 11, 22, 33, 44, 55, 44, 33, 22, 11 }, market.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(development, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 55, 44, 33, 22, 11, 22, 33, 44, 55 }, development.getImage()));
        } else {
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(market, "image"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(development, "image"));
        }
    
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(market.getEmployees()));
        Assert.assertEquals(3, market.getEmployees().size());
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(development.getEmployees()));
        Assert.assertEquals(3, development.getEmployees().size());
        
        Iterator<Employee> itr = market.getEmployees().iterator();
        Employee jim = itr.next();
        Employee kate = itr.next();
        Employee sam = itr.next();
        itr = development.getEmployees().iterator();
        Employee tom = itr.next();
        Employee linda = itr.next();
        Employee adam = itr.next();
        
        Assert.assertEquals(new Name("jim", "cotton"), jim.getName());
        Assert.assertEquals(new Name("kate", "hill"), kate.getName());
        Assert.assertEquals(new Name("Sam", "Carter"), sam.getName());
        Assert.assertEquals(new Name("tom", "george"), tom.getName());
        Assert.assertEquals(new Name("Linda", "Sharp"), linda.getName());
        Assert.assertEquals(new Name("adam", "brook"), adam.getName());
        if (employeeImageLoaded) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(jim, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 11, 22, 33, 44, 55, 66, 77, 88, 99 }, jim.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(kate, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 12, 23, 34, 45, 56, 67, 78, 89, 90 }, kate.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(sam, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 13, 24, 35, 46, 57, 68, 79, 80, 91 }, sam.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(tom, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 14, 25, 36, 47, 58, 69, 70, 81, 92 }, tom.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(linda, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 15, 26, 37, 48, 59, 60, 71, 82, 93 }, linda.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(adam, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 16, 27, 38, 40, 50, 61, 72, 83, 94 }, adam.getImage()));
        } else {
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(jim, "image"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(kate, "image"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(sam, "image"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(tom, "image"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(linda, "image"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(adam, "image"));
        }
    }

    private void assertLoadBlobViaTuples(List<Object[]> tuples, boolean departmentImageLoaded, boolean employeeImageLoaded) {
        
        Assert.assertEquals(6, tuples.size());
        Assert.assertSame(tuples.get(0)[0], tuples.get(1)[0]);
        Assert.assertSame(tuples.get(0)[0], tuples.get(2)[0]);
        Assert.assertSame(tuples.get(3)[0], tuples.get(4)[0]);
        Assert.assertSame(tuples.get(3)[0], tuples.get(5)[0]);
        
        Department market = (Department)tuples.get(0)[0];
        Department development = (Department)tuples.get(3)[0];
        Employee jim = (Employee)tuples.get(0)[1];
        Employee kate = (Employee)tuples.get(1)[1];
        Employee sam = (Employee)tuples.get(2)[1];
        Employee tom = (Employee)tuples.get(3)[1];
        Employee linda = (Employee)tuples.get(4)[1];
        Employee adam = (Employee)tuples.get(5)[1];
        
        Assert.assertEquals("market", market.getName());
        Assert.assertEquals("development", development.getName());
        if (departmentImageLoaded) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(market, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 11, 22, 33, 44, 55, 44, 33, 22, 11 }, market.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(development, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 55, 44, 33, 22, 11, 22, 33, 44, 55 }, development.getImage()));
        } else {
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(market, "image"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(development, "image"));
        }

        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(market.getEmployees()));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(development.getEmployees()));
        
        Assert.assertEquals(new Name("jim", "cotton"), jim.getName());
        Assert.assertEquals(new Name("kate", "hill"), kate.getName());
        Assert.assertEquals(new Name("Sam", "Carter"), sam.getName());
        Assert.assertEquals(new Name("tom", "george"), tom.getName());
        Assert.assertEquals(new Name("Linda", "Sharp"), linda.getName());
        Assert.assertEquals(new Name("adam", "brook"), adam.getName());
        if (employeeImageLoaded) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(jim, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 11, 22, 33, 44, 55, 66, 77, 88, 99 }, jim.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(kate, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 12, 23, 34, 45, 56, 67, 78, 89, 90 }, kate.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(sam, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 13, 24, 35, 46, 57, 68, 79, 80, 91 }, sam.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(tom, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 14, 25, 36, 47, 58, 69, 70, 81, 92 }, tom.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(linda, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 15, 26, 37, 48, 59, 60, 71, 82, 93 }, linda.getImage()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(adam, "image"));
            Assert.assertTrue(Arrays.equals(new byte[] { 16, 27, 38, 40, 50, 61, 72, 83, 94 }, adam.getImage()));
        } else {
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(jim, "image"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(kate, "image"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(sam, "image"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(tom, "image"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(linda, "image"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(adam, "image"));
        }
    }
}
