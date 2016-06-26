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

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.UncheckedException;
import org.babyfish.org.objectweb.asm.Attribute;
import org.babyfish.org.objectweb.asm.ClassReader;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.org.objectweb.asm.tree.AbstractInsnNode;
import org.babyfish.org.objectweb.asm.tree.ClassNode;
import org.babyfish.org.objectweb.asm.tree.FieldInsnNode;
import org.babyfish.org.objectweb.asm.tree.FieldNode;
import org.babyfish.org.objectweb.asm.tree.FrameNode;
import org.babyfish.org.objectweb.asm.tree.IincInsnNode;
import org.babyfish.org.objectweb.asm.tree.InsnList;
import org.babyfish.org.objectweb.asm.tree.InsnNode;
import org.babyfish.org.objectweb.asm.tree.IntInsnNode;
import org.babyfish.org.objectweb.asm.tree.JumpInsnNode;
import org.babyfish.org.objectweb.asm.tree.LabelNode;
import org.babyfish.org.objectweb.asm.tree.LdcInsnNode;
import org.babyfish.org.objectweb.asm.tree.LineNumberNode;
import org.babyfish.org.objectweb.asm.tree.LocalVariableNode;
import org.babyfish.org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.babyfish.org.objectweb.asm.tree.MethodInsnNode;
import org.babyfish.org.objectweb.asm.tree.MethodNode;
import org.babyfish.org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.babyfish.org.objectweb.asm.tree.TableSwitchInsnNode;
import org.babyfish.org.objectweb.asm.tree.TryCatchBlockNode;
import org.babyfish.org.objectweb.asm.tree.TypeInsnNode;
import org.babyfish.org.objectweb.asm.tree.VarInsnNode;
import org.babyfish.util.reflect.ClassInfo;
import org.babyfish.util.reflect.NoSuchClassException;

/**
 * @author Tao Chen
 */
public abstract class ClassEnhancer {
    
    private static final Map<Class<?>, ClassEnhancer> CACHE = new WeakHashMap<>();
    
    private static final ReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();
    
    private Class<?> clazz;
    
    private String resultClassName;
    
    private String resultInternalName;
    
    private Class<?> resultClass;

    protected ClassEnhancer(Class<?> clazz) {
        Arguments.mustNotBeNull("clazz", clazz);
        Arguments.mustBeClass("clazz", clazz);
        Arguments.mustNotBeFinal("clazz", clazz);
        this.clazz = clazz;
    }
    
    @SuppressWarnings("unchecked")
    protected static <T extends ClassEnhancer> T getInstance(Class<T> enhancerClass) {
        
        Lock lock;
        ClassEnhancer classEnhancer;
        
        (lock = CACHE_LOCK.readLock()).lock(); //1st locking
        try {
            classEnhancer = CACHE.get(enhancerClass); //1st reading
        } finally {
            lock.unlock();
        }
        
        if (classEnhancer == null) {
            (lock = CACHE_LOCK.writeLock()).lock(); //2nd locking
            try {
                classEnhancer = CACHE.get(enhancerClass); //2nd reading
                if (classEnhancer == null) {
                    ClassEnhancer instance = getInstance0(enhancerClass);
                    Context context = instance.new Context();
                    instance.resultClassName = context.newClassName;
                    instance.resultInternalName = context.newInternalName;
                    instance.resultClass = context.getResultClass();
                    classEnhancer = instance;
                    CACHE.put(enhancerClass, classEnhancer);
                }
            } finally {
                lock.unlock();
            }
        }
        return (T)classEnhancer;
    }
    
    protected final Class<?> getOriginalClass() {
        return this.clazz;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <T> Class<T> getResultClass() {
        Class<?> resultClass = this.resultClass;
        if (resultClass == null) {
            throw new IllegalStateException(accessFieldTooEarly(this.getClass(), "resultClass"));
        }
        return (Class)resultClass;
    }
    
    protected final String getResultClassName() {
        String resultClassName = this.resultClassName;
        if (resultClassName == null) {
            throw new IllegalStateException(accessFieldTooEarly(this.getClass(), "resultClassName"));
        }
        return resultClassName;
    }
    
    protected final String getResultInternalName() {
        String resultInternalName = this.resultInternalName;
        if (resultInternalName == null) {
            throw new IllegalProgramException(accessFieldTooEarly(this.getClass(), "resultInternalName"));
        }
        return resultInternalName;
    }

    protected boolean usedEnhancerLoader() {
        return false;
    }
    
    protected void doMethodFilter(MethodSource methodSource) {
        
    }
    
    protected boolean hideConstructor(Constructor<?> superConstructor) {
        return false;
    }
    
    protected InsnList getMoreConstructorInstructions(Constructor<?> superConstructor) {
        return null;
    }
    
    protected InsnList getMoreStaticBlockInstructions() {
        return null;
    }
    
    protected Collection<FieldNode> getMoreFieldNodes() {
        return null;
    }
    
    protected Collection<MethodNode> getMoreMethodNodes(MethodSourceFactory methodSourceFactory) {
        return null;
    }
    
    protected Collection<String> getInterfaces() {
        return null;
    }
    
    protected static MethodNode createMethodNode(
            int access, 
            String name, 
            String desc, 
            String signatrue, 
            String ... exceptions) {
        ArrayList<String> exceptionList = new ArrayList<String>();
        for (String exception : exceptions) {
            exceptionList.add(exception);
        }
        MethodNode methodNode = new MethodNode(Opcodes.ASM5);
        methodNode.access = access;
        methodNode.name = name;
        methodNode.desc = desc;
        methodNode.signature = signatrue;
        methodNode.exceptions = exceptionList;
        methodNode.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
        methodNode.localVariables = new ArrayList<LocalVariableNode>();
        return methodNode;
    }

    protected static MethodNode createMethodNode(
            int access, String name, String desc, String signature, List<String> exceptions) {
        MethodNode methodNode = new MethodNode(Opcodes.ASM5);
        methodNode.access = access;
        methodNode.name = name;
        methodNode.desc = desc;
        methodNode.signature = signature;
        methodNode.exceptions = exceptions != null ? new ArrayList<String>() : new ArrayList<String>();
        methodNode.localVariables = new ArrayList<LocalVariableNode>();
        methodNode.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
        return methodNode;
    }
    
    @SuppressWarnings("unchecked")
    protected static <N extends AbstractInsnNode> N cloneInsnNode(N abstractInsnNode) {
        if (abstractInsnNode instanceof FieldInsnNode) {
            FieldInsnNode old = (FieldInsnNode)abstractInsnNode;
            return (N)new FieldInsnNode(
                    old.getOpcode(), 
                    old.owner, 
                    old.name, 
                    old.desc);
        }
        if (abstractInsnNode instanceof FrameNode) {
            FrameNode old = (FrameNode)abstractInsnNode;
            return (N)new FrameNode(
                    old.type, 
                    old.local != null ? old.local.size() : 0, 
                    old.local != null ? old.local.toArray() : null,
                    old.stack != null ? old.stack.size() : 0, 
                    old.stack != null ? old.stack.toArray() : null);
        }
        if (abstractInsnNode instanceof IincInsnNode) {
            IincInsnNode old = (IincInsnNode)abstractInsnNode;
            return (N)new IincInsnNode(old.var, old.incr);
        }
        if (abstractInsnNode instanceof InsnNode) {
            return (N)new InsnNode(abstractInsnNode.getOpcode());
        }
        if (abstractInsnNode instanceof IntInsnNode) {
            IntInsnNode old = (IntInsnNode)abstractInsnNode;
            return (N)new IntInsnNode(old.getOpcode(), old.operand);
        }
        if (abstractInsnNode instanceof JumpInsnNode) {
            JumpInsnNode old = (JumpInsnNode)abstractInsnNode;
            return (N)new JumpInsnNode(old.getOpcode(), cloneInsnNode(old.label));
        }
        if (abstractInsnNode instanceof LabelNode) {
            LabelNode old = (LabelNode)abstractInsnNode;
            return (N)new LabelNode(old.getLabel());
        }
        if (abstractInsnNode instanceof LdcInsnNode) {
            LdcInsnNode old = (LdcInsnNode)abstractInsnNode;
            return (N)new LdcInsnNode(old.cst);
        }
        if (abstractInsnNode instanceof LineNumberNode) {
            LineNumberNode old = (LineNumberNode)abstractInsnNode;
            return (N)new LineNumberNode(old.line, cloneInsnNode(old.start));
        }
        if (abstractInsnNode instanceof LookupSwitchInsnNode) {
            LookupSwitchInsnNode old = (LookupSwitchInsnNode)abstractInsnNode;
            int[] keys = new int[old.keys.size()];
            for (int i = keys.length - 1; i >= 0; i--) {
                keys[i] = (Integer)old.keys.get(i);
            }
            LabelNode[] labels = new LabelNode[old.labels.size()];
            for (int i = labels.length - 1; i >= 0; i--) {
                labels[i] = cloneInsnNode((LabelNode)old.labels.get(i));
            }
            return (N)new LookupSwitchInsnNode(
                    cloneInsnNode(old.dflt), 
                    keys, 
                    labels);
        }
        if (abstractInsnNode instanceof MethodInsnNode) {
            MethodInsnNode old = (MethodInsnNode)abstractInsnNode;
            return (N)new MethodInsnNode(
                    old.getOpcode(), 
                    old.owner, 
                    old.name, 
                    old.desc,
                    old.itf);
        }
        if (abstractInsnNode instanceof MultiANewArrayInsnNode) {
            MultiANewArrayInsnNode old = (MultiANewArrayInsnNode)abstractInsnNode;
            return (N)new MultiANewArrayInsnNode(old.desc, old.dims);
        }
        if (abstractInsnNode instanceof TableSwitchInsnNode) {
            TableSwitchInsnNode old = (TableSwitchInsnNode)abstractInsnNode;
            LabelNode[] labels = new LabelNode[old.labels.size()];
            for (int i = labels.length - 1; i >= 0; i--) {
                labels[i] = cloneInsnNode((LabelNode)old.labels.get(i));
            }
            return (N)new TableSwitchInsnNode(
                    old.min, 
                    old.max, 
                    cloneInsnNode(old.dflt), 
                    labels);
        }
        if (abstractInsnNode instanceof TypeInsnNode) {
            TypeInsnNode old = (TypeInsnNode)abstractInsnNode;
            return (N)new TypeInsnNode(old.getOpcode(), old.desc);
        }
        if (abstractInsnNode instanceof VarInsnNode) {
            VarInsnNode old = (VarInsnNode)abstractInsnNode;
            return (N)new VarInsnNode(old.getOpcode(), old.var);
        }
        throw new AssertionError("cloneInsnNode");
    }
    
    protected static InsnList cloneInsnList(InsnList insnList) {
        InsnList newInsnList = new InsnList();
        for (AbstractInsnNode abstractInsnNode = insnList.getFirst();
                abstractInsnNode != null;
                abstractInsnNode = abstractInsnNode.getNext()) {
            newInsnList.add(cloneInsnNode(abstractInsnNode));
        }
        return newInsnList;
    }
    
    protected static List<TryCatchBlockNode> cloneTryCatchBlocks(
            List<TryCatchBlockNode> tryCatchBlocks) {
        if (tryCatchBlocks == null) {
            return null;
        }
        List<TryCatchBlockNode> clonedTryCatchBlocks = 
                new ArrayList<TryCatchBlockNode>(tryCatchBlocks.size());
        for (TryCatchBlockNode tryCatchBlockNode : tryCatchBlocks) {
            TryCatchBlockNode clonedTryCatchBlockNode = new TryCatchBlockNode(
                    cloneInsnNode(tryCatchBlockNode.start), 
                    cloneInsnNode(tryCatchBlockNode.end), 
                    cloneInsnNode(tryCatchBlockNode.handler), 
                    tryCatchBlockNode.type);
            clonedTryCatchBlocks.add(clonedTryCatchBlockNode);
        }
        return clonedTryCatchBlocks;
    }
    
    protected static List<LocalVariableNode> cloneLocalVariables(
            List<LocalVariableNode> localVariables) {
        if (localVariables == null) {
            return null;
        }
        List<LocalVariableNode> clonedLocalVariables =
                new ArrayList<LocalVariableNode>(localVariables.size());
        for (LocalVariableNode localVariableNode : localVariables) {
            LocalVariableNode clonedLocalVariableNode = new LocalVariableNode(
                    localVariableNode.name, 
                    localVariableNode.desc, 
                    localVariableNode.signature, 
                    cloneInsnNode(localVariableNode.start), 
                    cloneInsnNode(localVariableNode.end), 
                    localVariableNode.index);
            clonedLocalVariables.add(clonedLocalVariableNode);
        }
        return clonedLocalVariables;
    }

    private static ClassEnhancer getInstance0(
            Class<? extends ClassEnhancer> enhancerClass) {
        Arguments.mustNotBeNull("enhancerClass", enhancerClass);
        Arguments.mustBeCompatibleWithValue("enhancerClass", enhancerClass, ClassEnhancer.class);
        Arguments.mustNotBeAbstract("enhancerClass", enhancerClass);
        Constructor<? extends ClassEnhancer> constructor;
        try {
            constructor = enhancerClass.getDeclaredConstructor();
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(
                    mustSupportDefaultConstructor("enhancerClass", enhancerClass),
                    ex
            );
        }
        constructor.setAccessible(true);
        try {
            return constructor.newInstance();
        } catch (InstantiationException ex) {
            throw UncheckedException.rethrow(ex);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        } catch (InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex.getTargetException());
        }
    }

