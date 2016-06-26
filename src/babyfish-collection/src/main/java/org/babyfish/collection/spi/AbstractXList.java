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

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.NavigableSet;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XList;
import org.babyfish.collection.spi.base.BaseElements;
import org.babyfish.collection.spi.base.BaseElementsConflictHandler;
import org.babyfish.collection.spi.base.BaseElementsSpecialHandlerFactory;
import org.babyfish.collection.spi.base.BaseListIterator;
import org.babyfish.collection.spi.base.BasicAlgorithms;
import org.babyfish.collection.viewinfo.ListViewInfos;
import org.babyfish.collection.viewinfo.ListViewInfos.SubList;
import org.babyfish.data.ModificationAware;
import org.babyfish.data.View;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.StatefulObject;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public abstract class AbstractXList<E> extends StatefulObject implements XList<E> {
    
    BaseElements<E> baseElements;
    
    protected AbstractXList(BaseElements<E> baseElements) {
        if (this instanceof Serializable && this instanceof View) {
            throw new IllegalProgramException(
                    CommonMessages.viewCanNotBeSerializable(
                            this.getClass(),
                            Serializable.class,
                            View.class));
        }
        this.baseElements = Arguments.mustNotBeNull("baseElements", baseElements);
        if (!(this instanceof View) && !(this instanceof ModificationAware)) {
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
    protected AbstractXList() {
        
    }
    
    @SuppressWarnings("unchecked")
    protected final <T extends BaseElements<E>> T getBaseElements() {
        return (T)this.baseElements;
    }
    
    @Override
    public boolean isReadWriteLockSupported() {
        return this.baseElements.isReadWriteLockSupported();
    }
    
    @Override
    public BidiType bidiType() {
        return this.baseElements.bidiType();
    }

    @Override
    public final UnifiedComparator<? super E> unifiedComparator() {
        return this.baseElements.unifiedComparator();
    }
    
    @Override
    public void addValidator(Validator<E> validator) {
        this.baseElements.combineValidator(validator);
    }

    @Override
    public void removeValidator(Validator<E> validator) {
        this.baseElements.removeValidator(validator);
    }

    @Override
    public void validate(E e) {
        this.baseElements.validate(e);
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public int size() {
        return this.baseElements.allSize() - this.headHide() - this.tailHide();
    }

    @Override
    public E get(int index) {
        return this.baseElements.get(this.headHide(), this.tailHide(), index);
    }

    @Override
    public int indexOf(Object o) {
        return this.baseElements.indexOf(this.headHide(), this.tailHide(), o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.baseElements.lastIndexOf(this.headHide(), this.tailHide(), o);
    }

    @Override
    public boolean contains(Object o) {
        return this.baseElements.contains(this.headHide(), this.tailHide(), o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!this.baseElements.contains(this.headHide(), this.tailHide(), o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public E set(int index, E element) {
        return this.baseElements.set(
                this.headHide(), 
                this.tailHide(), 
                index, 
                element, 
                null, 
                this.createBaseElementsRangeChangeHandler());
    }

    @Override
    public boolean add(E e) {
        this.baseElements.add(
                this.headHide(), 
                this.tailHide(), 
                this.baseElements.allSize() - this.headHide() - this.tailHide(), 
                e, 
                null,
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
                null,
                this.createBaseElementsRangeChangeHandler());
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.baseElements.addAll(
                this.headHide(), 
                this.tailHide(), 
                this.baseElements.allSize() - this.headHide() - this.tailHide(), 
                c, 
                null,
                this.createBaseElementsRangeChangeHandler());
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return this.baseElements.addAll(
                this.headHide(), 
                this.tailHide(), 
                index, 
                c, 
                null,
                this.createBaseElementsRangeChangeHandler());
    }

    @Override
    public void clear() {
        this.baseElements.clear(this.headHide(), this.tailHide(), null);
    }

    @Override
    public E remove(int index) {
        return this.baseElements.removeAt(this.headHide(), this.tailHide(), index, null);
    }

    @Override
    public boolean remove(Object o) {
        int index = this.indexOf(o);
        if (index != -1) {
            this.baseElements.removeAt(this.headHide(), this.tailHide(), index, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.baseElements.removeAll(this.headHide(), this.tailHide(), c, null);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.baseElements.retainAll(this.headHide(), this.tailHide(), c, null);
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
    public XListIterator<E> listIterator(final int index) {
        
        final BaseListIterator<E> beItr = 
            this
            .baseElements
            .listIterator(
                    this.headHide(), 
                    this.tailHide(), 
                    index, 
                    this.createBaseElementsRangeChangeHandler()
            );
        final boolean nonFairLockSupported = this.baseElements.isReadWriteLockSupported();
        final UnifiedComparator<? super E> unifiedComparator = this.baseElements.unifiedComparator();
        
        return new XListIterator<E>() {

            @Override
            public boolean hasNext() {
                return beItr.hasNext();
            }

            @Override
            public E next() {
                return beItr.next();
            }

            @Override
            public int nextIndex() {
                return beItr.nextIndex();
            }

            @Override
            public boolean hasPrevious() {
                return beItr.hasPrevious();
            }

            @Override
            public E previous() {
                return beItr.previous();
            }

            @Override
            public int previousIndex() {
                return beItr.previousIndex();
            }

            @Override
            public void remove() {
                try {
                    beItr.remove(null);
                } finally {
                    this.syncParentModCount();
                }
            }

            @Override
            public void add(E e) {
                try {
                    beItr.add(e, null);
                } finally {
                    this.syncParentModCount();
                }
            }

            @Override
            public void set(E e) {
                try {
                    beItr.set(e, null);
                } finally {
                    this.syncParentModCount();
                }
            }

            @Override
            public boolean isReadWriteLockSupported() {
                return nonFairLockSupported;
            }

            @Override
            public UnifiedComparator<? super E> unifiedComparator() {
                return unifiedComparator;
            }

            @Override
            public ListViewInfos.ListIterator viewInfo() {
                return ListViewInfos.listIterator(index);
            }
            
            private void syncParentModCount() {
                AbstractXList<E> parentList = AbstractXList.this;
                if (parentList instanceof SubListImpl) {
                    ((SubListImpl<E>)parentList).syncModCount();
                }
            }
        };
    }
    
    protected int headHide() { return 0; }
    
    protected int tailHide() { return 0; }
    
    BaseElementsConflictHandler createBaseElementsRangeChangeHandler() {
        return null;
    }
    
    @Override
    public Object[] toArray() {
        return BasicAlgorithms.collectionToArray(this);
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        return BasicAlgorithms.collectionToArray(this, a);
    }

    @Override
    public int hashCode() {
        return BasicAlgorithms.listHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return BasicAlgorithms.listEquals(this, obj);
    }

    @Override
    public String toString() {
        return BasicAlgorithms.collectionToString(this);
    }
    
    @Override
    protected void onWriteState(Output out) throws IOException {
        if (this.baseElements == null) {
            throw new IllegalStateException(operationIsTooEarly(this.getClass(), "onWriteState"));
        }
        out.writeObject(this.baseElements);
    }

    @Override
    protected void onReadState(Input in) throws ClassNotFoundException, IOException {
        if (this.baseElements != null) {
            throw new IllegalStateException(operationIsTooEarly(this.getClass(), "onReadState"));
        }
        this.baseElements = in.readObject();
    }

    protected static class SubListImpl<E> extends AbstractXList<E> implements XListView<E> {
        
        private AbstractXList<E> parentList;
        
        private int headHide;
        
        private int tailHide;
        
        private int expectedModCount;

        protected SubListImpl(AbstractXList<E> parentList, int fromIndex, int toIndex) {
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
        public SubList viewInfo() {
            AbstractXList<E> parentList = this.parentList;
            int fromIndex = this.headHide - parentList.headHide();
            int toIndex = this.baseElements.allSize() - parentList.headHide() - this.tailHide;
            return ListViewInfos.subList(fromIndex, toIndex);
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
        public XListIterator<E> iterator() {
            return this.listIterator(0);
        }

        @Override
        public XListIterator<E> listIterator() {
            return this.listIterator(0);
        }

        @Override
        public XListIterator<E> listIterator(int index) {
            this.checkConcurrentModification();
            return super.listIterator(index);
        }

        @Override
        public XListView<E> subList(int fromIndex, int toIndex) {
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
            AbstractXList<E> parentList = this.parentList;
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
            AbstractXList<E> parentList = this.parentList;
            int newHeadHide = this.headHide - conflictAbsIndexes.headSet(this.headHide).size();
            int newTailHide = this.tailHide - conflictAbsIndexes.tailSet(oldAbsSize - this.tailHide).size();
            if (newHeadHide != this.headHide || newTailHide != this.tailHide) {
                SubListNewRange parent;
                if (parentList instanceof AbstractXList.SubListImpl) {
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
                AbstractXList<E> parentList = this.parentList;
                if (parentList instanceof AbstractXList.SubListImpl) {
                    ((SubListImpl<E>)parentList).rangeChanged(parent);
                }
            }
            this.headHide = subListNewRange.headHide;
            this.tailHide = subListNewRange.tailHide;
        }
        
    }

    public static final class SimpleImpl<E> extends AbstractXList<E> {

        public SimpleImpl(BaseElements<E> baseElements) {
            super(baseElements);
        }

    }
    
    static final class SubListNewRange {
        
        final int headHide;
        
        final int tailHide;
        
        final SubListNewRange parent;

        public SubListNewRange(int headHide, int tailHide, SubListNewRange parent) {
            this.headHide = headHide;
            this.tailHide = tailHide;
            this.parent = parent;
        }
    }
    
    private class SpecialHandlerFactoryImpl implements BaseElementsSpecialHandlerFactory<E> {

        private static final long serialVersionUID = -5962730153075195182L;

        @Override
        public BaseElementsConflictHandler createInversedResumeConflictHandler() {
            return AbstractXList.this.createBaseElementsRangeChangeHandler();
        }
    }
    
    @I18N
    private static native String operationIsTooEarly(Class<?> thisType, String operationName);
        
    @I18N
    private static native String viewBecameInvalid(Class<?> thisType);
}
