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
package org.babyfish.hibernate.jpacriteria;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.babyfish.collection.HashSet;

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "EMPLOYEE")
@SequenceGenerator(
        name = "employeeSequence", 
        sequenceName = "EMPLOYEE_ID_SEQ", 
        initialValue = 1, 
        allocationSize = 1
)
public class Employee {

    @Id
    @Column(name = "EMPLOYEE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employeeSequence")
    private Long id;
    
    @Column(name = "NAME", length = 50, nullable = false)
    private String name;
    
    @Column(name = "SALARY")
    private int salary;
    
    @Temporal(TemporalType.DATE)
    private Date birthday;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department;
    
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private Set<AnnualLeave> annualLeaves;

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Set<AnnualLeave> getAnnualLeaves() {
        Set<AnnualLeave> annualLeaves = this.annualLeaves;
        if (annualLeaves == null) {
            this.annualLeaves = annualLeaves = new HashSet<>();
        }
        return annualLeaves;
    }
    
}
