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
package org.babyfish.model.hibernate.instrument.impl;

import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.lang.bytecode.ScopedMethodVisitor;
import org.babyfish.lang.bytecode.ScopedMethodVisitorBuilder;
import org.babyfish.model.hibernate.instrument.spi.ASMConstants;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.instrument.spi.AbstractObjectModelGenerator;
import org.babyfish.model.instrument.spi.Identifiers;
import org.babyfish.model.jpa.instrument.metadata.JPAMetadataProperty;
import org.babyfish.model.jpa.metadata.JPAScalarType;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;

/**
 * @author Tao Chen
 */
class HibernateObjectModelProxyGenerator extends AbstractObjectModelGenerator {

    protected HibernateObjectModelProxyGenerator(ObjectModel4HibernateReplacer parent) {
        super(parent, Identifiers.OBJECT_MODEL_PROXY_SIMPLE_NAME);
    }

    @Override
    protected void generate(ClassVisitor cv) {
        
        String superInternalName = 
                this.getMetadataClass().getSuperClass() != null ? 
                this.getMetadataClass().getSuperClass().getInternalName() + '$' + this.getSimpleName() :
                ASMConstants.ABSTRACT_OBJECT_MODEL_PROXY_INTERNAL_NAME;
                
        cv.visit(
                this.getMetadataClass().getBytecodeVersion(),
                Opcodes.ACC_PROTECTED | Opcodes.ACC_STATIC, 
                this.getInternalName(), 
                null, 
                superInternalName, 
                new String[] { this.getMetadataClass().getInternalName() + '$' + Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME }
        );
        
        try (ScopedMethodVisitor mv =
                new ScopedMethodVisitorBuilder(Opcodes.ACC_PROTECTED, "<init>")
                .self(this.getDescriptor())
                .parameter("modelClass", ASMConstants.MODEL_CLASS_DESCRIPTOR)
                .parameter("owner", ASMConstants.HIBERNATE_PROXY_DESC)
                .build(cv)) {
            mv.visitCode();
            mv.load("this");
            mv.load("modelClass");
            mv.load("owner");
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    superInternalName, 
                    "<init>", 
                    '(' + 
                    ASMConstants.MODEL_CLASS_DESCRIPTOR + 
                    ASMConstants.HIBERNATE_PROXY_DESC + 
                    ")V", 
                    false
            );
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        this.generateLoadScalarsMethod(cv);
        
        for (MetadataProperty metadataProperty : this.getMetadataClass().getDeclaredProperties().values()) {
            this.generatePropertyMethod(cv, (JPAMetadataProperty)metadataProperty, false);
            this.generatePropertyMethod(cv, (JPAMetadataProperty)metadataProperty, true);
        }
        
        cv.visitEnd();
    }
    
    private void generatePropertyMethod(ClassVisitor cv, JPAMetadataProperty metadataProperty, boolean setter) {
        String descriptor = metadataProperty.getDescriptor();
        ScopedMethodVisitorBuilder mvBuilder = 
                new ScopedMethodVisitorBuilder(
                        Opcodes.ACC_PUBLIC, 
                        setter ? Identifiers.setterName(metadataProperty) : Identifiers.getterName(metadataProperty)
                )
                .self(this.getDescriptor());
        if (setter) {
            mvBuilder.parameter("value", descriptor, metadataProperty.getSignature());
        } else {
            mvBuilder.output(descriptor, metadataProperty.getSignature());
        }
        try (ScopedMethodVisitor mv = mvBuilder.build(cv)) {
            mv.visitCode();
            if (metadataProperty.getScalarType() == JPAScalarType.ID) {
                mv.load("this");
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        ASMConstants.ABSTRACT_OBJECT_MODEL_PROXY_INTERNAL_NAME, 
                        "owner", 
                        ASMConstants.HIBERNATE_PROXY_DESC
                );
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        ASMConstants.FROZEN_LAZY_INITIALIZER_INTERNAL_NAME, 
                        "get", 
                        '(' + ASMConstants.HIBERNATE_PROXY_DESC + ')' + ASMConstants.FROZEN_LAZY_INITIALIZER_DESC, 
                        true
                );
                if (setter) {
                    mv.load("value");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASMConstants.LAZY_INITIALIZER_INTERNAL_NAME, 
                            "setIdentifier", 
                            '(' + ASMConstants.SERIALIZABLE_DESCRIPTOR + ")V", 
                            true
                    );
                    mv.visitInsn(Opcodes.RETURN);
                } else {
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASMConstants.LAZY_INITIALIZER_INTERNAL_NAME, 
                            "getIdentifier", 
                            "()" + ASMConstants.SERIALIZABLE_DESCRIPTOR, 
                            true
                    );
                    if (descriptor.charAt(0) == 'L' && descriptor.charAt(descriptor.length() - 1) == ';') {
                        mv.visitTypeInsn(Opcodes.CHECKCAST, descriptor.substring(1, descriptor.length() - 1));
                    } else {
                        mv.visitTypeInsn(Opcodes.CHECKCAST, descriptor);
                    }
                    mv.visitInsn(Opcodes.ARETURN);
                }
            } else {
                mv.load("this");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASMConstants.ABSTRACT_OBJECT_MODEL_PROXY_INTERNAL_NAME, 
                        "getTargetObjectModel", 
                        "()" + ASMConstants.OBJECT_MODEL_DESCRIPTOR, 
                        false
                );
                mv.visitTypeInsn(
                        Opcodes.CHECKCAST, 
                        this.getMetadataClass().getInternalName() + 
                        '$' + 
                        Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME
                );
                if (setter) {
                    mv.load("value");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            this.getMetadataClass().getInternalName() + 
                            '$' + 
                            Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME,
                            Identifiers.setterName(metadataProperty), 
                            '(' + descriptor + ")V", 
                            true
                    );
                    mv.visitInsn(Opcodes.RETURN);
                } else {
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            this.getMetadataClass().getInternalName() + 
                            '$' + 
                            Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME,
                            Identifiers.getterName(metadataProperty), 
                            "()" + descriptor, 
                            true
                    );
                    mv.visitInsn(ASMUtils.getReturnCode(descriptor));
                }
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private void generateLoadScalarsMethod(ClassVisitor cv) {
        
        boolean needGenerate = false;
        for (MetadataProperty metadataProperty : this.getMetadataClass().getPropertyList()) {
            if (metadataProperty.getPropertyType() == PropertyType.SCALAR && metadataProperty.isDeferrable()) {
                needGenerate = true;
                break;
            }
        }
        if (!needGenerate) {
            return;
        }
        
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                "loadScalars", 
                "()V", 
                null,
                null
        );
        mv.visitCode();
        
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                ASMConstants.ABSTRACT_OBJECT_MODEL_PROXY_INTERNAL_NAME, 
                "getTargetObjectModel", 
                "()" + ASMConstants.OBJECT_MODEL_DESCRIPTOR, 
                false
        );
        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE, 
                ASMConstants.OBJECT_MODEL_INTERNAL_NAME, 
                "loadScalars", 
                "()V", 
                true
        );
        mv.visitInsn(Opcodes.RETURN);
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
