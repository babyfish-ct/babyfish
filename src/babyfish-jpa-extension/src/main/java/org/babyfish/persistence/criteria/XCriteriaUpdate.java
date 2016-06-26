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
package org.babyfish.persistence.criteria;

import java.util.List;

import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

/**
 * @author Tao Chen
 */
public interface XCriteriaUpdate<T> extends XCommonAbstractCriteria, CriteriaUpdate<T> {

    @Override
    XRoot<T> from(Class<T> entityClass);

    @Override
    XRoot<T> from(EntityType<T> entity);

    @Override
    XRoot<T> getRoot();
    
    List<Assignment> getAssignments();

    @Override
    <Y, X extends Y> XCriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, X value);

    @Override
    <Y> XCriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value);

    @Override
    <Y, X extends Y> XCriteriaUpdate<T> set(Path<Y> attribute, X value);

    @Override
    <Y> XCriteriaUpdate<T> set(Path<Y> attribute, Expression<? extends Y> value);

    @Override
    XCriteriaUpdate<T> set(String attributeName, Object value);

    @Override
    XCriteriaUpdate<T> where(Expression<Boolean> restriction);

    @Override
    XCriteriaUpdate<T> where(Predicate... restrictions);
}
