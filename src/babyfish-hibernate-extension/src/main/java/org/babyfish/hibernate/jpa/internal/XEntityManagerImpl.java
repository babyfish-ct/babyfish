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
package org.babyfish.hibernate.jpa.internal;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.hibernate.XSession;
import org.babyfish.hibernate.internal.QueryTemplateImpl;
import org.babyfish.hibernate.internal.XSessionBuilderImplementor;
import org.babyfish.hibernate.internal.XSessionImplementor;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.UncheckedException;
import org.babyfish.model.jpa.path.QueryPath;
import org.babyfish.model.jpa.path.QueryPaths;
import org.babyfish.model.jpa.path.TypedQueryPath;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XQuery;
import org.babyfish.persistence.XTypedQuery;
import org.babyfish.persistence.criteria.QueryTemplate;
import org.babyfish.persistence.criteria.XCommonAbstractCriteria;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaDelete;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XCriteriaUpdate;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jpa.internal.EntityManagerImpl;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Tao Chen
 */
public class XEntityManagerImpl extends EntityManagerImpl implements XEntityManager {

    private static final long serialVersionUID = 3359332222148371123L;
    
    private static final Method APPLY_PROPERTIES;
    
    private Class<?> sessionInterceptorClass;

    public XEntityManagerImpl(
            XEntityManagerFactoryImpl entityManagerFactory,
            PersistenceContextType pcType,
            SynchronizationType synchronizationType,
            PersistenceUnitTransactionType transactionType,
            boolean discardOnClose, 
            Class<?> sessionInterceptorClass,
            Map<?, ?> properties) {
        super(
                entityManagerFactory, 
                pcType, 
                synchronizationType, 
                transactionType,
                discardOnClose, 
                sessionInterceptorClass, 
                properties);
        this.sessionInterceptorClass = sessionInterceptorClass;
    }

    @Override
    public XEntityManagerFactoryImpl getEntityManagerFactory() {
        return (XEntityManagerFactoryImpl)super.getEntityManagerFactory();
    }
    
    @Override
    public XCriteriaBuilder getCriteriaBuilder() {
        return this.getEntityManagerFactory().getCriteriaBuilder();
    }

    @Override
    public <E> E find(Class<E> entityClass, Object primaryKey, String ... queryPaths) {
        return this.findByQueryPaths(entityClass, primaryKey, null, QueryPaths.compile(queryPaths));
    }

