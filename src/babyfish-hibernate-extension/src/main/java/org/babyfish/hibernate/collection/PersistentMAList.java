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
package org.babyfish.hibernate.collection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MAList;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.spi.laziness.AbstractLazyMAList;
import org.babyfish.hibernate.collection.spi.PersistentCollection;
import org.babyfish.hibernate.collection.spi.persistence.ListBasePersistence;
import org.babyfish.model.jpa.metadata.JPAModelProperty;
import org.hibernate.HibernateException;
import org.hibernate.LazyInitializationException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;

/**
 * @author Tao Chen
 */
public class PersistentMAList<E> extends AbstractLazyMAList<E> implements PersistentCollection<E> {

    private static final long serialVersionUID = 7158927227202022171L;
    
    public PersistentMAList(String role, SessionImplementor session, MAList<E> base) {
        super(null);
        RootData<E> rootData = this.<RootData<E>>getRootData();
        ListBasePersistence<E> basePersistence = rootData.basePersistence;
        basePersistence.setCurrentSession(session);
        basePersistence.setSnapshot(null, role, null);
        this.replace(base);
        rootData.setDirectlyAccessible();
    }

    @Override
    protected RootData<E> createRootData() {
        return new RootData<E>();
    }
    
    @Override
    protected void onModified(ListElementEvent<E> e) throws Throwable {
        this.dirty();
    }

    @Override
    public E getElement(Object entry) {
        return this.<RootData<E>>getRootData().basePersistence.getElement(entry);
    }

    @Override
    public Iterator<E> entries(CollectionPersister persister) {
        return this.<RootData<E>>getRootData().basePersistence.entries(persister);
    }

    @Override
    public E readFrom(ResultSet rs, CollectionPersister persister,
            CollectionAliases descriptor, Object owner)
            throws HibernateException, SQLException {
        return this.<RootData<E>>getRootData().basePersistence.readFrom(rs, persister, descriptor, owner);
    }

    @Override
    public Iterator<?> getDeletes(CollectionPersister persister,
            boolean indexIsFormula) throws HibernateException {
        return this.<RootData<E>>getRootData().basePersistence.getDeletes(persister, indexIsFormula);
    }

    @Override
    public Collection<E> getOrphans(Serializable snapshot, String entityName)
            throws HibernateException {
        return this.<RootData<E>>getRootData().basePersistence.getOrphans(snapshot, entityName);
    }

    @Override
    public Iterator<E> queuedAdditionIterator() {
        return this.<RootData<E>>getRootData().basePersistence.queuedAdditionIterator();
    }

    @Override
    public Collection<E> getQueuedOrphans(String entityName) {
        return this.<RootData<E>>getRootData().basePersistence.getQueuedOrphans(entityName);
    }

    @Override
    public Object getOwner() {
        return this.<RootData<E>>getRootData().basePersistence.getOwner();
    }

    @Override
    public void setOwner(Object entity) {
        this.<RootData<E>>getRootData().basePersistence.setOwner(entity);
    }

    @Override
    public boolean empty() {
        return this.<RootData<E>>getRootData().basePersistence.empty();
    }

    @Override
    public void setSnapshot(Serializable key, String role, Serializable snapshot) {
        this.<RootData<E>>getRootData().basePersistence.setSnapshot(key, role, snapshot);
    }

    @Override
    public void postAction() {
        this.<RootData<E>>getRootData().basePersistence.postAction();
    }

    @Override
    public Object getValue() {
        return this;
    }

    @Override
    public void beginRead() {
        this.<RootData<E>>getRootData().basePersistence.beginRead();
    }

    @Override
    public boolean endRead() {
        return this.<RootData<E>>getRootData().basePersistence.endRead();
    }

    @Override
    public boolean afterInitialize() {
        return this.<RootData<E>>getRootData().basePersistence.afterInitialize();
    }

    @Override
    public boolean isDirectlyAccessible() {
        return this.<RootData<E>>getRootData().basePersistence.isDirectlyAccessible();
    }

    @Override
    public boolean unsetSession(SessionImplementor currentSession) {
        return this.<RootData<E>>getRootData().basePersistence.unsetSession(currentSession);
    }

    @Override
    public boolean setCurrentSession(SessionImplementor session)
            throws HibernateException {
        return this.<RootData<E>>getRootData().basePersistence.setCurrentSession(session);
    }

