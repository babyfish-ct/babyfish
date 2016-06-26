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
package org.babyfish.model.jpa.metadata.internal;

import org.babyfish.model.jpa.metadata.JPAModelClass;
import org.babyfish.model.jpa.metadata.JPAModelProperty;
import org.babyfish.model.jpa.metadata.JPAScalarType;
import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.model.metadata.internal.ModelPropertyImpl;

public class JPAModelPropertyImpl extends ModelPropertyImpl implements JPAModelProperty {
    
    private JPAScalarType scalarType;
    
    private boolean inverse;

    public JPAModelPropertyImpl(
            int id, 
            String name, 
            PropertyType propertyType, 
            AssociationType associationType,
            boolean deferrable,
            boolean mandatory,
            Class<?> type, 
            Class<?> standardCollectionType, 
            Class<?> keyType, 
            Class<?> targetType,
            Dependency dependency,
            JPAScalarType scalarType,
            boolean inverse) {
        super(
                id, 
                name, 
                propertyType, 
                associationType, 
                deferrable, 
                mandatory, 
                type, 
                standardCollectionType, 
                keyType, 
                targetType, 
                dependency
        );
        this.scalarType = scalarType;
        this.inverse = inverse;
    }

    @Override
    public JPAScalarType getScalarType() {
        return this.scalarType;
    }

    @Override
    public boolean isInverse() {
        return this.inverse;
    }

    @Override
    public JPAModelClass getDeclaringlClass() {
        return (JPAModelClass)super.getDeclaringlClass();
    }

    @Override
    public JPAModelClass getKeyClass() {
        return (JPAModelClass)super.getKeyClass();
    }

    @Override
    public JPAModelClass getTargetClass() {
        return (JPAModelClass)super.getTargetClass();
    }

    @Override
    public JPAModelProperty getIndexProperty() {
        return (JPAModelProperty)super.getIndexProperty();
    }

    @Override
    public JPAModelProperty getKeyProperty() {
        return (JPAModelProperty)super.getKeyProperty();
    }

    @Override
    public JPAModelProperty getReferenceProperty() {
        return (JPAModelProperty)super.getReferenceProperty();
    }

    @Override
    public JPAModelProperty getConvarianceProperty() {
        return (JPAModelProperty)super.getConvarianceProperty();
    }

    @Override
    public JPAModelProperty getOppositeProperty() {
        return (JPAModelProperty)super.getOppositeProperty();
    }
}
