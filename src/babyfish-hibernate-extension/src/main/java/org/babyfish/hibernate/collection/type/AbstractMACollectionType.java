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
package org.babyfish.hibernate.collection.type;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.model.jpa.metadata.JPAModelProperty;
import org.hibernate.LazyInitializationException;
import org.hibernate.bytecode.instrumentation.spi.LazyPropertyInitializer;
import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.CustomCollectionType;
import org.hibernate.type.Type;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserCollectionType;

/**
 * @author Tao Chen
 */
public abstract class AbstractMACollectionType implements UserCollectionType, ParameterizedType {
    
    private JPAModelProperty modelProperty;
    
    private String role;
    
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    
    @Deprecated
    @Override
    public final void setParameterValues(Properties parameters) {
        this.setParameterValues((MACollectionProperties)parameters);
    }
    
    public final void setParameterValues(MACollectionProperties parameters) {
        Arguments.mustNotBeNull("parameters", parameters);
        Lock lock;
        (lock = this.readWriteLock.writeLock()).lock();
        try { 
            if (this.modelProperty != null) {
                throw new IllegalStateException(parameterValuesHaveBeenSet());
            }
            this.modelProperty = parameters.getModelProperty();
        } finally {
            lock.unlock();
        }
    }
    
    protected final JPAModelProperty getModelProperty() {
        Lock lock;
        (lock = this.readWriteLock.readLock()).lock();
        try {
            JPAModelProperty modelProperty = this.modelProperty;
            if (modelProperty == null) {
                throw new IllegalStateException(parameterValuesHaveNotBeenSet());
            }
            return modelProperty;
        } finally {
            lock.unlock();
        }
    }
    
    protected final String getRole() {
        String role = this.role;
        JPAModelProperty property = this.modelProperty;
        if (role == null && property != null) {
            this.role = role = 
                    property.getDeclaringlClass().getJavaType().getName() +
                    '.' +
                    property.getName();
        }
        return role;
    }
    
    @Override
    public final Iterator<?> getElementsIterator(Object collection) {
        /*
         *  In order to support disablity of ObjectModel, 
         *  collection may be LazyPropertyInitializer.UNFETCHED_PROPERTY
         */
        if (collection == LazyPropertyInitializer.UNFETCHED_PROPERTY) {
            return MACollections.emptySet().iterator();
        }
        return this.onGetElementsIterator(collection);
    }

    @Override
    public final boolean contains(Object collection, Object entity) {
        /*
         *  In order to support disablity of ObjectModel, 
         *  collection may be LazyPropertyInitializer.UNFETCHED_PROPERTY
         */
        if (collection == LazyPropertyInitializer.UNFETCHED_PROPERTY) {
            return false;
        }
        return this.onContains(collection, entity);
    }

