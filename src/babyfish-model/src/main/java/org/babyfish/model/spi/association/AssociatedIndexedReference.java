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
package org.babyfish.model.spi.association;

import java.io.IOException;

import org.babyfish.lang.Arguments;
import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.metadata.ModelProperty;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelProvider;
import org.babyfish.model.spi.reference.IndexedReference;
import org.babyfish.model.spi.reference.MAIndexedReferenceImpl;
import org.babyfish.model.spi.reference.event.IndexedValueEvent;

/**
 * @author Tao Chen
 */
public class AssociatedIndexedReference<T>
extends MAIndexedReferenceImpl<T>
implements AssociatedEndpoint {
    
    private static final long serialVersionUID = -4695991056750918549L;
    
    private Object owner;
    
    private boolean disabled;
    
    private ModelProperty associationProperty;
    
    private transient int oppositePropertyId;
    
    private transient boolean suspended;
    
    public AssociatedIndexedReference(ObjectModel objectModel, int propertyId) {
        Arguments.mustNotBeNull("objectModel", objectModel);
        this.owner = objectModel.getOwner();
        ModelProperty property = objectModel.getModelClass().getProperty(propertyId);
        if (property.getAssociationType() != AssociationType.INDEXED_REFERENCE) {
            throw new IllegalArgumentException(
                    CommonMessages.createFailedBecausePropertyMustBe(
                            this.getClass(), 
                            property,
                            IndexedReference.class
                    )
            );
        }
        if (property.getOppositeProperty() == null) {
            throw new IllegalArgumentException(
                    CommonMessages.createFailedBecauseOfunidirectionalAssociation(
                            this.getClass(), 
                            IndexedReference.class,
                            property
                    )
            );
        }
        this.associationProperty = property;
        this.oppositePropertyId = property.getOppositeProperty().getId();
    }
    
    @Override
    public final Object getOwner() {
        return this.owner;
    }
    
    @Override
    public ModelProperty getAssociationProperty() {
        return this.associationProperty;
    }

    @Override
    public final boolean isSuspended() {
        return this.suspended;
    }
    
    @Override
    public AssociatedEndpoint getOppositeEndpoint(Object opposite) {
        return ((ObjectModelProvider)opposite).objectModel().getAssociatedEndpoint(this.oppositePropertyId);
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public boolean isLoadable() {
        return true;
    }

    @Override
    public void load() {
        
    }

    @Override
    public final boolean isDisabled() {
        return this.disabled;
    }

    @Override
    public final void disable() {
        this.disabled = true;
    }

    @Override
    public final void enable() {
        this.disabled = false;
    }
    
    @Override
    public int getIndex() {
        this.requiredEnabled();
        return super.getIndex();
    }

    @Override
    public T get(boolean absolute) {
        this.requiredEnabled();
        return super.get(absolute);
    }

    @Override
    public int setIndex(int index) {
        this.enable();
        return super.setIndex(index);
    }

    @Override
    public T set(T value) {
        this.enable();
        return super.set(value);
    }

    @Override
    public T set(int index, T value) {
        this.enable();
        return super.set(index, value);
    }

    @Override
    protected void onModifying(IndexedValueEvent<T> e) throws Throwable {
        this.handler().preHandle(e);
    }
    
    private IndexedValueEventHandler<T> handler() {
        return new IndexedValueEventHandler<T>() {

            @Override
            protected AssociatedEndpoint getEndpoint() {
                return AssociatedIndexedReference.this;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedIndexedReference.this.suspended = suspended;
            }
        };
    }
    
    private void requiredEnabled() {
        if (this.disabled) {
            throw new IllegalStateException(
                    CommonMessages.currentReferenceIsDisabled()
            );
        }
    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.oppositePropertyId = this.associationProperty.getOppositeProperty().getId();
    }
}
