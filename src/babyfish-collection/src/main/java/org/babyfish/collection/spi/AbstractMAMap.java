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
import java.util.Map;

import org.babyfish.collection.MAMap;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.event.EntryElementEvent;
import org.babyfish.collection.event.EntryElementListener;
import org.babyfish.collection.event.KeySetElementEvent;
import org.babyfish.collection.event.KeySetElementListener;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementEvent.MapModification;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.collection.event.ValuesElementEvent;
import org.babyfish.collection.event.ValuesElementListener;
import org.babyfish.collection.event.modification.CollectionModifications;
import org.babyfish.collection.event.modification.EntryModifications;
import org.babyfish.collection.event.modification.IteratorModifications;
import org.babyfish.collection.event.modification.MapModifications;
import org.babyfish.collection.spi.base.BaseEntries;
import org.babyfish.collection.spi.base.BaseEntriesHandler;
import org.babyfish.collection.spi.base.BaseEntriesSpecialHandlerFactory;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.collection.spi.base.BaseEntryIterator;
import org.babyfish.collection.spi.base.NoEntryException;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.collection.viewinfo.MapViewInfos;
import org.babyfish.collection.viewinfo.MapViewInfos.KeySet;
import org.babyfish.data.ModificationException;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.Cause;
import org.babyfish.data.event.spi.GlobalAttributeContext;
import org.babyfish.data.event.spi.InAllChainAttributeContext;
import org.babyfish.lang.Arguments;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public abstract class AbstractMAMap<K, V> 
    extends AbstractXMap<K, V> 
    implements MAMap<K, V> {
    
    private static final Object AK_ELEMENT_LISTENER = new Object();
    
    private static final Object AK_MAP_ELEMENT_LISTENER = new Object();
    
    private static final Object AK_KEY_SET_ELEMENT_LISTENER = new Object();
    
    private static final Object AK_VALUES_ELEMENT_LISTENER = new Object();
    
    private static final Object AK_ENTRY_ELEMENT_LISTENER = new Object();

    protected transient MapElementListener<K, V> mapElementListener;
    
    protected AbstractMAMap(BaseEntries<K, V> baseEntries) {
        super(baseEntries);
        if (!(this instanceof View)) {
            baseEntries.initSpecialHandlerFactory(
                    this.new SpecialHandlerFactoryImpl()
            );
        }
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
    protected AbstractMAMap() {
        
    }

    protected void executeModifying(MapElementEvent<K, V> e) {
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

    protected void executeModified(MapElementEvent<K, V> e) {
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

    protected void onModifying(MapElementEvent<K, V> e) throws Throwable {
        
    }

    protected void onModified(MapElementEvent<K, V> e) throws Throwable {
        
    }

    protected void raiseModifying(MapElementEvent<K, V> e) throws Throwable {
        MapElementListener<K, V> mapElementListener = this.mapElementListener;
        if (mapElementListener != null) {
            e
            .getAttributeContext(AttributeScope.LOCAL)
            .addAttribute(AK_MAP_ELEMENT_LISTENER, mapElementListener);
            mapElementListener.modifying(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void raiseModified(MapElementEvent<K, V> e) throws Throwable {
        MapElementListener<K, V> mapElementListener = 
            (MapElementListener<K, V>)
            e
            .getAttributeContext(AttributeScope.LOCAL)
            .removeAttribute(AK_MAP_ELEMENT_LISTENER);
        if (mapElementListener != null) {
            mapElementListener.modified(e);
        }
    }
    
    protected void bubbleModifying(MapElementEvent<K, V> e) {
        
    }

    protected void bubbleModified(MapElementEvent<K, V> e) {
        
    }
    
    @Override
    protected MAEntrySetView<K, V> createEntrySet() {
        return new EntrySetImpl<K, V>(this);
    }

    @Override
    protected MAKeySetView<K, V> createKeySet() {
        return new KeySetImpl<K, V>(this);
    }

    @Override
    protected MAValuesView<K, V> createValues() {
        return new ValuesImpl<K, V>(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addMapElementListener(MapElementListener<? super K, ? super V> listener) {
        this.mapElementListener = MapElementListener.combine(
                    this.mapElementListener, 
                    (MapElementListener<K, V>)listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeMapElementListener(MapElementListener<? super K, ? super V> listener) {
        this.mapElementListener = MapElementListener.remove(
                    this.mapElementListener, 
                    (MapElementListener<K, V>)listener);
    }
    
    @Override
    public MAEntrySetView<K, V> entrySet() {
        return this.getEntrySet();
    }

    @Override
    public MAKeySetView<K, V> keySet() {
        return this.getKeySet();
    }

    @Override
    public MAValuesView<K, V> values() {
        return this.getValues();
    }
    
    @Override
    public MAEntry<K, V> entryOfKey(K key) {
        try {
            return new EntryOfKeyImpl<K, V>(this, key);
        } catch (NoEntryException ex) {
            return null;
        }
    }
    
    @Override
    public MAEntry<K, V> entryOfValue(V value) {
        try {
            return new EntryOfValueImpl<K, V>(this, value);
        } catch (NoEntryException ex) {
            return null;
        }
    }

    @Override
    public V put(K key, V value) {
        return this.baseEntries.put(
                key, 
                value, 
                this.new HandlerImpl4Map(MapModifications.put(key, value)));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.baseEntries.putAll(
                m, 
                this.new HandlerImpl4Map(MapModifications.putAll(m)));
    }

    @Override
    public void clear() {
        this.baseEntries.clear(
                this.new HandlerImpl4Map(MapModifications.<K, V>clear()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        BaseEntry<K, V> be = this.baseEntries.removeByKey(
                key, 
                this.new HandlerImpl4Map(MapModifications.<K, V>remove((K)key)));
        return be != null ? be.getValue() : null;
    }

    protected static class EntrySetImpl<K, V>
    extends AbstractXMap.EntrySetImpl<K, V>
    implements MAEntrySetView<K, V> {
        
        private transient ElementListener<Entry<K, V>> elementListener;
        
        protected EntrySetImpl(AbstractMAMap<K, V> parentMap) {
            super(parentMap);
        }
        
        protected void executeModifying(ElementEvent<Entry<K, V>> e) {
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

        protected void executeModified(ElementEvent<Entry<K, V>> e) {
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

        protected void onModifying(final ElementEvent<Entry<K, V>> e) throws Throwable {
            
        }

        protected void onModified(ElementEvent<Entry<K, V>> e) throws Throwable {
            
        }
        
        protected void raiseModifying(ElementEvent<Entry<K, V>> e) throws Throwable {
            ElementListener<Entry<K, V>> elementListener = this.elementListener;
            if (elementListener != null) {
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .addAttribute(AK_ELEMENT_LISTENER, elementListener);
                elementListener.modifying(e);
            }
        }
        
        @SuppressWarnings("unchecked")
        protected void raiseModified(ElementEvent<Entry<K, V>> e) throws Throwable {
            ElementListener<Entry<K, V>> elementListener = 
                (ElementListener<Entry<K, V>>)
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .removeAttribute(AK_ELEMENT_LISTENER);
            if (elementListener != null) {
                elementListener.modified(e);
            }
        }
        
        protected void bubbleModifying(final ElementEvent<Entry<K, V>> e) {
            MapElementEvent<K, V> bubbledEvent = new MapElementEvent<>(
                    this.getParentMap(), 
                    new Cause(e),
                    version -> e.getElement(version).getKey(),
                    version -> e.getElement(version).getValue()
            );
            this.<AbstractMAMap<K, V>>getParentMap().executeModifying(bubbledEvent);
        }
        
        protected void bubbleModified(ElementEvent<Entry<K, V>> e) {
            AbstractMAMap<K, V> parentMap = this.<AbstractMAMap<K, V>>getParentMap();
            MapElementEvent<K, V> bubbledEvent = e.getBubbledEvent(parentMap);
            parentMap.executeModified(bubbledEvent);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void addElementListener(ElementListener<? super Entry<K, V>> listener) {
            this.elementListener = ElementListener.combine(
                        this.elementListener, 
                        (ElementListener<Entry<K ,V>>)listener);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removeElementListener(ElementListener<? super Entry<K, V>> listener) {
            this.elementListener = ElementListener.remove(
                        this.elementListener, 
                        (ElementListener<Entry<K ,V>>)listener);
        }

        @Override
        public void clear() {
            this.getParentMap().baseEntries.clear(
                    this.new HandlerImpl4EntrySet(CollectionModifications.<Entry<K, V>>clear()));
        }
    
        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Entry<?, ?>)) {
                return false;
            }
            return this.getParentMap().baseEntries.removeByEntry(
                    o, 
                    this.new HandlerImpl4EntrySet(CollectionModifications.<Entry<K, V>>remove(o))
            ) != null;
        }
    
        @Override
        public boolean removeAll(Collection<?> c) {
            return this.getParentMap().baseEntries.removeAllByEntryCollection(
                    c, 
                    this.new HandlerImpl4EntrySet(CollectionModifications.<Entry<K, V>>removeAll(c)));
        }
    
        @Override
        public boolean retainAll(Collection<?> c) {
            return this.getParentMap().baseEntries.retainAllByEntryCollection(
                    c, 
                    this.new HandlerImpl4EntrySet(CollectionModifications.<Entry<K, V>>retainAll(c)));
        }

        @Override
        public MAEntrySetIterator<K, V> iterator() {
            return new IteratorImpl<K, V>(this);
        }

        protected static class IteratorImpl<K, V> implements MAEntrySetIterator<K, V> {
            
            private EntrySetImpl<K, V> parentEntrySet;
            
            private BaseEntryIterator<K, V> beIterator;
            
            private transient ElementListener<Entry<K, V>> elementListener;
            
            protected IteratorImpl(EntrySetImpl<K, V> parentEntrySet) {
                this.parentEntrySet = parentEntrySet;
                this.beIterator = parentEntrySet.getParentMap().baseEntries.iterator();
            }
            
            @SuppressWarnings("unchecked")
            protected final <T extends EntrySetImpl<K, V>> T getParentEntrySet() {
                return (T)this.parentEntrySet;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void addElementListener(ElementListener<? super Entry<K, V>> listener) {
                this.elementListener = ElementListener.combine(
                            this.elementListener, 
                            (ElementListener<Entry<K ,V>>)listener);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void removeElementListener(ElementListener<? super Entry<K, V>> listener) {
                this.elementListener = ElementListener.remove(
                            this.elementListener, 
                            (ElementListener<Entry<K ,V>>)listener);
            }
            
            protected void executeModifying(ElementEvent<Entry<K, V>> e) {
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

            protected void executeModified(ElementEvent<Entry<K, V>> e) {
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

            protected void onModifying(ElementEvent<Entry<K, V>> e) throws Throwable {
                
            }
            
            protected void onModified(ElementEvent<Entry<K, V>> e) throws Throwable {
                
            }
            
            protected void raiseModifying(ElementEvent<Entry<K, V>> e) throws Throwable {
                ElementListener<Entry<K, V>> elementListener = this.elementListener;
                if (elementListener != null) {
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .addAttribute(AK_ELEMENT_LISTENER, elementListener);
                    elementListener.modifying(e);
                }
            }
            
            @SuppressWarnings("unchecked")
            protected void raiseModified(ElementEvent<Entry<K, V>> e) throws Throwable {
                ElementListener<Entry<K, V>> elementListener = 
                    (ElementListener<Entry<K, V>>)
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .removeAttribute(AK_ELEMENT_LISTENER);
                if (elementListener != null) {
                    elementListener.modified(e);
                }
            }
            
            protected void bubbleModifying(ElementEvent<Entry<K, V>> e) {
                EntrySetImpl<K, V> parentEntrySet = this.parentEntrySet;
                ElementEvent<Entry<K, V>> bubbledEvent = new ElementEvent<>(
                        parentEntrySet, 
                        new Cause(e), 
                        null);
                parentEntrySet.executeModifying(bubbledEvent);
            }
            
            protected void bubbleModified(ElementEvent<Entry<K, V>> e) {
                EntrySetImpl<K, V> parentEntrySet = this.parentEntrySet;
                ElementEvent<Entry<K, V>> bubbledEvent = e.getBubbledEvent(parentEntrySet);
                parentEntrySet.executeModified(bubbledEvent);
            }
            
            @Override
            public boolean hasNext() {
                return this.beIterator.hasNext();
            }
        
            @Override
            public MAEntry<K, V> next() {
                try {
                    return new EntryImpl<K, V>(
                            this.parentEntrySet.<AbstractMAMap<K, V>>getParentMap(),
                            this.beIterator.next());
                } catch (NoEntryException e) {
                    throw new AssertionError();
                }
            }
        
            @Override
            public void remove() {
                this.beIterator.remove(this.new HandlerImpl4EntryIterator());
            }
            
            @Override
            public boolean isReadWriteLockSupported() {
                return this.parentEntrySet.isReadWriteLockSupported();
            }

            @Override
            public UnifiedComparator<? super Entry<K, V>> unifiedComparator() {
                return this.parentEntrySet.unifiedComparator();
            }

            @Override
            public CollectionViewInfos.Iterator viewInfo() {
                return CollectionViewInfos.iterator();
            }

            private class HandlerImpl4EntryIterator extends AbstractEntryHandlerImpl<K, V> {
                
                public HandlerImpl4EntryIterator() {
                    super(IteratorModifications.<Entry<K, V>>remove());
                }
                
                @Override
                protected Object eventSource() {
                    return IteratorImpl.this;
                }
                
                @Override
                protected void modifying(ElementEvent<Entry<K, V>> e) {
                    IteratorImpl.this.executeModifying(e);
                }

                @Override
                protected void modified(ElementEvent<Entry<K, V>> e) {
                    IteratorImpl.this.executeModified(e);
                }
                
            }
        }

        private class HandlerImpl4EntrySet extends AbstractEntryHandlerImpl<K, V> {
            
            public HandlerImpl4EntrySet(Modification<Entry<K, V>> modification) {
                super(modification);
            }
            
            @Override
            protected Object eventSource() {
                return EntrySetImpl.this;
            }
            @Override
            protected void modifying(ElementEvent<Entry<K, V>> e) {
                EntrySetImpl.this.executeModifying(e);
            }

            @Override
            protected void modified(ElementEvent<Entry<K, V>> e) {
                EntrySetImpl.this.executeModified(e);
            }
            
        }
        
    }
    
    protected static class KeySetImpl<K, V> extends AbstractMASet<K> implements MAKeySetView<K, V> {
        
        private AbstractMAMap<K, V> parentMap;
        
        private transient KeySetElementListener<K, V> keySetElementListener;

        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected KeySetImpl(AbstractMAMap<K, V> parentMap) {
            super((BaseEntries)parentMap.baseEntries);
            this.parentMap = Arguments.mustNotBeNull("parentMap", parentMap);
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractMAMap<K, V>> T getParentMap() {
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
        public final void addValidator(
                Validator<K> validator) 
        throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void removeValidator(
                Validator<K> validator) 
        throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public MAKeySetIterator<K, V> iterator() {
            return new IteratorImpl<K, V>(this);
        }

        @Override
        @Deprecated
        public final boolean add(K e) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        public final boolean addAll(Collection<? extends K> c) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public KeySet viewInfo() {
            return MapViewInfos.keySet();
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
        protected void raiseModified(ElementEvent<K> e) throws Throwable {
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
            AbstractMAMap<K, V> parentMap = this.parentMap;
            MapElementEvent<K, V> bubbledEvent = new MapElementEvent<>(
                    parentMap, 
                    new Cause(e),
                    version -> e.getElement(version),
                    version -> e.getValue()
             );
            parentMap.executeModifying(bubbledEvent);
        }
        
        protected void bubbleModified(KeySetElementEvent<K, V> e) {
            AbstractMAMap<K, V> parentMap = this.parentMap;
            MapElementEvent<K, V> bubbledEvent = e.getBubbledEvent(parentMap);
            parentMap.executeModified(bubbledEvent);
        }

        protected static class IteratorImpl<K, V> 
        extends AbstractMASet.AbstractIteratorImpl<K>
        implements MAKeySetIterator<K, V> {
            
            private transient KeySetElementListener<K, V> keySetElementListener;
            
            protected IteratorImpl(KeySetImpl<K, V> parentSet) {
                super(parentSet, false);
            }
            
            @Override
            public ViewInfo viewInfo() {
                return CollectionViewInfos.iterator();
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
    }
    
    protected static class ValuesImpl<K, V>
    extends AbstractXMap.ValuesImpl<K, V>
    implements MAValuesView<K, V> {
        
        private transient ElementListener<V> elementListener;
        
        private transient ValuesElementListener<K, V> valuesElementListener;
        
        protected ValuesImpl(AbstractMAMap<K, V> parentMap) {
            super(parentMap);
        }

        @Override
        public UnifiedComparator<? super V> unifiedComparator() {
            return this.getParentMap().baseEntries.valueUnifiedComparator();
        }
        
        protected void executeModifying(ValuesElementEvent<K, V> e) {
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

        protected void executeModified(ValuesElementEvent<K, V> e) {
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

        protected void onModifying(ValuesElementEvent<K, V> e) throws Throwable {
            
        }
        
        protected void onModified(ValuesElementEvent<K, V> e) throws Throwable {
            
        }
        
        protected void raiseModifying(ValuesElementEvent<K, V> e) throws Throwable {
            Throwable finalThrowable = null;
            try {
                ElementListener<V> elementListener = this.elementListener;
                if (elementListener != null) {
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .addAttribute(AK_ELEMENT_LISTENER, elementListener);
                    elementListener.modifying(e);
                }
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                ValuesElementListener<K, V> valuesElementListener = this.valuesElementListener;
                if (valuesElementListener != null) {
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .addAttribute(AK_VALUES_ELEMENT_LISTENER, valuesElementListener);
                    valuesElementListener.modifying(e);
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
        protected void raiseModified(ValuesElementEvent<K, V> e) throws Throwable {
            Throwable finalThrowable = null;
            try {
                ValuesElementListener<K, V> valuesElementListener = 
                    (ValuesElementListener<K, V>)
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .removeAttribute(AK_VALUES_ELEMENT_LISTENER);
                if (valuesElementListener != null) {
                    valuesElementListener.modified(e);
                }
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                ElementListener<V> elementListener = 
                    (ElementListener<V>)
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .removeAttribute(AK_ELEMENT_LISTENER);
                if (elementListener != null) {
                    elementListener.modified(e);
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
        
        protected void bubbleModifying(final ValuesElementEvent<K, V> e) {
            AbstractMAMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> bubbledEvent = new MapElementEvent<>(
                    parentMap, 
                    new Cause(e),
                    version -> e.getKey(),
                    version -> e.getElement(version)
            );
            parentMap.executeModifying(bubbledEvent);
        }
        
        protected void bubbleModified(ValuesElementEvent<K, V> e) {
            AbstractMAMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> bubbleEvent = e.getBubbledEvent(parentMap);
            parentMap.executeModified(bubbleEvent);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void addElementListener(ElementListener<? super V> listener) {
            this.elementListener = ElementListener.combine(
                        this.elementListener, 
                        (ElementListener<V>)listener);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removeElementListener(ElementListener<? super V> listener) {
            this.elementListener = ElementListener.remove(
                        this.elementListener, 
                        (ElementListener<V>)listener);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void addValuesElementListener(ValuesElementListener<? super K, ? super V> listener) {
            this.valuesElementListener = ValuesElementListener.combine(
                        this.valuesElementListener, 
                        (ValuesElementListener<K, V>)listener);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removeValuesElementListener(ValuesElementListener<? super K, ? super V> listener) {
            this.valuesElementListener = ValuesElementListener.remove(
                        this.valuesElementListener, 
                        (ValuesElementListener<K, V>)listener);
        }

        @Override
        public void clear() {
            this.<AbstractMAMap<K, V>>getParentMap().baseEntries.clear(
                    this.new HandlerImpl(CollectionModifications.<V>clear()));
        }

        @Override
        public boolean remove(Object o) {
            return this.<AbstractMAMap<K, V>>getParentMap().baseEntries.removeByValue(
                    o, 
                    this.new HandlerImpl(CollectionModifications.<V>remove(o))
            ) != null;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return this.<AbstractMAMap<K, V>>getParentMap().baseEntries.removeAllByValueCollection(
                    c, 
                    this.new HandlerImpl(CollectionModifications.<V>removeAll(c)));
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return this.<AbstractMAMap<K, V>>getParentMap().baseEntries.retainAllByValueCollection(
                    c, 
                    this.new HandlerImpl(CollectionModifications.<V>retainAll(c)));
        }
        
        @Override
        public MAValuesIterator<K, V> iterator() {
            return new IteratorImpl<K, V>(this);
        }
        
        protected static class IteratorImpl<K, V> implements MAValuesIterator<K, V> {
            
            private ValuesImpl<K, V> parentValues;
            
            private transient ElementListener<V> elementListener;
            
            private transient ValuesElementListener<K, V> valuesElementListener;
            
            private final BaseEntryIterator<K, V> beIterator;
            
            protected IteratorImpl(ValuesImpl<K, V> parentValues) {
                this.parentValues = Arguments.mustNotBeNull("parentValues", parentValues);
                this.beIterator = parentValues.getParentMap().baseEntries.iterator();
            }
            
            @SuppressWarnings("unchecked")
            protected final <T extends ValuesImpl<K, V>> T getParentValues() {
                return (T)this.parentValues;
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public void addElementListener(ElementListener<? super V> listener) {
                this.elementListener = ElementListener.combine(
                            this.elementListener, 
                            (ElementListener<V>)listener);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void removeElementListener(ElementListener<? super V> listener) {
                this.elementListener = ElementListener.remove(
                            this.elementListener, 
                            (ElementListener<V>)listener);
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public void addValuesElementListener(ValuesElementListener<? super K, ? super V> listener) {
                this.valuesElementListener = ValuesElementListener.combine(
                            this.valuesElementListener, 
                            (ValuesElementListener<K, V>)listener);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void removeValuesElementListener(ValuesElementListener<? super K, ? super V> listener) {
                this.valuesElementListener = ValuesElementListener.remove(
                            this.valuesElementListener, 
                            (ValuesElementListener<K, V>)listener);
            }

            protected void executeModifying(ValuesElementEvent<K, V> e) {
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

            protected void executeModified(ValuesElementEvent<K, V> e) {
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

            protected void onModifying(ValuesElementEvent<K, V> e) throws Throwable {
                
            }
            
            protected void onModified(ValuesElementEvent<K, V> e) throws Throwable {
                
            }
            
            protected void raiseModifying(ValuesElementEvent<K, V> e) throws Throwable {
                Throwable finalThrowable = null;
                try {
                    ElementListener<V> elementListener = this.elementListener;
                    if (elementListener != null) {
                        e
                        .getAttributeContext(AttributeScope.LOCAL)
                        .addAttribute(AK_ELEMENT_LISTENER, elementListener);
                        elementListener.modifying(e);
                    }
                } catch (Throwable ex) {
                    finalThrowable = ex;
                }
                try {
                    ValuesElementListener<K, V> valuesElementListener = this.valuesElementListener;
                    if (valuesElementListener != null) {
                        e
                        .getAttributeContext(AttributeScope.LOCAL)
                        .addAttribute(AK_VALUES_ELEMENT_LISTENER, valuesElementListener);
                        valuesElementListener.modifying(e);
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
            protected void raiseModified(ValuesElementEvent<K, V> e) throws Throwable {
                Throwable finalThrowable = null;
                try {
                    ValuesElementListener<K, V> valuesElementListener = 
                        (ValuesElementListener<K, V>)
                        e
                        .getAttributeContext(AttributeScope.LOCAL)
                        .removeAttribute(AK_VALUES_ELEMENT_LISTENER);
                    if (valuesElementListener != null) {
                        valuesElementListener.modified(e);
                    }
                } catch (Throwable ex) {
                    finalThrowable = ex;
                }
                try {
                    ElementListener<V> elementListener = 
                        (ElementListener<V>)
                        e
                        .getAttributeContext(AttributeScope.LOCAL)
                        .removeAttribute(AK_ELEMENT_LISTENER);
                    if (elementListener != null) {
                        elementListener.modified(e);
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
            
            protected void bubbleModifying(final ValuesElementEvent<K, V> e) {
                ValuesImpl<K, V> parentValues = this.parentValues;
                ValuesElementEvent<K, V> bubbledEvent = new ValuesElementEvent<>(
                        parentValues, 
                        new Cause(e), 
                        null,
                        null);
                parentValues.executeModifying(bubbledEvent);
            }
            
            protected void bubbleModified(ValuesElementEvent<K, V> e) {
                ValuesImpl<K, V> parentValues = this.parentValues;
                ValuesElementEvent<K, V> bubbledEvent = e.getBubbledEvent(parentValues);
                parentValues.executeModified(bubbledEvent);
            }

            @Override
            public boolean hasNext() {
                return this.beIterator.hasNext();
            }

            @Override
            public V next() {
                return this.beIterator.next().getValue();
            }

            @Override
            public void remove() {
                this.beIterator.remove(this.new HandlerImpl());
            }
            
            @Override
            public boolean isReadWriteLockSupported() {
                return this.parentValues.isReadWriteLockSupported();
            }

            @Override
            public UnifiedComparator<? super V> unifiedComparator() {
                return this.parentValues.unifiedComparator();
            }

            @Override
            public CollectionViewInfos.Iterator viewInfo() {
                return CollectionViewInfos.iterator();
            }

            private class HandlerImpl extends AbstractValuesHandlerImpl<K, V> {

                public HandlerImpl() {
                    super(IteratorModifications.<V>remove());
                }

                @Override
                protected Object eventSource() {
                    return IteratorImpl.this;
                }

                @Override
                protected void modifying(ValuesElementEvent<K, V> e) {
                    IteratorImpl.this.executeModifying(e);
                }

                @Override
                protected void modified(ValuesElementEvent<K, V> e) {
                    IteratorImpl.this.executeModified(e);
                }
                
            }
            
        }
        
        private class HandlerImpl extends AbstractValuesHandlerImpl<K, V> {

            public HandlerImpl(Modification<V> modification) {
                super(modification);
            }

            @Override
            protected Object eventSource() {
                return ValuesImpl.this;
            }

            @Override
            protected void modifying(ValuesElementEvent<K, V> e) {
                ValuesImpl.this.executeModifying(e);
            }

            @Override
            protected void modified(ValuesElementEvent<K, V> e) {
                ValuesImpl.this.executeModified(e);
            }
            
        }
        
    }
    
    protected abstract static class AbstractEntryImpl<K, V> 
    extends AbstractXMap.AbstractEntryImpl<K, V> 
    implements MAEntry<K, V> {
        
        private AbstractMAMap<K, V> parentMap;
        
        private transient ElementListener<V> elementListener;
        
        private transient EntryElementListener<K, V> entryElementListener;
        
        protected AbstractEntryImpl(
                AbstractMAMap<K, V> parentMap, 
                BaseEntry<K, V> baseEntry) throws NoEntryException {
            super (baseEntry);
            this.parentMap = Arguments.mustNotBeNull("parentMap", parentMap);
        }
        
        @Override
        public V setValue(V value) {
            return this.baseEntry.setValue(value, this.new HandlerImpl4Entry(EntryModifications.<V>set(value)));
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractMAMap<K, V>> T getParentMap() {
            return (T)this.parentMap;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void addElementListener(ElementListener<? super V> listener) {
            this.elementListener = ElementListener.combine(
                        this.elementListener, 
                        (ElementListener<V>)listener);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removeElementListener(ElementListener<? super V> listener) {
            this.elementListener = ElementListener.remove(
                        this.elementListener, 
                        (ElementListener<V>)listener);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void addEntryElementListener(EntryElementListener<? super K, ? super V> listener) {
            this.entryElementListener = EntryElementListener.combine(
                        this.entryElementListener, 
                        (EntryElementListener<K, V>)listener);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removeEntryElementListener(EntryElementListener<? super K, ? super V> listener) {
            this.entryElementListener = EntryElementListener.remove(
                        this.entryElementListener, 
                        (EntryElementListener<K, V>)listener);
        }

        protected void executeModifying(EntryElementEvent<K, V> e) {
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

        protected void executeModified(EntryElementEvent<K, V> e) {
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
        
        protected void onModifying(EntryElementEvent<K, V> e) throws Throwable {
            
        }
        
        protected void onModified(EntryElementEvent<K, V> e) throws Throwable {
            
        }
        
        protected void raiseModifying(EntryElementEvent<K, V> e) throws Throwable {
            Throwable finalThrowable = null;
            try {
                ElementListener<V> elementListener = this.elementListener;
                if (elementListener != null) {
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .addAttribute(AK_ELEMENT_LISTENER, elementListener);
                    elementListener.modifying(e);
                }
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                EntryElementListener<K, V> entryElementListener = this.entryElementListener;
                if (entryElementListener != null) {
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .addAttribute(AK_ENTRY_ELEMENT_LISTENER, entryElementListener);
                    entryElementListener.modifying(e);
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
        protected void raiseModified(EntryElementEvent<K, V> e) throws Throwable {
            Throwable finalThrowable = null;
            try {
                EntryElementListener<K, V> entryElementListener = 
                    (EntryElementListener<K, V>)
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .removeAttribute(AK_ENTRY_ELEMENT_LISTENER);
                if (entryElementListener != null) {
                    entryElementListener.modified(e);
                }
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                ElementListener<V> elementListener = 
                    (ElementListener<V>)
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .removeAttribute(AK_ELEMENT_LISTENER);
                if (elementListener != null) {
                    elementListener.modified(e);
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

        protected void bubbleModifying(final EntryElementEvent<K, V> e) {
            AbstractMAMap<K, V> parentMap = this.parentMap;
            MapElementEvent<K, V> bubbledEvent = new MapElementEvent<>(
                    parentMap,
                    new Cause(e),
                    version -> e.getKey(),
                    version -> e.getElement(version)
            );
            parentMap.executeModifying(bubbledEvent);
        }

        protected void bubbleModified(ElementEvent<V> e) {
            AbstractMAMap<K, V> parentMap = this.parentMap;
            MapElementEvent<K, V> bubbleEvent = e.getBubbledEvent(parentMap);
            parentMap.executeModified(bubbleEvent);
        }

        private class HandlerImpl4Entry implements BaseEntriesHandler<K, V> {
            
            private EntryModifications.SetByValue<V> modification;
            
            public HandlerImpl4Entry(EntryModifications.SetByValue<V> modification) {
                this.modification = modification;
            }
            
            @Override
            public Object createChangingArgument(K oldKey, V oldValue, K newKey, V newValue) {
                return EntryElementEvent.createReplaceEvent(
                        AbstractEntryImpl.this, 
                        this.modification, 
                        oldValue,
                        newValue,
                        oldKey);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void changing(K oldKey, V oldValue, K newKey, V newValue, Object argument) {
                assert oldKey == newKey;
                EntryElementEvent<K, V> event = (EntryElementEvent<K, V>)argument;
                AbstractEntryImpl.this.executeModifying(event);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void changed(K oldKey, V oldValue, K newKey, V newValue, Object argument) {
                EntryElementEvent<K, V> event = (EntryElementEvent<K, V>)argument;
                AbstractEntryImpl.this.executeModified(event);
            }

            @Override
            public Object createRemovingArgument(K oldKey, V oldValue) {
                return EntryElementEvent.createDetachEvent(
                        AbstractEntryImpl.this, 
                        this.modification,
                        oldValue,
                        oldKey);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void removing(K oldKey, V oldValue, Object argument) {
                EntryElementEvent<K, V> event = (EntryElementEvent<K, V>)argument;
                AbstractEntryImpl.this.executeModifying(event);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void removed(K oldKey, V oldValue, Object argument) {
                EntryElementEvent<K, V> event = (EntryElementEvent<K, V>)argument;
                AbstractEntryImpl.this.executeModified(event);
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public void setPreThrowable(Object argument, Throwable throwable) {
                EntryElementEvent<K, V> event = (EntryElementEvent<K, V>)argument;
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
        
    }
    
    protected static class EntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected EntryImpl(AbstractMAMap<K, V> parentMap, BaseEntry<K, V> baseEntry) 
        throws NoEntryException {
            super(parentMap, baseEntry);
        }

        @Override
        public MapViewInfos.Entry viewInfo() {
            return MapViewInfos.entry();
        }
    }
    
    protected static class EntryOfKeyImpl<K, V> extends AbstractEntryImpl<K, V> {
        
        private MapViewInfos.EntryOfKey viewInfo;

        protected EntryOfKeyImpl(AbstractMAMap<K, V> parentMap, K key) throws NoEntryException {
            super(parentMap, parentMap.baseEntries.getBaseEntry(key));
            this.viewInfo = MapViewInfos.entryOfKey(key);
        }

        @Override
        public ViewInfo viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class EntryOfValueImpl<K, V> extends AbstractEntryImpl<K, V> {
        
        private MapViewInfos.EntryOfValue viewInfo;

        protected EntryOfValueImpl(AbstractMAMap<K, V> parentMap, V value) throws NoEntryException {
            super(parentMap, parentMap.baseEntries.getBaseEntryByValue(value));
            this.viewInfo = MapViewInfos.entryOfValue(value);
        }

        @Override
        public ViewInfo viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    private class HandlerImpl4Map implements BaseEntriesHandler<K, V> {
        
        private final MapModification<K, V> modification;

        public HandlerImpl4Map(MapModification<K, V> modification) {
            this.modification = modification;
        }
        
        @Override
        public Object createAddingArgument(K key, V value) {
            return MapElementEvent.createAttachEvent(
                    AbstractMAMap.this, 
                    this.modification,
                    key,
                    value);
        }

        @Override
        public void adding(K key, V value, Object argument) {
            @SuppressWarnings("unchecked")
            MapElementEvent<K, V> event = (MapElementEvent<K, V>)argument; 
            AbstractMAMap.this.executeModifying(event);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void added(K key, V value, Object argument) {
            MapElementEvent<K, V> event = (MapElementEvent<K, V>)argument;
            AbstractMAMap.this.executeModified(event);
        }
        
        @Override
        public Object createChangingArgument(K oldKey, V oldValue, K newKey,
                V newValue) {
            return MapElementEvent.createReplaceEvent(
                    AbstractMAMap.this, 
                    this.modification,
                    oldKey,
                    newKey,
                    oldValue, 
                    newValue);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void changing(K oldKey, V oldValue, K newKey, V newValue, Object argument) {
            MapElementEvent<K, V> event = (MapElementEvent<K, V>)argument;
            AbstractMAMap.this.executeModifying(event);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void changed(K oldKey, V oldValue, K newKey, V newValue, Object argument) {
            MapElementEvent<K, V> event = (MapElementEvent<K, V>)argument;
            AbstractMAMap.this.executeModified(event);
        }
        
        @Override
        public Object createRemovingArgument(K oldKey, V oldValue) {
            return MapElementEvent.createDetachEvent(
                    AbstractMAMap.this, 
                    this.modification, 
                    oldKey,
                    oldValue);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removing(K oldKey, V oldValue, Object argument) {
            MapElementEvent<K, V> event = (MapElementEvent<K, V>)argument;
            AbstractMAMap.this.executeModifying(event);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removed(K oldKey, V oldValue, Object argument) {
            MapElementEvent<K, V> event = (MapElementEvent<K, V>)argument;
            AbstractMAMap.this.executeModified(event);
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
    
    private static abstract class AbstractEntryHandlerImpl<K, V> implements BaseEntriesHandler<K, V> {
        
        private final Modification<Entry<K, V>> modification;

        public AbstractEntryHandlerImpl(Modification<Entry<K, V>> modification) {
            this.modification = modification;
        }
        
        protected abstract Object eventSource();
        
        protected abstract void modifying(ElementEvent<Entry<K, V>> e);
        
        protected abstract void modified(ElementEvent<Entry<K, V>> e);

        @Override
        public final Object createRemovingArgument(K oldKey, V oldValue) {
            return ElementEvent.createDetachEvent(
                    this.eventSource(), 
                    this.modification, 
                    entry(oldKey, oldValue));
        }

        @SuppressWarnings("unchecked")
        @Override
        public final void removing(K oldKey, V oldValue, Object argument) {
            ElementEvent<Entry<K, V>> event = (ElementEvent<Entry<K, V>>)argument;
            this.modifying(event);
        }

        @SuppressWarnings("unchecked")
        @Override
        public final void removed(K oldKey, V oldValue, Object argument) {
            ElementEvent<Entry<K, V>> event = (ElementEvent<Entry<K, V>>)argument;
            this.modified(event);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void setPreThrowable(Object argument, Throwable throwable) {
            ElementEvent<Entry<K, V>> event = (ElementEvent<Entry<K, V>>)argument;
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
    
    private static abstract class AbstractValuesHandlerImpl<K, V> implements BaseEntriesHandler<K, V> {
        
        private final Modification<V> modification;
        
        public AbstractValuesHandlerImpl(Modification<V> modification) {
            this.modification = modification;
        }
        
        protected abstract Object eventSource();
        
        protected abstract void modifying(ValuesElementEvent<K, V> e);
        
        protected abstract void modified(ValuesElementEvent<K, V> e);

        @Override
        public Object createRemovingArgument(K oldKey, V oldValue) {
            return ValuesElementEvent.createDetachEvent(
                    this.eventSource(), 
                    this.modification, 
                    oldValue,
                    oldKey);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removing(K oldKey, V oldValue, Object argument) {
            ValuesElementEvent<K, V> event = (ValuesElementEvent<K, V>)argument;
            this.modifying(event);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removed(K oldKey, V oldValue, Object argument) {
            ValuesElementEvent<K, V> event = (ValuesElementEvent<K, V>)argument;
            this.modified(event);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void setPreThrowable(Object argument, Throwable throwable) {
            ValuesElementEvent<K, V> event = (ValuesElementEvent<K, V>)argument;
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
    
    private static <K, V> Entry<K, V> entry(final K key, final V value) {
        
        return new Entry<K, V>() {

            @Override
            public K getKey() {
                return key;
            }

            @Override
            public V getValue() {
                return value;
            }

            @Override
            public V setValue(V value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int hashCode() {
                return 
                (key == null ? 0 : key.hashCode()) ^
                (value == null ? 0 : value.hashCode());
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof Entry<?, ?>)) {
                    return false;
                }
                Entry<?, ?> other = (Entry<?, ?>)obj;
                Object otherKey = other.getKey();
                Object otherValue = other.getValue();
                return 
                    (key == otherKey || (key != null && key.equals(otherKey))) &&
                    (value == otherValue || (value != null && value.equals(otherValue)));
            }

            @Override
            public String toString() {
                return key + "=" + value;
            }
            
        };
    }
    
    private class SpecialHandlerFactoryImpl implements BaseEntriesSpecialHandlerFactory<K, V> {

        private static final long serialVersionUID = -4121831778722720450L;

        @Override
        public BaseEntriesHandler<K, V> createSuspendingHandler(K key) {
            return AbstractMAMap.this.new HandlerImpl4Map(
                    MapModifications.suspendViaFrozenContext(key)
            );
        }

        @Override
        public BaseEntriesHandler<K, V> createResumingHandler() {
            return AbstractMAMap.this.new HandlerImpl4Map(
                    MapModifications.resumeViaFrozenContext()
            );
        }

        @Override
        public BaseEntriesHandler<K, V> createInversedSuspendingHandler(V value) {
            return AbstractMAMap.this.new HandlerImpl4Map(
                    MapModifications.suspendViaInversedFrozenContext(value)
            );
        }

        @Override
        public BaseEntriesHandler<K, V> createInversedResumingHandler() {
            return AbstractMAMap.this.new HandlerImpl4Map(
                    MapModifications.resumeViaInversedFrozenContext()
            );
        }
        
    }
}
