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
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;

import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.UncheckedException;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.util.reflect.ClassInfo;
import org.babyfish.util.reflect.MethodDescriptor;
import org.babyfish.util.reflect.MethodImplementation;

/**
 * @author Tao Chen
 */
public abstract class ClassWrapper {
    
    private static final Map<Class<?>, ClassWrapper> CACHE = new WeakHashMap<>();
    
    private static final ReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();
    
    protected static final String RAW = "{raw}";
    
    protected static final String ARGUMENT_FORMAT = "{argument:%d}";
    
    private Class<?> rawType;
    
    private List<Class<?>> rawInterfaceTypes;

    private String rawInternalName;
    
    private List<Class<?>> argumentTypes;
    
    private List<Class<?>> interfaceTypes;
    
    private String resultClassName;
    
    private String resultInternalName;
    
    private Package resultPackage;
    
    private ClassLoader targetClassLoader;
    
    private ProtectionDomain targetProctionDomain;
    
    private Class<?> resultClass;
    
    private BiFunction<Object, Object[], Object> factory;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected ClassWrapper(Class<?> rawType, Class<?> ... argumentTypes) {
        Arguments.mustNotBeNull("rawType", rawType);
        Arguments.mustNotBeEnum("rawType", rawType);
        Arguments.mustNotBeAnnotation("rawType", rawType);
        Arguments.mustNotBePrimitive("rawType", rawType);
        Arguments.mustNotBeArray("rawType", rawType);
        this.rawType = rawType;
        this.rawInternalName = ASM.getInternalName(rawType);
        if (argumentTypes != null) {
            for (int i = argumentTypes.length - 1; i >= 0; i--) {
                String parameterName = "argumentTypes[" + i + ']';
                Arguments.mustNotBeNull(parameterName, argumentTypes[i]);
                Arguments.mustNotBeEqualToValue(parameterName, argumentTypes[i], void.class);
            }
            this.argumentTypes = MACollections.wrap(argumentTypes.clone());
        } else {
            this.argumentTypes = MACollections.emptyList();
        }
        this.rawInterfaceTypes = this.getMinInterfaces(this.onGetRawInterfaceTypes());
        if (this.rawInterfaceTypes == null) {
            throw new IllegalProgramException(
                    eachElementOfReturnValueCanOnlyBeInterface(
                            this.getClass(),
                            "onGetRawInterfaceTypes"
                    )
            );
        }
        
        this.interfaceTypes = this.getMinInterfaces(
                rawType.isInterface() ? new Class[] { rawType } : rawType.getInterfaces(), 
                this.onGetInterfaceTypes()
        );
        if (this.interfaceTypes == null) {
            throw new IllegalProgramException(
                    eachElementOfReturnValueCanOnlyBeInterface(
                            this.getClass(),
                            "onGetInterfaceTypes"
                    )
            );
        }
        if (this.interfaceTypes.isEmpty()) {
            throw new IllegalProgramException(noInterfaces(this.getClass()));
        }
        String resultClassName = this.rawType.getName();
        if (this.useWrapperLoader()) {
            Package package_ = this.rawType.getPackage();
            if (package_ != null) {
                resultClassName = resultClassName.substring(package_.getName().length() + 1);
            }
            Package wrapperPackage = this.getClass().getPackage();
            if (wrapperPackage != null) {
                this.resultPackage = wrapperPackage;
                resultClassName = wrapperPackage.getName() + '.' + resultClassName;
            }
            this.targetClassLoader = this.getClass().getClassLoader();
            this.targetProctionDomain = this.getClass().getProtectionDomain();
        } else {
            this.resultPackage = this.rawType.getPackage();
            this.targetClassLoader = this.rawType.getClassLoader();
            this.targetProctionDomain = this.rawType.getProtectionDomain();
        }
        resultClassName += 
                "_Wrpper_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E{wrapperClass=" +
                this.getClass().getName().replace('.', '\\') +
                "}";
        this.resultClassName = resultClassName;
        this.resultInternalName = resultClassName.replace(".", "/");
        this.resultClass = this.createResultClass();
        Class<BiFunction<Object, Object[], Object>> factoryClass = (Class)this.createFactoryClass();
        try {
            this.factory = factoryClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        }
    }
    
