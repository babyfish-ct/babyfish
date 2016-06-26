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
package org.hibernate.model.hibernate.metadata.internal;

import org.babyfish.model.ModelType;
import org.babyfish.model.jpa.metadata.internal.JPAModelClassImpl;
import org.babyfish.model.jpa.metadata.internal.JPAModelPropertyImpl;
import org.babyfish.model.metadata.ComparatorPart;
import org.hibernate.model.hibernate.spi.association.EntityReferenceComparator;

public class HibernateModelClassImpl extends JPAModelClassImpl {

    public HibernateModelClassImpl(
            ModelType type, 
            Class<?> javaType, 
            Class<?> superJavaType,
            JPAModelPropertyImpl[] declaredProperties, 
            ComparatorPart[] embeddableComparatorParts) {
        super(type, javaType, superJavaType, declaredProperties, embeddableComparatorParts);
    }

    @Override
    protected DefaultHibernateEntityEqualityComparator createDefaultEntityEqualityComparator() {
        return new DefaultHibernateEntityEqualityComparator(this);
    }

    private static class DefaultHibernateEntityEqualityComparator extends DefaultEntityEqualityComparator {

        public DefaultHibernateEntityEqualityComparator(HibernateModelClassImpl modelClass) {
            super(modelClass);
        }

        @Override
        protected boolean isSameEntity(Object a, Object b) {
            return EntityReferenceComparator.getInstance().same(a, b);
        }
    }
}
