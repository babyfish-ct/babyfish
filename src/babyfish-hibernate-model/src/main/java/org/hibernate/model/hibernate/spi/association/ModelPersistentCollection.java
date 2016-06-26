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
package org.hibernate.model.hibernate.spi.association;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;

interface ModelPersistentCollection extends PersistentCollection {

    @Deprecated
    @Override
    default void setOwner(Object entity) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean empty() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default void setSnapshot(Serializable key, String role, Serializable snapshot) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default void postAction() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Object getValue() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default void beginRead() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean endRead() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean afterInitialize() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean isDirectlyAccessible() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean unsetSession(SessionImplementor currentSession) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean setCurrentSession(SessionImplementor session)
            throws HibernateException {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default void initializeFromCache(CollectionPersister persister,
            Serializable disassembled, Object owner) throws HibernateException {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Object getIdentifier(Object entry, int i) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Object getIndex(Object entry, int i, CollectionPersister persister) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Object getSnapshotElement(Object entry, int i) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default void beforeInitialize(CollectionPersister persister,
            int anticipatedSize) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean equalsSnapshot(CollectionPersister persister)
            throws HibernateException {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean isSnapshotEmpty(Serializable snapshot) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Serializable disassemble(CollectionPersister persister)
            throws HibernateException {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean needsRecreate(CollectionPersister persister) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Serializable getSnapshot(CollectionPersister persister)
            throws HibernateException {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean entryExists(Object entry, int i) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean needsInserting(Object entry, int i, Type elemType)
            throws HibernateException {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean needsUpdating(Object entry, int i, Type elemType)
            throws HibernateException {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean isRowUpdatePossible() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean isWrapper(Object collection) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean hasQueuedOperations() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Serializable getKey() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default String getRole() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean isUnreferenced() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default boolean isDirty() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default void clearDirty() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Serializable getStoredSnapshot() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default void dirty() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default void preInsert(CollectionPersister persister)
            throws HibernateException {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default void afterRowInsert(CollectionPersister persister, Object entry,
            int i) throws HibernateException {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Object getElement(Object entry) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Iterator<?> entries(CollectionPersister persister) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Object readFrom(ResultSet rs, CollectionPersister persister,
            CollectionAliases descriptor, Object owner)
            throws HibernateException, SQLException {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Iterator<?> getDeletes(CollectionPersister persister,
            boolean indexIsFormula) throws HibernateException {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Collection<?> getOrphans(Serializable snapshot, String entityName)
            throws HibernateException {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Iterator<?> queuedAdditionIterator() {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    default Collection<?> getQueuedOrphans(String entityName) {
        throw new UnsupportedOperationException(
                CommonMessages.persistentCollectionBehaviorIsInvalid(
                        this.getClass(), 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }
}
