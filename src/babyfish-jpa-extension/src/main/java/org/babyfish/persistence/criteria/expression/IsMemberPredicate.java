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

import java.util.Collection;

import javax.persistence.criteria.Expression;

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractSimplePredicate;
import org.babyfish.persistence.criteria.spi.PluralAttributePath;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class IsMemberPredicate<E, C extends Collection<E>> extends AbstractSimplePredicate {
    
    private static final long serialVersionUID = 8290313787272548779L;

    private Expression<E> element;

    private PluralAttributePath<C> collection;
    
    public IsMemberPredicate(
            XCriteriaBuilder criteriaBuilder,
            Expression<E> element, 
            PluralAttributePath<C> collection) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("element", element);
        Arguments.mustNotBeNull("collection", collection);
        this.mustUnderSameCriteriaBuilder("element", element);
        this.mustUnderSameCriteriaBuilder("collection", collection);
        this.element = element;
        this.collection = collection;
    }

    public Expression<E> getElement() {
        return this.element;
    }

    public PluralAttributePath<C> getCollection() {
        return this.collection;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitIsMemberPredicate(this);
    }
    
    @Override
    public int getPriority() {
        return PriorityConstants.COMPARASION;
    }
}
