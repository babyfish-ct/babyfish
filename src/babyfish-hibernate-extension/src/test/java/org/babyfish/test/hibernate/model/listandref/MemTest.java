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
package org.babyfish.test.hibernate.model.listandref;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class MemTest {

    @Test
    public void testModifyDepartment() {
        
        Department department1 = new Department();
        Department department2 = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        assertList(department1.getEmployees());
        assertList(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        department1.getEmployees().add(0, employee1);
        assertList(department1.getEmployees(), employee1);
        assertList(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        department1.getEmployees().add(0, employee2);
        assertList(department1.getEmployees(), employee2, employee1);
        assertList(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        department2.getEmployees().add(0, employee1);
        assertList(department1.getEmployees(), employee2);
        assertList(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        department2.getEmployees().add(0, employee2);
        assertList(department1.getEmployees());
        assertList(department2.getEmployees(), employee2, employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        department2.getEmployees().remove(0);
        assertList(department1.getEmployees());
        assertList(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        department2.getEmployees().remove(0);
        assertList(department1.getEmployees());
        assertList(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
    }
    
    @Test
    public void testModifyEmployee() {
        
        Department department1 = new Department();
        Department department2 = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        assertList(department1.getEmployees());
        assertList(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee1.setDepartment(department1);
        assertList(department1.getEmployees(), employee1);
        assertList(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee2.setDepartment(department1);
        assertList(department1.getEmployees(), employee1, employee2);
        assertList(department2.getEmployees());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee1.setDepartment(department2);
        assertList(department1.getEmployees(), employee2);
        assertList(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee2.setDepartment(department2);
        assertList(department1.getEmployees());
        assertList(department2.getEmployees(), employee1, employee2);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee2.setDepartment(null);
        assertList(department1.getEmployees());
        assertList(department2.getEmployees(), employee1);
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee1.setDepartment(null);
        assertList(department1.getEmployees());
        assertList(department2.getEmployees());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getDepartment());
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
        assertList(department.getEmployees(), employee1, employee2, employee3);
        
        department.getEmployees().add(0, employee2);
        assertList(department.getEmployees(), employee2, employee1, employee3);
        
        department.getEmployees().add(0, employee3);
        assertList(department.getEmployees(), employee3, employee2, employee1);
        
        department.getEmployees().add(1, employee1);
        assertList(department.getEmployees(), employee3, employee1, employee2);
        
        department.getEmployees().add(1, employee2);
        assertList(department.getEmployees(), employee3, employee2, employee1);
        
        department.getEmployees().add(3, employee3);
        assertList(department.getEmployees(), employee2, employee1, employee3);
        
        department.getEmployees().add(3, employee2);
        assertList(department.getEmployees(), employee1, employee3, employee2);
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertList(List<E> list, E ... elements) {
        Assert.assertEquals(elements.length, list.size());
        int index = 0;
        for (E e : list) {
            Assert.assertEquals(e, elements[index++]);
        }
    }
    
}
