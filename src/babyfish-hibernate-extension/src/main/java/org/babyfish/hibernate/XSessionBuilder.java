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
import org.hibernate.SessionBuilder;

/**
 * @author Tao Chen
 */
public interface XSessionBuilder extends SessionBuilder {

    @Override
    XSession openSession();

    @Override
    XSessionBuilder interceptor(Interceptor interceptor);

    @Override
    XSessionBuilder noInterceptor();

    @Override
    XSessionBuilder connection(Connection connection);

    @Override
    XSessionBuilder connectionReleaseMode(ConnectionReleaseMode connectionReleaseMode);

    @Override
    XSessionBuilder autoJoinTransactions(boolean autoJoinTransactions);

    @Deprecated
    @Override
    XSessionBuilder autoClose(boolean autoClose);

    @Override
    XSessionBuilder flushBeforeCompletion(boolean flushBeforeCompletion);

    @Override
    XSessionBuilder tenantIdentifier(String tenantIdentifier);
}
