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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.NavigableSet;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.TreeSet;
import org.babyfish.lang.Nulls;
import org.babyfish.model.jpa.metadata.JPAModelClass;
import org.babyfish.model.metadata.ModelProperty;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.model.spi.ObjectModel;
import org.hibernate.HibernateException;
import org.hibernate.bytecode.instrumentation.internal.FieldInterceptionHelper;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.bytecode.instrumentation.spi.LazyPropertyInitializer;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.model.hibernate.spi.scalar.HibernateScalarLoader;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;

/**
 * @author Tao Chen
 */
class UncompletedInitializedProperties {

    private UncompletedInitializedProperties() {
        throw new UnsupportedOperationException();
    }
    
    static void update(
            EntityPersisterBridge entityPersisterBridge,
            Object[] state,
            Object entity, 
            Serializable id,
            Object rowId,
            SessionImplementor session) {
        FieldInterceptor fieldInterceptor = FieldInterceptionHelper.extractFieldInterceptor(entity);
        if (!(fieldInterceptor instanceof HibernateScalarLoader)) {
            return;
        }
        HibernateScalarLoader hibernateObjectModelScalarLoader = 
                (HibernateScalarLoader)fieldInterceptor;
        if (!hibernateObjectModelScalarLoader.isIncompletelyInitialized()) {
            return;
        }
        ObjectModel objectModel = hibernateObjectModelScalarLoader.getObjectModel();
        JPAModelClass modelClass = (JPAModelClass)objectModel.getModelClass();
        EntityPersister entityPersister = entityPersisterBridge.getEntityPersister();
        String[] names = entityPersister.getPropertyNames();
        Type[] types = entityPersister.getPropertyTypes();
        boolean[] updateabilities = entityPersister.getPropertyUpdateability();
        
        List<Integer> updatePropertyIndexList = new ArrayList<>();
        for (int propertyIndex = 0; propertyIndex < names.length; propertyIndex++) {
            if (updateabilities[propertyIndex]) {
                ModelProperty modelProperty = modelClass.getProperties().get(names[propertyIndex]);
                if (modelProperty.getPropertyType() == PropertyType.SCALAR) {
                    if (modelProperty.isDeferrable()) {
                        int propertyId = modelProperty.getId();
                        if (objectModel.isEnabed(propertyId) && objectModel.isLoaded(propertyId)) {
                            updatePropertyIndexList.add(propertyIndex);
                        }
                    }
                }
            }
        }
        if (updatePropertyIndexList.isEmpty()) {
            return;
        }
        int[] updatePropertyIndexes = new int[updatePropertyIndexList.size()];
        for (int i = updatePropertyIndexList.size() - 1; i >= 0; i--) {
            updatePropertyIndexes[i] = updatePropertyIndexList.get(i);
        }
        
        boolean[] tableUpdateNeeded = entityPersisterBridge.getTableUpdateNeeded(updatePropertyIndexes);
        boolean[][] propertyColumnUpdateable = entityPersisterBridge.getPropertyColumnUpdateable();
        for (int tableIndex = 0; tableIndex < entityPersisterBridge.getTableSpan(); tableIndex++) {
            if (tableUpdateNeeded[tableIndex]) {
                StringBuilder builder = new StringBuilder();
                builder
                .append("update ")
                .append(entityPersisterBridge.getTableName(tableIndex))
                .append(" set ");
                boolean addComma = false;
                for (int propertyIndex : updatePropertyIndexes) {
                    if (entityPersisterBridge.isPropertyOfTable(propertyIndex, tableIndex)) {
                        String[] columnNames = entityPersisterBridge.getPropertyColumnNames(propertyIndex);
                        for (int columnIndex = 0; columnIndex < columnNames.length; columnIndex++) {
                            if (propertyColumnUpdateable[propertyIndex][columnIndex]) {
                                if (addComma) {
                                    builder.append(", ");
                                } else {
                                    addComma = true;
                                }
                                builder.append(columnNames[columnIndex]).append(" = ?");
                            }
                        }
                    }
                }
                if (!addComma) {
                    continue;
                }
                builder.append(" where ");
                addComma = false;
                if (tableIndex == 0 && rowId != null) {
                    builder.append(entityPersisterBridge.getRowIdName()).append(" = ?");
                } else {
                    for (String keyColumn : entityPersisterBridge.getKeyColumns(tableIndex)) {
                        if (addComma) {
                            builder.append(", ");
                        } else {
                            addComma = true;
                        }
                        builder.append(keyColumn).append(" = ?");
                    }
                }
                String sql = builder.toString();
                JdbcCoordinator jdbcCoordinator = session.getTransactionCoordinator().getJdbcCoordinator();
                PreparedStatement preparedStatement = 
                        jdbcCoordinator
                        .getStatementPreparer()
                        .prepareStatement(sql);
                try {
                    int paramIndex = 1;
                    for (int propertyIndex : updatePropertyIndexes) {
                        if (entityPersisterBridge.isPropertyOfTable(propertyIndex, tableIndex)) {
                            types[propertyIndex].nullSafeSet(
                                    preparedStatement, 
                                    state[propertyIndex], 
                                    paramIndex, 
                                    propertyColumnUpdateable[propertyIndex], 
                                    session);
                            paramIndex += ArrayHelper.countTrue(propertyColumnUpdateable[propertyIndex]);
                        }
                    }
                    if (tableIndex == 0 && rowId != null) {
                        preparedStatement.setObject(paramIndex, rowId);
                    } else {
                        entityPersister.getIdentifierType().nullSafeSet(
                                preparedStatement, 
                                id != null ? id : objectModel.get(modelClass.getIdProperty().getId()), 
                                paramIndex, 
                                session
                        );
                    }
                    preparedStatement.executeUpdate();
                } catch (SQLException ex) {
                    throw new HibernateException(ex);
                } finally {
                    jdbcCoordinator.release(preparedStatement);
                }
            }
        }
    }
    
