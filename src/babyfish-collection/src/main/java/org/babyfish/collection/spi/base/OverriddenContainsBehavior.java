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

import java.util.Collection;

import org.babyfish.collection.UnifiedComparator;

/**
 * @author Tao Chen
 */
public abstract class OverriddenContainsBehavior {
    
    private OverriddenContainsBehavior() {
        
    }

    public static <E> OverriddenContainsBehavior of(
            Collection<E> c, 
            UnifiedComparator<? super E> unifiedComparator) {
        UnifiedComparator<? super E> unifiedComparatorOfC = UnifiedComparator.of(c);
        unifiedComparator = UnifiedComparator.emptyToNull(unifiedComparator);
        unifiedComparatorOfC = UnifiedComparator.emptyToNull(unifiedComparatorOfC);
        if (unifiedComparator == null ? 
                unifiedComparatorOfC == null :
                unifiedComparator.equals(unifiedComparatorOfC)) {
            return new NotOverriddenContainsBehavior(c);
        }
        return new HasBeenOverriddenContainsBehavior(c, unifiedComparator);
    }

    public abstract boolean isOverridden();
    
    public abstract boolean contains(Object o);
    
    private static class NotOverriddenContainsBehavior extends OverriddenContainsBehavior {
        
        private Collection<?> c;
        
        public NotOverriddenContainsBehavior(Collection<?> c) {
            this.c = c;
        }

        @Override
        public boolean isOverridden() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return this.c.contains(o);
        }
    
    }
    
    private static class HasBeenOverriddenContainsBehavior extends OverriddenContainsBehavior {
        
        private Collection<?> c;
        
        private UnifiedComparator<Object> unifiedComparator;
        
        @SuppressWarnings("unchecked")
        public HasBeenOverriddenContainsBehavior(Collection<?> c, UnifiedComparator<?> unifiedComparator) {
            this.c = c;
            this.unifiedComparator = (UnifiedComparator<Object>)unifiedComparator;
        }

        @Override
        public boolean isOverridden() {
            return true;
        }

        @Override
        public boolean contains(Object o) {
            UnifiedComparator<Object> unifiedComparator = this.unifiedComparator;
            for (Object e : this.c) {
                if (unifiedComparator.equals(e, o)) {
                    return true;
                }
            }
            return false;
        }
        
    }
    
}
