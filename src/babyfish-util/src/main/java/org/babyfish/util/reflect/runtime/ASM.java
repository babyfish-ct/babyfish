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
package org.babyfish.util.reflect.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.NullArgumentException;
import org.babyfish.lang.Nulls;
import org.babyfish.org.objectweb.asm.AnnotationVisitor;
import org.babyfish.org.objectweb.asm.Attribute;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.ClassWriter;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.org.objectweb.asm.tree.AbstractInsnNode;
import org.babyfish.org.objectweb.asm.tree.FieldInsnNode;
import org.babyfish.org.objectweb.asm.tree.InsnList;
import org.babyfish.org.objectweb.asm.tree.InsnNode;
import org.babyfish.org.objectweb.asm.tree.LdcInsnNode;
import org.babyfish.org.objectweb.asm.tree.MethodInsnNode;
import org.babyfish.org.objectweb.asm.tree.TypeInsnNode;
import org.babyfish.util.reflect.ClassInfo;
import org.babyfish.util.reflect.ConstructorInfo;
import org.babyfish.util.reflect.MethodBase;
import org.babyfish.util.reflect.MethodInfo;

/*
 * This class can not depend on org.babyfish.lang.Arguments that depends on 
 * org.babyfish.util.Resources that depends on this class
 */
/**
 * @author Tao Chen
 */
public final class ASM {
    
    private static final ReadWriteLock DYNAMIC_CLASSES_LOCK = new ReentrantReadWriteLock();
    
    private static final Method CLASS_LOADER_DEFINE_CLASS;
    
    private static final Method CLASS_LOADER_FIND_LOADED_CLASS;
    
    private static final int[] EMPTY_SLOTS = new int[0];
    
    private ASM() {
        
    }

    public static String getInternalName(org.babyfish.org.objectweb.asm.Type type) {
        switch (type.getSort()) {
        case org.babyfish.org.objectweb.asm.Type.BOOLEAN:
            return "Z";
        case org.babyfish.org.objectweb.asm.Type.CHAR:
            return "C";
        case org.babyfish.org.objectweb.asm.Type.BYTE:
            return "B";
        case org.babyfish.org.objectweb.asm.Type.SHORT:
            return "S";
        case org.babyfish.org.objectweb.asm.Type.INT:
            return "I";
        case org.babyfish.org.objectweb.asm.Type.LONG:
            return "J";
        case org.babyfish.org.objectweb.asm.Type.FLOAT:
            return "F";
        case org.babyfish.org.objectweb.asm.Type.DOUBLE:
            return "D";
        default:
            return type.getInternalName();
        }
    }
    
