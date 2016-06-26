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
package org.babyfish.hibernate.collection.spi.persistence;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.hibernate.collection.spi.PersistentCollection;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.IllegalProgramException;
import org.hibernate.HibernateException;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.Type;

/**
 * @author Tao Chen
 */
public abstract class AbstractBasePersistence<E> implements PersistentCollection<E> {

    private static final long serialVersionUID = 5060512525878325032L;
    
    private Object owner;
    
    private Serializable key;
    
    private String role;
    
    private Serializable storedSnapshot;
    
    private boolean dirty;
    
    AbstractBasePersistence() {
        if (this instanceof Collection<?> || this instanceof Map<?, ?>) {
            throw new IllegalProgramException(
                    "BasePersistence can not be collection");
        }
    }
    
    protected final void setInitialized() {
        this.setInitializing(false);
        this.setInitialized(true);
    }
    
    protected abstract void setInitialized(boolean initialized);
    
    protected abstract void setInitializing(boolean initializing);
    
    public final boolean isConnectedToSession() {
        SessionImplementor session = this.getSession();
        return 
                session != null && 
                session.isOpen() &&
                session.getPersistenceContext().containsCollection(this.getWrapperPersistentCollection());
    }
    
    protected abstract SessionImplementor getSession();
    
    protected abstract void setSession(SessionImplementor session);

    @Override
    public Object getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(Object owner) {
        this.owner = owner;
    }
    
    @Override
    public boolean unsetSession(SessionImplementor currentSession) {
        if (currentSession==this.getSession()) {
            this.setSession(null);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public final boolean setCurrentSession(SessionImplementor session)
            throws HibernateException {
        if (session == this.getSession()) {
            return false;
        } else {
            if (this.isConnectedToSession()) {
                CollectionEntry ce = session.getPersistenceContext().getCollectionEntry(this.getWrapperPersistentCollection());
                if (ce == null) {
                    throw new HibernateException(
                            "Illegal attempt to associate a collection with two open sessions");
                }
                else {
                    throw new HibernateException(
                            "Illegal attempt to associate a collection with two open sessions: " +
                            MessageHelper.collectionInfoString(
                                    ce.getLoadedPersister(), 
                                    ce.getLoadedKey(), 
                                    session.getFactory()));
                }
            }
            else {
                this.setSession(session);
                return true;
            }
        }
    }

    @Override
    public Serializable getKey() {
        return this.key;
    }

    @Override
    public boolean isUnreferenced() {
        return this.role == null;
    }

    @Override
    public String getRole() {
        return this.role;
    }

    @Override
    public String getNonNullRole() {
        String role = this.role;
        if (role == null) {
            throw new IllegalStateException(roleHasNotBeenInitialized(this.getClass()));
        }
        return role;
    }

    @Override
    public Serializable getStoredSnapshot() {
        return this.storedSnapshot;
    }

    @Override
    public void setSnapshot(Serializable key, String role, Serializable snapshot) {
        if (this.role != null && role != null && !this.role.equals(role)) {
            Arguments.mustBeEqualToOther("role", role, "this.role", this.role);
        }
        this.key = key;
        this.role = role;
        this.storedSnapshot = snapshot;
    }

    protected final Serializable getSnapshot() {
        return this.getSession().getPersistenceContext().getSnapshot(this.getWrapperPersistentCollection());
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void clearDirty() {
        this.dirty = false;
    }

    @Override
    public void dirty() {
        this.dirty = true;
    }

    @Override
    public void preInsert(CollectionPersister persister) 
            throws HibernateException {
        
    }

    @Override
    public void afterRowInsert(CollectionPersister persister, Object entry,int i) 
            throws HibernateException {
        
    }

    @Override
    public void postAction() {
        this.clearDirty();
    }

    protected static <E> Collection<E> getOrphans(
            XCollection<E> oldElements, 
            XCollection<E> currentElements, 
            String entityName, 
            SessionImplementor session) throws HibernateException {
    
        // short-circuit(s)
        if (currentElements.isEmpty()) {
            return oldElements; // no new elements, the old list contains only Orphans
        }
        if ( oldElements.size()==0) {
            return oldElements; // no old elements, so no Orphans neither
        }
    
        final EntityPersister entityPersister = session.getFactory().getEntityPersister( entityName );
        final Type idType = entityPersister.getIdentifierType();
    
        // create the collection holding the Orphans
        Collection<E> res = new ArrayList<E>(oldElements.unifiedComparator());
    
        // collect EntityIdentifier(s) of the *current* elements - add them into a HashSet for fast access
        Set<Serializable> currentIds = new HashSet<Serializable>();
        Set<E> currentSaving = new HashSet<E>(ReferenceEqualityComparator.getInstance());
        for (E current : currentElements) {
            if (current != null && ForeignKeys.isNotTransient( entityName, current, null, session)) {
                EntityEntry ee = session.getPersistenceContext().getEntry( current );
                if (ee != null && ee.getStatus() == Status.SAVING) {
                    currentSaving.add( current );
                }
                else {
                    Serializable currentId = ForeignKeys.getEntityIdentifierIfNotUnsaved(
                            entityName,
                            current,
                            session
                    );
                    currentIds.add( new TypedValue(idType, currentId));
                }
            }
        }
    
        // iterate over the *old* list
        for (E old : oldElements ) {
            if ( !currentSaving.contains( old ) ) {
                Serializable oldId = ForeignKeys.getEntityIdentifierIfNotUnsaved( entityName, old, session );
                if (!currentIds.contains( new TypedValue(idType, oldId))) {
                    res.add( old );
                }
            }
        }
    
        return res;
    }
    
    @Override
    public final Object getIdentifier(Object entry, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean needsRecreate(CollectionPersister persister) {
        return false;
    }

    @Deprecated
    @Override
    public final Object getValue() {
        throw new UnsupportedOperationException();
    }
    
    /*
     * Very important point 2:
     * Copy the source code from Hibernate
     * if Hibernate's source code use "this" to be the arguments of other modules,
     * please replace it to this.getWrapper()
     */
    protected final PersistentCollection<E> getWrapperPersistentCollection() {
        PersistentCollection<E> wrapper = this.onGetWrapperPersistentCollection();
        if (wrapper == null) {
            throw new IllegalProgramException(
                    methodCanNotReturnNull(
                            "onGetWrapperPersistentCollection", this.getClass()
                    )
            );
        }
        if (wrapper == this) {
            throw new IllegalProgramException(
                    methodCanNotReturnThis(
                            "onGetWrapperPersistentCollection", this.getClass()
                    )
            );
        }
        return wrapper;
    }
    
    protected abstract PersistentCollection<E> onGetWrapperPersistentCollection();
    
    @I18N    
    private static native String methodCanNotReturnNull(String methodName, Class<?> thisType);
        
    @I18N    
    private static native String methodCanNotReturnThis(String methodName, Class<?> thisType);
        
    @I18N    
    private static native String roleHasNotBeenInitialized(Class<?> thisType);
}
