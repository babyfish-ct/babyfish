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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MASet;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XSet;
import org.babyfish.collection.spi.laziness.QueuedOperationType;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.Ref;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.internal.JoinHelper;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.type.Type;

/**
 * @author Tao Chen
 */
public abstract class SetBasePersistence<E> extends AbstractBasePersistence<E> {
    
    private static final long serialVersionUID = 4884987773691896886L;
    
    private static final String MIDDLE_TABLE_ALIAS = "babyfish_mt_alias_";

    private transient List<E> tempList;
    
    protected SetBasePersistence() {
        
    }

    protected abstract void setBase(MASet<E> base);
    
    protected abstract MASet<E> getBase();
    
    protected abstract UnifiedComparator<? super E> unifiedComparator();
    
    protected abstract XMap<E, QueuedOperationType> getQueuedOperations();
    
    protected abstract void performQueuedOperations();

    @SuppressWarnings("unchecked")
    @Override
    public Serializable getSnapshot(CollectionPersister persister) throws HibernateException {
        XSet<E> baseSet = this.getBase();
        UnifiedComparator<? super E> unifiedComparator = baseSet.unifiedComparator();
        Map<E, E> clonedMap;
        if (unifiedComparator.comparator() != null) {
            clonedMap = new TreeMap<E, E>(
                    unifiedComparator.comparator(), 
                    unifiedComparator.comparator());
        } else { 
            clonedMap = new HashMap<E, E>(
                    unifiedComparator.equalityComparator(),
                    unifiedComparator.equalityComparator(),
                    baseSet.size() + 1,
                    1.F); 
        }
        for (E e : baseSet) {
            E copied = (E)persister.getElementType().deepCopy(
                    e, 
                    persister.getFactory());
            clonedMap.put(copied, copied);
        }
        return (Serializable)clonedMap;

    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equalsSnapshot(CollectionPersister persister)
            throws HibernateException {
        Set<E> baseSet = this.getBase();
        Type elementType = persister.getElementType();
        java.util.Map<E, E> sn = (java.util.Map<E, E>)this.getSnapshot();
        if ( sn.size() != baseSet.size() ) {
            return false;
        }
        else {
            Iterator<E> iter = baseSet.iterator();
            while ( iter.hasNext() ) {
                Object test = iter.next();
                Object oldValue = sn.get(test);
                if (oldValue==null || elementType.isDirty(oldValue, test, this.getSession())) {
                    return false;
                }
            }
            return true;
        }
    }
    
    @Override
    public boolean needsInserting(Object entry, int i, Type elemType) throws HibernateException {
        Map<?, ?> sn = (Map<?, ?>)getSnapshot();
        Object oldValue = sn.get(entry);
        // note that it might be better to iterate the snapshot but this is safe,
        // assuming the user implements equals() properly, as required by the Set
        // contract!
        return oldValue==null || elemType.isDirty( oldValue, entry, this.getSession());
    }
    
    @Override
    public boolean needsUpdating(Object entry, int i, Type elemType)
            throws HibernateException {
        return false;
    }

    @Override
    public boolean isSnapshotEmpty(Serializable snapshot) {
        return ((Map<?, ?>)snapshot).isEmpty();
    }

    @Override
    public void beginRead() {
        this.setInitializing(true);
        this.tempList = new ArrayList<E>(this.unifiedComparator());
    }

    @SuppressWarnings("unchecked")
    @Override
    public E readFrom(
            ResultSet rs, 
            CollectionPersister persister,
            CollectionAliases descriptor, 
            Object owner)
            throws HibernateException, SQLException {
        E element = (E)persister.readElement(
                rs, 
                owner,
                descriptor.getSuffixedElementAliases(),
                this.getSession());
        if (element != null) { 
            this.tempList.add(element); 
        }
        return element;
    }

    @Override
    public boolean endRead() {
        this.getBase().addAll(this.tempList);
        this.tempList = null;
        this.setInitialized();
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void beforeInitialize(CollectionPersister persister, int anticipatedSize) {
        Object instantiate = persister.getCollectionType().instantiate(anticipatedSize);
        if (!(instantiate instanceof MASet<?>)) {
            throw new IllegalProgramException(
                    CommonMessages.illegalInstantiate(
                            persister.getCollectionType().getClass(), 
                            MASet.class
                    )
            );
        }
        this.setBase((MASet<E>)instantiate);
    }

    @Override
    public boolean afterInitialize() {
        this.setInitialized();
        if (this.hasQueuedOperations()) {
            this.performQueuedOperations();
            return false;
        }
        else {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initializeFromCache(
            CollectionPersister persister,
            Serializable disassembled, 
            Object owner) throws HibernateException {
        Set<E> baseSet = this.getBase();
        Serializable[] arr = (Serializable[])disassembled;
        int size = arr.length;
        this.beforeInitialize( persister, size );
        for (int i = 0; i < size; i++ ) {
            E element = (E)persister.getElementType().assemble(arr[i], this.getSession(), owner);
            if ( element != null ) {
                baseSet.add(element);
            }
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
        Set<E> baseSet = this.getBase();
        Serializable[] result = new Serializable[baseSet.size()];
        Iterator<E> iter = baseSet.iterator();
        int i=0;
        while ( iter.hasNext() ) {
            result[i++] = persister.getElementType().disassemble( iter.next(), this.getSession(), null);
        }
        return result;
    }

    @Override
    public boolean isRowUpdatePossible() {
        return false;
    }

    @Override
    public boolean isWrapper(Object collection) {
        return this.getBase() == collection;
    }

    @Override
    public Iterator<E> queuedAdditionIterator() {
        XMap<E, QueuedOperationType> map = this.getQueuedOperations();
        final Iterator<Entry<E, QueuedOperationType>> entryIterator = map.entrySet().iterator(); 
        return new Iterator<E>() {

            @Override
            public boolean hasNext() {
                return entryIterator.hasNext();
            }

            @Override
            public E next() {
                Entry<E, QueuedOperationType> e = entryIterator.next();
                return e.getValue() == QueuedOperationType.ATTACH ? e.getKey() : null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<?> getDeletes(CollectionPersister persister, boolean indexIsFormula) 
            throws HibernateException {
        Set<E> baseSet = this.getBase();
        Type elementType = persister.getElementType();
        final Map<E, E> sn = (Map<E, E>)this.getSnapshot();
        List<E> deletes = new ArrayList<E>(this.unifiedComparator(), sn.size());
        Iterator<E> iter = sn.keySet().iterator();
        while (iter.hasNext()) {
            E test = iter.next();
            if (!baseSet.contains(test)) {
                // the element has been removed from the set
                deletes.add(test);
            }
        }
        iter = baseSet.iterator();
        while (iter.hasNext()) {
            E test = iter.next();
            E oldValue = sn.get(test);
            if (oldValue!=null && elementType.isDirty(test, oldValue, this.getSession())) {
                // the element has changed
                deletes.add(oldValue);
            }
        }
        return deletes.iterator();
    }

    @Override
    public Collection<E> getQueuedOrphans(String entityName) {
        XMap<E, QueuedOperationType> map = this.getQueuedOperations();
        if (!map.isEmpty()) {
            XCollection<E> additions = new ArrayList<E>(map.keyUnifiedComparator(), map.size());
            XCollection<E> removals = new ArrayList<E>(map.keyUnifiedComparator(), map.size());
            for (Entry<E, QueuedOperationType> entry : map.entrySet()) {
                if (entry.getValue() == QueuedOperationType.ATTACH) {
                    additions.add(entry.getKey());
                } else {
                    removals.add(entry.getKey());
                }
            }
            return getOrphans(removals, additions, entityName, this.getSession());
        }
        else {
            return MACollections.<E>emptySet();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<E> getOrphans(Serializable snapshot, String entityName)
            throws HibernateException {
        XMap<E, E> sn = (XMap<E, E>)snapshot;
        return getOrphans(sn.keySet(), this.getBase(), entityName, this.getSession());
    }

    @SuppressWarnings("unchecked")
    @Override
    public E getElement(Object entry) {
        return (E)entry;
    }

    @Override
    public final boolean entryExists(Object entry, int i) {
        return true;
    }

    @Deprecated
    @Override
    public final Object getIndex(Object entry, int i, CollectionPersister persister) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final Object getSnapshotElement(Object entry, int i) {
        throw new UnsupportedOperationException("Sets don't support updating by element");
    }
    
    /**
     * This method is used to replace 
     * "org.hibernate.collection.AbstractPersistentCollection#readElementExistence(Object element)"
     * @param element The example element to be read
     * @return The ref or readed element
     * <ul>
     *  <li>NonNull: Read successfully, check the value of ref to check the read value is null or not</li>
     *  <li>Null: Read failed</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    public Ref<E> visionallyRead(E element) {
        
        Arguments.mustNotBeNull("element", element);
        String role = this.getNonNullRole();
        
        SessionImplementor session = this.getSession();
        if (session == null || !session.isOpen() || !session.isConnected()) {
            return null;
        }
        
        SessionFactoryImplementor sessionFactory = session.getFactory();
        QueryableCollection collection = (QueryableCollection)sessionFactory.getCollectionPersister(role);
        EntityPersister elementPersister = collection.getElementPersister();
        Object elementId = elementPersister.getIdentifier(element, this.getSession());
        if (elementId == null) {
            return new Ref<>();
        }
        
        if (Boolean.TRUE.equals(
                elementPersister
                .getEntityMetamodel()
                .getIdentifierProperty()
                .getUnsavedValue()
                .isUnsaved((Serializable)elementId))) {
            return new Ref<>();
        }
        
        CriteriaImpl criteria = new CriteriaImpl(elementPersister.getEntityName(), session);
        
        /*
         * Add the condition of element.
         */
        criteria.add(Restrictions.idEq(elementId));
        
        //ownerKey, not ownerId
        Object ownerKey = collection.getCollectionType().getKeyOfOwner(this.getOwner(), session);
        //In Hibernate, isOneToMany means that there is no middle table
        //The @OneToMany of JPA with middle table is consider as many-to-many in Hibernate
        if (sessionFactory.getCollectionPersister(role).isOneToMany()) {
            String[] joinOwnerColumns = collection.getKeyColumnNames();
            StringBuilder sqlBuilder = new StringBuilder();
            for (int i = 0; i < joinOwnerColumns.length; i++) {
                if (i != 0) {
                    sqlBuilder.append(" and ");
                }
                sqlBuilder
                .append("{alias}.")
                .append(joinOwnerColumns[i])
                .append(" = ?");
            }
            criteria.add(
                    Restrictions.sqlRestriction(
                            sqlBuilder.toString(),
                            ownerKey,
                            collection.getKeyType()));
        } else {
            String lhsPropertyName = collection.getCollectionType().getLHSPropertyName();
            int lhsPropertyIndex = -1;
            if (lhsPropertyName != null)  {
                String[] propertyNames = collection.getOwnerEntityPersister().getPropertyNames();
                for (int i = propertyNames.length - 1; i >= 0; i--) {
                    if (propertyNames[i].equals(lhsPropertyName)) {
                        lhsPropertyIndex = i;
                        break;
                    }
                }
            }
            String[] lhsColumnNames = JoinHelper.getLHSColumnNames(
                    collection.getCollectionType(), 
                    lhsPropertyIndex,
                    (OuterJoinLoadable)elementPersister, 
                    sessionFactory);
            String[] joinElementColumnNames = collection.getElementColumnNames();
            String[] joinOwnerColumnNames = collection.getKeyColumnNames();
            StringBuilder subQueryBuilder = new StringBuilder();
            subQueryBuilder
            .append("exists(select * from ")
            .append(collection.getTableName())
            .append(" as ")
            .append(MIDDLE_TABLE_ALIAS)
            .append(" where ");
            for (int i = 0; i < joinElementColumnNames.length; i++) {
                if (i != 0) {
                    subQueryBuilder.append(" and ");
                }
                subQueryBuilder
                .append("{alias}.")
                .append(lhsColumnNames[i])
                .append(" = ")
                .append(MIDDLE_TABLE_ALIAS)
                .append('.')
                .append(joinElementColumnNames[i]);
            }
            for (int i = 0; i < joinOwnerColumnNames.length; i++) {
                subQueryBuilder
                .append(" and ")
                .append(MIDDLE_TABLE_ALIAS)
                .append(".")
                .append(joinOwnerColumnNames[i])
                .append(" = ?");
            }
            subQueryBuilder.append(')');
            criteria.add(
                    Restrictions.sqlRestriction(
                            subQueryBuilder.toString(),
                            ownerKey,
                            collection.getKeyType()));
        }
        FlushMode oldFlushMode = session.getFlushMode();
        session.setFlushMode(FlushMode.MANUAL);
        try {
            return new Ref<>((E)criteria.uniqueResult());
        } finally {
            session.setFlushMode(oldFlushMode);
        }
    }
}
