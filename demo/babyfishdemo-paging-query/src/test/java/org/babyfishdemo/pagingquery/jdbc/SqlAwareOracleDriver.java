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
package org.babyfishdemo.pagingquery.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.babyfish.lang.UncheckedException;

/**
 * @author Tao Chen
 */
public class SqlAwareOracleDriver extends SqlAwareDriver {

    public SqlAwareOracleDriver() {
        super(createRawDriver());
    }

    private static Driver createRawDriver() {
        try {
            Class<?> clazz = Class.forName("oracle.jdbc.OracleDriver");
            return (Driver)clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw UncheckedException.rethrow(ex);
        }
    }

    static {
        try {
            DriverManager.registerDriver(new SqlAwareOracleDriver());
        } catch (SQLException ex) {
            UncheckedException.rethrow(ex);
        }
    }
}
