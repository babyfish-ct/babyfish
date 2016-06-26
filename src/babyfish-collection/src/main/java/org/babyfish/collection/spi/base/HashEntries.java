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
package org.babyfish.collection.spi.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.FrozenEqualityComparator;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.spi.base.AbstractBaseEntriesImpl.Trigger.History;
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public class HashEntries<K, V> extends AbstractRootBaseEntriesImpl<K, V> implements BaseEntries<K, V> {
    
    private static final long serialVersionUID = 8630263080651900905L;
    
    private static final int MIN_CAPACITY = 2;
    
    private static final int MAX_CAPACITY = 1 << 30;
    
    private BaseEntryImpl<K, V>[] buckets;

    private int size;
    
    private Float loadFactor;
    
    private int initCapacity;
    
    private int threshold;
    
    int modCount;
    
    @SuppressWarnings("unchecked")
    public HashEntries(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Object valueComparatorOrEqualityComparatorOrUnifiedComparator,
            int initCapacity, 
            Float loadFactor) {
        super(
                bidiType,
                keyReplacementRule, 
                keyEqualityComparator, 
                valueComparatorOrEqualityComparatorOrUnifiedComparator);
        Arguments.mustBeLessThanOrEqualToValue("initCapacity", initCapacity, MAX_CAPACITY);
        if (initCapacity < MIN_CAPACITY) {
            initCapacity = MIN_CAPACITY;
        }
        int c = MIN_CAPACITY;
        while (c < initCapacity) {
            c <<= 1;
        }
        initCapacity = c;
    
        if (loadFactor != null) {
            Arguments.mustBeGreaterThanValue("loadFactor.floatValue()", loadFactor.floatValue(), 0);
            this.threshold = (int)(initCapacity * loadFactor);
        }
        this.buckets = new HashEntries.BaseEntryImpl[initCapacity];
        this.loadFactor = loadFactor;
        this.initCapacity = initCapacity;
        this.init();
    }
    
    @Override
    public boolean isReadWriteLockSupported() {
        return true;
    }
        
    @Override
    public int size() {
        return this.size;
    }
    
    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }
    
    @SuppressWarnings("unchecked")
    public EqualityComparator<? super K> equalityComparator() {
        return (EqualityComparator<? super K>)this.keyComparatorOrEqualityComparator();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final BaseEntryImpl<K, V> getBaseEntry(Object key) {
        BaseEntryImpl<K, V>[] buckets = this.buckets;
        if (key == null) {
            for (BaseEntryImpl<K, V> e = buckets[0]; e != null; e = e.next) {
                if (e.key == null) {
                    return e;
                }
            }
            return null;
        }
        UnifiedComparator<? super K> keyUnifiedComparator = this.keyUnifiedComparator();
        int hash = hash(keyUnifiedComparator.hashCode((K)key));
        int bucketIndex = (buckets.length - 1) & hash;
        for (BaseEntryImpl<K, V> e = buckets[bucketIndex]; e != null; e = e.next) {
            if (e.hash == hash) {
                K k = e.key;
                if (keyUnifiedComparator.equals((K)key, k)) { 
                    return e;
                }
            }
        }
        return null;
    }
    
    @Override
    protected V putWithoutTriggerFlushing(
            K key, 
            V value, 
            AttachProcessor<K, V> attachProcessor) {
        
        History<K, V> puttingHistory = attachProcessor.getPuttingHistory();
        boolean isKeyStrict = this.keyReplacementRule() == ReplacementRule.NEW_REFERENCE_WIN;
        V oldV = null;
        
        if (attachProcessor.beginExcute()) {
            try {
                BaseEntryImpl<K, V> be = 
                        puttingHistory != null ?
                        (BaseEntryImpl<K, V>)puttingHistory.getBaseEntry(0) :
                        this.getBaseEntry(key);
                if (be != null) {
                    oldV = this.changeEntry(
                            be, 
                            key, 
                            value, 
                            isKeyStrict, 
                            this.keyComparatorOrEqualityComparator()
                    );
                } else {
                    if (key == null) {
                        this.addEntry(0, null, value, 0);
                    } else {
                        int hash = hash(this.keyUnifiedComparator().hashCode(key));
                        int bucketIndex = (this.buckets.length - 1) & hash;
                        this.addEntry(hash, key, value, bucketIndex);
                    }
                    this.scale(true);
                }
                attachProcessor.endExecute(null);
            } catch (RuntimeException | Error ex) {
                attachProcessor.endExecute(ex);
            }
        }
        return oldV;
    }
    
    @Override
    protected final void putAllWithoutTriggerFlushing(
            Map<? extends K, ? extends V> m, 
            AttachProcessor<K, V> attachProcessor) {
        
        History<K, V> puttingHistory = attachProcessor.getPuttingHistory();
        UnifiedComparator<? super K> keyUnifiedComparator = this.keyUnifiedComparator();
        boolean isKeyStrict = this.keyReplacementRule() == ReplacementRule.NEW_REFERENCE_WIN;
        Object keyEqualityComparator = this.keyComparatorOrEqualityComparator();
        
        if (puttingHistory == null) {
            if (attachProcessor.beginExcute()) {
                try {
                    for (Entry<? extends K, ? extends V> e : m.entrySet()) {
                        K key = e.getKey();
                        BaseEntryImpl<K, V> be = this.getBaseEntry(key);
                        if (be != null) {
                            this.changeEntry(
                                    be, 
                                    key, 
                                    e.getValue(), 
                                    isKeyStrict, 
                                    keyEqualityComparator
                            );
                        } else {
                            if (key == null) {
                                this.addEntry(0, null, e.getValue(), 0);
                            } else {
                                int hash = hash(keyUnifiedComparator.hashCode(key));
                                this.addEntry(hash, key, e.getValue(), (this.buckets.length - 1) & hash);
                            }
                        }
                    }
                    this.scale(true);
                    attachProcessor.endExecute(null);
                } catch (RuntimeException | Error ex) {
                    attachProcessor.endExecute(ex);
                }
            }
        } else {
            if (attachProcessor.beginExcute()) {
                try {
                    int count = puttingHistory.getCount();
                    for (int i = 0; i < count; i++) {
                        BaseEntryImpl<K, V> be = (BaseEntryImpl<K, V>)puttingHistory.getBaseEntry(i);
                        K key = puttingHistory.getNewKey(i);
                        V value = puttingHistory.getNewValue(i);
                        if (be != null) {
                            this.changeEntry(
                                    be, 
                                    key, 
                                    value, 
                                    isKeyStrict, 
                                    keyEqualityComparator
                            );
                        } else {
                            if (key == null) {
                                this.addEntry(0, null, value, 0);
                            } else {
                                int hash = hash(keyUnifiedComparator.hashCode(key));
                                this.addEntry(hash, key, value, (this.buckets.length - 1) & hash);
                            }
                        }
                    }
                    this.scale(true);
                    attachProcessor.endExecute(null);
                } catch (RuntimeException | Error ex) {
                    attachProcessor.endExecute(ex);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected final boolean addAllWithoutTriggerFlushing(
            Collection<? extends K> c,
            Trigger<K, V> trigger) {
        
        // In this method, inversedEntries always is null
        
        UnifiedComparator<? super K> keyUnifiedComparator = this.keyUnifiedComparator();
        boolean isKeyStrict = this.keyReplacementRule() == ReplacementRule.NEW_REFERENCE_WIN;
        int oldSize = this.size;
        Object keyEqualityComparator = this.keyComparatorOrEqualityComparator();
        
        if (trigger == null) {
            for (K key : c) {
                BaseEntryImpl<K, V> be = this.getBaseEntry(key);
                if (be != null) {
                    this.changeEntry(
                            be, 
                            key, 
                            (V)PRESENT, 
                            isKeyStrict, 
                            keyEqualityComparator
                    );
                } else {
                    if (key == null) {
                        this.addEntry(0, null, (V)PRESENT, 0);
                    } else {
                        int hash = hash(keyUnifiedComparator.hashCode(key));
                        this.addEntry(hash, key, (V)PRESENT, (this.buckets.length - 1) & hash);
                    }
                }
            }
            this.scale(true);
        } else {
            int cSize = c.size();
            for (K key : c) {
                BaseEntryImpl<K, V> be = this.getBaseEntry(key);
                if (be != null) {
                    trigger.preChange(be, isKeyStrict ? key : be.getKey(), (V)PRESENT);
                } else {
                    trigger.preAdd(key, (V)PRESENT);
                }
            }
            if (trigger.beginExecute()) {
                try {
                    History<K, V> history = trigger.getHistory(0);
                    for (int i = 0; i < cSize; i++) {
                        K key = history.getNewKey(i);
                        BaseEntryImpl<K, V> be = (BaseEntryImpl<K, V>)history.getBaseEntry(i);
                        if (be != null) {
                            this.changeEntry(
                                    be, 
                                    key, 
                                    (V)PRESENT, 
                                    isKeyStrict, 
                                    keyEqualityComparator
                            );
                        } else {
                            if (key == null) {
                                this.addEntry(0, null, (V)PRESENT, 0);
                            } else {
                                int hash = hash(keyUnifiedComparator.hashCode(key));
                                int bucketIndex = (this.buckets.length - 1) & hash;
                                this.addEntry(hash, key, (V)PRESENT, bucketIndex);
                            }
                        }
                    }
                    this.scale(true);
                    trigger.endExecute(null);
                } catch (RuntimeException | Error ex) {
                    trigger.endExecute(ex);
                }
            }
            trigger.flush();
        }
        return oldSize != this.size;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void clear(Trigger<K, V> trigger) {
        if (trigger != null) {
            BaseEntryIterator<K, V> iterator = this.iterator();
            while (iterator.hasNext()) {
                trigger.preRemove(iterator.next());
            }
        }
        if (trigger == null || trigger.beginExecute()) {
            try {
                BaseEntryIterator<K, V> iterator = this.iterator();
                while (iterator.hasNext()) {
                    BaseEntryImpl<K, V> be = (BaseEntryImpl<K, V>)iterator.next();
                    be.recordRemove(true);
                }
                this.buckets = new BaseEntryImpl[this.initCapacity];
                if (this.loadFactor != null) {
                    this.threshold = (int)(this.initCapacity * this.loadFactor);
                }
                BaseEntries<V, K> inversedEntries = this.inversedEntries();
                if (inversedEntries != null) {
                    inversedEntries.clear(null);
                }
                this.modCount++;
                this.size = 0;
                this.afterAllRecordsRemove();
                if (trigger != null) {
                    trigger.endExecute(null);
                }
            } catch (RuntimeException | Error ex) {
                if (trigger == null) {
                    throw ex;
                }
                trigger.endExecute(ex);
            }
        }
        if (trigger != null) {
            trigger.flush();
        }
    }
    
    @Override
    protected BaseEntry<K, V> removeByEntry(Object o, Trigger<K, V> trigger) {
        BaseEntry<K, V> retVal = super.removeByEntry(o, trigger);
        this.scale(false);
        return retVal;
    }

    @Override
    protected BaseEntry<K, V> removeByKey(Object o, Trigger<K, V> trigger) {
        BaseEntry<K, V> retVal = super.removeByKey(o, trigger);
        this.scale(false);
        return retVal;
    }

    @Override
    protected BaseEntry<K, V> removeByValue(Object o, Trigger<K, V> trigger) {
        BaseEntry<K, V> retVal = super.removeByValue(o, trigger);
        this.scale(false);
        return retVal;
    }

    @Override
    protected boolean removeAllByEntryCollection(Collection<?> ec, Trigger<K, V> trigger) {
        boolean retVal = super.removeAllByEntryCollection(ec, trigger);
        this.scale(false);
        return retVal;
    }

    @Override
    protected boolean removeAllByKeyCollection(Collection<?> kc, Trigger<K, V> trigger) {
        boolean retVal = super.removeAllByKeyCollection(kc, trigger);
        this.scale(false);
        return retVal;
    }

    @Override
    protected boolean removeAllByValueCollection(Collection<?> vc, Trigger<K, V> trigger) {
        boolean retVal = super.removeAllByValueCollection(vc, trigger);
        this.scale(false);
        return retVal;
    }

    @Override
    protected boolean retainAllByEntryCollection(Collection<?> ec, Trigger<K, V> trigger) {
        boolean retVal = super.retainAllByEntryCollection(ec, trigger);
        this.scale(false);
        return retVal;
    }

    @Override
    protected boolean retainAllByKeyCollection(Collection<?> kc, Trigger<K, V> trigger) {
        boolean retVal = super.retainAllByKeyCollection(kc, trigger);
        this.scale(false);
        return retVal;
    }

    @Override
    protected boolean retainAllByValueCollection(Collection<?> vc, Trigger<K, V> trigger) {
        boolean retVal = super.retainAllByValueCollection(vc, trigger);
        this.scale(false);
        return retVal;
    }

    @Override
    public BaseEntryIterator<K, V> iterator() {
        return this.new EntryIterator();
    }

    @Override
    public int modCount() {
        return this.modCount;
    }
    
    @Override
    protected final void deleteBaseEntry(BaseEntry<K, V> be) {
        this.deleteBaseEntryImpl((BaseEntryImpl<K, V>)be);
    }

    protected void deleteBaseEntryImpl(BaseEntryImpl<K, V> be) {
        
        this.modCount++;
        BaseEntryImpl<K, V>[] buckets = this.buckets;
        int bucketIndex = (buckets.length - 1) & be.hash;
        BaseEntryImpl<K, V> prev = buckets[bucketIndex];
        BaseEntryImpl<K, V> ce = prev;
        while (ce != null) {
            BaseEntryImpl<K, V> next = ce.next;
            if (ce == be) {
                if (prev == ce) {
                    buckets[bucketIndex] = next;
                } else {
                    prev.next = next;
                }
                this.size--;
                be.recordRemove(false);
                BaseEntries<V, K> inversedEntries = this.inversedEntries();
                if (inversedEntries != null) {
                    inversedEntries.removeByKey(be.value, null);
                }
                return;
            }
            prev = ce;
            ce = next;
        }
    }
    
    /* virtual */ void transfer(BaseEntryImpl<K, V>[] newEntries) {
        BaseEntryImpl<K, V>[] buckets = this.buckets;
        int bucketCount = buckets.length;
        for (int i = 0; i < bucketCount; i++) {
            BaseEntryImpl<K, V> e = buckets[i];
            if (e != null) {
                buckets[i] = null;
                do {
                    BaseEntryImpl<K, V> next = e.next;
                    int newBucketIndex = (newEntries.length - 1) & e.hash;
                    e.next = newEntries[newBucketIndex];
                    newEntries[newBucketIndex] = e;
                    e = next;
                } while (e != null);
            }
        }
    }
    
    /* virtual */ BaseEntryImpl<K, V> createBaseEntry(
            int hash, K key, V value, BaseEntryImpl<K, V> next) {
        return new BaseEntryImpl<K, V>(this, hash, key, value, next);
    }
    
    /* virtual */ void afterAllRecordsRemove() {
        
    }
    
    /* virtual */ void init() {
        
    }
    
    @SuppressWarnings("unchecked")
    final void scale(boolean scaleUp) {
        if (this.loadFactor == null) {
            return;
        }
        int newCapacity = buckets.length;
        float loadFactor = this.loadFactor.floatValue();
        int size = this.size;
        int twiceSize = size << 1;
        if (scaleUp) {
            if (size > this.threshold) {
                do {
                    newCapacity <<= 1;
                } while ((int)(newCapacity * loadFactor) < size);
            }
        } else {
            if (twiceSize < this.threshold) {
                do {
                    newCapacity >>>= 1;
                } while (newCapacity * loadFactor > twiceSize);
            }
        }
        if (newCapacity < this.initCapacity) {
            newCapacity = this.initCapacity;
        } else if (newCapacity > MAX_CAPACITY) {
            newCapacity = MAX_CAPACITY;
        }
        if (buckets.length == newCapacity) {
            return;
        }
        BaseEntryImpl<K, V>[] buckets = this.buckets;
        if (buckets.length == MAX_CAPACITY) {
            return;
        }
        BaseEntryImpl<K, V>[] newEntries = new BaseEntryImpl[newCapacity];
        this.transfer(newEntries);
        this.buckets = newEntries;
        this.threshold = (int)(newCapacity * this.loadFactor);
    }
    
    private void addEntry(
            int hash, 
            K key, 
            V value, 
            int bucketIndex) {
        BaseEntryImpl<K, V>[] buckets = this.buckets;
        BaseEntryImpl<K, V> e = buckets[bucketIndex];
        buckets[bucketIndex] = this.createBaseEntry(hash, key, value, e);
        BaseEntries<V, K> inversedEntries = this.inversedEntries();
        if (inversedEntries != null) {
            inversedEntries.put(value, key, null);
        }
        this.size++;
        this.modCount++;
    }
    
    private V changeEntry(
            BaseEntryImpl<K, V> be,
            K newKey,
            V newValue,
            boolean isKeyStrict, // Unnecessary parameter for optimization
            Object keyEqualityComparator //Unnecessary parameter for optimization
        ) {
        V oldValue = be.value;
        if (isKeyStrict) {
            be.setRawKey(newKey, keyEqualityComparator);
        }
        be.value = newValue;
        be.recordChange();
        BaseEntries<V, K> inversedEntries = this.inversedEntries();
        if (inversedEntries != null) {
            inversedEntries.removeByKey(oldValue, null);
            inversedEntries.put(newValue, be.key, null);
        }
        return oldValue;
    }

    private static int hash(int h) {
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.initCapacity);
        out.writeObject(this.loadFactor);
        out.writeInt(this.size);
        out.writeInt(this.buckets.length);
        BaseEntryIterator<K, V> beItr = this.iterator();
        if (this instanceof TransientValueEntries) {
            while (beItr.hasNext()) {
                BaseEntryImpl<K, V> entry = (BaseEntryImpl<K, V>)beItr.next();
                out.writeObject(entry.key);
            } 
        } else {
            while (beItr.hasNext()) {
                BaseEntryImpl<K, V> entry = (BaseEntryImpl<K, V>)beItr.next();
                out.writeObject(entry.key);
                out.writeObject(entry.value);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.initCapacity = in.readInt();
        this.loadFactor = (Float)in.readObject();
        int size = in.readInt();
        this.buckets = new BaseEntryImpl[in.readInt()];
        this.threshold = (int)(this.buckets.hashCode() * this.loadFactor);
        this.init();
        if (this instanceof TransientValueEntries) {
            for (int i = size - 1; i >= 0; i--) {
                K key = (K)in.readObject();
                this.put(key, (V)PRESENT, null, null);
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                K key = (K)in.readObject();
                V value = (V)in.readObject();
                this.put(key, value, null, null);
            }
        }
    }

    private class EntryIterator implements BaseEntryIterator<K, V> {
        
        private int nextIndex;
        
        private BaseEntryImpl<K, V> next;
        
        private BaseEntryImpl<K, V> current;
        
        private int expectedModCount;
        
        public EntryIterator() {
            BaseEntryImpl<K, V>[] buckets = HashEntries.this.buckets;
            while (this.nextIndex < buckets.length) {
                if ((this.next = buckets[this.nextIndex++]) != null) {
                    break;
                }
            }
            this.expectedModCount = HashEntries.this.modCount;
        }
        
        protected Trigger<K, V> trigger(BaseEntriesHandler<K, V> handler) {
            return HashEntries.this.triggerOf(handler);
        }
    
        @Override
        public boolean hasNext() {
            return this.next != null;
        }
    
        @Override
        public BaseEntryImpl<K, V> next() {
            if (this.expectedModCount != HashEntries.this.modCount) {
                throw new ConcurrentModificationException(IteratorMessages.concurrentModifcation());
            }
            BaseEntryImpl<K, V> be = this.next;
            if (be == null) {
                throw new NoSuchElementException(IteratorMessages.noSuchElement());
            }
            if ((this.next = be.next) == null) {
                BaseEntryImpl<K, V>[] buckets = HashEntries.this.buckets;
                while (this.nextIndex < buckets.length) {
                    if ((this.next = buckets[this.nextIndex++]) != null) {
                        break;
                    }
                }
            }
            return this.current = be;
        }
    
        @Override
        public void remove(BaseEntriesHandler<K, V> handler) {
            if (this.current == null) {
                throw new IllegalStateException(IteratorMessages.removeNoExtractedElement());
            }
            if (this.expectedModCount != HashEntries.this.modCount) {
                throw new ConcurrentModificationException(IteratorMessages.concurrentModifcation());
            }
            Trigger<K, V> trigger = this.trigger(handler);
            if (trigger != null) {
                trigger.preRemove(this.current);
            }
            if (trigger == null || trigger.beginExecute()) {
                try {
                    HashEntries.this.deleteBaseEntryImpl(this.current);
                    this.current = null;
                    this.expectedModCount = HashEntries.this.modCount;
                    if (trigger != null) {
                        trigger.endExecute(null);
                    }
                } catch (RuntimeException | Error ex) {
                    if (trigger == null) {
                        throw ex;
                    }
                    trigger.endExecute(ex);
                }
            }
            if (trigger != null) {
                trigger.flush();
            }
        }
        
    }

    static class BaseEntryImpl<K, V> extends AbstractBaseEntryImpl<K, V> {
        
        /**
         * This field could be used like the "HashEntries.this" 
         * if this class was a non-static inner class of HashEntries<K, V>.
         * But it can be null after this object has be deleted from HashEntries.
         */
        HashEntries<K, V> owner;
        
        private int hash;
        
        private K key;
        
        private V value;
        
        private BaseEntryImpl<K, V> next;
        
        private UnifiedComparator<? super Entry<K, V>> unifiedComparator;
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public BaseEntryImpl(HashEntries<K, V> owner, int hash, K key, V value, BaseEntryImpl<K, V> next) {
            if (key != null) {
                Object keyEqualityComparator = owner.keyComparatorOrEqualityComparator();
                if (keyEqualityComparator instanceof FrozenEqualityComparator<?>) {
                    FrozenEqualityComparator<? super K> frozenEqualityComparator =
                            (FrozenEqualityComparator<? super K>)keyEqualityComparator;
                    frozenEqualityComparator.freeze(key, (FrozenContext)FrozenContext.create(owner));
                }
                this.key = key;
            }
            this.owner = owner;
            this.hash = hash;
            this.value = value;
            this.next = next;
            this.unifiedComparator = owner != null ? owner.entryUnifiedComparator() : null;
        }
        
        @Override
        public boolean isNonFairLockSupported() {
            return true;
        }

        @Override
        public BaseEntries<K, V> getOwner() {
            return this.owner;
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return this.value;
        }
        
        @Override
        protected void setRawValue(V value) {
            this.value = value;
        }

        @Override
        protected UnifiedComparator<? super Entry<K, V>> unifiedComparator() {
            return this.unifiedComparator;
        }

        /* virtual */ void recordChange() {
            
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        /* virtual */ void recordRemove(boolean clearAll) {
            HashEntries<K, V> owner = this.owner;
            K oldKey = this.key;
            this.owner = null;
            
            if (oldKey != null) {
                Object keyEqualityComparator = owner.keyComparatorOrEqualityComparator();
                if (keyEqualityComparator instanceof FrozenEqualityComparator<?>) {
                    FrozenEqualityComparator<? super K> frozenEqualityComparator =
                            (FrozenEqualityComparator<? super K>)keyEqualityComparator;
                    frozenEqualityComparator.unfreeze(
                            oldKey, 
                            (FrozenContext)FrozenContext.create(owner));
                }
            }
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void setRawKey(K key, Object equalityComparator) {
            /*
             * Actually, the parameter "equalityComparator" is unnecessary,
             * because it is very easy to get such as these methods
             *  
             * (1) this.owner.keyComparatorOrEqualityComparator()
             * (2) this.owner.keyUnifiedComparator().equalityComparator()
             * (3) this.owner.equalityComparator()
             * 
             * in owner.putAllImpl and owner.addAllImpl, this method
             * will be called for several times by a loop statement,
             * so the the outside method can store it as a local variable and
             * use it to be the second argument of this method in loop body
             * to optimize the program.
             * 
             * The parameter is Object, 
             * that means I choose the owner.keyComparatorOrEqualityComparator()
             * neither owner.keyUnifiedComparator().equalityComparator() 
             * nor owner.equalityComparator()
             * because it is the fastest one of them.
             * 
             * It is hard to understand why that is faster than this.equalityComparator()
             * because
             * "Object --CHECKCAST--> FrozenEqualityComparator"
             * is fast than
             * "Object --CHECKCAST--> EqualityComparator --CHECKCAST--> FrozenEqualityComparator"
             */
            K oldKey = this.key;
            if (oldKey != key) {
                if (equalityComparator instanceof FrozenEqualityComparator<?>) {
                    FrozenContext ctx = FrozenContext.create(this.owner);
                    FrozenEqualityComparator<? super K> frozenEqualityComparator =
                            (FrozenEqualityComparator<? super K>)equalityComparator;
                    if (key != null) {
                        frozenEqualityComparator.freeze(key, ctx);
                    }
                    this.key = key;
                    if (oldKey != null) {
                        frozenEqualityComparator.unfreeze(oldKey, ctx);
                    }
                } else {
                    this.key = key;
                }
            }
        }
        
    }
    
    public static class TransientValue<K, V> extends HashEntries<K, V> implements TransientValueEntries {

        private static final long serialVersionUID = 5186650749530128201L;

        public TransientValue(
                ReplacementRule keyReplacementRule,
                EqualityComparator<? super K> keyEqualityComparator,
                Object valueComparatorOrEqualityComparatorOrUnifiedComparator,
                int initCapacity, 
                Float loadFactor) {
            super(
                    BidiType.NONE,
                    keyReplacementRule, 
                    keyEqualityComparator,
                    valueComparatorOrEqualityComparatorOrUnifiedComparator, 
                    initCapacity,
                    loadFactor);
        }
        
    }
}
