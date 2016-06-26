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

import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;

import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public class Stack<E> {

    private List<E> list;
    
    public Stack() {
        this(new LinkedList<E>());
    }
    
    public Stack(List<E> list) {
        Arguments.mustNotBeNull("list", list);
        this.list = list;
    }
    
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public E push(E element) {
        this.list.add(element);
        return element;
    }
    
    public E pop() {
        List<E> list = this.list;
        int size = list.size();
        if (size == 0) {
            throw new EmptyStackException();
        }
        return list.remove(size - 1);
    }
    
    public E peek() {
        List<E> list = this.list;
        int size = list.size();
        if (size == 0) {
            throw new EmptyStackException();
        }
        return list.get(size - 1);
    }
}
