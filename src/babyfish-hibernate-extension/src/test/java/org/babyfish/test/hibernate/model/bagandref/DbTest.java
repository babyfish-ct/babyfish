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
package org.babyfish.test.hibernate.model.bagandref;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

import org.babyfish.collection.HashSet;
import org.babyfish.test.hibernate.model.AbstractHibernateTest;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class DbTest extends AbstractHibernateTest {
    
    @BeforeClass
    public static void initClass() {
        initSessionFactory(Department.class, Employee.class);
    }

    @Before
    public void initDb() {
        Consumer<Session> handler =
            session -> {
                session.createSQLQuery("DELETE FROM br_EMPLOYEE").executeUpdate();
                session.createSQLQuery("DELETE FROM br_DEPARTMENT").executeUpdate();
                session
                .createSQLQuery("INSERT INTO br_DEPARTMENT(DEPARTMENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 1L)
                .setString(1, "department1")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO br_DEPARTMENT(DEPARTMENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 2L)
                .setString(1, "department2")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO br_EMPLOYEE(EMPLOYEE_ID, NAME, DEPARTMENT_ID) VALUES(?, ?, NULL)")
                .setLong(0, 1L)
                .setString(1, "employee")
                .executeUpdate();
            };
        execute(handler);
    }
    
    @Test
    public void testModifyDepartment() {
        
        Consumer<Session> handler;
        
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null);
        
        handler =
            session -> {
                Department department1 = (Department)session.get(Department.class, 1L);
                Employee employee = (Employee)session.get(Employee.class, 1L);
                department1.getEmployees().add(employee);
            };
        execute(handler);
        assertEmployeeNames(1L, "employee");
        assertEmployeeNames(2L);
        assertDepartmentName(1L, "department1");
        
        handler =
            session -> {
                Department department2 = (Department)session.get(Department.class, 2L);
                Employee employee = (Employee)session.get(Employee.class, 1L);
                department2.getEmployees().add(employee);
            };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L, "employee");
        assertDepartmentName(1L, "department2");
        
        handler =
            session -> {
                Department department2 = (Department)session.get(Department.class, 2L);
                Iterator<Employee> itr = department2.getEmployees().iterator();
                itr.next();
                itr.remove();
            };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null);
    }
    
    @Test
    public void testModifyEmployee() {
        
        Consumer<Session> handler;
        
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null);
        
        handler =
            session -> {
                Department department1 = (Department)session.get(Department.class, 1L);
                Employee employee = (Employee)session.get(Employee.class, 1L);
                employee.setDepartment(department1);
            };
        execute(handler);
        assertEmployeeNames(1L, "employee");
        assertEmployeeNames(2L);
        assertDepartmentName(1L, "department1");
        
        handler =
            session -> {
                Department department2 = (Department)session.get(Department.class, 2L);
                Employee employee = (Employee)session.get(Employee.class, 1L);
                employee.setDepartment(department2);
            };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L, "employee");
        assertDepartmentName(1L, "department2");
        
        handler =
            session -> {
                Employee employee = (Employee)session.get(Employee.class, 1L);
                employee.setDepartment(null);
            };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null);
    }
    
    private static void assertEmployeeNames(final long departmentId, final String ... employeeNames) {
        Consumer<Session> handler = session -> {
            Department department = (Department)session.get(Department.class, departmentId);
            Set<String> employeeNames1 = new HashSet<String>();
            for (Employee employee : department.getEmployees()) {
                employeeNames1.add(employee.getName());
            }
            Set<String> employeeNames2 = new HashSet<String>();
            for (String employeeName : employeeNames) {
                employeeNames2.add(employeeName);
            }
            Assert.assertEquals(employeeNames1, employeeNames2);
        };
        execute(handler);
    }
    
    private static void assertDepartmentName(final long employeeId, final String departmentName) {
        Consumer<Session> handler = session -> {
            Employee employee = (Employee)session.get(Employee.class, employeeId);
            if (departmentName == null) {
                Assert.assertNull(employee.getDepartment());    
            } else {
                Assert.assertNotNull(employee.getDepartment());
                Assert.assertEquals(departmentName, employee.getDepartment().getName());
            }
        };
        execute(handler);
    }
}
