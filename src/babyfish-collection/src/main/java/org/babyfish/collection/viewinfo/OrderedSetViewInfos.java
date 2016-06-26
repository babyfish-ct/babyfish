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
public class OrderedSetViewInfos extends CollectionViewInfos {
    
    public static DescendingSet descendingSet() {
        return DescendingSet.INSTANCE;
    }
    
    public static DescendingIterator descendingIterator() {
        return DescendingIterator.INSTANCE;
    }
    
    public static class DescendingSet extends AbstractSingletonViewInfo {
        
        static final DescendingSet INSTANCE = getInstance(DescendingSet.class);
        
        private DescendingSet() {}
    }
    
    public static class DescendingIterator extends AbstractSingletonViewInfo {
        
        static final DescendingIterator INSTANCE = getInstance(DescendingIterator.class);
        
        private DescendingIterator() {}
    }
    
    @Deprecated
    protected OrderedSetViewInfos() {}
}
