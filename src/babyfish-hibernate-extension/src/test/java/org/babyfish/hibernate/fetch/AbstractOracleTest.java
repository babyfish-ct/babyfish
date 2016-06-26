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
package org.babyfish.hibernate.fetch;

import java.util.Map;
import java.util.Properties;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.junit.FilterDeclaration;
import org.babyfish.junit.FilterRunner;
import org.junit.runner.RunWith;

/**
 * @author Tao Chen
 */
@RunWith(FilterRunner.class)
@FilterDeclaration(filterClass = OracleFilter.class)
/*
 * Or
 * @FilterDeclarations(
 *      @FilterDeclaration(filterClass = OracleFilter.class)
 * )
 */
public abstract class AbstractOracleTest {

    private static final Map<String, String> ORACLE_PROPERTY_MAP;
    
    protected static Map<String, String> getOraclePropertyMap() {
        return ORACLE_PROPERTY_MAP;
    }
    
    protected static Properties getOracleProperites() {
        Properties properties = new Properties();
        properties.putAll(ORACLE_PROPERTY_MAP);
        return properties;
    }
    
    static {
        Map<String, String> map = new HashMap<>();
        String oracle = System.getProperty("oracle");
        if (oracle != null) {
            // For eclipse run/debug configuration, "-Doracle" means 
            // empty string, but for shell, it means "-Doracle=true"
            if (!oracle.startsWith("jdbc:oracle:")) { 
                oracle = "jdbc:oracle:thin:@localhost:1521:babyfish";
            }
            map.put(
                    "hibernate.dialect", 
                    // babfish's dialect, not hibernate's dialect
                    "org.babyfish.hibernate.dialect.Oracle10gDialect"
            );
            map.put(
                    "hibernate.connection.driver_class", 
                    "oracle.jdbc.OracleDriver"
            );
            map.put(
                    "hibernate.connection.url", 
                    oracle
            );
            map.put(
                    "hibernate.connection.username", 
                    System.getProperty("oracle.user", "babyfish")
            );
            map.put(
                    "hibernate.connection.password", 
                    System.getProperty("oracle.password", "123")
            );
        }
        ORACLE_PROPERTY_MAP = MACollections.unmodifiable(map);
    }
}
