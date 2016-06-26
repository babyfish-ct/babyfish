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
package org.babyfish.collection.spi.laziness;

import java.util.Collection;
import java.util.Comparator;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MAHashSet;
import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MASet;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XSet;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.collection.event.modification.CollectionModifications;
import org.babyfish.collection.spi.wrapper.event.AbstractElementEventDispatcher;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.data.ModificationException;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.Cause;
import org.babyfish.data.event.PropertyVersion;
import org.babyfish.data.event.spi.GlobalAttributeContext;
import org.babyfish.data.event.spi.InAllChainAttributeContext;
import org.babyfish.lang.Ref;
import org.babyfish.lang.ReferenceComparator;

/**
 * @author Tao Chen
 */
public abstract class AbstractLazyMASet<E> extends AbstractLazyXSet<E> implements MASet<E> {
    
    private static final Object AK_ELEMENT_LISTENER = new Object();
    
    private transient ElementListener<E> elementListener;
    
    protected AbstractLazyMASet(MASet<E> baseSet) {
        super(baseSet);
    }

    protected AbstractLazyMASet(AbstractLazyMASet<E> parent, ViewInfo viewInfo) {
        super(parent, viewInfo);
    }
    
    /**
     * This method should not be invoked by the customer immediately.
     * 
     * <p>
     * It is used to create the instance during the when 
     * {@link java.io.ObjectInputStream} reads this object from a stream.
     * Although the derived classes of this class may implement {@link java.io.Serializable},
     * but this abstract super class does not implement {@link java.io.Serializable}
     * because it have some derived class that implements {@link View} which can 
     * not be implement {@link java.io.Serializable}
     * </p>
     * 
     * <p>
     * If the derived class is still a class does not implement {@link java.io.Serializable},
     * please support a no arguments constructor and mark it with {@link Deprecated}  too, 
     * like this method.
     * </p>
     */
    @Deprecated
    protected AbstractLazyMASet() {
        
    }
    
    @Override
    protected abstract RootData<E> createRootData();
    
    @Deprecated
    @Override
    protected final XSet<E> createBaseView(XSet<E> parentBase, ViewInfo viewInfo) {
        return this.createBaseView((MASet<E>)parentBase, viewInfo);
    }
    
    protected MASet<E> createBaseView(MASet<E> parentSet, ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }

