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
package org.babyfish.model.instrument.impl;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.I18N;
import org.babyfish.lang.bytecode.ASMTreeUtils;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.model.Association;
import org.babyfish.model.Contravariance;
import org.babyfish.model.IndexOf;
import org.babyfish.model.KeyOf;
import org.babyfish.model.Model;
import org.babyfish.model.ModelType;
import org.babyfish.model.Scalar;
import org.babyfish.model.instrument.metadata.MetadataClass;
import org.babyfish.model.instrument.metadata.MetadataComparatorPart;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.instrument.metadata.spi.AbstractMetadataClass;
import org.babyfish.model.instrument.spi.AbstractObjectModelInstrumenter;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.org.objectweb.asm.tree.ClassNode;
import org.babyfish.org.objectweb.asm.tree.FieldNode;

/**
 * @author Tao Chen
 */
class MetadataClassImpl extends AbstractMetadataClass {
    
    MetadataClassImpl superClass;
    
    MetadataClassImpl ancestorClass;
    
    ModelType modelType;
    
    XOrderedMap<String, MetadataPropertyImpl> declaredProperties;
    
    XOrderedMap<String, MetadataPropertyImpl> properties;
    
    List<MetadataPropertyImpl> propertyList;
    
    Collection<MetadataComparatorPart> comparatorParts;
    
    private transient Unresolved unresolved;
    
    MetadataClassImpl(File classFile, ClassNode classNode) {
        super(classFile, classNode);
        this.unresolved = new Unresolved();
        this.unresolved.classNode = classNode;
    }

    public void init() {
        this.modelType = ASMTreeUtils.getAnnotationEnumValue(
                ModelType.class,
                ASMTreeUtils.getAnnotationNode(this.unresolved.classNode, Model.class),
                "type", 
                ModelType.REFERENCE);
        
        XOrderedMap<String, MetadataPropertyImpl> map = new LinkedHashMap<>();
        if (this.unresolved.classNode.fields != null) {
            for (FieldNode fieldNode : this.unresolved.classNode.fields) {
                if (ASMTreeUtils.getAnnotationNode(fieldNode, Scalar.class) != null ||
                        ASMTreeUtils.getAnnotationNode(fieldNode, Association.class) != null ||
                        ASMTreeUtils.getAnnotationNode(fieldNode, IndexOf.class) != null ||
                        ASMTreeUtils.getAnnotationNode(fieldNode, KeyOf.class) != null ||
                        ASMTreeUtils.getAnnotationNode(fieldNode, Contravariance.class) != null) {
                    MetadataPropertyImpl metadataProperty = new MetadataPropertyImpl(this, fieldNode);
                    map.put(fieldNode.name, metadataProperty);
                }
            }
        }
        this.declaredProperties = MACollections.unmodifiable(map);
    }
    
    public void afterInit() {
        if (this.modelType != ModelType.REFERENCE) {
            for (MetadataPropertyImpl metadataProperty : this.declaredProperties.values()) {
                if (metadataProperty.propertyType != PropertyType.SCALAR) {
                    throw new IllegalClassException(
                            nonScalarPropertyRequireReferenceModelType(
                                    metadataProperty,
                                    metadataProperty.getDeclaringClass(),
                                    ModelType.REFERENCE
                            )
                    );
                }
            }
        }
    }

    public void resolveSuperClass(AbstractObjectModelInstrumenter instrumenter) {
        String superTypeName = this.getSuperTypeName();
        while (!superTypeName.equals("java.lang.Object")) {
            this.superClass = instrumenter.getMetadataClass(superTypeName);
            if (this.superClass == null) {
                ClassNode superClassNode = instrumenter.getClassNode(superTypeName);
                superTypeName = superClassNode.superName.replace('/', '.');
            } else {
                switch (this.modelType) {
                case ABSTRACT:
                case EMBEDDABLE:
                    if (this.superClass.modelType == ModelType.REFERENCE) {
                        throw new IllegalClassException(
                                illegalSuperModelType(
                                        this,
                                        this.superClass,
                                        this.modelType,
                                        this.superClass.modelType
                                )
                        );
                    }
                    break;
                case REFERENCE:
                    if (this.superClass.modelType == ModelType.EMBEDDABLE) {
                        throw new IllegalClassException(
                                illegalSuperModelType(
                                        this,
                                        this.superClass,
                                        this.modelType,
                                        this.superClass.modelType
                                )
                        );
                    }
                }
                break;
            }
        }
    }
    
