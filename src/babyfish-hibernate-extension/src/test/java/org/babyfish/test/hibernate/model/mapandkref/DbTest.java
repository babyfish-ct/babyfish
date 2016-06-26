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
package org.babyfish.test.hibernate.model.mapandkref;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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
        Consumer<Session> handler = session -> {
                session.createSQLQuery("DELETE FROM mkr_EMPLOYEE").executeUpdate();
                session.createSQLQuery("DELETE FROM mkr_DEPARTMENT").executeUpdate();
                session
                .createSQLQuery("INSERT INTO mkr_DEPARTMENT(DEPARTMENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 1L)
                .setString(1, "department1")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO mkr_DEPARTMENT(DEPARTMENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 2L)
                .setString(1, "department2")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO mkr_EMPLOYEE(EMPLOYEE_ID, NAME, DEPARTMENT_ID) VALUES(?, ?, NULL)")
                .setLong(0, 1L)
                .setString(1, "employee1")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO mkr_EMPLOYEE(EMPLOYEE_ID, NAME, DEPARTMENT_ID) VALUES(?, ?, NULL)")
                .setLong(0, 2L)
                .setString(1, "employee2")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO mkr_EMPLOYEE(EMPLOYEE_ID, NAME, DEPARTMENT_ID) VALUES(?, ?, NULL)")
                .setLong(0, 3L)
                .setString(1, "employee3")
                .executeUpdate();
            };
        execute(handler);
    }
    
    @Test
    public void testModifyDepartment() {
        
        Consumer<Session> handler;
        
        assertEmployeeCodeAndNames(1L);
        assertEmployeeCodeAndNames(2L);
        assertDepartmentName(1L, null, null);
        assertDepartmentName(2L, null, null);
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            department1.getEmployees().put("I", employee1);
        };
        execute(handler);
        assertEmployeeCodeAndNames(1L, "I", "employee1");
        assertEmployeeCodeAndNames(2L);
        assertDepartmentName(1L, "department1", "I");
        assertDepartmentName(2L, null, null);
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            department1.getEmployees().put("II", employee2);
        };
        execute(handler);
        assertEmployeeCodeAndNames(1L, "I", "employee1", "II", "employee2");
        assertEmployeeCodeAndNames(2L);
        assertDepartmentName(1L, "department1", "I");
        assertDepartmentName(2L, "department1", "II");
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            department2.getEmployees().put("III", employee1);
        };
        execute(handler);
        assertEmployeeCodeAndNames(1L, "II", "employee2");
        assertEmployeeCodeAndNames(2L, "III", "employee1");
        assertDepartmentName(1L, "department2", "III");
        assertDepartmentName(2L, "department1", "II");
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            department2.getEmployees().put("IV", employee2);
        };
        execute(handler);
        assertEmployeeCodeAndNames(1L);
        assertEmployeeCodeAndNames(2L, "III", "employee1", "IV", "employee2");
        assertDepartmentName(1L, "department2", "III");
        assertDepartmentName(2L, "department2", "IV");
        
        handler = session ->{
            Department department2 = (Department)session.load(Department.class, 2L);
            department2.getEmployees().keySet().remove("III");
        };
        execute(handler);
        assertEmployeeCodeAndNames(1L);
        assertEmployeeCodeAndNames(2L, "IV", "employee2");
        assertDepartmentName(1L, null, null);
        assertDepartmentName(2L, "department2", "IV");
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            department2.getEmployees().values().remove(employee2);
        };
        execute(handler);
        assertEmployeeCodeAndNames(1L);
        assertEmployeeCodeAndNames(2L);
        assertDepartmentName(1L, null, null);
        assertDepartmentName(2L, null, null);
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
                Assert.assertEquals(employeeCodeInDepartment, employee.getCodeInDepartment());
            }
        };
        execute(handler);
    }
    
    private static void assertEmployeeCodeAndNames(
            final long departmentId, final String ... codeAndNames) {
        Consumer<Session> handler = session -> {
            Department department = (Department)session.get(Department.class, departmentId);
            Map<String, String> map1 = new HashMap<String, String>();
            
            for (Employee employee : department.getEmployees().values()) {
                map1.put(employee.getCodeInDepartment(), employee.getName());
            }
            Map<String, String> map2 = new HashMap<String, String>();
            for (int i = 0; i < codeAndNames.length - 1; i += 2) {
                map2.put(codeAndNames[i], codeAndNames[i + 1]);
            }
            Assert.assertEquals(map2, map1);
        };
        execute(handler);
    }
}
