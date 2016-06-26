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

import org.babyfish.collection.HashSet;

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "DEPARTMENT")
@SequenceGenerator(
        name = "departmentSequence", 
        sequenceName = "DEPARTMENT_ID_SEQ", 
        initialValue = 1, 
        allocationSize = 1
)
public class Department {

    @Id
    @Column(name = "DEPARTMENT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "departmentSequence")
    private Long id;
    
    @Column(name = "NAME", length = 50, nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID", nullable = false)
    private Company company;
    
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private Set<Employee> employees;
    
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private Set<Office> offices;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<Employee> getEmployees() {
        Set<Employee> employees = this.employees;
        if (employees == null) {
            this.employees = employees = new HashSet<>();
        }
        return employees;
    }

    public Set<Office> getOffices() {
        Set<Office> offices = this.offices;
        if (offices == null) {
            this.offices = offices = new HashSet<>();
        }
        return offices;
    }
}
