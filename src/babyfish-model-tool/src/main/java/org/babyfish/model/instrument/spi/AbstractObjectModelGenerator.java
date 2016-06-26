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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.lang.bytecode.VariableScope;
import org.babyfish.lang.bytecode.VariableScopeBuilder;
import org.babyfish.lang.instrument.bytecode.NestedClassGenerator;
import org.babyfish.model.NullComparatorType;
import org.babyfish.model.StringComparatorType;
import org.babyfish.model.instrument.metadata.MetadataClass;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;

/**
 * @author Tao Chen
 */
public abstract class AbstractObjectModelGenerator extends NestedClassGenerator {

    protected AbstractObjectModelGenerator(AbstractModelReplacer parent, String simpleName) {
        super(parent, simpleName);
    }
    
    protected final MetadataClass getMetadataClass() {
        return this.<AbstractModelReplacer>getRoot().getMetadataClass();
    }
    
    final void generateHashCodeScalarMethod(ClassVisitor cv, boolean itf) {
        
        List<MetadataProperty> propertyList = this.getMetadataClass().getPropertyList();    
        
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                "hashCodeScalar", 
                "(I" + ASMConstants.STRING_COMPARATOR_TYPE_DESCRIPTOR + ")I", 
                null,
                null
        );
        mv.visitCode();
        
