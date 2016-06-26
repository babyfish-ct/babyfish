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
package org.babyfishdemo.om4java.fec;

import java.util.Set;

import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.junit.Assert;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class FrozenEqualityComparatorTest {
 
    /*
     * "Department.getEmployees()" uses the "firstName" and "lastName" to calculate the hashCode 
     * of Employee and check whether two Employee objects are equal, but in order to keep the simplicity 
     * of our demo, the test code will NOT change the property "lastName" of Employee so that it always 
     * is null, ONLY the property "firstName" of Employee will be used by the test class.
     */
    @Test
    public void test() {
        Department department = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Employee employee3 = new Employee();
        employee1.setFirstName("E-1");
        employee2.setFirstName("E-2");
        employee3.setFirstName("E-3");
        
        {
            /*
             * Validate the initialized state of those objects.
             */
            assertDepartment(department);
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getDepartment());
            Assert.assertNull(employee3.getDepartment());
        }
        
        {
            /*
             * Add all the employees into the department,
             * the property "department" of all employees will be changed automatically and implicitly.
             */
            department.getEmployees().addAll(MACollections.wrap(employee1, employee2, employee3));
            assertDepartment(department, employee1, employee2, employee3);
            Assert.assertSame(department, employee1.getDepartment());
            Assert.assertSame(department, employee2.getDepartment());
            Assert.assertSame(department, employee3.getDepartment());
        }
        
        {
            /*
             * Change the firstName of employee1 from "E-1" to "Boss",
             * "department.employees" will be adjusted automatically.
             *
             * Unfortunately, XOrderedSet looks has not been changed.
             * it's not easy to demonstrate it.
             * 
             * But "contains" can demonstrate it easily.
             */
            employee1.setFirstName("Boss");
            assertDepartment(department, employee1, employee2, employee3); // Internal data structure is changed automatically even if it looks has not been changed.
            Assert.assertTrue(department.getEmployees().contains(createEmployeeByFirstName("Boss"))); // Changed automatically
            Assert.assertFalse(department.getEmployees().contains(createEmployeeByFirstName("E-1"))); // Changed automatically
            Assert.assertSame(department, employee1.getDepartment());
            Assert.assertSame(department, employee2.getDepartment());
            Assert.assertSame(department, employee3.getDepartment());
        }
        
        {
            /*
             * Change the name of employee2 from "E-2" to "Boss",
             * "department.getEmployees()" will be adjusted automatically.
             * 
             * Finally, employee1 whose firstName is "Boss" too
             * will be removed from the collection 
             * and 
             * its property "department" will be set to be null 
             * automatically and implicitly.
             */
            employee2.setFirstName("Boss");
            
            /*
             * Changed automatically
             * (1) employee1 is removed automatically
             * (2) employee2 is refreshed automatically(removed and added again)
             */
            assertDepartment(department, employee2, employee3); // Element crowding out effect: employee1 has been removed automatically
            Assert.assertTrue(department.getEmployees().contains(createEmployeeByFirstName("Boss")));
            Assert.assertFalse(department.getEmployees().contains(createEmployeeByFirstName("E-2"))); // Changed automatically
            
            Assert.assertNull(employee1.getDepartment()); 
            Assert.assertSame(department, employee2.getDepartment());
            Assert.assertSame(department, employee3.getDepartment());
        }
        
        {
            /*
             * Change the name of employee3 from "E-3" to "Boss",
             * "department.getEmployees()" will be adjusted automatically.
             * 
             * Finally, employee2 whose firstName is "Boss" too
             * will be removed from the collection 
             * and 
             * its property "department" will be set to be null 
             * automatically and implicitly.
             */
            employee3.setFirstName("Boss");
            
            /*
             * Changed automatically
             * (1) employee2 is removed automatically
             * (2) employee3 is refreshed automatically(removed and added again)
             */
            assertDepartment(department, employee3); // Element crowding out effect: employee2 has been removed automatically
            Assert.assertTrue(department.getEmployees().contains(createEmployeeByFirstName("Boss")));
            Assert.assertFalse(department.getEmployees().contains(createEmployeeByFirstName("E-3"))); // Changed automatically
            
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getDepartment()); 
            Assert.assertSame(department, employee3.getDepartment());
        }
    }
    
    private static void assertDepartment(Department department, Employee ... employees) {
        Assert.assertEquals(employees.length, department.getEmployees().size());
        Set<Employee> set = new HashSet<>((employees.length * 4 + 2) / 3);
        for (Employee employee : employees) {
            set.add(employee);
        }
        Assert.assertEquals(set, department.getEmployees());
    }
    
    private static Employee createEmployeeByFirstName(String firstName) {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        return employee;
    }
}
