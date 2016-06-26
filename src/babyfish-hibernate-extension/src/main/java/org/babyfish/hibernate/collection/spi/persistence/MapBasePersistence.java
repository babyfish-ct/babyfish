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

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XMap;
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
public abstract class MapBasePersistence<K, V> extends AbstractBasePersistence<V> {

    private static final long serialVersionUID = -5020841795509944819L;
    
    private static final String MIDDLE_TABLE_ALIAS = "babyfish_mt_alias_";
    
    protected abstract MAMap<K, V> getBase();
    
    protected abstract void initBase(MAMap<K, V> base);
    
    protected abstract XMap<K, V> getQueuedOrphans();
    
    protected abstract void preformQueuedOrphans();

    @SuppressWarnings("unchecked")
    @Override
    public V getElement(Object entry) {
        return ((Entry<K, V>)entry).getValue();
    }

    @Override
    public Iterator<Entry<K, V>> entries(CollectionPersister persister) {
        return MACollections.unmodifiable(this.getBase().entrySet()).iterator();
        //Not MACollections.unmodifiable(this.getBase().entrySet().iterator());
        //Entry is readonly to so that Entry.setValue can not work normally.
    }

    @SuppressWarnings("unchecked")
    @Override
    public V readFrom(
            ResultSet rs, 
            CollectionPersister persister,
            CollectionAliases descriptor, 
            Object owner) throws HibernateException, SQLException {
        V value = (V)persister.readElement( rs, owner, descriptor.getSuffixedElementAliases(), getSession() );
        K key = (K)persister.readIndex( rs, descriptor.getSuffixedIndexAliases(), getSession() );
        if ( value != null ) {
            this.getBase().put(key, value);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> getOrphans(Serializable snapshot, String entityName)
            throws HibernateException {
        XMap<K, V> sn = (XMap<K, V>)snapshot;
        return getOrphans(sn.values(), this.getBase().values(), entityName, getSession());
    }

    @Override
    public Iterator<V> queuedAdditionIterator() {
        return MACollections.<V>emptySet().iterator();
    }

    @Override
    public Collection<V> getQueuedOrphans(String entityName) {
        XMap<K, V> map = this.getQueuedOrphans();
        if (!map.isEmpty()) {
            XCollection<V> removals = new ArrayList<V>(map.valueUnifiedComparator(), map.size());
            for (V value : map.values()) {
                removals.add(value);
            }
            return getOrphans(removals, MACollections.<V>emptySet(), entityName, this.getSession());
        }
        else {
            return MACollections.<V>emptySet();
        }
    }

    @Override
    public boolean empty() {
        return this.getBase().isEmpty();
    }

    @Override
    public void beginRead() {
        this.setInitializing(true);
    }

    @Override
    public boolean endRead() {
        return this.afterInitialize();
    }

    @Override
    public boolean afterInitialize() {
        setInitialized();
        //do this bit after setting initialized to true or it will recurse
        if (this.hasQueuedOperations()) {
            this.preformQueuedOrphans();
            //cacheSize = -1;
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initializeFromCache(
            CollectionPersister persister, 
            Serializable disassembled, 
            Object owner) throws HibernateException {
        Serializable[] array = (Serializable[])disassembled;
        int size = array.length;
        beforeInitialize(persister, size);
        MAMap<K, V> baseMap = this.getBase();
        for ( int i = 0; i < size; i+=2 ) {
            baseMap.put(
                    (K)persister.getIndexType().assemble(array[i], getSession(), owner),
                    (V)persister.getElementType().assemble(array[i+1], getSession(), owner));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public K getIndex(Object entry, int i, CollectionPersister persister) {
        return ((Entry<K, V>)entry).getKey();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getSnapshotElement(Object entry, int i) {
        XMap<K, V> sn = (XMap<K, V>)getSnapshot();
        return sn.get(((Entry<K, V>)entry).getKey());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void beforeInitialize(CollectionPersister persister, int anticipatedSize) {
        Object instantiate = persister.getCollectionType().instantiate(anticipatedSize);
        if (!(instantiate instanceof MAMap<?, ?>)) {
            throw new IllegalProgramException(
                    CommonMessages.illegalInstantiate(
                            persister.getCollectionType().getClass(), 
                            MAMap.class
                    )
            );
        }
        this.initBase((MAMap<K, V>)instantiate);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equalsSnapshot(CollectionPersister persister) throws HibernateException {
        Type elementType = persister.getElementType();
        XMap<K, V> snapshotMap = (XMap<K, V>)this.getSnapshot();
        MAMap<K, V> baseMap = this.getBase();
        if (snapshotMap.size() != baseMap.size()) {
            return false;
        }
        Iterator<Entry<K, V>> iter = baseMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<K, V> entry = iter.next();
            if (elementType.isDirty(entry.getValue(), snapshotMap.get(entry.getKey()), this.getSession())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSnapshotEmpty(Serializable snapshot) {
        return ((XMap<?, ?>)snapshot).isEmpty();
    }

    @Override
    public Serializable disassemble(CollectionPersister persister) throws HibernateException {
        MAMap<K, V> baseMap = this.getBase();
        Serializable[] result = new Serializable[baseMap.size() << 1];
        Iterator<Entry<K, V>> iter = baseMap.entrySet().iterator();
        int i=0;
        while (iter.hasNext()) {
            Entry<K, V> e = iter.next();
            result[i++] = persister.getIndexType().disassemble(e.getKey(), getSession(), null);
            result[i++] = persister.getElementType().disassemble(e.getValue(), getSession(), null);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Serializable getSnapshot(CollectionPersister persister) throws HibernateException {
        MAMap<K, V> baseMap = this.getBase();
        Map<K, V> clonedMap;
        UnifiedComparator<? super K> keyUnifiedComparator = baseMap.keyUnifiedComparator();
        if (keyUnifiedComparator.comparator() != null) {
            clonedMap = new TreeMap<K, V>(
                    keyUnifiedComparator.comparator(),
                    baseMap.valueUnifiedComparator());
        } else {
            clonedMap = new HashMap<K, V>(
                    keyUnifiedComparator.equalityComparator(),
                    baseMap.valueUnifiedComparator(),
                    baseMap.size() + 1, 
                    1.F);
        }
        Iterator<Entry<K, V>> iter = baseMap.entrySet().iterator();
        while ( iter.hasNext() ) {
            Entry<K, V> e = (Entry<K, V>)iter.next();
            final V copy = (V)persister.getElementType().deepCopy(
                    e.getValue(), persister.getFactory());
            clonedMap.put(e.getKey(), copy);
        }
        return (Serializable)clonedMap;
    }

    @Override
    public boolean entryExists(Object entry, int i) {
        return ((Entry<?, ?>)entry).getValue() != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean needsInserting(Object entry, int i, Type elemType) throws HibernateException {
        final XMap<?, ?> snapshot = (XMap<K, V>)this.getSnapshot();
        Entry<?, ?> e = (Entry<?, ?>) entry;
        return e.getValue() != null && snapshot.get(e.getKey()) == null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean needsUpdating(Object entry, int i, Type elemType) throws HibernateException {
        final XMap<K, V> sn = (XMap<K, V>)this.getSnapshot();
        Entry<K, V> e = (Entry<K, V>)entry;
        V snValue = sn.get(e.getKey());
        return 
                e.getValue() != null && 
                snValue!=null && 
                elemType.isDirty(snValue, e.getValue(), this.getSession());
    }

    @Override
    public boolean isRowUpdatePossible() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<?> getDeletes(CollectionPersister persister,
            boolean indexIsFormula) throws HibernateException {
        MAMap<K, V> baseMap = this.getBase();
        List<Object> deletes = new ArrayList<Object>();
        Iterator<Entry<K, V>> iter = ((XMap<K, V>)this.getSnapshot()).entrySet().iterator();
        while (iter.hasNext()) {
            Entry<K, V> e = (Entry<K, V>)iter.next();
            K key = e.getKey();
            if (e.getValue() != null && baseMap.get(key) == null) {
                deletes.add(indexIsFormula ? e.getValue() : key);
            }
        }
        return deletes.iterator();
    }

    @Override
    public boolean isWrapper(Object collection) {
        return this.getBase() == collection;
    }

    @SuppressWarnings("unchecked")
    public Ref<V> visionallyRead(K key) {
        
        Arguments.mustNotBeNull("key", key);
        String role = this.getNonNullRole();
        
        SessionImplementor session = this.getSession();
        if (session == null || !session.isOpen() || !session.isConnected()) {
            return null;
        }
        
        SessionFactoryImplementor sessionFactory = session.getFactory();
        QueryableCollection collection = (QueryableCollection)sessionFactory.getCollectionPersister(role);
        EntityPersister elementPersister = collection.getElementPersister();
        
        String[] indexNames = collection.getIndexColumnNames();
        if (indexNames == null || indexNames[0] == null) {
            indexNames = collection.getIndexFormulas();
        }
        CriteriaImpl criteria = new CriteriaImpl(elementPersister.getEntityName(), session);
        
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
            
            sqlBuilder = new StringBuilder();
            for (int i = 0; i < indexNames.length; i++) {
                if (i != 0) {
                    sqlBuilder.append(" and ");
                }
                sqlBuilder
                .append("{alias}.")
                .append(indexNames[i])
                .append(" = ?");
            }
            criteria.add(
                    Restrictions.sqlRestriction(
                            sqlBuilder.toString(),
                            key,
                            collection.getIndexType()));
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
                .append(".")
                .append(joinElementColumnNames[i]);
            }
            for (int i = 0; i < joinOwnerColumnNames.length; i++) {
                subQueryBuilder
                .append(" and ")
                .append(MIDDLE_TABLE_ALIAS)
                .append('.')
                .append(joinOwnerColumnNames[i])
                .append(" = ?");
            }
            for (int i = 0; i < indexNames.length; i++) {
                subQueryBuilder
                .append(" and ")
                .append(MIDDLE_TABLE_ALIAS)
                .append('.')
                .append(indexNames[i])
                .append(" = ?");
            }
            subQueryBuilder.append(')');
            criteria.add(
                    Restrictions.sqlRestriction(
                            subQueryBuilder.toString(),
                            new Object[] { ownerKey, key },
                            new Type[] { collection.getKeyType(), collection.getIndexType() }));
        }
        FlushMode oldFlushMode = session.getFlushMode();
        session.setFlushMode(FlushMode.MANUAL);
        try {
            return new Ref<V>((V)criteria.uniqueResult());
        } finally {
            session.setFlushMode(oldFlushMode);
        }
    }
}
