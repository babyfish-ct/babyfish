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

import java.util.NavigableSet;

import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MAList;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XList;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.event.ListElementListener;
import org.babyfish.collection.spi.wrapper.event.AbstractListElementEventDispatcher;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.collection.viewinfo.ListViewInfos;
import org.babyfish.data.ModificationException;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.Cause;
import org.babyfish.data.event.PropertyVersion;

/**
 * @author Tao Chen
 */
public class AbstractWrapperMAList<E> extends AbstractWrapperXList<E> implements MAList<E> {
    
    private static final Object AK_ELEMENT_LISTENER = new Object();
    
    private static final Object AK_LIST_ELEMENT_LISTENER = new Object();
    
    private ElementListener<E> elementListener;
    
    private ListElementListener<E> listElementListener;

    protected AbstractWrapperMAList(MAList<E> base) {
        super(base);
    }

    protected AbstractWrapperMAList(
            AbstractWrapperMAList<E> parent,
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
    protected AbstractWrapperMAList() {
        
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addElementListener(ElementListener<? super E> listener) {
        this.elementListener = ElementListener.combine(
                this.elementListener, 
                (ElementListener<E>)listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeElementListener(ElementListener<? super E> listener) {
        this.elementListener = ElementListener.remove(
                this.elementListener, 
                (ElementListener<E>)listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addListElementListener(ListElementListener<? super E> listener) {
        this.listElementListener = ListElementListener.combine(
                    this.listElementListener, 
                    (ListElementListener<E>)listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeListElementListener(ListElementListener<? super E> listener) {
        this.listElementListener = ListElementListener.remove(
                    this.listElementListener, 
                    (ListElementListener<E>)listener);
    }

    @Override
    public MAListView<E> subList(int fromIndex, int toIndex) {
        return new SubListImpl<E>(this, fromIndex, toIndex);
    }

    @Override
    public MAListIterator<E> iterator() {
        return this.listIterator(0);
    }

    @Override
    public MAListIterator<E> listIterator() {
        return this.listIterator(0);
    }

    @Override
    public MAListIterator<E> listIterator(int index) {
        return new IteratorImpl<E>(this, index);
    }
    
    protected void executeModifying(ListElementEvent<E> e) {
        Throwable finalThrowable = null;
        try {
            this.onModifying(e);    
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            this.raiseModifying(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        try {
            this.bubbleModifying(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw new ModificationException(false, e, finalThrowable);
        }
    }
    
    protected void executeModified(ListElementEvent<E> e) {
        Throwable finalThrowable = null;
        try {
            this.bubbleModified(e);     
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            this.raiseModified(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        try {
            this.onModified(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw new ModificationException(true, e, finalThrowable);
        }
    }

    protected void onModifying(ListElementEvent<E> e) throws Throwable {
        
    }
    
    protected void onModified(ListElementEvent<E> e) throws Throwable {
        
    }

    protected void raiseModifying(ListElementEvent<E> e) throws Throwable {
        Throwable finalThrowable = null;
        try {
            ElementListener<E> elementListener = this.elementListener;
            if (elementListener != null) {
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .addAttribute(AK_ELEMENT_LISTENER, elementListener);
                elementListener.modifying(e);
            }
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            ListElementListener<E> listElementListener = this.listElementListener;
            if (listElementListener != null) {
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .addAttribute(AK_LIST_ELEMENT_LISTENER, listElementListener);
                listElementListener.modifying(e);
            }
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw finalThrowable;
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void raiseModified(ListElementEvent<E> e) throws Throwable {
        Throwable finalThrowable = null;
        try {
            ListElementListener<E> listElementListener = 
                (ListElementListener<E>)
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .removeAttribute(AK_LIST_ELEMENT_LISTENER);
            if (listElementListener != null) {
                listElementListener.modified(e);
            }
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            ElementListener<E> elementListener = 
                (ElementListener<E>)
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .removeAttribute(AK_ELEMENT_LISTENER);
            if (elementListener != null) {
                elementListener.modified(e);
            }
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw finalThrowable;
        }
    }

    protected void bubbleModifying(ListElementEvent<E> e) {
        
    }
    
    protected void bubbleModified(ListElementEvent<E> e) {
        
    }
    
    @Override
    protected RootData<E> createRootData() {
        return new RootData<E>();
    }

    @Deprecated
    @Override
    protected final XList<E> createBaseView(XList<E> parentBase, ViewInfo viewInfo) {
        return this.createBaseView((MAList<E>)parentBase, viewInfo);
    }
    
    protected MAList<E> createBaseView(MAList<E> parentBase, ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }

    @Override
    protected AbstractListElementEventDispatcher<E> createEventDispatcher() {
        return new AbstractListElementEventDispatcher<E>(this) {

            @Override
            protected boolean isDispatchable() {
                return 
                        this
                        .<AbstractWrapperMAList<E>>getOwner()
                        .getRootData()
                        .isDispatchable();
            }

            @Override
            protected void executePreDispatchedEvent(ListElementEvent<E> dispatchedEvent) {
                this.<AbstractWrapperMAList<E>>getOwner().executeModifying(dispatchedEvent);
            }

            @Override
            protected void executePostDispatchedEvent(ListElementEvent<E> dispatchedEvent) {
                this.<AbstractWrapperMAList<E>>getOwner().executeModified(dispatchedEvent);
            }
        };
    }

    protected static class SubListImpl<E> extends AbstractWrapperMAList<E> implements MAListView<E> {
        
        protected SubListImpl(AbstractWrapperMAList<E> parent, int fromIndex, int toIndex) {
            super(parent, ListViewInfos.subList(fromIndex, toIndex));
        }

        @Override
        public ViewInfo viewInfo() {
            return this.<XListView<E>>getBase().viewInfo();
        }
        
        @Override
        protected void bubbleModifying(final ListElementEvent<E> e) {
            AbstractWrapperMAList<E> parent = this.getParent();
            ListElementEvent<E> bubbledEvent = new ListElementEvent<>(
                    parent, 
                    new Cause(e), 
                    null, 
                    version -> {
                        int offset = ((ListViewInfos.SubList)this.viewInfo()).getFromIndex();
                        if (version == PropertyVersion.ATTACH) {
                            NavigableSet<Integer> conflictAbsIndexes = e.getConflictAbsoluteIndexes();
                            if (!conflictAbsIndexes.isEmpty()) {
                                int headHide = this.getHeadHide();
                                if (headHide != 0) {
                                    offset -= 
                                            conflictAbsIndexes.subSet(
                                                    headHide - offset, 
                                                    true, 
                                                    headHide,
                                                    false
                                            )
                                            .size();
                                }
                            }
                        }
                        return e.getIndex(version) + offset;
                    }
            );
            parent.executeModifying(bubbledEvent);
        }

        @Override
        protected void bubbleModified(ListElementEvent<E> e) {
            AbstractWrapperMAList<E> parent = this.getParent();
            ListElementEvent<E> bubbledEvent = e.getBubbledEvent(parent);
            parent.executeModified(bubbledEvent);
        }

        @Override
        protected MAList<E> createBaseView(MAList<E> parentBase, ViewInfo viewInfo) {
            if (viewInfo instanceof ListViewInfos.SubList) {
                ListViewInfos.SubList subListViewInfo = (ListViewInfos.SubList)viewInfo;
                return parentBase.subList(subListViewInfo.getFromIndex(), subListViewInfo.getToIndex());
            }
            return super.createBaseView(parentBase, viewInfo);
        }

    }

    protected static abstract class AbstractIteratorImpl<E> 
    extends AbstractWrapperXList.AbstractIteratorImpl<E> 
    implements MAListIterator<E> {

        private ElementListener<E> elementListener;
        
        private ListElementListener<E> listElementListener;
        
        public AbstractIteratorImpl(
                AbstractWrapperMAList<E> parent,
                ViewInfo viewInfo) {
            super(parent, viewInfo);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void addElementListener(ElementListener<? super E> listener) {
            this.elementListener = ElementListener.combine(
                            this.elementListener, (ElementListener<E>)listener);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removeElementListener(ElementListener<? super E> listener) {
            this.elementListener = ElementListener.remove(
                            this.elementListener, (ElementListener<E>)listener);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void addListElementListener(ListElementListener<? super E> listener) {
            this.listElementListener = ListElementListener.combine(
                        this.listElementListener, 
                        (ListElementListener<E>)listener);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removeListElementListener(
                ListElementListener<? super E> listener) {
            this.listElementListener = ListElementListener.remove(
                        this.listElementListener, 
                        (ListElementListener<E>)listener);
        }
        
        protected void executeModifying(ListElementEvent<E> e) {
            Throwable finalThrowable = null;
            try {
                this.onModifying(e);    
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                this.raiseModifying(e);
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            try {
                this.bubbleModifying(e);
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            if (finalThrowable != null) {
                throw new ModificationException(false, e, finalThrowable);
            }
        }
        
        protected void executeModified(ListElementEvent<E> e) {
            Throwable finalThrowable = null;
            try {
                this.bubbleModified(e);     
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                this.raiseModified(e);
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            try {
                this.onModified(e);
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            if (finalThrowable != null) {
                throw new ModificationException(true, e, finalThrowable);
            }
        }

        protected void onModifying(ListElementEvent<E> e) throws Throwable {
            
        }
        
        protected void onModified(ListElementEvent<E> e) throws Throwable {
            
        }

        protected void raiseModifying(ListElementEvent<E> e) throws Throwable {
            Throwable finalThrowable = null;
            try {
                ElementListener<E> elementListener = this.elementListener;
                if (elementListener != null) {
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .addAttribute(AK_ELEMENT_LISTENER, elementListener);
                    elementListener.modifying(e);
                }
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                ListElementListener<E> listElementListener = this.listElementListener;
                if (listElementListener != null) {
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .addAttribute(AK_LIST_ELEMENT_LISTENER, listElementListener);
                    listElementListener.modifying(e);
                }
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            if (finalThrowable != null) {
                throw finalThrowable;
            }
        }
        
        @SuppressWarnings("unchecked")
        protected void raiseModified(ListElementEvent<E> e) throws Throwable {
            Throwable finalThrowable = null;
            try {
                ListElementListener<E> listElementListener = 
                    (ListElementListener<E>)
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .removeAttribute(AK_LIST_ELEMENT_LISTENER);
                if (listElementListener != null) {
                    listElementListener.modified(e);
                }
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                ElementListener<E> elementListener = 
                    (ElementListener<E>)
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .removeAttribute(AK_ELEMENT_LISTENER);
                if (elementListener != null) {
                    elementListener.modified(e);
                }
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            if (finalThrowable != null) {
                throw finalThrowable;
            }
        }

        protected void bubbleModifying(ListElementEvent<E> e) {
            AbstractWrapperMAList<E> parent = this.<AbstractWrapperMAList<E>>getParent();
            ListElementEvent<E> bubbledEvent = new ListElementEvent<>(
                    parent, 
                    new Cause(e), 
                    null,
                    null);
            parent.executeModifying(bubbledEvent);
        }
        
        protected void bubbleModified(ListElementEvent<E> e) {
            AbstractWrapperMAList<E> parent = this.<AbstractWrapperMAList<E>>getParent();
            ListElementEvent<E> bubbledEvent = e.getBubbledEvent(parent);
            parent.executeModified(bubbledEvent);
        }

        @Deprecated
        @Override
        protected final XListIterator<E> createBaseView(
                XList<E> baseParent, ViewInfo viewInfo) {
            return this.createBaseView((MAList<E>)baseParent, viewInfo);
        }
        
        protected abstract MAListIterator<E> createBaseView(
                MAList<E> baseParent, ViewInfo viewInfo);

        @Override
        protected AbstractListElementEventDispatcher<E> createEventDispatcher() {
            return new AbstractListElementEventDispatcher<E>(this) {

                @Override
                protected boolean isDispatchable() {
                    return
                            this
                            .<AbstractIteratorImpl<E>>getOwner()
                            .getParent()
                            .getRootData()
                            .isDispatchable();
                }

                @Override
                protected void executePreDispatchedEvent(ListElementEvent<E> dispatchedEvent) {
                    this.<AbstractIteratorImpl<E>>getOwner().executeModifying(dispatchedEvent);
                }

                @Override
                protected void executePostDispatchedEvent(ListElementEvent<E> dispatchedEvent) {
                    this.<AbstractIteratorImpl<E>>getOwner().executeModified(dispatchedEvent);
                }
                
            };
        }
        
    }
    
    protected static class IteratorImpl<E> extends AbstractIteratorImpl<E> {

        public IteratorImpl(
                AbstractWrapperMAList<E> parent, 
                int index) {
            super(parent, ListViewInfos.listIterator(index));
        }

        @Override
        protected MAListIterator<E> createBaseView(
                MAList<E> baseParent, 
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
    }
    
    protected static class RootData<E> extends AbstractWrapperXList.RootData<E> {

        private static final long serialVersionUID = 8911284249630215109L;
        
        public RootData() {
            
        }

        @Deprecated
        @Override
        protected final void setBase(XList<E> base) {
            this.setBase((MAList<E>)base);
        }
        
        protected void setBase(MAList<E> base) {
            super.setBase(base);
        }

        @Override
        protected MAList<E> createDefaultBase(
                UnifiedComparator<? super E> unifiedComparator) {
            return new MAArrayList<E>(unifiedComparator);
        }
        
    }

}
