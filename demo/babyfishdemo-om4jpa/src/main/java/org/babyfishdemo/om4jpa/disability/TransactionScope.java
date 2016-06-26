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
package org.babyfishdemo.om4jpa.disability;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.persistence.EntityManager;

import org.babyfish.collection.HashMap;
import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;

/**
 * @author Tao Chen
 */
public class TransactionScope implements AutoCloseable {
    
    /*
     * For maven, no problem.
     * But, eclipse's embedded junit has some problems,
     * we'd better create different EntityManagerFactory with different connection string
     * for difference test cases.
     */
    private static Map<String, XEntityManagerFactory> entityManagerFactoryMap = new HashMap<>();
    
    private static ReadWriteLock entityManagerFactoryMapLock = new ReentrantReadWriteLock();
    
    private static final ThreadLocal<XEntityManager> ENTITY_MANAGER_LOCAL = 
            new ThreadLocal<>();
            
    private XEntityManager oldEntityManager;
    
    private boolean readonly;
    
    private boolean completed;
    
    private boolean closed;
    
    public TransactionScope(String connectionString) {
        this(connectionString, false);
    }
    
    public TransactionScope(String connectionString, boolean readonly) {
        XEntityManager entityManager = getEntityManagerFactory(connectionString).createEntityManager();
        if (!readonly) {
            entityManager.getTransaction().begin();
        }
        this.readonly = readonly;
        this.oldEntityManager = ENTITY_MANAGER_LOCAL.get();
        ENTITY_MANAGER_LOCAL.set(entityManager);
    }
    
    public static XEntityManager getEntityManager() {
        return ENTITY_MANAGER_LOCAL.get();
    }
    
    public static void recreateDatabase() {
        Lock lock;
        (lock = entityManagerFactoryMapLock.writeLock()).lock();
        try {
            entityManagerFactoryMap.clear();
        } finally {
            lock.unlock();
        }
    }
    
    public void compete() {
        this.completed = true;
    }
    
    public <T> T complete(T value) {
        this.completed = true;
        return value;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        EntityManager entityManager = ENTITY_MANAGER_LOCAL.get();
        try {
            if (!this.readonly) {
                if (this.completed) {
                    entityManager.getTransaction().commit();
                } else {
                    entityManager.getTransaction().rollback();
                }
            }
        } finally {
            ENTITY_MANAGER_LOCAL.set(this.oldEntityManager);
            entityManager.close();
        }
    }
    
    private static XEntityManagerFactory getEntityManagerFactory(String connectionString) {
        XEntityManagerFactory factory;
        Lock lock;
        
        (lock = entityManagerFactoryMapLock.readLock()).lock(); // 1st locking
        try {
            factory = entityManagerFactoryMap.get(connectionString); // 1st reading
        } finally {
            lock.unlock();
        }
        
        if (factory == null || !factory.isOpen()) { // 1st checking
            (lock = entityManagerFactoryMapLock.writeLock()).lock(); // 2nd locking
            try {
                factory = entityManagerFactoryMap.get(connectionString); // 2nd reading
                if (factory == null || !factory.isOpen()) { // 2nd checking
                    factory = createEntityManagerFactory(connectionString);
                    entityManagerFactoryMap.put(connectionString, factory);
                }
            } finally {
                lock.unlock();
            }
        }
        
        return factory;
    }
    
    private static XEntityManagerFactory createEntityManagerFactory(String connectionString) {
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.connection.url", connectionString);
        return new HibernatePersistenceProvider(
                TransactionScope.class.getPackage().getName().replace('.', '/') +
                "/persistence.xml"
        )
        .createEntityManagerFactory(null, properties);
    }
}
