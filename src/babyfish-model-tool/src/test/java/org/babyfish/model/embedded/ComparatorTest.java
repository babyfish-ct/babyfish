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

import java.util.Comparator;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.model.embedded.entities.Address;
import org.babyfish.model.embedded.entities.ContactInfo;
import org.babyfish.model.embedded.entities.Customer;
import org.babyfish.model.metadata.ModelClass;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class ComparatorTest {

    private static final EqualityComparator<Customer> EQUALITY_COMPARATOR;
    
    private static final Comparator<Customer> COMPARATOR;
    
    @Test
    public void testEquals() {
        Customer a = customer("12345678", "c@gmail.com", "ChengDu", 345);
        Customer b = customer("12345678", "c@gmail.com", "ChengDu", 345);
        Assert.assertNotSame(a, b);
        Assert.assertNotSame(a.getContactInfo(), b.getContactInfo());
        Assert.assertNotSame(a.getContactInfo().getAddress(), b.getContactInfo().getAddress());
        Assert.assertEquals(EQUALITY_COMPARATOR.hashCode(a), EQUALITY_COMPARATOR.hashCode(b));
        Assert.assertTrue(EQUALITY_COMPARATOR.equals(a, b));
        Assert.assertEquals(0, COMPARATOR.compare(a, b));
        Assert.assertEquals(0, COMPARATOR.compare(b, a));
    }
    
    @Test
    public void testNotEqualByPhone() {
        Customer a = customer("12345670", "c@gmail.com", "ChengDu", 345);
        Customer b = customer("12345678", "c@gmail.com", "ChengDu", 345);
        Assert.assertNotSame(a, b);
        Assert.assertNotSame(a.getContactInfo(), b.getContactInfo());
        Assert.assertNotSame(a.getContactInfo().getAddress(), b.getContactInfo().getAddress());
        Assert.assertTrue(EQUALITY_COMPARATOR.hashCode(a) != EQUALITY_COMPARATOR.hashCode(b));
        Assert.assertTrue(!EQUALITY_COMPARATOR.equals(a, b));
        Assert.assertTrue(COMPARATOR.compare(a, b) < 0);
        Assert.assertTrue(COMPARATOR.compare(b, a) > 0);
    }
    
    @Test
    public void testNotEqualByEmail() {
        Customer a = customer("12345678", "a@gmail.com", "ChengDu", 345);
        Customer b = customer("12345678", "c@gmail.com", "ChengDu", 345);
        Assert.assertNotSame(a, b);
        Assert.assertNotSame(a.getContactInfo(), b.getContactInfo());
        Assert.assertNotSame(a.getContactInfo().getAddress(), b.getContactInfo().getAddress());
        Assert.assertTrue(EQUALITY_COMPARATOR.hashCode(a) != EQUALITY_COMPARATOR.hashCode(b));
        Assert.assertTrue(!EQUALITY_COMPARATOR.equals(a, b));
        Assert.assertTrue(COMPARATOR.compare(a, b) < 0);
        Assert.assertTrue(COMPARATOR.compare(b, a) > 0);
    }
    
    @Test
    public void testNotEqualByCity() {
        Customer a = customer("12345678", "c@gmail.com", "BeiJing", 345);
        Customer b = customer("12345678", "c@gmail.com", "ChengDu", 345);
        Assert.assertNotSame(a, b);
        Assert.assertNotSame(a.getContactInfo(), b.getContactInfo());
        Assert.assertNotSame(a.getContactInfo().getAddress(), b.getContactInfo().getAddress());
        Assert.assertTrue(EQUALITY_COMPARATOR.hashCode(a) != EQUALITY_COMPARATOR.hashCode(b));
        Assert.assertTrue(!EQUALITY_COMPARATOR.equals(a, b));
        Assert.assertTrue(COMPARATOR.compare(a, b) < 0);
        Assert.assertTrue(COMPARATOR.compare(b, a) > 0);
    }
    
    @Test
    public void testNotEqualByStreetNo() {
        Customer a = customer("12345678", "c@gmail.com", "ChengDu", 340);
        Customer b = customer("12345678", "c@gmail.com", "ChengDu", 345);
        Assert.assertNotSame(a, b);
        Assert.assertNotSame(a.getContactInfo(), b.getContactInfo());
        Assert.assertNotSame(a.getContactInfo().getAddress(), b.getContactInfo().getAddress());
        Assert.assertTrue(EQUALITY_COMPARATOR.hashCode(a) != EQUALITY_COMPARATOR.hashCode(b));
        Assert.assertTrue(!EQUALITY_COMPARATOR.equals(a, b));
        Assert.assertTrue(COMPARATOR.compare(a, b) < 0);
        Assert.assertTrue(COMPARATOR.compare(b, a) > 0);
    }
    
    private static Customer customer(String phone, String email, String city, int streetNo) {
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setPhone(phone);
        contactInfo.setEmail(email);
        Address address = new Address();
        address.setCity(city);
        address.setStreetNo(streetNo);
        contactInfo.setAddress(address);
        Customer customer = new Customer();
        customer.setContactInfo(contactInfo);
        return customer;
    }
    
    static {
        EQUALITY_COMPARATOR = ModelClass.of(Customer.class).getEqualityComparator("contactInfo");
        COMPARATOR = ModelClass.of(Customer.class).getComparator("contactInfo");
    }
}
