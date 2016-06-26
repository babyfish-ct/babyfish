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
package org.babyfish.hibernate.collection.type;

import java.util.Iterator;
import java.util.Map;

import org.babyfish.collection.MAMap;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

/**
 * @author Tao Chen
 */
public abstract class AbstractMAMapType extends AbstractMACollectionType {

    @Override
    public abstract MAMap<?, ?> instantiate(int anticipatedSize);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object onReplaceElements(Object original, Object target,
            CollectionPersister persister, Object owner, Map copyCache,
            SessionImplementor session) throws HibernateException {
        
        // Must before result.clear()
        Iterator itr = this.getClonedIterator(((java.util.Map)original).entrySet());
        
        /*
         *  TODO:
         *  This code is copy from hibernate's CollectionType class.
         *  In its source code, hibernate's author have wrote another 
         *  _TODO_ comment "does not work for EntityMode.DOM4J yet!"
         *  So here, I must check the source code of newest version 
         *  hibernate to make sure whether these code should be changed. 
         */
        java.util.Map result = (java.util.Map)target;
        result.clear();
        
        while ( itr.hasNext() ) {
            java.util.Map.Entry me = (java.util.Map.Entry) itr.next();
            Object key = persister.getIndexType().replace( me.getKey(), null, session, owner, copyCache );
            Object value = persister.getElementType().replace( me.getValue(), null, session, owner, copyCache );
            result.put(key, value);
        }
        
        return result;
    }
}
