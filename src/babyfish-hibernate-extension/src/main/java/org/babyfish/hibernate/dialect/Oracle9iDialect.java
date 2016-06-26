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
package org.babyfish.hibernate.dialect;

import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitHelper;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.model.hibernate.spi.dialect.LimitedListDialect;

/**
 * @author Tao Chen
 */
public class Oracle9iDialect 
extends org.hibernate.dialect.Oracle9iDialect 
implements DistinctLimitDialect, LimitedListDialect, InstallableDialect {
    
    @Override
    public LimitHandler buildDistinctLimitHandler(
            final String sql,
            final RowSelection selection) {
        return new AbstractLitmitHandlerWrapper(this.buildLimitHandler(sql, selection)) {
            @Override
            public String getProcessedSql() {
                boolean hasOffset = 
                        supportsLimit() && 
                        supportsLimitOffset() && 
                        LimitHelper.hasFirstRow( selection ) && 
                        LimitHelper.hasMaxRows( selection );
                return OracleDistinctLimits.getOracleDistinctLimitString(sql, hasOffset);
            }
        };
    }
    
    @Override
    public int getMaxListLength() {
        return 1000;
    }
    
    @Override
    public void install(SessionFactoryImplementor sfi) {
        OracleDistinctLimits.install(sfi);
    }
}
