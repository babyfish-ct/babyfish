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

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XMap;
import org.babyfish.collection.spi.base.AbstractBaseEntriesImpl;
import org.babyfish.collection.spi.base.BaseEntries;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.collection.spi.base.BaseEntryIterator;
import org.babyfish.collection.spi.base.BasicAlgorithms;
import org.babyfish.collection.spi.base.NoEntryException;
import org.babyfish.collection.spi.base.TransientValueEntries;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.collection.viewinfo.MapViewInfos;
import org.babyfish.collection.viewinfo.MapViewInfos.EntrySet;
import org.babyfish.collection.viewinfo.MapViewInfos.KeySet;
import org.babyfish.collection.viewinfo.MapViewInfos.Values;
import org.babyfish.data.View;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.StatefulObject;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public abstract class AbstractXMap<K, V> extends StatefulObject implements XMap<K, V> {
    
    BaseEntries<K, V> baseEntries;
    
    private transient XEntrySetView<K, V> entrySet;
    
    private transient XKeySetView<K> keySet;
    
    private transient XValuesView<V> values;
    
    protected AbstractXMap(BaseEntries<K, V> baseEntries) {
        if (this instanceof Serializable && this instanceof View) {
            throw new IllegalProgramException(
                    CommonMessages.viewCanNotBeSerializable(
                            this.getClass(),
                            Serializable.class,
                            View.class));
        }
        this.baseEntries = Arguments.mustNotBeInstanceOfValue(
                "baseEntries", 
                Arguments.mustNotBeNull("baseEntries", baseEntries), 
                TransientValueEntries.class);
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
    protected AbstractXMap() {  
    }
    
    @SuppressWarnings("unchecked")
    protected final <T extends BaseEntries<K, V>> T getBaseEntries() {
        return (T)this.baseEntries;
    }

    @Override
    public boolean isReadWriteLockSupported() {
        return this.baseEntries.isReadWriteLockSupported();
    }
    
    @Override
    public BidiType bidiType() {
        return this.baseEntries.bidiType();
    }

    @Override
    public ReplacementRule keyReplacementRule() {
        return this.baseEntries.keyReplacementRule();
    }

    @Override
    public UnifiedComparator<? super K> keyUnifiedComparator() {
        return this.baseEntries.keyUnifiedComparator();
    }

    @Override
    public UnifiedComparator<? super V> valueUnifiedComparator() {
        return this.baseEntries.valueUnifiedComparator();
    }

    @Override
    public UnifiedComparator<? super Entry<K, V>> entryUnifiedComparator() {
        return this.baseEntries.entryUnifiedComparator();
    }

    @Override
    public void addKeyValidator(Validator<K> validator) {
        this.baseEntries.combineKeyValidator(validator);
    }

    @Override
    public void removeKeyValidator(Validator<K> validator) {
        this.baseEntries.removeKeyValidator(validator);
    }
    
    @Override
    public void validateKey(K key) {
        this.baseEntries.validateKey(key);
    }

    @Override
    public void addValueValidator(Validator<V> validator) {
        this.baseEntries.combineValueValidator(validator);
    }

    @Override
    public void removeValueValidator(Validator<V> validator) {
        this.baseEntries.removeValueValidator(validator);
    }

    @Override
    public void validateValue(V value) {
        this.baseEntries.validateValue(value);
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
    public boolean isEmpty() {
        return this.baseEntries.isEmpty();
    }

    @Override
    public int size() {
        return this.baseEntries.size();
    }
    
    @Override
    public boolean containsKey(Object key) {
        return this.baseEntries.containsKey(key);
    }
    
    @Override
    public boolean containsValue(Object value) {
        return this.baseEntries.containsValue(value);
    }
    
    @Override
    public XEntry<K, V> entryOfKey(K key) {
        try {
            return new EntryOfKeyImpl<>(this, key);
        } catch (NoEntryException ex) {
            return null;
        }
    }
    
    @Override
    public XEntry<K, V> entryOfValue(V value) {
        try {
            return new EntryOfValueImpl<>(this, value);
        } catch (NoEntryException ex) {
            return null;
        }
    }

    @Override
    public V get(Object key) {
        BaseEntry<K, V> be = this.baseEntries.getBaseEntry(key);
        return be == null ? null : be.getValue();
    }

    @Override
    public V put(K key, V value) {
        return this.baseEntries.put(key, value, null);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.baseEntries.putAll(m, null);
    }

    @Override
    public void clear() {
        this.baseEntries.clear(null);
    }

    @Override
    public V remove(Object key) {
        BaseEntry<K, V> be = this.baseEntries.removeByKey(key, null);
        return be != null ? be.getValue() : null;
    }
    
    @Override
    public int hashCode() {
        return BasicAlgorithms.mapHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return BasicAlgorithms.mapEquals(this, obj);
    }

    @Override
    public String toString() {
        return BasicAlgorithms.mapToString(this);
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
    
    @Override
    protected void onWriteState(Output out) throws IOException {
        if (this.baseEntries == null) {
            throw new IllegalStateException(operationIsTooEarly(this.getClass(), "onWriteState"));
        }
        out.writeObject(this.baseEntries);
    }

    @Override
    protected void onReadState(Input in) throws ClassNotFoundException, IOException {
        if (this.baseEntries != null) {
            throw new IllegalStateException(operationIsTooEarly(this.getClass(), "onReadState"));
        }
        this.baseEntries = in.readObject();
    }

    protected static class EntrySetImpl<K, V> implements XEntrySetView<K, V> {
        
        private AbstractXMap<K, V> parentMap;
                
        protected EntrySetImpl(AbstractXMap<K, V> parentMap) {
            this.parentMap = Arguments.mustNotBeNull("parentMap", parentMap);
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractXMap<K, V>> T getParentMap() {
            return (T)this.parentMap;
        }
        
        @Deprecated
        @Override
        public final void addValidator(
                Validator<Entry<K, V>> validator)
        throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void removeValidator(
                Validator<Entry<K, V>> validator)
        throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void validate(Entry<K, V> e) {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final boolean add(Entry<K, V> e) 
        throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final boolean addAll(
                Collection<? extends java.util.Map.Entry<K, V>> c) 
        throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isReadWriteLockSupported() {
            return this.parentMap.baseEntries.isReadWriteLockSupported();
        }
        
        @Override
        public final ReplacementRule replacementRule() {
            return this.parentMap.baseEntries.keyReplacementRule();
        }

        @Override
        public final UnifiedComparator<? super Entry<K, V>> unifiedComparator() {
            return this.parentMap.baseEntries.entryUnifiedComparator();
        }

        @Override
        public boolean isEmpty() {
            return this.parentMap.baseEntries.isEmpty();
        }
        
        @Override
        public int size() {
            return this.parentMap.baseEntries.size();
        }
        
        @Override
        public boolean contains(Object o) {
            return this.parentMap.baseEntries.containsEntry(o);
        }
        
        @Override
        public boolean containsAll(Collection<?> c) {
            BaseEntries<K, V> baseEntries = this.parentMap.baseEntries; 
            for (Object o : c) {
                if (!baseEntries.containsEntry(o)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public void clear() {
            this.parentMap.baseEntries.clear(null);
        }
        
        @Override
        public boolean remove(Object o) {
            return this.parentMap.baseEntries.removeByEntry(o, null) != null;
        }
        
        @Override
        public boolean removeAll(Collection<?> c) {
            return this.parentMap.baseEntries.removeAllByEntryCollection(c, null);
        }
        
        @Override
        public boolean retainAll(Collection<?> c) {
            return this.parentMap.baseEntries.retainAllByEntryCollection(c, null); 
        }
        
        @Override
        public XEntrySetIterator<K, V> iterator() {
            return new IteratorImpl<K, V>(this);
        }
        
        @Override
        public Object[] toArray() {
            return BasicAlgorithms.collectionToArray(this);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return BasicAlgorithms.collectionToArray(this, a);
        }

        @Override
        public int hashCode() {
            return BasicAlgorithms.setHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return BasicAlgorithms.setEquals(this, obj);
        }

        @Override
        public String toString() {
            return BasicAlgorithms.collectionToString(this);
        }

        @Override
        public EntrySet viewInfo() {
            return MapViewInfos.entrySet();
        }

        protected static class IteratorImpl<K, V> implements XEntrySetIterator<K, V> {
            
            private BaseEntryIterator<K, V> baseIterator;
            
            private boolean nonFairLockSupported;
            
            private UnifiedComparator<? super Entry<K, V>> unifiedComparator;
            
            protected IteratorImpl(EntrySetImpl<K, V> parentEntrySet) {
                this.baseIterator = parentEntrySet.parentMap.baseEntries.iterator();
                this.nonFairLockSupported = parentEntrySet.parentMap.baseEntries.isReadWriteLockSupported();
                this.unifiedComparator = parentEntrySet.parentMap.baseEntries.entryUnifiedComparator();
            }

            @Override
            public boolean hasNext() {
                return this.baseIterator.hasNext();
            }

            @Override
            public XEntry<K, V> next() {
                try {
                    return new EntryImpl<K, V>(this.baseIterator.next());
                } catch (NoEntryException e) {
                    throw new AssertionError();
                }
            }

            @Override
            public void remove() {
                this.baseIterator.remove(null);
            }
            
            @Override
            public boolean isReadWriteLockSupported() {
                return this.nonFairLockSupported;
            }

            @Override
            public UnifiedComparator<? super Entry<K, V>> unifiedComparator() {
                return this.unifiedComparator;
            }

            @Override
            public CollectionViewInfos.Iterator viewInfo() {
                return CollectionViewInfos.iterator();
            }
        }
    }
    
    protected static class KeySetImpl<K, V> extends AbstractXSet<K> implements XKeySetView<K> {
        
        private AbstractXMap<K, V> parentMap;
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected KeySetImpl(AbstractXMap<K, V> parentMap) {
            super((BaseEntries)parentMap.baseEntries);
            this.parentMap = Arguments.mustNotBeNull("parentMap", parentMap);
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractXMap<K, V>> T getParentMap() {
            return (T)this.parentMap;
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
        public boolean isReadWriteLockSupported() {
            return this.parentMap.baseEntries.isReadWriteLockSupported();
        }

        @Override
        public void clear() {
            super.clear();
        }

        @Override
        public boolean remove(Object o) {
            return super.remove(o);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return super.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return super.retainAll(c);
        }

        @Override
        public KeySet viewInfo() {
            return MapViewInfos.keySet();
        }
        
    }
    
    protected static class ValuesImpl<K, V> implements XValuesView<V> {
        
        private AbstractXMap<K, V> parentMap;
        
        protected ValuesImpl(AbstractXMap<K, V> parentMap) {
            this.parentMap = Arguments.mustNotBeNull("parentMap", parentMap);
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractXMap<K, V>> T getParentMap() {
            return (T)this.parentMap;
        }
        
        @Deprecated
        @Override
        public final void addValidator(Validator<V> validator) {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void removeValidator(Validator<V> validator) {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final void validate(V e) {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final boolean add(V e) 
        throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final boolean addAll(Collection<? extends V> c) 
        throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isReadWriteLockSupported() {
            return this.parentMap.baseEntries.isReadWriteLockSupported();
        }
        
        @Override
        public UnifiedComparator<? super V> unifiedComparator() {
            return this.parentMap.baseEntries.valueUnifiedComparator();
        }

        @Override
        public boolean isEmpty() {
            return this.parentMap.baseEntries.isEmpty();
        }

        @Override
        public int size() {
            return this.parentMap.baseEntries.size();
        }
        
        @Override
        public boolean contains(Object o) {
            return this.parentMap.baseEntries.containsValue(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            BaseEntries<K, V> baseEntries = this.parentMap.baseEntries;
            for (Object o : c) {
                if (!baseEntries.containsValue(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void clear() {
            this.parentMap.baseEntries.clear(null);
        }

        @Override
        public boolean remove(Object o) {
            return this.parentMap.baseEntries.removeByValue(o, null) != null;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return this.parentMap.baseEntries.removeAllByValueCollection(c, null);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return this.parentMap.baseEntries.retainAllByValueCollection(c, null);
        }
        
        @Override
        public XIterator<V> iterator() {
            final BaseEntryIterator<K, V> beIterator = 
                this.parentMap.baseEntries.iterator();
            final boolean nonFairLockSupported = this.parentMap.baseEntries.isReadWriteLockSupported();
            final UnifiedComparator<? super V> unifiedComparator = this.parentMap.baseEntries.valueUnifiedComparator();
            return new XIterator<V>() {
                @Override
                public boolean hasNext() {
                    return beIterator.hasNext();
                }
                @Override
                public V next() {
                    return beIterator.next().getValue();
                }
                @Override
                public void remove() {
                    beIterator.remove(null);
                }
                @Override
                public boolean isReadWriteLockSupported() {
                    return nonFairLockSupported;
                }
                @Override
                public UnifiedComparator<? super V> unifiedComparator() {
                    return unifiedComparator;
                }
                @Override
                public CollectionViewInfos.Iterator viewInfo() {
                    return CollectionViewInfos.iterator();
                }
            };
        }

        @Override
        public Object[] toArray() {
            return BasicAlgorithms.collectionToArray(this);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return BasicAlgorithms.collectionToArray(this, a);
        }

        @Override
        public String toString() {
            return BasicAlgorithms.collectionToString(this);
        }

        @Override
        public Values viewInfo() {
            return MapViewInfos.values();
        }
    }
    
    public static abstract class AbstractEntryImpl<K, V> implements XEntry<K, V> {
        
        protected BaseEntry<K, V> baseEntry;

        public AbstractEntryImpl(BaseEntry<K, V> baseEntry) throws NoEntryException {
            if (baseEntry == null) {
                throw new NoEntryException();
            }
            Arguments.mustBeInstanceOfValue(
                    "baseEntry.getOwner()", 
                    baseEntry.getOwner(), 
                    AbstractBaseEntriesImpl.class);
            this.baseEntry = baseEntry;
        }
        
        @Override
        public V setValue(V value) {
            return this.baseEntry.setValue(value, null);
        }
        
        @Override
        public K getKey() {
            return this.baseEntry.getKey();
        }

        @Override
        public V getValue() {
            return this.baseEntry.getValue();
        }
        
        @Override
        public boolean isAlive() {
            return this.baseEntry.getOwner() != null;
        }

        @Override
        public boolean isReadWriteLockSupported() {
            return this.baseEntry.isNonFairLockSupported();
        }

        @Override
        public UnifiedComparator<? super K> keyUnifiedComparator() {
            return this.baseEntry.getOwner().keyUnifiedComparator();
        }

        @Override
        public UnifiedComparator<? super V> valueUnifiedComparator() {
            return this.baseEntry.getOwner().valueUnifiedComparator();
        }

        @Override
        public UnifiedComparator<? super Entry<K, V>> unifiedComparator() {
            return this.baseEntry.getOwner().entryUnifiedComparator();
        }

        @Override
        public int hashCode() {
            return this.baseEntry.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this.baseEntry.equals(obj);
        }
        
        @Override
        public String toString() {
            return this.baseEntry.toString();
        }
    }
    
    protected static class EntryImpl<K, V> extends AbstractEntryImpl<K, V> {
        
        public EntryImpl(BaseEntry<K, V> baseEntry) throws NoEntryException {
            super(baseEntry);
        }

        @Override
        public MapViewInfos.Entry viewInfo() {
            return MapViewInfos.entry();
        }
    }
    
    protected static class EntryOfKeyImpl<K, V> extends AbstractEntryImpl<K, V> {
        
        private MapViewInfos.EntryOfKey viewInfo;

        public EntryOfKeyImpl(AbstractXMap<K, V> parentMap, K key) throws NoEntryException {
            super(parentMap.baseEntries.getBaseEntry(key));
            this.viewInfo = MapViewInfos.entryOfKey(key);
        }
        
        @Override
        public MapViewInfos.EntryOfKey viewInfo() {
            return this.viewInfo;
        }
    }
    
    protected static class EntryOfValueImpl<K, V> extends AbstractEntryImpl<K, V> {
        
        private MapViewInfos.EntryOfValue viewInfo;

        public EntryOfValueImpl(AbstractXMap<K, V> parentMap, V value) throws NoEntryException {
            super(parentMap.baseEntries.getBaseEntryByValue(value));
            this.viewInfo = MapViewInfos.entryOfValue(value);
        }
        
        @Override
        public MapViewInfos.EntryOfValue viewInfo() {
            return this.viewInfo;
        }
    }
    
    public static class SimpleImpl<K, V> extends AbstractXMap<K, V> {

        public SimpleImpl(BaseEntries<K, V> baseEntries) {
            super(baseEntries);
        }
    }
    
    @I18N
    private static native String operationIsTooEarly(Class<?> thisType, String operationName);
}
