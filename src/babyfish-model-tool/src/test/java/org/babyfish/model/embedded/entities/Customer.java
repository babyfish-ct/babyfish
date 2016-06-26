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

import org.babyfish.model.Association;
import org.babyfish.model.Model;
import org.babyfish.model.Scalar;

/**
 * @author Tao Chen
 */
@Model
public class Customer {
    
    @Scalar
    private Name name;
    
    @Scalar
    private ContactInfo contactInfo;
    
    @Association(opposite = "customers")
    private SalesSpecialist salesSpecialist;

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public SalesSpecialist getSalesSpecialist() {
        return salesSpecialist;
    }

    public void setSalesSpecialist(SalesSpecialist salesSpecialist) {
        this.salesSpecialist = salesSpecialist;
    }
    
    @Override
    public String toString() {
        return 
                "{ name: " +
                this.name +
                ", contactInfo: " +
                this.contactInfo +
                " }";
    }
}
