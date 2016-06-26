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
package org.babyfishdemo.om4jpa.entities.navigable;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.model.ComparatorProperty;
import org.babyfish.model.ComparatorRule;
import org.babyfish.model.Navigable;
import org.babyfish.model.jpa.JPAModel;

/**
 * @author Tao Chen
 */
@JPAModel
@Entity
@Table(name = "m2s_COMPANY")
@SequenceGenerator(
        name = "companySequence",
        sequenceName = "m2s_COMPANY_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Company {

    @Id
    @Column(name = "COMPANY_ID")
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE, 
            generator = "companySequence"
    )
    private Long id;
    
    @Column(name = "NAME")
    private String name;
    
    @Navigable
    @ComparatorRule(properties = @ComparatorProperty(name = "name"))
    @ManyToMany
    @MapKeyColumn(name = "PET_NAME")
    @JoinTable(
            name = "COMPANY_INVESTOR_MAPPING",
            joinColumns = @JoinColumn(name = "COMPANY_ID"),
            inverseJoinColumns = @JoinColumn(name = "INVESTOR_ID")
    )
    private Map<String, Investor> investors;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Investor> getInvestors() {
        return investors;
    }

    public void setInvestors(Map<String, Investor> investors) {
        this.investors = investors;
    }
}