    private static boolean isSystemClass(Class<?> clazz) {
        String className = clazz.getName();
        return className.startsWith("java.") || className.startsWith("javax.");
    }
    
    private static boolean isReturnCode(int opcode) {
        return 
                opcode == Opcodes.RETURN |
                opcode == Opcodes.IRETURN |
                opcode == Opcodes.LRETURN |
                opcode == Opcodes.FRETURN |
                opcode == Opcodes.DRETURN |
                opcode == Opcodes.ARETURN;
    }
    
    private static boolean isLoadCode(int opcode) {
        return 
                opcode == Opcodes.ILOAD |
                opcode == Opcodes.LLOAD |
                opcode == Opcodes.FLOAD |
                opcode == Opcodes.DLOAD |
                opcode == Opcodes.ALOAD;
    }
    
    private static boolean isVisible(int sourceAccess, Package sorucePackage, Package targetPackage) {
        if ((sourceAccess & Opcodes.ACC_PRIVATE) != 0) {
            return false;
        }
        if ((sourceAccess & (Opcodes.ACC_PROTECTED | Opcodes.ACC_PUBLIC)) != 0) {
            return true;
        }
        return sorucePackage == targetPackage;
    }
    
    private static boolean nullSafeListEquals(List<?> a, List<?>  b) {
        if (a != null && a.isEmpty()) {
            a = null;
        }
        if (b != null && b.isEmpty()) {
            b = null;
        }
        return a != null ? a.equals(b) : b == null;
    }
    
    private static boolean nullSafeEquals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
    
    private static String getFieldFieldName(FieldEntry fieldEntry) {
        return "FILED{name=\"" +
                fieldEntry.getName() +
                "\",owner=\"" +
                fieldEntry.declaringClassEntry.clazz.getName().replace('.', '\\') +
                "\"}";
    }
    
    private static String getFieldGetterName(FieldEntry fieldEntry) {
        return "get{name=\"" +
                fieldEntry.getName() +
                "\",owner=\"" +
                fieldEntry.declaringClassEntry.clazz.getName().replace('.', '\\') +
                "\"}";
    }
    
    private static String getFieldSetterName(FieldEntry fieldEntry) {
        return "set{name=\"" +
                fieldEntry.fieldNode.name +
                "\",owner=\"" +
                fieldEntry.declaringClassEntry.clazz.getName().replace('.', '\\') +
                "\"}"; 
    }
    
    private static String getMethodFieldName(MethodEntry methodEntry) {
        StringBuilder builder = new StringBuilder(); 
        builder
        .append(methodEntry.isInit() ? "CONSTRUCTOR" : "METHOD")
        .append("{name=\"")
        .append(methodEntry.getName())
        .append("\",owner=\"")
        .append(methodEntry.declaringClassEntry.clazz.getName().replace('.', '\\'))
        .append("\",parameterTypes=\"");
        boolean addComma = false;
        for (Type parameterType : Type.getArgumentTypes(methodEntry.getDescriptor().desc)) {
            if (addComma) {
                builder.append(',');
            } else {
                addComma = true;
            }
            builder.append(parameterType.getClassName().replace("[]", "*").replace('.', '\\'));
        }
        builder.append("\"}");
        return builder.toString();
    }
    
    private static Class<?> getMethodFieldType(MethodEntry methodEntry) {
        return methodEntry.isInit() ? Constructor.class : Method.class;
    }
    
    private class Context {
        
        Package targetPackage;
        
        ClassLoader targetClassLoader;
        
        ProtectionDomain targetProctionDomain;
        
        String newClassName;
        
        String newInternalName;
        
        ClassEntry newClassEntry;
        
        //Key is internal name
        XOrderedMap<String, ClassEntry> classEntries;
        
        //Key is internal name
        XMap<String, ClassEntry> invokedEntries;
        
        Context() {
            ClassEnhancer enhancer = ClassEnhancer.this;
            String newClassName = enhancer.clazz.getName();
            if (enhancer.usedEnhancerLoader()) {
                Package package_ = enhancer.clazz.getPackage();
                if (package_ != null) {
                    newClassName = newClassName.substring(package_.getName().length() + 1);
                }
                Package enchancerPackage = enhancer.getClass().getPackage();
                if (enchancerPackage != null) {
                    this.targetPackage = enchancerPackage;
                    newClassName = enchancerPackage.getName() + '.' + newClassName;
                }
                this.targetClassLoader = enhancer.getClass().getClassLoader();
                this.targetProctionDomain = enhancer.getClass().getProtectionDomain();
            } else {
                this.targetPackage = enhancer.clazz.getPackage();
                this.targetClassLoader = enhancer.clazz.getClassLoader();
                this.targetProctionDomain = enhancer.clazz.getProtectionDomain();
            }
            newClassName += 
                    "_Enhanced_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E{enhancerClass=" +
                    enhancer.getClass().getName().replace('.', '\\') +
                    '}';
            this.newClassName = newClassName;
            this.newInternalName = newClassName.replace('.', '/');
            XOrderedMap<String, ClassEntry> classEntries =
                    new LinkedHashMap<String, ClassEntry>(
                            true, 
                            OrderAdjustMode.NONE, 
                            OrderAdjustMode.NONE);
            for (Class<?> clazz = enhancer.clazz;
                    !isSystemClass(clazz);
                    clazz = clazz.getSuperclass()) {
                classEntries.put(
                        clazz.getName().replace('.', '/'), 
                        new ClassEntry(clazz));
            }
            ClassEntry superClassEntry = null;
            for (ClassEntry classEntry : classEntries.values()) {
                classEntry.superClassEntry = superClassEntry;
                superClassEntry = classEntry;
            }
            this.classEntries = classEntries;
            this.invokedEntries = new HashMap<>();
        }
        
        ClassEnhancer owner() {
            return ClassEnhancer.this;
        }
        
        Class<?> getResultClass() {
            boolean changed = false;
            changed |= this.modifyMethods();
            changed |= this.prepareNewClassNode();
            if (changed) {
                this.buildOverrideChain();
                this.buildBridgeChain();
                this.buildPolymorphicTarget();
                while (this.updateInvocations());
                this.resolveIllegalAccess();
            } else {
                if (!Modifier.isAbstract(ClassEnhancer.this.clazz.getModifiers())) {
                    return ClassEnhancer.this.clazz;
                }
            }
            Consumer<ClassVisitor> cvAction = (ClassVisitor cv) -> {
                Context.this.modifyNewClassNode();
                Context.this.newClassEntry.classNode.accept(cv);
            };
            return ASM.loadDynamicClass(
                    this.targetClassLoader, 
                    this.newClassName, 
                    this.targetProctionDomain, 
                    cvAction);
        }
        
        private boolean modifyMethods() {
            boolean retval = false;
            for (ClassEntry classEntry : this.classEntries.values()) {
                for (MethodEntry methodEntry : classEntry.methodEntries.values()) {
                    if (!methodEntry.isInit() && 
                            !methodEntry.isClinit() && 
                            (methodEntry.methodNode.access & Opcodes.ACC_BRIDGE) == 0) {
                        ClassEnhancer.this.doMethodFilter(new MethodSource(methodEntry));
                        if (methodEntry.setChanged()) {
                            retval = true;
                        }
                    }
                }
            }
            return retval;
        }

        private boolean prepareNewClassNode() {
            boolean retval = false;
            
            Package targetPackage = this.targetPackage;
            ClassEntry lastEntry = this.classEntries.lastEntry().getValue();
            ClassNode newClassNode = new ClassNode();
            newClassNode.version = Opcodes.V1_7;
            newClassNode.access = Opcodes.ACC_PUBLIC;
            newClassNode.name = this.newInternalName;
            newClassNode.superName = Type.getInternalName(ClassEnhancer.this.clazz);
            newClassNode.attrs = new ArrayList<Attribute>();
            newClassNode.fields = new ArrayList<FieldNode>();
            newClassNode.methods = new ArrayList<MethodNode>();
            
            Collection<String> interfaces = ClassEnhancer.this.getInterfaces();
            if (interfaces != null) {
                Set<String> itfs = new LinkedHashSet<String>((interfaces.size() * 4 + 2)/ 3, .75F);
                for (String itf : interfaces) {
                    if (itf != null && !itf.isEmpty()) {
                        itfs.add(itf);
                        retval = true;
                    }
                }
                List<String> itfList = new ArrayList<String>();
                itfList.addAll(itfs);
                newClassNode.interfaces = itfList;
            } else {
                newClassNode.interfaces = new ArrayList<String>();
            }
            
            for (MethodEntry methodEntry : lastEntry.methodEntries.values()) {
                MethodNode methodNode = methodEntry.methodNode;
                if ("<init>".equals(methodNode.name) && 
                        isVisible(
                                methodNode.access, 
                                lastEntry.clazz.getPackage(), 
                                targetPackage)) {
                    if (!ClassEnhancer.this.hideConstructor(methodEntry.constructor)) {
                        MethodNode newMethodNode = new MethodNode(Opcodes.ASM5);
                        newMethodNode.access = methodNode.access;
                        newMethodNode.name = "<init>";
                        newMethodNode.desc = methodNode.desc;
                        newMethodNode.signature = methodNode.signature;
                        newMethodNode.exceptions = methodNode.exceptions;
                        InsnList instractions = new InsnList();
                        instractions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        int slotIndex = 1;
                        for (Class<?> parameterType : methodEntry.constructor.getParameterTypes()) {
                            instractions.add(new VarInsnNode(ASM.getLoadCode(parameterType), slotIndex));
                            slotIndex += ASM.getSlotCount(parameterType);
                        }
                        instractions.add(new MethodInsnNode(
                                Opcodes.INVOKESPECIAL,
                                lastEntry.clazz.getName().replace('.', '/'),
                                "<init>",
                                methodNode.desc,
                                false));
                        InsnList moreInstructions = 
                                ClassEnhancer.this.getMoreConstructorInstructions(methodEntry.constructor);
                        if (moreInstructions != null && moreInstructions.size() != 0) {
                            instractions.add(moreInstructions);
                            retval = true;
                        }
                        instractions.add(new InsnNode(Opcodes.RETURN));
                        newMethodNode.instructions = instractions;
                        newClassNode.methods.add(newMethodNode);
                    }
                }
            }
            
            MethodNode clinitNode = new MethodNode(Opcodes.ASM5);
            clinitNode.access = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC;
            clinitNode.name = "<clinit>";
            clinitNode.desc = "()V";
            clinitNode.exceptions = new ArrayList<String>();
            clinitNode.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
            clinitNode.localVariables = new ArrayList<LocalVariableNode>();
            InsnList instructions = ClassEnhancer.this.getMoreStaticBlockInstructions();
            if (instructions != null) {
                retval = true;
            } else {
                instructions = new InsnList();
            }
            clinitNode.instructions = instructions;
            newClassNode.methods.add(clinitNode);
            
            Collection<FieldNode> moreFieldNodes = 
                    ClassEnhancer.this.getMoreFieldNodes();
            if (moreFieldNodes != null) {
                Set<String> fieldNames = new HashSet<String>();
                for (FieldNode fieldNode : moreFieldNodes) {
                    if (fieldNode != null) {
                        if (fieldNode.name == null || fieldNode.name.isEmpty()) {
                            throw new IllegalProgramException(
                                    getMoreFieldNodesReturnNoNameNodes(
                                            ClassEnhancer.this.getClass()
                                    )
                            );
                        }
                        if (!fieldNames.add(fieldNode.name)) {
                            throw new IllegalProgramException(
                                    getMoreFieldNodesReturnExistingField(
                                            ClassEnhancer.this.getClass(), fieldNode.name
                                    )
                            );
                        }
                        newClassNode.fields.add(fieldNode);
                        retval = true;
                    }
                }
            }
            
            Map<String, ClassEntry> allClassEntries = new LinkedHashMap<String, ClassEntry>();
            allClassEntries.putAll(this.classEntries);
            allClassEntries.put(newClassNode.name, new ClassEntry(newClassNode));
            Collection<MethodNode> moreMethodNodes = 
                    ClassEnhancer.this.getMoreMethodNodes(
                            new MethodSourceFactory(this));
            if (moreMethodNodes != null) {
                Set<Descriptor> methodDescriptors = new HashSet<Descriptor>();
                for (MethodNode methodNode : moreMethodNodes) {
                    if (methodNode != null) {
                        if (methodNode.name == null || methodNode.name.isEmpty()) {
                            throw new IllegalProgramException(
                                    getMoreMethodNodesReturnNoNameNodes(ClassEnhancer.this.getClass())
                            );
                        }
                        if (!methodDescriptors.add(new Descriptor(methodNode.name, methodNode.desc))) {
                            throw new IllegalProgramException(
                                    getMoreMethodNodesReturnExistingMethod(
                                            ClassEnhancer.this.getClass(),
                                            methodNode.name, 
                                            methodNode.desc
                                    )
                            );
                        }
                        newClassNode.methods.add(methodNode);
                        retval = true;
                    }
                }
            }
            ClassEntry newClassEntry = new ClassEntry(newClassNode);
            newClassEntry.superClassEntry = this.classEntries.lastEntry().getValue();
            this.newClassEntry = newClassEntry;
            this.classEntries.put(newClassNode.name, newClassEntry);
            return retval;
        }

