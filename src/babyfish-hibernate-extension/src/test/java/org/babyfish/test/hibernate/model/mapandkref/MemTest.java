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
package org.babyfish.test.hibernate.model.mapandkref;

import java.util.Collection;
import java.util.Map;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashMap;
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
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        department1.getEmployees().put("I", employee1);
        assertCollection(department1.getEmployees().keySet(), "I");
        assertCollection(department1.getEmployees().values(), employee1);
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        department1.getEmployees().put("II", employee2);
        assertCollection(department1.getEmployees().keySet(), "I", "II");
        assertCollection(department1.getEmployees().values(), employee1, employee2);
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        department2.getEmployees().put("III", employee1);
        assertCollection(department1.getEmployees().keySet(), "II");
        assertCollection(department1.getEmployees().values(), employee2);
        assertCollection(department2.getEmployees().keySet(), "III");
        assertCollection(department2.getEmployees().values(), employee1);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        department2.getEmployees().put("IV", employee2);
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet(), "III", "IV");
        assertCollection(department2.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        department2.getEmployees().keySet().remove("III");
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet(), "IV");
        assertCollection(department2.getEmployees().values(), employee2);
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        department2.getEmployees().values().remove(employee2);
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
    }
    
    @Test
    public void testModifyKeyAndValueOfEmployee() {
        Department department1 = new Department();
        Department department2 = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee1.setCodeInDepartment("I");
        employee1.setDepartment(department1);
        assertCollection(department1.getEmployees().keySet(), "I");
        assertCollection(department1.getEmployees().values(), employee1);
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee2.setCodeInDepartment("II");
        employee2.setDepartment(department1);
        assertCollection(department1.getEmployees().keySet(), "I", "II");
        assertCollection(department1.getEmployees().values(), employee1, employee2);
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee1.setCodeInDepartment("III");
        employee1.setDepartment(department2);
        assertCollection(department1.getEmployees().keySet(), "II");
        assertCollection(department1.getEmployees().values(), employee2);
        assertCollection(department2.getEmployees().keySet(), "III");
        assertCollection(department2.getEmployees().values(), employee1);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee2.setCodeInDepartment("IV");
        employee2.setDepartment(department2);
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet(), "III", "IV");
        assertCollection(department2.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee1.setCodeInDepartment(null);
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet(), "IV");
        assertCollection(department2.getEmployees().values(), employee2);
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee2.setCodeInDepartment(null);
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
    }
    
    @Test
    public void testModifyValueAndKeyOfEmployee() {
        Department department1 = new Department();
        Department department2 = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee1.setDepartment(department1);
        employee1.setCodeInDepartment("I");
        assertCollection(department1.getEmployees().keySet(), "I");
        assertCollection(department1.getEmployees().values(), employee1);
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee2.setDepartment(department1);
        employee2.setCodeInDepartment("II");
        assertCollection(department1.getEmployees().keySet(), "I", "II");
        assertCollection(department1.getEmployees().values(), employee1, employee2);
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee1.setDepartment(department2);
        employee1.setCodeInDepartment("III");
        assertCollection(department1.getEmployees().keySet(), "II");
        assertCollection(department1.getEmployees().values(), employee2);
        assertCollection(department2.getEmployees().keySet(), "III");
        assertCollection(department2.getEmployees().values(), employee1);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee2.setDepartment(department2);
        employee2.setCodeInDepartment("IV");
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet(), "III", "IV");
        assertCollection(department2.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee1.setDepartment(null);
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet(), "IV");
        assertCollection(department2.getEmployees().values(), employee2);
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee2.setDepartment(null);
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
    }
    
    @Test
    public void testModifyEmployee() {
        Department department1 = new Department();
        Department department2 = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee1.setDepartment("I", department1);
        assertCollection(department1.getEmployees().keySet(), "I");
        assertCollection(department1.getEmployees().values(), employee1);
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee2.setDepartment("II", department1);
        assertCollection(department1.getEmployees().keySet(), "I", "II");
        assertCollection(department1.getEmployees().values(), employee1, employee2);
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee1.setDepartment("III", department2);
        assertCollection(department1.getEmployees().keySet(), "II");
        assertCollection(department1.getEmployees().values(), employee2);
        assertCollection(department2.getEmployees().keySet(), "III");
        assertCollection(department2.getEmployees().values(), employee1);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee2.setDepartment("IV", department2);
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet(), "III", "IV");
        assertCollection(department2.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee1.setDepartment(null, null);
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet(), "IV");
        assertCollection(department2.getEmployees().values(), employee2);
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee2.setDepartment(null, null);
        assertCollection(department1.getEmployees().keySet());
        assertCollection(department1.getEmployees().values());
        assertCollection(department2.getEmployees().keySet());
        assertCollection(department2.getEmployees().values());
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
    }
    
    @Test
    public void testChangeKeyByDepartment() {
        
        Department department = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Employee employee3 = new Employee();
        department.getEmployees().put("I", employee1);
        department.getEmployees().put("II", employee2);
        department.getEmployees().put("III", employee3);
        assertCollection(department.getEmployees().keySet(), "I", "II", "III");
        assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertEquals("III", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
        
        department.getEmployees().put("IV", employee2);
        assertCollection(department.getEmployees().keySet(), "I", "III", "IV");
        assertCollection(department.getEmployees().values(), employee1, employee3, employee2);
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertEquals("III", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
        
        department.getEmployees().put("III", employee1);
        assertCollection(department.getEmployees().keySet(), "III", "IV");
        assertCollection(department.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertNull(employee3.getCodeInDepartment());
        Assert.assertNull(employee3.getDepartment());
        
        department.getEmployees().put("III", employee2);
        assertCollection(department.getEmployees().keySet(), "III");
        assertCollection(department.getEmployees().values(), employee2);
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals("III", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertNull(employee3.getCodeInDepartment());
        Assert.assertNull(employee3.getDepartment());
    }
    
    @Test
    public void testBatchChangeKeyByDepartment() {
        Department department = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Employee employee3 = new Employee();
        department.getEmployees().put("I", employee1);
        department.getEmployees().put("II", employee2);
        department.getEmployees().put("III", employee3);
        assertCollection(department.getEmployees().keySet(), "I", "II", "III");
        assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertEquals("III", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
        
        Map<String, Employee> map = new LinkedHashMap<String, Employee>();
        map.put("I", employee1);
        map.put("II", employee1);
        map.put("IV", employee3);
        department.getEmployees().putAll(map);
        assertCollection(department.getEmployees().keySet(), "II", "IV");
        assertCollection(department.getEmployees().values(), employee1, employee3);
        Assert.assertEquals("II", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals("IV", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
    }
    
    @Test
    public void testChangeKeyByEmployee() {
        
        Department department = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Employee employee3 = new Employee();
        department.getEmployees().put("I", employee1);
        department.getEmployees().put("II", employee2);
        department.getEmployees().put("III", employee3);
        assertCollection(department.getEmployees().keySet(), "I", "II", "III");
        assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertEquals("III", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
        
        employee2.setCodeInDepartment("IV");
        assertCollection(department.getEmployees().keySet(), "I", "IV", "III");
        assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertEquals("III", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
        
        employee1.setCodeInDepartment("III");
        assertCollection(department.getEmployees().keySet(), "III", "IV");
        assertCollection(department.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertNull(employee3.getCodeInDepartment());
        Assert.assertNull(employee3.getDepartment());
        
        //employee3 is still not a element of the map because it 
        //removed from it automatically in the previous step
        employee3.setCodeInDepartment("I");
        assertCollection(department.getEmployees().keySet(), "III", "IV");
        assertCollection(department.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertNull(employee3.getCodeInDepartment());
        Assert.assertNull(employee3.getDepartment());
        
        employee3.setDepartment(department);
        assertCollection(department.getEmployees().keySet(), "III", "IV", "I");
        assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertEquals("I", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertCollection(Collection<E> c, E ... elements) {
        if (elements.length == 0) {
            Assert.assertTrue(c.isEmpty());
        } else {
            Collection<E> other = new ArrayList<E>();
            for (int i = elements.length - 1; i >= 0; i--) {
                other.add(elements[i]);
            }
            Assert.assertEquals(other.size(), c.size());
            Assert.assertTrue(other.containsAll(c));
            Assert.assertTrue(c.containsAll(other));
        }
    }
    
}
