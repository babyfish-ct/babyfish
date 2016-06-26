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
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XMap;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.spi.laziness.AbstractLazyMANavigableMap;
import org.babyfish.collection.spi.laziness.LazyBehaviorProcessor;
import org.babyfish.collection.spi.laziness.QueuedOperationType;
import org.babyfish.hibernate.collection.spi.PersistentCollection;
import org.babyfish.hibernate.collection.spi.persistence.MapBasePersistence;
import org.babyfish.lang.Ref;
import org.babyfish.model.jpa.metadata.JPAModelProperty;
import org.hibernate.HibernateException;
import org.hibernate.LazyInitializationException;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;

/**
 * @author Tao Chen
 */
public class PersistentMANavigableMap<K, V>
extends AbstractLazyMANavigableMap<K, V> 
implements PersistentCollection<V>, Serializable {

    private static final long serialVersionUID = -5776907085003472510L;
    
    public PersistentMANavigableMap(String role, SessionImplementor session, MANavigableMap<K, V> base) {
        super(null);
        RootData<K, V> rootData = this.<RootData<K, V>>getRootData();
        MapBasePersistence<K, V> basePersistence = rootData.basePersistence;
        basePersistence.setCurrentSession(session);
        basePersistence.setSnapshot(null, role, null);
        this.replace(base);
        rootData.setDirectlyAccessible();
    }

    @Override
    protected RootData<K, V> createRootData() {
        return new RootData<K, V>();
    }

    @Override
    protected void onModified(MapElementEvent<K, V> e) throws Throwable {
        this.dirty();
    }

    @Override
    public V getElement(Object entry) {
        return this.<RootData<K, V>>getRootData().basePersistence.getElement(entry);
    }

    @Override
    public Iterator<Entry<K, V>> entries(CollectionPersister persister) {
        return this.<RootData<K, V>>getRootData().basePersistence.entries(persister);
    }

    @Override
    public V readFrom(ResultSet rs, CollectionPersister persister,
            CollectionAliases descriptor, Object owner)
            throws HibernateException, SQLException {
        return this.<RootData<K, V>>getRootData().basePersistence.readFrom(rs, persister, descriptor, owner);
    }

    @Override
    public Iterator<?> getDeletes(CollectionPersister persister,
            boolean indexIsFormula) throws HibernateException {
        return this.<RootData<K, V>>getRootData().basePersistence.getDeletes(persister, indexIsFormula);
    }

    @Override
    public Collection<V> getOrphans(Serializable snapshot, String entityName)
            throws HibernateException {
        return this.<RootData<K, V>>getRootData().basePersistence.getOrphans(snapshot, entityName);
    }

    @Override
    public Iterator<V> queuedAdditionIterator() {
        return this.<RootData<K, V>>getRootData().basePersistence.queuedAdditionIterator();
    }

    @Override
    public Collection<V> getQueuedOrphans(String entityName) {
        return this.<RootData<K, V>>getRootData().basePersistence.getQueuedOrphans(entityName);
    }

    @Override
    public Object getOwner() {
        return this.<RootData<K, V>>getRootData().basePersistence.getOwner();
    }

    @Override
    public void setOwner(Object entity) {
        this.<RootData<K, V>>getRootData().basePersistence.setOwner(entity);
    }

    @Override
    public boolean empty() {
        return this.<RootData<K, V>>getRootData().basePersistence.empty();
    }

    @Override
    public void setSnapshot(Serializable key, String role, Serializable snapshot) {
        this.<RootData<K, V>>getRootData().basePersistence.setSnapshot(key, role, snapshot);
    }

    @Override
    public void postAction() {
        this.<RootData<K, V>>getRootData().basePersistence.postAction();
    }

    @Override
    public Object getValue() {
        return this;
    }

    @Override
    public void beginRead() {
        this.<RootData<K, V>>getRootData().basePersistence.beginRead();
    }

    @Override
    public boolean endRead() {
        return this.<RootData<K, V>>getRootData().basePersistence.endRead();
    }

    @Override
    public boolean afterInitialize() {
        return this.<RootData<K, V>>getRootData().basePersistence.afterInitialize();
    }

    @Override
    public boolean isDirectlyAccessible() {
        return this.<RootData<K, V>>getRootData().basePersistence.isDirectlyAccessible();
    }

    @Override
    public boolean unsetSession(SessionImplementor currentSession) {
        return this.<RootData<K, V>>getRootData().basePersistence.unsetSession(currentSession);
    }

    @Override
    public boolean setCurrentSession(SessionImplementor session)
            throws HibernateException {
        return this.<RootData<K, V>>getRootData().basePersistence.setCurrentSession(session);
    }

    @Override
    public void initializeFromCache(CollectionPersister persister,
            Serializable disassembled, Object owner) throws HibernateException {
        this.<RootData<K, V>>getRootData().basePersistence.initializeFromCache(persister, disassembled, owner);
    }

    @Override
    public Object getIdentifier(Object entry, int i) {
        return this.<RootData<K, V>>getRootData().basePersistence.getIdentifier(entry, i);
    }

    @Override
    public K getIndex(Object entry, int i, CollectionPersister persister) {
        return this.<RootData<K, V>>getRootData().basePersistence.getIndex(entry, i, persister);
    }

    @Override
    public Object getSnapshotElement(Object entry, int i) {
        return this.<RootData<K, V>>getRootData().basePersistence.getSnapshotElement(entry, i);
    }

    @Override
    public void beforeInitialize(CollectionPersister persister,
            int anticipatedSize) {
        this.<RootData<K, V>>getRootData().basePersistence.beforeInitialize(persister, anticipatedSize);
    }

    @Override
    public boolean equalsSnapshot(CollectionPersister persister)
            throws HibernateException {
        return this.<RootData<K, V>>getRootData().basePersistence.equalsSnapshot(persister);
    }

    @Override
    public boolean isSnapshotEmpty(Serializable snapshot) {
        return this.<RootData<K, V>>getRootData().basePersistence.isSnapshotEmpty(snapshot);
    }

    @Override
    public Serializable disassemble(CollectionPersister persister)
            throws HibernateException {
        return this.<RootData<K, V>>getRootData().basePersistence.disassemble(persister);
    }

    @Override
    public boolean needsRecreate(CollectionPersister persister) {
        return this.<RootData<K, V>>getRootData().basePersistence.needsRecreate(persister);
    }

    @Override
    public Serializable getSnapshot(CollectionPersister persister)
            throws HibernateException {
        return this.<RootData<K, V>>getRootData().basePersistence.getSnapshot(persister);
    }

    @Override
    public void forceInitialization() throws HibernateException {
        this.<RootData<K, V>>getRootData().basePersistence.forceInitialization();
    }

    @Override
    public boolean entryExists(Object entry, int i) {
        return this.<RootData<K, V>>getRootData().basePersistence.entryExists(entry, i);
    }

    @Override
    public boolean needsInserting(Object entry, int i, Type elemType)
            throws HibernateException {
        return this.<RootData<K, V>>getRootData().basePersistence.needsInserting(entry, i, elemType);
    }

    @Override
    public boolean needsUpdating(Object entry, int i, Type elemType)
            throws HibernateException {
        return this.<RootData<K, V>>getRootData().basePersistence.needsUpdating(entry, i, elemType);
    }

    @Override
    public boolean isRowUpdatePossible() {
        return this.<RootData<K, V>>getRootData().basePersistence.isRowUpdatePossible();
    }

    @Override
    public boolean isWrapper(Object collection) {
        return this.<RootData<K, V>>getRootData().basePersistence.isWrapper(collection);
    }

    @Override
    public boolean wasInitialized() {
        return this.<RootData<K, V>>getRootData().basePersistence.wasInitialized();
    }

    @Override
    public boolean hasQueuedOperations() {
        return this.<RootData<K, V>>getRootData().basePersistence.hasQueuedOperations();
    }

    @Override
    public Serializable getKey() {
        return this.<RootData<K, V>>getRootData().basePersistence.getKey();
    }

    @Override
    public String getRole() {
        return this.<RootData<K, V>>getRootData().basePersistence.getRole();
    }
    
    @Override
    public String getNonNullRole() {
        return this.<RootData<K, V>>getRootData().basePersistence.getNonNullRole();
    }

    @Override
    public boolean isUnreferenced() {
        return this.<RootData<K, V>>getRootData().basePersistence.isUnreferenced();
    }

    @Override
    public boolean isDirty() {
        return this.<RootData<K, V>>getRootData().basePersistence.isDirty();
    }

    @Override
    public void clearDirty() {
        this.<RootData<K, V>>getRootData().basePersistence.clearDirty();
    }

    @Override
    public Serializable getStoredSnapshot() {
        return this.<RootData<K, V>>getRootData().basePersistence.getStoredSnapshot();
    }

    @Override
    public void dirty() {
        this.<RootData<K, V>>getRootData().basePersistence.dirty();
    }

    @Override
    public void preInsert(CollectionPersister persister)
            throws HibernateException {
        this.<RootData<K, V>>getRootData().basePersistence.preInsert(persister);
    }

    @Override
    public void afterRowInsert(CollectionPersister persister, Object entry,
            int i) throws HibernateException {
        this.<RootData<K, V>>getRootData().basePersistence.afterRowInsert(persister, entry, i);
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        this.writeState(out);
    }
    
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        this.readState(in);
    }

    protected static class RootData<K, V> extends AbstractLazyMANavigableMap.RootData<K, V> {
        
        private static final long serialVersionUID = 5614271070791272595L;

        private boolean loaded;
        
        private MapBasePersistence<K, V> basePersistence;
        
        private transient boolean loading;
        
        private transient SessionImplementor session;
        
        private transient JPAModelProperty modelProperty;
        
        private transient boolean directlyAccessible;
        
        public RootData() {
            
        }
    
        protected void onInitialize() {
            MANavigableMap<K, V> base = this.getBase(true);
            if (base != null) {
                this.directlyAccessible = true;
                this.loading = false;
                this.loaded = true;
            }
            this.basePersistence = new MapBasePersistence<K, V>() {
    
                private static final long serialVersionUID = -3476080660064401960L;
    
                @Override
                public boolean isDirectlyAccessible() {
                    return RootData.this.directlyAccessible;
                }
    
                @Override
                public void forceInitialization() throws HibernateException {
                    RootData.this.load();
                }
    
                @Override
                public boolean wasInitialized() {
                    return RootData.this.loaded;
                }
    
                @Override
                public boolean hasQueuedOperations() {
                    return RootData.this.hasQueuedOrphans();
                }
    
                @Override
                protected MANavigableMap<K, V> getBase() {
                    return RootData.this.getBase();
                }
    
                @Override
                protected void initBase(MAMap<K, V> base) {
                    RootData.this.setBase((MANavigableMap<K, V>)base);
                }
    
                @Override
                protected XMap<K, V> getQueuedOrphans() {
                    return RootData.this.getQueuedOrphans();
                }
    
                @Override
                protected void preformQueuedOrphans() {
                    RootData.this.performQueuedOrphans();
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
    
                @Override
                protected PersistentCollection<V> onGetWrapperPersistentCollection() {
                    return RootData.this.<PersistentMANavigableMap<K, V>>getRootWrapper();
                }
            };
        }
        
        public final MapBasePersistence<K, V> getBasePersistence() {
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
            MANavigableMap<K, V> base = this.getBase(true);
            if (base != null) {
                this.directlyAccessible = true;
                this.loading = false;
                this.loaded = true;
            }
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
            session.initializeCollection(this.<PersistentMANavigableMap<K, V>>getRootWrapper(), false);
        }
        
        @Override
        protected UnifiedComparator<? super K> getDefaultKeyUnifiedComparator() {
            return
                    this.basePersistence.getRole() == null ?
                    null :
                    (UnifiedComparator<? super K>)
                    this.getModelProperty().getKeyUnifiedComparator();
        }
        
        @Override
        protected UnifiedComparator<? super V> getDefaultValueUnifiedComparator() {
            return 
                    this.basePersistence.getRole() == null ?
                    null :
                    (UnifiedComparator<? super V>)
                    this.getModelProperty().getCollectionUnifiedComparator();
        }
        
        @Override
        protected MANavigableMap<K, V> createDefaultBase(
                UnifiedComparator<? super K> keyUnifiedComparator,
                UnifiedComparator<? super V> valueUnifiedComparator) {
            return new MATreeMap<>(
                    BidiType.NONNULL_VALUES,
                    keyUnifiedComparator.comparator(true),
                    valueUnifiedComparator
            );
        }

        @Override
        protected boolean onGetVisionallyReadable(QueuedOperationType nullOrOperationType) {
            if (nullOrOperationType != null) {
                return this.modelProperty.isInverse();
            }
            return super.onGetVisionallyReadable(nullOrOperationType);
        }

        @Override
        protected int onGetVisionalSize() {
            SessionImplementor session = this.session;
            CollectionEntry ce = 
                    session
                    .getPersistenceContext()
                    .getCollectionEntry(this.<PersistentMANavigableMap<K, V>>getRootWrapper());
            CollectionPersister persister = ce.getLoadedPersister();
            if (!persister.isInverse()) {
                return -1;
            }
            if (session != null && this.hasQueuedOrphans()) {
                session.flush();
            }
            return persister.getSize(ce.getLoadedKey(), session);
        }

        @Override
        protected Ref<V> onVisionallyRead(K key, QueuedOperationType nullOrOperationType) {
            SessionImplementor session = this.session;
            if (session != null && this.hasQueuedOrphans()) {
                session.flush();
            }
            return this.basePersistence.visionallyRead(key);
        }

        @Override
        protected LazyBehaviorProcessor createLazyBehaviorProcessor() {
            // TODO: Add annotation
            return LazyBehaviorProcessor.of(16, 2);
        }
    }
}
