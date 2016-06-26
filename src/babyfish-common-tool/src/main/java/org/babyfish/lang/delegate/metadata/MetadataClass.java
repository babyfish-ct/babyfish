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
package org.babyfish.lang.delegate.metadata;

import java.util.function.Consumer;

import org.babyfish.lang.Delegate;
import org.babyfish.lang.DelegateExceptionHandlingType;
import org.babyfish.lang.I18N;
import org.babyfish.lang.bytecode.ASMTreeUtils;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.signature.SignatureReader;
import org.babyfish.org.objectweb.asm.signature.SignatureVisitor;
import org.babyfish.org.objectweb.asm.tree.ClassNode;
import org.babyfish.org.objectweb.asm.tree.MethodNode;
import org.eclipse.sisu.space.asm.Type;

/**
 * @author Tao Chen
 */
public class MetadataClass {
    
    private ClassNode classNode;
    
    private String className;
    
    private String internalName;
    
    private String descriptor;
    
    private String typeParameterClause;
    
    private String typeArgumentClause;
    
    private DelegateExceptionHandlingType exceptionHandlingType;

    public MetadataClass(ClassNode classNode) {
        String className = classNode.name.replace('/', '.');
        if ((classNode.access & Opcodes.ACC_INTERFACE) == 0) {
            throw new IllegalClassException(
                    delegateMustBeInterface(className, Delegate.class)
            );
        }
        if (classNode.methods == null || classNode.methods.isEmpty()) {
            throw new IllegalClassException(
                    delegateMustHaveMethods(className, Delegate.class)
            );
        }
        this.classNode = classNode;
        this.className = className;
        this.internalName = classNode.name;
        this.descriptor = 'L' + classNode.name + ';';
        this.typeParameterClause = getTypeParameterClause(classNode.signature);
        this.typeArgumentClause = getTypeArgumentClause(classNode.signature);
        this.exceptionHandlingType = ASMTreeUtils.getAnnotationEnumValue(
                DelegateExceptionHandlingType.class, 
                ASMTreeUtils.getAnnotationNode(this.classNode, Delegate.class), 
                "value", 
                DelegateExceptionHandlingType.BREAK
        );
        
        String selfDescriptor = 'L' + classNode.name + ';';
        String methodDescriptor = '(' + selfDescriptor + selfDescriptor + ')' + selfDescriptor;
        boolean hasCombineMethod = false;
        boolean hasRemoveMethod = false;
        for (MethodNode methodNode : classNode.methods) {
            String name = methodNode.name;
            if (methodNode.desc.equals(methodDescriptor)) {
                switch(name) {
                case "combine":
                    hasCombineMethod = true;
                    this.validateCombinerMethodNode(methodNode, classNode);
                    break;
                case "remove":
                    hasRemoveMethod = true;
                    this.validateCombinerMethodNode(methodNode, classNode);
                    break;
                }
            }
        }
        if (!hasCombineMethod) {
            throw new IllegalClassException(
                    missCombinerMethod(
                            this.className, 
                            Delegate.class,
                            "combine" + '(' + this.className + ", " + this.className + ')'
                    )
            );
        }
        if (!hasRemoveMethod) {
            throw new IllegalClassException(
                    missCombinerMethod(
                            this.className, 
                            Delegate.class,
                            "remove" + '(' + this.className + ", " + this.className + ')'
                    )
            );
        }
    }
    
    public int getBytecodeVersion() {
        return this.classNode.version;
    }
    
    public String getClassName() {
        return this.className;
    }
    
    public String getInternalName() {
        return this.internalName;
    }
    
    public String getDescriptor() {
        return this.descriptor;
    }
    
    public String getTypeParameterClause() {
        return this.typeParameterClause;
    }

    public String getTypeArgumentClause() {
        return this.typeArgumentClause;
    }
    
    public DelegateExceptionHandlingType getExceptionHandlingType() {
        return this.exceptionHandlingType;
    }
    
    public void consumeInstanceMethods(Consumer<MethodNode> consumer) {
        for (MethodNode methodNode : this.classNode.methods) {
            if ((methodNode.access & Opcodes.ACC_STATIC) == 0) {
                String[] exceptions = null;
                if (methodNode.exceptions != null) {
                    exceptions = methodNode.exceptions.toArray(
                            new String[methodNode.exceptions.size()]
                    );
                }
                MethodNode clonedMethodNode = new MethodNode(
                        Opcodes.ASM5,
                        methodNode.access,
                        methodNode.name,
                        methodNode.desc,
                        methodNode.signature,
                        exceptions
                );
                consumer.accept(clonedMethodNode);
            }
        }
    }

