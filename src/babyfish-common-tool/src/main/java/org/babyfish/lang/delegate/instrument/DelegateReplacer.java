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
package org.babyfish.lang.delegate.instrument;

import java.io.File;

import org.babyfish.lang.bytecode.ScopedMethodVisitor;
import org.babyfish.lang.bytecode.ScopedMethodVisitorBuilder;
import org.babyfish.lang.delegate.metadata.MetadataClass;
import org.babyfish.lang.instrument.bytecode.Replacer;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;

/**
 * @author Tao Chen
 */
class DelegateReplacer extends Replacer {
    
    private MetadataClass metadataClass;

    DelegateReplacer(
            DelegateInstrumenter instrumenter, 
            String className, 
            File classFile) {
        super(instrumenter, className, classFile);
        this.metadataClass = instrumenter.getMetadataClass(className);
        new CombinedDelegateGenerator(this);
    }
    
    public MetadataClass getMetadataClass() {
        return this.metadataClass;
    }

    @Override
    protected ClassVisitor onCreateClassAdapter(ClassVisitor cv) {
        return this.new ClassAdapter(cv);
    }
    
    private void generateChainMethod(ClassVisitor cv, String methodName) {
        
        String argumentDesc = this.metadataClass.getDescriptor();
        String argumentSignature = null;
        if (this.metadataClass.getTypeArgumentClause() != null) {
            argumentSignature = 
                    'L' + 
                    this.metadataClass.getInternalName() + 
                    this.metadataClass.getTypeArgumentClause() + 
                    ';';
        }
        
        try (ScopedMethodVisitor mv = 
                new ScopedMethodVisitorBuilder(
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, 
                        methodName
                )
                .typeParameterSignatureClause(this.metadataClass.getTypeParameterClause())
                .parameter("a", argumentDesc, argumentSignature)
                .parameter("b", argumentDesc, argumentSignature)
                .output(argumentDesc, argumentSignature)
                .build(cv)) {
            
            mv.visitCode();
            
            mv.load("a");
            mv.load("b");
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    this.getInternalName() + '$' + Identifiers.COMBINED_SIMPLE_NAME, 
                    methodName, 
                    '(' +
                    this.getDescriptor() +
                    this.getDescriptor() +
                    ')' +
                    this.getDescriptor(),
                    false
            );
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private class ClassAdapter extends ClassVisitor {

        private String chainMethodDesc;
        
        public ClassAdapter(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
            String argumentDesc = DelegateReplacer.this.metadataClass.getDescriptor();
            this.chainMethodDesc = '(' + argumentDesc + argumentDesc + ')' + argumentDesc;
        }

        @Override
        public MethodVisitor visitMethod(
                int access, 
                String name, 
                String desc, 
                String signature, 
                String[] exceptions) {
            if ((access & Opcodes.ACC_STATIC) != 0 &&
                    desc.equals(this.chainMethodDesc) &&
                    (name.equals("combine") || (name.equals("remove")))) {
                DelegateReplacer.this.generateChainMethod(this.cv, name);
                return null;
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }
}
