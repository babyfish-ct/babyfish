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

import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.collection.spi.base.NoEntryException;
import org.babyfish.collection.spi.base.OrderedBaseEntries;
import org.babyfish.collection.viewinfo.OrderedMapViewInfos;
import org.babyfish.collection.viewinfo.OrderedMapViewInfos.DescendingKeySet;
import org.babyfish.collection.viewinfo.OrderedMapViewInfos.DescendingMap;
import org.babyfish.collection.viewinfo.OrderedMapViewInfos.FirstEntry;
import org.babyfish.collection.viewinfo.OrderedMapViewInfos.LastEntry;
import org.babyfish.collection.viewinfo.OrderedMapViewInfos.OrderedKeySet;
import org.babyfish.collection.viewinfo.OrderedSetViewInfos;
import org.babyfish.collection.viewinfo.OrderedSetViewInfos.DescendingSet;
import org.babyfish.data.View;

/**
 * @author Tao Chen
 */
public abstract class AbstractXOrderedMap<K, V> extends AbstractXMap<K, V> implements XOrderedMap<K, V> {

    protected AbstractXOrderedMap(OrderedBaseEntries<K, V> baseEntries) {
        super(baseEntries);
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
    protected AbstractXOrderedMap() {
        
    }

    @Override
    public boolean headAppend() {
        return this.<OrderedBaseEntries<K, V>>getBaseEntries().headAppend();
    }

    @Override
    public OrderAdjustMode replaceMode() {
        return this.<OrderedBaseEntries<K, V>>getBaseEntries().replaceMode();
    }

    @Override
    public OrderAdjustMode accessMode() {
        return this.<OrderedBaseEntries<K, V>>getBaseEntries().accessMode();
    }

    @Override
    public XOrderedMapView<K, V> descendingMap() {
        return new DescendingMapImpl<K, V>(this);
    }
    
    @Override
    public XOrderedKeySetView<K> keySet() {
        return this.getKeySet();
    }
    
    @Override
    public XOrderedKeySetView<K> descendingKeySet() {
        return new DescendingKeySetImpl<K>(this);
    }
    
    @Override
    public XEntry<K, V> firstEntry() {
        try {
            return new FirstEntryImpl<K, V>(this);
        } catch (NoEntryException ex) {
            return null;
        }
    }

    @Override
    public K firstKey() {
        BaseEntry<K, V> be = this.<OrderedBaseEntries<K, V>>getBaseEntries().first();
        return be == null ? null : be.getKey();
    }
    
    @Override
    public XEntry<K, V> lastEntry() {
        try {
            return new LastEntryImpl<K, V>(this);
        } catch (NoEntryException ex) {
            return null;
        }
    }

    @Override
    public K lastKey() {
        BaseEntry<K, V> be = this.<OrderedBaseEntries<K, V>>getBaseEntries().last();
        return be == null ? null : be.getKey();
    }

    @Override
    public V access(K key) {
        BaseEntry<K, V> be = this.<OrderedBaseEntries<K, V>>getBaseEntries().access(key, null);
        if (be != null) {
            return be.getValue();
        }
        return null;
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return this.<OrderedBaseEntries<K, V>>getBaseEntries().pollFirst(null);
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return this.<OrderedBaseEntries<K, V>>getBaseEntries().pollLast(null);
    }

    @Override
    protected XOrderedKeySetView<K> createKeySet() {
        return new KeySetImpl<K>(this);
    }

    protected static class DescendingMapImpl<K, V> 
    extends AbstractXOrderedMap<K, V> 
    implements XOrderedMapView<K, V> {
        
        protected DescendingMapImpl(AbstractXOrderedMap<K, V> parent) {
            super(((OrderedBaseEntries<K, V>)parent.baseEntries).descendingEntries());
        }
        
        @Override
        public DescendingMap viewInfo() {
            return OrderedMapViewInfos.descendingMap();
        }
        
    }

    protected static abstract class AbstractOrderedKeySetImpl<K> 
    extends AbstractXOrderedSet<K> 
    implements XOrderedKeySetView<K> {

        AbstractOrderedKeySetImpl(OrderedBaseEntries<K, Object> baseEntries) {
            super(baseEntries);
        }
        
        @Deprecated
        @Override
        public final boolean add(K e) 
        throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
        
        @Deprecated
        @Override
        public final boolean addAll(Collection<? extends K> c) 
        throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public XOrderedKeySetView<K> descendingSet() {
            return new DescendingSetImpl<K>(this);
        }
        
        protected static class DescendingSetImpl<K> extends AbstractOrderedKeySetImpl<K> {
            
            protected DescendingSetImpl(AbstractOrderedKeySetImpl<K> parent) {
                super(((OrderedBaseEntries<K, Object>)parent.baseEntries));
            }

            @Override
            public DescendingSet viewInfo() {
                return OrderedSetViewInfos.descendingSet();
            }
            
        }
        
    }
    
    protected static class KeySetImpl<K> extends AbstractOrderedKeySetImpl<K> {

        @SuppressWarnings("unchecked")
        KeySetImpl(AbstractXOrderedMap<K, ?> parent) {
            super((OrderedBaseEntries<K, Object>)parent.getBaseEntries());
        }

        @Override
        public OrderedKeySet viewInfo() {
            return OrderedMapViewInfos.orderedKeySet();
        }
        
    }
    
    protected static class DescendingKeySetImpl<K> extends AbstractOrderedKeySetImpl<K> {

        @SuppressWarnings("unchecked")
        DescendingKeySetImpl(AbstractXOrderedMap<K, ?> parent) {
            super(((OrderedBaseEntries<K, Object>)parent.getBaseEntries()).descendingEntries());
        }

        @Override
        public DescendingKeySet viewInfo() {
            return OrderedMapViewInfos.descendingKeySet(); 
        }
        
    }
    
    protected static class FirstEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected FirstEntryImpl(AbstractXOrderedMap<K, V> parentMap) 
        throws NoEntryException {
            super(((OrderedBaseEntries<K, V>)parentMap.baseEntries).first());
        }

        @Override
        public FirstEntry viewInfo() {
            return OrderedMapViewInfos.firstEntry();
        }
        
    }
    
    protected static class LastEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected LastEntryImpl(AbstractXOrderedMap<K, V> parentMap) throws NoEntryException {
            super(((OrderedBaseEntries<K, V>)parentMap.baseEntries).last());
        }
        
        @Override
        public LastEntry viewInfo() {
            return OrderedMapViewInfos.lastEntry();
        }
        
    }
    
}
