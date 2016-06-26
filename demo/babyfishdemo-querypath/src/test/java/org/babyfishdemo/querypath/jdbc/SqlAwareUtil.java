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
package org.babyfishdemo.querypath.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Set;

import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;

/**
 * @author Tao Chen
 */
class SqlAwareUtil {
    
    public static Connection wrap(final Connection con) {
        if (con instanceof SqlAware) {
            return con;
        }
        return (Connection)Proxy.newProxyInstance(
                SqlAware.class.getClassLoader(), 
                getWrapperInterfaces(con), 
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("prepareStatement") && 
                                args.length != 0 && 
                                args[0] instanceof String) {
                            SqlRecorder.prepare((String)args[0]);
                        }
                        return method.invoke(con, args);
                    }
                }
        );
    }
    
    private static Class<?>[] getWrapperInterfaces(Object o) {
        Set<Class<?>> set = new LinkedHashSet<>();
        for (Class<?> type = o.getClass(); type != Object.class; type = type.getSuperclass()) {
            set.addAll(MACollections.wrap(type.getInterfaces()));
        }
        set.add(SqlAware.class);
        return set.toArray(new Class[set.size()]);
    }
    
    private interface SqlAware {}
}
