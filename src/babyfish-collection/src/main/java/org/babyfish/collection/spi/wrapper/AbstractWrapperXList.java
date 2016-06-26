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

import java.util.Collection;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.BidiType;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XList;
import org.babyfish.collection.spi.wrapper.event.AbstractListElementEventDispatcher;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.collection.viewinfo.ListViewInfos;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;

/**
 * @author Tao Chen
 */
public class AbstractWrapperXList<E> extends AbstractWrapperXCollection<E> implements XList<E> {
    
    protected AbstractWrapperXList(XList<E> base) {
        super(base);
    }
    
    protected AbstractWrapperXList(AbstractWrapperXList<E> parent, ViewInfo viewInfo) {
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
    protected AbstractWrapperXList() {
        
    }

    @Override
    public BidiType bidiType() {
        return this.<XList<E>>getBase().bidiType();
    }

    @Override
    public int indexOf(Object o) {
        this.requiredEnabled();
        return this.<XList<E>>getBase().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        this.requiredEnabled();
        return this.<XList<E>>getBase().lastIndexOf(o);
    }

    @Override
    public E get(int index) {
        this.requiredEnabled();
        return this.<XList<E>>getBase().get(index);
    }

    @Override
    public E set(int index, E element) {
        this.enable();
        return this.<XList<E>>getBase().set(index, element);
    }

    @Override
    public void add(int index, E element) {
        this.enable();
        this.<XList<E>>getBase().add(index, element);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        this.enable();
        return this.<XList<E>>getBase().addAll(index, c);
    }

    @Override
    public E remove(int index) {
        this.enable();
        return this.<XList<E>>getBase().remove(index);
    }

    @Override
    public XListView<E> subList(int fromIndex, int toIndex) {
        return new SubListImpl<E>(this, fromIndex, toIndex);
    }
        
    @Override
    public XListIterator<E> iterator() {
        return this.listIterator(0);
    }

    @Override
    public XListIterator<E> listIterator() {
        return this.listIterator(0);
    }

    @Override
    public XListIterator<E> listIterator(int index) {
        return new IteratorImpl<E>(this, index);
    }
    
    @Override
    protected RootData<E> createRootData() {
        return new RootData<E>();
    }
    
    @Deprecated
    @Override
    protected final XCollection<E> createBaseView(
            XCollection<E> parentBase, ViewInfo viewInfo) {
        return this.createBaseView((XList<E>)parentBase, viewInfo);
    }
    
    protected XList<E> createBaseView(
            XList<E> parentBase, ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }
    
    @Override
    protected AbstractListElementEventDispatcher<E> createEventDispatcher() {
        return null;
    }

    @SuppressWarnings("unchecked")
    protected final int getHeadHide() {
        int headHide = 0;
        for (AbstractWrapperXList<E> list = this; list instanceof XListView; list = list.getParent()) {
            XListView<E> listView = (XListView<E>)list;
            headHide += ((ListViewInfos.SubList)listView.viewInfo()).getFromIndex();
        }
        return headHide;
    }

    @SuppressWarnings("unchecked")
    protected final int getTailHide() {
        int tailHide = 0;
        for (AbstractWrapperXList<E> list = this; list instanceof XListView; list = list.getParent()) {
            XListView<E> listView = (XListView<E>)list;
            tailHide += list.size() - ((ListViewInfos.SubList)listView.viewInfo()).getToIndex();
        }
        return tailHide;
    }

    public static class RootData<E> extends AbstractWrapperXCollection.RootData<E> {
        
        private static final long serialVersionUID = -3181919309055733821L;
        
        public RootData() {
            
        }

        @Deprecated
        @Override
        protected final void setBase(XCollection<E> base) {
            this.setBase((XList<E>)base);
        }
        
        protected void setBase(XList<E> base) {
            super.setBase(base);
        }

        @Override
        protected XList<E> createDefaultBase(
                UnifiedComparator<? super E> unifiedComparator) {
            return new ArrayList<E>(unifiedComparator);
        }
    }
    
    protected static class SubListImpl<E> extends AbstractWrapperXList<E> implements XListView<E> {

        protected SubListImpl(
                AbstractWrapperXList<E> parent, 
                int fromIndex, 
                int toIndex) {
            super(parent, ListViewInfos.subList(fromIndex, toIndex));
        }

        protected XList<E> createBaseView(
                XList<E> parentBase, ViewInfo viewInfo) {
            if (viewInfo instanceof ListViewInfos.SubList) {
                ListViewInfos.SubList subListViewInfo = (ListViewInfos.SubList)viewInfo;
            return parentBase.subList(
                    subListViewInfo.getFromIndex(), 
                    subListViewInfo.getToIndex());
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
        @Override
        public ViewInfo viewInfo() {
            return this.<XListView<E>>getBase().viewInfo();
        }
    }
    
    protected static abstract class AbstractIteratorImpl<E> 
    extends AbstractWrapperXCollection.AbstractIteratorImpl<E> 
    implements XListIterator<E> {

        protected AbstractIteratorImpl(
                AbstractWrapperXList<E> parent,
                ViewInfo viewInfo) {
            super(parent, viewInfo);
        }

        @Deprecated
        @Override
        protected final XIterator<E> createBaseView(
                XCollection<E> baseParent, ViewInfo viewInfo) {
            return this.createBaseView((XList<E>)baseParent, viewInfo);
        }
        
        protected abstract XListIterator<E> createBaseView(
                XList<E> baseParent, ViewInfo viewInfo);
        
        @Override
        public int nextIndex() {
            this.checkForComodification();
            this.getParent().requiredEnabled();
            return this.<XListIterator<E>>getBase().nextIndex();
        }

        @Override
        public boolean hasPrevious() {
            this.checkForComodification();
            this.getParent().requiredEnabled();
            return this.<XListIterator<E>>getBase().hasPrevious();
        }

        @Override
        public E previous() {
            this.checkForComodification();
            this.getParent().requiredEnabled();
            return this.<XListIterator<E>>getBase().previous();
        }

        @Override
        public int previousIndex() {
            this.checkForComodification();
            this.getParent().requiredEnabled();
            return this.<XListIterator<E>>getBase().previousIndex();
        }

        @Override
        public void set(E e) {
            this.checkForComodification();
            this.getParent().enable();
            this.<XListIterator<E>>getBase().set(e);
        }

        @Override
        public void add(E e) {
            this.checkForComodification();
            this.getParent().enable();
            this.<XListIterator<E>>getBase().add(e);
        }
    }
    
    protected static class IteratorImpl<E> extends AbstractIteratorImpl<E> {

        public IteratorImpl(
                AbstractWrapperXList<E> parent,
                int index) {
            super(parent, ListViewInfos.listIterator(index));
        }
        
        @Override
        protected XListIterator<E> createBaseView(
                XList<E> baseParent,
                ViewInfo viewInfo) {
            if (viewInfo instanceof ListViewInfos.ListIterator) {
                ListViewInfos.ListIterator listIteratorByIndexViewInfo =
                        (ListViewInfos.ListIterator)viewInfo;
                return baseParent.listIterator(listIteratorByIndexViewInfo.getIndex());
            }
            if (viewInfo instanceof CollectionViewInfos.Iterator) {
                return baseParent.listIterator();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
        
        @Override
        protected AbstractListElementEventDispatcher<E> createEventDispatcher() {
            return null;
        }
            
    }
}
