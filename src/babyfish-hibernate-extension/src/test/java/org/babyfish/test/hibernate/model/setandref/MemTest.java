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
package org.babyfish.test.hibernate.model.setandref;

import java.util.Iterator;
import java.util.Set;

import org.babyfish.collection.HashSet;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class MemTest {

    @Test
    public void testModifyDepartment() {
        
        Department department = new Department();
        Employee employee = new Employee();
        Department newDepartment = new Department();
        assertSet(department.getEmployees());
        Assert.assertNull(employee.getDepartment());
        assertSet(newDepartment.getEmployees());
        
        department.getEmployees().add(employee);
        assertSet(department.getEmployees(), employee);
        Assert.assertEquals(department, employee.getDepartment());
        assertSet(newDepartment.getEmployees());
        
        newDepartment.getEmployees().add(employee);
        assertSet(department.getEmployees());
        Assert.assertEquals(newDepartment, employee.getDepartment());
        assertSet(newDepartment.getEmployees(), employee);
        
        Iterator<Employee> itr = newDepartment.getEmployees().iterator();
        itr.next();
        itr.remove();
        assertSet(department.getEmployees());
        Assert.assertNull(employee.getDepartment());
        assertSet(newDepartment.getEmployees());
    }
    
    @Test
    public void testModifyEmployee() {
        
        Department department = new Department();
        Employee employee = new Employee();
        Department newDepartment = new Department();
        assertSet(department.getEmployees());
        Assert.assertNull(employee.getDepartment());
        assertSet(newDepartment.getEmployees());
        
        employee.setDepartment(department);
        assertSet(department.getEmployees(), employee);
        Assert.assertEquals(department, employee.getDepartment());
        assertSet(newDepartment.getEmployees());
        
        employee.setDepartment(newDepartment);
        assertSet(department.getEmployees());
        Assert.assertEquals(newDepartment, employee.getDepartment());
        assertSet(newDepartment.getEmployees(), employee);
        
        employee.setDepartment(null);
        assertSet(department.getEmployees());
        Assert.assertNull(employee.getDepartment());
        assertSet(newDepartment.getEmployees());
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertSet(Set<E> set, E ... elements) {
        Set<E> other = new HashSet<E>();
        for (E element : elements) {
            other.add(element);
        }
        Assert.assertEquals(other, set);
    }
    
}
