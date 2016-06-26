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

import java.util.Map.Entry;

/*
 * BaseListIterator<E> does not inherit java.util.ListIterator<E>,
 * BaseEntryIterator<K, V> does not inherit java.util.Iterator<Entry<K, V>>.
 * 
 * Be different with them, BaseEntry<K, V> inherits java.util.Map.Entry<K, V>
 * because the entries are fine grit objects, often there are many entry objects,
 * create wrapper of each one will reduce the performance seriously. So I have
 * to make a compromise on the design, let BaseEntry<K, V> inherits the Entry<K, V>
 * and disable the setValue(V)
 */

/**
 * @author Tao Chen
 */
public interface BaseEntry<K, V> extends Entry<K, V> {
    
    boolean isNonFairLockSupported();

    BaseEntries<K, V> getOwner();
    
    @Deprecated
    @Override
    V setValue(V value);
    
    V setValue(V value, BaseEntriesHandler<K, V> handler);
}
