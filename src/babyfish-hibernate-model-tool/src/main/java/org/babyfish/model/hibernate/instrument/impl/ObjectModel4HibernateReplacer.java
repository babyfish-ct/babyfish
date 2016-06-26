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

import java.io.File;

import org.babyfish.lang.bytecode.ScopedMethodVisitor;
import org.babyfish.lang.bytecode.ScopedMethodVisitorBuilder;
import org.babyfish.model.hibernate.instrument.spi.ASMConstants;
import org.babyfish.model.instrument.metadata.MetadataClass;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.instrument.spi.Identifiers;
import org.babyfish.model.jpa.instrument.impl.ObjectModel4JPAInstrumenter;
import org.babyfish.model.jpa.instrument.spi.AbstractObjectModel4JPAReplacer;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;

/**
 * @author Tao Chen
 */
public class ObjectModel4HibernateReplacer extends AbstractObjectModel4JPAReplacer {
    
    private boolean fieldHandled;
    
    private boolean fieldHandledMethods;

    public ObjectModel4HibernateReplacer(
            ObjectModel4JPAInstrumenter instrumenter, 
            String className,
            File classFile) {
        super(instrumenter, className, classFile);
        MetadataClass metadataClass = this.getMetadataClass();
        for (MetadataProperty metadataProperty : metadataClass.getPropertyList()) {
            if (metadataProperty.getPropertyType() == PropertyType.SCALAR && metadataProperty.isDeferrable()) {
                this.fieldHandled = true;
                if (metadataProperty.getDeclaringClass() == metadataClass) {
                    this.fieldHandledMethods = true;
                    break;
                }
            }
        }
    }

    @Override
    protected void createObjectModelTargetGenerator() {
        new HibernateObjectModelTargetGenerator(this);
    }
    
    @Override
    protected void createObjectModelProxyGenerator() {
        new HibernateObjectModelProxyGenerator(this);
    }
    
    @Override
    protected String getRuntimeModelClassImplDescriptor() {
        return ASMConstants.HIBERNATE_MODEL_CLASS_IMPL_DESCRIPTOR;
    }

    @Override
    protected String[] determineMoreInterfaces() {
        String[] interfaces = super.determineMoreInterfaces();
        if (this.fieldHandled) {
            int len = interfaces.length;
            String[] arr = new String[len + 1];
            System.arraycopy(interfaces, 0, arr, 0, len);
            arr[len] = ASMConstants.FIELD_HANDLED_INTERNAL_NAME;
            return arr;
        }
        return interfaces;
    }

    @Override
    protected ClassVisitor onCreateClassAdapter(ClassVisitor cv) {
        if (this.fieldHandledMethods) {
            return this.new ClassAdapter(super.onCreateClassAdapter(cv));
        }
        return super.onCreateClassAdapter(cv);
    }

