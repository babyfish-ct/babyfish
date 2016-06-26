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

import org.babyfish.collection.TreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.XSet;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;

/**
 * @author Tao Chen
 */
public abstract class AbstractLazyXNavigableSet<E> extends AbstractLazyXSet<E> implements XNavigableSet<E> {
    
    public AbstractLazyXNavigableSet(XNavigableSet<E> base) {
        super(base);
    }

    public AbstractLazyXNavigableSet(AbstractLazyXNavigableSet<E> parent, ViewInfo viewInfo) {
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
    protected AbstractLazyXNavigableSet() {
        
    }
    
    @Override
    protected abstract RootData<E> createRootData();
    
    @Deprecated
    @Override
    protected final XSet<E> createBaseView(
            XSet<E> parentBase, 
            ViewInfo viewInfo) {
        return this.createBaseView(
                (XNavigableSet<E>)parentBase, 
                viewInfo);
    }
    
    protected XNavigableSet<E> createBaseView(
            XNavigableSet<E> parentBase, 
            ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }

    @Override
    public Comparator<? super E> comparator() {
        this.requiredEnabled();
        return this.<XNavigableSet<E>>getBase().comparator();
    }

    @Override
    public E lower(E e) {
        this.requiredEnabled();
        return this.<XNavigableSet<E>>getBase().lower(e);
    }

    @Override
    public E floor(E e) {
        this.requiredEnabled();
        return this.<XNavigableSet<E>>getBase().floor(e);
    }

    @Override
    public E ceiling(E e) {
        this.requiredEnabled();
        return this.<XNavigableSet<E>>getBase().ceiling(e);
    }

    @Override
    public E higher(E e) {
        this.requiredEnabled();
        return this.<XNavigableSet<E>>getBase().higher(e);
    }

    @Override
    public E first() {
        this.requiredEnabled();
        return this.<XNavigableSet<E>>getBase().first();
    }

    @Override
    public E last() {
        this.requiredEnabled();
        return this.<XNavigableSet<E>>getBase().last();
    }
    
    @Override
    public E pollFirst() {
        this.enable();
        return this.<XNavigableSet<E>>getBase().pollFirst();
    }

    @Override
    public E pollLast() {
        this.enable();
        return this.<XNavigableSet<E>>getBase().pollLast();
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
        return this.subSet(
                fromElement, 
                true, 
                toElement, 
                false);
    }

    @Override
    public XNavigableSetView<E> subSet(
            E fromElement, boolean fromInclusive,
            E toElement, boolean toInclusive) {
        return new SubSetImpl<E>(this, fromElement, fromInclusive, toElement, toInclusive);
    }
    
    @Override
    public XIterator<E> descendingIterator() {
        return new DescendingIteratorImpl<E>(this);
    }
    
    protected static class AbstractSubSetImpl<E> extends AbstractLazyXNavigableSet<E> implements XNavigableSetView<E> {

        public AbstractSubSetImpl(AbstractLazyXNavigableSet<E> parent, ViewInfo viewInfo) {
            super(parent, viewInfo);
        }

        @Override
        public ViewInfo viewInfo() {
            return this.<XNavigableSetView<E>>getBase().viewInfo();
        }

        @Deprecated
        @Override
        protected final AbstractLazyXSet.RootData<E> createRootData() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    protected static class HeadSetImpl<E> extends AbstractSubSetImpl<E> {

        public HeadSetImpl(
                AbstractLazyXNavigableSet<E> parent,
                E toElement,
                boolean inclusive) {
            super(parent, NavigableSetViewInfos.headSet(toElement, inclusive));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected XNavigableSet<E> createBaseView(
                XNavigableSet<E> parentBase,
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableSetViewInfos.HeadSet) {
                NavigableSetViewInfos.HeadSet headSetByToElementAndInclusvie =
                        (NavigableSetViewInfos.HeadSet)viewInfo;
                return parentBase.headSet(
                        (E)headSetByToElementAndInclusvie.getToElement(), 
                        headSetByToElementAndInclusvie.isInclusive());
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
    }
    
    protected static class TailSetImpl<E> extends AbstractSubSetImpl<E> {

        public TailSetImpl(
                AbstractLazyXNavigableSet<E> parent,
                E fromElement,
                boolean inclusive) {
            super(parent, NavigableSetViewInfos.tailSet(fromElement, inclusive));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected XNavigableSet<E> createBaseView(
                XNavigableSet<E> parentBase,
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableSetViewInfos.TailSet) {
                NavigableSetViewInfos.TailSet tailSet =
                        (NavigableSetViewInfos.TailSet)viewInfo;
                return parentBase.tailSet(
                        (E)tailSet.getFromElement(), 
                        tailSet.isInclusive());
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
    }
    
    protected static class SubSetImpl<E> extends AbstractSubSetImpl<E> {

        public SubSetImpl(
                AbstractLazyXNavigableSet<E> parent,
                E fromElement,
                boolean fromInclusive,
                E toElement,
                boolean toInclusive) {
            super(
                    parent, 
                    NavigableSetViewInfos.subSet(
                            fromElement, 
                            fromInclusive, 
                            toElement, 
                            toInclusive));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected XNavigableSet<E> createBaseView(
                XNavigableSet<E> parentBase,
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableSetViewInfos.SubSet) {
                NavigableSetViewInfos.SubSet subSetViewInfo =
                        (NavigableSetViewInfos.SubSet)viewInfo;
                return parentBase.subSet(
                        (E)subSetViewInfo.getFromElement(), 
                        subSetViewInfo.isFromInclusive(),
                        (E)subSetViewInfo.getToElement(),
                        subSetViewInfo.isToInclusive());
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
    }
    
    protected static class DescendingSetImpl<E> extends AbstractSubSetImpl<E> {

        public DescendingSetImpl(AbstractLazyXNavigableSet<E> parent) {
            super(parent, NavigableSetViewInfos.descendingSet());
        }

        @Override
        protected XNavigableSet<E> createBaseView(
                XNavigableSet<E> parentBase,
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableSetViewInfos.DescendingSet) {
                return parentBase.descendingSet();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
    }
    
    protected static abstract class AbstractIteratorImpl<E> extends AbstractLazyXSet.AbstractIteratorImpl<E> {

        public AbstractIteratorImpl(
                AbstractLazyXNavigableSet<E> parent,
                ViewInfo viewInfo) {
            super(parent, viewInfo);
        }

        @Deprecated
        @Override
        protected final XIterator<E> createBaseView(
                XCollection<E> baseParent, ViewInfo viewInfo) {
            return this.createBaseView((XNavigableSet<E>)baseParent, viewInfo);
        }
        
        protected abstract XIterator<E> createBaseView(XNavigableSet<E> baseParent, ViewInfo viewInfo);
    }
    
    protected static class DescendingIteratorImpl<E> extends AbstractIteratorImpl<E> {

        public DescendingIteratorImpl(
                AbstractLazyXNavigableSet<E> parent) {
            super(parent, NavigableSetViewInfos.descendingIterator());
        }

        @Override
        protected XIterator<E> createBaseView(
                XNavigableSet<E> baseParent, ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableSetViewInfos.DescendingIterator) {
                return baseParent.descendingIterator();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
    }

    protected static abstract class Root<E> extends AbstractLazyXSet.RootData<E> {

        private static final long serialVersionUID = 8107868245299175212L;

        @Deprecated
        @Override
        protected final void setBase(XSet<E> base) {
            this.setBase((XNavigableSet<E>)base);
        }
        
        protected void setBase(XNavigableSet<E> base) {
            super.setBase(base);
        }
        
        @Override
        protected XNavigableSet<E> createDefaultBase(
                UnifiedComparator<? super E> unifiedComparator) {
            return new TreeSet<E>(unifiedComparator.comparator(true));
        }
    }
}
