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
import java.util.SortedSet;

import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.spi.wrapper.AbstractWrapperMANavigableSet;
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
public class AssociatedNavigableSet<E> 
extends AbstractWrapperMANavigableSet<E>
implements AssociatedEndpoint, Serializable {

    private static final long serialVersionUID = -948025344874227090L;
    
    private Object owner;
    
    private ModelProperty associationProperty;
    
    private transient int oppositePropertyId;
    
    private transient boolean suspended;
    
    public AssociatedNavigableSet(ObjectModel objectModel, int propertyId) {
        super(null);
        Arguments.mustNotBeNull("objectModel", objectModel);
        this.owner = objectModel.getOwner();
        ModelProperty property = objectModel.getModelClass().getProperty(propertyId);
        if (property.getAssociationType() != AssociationType.COLLECTION ||
                !SortedSet.class.isAssignableFrom(property.getStandardCollectionType())) {
            throw new IllegalArgumentException(
                    CommonMessages.createFailedBecausePropertyMustBe(
                            this.getClass(), 
                            property,
                            SortedSet.class
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
        MANavigableSet<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoaded();
        }
        return true;
    }

    @Override
    public final boolean isLoadable() {
        MANavigableSet<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoadable();
        }
        return true;
    }

    @Override
    public final void load() {
        MANavigableSet<E> base = this.getBase();
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
                return AssociatedNavigableSet.this;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedNavigableSet.this.suspended = suspended;
            }

            @Override
            protected boolean isLoadedObject(E opposite) {
                return AssociatedNavigableSet.this.isLoadedElement(opposite);
            }

            @Override
            protected boolean isAbandonableObject(E opposite) {
                return AssociatedNavigableSet.this.isAbandonableElement(opposite);
            }

            @Override
            protected void loadObject(E opposite) {
                AssociatedNavigableSet.this.loadElement(opposite);
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

    protected static class RootData<E> extends AbstractWrapperMANavigableSet.RootData<E> {

        private static final long serialVersionUID = -4840185558404355705L;
        
        private AssociatedNavigableSet<E> owner;

        public RootData(AssociatedNavigableSet<E> owner) {
            this.owner = owner;
        }

        @Override
        protected UnifiedComparator<? super E> getDefaultUnifiedComparator() {
            return (UnifiedComparator<? super E>)this.owner.associationProperty.getCollectionUnifiedComparator();
        }

        @Override
        protected MANavigableSet<E> createDefaultBase(UnifiedComparator<? super E> unifiedComparator) {
            return new MATreeSet<>(unifiedComparator.comparator(true));
        }

        /**
         * @exception IllegalArgumentException The parameter base's 
         * {@link MANavigableSet#replacementRule()} does not return {@link ReplacementRule#NEW_REFERENCE_WIN}
         */
        @Override
        protected void setBase(MANavigableSet<E> base) {
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
                    this.<AssociatedNavigableSet<E>>getRootWrapper());
            base.addValidator(validator);
        }

        @Override
        protected void onUnloadTranisentData() {
            XCollection<E> base = this.getBase();
            Validator<E> validator = new AssociatedCollectionValidator<E>(
                    this.<AssociatedNavigableSet<E>>getRootWrapper());
            base.removeValidator(validator);
        }
    }
    
    private static class Serialization implements Serializable {
        
        private static final long serialVersionUID = -7734201870995582582L;
        
        private AssociatedNavigableSet<?> endpoint;

        Serialization(AssociatedNavigableSet<?> endpoint) {
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
            AssociatedNavigableSet<?> endpoint = (AssociatedNavigableSet<?>)om.getAssociatedEndpoint(propertyId);
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
