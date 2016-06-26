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
package org.babyfish.collection;

import org.babyfish.collection.event.ListElementModificationAware;

/**
 * @author Tao Chen
 */
public interface MAList<E> extends XList<E>, MACollection<E>, ListElementModificationAware<E> {
    
    @Override
    MAListIterator<E> iterator();
    
    @Override
    MAListIterator<E> listIterator();
    
    @Override
    MAListIterator<E> listIterator(int index);
    
    @Override
    MAListView<E> subList(int fromIndex, int toIndex);
    
    interface MAListIterator<E> extends XListIterator<E>, MAIterator<E>, ListElementModificationAware<E> {
        
    }
    
    interface MAListView<E> extends XListView<E>, MAList<E> {
        
    }
}
