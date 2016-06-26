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

import org.babyfish.hibernate.XSession;
import org.babyfish.hibernate.XSessionBuilder;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.Interceptor;
import org.hibernate.engine.spi.SessionBuilderImplementor;
import org.hibernate.engine.spi.SessionOwner;

/**
 * @author Tao Chen
 */
public interface XSessionBuilderImplementor extends XSessionBuilder, SessionBuilderImplementor {

    public XSessionBuilderImplementor owner(SessionOwner sessionOwner);
    
    @Override
    XSession openSession();

    @Override
    XSessionBuilderImplementor interceptor(Interceptor interceptor);

    @Override
    XSessionBuilderImplementor noInterceptor();

    @Override
    XSessionBuilderImplementor connection(Connection connection);

    @Override
    XSessionBuilderImplementor connectionReleaseMode(ConnectionReleaseMode connectionReleaseMode);

    @Override
    XSessionBuilderImplementor autoJoinTransactions(boolean autoJoinTransactions);

    @Deprecated
    @Override
    XSessionBuilderImplementor autoClose(boolean autoClose);

    @Override
    XSessionBuilderImplementor flushBeforeCompletion(boolean flushBeforeCompletion);

    @Override
    XSessionBuilderImplementor tenantIdentifier(String tenantIdentifier);
}