    private List<Class<?>> getMinInterfaces(Class<?>[] ... arrs) {
        Set<Class<?>> set = new LinkedHashSet<>();
        if (arrs != null) {
            for (Class<?>[] arr : arrs) {
                if (arr != null) {
                    for (Class<?> itf : arr) {
                        if (itf != null) {
                            if (!itf.isInterface()) {
                                return null;
                            }
                            set.add(itf);
                        }
                    }
                }
            }
        }
        Class<?>[] minItfs = new Class[set.size()];
        int size = 0;
        for (Class<?> itf : set) {
            boolean ignore = false;
            for (int i = 0; i < size; i++) {
                Class<?> existingItf = minItfs[i];
                if (existingItf != null) {
                    if (itf.isAssignableFrom(existingItf)) {
                        ignore = true;
                        break;
                    } else if (existingItf.isAssignableFrom(itf)) {
                        minItfs[i] = itf;
                        ignore = true;
                        break;
                    }
                }
            }
            if (!ignore) {
                minItfs[size++] = itf;
            }
        }
        return MACollections.wrap(minItfs).subList(0, size);
    }
    
    @SuppressWarnings("unchecked")
    protected static <T extends ClassWrapper> T getInstance(Class<T> wrapperClass) {
        
        Lock lock;
        ClassWrapper classWrapper;
        
        (lock = CACHE_LOCK.readLock()).lock(); //1st locking
        try {
            classWrapper = CACHE.get(wrapperClass); //1st reading
        } finally {
            lock.unlock();
        }
        
        if (classWrapper == null) {
            (lock = CACHE_LOCK.writeLock()).lock(); //2nd locking
            try {
                classWrapper = CACHE.get(wrapperClass); //2nd reading
                if (classWrapper == null) {
                    ClassWrapper instance = getInstance0(wrapperClass);
                    classWrapper = instance;
                    CACHE.put(wrapperClass, classWrapper);
                }
            } finally {
                lock.unlock();
            }
        }
        return (T)classWrapper;
    }
    
