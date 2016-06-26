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

import org.babyfish.data.event.AttributeScope;
import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.lang.bytecode.ScopedMethodVisitor;
import org.babyfish.lang.bytecode.ScopedMethodVisitorBuilder;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;

/**
 * @author Tao Chen
 */
class ScalarSetterGenerator {

    private MetadataProperty metadataProperty;
    
    private String internalName;
    
    private String stateFieldName;
    
    private String frozenContextFieldName;
    
    public ScalarSetterGenerator(String objectModelInternalName, MetadataProperty metadataProperty) {
        this.metadataProperty = metadataProperty;
        this.internalName = objectModelInternalName;
        this.stateFieldName = Identifiers.stateFieldName(metadataProperty);
        this.frozenContextFieldName = Identifiers.frozenContextFieldName(metadataProperty);
    }
    
    public final void generate(ClassVisitor cv) {
        ScopedMethodVisitorBuilder mvBuilder = 
                new ScopedMethodVisitorBuilder(
                        Opcodes.ACC_PUBLIC,
                        Identifiers.setterName(this.metadataProperty)
                );
        try (ScopedMethodVisitor mv =
                mvBuilder
                .self(this.metadataProperty.getDeclaringClass().getDescriptor())
                .parameter("value", this.metadataProperty.getDescriptor(), this.metadataProperty.getSignature())
                .build(cv)) {
            
            mv.visitCode();
            
            if (this.metadataProperty.getPropertyType() == PropertyType.SCALAR) {
                mv.declare(
                        "ctx", 
                        ASMConstants.FROZEN_CONTEXT_DESCRIPTOR, 
                        'L' + 
                        ASMConstants.FROZEN_CONTEXT_INTERNAL_NAME + 
                        '<' + this.metadataProperty.getDeclaringClass().getDescriptor() + ">;"
                );
                mv.declare(
                        "event", 
                        ASMConstants.SCALAR_EVENT_DESCRIPTOR, 
                        null
                );
                mv.declare(
                        "finalException", 
                        "Ljava/lang/Throwable;", 
                        null
                );
                mv.declare(
                        "scalarLoader", 
                        ASMConstants.SCALAR_LOADER_DESCRIPTOR, 
                        null
                );
                this.generateInsns(mv);
            } else {
                mv.visitTypeInsn(Opcodes.NEW, ASMConstants.UNSUPPORTED_OPERATION_EXCEPTION_INTERNAL_NAME);
                mv.visitInsn(Opcodes.DUP);
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL, 
                        ASMConstants.UNSUPPORTED_OPERATION_EXCEPTION_INTERNAL_NAME, 
                        "<init>", 
                        "()V", 
                        false
                );
                mv.visitInsn(Opcodes.ATHROW);
            }
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private void generateInsns(ScopedMethodVisitor mv) {
        
        this.generateCloneArgumentInsns(mv);
        this.generateSetWhenLoadingInsns(mv);
        this.generateSetWhenUnloaded(mv);
        this.generateSetWhenExecutingInsns(mv);
        this.generateSetWhenNoDiffInsns(mv);
        
        this.generateLocalVariableInsns(mv);
        this.generateModifyingInsns(mv);
        this.generateModifyInsns(mv);
        this.generateModifiedInsns(mv);
        
        mv.load("finalException");
        Label fianlExceptionIsNullLabel = new Label();
        mv.visitJumpInsn(Opcodes.IFNULL, fianlExceptionIsNullLabel);
        mv.load("finalException");
        mv.visitInsn(Opcodes.ATHROW);
        mv.visitLabel(fianlExceptionIsNullLabel);
        
        mv.visitInsn(Opcodes.RETURN);
        
        Label endLocalVariableLabel = new Label();
        mv.visitLabel(endLocalVariableLabel);
    }
    
    private void generateCloneArgumentInsns(MethodVisitor mv) {
        if (this.metadataProperty.getSimpleType() != null && (
                Date.class.isAssignableFrom(this.metadataProperty.getSimpleType()) || 
                Calendar.class.isAssignableFrom(this.metadataProperty.getSimpleType()))) {
            Label clonedLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitJumpInsn(Opcodes.IFNULL, clonedLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    Type.getInternalName(this.metadataProperty.getSimpleType()), 
                    "clone", 
                    "()Ljava/lang/Object;", 
                    false
            );
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(this.metadataProperty.getSimpleType()));
            mv.visitVarInsn(Opcodes.ASTORE, 1);
            mv.visitLabel(clonedLabel);
        }
    }
    
