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

import java.util.Comparator;

import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.collection.spi.base.NavigableBaseEntries;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos;
import org.babyfish.data.View;

/**
 * @author Tao Chen
 */
public class AbstractXNavigableSet<E> extends AbstractXSet<E> implements XNavigableSet<E> {

    protected AbstractXNavigableSet(NavigableBaseEntries<E, Object> baseEntries) {
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
    protected AbstractXNavigableSet() {
        
    }
    
    @Override
    public Comparator<? super E> comparator() {
        return this.<NavigableBaseEntries<E, Object>>getBaseEntries().comparator();
    }
    
    @Override
    public XIterator<E> descendingIterator() {
        return this.descendingSet().iterator();
    }

    @Override
    public XNavigableSetView<E> descendingSet() {
        return new DescendingSetImpl<E>(this);
    }

    @Override
    public XNavigableSetView<E> headSet(E toElement) {
        return this.headSet(toElement, false);
    }

    @Override
    public XNavigableSetView<E> headSet(E toElement, boolean inclusive) {
        return new HeadSetImpl<E>(this, toElement, inclusive);
    }

    @Override
    public XNavigableSetView<E> tailSet(E fromElement) {
        return this.tailSet(fromElement, true);
    }

    @Override
    public XNavigableSetView<E> tailSet(E fromElement, boolean inclusive) {
        return new TailSetImpl<E>(this, fromElement, inclusive);
    }

    @Override
    public XNavigableSetView<E> subSet(E fromElement, E toElement) {
        return this.subSet(fromElement, true, toElement, false);
    }

    @Override
    public XNavigableSetView<E> subSet(
            E fromElement, boolean fromInclusive,
            E toElement, boolean toInclusive) {
        return new SubSetImpl<E>(this, fromElement, fromInclusive, toElement, toInclusive);
    }

    @Override
    public E first() {
        BaseEntry<E, Object> be = this.<NavigableBaseEntries<E, Object>>getBaseEntries().first();
        return be == null ? null : be.getKey();
    }

    @Override
    public E last() {
        BaseEntry<E, Object> be = this.<NavigableBaseEntries<E, Object>>getBaseEntries().last();
        return be == null ? null : be.getKey();
    }

    @Override
    public E floor(E e) {
        BaseEntry<E, Object> be = this.<NavigableBaseEntries<E, Object>>getBaseEntries().floor(e);
        return be == null ? null : be.getKey();
    }

    @Override
    public E ceiling(E e) {
        BaseEntry<E, Object> be = this.<NavigableBaseEntries<E, Object>>getBaseEntries().ceiling(e);
        return be == null ? null : be.getKey();
    }

    @Override
    public E lower(E e) {
        BaseEntry<E, Object> be = this.<NavigableBaseEntries<E, Object>>getBaseEntries().lower(e);
        return be == null ? null : be.getKey();
    }

    @Override
    public E higher(E e) {
        BaseEntry<E, Object> be = this.<NavigableBaseEntries<E, Object>>getBaseEntries().higher(e);
        return be == null ? null : be.getKey();
    }

    @Override
    public E pollFirst() {
        BaseEntry<E, Object> be = this.<NavigableBaseEntries<E, Object>>getBaseEntries().pollFirst(null);
        return be == null ? null : be.getKey();
    }

    @Override
    public E pollLast() {
        BaseEntry<E, Object> be = this.<NavigableBaseEntries<E, Object>>getBaseEntries().pollLast(null);
        return be == null ? null : be.getKey();
    }

    protected static abstract class AbstractSubSetImpl<E> extends AbstractXNavigableSet<E> implements XNavigableSetView<E> {

        AbstractSubSetImpl(NavigableBaseEntries<E, Object> baseEntries) {
            super(baseEntries);
        }
    }
    
    protected static class HeadSetImpl<E> extends AbstractSubSetImpl<E> {
        
        private NavigableSetViewInfos.HeadSet viewInfo;

        protected HeadSetImpl(AbstractXNavigableSet<E> parent, E toElement, boolean inclusive) {
            super(
                    ((NavigableBaseEntries<E, Object>)parent.baseEntries)
                    .subEntries(
                            false, null, false, 
                            true, toElement, inclusive));
            this.viewInfo = NavigableSetViewInfos.headSet(toElement, inclusive);
        }

        @Override
        public NavigableSetViewInfos.HeadSet viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class TailSetImpl<E> extends AbstractSubSetImpl<E> {
        
        private NavigableSetViewInfos.TailSet viewInfo;

        protected TailSetImpl(AbstractXNavigableSet<E> parent, E fromElement, boolean inclusive) {
            super(
                    ((NavigableBaseEntries<E, Object>)parent.baseEntries)
                    .subEntries(
                            true, fromElement, inclusive, 
                            false, null, false));
            this.viewInfo = NavigableSetViewInfos.tailSet(fromElement, inclusive);
        }

        @Override
        public NavigableSetViewInfos.TailSet viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class SubSetImpl<E> extends AbstractSubSetImpl<E> {
        
        private NavigableSetViewInfos.SubSet viewInfo;

        protected SubSetImpl(
                AbstractXNavigableSet<E> parent,
                E fromElement, boolean fromInclusive,
                E toElement, boolean toInclusive) {
            super(
                    ((NavigableBaseEntries<E, Object>)parent.baseEntries)
                    .subEntries(
                            true, fromElement, fromInclusive, 
                            true, toElement, toInclusive));
            this.viewInfo = NavigableSetViewInfos.subSet(
                    fromElement, fromInclusive, toElement, toInclusive);
        }

        @Override
        public NavigableSetViewInfos.SubSet viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class DescendingSetImpl<E> extends AbstractSubSetImpl<E> {
        
        protected DescendingSetImpl(AbstractXNavigableSet<E> parent) {
            super(((NavigableBaseEntries<E, Object>)parent.baseEntries).descendingEntries());
        }

        @Override
        public NavigableSetViewInfos.DescendingSet viewInfo() {
            return NavigableSetViewInfos.descendingSet();
        }
        
    }

}
