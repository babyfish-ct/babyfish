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
package org.babyfish.persistence.criteria.spi;

import java.util.Collection;
import java.util.Map.Entry;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.Nulls;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.XSubquery;
import org.babyfish.persistence.criteria.ext.DependencyPredicateBuilder;
import org.babyfish.persistence.criteria.ext.SelfGetter;
import org.babyfish.persistence.criteria.ext.SelfGetterFromRestrictedTarget;

/**
 * @author Tao Chen
 */
public class DependencyPredicateBuilderImpl<S, T> implements DependencyPredicateBuilder<S, T> {
    
    private AbstractFrom<?, S> from;
    
    private Class<T> targetType;
    
    private XOrderedSet<SelfGetter<S, T>> selfGetters;
    
    private Boolean has;
    
    private XOrderedMap<SingularAttribute<? super T, ?>, XOrderedSet<?>> includeMap;
    
    private XOrderedMap<SingularAttribute<? super T, ?>, XOrderedSet<?>> excludeMap;
    
    public DependencyPredicateBuilderImpl(AbstractFrom<?, S> from, Class<T> targetType) {
        this.from = Arguments.mustNotBeNull("from", from);
        this.targetType = Arguments.mustBeClass("targetType", Arguments.mustNotBeNull("targetType", targetType));
    }

    @Override
    public DependencyPredicateBuilder<S, T> addSelfGetter(SelfGetter<S, T> selfGetter) {
        if (selfGetter != null) {
            XOrderedSet<SelfGetter<S, T>> selfGetters = this.selfGetters;
            if (selfGetters == null) {
                this.selfGetters = selfGetters = new LinkedHashSet<>();
            }
            selfGetters.add(selfGetter);
        }
        return this;
    }

    @Override
    public DependencyPredicateBuilder<S, T> has(Boolean has) {
        this.has = has;
        return this;
    }

