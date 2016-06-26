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
import java.util.List;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MAList;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XList;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.spi.wrapper.AbstractWrapperMAList;
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
public class AssociatedList<E> 
extends AbstractWrapperMAList<E> 
implements AssociatedEndpoint, Serializable {
    
    private static final long serialVersionUID = 7541738276478793792L;
    
    private Object owner;
    
    private ModelProperty associationProperty;
    
    private transient int oppositePropertyId;
    
    private transient boolean suspended;
    
    public AssociatedList(ObjectModel objectModel, int propertyId) {
        super(null);
        Arguments.mustNotBeNull("objectModel", objectModel);
        this.owner = objectModel.getOwner();
        ModelProperty property = objectModel.getModelClass().getProperty(propertyId);
        if (property.getAssociationType() != AssociationType.LIST ||
                !List.class.isAssignableFrom(property.getStandardCollectionType())) {
            throw new IllegalArgumentException(
                    CommonMessages.createFailedBecausePropertyMustBe(
                            this.getClass(), 
                            property,
                            List.class
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
        MAList<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoaded();
        }
        return true;
    }

    @Override
    public final boolean isLoadable() {
        MAList<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoadable();
        }
        return true;
    }

    @Override
    public final void load() {
        MAList<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            ((LazinessManageable)base).load();
        }
    }

    @Override
    protected void onModifying(ListElementEvent<E> e) {
        if (this.oppositePropertyId != -1) {
            this.handler().preHandle(e);
        }
    }

    @Override
    protected void onModified(ListElementEvent<E> e) {
        if (this.oppositePropertyId !=- 1) {
            this.handler().postHandle(e);
        }
    }
    
    private ListElementEventHandler<E> handler() {
        return new ListElementEventHandler<E>() {
            
            @Override
            protected MAList<E> getBase() {
                return AssociatedList.this.getBase();
            }

            @Override
            protected AssociatedEndpoint getEndpoint() {
                return AssociatedList.this;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedList.this.suspended = suspended;
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
    
    protected static class RootData<E> extends AbstractWrapperMAList.RootData<E> {

        private static final long serialVersionUID = 3662444674024868889L;
        
        private AssociatedList<E> owner;
        
        public RootData(AssociatedList<E> owner) {
            this.owner = owner;
        }
        
        @Override
        protected void setBase(MAList<E> base) {
            if (base != null) {
                BidiType bidiType = base.bidiType();
                if (bidiType == BidiType.NONE) {
                    throw new IllegalArgumentException(
                            CommonMessages.baseBidiTypeMustNotBeNone(BidiType.NONE)
                    );
                }
            }
            super.setBase(base);
        }

        @Override
        protected UnifiedComparator<? super E> getDefaultUnifiedComparator() {
            return (UnifiedComparator<? super E>)this.owner.associationProperty.getCollectionUnifiedComparator();
        }

        @Override
        protected MAList<E> createDefaultBase(UnifiedComparator<? super E> unifiedComparator) {
            return new MAArrayList<>(BidiType.NONNULL_VALUES, unifiedComparator);
        }

        @Override
        protected void onLoadTransientData() {
            XList<E> base = this.getBase();
            Validator<E> validator = new AssociatedCollectionValidator<E>(
                    this.<AssociatedList<E>>getRootWrapper());
            base.addValidator(validator);
        }

        @Override
        protected void onUnloadTranisentData() {
            XList<E> base = this.getBase();
            Validator<E> validator = new AssociatedCollectionValidator<E>(
                    this.<AssociatedList<E>>getRootWrapper());
            base.removeValidator(validator);
        }
    }
    
    private static class Serialization implements Serializable {
        
        private static final long serialVersionUID = 2027267125734024862L;
        
        private AssociatedList<?> endpoint;

        Serialization(AssociatedList<?> endpoint) {
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
            AssociatedList<?> endpoint = (AssociatedList<?>)om.getAssociatedEndpoint(propertyId);
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
