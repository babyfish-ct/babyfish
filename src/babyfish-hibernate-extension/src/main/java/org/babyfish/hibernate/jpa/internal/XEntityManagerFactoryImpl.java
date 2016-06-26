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

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceException;
import javax.persistence.SynchronizationType;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.babyfish.collection.MACollections;
import org.babyfish.hibernate.cfg.SettingsFactory;
import org.babyfish.hibernate.ejb.HibernateXEntityManagerFactory;
import org.babyfish.hibernate.internal.QueryTemplateImpl;
import org.babyfish.hibernate.internal.XSessionFactoryImplementor;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfish.persistence.criteria.QueryTemplate;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaDelete;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XCriteriaUpdate;
import org.babyfish.persistence.criteria.spi.AbstractCriteriaBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.jpa.boot.internal.SettingsImpl;
import org.hibernate.jpa.internal.EntityManagerFactoryImpl;
import org.hibernate.model.hibernate.spi.dialect.LimitedListDialect;

/**
 * @author Tao Chen
 */
public class XEntityManagerFactoryImpl extends EntityManagerFactoryImpl implements HibernateXEntityManagerFactory {

    private static final long serialVersionUID = 7083006207314953694L;
    
    private XSessionFactoryImplementor sessionFactory;
    
    private PersistenceUnitTransactionType transactionType;
    
    private boolean discardOnClose;
    
    private Class<?> sessionInterceptorClass;
    
    private int inPredicateMaxPartitionSize;
    
    private boolean dbSchemaStrict;
    
    private XCriteriaBuilder criteriaBuilder = new AbstractCriteriaBuilder() {
        @Override
        public XEntityManagerFactory getEntityManagerFactory() {
            return XEntityManagerFactoryImpl.this;
        }
    };

    public XEntityManagerFactoryImpl(
            String persistenceUnitName,
            XSessionFactoryImplementor sessionFactory, 
            SettingsImpl settings,
            Map<?, ?> configurationValues, 
            org.babyfish.hibernate.cfg.Configuration cfg) {
        super(
                persistenceUnitName, 
                ((XSessionFactoryImplementor)sessionFactory).getRawSessionFactoryImpl(), 
                settings, 
                configurationValues, 
                cfg);
        this.sessionFactory = sessionFactory;
        this.transactionType = settings.getTransactionType();
        this.discardOnClose = settings.isReleaseResourcesOnCloseEnabled();
        this.sessionInterceptorClass = settings.getSessionInterceptorClass();
        this.inPredicateMaxPartitionSize = this.readInPredicateMaxPartitionSize();
        this.dbSchemaStrict = SettingsFactory.isDbSchemaStrict(sessionFactory.getProperties());
    }

    @Override
    public XCriteriaBuilder getCriteriaBuilder() {
        return this.criteriaBuilder;
    }

    @Override
    public XEntityManager createEntityManager() {
        return this.onCreateEntityManager(SynchronizationType.SYNCHRONIZED, MACollections.emptyMap());
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public XEntityManager createEntityManager(Map map) {
        return this.onCreateEntityManager(SynchronizationType.SYNCHRONIZED, map);
    }
    
    @Override
    public XEntityManager createEntityManager(SynchronizationType synchronizationType) {
        this.errorIfResourceLocalDueToExplicitSynchronizationType();
        return this.onCreateEntityManager(synchronizationType, MACollections.emptyMap());
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public XEntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        this.errorIfResourceLocalDueToExplicitSynchronizationType();
        return this.onCreateEntityManager(synchronizationType, map);
    }
    
    public <T> QueryTemplate<T> createQueryTemplate(XCriteriaQuery<T> criteriaQuery) {
        return new QueryTemplateImpl<T>(criteriaQuery);
    }
    
    public <T> QueryTemplate<T> createQueryTemplate(XCriteriaUpdate<T> criteriaUpdate) {
        return new QueryTemplateImpl<T>(criteriaUpdate);
    }
    
    public <T> QueryTemplate<T> createQueryTemplate(XCriteriaDelete<T> criteriaDelete) {
        return new QueryTemplateImpl<T>(criteriaDelete);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> cls) {
        if (SessionFactory.class.isAssignableFrom(cls)) {
            return (T)this.sessionFactory;
        }
        if (SessionFactoryImplementor.class.isAssignableFrom( cls ) ) {
            return (T) sessionFactory;
        }
        if (EntityManager.class.isAssignableFrom(cls)) {
            return (T) this;
        }
        throw new PersistenceException( "Hibernate cannot unwrap EntityManagerFactory as " + cls.getName() );
    }
    
    public XSessionFactoryImplementor getSessionFactoryImplementor() {
        return this.sessionFactory;
    }
    
    @Deprecated
    @Override
    public final SessionFactoryImpl getSessionFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInPredicateMaxPartitionSize() {
        return this.inPredicateMaxPartitionSize;
    }
    
    @Override
    public boolean isDbSchemaStrict() {
        return this.dbSchemaStrict;
    }

    protected XEntityManager onCreateEntityManager(SynchronizationType synchronizationType, Map<?, ?> map) {
        this.validateNotClosed();
        return new XEntityManagerImpl(
                this,
                PersistenceContextType.EXTENDED,
                synchronizationType,
                transactionType,
                discardOnClose,
                sessionInterceptorClass,
                map
        );
    }
    
    protected final PersistenceUnitTransactionType getTransactionType() {
        return this.transactionType;
    }

    protected final boolean isDiscardOnClose() {
        return this.discardOnClose;
    }

    protected final Class<?> getSessionInterceptorClass() {
        return this.sessionInterceptorClass;
    }
    
    protected final void errorIfResourceLocalDueToExplicitSynchronizationType() {
        if ( transactionType == PersistenceUnitTransactionType.RESOURCE_LOCAL ) {
            throw new IllegalStateException(
                    "Illegal attempt to specify a SynchronizationType when building an EntityManager from a " +
                            "EntityManagerFactory defined as RESOURCE_LOCAL "
            );
        }
    }
    
    private int readInPredicateMaxPartitionSize() {
        int maxListLength;
        Dialect dialect = this.getSessionFactoryImplementor().getDialect();
        if (dialect instanceof LimitedListDialect) {
            maxListLength = ((LimitedListDialect)dialect).getMaxListLength();
        } else {
            maxListLength = Integer.MAX_VALUE;
        }
        return Math.max(maxListLength, 2);
    }
}
