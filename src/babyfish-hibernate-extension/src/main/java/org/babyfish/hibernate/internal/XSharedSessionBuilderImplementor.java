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

import java.sql.Connection;

import org.babyfish.hibernate.XSharedSessionBuilder;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.Interceptor;
import org.hibernate.engine.spi.SessionBuilderImplementor;

/**
 * @author Tao Chen
 */
public interface XSharedSessionBuilderImplementor extends SessionBuilderImplementor, XSharedSessionBuilder {

    @Override
    XSharedSessionBuilderImplementor interceptor();

    @Override
    XSharedSessionBuilderImplementor connection();

    @Override
    XSharedSessionBuilderImplementor connectionReleaseMode();

    @Override
    XSharedSessionBuilderImplementor autoJoinTransactions();

    @Override
    XSharedSessionBuilderImplementor autoClose();

    @Override
    XSharedSessionBuilderImplementor flushBeforeCompletion();

    @Override
    XSharedSessionBuilderImplementor transactionContext();

    @Override
    XSharedSessionBuilderImplementor interceptor(Interceptor interceptor);

    @Override
    XSharedSessionBuilderImplementor noInterceptor();

    @Override
    XSharedSessionBuilderImplementor connection(Connection connection);

    @Override
    XSharedSessionBuilderImplementor connectionReleaseMode(ConnectionReleaseMode connectionReleaseMode);

    @Override
    XSharedSessionBuilderImplementor autoJoinTransactions(boolean autoJoinTransactions);

    @Override
    XSharedSessionBuilderImplementor autoClose(boolean autoClose);

    @Override
    XSharedSessionBuilderImplementor flushBeforeCompletion(boolean flushBeforeCompletion);
    
    @Override
    XSharedSessionBuilderImplementor tenantIdentifier(String tenantIdentifier);
    
}
