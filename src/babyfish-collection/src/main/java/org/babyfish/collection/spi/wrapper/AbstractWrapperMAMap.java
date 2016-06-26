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
package org.babyfish.collection.spi.wrapper;

import java.util.Comparator;

import org.babyfish.collection.MAHashMap;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XMap;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.event.EntryElementEvent;
import org.babyfish.collection.event.EntryElementListener;
import org.babyfish.collection.event.KeySetElementEvent;
import org.babyfish.collection.event.KeySetElementListener;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.collection.event.ValuesElementEvent;
import org.babyfish.collection.event.ValuesElementListener;
import org.babyfish.collection.spi.base.NoEntryException;
import org.babyfish.collection.spi.wrapper.event.AbstractElementEventDispatcher;
import org.babyfish.collection.spi.wrapper.event.AbstractEntryElementEventDispatcher;
import org.babyfish.collection.spi.wrapper.event.AbstractKeySetElementEventDispatcher;
import org.babyfish.collection.spi.wrapper.event.AbstractMapElementEventDispatcher;
import org.babyfish.collection.spi.wrapper.event.AbstractValuesElementEventDispatcher;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.collection.viewinfo.MapViewInfos;
import org.babyfish.data.ModificationException;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.Cause;

/**
 * @author Tao Chen
 */
public abstract class AbstractWrapperMAMap<K, V> extends AbstractWrapperXMap<K, V> implements MAMap<K, V> {
    
    private static final Object AK_ELEMENT_LISTENER = new Object();
    
    private static final Object AK_MAP_ELEMENT_LISTENER = new Object();
    
    private static final Object AK_KEY_SET_ELEMENT_LISTENER = new Object();
    
    private static final Object AK_VALUES_ELEMENT_LISTENER = new Object();
    
    private static final Object AK_ENTRY_ELEMENT_LISTENER = new Object();

    private transient MapElementListener<K, V> mapElementListener;

    protected AbstractWrapperMAMap(MAMap<K, V> base) {
        super(base);
    }

    protected AbstractWrapperMAMap(
            AbstractWrapperMAMap<K, V> parent, 
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
    protected AbstractWrapperMAMap() {
        
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
            return new EntryOfKeyImpl<>(this, key);
        } catch (NoEntryException ex) {
            return null;
        }
    }
    
    @Override
    public MAEntry<K, V> entryOfValue(V value) {
        try {
            return new EntryOfValueImpl<>(this, value);
        } catch (NoEntryException ex) {
            return null;
        }
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
        AbstractWrapperMAMap<K, V> parent = this.getParent();
        if (parent != null) {
            MapElementEvent<K, V> bubbledEvent = new MapElementEvent<>(
                    parent, 
                    new Cause(e), 
                    null,
                    null);
            parent.executeModifying(bubbledEvent);
        }
    }

    protected void bubbleModified(MapElementEvent<K, V> e) {
        AbstractWrapperMAMap<K, V> parent = this.getParent();
        if (parent != null) {
            MapElementEvent<K, V> bubbledEvent = e.getBubbledEvent(parent);
            parent.executeModified(bubbledEvent);
        }
    }
    
    @Override
    protected RootData<K, V> createRootData() {
        return new RootData<K, V>();
    }
    
    @Deprecated
    @Override
    protected final XMap<K, V> createBaseView(
            XMap<K, V> parentBase,
            ViewInfo viewInfo) {
        return this.createBaseView((MAMap<K, V>)parentBase, viewInfo);
    }
    
    protected MAMap<K, V> createBaseView(
            MAMap<K, V> parentBase,
            ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }
    
