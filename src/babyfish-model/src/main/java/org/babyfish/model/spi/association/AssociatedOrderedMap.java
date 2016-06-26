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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.spi.wrapper.AbstractWrapperMAOrderedMap;
import org.babyfish.data.LazinessManageable;
import org.babyfish.lang.Arguments;
import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.metadata.ModelProperty;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelProvider;

/**
 * @author Tao Chen
 */
public class AssociatedOrderedMap<K, V> 
extends AbstractWrapperMAOrderedMap<K, V> 
implements AssociatedEndpoint, Serializable {

    private static final long serialVersionUID = -5009326562013053974L;
    
    private Object owner;
    
    private ModelProperty associationProperty;
    
    private transient int oppositePropertyId;
    
    private transient boolean suspended;
    
    public AssociatedOrderedMap(ObjectModel objectModel, int propertyId) {
        super(null);
        Arguments.mustNotBeNull("objectModel", objectModel);
        this.owner = objectModel.getOwner();
        ModelProperty property = objectModel.getModelClass().getProperty(propertyId);
        if (property.getAssociationType() != AssociationType.MAP) {
            throw new IllegalArgumentException(
                CommonMessages.createFailedBecausePropertyMustBe(
                        this.getClass(), 
                        property,
                        Map.class
                )
            );
        }
        if (SortedMap.class.isAssignableFrom(property.getStandardCollectionType())) {
            throw new IllegalArgumentException(
                    CommonMessages.createFailedBecausePropertyMustNotBe(
                            this.getClass(), 
                            property,
                            SortedMap.class
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
    public final AssociatedEndpoint getOppositeEndpoint(Object opposite) {
        if (this.oppositePropertyId == -1) {
            return null;
        }
        return ((ObjectModelProvider)opposite).objectModel().getAssociatedEndpoint(this.oppositePropertyId);
    }
    
    @Override
    public final boolean isLoaded() {
        MAOrderedMap<K, V> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoaded();
        }
        return true;
    }

    @Override
    public final boolean isLoadable() {
        MAOrderedMap<K, V> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoadable();
        }
        return true;
    }

    @Override
    public final void load() {
        MAOrderedMap<K, V> base = this.getBase();
        if (base instanceof LazinessManageable) {
            ((LazinessManageable)base).load();
        }
    }

    protected boolean isLoadedValue(V value) {
        return true;
    }
    
    protected boolean isAbandonableValue(V value) {
        return false;
    }
    
    protected void loadValue(V value) {
        
    }
    
    @Override
    protected void onModifying(MapElementEvent<K, V> e) throws Throwable {
        if (this.oppositePropertyId != -1) {
            this.handler().preHandle(e);
        }
    }
    
    @Override
    protected void onModified(MapElementEvent<K, V> e) throws Throwable {
        if (this.oppositePropertyId != -1) {
            this.handler().postHandle(e);
        }
    }
    
    private MapElementEventHandler<K, V> handler() {
        return new MapElementEventHandler<K, V>() {

            @Override
            protected AssociatedEndpoint getEndpoint() {
                return AssociatedOrderedMap.this;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedOrderedMap.this.suspended = suspended;
            }

            @Override
            protected boolean isLoadedObject(V opposite) {
                return AssociatedOrderedMap.this.isLoadedValue(opposite);
            }

            @Override
            protected boolean isAbandonableObject(V opposite) {
                return AssociatedOrderedMap.this.isAbandonableValue(opposite);
            }

            @Override
            protected void loadObject(V opposite) {
                AssociatedOrderedMap.this.loadValue(opposite);
            }
        };
    }

    @Override
    protected RootData<K, V> createRootData() {
        return new RootData<K, V>(this);
    }
    
    protected Object writeReplace() throws ObjectStreamException {
        return new Serialization(this);
    }
    
    protected static class RootData<K, V> extends AbstractWrapperMAOrderedMap.RootData<K, V> {
        
        private static final long serialVersionUID = -6128129267074846438L;

        private AssociatedOrderedMap<K, V> owner;
        
        public RootData(AssociatedOrderedMap<K, V> owner) {
            this.owner = owner;
        }

        @Override
        protected UnifiedComparator<? super K> getDefaultKeyUnifiedComparator() {
            return this.owner.getAssociationProperty().getKeyUnifiedComparator();
        }

        @Override
        protected UnifiedComparator<? super V> getDefaultValueUnifiedComparator() {
            return this.owner.getAssociationProperty().getCollectionUnifiedComparator();
        }

        @Override
        protected MAOrderedMap<K, V> createDefaultBase(
                UnifiedComparator<? super K> keyUnifiedComparator,
                UnifiedComparator<? super V> valueUnifiedComparator) {
            return new MALinkedHashMap<>(
                    BidiType.NONNULL_VALUES,
                    keyUnifiedComparator.equalityComparator(true), 
                    valueUnifiedComparator
            );
        }

        /**
         * @exception IllegalArgumentException The parameter base's 
         * {@link MAOrderedMap#keyReplacementRule()} does not return {@link ReplacementRule#NEW_REFERENCE_WIN}
         */
        @Override
        protected void setBase(MAOrderedMap<K, V> base) {
            if (this.getBase(true) != base) {
                if (base != null) {
                    if (base.keyReplacementRule() != ReplacementRule.NEW_REFERENCE_WIN) {
                        throw new IllegalArgumentException(
                                CommonMessages.baseReplacementRuleMustBe(
                                        base.keyReplacementRule()
                                )
                        );
                    }
                    BidiType bidiType = base.bidiType();
                    if (bidiType == BidiType.NONE) {
                        throw new IllegalArgumentException(
                                CommonMessages.baseBidiTypeMustNotBeNone(BidiType.NONE)
                        );
                    }
                }
                super.setBase(base);
            }
        }
    }
    
    private static class Serialization implements Serializable {
    
        private static final long serialVersionUID = 6783502040871897747L;
        
        private AssociatedOrderedMap<?, ?> endpoint;

        Serialization(AssociatedOrderedMap<?, ?> endpoint) {
            this.endpoint = endpoint;
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(((ObjectModelProvider)this.endpoint.getOwner()).objectModel());
            out.writeInt(this.endpoint.getAssociationProperty().getId());
            out.writeObject(this.endpoint.getBase(true));
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            ObjectModel om = (ObjectModel)in.readObject();
            int propertyId = in.readInt();
            Object base = in.readObject();
            AssociatedOrderedMap<?, ?> endpoint = (AssociatedOrderedMap<?, ?>)om.getAssociatedEndpoint(propertyId);
            if (base != null) {
                endpoint.replace(base);
            }
            this.endpoint = endpoint;
        }
        
        private Object readResolve() throws ObjectStreamException {
            return this.endpoint;
        }
    }
}
