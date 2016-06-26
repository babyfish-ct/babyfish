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
package org.hibernate.model.hibernate.spi.scalar;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;
import org.babyfish.model.jpa.metadata.JPAModelClass;
import org.babyfish.model.metadata.ModelProperty;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.model.spi.DirtinessAwareScalarLoader;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ScalarLoader;
import org.hibernate.FlushMode;
import org.hibernate.LazyInitializationException;
import org.hibernate.bytecode.instrumentation.spi.AbstractFieldInterceptor;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.bytecode.internal.javassist.FieldHandler;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.model.hibernate.spi.dialect.LimitedListDialect;
import org.hibernate.transform.ResultTransformer;

/**
 * @author Tao Chen
 */
public class HibernateScalarLoader 
implements 
    ScalarLoader, 
    DirtinessAwareScalarLoader, 
    FieldInterceptor, 
    FieldHandler, 
    Serializable {
    
    private static final long serialVersionUID = 515855421447825375L;
    
    private static final int STATE_INITIALIZIED = 1;
    
    private static final int STATE_UNINITIALIZIED = 2;

    private ObjectModel defaultObjectModel;
    
    private transient SessionImplementor session;
    
    private boolean dirty;

    @SuppressWarnings("unchecked")
    public HibernateScalarLoader(ObjectModel objectModel, FieldHandler handler) {
        Arguments.mustNotBeNull("objectModel", objectModel);
        Arguments.mustBeInstanceOfValue("handler", handler, AbstractFieldInterceptor.class);
        this.defaultObjectModel = objectModel;
        AbstractFieldInterceptor abstractFieldInterceptor = (AbstractFieldInterceptor)handler;
        this.session = abstractFieldInterceptor.getSession();
        Set<String> uninitializedFields = abstractFieldInterceptor.getUninitializedFields();
        if (!Nulls.isNullOrEmpty(uninitializedFields)) {
            Map<String, ModelProperty> properties = this.defaultObjectModel.getModelClass().getProperties();
            for (String uninitializedField : uninitializedFields) {
                this.defaultObjectModel.unload(properties.get(uninitializedField).getId());
            }
        }
    }
    
    public ObjectModel getObjectModel() {
        return this.defaultObjectModel;
    }

    @Override
    public void setSession(SessionImplementor session) {
        this.session = session;
    }

    @Override
    public boolean isInitialized() {
        int state = getInitializationState();
        return (state & STATE_INITIALIZIED) != 0 && (state & STATE_UNINITIALIZIED) == 0;
    }
    
    public boolean isIncompletelyInitialized() {
        int state = getInitializationState();
        return (state & STATE_INITIALIZIED) != 0 && (state & STATE_UNINITIALIZIED) != 0;
    }

    @Override
    public boolean isInitialized(String field) {
        ModelProperty modelProperty = this.defaultObjectModel.getModelClass().getProperties().get(field);
        return this.defaultObjectModel.isLoaded(modelProperty.getId());
    }

    @Override
    public void dirty() {
        this.dirty = true;
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void clearDirty() {
        this.dirty = false;
    }
    
    private int getInitializationState() {
        int initializationState = 0;
        ObjectModel objectModel = this.defaultObjectModel;
        for (ModelProperty modelProperty : this.defaultObjectModel.getModelClass().getPropertyList()) {
            if (modelProperty.getPropertyType() == PropertyType.SCALAR && modelProperty.isDeferrable()) {
                int propertyId = modelProperty.getId();
                if (objectModel.isDisabled(propertyId) || objectModel.isUnloaded(propertyId)) {
                    initializationState |= STATE_UNINITIALIZIED;
                } else {
                    initializationState |= STATE_INITIALIZIED;
                }
                if ((initializationState & (STATE_UNINITIALIZIED | STATE_INITIALIZIED)) == (STATE_UNINITIALIZIED | STATE_INITIALIZIED)) {
                    break;
                }
            }
        }
        return initializationState;   
    }

    @Override
    public void load(Collection<org.babyfish.model.spi.ObjectModel> objectModels, int[] propertyIds) {
        
        SessionImplementor session = this.session;
        if (session == null) {
            throw new LazyInitializationException("entity with lazy properties is not associated with a session");
        }
        else if (!session.isOpen() || !session.isConnected()) {
            throw new LazyInitializationException("session is not connected");
        }
        
        int partitionSize = -1;
        Dialect dialect = session.getFactory().getDialect();
        if (dialect instanceof LimitedListDialect) {
            int maxListLength = ((LimitedListDialect)dialect).getMaxListLength();
            if (objectModels.size() > maxListLength) {
                partitionSize = maxListLength;
            }
        }
        
        if (partitionSize == -1) {
            this.loadPartition(objectModels, propertyIds);
            return;
        }
        
        List<ObjectModel> objectModelList;
        if (objectModels instanceof List<?>) {
            objectModelList = (List<ObjectModel>)objectModels;
        } else {
            objectModelList = new ArrayList<>(objectModels);
        }
        while (!objectModelList.isEmpty()) {
            if (objectModelList.size() <= partitionSize) {
                this.loadPartition(objectModelList, propertyIds);
                break;
            }
            this.loadPartition(objectModelList.subList(0, partitionSize), propertyIds);
            objectModelList = objectModelList.subList(partitionSize, objectModelList.size());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadPartition(Collection<ObjectModel> objectModels, int[] propertyIds) {
        boolean batch = objectModels.size() > 1;
        ObjectModel firstObjectModel = objectModels.iterator().next();
        JPAModelClass modelClass = (JPAModelClass)firstObjectModel.getModelClass();
        ModelProperty idProperty = modelClass.getIdProperty();
        Map<Object, ObjectModel> idMap = new LinkedHashMap<>();
        for (ObjectModel objectModel : objectModels) {
            idMap.put(objectModel.get(idProperty.getId()), objectModel);
        }
        
        CriteriaImpl criteria = new CriteriaImpl(modelClass.getJavaType().getName(), session);
        ProjectionList projectionList = Projections.projectionList();
        if (batch) {
            projectionList.add(Projections.property(idProperty.getName()));
        }
        for (int propertyId : propertyIds) {
            String propertyName = modelClass.getProperty(propertyId).getName();
            projectionList.add(Projections.property(propertyName));
        }
        if (batch) {
            criteria
            .add(
                    Restrictions.in(
                            idProperty.getName(), 
                            idMap.keySet()
                    )
            );
        } else {
            criteria
            .add(
                    Restrictions.eq(
                            idProperty.getName(), 
                            idMap.keySet().iterator().next()
                    )
            );
        }
        criteria
        .setProjection(projectionList)
        .setResultTransformer(new ResultTransformer() {
            
            private static final long serialVersionUID = -1387181124646452221L;
            
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                return tuple;
            }
            @SuppressWarnings("rawtypes")
            @Override
            public List transformList(List collection) {
                return collection;
            }
        });
        List<Object[]> tuples;
        FlushMode oldFlushMode = session.getFlushMode();
        session.setFlushMode(FlushMode.MANUAL);
        try {
            tuples = (List<Object[]>)criteria.list();
        } finally {
            session.setFlushMode(oldFlushMode);
        }
        if (batch) {
            for (Object[] tuple : tuples) {
                ObjectModel objectModel = idMap.get(tuple[0]);
                for (int i = propertyIds.length - 1; i >= 0; i--) {
                    objectModel.set(propertyIds[i], tuple[i + 1]);
                }
            }
        } else {
            Object[] firstTuple = tuples.get(0);
            for (int i = propertyIds.length - 1; i >= 0; i--) {
                firstObjectModel.set(propertyIds[i], firstTuple[i]);
            }
        }
    }
    
    /*
     * All of these read/write interceptor methods are deprecated and final,
     * because 
     * (1) babyfish must keep some compatibilities with hibernate so that this class must 
     *      implement the interface "org.hibernate.bytecode.internal.javassist.FieldHandler"
     * (2) but, actually, babyfish never invokes them, all the functionalities have been
     *      implemented in the dynamically-generated-bytecode of ObjectModel. 
     */
    @Deprecated
    @Override
    public final int writeInt(Object obj, String name, int oldValue, int newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final char writeChar(Object obj, String name, char oldValue, char newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final byte writeByte(Object obj, String name, byte oldValue, byte newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final boolean writeBoolean(Object obj, String name, boolean oldValue, boolean newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final short writeShort(Object obj, String name, short oldValue, short newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final float writeFloat(Object obj, String name, float oldValue, float newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final double writeDouble(Object obj, String name, double oldValue, double newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final long writeLong(Object obj, String name, long oldValue, long newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final Object writeObject(Object obj, String name, Object oldValue, Object newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final int readInt(Object obj, String name, int oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final char readChar(Object obj, String name, char oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final byte readByte(Object obj, String name, byte oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final boolean readBoolean(Object obj, String name, boolean oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final short readShort(Object obj, String name, short oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final float readFloat(Object obj, String name, float oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final double readDouble(Object obj, String name, double oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final long readLong(Object obj, String name, long oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final Object readObject(Object obj, String name, Object oldValue) {
        throw new UnsupportedOperationException();
    }
}
