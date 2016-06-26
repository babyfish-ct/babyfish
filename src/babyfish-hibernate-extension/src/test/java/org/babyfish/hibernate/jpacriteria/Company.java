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
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.collection.HashSet;

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "COMPANY")
@SequenceGenerator(
        name = "companySequence", 
        sequenceName = "COMPANY_ID_SEQ", 
        initialValue = 1, 
        allocationSize = 1
)
public class Company {

    @Id
    @Column(name = "COMPANY_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "companySequence")
    private Long id;
    
    @Column(name = "NAME", length = 50, nullable = false)
    private String name;
    
    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private Set<Department> departments;
    
    @ManyToMany(mappedBy = "companies", fetch = FetchType.LAZY)
    private Set<Investor> investors;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Department> getDepartments() {
        Set<Department> departments = this.departments;
        if (departments == null) {
            this.departments = departments = new HashSet<>();
        }
        return departments;
    }

    public Set<Investor> getInvestors() {
        Set<Investor> investors = this.investors;
        if (investors == null) {
            this.investors = investors = new HashSet<>();
        }
        return investors;
    }
    
    
}
