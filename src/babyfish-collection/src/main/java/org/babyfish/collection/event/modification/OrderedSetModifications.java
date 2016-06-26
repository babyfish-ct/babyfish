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
package org.babyfish.collection.event.modification;

import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.data.event.spi.AbstractModification;

/**
 * @author Tao Chen
 */
public class OrderedSetModifications extends CollectionModifications {
    
    public static <E> PollFirst<E> pollFirst() {
        return new PollFirst<>();
    }
    
    public static <E> PollLast<E> pollLast() {
        return new PollLast<>();
    }
    
    public static class PollFirst<E> extends AbstractModification implements Modification<E> {
       
        private static final long serialVersionUID = 2908859215114497257L;

        PollFirst() {}
    }
    
    public static class PollLast<E> extends AbstractModification implements Modification<E> {
       
        private static final long serialVersionUID = -3073952431923789295L;

        PollLast() {}
    }
    
    @Deprecated
    protected OrderedSetModifications() {}
}
