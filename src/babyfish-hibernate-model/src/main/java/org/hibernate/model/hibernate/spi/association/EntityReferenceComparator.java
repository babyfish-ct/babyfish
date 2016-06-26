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
package org.hibernate.model.hibernate.spi.association;

import java.io.Serializable;

import org.babyfish.lang.ReferenceComparator;
import org.babyfish.lang.Singleton;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

/**
 * @author Tao Chen
 */
public class EntityReferenceComparator<T> extends Singleton implements ReferenceComparator<T>, Serializable {

    private static final long serialVersionUID = 4051004837047556133L;
    
    private static final EntityReferenceComparator<?> INSTANCE =
        getInstance(EntityReferenceComparator.class);
    
    private EntityReferenceComparator() {
        
    }
    
    @SuppressWarnings("unchecked")
    public static <T> EntityReferenceComparator<T> getInstance() {
        return (EntityReferenceComparator<T>)INSTANCE;
    }

    @Override
    public boolean same(T obj1, T obj2) {
        return getImplementation(obj1) == getImplementation(obj2);
    }
    
    private static Object getImplementation(Object proxy) {
        if (proxy instanceof HibernateProxy) {
            LazyInitializer hibernateLazyInitializer = 
                ((HibernateProxy)proxy).getHibernateLazyInitializer();
            if (!hibernateLazyInitializer.isUninitialized()) {
                Object implementation = 
                    hibernateLazyInitializer
                    .getImplementation();
                return getImplementation(implementation);
            }
        }
        return proxy;
    }
    
}
