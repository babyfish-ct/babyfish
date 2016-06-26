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
package org.babyfishdemo.om4jpa.entities.r2r;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.model.jpa.JPAModel;

@JPAModel // Using ObjectModel4JPA, requires compilation-time bytecode instrument
@Entity
@Table(name = "r2r_PERSON")
@SequenceGenerator(
        name = "personSequence",
        sequenceName = "r2r_PERSON_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Person {

    @Id
    @Column(name = "PERSON_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "personSequence")
    private Long id;
    
    @Column(name = "NAME", length = 50, nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GRID_FRIEND_ID")
    private Person girlFriend;
    
    @OneToOne(
            mappedBy = "girlFriend",
            optional = true,
            
            // Emphasize the fetch is EAGER!
            // When @OneToOne uses optional="true", FetchType.LAZY will be ignored. T_T
            fetch = FetchType.LAZY
    )
    private Person boyFriend;

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

    public Person getGirlFriend() {
        return girlFriend;
    }

    public void setGirlFriend(Person girlFriend) {
        this.girlFriend = girlFriend;
    }

    public Person getBoyFriend() {
        return boyFriend;
    }

    public void setBoyFriend(Person boyFriend) {
        this.boyFriend = boyFriend;
    }
}
