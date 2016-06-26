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
import java.util.Set;

import org.babyfish.model.Association;
import org.babyfish.model.ComparatorProperty;
import org.babyfish.model.ComparatorRule;
import org.babyfish.model.Model;
import org.babyfish.model.Scalar;

@Model
public class Investor implements Serializable {

    private static final long serialVersionUID = 7236391974767971387L;

    @Scalar
    private FullName fullName;
    
    @Association(opposite = "investors")
    @ComparatorRule(properties = @ComparatorProperty(name = "name"))
    private Set<Company> companies;

    public FullName getFullName() {
        return fullName;
    }

    public void setFullName(FullName fullName) {
        this.fullName = fullName;
    }

    public Set<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(Set<Company> companies) {
        this.companies = companies;
    }
}
