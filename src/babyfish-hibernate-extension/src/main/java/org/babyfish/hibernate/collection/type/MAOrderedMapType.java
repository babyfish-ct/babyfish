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
import java.util.Map.Entry;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.BidiType;
import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.hibernate.collection.PersistentMAOrderedMap;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

/**
 * @author Tao Chen
 */
public class MAOrderedMapType extends AbstractMAMapType {

    @Override
    public MAOrderedMap<?, ?> instantiate(int anticipatedSize) {
        EqualityComparator<Object> keyEqualityComparator =
                (EqualityComparator<Object>)
                this
                .getModelProperty()
                .getKeyUnifiedComparator()
                .equalityComparator();
        UnifiedComparator<Object> valueUnifiedComparator =
                this
                .getModelProperty()
                .getCollectionUnifiedComparator();
        return new MALinkedHashMap<Object, Object>(
                BidiType.NONNULL_VALUES,
                ReplacementRule.NEW_REFERENCE_WIN,
                keyEqualityComparator,
                valueUnifiedComparator,
                false,
                OrderAdjustMode.NONE,
                OrderAdjustMode.NONE);
    }

    @Override
    public final PersistentCollection instantiate(
            SessionImplementor session, CollectionPersister persister) throws HibernateException {
        return new PersistentMAOrderedMap<Object, Object>(this.getRole(), session, null);
    }

    @Deprecated
    @Override
    public final PersistentCollection wrap(SessionImplementor session, Object collection) {
        return this.wrap(session, (MAOrderedMap<?, ?>)collection);
    }
    
    @SuppressWarnings("unchecked")
    public PersistentCollection wrap(SessionImplementor session, MAOrderedMap<?, ?> map) {
        return new PersistentMAOrderedMap<Object, Object>(this.getRole(), session, (MAOrderedMap<Object, Object>)map);
    }

    @Override
    public Iterator<?> onGetElementsIterator(Object collection) {
        return this.getClonedIterator(((MAOrderedMap<?, ?>)collection).values());
    }

    
    @Override
    public boolean onContains(Object collection, Object entity) {
        return ((MAOrderedMap<?, ?>)collection).containsValue(entity);
    }

    @Override
    public Object onIndexOf(Object collection, Object entity) {
        for (Entry<?, ?> entry : ((MAOrderedMap<?, ?>)collection).entrySet()) {
            if (entry.getValue() == entity) {
                return entry.getKey();
            }
        }
        return null;
    }
}
