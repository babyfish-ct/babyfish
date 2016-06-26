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

import java.util.NavigableSet;

import org.babyfish.model.Association;
import org.babyfish.model.ComparatorProperty;
import org.babyfish.model.ComparatorRule;
import org.babyfish.model.Model;
import org.babyfish.model.Scalar;

/**
 * @author Tao Chen
 */
@Model // Using ObjectModel4Java, requires compilation-time byte code instrument
public class Department {
 
    @Scalar
    private String name;
    
    /*
     * Use 
     * @ReferenceComparisonRule("firstName, lastName")
     * to specify the
     * "org.babyfish.collection.ForzenComparator" to this SortedSet
     * so that 
     * 
     * (1) This collection uses the property "firstName" and "lastName" 
     * to  compare two Employee objects. 
     * (2) The employee objects are consider as "Unstable Collection Elements" 
     * by this collection so that this collection will be adjusted automatically 
     * when the property "firstName" or "lastName" of those employee objects 
     * are changed by the program.
     */
    @ComparatorRule(properties = { 
            @ComparatorProperty(name = "firstName"), 
            @ComparatorProperty(name = "lastName")
    })
    @Association(opposite = "department")
    private NavigableSet<Employee> employees;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NavigableSet<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(NavigableSet<Employee> employees) {
        this.employees = employees;
    }
}