    @Override
    public final Object indexOf(Object collection, Object entity) {
        /*
         *  In order to support disablity of ObjectModel, 
         *  collection may be LazyPropertyInitializer.UNFETCHED_PROPERTY
         */
        if (collection == LazyPropertyInitializer.UNFETCHED_PROPERTY) {
            return null;
        }
        return this.onIndexOf(collection, entity);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public final Object replaceElements(
            Object original, 
            Object target,
            CollectionPersister persister, 
            Object owner, 
            Map copyCache,
            SessionImplementor session) {
        
        /*
         *  In order to support disablity of ObjectModel, 
         *  collection may be LazyPropertyInitializer.UNFETCHED_PROPERTY
         */
        if (original == LazyPropertyInitializer.UNFETCHED_PROPERTY ||
                target == LazyPropertyInitializer.UNFETCHED_PROPERTY) {
            return target;
        }
        return this.onReplaceElements(
                original, 
                target, 
                persister, 
                owner, 
                copyCache, 
                session);
    }

    protected Iterator<?> onGetElementsIterator(Object collection) {
        return this.getClonedIterator((Collection<?>)collection);
    }

    protected boolean onContains(Object collection, Object entity) {
        return ((Collection<?>)collection).contains(entity);
    }

    protected Object onIndexOf(Object collection, Object entity) {
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) 
    protected Object onReplaceElements(
            Object original, 
            Object target,
            CollectionPersister persister, 
            Object owner, 
            Map copyCache,
            SessionImplementor session) {
        // Must before result.clear()
        Iterator itr = this.getClonedIterator((java.util.Collection)original);
        
        /*
         *  TODO:
         *  This code is copy from hibernate's CollectionType class.
         *  In its source code, hibernate's author have wrote another 
         *  _TODO_ comment "does not work for EntityMode.DOM4J yet!"
         *  So here, I must check the source code of newest version 
         *  hibernate to make sure whether these code should be changed. 
         */
        java.util.Collection result = (java.util.Collection)target;
        result.clear();

        // copy elements into newly empty target collection
        Type elemType = persister.getElementType();
        while (itr.hasNext()) {
            result.add(elemType.replace(itr.next(), null, session, owner, copyCache));
        }

        // if the original is a PersistentCollection, and that original
        // was not flagged as dirty, then reset the target's dirty flag
        // here after the copy operation.
        // </p>
        // One thing to be careful of here is a "bare" original collection
        // in which case we should never ever ever reset the dirty flag
        // on the target because we simply do not know...
        if (original instanceof PersistentCollection) {
            if (result instanceof PersistentCollection) {
                if (!((PersistentCollection)original).isDirty()) {
                    ((PersistentCollection)result).clearDirty();
                }
            }
        }

        return result;
    }
    
    protected final Iterator<?> getClonedIterator(Collection<?> collection) {
        /*
         * BabyFish supports mutable collection element.
         * All the sets/maps contain an element will be adjusted when the field(s) 
         * can affect the hashCode, equality behavior or comparator behavior of 
         * this element is changed.
         * 
         * Hibernate/JPA supports id-generation and CascaseType.PERSIST,
         * When we persist a parent object with child objects,
         * if the child object supports id-generation and the parent object supports
         * CascadeType.PERSIST for the child object, the origianl(non-cloned) iterator
         * can cause some problems:
         * 
         * (1) Hibernate get the iterator to persist the child objects one by one,
         * when one child object is saved, its id will be changed to the generated
         * value by Hibernate.
         * (2) When id is changed, if the collection "parent.childObjects" use the id
         * to implement the hashCode, equals or comparator behavior, the collection will
         * be changed, the element will be removed from the collection(suspended) 
         * before its changing and it will be re-added into the collection(resumed)
         * after its changing. 
         * (3) Hibernate continue to use this iterator whose collection has already
         * been changed to persist other child ojects. Of course, 
         * java.util.ConcurrentModificationException will raise.
         * 
         * So, let's return the cloned-iterator to hibernate.
         */
        final Object[] arr = collection.toArray();
        return new Iterator<Object>() {
            
            private int index;
            
            @Override
            public boolean hasNext() {
                return this.index < arr.length;
            }

            @Override
            public Object next() {
                 if (this.index >= arr.length) {
                     throw new NoSuchElementException();
                 }
                 return arr[this.index++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    protected static Object instantiate(CollectionPersister persister, int anticipatedSize) {
        return 
        ((AbstractMACollectionType)((CustomCollectionType)persister.getCollectionType()).getUserType())
        .instantiate(anticipatedSize);
    }
    
    protected static void throwLazyInitializationExceptionIfNotConnected(AbstractPersistentCollection c) {
        if (!isConnectedToSession(c))  {
            throwLazyInitializationException(c, "no session or session was closed");
        }
        if (!c.getSession().isConnected()) {
            throwLazyInitializationException(c, "session is disconnected");
        }
    }
    
    protected static void throwLazyInitializationException(AbstractPersistentCollection c, String message) {
        throw new LazyInitializationException(
                "failed to lazily initialize a collection" + 
                (c.getRole()==null ?  "" : " of role: " + c.getRole()) + 
                ", " + 
                message);
    }
    
    protected static boolean isConnectedToSession(AbstractPersistentCollection c) {
        SessionImplementor session = c.getSession();
        return session!=null && 
                session.isOpen() &&
                session.getPersistenceContext().containsCollection(c);
    }
    
    @I18N
    private static native String parameterValuesHaveBeenSet();
        
    @I18N
    private static native String parameterValuesHaveNotBeenSet();
}
