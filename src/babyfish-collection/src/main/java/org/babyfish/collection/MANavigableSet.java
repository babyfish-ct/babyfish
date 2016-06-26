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

/**
 * @author Tao Chen
 */
public interface MANavigableSet<E> extends XNavigableSet<E>, MASortedSet<E> {

    @Override
    MAIterator<E> iterator();
    
    @Override
    MAIterator<E> descendingIterator();

    @Override
    MANavigableSetView<E> descendingSet();
    
    @Override
   MANavigableSetView<E> headSet(E toElement);

    @Override
   MANavigableSetView<E> tailSet(E fromElement);

    @Override
    MANavigableSetView<E> subSet(E fromElement, E toElement);

    @Override
    MANavigableSetView<E> headSet(E toElement, boolean inclusive);

    @Override
    MANavigableSetView<E> tailSet(E fromElement, boolean inclusive);
    
    @Override
    MANavigableSetView<E> subSet(
            E fromElement, boolean fromInclusive,
            E toElement,   boolean toInclusive);
    
    interface MANavigableSetView<E> extends MANavigableSet<E>, MASortedSetView<E>, XNavigableSetView<E> {
        
    }
    
}
