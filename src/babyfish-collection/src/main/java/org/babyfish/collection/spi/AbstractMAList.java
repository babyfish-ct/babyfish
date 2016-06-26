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

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.NavigableSet;

import org.babyfish.collection.MAList;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.event.ListElementListener;
import org.babyfish.collection.event.modification.ListIteratorModifications;
import org.babyfish.collection.event.modification.ListModifications;
import org.babyfish.collection.event.spi.ConflictAbsoluteIndexes;
import org.babyfish.collection.spi.base.BaseElements;
import org.babyfish.collection.spi.base.BaseElementsConflictHandler;
import org.babyfish.collection.spi.base.BaseElementsHandler;
import org.babyfish.collection.spi.base.BaseElementsSpecialHandlerFactory;
import org.babyfish.collection.spi.base.BaseListIterator;
import org.babyfish.collection.viewinfo.ListViewInfos;
import org.babyfish.data.ModificationException;
import org.babyfish.data.View;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.Cause;
import org.babyfish.data.event.PropertyVersion;
import org.babyfish.data.event.spi.GlobalAttributeContext;
import org.babyfish.data.event.spi.InAllChainAttributeContext;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
public abstract class AbstractMAList<E> extends AbstractXList<E> implements MAList<E> {
    
    private static final Object AK_ELEMENT_LISTENER = new Object();

    private static final Object AK_LIST_ELEMENT_LISTENER = new Object();

    protected transient ElementListener<E> elementListener;
    
    protected transient ListElementListener<E> listElementListener;

