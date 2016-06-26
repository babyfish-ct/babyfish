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

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.BidiType;
import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MAList;
import org.babyfish.hibernate.collection.PersistentMAList;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

/**
 * @author Tao Chen
 */
public class MAListType extends AbstractMACollectionType {

    @Override
    public MAList<?> instantiate(int anticipatedSize) {
        EqualityComparator<? super Object> equalityComparator = 
            (EqualityComparator<? super Object>)
            this
            .getModelProperty()
            .getCollectionUnifiedComparator()
            .equalityComparator();
        if (anticipatedSize > 0) {
            return new MAArrayList<Object>(BidiType.NONNULL_VALUES, equalityComparator, anticipatedSize);
        }
        return new MAArrayList<Object>(BidiType.NONNULL_VALUES, equalityComparator);
    }

    @Override
    public final PersistentCollection instantiate(
            SessionImplementor session, CollectionPersister persister) throws HibernateException {
        return new PersistentMAList<Object>(this.getRole(), session, null);
    }

    @Deprecated
    @Override
    public final PersistentCollection wrap(SessionImplementor session, Object collection) {
        return this.wrap(session, (MAList<?>)collection);
    }
    
    @SuppressWarnings("unchecked")
    public PersistentCollection wrap(SessionImplementor session, MAList<?> list) {
        return new PersistentMAList<Object>(this.getRole(), session, (MAList<Object>)list);
    }
    
    @Override
    public Object onIndexOf(Object collection, Object entity) {
        MAList<?> list = (MAList<?>)collection;
        int index = 0;
        for (Object o : list) {
            if (o == entity) {
                return index;
            }
            index++;
        }
        return null;
    }
}