    private void generateSetWhenLoadingInsns(ScopedMethodVisitor mv) {
        if (this.metadataProperty.isDeferrable()) {
            
            Label normalSettingLabel = new Label();
            
            /*
             * Do simple assignment when the current object model is loading.
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    ASMConstants.ABSTRACT_OBJECT_MODEL_IMPL_INTERNAL_NAME,
                    "loading", 
                    "Z");
            mv.visitJumpInsn(Opcodes.IFEQ, normalSettingLabel);
            this.generateScalarAssignmentInsns(mv, false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(normalSettingLabel);
        }
    }
    
    private void generateSetWhenUnloaded(ScopedMethodVisitor mv) {
        if (!this.metadataProperty.isDeferrable()) {
            return;
        }
        /*
         * Do simple assignment when the current object property is unloaded.
         * Of course, we can choose to load them at first, but that
         * may reduce the performance.
         * So I choose the special optimization, just do simple assignment
         * when the current property is unloaded, it does not matter that no
         * event is raised when assign a value to an unloaded scalar property.
         */
        Label loadedLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(
                Opcodes.GETFIELD, 
                this.internalName, 
                this.stateFieldName, 
                "I"
        );
        mv.visitInsn(ASMConstants.OP_CONST_UNLOADED);
        mv.visitInsn(Opcodes.IAND);
        mv.visitJumpInsn(Opcodes.IFEQ, loadedLabel);
        this.generateScalarAssignmentInsns(mv, true);
        mv.visitInsn(Opcodes.RETURN);
        
        mv.visitLabel(loadedLabel);
    }
    
    private void generateSetWhenExecutingInsns(MethodVisitor mv) {
        Label notDuringFireingLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(
                Opcodes.GETFIELD, 
                ASMConstants.ABSTRACT_OBJECT_MODEL_IMPL_INTERNAL_NAME, 
                "executing", 
                "Z"
        );
        mv.visitJumpInsn(Opcodes.IFEQ, notDuringFireingLabel);
        mv.visitLdcInsn(Type.getType(this.metadataProperty.getDeclaringClass().getDescriptor()));
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                ASMConstants.OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                "setDuringExecuting", 
                "(Ljava/lang/Class;)Ljava/lang/RuntimeException;",
                false
        );
        mv.visitInsn(Opcodes.ATHROW);
        mv.visitLabel(notDuringFireingLabel);
    }
    
