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

import org.babyfish.collection.BidiType;
import org.babyfish.collection.ReaderOptimizationType;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.spi.base.AbstractBaseElementsImpl.Trigger.History;
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public final class LinkedElements<E> extends AbstractBaseElementsImpl<E> {
    
    private static final long serialVersionUID = 6722586312433255701L;

    private NodeImpl<E> invalid;
    
    private int allSize;
    
    private ReaderOptimizationType readerOptimizationType;
    
    //Both this member and its fields can not be changed 
    //when readerOptimizationType == ReaderOptimizationType.OPITIMIZE_READ_LOCK
    private transient LastAccess<E> lastAccess;
    
    private transient int modCount;
    
    public LinkedElements(
            BidiType bidiType,
            ReaderOptimizationType readerOptimizationType, 
            Object comparatorOrEqualityComparatorOrUnifiedComparator) {
        super(bidiType, comparatorOrEqualityComparatorOrUnifiedComparator);
        this.readerOptimizationType = Arguments.mustNotBeNull("readerOptimizationType", readerOptimizationType);
        NodeImpl<E> invalid = new NodeImpl<>();
        this.invalid = invalid;
        invalid.prev = invalid.next = invalid;
    }
    
    public ReaderOptimizationType readerOptimizationType() {
        return this.readerOptimizationType;
    }
    
    @Override
    public boolean isReadWriteLockSupported() {
        return this.readerOptimizationType == ReaderOptimizationType.OPTIMIZE_READ_LOCK;
    }

    @Override
    public boolean randomAccess() {
        return false;
    }
    
    @Override
    public int allSize() {
        return this.allSize;
    }
    
    @Override
    public E get(int subListHeadHide, int subListTailHide, int index) {
        this.checkSubListRange(subListHeadHide, subListTailHide);
        Arguments.indexMustBetweenOther(
                "index", 
                index, 
                "0", 
                0, 
                true, 
                "allSize() - subListHeadHide - subListTailHide", 
                this.allSize - subListHeadHide - subListTailHide, 
                false);
        NodeImpl<E> node = this.nodeAt(subListHeadHide + index, true);
        return node == null ? null : node.value;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected int firstIndex(int subListHeadHide, int subListTailHide, Object o) {
        
        this.checkSubListRange(subListHeadHide, subListTailHide);
        
        UnifiedComparator<? super E> unifiedComparator = this.unifiedComparator();
        LastAccess<E> lastAccess = this.lastAccess;
        if (lastAccess !=null && 
                lastAccess.matchFirst && 
                lastAccess.index >= subListHeadHide && 
                lastAccess.index < this.allSize - subListTailHide) {
            if (o == null ? 
                    lastAccess.node.value == null : 
                    unifiedComparator.equals(
                            (E)o, 
                            lastAccess.node.value)) {
                return lastAccess.index - subListHeadHide;
            }
        }
        int absIndex = subListHeadHide;
        int fenceIndex = this.allSize - subListTailHide;
        NodeImpl<E> node = this.nodeAt(absIndex, false);
        if (o == null) {
            while (absIndex < fenceIndex) {
                if (node.value == null) {
                    break;
                }
                absIndex++;
                node = node.next;
            }
        } else {
            while (absIndex < fenceIndex) {
                if (unifiedComparator.equals((E)o, node.value)) {
                    break;
                }
                absIndex++;
                node = node.next;
            }
        }
        if (absIndex != fenceIndex) {
            if (this.readerOptimizationType == ReaderOptimizationType.OPTIMIZE_READING) {
                boolean fullScan = 
                    subListHeadHide == 0 && subListTailHide == 0;
                if (lastAccess == null) {
                    this.lastAccess = new LastAccess<E>(absIndex, node, fullScan, false);
                } else {
                    lastAccess.index = absIndex;
                    lastAccess.node = node;
                    lastAccess.matchFirst = fullScan;
                }
            }
            return absIndex - subListHeadHide;
        }
        return -1;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected int lastIndex(int subListHeadHide, int subListTailHide, Object o) {
        
        this.checkSubListRange(subListHeadHide, subListTailHide);
        
        UnifiedComparator<? super E> unifiedComparator = this.unifiedComparator();
        LastAccess<E> lastAccess = this.lastAccess;
        if (lastAccess !=null && 
                lastAccess.matchLast && 
                lastAccess.index >= subListHeadHide && 
                lastAccess.index < this.allSize - subListTailHide) {
            if (o == null ? 
                    lastAccess.node.value == null : 
                        unifiedComparator.equals((E)o, lastAccess.node.value)) {
                return lastAccess.index - subListHeadHide;
            }
        }
        int absIndex = this.allSize - subListTailHide;
        NodeImpl<E> node = this.nodeAt(absIndex, false);
        if (o == null) {
            while (absIndex >= subListHeadHide) {
                if (node.value == null) {
                    break;
                }
                absIndex++;
                node = node.next;
            }
        } else {
            while (absIndex >= subListHeadHide) {
                if (unifiedComparator.equals((E)o, node.value)) {
                    break;
                }
                absIndex++;
                node = node.next;
            }
        }
        if (absIndex != subListHeadHide - 1) {
            if (this.readerOptimizationType == ReaderOptimizationType.OPTIMIZE_READING) {
                boolean fullScan = 
                    subListHeadHide == 0 && subListTailHide == 0;
                if (lastAccess == null) {
                    this.lastAccess = new LastAccess<E>(absIndex, node, false, fullScan);
                } else {
                    lastAccess.index = absIndex;
                    lastAccess.node = node;
                    lastAccess.matchLast = fullScan;
                }
            }
            return absIndex - subListHeadHide;
        }
        return -1;
    }

    @Override
    protected final boolean isNodeSupported() {
        return true;
    }

    @Override
    protected void addImpl(E element, AttachProcessor<E> attachProcessor) {
        
        Trigger<E> trigger = attachProcessor.getTrigger();
        if (trigger != null) {
            trigger.preAdd(attachProcessor.getActualIndex(false), element);
        }
        if (attachProcessor.beginExecute()) {
            try {
                int absIndex = attachProcessor.getActualIndex(true);
                LastAccess<E> lastAccess = this.lastAccess;
                NodeImpl<E> addBefore = 
                    absIndex == this.allSize ?
                            this.invalid :
                            this.nodeAt(absIndex, false);
                if (this.readerOptimizationType == ReaderOptimizationType.OPTIMIZE_READING && lastAccess != null) {
                    if (absIndex <= lastAccess.index) {
                        if (lastAccess.matchFirst &&
                                (element == null ? lastAccess.node.value == null : element.equals(lastAccess.node.value))) {
                            lastAccess.matchFirst = false;
                        }
                        lastAccess.index++;
                    } else {
                        if (lastAccess.matchLast && 
                                (element == null ? lastAccess.node.value == null : element.equals(lastAccess.node.value))) {
                            lastAccess.matchLast = false;
                        }
                    }
                }
                this.addBefore(addBefore, element);
                BaseEntries<E, Object> inversedEntries = this.inversedEntries();
                if (inversedEntries != null) {
                    inversedEntries.put(element, null, null);
                }
                this.allSize++;
                this.modCount++;
                attachProcessor.endExecute(null);
            } catch (RuntimeException | Error ex) {
                attachProcessor.endExecute(ex);
            }
        }
        attachProcessor.flush();
    }
    
    @Override
    protected void addAllImpl(Collection<? extends E> c, AttachProcessor<E> attachProcessor) {
        
        Trigger<E> trigger = attachProcessor.getTrigger();
        if (trigger != null) {
            int addedCount = 0;
            int actualIndex = attachProcessor.getActualIndex(false);
            for (E e : c) {
                trigger.preAdd(actualIndex + addedCount++, e);
            }
        }
        if (attachProcessor.beginExecute()) {
            try {
                int absIndex = attachProcessor.getActualIndex(true);
                //Don't call c.size() because some implementation of it is not fast.
                NodeImpl<E> addBefore = 
                    absIndex == this.allSize ?
                            this.invalid :
                            this.nodeAt(absIndex, false);
                LastAccess<E> lastAccess = this.lastAccess;
                if (this.readerOptimizationType == ReaderOptimizationType.OPTIMIZE_READING && lastAccess != null) {
                    for (E e : c) {
                        if (absIndex <= lastAccess.index) {
                            if (lastAccess.matchFirst &&
                                    (e == null ? lastAccess.node.value == null : e.equals(lastAccess.node.value))) {
                                lastAccess.matchFirst = false;
                            }
                            lastAccess.index++;
                        } else {
                            if (lastAccess.matchLast &&
                                    (e == null ? lastAccess.node.value == null : e.equals(lastAccess.node.value))) {
                                lastAccess.matchLast = false;
                            }
                        }
                        this.addBefore(addBefore, e);
                        this.allSize++;
                    }
                } else {
                    for (E e : c) {
                        this.addBefore(addBefore, e);
                        this.allSize++;
                    }
                }
                BaseEntries<E, Object> inversedEntries = this.inversedEntries();
                if (inversedEntries != null) {
                    for (E e : c) {
                        inversedEntries.put(e, null, null);
                    }
                }
                this.modCount++;
                attachProcessor.endExecute(null);
            } catch (RuntimeException | Error ex) {
                attachProcessor.endExecute(ex);
            }
        }
        attachProcessor.flush();
    }

    @Override
    protected E setImpl(E element, AttachProcessor<E> attachProcessor) {
        int expectedAbsIndex = attachProcessor.getExpectedIndex(true);
        NodeImpl<E> node = this.nodeAt(expectedAbsIndex, true);
        Trigger<E> trigger = attachProcessor.getTrigger();
        if (trigger != null) {
            trigger.preChange(
                    attachProcessor.getExpectedIndex(false),
                    attachProcessor.getActualIndex(false), 
                    node.value, 
                    element);
        }
        E retval = null;
        if (attachProcessor.beginExecute()) {
            try {
                retval = node.value;
                node.value = element;
                BaseEntries<E, Object> inversedEntries = this.inversedEntries();
                if (inversedEntries != null) {
                    inversedEntries.removeByKey(retval, null);
                    inversedEntries.put(element, null, null);
                }
                attachProcessor.endExecute(null);
            } catch (RuntimeException | Error ex) {
                attachProcessor.endExecute(ex);
            }
        }
        attachProcessor.flush();
        return retval;
    }

    @Override
    protected void clear(int subListHeadHide, int subListTailHide, Trigger<E> trigger) {
        this.checkSubListRange(subListHeadHide, subListTailHide);
        NodeImpl<E> firstNode = this.nodeAt(subListHeadHide, false);
        NodeImpl<E> lastNode = this.nodeAt(this.allSize - subListTailHide - 1, false);
        BaseEntries<E, Object> inversedEntries = this.inversedEntries();
        if (inversedEntries != null) {
            if (subListHeadHide == 0 && subListTailHide == 0) {
                inversedEntries.clear(null);
            } else {
                NodeImpl<E> fenceNode = lastNode.next;
                for (NodeImpl<E> removedNode = firstNode; 
                        removedNode != fenceNode; 
                        removedNode = removedNode.next) {
                    inversedEntries.removeByKey(removedNode.value, null);
                }
            }
        }
        LastAccess<E> lastAccess = this.lastAccess;
        if (trigger == null) {
            firstNode.prev.next = lastNode.next;
            lastNode.next.prev = firstNode.prev;
            this.allSize = subListHeadHide + subListTailHide;
            if (this.readerOptimizationType == ReaderOptimizationType.OPTIMIZE_READING &&
                    lastAccess != null && 
                    lastAccess.index >= subListHeadHide && 
                    lastAccess.index < this.allSize - subListTailHide) {
                this.lastAccess = null;
            }
            this.modCount++;
        } else {
            int index = 0;
            NodeImpl<E> fenceNode = lastNode.next;
            for (NodeImpl<E> node = firstNode; node != fenceNode; node = node.next) {
                trigger.preRemove(index, node.value);
                index++;
            }
            if (trigger.beginExecute()) {
                try {
                    firstNode.prev.next = lastNode.next;
                    lastNode.next.prev = firstNode.prev;
                    this.allSize = subListHeadHide + subListTailHide;
                    if (this.readerOptimizationType == ReaderOptimizationType.OPTIMIZE_READING &&
                            lastAccess != null && 
                            lastAccess.index >= subListHeadHide && 
                            lastAccess.index < this.allSize - subListTailHide) {
                        this.lastAccess = null;
                    }
                    this.modCount++;
                    trigger.endExecute(null);
                } catch (RuntimeException | Error ex) {
                    trigger.endExecute(ex);
                }
            }
            trigger.flush();
        }
    }
    
    @Override
    protected boolean remove(int subListHeadHide, int subListTailHide, Object o, Trigger<E> trigger) {
        // Need not to call this.checkSubListRange because this.indexOf will call it
        int index = this.indexOf(subListHeadHide, subListTailHide, o);
        if (index != -1) {
            this.removeAt(subListHeadHide, subListTailHide, index, trigger);
            return true;
        }
        return false;
    }
    
    @Override
    protected E removeAt(int subListHeadHide, int subListTailHide, int index, Trigger<E> trigger) {
        this.checkSubListRange(subListHeadHide, subListTailHide);
        Arguments.indexMustBetweenOther(
                "index", 
                index, 
                "0", 
                0, 
                true, 
                "allSize() - subListHeadHide - subListTailHide", 
                this.allSize - subListHeadHide - subListTailHide, 
                false);
        if (trigger == null) {
            NodeImpl<E> node = this.deleteAt(subListHeadHide + index, null);
            this.modCount++;
            return node.value;
        } else {
            NodeImpl<E> node = this.nodeAt(subListHeadHide + index, true);
            trigger.preRemove(index, node.value);
            if (trigger.beginExecute()) {
                try {
                    this.deleteAt(subListHeadHide + index, node);
                    this.modCount++;
                    trigger.endExecute(null);
                } catch (RuntimeException | Error ex) {
                    trigger.endExecute(ex);
                }
            }
            trigger.flush();
            return node.value;
        }
    }
    
    @Override
    protected boolean removeAll(int subListHeadHide, int subListTailHide, Collection<?> c, Trigger<E> trigger) {
        this.checkSubListRange(subListHeadHide, subListTailHide);
        int fenceIndex = this.allSize - subListTailHide;
        NodeImpl<E> node = this.nodeAt(subListHeadHide, false);
        @SuppressWarnings("unchecked")
        OverriddenContainsBehavior ocb = OverriddenContainsBehavior.of(
                (Collection<? extends E>)c, this.unifiedComparator());
        if (trigger == null) {
            int deleteIndex = subListHeadHide;
            int absIndex = subListHeadHide;
            while (absIndex < fenceIndex) {
                if (ocb.contains(node.value)) {
                    this.deleteAt(deleteIndex, node);
                } else {
                    deleteIndex++;
                }
                absIndex++;
                node = node.next;
            }
            if (deleteIndex != absIndex) {
                this.modCount++;
                return true;
            } 
            return false;
        }
        
        for (int absIndex = subListHeadHide; absIndex < fenceIndex; absIndex++) {
            if (ocb.contains(node.value)) {
                trigger.preRemove(absIndex - subListHeadHide, node);
            }
            node = node.next;
        }
        if (trigger.beginExecute()) {
            try {
                History<E> history = trigger.getHistory(0);
                for (int i = 0; i < trigger.getLength(); i++) {
                    this.deleteAt(history.getOldIndex(i) - i, (NodeImpl<E>)history.getNode(i));
                }
                if (trigger.getLength() != 0) {
                    this.modCount++;
                }
                trigger.endExecute(null);
            } catch (RuntimeException | Error ex) {
                trigger.endExecute(ex);
            }
        }
        trigger.flush();
        return trigger.getLength() != 0;
    }
    
    @Override
    protected boolean retainAll(int subListHeadHide, int subListTailHide, Collection<?> c, Trigger<E> trigger) {
        this.checkSubListRange(subListHeadHide, subListTailHide);
        int fenceIndex = this.allSize - subListTailHide;
        NodeImpl<E> node = this.nodeAt(subListHeadHide, false);
        @SuppressWarnings("unchecked")
        OverriddenContainsBehavior ocb = OverriddenContainsBehavior.of(
                (Collection<? extends E>)c, this.unifiedComparator());
        if (trigger == null) {
            int deleteIndex = subListHeadHide;
            int absIndex = subListHeadHide;
            while (absIndex < fenceIndex) {
                if (!ocb.contains(node.value)) {
                    this.deleteAt(deleteIndex, node);
                } else {
                    deleteIndex++;
                }
                absIndex++;
                node = node.next;
            }
            if (deleteIndex != absIndex) {
                this.modCount++;
                return true;
            } 
            return false;
        }
        
        for (int absIndex = subListHeadHide; absIndex < fenceIndex; absIndex++) {
            if (!ocb.contains(node.value)) {
                trigger.preRemove(absIndex - subListHeadHide, node);
            }
            node = node.next;
        }
        if (trigger.beginExecute()) {
            try {
                History<E> history = trigger.getHistory(0);
                for (int i = 0; i < trigger.getLength(); i++) {
                    this.deleteAt(history.getOldIndex(i) - i, (NodeImpl<E>)history.getNode(i));
                }
                if (trigger.getLength() != 0) {
                    this.modCount++;
                }
                trigger.endExecute(null);
            } catch (RuntimeException | Error ex) {
                trigger.endExecute(ex);
            }
        }
        trigger.flush();
        return trigger.getLength() != 0;
    }
    
    @Override
    public BaseListIterator<E> listIterator(
            int subListHeadHide, 
            int subListTailHide, 
            int index, 
            BaseElementsConflictHandler conflictHandler) {
        return this.new BaseElementIteratorImpl(subListHeadHide, subListTailHide, index, conflictHandler);
    }

    @Override
    public int modCount() {
        return this.modCount;
    }

    private NodeImpl<E> nodeAt(int absIndex, boolean updateLastAccess) {
        LastAccess<E> lastAccess = this.lastAccess;
        NodeImpl<E> lastAccessNode = lastAccess == null ? null : lastAccess.node;
        int fromHead = absIndex;
        int fromEnd = this.allSize - absIndex - 1;
        int fromLastAccess = lastAccess == null ? Integer.MAX_VALUE : Math.abs(absIndex - lastAccess.index);
        
        if (fromLastAccess < fromHead && fromLastAccess < fromEnd) {
            assert lastAccess != null;
            if (absIndex >= lastAccess.index) {
                for (int i = 0; i < fromLastAccess; i++) {
                    lastAccessNode = lastAccessNode.next;
                }
            } else {
                for (int i = 0; i < fromLastAccess; i++) {
                    lastAccessNode = lastAccessNode.prev;
                }
            }
        } else {
            if (fromHead < fromEnd) {
                lastAccessNode = this.invalid.next;
                for (int i = 0; i < fromHead; i++) {
                    lastAccessNode = lastAccessNode.next;
                }
            } else {
                lastAccessNode = this.invalid.prev;
                for (int i = 0; i < fromEnd; i++) {
                    lastAccessNode = lastAccessNode.prev;
                }
            }
        }
        
        if (this.readerOptimizationType == ReaderOptimizationType.OPTIMIZE_READING && updateLastAccess) {
            if (lastAccess == null) {
                this.lastAccess = new LastAccess<E>(absIndex, lastAccessNode);
            } else {
                lastAccess.index = absIndex;
                lastAccess.node = lastAccessNode;
            }
        }
        return lastAccessNode;
    }

    /**
     * Notes, this method doesn't modify {@link #modCount}
     * @param absIndex
     * @return
     */
    private NodeImpl<E> deleteAt(int absIndex, NodeImpl<E> node /* = null */) {
        if (node == null) {
            node = this.nodeAt(absIndex, false);
        }
        if (node != null) {
            LastAccess<E> lastAccess = this.lastAccess;
            if (this.readerOptimizationType == ReaderOptimizationType.OPTIMIZE_READING && lastAccess != null) {
                if (node == lastAccess.node) {
                    if (lastAccess.node.next == this.invalid) {
                        this.lastAccess = null;
                    } else {
                        lastAccess.node = node.next;
                        if (lastAccess.matchFirst) {
                            if (node.value == null ? 
                                    lastAccess.node.value != null : 
                                        !node.value.equals(lastAccess.node.value)) {
                                lastAccess.matchFirst = false;
                            }
                        }
                    }
                } else if (absIndex < lastAccess.index) {
                    lastAccess.index--;
                }
            }
            node.prev.next = node.next;
            node.next.prev = node.prev;
            this.allSize--;
            BaseEntries<E, Object> inversedEntries = this.inversedEntries();
            if (inversedEntries != null) {
                inversedEntries.removeByKey(node.value, null);
            }
        }
        return node;
    }
    
    /**
     * This method can only be called by 
     * {@link #add0(int, Object)} and {@link #add0(int, Collection)}
     * because it doesn't modify the 
     * {@link #lastAccess} and {@link #allSize}
     * @param node
     * @param value
     */
    private void addBefore(NodeImpl<E> node, E value) {
        NodeImpl<E> newNode = new NodeImpl<E>();
        newNode.prev = node.prev;
        newNode.next = node;
        newNode.value = value;
        node.prev.next = newNode;
        node.prev = newNode;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.allSize);
        int allSize = this.allSize;
        NodeImpl<E> invalid = this.invalid;
        NodeImpl<E> node = invalid.next;
        for (int i = 0; i < allSize; i++) {
            if (node == invalid) {
                throw new AssertionError();
            }
            out.writeObject(node.value);
            node = node.next;
        }
        if (node != invalid) {
            throw new AssertionError();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.allSize = in.readInt();
        int allSize = this.allSize;
        NodeImpl<E> invalid = new NodeImpl<>();
        NodeImpl<E> prevNode = invalid;
        if (allSize != 0) {
            for (int i = 0; i < allSize; i++) {
                E e = (E)in.readObject();
                NodeImpl<E> node = new NodeImpl<E>();
                prevNode.next = node;
                node.prev = prevNode;
                node.value = e;
                prevNode = node;
            }
            prevNode.next = invalid;
            invalid.prev = prevNode;
        }
        this.invalid = invalid;
        BaseEntries<E, Object> inversedEntries = this.inversedEntries();
        if (inversedEntries != null) {
            for (NodeImpl<E> node = invalid.next; node != invalid; node = node.next) {
                inversedEntries.put(node.value, null, null);
            }
        }
    }
    
    private static class NodeImpl<E> implements Node<E> {
        NodeImpl<E> prev;
        NodeImpl<E> next;
        E value;
        @Override
        public E get() {
            return this.value;
        }
        @Override
        public void set(E value) {
            this.value = value;
        }
    }
    
    private static class LastAccess<E> {
        
        int index;
        
        NodeImpl<E> node;
        
        boolean matchFirst;
        
        boolean matchLast;
        
        LastAccess(int index, NodeImpl<E> node) {
            this(index, node, false, false);
        }
        
        LastAccess(
                int index, 
                NodeImpl<E> node, 
                boolean matchFirst, 
                boolean matchLast) {
            this.index = index;
            this.node = node;
            this.matchFirst = matchFirst;
            this.matchLast = matchLast;
        }
    }

    private class BaseElementIteratorImpl extends AbstractBaseElementIteratorImpl {

        private NodeImpl<E> lastRetNode;
        
        BaseElementIteratorImpl(
                int subListHeadHide, 
                int subListTailHide,
                int index,
                BaseElementsConflictHandler conflictHandler) {
            super(subListHeadHide, subListTailHide, index, conflictHandler);
        }

        @Override
        protected E get(int absoluteIndex) {
            return this.getNode(absoluteIndex).value;
        }
        
        @Override
        public void reset() {
            this.lastRetNode = null;
        }
        
        private NodeImpl<E> getNode(int absoluteIndex) {
            NodeImpl<E> lastRetNode = this.lastRetNode;
            if (lastRetNode != null) {
                switch (absoluteIndex - this.lastReturnedIndex()) {
                case 0:
                    return lastRetNode;
                case 1:
                    return (this.lastRetNode = lastRetNode.next);
                case -1:
                    return (this.lastRetNode = lastRetNode.prev);
                }
            }
            return (this.lastRetNode = LinkedElements.this.nodeAt(absoluteIndex, true));
        }
        
    }
    
}
