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
package org.babyfish.persistence.criteria.spi;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;

import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public class SingularAttributePath<X> extends AbstractPath<X> {

    private static final long serialVersionUID = 436933653541939703L;
    
    private SingularAttribute<?, X> attribute;
    
    private ManagedType<X> managedType;

    public SingularAttributePath(
            AbstractPath<?> parentPath,
            SingularAttribute<?, X> attribute) {
        super(Arguments.mustNotBeNull("parentPath", parentPath).getCriteriaBuilder(), parentPath);
        this.attribute = attribute;
        if ( Attribute.PersistentAttributeType.BASIC != attribute.getPersistentAttributeType() ) {
            if ( Attribute.PersistentAttributeType.EMBEDDED == attribute.getPersistentAttributeType() ) {
                this.managedType = (EmbeddableType<X>)attribute.getType();
            }
            else {
                this.managedType = (IdentifiableType<X>)attribute.getType();
            }
        }
    }
    
    public SingularAttribute<?, X> getAttribute() {
        return this.attribute;
    }

    @Override
    protected boolean isReferenceable() {
        return this.managedType != null;
    }

    @Override
    protected Attribute<? super X, ?> onLocateAttribute(String attributeName) {
        return this.managedType.getAttribute(attributeName);
    }

    @Override
    public Class<? extends X> getJavaType() {
        return this.attribute.getJavaType();
    }
    
    @Override
    public Bindable<X> getModel() {
        return this.getAttribute();
    }
    
    @Override
    public String toString() {
        return 
                this.getParentPath().toString() + 
                '.' + 
                this.attribute.getName();
    }
}
