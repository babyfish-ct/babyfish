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

import java.sql.Date;

import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.spi.AbstractFunction;

/**
 * @author Tao Chen
 */
public class CurrentDateFunction extends AbstractFunction<Date> {
    
    private static final long serialVersionUID = -6106486273438185559L;
    
    private static final String FUNCTION_NAME = "current_date";

    public CurrentDateFunction(
            XCriteriaBuilder criteriaBuilder) {
        super(criteriaBuilder, FUNCTION_NAME);
    }

    @Override
    public Class<? extends Date> getJavaType() {
        return Date.class;
    }

}
