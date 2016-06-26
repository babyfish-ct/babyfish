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
package org.babyfish.test.hibernate.model.osetandnset;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
@Table(name = "osns_COURSE")
@SequenceGenerator(
        name = "courseSequence",
        sequenceName = "osns_COURSE_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Course {
    
    @Id
    @Column(name = "COURSE_ID")
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "courseSequence"
    )
    private Long id;
    
    @Column(name = "NAME")
    private String name;
    
    @Navigable
    @ManyToMany
    @JoinTable(
            name = "osns_COURSE_STUDENT",
            joinColumns = @JoinColumn(name = "COURSE_ID", referencedColumnName = "COURSE_ID"),
            inverseJoinColumns = @JoinColumn(name = "STUDENT_ID", referencedColumnName = "STUDENT_ID")
    )
    @ComparatorRule(properties = @ComparatorProperty(name = "name"))
    private Set<Student> students;
    
    public Course() {}
    
    public Course(String name) {
        this.name = name;
    }

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

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }
}
