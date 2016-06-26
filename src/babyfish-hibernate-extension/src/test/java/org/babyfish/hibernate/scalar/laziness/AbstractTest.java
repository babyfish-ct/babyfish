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
package org.babyfish.hibernate.scalar.laziness;

import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;

/**
 * @author Tao Chen
 */
public abstract class AbstractTest {
    
    protected static XEntityManagerFactory initEntityManagerFactory() {
        XEntityManagerFactory entityManagerFactory =
            new HibernatePersistenceProvider(
                    AbstractTest.class.getPackage().getName().replace('.', '/') + 
                    "/persistence.xml")
            .createEntityManagerFactory(null, null);
        XEntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            try {
                Department market = new Department();
                Employee jim = new Employee();
                market.setName("Market");
                market.setImage(new byte[] { 99, 88, 77, 66, 55, 44, 33, 22, 11 });
                //market.setDescription("This a depertment that is reposonsable for market work");
                jim.setName("Jim");
                jim.setImage(new byte[] { 11, 22, 33, 44, 55, 66, 77, 88, 99 });
                market.getEmployees().add(jim);
                em.persist(market);
                em.persist(jim);
                
                em.flush();
                
                em.createQuery("update Department set version = 10").executeUpdate();
            } catch (final RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return entityManagerFactory;
    }
}
