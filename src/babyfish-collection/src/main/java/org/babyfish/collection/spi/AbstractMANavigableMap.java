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
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.KeySetElementEvent;
import org.babyfish.collection.event.KeySetElementListener;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementEvent.MapModification;
import org.babyfish.collection.event.modification.NavigableMapModifications;
import org.babyfish.collection.spi.base.BaseEntriesHandler;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.collection.spi.base.NavigableBaseEntries;
import org.babyfish.collection.spi.base.NoEntryException;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos;
import org.babyfish.data.View;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.Cause;
import org.babyfish.data.event.spi.GlobalAttributeContext;
import org.babyfish.data.event.spi.InAllChainAttributeContext;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
public abstract class AbstractMANavigableMap<K, V> 
extends AbstractMAMap<K, V> 
implements MANavigableMap<K, V> {
    
    private static final Object AK_KEY_SET_ELEMENT_LISTENER = new Object();
    
    protected AbstractMANavigableMap(
            NavigableBaseEntries<K, V> navigableBaseEntries) {
        super(navigableBaseEntries);
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
    protected AbstractMANavigableMap() {
        
    }
        
    @Override
    public MAEntry<K, V> firstEntry() {
        try {
            return new FirstEntryImpl<K, V>(this);
        } catch (NoEntryException e) {
            return null;
        }
    }

    @Override
    public MAEntry<K, V> lastEntry() {
        try {
            return new LastEntryImpl<K, V>(this);
        } catch (NoEntryException e) {
            return null;
        }
    }

    @Override
    public MAEntry<K, V> floorEntry(K key) {
        try {
            return new FloorEntryImpl<K, V>(this, key);
        } catch (NoEntryException e) {
            return null;
        }
    }

    @Override
    public MAEntry<K, V> ceilingEntry(K key) {
        try {
            return new CeilingEntryImpl<K, V>(this, key);
        } catch (NoEntryException e) {
            return null;
        }
    }

    @Override
    public MAEntry<K, V> lowerEntry(K key) {
        try {
            return new LowerEntryImpl<K, V>(this, key);
        } catch (NoEntryException e) {
            return null;
        }
    }

    @Override
    public MAEntry<K, V> higherEntry(K key) {
        try {
            return new HigherEntryImpl<K, V>(this, key);
        } catch (NoEntryException e) {
            return null;
        }
    }

    @Override
    public final MANavigableMapView<K, V> headMap(K toKey) {
        return this.headMap(toKey, false);
    }

    @Override
    public final MANavigableMapView<K, V> headMap(K toKey, boolean inclusive) {
        return new HeadMapImpl<K, V>(this, toKey, inclusive);
    }

    @Override
    public final MANavigableMapView<K, V> tailMap(K fromKey) {
        return this.tailMap(fromKey, true);
    }

    @Override
    public final MANavigableMapView<K, V> tailMap(K fromKey, boolean inclusive) {
        return new TailMapImpl<K, V>(this, fromKey, inclusive);
    }

    @Override
    public final MANavigableMapView<K, V> subMap(K fromKey, K toKey) {
        return this.subMap(fromKey, true, toKey, false);
    }

    @Override
    public final MANavigableMapView<K, V> subMap(K fromKey, boolean fromInclusive,
            K toKey, boolean toInclusive) {
        return new SubMapImpl<K, V>(this, fromKey, fromInclusive, toKey, toInclusive);
    }

    @Override
    public final MANavigableMapView<K, V> descendingMap() {
        return new DescendingMapImpl<K, V>(this);
    }
    
    @Override
    public final MANavigableKeySetView<K, V> keySet() {
        return this.getKeySet();
    }

    @Override
    public final MANavigableKeySetView<K, V> navigableKeySet() {
        return this.getKeySet();
    }

    @Override
    public final MANavigableKeySetView<K, V> descendingKeySet() {
        return new DescendingKeySetImpl<K, V>(this);
    }

    @Override
    public K firstKey() {
        BaseEntry<K, V> be = ((NavigableBaseEntries<K, V>)this.baseEntries).first();
        if (be != null) {
            return be.getKey();
        }
        throw new NoSuchElementException(noFirstKey(this.getClass()));
    }

    @Override
    public K lastKey() {
        BaseEntry<K, V> be = ((NavigableBaseEntries<K, V>)this.baseEntries).last();
        if (be != null) {
            return be.getKey();
        }
        throw new NoSuchElementException(noLastKey(this.getClass()));
    }

    @Override
    public K floorKey(K key) {
        BaseEntry<K, V> be = ((NavigableBaseEntries<K, V>)this.baseEntries).floor(key);
        return be == null ? null : be.getKey();
    }

    @Override
    public K ceilingKey(K key) {
        BaseEntry<K, V> be = ((NavigableBaseEntries<K, V>)this.baseEntries).ceiling(key);
        return be == null ? null : be.getKey();
    }

    @Override
    public K lowerKey(K key) {
        BaseEntry<K, V> be = ((NavigableBaseEntries<K, V>)this.baseEntries).lower(key);
        return be == null ? null : be.getKey();
    }

    @Override
    public K higherKey(K key) {
        BaseEntry<K, V> be = ((NavigableBaseEntries<K, V>)this.baseEntries).higher(key);
        return be == null ? null : be.getKey();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        // The returned entry is dead, need not wrap it.
        return ((NavigableBaseEntries<K, V>)this.baseEntries).pollFirst(
                this.new HandlerImpl4NavigableMap(
                        NavigableMapModifications.<K, V>pollFirstEntry()));
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        // The returned entry is dead, need not wrap it.
        return ((NavigableBaseEntries<K, V>)this.baseEntries).pollFirst(
                this.new HandlerImpl4NavigableMap(
                        NavigableMapModifications.<K, V>pollLastEntry()));
    }

    @Override
    public Comparator<? super K> comparator() {
        return ((NavigableBaseEntries<K, V>)this.baseEntries).comparator();
    }
    
    @Override
    protected MANavigableKeySetView<K, V> createKeySet() {
        return new KeySetImpl<K, V>(this);
    }

    protected static abstract class AbstractSubMapImpl<K, V> extends AbstractMANavigableMap<K, V> implements MANavigableMapView<K, V> {
        
        private AbstractMANavigableMap<K, V> parentMap;
        
        protected AbstractSubMapImpl(AbstractMANavigableMap<K, V> parentMap, NavigableBaseEntries<K, V> baseEntries) {
            super(baseEntries);
            this.parentMap = Arguments.mustNotBeNull("parentMap", parentMap);
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractMANavigableMap<K, V>> T getParentMap() {
            return (T)this.parentMap;
        }

        @Override
        protected void bubbleModifying(MapElementEvent<K, V> e) {
            AbstractMANavigableMap<K, V> parentMap = this.parentMap;
            MapElementEvent<K, V> bubbledEvent = new MapElementEvent<>(
                    parentMap, 
                    new Cause(e), 
                    null,
                    null);
            parentMap.executeModifying(bubbledEvent);
        }

        @Override
        protected void bubbleModified(MapElementEvent<K, V> e) {
            AbstractMANavigableMap<K, V> parentMap = this.parentMap;
            MapElementEvent<K, V> bubbledEvent = e.getBubbledEvent(parentMap);
            parentMap.executeModified(bubbledEvent);
        }
        
    }
    
    protected static class HeadMapImpl<K, V> extends AbstractSubMapImpl<K, V> {

        private NavigableMapViewInfos.HeadMap viewInfo;
        
        protected HeadMapImpl(AbstractMANavigableMap<K, V> parentMap, K toKey, boolean toInclusive) {
            super(
                    parentMap,
                    ((NavigableBaseEntries<K, V>)parentMap.baseEntries)
                    .subEntries(
                            false, null, false, 
                            true, toKey, toInclusive));
            this.viewInfo = NavigableMapViewInfos.headMap(toKey, toInclusive);
        }

        @Override
        public NavigableMapViewInfos.HeadMap viewInfo() {
            return this.viewInfo;
        }
    }
    
    protected static class TailMapImpl<K, V> extends AbstractSubMapImpl<K, V> {

        private NavigableMapViewInfos.TailMap viewInfo;
        
        protected TailMapImpl(AbstractMANavigableMap<K, V> parentMap, K fromKey, boolean fromInclusive) {
            super(
                    parentMap,
                    ((NavigableBaseEntries<K, V>)parentMap.baseEntries)
                    .subEntries(
                            true, fromKey, fromInclusive, 
                            false, null, false));
            this.viewInfo = NavigableMapViewInfos.tailMap(fromKey, fromInclusive);
        }

        @Override
        public NavigableMapViewInfos.TailMap viewInfo() {
            return this.viewInfo;
        }
    }
    
    protected static class SubMapImpl<K, V> extends AbstractSubMapImpl<K, V> {

        private NavigableMapViewInfos.SubMap viewInfo;
        
        protected SubMapImpl(
                AbstractMANavigableMap<K, V> parentMap, 
                K fromKey, boolean fromInclusive,
                K toKey, boolean toInclusive) {
            super(
                    parentMap,
                    ((NavigableBaseEntries<K, V>)parentMap.baseEntries)
                    .subEntries(
                            true, fromKey, fromInclusive, 
                            true, toKey, toInclusive));
            this.viewInfo = NavigableMapViewInfos.subMap(
                    fromKey, fromInclusive, toKey, toInclusive);
        }

        @Override
        public NavigableMapViewInfos.SubMap viewInfo() {
            return this.viewInfo;
        }
    }
    
    
    protected static class DescendingMapImpl<K, V> extends AbstractSubMapImpl<K, V> {

        protected DescendingMapImpl(AbstractMANavigableMap<K, V> parentMap) {
            super(
                    parentMap,
                    ((NavigableBaseEntries<K, V>)parentMap.baseEntries)
                    .descendingEntries());
        }

        @Override
        public NavigableMapViewInfos.DescendingMap viewInfo() {
            return NavigableMapViewInfos.descendingMap();
        }
        
    }
    
    protected static abstract class AbstractNavigableKeySetImpl<K, V>
    extends AbstractMANavigableSet<K> 
    implements MANavigableKeySetView<K, V> {
        
        private AbstractMANavigableMap<K, V> parentMap;
        
        private transient KeySetElementListener<K, V> keySetElementListener;
        
        AbstractNavigableKeySetImpl(AbstractMANavigableMap<K, V> parentMap, NavigableBaseEntries<K, Object> navigableBaseEntries) {
            super(navigableBaseEntries);
            this.parentMap = Arguments.mustNotBeNull("parentMap", parentMap);
        }
        
        AbstractNavigableKeySetImpl(NavigableBaseEntries<K, Object> navigableBaseEntries) {
            super(navigableBaseEntries);
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractMANavigableMap<K, V>> T getParentMap() {
            AbstractMANavigableMap<K, V> parentMap = this.parentMap;
            if (parentMap == null) {
                throw new IllegalStateException(noParentMap(this.getClass()));
            }
            return (T)parentMap;
        }
        
        @Override
        @Deprecated
        public final boolean add(K e) {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        public final boolean addAll(Collection<? extends K> c) {
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

        @SuppressWarnings("unchecked")
        @Override
        @Deprecated
        protected final void onModifying(ElementEvent<K> e) throws Throwable {
            this.onModifying((KeySetElementEvent<K, V>)e);
        }

        @SuppressWarnings("unchecked")
        @Override
        @Deprecated
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
        
        @Override
        public MANavigableKeySetView<K, V> descendingSet() {
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

        @Override
        public MANavigableKeySetView<K, V> headSet(K toElement) {
            return this.headSet(toElement, false);
        }

        @Override
        public MANavigableKeySetView<K, V> headSet(K toElement, boolean inclusive) {
            return new HeadSetImpl<K, V>(this, toElement, inclusive);
        }

        @Override
        public MANavigableKeySetView<K, V> tailSet(K fromElement) {
            return this.tailSet(fromElement, true);
        }

        @Override
        public MANavigableKeySetView<K, V> tailSet(K fromElement, boolean inclusive) {
            return new TailSetImpl<K, V>(this, fromElement, inclusive);
        }

        @Override
        public MANavigableKeySetView<K, V> subSet(K fromElement, K toElement) {
            return this.subSet(fromElement, true, toElement, false);
        }

        @Override
        public MANavigableKeySetView<K, V> subSet(
                K fromElement, boolean fromInclusive,
                K toElement, boolean toInclusive) {
            return new SubSetImpl<K, V>(
                    this,
                    fromElement, fromInclusive, 
                    toElement, toInclusive);
        }
        
        protected static abstract class AbstractSubSetImpl<K, V> extends AbstractNavigableKeySetImpl<K, V> {
            
            private AbstractNavigableKeySetImpl<K, V> parentSet;
            
            AbstractSubSetImpl(
                    AbstractNavigableKeySetImpl<K, V> parentSet, 
                    boolean hasFrom, K from, boolean fromInclusive,
                    boolean hasEnd, K to, boolean toInclusive) {
                super((
                        (NavigableBaseEntries<K, Object>)
                        Arguments.mustNotBeNull("parentSet", parentSet)
                        .baseEntries)
                        .subEntries(
                                hasFrom, from, fromInclusive, 
                                hasEnd, to, toInclusive));
                this.parentSet = parentSet;
            }
            
            @SuppressWarnings("unchecked")
            AbstractSubSetImpl(
                    AbstractNavigableKeySetImpl<K, V> parentSet, 
                    NavigableBaseEntries<K, ?> baseEntries) {
                super((NavigableBaseEntries<K, Object>)baseEntries);
                this.parentSet = parentSet;
            }
            
            @SuppressWarnings("unchecked")
            protected final <T extends AbstractNavigableKeySetImpl<K, V>> T getParentSet() {
                return (T)this.parentSet;
            }
            
            @Override
            protected void bubbleModifying(KeySetElementEvent<K, V> e) {
                AbstractNavigableKeySetImpl<K, V> parentSet = this.parentSet;
                KeySetElementEvent<K, V> event = new KeySetElementEvent<>(
                        parentSet, 
                        new Cause(e), 
                        null,
                        null);
                parentSet.executeModifying(event);
            }

            @Override
            protected void bubbleModified(KeySetElementEvent<K, V> e) {
                AbstractNavigableKeySetImpl<K, V> parentSet = this.parentSet;
                KeySetElementEvent<K, V> bubbleEvent = e.getBubbledEvent(parentSet);
                parentSet.executeModified(bubbleEvent);
            }
            
        }
        
        protected static class HeadSetImpl<K, V> extends AbstractSubSetImpl<K, V> {
            
            protected NavigableSetViewInfos.HeadSet viewInfo;
            
            protected HeadSetImpl(
                    AbstractNavigableKeySetImpl<K, V> parentSet, 
                    K to, 
                    boolean inclusive) {
                super(
                        parentSet,
                        false, null, false, 
                        true, to, inclusive);
                this.viewInfo = NavigableSetViewInfos.headSet(to, inclusive);
            }

            @Override
            public NavigableSetViewInfos.HeadSet viewInfo() {
                return this.viewInfo;
            }
            
        }
        
        protected static class TailSetImpl<K, V> extends AbstractSubSetImpl<K, V> {
            
            protected NavigableSetViewInfos.TailSet viewInfo;
            
            protected TailSetImpl(
                    AbstractNavigableKeySetImpl<K, V> parentSet, 
                    K from, 
                    boolean inclusive) {
                super(
                        parentSet, 
                        true, from, inclusive, 
                        false, null, false);
                this.viewInfo = NavigableSetViewInfos.tailSet(from, inclusive);
            }

            @Override
            public NavigableSetViewInfos.TailSet viewInfo() {
                return this.viewInfo;
            }
            
        }
        
        protected static class SubSetImpl<K, V> extends AbstractSubSetImpl<K, V> {
            
            private NavigableSetViewInfos.SubSet viewInfo;
            
            protected SubSetImpl(
                    AbstractNavigableKeySetImpl<K, V> parentSet, 
                    K from, 
                    boolean fromInclusive, 
                    K to, 
                    boolean toInclusive) {
                super(
                        parentSet,
                        true, from, fromInclusive, 
                        true, to, toInclusive);
                this.viewInfo = NavigableSetViewInfos.subSet(from, fromInclusive, to, toInclusive);
            }

            @Override
            public NavigableSetViewInfos.SubSet viewInfo() {
                return this.viewInfo;
            }
            
        }
        
        protected static class DescendingSetImpl<K, V> extends AbstractSubSetImpl<K, V> {
            
            protected DescendingSetImpl(
                    AbstractNavigableKeySetImpl<K, V> parentSet) {
                super(
                        parentSet,
                        ((NavigableBaseEntries<K, Object>)parentSet.baseEntries)
                        .descendingEntries());
            }

            @Override
            public NavigableSetViewInfos.DescendingSet viewInfo() {
                return NavigableSetViewInfos.descendingSet();
            }
            
        }
        
        protected static abstract class AbstractIteratorImpl<K, V>
        extends AbstractMANavigableSet.AbstractIteratorImpl<K>
        implements MAKeySetIterator<K, V> {
            
            protected transient KeySetElementListener<K, V> keySetElementListener;
            
            AbstractIteratorImpl(
                    AbstractNavigableKeySetImpl<K, V> parentSet, 
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
            @Override
            @Deprecated
            protected final void onModifying(ElementEvent<K> e) throws Throwable {
                this.onModifying((KeySetElementEvent<K, V>)e);
            }
            
            @SuppressWarnings("unchecked")
            @Override
            @Deprecated
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

            protected IteratorImpl(AbstractNavigableKeySetImpl<K, V> parentSet) {
                super(parentSet, false);
            }

            @Override
            public CollectionViewInfos.Iterator viewInfo() {
                return CollectionViewInfos.iterator();
            }
            
        }
        
        protected static class DescendingIteratorImpl<K, V> extends AbstractIteratorImpl<K, V> {

            protected DescendingIteratorImpl(AbstractNavigableKeySetImpl<K, V> parentSet) {
                super(parentSet, true);
            }
            
            @Override
            public NavigableSetViewInfos.DescendingIterator viewInfo() {
                return NavigableSetViewInfos.descendingIterator();
            }
            
        }
        
    }
    
    protected static class KeySetImpl<K, V> extends AbstractNavigableKeySetImpl<K, V> {
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        KeySetImpl(AbstractMANavigableMap<K, V> parentMap) {
            super(
                    parentMap,
                    (NavigableBaseEntries)parentMap.baseEntries);
        }

        @Override
        protected void bubbleModifying(final KeySetElementEvent<K, V> e) {
            AbstractMANavigableMap<K, V> parentMap = this.getParentMap();
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
            AbstractMANavigableMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> bubbledEvent = e.getBubbledEvent(parentMap);
            parentMap.executeModified(bubbledEvent);
        }

        @Override
        public NavigableMapViewInfos.NavigableKeySet viewInfo() {
            return NavigableMapViewInfos.navigableKeySet();
        }
        
    }
    
    protected static class DescendingKeySetImpl<K, V> extends AbstractNavigableKeySetImpl<K, V> {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        DescendingKeySetImpl(AbstractMANavigableMap<K, V> parentMap) {
            super(
                    parentMap,
                    ((NavigableBaseEntries)parentMap.baseEntries)
                    .descendingEntries());
        }

        @Override
        protected void bubbleModifying(final KeySetElementEvent<K, V> e) {
            AbstractMANavigableMap<K, V> parentMap = this.getParentMap();
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
            AbstractMANavigableMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> bubbleEvent = e.getBubbledEvent(parentMap);
            parentMap.executeModified(bubbleEvent);
        }

        @Override
        public NavigableMapViewInfos.DescendingKeySet viewInfo() {
            return NavigableMapViewInfos.descendingKeySet();
        }
        
    }
    
    protected static class FirstEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected FirstEntryImpl(AbstractMANavigableMap<K, V> parentMap) throws NoEntryException {
            super(parentMap, ((NavigableBaseEntries<K, V>)parentMap.baseEntries).first());
        }

        @Override
        public NavigableMapViewInfos.FirstEntry viewInfo() {
            return NavigableMapViewInfos.firstEntry();
        }
        
    }
    
    protected static class LastEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected LastEntryImpl(AbstractMANavigableMap<K, V> parentMap) throws NoEntryException {
            super(parentMap, ((NavigableBaseEntries<K, V>)parentMap.baseEntries).last());
        }
        
        @Override
        public NavigableMapViewInfos.LastEntry viewInfo() {
            return NavigableMapViewInfos.lastEntry();
        }
        
    }
    
    protected static class FloorEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        private NavigableMapViewInfos.FloorEntry viewInfo;
        
        protected FloorEntryImpl(AbstractMANavigableMap<K, V> parentMap, K key) throws NoEntryException {
            super(parentMap, ((NavigableBaseEntries<K, V>)parentMap.baseEntries).floor(key));
            this.viewInfo = NavigableMapViewInfos.floorEntry(key);
        }
        
        @Override
        public NavigableMapViewInfos.FloorEntry viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class CeilingEntryImpl<K, V> extends AbstractEntryImpl<K, V> {
        
        private NavigableMapViewInfos.CeilingEntry viewInfo;

        protected CeilingEntryImpl(AbstractMANavigableMap<K, V> parentMap, K key) throws NoEntryException {
            super(parentMap, ((NavigableBaseEntries<K, V>)parentMap.baseEntries).ceiling(key));
            this.viewInfo = NavigableMapViewInfos.ceilingEntry(key);
        }
        
        @Override
        public NavigableMapViewInfos.CeilingEntry viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class LowerEntryImpl<K, V> extends AbstractEntryImpl<K, V> {
        
        private NavigableMapViewInfos.LowerEntry viewInfo;

        protected LowerEntryImpl(AbstractMANavigableMap<K, V> parentMap, K key) throws NoEntryException {
            super(parentMap, ((NavigableBaseEntries<K, V>)parentMap.baseEntries).lower(key));
            this.viewInfo = NavigableMapViewInfos.lowerEntry(key);
        }
        
        @Override
        public NavigableMapViewInfos.LowerEntry viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class HigherEntryImpl<K, V> extends AbstractEntryImpl<K, V> {
        
        private NavigableMapViewInfos.HigherEntry viewInfo;

        protected HigherEntryImpl(AbstractMANavigableMap<K, V> parentMap, K key) throws NoEntryException {
            super(parentMap, ((NavigableBaseEntries<K, V>)parentMap.baseEntries).higher(key));
            this.viewInfo = NavigableMapViewInfos.higherEntry(key);
        }
        
        @Override
        public NavigableMapViewInfos.HigherEntry viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    class HandlerImpl4NavigableMap implements BaseEntriesHandler<K, V> {
        
        private final MapModification<K, V> modification;

        public HandlerImpl4NavigableMap(MapModification<K, V> modification) {
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
        
        @Deprecated
        @Override
        public final Object createChangingArgument(K oldKey, V oldValue, K newKey, V newValue) {
            // Only for pollFirstEntry & pollLastEntry, only support removing&removed
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void changing(K oldKey, V oldValue, K newKey, V newValue, Object argument) {
            // Only for pollFirstEntry & pollLastEntry, only support removing&removed
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void changed(K oldKey, V oldValue, K newKey, V newValue, Object argument) {
            // Only for pollFirstEntry & pollLastEntry, only support removing&removed
            throw new UnsupportedOperationException();
        }

        @Override
        public Object createRemovingArgument(K oldKey, V oldValue) {
            return MapElementEvent.createDetachEvent(
                    AbstractMANavigableMap.this, 
                    this.modification, 
                    oldKey,
                    oldValue);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removing(K oldKey, V oldValue, Object argument) {
            MapElementEvent<K, V> e = (MapElementEvent<K, V>)argument; 
            AbstractMANavigableMap.this.executeModifying(e);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removed(K oldKey, V oldValue, Object argument) {
            MapElementEvent<K, V> e = (MapElementEvent<K, V>)argument;
            AbstractMANavigableMap.this.executeModified(e);
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
    
    @I18N
    private static native String noFirstKey(Class<?> thisType);
        
    @I18N
    private static native String noLastKey(Class<?> thisType);
        
    @I18N
    private static native String noParentMap(Class<?> thisType);
}
