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
package org.babyfish.test.hibernate.cfg;

import java.lang.reflect.Field;

import junit.framework.Assert;

import org.babyfish.data.LazinessManageable;
import org.babyfish.hibernate.cfg.Configuration;
import org.babyfish.lang.UncheckedException;
import org.hibernate.ObjectDeletedException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class CascadeTest {
    
    private static SessionFactory babyfishSessionFactory;
    
    private static SessionFactory hibernateSessionFactory;
    
    @SuppressWarnings("deprecation")
    @BeforeClass
    public static void initClass() {
        babyfishSessionFactory =
                new Configuration()
                .configure(
                        CascadeTest.class.getPackage().getName().replace('.', '/') + 
                        "/hibernate.cfg.xml")
                .buildSessionFactory();
        hibernateSessionFactory = getHibernateSessionFactory(babyfishSessionFactory);
    }
    
    @AfterClass
    public static void destroyClass() {
        if (babyfishSessionFactory != null) {
            babyfishSessionFactory.close();
            babyfishSessionFactory = null;
            hibernateSessionFactory = null;
        }
    }
    
    @Before
    public void init() {
        Session session = hibernateSessionFactory.openSession();
        try {
            Transaction tx = session.beginTransaction();
            try {
                session
                .createSQLQuery("DELETE FROM ct_EMPLOYEE")
                .executeUpdate();
                session
                .createSQLQuery("DELETE FROM ct_DEPARTMENT")
                .executeUpdate();
                session
                .createSQLQuery("DELETE FROM ct_EMPLOYEE_HOLDER")
                .executeUpdate();
                
                session
                .createSQLQuery(
                        "INSERT INTO ct_DEPARTMENT(DEPARTMENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 1L)
                .setString(1, "Market")
                .executeUpdate();
                
                session
                .createSQLQuery(
                        "INSERT INTO ct_EMPLOYEE_HOLDER(EMPLOYEE_HOLDER_ID) VALUES(?)")
                .setLong(0, 1L)
                .executeUpdate();
                
                session
                .createSQLQuery(
                        "INSERT INTO ct_EMPLOYEE(EMPLOYEE_ID, NAME, DEPARTMENT_ID, EMPLOYEE_HOLDER_ID) VALUES(?, ?, ?, ?)")
                .setLong(0, 1L)
                .setString(1, "Jim")
                .setLong(2, 1L)
                .setLong(3, 1L)
                .executeUpdate();
                session
                .createSQLQuery(
                        "INSERT INTO ct_EMPLOYEE(EMPLOYEE_ID, NAME, DEPARTMENT_ID, EMPLOYEE_HOLDER_ID) VALUES(?, ?, ?, ?)")
                .setLong(0, 2L)
                .setString(1, "Kate")
                .setLong(2, 1L)
                .setLong(3, 1L)
                .executeUpdate();
                
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            } catch (Error err) {
                tx.rollback();
                throw err;
            }
            tx.commit();
        } finally {
            session.close();
        }
    }

    @Test(expected = ObjectDeletedException.class)
    public void testHibernateSessionFactoryByDeletingEmployee() {
        this.deleteEmployee(false);
    }
    
    @Test
    public void testBabyFishSessionFactoryByDeletingEmployee() {
        this.deleteEmployee(true);
    }
    
    @Test
    public void testHibernateSessionFactoryByDeletingEmployeeHolder() {
        this.deleteEmployeeHolder(false);
    }
    
    @Test
    public void testBabyFishSessionFactoryByDeletingEmployeeHolder() {
        this.deleteEmployeeHolder(true);
    }
    
    private void deleteEmployee(boolean useBabyfishSessionFactory) {
        Session session = useBabyfishSessionFactory ?
                babyfishSessionFactory.openSession() :
                hibernateSessionFactory.openSession();
        try {
            Transaction tx = session.beginTransaction();
            try {
                Department department = (Department)session.get(Department.class, 1L);
                Assert.assertNotNull(department);
                session.delete(department.getEmployees().iterator().next());
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            } catch (Error err) {
                tx.rollback();
                throw err;
            }
            tx.commit();
        } finally {
            session.close();
        }
    }
    
    private void deleteEmployeeHolder(boolean useBabyfishSessionFactory) {
        Session session = useBabyfishSessionFactory ?
                babyfishSessionFactory.openSession() :
                hibernateSessionFactory.openSession();
        try {
            Transaction tx = session.beginTransaction();
            try {
                EmployeeHolder employeeHolder = (EmployeeHolder)session.get(EmployeeHolder.class, 1L);
                ((LazinessManageable)employeeHolder.getEmployees()).load();
                Assert.assertNotNull(employeeHolder);
                session.delete(employeeHolder);
            } catch (RuntimeException ex) {
                tx.rollback();
                throw ex;
            } catch (Error ex) {
                tx.rollback();
                throw ex;
            }
            tx.commit();
        } finally {
            session.close();
        }
    }
    
    private static SessionFactory getHibernateSessionFactory(SessionFactory sessionFactory) {
        Class<?> clazz = sessionFactory.getClass();
        try {
            Field rawField = clazz.getDeclaredField("{raw}");
            rawField.setAccessible(true);
            return (SessionFactory)rawField.get(sessionFactory);
        } catch (Throwable ex) {
            throw UncheckedException.rethrow(ex);
        }
    }
    
}
