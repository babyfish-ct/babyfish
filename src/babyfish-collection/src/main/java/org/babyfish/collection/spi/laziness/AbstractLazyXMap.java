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
import java.util.Map;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XMap;
import org.babyfish.collection.spi.base.NoEntryException;
import org.babyfish.collection.spi.wrapper.AbstractWrapperXMap;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.collection.viewinfo.MapViewInfos;
import org.babyfish.data.LazinessManageable;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;
import org.babyfish.lang.Ref;

/**
 * @author Tao Chen
 */
public abstract class AbstractLazyXMap<K, V> 
extends AbstractWrapperXMap<K, V> 
implements LazinessManageable {
    
    protected AbstractLazyXMap(XMap<K, V> base) {
        super(base);
    }

    protected AbstractLazyXMap(
            AbstractLazyXMap<K, V> parent,
            ViewInfo viewInfo) {
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
    protected AbstractLazyXMap() {
        
    }

    @Override
    public final boolean isLoaded() {
        return this.<RootData<K, V>>getRootData().isLoaded();
    }

    @Override
    public final boolean isLoadable() {
        return this.<RootData<K, V>>getRootData().isLoadable();
    }

    @Override
    public final void load() {
        this.<RootData<K, V>>getRootData().load();
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
            return "{ lazyMap : NotLoaded }";
        }
        return super.toString();
    }

    @Override
    public int size() {
        this.requiredEnabled();
        if (this.getParent() == null) {
            int size = this.<RootData<K, V>>getRootData().visionallyReadSize();
            if (size != -1) {
                return size;
            }
        }
        this.load();
        return this.getBase().size();
    }

    @Override
    public boolean isEmpty() {
        this.requiredEnabled();
        if (0 == this.<RootData<K, V>>getRootData().visionallyReadSize()) {
            return true;
        }
        this.load();
        return this.getBase().isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        this.requiredEnabled();
        RootData<K, V> rootData = this.<RootData<K, V>>getRootData();
        Ref<V> ref = rootData.visionallyRead((K)key, null);
        if (ref != null) {
            return ref.get();
        }
        rootData.load();
        return this.getBase().get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key) {
        this.requiredEnabled();
        RootData<K, V> rootData = this.<RootData<K, V>>getRootData();
        Ref<V> ref = rootData.visionallyRead((K)key, null);
        if (ref != null) {
            return ref.get() != null;
        }
        rootData.load();
        return this.getBase().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        this.requiredEnabled();
        if (this.<RootData<K, V>>getRootData().getQueuedOrphans().containsValue(value)) {
            return false;
        }
        this.load();
        return this.getBase().containsValue(value);
    }

    @Override
    public XEntry<K, V> entryOfValue(V value) {
        this.requiredEnabled();
        this.load();
        return this.getBase().entryOfValue(value);
    }

    @Override
    public XEntry<K, V> entryOfKey(K key) {
        try {
            return new RealEntryImpl<>(this, key);
        } catch (NoEntryException ex) {
            return null;
        }
    }

    @Override
    public V put(K key, V value) {
        this.enable();
        this.load();
        return this.getBase().put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.enable();
        this.load();
        this.getBase().putAll(m);
    }

    @Override
    public void clear() {
        this.enable();
        this.load();
        this.getBase().clear();
    }

    @Override
    public V remove(Object key) {
        this.enable();
        Ref<V> retval = this.visionallyRemove(key);
        if (retval != null) {
            return retval.get();
        }
        this.load();
        return this.getBase().remove(key);
    }
    
    @Override
    public XEntrySetView<K, V> entrySet() {
        return this.getEntrySet();
    }

    @Override
    public XKeySetView<K> keySet() {
        return this.getKeySet();
    }

    @Override
    public XValuesView<V> values() {
        return this.getValues();
    }

    @Override
    protected XEntrySetView<K, V> createEntrySet() {
        return new EntrySetImpl<K, V>(this);
    }

    @Override
    protected XKeySetView<K> createKeySet() {
        return new KeySetImpl<K, V>(this);
    }

    @Override
    protected XValuesView<V> createValues() {
        return new ValuesImpl<K, V>(this);
    }

    protected final Ref<V> visionallyRemove(Object key) {
        return this.visionallyRemoveImpl(key);
    }
    
    @SuppressWarnings("unchecked")
    Ref<V> visionallyRemoveImpl(Object key) {
        RootData<K, V> rootData = this.getRootData();
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
        Ref<V> ref = rootData.visionallyRead((K)key, QueuedOperationType.DETACH);
        if (ref != null) {
            rootData.visinallyRemove((K)key, ref.get());
        }
        return ref;
    }

    @Override
    protected abstract RootData<K, V> createRootData();
    
    protected static abstract class AbstractKeySetImpl<K, V> 
    extends AbstractWrapperXMap.AbstractKeySetImpl<K, V>
    implements LazinessManageable {

        protected AbstractKeySetImpl(
                AbstractLazyXMap<K, V> parentMap,
                ViewInfo viewInfo) {
            super(parentMap, viewInfo);
        }

        protected AbstractKeySetImpl(
                AbstractKeySetImpl<K, V> parent,
                ViewInfo viewInfo) {
            super(parent, viewInfo);
        }
        
        @Override
        public final boolean isLoaded() {
            return this.<RootData<K, V>>getRootData().isLoaded();
        }

        @Override
        public final boolean isLoadable() {
            return this.<RootData<K, V>>getRootData().isLoadable();
        }

        @Override
        public final void load() {
            this.<RootData<K, V>>getRootData().load();
        }

        @Override
        public int hashCode() {
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().equals(obj);
        }

        @Override
        public String toString() {
            if (!this.<RootData<K, V>>getRootData().isLoaded()) {
                return "[lazyKeySet : NotLoaded]";
            }
            return this.getBase().toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(Object o) {
            this.requiredEnabled();
            RootData<K, V> rootData = this.<RootData<K, V>>getRootData();
            Ref<V> ref = rootData.visionallyRead((K)o, null);
            if (ref != null) {
                return ref.get() != null;
            }
            rootData.load();
            return this.getBase().contains(o);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean containsAll(Collection<?> c) {
            this.requiredEnabled();
            RootData<K, V> rootData = this.<RootData<K, V>>getRootData();
            for (Object o : c) {
                Ref<V> ref = rootData.visionallyRead((K)o, null);
                if (ref == null) {
                    rootData.load();
                    return this.getBase().containsAll(c);
                }
                if (ref.get() == null) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Object[] toArray() {
            this.requiredEnabled();
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            this.requiredEnabled();
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().toArray(a);
        }

        @Override
        public boolean isEmpty() {
            this.requiredEnabled();
            RootData<K, V> rootData = this.<RootData<K, V>>getRootData();
            if (0 == rootData.visionallyReadSize()) {
                return true;
            } 
            rootData.load();
            return this.getBase().isEmpty();
        }

        @Override
        public int size() {
            this.requiredEnabled();
            RootData<K, V> rootData = this.<RootData<K, V>>getRootData();
            if (this.getParent() == null && this.<AbstractLazyXMap<K, V>>getParentMap().getParent() == null) {
                int size = rootData.visionallyReadSize();
                if (size != -1) {
                    return size;
                }
            }
            rootData.load();
            return this.getBase().size();
        }

        @Override
        public void clear() {
            this.enable();
            this.<RootData<K, V>>getRootData().load();
            this.getBase().clear();
        }

        @Override
        public boolean remove(Object o) {
            this.enable();
            Boolean retval = this.visionallyRemove(o);
            if (retval != null) {
                return retval.booleanValue();
            }
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().remove(o);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            this.enable();
            Boolean retval = this.visionallyRemoveAll(c);
            if (retval != null) {
                return retval.booleanValue();
            }
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            this.enable();
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().retainAll(c);
        }
        
        @Override
        public XIterator<K> iterator() {
            return new IteratorImpl<K, V>(this);
        }
        
        protected final Boolean visionallyRemove(Object o) {
            return this.visionallyRemoveImpl(o);
        }
        
        protected final Boolean visionallyRemoveAll(Collection<?> c) {
            return this.visionallyRemoveAllImpl(c);
        }
        
        @SuppressWarnings("unchecked")
        Boolean visionallyRemoveImpl(Object o) {
            RootData<K, V> rootData = this.<RootData<K, V>>getRootData();
            if (rootData.isLoaded()) {
                return null;
            }
            if (!rootData.isVisionallyReadable(QueuedOperationType.DETACH)) {
                return null;
            }
            if (rootData.isLoading()) {
                throw new IllegalStateException(CommonMessages.visionOperationWhenDataIsBeingLoaded());
            }
            Ref<V> ref = rootData.visionallyRead((K)o, QueuedOperationType.DETACH);
            if (ref != null) {
                if (ref.get() == null) {
                    return false;
                }
                rootData.visinallyRemove((K)o, ref.get());
                return true;
            }
            return null;
        }
        
        @SuppressWarnings("unchecked")
        Boolean visionallyRemoveAllImpl(Collection<?> c) {
            RootData<K, V> rootData = this.<RootData<K, V>>getRootData();
            if (rootData.isLoaded()) {
                return null;
            }
            if (!rootData.isVisionallyReadable(QueuedOperationType.DETACH)) {
                return null;
            }
            if (rootData.isLoading()) {
                throw new IllegalStateException(CommonMessages.visionOperationWhenDataIsBeingLoaded());
            }
            int len = 0;
            Object[] arr = new Object[c.size() << 1];
            for (Object o : c) {
                Ref<V> ref = rootData.visionallyRead((K)o, QueuedOperationType.DETACH);
                if (ref == null) {
                    return null;
                }
                if (ref.get() == null) {
                    continue;
                }
                arr[len++] = o;
                arr[len++] = ref.get();
            }
            for (int i = 0; i < len; i += 2) {
                rootData.visinallyRemove((K)arr[i], (V)arr[i + 1]);
            }
            return len != 0;
        }
        
        protected static abstract class AbstractIteratorImpl<K, V>
        extends AbstractWrapperXMap.AbstractKeySetImpl.AbstractIteratorImpl<K, V> {

            protected AbstractIteratorImpl(
                    AbstractKeySetImpl<K, V> parent,
                    ViewInfo viewInfo) {
                super(loadParent(parent), viewInfo);
            }
            
            private static <K, V> AbstractKeySetImpl<K, V> loadParent(
                    AbstractKeySetImpl<K, V> parent) {
                parent.<RootData<K, V>>getRootData().load();
                return parent;
            }
            
        }
        
        protected static class IteratorImpl<K, V> extends AbstractIteratorImpl<K, V> {
            
            public IteratorImpl(AbstractKeySetImpl<K, V> parent) {
                super(parent, CollectionViewInfos.iterator());
            }

            @Override
            protected XIterator<K> createBaseView(
                    XKeySetView<K> baseKeySet,
                    ViewInfo viewInfo) {
                if (viewInfo instanceof CollectionViewInfos.Iterator) {
                    return baseKeySet.iterator();
                }
                throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
            }
        }
    }
    
    protected static class KeySetImpl<K, V> extends AbstractKeySetImpl<K, V> {

        protected KeySetImpl(AbstractLazyXMap<K, V> parentMap) {
            super(parentMap, MapViewInfos.keySet());
        }

        @Override
        protected XKeySetView<K> createBaseView(
                XMap<K, V> baseMap, ViewInfo viewInfo) {
            if (viewInfo instanceof MapViewInfos.KeySet) {
                return baseMap.keySet();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
    }
    
    protected static class EntrySetImpl<K, V> 
    extends AbstractWrapperXMap.EntrySetImpl<K, V> 
    implements LazinessManageable {

        public EntrySetImpl(AbstractLazyXMap<K, V> parentMap) {
            super(parentMap);
        }
        
        @Override
        public final boolean isLoaded() {
            return this.<RootData<K, V>>getRootData().isLoaded();
        }

        @Override
        public final boolean isLoadable() {
            return this.<RootData<K, V>>getRootData().isLoadable();
        }

        @Override
        public final void load() {
            this.<RootData<K, V>>getRootData().load();
        }

        @Override
        public int hashCode() {
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().equals(obj);
        }

        @Override
        public String toString() {
            if (!this.<RootData<K, V>>getRootData().isLoaded()) {
                return "{ lazyKeySet : NotLoaded }";
            }
            return this.getBase().toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(Object o) {
            this.requiredEnabled();
            if (!(o instanceof Entry<?, ?>)) {
                return false;
            }
            Entry<K, V> e = (Entry<K, V>)o;
            RootData<K, V> rootData = this.getRootData();
            Ref<V> ref = rootData.visionallyRead(e.getKey(), null);
            if (ref != null) {
                if (ref.get() == null) {
                    return false;
                }
                if (!rootData.valueUnifiedComparator().equals(ref.get(), e.getValue())) {
                    return false;
                }
                return true;
            }
            rootData.load();
            return this.getBase().contains(o);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean containsAll(Collection<?> c) {
            this.requiredEnabled();
            RootData<K, V> rootData = this.getRootData();
            UnifiedComparator<? super V> valueUnifiedComparator = rootData.valueUnifiedComparator();
            for (Object o : c) {
                if (!(o instanceof Entry<?, ?>)) {
                    return false;
                }
                Entry<K, V> e = (Entry<K, V>)o;
                Ref<V> ref = rootData.visionallyRead(e.getKey(), null);
                if (ref == null) {
                    rootData.load();
                    return this.getBase().containsAll(c);
                }
                if (ref.get() == null) {
                    return false;
                }
                if (!valueUnifiedComparator.equals(ref.get(), e.getValue())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Object[] toArray() {
            this.requiredEnabled();
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            this.requiredEnabled();
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().toArray(a);
        }

        @Override
        public boolean isEmpty() {
            this.requiredEnabled();
            RootData<K, V> rootData = this.<RootData<K, V>>getRootData();
            if (0 == rootData.visionallyReadSize()) {
                return true;
            }
            rootData.load();
            return this.getBase().isEmpty();
        }

        @Override
        public int size() {
            this.requiredEnabled();
            RootData<K, V> rootData = this.<RootData<K, V>>getRootData();
            if (this.<AbstractLazyXMap<K, V>>getParentMap().getParent() != null) {
                int size = rootData.visionallyReadSize();
                if (size != -1) {
                    return size;
                }
            }
            rootData.load();
            return this.getBase().size();
        }

        @Override
        public void clear() {
            this.enable();
            this.<RootData<K, V>>getRootData().load();
            this.getBase().clear();
        }

        @Override
        public boolean remove(Object o) {
            this.enable();
            Boolean retval = this.visionallyRemove(o);
            if (retval != null) {
                return retval.booleanValue();
            }
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().remove(o);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            this.enable();
            Boolean retval = this.visionallyRemoveAll(c);
            if (retval != null) {
                return retval.booleanValue();
            }
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            this.enable();
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().retainAll(c);
        }

        @Override
        public XEntrySetIterator<K, V> iterator() {
            return new IteratorImpl<K, V>(this);
        }
        
        protected final Boolean visionallyRemove(Object o) {
            return this.visionallRemoveImpl(o);
        }
        
        protected final Boolean visionallyRemoveAll(Collection<?> c) {
            return this.visionallyRemoveAllImpl(c);
        }
        
        @SuppressWarnings("unchecked")
        Boolean visionallRemoveImpl(Object o) {
            if (!(o instanceof Entry<?, ?>)) {
                return false;
            }
            RootData<K, V> rootData = this.<RootData<K, V>>getRootData();
            if (rootData.isLoaded()) {
                return null;
            }
            if (!rootData.isVisionallyReadable(QueuedOperationType.DETACH)) {
                return null;
            }
            if (rootData.isLoading()) {
                throw new IllegalStateException(CommonMessages.visionOperationWhenDataIsBeingLoaded());
            }
            Entry<K, V> e = (Entry<K, V>)o;
            Ref<V> ref = rootData.visionallyRead(e.getKey(), null);
            if (ref != null) {
                if (ref.get() == null) {
                    return false;
                }
                if (!rootData.valueUnifiedComparator().equals(ref.get(), e.getValue())) {
                    return false;
                }
                rootData.visinallyRemove(e.getKey(), ref.get());
                return true;
            }
            return null;
        }
        
        @SuppressWarnings("unchecked")
        Boolean visionallyRemoveAllImpl(Collection<?> c) {
            RootData<K, V> rootData = this.<RootData<K, V>>getRootData();
            if (rootData.isLoaded()) {
                return null;
            }
            if (!rootData.isVisionallyReadable(QueuedOperationType.DETACH)) {
                return null;
            }
            if (rootData.isLoading()) {
                throw new IllegalStateException(CommonMessages.visionOperationWhenDataIsBeingLoaded());
            }
            int len = 0;
            Object[] arr = new Object[c.size() << 1];
            UnifiedComparator<? super V> valueUnifiedComparator = rootData.valueUnifiedComparator();
            for (Object o : c) {
                if (o instanceof Entry<?, ?>) {
                    Entry<K, V> e = (Entry<K, V>)o;
                    Ref<V> ref = rootData.visionallyRead(e.getKey(), QueuedOperationType.DETACH);
                    if (ref == null) {
                        return null;
                    }
                    if (ref.get() == null) {
                        continue;
                    }
                    if (!valueUnifiedComparator.equals(ref.get(), e.getValue())) {
                        continue;
                    }
                    arr[len++] = e.getKey();
                    arr[len++] = ref.get();
                }
            }
            for (int i = 0; i < len; i += 2) {
                rootData.visinallyRemove((K)arr[i], (V)arr[i + 1]);
            }
            return len != 0;
        }
        
        protected static class IteratorImpl<K, V> 
        extends AbstractWrapperXMap.EntrySetImpl.IteratorImpl<K, V> {

            public IteratorImpl(
                    EntrySetImpl<K, V> parent) {
                super(loadParent(parent));
            }
            
            private static <K, V> EntrySetImpl<K, V> loadParent(EntrySetImpl<K, V> parent) {
                parent.<RootData<K, V>>getRootData().load();
                return parent;
            }

            @Override
            public XEntry<K, V> next() {
                return new EntryImpl<>(this);
            }
        }
    }

    protected static class ValuesImpl<K, V> 
    extends AbstractWrapperXMap.ValuesImpl<K, V>
    implements LazinessManageable {
    
        public ValuesImpl(AbstractLazyXMap<K, V> parentMap) {
            super(parentMap);
        }
        
        @Override
        public final boolean isLoaded() {
            return this.<RootData<K, V>>getRootData().isLoaded();
        }

        @Override
        public final boolean isLoadable() {
            return this.<RootData<K, V>>getRootData().isLoadable();
        }

        @Override
        public final void load() {
            this.<RootData<K, V>>getRootData().load();
        }

        @Override
        public String toString() {
            if (!this.<RootData<K, V>>getRootData().isLoaded()) {
                return "{ lazyValues : NotLoaded }";
            }
            return this.getBase().toString();
        }

        @Override
        public boolean contains(Object o) {
            this.requiredEnabled();
            if (this.<RootData<K, V>>getRootData().getQueuedOrphans().containsValue(o)) {
                return false;
            }
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().contains(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            this.requiredEnabled();
            if (this.<RootData<K, V>>getRootData().getQueuedOrphans().values().containsAll(c)) {
                return false;
            }
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().containsAll(c);
        }

        @Override
        public boolean isEmpty() {
            this.requiredEnabled();
            RootData<K, V> rootData = this.getRootData();
            if (0 == rootData.visionallyReadSize()) {
                return true;
            }
            rootData.load();
            return this.getBase().isEmpty();
        }

        @Override
        public int size() {
            this.requiredEnabled();
            RootData<K, V> rootData = this.getRootData();
            if (this.<AbstractLazyXMap<K, V>>getParentMap().getParent() == null) {
                int size = rootData.visionallyReadSize();
                if (size != -1) {
                    return size;
                }
            }
            rootData.load();
            return this.getBase().size();
        }

        @Override
        public Object[] toArray() {
            this.requiredEnabled();
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            this.requiredEnabled();
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().toArray(a);
        }

        @Override
        public void clear() {
            this.enable();
            this.<RootData<K, V>>getRootData().load();
            this.getBase().clear();
        }

        @Override
        public boolean remove(Object o) {
            this.enable();
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().remove(o);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            this.enable();
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            this.enable();
            this.<RootData<K, V>>getRootData().load();
            return this.getBase().retainAll(c);
        }

        @Override
        public XIterator<V> iterator() {
            return new IteratorImpl<K, V>(this);
        }
        
        protected static class IteratorImpl<K, V> extends AbstractWrapperXMap.ValuesImpl.IteratorImpl<K, V> {

            public IteratorImpl(ValuesImpl<K, V> parent) {
                super(loadParent(parent));
            }
            
            private static <K, V> ValuesImpl<K, V> loadParent(ValuesImpl<K, V> parent) {
                parent.<RootData<K, V>>getRootData().load();
                return parent;
            }
            
        }
    }
    
    protected static abstract class AbstractEntryImpl<K, V> extends AbstractWrapperXMap.AbstractEntryImpl<K, V> {

        public AbstractEntryImpl(AbstractLazyXMap<K, V> parentMap, ViewInfo viewInfo) throws NoEntryException {
            super(loadedParentMap(parentMap), viewInfo);
        }

        public AbstractEntryImpl(AbstractLazyXMap.EntrySetImpl.IteratorImpl<K, V> iterator) {
            super(iterator);
        }
        
        private static <K, V> AbstractLazyXMap<K, V> loadedParentMap(AbstractLazyXMap<K, V> parentMap) {
            parentMap.load();
            return parentMap;
        }
    }
    
    protected static class EntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        public EntryImpl(AbstractLazyXMap.EntrySetImpl.IteratorImpl<K, V> iterator) {
            super(iterator);
        }
    }
    
    protected static class RealEntryImpl<K, V> extends AbstractEntryImpl<K, V> {
        
        protected RealEntryImpl(AbstractLazyXMap<K, V> parentMap, K key) throws NoEntryException {
            super(parentMap, MapViewInfos.entryOfKey(key));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected org.babyfish.collection.XMap.XEntry<K, V> createBaseView(XMap<K, V> baseMap, ViewInfo viewInfo) {
            if (viewInfo instanceof MapViewInfos.EntryOfKey) {
                K key = (K)((MapViewInfos.EntryOfKey)viewInfo).getKey();
                return baseMap.entryOfKey(key);
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
    }

    protected static abstract class RootData<K, V> extends AbstractWrapperXMap.RootData<K, V> {
        
        private static final long serialVersionUID = -2206856734988661149L;
        
        private XMap<K, V> delayOrphanMap;
        
        private LazyBehaviorProcessor lazyBehaviorProcessor;
        
        public RootData() {
            
        }
        
        public abstract boolean isLoaded();
        
        public abstract boolean isLoading();
        
        public abstract boolean isLoadable();
        
        protected abstract void setLoaded(boolean loaded);
        
        protected abstract void setLoading(boolean loading);
        
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
                    this.performQueuedOrphans();
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
            return !this.isLoaded() && this.onGetVisionallyReadable(nullOrOperationType);
        }

        public final boolean hasQueuedOrphans() {
            return this.delayOrphanMap != null;
        }
        
        public final XMap<K, V> getQueuedOrphans() {
            XMap<K, V> map = this.delayOrphanMap;
            if (map == null) {
                return MACollections.emptyMap();
            }
            return MACollections.unmodifiable(map);
        }

        public final boolean performQueuedOrphans() {
            if (this.isLoaded()) {
                XMap<K, V> map = this.delayOrphanMap;
                if (map != null) {
                    this.delayOrphanMap = null;
                    this.getBase().keySet().removeAll(map.keySet());
                    return true;
                }
            }
            return false;
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
        
        public final Ref<V> visionallyRead(K key, QueuedOperationType nullOrOperationType) {
            XMap<K, V> delayOrphanMap = this.delayOrphanMap;
            if (delayOrphanMap != null && delayOrphanMap.containsKey(key)) {
                return new Ref<V>(null);
            }
            if (!this.isVisionallyReadable(nullOrOperationType)) {
                return null;
            }
            return this.onVisionallyRead(key, nullOrOperationType);
        }
        
        protected abstract void onLoad();

        protected boolean onGetVisionallyReadable(QueuedOperationType nullOrOperationType) {
            return true;
        }
        
        protected int onGetVisionalSize() {
            return -1;
        }
        
        protected Ref<V> onVisionallyRead(K key, QueuedOperationType nullOrOperationType) {
            return null;
        }
        
        protected LazyBehaviorProcessor createLazyBehaviorProcessor() {
            return null;
        }
        
        final void visinallyRemove(K key, V value) {
            XMap<K, V> delayOrphanMap = this.delayOrphanMap;
            if (delayOrphanMap == null) {
                UnifiedComparator<? super K> keyUnifiedComparator = this.keyUnifiedComparator();
                if (keyUnifiedComparator.comparator() != null) {
                    delayOrphanMap = new TreeMap<K, V>(
                            ReplacementRule.NEW_REFERENCE_WIN,
                            keyUnifiedComparator.comparator(),
                            this.valueUnifiedComparator());
                } else {
                    delayOrphanMap = new LinkedHashMap<K, V>(
                            ReplacementRule.NEW_REFERENCE_WIN,
                            keyUnifiedComparator.equalityComparator(),
                            this.valueUnifiedComparator(),
                            false,
                            OrderAdjustMode.TAIL,
                            OrderAdjustMode.TAIL);
                }
                this.delayOrphanMap = delayOrphanMap;
            }
            delayOrphanMap.put(key, value);
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
