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
package org.babyfish.hibernate.merge;

import javax.persistence.EntityManagerFactory;

import junit.framework.Assert;

import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class MergeTest {
    
    private XEntityManagerFactory entityManagerFactory;
    
    @Before
    public void initEntityManagerFactory() {
        this.entityManagerFactory =
            new HibernatePersistenceProvider(
                    MergeTest.class.getPackage().getName().replace('.', '/') + 
                    "/persistence.xml")
            .createEntityManagerFactory(null, null);
        XEntityManager em = this.entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            try {
                Investor investor1 = new Investor();
                investor1.setName("investor1");
                Investor investor2 = new Investor();
                investor2.setName("investor2");
                Company company = new Company();
                company.setName("company");
                Department department1 = new Department();
                department1.setName("department1");
                Department department2 = new Department();
                department2.setName("department2");
                Site site1 = new Site();
                site1.setName("site1");
                Site site2 = new Site();
                site2.setName("site2");
                Office site1Office1 = new Office();
                site1Office1.setName("site1Office1");
                Office site1Office2 = new Office();
                site1Office2.setName("site1Office2");
                Office site2Office1 = new Office();
                site2Office1.setName("site2Office1");
                Office site2Office2 = new Office();
                site2Office2.setName("site2Office2");
                em.persist(investor1);
                em.persist(investor2);
                em.persist(company);
                em.persist(department1);
                em.persist(department2);
                em.persist(site1);
                em.persist(site2);
                em.persist(site1Office1);
                em.persist(site1Office2);
                em.persist(site2Office1);
                em.persist(site2Office2);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    @After
    public void diposeEntityManagerFactory() {
        EntityManagerFactory emf = this.entityManagerFactory;
        if (emf != null) {
            this.entityManagerFactory = null;
            emf.close();
        }
    }
    
    @Test
    public void testMerge() {
        Investor investor1;
        Investor investor2;
        Company company;
        Department department1;
        Department department2;
        Site site1;
        Site site2;
        Office site1Office1;
        Office site1Office2;
        Office site2Office1;
        Office site2Office2;
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            company = em.find(Company.class, 1L, "sites", "investors");
            department1 = em.getReference(Department.class, 1L);
            department2 = em.getReference(Department.class, 2L);
            site1 = em.find(Site.class, 1L, "offices");
            site2 = em.find(Site.class, 2L, "offices");
            investor1 = em.getReference(Investor.class, 1L);
            investor2 = em.getReference(Investor.class, 2L);
            site1Office1 = em.getReference(Office.class, 1L);
            site1Office2 = em.getReference(Office.class, 2L);
            site2Office1 = em.getReference(Office.class, 3L);
            site2Office2 = em.getReference(Office.class, 4L);
            Assert.assertEquals("company", company.getName());
            Assert.assertEquals("department1", department1.getName());
            Assert.assertEquals("department2", department2.getName());
            Assert.assertEquals("site1", site1.getName());
            Assert.assertEquals("site2", site2.getName());
            Assert.assertEquals("investor1", investor1.getName());
            Assert.assertEquals("investor2", investor2.getName());
            Assert.assertEquals("site1Office1", site1Office1.getName());
            Assert.assertEquals("site1Office2", site1Office2.getName());
            Assert.assertEquals("site2Office1", site2Office1.getName());
            Assert.assertEquals("site2Office2", site2Office2.getName());
        }
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            try {
                company.getInvestors().add(investor1);
                company.getInvestors().add(investor2);
                department1.setCompany(company);
                department2.setCompany(company);
                company.getSites().put("S1", site1);
                company.getSites().put("S2", site2);
                site1.getOffices().add(site1Office1);
                site1.getOffices().add(site1Office2);
                site2.getOffices().add(site2Office1);
                site2.getOffices().add(site2Office2);
                em.merge(company);
                em.merge(department1);
                em.merge(department2);
                em.merge(site1);
                em.merge(site2);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        }
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            company = em.find(Company.class, 1L, "sites", "investors");
            department1 = em.find(Department.class, 1L);
            department2 = em.find(Department.class, 2L);
            site1 = em.find(Site.class, 1L, "offices");
            site2 = em.find(Site.class, 2L, "offices");
            investor1 = em.getReference(Investor.class, 1L);
            investor2 = em.getReference(Investor.class, 2L);
            site1Office1 = em.getReference(Office.class, 1L);
            site1Office2 = em.getReference(Office.class, 2L);
            site2Office1 = em.getReference(Office.class, 3L);
            site2Office2 = em.getReference(Office.class, 4L);
        }
        Assert.assertEquals(1L, department1.getCompany().getId().intValue());
        Assert.assertEquals(1L, department2.getCompany().getId().intValue());
        Assert.assertEquals(2, company.getSites().size());
        Assert.assertEquals(2, company.getInvestors().size());
        Assert.assertEquals(2, site1.getOffices().size());
        Assert.assertEquals(2, site2.getOffices().size());
    }
}
