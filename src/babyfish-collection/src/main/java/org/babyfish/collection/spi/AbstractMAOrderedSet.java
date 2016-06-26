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

import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.modification.OrderedSetModifications;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.collection.spi.base.OrderedBaseEntries;
import org.babyfish.collection.viewinfo.OrderedSetViewInfos;
import org.babyfish.collection.viewinfo.OrderedSetViewInfos.DescendingIterator;
import org.babyfish.data.View;
import org.babyfish.data.event.Cause;
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public abstract class AbstractMAOrderedSet<E> 
extends AbstractMASet<E> 
implements MAOrderedSet<E> {

    protected AbstractMAOrderedSet(OrderedBaseEntries<E, Object> orderedBaseEntries) {
        super(orderedBaseEntries);
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
    protected AbstractMAOrderedSet() {
        
    }
    
    @Override
    public MAIterator<E> descendingIterator() {
        return new DescendingIteratorImpl<E>(this);
    }

    @Override
    public MAOrderedSetView<E> descendingSet() {
        return new DescendingSetImpl<E>(this);
    }

    @Override
    public boolean headAppend() {
        return ((OrderedBaseEntries<E, Object>)this.baseEntries).headAppend();
    }

    @Override
    public OrderAdjustMode replaceMode() {
        return ((OrderedBaseEntries<E, Object>)this.baseEntries).replaceMode();
    }
    
    @Override
    public E first() {
        BaseEntry<E, Object> be = ((OrderedBaseEntries<E, Object>)this.baseEntries).first();
        return be == null ? null : be.getKey();
    }
    
    @Override
    public E last() {
        BaseEntry<E, Object> be = ((OrderedBaseEntries<E, Object>)this.baseEntries).last();
        return be == null ? null : be.getKey();
    }

    @Override
    public E pollFirst() {
        BaseEntry<E, Object> be = 
            ((OrderedBaseEntries<E, Object>)this.baseEntries).pollFirst(
                    this.new HandlerImpl4Set(OrderedSetModifications.<E>pollFirst()));
        return be == null ? null : be.getKey();
    }
    
    @Override
    public E pollLast() {
        BaseEntry<E, Object> be = 
            ((OrderedBaseEntries<E, Object>)this.baseEntries).pollLast(
                    this.new HandlerImpl4Set(OrderedSetModifications.<E>pollLast()));
        return be == null ? null : be.getKey();
    }

    protected static class DescendingSetImpl<E> extends AbstractMAOrderedSet<E> implements MAOrderedSetView<E> {
        
        private AbstractMAOrderedSet<E> parentSet;

        protected DescendingSetImpl(AbstractMAOrderedSet<E> parentSet) {
            super(
                    (
                            (OrderedBaseEntries<E, Object>)
                            Arguments.mustNotBeNull("parentSet", parentSet)
                            .baseEntries
                    )
                    .descendingEntries());
            this.parentSet = parentSet;
        }
        
        @Override
        protected void bubbleModifying(ElementEvent<E> e) {
            AbstractMAOrderedSet<E> parentSet = this.parentSet;
            ElementEvent<E> bubbledEvent = new ElementEvent<>(
                    parentSet, 
                    new Cause(e), 
                    null);
            parentSet.executeModifying(bubbledEvent);
        }
        
        @Override
        protected void bubbleModified(ElementEvent<E> e) {
            AbstractMAOrderedSet<E> parentSet = this.parentSet;
            ElementEvent<E> bubbledEvent = e.getBubbledEvent(parentSet);
            parentSet.executeModified(bubbledEvent);
        }
        
        @Override
        public OrderedSetViewInfos.DescendingSet viewInfo() {
            return OrderedSetViewInfos.descendingSet(); 
        }
    }
    
    protected static class DescendingIteratorImpl<E> extends AbstractMASet.AbstractIteratorImpl<E> {

        protected DescendingIteratorImpl(AbstractMAOrderedSet<E> parentSet) {
            super(parentSet, true);
        }

        @Override
        public DescendingIterator viewInfo() {
            return OrderedSetViewInfos.descendingIterator();
        }
        
    }

}
