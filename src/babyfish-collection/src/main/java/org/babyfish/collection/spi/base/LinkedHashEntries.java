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

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.BidiType;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.ReplacementRule;

/**
 * @author Tao Chen
 */
public class LinkedHashEntries<K, V> extends HashEntries<K, V> implements OrderedBaseEntries<K, V> {
    
    private static final long serialVersionUID = -7604688148083409576L;
    
    private static final OrderAdjustMode[] OAM_VALUES = OrderAdjustMode.values();
    
    private int initFlags;
    
    private transient BaseEntryImpl<K, V> invalid;
    
    private transient OrderedBaseEntries<K, V> descendingEntries;
    
    public LinkedHashEntries(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            EqualityComparator<? super K> keyEqualityComparator,
            Object valueComparatorOrEqualityComparatorOrUnifiedComparator,
            int initCapacity, 
            Float loadFactor,
            boolean headAppend,
            OrderAdjustMode accessMode,
            OrderAdjustMode replaceMode) {
        super(
                bidiType,
                keyReplacementRule, 
                keyEqualityComparator, 
                valueComparatorOrEqualityComparatorOrUnifiedComparator, 
                initCapacity, 
                loadFactor);
        this.initFlags = 
            (headAppend ? 0x01 : 0x00) |
            (accessMode.ordinal() << 8) |
            (replaceMode.ordinal() << 16);
    }

    @Override
    public boolean headAppend() {
        return (this.initFlags & 0x01) != 0;
    }
    
    @Override
    public OrderAdjustMode accessMode() {
        return OAM_VALUES[(this.initFlags >>> 8) & 0xFF];
    }

    @Override
    public OrderAdjustMode replaceMode() {
        return OAM_VALUES[(this.initFlags >>> 16) & 0xFF];
    }

    @Override
    public OrderedBaseEntries<K, V> descendingEntries() {
        if (this.descendingEntries == null) {
            this.descendingEntries = this.new DescendingEntries();
        }
        return this.descendingEntries;
    }

    @Override
    public BaseEntry<K, V> first() {
        BaseEntry<K, V> be = this.invalid.after;
        return be == this.invalid ? null : be;
    }

    @Override
    public BaseEntry<K, V> last() {
        BaseEntry<K, V> be = this.invalid.before;
        return be == this.invalid ? null : be;
    }
    
    @Override
    public final BaseEntry<K, V> access(K key, BaseEntriesHandler<K, V> handler) {
        return this.access(key, this.triggerOf(handler));
    }
    
    @Override
    public final BaseEntry<K, V> pollFirst(BaseEntriesHandler<K, V> handler) {
        return this.pollFirst(this.triggerOf(handler));
    }
    
    @Override
    public final BaseEntry<K, V> pollLast(BaseEntriesHandler<K, V> handler) {
        return this.pollLast(this.triggerOf(handler));
    }
    
    protected BaseEntry<K, V> access(K key, Trigger<K, V> trigger) {
        BaseEntryImpl<K, V> be = (BaseEntryImpl<K, V>)this.getBaseEntry(key);
        
        if (be == null) {
            return null;
        }
        
        OrderAdjustMode accessMode = this.accessMode();
        if (accessMode == null || accessMode == OrderAdjustMode.NONE) {
            return be;
        }
        
        switch (accessMode) {
        case HEAD:
        case PREV:
            if (be.isHead()) {
                return be;
            }
            break;
        case TAIL:
        case NEXT:
            if (be.isTail()) {
                return be;
            }
            break;
        default:
        }
        
        if (trigger == null) {
            switch (accessMode) {
            case HEAD:
                be.moveToHead();
                break;
            case TAIL:
                be.moveToTail();
                break;
            case PREV:
                be.moveToPrev();
                break;
            case NEXT:
                be.moveToNext();
                break;
            default:
            }
        } else {
            trigger.preChange(be, be.getKey(), be.getValue());
            if (trigger.beginExecute()) {
                try {
                    switch (accessMode) {
                    case HEAD:
                        be.moveToHead();
                        break;
                    case TAIL:
                        be.moveToTail();
                        break;
                    case PREV:
                        be.moveToPrev();
                        break;
                    case NEXT:
                        be.moveToNext();
                        break;
                    default:
                    }
                    trigger.endExecute(null);
                } catch (RuntimeException | Error ex) {
                    trigger.endExecute(ex);
                }
            }
            trigger.flush();
        }
        
        return be;
    }

