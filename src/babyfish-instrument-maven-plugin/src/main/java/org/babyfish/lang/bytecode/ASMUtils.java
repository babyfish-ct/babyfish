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

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import org.babyfish.org.objectweb.asm.Handle;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;

/**
 * @author Tao Chen
 */
public class ASMUtils {

    public static int getSlotCount(String desc) {
        return "D".equals(desc) || "J".equals(desc) ? 2 : 1;
    }
    
    public static int getLoadCode(String desc) {
        switch (desc.charAt(0)) {
        case 'Z':
        case 'C':
        case 'B':
        case 'S':
        case 'I':
            return Opcodes.ILOAD;
        case 'J':
            return Opcodes.LLOAD;
        case 'F':
            return Opcodes.FLOAD;
        case 'D':
            return Opcodes.DLOAD;
        default:
            return Opcodes.ALOAD;
        }
    }
    
    public static int getStoreCode(String desc) {
        switch (desc.charAt(0)) {
        case 'Z':
        case 'C':
        case 'B':
        case 'S':
        case 'I':
            return Opcodes.ISTORE;
        case 'J':
            return Opcodes.LSTORE;
        case 'F':
            return Opcodes.FSTORE;
        case 'D':
            return Opcodes.DSTORE;
        default:
            return Opcodes.ASTORE;
        }
    }
    
    public static int getDefaultCode(String desc) {
        switch (desc.charAt(0)) {
        case 'Z':
        case 'C':
        case 'B':
        case 'S':
        case 'I':
            return Opcodes.ICONST_0;
        case 'J':
            return Opcodes.LCONST_0;
        case 'F':
            return Opcodes.FCONST_0;
        case 'D':
            return Opcodes.DCONST_0;
        default:
            return Opcodes.ACONST_NULL;
        }
    }
    
    public static int getReturnCode(String desc) {
        switch (desc.charAt(0)) {
        case 'V':
            return Opcodes.RETURN;
        case 'Z':
        case 'C':
        case 'B':
        case 'S':
        case 'I':
            return Opcodes.IRETURN;
        case 'J':
            return Opcodes.LRETURN;
        case 'F':
            return Opcodes.FRETURN;
        case 'D':
            return Opcodes.DRETURN;
        default:
            return Opcodes.ARETURN;
        }
    }
    
    public static String toClassName(String name) {
        if (name.charAt(0) == '[') { // [[[Ljava/lang/Object;
            throw new IllegalArgumentException("laxClassName can't start with '['");
        }
        if (name.endsWith("[]")) { //java.lang.Object[][][]
            throw new IllegalArgumentException("laxClassName can't end with '[]'");
        }
        if (name.charAt(0) == 'L' && name.charAt(name.length() - 1) == ';') {
            name = name.substring(1, name.length() - 1);
        }
        return name.replace('/', '.');
    }
    
    public static String toInternalName(String name) {
        if (name.charAt(0) == 'L' && name.charAt(name.length() - 1) == ';') {
            name = name.substring(1, name.length() - 1);
        }
        return name.replace('.', '/');
    }
    
    public static String toDescriptor(String name) {
        switch (name.charAt(0)) {
        case 'Z':
        case 'C':
        case 'B':
        case 'S':
        case 'I':
        case 'J':
        case 'F':
        case 'D':
        case '[':
            return name;
        case 'L':
            if (name.charAt(name.length() - 1) == ';') {
                return name;
            }
        default:
            return 'L' + name.replace('.', '/') + ';';
        }
    }
    
