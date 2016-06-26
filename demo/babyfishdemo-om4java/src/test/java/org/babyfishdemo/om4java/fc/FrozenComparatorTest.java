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
package org.babyfishdemo.om4java.fc;

import org.babyfish.collection.MACollections;
import org.junit.Assert;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class FrozenComparatorTest {
 
    /*
     * "Department.employees" uses the "firstName" and "lastName" to compare two Employee objects, 
     * but in order to keep the simplicity of our demo, the test code will NOT change the property "lastName" 
     * of Employee so that it always is null, ONLY the property "firstName" of Employee will be used by this test class.
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
            
            assertDepartment(department, employee1, employee2, employee3); // Changed by you
            Assert.assertSame(department, employee1.getDepartment()); // Changed automatically
            Assert.assertSame(department, employee2.getDepartment()); // Changed automatically
            Assert.assertSame(department, employee3.getDepartment()); // Changed automatically
        }
        
        {
            /*
             * Change the firstName of employee1 from "E-1" to "E-I",
             * The "department.getEmployees()" is adjusted automatically
             * so that its order is changed.
             *  
             * now, the order of the navigable set is:
             * employee2(E-2) < employee3(E-3) < employee1(E-I)
             */
            employee1.setFirstName("E-I");
            
            assertDepartment(department, employee2, employee3, employee1); // Changed automatically, employee1 become the last element
            Assert.assertSame(department, employee1.getDepartment());
            Assert.assertSame(department, employee2.getDepartment());
            Assert.assertSame(department, employee3.getDepartment());
        }
        
        {
            /*
             * Change the firstName of employee2 from "E-2" to "E-II",
             * The "department.getEmployees()" is adjusted automatically
             * so that its order is changed.
             *  
             * now, employee3(E-3) < employee1(E-I) < employee2(E-II)
             */
            employee2.setFirstName("E-II");
            
            assertDepartment(department, employee3, employee1, employee2); // Changed automatically, employee2 become the last element
            Assert.assertSame(department, employee1.getDepartment());
            Assert.assertSame(department, employee2.getDepartment());
            Assert.assertSame(department, employee3.getDepartment());
        }
        
        {
            /*
             * Change the firstName of employee3 from "E-3" to "E-III",
             * The "department.getEmployees()" is adjusted automatically
             * so that its order is changed.
             *  
             * now, the order of the navigable set is:
             * employee1(E-I) < employee2(E-II) < employee3(E-III)
             */
            employee3.setFirstName("E-III");
            
            assertDepartment(department, employee1, employee2, employee3); // Changed automatically, employee3 become the last element
            Assert.assertSame(department, employee1.getDepartment());
            Assert.assertSame(department, employee2.getDepartment());
            Assert.assertSame(department, employee3.getDepartment());
        }
        
        /*
         * Change the firstName of employee1 from "E-I" to "E-X",
         * The "department.getEmployees()" is adjusted automatically
         * so that its order is changed.
         *  
         * now, the order of the navigable set is:
         * employee2(E-II) < employee3(E-III) < employee1(E-X)
         */
        {
            employee1.setFirstName("E-X");
            
            assertDepartment(department, employee2, employee3, employee1); // Changed automatically, employee1 because the last element
            Assert.assertSame(department, employee1.getDepartment());
            Assert.assertSame(department, employee2.getDepartment());
            Assert.assertSame(department, employee3.getDepartment());
        }
        
        {
            /*
             * Change the name of employee2 from "E-II" to "E-X"
             *
             * The "department.getEmployees()" is adjusted automatically.
             * 
             * Finally:
             * (1) employee1 whose firstName is "E-X" too, 
             *       so it will be removed from the collection 
             *     and
             *     its property "department" will be set to be null 
             *     automatically and implicitly.
             * (2) employee2 will become the last child of this collection because "E-III" < "EX".
             */
            employee2.setFirstName("E-X");
            
            /*
             * Changed automatically
             * (1) Element crowding out effect: employee1 is removed from the navigable map automatically
             * (2) employee2 become the last element of the navigable map
             */
            assertDepartment(department, employee3, employee2);
            
            Assert.assertNull(employee1.getDepartment()); // Changed automatically
            Assert.assertSame(department, employee2.getDepartment());
            Assert.assertSame(department, employee3.getDepartment());
        }
        
        {
            /*
             * Change the name of employee3 from "E-III" to "E-X"
             *
             * The "department.getEmployees()" is adjusted automatically.
             * 
             * Finally:
             * employee2 whose firstName is "E-X" too 
             * will be removed from the collection 
             * and
             * its property "department" will be set to be null 
             * automatically and implicitly.
             */
            employee3.setFirstName("E-X");
            
            /*
             * Changed automatically
             * (1) Element crowding out effect: employee2 is removed automatically
             * (2) employee3 is refreshed(removed and added again), but it's not easy to see that.
             */
            assertDepartment(department, employee3);
            /*
             * But it can be seen by these two statement
             */
            Assert.assertTrue(department.getEmployees().contains(createEmployeeByFirstName("E-X")));
            Assert.assertFalse(department.getEmployees().contains(createEmployeeByFirstName("E-3")));
            
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getDepartment()); // Changed automatically
            Assert.assertSame(department, employee3.getDepartment());
        }
    }
    
    private static void assertDepartment(Department department, Employee ... employees) {
        Assert.assertEquals(employees.length, department.getEmployees().size());
        int index = 0;
        for (Employee employee : department.getEmployees()) {
            Assert.assertSame(employees[index++], employee);
        }
    }
    
    private static Employee createEmployeeByFirstName(String firstName) {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        return employee;
    }
}