    protected BaseEntry<K, V> pollFirst(Trigger<K, V> trigger) {
        BaseEntryImpl<K, V> be = this.invalid.after;
        if (be != null) {
            if (trigger == null) {
                this.deleteBaseEntryImpl(be);
            } else {
                trigger.preRemove(be);
                if (trigger.beginExecute()) {
                    try {
                        this.deleteBaseEntryImpl(be);
                        trigger.endExecute(null);
                    } catch (RuntimeException | Error ex) {
                        trigger.endExecute(ex);
                    }
                }
                trigger.flush();
            }
        }
        return be;
    }

    protected BaseEntry<K, V> pollLast(Trigger<K, V> trigger) {
        BaseEntryImpl<K, V> be = this.invalid.before;
        if (be != null) {
            if (trigger == null) {
                this.deleteBaseEntryImpl(be);
            } else {
                trigger.preRemove(be);
                if (trigger.beginExecute()) {
                    try {
                        this.deleteBaseEntryImpl(be);
                        trigger.endExecute(null);
                    } catch (RuntimeException | Error ex) {
                        trigger.endExecute(ex);
                    }
                }
                trigger.flush();
            }
        }
        return be;
    }

    @Override
    BaseEntryImpl<K, V> createBaseEntry(
            int hash, K key, V value, HashEntries.BaseEntryImpl<K, V> next) {
        return new BaseEntryImpl<K, V>(this, hash, key, value, next);
    }
    
    @Override
    void afterAllRecordsRemove() {
        this.invalid.before = this.invalid.after = this.invalid;
    }

    @Override
    void init() {
        this.invalid = new BaseEntryImpl<>();
    }

    @Override
    public BaseEntryIterator<K, V> iterator() {
        return this.new AscendingEntryIterator();
    }

    @Override
    protected LinkedFrozenContextSuspending<K, V> createFrozenContextSuspending(BaseEntry<K, V> be) {
        return new LinkedFrozenContextSuspending<>(be);
    }

    @Override
    protected void onFrozenContextResumed(FrozenContextSuspending<K, V> suspending) {
        BaseEntryImpl<K, V> be = (BaseEntryImpl<K, V>)this.getBaseEntry(suspending.getKey());
        BaseEntryImpl<K, V> suspendAfter = ((LinkedFrozenContextSuspending<K, V>)suspending).getAfter();
        if (suspendAfter.getOwner() != null) {
            be.moveBefore(suspendAfter);
        }
    }

    static class BaseEntryImpl<K, V> extends HashEntries.BaseEntryImpl<K, V> {
        
        BaseEntryImpl<K, V> before;
        
        BaseEntryImpl<K, V> after;
        
        /**
         * Only for {@link LinkedHashEntries#invalid
         */
        BaseEntryImpl() {
            super(null, 0, null, null, null);
            this.before = this.after = this;
        }

        BaseEntryImpl(LinkedHashEntries<K, V> owner, int hash, K key, V value, HashEntries.BaseEntryImpl<K, V> next) {
            super(owner, hash, key, value, next);
            assert owner != null;
            if (owner.headAppend()) {
                this.before = owner.invalid;
                this.after = owner.invalid.after;
            } else {
                this.after = owner.invalid;
                this.before = owner.invalid.before;
            }
            this.before.after = this;
            this.after.before = this;
        }
        
