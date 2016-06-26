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
package org.babyfish.collection.viewinfo;

import org.babyfish.data.spi.AbstractSingletonViewInfo;

/**
 * @author Tao Chen
 */
public class OrderedMapViewInfos extends MapViewInfos {

    public static DescendingMap descendingMap() {
        return DescendingMap.INSTANCE;
    }
    
    public static OrderedKeySet orderedKeySet() {
        return OrderedKeySet.INSTANCE;
    }
    
    public static DescendingKeySet descendingKeySet() {
        return DescendingKeySet.INSTANCE;
    }
    
    public static FirstEntry firstEntry() {
        return FirstEntry.INSTANCE;
    }
    
    public static LastEntry lastEntry() {
        return LastEntry.INSTANCE;
    }
    
    public static class DescendingMap extends AbstractSingletonViewInfo {
        
        static final DescendingMap INSTANCE = getInstance(DescendingMap.class);
        
        private DescendingMap() {}
    }
    
    public static class OrderedKeySet extends KeySet {
        
        static final OrderedKeySet INSTANCE = getInstance(OrderedKeySet.class);
        
        private OrderedKeySet() {}
    }
    
    public static class DescendingKeySet extends AbstractSingletonViewInfo {
        
        static final DescendingKeySet INSTANCE = getInstance(DescendingKeySet.class);
        
        private DescendingKeySet() {}
    }
    
    public static class FirstEntry extends AbstractSingletonViewInfo {
        
        static final FirstEntry INSTANCE = getInstance(FirstEntry.class);
        
        private FirstEntry() {}
    }
    
    public static class LastEntry extends AbstractSingletonViewInfo {
        
        static final LastEntry INSTANCE = getInstance(LastEntry.class);
        
        private LastEntry() {}
    }

    @Deprecated
    protected OrderedMapViewInfos() {}
}
