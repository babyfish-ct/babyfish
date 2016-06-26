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

import org.babyfish.hibernate.XSession;
import org.babyfish.hibernate.context.spi.CurrentXSessionContext;
import org.babyfish.hibernate.internal.XSessionFactoryImplementor;
import org.hibernate.HibernateException;
import org.springframework.orm.hibernate4.SpringJtaSessionContext;

/**
 * @author Tao Chen
 */
public class SpringJtaXSessionContext extends SpringJtaSessionContext implements CurrentXSessionContext {

    private static final long serialVersionUID = -4797202677452796591L;

    public SpringJtaXSessionContext(XSessionFactoryImplementor factory) {
        super(factory);
    }

    @Override
    public XSession currentSession() throws HibernateException {
        return (XSession)super.currentSession();
    }
}