    @Override
    protected AbstractElementEventDispatcher<E> createEventDispatcher() {
        return new AbstractElementEventDispatcher<E>(this) {

            @Override
            protected boolean isDispatchable() {
                return this
                        .<AbstractLazyMASet<E>>getOwner()
                        .getRootData()
                        .isDispatchable();
            }

            @Override
            protected void executePreDispatchedEvent(ElementEvent<E> dispatchedEvent) {
                this.<AbstractLazyMASet<E>>getOwner().executeModifying(dispatchedEvent);
            }

            @Override
            protected void executePostDispatchedEvent(ElementEvent<E> dispatchedEvent) {
                this.<AbstractLazyMASet<E>>getOwner().executeModified(dispatchedEvent);
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addElementListener(ElementListener<? super E> listener) {
        this.elementListener = ElementListener.combine(
                    this.elementListener, 
                    (ElementListener<E>)listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeElementListener(ElementListener<? super E> listener) {
        this.elementListener = ElementListener.remove(
                    this.elementListener, 
                    (ElementListener<E>)listener);
    }
    
    protected void executeModifying(ElementEvent<E> e) {
        Throwable finalThrowable = null;
        try {
            this.onModifying(e);    
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            this.raiseModifying(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        try {
            this.bubbleModifying(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw new ModificationException(false, e, finalThrowable);
        }
    }

    protected void executeModified(ElementEvent<E> e) {
        Throwable finalThrowable = null;
        try {
            this.bubbleModified(e);     
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            this.raiseModified(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        try {
            this.onModified(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw new ModificationException(true, e, finalThrowable);
        }
    }

    protected void onModifying(ElementEvent<E> e) throws Throwable {
        
    }

    protected void onModified(ElementEvent<E> e) throws Throwable {
        
    }

    protected void raiseModifying(ElementEvent<E> e) throws Throwable {
        ElementListener<E> elementListener = this.elementListener;
        if (elementListener != null) {
            e
            .getAttributeContext(AttributeScope.LOCAL)
            .addAttribute(AK_ELEMENT_LISTENER, elementListener);
            elementListener.modifying(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void raiseModified(ElementEvent<E> e) throws Throwable {
        ElementListener<E> elementListener = 
            (ElementListener<E>)
            e
            .getAttributeContext(AttributeScope.LOCAL)
            .removeAttribute(AK_ELEMENT_LISTENER);
        if (elementListener != null) {
            elementListener.modified(e);
        }
    }
    
    protected void bubbleModifying(ElementEvent<E> e) {
        AbstractLazyMASet<E> parent = this.getParent();
        if (parent != null) {
            ElementEvent<E> bubbledEvent = new ElementEvent<>(
                    parent, new Cause(e), null
            );
            parent.executeModifying(bubbledEvent);
        }
    }

    protected void bubbleModified(ElementEvent<E> e) {
        AbstractLazyMASet<E> parent = this.getParent();
        if (parent != null) {
            ElementEvent<E> bubbledEvent = e.getBubbledEvent(parent);
            parent.executeModified(bubbledEvent);
        }
    }
    
    @Override
    public MAIterator<E> iterator() {
        return new IteratorImpl<E>(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    Ref<E> visionallyAddImpl(E element) {
        RootData<E> rootData = this.getRootData();
        if (rootData.isLoaded()) {
            return null;
        }
        if (!rootData.isVisionallyReadable(QueuedOperationType.ATTACH)) {
            return null;
        }
        if (!rootData.getLazyBehaviorProcessor().preVisionallyRead(1)) {
            return null;
        }
        if (rootData.isLoading()) {
            throw new IllegalStateException(CommonMessages.visionOperationWhenDataIsBeingLoaded());
        }
        ReferenceComparator<? super E> referenceComparator = 
                rootData.getReferenceComparator();
        Ref<E> originalElementRef = rootData.visionallyRead(element, QueuedOperationType.ATTACH);
        if (originalElementRef == null) {
            return null;
        }
        E originalElement = originalElementRef.get();
        if (referenceComparator == null ? 
                        originalElement != element : 
                        !referenceComparator.same(originalElement, element)) {
            Modification<E> modification = CollectionModifications.add(element);
            ElementEvent<E> event = originalElement == null ?
                    ElementEvent.createAttachEvent(this, modification, element) :
                    ElementEvent.createReplaceEvent(this, modification, originalElement, element);
            this.doVisionallyOperationWithEvent(QueuedOperationType.ATTACH, event);
        }
        //return new Ref<E>(originalElement);
        return originalElementRef;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    Boolean visionallyAddAllImpl(Collection<? extends E> c) {
        if (c.isEmpty()) {
            return false;
        }
        RootData<E> rootData = this.getRootData();
        if (rootData.isLoaded()) {
            return null;
        }
        if (!rootData.isVisionallyReadable(QueuedOperationType.ATTACH)) {
            return null;
        }
        if (!rootData.getLazyBehaviorProcessor().preVisionallyRead(c.size())) {
            return null;
        }
        if (rootData.isLoading()) {
            throw new IllegalStateException(CommonMessages.visionOperationWhenDataIsBeingLoaded());
        }
        Modification<E> modification = CollectionModifications.addAll(c);
        ReferenceComparator<? super E> referenceComparator = 
                rootData.getReferenceComparator();
        c = this.standardCollection(c);
        int len = 0;
        ElementEvent<E>[] arr = new ElementEvent[c.size()];
        boolean retval = false;
        for (E e : (Collection<E>)c) {
            Ref<E> originalElementRef = rootData.visionallyRead(e, QueuedOperationType.ATTACH);
            if (originalElementRef == null) {
                return null;
            }
            E originalElement = originalElementRef.get();
            if (referenceComparator == null ?
                    originalElement == e :
                    referenceComparator.same(originalElement, e)) {
                continue;
            }
            if (originalElement == null) {
                retval = true;
            }
            arr[len++] = originalElement == null ?
                    ElementEvent.createAttachEvent(this, modification, e) :
                    ElementEvent.createReplaceEvent(this, modification, originalElement, e);
        }
        this.doVisionallyOperationWithEvent(QueuedOperationType.ATTACH, arr);
        return retval;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    Boolean visionallyRemoveImpl(Object element) {
        RootData<E> rootData = this.getRootData();
        if (rootData.isLoaded()) {
            return null;
        }
        if (!rootData.isVisionallyReadable(QueuedOperationType.DETACH)) {
            return null;
        }
        if (!rootData.getLazyBehaviorProcessor().preVisionallyRead(1)) {
            return null;
        }
        if (rootData.isLoading()) {
            throw new IllegalStateException(CommonMessages.visionOperationWhenDataIsBeingLoaded());
        }
        E e = (E)element;
        Ref<E> originalElementRef = rootData.visionallyRead(e, QueuedOperationType.DETACH);
        if (originalElementRef == null) {
            return null;
        }
        E originalElement = originalElementRef.get();
        if (originalElement == null) {
            return false;
        }
        Modification<E> modification = CollectionModifications.remove(element);
        ElementEvent<E> event = ElementEvent.createDetachEvent(this, modification, originalElement);
        this.doVisionallyOperationWithEvent(QueuedOperationType.DETACH, event);
        return true;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    Boolean visionallyRemoveAllImpl(Collection<?> c) {
        if (c.isEmpty()) {
            return false;
        }
        RootData<E> rootData = this.getRootData();
        if (rootData.isLoaded()) {
            return null;
        }
        if (!rootData.isVisionallyReadable(QueuedOperationType.DETACH)) {
            return null;
        }
        if (!rootData.getLazyBehaviorProcessor().preVisionallyRead(c.size())) {
            return null;
        }
        if (rootData.isLoading()) {
            throw new IllegalStateException(CommonMessages.visionOperationWhenDataIsBeingLoaded());
        }
        Modification<E> modification = CollectionModifications.removeAll(c);
        int len = 0;
        ElementEvent<E>[] arr = new ElementEvent[c.size()];
        for (E e : (Collection<E>)c) {
            Ref<E> originalElementRef = rootData.visionallyRead(e, QueuedOperationType.DETACH);
            if (originalElementRef == null) {
                return null;
            }
            E originalElement = originalElementRef.get();
            if (originalElement == null) {
                continue;
            }
            arr[len++] = ElementEvent.createDetachEvent(this, modification, originalElement);
        }
        this.doVisionallyOperationWithEvent(QueuedOperationType.DETACH, arr);
        return len != 0;
    }
    
    @SuppressWarnings("unchecked")
    private void doVisionallyOperationWithEvent(QueuedOperationType queuedOperationType, ElementEvent<E> ... events) {
        Throwable finalException = null;
        
        for (ElementEvent<E> event : events) {
            try {
                this.executeModifying(event);
            } catch (RuntimeException | Error ex) {
                if (finalException == null) {
                    finalException = ex;
                }
                ((InAllChainAttributeContext)event.getAttributeContext(AttributeScope.IN_ALL_CHAIN)).setPreThrowable(ex);
            }
        }
        
        if (finalException == null) {
            RootData<E> rootData = this.getRootData();
            PropertyVersion propertyVersion = 
                    queuedOperationType == QueuedOperationType.DETACH ? 
                            PropertyVersion.DETACH : 
                            PropertyVersion.ATTACH;
            for (ElementEvent<E> event : events) {
                E element = event.getElement(propertyVersion);
                try {
                    rootData.visionallyOperate(element, queuedOperationType);
                } catch (RuntimeException | Error ex) {
                    if (finalException == null) {
                        finalException = ex;
                    }
                }
            }
            if (finalException == null) {
                ((GlobalAttributeContext)events[0].getModification().getAttributeContext()).success();
            } else {
                ((GlobalAttributeContext)events[0].getModification().getAttributeContext()).setThrowable(finalException);
            }
        }
        
        for (ElementEvent<E> event : events) {
            try {
                this.executeModified(event);
            } catch (RuntimeException ex) {
                if (finalException == null) {
                    finalException = ex;
                }
            }
        }
        if (finalException instanceof RuntimeException) {
            throw (RuntimeException)finalException;
        }
        if (finalException instanceof Error) {
            throw (Error)finalException;
        }
    }
        
    @SuppressWarnings("unchecked")
    private XSet<E> standardCollection(Collection<?> c) {
        UnifiedComparator<? super E> uifiedComparator = this.getBase().unifiedComparator();
        if (c instanceof XSet<?>) {
            if (uifiedComparator.equals(((XSet<?>)c).unifiedComparator())) {
                return (XSet<E>)c;
            }
        }
        XSet<E> newC;
        if (uifiedComparator.comparator() != null) {
            newC = new TreeSet<E>(uifiedComparator.comparator());
        } else {
            newC = new LinkedHashSet<E>(uifiedComparator.equalityComparator());
        }
        newC.addAll((XSet<E>)c);
        return newC;
    }
    
    protected static abstract class AbstractIteratorImpl<E> extends AbstractLazyXSet.AbstractIteratorImpl<E> implements MAIterator<E> {
        
        private transient ElementListener<E> elementListener;

        protected AbstractIteratorImpl(AbstractLazyXSet<E> owner, ViewInfo viewInfo) {
            super(owner, viewInfo);
        }
        
        @Override
        protected AbstractElementEventDispatcher<E> createEventDispatcher() {
            return new AbstractElementEventDispatcher<E>(this) {

                @Override
                protected boolean isDispatchable() {
                    return this
                            .<AbstractIteratorImpl<E>>getOwner()
                            .<AbstractLazyMASet<E>>getParent()
                            .getRootData()
                            .isDispatchable();
                }

                @Override
                protected void executePreDispatchedEvent(ElementEvent<E> dispatchedEvent) {
                    this.<AbstractIteratorImpl<E>>getOwner().executeModifying(dispatchedEvent);
                }

                @Override
                protected void executePostDispatchedEvent(ElementEvent<E> dispatchedEvent) {
                    this.<AbstractIteratorImpl<E>>getOwner().executeModified(dispatchedEvent);
                }
            };
        }

        @SuppressWarnings("unchecked")
        @Override
        public void addElementListener(ElementListener<? super E> listener) {
            this.elementListener = ElementListener.combine(
                        this.elementListener, 
                        (ElementListener<E>)listener);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removeElementListener(ElementListener<? super E> listener) {
            this.elementListener = ElementListener.remove(
                        this.elementListener, 
                        (ElementListener<E>)listener);
        }
        
        protected void executeModifying(ElementEvent<E> e) {
            Throwable finalThrowable = null;
            try {
                this.onModifying(e);    
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                this.raiseModifying(e);
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            try {
                this.bubbleModifying(e);
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            if (finalThrowable != null) {
                throw new ModificationException(false, e, finalThrowable);
            }
        }

        protected void executeModified(ElementEvent<E> e) {
            Throwable finalThrowable = null;
            try {
                this.bubbleModified(e);     
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                this.raiseModified(e);
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            try {
                this.onModified(e);
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            if (finalThrowable != null) {
                throw new ModificationException(true, e, finalThrowable);
            }
        }

        protected void onModifying(ElementEvent<E> e) throws Throwable {
            
        }

        protected void onModified(ElementEvent<E> e) throws Throwable {
            
        }

        protected void raiseModifying(ElementEvent<E> e) throws Throwable {
            ElementListener<E> elementListener = this.elementListener;
            if (elementListener != null) {
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .addAttribute(AK_ELEMENT_LISTENER, elementListener);
                elementListener.modifying(e);
            }
        }
        
        @SuppressWarnings("unchecked")
        protected void raiseModified(ElementEvent<E> e) throws Throwable {
            ElementListener<E> elementListener = 
                (ElementListener<E>)
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .removeAttribute(AK_ELEMENT_LISTENER);
            if (elementListener != null) {
                elementListener.modified(e);
            }
        }
        
        protected void bubbleModifying(ElementEvent<E> e) {
            AbstractLazyMASet<E> parent = this.<AbstractLazyMASet<E>>getParent();
            ElementEvent<E> bubbledEvent = new ElementEvent<>(
                    parent, new Cause(e), null
            );
            parent.executeModifying(bubbledEvent);
        }

        protected void bubbleModified(ElementEvent<E> e) {
            AbstractLazyMASet<E> parent = this.<AbstractLazyMASet<E>>getParent();
            ElementEvent<E> bubbledEvent = e.getBubbledEvent(parent);
            parent.executeModified(bubbledEvent);
        }

        @Deprecated
        @Override
        protected final XIterator<E> createBaseView(
                XCollection<E> baseParent, ViewInfo viewInfo) {
            return this.createBaseView((MASet<E>)baseParent, viewInfo);
        }
        
        protected abstract MAIterator<E> createBaseView(
                MASet<E> baseParent, ViewInfo viewInfo);
        
    }
    
    protected static class IteratorImpl<E> extends AbstractIteratorImpl<E> {

        protected IteratorImpl(AbstractLazyXSet<E> owner) {
            super(owner, CollectionViewInfos.iterator());
        }

        @Override
        protected MAIterator<E> createBaseView(
                MASet<E> baseParent, ViewInfo viewInfo) {
            if (viewInfo instanceof CollectionViewInfos.Iterator) {
                return baseParent.iterator();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
    }
    
    protected abstract static class RootData<E> extends AbstractLazyXSet.RootData<E> {

        private static final long serialVersionUID = -5800321224266756962L;
        
        private static final Object AK_DELAY_MAP_EVENT_HANDLE = new Object();
        
        public RootData() {
            
        }

        @Deprecated
        @Override
        protected final void setBase(XSet<E> base) {
            this.setBase((MASet<E>)base);
        }
        
        protected void setBase(MASet<E> base) {
            super.setBase(base);
        }

        @Override
        protected MASet<E> createDefaultBase(
                UnifiedComparator<? super E> unifiedComparator) {
            Comparator<? super E> comparator = unifiedComparator.comparator();
            if (comparator != null) {
                return new MATreeSet<E>(comparator);
            }
            return new MAHashSet<E>(unifiedComparator.equalityComparator());
        }

        @Override
        XMap<E, QueuedOperationType> createDelayMap() {
            UnifiedComparator<? super E> unifiedComparator = this.unifiedComparator();
            Comparator<? super E> comparator = unifiedComparator.comparator();
            MAMap<E, QueuedOperationType> map;
            if (comparator != null) {
                map = new MATreeMap<E, QueuedOperationType>(
                        ReplacementRule.NEW_REFERENCE_WIN, 
                        comparator);
            } else {
                map = new MALinkedHashMap<E, QueuedOperationType>(
                        ReplacementRule.NEW_REFERENCE_WIN, 
                        unifiedComparator.equalityComparator(),
                        (EqualityComparator<QueuedOperationType>)null,
                        false,
                        OrderAdjustMode.TAIL,
                        OrderAdjustMode.TAIL);
            }
            map.addMapElementListener(
                    new MapElementListener<E, QueuedOperationType>() {
                        @Override
                        public void modifying(MapElementEvent<E, QueuedOperationType> e) throws Throwable {
                            if (RootData.this.isDelayMapEventEnabled()) {
                                ElementEvent<E> event;
                                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                                    E element = e.getKey(PropertyVersion.ATTACH);
                                    if (e.getValue(PropertyVersion.ATTACH) == QueuedOperationType.DETACH) {
                                        event = ElementEvent.createDetachEvent(
                                                RootData.this.getRootWrapper(), 
                                                CollectionModifications.<E>remove(element), 
                                                element);
                                    } else {
                                        event = ElementEvent.createAttachEvent(
                                                RootData.this.getRootWrapper(), 
                                                CollectionModifications.<E>add(element), 
                                                element);
                                    }
                                } else {
                                    E element = e.getKey(PropertyVersion.DETACH);
                                    if (e.getValue(PropertyVersion.DETACH) == QueuedOperationType.DETACH) {
                                        event = ElementEvent.createAttachEvent(
                                                RootData.this.getRootWrapper(), 
                                                CollectionModifications.<E>remove(element), 
                                                element);
                                    } else {
                                        event = ElementEvent.createDetachEvent(
                                                RootData.this.getRootWrapper(), 
                                                CollectionModifications.<E>add(element), 
                                                element);
                                    }
                                }
                                e
                                .getAttributeContext(AttributeScope.LOCAL)
                                .addAttribute(AK_DELAY_MAP_EVENT_HANDLE, event);
                                RootData.this.<AbstractLazyMASet<E>>getRootWrapper().executeModifying(event);
                            }
                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        public void modified(MapElementEvent<E, QueuedOperationType> e) throws Throwable {
                            ElementEvent<E> event =
                            (ElementEvent<E>)
                            e
                            .getAttributeContext(AttributeScope.LOCAL)
                            .removeAttribute(AK_DELAY_MAP_EVENT_HANDLE);
                            if (event != null) {
                                RootData.this.<AbstractLazyMASet<E>>getRootWrapper().executeModified(event);
                            }
                        }
                    });
            return map;
        }
        
    }

}
