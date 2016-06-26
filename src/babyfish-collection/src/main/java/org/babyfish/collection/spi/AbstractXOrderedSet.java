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

import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.collection.spi.base.OrderedBaseEntries;
import org.babyfish.collection.viewinfo.OrderedSetViewInfos;
import org.babyfish.collection.viewinfo.OrderedSetViewInfos.DescendingSet;
import org.babyfish.data.View;

/**
 * @author Tao Chen
 */
public abstract class AbstractXOrderedSet<E> extends AbstractXSet<E> implements XOrderedSet<E> {

    protected AbstractXOrderedSet(OrderedBaseEntries<E, Object> baseEntries) {
        super(baseEntries);
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
    protected AbstractXOrderedSet() {
        
    }

    @Override
    public XIterator<E> descendingIterator() {
        return this.descendingSet().iterator();
    }
    
    @Override
    public XOrderedSetView<E> descendingSet() {
        return new DescendingSetImpl<E>(this.<OrderedBaseEntries<E, Object>>getBaseEntries().descendingEntries());
    }

    @Override
    public E first() {
        BaseEntry<E, Object> be = this.<OrderedBaseEntries<E, Object>>getBaseEntries().first();
        return be == null ? null : be.getKey();
    }

    @Override
    public boolean headAppend() {
        return this.<OrderedBaseEntries<E, Object>>getBaseEntries().headAppend();
    }

    @Override
    public E last() {
        BaseEntry<E, Object> be = this.<OrderedBaseEntries<E, Object>>getBaseEntries().last();
        return be == null ? null : be.getKey();
    }

    @Override
    public E pollFirst() {
        BaseEntry<E, Object> be = this.<OrderedBaseEntries<E, Object>>getBaseEntries().pollFirst(null);
        return be == null ? null : be.getKey();
    }

    @Override
    public E pollLast() {
        BaseEntry<E, Object> be = this.<OrderedBaseEntries<E, Object>>getBaseEntries().pollLast(null);
        return be == null ? null : be.getKey();
    }

    @Override
    public OrderAdjustMode replaceMode() {
        return this.<OrderedBaseEntries<E, Object>>getBaseEntries().replaceMode();
    }
    
    protected static class DescendingSetImpl<E> extends AbstractXOrderedSet<E> implements XOrderedSetView<E> {

        protected DescendingSetImpl(OrderedBaseEntries<E, Object> baseEntries) {
            super(baseEntries);
        }

        @Override
        public DescendingSet viewInfo() {
            return OrderedSetViewInfos.descendingSet();
        }
        
    }

}
