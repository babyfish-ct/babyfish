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

import java.util.function.Consumer;

import org.babyfish.lang.DelegateExceptionHandlingType;
import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.lang.bytecode.ScopedMethodVisitor;
import org.babyfish.lang.bytecode.ScopedMethodVisitor.Catch;
import org.babyfish.lang.bytecode.ScopedMethodVisitorBuilder;
import org.babyfish.lang.delegate.metadata.MetadataClass;
import org.babyfish.lang.instrument.bytecode.NestedClassGenerator;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.org.objectweb.asm.tree.MethodNode;

/**
 * @author Tao Chen
 */
class CombinedDelegateGenerator extends NestedClassGenerator {

    private MetadataClass metadataClass;
    
    protected CombinedDelegateGenerator(DelegateReplacer parent) {
        super(parent, Identifiers.COMBINED_SIMPLE_NAME);
        this.metadataClass = parent.getMetadataClass();
    }

    @Override
    protected int determineAccess() {
        return Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
    }

    @Override
    protected void generate(ClassVisitor cv) {
        String signature = null;
        if (this.metadataClass.getTypeParameterClause() != null) {
            signature = 
                    this.metadataClass.getTypeParameterClause() +
                    ASMConstants.ABSTRACT_COMBINED_DELEGATE_DESCRIPTOR +
                    'L' + 
                    this.metadataClass.getInternalName() +
                    this.metadataClass.getTypeArgumentClause() +
                    ';';
        }
        cv.visit(
                this.metadataClass.getBytecodeVersion(), 
                Opcodes.ACC_PRIVATE, 
                this.getInternalName(), 
                signature, 
                ASMConstants.ABSTRACT_COMBINED_DELEGATE_INTERNAL_NAME, 
                new String[] { this.metadataClass.getInternalName() }
        );
        this.generateMembers(cv);
        cv.visitEnd();
    }
    
    private void generateMembers(ClassVisitor cv) {
        cv.visitField(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                Identifiers.CREATOR_NAME, 
                ASMConstants.BI_FUNCTION_DESCRIPTOR, 
                'L' + 
                ASMConstants.BI_FUNCTION_INTERNAL_NAME + 
                "<[Ljava/lang/Object;Ljava/lang/Integer;" +
                ASMConstants.ABSTRACT_COMBINED_DELEGATE_DESCRIPTOR +
                ">;", 
                null
        )
        .visitEnd();
        this.generateConstructor(cv);
        this.metadataClass.consumeInstanceMethods(methodNode -> {
            this.generateInstanceMethod(cv, methodNode);
        });
        this.generateChainMethod(cv, "combine");
        this.generateChainMethod(cv, "remove");
        this.generateStaticConstructor(cv);
    }
    
    private void generateConstructor(ClassVisitor cv) {
        try (ScopedMethodVisitor mv = 
                new ScopedMethodVisitorBuilder(0, "<init>")
                .self(this.getDescriptor())
                .parameter("delegates", "[Ljava/lang/Object;")
                .parameter("delegateCount", "I")
                .build(cv)) {
            mv.visitCode();
            mv.load("this");
            mv.load("delegates");
            mv.load("delegateCount");
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    ASMConstants.ABSTRACT_COMBINED_DELEGATE_INTERNAL_NAME, 
                    "<init>", 
                    "([Ljava/lang/Object;I)V", 
                    false
            );
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private void generateInstanceMethod(ClassVisitor cv, MethodNode methodNode) {
        String[] exceptions = null;
        if (methodNode.exceptions != null) {
            exceptions = methodNode.exceptions.toArray(
                    new String[methodNode.exceptions.size()]
            );
        }
        try (ScopedMethodVisitor mv = ScopedMethodVisitorBuilder.build(
                cv, 
                this.getDescriptor(), 
                Opcodes.ACC_PUBLIC, 
                methodNode.name, 
                methodNode.desc, 
                methodNode.signature, 
                exceptions, 
                i -> "p" + (i + 1))) {
            
            mv.visitCode();
            
            mv.declare("arr", "[Ljava/lang/Object;");
            mv.declare("max", "I");
            mv.declare("exception", "Ljava/lang/Throwable;");
            mv.declare("index", "I");
            
            mv.load("this");
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    ASMConstants.ABSTRACT_COMBINED_DELEGATE_INTERNAL_NAME, 
                    "delegates", 
                    "[Ljava/lang/Object;"
            );
            mv.store("arr");
            
            mv.load("this");
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    ASMConstants.ABSTRACT_COMBINED_DELEGATE_INTERNAL_NAME, 
                    "delegateCount", 
                    "I"
            );
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.ISUB);
            mv.store("max");
            
            if (this.metadataClass.getExceptionHandlingType() == 
                    DelegateExceptionHandlingType.CONTINUE) {
                mv.visitInsn(Opcodes.ACONST_NULL);
                mv.store("exception");
            }
            
            mv.visitInsn(Opcodes.ICONST_0);
            mv.store("index");
            
            Label beginLoopLabel = new Label();
            Label endLoopLabel = new Label();
            
