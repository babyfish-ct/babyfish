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
package org.babyfish.test.hibernate.model.setandref;

import java.util.function.Consumer;

import org.babyfish.lang.Ref;
import org.babyfish.test.hibernate.model.AbstractHibernateTest;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class DummyModificationTest extends AbstractHibernateTest {

    @BeforeClass
    public static void initClass() {
        initSessionFactory(Department.class, Employee.class);
    }
    
    @Before
    public void initDb() {
        Consumer<Session> handler = session -> {
                session.createSQLQuery("DELETE FROM sr_EMPLOYEE").executeUpdate();
                session.createSQLQuery("DELETE FROM sr_DEPARTMENT").executeUpdate();
                session
                .createSQLQuery("INSERT INTO sr_DEPARTMENT(DEPARTMENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 1L)
                .setString(1, "department1")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO sr_DEPARTMENT(DEPARTMENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 2L)
                .setString(1, "department2")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO sr_EMPLOYEE(EMPLOYEE_ID, NAME, DEPARTMENT_ID) VALUES(?, ?, NULL)")
                .setLong(0, 1L)
                .setString(1, "employee")
                .executeUpdate();
            };
        execute(handler);
    }
    
    @Test
    public void testSuspendedAdding() {
        final Ref<Department> departmentRef1 = new Ref<>();
        final Ref<Department> departmentRef2 = new Ref<>();
        final Ref<Employee> employeeRef = new Ref<>();
        Consumer<Session> handler = session -> {
            employeeRef.set((Employee)session.get(Employee.class, 1L));
            departmentRef1.set((Department)session.get(Department.class, 1L));
            departmentRef2.set((Department)session.get(Department.class, 2L));
        };
        execute(handler);
        
        employeeRef.get().setDepartment(departmentRef1.get());
        Assert.assertFalse(Hibernate.isInitialized(departmentRef1.get().getEmployees()));
        Assert.assertFalse(Hibernate.isInitialized(departmentRef2.get().getEmployees()));
        Assert.assertTrue(departmentRef1.get().getEmployees().contains(employeeRef.get()));
        
        employeeRef.get().setDepartment(departmentRef2.get());
        Assert.assertFalse(Hibernate.isInitialized(departmentRef1.get().getEmployees()));
        Assert.assertFalse(Hibernate.isInitialized(departmentRef2.get().getEmployees()));
        Assert.assertFalse(departmentRef1.get().getEmployees().contains(employeeRef.get()));
        Assert.assertTrue(departmentRef2.get().getEmployees().contains(employeeRef.get()));
    }
    
    @Test
    public void testSuspendedRemoving() {
        Consumer<Session> handler = session -> {
            Department department = (Department)session.get(Department.class, 1L);
            Employee employee = (Employee)session.get(Employee.class, 1L);
            employee.setDepartment(department);
        };
        execute(handler);
        
        final Ref<Department> departmentRef1 = new Ref<>();
        final Ref<Department> departmentRef2 = new Ref<>();
        final Ref<Employee> employeeRef = new Ref<>();
        handler = session -> {
            departmentRef1.set((Department)session.get(Department.class, 1L));
            departmentRef2.set((Department)session.get(Department.class, 2L));
            employeeRef.set((Employee)session.get(Employee.class, 1L));
        };
        execute(handler);
        
        employeeRef.get().setDepartment(departmentRef2.get());
        Assert.assertFalse(Hibernate.isInitialized(departmentRef1.get().getEmployees()));
        Assert.assertFalse(Hibernate.isInitialized(departmentRef2.get().getEmployees()));
        Assert.assertFalse(departmentRef1.get().getEmployees().contains(employeeRef.get()));
        Assert.assertTrue(departmentRef2.get().getEmployees().contains(employeeRef.get()));
    }
}