    static int[] mergeDirty(int[] dirty, Object entity, Object[] currentState, Object[] previousState) {
        FieldInterceptor fieldInterceptor = FieldInterceptionHelper.extractFieldInterceptor(entity);
        if (!(fieldInterceptor instanceof HibernateScalarLoader)) {
            return dirty;
        }
        HibernateScalarLoader hibernateObjectModelScalarLoader = 
                (HibernateScalarLoader)fieldInterceptor;
        if (!hibernateObjectModelScalarLoader.isDirty()) {
            if (dirty != null) {
                NavigableSet<Integer> uset = null;
                for (int dirtyIndex : dirty) {
                    if (previousState[dirtyIndex] == LazyPropertyInitializer.UNFETCHED_PROPERTY) {
                        if (uset == null) {
                            uset = new TreeSet<>();
                        }
                        uset.add(dirtyIndex);
                    }
                }
                if (uset != null) {
                    NavigableSet<Integer> result = new TreeSet<>();
                    for (int dirtyIndex : dirty) {
                        result.add(dirtyIndex);
                    }
                    result.removeAll(uset);
                    if (result.isEmpty()) {
                        return null;
                    }
                    return MACollections.toIntArray(result);
                }
            }
            return dirty;
        }
        NavigableSet<Integer> set = null;
        for (int i = currentState.length - 1; i >= 0; i--) {
            if (currentState[i] != LazyPropertyInitializer.UNFETCHED_PROPERTY &&
                    previousState[i] == LazyPropertyInitializer.UNFETCHED_PROPERTY) {
                if (set == null) {
                    set = new TreeSet<>();
                }
                set.add(i);
            }
        }
        if (Nulls.isNullOrEmpty(set)) {
            return dirty;
        }
        if (!Nulls.isNullOrEmpty(dirty)) {
            for (int i : dirty) {
                set.add(i);
            }
        }
        if (set.isEmpty()) {
            return null;
        }
        return MACollections.toIntArray(set);
    } 
    
    interface EntityPersisterBridge {
        
        EntityPersister getEntityPersister();
        
        int getTableSpan();
        
        String getTableName(int tableIndex);
        
        boolean isPropertyOfTable(int propertyIndex, int tableIndex);
        
        boolean[] getTableUpdateNeeded(int[] updatePropertyIndexes);
        
        String getRowIdName();
        
        String[] getKeyColumns(int tableIndex);
        
        String[] getPropertyColumnNames(int propertyIndex);
        
        boolean[][] getPropertyColumnUpdateable();
    }
}