        @Override
        void recordChange() {
            LinkedHashEntries<K, V> linkedOwner = (LinkedHashEntries<K, V>)this.owner;
            OrderAdjustMode replaceMode = linkedOwner.replaceMode();
            if (OrderAdjustMode.HEAD.equals(replaceMode)) {
                this.moveToHead();
            } else if (OrderAdjustMode.TAIL.equals(replaceMode)) {
                this.moveToTail();
            } else if (OrderAdjustMode.PREV.equals(replaceMode)) {
                this.moveToPrev();
            } else if (OrderAdjustMode.NEXT.equals(replaceMode)) {
                this.moveToNext();
            }
        }

        @Override
        void recordRemove(boolean clearAll) {
            /*
             * When clearAll is true
             * In order to optimize the program, owner.head and owner.tail is not clear here.
             * They are clean in {@link LinkedHashEntries#afterAllRecordsRemove()} 
             */
            if (!clearAll) {
                this.before.after = this.after;
                this.after.before = this.before;
            }
            
            super.recordRemove(clearAll);
        }

        void moveToHead() {
            BaseEntryImpl<K, V> invalid = ((LinkedHashEntries<K, V>)this.owner).invalid;
            if (this != invalid && this.before != invalid) {
                this.before.after = this.after;
                this.after.before = this.before;
                this.before = invalid;
                this.after = invalid.after;
                this.before.after = this;
                this.after.before = this;
            }
        }
        
        void moveToTail() {
            BaseEntryImpl<K, V> invalid = ((LinkedHashEntries<K, V>)this.owner).invalid;
            if (this != invalid && this.after != invalid) {
                this.before.after = this.after;
                this.after.before = this.before;
                this.after = invalid;
                this.before = invalid.before;
                this.before.after = this;
                this.after.before = this;
            }
        }
        
        void moveToPrev() {
            BaseEntryImpl<K, V> invalid = ((LinkedHashEntries<K, V>)this.owner).invalid;
            BaseEntryImpl<K, V> target = this.before;
            if (this != invalid && target != invalid) {
                this.before.after = this.after;
                this.after.before = this.before;
                this.after = target;
                this.before = target.before;
                this.before.after = this;
                this.after.before = this;
            }
        }
        
        void moveToNext() {
            BaseEntryImpl<K, V> invalid = ((LinkedHashEntries<K, V>)this.owner).invalid;
            BaseEntryImpl<K, V> target = this.after;
            if (this != invalid && target != invalid) {
                this.before.after = this.after;
                this.after.before = this.before;
                this.before = target;
                this.after = target.after;
                this.before.after = this;
                this.after.before = this;
            }
        }
        
        void moveBefore(BaseEntry<K, V> target) {
            BaseEntryImpl<K, V> invalid = ((LinkedHashEntries<K, V>)this.owner).invalid;
            if (this != target && this != invalid && target != invalid) {
                BaseEntryImpl<K, V> tgtImpl = (BaseEntryImpl<K, V>)target;
                this.before.after = this.after;
                this.after.before = this.before;
                this.after = tgtImpl;
                this.before = tgtImpl.before;
                this.before.after = this;
                this.after.before = this;
            }
        }
        
        boolean isHead() {
            BaseEntryImpl<K, V> invalid = ((LinkedHashEntries<K, V>)this.owner).invalid;
            return this == invalid.after;
        }
        
        boolean isTail() {
            BaseEntryImpl<K, V> invalid = ((LinkedHashEntries<K, V>)this.owner).invalid;
            return this == invalid.before;
        }
    }
    
    protected static class LinkedFrozenContextSuspending<K, V> extends FrozenContextSuspending<K, V> {

        private BaseEntryImpl<K, V> after;
        
        protected LinkedFrozenContextSuspending(BaseEntry<K, V> be) {
            super(be);
            this.after = ((BaseEntryImpl<K, V>)be).after;
        }
        
