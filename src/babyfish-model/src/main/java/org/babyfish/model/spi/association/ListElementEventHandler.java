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

import java.util.Collection;
import java.util.Iterator;

import org.babyfish.collection.MAList;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.EventAttributeContext;
import org.babyfish.data.event.ModificationType;
import org.babyfish.data.event.PropertyVersion;
import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.spi.reference.IndexedReference;
import org.babyfish.model.spi.reference.Reference;

/**
 * @author Tao Chen
 */
abstract class ListElementEventHandler<E> extends EventHandler {
    
    private static final Object AK_INDEX_RANGE = new Object();
    
    protected abstract MAList<E> getBase();
    
    /*
     * Choose validate in "modifying" event so that the end-point validation 
     * of both two sides can be executed before real data changing.
     */
    @SuppressWarnings("unchecked")
    public void preHandle(ListElementEvent<E> e) {
        
        AssociatedEndpoint endpoint = this.getEndpoint();
        if (endpoint.getAssociationProperty().getOppositeProperty() == null) {
             return;
        }
         
        if (!endpoint.isSuspended()) {
            this.setSuspended(true);
            try {
                if (endpoint.getAssociationProperty().getOppositeProperty().getAssociationType() == AssociationType.INDEXED_REFERENCE) {
                    EventAttributeContext globalContext = e.getAttributeContext(AttributeScope.GLOBAL);
                    IndexRange indexRange = globalContext.getAttribute(AK_INDEX_RANGE);
                    if (indexRange == null) {
                        indexRange = new IndexRange();
                        globalContext.setAttribute(AK_INDEX_RANGE, indexRange);
                    }
                    indexRange.preHandle(e);
                }
                E attachedElement = e.getElement(PropertyVersion.ATTACH);
                if (attachedElement != null) {
                    AssociatedEndpoint oppositeEndpoint = endpoint.getOppositeEndpoint(attachedElement);
                    if (!oppositeEndpoint.isSuspended()) {
                        if (oppositeEndpoint instanceof Reference<?>) {
                            Reference<Object> reference = (Reference<Object>)oppositeEndpoint;
                            reference.validate(endpoint.getOwner());
                        } else if (oppositeEndpoint instanceof Collection) {
                            XCollection<Object> collection = (XCollection<Object>)oppositeEndpoint;
                            collection.validate(endpoint.getOwner());
                        } else {
                            throw new AssertionError();
                        }
                    }
                }
            } finally {
                this.setSuspended(false);
            }
        }
    }
    
    /*
     * Choose to modify opposite end-point in the "modified" event, becasue
     * 
     * If do the modification for opposite end-point in "modifying", such as:
     *      parent1.getChildNodes().addAll(parent2.getChildNodes());
     * The java.util.ConconrrentModificationException will be raised.
     */
    @SuppressWarnings("unchecked")
    public void postHandle(ListElementEvent<E> e) {
        
        AssociatedEndpoint endpoint = this.getEndpoint();
        if (endpoint.getAssociationProperty().getOppositeProperty() == null) {
             return;
        }
         
        if (!endpoint.isSuspended() && e.isModificationSuccessed()) {
            this.setSuspended(true);
            try {
                E detachedElement = e.getElement(PropertyVersion.DETACH);
                E attachedElement = e.getElement(PropertyVersion.ATTACH);
                if (detachedElement != null) {
                    AssociatedEndpoint oppositeEndpoint = endpoint.getOppositeEndpoint(detachedElement);
                    if (!oppositeEndpoint.isSuspended()) {
                        boolean isOppositeEndpointDisabled = oppositeEndpoint.isDisabled();
                        try {
                            if (oppositeEndpoint instanceof Reference<?>) {
                                Reference<?> reference = (Reference<?>)oppositeEndpoint;
                                reference.set(null);
                            } else if (oppositeEndpoint instanceof Collection<?>) {
                                Collection<?> collection = (Collection<?>)oppositeEndpoint;
                                collection.remove(endpoint.getOwner());
                            } else {
                                throw new AssertionError();
                            }
                        } finally {
                            if (isOppositeEndpointDisabled) {
                                oppositeEndpoint.disable();
                            }
                        }
                    }
                }
                if (attachedElement != null) {
                    AssociatedEndpoint oppositeEndpoint = endpoint.getOppositeEndpoint(attachedElement);
                    if (!oppositeEndpoint.isSuspended()) {
                        boolean isOppositeEndpointDisabled = oppositeEndpoint.isDisabled();
                        try {
                            if (oppositeEndpoint instanceof Reference<?>) {
                                Reference<Object> reference = (Reference<Object>)oppositeEndpoint;
                                reference.set(endpoint.getOwner());
                            } else if (oppositeEndpoint instanceof Collection<?>) {
                                Collection<Object> collection = (Collection<Object>)oppositeEndpoint;
                                collection.add(endpoint.getOwner());
                            } else {
                                throw new AssertionError();
                            }
                        } finally {
                            if (isOppositeEndpointDisabled) {
                                oppositeEndpoint.disable();
                            }
                        }
                    }
                }
                if (endpoint.getAssociationProperty().getOppositeProperty().getAssociationType() == AssociationType.INDEXED_REFERENCE) {
                    EventAttributeContext globalContext = e.getAttributeContext(AttributeScope.GLOBAL);
                    IndexRange indexRange = globalContext.getAttribute(AK_INDEX_RANGE);
                    if (indexRange != null && indexRange.postHandle()) {
                        globalContext.removeAttribute(AK_INDEX_RANGE);
                        MAList<E> baseList = this.getBase();
                        int minIndex = indexRange.getMinIndex();
                        int maxLoopCount = indexRange.getMaxIndex() - minIndex - 1;
                        Iterator<E> itr = baseList.listIterator(minIndex);
                        int loopCount = 0;
                        while (itr.hasNext() && loopCount < maxLoopCount) {
                            E element = itr.next();
                            IndexedReference<?> indexedReference = (IndexedReference<?>)endpoint.getOppositeEndpoint(element);
                            indexedReference.setIndex(minIndex + loopCount);
                            loopCount++;
                        }
                    }
                }
            } finally {
                this.setSuspended(false);
            }
        }
    }
    
    private static class IndexRange {
        
        private int minIndex = Integer.MAX_VALUE;
        
        private int maxIndex = -1;
        
        private int eventCount;
        
        public void preHandle(ListElementEvent<?> e) {
            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                this.minIndex = Math.min(this.minIndex, e.getIndex(PropertyVersion.DETACH));
            }
            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                this.minIndex = Math.min(this.minIndex, e.getIndex(PropertyVersion.ATTACH));
            }
            if (this.maxIndex == -1 && e.getModificationType() == ModificationType.REPLACE) {
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    this.maxIndex = Math.max(this.maxIndex, e.getIndex(PropertyVersion.DETACH));
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    this.maxIndex = Math.max(this.maxIndex, e.getIndex(PropertyVersion.ATTACH));
                }
            } else {
                this.maxIndex = Integer.MAX_VALUE;
            }
            this.eventCount++;
        }
        
        public boolean postHandle() {
            return --this.eventCount == 0;
        }
        
        public int getMinIndex() {
            return this.minIndex;
        }
        
        public int getMaxIndex() {
            return this.maxIndex;
        }
    }
}
