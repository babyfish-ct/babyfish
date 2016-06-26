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
import org.babyfish.model.embedded.entities.Company;
import org.babyfish.model.embedded.entities.ContactInfo;
import org.babyfish.model.embedded.entities.Site;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class CompanyTest {
    
    @Test
    public void testChangeScalar() {
        Company company = new Company();
        site("ChengDu Site", contactInfo("123454321", "c@x.com", address("ChengDu", 100))).setCompany(company);
        site("BeiJing Site", contactInfo("123456789", "b@x.com", address("BeiJing", 200))).setCompany(company);
        String unchangedSites =
                "{" +
                    "{ " +
                        "phone: '123454321', " +
                        "email: 'c@x.com', " +
                        "address: { " +
                            "city: 'ChengDu', " +
                            "streetNo: 100 " +
                        "} " +
                    "}" +
                    "=" +
                    "{ " +
                        "name: 'ChengDu Site', " +
                        "contactInfo: { " +
                            "phone: '123454321', " +
                            "email: 'c@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 100 " +
                            "} " +
                        "} " +
                    "}, " +
                    "{ " +
                        "phone: '123456789', " +
                        "email: 'b@x.com', " +
                        "address: { " +
                            "city: 'BeiJing', " +
                            "streetNo: 200 " +
                        "} " +
                    "}" +
                    "=" +
                    "{ " +
                        "name: 'BeiJing Site', " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'b@x.com', " +
                            "address: { " +
                                "city: 'BeiJing', " +
                                "streetNo: 200 " +
                            "} " +
                        "} " +
                    "}" +
                "}";
        Assert.assertEquals(unchangedSites, company.getSites().toString());
        
        company
        .getSites()
        .values()
        .iterator()
        .next()
        .setContactInfo(contactInfo("987654321", "c@x.com", address("ChengDu", 100)));
        
        String changedSites =
                "{" +
                    "{ " +
                        "phone: '123456789', " +
                        "email: 'b@x.com', " +
                        "address: { " +
                            "city: 'BeiJing', " +
                            "streetNo: 200 " +
                        "} " +
                    "}" +
                    "=" +
                    "{ " +
                        "name: 'BeiJing Site', " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'b@x.com', " +
                            "address: { " +
                                "city: 'BeiJing', " +
                                "streetNo: 200 " +
                            "} " +
                        "} " +
                    "}, " +
                    "{ " +
                        "phone: '987654321', " +
                        "email: 'c@x.com', " +
                        "address: { " +
                            "city: 'ChengDu', " +
                            "streetNo: 100 " +
                        "} " +
                    "}" +
                    "=" +
                    "{ " +
                        "name: 'ChengDu Site', " +
                        "contactInfo: { " +
                            "phone: '987654321', " +
                            "email: 'c@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 100 " +
                            "} " +
                        "} " +
                    "}" +
                "}";
        Assert.assertEquals(changedSites, company.getSites().toString());
    }
    
    @Test
    public void testChangeLevel1EmbeddedScalar() {
        Company company1 = new Company();
        Company company2 = new Company();
        ContactInfo sharedContactInfo = contactInfo("123454321", "c@x.com", address("ChengDu", 100));
        site("ChengDu Site", sharedContactInfo).setCompany(company1);
        site("BeiJing Site", contactInfo("123456789", "b@x.com", address("BeiJing", 200))).setCompany(company1);
        site("ChengDu Site", sharedContactInfo).setCompany(company2);
        site("BeiJing Site", contactInfo("123456789", "b@x.com", address("BeiJing", 200))).setCompany(company2);
        String unchangedSites =
                "{" +
                    "{ " +
                        "phone: '123454321', " +
                        "email: 'c@x.com', " +
                        "address: { " +
                            "city: 'ChengDu', " +
                            "streetNo: 100 " +
                        "} " +
                    "}" +
                    "=" +
                    "{ " +
                        "name: 'ChengDu Site', " +
                        "contactInfo: { " +
                            "phone: '123454321', " +
                            "email: 'c@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 100 " +
                            "} " +
                        "} " +
                    "}, " +
                    "{ " +
                        "phone: '123456789', " +
                        "email: 'b@x.com', " +
                        "address: { " +
                            "city: 'BeiJing', " +
                            "streetNo: 200 " +
                        "} " +
                    "}" +
                    "=" +
                    "{ " +
                        "name: 'BeiJing Site', " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'b@x.com', " +
                            "address: { " +
                                "city: 'BeiJing', " +
                                "streetNo: 200 " +
                            "} " +
                        "} " +
                    "}" +
                "}";
        Assert.assertEquals(unchangedSites, company1.getSites().toString());
        Assert.assertEquals(unchangedSites, company2.getSites().toString());
        
        sharedContactInfo.setPhone("987654321");
        String changedSites =
                "{" +
                    "{ " +
                        "phone: '123456789', " +
                        "email: 'b@x.com', " +
                        "address: { " +
                            "city: 'BeiJing', " +
                            "streetNo: 200 " +
                        "} " +
                    "}" +
                    "=" +
                    "{ " +
                        "name: 'BeiJing Site', " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'b@x.com', " +
                            "address: { " +
                                "city: 'BeiJing', " +
                                "streetNo: 200 " +
                            "} " +
                        "} " +
                    "}, " +
                    "{ " +
                        "phone: '987654321', " +
                        "email: 'c@x.com', " +
                        "address: { " +
                            "city: 'ChengDu', " +
                            "streetNo: 100 " +
                        "} " +
                    "}" +
                    "=" +
                    "{ " +
                        "name: 'ChengDu Site', " +
                        "contactInfo: { " +
                            "phone: '987654321', " +
                            "email: 'c@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 100 " +
                            "} " +
                        "} " +
                    "}" +
                "}";
        Assert.assertEquals(changedSites, company1.getSites().toString());
        Assert.assertEquals(changedSites, company2.getSites().toString());
    }
    
    @Test
    public void testChangeLevel2EmbeddedScalar() {
        Company company1 = new Company();
        Company company2 = new Company();
        Address sharedAddress = address("ChengDu", 100);
        site("ChengDu Site-1", contactInfo("123456789", "c@x.com", sharedAddress)).setCompany(company1);
        site("ChengDu Site-2", contactInfo("123456789", "c@x.com", address("ChengDu", 200))).setCompany(company1);
        site("ChengDu Site-1", contactInfo("123456789", "c@x.com", sharedAddress)).setCompany(company2);
        site("ChengDu Site-2", contactInfo("123456789", "c@x.com", address("ChengDu", 200))).setCompany(company2);
        String unchangedSites =
                "{" +
                    "{ " +
                        "phone: '123456789', " +
                        "email: 'c@x.com', " +
                        "address: { " +
                            "city: 'ChengDu', " +
                            "streetNo: 100 " +
                        "} " +
                    "}" +
                    "=" +
                    "{ " +
                        "name: 'ChengDu Site-1', " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'c@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 100 " +
                            "} " +
                        "} " +
                    "}, " +
                    "{ " +
                        "phone: '123456789', " +
                        "email: 'c@x.com', " +
                        "address: { " +
                            "city: 'ChengDu', " +
                            "streetNo: 200 " +
                        "} " +
                    "}" +
                    "=" +
                    "{ " +
                        "name: 'ChengDu Site-2', " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'c@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 200 " +
                            "} " +
                        "} " +
                    "}" +
                "}";
        Assert.assertEquals(unchangedSites, company1.getSites().toString());
        Assert.assertEquals(unchangedSites, company2.getSites().toString());
        
        sharedAddress.setStreetNo(300);
        String changedSites =
                "{" +
                    "{ " +
                        "phone: '123456789', " +
                        "email: 'c@x.com', " +
                        "address: { " +
                            "city: 'ChengDu', " +
                            "streetNo: 200 " +
                        "} " +
                    "}" +
                    "=" +
                    "{ " +
                        "name: 'ChengDu Site-2', " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'c@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 200 " +
                            "} " +
                        "} " +
                    "}, " +
                    "{ " +
                        "phone: '123456789', " +
                        "email: 'c@x.com', " +
                        "address: { " +
                            "city: 'ChengDu', " +
                            "streetNo: 300 " +
                        "} " +
                    "}" +
                    "=" +
                    "{ " +
                        "name: 'ChengDu Site-1', " +
                        "contactInfo: { " +
                            "phone: '123456789', " +
                            "email: 'c@x.com', " +
                            "address: { " +
                                "city: 'ChengDu', " +
                                "streetNo: 300 " +
                            "} " +
                        "} " +
                    "}" +
                "}";
        Assert.assertEquals(changedSites, company1.getSites().toString());
        Assert.assertEquals(changedSites, company2.getSites().toString());
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
    
    private static Site site(String name, ContactInfo contactInfo) {
        Site site = new Site();
        site.setName(name);
        site.setContactInfo(contactInfo);
        return site;
    }
}
