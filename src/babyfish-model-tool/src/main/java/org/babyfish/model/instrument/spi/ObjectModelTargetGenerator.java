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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.babyfish.collection.ArrayList;
import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.lang.bytecode.ScopedMethodVisitor;
import org.babyfish.lang.bytecode.ScopedMethodVisitorBuilder;
import org.babyfish.lang.bytecode.VariableScope;
import org.babyfish.lang.bytecode.VariableScopeBuilder;
import org.babyfish.model.instrument.metadata.MetadataClass;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.model.spi.association.AssociatedEndpoint;
import org.babyfish.model.spi.association.AssociatedIndexedReference;
import org.babyfish.model.spi.association.AssociatedKeyedReference;
import org.babyfish.model.spi.association.AssociatedList;
import org.babyfish.model.spi.association.AssociatedNavigableMap;
import org.babyfish.model.spi.association.AssociatedNavigableSet;
import org.babyfish.model.spi.association.AssociatedOrderedMap;
import org.babyfish.model.spi.association.AssociatedOrderedSet;
import org.babyfish.model.spi.association.AssociatedReference;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;

/**
 * @author Tao Chen
 */
public class ObjectModelTargetGenerator extends AbstractObjectModelGenerator {
    
    private boolean overrideLoadScalars;

    private boolean overrideInitAssociations;
    
    public ObjectModelTargetGenerator(AbstractModelReplacer parent) {
        super(
                parent,
                parent.isProxySupported() ? 
                        Identifiers.OBJECT_MODEL_TARGET_SIMPLE_NAME : 
                        Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME
        );
        for (MetadataProperty metadataProperty : this.getMetadataClass().getDeclaredProperties().values()) {
            if (metadataProperty.getPropertyType() == PropertyType.SCALAR && metadataProperty.isDeferrable()) {
                this.overrideLoadScalars = true;
                break;
            }
        }
        for (MetadataProperty metadataProperty : this.getMetadataClass().getDeclaredProperties().values()) {
            if (metadataProperty.getPropertyType() == PropertyType.ASSOCIATION ||
                    metadataProperty.getPropertyType() == PropertyType.CONTRAVARIANCE) {
                this.overrideInitAssociations = true;
                break;
            }
        }
    }
    
    protected final String getSuperInternalName() {
        MetadataClass superClass = this.getMetadataClass().getSuperClass();
        if (superClass != null) {
            return superClass.getInternalName() + '$' + this.getSimpleName();
        } else {
            return ASMConstants.ABSTRACT_OBJECT_MODEL_IMPL_INTERNAL_NAME;
        }
    }

    @Override
    protected int determineAccess() {
        return Opcodes.ACC_PROTECTED | Opcodes.ACC_STATIC;
    }
    
    protected String[] determineInterfaces() {
        if (!this.<AbstractModelReplacer>getParent().isProxySupported()) {
            return new String[0];
        }
        return new String[] {
                this.getMetadataClass().getInternalName() + 
                '$' + 
                Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME
        };
    }
    
    @Override
    protected final void generate(ClassVisitor cv) {
        cv.visit(
                this.getMetadataClass().getBytecodeVersion(), 
                this.determineAccess(), 
                this.getInternalName(),  
                null,
                this.getSuperInternalName(),
                this.determineInterfaces()
        );
        
        this.generateMembers(cv);
        
        cv.visitEnd();
    }

    private Class<? extends AssociatedEndpoint> determinAssociatedEndpointType(MetadataProperty metadataProperty) {
        switch (metadataProperty.getPropertyType()) {
        case CONTRAVARIANCE:
        case INDEX:
        case KEY:
            throw new AssertionError("Internal bug");
        default:
        }
        return this.onDeterminAssociatedEndpointType(metadataProperty);
    }
    
    protected Class<? extends AssociatedEndpoint> onDeterminAssociatedEndpointType(MetadataProperty metadataProperty) {
        switch (metadataProperty.getAssociationType()) {
        case LIST:
            return AssociatedList.class;
        case COLLECTION:
            if (SortedSet.class.isAssignableFrom(metadataProperty.getStandardCollectionType())) {
                return AssociatedNavigableSet.class;
            }
            return AssociatedOrderedSet.class;
        case MAP:
            if (SortedMap.class.isAssignableFrom(metadataProperty.getStandardCollectionType())) {
                return AssociatedNavigableMap.class;
            }
            return AssociatedOrderedMap.class;
        case REFERENCE:
            return AssociatedReference.class;
        case INDEXED_REFERENCE:
            return AssociatedIndexedReference.class;
        case KEYED_REFERENCE:
            return AssociatedKeyedReference.class;
        default:
            return null;
        }
    }
    
    protected final String fieldInternalName(MetadataProperty metadataProperty) {
        switch (metadataProperty.getPropertyType()) {
        case CONTRAVARIANCE:
        case INDEX:
        case KEY:
            throw new AssertionError("Internal bug");
        default:
        }
        Class<?> associatedEndpointType = this.determinAssociatedEndpointType(metadataProperty);
        if (associatedEndpointType == null) {
            String descriptor = metadataProperty.getDescriptor();
            if (descriptor.charAt(0) == 'L') {
                return descriptor.substring(1, descriptor.length() - 1);
            }
            return descriptor;
        }
        return Type.getInternalName(associatedEndpointType);
    }
    
    protected final String fieldDescriptor(MetadataProperty metadataProperty) {
        
        Class<?> associatedEndpointType = this.determinAssociatedEndpointType(metadataProperty);
        if (associatedEndpointType == null) {
            return metadataProperty.getDescriptor();
        }
        return Type.getDescriptor(associatedEndpointType);
    }
    
    protected final String fieldSignature(MetadataProperty metadataProperty) {
        
        StringBuilder builder = new StringBuilder();
        
        Class<?> associatedEndpointType = this.determinAssociatedEndpointType(metadataProperty);
        if (associatedEndpointType == null) {
            return metadataProperty.getSignature();
        }
        
        builder
        .append('L')
        .append(Type.getInternalName(associatedEndpointType));
        
        switch (metadataProperty.getAssociationType()) {
        case REFERENCE:
        case INDEXED_REFERENCE:
            builder.append('<').append(this.getDescriptor()).append(">;");
            break;
        case KEYED_REFERENCE:
            builder
            .append('<')
            .append(metadataProperty.getKeyProperty().getDescriptor())
            .append(metadataProperty.getDescriptor())
            .append(">;");
            break;
        default:
            String signature = metadataProperty.getSignature();
            builder.append(signature.substring(signature.indexOf('<')));
        }
        return builder.toString();
    }
    
    private void generateMembers(ClassVisitor cv) {
        
        cv
        .visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                Identifiers.DEFERRABLE_SCALAR_PROPERTY_IDS, 
                "[I", 
                null, 
                null
        )
        .visitEnd();
        
        if (!this.<AbstractModelReplacer>getParent().isProxySupported()) {
            this.generateEmbededComparatorStaticFields(cv);
        }
        
        for (MetadataProperty metadataProperty : this.getMetadataClass().getDeclaredProperties().values()) {
            this.generatePropertyFields(cv, metadataProperty);
        }
        
        this.generateConstructor(cv);
        if (this.overrideInitAssociations) {
            this.generateInitAssociatedEndpoints(cv);
        }
        
        for (MetadataProperty metadataProperty : this.getMetadataClass().getDeclaredProperties().values()) {
            this.generatePropertyGetter(cv, metadataProperty);
            this.generatePropertySetter(cv, metadataProperty);
        }
        
