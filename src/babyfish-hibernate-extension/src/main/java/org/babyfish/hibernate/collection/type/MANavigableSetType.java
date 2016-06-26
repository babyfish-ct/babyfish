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

import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.hibernate.collection.PersistentMANavigableSet;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

/**
 * @author Tao Chen
 */
public class MANavigableSetType extends AbstractMACollectionType {
    
    public MANavigableSet<?> instantiate(int anticipatedSize) {
        Comparator<? super Object> comparator = 
            (Comparator<? super Object>)
            this
            .getModelProperty()
            .getCollectionUnifiedComparator()
            .comparator(true);
        return new MATreeSet<Object>(
                ReplacementRule.NEW_REFERENCE_WIN,
                comparator);
    }

    @Override
    public final PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister) throws HibernateException {
        return new PersistentMANavigableSet<Object>(this.getRole(), session, null);
    }
    
    @Deprecated
    @Override
    public final PersistentCollection wrap(SessionImplementor session, Object collection) {
        return this.wrap(session, (MANavigableSet<?>)collection);
    }

    @SuppressWarnings("unchecked")
    public PersistentCollection wrap(SessionImplementor session, MANavigableSet<?> collection) {
        return new PersistentMANavigableSet<Object>(this.getRole(), session, (MANavigableSet<Object>)collection);
    }
}
