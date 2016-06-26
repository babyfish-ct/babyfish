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

import org.babyfish.collection.event.MapElementEvent.MapModification;
import org.babyfish.data.event.spi.AbstractModification;

/**
 * @author Tao Chen
 */
public class NavigableMapModifications extends MapModifications {

    public static <K, V> PollFirstEntry<K, V> pollFirstEntry() {
        return new PollFirstEntry<>();
    }
    
    public static <K, V> PollLastEntry<K, V> pollLastEntry() {
        return new PollLastEntry<>();
    }
    
    public static class PollFirstEntry<K, V> extends AbstractModification implements MapModification<K, V> {
        
        private static final long serialVersionUID = 4939645559204696073L;

        PollFirstEntry() {}
    }
    
    public static class PollLastEntry<K, V> extends AbstractModification implements MapModification<K, V> {
       
        private static final long serialVersionUID = 2976118478496853013L;

        PollLastEntry() {}
    }
    
    @Deprecated
    protected NavigableMapModifications() {}
}
