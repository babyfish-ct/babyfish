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
package org.babyfish.hibernate.internal;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.lang.Arguments;
import org.hibernate.FetchMode;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.tuple.NonIdentifierAttribute;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

/**
 * @author Tao Chen
 */
public class ImplicitCollectionJoins {
    
    private static final Map<SessionFactoryImplementor, ImplicitCollectionJoins> INSTANCES =
            new HashMap<>(
                    ReferenceEqualityComparator.<SessionFactoryImplementor>getInstance(), 
                    (EqualityComparator<Object>)null);
            
    private static final ReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();
    
    private SessionFactoryImplementor factory;
    
    private Map<String, Boolean> cache = new WeakHashMap<>();
    
    private ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    
    private ImplicitCollectionJoins(SessionFactoryImplementor factory) {
        this.factory = Arguments.mustNotBeNull("factory", factory);
        
        Lock lock;
        (lock = INSTANCES_LOCK.writeLock()).lock();
        try {
            if (INSTANCES.containsKey(factory)) {
                throw new AssertionError();
            }
            INSTANCES.put(factory, this);
        } finally {
            lock.unlock();
        }
    }
    
    public static ImplicitCollectionJoins getInstance(SessionFactoryImplementor factory) {
        ImplicitCollectionJoins implicitCollectionJoins;
        Lock lock;
        (lock = INSTANCES_LOCK.readLock()).lock();
        try {
            implicitCollectionJoins = INSTANCES.get(factory); //1st reading
        } finally {
            lock.unlock();
        }
        if (implicitCollectionJoins == null) { //1st chekcing
            (lock = INSTANCES_LOCK.writeLock()).lock();
            try {
                implicitCollectionJoins = INSTANCES.get(factory); //2nd reading
                if (implicitCollectionJoins == null) { //2nd checking
                    implicitCollectionJoins = new ImplicitCollectionJoins(factory);
                    INSTANCES.put(factory, implicitCollectionJoins);
                }
            } finally {
                lock.unlock();
            }
        }
        return implicitCollectionJoins;
    }
    
    public SessionFactoryImplementor getFactory() {
        return this.factory;
    }
    
    public boolean hasImplicitCollectionJoinProperties(String entityName) {
        Boolean booleanValue;
        Lock lock;
        (lock = this.cacheLock.readLock()).lock();
        try {
            booleanValue = this.cache.get(entityName); //1st reading
        } finally {
            lock.unlock();
        }
        if (booleanValue == null) { //1st checking
            (lock = this.cacheLock.writeLock()).lock();
            try {
                booleanValue = this.cache.get(entityName); //2nd reading
                if (booleanValue == null) { //2nd checking
                    this.validateWithoutCacheableExceptions(entityName);
                    Map<String, Boolean> contextMap = new HashMap<>();
                    booleanValue = this.hasImplicitCollectionJoins0(entityName, contextMap);
                    this.cache.putAll(contextMap);
                }
            } finally {
                lock.unlock();
            }
        }
        return booleanValue;
    }
    
    private boolean hasImplicitCollectionJoins0(String entityName, Map<String, Boolean> contextMap) {
        
        Boolean preRet = contextMap.get(entityName);
        if (preRet != null) {
            return preRet;
        }
        
        /*
         * Avoid dead recursion
         */
        if (contextMap.containsKey(entityName)) {
            return false;
        }
        contextMap.put(entityName, null);
        
        boolean retval = this.hasImplicitCollectionJoins1(entityName, contextMap);
        contextMap.put(entityName, retval);
        return retval;
    }
    
    private boolean hasImplicitCollectionJoins1(String entityName, Map<String, Boolean> contextMap) {
        EntityPersister persister = this.factory.getEntityPersister(entityName);
        for (NonIdentifierAttribute nonIdAttribute : persister.getEntityMetamodel().getProperties()) {
            Type propertyType = nonIdAttribute.getType();
            if (nonIdAttribute.getFetchMode() == FetchMode.JOIN) {
                if (propertyType.isCollectionType()) { //find collection with {fetch = "join"}
                    return true; 
                }
                if (propertyType instanceof AssociationType) {
                    String childEntityName = ((AssociationType)propertyType).getAssociatedEntityName(this.factory);
                    if (this.hasImplicitCollectionJoins0(childEntityName, contextMap)) {
                        return true;
                    }
                }
            } else if (propertyType instanceof CompositeType) {
                if (this.hasImplicitCollectionJoins0((CompositeType)propertyType, contextMap)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean hasImplicitCollectionJoins0(CompositeType compositeType, Map<String, Boolean> contextMap) {
        Type[] subtypes = compositeType.getSubtypes();
        for (int i = subtypes.length - 1; i >= 0; i--) {
            Type subtype = subtypes[i];
            if (compositeType.getFetchMode(i) == FetchMode.JOIN) {
                if (subtype.isCollectionType()) { //find collection with {fetch = "join"}
                    return true; 
                }
                if (subtype instanceof AssociationType) {
                    String childEntityName = ((AssociationType)subtype).getAssociatedEntityName(this.factory);
                    if (this.hasImplicitCollectionJoins0(childEntityName, contextMap)) {
                        return true;
                    }
                }
            } else if (subtype instanceof CompositeType) {
                if (this.hasImplicitCollectionJoins0((CompositeType)subtype, contextMap)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    //The exception of this method should not be cached
    private void validateWithoutCacheableExceptions(String entityName) {
        this.factory.getEntityPersister(Arguments.mustNotBeNull("entityName", entityName));
    }
}
