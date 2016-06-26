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
package org.babyfish.model.data.entities;

import java.io.Serializable;

import org.babyfish.lang.I18N;
import org.babyfish.model.ComparatorProperty;
import org.babyfish.model.ComparatorRule;
import org.babyfish.model.Model;
import org.babyfish.model.ModelType;
import org.babyfish.model.Scalar;
import org.babyfish.model.StringComparatorType;

@Model(type = ModelType.EMBEDDABLE)
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

    private static final long serialVersionUID = -5319419764528891888L;

    @Scalar
    private String firstName;
    
    @Scalar
    private String lastName;
    
    public FullName(String firstName, String lastName) {
        this.setFirstName(firstName);
        this.setLastName(lastName);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName != null && firstName.indexOf(' ') != -1) {
            throw new IllegalArgumentException(mustNotContainSpace("firstName", firstName));
        }
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName != null && lastName.indexOf(' ') != -1) {
            throw new IllegalArgumentException(mustNotContainSpace("lastName", lastName));
        }
        this.lastName = lastName;
    }

    @I18N
    private static native String mustNotContainSpace(String argumentName, String argumentValue);
}
