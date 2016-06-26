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
package org.babyfish.model.spi.reference;

import org.babyfish.data.event.AttributeScope;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.ReferenceComparator;
import org.babyfish.model.spi.reference.event.IndexedValueEvent;
import org.babyfish.model.spi.reference.event.IndexedValueListener;
import org.babyfish.model.spi.reference.event.ValueEvent;
import org.babyfish.model.spi.reference.event.modification.IndexedReferenceModifications;

/**
 * @author Tao Chen
 */
public class MAIndexedReferenceImpl<T> extends MAReferenceImpl<T> implements MAIndexedReference<T> {
    
    private static final long serialVersionUID = 525974194902018606L;

    private static final Object AK_INDEXED_VALUE_LISTENER = new Object();
    
    protected int index = INVALID_INDEX;
    
    protected transient IndexedValueListener<T> indexedValueListener;
    
    public MAIndexedReferenceImpl() {
        super();
    }

    public MAIndexedReferenceImpl(ReferenceComparator<? super T> comparator) {
        super(comparator);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void addIndexedValueListener(IndexedValueListener<? super T> listener) {
        this.indexedValueListener = IndexedValueListener.combine(
                    (IndexedValueListener)this.indexedValueListener, 
                    (IndexedValueListener<T>)listener);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void removeIndexedValueListener(IndexedValueListener<? super T> listener) {
        this.indexedValueListener = IndexedValueListener.remove(
                    (IndexedValueListener)this.indexedValueListener, 
                    (IndexedValueListener<T>)listener);
    }
    
    @Override
    public int getIndex() {
        return this.getIndex(false);
    }

    @Override
    public int getIndex(boolean absolute) {
        return absolute || this.value != null ? this.index : INVALID_INDEX;
    }

    @Override
    public T get(boolean absolute) {
        return absolute || this.index != INVALID_INDEX ? this.value : null;
    }

    @Override
    public int setIndex(final int index) {
        int oldIndex = this.index;
        if (this.index == index) {
            return index;
        }
        Arguments.mustBeGreaterThanOrEqualToValue("index", index, INVALID_INDEX);
        T oldValue = this.value;
        IndexedValueEvent<T> event = IndexedValueEvent.createReplaceEvent(
                this, 
                IndexedReferenceModifications.<T>setIndex(oldIndex), 
                oldValue, 
                oldValue,
                oldIndex,
                index);
        this.setRaw(event, reference -> { ((MAIndexedReferenceImpl<T>)reference).index = index; });
        return oldIndex;
    }

    @Override
    public T set(final T value) {
        T oldValue = this.value;
        if (oldValue == value) {
            return oldValue;
        }
        this.validate(value);
        int oldIndex = this.getIndex(true);
        IndexedValueEvent<T> event = IndexedValueEvent.createReplaceEvent(
                this, 
                IndexedReferenceModifications.set(value), 
                oldValue, 
                value,
                oldIndex,
                oldIndex);
        this.setRaw(event, reference -> { reference.value = value; });
        return oldValue;
    }

    @Override
    public T set(final int index, final T value) {
        int oldIndex = this.getIndex(true);
        T oldValue = this.value;
        if (oldIndex == index && oldValue == value) {
            return oldValue;
        }
        this.validate(value);
        IndexedValueEvent<T> event = IndexedValueEvent.createReplaceEvent(
                this, 
                IndexedReferenceModifications.set(index, value), 
                oldValue, 
                value,
                oldIndex,
                index);
        this.setRaw(event, reference -> {
            ((MAIndexedReferenceImpl<T>)reference).index = index;
            reference.value = value;
        });
        return oldValue;
    }

    @Deprecated
    @Override
    protected final void executeModifying(ValueEvent<T> e) {
        this.executeModifying((IndexedValueEvent<T>)e);
    }

    @Deprecated
    @Override
    protected final void executeModified(ValueEvent<T> e) {
        this.executeModified((IndexedValueEvent<T>)e);
    }

    @Deprecated
    @Override
    protected final void onModifying(ValueEvent<T> e) throws Throwable {
        this.onModifying((IndexedValueEvent<T>)e);
    }

    @Deprecated
    @Override
    protected final void onModified(ValueEvent<T> e) throws Throwable {
        this.onModified((IndexedValueEvent<T>)e);
    }

    @Deprecated
    @Override
    protected final void raiseModifying(ValueEvent<T> e) throws Throwable {
        this.raiseModifying((IndexedValueEvent<T>)e);
    }

    @Deprecated
    @Override
    protected final void raiseModified(ValueEvent<T> e) throws Throwable {
        this.raiseModified((IndexedValueEvent<T>)e);
    }

    protected void executeModifying(IndexedValueEvent<T> e) {
        super.executeModifying(e);
    }

    protected void executeModified(IndexedValueEvent<T> e) {
        super.executeModified(e);
    }
    
    protected void onModifying(IndexedValueEvent<T> e) throws Throwable {
        
    }
    
    protected void onModified(IndexedValueEvent<T> e) throws Throwable {
        
    }
    
    protected void raiseModifying(IndexedValueEvent<T> e) throws Throwable {
        Throwable finalThrowable = null;
        try {
            super.raiseModifying(e);
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            IndexedValueListener<T> indexedValueListener = this.indexedValueListener;
            if (indexedValueListener != null) {
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .addAttribute(AK_INDEXED_VALUE_LISTENER, indexedValueListener);
                indexedValueListener.modifying(e);
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
    protected void raiseModified(IndexedValueEvent<T> e) throws Throwable {
        Throwable finalThrowable = null;
        try {
            super.raiseModified(e);
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            IndexedValueListener<T> indexedValueListener = 
                (IndexedValueListener<T>)
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .removeAttribute(AK_INDEXED_VALUE_LISTENER);
            if (indexedValueListener != null) {
                indexedValueListener.modified(e);
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
}
