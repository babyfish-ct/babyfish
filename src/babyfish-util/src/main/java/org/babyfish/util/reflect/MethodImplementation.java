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
package org.babyfish.util.reflect;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Equality;
import org.babyfish.lang.I18N;
import org.babyfish.util.GraphTravelActionAdapter;
import org.babyfish.util.GraphTravelContext;

/**
 * @author Tao Chen
 */
public class MethodImplementation {

    private ClassInfo<?> declaringClass;
    
    private MethodDescriptor descriptor;
    
    private Set<MethodDescriptor> descriptors;
    
    private MethodInfo implementedMethod;
    
    private Collection<MethodInfo> methods;
    
    private MethodImplementation target;
    
    private Collection<MethodImplementation> bridges;
    
    private transient int public_;
    
    private transient int protected_;
    
    private transient int synthetic;
    
    private transient int hash;
    
    private MethodImplementation(
            ClassInfo<?> declaringClass, 
            MethodDescriptor descriptor,
            MethodInfo implementedMethod,
            MethodInfo[] methods) {
        this.declaringClass = declaringClass;
        this.descriptor = descriptor;
        this.implementedMethod = implementedMethod;
        this.methods = MACollections.wrap(methods);
    }

    public static Map<MethodDescriptor, MethodImplementation> getMethodImplementationMap(
            Class<?> clazz) {
        ClassInfo<?> classInfo = ClassInfo.of(clazz);
        return getMethodImplementationMap(classInfo);
    }

    public static Map<MethodDescriptor, MethodImplementation> getMethodImplementationMap( 
            ClassInfo<?> firstClassInfo, 
            ClassInfo<?> ... otherClassInfos) {
        Package pkg = firstClassInfo.getRawClass().getPackage();
        return getMethodImplementationMap(
                pkg != null ? pkg.getName() : null, 
                firstClassInfo, 
                otherClassInfos);
    }

    public static Map<MethodDescriptor, MethodImplementation> getMethodImplementationMap(
            String declaringPackage, 
            ClassInfo<?> firstClassInfo, 
            ClassInfo<?> ... otherClassInfos) {
        //TODO: Create the My WeakHashMap
        //Support Weak key, Weak value, Weak all, WeakReference/SoftReference.
        return createMethodImplementations(
                declaringPackage, 
                firstClassInfo, 
                otherClassInfos);
    }

    private static Map<MethodDescriptor, MethodImplementation> createMethodImplementations(
            String declaringPackage, 
            ClassInfo<?> firstClassInfo, 
            ClassInfo<?> ... otherClassInfos) {
        Context context = new Context(declaringPackage, firstClassInfo, otherClassInfos);
        context.initBuilderMap();
        
        Map<MethodDescriptor, ImplementationBuilder> map = context.builderMap;
        if (context.clazz != null) {
            fillBridges(context.clazz, declaringPackage, map);
        }
        for (ClassInfo<?> interfaze : context.minimumInterfaces) {
            fillBridges(interfaze, declaringPackage, map);
        }
        
        Map<MethodDescriptor, MethodImplementation> implementationMap =
            new HashMap<MethodDescriptor, MethodImplementation>();
        for (ImplementationBuilder builder : map.values()) {
            builder.impl = new MethodImplementation(
                    context.clazz != null ? context.clazz : context.minimumInterfaces[0], 
                    builder.descriptor,
                    builder.implementedMethod, 
                    builder.methods.toArray(new MethodInfo[builder.methods.size()]));
            implementationMap.put(builder.descriptor, builder.impl);
        }
        
        for (ImplementationBuilder builder : map.values()) {
            if (builder.secondParseTargetBuilder != null) {
                builder.impl.target = builder.secondParseTargetBuilder.impl;
            }
            MethodImplementation[] bridgeArr = new MethodImplementation[builder.secondParseBridgeBuildes.size()];
            int bridgeIndex = 0;
            for (ImplementationBuilder implementationBuilder : builder.secondParseBridgeBuildes) {
                bridgeArr[bridgeIndex++] = implementationBuilder.impl;
            }
            builder.impl.bridges = MACollections.wrap(bridgeArr);
        }
        
        return MACollections.unmodifiable(implementationMap);
    }

