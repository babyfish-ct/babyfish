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
package org.babyfish.hibernate.cfg;

import java.util.Map;
import java.util.Properties;

import org.babyfish.hibernate.hql.XQueryPlan;
import org.babyfish.hibernate.hql.XQueryTranslatorFactory;
import org.babyfish.hibernate.hql.XQueryTranslatorFactoryImpl;
import org.babyfish.lang.I18N;
import org.hibernate.HibernateException;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.query.spi.EntityGraphQueryHint;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.spi.FilterTranslator;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.hql.spi.QueryTranslatorFactory;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tao Chen
 */
public class SettingsFactory extends org.hibernate.cfg.SettingsFactory {

    private static final long serialVersionUID = -2662239924044486522L;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsFactory.class);
    
    private static final String OBJECT_QUERY_TRANSLATOR_FACTORY = "babyfish.hibernate.object_query_translator_factory";

    public static final String ENABLE_LIMIT_IN_MEMORY = "babyfish.hibernate.enable_limit_in_memory";
    
    public static final String CREATE_ORACLE_DISTINCT_RANK = "babyfish.hibernate.create_oracle_distinct_rank";
    
    public static final String STRICT_DB_SCHEMA = "babyfish.hibernate.strict_db_schema";
    
    public static boolean isLimitInMemoryEnabled(Map<?, ?> properties) {
        return isTrue(properties, ENABLE_LIMIT_IN_MEMORY);
    }
    
    public static boolean isDistinctRankCreateable(Map<?, ?> properties) {
        return isTrue(properties, CREATE_ORACLE_DISTINCT_RANK);
    }
    
    public static boolean isDbSchemaStrict(Map<?, ?> properties) {
        return isTrue(properties, STRICT_DB_SCHEMA);
    }
    
    private static boolean isTrue(Map<?, ?> properties, String propertyName) {
        if (properties == null) {
            return false;
        }
        Object value = properties.get(propertyName);
        return "true".equals(value) || Boolean.TRUE.equals(value);
    }

    @Override
    protected final QueryTranslatorFactory createQueryTranslatorFactory(Properties properties, ServiceRegistry serviceRegistry) {
        if (properties.containsKey(Environment.QUERY_TRANSLATOR)) {
            throw new HibernateException(
                    "the property \"" +
                    Environment.QUERY_TRANSLATOR +
                    "\" is deprecated by \"" +
                    OBJECT_QUERY_TRANSLATOR_FACTORY +
                    "\"");
        }
        String className = ConfigurationHelper.getString(
                AvailableSettings.QUERY_TRANSLATOR, 
                properties,
                XQueryTranslatorFactoryImpl.class.getName());
        LOGGER.info("Entity query translator: " + className);
        final XQueryTranslatorFactory translatorFactory;
        Class<?> xQueryTranslatorFactoryImplClass;
        try {
            xQueryTranslatorFactoryImplClass = ReflectHelper.classForName(className);
        } catch (ClassNotFoundException ex) {
            throw new HibernateException(
                    notExistingXQueryTransalatorFactoryImpl(
                            XQueryTranslatorFactory.class,
                            className
                    ),
                    ex
            );
        }
        try {
            translatorFactory = (XQueryTranslatorFactory)xQueryTranslatorFactoryImplClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new HibernateException(
                    failedToInstantiateXQueryTranslatorFactory(
                            XQueryTranslatorFactory.class, 
                            xQueryTranslatorFactoryImplClass
                    ), 
                    ex);
        }
        
        return new QueryTranslatorFactory() {
            
            @SuppressWarnings({ "unchecked", "rawtypes" }) 
            @Override
            public QueryTranslator createQueryTranslator(
                    String queryIdentifier,
                    String queryString, 
                    Map filters, 
                    SessionFactoryImplementor factory,
                    EntityGraphQueryHint entityGraphQueryHint) {
                return translatorFactory.createQueryTranslator(
                        queryIdentifier, 
                        queryString, 
                        XQueryPlan.currentPathPlanKey(), 
                        filters, 
                        factory,
                        entityGraphQueryHint);
            }
            
            @SuppressWarnings({ "unchecked", "rawtypes" }) 
            @Override
            public FilterTranslator createFilterTranslator(
                    String queryIdentifier,
                    String queryString, 
                    Map filters, 
                    SessionFactoryImplementor factory) {
                return translatorFactory.createFilterTranslator(
                        queryIdentifier, 
                        queryString, 
                        XQueryPlan.currentPathPlanKey(), 
                        filters, 
                        factory);
            }
        };
    }

    @I18N    
    private static native String notExistingXQueryTransalatorFactoryImpl(Class<?> interfaceType, String implementationClassName);
        
    @I18N    
    private static native String failedToInstantiateXQueryTranslatorFactory(Class<?> interfaceType, Class<?> implementationType);
}
