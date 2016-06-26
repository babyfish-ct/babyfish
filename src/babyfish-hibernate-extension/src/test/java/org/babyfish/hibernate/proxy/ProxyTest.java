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
package org.babyfish.hibernate.proxy;

import java.lang.reflect.Field;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.data.event.PropertyVersion;
import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.hibernate.Hibernate;
import org.hibernate.model.hibernate.spi.proxy.FrozenLazyInitializer;
import org.hibernate.proxy.HibernateProxy;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ProxyTest {
    
    private static final ModelClass COMPANY_MODEL_CLASS = ModelClass.of(Company.class);
    
    private static XEntityManagerFactory entityManagerFactory;
    
    @BeforeClass
    public static void initEntityManager() {
        entityManagerFactory =
                new HibernatePersistenceProvider(
                        ProxyTest.class.getPackage().getName().replace('.', '/') + 
                        "/persistence.xml")
                .createEntityManagerFactory(null, null);
        XEntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            try {
                Department market = new Department();
                Department development = new Department();
                Employee jim = new Employee();
                Employee kate = new Employee();
                market.setName("market");
                development.setName("development");
                jim.setName("jim");
                kate.setName("kate");
                market.getEmployees().add(jim);
                development.getEmployees().add(kate);
                em.persist(market);
                em.persist(development);
                em.persist(jim);
                em.persist(kate);
            } catch (final RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    @AfterClass
    public static void diposeEntityManagerFactory() {
        EntityManagerFactory emf = entityManagerFactory;
        if (emf != null) {
            entityManagerFactory = null;
            emf.close();
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testUnloadedProxy() {
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            Department market = em.getReference(Employee.class, 1L).getDepartment();
            Department development = em.getReference(Employee.class, 2L).getDepartment();
            Assert.assertEquals(1L, market.getId().longValue());
            Assert.assertEquals(2L, development.getId().longValue());
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(market));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(development));
            UnifiedComparator<? super Department> unifiedComparator =
                    (UnifiedComparator)
                    COMPANY_MODEL_CLASS
                    .getDeclaredProperties()
                    .get("departments")
                    .getCollectionUnifiedComparator();
            MAOrderedSet<Department> departments = 
                    new MALinkedHashSet<Department>(
                            unifiedComparator.equalityComparator(true));
            
            Assert.assertFalse(isFrozenProxy(market));
            Assert.assertFalse(isFrozenProxy(development));
            departments.add(market);
            departments.add(development);
            Assert.assertTrue(isFrozenProxy(market));
            Assert.assertTrue(isFrozenProxy(development));
            
            final StringBuilder builder = new StringBuilder();
            departments.addElementListener(
                    new ElementListener<Department>() {
                        @Override
                        public void modified(ElementEvent<Department> e)
                                throws Throwable {
                            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                                Department detached = e.getElement(PropertyVersion.DETACH);
                                builder
                                .append("-[")
                                .append(detached.getId())
                                .append(", ")
                                .append(detached.getName())
                                .append(']');
                            }
                            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                                Department attached = e.getElement(PropertyVersion.ATTACH);
                                builder
                                .append("+[")
                                .append(attached.getId())
                                .append(", ")
                                .append(attached.getName())
                                .append(']');
                            }
                        }
                    });
            Assert.assertEquals("", builder.toString());
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(development));
            development.setId(1L);
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(development));
            Assert.assertEquals("-[2, development]-[1, market]+[1, development]", builder.toString());
            Assert.assertFalse(isFrozenProxy(market));
            Assert.assertTrue(isFrozenProxy(development));
            Assert.assertEquals(1L, development.getId().longValue());
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testLoadedProxyByModifyProxy() {
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            Department market = em.getReference(Employee.class, 1L).getDepartment();
            Department development = em.getReference(Employee.class, 2L).getDepartment();
            Assert.assertEquals(1L, market.getId().longValue());
            Assert.assertEquals(2L, development.getId().longValue());
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(market));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(development));
            Hibernate.initialize(market);
            Hibernate.initialize(development);
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(market));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(development));
            UnifiedComparator<? super Department> unifiedComparator =
                    (UnifiedComparator)
                    COMPANY_MODEL_CLASS
                    .getDeclaredProperties()
                    .get("departments")
                    .getCollectionUnifiedComparator();
            MAOrderedSet<Department> departments = 
                    new MALinkedHashSet<Department>(
                            unifiedComparator.equalityComparator(true));
            
            Assert.assertFalse(isFrozenProxy(market));
            Assert.assertFalse(isFrozenProxy(development));
            departments.add(market);
            departments.add(development);
            Assert.assertTrue(isFrozenProxy(market));
            Assert.assertTrue(isFrozenProxy(development));
            
            final StringBuilder builder = new StringBuilder();
            departments.addElementListener(
                    new ElementListener<Department>() {
                        @Override
                        public void modified(ElementEvent<Department> e)
                                throws Throwable {
                            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                                Department detached = e.getElement(PropertyVersion.DETACH);
                                builder
                                .append("-[")
                                .append(detached.getId())
                                .append(", ")
                                .append(detached.getName())
                                .append(']');
                            }
                            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                                Department attached = e.getElement(PropertyVersion.ATTACH);
                                builder
                                .append("+[")
                                .append(attached.getId())
                                .append(", ")
                                .append(attached.getName())
                                .append(']');
                            }
                        }
                    });
            Assert.assertEquals("", builder.toString());
            development.setId(1L);
            Assert.assertEquals(
                    1L, 
                    (
                            (Department)
                            ((HibernateProxy)development)
                            .getHibernateLazyInitializer()
                            .getImplementation()
                    ).getId().longValue());
            Assert.assertEquals("-[2, development]-[1, market]+[1, development]", builder.toString());
            Assert.assertFalse(isFrozenProxy(market));
            Assert.assertTrue(isFrozenProxy(development));
            Assert.assertEquals(1L, development.getId().longValue());
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testLoadedProxyByModifyTarget() {
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            Department market = em.getReference(Employee.class, 1L).getDepartment();
            Department development = em.getReference(Employee.class, 2L).getDepartment();
            Assert.assertEquals(1L, market.getId().longValue());
            Assert.assertEquals(2L, development.getId().longValue());
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(market));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(development));
            Hibernate.initialize(market);
            Hibernate.initialize(development);
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(market));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(development));
            UnifiedComparator<? super Department> unifiedComparator =
                    (UnifiedComparator)
                    COMPANY_MODEL_CLASS
                    .getDeclaredProperties()
                    .get("departments")
                    .getCollectionUnifiedComparator();
            MAOrderedSet<Department> departments = 
                    new MALinkedHashSet<Department>(
                            unifiedComparator.equalityComparator(true));
            
            Assert.assertFalse(isFrozenProxy(market));
            Assert.assertFalse(isFrozenProxy(development));
            departments.add(market);
            departments.add(development);
            Assert.assertTrue(isFrozenProxy(market));
            Assert.assertTrue(isFrozenProxy(development));
            
            final StringBuilder builder = new StringBuilder();
            departments.addElementListener(
                    new ElementListener<Department>() {
                        @Override
                        public void modified(ElementEvent<Department> e)
                                throws Throwable {
                            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                                Department detached = e.getElement(PropertyVersion.DETACH);
                                builder
                                .append("-[")
                                .append(detached.getId())
                                .append(", ")
                                .append(detached.getName())
                                .append(']');
                            }
                            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                                Department attached = e.getElement(PropertyVersion.ATTACH);
                                builder
                                .append("+[")
                                .append(attached.getId())
                                .append(", ")
                                .append(attached.getName())
                                .append(']');
                            }
                        }
                    });
            Assert.assertEquals("", builder.toString());
            ((Department)
            ((HibernateProxy)development)
            .getHibernateLazyInitializer()
            .getImplementation())
            .setId(1L);
            Assert.assertEquals(1L, development.getId().longValue());
            Assert.assertEquals("-[2, development]-[1, market]+[1, development]", builder.toString());
            Assert.assertFalse(isFrozenProxy(market));
            Assert.assertTrue(isFrozenProxy(development));
            Assert.assertEquals(1L, development.getId().longValue());
        }
    }
    
    private static boolean isFrozenProxy(Object proxy) {
        if (proxy instanceof HibernateProxy) {
            HibernateProxy hibernateProxy = (HibernateProxy)proxy;
            FrozenLazyInitializer frozenLazyInitializer = 
                    FrozenLazyInitializer.get(hibernateProxy);
            try {
                Field field = frozenLazyInitializer.getClass().getDeclaredField("idFrozenContext");
                field.setAccessible(true);
                return field.get(frozenLazyInitializer) != null;
            } catch (Exception ex) {
                throw new RuntimeException();
            }
        }
        throw new IllegalArgumentException();
    }
}
