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
package org.babyfish.model.embedded;

import junit.framework.Assert;

import org.babyfish.model.embedded.entities.Address;
import org.babyfish.model.embedded.entities.ContactInfo;
import org.babyfish.model.embedded.entities.Customer;
import org.babyfish.model.embedded.entities.Name;
import org.babyfish.model.embedded.entities.SalesSpecialist;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class SalesSpecialistTest {
    
    @Test
    public void testAddCustomerWithSameContactInfo() {
        SalesSpecialist salesSpecialist = new SalesSpecialist();
        Assert.assertEquals("[]", salesSpecialist.getCustomers().toString());
        
        customer(
                contactInfo("123456789", "a@x.com", address("ChengDu", 123)),
                name("Jim", "Green")
        ).setSalesSpecialist(salesSpecialist);
        Assert.assertEquals(
                "[" +
                    "{ " +
                        "name: { " +
                            "firstName: 'Jim', lastName: 'Green' " +
                        "}, " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'a@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 123 " +
                            "} " +
                        "} " +
                    "}" +
                "]", 
                salesSpecialist.getCustomers().toString()
        );
        
        customer(
                contactInfo("123456789", "a@x.com", address("ChengDu", 123)),
                name("Sam", "White")
        ).setSalesSpecialist(salesSpecialist);
        Assert.assertEquals(
                "[" +
                    "{ " +
                        "name: { " +
                            "firstName: 'Sam', lastName: 'White' " +
                        "}, " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'a@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 123 " +
                            "} " +
                        "} " +
                    "}" +
                "]", 
                salesSpecialist.getCustomers().toString()
        );
    }
    
    @Test
    public void testChangeScalar() {
        SalesSpecialist salesSpecialist = new SalesSpecialist();
        customer(
                contactInfo("123454321", "a@x.com", address("ChengDu", 123)),
                name("Jim", "Green")
        ).setSalesSpecialist(salesSpecialist);
        customer(
                contactInfo("123456789", "c@x.com", address("ShangHai", 321)),
                name("Sam", "White")
        ).setSalesSpecialist(salesSpecialist);
        Assert.assertEquals(
                "[" +
                    "{ " +
                        "name: { " +
                            "firstName: 'Jim', lastName: 'Green' " +
                        "}, " +
                        "contactInfo: { " +
                            "phone: '123454321', " +
                            "email: 'a@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 123 " +
                            "} " +
                        "} " +
                    "}, " +
                    "{ " +
                        "name: { " +
                            "firstName: 'Sam', lastName: 'White' " +
                        "}, " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'c@x.com', " +
                            "address: { " +
                                "city: 'ShangHai', " +
                                "streetNo: 321 " +
                            "} " +
                        "} " +
                    "}" +
                "]",
                salesSpecialist.getCustomers().toString()
        );
        salesSpecialist.getCustomers().iterator().next().setContactInfo(
                contactInfo("987654321", "x@x.com", address("BeiJing", 555))
        );
        Assert.assertEquals(
                "[" +
                    "{ " +
                        "name: { " +
                            "firstName: 'Sam', lastName: 'White' " +
                        "}, " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'c@x.com', " +
                            "address: { " +
                                "city: 'ShangHai', " +
                                "streetNo: 321 " +
                            "} " +
                        "} " +
                    "}, " +
                    "{ " +
                        "name: { " +
                            "firstName: 'Jim', lastName: 'Green' " +
                        "}, " +
                        "contactInfo: { " +
                            "phone: '987654321', " +
                            "email: 'x@x.com', " +
                            "address: { " +
                                "city: 'BeiJing', " +
                                "streetNo: 555 " +
                            "} " +
                        "} " +
                    "}" +
                "]",
                salesSpecialist.getCustomers().toString()
        );
    }
    
    @Test
    public void testChangeLevel1EmbeddedScalar() {
        SalesSpecialist salesSpecialist1 = new SalesSpecialist();
        SalesSpecialist salesSpecialist2 = new SalesSpecialist();
        ContactInfo sharedContactInfo = contactInfo("123454321", "a@x.com", address("ChengDu", 123));
        
        customer(
                sharedContactInfo,
                name("Jim", "Green")
        ).setSalesSpecialist(salesSpecialist1);
        customer(
                contactInfo("123456789", "c@x.com", address("ShangHai", 321)),
                name("Sam", "White")
        ).setSalesSpecialist(salesSpecialist1);
        customer(
                sharedContactInfo,
                name("Jim", "Green")
        ).setSalesSpecialist(salesSpecialist2);
        customer(
                contactInfo("123456789", "c@x.com", address("ShangHai", 321)),
                name("Sam", "White")
        ).setSalesSpecialist(salesSpecialist2);
        String unchangedCustomers = 
                "[" +
                    "{ " +
                        "name: { " +
                            "firstName: 'Jim', lastName: 'Green' " +
                        "}, " +
                        "contactInfo: { " +
                            "phone: '123454321', " +
                            "email: 'a@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 123 " +
                            "} " +
                        "} " +
                    "}, " +
                    "{ " +
                        "name: { " +
                            "firstName: 'Sam', lastName: 'White' " +
                        "}, " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'c@x.com', " +
                            "address: { " +
                                "city: 'ShangHai', " +
                                "streetNo: 321 " +
                            "} " +
                        "} " +
                    "}" +
                "]";
        Assert.assertEquals(unchangedCustomers, salesSpecialist1.getCustomers().toString());
        Assert.assertEquals(unchangedCustomers, salesSpecialist1.getCustomers().toString());
        
        sharedContactInfo.setPhone("987654321");
        String changedCustomers = 
                "[" +
                "{ " +
                    "name: { " +
                        "firstName: 'Sam', lastName: 'White' " +
                    "}, " +
                    "contactInfo: { " +
                        "phone: '123456789', " +
                        "email: 'c@x.com', " +
                        "address: { " +
                            "city: 'ShangHai', " +
                            "streetNo: 321 " +
                        "} " +
                    "} " +
                "}, " +
                "{ " +
                    "name: { " +
                        "firstName: 'Jim', lastName: 'Green' " +
                    "}, " +
                    "contactInfo: { " +
                        "phone: '987654321', " +
                        "email: 'a@x.com', " +
                        "address: { " +
                            "city: 'ChengDu', " +
                            "streetNo: 123 " +
                        "} " +
                    "} " +
                "}" +
            "]";
        Assert.assertEquals(changedCustomers, salesSpecialist1.getCustomers().toString());
        Assert.assertEquals(changedCustomers, salesSpecialist2.getCustomers().toString());
    }
    
    @Test
    public void testChangeLevel2EmbeddedScalar() {
        SalesSpecialist salesSpecialist1 = new SalesSpecialist();
        SalesSpecialist salesSpecialist2 = new SalesSpecialist();
        Address sharedAddress = address("ChengDu", 120);
        
        customer(
                contactInfo("123456789", "x@x.com", sharedAddress),
                name("Jim", "Green")
        ).setSalesSpecialist(salesSpecialist1);
        customer(
                contactInfo("123456789", "x@x.com", address("ChengDu", 140)),
                name("Sam", "White")
        ).setSalesSpecialist(salesSpecialist1);
        customer(
                contactInfo("123456789", "x@x.com", sharedAddress),
                name("Jim", "Green")
        ).setSalesSpecialist(salesSpecialist2);
        customer(
                contactInfo("123456789", "x@x.com", address("ChengDu", 140)),
                name("Sam", "White")
        ).setSalesSpecialist(salesSpecialist2);
        String unchangedCustomers = 
                "[" +
                    "{ " +
                        "name: { " +
                            "firstName: 'Jim', lastName: 'Green' " +
                        "}, " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'x@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 120 " +
                            "} " +
                        "} " +
                    "}, " +
                    "{ " +
                        "name: { " +
                            "firstName: 'Sam', lastName: 'White' " +
                        "}, " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'x@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 140 " +
                            "} " +
                        "} " +
                    "}" +
                "]";
        Assert.assertEquals(unchangedCustomers, salesSpecialist1.getCustomers().toString());
        Assert.assertEquals(unchangedCustomers, salesSpecialist2.getCustomers().toString());
        
        sharedAddress.setStreetNo(200);
        String changedCustomers = 
                "[" +
                    "{ " +
                        "name: { " +
                            "firstName: 'Sam', lastName: 'White' " +
                        "}, " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'x@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 140 " +
                            "} " +
                        "} " +
                    "}, " +
                    "{ " +
                        "name: { " +
                            "firstName: 'Jim', lastName: 'Green' " +
                        "}, " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'x@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 200 " +
                            "} " +
                        "} " +
                    "}" +
                "]";
        Assert.assertEquals(changedCustomers, salesSpecialist1.getCustomers().toString());
        Assert.assertEquals(changedCustomers, salesSpecialist2.getCustomers().toString());
    }

    private static ContactInfo contactInfo(String phone, String email, Address address) {
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setPhone(phone);
        contactInfo.setEmail(email);
        contactInfo.setAddress(address);
        return contactInfo;
    }
    
    private static Address address(String city, int streetNo) {
        Address address = new Address();
        address.setCity(city);
        address.setStreetNo(streetNo);
        return address;
    }
    
    private static Name name(String firstName, String lastName) {
        Name name = new Name();
        name.setFirstName(firstName);
        name.setLastName(lastName);
        return name;
    }
    
    private static Customer customer(ContactInfo contactInfo, Name name) {
        Customer customer = new Customer();
        customer.setContactInfo(contactInfo);
        customer.setName(name);
        return customer;
    }
}
