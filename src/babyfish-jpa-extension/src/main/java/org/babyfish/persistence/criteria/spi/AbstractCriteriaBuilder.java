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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.persistence.Tuple;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Predicate.BooleanOperator;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.LikeMode;
import org.babyfish.persistence.criteria.XCollectionJoin;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaDelete;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XCriteriaUpdate;
import org.babyfish.persistence.criteria.XJoin;
import org.babyfish.persistence.criteria.XListJoin;
import org.babyfish.persistence.criteria.XMapJoin;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.XSetJoin;
import org.babyfish.persistence.criteria.expression.AbsFunction;
import org.babyfish.persistence.criteria.expression.Aggregation;
import org.babyfish.persistence.criteria.expression.Aggregation.Max;
import org.babyfish.persistence.criteria.expression.BetweenPredicate;
import org.babyfish.persistence.criteria.expression.BinaryArithmeticExpression;
import org.babyfish.persistence.criteria.expression.CoalesceExpression;
import org.babyfish.persistence.criteria.expression.ComparisonPredicate;
import org.babyfish.persistence.criteria.expression.CompoundPredicate;
import org.babyfish.persistence.criteria.expression.CompoundSelectionImpl;
import org.babyfish.persistence.criteria.expression.ConcatExpression;
import org.babyfish.persistence.criteria.expression.ConstantExpression;
import org.babyfish.persistence.criteria.expression.ConvertExpression;
import org.babyfish.persistence.criteria.expression.CurrentDateFunction;
import org.babyfish.persistence.criteria.expression.CurrentDatestampFunction;
import org.babyfish.persistence.criteria.expression.CurrentTimeFunction;
import org.babyfish.persistence.criteria.expression.ExistsPredicate;
import org.babyfish.persistence.criteria.expression.InPredicate;
import org.babyfish.persistence.criteria.expression.IsEmptyPredicate;
import org.babyfish.persistence.criteria.expression.IsMemberPredicate;
import org.babyfish.persistence.criteria.expression.IsTruePredicate;
import org.babyfish.persistence.criteria.expression.LengthFunction;
import org.babyfish.persistence.criteria.expression.LikePredicate;
import org.babyfish.persistence.criteria.expression.LiteralExpression;
import org.babyfish.persistence.criteria.expression.LocateFunction;
import org.babyfish.persistence.criteria.expression.LowerFunction;
import org.babyfish.persistence.criteria.expression.NullLiteralExpression;
import org.babyfish.persistence.criteria.expression.NullifExpression;
import org.babyfish.persistence.criteria.expression.NullnessPredicate;
import org.babyfish.persistence.criteria.expression.ParameterExpressionImpl;
import org.babyfish.persistence.criteria.expression.SearchedCaseExpression;
import org.babyfish.persistence.criteria.expression.SimpleCaseExpression;
import org.babyfish.persistence.criteria.expression.SizeExpression;
import org.babyfish.persistence.criteria.expression.SqrtFunction;
import org.babyfish.persistence.criteria.expression.SubqueryComparisonModifierExpression;
import org.babyfish.persistence.criteria.expression.SubstringFunction;
import org.babyfish.persistence.criteria.expression.TrimExpression;
import org.babyfish.persistence.criteria.expression.UnaryArithmeticExpression;
import org.babyfish.persistence.criteria.expression.UpperFunction;
import org.babyfish.persistence.criteria.ext.DependencyPredicateBuilder;

/**
 * @author Tao Chen
 */
public abstract class AbstractCriteriaBuilder implements XCriteriaBuilder {
    
    private Expression<Character> defaultTrimCharacter = this.constant(' ');

    @Override
    public XCriteriaQuery<Object> createQuery() {
        return new CriteriaQueryImpl<>(this, Object.class);
    }

    @Override
    public <T> XCriteriaQuery<T> createQuery(Class<T> resultClass) {
        return new CriteriaQueryImpl<>(this, resultClass);
    }
    
    @Override
    public XCriteriaQuery<Tuple> createTupleQuery() {
        return new CriteriaQueryImpl<>(this, Tuple.class);
    }

