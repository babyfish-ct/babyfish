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

import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.collection.XSet;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.collection.viewinfo.OrderedSetViewInfos;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;

/**
 * @author Tao Chen
 */
public class AbstractWrapperXOrderedSet<E> extends AbstractWrapperXSet<E> implements XOrderedSet<E> {

    protected AbstractWrapperXOrderedSet(XOrderedSet<E> base) {
        super(base);
    }

    protected AbstractWrapperXOrderedSet(
            AbstractWrapperXOrderedSet<E> parent,
            ViewInfo viewInfo) {
        super(parent, viewInfo);
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
    protected AbstractWrapperXOrderedSet() {
        
    }

    @Override
    public boolean headAppend() {
        return this.<XOrderedSet<E>>getBase().headAppend();
    }

    @Override
    public OrderAdjustMode replaceMode() {
        return this.<XOrderedSet<E>>getBase().replaceMode();
    }

    @Override
    public XOrderedSetView<E> descendingSet() {
        return new DescendingSetImpl<E>(this);
    }

    @Override
    public XIterator<E> descendingIterator() {
        return new DescendingIteratorImpl<E>(this);
    }

    @Override
    public E first() {
        this.requiredEnabled();
        return this.<XOrderedSet<E>>getBase().first();
    }

    @Override
    public E last() {
        this.requiredEnabled();
        return this.<XOrderedSet<E>>getBase().last();
    }

    @Override
    public E pollFirst() {
        this.enable();
        return this.<XOrderedSet<E>>getBase().pollFirst();
    }

    @Override
    public E pollLast() {
        this.enable();
        return this.<XOrderedSet<E>>getBase().pollLast();
    }

    @Override
    protected RootData<E> createRootData() {
        return new RootData<E>();
    }
    
    @Deprecated
    @Override
    protected final XSet<E> createBaseView(XSet<E> parentBase, ViewInfo viewInfo) {
        return this.createBaseView((XOrderedSet<E>)parentBase, viewInfo);
    }
    
    protected XOrderedSet<E> createBaseView(
            XOrderedSet<E> parentBase, ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }

    protected static class DescendingSetImpl<E> 
    extends AbstractWrapperXOrderedSet<E> 
    implements XOrderedSetView<E> {

        public DescendingSetImpl(AbstractWrapperXOrderedSet<E> parent) {
            super(parent, OrderedSetViewInfos.descendingSet());
        }

        @Override
        protected XOrderedSet<E> createBaseView(XOrderedSet<E> parentBase, ViewInfo viewInfo) {
            if (viewInfo instanceof OrderedSetViewInfos.DescendingSet) {
                return parentBase.descendingSet();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }

        @Override
        public ViewInfo viewInfo() {
            return this.<XOrderedSetView<E>>getBase().viewInfo();
        }
        
    }
    
    protected static abstract class AbstractIteratorImpl<E> extends AbstractWrapperXSet.AbstractIteratorImpl<E> {

        protected AbstractIteratorImpl(
                AbstractWrapperXOrderedSet<E> parent, ViewInfo viewInfo) {
            super(parent, viewInfo);
        }

        @Deprecated
        @Override
        protected final XIterator<E> createBaseView(
                XCollection<E> baseParent, ViewInfo viewInfo) {
            return this.createBaseView((XOrderedSet<E>)baseParent, viewInfo);
        }
        
        protected abstract XIterator<E> createBaseView(
                XOrderedSet<E> baseParent, ViewInfo viewInfo);
    }
    
    protected static class DescendingIteratorImpl<E> extends AbstractIteratorImpl<E> {

        protected DescendingIteratorImpl(AbstractWrapperXOrderedSet<E> parent) {
            super(parent, CollectionViewInfos.iterator());
        }

        @Override
        protected XIterator<E> createBaseView(
                XOrderedSet<E> baseParent, ViewInfo viewInfo) {
            if (viewInfo instanceof OrderedSetViewInfos.DescendingIterator) {
                return baseParent.descendingIterator();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
    }
    
    protected static class RootData<E> extends AbstractWrapperXSet.RootData<E> {

        private static final long serialVersionUID = -2975418346993417391L;
        
        public RootData() {
            
        }

        @Deprecated
        @Override
        protected final void setBase(XSet<E> base) {
            this.setBase((XOrderedSet<E>)base);
        }
        
        protected void setBase(XOrderedSet<E> base) {
            super.setBase(base);
        }

        @Override
        protected XOrderedSet<E> createDefaultBase(
                UnifiedComparator<? super E> unifiedComparator) {
            return new LinkedHashSet<E>(unifiedComparator.equalityComparator(true));
        }
        
    }
}