    private static void fillBridges(
            ClassInfo<?> classInfo, 
            final String declaringPackage,
            final Map<MethodDescriptor, ImplementationBuilder> map) {
        for (final MethodInfo method : classInfo.getDeclaredMethods()) {
            if (isVirtualMethod(method, declaringPackage)) {
                ClassInfo.MINIMUM_TRAVELER.depthFirstTravel(
                        classInfo, 
                        new GraphTravelActionAdapter<ClassInfo<?>>() {
                            @Override
                            public void preTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                                if (ctx.getDepth() != 0) {
                                    ClassInfo<?> shadowClassInfo = ctx.getNode();
                                    for (MethodInfo shadowMethod : shadowClassInfo.getDeclaredMethods()) {
                                        if (isVirtualMethod(shadowMethod, declaringPackage)) {
                                            processMethods(method, shadowMethod, map);
                                        }
                                    }
                                }
                            }
                        });
            }
        }
        if (!classInfo.isInterface()) {
            ClassInfo<?> superClassInfo = classInfo.getSuperClass();
            if (superClassInfo != null) {
                fillBridges(superClassInfo, declaringPackage, map);
            }
        }
        for (ClassInfo<?> interfaceInfo : classInfo.getMinimumInterfaces()) {
            fillBridges(interfaceInfo, declaringPackage, map);
        }
    }

    private static void processMethods(
            MethodInfo method, 
            MethodInfo shadowMethod,
            final Map<MethodDescriptor, ImplementationBuilder> map) {
        if (shadowMethod.getName().equals(method.getName())) {
            if (shadowMethod.getReturnType() != method.getReturnType() ||
                !shadowMethod.getParameterTypes().equals(method.getParameterTypes())) {
                if (shadowMethod.getResolvedReturnType().isAssignableFrom(method.getReturnType()) &&
                        shadowMethod.getResolvedParameterTypes().equals(method.getParameterTypes())) {
                    ImplementationBuilder builder = map.get(method.getDescriptor());
                    ImplementationBuilder shadowBuilder = map.get(shadowMethod.getDescriptor());
                    ImplementationBuilder oldTarget = shadowBuilder.secondParseTargetBuilder;
                    if (oldTarget == null || oldTarget.descriptor.isAssignableFrom(method.getDescriptor())) {
                        if (oldTarget != null) {
                            oldTarget.secondParseBridgeBuildes.remove(oldTarget);
                        }
                        shadowBuilder.secondParseTargetBuilder = builder;
                        builder.secondParseBridgeBuildes.add(shadowBuilder);
                    }
                }
            }
        }
    }

    private static boolean isVirtualMethod(MethodInfo method, String declaringPackage) {
        ModifierSet modifiers = method.getModifiers();
        if (modifiers.isPrivate() || modifiers.isStatic()) {
            return false;
        }
        if (!modifiers.isProtected() && !modifiers.isPublic()) {
            Package pkg = method.getDeclaringClass().getRawClass().getPackage();
            if (pkg == null) {
                if (declaringPackage != null && !declaringPackage.isEmpty()) {
                    return false;
                }
            } else {
                if (!pkg.getName().equals(declaringPackage)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isPublic() {
        int public_ = this.public_;
        if (public_ == 0) {
            public_ = -1;
            for (MethodInfo method : methods) {
                if (method.getModifiers().isPublic()) {
                    public_ = 1;
                    break;
                }
            }
            this.public_ = public_;
        }
        return public_ == 1;
    }

    public boolean isProtected() {
        int protected_ = this.protected_;
        if (protected_ == 0) {
            protected_ = -1;
            if (!this.isPublic()) {
                for (MethodInfo method : methods) {
                    if (method.getModifiers().isProtected()) {
                        protected_ = 1;
                        break;
                    }
                }
            }
            this.protected_ = protected_;
        }
        return protected_ == 1;
    }

    public boolean isOverridable() {
        MethodInfo implementedMethod = this.implementedMethod;
        return implementedMethod == null || !implementedMethod.getModifiers().isFinal();
    }

    public boolean isMustBeOverridden() {
        MethodInfo implementedMethod = this.implementedMethod;
        if (implementedMethod == null) {
            return true;
        }
        ModifierSet implementedMethodModifiers = implementedMethod.getModifiers();
        if (implementedMethodModifiers.isFinal()) {
            return false;
        }
        if (implementedMethodModifiers.isAbstract()) {
            return true;
        }
        MethodImplementation target = this.target;
        if (target != null && target.implementedMethod == null) {
            return true;
        }
        return false;
    }

    public boolean isBridge() {
        return this.target != null;
    }

    public boolean isSynthetic() {
        int synthetic = this.synthetic;
        if (synthetic == 0) {
            synthetic = -1;
            MethodImplementation target = this.target;
            if (target != null) {
                if (!this.descriptor.getParameterTypes().equals(
                        target.descriptor.getParameterTypes())) {
                    synthetic = 1;
                }
            }
            this.synthetic = synthetic;
        }
        return synthetic == 1;
    }
    
    public ClassInfo<?> getDeclaringClass() {
        return this.declaringClass;
    }

    public MethodDescriptor getDescriptor() {
        return this.descriptor;
    }

    public Set<MethodDescriptor> getSelfAndBridgeDescriptors() {
        Set<MethodDescriptor> descriptors = this.descriptors;
        if (descriptors == null) {
            descriptors = new HashSet<MethodDescriptor>();
            descriptors.add(this.descriptor);
            for (MethodImplementation bridgeImplementation : this.bridges) {
                descriptors.add(bridgeImplementation.getDescriptor());
            }
            this.descriptors = descriptors = MACollections.unmodifiable(descriptors);
        }
        return descriptors;
    }

    public MethodInfo getImplementedMethod() {
        return this.implementedMethod;
    }

    public Collection<MethodInfo> getMethods() {
        return this.methods;
    }

    public MethodImplementation getBridgeTargetImplementation() {
        return this.target;
    }

    public Collection<MethodImplementation> getBridgeImplementations() {
        return this.bridges;
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            hash = this.declaringClass.hashCode() ^ this.descriptor.hashCode();
            if (hash == 0) {
                hash = 34572834;
            }
            this.hash = hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        Equality<MethodImplementation> equality = Equality.of(MethodImplementation.class, this, obj);
        MethodImplementation other = equality.other();
        if (other == null) {
            return equality.returnValue();
        }
        return 
            this.declaringClass.equals(other.getDeclaringClass()) &&
            this.descriptor.equals(other.getDescriptor());
    }

    @Override
    public String toString() {
        return this.descriptor.toString();
    }
    
    private static class Context {
        
        String declaringPackage;
        
        ClassInfo<?> clazz;
        
        ClassInfo<?>[] minimumInterfaces;
        
        ClassInfo<?>[] allClassInfos;
        
        Map<MethodDescriptor, ImplementationBuilder> builderMap;
        
        Context(String defaultPackage, ClassInfo<?> firstClassInfo, ClassInfo<?> ... otherClassInfos) {
            Set<ClassInfo<?>> interfaces = new HashSet<ClassInfo<?>>();
            Map<Class<?>, ClassInfo<?>> map = new HashMap<Class<?>, ClassInfo<?>>();
            this.add0(firstClassInfo, interfaces, map);
            for (ClassInfo<?> otherClassInfo : otherClassInfos) {
                this.add0(otherClassInfo, interfaces, map);
            }
            ClassInfo<?> clazz = this.clazz;
            ClassInfo<?>[] arr = interfaces.toArray(new ClassInfo[interfaces.size()]);
            for (ClassInfo<?> interfaze : arr) {
                Iterator<ClassInfo<?>> itr = interfaces.iterator();
                while (itr.hasNext()) {
                    ClassInfo<?> otherInterface = itr.next();
                    if (clazz != null && 
                            otherInterface.getRawClass().isAssignableFrom(clazz.getRawClass())) {
                        itr.remove();
                        continue;
                    }
                    if (interfaze != otherInterface && 
                            otherInterface.getRawClass().isAssignableFrom(interfaze.getRawClass())) {
                        itr.remove();
                        continue;
                    }
                }
            }
            this.declaringPackage = defaultPackage;
            this.minimumInterfaces = interfaces.toArray(new ClassInfo[interfaces.size()]);
            this.allClassInfos = map.values().toArray(new ClassInfo[map.size()]);
        }
        
        void add0(ClassInfo<?> classInfo, Set<ClassInfo<?>> interfaces, Map<Class<?>, ClassInfo<?>> map) {
            if (classInfo == null) {
                return;
            }
            if (classInfo.isInterface()) {
                interfaces.add(classInfo);
            } else {
                if (this.clazz != null) {
                    throw new IllegalArgumentException(twoManyClasses(this.clazz, classInfo));
                }
                this.clazz = classInfo;
            }
            add0(classInfo, map);
        }
        
        static void add0(ClassInfo<?> classInfo, Map<Class<?>, ClassInfo<?>> map) {
            if (classInfo == null) {
                return;
            }
            ClassInfo<?> ci = map.put(classInfo.getRawClass(), classInfo);
            if (ci != null && !ci.equals(classInfo)) {
                throw new IllegalArgumentException(
                        duplicatedTypesWithDifferentTypeArguments(ci, classInfo)
                );
            }
            for (ClassInfo<?> interfaze : classInfo.getMinimumInterfaces()) {
                add0(interfaze, map);
            }
            if (!classInfo.isInterface()) {
                add0(classInfo.getSuperClass(), map);
            }
        }
        
        void initBuilderMap() {
            this.builderMap = new HashMap<MethodDescriptor, ImplementationBuilder>();
            for (ClassInfo<?> classInfo : this.allClassInfos) {
                for (MethodInfo method : classInfo.getDeclaredMethods()) {
                    this.addMethod0(method);
                }
            }
        }
        
        void addMethod0(MethodInfo method) {
            if (isVirtualMethod(method, this.declaringPackage)) {
                ImplementationBuilder builder = getBuilder(method.getDescriptor());
                builder.add(method);
            }
        }
        
        ImplementationBuilder getBuilder(MethodDescriptor descriptor) {
            Map<MethodDescriptor, ImplementationBuilder> map = this.builderMap;
            ImplementationBuilder builder = map.get(descriptor);
            if (builder == null) {
                builder = new ImplementationBuilder(descriptor);
                map.put(descriptor, builder);
            }
            return builder;
        }
        
    }
    
    private static class ImplementationBuilder {
        
        MethodDescriptor descriptor;
        
        MethodInfo implementedMethod;
        
        Set<MethodInfo> methods;
        
        MethodImplementation impl;
        
        ImplementationBuilder secondParseTargetBuilder;
        
        Set<ImplementationBuilder> secondParseBridgeBuildes;
        
        ImplementationBuilder(MethodDescriptor descriptor) {
            this.descriptor = descriptor;
            this.methods = new HashSet<MethodInfo>();
            this.secondParseBridgeBuildes = new HashSet<ImplementationBuilder>();
        }
        
        void add(MethodInfo method) {
            Class<?> declaringRawClass = method.getDeclaringClass().getRawClass();
            if (!declaringRawClass.isInterface()) {
                MethodInfo implementedMethod = this.implementedMethod;
                if (implementedMethod == null || 
                        implementedMethod
                        .getDeclaringClass()
                        .getRawClass()
                        .isAssignableFrom(declaringRawClass)) {
                    this.implementedMethod = method;
                }
            }
            this.methods.add(method);
        }
    }
    
    @I18N
    private static native String twoManyClasses(ClassInfo<?> classInfo1, ClassInfo<?> classInfo2);
        
    @I18N
    private static native String duplicatedTypesWithDifferentTypeArguments(ClassInfo<?> classInfo1, ClassInfo<?> classInfo2);
}
