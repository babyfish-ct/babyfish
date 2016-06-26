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
package org.babyfish.model.classic.s2r;

import java.util.Iterator;

import org.babyfish.model.classic.Utils;
import org.babyfish.model.classic.s2r.entities.Department;
import org.babyfish.model.classic.s2r.entities.Employee;
import org.junit.Test;

import junit.framework.Assert;

public class ModelTest {

    @Test
    public void testModifyDepartment() {
        
        Department department = new Department();
        Employee employee = new Employee();
        Department newDepartment = new Department();
        Utils.assertCollection(department.getEmployees());
        Assert.assertNull(employee.getDepartment());
        Utils.assertCollection(newDepartment.getEmployees());
        
        department.getEmployees().add(employee);
        Utils.assertCollection(department.getEmployees(), employee);
        Assert.assertEquals(department, employee.getDepartment());
        Utils.assertCollection(newDepartment.getEmployees());
        
        newDepartment.getEmployees().add(employee);
        Utils.assertCollection(department.getEmployees());
        Assert.assertEquals(newDepartment, employee.getDepartment());
        Utils.assertCollection(newDepartment.getEmployees(), employee);
        
        Iterator<Employee> itr = newDepartment.getEmployees().iterator();
        itr.next();
        itr.remove();
        Utils.assertCollection(department.getEmployees());
        Assert.assertNull(employee.getDepartment());
        Utils.assertCollection(newDepartment.getEmployees());
    }
    
    @Test
    public void testModifyEmployee() {
        
        Department department = new Department();
        Employee employee = new Employee();
        Department newDepartment = new Department();
        Utils.assertCollection(department.getEmployees());
        Assert.assertNull(employee.getDepartment());
        Utils.assertCollection(newDepartment.getEmployees());
        
        employee.setDepartment(department);
        Utils.assertCollection(department.getEmployees(), employee);
        Assert.assertEquals(department, employee.getDepartment());
        Utils.assertCollection(newDepartment.getEmployees());
        
        employee.setDepartment(newDepartment);
        Utils.assertCollection(department.getEmployees());
        Assert.assertEquals(newDepartment, employee.getDepartment());
        Utils.assertCollection(newDepartment.getEmployees(), employee);
        
        employee.setDepartment(null);
        Utils.assertCollection(department.getEmployees());
        Assert.assertNull(employee.getDepartment());
        Utils.assertCollection(newDepartment.getEmployees());
    }
}
