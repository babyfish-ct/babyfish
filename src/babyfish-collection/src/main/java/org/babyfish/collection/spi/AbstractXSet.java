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

import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XMap.XKeySetView;
import org.babyfish.collection.XSet;
import org.babyfish.collection.spi.base.BaseEntries;
import org.babyfish.collection.spi.base.BaseEntryIterator;
import org.babyfish.collection.spi.base.BasicAlgorithms;
import org.babyfish.collection.spi.base.TransientValueEntries;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.data.View;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.StatefulObject;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public abstract class AbstractXSet<E> extends StatefulObject implements XSet<E> {
    
    BaseEntries<E, Object> baseEntries;
    
    protected AbstractXSet(BaseEntries<E, Object> baseEntries) {
        if (this instanceof Serializable && this instanceof View) {
            throw new IllegalProgramException(
                    CommonMessages.viewCanNotBeSerializable(
                            this.getClass(),
                            Serializable.class,
                            View.class));
        }
        if (!(this instanceof View)) {
            if (this instanceof XMap.XKeySetView) {
                Arguments.mustNotBeInstanceOfValueWhen(
                        whenThisIsInstanceofXKeySetView(XKeySetView.class), 
                        "baseEntries", 
                        baseEntries, 
                        TransientValueEntries.class);
            } else {
                Arguments.mustBeInstanceOfValueWhen(
                        whenThisIsNotInstanceofXKeySetView(XKeySetView.class),
                        "baseEntries", 
                        baseEntries, 
                        TransientValueEntries.class);
            }
        }
        this.baseEntries = Arguments.mustNotBeNull("baseEntries", baseEntries);
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
    protected AbstractXSet() {
        
    }
    
    @SuppressWarnings("unchecked")
    protected final <T extends BaseEntries<E, Object>> T getBaseEntries() {
        return (T)this.baseEntries;
    }
    
    @Override
    public boolean isReadWriteLockSupported() {
        return this.baseEntries.isReadWriteLockSupported();
    }

    @Override
    public final ReplacementRule replacementRule() {
        return this.baseEntries.keyReplacementRule();
    }

    @Override
    public final UnifiedComparator<? super E> unifiedComparator() {
        return this.baseEntries.keyUnifiedComparator();
    }

    @Override
    public void addValidator(Validator<E> validator) {
        this.baseEntries.combineKeyValidator(validator);
    }

    @Override
    public void removeValidator(Validator<E> validator) {
        this.baseEntries.removeKeyValidator(validator);
    }

    @Override
    public void validate(E e) {
        this.baseEntries.validateKey(e);
    }

    @Override
    public boolean isEmpty() {
        return this.baseEntries.isEmpty();
    }

    @Override
    public int size() {
        return this.baseEntries.size();
    }
    
    @Override
    public boolean contains(Object o) {
        return this.baseEntries.containsKey(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        BaseEntries<E, Object> baseEntries = this.baseEntries;
        for (Object e : c) {
            if (!baseEntries.containsKey(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean add(E e) {
        return this.baseEntries.put(e, BaseEntries.PRESENT, null) == null;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.baseEntries.addAll(c, null);
    }

    @Override
    public void clear() {
        this.baseEntries.clear(null);
    }

    @Override
    public boolean remove(Object o) {
        return this.baseEntries.removeByKey(o, null) != null;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.baseEntries.removeAllByKeyCollection(c, null);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.baseEntries.retainAllByKeyCollection(c, null);
    }
    
    @Override
    public XIterator<E> iterator() {
        final BaseEntryIterator<E, Object> beIterator = this.baseEntries.iterator();
        final boolean nonFairLockSupported = this.baseEntries.isReadWriteLockSupported();
        final UnifiedComparator<? super E> unifiedComparator = this.baseEntries.keyUnifiedComparator();
        return new XIterator<E>() {
            @Override
            public boolean hasNext() {
                return beIterator.hasNext();
            }
            @Override
            public E next() {
                return beIterator.next().getKey();
            }
            @Override
            public void remove() {
                beIterator.remove(null);
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
            public CollectionViewInfos.Iterator viewInfo() {
                return CollectionViewInfos.iterator();
            }
        };
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
        return BasicAlgorithms.setHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return BasicAlgorithms.setEquals(this, obj);
    }

    @Override
    public String toString() {
        return BasicAlgorithms.collectionToString(this);
    }
    
    @Override
    protected void onWriteState(Output out) throws IOException {
        if (this.baseEntries == null) {
            throw new IllegalStateException(operationIsTooEarly(this.getClass(), "onWriteState"));
        }
        out.writeObject(this.baseEntries);
    }

    @Override
    protected void onReadState(Input in) throws ClassNotFoundException, IOException {
        if (this.baseEntries != null) {
            throw new IllegalStateException(operationIsTooEarly(this.getClass(), "onWriteState"));
        }
        this.baseEntries = in.readObject();
    }
    
    @I18N
    private static native String operationIsTooEarly(Class<?> thisType, String operationName);

    @SuppressWarnings("rawtypes")
    @I18N
    private static native String whenThisIsInstanceofXKeySetView(Class<XKeySetView> keySetViewType);

    @SuppressWarnings("rawtypes")
    @I18N
    private static native String whenThisIsNotInstanceofXKeySetView(Class<XKeySetView> keySetViewType);
}
