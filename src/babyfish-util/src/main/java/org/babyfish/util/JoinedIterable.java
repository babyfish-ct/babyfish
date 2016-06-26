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

import org.babyfish.collection.MACollections;

/**
 * @author Tao Chen
 */
public class JoinedIterable<E> implements Iterable<E> {

    @SuppressWarnings("unchecked")
    private static final JoinedIterable<?> EMPTY =
        new JoinedIterable<Object>(new Iterable[0], 0);
    
    private Iterable<E>[] iterables;
    
    private int length;

    private JoinedIterable(Iterable<E>[] iterables, int length) {
        this.iterables = iterables;
        this.length = length;
    }
    
    @SuppressWarnings("unchecked")
    public static <E> JoinedIterable<E> empty() {
        return (JoinedIterable<E>)EMPTY;
    }
    
    @SuppressWarnings("unchecked")
    public JoinedIterable<E> join(Collection<? extends Iterable<? extends E>> iterables) {
        if (iterables != null) {
            int len = this.length;
            Iterable<E>[] arr = new Iterable[len + iterables.size()];
            for (Iterable<? extends E> itr : iterables) {
                if (itr != null) {
                    arr[len++] = (Iterable<E>)itr;
                }
            }
            if (len != this.length) {
                System.arraycopy(this.iterables, 0, arr, 0, this.length);
                return new JoinedIterable<E>(arr, len);
            }
        }
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public JoinedIterable<E> join(Iterable<? extends E> ... iterables) {
        if (iterables == null || iterables.length == 0) {
            return this;
        }
        return this.join(MACollections.wrap(iterables));
    }
    
    @Override
    public Iterator<E> iterator() {
        Iterable<E>[] iterables = this.iterables;
        int len = this.length;
        @SuppressWarnings("unchecked")
        Iterator<E>[] iterators = new Iterator[len];
        int itrLen = 0;
        for (int i = 0; i < len; i++) {
            Iterator<E> iterator = iterables[i].iterator();
            if (iterator != null) {
                iterators[itrLen++] = iterator;
            }
        }
        return new JoinedIterator<E>(iterators, itrLen);
    }

}
