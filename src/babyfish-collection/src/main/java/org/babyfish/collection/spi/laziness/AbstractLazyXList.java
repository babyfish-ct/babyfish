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

import java.util.Collection;

import org.babyfish.collection.XList;
import org.babyfish.collection.spi.wrapper.AbstractWrapperXList;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.collection.viewinfo.ListViewInfos;
import org.babyfish.data.LazinessManageable;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;

/**
 * @author Tao Chen
 */
public abstract class AbstractLazyXList<E> 
extends AbstractWrapperXList<E> 
implements LazinessManageable {
    
    protected AbstractLazyXList(XList<E> base) {
        super(base);
    }

    protected AbstractLazyXList(
            AbstractWrapperXList<E> parent,
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
    protected AbstractLazyXList() {
        
    }

    @Override
    public final boolean isLoaded() {
        return this.<RootData<E>>getRootData().isLoaded();
    }

    @Override
    public final boolean isLoadable() {
        return this.<RootData<E>>getRootData().isLoadable();
    }

    @Override
    public final void load() {
        this.<RootData<E>>getRootData().load();
    }
    
    @Override
    public int hashCode() {
        this.load();
        return this.getBase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        this.load();
        return this.getBase().equals(obj);
    }

    @Override
    public String toString() {
        if (!this.isLoaded()) {
            return "{ lazyList : Not Loaded }";
        }
        return this.getBase().toString();
    }

    @Override
    public boolean isEmpty() {
        this.requiredEnabled();
        int size = this.<RootData<E>>getRootData().visionallyReadSize();
        if (size == 0) {
            return true;
        }
        this.load();
        return this.getBase().isEmpty();
    }

    @Override
    public int size() {
        this.requiredEnabled();
        if (!(this instanceof View)) {
            int size = this.<RootData<E>>getRootData().visionallyReadSize();
            if (size != -1) {
                return size;
            }       
        }
        this.load();
        return this.getBase().size();
    }
    
    @Override
    public boolean contains(Object o) {
        this.requiredEnabled();
        this.load();
        return this.<XList<E>>getBase().contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        this.requiredEnabled();
        this.load();
        return this.<XList<E>>getBase().containsAll(c);
    }

    @Override
    public int indexOf(Object o) {
        this.requiredEnabled();
        this.load();
        return this.<XList<E>>getBase().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        this.requiredEnabled();
        this.load();
        return this.<XList<E>>getBase().lastIndexOf(o);
    }

    @Override
    public E get(int index) {
        this.requiredEnabled();
        this.load();
        return this.<XList<E>>getBase().get(index);
    }
    
    @Override
    public Object[] toArray() {
        this.requiredEnabled();
        this.load();
        return this.getBase().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        this.requiredEnabled();
        this.load();
        return this.getBase().toArray(a);
    }

    @Override
    public boolean add(E element) {
        this.enable();
        this.load();
        return this.<XList<E>>getBase().add(element);
    }
    
    @Override
    public void add(int index, E element) {
        this.enable();
        this.load();
        this.<XList<E>>getBase().add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        this.enable();
        this.load();
        return this.<XList<E>>getBase().addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        this.enable();
        this.load();
        return this.<XList<E>>getBase().addAll(index, c);
    }
    
    @Override
    public void clear() {
        this.enable();
        this.load();
        this.<XList<E>>getBase().clear();
    }

    @Override
    public E remove(int index) {
        this.enable();
        this.load();
        return this.<XList<E>>getBase().remove(index);
    }

    @Override
    public boolean remove(Object o) {
        this.enable();
        this.load();
        return super.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        this.enable();
        this.load();
        return this.<XList<E>>getBase().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        this.enable();
        this.load();
        return this.<XList<E>>getBase().retainAll(c);
    }

    @Override
    public E set(int index, E element) {
        this.enable();
        this.load();
        return this.<XList<E>>getBase().set(index, element);
    }

    @Override
    public XListView<E> subList(int fromIndex, int toIndex) {
        return new SubListImpl<E>(this, fromIndex, toIndex);
    }

    @Override
    public XListIterator<E> listIterator(int index) {
        return new IteratorImpl<E>(this, index);
    }

    @Override
    protected abstract RootData<E> createRootData();
    
    protected static class SubListImpl<E> extends AbstractLazyXList<E> implements XListView<E> {

        protected SubListImpl(
                AbstractLazyXList<E> parent,
                int fromIndex,
                int toIndex) {
            super(parent, ListViewInfos.subList(fromIndex, toIndex));
        }
        
        @Override
        protected XList<E> createBaseView(
                XList<E> parentBase,
                ViewInfo viewInfo) {
            if (viewInfo instanceof ListViewInfos.SubList) {
                ListViewInfos.SubList subListViewInfo = (ListViewInfos.SubList)viewInfo;
                return parentBase.subList(
                        subListViewInfo.getFromIndex(), 
                        subListViewInfo.getToIndex());
            }
            return super.createBaseView(parentBase, viewInfo);
        }
        
        @Deprecated
        @Override
        protected final RootData<E> createRootData() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ViewInfo viewInfo() {
            return this.<XListView<E>>getBase().viewInfo();
        }
    }
    
    protected static abstract class AbstractIteratorImpl<E> extends AbstractWrapperXList.AbstractIteratorImpl<E> {

        protected AbstractIteratorImpl(AbstractLazyXList<E> parent, ViewInfo viewInfo) {
            super(loadParent(parent), viewInfo);
        }
        
        private static <E> AbstractLazyXList<E> loadParent(AbstractLazyXList<E> parent) {
            parent.load();
            return parent;
        }
    }
    
    protected static class IteratorImpl<E> extends AbstractIteratorImpl<E> {
        
        public IteratorImpl(
                AbstractLazyXList<E> parent,
                int index) {
            super(parent, ListViewInfos.listIterator(index));
        }
        
        @Override
        protected XListIterator<E> createBaseView(
                XList<E> baseParent,
                ViewInfo viewInfo) {
            if (viewInfo instanceof ListViewInfos.ListIterator) {
                ListViewInfos.ListIterator listIteratorViewInfo =
                        (ListViewInfos.ListIterator)viewInfo;
                return baseParent.listIterator(listIteratorViewInfo.getIndex());
            }
            if (viewInfo instanceof CollectionViewInfos.Iterator) {
                return baseParent.listIterator();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }
    }
    
    protected static abstract class RootData<E> extends AbstractWrapperXList.RootData<E> {
        
        private static final long serialVersionUID = -7864408595209010787L;
        
        public RootData() {
            
        }

        public abstract boolean isLoaded();

        public abstract boolean isLoading();
        
        protected abstract void setLoaded(boolean loaded);
        
        protected abstract void setLoading(boolean loading);
        
        public abstract boolean isLoadable();

        public final void load() {
            if (!this.isLoaded()) {
                if (this.isLoading()) {
                    throw new IllegalStateException(CommonMessages.loadingOperationWhenDataIsBeingLoaded());
                }
                this.setLoading(true);
                try {
                    this.onLoad();
                } finally {
                    this.setLoading(false);
                }
                this.setLoaded(true);
            }
        }
        
        @Override
        public boolean isDispatchable() {
            if (this.isLoading()) {
                return false;
            }
            return super.isDispatchable();
        }
        
        public final boolean isReadableVision() {
            return !this.isLoaded() && this.onGetIsReadableVision();
        }
        
        public final int visionallyReadSize() {
            if (this.isReadableVision()) {
                return this.onGetVisionalSize();
            }
            return -1;
        }
        
        protected int onGetVisionalSize() {
            return -1;
        }
        
        protected boolean onGetIsReadableVision() {
            return false;
        }
        
        protected abstract void onLoad();
    }

}
