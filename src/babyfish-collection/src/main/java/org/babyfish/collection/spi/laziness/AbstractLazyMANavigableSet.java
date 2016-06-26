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
package org.babyfish.collection.spi.laziness;

import java.util.Comparator;

import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.MASet;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos;
import org.babyfish.collection.viewinfo.SortedSetViewInfos;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;

/**
 * @author Tao Chen
 */
public abstract class AbstractLazyMANavigableSet<E> extends AbstractLazyMASet<E> implements MANavigableSet<E> {
    
    protected AbstractLazyMANavigableSet(MANavigableSet<E> base) {
        super(base);
    }
    
    protected AbstractLazyMANavigableSet(
            AbstractLazyMANavigableSet<E> parent, ViewInfo viewInfo) {
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
    protected AbstractLazyMANavigableSet() {
        
    }

    @Override
    public Comparator<? super E> comparator() {
        return this.<MANavigableSet<E>>getBase().comparator();
    }

    @Override
    public E first() {
        this.requiredEnabled();
        return this.<MANavigableSet<E>>getBase().first();
    }

    @Override
    public E last() {
        this.requiredEnabled();
        return this.<MANavigableSet<E>>getBase().last();
    }

    @Override
    public E floor(E e) {
        this.requiredEnabled();
        return this.<MANavigableSet<E>>getBase().floor(e);
    }

    @Override
    public E ceiling(E e) {
        this.requiredEnabled();
        return this.<MANavigableSet<E>>getBase().ceiling(e);
    }

    @Override
    public E lower(E e) {
        this.requiredEnabled();
        return this.<MANavigableSet<E>>getBase().lower(e);
    }

    @Override
    public E higher(E e) {
        this.requiredEnabled();
        return this.<MANavigableSet<E>>getBase().higher(e);
    }

    @Override
    public E pollFirst() {
        this.enable();
        return this.<MANavigableSet<E>>getBase().pollFirst();
    }

    @Override
    public E pollLast() {
        this.enable();
        return this.<MANavigableSet<E>>getBase().pollLast();
    }

    @Override
    public MAIterator<E> descendingIterator() {
        return new DescendingIteratorImpl<E>(this);
    }

    @Override
    public MANavigableSetView<E> descendingSet() {
        return new DescendingSetImpl<E>(this);
    }

    @Override
    public MANavigableSetView<E> headSet(E toElement) {
        return this.headSet(toElement, false);
    }

    @Override
    public MANavigableSetView<E> headSet(E toElement, boolean inclusive) {
        return new HeadSetImpl<E>(this, toElement, inclusive);
    }

    @Override
    public MANavigableSetView<E> tailSet(E fromElement) {
        return this.tailSet(fromElement, true);
    }

    @Override
    public MANavigableSetView<E> tailSet(E fromElement, boolean inclusive) {
        return new TailSetImpl<E>(this, fromElement, inclusive);
    }

    @Override
    public MANavigableSetView<E> subSet(E fromElement, E toElement) {
        return this.subSet(
                fromElement, 
                true, 
                toElement, 
                false);
    }

    @Override
    public MANavigableSetView<E> subSet(
            E fromElement, boolean fromInclusive,
            E toElement, boolean toInclusive) {
        return new SubSetImpl<E>(this, fromElement, fromInclusive, toElement, toInclusive);
    }

    @Override
    protected abstract RootData<E> createRootData();

    @Deprecated
    @Override
    protected final MASet<E> createBaseView(MASet<E> parentBase, ViewInfo viewInfo) {
        return this.createBaseView((MANavigableSet<E>)parentBase, viewInfo);
    }
    
    protected MANavigableSet<E> createBaseView(
            MANavigableSet<E> parentBase, ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }
    
    protected static abstract class AbstractSubSetImpl<E> 
    extends AbstractLazyMANavigableSet<E> 
    implements MANavigableSetView<E> {

        protected AbstractSubSetImpl(
                AbstractLazyMANavigableSet<E> parent,
                ViewInfo viewInfo) {
            super(parent, viewInfo);
        }

        @Override
        public ViewInfo viewInfo() {
            return this.<MANavigableSetView<E>>getBase().viewInfo();
        }

        @Deprecated
        @Override
        protected final RootData<E> createRootData() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    protected static class HeadSetImpl<E> extends AbstractSubSetImpl<E> {

        public HeadSetImpl(
                AbstractLazyMANavigableSet<E> parent,
                E toElement,
                boolean inclusive) {
            super(parent, NavigableSetViewInfos.headSet(toElement, inclusive));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected MANavigableSet<E> createBaseView(
                MANavigableSet<E> parentBase, ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableSetViewInfos.HeadSet) {
                NavigableSetViewInfos.HeadSet headSetViewInfo =
                        (NavigableSetViewInfos.HeadSet)viewInfo;
                return parentBase.headSet((E)headSetViewInfo.getToElement(), headSetViewInfo.isInclusive());
            }
            if (viewInfo instanceof SortedSetViewInfos.HeadSet) {
                SortedSetViewInfos.HeadSet headSetViewInfo =
                        (SortedSetViewInfos.HeadSet)viewInfo;
                return parentBase.headSet((E)headSetViewInfo.getToElement());
            }
            return super.createBaseView(parentBase, viewInfo);
        }
        
    }
    
    protected static class TailSetImpl<E> extends AbstractSubSetImpl<E> {

        public TailSetImpl(
                AbstractLazyMANavigableSet<E> parent,
                E fromElement,
                boolean inclusive) {
            super(parent, NavigableSetViewInfos.tailSet(fromElement, inclusive));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected MANavigableSet<E> createBaseView(
                MANavigableSet<E> parentBase, ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableSetViewInfos.TailSet) {
                NavigableSetViewInfos.TailSet tailSetViewInfo =
                        (NavigableSetViewInfos.TailSet)viewInfo;
                return parentBase.tailSet((E)tailSetViewInfo.getFromElement(), tailSetViewInfo.isInclusive());
            }
            if (viewInfo instanceof SortedSetViewInfos.TailSet) {
                SortedSetViewInfos.TailSet tailSetViewInfo =
                        (SortedSetViewInfos.TailSet)viewInfo;
                return parentBase.tailSet((E)tailSetViewInfo.getFromElement());
            }
            return super.createBaseView(parentBase, viewInfo);
        }
        
    }
    
    protected static class SubSetImpl<E> extends AbstractSubSetImpl<E> {

        public SubSetImpl(
                AbstractLazyMANavigableSet<E> parent,
                E fromElement,
                boolean fromInclusive,
                E toElement,
                boolean toInclusive) {
            super(parent, NavigableSetViewInfos.subSet(fromElement, fromInclusive, toElement, toInclusive));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected MANavigableSet<E> createBaseView(MANavigableSet<E> parentBase, ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableSetViewInfos.SubSet) {
                NavigableSetViewInfos.SubSet subSetViewInfo =
                        (NavigableSetViewInfos.SubSet)viewInfo;
                return parentBase.subSet(
                        (E)subSetViewInfo.getFromElement(), 
                        subSetViewInfo.isFromInclusive(), 
                        (E)subSetViewInfo.getToElement(), 
                        subSetViewInfo.isToInclusive());
            }
            if (viewInfo instanceof SortedSetViewInfos.SubSet) {
                SortedSetViewInfos.SubSet subSetViewInfo =
                        (SortedSetViewInfos.SubSet)viewInfo;
                return parentBase.subSet(
                        (E)subSetViewInfo.getFromElement(), 
                        (E)subSetViewInfo.getToElement());
            }
            return super.createBaseView(parentBase, viewInfo);
        }
        
    }
    
    protected static class DescendingSetImpl<E> extends AbstractSubSetImpl<E> {

        public DescendingSetImpl(
                AbstractLazyMANavigableSet<E> parent) {
            super(parent, NavigableSetViewInfos.descendingSet());
        }

        @Override
        protected MANavigableSet<E> createBaseView(MANavigableSet<E> parentBase, ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableSetViewInfos.DescendingSet) {
                return parentBase.descendingSet();
            }
            return super.createBaseView(parentBase, viewInfo);
        }
        
    }
    
    protected static abstract class AbstractIteratorImpl<E> extends AbstractLazyMASet.AbstractIteratorImpl<E> {

        public AbstractIteratorImpl(
                AbstractLazyMANavigableSet<E> parent,
                ViewInfo viewInfo) {
            super(parent, viewInfo);
        }

        @Deprecated
        @Override
        protected final MAIterator<E> createBaseView(
                MASet<E> baseParent, ViewInfo viewInfo) {
            return this.createBaseView((MANavigableSet<E>)baseParent, viewInfo);
        }
        
        protected abstract MAIterator<E> createBaseView(MANavigableSet<E> baseParent, ViewInfo viewInfo);
    }
    
    protected static class DescendingIteratorImpl<E> extends AbstractIteratorImpl<E> {

        public DescendingIteratorImpl(AbstractLazyMANavigableSet<E> parent) {
            super(parent, NavigableSetViewInfos.descendingIterator());
        }

        @Override
        protected MAIterator<E> createBaseView(MANavigableSet<E> baseParent, ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableSetViewInfos.DescendingIterator) {
                return baseParent.descendingIterator();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
    }
    
    protected static abstract class RootData<E> extends AbstractLazyMASet.RootData<E> {

        private static final long serialVersionUID = 8285147803883436480L;

        protected RootData() {
            
        }
        
        @Deprecated
        @Override
        protected final void setBase(MASet<E> base) {
            this.setBase((MANavigableSet<E>)base);
        }
        
        protected void setBase(MANavigableSet<E> base) {
            super.setBase(base);
        }
        
        @Override
        protected MANavigableSet<E> createDefaultBase(
                UnifiedComparator<? super E> unifiedComparator) {
            return new MATreeSet<>(unifiedComparator.comparator(true));
        }
    }
}
