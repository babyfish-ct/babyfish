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
import org.babyfish.model.spi.reference.KeyedReference;
import org.babyfish.model.spi.reference.MAKeyedReferenceImpl;
import org.babyfish.model.spi.reference.event.KeyedValueEvent;

/**
 * @author Tao Chen
 */
public class AssociatedKeyedReference<K, T> 
extends MAKeyedReferenceImpl<K, T> 
implements AssociatedEndpoint {
    
    private static final long serialVersionUID = -657466488844239792L;
    
    private Object owner;
    
    private boolean disabled;
    
    private ModelProperty associationProperty;
    
    private transient int oppositePropertyId;
    
    private transient boolean suspended;

    public AssociatedKeyedReference(ObjectModel objectModel, int propertyId) {
        Arguments.mustNotBeNull("objectModel", objectModel);
        this.owner = objectModel.getOwner();
        ModelProperty property = objectModel.getModelClass().getProperty(propertyId);
        if (property.getAssociationType() != AssociationType.KEYED_REFERENCE) {
            throw new IllegalArgumentException(
                    CommonMessages.createFailedBecausePropertyMustBe(
                            this.getClass(), 
                            property,
                            KeyedReference.class
                    )
            );
        }
        if (property.getOppositeProperty() == null) {
            throw new IllegalArgumentException(
                    CommonMessages.createFailedBecauseOfunidirectionalAssociation(
                            this.getClass(), 
                            KeyedReference.class,
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
    public K getKey() {
        this.requiredEnabled();
        return super.getKey();
    }

    @Override
    public K getKey(boolean absolute) {
        this.requiredEnabled();
        return super.getKey(absolute);
    }

    @Override
    public K setKey(K key) {
        this.enable();
        return super.setKey(key);
    }

    @Override
    public T set(T value) {
        this.enable();
        return super.set(value);
    }

    @Override
    public T set(K key, T value) {
        this.enable();
        return super.set(key, value);
    }

    protected boolean isLoadedValue(T value) {
        return true;
    }
    
    protected boolean isAbandonableValue(T value) {
        return false;
    }
    
    protected void loadValue(T value) {
        
    }

    /*
     * Specially, be different with other associated end-points, 
     * AssociatiedKeyedReference chooses to change the opposite end-point
     * in the "modifying" event, not the "modified" event. 
     * 
     * For the Map-KeyReference association, if the KeyedReference is 
     * changed to be null, the modifying event with the opposite map removing 
     * happen before the modification of the KeyedRefence itself, so that
     * the flush in the "visionallyRemove" of the map will not update the
     * database and the "visionallyRemove" need not to load the map, because
     * the key to be removed can still be queried from database.
     */
    @Override
    protected void onModifying(KeyedValueEvent<K, T> e) throws Throwable {
        this.handler().preHandle(e);
    }
    
    private KeyedValueEventHandler<K, T> handler() {
        return new KeyedValueEventHandler<K, T>() {

            @Override
            protected AssociatedEndpoint getEndpoint() {
                return AssociatedKeyedReference.this;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedKeyedReference.this.suspended = suspended;
            }

            @Override
            protected boolean isLoadedObject(T opposite) {
                return AssociatedKeyedReference.this.isLoadedValue(opposite);
            }

            @Override
            protected boolean isAbandonableObject(T opposite) {
                return AssociatedKeyedReference.this.isAbandonableValue(opposite);
            }

            @Override
            protected void loadObject(T opposite) {
                AssociatedKeyedReference.this.loadValue(opposite);
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
