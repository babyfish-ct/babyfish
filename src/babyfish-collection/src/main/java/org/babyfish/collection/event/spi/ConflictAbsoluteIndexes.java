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
package org.babyfish.collection.event.spi;

import java.util.NavigableSet;

import org.babyfish.collection.MACollections;
import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;

/**
 * @author Tao Chen
 */
public class ConflictAbsoluteIndexes {

    private static final Object AK_CONFLICT_ABSOLUTE_INDEXES = new Object();
    
    private ConflictAbsoluteIndexes() {
        throw new UnsupportedOperationException();
    }
    
    public static NavigableSet<Integer> get(ListElementEvent<?> e) {
        NavigableSet<Integer> conflictAbsIndexes =
                e
                .getFinalModification()
                .getAttributeContext()
                .getAttribute(AK_CONFLICT_ABSOLUTE_INDEXES);
        if (conflictAbsIndexes != null) {
            return conflictAbsIndexes;
        }
        return MACollections.emptyNavigableSet();
    }
    
    public static <E> void set(Modification<E> modification, NavigableSet<Integer> conflictAbsIndexes) {
        if (Nulls.isNullOrEmpty(conflictAbsIndexes)) {
            conflictAbsIndexes = null;
        }
        Arguments.mustNotBeNull("modification", modification)
        .getAttributeContext()
        .addAttribute(AK_CONFLICT_ABSOLUTE_INDEXES, conflictAbsIndexes);
    }
}