        public BaseEntryImpl<K, V> getAfter() {
            return this.after;
        }

        @Override
        protected void onConflictBaseEntryDeleted(BaseEntry<K, V> conflictBaseEntry) {
            if (this.after == conflictBaseEntry) {
                this.after = ((BaseEntryImpl<K, V>)conflictBaseEntry).after;
            }
        }
    }
    
    private class DescendingEntries 
    extends AbstractBaseEntriesImpl<K, V> 
    implements OrderedBaseEntries<K, V> {

        @Override
        protected AbstractBaseEntriesImpl<K, V> getParent() {
            return LinkedHashEntries.this;
        }
        
        @Override
        public boolean isReadWriteLockSupported() {
            return LinkedHashEntries.this.isReadWriteLockSupported();
        }
        
        @Override
        public boolean headAppend() {
            return !LinkedHashEntries.this.headAppend();
        }

        @Override
        public OrderAdjustMode replaceMode() {
            return LinkedHashEntries.this.replaceMode().descendingMode();
        }

        @Override
        public OrderAdjustMode accessMode() {
            return LinkedHashEntries.this.accessMode().descendingMode();
        }

        @Override
        public OrderedBaseEntries<K, V> descendingEntries() {
            return LinkedHashEntries.this;
        }

        @Override
        public BaseEntryIterator<K, V> iterator() {
            return LinkedHashEntries.this.new DescendingEntryIterator();
        }

        @Override
        public boolean isEmpty() {
            return LinkedHashEntries.this.isEmpty();
        }

        @Override
        public int size() {
            return LinkedHashEntries.this.size();
        }

        @Override
        protected void deleteBaseEntry(BaseEntry<K, V> be) {
            LinkedHashEntries.this.deleteBaseEntry(be);
        }

        @Override
        public BaseEntry<K, V> access(K key, BaseEntriesHandler<K, V> handler) {
            return LinkedHashEntries.this.access(key, handler);
        }

        @Override
        public void clear(Trigger<K, V> trigger) {
            LinkedHashEntries.this.clear(trigger);
        }
        
        @Override
        public BaseEntry<K, V> getBaseEntry(Object key) {
            return LinkedHashEntries.this.getBaseEntry(key);
        }

        @Override
        protected V putWithoutTriggerFlushing(
                K key, 
                V value,
                AttachProcessor<K, V> attachProcessor) {
            return LinkedHashEntries.this.putWithoutTriggerFlushing(key, value, attachProcessor);
        }

        @Override
        protected void putAll(
                Map<? extends K, ? extends V> m,
                Trigger<K, V> trigger) {
            LinkedHashEntries.this.putAll(m, trigger);
        }
        
        @Override
        protected boolean addAll(
                Collection<? extends K> kc, 
                Trigger<K, V> trigger) {
            return LinkedHashEntries.this.addAll(kc, trigger);
        }

        @Override
        public BaseEntry<K, V> first() {
            return LinkedHashEntries.this.last();
        }

        @Override
        public BaseEntry<K, V> last() {
            return LinkedHashEntries.this.first();
        }

        @Override
        public BaseEntry<K, V> pollFirst(BaseEntriesHandler<K, V> handler) {
            return LinkedHashEntries.this.pollLast(handler);
        }

        @Override
        public BaseEntry<K, V> pollLast(BaseEntriesHandler<K, V> handler) {
            return LinkedHashEntries.this.pollFirst(handler);
        }
        
        @Override
        public int modCount() {
            return LinkedHashEntries.this.modCount;
        }
        
    }
    
    private abstract class AbstractEntryIterator implements BaseEntryIterator<K, V> {
        
        private final BaseEntryImpl<K, V> invalid;
        
        private int expectedModCount;
        
        private BaseEntryImpl<K, V> willReturn;
        
        private BaseEntryImpl<K, V> lastReturned;
        
