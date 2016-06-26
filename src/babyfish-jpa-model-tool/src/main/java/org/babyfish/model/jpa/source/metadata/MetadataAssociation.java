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

import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.babyfish.model.jpa.source.ElementHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tao Chen
 */
public class MetadataAssociation extends MetadataProperty {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataAssociation.class);

    private boolean collection;
    
    private Object relatedMetadataClass;
    
    MetadataAssociation(MetadataClass owner, Element element) {
        super(owner, element);
        
        this.collection = ElementHelper.containsAnyAnnotation(element, OneToMany.class, ManyToMany.class);
        
        Class<?> targetEntity = null;
        if (ElementHelper.containsAnyAnnotation(element, OneToOne.class)) {
            targetEntity = (Class<?>)ElementHelper.getAnnotationValue(element, OneToOne.class, "targetEntity");
        } else if (ElementHelper.containsAnyAnnotation(element, ManyToOne.class)) {
            targetEntity = (Class<?>)ElementHelper.getAnnotationValue(element, ManyToOne.class, "targetEntity");
        } else if (ElementHelper.containsAnyAnnotation(element, OneToMany.class)) {
            targetEntity = (Class<?>)ElementHelper.getAnnotationValue(element, OneToMany.class, "targetEntity");
        } else if (ElementHelper.containsAnyAnnotation(element, ManyToMany.class)) {
            targetEntity = (Class<?>)ElementHelper.getAnnotationValue(element, ManyToMany.class, "targetEntity");
        }
        if (targetEntity == null || targetEntity == void.class) {
            DeclaredType declaredType;
            if (element instanceof ExecutableElement) {
                declaredType = (DeclaredType)((ExecutableElement)element).getReturnType();
            } else {
                declaredType = (DeclaredType)(element).asType();
            }
            if (this.collection) {
                List<? extends TypeMirror> typeArguments = 
                        declaredType.getTypeArguments();
                if (typeArguments.isEmpty()) {
                    String message = 
                            "The association \"" +
                            this.name +
                            "\" of \"" +
                            this.getOwner().getName() +
                            "\" is invalid, it has neither targetEntity of \"" +
                            "@" +
                            (ElementHelper.containsAnyAnnotation(element, OneToMany.class) ? 
                            OneToMany.class.getName() :
                            ManyToMany.class.getName()) +
                            "\" nor type arguments.";
                            LOGGER.error(message);
                    throw new IllegalArgumentException(message);
                }
                this.relatedMetadataClass = typeArguments.get(typeArguments.size() - 1).toString();
            } else {
                this.relatedMetadataClass = declaredType.toString();
            }
        } else {
            this.relatedMetadataClass = targetEntity.getName();
        }
    }

    public boolean isCollection() {
        return this.collection;
    }
    
    public MetadataClass getRelatedMetadataClass() {
        return (MetadataClass)this.relatedMetadataClass;
    }
    
    void secondaryPass(Map<String, MetadataClass> metaClasses) {
        String oppositeEndpointName = (String)this.relatedMetadataClass;
        this.relatedMetadataClass = metaClasses.get(oppositeEndpointName);
        if (this.relatedMetadataClass == null) {
            throw new IllegalArgumentException(
                    "Failed to resolve association property " +
                    this.owner.getName() +
                    '.' +
                    this.name +
                    ", because there is no opposite entity: " +
                    oppositeEndpointName);
        }
    }
}
