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
package org.babyfish.model.classic.l2ir.entities;

import org.babyfish.model.Association;
import org.babyfish.model.IndexOf;
import org.babyfish.model.Model;

@Model
public class Employee {

    @IndexOf("department")
    private int indexInDepartment;
    
    @Association(opposite = "employees")
    private Department department;

    public int getIndexInDepartment() {
        return indexInDepartment;
    }

    public void setIndexInDepartment(int indexInDepartment) {
        this.indexInDepartment = indexInDepartment;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
    
    public void setDepartment(int indexInDepartment, Department department) {
        this.indexInDepartment = indexInDepartment;
        this.department = department;
    }
}
