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
package org.babyfish.model.jpa.source.metadata;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.persistence.Basic;
import javax.persistence.Embedded;
import javax.persistence.FetchType;

import org.babyfish.model.jpa.source.ElementHelper;

/**
 * @author Tao Chen
 */
public class MetadataScalar extends MetadataProperty {

    private boolean embedded;
    
    private boolean lazy;
    
    private Object relatedMetadataClass;
    
    MetadataScalar(MetadataClass owner, Element element) {
        super(owner, element);
        this.embedded = ElementHelper.containsAnyAnnotation(element, Embedded.class);
        this.lazy = FetchType.LAZY.name().equals(ElementHelper.getAnnotationValue(element, Basic.class, "fetch"));
        if (this.embedded) {
            if (element instanceof ExecutableElement) {
                this.relatedMetadataClass = ((DeclaredType)((ExecutableElement)element).getReturnType()).toString();
            } else {
                this.relatedMetadataClass = ((DeclaredType)(element).asType()).toString();
            }
        }
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public boolean isLazy() {
        return lazy;
    }
    
    public MetadataClass getRelatedMetadataClass() {
        return (MetadataClass)relatedMetadataClass;
    }

    void secondaryPass(Map<String, MetadataClass> metaClasses) {
        String embeddedName = (String)this.relatedMetadataClass;
        if (embeddedName != null) {
            this.relatedMetadataClass = metaClasses.get(embeddedName);
            if (this.relatedMetadataClass == null) {
                throw new IllegalArgumentException(
                        "Failed to resolve the embedded property " +
                        this.owner.getName() +
                        '.' +
                        this.name +
                        ", because there is no opposite emembeddable type: " +
                        embeddedName);
            }
        }
    }
}
