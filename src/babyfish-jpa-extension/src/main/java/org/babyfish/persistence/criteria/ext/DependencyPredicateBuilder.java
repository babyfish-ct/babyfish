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
package org.babyfish.persistence.criteria.ext;

import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;


/**
 * @author Tao Chen
 */
public interface DependencyPredicateBuilder<S, T> {
    
    DependencyPredicateBuilder<S, T> addSelfGetter(SelfGetter<S, T> selfGetter);
    
    DependencyPredicateBuilder<S, T> has(Boolean has);
    
    DependencyPredicateBuilder<S, T> includeAny(Iterable<? extends T> targets);
    
    @SuppressWarnings("unchecked")
    DependencyPredicateBuilder<S, T> includeAny(T ... targets);
    
    <V> DependencyPredicateBuilder<S, T> includeAny(
            SingularAttribute<? super T, V> valueAttribute, Iterable<V> values);
    
    @SuppressWarnings("unchecked")
    <V> DependencyPredicateBuilder<S, T> includeAny(
            SingularAttribute<? super T, V> valueAttribute, V ... values);
    
    DependencyPredicateBuilder<S, T> excludeAll(Iterable<? extends T> targets);
    
    @SuppressWarnings("unchecked")
    DependencyPredicateBuilder<S, T> excludeAll(T ... targets);
    
    <V> DependencyPredicateBuilder<S, T> excludeAll(
            SingularAttribute<? super T, V> valueAttribute, Iterable<V> values);
    
    @SuppressWarnings("unchecked")
    <V> DependencyPredicateBuilder<S, T> excludeAll(
            SingularAttribute<? super T, V> valueAttribute, V ... values);
    
    Predicate build();  
}
