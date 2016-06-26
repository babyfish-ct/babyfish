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

import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.MASet;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.viewinfo.OrderedSetViewInfos;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;

/**
 * @author Tao Chen
 */
public class AbstractWrapperMAOrderedSet<E> extends AbstractWrapperMASet<E> implements MAOrderedSet<E> {

    protected AbstractWrapperMAOrderedSet(MAOrderedSet<E> base) {
        super(base);
    }

    protected AbstractWrapperMAOrderedSet(
            AbstractWrapperMAOrderedSet<E> parent, ViewInfo viewInfo) {
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
    protected AbstractWrapperMAOrderedSet() {
        
    }

    @Override
    public boolean headAppend() {
        return this.<MAOrderedSet<E>>getBase().headAppend();
    }

    @Override
    public OrderAdjustMode replaceMode() {
        return this.<MAOrderedSet<E>>getBase().replaceMode();
    }

    @Override
    public E first() {
        this.requiredEnabled();
        return this.<MAOrderedSet<E>>getBase().first();
    }

    @Override
    public E last() {
        this.requiredEnabled();
        return this.<MAOrderedSet<E>>getBase().last();
    }

    @Override
    public E pollFirst() {
        this.enable();
        return this.<MAOrderedSet<E>>getBase().pollFirst();
    }

    @Override
    public E pollLast() {
        this.enable();
        return this.<MAOrderedSet<E>>getBase().pollLast();
    }

    @Override
    public MAOrderedSetView<E> descendingSet() {
        return new DescendingSetImpl<E>(this);
    }

    @Override
    public MAIterator<E> descendingIterator() {
        return new DescendingIteratorImpl<E>(this);
    }
    
    @Override
    protected RootData<E> createRootData() {
        return new RootData<E>();
    }

    @Deprecated
    @Override
    protected final MASet<E> createBaseView(
            MASet<E> parentBase, ViewInfo viewInfo) {
        return this.createBaseView((MAOrderedSet<E>)parentBase, viewInfo);
    }
    
    protected MAOrderedSet<E> createBaseView(
            MAOrderedSet<E> parentBase, ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }
    
    protected static class DescendingSetImpl<E> extends AbstractWrapperMAOrderedSet<E> implements MAOrderedSetView<E> {

        protected DescendingSetImpl(AbstractWrapperMAOrderedSet<E> parent) {
            super(parent, OrderedSetViewInfos.descendingSet());
        }

        @Override
        protected MAOrderedSet<E> createBaseView(MAOrderedSet<E> parentBase, ViewInfo viewInfo) {
            if (viewInfo instanceof OrderedSetViewInfos.DescendingSet) {
                return parentBase.descendingSet();
            }
            return super.createBaseView(parentBase, viewInfo);
        }

        @Override
        public ViewInfo viewInfo() {
            return this.<XOrderedSetView<E>>getBase().viewInfo();
        }
        
    }
    
    protected static abstract class AbstractIteratorImpl<E> extends AbstractWrapperMASet.AbstractIteratorImpl<E> {

        public AbstractIteratorImpl(
                AbstractWrapperMAOrderedSet<E> owner, ViewInfo viewInfo) {
            super(owner, viewInfo);
        }

        @Deprecated
        @Override
        protected final MAIterator<E> createBaseView(
                MASet<E> baseParent, ViewInfo viewInfo) {
            return this.createBaseView((MAOrderedSet<E>)baseParent, viewInfo);
        }
        
        protected abstract MAIterator<E> createBaseView(
                MAOrderedSet<E> baseParent, ViewInfo viewInfo);
        
    }
    
    protected static class DescendingIteratorImpl<E> extends AbstractIteratorImpl<E> {

        public DescendingIteratorImpl(AbstractWrapperMAOrderedSet<E> owner) {
            super(owner, OrderedSetViewInfos.descendingSet());
        }

        @Override
        protected MAIterator<E> createBaseView(
                MAOrderedSet<E> baseParent, ViewInfo viewInfo) {
            if (viewInfo instanceof OrderedSetViewInfos.DescendingSet) {
                return baseParent.descendingIterator();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
    }

    protected static class RootData<E> extends AbstractWrapperMASet.RootData<E> {

        private static final long serialVersionUID = -4364742452819154825L;
        
        public RootData() {
            
        }

        @Deprecated
        @Override
        protected final void setBase(MASet<E> base) {
            this.setBase((MAOrderedSet<E>)base);
        }
        
        protected void setBase(MAOrderedSet<E> base) {
            super.setBase(base);
        }

        @Override
        protected MAOrderedSet<E> createDefaultBase(
                UnifiedComparator<? super E> unifiedComparator) {
            return new MALinkedHashSet<E>(unifiedComparator.equalityComparator(true));
        }
    }

}
