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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAList;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XList;
import org.babyfish.lang.IllegalProgramException;
import org.hibernate.HibernateException;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;

/**
 * @author Tao Chen
 */
public abstract class ListBasePersistence<E> extends AbstractBasePersistence<E> {

    private static final long serialVersionUID = -3164892907541459367L;
   
    protected ListBasePersistence() {
        
    }
    
    protected abstract void initBase(MAList<E> base);
    
    protected abstract MAList<E> getBase();
    
    protected abstract UnifiedComparator<? super E> unifiedComparator();

    @Override
    public final Iterator<E> queuedAdditionIterator() {
        return MACollections.<E>emptySet().iterator();
    }

    @Override
    public final Collection<E> getQueuedOrphans(String entityName) {
        return MACollections.<E>emptySet();
    }

    @Override
    public final boolean hasQueuedOperations() {
        return false;
    }

    @Override
    public void beginRead() {
        this.setInitializing(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public E readFrom(ResultSet rs, CollectionPersister persister,
            CollectionAliases descriptor, Object owner)
            throws HibernateException, SQLException {
        List<E> baseList = this.getBase();
        E element = (E)persister.readElement(
                rs, 
                owner, 
                descriptor.getSuffixedElementAliases(), 
                this.getSession()) ;
        int index = ((Integer) persister.readIndex(
                rs, 
                descriptor.getSuffixedIndexAliases(), 
                this.getSession())).intValue();
        //pad with nulls from the current last element up to the new index
        for (int i = baseList.size(); i <= index; i++) {
            baseList.add(i, null);
        }
        baseList.set(index, element);
        return element;
    }

    @Override
    public boolean endRead() {
        return this.afterInitialize();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void beforeInitialize(CollectionPersister persister, int anticipatedSize) {
        Object instantiate = persister.getCollectionType().instantiate(anticipatedSize);
        if (!(instantiate instanceof MAList<?>)) {
            throw new IllegalProgramException(
                    CommonMessages.illegalInstantiate(
                            persister.getCollectionType().getClass(),
                            MAList.class
                    )
            );
        }
        this.initBase((MAList<E>)instantiate);
    }

    @Override
    public boolean afterInitialize() {
        this.setInitialized();
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initializeFromCache(CollectionPersister persister,
            Serializable disassembled, Object owner) throws HibernateException {
        List<E> baseList = this.getBase();
        Serializable[] array = (Serializable[])disassembled;
        int size = array.length;
        beforeInitialize(persister, size);
        for ( int i = 0; i < size; i++ ) {
            baseList.add((E)persister.getElementType().assemble(array[i], getSession(), owner));
        }
    }

    @Override
    public boolean empty() {
        return this.getBase().isEmpty();
    }

    @Override
    public Iterator<E> entries(CollectionPersister persister) {
        return MACollections.unmodifiable(this.getBase().iterator());
    }

    @Override
    public Serializable disassemble(CollectionPersister persister)
            throws HibernateException {
        List<E> baseList = this.getBase();
        int length = baseList.size();
        Serializable[] result = new Serializable[length];
        for ( int i=0; i<length; i++ ) {
            result[i] = persister.getElementType().disassemble(baseList.get(i), getSession(), null);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E getElement(Object entry) {
        return (E)entry;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Iterator<?> getDeletes(
            CollectionPersister persister, boolean indexIsFormula) 
    throws HibernateException {
        List<E> baseList = this.getBase();
        List<Object> deletes = new ArrayList<Object>();
        List<E> sn = (List<E>)this.getSnapshot();
        int end;
        if (sn.size() > baseList.size()) {
            for (int i= baseList.size(); i < sn.size(); i++) {
                deletes.add(indexIsFormula ? sn.get(i) : i);
            }
            end = baseList.size();
        }
        else {
            end = sn.size();
        }
        for (int i=0; i<end; i++) {
            if (baseList.get(i) == null && sn.get(i) != null ) {
                deletes.add(indexIsFormula ? sn.get(i) : i);
            }
        }
        return (Iterator)deletes.iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<E> getOrphans(Serializable snapshot, String entityName)
            throws HibernateException {
        XList<E> sn = (XList<E>)snapshot;
        return getOrphans(sn, this.getBase(), entityName, getSession());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equalsSnapshot(CollectionPersister persister) throws HibernateException {
        List<E> baseList = this.getBase();
        List<E> sn = (List<E>) getSnapshot();
        Type elementType = persister.getElementType();
        if (sn.size()!= baseList.size()) {
            return false;
        }
        Iterator<E> iter = baseList.iterator();
        Iterator<E> sniter = sn.iterator();
        while (iter.hasNext()) {
            if (elementType.isDirty( iter.next(), sniter.next(), getSession())) {
                return false;
            }
        }
        return true;
    }
    
    @SuppressWarnings("unchecked")
    public boolean needsInserting(Object entry, int i, Type elemType) throws HibernateException {
        final List<E> sn = (List<E>)this.getSnapshot();
        List<E> list = this.getBase();
        return list.get(i)!=null && ( i >= sn.size() || sn.get(i)==null );
    }

    @SuppressWarnings("unchecked")
    public boolean needsUpdating(Object entry, int i, Type elemType) throws HibernateException {
        final List<E> sn = (List<E>)this.getSnapshot();
        List<E> list = this.getBase();
        return i<sn.size() && sn.get(i)!=null && list.get(i)!=null &&
            elemType.isDirty( list.get(i), sn.get(i), getSession() );
    }

    @Override
    public boolean isSnapshotEmpty(Serializable snapshot) {
        return ((Collection<?>) snapshot).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Serializable getSnapshot(CollectionPersister persister)
            throws HibernateException {
        MAList<E> baseList = this.getBase();
        ArrayList<E> clonedList = new ArrayList<E>(baseList.unifiedComparator(), baseList.size());
        Iterator<E> iter = baseList.iterator();
        while (iter.hasNext()) {
            E deepCopy = 
                    (E)persister
                    .getElementType()
                    .deepCopy(iter.next(), persister.getFactory());
            clonedList.add(deepCopy);
        }
        return clonedList;
    }

    @Override
    public boolean isRowUpdatePossible() {
        return true;
    }

    @Override
    public boolean isWrapper(Object collection) {
        return this.getBase() == collection;
    }

    @Override
    public final boolean entryExists(Object entry, int i) {
        return entry != null;
    }

    @Override
    public Object getIndex(Object entry, int i, CollectionPersister persister) {
        return i;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getSnapshotElement(Object entry, int i) {
        return ((List<E>)this.getSnapshot()).get(i);
    }

}
