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
package org.babyfish.hibernate.hql;

import java.util.Map;

import org.babyfish.model.jpa.path.spi.PathPlanKey;
import org.hibernate.Filter;
import org.hibernate.engine.query.spi.EntityGraphQueryHint;
import org.hibernate.engine.spi.SessionFactoryImplementor;

/**
 * @author Tao Chen
 */
public interface XQueryTranslatorFactory {

    XQueryTranslator createQueryTranslator(
            String queryIdentifier, 
            String queryString, 
            PathPlanKey pathPlanKey,
            Map<String, Filter> filters, 
            SessionFactoryImplementor factory,
            EntityGraphQueryHint entityGraphQueryHint);

    XFilterTranslator createFilterTranslator(
            String queryIdentifier, 
            String queryString,
            PathPlanKey pathPlanKey,
            Map<String, Filter> filters, 
            SessionFactoryImplementor factory);
    
}