            mv.visitLabel(beginLoopLabel);
            this.visitNonLastDispatchInsn(mv, methodNode);
            mv.visitIincInsn(mv.slot("index"), 1);
            mv.load("index");
            mv.load("max");
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, endLoopLabel);
            mv.visitJumpInsn(Opcodes.GOTO, beginLoopLabel);
            mv.visitLabel(endLoopLabel);
            
            this.visitLastDispatchInsn(mv, methodNode);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private void visitNonLastDispatchInsn(ScopedMethodVisitor mv, MethodNode methodNode) {
        
        Consumer<ScopedMethodVisitor> insnLabmda = v -> {
            
            this.visitDispatchInvocationInsnImpl(v, methodNode);
            
            switch (methodNode.desc.substring(methodNode.desc.length() - 2)) {
            case ")V":
                break;
            case ")J":
            case ")D":
                v.visitInsn(Opcodes.POP2);
                break;
            default:
                v.visitInsn(Opcodes.POP);
            }
        };
        
        if (this.metadataClass.getExceptionHandlingType() == DelegateExceptionHandlingType.CONTINUE) {
            mv.visitTryCatch(insnLabmda, dispatchExceptionHandler());
        } else {
            insnLabmda.accept(mv);
        }
    }
    
    private void visitLastDispatchInsn(ScopedMethodVisitor mv, MethodNode methodNode) {
        
        String returnDescriptor = Type.getReturnType(methodNode.desc).getDescriptor();
        if (this.metadataClass.getExceptionHandlingType() == DelegateExceptionHandlingType.CONTINUE) {
            try (ScopedMethodVisitor subMv = mv.createSubScope()) {
                if (!methodNode.desc.endsWith(")V") &&
                        this.metadataClass.getExceptionHandlingType() ==
                        DelegateExceptionHandlingType.CONTINUE) {
                    subMv.declareImmediately(
                            "returnValue", 
                            Type.getReturnType(methodNode.desc).getDescriptor()
                    );
                    subMv.visitInsn(ASMUtils.getDefaultCode(returnDescriptor));
                    subMv.store("returnValue");
                }
                boolean hasReturnValue = !returnDescriptor.equals("V");
                subMv.visitTryCatch(
                        tryMv -> {
                            this.visitDispatchInvocationInsnImpl(tryMv, methodNode);
                            if (hasReturnValue) {
                                tryMv.store("returnValue");
                            }
                        }, 
                        dispatchExceptionHandler()
                );
                Label afterThrowingLabel = new Label();
                subMv.load("exception");
                subMv.visitJumpInsn(Opcodes.IFNULL, afterThrowingLabel);
                subMv.load("exception");
                subMv.visitInsn(Opcodes.ATHROW);
                subMv.visitLabel(afterThrowingLabel);
                if (hasReturnValue) {
                    subMv.load("returnValue");
                }
            }
        } else {
            this.visitDispatchInvocationInsnImpl(mv, methodNode);
        }
        mv.visitInsn(ASMUtils.getReturnCode(returnDescriptor));
    }
    
    private void visitDispatchInvocationInsnImpl(ScopedMethodVisitor mv, MethodNode methodNode) {
        
        int argumentCount = Type.getArgumentTypes(methodNode.desc).length;
        
        mv.load("arr");
        mv.load("index");
        mv.visitInsn(Opcodes.AALOAD);
        mv.visitTypeInsn(Opcodes.CHECKCAST, this.metadataClass.getInternalName());
        for (int i = 1; i <= argumentCount; i++) {
            mv.load("p" + i);
        }
        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE, 
                this.metadataClass.getInternalName(), 
                methodNode.name, 
                methodNode.desc, 
                true
        );
    }
    
    private static Catch dispatchExceptionHandler() {
        return new Catch(
                mv -> {
                    Label endIfLabel = new Label();
                    mv.load("exception");
                    mv.visitJumpInsn(Opcodes.IFNONNULL, endIfLabel);
                    mv.load("ex");
                    mv.store("exception");
                    mv.visitLabel(endIfLabel);
                }
        );
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
                        Opcodes.ACC_STATIC, 
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
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.getInternalName(), 
                    Identifiers.CREATOR_NAME, 
                    ASMConstants.BI_FUNCTION_DESCRIPTOR
            );
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.ABSTRACT_COMBINED_DELEGATE_INTERNAL_NAME, 
                    methodName, 
                    "(Ljava/lang/Object;Ljava/lang/Object;" + 
                    ASMConstants.BI_FUNCTION_DESCRIPTOR + 
                    ")Ljava/lang/Object;",
                    false
            );
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.metadataClass.getInternalName());
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private void generateStaticConstructor(ClassVisitor cv) {
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        
        ASMUtils.visitLambda(
                mv, 
                ASMConstants.BI_FUNCTION_DESCRIPTOR, 
                "apply", 
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", 
                "([Ljava/lang/Object;Ljava/lang/Integer;)" + ASMConstants.ABSTRACT_COMBINED_DELEGATE_DESCRIPTOR, 
                null, 
                Opcodes.H_NEWINVOKESPECIAL, 
                this.getInternalName(), 
                "<init>", 
                "([Ljava/lang/Object;I)V"
        );
        mv.visitFieldInsn(
                Opcodes.PUTSTATIC, 
                this.getInternalName(), 
                Identifiers.CREATOR_NAME, 
                ASMConstants.BI_FUNCTION_DESCRIPTOR
        );
                
        mv.visitInsn(Opcodes.RETURN);
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
