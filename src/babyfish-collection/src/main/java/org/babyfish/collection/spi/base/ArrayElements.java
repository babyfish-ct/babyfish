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
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.spi.base.AbstractBaseElementsImpl.Trigger.History;
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public class ArrayElements<E> extends AbstractBaseElementsImpl<E> {
    
    private static final long serialVersionUID = 8609240554423087388L;

    private Object[] data;
    
    private int allSize;
    
    private float expandFactor;
    
    private Float collapseFactor;
    
    private int modCount;
    
    public ArrayElements(
            BidiType bidiType,
            Object comparatorOrEqualityComparatorOrUnifiedComparator,
            int initCapacity, 
            float expandFactor, 
            Float collapseFactor) {
        super(bidiType, comparatorOrEqualityComparatorOrUnifiedComparator);
        Arguments.mustBeGreaterThanOrEqualToValue("initCapacity", initCapacity, 0);
        Arguments.mustBetweenValue("expandFactory", expandFactor, 1, true, 4, true);
        if (collapseFactor != null) {
            float factor = collapseFactor.floatValue();
            Arguments.mustBetweenValue("expandFactory", factor, 0.1F, true, 0.9F, true);
        }
        if (initCapacity != 0) {
            this.data = new Object[initCapacity];
        }
    }

    @Override
    public boolean isReadWriteLockSupported() {
        return true;
    }

    @Override
    public boolean randomAccess() {
        return true;
    }

    @Override
    public int allSize() {
        return this.allSize;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected int firstIndex(int subListHeadHide, int subListTailHide, Object o) {
        this.checkSubListRange(subListHeadHide, subListTailHide);
        Object[] data = this.data;
        int fenceIndex = this.allSize - subListTailHide;
        if (o == null) {
            for (int i = subListHeadHide; i < fenceIndex; i++) {
                if (data[i] == null) {
                    return i - subListHeadHide;
                }
            }
        } else {
            UnifiedComparator<? super E> unifiedComparator = this.unifiedComparator();
            for (int i = subListHeadHide; i < fenceIndex; i++) {
                E ei = (E)data[i];
                if (unifiedComparator.equals((E)o, ei)) {
                    return i - subListHeadHide;
                }
            }
        }
        return -1;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected int lastIndex(int subListHeadHide, int subListTailHide, Object o) {
        this.checkSubListRange(subListHeadHide, subListTailHide);
        Object[] data = this.data;
        if (o == null) {
            for (int i = this.allSize - subListTailHide - 1; i >= subListHeadHide; i--) {
                if (data[i] == null) {
                    return i - subListHeadHide;
                }
            }
        } else {
            UnifiedComparator<? super E> unifiedComparator = this.unifiedComparator();
            for (int i = this.allSize - subListTailHide - 1; i >= subListHeadHide; i--) {
                E ei = (E)data[i];
                if (unifiedComparator.equals((E)o, ei)) {
                    return i - subListHeadHide;
                }
            }
        }
        return -1;
    }
    
    @SuppressWarnings("unchecked")
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
                true);
        return (E)this.data[subListHeadHide + index];
    }
    
    @Override
    protected void addImpl(E element, AttachProcessor<E> attachProcessor) {
        
        Trigger<E> trigger = attachProcessor.getTrigger();
        if (trigger != null) {
            trigger.preAdd(attachProcessor.getActualIndex(false), element);
        }
        if (attachProcessor.beginExecute()) {
            try {
                int absoluteIndex = attachProcessor.getActualIndex(true);
                int newCapacity = this.expandCapacity(this.allSize + 1);
                if (newCapacity != -1) {
                    Object[] newData = new Object[newCapacity];
                    if (this.data != null) {
                        System.arraycopy(this.data, 0, newData, 0, absoluteIndex);
                        System.arraycopy(this.data, absoluteIndex, newData, absoluteIndex + 1, this.allSize - absoluteIndex);
                    }
                    this.data = newData;
                } else {
                    if (this.data != null) {
                        System.arraycopy(this.data, absoluteIndex, this.data, absoluteIndex + 1, this.allSize - absoluteIndex);
                    }
                }
                this.data[absoluteIndex] = element;
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

    @SuppressWarnings("unchecked")
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
                int absoluteIndex = attachProcessor.getActualIndex(true);
                Object[] appendData = c.toArray();
                int newCapacity = this.expandCapacity(this.allSize + appendData.length);
                if (newCapacity != -1) {
                    Object[] newData = new Object[newCapacity];
                    if (this.data != null) {
                        System.arraycopy(this.data, 0, newData, 0, absoluteIndex);
                        System.arraycopy(
                                this.data, 
                                absoluteIndex, newData, 
                                absoluteIndex + appendData.length, 
                                this.allSize - absoluteIndex);
                    }
                    this.data = newData;
                } else if (appendData.length != 0) {
                    if (this.data != null) {
                        System.arraycopy(
                                this.data, 
                                absoluteIndex, 
                                this.data, 
                                absoluteIndex + appendData.length, 
                                this.allSize - absoluteIndex);
                    }
                }
                System.arraycopy(appendData, 0, this.data, absoluteIndex, appendData.length);
                BaseEntries<E, Object> inversedEntries = this.inversedEntries();
                if (inversedEntries != null) {
                    for (int i = appendData.length - 1; i >= 0; i--) {
                        inversedEntries.put((E)appendData[i], null, null);
                    }
                }
                this.allSize += appendData.length;
                this.modCount++;
                attachProcessor.endExecute(null);
            } catch (RuntimeException | Error ex) {
                attachProcessor.endExecute(ex);
            }
        }
        attachProcessor.flush();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected E setImpl(E element, AttachProcessor<E> attachProcessor) {
        Trigger<E> trigger = attachProcessor.getTrigger();
        if (trigger != null) {
            trigger.preChange(
                    attachProcessor.getExpectedIndex(false), 
                    attachProcessor.getActualIndex(false), 
                    this.get(0, 0, attachProcessor.getExpectedIndex(true)), 
                    element);
        }
        E retval = null;
        if (attachProcessor.beginExecute()) {
            try {
                int absoluteIndex = attachProcessor.getActualIndex(true);
                Object[] data = this.data;
                retval = (E)data[absoluteIndex];
                data[absoluteIndex] = element;
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
    
    @SuppressWarnings("unchecked")
    @Override
    protected void clear(
            int subListHeadHide, 
            int subListTailHide, 
            Trigger<E> trigger) {
        this.checkSubListRange(subListHeadHide, subListTailHide);
        int subListSize = this.allSize - subListHeadHide - subListTailHide;
        if (subListSize == 0) {
            return;
        }
        if (trigger != null) {
            for (int i = 0; i < subListSize; i++) {
                trigger.preRemove(i, (E)this.data[subListHeadHide + i]);
            }
        }
        if (trigger == null || trigger.beginExecute()) {
            try {
                BaseEntries<E, Object> inversedEntries = this.inversedEntries();
                if (this.allSize == subListSize) {
                    this.data = null;
                    if (inversedEntries != null) {
                        inversedEntries.clear(null);
                    }
                } else {
                    assert this.data != null;
                    Object[] oldData = this.data;
                    if (inversedEntries != null) {
                        for (int i = this.allSize - subListTailHide - 1; i >= subListHeadHide; i--) {
                            inversedEntries.removeByKey(oldData[i], null);
                        }
                    }
                    int newCapacity = this.collapseCapacity(this.allSize - subListSize);
                    if (newCapacity != -1) {
                        Object[] newData = new Object[newCapacity];
                        System.arraycopy(oldData, 0, newData, 0, subListHeadHide);
                        System.arraycopy(oldData, subListHeadHide + subListSize, newData, subListHeadHide, subListTailHide);
                        this.data = newData;
                    } else {
                        System.arraycopy(oldData, subListHeadHide + subListSize, oldData, subListHeadHide, subListTailHide);
                        
                        //Let JVM GC the unnecessary objects
                        int fenceIndex = this.allSize;
                        for (int i = fenceIndex - subListSize; i < fenceIndex; i++) {
                            oldData[i] = null;
                        }
                    }
                }
                this.allSize -= subListSize;
                this.modCount++;
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
    
    @SuppressWarnings("unchecked")
    @Override
    protected E removeAt(
            int subListHeadHide, 
            int subListTailHide, 
            int index, 
            Trigger<E> trigger) {
        this.checkSubListRange(subListHeadHide, subListTailHide);
        int allSize = this.allSize;
        int subListSize = allSize - subListHeadHide - subListTailHide;
        Arguments.indexMustBetweenOther(
                "index", 
                index, 
                "0", 
                0, 
                true, 
                "allSize() - subListHeadHide - subListTailHide", 
                subListSize, 
                false);
        int absIndex = subListHeadHide + index;
        if (trigger != null) {
            trigger.preRemove(index, (E)this.data[absIndex]);
        }
        E retval = null;
        if (trigger == null || trigger.beginExecute()) {
            try {
                assert this.data != null;
                if (allSize == 1) {
                    retval = (E)this.data[0];
                    this.data = null;
                } else {
                    int newCapacity = this.collapseCapacity(this.allSize - 1);
                    Object[] oldData = this.data;
                    retval = (E)this.data[absIndex];
                    if (newCapacity != -1) {
                        Object[] newData = new Object[newCapacity];
                        System.arraycopy(oldData, 0, newData, 0, absIndex);
                        System.arraycopy(oldData, absIndex + 1, newData, absIndex, allSize - absIndex - 1);
                        this.data = newData;
                    } else {
                        System.arraycopy(oldData, absIndex + 1, oldData, absIndex, allSize - absIndex - 1);
                        
                        //Let JVM free the unnecessary objects
                        oldData[allSize - 1] = null;
                    }
                }
                BaseEntries<E, Object> inversedEntries = this.inversedEntries();
                if (inversedEntries != null) {
                    inversedEntries.removeByKey(retval, null);
                }
                this.allSize--;
                this.modCount++;
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
        return retval;
    }
    
    @Override
    protected boolean remove(
            int subListHeadHide,
            int subListTailHide,
            Object o,
            Trigger<E> trigger) {
        
        //Need to call this.checkSubListRange because this.indexOf will call it
        int index = this.indexOf(subListHeadHide, subListTailHide, o);
        if (index != -1) {
            this.removeAt(subListHeadHide, subListTailHide, index, trigger);
            return true;
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected boolean removeAll(
            int subListHeadHide,
            int subListTailHide,
            Collection<?> c,
            Trigger<E> trigger) {
        this.checkSubListRange(subListHeadHide, subListTailHide);
        Object[] data = this.data;
        int fenceIndex = this.allSize - subListTailHide;
        OverriddenContainsBehavior ocb = OverriddenContainsBehavior.of(
                (Collection<? extends E>)c, this.unifiedComparator());
        if (trigger == null) {
            int deleteCount = 0;
            for (int absIndex = subListHeadHide; absIndex < fenceIndex; absIndex++) {
                Object e = data[absIndex - deleteCount];
                if (ocb.contains(e)) {
                    this.removeAt(
                            subListHeadHide, 
                            subListTailHide, 
                            absIndex - subListHeadHide - deleteCount++, 
                            (Trigger<E>)null);
                }
            }
            // Need not to modify the size and modCount because the remove has modified them
            return deleteCount != 0;
        }
        
        for (int absIndex = subListHeadHide; absIndex < fenceIndex; absIndex++) {
            Object e = data[absIndex];
            if (ocb.contains(e)) {
                trigger.preRemove(absIndex - subListHeadHide, (E)data[absIndex]);
            }
        }
        if (trigger.beginExecute()) {
            try {
                History<E> history = trigger.getHistory(0);
                for (int i = 0; i < trigger.getLength(); i++) {
                    int removeAt = history.getOldIndex(i);
                    this.removeAt(subListHeadHide, subListTailHide, removeAt - i, (Trigger<E>)null);
                }
                trigger.endExecute(null);
            } catch (RuntimeException | Error ex) {
                trigger.endExecute(ex);
            }
        }
        trigger.flush();
        return trigger.getLength() != 0;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected boolean retainAll(
            int subListHeadHide,
            int subListTailHide,
            Collection<?> c,
            Trigger<E> trigger) {
        this.checkSubListRange(subListHeadHide, subListTailHide);
        Object[] data = this.data;
        int fenceIndex = this.allSize - subListTailHide;
        OverriddenContainsBehavior ocb = OverriddenContainsBehavior.of(
                (Collection<? extends E>)c, this.unifiedComparator());
        if (trigger == null) {
            int deleteCount = 0;
            for (int absIndex = subListHeadHide; absIndex < fenceIndex; absIndex++) {
                Object e = data[absIndex - deleteCount];
                if (!ocb.contains(e)) {
                    this.removeAt(
                            subListHeadHide, 
                            subListTailHide, 
                            absIndex - subListHeadHide - deleteCount++, 
                            (Trigger<E>)null);
                }
            }
            // Need not to modify the size and modCount because the remove has modified them
            return deleteCount != 0;
        }
        
        for (int absIndex = subListHeadHide; absIndex < fenceIndex; absIndex++) {
            Object e = data[absIndex];
            if (!ocb.contains(e)) {
                trigger.preRemove(absIndex - subListHeadHide, (E)data[absIndex]);
            }
        }
        if (trigger.beginExecute()) {
            try {
                History<E> history = trigger.getHistory(0);
                for (int i = 0; i < trigger.getLength(); i++) {
                    int removeAt = history.getOldIndex(i);
                    this.removeAt(subListHeadHide, subListTailHide, removeAt - i, (Trigger<E>)null);
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
            BaseElementsConflictHandler rangeChangeHandler) {
        return this.new BaseElementIteratorImpl(subListHeadHide, subListTailHide, index, rangeChangeHandler);
    }
    
    @Override
    public int modCount() {
        return this.modCount;
    }
    
    private int expandCapacity(int newSize) {
        int oldCapacity = this.data == null ? 0 : this.data.length;
        if (oldCapacity < newSize) {
            int newCapacity = (int)(oldCapacity * this.expandFactor);
            if (newCapacity < newSize) {
                newCapacity = newSize;
            }
            return newCapacity;
        }
        return -1;
    }

    private int collapseCapacity(int newSize) {
        if (this.collapseFactor == null) {
            return -1;
        }
        float factor = this.collapseFactor.floatValue();
        int oldCapacity = this.data == null ? 0 : this.data.length;
        if (oldCapacity > newSize) {
            int newCapacity = (int)(oldCapacity * factor);
            if (newCapacity >= newSize) {
                return newCapacity < oldCapacity ? newCapacity : -1;
            }
        }
        return -1;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.allSize);
        out.writeFloat(this.expandFactor);
        out.writeObject(this.collapseFactor);
        Object[] data = this.data;
        out.writeInt(data != null ? data.length : -1);
        if (data != null) {
            int allSize = this.allSize;
            for (int i = 0; i < allSize; i++) {
                out.writeObject(data[i]);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.allSize = in.readInt();
        this.expandFactor = in.readFloat();
        this.collapseFactor = (Float)in.readObject();
        int capacity = in.readInt();
        if (capacity == -1) {
            this.data = null;
        } else {
            Object[] data = new Object[capacity];
            int allSize = this.allSize;
            for (int i = 0; i < allSize; i++) {
                data[i] = in.readObject();
            }
            this.data = data;
            BaseEntries<E, Object> inversedEntries = this.inversedEntries();
            if (inversedEntries != null) {
                for (int i = 0; i < allSize; i++) {
                    inversedEntries.put((E)data[i], null, null);
                }
            }
        }
    }

    private class BaseElementIteratorImpl extends AbstractBaseElementIteratorImpl {

        BaseElementIteratorImpl(
                int subListHeadHide, 
                int subListTailHide,
                int index,
                BaseElementsConflictHandler conflictHandler) {
            super(subListHeadHide, subListTailHide, index, conflictHandler);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected E get(int absoluteIndex) {
            return (E)ArrayElements.this.data[absoluteIndex];
        }
        
        @Override
        public void reset() {
            
        }
        
    }
    
}
