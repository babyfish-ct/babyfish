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
import java.util.Map;

import org.babyfish.lang.Arguments;
import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.metadata.ModelProperty;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelProvider;
import org.babyfish.model.spi.reference.IndexedReference;
import org.babyfish.model.spi.reference.KeyedReference;
import org.babyfish.model.spi.reference.MAReferenceImpl;
import org.babyfish.model.spi.reference.Reference;
import org.babyfish.model.spi.reference.event.ValueEvent;

/**
 * @author Tao Chen
 */
public class AssociatedReference<T> 
extends MAReferenceImpl<T> 
implements AssociatedEndpoint {
    
    private static final long serialVersionUID = -9058759202938343593L;
    
    private Object owner;
    
    private boolean disabled;
    
    private ModelProperty associationProperty;
    
    private transient int oppositePropertyId;
    
    private transient boolean suspended;
    
    public AssociatedReference(ObjectModel objectModel, int propertyId) {
        Arguments.mustNotBeNull("objectModel", objectModel);
        this.owner = objectModel.getOwner();
        ModelProperty property = objectModel.getModelClass().getProperty(propertyId);
        AssociationType associationType = property.getAssociationType();
        if (property.getAssociationType() != AssociationType.REFERENCE) {
            if (associationType == AssociationType.INDEXED_REFERENCE) {
                throw new IllegalArgumentException(
                        CommonMessages.createFailedBecausePropertyMustNotBe(
                                this.getClass(), 
                                property,
                                IndexedReference.class
                        )
                );
            }
            if (associationType == AssociationType.KEYED_REFERENCE) {
                throw new IllegalArgumentException(
                        CommonMessages.createFailedBecausePropertyMustNotBe(
                                this.getClass(), 
                                property,
                                KeyedReference.class
                        )
                );
            }
            throw new IllegalArgumentException(
                    CommonMessages.createFailedBecausePropertyMustBe(
                            this.getClass(), 
                            property,
                            Reference.class
                    )
            );
        }
        this.associationProperty = property;
        if (property.getOppositeProperty() != null) {
            this.oppositePropertyId = property.getOppositeProperty().getId();
        } else {
            this.oppositePropertyId = -1;
        }
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
        if (this.oppositePropertyId == -1) {
            return null;
        }
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
    public T get(boolean absolute) {
        this.requiredEnabled();
        return super.get(absolute);
    }

    @Override
    public T set(T value) {
        if (value != null && !(this instanceof KeyedReference<?, ?>)) {
            AssociatedEndpoint oppositeEndpoint = this.getOppositeEndpoint(value);
            if (oppositeEndpoint != null &&
                    !oppositeEndpoint.isSuspended() && 
                    Map.class.isAssignableFrom(oppositeEndpoint.getAssociationProperty().getType())) {
                throw new UnsupportedOperationException(
                        CommonMessages.canNotAttachElementToSpecialAssociation(
                                this.associationProperty,
                                this.associationProperty.getType(),
                                oppositeEndpoint.getAssociationProperty(),
                                oppositeEndpoint.getAssociationProperty().getType()
                        )
                );
            }
        }
        this.enable();
        return super.set(value);
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
    
    protected boolean isLoadedValue(T value) {
        return true;
    }
    
    protected boolean isAbandonableValue(T value) {
        return false;
    }
    
    protected void loadValue(T value) {
        
    }

    @Override
    protected void onModifying(ValueEvent<T> e) throws Throwable {
        if (this.oppositePropertyId != -1) {
            this.handler().preHandle(e);
        }
    }

    private void requiredEnabled() {
        if (this.disabled) {
            throw new IllegalStateException(
                    CommonMessages.currentReferenceIsDisabled()
            );
        }
    }
    
    private ValueEventHandler<T> handler() {
        return new ValueEventHandler<T>() {

            @Override
            protected AssociatedEndpoint getEndpoint() {
                return AssociatedReference.this;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedReference.this.suspended = suspended;
            }

            @Override
            protected boolean isLoadedObject(T opposite) {
                return AssociatedReference.this.isLoadedValue(opposite);
            }

            @Override
            protected boolean isAbandonableObject(T opposite) {
                return AssociatedReference.this.isAbandonableValue(opposite);
            }

            @Override
            protected void loadObject(T opposite) {
                AssociatedReference.this.loadValue(opposite);
            }
        };
    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.oppositePropertyId = this.associationProperty.getOppositeProperty().getId();
    }
}
