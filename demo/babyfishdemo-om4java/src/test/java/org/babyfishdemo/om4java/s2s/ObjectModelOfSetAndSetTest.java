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
package org.babyfishdemo.om4java.s2s;

import java.util.Set;

import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.babyfishdemo.om4java.s2s.Company;
import org.babyfishdemo.om4java.s2s.Investor;
import org.junit.Assert;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class ObjectModelOfSetAndSetTest {
 
    @Test
    public void test() {
        Company company1 = new Company();
        Company company2 = new Company();
        Company company3 = new Company();
        Investor investor1 = new Investor();
        Investor investor2 = new Investor();
        Investor investor3 = new Investor();
        
        /*
         * Validate the initialized state of these objects
         */
        assertCompany(company1);
        assertCompany(company2);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2);
        assertInvestor(investor3);
        
        /*
         * Add investor2 and investor3 into company1.
         * company1 will be added into the collection property "companies" of 
         * investor2 and investor3 automatically and implicitly.
         */
        company1.getInvestors().addAll(MACollections.wrap(investor2, investor3));
        assertCompany(company1, investor2, investor3); // Changed by you
        assertCompany(company2);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2, company1); // Changed automatically
        assertInvestor(investor3, company1); // Changed automatically
        
        /*
         * Add all the investors of company1 into company2,
         * this is many-to-many association, so company2 will NOT seize these investors, 
         * just share them with company1.
         * company2 will be added into the collection property "companies" of 
         * investor2 and investor3 automatically and implicitly.
         */
        company2.getInvestors().addAll(company1.getInvestors());
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor2, investor3); // Changed by you
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2, company1, company2); // Changed automatically
        assertInvestor(investor3, company1, company2); // Changed automatically
        
        /*
         * Remove company2 from investor2.
         * investor2 will be removed from company2 automatically and implicitly.
         */
        investor2.getCompanies().remove(company2);
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor3); // Changed automatically
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2, company1); // Changed by you
        assertInvestor(investor3, company1, company2);
        
        /*
         * Add company2 into investor1.
         * investor1 will be added into company2 automatically and implicitly.
         */
        investor1.getCompanies().add(company2);
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor3, investor1); // Changed automatically
        assertCompany(company3);
        assertInvestor(investor1, company2); //Changed by you
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Add all the investors of company2 into company3,
         * this is many-to-many association, so company3 will NOT seize these investors, 
         * just share them with company2.
         * company3 will be added into the collection property "companies" of 
         * investor3 and investor1 automatically and implicitly.
         */
        company3.getInvestors().addAll(company2.getInvestors());
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor3, investor1);
        assertCompany(company3, investor3, investor1); // Changed by you
        assertInvestor(investor1, company2, company3); // Changed automatically
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1, company2, company3); // Changed automatically
        
        /*
         * Remove company3 from investor3.
         * investor3 will be removed from company3 automatically and implicitly.
         */
        investor3.getCompanies().remove(company3);
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor3, investor1);
        assertCompany(company3, investor1); // Changed automatically
        assertInvestor(investor1, company2, company3);
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1, company2); // Changed by you
        
        /*
         * Add company3 into investor2.
         * investor1 will be added into company2 automatically and implicitly.
         */
        investor2.getCompanies().add(company3);
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor3, investor1);
        assertCompany(company3, investor1, investor2); // Changed automatically
        assertInvestor(investor1, company2, company3);
        assertInvestor(investor2, company3, company1); // Changed by you
        assertInvestor(investor3, company1, company2);
        
        /*
         * Clear companies of investor1.
         * investor1 will be removed from company2 and company3 automatically and implicitly.
         */
        investor1.getCompanies().clear();
        assertCompany(company1, investor2, investor3);
        assertCompany(company2, investor3); // Changed automatically
        assertCompany(company3, investor2); // Changed automatically
        assertInvestor(investor1); // Changed by you
        assertInvestor(investor2, company3, company1);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Clear companies of investor2.
         * investor2 will be removed from company3 and company1 automatically and implicitly.
         */
        investor2.getCompanies().clear();
        assertCompany(company1, investor3); // Changed automatically
        assertCompany(company2, investor3);
        assertCompany(company3); // Changed automatically
        assertInvestor(investor1);
        assertInvestor(investor2); // Changed by you
        assertInvestor(investor3, company1, company2);
        
        /*
         * Clear companies of investor3.
         * investor3 will be removed from company1 and company2 automatically and implicitly.
         */
        investor3.getCompanies().clear();
        assertCompany(company1); // Changed automatically
        assertCompany(company2); // Changed automatically
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2);
        assertInvestor(investor3); // Changed by you
    }
 
    private static void assertCompany(Company company, Investor ... investors) {
        Assert.assertEquals(investors.length, company.getInvestors().size());
        Set<Investor> set = new HashSet<>((investors.length * 4 + 2) / 3);
        for (Investor investor : investors) {
            set.add(investor);
        }
        Assert.assertEquals(set, company.getInvestors());
    }
    
    private static void assertInvestor(Investor investor, Company ... companies) {
        Assert.assertEquals(companies.length, investor.getCompanies().size());
        Set<Company> set = new HashSet<>((companies.length * 4 + 2) / 3);
        for (Company company : companies) {
            set.add(company);
        }
        Assert.assertEquals(set, investor.getCompanies());
    }
}
