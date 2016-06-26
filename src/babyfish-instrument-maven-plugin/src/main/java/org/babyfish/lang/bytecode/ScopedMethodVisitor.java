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
package org.babyfish.lang.bytecode;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;

/**
 * @author Tao Chen
 */
public class ScopedMethodVisitor extends MethodVisitor implements VariableScope {
    
    private VariableScopeBuilder scopeBuilder;
    
    private VariableScope scope;
    
    ScopedMethodVisitor(VariableScopeBuilder scopeBuilder, MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
        this.scopeBuilder = scopeBuilder;
    }
    
    private ScopedMethodVisitor(VariableScope scope, MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
        this.scope = scope;
    }
    
    public void visitTryCatch(
            Consumer<ScopedMethodVisitor> tryGenerationLambda, 
            Catch ... catches) {
        this.visitTryCatch("ex", tryGenerationLambda, catches);
    }
    
    public void visitTryCatch(
            String exVariableName, 
            Consumer<ScopedMethodVisitor> tryGenerationLambda, 
            Catch ... catches) {
        if (catches.length == 0) {
            tryGenerationLambda.accept(this);
            return;
        }
        Set<String> exTypeSet = new HashSet<>();
        for (Catch katch : catches) {
            if (!exTypeSet.add(katch.exceptionInternalName)) {
                throw new IllegalArgumentException(
                        "Duplicated catches for \"" 
                        + katch.exceptionInternalName + 
                        "\""
                );
            }
        }
        
        Label beginTryLabel = new Label();
        Label endTryLabel = new Label();
        Label[] handleLabels = new Label[catches.length];
        for (int i = handleLabels.length - 1; i >= 0; i--) {
            handleLabels[i] = new Label();
        }
        Label finalEndLabel = new Label();
        
        this.mv.visitLabel(beginTryLabel);
        tryGenerationLambda.accept(this);
        this.mv.visitJumpInsn(Opcodes.GOTO, finalEndLabel);
        this.mv.visitLabel(endTryLabel);
        for (int i = 0; i < catches.length; i++) {
            this.mv.visitLabel(handleLabels[i]);
            try (ScopedMethodVisitor subMV = this.createSubScope()) {
                subMV.declare(exVariableName, ASMUtils.toDescriptor(catches[i].exceptionInternalName));
                subMV.store(exVariableName);
                catches[i].generationLambda.accept(subMV);
                this.mv.visitJumpInsn(Opcodes.GOTO, finalEndLabel);
            }
        }
        this.mv.visitLabel(finalEndLabel);
        
        for (int i = 0; i < catches.length; i++) {
            mv.visitTryCatchBlock(beginTryLabel, endTryLabel, handleLabels[i], catches[i].exceptionInternalName);
        }
    }

    public void visitTryFinally(
            Consumer<ScopedMethodVisitor> tryGenerationLambda, 
            Consumer<ScopedMethodVisitor> finallyGenerationLambda) {
        
        Label tryLabel = new Label();
        Label catchLabel = new Label();
        Label finallyLabel = new Label();
        
        this.mv.visitLabel(tryLabel);
        tryGenerationLambda.accept(new NoReturningMethodVisitor(this.scope, this.mv));
        this.mv.visitJumpInsn(Opcodes.GOTO, finallyLabel);
        this.mv.visitLabel(catchLabel);
        try (ScopedMethodVisitor subMV = this.createSubScope()) {
            String hiddenName = subMV.allocateHiddenName();
            subMV.declare(hiddenName, "Ljava/lang/Throwable;");
            subMV.store(hiddenName);
            finallyGenerationLambda.accept(this);
            subMV.load(hiddenName);
            this.mv.visitInsn(Opcodes.ATHROW);
        }
        this.mv.visitLabel(finallyLabel);
        finallyGenerationLambda.accept(this);
        
        this.mv.visitTryCatchBlock(tryLabel, catchLabel, catchLabel, null);
    }

    @Override
    public ScopedMethodVisitor declare(String name, String desc) {
        this.scope.declare(name, desc);
        return this;
    }

    @Override
    public ScopedMethodVisitor declare(String name, String desc, String signature) {
        this.scope.declare(name, desc, signature);
        return this;
    }

    @Override
    public VariableScope declareImmediately(String name, String desc) {
        this.scope.declareImmediately(name, desc);
        return this;
    }

    @Override
    public VariableScope declareImmediately(String name, String desc, String signature) {
        this.scope.declareImmediately(name, desc, signature);
        return this;
    }

    @Override
    public ScopedMethodVisitor store(String name) {
        this.scope.store(name);
        return this;
    }

    @Override
    public ScopedMethodVisitor load(String name) {
        this.scope.load(name);
        return this;
    }

    @Override
    public int slot(String name) {
        return this.scope.slot(name);
    }

    @Override
    public String descriptor(String name) {
        return this.scope.descriptor(name);
    }

    @Override
    public String allocateHiddenName() {
        return this.scope.allocateHiddenName();
    }

    @Override
    public ScopedMethodVisitor createSubScope() {
        return new ScopedMethodVisitor(this.scope.createSubScope(), this.mv);
    }

    @Override
    public void visitCode() {
        
        super.visitCode();
        
        if (this.scopeBuilder == null) {
            throw new IllegalStateException("'visitCode' can only be supported by root ScopedMethodVisitor");
        }
        if (this.scope != null) {
            throw new IllegalStateException("'visitCode' has been already been executed");
        }
        this.scope = this.scopeBuilder.build(mv);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        
        if (this.scope == null) {
            throw new IllegalStateException("'visitCode' has not been executed before exeute the 'visitMaxs'");
        }
        this.scope.close();
        this.scopeBuilder = null;
        this.scope = null;
        
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void close() {
        if (this.scopeBuilder != null) {
            throw new IllegalStateException("root ScopedMethodVisitor requires the 'visitMaxs' before closing");
        } else if (this.scope != null) { // child visitor, close directly.
            this.scope.close();
        }
    }
    
    public static final class Catch {
        
        String exceptionInternalName;
        
        Consumer<ScopedMethodVisitor> generationLambda;
        
        public Catch(Consumer<ScopedMethodVisitor> generationLambda) {
            this.exceptionInternalName = "java/lang/Throwable";
            this.generationLambda = generationLambda;
        }

        public Catch(String exceptionInternalName, Consumer<ScopedMethodVisitor> generationLambda) {
            if (exceptionInternalName == null || exceptionInternalName.isEmpty()) {
                exceptionInternalName = "java/lang/Throwable";
            }
            this.exceptionInternalName = exceptionInternalName;
            this.generationLambda = generationLambda;
        }
    }
    
    private static class NoReturningMethodVisitor extends ScopedMethodVisitor {

        NoReturningMethodVisitor(VariableScope scope, MethodVisitor mv) {
            super(scope, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            switch (opcode) {
            case Opcodes.RETURN:
            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
            case Opcodes.FRETURN:
            case Opcodes.DRETURN:
            case Opcodes.ARETURN:
                throw new UnsupportedOperationException("Can't generate returning statement in the try block of try-finally statement");
            default:
                super.visitInsn(opcode);
            }
        }
    }
}
