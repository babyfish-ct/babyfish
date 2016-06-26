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
package org.babyfish.lang.i18n.instrument;

import java.io.File;

import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.lang.bytecode.ScopedMethodVisitor;
import org.babyfish.lang.bytecode.ScopedMethodVisitorBuilder;
import org.babyfish.lang.i18n.metadata.MetadataClass;
import org.babyfish.lang.i18n.metadata.MetadataMethod;
import org.babyfish.lang.instrument.bytecode.Replacer;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;

/**
 * @author Tao Chen
 */
public class TypedI18NReplacer extends Replacer {

    private MetadataClass metadataClass;
    
    protected TypedI18NReplacer(
            TypedI18NInstrumenter instrumenter,
            String className, 
            File classFile) {
        super(instrumenter, className, classFile);
        this.metadataClass = instrumenter.getMetadataClass(className);
    }

    @Override
    protected ClassVisitor onCreateClassAdapter(ClassVisitor cv) {
        return this.new ClassAdapter(cv);
    }

    private class ClassAdapter extends ClassVisitor {
        
        public ClassAdapter(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this
            .cv
            .visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, 
                    Identifiers.RESOURCE_BUNDLE_FILED_NAME, 
                    ASMConstants.RESOURCE_BUNDLE_DESCRIPTOR, 
                    null,
                    null
            )
            .visitEnd();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MetadataMethod metadataMethod = TypedI18NReplacer.this.metadataClass.getDeclaredMethod(name, desc);
            if (metadataMethod == null) {
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
            ScopedMethodVisitorBuilder builder = 
                    new ScopedMethodVisitorBuilder(access & ~Opcodes.ACC_NATIVE, name)
                    .output("Ljava/lang/String;");
            int parameterCount = metadataMethod.getParameterCount();
            for (int i = 0; i < parameterCount; i++) {
                builder.parameter(
                        metadataMethod.getParameterName(i),
                        metadataMethod.getParameterDescriptor(i)
                );
            }
            try (ScopedMethodVisitor mv = builder.build(this.cv)) {
                mv.visitCode();
                this.generateI18NInsns(mv, metadataMethod);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
            return null;
        }
        
        private void generateI18NInsns(ScopedMethodVisitor mv, MetadataMethod metadataMethod) {
            String internalName = TypedI18NReplacer.this.getInternalName();
            Label bundleIsLoadedLabel = new Label();
            
            mv.declare("bundle", ASMConstants.RESOURCE_BUNDLE_DESCRIPTOR);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    internalName, 
                    Identifiers.RESOURCE_BUNDLE_FILED_NAME, 
                    ASMConstants.RESOURCE_BUNDLE_DESCRIPTOR
            );
            mv.store("bundle");
            
            mv.load("bundle");
            mv.visitJumpInsn(Opcodes.IFNONNULL, bundleIsLoadedLabel);
            mv.visitLdcInsn(internalName);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.RESOURCE_BUNDLE_INTERNAL_NAME, 
                    "getBundle", 
                    "(Ljava/lang/String;)" + ASMConstants.RESOURCE_BUNDLE_DESCRIPTOR, 
                    false
            );
            mv.store("bundle");
            mv.load("bundle");
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC, 
                    internalName, 
                    Identifiers.RESOURCE_BUNDLE_FILED_NAME, 
                    ASMConstants.RESOURCE_BUNDLE_DESCRIPTOR
            );
            mv.visitLabel(bundleIsLoadedLabel);
            mv.visitFrame(
                    Opcodes.F_APPEND, 
                    1, 
                    new Object[] { ASMConstants.RESOURCE_BUNDLE_INTERNAL_NAME },
                    0,
                    null
            );
            
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    internalName, 
                    Identifiers.RESOURCE_BUNDLE_FILED_NAME, 
                    ASMConstants.RESOURCE_BUNDLE_DESCRIPTOR
            );
            mv.visitLdcInsn(metadataMethod.getName());
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ASMConstants.RESOURCE_BUNDLE_INTERNAL_NAME, 
                    "getString", 
                    "(Ljava/lang/String;)Ljava/lang/String;", 
                    false
            );
            int parameterCount = metadataMethod.getParameterCount();
            if (parameterCount != 0) {
                int slot = 0;
                for (int i = 0; i < parameterCount; i++) {
                    mv.visitLdcInsn("{" + i + "}");
                    String pDesc = metadataMethod.getParameterDescriptor(i);
                    String boxTypeInternalName;
                    switch (pDesc) {
                    case "Z":
                        boxTypeInternalName = "java/lang/Boolean";
                        break;
                    case "C":
                        boxTypeInternalName = "java/lang/Character";
                        break;
                    case "B":
                        boxTypeInternalName = "java/lang/Byte";
                        break;
                    case "S":
                        boxTypeInternalName = "java/lang/Short";
                        break;
                    case "I":
                        boxTypeInternalName = "java/lang/Integer";
                        break;
                    case "J":
                        boxTypeInternalName = "java/lang/Long";
                        break;
                    case "F":
                        boxTypeInternalName = "java/lang/Float";
                        break;
                    case "D":
                        boxTypeInternalName = "java/lang/Double";
                        break;
                    default:
                        boxTypeInternalName = null;
                        break;
                    }
                    if (boxTypeInternalName != null) {
                        mv.visitVarInsn(ASMUtils.getLoadCode(pDesc), slot);
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC, 
                                boxTypeInternalName, 
                                "toString", 
                                '(' + pDesc + ")Ljava/lang/String;", 
                                false
                        );
                    } else {
                        mv.visitVarInsn(Opcodes.ALOAD, slot);
                        mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC, 
                                ASMConstants.I18N_INTERNAL_NAME, 
                                "toString", 
                                "(Ljava/lang/Object;)Ljava/lang/String;", 
                                false
                        );
                    }
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            "java/lang/String", 
                            "replace", 
                            '(' +
                            ASMConstants.CHAR_SEQUENCE_DESCRIPTOR +
                            ASMConstants.CHAR_SEQUENCE_DESCRIPTOR +
                            ")Ljava/lang/String;", 
                            false
                    );
                    slot += ASMUtils.getSlotCount(pDesc);
                }
            }
            mv.visitInsn(Opcodes.ARETURN);
        }
    }
}
