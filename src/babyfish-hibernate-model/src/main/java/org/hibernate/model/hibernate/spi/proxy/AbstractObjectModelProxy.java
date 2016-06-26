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
package org.hibernate.model.hibernate.spi.proxy;

import java.io.Serializable;

import org.babyfish.collection.FrozenContext;
import org.babyfish.lang.Nulls;
import org.babyfish.model.event.ScalarListener;
import org.babyfish.model.jpa.metadata.JPAModelClass;
import org.babyfish.model.jpa.metadata.JPAModelProperty;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelProvider;
import org.babyfish.model.spi.ScalarLoader;
import org.babyfish.model.spi.association.AssociatedEndpoint;
import org.hibernate.proxy.HibernateProxy;

public abstract class AbstractObjectModelProxy implements ObjectModel, Serializable {
    
    private static final long serialVersionUID = -3166464044156270222L;

    private JPAModelClass modelClass;
    
    protected HibernateProxy owner;
    
    protected AbstractObjectModelProxy(ModelClass modelClass, HibernateProxy owner) {
        this.modelClass = (JPAModelClass)modelClass;
        this.owner = owner;
    }

    @Override
    public ModelClass getModelClass() {
        return this.modelClass;
    }

    @Override
    public Object getOwner() {
        return this.owner;
    }
    
    @Override
    public void addScalarListener(ScalarListener listener) {
        FrozenLazyInitializer.get(this.owner).addScalarListener(listener);
    }

    @Override
    public void removeScalarListener(ScalarListener listener) {
        FrozenLazyInitializer.get(this.owner).removeScalarListener(listener);
    }

    @Override
    public void freezeScalar(int scalarPropertyId, FrozenContext<?> ctx) {
        if (scalarPropertyId == this.modelClass.getIdProperty().getId()) {
            FrozenLazyInitializer.get(this.owner).freezeIdentifier(ctx);
        } else {
            this.getTargetObjectModel().freezeScalar(scalarPropertyId, ctx);
        }
    }

    @Override
    public void unfreezeScalar(int scalarPropertyId, FrozenContext<?> ctx) {
        if (scalarPropertyId == this.modelClass.getIdProperty().getId()) {
            FrozenLazyInitializer.get(this.owner).unfreezeIdentifier(ctx);
        } else {
            this.getTargetObjectModel().unfreezeScalar(scalarPropertyId, ctx);
        }
    }

    @Override
    public Object get(int propertyId) {
        if (propertyId == this.modelClass.getIdProperty().getId()) {
            return FrozenLazyInitializer.get(this.owner).getIdentifier();
        }
        return this.getTargetObjectModel().get(propertyId);
    }

    @Override
    public void set(int propertyId, Object value) {
        if (propertyId == this.modelClass.getIdProperty().getId()) {
            FrozenLazyInitializer.get(this.owner).setIdentifier((Serializable)value);
        } else {
            this.getTargetObjectModel().set(propertyId, value);
        }
    }

    @Override
    public AssociatedEndpoint getAssociatedEndpoint(int propertyId) {
        return this.getTargetObjectModel().getAssociatedEndpoint(propertyId);
    }

    @Override
    public boolean isLoaded(int propertyId) {
        if (propertyId == this.modelClass.getIdProperty().getId()) {
            return true;
        }
        return this.getTargetObjectModel().isLoaded(propertyId);
    }

    @Override
    public void load(int propertyId) {
        if (propertyId != this.modelClass.getIdProperty().getId()) {
            this.getTargetObjectModel().load(propertyId);
        }
    }

    @Override
    public void load(int... propertyIds) {
        propertyIds = this.excludeIdProperty(propertyIds);
        if (!Nulls.isNullOrEmpty(propertyIds)) {
            this.getTargetObjectModel().load(propertyIds);
        }
    }

    @Override
    public void batchLoad(Iterable<ObjectModel> objectModels, int[] propertyIds) {
        propertyIds = this.excludeIdProperty(propertyIds);
        if (!Nulls.isNullOrEmpty(propertyIds)) {
            this.getTargetObjectModel().batchLoad(objectModels, propertyIds);
        }
    }

    @Override
    public void unload(int propertyId) {
        if (propertyId != this.modelClass.getIdProperty().getId()) {
            this.getTargetObjectModel().unload(propertyId);
        }
    }

    @Override
    public boolean isDisabled(int propertyId) {
        JPAModelProperty modelProperty = (JPAModelProperty)this.modelClass.getProperty(propertyId);
        switch (modelProperty.getScalarType()) {
        case ID:
        case VERSION:
            return false;
        default:
            return this.getTargetObjectModel().isDisabled(propertyId);
        }
    }

    @Override
    public void enable(int propertyId) {
        JPAModelProperty modelProperty = (JPAModelProperty)this.modelClass.getProperty(propertyId);
        switch (modelProperty.getScalarType()) {
        case ID:
        case VERSION:
            return;
        default:
            this.getTargetObjectModel().enable(propertyId);
        }
    }

    @Override
    public void disable(int propertyId) {
        JPAModelProperty modelProperty = (JPAModelProperty)this.modelClass.getProperty(propertyId);
        switch (modelProperty.getScalarType()) {
        case ID:
        case VERSION:
            return;
        default:
            this.getTargetObjectModel().disable(propertyId);
        }
    }

    @Override
    public ScalarLoader getScalarLoader() {
        return this.getTargetObjectModel().getScalarLoader();
    }

    @Override
    public void setScalarLoader(ScalarLoader scalarLoader) {
        this.getTargetObjectModel().setScalarLoader(scalarLoader);
    }
    
    protected final ObjectModel getTargetObjectModel() {
        return ((ObjectModelProvider)FrozenLazyInitializer.get(this.owner).getImplementation()).objectModel();
    }

    private int[] excludeIdProperty(int[] propertyIds) {
        if (Nulls.isNullOrEmpty(propertyIds)) {
            return propertyIds;
        }
        int idPropertyId = this.modelClass.getIdProperty().getId();
        for (int i = propertyIds.length - 1; i >= 0; i--) {
            if (propertyIds[i] == idPropertyId) {
                int[] arr = new int[propertyIds.length - 1];
                System.arraycopy(propertyIds, i + 1, arr, i, propertyIds.length - i - 1);
                System.arraycopy(propertyIds, 0, arr, 0, i);
                return arr;
            }
        }
        return propertyIds;
    }
}
