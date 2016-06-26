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
package org.babyfish.model.instrument.metadata;

import java.util.Collection;

import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.metadata.PropertyType;

/**
 * @author Tao Chen
 */
public interface MetadataProperty {

    MetadataClass getDeclaringClass();
    
    String getName();
    
    int getId();
    
    PropertyType getPropertyType();
    
    AssociationType getAssociationType();
    
    boolean isDeferrable();
    
    boolean isMandatory();
    
    boolean isAbsolute();
    
    String getSignature();
    
    String getDescriptor();
    
    String getKeyDescriptor();
    
    String getTargetDescriptor();
    
    Class<?> getSimpleType();
    
    Class<?> getStandardCollectionType();
    
    String getKeyTypeName();
    
    MetadataClass getKeyClass();
    
    String getTargetTypeName();
    
    MetadataClass getTargetClass();
    
    MetadataProperty getIndexProperty();
    
    MetadataProperty getKeyProperty();
    
    MetadataProperty getReferenceProperty();
    
    MetadataProperty getConvarianceProperty();
    
    MetadataProperty getOppositeProperty();
    
    Collection<MetadataComparatorPart> getComparatorParts();
}