    private void generateSetWhenNoDiffInsns(MethodVisitor mv) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(
                Opcodes.GETFIELD, 
                this.internalName, 
                this.metadataProperty.getName(), 
                this.metadataProperty.getDescriptor()
        );
        mv.visitVarInsn(
                ASMUtils.getLoadCode(this.metadataProperty.getDescriptor()), 
                1
        );
        ASMUtils.visitEquals(
                mv, 
                this.metadataProperty.getDescriptor(), 
                true, 
                v ->{
                    ScalarSetterGenerator.this.generateScalarStateCleanInsns(v);
                    v.visitInsn(Opcodes.RETURN);
                }
        );
    }
    
    private void generateLocalVariableInsns(ScopedMethodVisitor mv) {
        /*
         * FrozenContext<?> ctx = this.?frozenContext;
         * ScalarEvent event = ScalarEvent.createReplaceEvent(
         *      this,
         *      ObjectModelModifications.setScalar(ldc:propertyId, ?),
         *      this.?,
         *      ?);
         * Throwable finalException = null;
         */
        if (this.frozenContextFieldName != null) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    this.frozenContextFieldName, 
                    ASMConstants.FROZEN_CONTEXT_DESCRIPTOR
            );
            mv.store("ctx");
        }
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitLdcInsn(this.metadataProperty.getId());
        ASMUtils.visitBox(
                mv,
                this.metadataProperty.getDescriptor(), 
                v -> {
                    v.visitVarInsn(ASMUtils.getLoadCode(ScalarSetterGenerator.this.metadataProperty.getDescriptor()), 1);
                });
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                ASMConstants.OBJECT_MODEL_MODIFICATIONS_INTERNAL_NAME, 
                "set", 
                "(ILjava/lang/Object;)" + 
                ASMConstants.SET_BY_SCALAR_PROPERTY_ID_AND_VALUE_DESCRIPTOR,
                false);
        mv.visitLdcInsn(metadataProperty.getId());
        ASMUtils.visitBox(
                mv,
                this.metadataProperty.getDescriptor(), 
                v -> {
                    v.visitVarInsn(Opcodes.ALOAD, 0);
                    v.visitFieldInsn(
                            Opcodes.GETFIELD, 
                            ScalarSetterGenerator.this.internalName,
                            ScalarSetterGenerator.this.metadataProperty.getName(), 
                            ScalarSetterGenerator.this.metadataProperty.getDescriptor()
                    );
                });
        ASMUtils.visitBox(
                mv,
                this.metadataProperty.getDescriptor(), 
                v -> {
                    v.visitVarInsn(
                            ASMUtils.getLoadCode(
                                    ScalarSetterGenerator.this.metadataProperty.getDescriptor()
                            ), 
                            1
                    );
                });
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                ASMConstants.SCALAR_EVENT_INTERNAL_NAME, 
                "createReplaceEvent", 
                "(Ljava/lang/Object;" +
                ASMConstants.SET_BY_SCALAR_PROPERTY_ID_AND_VALUE_DESCRIPTOR +
                "ILjava/lang/Object;Ljava/lang/Object;)" +
                ASMConstants.SCALAR_EVENT_DESCRIPTOR,
                false);
        mv.store("event");
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.store("finalException");
    }
    
    private void generateModifyingInsns(ScopedMethodVisitor mv) {
        /*
         * try {
         *      this.{executeModifying}(event);
         * } catch (Throwable ex) {
         *      if (finalException == null) {
         *          finalException = ex;
         *      }
         *      ((InAllChainAttributeContext)event.getAttributeContext(AttributeScope.IN_ALL_CHAIN))
         *      .setPreThrowable(ex);
         * }
         */
        mv.visitTryCatch(
                v -> {
                    v.visitVarInsn(Opcodes.ALOAD, 0);
                    v.load("event");
                    v.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASMConstants.ABSTRACT_OBJECT_MODEL_IMPL_INTERNAL_NAME, 
                        "executeModifying", 
                        '(' + ASMConstants.SCALAR_EVENT_DESCRIPTOR + ")V",
                        false);
                }, 
                new ScopedMethodVisitor.Catch(
                        v -> {
                            v.load("ex");
                            v.store("finalException");
                            v.load("event");
                            ASMUtils.visitEnumLdc(v, AttributeScope.IN_ALL_CHAIN);
                            v.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL, 
                                    ASMConstants.MODIFICATION_EVENT_INTERNAL_NAME, 
                                    "getAttributeContext", 
                                    '(' + 
                                    ASMConstants.ATTRIBUTE_SCOPE_DESCRIPTOR + 
                                    ')' + 
                                    ASMConstants.EVENT_ATTRIBUTE_CONTEXT_DESCRIPTOR,
                                    false);
                            v.visitTypeInsn(Opcodes.CHECKCAST, ASMConstants.IN_ALL_CHAIN_ATTRIBUTE_CONTEXT_INTERNAL_NAME);
                            v.load("ex");
                            v.visitMethodInsn(
                                    Opcodes.INVOKEINTERFACE,
                                    ASMConstants.IN_ALL_CHAIN_ATTRIBUTE_CONTEXT_INTERNAL_NAME,
                                    "setPreThrowable",
                                    "(Ljava/lang/Throwable;)V",
                                    true);
                })
        );
    }
    
    private void generateModifyInsns(ScopedMethodVisitor mv) {
        /*
         * if (finalException == null) {
         *     try {
         *        << 
         *            suspendFreezing 
         *         try {
         *            changeScalar
         *            clearScalarSate
         *            setDirtyForScalarLoader
         *         } finally {
         *            resumeFreezing
         *         }
         *         >>
         *          ((GlobalAttributeContext)event.getAttributeContext(AttributeScope.GLOBAL)).success();
         *     } catch (Throwable ex) {
         *          if (finalException == null) {
         *              finalException = ex;
         *          }
         *          ((GlobalAttributeContext)event.getAttributeContext(AttributeScope.GLOBAL)).setThrowable(ex);
         *     }
         * }
         */
        Label finihsedLabel = new Label();
        mv.load("finalException");
        mv.visitJumpInsn(Opcodes.IFNONNULL, finihsedLabel);
        mv.visitTryCatch(
                v -> {
                    ScalarSetterGenerator.this.generateScalarChaningWithFrozenSuspendedScopeInsns(v);
                    v.load("event");
                    ASMUtils.visitEnumLdc(v, AttributeScope.GLOBAL);
                    v.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            ASMConstants.MODIFICATION_EVENT_INTERNAL_NAME, 
                            "getAttributeContext", 
                            '(' + 
                            ASMConstants.ATTRIBUTE_SCOPE_DESCRIPTOR + 
                            ')' + 
                            ASMConstants.EVENT_ATTRIBUTE_CONTEXT_DESCRIPTOR,
                            false);
                    v.visitTypeInsn(Opcodes.CHECKCAST, ASMConstants.GLOBAL_ATTRIBUTE_CONTEXT_INTERNAL_NAME);
                    v.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE,
                            ASMConstants.GLOBAL_ATTRIBUTE_CONTEXT_INTERNAL_NAME,
                            "success",
                            "()V",
                            true);
                },  
                new ScopedMethodVisitor.Catch(
                        v -> {
                            setFinalException(v, "ex");
                            v.load("event");
                            ASMUtils.visitEnumLdc(v, AttributeScope.GLOBAL);
                            v.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL, 
                                    ASMConstants.MODIFICATION_EVENT_INTERNAL_NAME, 
                                    "getAttributeContext", 
                                    '(' + 
                                    ASMConstants.ATTRIBUTE_SCOPE_DESCRIPTOR + 
                                    ')' + 
                                    ASMConstants.EVENT_ATTRIBUTE_CONTEXT_DESCRIPTOR,
                                    false);
                            v.visitTypeInsn(Opcodes.CHECKCAST, ASMConstants.GLOBAL_ATTRIBUTE_CONTEXT_INTERNAL_NAME);
                            v.load("ex");
                            v.visitMethodInsn(
                                    Opcodes.INVOKEINTERFACE,
                                    ASMConstants.GLOBAL_ATTRIBUTE_CONTEXT_INTERNAL_NAME,
                                    "setThrowable",
                                    "(Ljava/lang/Throwable;)V",
                                    true);
                        }
                ));
        mv.visitLabel(finihsedLabel);
    }
    
    private void generateModifiedInsns(ScopedMethodVisitor mv) {
        /*
         * try {
         *      this.{executeModified}(event);
         * } catch (Throwable ex) {
         *      if (finalException == null) {
         *          finalException = ex;
         *      }
         * }
         */
        mv.visitTryCatch(
                v -> {
                    v.visitVarInsn(Opcodes.ALOAD, 0);
                    v.load("event");
                    v.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASMConstants.ABSTRACT_OBJECT_MODEL_IMPL_INTERNAL_NAME, 
                        "executeModified", 
                        '(' + ASMConstants.SCALAR_EVENT_DESCRIPTOR + ")V",
                        false);
                }, 
                new ScopedMethodVisitor.Catch(
                        v -> {
                            setFinalException(v, "ex");
                        })
                );
    }
    
    private void generateScalarChaningWithFrozenSuspendedScopeInsns(ScopedMethodVisitor mv) {
        if (this.frozenContextFieldName == null) {
            this.generateScalarAssignmentInsns(mv, true);
            return;
        }
        mv.load("ctx");
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(
                Opcodes.GETFIELD, 
                ASMConstants.ABSTRACT_OBJECT_MODEL_IMPL_INTERNAL_NAME, 
                "owner", 
                "Ljava/lang/Object;"
        );
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                ASMConstants.FROZEN_CONTEXT_INTERNAL_NAME, 
                "suspendFreezing", 
                '(' +ASMConstants.FROZEN_CONTEXT_DESCRIPTOR + "Ljava/lang/Object;)V", 
                false
        );
        mv.visitTryFinally(
                v -> {
                    ScalarSetterGenerator.this.generateScalarChaningUnderFrozenSuspendedScopeInsns(v);
                }, 
                v -> {
                    v.load("ctx");
                    v.visitMethodInsn(
                            Opcodes.INVOKESTATIC, 
                            ASMConstants.FROZEN_CONTEXT_INTERNAL_NAME, 
                            "resumeFreezing", 
                            '(' + ASMConstants.FROZEN_CONTEXT_DESCRIPTOR + ")V", 
                            false
                    );
                }
        );
    }
    
    private void generateScalarChaningUnderFrozenSuspendedScopeInsns(ScopedMethodVisitor mv) {
        if (this.metadataProperty.getTargetClass() == null) {
            this.generateScalarAssignmentInsns(mv, true);
            return;
        }
        
        mv.declare("embededScalarListener", ASMConstants.SCALAR_LISTENER_DESCRIPTOR, null);
        mv.declare("oldValue", this.metadataProperty.getDescriptor(), null);
        
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(
                Opcodes.GETFIELD, 
                this.internalName, 
                this.metadataProperty.getName(), 
                this.metadataProperty.getDescriptor()
        );
        mv.store("oldValue");
        
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
        mv.store("embededScalarListener");
        
        Label oldIsNotNullLabel = new Label();
        mv.load("oldValue");
        mv.visitJumpInsn(Opcodes.IFNULL, oldIsNotNullLabel);
        mv.load("oldValue");
        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE, 
                ASMConstants.OBJECT_MODEL_PROVIDER_INTERNAL_NAME, 
                "objectModel", 
                "()" + ASMConstants.OBJECT_MODEL_DESCRIPTOR, 
                true
        );
        mv.load("embededScalarListener");
        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE, 
                ASMConstants.OBJECT_MODEL_INTERNAL_NAME, 
                "removeScalarListener", 
                '(' + ASMConstants.SCALAR_LISTENER_DESCRIPTOR + ")V", 
                true
        );
        mv.visitLabel(oldIsNotNullLabel);
        
        this.generateScalarAssignmentInsns(mv, true);
        
        Label newIsNotNullLabel = new Label();
        mv.load("value");
        mv.visitJumpInsn(Opcodes.IFNULL, newIsNotNullLabel);
        mv.load("value");
        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE, 
                ASMConstants.OBJECT_MODEL_PROVIDER_INTERNAL_NAME, 
                "objectModel", 
                "()" + ASMConstants.OBJECT_MODEL_DESCRIPTOR, 
                true
        );
        mv.load("embededScalarListener");
        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE, 
                ASMConstants.OBJECT_MODEL_INTERNAL_NAME, 
                "addScalarListener", 
                '(' + ASMConstants.SCALAR_LISTENER_DESCRIPTOR + ")V", 
                true
        );
        mv.visitLabel(newIsNotNullLabel);
    }
    
    private void generateScalarAssignmentInsns(ScopedMethodVisitor mv, boolean makeDirty) {
        
        /*
         * this.? = ?;
         */
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(ASMUtils.getLoadCode(this.metadataProperty.getDescriptor()), 1);
        mv.visitFieldInsn(
                Opcodes.PUTFIELD, 
                this.internalName, 
                this.metadataProperty.getName(), 
                this.metadataProperty.getDescriptor());
        
        /*
         * this.?{state} = 0;
         */
        this.generateScalarStateCleanInsns(mv);
        
        if (makeDirty) {
            /*
             * ObjectModelLoader scalarLoader = this.scalarLoader;
             * if (scalarLoader instanceof ObjectModelLoaderDirtinessAware) {
             *      ((ObjectModelLoaderDirtinessAware)scalarLoader).dirty();
             * }
             */
            Label nonDirtinessAwareLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    ASMConstants.ABSTRACT_OBJECT_MODEL_IMPL_INTERNAL_NAME, 
                    "scalarLoader", 
                    ASMConstants.SCALAR_LOADER_DESCRIPTOR
            );
            mv.store("scalarLoader");
            mv.load("scalarLoader");
            mv.visitTypeInsn(Opcodes.INSTANCEOF, ASMConstants.DIRTY_AWARE_LOADER_INTERNAL_NAME);
            mv.visitJumpInsn(Opcodes.IFEQ, nonDirtinessAwareLabel);
            mv.load("scalarLoader");
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASMConstants.DIRTY_AWARE_LOADER_INTERNAL_NAME);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASMConstants.DIRTY_AWARE_LOADER_INTERNAL_NAME, 
                    "dirty", 
                    "()V",
                    true);
            mv.visitLabel(nonDirtinessAwareLabel);
        }
    }
    
    private void generateScalarStateCleanInsns(MethodVisitor mv) {
        if (this.stateFieldName == null) {
            return;
        }
        
        /*
         * this.?{state} = 0;
         */
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitFieldInsn(
                Opcodes.PUTFIELD, 
                this.internalName, 
                this.stateFieldName, 
                "I"
        );
    }
    
    private static void setFinalException(ScopedMethodVisitor mv, String exName) {
        Label notNullLabel = new Label();
        mv.load("finalException");
        mv.visitJumpInsn(Opcodes.IFNONNULL, notNullLabel);
        mv.load(exName);
        mv.store("finalException");
        mv.visitLabel(notNullLabel);
    }
}
