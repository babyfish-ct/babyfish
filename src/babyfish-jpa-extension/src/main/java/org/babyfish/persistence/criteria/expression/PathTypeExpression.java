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

import javax.persistence.criteria.Path;

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class PathTypeExpression<T> extends AbstractExpression<Class<? extends T>> {
    
    private static final long serialVersionUID = 1124672496445720079L;
    
    private Path<T> path;

    public PathTypeExpression(XCriteriaBuilder criteriaBuilder, Path<T> path) {
        super(criteriaBuilder);
        this.path = this.mustUnderSameCriteriaBuilder("path", Arguments.mustNotBeNull("path", path));
    }
    
    public Path<T> getPath() {
        return this.path;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Class<? extends T>> getJavaType() {
        return (Class<? extends Class<? extends T>>)this.path.getJavaType();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPathTypeExpression(this);
    }
    
    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }

}
