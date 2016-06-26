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
package org.babyfishdemo.om4java.l2ir;

import java.util.Iterator;

import org.babyfishdemo.om4java.l2ir.Department;
import org.babyfishdemo.om4java.l2ir.Employee;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ObjectModelOfListAndIndexedReferenceTest {

    @Test
    public void test() {
        /*
         * Prepare two departments and 4 employees
         */
        Department department1 = new Department();
        Department department2 = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Employee employee3 = new Employee();
        Employee employee4 = new Employee();
        
        {
            /*
             * Validate the initialized state of these objects
             */
            assertDepartment(department1);
            assertDepartment(department2);
            assertEmployee(employee1, -1, null);
            assertEmployee(employee2, -1, null);
            assertEmployee(employee3, -1, null);
            assertEmployee(employee4, -1, null);
        }
        
        {
            /*
             * Add employee1 into department1.
             * The property "index" and "department" of employee1 will be changed automatically and implicitly.
             */
            department1.getEmployees().add(employee1);
            
            assertDepartment(department1, employee1); // Changed by you
            assertDepartment(department2);
            assertEmployee(employee1, 0, department1); // Changed automatically
            assertEmployee(employee2, -1, null);
            assertEmployee(employee3, -1, null);
            assertEmployee(employee4, -1, null);
        }
        
        {
            /*
             * Set department1 to be the parent of employe2 and let employee2 be the first child of department1,
             * after both the property "index" and "department" are changed
             * 
             * (1) The property "employees" of department1 will be changed automatically and implicitly.
             * (2) Before employe2 become the first child of department1, employee1 already is the first child of department1,
             * so the property "index" of employee1 will be increased automatically and implicitly.
             */
            employee2.setIndex(0);
            employee2.setDepartment(department1);
            
            assertDepartment(department1, employee2, employee1); // Changed automatically
            assertDepartment(department2);
            assertEmployee(employee1, 1, department1); // index+, changed automatically
            assertEmployee(employee2, 0, department1); // Changed by you
            assertEmployee(employee3, -1, null);
            assertEmployee(employee4, -1, null);
        }
        
        {
            /*
             * Insert employee3 into department1 and make sure the employee3 is the first child of department1
             * (1) The property "index" and "department" of employee3 will be changed automatically and implicitly.
             * (2) employee3 will be the first child of department1, there are two existing children: employee2 and employee1,
             * so the property "index" of both employee2 and employee1 will be increased automatically and implicitly.
             */
            department1.getEmployees().add(0, employee3);
            
            assertDepartment(department1, employee3, employee2, employee1); // Changed by you
            assertDepartment(department2);
            assertEmployee(employee1, 2, department1); // index++, changed automatically
            assertEmployee(employee2, 1, department1); // index++, changed automatically
            assertEmployee(employee3, 0, department1); // Changed automatically
            assertEmployee(employee4, -1, null);
        }
        
        {
            /*
             * Set department1 to be the parent of employe4, and make sure employee4 to be the first child of department1,
             * after both the property "index" and "department" are changed
             * (1) The property "employees" of department1 will be changed automatically and implicitly.
             * (2) Before employe4 will be the first child of department1, 
             * employee3, employee2 and employee1 are existing children,
             * so the property "index" of employee3, employee2, employee1 will be increased automatically and implicitly.
             */
            employee4.setIndex(0);
            employee4.setDepartment(department1);
            
            assertDepartment(department1, employee4, employee3, employee2, employee1); // Changed automatically
            assertDepartment(department2);
            assertEmployee(employee1, 3, department1); // index++, changed automatically
            assertEmployee(employee2, 2, department1); // index++, changed automatically
            assertEmployee(employee3, 1, department1); // index++, changed automatically
            assertEmployee(employee4, 0, department1); // Changed by you
        }
        
        {
            /*
             * Change the parent object of employee4 to be department2.
             * (1) The property "employees" of the old parent object department1 will be changed automatically and implicitly.
             * (2) The property "employees" of the new parent object department2 will be changed automatically and implicitly.
             * (3) The property "index" of employee3, employee2 and employee1 will be decreased automatically and implicitly
             * because they are after the employee4 before modification.
             */
            employee4.setDepartment(department2);
            
            assertDepartment(department1, employee3, employee2, employee1); // Changed automatically
            assertDepartment(department2, employee4); // Changed automatically
            assertEmployee(employee1, 2, department1); // index--, changed automatically
            assertEmployee(employee2, 1, department1); // index--, changed automatically
            assertEmployee(employee3, 0, department1); // index--, changed automatically
            assertEmployee(employee4, 0, department2); // Changed by you
        }
        
        {
            /*
             * Let department2 seize all the employees of department1
             * (1) The original parent object department1 of employee3, employee2 and employee1 lost all the employees,
             * so its property "employees" will be clean automatically and implicitly.
             * (2) The property "index" and "department" of employee3, employee2 and employee1 
             * will be changed automatically and implicitly. 
             */
            department2.getEmployees().addAll(department1.getEmployees());
            
            assertDepartment(department1); // Changed automatically
            assertDepartment(department2, employee4, employee3, employee2, employee1); // Changed by you
            assertEmployee(employee1, 3, department2); // Changed automatically
            assertEmployee(employee2, 2, department2); // Changed automatically
            assertEmployee(employee3, 1, department2); // Changed automatically
            assertEmployee(employee4, 0, department2);
        }
        
        {
            /*
             * employee2 is already belong to department2, but insist on adding employee2 into department2 AGAIN 
             * and make sure it will become the first child of department2,
             * (1) The property "index" of employee2 will be changed automatically and implicitly.
             * (2) The property "index" of employee4 and employee3 will be changed automatically and implicitly
             * because they are before employee2 before modification.
             */
            department2.getEmployees().add(0, employee2);
            
            assertDepartment(department1);
            assertDepartment(department2, employee2, employee4, employee3, employee1); // Changed by you
            assertEmployee(employee1, 3, department2); 
            assertEmployee(employee2, 0, department2); // Changed automatically
            assertEmployee(employee3, 2, department2); // index++, changed automatically
            assertEmployee(employee4, 1, department2); // index++, changed automatically
        } 
        
        {
            /*
             * Change the the property "index" of employee4 to let it be the last child of department2,
             * (1) The property "employees" of department2 will be changed automatically and implicitly
             * (2) The property "index" of employee3 and employee1 will be changed automatically and implicitly
             * because they are after employee4 before modification.
             */
            employee4.setIndex(3);
            
            assertDepartment(department1);
            assertDepartment(department2, employee2, employee3, employee1, employee4); // Changed automatically
            assertEmployee(employee1, 2, department2); // index-- automatically
            assertEmployee(employee2, 0, department2);
            assertEmployee(employee3, 1, department2); // index-- automatically
            assertEmployee(employee4, 3, department2);
        }
        
        {
            /*
             * Advance functionality:
             * Remove employee3 and employee1 from department2 by modifying the subList VIEW of java.util.List
             * (1) The property "index" and "department" of employee3 and employee1 
             * will be changed automatically and implicitly.
             * (2) The property "index" of employee4 will be changed automatically and implicitly
             * because it is after employee3 and employee1 before modification.
             */
            //subList(1, 3).clear() <=> remove(index 1 and 2) <=> remove(employee3 and employee1);
            department2.getEmployees().subList(1, 3).clear();
            
            assertDepartment(department1);
            assertDepartment(department2, employee2, employee4); // Changed by you
            assertEmployee(employee1, -1, null); // Changed automatically
            assertEmployee(employee2, 0, department2);
            assertEmployee(employee3, -1, null); // Changed automatically
            assertEmployee(employee4, 1, department2); // index -= 2, changed automatically
        }
        
        {
            /*
             * Advance functionality:
             * Remove the first child "employee2" of department by ITERATOR.
             * (1) The property "index" and "department" of employee will be changed automatically and implicitly.
             * (2) The property "index" of employee4 will be changed automatically and implicitly
             * because it is after employee2 before modification.
             */
            Iterator<Employee> itr = department2.getEmployees().iterator();
            itr.next(); //Let iterator point to the firstChild
            itr.remove(); //Remove the first child via the iterator
            
            assertDepartment(department1);
            assertDepartment(department2, employee4); // Changed by you
            assertEmployee(employee1, -1, null);
            assertEmployee(employee2, -1, null); // Changed automatically
            assertEmployee(employee3, -1, null);
            assertEmployee(employee4, 0, department2); // index--, changed automatically
        }
        
        {
            /*
             * Remove employee4 of department2 by set the index of employee4,
             * (1) The property "employees" of department2 will be changed automatically and implicitly.
             * (2) The property "parent" of employee4 will be set to null automatically and implicitly.
             */
            employee4.setIndex(-1);
            
            assertDepartment(department1);
            assertDepartment(department2); // Changed automatically
            assertEmployee(employee1, -1, null);
            assertEmployee(employee2, -1, null);
            assertEmployee(employee3, -1, null);
            assertEmployee(employee4, -1, null); // index is changed by your, parent is changed automatically
        }
    }
    
    //Validate the child employee objects of a department object
    private static void assertDepartment(Department department, Employee ... employees) {
        Assert.assertEquals(employees.length, department.getEmployees().size());
        int index = 0;
        for (Employee actualEmployee : department.getEmployees()) {
            Assert.assertSame(employees[index++], actualEmployee);
        }
    }
    
    //Valiate the parent department object and the position in employee list of the parent department of a employee object
    private static void assertEmployee(Employee employee, int index, Department department) {
        Assert.assertEquals(index, employee.getIndex());
        Assert.assertSame(department, employee.getDepartment());
    }
}
