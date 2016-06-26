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
package org.babyfish.persistence.criteria.expression;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Expression;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.model.jpa.path.spi.EntityManagerFactoryConfigurable;
import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractSimplePredicate;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class InPredicate<T> extends AbstractSimplePredicate implements In<T> {

    private static final long serialVersionUID = -2210373342558775003L;
    
    private Expression<? extends T> expression;
    
    private List<Expression<? extends T>> values;
    
    private List<Partition<T>> partitions;
    
    private int maxPartitionSize;

    public InPredicate(
            XCriteriaBuilder criteriaBuilder, 
            Expression<? extends T> expression) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("expression", expression);
        this.mustUnderSameCriteriaBuilder("expression", expression);
        this.expression = expression;
        this.values = new ArrayList<>();
        XEntityManagerFactory entityManagerFactory = criteriaBuilder.getEntityManagerFactory();
        Arguments.mustBeInstanceOfValue("entityManagerFactory", entityManagerFactory, EntityManagerFactoryConfigurable.class);
        this.maxPartitionSize = 
                ((EntityManagerFactoryConfigurable)entityManagerFactory)
                .getInPredicateMaxPartitionSize();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Expression<T> getExpression() {
        return (Expression<T>)this.expression;
    }
    
    public List<Expression<? extends T>> getValues() {
        return MACollections.unmodifiable(this.values);
    }

    @Override
    public In<T> value(Expression<? extends T> value) {
        this.checkState();
        if (value != null) {
            this.mustUnderSameCriteriaBuilder("value", value);
            this.values.add(value);
        }
        return this;
    }

    @Override
    public In<T> value(T value) {
        return this.value(this.getCriteriaBuilder().literal(value));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitInPredicate(this);
    }
    
    @Override
    public int getPriority() {
        return PriorityConstants.COMPARASION;
    }
    
    public int getMaxParationSize() {
        return this.maxPartitionSize;
    }
    
    public List<Partition<T>> getPartitions() {
        List<Partition<T>> partitions = this.partitions;
        if (partitions == null) {
            throw new IllegalStateException(
                    methodCanOnlyBeInvokedWhenThisIsFrozen("getPartitions", InPredicate.class)
            );
        }
        return partitions;
    }

    @Override
    protected void onFrozen() {
        int size = this.values.size();
        int maxParationSize = this.maxPartitionSize;
        int partitionCount = (int)(((long)size + maxParationSize - 1) / maxParationSize);
        List<Partition<T>> partitions = new ArrayList<>(partitionCount);
        for (int i = 0; i < partitionCount; i++) {
            int from = i * maxParationSize;
            int to = Math.min(from + maxParationSize, size);
            partitions.add(new Partition<T>(this.values.subList(from, to)));
        }
        this.partitions = MACollections.unmodifiable(partitions);
    }

    @Override
    protected void onUnfrozen() {
        this.partitions = null;
    }
    
    public static class Partition<T> {
        
        private List<Expression<? extends T>> values;
        
        private boolean needExpand;
        
        private Partition(List<Expression<? extends T>> values) {
            this.values = MACollections.unmodifiable(values);
            for (Expression<? extends T> expression : this.values) {
                if (!(expression instanceof LiteralExpression<?>) &&
                        !(expression instanceof ConstantExpression<?>)) {
                    this.needExpand = true;
                    break;
                }
            }
        }
        
        public List<Expression<? extends T>> getValues() {
            return this.values;
        }

        public boolean isNeedExpand() {
            return this.needExpand;
        }
    }
    
    @SuppressWarnings("rawtypes")
    @I18N
    private static native String methodCanOnlyBeInvokedWhenThisIsFrozen(String methodName, Class<InPredicate> predicateType);
}
