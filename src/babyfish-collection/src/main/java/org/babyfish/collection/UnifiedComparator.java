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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;

import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
public abstract class UnifiedComparator<T> implements Serializable {

    private static final long serialVersionUID = 5211371532347287567L;
    
    private static final EmptyWrapper<?> EMPTY = new EmptyWrapper<Object>();
    
    private UnifiedComparator() {
        
    }
    
    public static <T> UnifiedComparator<T> of(Comparator<T> comparator) {
        if (comparator == null) {
            return null;
        }
        if (comparator instanceof FrozenComparator<?>) {
            return new FrozenComparatorWrapper<T>((FrozenComparator<T>)comparator);
        }
        return new ComparatorWrapper<T>(comparator);
    }
    
    public static <T> UnifiedComparator<T> of(EqualityComparator<T> equalityComparator) {
        if (equalityComparator == null) {
            return null;
        }
        if (equalityComparator instanceof FrozenEqualityComparator<?>) {
            return new FrozenEqualityComparatorWrapper<T>((FrozenEqualityComparator<T>)equalityComparator);
        }
        return new EqualityComparatorWrapper<T>(equalityComparator);
    }

    public static <T> UnifiedComparator<T> of(FrozenComparator<T> frozenComparator) {
        if (frozenComparator == null) {
            return null;
        }
        return new FrozenComparatorWrapper<T>(frozenComparator);
    }

    public static <T> UnifiedComparator<T> of(FrozenEqualityComparator<T> frozenEqualityComparator) {
        if (frozenEqualityComparator == null) {
            return null;
        }
        return new FrozenEqualityComparatorWrapper<T>(frozenEqualityComparator);
    }
    
    public static <E> UnifiedComparator<? super E> of(Collection<E> c) {
        return of(c, false);
    }

