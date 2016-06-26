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
package org.babyfish.model.embedded.entities;

import java.util.NavigableSet;

import org.babyfish.model.Association;
import org.babyfish.model.ComparatorProperty;
import org.babyfish.model.ComparatorRule;
import org.babyfish.model.Model;

/**
 * @author Tao Chen
 */
@Model
public class SalesSpecialist {

    @Association(opposite = "salesSpecialist")
    @ComparatorRule(properties = @ComparatorProperty(name = "contactInfo"))
    private NavigableSet<Customer> customers;

    public NavigableSet<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(NavigableSet<Customer> customers) {
        this.customers = customers;
    }
}
