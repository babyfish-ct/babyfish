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
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.FrozenComparator;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XMap;
import org.babyfish.collection.spi.base.AbstractBaseEntriesImpl.Trigger.History;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
public class RedBlackTreeEntries<K, V> 
extends AbstractRootBaseEntriesImpl<K, V> 
implements NavigableBaseEntries<K, V> {

    private static final long serialVersionUID = -6731784374752661716L;
    
    private BaseEntryImpl<K, V> root;

    private int modCount;

    public RedBlackTreeEntries(
            BidiType bidiType,
            ReplacementRule keyReplacementRule,
            Comparator<? super K> comparator, 
            Object valueComparatorOrEqualityComparatorOrUnifiedComparator) {
        super(
                bidiType,
                keyReplacementRule, 
                comparator, 
                valueComparatorOrEqualityComparatorOrUnifiedComparator);
    }
    
    @Override
    public boolean isReadWriteLockSupported() {
        return true;
    }
    
    @Override
    protected void deleteBaseEntry(BaseEntry<K, V> be) {
        this.deleteBaseEntry0((BaseEntryImpl<K, V>)be);
    }
    
    @Override
    public final NavigableBaseEntries<K, V> descendingEntries() {
        return this.new SubTree(NavigableRange.descendingRange(this));
    }

    @Override
    public final NavigableBaseEntries<K, V> subEntries(
            boolean hasFrom, K from, boolean fromInclusive, 
            boolean hasTo, K to, boolean toInclusive) {
        return this.new SubTree(
                NavigableRange.subRange(
                        this, 
                        hasFrom, from, fromInclusive, 
                        hasTo, to, toInclusive
                )
        );
    }

    @Override
    public final int size() {
        return this.root == null ? 0 : this.root.size;
    }
    
    @Override
    public final boolean isEmpty() {
        return this.root == null;
    }

    @Override
    public final Comparator<? super K> comparator() {
        return this.keyUnifiedComparator().comparator();
    }

    @Override
    public NavigableRange<K> range() {
        return null;
    }

    @Override
    public BaseEntryIterator<K, V> iterator() {
        return this.new AscendingEntryIterator(this.first(), null);
    }
    
    @SuppressWarnings("unchecked")
    public final BaseEntryImpl<K, V> getBaseEntry(Object key) {
        Comparator<? super K> comparator = this.keyUnifiedComparator().comparator();
        BaseEntryImpl<K, V> p = this.root;
        if (comparator != null) {
            while (p != null) {
                int cmp = comparator.compare((K) key, p.key);
                if (cmp < 0) {
                    p = p.left;
                } else if (cmp > 0) {
                    p = p.right;
                } else {
                    return p;
                }
            }
        } else {
            Arguments.mustBeInstanceOfValue(
                    "key", 
                    Arguments.mustNotBeNull("key", key), //be compatible with JDK, throw NullPointerException 
                    Comparable.class);
            Comparable<? super K> comparable = (Comparable<? super K>) key;
            while (p != null) {
                int cmp = comparable.compareTo(p.key);
                if (cmp < 0) {
                    p = p.left;
                } else if (cmp > 0) {
                    p = p.right;
                } else {
                    return p;
                }
            }
        }
        return null;
    }
    
    @Override
    public final BaseEntry<K, V> pollFirst(BaseEntriesHandler<K, V> handler) {
        return this.pollFirst(this.triggerOf(handler));
    }
    
    @Override
    public final BaseEntry<K, V> pollLast(BaseEntriesHandler<K, V> handler) {
        return this.pollLast(this.triggerOf(handler));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final V putWithoutTriggerFlushing(
            K key, 
            V value, 
            AttachProcessor<K, V> attachProcessor) {
        
        boolean isKeyStrict = this.keyReplacementRule() == ReplacementRule.NEW_REFERENCE_WIN;
        if (attachProcessor.beginExcute()) {
            try {
                BaseEntryImpl<K, V> be = this.root;
                if (be == null) {
                    this.root = new BaseEntryImpl<K, V>(this, null, key, value);
                    BaseEntries<V, K> inversedEntries = this.inversedEntries();
                    if (inversedEntries != null) {
                        inversedEntries.put(value, key, null);
                    }
                    this.modCount++;
                    attachProcessor.endExecute(null);
                    return null;
                }
                BaseEntryImpl<K, V> parent = null;
                int cmp = 0;
                Comparator<? super K> comparator = this.keyUnifiedComparator().comparator();
                if (comparator != null) {
                    do {
                        parent = be;
                        cmp = comparator.compare(key, be.key);
                        if (cmp < 0) {
                            be = be.left;
                        } else if (cmp > 0) {
                            be = be.right;
                        } else {
                            if (isKeyStrict) {
                                be.setRawKey(key, this.keyComparatorOrEqualityComparator());
                            }
                            V oldValue = be.value;
                            be.value = value;
                            BaseEntries<V, K> inversedEntries = this.inversedEntries();
                            if (inversedEntries != null) {
                                inversedEntries.removeByKey(oldValue, null);
                                inversedEntries.put(value, be.key, null);
                            }
                            attachProcessor.endExecute(null);
                            return oldValue;
                        }
                    } while (be != null);
                } else {
                    Arguments.mustBeInstanceOfValue(
                            "key", 
                            Arguments.mustNotBeNull("key", key), //be compatible with JDK, throw NullPointerException 
                            Comparable.class);
                    Comparable<? super K> comparable = (Comparable<? super K>) key;
                    do {
                        parent = be;
                        cmp = comparable.compareTo(be.key);
                        if (cmp < 0) {
                            be = be.left;
                        } else if (cmp > 0) {
                            be = be.right;
                        } else {
                            if (isKeyStrict) {
                                be.setRawKey(key, this.keyComparatorOrEqualityComparator());
                            }
                            V oldValue = be.value;
                            be.value = value;
                            BaseEntries<V, K> inversedEntries = this.inversedEntries();
                            if (inversedEntries != null) {
                                inversedEntries.removeByKey(oldValue, null);
                                inversedEntries.put(value, key, null);
                            }
                            attachProcessor.endExecute(null);
                            return oldValue;
                        }
                    } while (be != null);
                }
                BaseEntryImpl<K, V> newEntry = new BaseEntryImpl<K, V>(this, parent, key, value);
                if (cmp < 0) {
                    parent.left = newEntry;
                } else {
                    parent.right = newEntry;
                }
                while (parent != null) {
                    parent.size++;
                    parent = parent.parent;
                }
                this.fixAfterInsertion(newEntry);
                BaseEntries<V, K> inversedEntries = this.inversedEntries();
                if (inversedEntries != null) {
                    inversedEntries.put(value, key, null);
                }
                this.modCount++;
                attachProcessor.endExecute(null);
                return null;
            } catch (RuntimeException | Error ex) {
                attachProcessor.endExecute(ex);
            }
        }
        
        return null;
    }

    @Override
    protected void putAllWithoutTriggerFlushing(
            Map<? extends K, ? extends V> m,
            AttachProcessor<K, V> attachProcessor) {
        
        History<K, V> puttingHistory = attachProcessor.getPuttingHistory();
        int mapSize = m.size();
        boolean isKeyStrict = this.keyReplacementRule() == ReplacementRule.NEW_REFERENCE_WIN;

        if (mapSize == 1) {
            Entry<? extends K, ? extends V> entry = m.entrySet().iterator().next();
            this.putWithoutTriggerFlushing(entry.getKey(), entry.getValue(), attachProcessor);
            return;
        }

        if (this.root == null && 
                m instanceof SortedMap<?, ?> &&
                Objects.equals(
                        this.keyComparatorOrEqualityComparator(), 
                        ((SortedMap<?, ?>)m).comparator()
                ) &&
                (
                        this.bidiType() == null ||
                        (
                                m instanceof XMap<?, ?> && 
                                this.bidiType() == 
                                ((XMap<?, ?>)m).bidiType()
                        ) 
                )
        ) {
            /*
             * Increase modCount at first to wreck the iterator if the
             * parameter is this object or wrapper of this object
             */
            this.modCount++;
            if (attachProcessor.beginExcute()) {
                try {
                    Iterator<? extends Entry<? extends K, ? extends V>> iterator = 
                        m.entrySet().iterator();
                    try {
                        this.buildFromSorted(mapSize, iterator, null, null);
                    } catch (IOException impossible) {
                    } catch (ClassNotFoundException impossible) {
                    }
                    attachProcessor.endExecute(null);
                } catch (RuntimeException | Error ex) {
                    attachProcessor.endExecute(ex);
                }
            }
            return;
        }

        if (puttingHistory == null) {
            if (attachProcessor.beginExcute()) {
                try {
                    for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
                        this.put(
                                entry.getKey(), 
                                entry.getValue(), 
                                null,
                                null
                        );
                    }
                    attachProcessor.endExecute(null);
                } catch (RuntimeException | Error ex) {
                    attachProcessor.endExecute(ex);
                }
                return;
            }
        } else {
            if (attachProcessor.beginExcute()) {
                int count = puttingHistory.getCount();
                Object keyComparator = this.keyComparatorOrEqualityComparator();
                try {
                    for(int i = 0; i < count; i++) {
                        BaseEntryImpl<K, V> be = (BaseEntryImpl<K, V>)puttingHistory.getBaseEntry(i);
                        if (be == null) {
                            this.put(
                                    puttingHistory.getNewKey(i), 
                                    puttingHistory.getNewValue(i), 
                                    null,
                                    null);
                        } else {
                            if (isKeyStrict) {
                                be.setRawKey(puttingHistory.getNewKey(i), keyComparator);
                            }
                            V value = puttingHistory.getNewValue(i);
                            V oldValue = be.value;
                            be.value = value;
                            BaseEntries<V, K> inversedEntries = this.inversedEntries();
                            if (inversedEntries != null) {
                                inversedEntries.removeByKey(oldValue, null);
                                inversedEntries.put(value, be.key, null);
                            };
                        }
                    }
                    attachProcessor.endExecute(null);
                } catch (RuntimeException | Error ex) {
                    attachProcessor.endExecute(ex);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected boolean addAllWithoutTriggerFlushing(
            Collection<? extends K> c, 
            Trigger<K, V> trigger) {
        
        // In this method, inversedEntries always is null
        
        int cSize = c.size();
        boolean isKeyStrict = this.keyReplacementRule() == ReplacementRule.NEW_REFERENCE_WIN;
        
        if (cSize == 1) {
            BaseEntryImpl<K, V> oldRoot = this.root;
            K k = c.iterator().next();
            int oldSize = oldRoot == null ? 0 : oldRoot.size;
            this.put(k, (V)PRESENT, trigger, null);
            return oldSize != root.size;
        }

        if (this.root == null && 
                c instanceof SortedSet<?> &&
                Objects.equals(
                        this.keyComparatorOrEqualityComparator(), 
                        ((SortedSet<?>)c).comparator()
                )
        ) {
            /*
             * Increase modCount at first to wreck the iterator if the
             * parameter is this object or wrapper of this object
             */
            this.modCount++;
            if (trigger != null) {
                Iterator<?> iterator = c.iterator();
                for (int i = 0; i < cSize; i++) {
                    K k = (K)iterator.next();
                    trigger.preAdd(k, (V)PRESENT);
                }
            }
            if (trigger == null) {
                Iterator<?> iterator = c.iterator();
                try {
                    this.buildFromSorted(cSize, iterator, null, (V)PRESENT);
                } catch (IOException impossible) {
                } catch (ClassNotFoundException impossible) {
                }
            } else if (trigger.beginExecute()) {
                try {
                    Iterator<?> iterator = c.iterator();
                    try {
                        this.buildFromSorted(cSize, iterator, null, (V)PRESENT);
                    } catch (IOException impossible) {
                    } catch (ClassNotFoundException impossible) {
                    }
                    trigger.endExecute(null);
                } catch (RuntimeException | Error ex) {
                    trigger.endExecute(ex);
                }
            }
            if (trigger != null) {
                trigger.flush();
            }
            return cSize != 0;
        }

        if (trigger == null) {
            int oldSize = this.size();
            for (K k : c) {
                this.put(k, (V)PRESENT, (Trigger<K, V>)null, null);
            }
            return oldSize != this.size();
        }

        Iterator<?> iterator = c.iterator();
        for (int i = 0; i < cSize; i++) {
            K k = (K)iterator.next();
            BaseEntryImpl<K, V> be = this.getBaseEntry(k);
            if (be == null) {
                trigger.preAdd(k, (V)PRESENT);
            } else {
                trigger.preChange(be, isKeyStrict ? k : be.key, (V)PRESENT);
            }
        }
        
        int oldSize = this.size();
        if (trigger.beginExecute()) {
            try {
                History<K, V> history = trigger.getHistory(0);
                Object keyComparator = this.keyComparatorOrEqualityComparator();
                for (int i = 0; i < cSize; i++) {
                    K k = history.getNewKey(i);
                    BaseEntryImpl<K, V> be = (BaseEntryImpl<K, V>)history.getBaseEntry(i);
                    if (be == null) {
                        this.put(k, (V)PRESENT, (Trigger<K, V>)null, null);
                    } else {
                        if (isKeyStrict) {
                            be.setRawKey(k, keyComparator);
                        }
                        be.value = (V)PRESENT;
                    }
                }
                trigger.endExecute(null);
            } catch (RuntimeException | Error ex) {
                trigger.endExecute(ex);
            }
        }
        trigger.flush();
        return oldSize != this.size();
    }

    @Override
    protected void clear(Trigger<K, V> trigger) {
        if (trigger != null) {
            BaseEntryIterator<K, V> iterator = this.iterator();
            while (iterator.hasNext()) {
                trigger.preRemove(iterator.next());
            }
        }
    
        if (trigger == null) {
            BaseEntryIterator<K, V> iterator = this.iterator();
            while (iterator.hasNext()) {
                BaseEntryImpl<K, V> be = (BaseEntryImpl<K, V>)iterator.next();
                be.recordRemove();
            }
            BaseEntries<V, K> inversedEntries = this.inversedEntries();
            if (inversedEntries != null) {
                inversedEntries.clear(null);
            }
            this.root = null;
            this.modCount++;
        } else if (trigger.beginExecute()) {
            try {
                BaseEntryIterator<K, V> iterator = this.iterator();
                while (iterator.hasNext()) {
                    BaseEntryImpl<K, V> be = (BaseEntryImpl<K, V>)iterator.next();
                    be.recordRemove();
                }
                BaseEntries<V, K> inversedEntries = this.inversedEntries();
                if (inversedEntries != null) {
                    inversedEntries.clear(null);
                }
                this.root = null;
                this.modCount++;
                trigger.endExecute(null);
            } catch (RuntimeException | Error ex) {
                trigger.endExecute(ex);
            }
        }
        if (trigger != null) {
            trigger.flush();
        }
    }

    protected BaseEntry<K, V> pollFirst(Trigger<K, V> trigger) {
        BaseEntryImpl<K, V> be = this.first();
        if (be != null) {
            if (trigger != null) {
                trigger.preRemove(be);
                if (trigger.beginExecute()) {
                    try {
                        this.deleteBaseEntry0(be);
                        trigger.endExecute(null);
                    } catch (RuntimeException | Error ex) {
                        trigger.endExecute(ex);
                    }
                }
                trigger.flush();
            } else {
                this.deleteBaseEntry(be);
            }
        }
        return be;
    }
    
    protected BaseEntry<K, V> pollLast(Trigger<K, V> trigger) {
        BaseEntryImpl<K, V> be = this.last();
        if (be != null) {
            if (trigger != null) {
                trigger.preRemove(be);
                if (trigger.beginExecute()) {
                    try {
                        this.deleteBaseEntry0(be);
                        trigger.endExecute(null);
                    } catch (RuntimeException | Error ex) {
                        trigger.endExecute(ex);
                    }
                }
                trigger.flush();
            } else {
                this.deleteBaseEntry(be);
            }
        }
        return be;
    }

    /*
     * If the return is not null, EntryIterator use it to change field "next";
     */
    final BaseEntryImpl<K, V> deleteBaseEntry0(final BaseEntryImpl<K, V> p) {
        
        this.modCount++;
        BaseEntryImpl<K, V> s = null;

        if (p.left != null && p.right != null) {
            
            s = successor(p);

            /*
             * In JDK's implementation, here has only 3 statements expect the
             * first statment: 
             *    p.key = s.key; 
             *    p.value = s.value; 
             *    p = s;
             * 
             * Although this is really a simple and fast way. but it is not
             * befitting for my implementation, because
             * 
             * 1) This solution will cause any instance of Entry<K, V> contains
             * different data in different time, so it's hard to indicate
             * whether a Entry<K, V> is belonged to a red-black tree. 2) When
             * the program calls the method "deleteEntry" with a parameter of
             * that left field and right field are both not null, JDK's
             * implementation will change the data of another Entry<K, V>(
             * "p = s;" make p can not be delete, the target to be deleted
             * redirects to s, p can stay in the tree after deleting,
             * "p.key = s.key;" and "p.value = s.value;" change the data of the
             * the Entry<K, V> p that is not to be delete actually). That means
             * other Entry<K, V> may be affected when program deletes an
             * Entry<K, V>. but for me, For example, in method "removeAll" of my
             * solution, I want to storage many EntryK<K, V> objects before
             * deleting them, then I try to fire the events one by one, after
             * that, I continue to call method "deleteBaseEntry" by those
             * Entry<K, V> objects again and again. the deleting on any object
             * of Entry<K, V> should not affect the next objects.
             * 
             * So, here I don't want to swap key and value of two entries, I
             * want to swap their references in the whole tree, but key and
             * value will not changed in this code section. This code section do
             * three things
             * 
             * 1) Swap the color of them 2) If root is same to p, redirect root
             * to s 3) Swap references
             * 
             * because here p.right is not absolutely, so I can make sure 1) The
             * depth of s is greater than the depth of p, so there has only one
             * case the make both p and s are neighbor of each other: p.right ==
             * s 2) s.left is null absolutely
             */
            /*
             * Swap color
             */
            boolean pred = p.red;
            p.red = s.red;
            s.red = pred;
            /*
             * Swap size
             */
            int psize = p.size;
            p.size = s.size;
            s.size = psize;
            /*
             * If root is same to p, redirect root to s
             */
            if (this.root == p) {
                this.root = s;
            }
            /*
             * Swap references.
             */
            BaseEntryImpl<K, V> pp = p.parent;
            BaseEntryImpl<K, V> pl = p.left;
            BaseEntryImpl<K, V> pr = p.right;
            BaseEntryImpl<K, V> sp = s.parent;
            assert s.left == null;
            BaseEntryImpl<K, V> sr = s.right;
            if (pp != null) {
                if (pp.left == p) {
                    pp.left = s;
                } else {
                    pp.right = s;
                }
            }
            if (pl != null) {
                pl.parent = s;
            }
            if (pr != null) {
                pr.parent = pr == s ? pp : s;
            }
            if (sp != null) {
                if (sp.left == s) {
                    sp.left = p;
                } else {
                    sp.right = pr == s ? sr : p;
                }
            }
            if (sr != null) {
                sr.parent = p;
            }
            p.parent = pr == s ? s : sp;
            p.left = null;
            p.right = sr;
            s.parent = pp;
            s.left = pl;
            s.right = pr == s ? p : pr;
        }

        BaseEntryImpl<K, V> replacement = p.left != null ? p.left : p.right;
        if (replacement != null) {
            replacement.parent = p.parent;
            if (p.parent == null) {
                this.root = replacement;
            } else if (p == p.parent.left) {
                p.parent.left = replacement;
            } else {
                p.parent.right = replacement;
            }
            p.left = null;
            p.right = null;
            p.parent = null;
            p.recordRemove();
            for (BaseEntryImpl<K, V> rp = replacement.parent; rp != null; rp = rp.parent) {
                rp.size--;
            }
            if (!isRed(p)) {
                this.fixAfterDeletion(replacement);
            }
            BaseEntries<V, K> inversedEntries = this.inversedEntries();
            if (inversedEntries != null) {
                inversedEntries.removeByKey(p.value, null);
            }
        } else if (p.parent == null) {
            this.root = null;
            p.recordRemove();
            BaseEntries<V, K> inversedEntries = this.inversedEntries();
            if (inversedEntries != null) {
                inversedEntries.removeByKey(p.value, null);
            }
        } else {
            if (!isRed(p)) {
                this.fixAfterDeletion(p);
            }
            if (p.parent != null) {
                if (p == p.parent.left) {
                    p.parent.left = null;
                } else if (p == p.parent.right) {
                    p.parent.right = null;
                }
                for (BaseEntryImpl<K, V> pp = p.parent; pp != null; pp = pp.parent) {
                    pp.size--;
                }
                p.parent = null;
            }
            p.recordRemove();
            BaseEntries<V, K> inversedEntries = this.inversedEntries();
            if (inversedEntries != null) {
                inversedEntries.removeByKey(p.value, null);
            }
        }

        return s;
    }

    @Override
    public BaseEntryImpl<K, V> first() {
        BaseEntryImpl<K, V> p = this.root;
        if (p != null) {
            while (p.left != null) {
                p = p.left;
            }
        }
        return p;
    }

    @Override
    public BaseEntryImpl<K, V> last() {
        BaseEntryImpl<K, V> p = this.root;
        if (p != null) {
            while (p.right != null) {
                p = p.right;
            }
        }
        return p;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final BaseEntryImpl<K, V> floor(K key) {
        BaseEntryImpl<K, V> p = this.root;
        Comparator<? super K> comparator = this.keyUnifiedComparator().comparator();
        while (p != null) {
            int cmp = 
                comparator != null ? 
                        comparator.compare(key, p.key) :
                        ((Comparable<? super K>) key).compareTo(p.key);
            if (cmp > 0) {
                if (p.right != null)
                    p = p.right;
                else
                    return p;
            } else if (cmp < 0) {
                if (p.left != null) {
                    p = p.left;
                } else {
                    BaseEntryImpl<K, V> parent = p.parent;
                    BaseEntryImpl<K, V> ch = p;
                    while (parent != null && ch == parent.left) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            } else
                return p;

        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final BaseEntryImpl<K, V> ceiling(K key) {
        BaseEntryImpl<K, V> p = this.root;
        Comparator<? super K> comparator = this.keyUnifiedComparator().comparator();
        while (p != null) {
            int cmp = 
                comparator != null ? 
                        comparator.compare(key, p.key) :
                        ((Comparable<? super K>) key).compareTo(p.key);
            if (cmp < 0) {
                if (p.left != null)
                    p = p.left;
                else
                    return p;
            } else if (cmp > 0) {
                if (p.right != null) {
                    p = p.right;
                } else {
                    BaseEntryImpl<K, V> parent = p.parent;
                    BaseEntryImpl<K, V> ch = p;
                    while (parent != null && ch == parent.right) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            } else
                return p;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final BaseEntryImpl<K, V> lower(K key) {
        BaseEntryImpl<K, V> p = this.root;
        Comparator<? super K> comparator = this.keyUnifiedComparator().comparator();
        while (p != null) {
            int cmp = 
                comparator != null ? 
                        comparator.compare(key, p.key) :
                        ((Comparable<? super K>) key).compareTo(p.key);
            if (cmp > 0) {
                if (p.right != null)
                    p = p.right;
                else
                    return p;
            } else {
                if (p.left != null) {
                    p = p.left;
                } else {
                    BaseEntryImpl<K, V> parent = p.parent;
                    BaseEntryImpl<K, V> ch = p;
                    while (parent != null && ch == parent.left) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public final BaseEntryImpl<K, V> higher(K key) {
        BaseEntryImpl<K, V> p = this.root;
        Comparator<? super K> comparator = this.keyUnifiedComparator().comparator();
        while (p != null) {
            int cmp = 
                comparator != null ? 
                        comparator.compare(key, p.key) :
                        ((Comparable<? super K>) key).compareTo(p.key);
            if (cmp < 0) {
                if (p.left != null)
                    p = p.left;
                else
                    return p;
            } else {
                if (p.right != null) {
                    p = p.right;
                } else {
                    BaseEntryImpl<K, V> parent = p.parent;
                    BaseEntryImpl<K, V> ch = p;
                    while (parent != null && ch == parent.right) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            }
        }
        return null;
    }
    
    @Override
    public int modCount() {
        return this.modCount;
    }

    private void fixAfterInsertion(BaseEntryImpl<K, V> x) {

        x.red = true;

        while (x != null && x != this.root && x.parent.red) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                BaseEntryImpl<K, V> y = rightOf(parentOf(parentOf(x)));
                if (isRed(y)) {
                    setRed(parentOf(x), false);
                    setRed(y, false);
                    setRed(parentOf(parentOf(x)), true);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setRed(parentOf(x), false);
                    setRed(parentOf(parentOf(x)), true);
                    rotateRight(parentOf(parentOf(x)));
                }
            } else {
                BaseEntryImpl<K, V> y = leftOf(parentOf(parentOf(x)));
                if (isRed(y)) {
                    setRed(parentOf(x), false);
                    setRed(y, false);
                    setRed(parentOf(parentOf(x)), true);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setRed(parentOf(x), false);
                    setRed(parentOf(parentOf(x)), true);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }

        this.root.red = false;
    }

    private void fixAfterDeletion(BaseEntryImpl<K, V> x) {
        while (x != this.root && !isRed(x)) {
            if (x == leftOf(parentOf(x))) {
                BaseEntryImpl<K, V> sib = rightOf(parentOf(x));

                if (isRed(sib)) {
                    setRed(sib, false);
                    setRed(parentOf(x), true);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }

                if (!isRed(leftOf(sib)) && !isRed(rightOf(sib))) {
                    setRed(sib, true);
                    x = parentOf(x);
                } else {
                    if (!isRed(rightOf(sib))) {
                        setRed(leftOf(sib), false);
                        setRed(sib, true);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setRed(sib, isRed(parentOf(x)));
                    setRed(parentOf(x), false);
                    setRed(rightOf(sib), false);
                    rotateLeft(parentOf(x));
                    x = this.root;
                }
            } else {
                BaseEntryImpl<K, V> sib = leftOf(parentOf(x));
                if (isRed(sib)) {
                    setRed(sib, false);
                    setRed(parentOf(x), true);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (!isRed(rightOf(sib)) && !isRed(leftOf(sib))) {
                    setRed(sib, true);
                    x = parentOf(x);
                } else {
                    if (!isRed(leftOf(sib))) {
                        setRed(rightOf(sib), false);
                        setRed(sib, true);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setRed(sib, isRed(parentOf(x)));
                    setRed(parentOf(x), false);
                    setRed(leftOf(sib), false);
                    rotateRight(parentOf(x));
                    x = this.root;
                }
            }
        }

        setRed(x, false);
    }

    private void rotateLeft(BaseEntryImpl<K, V> p) {
        if (p != null) {
            BaseEntryImpl<K, V> r = p.right;
            p.right = r.left;
            if (r.left != null) {
                r.left.parent = p;
            }
            r.parent = p.parent;
            if (p.parent == null) {
                this.root = r;
            } else if (p.parent.left == p) {
                p.parent.left = r;
            } else {
                p.parent.right = r;
            }
            r.left = p;
            p.parent = r;
            p.size = 1 + sizeOf(p.left) + sizeOf(p.right);
            r.size = 1 + sizeOf(r.left) + sizeOf(r.right);
        }
    }

    private void rotateRight(BaseEntryImpl<K, V> p) {
        if (p != null) {
            BaseEntryImpl<K, V> l = p.left;
            p.left = l.right;
            if (l.right != null) {
                l.right.parent = p;
            }
            l.parent = p.parent;
            if (p.parent == null) {
                this.root = l;
            } else if (p.parent.right == p) {
                p.parent.right = l;
            } else {
                p.parent.left = l;
            }
            l.right = p;
            p.parent = l;
            p.size = 1 + sizeOf(p.left) + sizeOf(p.right);
            l.size = 1 + sizeOf(l.left) + sizeOf(l.right);
        }
    }

    private void buildFromSorted(
            int size, 
            Iterator<?> iterator,
            ObjectInputStream stream, 
            V defaultVal) throws IOException,
            ClassNotFoundException {
        this.root = this.buildFromSorted(
                this.inversedEntries(),
                0, 
                0, 
                size - 1, 
                computeRedLevel(size),
                iterator, 
                stream, 
                defaultVal);
    }

    @SuppressWarnings("unchecked")
    private BaseEntryImpl<K, V> buildFromSorted(
            BaseEntries<V, K> inversedEntries, //Unnecessary argument for optimization
            int level, 
            int lo, 
            int hi,
            int redLevel, 
            Iterator<?> iterator, 
            ObjectInputStream stream,
            V defaultVal) throws IOException, ClassNotFoundException {
        if (hi < lo) {
            return null;
        }
        int mid = (lo + hi) / 2;
        BaseEntryImpl<K, V> left = null;
        if (lo < mid) {
            left = this.buildFromSorted(
                    inversedEntries,
                    level + 1, 
                    lo, 
                    mid - 1, 
                    redLevel,
                    iterator, 
                    stream, 
                    defaultVal);
        }

        K key;
        V value;
        if (iterator != null) {
            if (defaultVal == null) {
                Entry<? extends K, ? extends V> entry = 
                    (Entry<? extends K, ? extends V>) iterator.next();
                key = entry.getKey();
                value = entry.getValue();
            } else {
                key = (K) iterator.next();
                value = defaultVal;
            }
        } else {
            key = (K) stream.readObject();
            value = (defaultVal != null ? defaultVal : (V)stream.readObject());
        }
        BaseEntryImpl<K, V> middle = new BaseEntryImpl<K, V>(this, null, key, value);
        if (inversedEntries != null) {
            inversedEntries.put(value, key, null);
        }

        if (level == redLevel) {
            middle.red = true;
        }

        if (left != null) {
            middle.left = left;
            left.parent = middle;
            middle.size += left.size;
        }

        if (mid < hi) {
            BaseEntryImpl<K, V> right = this.buildFromSorted(
                    inversedEntries,
                    level + 1, 
                    mid + 1,
                    hi, 
                    redLevel, 
                    iterator, 
                    stream, 
                    defaultVal);
            middle.right = right;
            right.parent = middle;
            middle.size += right.size;
        }

        return middle;
    }

    private static int computeRedLevel(int sz) {
        int level = 0;
        for (int m = sz - 1; m >= 0; m = m / 2 - 1) {
            level++;
        }
        return level;
    }

    private static <K, V> boolean isRed(BaseEntryImpl<K, V> p) {
        return (p == null ? false : p.red);
    }

    private static <K, V> void setRed(BaseEntryImpl<K, V> p, boolean red) {
        if (p != null) {
            p.red = red;
        }
    }

    private static <K, V> BaseEntryImpl<K, V> parentOf(BaseEntryImpl<K, V> p) {
        return (p == null ? null : p.parent);
    }

    private static <K, V> BaseEntryImpl<K, V> leftOf(BaseEntryImpl<K, V> p) {
        return (p == null) ? null : p.left;
    }

    private static <K, V> BaseEntryImpl<K, V> rightOf(BaseEntryImpl<K, V> p) {
        return (p == null) ? null : p.right;
    }
    
    private static <K, V> int sizeOf(BaseEntryImpl<K, V> p) {
        return (p == null) ? 0 : p.size;
    }

    private static <K, V> BaseEntryImpl<K, V> successor(BaseEntryImpl<K, V> t) {
        if (t == null) {
            return null;
        } else if (t.right != null) {
            BaseEntryImpl<K, V> p = t.right;
            while (p.left != null) {
                p = p.left;
            }
            return p;
        } else {
            BaseEntryImpl<K, V> p = t.parent;
            BaseEntryImpl<K, V> ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    private static <K, V> BaseEntryImpl<K, V> predecessor(BaseEntryImpl<K, V> t) {
        if (t == null) {
            return null;
        } else if (t.left != null) {
            BaseEntryImpl<K, V> p = t.left;
            while (p.right != null)
                p = p.right;
            return p;
        } else {
            BaseEntryImpl<K, V> p = t.parent;
            BaseEntryImpl<K, V> ch = t;
            while (p != null && ch == p.left) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.size());
        BaseEntryIterator<K, V> beItr = this.iterator();
        if (this instanceof TransientValueEntries) {
            while (beItr.hasNext()) {
                BaseEntryImpl<K, V> be = (BaseEntryImpl<K, V>)beItr.next();
                out.writeObject(be.key);
            }
        } else {
            while (beItr.hasNext()) {
                BaseEntryImpl<K, V> be = (BaseEntryImpl<K, V>)beItr.next();
                out.writeObject(be.key);
                out.writeObject(be.value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        this.buildFromSorted(
                size, 
                null, 
                in, 
                this instanceof TransientValueEntries ? (V)PRESENT : null);
    }
    
    private class SubTree 
        extends AbstractBaseEntriesImpl<K, V> 
        implements NavigableBaseEntries<K, V> {
        
        private NavigableRange<K> range;
        
        private int sizeModCount;
        
        private int size;
        
        private int isEmptyModCount;
        
        private boolean isEmpty;
        
        private int absLowestModCount;
        
        private BaseEntryImpl<K, V> absLowest;
        
        private int absHighestModCount;
        
        private BaseEntryImpl<K, V> absHighest;
        
        private int absLowFenceModCount;
        
        private BaseEntryImpl<K, V> absLowFence;
        
        private int absHighFenceModCount;
        
        private BaseEntryImpl<K, V> absHighFence;
        
        public SubTree(NavigableRange<K> range) {
            this.range = range;
            int diffModCount = RedBlackTreeEntries.this.modCount - 1;
            this.sizeModCount = diffModCount;
            this.absLowestModCount = diffModCount;
            this.absHighestModCount = diffModCount;
            this.absLowFenceModCount = diffModCount;
            this.absHighFenceModCount = diffModCount;
        }
        
        @Override
        public boolean isReadWriteLockSupported() {
            return RedBlackTreeEntries.this.isReadWriteLockSupported();
        }
        
        @Override
        public int modCount() {
            return RedBlackTreeEntries.this.modCount;
        }

        @Override
        public NavigableRange<K> range() {
            return this.range;
        }

        @Override
        public Comparator<? super K> comparator() {
            return this.range.comparator(false);
        }
        
        @Override
        public UnifiedComparator<? super K> keyUnifiedComparator() {
            return UnifiedComparator.nullToEmpty(
                    UnifiedComparator.of(this.range.comparator(false))
            );
        }

        @Override
        public int size() {
            RedBlackTreeEntries<K, V> owner = RedBlackTreeEntries.this;
            if (this.sizeModCount == owner.modCount) {
                return this.size;
            }
            this.sizeModCount = owner.modCount;
            
            BaseEntryImpl<K, V> root = RedBlackTreeEntries.this.root;
            if (root == null) {
                return this.size = 0;
            }
            
            int size = root.size;
            NavigableRange<K> range = this.range;
            if (range.hasFrom(true)) {
                BaseEntryImpl<K, V> low = this.absLowest();
                if (low == null) {
                    return this.size = 0;
                }
                size -= sizeOf(low.left);
                while (true) {
                    BaseEntryImpl<K, V> lp = low.parent;
                    if (lp == null) {
                        break;
                    }
                    if (lp.right == low) {
                        size -= sizeOf(lp.left);
                        size--;
                    }
                    low = lp;
                }
            }
            if (range.hasTo(true)) {
                BaseEntryImpl<K, V> high = this.absHighest();
                if (high == null) {
                    return this.size = 0;
                }
                size -= sizeOf(high.right);
                while (true) {
                    BaseEntryImpl<K, V> hp = high.parent;
                    if (hp == null) {
                        break;
                    }
                    if (hp.left == high) {
                        size -= sizeOf(hp.right);
                        size--;
                    }
                    high = hp;
                }
            }
            return this.size = size;
        }
        
        @Override
        public boolean isEmpty() {
            RedBlackTreeEntries<K, V> owner = RedBlackTreeEntries.this;
            if (this.isEmptyModCount == owner.modCount) {
                return this.isEmpty;
            }
            this.isEmptyModCount = owner.modCount;
            
            BaseEntryImpl<K, V> absLowest = this.absLowest();
            return this.isEmpty = absLowest == null || this.range.tooHigh(absLowest.key, true);
        }
        
        @Override
        public BaseEntryImpl<K, V> first() {
            if (this.range.descending()) {
                return this.absHighest();
            }
            return this.absLowest();
        }

        @Override
        public BaseEntryImpl<K, V> last() {
            if (this.range.descending()) {
                return this.absLowest();
            }
            return this.absHighest();
        }

        @Override
        public BaseEntry<K, V> floor(K key) {
            if (this.range.descending()) {
                return this.absCeiling(key);
            }
            return this.absFloor(key);
        }

        @Override
        public BaseEntry<K, V> ceiling(K key) {
            if (this.range.descending()) {
                return this.absFloor(key);
            }
            return this.absCeiling(key);
        }

        @Override
        public BaseEntry<K, V> lower(K key) {
            if (this.range.descending()) {
                return this.absHigher(key);
            }
            return this.absLower(key);
        }

        @Override
        public BaseEntry<K, V> higher(K key) {
            if (this.range.descending()) {
                return this.absLower(key);
            }
            return this.absHigher(key);
        }

        @SuppressWarnings("unchecked")
        @Override
        public BaseEntry<K, V> getBaseEntry(Object key) {
            K k = (K)key;
            NavigableRange<K> range = this.range;
            if (!range.tooLow(k, true) && !range.tooHigh(k, true)) {
                return RedBlackTreeEntries.this.getBaseEntry(k);
            }
            return null;
        }
        
        @Override
        public final BaseEntry<K, V> pollFirst(BaseEntriesHandler<K, V> handler) {
            return this.pollFirst(this.triggerOf(handler));
        }
        
        @Override
        public final BaseEntry<K, V> pollLast(BaseEntriesHandler<K, V> handler) {
            return this.pollLast(this.triggerOf(handler));
        }
        
        @Override
        public NavigableBaseEntries<K, V> descendingEntries() {
            return RedBlackTreeEntries.this.new SubTree(NavigableRange.descendingRange(this));
        }

        @Override
        public NavigableBaseEntries<K, V> subEntries(
                boolean hasFrom, K from, boolean fromInclusive, 
                boolean hasTo, K to, boolean toInclusive) {
            return RedBlackTreeEntries.this.new SubTree(
                    NavigableRange.subRange(
                            this,
                            hasFrom, from, fromInclusive,
                            hasTo, to, toInclusive
                    )
            );
        }

        @Override
        public BaseEntryIterator<K, V> iterator() {
            if (this.range.descending()) {
                return RedBlackTreeEntries.this.new DescendingEntryIterator(
                        this.absHighest(), 
                        this.absLowFence()
                );
            }
            return RedBlackTreeEntries.this.new AscendingEntryIterator(
                    this.absLowest(), 
                    this.absHighFence()
            );
        }

        @Override
        protected AbstractBaseEntriesImpl<K, V> getParent() {
            return RedBlackTreeEntries.this;
        }

        @Override
        protected void deleteBaseEntry(BaseEntry<K, V> be) {
            RedBlackTreeEntries.this.deleteBaseEntry0((BaseEntryImpl<K, V>)be);
        }

        @Override
        protected V put(K key, V value, Trigger<K, V> trigger, FrozenContextSuspending<K, V> suspending) {
            if (!this.range.contains(key)) {
                throw new IllegalArgumentException(elementOfKeyOutOfRange(this.range));
            }
            return RedBlackTreeEntries.this.put(key, value, trigger, suspending);
        }

        @Override
        protected void putAll(Map<? extends K, ? extends V> m, Trigger<K, V> trigger) {
            if (!this.range.containsAll(m.keySet())) {
                throw new IllegalArgumentException(elementOfKeyOutOfRange(this.range));
            }
            RedBlackTreeEntries.this.putAll(m, trigger);
        }

        @Override
        protected boolean addAll(
                Collection<? extends K> kc, 
                Trigger<K, V> trigger) {
            if (!this.range.containsAll(kc)) {
                throw new IllegalArgumentException(elementOfKeyOutOfRange(this.range));
            }
            return RedBlackTreeEntries.this.addAll(kc, trigger);
        }

        protected BaseEntry<K, V> pollFirst(Trigger<K, V> trigger) {
            BaseEntryImpl<K, V> be = this.first();
            if (be != null) {
                if (trigger != null) {
                    trigger.preRemove(be);
                    if (trigger.beginExecute()) {
                        try {
                            RedBlackTreeEntries.this.deleteBaseEntry0(be);
                            trigger.endExecute(null);
                        } catch (RuntimeException | Error ex) {
                            trigger.endExecute(ex);
                        }
                    }
                    trigger.flush();
                } else {
                    this.deleteBaseEntry(be);
                }
            }
            return be;
        }
        
        protected BaseEntry<K, V> pollLast(Trigger<K, V> trigger) {
            BaseEntryImpl<K, V> be = this.last();
            if (be != null) {
                if (trigger != null) {
                    trigger.preRemove(be);
                    if (trigger.beginExecute()) {
                        try {
                            RedBlackTreeEntries.this.deleteBaseEntry0(be);
                            trigger.endExecute(null);
                        } catch (RuntimeException | Error ex) {
                            trigger.endExecute(ex);
                        }
                    }
                    trigger.flush();
                } else {
                    this.deleteBaseEntry(be);
                }
            }
            return be;
        }
        
        @Override
        protected void clear(Trigger<K, V> trigger) {
            
            NavigableRange<K> range = this.range;
            if (!range.hasFrom(true) && !range.hasTo(true)) {
                RedBlackTreeEntries.this.clear(trigger);
                return;
            }
        
            if (trigger != null) {
                BaseEntryIterator<K, V> iterator = this.iterator();
                while (iterator.hasNext()) {
                    trigger.preRemove(iterator.next());
                }
            }
        
            BaseEntryIterator<K, V> iterator = this.iterator();
            if (trigger == null) {
                while (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove(null);
                }
            } else if (trigger.beginExecute()) {
                try {
                    while (iterator.hasNext()) {
                        iterator.next();
                        iterator.remove(null);
                    }
                    trigger.endExecute(null);
                } catch (RuntimeException | Error ex) {
                    trigger.endExecute(ex);
                }
                trigger.flush();
            }
        }

        private BaseEntryImpl<K,V> absLowest() {
            
            RedBlackTreeEntries<K, V> owner = RedBlackTreeEntries.this;
            if (this.absLowestModCount == owner.modCount) {
                return this.absLowest;
            }
            this.absLowestModCount = owner.modCount;
            
            NavigableRange<K> range = this.range;
            BaseEntryImpl<K, V> e = null;
            if (!range.hasFrom(true)) {
                e = RedBlackTreeEntries.this.first();
            } else {
                e = range.fromInclusive(true) ? 
                        owner.ceiling(range.from(true)) :
                        owner.higher(range.from(true));
            }
            return this.absLowest = (e == null || this.range.tooHigh(e.key, true)) ? null : e;
        }

        private BaseEntryImpl<K,V> absHighest() {
            
            RedBlackTreeEntries<K, V> owner = RedBlackTreeEntries.this;
            if (this.absHighestModCount == owner.modCount) {
                return this.absHighest;
            }
            this.absHighestModCount = owner.modCount;
            
            NavigableRange<K> range = this.range;
            BaseEntryImpl<K, V> e = null;
            if (!range.hasTo(true)) {
                e = owner.last();
            } else {
                e = range.toInclusive(true) ?
                        owner.floor(range.to(true)) :
                        owner.lower(range.to(true));
            }
            return this.absHighest = (e == null || this.range.tooLow(e.key, true)) ? null : e;
        }

        private BaseEntryImpl<K, V> absLowFence() {
            
            RedBlackTreeEntries<K, V> owner = RedBlackTreeEntries.this;
            if (this.absLowFenceModCount == owner.modCount) {
                return this.absLowFence;
            }
            this.absLowFenceModCount = owner.modCount;
            
            NavigableRange<K> range = this.range;
            if (!range.hasFrom(true)) {
                return this.absLowFence = null;
            }
            K minimum = range.from(true);
            return this.absLowFence =
                range.fromInclusive(true) ? 
                    owner.lower(minimum) : 
                    owner.floor(minimum);
        }

        private BaseEntryImpl<K, V> absHighFence() {
            
            RedBlackTreeEntries<K, V> owner = RedBlackTreeEntries.this;
            if (this.absHighFenceModCount == owner.modCount) {
                return this.absHighFence;
            }
            this.absHighFenceModCount = owner.modCount;
            
            NavigableRange<K> range = this.range;
            if (!range.hasTo(true)) {
                return this.absHighFence = null;
            }
            K maximum = range.to(true);
            return this.absHighFence =
                range.toInclusive(true) ? 
                    owner.higher(maximum) :
                    owner.ceiling(maximum);
        }

        private BaseEntryImpl<K,V> absFloor(K key) {
            NavigableRange<K> range = this.range;
            if (range.tooHigh(key, true)) {
                return this.absHighest();
            }
            BaseEntryImpl<K, V> be = RedBlackTreeEntries.this.floor(key);
            return (be == null || range.tooLow(be.key, true)) ? null : be;
        }

        private BaseEntryImpl<K,V> absCeiling(K key) {
            NavigableRange<K> range = this.range;
            if (range.tooLow(key, true)) {
                return this.absLowest();
            }
            BaseEntryImpl<K, V> be = RedBlackTreeEntries.this.ceiling(key);
            return (be == null || range.tooHigh(be.key, true)) ? null : be;
        }

        private BaseEntryImpl<K,V> absLower(K key) {
            NavigableRange<K> range = this.range;
            if (range.tooHigh(key, true)) {
                return this.absHighest();
            }
            BaseEntryImpl<K, V> be = RedBlackTreeEntries.this.lower(key);
            return (be == null || range.tooLow(be.key, true)) ? null : be;
        }

        private BaseEntryImpl<K,V> absHigher(K key) {
            NavigableRange<K> range = this.range;
            if (range.tooLow(key, true)) {
                return this.absLowest();
            }
            BaseEntryImpl<K,V> be = RedBlackTreeEntries.this.higher(key);
            return (be == null || range.tooHigh(be.key, true)) ? null : be;
        }
    }

    private abstract class AbstractEntryIterator implements BaseEntryIterator<K, V> {
        
        /*
         * In JDK source code, iterator implementation contains a field named
         * "fenceKey" not "fence", because in that implementation, method
         * "deleteEntry" may change the identifier of each each entry object,
         * sometimes the method may swap the key and value of two entries. in
         * JDK's implementation, use these code to decide whether a entry(e) is
         * the invalid position: e == null || e.key == this.fenceKey But in my
         * implementation, method deleteBaseEntry can guarantee that each entry
         * object's key and value is read-only, so here I can use these code to
         * decide whether the entry(e) is the invalid position: e == this.fence
         */
        private final BaseEntryImpl<K, V> fence;

        private BaseEntryImpl<K, V> lastReturned;

        private BaseEntryImpl<K, V> next;
        
        private int expectedModCount;

        public AbstractEntryIterator(
                BaseEntryImpl<K, V> first, 
                BaseEntryImpl<K, V> fence) {
            this.next = first;
            this.fence = first == null ? null : fence;
            this.expectedModCount = RedBlackTreeEntries.this.modCount;
        }
        
        protected Trigger<K, V> trigger(BaseEntriesHandler<K, V> handler) {
            return RedBlackTreeEntries.this.triggerOf(handler);
        }

        public final boolean hasNext() {
            return this.next != this.fence;
        }

        protected final BaseEntryImpl<K, V> nextEntry() {
            RedBlackTreeEntries<K, V> owner = RedBlackTreeEntries.this;
            if (this.expectedModCount != owner.modCount) {
                throw new ConcurrentModificationException(IteratorMessages.concurrentModifcation());
            }
            final BaseEntryImpl<K, V> e = this.next;
            if (e == this.fence) {
                throw new NoSuchElementException(IteratorMessages.noSuchElement());
            }
            this.next = RedBlackTreeEntries.successor(e);
            return this.lastReturned = e;
        }

        protected final BaseEntryImpl<K, V> prevEntry() {
            RedBlackTreeEntries<K, V> owner = RedBlackTreeEntries.this;
            if (this.expectedModCount != owner.modCount) {
                throw new ConcurrentModificationException(IteratorMessages.concurrentModifcation());
            }
            final BaseEntryImpl<K, V> e = this.next;
            if (e == this.fence) {
                throw new NoSuchElementException(IteratorMessages.noSuchElement());
            }
            
            this.next = RedBlackTreeEntries.predecessor(e);
            return this.lastReturned = e;
        }

        protected final void removeAscending(BaseEntriesHandler<K, V> handler) {
            final RedBlackTreeEntries<K, V> owner = RedBlackTreeEntries.this;
            if (this.expectedModCount != owner.modCount) {
                throw new ConcurrentModificationException(IteratorMessages.concurrentModifcation());
            }
            final BaseEntryImpl<K, V> e = this.lastReturned;
            if (e == this.fence) {
                throw new IllegalStateException(IteratorMessages.removeNoExtractedElement());
            }
            Trigger<K, V> trigger = this.trigger(handler);
            if (trigger != null) {
                trigger.preRemove(e);
            }
            
            /*
             * In the TreeMap implementation of JDK, this "if" statement is: 
             * if (e.left != null && e.right != null) { 
             *      this.next = e; 
             * }
             * this.deleteEntry(e);
             * 
             * because in JDK's implementation, method "deleteEntry" swaps the
             * key/value pairs of two entries if both the left field and right
             * field of the parameter is not null.
             * 
             * But for special reasons, In my implementation, the method
             * "deleteEntry" keeps the key/value pair but swaps their references
             * in the red-black tree. so my code is: 
             * BaseEntryImpl<K, V> newNext = owner.deleteBaseEntry(e); 
             * if (newNext != null) { 
             *      this.next = newNext; 
             * }
             */
            if (trigger == null || trigger.beginExecute()) {
                try {
                    BaseEntryImpl<K, V> newNext = owner.deleteBaseEntry0(e);
                    if (newNext != null) {
                        this.next = newNext;
                    }
                    this.expectedModCount = owner.modCount;
                    this.lastReturned = null;
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

        protected final void removeDescending(BaseEntriesHandler<K, V> handler) {
            final RedBlackTreeEntries<K, V> owner = RedBlackTreeEntries.this;
            if (this.expectedModCount != owner.modCount) {
                throw new ConcurrentModificationException(IteratorMessages.concurrentModifcation());
            }
            final BaseEntryImpl<K, V> e = this.lastReturned;
            if (e == this.fence) {
                throw new IllegalStateException(IteratorMessages.removeNoExtractedElement());
            }
            
            Trigger<K, V> trigger = this.trigger(handler);
            if (trigger != null) {
                trigger.preRemove(e);
            }
            if (trigger == null || trigger.beginExecute()) {
                try {
                    owner.deleteBaseEntry0(e);
                    this.expectedModCount = owner.modCount;
                    this.lastReturned = null;
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

    class AscendingEntryIterator extends AbstractEntryIterator {

        public AscendingEntryIterator(
                BaseEntryImpl<K, V> first, 
                BaseEntryImpl<K, V> fence) {
            RedBlackTreeEntries.this.super(first, fence);
        }

        @Override
        public final BaseEntryImpl<K, V> next() {
            return this.nextEntry();
        }

        @Override
        public final void remove(BaseEntriesHandler<K, V> handler) {
            this.removeAscending(handler);
        }

    }

    class DescendingEntryIterator extends AbstractEntryIterator {

        public DescendingEntryIterator(
                BaseEntryImpl<K, V> first, 
                BaseEntryImpl<K, V> fence) {
            RedBlackTreeEntries.this.super(first, fence);
        }

        @Override
        public final BaseEntryImpl<K, V> next() {
            return this.prevEntry();
        }

        @Override
        public final void remove(BaseEntriesHandler<K, V> handler) {
            this.removeDescending(handler);
        }

    }

    static class BaseEntryImpl<K, V> extends AbstractBaseEntryImpl<K, V> {
        
        /**
         * This field could be used like the "ReadBlackTreeEntries.this" 
         * if this class was a non-static inner class of ReadBlackTreeEntries<K, V>.
         * But it can be null after this object has be deleted from ReadBlackTreeEntries.
         */
        RedBlackTreeEntries<K, V> owner;
        
        /*
         * This field is final, it's very important!
         * 
         * This RedBlackTree does not want to implement the method
         * deleteBaseEntry like JDK's implementation.
         * 
         * In JDK, TreeMap swaps the key/value pair of two entries when its
         * method "deleteEntry" is called with a parameter whose left and
         * right are both not null.
         * 
         * Here I must not do this thing like JDK, because 1) This solution will
         * cause each entry's identifier is not unique, it's will make me be
         * hard to implements modification aware event notification mechanism.
         * 2) This solution may change another entry's data when the program
         * deletes a entry. that can also make modification aware event
         * notification mechanism is hard to implement.
         * 
         * So, In my implementation, method "deleteBaseEntry" never try to swap
         * key/value pair of two entries. It swap the references of two entries
         * if need.
         * 
         * Here, I mark this field to be final, so the complier can report some
         * errors if I forget this important thing.
         */
        private K key;
    
        private V value;
        
        private BaseEntryImpl<K, V> left;
    
        private BaseEntryImpl<K, V> right;
    
        private BaseEntryImpl<K, V> parent;
        
        /*
         * The count of this entry and all the entries that 
         * are children of this entry (recursive)
         * 
         * assert
         *    this.size == 
         *      1 +
         *      (this.left == null ? 0 : this.left.size) +
         *      (this.right == null ? 0 : this.right.size);
         * 
         * This field can improve the efficiency the method 
         * "size()" of SubTree
         * @see RedBlackTree#subTree(
         *    boolean, K, boolean, 
         *    boolean, K, boolean)
         * 
         * Specially!!!
         * When the size is zero, that means the
         * entry has been dropped by tree so that the entry
         * can not raise any event when its method "setValue"
         * is called.
         */
        private int size;
    
        private boolean red;
        
        private UnifiedComparator<? super Entry<K, V>> unifiedComparator;
    
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public BaseEntryImpl(RedBlackTreeEntries<K, V> owner, BaseEntryImpl<K, V> parent, K key, V value) {
            if (key != null) {
                Object keyComparator = owner.keyComparatorOrEqualityComparator();
                if (keyComparator instanceof FrozenComparator<?>) {
                    FrozenComparator<? super K> frozenComparator =
                            (FrozenComparator<? super K>)keyComparator;
                    frozenComparator.freeze(key, (FrozenContext)FrozenContext.create(owner));
                }
                this.key = key;
            }
            this.owner = owner;
            this.parent = parent;
            this.value = value;
            this.size = 1;
            this.unifiedComparator = owner != null ? owner.entryUnifiedComparator() : null;
        }
        
        @Override
        public boolean isNonFairLockSupported() {
            return true;
        }
        
        @Override
        public AbstractBaseEntriesImpl<K, V> getOwner() {
            return this.owner;
        }
        
        @Override
        public final K getKey() {
            return this.key;
        }
        
        @Override
        public final V getValue() {
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
    
        @SuppressWarnings({ "unchecked", "rawtypes" })
        /* virtual */
        void recordRemove() {
            RedBlackTreeEntries<K, V> owner = this.owner;
            K oldKey = this.key;
            this.owner = null;
            this.size = 0;
            
            if (oldKey != null) {
                Object keyComparator = 
                        owner.keyComparatorOrEqualityComparator();
                if (keyComparator instanceof FrozenComparator<?>) {
                    FrozenComparator<? super K> frozenComparator =
                            (FrozenComparator<? super K>)keyComparator;
                    frozenComparator.unfreeze(
                            oldKey, 
                            (FrozenContext)FrozenContext.create(owner));
                }
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void setRawKey(K key, Object comparator) {
            /*
             * Actually, the parameter "comparator" is unnecessary,
             * because it is very easy to get such as these methods
             *  
             * (1) this.owner.keyComparatorOrEqualityComparator()
             * (2) this.owner.keyUnifiedComparator().comparator()
             * (3) this.owner.comparator()
             * 
             * in owner.putAllImpl and owner.addAllImpl, this method
             * will be called for several times by a loop statement,
             * so the the outside method can store it as a local variable and
             * use it to be the second argument of this method in loop body
             * to optimize the program.
             * 
             * The parameter is Object, 
             * that means I choose the owner.keyComparatorOrEqualityComparator()
             * neither owner.keyUnifiedComparator().comparator() 
             * nor owner.comparator()
             * because it is the fastest one of them.
             * 
             * It is hard to understand why that is faster than this.comparator()
             * because
             * "Object --CHECKCAST--> FrozenComparator"
             * is fast than
             * "Object --CHECKCAST--> Comparator --CHECKCAST--> FrozenComparator"
             */
            K oldKey = this.key;
            if (oldKey != key) {
                if (comparator instanceof FrozenComparator<?>) {
                    FrozenContext frozenContext = FrozenContext.create(this.owner);
                    FrozenComparator<? super K> frozenComparator =
                            (FrozenComparator<? super K>)comparator;
                    if (key != null) {
                        frozenComparator.freeze(key, frozenContext);
                    }
                    this.key = key;
                    if (oldKey != null) {
                        frozenComparator.unfreeze(oldKey, frozenContext);
                    }
                } else {
                    this.key = key;
                }
            }
        }
    }
    
    public static class TransientValue<K, V> extends RedBlackTreeEntries<K, V> implements TransientValueEntries {

        private static final long serialVersionUID = -7420559212323684655L;

        public TransientValue(
                ReplacementRule keyReplacementRule,
                Comparator<? super K> comparator,
                Object valueComparatorOrEqualityComparatorOrUnifiedComparator) {
            super(
                    BidiType.NONE,
                    keyReplacementRule, 
                    comparator,
                    valueComparatorOrEqualityComparatorOrUnifiedComparator);
        }
    }
    
    @I18N
    private static native String elementOfKeyOutOfRange(NavigableRange<?> range);
}
