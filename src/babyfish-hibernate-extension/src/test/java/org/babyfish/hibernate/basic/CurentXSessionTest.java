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
package org.babyfish.hibernate.basic;

import junit.framework.Assert;

import org.babyfish.hibernate.XSession;
import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.cfg.Configuration;
import org.hibernate.Transaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class CurentXSessionTest {

    private static final String RESOURCE =
            SessionBuilderTest.class.getPackage().getName().replace('.', '/') + "/hibernate.cfg.xml";
    
    private static XSessionFactory sessionFactory;
    
    @SuppressWarnings("deprecation")
    @BeforeClass
    public static void initSessionFactory() {
        sessionFactory =
            new Configuration()
            .configure(RESOURCE)
            .addAnnotatedClass(Employee.class)
            .buildSessionFactory();
        
    }
    
    @AfterClass
    public static void disposeSessionFactory() {
        XSessionFactory sf = sessionFactory;
        if (sf != null) {
            sessionFactory = null;
            sf.close();
        }
    }
    
    @Test
    public void testCurrentXSession() {
        long count;
        long newCount;
        XSession session;
        Transaction tx;
        Object s1, s2, s3;
        
        s1 = session = sessionFactory.getCurrentSession();
        tx = session.beginTransaction();
        Assert.assertSame(session, sessionFactory.getCurrentSession());
        try {
            count = (Long)session.createQuery("select count(e) from Employee e").uniqueResult();
        } catch (RuntimeException | Error ex) {
            tx.rollback();
            throw ex;
        }
        tx.commit();
        
        s2 = session = sessionFactory.getCurrentSession();
        tx = session.beginTransaction();
        Assert.assertSame(session, sessionFactory.getCurrentSession());
        try {
            Employee employee = new Employee();
            employee.setName("Name" + System.currentTimeMillis());
            session.save(employee);
        } catch (RuntimeException | Error ex) {
            tx.rollback();
            throw ex;
        }
        tx.commit();
        
        s3 = session = sessionFactory.getCurrentSession();
        tx = session.beginTransaction();
        Assert.assertSame(session, sessionFactory.getCurrentSession());
        try {
            newCount = (Long)session.createQuery("select count(e) from Employee e").uniqueResult();
        } catch (RuntimeException | Error ex) {
            tx.rollback();
            throw ex;
        }
        tx.commit();
        
        Assert.assertEquals(newCount, count + 1);
        Assert.assertNotNull(s1);
        Assert.assertNotSame(s1, s2);
        Assert.assertNotNull(s2);
        Assert.assertNotSame(s2, s3);
        Assert.assertNotNull(s3);
        Assert.assertNotSame(s3, s1);
    }
}