    @Override
    public void initializeFromCache(CollectionPersister persister,
            Serializable disassembled, Object owner) throws HibernateException {
        this.<RootData<E>>getRootData().basePersistence.initializeFromCache(persister, disassembled, owner);
    }

    @Override
    public Object getIdentifier(Object entry, int i) {
        return this.<RootData<E>>getRootData().basePersistence.getIdentifier(entry, i);
    }

    @Override
    public Object getIndex(Object entry, int i, CollectionPersister persister) {
        return this.<RootData<E>>getRootData().basePersistence.getIndex(entry, i, persister);
    }

    @Override
    public Object getSnapshotElement(Object entry, int i) {
        return this.<RootData<E>>getRootData().basePersistence.getSnapshotElement(entry, i);
    }

    @Override
    public void beforeInitialize(CollectionPersister persister,
            int anticipatedSize) {
        this.<RootData<E>>getRootData().basePersistence.beforeInitialize(persister, anticipatedSize);
    }

    @Override
    public boolean equalsSnapshot(CollectionPersister persister)
            throws HibernateException {
        return this.<RootData<E>>getRootData().basePersistence.equalsSnapshot(persister);
    }

    @Override
    public boolean isSnapshotEmpty(Serializable snapshot) {
        return this.<RootData<E>>getRootData().basePersistence.isSnapshotEmpty(snapshot);
    }

    @Override
    public Serializable disassemble(CollectionPersister persister)
            throws HibernateException {
        return this.<RootData<E>>getRootData().basePersistence.disassemble(persister);
    }

    @Override
    public boolean needsRecreate(CollectionPersister persister) {
        return this.<RootData<E>>getRootData().basePersistence.needsRecreate(persister);
    }

    @Override
    public Serializable getSnapshot(CollectionPersister persister)
            throws HibernateException {
        return this.<RootData<E>>getRootData().basePersistence.getSnapshot(persister);
    }

    @Override
    public void forceInitialization() throws HibernateException {
        this.<RootData<E>>getRootData().basePersistence.forceInitialization();
    }

    @Override
    public boolean entryExists(Object entry, int i) {
        return this.<RootData<E>>getRootData().basePersistence.entryExists(entry, i);
    }

    @Override
    public boolean needsInserting(Object entry, int i, Type elemType)
            throws HibernateException {
        return this.<RootData<E>>getRootData().basePersistence.needsInserting(entry, i, elemType);
    }

    @Override
    public boolean needsUpdating(Object entry, int i, Type elemType)
            throws HibernateException {
        return this.<RootData<E>>getRootData().basePersistence.needsUpdating(entry, i, elemType);
    }

    @Override
    public boolean isRowUpdatePossible() {
        return this.<RootData<E>>getRootData().basePersistence.isRowUpdatePossible();
    }

    @Override
    public boolean isWrapper(Object collection) {
        return this.<RootData<E>>getRootData().basePersistence.isWrapper(collection);
    }

    @Override
    public boolean wasInitialized() {
        return this.<RootData<E>>getRootData().basePersistence.wasInitialized();
    }

    @Override
    public boolean hasQueuedOperations() {
        return this.<RootData<E>>getRootData().basePersistence.hasQueuedOperations();
    }

    @Override
    public Serializable getKey() {
        return this.<RootData<E>>getRootData().basePersistence.getKey();
    }

    @Override
    public String getRole() {
        return this.<RootData<E>>getRootData().basePersistence.getRole();
    }

    @Override
    public String getNonNullRole() {
        return this.<RootData<E>>getRootData().basePersistence.getNonNullRole();
    }

    @Override
    public boolean isUnreferenced() {
        return this.<RootData<E>>getRootData().basePersistence.isUnreferenced();
    }

    @Override
    public boolean isDirty() {
        return this.<RootData<E>>getRootData().basePersistence.isDirty();
    }

    @Override
    public void clearDirty() {
        this.<RootData<E>>getRootData().basePersistence.clearDirty();
    }

    @Override
    public Serializable getStoredSnapshot() {
        return this.<RootData<E>>getRootData().basePersistence.getStoredSnapshot();
    }

    @Override
    public void dirty() {
        this.<RootData<E>>getRootData().basePersistence.dirty();
    }

    @Override
    public void preInsert(CollectionPersister persister)
            throws HibernateException {
        this.<RootData<E>>getRootData().basePersistence.preInsert(persister);
    }