    private static ClassWrapper getInstance0(Class<? extends ClassWrapper> wrapperClass) {
        Arguments.mustNotBeNull("enhancerClass", wrapperClass);
        Arguments.mustBeCompatibleWithValue("wrapperClass", wrapperClass, ClassWrapper.class);
        Arguments.mustNotBeAbstract("wrapperClass", wrapperClass);
        Constructor<? extends ClassWrapper> constructor;
        try {
            constructor = wrapperClass.getDeclaredConstructor();
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(
                    mustSupportDefaultConstructor(wrapperClass),
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
    
    protected final String getRawInternalName() {
        return this.rawInternalName;
    }
    
    protected final Class<?> getRawType() {
        return this.rawType;
    }
    
    protected final Collection<Class<?>> getRawInterfaces() {
        return this.rawInterfaceTypes;
    }
    
    protected final Class<?> getArgumentType(int index) {
        return this.argumentTypes.get(index);
    }

    protected final Collection<Class<?>> getInterfaceTypes() {
        return this.interfaceTypes;
    }
    
    protected final String getResultClassName() {
        return this.resultClassName;
    }
    
    protected final String getResultInternalName() {
        return this.resultInternalName;
    }
    
    protected final Package getResultPackage() {
        return this.resultPackage;
    }
    
    protected final Class<?> getResultClass() {
        return this.resultClass;
    }
    
    protected final void generateGetRaw(MethodVisitor mv) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(
                Opcodes.GETFIELD, 
                this.resultInternalName, 
                RAW, 
                ASM.getDescriptor(this.rawType));
    }
    
    protected final void generateGetArgument(MethodVisitor mv, int index) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(
                Opcodes.GETFIELD, 
                this.resultInternalName, 
                String.format(ARGUMENT_FORMAT, index), 
                ASM.getDescriptor(this.argumentTypes.get(index)));
    }
    
    protected final Method generateInvokeRawMethodCode(XMethodVisitor mv, MethodImplementation methodImplementation) {
        this.generateGetRaw(mv);
        Method rawMethod = null;
        for (MethodDescriptor descriptor : methodImplementation.getSelfAndBridgeDescriptors()) {
            try {
                rawMethod = this.rawType.getMethod(
                        descriptor.getName(), 
                        descriptor.getParameterTypes().toArray(new Class[descriptor.getParameterTypes().size()])
                );
                break;
            } catch (NoSuchMethodException ex) {
                //Ignore exception
            }
        }
        if (rawMethod == null) {
            for (Class<?> rawInterfaceType : this.rawInterfaceTypes) {
                for (MethodDescriptor descriptor : methodImplementation.getSelfAndBridgeDescriptors()) {
                    try {
                        rawMethod = rawInterfaceType.getMethod(
                                descriptor.getName(), 
                                descriptor.getParameterTypes().toArray(new Class[descriptor.getParameterTypes().size()])
                        );
                        break;
                    } catch (NoSuchMethodException ex) {
                        //Ingore exception
                    }
                }
                if (rawMethod != null) {
                    break;
                }
            }
        }
        if (rawMethod == null) {
            throw new IllegalProgramException(
                    canNotInvokeRawMethod(
                            this.getClass(),
                            methodImplementation.getSelfAndBridgeDescriptors(),
                            this.rawInterfaceTypes
                    )
            );
        }
        Class<?> invokedType = rawMethod.getDeclaringClass();
        if (invokedType.isAssignableFrom(this.rawType)) {
            mv.visitTypeInsn(
                    Opcodes.CHECKCAST, 
                    ASM.getInternalName(invokedType)
            );
        }
        MethodDescriptor methodDescriptor = methodImplementation.getDescriptor();
        int slot = 1;
        for (Class<?> parameterType : methodDescriptor.getParameterTypes()) {
            mv.visitVarInsn(ASM.getLoadCode(parameterType), slot);
            slot += ASM.getSlotCount(parameterType);
        }
        mv.visitMethodInsn(
                invokedType.isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, 
                ASM.getInternalName(invokedType), 
                rawMethod.getName(), 
                ASM.getDescriptor(rawMethod),
                invokedType.isInterface());
        return rawMethod;
    }

    @SuppressWarnings("unchecked")
    protected final <T> T createProxy(T raw, T ... args) {
        return (T)this.factory.apply(raw, args);
    }

    protected Class<?>[] onGetRawInterfaceTypes() {
        return null;
    }
    
    protected Class<?>[] onGetInterfaceTypes() {
        return null;
    }

    protected boolean useWrapperLoader() {
        return false;
    }
    
    private Class<?> createResultClass() {
        return ASM.loadDynamicClass(
                this.targetClassLoader, 
                this.resultClassName, 
                this.targetProctionDomain, 
                (ClassVisitor cv) -> {
                    ClassWrapper.this.genrateResultClass(cv);
                });
    }
    
    private void genrateResultClass(ClassVisitor cv) {
        Collection<String> interfaceNames = new LinkedHashSet<>(((this.interfaceTypes.size() + 1)* 4 + 2) / 3);
        for (Class<?> interfaceType : this.interfaceTypes) {
            interfaceNames.add(ASM.getInternalName(interfaceType));
        }
        ClassInfo<?>[] otherInferfaceInfos = new ClassInfo[this.interfaceTypes.size() - 1];
        for (int i = otherInferfaceInfos.length - 1; i >= 0; i--) {
            otherInferfaceInfos[i] = ClassInfo.of(this.interfaceTypes.get(i + 1));
        }
        Map<MethodDescriptor, MethodImplementation> methodImplementationMap = 
                MethodImplementation.getMethodImplementationMap(
                        ClassInfo.of(this.interfaceTypes.get(0)), 
                        otherInferfaceInfos
                );
        cv.visit(
                Opcodes.V1_7, 
                Opcodes.ACC_PUBLIC, 
                this.getResultInternalName(), 
                null, 
                "java/lang/Object", 
                interfaceNames.toArray(new String[interfaceNames.size()]));
        cv
        .visitField(
                Opcodes.ACC_PROTECTED, 
                RAW, 
                ASM.getDescriptor(this.rawType), 
                null,
                null
        )
        .visitEnd();
        int index = 0;
        for (Class<?> argumentType : this.argumentTypes) {
            cv
            .visitField(
                    Opcodes.ACC_PROTECTED, 
                    String.format(ARGUMENT_FORMAT, index++), 
                    ASM.getDescriptor(argumentType), 
                    null,
                    null
            )
            .visitEnd();
        }
        this.generateConstructor(cv);
        this.generateMoreMemebers(cv);
        for (MethodImplementation methodImplementation : methodImplementationMap.values()) {
            this.generateMethod(cv, methodImplementation);
        }
        cv.visitEnd();
        //new org.babyfish.org.objectweb.asm.ClassReader(((ClassWriter)cv).toByteArray()).accept(new org.babyfish.org.objectweb.asm.util.TraceClassVisitor(new java.io.PrintWriter(System.out)),0);
    }
    
    private void generateConstructor(ClassVisitor cv) {
        StringBuilder builder = new StringBuilder('(' + ASM.getDescriptor(this.rawType));
        for (Class<?> argumentType : this.argumentTypes) {
            builder.append(ASM.getDescriptor(argumentType));
        }
        builder.append(")V");
        XMethodVisitor mv = ASM.visitMethod(
                cv,
                Opcodes.ACC_PUBLIC, 
                "<init>", 
                builder.toString(), 
                null, 
                null);
        mv.visitCode();
        this.generateInitCodeWithoutReturning(mv);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    protected void generateMoreMemebers(ClassVisitor cv) {
        
    }
    
    protected void generateInitCodeWithoutReturning(XMethodVisitor mv) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                "java/lang/Object",
                "<init>", 
                "()V",
                false);
        for (Class<?> rawInterfaceType : this.rawInterfaceTypes) {
            mv.visitLdcInsn("raw");
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitLdcInsn(org.babyfish.org.objectweb.asm.Type.getType(rawInterfaceType));
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(Arguments.class), 
                    "mustBeInstanceOfValue", 
                    "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;",
                    false
            );
        }
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitFieldInsn(
                Opcodes.PUTFIELD, 
                this.resultInternalName, 
                RAW, 
                ASM.getDescriptor(this.rawType));
        int index = 0;
        int slotIndex = 2;
        for (Class<?> argumentType : this.argumentTypes) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, slotIndex);
            slotIndex += ASM.getSlotCount(argumentType);
            mv.visitFieldInsn(
                    Opcodes.PUTFIELD, 
                    this.resultInternalName, 
                    String.format(ARGUMENT_FORMAT, index++), 
                    ASM.getDescriptor(argumentType));
        }
    }
    
    private void generateMethod(ClassVisitor cv, MethodImplementation methodImplementation) {
        XMethodVisitor mv;
        MethodDescriptor descriptor = methodImplementation.getDescriptor();
        if (methodImplementation.isBridge()) {
            int synthetic = methodImplementation.isSynthetic() ? Opcodes.ACC_SYNTHETIC : 0;
            mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE | synthetic, 
                    descriptor.getName(), 
                    descriptor.toByteCodeDescriptor(), 
                    null, 
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            MethodImplementation target = methodImplementation.getBridgeTargetImplementation();
            int slot = 1;
            List<Class<?>> parameterTypes = descriptor.getParameterTypes();
            List<Class<?>> targetParameterTypes = descriptor.getParameterTypes();
            int size = parameterTypes.size();
            for (int i = 0; i < size; i++) {
                Class<?> parameterType = parameterTypes.get(i);
                Class<?> targetParameterType = targetParameterTypes.get(i);
                mv.visitVarInsn(ASM.getLoadCode(parameterType), slot);
                if (parameterType != targetParameterType) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(targetParameterType));
                }
                slot += ASM.getSlotCount(parameterType);
            }
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.getResultInternalName(), 
                    target.getDescriptor().getName(), 
                    target.getDescriptor().toByteCodeDescriptor(),
                    false);
            mv.visitInsn(ASM.getReturnCode(descriptor.getReturnType()));
        } else {
            mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    descriptor.getName(), 
                    descriptor.toByteCodeDescriptor(), 
                    null, 
                    null);
            mv.visitCode();
            this.generateMethodCode(mv, methodImplementation);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    protected void generateMethodCode(XMethodVisitor mv, MethodImplementation methodImplementation) {
        MethodDescriptor methodDescriptor = methodImplementation.getDescriptor();
        Class<?> rawReturnType = this.generateInvokeRawMethodCode(mv, methodImplementation).getReturnType();
        Class<?> returnType = methodDescriptor.getReturnType();
        if (rawReturnType != returnType && !returnType.isAssignableFrom(rawReturnType)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(returnType));
        }
        mv.visitInsn(ASM.getReturnCode(returnType));
    }
    
    private Class<?> createFactoryClass() {
        Constructor<?> constructor;
        Class<?>[] parameterTypes = new Class[this.argumentTypes.size() + 1];
        parameterTypes[0] = this.rawType;
        for (int i = this.argumentTypes.size() - 1; i >= 0; i--) {
            parameterTypes[i + 1] = argumentTypes.get(i);
        }
        try {
            constructor = resultClass.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new IllegalProgramException(
                    resultClassMustSupportDefaultConstructor(
                            this.getClass(),
                            this.resultClass
                    ),
                    ex
            );
        }
        if (!Modifier.isPublic(constructor.getModifiers())) {
            throw new IllegalProgramException(
                    defaultConstructorOfResultClassMustBePublic(
                            this.getClass(),
                            this.resultClass
                    )
            );
        }
        final String resultFactoryClassName =
            this.rawType.getName() +
            "_Wrpper_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E:Factory{wrapperClass=" +
            this.getClass().getName().replace('.', '\\') +
            "}";
        return ASM.loadDynamicClass(
                this.targetClassLoader, 
                resultFactoryClassName, 
                BiFunction.class.getProtectionDomain(), 
                (ClassVisitor cv) -> {
                    ClassWrapper.this.generateFactoryClass(cv, resultFactoryClassName);
                });
    }
    
    private void generateFactoryClass(ClassVisitor cv, String factoryClassname) {
        cv.visit(
                Opcodes.V1_7, 
                Opcodes.ACC_PUBLIC, 
                factoryClassname.replace('.', '/'), 
                null, 
                "java/lang/Object", 
                new String[] { ASM.getInternalName(BiFunction.class) });
        
        XMethodVisitor mv = ASM.visitMethod(
                cv, 
                Opcodes.ACC_PUBLIC, 
                "<init>", 
                "()V", 
                null, 
                null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                "java/lang/Object", 
                "<init>", 
                "()V",
                false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        
        mv = ASM.visitMethod(
                cv, 
                Opcodes.ACC_PUBLIC, 
                "apply", 
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", 
                null, 
                null);
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, this.resultInternalName);
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(this.rawType));
        int index = 0;
        for (Class<?> argumentType : this.argumentTypes) {
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(Object[].class));
            mv.visitLdcInsn(index++);
            mv.visitInsn(Opcodes.AALOAD);
            if (argumentType.isPrimitive()) {
                mv.visitUnbox(argumentType, null);
            } else if (argumentType != Object.class) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(argumentType));
            }
        }
        StringBuilder builder = new StringBuilder('(' + ASM.getDescriptor(this.rawType));
        for (Class<?> argumentType : this.argumentTypes) {
            builder.append(ASM.getDescriptor(argumentType));
        }
        builder.append(")V");
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                this.resultInternalName, 
                "<init>", 
                builder.toString(),
                false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        
        cv.visitEnd();
    }
    
    @I18N
    private static native String mustSupportDefaultConstructor(Class<? extends ClassWrapper> runtimeType);

    @I18N
    private static native String eachElementOfReturnValueCanOnlyBeInterface(
            Class<? extends ClassWrapper> runtimeType, String methodName);

    @I18N
    private static native String noInterfaces(Class<? extends ClassWrapper> runtimeType);

    @I18N
    private static native String canNotInvokeRawMethod(
            Class<? extends ClassWrapper> runtimeType,
            Set<MethodDescriptor> selfAndBridgeDescriptors,
            List<Class<?>> rawInterfaceTypes);

    @I18N
    private static native String resultClassMustSupportDefaultConstructor(
            Class<? extends ClassWrapper> runtimeType, Class<?> resultClass);

    @I18N
    private static native String defaultConstructorOfResultClassMustBePublic(
            Class<? extends ClassWrapper> runtimeType, 
            Class<?> resultClass);
}
