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

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;

import org.babyfish.collection.BidiType;
import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.hibernate.collection.PersistentMANavigableMap;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

/**
 * @author Tao Chen
 */
public class MANavigableMapType extends AbstractMAMapType {

    @Override
    public MANavigableMap<?, ?> instantiate(int anticipatedSize) {
        Comparator<? super Object> keyComparator = 
                (Comparator<? super Object>)
                this
                .getModelProperty()
                .getKeyUnifiedComparator()
                .comparator();
        UnifiedComparator<Object> valueUnifiedComparator = 
                this
                .getModelProperty()
                .getCollectionUnifiedComparator();
        return new MATreeMap<Object, Object>(
                BidiType.NONNULL_VALUES,
                ReplacementRule.NEW_REFERENCE_WIN,
                keyComparator,
                valueUnifiedComparator);
    }

    @Override
    public final PersistentCollection instantiate(
            SessionImplementor session, CollectionPersister persister) throws HibernateException {
        return new PersistentMANavigableMap<Object, Object>(this.getRole(), session, null);
    }

    @Deprecated
    @Override
    public final PersistentCollection wrap(SessionImplementor session, Object collection) {
        return this.wrap(session, (MANavigableMap<?, ?>)collection);
    }
    
    @SuppressWarnings("unchecked")
    public PersistentCollection wrap(SessionImplementor session, MANavigableMap<?, ?> map) {
        return new PersistentMANavigableMap<Object, Object>(this.getRole(), session, (MANavigableMap<Object, Object>)map);
    }

    @Override
    public Iterator<?> onGetElementsIterator(Object collection) {
        return this.getClonedIterator(((MANavigableMap<?, ?>)collection).values());
    }

    
    @Override
    public boolean onContains(Object collection, Object entity) {
        return ((MANavigableMap<?, ?>)collection).containsValue(entity);
    }

    @Override
    public Object onIndexOf(Object collection, Object entity) {
        for (Entry<?, ?> entry : ((MANavigableMap<?, ?>)collection).entrySet()) {
            if (entry.getValue() == entity) {
                return entry.getKey();
            }
        }
        return null;
    }
}