    @Override
    public void afterRowInsert(CollectionPersister persister, Object entry,
            int i) throws HibernateException {
        this.<RootData<E>>getRootData().basePersistence.afterRowInsert(persister, entry, i);
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        this.writeState(out);
    }
    
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        this.readState(in);
    }
    
    protected static class RootData<E> extends AbstractLazyMAList.RootData<E> {

        private static final long serialVersionUID = -2618782897285373582L;

        private boolean loaded;
        
        private ListBasePersistence<E> basePersistence;
        
        private transient boolean loading;
        
        private transient SessionImplementor session;
        
        private transient JPAModelProperty modelProperty;
        
        private transient boolean directlyAccessible;
        
        public RootData() {
            
        }
        
        protected void onInitialize() {
            MAList<E> base = this.getBase(true);
            if (base != null) {
                this.directlyAccessible = true;
                this.loading = false;
                this.loaded = true;
            }
            this.basePersistence = new ListBasePersistence<E>() {
                
                private static final long serialVersionUID = 8607658837550099941L;

                @Override
                public void forceInitialization() throws HibernateException {
                    RootData.this.load();
                }
                
                @Override
                protected void initBase(MAList<E> base) {
                    RootData.this.setBase(base);
                }

                @Override
                protected MAList<E> getBase() {
                    return RootData.this.getBase();
                }

                @Override
                protected UnifiedComparator<? super E> unifiedComparator() {
                    return RootData.this.unifiedComparator();
                }

                @Override
                protected PersistentCollection<E> onGetWrapperPersistentCollection() {
                    return RootData.this.<PersistentMAList<E>>getRootWrapper();
                }

                @Override
                public boolean isDirectlyAccessible() {
                    return RootData.this.directlyAccessible;
                }

                @Override
                public boolean wasInitialized() {
                    return RootData.this.loaded;
                }

                @Override
                protected void setInitialized(boolean initialized) {
                    RootData.this.loaded = initialized;
                }

                @Override
                protected void setInitializing(boolean initializing) {
                    RootData.this.loading = initializing;
                }

                @Override
                protected SessionImplementor getSession() {
                    return RootData.this.session;
                }

                @Override
                protected void setSession(SessionImplementor session) {
                    RootData.this.session = session;
                }
            };
        }
        
        public final ListBasePersistence<E> getBasePersistence() {
            return this.basePersistence;
        }

        public final JPAModelProperty getModelProperty() {
            JPAModelProperty modelProperty = this.modelProperty;
            if (modelProperty == null) {
                String role = this.basePersistence.getNonNullRole();
                this.modelProperty = modelProperty = JPAModelPropertyFactory.of(role);
            }
            return modelProperty;
        }

        @Override
        public final boolean isLoaded() {
            return this.loaded;
        }

        @Override
        public final boolean isLoading() {
            return this.loading;
        }

        @Override
        public final boolean isLoadable() {
            return this.basePersistence.isConnectedToSession();
        }

        @Override
        protected final void setLoaded(boolean loaded) {
            this.loaded = loaded;
        }

        @Override
        protected final void setLoading(boolean loading) {
            this.loading = loading;
        }

        protected final void setDirectlyAccessible() {
            MAList<E> base = this.getBase(true);
            if (base != null) {
                this.directlyAccessible = true;
                this.loading = false;
                this.loaded = true;
            }
        }

        @Override
        protected UnifiedComparator<? super E> getDefaultUnifiedComparator() {
            return 
                    this.basePersistence.getRole() == null ?
                    null :
                    (UnifiedComparator<? super E>)
                    this.getModelProperty().getCollectionUnifiedComparator();
        }

        @Override
        protected MAList<E> createDefaultBase(UnifiedComparator<? super E> unifiedComparator) {
            return new MAArrayList<>(BidiType.NONNULL_VALUES, unifiedComparator);
        }

        @Override
        protected boolean onGetIsReadableVision() {
            return this.getModelProperty().isInverse();
        }

        @Override
        protected void onLoad() {
            SessionImplementor session = this.session;
            if (session==null) {
                throw new LazyInitializationException(
                        CommonMessages.rootTypeRetainNoSession(this.getClass(), SessionImplementor.class)
                );
            }
            if (!session.isConnected()) {
                throw new LazyInitializationException(
                        CommonMessages.rootTypeRetainDisconnectedSession(this.getClass(), SessionImplementor.class)
                );
            }
            session.initializeCollection(this.<PersistentMAList<E>>getRootWrapper(), false);
        }
    }
    
}