    @Override
    protected AbstractMapElementEventDispatcher<K, V> createEventDispatcher() {
        return new AbstractMapElementEventDispatcher<K, V>(this) {
            
            @Override
            protected boolean isDispatchable() {
                return this
                        .<AbstractWrapperMAMap<K, V>>getOwner()
                        .getRootData()
                        .isDispatchable();
            }

            @Override
            protected void executePreDispatchedEvent(MapElementEvent<K, V> dispatchedEvent) {
                AbstractWrapperMAMap<K, V> owner = this.getOwner();
                owner.executeModifying(dispatchedEvent);
            }

            @Override
            protected void executePostDispatchedEvent(MapElementEvent<K, V> dispatchedEvent) {
                AbstractWrapperMAMap<K, V> owner = this.getOwner();
                owner.executeModified(dispatchedEvent);
            }
        };
    }

    protected static class EntrySetImpl<K, V> extends AbstractWrapperXMap.EntrySetImpl<K, V> implements MAEntrySetView<K, V> {

        private transient ElementListener<Entry<K, V>> elementListener;
        
        protected EntrySetImpl(AbstractWrapperMAMap<K, V> parentMap) {
            super(parentMap);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void addElementListener(ElementListener<? super Entry<K, V>> listener) {
            this.elementListener = ElementListener.combine(
                        this.elementListener, 
                        (ElementListener<Entry<K, V>>)listener);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removeElementListener(ElementListener<? super Entry<K, V>> listener) {
            this.elementListener = ElementListener.remove(
                        this.elementListener, 
                        (ElementListener<Entry<K, V>>)listener);
        }

        @Override
        public MAEntrySetIterator<K, V> iterator() {
            return new IteratorImpl<K, V>(this);
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
            AbstractWrapperMAMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> bubbledEvent = new MapElementEvent<>(
                    parentMap, 
                    new Cause(e),
                    version -> e.getElement(version).getKey(),
                    version -> e.getElement(version).getValue()
            );
            parentMap.executeModifying(bubbledEvent);
        }
        
        protected void bubbleModified(ElementEvent<Entry<K, V>> e) {
            AbstractWrapperMAMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> bubbledEvent = e.getBubbledEvent(parentMap);
            parentMap.executeModified(bubbledEvent);
        }

        @Override
        protected AbstractElementEventDispatcher<Entry<K, V>> createEventDispatcher() {
            return new AbstractElementEventDispatcher<Entry<K,V>>(this) {

                @Override
                protected boolean isDispatchable() {
                    return this
                            .<EntrySetImpl<K, V>>getOwner()
                            .getParentMap()
                            .getRootData()
                            .isDispatchable();
                }

                @Override
                protected void executePreDispatchedEvent(ElementEvent<Entry<K, V>> dispatchedEvent) {
                    EntrySetImpl<K, V> owner = this.getOwner();
                    owner.executeModifying(dispatchedEvent);
                }

                @Override
                protected void executePostDispatchedEvent(ElementEvent<Entry<K, V>> dispatchedEvent) {
                    EntrySetImpl<K, V> owner = this.getOwner();
                    owner.executeModified(dispatchedEvent);
                }

            };
        }

        protected static class IteratorImpl<K, V> 
        extends AbstractWrapperXMap.EntrySetImpl.IteratorImpl<K, V> 
        implements MAEntrySetIterator<K, V> {

            private ElementListener<Entry<K, V>> elementListener;
            
            protected IteratorImpl(EntrySetImpl<K, V> parent) {
                super(parent);
            }
            
            public MAEntry<K, V> next() {
                return new EntryImpl<K, V>(this);
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public void addElementListener(ElementListener<? super Entry<K, V>> listener) {
                this.elementListener = ElementListener.combine(
                            this.elementListener, 
                            (ElementListener<Entry<K, V>>)listener);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void removeElementListener(ElementListener<? super Entry<K, V>> listener) {
                this.elementListener = ElementListener.remove(
                            this.elementListener, 
                            (ElementListener<Entry<K, V>>)listener);
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
                EntrySetImpl<K, V> parent = this.getParent();
                ElementEvent<Entry<K, V>> bubbledEvent = new ElementEvent<>(
                        parent, 
                        new Cause(e), 
                        null);
                parent.executeModifying(bubbledEvent);
            }
            
            protected void bubbleModified(ElementEvent<Entry<K, V>> e) {
                EntrySetImpl<K, V> parent = this.getParent();
                ElementEvent<Entry<K, V>> bubbledEvent = e.getBubbledEvent(parent);
                parent.executeModified(bubbledEvent);
            }

            @Override
            protected AbstractElementEventDispatcher<Entry<K, V>> createEventDispatcher() {
                return new AbstractElementEventDispatcher<Entry<K,V>>(this) {

                    @Override
                    protected boolean isDispatchable() {
                        return this
                                .<IteratorImpl<K, V>>getOwner()
                                .<EntrySetImpl<K, V>>getParent()
                                .getParentMap()
                                .getRootData()
                                .isDispatchable();
                    }

                    @Override
                    protected void executePreDispatchedEvent(ElementEvent<Entry<K, V>> dispatchedEvent) {
                        IteratorImpl<K, V> owner = this.getOwner();
                        owner.executeModifying(dispatchedEvent);
                    }

                    @Override
                    protected void executePostDispatchedEvent(ElementEvent<Entry<K, V>> dispatchedEvent) {
                        IteratorImpl<K, V> owner = this.getOwner();
                        owner.executeModified(dispatchedEvent);
                    }
                };
            }
        }
        
    }
    
    protected static abstract class AbstractKeySetImpl<K, V> 
    extends AbstractWrapperXMap.AbstractKeySetImpl<K, V> 
    implements MAKeySetView<K, V> {

        private transient ElementListener<K> elementListener;
        
        private transient KeySetElementListener<K, V> keySetElementListener;
        
        protected AbstractKeySetImpl(
                AbstractKeySetImpl<K, V> parent, ViewInfo viewInfo) {
            super(parent,  viewInfo);
        }

        protected AbstractKeySetImpl(
                AbstractWrapperMAMap<K, V> parentMap,
                ViewInfo viewInfo) {
            super(parentMap, viewInfo);
        }
        
        @Override
        public MAKeySetIterator<K, V> iterator() {
            return new IteratorImpl<K, V>(this);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void addElementListener(ElementListener<? super K> listener) {
            this.elementListener = ElementListener.combine(
                        this.elementListener, 
                        (ElementListener<K>)listener);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removeElementListener(ElementListener<? super K> listener) {
            this.elementListener = ElementListener.remove(
                        this.elementListener, 
                        (ElementListener<K>)listener);
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
        
        protected void executeModifying(KeySetElementEvent<K, V> e) {
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

        protected void executeModified(KeySetElementEvent<K, V> e) {
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

        protected void onModifying(KeySetElementEvent<K, V> e) throws Throwable {
            
        }

        protected void onModified(KeySetElementEvent<K, V> e) throws Throwable {
            
        }

        protected void raiseModifying(KeySetElementEvent<K, V> e) throws Throwable {
            Throwable finalThrowable = null;
            try {
                ElementListener<K> elementListener = this.elementListener;
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
                ElementListener<K> elementListener = 
                    (ElementListener<K>)
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
        
        protected void bubbleModifying(final KeySetElementEvent<K, V> e) {
            AbstractKeySetImpl<K, V> parent = this.getParent();
            if (parent != null) {
                KeySetElementEvent<K, V> bubbledEvent =
                        new KeySetElementEvent<>(
                                parent, new Cause(e), null, null
                        );
                parent.executeModifying(bubbledEvent);
            } else {
                AbstractWrapperMAMap<K, V> parentMap = this.getParentMap();
                MapElementEvent<K, V> bubbledEvent = new MapElementEvent<>(
                        parentMap, 
                        new Cause(e),
                        version -> e.getElement(version),
                        version -> e.getValue()
                );
                parentMap.executeModifying(bubbledEvent);
            }
        }

        protected void bubbleModified(KeySetElementEvent<K, V> e) {
            AbstractKeySetImpl<K, V> parent = this.getParent();
            if (parent != null) {
                KeySetElementEvent<K, V> bubbledEvent = e.getBubbledEvent(parent);
                parent.executeModified(bubbledEvent);
            } else {
                AbstractWrapperMAMap<K, V> parentMap = this.getParentMap();
                MapElementEvent<K, V> bubbledEvent = e.getBubbledEvent(parentMap);
                parentMap.executeModified(bubbledEvent);
            }
        }
        
        @Deprecated
        @Override
        protected final XKeySetView<K> createBaseView(
                XMap<K, V> baseMap, ViewInfo viewInfo) {
            return this.createBaseView((MAMap<K, V>)baseMap, viewInfo);
        }
        
        protected MAKeySetView<K, V> createBaseView(
                MAMap<K, V> baseMap, ViewInfo viewInfo) {
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }

        @SuppressWarnings("unchecked")
        @Deprecated
        @Override
        protected final XKeySetView<K> createBaseView(
                XKeySetView<K> parentBase, ViewInfo viewInfo) {
            return this.createBaseView((MAKeySetView<K, V>)parentBase, viewInfo);
        }
        
        protected MAKeySetView<K, V> createBaseView(
                MAKeySetView<K, V> parentBase, ViewInfo viewInfo) {
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }

        @Override
        protected AbstractKeySetElementEventDispatcher<K, V> createEventDispatcher() {
            return new AbstractKeySetElementEventDispatcher<K, V>(this) {

                @Override
                protected boolean isDispatchable() {
                    return 
                            this
                            .<AbstractKeySetImpl<K, V>>getOwner()
                            .getParentMap()
                            .getRootData()
                            .isDispatchable();
                }

                @Override
                protected void executePreDispatchedEvent(KeySetElementEvent<K, V> dispatchedEvent) {
                    this
                    .<AbstractKeySetImpl<K, V>>getOwner()
                    .executeModifying(dispatchedEvent);
                }

                @Override
                protected void executePostDispatchedEvent(KeySetElementEvent<K, V> dispatchedEvent) {
                    this
                    .<AbstractKeySetImpl<K, V>>getOwner()
                    .executeModified(dispatchedEvent);
                }
            };
        }

        protected static abstract class AbstractIteratorImpl<K, V> 
        extends AbstractWrapperXMap.KeySetImpl.AbstractIteratorImpl<K, V> 
        implements MAKeySetIterator<K, V> {

            private transient ElementListener<K> elementListener;
            
            private transient KeySetElementListener<K, V> keySetElementListener;
            
            protected AbstractIteratorImpl(
                    AbstractKeySetImpl<K, V> parent, 
                    ViewInfo initialViewInfo) {
                super(parent, initialViewInfo);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void addElementListener(ElementListener<? super K> listener) {
                this.elementListener = ElementListener.combine(
                            this.elementListener, 
                            (ElementListener<K>)listener);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void removeElementListener(ElementListener<? super K> listener) {
                this.elementListener = ElementListener.remove(
                            this.elementListener, 
                            (ElementListener<K>)listener);
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
            
            protected void executeModifying(KeySetElementEvent<K, V> e) {
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

            protected void executeModified(KeySetElementEvent<K, V> e) {
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

            protected void onModifying(KeySetElementEvent<K, V> e) throws Throwable {
                
            }

            protected void onModified(KeySetElementEvent<K, V> e) throws Throwable {
                
            }

            protected void raiseModifying(KeySetElementEvent<K, V> e) throws Throwable {
                Throwable finalThrowable = null;
                try {
                    ElementListener<K> elementListener = this.elementListener;
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
                    ElementListener<K> elementListener = 
                        (ElementListener<K>)
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
            
            protected void bubbleModifying(KeySetElementEvent<K, V> e) {
                AbstractKeySetImpl<K, V> parent = this.getParent();
                KeySetElementEvent<K, V> bubbledEvent = new KeySetElementEvent<>(
                        parent, 
                        new Cause(e),
                        null,
                        null);
                parent.executeModifying(bubbledEvent);
            }

            protected void bubbleModified(KeySetElementEvent<K, V> e) {
                AbstractKeySetImpl<K, V> parent = this.getParent();
                KeySetElementEvent<K, V> bubbledEvent = e.getBubbledEvent(parent);
                parent.executeModified(bubbledEvent);
            }

            @SuppressWarnings("unchecked")
            @Deprecated
            @Override
            protected final XIterator<K> createBaseView(XKeySetView<K> baseKeySet, ViewInfo viewInfo) {
                return this.createBaseView(
                        (MAKeySetView<K, V>)baseKeySet, viewInfo);
            }

            protected abstract MAKeySetIterator<K, V> createBaseView(
                    MAKeySetView<K, V> baseKeySet,
                    ViewInfo viewInfo);

            @Override
            protected AbstractKeySetElementEventDispatcher<K, V> createEventDispatcher() {
                return new AbstractKeySetElementEventDispatcher<K, V>(this) {

                    @Override
                    protected boolean isDispatchable() {
                        return 
                                this
                                .<AbstractIteratorImpl<K, V>>getOwner()
                                .getParent()
                                .getParentMap()
                                .getRootData()
                                .isDispatchable();
                    }

                    @Override
                    protected void executePreDispatchedEvent(
                            KeySetElementEvent<K, V> dispatchedEvent) {
                        this
                        .<AbstractIteratorImpl<K, V>>getOwner()
                        .executeModifying(dispatchedEvent);
                    }

                    @Override
                    protected void executePostDispatchedEvent(
                            KeySetElementEvent<K, V> dispatchedEvent) {
                        this
                        .<AbstractIteratorImpl<K, V>>getOwner()
                        .executeModified(dispatchedEvent);
                    }
                };
            }
            
        }
        
        protected static class IteratorImpl<K, V> extends AbstractIteratorImpl<K, V> {

            protected IteratorImpl(
                    AbstractKeySetImpl<K, V> parent) {
                super(parent, CollectionViewInfos.iterator());
            }

            @Override
            protected MAKeySetIterator<K, V> createBaseView(
                    MAKeySetView<K, V> baseKeySet,
                    ViewInfo viewInfo) {
                if (viewInfo instanceof CollectionViewInfos.Iterator) {
                    return baseKeySet.iterator();
                }
                throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
            }
            
        }
        
    }
    
    protected static class KeySetImpl<K, V> extends AbstractKeySetImpl<K, V> {

        protected KeySetImpl(AbstractWrapperMAMap<K, V> parentMap) {
            super(parentMap, MapViewInfos.keySet());
        }

        @Override
        protected MAKeySetView<K, V> createBaseView(MAMap<K, V> baseMap, ViewInfo viewInfo) {
            if (viewInfo instanceof MapViewInfos.KeySet) {
                return baseMap.keySet();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
    }
    
    protected static class ValuesImpl<K, V> extends AbstractWrapperXMap.ValuesImpl<K, V> implements MAValuesView<K, V> {

        private transient ElementListener<V> elementListener;
        
        private transient ValuesElementListener<K, V> valuesElementListener;
        
        protected ValuesImpl(AbstractWrapperXMap<K, V> parentMap) {
            super(parentMap);
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
        
        public MAValuesIterator<K, V> iterator() {
            return new IteratorImpl<K, V>(this);
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
            AbstractWrapperMAMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> event = new MapElementEvent<>(
                    parentMap, 
                    new Cause(e),
                    version -> e.getKey(),
                    version -> e.getElement(version)
            );
            parentMap.executeModifying(event);
        }
        
        protected void bubbleModified(ValuesElementEvent<K, V> e) {
            AbstractWrapperMAMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> bubbleEvent = e.getBubbledEvent(parentMap);
            parentMap.executeModified(bubbleEvent);
        }
        
        @Override
        protected AbstractValuesElementEventDispatcher<K, V> createEventDispatcher() {
            return new AbstractValuesElementEventDispatcher<K, V>(this) {

                @Override
                protected boolean isDispatchable() {
                    return 
                            this
                            .<ValuesImpl<K, V>>getOwner()
                            .getParentMap()
                            .getRootData()
                            .isDispatchable();
                }

                @Override
                protected void executePreDispatchedEvent(ValuesElementEvent<K, V> dispatchedEvent) {
                    this
                    .<ValuesImpl<K, V>>getOwner()
                    .executeModifying(dispatchedEvent);
                }

                @Override
                protected void executePostDispatchedEvent(ValuesElementEvent<K, V> dispatchedEvent) {
                    this
                    .<ValuesImpl<K, V>>getOwner()
                    .executeModified(dispatchedEvent);
                }
            };
        }

        protected static class IteratorImpl<K, V> 
        extends AbstractWrapperXMap.ValuesImpl.IteratorImpl<K, V>
        implements MAValuesIterator<K, V> {
            
            private ElementListener<V> elementListener;
            
            private ValuesElementListener<K, V> valuesElementListener;

            protected IteratorImpl(ValuesImpl<K, V> parent) {
                super(parent);
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
                ValuesImpl<K, V> parent = this.getParent();
                ValuesElementEvent<K, V> event = new ValuesElementEvent<>(
                        parent, 
                        new Cause(e), 
                        null, 
                        null);
                parent.executeModifying(event);
            }
            
            protected void bubbleModified(ValuesElementEvent<K, V> e) {
                ValuesImpl<K, V> parent = this.getParent();
                ValuesElementEvent<K, V> bubbleEvent = e.getBubbledEvent(parent);
                parent.executeModified(bubbleEvent);
            }

            @Override
            protected AbstractValuesElementEventDispatcher<K, V> createEventDispatcher() {
                return new AbstractValuesElementEventDispatcher<K, V>(this) {

                    @Override
                    protected boolean isDispatchable() {
                        return 
                                this
                                .<IteratorImpl<K, V>>getOwner()
                                .getParent()
                                .getParentMap()
                                .getRootData()
                                .isDispatchable();
                    }

                    @Override
                    protected void executePreDispatchedEvent(ValuesElementEvent<K, V> dispatchedEvent) {
                        this
                        .<IteratorImpl<K, V>>getOwner()
                        .executeModifying(dispatchedEvent);
                    }

                    @Override
                    protected void executePostDispatchedEvent(ValuesElementEvent<K, V> dispatchedEvent) {
                        this
                        .<IteratorImpl<K, V>>getOwner()
                        .executeModified(dispatchedEvent);
                    }
                };
            }
        }
    }
    
    protected static abstract class AbstractEntryImpl<K, V> extends AbstractWrapperXMap.AbstractEntryImpl<K, V> implements MAEntry<K, V> {
        
        private transient ElementListener<V> elementListener;
        
        private transient EntryElementListener<K, V> entryElementListener;
        
        protected AbstractEntryImpl(AbstractWrapperMAMap<K, V> parentMap, ViewInfo viewInfo) throws NoEntryException {
            super(parentMap, viewInfo);
        }
        
        protected AbstractEntryImpl(AbstractWrapperMAMap.EntrySetImpl.IteratorImpl<K, V> iterator) {
            super(iterator);
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
            Cause cause = new Cause(e);
            AbstractWrapperMAMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> bubbledEvent = new MapElementEvent<>(
                    parentMap,
                    cause,
                    version -> e.getKey(),
                    version -> e.getElement(version)
            );
            parentMap.executeModifying(bubbledEvent);
        }

        protected void bubbleModified(ElementEvent<V> e) {
            AbstractWrapperMAMap<K, V> parentMap = this.getParentMap();
            MapElementEvent<K, V> bubbledEvent = e.getBubbledEvent(parentMap);
            parentMap.executeModified(bubbledEvent);
        }

        @Deprecated
        @Override
        protected final XEntry<K, V> createBaseView(
                XMap<K, V> parentBase, ViewInfo viewInfo) {
            return this.createBaseView((MAMap<K, V>)parentBase, viewInfo);
        }
        
        protected MAEntry<K, V> createBaseView(
                MAMap<K, V> parentBase, ViewInfo viewInfo) {
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }

        @Override
        protected AbstractEntryElementEventDispatcher<K, V> createEventDispatcher() {
            return new AbstractEntryElementEventDispatcher<K, V>(this) {

                @Override
                protected boolean isDispatchable() {
                    return 
                            this
                            .<EntryImpl<K, V>>getOwner()
                            .getParentMap()
                            .getRootData()
                            .isDispatchable();
                }

                @Override
                protected void executePreDispatchedEvent(EntryElementEvent<K, V> dispatchedEvent) {
                    this
                    .<EntryImpl<K, V>>getOwner()
                    .executeModifying(dispatchedEvent);
                }

                @Override
                protected void executePostDispatchedEvent(EntryElementEvent<K, V> dispatchedEvent) {
                    this
                    .<EntryImpl<K, V>>getOwner()
                    .executeModified(dispatchedEvent);
                }
            };
        }
    }
    
    protected static class EntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected EntryImpl(AbstractWrapperMAMap.EntrySetImpl.IteratorImpl<K, V> iterator) {
            super(iterator);
        }
    }
    
    protected static final class EntryOfKeyImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected EntryOfKeyImpl(AbstractWrapperMAMap<K, V> parentMap, K key) throws NoEntryException {
            super(parentMap, MapViewInfos.entryOfKey(key));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected MAEntry<K, V> createBaseView(MAMap<K, V> parentBase, ViewInfo viewInfo) {
            if (viewInfo instanceof MapViewInfos.EntryOfKey) {
                K key = (K)((MapViewInfos.EntryOfKey)viewInfo).getKey();
                return parentBase.entryOfKey(key);
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
    }
    
    protected static final class EntryOfValueImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected EntryOfValueImpl(AbstractWrapperMAMap<K, V> parentMap, V value) throws NoEntryException {
            super(parentMap, MapViewInfos.entryOfValue(value));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected MAEntry<K, V> createBaseView(MAMap<K, V> parentBase, ViewInfo viewInfo) {
            if (viewInfo instanceof MapViewInfos.EntryOfValue) {
                V value = (V)((MapViewInfos.EntryOfValue)viewInfo).getValue();
                return parentBase.entryOfValue(value);
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
    }
    
    protected static class RootData<K, V> extends AbstractWrapperXMap.RootData<K, V> {

        private static final long serialVersionUID = -5887533689874942399L;
        
        public RootData() {
            
        }

        @Deprecated
        @Override
        protected final void setBase(XMap<K, V> base) {
            this.setBase((MAMap<K, V>)base);
        }
        
        protected void setBase(MAMap<K, V> base) {
            super.setBase(base);
        }

        @Override
        protected MAMap<K, V> createDefaultBase(
                UnifiedComparator<? super K> keyUnifiedComparator,
                UnifiedComparator<? super V> valueUnifiedComparator) {
            Comparator<? super K> comparator = keyUnifiedComparator.comparator();
            if (comparator != null) {
                return new MATreeMap<K, V>(comparator, valueUnifiedComparator);
            }
            return new MAHashMap<K, V>(
                    keyUnifiedComparator.equalityComparator(), 
                    valueUnifiedComparator);
        }
        
    }

}
