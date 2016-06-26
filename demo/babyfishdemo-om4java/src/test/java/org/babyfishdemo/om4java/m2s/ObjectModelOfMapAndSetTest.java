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
package org.babyfishdemo.om4java.m2s;

import java.util.Map;
import java.util.Set;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfishdemo.om4java.m2s.Company;
import org.babyfishdemo.om4java.m2s.Investor;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ObjectModelOfMapAndSetTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testDiffWithListAndSet() {
        Company company = new Company();
        Investor investor = new Investor();
        
        /*
         * Be DIFFERENT with ListAndSet, for Map-Set association,  
         * You can NOT create the association by Adding the Set side
         * because the Set can not specify what the key in the opposite Map is.
         * So babyfish throws exception when you want to call the "add" or "addAll"
         * of Set side.
         * 
         * But you can call any removing behavior of set side to destroy the association :)
         */
        investor.getCompanies().add(company);
    }
    
    @Test
    public void testSameWithListAndSet() {
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
        company1.getInvestors().putAll(investors("IV-02", investor2, "IV-03", investor3));
        assertCompany(company1, "IV-02", investor2, "IV-03", investor3);
        assertCompany(company2);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1);
        
        /*
         * Add all the investors of company1 into company2,
         * this is many-to-many association, so company2 will NOT seize these investors, 
         * just share them with company1.
         * company2 will be added into the collection property "companies" of 
         * investor2 and investor3 automatically and implicitly.
         */
        company2.getInvestors().putAll(company1.getInvestors());
        assertCompany(company1, "IV-02", investor2, "IV-03", investor3);
        assertCompany(company2, "IV-02", investor2, "IV-03", investor3);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2, company1, company2);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Remove company2 from investor2.
         * investor2 will be removed from company2 automatically and implicitly.
         */
        investor2.getCompanies().remove(company2);
        assertCompany(company1, "IV-02", investor2, "IV-03", investor3);
        assertCompany(company2, "IV-03", investor3);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Add investor1 into company2.
         * company2 will be added into investor1 automatically and implicitly.
         */
        company2.getInvestors().put("IV-01", investor1);
        assertCompany(company1, "IV-02", investor2, "IV-03", investor3);
        assertCompany(company2, "IV-03", investor3, "IV-01", investor1);
        assertCompany(company3);
        assertInvestor(investor1, company2);
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Add all the investors of company2 into company3,
         * this is many-to-many association, so company3 will NOT seize these investors, 
         * just share them with company2.
         * company3 will be added into the collection property "companies" of 
         * investor3 and investor1 automatically and implicitly.
         */
        company3.getInvestors().putAll(company2.getInvestors());
        assertCompany(company1, "IV-02", investor2, "IV-03", investor3);
        assertCompany(company2, "IV-03", investor3, "IV-01", investor1);
        assertCompany(company3, "IV-03", investor3, "IV-01", investor1);
        assertInvestor(investor1, company2, company3);
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1, company2, company3);
        
        /*
         * Remove company3 from investor3.
         * investor3 will be removed from company3 automatically and implicitly.
         */
        investor3.getCompanies().remove(company3);
        assertCompany(company1, "IV-02", investor2, "IV-03", investor3);
        assertCompany(company2, "IV-03", investor3, "IV-01", investor1);
        assertCompany(company3, "IV-01", investor1);
        assertInvestor(investor1, company2, company3);
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Clear companies of investor1.
         * investor1 will be removed from company2 and company3 automatically and implicitly.
         */
        investor1.getCompanies().clear();
        assertCompany(company1, "IV-02", investor2, "IV-03", investor3);
        assertCompany(company2, "IV-03", investor3);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2, company1);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Clear companies of investor2.
         * investor2 will be removed from company1 automatically and implicitly.
         */
        investor2.getCompanies().clear();
        assertCompany(company1, "IV-03", investor3);
        assertCompany(company2, "IV-03", investor3);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2);
        assertInvestor(investor3, company1, company2);
        
        /*
         * Clear companies of investor3.
         * investor3 will be removed from company1 and company2 automatically and implicitly.
         */
        investor3.getCompanies().clear();
        assertCompany(company1);
        assertCompany(company2);
        assertCompany(company3);
        assertInvestor(investor1);
        assertInvestor(investor2);
        assertInvestor(investor3);
    }
 
    private static void assertCompany(Company company, Object ... keyValuePairs) {
        Assert.assertEquals(
                investors(keyValuePairs), 
                company.getInvestors()
        );
    }
    
    private static void assertInvestor(Investor investor, Company ... companies) {
        Assert.assertEquals(companies.length, investor.getCompanies().size());
        Set<Company> set = new HashSet<>((companies.length * 4 + 2) / 3);
        for (Company company : companies) {
            set.add(company);
        }
        Assert.assertEquals(set, investor.getCompanies());
    }
    
    private static Map<String, Investor> investors(Object ... keyValuePairs) {
        int len = keyValuePairs.length;
        if (len % 2 != 0) {
            throw new IllegalArgumentException("The length of keyValuePairs must be even number");
        }
        Map<String, Investor> map = new HashMap<>(
                (EqualityComparator<String>)null,
                ReferenceEqualityComparator.getInstance()
        );
        for (int i = 0; i < len; i += 2) {
            String petName = (String)keyValuePairs[i];
            Investor investor = (Investor)keyValuePairs[i + 1];
            map.put(petName, investor);
        }
        return map;
    }
}
