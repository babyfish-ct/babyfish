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
package org.babyfish.model.instrument.spi;

import org.babyfish.model.instrument.metadata.MetadataClass;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.metadata.PropertyType;

/**
 * @author Tao Chen
 */
public class Identifiers {
    
    public static final String MODEL_CLASS_FIELD_NAME = "{MODEL_CLASS}";
    
    public static final String DEFERRABLE_SCALAR_PROPERTY_IDS = "{DEFERRABLE_SCALAR_PROPERTY_IDS}";
    
    public static final String OBJECT_MODEL_CONTRACT_SIMPLE_NAME = "{ObjectModel}";
    
    public static final String OBJECT_MODEL_TARGET_SIMPLE_NAME = "{ObjectModelTarget}";
    
    public static final String OBJECT_MODEL_PROXY_SIMPLE_NAME = "{ObjectModelProxy}";
    
    public static final String OBJECT_MODEL_FIELD_NAME = "{om}";
    
    public static final String CREATE_OBJECT_MODEL_METHOD_NAME = "{createObjectModel}";
    
    public static final String INIT_ASSOCIATED_ENDPOINTS_METHOD_NAME = "{initAssociatedEndpoints}";
    
    private static final char PREFIX = '{';
    
    private static final String PROPERTY_REFERENCE_POSTFIX = ":Reference}";
    
    private static final String PROPERTY_SCALAR_STATE_POSTFIX = ":ScalarSate}";
    
    private static final String PROPERTY_FROZEN_CONTEXT_POSTFIX = ":FrozenContext}";
    
    public static String fieldName(MetadataProperty metadataProperty) {
        switch (metadataProperty.getPropertyType()) {
        case CONTRAVARIANCE:
        case INDEX:
        case KEY:
            return null;
        case ASSOCIATION:
            if (metadataProperty.getStandardCollectionType() == null) {
                return PREFIX + metadataProperty.getName() + PROPERTY_REFERENCE_POSTFIX;
            }
        default:
            return metadataProperty.getName();
        }
    }
    
    public static String stateFieldName(MetadataProperty metadataProperty) {
        if (metadataProperty.getPropertyType() == PropertyType.SCALAR && (
                metadataProperty.isDeferrable() || !metadataProperty.isMandatory())) {
            return PREFIX + metadataProperty.getName() + PROPERTY_SCALAR_STATE_POSTFIX;
        }
        return null;
    }
    
    public static String frozenContextFieldName(MetadataProperty metadataProperty) {
        if (metadataProperty.getPropertyType() == PropertyType.SCALAR && 
                metadataProperty.getDescriptor().charAt(0) != '[') {
            return PREFIX + metadataProperty.getName() + PROPERTY_FROZEN_CONTEXT_POSTFIX;
        }
        return null;
    }
    
    public static String getterName(MetadataProperty metadataProperty) {
        String name = metadataProperty.getName();
        return 
                (metadataProperty.getDescriptor().equals("Z") ? "is" : "get") + 
                Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
    
    public static String setterName(MetadataProperty metadataProperty) {
        String name = metadataProperty.getName();
        return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
    
    public static String embeddedComparatorStaticFieldName(MetadataClass embeddedMetadataClass) {
        return "{EMBEDED_COMPARATOR_" + embeddedMetadataClass.getName().replace(".", "::") + "}";
    }
    
    public static String embeddedComparatorStaticFieldName(MetadataProperty metadataProperty) {
        if (metadataProperty.getPropertyType() == PropertyType.SCALAR && metadataProperty.getTargetClass() != null) {
            return embeddedComparatorStaticFieldName(metadataProperty.getTargetClass());
        }
        return null;
    }
    
    public static String embeddedEqualityComparatorStaticFieldName(MetadataClass embeddedMetadataClass) {
        return "{EMBEDED_EQUALITY_COMPARATOR_" + embeddedMetadataClass.getName().replace(".", "::") + "}";
    }
    
    public static String embeddedEqualityComparatorStaticFieldName(MetadataProperty metadataProperty) {
        if (metadataProperty.getPropertyType() == PropertyType.SCALAR && metadataProperty.getTargetClass() != null) {
            return embeddedEqualityComparatorStaticFieldName(metadataProperty.getTargetClass());
        }
        return null;
    }
    
    public static String embeddedScalarListenerImplSimpleName(MetadataProperty metadataProperty) {
        if (metadataProperty.getPropertyType() == PropertyType.SCALAR &&
                metadataProperty.getTargetClass() != null) {
            return "{EmbededScalarListenerImpl_" + metadataProperty.getName() + "}";
        }
        return null;
    }
    
    private Identifiers() {}
}
