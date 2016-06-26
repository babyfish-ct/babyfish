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

import java.util.List;
import java.util.function.Consumer;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.model.instrument.metadata.MetadataClass;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.org.objectweb.asm.Handle;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.org.objectweb.asm.commons.AnalyzerAdapter;
import org.babyfish.org.objectweb.asm.tree.AbstractInsnNode;
import org.babyfish.org.objectweb.asm.tree.FieldInsnNode;
import org.babyfish.org.objectweb.asm.tree.InsnList;
import org.babyfish.org.objectweb.asm.tree.MethodNode;
import org.babyfish.org.objectweb.asm.tree.TypeInsnNode;

/**
 * @author Tao Chen
 */
final class ModelMethodAdapter extends AnalyzerAdapter {
    
    private  MethodVisitor finalMv;
    
    private MetadataClass metadataClass;
    
    private boolean contractInterface;
    
    private String ancestorObjectModelContractDescriptor;
    
    private String objectModelContractDescriptor;
    
    private String objectModelContractInternalName;
    
    private Consumer<MethodVisitor> preClinitLambda;
    
    private Consumer<MethodVisitor> preInitLambda;
    
    // Key: AbstractInsnNode that make the stack top is the current class.
    // Value: the stack length of that AbstractInsnNode
    private XOrderedMap<AbstractInsnNode, Integer> selfWillBeStackTopInsnMap = new LinkedHashMap<>();
    
    public ModelMethodAdapter(
            boolean contractInterface,
            MetadataClass metadataClass, 
            int access, 
            String name, 
            String desc, 
            String signature, 
            String[] exceptions,
            MethodVisitor mv,
            Consumer<MethodVisitor> preClinitLambda,
            Consumer<MethodVisitor> preInitLambda) {
        super(
                Opcodes.ASM5, 
                metadataClass.getInternalName(), 
                access, 
                name, 
                desc, 
                new MethodNode(Opcodes.ASM5, access, name, desc, signature, exceptions)
        );
        this.finalMv = mv;
        this.contractInterface = contractInterface;
        this.metadataClass = metadataClass;
        this.ancestorObjectModelContractDescriptor =
                'L' + 
                metadataClass.getAncestorClass().getInternalName() + 
                '$' + 
                Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME + 
                ';';
        this.objectModelContractDescriptor =
                'L' +
                metadataClass.getInternalName() + 
                '$' + 
                Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME +
                ';';
        this.objectModelContractInternalName = 
                this.objectModelContractDescriptor
                .substring(1, this.objectModelContractDescriptor.length() - 1);
        if ((access & Opcodes.ACC_STATIC) != 0 &&
                name.equals("<clinit>") &&
                desc.equals("()V")) {
            this.preClinitLambda = preClinitLambda;
        }
        if ((access & Opcodes.ACC_STATIC) == 0 && 
                name.equals("<init>") && 
                desc.endsWith(")V")) {
            this.preInitLambda = preInitLambda;
        }
    }