    public static void visitClassLdc(MethodVisitor mv, Class<?> clazz) {
        if (clazz == null) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        }
        visitClassLdc(mv, Type.getDescriptor(clazz));
    }
    
    public static void visitClassLdc(MethodVisitor mv, String descriptor) {
        if (descriptor == null || descriptor.isEmpty()) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        }
        switch (descriptor.charAt(0)) {
        case 'Z':
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    "java/lang/Boolean", 
                    "TYPE", 
                    "Ljava/lang/Class;"
            );
            break;
        case 'C':
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    "java/lang/Character", 
                    "TYPE", 
                    "Ljava/lang/Class;"
            );
            break;
        case 'B':
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    "java/lang/Byte", 
                    "TYPE", 
                    "Ljava/lang/Class;"
            );
            break;
        case 'S':
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    "java/lang/Short", 
                    "TYPE", 
                    "Ljava/lang/Class;"
            );
            break;
        case 'I':
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    "java/lang/Integer", 
                    "TYPE", 
                    "Ljava/lang/Class;"
            );
            break;
        case 'J':
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    "java/lang/Long", 
                    "TYPE", 
                    "Ljava/lang/Class;"
            );
            break;
        case 'F':
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    "java/lang/Float", 
                    "TYPE", 
                    "Ljava/lang/Class;"
            );
            break;
        case 'D':
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    "java/lang/Double", 
                    "TYPE", 
                    "Ljava/lang/Class;"
            );
            break;
        default:
            mv.visitLdcInsn(Type.getType(descriptor));
            break;
        }
    }

    public static <E extends Enum<E>> void visitEnumLdc(
            MethodVisitor mv, 
            Enum<E> enumValue) {
        if (enumValue == null) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        }
        Class<?> eventClass = enumValue.getClass();
        mv.visitFieldInsn(
                Opcodes.GETSTATIC, 
                eventClass.getName().replace('.', '/'), 
                enumValue.name(), 
                Type.getDescriptor(eventClass));
    }
    
    public static void visitLambda(
            MethodVisitor mv,
            String lambdaDesc,
            String lambdaMethodName,
            String lambdaMethodErasedDesc,
            String lambdaMethodRuntimeDesc,
            String[] lambdaHighLevelVariableDescs,
            int targetTag, 
            String targetOwner, 
            String targetMethodName, 
            String targetMethodDesc) {
        
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        if (lambdaHighLevelVariableDescs != null) {
            for (String lambdaHighLevelVariableDesc : lambdaHighLevelVariableDescs) {
                builder.append(lambdaHighLevelVariableDesc);
            }
        }
        builder.append(')');
        builder.append(lambdaDesc);
        
        mv.visitInvokeDynamicInsn(
                lambdaMethodName, 
                builder.toString(), 
                new Handle(
                        Opcodes.H_INVOKESTATIC,
                        "java/lang/invoke/LambdaMetafactory",
                        "metafactory",
                        '(' +
                        "Ljava/lang/invoke/MethodHandles$Lookup;" +
                        "Ljava/lang/String;" +
                        "Ljava/lang/invoke/MethodType;" +
                        "Ljava/lang/invoke/MethodType;" +
                        "Ljava/lang/invoke/MethodHandle;" +
                        "Ljava/lang/invoke/MethodType;" +
                        ')' + 
                        "Ljava/lang/invoke/CallSite;"
                ), 
                Type.getMethodType(lambdaMethodErasedDesc),
                new Handle(
                        targetTag,
                        targetOwner,
                        targetMethodName,
                        targetMethodDesc
                ),
                Type.getMethodType(lambdaMethodRuntimeDesc)
        );
    }
    
    public static void visitBox(
            MethodVisitor mv, 
            String descriptor, 
            Consumer<MethodVisitor> loadVarLabmda) {
        
        if (descriptor.equals("V")) {
            throw new IllegalArgumentException("descriptor cannot be 'V'");
        }
        
        String boxInternalName;
        String initDesc;
        switch (descriptor.charAt(0)) {
        case 'Z':
            boxInternalName = "java/lang/Boolean";
            initDesc = "(Z)V";
            break;
        case 'C':
            boxInternalName = "java/lang/Character";
            initDesc = "(C)V";
            break;
        case 'B':
            boxInternalName = "java/lang/Byte";
            initDesc = "(B)V";
            break;
        case 'S':
            boxInternalName = "java/lang/Short";
            initDesc = "(S)V";
            break;
        case 'I':
            boxInternalName = "java/lang/Integer";
            initDesc = "(I)V";
            break;
        case 'J':
            boxInternalName = "java/lang/Long";
            initDesc = "(J)V";
            break;
        case 'F':
            boxInternalName = "java/lang/Float";
            initDesc = "(F)V";
            break;
        case 'D':
            boxInternalName = "java/lang/Double";
            initDesc = "(D)V";
            break;
        default:
            loadVarLabmda.accept(mv);
            return;
        }
        mv.visitTypeInsn(Opcodes.NEW, boxInternalName);
        mv.visitInsn(Opcodes.DUP);
        loadVarLabmda.accept(mv);
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                boxInternalName, 
                "<init>", 
                initDesc, 
                false
        );
    }
    
    public static void visitEquals(
            MethodVisitor mv, 
            String descriptor, 
            boolean nullable,
            Consumer<MethodVisitor> equalBranchLambda) {
        
        if (descriptor.equals("V")) {
            throw new IllegalArgumentException("descriptor cannot be 'V'");
        }
        
        if ("Z".equals(descriptor) ||
                "C".equals(descriptor) ||
                "B".equals(descriptor) ||
                "S".equals(descriptor) ||
                "I".equals(descriptor)) {
            Label notEqualLabel = new Label();
            mv.visitJumpInsn(Opcodes.IF_ICMPNE, notEqualLabel);
            equalBranchLambda.accept(mv);
            mv.visitLabel(notEqualLabel);
        } else if ("J".equals(descriptor)) {
            Label notEqualLabel = new Label();
            mv.visitInsn(Opcodes.LCMP);
            mv.visitJumpInsn(Opcodes.IFNE, notEqualLabel);
            equalBranchLambda.accept(mv);
            mv.visitLabel(notEqualLabel);
        } else if ("F".equals(descriptor)) {
            Label notEqualLabel = new Label();
            mv.visitInsn(Opcodes.FCMPL);
            mv.visitJumpInsn(Opcodes.IFNE, notEqualLabel);
            equalBranchLambda.accept(mv);
            mv.visitLabel(notEqualLabel);
        } else if ("D".equals(descriptor)) {
            Label notEqualLabel = new Label();
            mv.visitInsn(Opcodes.DCMPL);
            mv.visitJumpInsn(Opcodes.IFNE, notEqualLabel);
            equalBranchLambda.accept(mv);
            mv.visitLabel(notEqualLabel);
        } else if (descriptor.charAt(0) == '[') {
            String arrayDesc;
            switch (descriptor.charAt(1)) {
            case 'Z':
            case 'C':
            case 'B':
            case 'S':
            case 'I':
            case 'J':
            case 'F':
            case 'D':
                arrayDesc = descriptor;
                break;
            default:
                arrayDesc = "[Ljava/lang/Object;";
            }
            Label notEqualLabel = new Label();
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    Type.getInternalName(Arrays.class), 
                    "equals", 
                    '(' + arrayDesc + arrayDesc + ")Z",
                    false
            );
            mv.visitJumpInsn(Opcodes.IFEQ, notEqualLabel);
            equalBranchLambda.accept(mv);
            mv.visitLabel(notEqualLabel);
        } else {
            Label notEqualLabel = new Label();
            if (nullable) {
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        Type.getInternalName(Objects.class), 
                        "equals", 
                        "(Ljava/lang/Object;Ljava/lang/Object;)Z",
                        false
                );
            } else {
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/Object", 
                        "equals", 
                        "(Ljava/lang/Object;)Z",
                        false
                );
            }
            mv.visitJumpInsn(Opcodes.IFEQ, notEqualLabel);
            equalBranchLambda.accept(mv);
            mv.visitLabel(notEqualLabel);
        }
    }
    
    public static void visitUnbox(MethodVisitor mv, String descriptor, boolean needCheckcast) {
        String boxInternalName;
        String unboxName;
        String unboxDesc;
        switch (descriptor.charAt(0)) {
        case 'Z':
            boxInternalName = "java/lang/Boolean";
            unboxName = "booleanValue";
            unboxDesc = "()Z";
            break;
        case 'C':
            boxInternalName = "java/lang/Character";
            unboxName = "charValue";
            unboxDesc = "()C";
            break;
        case 'B':
            boxInternalName = "java/lang/Byte";
            unboxName = "byteValue";
            unboxDesc = "()B";
            break;
        case 'S':
            boxInternalName = "java/lang/Short";
            unboxName = "shortValue";
            unboxDesc = "()S";
            break;
        case 'I':
            boxInternalName = "java/lang/Integer";
            unboxName = "intValue";
            unboxDesc = "()I";
            break;
        case 'J':
            boxInternalName = "java/lang/Long";
            unboxName = "longValue";
            unboxDesc = "()J";
            break;
        case 'F':
            boxInternalName = "java/lang/Float";
            unboxName = "floatValue";
            unboxDesc = "()F";
            break;
        case 'D':
            boxInternalName = "java/lang/Double";
            unboxName = "doubleValue";
            unboxDesc = "()D";
            break;
        default:
            if (needCheckcast) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, toInternalName(descriptor));
            }
            return;
        }
        if (needCheckcast) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, boxInternalName);
        }
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                boxInternalName, 
                unboxName, 
                unboxDesc, 
                false
        );
    }

    private ASMUtils() {
        
    }
}
