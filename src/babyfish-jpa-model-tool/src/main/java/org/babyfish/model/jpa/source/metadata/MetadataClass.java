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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.model.jpa.source.ElementHelper;

/**
 * @author Tao Chen
 */
public class MetadataClass {

    private boolean entity;
    
    private String name;
    
    private String simpleName;
    
    private String packageName;
    
    private XOrderedMap<String, MetadataProperty> properties;
    
    private XOrderedMap<String, MetadataAssociation> associations;
    
    private XOrderedMap<String, MetadataScalar> scalars;
    
    private Object supperMetadataClass;
    
    private AccessType accessType;
    
    public MetadataClass(TypeElement typeElement) {
        this.entity = ElementHelper.containsAnyAnnotation(typeElement, Entity.class);
        this.name = typeElement.getQualifiedName().toString();
        this.simpleName = typeElement.getSimpleName().toString();
        for (Element enclosing = typeElement.getEnclosingElement();
                ;
                enclosing = enclosing.getEnclosingElement()) {
            if (enclosing.getKind() == ElementKind.PACKAGE) {
                this.packageName = ((PackageElement)enclosing).getQualifiedName().toString();
                break;
            }
        }
        this.accessType = getDefaultAccessType(typeElement);
        
        XOrderedMap<String, MetadataProperty> properties = new LinkedHashMap<>();
        XOrderedMap<String, MetadataAssociation> associations = new LinkedHashMap<>();
        XOrderedMap<String, MetadataScalar> scalars = new LinkedHashMap<>();
        for (Element element : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            MetadataProperty property = MetadataProperty.of(this, element);
            if (property instanceof MetadataScalar) {
                MetadataScalar scalar = (MetadataScalar)property;
                properties.put(scalar.getName(), scalar);
                scalars.put(scalar.getName(), scalar);
            } else if (property instanceof MetadataAssociation) {
                MetadataAssociation association = (MetadataAssociation)property;
                properties.put(association.getName(), association);
                associations.put(association.getName(), association);
            }
        }
        for (Element element : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            MetadataProperty property = MetadataProperty.of(this, element);
            if (property instanceof MetadataScalar) {
                MetadataScalar scalar = (MetadataScalar)property;
                properties.put(scalar.getName(), scalar);
                scalars.put(scalar.getName(), scalar);
            } else if (property instanceof MetadataAssociation) {
                MetadataAssociation association = (MetadataAssociation)property;
                properties.put(association.getName(), association);
                associations.put(association.getName(), association);
            }
        }
        this.properties = MACollections.unmodifiable(properties);
        this.associations = MACollections.unmodifiable(associations);
        this.scalars = MACollections.unmodifiable(scalars);
        this.supperMetadataClass = typeElement.getSuperclass();
    }
    
    public boolean isEntity() {
        return this.entity;
    }

    public String getName() {
        return this.name;
    }
    
    public String getSimpleName() {
        return this.simpleName;
    }
    
    public String getPackageName() {
        return this.packageName;
    }
    
    public AccessType getAccessType() {
        return this.accessType;
    }
    
    public MetadataClass getSuperMetadataClass() {
        return (MetadataClass)this.supperMetadataClass;
    }
    
    public Map<String, MetadataProperty> getProperties() {
        return this.properties;
    }
    
    public Map<String, MetadataAssociation> getAssociations() {
        return this.associations;
    }
    
    public Map<String, MetadataScalar> getScalars() {
        return this.scalars;
    }
    
    public void secondaryPass(Map<String, MetadataClass> metadataClasses) {
        TypeMirror superType = (TypeMirror)this.supperMetadataClass;
        this.supperMetadataClass = null;
        while (superType instanceof DeclaredType) {
            MetadataClass superMetadataClass = metadataClasses.get(superType.toString());
            if (superMetadataClass != null) {
                this.supperMetadataClass = superMetadataClass;
                break;
            }
            superType = ((TypeElement)((DeclaredType)superType).asElement()).getSuperclass();
        }
        for (MetadataAssociation association : this.associations.values()) {
            association.secondaryPass(metadataClasses);
        }
        for (MetadataScalar scalar : this.scalars.values()) {
            scalar.secondaryPass(metadataClasses);
        }
    }
    
    private AccessType getDefaultAccessType(TypeElement typeElement) {
        AccessType accessType = ElementHelper.getAnnotationEnumValue(typeElement, Access.class, "value", AccessType.class);
        if (accessType != null) {
            return accessType;
        }
        for (Element element : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (ElementHelper.containsAnyAnnotation(element, Id.class)) {
                return AccessType.FIELD;
            }
        }
        for (Element element : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (ElementHelper.containsAnyAnnotation(element, Id.class)) {
                return AccessType.FIELD;
            }
        }
        TypeMirror superTypeMirror = typeElement.getSuperclass();
        if (superTypeMirror instanceof DeclaredType) {
            TypeElement superElement = ((TypeElement)((DeclaredType)superTypeMirror).asElement());
            if (!superElement.getQualifiedName().equals(Object.class.getName())) {
                return getDefaultAccessType(superElement);
            }
        }
        return AccessType.PROPERTY;
    }
}