        try (VariableScope scope = 
                new VariableScopeBuilder()
                .parameter("this", this.getDescriptor())
                .parameter("scalarPropertyId", "I")
                .parameter("stringCompartorType", ASMConstants.STRING_COMPARATOR_TYPE_DESCRIPTOR)
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
                                "hashCodeNonScalar", 
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
                                "hashCodeArray", 
                                "(Ljava/lang/Class;ILjava/lang/String;)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                                false
                        );
                        mv.visitInsn(Opcodes.ATHROW);
                    } else {
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitMethodInsn(
                                itf ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, 
                                this.getInternalName(), 
                                Identifiers.getterName(metadataProperty), 
                                "()" + metadataProperty.getDescriptor(), 
                                itf
                        );
                        switch (metadataProperty.getDescriptor().charAt(0)) {
                        case 'Z':
                            Label falseLabel = new Label();
                            Label endLabel = new Label();
                            mv.visitJumpInsn(Opcodes.IFEQ, falseLabel);
                            mv.visitLdcInsn(1031);
                            mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                            mv.visitLabel(falseLabel);
                            mv.visitLdcInsn(1037);
                            mv.visitLabel(endLabel);
                            mv.visitInsn(Opcodes.IRETURN);
                            break;
                        case 'C':
                        case 'B':
                        case 'S':
                        case 'I':
                            mv.visitInsn(Opcodes.IRETURN);
                            break;
                        case 'F':
                            mv.visitMethodInsn(
                                    Opcodes.INVOKESTATIC, 
                                    "java/lang/Float", 
                                    "floatToIntBits", 
                                    "(F)I", 
                                    false
                            );
                            mv.visitInsn(Opcodes.IRETURN);
                            break;
                        case 'D':
                            mv.visitMethodInsn(
                                    Opcodes.INVOKESTATIC, 
                                    "java/lang/Double", 
                                    "doubleToLongBits", 
                                    "(D)J", 
                                    false
                            );
                            // No break
                        case 'J':
                            try (VariableScope childScope = scope.createSubScope()) {
                                childScope.declare("bits", "J", null);
                                childScope.store("bits");
                                childScope.load("bits");
                                mv.visitIntInsn(Opcodes.BIPUSH, 32);
                                mv.visitInsn(Opcodes.LUSHR);
                                childScope.load("bits");
                                mv.visitInsn(Opcodes.LXOR);
                                mv.visitInsn(Opcodes.L2I);
                                mv.visitInsn(Opcodes.IRETURN);
                            }
                            break;
                        default:
                            try (VariableScope childScope = scope.createSubScope()) {
                                childScope.declare("value", metadataProperty.getDescriptor(), null);
                                childScope.store("value");
                                
                                Label notNullLabel = new Label();
                                Label endObjectLabel = new Label();
                                childScope.load("value");
                                mv.visitJumpInsn(Opcodes.IFNONNULL, notNullLabel);
                                mv.visitInsn(Opcodes.ICONST_0);
                                mv.visitJumpInsn(Opcodes.GOTO, endObjectLabel);
                                mv.visitLabel(notNullLabel);
                                if (metadataProperty.getSimpleType() == String.class) {
                                    Label endStringComparatorTypeCheckLabel = new Label();
                                    mv.visitVarInsn(Opcodes.ALOAD, 2);
                                    ASMUtils.visitEnumLdc(mv, StringComparatorType.INSENSITIVE);
                                    mv.visitJumpInsn(Opcodes.IF_ACMPNE, endStringComparatorTypeCheckLabel);
                                    childScope.load("value");
                                    mv.visitJumpInsn(Opcodes.IFNULL, endStringComparatorTypeCheckLabel);
                                    childScope.load("value");
                                    mv.visitMethodInsn(
                                            Opcodes.INVOKEVIRTUAL, 
                                            "java/lang/String", 
                                            "toUpperCase", 
                                            "()Ljava/lang/String;", 
                                            false
                                    );
                                    childScope.store("value");
                                    mv.visitLabel(endStringComparatorTypeCheckLabel);
                                }
                                if (metadataProperty.getTargetClass() != null) {
                                    mv.visitFieldInsn(
                                            Opcodes.GETSTATIC, 
                                            this.getInternalName(), 
                                            Identifiers.embeddedEqualityComparatorStaticFieldName(metadataProperty), 
                                            ASMConstants.EQUALITY_COMPARATOR_DESCRIPTOR
                                    );
                                    childScope.load("value");
                                    mv.visitMethodInsn(
                                            Opcodes.INVOKEINTERFACE, 
                                            ASMConstants.EQUALITY_COMPARATOR_INTERNAL_NAME, 
                                            "hashCode", 
                                            "(Ljava/lang/Object;)I", 
                                            true
                                    );
                                } else {
                                    childScope.load("value");
                                    mv.visitMethodInsn(
                                            Opcodes.INVOKEVIRTUAL, 
                                            "java/lang/Object", 
                                            "hashCode", 
                                            "()I", 
                                            false
                                    );
                                }
                                mv.visitLabel(endObjectLabel);
                                mv.visitInsn(Opcodes.IRETURN);
                            }
                            break;
                        }
                    }
                }
                
                mv.visitLabel(defaultLabel);
            }
            
            ASMUtils.visitClassLdc(mv, this.getMetadataClass().getDescriptor());
            scope.load("scalarPropertyId");
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                    "hashCodeNonExisting", 
                    "(Ljava/lang/Class;I)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                    false
            );
            mv.visitInsn(Opcodes.ATHROW);
        }
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    final void generateEqualsScalarMethod(ClassVisitor cv, boolean itf) {
        
        List<MetadataProperty> propertyList = this.getMetadataClass().getPropertyList();    
        
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                "equalsScalar", 
                "(I" + 
                ASMConstants.STRING_COMPARATOR_TYPE_DESCRIPTOR + 
                ASMConstants.OBJECT_MODEL_DESCRIPTOR +  
                ")Z", 
                null,
                null
        );
        mv.visitCode();
        
        try (VariableScope scope = 
                new VariableScopeBuilder()
                .parameter("this", this.getDescriptor())
                .parameter("scalarPropertyId", "I")
                .parameter("stringComparatorType", ASMConstants.STRING_COMPARATOR_TYPE_DESCRIPTOR)
                .parameter("other", ASMConstants.OBJECT_MODEL_DESCRIPTOR)
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
                                "equalsNonScalar", 
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
                                "equalsArray", 
                                "(Ljava/lang/Class;ILjava/lang/String;)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                                false
                        );
                        mv.visitInsn(Opcodes.ATHROW);
                    } else if (metadataProperty.getDescriptor().charAt(0) == 'L') {
                        try (VariableScope childScope = scope.createSubScope()) {
                            childScope.declare("left", metadataProperty.getDescriptor(), null);
                            childScope.declare("right", metadataProperty.getDescriptor(), null);
                            Label leftIsNotNullLabel = new Label();
                            Label rightIsNotNullWhenLeftIsNullLabel = new Label();
                            Label endNestedIfLabel = new Label();
                            
                            scope.load("this");
                            mv.visitMethodInsn(
                                    itf ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, 
                                    this.getInternalName(), 
                                    Identifiers.getterName(metadataProperty), 
                                    "()" + metadataProperty.getDescriptor(), 
                                    itf
                            );
                            childScope.store("left");
                            
                            scope.load("other");
                            mv.visitTypeInsn(Opcodes.CHECKCAST, this.getInternalName());
                            mv.visitMethodInsn(
                                    itf ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, 
                                    this.getInternalName(), 
                                    Identifiers.getterName(metadataProperty), 
                                    "()" + metadataProperty.getDescriptor(), 
                                    itf
                            );
                            childScope.store("right");
                            
                            childScope.load("left");
                            mv.visitJumpInsn(Opcodes.IFNONNULL, leftIsNotNullLabel);
                            childScope.load("right");
                            mv.visitJumpInsn(Opcodes.IFNONNULL, rightIsNotNullWhenLeftIsNullLabel);
                            mv.visitInsn(Opcodes.ICONST_1);
                            mv.visitJumpInsn(Opcodes.GOTO, endNestedIfLabel);
                            mv.visitLabel(rightIsNotNullWhenLeftIsNullLabel);
                            mv.visitInsn(Opcodes.ICONST_0);
                            mv.visitLabel(endNestedIfLabel);
                            mv.visitInsn(Opcodes.IRETURN);
                            
                            mv.visitLabel(leftIsNotNullLabel);
                            if (metadataProperty.getSimpleType() == String.class) {
                                Label finishedStringComparatorTypeCheckLabel = new Label();
                                Label rightIsNotNullWhenLeftIsNotNullLabel = new Label();
                                scope.load("stringComparatorType");
                                ASMUtils.visitEnumLdc(mv, StringComparatorType.INSENSITIVE);
                                mv.visitJumpInsn(Opcodes.IF_ACMPNE, finishedStringComparatorTypeCheckLabel);
                                childScope.load("right");
                                mv.visitJumpInsn(Opcodes.IFNONNULL, rightIsNotNullWhenLeftIsNotNullLabel);
                                mv.visitInsn(Opcodes.ICONST_0);
                                mv.visitInsn(Opcodes.IRETURN);
                                mv.visitLabel(rightIsNotNullWhenLeftIsNotNullLabel);
                                childScope.load("left");
                                mv.visitMethodInsn(
                                        Opcodes.INVOKEVIRTUAL, 
                                        "java/lang/String", 
                                        "toUpperCase", 
                                        "()Ljava/lang/String;", 
                                        false
                                );
                                childScope.store("left");
                                childScope.load("right");
                                mv.visitMethodInsn(
                                        Opcodes.INVOKEVIRTUAL, 
                                        "java/lang/String", 
                                        "toUpperCase", 
                                        "()Ljava/lang/String;", 
                                        false
                                );
                                childScope.store("right");
                                mv.visitLabel(finishedStringComparatorTypeCheckLabel);
                            }
                            
                            if (metadataProperty.getTargetClass() != null) {
                                mv.visitFieldInsn(
                                        Opcodes.GETSTATIC, 
                                        this.getInternalName(), 
                                        Identifiers.embeddedEqualityComparatorStaticFieldName(metadataProperty), 
                                        ASMConstants.EQUALITY_COMPARATOR_DESCRIPTOR
                                );
                                childScope.load("left");
                                childScope.load("right");
                                mv.visitMethodInsn(
                                        Opcodes.INVOKEINTERFACE, 
                                        ASMConstants.EQUALITY_COMPARATOR_INTERNAL_NAME, 
                                        "equals", 
                                        "(Ljava/lang/Object;Ljava/lang/Object;)Z", 
                                        true
                                );
                            } else {
                                childScope.load("left");
                                childScope.load("right");
                                mv.visitMethodInsn(
                                        Opcodes.INVOKEVIRTUAL, 
                                        "java/lang/Object", 
                                        "equals", 
                                        "(Ljava/lang/Object;)Z", 
                                        false
                                );
                            }
                            mv.visitInsn(Opcodes.IRETURN);
                        }
                    } else {
                        scope.load("this");
                        mv.visitMethodInsn(
                                itf ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, 
                                this.getInternalName(), 
                                Identifiers.getterName(metadataProperty), 
                                "()" + metadataProperty.getDescriptor(), 
                                itf
                        );
                        scope.load("other");
                        mv.visitTypeInsn(Opcodes.CHECKCAST, this.getInternalName());
                        mv.visitMethodInsn(
                                itf ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, 
                                this.getInternalName(), 
                                Identifiers.getterName(metadataProperty), 
                                "()" + metadataProperty.getDescriptor(), 
                                itf
                        );
                        Label falseLabel = new Label();
                        Label endLabel = new Label();
                        switch (metadataProperty.getDescriptor().charAt(0)) {
                        case 'Z':
                        case 'C':
                        case 'B':
                        case 'S':
                        case 'I':
                            mv.visitJumpInsn(Opcodes.IF_ICMPNE, falseLabel);
                            mv.visitInsn(Opcodes.ICONST_1);
                            mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                            mv.visitLabel(falseLabel);
                            mv.visitInsn(Opcodes.ICONST_0);
                            mv.visitLabel(endLabel);
                            mv.visitInsn(Opcodes.IRETURN);
                            break;
                        case 'F':
                            mv.visitInsn(Opcodes.FCMPL);
                            mv.visitJumpInsn(Opcodes.IFNE, falseLabel);
                            mv.visitInsn(Opcodes.ICONST_1);
                            mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                            mv.visitLabel(falseLabel);
                            mv.visitInsn(Opcodes.ICONST_0);
                            mv.visitLabel(endLabel);
                            mv.visitInsn(Opcodes.IRETURN);
                            break;
                        case 'D':
                            mv.visitInsn(Opcodes.DCMPL);
                            mv.visitJumpInsn(Opcodes.IFNE, falseLabel);
                            mv.visitInsn(Opcodes.ICONST_1);
                            mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                            mv.visitLabel(falseLabel);
                            mv.visitInsn(Opcodes.ICONST_0);
                            mv.visitLabel(endLabel);
                            mv.visitInsn(Opcodes.IRETURN);
                            break;
                        case 'J':
                            mv.visitInsn(Opcodes.LCMP);
                            mv.visitJumpInsn(Opcodes.IFNE, falseLabel);
                            mv.visitInsn(Opcodes.ICONST_1);
                            mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                            mv.visitLabel(falseLabel);
                            mv.visitInsn(Opcodes.ICONST_0);
                            mv.visitLabel(endLabel);
                            mv.visitInsn(Opcodes.IRETURN);
                            break;
                        default:
                            throw new AssertionError("Internal bug");
                        }
                    }
                }
                
                mv.visitLabel(defaultLabel);
            }
            
            ASMUtils.visitClassLdc(mv, this.getMetadataClass().getDescriptor());
            scope.load("scalarPropertyId");
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                    "equalsNonExisting", 
                    "(Ljava/lang/Class;I)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                    false
            );
            mv.visitInsn(Opcodes.ATHROW);
        }
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    final void generateCompareScalarMethod(ClassVisitor cv, boolean itf) {
        
        List<MetadataProperty> propertyList = this.getMetadataClass().getPropertyList();    
        
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                "compareScalar", 
                "(I" +  
                ASMConstants.STRING_COMPARATOR_TYPE_DESCRIPTOR + 
                ASMConstants.NULL_COMPARATOR_TYPE_DESCRIPTOR + 
                ASMConstants.OBJECT_MODEL_DESCRIPTOR + 
                ")I", 
                null,
                null
        );
        mv.visitCode();
        
        try (VariableScope scope = 
                new VariableScopeBuilder()
                .parameter("this", this.getDescriptor())
                .parameter("scalarPropertyId", "I")
                .parameter("stringComparatorType", ASMConstants.STRING_COMPARATOR_TYPE_DESCRIPTOR)
                .parameter("nullComparatorType", ASMConstants.NULL_COMPARATOR_TYPE_DESCRIPTOR)
                .parameter("other", ASMConstants.OBJECT_MODEL_DESCRIPTOR)
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
                                "compareNonScalar", 
                                "(Ljava/lang/Class;ILjava/lang/String;)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                                false
                        );
                        mv.visitInsn(Opcodes.ATHROW);
                    } else {
                        char descChar = metadataProperty.getDescriptor().charAt(0);
                        if (descChar == '[') {
                            ASMUtils.visitClassLdc(mv, metadataProperty.getDeclaringClass().getDescriptor());
                            mv.visitLdcInsn(metadataProperty.getId());
                            mv.visitLdcInsn(metadataProperty.getName());
                            mv.visitMethodInsn(
                                    Opcodes.INVOKESTATIC, 
                                    ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                                    "compareArray", 
                                    "(Ljava/lang/Class;ILjava/lang/String;)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                                    false
                            );
                            mv.visitInsn(Opcodes.ATHROW);
                        } else if (descChar == 'L') {
                            try (VariableScope childScope = scope.createSubScope()) {
                                childScope.declare("left", metadataProperty.getDescriptor(), null);
                                childScope.declare("right", metadataProperty.getDescriptor(), null);
                                Label notSameLabel = new Label();
                                Label leftIsNotNullLabel = new Label();
                                Label rightIsNotNullLabel = new Label();
                                Label nullsLastWhenLeftIsNullLabel = new Label();
                                Label nullsLastWhenRightIsNullLabel = new Label();
                                Label returnWhenLeftIsNullLabel = new Label();
                                Label returnWhenRightIsNullLabel = new Label();
                                
                                scope.load("this");
                                mv.visitMethodInsn(
                                        itf ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, 
                                        this.getInternalName(), 
                                        Identifiers.getterName(metadataProperty), 
                                        "()" + metadataProperty.getDescriptor(), 
                                        itf
                                );
                                childScope.store("left");
                                
                                scope.load("other");
                                mv.visitTypeInsn(Opcodes.CHECKCAST, this.getInternalName());
                                mv.visitMethodInsn(
                                        itf ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, 
                                        this.getInternalName(), 
                                        Identifiers.getterName(metadataProperty), 
                                        "()" + metadataProperty.getDescriptor(), 
                                        itf
                                );
                                childScope.store("right");
                                
                                childScope.load("left");
                                childScope.load("right");
                                mv.visitJumpInsn(Opcodes.IF_ACMPNE, notSameLabel);
                                mv.visitInsn(Opcodes.ICONST_0);
                                mv.visitInsn(Opcodes.IRETURN);
                                mv.visitLabel(notSameLabel);
                                
                                childScope.load("left");
                                mv.visitJumpInsn(Opcodes.IFNONNULL, leftIsNotNullLabel);
                                scope.load("nullComparatorType");
                                ASMUtils.visitEnumLdc(mv, NullComparatorType.NULLS_LAST);
                                mv.visitJumpInsn(Opcodes.IF_ACMPEQ, nullsLastWhenLeftIsNullLabel);
                                mv.visitInsn(Opcodes.ICONST_M1);
                                mv.visitJumpInsn(Opcodes.GOTO, returnWhenLeftIsNullLabel);
                                mv.visitLabel(nullsLastWhenLeftIsNullLabel);
                                mv.visitInsn(Opcodes.ICONST_1);
                                mv.visitLabel(returnWhenLeftIsNullLabel);
                                mv.visitInsn(Opcodes.IRETURN);
                                mv.visitLabel(leftIsNotNullLabel);
                                
                                childScope.load("right");
                                mv.visitJumpInsn(Opcodes.IFNONNULL, rightIsNotNullLabel);
                                scope.load("nullComparatorType");
                                ASMUtils.visitEnumLdc(mv, NullComparatorType.NULLS_LAST);
                                mv.visitJumpInsn(Opcodes.IF_ACMPEQ, nullsLastWhenRightIsNullLabel);
                                mv.visitInsn(Opcodes.ICONST_1);
                                mv.visitJumpInsn(Opcodes.GOTO, returnWhenRightIsNullLabel);
                                mv.visitLabel(nullsLastWhenRightIsNullLabel);
                                mv.visitInsn(Opcodes.ICONST_M1);
                                mv.visitLabel(returnWhenRightIsNullLabel);
                                mv.visitInsn(Opcodes.IRETURN);
                                mv.visitLabel(rightIsNotNullLabel);
                                
                                if (metadataProperty.getSimpleType() == String.class) {
                                    Label finishInsensitiveLabel = new Label();
                                    scope.load("stringComparatorType");
                                    ASMUtils.visitEnumLdc(mv, StringComparatorType.INSENSITIVE);
                                    mv.visitJumpInsn(Opcodes.IF_ACMPNE, finishInsensitiveLabel);
                                    childScope.load("left");
                                    mv.visitMethodInsn(
                                            Opcodes.INVOKEVIRTUAL, 
                                            "java/lang/String", 
                                            "toUpperCase", 
                                            "()Ljava/lang/String;",
                                            false
                                    );
                                    childScope.store("left");
                                    childScope.load("right");
                                    mv.visitMethodInsn(
                                            Opcodes.INVOKEVIRTUAL, 
                                            "java/lang/String", 
                                            "toUpperCase", 
                                            "()Ljava/lang/String;",
                                            false
                                    );
                                    childScope.store("right");
                                    mv.visitLabel(finishInsensitiveLabel);
                                }
                                
                                if (metadataProperty.getTargetClass() != null) {
                                    mv.visitFieldInsn(
                                            Opcodes.GETSTATIC, 
                                            this.getInternalName(), 
                                            Identifiers.embeddedComparatorStaticFieldName(metadataProperty), 
                                            ASMConstants.COMPARATOR_DESCRIPTOR
                                    );
                                    childScope.load("left");
                                    childScope.load("right");
                                    mv.visitMethodInsn(
                                            Opcodes.INVOKEINTERFACE, 
                                            ASMConstants.COMPARATOR_INTERNAL_NAME, 
                                            "compare", 
                                            "(Ljava/lang/Object;Ljava/lang/Object;)I",
                                            true
                                    );
                                } else {
                                    childScope.load("left");
                                    if (metadataProperty.getSimpleType() == null) {
                                        mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Comparable");
                                    }
                                    childScope.load("right");
                                    mv.visitMethodInsn(
                                            Opcodes.INVOKEINTERFACE, 
                                            "java/lang/Comparable", 
                                            "compareTo", 
                                            "(Ljava/lang/Object;)I", 
                                            true
                                    );
                                }
                                mv.visitInsn(Opcodes.IRETURN);
                            }
                        } else {
                            try (VariableScope childScope = scope.createSubScope()) {
                                
                                Label notEqLabel = new Label();
                                Label geLabel = new Label();
                                Label endLabel = new Label();
                                childScope.declare("left", metadataProperty.getDescriptor(), null);
                                childScope.declare("right", metadataProperty.getDescriptor(), null);
                                scope.load("this");
                                mv.visitMethodInsn(
                                        itf ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, 
                                        this.getInternalName(), 
                                        Identifiers.getterName(metadataProperty), 
                                        "()" + metadataProperty.getDescriptor(), 
                                        itf
                                );
                                childScope.store("left");
                                scope.load("other");
                                mv.visitTypeInsn(Opcodes.CHECKCAST, this.getInternalName());
                                mv.visitMethodInsn(
                                        itf ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, 
                                        this.getInternalName(), 
                                        Identifiers.getterName(metadataProperty), 
                                        "()" + metadataProperty.getDescriptor(), 
                                        itf
                                );
                                childScope.store("right");
                                
                                switch (descChar) {
                                case 'Z':
                                    childScope.load("left");
                                    childScope.load("right");
                                    mv.visitJumpInsn(Opcodes.IF_ICMPNE, notEqLabel);
                                    mv.visitInsn(Opcodes.ICONST_0);
                                    mv.visitInsn(Opcodes.IRETURN);
                                    mv.visitLabel(notEqLabel);
                                    childScope.load("left");
                                    mv.visitJumpInsn(Opcodes.IFNE, geLabel);
                                    mv.visitInsn(Opcodes.ICONST_M1);
                                    mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                                    mv.visitLabel(geLabel);
                                    mv.visitInsn(Opcodes.ICONST_1);
                                    mv.visitLabel(endLabel);
                                    mv.visitInsn(Opcodes.IRETURN);
                                    mv.visitEnd();
                                    break;
                                case 'C':
                                case 'B':
                                case 'S':
                                case 'I':
                                    childScope.load("left");
                                    childScope.load("right");
                                    mv.visitJumpInsn(Opcodes.IF_ICMPNE, notEqLabel);
                                    mv.visitInsn(Opcodes.ICONST_0);
                                    mv.visitInsn(Opcodes.IRETURN);
                                    mv.visitLabel(notEqLabel);
                                    childScope.load("left");
                                    childScope.load("right");
                                    mv.visitJumpInsn(Opcodes.IF_ICMPGE, geLabel);
                                    mv.visitInsn(Opcodes.ICONST_M1);
                                    mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                                    mv.visitLabel(geLabel);
                                    mv.visitInsn(Opcodes.ICONST_1);
                                    mv.visitLabel(endLabel);
                                    mv.visitInsn(Opcodes.IRETURN);
                                    break;
                                case 'J':
                                    childScope.load("left");
                                    childScope.load("right");
                                    mv.visitInsn(Opcodes.LCMP);
                                    mv.visitJumpInsn(Opcodes.IFNE, notEqLabel);
                                    mv.visitInsn(Opcodes.ICONST_0);
                                    mv.visitInsn(Opcodes.IRETURN);
                                    mv.visitLabel(notEqLabel);
                                    childScope.load("left");
                                    childScope.load("right");
                                    mv.visitInsn(Opcodes.LCMP);
                                    mv.visitJumpInsn(Opcodes.IFGE, geLabel);
                                    mv.visitInsn(Opcodes.ICONST_M1);
                                    mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                                    mv.visitLabel(geLabel);
                                    mv.visitInsn(Opcodes.ICONST_1);
                                    mv.visitLabel(endLabel);
                                    mv.visitInsn(Opcodes.IRETURN);
                                    break;
                                case 'F':
                                    childScope.load("left");
                                    childScope.load("right");
                                    mv.visitInsn(Opcodes.FCMPL);
                                    mv.visitJumpInsn(Opcodes.IFNE, notEqLabel);
                                    mv.visitInsn(Opcodes.ICONST_0);
                                    mv.visitInsn(Opcodes.IRETURN);
                                    mv.visitLabel(notEqLabel);
                                    childScope.load("left");
                                    childScope.load("right");
                                    mv.visitInsn(Opcodes.FCMPG);
                                    mv.visitJumpInsn(Opcodes.IFGE, geLabel);
                                    mv.visitInsn(Opcodes.ICONST_M1);
                                    mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                                    mv.visitLabel(geLabel);
                                    mv.visitInsn(Opcodes.ICONST_1);
                                    mv.visitLabel(endLabel);
                                    mv.visitInsn(Opcodes.IRETURN);
                                    break;
                                case 'D':
                                    childScope.load("left");
                                    childScope.load("right");
                                    mv.visitInsn(Opcodes.DCMPL);
                                    mv.visitJumpInsn(Opcodes.IFNE, notEqLabel);
                                    mv.visitInsn(Opcodes.ICONST_0);
                                    mv.visitInsn(Opcodes.IRETURN);
                                    mv.visitLabel(notEqLabel);
                                    childScope.load("left");
                                    childScope.load("right");
                                    mv.visitInsn(Opcodes.DCMPG);
                                    mv.visitJumpInsn(Opcodes.IFGE, geLabel);
                                    mv.visitInsn(Opcodes.ICONST_M1);
                                    mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                                    mv.visitLabel(geLabel);
                                    mv.visitInsn(Opcodes.ICONST_1);
                                    mv.visitLabel(endLabel);
                                    mv.visitInsn(Opcodes.IRETURN);
                                    break;
                                default:
                                    throw new AssertionError("Internal bug");
                                }
                            }
                        }
                    }
                }
                
                mv.visitLabel(defaultLabel);
            }
            
            ASMUtils.visitClassLdc(mv, this.getMetadataClass().getDescriptor());
            scope.load("scalarPropertyId");
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                    "equalsNonExisting", 
                    "(Ljava/lang/Class;I)" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                    false
            );
            mv.visitInsn(Opcodes.ATHROW);
        }
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    final void generateEmbededComparatorStaticFields(ClassVisitor cv) {
        for (MetadataClass embeddedMetadataClass : this.getEmbeddedMetadataClasses()) {
            cv
            .visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    Identifiers.embeddedComparatorStaticFieldName(embeddedMetadataClass), 
                    ASMConstants.COMPARATOR_DESCRIPTOR, 
                    null,
                    null
            )
            .visitEnd();
            cv
            .visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    Identifiers.embeddedEqualityComparatorStaticFieldName(embeddedMetadataClass), 
                    ASMConstants.EQUALITY_COMPARATOR_DESCRIPTOR, 
                    null,
                    null
            )
            .visitEnd();
        }
    }
    
    final void generateInitComparatorStaticFieldInsns(MethodVisitor mv) {
        for (MetadataClass embeddedMetadataClass : this.getEmbeddedMetadataClasses()) {
            ASMUtils.visitClassLdc(mv, embeddedMetadataClass.getDescriptor());
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.MODEL_CLASS_INTERNAL_NAME, 
                    "of", 
                    "(Ljava/lang/Class;)" + ASMConstants.MODEL_CLASS_DESCRIPTOR, 
                    false
            );
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASMConstants.MODEL_CLASS_INTERNAL_NAME, 
                    "getDefaultComparator", 
                    "()" + ASMConstants.COMPARATOR_DESCRIPTOR,
                    true
            );
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC, 
                    this.getInternalName(), 
                    Identifiers.embeddedComparatorStaticFieldName(embeddedMetadataClass), 
                    ASMConstants.COMPARATOR_DESCRIPTOR
            );
            
            ASMUtils.visitClassLdc(mv, embeddedMetadataClass.getDescriptor());
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.MODEL_CLASS_INTERNAL_NAME, 
                    "of", 
                    "(Ljava/lang/Class;)" + ASMConstants.MODEL_CLASS_DESCRIPTOR, 
                    false
            );
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASMConstants.MODEL_CLASS_INTERNAL_NAME, 
                    "getDefaultEqualityComparator", 
                    "()" + ASMConstants.EQUALITY_COMPARATOR_DESCRIPTOR,
                    true
            );
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC, 
                    this.getInternalName(), 
                    Identifiers.embeddedEqualityComparatorStaticFieldName(embeddedMetadataClass), 
                    ASMConstants.EQUALITY_COMPARATOR_DESCRIPTOR
            );
        }
    }
    
    private Collection<MetadataClass> getEmbeddedMetadataClasses() {
        Set<MetadataClass> embededdMetadataClasses = new LinkedHashSet<>(ReferenceEqualityComparator.getInstance());
        for (MetadataProperty declaredProperty : this.getMetadataClass().getDeclaredProperties().values()) {
            if (declaredProperty.getPropertyType() == PropertyType.SCALAR && declaredProperty.getTargetClass() != null) {
                boolean override = false;
                if (this.getMetadataClass().getSuperClass() != null) {
                    for (MetadataProperty superProperty : this.getMetadataClass().getSuperClass().getProperties().values()) {
                        if (superProperty.getTargetClass() == declaredProperty.getTargetClass()) {
                            override = true;
                            break;
                        }
                    }
                }
                if (!override) {
                    embededdMetadataClasses.add(declaredProperty.getTargetClass());
                }
            }
        }
        return embededdMetadataClasses;
    }
}
