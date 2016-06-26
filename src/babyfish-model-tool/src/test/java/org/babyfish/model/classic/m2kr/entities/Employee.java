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
package org.babyfish.model.classic.m2kr.entities;

import org.babyfish.model.Association;
import org.babyfish.model.KeyOf;
import org.babyfish.model.Model;

@Model
public class Employee {

    @KeyOf("department")
    private String codeInDepartment;
    
    @Association(opposite = "employees")
    private Department department;

    public String getCodeInDepartment() {
        return codeInDepartment;
    }

    public void setCodeInDepartment(String codeInDepartment) {
        this.codeInDepartment = codeInDepartment;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
    
    public void setDepartment(String codeInDepartment, Department department) {
        this.codeInDepartment = codeInDepartment;
        this.department = department;
    }
}
