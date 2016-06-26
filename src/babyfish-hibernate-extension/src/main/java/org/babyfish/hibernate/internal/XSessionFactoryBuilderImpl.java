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
package org.babyfish.hibernate.internal;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.babyfish.hibernate.XMetadata;
import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.XSessionFactoryBuilder;
import org.babyfish.hibernate.hql.XQueryPlanCache;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Interceptor;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.proxy.EntityNotFoundDelegate;

/**
 * @author Tao Chen
 */
public class XSessionFactoryBuilderImpl implements XSessionFactoryBuilder {
    
    private static final Field QUERY_PLAN_CACHE_FIELD;
    
    SessionFactoryOptionsImpl options;

    private final MetadataImplementor metadata;

    protected XSessionFactoryBuilderImpl(XMetadata metadata) {
        this.metadata = (MetadataImplementor)metadata;
        options = new SessionFactoryOptionsImpl(metadata.getOptions().getServiceRegistry());
    }

    @Override
    public XSessionFactoryBuilder with(Interceptor interceptor) {
        this.options.interceptor = interceptor;
        return this;
    }

    @Override
    public XSessionFactoryBuilder with(EntityNotFoundDelegate entityNotFoundDelegate) {
        this.options.entityNotFoundDelegate = entityNotFoundDelegate;
        return this;
    }

    @Override
    public XSessionFactory build() {
        SessionFactoryImpl factory;
        try {
            factory = new SessionFactoryImpl(this.metadata, this.options, null);
        } finally {
            //TODO: this.namedQueries.putAll(this.tmpNamedQueryDefinitions);
            //TODO: this.tmpNamedQueryDefinitions.clear();
        }
        
        //TODO: setNamedQueries(factory, this.namedQueries);
        //TODO: checkNamedQueries(factory);
        setQueryPlanceCache(factory, this.createQueryPlanCache(factory));
        return SessionFactoryImplWrapper.wrap(factory);
    }
    
    protected XQueryPlanCache createQueryPlanCache(SessionFactoryImplementor factory) {
        return new XQueryPlanCache(factory);
    }
    
    static void setQueryPlanceCache(SessionFactoryImpl factory, QueryPlanCache queryPlanCache) {
        try {
            QUERY_PLAN_CACHE_FIELD.set(factory, queryPlanCache);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        }
    }

    private static class SessionFactoryOptionsImpl implements SessionFactory.SessionFactoryOptions {

        private Interceptor interceptor = EmptyInterceptor.INSTANCE;

        private EntityNotFoundDelegate entityNotFoundDelegate = new EntityNotFoundDelegate() {
                public void handleEntityNotFound(String entityName, Serializable id) {
                    throw new ObjectNotFoundException( id, entityName );
                }
        };
        
        private StandardServiceRegistry serviceRegistry;
        
        public SessionFactoryOptionsImpl(StandardServiceRegistry serviceRegistry) {
            this.serviceRegistry = serviceRegistry;
        }

        @Override
        public Interceptor getInterceptor() {
            return this.interceptor;
        }

        @Override
        public EntityNotFoundDelegate getEntityNotFoundDelegate() {
            return this.entityNotFoundDelegate;
        }

        @Override
        public StandardServiceRegistry getServiceRegistry() {
            return this.serviceRegistry;
        }
    }
    
    static {
        Field queryPlanCacheField;
        try {
            queryPlanCacheField = SessionFactoryImpl.class.getDeclaredField("queryPlanCache");
        } catch (NoSuchFieldException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        queryPlanCacheField.setAccessible(true);
        
        QUERY_PLAN_CACHE_FIELD = queryPlanCacheField;
    }
}
