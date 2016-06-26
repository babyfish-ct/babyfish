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
package org.babyfish.persistence.criteria.spi;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.persistence.Constants;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XTypedQuery;
import org.babyfish.persistence.criteria.QueryTemplate;
import org.babyfish.persistence.criteria.XCommonAbstractCriteria;
import org.babyfish.persistence.criteria.XCriteriaQuery;

/**
 * @author Tao Chen
 */
public abstract class AbstractQueryTemplate<T> implements QueryTemplate<T> {
    
    private Class<T> resultType;
    
    private CompoundSelection<Tuple> tupleSelection;
    
    private String jpql;
    
    private Collection<LiteralParameter> literalParameters;
    
    @SuppressWarnings("unchecked")
    protected AbstractQueryTemplate(XCommonAbstractCriteria commonAbstractCriteria) {
        if (commonAbstractCriteria instanceof XCriteriaQuery<?>) {
            XCriteriaQuery<T> query = (XCriteriaQuery<T>)commonAbstractCriteria;
            this.resultType = query.getResultType();
            if (this.resultType == Tuple.class) {
                this.tupleSelection = (CompoundSelection<Tuple>)query.getSelection();
        }
        }
        this.init(commonAbstractCriteria);
    }
    
    @Override
    public Class<T> getResultType() {
        return this.resultType;
    }
    
    @Override
    public XTypedQuery<T> createQuery(XEntityManager entityManager) {
        XTypedQuery<T> query = entityManager.createQuery(this.jpql, this.getResultType());
        if (this.tupleSelection != null) {
            this.setTupleTransfromer(query, this.tupleSelection);
        }
        for (LiteralParameter ip : this.literalParameters) {
            query.setParameter(ip.getName(), ip.getValue());
        }
        return query;
    }
    
    @Override
    public Query createUpdate(XEntityManager entityManager) {
        Query query = entityManager.createQuery(this.jpql);
        for (LiteralParameter ip : this.literalParameters) {
            query.setParameter(ip.getName(), ip.getValue());
        }
        return query;
    }
    
    @Override
    public Query createDelete(XEntityManager entityManager) {
        Query query = entityManager.createQuery(this.jpql);
        for (LiteralParameter ip : this.literalParameters) {
            query.setParameter(ip.getName(), ip.getValue());
        }
        return query;
    }
    
    @Override
    public String toString() {
        return this.jpql;
    }

    protected abstract void init(XCommonAbstractCriteria query);
    
    protected abstract void setTupleTransfromer(XTypedQuery<?> query, CompoundSelection<Tuple> tupleSelection);
    
    protected final void init(String jpql, Collection<LiteralParameter> implictParameters) {
        Arguments.mustNotBeNull("jpql", jpql);
        Arguments.mustNotBeEmpty("jpql", jpql);
        this.jpql = jpql;
        if (implictParameters == null) {
        this.literalParameters = MACollections.emptySet();
        } else {
            this.literalParameters = MACollections.unmodifiable(implictParameters);
        }
    }
    
    protected static final class LiteralParameter implements Serializable {
        
        private static final long serialVersionUID = 336399478255717833L;

        private String name;
        
        private Object value;
        
        public LiteralParameter(int literalPosition, Object value) {
            this.name = Constants.LITERAL_PARAMTER_NAME_PREFIX + literalPosition;
            this.value = value;
        }

        public String getName() {
            return this.name;
        }

        public Object getValue() {
            return this.value;
        }
    }
}
