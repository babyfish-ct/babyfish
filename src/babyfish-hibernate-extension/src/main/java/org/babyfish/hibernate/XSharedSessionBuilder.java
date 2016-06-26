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
package org.babyfish.hibernate;

import java.sql.Connection;

import org.hibernate.ConnectionReleaseMode;
import org.hibernate.Interceptor;
import org.hibernate.SharedSessionBuilder;

/**
 * @author Tao Chen
 */
public interface XSharedSessionBuilder extends XSessionBuilder, SharedSessionBuilder {

    @Override
    XSharedSessionBuilder interceptor();

    @Override
    XSharedSessionBuilder connection();

    @Override
    XSharedSessionBuilder connectionReleaseMode();

    @Override
    XSharedSessionBuilder autoJoinTransactions();

    @Override
    XSharedSessionBuilder autoClose();

    @Override
    XSharedSessionBuilder flushBeforeCompletion();

    @Override
    XSharedSessionBuilder transactionContext();

    @Override
    XSharedSessionBuilder interceptor(Interceptor interceptor);

    @Override
    XSharedSessionBuilder noInterceptor();

    @Override
    XSharedSessionBuilder connection(Connection connection);

    @Override
    XSharedSessionBuilder connectionReleaseMode(ConnectionReleaseMode connectionReleaseMode);

    @Override
    XSharedSessionBuilder autoJoinTransactions(boolean autoJoinTransactions);

    @Override
    XSharedSessionBuilder autoClose(boolean autoClose);

    @Override
    XSharedSessionBuilder flushBeforeCompletion(boolean flushBeforeCompletion);
}
