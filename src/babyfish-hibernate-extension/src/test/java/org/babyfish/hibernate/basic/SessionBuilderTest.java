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
import org.babyfish.hibernate.XSessionBuilder;
import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.cfg.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Tao Chen
 */
public class SessionBuilderTest {

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
    public void testSessionFactorySessionBuilder() {
        XSessionBuilder sessionBuilder = sessionFactory.withOptions();
        Assert.assertNotSame(sessionBuilder, sessionBuilder = sessionBuilder.noInterceptor());
        XSession session = sessionBuilder.openSession();
        try {
            //TODO: Assert.assertSame(session, sessionFactory.getCurrentSession());
        } finally {
            session.close();
        }
    }
    
    @Test
    public void testSharedSessionBuilder() {
        XSession session = sessionFactory.openSession();
        try {
            XSession session2 = session.sessionWithOptions().noInterceptor().openSession();
            try {
                Assert.assertNotSame(session, session2);
            } finally {
                session2.close();
            }
        } finally {
            session.close();
        }
    }
}
