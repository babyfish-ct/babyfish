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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.hibernate.cfg.Configuration;
import org.babyfish.hibernate.cfg.SettingsFactory;
import org.babyfish.lang.I18N;
import org.babyfish.model.jpa.path.spi.PathPlanKey;
import org.babyfish.persistence.QueryType;
import org.hibernate.Filter;
import org.hibernate.QueryException;
import org.hibernate.engine.query.spi.EntityGraphQueryHint;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.spi.QueryTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tao Chen
 */
public class XQueryPlan extends HQLQueryPlan {
    
    private static final long serialVersionUID = 4858445245681674156L;
    
    private static final Logger log = LoggerFactory.getLogger(XQueryPlan.class);
    
    private static final ThreadLocal<Object> PATH_PLAN_KEY_TL = new ThreadLocal<Object>();
    
    private PathPlanKey pathPlanKey;
    
    static void registerPathPlanKey(PathPlanKey pathPlanKey) {
        PATH_PLAN_KEY_TL.set(pathPlanKey != null ? pathPlanKey : new Object());
    }
    
    public static PathPlanKey currentPathPlanKey() {
        Object o = PATH_PLAN_KEY_TL.get();
        if (o == null && !Configuration.isPathPlanKeyVlidationSuspended()) {
            throw new IllegalStateException(
                    notDuringAnyInvocation(
                            XQueryPlanCache.class, 
                            "getHQLQueryPlan", 
                            new Class[] {
                                String.class,
                                boolean.class,
                                Map.class
                            }, 
                            new Class[] {
                                String.class,
                                PathPlanKey.class,
                                boolean.class,
                                Map.class
                            }
                    )
            );
        }
        return o instanceof PathPlanKey ? (PathPlanKey)o : null;
    }

    protected XQueryPlan(
            String hql, 
            boolean shallow,
            Map<String, Filter> enabledFilters, 
            SessionFactoryImplementor factory) {
        super(hql, shallow, enabledFilters, factory);
        this.pathPlanKey = currentPathPlanKey();
        PATH_PLAN_KEY_TL.remove();
    }

    protected XQueryPlan(
            String hql, 
            String collectionRole,
            boolean shallow, 
            Map<String, Filter> enabledFilters,
            SessionFactoryImplementor factory,
            EntityGraphQueryHint entityGraphQueryHint) {
        super(hql, collectionRole, shallow, enabledFilters, factory, entityGraphQueryHint);
        this.pathPlanKey = currentPathPlanKey();
        PATH_PLAN_KEY_TL.remove();
    }

    public final PathPlanKey getPathPlanKey() {
        return this.pathPlanKey;
    }
    
    public <T> List<T> performList(
            SessionImplementor session,
            QueryParameters queryParameters, 
            QueryType queryMode) {
        if (log.isTraceEnabled()) {
            log.trace( "find: " + getSourceQuery() );
            queryParameters.traceParameters(session.getFactory());
        }
        QueryTranslator[] translators = this.getTranslators();
        boolean hasLimit = 
                queryParameters.getRowSelection() != null &&
                queryParameters.getRowSelection().definesLimits();
        boolean needsMemoryLimit = hasLimit && translators.length > 1;
        QueryParameters queryParametersToUse;
        if (needsMemoryLimit) {
            if (!SettingsFactory.isLimitInMemoryEnabled(session.getFactory().getProperties())) {
                throw new QueryException(
                        hibernateLimitInMemoryForPolymorphicQueryIsNotEnabled(
                                SettingsFactory.ENABLE_LIMIT_IN_MEMORY
                        )
                );
            }
            log.warn("firstResult/maxResults specified on polymorphic query; applying in memory!");
            RowSelection selection = new RowSelection();
            selection.setFetchSize( queryParameters.getRowSelection().getFetchSize() );
            selection.setTimeout( queryParameters.getRowSelection().getTimeout() );
            queryParametersToUse = queryParameters.createCopyUsing( selection );
        }
        else {
            queryParametersToUse = queryParameters;
        }

        List<T> combinedResults = new ArrayList<T>();
        Set<T> distinction = new LinkedHashSet<T>(ReferenceEqualityComparator.getInstance());
        int includedCount = -1;
        translator_loop: for (int i = 0; i < translators.length; i++ ) {
            List<T> tmp = ((XQueryTranslator)translators[i]).list(session, queryParametersToUse, queryMode);
            if (needsMemoryLimit) {
                // NOTE : firstRow is zero-based
                int first = queryParameters.getRowSelection().getFirstRow() == null
                            ? 0
                            : queryParameters.getRowSelection().getFirstRow().intValue();
                int max = queryParameters.getRowSelection().getMaxRows() == null
                            ? -1
                            : queryParameters.getRowSelection().getMaxRows().intValue();
                final int size = tmp.size();
                for (int x = 0; x < size; x++) {
                    final T result = tmp.get(x);
                    if (!distinction.add(result)) {
                        continue;
                    }
                    includedCount++;
                    if (includedCount < first) {
                        continue;
                    }
                    combinedResults.add(result);
                    if (max >= 0 && includedCount > max) {
                        break translator_loop; // break the outer loop !!!
                    }
                }
            }
            else {
                if (translators.length == 1) {
                    return tmp;
                } else {
                    combinedResults.addAll(tmp);
                }
            }
        }
        return combinedResults;
    }

    public long performUnlimitedCount(
            SessionImplementor session,
            QueryParameters queryParameters, 
            QueryType queryType) {
        long count = 0;
        for (QueryTranslator translator : this.getTranslators()) {
            XQueryTranslator xTranslator = (XQueryTranslator)translator;
            count += xTranslator.unlimitedCount(session, queryParameters, queryType);
        }
        return count;
    }

    @I18N
    private static native String notDuringAnyInvocation(
                Class<XQueryPlanCache> ownerType, 
                String methodName,
                Class<?>[] parameterTypes1,
                Class<?>[] parameterTypes2);
        
    @I18N
    private static native String hibernateLimitInMemoryForPolymorphicQueryIsNotEnabled(String configurationPropertyName);
}
