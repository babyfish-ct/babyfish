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
package org.babyfish.hibernate.collection.spi;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.hibernate.HibernateException;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;

/**
 * @author Tao Chen
 */
public interface PersistentCollection<E> extends org.hibernate.collection.spi.PersistentCollection, Serializable {
    
    String getNonNullRole();
    
    @Override
    E getElement(Object entry);
    
    @Override
    E readFrom(
            ResultSet rs, 
            CollectionPersister persister,
            CollectionAliases descriptor, 
            Object owner) throws HibernateException, SQLException;
    
    @Override
    Collection<E> getOrphans(
            Serializable snapshot, 
            String entityName) throws HibernateException;
    
    @Override
    Iterator<E> queuedAdditionIterator();
    
    @Override
    Collection<E> getQueuedOrphans(String entityName);
    
}
