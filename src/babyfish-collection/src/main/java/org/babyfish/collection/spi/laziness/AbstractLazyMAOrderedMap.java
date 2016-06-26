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

import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.spi.base.NoEntryException;
import org.babyfish.collection.viewinfo.OrderedMapViewInfos;
import org.babyfish.collection.viewinfo.OrderedSetViewInfos;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;

/**
 * @author Tao Chen
 */
public abstract class AbstractLazyMAOrderedMap<K, V> extends AbstractLazyMAMap<K, V> implements MAOrderedMap<K, V> {
    
    protected AbstractLazyMAOrderedMap(MAOrderedMap<K, V> base) {
        super(base);
    }

    protected AbstractLazyMAOrderedMap(
            AbstractLazyMAOrderedMap<K, V> parent,
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
    protected AbstractLazyMAOrderedMap() {
        
    }

    @Override
    public MAOrderedMapView<K, V> descendingMap() {
        return new DescendingMapImpl<K, V>(this);
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
    public boolean headAppend() {
        return this.<MAOrderedMap<K, V>>getBase().headAppend();
    }

    @Override
    public OrderAdjustMode accessMode() {
        return this.<MAOrderedMap<K, V>>getBase().accessMode();
    }

    @Override
    public OrderAdjustMode replaceMode() {
        return this.<MAOrderedMap<K, V>>getBase().replaceMode();
    }

    @Override
    public K firstKey() {
        this.requiredEnabled();
        return this.<MAOrderedMap<K, V>>getBase().firstKey();
    }

    @Override
    public K lastKey() {
        this.requiredEnabled();
        return this.<MAOrderedMap<K, V>>getBase().lastKey();
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
    public V access(K key) {
        this.enable();
        return this.<MAOrderedMap<K, V>>getBase().access(key);
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        this.enable();
        return this.<MAOrderedMap<K, V>>getBase().pollFirstEntry();
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        this.enable();
        return this.<MAOrderedMap<K, V>>getBase().pollLastEntry();
    }
    
    @Override
    protected abstract RootData<K, V> createRootData();

    @Deprecated
    @Override
    protected final MAMap<K, V> createBaseView(
            MAMap<K, V> parentBase,
            ViewInfo viewInfo) {
        return this.createBaseView((MAOrderedMap<K, V>)parentBase, viewInfo);
    }
    
    protected MAOrderedMap<K, V> createBaseView(
            MAOrderedMap<K, V> parentBase,
            ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }
    
    @Override
    protected MAOrderedKeySetView<K, V> createKeySet() {
        return new KeySetImpl<K, V>(this);
    }
    
    protected static class DescendingMapImpl<K, V> 
    extends AbstractLazyMAOrderedMap<K, V> 
    implements MAOrderedMapView<K, V> {

        protected DescendingMapImpl(AbstractLazyMAOrderedMap<K, V> parent) {
            super(parent, OrderedMapViewInfos.descendingMap());
        }

        @Override
        protected MAOrderedMap<K, V> createBaseView(
                MAOrderedMap<K, V> parentBase, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof OrderedMapViewInfos.DescendingMap) {
                return parentBase.descendingMap();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }

        @Override
        public ViewInfo viewInfo() {
            return this.<MAOrderedMapView<K, V>>getBase().viewInfo();
        }

        @Deprecated
        @Override
        protected final RootData<K, V> createRootData() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    protected static class AbstractKeySetImpl<K, V> 
    extends AbstractLazyMAMap.AbstractKeySetImpl<K, V> 
    implements MAOrderedKeySetView<K, V> {

        protected AbstractKeySetImpl(
                AbstractLazyMAOrderedMap<K, V> parentMap,
                ViewInfo initialViewInfo) {
            super(parentMap, initialViewInfo);
        }

        protected AbstractKeySetImpl(
                AbstractKeySetImpl<K, V> parent,
                ViewInfo viewInfo) {
            super(parent, viewInfo);
        }

        @Override
        public MAOrderedKeySetView<K, V> descendingSet() {
            return new DescendingSetImpl<K, V>(this);
        }

        @Override
        public MAKeySetIterator<K, V> descendingIterator() {
            return new DescendingIteratorImpl<K, V>(this);
        }

        @Override
        public boolean headAppend() {
            return this.<MAOrderedKeySetView<K, V>>getBase().headAppend();
        }

        @Override
        public OrderAdjustMode replaceMode() {
            return this.<MAOrderedKeySetView<K, V>>getBase().replaceMode();
        }

        @Override
        public K first() {
            this.requiredEnabled();
            return this.<MAOrderedKeySetView<K, V>>getBase().first();
        }

        @Override
        public K last() {
            this.requiredEnabled();
            return this.<MAOrderedKeySetView<K, V>>getBase().last();
        }

        @Override
        public K pollFirst() {
            this.enable();
            return this.<MAOrderedKeySetView<K, V>>getBase().pollFirst();
        }

        @Override
        public K pollLast() {
            this.enable();
            return this.<MAOrderedKeySetView<K, V>>getBase().pollLast();
        }

        @Deprecated
        @Override
        protected final MAKeySetView<K, V> createBaseView(
                MAMap<K, V> baseMap, ViewInfo viewInfo) {
            return this.createBaseView((MAOrderedMap<K, V>)baseMap, viewInfo);
        }
        
        protected MAOrderedKeySetView<K, V> createBaseView(
                MAOrderedMap<K, V> baseMap, ViewInfo viewInfo) {
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }

        @Deprecated
        @Override
        protected final MAKeySetView<K, V> createBaseView(
                MAKeySetView<K, V> parentBase, ViewInfo viewInfo) {
            return this.createBaseView((MAOrderedKeySetView<K, V>)parentBase, viewInfo);
        }
        
        protected MAOrderedKeySetView<K, V> createBaseView(
                MAOrderedKeySetView<K, V> parentBase, ViewInfo viewInfo) {
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
        protected static class DescendingSetImpl<K, V> extends AbstractKeySetImpl<K, V> {

            public DescendingSetImpl(AbstractKeySetImpl<K, V> parent) {
                super(parent, OrderedSetViewInfos.descendingSet());
            }

            @Override
            protected MAOrderedKeySetView<K, V> createBaseView(
                    MAOrderedKeySetView<K, V> parentBase,
                    ViewInfo viewInfo) {
                if (viewInfo instanceof OrderedSetViewInfos.DescendingSet) {
                    return parentBase.descendingSet();
                }
                throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
            }
            
        }
        
        protected static abstract class AbstractIteratorImpl<K, V>
        extends AbstractLazyMAMap.AbstractKeySetImpl.AbstractIteratorImpl<K, V> {

            protected AbstractIteratorImpl(
                    AbstractKeySetImpl<K, V> parent,
                    ViewInfo viewInfo) {
                super(parent, viewInfo);
            }
            
            @Deprecated
            @Override
            protected final MAKeySetIterator<K, V> createBaseView(
                    MAKeySetView<K, V> baseKeySet,
                    ViewInfo viewInfo) {
                return this.createBaseView(
                        (MAOrderedKeySetView<K, V>)baseKeySet, viewInfo);
            }
            
            protected abstract MAKeySetIterator<K, V> createBaseView(
                    MAOrderedKeySetView<K, V> baseKeySet,
                    ViewInfo viewInfo);
        }
        
        protected static class DescendingIteratorImpl<K, V> extends AbstractIteratorImpl<K, V> {

            protected DescendingIteratorImpl(AbstractKeySetImpl<K, V> parent) {
                super(parent, OrderedSetViewInfos.descendingIterator());
            }

            @Override
            protected MAKeySetIterator<K, V> createBaseView(
                    MAOrderedKeySetView<K, V> baseKeySet,
                    ViewInfo viewInfo) {
                if (viewInfo instanceof OrderedSetViewInfos.DescendingIterator) {
                    return baseKeySet.descendingIterator();
                }
                throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
            }
            
        }
    }
    
    protected static class KeySetImpl<K, V> extends AbstractKeySetImpl<K, V> {

        protected KeySetImpl(AbstractLazyMAOrderedMap<K, V> parentMap) {
            super(parentMap, OrderedMapViewInfos.keySet());
        }

        @Override
        protected MAOrderedKeySetView<K, V> createBaseView(
                MAOrderedMap<K, V> baseMap, ViewInfo viewInfo) {
            if (viewInfo instanceof OrderedMapViewInfos.KeySet) {
                return baseMap.keySet();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
    }
    
    protected static class DescendingKeySetImpl<K, V> extends AbstractKeySetImpl<K, V> {
        protected DescendingKeySetImpl(AbstractLazyMAOrderedMap<K, V> parentMap) {
            super(parentMap, OrderedMapViewInfos.descendingKeySet());
        }

        @Override
        protected MAOrderedKeySetView<K, V> createBaseView(
                MAOrderedMap<K, V> baseMap, ViewInfo viewInfo) {
            if (viewInfo instanceof OrderedMapViewInfos.DescendingKeySet) {
                return baseMap.descendingKeySet();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
    }
    
    protected static abstract class AbstractEntryImpl<K, V> extends AbstractLazyMAMap.AbstractEntryImpl<K, V> {

        protected AbstractEntryImpl(
                AbstractLazyMAOrderedMap<K, V> parentMap,
                ViewInfo initialViewInfo) throws NoEntryException {
            super(parentMap, initialViewInfo);
        }

        @Deprecated
        @Override
        protected final MAEntry<K, V> createBaseView(
                MAMap<K, V> map, ViewInfo viewInfo) {
            return this.createBaseView((MAOrderedMap<K, V>)map, viewInfo);
        }
        
        protected abstract MAEntry<K, V> createBaseView(
                MAOrderedMap<K, V> baseMap, ViewInfo viewInfo);
        
    }
    
    protected static class FirstEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected FirstEntryImpl(AbstractLazyMAOrderedMap<K, V> parentMap) throws NoEntryException {
            super(parentMap, OrderedMapViewInfos.firstEntry());
        }

        @Override
        protected MAEntry<K, V> createBaseView(
                MAOrderedMap<K, V> baseMap, ViewInfo viewInfo) {
            if (viewInfo instanceof OrderedMapViewInfos.FirstEntry) {
                return baseMap.firstEntry();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
    }
    
    protected static class LastEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected LastEntryImpl(AbstractLazyMAOrderedMap<K, V> parentMap) throws NoEntryException {
            super(parentMap, OrderedMapViewInfos.lastEntry());
        }

        @Override
        protected MAEntry<K, V> createBaseView(
                MAOrderedMap<K, V> baseMap, ViewInfo viewInfo) {
            if (viewInfo instanceof OrderedMapViewInfos.LastEntry) {
                return baseMap.lastEntry();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
    }
    
    protected static abstract class RootData<K, V> extends AbstractLazyMAMap.RootData<K, V> {

        private static final long serialVersionUID = -6556826422648621287L;
        
        protected RootData() {
            
        }

        @Deprecated
        @Override
        protected final void setBase(MAMap<K, V> base) {
            this.setBase((MAOrderedMap<K, V>)base);
        }
        
        protected void setBase(MAOrderedMap<K, V> base) {
            super.setBase(base);
        }
        
        @Override
        protected MAOrderedMap<K, V> createDefaultBase(
                UnifiedComparator<? super K> keyUnifiedComparator,
                UnifiedComparator<? super V> valueUnifiedComparator) {
            return new MALinkedHashMap<>(
                    keyUnifiedComparator.equalityComparator(true),
                    valueUnifiedComparator);
        }
    }
}
