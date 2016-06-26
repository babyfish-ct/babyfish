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
import javax.persistence.metamodel.PluralAttribute;

import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public class PluralAttributePath<X> extends AbstractPath<X> {

    private static final long serialVersionUID = 2201995160130219298L;
    
    private PluralAttribute<?, X, ?> attribute;

    protected PluralAttributePath(
            AbstractPath<?> parentPath,
            PluralAttribute<?, X, ?> attribute) {
        super(Arguments.mustNotBeNull("parentPath", parentPath).getCriteriaBuilder(), parentPath);
        this.attribute = attribute;
    }
    
    public PluralAttribute<?, X, ?> getAttribute() {
        return this.attribute;
    }

    @Override
    protected final boolean isReferenceable() {
        return false;
    }

    @Override
    protected final Attribute<? super X, ?> onLocateAttribute(String attributeName) {
        throw new IllegalArgumentException("Plural attribute paths cannot be further dereferenced");
    }

    @Override
    public Class<? extends X> getJavaType() {
        return this.attribute.getJavaType();
    }
    
    @Override
    public Bindable<X> getModel() {
        return null;
    }
    
    @Override
    public String toString() {
        return this.getParentPath().toString() +
                '.' +
                this.attribute.getName();
    }

}
