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

import java.util.List;

import org.babyfish.hibernate.hql.XQueryTranslatorImpl;
import org.hibernate.HibernateException;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.ast.tree.SelectClause;
import org.hibernate.loader.hql.QueryLoader;
import org.hibernate.transform.ResultTransformer;

/**
 * @author Tao Chen
 */
public class UnlimitedCountLoader extends QueryLoader {

    protected XQueryTranslatorImpl queryTranslator;
    
    protected boolean distinct;
    
    public UnlimitedCountLoader(
            XQueryTranslatorImpl queryTranslator,
            SessionFactoryImplementor factory, 
            SelectClause selectClause, 
            boolean distinct) {
        super(queryTranslator, factory, selectClause);
        this.queryTranslator = queryTranslator;
        this.distinct = distinct;
    }
    
    @Override
    public String getSQLString() {
        return this.distinct?
                this.queryTranslator.getDistinctCountSQLString() :
                this.queryTranslator.getCountSQLString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final List<Long> list(
            SessionImplementor session, 
            QueryParameters queryParameters)
            throws HibernateException {
        boolean hasLimit = 
                queryParameters.getRowSelection() != null && 
                queryParameters.getRowSelection().definesLimits();
        if (hasLimit) {
            RowSelection selection = new RowSelection();
            selection.setFetchSize( queryParameters.getRowSelection().getFetchSize() );
            selection.setTimeout( queryParameters.getRowSelection().getTimeout() );
            queryParameters = queryParameters.createCopyUsing(selection);
        }
        return super.list(session, queryParameters);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List getResultList(List results,
            ResultTransformer resultTransformer) throws QueryException {
        return results;
    }
}
