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

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

/**
 * @author Tao Chen
 */
public interface XJoin<Z, X> extends Join<Z, X>, XFrom<Z, X> {
    
    @Override
    XJoin<Z, X> alias(String alias);
    
    @Override
    XFrom<?, Z> getParent();
    
    @Override
    XJoin<Z, X> getCorrelationParent();
    
    JoinMode getJoinMode();
    
    @Override
    XJoin<Z, X> on(Expression<Boolean> restriction);

    @Override
    XJoin<Z, X> on(Predicate... restrictions);
}
