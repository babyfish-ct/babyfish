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
package org.babyfish.hibernate.persister;

import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Tao Chen
 */
public class StandardPersisterClassResolver extends org.hibernate.persister.internal.StandardPersisterClassResolver {

    private static final long serialVersionUID = 3665499181700048132L;

    @Override
    public Class<? extends EntityPersister> getEntityPersisterClass(EntityBinding metadata) {
        return replace(super.getEntityPersisterClass(metadata));
    }

    @Override
    public Class<? extends EntityPersister> getEntityPersisterClass(PersistentClass metadata) {
        
        return replace(super.getEntityPersisterClass(metadata));
    }

    private static Class<? extends EntityPersister> replace(Class<? extends EntityPersister> persisterClass)  {
        if (persisterClass == org.hibernate.persister.entity.SingleTableEntityPersister.class) {
            return SingleTableEntityPersister.class;
        }
        if (persisterClass == org.hibernate.persister.entity.UnionSubclassEntityPersister.class) {
            return UnionSubclassEntityPersister.class;
        }
        if (persisterClass == org.hibernate.persister.entity.JoinedSubclassEntityPersister.class) {
            return JoinedSubclassEntityPersister.class;
        }
        return persisterClass;
    }
}