    public static <E> UnifiedComparator<? super E> of(Collection<E> c, boolean strict) {
        if (c instanceof XCollection<?>) {
            return emptyToNull(((XCollection<E>)c).unifiedComparator());
        }
        if (c instanceof SortedSet<?>) {
            return of(((SortedSet<E>)c).comparator());
        }
        if (strict) {
            throw new UnknownUnifiedComparatorException();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> UnifiedComparator<? super T> of(Object o) {
        if (o instanceof Collection<?>) {
            Collection<T> c = (Collection<T>)o;
            return of(c);
        }
        if (o instanceof UnifiedComparator<?>) {
            return (UnifiedComparator<T>)o;
        }
        if (o instanceof FrozenComparator<?>) {
            return new FrozenComparatorWrapper<T>((FrozenComparator<T>)o);
        }
        if (o instanceof FrozenEqualityComparator<?>) {
            return new FrozenEqualityComparatorWrapper<T>((FrozenEqualityComparator<T>)o);
        }
        if (o instanceof Comparator<?>) {
            return new ComparatorWrapper<T>((Comparator<T>)o);
        }
        if (o instanceof EqualityComparator<?>) {
            return new EqualityComparatorWrapper<T>((EqualityComparator<T>)o);
        }
        if (o == null) {
            return null;
        }
        throw new IllegalArgumentException(
                mustBeNullOrInstanceOfAnyOf(
                        "o", 
                        Collection.class,
                        UnifiedComparator.class,
                        FrozenComparator.class,
                        FrozenEqualityComparator.class,
                        Comparator.class,
                        EqualityComparator.class
                )
        );
    }
    
    public static Object unwrap(Object o) {
        if (o instanceof UnifiedComparator<?>) {
            UnifiedComparator<?> uc = (UnifiedComparator<?>)o;
            return uc.unwrap();
        }
        if (o == null) {
            return null;
        }
        if (o instanceof Comparator<?>) {
            return o;
        }
        if (o instanceof EqualityComparator<?>) {
            return o;
        }
        throw new IllegalArgumentException(
                mustBeNullOrInstanceOfAnyOf("o", Comparator.class, EqualityComparator.class)
        );
    }
    
    @SuppressWarnings("unchecked")
    public static <T> UnifiedComparator<T> empty() {
        return (UnifiedComparator<T>)EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <T> UnifiedComparator<T> nullToEmpty(UnifiedComparator<T> unifiedComparator) {
        return unifiedComparator == null ? (UnifiedComparator<T>)EMPTY : unifiedComparator;
    }

    public static <T> UnifiedComparator<T> emptyToNull(UnifiedComparator<T> unifiedComparator) {
        return EMPTY.equals(unifiedComparator) ? null : unifiedComparator;
    }

    public abstract boolean isEmpty();
    
    public final Comparator<T> comparator() {
        return this.comparator(false);
    }
    
    public final EqualityComparator<T> equalityComparator() {
        return this.equalityComparator(false);
    }
    
    public FrozenComparator<T> frozenComparator() {
        return this.frozenComparator(false);
    }
    
    public FrozenEqualityComparator<T> froznEqualityComparator() {
        return this.frozenEqualityComparator(false);
    }
    
    public abstract Object unwrap();
    
    public abstract Comparator<T> comparator(boolean force);
    
    public abstract EqualityComparator<T> equalityComparator(boolean force);
    
    public abstract FrozenComparator<T> frozenComparator(boolean force);
    
    public abstract FrozenEqualityComparator<T> frozenEqualityComparator(boolean force);
    
    public abstract int hashCode(T obj);
    
    public abstract boolean equals(T obj1, T obj2);
    
    public abstract void freeze(T obj, FrozenContext<T> ctx);
    
    public abstract void unfreeze(T obj, FrozenContext<T> ctx);
    
    private static final class EmptyWrapper<T> extends UnifiedComparator<T> {

        private static final long serialVersionUID = 8861730921145678128L;

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Object unwrap() {
            return null;
        }

        @Override
        public Comparator<T> comparator(boolean force) {
            return null;
        }

        @Override
        public EqualityComparator<T> equalityComparator(boolean force) {
            return null;
        }
        
        @Override
        public FrozenComparator<T> frozenComparator(boolean force) {
            return null;
        }

        @Override
        public FrozenEqualityComparator<T> frozenEqualityComparator(boolean force) {
            return null;
        }

        @Override
        public int hashCode(T obj) {
            return obj != null ? obj.hashCode() : 0;
        }

        @Override
        public boolean equals(T obj1, T obj2) {
            if (obj1 == obj2) {
                return true; 
            }
            if (obj1 == null || obj2 == null) {
                return false;
            }
            return obj1.equals(obj2);
        }

        @Override
        public int hashCode() {
            return 342567346;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || obj instanceof EmptyWrapper<?>;
        }
        
        @Override
        public void freeze(T obj, FrozenContext<T> ctx) {
            
        }
        
        @Override
        public void unfreeze(T obj, FrozenContext<T> ctx) {
            
        }

        protected final Object writeReplace() throws ObjectStreamException {
            return Replacement.INSTANCE;
        }
        
        private static class Replacement implements Serializable {
            
            private static final long serialVersionUID = -3586046993842200057L;
            
            private static final Replacement INSTANCE = new Replacement();

            private Replacement() {
                
            }

            private Object readResolve() throws ObjectStreamException {
                return EMPTY;
            }
            
        } 
    }
    
    private static final class ComparatorWrapper<T> extends UnifiedComparator<T> {
    
        private static final long serialVersionUID = 7336410592862005740L;
        
        Comparator<T> comparator;

        public ComparatorWrapper(Comparator<T> comparator) {
            if (comparator instanceof FrozenComparator<?>) {
                throw new AssertionError();
            }
            this.comparator = comparator;
        }
        
        @Override
        public boolean isEmpty() {
            return false;
        }
        
        @Override
        public Object unwrap() {
            return this.comparator;
        }

        @Override
        public Comparator<T> comparator(boolean force) {
            return this.comparator;
        }

        @Override
        public EqualityComparator<T> equalityComparator(boolean force) {
            if (force) {
                throw new IllegalStateException(
                        currentUnifiedComparatorIsNotAWrapperOf(
                                UnifiedComparator.class, EqualityComparator.class
                        )
                );
            }
            return null;
        }
        
        @Override
        public FrozenComparator<T> frozenComparator(boolean force) {
            if (force) {
                throw new IllegalStateException(
                        currentUnifiedComparatorIsNotAWrapperOf(
                                UnifiedComparator.class, FrozenComparator.class
                        )
                );
            }
            return null;
        }

        @Override
        public FrozenEqualityComparator<T> frozenEqualityComparator(boolean force) {
            if (force) {
                throw new IllegalStateException(
                        currentUnifiedComparatorIsNotAWrapperOf(
                                UnifiedComparator.class, FrozenEqualityComparator.class
                        )
                );
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public int hashCode(T obj) {
            if (this.comparator instanceof HashCalculator<?>) {
                return ((HashCalculator<T>)this.comparator).hashCode(obj);
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(T obj1, T obj2) {
            return this.comparator.compare(obj1, obj2) == 0;
        }
        
        @Override
        public void freeze(T obj, FrozenContext<T> ctx) {
            
        }
        
        @Override
        public void unfreeze(T obj, FrozenContext<T> ctx) {
            
        }
        
        @Override
        public int hashCode() {
            return this.comparator.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ComparatorWrapper<?>)) {
                return false;
            }
            ComparatorWrapper<?> other = (ComparatorWrapper<?>)obj;
            return this.comparator.equals(other.comparator);
        }
    }
    
    private static final class EqualityComparatorWrapper<T> extends UnifiedComparator<T> {
        
        private static final long serialVersionUID = 2937294868255485559L;
        
        EqualityComparator<T> equalityComparator;

        public EqualityComparatorWrapper(EqualityComparator<T> equalityComparator) {
            if (equalityComparator instanceof FrozenEqualityComparator<?>) {
                throw new AssertionError();
            }
            this.equalityComparator = equalityComparator;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
        
        @Override
        public Object unwrap() {
            return this.equalityComparator;
        }

        @Override
        public Comparator<T> comparator(boolean force) {
            if (force) {
                throw new IllegalStateException(
                        currentUnifiedComparatorIsNotAWrapperOf(
                                UnifiedComparator.class, Comparator.class
                        )
                );
            }
            return null;
        }

        @Override
        public EqualityComparator<T> equalityComparator(boolean force) {
            return this.equalityComparator;
        }
        
        @Override
        public FrozenComparator<T> frozenComparator(boolean force) {
            if (force) {
                throw new IllegalStateException(
                        currentUnifiedComparatorIsNotAWrapperOf(
                                UnifiedComparator.class, FrozenComparator.class
                        )
                );
            }
            return null;
        }

        @Override
        public FrozenEqualityComparator<T> frozenEqualityComparator(boolean force) {
            if (force) {
                throw new IllegalStateException(
                        currentUnifiedComparatorIsNotAWrapperOf(
                                UnifiedComparator.class, FrozenEqualityComparator.class
                        )
                );
            }
            return null;
        }

        @Override
        public int hashCode(T obj) {
            return obj == null ? 0 : this.equalityComparator.hashCode(obj);
        }

        @Override
        public boolean equals(T obj1, T obj2) {
            if (obj1 == obj2) {
                return true;
            }
            if (obj1 == null || obj2 == null) {
                return false;
            }
            return this.equalityComparator.equals(obj1, obj2);
        }
        
        @Override
        public void freeze(T obj, FrozenContext<T> ctx) {
            
        }
        
        @Override
        public void unfreeze(T obj, FrozenContext<T> ctx) {
            
        }
        
        @Override
        public int hashCode() {
            return this.equalityComparator.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof EqualityComparatorWrapper<?>)) {
                return false;
            }
            EqualityComparatorWrapper<?> other = (EqualityComparatorWrapper<?>)obj;
            return this.equalityComparator.equals(other.equalityComparator);
        }
        
    }
    
    private static final class FrozenComparatorWrapper<T> extends UnifiedComparator<T> {
        
        private static final long serialVersionUID = 944275751575121065L;
        
        FrozenComparator<T> frozenComparator;

        public FrozenComparatorWrapper(FrozenComparator<T> frozenComparator) {
            this.frozenComparator = frozenComparator;
        }
        
        @Override
        public boolean isEmpty() {
            return false;
        }
        
        @Override
        public Object unwrap() {
            return this.frozenComparator;
        }

        @Override
        public Comparator<T> comparator(boolean force) {
            return this.frozenComparator;
        }

        @Override
        public EqualityComparator<T> equalityComparator(boolean force) {
            if (force) {
                throw new IllegalStateException(
                        currentUnifiedComparatorIsNotAWrapperOf(
                                UnifiedComparator.class, EqualityComparator.class
                        )
                );
            }
            return null;
        }
        
        @Override
        public FrozenComparator<T> frozenComparator(boolean force) {
            return this.frozenComparator;
        }

        @Override
        public FrozenEqualityComparator<T> frozenEqualityComparator(boolean force) {
            if (force) {
                throw new IllegalStateException(
                        currentUnifiedComparatorIsNotAWrapperOf(
                                UnifiedComparator.class, FrozenEqualityComparator.class
                        )
                );
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public int hashCode(T obj) {
            if (this.frozenComparator instanceof HashCalculator<?>) {
                return ((HashCalculator<T>)this.frozenComparator).hashCode(obj);
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(T obj1, T obj2) {
            return this.frozenComparator.compare(obj1, obj2) == 0;
        }
        
        @Override
        public void freeze(T obj, FrozenContext<T> ctx) {
            if (obj != null) {
                this.frozenComparator.freeze(obj, ctx);
            }
        }
        
        @Override
        public void unfreeze(T obj, FrozenContext<T> ctx) {
            if (obj != null) {
                this.frozenComparator.unfreeze(obj, ctx);
            }
        }
        
        @Override
        public int hashCode() {
            return this.frozenComparator.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FrozenComparatorWrapper<?>)) {
                return false;
            }
            FrozenComparatorWrapper<?> other = (FrozenComparatorWrapper<?>)obj;
            return this.frozenComparator.equals(other.frozenComparator);
        }
    }
    
    private static final class FrozenEqualityComparatorWrapper<T> extends UnifiedComparator<T> {
        
        private static final long serialVersionUID = 7219686347736500678L;
        
        FrozenEqualityComparator<T> frozenEqualityComparator;

        public FrozenEqualityComparatorWrapper(
                FrozenEqualityComparator<T> frozenEqualityComparator) {
            this.frozenEqualityComparator = frozenEqualityComparator;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
        
        @Override
        public Object unwrap() {
            return this.frozenEqualityComparator;
        }

        @Override
        public Comparator<T> comparator(boolean force) {
            if (force) {
                throw new IllegalStateException(
                        currentUnifiedComparatorIsNotAWrapperOf(
                                UnifiedComparator.class, Comparator.class
                        )
                );
            }
            return null;
        }

        @Override
        public EqualityComparator<T> equalityComparator(boolean force) {
            return this.frozenEqualityComparator;
        }
        
        @Override
        public FrozenComparator<T> frozenComparator(boolean force) {
            if (force) {
                throw new IllegalStateException(
                        currentUnifiedComparatorIsNotAWrapperOf(
                                UnifiedComparator.class, FrozenComparator.class
                        )
                );
            }
            return null;
        }

        @Override
        public FrozenEqualityComparator<T> frozenEqualityComparator(boolean force) {
            return this.frozenEqualityComparator;
        }

        @Override
        public int hashCode(T obj) {
            return obj == null ? 0 : this.frozenEqualityComparator.hashCode(obj);
        }

        @Override
        public boolean equals(T obj1, T obj2) {
            if (obj1 == obj2) {
                return true;
            }
            if (obj1 == null || obj2 == null) {
                return false;
            }
            return this.frozenEqualityComparator.equals(obj1, obj2);
        }
        
        @Override
        public void freeze(T obj, FrozenContext<T> ctx) {
            if (obj != null) {
                this.frozenEqualityComparator.freeze(obj, ctx);
            }
        }
        
        @Override
        public void unfreeze(T obj, FrozenContext<T> ctx) {
            if (obj != null) {
                this.frozenEqualityComparator.unfreeze(obj, ctx);
            }
        }
        
        @Override
        public int hashCode() {
            return this.frozenEqualityComparator.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FrozenEqualityComparatorWrapper<?>)) {
                return false;
            }
            FrozenEqualityComparatorWrapper<?> other = (FrozenEqualityComparatorWrapper<?>)obj;
            return this.frozenEqualityComparator.equals(other.frozenEqualityComparator);
        }
    }
    
    @I18N
    private static native String mustBeNullOrInstanceOfAnyOf(
            String parameterName, Class<?> ... types);
        
    @SuppressWarnings("rawtypes")
    @I18N
    private static native String currentUnifiedComparatorIsNotAWrapperOf(
            Class<UnifiedComparator> unifiedComparatorType, Class<?> baseType);    
}