    protected AbstractMAList(BaseElements<E> baseElements) {
        super(baseElements);
        if (!(this instanceof View)) {
            baseElements.initSpecialHandlerFactory(this.new SpecialHandlerFactoryImpl());
        }
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
    protected AbstractMAList() {
        
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
        
    }
    
    protected void bubbleModified(ListElementEvent<E> e) {
        
    }

    @Override
    public boolean add(E e) {
        this.baseElements.add(
                this.headHide(),
                this.tailHide(),
                this.baseElements.allSize() - this.headHide() - this.tailHide(), 
                e, 
                this.new HandlerImpl4List(ListModifications.add(e)),
                this.createBaseElementsRangeChangeHandler());
        return true;
    }

    @Override
    public void add(int index, E element) {
        this.baseElements.add(
                this.headHide(),
                this.tailHide(),
                index, 
                element, 
                this.new HandlerImpl4List(
                        ListModifications.add(index, element)),
                        this.createBaseElementsRangeChangeHandler());
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.baseElements.addAll(
                this.headHide(),
                this.tailHide(),
                this.baseElements.allSize() - this.headHide() - this.tailHide(),
                c, 
                this.new HandlerImpl4List(ListModifications.addAll(c)),
                this.createBaseElementsRangeChangeHandler());
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return this.baseElements.addAll(
                this.headHide(),
                this.tailHide(),
                index,
                c, 
                this.new HandlerImpl4List(ListModifications.addAll(index, c)),
                this.createBaseElementsRangeChangeHandler());
    }

    @Override
    public void clear() {
        this.baseElements.clear(
                this.headHide(),
                this.tailHide(),
                this.new HandlerImpl4List(ListModifications.<E>clear()));
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
        return new ListIteratorImpl<E>(this, index);
    }

    @Override
    public E remove(int index) {
        return this.baseElements.removeAt(
                this.headHide(),
                this.tailHide(),
                index, 
                this.new HandlerImpl4List(ListModifications.<E>remove(index)));
    }

    @Override
    public boolean remove(Object o) {
        return this.baseElements.remove(
                this.headHide(),
                this.tailHide(),
                o, 
                this.new HandlerImpl4List(ListModifications.<E>remove(o)));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.baseElements.removeAll(
                this.headHide(),
                this.tailHide(),
                c, 
                this.new HandlerImpl4List(ListModifications.<E>removeAll(c)));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.baseElements.retainAll(
                this.headHide(),
                this.tailHide(),
                c, 
                this.new HandlerImpl4List(ListModifications.<E>retainAll(c)));
    }

    @Override
    public E set(int index, E element) {
        return this.baseElements.set(
                this.headHide(),
                this.tailHide(),
                index, 
                element, 
                this.new HandlerImpl4List(ListModifications.set(index, element)),
                this.createBaseElementsRangeChangeHandler());
    }

    @Override
    public MAListView<E> subList(int fromIndex, int toIndex) {
        return new SubListImpl<E>(this, fromIndex, toIndex);
    }
    
    protected static class ListIteratorImpl<E> implements MAListIterator<E> {
        
        private AbstractMAList<E> parentList;
        
        private BaseListIterator<E> iterator;
        
        protected transient ElementListener<E> elementListener;
        
        protected transient ListElementListener<E> listElementListener;
        
        private ListViewInfos.ListIterator viewInfo;
        
        protected ListIteratorImpl(AbstractMAList<E> parentList, int index) {
            this.parentList = Arguments.mustNotBeNull("parentList", parentList);
            this.iterator = parentList.baseElements.listIterator(
                    parentList.headHide(),
                    parentList.tailHide(),
                    index, 
                    parentList.createBaseElementsRangeChangeHandler());
            this.viewInfo = ListViewInfos.listIterator(index);
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractMAList<E>> T getParentList() {
            return (T)this.parentList;
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
            AbstractMAList<E> parentList = this.parentList;
            ListElementEvent<E> bubbledEvent = new ListElementEvent<>(
                    parentList, 
                    new Cause(e), 
                    null, 
                    null);
            parentList.executeModifying(bubbledEvent);
        }

        protected void bubbleModified(ListElementEvent<E> e) {
            AbstractMAList<E> parentList = this.parentList;
            ListElementEvent<E> bubbleEvent = e.getBubbledEvent(parentList);
            parentList.executeModified(bubbleEvent);
        }
        
        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return this.iterator.hasPrevious();
        }

        @Override
        public E next() {
            return this.iterator.next();
        }

        @Override
        public int nextIndex() {
            return this.iterator.nextIndex();
        }

        @Override
        public E previous() {
            return this.iterator.previous();
        }

        @Override
        public int previousIndex() {
            return this.iterator.previousIndex();
        }

        @Override
        public void remove() {
            try {
                this.iterator.remove(new HandlerImpl4ListIterator(ListIteratorModifications.<E>remove()));
            } finally {
                this.syncParentModCount();
            }
        }

        @Override
        public void set(E e) {
            try {
                this.iterator.set(e, new HandlerImpl4ListIterator(ListIteratorModifications.set(e)));
            } finally {
                this.syncParentModCount();
            }
        }
        
        @Override
        public void add(E e) {
            try {
                this.iterator.add(e, new HandlerImpl4ListIterator(ListIteratorModifications.add(e)));
            } finally {
                this.syncParentModCount();
            }
        }

        @Override
        public boolean isReadWriteLockSupported() {
            return this.parentList.isReadWriteLockSupported();
        }

        @Override
        public UnifiedComparator<? super E> unifiedComparator() {
            return this.parentList.unifiedComparator();
        }

        @Override
        public ListViewInfos.ListIterator viewInfo() {
            return this.viewInfo;
        }
        
        private void syncParentModCount() {
            if (this.parentList instanceof SubListImpl) {
                ((SubListImpl<E>)this.parentList).syncModCount();
            }
        }

        private class HandlerImpl4ListIterator implements BaseElementsHandler<E> {
            
            private Modification<E> modification;

            public HandlerImpl4ListIterator(Modification<E> modification) {
                this.modification = modification;
            }
            
            @Override
            public Object createAddingArgument(int index, E element) {
                return ListElementEvent.createAttachEvent(
                        ListIteratorImpl.this, 
                        this.modification, 
                        element, 
                        index
                );
            }

            @SuppressWarnings("unchecked")
            @Override
            public void adding(int index, E element, Object argument) {
                ListElementEvent<E> event = (ListElementEvent<E>)argument;
                ListIteratorImpl.this.executeModifying(event);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void added(int index, E element, Object argument) {
                ListElementEvent<E> event = (ListElementEvent<E>)argument;
                ListIteratorImpl.this.executeModified(event);
            }

            @Override
            public Object createChangingArgument(int oldIndex, int newIndex, E oldElement, E newElement) {
                return ListElementEvent.createReplaceEvent(
                        ListIteratorImpl.this, 
                        this.modification,
                        oldElement, 
                        newElement, 
                        oldIndex,
                        newIndex
                );
            }

            @SuppressWarnings("unchecked")
            @Override
            public void changing(int oldIndex, int newIndex, E oldElement, E newElement, Object argument) {
                ListElementEvent<E> event = (ListElementEvent<E>)argument;
                ListIteratorImpl.this.executeModifying(event);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void changed(int oldIndex, int newIndex, E oldElement, E newElement, Object argument) {
                ListElementEvent<E> event = (ListElementEvent<E>)argument;
                ListIteratorImpl.this.executeModified(event);
            }

            @Override
            public Object createRemovingArgument(int index, E element) {
                return ListElementEvent.createDetachEvent(
                        ListIteratorImpl.this, 
                        this.modification,
                        element, 
                        index
                );
            }

            @SuppressWarnings("unchecked")
            @Override
            public void removing(int index, E element, Object argument) {
                ListElementEvent<E> event = (ListElementEvent<E>)argument;
                ListIteratorImpl.this.executeModifying(event);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void removed(int index, E element, Object argument) {
                ListElementEvent<E> event = (ListElementEvent<E>)argument;
                ListIteratorImpl.this.executeModified(event);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void setPreThrowable(Object argument, Throwable throwable) {
                ListElementEvent<E> event = (ListElementEvent<E>)argument;
                ((InAllChainAttributeContext)event.getAttributeContext(AttributeScope.IN_ALL_CHAIN))
                .setPreThrowable(throwable);
            }

            @Override
            public void setNullOrThrowable(Throwable nullOrThrowable) {
                if (nullOrThrowable != null) {
                    ((GlobalAttributeContext)this.modification.getAttributeContext()).setThrowable(nullOrThrowable);
                } else {
                    ((GlobalAttributeContext)this.modification.getAttributeContext()).success();
                }
            }

            @Override
            public void setConflictAbsIndexes(NavigableSet<Integer> conflictAbsIndexes) {
                ConflictAbsoluteIndexes.set(this.modification, conflictAbsIndexes);
            }
        }
    }
    
    protected static class SubListImpl<E> extends AbstractMAList<E> implements MAListView<E> {
        
        private AbstractMAList<E> parentList;
        
        private int headHide;
        
        private int tailHide;
        
        private int expectedModCount;
        
        protected SubListImpl(AbstractMAList<E> parentList, int fromIndex, int toIndex) {
            super(Arguments.mustNotBeNull("parentList", parentList).baseElements);
            this.parentList = parentList;
            Arguments.indexMustBeGreaterThanOrEqualToValue("fromIndex", fromIndex, 0);
            Arguments.indexMustBeLessThanOrEqualToValue("fromIndex", fromIndex, parentList.size());
            Arguments.indexMustBeLessThanOrEqualToOther("fromIndex", fromIndex, "toIndex", toIndex);
            this.headHide = parentList.headHide() + fromIndex;
            this.tailHide =  this.baseElements.allSize() - parentList.headHide() - toIndex;
            this.expectedModCount = this.baseElements.modCount();
        }
        
        @Override
        public ListViewInfos.SubList viewInfo() {
            AbstractXList<E> parentList = this.parentList;
            int fromIndex = this.headHide - parentList.headHide();
            int toIndex = this.baseElements.allSize() - parentList.headHide() - this.tailHide;
            return ListViewInfos.subList(fromIndex, toIndex);
        }
        
        protected void onRangeChanged() {
            
        }
        
        @Override
        protected int headHide() {
            return this.headHide;
        }

        @Override
        protected int tailHide() {
            return this.tailHide;
        }
        
        @Override
        protected void bubbleModifying(final ListElementEvent<E> e) {
            AbstractMAList<E> parentList = this.parentList;
            ListElementEvent<E> bubbledEvent = new ListElementEvent<>(
                    parentList, 
                    new Cause(e), 
                    null, 
                    version -> {
                        int offset = this.viewInfo().getFromIndex();
                        if (version == PropertyVersion.ATTACH) {
                            NavigableSet<Integer> conflictAbsIndexes = 
                                    e.getConflictAbsoluteIndexes();
                            if (!conflictAbsIndexes.isEmpty()) {
                                offset -= 
                                        conflictAbsIndexes.subSet(
                                            this.headHide - offset, 
                                            true, 
                                            this.headHide, 
                                            false
                                        )
                                        .size();
                            }
                        }
                        return e.getIndex(version) + offset;
                    }
            );
            parentList.executeModifying(bubbledEvent);
        }
        
        @Override
        protected void bubbleModified(ListElementEvent<E> e) {
            AbstractMAList<E> parentList = this.parentList;
            ListElementEvent<E> bubbleEvent = e.getBubbledEvent(parentList);
            parentList.executeModified(bubbleEvent);
        }

        @Override
        public int size() {
            this.checkConcurrentModification();
            return super.size();
        }

        @Override
        public boolean isEmpty() {
            this.checkConcurrentModification();
            return super.isEmpty();
        }

        @Override
        public int indexOf(Object o) {
            this.checkConcurrentModification();
            return super.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            this.checkConcurrentModification();
            return super.lastIndexOf(o);
        }

        @Override
        public boolean contains(Object o) {
            this.checkConcurrentModification();
            return super.contains(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            this.checkConcurrentModification();
            return super.containsAll(c);
        }

        @Override
        public E get(int index) {
            this.checkConcurrentModification();
            return super.get(index);
        }

        @Override
        public E set(int index, E element) {
            this.checkConcurrentModification();
            try {
                return super.set(index, element);
            } finally {
                this.syncModCount();
            }
        }

        @Override
        public boolean add(E e) {
            this.checkConcurrentModification();
            try {
                return super.add(e);
            } finally {
                this.syncModCount();
            }
        }

        @Override
        public void add(int index, E element) {
            this.checkConcurrentModification();
            try {
                super.add(index, element);
            } finally {
                this.syncModCount();
            }
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            this.checkConcurrentModification();
            try {
                return super.addAll(c);
            } finally {
                this.syncModCount();
            }
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            this.checkConcurrentModification();
            try {
                return super.addAll(index, c);
            } finally {
                this.syncModCount();
            }
        }

        @Override
        public void clear() {
            this.checkConcurrentModification();
            try {
                super.clear();
            } finally {
                this.syncModCount();
            }
        }

        @Override
        public MAListIterator<E> iterator() {
            this.checkConcurrentModification();
            return super.iterator();
        }

        @Override
        public MAListIterator<E> listIterator() {
            this.checkConcurrentModification();
            return super.listIterator();
        }

        @Override
        public MAListIterator<E> listIterator(int index) {
            this.checkConcurrentModification();
            return super.listIterator(index);
        }

        @Override
        public E remove(int index) {
            this.checkConcurrentModification();
            try {
                return super.remove(index);
            } finally {
                this.syncModCount();
            }
        }

        @Override
        public boolean remove(Object o) {
            this.checkConcurrentModification();
            try {
                return super.remove(o);
            } finally {
                this.syncModCount();
            }
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            this.checkConcurrentModification();
            try {
                return super.removeAll(c);
            } finally {
                this.syncModCount();
            }
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            this.checkConcurrentModification();
            try {
                return super.retainAll(c);
            } finally {
                this.syncModCount();
            }
        }

        @Override
        public MAListView<E> subList(int fromIndex, int toIndex) {
            this.checkConcurrentModification();
            return super.subList(fromIndex, toIndex);
        }

        private void checkConcurrentModification() {
            if (this.expectedModCount != this.baseElements.modCount()) {
                throw new ConcurrentModificationException(viewBecameInvalid(this.getClass()));
            }
        }
        
        private void syncModCount() {
            this.expectedModCount = this.baseElements.modCount();
            AbstractMAList<E> parentList = this.parentList;
            if (parentList instanceof SubListImpl<?>) {
                ((SubListImpl<E>)parentList).syncModCount();
            }
        }
        
        @Override
        BaseElementsConflictHandler createBaseElementsRangeChangeHandler() {
            return new BaseElementsConflictHandler() {
                @Override
                public Object resovling(int absSize, NavigableSet<Integer> absIndexes) {
                    return SubListImpl.this.rangeChanging(absSize, absIndexes);
                }
                @Override
                public void resolved(Object retValOfResolving) {
                    if (retValOfResolving != null) {
                        SubListImpl.this.rangeChanged((SubListNewRange)retValOfResolving);
                    }
                }
            };
        }
        
        private SubListNewRange rangeChanging(int oldAbsSize, NavigableSet<Integer> conflictAbsIndexes) {
            AbstractMAList<E> parentList = this.parentList;
            int newHeadHide = this.headHide - conflictAbsIndexes.headSet(this.headHide).size();
            int newTailHide = this.tailHide - conflictAbsIndexes.tailSet(oldAbsSize - this.tailHide).size();
            if (newHeadHide != this.headHide || newTailHide != this.tailHide) {
                SubListNewRange parent;
                if (parentList instanceof SubListImpl<?>) {
                    parent = ((SubListImpl<E>)parentList).rangeChanging(oldAbsSize, conflictAbsIndexes);
                } else {
                    parent = null;
                }
                return new SubListNewRange(
                        newHeadHide,
                        newTailHide,
                        parent);
            }
            return null;
        }
        
        private void rangeChanged(SubListNewRange subListNewRange) {
            SubListNewRange parent = subListNewRange.parent;
            if (parent != null) {
                AbstractMAList<E> parentList = this.parentList;
                if (parentList instanceof SubListImpl<?>) {
                    ((SubListImpl<E>)parentList).rangeChanged(parent);
                }
            }
            this.headHide = subListNewRange.headHide;
            this.tailHide = subListNewRange.tailHide;
        }
        
    }
    
    private class HandlerImpl4List implements BaseElementsHandler<E> {
        
        private final Modification<E> modification;
        
        HandlerImpl4List(Modification<E> modification) {
            this.modification = modification;
        }
        
        @Override
        public Object createAddingArgument(int index, E element) {
            return ListElementEvent.createAttachEvent(
                    AbstractMAList.this, 
                    this.modification, 
                    element, 
                    index
            );
        }

        @SuppressWarnings("unchecked")
        @Override
        public void adding(int index, E element, Object argument) {
            ListElementEvent<E> event = (ListElementEvent<E>)argument;
            AbstractMAList.this.executeModifying(event);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void added(int index, E element, Object argument) {
            ListElementEvent<E> event = (ListElementEvent<E>)argument;
            AbstractMAList.this.executeModified(event);
        }

        @Override
        public Object createChangingArgument(int oldIndex, int newIndex, E oldElement, E newElement) {
            return ListElementEvent.createReplaceEvent(
                    AbstractMAList.this, 
                    this.modification, 
                    oldElement, 
                    newElement, 
                    oldIndex,
                    newIndex
            );
        }

        @SuppressWarnings("unchecked")
        @Override
        public void changing(int oldIndex, int newIndex, E oldElement, E newElement, Object argument) {
            ListElementEvent<E> event = (ListElementEvent<E>)argument;
            AbstractMAList.this.executeModifying(event);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void changed(int oldIndex, int newIndex, E oldElement, E newElement, Object argument) {
            ListElementEvent<E> event = (ListElementEvent<E>)argument;
            AbstractMAList.this.executeModified(event);
        }

        @Override
        public Object createRemovingArgument(int index, E element) {
            return ListElementEvent.createDetachEvent(
                    AbstractMAList.this, 
                    this.modification, 
                    element, 
                    index
            );
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removing(int index, E element, Object argument) {
            ListElementEvent<E> event = (ListElementEvent<E>)argument;
            AbstractMAList.this.executeModifying(event);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removed(int index, E element, Object argument) {
            ListElementEvent<E> event = (ListElementEvent<E>)argument;
            AbstractMAList.this.executeModified(event);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void setPreThrowable(Object argument, Throwable throwable) {
            ListElementEvent<E> event = (ListElementEvent<E>)argument;
            ((InAllChainAttributeContext)event.getAttributeContext(AttributeScope.IN_ALL_CHAIN))
            .setPreThrowable(throwable);
        }

        @Override
        public void setNullOrThrowable(Throwable nullOrThrowable) {
            if (nullOrThrowable != null) {
                ((GlobalAttributeContext)this.modification.getAttributeContext()).setThrowable(nullOrThrowable);
            } else {
                ((GlobalAttributeContext)this.modification.getAttributeContext()).success();
            }
        }

        @Override
        public void setConflictAbsIndexes(NavigableSet<Integer> conflictAbsIndexes) {
            ConflictAbsoluteIndexes.set(this.modification, conflictAbsIndexes);
        }
    }
    
    private class SpecialHandlerFactoryImpl implements BaseElementsSpecialHandlerFactory<E> {

        private static final long serialVersionUID = -7038660350192823337L;

        @Override
        public BaseElementsHandler<E> createInversedSuspendingHandler(E element) {
            return new HandlerImpl4List(ListModifications.suspendViaInversedFrozenContext(element));
        }

        @Override
        public BaseElementsHandler<E> createInversedResumingHandler() {
            return new HandlerImpl4List(ListModifications.resumeViaInversedFrozenContext());
        }

        @Override
        public BaseElementsConflictHandler createInversedResumeConflictHandler() {
            return AbstractMAList.this.createBaseElementsRangeChangeHandler();
        }
    }

    @I18N
    private static native String viewBecameInvalid(Class<?> thisType);
}
