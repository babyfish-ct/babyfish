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
package org.babyfish.collection.spi.base;

import java.util.NavigableSet;

/**
 * @author Tao Chen
 */
public interface BaseElementsHandler<E> {

    Object createAddingArgument(int index, E element);
    
    void  adding(int index, E element, Object argument);
    
    void added(int index, E element, Object argument);
    
    Object createChangingArgument(int oldIndex, int newIndex, E oldElement, E newElement);
    
    void changing(int oldIndex, int newIndex, E oldElement, E newElement, Object argument);
    
    void changed(int oldIndex, int newIndex, E oldElement, E newElement, Object argument);
    
    Object createRemovingArgument(int index, E element);
    
    void removing(int index, E element, Object argument);
    
    void removed(int index, E element, Object argument);
    
    void setPreThrowable(Object argument, Throwable throwable);
    
    void setNullOrThrowable(Throwable nullOrThrowable);
    
    void setConflictAbsIndexes(NavigableSet<Integer> conflictAbsIndexes);
}
