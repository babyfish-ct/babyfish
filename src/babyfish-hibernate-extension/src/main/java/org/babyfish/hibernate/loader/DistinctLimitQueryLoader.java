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
package org.babyfish.hibernate.loader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.babyfish.hibernate.dialect.DistinctLimitDialect;
import org.babyfish.hibernate.hql.XQueryTranslatorImpl;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitHelper;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.ast.tree.SelectClause;
import org.hibernate.loader.hql.QueryLoader;
import org.hibernate.loader.spi.AfterLoadAction;
import org.hibernate.transform.ResultTransformer;

/**
 * @author Tao Chen
 */
public class DistinctLimitQueryLoader extends QueryLoader {
    
    public DistinctLimitQueryLoader(
            XQueryTranslatorImpl queryTranslator,
            SessionFactoryImplementor factory, 
            SelectClause selectClause) {
        super(queryTranslator, factory, selectClause);
    }

    @Override
    protected LimitHandler getLimitHandler(String sql, RowSelection selection) {
        Dialect dialect = this.getFactory().getDialect();
        if (dialect instanceof DistinctLimitDialect && LimitHelper.hasMaxRows(selection)) {
            return ((DistinctLimitDialect)dialect).buildDistinctLimitHandler(sql, selection);
        }
        return super.getLimitHandler(sql, selection);
    }

    @Override
    protected List<?> processResultSet(
            ResultSet rs,
            QueryParameters queryParameters, 
            SessionImplementor session,
            boolean returnProxies, 
            ResultTransformer forcedResultTransformer,
            int maxRows, 
            List<AfterLoadAction> afterLoadActions)
            throws SQLException {
        Dialect dialect = this.getFactory().getDialect();
        if (dialect instanceof DistinctLimitDialect) {
            return super.processResultSet(
                    rs, 
                    queryParameters, 
                    session, 
                    returnProxies,
                    forcedResultTransformer, 
                    Integer.MAX_VALUE, 
                    afterLoadActions);
        }
        return super.processResultSet(
                rs, 
                queryParameters, 
                session, 
                returnProxies,
                forcedResultTransformer, 
                maxRows, 
                afterLoadActions);
    }
}
