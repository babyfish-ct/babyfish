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
package org.babyfish.model.classic.l2ir;

import org.babyfish.model.classic.Utils;
import org.babyfish.model.classic.l2ir.entities.Department;
import org.babyfish.model.classic.l2ir.entities.Employee;
import org.babyfish.model.spi.reference.IndexedReference;
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
        Assert.assertEquals(IndexedReference.INVALID_INDEX, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(IndexedReference.INVALID_INDEX, employee2.getIndexInDepartment());
        
        department1.getEmployees().add(0, employee1);
        Utils.assertCollection(department1.getEmployees(), employee1);
        Utils.assertCollection(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(IndexedReference.INVALID_INDEX, employee2.getIndexInDepartment());
        
        department1.getEmployees().add(0, employee2);
        Utils.assertCollection(department1.getEmployees(), employee2, employee1);
        Utils.assertCollection(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        
        department2.getEmployees().add(0, employee1);
        Utils.assertCollection(department1.getEmployees(), employee2);
        Utils.assertCollection(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        
        department2.getEmployees().add(0, employee2);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees(), employee2, employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        
        department2.getEmployees().remove(0);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(-1, employee2.getIndexInDepartment());
        
        department2.getEmployees().remove(0);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals(-1, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(-1, employee2.getIndexInDepartment());
    }
    
    @Test
    public void testModifyIndexAndValueOfEmployee() {
        
        Department department1 = new Department();
        Department department2 = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals(IndexedReference.INVALID_INDEX, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(IndexedReference.INVALID_INDEX, employee2.getIndexInDepartment());
        
        employee1.setIndexInDepartment(0);
        employee1.setDepartment(department1);
        Utils.assertCollection(department1.getEmployees(), employee1);
        Utils.assertCollection(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(IndexedReference.INVALID_INDEX, employee2.getIndexInDepartment());
        
        employee2.setIndexInDepartment(0);
        employee2.setDepartment(department1);
        Utils.assertCollection(department1.getEmployees(), employee2, employee1);
        Utils.assertCollection(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        
        employee1.setIndexInDepartment(0);
        employee1.setDepartment(department2);
        Utils.assertCollection(department1.getEmployees(), employee2);
        Utils.assertCollection(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        
        employee2.setIndexInDepartment(0);
        employee2.setDepartment(department2);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees(), employee2, employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        
        employee2.setIndexInDepartment(-1);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(-1, employee2.getIndexInDepartment());
        
        employee1.setIndexInDepartment(-1);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals(-1, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(-1, employee2.getIndexInDepartment());
    }
    
    @Test
    public void testModifyValueAndIndexOfEmployee() {
        
        Department department1 = new Department();
        Department department2 = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals(IndexedReference.INVALID_INDEX, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(IndexedReference.INVALID_INDEX, employee2.getIndexInDepartment());
        
        employee1.setDepartment(department1);
        employee1.setIndexInDepartment(0);
        Utils.assertCollection(department1.getEmployees(), employee1);
        Utils.assertCollection(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(IndexedReference.INVALID_INDEX, employee2.getIndexInDepartment());
        
        employee2.setDepartment(department1);
        employee2.setIndexInDepartment(0);
        Utils.assertCollection(department1.getEmployees(), employee2, employee1);
        Utils.assertCollection(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        
        //Modify the index of employee1 to avoid IndexOutOfBoundsException
        employee1.setIndexInDepartment(-1);
        
        employee1.setDepartment(department2);
        employee1.setIndexInDepartment(0);
        Utils.assertCollection(department1.getEmployees(), employee2);
        Utils.assertCollection(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        
        employee2.setDepartment(department2);
        employee2.setIndexInDepartment(0);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees(), employee2, employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        
        Utils.assertCollection(department1.getEmployees());
        employee2.setDepartment(null);
        Utils.assertCollection(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(-1, employee2.getIndexInDepartment());
        
        Utils.assertCollection(department1.getEmployees());
        employee1.setDepartment(null);
        Utils.assertCollection(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals(-1, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(-1, employee2.getIndexInDepartment());
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
        Assert.assertEquals(IndexedReference.INVALID_INDEX, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(IndexedReference.INVALID_INDEX, employee2.getIndexInDepartment());
        
        employee1.setDepartment(0, department1);
        Utils.assertCollection(department1.getEmployees(), employee1);
        Utils.assertCollection(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(IndexedReference.INVALID_INDEX, employee2.getIndexInDepartment());
        
        employee2.setDepartment(0, department1);
        Utils.assertCollection(department1.getEmployees(), employee2, employee1);
        Utils.assertCollection(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        
        employee1.setDepartment(0, department2);
        Utils.assertCollection(department1.getEmployees(), employee2);
        Utils.assertCollection(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        
        employee2.setDepartment(0, department2);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees(), employee2, employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        
        employee2.setDepartment(-1, null);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(-1, employee2.getIndexInDepartment());
        
        employee1.setDepartment(-1, null);
        Utils.assertCollection(department1.getEmployees());
        Utils.assertCollection(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals(-1, employee1.getIndexInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals(-1, employee2.getIndexInDepartment());
    }
    
    @Test
    public void testChangeOrderByDepartment() {
        
        Department department = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Employee employee3 = new Employee();
        department.getEmployees().add(employee1);
        department.getEmployees().add(employee2);
        department.getEmployees().add(employee3);
        Utils.assertCollection(department.getEmployees(), employee1, employee2, employee3);
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertEquals(1, employee2.getIndexInDepartment());
        Assert.assertEquals(2, employee3.getIndexInDepartment());
        
        department.getEmployees().add(0, employee2);
        Utils.assertCollection(department.getEmployees(), employee2, employee1, employee3);
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        Assert.assertEquals(2, employee3.getIndexInDepartment());
        
        department.getEmployees().add(0, employee3);
        Utils.assertCollection(department.getEmployees(), employee3, employee2, employee1);
        Assert.assertEquals(2, employee1.getIndexInDepartment());
        Assert.assertEquals(1, employee2.getIndexInDepartment());
        Assert.assertEquals(0, employee3.getIndexInDepartment());
        
        department.getEmployees().add(1, employee1);
        Utils.assertCollection(department.getEmployees(), employee3, employee1, employee2);
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(2, employee2.getIndexInDepartment());
        Assert.assertEquals(0, employee3.getIndexInDepartment());
        
        department.getEmployees().add(1, employee2);
        Utils.assertCollection(department.getEmployees(), employee3, employee2, employee1);
        Assert.assertEquals(2, employee1.getIndexInDepartment());
        Assert.assertEquals(1, employee2.getIndexInDepartment());
        Assert.assertEquals(0, employee3.getIndexInDepartment());
        
        department.getEmployees().add(3, employee3);
        Utils.assertCollection(department.getEmployees(), employee2, employee1, employee3);
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        Assert.assertEquals(2, employee3.getIndexInDepartment());
        
        department.getEmployees().add(3, employee2);
        Utils.assertCollection(department.getEmployees(), employee1, employee3, employee2);
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertEquals(2, employee2.getIndexInDepartment());
        Assert.assertEquals(1, employee3.getIndexInDepartment());
    }
    
    @Test
    public void testChangeOrderByEmployee() {
        
        Department department = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Employee employee3 = new Employee();
        department.getEmployees().add(employee1);
        department.getEmployees().add(employee2);
        department.getEmployees().add(employee3);
        Utils.assertCollection(department.getEmployees(), employee1, employee2, employee3);
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertEquals(1, employee2.getIndexInDepartment());
        Assert.assertEquals(2, employee3.getIndexInDepartment());
        
        employee2.setIndexInDepartment(0);
        Utils.assertCollection(department.getEmployees(), employee2, employee1, employee3);
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        Assert.assertEquals(2, employee3.getIndexInDepartment());
        
        employee3.setIndexInDepartment(0);
        Utils.assertCollection(department.getEmployees(), employee3, employee2, employee1);
        Assert.assertEquals(2, employee1.getIndexInDepartment());
        Assert.assertEquals(1, employee2.getIndexInDepartment());
        Assert.assertEquals(0, employee3.getIndexInDepartment());
        
        employee1.setIndexInDepartment(1);
        Utils.assertCollection(department.getEmployees(), employee3, employee1, employee2);
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(2, employee2.getIndexInDepartment());
        Assert.assertEquals(0, employee3.getIndexInDepartment());
        
        employee2.setIndexInDepartment(1);
        Utils.assertCollection(department.getEmployees(), employee3, employee2, employee1);
        Assert.assertEquals(2, employee1.getIndexInDepartment());
        Assert.assertEquals(1, employee2.getIndexInDepartment());
        Assert.assertEquals(0, employee3.getIndexInDepartment());
        
        employee3.setIndexInDepartment(2);
        Utils.assertCollection(department.getEmployees(), employee2, employee1, employee3);
        Assert.assertEquals(1, employee1.getIndexInDepartment());
        Assert.assertEquals(0, employee2.getIndexInDepartment());
        Assert.assertEquals(2, employee3.getIndexInDepartment());
        
        employee2.setIndexInDepartment(2);
        Utils.assertCollection(department.getEmployees(), employee1, employee3, employee2);
        Assert.assertEquals(0, employee1.getIndexInDepartment());
        Assert.assertEquals(2, employee2.getIndexInDepartment());
        Assert.assertEquals(1, employee3.getIndexInDepartment());
    }
}