        this.generateObjectOperationMethods(cv, true);
        this.generateObjectOperationMethods(cv, false);
        this.generateGetAssociatedEndpointMethod(cv);
        
        this.generateGetDisablityPropertyMethod(cv);
        this.generateSetDisablityPropertyMethod(cv, true);
        this.generateSetDisablityPropertyMethod(cv, false);
        
        this.generateIsLoadedMethod(cv);
        this.generateUnloadMethod(cv);
        this.generateLoadScalars(cv);
        
        if (!this.<AbstractModelReplacer>getParent().isProxySupported()) {
            this.generateHashCodeScalarMethod(cv, false);
            this.generateEqualsScalarMethod(cv, false);
            this.generateCompareScalarMethod(cv, false);
        }
        
        this.generateScalarFrozenContextOperationMethods(cv, true);
        this.generateScalarFrozenContextOperationMethods(cv, false);
        this.generateGetEmbeddedFrozenContextMethod(cv);
        
        this.generateMoreMembers(cv);
        
        if (this.getMetadataClass().getSuperClass() == null || !this.getMetadataClass().getDeclaredProperties().isEmpty()) {
            this.generateWriteObject(cv);
            this.generateReadObject(cv);
        }
        
        this.generateStaticConstructor(cv);
    }
    
    protected void generateMoreMembers(ClassVisitor cv) {}
    
    private void generatePropertyFields(ClassVisitor cv, MetadataProperty metadataProperty) {
        String fieldName = Identifiers.fieldName(metadataProperty);
        if (fieldName != null) {
            cv
            .visitField(
                    Opcodes.ACC_PROTECTED, 
                    fieldName, 
                    this.fieldDescriptor(metadataProperty), 
                    this.fieldSignature(metadataProperty), 
                    null
            )
            .visitEnd();
            String stateFieldName = Identifiers.stateFieldName(metadataProperty);
            String frozenContextFieldName = Identifiers.frozenContextFieldName(metadataProperty);
            if (stateFieldName != null) {
                cv
                .visitField(
                        Opcodes.ACC_PROTECTED, 
                        stateFieldName, 
                        "I", 
                        null, 
                        null
                )
                .visitEnd();
            }
            if (frozenContextFieldName != null) {
                cv
                .visitField(
                        Opcodes.ACC_PROTECTED | Opcodes.ACC_TRANSIENT, 
                        Identifiers.frozenContextFieldName(metadataProperty), 
                        ASMConstants.FROZEN_CONTEXT_DESCRIPTOR, 
                        'L' + 
                        ASMConstants.FROZEN_CONTEXT_INTERNAL_NAME + 
                        "<" +
                        this.getMetadataClass().getDescriptor() +
                        ">;",
                        null)
                .visitEnd();
            }
        }
    }

    private void generateConstructor(ClassVisitor cv) {
        
        MetadataClass superClass = this.getMetadataClass().getSuperClass();
        String superOwnerDescriptor = superClass != null ? superClass.getDescriptor() : "Ljava/lang/Object;";
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PROTECTED, 
                "<init>", 
                '(' +
                ASMConstants.MODEL_CLASS_DESCRIPTOR +
                this.getMetadataClass().getDescriptor() +
                ")V", 
                null, 
                null
        );
        mv.visitCode();
        
        try (VariableScope scope = 
                new VariableScopeBuilder()
                .parameter("this", this.getDescriptor())
                .parameter("modelClass", ASMConstants.MODEL_CLASS_DESCRIPTOR)
                .parameter("owner", superOwnerDescriptor)
                .build(mv)) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    this.getSuperInternalName(), 
                    "<init>", 
                    '(' +
                    ASMConstants.MODEL_CLASS_DESCRIPTOR +
                    superOwnerDescriptor +
                    ")V", 
                    false
            );
            
            if (this.overrideInitAssociations) {
                scope.load("this");
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL, 
                        this.getInternalName(), 
                        Identifiers.INIT_ASSOCIATED_ENDPOINTS_METHOD_NAME, 
                        "()V", 
                        false
                );
            }
            
            mv.visitInsn(Opcodes.RETURN);
        }
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateInitAssociatedEndpoints(ClassVisitor cv) {
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PRIVATE, 
                Identifiers.INIT_ASSOCIATED_ENDPOINTS_METHOD_NAME, 
                "()V", 
                null,
                null
        );
        mv.visitCode();
        
        for (MetadataProperty metadataProperty : this.getMetadataClass().getDeclaredProperties().values()) {
            if (metadataProperty.getPropertyType() == PropertyType.ASSOCIATION) {
                String fieldInternalName = this.fieldInternalName(metadataProperty);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitTypeInsn(Opcodes.NEW, fieldInternalName);
                mv.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitLdcInsn(metadataProperty.getId());
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL, 
                        fieldInternalName, 
                        "<init>", 
                        '(' + ASMConstants.OBJECT_MODEL_DESCRIPTOR + "I)V", 
                        false
                );
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        this.getInternalName(), 
                        Identifiers.fieldName(metadataProperty), 
                        this.fieldDescriptor(metadataProperty)
                );
            }
        }
        
        for (MetadataProperty metadataProperty : this.getMetadataClass().getDeclaredProperties().values()) {
            String keyDescriptor = null;
            switch (metadataProperty.getPropertyType()) {
            case ASSOCIATION:
                if (metadataProperty.getKeyProperty() != null) {
                    keyDescriptor = metadataProperty.getKeyProperty().getDescriptor();
                } else {
                    keyDescriptor = metadataProperty.getKeyDescriptor();
                }
            case CONTRAVARIANCE:
                String targetDescriptor = metadataProperty.getTargetDescriptor();
                MetadataProperty associationProperty =  baseProperty(metadataProperty);
                String fieldDescriptor = this.fieldDescriptor(associationProperty);
                String fieldInternalName = this.fieldInternalName(associationProperty);
                String fieldName = Identifiers.fieldName(associationProperty);
                // TODO: 
                // Remove "&& ..." after "addKeyValidator" is added into "KeyedReference"
                if (keyDescriptor != null && associationProperty.getAssociationType() == AssociationType.MAP) {
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(
                            Opcodes.GETFIELD, 
                            this.getInternalName(), 
                            fieldName, 
                            fieldDescriptor
                    );
                    ASMUtils.visitClassLdc(mv, keyDescriptor);
                    mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC, 
                            ASMConstants.VALIDATORS_INTERNAL_NAME, 
                            "instanceOf", 
                            "(Ljava/lang/Class;)" + ASMConstants.VALIDATOR_DESCRIPTOR, 
                            false
                    );
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            fieldInternalName, 
                            "addKeyValidator", 
                            '(' + ASMConstants.VALIDATOR_DESCRIPTOR + ")V", 
                            false
                    );
                }
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.getInternalName(), 
                        fieldName, 
                        fieldDescriptor
                );
                ASMUtils.visitClassLdc(mv, targetDescriptor);
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        ASMConstants.VALIDATORS_INTERNAL_NAME, 
                        "instanceOf", 
                        "(Ljava/lang/Class;)" + ASMConstants.VALIDATOR_DESCRIPTOR, 
                        false
                );
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        fieldInternalName, 
                        associationProperty.getAssociationType() == AssociationType.MAP ? "addValueValidator" : "addValidator", 
                        '(' + ASMConstants.VALIDATOR_DESCRIPTOR + ")V", 
                        false
                );
                break;
            default:
                break;
            }
        }
        
        mv.visitMaxs(0, 0);
        mv.visitInsn(Opcodes.RETURN);
    }
    
    private void generatePropertyGetter(ClassVisitor cv, MetadataProperty metadataProperty) {
        
        MetadataProperty associationProperty = metadataProperty;
        
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC,
                Identifiers.getterName(metadataProperty),
                "()" + metadataProperty.getDescriptor(),
                metadataProperty.getSignature() != null ? "()" + metadataProperty.getSignature() : null,
                null
        );
        mv.visitCode();
        
        try (VariableScope scope = 
                new VariableScopeBuilder()
                .parameter("this", this.getDescriptor())
                .build(mv)
        ) {
            switch (metadataProperty.getPropertyType()) {
            case SCALAR:
                this.generateScalarGetterInsns(mv, metadataProperty);
                break;
            case INDEX:
                scope.load("this");
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.getInternalName(), 
                        Identifiers.fieldName(metadataProperty.getReferenceProperty()), 
                        this.fieldDescriptor(metadataProperty.getReferenceProperty())
                );
                mv.visitInsn(metadataProperty.isAbsolute() ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.fieldInternalName(metadataProperty.getReferenceProperty()), 
                        "getIndex", 
                        "(Z)I",
                        false
                );
                mv.visitInsn(Opcodes.IRETURN);
                break;
            case KEY:
                scope.load("this");
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.getInternalName(), 
                        Identifiers.fieldName(metadataProperty.getReferenceProperty()), 
                        this.fieldDescriptor(metadataProperty.getReferenceProperty())
                );
                mv.visitInsn(metadataProperty.isAbsolute() ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.fieldInternalName(metadataProperty.getReferenceProperty()), 
                        "getKey", 
                        "(Z)Ljava/lang/Object;",
                        false
                );
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.toInternalName(metadataProperty.getDescriptor()));
                mv.visitInsn(Opcodes.ARETURN);
                break;
            case CONTRAVARIANCE:
                associationProperty = baseProperty(metadataProperty);
            case ASSOCIATION:
                scope.load("this");
                String fieldInternalName = this.fieldInternalName(associationProperty);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.getInternalName(), 
                        Identifiers.fieldName(associationProperty), 
                        this.fieldDescriptor(associationProperty)
                );
                Class<?> standardCollectionType = associationProperty.getStandardCollectionType();
                if (standardCollectionType == null) {
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            fieldInternalName, 
                            "get", 
                            "()Ljava/lang/Object;",
                            false
                    );
                    mv.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.toInternalName(metadataProperty.getDescriptor()));
                }
                mv.visitInsn(Opcodes.ARETURN);
                break;
            default:
                throw new AssertionError("Internal bug");
            }
        }
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateScalarGetterInsns(MethodVisitor mv, MetadataProperty metadataProperty) {
        
        if (!metadataProperty.isMandatory()) {
            Label enabledLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.getInternalName(), 
                    Identifiers.stateFieldName(metadataProperty), 
                    "I"
            );
            mv.visitInsn(ASMConstants.OP_CONST_DISABLED);
            mv.visitInsn(Opcodes.IAND);
            mv.visitJumpInsn(Opcodes.IFEQ, enabledLabel);
            ASMUtils.visitClassLdc(mv, metadataProperty.getDeclaringClass().getDescriptor());
            mv.visitLdcInsn(metadataProperty.getId());
            mv.visitLdcInsn(metadataProperty.getName());
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                    "getDisabled", 
                    "(Ljava/lang/Class;ILjava/lang/String;)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                    false
            );
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitLabel(enabledLabel);
        }
        
        if (metadataProperty.isDeferrable()) {
            Label loadedLabel = new Label();
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    ASMConstants.ABSTRACT_OBJECT_MODEL_IMPL_INTERNAL_NAME, 
                    "loading", 
                    "Z"
            );
            mv.visitJumpInsn(Opcodes.IFNE, loadedLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.getInternalName(), 
                    Identifiers.stateFieldName(metadataProperty), 
                    "I"
            );
            mv.visitInsn(ASMConstants.OP_CONST_UNLOADED);
            mv.visitInsn(Opcodes.IAND);
            mv.visitJumpInsn(Opcodes.IFEQ, loadedLabel);
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.getInternalName(), 
                    "loadScalars", 
                    "()V",
                    false
            );
            
            mv.visitLabel(loadedLabel);
        }
        
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(
                Opcodes.GETFIELD, 
                this.getInternalName(), 
                metadataProperty.getName(), 
                metadataProperty.getDescriptor()
        );
        if (metadataProperty.getSimpleType() != null && (
                Date.class.isAssignableFrom(metadataProperty.getSimpleType()) || 
                Calendar.class.isAssignableFrom(metadataProperty.getSimpleType()))) {
            
            Label notNullLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFNONNULL, notNullLabel);
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(notNullLabel);
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.getInternalName(), 
                    metadataProperty.getName(), 
                    metadataProperty.getDescriptor()
            );
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    Type.getInternalName(metadataProperty.getSimpleType()), 
                    "clone", 
                    "()Ljava/lang/Object;", 
                    false
            );
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.toInternalName(metadataProperty.getDescriptor()));
        }
        mv.visitInsn(ASMUtils.getReturnCode(metadataProperty.getDescriptor()));
    }
    
    private void generatePropertySetter(ClassVisitor cv, MetadataProperty metadataProperty) {
        
        if (metadataProperty.getPropertyType() == PropertyType.SCALAR) {
            new ScalarSetterGenerator(this.getInternalName(), metadataProperty).generate(cv);
            return;
        }
        
        MetadataProperty associationProprty = metadataProperty;
        
        String setterSignature = metadataProperty.getSignature();
        if (setterSignature != null) {
            setterSignature = '(' + setterSignature + ")V";
        }
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                Identifiers.setterName(metadataProperty), 
                '(' + metadataProperty.getDescriptor() + ")V",
                setterSignature, 
                null
        );
        mv.visitCode();
        
        try (VariableScope scope = 
                new VariableScopeBuilder()
                .parameter("this", this.getDescriptor())
                .parameter("value", metadataProperty.getDescriptor(), metadataProperty.getSignature())
                .build(mv)) {
            
            switch (metadataProperty.getPropertyType()) {
            case INDEX:
                scope.load("this");
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.getInternalName(), 
                        Identifiers.fieldName(metadataProperty.getReferenceProperty()), 
                        this.fieldDescriptor(metadataProperty.getReferenceProperty())
                );
                scope.load("value");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.fieldInternalName(metadataProperty.getReferenceProperty()), 
                        "setIndex", 
                        "(I)I",
                        false
                );
                mv.visitInsn(Opcodes.POP);
                break;
            case KEY:
                scope.load("this");
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.getInternalName(), 
                        Identifiers.fieldName(metadataProperty.getReferenceProperty()), 
                        this.fieldDescriptor(metadataProperty.getReferenceProperty())
                );
                scope.load("value");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.fieldInternalName(metadataProperty.getReferenceProperty()), 
                        "setKey", 
                        "(Ljava/lang/Object;)Ljava/lang/Object;",
                        false
                );
                mv.visitInsn(Opcodes.POP);
                break;
            case CONTRAVARIANCE:
                associationProprty = baseProperty(metadataProperty);
            case ASSOCIATION:
                Label afterChangeLabel = new Label();
                scope.load("this");
                String fieldInternalName = this.fieldInternalName(associationProprty);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.getInternalName(), 
                        Identifiers.fieldName(associationProprty), 
                        this.fieldDescriptor(associationProprty)
                );
                Class<?> standardCollectionType = associationProprty.getStandardCollectionType();
                if (standardCollectionType == null) {
                    scope.load("value");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            fieldInternalName, 
                            "set", 
                            "(Ljava/lang/Object;)Ljava/lang/Object;",
                            false
                    );
                    mv.visitInsn(Opcodes.POP);
                } else if (Map.class.isAssignableFrom(standardCollectionType)) {
                    scope.declare("m", "Ljava/util/Map;");
                    scope.store("m");
                    scope.load("m");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            fieldInternalName, 
                            "clear", 
                            "()V", 
                            false
                    );
                    scope.load("value");
                    mv.visitJumpInsn(Opcodes.IFNULL, afterChangeLabel);
                    scope.load("m");
                    scope.load("value");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            fieldInternalName, 
                            "putAll", 
                            "(Ljava/util/Map;)Z", 
                            false
                    );
                    mv.visitInsn(Opcodes.POP);
                    mv.visitLabel(afterChangeLabel);
                } else {
                    scope.declare("c", "Ljava/util/Collection;");
                    scope.store("c");
                    scope.load("c");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            fieldInternalName, 
                            "clear", 
                            "()V", 
                            false
                    );
                    scope.load("value");
                    mv.visitJumpInsn(Opcodes.IFNULL, afterChangeLabel);
                    scope.load("c");
                    scope.load("value");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            fieldInternalName, 
                            "addAll", 
                            "(Ljava/util/Collection;)Z", 
                            false
                    );
                    mv.visitInsn(Opcodes.POP);
                    mv.visitLabel(afterChangeLabel);
                }
                break;
            default:
                throw new AssertionError("Internal bug");
            }
        }
        
        mv.visitInsn(Opcodes.RETURN);
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateObjectOperationMethods(ClassVisitor cv, boolean getter) {
        
        List<MetadataProperty> propertyList = this.getMetadataClass().getPropertyList();
        
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                getter ? "get" : "set", 
                getter ? "(I)Ljava/lang/Object;" : "(ILjava/lang/Object;)V", 
                null,
                null
        );
        mv.visitCode();
        
        VariableScopeBuilder variableScopeBuilder = 
                new VariableScopeBuilder()
                .parameter("this", this.getDescriptor())
                .parameter("propertyId", "I");
        if (!getter) {
            variableScopeBuilder.parameter("value", "Ljava/lang/Object;");
        }
        try (VariableScope scope = variableScopeBuilder.build(mv)) {
            if (!propertyList.isEmpty()) {
                Label defaultLabel = new Label();
                Label[] labels = new Label[propertyList.size()];
                for (int i = labels.length - 1; i >= 0; i--) {
                    labels[i] = new Label();
                }
                
                mv.visitVarInsn(Opcodes.ILOAD, 1);
                mv.visitTableSwitchInsn(// It's very important to use table-switch, not lookup-switch!!!
                        0, 
                        propertyList.size() - 1, 
                        defaultLabel, 
                        labels
                );
                
                for (final MetadataProperty metadataProperty : propertyList) {
                    
                    mv.visitLabel(labels[metadataProperty.getId()]);
                    
                    if (getter) {
                        ASMUtils.visitBox(mv, metadataProperty.getDescriptor(), v -> {
                            v.visitVarInsn(Opcodes.ALOAD, 0);
                            v.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL, 
                                    ObjectModelTargetGenerator.this.getInternalName(), 
                                    Identifiers.getterName(metadataProperty), 
                                    "()" + metadataProperty.getDescriptor(),
                                    false
                            );
                        });
                        mv.visitInsn(Opcodes.ARETURN);
                    } else {
                        Label nullLabel = new Label();
                        Label endIfLabel = new Label();
                        scope.load("this");
                            if (metadataProperty.getSimpleType() != null && metadataProperty.getSimpleType().isPrimitive()) {
                            scope.load("value");
                            mv.visitJumpInsn(Opcodes.IFNULL, nullLabel);
                            scope.load("value");
                            ASMUtils.visitUnbox(mv, metadataProperty.getDescriptor(), true);
                            mv.visitJumpInsn(Opcodes.GOTO, endIfLabel);
                            mv.visitLabel(nullLabel);
                            mv.visitInsn(ASMUtils.getDefaultCode(metadataProperty.getDescriptor()));
                            mv.visitLabel(endIfLabel);
                        } else {
                            scope.load("value");
                            ASMUtils.visitUnbox(mv, metadataProperty.getDescriptor(), true);
                        }
                        mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL, 
                                ObjectModelTargetGenerator.this.getInternalName(), 
                                Identifiers.setterName(metadataProperty), 
                                '(' + metadataProperty.getDescriptor() + ")V",
                                false
                        );
                        mv.visitInsn(Opcodes.RETURN);
                    }
                }
                
                mv.visitLabel(defaultLabel);
            }
            
            ASMUtils.visitClassLdc(mv, this.getMetadataClass().getDescriptor());
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                    getter ? "getNonExisting" : "setNonExisting", 
                    "(Ljava/lang/Class;I)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                    false
            );
            mv.visitInsn(Opcodes.ATHROW);
        }
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateGetAssociatedEndpointMethod(ClassVisitor cv) {
        
        List<MetadataProperty> propertyList = this.getMetadataClass().getPropertyList();    
        
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                "getAssociatedEndpoint", 
                "(I)" + ASMConstants.ASSOCIATED_ENDPOINT_DESCRIPTOR, 
                null,
                null
        );
        mv.visitCode();
        
        try (VariableScope scope = 
                new VariableScopeBuilder()
                .parameter("this", this.getDescriptor())
                .parameter("associationPropertyId", "I")
                .build(mv)
        ) {
            
            if (!propertyList.isEmpty()) {
                Label defaultLabel = new Label();
                Label[] labels = new Label[propertyList.size()];
                for (int i = labels.length - 1; i >= 0; i--) {
                    labels[i] = new Label();
                }
                
                scope.load("associationPropertyId");
                mv.visitTableSwitchInsn(// It's very important to use table-switch, not lookup-switch!!!
                        0, 
                        propertyList.size() - 1, 
                        defaultLabel, 
                        labels
                );
                
                for (MetadataProperty metadataProperty : propertyList) {
                    
                    mv.visitLabel(labels[metadataProperty.getId()]);
                    metadataProperty = baseProperty(metadataProperty);
                    
                    switch (metadataProperty.getPropertyType()) {
                    case SCALAR:
                        ASMUtils.visitClassLdc(mv, this.getMetadataClass().getDescriptor());
                        mv.visitLdcInsn(metadataProperty.getId());
                        mv.visitLdcInsn(metadataProperty.getName());
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC, 
                                ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                                "getNonAssociation", 
                                "(Ljava/lang/Class;ILjava/lang/String;)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                                false
                        );
                        mv.visitInsn(Opcodes.ATHROW);
                        break;
                    case ASSOCIATION:
                        scope.load("this");
                        mv.visitFieldInsn(
                                Opcodes.GETFIELD, 
                                this.getInternalName(), 
                                Identifiers.fieldName(metadataProperty), 
                                this.fieldDescriptor(metadataProperty)
                        );
                        mv.visitInsn(Opcodes.ARETURN);
                        break;
                    default:
                        throw new AssertionError("Internal bug");
                    }
                }
                
                mv.visitLabel(defaultLabel);
            }
            
            ASMUtils.visitClassLdc(mv, this.getMetadataClass().getDescriptor());
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                    "getNonExistingAsAssociation", 
                    "(Ljava/lang/Class;I)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                    false
            );
            mv.visitInsn(Opcodes.ATHROW);
        }
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateGetDisablityPropertyMethod(ClassVisitor cv) {
        
        List<MetadataProperty> propertyList = this.getMetadataClass().getPropertyList();    
        
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                "isDisabled", 
                "(I)Z", 
                null,
                null
        );
        mv.visitCode();
        
        try (VariableScope scope = 
                new VariableScopeBuilder()
                .parameter("this", this.getDescriptor())
                .parameter("propertyId", "I")
                .build(mv)
        ) {
            
            if (!propertyList.isEmpty()) {
                Label defaultLabel = new Label();
                Label[] labels = new Label[propertyList.size()];
                for (int i = labels.length - 1; i >= 0; i--) {
                    labels[i] = new Label();
                }
                
                scope.load("propertyId");
                mv.visitTableSwitchInsn(// It's very important to use table-switch, not lookup-switch!!!
                        0, 
                        propertyList.size() - 1, 
                        defaultLabel, 
                        labels
                );
                
                for (MetadataProperty metadataProperty : propertyList) {
                    
                    mv.visitLabel(labels[metadataProperty.getId()]);
                    metadataProperty = baseProperty(metadataProperty);
                    
                    if (metadataProperty.getPropertyType() == PropertyType.SCALAR) {
                        String stateFieldName = Identifiers.stateFieldName(metadataProperty);
                        if (stateFieldName != null && !metadataProperty.isMandatory()) {
                            Label falseLabel = new Label();
                            scope.load("this");
                            mv.visitFieldInsn(
                                    Opcodes.GETFIELD, 
                                    this.getInternalName(), 
                                    stateFieldName, 
                                    "I"
                            );
                            mv.visitInsn(ASMConstants.OP_CONST_DISABLED);
                            mv.visitInsn(Opcodes.IAND);
                            mv.visitJumpInsn(Opcodes.IFEQ, falseLabel);
                            mv.visitInsn(Opcodes.ICONST_1);
                            mv.visitInsn(Opcodes.IRETURN);
                            mv.visitLabel(falseLabel);
                        }
                        mv.visitInsn(Opcodes.ICONST_0);
                        mv.visitInsn(Opcodes.IRETURN);
                    } else {
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitFieldInsn(
                                Opcodes.GETFIELD, 
                                this.getInternalName(), 
                                Identifiers.fieldName(metadataProperty), 
                                this.fieldDescriptor(metadataProperty)
                        );
                        mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL, 
                                this.fieldInternalName(metadataProperty), 
                                "isDisabled", 
                                "()Z", 
                                false
                        );
                        mv.visitInsn(Opcodes.IRETURN);
                    }
                }
                
                mv.visitLabel(defaultLabel);
            }
            
            ASMUtils.visitClassLdc(mv, this.getMetadataClass().getDescriptor());
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                    "checkDisablityForNonExisting", 
                    "(Ljava/lang/Class;I)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                    false
            );
            mv.visitInsn(Opcodes.ATHROW);
        }
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateSetDisablityPropertyMethod(ClassVisitor cv, boolean enable) {
        
        List<MetadataProperty> propertyList = this.getMetadataClass().getPropertyList();    
        
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                enable ? "enable" : "disable", 
                "(I)V", 
                null,
                null
        );
        mv.visitCode();
        
        try (VariableScope scope = 
                new VariableScopeBuilder()
                .parameter("this", this.getDescriptor())
                .parameter("propertyId", "I")
                .build(mv)
        ) {
            
            if (!propertyList.isEmpty()) {
                Label defaultLabel = new Label();
                Label[] labels = new Label[propertyList.size()];
                for (int i = labels.length - 1; i >= 0; i--) {
                    labels[i] = new Label();
                }
                
                mv.visitVarInsn(Opcodes.ILOAD, 1);
                mv.visitTableSwitchInsn(// It's very important to use table-switch, not lookup-switch!!!
                        0, 
                        propertyList.size() - 1, 
                        defaultLabel, 
                        labels
                );
                
                for (MetadataProperty metadataProperty : propertyList) {
                    
                    mv.visitLabel(labels[metadataProperty.getId()]);
                    metadataProperty = baseProperty(metadataProperty);
                    
                    if (metadataProperty.getPropertyType() == PropertyType.SCALAR) {
                        String stateFieldName = Identifiers.stateFieldName(metadataProperty);
                        if (stateFieldName != null && !metadataProperty.isMandatory()) {
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitFieldInsn(
                                    Opcodes.GETFIELD, 
                                    this.getInternalName(), 
                                    stateFieldName, 
                                    "I"
                            );
                            if (enable) {
                                mv.visitLdcInsn(ASMConstants.LDC_CONST_CLEAR_DISABLED);
                                mv.visitInsn(Opcodes.IAND);
                            } else {
                                mv.visitInsn(ASMConstants.OP_CONST_DISABLED);
                                mv.visitInsn(Opcodes.IOR);
                            }
                            mv.visitFieldInsn(
                                    Opcodes.PUTFIELD, 
                                    this.getInternalName(), 
                                    stateFieldName, 
                                    "I"
                            );
                        }
                    } else {
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitFieldInsn(
                                Opcodes.GETFIELD, 
                                this.getInternalName(), 
                                Identifiers.fieldName(metadataProperty), 
                                this.fieldDescriptor(metadataProperty)
                        );
                        mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL, 
                                this.fieldInternalName(metadataProperty), 
                                enable ? "enable" : "disable", 
                                "()V", 
                                false
                        );
                    }
                    mv.visitInsn(Opcodes.RETURN);
                }
                
                mv.visitLabel(defaultLabel);
            }
            
            ASMUtils.visitClassLdc(mv, this.getMetadataClass().getDescriptor());
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                    enable ? "enableNonExisting" : "disableNonExisting", 
                    "(Ljava/lang/Class;I)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                    false
            );
            mv.visitInsn(Opcodes.ATHROW);
        }
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateIsLoadedMethod(ClassVisitor cv) {
        List<MetadataProperty> propertyList = this.getMetadataClass().getPropertyList();    
        
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                "isLoaded", 
                "(I)Z", 
                null,
                null
        );
        
        mv.visitCode();
        
        try (VariableScope scope = 
                new VariableScopeBuilder()
                .parameter("this", this.getDescriptor())
                .parameter("propertyId", "I")
                .build(mv)) {
            
            if (!propertyList.isEmpty()) {
                Label defaultLabel = new Label();
                Label[] labels = new Label[propertyList.size()];
                for (int i = labels.length - 1; i >= 0; i--) {
                    labels[i] = new Label();
                }
                
                scope.load("propertyId");
                mv.visitTableSwitchInsn(// It's very important to use table-switch, not lookup-switch!!!
                        0, 
                        propertyList.size() - 1, 
                        defaultLabel, 
                        labels
                );
                for (MetadataProperty metadataProperty : propertyList) {
                    
                    mv.visitLabel(labels[metadataProperty.getId()]);
                    metadataProperty = baseProperty(metadataProperty);
                    
                    switch (metadataProperty.getPropertyType()) {
                    case SCALAR:
                        if (!metadataProperty.isDeferrable()) {
                            mv.visitInsn(Opcodes.ICONST_1);
                            mv.visitInsn(Opcodes.IRETURN);
                        } else {
                            Label unloadedLabel = new Label();
                            String stateFieldName = Identifiers.stateFieldName(metadataProperty);
                            scope.load("this");
                            mv.visitFieldInsn(
                                    Opcodes.GETFIELD, 
                                    this.getInternalName(), 
                                    stateFieldName, 
                                    "I"
                            );
                            mv.visitInsn(ASMConstants.OP_CONST_UNLOADED);
                            mv.visitInsn(Opcodes.IAND);
                            mv.visitJumpInsn(Opcodes.IFNE, unloadedLabel);
                            mv.visitInsn(Opcodes.ICONST_1);
                            mv.visitInsn(Opcodes.IRETURN);
                            mv.visitLabel(unloadedLabel);
                            mv.visitInsn(Opcodes.ICONST_0);
                            mv.visitInsn(Opcodes.IRETURN);
                        }
                        break;
                    case ASSOCIATION:
                        scope.load("this");
                        mv.visitFieldInsn(
                                Opcodes.GETFIELD, 
                                this.getInternalName(), 
                                Identifiers.fieldName(metadataProperty), 
                                this.fieldDescriptor(metadataProperty)
                        );
                        mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL, 
                                this.fieldInternalName(metadataProperty), 
                                "isLoaded", 
                                "()Z", 
                                false
                        );
                        mv.visitInsn(Opcodes.IRETURN);
                        break;
                    default:
                        throw new AssertionError("Internal bug");
                    }
                }
                
                mv.visitLabel(defaultLabel);
            }
            
            ASMUtils.visitClassLdc(mv, this.getMetadataClass().getDescriptor());
            scope.load("propertyId");
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                    "checkLazinessForNonExisting", 
                    "(Ljava/lang/Class;I)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                    false
            );
            mv.visitInsn(Opcodes.ATHROW);
        }
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateUnloadMethod(ClassVisitor cv) {
        List<MetadataProperty> propertyList = this.getMetadataClass().getPropertyList();    
        
        try (ScopedMethodVisitor mv = 
                new ScopedMethodVisitorBuilder(Opcodes.ACC_PUBLIC, "unload")
                .self(this.getDescriptor())
                .parameter("propertyId", "I")
                .build(cv)) {
            
            mv.visitCode();
            
            if (!propertyList.isEmpty()) {
                Label defaultLabel = new Label();
                Label[] labels = new Label[propertyList.size()];
                for (int i = labels.length - 1; i >= 0; i--) {
                    labels[i] = new Label();
                }
                
                mv.load("propertyId");
                mv.visitTableSwitchInsn(// It's very important to use table-switch, not lookup-switch!!!
                        0, 
                        propertyList.size() - 1, 
                        defaultLabel, 
                        labels
                );
                for (MetadataProperty metadataProperty : propertyList) {
                    
                    mv.visitLabel(labels[metadataProperty.getId()]);
                    
                    if (metadataProperty.isDeferrable() && metadataProperty.getPropertyType() == PropertyType.SCALAR) {
                        String frozenContextFieldName = Identifiers.frozenContextFieldName(metadataProperty);
                        if (frozenContextFieldName != null) {
                            Label frozenIsNullLabel = new Label();
                            mv.load("this");
                            mv.visitFieldInsn(
                                    Opcodes.GETFIELD, 
                                    this.getInternalName(), 
                                    frozenContextFieldName, 
                                    ASMConstants.FROZEN_CONTEXT_DESCRIPTOR
                            );
                            mv.visitJumpInsn(Opcodes.IFNULL, frozenIsNullLabel);
                            ASMUtils.visitClassLdc(mv, this.getMetadataClass().getDescriptor());
                            mv.visitLdcInsn(metadataProperty.getId());
                            mv.visitLdcInsn(metadataProperty.getName());
                            mv.visitMethodInsn(
                                    Opcodes.INVOKESTATIC, 
                                    ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                                    "unloadFrozen", 
                                    "(Ljava/lang/Class;ILjava/lang/String;)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                                    false
                            );
                            mv.visitInsn(Opcodes.ATHROW);
                            mv.visitLabel(frozenIsNullLabel);
                        }
                        String stateFieldName = Identifiers.stateFieldName(metadataProperty);
                        mv.load("this");
                        mv.load("this");
                        mv.visitFieldInsn(
                                Opcodes.GETFIELD, 
                                this.getInternalName(), 
                                stateFieldName, 
                                "I"
                        );
                        mv.visitInsn(ASMConstants.OP_CONST_UNLOADED);
                        mv.visitInsn(Opcodes.IOR);
                        mv.visitFieldInsn(
                                Opcodes.PUTFIELD, 
                                this.getInternalName(), 
                                stateFieldName, 
                                "I"
                        );
                        mv.load("this");
                        mv.visitInsn(ASMUtils.getDefaultCode(metadataProperty.getDescriptor()));
                        mv.visitFieldInsn(
                                Opcodes.PUTFIELD, 
                                this.getInternalName(), 
                                metadataProperty.getName(), 
                                metadataProperty.getDescriptor()
                        );
                    }
                    mv.visitInsn(Opcodes.RETURN);
                }
                
                mv.visitLabel(defaultLabel);
            }
            
            ASMUtils.visitClassLdc(mv, this.getMetadataClass().getDescriptor());
            mv.load("propertyId");
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                    "unloadNonExisting", 
                    "(Ljava/lang/Class;I)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                    false
            );
            mv.visitInsn(Opcodes.ATHROW);
        
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private void generateLoadScalars(ClassVisitor cv) {
        if (!this.overrideLoadScalars) {
            return;
        }
        
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "loadScalars", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(
                Opcodes.GETSTATIC, 
                this.getInternalName(),
                Identifiers.DEFERRABLE_SCALAR_PROPERTY_IDS,
                "[I"
        );
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                ASMConstants.ABSTRACT_OBJECT_MODEL_IMPL_INTERNAL_NAME, 
                "load", 
                "([I)V", 
                false
        );
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateScalarFrozenContextOperationMethods(ClassVisitor cv, boolean freeze) {
        
        List<MetadataProperty> propertyList = this.getMetadataClass().getPropertyList();
        
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                freeze ? "freezeScalar" : "unfreezeScalar", 
                "(I" + ASMConstants.FROZEN_CONTEXT_DESCRIPTOR + ")V", 
                "(IL" + ASMConstants.FROZEN_CONTEXT_INTERNAL_NAME + "<*>;)V",
                null
        );
        mv.visitCode();
        
        try (VariableScope scope = 
                new VariableScopeBuilder()
                .parameter("this", this.getDescriptor())
                .parameter("scalarPropertyId", "I")
                .parameter("ctx", ASMConstants.FROZEN_CONTEXT_DESCRIPTOR, 'L' + ASMConstants.FROZEN_CONTEXT_INTERNAL_NAME + "<*>;")
                .build(mv)
        ) {
            if (!propertyList.isEmpty()) {
                Label defaultLabel = new Label();
                Label[] labels = new Label[propertyList.size()];
                for (int i = labels.length - 1; i >= 0; i--) {
                    labels[i] = new Label();
                }
                
                scope.load("scalarPropertyId");
                mv.visitTableSwitchInsn(// It's very important to use table-switch, not lookup-switch!!!
                        0, 
                        propertyList.size() - 1, 
                        defaultLabel, 
                        labels
                );
                
                for (MetadataProperty metadataProperty : propertyList) {
                    
                    mv.visitLabel(labels[metadataProperty.getId()]);
                    
                    if (metadataProperty.getPropertyType() != PropertyType.SCALAR) {
                        ASMUtils.visitClassLdc(mv, metadataProperty.getDeclaringClass().getDescriptor());
                        mv.visitLdcInsn(metadataProperty.getId());
                        mv.visitLdcInsn(metadataProperty.getName());
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC, 
                                ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                                freeze ? "freezeNonScalar" : "unfreezeNonScalar", 
                                "(Ljava/lang/Class;ILjava/lang/String;)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                                false
                        );
                        mv.visitInsn(Opcodes.ATHROW);
                    } else if (metadataProperty.getDescriptor().charAt(0) == '[') {
                        ASMUtils.visitClassLdc(mv, metadataProperty.getDeclaringClass().getDescriptor());
                        mv.visitLdcInsn(metadataProperty.getId());
                        mv.visitLdcInsn(metadataProperty.getName());
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC, 
                                ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                                freeze ? "freezeArray" : "unfreezeArray", 
                                "(Ljava/lang/Class;ILjava/lang/String;)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                                false
                        );
                        mv.visitInsn(Opcodes.ATHROW);
                    } else {
                        String frozenContextFieldName = Identifiers.frozenContextFieldName(metadataProperty);
                        scope.load("this");
                        scope.load("this");
                        mv.visitFieldInsn(
                                Opcodes.GETFIELD, 
                                this.getInternalName(), 
                                frozenContextFieldName, 
                                ASMConstants.FROZEN_CONTEXT_DESCRIPTOR
                        );
                        scope.load("ctx");
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC, 
                                ASMConstants.FROZEN_CONTEXT_INTERNAL_NAME, 
                                freeze ? "combine" : "remove", 
                                '(' + 
                                ASMConstants.FROZEN_CONTEXT_DESCRIPTOR + 
                                ASMConstants.FROZEN_CONTEXT_DESCRIPTOR + 
                                ')' + 
                                ASMConstants.FROZEN_CONTEXT_DESCRIPTOR, 
                                false
                        );
                        mv.visitFieldInsn(
                                Opcodes.PUTFIELD, 
                                this.getInternalName(), 
                                frozenContextFieldName, 
                                ASMConstants.FROZEN_CONTEXT_DESCRIPTOR
                        );
                        mv.visitInsn(Opcodes.RETURN);
                    }
                }
                
                mv.visitLabel(defaultLabel);
            }
            
            ASMUtils.visitClassLdc(mv, this.getMetadataClass().getDescriptor());
            scope.load("scalarPropertyId");
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                    freeze ? "freezeNonExisting" : "unfreezeNonExisting", 
                    "(Ljava/lang/Class;I)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                    false
            );
            mv.visitInsn(Opcodes.ATHROW);
        }
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateGetEmbeddedFrozenContextMethod(ClassVisitor cv) {
        
        List<MetadataProperty> propertyList = this.getMetadataClass().getPropertyList();
        
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PROTECTED, 
                "getEmbeddedFrozenContext", 
                "(I)" + ASMConstants.FROZEN_CONTEXT_DESCRIPTOR, 
                "(I)L" + ASMConstants.FROZEN_CONTEXT_INTERNAL_NAME + "<*>;",
                null
        );
        mv.visitCode();
        
        try (VariableScope scope = 
                new VariableScopeBuilder()
                .parameter("this", this.getDescriptor())
                .parameter("scalarPropertyId", "I")
                .build(mv)
        ) {
            int minEmbeddedPropertyId = -1;
            int maxEmbeddedPropertyId = propertyList.size();
            while (++minEmbeddedPropertyId < maxEmbeddedPropertyId) {
                MetadataProperty property = propertyList.get(minEmbeddedPropertyId);
                if (property.getPropertyType() == PropertyType.SCALAR && property.getTargetClass() != null) {
                    break;
                }
            }
            while (--maxEmbeddedPropertyId >= 0) {
                MetadataProperty property = propertyList.get(maxEmbeddedPropertyId);
                if (property.getPropertyType() == PropertyType.SCALAR && property.getTargetClass() != null) {
                    break;
                }
            }
            if (minEmbeddedPropertyId <= maxEmbeddedPropertyId) {
                Label defaultLabel = new Label();
                Label[] labels = new Label[maxEmbeddedPropertyId - minEmbeddedPropertyId + 1];
                for (int i = labels.length - 1; i >= 0; i--) {
                    labels[i] = new Label();
                }
                
                scope.load("scalarPropertyId");
                mv.visitTableSwitchInsn(// It's very important to use table-switch, not lookup-switch!!!
                        minEmbeddedPropertyId, 
                        maxEmbeddedPropertyId, 
                        defaultLabel, 
                        labels
                );
                
                for (int propertyId = minEmbeddedPropertyId; propertyId <= maxEmbeddedPropertyId; propertyId++) {
                    MetadataProperty metadataProperty = propertyList.get(propertyId);
                    mv.visitLabel(labels[propertyId - minEmbeddedPropertyId]);
                    
                    if (metadataProperty.getPropertyType() != PropertyType.SCALAR || 
                            metadataProperty.getTargetClass() == null) {
                        MetadataProperty nextProperty = propertyList.get(propertyId + 1);
                        if (nextProperty.getPropertyType() == PropertyType.SCALAR && nextProperty.getTargetClass() != null) {
                            mv.visitInsn(Opcodes.ACONST_NULL);
                            mv.visitInsn(Opcodes.ARETURN);
                        }
                    } else {
                        scope.load("this");
                        mv.visitFieldInsn(
                                Opcodes.GETFIELD, 
                                this.getInternalName(), 
                                Identifiers.frozenContextFieldName(metadataProperty), 
                                ASMConstants.FROZEN_CONTEXT_DESCRIPTOR
                        );
                        mv.visitInsn(Opcodes.ARETURN);
                    }
                }
                
                mv.visitLabel(defaultLabel);
            }
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
        }
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateWriteObject(ClassVisitor cv) {
        try (ScopedMethodVisitor mv =
                new ScopedMethodVisitorBuilder(
                        Opcodes.ACC_PRIVATE, 
                        "writeObject", 
                        ASMConstants.IO_EXCEPTION_INTERNAL_NAME)
                .self(this.getDescriptor())
                .parameter("out", ASMConstants.OBJECT_OUTPUT_STREAM_DESCRIPTOR)
                .build(cv)) {
            
            mv.visitCode();
            
            for (MetadataProperty metadataProperty : this.getMetadataClass().getDeclaredProperties().values()) {
                if (metadataProperty.getPropertyType() != PropertyType.SCALAR) {
                    continue;
                }
                String stateFieldName = Identifiers.stateFieldName(metadataProperty);
                if (stateFieldName != null) {
                    mv.load("out");
                    mv.load("this");
                    mv.visitFieldInsn(
                            Opcodes.GETFIELD, 
                            this.getInternalName(), 
                            stateFieldName, 
                            "I"
                    );
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            ASMConstants.OBJECT_OUTPUT_STREAM_INTERNAL_NAME, 
                            "writeInt", 
                            "(I)V", 
                            false
                    );
                }
                mv.load("out");
                mv.load("this");
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.getInternalName(), 
                        Identifiers.fieldName(metadataProperty), 
                        this.fieldDescriptor(metadataProperty)
                );
                visitIOMethodInvocation(mv, metadataProperty, false);
            }
            for (MetadataProperty metadataProperty : this.getMetadataClass().getDeclaredProperties().values()) {
                if (metadataProperty.getPropertyType() != PropertyType.ASSOCIATION) {
                    continue;
                }
                mv.load("out");
                mv.load("this");
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.getInternalName(), 
                        Identifiers.fieldName(metadataProperty), 
                        this.fieldDescriptor(metadataProperty)
                );
                visitIOMethodInvocation(mv, metadataProperty, false);
            }
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private void generateReadObject(ClassVisitor cv) {
        try (ScopedMethodVisitor mv =
                new ScopedMethodVisitorBuilder(
                        Opcodes.ACC_PRIVATE, 
                        "readObject", 
                        ASMConstants.IO_EXCEPTION_INTERNAL_NAME,
                        ASMConstants.CLASS_NOT_FOUND_EXCEPTION_INTERNAL_NAME)
                .self(this.getDescriptor())
                .parameter("in", ASMConstants.OBJECT_INPUT_STREAM_DESCRIPTOR)
                .build(cv)) {
            
            mv.visitCode();
            
            if (this.getMetadataClass().getSuperClass() == null) {
                mv.load("this");
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        ASMConstants.ABSTRACT_OBJECT_MODEL_IMPL_INTERNAL_NAME, 
                        "owner", 
                        "Ljava/lang/Object;"
                );
                mv.visitTypeInsn(Opcodes.CHECKCAST, this.getMetadataClass().getInternalName());
                mv.load("this");
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        this.getMetadataClass().getInternalName(), 
                        Identifiers.OBJECT_MODEL_FIELD_NAME, 
                        'L' + 
                        this.getMetadataClass().getInternalName() + 
                        '$' + 
                        Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME + 
                        ';'
                );
            }
            
            if (this.overrideInitAssociations) {
                mv.load("this");
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL, 
                        this.getInternalName(), 
                        Identifiers.INIT_ASSOCIATED_ENDPOINTS_METHOD_NAME, 
                        "()V", 
                        false
                );
            }
            
            for (MetadataProperty metadataProperty : this.getMetadataClass().getDeclaredProperties().values()) {
                if (metadataProperty.getPropertyType() != PropertyType.SCALAR) {
                    continue;
                }
                String stateFieldName = Identifiers.stateFieldName(metadataProperty);
                if (stateFieldName != null) {
                    mv.load("this");
                    mv.load("in");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            ASMConstants.OBJECT_INPUT_STREAM_INTERNAL_NAME, 
                            "readInt", 
                            "()I", 
                            false
                    );
                    mv.visitFieldInsn(
                            Opcodes.PUTFIELD, 
                            this.getInternalName(), 
                            stateFieldName, 
                            "I"
                    );
                }
                String fieldName = Identifiers.fieldName(metadataProperty);
                String fieldDescriptor = this.fieldDescriptor(metadataProperty);
                
                mv.load("this");
                mv.load("in");
                visitIOMethodInvocation(mv, metadataProperty, true);
                if (metadataProperty.getSimpleType() == null || !metadataProperty.getSimpleType().isPrimitive()) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, this.fieldInternalName(metadataProperty));
                }
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        this.getInternalName(), 
                        fieldName, 
                        fieldDescriptor
                );
                if (metadataProperty.getTargetClass() != null) {
                    Label endLabel = new Label();
                    mv.load("this");
                    mv.visitFieldInsn(
                            Opcodes.GETFIELD, 
                            this.getInternalName(), 
                            fieldName, 
                            fieldDescriptor
                    );
                    mv.visitJumpInsn(Opcodes.IFNULL, endLabel);
                    mv.load("this");
                    mv.visitFieldInsn(
                            Opcodes.GETFIELD, 
                            this.getInternalName(), 
                            fieldName, 
                            fieldDescriptor
                    );
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            metadataProperty.getTargetClass().getInternalName(), 
                            "objectModel", 
                            "()" + ASMConstants.OBJECT_MODEL_DESCRIPTOR, 
                            false
                    );
                    mv.visitTypeInsn(Opcodes.NEW, ASMConstants.EMBEDDED_SCALAR_LISTENER_IMPL_INTERNAL_NAME);
                    mv.visitInsn(Opcodes.DUP);
                    mv.load("this");
                    mv.visitLdcInsn(metadataProperty.getId());
                    mv.visitMethodInsn(
                            Opcodes.INVOKESPECIAL, 
                            ASMConstants.EMBEDDED_SCALAR_LISTENER_IMPL_INTERNAL_NAME, 
                            "<init>", 
                            '(' + ASMConstants.ABSTRACT_OBJECT_MODEL_IMPL_DESCRITPOR + "I)V", 
                            false
                    );
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASMConstants.OBJECT_MODEL_INTERNAL_NAME, 
                            "addScalarListener", 
                            '(' + ASMConstants.SCALAR_LISTENER_DESCRIPTOR + ")V", 
                            true
                    );
                    mv.visitLabel(endLabel);
                }
            }
            for (MetadataProperty metadataProperty : this.getMetadataClass().getDeclaredProperties().values()) {
                if (metadataProperty.getPropertyType() != PropertyType.ASSOCIATION) {
                    continue;
                }
                boolean assignReadedObject = metadataProperty.getStandardCollectionType() == null;
                if (assignReadedObject) {
                    mv.load("this");
                }
                mv.load("in");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASMConstants.OBJECT_INPUT_STREAM_INTERNAL_NAME, 
                        "readObject", 
                        "()Ljava/lang/Object;", 
                        false
                );
                if (assignReadedObject) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, this.fieldInternalName(metadataProperty));
                    mv.visitFieldInsn(
                            Opcodes.PUTFIELD, 
                            this.getInternalName(), 
                            Identifiers.fieldName(metadataProperty), 
                            this.fieldDescriptor(metadataProperty)
                    );
                }
            }
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private static void visitIOMethodInvocation(MethodVisitor mv, MetadataProperty metadataProperty, boolean read) {
        String postfix;
        String desc;
        if (metadataProperty.getSimpleType() != null && metadataProperty.getSimpleType().isPrimitive()) {
            postfix = metadataProperty.getSimpleType().getName();
            postfix = Character.toUpperCase(postfix.charAt(0)) + postfix.substring(1);
            desc = metadataProperty.getDescriptor();
        } else {
            postfix = "Object";
            desc = "Ljava/lang/Object;";
        }
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                read ? ASMConstants.OBJECT_INPUT_STREAM_INTERNAL_NAME : ASMConstants.OBJECT_OUTPUT_STREAM_INTERNAL_NAME, 
                (read ? "read" : "write") + postfix, 
                read ? "()" + desc : '(' + desc + ")V", 
                false
        );
    }

    private void generateStaticConstructor(ClassVisitor cv) {
        
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        
        if (this.overrideLoadScalars) {
            this.generateInitDeferrableScalarPropertyIdsInsn(mv);
        }
        if (!this.<AbstractModelReplacer>getParent().isProxySupported()) {
            this.generateInitComparatorStaticFieldInsns(mv);
        }
        
        mv.visitInsn(Opcodes.RETURN);
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateInitDeferrableScalarPropertyIdsInsn(MethodVisitor mv) {
        
        List<Integer> deferrableScalarPropertyIds = new ArrayList<>();
        for (MetadataProperty metadataProperty : this.getMetadataClass().getPropertyList()) {
            if (metadataProperty.getPropertyType() == PropertyType.SCALAR && metadataProperty.isDeferrable()) {
                deferrableScalarPropertyIds.add(metadataProperty.getId());
            }
        }
        mv.visitLdcInsn(deferrableScalarPropertyIds.size());
        mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
        int index = 0;
        for (Integer deferrablePropertyId : deferrableScalarPropertyIds) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(index++);
            mv.visitLdcInsn(deferrablePropertyId);
            mv.visitInsn(Opcodes.IASTORE);
        }
        mv.visitFieldInsn(
                Opcodes.PUTSTATIC,
                this.getInternalName(),
                Identifiers.DEFERRABLE_SCALAR_PROPERTY_IDS, 
                "[I"
        );
    }

    private static MetadataProperty baseProperty(MetadataProperty metadataProperty) {
        switch (metadataProperty.getPropertyType()) {
        case SCALAR:
            return metadataProperty;
        case INDEX:
        case KEY:
            return baseProperty(metadataProperty.getReferenceProperty());
        default:
            while (true) {
                MetadataProperty convarianceProperty = metadataProperty.getConvarianceProperty();
                if (convarianceProperty == null) {
                    break;
                }
                metadataProperty = convarianceProperty;
            }
            return metadataProperty;
        }
    }
}