    private void validateCombinerMethodNode(MethodNode methodNode, ClassNode classNode) {
        if ((methodNode.access & Opcodes.ACC_STATIC) == 0) {
            throw new IllegalClassException(
                    methodShouldBeStatic(
                            this.className,
                            Delegate.class,
                            methodString(methodNode)
                    )
            );
        }
        String methodTypeParameterClause = getTypeParameterClause(methodNode.signature);
        if (this.typeParameterClause == null && methodTypeParameterClause != null) {
            throw new IllegalClassException(
                    methodMustNotBeGeneric(
                            this.className, 
                            Delegate.class, 
                            methodString(methodNode)
                    )
            );
        }
        if (this.typeParameterClause != null && methodTypeParameterClause == null) {
            throw new IllegalClassException(
                    methodMustBeGeneric(
                            this.className, 
                            Delegate.class, 
                            methodString(methodNode)
                    )
            );
        }
        if (methodTypeParameterClause == null) {
            return;
        }
        if (!methodTypeParameterClause.equals(typeParameterClause)) {
            throw new IllegalClassException(
                    methodHasIllegalGenericParameters(
                            this.className, 
                            Delegate.class, 
                            methodString(methodNode)
                    )
            );
        }
        String argumentSignature = 'L' + classNode.name + this.typeArgumentClause + ';';
        String expectedSignaturePostfix =
                '(' + argumentSignature + argumentSignature + ')' + argumentSignature;
        if (!methodNode.signature.substring(methodTypeParameterClause.length())
                .equals(expectedSignaturePostfix)) {
            throw new IllegalClassException(
                    methodHasIllegalSignature(
                            this.className, 
                            Delegate.class, 
                            methodString(methodNode),
                            methodTypeParameterClause + expectedSignaturePostfix, 
                            methodNode.signature
                    )
            );
        }
    }
    
    private static String getTypeParameterClause(String signature) {
        if (signature == null || signature.charAt(0) != '<') {
            return null;
        }
        int len = signature.length();
        int depth = 0;
        for (int i = 0; i < len; i++) {
            char c = signature.charAt(i);
            if (c == '<') {
                depth++;
            }
            if (c == '>' && --depth == 0) {
                return signature.substring(0, i + 1);
            }
        }
        throw new AssertionError("Inernal bug");
    }
    
    private static String getTypeArgumentClause(String signature) {
        if (signature == null || signature.charAt(0) != '<') {
            return null;
        }
        final StringBuilder builder = new StringBuilder();
        builder.append('<');
        new SignatureReader(signature).accept(new SignatureVisitor(Opcodes.ASM5) {
            @Override
            public void visitFormalTypeParameter(String name) {
                builder.append('T').append(name).append(';');
            }
        });
        builder.append('>');
        return builder.toString();
    }
    
    private static String methodString(MethodNode methodNode) {
        StringBuilder builder = new StringBuilder();
        builder
        .append(Type.getReturnType(methodNode.desc).getClassName())
        .append(' ')
        .append(methodNode.name)
        .append('(');
        boolean addComma = false;
        for (Type argumentType : Type.getArgumentTypes(methodNode.desc)) {
            if (addComma) {
                builder.append(", ");
            } else {
                addComma = true;
            }
            builder.append(argumentType.getClassName());
        }
        builder.append(')');
        return builder.toString();
    }
    
    @I18N
    private static native String delegateMustBeInterface(
            String illegalTypeName, Class<Delegate> delegateTypeConstant);
    
    @I18N
    private static native String delegateMustHaveMethods(
            String illegalTypeName, Class<Delegate> delegateTypeConstant);
    
    @I18N
    private static native String methodShouldBeStatic(
            String illegalTypeName, 
            Class<Delegate> delegateTypeConstant, 
            String methodString);
    
    @I18N
    private static native String methodMustNotBeGeneric(
            String illegalTypeName, 
            Class<Delegate> delegateTypeConstant, 
            String methodString);
    
    @I18N
    private static native String methodMustBeGeneric(
            String illegalTypeName, 
            Class<Delegate> delegateTypeConstant, 
            String methodString);
    
    @I18N
    private static native String methodHasIllegalGenericParameters(
            String illegalTypeName, 
            Class<Delegate> delegateTypeConstant, 
            String methodString);

    @I18N
    private static native String methodHasIllegalSignature(
            String illegalTypeName,
            Class<Delegate> delegateTypeConstant,
            String methodString,
            String expectedSignature,
            String atualSignature);
    
    @I18N
    private static native String missCombinerMethod(
            String illegalTypeName,
            Class<Delegate> delegateTypeConstant,
            String missedMethodString
    );
}
