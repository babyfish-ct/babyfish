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
package org.babyfish.model.classic.m2kr;

import java.util.Map;
import java.util.Map.Entry;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.model.classic.Utils;
import org.babyfish.model.classic.m2kr.entities.Department;
import org.babyfish.model.classic.m2kr.entities.Employee;
import org.junit.Test;

import junit.framework.Assert;

public class ModelTest {

    @Test
    public void testModifyDepartment() {
        Department department1 = new Department();
        Department department2 = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        department1.getEmployees().put("I", employee1);
        Utils.assertCollection(department1.getEmployees().keySet(), "I");
        Utils.assertCollection(department1.getEmployees().values(), employee1);
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        department1.getEmployees().put("II", employee2);
        Utils.assertCollection(department1.getEmployees().keySet(), "I", "II");
        Utils.assertCollection(department1.getEmployees().values(), employee1, employee2);
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        department2.getEmployees().put("III", employee1);
        Utils.assertCollection(department1.getEmployees().keySet(), "II");
        Utils.assertCollection(department1.getEmployees().values(), employee2);
        Utils.assertCollection(department2.getEmployees().keySet(), "III");
        Utils.assertCollection(department2.getEmployees().values(), employee1);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        department2.getEmployees().put("IV", employee2);
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet(), "III", "IV");
        Utils.assertCollection(department2.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        department2.getEmployees().keySet().remove("III");
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet(), "IV");
        Utils.assertCollection(department2.getEmployees().values(), employee2);
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        department2.getEmployees().values().remove(employee2);
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
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
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee1.setCodeInDepartment("I");
        employee1.setDepartment(department1);
        Utils.assertCollection(department1.getEmployees().keySet(), "I");
        Utils.assertCollection(department1.getEmployees().values(), employee1);
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee2.setCodeInDepartment("II");
        employee2.setDepartment(department1);
        Utils.assertCollection(department1.getEmployees().keySet(), "I", "II");
        Utils.assertCollection(department1.getEmployees().values(), employee1, employee2);
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee1.setCodeInDepartment("III");
        employee1.setDepartment(department2);
        Utils.assertCollection(department1.getEmployees().keySet(), "II");
        Utils.assertCollection(department1.getEmployees().values(), employee2);
        Utils.assertCollection(department2.getEmployees().keySet(), "III");
        Utils.assertCollection(department2.getEmployees().values(), employee1);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee2.setCodeInDepartment("IV");
        employee2.setDepartment(department2);
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet(), "III", "IV");
        Utils.assertCollection(department2.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee1.setCodeInDepartment(null);
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet(), "IV");
        Utils.assertCollection(department2.getEmployees().values(), employee2);
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee2.setCodeInDepartment(null);
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
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
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee1.setDepartment(department1);
        employee1.setCodeInDepartment("I");
        Utils.assertCollection(department1.getEmployees().keySet(), "I");
        Utils.assertCollection(department1.getEmployees().values(), employee1);
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee2.setDepartment(department1);
        employee2.setCodeInDepartment("II");
        Utils.assertCollection(department1.getEmployees().keySet(), "I", "II");
        Utils.assertCollection(department1.getEmployees().values(), employee1, employee2);
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee1.setDepartment(department2);
        employee1.setCodeInDepartment("III");
        Utils.assertCollection(department1.getEmployees().keySet(), "II");
        Utils.assertCollection(department1.getEmployees().values(), employee2);
        Utils.assertCollection(department2.getEmployees().keySet(), "III");
        Utils.assertCollection(department2.getEmployees().values(), employee1);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee2.setDepartment(department2);
        employee2.setCodeInDepartment("IV");
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet(), "III", "IV");
        Utils.assertCollection(department2.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee1.setDepartment(null);
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet(), "IV");
        Utils.assertCollection(department2.getEmployees().values(), employee2);
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee2.setDepartment(null);
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
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
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee1.setDepartment("I", department1);
        Utils.assertCollection(department1.getEmployees().keySet(), "I");
        Utils.assertCollection(department1.getEmployees().values(), employee1);
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        
        employee2.setDepartment("II", department1);
        Utils.assertCollection(department1.getEmployees().keySet(), "I", "II");
        Utils.assertCollection(department1.getEmployees().values(), employee1, employee2);
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department1, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee1.setDepartment("III", department2);
        Utils.assertCollection(department1.getEmployees().keySet(), "II");
        Utils.assertCollection(department1.getEmployees().values(), employee2);
        Utils.assertCollection(department2.getEmployees().keySet(), "III");
        Utils.assertCollection(department2.getEmployees().values(), employee1);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department1, employee2.getDepartment());
        
        employee2.setDepartment("IV", department2);
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet(), "III", "IV");
        Utils.assertCollection(department2.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department2, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee1.setDepartment(null, null);
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet(), "IV");
        Utils.assertCollection(department2.getEmployees().values(), employee2);
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department2, employee2.getDepartment());
        
        employee2.setDepartment(null, null);
        Utils.assertCollection(department1.getEmployees().keySet());
        Utils.assertCollection(department1.getEmployees().values());
        Utils.assertCollection(department2.getEmployees().keySet());
        Utils.assertCollection(department2.getEmployees().values());
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
        Utils.assertCollection(department.getEmployees().keySet(), "I", "II", "III");
        Utils.assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertEquals("III", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
        
        department.getEmployees().put("IV", employee2);
        Utils.assertCollection(department.getEmployees().keySet(), "I", "III", "IV");
        Utils.assertCollection(department.getEmployees().values(), employee1, employee3, employee2);
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertEquals("III", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
        
        department.getEmployees().put("III", employee1);
        Utils.assertCollection(department.getEmployees().keySet(), "III", "IV");
        Utils.assertCollection(department.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertNull(employee3.getCodeInDepartment());
        Assert.assertNull(employee3.getDepartment());
        
        department.getEmployees().put("III", employee2);
        Utils.assertCollection(department.getEmployees().keySet(), "III");
        Utils.assertCollection(department.getEmployees().values(), employee2);
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
        Utils.assertCollection(department.getEmployees().keySet(), "I", "II", "III");
        Utils.assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
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
        Utils.assertCollection(department.getEmployees().keySet(), "II", "IV");
        Utils.assertCollection(department.getEmployees().values(), employee1, employee3);
        Assert.assertEquals("II", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals("IV", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
    }
    
    @Test
    public void testChangeKeyBySetValueOfEntryOfDepartment() {
        Department department = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Employee employee3 = new Employee();
        department.getEmployees().put("I", employee1);
        department.getEmployees().put("II", employee2);
        department.getEmployees().put("III", employee3);
        Utils.assertCollection(department.getEmployees().keySet(), "I", "II", "III");
        Utils.assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertEquals("III", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
        
        findEntry(department.getEmployees(), "I").setValue(employee2);
        Utils.assertCollection(department.getEmployees().keySet(), "I", "III");
        Utils.assertCollection(department.getEmployees().values(), employee2, employee3);
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertEquals("I", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertEquals("III", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
        
        findEntry(department.getEmployees(), "I").setValue(employee3);
        Utils.assertCollection(department.getEmployees().keySet(), "I");
        Utils.assertCollection(department.getEmployees().values(), employee3);
        Assert.assertNull(employee1.getCodeInDepartment());
        Assert.assertNull(employee1.getDepartment());
        Assert.assertNull(employee2.getCodeInDepartment());
        Assert.assertNull(employee2.getDepartment());
        Assert.assertEquals("I", employee3.getCodeInDepartment());
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
        Utils.assertCollection(department.getEmployees().keySet(), "I", "II", "III");
        Utils.assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("II", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertEquals("III", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
        
        employee2.setCodeInDepartment("IV");
        Utils.assertCollection(department.getEmployees().keySet(), "I", "IV", "III");
        Utils.assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
        Assert.assertEquals("I", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertEquals("III", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
        
        employee1.setCodeInDepartment("III");
        Utils.assertCollection(department.getEmployees().keySet(), "III", "IV");
        Utils.assertCollection(department.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertNull(employee3.getCodeInDepartment());
        Assert.assertNull(employee3.getDepartment());
        
        //employee3 is still not a element of the map because it 
        //removed from it automatically in the previous step
        employee3.setCodeInDepartment("I");
        Utils.assertCollection(department.getEmployees().keySet(), "III", "IV");
        Utils.assertCollection(department.getEmployees().values(), employee1, employee2);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertNull(employee3.getCodeInDepartment());
        Assert.assertNull(employee3.getDepartment());
        
        employee3.setDepartment(department);
        Utils.assertCollection(department.getEmployees().keySet(), "III", "IV", "I");
        Utils.assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
        Assert.assertEquals("III", employee1.getCodeInDepartment());
        Assert.assertEquals(department, employee1.getDepartment());
        Assert.assertEquals("IV", employee2.getCodeInDepartment());
        Assert.assertEquals(department, employee2.getDepartment());
        Assert.assertEquals("I", employee3.getCodeInDepartment());
        Assert.assertEquals(department, employee3.getDepartment());
    }
    
    private static <K, V> Entry<K, V> findEntry(Map<K, V> map, K key) {
        for (Entry<K, V> entry : map.entrySet()) {
            if (key.equals(entry.getKey())) {
                return entry;
            }
        }
        return null;
    }
}