    @Override
    public <E> E find(Class<E> entityClass, Object primaryKey, LockModeType lockModeType, String ... queryPaths) {
        return this.findByQueryPaths(entityClass, primaryKey, lockModeType, QueryPaths.compile(queryPaths));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E find(Class<E> entityClass, Object primaryKey, TypedQueryPath<E> ... queryPaths) {
        return this.findByQueryPaths(entityClass, primaryKey, null, queryPaths);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E find(Class<E> entityClass, Object primaryKey, LockModeType lockModeType, TypedQueryPath<E> ... queryPaths) {
        return this.findByQueryPaths(entityClass, primaryKey, lockModeType, queryPaths);
    }
    
    @Override
    public <E> List<E> find(Class<E> entityClass, Iterable<?> primaryKeys, String ... queryPaths) {
        return this.findByQueryPaths(entityClass, primaryKeys, null, QueryPaths.compile(queryPaths));
    }

    @Override
    public <E> List<E> find(Class<E> entityClass, Iterable<?> primaryKeys, LockModeType lockModeType, String ... queryPaths) {
        return this.findByQueryPaths(entityClass, primaryKeys, lockModeType, QueryPaths.compile(queryPaths));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> List<E> find(Class<E> entityClass, Iterable<?> primaryKeys, TypedQueryPath<E> ... queryPaths) {
        return this.findByQueryPaths(entityClass, primaryKeys, null, queryPaths);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> List<E> find(Class<E> entityClass, Iterable<?> primaryKeys, LockModeType lockModeType, TypedQueryPath<E> ... queryPaths) {
        return this.findByQueryPaths(entityClass, primaryKeys, lockModeType, queryPaths);
    }
    
    @Override
    public XQuery createQuery(String qlString) {
        this.checkOpen();
        try {
            XSession session = (XSession)this.internalGetSession();
            return this.applyProperties(new XTypedQueryImpl<>(session.createQuery(qlString), this));
        }
        catch (RuntimeException ex) {
            throw convert(ex);
        }
    }
    
    @Override
    public XQuery createNamedQuery(String name) {
        return this.createNamedQuery(name, null);
    }

    @Override
    public <T> XTypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        this.checkOpen();
        try {
            XSession session = (XSession)this.internalGetSession();
            org.babyfish.hibernate.XQuery query = session.createQuery(qlString);
            resultClassChecking(resultClass, query);
            return new XTypedQueryImpl<>(query, this);
        }
        catch (RuntimeException ex) {
            throw convert(ex);
        }
    }
    
    @Override
    public <T> XTypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        this.checkOpen();
        SessionFactoryImplementor sfi = this.getEntityManagerFactory().getSessionFactoryImplementor();

        // only hql/jpql query, not support native sql
        NamedQueryDefinition namedQueryDefinition = sfi.getNamedQueryRepository().getNamedQueryDefinition(name);
        if (namedQueryDefinition != null) {
            XSessionImplementor session = (XSessionImplementor)this.internalGetSession();
            org.babyfish.hibernate.XQuery query = session.createQuery( namedQueryDefinition );
            if (resultClass != null ) {
                resultClassChecking(resultClass, query);
            }
            XTypedQueryImpl<T> jpaQuery = new XTypedQueryImpl<T>(query, this);
            this.applySavedSettings(namedQueryDefinition, jpaQuery);
            return jpaQuery;
        }
        throw convert(new IllegalArgumentException( "No query defined for that name[" + name + "]"));
    }
    
    @Override
    public <T> XTypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return this
                .getEntityManagerFactory()
                .createQueryTemplate(
                        (XCriteriaQuery<T>)criteriaQuery
                ).createQuery(this);
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Query createQuery(CriteriaUpdate criteriaUpdate) {
        return new QueryTemplateImpl<Object>((XCommonAbstractCriteria)criteriaUpdate).createUpdate(this);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Query createQuery(CriteriaDelete criteriaDelete) {
        return new QueryTemplateImpl<Object>((XCommonAbstractCriteria)criteriaDelete).createUpdate(this);
    }
    
    @Override
    public <T> QueryTemplate<T> createQueryTemplate(XCriteriaQuery<T> criteriaQuery) {
        return new QueryTemplateImpl<T>(criteriaQuery);
    }

    @Override
    public <T> QueryTemplate<T> createQueryTemplate(XCriteriaUpdate<T> criteriaUpdate) {
        return new QueryTemplateImpl<T>(criteriaUpdate);
    }
    
    @Override
    public <T> QueryTemplate<T> createQueryTemplate(XCriteriaDelete<T> criteriaDelete) {
        return new QueryTemplateImpl<T>(criteriaDelete);
    }
    
    @Override
    protected XEntityManagerFactoryImpl internalGetEntityManagerFactory() {
        return (XEntityManagerFactoryImpl)super.internalGetEntityManagerFactory();
    }

    @Override
    protected XSession internalGetSession() {
        XSession session = (XSession)this.session;
        if (session == null ) {
            XSessionBuilderImplementor sessionBuilder = internalGetEntityManagerFactory().getSessionFactoryImplementor().withOptions();
            sessionBuilder.owner( this );
            if (this.sessionInterceptorClass != null) {
                try {
                    Interceptor interceptor = (Interceptor)this.sessionInterceptorClass.newInstance();
                    sessionBuilder.interceptor( interceptor );
                }
                catch (InstantiationException ex) {
                    throw new PersistenceException("Unable to instantiate session interceptor: " + this.sessionInterceptorClass, ex);
                }
                catch (IllegalAccessException ex) {
                    throw new PersistenceException("Unable to instantiate session interceptor: " + this.sessionInterceptorClass, ex);
                }
                catch (ClassCastException ex) {
                    throw new PersistenceException("Session interceptor does not implement Interceptor: " + this.sessionInterceptorClass, ex);
                }
            }
            sessionBuilder.autoJoinTransactions( getTransactionType() != PersistenceUnitTransactionType.JTA );
            this.session = session = sessionBuilder.openSession();
        }
        return session;
    }

    protected <E> E findByQueryPaths(
            Class<E> entityClass, 
            Object primaryKey, 
            LockModeType nullableLockModeType, //can be null 
            QueryPath[] queryPaths) {
        if (queryPaths == null || queryPaths.length == 0) {
            return this.find(entityClass, primaryKey, nullableLockModeType, (Map<String, Object>)null);
        }
        
        //This exception need not to be converted because this is a reading action so that it is not need to markAsRollback
        Arguments.mustBeInstanceOfValue("primaryKey", primaryKey, Serializable.class);
        
        try {
            SessionFactoryImplementor sfi = (SessionFactoryImplementor)this.getEntityManagerFactory().unwrap(SessionFactoryImplementor.class);
            EntityPersister persister = sfi.getEntityPersister(entityClass.getName());
            String idPropertyName = persister.getIdentifierPropertyName();
            XTypedQuery<E> typedQuery = 
                    this
                    .createQuery(
                            "select babyfish_alias_0 from " + 
                            entityClass.getName() + 
                            " babyfish_alias_0 where babyfish_alias_0." + 
                            idPropertyName + 
                            " = :id", 
                            entityClass
                    )
                    .setQueryPaths(queryPaths)
                    .setParameter("id", primaryKey);
            if (nullableLockModeType != null) {
                typedQuery.setLockMode(nullableLockModeType);
            }
            return typedQuery.getSingleResult(true);
        } catch (NoResultException ex) {
            throw new EntityNotFoundException();
        } catch (MappingException ex) {
            throw new IllegalArgumentException(ex);
        } catch (HibernateException ex) {
            throw this.convert(ex);
        }
    }
    
    protected <E> List<E> findByQueryPaths(
            Class<E> entityClass, 
            Iterable<?> primaryKeys, 
            LockModeType nullableLockModeType, //can be null 
            QueryPath[] queryPaths) {
        //This exception need not to be converted because this is a reading action so that it is not need to markAsRollback
        Arguments.mustNotBeNull("primaryKeys", primaryKeys);
        
        Set<Object> primaryKeySet; 
        if (primaryKeys instanceof Collection<?>) {
            primaryKeySet = new LinkedHashSet<>((((Collection<?>)primaryKeys).size() * 4 + 2) / 3);
        } else {
            primaryKeySet = new LinkedHashSet<>();
        }
        for (Object primaryKey : primaryKeys) {
            if (primaryKey != null) {
                primaryKeySet.add(primaryKey);
            }
        }
        if (primaryKeySet.isEmpty()) {
            //Let the returned list can be modified, so don't let it return MACollections.emptyList();
            return new ArrayList<>();
        }
        
        try {
            SessionFactoryImplementor sfi = (SessionFactoryImplementor)this.getEntityManagerFactory().unwrap(SessionFactoryImplementor.class);
            EntityPersister persister = sfi.getEntityPersister(entityClass.getName());
            String idPropertyName = persister.getIdentifierPropertyName();
            XTypedQuery<E> typedQuery;
            if (primaryKeySet.size() == 1) {
                typedQuery =
                        this
                        .createQuery(
                                "select babyfish_alias_0 from " + 
                                entityClass.getName() + 
                                " babyfish_alias_0 where babyfish_alias_0." + 
                                idPropertyName + 
                                " = :id", 
                                entityClass
                        )
                        .setParameter("id", primaryKeySet.iterator().next());
            } else {
                typedQuery =
                        this
                        .createQuery(
                                "select babyfish_alias_0 from " + 
                                entityClass.getName() + 
                                " babyfish_alias_0 where babyfish_alias_0." + 
                                idPropertyName + 
                                " in (:ids)", 
                                entityClass
                        )
                        .setParameter("ids", primaryKeySet);
            }
            typedQuery.setQueryPaths(queryPaths);
            if (nullableLockModeType != null) {
                typedQuery.setLockMode(nullableLockModeType);
            }
            return typedQuery.getResultList();
        } catch (MappingException ex) {
            throw new IllegalArgumentException(ex);
        } catch (HibernateException ex) {
            throw this.convert(ex);
        }
    }
    
    protected XQuery applyProperties(XQuery query) {
        try {
            return (XQuery)APPLY_PROPERTIES.invoke(this, query);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        } catch (InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex.getTargetException());
        }
    }
    
    static {
        Method method = null;
        for (Class<?> clazz = EntityManagerImpl.class; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod("applyProperties", javax.persistence.Query.class);
            } catch (NoSuchMethodException ex) {
                // Ignore exception
            }
        }
        if (method == null) {
            throw new AssertionError("no method applyProperties in EntityManagerImpl");
        }
        method.setAccessible(true);
        APPLY_PROPERTIES = method;
    }
}