    @Override
    public <T> XCriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity) {
        return new CriteriaUpdateImpl<>(this);
    }
    
    @Override
    public <T> XCriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity) {
        return new CriteriaDeleteImpl<>(this);
    }
    
    @Override
    public <Y> CompoundSelection<Y> construct(Class<Y> resultClass, Iterable<Selection<?>> selections) {
        return new CompoundSelectionImpl<>(this, resultClass, selections);
    }

    @Override
    public <Y> CompoundSelection<Y> construct(Class<Y> resultClass, Selection<?>... selections) {
        return this.construct(resultClass, MACollections.wrap(selections));
    }
    
    @Override
    public CompoundSelection<Tuple> tuple(Iterable<Selection<?>> selections) {
        return new CompoundSelectionImpl<Tuple>(this, Tuple.class, selections);
    }

    @Override
    public CompoundSelection<Tuple> tuple(Selection<?>... selections) {
        return this.tuple(MACollections.wrap(selections));
    }
    
    @Override
    public CompoundSelection<Object[]> array(Iterable<Selection<?>> selections) {
        return new CompoundSelectionImpl<Object[]>(this, Object[].class, selections);
    }

    @Override
    public CompoundSelection<Object[]> array(Selection<?>... selections) {
        return this.array(MACollections.wrap(selections));
    }
    
    @Override
    public Order asc(Expression<?> x) {
        return new OrderImpl(this, x, true);
    }

    @Override
    public Order desc(Expression<?> x) {
        return new OrderImpl(this, x, false);
    }

    @Override
    public <N extends Number> Expression<Double> avg(Expression<N> x) {
        return new Aggregation.Avg<N>(this, x);
    }

    @Override
    public <N extends Number> Expression<N> sum(Expression<N> x) {
        return new Aggregation.Sum<N>(this, x);
    }

    @Override
    public Expression<Long> sumAsLong(Expression<Integer> x) {
        return new Aggregation.SumAsLong(this, x);
    }

    @Override
    public Expression<Double> sumAsDouble(Expression<Float> x) {
        return new Aggregation.SumAsDouble(this, x);
    }

    @Override
    public <N extends Number> Expression<N> max(Expression<N> x) {
        return new Max<N>(this, x);
    }

    @Override
    public <N extends Number> Expression<N> min(Expression<N> x) {
        return new Aggregation.Min<N>(this, x);
    }

    @Override
    public <X extends Comparable<? super X>> Expression<X> greatest(Expression<X> x) {
        return new Aggregation.Greatest<X>(this, x);
    }

    @Override
    public <X extends Comparable<? super X>> Expression<X> least(Expression<X> x) {
        return new Aggregation.Least<X>(this, x);
    }

    @Override
    public Expression<Long> count(Expression<?> x) {
        return new Aggregation.Count(this, x, false);
    }

    @Override
    public Expression<Long> countDistinct(Expression<?> x) {
        return new Aggregation.Count(this, x, true);
    }

    @Override
    public Predicate exists(Subquery<?> subquery) {
        return new ExistsPredicate(this, subquery);
    }

    @Override
    public <Y> Expression<Y> all(Subquery<Y> subquery) {
        return new SubqueryComparisonModifierExpression.All<Y>(this, subquery);
    }

    @Override
    public <Y> Expression<Y> some(Subquery<Y> subquery) {
        return new SubqueryComparisonModifierExpression.Some<Y>(this, subquery);
    }

    @Override
    public <Y> Expression<Y> any(Subquery<Y> subquery) {
        return new SubqueryComparisonModifierExpression.Any<Y>(this, subquery);
    }

    @Override
    public Predicate and(Expression<Boolean> x, Expression<Boolean> y) {
        return CompoundPredicate.of(this, BooleanOperator.AND, this.isTrue(x), this.isTrue(y));
    }

    @Override
    public Predicate and(Predicate... restrictions) {
        return CompoundPredicate.of(this, BooleanOperator.AND, restrictions);
    }

    @Override
    public Predicate or(Expression<Boolean> x, Expression<Boolean> y) {
        return CompoundPredicate.of(this, BooleanOperator.OR, this.isTrue(x), this.isTrue(y));
    }

    @Override
    public Predicate or(Predicate... restrictions) {
        return CompoundPredicate.of(this, BooleanOperator.OR, restrictions);
    }

    @Override
    public Predicate not(Expression<Boolean> restriction) {
        Predicate predicate = this.isTrue(restriction);
        if (predicate != null) {
            predicate = predicate.not();
        }
        return predicate;
    }

    @Override
    public Predicate conjunction() {
        return null;
    }

    @Override
    public Predicate disjunction() {
        return null;
    }

    @Override
    public Predicate isTrue(Expression<Boolean> x) {
        if (x == null) {
            return null;
        }
        if (x instanceof Predicate) {
            return (Predicate)x;
        }
        return new IsTruePredicate(this, x);
    }

    @Override
    public Predicate isFalse(Expression<Boolean> x) {
        return this.not(this.isTrue(x));
    }

    @Override
    public Predicate isNull(Expression<?> x) {
        return new NullnessPredicate(this, x);
    }

    @Override
    public Predicate isNotNull(Expression<?> x) {
        return this.not(this.isNull(x));
    }

    @Override
    public Predicate equal(Expression<?> x, Expression<?> y) {
        return new ComparisonPredicate.Equal(this, x, y);
    }

    @Override
    public Predicate equal(Expression<?> x, Object y) {
        return this.equal(x, this.literal(y));
    }

    @Override
    public Predicate notEqual(Expression<?> x, Expression<?> y) {
        return new ComparisonPredicate.NotEqual(this, x, y);
    }

    @Override
    public Predicate notEqual(Expression<?> x, Object y) {
        return this.notEqual(x, this.literal(y));
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThan(
            Expression<? extends Y> x, Expression<? extends Y> y) {
        return new ComparisonPredicate.GreaterThan(this, x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThan(
            Expression<? extends Y> x, Y y) {
        return this.greaterThan(x, this.literal(y));
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(
            Expression<? extends Y> x, Expression<? extends Y> y) {
        return new ComparisonPredicate.GreaterThanOrEqual(this, x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(
            Expression<? extends Y> x, Y y) {
        return this.greaterThanOrEqualTo(x, this.literal(y));
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThan(
            Expression<? extends Y> x, Expression<? extends Y> y) {
        return new ComparisonPredicate.LessThan(this, x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThan(
            Expression<? extends Y> x, Y y) {
        return this.lessThan(x, this.literal(y));
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(
            Expression<? extends Y> x, Expression<? extends Y> y) {
        return new ComparisonPredicate.LessThanOrEqual(this, x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(
            Expression<? extends Y> x, Y y) {
        return this.lessThanOrEqualTo(x, this.literal(y));
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate between(
            Expression<? extends Y> v, 
            Expression<? extends Y> x,
            Expression<? extends Y> y) {
        if (x == null && y == null) {
            return null;
        }
        return new BetweenPredicate<Y>(this, v, x, y);
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate between(
            Expression<? extends Y> v, Y x, Y y) {
        return this.between(
                v, 
                x != null ? this.literal(x) : null, 
                y != null ? this.literal(y) : null
        );
    }

    @Override
    public Predicate gt(Expression<? extends Number> x, Expression<? extends Number> y) {
        return new ComparisonPredicate.GreaterThan(this, x, y);
    }

    @Override
    public Predicate gt(Expression<? extends Number> x, Number y) {
        return this.gt(x, this.literal(y));
    }

    @Override
    public Predicate ge(Expression<? extends Number> x, Expression<? extends Number> y) {
        return new ComparisonPredicate.GreaterThanOrEqual(this, x, y);
    }

    @Override
    public Predicate ge(Expression<? extends Number> x, Number y) {
        return this.ge(x, this.literal(y));
    }

    @Override
    public Predicate lt(Expression<? extends Number> x, Expression<? extends Number> y) {
        return new ComparisonPredicate.LessThan(this, x, y);
    }

    @Override
    public Predicate lt(Expression<? extends Number> x, Number y) {
        return this.lt(x, this.literal(y));
    }

    @Override
    public Predicate le(Expression<? extends Number> x, Expression<? extends Number> y) {
        return new ComparisonPredicate.LessThanOrEqual(this, x, y);
    }

    @Override
    public Predicate le(Expression<? extends Number> x, Number y) {
        return this.le(x, this.literal(y));
    }

    @Override
    public <N extends Number> Expression<N> neg(Expression<N> x) {
        return new UnaryArithmeticExpression.Neg<N>(this, x);
    }

    @Override
    public <N extends Number> Expression<N> abs(Expression<N> x) {
        return new AbsFunction<N>(this, x);
    }

    @Override
    public <N extends Number> Expression<N> sum(
            Expression<? extends N> x, 
            Expression<? extends N> y) {
        return new BinaryArithmeticExpression.Sum<N>(this, x, y);
    }

    @Override
    public <N extends Number> Expression<N> sum(Expression<? extends N> x, N y) {
        return this.sum(x, this.literal(y));
    }

    @Override
    public <N extends Number> Expression<N> sum(N x, Expression<? extends N> y) {
        return this.sum(this.literal(x), y);
    }

    @Override
    public <N extends Number> Expression<N> diff(
            Expression<? extends N> x,
            Expression<? extends N> y) {
        return new BinaryArithmeticExpression.Diff<N>(this, x, y);
    }

    @Override
    public <N extends Number> Expression<N> diff(Expression<? extends N> x, N y) {
        return this.diff(x, this.literal(y));
    }

    @Override
    public <N extends Number> Expression<N> diff(N x, Expression<? extends N> y) {
        return this.diff(this.literal(x), y);
    }

    @Override
    public <N extends Number> Expression<N> prod(
            Expression<? extends N> x, 
            Expression<? extends N> y) {
        return new BinaryArithmeticExpression.Prod<N>(this, x, y);
    }

    @Override
    public <N extends Number> Expression<N> prod(Expression<? extends N> x, N y) {
        return this.prod(x, this.literal(y));
    }

    @Override
    public <N extends Number> Expression<N> prod(N x, Expression<? extends N> y) {
        return this.prod(this.literal(x), y);
    }

    @Override
    public Expression<Number> quot(
            Expression<? extends Number> x,
            Expression<? extends Number> y) {
        return new BinaryArithmeticExpression.Quot<Number>(this, x, y);
    }

    @Override
    public Expression<Number> quot(Expression<? extends Number> x, Number y) {
        return this.quot(x, this.literal(y));
    }

    @Override
    public Expression<Number> quot(Number x, Expression<? extends Number> y) {
        return this.quot(this.literal(x), y);
    }

    @Override
    public Expression<Integer> mod(Expression<Integer> x, Expression<Integer> y) {
        return new BinaryArithmeticExpression.Mod(this, x, y);
    }

    @Override
    public Expression<Integer> mod(Expression<Integer> x, Integer y) {
        return this.mod(x, this.literal(y));
    }

    @Override
    public Expression<Integer> mod(Integer x, Expression<Integer> y) {
        return this.mod(this.literal(x), y);
    }

    @Override
    public Expression<Double> sqrt(Expression<? extends Number> x) {
        return new SqrtFunction(this, x);
    }

    @Override
    public Expression<Integer> toInteger(Expression<? extends Number> number) {
        return new ConvertExpression.ToInteger(this, number);
    }

    @Override
    public Expression<Long> toLong(Expression<? extends Number> number) {
        return new ConvertExpression.ToLong(this, number);
    }

    @Override
    public Expression<Float> toFloat(Expression<? extends Number> number) {
        return new ConvertExpression.ToFloat(this, number);
    }

    @Override
    public Expression<Double> toDouble(Expression<? extends Number> number) {
        return new ConvertExpression.ToDouble(this, number);
    }

    @Override
    public Expression<BigInteger> toBigInteger(
            Expression<? extends Number> number) {
        return new ConvertExpression.ToBigInteger(this, number);
    }

    @Override
    public Expression<BigDecimal> toBigDecimal(
            Expression<? extends Number> number) {
        return new ConvertExpression.ToBigDecimal(this, number);
    }

    @Override
    public Expression<String> toString(Expression<Character> character) {
        return new ConvertExpression.ToString(this, character);
    }

    @Override
    public <T> Expression<T> literal(T value) {
        return new LiteralExpression<T>(this, value);
    }

    @Override
    public <T> Expression<T> nullLiteral(Class<T> resultClass) {
        return new NullLiteralExpression<T>(this, resultClass);
    }

    @Override
    public <T> Expression<T> constant(T value) {
        return new ConstantExpression<T>(this, value);
    }

    @Override
    public <T> ParameterExpression<T> parameter(Class<T> paramClass) {
        return this.parameter(paramClass, null);
    }

    @Override
    public <T> ParameterExpression<T> parameter(Class<T> paramClass, String name) {
        return new ParameterExpressionImpl<T>(this, paramClass, name);
    }

    @Override
    public <C extends Collection<?>> Predicate isEmpty(Expression<C> collection) {
        Arguments.mustBeInstanceOfValue("collection", collection, PluralAttributePath.class);
        return new IsEmptyPredicate<C>(this, (PluralAttributePath<C>)collection);
    }

    @Override
    public <C extends Collection<?>> Predicate isNotEmpty(Expression<C> collection) {
        return this.not(this.isEmpty(collection));
    }

    @Override
    public <C extends Collection<?>> Expression<Integer> size(Expression<C> collection) {
        if (!(collection instanceof PluralAttributePath<?>)) {
            Arguments.mustBeInstanceOfValue("collection", collection, PluralAttributePath.class);
        }
        return new SizeExpression<C>(this, (PluralAttributePath<C>)collection);
    }

    @Override
    public <C extends Collection<?>> Expression<Integer> size(C collection) {
        return this.literal(collection == null ? 0 : collection.size());
    }

    @Override
    public <E, C extends Collection<E>> Predicate isMember(
            Expression<E> elem, Expression<C> collection) {
        Arguments.mustBeInstanceOfValue("collection", collection, PluralAttributePath.class);
        return new IsMemberPredicate<E, C>(this, elem, (PluralAttributePath<C>)collection);
    }

    @Override
    public <E, C extends Collection<E>> Predicate isMember(
            E elem, Expression<C> collection) {
        return this.isMember(this.literal(elem), collection);
    }

    @Override
    public <E, C extends Collection<E>> Predicate isNotMember(
            Expression<E> elem, Expression<C> collection) {
        return this.not(this.isMember(elem, collection));
    }

    @Override
    public <E, C extends Collection<E>> Predicate isNotMember(
            E elem, Expression<C> collection) {
        return this.not(this.isMember(elem, collection));
    }
    
    @Override
    public <K, M extends Map<K, ?>> Expression<Set<K>> keys(M map) {
        return this.literal(map.keySet());
    }

    @Override
    public <V, M extends Map<?, V>> Expression<Collection<V>> values(M map) {
        return this.literal(map.values());
    }

    @Override
    public Predicate like(Expression<String> x, Expression<String> pattern) {
        return this.like(x, pattern, null);
    }

    @Override
    public Predicate like(Expression<String> x, String pattern) {
        return this.like(x, this.literal(pattern), null);
    }

    @Override
    public Predicate like(
            Expression<String> x, 
            Expression<String> pattern, 
            Expression<Character> escapeChar) {
        return new LikePredicate(this, x, pattern, escapeChar);
    }

    @Override
    public Predicate like(
            Expression<String> x, 
            Expression<String> pattern,
            char escapeChar) {
        return this.like(x, pattern, this.literal(escapeChar));
    }

    @Override
    public Predicate like(
            Expression<String> x, 
            String pattern, 
            Expression<Character> escapeChar) {
        return this.like(x, this.literal(pattern), escapeChar);
    }

    @Override
    public Predicate like(Expression<String> x, String pattern, char escapeChar) {
        return this.like(x, this.literal(pattern), this.literal(escapeChar));
    }

    @Override
    public Predicate like(Expression<String> x, String pattern, LikeMode likeMode) {
        if (likeMode != null) {
            pattern = likeMode.pattern(pattern);
        }
        return this.like(x, pattern);
    }

    @Override
    public Predicate insensitivelyLike(Expression<String> x, String pattern) {
        return this.insensitivelyLike(x, pattern, null);
    }

    @Override
    public Predicate insensitivelyLike(Expression<String> x, String pattern, LikeMode likeMode) {
        if (likeMode != null) {
            pattern = likeMode.pattern(pattern);
        }
        return this.like(this.upper(x), pattern.toUpperCase());
    }

    @Override
    public Predicate notLike(Expression<String> x, Expression<String> pattern) {
        return this.not(this.like(x, pattern));
    }

    @Override
    public Predicate notLike(Expression<String> x, String pattern) {
        return this.not(this.like(x, pattern));
    }

    @Override
    public Predicate notLike(
            Expression<String> x, 
            Expression<String> pattern,
            Expression<Character> escapeChar) {
        return this.not(this.like(x, pattern, escapeChar));
    }

    @Override
    public Predicate notLike(
            Expression<String> x, 
            Expression<String> pattern,
            char escapeChar) {
        return this.not(this.like(x, pattern, escapeChar));
    }

    @Override
    public Predicate notLike(
            Expression<String> x, 
            String pattern,
            Expression<Character> escapeChar) {
        return this.not(this.like(x, pattern, escapeChar));
    }

    @Override
    public Predicate notLike(
            Expression<String> x, 
            String pattern,
            char escapeChar) {
        return this.not(this.like(x, pattern, escapeChar));
    }

    @Override
    public Predicate notLike(Expression<String> x, String pattern, LikeMode likeMode) {
        return this.not(this.like(x, pattern, likeMode));
    }

    @Override
    public Predicate notInsensitivelyLike(Expression<String> x, String pattern) {
        return this.not(this.insensitivelyLike(x, pattern));
    }

    @Override
    public Predicate notInsensitivelyLike(Expression<String> x, String pattern, LikeMode likeMode) {
        return this.not(this.insensitivelyLike(x, pattern, likeMode));
    }

    @Override
    public Expression<String> concat(Expression<String> x, Expression<String> y) {
        return this.concat().value(x).value(y);
    }

    @Override
    public Expression<String> concat(Expression<String> x, String y) {
        return this.concat(x, this.literal(y));
    }
    
    @Override
    public Concat concat() {
        return new ConcatExpression(this);
    }

    @Override
    public Expression<String> concat(String x, Expression<String> y) {
        return this.concat(this.literal(x), y);
    }

    @Override
    public Expression<String> substring(Expression<String> x, Expression<Integer> from) {
        return this.substring(x, from, null);
    }

    @Override
    public Expression<String> substring(Expression<String> x, int from) {
        return this.substring(x, this.literal(from), null);
    }

    @Override
    public Expression<String> substring(
            Expression<String> x,
            Expression<Integer> from, 
            Expression<Integer> len) {
        return new SubstringFunction(this, x, from, len);
    }

    @Override
    public Expression<String> substring(
            Expression<String> x, 
            int from, 
            int len) {
        return this.substring(x, this.literal(from), this.literal(len));
    }

    @Override
    public Expression<String> trim(Expression<String> x) {
        return this.trim(TrimExpression.DEFAULT_TRIM_SPEC, this.defaultTrimCharacter, x);
    }

    @Override
    public Expression<String> trim(Trimspec ts, Expression<String> x) {
        return this.trim(ts, this.defaultTrimCharacter, x);
    }

    @Override
    public Expression<String> trim(Expression<Character> t, Expression<String> x) {
        return this.trim(TrimExpression.DEFAULT_TRIM_SPEC, t, x);
    }

    @Override
    public Expression<String> trim(Trimspec ts, Expression<Character> t, Expression<String> x) {
        return new TrimExpression(this, ts, t, x);
    }

    @Override
    public Expression<String> trim(char t, Expression<String> x) {
        return this.trim(TrimExpression.DEFAULT_TRIM_SPEC, this.literal(t), x);
    }

    @Override
    public Expression<String> trim(Trimspec ts, char t, Expression<String> x) {
        return this.trim(ts, this.literal(t), x);
    }

    @Override
    public Expression<String> lower(Expression<String> x) {
        return new LowerFunction(this, x);
    }

    @Override
    public Expression<String> upper(Expression<String> x) {
        return new UpperFunction(this, x);
    }

    @Override
    public Expression<Integer> length(Expression<String> x) {
        return new LengthFunction(this, x);
    }

    @Override
    public Expression<Integer> locate(
            Expression<String> x,
            Expression<String> pattern, 
            Expression<Integer> from) {
        return new LocateFunction(this, x, pattern, from);
    }

    @Override
    public Expression<Integer> locate(
            Expression<String> x, Expression<String> pattern) {
        return this.locate(x, pattern, null);
    }

    @Override
    public Expression<Integer> locate(
            Expression<String> x, 
            String pattern,
            int from) {
        return this.locate(x, this.literal(pattern), this.literal(from));
    }

    @Override
    public Expression<Integer> locate(Expression<String> x, String pattern) {
        return this.locate(x, this.literal(pattern), null);
    }

    @Override
    public Expression<Date> currentDate() {
        return new CurrentDateFunction(this);
    }

    @Override
    public Expression<Timestamp> currentTimestamp() {
        return new CurrentDatestampFunction(this);
    }

    @Override
    public Expression<Time> currentTime() {
        return new CurrentTimeFunction(this);
    }

    @Override
    public <T> In<T> in(Expression<? extends T> expression) {
        return new InPredicate<T>(this, expression);
    }
    
    @Override
    public <T> Predicate in(Expression<? extends T> x, Iterable<T> values) {
        In<T> in = this.in(x);
        if (values != null) {
            for (T value : values) {
                in.value(value);
            }
        }
        return in;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> Predicate in(Expression<? extends T> x, Subquery<T> subquery) {
        return this.in(x).value((Expression) subquery);
    }

    @Override
    public <Y> Expression<Y> coalesce(
            Expression<? extends Y> x, Expression<? extends Y> y) {
        return this.<Y>coalesce()
                .value(x)
                .value(y);
    }

    @Override
    public <Y> Expression<Y> coalesce(Expression<? extends Y> x, Y y) {
        return this.coalesce(x, this.literal(y));
    }

    @Override
    public <T> Coalesce<T> coalesce() {
        return new CoalesceExpression<T>(this);
    }

    @Override
    public <Y> Expression<Y> nullif(Expression<Y> x, Expression<?> y) {
        return new NullifExpression<Y>(this, x, y);
    }

    @Override
    public <Y> Expression<Y> nullif(Expression<Y> x, Y y) {
        return this.nullif(x, this.literal(y));
    }

    @Override
    public <C, R> SimpleCase<C, R> selectCase(Expression<? extends C> expression) {
        return new SimpleCaseExpression<C, R>(this, expression);
    }

    @Override
    public <R> Case<R> selectCase() {
        return new SearchedCaseExpression<R>(this);
    }

    @Override
    public <T> Expression<T> function(String name, final Class<T> type, Expression<?>... args) {
        
        return new AbstractFunction<T>(this, name, args) {

            private static final long serialVersionUID = 1983144347737818446L;

            @Override
            public Class<? extends T> getJavaType() {
                return type;
            }
            
        };
    }

    @Override
    public <X, T extends X> XRoot<T> treat(Root<X> root, Class<T> type) {
        return new TreatedRootImpl<>((RootImpl<X>)root, type);
    }

    @Override
    public <X, K, T, V extends T> XMapJoin<X, K, V> treat(MapJoin<X, K, T> join, Class<V> type) {
        return new TreatedMapAttributeJoin<>((MapAttributeJoin<X, K, T>)join, type);
    }

    @Override
    public <X, T, E extends T> XListJoin<X, E> treat(ListJoin<X, T> join, Class<E> type) {
        return new TreatedListAttributeJoin<>((ListAttributeJoin<X, T>)join, type);
    }

    @Override
    public <X, T, E extends T> XSetJoin<X, E> treat(SetJoin<X, T> join, Class<E> type) {
        return new TreatedSetAttributeJoin<>((SetAttributeJoin<X, T>)join, type);
    }

    @Override
    public <X, T, E extends T> XCollectionJoin<X, E> treat(CollectionJoin<X, T> join, Class<E> type) {
        return new TreatedCollectionAttributeJoin<>((CollectionAttributeJoin<X, T>)join, type);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <X, T, V extends T> XJoin<X, V> treat(Join<X, T> join, Class<V> type) {
        if (join instanceof MapJoin<?, ?, ?>) {
            return this.treat((MapJoin)join, type);
        }
        if (join instanceof ListJoin<?, ?>) {
            return this.treat((ListJoin<X, T>)join, type);
        }
        if (join instanceof SetJoin<?, ?>) {
            return this.treat((SetJoin<X, T>)join, type);
        }
        if (join instanceof CollectionJoin<?, ?>) {
            return this.treat((CollectionJoin<X, T>)join, type);
        }
        Arguments.mustBeInstanceOfValue("join", join, SingularAttributeJoin.class);
        return new TreatedSingularAttributeJoin<>((SingularAttributeJoin<X, T>)join, type);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <X, T extends X> Path<T> treat(Path<X> path, Class<T> type) {
        if (path instanceof Join<?, ?>) {
            return this.treat((Join)path, type);
        }
        Arguments.mustBeInstanceOfValue("path", path, SingularAttributePath.class);
        return new TreatedSingularAttributePath<>((SingularAttributePath<X>)path, type);
    }

    @Override
    public <X, Y> DependencyPredicateBuilder<X, Y> dependencyPredicateBuilder(From<?, X> from, Class<Y> targetType) {
        return new DependencyPredicateBuilderImpl<>((AbstractFrom<?, X>)from, targetType);
    }
}
