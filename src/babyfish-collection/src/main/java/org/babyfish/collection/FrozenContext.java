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

import java.lang.ref.WeakReference;

import org.babyfish.collection.spi.base.BaseEntries;
import org.babyfish.collection.spi.base.FrozenContextSuspending;
import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
public abstract class FrozenContext<T> {
    
    private FrozenContext() {
        
    }
    
    abstract void suspendFreezing(T obj);
    
    abstract void resumeFreezing();
    
    abstract boolean isAlive();
    
    public static <K> FrozenContext<K> create(BaseEntries<K, ?> baseEntries) {
        if (!baseEntries.isRoot()) {
            throw new IllegalArgumentException(baseEntriesMustBeRoot());
        }
        return new SingleImpl<>(baseEntries);
    }
    
    public static <T> void suspendFreezing(FrozenContext<T> ctx, T obj) {
        if (ctx != null) {
            ctx.suspendFreezing(obj);
        }
    }
    
    public static <T> void resumeFreezing(FrozenContext<T> ctx) {
        if (ctx != null) {
            ctx.resumeFreezing();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> FrozenContext<T> combine(FrozenContext<T> ctx1, FrozenContext<T> ctx2) {
        if (ctx1 == null || !ctx1.isAlive()) {
            return ctx2 != null && ctx2.isAlive() ? ctx2 : null;
        }
        if (ctx2 == null || !ctx2.isAlive()) {
            return ctx1 != null && ctx1.isAlive() ? ctx1 : null;
        }
        int len1 = len(ctx1);
        int len2 = len(ctx2);
        SingleImpl<T>[] arr = new SingleImpl[len1 + len2];
        int len = copyTo(ctx1, arr, 0);
        SingleImpl<T>[] addArr = toArray(ctx2);
        for (int i = 0; i < len2; i++) {
            SingleImpl<T> addCtx = addArr[i];
            if (addCtx != null) {
                boolean duplicated = false;
                for (int ii = len - 1; ii >= 0; ii--) {
                    if (arr[ii].same(addCtx)) {
                        duplicated = true;
                        break;
                    }
                }
                if (!duplicated) {
                    arr[len++] = addCtx;
                }
            }
        }
        if (len << 1 <= len1 + len2) {
            Trim<T> trim = new Trim<T>(arr, len);
            trim.trim(len);
            arr = trim.arr;
            len = trim.len;
        }
        if (len == 0) {
            return null;
        }
        if (len == 1) {
            return arr[0];
        }
        return new CombinedImpl<T>(arr, len);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> FrozenContext<T> remove(FrozenContext<T> ctx1, FrozenContext<T> ctx2) {
        if (ctx1 == null || !ctx1.isAlive()) {
            return null;
        }
        if (ctx2 == null || !ctx2.isAlive()) {
            return ctx1 != null && ctx1.isAlive() ? ctx1 : null;
        }
        int len1 = len(ctx1);
        int len2 = len(ctx2);
        SingleImpl<T>[] arr = new SingleImpl[len1];
        int oldLen = copyTo(ctx1, arr, 0);
        int len = oldLen;
        SingleImpl<T>[] removeArr = toArray(ctx2);
        for (int i = oldLen - 1; i >= 0; i--) {
            for (int ii = len2 - 1; ii >= 0; ii--) {
                SingleImpl<T> removeCtx = removeArr[ii];
                if (removeCtx != null) {
                    if (arr[i].same(removeCtx)) {
                        arr[i] = null;
                        len--;
                    }
                }
            }
        }
        if (len << 1 <= oldLen) {
            Trim<T> trim = new Trim<T>(arr, len);
            trim.trim(len);
            arr = trim.arr;
            len = trim.len;
        }
        if (len == 0) {
            return null;
        }
        if (len == 1) {
            return arr[0];
        }
        return new CombinedImpl<T>(arr, len);
    }
    
    private static int len(FrozenContext<?> ctx) {
        return ctx instanceof CombinedImpl<?> ? ((CombinedImpl<?>)ctx).len : 1;
    }
    
    private static <T> int copyTo(FrozenContext<T> ctx, SingleImpl<T>[] arr, int offset) {
        int index = offset;
        if (ctx instanceof CombinedImpl<?>) {
            CombinedImpl<T> combinedImpl = (CombinedImpl<T>)ctx;
            SingleImpl<T>[] src = combinedImpl.arr;
            int srcLen = combinedImpl.len;
            for (int i = 0; i < srcLen; i++) {
                SingleImpl<T> srcItem = src[i];
                if (srcItem != null && srcItem.isAlive()) {
                    arr[index++] = srcItem;
                }
            }
        } else if (ctx.isAlive()) {
            arr[index++] = (SingleImpl<T>)ctx;
        }
        return index - offset;
    }
    
    @SuppressWarnings("unchecked")
    private static <T> SingleImpl<T>[] toArray(FrozenContext<T> ctx) {
        return ctx instanceof CombinedImpl<?> ? 
                ((CombinedImpl<T>)ctx).arr : 
                new SingleImpl[] { (SingleImpl<T>)ctx };
    }
    
    private static class SingleImpl<K> extends FrozenContext<K> {
        
        private WeakReference<BaseEntries<K, Object>> baseEntriesReference;
        
        private FrozenContextSuspending<K, Object> suspending;
        
        private int suspendCount;
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private SingleImpl(BaseEntries<K, ?> baseEntries) {
            this.baseEntriesReference = new WeakReference<BaseEntries<K, Object>>((BaseEntries)baseEntries); 
        }
        
        @Override
        void suspendFreezing(K obj) {
            if (this.suspendCount++ == 0) {
                BaseEntries<K, Object> baseEntries = this.baseEntriesReference.get();
                if (baseEntries != null) {
                    this.suspending = baseEntries.suspendViaFrozenContext(obj);
                }
            }
        }
        
        @Override
        void resumeFreezing() {
            int suspendCount = this.suspendCount - 1;
            if (suspendCount < 0) {
                throw new IllegalStateException(canNotResume());
            }
            this.suspendCount = suspendCount;
            if (suspendCount == 0) {
                BaseEntries<K, Object> baseEntries = this.baseEntriesReference.get();
                if (baseEntries != null) {
                    FrozenContextSuspending<K, Object> suspending = this.suspending;
                    this.suspending = null;
                    baseEntries.resumeViaFronzeContext(suspending);
                }
            }
        }
        
        @Override
        boolean isAlive() {
            return this.baseEntriesReference.get() != null;
        }
        
        boolean same(SingleImpl<K> other) {
            BaseEntries<K, ?> baseEntries = this.baseEntriesReference.get();
            if (baseEntries != null) {
                return baseEntries == other.baseEntriesReference.get();
            }
            return false;
        }
    }
    
    private static class CombinedImpl<T> extends FrozenContext<T> {
        
        private SingleImpl<T>[] arr;
        
        private int len;
        
        CombinedImpl(SingleImpl<T>[] arr, int len) {
            this.arr = arr;
            this.len = len;
        }

        @Override
        void suspendFreezing(T obj) {
            SingleImpl<T>[] arr = this.arr;
            int len = this.len;
            int missCount = 0;
            for (int i = 0; i < len; i++) {
                SingleImpl<T> ctx = arr[i];
                if (ctx == null) {
                    missCount++;
                } else if (!ctx.isAlive()) {
                    arr[i] = null;
                    missCount++;
                } else {
                    ctx.suspendFreezing(obj);
                }
            }
            if (missCount << 1 >= len) {
                Trim<T> trim = new Trim<T>(arr, len);
                trim.trim(len - missCount);
                this.arr = trim.arr;
                this.len = trim.len;
            }
        }

        @Override
        void resumeFreezing() {
            SingleImpl<T>[] arr = this.arr;
            int len = this.len;
            int missCount = 0;
            for (int i = 0; i < len; i++) {
                SingleImpl<T> ctx = arr[i];
                if (ctx == null) {
                    missCount++;
                } else if (!ctx.isAlive()) {
                    arr[i] = null;
                    missCount++;
                } else {
                    ctx.resumeFreezing();
                }
            }
            if (missCount << 1 >= len) {
                Trim<T> trim = new Trim<T>(arr, len);
                trim.trim(len - missCount);
                this.arr = trim.arr;
                this.len = trim.len;
            }
        }

        @Override
        boolean isAlive() {
            return this.len != 0;
        }
    }
    
    private static class Trim<T> {
        
        private static final SingleImpl<?>[] EMPTY_SINGLE_IMPLS = new SingleImpl[0];
        
        SingleImpl<T>[] arr;
        
        int len;
        
        Trim(SingleImpl<T>[] arr, int len) {
            this.arr = arr;
            this.len = len;
        }
        
        @SuppressWarnings("unchecked")
        void trim(int trimLen) {
            if (trimLen == 0) {
                this.arr = (SingleImpl<T>[])EMPTY_SINGLE_IMPLS;
                this.len = 0;
            } else {
                SingleImpl<T>[] arr = this.arr;
                int len = this.len;
                SingleImpl<T>[] newArr = new SingleImpl[trimLen];
                int index = 0;
                for (int i = 0; i < len; i++) {
                    SingleImpl<T> ctx = arr[i];
                    if (ctx != null && ctx.isAlive()) {
                        newArr[index++] = ctx;
                    }
                }
                this.arr = newArr;
                this.len = index;
            }
        }
    }

    @I18N
    private static native String baseEntriesMustBeRoot();
        
    @I18N
    private static native String canNotResume();
}

