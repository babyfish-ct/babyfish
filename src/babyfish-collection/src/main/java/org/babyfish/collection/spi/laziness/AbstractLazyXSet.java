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
import java.util.Map.Entry;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XSet;
import org.babyfish.collection.spi.wrapper.AbstractWrapperXSet;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.data.LazinessManageable;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;
import org.babyfish.lang.Ref;
import org.babyfish.lang.ReferenceComparator;

/**
 * @author Tao Chen
 */
public abstract class AbstractLazyXSet<E>
extends AbstractWrapperXSet<E> 
implements LazinessManageable {
    
    protected AbstractLazyXSet(XSet<E> base) {
        super(base);
    }
    
    protected AbstractLazyXSet(AbstractLazyXSet<E> parent, ViewInfo viewInfo) {
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
    protected AbstractLazyXSet() {
        
    }
    
    @Override
    public final boolean isLoaded() {
        return this.<RootData<E>>getRootData().isLoaded();
    }

    @Override
    public final boolean isLoadable() {
        return this.<RootData<E>>getRootData().isLoadable();
    }

    @Override
    public final void load() {
        this.<RootData<E>>getRootData().load();
    }

    @Override
    public int hashCode() {
        this.load();
        return this.getBase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        this.load();
        return this.getBase().equals(obj);
    }

    @Override
    public String toString() {
        if (!this.isLoaded()) {
            return "{ lazySet : Not Loaded }";
        }
        return this.getBase().toString();
    }

    @Override
    public boolean isEmpty() {
        this.requiredEnabled();
        Boolean visionIsEmpty = this.<RootData<E>>getRootData().visionallyReadIsEmpty();
        if (visionIsEmpty != null) {
            return visionIsEmpty;
        }
        this.load();
        return this.getBase().isEmpty();
    }

    @Override
    public int size() {
        this.requiredEnabled();
        if (this.getParent() == null) {
            int size = this.<RootData<E>>getRootData().visionallyReadSize();
            if (size != -1) {
                return size;
            }
        }
        this.load();
        return this.getBase().size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        this.requiredEnabled();
        Ref<E> ref = 
                this
                .<RootData<E>>getRootData()
                .visionallyRead(
                        (E)o, 
                        null
                );
        if (ref != null) {
            E originalElement = ref.get();
            if (originalElement == null) {
                return false;
            }
            return true;
        }
        this.load();
        return this.getBase().contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        this.requiredEnabled();
        this.load();
        return this.getBase().containsAll(c);
    }

    @Override
    public Object[] toArray() {
        this.requiredEnabled();
        this.load();
        return this.getBase().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        this.requiredEnabled();
        this.load();
        return this.getBase().toArray(a);
    }

    @Override
    public void clear() {
        this.enable();
        this.load();
        this.getBase().clear();
    }

    @Override
    public boolean add(E e) {
        this.enable();
        Ref<E> ref = this.visionallyAdd(e);
        if (ref != null) {
            return ref.get() == null;
        }
        this.load();
        return this.getBase().add(e);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        this.enable();
        Boolean retval = this.visionallyRemove((E)o);
        if (retval != null) {
            return retval.booleanValue();
        }
        this.load();
        return this.getBase().remove(o);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        this.enable();
        Boolean retval = this.visionallyAddAll(c);
        if (retval != null) {
            return retval.booleanValue();
        }
        this.load();
        return this.getBase().addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        this.enable();
        Boolean retval = this.visionallyRemoveAll(c);
        if (retval != null) {
            return retval.booleanValue();
        }
        this.load();
        return this.getBase().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        this.enable();
        this.load();
        return this.getBase().retainAll(c);
    }

    @Override
    public XIterator<E> iterator() {
        return new IteratorImpl<E>(this);
    }
    
    public XMap<E, QueuedOperationType> getQueuedOperations() {
        return this.<RootData<E>>getRootData().getQueuedOperations();
    }
    
    public void performQueuedOperations() {
        this.<RootData<E>>getRootData().performQueuedOperations();
    }

    @Override
    protected abstract RootData<E> createRootData();
    
    protected final Ref<E> visionallyAdd(E element) {
        return this.visionallyAddImpl(element);
    }

    protected final Boolean visionallyAddAll(Collection<? extends E> c) {
        return this.visionallyAddAllImpl(c);
    }

    protected final Boolean visionallyRemove(Object element) {
        return this.visionallyRemoveImpl(element);
    }

    protected final Boolean visionallyRemoveAll(Collection<?> c) {
        return this.visionallyRemoveAllImpl(c);
    }

    Ref<E> visionallyAddImpl(E element) {
        RootData<E> rootData = this.<RootData<E>>getRootData();
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
        Ref<E> originalElementRef = rootData.visionallyRead(element, QueuedOperationType.ATTACH);
        if (originalElementRef == null) {
            return null;
        }
        rootData.visionallyOperate(element, QueuedOperationType.ATTACH);
        return originalElementRef;
    }
    
    @SuppressWarnings("unchecked")
    Boolean visionallyAddAllImpl(Collection<? extends E> c) {
        RootData<E> rootData = this.<RootData<E>>getRootData();
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
        ReferenceComparator<? super E> referenceComparator = 
                rootData.getReferenceComparator();
        int len = 0;
        E[] arr = (E[])new Object[c.size()];
        boolean retval = false;
        for (E e : (Collection<E>)c) {
            Ref<E> originalElementRef = rootData.visionallyRead(e, QueuedOperationType.ATTACH);
            if (originalElementRef == null) {
                return null;
            }
            E originalElement = originalElementRef.get();
            if (originalElement == null) {
                retval = true;
            }
            if (referenceComparator == null ?
                    originalElement == e :
                    referenceComparator.same(originalElement, e)) {
                continue;
            }
            arr[len++] = e;
        }
        for (int i = 0; i < len; i++) {
            rootData.visionallyOperate(arr[i], QueuedOperationType.ATTACH);
        }
        return retval;
    }
    
    @SuppressWarnings("unchecked")
    Boolean visionallyRemoveImpl(Object element) {
        RootData<E> rootData = this.<RootData<E>>getRootData();
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
        rootData.visionallyOperate(originalElement, QueuedOperationType.DETACH);
        return true;
    }
    
    @SuppressWarnings("unchecked")
    Boolean visionallyRemoveAllImpl(Collection<?> c) {
        RootData<E> rootData = this.<RootData<E>>getRootData();
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
        int len = 0;
        E[] arr = (E[])new Object[c.size()];
        for (E e : (Collection<E>)c) {
            Ref<E> originalElementRef = rootData.visionallyRead(e, QueuedOperationType.DETACH);
            if (originalElementRef == null) {
                return null;
            }
            E originalElement = originalElementRef.get();
            if (originalElement == null) {
                continue;
            }
            arr[len++] = originalElement;
        }
        for (int i = 0; i < len; i++) {
            rootData.visionallyOperate(arr[i], QueuedOperationType.DETACH);
        }
        return len != 0;
    }
    
    protected static abstract class AbstractIteratorImpl<E> extends AbstractWrapperXSet.AbstractIteratorImpl<E> {
        
        public AbstractIteratorImpl(AbstractLazyXSet<E> parent, ViewInfo viewInfo) {
            super(loadParent(parent), viewInfo);
        }
        
        private static <E> AbstractLazyXSet<E> loadParent(AbstractLazyXSet<E> parent) {
            parent.load();
            return parent;
        }
    }
    
    protected static class IteratorImpl<E> extends AbstractIteratorImpl<E> {

        public IteratorImpl(AbstractLazyXSet<E> parent) {
            super(parent, CollectionViewInfos.iterator());
        }

        @Override
        protected XIterator<E> createBaseView(
                XCollection<E> baseParent, ViewInfo viewInfo) {
            if (viewInfo instanceof CollectionViewInfos.Iterator) {
                return baseParent.iterator();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
    }
    
    protected static abstract class RootData<E> extends AbstractWrapperXSet.RootData<E> {
        
        private static final long serialVersionUID = -1003020604568816843L;
        
        private XMap<E, QueuedOperationType> delayMap;
        
        private transient boolean delayMapEventDisabled;
        
        private transient LazyBehaviorProcessor lazyBehaviorProcessor;
        
        protected RootData() {
            
        }
        
        public abstract boolean isLoaded();
        
        public abstract boolean isLoading();
        
        protected abstract void setLoaded(boolean loaded);
        
        protected abstract void setLoading(boolean loading);
        
        public abstract boolean isLoadable();

        public final void load() {
            if (!this.isLoaded()) {
                if (this.isLoading()) {
                    throw new IllegalStateException(CommonMessages.loadingOperationWhenDataIsBeingLoaded());
                }
                this.setLoading(true);
                try {
                    this.onLoad();
                    //The second setLoading(true) is very important! 
                    //because onLoad() may set the initializing to be false.
                    this.setLoading(true);
                    this.performQueuedOperations();
                } finally {
                    this.setLoading(false);
                }
                this.setLoaded(true);
            }
        }
        
        @Override
        public boolean isDispatchable() {
            if (this.isLoading()) {
                return false;
            }
            return super.isDispatchable();
        }

        public final boolean isVisionallyReadable(QueuedOperationType nullOrOperationType) {
            if (this.isLoaded()) {
                return false;
            }
            return this.onGetVisionallyReadable(nullOrOperationType);
        }
        
        public final Boolean visionallyReadIsEmpty() {
            if (this.isVisionallyReadable(null)) {
                XMap<E, QueuedOperationType> delayMap = this.delayMap;
                if (delayMap != null && delayMap.containsValue(QueuedOperationType.ATTACH)) {
                    return false;
                }
                LazyBehaviorProcessor lazyBehaviorProcessor = this.getLazyBehaviorProcessor();
                int visionalSize = this.visionallyReadSize();
                if (visionalSize != -1) {
                    lazyBehaviorProcessor.visionallyReadSize();
                    return visionalSize == 0;
                }
            }
            return null;
        }
        
        public final int visionallyReadSize() {
            if (this.isVisionallyReadable(null)) {
                LazyBehaviorProcessor lazyBehaviorProcessor = this.getLazyBehaviorProcessor();
                if (lazyBehaviorProcessor.preVisionallyReadSize()) {
                    int size = this.onGetVisionalSize();
                    if (size != -1) {
                        lazyBehaviorProcessor.visionallyReadSize();
                    }
                }
            }
            return -1;
        }

        public final Ref<E> visionallyRead(E like, QueuedOperationType nullOrQueuedOperationType) {
            XMap<E, QueuedOperationType> delayMap = this.delayMap;
            if (delayMap != null) {
                QueuedOperationType operator = delayMap.get(like);
                if (operator != null) {
                    if (operator == QueuedOperationType.DETACH) {
                        return new Ref<E>();
                    }
                    Entry<E, ?> real = delayMap.entryOfKey(like);
                    if (real != null) {
                        return new Ref<E>(real.getKey());
                    }
                }
            }
            if (!this.isVisionallyReadable(nullOrQueuedOperationType)) {
                return null;
            }
            Ref<E> ref = this.onVisionallyRead(like, nullOrQueuedOperationType);
            if (ref != null && !(ref instanceof DummyReadingResultRef<?>)) {
                this.getLazyBehaviorProcessor().visionallyRead(1);
            }
            return ref;
        }
        
        public ReferenceComparator<? super E> getReferenceComparator() {
            return null;
        }
        
        public final boolean hasQueuedOperations() {
            return this.delayMap != null;
        }
        
        public final boolean performQueuedOperations() {
            if (this.isLoaded()) {
                XMap<E, QueuedOperationType> delayMap = this.delayMap;
                if (delayMap != null) {
                    this.delayMap = null;
                    XSet<E> base = this.getBase();
                    for (Entry<E, QueuedOperationType> entry : delayMap.entrySet()) {
                        if (entry.getValue() == QueuedOperationType.ATTACH) {
                            base.add(entry.getKey());
                        } else {
                            base.remove(entry.getKey());
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        
        public final XMap<E, QueuedOperationType> getQueuedOperations() {
            XMap<E, QueuedOperationType> delayMap = this.delayMap;
            if (delayMap == null) {
                return MACollections.emptyMap();
            }
            return MACollections.unmodifiable(delayMap);
        }

        protected abstract void onLoad();
        
        protected boolean onGetVisionallyReadable(QueuedOperationType nullOrOperationType) {
            return true;
        }
        
        protected Ref<E> onVisionallyRead(E element, QueuedOperationType nullOrQueuedOperationType) {
            return null;
        }

        protected int onGetVisionalSize() {
            return -1;
        }
        
        protected LazyBehaviorProcessor createLazyBehaviorProcessor() {
            return null;
        }
        
        final void visionallyOperate(E element, QueuedOperationType operator) {
            XMap<E, QueuedOperationType> map = this.delayMap;
            if (map == null) {
                this.delayMap = map = this.createDelayMap();
            }
            this.delayMapEventDisabled = true;
            try {
                map.put(element, operator);
            } finally {
                this.delayMapEventDisabled = false;
            }
            if (map.isEmpty()) {
                this.delayMap = null;
            }
        }
        
        /*virtual*/ XMap<E, QueuedOperationType> createDelayMap() {
            UnifiedComparator<? super E> unifiedComparator = this.unifiedComparator();
            Comparator<? super E> comparator = unifiedComparator.comparator();
            if (comparator != null) {
                return new MATreeMap<E, QueuedOperationType>(
                        ReplacementRule.NEW_REFERENCE_WIN, 
                        comparator);
            } 
            return new MALinkedHashMap<E, QueuedOperationType>(
                        ReplacementRule.NEW_REFERENCE_WIN, 
                        unifiedComparator.equalityComparator(),
                        (EqualityComparator<QueuedOperationType>)null,
                        false,
                        OrderAdjustMode.TAIL,
                        OrderAdjustMode.TAIL);
        }
        
        final boolean isDelayMapEventEnabled() {
            return !this.delayMapEventDisabled;
        }
        
        final LazyBehaviorProcessor getLazyBehaviorProcessor() {
            LazyBehaviorProcessor lazyBehaviorProcessor = this.lazyBehaviorProcessor;
            if (lazyBehaviorProcessor == null) {
                lazyBehaviorProcessor = this.createLazyBehaviorProcessor();
                if (lazyBehaviorProcessor == null) {
                    lazyBehaviorProcessor = LazyBehaviorProcessor.unlimited();
                }
                this.lazyBehaviorProcessor = lazyBehaviorProcessor;
            }
            return lazyBehaviorProcessor;
        }
    }
}
