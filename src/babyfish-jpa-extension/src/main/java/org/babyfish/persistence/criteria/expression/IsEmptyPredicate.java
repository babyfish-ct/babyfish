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

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractSimplePredicate;
import org.babyfish.persistence.criteria.spi.PluralAttributePath;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class IsEmptyPredicate<C extends Collection<?>> extends AbstractSimplePredicate {

    private static final long serialVersionUID = -1318502324135818122L;
    
    private PluralAttributePath<C> collection;
    
    public IsEmptyPredicate(XCriteriaBuilder criteriaBuilder, PluralAttributePath<C> collection) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("collection", collection);
        this.mustUnderSameCriteriaBuilder("collection", collection);
        this.collection = collection;
    }

    public PluralAttributePath<C> getCollection() {
        return this.collection;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitIsEmptyPredicate(this);
    }

    @Override
    public int getPriority() {
        return PriorityConstants.COMPARASION;
    }
}
