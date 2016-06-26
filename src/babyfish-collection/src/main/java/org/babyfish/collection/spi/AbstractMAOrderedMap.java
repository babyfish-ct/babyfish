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
package org.babyfish.collection.spi;

import java.util.Collection;

import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.KeySetElementEvent;
import org.babyfish.collection.event.KeySetElementListener;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementEvent.MapModification;
import org.babyfish.collection.event.modification.OrderedMapModifications;
import org.babyfish.collection.spi.base.BaseEntriesHandler;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.collection.spi.base.NoEntryException;
import org.babyfish.collection.spi.base.OrderedBaseEntries;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.collection.viewinfo.OrderedMapViewInfos;
import org.babyfish.collection.viewinfo.OrderedMapViewInfos.DescendingKeySet;
import org.babyfish.collection.viewinfo.OrderedMapViewInfos.DescendingMap;
import org.babyfish.collection.viewinfo.OrderedMapViewInfos.OrderedKeySet;
import org.babyfish.collection.viewinfo.OrderedSetViewInfos;
import org.babyfish.collection.viewinfo.OrderedSetViewInfos.DescendingIterator;
import org.babyfish.data.View;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.Cause;
import org.babyfish.data.event.spi.GlobalAttributeContext;
import org.babyfish.data.event.spi.InAllChainAttributeContext;
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public abstract class AbstractMAOrderedMap<K, V> 
extends AbstractMAMap<K, V> 
implements MAOrderedMap<K, V> {

    private static final Object AK_KEY_SET_ELEMENT_LISTENER = new Object();
    
    protected AbstractMAOrderedMap(OrderedBaseEntries<K, V> orderedBaseEntries) {
        super(orderedBaseEntries);
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
    protected AbstractMAOrderedMap() {
        
    }

    @Override
    public boolean headAppend() {
        return ((OrderedBaseEntries<K, V>)this.baseEntries).headAppend();
    }

    @Override
    public OrderAdjustMode replaceMode() {
        return ((OrderedBaseEntries<K, V>)this.baseEntries).replaceMode();
    }

    @Override
    public OrderAdjustMode accessMode() {
        return ((OrderedBaseEntries<K, V>)this.baseEntries).accessMode();
    }
    
    @Override
    public MAOrderedKeySetView<K, V> keySet() {
        return this.getKeySet();
    }
    
    @Override
    public MAOrderedKeySetView<K, V> descendingKeySet() {
        return new DescendingKeySetImpl<K, V>(this);
    }

    @Override
    public MAOrderedMapView<K, V> descendingMap() {
        return new DescendingMapImpl<K, V>(this);
    }
    
    @Override
    public MAEntry<K, V> firstEntry() {
        try {
            return new FirstEntryImpl<K, V>(this);
        } catch (NoEntryException ex) {
            return null;
        }
    }

    @Override
    public MAEntry<K, V> lastEntry() {
        try {
            return new LastEntryImpl<K, V>(this);
        } catch (NoEntryException ex) {
            return null;
        }
    }

    @Override
    public K firstKey() {
        BaseEntry<K, V> be = ((OrderedBaseEntries<K, V>)this.baseEntries).first();
        return be == null ? null : be.getKey();
    }

    @Override
    public K lastKey() {
        BaseEntry<K, V> be = ((OrderedBaseEntries<K, V>)this.baseEntries).last();
        return be == null ? null : be.getKey();
    }

    @Override
    public V access(K key) {
        BaseEntry<K, V> be=
                ((OrderedBaseEntries<K, V>)this.baseEntries).access(
                        key,
                        this.new HandlerImpl4OrderedMap(OrderedMapModifications.<K, V>access(key)));
        if (be != null) {
            return be.getValue();
        }
        return null;
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        // The returned entry is dead, need not wrap it.
        return ((OrderedBaseEntries<K, V>)this.baseEntries).pollFirst(
                this.new HandlerImpl4OrderedMap(OrderedMapModifications.<K, V>pollFirstEntry()));
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        // The returned entry is dead, need not wrap it.
        return ((OrderedBaseEntries<K, V>)this.baseEntries).pollLast(
                this.new HandlerImpl4OrderedMap(OrderedMapModifications.<K, V>pollLastEntry()));
    }

    @Override
    protected MAOrderedKeySetView<K, V> createKeySet() {
        return new KeySetImpl<K, V>(this);
    }

    protected static class DescendingMapImpl<K, V> 
    extends AbstractMAOrderedMap<K, V> 
    implements MAOrderedMapView<K, V> {
        
        private AbstractMAOrderedMap<K, V> parentMap;
        
        protected DescendingMapImpl(AbstractMAOrderedMap<K, V> parentMap) {
            super(
                    ((OrderedBaseEntries<K, V>)Arguments.mustNotBeNull("parentMap", parentMap).baseEntries)
                    .descendingEntries());
            this.parentMap = parentMap;
        }
        
        @Override
        public DescendingMap viewInfo() {
            return OrderedMapViewInfos.descendingMap();
        }

        @Override
        protected void bubbleModifying(MapElementEvent<K, V> e) {
            AbstractMAOrderedMap<K, V> parentMap = this.parentMap;
            MapElementEvent<K, V> bubbledEvent = new MapElementEvent<>(
                    parentMap, 
                    new Cause(e), 
                    null, 
                    null);
            parentMap.executeModifying(bubbledEvent);
        }

        @Override
        protected void bubbleModified(MapElementEvent<K, V> e) {
            AbstractMAOrderedMap<K, V> parentMap = this.parentMap;
            MapElementEvent<K, V> bubbledEvent = e.getBubbledEvent(parentMap);
            parentMap.executeModified(bubbledEvent);
        }
    }
    
    protected static abstract class AbstractOrderedKeySetImpl<K, V> 
    extends AbstractMAOrderedSet<K> 
    implements MAOrderedKeySetView<K, V> {
        
        private AbstractMAOrderedMap<K, V> parentMap;
        
        private transient KeySetElementListener<K, V> keySetElementListener;

        AbstractOrderedKeySetImpl(
                AbstractMAOrderedMap<K, V> parentMap,
                OrderedBaseEntries<K, Object> orderedBaseEntries) {
            super(orderedBaseEntries);
            this.parentMap = parentMap;
        }
        
        AbstractOrderedKeySetImpl(OrderedBaseEntries<K, Object> orderedBaseEntries) {
            super(orderedBaseEntries);
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractMAOrderedMap<K, V>> T getParentMap() {
            return (T)this.parentMap;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void addKeySetElementListener(
                KeySetElementListener<? super K, ? super V> listener) {
            this.keySetElementListener = KeySetElementListener.combine(
                        this.keySetElementListener, 
                        (KeySetElementListener<K, V>)listener);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removeKeySetElementListener(
                KeySetElementListener<? super K, ? super V> listener) {
            this.keySetElementListener = KeySetElementListener.remove(
                        this.keySetElementListener, 
                        (KeySetElementListener<K, V>)listener);
        }

        @Deprecated
        @Override
        public final boolean add(K e) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final boolean addAll(Collection<? extends K> c) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
        
        @SuppressWarnings("unchecked")
        @Deprecated
        @Override
        protected final void executeModifying(ElementEvent<K> e) {
            this.executeModifying((KeySetElementEvent<K, V>)e);
        }
        
        @SuppressWarnings("unchecked")
        @Deprecated
        @Override
        protected final void executeModified(ElementEvent<K> e) {
            this.executeModified((KeySetElementEvent<K, V>)e);
        }
        
        protected void executeModifying(KeySetElementEvent<K, V> e) {
            super.executeModifying(e);
        }
        
        protected void executeModified(KeySetElementEvent<K, V> e) {
            super.executeModified(e);
        }

        @Deprecated
        @SuppressWarnings("unchecked")
        @Override
        protected final void onModifying(ElementEvent<K> e) throws Throwable {
            this.onModifying((KeySetElementEvent<K, V>)e);
        }

        @Deprecated
        @SuppressWarnings("unchecked")
        @Override
        protected final void onModified(ElementEvent<K> e) throws Throwable {
            this.onModified((KeySetElementEvent<K, V>)e);
        }
        
        protected void onModifying(KeySetElementEvent<K, V> e) throws Throwable {
            
        }

        protected void onModified(KeySetElementEvent<K, V> e) throws Throwable {
            
        }
        
        @SuppressWarnings("unchecked")
        @Override
        @Deprecated
        protected final void raiseModifying(ElementEvent<K> e) throws Throwable {
            this.raiseModifying((KeySetElementEvent<K, V>)e);
        }

        @SuppressWarnings("unchecked")
        @Override
        @Deprecated
        protected final void raiseModified(ElementEvent<K> e) throws Throwable {
            this.raiseModified((KeySetElementEvent<K, V>)e);
        }
        
        protected void raiseModifying(KeySetElementEvent<K, V> e) throws Throwable {
            Throwable finalThrowable = null;
            try {
                super.raiseModifying(e);
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                KeySetElementListener<K, V> keySetElementListener = this.keySetElementListener;
                if (keySetElementListener != null) {
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .addAttribute(AK_KEY_SET_ELEMENT_LISTENER, keySetElementListener);
                    keySetElementListener.modifying(e);
                }
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            if (finalThrowable != null) {
                throw finalThrowable;
            }
        }
        
        @SuppressWarnings("unchecked")
        protected void raiseModified(KeySetElementEvent<K, V> e) throws Throwable {
            Throwable finalThrowable = null;
            try {
                KeySetElementListener<K, V> keySetElementListener = 
                    (KeySetElementListener<K, V>)
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .removeAttribute(AK_KEY_SET_ELEMENT_LISTENER);
                if (keySetElementListener != null) {
                    keySetElementListener.modified(e);
                }
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                super.raiseModified(e);
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            if (finalThrowable != null) {
                throw finalThrowable;
            }
        }
        
        @SuppressWarnings("unchecked")
        @Override
        @Deprecated
        protected final void bubbleModifying(ElementEvent<K> e) {
            this.bubbleModifying((KeySetElementEvent<K, V>)e);
        }

        @SuppressWarnings("unchecked")
        @Override
        @Deprecated
        protected final void bubbleModified(ElementEvent<K> e) {
            this.bubbleModified((KeySetElementEvent<K, V>)e);
        }
        
        protected abstract void bubbleModifying(final KeySetElementEvent<K, V> e);
        
        protected abstract void bubbleModified(KeySetElementEvent<K, V> e);

        @Override
        public MAOrderedKeySetView<K, V> descendingSet() {
            return new DescendingSetImpl<K, V>(this);
        }
        
        @Override
        public MAKeySetIterator<K, V> iterator() {
            return new IteratorImpl<K, V>(this);
        }
        
        @Override
        public MAKeySetIterator<K, V> descendingIterator() {
            return new DescendingIteratorImpl<K, V>(this);
        }
        
        protected static class DescendingSetImpl<K, V> extends AbstractOrderedKeySetImpl<K, V> {

            private AbstractOrderedKeySetImpl<K, V> parentSet;
            
            protected DescendingSetImpl(AbstractOrderedKeySetImpl<K, V> parentSet) {
                super(((OrderedBaseEntries<K, Object>)parentSet.baseEntries).descendingEntries());
                this.parentSet = parentSet;
            }
            
            @SuppressWarnings("unchecked")
            protected final <T extends AbstractOrderedKeySetImpl<K, V>> T getParentSet() {
                return (T)this.parentSet;
            }
            
            @Override
            protected void bubbleModifying(final KeySetElementEvent<K, V> e) {
                AbstractOrderedKeySetImpl<K, V> parentSet = this.parentSet;
                KeySetElementEvent<K, V> event = new KeySetElementEvent<>(
                        parentSet, 
                        new Cause(e), 
                        null,
                        null);
                parentSet.executeModifying(event);
            }

            @Override
            protected void bubbleModified(KeySetElementEvent<K, V> e) {
                AbstractOrderedKeySetImpl<K, V> parentSet = this.parentSet;
                KeySetElementEvent<K, V> bubbleEvent = e.getBubbledEvent(parentSet);
                parentSet.executeModified(bubbleEvent);
            }

            @Override
            public DescendingIterator viewInfo() {
                return OrderedSetViewInfos.descendingIterator();
            }
        }
        
        protected static abstract class AbstractIteratorImpl<K, V> 
        extends AbstractMASet.AbstractIteratorImpl<K> 
        implements MAKeySetIterator<K, V> {
            
            private transient KeySetElementListener<K, V> keySetElementListener;

            AbstractIteratorImpl(
                    AbstractOrderedKeySetImpl<K, V> parentSet, 
                    boolean descending) {
                super(parentSet, descending);
            }
            
            @SuppressWarnings("unchecked")
            @Deprecated
            @Override
            protected final void executeModifying(ElementEvent<K> e) {
                this.executeModifying((KeySetElementEvent<K, V>)e);
            }
            
            @SuppressWarnings("unchecked")
            @Deprecated
            @Override
            protected final void executeModified(ElementEvent<K> e) {
                this.executeModified((KeySetElementEvent<K, V>)e);
            }
            
            protected void executeModifying(KeySetElementEvent<K, V> e) {
                super.executeModifying(e);
            }
            
            protected void executeModified(KeySetElementEvent<K, V> e) {
                super.executeModified(e);
            }

            @SuppressWarnings("unchecked")
            @Deprecated
            @Override
            protected final void onModifying(ElementEvent<K> e) throws Throwable {
                this.onModifying((KeySetElementEvent<K, V>)e);
            }
            
            @SuppressWarnings("unchecked")
            @Deprecated
            @Override
            protected final void onModified(ElementEvent<K> e) throws Throwable {
                this.onModified((KeySetElementEvent<K, V>)e);
            }
            
            protected void onModifying(KeySetElementEvent<K, V> e) throws Throwable {
                
            }

            protected void onModified(KeySetElementEvent<K, V> e) throws Throwable {
                
            }

            @SuppressWarnings("unchecked")
            @Override
            @Deprecated
            protected final void raiseModifying(ElementEvent<K> e) throws Throwable {
                this.raiseModifying((KeySetElementEvent<K, V>)e);
            }

            @SuppressWarnings("unchecked")
            @Override
            @Deprecated
            protected final void raiseModified(ElementEvent<K> e) throws Throwable {
                this.raiseModified((KeySetElementEvent<K, V>)e);
            }
            
            protected void raiseModifying(KeySetElementEvent<K, V> e) throws Throwable {
                Throwable finalThrowable = null;
                try {
                    super.raiseModifying(e);
                } catch (Throwable ex) {
                    finalThrowable = ex;
                }
                try {
                    KeySetElementListener<K, V> keySetElementListener = this.keySetElementListener;
                    if (keySetElementListener != null) {
                        e
                        .getAttributeContext(AttributeScope.LOCAL)
                        .addAttribute(AK_KEY_SET_ELEMENT_LISTENER, keySetElementListener);
                        keySetElementListener.modifying(e);
                    }
                } catch (Throwable ex) {
                    if (finalThrowable == null) {
                        finalThrowable = ex;
                    }
                }
                if (finalThrowable != null) {
                    throw finalThrowable;
                }
            }
            
            @SuppressWarnings("unchecked")
            protected void raiseModified(KeySetElementEvent<K, V> e) throws Throwable {
                Throwable finalThrowable = null;
                try {
                    KeySetElementListener<K, V> keySetElementListener = 
                        (KeySetElementListener<K, V>)
                        e
                        .getAttributeContext(AttributeScope.LOCAL)
                        .removeAttribute(AK_KEY_SET_ELEMENT_LISTENER);
                    if (keySetElementListener != null) {
                        keySetElementListener.modified(e);
                    }
                } catch (Throwable ex) {
                    finalThrowable = ex;
                }
                try {
                    super.raiseModified(e);
                } catch (Throwable ex) {
                    if (finalThrowable == null) {
                        finalThrowable = ex;
                    }
                }
                if (finalThrowable != null) {
                    throw finalThrowable;
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            @Deprecated
            protected final void bubbleModifying(ElementEvent<K> e) {
                this.bubbleModifying((KeySetElementEvent<K, V>)e);
            }

            @SuppressWarnings("unchecked")
            @Override
            @Deprecated
            protected final void bubbleModified(ElementEvent<K> e) {
                this.bubbleModified((KeySetElementEvent<K, V>)e);
            }
            
            protected void bubbleModifying(final KeySetElementEvent<K, V> e) {
                super.bubbleModifying(e);
            }
            
            protected void bubbleModified(KeySetElementEvent<K, V> e) {
                super.bubbleModified(e);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void addKeySetElementListener(
                    KeySetElementListener<? super K, ? super V> listener) {
                this.keySetElementListener = KeySetElementListener.combine(
                            this.keySetElementListener, 
                            (KeySetElementListener<K, V>)listener);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void removeKeySetElementListener(
                    KeySetElementListener<? super K, ? super V> listener) {
                this.keySetElementListener = KeySetElementListener.remove(
                            this.keySetElementListener, 
                            (KeySetElementListener<K, V>)listener);
            }
            
        }
        
        protected static class IteratorImpl<K, V> extends AbstractIteratorImpl<K, V> {

            protected IteratorImpl(AbstractOrderedKeySetImpl<K, V> parentSet) {
                super(parentSet, false);
            }

            @Override
            public CollectionViewInfos.Iterator viewInfo() {
                return CollectionViewInfos.iterator();
            }
        }
        
        protected static class DescendingIteratorImpl<K, V> extends AbstractIteratorImpl<K, V> {

            protected DescendingIteratorImpl(AbstractOrderedKeySetImpl<K, V> parentSet) {
                super(parentSet, true);
            }

            @Override
            public DescendingIterator viewInfo() {
                return OrderedSetViewInfos.descendingIterator();
            }
            
        }
        
    }
    
    protected static class KeySetImpl<K, V> extends AbstractOrderedKeySetImpl<K, V> {
        
        @SuppressWarnings("unchecked")
        protected KeySetImpl(AbstractMAOrderedMap<K, V> parentMap) {
            super(
                    parentMap,
                    (OrderedBaseEntries<K, Object>)parentMap.baseEntries);
        }

        @Override
        protected void bubbleModifying(final KeySetElementEvent<K, V> e) {
            AbstractMAOrderedMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> bubbledEvent = new MapElementEvent<>(
                    parentMap, 
                    new Cause(e),
                    version -> e.getElement(version),
                    version -> e.getValue()
            );
            parentMap.executeModifying(bubbledEvent);
        }

        @Override
        protected void bubbleModified(KeySetElementEvent<K, V> e) {
            AbstractMAOrderedMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> bubbledEvent = e.getBubbledEvent(parentMap);
            parentMap.executeModified(bubbledEvent);
        }

        @Override
        public OrderedKeySet viewInfo() {
            return OrderedMapViewInfos.orderedKeySet();
        }
        
    }
    
    protected static class DescendingKeySetImpl<K, V> extends AbstractOrderedKeySetImpl<K, V> {
        
        @SuppressWarnings("unchecked")
        protected DescendingKeySetImpl(AbstractMAOrderedMap<K, V> parentMap) {
            super(
                    parentMap,
                    ((OrderedBaseEntries<K, Object>)parentMap.baseEntries)
                    .descendingEntries());
        }

        @Override
        protected void bubbleModifying(final KeySetElementEvent<K, V> e) {
            AbstractMAOrderedMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> event = new MapElementEvent<>(
                    parentMap, 
                    new Cause(e),
                    version -> e.getElement(version),
                    version -> e.getValue()
            );
            parentMap.executeModifying(event);
        }

        @Override
        protected void bubbleModified(KeySetElementEvent<K, V> e) {
            AbstractMAOrderedMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> bubbleEvent = e.getBubbledEvent(parentMap);
            parentMap.executeModified(bubbleEvent);
        }

        @Override
        public DescendingKeySet viewInfo() {
            return OrderedMapViewInfos.descendingKeySet();
        }
        
    }
    
    class HandlerImpl4OrderedMap implements BaseEntriesHandler<K, V> {
        
        private final MapModification<K, V> modification;

        public HandlerImpl4OrderedMap(MapModification<K, V> modification) {
            this.modification = modification;
        }

        @Deprecated
        @Override
        public final Object createAddingArgument(K key, V value) {
            // Only for pollFirstEntry & pollLastEntry, only support removing&removed
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void adding(K key, V value, Object argument) {
            // Only for pollFirstEntry & pollLastEntry, only support removing&removed
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void added(K key, V value, Object argument) {
            // Only for pollFirstEntry & pollLastEntry, only support removing&removed
            throw new UnsupportedOperationException();
        }

        @Override
        public Object createChangingArgument(K oldKey, V oldValue, K newKey, V newValue) {
            return MapElementEvent.createReplaceEvent(
                    AbstractMAOrderedMap.this, 
                    this.modification, 
                    oldKey, 
                    newKey, 
                    oldValue, 
                    newValue);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void changing(K oldKey, V oldValue, K newKey, V newValue, Object argument) {
            MapElementEvent<K, V> e = (MapElementEvent<K, V>)argument;
            AbstractMAOrderedMap.this.executeModifying(e); 
        }

        @SuppressWarnings("unchecked")
        @Override
        public void changed(K oldKey, V oldValue, K newKey, V newValue, Object argument) {
            MapElementEvent<K, V> e = (MapElementEvent<K, V>)argument;
            AbstractMAOrderedMap.this.executeModified(e);
        }

        @Override
        public Object createRemovingArgument(K oldKey, V oldValue) {
            return MapElementEvent.createDetachEvent(
                    AbstractMAOrderedMap.this, 
                    this.modification, 
                    oldKey,
                    oldValue);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removing(K oldKey, V oldValue, Object argument) {
            MapElementEvent<K, V> e = (MapElementEvent<K, V>)argument;
            AbstractMAOrderedMap.this.executeModifying(e);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removed(K oldKey, V oldValue, Object argument) {
            MapElementEvent<K, V> e = (MapElementEvent<K, V>)argument;
            AbstractMAOrderedMap.this.executeModified(e);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void setPreThrowable(Object argument, Throwable throwable) {
            MapElementEvent<K, V> event = (MapElementEvent<K, V>)argument;
            ((InAllChainAttributeContext)event.getAttributeContext(AttributeScope.IN_ALL_CHAIN))
            .setPreThrowable(throwable);
        }

        @Override
        public void setNullOrThrowable(Throwable throwable) {
            if (throwable != null) {
                ((GlobalAttributeContext)this.modification.getAttributeContext()).setThrowable(throwable);
            } else {
                ((GlobalAttributeContext)this.modification.getAttributeContext()).success();    
            }
        }
    }
    
    protected static class FirstEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected FirstEntryImpl(AbstractMAOrderedMap<K, V> parentMap) 
        throws NoEntryException {
            super(parentMap, ((OrderedBaseEntries<K, V>)parentMap.baseEntries).first());
        }

        @Override
        public OrderedMapViewInfos.FirstEntry viewInfo() {
            return OrderedMapViewInfos.firstEntry();
        }
        
    }
    
    protected static class LastEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected LastEntryImpl(AbstractMAOrderedMap<K, V> parentMap) 
        throws NoEntryException {
            super(parentMap, ((OrderedBaseEntries<K, V>)parentMap.baseEntries).last());
        }

        @Override
        public OrderedMapViewInfos.LastEntry viewInfo() {
            return OrderedMapViewInfos.lastEntry();
        }
        
    }

}
