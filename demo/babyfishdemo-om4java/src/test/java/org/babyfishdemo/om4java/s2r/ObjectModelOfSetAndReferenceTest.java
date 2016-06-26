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
package org.babyfishdemo.om4java.s2r;

import java.util.Iterator;
import java.util.Set;
 


import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.babyfishdemo.om4java.s2r.Department;
import org.babyfishdemo.om4java.s2r.Employee;
import org.junit.Assert;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class ObjectModelOfSetAndReferenceTest {
 
    @Test
    public void test() {
        /*
         * Prepare two departments and 4 employees
         */
        Department department1 = new Department();
        Department department2 = new Department();
        Employee employee1 = new Employee("E-1");
        Employee employee2 = new Employee("E-2");
        Employee employee3 = new Employee("E-3");
        Employee employee4 = new Employee("E-4");
        
        {
            /*
             * Validate the initialized state of these objects
             */
            assertDepartment(department1);
            assertDepartment(department2);
            assertEmployee(employee1, null);
            assertEmployee(employee2, null);
            assertEmployee(employee3, null);
            assertEmployee(employee4, null);
        }
        
        {
            /*
             * Add employee1 into department1.
             * The property "department" of employee1 will be changed automatically and implicitly.
             */
            department1.getEmployees().add(employee1);
            
            assertDepartment(department1, employee1); // Changed by you
            assertDepartment(department2);
            assertEmployee(employee1, department1); // Changed automatically
            assertEmployee(employee2, null);
            assertEmployee(employee3, null);
            assertEmployee(employee4, null);
        }
        
        {
            /*
             * Set department1 to be the parent of employe2.
             * The property "employees" of department1 will be changed automatically and implicitly.
             */
            employee2.setDepartment(department1);
            
            assertDepartment(department1, employee1, employee2); // Changed automatically
            assertDepartment(department2);
            assertEmployee(employee1, department1);
            assertEmployee(employee2, department1); // Changed by you
            assertEmployee(employee3, null);
            assertEmployee(employee4, null);
        }
        
        {
            /*
             * Add employee3 and employee4 into department1.
             * (1) The property "department" of employee3 or employee4 will be changed automatically and implicitly.
             */
            department1.getEmployees().addAll(MACollections.wrap(employee3, employee4));
            
            assertDepartment(department1, employee1, employee2, employee3, employee4); // Changed by you
            assertDepartment(department2);
            assertEmployee(employee1, department1);
            assertEmployee(employee2, department1);
            assertEmployee(employee3, department1); // Changed automatically
            assertEmployee(employee4, department1); // Changed automatically
        }
        
        {
        /*
         * Change the parent object of employee4 to be department2.
         * (1) The property "employees" of the old parent object department1 will be changed automatically and implicitly.
         * (2) The property "employees" of the new parent object department2 will be changed automatically and implicitly.
         */
            employee4.setDepartment(department2);
            
            assertDepartment(department1, employee1, employee2, employee3); // Changed automatically
            assertDepartment(department2, employee4); // Changed automatically
            assertEmployee(employee1, department1);
            assertEmployee(employee2, department1);
            assertEmployee(employee3, department1);
            assertEmployee(employee4, department2); // Changed by you
        }
        
        {
            /*
             * Let department2 seize all the employees of department1
             * (1) The property "department" of employee1, employee2 and employee3 
             *    will be changed automatically and implicitly.
             * (2) The original parent object department1 of employee1, employee2 and employee3 lost all the employees,
             *  so its property "employees" will be clean automatically and implicitly.
             */
            department2.getEmployees().addAll(department1.getEmployees());
            
            assertDepartment(department1); // Changed automatically
            assertDepartment(department2, employee1, employee2, employee3, employee4); // Changed by you
            assertEmployee(employee1, department2); // Changed automatically
            assertEmployee(employee2, department2); // Changed automatically
            assertEmployee(employee3, department2); // Changed automatically
            assertEmployee(employee4, department2);
        }
        
        {
            /*
             * Advance functionality:
             * Remove all the employees whose name ends with even number,
             * that means employee2 and employee4 will be removed.
             * The property "department" of employee2 and employee4 will be changed to be null automatically and implicitly.
             */
            Iterator<Employee> itr = department2.getEmployees().iterator();
            while (itr.hasNext()) {
                Employee employee = itr.next();
                int number = Integer.parseInt(employee.getName().substring(employee.getName().indexOf('-') + 1));
                if (number % 2 == 0) {
                    itr.remove(); //Can ONLY invoke remove of iterator, can NOT invoke remove of collection
                }
            }
            
            assertDepartment(department1);
            assertDepartment(department2, employee1, employee3); // Changed by you
            assertEmployee(employee1, department2);
            assertEmployee(employee2, null); // Changed automatically
            assertEmployee(employee3, department2);
            assertEmployee(employee4, null); // Changed automatically
        }
        
        {
            /*
             * Change the property "department" of employee1 to be null.
             * The property "employees" of the parent object department2 will be changed automatically and implicitly.
             */
            employee1.setDepartment(null);
            
            assertDepartment(department1);
            assertDepartment(department2, employee3); // Changed automatically
            assertEmployee(employee1, null); // Changed by you
            assertEmployee(employee2, null);
            assertEmployee(employee3, department2);
            assertEmployee(employee4, null);
        }
        
        {
            /*
             * Clean the property "employees" of department2.
             * The property "department" of employee3 that is the last child of department2 
             * will be changed automatically and implicitly.
             */
            department2.getEmployees().clear();
            
            assertDepartment(department1);
            assertDepartment(department2); // Changed by you
            assertEmployee(employee1, null);
            assertEmployee(employee2, null);
            assertEmployee(employee3, null); // Changed automatically
            assertEmployee(employee4, null);
        }
    }
    
    //Validate the child employee objects of a department object
    private static void assertDepartment(Department department, Employee ... employees) {
        Assert.assertEquals(employees.length, department.getEmployees().size());
        if (employees.length != 0) {
            Set<Employee> set = new HashSet<>((employees.length * 4 + 2) / 3);
            for (Employee employee : employees) {
                set.add(employee);
            }
            Assert.assertEquals(department.getEmployees(), set);
        }
    }
    
    //Valiate the parent department object of a employee object
    private static void assertEmployee(Employee employee, Department department) {
        Assert.assertSame(department, employee.getDepartment());
    }
}