    @Override
    public DependencyPredicateBuilder<S, T> includeAny(Iterable<? extends T> targets) {
        this.includeImpl(null, orderedSet(targets));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DependencyPredicateBuilder<S, T> includeAny(T... targets) {
        this.includeImpl(null, orderedSet(targets));
        return this;
    }

    @Override
    public <V> DependencyPredicateBuilder<S, T> includeAny(SingularAttribute<? super T, V> valueAttribute, Iterable<V> values) {
        this.includeImpl(Arguments.mustNotBeNull("valueAttribute", valueAttribute), orderedSet(values));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> DependencyPredicateBuilder<S, T> includeAny(SingularAttribute<? super T, V> valueAttribute, V... values) {
        this.includeImpl(Arguments.mustNotBeNull("valueAttribute", valueAttribute), orderedSet(values));
        return this;
    }

    @Override
    public DependencyPredicateBuilder<S, T> excludeAll(Iterable<? extends T> targets) {
        this.excludeImpl(null, orderedSet(targets));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DependencyPredicateBuilder<S, T> excludeAll(T... targets) {
        this.excludeImpl(null, orderedSet(targets));
        return this;
    }

    @Override
    public <V> DependencyPredicateBuilder<S, T> excludeAll(SingularAttribute<? super T, V> valueAttribute, Iterable<V> values) {
        this.excludeImpl(Arguments.mustNotBeNull("valueAttribute", valueAttribute), orderedSet(values));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> DependencyPredicateBuilder<S, T> excludeAll(SingularAttribute<? super T, V> valueAttribute, V... values) {
        this.excludeImpl(Arguments.mustNotBeNull("valueAttribute", valueAttribute), orderedSet(values));
        return this;
    }

    @Override
    public Predicate build() {
        if (Nulls.isNullOrEmpty(this.selfGetters)) {
            throw new IllegalStateException(
                    noSelfGetter(
                            DependencyPredicateBuilder.class, 
                            SelfGetter.class
                    )
            );
        }
        
        Boolean has = this.has;
        XOrderedMap<SingularAttribute<? super T, ?>, XOrderedSet<?>> includeMap = this.includeMap;
        XOrderedMap<SingularAttribute<? super T, ?>, XOrderedSet<?>> excludeMap = this.excludeMap;
        if (has == null && Nulls.isNullOrEmpty(includeMap) && Nulls.isNullOrEmpty(excludeMap)) {
            return null;
        }
        
        Class<T> targetType = this.targetType;
        AbstractFrom<?, S> from = this.from;
        XCriteriaBuilder cb = from.getCriteriaBuilder();
        Predicate predicate = null;
        Predicate negPredicate = null;
        for (SelfGetter<S, T> selfGetter : this.selfGetters) {
            if (Boolean.TRUE.equals(has) || !Nulls.isNullOrEmpty(includeMap)) {
                XSubquery<Integer> sq = from.getCommonAbstractCriteria().subquery(Integer.class);
                XRoot<T> target = sq.from(targetType);
                Path<S> self = safeGetSelf(selfGetter, target);
                sq
                .where(
                        cb.equal(from, self),
                        targetPredicate(cb, target, selfGetter),
                        valuePredicate(cb, target, includeMap)
                )
                .select(cb.constant(0));
                predicate = cb.or(predicate, cb.exists(sq));
            }
            if (Boolean.FALSE.equals(has) || !Nulls.isNullOrEmpty(excludeMap)) {
                XSubquery<Integer> sq = this.from.getCommonAbstractCriteria().subquery(Integer.class);
                XRoot<T> target = sq.from(targetType);
                Path<S> self = safeGetSelf(selfGetter, target);
                sq
                .where(
                        cb.equal(from, self),
                        targetPredicate(cb, target, selfGetter),
                        //Optimize, if "has" is false, need not check the exclusive restrictions.
                        Boolean.FALSE.equals(has) ? null : valuePredicate(cb, target, excludeMap)
                )
                .select(cb.constant(0));
                negPredicate = cb.and(negPredicate, cb.not(cb.exists(sq)));
            }
        }
        
        return cb.and(predicate, negPredicate);
    }
    
    private void includeImpl(SingularAttribute<? super T, ?> valueAttribute, XOrderedSet<?> values) {
        if (Nulls.isNullOrEmpty(values)) {
            return;
        }
        XOrderedMap<SingularAttribute<? super T, ?>, XOrderedSet<?>> includeMap = this.includeMap;
        if (includeMap == null) {
            this.includeMap = includeMap = new LinkedHashMap<>();
        }
        includeMap.put(valueAttribute, values);
    }
    
    private void excludeImpl(SingularAttribute<? super T, ?> valueAttribute, XOrderedSet<?> values) {
        if (Nulls.isNullOrEmpty(values)) {
            return;
        }
        XOrderedMap<SingularAttribute<? super T, ?>, XOrderedSet<?>> excludeMap = this.excludeMap;
        if (excludeMap == null) {
            this.excludeMap = excludeMap = new LinkedHashMap<>();
        }
        excludeMap.put(valueAttribute, values);
    }
    
    private static <E> XOrderedSet<E> orderedSet(Iterable<E> iterable) {
        if (Nulls.isNullOrEmpty(iterable)) {
            return MACollections.emptyOrderedSet();
        }
        XOrderedSet<E> set; 
        if (iterable instanceof Collection<?>) {
            set = new LinkedHashSet<>((((Collection<?>)iterable).size() * 4 + 2) / 3);
        } else {
            set = new LinkedHashSet<>();
        }
        for (E e : iterable) {
            if (e != null) {
                set.add(e);
            }
        }
        if (set.isEmpty()) {
            return MACollections.emptyOrderedSet();
        }
        return set;
    }
    
    private static <X, Y> Path<X> safeGetSelf(SelfGetter<X, Y> selfGetter, XRoot<Y> target) {
        Path<X> self = selfGetter.getSelf(target);
        if (self == null) {
            throw new IllegalProgramException(
                    selfGetterCanNotReturnNull(
                            selfGetter.getClass(),
                            "Path<S> getSelf(XRoot<T> target)"
                    )
            );
        }
        return self;
    }
    
    private static <E> XOrderedSet<E> orderedSet(E[] values) {
        if (Nulls.isNullOrEmpty(values)) {
            return MACollections.emptyOrderedSet();
        }
        XOrderedSet<E> set = new LinkedHashSet<>((values.length * 4 + 2) / 3);
        for (E e : values) {
            if (e != null) {
                set.add(e);
            }
        }
        if (set.isEmpty()) {
            return MACollections.emptyOrderedSet();
        }
        return set;
    }
    
    private static <Y> Predicate targetPredicate(
            XCriteriaBuilder cb, 
            XRoot<Y> target, 
            SelfGetter<?, Y> selfGetter) {
        if (selfGetter instanceof SelfGetterFromRestrictedTarget<?, ?>) {
            SelfGetterFromRestrictedTarget<?, Y> selfGetterFromRestrictedTarget =
                    (SelfGetterFromRestrictedTarget<?, Y>)selfGetter;
            return selfGetterFromRestrictedTarget.restrictTarget(cb, target);
        }
        return null;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <Y> Predicate valuePredicate(
            XCriteriaBuilder cb, 
            XRoot<Y> target, 
            XOrderedMap<SingularAttribute<? super Y, ?>, XOrderedSet<?>> valueMap) {
        if (Nulls.isNullOrEmpty(valueMap)) {
            return null;
        }
        Predicate predicate = null;
        for (Entry<SingularAttribute<? super Y, ?>, XOrderedSet<?>> entry : valueMap.entrySet()) {
            SingularAttribute<? super Y, ?> valueAttribute = entry.getKey();
            if (valueAttribute == null) {
                predicate = cb.or(predicate, cb.in(target, (Iterable<Y>)entry.getValue()));
            } else {
                predicate = cb.or(predicate, cb.in((Expression)target.get(valueAttribute), entry.getValue()));
            }
        }
        return predicate;
    }
    
    @SuppressWarnings("rawtypes")
    @I18N
    private static native String noSelfGetter(
            Class<DependencyPredicateBuilder> dependencyPredicateBuilderType, 
            Class<SelfGetter> selfGetterType);
    
    @SuppressWarnings("rawtypes")
    @I18N
    private static native String selfGetterCanNotReturnNull(
            Class<? extends SelfGetter> actualSelfGetterType,
            String methodSignature);
}
