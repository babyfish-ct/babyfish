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
package org.babyfish.hibernate.persister;

import java.io.Serializable;

import org.babyfish.hibernate.persister.UncompletedInitializedProperties.EntityPersisterBridge;
import org.hibernate.HibernateException;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Tao Chen
 */
public class UnionSubclassEntityPersister extends org.hibernate.persister.entity.UnionSubclassEntityPersister {

    public UnionSubclassEntityPersister(
            EntityBinding entityBinding,
            EntityRegionAccessStrategy cacheAccessStrategy,
            NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy,
            SessionFactoryImplementor factory, 
            Mapping mapping) throws HibernateException {
        super(entityBinding, 
                cacheAccessStrategy, 
                naturalIdRegionAccessStrategy,
                factory, 
                mapping);
    }

    public UnionSubclassEntityPersister(
            PersistentClass persistentClass,
            EntityRegionAccessStrategy cacheAccessStrategy,
            NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy,
            SessionFactoryImplementor factory, 
            Mapping mapping) throws HibernateException {
        super(persistentClass, 
                cacheAccessStrategy, 
                naturalIdRegionAccessStrategy,
                factory, 
                mapping);
    }
    
    @Override
    public int[] findDirty(
            Object[] currentState, 
            Object[] previousState, 
            Object entity, 
            SessionImplementor session)
            throws HibernateException {
        int[] dirty = super.findDirty(currentState, previousState, entity, session);
        return UncompletedInitializedProperties.mergeDirty(dirty, entity, currentState, previousState);
    }

    @Override
    public Serializable insert(Object[] fields, Object object, SessionImplementor session) throws HibernateException {
        Serializable id = super.insert(fields, object, session);
        EntityPersisterBridge entityPersisterBridge = this.new BridgeImpl();
        UncompletedInitializedProperties.update(entityPersisterBridge, fields, object, id, null, session);
        return id;
    }

    @Override
    public void update(
            Serializable id, 
            Object[] fields, 
            int[] dirtyFields,
            boolean hasDirtyCollection, 
            Object[] oldFields, 
            Object oldVersion,
            Object object, 
            Object rowId, 
            SessionImplementor session)
            throws HibernateException {
        super.update(
                id, 
                fields, 
                dirtyFields, 
                hasDirtyCollection, 
                oldFields,
                oldVersion, 
                object, 
                rowId, 
                session);
        EntityPersisterBridge entityPersisterBridge = this.new BridgeImpl();
        UncompletedInitializedProperties.update(entityPersisterBridge, fields, object, null, rowId, session);
    }
    
    private class BridgeImpl implements EntityPersisterBridge {

        @Override
        public EntityPersister getEntityPersister() {
            return UnionSubclassEntityPersister.this;
        }

        @Override
        public int getTableSpan() {
            return UnionSubclassEntityPersister.this.getTableSpan();
        }

        @Override
        public String getTableName(int tableIndex) {
            return UnionSubclassEntityPersister.this.getTableName(tableIndex);
        }

        @Override
        public boolean isPropertyOfTable(int propertyIndex, int tableIndex) {
            return UnionSubclassEntityPersister.this.isPropertyOfTable(propertyIndex, tableIndex);
        }

        @Override
        public boolean[] getTableUpdateNeeded(int[] updatePropertyIndexes) {
            return UnionSubclassEntityPersister.this.getTableUpdateNeeded(updatePropertyIndexes, false);
        }

        @Override
        public String getRowIdName() {
            return UnionSubclassEntityPersister.this.rowIdName;
        }

        @Override
        public String[] getKeyColumns(int tableIndex) {
            return UnionSubclassEntityPersister.this.getKeyColumns(tableIndex);
        }

        @Override
        public String[] getPropertyColumnNames(int propertyIndex) {
            return UnionSubclassEntityPersister.this.getPropertyColumnNames(propertyIndex);
        }

        @Override
        public boolean[][] getPropertyColumnUpdateable() {
            return UnionSubclassEntityPersister.this.getPropertyColumnUpdateable();
        }
    }
}
