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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Map;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XMap;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.event.ElementModificationAware;
import org.babyfish.collection.spi.base.EntryEqualityComparator;
import org.babyfish.collection.spi.base.NoEntryException;
import org.babyfish.collection.spi.wrapper.event.AbstractElementEventDispatcher;
import org.babyfish.collection.spi.wrapper.event.AbstractEntryElementEventDispatcher;
import org.babyfish.collection.spi.wrapper.event.AbstractKeySetElementEventDispatcher;
import org.babyfish.collection.spi.wrapper.event.AbstractMapElementEventDispatcher;
import org.babyfish.collection.spi.wrapper.event.AbstractValuesElementEventDispatcher;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.collection.viewinfo.MapViewInfos;
import org.babyfish.data.AttributeContext;
import org.babyfish.data.DisablityManageable;
import org.babyfish.data.ModificationAware;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.StatefulObject;
import org.babyfish.validator.Validator;
import org.babyfish.validator.Validators;

/**
 * @author Tao Chen
 */
public abstract class AbstractWrapperXMap<K, V> extends StatefulObject implements DisablityManageable, XMap<K, V> {
    
    private RootData<K, V> rootData;
    
    private transient XMap<K, V> base;
    
    private transient AbstractWrapperXMap<K, V> parent;
    
    private transient int expectRootModCount;
    
    private transient ViewInfo viewInfo;
    
    private transient XEntrySetView<K, V> entrySet;
    
    private transient XKeySetView<K> keySet;
    
    private transient XValuesView<V> values;
    
    protected AbstractWrapperXMap(XMap<K, V> base) {
        Arguments.mustNotBeInstanceOfValue("this", this, View.class);
        Arguments.mustNotBeInstanceOfValue("base", base, View.class);
        RootData<K, V> rootData = this.createRootData();
        if (rootData == null) {
            throw new IllegalProgramException(CommonMessages.createRootDataMustReturnNonNull(this.getClass()));
        }
        rootData.rootWrapper = this;
        rootData.setBase(base);
        rootData.onInitialize();
        this.rootData = rootData;
        if (base != null) {
            this.setBase(base);
        }
    }
    