        private void buildOverrideChain() {
            ClassEntry[] arr = 
                    this.classEntries.values().toArray(
                            new ClassEntry[this.classEntries.size()]);
            for (int i = 0; i < arr.length; i++) {
                for (int ii = i + 1; ii < arr.length; ii++) {
                    for (MethodEntry methodEntry1 : arr[i].methodEntries.values()) {
                        int access1 = methodEntry1.methodNode.access;
                        if ((access1 & (Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE)) == 0) {
                             MethodEntry methodEntry2 = arr[ii].methodEntries.get(methodEntry1.getDescriptor());
                             if (methodEntry2 != null) {
                                 int access2 = methodEntry1.methodNode.access;
                                 if ((access2 & (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE)) == 0) {
                                     methodEntry1.overrideTarget = methodEntry2;
                                 }
                             }
                        }
                    }
                }
            }
        }
        
        private void buildBridgeChain() {
            for (ClassEntry classEntry : this.classEntries.values()) {
                for (MethodEntry methodEntry : classEntry.methodEntries.values()) {
                    if ((methodEntry.methodNode.access & Opcodes.ACC_BRIDGE) != 0) {
                        if (methodEntry.instructions.size() < 3) {
                            throw new AssertionError("illegal bridge method");
                        }
                        AbstractInsnNode firstInsnNode = methodEntry.methodNode.instructions.getFirst();
                        AbstractInsnNode lastInsnNode = methodEntry.methodNode.instructions.getLast();
                        AbstractInsnNode previousOfLastInsnNode = lastInsnNode.getPrevious();
                        if (firstInsnNode.getOpcode() != Opcodes.ALOAD || 
                                !(firstInsnNode instanceof VarInsnNode) || 
                                ((VarInsnNode)firstInsnNode).var != 0) {
                            throw new AssertionError("illegal bridge method");
                        }
                        if (previousOfLastInsnNode.getOpcode() != Opcodes.INVOKEVIRTUAL || 
                                !(previousOfLastInsnNode instanceof MethodInsnNode) ||
                                !((MethodInsnNode)previousOfLastInsnNode).owner.equals(classEntry.classNode.name)) {
                            throw new AssertionError("illegal bridge method");
                        }
                        if (!isReturnCode(lastInsnNode.getOpcode()) || 
                                !(lastInsnNode instanceof InsnNode)) {
                            throw new AssertionError("illegal bridge method");
                        }
                        for (AbstractInsnNode abstractInsnNode = firstInsnNode.getNext();
                                abstractInsnNode != previousOfLastInsnNode;
                                abstractInsnNode = abstractInsnNode.getNext()) {
                            if (abstractInsnNode instanceof VarInsnNode && isLoadCode(abstractInsnNode.getOpcode())) {
                                continue;
                            }
                            if (abstractInsnNode instanceof TypeInsnNode && abstractInsnNode.getOpcode() == Opcodes.CHECKCAST) {
                                continue;
                            }
                            throw new AssertionError("illegal bridge method");
                        }
                        MethodInsnNode invokeVirtualNode = (MethodInsnNode)previousOfLastInsnNode;
                        MethodEntry targetMethodEntry = classEntry.methodEntries.get(
                                new Descriptor(invokeVirtualNode.name, invokeVirtualNode.desc));
                        if (targetMethodEntry == null) {
                            throw new AssertionError("illegal bridge method");
                        }
                        methodEntry.bridgeTarget = targetMethodEntry;
                    }
                }
            }
        }
        
        private void buildPolymorphicTarget() {
            for (ClassEntry classEntry : this.classEntries.values()) {
                for (MethodEntry methodEntry : classEntry.methodEntries.values()) {
                    MethodEntry polymorphicTarget = methodEntry;
                    while (true) {
                        if (polymorphicTarget.overrideTarget != null) {
                            polymorphicTarget = polymorphicTarget.overrideTarget;
                        } else if (polymorphicTarget.bridgeTarget != null) {
                            polymorphicTarget = polymorphicTarget.bridgeTarget;
                        } else {
                            break;
                        }
                    }
                    methodEntry.polymorphicTarget = polymorphicTarget;
                }
            }
        }
        
