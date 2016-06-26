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
package org.babyfish.hibernate.count;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.hsqldb.jdbcDriver;

/**
 * @author Tao Chen
 */
public class SqlAwareDriver extends jdbcDriver {
    
    private static final ThreadLocal<List<String>> SQL_LIST_LOCAL = new ThreadLocal<>();

    public static void clearSqlList() {
        SQL_LIST_LOCAL.set(null);
    }
    
    public static List<String> getSqlList() {
        List<String> sqlList = SQL_LIST_LOCAL.get();
        if (sqlList == null) {
            return MACollections.emptyList();
        }
        return MACollections.unmodifiable(sqlList);
    }
    
    @Override
    public Connection connect(String connectionString, Properties properties) throws SQLException {
        final Connection con = super.connect(connectionString, properties);
        return (Connection)Proxy.newProxyInstance(
                con.getClass().getClassLoader(), 
                con.getClass().getInterfaces(), 
                new InvocationHandler() {
                    @Override
                    public Object invoke(
                            Object proxy, 
                            Method method,
                            Object[] args) throws Throwable {
                        if (method.getName().equals("prepareStatement") && args.length > 0 && args[0] instanceof String) {
                            List<String> sqlList = SQL_LIST_LOCAL.get();
                            if (sqlList == null) {
                                sqlList = new ArrayList<>();
                                SQL_LIST_LOCAL.set(sqlList);
                            }
                            sqlList.add((String)args[0]);
                        }
                        return method.invoke(con, args);
                    }
                }
        );
    }
}
