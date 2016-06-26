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
package org.babyfishdemo.spring.entities;

import java.util.Date;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.babyfish.model.jpa.JPAModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * @author Tao Chen
 */
@JPAModel
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
    @Column(name = "EMPLOYEE_ID", nullable = false)
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE, 
            generator = "employeeSequence"
    )
    private Long id;
    
    @Version
    @Column(name = "VERSION", nullable = false)
    private int version;
    
    @Embedded
    private Name name;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "GENDER")
    private Gender gender;
    
    @Column(name = "BIRTHDAY", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date birthday;
    
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;
    
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "IMAGE", nullable = false)
    private byte[] image;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department;
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL) //JPA cascade
    @OnDelete(action = OnDeleteAction.CASCADE) //database cascade
    private Set<AnnualLeave> annualLeaves;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUPERVISOR_ID")
    private Employee supervisor;
    
    @OneToMany(mappedBy = "supervisor")
    private Set<Employee> subordinates;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Set<AnnualLeave> getAnnualLeaves() {
        return annualLeaves;
    }

    public void setAnnualLeaves(Set<AnnualLeave> annualLeaves) {
        this.annualLeaves = annualLeaves;
    }
    
    public Employee getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Employee supervisor) {
        this.supervisor = supervisor;
    }

    public Set<Employee> getSubordinates() {
        return subordinates;
    }

    public void setSubordinates(Set<Employee> subordinates) {
        this.subordinates = subordinates;
    }

    public static enum Gender {
        MALE,
        FEMALE
    }
}