    @Override
    public void visitCode() {
        super.visitCode();
        if (this.preClinitLambda != null) {
            this.preClinitLambda.accept(this);
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        ((MethodNode)this.mv).accept(this.finalMv);
    }

    @Deprecated
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (this.isSuperInitInsnNode(opcode, owner, name, desc)) {
            super.visitMethodInsn(opcode, owner, name, desc);
            this.visitObjectModelInitialization();
        } else {
            super.visitMethodInsn(opcode, owner, name, desc);
        }
        this.registerSelfStackTopInsnNode();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (this.isSuperInitInsnNode(opcode, owner, name, desc)) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            this.visitObjectModelInitialization();
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
        this.registerSelfStackTopInsnNode();
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (owner.equals(this.metadataClass.getInternalName()) &&
                this.metadataClass.getDeclaredProperties().containsKey(name)) {
            MetadataProperty metadataProperty = this.metadataClass.getDeclaredProperties().get(name);
            Integer stackLength = this.stack.size();
            if (opcode == Opcodes.PUTFIELD) {
                stackLength -= ASMUtils.getSlotCount(desc);
            }
            InsnList instructions = ((MethodNode)this.mv).instructions;
            boolean findOwner = false;
            for (AbstractInsnNode abstractInsnNode = instructions.getLast();
                    abstractInsnNode != null;
                    abstractInsnNode = abstractInsnNode.getPrevious()) {
                if (stackLength.equals(this.selfWillBeStackTopInsnMap.get(abstractInsnNode))) {
                    FieldInsnNode objectModelFieldInsnNode = 
                            new FieldInsnNode(
                                    Opcodes.GETFIELD,
                                    this.metadataClass.getAncestorClass().getInternalName(),
                                    Identifiers.OBJECT_MODEL_FIELD_NAME,
                                    this.ancestorObjectModelContractDescriptor
                            );
                    instructions.insert(abstractInsnNode, objectModelFieldInsnNode);
                    if (this.metadataClass.getSuperClass() != null) {
                        instructions.insert(
                                objectModelFieldInsnNode, 
                                new TypeInsnNode(Opcodes.CHECKCAST, this.objectModelContractInternalName)
                        );
                    }
                    findOwner = true;
                    break;
                } 
            }
            if (!findOwner) {
                throw new AssertionError("Internal bug");
            }
            if (opcode == Opcodes.GETFIELD) {
                this.stack.set(
                        this.stack.size() - 1, 
                        this.objectModelContractDescriptor
                );
                super.visitMethodInsn(
                        this.contractInterface ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, 
                        this.objectModelContractInternalName, 
                        Identifiers.getterName(metadataProperty), 
                        "()" + desc, 
                        this.contractInterface
                );
            } else {
                this.stack.set(
                        this.stack.size() - 1 - ASMUtils.getSlotCount(desc), 
                        this.objectModelContractDescriptor
                );
                super.visitMethodInsn(
                        this.contractInterface ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, 
                        this.objectModelContractInternalName, 
                        Identifiers.setterName(metadataProperty), 
                        '(' + desc + ")V", 
                        this.contractInterface
                );
            }
        } else {
            super.visitFieldInsn(opcode, owner, name, desc);
        }
        this.registerSelfStackTopInsnNode();
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
        this.registerSelfStackTopInsnNode();
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
        this.registerSelfStackTopInsnNode();
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode, type);
        this.registerSelfStackTopInsnNode();
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        this.registerSelfStackTopInsnNode();
    }
    
    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
        this.registerSelfStackTopInsnNode();
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        this.registerSelfStackTopInsnNode();
    }

    @Override
    public void visitLdcInsn(Object cst) {
        super.visitLdcInsn(cst);
        this.registerSelfStackTopInsnNode();
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment);
        this.registerSelfStackTopInsnNode();
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        super.visitTableSwitchInsn(min, max, dflt, labels);
        this.registerSelfStackTopInsnNode();
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        super.visitLookupSwitchInsn(dflt, keys, labels);
        this.registerSelfStackTopInsnNode();
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        super.visitMultiANewArrayInsn(desc, dims);
        this.registerSelfStackTopInsnNode();
    }

    private void registerSelfStackTopInsnNode() {
        List<Object> stack = this.stack;
        if (stack != null && 
                !stack.isEmpty() && 
                stack.get(stack.size() - 1).equals(this.metadataClass.getInternalName())) {
            this.selfWillBeStackTopInsnMap.put(
                    ((MethodNode)this.mv).instructions.getLast(), 
                    stack.size()
            );
        }
    }
    
    private boolean isSuperInitInsnNode(int opcode, String owner, String name, String desc) {
        return
                this.preInitLambda != null && 
                opcode == Opcodes.INVOKESPECIAL &&
                owner.equals(this.metadataClass.getSuperTypeName().replace('.', '/')) &&
                name.equals("<init>") &&
                desc.endsWith(")V") &&
                this.stack.size() == (Type.getArgumentsAndReturnSizes(desc) >> 2);
    }
    
    private void visitObjectModelInitialization() {
        Consumer<MethodVisitor> preInitLabmda = this.preInitLambda;
        this.preInitLambda = null;
        preInitLabmda.accept(this);
    }
}
