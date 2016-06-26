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

import org.babyfish.collection.UnifiedComparator;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.lang.ReferenceComparator;
import org.babyfish.model.spi.reference.event.KeyedValueEvent;
import org.babyfish.model.spi.reference.event.KeyedValueEvent.KeyedModification;
import org.babyfish.model.spi.reference.event.KeyedValueListener;
import org.babyfish.model.spi.reference.event.ValueEvent;
import org.babyfish.model.spi.reference.event.modification.KeyedReferenceModifications;

/**
 * @author Tao Chen
 */
public class MAKeyedReferenceImpl<K, T> extends MAReferenceImpl<T> implements MAKeyedReference<K, T> {
    
    private static final long serialVersionUID = 604216877247897556L;

    private static final Object AK_KEYED_VALUE_LISTENER = new Object();
    
    private UnifiedComparator<? super K> keyComparator;
    
    protected K key;
    
    protected transient KeyedValueListener<K, T> keyedValueListener;
    
    public MAKeyedReferenceImpl() {
        super();
    }

    public MAKeyedReferenceImpl(UnifiedComparator<? super K> keyComparator, ReferenceComparator<? super T> comparator) {
        super(comparator);
        this.keyComparator = keyComparator;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void addKeyedValueListener(KeyedValueListener<?super K, ? super T> listener) {
        this.keyedValueListener = KeyedValueListener.combine(
                    (KeyedValueListener)this.keyedValueListener, 
                    (KeyedValueListener<K, T>)listener);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void removeKeyedValueListener(KeyedValueListener<?super K, ? super T> listener) {
        this.keyedValueListener = KeyedValueListener.remove(
                    (KeyedValueListener)this.keyedValueListener, 
                    (KeyedValueListener<K, T>)listener);
    }
    
    @Override
    public UnifiedComparator<? super K> keyComparator() {
        return this.keyComparator;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.containsKey(key, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key, boolean absolute) {
        K thisKey = this.getKey(absolute);
        if (thisKey == null) {
            return key == null;
        }
        if (thisKey == key) {
            return true;
        }
        if (this.keyComparator == null) {
            return thisKey.equals(key);
        }
        return this.keyComparator().equals(thisKey, ((K)key));
    }
    
    @Override
    public K getKey() {
        return this.getKey(false);
    }

    @Override
    public K getKey(boolean absolute) {
        return absolute || this.value != null ? this.key : null;
    }

    @Override
    public T get(boolean absolute) {
        return absolute || this.key != null ? this.value : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public K setKey(final K key) {
        K oldKey = this.key;
        if (oldKey == key) {
            return oldKey;
        }
        T oldValue = this.value;
        KeyedValueEvent<K, T> event = KeyedValueEvent.createReplaceEvent(
                this, 
                KeyedReferenceModifications.<K, T>setKey(oldKey),
                oldValue, 
                oldValue,
                oldKey,
                key);
        this.setRaw(event, reference -> { ((MAKeyedReferenceImpl<K, T>)reference).key = key; });
        return oldKey;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T set(final T value) {
        T oldValue = this.value;
        if (oldValue == value) {
            return oldValue;
        }
        this.validate(value);
        K oldKey = this.getKey(true);
        KeyedValueEvent<K, T> event = KeyedValueEvent.createReplaceEvent(
                this, 
                (KeyedModification<K, T>)KeyedReferenceModifications.set(value),
                oldValue, 
                value,
                oldKey,
                oldKey
        );
        this.setRaw(event, reference -> { reference.value = value; });
        return oldValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T set(final K key, final T value) {
        K oldKey = this.getKey(true);
        T oldValue = this.value;
        if (oldKey == key && oldValue == value) {
            return oldValue;
        }
        this.validate(value);
        KeyedValueEvent<K, T> event = KeyedValueEvent.createReplaceEvent(
                this, 
                KeyedReferenceModifications.set(key, value), 
                oldValue, 
                value,
                oldKey,
                key);
        this.setRaw(event, reference -> {
            ((MAKeyedReferenceImpl<K, T>)reference).key = key;
            reference.value = value;
        });
        return oldValue;
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @Override
    protected final void executeModifying(ValueEvent<T> e) {
        this.executeModifying((KeyedValueEvent<K, T>)e);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @Override
    protected final void executeModified(ValueEvent<T> e) {
        this.executeModified((KeyedValueEvent<K, T>)e);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @Override
    protected final void onModifying(ValueEvent<T> e) throws Throwable {
        this.onModifying((KeyedValueEvent<K, T>)e);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @Override
    protected final void onModified(ValueEvent<T> e) throws Throwable {
        this.onModified((KeyedValueEvent<K, T>)e);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @Override
    protected final void raiseModifying(ValueEvent<T> e) throws Throwable {
        this.raiseModifying((KeyedValueEvent<K, T>)e);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @Override
    protected final void raiseModified(ValueEvent<T> e) throws Throwable {
        this.raiseModified((KeyedValueEvent<K, T>)e);
    }

    protected void executeModifying(KeyedValueEvent<K, T> e) {
        super.executeModifying(e);
    }

    protected void executeModified(KeyedValueEvent<K, T> e) {
        super.executeModified(e);
    }
    
    protected void onModifying(KeyedValueEvent<K, T> e) throws Throwable {
        
    }
    
    protected void onModified(KeyedValueEvent<K, T> e) throws Throwable {
        
    }
    
    protected void raiseModifying(KeyedValueEvent<K, T> e) throws Throwable {
        Throwable finalThrowable = null;
        try {
            super.raiseModifying(e);
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            KeyedValueListener<K, T> keyedValueListener = this.keyedValueListener;
            if (keyedValueListener != null) {
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .addAttribute(AK_KEYED_VALUE_LISTENER, keyedValueListener);
                keyedValueListener.modifying(e);
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
    protected void raiseModified(KeyedValueEvent<K, T> e) throws Throwable {
        Throwable finalThrowable = null;
        try {
            super.raiseModified(e);
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            KeyedValueListener<K, T> keyedValueListener = 
                (KeyedValueListener<K, T>)
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .removeAttribute(AK_KEYED_VALUE_LISTENER);
            if (keyedValueListener != null) {
                keyedValueListener.modified(e);
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
