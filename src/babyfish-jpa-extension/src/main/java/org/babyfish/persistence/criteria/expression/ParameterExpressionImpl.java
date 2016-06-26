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

import javax.persistence.criteria.ParameterExpression;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.persistence.Constants;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractExpression;
import org.babyfish.persistence.criteria.spi.Visitor;

/**
 * @author Tao Chen
 */
public class ParameterExpressionImpl<T> extends AbstractExpression<T> implements ParameterExpression<T> {
    
    private static final long serialVersionUID = 4709437146322229160L;
    
    private Class<T> parameterType;
    
    private String name;
    
    private Integer position;

    public ParameterExpressionImpl(XCriteriaBuilder criteriaBuilder, Class<T> parameterType, String name) {
        super(criteriaBuilder);
        Arguments.mustNotBeNull("parameterType", parameterType);
        if (name != null && name.startsWith(Constants.LITERAL_PARAMTER_NAME_PREFIX)) {
            throw new IllegalArgumentException(
                    parameterCanNotStartWith("name", Constants.LITERAL_PARAMTER_NAME_PREFIX)
            );
        }
        this.parameterType = parameterType;
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

    @Override
    public Class<? extends T> getJavaType() {
        return this.parameterType;
    }

    @Override
    public Class<T> getParameterType() {
        return this.parameterType;
    }
    
    public boolean setPosition(int position) {
        //Don't check this.isLocked()
        //because it is loced before this.setPosition
        
        Arguments.mustBeGreaterThanOrEqualToValue("position", position, 0);
        if (this.name != null) {
            throw new UnsupportedOperationException(canNotSetPositionOfNamedParameter());
        }
        if (this.position == null) {
            this.position = position;
            return true;
        }
        return false;
    }

    @Override
    public Integer getPosition() {
        return this.position;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitParameterExpression(this);
    }

    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }
    
    @I18N
    private static native String canNotSetPositionOfNamedParameter();
    
    @I18N
    private static native String parameterCanNotStartWith(String parameterName, String prefix);
}
