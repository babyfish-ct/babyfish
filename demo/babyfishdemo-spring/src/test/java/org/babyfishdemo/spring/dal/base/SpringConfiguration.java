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
package org.babyfishdemo.spring.dal.base;

import javax.sql.DataSource;

import org.babyfishdemo.spring.dal.jdbc.SqlAwareUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author Tao Chen
 */
@Configuration
@ImportResource("classpath:unit-test.spring.xml")
public class SpringConfiguration {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String oracle = System.getProperty("oracle");
        if (oracle != null) {
            // For eclipse run/debug configuration, "-Doracle" means 
            // empty string, but for shell, it means "-Doracle=true"
            if (!oracle.startsWith("jdbc:oracle:")) { 
                oracle = "jdbc:oracle:thin:@localhost:1521:babyfish";
            }
            String oracleUser = System.getProperty("oracle.user", "babyfish_demo");
            String oraclePassword = System.getProperty("oracle.password", "123");
            dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
            dataSource.setUrl(oracle);
            dataSource.setUsername(oracleUser);
            dataSource.setPassword(oraclePassword);
        } else {
            dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
            dataSource.setUrl("jdbc:hsqldb:mem:babyfishdemo-spring");
            dataSource.setUsername("sa");
        }
        return SqlAwareUtil.wrap(dataSource);
    }
    
    @Bean
    public String hibernateDialect() {
        return System.getProperty("oracle") != null ?
                
                // Oracle: This is babyfish's dialect, not hibernate's dialect
                "org.babyfish.hibernate.dialect.Oracle10gDialect" :
                    
                // HSQLDB
                "org.hibernate.dialect.HSQLDialect";
    }
}
