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
import java.util.Collection;
import java.util.SortedMap;
import java.util.SortedSet;

import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.spi.wrapper.AbstractWrapperMAOrderedSet;
import org.babyfish.data.LazinessManageable;
import org.babyfish.lang.Arguments;
import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.metadata.ModelProperty;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelProvider;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public class AssociatedOrderedSet<E> 
extends AbstractWrapperMAOrderedSet<E>
implements AssociatedEndpoint, Serializable {

    private static final long serialVersionUID = -948025344874227090L;
    
    private Object owner;
    
    private ModelProperty associationProperty;
    
    private transient int oppositePropertyId;
    
    private transient boolean suspended;
    
    public AssociatedOrderedSet(ObjectModel objectModel, int propertyId) {
        super(null);
        Arguments.mustNotBeNull("objectModel", objectModel);
        this.owner = objectModel.getOwner();
        ModelProperty property = objectModel.getModelClass().getProperty(propertyId);
        if (property.getAssociationType() != AssociationType.COLLECTION) {
            throw new IllegalArgumentException(
                CommonMessages.createFailedBecausePropertyMustBe(
                        this.getClass(), 
                        property,
                        Collection.class
                )
            );
        }
        if (SortedSet.class.isAssignableFrom(property.getStandardCollectionType())) {
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
        MAOrderedSet<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoaded();
        }
        return true;
    }

    @Override
    public final boolean isLoadable() {
        MAOrderedSet<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoadable();
        }
        return true;
    }

    @Override
    public final void load() {
        MAOrderedSet<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            ((LazinessManageable)base).load();
        }
    }

    protected boolean isLoadedElement(E element) {
        return true;
    }
    
    protected boolean isAbandonableElement(E element) {
        return false;
    }
    
    protected void loadElement(E element) {
        
    }

    @Override
    protected void onModifying(ElementEvent<E> e) throws Throwable {
        if (this.oppositePropertyId != -1) {
            this.handler().preHandle(e);
        }
    }
    
    @Override
    protected void onModified(ElementEvent<E> e) throws Throwable {
        if (this.oppositePropertyId != -1) {
            this.handler().postHandle(e);
        }
    }
    
    private ElementEventHandler<E> handler() {
        return new ElementEventHandler<E>() {

            @Override
            protected AssociatedEndpoint getEndpoint() {
                return AssociatedOrderedSet.this;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedOrderedSet.this.suspended = suspended;
            }

            @Override
            protected boolean isLoadedObject(E opposite) {
                return AssociatedOrderedSet.this.isLoadedElement(opposite);
            }

            @Override
            protected boolean isAbandonableObject(E opposite) {
                return AssociatedOrderedSet.this.isAbandonableElement(opposite);
            }

            @Override
            protected void loadObject(E opposite) {
                AssociatedOrderedSet.this.loadElement(opposite);
            }
        };
    }
    
    @Override
    protected RootData<E> createRootData() {
        return new RootData<E>(this);
    }
    
    protected Object writeReplace() throws ObjectStreamException {
        return new Serialization(this);
    }

    protected static class RootData<E> extends AbstractWrapperMAOrderedSet.RootData<E> {

        private static final long serialVersionUID = -4840185558404355705L;
        
        private AssociatedOrderedSet<E> owner;
        
        public RootData(AssociatedOrderedSet<E> owner) {
            this.owner = owner;
        }

        @Override
        protected UnifiedComparator<? super E> getDefaultUnifiedComparator() {
            return (UnifiedComparator<? super E>)this.owner.associationProperty.getCollectionUnifiedComparator();
        }

        @Override
        protected MAOrderedSet<E> createDefaultBase(UnifiedComparator<? super E> unifiedComparator) {
            return new MALinkedHashSet<>(unifiedComparator.equalityComparator(true));
        }

        /**
         * @exception IllegalArgumentException The parameter base's 
         * {@link MAOrderedSet#replacementRule()} does not return {@link ReplacementRule#NEW_REFERENCE_WIN}
         */
        @Override
        protected void setBase(MAOrderedSet<E> base) {
            if (this.getBase(true) != base) {
                if (base != null && base.replacementRule() != ReplacementRule.NEW_REFERENCE_WIN) {
                    throw new IllegalArgumentException(
                            CommonMessages.baseReplacementRuleMustBe(
                                    base.replacementRule()
                            )
                    );
                }
                super.setBase(base);
            }
        }
        
        @Override
        protected void onLoadTransientData() {
            XCollection<E> base = this.getBase();
            Validator<E> validator = new AssociatedCollectionValidator<E>(
                    this.<AssociatedOrderedSet<E>>getRootWrapper());
            base.addValidator(validator);
        }

        @Override
        protected void onUnloadTranisentData() {
            XCollection<E> base = this.getBase();
            Validator<E> validator = new AssociatedCollectionValidator<E>(
                    this.<AssociatedOrderedSet<E>>getRootWrapper());
            base.removeValidator(validator);
        }
    }
    
    private static class Serialization implements Serializable {
        
        private static final long serialVersionUID = 7667563989450737656L;
        
        private AssociatedOrderedSet<?> endpoint;

        Serialization(AssociatedOrderedSet<?> endpoint) {
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
            AssociatedOrderedSet<?> endpoint = (AssociatedOrderedSet<?>)om.getAssociatedEndpoint(propertyId);
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
