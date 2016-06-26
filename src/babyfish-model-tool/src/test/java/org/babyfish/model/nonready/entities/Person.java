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
package org.babyfish.model.nonready.entities;

import org.babyfish.model.Model;
import org.babyfish.model.Scalar;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.spi.ObjectModelProvider;

@Model
public class Person {
    
    public static final int ADDRESS_ID = ModelClass.of(Person.class).getProperties().get("address").getId();
        
    @Scalar
    private String name;
    
    @Scalar(deferrable = true)
    private String address;
    
    @Scalar(deferrable = true)
    private byte[] image;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
    
    public boolean isAddressDisabled() {
        return ((ObjectModelProvider)this).objectModel().isDisabled(ADDRESS_ID);
    }
    
    public void disableAddress() {
        ((ObjectModelProvider)this).objectModel().disable(ADDRESS_ID);
    }
    
    public void enableAddress() {
        ((ObjectModelProvider)this).objectModel().enable(ADDRESS_ID);
    }
}
