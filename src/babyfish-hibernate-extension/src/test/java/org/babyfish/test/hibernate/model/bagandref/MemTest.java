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
package org.babyfish.test.hibernate.model.bagandref;

import java.util.Collection;
import java.util.Iterator;

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
        assertCollection(department.getEmployees());
        Assert.assertNull(employee.getDepartment());
        assertCollection(newDepartment.getEmployees());
        
        department.getEmployees().add(employee);
        assertCollection(department.getEmployees(), employee);
        Assert.assertEquals(department, employee.getDepartment());
        assertCollection(newDepartment.getEmployees());
        
        newDepartment.getEmployees().add(employee);
        assertCollection(department.getEmployees());
        Assert.assertEquals(newDepartment, employee.getDepartment());
        assertCollection(newDepartment.getEmployees(), employee);
        
        Iterator<Employee> itr = newDepartment.getEmployees().iterator();
        itr.next();
        itr.remove();
        assertCollection(department.getEmployees());
        Assert.assertNull(employee.getDepartment());
        assertCollection(newDepartment.getEmployees());
    }
    
    @Test
    public void testModifyEmployee() {
        
        Department department = new Department();
        Employee employee = new Employee();
        Department newDepartment = new Department();
        assertCollection(department.getEmployees());
        Assert.assertNull(employee.getDepartment());
        assertCollection(newDepartment.getEmployees());
        
        employee.setDepartment(department);
        assertCollection(department.getEmployees(), employee);
        Assert.assertEquals(department, employee.getDepartment());
        assertCollection(newDepartment.getEmployees());
        
        employee.setDepartment(newDepartment);
        assertCollection(department.getEmployees());
        Assert.assertEquals(newDepartment, employee.getDepartment());
        assertCollection(newDepartment.getEmployees(), employee);
        
        employee.setDepartment(null);
        assertCollection(department.getEmployees());
        Assert.assertNull(employee.getDepartment());
        assertCollection(newDepartment.getEmployees());
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertCollection(Collection<E> collection, E ... elements) {
        Assert.assertEquals(elements.length, collection.size());
        int index = 0;
        for (E e : collection) {
            Assert.assertEquals(elements[index++], e);
        }
    }
    
}
