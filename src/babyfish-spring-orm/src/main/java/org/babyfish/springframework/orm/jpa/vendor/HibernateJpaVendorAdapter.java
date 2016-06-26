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
package org.babyfish.springframework.orm.jpa.vendor;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;

import org.babyfish.hibernate.ejb.HibernateXEntityManagerFactory;
import org.babyfish.persistence.XEntityManager;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.DerbyDialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.InformixDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SybaseDialect;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;

/**
 * @author Tao Chen
 */
@SuppressWarnings("deprecation")
public class HibernateJpaVendorAdapter extends AbstractJpaVendorAdapter {

    private final PersistenceProvider persistenceProvider = 
            new org.babyfish.hibernate.jpa.HibernatePersistenceProvider();

    private final JpaDialect jpaDialect = new HibernateJpaDialect();


    public PersistenceProvider getPersistenceProvider() {
        return this.persistenceProvider;
    }

    @Override
    public String getPersistenceProviderRootPackage() {
        return "org.hibernate";
    }

    @Override
    public Map<String, Object> getJpaPropertyMap() {
        Map<String, Object> jpaProperties = new HashMap<String, Object>();

        if (getDatabasePlatform() != null) {
            jpaProperties.put(Environment.DIALECT, getDatabasePlatform());
        }
        else if (getDatabase() != null) {
            Class<?> databaseDialectClass = determineDatabaseDialectClass(getDatabase());
            if (databaseDialectClass != null) {
                jpaProperties.put(Environment.DIALECT, databaseDialectClass.getName());
            }
        }

        if (isGenerateDdl()) {
            jpaProperties.put(Environment.HBM2DDL_AUTO, "update");
        }
        if (isShowSql()) {
            jpaProperties.put(Environment.SHOW_SQL, "true");
        }

        return jpaProperties;
    }

    /**
     * Determine the Hibernate database dialect class for the given target database.
     * @param database the target database
     * @return the Hibernate database dialect class, or {@code null} if none found
     */
    protected Class<?> determineDatabaseDialectClass(Database database) {
        switch (database) {
            case ORACLE: return org.babyfish.hibernate.dialect.Oracle10gDialect.class;
            case DB2: return DB2Dialect.class;
            case DERBY: return DerbyDialect.class;
            case H2: return H2Dialect.class;
            case HSQL: return HSQLDialect.class;
            case INFORMIX: return InformixDialect.class;
            case MYSQL: return MySQLDialect.class;
            case POSTGRESQL: return PostgreSQLDialect.class;
            case SQL_SERVER: return SQLServerDialect.class;
            case SYBASE: return SybaseDialect.class;
            default: return null;
        }
    }

    @Override
    public JpaDialect getJpaDialect() {
        return this.jpaDialect;
    }

    @Override
    public Class<? extends EntityManagerFactory> getEntityManagerFactoryInterface() {
        return HibernateXEntityManagerFactory.class;
    }

    @Override
    public Class<? extends EntityManager> getEntityManagerInterface() {
        return XEntityManager.class;
    }

}
