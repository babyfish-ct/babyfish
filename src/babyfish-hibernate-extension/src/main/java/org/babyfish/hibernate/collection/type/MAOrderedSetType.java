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
import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.hibernate.collection.PersistentMAOrderedSet;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

/**
 * @author Tao Chen
 */
public class MAOrderedSetType extends AbstractMACollectionType {
    
    public MAOrderedSet<?> instantiate(int anticipatedSize) {
        EqualityComparator<? super Object> equalityComparator =
                (EqualityComparator<? super Object>)
                this
                .getModelProperty()
                .getCollectionUnifiedComparator()
                .equalityComparator(true);
        return new MALinkedHashSet<Object>(
                ReplacementRule.NEW_REFERENCE_WIN,
                equalityComparator,
                anticipatedSize,
                .75F,
                false,
                OrderAdjustMode.NONE,
                OrderAdjustMode.NONE);
    }

    @Override
    public final PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister) throws HibernateException {
        return new PersistentMAOrderedSet<Object>(this.getRole(), session, null);
    }

    @Deprecated
    @Override
    public final PersistentCollection wrap(SessionImplementor session, Object collection) {
        return this.wrap(session, (MAOrderedSet<?>)collection);
    }
    
    @SuppressWarnings("unchecked")
    public PersistentCollection wrap(SessionImplementor session, MAOrderedSet<?> collection) {
        return new PersistentMAOrderedSet<Object>(this.getRole(), session, (MAOrderedSet<Object>)collection);
    }
}
