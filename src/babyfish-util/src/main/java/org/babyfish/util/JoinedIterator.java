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
package org.babyfish.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
public class JoinedIterator<E> implements Iterator<E> {
    
    @SuppressWarnings("unchecked")
    private static final JoinedIterator<?> EMPTY = 
        new JoinedIterator<Object>(new Iterator[0], 0);
    
    private Iterator<E>[] iterators;
    
    private int length;

    private int currentIteratorIndex;

    private Iterator<E> lastUsedIterator;
    
    JoinedIterator(Iterator<E>[] iterators, int length) {
        this.iterators = iterators;
        this.length = length;
    }
    
    @SuppressWarnings("unchecked")
    public JoinedIterator<E> join(Collection<Iterator<? extends E>> iterators) {
        if (iterators != null) {
            int len = this.length;
            Iterator<E>[] arr = new Iterator[len + iterators.size()];
            for (Iterator<? extends E> itr : iterators) {
                if (itr != null) {
                    arr[len++] = (Iterator<E>)itr;
                }
            }
            if (len != this.length) {
                System.arraycopy(this.iterators, 0, arr, 0, this.length);
                return new JoinedIterator<E>(arr, len);
            }
        }
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public JoinedIterator<E> join(Iterator<? extends E> ... iterators) {
        if (iterators == null || iterators.length == 0) {
            return this;
        }
        return this.join(MACollections.wrap(iterators));
    }
    
    @SuppressWarnings("unchecked")
    public static <E> JoinedIterator<E> empty() {
        return (JoinedIterator<E>)EMPTY;
    }

    @Override
    public boolean hasNext() {
        Iterator<E>[] arr = this.iterators;
        while (this.currentIteratorIndex < this.length) {
            if (arr[this.currentIteratorIndex].hasNext()) {
                return true;
            }
            this.currentIteratorIndex++;
        }
        return false;
    }

    @Override
    public E next() {
        if (this.hasNext()) {
            return (this.lastUsedIterator = this.iterators[this.currentIteratorIndex]).next(); 
        }
        throw new NoSuchElementException(noSuchElement());
    }

    @Override
    public void remove() {
        Iterator<E> lastUsedIterator = this.lastUsedIterator;
        if (lastUsedIterator != null) {
            this.lastUsedIterator = null;
            lastUsedIterator.remove();
        }
        throw new IllegalStateException(removeNoExtractedElement());
    }

    @I18N
    private static native String noSuchElement();
        
    @I18N
    private static native String removeNoExtractedElement();
}
