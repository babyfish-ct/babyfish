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
package org.babyfish.test.collection.bidi;

import java.util.Comparator;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenComparator;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.FrozenEqualityComparator;

public class Unstable {
    
    public static final Comparator<Unstable> COMPARATOR = 
            new ComparatorImpl();
    
    public static final EqualityComparator<Unstable> EQUALITY_COMPARATOR = 
            new EqualityComparatorImpl();
    
    private String val;
    
    private transient FrozenContext<Unstable> valFrozenContext;

    public Unstable(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        FrozenContext<Unstable> ctx = this.valFrozenContext;
        FrozenContext.suspendFreezing(ctx, this);
        this.val = val;
        FrozenContext.resumeFreezing(ctx);
    }
    
    @Override
    public String toString() {
        return this.val;
    }

    private static abstract class AbstractComparator {
        
        public void freeze(Unstable obj, FrozenContext<Unstable> ctx) {
            obj.valFrozenContext = FrozenContext.combine(obj.valFrozenContext, ctx);
        }

        public void unfreeze(Unstable obj, FrozenContext<Unstable> ctx) {
            obj.valFrozenContext = FrozenContext.remove(obj.valFrozenContext, ctx);
        }
    }
    
    private static class ComparatorImpl 
    extends AbstractComparator 
    implements FrozenComparator<Unstable> {
        @Override
        public int compare(Unstable o1, Unstable o2) {
            return o1.val.compareTo(o2.val);
        }
    }
    
    private static class EqualityComparatorImpl 
    extends AbstractComparator 
    implements FrozenEqualityComparator<Unstable> {

        @Override
        public int hashCode(Unstable o) {
            return o.val.hashCode();
        }

        @Override
        public boolean equals(Unstable o1, Unstable o2) {
            return o1.val.equals(o2.val);
        }
    }
}