    public static String getInternalName(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return getDescriptor(clazz);
        }
        return org.babyfish.org.objectweb.asm.Type.getInternalName(clazz);
    }
    
    public static String getInternalName(String desc) {
        if (desc.startsWith("[")) {
            return desc;
        }
        if (desc.startsWith("L") && desc.endsWith(";")) {
            return desc.substring(1, desc.length() - 1);
        }
        return desc;
    }
    
    public static String getDescriptor(Class<?> clazz) {
        return org.babyfish.org.objectweb.asm.Type.getDescriptor(clazz);
    }
    
    public static String getDescriptor(org.babyfish.org.objectweb.asm.Type type) {
        switch (type.getSort()) {
        case org.babyfish.org.objectweb.asm.Type.BOOLEAN:
            return "Z";
        case org.babyfish.org.objectweb.asm.Type.CHAR:
            return "C";
        case org.babyfish.org.objectweb.asm.Type.BYTE:
            return "B";
        case org.babyfish.org.objectweb.asm.Type.SHORT:
            return "S";
        case org.babyfish.org.objectweb.asm.Type.INT:
            return "I";
        case org.babyfish.org.objectweb.asm.Type.LONG:
            return "J";
        case org.babyfish.org.objectweb.asm.Type.FLOAT:
            return "F";
        case org.babyfish.org.objectweb.asm.Type.DOUBLE:
            return "D";
        default:
            return type.getDescriptor();
        }
    }
    
    public static String getDescriptor(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        for (Class<?> parameterType : method.getParameterTypes()) {
            builder.append(getDescriptor(parameterType));
        }
        builder.append(')'); 
        builder.append(getDescriptor(method.getReturnType()));
        return builder.toString();
    }

    public static String getDescriptor(Constructor<?> constructor) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        for (Class<?> parameterType : constructor.getParameterTypes()) {
            builder.append(getDescriptor(parameterType));
        }
        builder.append(")V"); 
        return builder.toString();
    }

    public static String getDescriptor(List<Class<?>> parameterTypes) {
        StringBuilder builder = new StringBuilder();
        for (Class<?> parameterType : parameterTypes) {
            builder.append(getDescriptor(parameterType));
        }
        return builder.toString();
    }

    public static String getDescriptor(MethodBase methodBase, boolean resolved) {
        StringBuilder builder = new StringBuilder();
        List<Class<?>> parameterTypes =
            resolved ?
                    methodBase.getResolvedParameterTypes() : 
                    methodBase.getParameterTypes();
        builder.append('(');
        builder.append(getDescriptor(parameterTypes));
        builder.append(')');
        if (methodBase instanceof MethodInfo) {
            MethodInfo mi = (MethodInfo)methodBase;
            Class<?> returnType = resolved ? mi.getResolvedReturnType() : mi.getReturnType(); 
            builder.append(getDescriptor(returnType));
        } else {
            builder.append('V');
        }
        return builder.toString();
    }

    public static String getDescriptor(String internalName) {
        if (internalName.equals("I") ||
                internalName.equals("J") ||
                internalName.equals("F") ||
                internalName.equals("D") ||
                internalName.startsWith("L") ||
                internalName.startsWith("[")) {
            return internalName;
        }
        return 'L' + internalName + ';';
    }
    
    public static String[] getExceptionInternalNames(Method method) {
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        String[] _throws = new String[exceptionTypes.length];
        for (int i = _throws.length - 1; i >= 0; i--) {
            _throws[i] = exceptionTypes[i].getName().replace('.', '/');
        }
        return _throws;
    }
    
    public static String[] getExceptionInternalNames(Constructor<?> constructor) {
        Class<?>[] exceptionTypes = constructor.getExceptionTypes();
        String[] _throws = new String[exceptionTypes.length];
        for (int i = _throws.length - 1; i >= 0; i--) {
            _throws[i] = exceptionTypes[i].getName().replace('.', '/');
        }
        return _throws;
    }
    
    public static String[] getExceptionInternalNames(MethodBase methodBase) {
        if (methodBase instanceof MethodInfo) {
            return getExceptionInternalNames(((MethodInfo)methodBase).getRawMethod());
        }
        return getExceptionInternalNames(((ConstructorInfo<?>)methodBase).getRawConstructor());
    }
    
    public static int[] getParameterSlots(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        int len = parameterTypes.length;
        if (len == 0) {
            return EMPTY_SLOTS;
        }
        int slot = Modifier.isStatic(method.getModifiers()) ? 0 : 1;
        int[] slots = new int[len];
        for (int i = 0; i < len; i++) {
            slots[i] = slot;
            slot += getSlotCount(parameterTypes[i]);
        }
        return slots;
    }
    
    public static SlotAllocator createSlotAllocator(int baseSlot) {
        return new SlotAllocatatorImpl(baseSlot);
    }
    
    public static int getSlotCount(Method method) {
        int slots = Modifier.isStatic(method.getModifiers()) ? 0 : 1;
        for (Class<?> parameterType : method.getParameterTypes()) {
            slots += getSlotCount(parameterType);
        }
        return slots;
    }
    
    public static int getSlotCount(Constructor<?> consturctor) {
        int slots = 1;
        for (Class<?> parameterType : consturctor.getParameterTypes()) {
            slots += getSlotCount(parameterType);
        }
        return slots;
    }
    
    public static int getSlotCount(MethodBase methodBase) {
        if (methodBase instanceof MethodInfo) {
            return getSlotCount(((MethodInfo)methodBase).getRawMethod());
        }
        return getSlotCount(((ConstructorInfo<?>)methodBase).getRawConstructor());
    }
    
    public static int getSlotCount(Class<?> clazz) {
        return clazz == double.class || clazz == long.class ? 2 : 1;
    }
    
    public static int getSlotCount(String desc) {
        return "D".equals(desc) || "J".equals(desc) ? 2 : 1;
    }
    
    public static int getDefaultCode(Class<?> clazz) {
        ExceptionUtil.mustNotBeNull("clazz", clazz);
        ExceptionUtil.mustNotBeEqualToValue("clazz", clazz, void.class);
        if (boolean.class == clazz) {
            return Opcodes.ICONST_0;
        }
        if (char.class == clazz) {
            return Opcodes.ICONST_0;
        }
        if (byte.class == clazz) {
            return Opcodes.ICONST_0;
        }
        if (short.class == clazz) {
            return Opcodes.ICONST_0;
        }
        if (int.class == clazz) {
            return Opcodes.ICONST_0;
        }
        if (long.class == clazz) {
            return Opcodes.LCONST_0;
        }
        if (float.class == clazz) {
            return Opcodes.FCONST_0;
        }
        if (double.class == clazz) {
            return Opcodes.DCONST_0;
        }
        return Opcodes.ACONST_NULL;
    }
    
    public static int getDefaultCode(String desc) {
        ExceptionUtil.mustNotBeNull("desc", desc);
        ExceptionUtil.mustNotBeEmpty("desc", desc);
        ExceptionUtil.mustNotBeEqualToValue("desc", desc, "V");
        switch (desc) {
        case "Z":
        case "C":
        case "B":
        case "S":
        case "I":
            return Opcodes.ICONST_0;
        case "J":
            return Opcodes.LCONST_0;
        case "F":
            return Opcodes.FCONST_0;
        case "D":
            return Opcodes.DCONST_0;
        }
        return Opcodes.ACONST_NULL;
    }

    public static int getReturnCode(Class<?> clazz) {
        ExceptionUtil.mustNotBeNull("clazz", clazz);
        if (clazz == void.class) {
            return Opcodes.RETURN;
        }
        if (clazz == boolean.class || 
                clazz == char.class || 
                clazz == byte.class || 
                clazz == short.class || 
                clazz == int.class) {
            return Opcodes.IRETURN;
        } 
        if (clazz == long.class) {
            return Opcodes.LRETURN;
        }
        if (clazz == float.class) {
            return Opcodes.FRETURN;
        }
        if (clazz == double.class) {
            return Opcodes.DRETURN;
        }
        return Opcodes.ARETURN;
    }
    
    public static int getReturnCode(String desc) {
        ExceptionUtil.mustNotBeNull("desc", desc);
        ExceptionUtil.mustNotBeEmpty("desc", desc);
        ExceptionUtil.mustNotBeEqualToValue("desc", desc, "V");
        switch (desc) {
        case "Z":
        case "C":
        case "B":
        case "S":
        case "I":
            return Opcodes.IRETURN;
        case "J":
            return Opcodes.LRETURN;
        case "F":
            return Opcodes.FRETURN;
        case "D":
            return Opcodes.DRETURN;
        }
        return Opcodes.ARETURN;
    }

    public static int getLoadCode(Class<?> clazz) {
        ExceptionUtil.mustNotBeNull("clazz", clazz);
        ExceptionUtil.mustNotBeEqualToValue("clazz", clazz, void.class);
        if (clazz == boolean.class || clazz == char.class 
                || clazz == byte.class || clazz == short.class || clazz == int.class) {
            return Opcodes.ILOAD;
        } 
        if (clazz == long.class) {
            return Opcodes.LLOAD;
        }
        if (clazz == float.class) {
            return Opcodes.FLOAD;
        }
        if (clazz == double.class) {
            return Opcodes.DLOAD;
        }
        return Opcodes.ALOAD;
    }
    
    public static int getLoadCode(String desc) {
        ExceptionUtil.mustNotBeNull("desc", desc);
        ExceptionUtil.mustNotBeEmpty("desc", desc);
        ExceptionUtil.mustNotBeEqualToValue("desc", desc, "V");
        switch (desc) {
        case "Z":
        case "C":
        case "B":
        case "S":
        case "I":
            return Opcodes.ILOAD;
        case "J":
            return Opcodes.LLOAD;
        case "F":
            return Opcodes.FLOAD;
        case "D":
            return Opcodes.DLOAD;
        }
        return Opcodes.ALOAD;
    }
    
    public static int getStoreCode(Class<?> clazz) {
        ExceptionUtil.mustNotBeNull("clazz", clazz);
        ExceptionUtil.mustNotBeEqualToValue("clazz", clazz, void.class);
        if (clazz == boolean.class || clazz == char.class 
                || clazz == byte.class || clazz == short.class || clazz == int.class) {
            return Opcodes.ISTORE;
        } 
        if (clazz == long.class) {
            return Opcodes.LSTORE;
        }
        if (clazz == float.class) {
            return Opcodes.FSTORE;
        }
        if (clazz == double.class) {
            return Opcodes.DSTORE;
        }
        return Opcodes.ASTORE;
    }
    
    public static int getStoreCode(String desc) {
        ExceptionUtil.mustNotBeNull("desc", desc);
        ExceptionUtil.mustNotBeEmpty("desc", desc);
        ExceptionUtil.mustNotBeEqualToValue("desc", desc, "V");
        switch (desc) {
        case "Z":
        case "C":
        case "B":
        case "S":
        case "I":
            return Opcodes.ISTORE;
        case "J":
            return Opcodes.LSTORE;
        case "F":
            return Opcodes.FSTORE;
        case "D":
            return Opcodes.DSTORE;
        }
        return Opcodes.ASTORE;
    }
    
    public static int getLoadArrayItemCode(Class<?> componentType) {
        ExceptionUtil.mustNotBeNull("componentType", componentType);
        ExceptionUtil.mustNotBeEqualToValue("componentType", componentType, void.class);
        if (componentType == boolean.class || componentType == char.class 
                || componentType == byte.class || componentType == short.class || componentType == int.class) {
            return Opcodes.IALOAD;
        } 
        if (componentType == long.class) {
            return Opcodes.LALOAD;
        }
        if (componentType == float.class) {
            return Opcodes.FALOAD;
        }
        if (componentType == double.class) {
            return Opcodes.DALOAD;
        }
        return Opcodes.AALOAD;
    }
    
    public static int getLoadArrayItemCode(String desc) {
        ExceptionUtil.mustNotBeNull("desc", desc);
        ExceptionUtil.mustNotBeEmpty("desc", desc);
        ExceptionUtil.mustNotBeEqualToValue("desc", desc, "V");
        switch (desc) {
        case "Z":
        case "C":
        case "B":
        case "S":
        case "I":
            return Opcodes.IALOAD;
        case "J":
            return Opcodes.LALOAD;
        case "F":
            return Opcodes.FALOAD;
        case "D":
            return Opcodes.DALOAD;
        }
        return Opcodes.AALOAD;
    }
    
    public static int getStoreArrayItemCode(Class<?> componentType) {
        ExceptionUtil.mustNotBeNull("componentType", componentType);
        ExceptionUtil.mustNotBeEqualToValue("componentType", componentType, void.class);
        if (componentType == boolean.class || componentType == char.class 
                || componentType == byte.class || componentType == short.class || componentType == int.class) {
            return Opcodes.IASTORE;
        } 
        if (componentType == long.class) {
            return Opcodes.LASTORE;
        }
        if (componentType == float.class) {
            return Opcodes.FASTORE;
        }
        if (componentType == double.class) {
            return Opcodes.DASTORE;
        }
        return Opcodes.AASTORE;
    }
    
    public static int getStoreArrayItemCode(String desc) {
        ExceptionUtil.mustNotBeNull("desc", desc);
        ExceptionUtil.mustNotBeEmpty("desc", desc);
        ExceptionUtil.mustNotBeEqualToValue("desc", desc, "V");
        switch (desc) {
        case "Z":
        case "C":
        case "B":
        case "S":
        case "I":
            return Opcodes.IASTORE;
        case "J":
            return Opcodes.LASTORE;
        case "F":
            return Opcodes.FASTORE;
        case "D":
            return Opcodes.DASTORE;
        }
        return Opcodes.AASTORE;
    }
    
    public static int getPopCode(Class<?> clazz) {
        ExceptionUtil.mustNotBeNull("clazz", clazz);
        ExceptionUtil.mustNotBeEqualToValue("clazz", clazz, void.class);
        if (clazz == long.class || clazz == double.class) {
            return Opcodes.POP2;
        }
        return Opcodes.POP;
    }
    
    public static int getDupCode(Class<?> clazz) {
        ExceptionUtil.mustNotBeNull("clazz", clazz);
        ExceptionUtil.mustNotBeEqualToValue("clazz", clazz, void.class);
        if (clazz == long.class || clazz == double.class) {
            return Opcodes.DUP2;
        }
        return Opcodes.POP;
    }
    
    public static String[] getParameterDescriptors(String methodDescriptor) {
        ExceptionUtil.mustNotBeNull("methodDescriptor", methodDescriptor);
        ExceptionUtil.mustNotBeEmpty("methodDescriptor", methodDescriptor);
        if (methodDescriptor.charAt(0) != '(') {
            throw new IllegalArgumentException(
                    ExceptionUtil.invalidMethodDescriptor("methodDescriptor", methodDescriptor)
            );
        }
        int index = methodDescriptor.lastIndexOf(')');
        if (index == -1) {
            throw new IllegalArgumentException(
                    ExceptionUtil.invalidMethodDescriptor("methodDescriptor", methodDescriptor)
            );
        }
        String parameterDescs = methodDescriptor.substring(1, index);
        List<String> list = new ArrayList<>();
        StringBuilder builder = null;
        int len = parameterDescs.length();
        for (int i = 0; i < len; i++) {
            char c = parameterDescs.charAt(i);
            if (builder == null) {
                if (c == 'Z') {
                    list.add("Z");
                } else if (c == 'C') {
                    list.add("C");
                } else if (c == 'B') {
                    list.add("B");
                } else if (c == 'S') {
                    list.add("S");
                } else if (c == 'I') {
                    list.add("I");
                } else if (c == 'J') {
                    list.add("J");
                } else if (c == 'F') {
                    list.add("F");
                } else if (c == 'D') {
                    list.add("D");
                } else {
                    if (builder == null) {
                        builder = new StringBuilder();
                    }
                    builder.append(c);
                }
            } else {
                builder.append(c);
                if (c == ';') {
                    list.add(builder.toString());
                    builder = null;
                }
            }
        }
        if (builder != null) {
            throw new IllegalArgumentException(
                    ExceptionUtil.invalidMethodDescriptor("methodDescriptor", methodDescriptor)
            );
        }
        return list.toArray(new String[list.size()]);
    }
    
    public static String getReturnedDescriptor(String methodDescriptor) {
        ExceptionUtil.mustNotBeNull("methodDescriptor", methodDescriptor);
        ExceptionUtil.mustNotBeEmpty("methodDescriptor", methodDescriptor);
        int index = methodDescriptor.lastIndexOf(')');
        if (index == -1) {
            throw new IllegalArgumentException(
                    ExceptionUtil.invalidMethodDescriptor("methodDescriptor", methodDescriptor)
            );
        }
        return methodDescriptor.substring(index + 1);
    }
    
    public static XMethodVisitor visitMethod(
            ClassVisitor cv, 
            int access,
            String name,
            String desc,
            String signature,
            String[] exceptions) {
        return new XMethodVisitorImpl(cv, access, name, desc, signature, exceptions);
    }
    
    public static void visitClassLdc(MethodVisitor mv, Class<?> clazz) {
        if (clazz == null) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        }
        visitClassLdc(mv, getDescriptor(clazz));
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
                ASM.getDescriptor(eventClass));
    }
    
    public static void visitBox(MethodVisitor mv, String descriptor, Consumer<MethodVisitor> loadVarLabmda) {
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
                mv.visitTypeInsn(Opcodes.CHECKCAST, getInternalName(descriptor));
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
    
    public static void visitNewObjectWithoutParameters(
            MethodVisitor mv, String typeInternalName) {
        mv.visitTypeInsn(Opcodes.NEW, typeInternalName);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                typeInternalName, 
                "<init>", 
                "()V",
                false);
    }
    
    public static void visitNewObjectWithConstant(
            MethodVisitor mv, String typeInternalName, Class<?> parameterType, Object constant) {
        mv.visitTypeInsn(Opcodes.NEW, typeInternalName);
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(constant);
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                typeInternalName, 
                "<init>", 
                "(" + getDescriptor(parameterType) + ")V",
                false);
    }
    
    public static void visitNewObjectWithConstants(
            MethodVisitor mv, 
            String typeInternalName, 
            Class<?>[] parameterTypes, 
            Object[] constants) {
        int parameterTypeCount = parameterTypes == null ? 0 : parameterTypes.length;
        int constantCount = constants == null ? 0 : constants.length;
        if (parameterTypeCount != constantCount) {
            throw new IllegalArgumentException(
                    ExceptionUtil.invalidConstantCountOfNewOperation(
                            parameterTypes.length,
                            constants.length
                    )
            );
        }
        if (parameterTypeCount == 0) {
            visitNewObjectWithoutParameters(mv, typeInternalName);
            return;
        }
        if (parameterTypeCount == 1) {
            visitNewObjectWithConstant(mv, typeInternalName, parameterTypes[0], constants[0]);
            return;
        }
        
        StringBuilder parametersDesc = new StringBuilder();
        for (Class<?> parameterType : parameterTypes) {
            parametersDesc.append(getDescriptor(parameterType));
        }
        mv.visitTypeInsn(Opcodes.NEW, typeInternalName);
        for (Object constant : constants) {
            mv.visitLdcInsn(constant);
        }
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                typeInternalName, 
                "<init>", 
                "(" + parametersDesc.toString() + ")V",
                false);
    }
    
    public static void visitEquals(
            MethodVisitor mv, 
            String descriptor, 
            boolean nullable,
            Consumer<MethodVisitor> equalAction) {
        ExceptionUtil.mustNotBeNull("descriptor", descriptor);
        ExceptionUtil.mustNotBeEmpty("descriptor", descriptor);
        ExceptionUtil.mustNotBeEqualToValue("descriptor", descriptor, "V");
        
        if ("Z".equals(descriptor) ||
                "C".equals(descriptor) ||
                "B".equals(descriptor) ||
                "S".equals(descriptor) ||
                "I".equals(descriptor)) {
            Label notEqualLabel = new Label();
            mv.visitJumpInsn(Opcodes.IF_ICMPNE, notEqualLabel);
            equalAction.accept(mv);
            mv.visitLabel(notEqualLabel);
        } else if ("J".equals(descriptor)) {
            Label notEqualLabel = new Label();
            mv.visitInsn(Opcodes.LCMP);
            mv.visitJumpInsn(Opcodes.IFNE, notEqualLabel);
            equalAction.accept(mv);
            mv.visitLabel(notEqualLabel);
        } else if ("F".equals(descriptor)) {
            Label notEqualLabel = new Label();
            mv.visitInsn(Opcodes.FCMPL);
            mv.visitJumpInsn(Opcodes.IFNE, notEqualLabel);
            equalAction.accept(mv);
            mv.visitLabel(notEqualLabel);
        } else if ("D".equals(descriptor)) {
            Label notEqualLabel = new Label();
            mv.visitInsn(Opcodes.DCMPL);
            mv.visitJumpInsn(Opcodes.IFNE, notEqualLabel);
            equalAction.accept(mv);
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
                    ASM.getInternalName(Arrays.class), 
                    "equals", 
                    '(' + arrayDesc + arrayDesc + ")Z",
                    false
            );
            mv.visitJumpInsn(Opcodes.IFEQ, notEqualLabel);
            equalAction.accept(mv);
            mv.visitLabel(notEqualLabel);
        } else {
            Label notEqualLabel = new Label();
            if (nullable) {
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        ASM.getInternalName(Nulls.class), 
                        "equals", 
                        "(Ljava/lang/Object;Ljava/lang/Object;)Z",
                        false);
            } else {
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/Object", 
                        "equals", 
                        "(Ljava/lang/Object;)Z",
                        false);
            }
            mv.visitJumpInsn(Opcodes.IFEQ, notEqualLabel);
            equalAction.accept(mv);
            mv.visitLabel(notEqualLabel);
        }
    }
    
    public static void visitToString(MethodVisitor mv, Class<?> clazz) {
        ExceptionUtil.mustNotBeNull("clazz", clazz);
        ExceptionUtil.mustNotBeEqualToValue("clazz", clazz, void.class);
        if (clazz == boolean.class) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    Boolean.class.getName().replace('.', '/'), 
                    "toString", 
                    "(Z)Ljava/lang/String;",
                    false);
            return;
        }
        if (clazz == char.class) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    Character.class.getName().replace('.', '/'), 
                    "toString", 
                    "(C)Ljava/lang/String;",
                    false);
            return;
        }
        if (clazz == byte.class) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    Byte.class.getName().replace('.', '/'), 
                    "toString", 
                    "(B)Ljava/lang/String;",
                    false);
            return;
        }
        if (clazz == short.class) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    Short.class.getName().replace('.', '/'), 
                    "toString", 
                    "(S)Ljava/lang/String;",
                    false);
            return;
        }
        if (clazz == int.class) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    Integer.class.getName().replace('.', '/'), 
                    "toString", 
                    "(I)Ljava/lang/String;",
                    false);
            return;
        }
        if (clazz == long.class) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    Long.class.getName().replace('.', '/'), 
                    "toString", 
                    "(J)Ljava/lang/String;",
                    false);
            return;
        }
        if (clazz == float.class) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    Float.class.getName().replace('.', '/'), 
                    "toString", 
                    "(F)Ljava/lang/String;",
                    false);
            return;
        }
        if (clazz == double.class) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    Double.class.getName().replace('.', '/'), 
                    "toString", 
                    "(D)Ljava/lang/String;",
                    false);
            return;
        }
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                "java/lang/Object", 
                "toString", 
                "()Ljava/lang/String;",
                false);
    }
    
    public static void visitNullSafeToString(MethodVisitor mv, Class<?> clazz) {
        if (!clazz.isPrimitive()) {
            Label notNullLabel = new Label();
            Label endIfLabel = new Label();
            mv.visitInsn(Opcodes.DUP);
            mv.visitJumpInsn(Opcodes.IFNONNULL, notNullLabel);
            mv.visitInsn(Opcodes.POP);
            mv.visitLdcInsn("null");
            mv.visitJumpInsn(Opcodes.GOTO, endIfLabel);
            mv.visitLabel(notNullLabel);
            visitToString(mv, clazz);
            mv.visitLabel(endIfLabel);
        } else {
            visitToString(mv, clazz);
        }
    }
    
    public static InsnList getBoxInsnList(Class<?> primitiveTypeDescriptor, AbstractInsnNode loadPrimtiveInsnNode) {
        InsnList tmpList = new InsnList();
        tmpList.add(loadPrimtiveInsnNode);
        return getBoxInsnList(primitiveTypeDescriptor, tmpList);
    }
    
    public static InsnList getBoxInsnList(String primitiveTypeDescriptor, AbstractInsnNode loadPrimtiveInsnNode) {
        InsnList tmpList = new InsnList();
        tmpList.add(loadPrimtiveInsnNode);
        return getBoxInsnList(primitiveTypeDescriptor, tmpList);
    }
    
    public static InsnList getBoxInsnList(org.babyfish.org.objectweb.asm.Type primitiveType, AbstractInsnNode loadPrimtiveInsnNode) {
        InsnList tmpList = new InsnList();
        tmpList.add(loadPrimtiveInsnNode);
        return getBoxInsnList(primitiveType.getDescriptor(), tmpList);
    }
    
    public static InsnList getBoxInsnList(Class<?> primitiveType, InsnList loadPrimtiveInsnNodeList) {
        if (!primitiveType.isPrimitive()) {
            return loadPrimtiveInsnNodeList;
        }
        ExceptionUtil.mustNotBeEqualToValue("primitiveType", primitiveType, void.class);
        Class<?> clazz = ClassInfo.box(primitiveType);
        String internalName = org.babyfish.org.objectweb.asm.Type.getInternalName(clazz);
        InsnList insnList = new InsnList();
        insnList.add(new TypeInsnNode(Opcodes.NEW, internalName));
        insnList.add(new InsnNode(Opcodes.DUP));
        insnList.add(loadPrimtiveInsnNodeList);
        insnList.add(
                new MethodInsnNode(
                        Opcodes.INVOKESPECIAL, 
                        internalName, 
                        "<init>", 
                        '(' + getDescriptor(primitiveType) + ")V",
                        false));
        return insnList;
    }
    
    public static InsnList getBoxInsnList(String primitiveType, InsnList loadPrimtiveInsnNodeList) {
        if ("Z".equals(primitiveType)) {
            return getBoxInsnList(boolean.class, loadPrimtiveInsnNodeList);
        }
        if ("C".equals(primitiveType)) {
            return getBoxInsnList(char.class, loadPrimtiveInsnNodeList);
        }
        if ("B".equals(primitiveType)) {
            return getBoxInsnList(byte.class, loadPrimtiveInsnNodeList);
        }
        if ("S".equals(primitiveType)) {
            return getBoxInsnList(short.class, loadPrimtiveInsnNodeList);
        }
        if ("I".equals(primitiveType)) {
            return getBoxInsnList(int.class, loadPrimtiveInsnNodeList);
        }
        if ("J".equals(primitiveType)) {
            return getBoxInsnList(long.class, loadPrimtiveInsnNodeList);
        }
        if ("F".equals(primitiveType)) {
            return getBoxInsnList(float.class, loadPrimtiveInsnNodeList);
        }
        if ("D".equals(primitiveType)) {
            return getBoxInsnList(double.class, loadPrimtiveInsnNodeList);
        }
        return getBoxInsnList(Object.class, loadPrimtiveInsnNodeList);
    }
    
    public static InsnList getBoxInsnList(org.babyfish.org.objectweb.asm.Type primitiveType, InsnList loadPrimtiveInsnNodeList) {
        return getBoxInsnList(primitiveType.getDescriptor(), loadPrimtiveInsnNodeList);
    }
    
    public static InsnList getUnboxInsnList(Class<?> primitiveType, boolean checkcast) {
        ExceptionUtil.mustNotBeEqualToValue("primitiveType", primitiveType, void.class);
        InsnList insnList = new InsnList();
        if (!primitiveType.isPrimitive()) {
            return insnList;
        }
        Class<?> clazz = ClassInfo.box(primitiveType);
        String internalName = org.babyfish.org.objectweb.asm.Type.getInternalName(clazz);
        if (checkcast) {
            insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, internalName));
        }
        insnList.add(
                new MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL, 
                        internalName, 
                        primitiveType.getName() + "Value", 
                        "()" + getDescriptor(primitiveType),
                        false));
        return insnList;
    }
    
    public static InsnList getUnboxInsnList(String primitiveTypeDescriptor, boolean checkcast) {
        if ("Z".equals(primitiveTypeDescriptor)) {
            return getUnboxInsnList(boolean.class, checkcast);
        }
        if ("C".equals(primitiveTypeDescriptor)) {
            return getUnboxInsnList(char.class, checkcast);
        }
        if ("B".equals(primitiveTypeDescriptor)) {
            return getUnboxInsnList(byte.class, checkcast);
        }
        if ("S".equals(primitiveTypeDescriptor)) {
            return getUnboxInsnList(short.class, checkcast);
        }
        if ("I".equals(primitiveTypeDescriptor)) {
            return getUnboxInsnList(int.class, checkcast);
        }
        if ("J".equals(primitiveTypeDescriptor)) {
            return getUnboxInsnList(long.class, checkcast);
        }
        if ("F".equals(primitiveTypeDescriptor)) {
            return getUnboxInsnList(float.class, checkcast);
        }
        if ("D".equals(primitiveTypeDescriptor)) {
            return getUnboxInsnList(double.class, checkcast);
        }
        return getUnboxInsnList(Object.class, checkcast);
    }
    
    public static InsnList getUnboxInsnList(org.babyfish.org.objectweb.asm.Type primitiveType, boolean checkcast) {
        return getUnboxInsnList(primitiveType.getDescriptor(), checkcast);
    }
    
    public static AbstractInsnNode getClassLdc(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return new FieldInsnNode(
                    Opcodes.GETSTATIC, 
                    org.babyfish.org.objectweb.asm.Type.getInternalName(ClassInfo.box(clazz)),
                    "TYPE",
                    "Ljava/lang/Class;");
        }
        return new LdcInsnNode(org.babyfish.org.objectweb.asm.Type.getType(clazz));
    }
    
    public static AbstractInsnNode getClassLdc(String descriptor) {
        if (descriptor.equals("Z")) {
            return new FieldInsnNode(
                    Opcodes.GETSTATIC, 
                    org.babyfish.org.objectweb.asm.Type.getInternalName(Boolean.class),
                    "TYPE",
                    "Ljava/lang/Class;");
        }
        if (descriptor.equals("C")) {
            return new FieldInsnNode(
                    Opcodes.GETSTATIC, 
                    org.babyfish.org.objectweb.asm.Type.getInternalName(Character.class),
                    "TYPE",
                    "Ljava/lang/Class;");
        }
        if (descriptor.equals("B")) {
            return new FieldInsnNode(
                    Opcodes.GETSTATIC, 
                    org.babyfish.org.objectweb.asm.Type.getInternalName(Byte.class),
                    "TYPE",
                    "Ljava/lang/Class;");
        }
        if (descriptor.equals("S")) {
            return new FieldInsnNode(
                    Opcodes.GETSTATIC, 
                    org.babyfish.org.objectweb.asm.Type.getInternalName(Short.class),
                    "TYPE",
                    "Ljava/lang/Class;");
        }
        if (descriptor.equals("I")) {
            return new FieldInsnNode(
                    Opcodes.GETSTATIC, 
                    org.babyfish.org.objectweb.asm.Type.getInternalName(Integer.class),
                    "TYPE",
                    "Ljava/lang/Class;");
        }
        if (descriptor.equals("J")) {
            return new FieldInsnNode(
                    Opcodes.GETSTATIC, 
                    org.babyfish.org.objectweb.asm.Type.getInternalName(Long.class),
                    "TYPE",
                    "Ljava/lang/Class;");
        }
        if (descriptor.equals("F")) {
            return new FieldInsnNode(
                    Opcodes.GETSTATIC, 
                    org.babyfish.org.objectweb.asm.Type.getInternalName(Float.class),
                    "TYPE",
                    "Ljava/lang/Class;");
        }
        if (descriptor.equals("D")) {
            return new FieldInsnNode(
                    Opcodes.GETSTATIC, 
                    org.babyfish.org.objectweb.asm.Type.getInternalName(Double.class),
                    "TYPE",
                    "Ljava/lang/Class;");
        }
        return new LdcInsnNode(org.babyfish.org.objectweb.asm.Type.getType(descriptor));
    }
    
    public static AbstractInsnNode getClassLdc(org.babyfish.org.objectweb.asm.Type type) {
        return getClassLdc(type.getDescriptor());
    }
    
    public static Class<?> loadDynamicClass(
            ClassLoader classLoader, 
            String className, 
            ProtectionDomain protectionDomain,
            Consumer<ClassVisitor> cvAction) {
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        Class<?> clazz;
        Lock lock;
        (lock = DYNAMIC_CLASSES_LOCK.readLock()).lock(); //1st locking
        try {
            clazz = findLoadedClass(classLoader, className);
        } finally {
            lock.unlock();
        }
        if (clazz == null) { //1st checking
            (lock = DYNAMIC_CLASSES_LOCK.writeLock()).lock(); //2nd locking
            try {
                clazz = findLoadedClass(classLoader, className);
                if (clazz == null) { //2nd checking
                    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                    cvAction.accept(cw);
                    clazz = defineClass(
                            classLoader, 
                            className, 
                            cw.toByteArray(), 
                            protectionDomain);
                }
            } finally {
                lock.unlock();
            }
        }
        return clazz;
    }
    
    private static Class<?> findLoadedClass(final ClassLoader classLoader, final String className) {
        return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
            @Override
            public Class<?> run() {
                try {
                    return (Class<?>)CLASS_LOADER_FIND_LOADED_CLASS.invoke(classLoader, className);
                } catch (IllegalArgumentException ex) {
                    throw new AssertionError("Impossible", ex);
                } catch (IllegalAccessException ex) {
                    throw new AssertionError("Impossible", ex);
                } catch (InvocationTargetException ex) {
                    throw new AssertionError("Impossible, when find " + className + ex.getTargetException());
                }
            }
        });
    }

    private static Class<?> defineClass(
            ClassLoader classLoader, 
            String className, 
            byte[] byteCode,
            ProtectionDomain protectionDomain) {
        return defineClass(
                classLoader, 
                className,
                byteCode,
                0,
                byteCode.length,
                protectionDomain);
    }
    
    private static Class<?> defineClass(
            final ClassLoader classLoader, 
            final String className, 
            final byte[] byteCode,
            final int offset,
            final int length,
            final ProtectionDomain protectionDomain) {
        return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
            @Override
            public Class<?> run() {
                try {
                    return (Class<?>)CLASS_LOADER_DEFINE_CLASS.invoke(
                            classLoader, 
                            className, 
                            byteCode,
                            offset,
                            length,
                            protectionDomain);
                } catch (IllegalArgumentException ex) {
                    throw new AssertionError("Impossible", ex);
                } catch (IllegalAccessException ex) {
                    throw new AssertionError("Impossible", ex);
                } catch (InvocationTargetException ex) {
                    throw new AssertionError("Impossible, when load " + className, ex.getTargetException());
                }
            }
        });
    }
    
    private static class XMethodVisitorImpl extends XMethodVisitor implements SlotAllocator {
        
        private int[] paramSlots;
        
        private SlotAllocator slotAllocator;
        
        public XMethodVisitorImpl(
                ClassVisitor cv, 
                int access,
                String name,
                String desc,
                String signature,
                String[] exceptions) {
            super(Opcodes.ASM5);
            this.slotAllocator = new SlotAllocatatorImpl(calcBaseSlot(access, desc));
            this.mv = cv.visitMethod(access, name, desc, signature, exceptions);
            this.paramSlots = MACollections.toIntArray(paramSlots(access, desc));
        }
        
        @Override
        public int paramSlot(int paramIndex) {
            return this.paramSlots[paramIndex];
        }

        @Override
        public void visitTryCatchBlock(
                Consumer<XMethodVisitor> tryBlock,
                Catch ... catches) {
            ExceptionUtil.mustNotBeEmpty("catches", catches);
            Label tryLabel = new Label();
            XOrderedMap<String, Label> catchLabels = new LinkedHashMap<>();
            XOrderedMap<String, Consumer<XMethodVisitor>> catchActions = new LinkedHashMap<>();
            Label endLabel = new Label();
            for (int i = 0; i < catches.length; i++) {
                for (String exceptionInternalName : catches[i].getExceptionInternalNames()) {
                    catchLabels.put(exceptionInternalName, new Label());
                    catchActions.put(exceptionInternalName, catches[i].getMvAction());
                }
            }
            
            MethodVisitor innerMV = this.mv;
            for (Entry<String, Label> entry : catchLabels.entrySet()) {
                innerMV.visitTryCatchBlock(
                        tryLabel, 
                        catchLabels.firstEntry().getValue(), 
                        entry.getValue(), 
                        entry.getKey());
            }
            
            innerMV.visitLabel(tryLabel);
            tryBlock.accept(this);
            innerMV.visitJumpInsn(Opcodes.GOTO, endLabel);
            
            for (Entry<String, Label> entry : catchLabels.entrySet()) {
                innerMV.visitLabel(entry.getValue());
                catchActions.get(entry.getKey()).accept(this);
                innerMV.visitJumpInsn(Opcodes.GOTO, endLabel);
            }
            
            innerMV.visitLabel(endLabel);
        }
        
        @Override
        public void visitTryFinally(
                Consumer<MethodVisitor> tryAction,
                Consumer<MethodVisitor> finallyAction) {
            MethodVisitor mv = this.mv;
            Label tryLabel = new Label();
            Label catchLabel = new Label();
            Label finallyLabel = new Label();
            final int temporaryExceptionForFinallyIndex = this.aSlot("temproaryExceptionForFinallyIndex:92B8C17E_BF4E_4135_B596_5A76E0FEBF4E");
            
            mv.visitTryCatchBlock(tryLabel, catchLabel, catchLabel, null);
            mv.visitLabel(tryLabel);
            tryAction.accept(mv);
            mv.visitJumpInsn(Opcodes.GOTO, finallyLabel);
            mv.visitLabel(catchLabel);
            mv.visitVarInsn(Opcodes.ASTORE, temporaryExceptionForFinallyIndex);
            finallyAction.accept(mv);
            mv.visitVarInsn(Opcodes.ALOAD, temporaryExceptionForFinallyIndex);
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitLabel(finallyLabel);
            finallyAction.accept(mv);
        }

        @Override
        public void visitHashCode(Class<?> clazz, boolean nullable) {
            // This method should NOT generate any "Opcodes.IRETURN"
            ExceptionUtil.mustNotBeNull("clazz", clazz);
            ExceptionUtil.mustNotBeEqualToValue("clazz", clazz, void.class);
            if (clazz == char.class || clazz == byte.class || clazz == short.class || clazz == int.class) {
                return;
            }
            MethodVisitor mv = this.mv;
            
            if (clazz == boolean.class) {
                Label ifFalseLabel = new Label();
                Label endIfLabel = new Label();
                mv.visitJumpInsn(Opcodes.IFEQ, ifFalseLabel);
                mv.visitLdcInsn(1231);
                mv.visitJumpInsn(Opcodes.GOTO, endIfLabel);
                mv.visitLabel(ifFalseLabel);
                mv.visitLdcInsn(1237);
                mv.visitLabel(endIfLabel);
            } else if (clazz == long.class) {
                mv.visitInsn(Opcodes.DUP2);
                mv.visitLdcInsn(32);
                mv.visitInsn(Opcodes.LUSHR);
                mv.visitInsn(Opcodes.LXOR);
                mv.visitInsn(Opcodes.L2I);
            } else if (clazz == float.class) {
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        Float.class.getName().replace('.', '/'), 
                        "floatToIntBits", 
                        "(F)I",
                        false);
            } else if (clazz == double.class) {
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        Double.class.getName().replace('.', '/'), 
                        "doubleToLongBits", 
                        "(D)J",
                        false);
                this.visitHashCode(long.class, false);
            } else {
                Label endAllLabel = null;
                if (nullable) {
                    endAllLabel = new Label();
                    Label isNotNullLabel = new Label();
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitJumpInsn(Opcodes.IFNONNULL, isNotNullLabel);
                    mv.visitInsn(Opcodes.POP);
                    mv.visitInsn(Opcodes.ICONST_0);
                    mv.visitJumpInsn(Opcodes.GOTO, endAllLabel);
                    mv.visitLabel(isNotNullLabel);
                }
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/Object", 
                        "hashCode", 
                        "()I",
                        false);
                if (nullable) {
                    mv.visitLabel(endAllLabel);
                }
            }
        }

        @Override
        public void visitEquals(Class<?> clazz, boolean nullable) {
            // This method should NOT generate any "Opcodes.IRETURN"
            ExceptionUtil.mustNotBeNull("clazz", clazz);
            ExceptionUtil.mustNotBeEqualToValue("clazz", clazz, void.class);
            MethodVisitor mv = this.mv;
            if (clazz == boolean.class ||
                    clazz == char.class ||
                    clazz == byte.class ||
                    clazz == short.class ||
                    clazz == int.class) {
                Label eqLabel = new Label();
                Label endIfLabel = new Label();
                mv.visitJumpInsn(Opcodes.IF_ICMPEQ, eqLabel);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitJumpInsn(Opcodes.GOTO, endIfLabel);
                mv.visitLabel(eqLabel);
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitLabel(endIfLabel);
            } else if (clazz == long.class) {
                Label eqLabel = new Label();
                Label endIfLabel = new Label();
                mv.visitInsn(Opcodes.LCMP);
                mv.visitJumpInsn(Opcodes.IFEQ, eqLabel);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitJumpInsn(Opcodes.GOTO, endIfLabel);
                mv.visitLabel(eqLabel);
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitLabel(endIfLabel);
            } else if (clazz == float.class) {
                Label eqLabel = new Label();
                Label endIfLabel = new Label();
                mv.visitInsn(Opcodes.FCMPL);
                mv.visitJumpInsn(Opcodes.IFEQ, eqLabel);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitJumpInsn(Opcodes.GOTO, endIfLabel);
                mv.visitLabel(eqLabel);
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitLabel(endIfLabel);
            } else if (clazz == double.class) {
                Label eqLabel = new Label();
                Label endIfLabel = new Label();
                mv.visitInsn(Opcodes.DCMPL);
                mv.visitJumpInsn(Opcodes.IFEQ, eqLabel);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitJumpInsn(Opcodes.GOTO, endIfLabel);
                mv.visitLabel(eqLabel);
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitLabel(endIfLabel);
            } else {
                if (nullable) {
                    mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC, 
                            ASM.getInternalName(Nulls.class), 
                            "equals", 
                            "(Ljava/lang/Object;Ljava/lang/Object;)Z",
                            false);
                } else {
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            "java/lang/Object", 
                            "equals", 
                            "(Ljava/lang/Object;)Z",
                            false);
                }
            }
        }

        @Override
        public void visitCompare(Class<?> clazz, Boolean nullsLast) {
            // This method should NOT generate any "Opcodes.IRETURN"
            ExceptionUtil.mustNotBeNull("clazz", clazz);
            ExceptionUtil.mustNotBeEqualToValue("clazz", clazz, void.class);
            Label endAllLabel = new Label();
            MethodVisitor mv = this.mv;
            if (clazz == boolean.class ||
                    clazz == char.class ||
                    clazz == byte.class ||
                    clazz == short.class ||
                    clazz == int.class) {
                mv.visitInsn(Opcodes.ISUB);
            } else if (clazz == long.class) {
                mv.visitInsn(Opcodes.LCMP);
            } else if (clazz == long.class) {
                mv.visitInsn(Opcodes.LCMP);
            } else if (clazz == float.class) {
                mv.visitInsn(Opcodes.FCMPL);
            } else if (clazz == double.class) {
                mv.visitInsn(Opcodes.DCMPL);
            } else {
                if (nullsLast != null) {
                    mv.visitLdcInsn(nullsLast);
                    mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC, 
                            ASM.getInternalName(Nulls.class), 
                            "compare", 
                            "(Ljava/lang/Object;Ljava/lang/Object;Z)I",
                            false);
                } else {
                    mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC, 
                            ASM.getInternalName(Nulls.class), 
                            "compare", 
                            "(Ljava/lang/Object;Ljava/lang/Object;)I",
                            false);
                }
            }
            mv.visitLabel(endAllLabel);
        }

        @Override
        public void visitBox(Class<?> clazz, Consumer<XMethodVisitor> action) {
            ExceptionUtil.mustNotBeNull("clazz", clazz);
            ExceptionUtil.mustNotBeNull("action", action);
            if (!clazz.isPrimitive()) {
                action.accept(this);
                return;
            }
            Class<?> boxClass;
            String desc;
            if (clazz == boolean.class) {
                boxClass = Boolean.class;
                desc = "(Z)V";
            } else if (clazz == char.class) {
                boxClass = Character.class;
                desc = "(C)V";
            } else if (clazz == byte.class) {
                boxClass = Byte.class;
                desc = "(B)V";
            } else if (clazz == short.class) {
                boxClass = Short.class;
                desc = "(S)V";
            } else if (clazz == int.class) {
                boxClass = Integer.class;
                desc = "(I)V";
            } else if (clazz == long.class) {
                boxClass = Long.class;
                desc = "(J)V";
            } else if (clazz == float.class) {
                boxClass = Float.class;
                desc = "(F)V";
            } else if (clazz == double.class) {
                boxClass = Double.class;
                desc = "(D)V";
            } else {
                throw new AssertionError(
                        "The primitive \"" +
                        clazz.getName() +
                        "\" is not supported, maybe the version of babyfish is too low.");
            }
            String internalName = boxClass.getName().replace('.', '/');
            mv.visitTypeInsn(Opcodes.NEW, internalName);
            mv.visitInsn(Opcodes.DUP);
            action.accept(this);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, internalName, "<init>", desc, false);
        }

        @Override
        public void visitUnbox(Class<?> clazz, Object nullDefaultValue) {
            ExceptionUtil.mustNotBeNull("clazz", clazz);
            if (!clazz.isPrimitive()) {
                return;
            }
            Label endLabel = null;
            if (nullDefaultValue != null) {
                if (nullDefaultValue != JVM_PRIMTIVIE_DEFAULT_VALUE &&
                        nullDefaultValue.getClass() != ClassInfo.box(clazz, true)) {
                    ExceptionUtil.mustBeInstanceOf("nullDefaultValue", nullDefaultValue, clazz);
                }
                Label notNullLbl = new Label();
                endLabel = new Label();
                mv.visitInsn(Opcodes.DUP);
                mv.visitJumpInsn(Opcodes.IFNONNULL, notNullLbl);
                
                mv.visitInsn(Opcodes.POP);
                if (nullDefaultValue == XMethodVisitor.JVM_PRIMTIVIE_DEFAULT_VALUE) {
                    mv.visitInsn(ASM.getDefaultCode(clazz));
                } else {
                    mv.visitLdcInsn(nullDefaultValue);
                }
                
                mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                mv.visitLabel(notNullLbl);
            }
            if (boolean.class == clazz) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/Boolean", 
                        "booleanValue", 
                        "()Z",
                        false);
            } else if (char.class == clazz) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/Character", 
                        "charValue", 
                        "()C",
                        false);
            } else if (byte.class == clazz) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Byte");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/Byte", 
                        "byteValue", 
                        "()B",
                        false);
            } else if (short.class == clazz) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Short");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/Short", 
                        "shortValue", 
                        "()S",
                        false);
            } else if (int.class == clazz) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/Integer", 
                        "intValue", 
                        "()I",
                        false);
            } else if (long.class == clazz) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/Long", 
                        "longValue", 
                        "()J",
                        false);
            } else if (float.class == clazz) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/Float", 
                        "floatValue", 
                        "()F",
                        false);
            } else if (double.class == clazz) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/Double", 
                        "doubleValue", 
                        "()D",
                        false);
            }
            if(endLabel != null) {
                mv.visitLabel(endLabel);
            }
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            this.mv.visitVarInsn(opcode, var);
        }
        
        public void visitAStoreInsnIfNull(int varindex) {
            MethodVisitor mv = this.mv;
            Label isNullLabel = new Label();
            Label endIfLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, varindex);
            mv.visitJumpInsn(Opcodes.IFNULL, isNullLabel);
            mv.visitInsn(Opcodes.POP);
            mv.visitJumpInsn(Opcodes.GOTO, endIfLabel);
            mv.visitLabel(isNullLabel);
            mv.visitVarInsn(Opcodes.ASTORE, varindex);
            mv.visitLabel(endIfLabel);
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return this.mv.visitAnnotationDefault();
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return this.mv.visitAnnotation(desc, visible);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter,
                String desc, boolean visible) {
            return this.mv.visitParameterAnnotation(parameter, desc, visible);
        }

        @Override
        public void visitAttribute(Attribute attr) {
            this.mv.visitAttribute(attr);
        }

        @Override
        public void visitCode() {
            this.mv.visitCode();
        }

        @Override
        public void visitFrame(
                int type, 
                int nLocal, 
                Object[] local,
                int nStack, 
                Object[] stack) {
            //this.mv.visitFrame(type, nLocal, local, nStack, stack);
        }

        @Override
        public void visitInsn(int opcode) {
            this.mv.visitInsn(opcode);
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            this.mv.visitIntInsn(opcode, operand);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            this.mv.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name,
                String desc) {
            this.mv.visitFieldInsn(opcode, owner, name, desc);
        }
        
        @Deprecated
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            this.mv.visitMethodInsn(opcode, owner, name, desc);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            this.mv.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitLabel(Label label) {
            this.mv.visitLabel(label);
        }

        @Override
        public void visitLdcInsn(Object cst) {
            this.mv.visitLdcInsn(cst);
        }

        @Override
        public void visitIincInsn(int var, int increment) {
            this.mv.visitIincInsn(var, increment);
        }

        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label ... labels) {
            this.mv.visitTableSwitchInsn(min, max, dflt, labels);
        }

        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            this.mv.visitLookupSwitchInsn(dflt, keys, labels);
        }

        @Override
        public void visitMultiANewArrayInsn(String desc, int dims) {
            this.mv.visitMultiANewArrayInsn(desc, dims);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            this.mv.visitTryCatchBlock(start, end, handler, type);
        }

        @Override
        public void visitLocalVariable(String name, String desc,
                String signature, Label start, Label end, int index) {
            this.mv.visitLocalVariable(name, desc, signature, start, end, index);
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            this.mv.visitLineNumber(line, start);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            this.mv.visitMaxs(maxStack, maxLocals);
        }

        @Override
        public void visitEnd() {
            this.mv.visitEnd();
        }
        
        private static int calcBaseSlot(int access, String desc) {
            int baseSlot = (access & Opcodes.ACC_STATIC) == 0 ? 1 : 0;
            int len = desc.length();
            List<Integer> list = new ArrayList<Integer>();
            boolean inArr = false;
            for (int i = 0; i < len; i++) {
                char c = desc.charAt(i);
                if (c == '[') {
                    inArr = true;
                } else {
                    switch (c) {
                    case '(':
                        break;
                    case ')':
                        i = len;
                        break;
                        
                    case 'J':
                    case 'D':
                        list.add(baseSlot);
                        baseSlot += inArr ? 1 : 2;
                        break;
                        
                    case 'L':
                        i = desc.indexOf(';', i + 1);
                        if (i == -1) {
                            throw new IllegalArgumentException(
                                    ExceptionUtil.invalidMethodDescriptor("desc", desc)
                            );
                        }
                    case 'Z':
                    case 'C':
                    case 'B':
                    case 'S':
                    case 'I':
                    case 'F':
                        list.add(baseSlot++);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                ExceptionUtil.invalidMethodDescriptor("desc", desc)
                        );
                    }
                    inArr = false;
                }
            }
            return baseSlot;
        }
        
        private static List<Integer> paramSlots(int access, String desc) {
            int baseSlot = (access & Opcodes.ACC_STATIC) == 0 ? 1 : 0;
            int len = desc.length();
            List<Integer> list = new ArrayList<Integer>();
            boolean inArr = false;
            for (int i = 0; i < len; i++) {
                char c = desc.charAt(i);
                if (c == '[') {
                    inArr = true;
                } else {
                    switch (c) {
                    case '(':
                        break;
                    case ')':
                        i = len;
                        break;
                        
                    case 'J':
                    case 'D':
                        list.add(baseSlot);
                        baseSlot += inArr ? 1 : 2;
                        break;
                        
                    case 'L':
                        i = desc.indexOf(';', i + 1);
                        if (i == -1) {
                            throw new IllegalArgumentException(
                                    ExceptionUtil.invalidMethodDescriptor("desc", desc)
                            );
                        }
                    case 'Z':
                    case 'C':
                    case 'B':
                    case 'S':
                    case 'I':
                    case 'F':
                        list.add(baseSlot++);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                ExceptionUtil.invalidMethodDescriptor("desc", desc)
                        );
                    }
                    inArr = false;
                }
            }
            return list;
        }

        @Override
        public int iSlot(String variableName) {
            return this.slotAllocator.iSlot(variableName);
        }

        @Override
        public int lSlot(String variableName) {
            return this.slotAllocator.lSlot(variableName);
        }

        @Override
        public int fSlot(String variableName) {
            return this.slotAllocator.fSlot(variableName);
        }

        @Override
        public int dSlot(String variableName) {
            return this.dSlot(variableName);
        }

        @Override
        public int aSlot(String variableName) {
            return this.slotAllocator.aSlot(variableName);
        }

        @Override
        public int slot(String variableName, Class<?> variableType) {
            return this.slotAllocator.slot(variableName, variableType);
        }
    }
    
    private static class SlotAllocatatorImpl implements SlotAllocator {
        
        private static final int WIDE = 1 << 7;
        
        private static final int TYPE_I = 0;
        
        private static final int TYPE_L = 1 | WIDE;
        
        private static final int TYPE_F = 2;
        
        private static final int TYPE_D = 3 | WIDE;
        
        private static final int TYPE_A = 4;
        
        private Map<String, Slot> slots;
        
        private int currentSlot;
        
        SlotAllocatatorImpl(int baseSlot) {
            this.slots = new HashMap<>();
            this.currentSlot = baseSlot;
        }
        
        @Override
        public int iSlot(String variableName) {
            return this.slot(variableName, TYPE_I);
        }


        @Override
        public int lSlot(String variableName) {
            return this.slot(variableName, TYPE_L);
        }


        @Override
        public int fSlot(String variableName) {
            return this.slot(variableName, TYPE_F);
        }


        @Override
        public int dSlot(String variableName) {
            return this.slot(variableName, TYPE_D);
        }


        @Override
        public int aSlot(String variableName) {
            return this.slot(variableName, TYPE_A);
        }

        @Override
        public int slot(String variableName, Class<?> variableType) {
            if (variableType == long.class) {
                return slot(variableName, TYPE_L);
            }
            if (variableType == double.class) {
                return slot(variableName, TYPE_D);
            }
            if (variableType == float.class) {
                return slot(variableName, TYPE_F);
            }
            if (variableType.isPrimitive()) {
                return slot(variableName, TYPE_I);
            }
            return slot(variableName, TYPE_A);
        }

        private int slot(String variableName, int variableType) {
            Map<String, Slot> slots = this.slots;
            Slot slot = slots.get(variableName);
            if (slot != null) {
                if (slot.type != variableType) {
                    throw new IllegalStateException();
                }
            } else {
                slot = new Slot(this.currentSlot, variableType);
                slots.put(variableName, slot);
                if ((variableType & WIDE) != 0) {
                    this.currentSlot += 2;
                } else {
                    this.currentSlot++;
                }
            }
            return slot.index;
        }
        
        private static class Slot {
            int index;
            int type;
            Slot(int index, int type) {
                this.index = index;
                this.type = type;
            }
        }
    }
    
    /*
     * This class can not use org.babyfish.lang.Arguments, so use this class
     */
    private static class ExceptionUtil {
        
        private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(ExceptionUtil.class.getName());
        
        static void mustNotBeNull(String parameterName, Object argument) {
            if (argument == null) {
                throw new NullArgumentException(get("mustNotBeNull", parameterName));
            }
        }
        
        static void mustNotBeEmpty(String parameterName, String argument) {
            if (argument != null && argument.isEmpty()) {
                throw new IllegalArgumentException(get("mustNotBeEmpty", parameterName));
            }
        }
        
        static <T> void mustNotBeEmpty(String parameterName, T[] argument) {
            if (argument != null && argument.length == 0) {
                throw new IllegalArgumentException(get("mustNotBeEmpty", parameterName));
            }
        }
        
        static void mustNotBeEqualToValue(String parameterName, Object argument, Object value) {
            if (argument != null && argument.equals(value)) {
                throw new IllegalArgumentException(get("mustNotBeEqualToValue", parameterName, value));
            }
        }
        
        static String invalidMethodDescriptor(String parameterName, String argument) {
            return get("badMethodDescriptor", parameterName, argument);
        }
        
        static String invalidConstantCountOfNewOperation(int expectedParameterType, int actualArgumentCount) {
            return get("invalidConstantCountOfNewOperation", expectedParameterType, actualArgumentCount); 
        }
        
        static void mustBeInstanceOf(
                String parameterName, 
                Object argument, 
                Class<?> classValue) {
            if (!classValue.isAssignableFrom(argument.getClass())) {
                throw new IllegalArgumentException(
                        get("mustBeInstanceOf", parameterName, argument, classValue.getName())
                );
            }
        }

        private static String get(String key, Object ... args) {
            int size = args.length;
            String value = BUNDLE.getString(key);
            for (int i = 0; i < size; i++) {
                Object o = args[i];
                value = value.replaceAll("\\{" + i + "\\}", o != null ? o.toString() : "null");
            }
            return value;
        }
    }
    
    static {
        Method method = AccessController.doPrivileged(new PrivilegedAction<Method>() {
            @Override
            public Method run() {
                try {
                    return ClassLoader.class.getDeclaredMethod(
                            "defineClass", 
                            String.class, 
                            byte[].class, 
                            int.class, 
                            int.class, 
                            ProtectionDomain.class);
                } catch (NoSuchMethodException ex) {
                    throw new AssertionError("Bad JDK", ex);
                }
            }
        });
        method.setAccessible(true);
        CLASS_LOADER_DEFINE_CLASS = method;
        
        method = AccessController.doPrivileged(new PrivilegedAction<Method>() {
            @Override
            public Method run() {
                try {
                    return ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
                } catch (NoSuchMethodException ex) {
                    throw new AssertionError("Bad JDK", ex);
                }
            }
        });
        method.setAccessible(true);
        CLASS_LOADER_FIND_LOADED_CLASS = method;
    }
}
