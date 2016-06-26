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

import java.util.Comparator;

import org.babyfish.collection.MACollection;
import org.babyfish.collection.MAHashSet;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.spi.wrapper.event.AbstractElementEventDispatcher;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.data.ModificationException;
import org.babyfish.data.View;
import org.babyfish.data.ViewInfo;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.Cause;

/**
 * @author Tao Chen
 */
public abstract class AbstractWrapperMACollection<E> 
extends AbstractWrapperXCollection<E> 
implements MACollection<E> {
    
    private static final Object AK_ELEMENT_LISTENER = new Object();
    
    private transient ElementListener<E> elementListener;

    protected AbstractWrapperMACollection(MACollection<E> base) {
        super(base);
    }

    protected AbstractWrapperMACollection(
            AbstractWrapperMACollection<E> parent, ViewInfo viewInfo) {
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
    protected AbstractWrapperMACollection() {
        
    }

    @Override
    protected RootData<E> createRootData() {
        return new RootData<E>();
    }

    @Deprecated
    @Override
    protected final XCollection<E> createBaseView(
            XCollection<E> parentBase, ViewInfo viewInfo) {
        return this.createBaseView((MACollection<E>)parentBase, viewInfo);
    }
    
    protected MACollection<E> createBaseView(MACollection<E> parentBase, ViewInfo viewInfo) {
        throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
    }
    
    @Override
    protected final AbstractElementEventDispatcher<E> createEventDispatcher() {
        
        return new AbstractElementEventDispatcher<E>(this) {
            @Override
            protected boolean isDispatchable() {
                return 
                        this
                        .<AbstractWrapperMACollection<E>>getOwner()
                        .getRootData()
                        .isDispatchable();
            }

            @Override
            protected void executePreDispatchedEvent(ElementEvent<E> dispatchedEvent) {
                this.<AbstractWrapperMACollection<E>>getOwner().executeModifying(dispatchedEvent);
            }

            @Override
            protected void executePostDispatchedEvent(ElementEvent<E> dispatchedEvent) {
                this.<AbstractWrapperMACollection<E>>getOwner().executeModified(dispatchedEvent);
            }
        };
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
    
    protected void executeModifying(ElementEvent<E> e) {
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

    protected void executeModified(ElementEvent<E> e) {
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

    protected void onModifying(ElementEvent<E> e) throws Throwable {
        
    }

    protected void onModified(ElementEvent<E> e) throws Throwable {
        
    }

    protected void raiseModifying(ElementEvent<E> e) throws Throwable {
        ElementListener<E> elementListener = this.elementListener;
        if (elementListener != null) {
            e
            .getAttributeContext(AttributeScope.LOCAL)
            .addAttribute(AK_ELEMENT_LISTENER, elementListener);
            elementListener.modifying(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void raiseModified(ElementEvent<E> e) throws Throwable {
        ElementListener<E> elementListener = 
            (ElementListener<E>)
            e
            .getAttributeContext(AttributeScope.LOCAL)
            .removeAttribute(AK_ELEMENT_LISTENER);
        if (elementListener != null) {
            elementListener.modified(e);
        }
    }
    
    protected void bubbleModifying(ElementEvent<E> e) {
        AbstractWrapperMACollection<E> parent = this.getParent();
        if (parent != null) {
            ElementEvent<E> bubbledEvent = new ElementEvent<>(
                    parent, new Cause(e), null
            );
            parent.executeModifying(bubbledEvent);
        }
    }

    protected void bubbleModified(ElementEvent<E> e) {
        AbstractWrapperMACollection<E> parent = this.getParent();
        if (parent != null) {
            ElementEvent<E> bubbledEvent = e.getBubbledEvent(parent);
            parent.executeModified(bubbledEvent);
        }
    }

    @Override
    public MAIterator<E> iterator() {
        return new IteratorImpl<E>(this);
    }
    
    protected static abstract class AbstractIteratorImpl<E> 
    extends AbstractWrapperXCollection.AbstractIteratorImpl<E> 
    implements MAIterator<E> {
        
        private transient ElementListener<E> elementListener;

        public AbstractIteratorImpl(AbstractWrapperMACollection<E> owner, ViewInfo viewInfo) {
            super(owner, viewInfo);
        }

        @Override
        protected AbstractElementEventDispatcher<E> createEventDispatcher() {
            return new AbstractElementEventDispatcher<E>(this) {

                @Override
                protected boolean isDispatchable() {
                    return this
                            .<AbstractIteratorImpl<E>>getOwner()
                            .getParent()
                            .getRootData()
                            .isDispatchable();
                }

                @Override
                protected void executePreDispatchedEvent(ElementEvent<E> dispatchedEvent) {
                    this.<AbstractIteratorImpl<E>>getOwner().executeModifying(dispatchedEvent);
                }

                @Override
                protected void executePostDispatchedEvent(ElementEvent<E> dispatchedEvent) {
                    this.<AbstractIteratorImpl<E>>getOwner().executeModified(dispatchedEvent);
                }
            };
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
        
        protected void executeModifying(ElementEvent<E> e) {
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

        protected void executeModified(ElementEvent<E> e) {
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

        protected void onModifying(ElementEvent<E> e) throws Throwable {
            
        }

        protected void onModified(ElementEvent<E> e) throws Throwable {
            
        }

        protected void raiseModifying(ElementEvent<E> e) throws Throwable {
            ElementListener<E> elementListener = this.elementListener;
            if (elementListener != null) {
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .addAttribute(AK_ELEMENT_LISTENER, elementListener);
                elementListener.modifying(e);
            }
        }
        
        @SuppressWarnings("unchecked")
        protected void raiseModified(ElementEvent<E> e) throws Throwable {
            ElementListener<E> elementListener = 
                (ElementListener<E>)
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .removeAttribute(AK_ELEMENT_LISTENER);
            if (elementListener != null) {
                elementListener.modified(e);
            }
        }
        
        protected void bubbleModifying(ElementEvent<E> e) {
            AbstractWrapperMACollection<E> parent = this.<AbstractWrapperMACollection<E>>getParent();
            ElementEvent<E> bubbledEvent = new ElementEvent<>(
                    parent, new Cause(e), null
            );
            parent.executeModifying(bubbledEvent);
        }

        protected void bubbleModified(ElementEvent<E> e) {
            AbstractWrapperMACollection<E> parent = this.<AbstractWrapperMACollection<E>>getParent();
            ElementEvent<E> bubbledEvent = e.getBubbledEvent(parent);
            parent.executeModified(bubbledEvent);
        }

        @Deprecated
        @Override
        protected final XIterator<E> createBaseView(
                XCollection<E> baseParent, ViewInfo viewInfo) {
            return this.createBaseView((MACollection<E>)baseParent, viewInfo);
        }
        
        protected abstract MAIterator<E> createBaseView(
                MACollection<E> baseParent, ViewInfo viewInfo);
    }
    
    protected static class IteratorImpl<E> extends AbstractIteratorImpl<E> {

        public IteratorImpl(AbstractWrapperMACollection<E> parent) {
            super(parent, CollectionViewInfos.iterator());
        }

        @Override
        protected org.babyfish.collection.MACollection.MAIterator<E> createBaseView(
                MACollection<E> baseParent, ViewInfo viewInfo) {
            if (viewInfo instanceof CollectionViewInfos.Iterator) {
                return baseParent.iterator();
            }
            throw new IllegalArgumentException(CommonMessages.illegalViewInfo());
        }

    }
    
    protected static class RootData<E> extends AbstractWrapperXCollection.RootData<E> {

        private static final long serialVersionUID = -2405437879902227661L;
        
        public RootData() {
            
        }

        @Override
        @Deprecated
        protected final void setBase(XCollection<E> base) {
            this.setBase((MACollection<E>)base);
        }
        
        protected void setBase(MACollection<E> base) {
            super.setBase(base);
        }

        @Override
        protected MACollection<E> createDefaultBase(
                UnifiedComparator<? super E> unifiedComparator) {
            Comparator<? super E> comparator = unifiedComparator.comparator();
            if (comparator != null) {
                return new MATreeSet<E>(comparator);
            }
            return new MAHashSet<E>(unifiedComparator.equalityComparator());
        }
        
    }

}
