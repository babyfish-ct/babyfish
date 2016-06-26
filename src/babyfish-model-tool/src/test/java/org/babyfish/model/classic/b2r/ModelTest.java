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
package org.babyfish.model.classic.b2r;

import org.babyfish.model.classic.Utils;
import org.babyfish.model.classic.b2r.entities.Department;
import org.babyfish.model.classic.b2r.entities.Employee;
import org.junit.Test;

import junit.framework.Assert;

public class ModelTest {

    @Test
    public void testModifyDepartment() {
        
        Department department1 = new Department();
        Department department2 = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        department1.getEmployees().add(employee1);
        Utils.assertCollection(department1.getEmployees(), employee1);
        Utils.assertCollection(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        department1.getEmployees().add(employee2);
        Utils.assertCollection(department1.getEmployees(), employee1, employee2);
        Utils.assertCollection(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        department2.getEmployees().add(employee1);
        Utils.assertCollection(department1.getEmployees(), employee2);
        Utils.assertCollection(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        department2.getEmployees().add(employee2);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees(), employee1, employee2);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        department2.getEmployees().remove(department2.getEmployees().iterator().next());
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees(), employee2);
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        department2.getEmployees().remove(department2.getEmployees().iterator().next());
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
    }
    
    @Test
    public void testModifyEmployee() {
        
        Department department1 = new Department();
        Department department2 = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee1.setDepartment(department1);
        Utils.assertCollection(department1.getEmployees(), employee1);
        Utils.assertCollection(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee2.setDepartment(department1);
        Utils.assertCollection(department1.getEmployees(), employee1, employee2);
        Utils.assertCollection(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee1.setDepartment(department2);
        Utils.assertCollection(department1.getEmployees(), employee2);
        Utils.assertCollection(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee2.setDepartment(department2);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees(), employee1, employee2);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee2.setDepartment(null);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee1.setDepartment(null);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
    }
}