    public void resolveAncestorClass() {
        if (this.superClass == null) {
            this.ancestorClass = this;
        } else {
            this.superClass.resolveAncestorClass();
            this.ancestorClass = this.superClass.ancestorClass;
        }
    }
    
    public void validateNonReferenceClass() {
        if (this.modelType != ModelType.REFERENCE && this.superClass == null) {
            boolean hasComparableProperty = false;
            for (MetadataPropertyImpl metadataProperty : this.declaredProperties.values()) {
                if (metadataProperty.getDescriptor().charAt(0) != '[') {
                    hasComparableProperty = true;
                    break;
                }
            }
            if (!hasComparableProperty) {
                throw new IllegalClassException(
                        nonReferenceRootModelIsNotComparable(this, this.modelType)
                );
            }
        }
    }
    
    public void resolveProperties() {
        if (this.properties != null) {
            return;
        }
        if (this.superClass == null) {
            this.properties = this.declaredProperties;
            return;
        }
        this.superClass.resolveProperties();
        if (this.declaredProperties.isEmpty()) {
            this.properties = this.superClass.properties;
            return;
        }
        XOrderedMap<String, MetadataPropertyImpl> map = new LinkedHashMap<>(this.superClass.properties);
        for (MetadataPropertyImpl property : this.declaredProperties.values()) {
            MetadataProperty superProperty = map.put(property.getName(), property);
            if (superProperty != null && !superProperty.getName().equals(property.unresolved.contravarianceFrom)) {
                if (property.getPropertyType() != PropertyType.CONTRAVARIANCE) {
                    throw new IllegalClassException(
                            requireContravarianceAnnotation(
                                    property,
                                    superProperty,
                                    Contravariance.class
                            )
                    );
                }
            }
        }
        this.properties = MACollections.unmodifiable(map);
    }
    
    public void resolvePropertyList() {
        if (this.propertyList != null) {
            return;
        }
        if (this.superClass != null) {
            this.superClass.resolvePropertyList();
        }
        int propertyId = this.superClass != null ? this.superClass.propertyList.size() : 0;
        MetadataPropertyImpl[] arr = new MetadataPropertyImpl[propertyId + this.declaredProperties.size()];
        if (this.superClass != null) {
            this.superClass.propertyList.toArray(arr);
        }
        for (MetadataPropertyImpl property : this.declaredProperties.values()) {
            arr[propertyId] = property;
            property.id = propertyId++;
        }
        this.propertyList = MACollections.wrap(arr);
    }
    
    public void resolveComparatorParts() {
        this.comparatorParts = this.determineComparatorParts(this.unresolved.classNode);
    }

    @Override
    public void finish() {
        this.unresolved = null;
    }

    @Override
    public ModelType getModelType() {
        return this.modelType;
    }

    @Override
    public MetadataClass getSuperClass() {
        return this.superClass;
    }

    @Override
    public MetadataClass getAncestorClass() {
        return this.ancestorClass;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public XOrderedMap<String, MetadataProperty> getDeclaredProperties() {
        return (XOrderedMap)this.declaredProperties;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public XOrderedMap<String, MetadataProperty> getProperties() {
        return (XOrderedMap)this.properties;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<MetadataProperty> getPropertyList() {
        return (List)this.propertyList;
    }

    @Override
    public Collection<MetadataComparatorPart> getComparatorParts() {
        return this.comparatorParts;
    }

    private static class Unresolved {
        
        ClassNode classNode;
    }
    
    @I18N
    private static native String nonScalarPropertyRequireReferenceModelType(
            MetadataProperty property,
            MetadataClass declaringClass,
            ModelType referenceModelTypeConstant);
    
    @I18N
    private static native String illegalSuperModelType(
            MetadataClassImpl metadataClass,
            MetadataClassImpl superMetataClass,
            ModelType thisModelType,
            ModelType superModelType);
    
    @I18N
    private static native String nonReferenceRootModelIsNotComparable(
            MetadataClassImpl metadataClass,
            ModelType modelType);
    
    @I18N
    private static native String requireContravarianceAnnotation(
            MetadataProperty property,
            MetadataProperty superProperty,
            Class<Contravariance> contravarianceTypeConstant);
}
