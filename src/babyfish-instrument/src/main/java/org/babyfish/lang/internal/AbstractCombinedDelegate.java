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
package org.babyfish.lang.internal;

import java.util.function.BiFunction;

/**
 * @author Tao Chen
 */
public abstract class AbstractCombinedDelegate {

    protected int delegateCount;
    
    protected Object[] delegates;
    
    protected AbstractCombinedDelegate(Object[] delegates, int delegateCount) {
        this.delegates = delegates;
        this.delegateCount = delegateCount;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName()).append(" { ");
        Object[] arr = this.delegates;
        int len = this.delegateCount;
        for (int i = 0; i < len; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(arr[i]);
        }
        builder.append(" }");
        return builder.toString();
    }

    protected static Object combine(
            Object a, 
            Object b, 
            BiFunction<Object[], Integer, AbstractCombinedDelegate> creator) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        int count1 = count(a);
        int count2 = count(b);
        Object[] arr = new Object[count1 + count2];
        toArray(a, arr, 0);
        toArray(b, arr, count1);
        return creator.apply(arr, arr.length);
    }
    
    protected static Object remove(
            Object a, 
            Object b,
            BiFunction<Object[], Integer, AbstractCombinedDelegate> creator) {
        if (a == null) {
            return null;
        }
        if (b == null) {
            return a;
        }
        Remover remover = new Remover(a);
        if (b instanceof AbstractCombinedDelegate) {
            AbstractCombinedDelegate cb = (AbstractCombinedDelegate)b;
            Object[] delegates = cb.delegates;
            int count = cb.delegateCount;
            for (int i = 0; i < count; i++) {
                remover.remove(delegates[i]);
            }
        } else {
            remover.remove(b);
        }
        Object[] arr = remover.dst;
        if (arr == null) {
            return a;
        }
        switch (remover.count) {
        case 0:
            return null;
        case 1:
            return arr[0];
        default:
            return creator.apply(arr, remover.count);
        }
    }
    
    private static int count(Object o) {
        if (o instanceof AbstractCombinedDelegate) {
            return ((AbstractCombinedDelegate)o).delegateCount;
        }
        return 1;
    }
    
    private static void toArray(Object o, Object[] target, int offset) {
        if (o instanceof AbstractCombinedDelegate) {
            AbstractCombinedDelegate combined = (AbstractCombinedDelegate)o;
            System.arraycopy(combined.delegates, 0, target, offset, combined.delegateCount);
        } else {
            target[offset] = o;
        }
    }
    
    private static class Remover {
        
        private Object[] src;
        
        int count;
        
        Object[] dst;
        
        Remover(Object o) {
            if (o instanceof AbstractCombinedDelegate) {
                AbstractCombinedDelegate combined = (AbstractCombinedDelegate)o;
                this.src = combined.delegates;
                this.count = combined.delegateCount;
            } else {
                this.src = new Object[] { o };
                this.count = 1;
            }
        }
        
        void remove(Object o) {
            Object[] src = this.dst != null ? this.dst : this.src;
            int count = this.count;
            for (int i = 0; i < count; i++) {
                if (src[i].equals(o)) {
                    System.arraycopy(src, i + 1, this.lazyCreateDst(), i, --this.count - i);
                    this.dst[this.count] = null;
                    return;
                }
            }
        }
        
        private Object[] lazyCreateDst() {
            Object[] dst = this.dst;
            if (dst == null) {
                int count = this.count;
                dst = new Object[count];
                System.arraycopy(this.src, 0, dst, 0, count);
                this.dst = dst;
            }
            return dst;
        }
    }
}
