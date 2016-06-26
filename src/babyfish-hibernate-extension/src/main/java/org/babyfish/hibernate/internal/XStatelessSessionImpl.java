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
import java.sql.Connection;

import org.babyfish.lang.Arguments;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.procedure.ProcedureCall;

/**
 * @author Tao Chen
 */
@SuppressWarnings("rawtypes")
public class XStatelessSessionImpl implements StatelessSession {
    
    private static final long serialVersionUID = -2051561430345184911L;
    
    private StatelessSession raw;
    
    public XStatelessSessionImpl(StatelessSession raw) {
        Arguments.mustNotBeNull("raw", raw);
        this.raw = raw;
    }
    
    @Override
    public final int hashCode() {
        return this.raw.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return this.raw.equals(obj);
    }

    @Override
    public void delete(Object entity) {
        this.raw.delete(entity);
    }

    @Override
    public void delete(String entityName, Object entity) {
        this.raw.delete(entityName, entity);
    }

    @Override
    public void close() {
        this.raw.close();
    }

    @Override
    public Serializable insert(Object entity) {
        return this.raw.insert(entity);
    }

    @Override
    public Serializable insert(String entityName, Object entity) {
        return this.raw.insert(entityName, entity);
    }

    @Override
    public void update(Object entity) {
        this.raw.update(entity);
    }

    @Override
    public void update(String entityName, Object entity) {
        this.raw.update(entityName, entity);
    }

    @Override
    public Object get(String entityName, Serializable id) {
        return this.raw.get(entityName, id);
    }

    @Override
    public Object get(Class entityClass, Serializable id) {
        return this.raw.get(entityClass, id);
    }

    @Override
    public Object get(String entityName, Serializable id, LockMode lockMode) {
        return this.raw.get(entityName, id, lockMode);
    }

    @Override
    public Object get(Class entityClass, Serializable id, LockMode lockMode) {
        return this.raw.get(entityClass, id, lockMode);
    }

    @Override
    public void refresh(Object entity) {
        this.raw.refresh(entity);
    }

    @Override
    public void refresh(String entityName, Object entity) {
        this.raw.refresh(entityName, entity);
    }

    @Override
    public void refresh(Object entity, LockMode lockMode) {
        this.raw.refresh(entity, lockMode);
    }

    @Override
    public void refresh(String entityName, Object entity, LockMode lockMode) {
        this.raw.refresh(entityName, entity, lockMode);
    }

    @Override
    public Query createQuery(String queryString) {
        return this.raw.createQuery(queryString);
    }

    @Override
    public Query getNamedQuery(String queryName) {
        return this.raw.getNamedQuery(queryName);
    }

    @Override
    public Criteria createCriteria(Class persistentClass) {
        return this.raw.createCriteria(persistentClass);
    }

    @Override
    public Criteria createCriteria(Class persistentClass, String alias) {
        return this.raw.createCriteria(persistentClass, alias);
    }

    @Override
    public Criteria createCriteria(String entityName) {
        return this.raw.createCriteria(entityName);
    }

    @Override
    public Criteria createCriteria(String entityName, String alias) {
        return this.raw.createCriteria(entityName, alias);
    }

    @Override
    public SQLQuery createSQLQuery(String queryString)
            throws HibernateException {
        return this.raw.createSQLQuery(queryString);
    }

    @Override
    public Transaction beginTransaction() {
        return this.raw.beginTransaction();
    }

    @Override
    public Transaction getTransaction() {
        return this.raw.getTransaction();
    }

    @Deprecated
    @Override
    public Connection connection() {
        return this.raw.connection();
    }

    @Override
    public String getTenantIdentifier() {
        return this.raw.getTenantIdentifier();
    }

    @Override
    public ProcedureCall getNamedProcedureCall(String name) {
        return this.raw.getNamedProcedureCall(name);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName) {
        return this.raw.createStoredProcedureCall(procedureName);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {
        return this.raw.createStoredProcedureCall(procedureName, resultClasses);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {
        return this.raw.createStoredProcedureCall(procedureName, resultSetMappings);
    }
    
}

