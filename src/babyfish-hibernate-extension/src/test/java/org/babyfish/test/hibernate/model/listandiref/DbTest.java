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
package org.babyfish.test.hibernate.model.listandiref;

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
                session.createSQLQuery("DELETE FROM lir_EMPLOYEE").executeUpdate();
                session.createSQLQuery("DELETE FROM lir_DEPARTMENT").executeUpdate();
                session
                .createSQLQuery("INSERT INTO lir_DEPARTMENT(DEPARTMENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 1L)
                .setString(1, "department1")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO lir_DEPARTMENT(DEPARTMENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 2L)
                .setString(1, "department2")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO lir_EMPLOYEE(EMPLOYEE_ID, NAME, DEPARTMENT_ID) VALUES(?, ?, NULL)")
                .setLong(0, 1L)
                .setString(1, "employee1")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO lir_EMPLOYEE(EMPLOYEE_ID, NAME, DEPARTMENT_ID) VALUES(?, ?, NULL)")
                .setLong(0, 2L)
                .setString(1, "employee2")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO lir_EMPLOYEE(EMPLOYEE_ID, NAME, DEPARTMENT_ID) VALUES(?, ?, NULL)")
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
        assertDepartmentName(1L, null, -1);
        assertDepartmentName(2L, null, -1);
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            department1.getEmployees().add(0, employee1);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee1");
        assertEmployeeNames(2L);
        assertDepartmentName(1L, "department1", 0);
        assertDepartmentName(2L, null, -1);
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            department1.getEmployees().add(0, employee2);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2", "employee1");
        assertEmployeeNames(2L);
        assertDepartmentName(1L, "department1", 1);
        assertDepartmentName(2L, "department1", 0);
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            department2.getEmployees().add(0, employee1);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2");
        assertEmployeeNames(2L, "employee1");
        assertDepartmentName(1L, "department2", 0);
        assertDepartmentName(2L, "department1", 0);
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            department2.getEmployees().add(0, employee2);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L, "employee2", "employee1");
        assertDepartmentName(1L, "department2", 1);
        assertDepartmentName(2L, "department2", 0);
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            department2.getEmployees().remove(0);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L, "employee1");
        assertDepartmentName(1L, "department2", 0);
        assertDepartmentName(2L, null, -1);
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            department2.getEmployees().remove(0);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null, -1);
        assertDepartmentName(2L, null, -1);
    }
    
    @Test
    public void testModifyIndexAndValueOfEmployee() {
        
        Consumer<Session> handler;
        
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null, -1);
        assertDepartmentName(2L, null, -1);
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            employee1.setIndexInDepartment(0);
            employee1.setDepartment(department1);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee1");
        assertEmployeeNames(2L);
        assertDepartmentName(1L, "department1", 0);
        assertDepartmentName(2L, null, -1);
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            employee2.setIndexInDepartment(0);
            employee2.setDepartment(department1);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2", "employee1");
        assertEmployeeNames(2L);
        assertDepartmentName(1L, "department1", 1);
        assertDepartmentName(2L, "department1", 0);
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            employee1.setIndexInDepartment(0);
            employee1.setDepartment(department2);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2");
        assertEmployeeNames(2L, "employee1");
        assertDepartmentName(1L, "department2", 0);
        assertDepartmentName(2L, "department1", 0);
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            employee1.setIndexInDepartment(0);
            employee1.setDepartment(department2);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2");
        assertEmployeeNames(2L, "employee1");
        assertDepartmentName(1L, "department2", 0);
        assertDepartmentName(2L, "department1", 0);
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            employee2.setIndexInDepartment(0);
            employee2.setDepartment(department2);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L, "employee2", "employee1");
        assertDepartmentName(1L, "department2", 1);
        assertDepartmentName(2L, "department2", 0);
        
        handler = session -> {
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            employee2.setIndexInDepartment(-1);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L, "employee1");
        assertDepartmentName(1L, "department2", 0);
        assertDepartmentName(2L, null, -1);
        
        handler = session -> {
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            employee1.setIndexInDepartment(-1);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null, -1);
        assertDepartmentName(2L, null, -1);
    }
    
    @Test
    public void testModifyValueAndIndexOfEmployee() {
        
        Consumer<Session> handler;
        
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null, -1);
        assertDepartmentName(2L, null, -1);
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            employee1.setDepartment(department1);
            employee1.setIndexInDepartment(0);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee1");
        assertEmployeeNames(2L);
        assertDepartmentName(1L, "department1", 0);
        assertDepartmentName(2L, null, -1);
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            employee2.setDepartment(department1);
            employee2.setIndexInDepartment(0);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2", "employee1");
        assertEmployeeNames(2L);
        assertDepartmentName(1L, "department1", 1);
        assertDepartmentName(2L, "department1", 0);
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            //Modify the index of employee1 to avoid IndexOutOfBoundsException
            employee1.setIndexInDepartment(-1);
            
            employee1.setDepartment(department2);
            employee1.setIndexInDepartment(0);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2");
        assertEmployeeNames(2L, "employee1");
        assertDepartmentName(1L, "department2", 0);
        assertDepartmentName(2L, "department1", 0);
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            employee2.setDepartment(department2);
            employee2.setIndexInDepartment(0);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L, "employee2", "employee1");
        assertDepartmentName(1L, "department2", 1);
        assertDepartmentName(2L, "department2", 0);
        
        handler = session -> {
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            employee2.setDepartment(null);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L, "employee1");
        assertDepartmentName(1L, "department2", 0);
        assertDepartmentName(2L, null, -1);
        
        handler = session -> {
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            employee1.setDepartment(null);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null, -1);
        assertDepartmentName(2L, null, -1);
    }
    
    @Test
    public void testModifyEmployee() {
        
        Consumer<Session> handler;
        
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null, -1);
        assertDepartmentName(2L, null, -1);
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            employee1.setDepartment(0, department1);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee1");
        assertEmployeeNames(2L);
        assertDepartmentName(1L, "department1", 0);
        assertDepartmentName(2L, null, -1);
        
        handler = session -> {
            Department department1 = (Department)session.load(Department.class, 1L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            employee2.setDepartment(0, department1);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2", "employee1");
        assertEmployeeNames(2L);
        assertDepartmentName(1L, "department1", 1);
        assertDepartmentName(2L, "department1", 0);
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            employee1.setDepartment(0, department2);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2");
        assertEmployeeNames(2L, "employee1");
        assertDepartmentName(1L, "department2", 0);
        assertDepartmentName(2L, "department1", 0);
        
        handler = session -> {
            Department department2 = (Department)session.load(Department.class, 2L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            employee2.setDepartment(0, department2);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L, "employee2", "employee1");
        assertDepartmentName(1L, "department2", 1);
        assertDepartmentName(2L, "department2", 0);
        
        handler = session -> {
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            employee2.setDepartment(-1, null);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L, "employee1");
        assertDepartmentName(1L, "department2", 0);
        assertDepartmentName(2L, null, -1);
        
        handler = session -> {
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            employee1.setDepartment(-1, null);
        };
        execute(handler);
        assertEmployeeNames(1L);
        assertEmployeeNames(2L);
        assertDepartmentName(1L, null, -1);
        assertDepartmentName(2L, null, -1);
    }
    
    @Test
    public void testChangeOrderByDepartment() {
        
        Consumer<Session> handler;
        
        handler = session -> {
            Department department = (Department)session.load(Department.class, 1L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            Employee employee3 = (Employee)session.load(Employee.class, 3L);
            department.getEmployees().add(employee1);
            department.getEmployees().add(employee2);
            department.getEmployees().add(employee3);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee1", "employee2", "employee3");
        assertDepartmentName(1L, "department1", 0);
        assertDepartmentName(2L, "department1", 1);
        assertDepartmentName(3L, "department1", 2);
        
        handler = session -> {
            Department department = (Department)session.load(Department.class, 1L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            department.getEmployees().add(0, employee2);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2", "employee1", "employee3");
        assertDepartmentName(1L, "department1", 1);
        assertDepartmentName(2L, "department1", 0);
        assertDepartmentName(3L, "department1", 2);
        
        handler = session -> {
            Department department = (Department)session.load(Department.class, 1L);
            Employee employee3 = (Employee)session.load(Employee.class, 3L);
            department.getEmployees().add(0, employee3);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee3", "employee2", "employee1");
        assertDepartmentName(1L, "department1", 2);
        assertDepartmentName(2L, "department1", 1);
        assertDepartmentName(3L, "department1", 0);
        
        handler = session -> {
            Department department = (Department)session.load(Department.class, 1L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            department.getEmployees().add(1, employee1);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee3", "employee1", "employee2");
        assertDepartmentName(1L, "department1", 1);
        assertDepartmentName(2L, "department1", 2);
        assertDepartmentName(3L, "department1", 0);
        
        handler = session -> {
            Department department = (Department)session.load(Department.class, 1L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            department.getEmployees().add(1, employee2);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee3", "employee2", "employee1");
        assertDepartmentName(1L, "department1", 2);
        assertDepartmentName(2L, "department1", 1);
        assertDepartmentName(3L, "department1", 0);
        
        handler = session -> {
            Department department = (Department)session.load(Department.class, 1L);
            Employee employee3 = (Employee)session.load(Employee.class, 3L);
            department.getEmployees().add(3, employee3);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2", "employee1", "employee3");
        assertDepartmentName(1L, "department1", 1);
        assertDepartmentName(2L, "department1", 0);
        assertDepartmentName(3L, "department1", 2);
        
        handler = session -> {
            Department department = (Department)session.load(Department.class, 1L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            department.getEmployees().add(3, employee2);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee1", "employee3", "employee2");
        assertDepartmentName(1L, "department1", 0);
        assertDepartmentName(2L, "department1", 2);
        assertDepartmentName(3L, "department1", 1);
    }
    
    @Test
    public void testChangeOrderByEmployee() {
        
        Consumer<Session> handler;
        
        handler = session -> {
            Department department = (Department)session.load(Department.class, 1L);
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            Employee employee3 = (Employee)session.load(Employee.class, 3L);
            department.getEmployees().add(employee1);
            department.getEmployees().add(employee2);
            department.getEmployees().add(employee3);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee1", "employee2", "employee3");
        assertDepartmentName(1L, "department1", 0);
        assertDepartmentName(2L, "department1", 1);
        assertDepartmentName(3L, "department1", 2);
        
        handler = session -> {
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            employee2.setIndexInDepartment(0);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2", "employee1", "employee3");
        assertDepartmentName(1L, "department1", 1);
        assertDepartmentName(2L, "department1", 0);
        assertDepartmentName(3L, "department1", 2);
        
        handler = session -> {
            Employee employee3 = (Employee)session.load(Employee.class, 3L);
            employee3.setIndexInDepartment(0);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee3", "employee2", "employee1");
        assertDepartmentName(1L, "department1", 2);
        assertDepartmentName(2L, "department1", 1);
        assertDepartmentName(3L, "department1", 0);
        
        handler = session -> {
            Employee employee1 = (Employee)session.load(Employee.class, 1L);
            employee1.setIndexInDepartment(1);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee3", "employee1", "employee2");
        assertDepartmentName(1L, "department1", 1);
        assertDepartmentName(2L, "department1", 2);
        assertDepartmentName(3L, "department1", 0);
        
        handler = session -> {
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            employee2.setIndexInDepartment(1);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee3", "employee2", "employee1");
        assertDepartmentName(1L, "department1", 2);
        assertDepartmentName(2L, "department1", 1);
        assertDepartmentName(3L, "department1", 0);
        
        handler = session -> {
            Employee employee3 = (Employee)session.load(Employee.class, 3L);
            employee3.setIndexInDepartment(2);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee2", "employee1", "employee3");
        assertDepartmentName(1L, "department1", 1);
        assertDepartmentName(2L, "department1", 0);
        assertDepartmentName(3L, "department1", 2);
        
        handler = session -> {
            Employee employee2 = (Employee)session.load(Employee.class, 2L);
            employee2.setIndexInDepartment(2);
        };
        execute(handler);
        assertEmployeeNames(1L, "employee1", "employee3", "employee2");
        assertDepartmentName(1L, "department1", 0);
        assertDepartmentName(2L, "department1", 2);
        assertDepartmentName(3L, "department1", 1);
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
    
    private static void assertDepartmentName(
            final long employeeId, 
            final String departmentName,
            final int employeeIndexInDepartment) {
        Consumer<Session> handler = session -> {
            Employee employee = (Employee)session.get(Employee.class, employeeId);
            if (departmentName == null) {
                Assert.assertNull(employee.getDepartment());    
            } else {
                Assert.assertNotNull(employee.getDepartment());
                Assert.assertEquals(departmentName, employee.getDepartment().getName());
                Assert.assertEquals(employeeIndexInDepartment, employee.getIndexInDepartment());
            }
        };
        execute(handler);
    }
}