        private boolean updateInvocations() {
            boolean retval = false;
            for (ClassEntry invokingClassEntry : this.classEntries.values()) {
                for (MethodEntry invokingMethodEntry : invokingClassEntry.methodEntries.values()) {
                    InsnList instructions = invokingMethodEntry.instructions;
                    for (AbstractInsnNode abstractInsnNode = instructions.getFirst();
                            abstractInsnNode != null;
                            abstractInsnNode = abstractInsnNode.getNext()) {
                        if (abstractInsnNode instanceof MethodInsnNode) {
                            MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;
                            ClassEntry invokedClassEntry = this.classEntries.get(methodInsnNode.owner);
                            if (invokedClassEntry != null) {
                                MethodEntry invokedMethodEntry = invokedClassEntry.getMethodEntry(methodInsnNode.name, methodInsnNode.desc);
                                if (invokedMethodEntry != null) {
                                    invokedClassEntry = invokedMethodEntry.declaringClassEntry;
                                    if (invokedClassEntry.clazz != null /* Not new class entry */) {
                                        if (methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                                            invokedMethodEntry = invokedMethodEntry.polymorphicTarget;
                                        }
                                        if (invokedMethodEntry.changed && 
                                                !invokedMethodEntry.isReplacedFor(invokingMethodEntry)) {
                                            if (!invokedMethodEntry.isOverriable(this.targetPackage) || 
                                                    methodInsnNode.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                                                if (invokingMethodEntry.isInit()) {
                                                    throw new AssertionError(
                                                            "<init> of \"" +
                                                            invokingMethodEntry.declaringClassEntry.classNode.name +
                                                            "\" + invoked changed method:" + 
                                                            invokedMethodEntry.declaringClassEntry.classNode.name +
                                                            "::" +
                                                            invokedMethodEntry.methodNode.name +
                                                            invokedMethodEntry.methodNode.desc);
                                                } else if (invokingMethodEntry.isClinit()) {
                                                    throw new AssertionError(
                                                            "<clinit> of \"" +
                                                            invokingMethodEntry.declaringClassEntry.classNode.name +
                                                            "\" + invoked changed method:" +  
                                                            invokedMethodEntry.declaringClassEntry.classNode.name +
                                                            "::" +
                                                            invokedMethodEntry.methodNode.name +
                                                            invokedMethodEntry.methodNode.desc);
                                                }
                                                //Notes, the new methods can not be marked as "replaced"
                                                invokedMethodEntry.replaceFor(invokingMethodEntry);
                                                MethodInsnNode replacedMethodInsnNode = 
                                                        new ReplacedMethodInsnNode(
                                                                this.newInternalName, 
                                                                invokedClassEntry.clazz, 
                                                                methodInsnNode);
                                                instructions.insert(methodInsnNode, replacedMethodInsnNode);
                                                instructions.remove(methodInsnNode);
                                                abstractInsnNode = replacedMethodInsnNode; //Make the loop can continue
                                                //Notes, the new methods can not be marked as "changed"
                                                invokingMethodEntry.changed = (invokingClassEntry.clazz != null);
                                                retval = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return retval;
        }
        
        private void resolveIllegalAccess() {
            for (ClassEntry classEntry : this.classEntries.values()) {
                for (MethodEntry methodEntry : classEntry.methodEntries.values()) {
                    if (methodEntry.changed || classEntry.clazz == null /* New class node */) {
                        InsnList instructions = methodEntry.instructions;
                        for (AbstractInsnNode abstractInsnNode = instructions.getFirst();
                        abstractInsnNode != null;
                        abstractInsnNode = abstractInsnNode.getNext()) {
                            if (abstractInsnNode instanceof FieldInsnNode) {
                                FieldInsnNode fieldInsnNode = (FieldInsnNode)abstractInsnNode;
                                ClassEntry invokedClassEntry = this.getInovkedEntry(fieldInsnNode.owner);
                                if (invokedClassEntry != null) {
                                    FieldEntry invokedFieldEntry = invokedClassEntry.getFieldEntry(fieldInsnNode.name);
                                    if (invokedFieldEntry != null) {
                                        invokedClassEntry = invokedFieldEntry.declaringClassEntry;
                                        if (invokedClassEntry.clazz != null /* Not new class entry */) {
                                            invokedFieldEntry.ownerVisible = this.isVisibleClass(invokedFieldEntry.field.getDeclaringClass());
                                            invokedFieldEntry.typeVisible = this.isVisibleClass(invokedFieldEntry.field.getType());
                                            int access = invokedFieldEntry.getAccess();
                                            if (invokedFieldEntry.ownerVisible && invokedFieldEntry.typeVisible) {
                                                if ((access & Opcodes.ACC_PUBLIC) != 0) {
                                                    continue;
                                                } else if ((access & Opcodes.ACC_PRIVATE) == 0) {
                                                    if (invokedClassEntry.clazz.getPackage() == this.targetPackage) {
                                                        continue;
                                                    }
                                                }
                                            }
                                            boolean getter = 
                                                    fieldInsnNode.getOpcode() == Opcodes.GETFIELD || 
                                                    fieldInsnNode.getOpcode() == Opcodes.GETSTATIC;
                                            if (getter) {
                                                if (invokedFieldEntry.getterDescriptorToAvoidIllegalAccessing == null) {
                                                    StringBuilder builder = new StringBuilder();
                                                    builder.append('(');
                                                    if ((access & Opcodes.ACC_STATIC) == 0) {
                                                        builder
                                                        .append('L')
                                                        .append(
                                                                invokedFieldEntry.ownerVisible ?
                                                                invokedFieldEntry.declaringClassEntry.getInternalName() : 
                                                                "java/lang/Object"
                                                        )
                                                        .append(';');
                                                    }
                                                    builder
                                                    .append(')')
                                                    .append(
                                                        invokedFieldEntry.typeVisible ?
                                                        invokedFieldEntry.getDescriptor() :
                                                        "Ljava/lang/Object;"
                                                    );
                                                    invokedFieldEntry.getterDescriptorToAvoidIllegalAccessing = builder.toString();
                                                }
                                                if (invokedFieldEntry.getterDescriptorToAvoidIllegalAccessing != null) {
                                                    abstractInsnNode = new MethodInsnNode(
                                                            Opcodes.INVOKESTATIC, 
                                                            this.newInternalName, 
                                                            getFieldGetterName(invokedFieldEntry), 
                                                            invokedFieldEntry.getterDescriptorToAvoidIllegalAccessing,
                                                            false);
                                                    instructions.insert(fieldInsnNode, abstractInsnNode);
                                                    instructions.remove(fieldInsnNode);
                                                }
                                            } else {
                                                if (invokedFieldEntry.setterDescriptorToAvoidIllegalAccessing == null) {
                                                    StringBuilder builder = new StringBuilder();
                                                    builder.append('(');
                                                    if ((access & Opcodes.ACC_STATIC) == 0) {
                                                        builder
                                                        .append('L')
                                                        .append(
                                                                invokedFieldEntry.ownerVisible ?
                                                                invokedFieldEntry.declaringClassEntry.getInternalName() : 
                                                                "java/lang/Object"
                                                        )
                                                        .append(';');
                                                    }
                                                    builder
                                                    .append(
                                                        invokedFieldEntry.typeVisible ?
                                                        invokedFieldEntry.getDescriptor() :
                                                        "Ljava/lang/Object;"
                                                    )
                                                    .append(")V");
                                                    invokedFieldEntry.setterDescriptorToAvoidIllegalAccessing = builder.toString();
                                                }
                                                if (invokedFieldEntry.setterDescriptorToAvoidIllegalAccessing != null) {
                                                    abstractInsnNode = new MethodInsnNode(
                                                            Opcodes.INVOKESTATIC, 
                                                            this.newInternalName, 
                                                            getFieldSetterName(invokedFieldEntry), 
                                                            invokedFieldEntry.setterDescriptorToAvoidIllegalAccessing,
                                                            false);
                                                    instructions.insert(fieldInsnNode, abstractInsnNode);
                                                    instructions.remove(fieldInsnNode);
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (abstractInsnNode instanceof MethodInsnNode) {
                                MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;
                                ClassEntry invokedClassEntry = this.getInovkedEntry(methodInsnNode.owner);
                                if (invokedClassEntry != null) {
                                    if (!(methodInsnNode instanceof ReplacedMethodInsnNode)) {
                                        String name = methodInsnNode.name;
                                        MethodEntry invokedMethodEntry = invokedClassEntry.getMethodEntry(name, methodInsnNode.desc);
                                        if (invokedMethodEntry != null) {
                                            invokedClassEntry = invokedMethodEntry.declaringClassEntry;
                                            if (invokedClassEntry.clazz != null /* Not new class entry */) {
                                                if (invokedMethodEntry.descriptorToAvoidIllegalAccessing == null) {
                                                    invokedMethodEntry.ownerVisible = this.isVisibleClass(invokedMethodEntry.getMember().getDeclaringClass());
                                                    if (invokedMethodEntry.method != null) {
                                                        invokedMethodEntry.returnVisible = this.isVisibleClass(invokedMethodEntry.method.getReturnType());
                                                    }
                                                    Class<?>[] parameterTypes = 
                                                            invokedMethodEntry.method != null ? 
                                                                    invokedMethodEntry.method.getParameterTypes() : 
                                                                    invokedMethodEntry.constructor.getParameterTypes();
                                                    for (int i = parameterTypes.length - 1; i >= 0; i--) {
                                                        invokedMethodEntry.parameterVisisbles[i] = this.isVisibleClass(parameterTypes[i]);
                                                    }
                                                    int access = invokedMethodEntry.getAccess();
                                                    if (invokedMethodEntry.ownerVisible && invokedMethodEntry.returnVisible && invokedMethodEntry.areAllParametersVisible()) {
                                                        if ((access & Opcodes.ACC_PUBLIC) != 0) {
                                                            continue;
                                                        } else if ((access & Opcodes.ACC_PRIVATE) == 0) {
                                                            if (invokedClassEntry.clazz.getPackage() == this.targetPackage) {
                                                                continue;
                                                            }
                                                        }
                                                    }
                                                    StringBuilder builder = new StringBuilder();
                                                    builder.append('(');
                                                    if ((access & Opcodes.ACC_STATIC) == 0 && !invokedMethodEntry.isInit()) {
                                                        builder
                                                        .append('L')
                                                        .append(
                                                                invokedMethodEntry.ownerVisible ?
                                                                invokedMethodEntry.declaringClassEntry.getInternalName() : 
                                                                "java/lang/Object"
                                                        )
                                                        .append(';');
                                                    }
                                                    for (int i = 0; i < parameterTypes.length; i++) {
                                                        builder
                                                        .append(
                                                            invokedMethodEntry.parameterVisisbles[i] ?
                                                            ASM.getDescriptor(parameterTypes[i]) :
                                                            "Ljava/lang/Object;"
                                                        );
                                                    }
                                                    builder.append(')');
                                                    if (invokedMethodEntry.method != null) {
                                                        builder
                                                        .append(
                                                                invokedMethodEntry.returnVisible ?
                                                                ASM.getDescriptor(invokedMethodEntry.method.getReturnType()) :
                                                                "Ljava/lang/Object;"
                                                            );
                                                    } else {
                                                        builder.append(
                                                                invokedMethodEntry.ownerVisible ?
                                                                ASM.getDescriptor(invokedMethodEntry.declaringClassEntry.clazz) :
                                                                "Ljava/lang/Object;");
                                                    }
                                                    invokedMethodEntry.descriptorToAvoidIllegalAccessing = builder.toString();
                                                }
                                                if (invokedMethodEntry.descriptorToAvoidIllegalAccessing != null) {
                                                    if (invokedMethodEntry.isInit()) {
                                                        boolean matched = false;
                                                        for (AbstractInsnNode prev = abstractInsnNode.getPrevious();
                                                                prev != null;
                                                                prev = prev.getPrevious()) {
                                                            if (prev.getOpcode() == Opcodes.NEW &&
                                                                    ASM.getInternalName(((TypeInsnNode)prev).desc).equals(methodInsnNode.owner)) {
                                                                if (prev.getNext().getOpcode() != Opcodes.DUP) {
                                                                    throw new IllegalProgramException(
                                                                            nextOpCodeOfNewMustBeDup(
                                                                                    ClassEnhancer.this.getClass(),
                                                                                    methodEntry.descriptor.name,
                                                                                    methodEntry.descriptor.desc
                                                                            )
                                                                    );
                                                                }
                                                                instructions.remove(prev.getNext());
                                                                instructions.remove(prev);
                                                                matched = true;
                                                                break;
                                                            }
                                                        }
                                                        if (!matched) {
                                                            continue;
                                                        }
                                                    }
                                                    abstractInsnNode = new MethodInsnNode(
                                                            Opcodes.INVOKESTATIC, 
                                                            this.newInternalName, 
                                                            this.getMethodInvocationName(invokedMethodEntry), 
                                                            invokedMethodEntry.descriptorToAvoidIllegalAccessing,
                                                            false);
                                                    instructions.insertBefore(methodInsnNode, abstractInsnNode);
                                                    instructions.remove(methodInsnNode);
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (abstractInsnNode instanceof TypeInsnNode) {
                                TypeInsnNode typeInsnNode = (TypeInsnNode)abstractInsnNode;
                                Class<?> type;
                                try {
                                    type = ClassInfo.forInternalName(typeInsnNode.desc);
                                } catch (NoSuchClassException ex) {
                                    continue;
                                }
                                if (this.isVisibleClass(type)) {
                                    continue;
                                }
                                if (typeInsnNode.getOpcode() == Opcodes.CHECKCAST) {
                                    abstractInsnNode = typeInsnNode.getPrevious();
                                    instructions.remove(typeInsnNode);
                                } else if (typeInsnNode.getOpcode() == Opcodes.ANEWARRAY) {
                                    InsnList tmpInstructions = new InsnList();
                                    tmpInstructions.add(new LdcInsnNode(typeInsnNode.desc));
                                    tmpInstructions.add(new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            ASM.getInternalName(ClassInfo.class),
                                            "forInternalName",
                                            "(Ljava/lang/String;)Ljava/lang/Class;",
                                            false));
                                    tmpInstructions.add(new InsnNode(Opcodes.SWAP));
                                    tmpInstructions.add(new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            ASM.getInternalName(Array.class),
                                            "newInstance",
                                            "(Ljava/lang/Class;I)Ljava/lang/Object;",
                                            false));
                                    tmpInstructions.add(new TypeInsnNode(Opcodes.CHECKCAST, "[Ljava/lang/Object;"));
                                    abstractInsnNode = tmpInstructions.getLast();
                                    instructions.insertBefore(typeInsnNode, tmpInstructions);
                                    instructions.remove(typeInsnNode);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        private void modifyNewClassNode() {
            ClassEntry newClassEntry = this.newClassEntry;
            ClassNode newClassNode = newClassEntry.classNode;
            List<ClassEntry> ces = new ArrayList<>(this.classEntries.size() + this.invokedEntries.size());
            ces.addAll(this.classEntries.values());
            ces.addAll(this.invokedEntries.values());
            for (ClassEntry ce : ces) {
                for (FieldEntry fieldEntry : ce.fieldEntries.values()) {
                    if (fieldEntry.getterDescriptorToAvoidIllegalAccessing != null || 
                            fieldEntry.setterDescriptorToAvoidIllegalAccessing != null) {
                        FieldNode fieldNode = new FieldNode(
                                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                                getFieldFieldName(fieldEntry), 
                                Type.getDescriptor(Field.class), 
                                null,
                                null);
                        newClassNode.fields.add(fieldNode);
                        if (fieldEntry.getterDescriptorToAvoidIllegalAccessing != null) {
                            newClassNode.methods.add(this.createGetterMethod(fieldEntry));
                        }
                        if (fieldEntry.setterDescriptorToAvoidIllegalAccessing != null) {
                            newClassNode.methods.add(this.createSetterMethod(fieldEntry));
                        }
                    }
                }
                for (MethodEntry methodEntry : ce.methodEntries.values()) {
                    if (methodEntry.changed) {
                        String methodName;
                        if (methodEntry.isReplaced() || !methodEntry.isOverriable(targetPackage)) {
                            methodName = ReplacedMethodInsnNode.replaceName(ce.clazz, methodEntry.methodNode.name);
                        } else {
                            methodName = methodEntry.methodNode.name;
                        }
                        MethodNode methodNode = methodEntry.methodNode;
                        MethodNode newMethodNode = new MethodNode(Opcodes.ASM5);
                        newMethodNode.access = methodNode.access;
                        newMethodNode.name = methodName;
                        newMethodNode.desc = methodNode.desc;
                        newMethodNode.signature = methodNode.signature;
                        newMethodNode.exceptions = methodNode.exceptions;
                        newMethodNode.tryCatchBlocks = methodEntry.tryCatchBlocks; //Not methodNode.tryCatchBlocks
                        newMethodNode.localVariables = methodEntry.localVariables; //Not methodNode.localVariables
                        newMethodNode.instructions = methodEntry.instructions; //Not methodNode.instructions
                        newClassNode.methods.add(newMethodNode);
                    }
                    if (methodEntry.descriptorToAvoidIllegalAccessing != null) {
                        FieldNode fieldNode = new FieldNode(
                                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                                getMethodFieldName(methodEntry), 
                                ASM.getDescriptor(getMethodFieldType(methodEntry)), 
                                null,
                                null);
                        newClassNode.fields.add(fieldNode);
                        newClassNode.methods.add(this.createInovcationMethod(methodEntry));
                    }
                }
            }
            
            InsnList tmpInstructions = new InsnList();
            List<TryCatchBlockNode> tmpTryCatchBlocks = new ArrayList<TryCatchBlockNode>();
            for (ClassEntry classEntry : ces) {
                for (FieldEntry fieldEntry : classEntry.fieldEntries.values()) {
                    if (fieldEntry.getterDescriptorToAvoidIllegalAccessing != null || 
                            fieldEntry.setterDescriptorToAvoidIllegalAccessing != null) {
                        LabelNode tryLabelNode = new LabelNode(new Label());
                        LabelNode endTryLabelNode = new LabelNode(new Label());
                        LabelNode catchNoSuchFieldLabelNode = new LabelNode(new Label());
                        LabelNode catchClassNotFoundLabelNode = new LabelNode(new Label());
                        LabelNode endCatchLabelNode = new LabelNode(new Label());
                        tmpTryCatchBlocks.add(
                                new TryCatchBlockNode(
                                        tryLabelNode, 
                                        endTryLabelNode, 
                                        catchNoSuchFieldLabelNode, 
                                        Type.getInternalName(NoSuchFieldException.class)));
                        if (!fieldEntry.ownerVisible) {
                            tmpTryCatchBlocks.add(
                                    new TryCatchBlockNode(
                                            tryLabelNode, 
                                            endTryLabelNode, 
                                            catchClassNotFoundLabelNode, 
                                            Type.getInternalName(ClassNotFoundException.class)));
                        }
                        tmpInstructions.add(tryLabelNode);
                        if (fieldEntry.ownerVisible) {
                            tmpInstructions.add(new LdcInsnNode(Type.getType(fieldEntry.declaringClassEntry.clazz)));
                        } else {
                            tmpInstructions.add(new LdcInsnNode(fieldEntry.declaringClassEntry.clazz.getName()));
                            tmpInstructions.add(
                                    new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            ASM.getInternalName(ClassInfo.class),
                                            "forInternalName",
                                            "(Ljava/lang/String;)Ljava/lang/Class;",
                                            false));
                        }
                        tmpInstructions.add(new LdcInsnNode(fieldEntry.getName()));
                        tmpInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL, 
                                "java/lang/Class", 
                                "getDeclaredField", 
                                "(Ljava/lang/String;)" + Type.getDescriptor(Field.class),
                                false));
                        tmpInstructions.add(new InsnNode(Opcodes.DUP));
                        tmpInstructions.add(new LdcInsnNode(true));
                        tmpInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL, 
                                Type.getInternalName(Field.class), 
                                "setAccessible", 
                                "(Z)V",
                                false));
                        tmpInstructions.add(
                                new FieldInsnNode(
                                        Opcodes.PUTSTATIC, 
                                        this.newInternalName, 
                                        getFieldFieldName(fieldEntry), 
                                        Type.getDescriptor(Field.class)));
                        tmpInstructions.add(endTryLabelNode);
                        tmpInstructions.add(new JumpInsnNode(Opcodes.GOTO, endCatchLabelNode));
                        tmpInstructions.add(catchNoSuchFieldLabelNode);
                        tmpInstructions.add(new InsnNode(Opcodes.POP)); //pop exception
                        tmpInstructions.add(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(AssertionError.class)));
                        tmpInstructions.add(new InsnNode(Opcodes.DUP));
                        tmpInstructions.add(new LdcInsnNode("Internal bug: 1"));
                        tmpInstructions.add(
                                new MethodInsnNode(
                                        Opcodes.INVOKESPECIAL, 
                                        Type.getInternalName(AssertionError.class), 
                                        "<init>", 
                                        "(Ljava/lang/String;)V",
                                        false));
                        tmpInstructions.add(new InsnNode(Opcodes.ATHROW));
                        if (!fieldEntry.ownerVisible) {
                            tmpInstructions.add(catchClassNotFoundLabelNode);
                            tmpInstructions.add(new InsnNode(Opcodes.POP)); //pop exception
                            tmpInstructions.add(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(AssertionError.class)));
                            tmpInstructions.add(new InsnNode(Opcodes.DUP));
                            tmpInstructions.add(new LdcInsnNode("Internal bug: 2"));
                            tmpInstructions.add(
                                    new MethodInsnNode(
                                            Opcodes.INVOKESPECIAL, 
                                            Type.getInternalName(AssertionError.class), 
                                            "<init>", 
                                            "(Ljava/lang/String;)V",
                                            false));
                            tmpInstructions.add(new InsnNode(Opcodes.ATHROW));
                        }
                        tmpInstructions.add(endCatchLabelNode);
                    }
                }
                for (MethodEntry methodEntry : classEntry.methodEntries.values()) {
                    if (methodEntry.descriptorToAvoidIllegalAccessing != null) {
                        Type[] parameterTypes = Type.getArgumentTypes(methodEntry.getDescriptor().desc);
                        LabelNode tryLabelNode = new LabelNode(new Label());
                        LabelNode endTryLabelNode = new LabelNode(new Label());
                        LabelNode catchNoSuchMethodLabelNode = new LabelNode(new Label());
                        LabelNode catchClassNotFoundLabelNode = new LabelNode(new Label());
                        LabelNode endCatchLabelNode = new LabelNode(new Label());
                        tmpTryCatchBlocks.add(
                                new TryCatchBlockNode(
                                        tryLabelNode, 
                                        endTryLabelNode, 
                                        catchNoSuchMethodLabelNode, 
                                        Type.getInternalName(NoSuchMethodException.class)));
                        if (!methodEntry.ownerVisible || !methodEntry.areAllParametersVisible()) {
                            tmpTryCatchBlocks.add(
                                    new TryCatchBlockNode(
                                            tryLabelNode, 
                                            endTryLabelNode, 
                                            catchClassNotFoundLabelNode, 
                                            Type.getInternalName(ClassNotFoundException.class)));
                        }
                        tmpInstructions.add(tryLabelNode);
                        if (methodEntry.ownerVisible) {
                            tmpInstructions.add(new LdcInsnNode(Type.getType(methodEntry.declaringClassEntry.clazz)));
                        } else {
                            tmpInstructions.add(new LdcInsnNode(methodEntry.declaringClassEntry.clazz.getName()));
                            tmpInstructions.add(
                                    new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            ASM.getInternalName(ClassInfo.class),
                                            "forInternalName",
                                            "(Ljava/lang/String;)Ljava/lang/Class;",
                                            false));
                        }
                        if (!methodEntry.isInit()) {
                            tmpInstructions.add(new LdcInsnNode(methodEntry.getName()));
                        }
                        tmpInstructions.add(new LdcInsnNode(parameterTypes.length));
                        tmpInstructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, Type.getInternalName(Class.class)));
                        for (int i = 0; i < parameterTypes.length; i++) {
                            tmpInstructions.add(new InsnNode(Opcodes.DUP));
                            tmpInstructions.add(new LdcInsnNode(i));
                            if (methodEntry.parameterVisisbles[i]) {
                                tmpInstructions.add(ASM.getClassLdc(parameterTypes[i]));
                            } else {
                                tmpInstructions.add(new LdcInsnNode(parameterTypes[i].getDescriptor()));
                                tmpInstructions.add(
                                        new MethodInsnNode(
                                                Opcodes.INVOKESTATIC,
                                                ASM.getInternalName(ClassInfo.class),
                                                "forInternalName",
                                                "(Ljava/lang/String;)Ljava/lang/Class;",
                                                false));
                            }
                            tmpInstructions.add(new InsnNode(Opcodes.AASTORE));
                        }
                        tmpInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL, 
                                "java/lang/Class", 
                                methodEntry.isInit() ? "getDeclaredConstructor" : "getDeclaredMethod", 
                                (methodEntry.isInit() ? "([Ljava/lang/Class;)" : "(Ljava/lang/String;[Ljava/lang/Class;)") +
                                ASM.getDescriptor(getMethodFieldType(methodEntry)),
                                false));
                        tmpInstructions.add(new InsnNode(Opcodes.DUP));
                        tmpInstructions.add(new LdcInsnNode(true));
                        tmpInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL, 
                                Type.getInternalName(AccessibleObject.class), 
                                "setAccessible", 
                                "(Z)V",
                                false));
                        tmpInstructions.add(
                                new FieldInsnNode(
                                        Opcodes.PUTSTATIC, 
                                        this.newInternalName, 
                                        getMethodFieldName(methodEntry), 
                                        ASM.getDescriptor(getMethodFieldType(methodEntry))));
                        tmpInstructions.add(endTryLabelNode);
                        tmpInstructions.add(new JumpInsnNode(Opcodes.GOTO, endCatchLabelNode));
                        tmpInstructions.add(catchNoSuchMethodLabelNode);
                        tmpInstructions.add(new InsnNode(Opcodes.POP)); //pop exception
                        tmpInstructions.add(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(AssertionError.class)));
                        tmpInstructions.add(new InsnNode(Opcodes.DUP));
                        tmpInstructions.add(new LdcInsnNode("Internal bug: 3"));
                        tmpInstructions.add(
                                new MethodInsnNode(
                                        Opcodes.INVOKESPECIAL, 
                                        Type.getInternalName(AssertionError.class), 
                                        "<init>", 
                                        "(Ljava/lang/String;)V",
                                        false));
                        tmpInstructions.add(new InsnNode(Opcodes.ATHROW));
                        if (!methodEntry.ownerVisible || !methodEntry.areAllParametersVisible()) {
                            tmpInstructions.add(catchClassNotFoundLabelNode);
                            tmpInstructions.add(new VarInsnNode(Opcodes.ASTORE, 0)); //pop exception
                            tmpInstructions.add(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(AssertionError.class)));
                            tmpInstructions.add(new InsnNode(Opcodes.DUP));
                            tmpInstructions.add(new LdcInsnNode("Internal bug: 4"));
                            tmpInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); //pop exception
                            tmpInstructions.add(
                                    new MethodInsnNode(
                                            Opcodes.INVOKESPECIAL, 
                                            Type.getInternalName(AssertionError.class), 
                                            "<init>", 
                                            "(Ljava/lang/String;Ljava/lang/Throwable;)V",
                                            false));
                            tmpInstructions.add(new InsnNode(Opcodes.ATHROW));
                        }
                        tmpInstructions.add(endCatchLabelNode);
                    }
                }
            }
            Descriptor clinitDescriptor = new Descriptor("<clinit>", "()V");
            MethodNode clinitNode = this.newClassEntry.getMethodEntry(clinitDescriptor).methodNode;
            if (clinitNode.instructions.size() == 0 && tmpInstructions.size() == 0) {
                this.newClassEntry.methodEntries.remove(clinitDescriptor);
                this.newClassEntry.classNode.methods.remove(clinitNode);
            } else {
                if (clinitNode.instructions.size() != 0) {
                    clinitNode.instructions.insertBefore(clinitNode.instructions.getFirst(), tmpInstructions);
                } else {
                    clinitNode.instructions.add(tmpInstructions);
                }
                clinitNode.tryCatchBlocks.addAll(0, tmpTryCatchBlocks);
                clinitNode.instructions.add(new InsnNode(Opcodes.RETURN));
            }
            
            //newClassNode.accept(new org.objectweb.asm.util.TraceClassVisitor(new java.io.PrintWriter(System.out)));
        }
        
        private MethodNode createGetterMethod(FieldEntry fieldEntry) {
            boolean isStatic = (fieldEntry.getAccess() & Opcodes.ACC_STATIC) != 0;
            String originalDesc = fieldEntry.getDescriptor();
            TypeCategory thatTypeCategory = TypeCategory.of(originalDesc);
            LabelNode tryLabelNode = new LabelNode(new Label());
            LabelNode catchLabelNode = new LabelNode(new Label());
            
            MethodNode methodNode = new MethodNode(Opcodes.ASM5);
            methodNode.access = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC;
            methodNode.name = getFieldGetterName(fieldEntry);
            methodNode.desc = fieldEntry.getterDescriptorToAvoidIllegalAccessing;
            methodNode.exceptions = new ArrayList<String>();
            methodNode.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
            methodNode.localVariables = new ArrayList<LocalVariableNode>();
            
            methodNode.tryCatchBlocks.add(
                    new TryCatchBlockNode(
                            tryLabelNode, 
                            catchLabelNode, 
                            catchLabelNode, 
                            Type.getInternalName(IllegalAccessException.class)));
            InsnList instructions = new InsnList();
            
            /*
             * try {
             */
            instructions.add(tryLabelNode);
            instructions.add(
                    new FieldInsnNode(
                            Opcodes.GETSTATIC, 
                            this.newInternalName, 
                            getFieldFieldName(fieldEntry), 
                            Type.getDescriptor(Field.class)));
            if (isStatic) {
                instructions.add(new InsnNode(Opcodes.ACONST_NULL));
            } else {
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            }
            instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL, 
                            Type.getInternalName(Field.class), 
                            "get", 
                            "(Ljava/lang/Object;)Ljava/lang/Object;",
                            false));
            if (thatTypeCategory == TypeCategory.A) {
                if (fieldEntry.typeVisible && !originalDesc.equals("java/lang/Object")) {
                    instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, ASM.getInternalName(originalDesc)));
                }
            } else {
                instructions.add(ASM.getUnboxInsnList(originalDesc, true));
            }
            instructions.add(new InsnNode(thatTypeCategory.getReturnCode()));
            
            /*
             * catch (IllegalAccessException ex)
             */
            instructions.add(catchLabelNode);
            instructions.add(new InsnNode(Opcodes.POP)); //pop exception
            instructions.add(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(AssertionError.class)));
            instructions.add(new InsnNode(Opcodes.DUP));
            instructions.add(new LdcInsnNode("Internal bug: 5"));
            instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESPECIAL, 
                    Type.getInternalName(AssertionError.class), 
                    "<init>", 
                    "(Ljava/lang/String;)V",
                    false));
            instructions.add(new InsnNode(Opcodes.ATHROW));
            
            methodNode.instructions = instructions;
            return methodNode;
        }
        
        private MethodNode createSetterMethod(FieldEntry fieldEntry) {
            boolean isStatic = (fieldEntry.getAccess() & Opcodes.ACC_STATIC) != 0;
            String thatDesc = fieldEntry.getDescriptor();
            TypeCategory thatTypeCategory = TypeCategory.of(thatDesc);
            LabelNode tryLabelNode = new LabelNode(new Label());
            LabelNode catchLabelNode = new LabelNode(new Label());
            
            MethodNode methodNode = new MethodNode(Opcodes.ASM5);
            methodNode.access = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC;
            methodNode.name = getFieldSetterName(fieldEntry);
            methodNode.desc = fieldEntry.setterDescriptorToAvoidIllegalAccessing;
            methodNode.exceptions = new ArrayList<String>();
            methodNode.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
            methodNode.localVariables = new ArrayList<LocalVariableNode>();
            
            methodNode.tryCatchBlocks.add(
                    new TryCatchBlockNode(
                            tryLabelNode, 
                            catchLabelNode, 
                            catchLabelNode, 
                            Type.getInternalName(IllegalAccessException.class)));
            InsnList instructions = new InsnList();
            
            /*
             * try {
             */
            instructions.add(tryLabelNode);
            instructions.add(
                    new FieldInsnNode(
                            Opcodes.GETSTATIC, 
                            this.newInternalName, 
                            getFieldFieldName(fieldEntry), 
                            Type.getDescriptor(Field.class)));
            if (isStatic) {
                instructions.add(new InsnNode(Opcodes.ACONST_NULL));
            } else {
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            }
            instructions.add(ASM.getBoxInsnList(thatDesc, new VarInsnNode(thatTypeCategory.getLoadCode(), isStatic ? 0 : 1)));
            instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL, 
                            Type.getInternalName(Field.class), 
                            "set", 
                            "(Ljava/lang/Object;Ljava/lang/Object;)V",
                            false));
            instructions.add(new InsnNode(Opcodes.RETURN));
            
            /*
             * catch (IllegalAccessException ex)
             */
            instructions.add(catchLabelNode);
            instructions.add(new InsnNode(Opcodes.POP)); //pop exception
            instructions.add(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(AssertionError.class)));
            instructions.add(new InsnNode(Opcodes.DUP));
            instructions.add(new LdcInsnNode("Internal bug: 6"));
            instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESPECIAL, 
                    Type.getInternalName(AssertionError.class), 
                    "<init>", 
                    "(Ljava/lang/String;)V",
                    false));
            instructions.add(new InsnNode(Opcodes.ATHROW));
            
            methodNode.instructions = instructions;
            return methodNode;
        }
        
        private MethodNode createInovcationMethod(MethodEntry methodEntry) {
            boolean isStatic = (methodEntry.getAccess() & Opcodes.ACC_STATIC) != 0;
            Type returnType = Type.getReturnType(methodEntry.getDescriptor().desc);
            Type[] parameterTypes = Type.getArgumentTypes(methodEntry.getDescriptor().desc);
            LabelNode tryLabelNode = new LabelNode(new Label());
            LabelNode catchIllegalAccessLabelNode = new LabelNode(new Label());
            LabelNode catchInvocationTargetLabelNode = new LabelNode(new Label());
            
            MethodNode methodNode = new MethodNode(Opcodes.ASM5);
            methodNode.access = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC;
            methodNode.name = this.getMethodInvocationName(methodEntry);
            methodNode.desc = methodEntry.descriptorToAvoidIllegalAccessing;
            methodNode.exceptions = methodEntry.getExceptions();
            methodNode.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
            methodNode.localVariables = new ArrayList<LocalVariableNode>();
            
            methodNode.tryCatchBlocks.add(
                    new TryCatchBlockNode(
                            tryLabelNode, 
                            catchIllegalAccessLabelNode, 
                            catchIllegalAccessLabelNode, 
                            Type.getInternalName(IllegalAccessException.class)));
            methodNode.tryCatchBlocks.add(
                    new TryCatchBlockNode(
                            tryLabelNode, 
                            catchIllegalAccessLabelNode, 
                            catchInvocationTargetLabelNode, 
                            Type.getInternalName(InvocationTargetException.class)));
            InsnList instructions = new InsnList();
            
            /*
             * try {
             */
            instructions.add(tryLabelNode);
            instructions.add(
                    new FieldInsnNode(
                            Opcodes.GETSTATIC, 
                            this.newInternalName, 
                            getMethodFieldName(methodEntry), 
                            ASM.getDescriptor(getMethodFieldType(methodEntry))));
            if (!methodEntry.isInit()) {
                if (isStatic) {
                    instructions.add(new InsnNode(Opcodes.ACONST_NULL));
                } else {
                    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                }
            }
            instructions.add(new LdcInsnNode(parameterTypes.length));
            instructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
            if (parameterTypes.length != 0) {
                int slot = isStatic || methodEntry.isInit() ? 0 : 1;
                for (int i = 0; i < parameterTypes.length; i++) {
                    instructions.add(new InsnNode(Opcodes.DUP));
                    instructions.add(new LdcInsnNode(i));
                    Type parameterType = parameterTypes[i];
                    TypeCategory typeCategory = TypeCategory.of(parameterType);
                    instructions.add(ASM.getBoxInsnList(parameterType, new VarInsnNode(typeCategory.getLoadCode(), slot)));
                    instructions.add(new InsnNode(Opcodes.AASTORE));
                    slot += typeCategory.getSlotCount();
                }
            }
            if (methodEntry.isInit()) {
                instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL, 
                                Type.getInternalName(Constructor.class), 
                                "newInstance", 
                                "([Ljava/lang/Object;)Ljava/lang/Object;",
                                false));
                if (methodEntry.ownerVisible) {
                    instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, methodEntry.declaringClassEntry.getInternalName()));
                }
                instructions.add(new InsnNode(Opcodes.ARETURN));
            } else {
                instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL, 
                                Type.getInternalName(Method.class), 
                                "invoke", 
                                "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                                false));
                TypeCategory returnTypeCategory = TypeCategory.of(returnType);
                if (returnTypeCategory == TypeCategory.A) {
                    if (methodEntry.returnVisible) {
                        instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, returnType.getInternalName()));
                    }
                } else if (returnTypeCategory != TypeCategory.V &&
                        !ASM.getInternalName(returnType).equals(Type.getInternalName(Object.class))) {
                    instructions.add(ASM.getUnboxInsnList(ASM.getInternalName(returnType), true));
                }
                instructions.add(new InsnNode(returnTypeCategory.getReturnCode()));
            }
            
            /*
             * } catch (IllegalAccessException ex) {
             */
            instructions.add(catchIllegalAccessLabelNode);
            instructions.add(new InsnNode(Opcodes.POP)); //pop exception
            instructions.add(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(AssertionError.class)));
            instructions.add(new InsnNode(Opcodes.DUP));
            instructions.add(new LdcInsnNode("Internal bug: 7"));
            instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESPECIAL, 
                    Type.getInternalName(AssertionError.class), 
                    "<init>", 
                    "(Ljava/lang/String;)V",
                    false));
            instructions.add(new InsnNode(Opcodes.ATHROW));
            
            /*
             * } catch (InvocationTargetException ex)
             */
            instructions.add(catchInvocationTargetLabelNode);
            instructions.add(
                    new MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            Type.getInternalName(InvocationTargetException.class),
                            "getTargetException",
                            "()Ljava/lang/Throwable;",
                            false));
            instructions.add(new InsnNode(Opcodes.ATHROW));
            
            methodNode.instructions = instructions;
            return methodNode;
        }
        
        private String getMethodInvocationName(MethodEntry methodEntry) {
            if (methodEntry.isInit()) {
                return "new{owner=\"" + 
                        methodEntry.declaringClassEntry.clazz.getName().replace('.', '\\') +
                        "\"}";
            }
            return "invoke{name=\"" +
                    methodEntry.getName() +
                    "\",owner=\"" +
                    methodEntry.declaringClassEntry.clazz.getName().replace('.', '\\') +
                    "\"}"; 
        }
        
        private boolean isVisibleClass(Class<?> clazz) {
            return this.isVisibleClass0(clazz, true);
        }
        
        private boolean isVisibleClass0(Class<?> clazz, boolean isLeafClass) {
            if (clazz.isArray()) {
                return isVisibleClass0(clazz.getComponentType(), isLeafClass);
            }
            int modifiers = clazz.getModifiers();
            Class<?> declaringClass = clazz.getDeclaringClass();
            if (!Modifier.isPublic(modifiers)) {
                if (Modifier.isPrivate(modifiers) && ClassEnhancer.this.usedEnhancerLoader()) {
                    return false;
                }
                //For JVM(not java), private type under the same package and same class loader are visible
                if (this.targetPackage != clazz.getPackage()) {
                    if (!Modifier.isProtected(modifiers)) {
                        return false;
                    }
                    if (!isLeafClass) {
                        return false;
                    }
                    if (declaringClass == null || 
                            !clazz
                            .getDeclaringClass()
                            .isAssignableFrom(this.classEntries.lastEntry()
                            .getValue().clazz)) {
                        return false;
                    }
                }
            }
            return declaringClass != null ? isVisibleClass0(declaringClass, false) : true;
        }
        
        private ClassEntry getInovkedEntry(String internalName) {
            if (internalName.equals(this.newInternalName)) {
                return this.newClassEntry;
            }
            return this.getInovkedEntry0(ClassInfo.forInternalName(internalName));
        }
        
        private ClassEntry getInovkedEntry0(Class<?> clazz) {
            String internalName = ASM.getInternalName(clazz);
            ClassEntry ce = this.classEntries.get(internalName);
            if (ce == null) {
                ce = this.invokedEntries.get(internalName);
                if (ce == null) {
                    ce = new ClassEntry(clazz, false);
                    this.invokedEntries.put(internalName, ce);
                    if (ce.clazz.getSuperclass() != null) {
                        ce.superClassEntry = this.getInovkedEntry0(clazz.getSuperclass());
                    }
                }
            }
            return ce;
        }
    }
    
    public static final class MethodSource {
        
        MethodEntry methodEntry;
        
        private MethodSource(MethodEntry methodEntry) {
            this.methodEntry = methodEntry;
        }
        
        public Method getMethod() {
            return this.methodEntry.method;
        }

        public InsnList getInstructions() {
            return this.methodEntry.getInstructions();
        }
        
        public List<TryCatchBlockNode> getTryCatchBlocks() {
            return this.methodEntry.getTryCatchBlocks();
        }
        
        public List<LocalVariableNode> getLocalVariables() {
            return this.methodEntry.getLocalVariableNodes();
        }
        
        public InsnList getOldInstructions() {
            return this.methodEntry.methodNode.instructions;
        }
        
        public List<TryCatchBlockNode> getOldTryCatchBlocks() {
            return this.methodEntry.methodNode.tryCatchBlocks;
        }
        
        public List<LocalVariableNode> getOldLocalVariables() {
            return this.methodEntry.methodNode.localVariables;
        }
        
    }
    
    protected static class MethodSourceFactory {
        
        private Context context;
        
        private MethodSourceFactory(Context context) {
            this.context = context;
        }
        
        public MethodSource getMethodSource(Method method) {
            ClassEntry classEntry = this.context.classEntries.get(
                    Type.getInternalName(method.getDeclaringClass()));
            if (classEntry != null) {
                MethodEntry methodEntry = classEntry.methodEntries.get(
                        new Descriptor(method.getName(), Type.getMethodDescriptor(method)));
                return new MethodSource(methodEntry);
            }
            throw new IllegalArgumentException(
                    canNotFindMethodSource(
                            this.context.owner().getClass(),
                            method,
                            this.context.classEntries.firstEntry().getValue().clazz
                    )
            );
        }
    }
    
    private static class ClassEntry {
        
        private Class<?> clazz;
        
        private ClassNode classNode;
        
        private ClassEntry superClassEntry;
        
        private XOrderedMap<String, FieldEntry> fieldEntries;
        
        private XOrderedMap<Descriptor, MethodEntry> methodEntries;
        
        ClassEntry(Class<?> clazz) {
            this(clazz, true);
        }
        
        //For inherited class entries
        ClassEntry(Class<?> clazz, boolean readByteCode) {
            ClassReader cr;
            try {
                cr = new ClassReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class"));
            } catch (IOException ex) {
                throw new IllegalArgumentException(canNotReadByteCode(clazz), ex);
            }       
            ClassNode classNode = new ClassNode(Opcodes.ASM5);
            cr.accept(classNode, ClassReader.SKIP_DEBUG);
            this.clazz = clazz;
            XOrderedMap<String, FieldEntry> fieldEntries = new LinkedHashMap<String, FieldEntry>();
            XOrderedMap<Descriptor, MethodEntry> methodEntries = new LinkedHashMap<Descriptor, MethodEntry>();
            if (readByteCode) {
                this.classNode = classNode;
                for (FieldNode fieldNode : (List<FieldNode>)classNode.fields) {
                    fieldEntries.put(fieldNode.name, new FieldEntry(this, fieldNode));
                }
                this.fieldEntries = fieldEntries;
                for (MethodNode methodNode : (List<MethodNode>)classNode.methods) {
                    MethodEntry methodEntry = new MethodEntry(this, methodNode);
                    methodEntries.put(methodEntry.getDescriptor(), methodEntry);
                }
                this.methodEntries = methodEntries;
            } else {
                for (Field field : clazz.getDeclaredFields()) {
                    fieldEntries.put(field.getName(), new FieldEntry(this, field));
                }
                this.fieldEntries = fieldEntries;
                for (Method method : clazz.getDeclaredMethods()) {
                    MethodEntry methodEntry = new MethodEntry(this, method);
                    methodEntries.put(methodEntry.getDescriptor(), methodEntry);
                }
                for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                    MethodEntry methodEntry = new MethodEntry(this, constructor);
                    methodEntries.put(methodEntry.getDescriptor(), methodEntry);
                }
                this.methodEntries = methodEntries;
            }
        }
        
        //Only for new class entry
        ClassEntry(ClassNode classNode) {
            this.classNode = classNode;
            XOrderedMap<String, FieldEntry> fieldEntries = new LinkedHashMap<String, FieldEntry>();
            for (FieldNode fieldNode : (List<FieldNode>)classNode.fields) {
                fieldEntries.put(fieldNode.name, new FieldEntry(this, fieldNode));
            }
            this.fieldEntries = fieldEntries;
            XOrderedMap<Descriptor, MethodEntry> methodEntries = new LinkedHashMap<Descriptor, MethodEntry>();
            for (MethodNode methodNode : (List<MethodNode>)classNode.methods) {
                MethodEntry methodEntry = new MethodEntry(this, methodNode);
                methodEntries.put(methodEntry.getDescriptor(), methodEntry);
            }
            this.methodEntries = methodEntries;
        }
        
        MethodEntry getMethodEntry(Descriptor descriptor) {
            MethodEntry methodEntry = this.methodEntries.get(descriptor);
            if (methodEntry == null) {
                if (this.superClassEntry == null) {
                    return null;
                }
                methodEntry = this.superClassEntry.getMethodEntry(descriptor);
            }
            return methodEntry;
        }
        
        MethodEntry getMethodEntry(String name, String desc) {
            return this.getMethodEntry(new Descriptor(name, desc));
        }
        
        FieldEntry getFieldEntry(String name) {
            FieldEntry fieldEntry = this.fieldEntries.get(name);
            if (fieldEntry == null) {
                if (this.superClassEntry == null) {
                    return null;
                }
                fieldEntry = this.superClassEntry.getFieldEntry(name);
            }
            return fieldEntry;
        }
        
        String getInternalName() {
            if (this.classNode != null) {
                return this.classNode.name;
            }
            return ASM.getInternalName(this.clazz);
        }
    }
    
    private static class FieldEntry {
        
        ClassEntry declaringClassEntry;
        
        FieldNode fieldNode;
        
        Field field;
        
        boolean ownerVisible;
        
        boolean typeVisible;
        
        String getterDescriptorToAvoidIllegalAccessing;
        
        String setterDescriptorToAvoidIllegalAccessing;
        
        FieldEntry(ClassEntry declaringClassEntry, FieldNode fieldNode) {
            super();
            if (declaringClassEntry.clazz != null) {
                try {
                    this.field = declaringClassEntry.clazz.getDeclaredField(fieldNode.name);
                } catch (NoSuchFieldException | SecurityException ex) {
                    throw new AssertionError("Internal bug", ex);
                }
            }
            this.declaringClassEntry = declaringClassEntry;
            this.fieldNode = fieldNode;
        }
        
        FieldEntry(ClassEntry declaringClassEntry, Field field) {
            super();
            this.declaringClassEntry = declaringClassEntry;
            this.field = field;
        }
        
        String getName() {
            if (this.fieldNode != null) {
                return this.fieldNode.name;
            }
            return this.field.getName();
        }
        
        int getAccess() {
            if (this.fieldNode != null) {
                return this.fieldNode.access;
            }
            return this.field.getModifiers();
        }
        
        String getDescriptor() {
            if (this.fieldNode != null) {
                return this.fieldNode.desc;
            }
            return ASM.getDescriptor(this.field.getType());
        }
    }
    
    private static class MethodEntry {
        
        ClassEntry declaringClassEntry;
        
        MethodNode methodNode;
        
        Method method; //Be careful, it is null if its is a method of new class.
        
        Constructor<?> constructor; //Be careful, it is null if its is a constructor of new class.
        
        Descriptor descriptor;
        
        InsnList instructions;
        
        List<TryCatchBlockNode> tryCatchBlocks;
        
        List<LocalVariableNode> localVariables;
        
        MethodEntry overrideTarget;
        
        MethodEntry bridgeTarget;
        
        MethodEntry polymorphicTarget;
        
        boolean changed;
        
        //Only available when this.method or this.constructor is null
        boolean ownerVisible;
        
        //Only available when this.method or this.constructor is null
        boolean returnVisible;
        
        //Only available when this.method or this.constructor is null
        boolean[] parameterVisisbles;
        
        String descriptorToAvoidIllegalAccessing;
        
        Set<MethodEntry> replacedForSet;
        
        MethodEntry(ClassEntry declaringClassEntry, MethodNode methodNode) {
            this.declaringClassEntry = declaringClassEntry;
            this.methodNode = methodNode;
            this.descriptor = new Descriptor(this.methodNode.name, this.methodNode.desc);
            if (declaringClassEntry.clazz != null) {
                boolean valid = false;
                if (methodNode.name.equals("<init>")) {
                    for (Constructor<?> constructor : declaringClassEntry.clazz.getDeclaredConstructors()) {
                        if (ASM.getDescriptor(constructor).equals(methodNode.desc)) {
                            this.constructor = constructor;
                            valid = true;
                            break;
                        }
                    }
                } else {
                    for (Method method : declaringClassEntry.clazz.getDeclaredMethods()) {
                        if (methodNode.name.equals(method.getName()) && ASM.getDescriptor(method).equals(methodNode.desc)) {
                            this.method = method;
                            valid = true;
                            break;
                        }
                    }
                }
                if (!valid && !methodNode.name.equals("<clinit>")) {
                    throw new AssertionError("no such method");
                }
            }
            
            if (declaringClassEntry.clazz != null /* Not new class entry */) {
                this.instructions = cloneInsnList(this.methodNode.instructions);
                
                if (this.methodNode.tryCatchBlocks != null) {
                    this.tryCatchBlocks = cloneTryCatchBlocks((List<TryCatchBlockNode>)this.methodNode.tryCatchBlocks);
                } else {
                    this.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
                }
                
                if (this.methodNode.localVariables != null) {
                    this.localVariables = cloneLocalVariables(this.methodNode.localVariables);
                } else {
                    this.localVariables = new ArrayList<LocalVariableNode>();
                }
            } else {
                this.instructions = this.methodNode.instructions;
                this.tryCatchBlocks = this.methodNode.tryCatchBlocks;
                this.localVariables = this.methodNode.localVariables;
            }
            
            this.ownerVisible = true;
            this.returnVisible = true;
            if (this.method != null) {
                this.parameterVisisbles = new boolean[this.method.getParameterTypes().length];
                Arrays.fill(this.parameterVisisbles, true);
            } else if (this.constructor != null) {
                this.parameterVisisbles = new boolean[this.constructor.getParameterTypes().length];
                Arrays.fill(this.parameterVisisbles, true);
            }
        }
        
        MethodEntry(ClassEntry declaringClassEntry, Method method) {
            this.declaringClassEntry = declaringClassEntry;
            this.method = method;
            this.descriptor = new Descriptor(method.getName(), ASM.getDescriptor(method));
            this.ownerVisible = true;
            this.returnVisible = true;
            this.parameterVisisbles = new boolean[method.getParameterTypes().length];
            Arrays.fill(this.parameterVisisbles, true);
        }
        
        MethodEntry(ClassEntry declaringClassEntry, Constructor<?> constructor) {
            this.declaringClassEntry = declaringClassEntry;
            this.constructor = constructor;
            this.descriptor = new Descriptor("<init>", ASM.getDescriptor(constructor));
            this.ownerVisible = true;
            this.returnVisible = true;
            this.parameterVisisbles = new boolean[constructor.getParameterTypes().length];
            Arrays.fill(this.parameterVisisbles, true);
        }

        InsnList getInstructions() {
            return this.instructions;
        }

        List<TryCatchBlockNode> getTryCatchBlocks() {
            return this.tryCatchBlocks;
        }

        List<LocalVariableNode> getLocalVariableNodes() {
            return this.localVariables;
        }
        
        boolean setChanged() {
            if (this.declaringClassEntry.clazz != null) {
                return this.changed = this.calcIsChanged();
            }
            throw new AssertionError("setChanged");
        }
        
        Descriptor getDescriptor() {
            return this.descriptor;
        }
        
        List<String> getExceptions() {
            if (this.methodNode != null) {
                return MACollections.unmodifiable(this.methodNode.exceptions);
            }
            Class<?>[] exceptionTypes = 
                    this.method != null ? 
                            this.method.getExceptionTypes() : 
                            this.constructor.getExceptionTypes();
            List<String> list = new ArrayList<>(exceptionTypes.length);
            for (Class<?> exceptionType : exceptionTypes) {
                list.add(ASM.getInternalName(exceptionType));
            }
            return MACollections.unmodifiable(list);
        }
        
        boolean isInit() {
            if (this.methodNode != null) {
                return this.methodNode.name.equals("<init>");
            }
            return this.constructor != null;
        }
        
        boolean isClinit() {
            return this.methodNode.name.equals("<clinit>");
        }
        
        boolean isOverriable(Package targetPackage) {
            if (this.polymorphicTarget != this) {
                return false;
            }
            if ((this.methodNode.access & (Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE)) != 0){
                return false;
            }
            return isVisible(this.methodNode.access, this.declaringClassEntry.clazz.getPackage(), targetPackage);
        }
        
        boolean isReplaced() {
            return this.replacedForSet != null && !this.replacedForSet.isEmpty();
        }
        
        boolean isReplacedFor(MethodEntry invoker) {
            return this.replacedForSet != null && this.replacedForSet.contains(invoker);
        }
        
        void replaceFor(MethodEntry invoker) {
            if (this.declaringClassEntry.clazz != null) {
                Set<MethodEntry> replacedForSet = this.replacedForSet;
                if (replacedForSet == null) {
                    this.replacedForSet = replacedForSet = new HashSet<MethodEntry>();
                }
                replacedForSet.add(invoker);
            }
        }
        
        String getName() {
            if (this.methodNode != null) {
                return methodNode.name;
            }
            if (this.method != null) {
                return this.method.getName();
            }
            return "<init>";
        }
        
        int getAccess() {
            if (this.methodNode != null) {
                return this.methodNode.access;
            }
            if (this.method != null) {
                return this.method.getModifiers();
            }
            return this.constructor.getModifiers();
        }
        
        Member getMember() {
            if (this.method != null) {
                return this.method;
            }
            return this.constructor;
        }
        
        boolean areAllParametersVisible() {
            for (boolean parameterVisible : this.parameterVisisbles) {
                if (!parameterVisible) {
                    return false;
                }
            }
            return true;
        }
        
        private boolean calcIsChanged() {
            if (this.instructions.size() != this.methodNode.instructions.size()) {
                return true;
            }
            for (AbstractInsnNode 
                    oldNode = this.methodNode.instructions.getFirst(),
                    newNode = this.instructions.getFirst()
                    ;
                    oldNode != null
                    ;
                    oldNode = oldNode.getNext(),
                    newNode = newNode.getNext()) {
                if (!insnNodeEquals(oldNode, newNode)) {
                    return true;
                }
            }
        
            if (this.methodNode.tryCatchBlocks == null) {
                if (!this.tryCatchBlocks.isEmpty()) {
                    return true;
                }
            } else {
                if (this.tryCatchBlocks.size() != this.methodNode.tryCatchBlocks.size()) {
                    return true;
                }
                Iterator<TryCatchBlockNode> tcbItr1 = this.tryCatchBlocks.iterator();
                Iterator<TryCatchBlockNode> tcbItr2 = this.methodNode.tryCatchBlocks.iterator();
                while (tcbItr1.hasNext()) {
                    TryCatchBlockNode node1 = tcbItr1.next();
                    TryCatchBlockNode node2 = tcbItr2.next();
                    if (!insnNodeEquals(node1.start, node2.start) ||
                            !insnNodeEquals(node1.end, node2.end) ||
                            !insnNodeEquals(node1.handler, node2.handler) ||
                            !nullSafeEquals(node1.type, node2.type)) {
                        return true;
                    }
                }
            }
            
            if (this.methodNode.localVariables == null) {
                if (!this.localVariables.isEmpty()) {
                    return true;
                }
            } else {
                if (this.localVariables.size() != this.methodNode.localVariables.size()) {
                    return true;
                }
                Iterator<LocalVariableNode> lvItr1 = this.localVariables.iterator();
                Iterator<LocalVariableNode> lvItr2 = this.methodNode.localVariables.iterator();
                while (lvItr1.hasNext()) {
                    LocalVariableNode node1 = lvItr1.next();
                    LocalVariableNode node2 = lvItr2.next();
                    if (!node1.name.equals(node2.name) ||
                            !node1.desc.equals(node2.desc) ||
                            !insnNodeEquals(node1.start, node2.start) ||
                            !insnNodeEquals(node1.end, node2.end)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        private static boolean insnNodeEquals(AbstractInsnNode insnNode1, AbstractInsnNode insnNode2) {
            if (insnNode1.getClass() != insnNode2.getClass()) {
                return false;
            }
            if (insnNode1 instanceof FieldInsnNode) {
                FieldInsnNode node1 = (FieldInsnNode)insnNode1;
                FieldInsnNode node2 = (FieldInsnNode)insnNode2;
                return node1.getOpcode() == node2.getOpcode() &&
                        node1.owner.equals(node2.owner) &&
                        node1.name.equals(node2.name) &&
                        node1.desc.equals(node2.desc);
            }
            if (insnNode1 instanceof FrameNode) {
                FrameNode node1 = (FrameNode)insnNode1;
                FrameNode node2 = (FrameNode)insnNode2;
                return node1.type == node2.type &&
                        nullSafeListEquals(node1.local, node2.local) &&
                        nullSafeListEquals(node1.stack, node2.stack);
            }
            if (insnNode1 instanceof IincInsnNode) {
                IincInsnNode node1 = (IincInsnNode)insnNode1;
                IincInsnNode node2 = (IincInsnNode)insnNode2;
                return node1.var == node2.var &&
                        node1.incr == node2.incr;
            }
            if (insnNode1 instanceof InsnNode) {
                return insnNode1.getOpcode() == insnNode2.getOpcode();
            }
            if (insnNode1 instanceof IntInsnNode) {
                IntInsnNode node1 = (IntInsnNode)insnNode1;
                IntInsnNode node2 = (IntInsnNode)insnNode2;
                return node1.getOpcode() == node2.getOpcode() &&
                        node1.operand == node2.operand;
            }
            if (insnNode1 instanceof JumpInsnNode) {
                JumpInsnNode node1 = (JumpInsnNode)insnNode1;
                JumpInsnNode node2 = (JumpInsnNode)insnNode2;
                return node1.getOpcode() == node2.getOpcode() &&
                        insnNodeEquals(node1.label, node2.label);
            }
            if (insnNode1 instanceof LabelNode) {
                LabelNode node1 = (LabelNode)insnNode1;
                LabelNode node2 = (LabelNode)insnNode2;
                return node1.getLabel() == node2.getLabel();
            }
            if (insnNode1 instanceof LdcInsnNode) {
                LdcInsnNode node1 = (LdcInsnNode)insnNode1;
                LdcInsnNode node2 = (LdcInsnNode)insnNode2;
                return node1.cst == null ? node2.cst == null : node1.cst.equals(node2.cst);
            }
            if (insnNode1 instanceof LineNumberNode) {
                LineNumberNode node1 = (LineNumberNode)insnNode1;
                LineNumberNode node2 = (LineNumberNode)insnNode2;
                return node1.line == node2.line &&
                        insnNodeEquals(node1.start, node2.start);
            }
            if (insnNode1 instanceof LookupSwitchInsnNode) {
                LookupSwitchInsnNode node1 = (LookupSwitchInsnNode)insnNode1;
                LookupSwitchInsnNode node2 = (LookupSwitchInsnNode)insnNode2;
                if (!insnNodeEquals(node1.dflt, node2.dflt)) {
                    return false;
                }
                if (!node1.keys.equals(node2.keys)) {
                    return false;
                }
                Iterator<LabelNode> itr1 = node1.labels.iterator();
                Iterator<LabelNode> itr2 = node2.labels.iterator();
                while (itr1.hasNext() && itr2.hasNext()) {
                    if (!insnNodeEquals(itr1.next(), itr2.next())) {
                        return false;
                    }
                }
                return itr1.hasNext() == itr2.hasNext();
            }
            if (insnNode1 instanceof MethodInsnNode) {
                MethodInsnNode node1 = (MethodInsnNode)insnNode1;
                MethodInsnNode node2 = (MethodInsnNode)insnNode2;
                return node1.getOpcode() == node2.getOpcode() &&
                        node1.owner.equals(node2.owner) &&
                        node1.name.equals(node2.name) &&
                        node1.desc.equals(node2.desc);
            }
            if (insnNode1 instanceof MultiANewArrayInsnNode) {
                MultiANewArrayInsnNode node1 = (MultiANewArrayInsnNode)insnNode1;
                MultiANewArrayInsnNode node2 = (MultiANewArrayInsnNode)insnNode2;
                return node1.desc.equals(node2.desc) &&
                        node1.dims == node2.dims;
            }
            if (insnNode1 instanceof TableSwitchInsnNode) {
                TableSwitchInsnNode node1 = (TableSwitchInsnNode)insnNode1;
                TableSwitchInsnNode node2 = (TableSwitchInsnNode)insnNode2;
                if (node1.min != node2.min || node1.max != node2.max) {
                    return false;
                }
                if (!insnNodeEquals(node1.dflt, node2.dflt)) {
                    return false;
                }
                Iterator<LabelNode> itr1 = node1.labels.iterator();
                Iterator<LabelNode> itr2 = node2.labels.iterator();
                while (itr1.hasNext() && itr2.hasNext()) {
                    if (!insnNodeEquals(itr1.next(), itr2.next())) {
                        return false;
                    }
                }
                return itr1.hasNext() == itr2.hasNext();
            }
            if (insnNode1 instanceof TypeInsnNode) {
                TypeInsnNode node1 = (TypeInsnNode)insnNode1;
                TypeInsnNode node2 = (TypeInsnNode)insnNode2;
                return node1.getOpcode() == node2.getOpcode() &&
                        node1.desc.equals(node2.desc);
            }
            if (insnNode1 instanceof VarInsnNode) {
                VarInsnNode node1 = (VarInsnNode)insnNode1;
                VarInsnNode node2 = (VarInsnNode)insnNode2;
                return node1.getOpcode() == node2.getOpcode() &&
                        node1.var == node2.var;
            }
            throw new AssertionError("insnNodeEquals");
        }
    }
    
    private static class Descriptor {
        
        private String name;
        
        private String desc;

        public Descriptor(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode() ^ this.desc.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Descriptor)) {
                return false;
            }
            Descriptor other = (Descriptor)obj;
            return this.name.equals(other.name) && this.desc.equals(other.desc);
        }
    }
    
    private static class ReplacedMethodInsnNode extends MethodInsnNode {
        
        ReplacedMethodInsnNode(
                String newInternalName,
                Class<?> owner,
                MethodInsnNode methodInsnNode) {
            super(
                    methodInsnNode.getOpcode(), 
                    newInternalName, 
                    replaceName(owner, methodInsnNode.name), 
                    methodInsnNode.desc,
                    false);
        }
        
        static String replaceName(Class<?> owner, String name) {
            return name + "{replaceFrom:" + owner.getName().replace('.', '\\') + ']';
        }
    }
 
    @I18N
    private static native String canNotReadByteCode(Class<?> clazz);

    @I18N
    private static native String mustSupportDefaultConstructor(String parameterName, Class<?> argument);

    @I18N
    private static native String accessFieldTooEarly(Class<? extends ClassEnhancer> runtimeType, String fieldName);

    @I18N
    private static native String canNotFindMethodSource(Class<? extends ClassEnhancer> runtimeType, Method method, Class<?> rawClass);

    @I18N
    private static native String nextOpCodeOfNewMustBeDup(Class<? extends ClassEnhancer> runtimeType, String name, String desc);

    @I18N
    private static native String getMoreFieldNodesReturnNoNameNodes(Class<? extends ClassEnhancer> runtimeType);

    @I18N
    private static native String getMoreFieldNodesReturnExistingField(Class<? extends ClassEnhancer> runtimeType, String name);

    @I18N
    private static native String getMoreMethodNodesReturnNoNameNodes(Class<? extends ClassEnhancer> runtimeType);

    @I18N
    private static native String getMoreMethodNodesReturnExistingMethod(Class<? extends ClassEnhancer> runtimeType, String name, String desc);
}