        protected AbstractEntryIterator(BaseEntryImpl<K, V> first) {
            this.invalid = LinkedHashEntries.this.invalid;
            this.willReturn = first;
            this.expectedModCount = LinkedHashEntries.this.modCount;
        }
        
        protected Trigger<K, V> trigger(BaseEntriesHandler<K, V> handler) {
            return LinkedHashEntries.this.triggerOf(handler);
        }

        @Override
        public boolean hasNext() {
            return this.willReturn != this.invalid;
        }
        
        protected BaseEntryImpl<K, V> prevEntry() {
            final LinkedHashEntries<K, V> owner = LinkedHashEntries.this;
            if (this.expectedModCount != owner.modCount) {
                throw new ConcurrentModificationException(IteratorMessages.concurrentModifcation());
            }
            if (this.willReturn == this.invalid) {
                throw new NoSuchElementException(IteratorMessages.noSuchElement());
            }
            BaseEntryImpl<K, V> e = this.willReturn;
            this.willReturn = e.before;
            return this.lastReturned = e;
        }

        protected BaseEntryImpl<K, V> nextEntry() {
            final LinkedHashEntries<K, V> owner = LinkedHashEntries.this;
            if (this.expectedModCount != owner.modCount) {
                throw new ConcurrentModificationException(IteratorMessages.concurrentModifcation());
            }
            if (this.willReturn == this.invalid) {
                throw new NoSuchElementException(IteratorMessages.noSuchElement());
            }
            BaseEntryImpl<K, V> e = this.willReturn;
            this.willReturn = e.after;
            return this.lastReturned = e;
        }

        @Override
        public void remove(BaseEntriesHandler<K, V> handler) {
            final LinkedHashEntries<K, V> owner = LinkedHashEntries.this;
            if (this.expectedModCount != owner.modCount) {
                throw new ConcurrentModificationException(IteratorMessages.concurrentModifcation());
            }
            final BaseEntryImpl<K, V> e = this.lastReturned;
            if (e == this.invalid) {
                throw new IllegalStateException(IteratorMessages.removeNoExtractedElement());
            }
            Trigger<K, V> trigger = this.trigger(handler);
            if (trigger != null) {
                trigger.preRemove(e);
            }
            if (trigger == null || trigger.beginExecute()) {
                try {
                    owner.deleteBaseEntryImpl(e);
                    owner.scale(false);
                    this.lastReturned = null;
                    this.expectedModCount = owner.modCount;
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
    
    private class AscendingEntryIterator extends AbstractEntryIterator {

        protected AscendingEntryIterator() {
            LinkedHashEntries.this.super(LinkedHashEntries.this.invalid.after);
        }

        @Override
        public BaseEntryImpl<K, V> next() {
            return this.nextEntry();
        }
        
    }
    
    private class DescendingEntryIterator extends AbstractEntryIterator {

        protected DescendingEntryIterator() {
            LinkedHashEntries.this.super(LinkedHashEntries.this.invalid.before);
        }

        @Override
        public BaseEntryImpl<K, V> next() {
            return this.prevEntry();
        }
        
    }
    
    public static class TransientValue<K, V> extends LinkedHashEntries<K, V> implements TransientValueEntries {

        private static final long serialVersionUID = -4075720144429138844L;

        public TransientValue(
                ReplacementRule keyReplacementRule,
                EqualityComparator<? super K> keyEqualityComparator,
                Object valueComparatorOrEqualityComparatorOrUnifiedComparator,
                int initCapacity, 
                Float loadFactor, 
                boolean headAppend, 
                OrderAdjustMode accessMode,
                OrderAdjustMode replaceMode) {
            super(
                    BidiType.NONE,
                    keyReplacementRule, 
                    keyEqualityComparator,
                    valueComparatorOrEqualityComparatorOrUnifiedComparator, 
                    initCapacity,
                    loadFactor, 
                    headAppend, 
                    accessMode,
                    replaceMode);
        }
        
    }
}
