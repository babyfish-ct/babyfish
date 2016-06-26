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

import javax.persistence.Persistence;

import org.babyfish.hibernate.XQuery;
import org.babyfish.hibernate.XSession;
import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.cfg.Configuration;
import org.babyfish.model.jpa.path.CollectionFetchType;
import org.babyfish.model.jpa.path.GetterType;
import org.babyfish.model.jpa.path.QueryPaths;
import org.babyfish.persistence.QueryType;
import org.hibernate.Hibernate;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class HQLTest {

    private static final String RESOURCE =
            HQLTest.class.getPackage().getName().replace('.', '/') + "/hibernate.cfg.xml";
    
    private static final DateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd");
    
    private static XSessionFactory sessionFactory;
    
    private XSession session;
    
    @SuppressWarnings("deprecation")
    @BeforeClass
    public static void initSessionFactory() {
        sessionFactory =
            new Configuration()
            .configure(RESOURCE)
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
                session.save(market);
                session.save(development);
                session.save(jim);
                session.save(kate);
                session.save(sam);
                session.save(tom);
                session.save(linda);
                session.save(adam);
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
            } catch (RuntimeException | Error ex) {
                tx.rollback();
                throw ex;
            }
            tx.commit();
        } finally {
            session.close();
        }
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
    }
    
    @After
    public void disposeSession() {
        XSession s = this.session;
        if (s != null) {
            this.session = null;
            s.close();
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testHibernateJoin() {
        List<Object[]> items = this.session.createQuery("from Department d inner join d.employees").list();
        Assert.assertEquals(6, items.size());
        for (Object[] item : items) {
            Assert.assertTrue(item[0] instanceof Department);
            Assert.assertTrue(item[1] instanceof Employee);
        }
    }
    
    @Test
    public void testCollectionFetchByAllModeAndQueryPath() {
        this.testCollectionFetchByAllMode(
                query -> {
                    query.setQueryPaths(
                            QueryPaths.compile(
                                "this.employees", 
                                "this..employees.annualLeaves", 
                                "this.employees.supervisor.supervisor.supervisor.supervisor"
                            )
                    );
                },
                false, 
                null);
        this.testCollectionFetchByAllMode(
                query -> {
                    query.setQueryPaths(
                            QueryPaths.compile(
                                "this.employees",
                                "this..employees.annualLeaves",
                                "this.employees.supervisor.supervisor.supervisor.supervisor",
                                "pre order by this.name asc",
                                "post order by this.employees.name.firstName asc"
                            )
                    );
                },
                false, 
                false);
        this.testCollectionFetchByAllMode(
                query -> {
                    query.setQueryPaths(
                            QueryPaths.compile(
                                "this.employees",
                                "this..employees.annualLeaves",
                                "this.employees.supervisor.supervisor.supervisor.supervisor",
                                "pre order by this.name asc",
                                "post order by this.employees.name.firstName desc"
                            )
                    );
                },
                false, 
                true);
        this.testCollectionFetchByAllMode(
                query -> {
                    query.setQueryPaths(
                            QueryPaths.compile(
                                "this.employees",
                                "this..employees.annualLeaves",
                                "this.employees.supervisor.supervisor.supervisor.supervisor",
                                "pre order by this.name desc",
                                "post order by this.employees.name.firstName asc"
                            )
                    );
                },
                true, 
                false);
        this.testCollectionFetchByAllMode(
                query -> {
                    query.setQueryPaths(
                            QueryPaths.compile(
                                "this.employees",
                                "this..employees.annualLeaves",
                                "this.employees.supervisor.supervisor.supervisor.supervisor",
                                "pre order by this.name desc",
                                "post order by this.employees.name.firstName desc"
                            )
                    );
                },
                true, 
                true);
    }
    
    @Test
    public void testCollectionFetchByAllModeAndTeypedQueryPath() {
        this.testCollectionFetchByAllMode(
                query-> {
                    query.setQueryPaths(
                            Department__.begin().employees().end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end()
                    );
                },
                false, 
                null);
        this.testCollectionFetchByAllMode(
                query -> {
                    query.setQueryPaths(
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
                query -> {
                    query.setQueryPaths(
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
                query -> {
                    query.setQueryPaths(
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
                query -> {
                    query.setQueryPaths(
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
    
    @SuppressWarnings("unchecked")
    private void testCollectionFetchByAllMode(Consumer<XQuery> addQueryPathLambda, boolean departmentNameDesc, Boolean employeeNameDesc) {
        XQuery query;
        List<Department> departments;
        
        this.session.clear();
        query = 
                this
                .session
                .createQuery(
                        "select d " +
                        "from Department d " +
                        "left join d.employees e " +
                        "where e.name.firstName like :name " +
                        "order by d.name asc")
                .setString("name", "%m%");
        Assert.assertEquals(4, query.setQueryType(QueryType.RESULT).unlimitedCount());
        Assert.assertEquals(2, query.setQueryType(QueryType.DISTINCT).unlimitedCount());
        departments = query.list();
        Assert.assertFalse(departments.isEmpty());
        for (Department department : departments) {
            Assert.assertFalse(Hibernate.isInitialized(department.getEmployees()));
        }
        
        addQueryPathLambda.accept(query);
        
        this.session.clear();
        Assert.assertEquals(24, query.setQueryType(QueryType.RESULT).unlimitedCount());
        departments = query.list();
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
        
        this.session.clear();
        Assert.assertEquals(2, query.setQueryType(QueryType.DISTINCT).unlimitedCount());
        departments = query.list();
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
                query -> {
                    query.setQueryPaths(
                            QueryPaths.compile(
                                    "this.partial(employees)",
                                    "this..employees.annualLeaves",
                                    "this.employees.supervisor.supervisor.supervisor.supervisor"
                            )
                    );
                }, 
                false, 
                null);
        this.testCollectionFetchByPartialMode(
                query -> {
                    query.setQueryPaths(
                            QueryPaths.compile(
                                    "this.partial(employees)",
                                    "this..employees.annualLeaves",
                                    "this.employees.supervisor.supervisor.supervisor.supervisor",
                                    "pre order by this.name asc",
                                    "post order by this.employees.name.firstName asc"
                            )
                    );
                }, 
                false, 
                false);
        this.testCollectionFetchByPartialMode(
                query -> {
                    query.setQueryPaths(
                            QueryPaths.compile(
                                    "this.partial(employees)",
                                    "this..employees.annualLeaves",
                                    "this.employees.supervisor.supervisor.supervisor.supervisor",
                                    "pre order by this.name asc",
                                    "post order by this.employees.name.firstName desc"
                            )
                    );
                }, 
                false, 
                true);
        this.testCollectionFetchByPartialMode(
                query -> {
                    query.setQueryPaths(
                            QueryPaths.compile(
                                "this.partial(employees)",
                                "this..employees.annualLeaves",
                                "this.employees.supervisor.supervisor.supervisor.supervisor",
                                "pre order by this.name desc",
                                "post order by this.employees.name.firstName asc"
                            )
                    );
                }, 
                true, 
                false);
        this.testCollectionFetchByPartialMode(
                query -> {
                    query.setQueryPaths(
                            QueryPaths.compile(
                                "this.partial(employees)",
                                "this..employees.annualLeaves",
                                "this.employees.supervisor.supervisor.supervisor.supervisor",
                                "pre order by this.name desc",
                                "post order by this.employees.name.firstName desc"
                            )
                    );
                }, 
                true, 
                true);
    }
    
    @Test
    public void testCollectionFetchByPartialModeAndTypedQueryPath() {
        this.testCollectionFetchByPartialMode(
                query -> {
                    query.setQueryPaths(
                            Department__.begin().employees(CollectionFetchType.PARTIAL).end(),
                            Department__.begin().employees(GetterType.REQUIRED).annualLeaves().end(),
                            Department__.begin().employees().supervisor().supervisor().supervisor().supervisor().end()
                    );
                }, 
                false, 
                null);
        this.testCollectionFetchByPartialMode(
                query -> {
                    query.setQueryPaths(
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
                query -> {
                    query.setQueryPaths(
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
                query -> {
                    query.setQueryPaths(
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
                query -> {
                    query.setQueryPaths(
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
    
    @SuppressWarnings("unchecked")
    private void testCollectionFetchByPartialMode(Consumer<XQuery> addQueryPathLambda, boolean departmentNameDesc, Boolean employeeNameDesc) {
        List<Department> departments;
        XQuery query;
        
        this.session.clear();
        query = 
                this
                .session
                .createQuery("select d from Department d left join d.employees e where e.name.firstName like :name order by d.name asc")
                .setString("name", "%m%");
        
        Assert.assertEquals(4, query.setQueryType(QueryType.RESULT).unlimitedCount());
        Assert.assertEquals(2, query.setQueryType(QueryType.DISTINCT).unlimitedCount());
        departments = query.list();
        Assert.assertFalse(departments.isEmpty());
        for (Department department : departments) {
            Assert.assertFalse(Hibernate.isInitialized(department.getEmployees()));
        }
        
        addQueryPathLambda.accept(query);
        
        this.session.clear();
        Assert.assertEquals(8, query.setQueryType(QueryType.RESULT).unlimitedCount());
        departments = query.list();
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
        
        this.session.clear();
        Assert.assertEquals(2, query.setQueryType(QueryType.DISTINCT).unlimitedCount());
        departments = query.list();
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
        long count =
                this
                .session
                .createQuery("select d from Department d left join d.employees e")
                .setQueryPaths(Department__.begin().employees(CollectionFetchType.PARTIAL).end())
                .setQueryType(QueryType.RESULT)
                .unlimitedCount();
        Assert.assertEquals(6, count);
    }
    
    @Test
    public void testMergetPartialFetchToNotSharedJoinFailed() {
        long count =
                this
                .session
                .createQuery("select d from Department d left join d.employees babyfish_not_shared_alias_e")
                .setQueryPaths(Department__.begin().employees(CollectionFetchType.PARTIAL).end())
                .setQueryType(QueryType.RESULT)
                .unlimitedCount();
        Assert.assertEquals(18, count);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testTupleQueryPath() {
        XQuery query = 
                this
                .session
                .createQuery("select m, e from Employee m, Employee e where e.name = :name and m = e.supervisor")
                .setParameter("name", new Name("kate", "hill"))
                .setQueryPaths("m", Employee__.begin().department().end(), Employee__.begin().annualLeaves().end())
                .setQueryPaths("e", Employee__.begin().department().end(), Employee__.begin().annualLeaves().end());
        List<Object[]> tuples = query.list();
        Assert.assertEquals(4, tuples.size());
        for (Object[] tuple : tuples) {
            Employee m = (Employee)tuple[0];
            Employee e = (Employee)tuple[1];
            Assert.assertFalse(m.getId().equals(e.getId()));
            Assert.assertTrue(m.getId().equals(e.getSupervisor().getId()));
            Assert.assertTrue(Hibernate.isInitialized(m.getDepartment()));
            Assert.assertTrue(Hibernate.isInitialized(m.getAnnualLeaves()));
            Assert.assertTrue(Hibernate.isInitialized(e.getDepartment()));
            Assert.assertTrue(Hibernate.isInitialized(e.getAnnualLeaves()));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testNamedQueryWithNamedParameter() {
        XQuery query = 
                this
                .session
                .getNamedQuery("getEmployeesByDepartmentName1")
                .setString("departmentName", "market");
        List<Employee> employees;
        
        employees = query.list();
        Assert.assertFalse(employees.isEmpty());
        for (Employee employee : employees) {
            Assert.assertEquals("market", employee.getDepartment().getName());
            Assert.assertFalse(Hibernate.isInitialized(employee.getAnnualLeaves()));
        }
        
        employees =
                query
                .setQueryPaths(Employee__.begin().annualLeaves().end())
                .list();
        Assert.assertFalse(employees.isEmpty());
        for (Employee employee : employees) {
            Assert.assertEquals("market", employee.getDepartment().getName());
            Assert.assertTrue(Hibernate.isInitialized(employee.getAnnualLeaves()));
            Assert.assertFalse(employee.getAnnualLeaves().isEmpty());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testNamedQueryWithIndexedParameter() {
        
        XQuery query = 
                this
                .session
                .getNamedQuery("getEmployeesByDepartmentName2")
                .setString(0, "market");
        List<Employee> employees;
        
        employees = query.list();
        Assert.assertFalse(employees.isEmpty());
        for (Employee employee : employees) {
            Assert.assertEquals("market", employee.getDepartment().getName());
            Assert.assertFalse(Hibernate.isInitialized(employee.getAnnualLeaves()));
        }
        
        employees =
                query
                .setQueryPaths(Employee__.begin().annualLeaves().end())
                .list();
        Assert.assertFalse(employees.isEmpty());
        for (Employee employee : employees) {
            Assert.assertEquals("market", employee.getDepartment().getName());
            Assert.assertTrue(Hibernate.isInitialized(employee.getAnnualLeaves()));
            Assert.assertFalse(employee.getAnnualLeaves().isEmpty());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testMergetFetchFailedOnWithCaluseJoin() {
        List<Object[]> tuples = 
                this
                .session
                .createQuery("select d, e from Department d left join d.employees e on e.name.firstName like :employeeName")
                .setString("employeeName", "%a%")
                .setQueryPaths(Department__.begin().employees(CollectionFetchType.PARTIAL).end())
                .list();
        Assert.assertEquals(12, tuples.size());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testMergeJoinSuccessfulOnWithClauseJoin() {
        List<Object[]> tuples = 
                this
                .session
                .createQuery("select d, e from Department d left join d.employees e on e.name.firstName like :employeeName")
                .setString("employeeName", "%a%")
                .setQueryPaths(Department__.preOrderBy().employees().name().asc())
                .list();
        Assert.assertEquals(4, tuples.size());
        Assert.assertEquals("development", ((Department)tuples.get(0)[0]).getName());
        Assert.assertEquals(new Name("adam", "brook"), ((Employee)tuples.get(0)[1]).getName());
        Assert.assertEquals("market", ((Department)tuples.get(1)[0]).getName());
        Assert.assertEquals(new Name("kate", "hill"), ((Employee)tuples.get(1)[1]).getName());
        Assert.assertEquals("development", ((Department)tuples.get(2)[0]).getName());
        Assert.assertEquals(new Name("linda", "sharp"), ((Employee)tuples.get(2)[1]).getName());
        Assert.assertEquals("market", ((Department)tuples.get(3)[0]).getName());
        Assert.assertEquals(new Name("sam", "carter"), ((Employee)tuples.get(3)[1]).getName());
    }
    
    @Test(expected = QueryException.class)
    public void testDistinctLimitFailed() {
        this
        .session
        .createQuery("from Department")
        .setQueryPaths(Department__.begin().employees().end())
        .setFirstResult(1)
        .setMaxResults(1)
        .list();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testMoveComplexOrderBy() {
        List<Employee> employees =
                this
                .session
                .createQuery("select e from Employee e order by e.id asc, e.name.firstName asc")
                .setQueryPaths(Employee__.preOrderBy().name().firstName().asc())
                .list();
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
        Department market = (Department)this.session.get(Department.class, 1L);
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(market, "image"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBlobLoaded() {
        Department market = this.session.get(
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
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadDepartmentBlobWithSelection() {
        
        List<Department> departments = 
                this
                .session
                .createQuery("select d from Department d")
                .setQueryPaths(
                        Department__.begin().image().end(),
                        Department__.begin().employees().end(),
                        Department__.preOrderBy().id().asc(),
                        Department__.preOrderBy().employees().id().asc()
                )
                .list();
        this.assertLoadBlob(departments, true, false);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadDepartmentBlobWithoutSelection() {
        
        List<Department> departments = 
                this
                .session
                .createQuery("from Department")
                .setQueryPaths(
                        Department__.begin().image().end(),
                        Department__.begin().employees().end(),
                        Department__.preOrderBy().id().asc(),
                        Department__.preOrderBy().employees().id().asc()
                )
                .list();
        this.assertLoadBlob(departments, true, false);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadEmployeeBlobWithSelection() {
        
        List<Department> departments = 
                this
                .session
                .createQuery("select d from Department d")
                .setQueryPaths(
                        Department__.begin().employees().image().end(),
                        Department__.preOrderBy().id().asc(),
                        Department__.preOrderBy().employees().id().asc()
                )
                .list();
        this.assertLoadBlob(departments, false, true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadEmployeeBlobWithoutSelection() {
        
        List<Department> departments = 
                this
                .session
                .createQuery("from Department")
                .setQueryPaths(
                        Department__.begin().employees().image().end(),
                        Department__.preOrderBy().id().asc(),
                        Department__.preOrderBy().employees().id().asc()
                )
                .list();
        this.assertLoadBlob(departments, false, true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadAllBlobsWithSelection() {
        
        List<Department> departments = 
                this
                .session
                .createQuery("select d from Department d")
                .setQueryPaths(
                        Department__.begin().image().end(),
                        Department__.begin().employees().image().end(),
                        Department__.preOrderBy().id().asc(),
                        Department__.preOrderBy().employees().id().asc()
                )
                .list();
        this.assertLoadBlob(departments, true, true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadAllBlobsWithoutSelection() {
        
        List<Department> departments = 
                this
                .session
                .createQuery("from Department")
                .setQueryPaths(
                        Department__.begin().image().end(),
                        Department__.begin().employees().image().end(),
                        Department__.preOrderBy().id().asc(),
                        Department__.preOrderBy().employees().id().asc()
                )
                .list();
        this.assertLoadBlob(departments, true, true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadDepartmentBlobViaTuplesByMode1() {
        List<Object[]> tuples = 
                this
                .session
                .createQuery("select d, e from Department d, Employee e where d = e.department")
                .setQueryPaths(
                        "d", 
                        Department__.begin().image().end(),
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.preOrderBy().id().asc()
                )
                .list();
        this.assertLoadBlobViaTuples(tuples, true, false);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadDepartmentBlobViaTuplesByMode2() {
        List<Object[]> tuples = 
                this
                .session
                .createQuery("from Department d, Employee e where d = e.department")
                .setQueryPaths(
                        "d", 
                        Department__.begin().image().end(),
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.preOrderBy().id().asc()
                )
                .list();
        this.assertLoadBlobViaTuples(tuples, true, false);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadDepartmentBlobViaTuplesByMode3() {
        List<Object[]> tuples = 
                this
                .session
                // Look carefully, this is "join", not "join fetch"!
                .createQuery("select d, e from Department d join d.employees e")
                .setQueryPaths(
                        "d", 
                        Department__.begin().image().end(),
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.preOrderBy().id().asc()
                )
                .list();
        this.assertLoadBlobViaTuples(tuples, true, false);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadDepartmentBlobViaTuplesByMode4() {
        List<Object[]> tuples = 
                this
                .session
                // Look carefully, this is "join", not "join fetch"!
                .createQuery("from Department d join d.employees e")
                .setQueryPaths(
                        "d", 
                        Department__.begin().image().end(),
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.preOrderBy().id().asc()
                )
                .list();
        this.assertLoadBlobViaTuples(tuples, true, false);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadEmployeeBlobViaTuplesByMode1() {
        List<Object[]> tuples = 
                this
                .session
                .createQuery("select d, e from Department d, Employee e where d = e.department")
                .setQueryPaths(
                        "d", 
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.begin().image().end(),
                        Employee__.preOrderBy().id().asc()
                )
                .list();
        this.assertLoadBlobViaTuples(tuples, false, true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadEmployeeBlobViaTuplesByMode2() {
        List<Object[]> tuples = 
                this
                .session
                .createQuery("from Department d, Employee e where d = e.department")
                .setQueryPaths(
                        "d", 
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.begin().image().end(),
                        Employee__.preOrderBy().id().asc()
                )
                .list();
        this.assertLoadBlobViaTuples(tuples, false, true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadEmployeeBlobViaTuplesByMode3() {
        List<Object[]> tuples = 
                this
                .session
                // Look carefully, this is "join", not "join fetch"!
                .createQuery("select d, e from Department d join d.employees e")
                .setQueryPaths(
                        "d", 
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.begin().image().end(),
                        Employee__.preOrderBy().id().asc()
                )
                .list();
        this.assertLoadBlobViaTuples(tuples, false, true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadEmployeeBlobViaTuplesByMode4() {
        List<Object[]> tuples = 
                this
                .session
                // Look carefully, this is "join", not "join fetch"!
                .createQuery("from Department d join d.employees e")
                .setQueryPaths(
                        "d", 
                        Department__.preOrderBy().id().asc()
                )
                .setQueryPaths(
                        "e",
                        Employee__.begin().image().end(),
                        Employee__.preOrderBy().id().asc()
                )
                .list();
        this.assertLoadBlobViaTuples(tuples, false, true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadAllBlobsViaTuplesByMode1() {
        List<Object[]> tuples = 
                this
                .session
                .createQuery("select d, e from Department d, Employee e where d = e.department")
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
                .list();
        this.assertLoadBlobViaTuples(tuples, true, true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadAllBlobsViaTuplesByMode2() {
        List<Object[]> tuples = 
                this
                .session
                .createQuery("from Department d, Employee e where d = e.department")
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
                .list();
        this.assertLoadBlobViaTuples(tuples, true, true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadAllBlobsViaTuplesByMode3() {
        List<Object[]> tuples = 
                this
                .session
                // Look carefully, this is "join", not "join fetch"!
                .createQuery("select d, e from Department d join d.employees e")
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
                .list();
        this.assertLoadBlobViaTuples(tuples, true, true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadAllBlobsViaTuplesByMode4() {
        List<Object[]> tuples = 
                this
                .session
                // Look carefully, this is "join", not "join fetch"!
                .createQuery("from Department d join d.employees e")
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
                .list();
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

