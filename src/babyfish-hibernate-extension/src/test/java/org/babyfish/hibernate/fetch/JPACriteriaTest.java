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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.model.jpa.path.CollectionFetchType;
import org.babyfish.model.jpa.path.GetterType;
import org.babyfish.persistence.QueryType;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfish.persistence.XTypedQuery;
import org.babyfish.persistence.criteria.JoinMode;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XRoot;
import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class JPACriteriaTest {
    
    private static final DateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd");

    private static XEntityManagerFactory entityManagerFactory;
    
    private XEntityManager entityManager;
    
    @BeforeClass
    public static void initEntityManagerFactory() {
        entityManagerFactory =
                new HibernatePersistenceProvider(
                        JPQLTest.class.getPackage().getName().replace('.', '/') + 
                        "/persistence.xml")
                .createEntityManagerFactory(null, null);
        XEntityManager em = entityManagerFactory.createEntityManager();
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
                sam.setName(new Name("sam", "carter"));
                sam.setImage(new byte[] { 13, 24, 35, 46, 57, 68, 79, 80, 91 });
                tom.setName(new Name("tom", "george"));
                tom.setImage(new byte[] { 14, 25, 36, 47, 58, 69, 70, 81, 92 });
                linda.setName(new Name("linda", "sharp"));
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
                Employee[] employees = { jim, kate, sam, tom, linda, adam };
                for (Employee employee : employees) {
                    employee.getAnnualLeaves().add(
                            new AnnualLeave(
                                    date("2012-06-05"), 
                                    date("2012-06-08"), 
                                    "Spouse is ill, goto hospital.")
                    );
                    employee.getAnnualLeaves().add(
                            new AnnualLeave(date(
                                    "2012-07-05"), 
                                    date("2012-07-08"),
                                    "10th anniversary of marriage."
                            )
                    );
                }
            } catch (final RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
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
    public void testHibernateJoin() {
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> criteria = cb.createQuery(Object[].class);
        Root<Department> d = criteria.from(Department.class);
        Join<Department, Employee> e = d.join(Department_.employees);
        criteria.multiselect(d, e);
        List<Object[]> items = 
                this
                .entityManager
                .createQuery(criteria)
                .getResultList();
        Assert.assertEquals(6, items.size());
        for (Object[] item : items) {
            Assert.assertTrue(item[0] instanceof Department);
            Assert.assertTrue(item[1] instanceof Employee);
        }
    }
    
    @Test
    public void testCollectionFetchByAllModeAndQueryPath() {
        this.testCollectionFetchByAllMode(typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees().end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end()
                    );
                },
                false, 
                null);
        this.testCollectionFetchByAllMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees().end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().asc(),
                            Department__.postOrderBy().employees().name().firstName().asc()
                    );
                },
                false, 
                false);
        this.testCollectionFetchByAllMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees().end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().asc(),
                            Department__.postOrderBy().employees().name().firstName().desc()
                    );
                },
                false, 
                true);
        this.testCollectionFetchByAllMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees().end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().desc(),
                            Department__.postOrderBy().employees().name().firstName().asc()
                    );
                },
                true, 
                false);
        this.testCollectionFetchByAllMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees().end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().desc(),
                            Department__.postOrderBy().employees().name().firstName().desc()
                    );
                },
                true, 
                true);
    }
    
    @Test
    public void testCollectionFetchByAllModeAndTeypedQueryPath() {
        this.testCollectionFetchByAllMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees().end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end()
                    );
                },
                false, 
                null);
        this.testCollectionFetchByAllMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees().end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().asc(),
                            Department__.postOrderBy().employees().name().firstName().asc()
                    );
                },
                false, 
                false);
        this.testCollectionFetchByAllMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees().end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().asc(),
                            Department__.postOrderBy().employees().name().firstName().desc()
                    );
                },
                false, 
                true);
        this.testCollectionFetchByAllMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees().end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().desc(),
                            Department__.postOrderBy().employees().name().firstName().asc()
                    );
                },
                true, 
                false);
        this.testCollectionFetchByAllMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees().end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().desc(),
                            Department__.postOrderBy().employees().name().firstName().desc()
                    );
                },
                true, 
                true);
    }
    
    private void testCollectionFetchByAllMode(Consumer<XTypedQuery<Department>> addQueryPathLambda, boolean departmentNameDesc, Boolean employeeNameDesc) {
        XTypedQuery<Department> typedQuery;
        List<Department> departments;
        
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Department> cq = cb.createQuery(Department.class);
        Root<Department> d = cq.from(Department.class);
        Join<Department, Employee> e = d.join(Department_.employees);
        cq
        .where(cb.like(e.get(Employee_.name).get(Name_.firstName), "%m%"))
        .orderBy(cb.asc(d.get(Department_.name)))
        .select(d);
        
        this.entityManager.clear();
        typedQuery = this.entityManager.createQuery(cq);
        Assert.assertEquals(4, typedQuery.setQueryType(QueryType.RESULT).getUnlimitedCount());
        Assert.assertEquals(2, typedQuery.setQueryType(QueryType.DISTINCT).getUnlimitedCount());
        departments = typedQuery.getResultList();
        Assert.assertFalse(departments.isEmpty());
        for (Department department : departments) {
            Assert.assertFalse(Hibernate.isInitialized(department.getEmployees()));
        }
        
        addQueryPathLambda.accept(typedQuery);
        
        this.entityManager.clear();
        Assert.assertEquals(24, typedQuery.setQueryType(QueryType.RESULT).getUnlimitedCount());
        departments = typedQuery.getResultList();
        Assert.assertEquals(24, departments.size());
        for (Department department : departments) {
            Assert.assertTrue(Hibernate.isInitialized(department.getEmployees()));
            Assert.assertEquals(3, department.getEmployees().size());
            for (Employee employee : department.getEmployees()) {
                Assert.assertTrue(Hibernate.isInitialized(employee.getAnnualLeaves()));
                Assert.assertEquals(2, employee.getAnnualLeaves().size());
            }
        }
        assertOrder(departments, departmentNameDesc, employeeNameDesc);
        
        this.entityManager.clear();
        Assert.assertEquals(2, typedQuery.setQueryType(QueryType.DISTINCT).getUnlimitedCount());
        departments = typedQuery.getResultList();
        Assert.assertEquals(2, departments.size());
        for (Department department : departments) {
            Assert.assertTrue(Hibernate.isInitialized(department.getEmployees()));
            Assert.assertEquals(3, department.getEmployees().size());
            for (Employee employee : department.getEmployees()) {
                Assert.assertTrue(Hibernate.isInitialized(employee.getAnnualLeaves()));
                Assert.assertEquals(2, employee.getAnnualLeaves().size());
            }
        }
        assertOrder(departments, departmentNameDesc, employeeNameDesc);
    }
    
    @Test
    public void testCollectionFetchByPartialModeAndQueryPath() {
        this.testCollectionFetchByPartialMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees(CollectionFetchType.PARTIAL).end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end()
                    );
                }, 
                false, 
                null);
        this.testCollectionFetchByPartialMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees(CollectionFetchType.PARTIAL).end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().asc(),
                            Department__.postOrderBy().employees().name().firstName().asc()
                    );
                }, 
                false, 
                false);
        this.testCollectionFetchByPartialMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees(CollectionFetchType.PARTIAL).end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().asc(),
                            Department__.postOrderBy().employees().name().firstName().desc()
                    );
                }, 
                false, 
                true);
        this.testCollectionFetchByPartialMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees(CollectionFetchType.PARTIAL).end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().desc(),
                            Department__.postOrderBy().employees().name().firstName().asc()
                    );
                }, 
                true, 
                false);
        this.testCollectionFetchByPartialMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees(CollectionFetchType.PARTIAL).end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().desc(),
                            Department__.postOrderBy().employees().name().firstName().desc()
                    );
                }, 
                true, 
                true);
    }
    
    @Test
    public void testCollectionFetchByPartialModeAndTypedQueryPath() {
        this.testCollectionFetchByPartialMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees(CollectionFetchType.PARTIAL).end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end()
                    );
                }, 
                false, 
                null);
        this.testCollectionFetchByPartialMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees(CollectionFetchType.PARTIAL).end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().asc(),
                            Department__.postOrderBy().employees().name().firstName().asc()
                    );
                }, 
                false, 
                false);
        this.testCollectionFetchByPartialMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees(CollectionFetchType.PARTIAL).end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().asc(),
                            Department__.postOrderBy().employees().name().firstName().desc()
                    );
                }, 
                false, 
                true);
        this.testCollectionFetchByPartialMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees(CollectionFetchType.PARTIAL).end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().desc(),
                            Department__.postOrderBy().employees().name().firstName().asc()
                    );
                }, 
                true, 
                false);
        this.testCollectionFetchByPartialMode(
                typedQuery -> {
                    typedQuery.setQueryPaths(
                            Department__.begin().employees(CollectionFetchType.PARTIAL).end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end(),
                            Department__.preOrderBy().name().desc(),
                            Department__.postOrderBy().employees().name().firstName().desc()
                    );
                }, 
                true, 
                true);
    }
    
    private void testCollectionFetchByPartialMode(Consumer<XTypedQuery<Department>> addQueryPathLambda, boolean departmentNameDesc, Boolean employeeNameDesc) {
        List<Department> departments;
        XTypedQuery<Department> typedQuery;
        
        this.entityManager.clear();
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Department> criteria = cb.createQuery(Department.class);
        Root<Department> d = criteria.from(Department.class);
        Join<Department, Employee> e = d.join(Department_.employees);
        criteria
        .where(cb.like(e.get(Employee_.name).get(Name_.firstName), cb.parameter(String.class, "name")))
        .orderBy(cb.asc(d.get(Department_.name)))
        .select(d);
        typedQuery = 
                this
                .entityManager
                .createQuery(criteria)
                .setParameter("name", "%m%");
        
        Assert.assertEquals(4, typedQuery.setQueryType(QueryType.RESULT).getUnlimitedCount());
        Assert.assertEquals(2, typedQuery.setQueryType(QueryType.DISTINCT).getUnlimitedCount());
        departments = typedQuery.getResultList();
        Assert.assertFalse(departments.isEmpty());
        for (Department department : departments) {
            Assert.assertFalse(Hibernate.isInitialized(department.getEmployees()));
        }
        
        addQueryPathLambda.accept(typedQuery);
        
        this.entityManager.clear();
        Assert.assertEquals(8, typedQuery.setQueryType(QueryType.RESULT).getUnlimitedCount());
        departments = typedQuery.getResultList();
        Assert.assertEquals(8, departments.size());
        for (Department department : departments) {
            Assert.assertTrue(Hibernate.isInitialized(department.getEmployees()));
            Assert.assertEquals(2, department.getEmployees().size());
            for (Employee employee : department.getEmployees()) {
                Assert.assertTrue(Hibernate.isInitialized(employee.getAnnualLeaves()));
                Assert.assertEquals(2, employee.getAnnualLeaves().size());
            }
        }
        assertOrder(departments, departmentNameDesc, employeeNameDesc);
        
        this.entityManager.clear();
        Assert.assertEquals(2, typedQuery.setQueryType(QueryType.DISTINCT).getUnlimitedCount());
        departments = typedQuery.getResultList();
        Assert.assertEquals(2, departments.size());
        for (Department department : departments) {
            Assert.assertTrue(Hibernate.isInitialized(department.getEmployees()));
            Assert.assertEquals(2, department.getEmployees().size());
            for (Employee employee : department.getEmployees()) {
                Assert.assertTrue(Hibernate.isInitialized(employee.getAnnualLeaves()));
                Assert.assertEquals(2, employee.getAnnualLeaves().size());
            }
        }
        assertOrder(departments, departmentNameDesc, employeeNameDesc);
    }
    
    @Test
    public void testMergetPartialFetchToSharedJoinSucessful() {
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        department.join(Department_.employees, JoinMode.REQUIRED_TO_MERGE_EXISTS);
        department.fetch(Department_.employees, CollectionFetchType.PARTIAL);
        long count =
                this
                .entityManager
                .createQuery(cq)
                .setQueryPaths(Department__.begin().employees(CollectionFetchType.PARTIAL).end())
                .setQueryType(QueryType.RESULT)
                .getUnlimitedCount();
        Assert.assertEquals(6, count);
    }
    
    @Test
    public void testMergetPartialFetchToNotSharedJoinFailed() {
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        department.join(Department_.employees, JoinMode.REQUIRED_TO_CREATE_NEW);
        department.fetch(Department_.employees, CollectionFetchType.PARTIAL);
        cq.select(department);
        long count =
                this
                .entityManager
                .createQuery(cq)
                .setQueryPaths(Department__.begin().employees(CollectionFetchType.PARTIAL).end())
                .setQueryType(QueryType.RESULT)
                .getUnlimitedCount();
        Assert.assertEquals(18, count);
    }
    
    @Test
    public void testGetEmployeeByDepartmentName() {
        
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> criteria = cb.createQuery(Employee.class);
        Root<Employee> e = criteria.from(Employee.class);
        Join<Employee, Department> d = e.join(Employee_.department);
        criteria.where(
                cb.equal(
                        d.get(Department_.name), 
                        cb.parameter(String.class, "departmentName")
                )
        );
        criteria.select(e);
        XTypedQuery<Employee> typedQuery = 
                this
                .entityManager
                .createQuery(criteria)
                .setParameter("departmentName", "market");
        List<Employee> employees;
        
        employees =
                typedQuery
                .getResultList();
        Assert.assertFalse(employees.isEmpty());
        for (Employee employee : employees) {
            Assert.assertEquals("market", employee.getDepartment().getName());
            Assert.assertFalse(Hibernate.isInitialized(employee.getAnnualLeaves()));
        }
        
        employees =
                typedQuery
                .setQueryPaths(Employee__.begin().annualLeaves().end())
                .getResultList();
        Assert.assertFalse(employees.isEmpty());
        for (Employee employee : employees) {
            Assert.assertEquals("market", employee.getDepartment().getName());
            Assert.assertTrue(Hibernate.isInitialized(employee.getAnnualLeaves()));
            Assert.assertFalse(employee.getAnnualLeaves().isEmpty());
        }
    }
    
    @Test
    public void testTupleQueryPath() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        Root<Employee> me = cq.from(Employee.class).alias("m");
        Root<Employee> ee = cq.from(Employee.class).alias("e");
        cq
        .where(
                cb.equal(me, ee.get(Employee_.supervisor)),
                cb.equal(ee.get(Employee_.name), new Name("kate", "hill"))
        )
        .multiselect(me, ee);
        XTypedQuery<Tuple> typedQuery = 
                this
                .entityManager
                .createQuery(cq)
                .setQueryPaths("m", Employee__.begin().department().end(), Employee__.begin().annualLeaves().end())
                .setQueryPaths("e", Employee__.begin().department().end(), Employee__.begin().annualLeaves().end());
        List<Tuple> tuples = typedQuery.getResultList();
        Assert.assertEquals(4, tuples.size());
        for (Tuple tuple : tuples) {
            Employee m = (Employee)tuple.get("m");
            Employee e = (Employee)tuple.get("e");
            Assert.assertFalse(m.getId().equals(e.getId()));
            Assert.assertTrue(m.getId().equals(e.getSupervisor().getId()));
            Assert.assertTrue(Hibernate.isInitialized(m.getDepartment()));
            Assert.assertTrue(Hibernate.isInitialized(m.getAnnualLeaves()));
            Assert.assertTrue(Hibernate.isInitialized(e.getDepartment()));
            Assert.assertTrue(Hibernate.isInitialized(e.getAnnualLeaves()));
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDistinctLimitFailed() {
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        cq.from(Department.class);
        this
        .entityManager
        .createQuery(cq)
        .setQueryPaths(Department__.begin().employees().end())
        .setFirstResult(1)
        .setMaxResults(1)
        .getResultList();
    }
    
    @Test
    public void testMoveComplexOrderBy() {
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> employee = cq.from(Employee.class);
        cq.orderBy(
                cb.asc(employee.get(Employee_.id)),
                cb.asc(employee.get(Employee_.name).get(Name_.firstName))
        );
        List<Employee> employees =
                this
                .entityManager
                .createQuery(cq)
                .setQueryPaths(Employee__.preOrderBy().name().firstName().asc())
                .getResultList();
        Assert.assertEquals(6, employees.size());
        Assert.assertEquals(new Name("adam", "brook"), employees.get(0).getName());
        Assert.assertEquals(new Name("jim", "cotton"), employees.get(1).getName());
        Assert.assertEquals(new Name("kate", "hill"), employees.get(2).getName());
        Assert.assertEquals(new Name("linda", "sharp"), employees.get(3).getName());
        Assert.assertEquals(new Name("sam", "carter"), employees.get(4).getName());
        Assert.assertEquals(new Name("tom", "george"), employees.get(5).getName());
    }
    
    @Test
    public void testBlobNotLoaded() {
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Department> cq = cb.createQuery(Department.class);
        Root<Department> department = cq.from(Department.class);
        cq.where(cb.equal(department.get(Department_.id), 1L));
        Department market = this.entityManager.createQuery(cq).getSingleResult();
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(market, "image"));
    }
    
    @Test
    public void testBlobLoaded() {
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Department> cq = cb.createQuery(Department.class);
        Root<Department> department = cq.from(Department.class);
        cq.where(cb.equal(department.get(Department_.id), 1L));
        Department market = 
                this
                .entityManager
                .createQuery(cq)
                .setQueryPaths(Department__.begin().image().end())
                .getSingleResult();
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
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Department> cq = cb.createQuery(Department.class);
        Root<Department> department = cq.from(Department.class);
        cq.select(department);
        List<Department> departments = 
                this
                .entityManager
                .createQuery(cq)
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
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Department> cq = cb.createQuery(Department.class);
        cq.from(Department.class);
        List<Department> departments = 
                this
                .entityManager
                .createQuery(cq)
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
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Department> cq = cb.createQuery(Department.class);
        Root<Department> department = cq.from(Department.class);
        cq.select(department);
        List<Department> departments = 
                this
                .entityManager
                .createQuery(cq)
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
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Department> cq = cb.createQuery(Department.class);
        cq.from(Department.class);
        List<Department> departments = 
                this
                .entityManager
                .createQuery(cq)
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
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Department> cq = cb.createQuery(Department.class);
        Root<Department> department = cq.from(Department.class);
        cq.select(department);
        List<Department> departments = 
                this
                .entityManager
                .createQuery(cq)
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
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Department> cq = cb.createQuery(Department.class);
        cq.from(Department.class);
        List<Department> departments = 
                this
                .entityManager
                .createQuery(cq)
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
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        Root<Department> department = cq.from(Department.class).alias("d");
        Root<Employee> employee = cq.from(Employee.class).alias("e");
        cq
        .where(cb.equal(employee.get(Employee_.department), department))
        .select(cb.tuple(department, employee));
        List<Tuple> tuples = 
                this
                .entityManager
                .createQuery(cq)
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
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        Root<Department> department = cq.from(Department.class).alias("d");
        Root<Employee> employee = cq.from(Employee.class).alias("e");
        cq.where(cb.equal(employee.get(Employee_.department), department));
        List<Tuple> tuples = 
                this
                .entityManager
                .createQuery(cq)
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
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        XRoot<Department> department = cq.from(Department.class).alias("d");
        Join<Department, Employee> employee = department.join(Department_.employees).alias("e");
        cq.select(cb.tuple(department, employee));
        List<Tuple> tuples = 
                this
                .entityManager
                // Look carefully, this is "join", not "join fetch"!
                .createQuery(cq)
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
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        cq
        .from(Department.class).alias("d")
        .join(Department_.employees, JoinMode.REQUIRED_TO_MERGE_EXISTS)
        .alias("e");
        List<Tuple> tuples = 
                this
                .entityManager
                // Look carefully, this is "join", not "join fetch"!
                .createQuery(cq)
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
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        Root<Department> department = cq.from(Department.class).alias("d");
        Root<Employee> employee = cq.from(Employee.class).alias("e");
        cq
        .where(cb.equal(employee.get(Employee_.department), department))
        .select(cb.tuple(department, employee));
        List<Tuple> tuples = 
                this
                .entityManager
                .createQuery(cq)
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
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        Root<Department> department = cq.from(Department.class).alias("d");
        Root<Employee> employee = cq.from(Employee.class).alias("e");
        cq.where(cb.equal(employee.get(Employee_.department), department));
        List<Tuple> tuples = 
                this
                .entityManager
                .createQuery(cq)
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
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        XRoot<Department> department = cq.from(Department.class).alias("d");
        Join<Department, Employee> employee = department.join(Department_.employees).alias("e");
        cq.select(cb.tuple(department, employee));
        List<Tuple> tuples = 
                this
                .entityManager
                // Look carefully, this is "join", not "join fetch"!
                .createQuery(cq)
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
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        cq
        .from(Department.class).alias("d")
        .join(Department_.employees, JoinMode.REQUIRED_TO_MERGE_EXISTS)
        .alias("e");
        List<Tuple> tuples = 
                this
                .entityManager
                // Look carefully, this is "join", not "join fetch"!
                .createQuery(cq)
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
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        Root<Department> department = cq.from(Department.class).alias("d");
        Root<Employee> employee = cq.from(Employee.class).alias("e");
        cq
        .where(cb.equal(employee.get(Employee_.department), department))
        .select(cb.tuple(department, employee));
        List<Tuple> tuples = 
                this
                .entityManager
                .createQuery(cq)
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
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        Root<Department> department = cq.from(Department.class).alias("d");
        Root<Employee> employee = cq.from(Employee.class).alias("e");
        cq.where(cb.equal(employee.get(Employee_.department), department));
        List<Tuple> tuples = 
                this
                .entityManager
                .createQuery(cq)
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
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        XRoot<Department> department = cq.from(Department.class).alias("d");
        Join<Department, Employee> employee = department.join(Department_.employees).alias("e");
        cq.select(cb.tuple(department, employee));
        List<Tuple> tuples = 
                this
                .entityManager
                // Look carefully, this is "join", not "join fetch"!
                .createQuery(cq)
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
        XCriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        cq
        .from(Department.class).alias("d")
        .join(Department_.employees, JoinMode.REQUIRED_TO_MERGE_EXISTS)
        .alias("e");
        List<Tuple> tuples = 
                this
                .entityManager
                // Look carefully, this is "join", not "join fetch"!
                .createQuery(cq)
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
        Assert.assertEquals(new Name("sam", "carter"), sam.getName());
        Assert.assertEquals(new Name("tom", "george"), tom.getName());
        Assert.assertEquals(new Name("linda", "sharp"), linda.getName());
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

    private void assertLoadBlobViaTuples(List<Tuple> tuples, boolean departmentImageLoaded, boolean employeeImageLoaded) {
        
        Assert.assertEquals(6, tuples.size());
        Assert.assertSame(tuples.get(0).get(0), tuples.get(1).get(0));
        Assert.assertSame(tuples.get(0).get(0), tuples.get(2).get(0));
        Assert.assertSame(tuples.get(3).get(0), tuples.get(4).get(0));
        Assert.assertSame(tuples.get(3).get(0), tuples.get(5).get(0));
        
        Department market = (Department)tuples.get(0).get(0);
        Department development = (Department)tuples.get(3).get(0);
        Employee jim = (Employee)tuples.get(0).get(1);
        Employee kate = (Employee)tuples.get(1).get(1);
        Employee sam = (Employee)tuples.get(2).get(1);
        Employee tom = (Employee)tuples.get(3).get(1);
        Employee linda = (Employee)tuples.get(4).get(1);
        Employee adam = (Employee)tuples.get(5).get(1);
        
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
        Assert.assertEquals(new Name("sam", "carter"), sam.getName());
        Assert.assertEquals(new Name("tom", "george"), tom.getName());
        Assert.assertEquals(new Name("linda", "sharp"), linda.getName());
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
    
    private void assertOrder(List<Department> departments, boolean departmentNameDesc, Boolean employeeNameDesc) {
        String prevDepartmentName = null;
        for (Department department : departments) {
            String departmentName = department.getName();
            if (departmentName != null) {
                if (prevDepartmentName != null) {
                    if (departmentNameDesc) {
                        Assert.assertTrue(prevDepartmentName.compareTo(departmentName) >= 0);
                    } else {
                        Assert.assertTrue(prevDepartmentName.compareTo(departmentName) <= 0);
                    }
                }
                prevDepartmentName = departmentName;
            }
            if (employeeNameDesc != null) {
                Name prevEmployeeName = null;
                for (Employee employee : department.getEmployees()) {
                    Name employeeName = employee.getName();
                    if (employeeName != null) {
                        if (prevEmployeeName != null) {
                            if (employeeNameDesc.booleanValue()) {
                                Assert.assertTrue(prevEmployeeName.compareTo(employeeName) >= 0);
                            } else {
                                Assert.assertTrue(prevEmployeeName.compareTo(employeeName) <= 0);
                            }
                        }
                        prevEmployeeName = employeeName;
                    }
                }
            }
        }
    }
    
    private static Date date(String date) {
        try {
            return DATE_FORMAT.parse(date);
        } catch (ParseException ex) {
            throw new IllegalArgumentException();
        }
    }
}
