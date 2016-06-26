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

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;

import org.babyfish.persistence.criteria.Assignment;
import org.babyfish.persistence.criteria.XCriteriaBuilder;

/**
 * @author Tao Chen
 */
public class AssignmentImpl extends AbstractNode implements Assignment {

    private static final long serialVersionUID = 4247830982724634943L;

    private Path<?> path;
    
    private Expression<?> expression;

    AssignmentImpl(XCriteriaBuilder criteriaBuilder, Path<?> path, Expression<?> expression) {
        super(criteriaBuilder);
        this.path = path;
        this.expression = expression;
    }

    public Path<?> getPath() {
        return path;
    }

    public Expression<?> getExpression() {
        return expression;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAssignment(this);
    }
}
