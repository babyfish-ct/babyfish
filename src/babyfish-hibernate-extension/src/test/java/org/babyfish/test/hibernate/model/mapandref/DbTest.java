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
package org.babyfish.test.hibernate.model.mapandref;

import java.util.Set;
import java.util.function.Consumer;

import org.babyfish.collection.HashSet;
import org.babyfish.test.hibernate.model.AbstractHibernateTest;
import org.hibernate.Hibernate;
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
        Consumer<Session> handler = session -> {
                session.createSQLQuery("DELETE FROM mr_DEPARTMENT_EMPLOYEE").executeUpdate();
                session.createSQLQuery("DELETE FROM mr_EMPLOYEE").executeUpdate();
                session.createSQLQuery("DELETE FROM mr_DEPARTMENT").executeUpdate();
                session
                .createSQLQuery("INSERT INTO mr_DEPARTMENT(DEPARTMENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 1L)
                .setString(1, "department1")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO mr_DEPARTMENT(DEPARTMENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 2L)
                .setString(1, "department2")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO mr_EMPLOYEE(EMPLOYEE_ID, NAME) VALUES(?, ?)")
                .setLong(0, 1L)
                .setString(1, "employee1")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO mr_EMPLOYEE(EMPLOYEE_ID, NAME) VALUES(?, ?)")
                .setLong(0, 2L)
                .setString(1, "employee2")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO mr_EMPLOYEE(EMPLOYEE_ID, NAME) VALUES(?, ?)")
                .setLong(0, 3L)
                .setString(1, "employee3")
                .executeUpdate();
            };
        execute(handler);
    }
    
    @Test
    public void testModifyDepartment() {
        
        Consumer<Session> handler;
        
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null, null);
        assertDepartmentName(2L, null, null);
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            department1.getEmployees().put("I", employee1);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee1");
        assertEmployeeNames(2L);
        assertDepartmentName(1L, "department1", "I");
        assertDepartmentName(2L, null, null);
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            department1.getEmployees().put("II", employee2);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee1", "employee2");
        assertEmployeeNames(2L);
        assertDepartmentName(1L, "department1", "I");
        assertDepartmentName(2L, "department1", "II");
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            department2.getEmployees().put("III", employee1);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2");
        assertEmployeeNames(2L, "employee1");
        assertDepartmentName(1L, "department2", "III");
        assertDepartmentName(2L, "department1", "II");
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            department2.getEmployees().put("IV", employee2);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L, "employee1", "employee2");
        assertDepartmentName(1L, "department2", "III");
        assertDepartmentName(2L, "department2", "IV");
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            department2.getEmployees().keySet().remove("III");
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L, "employee2");
        assertDepartmentName(1L, null, null);
        assertDepartmentName(2L, "department2", "IV");
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            department2.getEmployees().values().remove(employee2);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null, null);
        assertDepartmentName(2L, null, null);
    }
    
    @Test
    public void testVisionallyRead() {
        Consumer<Session> handler;
        
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null, null);
        assertDepartmentName(2L, null, null);
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            department1.getEmployees().put("I", employee1);
            
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            department2.getEmployees().put("II", employee2);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee1");
        assertEmployeeNames(2L, "employee2");
        assertDepartmentName(1L, "department1", "I");
        assertDepartmentName(2L, "department2", "II");
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Department department2 = (Department)session.load(Department.class, 2L);
            Assert.assertTrue(department1.getEmployees().containsKey("I"));
            Assert.assertTrue(department2.getEmployees().containsKey("II"));
            Assert.assertFalse(Hibernate.isInitialized(department1.getEmployees()));
            Assert.assertFalse(Hibernate.isInitialized(department2.getEmployees()));
        };
        execute(handler);
    }
    
    private static void assertDepartmentName(
            final long employeeId, 
            final String departmentName,
            final String employeeCodeInDepartment) {
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
    
    private static void assertEmployeeNames(
            final long departmentId, final String ... names) {
        Consumer<Session> handler = session -> {
            Department department = (Department)session.get(Department.class, departmentId);
            Set<String> set1 = new HashSet<>();
            for (Employee employee : department.getEmployees().values()) {
                set1.add(employee.getName());
            }
            Set<String> set2 = new HashSet<>();
            for (String name : names) {
                set2.add(name);
            }
            Assert.assertEquals(set2, set1);
        };
        execute(handler);
    }
}
