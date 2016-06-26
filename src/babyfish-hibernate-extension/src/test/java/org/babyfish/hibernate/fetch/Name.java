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
package org.babyfish.hibernate.fetch;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.babyfish.model.jpa.JPAModel;
import org.babyfish.model.metadata.ModelClass;

/**
 * @author Tao Chen
 */
@JPAModel
@Embeddable
public class Name implements Comparable<Name> {
    
    private static final ModelClass MODEL_CLASS = ModelClass.of(Name.class);
    
    @Column(name = "FIRST_NAME", length = 20)
    private String firstName;

    @Column(name = "LAST_NAME", length = 20)
    private String lastName;
    
    public Name() {}

    public Name(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "{ firstName: " +
                this.firstName +
                ", lastName: " +
                this.lastName +
                "}";
    }

    @Override
    public int hashCode() {
        return MODEL_CLASS.getDefaultEqualityComparator().hashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return MODEL_CLASS.getDefaultEqualityComparator().equals(this, obj);
    }

    @Override
    public int compareTo(Name o) {
        return MODEL_CLASS.getDefaultComparator().compare(this, o);
    }
}
