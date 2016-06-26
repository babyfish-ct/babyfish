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
package org.babyfish.springframework.orm.hibernate;

import javax.transaction.TransactionManager;

import org.babyfish.hibernate.XSession;
import org.babyfish.hibernate.context.spi.CurrentXSessionContext;
import org.babyfish.hibernate.internal.XSessionFactoryImplementor;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.springframework.core.Ordered;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Tao Chen
 */
public class SpringXSessionContext implements CurrentXSessionContext {

    private static final long serialVersionUID = -441233267210779294L;

    private final SessionFactoryImplementor sessionFactory;

    private final CurrentXSessionContext jtaSessionContext;


    public SpringXSessionContext(XSessionFactoryImplementor sessionFactory) {
        this.sessionFactory = sessionFactory;
        JtaPlatform jtaPlatform = sessionFactory.getServiceRegistry().getService(JtaPlatform.class);
        TransactionManager transactionManager = jtaPlatform.retrieveTransactionManager();
        this.jtaSessionContext = (transactionManager != null ? new SpringJtaXSessionContext(sessionFactory) : null);
    }


    /**
     * Retrieve the Spring-managed Session for the current thread, if any.
     */
    @SuppressWarnings("deprecation")
    public XSession currentSession() throws HibernateException {
        Object value = TransactionSynchronizationManager.getResource(this.sessionFactory);
        if (value instanceof XSession) {
            return (XSession)value;
        }
        else if (value instanceof SessionHolder) {
            SessionHolder sessionHolder = (SessionHolder) value;
            XSession session = (XSession)sessionHolder.getSession();
            if (TransactionSynchronizationManager.isSynchronizationActive() &&
                    !sessionHolder.isSynchronizedWithTransaction()) {
                TransactionSynchronizationManager.registerSynchronization(
                        new SpringSessionSynchronization(sessionHolder, this.sessionFactory));
                sessionHolder.setSynchronizedWithTransaction(true);
                // Switch to FlushMode.AUTO, as we have to assume a thread-bound Session
                // with FlushMode.MANUAL, which needs to allow flushing within the transaction.
                FlushMode flushMode = session.getFlushMode();
                if (FlushMode.isManualFlushMode(flushMode) &&
                        !TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                    session.setFlushMode(FlushMode.AUTO);
                    sessionHolder.setPreviousFlushMode(flushMode);
                }
            }
            return session;
        }
        else if (this.jtaSessionContext != null) {
            XSession session = this.jtaSessionContext.currentSession();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new SpringFlushSynchronization(session));
            }
            return session;
        }
        else {
            throw new HibernateException("No Session found for current thread");
        }
    }
    
    private static class SpringSessionSynchronization implements TransactionSynchronization, Ordered {

        private final SessionHolder sessionHolder;

        private final SessionFactory sessionFactory;

        private boolean holderActive = true;


        public SpringSessionSynchronization(SessionHolder sessionHolder, SessionFactory sessionFactory) {
            this.sessionHolder = sessionHolder;
            this.sessionFactory = sessionFactory;
        }

        private Session getCurrentSession() {
            return this.sessionHolder.getSession();
        }


        public int getOrder() {
            return SessionFactoryUtils.SESSION_SYNCHRONIZATION_ORDER;
        }

        public void suspend() {
            if (this.holderActive) {
                TransactionSynchronizationManager.unbindResource(this.sessionFactory);
                // Eagerly disconnect the Session here, to make release mode "on_close" work on JBoss.
                getCurrentSession().disconnect();
            }
        }

        public void resume() {
            if (this.holderActive) {
                TransactionSynchronizationManager.bindResource(this.sessionFactory, this.sessionHolder);
            }
        }

        public void flush() {
            try {
                //TODO: SessionFactoryUtils.logger.debug("Flushing Hibernate Session on explicit request");
                getCurrentSession().flush();
            }
            catch (HibernateException ex) {
                throw SessionFactoryUtils.convertHibernateAccessException(ex);
            }
        }

        @SuppressWarnings("deprecation")
        public void beforeCommit(boolean readOnly) throws DataAccessException {
            if (!readOnly) {
                Session session = getCurrentSession();
                // Read-write transaction -> flush the Hibernate Session.
                // Further check: only flush when not FlushMode.MANUAL.
                if (!FlushMode.isManualFlushMode(session.getFlushMode())) {
                    try {
                        //TODO: SessionFactoryUtils.logger.debug("Flushing Hibernate Session on transaction synchronization");
                        session.flush();
                    }
                    catch (HibernateException ex) {
                        throw SessionFactoryUtils.convertHibernateAccessException(ex);
                    }
                }
            }
        }

        public void beforeCompletion() {
            Session session = this.sessionHolder.getSession();
            if (this.sessionHolder.getPreviousFlushMode() != null) {
                // In case of pre-bound Session, restore previous flush mode.
                session.setFlushMode(this.sessionHolder.getPreviousFlushMode());
            }
            // Eagerly disconnect the Session here, to make release mode "on_close" work nicely.
            session.disconnect();
        }

        public void afterCommit() {
        }

        public void afterCompletion(int status) {
            try {
                if (status != STATUS_COMMITTED) {
                    // Clear all pending inserts/updates/deletes in the Session.
                    // Necessary for pre-bound Sessions, to avoid inconsistent state.
                    this.sessionHolder.getSession().clear();
                }
            }
            finally {
                this.sessionHolder.setSynchronizedWithTransaction(false);
            }
        }
    }
    
    private static class SpringFlushSynchronization extends TransactionSynchronizationAdapter {

        private final Session session;


        public SpringFlushSynchronization(Session session) {
            this.session = session;
        }


        @Override
        public void flush() {
            try {
                //TODO: SessionFactoryUtils.logger.debug("Flushing Hibernate Session on explicit request");
                this.session.flush();
            }
            catch (HibernateException ex) {
                throw SessionFactoryUtils.convertHibernateAccessException(ex);
            }
        }


        @Override
        public boolean equals(Object obj) {
            return (obj instanceof SpringFlushSynchronization &&
                    this.session == ((SpringFlushSynchronization) obj).session);
        }

        @Override
        public int hashCode() {
            return this.session.hashCode();
        }

    }
}
