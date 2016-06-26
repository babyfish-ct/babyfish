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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.dialect.pagination.LimitHandler;

/**
 * @author Tao Chen
 */
abstract class AbstractLitmitHandlerWrapper implements LimitHandler {

    protected final LimitHandler raw;

    protected AbstractLitmitHandlerWrapper(LimitHandler raw) {
        this.raw = raw;
    }

    @Override
    public boolean supportsLimit() {
        return this.raw.supportsLimit();
    }

    @Override
    public boolean supportsLimitOffset() {
        return this.raw.supportsLimitOffset();
    }

    @Override
    public String getProcessedSql() {
        return this.raw.getProcessedSql();
    }

    @Override
    public int bindLimitParametersAtStartOfQuery(PreparedStatement statement,
            int index) throws SQLException {
        return this.raw.bindLimitParametersAtStartOfQuery(statement, index);
    }

    @Override
    public int bindLimitParametersAtEndOfQuery(PreparedStatement statement,
            int index) throws SQLException {
        return this.raw.bindLimitParametersAtEndOfQuery(statement, index);
    }

    @Override
    public void setMaxRows(PreparedStatement statement) throws SQLException {
        this.raw.setMaxRows(statement);
    }
}
