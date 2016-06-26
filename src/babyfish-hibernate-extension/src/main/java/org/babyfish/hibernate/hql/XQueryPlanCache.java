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
package org.babyfish.hibernate.hql;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.UncheckedException;
import org.babyfish.model.jpa.path.spi.PathPlanKey;
import org.hibernate.Filter;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.FilterImpl;
import org.hibernate.internal.util.collections.BoundedConcurrentHashMap;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tao Chen
 */
public class XQueryPlanCache extends QueryPlanCache {

    private static final long serialVersionUID = 6240348305328496209L;
    
    private static final Logger log = LoggerFactory.getLogger(XQueryPlanCache.class);
    
    private static boolean QUERY_PLAN_LOCATE_INFO_ENABLED = 
            "true".equals(System.getProperty(
                    XQueryPlanCache.class.getName() +
                    "PLAN_LOCATED_INFO_ENABLED")
            );
    
    private static final Field PLAN_CACHE_FIELD;
    
    private static final Constructor<?> DYNAMIC_FILTER_KEY_CONSTRUCTOR;
    
    private SessionFactoryImplementor factory;
    
    protected final BoundedConcurrentHashMap<XHQLQueryPlanKey, XQueryPlan> queryPlanCache;
    
    @SuppressWarnings("unchecked")
    public XQueryPlanCache(SessionFactoryImplementor factory) {
        super(factory);
        this.factory = factory;
        try {
            this.queryPlanCache = (BoundedConcurrentHashMap<XHQLQueryPlanKey, XQueryPlan>)PLAN_CACHE_FIELD.get(this);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" }) 
    @Override
    public final XQueryPlan getHQLQueryPlan(
            String queryString, 
            boolean shallow,
            Map enabledFilters) throws QueryException, MappingException {
        return this.getHQLQueryPlan0(queryString, null, shallow, enabledFilters);
    }
    
    public final XQueryPlan getHQLQueryPlan(
            String queryString,
            PathPlanKey pathPlanKey,
            boolean shallow,
            Map<String, Filter> enabledFilters) throws QueryException, MappingException {
        return getHQLQueryPlan0(
                queryString, 
                pathPlanKey, 
                shallow, 
                enabledFilters);
    }

    private XQueryPlan getHQLQueryPlan0(
            String queryString,
            PathPlanKey pathPlanKey,
            boolean shallow,
            Map<String, Filter> enabledFilters) throws QueryException, MappingException {
        XHQLQueryPlanKey key = new XHQLQueryPlanKey(
                queryString,
                pathPlanKey, 
                shallow, 
                enabledFilters);
        XQueryPlan plan = (XQueryPlan)this.queryPlanCache.get(key);
        if (plan == null) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "unable to locate HQL query plan in cache; generating (" + 
                        queryString + 
                        ") when the root query path plan key is :" + 
                        pathPlanKey == null ? "null" : "\r\n" + pathPlanKey);
            }
            XQueryPlan.registerPathPlanKey(pathPlanKey);
            plan = this.createHQLQueryPlan(queryString, pathPlanKey, shallow, enabledFilters, this.factory);
        }
        else {
            if (log.isTraceEnabled()) {
                log.trace(
                        "located HQL query plan in cache (" + 
                        queryString + 
                        ") when the root query path path key is:\r\n" + 
                        pathPlanKey == null ? "null" : "\r\n" + pathPlanKey);
            } else if (QUERY_PLAN_LOCATE_INFO_ENABLED && log.isInfoEnabled()) {
                log.info(
                        "located HQL query plan in cache (" + 
                        queryString + 
                        ") when the root query path path key is:\r\n" + 
                        pathPlanKey == null ? "null" : "\r\n" + pathPlanKey);
            }
        }
        this.queryPlanCache.put(key, plan);
        return plan;
    }
    
    protected XQueryPlan createHQLQueryPlan(
            String queryString,
            PathPlanKey pathPlanKey,
            boolean shallow,
            Map<String, Filter> enabledFilters,
            SessionFactoryImplementor factory) {
        return new XQueryPlan(
                queryString, 
                shallow, 
                enabledFilters, 
                this.factory);
    }
    
    protected final SessionFactoryImplementor getFactory() {
        return this.factory;
    }
    
    private static Object newDynamicFilterKey(FilterImpl filterImpl) {
        try {
            return DYNAMIC_FILTER_KEY_CONSTRUCTOR.newInstance(filterImpl);
        } catch (InstantiationException ex) {
            throw new AssertionError("Internal bug", ex);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        } catch (InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex.getTargetException());
        }
    }

    protected static class XHQLQueryPlanKey implements Serializable {
    
        private static final long serialVersionUID = -8220417824588885830L;

        private String query;
        
        private PathPlanKey pathPlanKey;
        
        private boolean shallow;
        
        //Set<DynamicFilterKey> but DynamicFilterKey is not visible
        private Set<Object> filterKeys;
        
        private transient int hashCode;

        public XHQLQueryPlanKey(
                String query, 
                PathPlanKey pathPlanKey, 
                boolean shallow, 
                Map<String, Filter> enabledFilters) {
            this.query = query;
            this.pathPlanKey = pathPlanKey;
            this.shallow = shallow;
            if (enabledFilters == null || enabledFilters.isEmpty()) {
                filterKeys = MACollections.emptySet();
            }
            else {
                Set<Object> tmp = new HashSet<Object>(
                        CollectionHelper.determineProperSizing( enabledFilters ),
                        CollectionHelper.LOAD_FACTOR);
                for (Filter filter : enabledFilters.values()) {
                    tmp.add(newDynamicFilterKey((FilterImpl)filter));
                }
                this.filterKeys = MACollections.unmodifiable(tmp);
            }
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof XHQLQueryPlanKey)) {
                return false;
            }
            final XHQLQueryPlanKey other = (XHQLQueryPlanKey)o;
            return this.shallow == other.shallow &&
                    Nulls.equals(this.pathPlanKey, other.pathPlanKey) &&
                    this.filterKeys.equals(other.filterKeys) && 
                    this.query.equals(other.query);
        }

        public int hashCode() {
            int hash = this.hashCode;
            if (hash == 0) {
                hash = query.hashCode();
                hash = 31 * hash + Nulls.hashCode(this.pathPlanKey);
                hash = 29 * hash + (shallow ? 1 : 0);
                hash = 29 * hash + this.filterKeys.hashCode();
                if (hash == 0) {
                    hash = -1;
                }
                this.hashCode = hash;
            }
            return hash;
        }
    }
    
    static {
        Constructor<?> constructor = null;
        for (Class<?> clazz : QueryPlanCache.class.getDeclaredClasses()) {
            if (clazz.getSimpleName().equals("DynamicFilterKey")) {
                try {
                    constructor = clazz.getDeclaredConstructor(FilterImpl.class);
                } catch (NoSuchMethodException ex) {
                    throw new AssertionError("Internal bug", ex);
                }
                break;
            }
        }
        if (constructor == null) {
            throw new AssertionError();
        }
        constructor.setAccessible(true);
        
        Field planCacheField;
        try {
            planCacheField = QueryPlanCache.class.getDeclaredField("queryPlanCache");
        } catch (NoSuchFieldException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        planCacheField.setAccessible(true);
        
        DYNAMIC_FILTER_KEY_CONSTRUCTOR = constructor;
        PLAN_CACHE_FIELD  = planCacheField;
    }
}