    protected AbstractWrapperXMap(
            AbstractWrapperXMap<K, V> parent, ViewInfo viewInfo) {
        Arguments.mustNotBeNull("parent", parent);
        Arguments.mustBeInstanceOfValue("this", this, View.class);
        Arguments.mustNotBeInstanceOfValue("this", this, Serializable.class);
        this.parent = parent;
        this.rootData = parent.rootData;
        this.viewInfo = viewInfo;
        XMap<K, V> base = parent.getBase(true);
        if (base != null) {
            base = this.createBaseView(base, viewInfo);
            if (!(base instanceof View)) {
                throw new IllegalProgramException(
                        CommonMessages.createBaseViewMustReturnView(this.getClass(), View.class)
                );
            }
            this.setBase(base);
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
    protected AbstractWrapperXMap() {
        
    }
    
    @Override
    public boolean isReadWriteLockSupported() {
        return this.rootData instanceof ConstructOnlyRootData && this.rootData.base.isReadWriteLockSupported();
    }
    
    @Override
    public BidiType bidiType() {
        return this.getBase().bidiType();
    }
    
    @Override
    public final UnifiedComparator<? super K> keyUnifiedComparator() {
        return this.rootData.keyUnifiedComparator();
    }

    @Override
    public final UnifiedComparator<? super V> valueUnifiedComparator() {
        return this.rootData.valueUnifiedComparator();
    }

    @Override
    public final UnifiedComparator<? super Entry<K, V>> entryUnifiedComparator() {
        RootData<K, V> rootData = this.rootData;
        EqualityComparator<Entry<K, V>> equalityComparator =
                EntryEqualityComparator.of(
                        rootData.keyUnifiedComparator(), 
                        rootData.valueUnifiedComparator());
        return UnifiedComparator.of(equalityComparator);
    }

    @Override
    public final void addKeyValidator(Validator<K> validator) {
        this.rootData.addKeyValidator(validator);
    }

    @Override
    public final void removeKeyValidator(Validator<K> validator) {
        this.rootData.removeKeyValidator(validator);
    }

    @Override
    public void validateKey(K key) {
        this.rootData.validateKey(key);
    }

    @Override
    public final void addValueValidator(Validator<V> validator) {
        this.rootData.addValueValidator(validator);
    }

    @Override
    public final void removeValueValidator(Validator<V> validator) {
        this.rootData.removeValueValidator(validator);
    }

    @Override
    public void validateValue(V value) {
        this.rootData.validateValue(value);
    }

    @Override
    public final ReplacementRule keyReplacementRule() {
        return this.getBase().keyReplacementRule();
    }

    @Override
    public final boolean isDisabled() {
        return this.rootData.isDisabled();
    }

    @Override
    public final void disable() {
        this.rootData.setDisabled(true);
    }

    @Override
    public final void enable() {
        this.rootData.setDisabled(false);
    }

    @Override
    public int hashCode() {
        return this.getBase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.getBase().equals(obj);
    }

    @Override
    public String toString() {
        return this.getBase().toString();
    }

    @Override
    public boolean containsKey(Object key) {
        this.requiredEnabled();
        return this.getBase().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        this.requiredEnabled();
        return this.getBase().containsValue(value);
    }

    @Override
    public XEntry<K, V> entryOfValue(V value) {
        this.requiredEnabled();
        return this.getBase().entryOfValue(value);
    }

    @Override
    public int size() {
        this.requiredEnabled();
        return this.getBase().size();
    }

    @Override
    public boolean isEmpty() {
        this.requiredEnabled();
        return this.getBase().isEmpty();
    }

    @Override
    public V get(Object key) {
        this.requiredEnabled();
        return this.getBase().get(key);
    }

    @Override
    public V put(K key, V value) {
        this.enable();
        return this.getBase().put(key, value);
    }

    @Override
    public V remove(Object key) {
        this.enable();
        return this.getBase().remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.enable();
        this.getBase().putAll(m);
    }

    @Override
    public void clear() {
        this.enable();
        this.getBase().clear();
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
    
    @SuppressWarnings("unchecked")
    protected final <T extends RootData<K, V>> T getRootData() {
        return (T)this.rootData;
    }
    
    protected final <T extends XMap<K, V>> T getBase() {
        return this.getBase(false);
    }

    @SuppressWarnings("unchecked")
    protected final <T extends XMap<K, V>> T getBase(boolean canReturnNull) {
        XMap<K, V> base = this.base;
        RootData<K, V> rootData = this.rootData;
        if (base == null || this.expectRootModCount != rootData.modCount) {
            if (canReturnNull) {
                return null;
            }
            AbstractWrapperXMap<K, V> parent = this.parent;
            if (parent == null) {
                base = rootData.getBase();
            } else {
                base = this.createBaseView(parent.getBase(false), this.viewInfo);
            }
            if (base != null) {
                this.setBase(base);
            }
        }
        return (T)base;
    }
    
    @SuppressWarnings("unchecked")
    protected final <T extends XMap<K, V>> T getParent() {
        return (T)this.parent;
    }

    protected RootData<K, V> createRootData() {
        return new RootData<K, V>();
    }
    
    protected XMap<K, V> createBaseView(
            XMap<K, V> parentBase, 
            ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }
    
    protected AbstractMapElementEventDispatcher<K, V> createEventDispatcher() {
        throw new IllegalProgramException(
                CommonMessages.mustOverrideCreateEventDispatcher(
                        this.getClass(), AbstractWrapperXMap.class)
        );
    }
    
    protected void onLoadBase(BaseContext<K, V> ctx) {
        
    }

    protected void onUnloadBase(BaseContext<K, V> ctx) {
        
    }
    
    protected final void requiredEnabled() {
        if (this.rootData.disabled) {
            throw new IllegalStateException(
                    CommonMessages.currentCollectionIsDisabled(this.getClass())
            );
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setBase(XMap<K, V> base) {
        XMap<K, V> oldBase = this.base;
        if (oldBase != base) {
            AbstractMapElementEventDispatcher<K, V> dispatcher = null;
            if (this instanceof ModificationAware) {
                dispatcher = this.eventDispatcher();
                Arguments.mustBeInstanceOfValueWhen(
                        CommonMessages.whenThisIsModificationAware(ModificationAware.class),
                        "base", 
                        base, 
                        MAMap.class);
            }
            
            BaseContext<K, V> ctx = new BaseContext<K, V>(oldBase, base);
            this.onUnloadBase(ctx);
            if (oldBase != null && dispatcher != null) {
                ((MAMap<K, V>)oldBase).removeMapElementListener(dispatcher);
            }
            if (oldBase instanceof WrapperMapAware<?, ?>) {
                ((WrapperMapAware<K, V>)oldBase).setWrapperMap(null);
            }
            this.base = base;
            this.expectRootModCount = this.rootData.modCount;
            if (base instanceof WrapperMapAware<?, ?>) {
                ((WrapperMapAware<K, V>)base).setWrapperMap(this);
            }
            if (dispatcher != null) {
                ((MAMap<K, V>)base).addMapElementListener(dispatcher);
            }
            this.onLoadBase(ctx);
        }
    }
    
    private AbstractMapElementEventDispatcher<K, V> eventDispatcher() {
        AbstractMapElementEventDispatcher<K, V> dispatcher = this.createEventDispatcher();
        if (dispatcher == null) {
            throw new IllegalProgramException(
                    CommonMessages.createEventDispatcherMustReturnNonNull(this.getClass())
            );
        }
        if (dispatcher.getOwner() != this) {
            throw new IllegalProgramException(
                    CommonMessages.ownerOfReturnedValueOfCreateEventDispatcherMustBeThis(this.getClass())
            );
        }
        return dispatcher;
    }
    
    @Override
    protected void onWriteState(Output out) throws IOException {
        out.writeObject(this.rootData);
    }

    @Override
    protected void onReadState(Input in) throws ClassNotFoundException, IOException {
        RootData<K, V> rootData = in.readObject();
        boolean oldDeserializing = rootData.deserializing;
        rootData.deserializing = true;
        try {
            this.rootData = rootData;
            XMap<K, V> base = rootData.getBase(true);
            if (base != null) {
                this.setBase(base);
            }
        } finally {
            rootData.deserializing = oldDeserializing;
        }
    }
    
    @SuppressWarnings("unchecked")
    protected final void replace(Object base) {
        Arguments.mustNotBeInstanceOfValue("this", this, View.class);
        Arguments.mustNotBeInstanceOfValue("base", base, View.class);
        Arguments.mustBeInstanceOfValue("base", base, XMap.class);
        
        // Unnecessary but for optimization
        if (this instanceof ModificationAware) {
            MAMap<K, V> oldBase = (MAMap<K, V>)this.base;
            if (oldBase != null) {
                oldBase.removeMapElementListener(this.eventDispatcher());
            }
        }
        
        // Only need to change "root.base", because "this.base" will be 
        // refreshed automatically when "this.getBase()" is called later.
        this.getRootData().setBase((XMap<K, V>)base);
    }
    
    @SuppressWarnings("unchecked")
    protected final <T extends XEntrySetView<K, V>> T getEntrySet() {
        XEntrySetView<K, V> entrySet = this.entrySet;
        if (entrySet == null) {
            this.entrySet = entrySet = this.createEntrySet();
        }
        return (T)entrySet;
    }
    
    @SuppressWarnings("unchecked")
    protected final <T extends XKeySetView<K>> T getKeySet() {
        XKeySetView<K> keySet = this.keySet;
        if (keySet == null) {
            this.keySet = keySet = this.createKeySet();
        }
        return (T)keySet;
    }
    
    @SuppressWarnings("unchecked")
    protected final <T extends XValuesView<V>> T getValues() {
        XValuesView<V> values = this.values;
        if (values == null) {
            this.values = values = this.createValues();
        }
        return (T)values;
    }
    
    protected XEntrySetView<K, V> createEntrySet() {
        return new EntrySetImpl<K, V>(this);
    }
    
    protected XKeySetView<K> createKeySet() {
        return new KeySetImpl<K, V>(this);
    }
    
    protected XValuesView<V> createValues() {
        return new ValuesImpl<K, V>(this);
    }

    protected static final class BaseContext<K, V> extends AttributeContext {
        
        private static final long serialVersionUID = -8226936343991507867L;

        private XMap<K, V> oldBase;
        
        private XMap<K, V> newBase;
        
        BaseContext(XMap<K, V> oldBase, XMap<K, V> newBase) {
            super();
            this.oldBase = oldBase;
            this.newBase = newBase;
        }
        
        public XMap<K, V> getOldBase() {
            return this.oldBase;
        }
    
        public XMap<K, V> getNewBase() {
            return this.newBase;
        }
    }
    
    protected static class EntrySetImpl<K, V> implements XEntrySetView<K, V>, DisablityManageable {
        
        private AbstractWrapperXMap<K, V> parentMap;
        
        private XEntrySetView<K, V> base;
                
        private int expectedModCount;
        
        protected EntrySetImpl(AbstractWrapperXMap<K, V> parentMap) {
            Arguments.mustNotBeNull("parentMap", parentMap);
            this.parentMap = parentMap;
        }
        
        @Deprecated
        @Override
        public final boolean add(Entry<K, V> e) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final boolean addAll(Collection<? extends Entry<K, V>> c)
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void addValidator(Validator<Entry<K, V>> validator) {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void removeValidator(Validator<Entry<K, V>> validator) {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void validate(Entry<K, V> e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final boolean isDisabled() {
            return this.parentMap.rootData.disabled;
        }

        @Override
        public void disable() {
            this.parentMap.rootData.setDisabled(true);
        }

        @Override
        public void enable() {
            this.parentMap.rootData.setDisabled(false);
        }

        @Override
        public boolean isReadWriteLockSupported() {
            return this.parentMap.isReadWriteLockSupported();
        }

        @Override
        public final ReplacementRule replacementRule() {
            return this.getBase().replacementRule();
        }

        @Override
        public final UnifiedComparator<? super Entry<K, V>> unifiedComparator() {
            RootData<K, V> rootData = this.parentMap.rootData;
            EntryEqualityComparator<K, V> entryEqualityComparator =
                    EntryEqualityComparator.of(
                            rootData.keyUnifiedComparator(), 
                            rootData.valueUnifiedComparator());
            return UnifiedComparator.of(entryEqualityComparator);
        }

        @Override
        public ViewInfo viewInfo() {
            return MapViewInfos.entrySet();
        }

        @Override
        public int hashCode() {
            return this.getBase().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.getBase().equals(obj);
        }

        @Override
        public String toString() {
            return this.getBase().toString();
        }

        @Override
        public boolean contains(Object o) {
            this.parentMap.requiredEnabled();
            return this.getBase().contains(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            this.parentMap.requiredEnabled();
            return this.getBase().containsAll(c);
        }

        @Override
        public Object[] toArray() {
            this.parentMap.requiredEnabled();
            return this.getBase().toArray();
        }
    
        @Override
        public <T> T[] toArray(T[] a) {
            this.parentMap.requiredEnabled();
            return this.getBase().toArray(a);
        }
    
        @Override
        public boolean isEmpty() {
            this.parentMap.requiredEnabled();
            return this.getBase().isEmpty();
        }

        @Override
        public int size() {
            this.parentMap.requiredEnabled();
            return this.getBase().size();
        }

        @Override
        public void clear() {
            this.enable();
            this.getBase().clear();
        }
    
        @Override
        public boolean remove(Object o) {
            this.enable();
            return this.getBase().remove(o);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            this.enable();
            return this.getBase().removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            this.parentMap.enable();
            return this.getBase().retainAll(c);
        }

        @Override
        public XEntrySetIterator<K, V> iterator() {
            return new IteratorImpl<K, V>(this);
        }
        
        protected final XEntrySetView<K, V> getBase() {
            XEntrySetView<K, V> base = this.base;
            AbstractWrapperXMap<K, V> parentMap = this.parentMap;
            int modCount = parentMap.rootData.modCount;
            if (base == null || this.expectedModCount != modCount) {
                base = parentMap.getBase().entrySet();
                this.expectedModCount = modCount;
                this.setBase(base);
            }
            return base;
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractWrapperXMap<K, V>> T getParentMap() {
            return (T)this.parentMap;
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends RootData<K, V>> T getRootData() {
            return (T)this.parentMap.rootData;
        }
        
        protected final void requiredEnabled() {
            this.parentMap.requiredEnabled();
        }
        
        @SuppressWarnings("unchecked")
        private void setBase(XEntrySetView<K, V> base) {
            if (this instanceof ModificationAware) {
                AbstractElementEventDispatcher<Entry<K, V>> dispatcher = this.eventDispatcher();
                XEntrySetView<K, V> oldBase = this.base;
                if (oldBase != null) {
                    ((ElementModificationAware<Entry<K, V>>)oldBase).removeElementListener(dispatcher);
                }
                ((ElementModificationAware<Entry<K, V>>)base).addElementListener(dispatcher);
            }
            this.base = base;
        }
        
        protected AbstractElementEventDispatcher<Entry<K, V>> createEventDispatcher() {
            throw new IllegalProgramException(
                    CommonMessages.mustOverrideCreateEventDispatcher(
                            this.getClass(), EntrySetImpl.class)
            );
        }
        
        private AbstractElementEventDispatcher<Entry<K, V>> eventDispatcher() {
            AbstractElementEventDispatcher<Entry<K, V>> dispatcher = this.createEventDispatcher();
            if (dispatcher == null) {
                throw new IllegalProgramException(
                        CommonMessages.createEventDispatcherMustReturnNonNull(this.getClass())
                );
            }
            if (dispatcher.getOwner() != this) {
                throw new IllegalProgramException(
                        CommonMessages.ownerOfReturnedValueOfCreateEventDispatcherMustBeThis(this.getClass())
                );
            }
            return dispatcher;
        }
        
        protected static class IteratorImpl<K, V> implements XEntrySetIterator<K, V> {
            
            private EntrySetImpl<K, V> parent;
            
            private XEntrySetIterator<K, V> base;
            
            private int expectedModcount;
            
            protected IteratorImpl(EntrySetImpl<K, V> parent) {
                Arguments.mustNotBeNull("parent", parent);
                this.parent = parent;
            }

            @Override
            public boolean isReadWriteLockSupported() {
                return this.base.isReadWriteLockSupported();
            }

            @Override
            public UnifiedComparator<? super Entry<K, V>> unifiedComparator() {
                return this.base.unifiedComparator();
            }

            @Override
            public ViewInfo viewInfo() {
                return CollectionViewInfos.iterator();
            }

            @Override
            public boolean hasNext() {
                this.parent.parentMap.requiredEnabled();
                return this.getBase().hasNext();
            }

            @Override
            public XEntry<K, V> next() {
                this.parent.parentMap.requiredEnabled();
                return new EntryImpl<K, V>(this);
            }

            @Override
            public void remove() {
                this.parent.parentMap.enable();
                this.getBase().remove();
            }

            protected XEntrySetIterator<K, V> getBase() {
                XEntrySetIterator<K, V> base = this.base;
                int modCount = this.parent.parentMap.rootData.modCount;
                if (base == null) {
                    base = this.parent.getBase().iterator();
                    this.expectedModcount = modCount;
                    this.setBase(base);
                } else if (this.expectedModcount != modCount) {
                    throw new ConcurrentModificationException(CommonMessages.concurrentModification());
                }
                return base;
            }
            
            @SuppressWarnings("unchecked")
            protected final <T extends XEntrySetView<K, V>> T getParent() {
                return (T)this.parent;
            }
            
            @SuppressWarnings("unchecked")
            private void setBase(XEntrySetIterator<K, V> base) {
                if (this instanceof ModificationAware) {
                    AbstractElementEventDispatcher<Entry<K, V>> dispatcher = this.eventDispatcher();
                    XEntrySetIterator<K, V> oldBase = this.base;
                    if (oldBase != null) {
                        ((ElementModificationAware<Entry<K, V>>)oldBase).removeElementListener(dispatcher);
                    }
                    ((ElementModificationAware<Entry<K, V>>)base).addElementListener(dispatcher);
                }
                this.base = base;
            }
            
            protected AbstractElementEventDispatcher<Entry<K, V>> createEventDispatcher() {
                throw new IllegalProgramException(
                        CommonMessages.mustOverrideCreateEventDispatcher(
                                this.getClass(), IteratorImpl.class)
                );
            }
            
            private AbstractElementEventDispatcher<Entry<K, V>> eventDispatcher() {
                AbstractElementEventDispatcher<Entry<K, V>> dispatcher = this.createEventDispatcher();
                if (dispatcher == null) {
                    throw new IllegalProgramException(
                            CommonMessages.createEventDispatcherMustReturnNonNull(this.getClass())
                    );
                }
                if (dispatcher.getOwner() != this) {
                    throw new IllegalProgramException(
                            CommonMessages.ownerOfReturnedValueOfCreateEventDispatcherMustBeThis(this.getClass())
                    );
                }
                return dispatcher;
            }
            
        }
    }

    protected static abstract class AbstractKeySetImpl<K, V> implements XKeySetView<K>, DisablityManageable {
        
        private AbstractWrapperXMap<K, V> parentMap;
        
        private AbstractKeySetImpl<K, V> parent;
        
        private XKeySetView<K> base;
        
        private int expectedModCount;
        
        private ViewInfo viewInfo;
        
        protected AbstractKeySetImpl(
                AbstractWrapperXMap<K, V> parentMap, 
                ViewInfo viewInfo) {
            this.parentMap = parentMap;
            this.viewInfo = viewInfo;
        }
        
        protected AbstractKeySetImpl(
                AbstractKeySetImpl<K, V> parent,
                ViewInfo viewInfo) {
            this.parent = parent;
            this.parentMap = parent.parentMap;
            this.viewInfo = viewInfo;
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

        @Deprecated
        @Override
        public final void addValidator(Validator<K> validator) {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void removeValidator(Validator<K> validator) {
            throw new UnsupportedOperationException();
        }
        
        @Deprecated
        @Override
        public final void validate(K e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDisabled() {
            return this.getRootData().isDisabled();
        }

        @Override
        public void disable() {
            this.getRootData().setDisabled(true);
        }

        @Override
        public void enable() {
            this.getRootData().setDisabled(false);
        }

        @Override
        public boolean isReadWriteLockSupported() {
            return this.parentMap.isReadWriteLockSupported();
        }

        @Override
        public final UnifiedComparator<? super K> unifiedComparator() {
            return this.parentMap.rootData.keyUnifiedComparator();
        }

        @Override
        public final ViewInfo viewInfo() {
            if (this.getRootData().getBase(true) == null) {
                return this.viewInfo;
            }
            return this.getBase().viewInfo();
        }

        @Override
        public final ReplacementRule replacementRule() {
            return this.getBase().replacementRule();
        }

        @Override
        public int hashCode() {
            return this.getBase().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.getBase().equals(obj);
        }

        @Override
        public String toString() {
            return this.getBase().toString();
        }

        @Override
        public boolean contains(Object o) {
            this.requiredEnabled();
            return this.getBase().contains(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            this.requiredEnabled();
            return this.getBase().containsAll(c);
        }

        @Override
        public Object[] toArray() {
            this.requiredEnabled();
            return this.getBase().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            this.requiredEnabled();
            return this.getBase().toArray(a);
        }

        @Override
        public boolean isEmpty() {
            this.requiredEnabled();
            return this.getBase().isEmpty();
        }

        @Override
        public int size() {
            this.requiredEnabled();
            return this.getBase().size();
        }

        @Override
        public void clear() {
            this.requiredEnabled();
            this.getBase().clear();
        }

        @Override
        public boolean remove(Object o) {
            this.enable();
            return this.getBase().remove(o);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            this.enable();
            return this.getBase().removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            this.enable();
            return this.getBase().retainAll(c);
        }

        @Override
        public XIterator<K> iterator() {
            return new IteratorImpl<K, V>(this);
        }

        protected XKeySetView<K> createBaseView(
                XMap<K, V> baseMap, 
                ViewInfo viewInfo) {
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
        protected XKeySetView<K> createBaseView(
                XKeySetView<K> parentBase,
                ViewInfo viewInfo) {
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }

        @SuppressWarnings("unchecked")
        protected final <T extends XKeySetView<K>> T getBase() {
            XKeySetView<K> base = this.base;
            AbstractWrapperXMap<K, V> parentMap = this.parentMap;
            int modCount = parentMap.rootData.modCount;
            if (base == null || this.expectedModCount != modCount) {
                AbstractKeySetImpl<K, V> parent = this.getParent();
                if (parent != null) {
                    base = this.createBaseView(
                            parent.<XKeySetView<K>>getBase(), 
                            viewInfo);
                } else {
                    base = this.createBaseView(
                            parentMap.<XMap<K, V>>getBase(), 
                            this.viewInfo);
                }
                this.expectedModCount = modCount;
                this.setBase(base);
            }
            return (T)base;
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractWrapperXMap<K, V>> T getParentMap() {
            return (T)this.parentMap;
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractKeySetImpl<K, V>> T getParent() {
            return (T)this.parent;
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends RootData<K, V>> T getRootData() {
            return (T)this.parentMap.rootData;
        }
        
        protected final void requiredEnabled() {
            this.parentMap.requiredEnabled();
        }
        
        @SuppressWarnings("unchecked")
        private void setBase(XKeySetView<K> base) {
            if (this instanceof ModificationAware) {
                AbstractElementEventDispatcher<K> dispatcher = this.eventDispatcher();
                XKeySetView<K> oldBase = this.base;
                if (oldBase != null) {
                    ((ElementModificationAware<K>)oldBase).removeElementListener(dispatcher);
                }
                ((ElementModificationAware<K>)base).addElementListener(dispatcher);
            }
            this.base = base;
        }
        
        protected AbstractKeySetElementEventDispatcher<K, V> createEventDispatcher() {
            throw new IllegalProgramException(
                    CommonMessages.mustOverrideCreateEventDispatcher(
                            this.getClass(), AbstractKeySetImpl.class)
            );
        }
        
        private AbstractKeySetElementEventDispatcher<K, V> eventDispatcher() {
            AbstractKeySetElementEventDispatcher<K, V> dispatcher = this.createEventDispatcher();
            if (dispatcher == null) {
                throw new IllegalProgramException(
                        CommonMessages.createEventDispatcherMustReturnNonNull(this.getClass())
                );
            }
            if (dispatcher.getOwner() != this) {
                throw new IllegalProgramException(
                        CommonMessages.ownerOfReturnedValueOfCreateEventDispatcherMustBeThis(this.getClass())
                );
            }
            return dispatcher;
        }
        
        protected static abstract class AbstractIteratorImpl<K, V> implements XIterator<K> {
            
            private AbstractKeySetImpl<K, V> parent;
            
            private XIterator<K> base;
            
            private ViewInfo viewInfo;
            
            private transient int expectedModCount;
            
            protected AbstractIteratorImpl(
                    AbstractKeySetImpl<K, V> parent, 
                    ViewInfo viewInfo) {
                Arguments.mustNotBeNull("parent", parent);
                this.parent = parent;
                this.viewInfo = viewInfo;
            }

            @Override
            public boolean isReadWriteLockSupported() {
                return this.getBase().isReadWriteLockSupported();
            }

            @Override
            public UnifiedComparator<? super K> unifiedComparator() {
                return this.getBase().unifiedComparator();
            }

            @Override
            public ViewInfo viewInfo() {
                if (this.getRootData().getBase(true) == null) {
                    return this.viewInfo;
                }
                return this.getBase().viewInfo();
            }

            @Override
            public boolean hasNext() {
                this.parent.requiredEnabled();
                return this.getBase().hasNext();
            }

            @Override
            public K next() {
                this.parent.requiredEnabled();
                return this.getBase().next();
            }

            @Override
            public void remove() {
                this.parent.enable();
                this.getBase().remove();
            }

            protected final XIterator<K> getBase() {
                XIterator<K> base = this.base;
                int modCount = this.parent.parentMap.expectRootModCount;
                if (base == null) {
                    base = this.createBaseView(
                            this.parent.getBase(), 
                            this.viewInfo);
                    this.expectedModCount = modCount;
                    this.setBase(base);
                } else if (this.expectedModCount != modCount) {
                    throw new ConcurrentModificationException(CommonMessages.concurrentModification());
                }
                return base;
            }
            
            @SuppressWarnings("unchecked")
            protected final <T extends AbstractKeySetImpl<K, V>> T getParent() {
                return (T)this.parent;
            }
            
            @SuppressWarnings("unchecked")
            protected final <T extends RootData<K, V>> T getRootData() {
                return (T)this.parent.parentMap.rootData;
            }
            
            protected abstract XIterator<K> createBaseView(XKeySetView<K> baseKeySet, ViewInfo viewInfo);
            
            @SuppressWarnings("unchecked")
            private void setBase(XIterator<K> base) {
                if (this instanceof ModificationAware) {
                    AbstractElementEventDispatcher<K> dispatcher = this.eventDispatcher();
                    XIterator<K> oldBase = this.base;
                    if (oldBase != null) {
                        ((ElementModificationAware<K>)oldBase).removeElementListener(dispatcher);
                    }
                    ((ElementModificationAware<K>)base).addElementListener(dispatcher);
                }
                this.base = base;
            }
            
            protected AbstractKeySetElementEventDispatcher<K, V> createEventDispatcher() {
                throw new IllegalProgramException(
                        CommonMessages.mustOverrideCreateEventDispatcher(
                                this.getClass(), AbstractIteratorImpl.class)
                );
            }
            
            private AbstractKeySetElementEventDispatcher<K, V> eventDispatcher() {
                AbstractKeySetElementEventDispatcher<K, V> dispatcher = this.createEventDispatcher();
                if (dispatcher == null) {
                    throw new IllegalProgramException(
                            CommonMessages.createEventDispatcherMustReturnNonNull(this.getClass())
                    );
                }
                if (dispatcher.getOwner() != this) {
                    throw new IllegalProgramException(
                            CommonMessages.ownerOfReturnedValueOfCreateEventDispatcherMustBeThis(this.getClass())
                    );
                }
                return dispatcher;
            }
        }
        
        protected static class IteratorImpl<K, V> extends AbstractIteratorImpl<K, V> {

            protected IteratorImpl(AbstractKeySetImpl<K, V> parent) {
                super(parent, CollectionViewInfos.iterator());
            }

            @Override
            public ViewInfo viewInfo() {
                return CollectionViewInfos.iterator();
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

        protected KeySetImpl(AbstractWrapperXMap<K, V> parentMap) {
            super(parentMap, MapViewInfos.keySet());
        }

        @Override
        protected XKeySetView<K> createBaseView(
                XMap<K, V> baseMap, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof MapViewInfos.KeySet) {
                return baseMap.keySet();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
    }
    
    protected static class ValuesImpl<K, V> implements XValuesView<V>, DisablityManageable {
        
        private AbstractWrapperXMap<K, V> parentMap;
        
        private XValuesView<V> base;
        
        private int expectedModCount;
        
        protected ValuesImpl(AbstractWrapperXMap<K, V> parentMap) {
            Arguments.mustNotBeNull("parentMap", parentMap);
            this.parentMap = parentMap;
        }

        @Deprecated
        @Override
        public final boolean add(V e) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final boolean addAll(Collection<? extends V> c) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void addValidator(Validator<V> validator) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void removeValidator(Validator<V> validator) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
        
        @Deprecated
        @Override
        public final void validate(V e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDisabled() {
            return this.parentMap.rootData.disabled;
        }

        @Override
        public void disable() {
            this.parentMap.rootData.setDisabled(true);
        }

        @Override
        public void enable() {
            this.parentMap.rootData.setDisabled(false);
        }

        @Override
        public boolean isReadWriteLockSupported() {
            return this.parentMap.isReadWriteLockSupported();
        }

        @Override
        public ViewInfo viewInfo() {
            return MapViewInfos.values();
        }

        @Override
        public final UnifiedComparator<? super V> unifiedComparator() {
            return this.parentMap.rootData.valueUnifiedComparator();
        }

        @Override
        public String toString() {
            return this.getBase().toString();
        }

        @Override
        public boolean contains(Object o) {
            this.parentMap.requiredEnabled();
            return this.getBase().contains(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            this.parentMap.requiredEnabled();
            return this.getBase().containsAll(c);
        }

        @Override
        public boolean isEmpty() {
            this.parentMap.requiredEnabled();
            return this.getBase().isEmpty();
        }

        @Override
        public int size() {
            this.parentMap.requiredEnabled();
            return this.getBase().size();
        }

        @Override
        public Object[] toArray() {
            this.parentMap.requiredEnabled();
            return this.getBase().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            this.parentMap.requiredEnabled();
            return this.getBase().toArray(a);
        }

        @Override
        public void clear() {
            this.enable();
            this.getBase().clear();
        }

        @Override
        public boolean remove(Object o) {
            this.enable();
            return this.getBase().remove(o);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            this.enable();
            return this.getBase().removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            this.enable();
            return this.getBase().retainAll(c);
        }

        @Override
        public XIterator<V> iterator() {
            return new IteratorImpl<K, V>(this);
        }

        protected final XValuesView<V> getBase() {
            XValuesView<V> base = this.base;
            AbstractWrapperXMap<K, V> parentMap = this.parentMap;
            int modCount = parentMap.rootData.modCount;
            if (base == null || this.expectedModCount != modCount) {
                base = parentMap.getBase().values();
                this.expectedModCount = modCount;
                this.setBase(base);
            }
            return base;
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractWrapperXMap<K, V>> T getParentMap() {
            return (T)this.parentMap;
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends RootData<K, V>> T getRootData() {
            return (T)this.parentMap.rootData;
        }
        
        @SuppressWarnings("unchecked")
        private void setBase(XValuesView<V> base) {
            if (this instanceof ModificationAware) {
                AbstractElementEventDispatcher<V> dispatcher = this.eventDispatcher();
                XValuesView<V> oldBase = this.base;
                if (oldBase != null) {
                    ((ElementModificationAware<V>)oldBase).removeElementListener(dispatcher);
                }
                ((ElementModificationAware<V>)base).addElementListener(dispatcher);
            }
            this.base = base;
        }
        
        protected AbstractValuesElementEventDispatcher<K, V> createEventDispatcher() {
            throw new IllegalProgramException(
                    CommonMessages.mustOverrideCreateEventDispatcher(
                            this.getClass(), ValuesImpl.class)
            );
        }
        
        protected final void requiredEnabled() {
            this.parentMap.requiredEnabled();
        }
        
        private AbstractValuesElementEventDispatcher<K, V> eventDispatcher() {
            AbstractValuesElementEventDispatcher<K, V> dispatcher = this.createEventDispatcher();
            if (dispatcher == null) {
                throw new IllegalProgramException(
                        CommonMessages.createEventDispatcherMustReturnNonNull(this.getClass())
                );
            }
            if (dispatcher.getOwner() != this) {
                throw new IllegalProgramException(
                        CommonMessages.ownerOfReturnedValueOfCreateEventDispatcherMustBeThis(this.getClass())
                );
            }
            return dispatcher;
        }
        
        protected static class IteratorImpl<K, V> implements XIterator<V> {
            
            private ValuesImpl<K, V> parent;
            
            private XIterator<V> base;
            
            private int expectedModCount;
            
            protected IteratorImpl(ValuesImpl<K, V> parent) {
                Arguments.mustNotBeNull("parent", parent);
                this.parent = parent;
            }

            @Override
            public boolean hasNext() {
                this.parent.parentMap.requiredEnabled();
                return this.getBase().hasNext();
            }

            @Override
            public V next() {
                this.parent.parentMap.requiredEnabled();
                return this.getBase().next();
            }

            @Override
            public void remove() {
                this.parent.parentMap.enable();
                this.getBase().remove();
            }

            @Override
            public boolean isReadWriteLockSupported() {
                return this.base.isReadWriteLockSupported();
            }

            @Override
            public UnifiedComparator<? super V> unifiedComparator() {
                return this.base.unifiedComparator();
            }

            @Override
            public ViewInfo viewInfo() {
                return CollectionViewInfos.iterator();
            }
            
            protected final XIterator<V> getBase() {
                XIterator<V> base = this.base;
                int modCount = this.parent.parentMap.rootData.modCount;
                if (base == null) {
                    base = this.parent.getBase().iterator();
                    this.expectedModCount = modCount;
                    this.setBase(base);
                } else if (this.expectedModCount != modCount) {
                    throw new ConcurrentModificationException(CommonMessages.concurrentModification());
                }
                return base;
            }
            
            @SuppressWarnings("unchecked")
            protected final <T extends ValuesImpl<K, V>> T getParent() {
                return (T)this.parent;
            }
            
            @SuppressWarnings("unchecked")
            private void setBase(XIterator<V> base) {
                if (this instanceof ModificationAware) {
                    AbstractElementEventDispatcher<V> dispatcher = this.eventDispatcher();
                    XIterator<V> oldBase = this.base;
                    if (oldBase != null) {
                        ((ElementModificationAware<V>)oldBase).removeElementListener(dispatcher);
                    }
                    ((ElementModificationAware<V>)base).addElementListener(dispatcher);
                }
                this.base = base;
            }
            
            protected AbstractValuesElementEventDispatcher<K, V> createEventDispatcher() {
                throw new IllegalProgramException(
                        CommonMessages.mustOverrideCreateEventDispatcher(
                                this.getClass(), IteratorImpl.class)
                );
            }
            
            private AbstractValuesElementEventDispatcher<K, V> eventDispatcher() {
                AbstractValuesElementEventDispatcher<K, V> dispatcher = this.createEventDispatcher();
                if (dispatcher == null) {
                    throw new IllegalProgramException(
                            CommonMessages.createEventDispatcherMustReturnNonNull(this.getClass())
                    );
                }
                if (dispatcher.getOwner() != this) {
                    throw new IllegalProgramException(
                            CommonMessages.ownerOfReturnedValueOfCreateEventDispatcherMustBeThis(this.getClass())
                    );
                }
                return dispatcher;
            }
        }
    }
    
    protected static abstract class AbstractEntryImpl<K, V> implements XEntry<K, V> {
        
        private AbstractWrapperXMap<K, V> parentMap;
        
        private XEntry<K, V> base;
        
        protected AbstractEntryImpl(
                AbstractWrapperXMap<K, V> parentMap, 
                ViewInfo viewInfo) throws NoEntryException {
            Arguments.mustNotBeNull("parentMap", parentMap);
            Arguments.mustNotBeNull("viewInfo", viewInfo);
            XEntry<K, V> base = this.createBaseView(
                    parentMap.getBase(), 
                    viewInfo);
            if (base == null) {
                throw new NoEntryException();
            }
            this.base = base;
            if (base instanceof ModificationAware) {
                MAMap.MAEntry<K, V> maBase = (MAMap.MAEntry<K, V>)base;
                maBase.addElementListener(this.eventDispatcher());
            }
        }
        
        protected AbstractEntryImpl(AbstractWrapperXMap.EntrySetImpl.IteratorImpl<K, V> iterator) {
            this.parentMap = iterator.parent.parentMap;
            this.base = iterator.getBase().next();
            if (base instanceof ModificationAware) {
                MAMap.MAEntry<K, V> maBase = (MAMap.MAEntry<K, V>)base;
                maBase.addElementListener(this.eventDispatcher());
            }
        }
        
        @Override
        public boolean isReadWriteLockSupported() {
            return this.parentMap.isReadWriteLockSupported();
        }
        
        @Override
        public K getKey() {
            this.parentMap.requiredEnabled();
            return this.base.getKey();
        }
    
        @Override
        public V getValue() {
            this.parentMap.requiredEnabled();
            return this.base.getValue();
        }
    
        @Override
        public V setValue(V value) {
            this.parentMap.enable();
            return this.base.setValue(value);
        }
    
        @Override
        public ViewInfo viewInfo() {
            return this.base.viewInfo();
        }
    
        @Override
        public UnifiedComparator<? super K> keyUnifiedComparator() {
            return this.base.keyUnifiedComparator();
        }
    
        @Override
        public UnifiedComparator<? super V> valueUnifiedComparator() {
            return this.valueUnifiedComparator();
        }
    
        @Override
        public UnifiedComparator<? super Entry<K, V>> unifiedComparator() {
            return this.base.unifiedComparator();
        }
    
        @Override
        public boolean isAlive() {
            return this.base.isAlive();
        }
        
        protected final XEntry<K, V> getBase() {
            return this.base;
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractWrapperXMap<K, V>> T getParentMap() {
            return (T)this.parentMap;
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends RootData<K, V>> T getRootData() {
            return (T)this.parentMap.rootData;
        }
        
        protected XEntry<K, V> createBaseView(XMap<K, V> baseMap, ViewInfo viewInfo) {
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
        protected AbstractEntryElementEventDispatcher<K, V> createEventDispatcher() {
            throw new IllegalProgramException(
                    CommonMessages.mustOverrideCreateEventDispatcher(
                            this.getClass(), 
                            AbstractEntryImpl.class)
            );
        }
        
        private DispatcherWrapper<K, V> eventDispatcher() {
            final AbstractEntryElementEventDispatcher<K, V> dispatcher = this.createEventDispatcher();
            if (dispatcher == null) {
                throw new IllegalProgramException(
                        CommonMessages.createEventDispatcherMustReturnNonNull(this.getClass())
                );
            }
            if (dispatcher.getOwner() != this) {
                throw new IllegalProgramException(
                        CommonMessages.ownerOfReturnedValueOfCreateEventDispatcherMustBeThis(this.getClass())
                );
            }
            return new DispatcherWrapper<K, V>(dispatcher);
        }
        
        private static class DispatcherWrapper<K, V> implements ElementListener<V> {
            
            private AbstractEntryElementEventDispatcher<K, V> dispatcher;
            
            DispatcherWrapper(AbstractEntryElementEventDispatcher<K, V> dispatcher) {
                this.dispatcher = dispatcher;
            }
    
            @SuppressWarnings("unchecked")
            @Override
            public void modifying(ElementEvent<V> e) throws Throwable {
                XEntry<K, V> source = (XEntry<K, V>)e.getSource();
                if (source.isAlive()) {
                    this.dispatcher.modifying(e);
                }
            }
    
            @SuppressWarnings("unchecked")
            @Override
            public void modified(ElementEvent<V> e) throws Throwable {
                XEntry<K, V> source = (XEntry<K, V>)e.getSource();
                if (source.isAlive()) {
                    this.dispatcher.modified(e);
                }
            }
    
            @Override
            public int hashCode() {
                return dispatcher.hashCode();
            }
    
            @SuppressWarnings("unchecked")
            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof DispatcherWrapper<?, ?>)) {
                    return false;
                }
                DispatcherWrapper<K, V> other = (DispatcherWrapper<K, V>)obj;
                return this.dispatcher.equals(other.dispatcher);
            }
            
        }
    }
    
    protected static class EntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected EntryImpl(AbstractWrapperXMap.EntrySetImpl.IteratorImpl<K, V> iterator) {
            super(iterator);
        }
    }
    
    protected static final class RealEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected RealEntryImpl(AbstractWrapperXMap<K, V> parentMap, K key) throws NoEntryException {
            super(parentMap, MapViewInfos.entryOfKey(key));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected XMap.XEntry<K, V> createBaseView(XMap<K, V> map, ViewInfo viewInfo) {
            if (viewInfo instanceof MapViewInfos.EntryOfKey) {
                K key = (K)((MapViewInfos.EntryOfKey)viewInfo).getKey();
                return map.entryOfKey(key);
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
    }

    protected static class RootData<K, V> implements Serializable {
        
        private static final long serialVersionUID = -8539472407818389873L;
        
        private AbstractWrapperXMap<K, V> rootWrapper;
        
        private XMap<K, V> base;
        
        private Validator<K> keyValidator;
        
        private Validator<V> valueValidator;
        
        private boolean disabled;
        
        private transient int modCount;
        
        private transient boolean deserializing;
        
        public final <T extends XMap<K, V>> T getBase() {
            return this.getBase(false);
        }

        @SuppressWarnings("unchecked")
        public final <T extends XMap<K, V>> T getBase(boolean canReturnNull) {
            XMap<K, V> base = (T)this.base;
            if (base == null) {
                if (this instanceof ConstructOnlyRootData) {
                    throw new IllegalProgramException(
                            CommonMessages.mustSetTheBaseInContstructorWhenTheRootDataImplementsRootData(
                                    ConstructOnlyRootData.class)
                    );
                }
                if (canReturnNull) {
                    return null;
                }
                UnifiedComparator<? super K> defaultKeyUnifiedComparator =
                        this.getDefaultKeyUnifiedComparator();
                UnifiedComparator<? super V> defaultValueUnifiedComparator =
                        this.getDefaultValueUnifiedComparator();
                base = this.createDefaultBase(
                        UnifiedComparator.nullToEmpty(defaultKeyUnifiedComparator),
                        UnifiedComparator.nullToEmpty(defaultValueUnifiedComparator));
                if (base == null) {
                    throw new IllegalProgramException(CommonMessages.createDefaultBaseMustReturnNonNull(this.getClass()));
                }
                this.setBase(base);
            }
            return (T)base;
        }

        protected void setBase(XMap<K, V> base) {
            if (this.deserializing) {
                throw new IllegalStateException(CommonMessages.canNotSetBaseDuringSerializing());
            }
            XMap<K, V> oldBase = this.base;
            if (oldBase != null && this instanceof SetOnceOnlyRootData) {
                throw new IllegalStateException(
                        CommonMessages.canNotSetBaseTwiceWhenTheRootDataIsSetOnceOnlyRootData(
                                SetOnceOnlyRootData.class));
            }
            if (base == null && this instanceof ConstructOnlyRootData) {
                throw new IllegalArgumentException(
                        CommonMessages.canNotSetNullBaseWhenTheRootDataIsContructorOnlyRootData(
                                ConstructOnlyRootData.class));
            }
            if (oldBase != base) {
                Arguments.mustNotBeInstanceOfValue("base", base, View.class);
                if (base != null) {
                    UnifiedComparator<? super K> defaultKeyUnifiedComparator =
                            this.getDefaultKeyUnifiedComparator();
                    if (defaultKeyUnifiedComparator != null && 
                            !defaultKeyUnifiedComparator.equals(base.keyUnifiedComparator())) {
                        Arguments.mustBeEqualToOtherWhen(
                                CommonMessages.whenDefaultUnifiedComparatorIsNotNull(), 
                                "base.keyUnifiedComparator()", 
                                base.keyUnifiedComparator(), 
                                "this.getDefaultKeyUnifiedComparator()", 
                                defaultKeyUnifiedComparator);
                    }
                    UnifiedComparator<? super V> defaultValueUnifiedComparator =
                            this.getDefaultValueUnifiedComparator();
                    if (defaultValueUnifiedComparator != null && 
                            !defaultValueUnifiedComparator.equals(base.valueUnifiedComparator())) {
                        Arguments.mustBeEqualToOtherWhen(
                                CommonMessages.whenDefaultUnifiedComparatorIsNotNull(), 
                                "base.valueUnifiedComparator()", 
                                base.valueUnifiedComparator(), 
                                "this.getDefaultValueUnifiedComparator", 
                                defaultValueUnifiedComparator);
                    }
                }
                
                Validator<K> keyValidator = this.keyValidator;
                Validator<V> valueValidator = this.valueValidator;
                if (oldBase != null) {
                    this.onUnloadTranisentData();
                    if (keyValidator != null) {
                        oldBase.removeKeyValidator(keyValidator);
                    }
                    if (valueValidator != null) {
                        oldBase.removeValueValidator(valueValidator);
                    }
                }
                this.base = base;
                this.modCount++;
                if (base != null) {
                    if (keyValidator != null) {
                        base.addKeyValidator(keyValidator);
                        if (this instanceof SetOnceOnlyRootData) {
                            this.keyValidator = null;
                        }
                    }
                    if (valueValidator != null) {
                        if (base != null) {
                            base.addValueValidator(valueValidator);
                        }
                        if (this instanceof SetOnceOnlyRootData) {
                            this.valueValidator = null;
                        }
                    }
                    this.onLoadTransientData();
                }
            }
        }
        
        public final UnifiedComparator<? super K> keyUnifiedComparator() {
            XMap<K, V> base = this.base;
            if (base != null) {
                return base.keyUnifiedComparator();
            }
            return UnifiedComparator.nullToEmpty(this.getDefaultKeyUnifiedComparator());
        }
        
        public final UnifiedComparator<? super V> valueUnifiedComparator() {
            XMap<K, V> base = this.base;
            if (base != null) {
                return base.valueUnifiedComparator();
            }
            return this.getDefaultValueUnifiedComparator();
        }
        
        public final void addKeyValidator(Validator<K> validator) {
            XMap<K, V> base = this.base;
            if (base == null || !(this instanceof SetOnceOnlyRootData)) {
                this.keyValidator = Validators.combine(this.keyValidator, validator);
            }
            if (base != null) {
                base.addKeyValidator(validator);
            }
        }
        
        public final void removeKeyValidator(Validator<K> validator) {
            XMap<K, V> base = this.base;
            if (base == null || !(this instanceof SetOnceOnlyRootData)) {
                this.keyValidator = Validators.remove(this.keyValidator, validator);
            }
            if (base != null) {
                base.removeKeyValidator(validator);
            }
        }
        
        public final void validateKey(K key) {
            XMap<K, V> base = this.base;
            if (base == null) {
                Validator<K> keyValidator = this.keyValidator;
                if (keyValidator != null) {
                    keyValidator.validate(key);
                }
            }
            if (base != null) {
                base.validateKey(key);
            }
        }
        
        public final void addValueValidator(Validator<V> validator) {
            XMap<K, V> base = this.base;
            if (base == null || !(this instanceof SetOnceOnlyRootData)) {
                this.valueValidator = Validators.combine(this.valueValidator, validator);
            }
            if (base != null) {
                base.addValueValidator(validator);
            }
        }
        
        public final void removeValueValidator(Validator<V> validator) {
            XMap<K, V> base = this.base;
            if (base == null || !(this instanceof SetOnceOnlyRootData)) {
                this.valueValidator = Validators.remove(this.valueValidator, validator);
            }
            if (base != null) {
                base.removeValueValidator(validator);
            }
        }
        
        public final void validateValue(V value) {
            XMap<K, V> base = this.base;
            if (base == null) {
                Validator<V> valueValidator = this.valueValidator;
                if (valueValidator != null) {
                    valueValidator.validate(value);
                }
            }
            if (base != null) {
                base.validateValue(value);
            }
        }
        
        public final boolean isDisabled() {
            return disabled;
        }

        public final void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        public boolean isDispatchable() {
            return true;
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractWrapperXMap<K, V>> T getRootWrapper() {
            AbstractWrapperXMap<K, V> rootWrapper = this.rootWrapper;
            if (rootWrapper == null) {
                throw new IllegalStateException(
                        CommonMessages.invokeGetRootOwnerTooEarlySoThatTheRootOwnerIsNull()
                );
            }
            return (T)rootWrapper;
        }
    
        protected UnifiedComparator<? super K> getDefaultKeyUnifiedComparator() {
            return null;
        }
        
        protected UnifiedComparator<? super V> getDefaultValueUnifiedComparator() {
            return null;
        }
        
        protected XMap<K, V> createDefaultBase(
                UnifiedComparator<? super K> keyUnifiedComparator, 
                UnifiedComparator<? super V> valueUnifiedComparator) {
            Comparator<? super K> keyComparator = keyUnifiedComparator.comparator();
            if (keyComparator != null) {
                return new TreeMap<K, V>(keyComparator, valueUnifiedComparator);
            }
            return new HashMap<K, V>(keyUnifiedComparator.equalityComparator(), valueUnifiedComparator);
        }
        
        /**
         * Like the constructor, it is used to initialize this object.
         * The {@link #getRootWrapper()} can not be invoked in the constructor,
         * but it can be invoked in this method.
         */
        protected void onInitialize() {
            
        }
        
        protected void onLoadTransientData() {
            
        }
        
        protected void onUnloadTranisentData() {
            
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.readObject();
            this.onLoadTransientData();
        }
    }
    
}
