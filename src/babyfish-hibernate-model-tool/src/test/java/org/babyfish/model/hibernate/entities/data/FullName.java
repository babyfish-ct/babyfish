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
package org.babyfish.model.hibernate.entities.data;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.babyfish.model.ComparatorProperty;
import org.babyfish.model.ComparatorRule;
import org.babyfish.model.StringComparatorType;
import org.babyfish.model.jpa.JPAModel;

@JPAModel
@Embeddable
@ComparatorRule(properties = {
        @ComparatorProperty(
                name = "firstName",
                stringComparatorType = StringComparatorType.INSENSITIVE
        ),
        @ComparatorProperty(
                name = "lastName",
                stringComparatorType = StringComparatorType.INSENSITIVE
        ),
})
public class FullName implements Serializable {

    private static final long serialVersionUID = 8902091858951992527L;

    private String firstName;
    
    private String lastName;
    
    public FullName() {}

    public FullName(String firstName, String lastName) {
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
}
