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
package org.babyfish.model.jpa.instrument.impl;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.I18N;
import org.babyfish.lang.bytecode.ASMTreeUtils;
import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.model.Contravariance;
import org.babyfish.model.ModelType;
import org.babyfish.model.instrument.metadata.MetadataClass;
import org.babyfish.model.instrument.metadata.MetadataComparatorPart;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.instrument.metadata.spi.AbstractMetadataClass;
import org.babyfish.model.instrument.spi.AbstractObjectModelInstrumenter;
import org.babyfish.model.jpa.JPAModel;
import org.babyfish.model.jpa.instrument.metadata.JPAMetadataClass;
import org.babyfish.model.jpa.instrument.metadata.JPAMetadataProperty;
import org.babyfish.model.jpa.metadata.JPAScalarType;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.org.objectweb.asm.tree.AnnotationNode;
import org.babyfish.org.objectweb.asm.tree.ClassNode;
import org.babyfish.org.objectweb.asm.tree.FieldNode;
import org.babyfish.org.objectweb.asm.tree.MethodNode;

/**
 * @author Tao Chen
 */
final class JPAMetadataClassImpl extends AbstractMetadataClass implements JPAMetadataClass {
    
    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] JPA_MODEL_ANNOATION_TYPES = new Class[] { 
            Entity.class, Embeddable.class, MappedSuperclass.class 
    };
    
    private static final ModelType[] MODEL_TYPES = 
            new ModelType[] { ModelType.REFERENCE, ModelType.EMBEDDABLE, ModelType.ABSTRACT };
    
    ModelType modelType;
    
    XOrderedMap<String, JPAMetadataPropertyImpl> declaredProperties;
    
    XOrderedMap<String, JPAMetadataPropertyImpl> properties;
    
    List<JPAMetadataPropertyImpl> propertyList;
    
    JPAMetadataClassImpl superClass;
    
    JPAMetadataClassImpl ancestorClass;
    
    JPAMetadataPropertyImpl idProperty;
    
    JPAMetadataPropertyImpl versionProperty;
    
    Collection<MetadataComparatorPart> comparatorParts;
    
    Unresolved unresolved;

    protected JPAMetadataClassImpl(File classFile, ClassNode classNode) {
        super(classFile, classNode);
        this.unresolved = new Unresolved();
        this.unresolved.classNode = classNode;
        this.determineModelType();
        this.validateAccess();
        this.validateMethods();
        this.initDeclaredProperties();
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

    @Override
    public JPAMetadataProperty getIdProperty() {
        return this.idProperty;
    }

    @Override
    public JPAMetadataProperty getVersionProperty() {
        return this.versionProperty;
    }

    @Override
    public void finish() {
        this.unresolved = null;
    }
    
    public void resolveSuperClass(AbstractObjectModelInstrumenter instrumenter) {
        JPAMetadataClassImpl superClass = null;
        String superTypeName = this.getSuperTypeName();
        while (!superTypeName.equals("java.lang.Object")) {
            superClass = instrumenter.getMetadataClass(superTypeName);
            if (superClass == null) {
                ClassNode superClassNode = instrumenter.getClassNode(superTypeName);
                superTypeName = superClassNode.superName.replace('/', '.');
            } else {
                switch (this.modelType) {
                case REFERENCE:
                    if (superClass.getModelType() == ModelType.EMBEDDABLE) {
                        throw new IllegalClassException(
                                invalidSuperModelType(
                                        this.getName(),
                                        this.unresolved.modelAnnotationType,
                                        superClass.getName(),
                                        superClass.unresolved.modelAnnotationType
                                )
                        );
                    }
                    break;
                default:
                    if (superClass.getModelType() != ModelType.ABSTRACT) {
                        throw new IllegalClassException(
                                invalidSuperModelType(
                                        this.getName(),
                                        this.unresolved.modelAnnotationType,
                                        superClass.getName(),
                                        superClass.unresolved.modelAnnotationType
                                )
                        );
                    }
                    break;
                }
                break;
            }
        }
        ClassNode classNode = this.unresolved.classNode;
        AnnotationNode overrideNode = ASMTreeUtils.getAnnotationNode(classNode, AttributeOverrides.class);
        if (overrideNode == null) {
            overrideNode = ASMTreeUtils.getAnnotationNode(classNode, AttributeOverride.class);
        }
        if (overrideNode != null) {
            if (superClass == null || superClass.getModelType() != ModelType.ABSTRACT) {
                throw new IllegalClassException(
                        overridingRequiresSuperAbstractModel(
                                this.getName(),
                                ASMUtils.toClassName(overrideNode.desc),
                                JPAModel.class,
                                MappedSuperclass.class
                        )
                );
            }
        }
        this.superClass = superClass;
    }
    
    public void resolveAncestorClass() {
        if (this.superClass == null) {
            this.ancestorClass = this;
        } else {
            this.superClass.resolveAncestorClass();
            this.ancestorClass = this.superClass.ancestorClass;
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
        XOrderedMap<String, JPAMetadataPropertyImpl> map = new LinkedHashMap<>(this.superClass.properties);
        for (JPAMetadataPropertyImpl property : this.declaredProperties.values()) {
            MetadataProperty superProperty = map.put(property.getName(), property);
            if (superProperty != null && !superProperty.getName().equals(property.unresolved.contravarianceFrom)) {
                throw new IllegalClassException(
                        missContravarinaceWithSameName(
                                property,
                                superProperty,
                                Contravariance.class,
                                property.getName()
                        )
                );
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
        JPAMetadataPropertyImpl[] arr = new JPAMetadataPropertyImpl[propertyId + this.declaredProperties.size()];
        if (this.superClass != null) {
            this.superClass.propertyList.toArray(arr);
        }
        for (JPAMetadataPropertyImpl property : this.declaredProperties.values()) {
            arr[propertyId] = property;
            property.id = propertyId++;
        }
        this.propertyList = MACollections.wrap(arr);
    }
    
    public void resolveIdProperty() {
        if (this.unresolved.idPropertyResolved) {
            return;
        }
        this.unresolved.idPropertyResolved = true;
        
        if (this.superClass != null) {
            this.superClass.resolveIdProperty();
            this.idProperty = this.superClass.idProperty;
        }
        for (JPAMetadataPropertyImpl property : this.declaredProperties.values()) {
            if (property.scalarType == JPAScalarType.ID) {
                if (this.idProperty != null) {
                    throw new IllegalClassException(
                            duplicatedSpecialProperty(
                                    property,
                                    this.idProperty,
                                    Id.class
                            )
                    );
                }
                if (this.modelType != ModelType.REFERENCE) {
                    throw new IllegalClassException(
                            specicalPropertyRequiresEntity(
                                    property,
                                    Id.class,
                                    Entity.class
                            )
                    );
                }
                this.idProperty = property;
            }
        }
        if (this.modelType == ModelType.REFERENCE && this.idProperty == null) {
            throw new IllegalClassException(
                    idPropertyIsRequired(
                            this.getName(),
                            Id.class,
                            Entity.class
                    )
            );
        }
    }
    
    public void resolveVersionProperty() {
        if (this.unresolved.versionPropertyResolved) {
            return;
        }
        this.unresolved.versionPropertyResolved = true;
        
        if (this.superClass != null) {
            this.superClass.resolveVersionProperty();
            this.versionProperty = this.superClass.versionProperty;
        }
        for (JPAMetadataPropertyImpl property : this.declaredProperties.values()) {
            if (property.scalarType == JPAScalarType.VERSION) {
                if (this.versionProperty != null) {
                    throw new IllegalClassException(
                            duplicatedSpecialProperty(
                                    property,
                                    this.versionProperty,
                                    Version.class
                            )
                    );
                }
                if (this.modelType != ModelType.REFERENCE) {
                    throw new IllegalClassException(
                            specicalPropertyRequiresEntity(
                                    property,
                                    Version.class,
                                    Entity.class
                            )
                    );
                }
                this.idProperty = property;
            }
        }
    }
    
    public void resolveComparatorParts() {
        this.comparatorParts = this.determineComparatorParts(this.unresolved.classNode);
    }
    
    private void determineModelType() {
        
        Unresolved unresolved = this.unresolved;
        ClassNode classNode = unresolved.classNode;
        
        ModelType type = null;
        for (int i = MODEL_TYPES.length - 1; i >= 0; i--) {
            if (ASMTreeUtils.getAnnotationNode(classNode, JPA_MODEL_ANNOATION_TYPES[i]) != null) {
                if (unresolved.modelAnnotationType != null) {
                    throw new IllegalClassException(
                            conflictAnnotations(
                                    this.getName(), 
                                    unresolved.modelAnnotationType, 
                                    JPA_MODEL_ANNOATION_TYPES[i]
                            )
                    );
                }
                unresolved.modelAnnotationType = JPA_MODEL_ANNOATION_TYPES[i];
                type = MODEL_TYPES[i];
            }
        }
        if (type == null) {
            throw new IllegalClassException(
                    missAnnotation(
                            this.getName(), 
                            JPA_MODEL_ANNOATION_TYPES
                    )
            );
        }
        this.modelType = type;
    }

    private void validateAccess() {
        ClassNode classNode = this.unresolved.classNode;
        AnnotationNode accessNode = ASMTreeUtils.getAnnotationNode(classNode, Access.class);
        if (accessNode != null) {
            throw new IllegalClassException(
                    unexpectedJpaModelAccess(
                            this.getName(),
                            Access.class,
                            JPAModel.class
                    )
            );
        }
    }
    
    private void validateMethods() {
        ClassNode classNode = this.unresolved.classNode;
        if (classNode.methods == null) {
            return;
        }
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.visibleAnnotations != null) {
                for (AnnotationNode annotationNode : methodNode.visibleAnnotations) {
                    String annotationClassName = ASMUtils.toClassName(annotationNode.desc);
                    int lastDotIndex = annotationClassName.lastIndexOf('.');
                    if (lastDotIndex != -1 && annotationClassName.substring(0, lastDotIndex).equals("javax.persistence")) {
                        Type[] types = Type.getArgumentTypes(methodNode.desc);
                        StringBuilder builder = new StringBuilder();
                        builder.append(methodNode.name).append('(');
                        boolean addComma = false;
                        for (Type type : types) {
                            if (addComma) {
                                builder.append(", ");
                            } else {
                                builder.append(ASMUtils.toClassName(type.getDescriptor()));
                            }
                        }
                        builder.append(')');
                        throw new IllegalClassException(
                                methodShouldNotBeMarkedBy(
                                        this.getName(),
                                        builder.toString(),
                                        annotationClassName,
                                        JPAModel.class
                                )
                        );
                    }
                }
            }
        }
    }
    
    private void initDeclaredProperties() {
        ClassNode classNode = this.unresolved.classNode;
        if (classNode.fields == null) {
            this.declaredProperties = MACollections.emptyOrderedMap();
            return;
        }
        XOrderedMap<String, JPAMetadataPropertyImpl> map = new LinkedHashMap<>();
        for (FieldNode fieldNode : classNode.fields) {
            Set<String> annotationClassNames = new LinkedHashSet<>();
            collectAnnoationClassNames(fieldNode.visibleAnnotations, "javax.persistence", annotationClassNames);
            collectAnnoationClassNames(
                    fieldNode.invisibleAnnotations, 
                    Contravariance.class.getPackage().getName(), 
                    annotationClassNames);
            if ((fieldNode.access & Opcodes.ACC_STATIC) != 0) {
                if (!annotationClassNames.isEmpty()) {
                    throw new IllegalClassException(
                            annotatedFieldCanNotBeStatic(
                                    this.getName(), 
                                    fieldNode.name, 
                                    annotationClassNames.iterator().next()
                            )
                    );
                }
                continue;
            }
            if ((fieldNode.access & Opcodes.ACC_PRIVATE) == 0 && !annotationClassNames.isEmpty()) {
                throw new IllegalClassException(
                        annotatedFieldMustBePrivate(
                                this.getName(), 
                                fieldNode.name, 
                                annotationClassNames.iterator().next()
                        )
                );
            }
            if (annotationClassNames.remove(Transient.class.getName())) {
                annotationClassNames.remove(Contravariance.class.getName());
                if (!annotationClassNames.isEmpty()) {
                    throw new IllegalClassException(
                            transientFieldCanNotBeMarkedBy(
                                    this.getName(),
                                    fieldNode.name,
                                    annotationClassNames.iterator().next(),
                                    Transient.class
                            )
                    );
                }
            }
            map.put(fieldNode.name, new JPAMetadataPropertyImpl(this, fieldNode));
        }
        this.declaredProperties = MACollections.unmodifiable(map);
    }
    
    private static void collectAnnoationClassNames(
            List<AnnotationNode> annotationNodes, 
            String packageName,
            Set<String> outputClassNames) {
        if (annotationNodes == null) {
            return;
        }
        for (AnnotationNode annotationNode : annotationNodes) {
            String className = ASMUtils.toClassName(annotationNode.desc);
            int lastDotIndex = className.lastIndexOf('.');
            if (lastDotIndex != -1 && className.substring(0, lastDotIndex).equals(packageName)) {
                outputClassNames.add(className);
            }
        }
    }
    
    private static class Unresolved {
        
        ClassNode classNode;
        
        Class<? extends Annotation> modelAnnotationType;
        
        boolean idPropertyResolved;
        
        boolean versionPropertyResolved;
    }

    @I18N    
    private static native String conflictAnnotations(
                String className, 
                Class<? extends Annotation> annoationType1, 
                Class<? extends Annotation> annoationType2);

    @I18N    
    private static native String missAnnotation(
                String className,
                Class<?>[] annotationTypes
        );
        
    @I18N    
    private static native String invalidSuperModelType(
                String className, 
                Class<?> modelAnnoatationType, 
                String superClassName, 
                Class<?> superModelAnnoatationType);
        
    @I18N    
    private static native String unexpectedJpaModelAccess(
                String className, 
                Class<Access> accessTypeConstant, 
                Class<JPAModel> jpaModelTypeConstant);
        
    @I18N    
    private static native String annotatedFieldCanNotBeStatic(
                String className,
                String fieldName,
                String annotationClassName);
        
    @I18N    
    private static native String annotatedFieldMustBePrivate(
                String className,
                String fieldName,
                String annotationClassName);
        
    @I18N    
    private static native String transientFieldCanNotBeMarkedBy(
                String className, 
                String fieldName, 
                String invalidAnnotationClassName,
                Class<Transient> transientTypeConstant);
        
    @I18N    
    private static native String methodShouldNotBeMarkedBy(
                String className, 
                String method, 
                String jpaAnnotationClassName,
                Class<JPAModel> jpaModelTypeConstant);
        
    @I18N    
    private static native String overridingRequiresSuperAbstractModel(
                String className, 
                String overrideAnnotationClassName, 
                Class<JPAModel> jpaModelTypeConstant,
                Class<MappedSuperclass> mappedSuperclassTypeConstant);
        
    @I18N    
    private static native String missContravarinaceWithSameName(
                JPAMetadataPropertyImpl property, 
                MetadataProperty superProperty,
                Class<Contravariance> contravarianceType, 
                String convarianceName);
        
    @I18N    
    private static native String duplicatedSpecialProperty(
                JPAMetadataPropertyImpl property, 
                JPAMetadataPropertyImpl idProperty,
                Class<? extends Annotation> annotationType);
        
    @I18N    
    private static native String specicalPropertyRequiresEntity(
                JPAMetadataPropertyImpl property, 
                Class<? extends Annotation> annotationType, 
                Class<Entity> entityTypeConstant);
        
    @I18N    
    private static native String idPropertyIsRequired(
                String className, 
                Class<Id> idTypeConstant, 
                Class<Entity> entityTypeConstant);
}