    @Override
    protected boolean isJPAAnnotation(String annotationDesc) {
        int lastSlashIndex = annotationDesc.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            String packageName = annotationDesc.substring(1, lastSlashIndex);
            return 
                    packageName.equals("javax/persistence") || 
                    packageName.equals("org/hibernate/annotations");
        }
        return false;
    }

    @Override
    protected void generateCreateObjectModelInsns(MethodVisitor mv) {
        if (this.isProxySupported()) {
            Label isNotProxyLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.INSTANCEOF, ASMConstants.HIBERNATE_RROXY_INTERNAL_NAME);
            mv.visitJumpInsn(Opcodes.IFEQ, isNotProxyLabel);
            this.generateCreateObjectModelProxy(mv);
            mv.visitLabel(isNotProxyLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
        super.generateCreateObjectModelInsns(mv);
    }
    
    private void generateCreateObjectModelProxy(MethodVisitor mv) {
        String internalName = this.getMetadataClass().getInternalName() + '$' + Identifiers.OBJECT_MODEL_PROXY_SIMPLE_NAME;
        mv.visitTypeInsn(Opcodes.NEW, internalName);
        mv.visitInsn(Opcodes.DUP);
        mv.visitFieldInsn(
                Opcodes.GETSTATIC, 
                this.getInternalName(), 
                Identifiers.MODEL_CLASS_FIELD_NAME, 
                ASMConstants.MODEL_CLASS_DESCRIPTOR
        );
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, ASMConstants.HIBERNATE_RROXY_INTERNAL_NAME);
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                internalName, 
                "<init>", 
                '(' + 
                ASMConstants.MODEL_CLASS_DESCRIPTOR + 
                ASMConstants.HIBERNATE_PROXY_DESC + ")V", 
                false
        );
        mv.visitInsn(Opcodes.ARETURN);
    }
    
    private class ClassAdapter extends ClassVisitor {

        public ClassAdapter(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public void visitEnd() {
            this.generateGetFieldHandlerMethod();
            this.generateSetFieldHandlerMethod();
            super.visitEnd();
        }
        
        private void generateGetFieldHandlerMethod() {
            
            ObjectModel4HibernateReplacer that = ObjectModel4HibernateReplacer.this;
            
            try (ScopedMethodVisitor mv = 
                    new ScopedMethodVisitorBuilder(Opcodes.ACC_PUBLIC, "getFieldHandler")
                    .self(that.getDescriptor())
                    .output(ASMConstants.FIELD_HANDLER_DESCRIPTOR)
                    .build(this.cv)) {
                
                mv.visitCode();
                
                mv.load("this");
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        that.getMetadataClass().getAncestorClass().getInternalName(), 
                        Identifiers.OBJECT_MODEL_FIELD_NAME, 
                        'L' + 
                        that.getMetadataClass().getAncestorClass().getInternalName() +
                        '$' + 
                        Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME + 
                        ';'
                );
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASMConstants.OBJECT_MODEL_INTERNAL_NAME, 
                        "getScalarLoader", 
                        "()" + ASMConstants.SCALAR_LOADER_DESCRIPTOR,
                        true
                );
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASMConstants.FIELD_HANDLER_INTERNAL_NAME);
                mv.visitInsn(Opcodes.ARETURN);
                
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        
        private void generateSetFieldHandlerMethod() {
            
            ObjectModel4HibernateReplacer that = ObjectModel4HibernateReplacer.this;
            
            try (ScopedMethodVisitor mv = 
                    new ScopedMethodVisitorBuilder(Opcodes.ACC_PUBLIC, "setFieldHandler")
                    .self(that.getDescriptor())
                    .parameter("handler", ASMConstants.FIELD_HANDLER_DESCRIPTOR)
                    .build(this.cv)) {
                
                mv.visitCode();
                
                mv.declare("om", ASMConstants.OBJECT_MODEL_DESCRIPTOR);
                mv.load("this");
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        that.getMetadataClass().getAncestorClass().getInternalName(), 
                        Identifiers.OBJECT_MODEL_FIELD_NAME, 
                        'L' + 
                        that.getMetadataClass().getAncestorClass().getInternalName() +
                        '$' + 
                        Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME + 
                        ';'
                );
                mv.store("om");
                
                mv.load("om");
                mv.visitTypeInsn(Opcodes.NEW, ASMConstants.HIBERNATE_SCALAR_LOADER_INTERNAL_NAME);
                mv.visitInsn(Opcodes.DUP);
                mv.load("om");
                mv.load("handler");
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL, 
                        ASMConstants.HIBERNATE_SCALAR_LOADER_INTERNAL_NAME, 
                        "<init>", 
                        '(' + 
                        ASMConstants.OBJECT_MODEL_DESCRIPTOR + 
                        ASMConstants.FIELD_HANDLER_DESCRIPTOR + 
                        ")V", 
                        false
                );
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASMConstants.OBJECT_MODEL_INTERNAL_NAME, 
                        "setScalarLoader", 
                        '(' + ASMConstants.SCALAR_LOADER_DESCRIPTOR + ")V",
                        true
                );
                
                mv.visitInsn(Opcodes.RETURN);
                
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
    }
}
