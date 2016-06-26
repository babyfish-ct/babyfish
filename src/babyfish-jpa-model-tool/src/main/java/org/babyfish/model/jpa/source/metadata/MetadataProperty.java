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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.babyfish.lang.I18N;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.Strings;
import org.babyfish.model.jpa.source.ElementHelper;
import org.babyfish.model.jpa.source.TypedQueryPathProcessor;

/**
 * @author Tao Chen
 */
public class MetadataProperty {

    protected MetadataClass owner;
    
    protected String name;
    
    static MetadataProperty of(MetadataClass owner, Element element) {
        if (element.getModifiers().contains(Modifier.STATIC)) {
            return null;
        }
        if (ElementHelper.containsAnyAnnotation(element, Transient.class)) {
            return null;
        }
        AccessType forceAccessType = ElementHelper.getAnnotationEnumValue(element, Access.class, "value", AccessType.class);
        if (element.getKind() == ElementKind.FIELD) {
            if (owner.getAccessType() != AccessType.FIELD &&
                    forceAccessType != AccessType.FIELD) {
                return null;
            }
        } else if (element.getKind() == ElementKind.METHOD) {
            if (owner.getAccessType() != AccessType.PROPERTY &&
                    forceAccessType != AccessType.PROPERTY) {
                return null;
            }
        } else {
            return null;
        }
        if (ElementHelper.containsAnyAnnotation(
                element, 
                OneToOne.class, 
                ManyToOne.class, 
                OneToMany.class, 
                ManyToMany.class)) {
            return new MetadataAssociation(owner, element);
        }
        if (element instanceof VariableElement) {
            return new MetadataScalar(owner, element);
        } else {
            String methodName = element.getSimpleName().toString();
            if (methodName.startsWith("get") || methodName.startsWith("is")) {
                return new MetadataScalar(owner, element);
            }
        }
        return null;
    }
    
    MetadataProperty(MetadataClass owner, Element element) {
        this.owner = owner;
        String name = element.getSimpleName().toString();
        if (!(element instanceof VariableElement)) {
            name = element.getSimpleName().toString();
            if (name.startsWith("get")) {
                if (name.length() == 3) {
                    throw new IllegalProgramException(
                            methodNameIsInvalid(owner.getName(), "get")
                    );
                }
                name = name.substring(3);
            } else if (name.startsWith("is")) {
                if (name.length() == 2) {
                    throw new IllegalProgramException(
                            methodNameIsInvalid(owner.getName(), "is")
                    );
                }
                name = name.substring(2);
            } else {
                throw new IllegalProgramException(
                        methodNameIsInvalid(owner.getName(), element.getSimpleName().toString())
                );
            }
            name = Strings.toCamelCase(name);
        }
        if (name.equals("end")) {
            throw new IllegalProgramException(
                    propertyNameIsNotAllowed(
                            owner.getName(), 
                            "end",
                            TypedQueryPathProcessor.class
                    )
            );
        }
        if (name.equals("asc")) {
            throw new IllegalProgramException(
                    propertyNameIsNotAllowed(
                            owner.getName(), 
                            "asc",
                            TypedQueryPathProcessor.class
                    )
            );
        }
        if (name.equals("desc")) {
            throw new IllegalProgramException(
                    propertyNameIsNotAllowed(
                            owner.getName(), 
                            "desc",
                            TypedQueryPathProcessor.class
                    )
            );
        }
        this.name = name;
    }
    
    public MetadataClass getOwner() {
        return this.owner;
    }

    public String getName() {
        return this.name;
    }
    
    @I18N
    private static native String methodNameIsInvalid(
            String entityClass,
            String propertyName);
    
    @I18N
    private static native String propertyNameIsNotAllowed(
            String entityClass,
            String propertyName,
            Class<TypedQueryPathProcessor> thisType);
}
