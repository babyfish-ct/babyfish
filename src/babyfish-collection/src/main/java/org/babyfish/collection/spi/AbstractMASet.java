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

import org.babyfish.collection.MASet;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.event.KeySetElementEvent;
import org.babyfish.collection.event.KeySetElementModificationAware;
import org.babyfish.collection.event.modification.CollectionModifications;
import org.babyfish.collection.event.modification.IteratorModifications;
import org.babyfish.collection.event.modification.SetModifications;
import org.babyfish.collection.spi.base.BaseEntries;
import org.babyfish.collection.spi.base.BaseEntriesHandler;
import org.babyfish.collection.spi.base.BaseEntriesSpecialHandlerFactory;
import org.babyfish.collection.spi.base.BaseEntryIterator;
import org.babyfish.collection.spi.base.DescendingBaseEntries;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.data.ModificationException;
import org.babyfish.data.View;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.Cause;
import org.babyfish.data.event.spi.GlobalAttributeContext;
import org.babyfish.data.event.spi.InAllChainAttributeContext;
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public abstract class AbstractMASet<E> 
extends AbstractXSet<E> 
implements MASet<E> {
    
    private static final EventCreator<?> EVENT_CREATOR_4_SET =
            new EventCreatorImpl4Set<>();
    
    private static final Object AK_ELEMENT_LISTENER = new Object();
    
    protected transient ElementListener<E> elementListener;
    
    protected AbstractMASet(BaseEntries<E, Object> baseEntries) {
        super(baseEntries);
        if (!(this instanceof View)) {
            baseEntries.initSpecialHandlerFactory(
                    this.new SpecialHandlerFactoryImpl()
            );
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
    protected AbstractMASet() {
        
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
        
    }

    protected void bubbleModified(ElementEvent<E> e) {
        
    }
    
    @SuppressWarnings("unchecked")
    protected final EventCreator<E> eventCreator() {
        return this instanceof KeySetElementModificationAware<?, ?> ?
                this.new EventCreatorImpl4KeySet() :
                (EventCreator<E>)EVENT_CREATOR_4_SET;
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
    
    @Override
    public boolean add(E e) {
        return null == this.baseEntries.put(
                e, 
                BaseEntries.PRESENT, 
                this.new HandlerImpl4Set(CollectionModifications.add(e)));
    }
    
    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.baseEntries.addAll(
                c,
                this.new HandlerImpl4Set(CollectionModifications.addAll(c)));
    }

    @Override
    public void clear() {
        this.baseEntries.clear(
                this.new HandlerImpl4Set(CollectionModifications.<E>clear()));
    }

    @Override
    public boolean remove(Object o) {
        return null != this.baseEntries.removeByKey(
                o, 
                this.new HandlerImpl4Set(CollectionModifications.<E>remove(o)));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.baseEntries.removeAllByKeyCollection(
                c, 
                this.new HandlerImpl4Set(CollectionModifications.<E>removeAll(c)));
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        return this.baseEntries.retainAllByKeyCollection(
                c, 
                this.new HandlerImpl4Set(CollectionModifications.<E>retainAll(c)));
    }
    
    @Override
    public MAIterator<E> iterator() {
        return new IteratorImpl<E>(this);
    }

    protected interface EventCreator<E> {
        
        ElementEvent<E> createDetachEvent(Object source, Modification<E> modification, E element, Object value);
        
        ElementEvent<E> createAttachEvent(Object source, Modification<E> modification, E element, Object value);
        
        ElementEvent<E> createBubbleEvent(Object source, ElementEvent<E> e);
    }
    
    protected static abstract class AbstractIteratorImpl<E> implements MAIterator<E> {
        
        private AbstractMASet<E> parentSet;
        
        private BaseEntryIterator<E, Object> beIterator;
        
        private transient ElementListener<E> elementListener;
        
        protected AbstractIteratorImpl(AbstractMASet<E> parentSet, boolean descending) {
            this.parentSet = parentSet;
            BaseEntries<E, Object> baseEntries = parentSet.baseEntries;
            if (descending) {
                if (!(baseEntries instanceof DescendingBaseEntries<?, ?>)) {
                    throw new IllegalStateException(
                            CommonMessages.illegalDescendingOnNonDescendingSet(
                                    DescendingBaseEntries.class
                            )
                    );
                }
                baseEntries = 
                    ((DescendingBaseEntries<E, Object>)baseEntries)
                    .descendingEntries();
            }
            this.beIterator = baseEntries.iterator();
        }
        
        @SuppressWarnings("unchecked")
        protected final <T extends AbstractMASet<E>> T getParentSet() {
            return (T)this.parentSet;
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
            AbstractMASet<E> parentSet = this.parentSet;
            ElementEvent<E> bubbledEvent = 
                parentSet.eventCreator().createBubbleEvent(parentSet, e);
            parentSet.executeModifying(bubbledEvent);
        }
        
        protected void bubbleModified(ElementEvent<E> e) {
            AbstractMASet<E> parentSet = this.parentSet;
            ElementEvent<E> bubbleEvent = e.getBubbledEvent(parentSet);
            parentSet.executeModified(bubbleEvent);
        }

        @Override
        public boolean hasNext() {
            return this.beIterator.hasNext();
        }

        @Override
        public E next() {
            return this.beIterator.next().getKey();
        }

        @Override
        public void remove() {
            this.beIterator.remove(this.new HandlerImpl4Iterator());
        }

        @Override
        public boolean isReadWriteLockSupported() {
            return this.parentSet.isReadWriteLockSupported();
        }

        @Override
        public UnifiedComparator<? super E> unifiedComparator() {
            return this.parentSet.unifiedComparator();
        }

        private class HandlerImpl4Iterator implements BaseEntriesHandler<E, Object> {
            
            private Modification<E> modification = IteratorModifications.remove();

            @Override
            public Object createRemovingArgument(E oldKey, Object oldValue) {
                return AbstractIteratorImpl.this.parentSet.eventCreator().createDetachEvent(
                        AbstractIteratorImpl.this, 
                        this.modification, 
                        oldKey, 
                        oldValue);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void removing(E oldKey, Object oldValue, Object argument) {
                ElementEvent<E> e = (ElementEvent<E>)argument;
                AbstractIteratorImpl.this.executeModifying(e);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void removed(E oldKey, Object oldValue, Object argument) {
                ElementEvent<E> e = (ElementEvent<E>)argument;
                AbstractIteratorImpl.this.executeModified(e);
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public void setPreThrowable(Object argument, Throwable throwable) {
                ElementEvent<E> event = (ElementEvent<E>)argument;
                ((InAllChainAttributeContext)event.getAttributeContext(AttributeScope.IN_ALL_CHAIN))
                .setPreThrowable(throwable);
            }

            @Override
            public void setNullOrThrowable(Throwable throwable) {
                if (throwable != null) {
                    ((GlobalAttributeContext)this.modification.getAttributeContext()).setThrowable(throwable);
                } else {
                    ((GlobalAttributeContext)this.modification.getAttributeContext()).success();    
                }
            }
        }
        
    }
    
    protected static class IteratorImpl<E> extends AbstractIteratorImpl<E> {
        
        protected IteratorImpl(AbstractMASet<E> parentSet) {
            super(parentSet, false);
        }

        @Override
        public CollectionViewInfos.Iterator viewInfo() {
            return CollectionViewInfos.iterator();
        }
    }
    
    final class HandlerImpl4Set implements BaseEntriesHandler<E, Object> {
        
        private final Modification<E> modification;
        
        private final boolean handleChange;
        
        public HandlerImpl4Set(Modification<E> modification) {
            this.modification = modification;
            AbstractMASet<E> owner = AbstractMASet.this;
            this.handleChange = 
                owner.baseEntries.keyReplacementRule() == 
                ReplacementRule.NEW_REFERENCE_WIN
                &&
                !(owner instanceof KeySetElementModificationAware<?, ?>);
        }

        @Override
        public Object createAddingArgument(E key, Object value) {
            AbstractMASet<E> owner = AbstractMASet.this;
            return owner.eventCreator().createAttachEvent(
                    owner, 
                    this.modification, 
                    key, 
                    value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void adding(E key, Object value, Object argument) {
            ElementEvent<E> e = (ElementEvent<E>)argument; 
            AbstractMASet.this.executeModifying(e);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void added(E oldKey, Object oldValue, Object argument) {
            ElementEvent<E> e = (ElementEvent<E>)argument;
            AbstractMASet.this.executeModified(e);
        }

        @Override
        public Object createChangingArgument(E oldKey, Object oldValue,
                E newKey, Object newValue) {
            if (this.handleChange) {
                return ElementEvent.createReplaceEvent(
                        AbstractMASet.this, 
                        this.modification, 
                        oldKey, 
                        newKey);
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void changing(E oldKey, Object oldValue, E newKey, Object newValue, Object argument) {
            ElementEvent<E> e = (ElementEvent<E>)argument;
            if (e != null) {
                AbstractMASet.this.executeModifying(e);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void changed(E oldKey, Object oldValue, E newKey, Object newValue, Object argument) {
            ElementEvent<E> e = (ElementEvent<E>)argument;
            if (e != null) {
                AbstractMASet.this.executeModified(e);
            }
        }

        @Override
        public Object createRemovingArgument(E oldKey, Object oldValue) {
            AbstractMASet<E> owner = AbstractMASet.this;
            return owner.eventCreator().createDetachEvent(
                    owner, 
                    this.modification, 
                    oldKey, 
                    oldValue);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removing(E oldKey, Object oldValue, Object argument) {
            ElementEvent<E> e = (ElementEvent<E>)argument;
            AbstractMASet.this.executeModifying(e);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void removed(E oldKey, Object oldValue, Object argument) {
            ElementEvent<E> e = (ElementEvent<E>)argument;
            AbstractMASet.this.executeModified(e);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void setPreThrowable(Object argument, Throwable throwable) {
            ElementEvent<E> event = (ElementEvent<E>)argument;
            ((InAllChainAttributeContext)event.getAttributeContext(AttributeScope.IN_ALL_CHAIN))
            .setPreThrowable(throwable);
        }

        @Override
        public void setNullOrThrowable(Throwable throwable) {
            if (throwable != null) {
                ((GlobalAttributeContext)this.modification.getAttributeContext()).setThrowable(throwable);
            } else {
                ((GlobalAttributeContext)this.modification.getAttributeContext()).success();    
            }
        }
    }
    
    private static class EventCreatorImpl4Set<E> implements EventCreator<E> {

        @Override
        public ElementEvent<E> createDetachEvent(Object source,
                Modification<E> modification, E element, Object value) {
            return ElementEvent.createDetachEvent(source, modification, element);
        }

        @Override
        public ElementEvent<E> createAttachEvent(Object source,
                Modification<E> modification, E element, Object value) {
            return ElementEvent.createAttachEvent(source, modification, element);
        }

        @Override
        public ElementEvent<E> createBubbleEvent(Object source, ElementEvent<E> e) {
            return new ElementEvent<>(source, new Cause(e), null);
        }
    }
    
    private class EventCreatorImpl4KeySet implements EventCreator<E> {
        
        @Override
        public KeySetElementEvent<E, Object> createDetachEvent(Object source,
                Modification<E> modification, E element, Object value) {
            return KeySetElementEvent.createDetachEvent(source, modification, element, value);
        }

        @Override
        public KeySetElementEvent<E, Object> createAttachEvent(Object source,
                Modification<E> modification, E element, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public KeySetElementEvent<E, Object> createBubbleEvent(Object source, ElementEvent<E> e) {
            Arguments.mustBeInstanceOfValue("e", e, KeySetElementEvent.class);
            return new KeySetElementEvent<>(
                    source, 
                    new Cause(e), 
                    null, 
                    null);
        }
    }
    
    private class SpecialHandlerFactoryImpl implements BaseEntriesSpecialHandlerFactory<E, Object> {

        private static final long serialVersionUID = -1808843657376861934L;

        @Override
        public BaseEntriesHandler<E, Object> createSuspendingHandler(E key) {
            return AbstractMASet.this.new HandlerImpl4Set(
                    SetModifications.suspendViaFrozenContext(key)
            );
        }

        @Override
        public BaseEntriesHandler<E, Object> createResumingHandler() {
            return AbstractMASet.this.new HandlerImpl4Set(
                    SetModifications.resumeViaFrozenContext()
            );
        }
    }
}
