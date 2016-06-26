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

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.Ref;
import org.babyfish.util.GraphTravelAction;
import org.babyfish.util.GraphTravelContext;
import org.babyfish.util.GraphTraveler;
import org.babyfish.util.JoinedIterator;
import org.babyfish.util.reflect.PropertyInfo.InvalidPropertyException;

/**
 * @author Tao Chen
 */
public final class ClassInfo<T> implements AnnotatedElement {
    
    private static final Type[] EMPTY_TYPES = new Type[0];
    
    private static final Map<Class<?>, Class<?>> PRIMITIVE_BOX_MAP;
    
    private static final Map<Class<?>, Class<?>> BOX_PRIMITIVE_MAP;
    
    private static final Map<Class<?>, WeakHashMap<Type, SoftReference<ClassInfo<?>>>> CACHE = new WeakHashMap<>();
    
    private static final ReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();
    
    public static final GraphTraveler<ClassInfo<?>> MINIMUM_TRAVELER =
        new GraphTraveler<ClassInfo<?>>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            protected Iterator<ClassInfo<?>> getNeighborNodeIterator(ClassInfo<?> classInfo) {
                return 
                classInfo.isInterface() ?
                        (Iterator)classInfo.getMinimumInterfaces().iterator()
                        :
                        JoinedIterator
                        .<ClassInfo<?>>empty()
                        .join(MACollections.<ClassInfo<?>>wrap(classInfo.getSuperClass()).iterator())
                        .join(classInfo.getMinimumInterfaces().iterator());
            }
        };
        
    private static final GraphTraveler<ClassInfo<?>> CLASS_TRAVELER =
        new GraphTraveler<ClassInfo<?>>() {
            @Override
            protected Iterator<ClassInfo<?>> getNeighborNodeIterator(ClassInfo<?> node) {
                ClassInfo<?> superClass = node.getSuperClass();
                if (superClass != null) {
                    return MACollections.<ClassInfo<?>>wrap(superClass).iterator();
                }
                return null;
            }
        };
        
    private static final GraphTraveler<ClassInfo<?>> INTERFACE_TRAVELER =
        new GraphTraveler<ClassInfo<?>>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            protected Iterator<ClassInfo<?>> getNeighborNodeIterator(ClassInfo<?> node) {
                return (Iterator)node.getMinimumInterfaces().iterator();
            }
        };

    final Type rawType;
    
    private ClassInfo<?> declaringClass;
    
    private List<TypeVariable<Class<T>>> typeParameters;
    
    private ClassInfo<?> superClass;
    
    private List<ClassInfo<?>> interfaces;
    
    private List<ClassInfo<? super T>> minimumInterfaces;
    
    private SoftReference<Collection<FieldInfo>> declaredFieldsRef;
    
    private SoftReference<Collection<MethodInfo>> declaredMethodsRef;
    
    private SoftReference<Collection<ConstructorInfo<T>>> declaredConstructorsRef;
    
    private SoftReference<Collection<PropertyInfo>> declaredPropertiesRef;
    
    private SoftReference<Collection<FieldInfo>> fieldsRef;
    
    private SoftReference<Collection<MethodInfo>> methodsRef;
    
    private SoftReference<Collection<PropertyInfo>> propertiesRef;
    
    private SoftReference<Collection<ClassInfo<?>>> declaredNestedClassesRef;
    
    private SoftReference<Collection<ClassInfo<?>>> nestedClassesRef;
    
    private SoftReference<Map<MethodDescriptor, MethodImplementation>> methodImplementationMapRef;
    
    private transient int hash;

    @SuppressWarnings("unchecked")
    private ClassInfo(
            Map<Type, ClassInfo<?>> contextMap,
            ClassInfo<?> declaringClass,
            GenericResolver genericResolver) {
        this.rawType = genericResolver.rawTypeForClassInfo;
        this.declaringClass = declaringClass;
        this.validate();
        Class<T> rawClass = this.getRawClass();
        this.typeParameters = MACollections.wrap(rawClass.getTypeParameters());
        Type superType = rawClass.getGenericSuperclass();
        if (superType instanceof Class<?>) {
            this.superClass = of(
                    contextMap,
                    (Class<?>)superType,
                    genericResolver);
        } else if (superType instanceof ParameterizedType) {
            ParameterizedType parameterizedSuperType = (ParameterizedType)superType;
            this.superClass = of(
                    contextMap,
                    parameterizedSuperType,
                    genericResolver);
        } else {
            this.superClass = null;
        }
        
        Type[] interfaceRawTypes = rawClass.getGenericInterfaces();
        ClassInfo<?>[] interfaces = new ClassInfo[interfaceRawTypes.length];
        if (interfaceRawTypes.length != 0) {
            for (int i = interfaces.length - 1; i >= 0; i--) {
                Type interface_ = interfaceRawTypes[i];
                if (interface_ instanceof Class<?>) {
                    interfaces[i] = of(
                            contextMap,
                            (Class<? super T>)interface_, 
                            genericResolver);
                } else {
                    ParameterizedType parameterizedInterface = (ParameterizedType)interface_;
                    interfaces[i] = of(
                            contextMap,
                            parameterizedInterface
                            , genericResolver);
                }
            }
        }
        this.interfaces = MACollections.wrap(interfaces);
        
        Map<Class<?>, ClassInfo<?>> minimumInterfaceMap = 
            new LinkedHashMap<Class<?>, ClassInfo<?>>(interfaces.length);
        Class<?> superRawClass = this.getSuperClass() == null ? null : this.getSuperClass().getRawClass();
        for (ClassInfo<?> interfaceClassInfo : interfaces) {
            Class<?> interfaceRawClass = interfaceClassInfo.getRawClass();
            if (superRawClass != null) {
                if (interfaceRawClass.isAssignableFrom(superRawClass)) {
                    break;
                }
            }
            Iterator<Class<?>> keyIterator = minimumInterfaceMap.keySet().iterator();
            while (keyIterator.hasNext()) {
                Class<?> existingInterfaceRawClass = keyIterator.next();
                if (interfaceRawClass.isAssignableFrom(existingInterfaceRawClass)) {
                    interfaceRawClass = null;
                    break;
                } else if (existingInterfaceRawClass.isAssignableFrom(interfaceRawClass)) {
                    keyIterator.remove();
                    continue;
                }
            }
            if (interfaceRawClass != null) {
                minimumInterfaceMap.put(interfaceRawClass, interfaceClassInfo);
            }
        }
        this.minimumInterfaces = MACollections.wrap(
                minimumInterfaceMap.values().toArray(
                        (ClassInfo<? super T>[])new ClassInfo[minimumInterfaceMap.size()]));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> ClassInfo<T> of(Class<T> clazz, Type ... actualTypeArguments) {
        Type cascadeMakedType = GenericTypes.cascadeMakeTypeOrParameterizedType(clazz, actualTypeArguments);
        if (actualTypeArguments == null || actualTypeArguments.length == 0 || cascadeMakedType instanceof Class<?>) {
            return (ClassInfo<T>)of(
                    new HashMap<Type, ClassInfo<?>>(), 
                    clazz,
                    null);
        }
        return (ClassInfo<T>)of(
                new HashMap<Type, ClassInfo<?>>(),
                (ParameterizedType)cascadeMakedType,
                null);
    }

    @SuppressWarnings("unchecked")
    public static <T> ClassInfo<T> of(
            ClassInfo<?> declaringClass, 
            Class<T> clazz, 
            Type ... actualTypeArguments) {
        Arguments.mustNotBeNull("declaringClass", declaringClass);
        return (ClassInfo<T>)of(
                new HashMap<Type, ClassInfo<?>>(),
                declaringClass,
                new GenericResolver(
                        GenericTypes.makeParameterizedType(
                                declaringClass.getRawType(), 
                                clazz, 
                                actualTypeArguments), 
                        null));
    }
    
    public static ClassInfo<?> of(ParameterizedType parameterizedType) {
        return of(
                new HashMap<Type, ClassInfo<?>>(), 
                parameterizedType,
                null);
    }
    
    public static ClassInfo<?> of(Type type) {
        if (type instanceof Class<?>) {
            /*
             * Here, invoke of(Class, Type[]) with two arguments. 
             * otherwise, unlimited recursion will raise.
             */
            return of((Class<?>)type, EMPTY_TYPES);
        }
        if (type instanceof ParameterizedType) {
            return of((ParameterizedType)type);
        }
        Arguments.mustBeInstanceOfAnyOfValue("type", type, Class.class, ParameterizedType.class);
        throw new AssertionError(/* impossible */);
    }
    
    private static ClassInfo<?> of(
            Map<Type, ClassInfo<?>> contextMap, 
            Class<?> clazz,
            GenericResolver derivedClassResolver) {
        Class<?> ownerClass = clazz.getDeclaringClass();
        ClassInfo<?> ownerClassInfo = null;
        if (ownerClass != null) {
            ownerClassInfo = of(contextMap, ownerClass, null);
        }
        return of(
                contextMap,
                ownerClassInfo,
                new GenericResolver(clazz, derivedClassResolver));
    }
    
    private static ClassInfo<?> of(
            Map<Type, ClassInfo<?>> contextMap, 
            ParameterizedType parameterizedType,
            GenericResolver derviedClassResolver) {
        Type ownerType = parameterizedType.getOwnerType();
        ClassInfo<?> ownerClassInfo = null;
        if (ownerType != null) {
            if (ownerType instanceof Class<?>) {
                ownerClassInfo = of(contextMap, (Class<?>)ownerType, null);
            } else {
                ownerClassInfo = of(contextMap, (ParameterizedType)ownerType, null);
            }
        }
        return of(
                contextMap,
                ownerClassInfo,
                new GenericResolver(parameterizedType, derviedClassResolver));
    } 
    
    private static ClassInfo<?> of(
            Map<Type, ClassInfo<?>> contextMap, 
            ClassInfo<?> declaringClass,
            GenericResolver genericResolver) {
        Class<?> clazz = genericResolver.getRawClass();
        Type type = genericResolver.rawTypeForClassInfo;
        ClassInfo<?> classInfo = null;
        WeakHashMap<Type, SoftReference<ClassInfo<?>>> map;
        Lock lock;
        
        (lock = CACHE_LOCK.readLock()).lock();
        try {
            // 1st reading in read lock.
            map = CACHE.get(clazz);
            if (map != null) {
                SoftReference<ClassInfo<?>> softReference = map.get(type);
                if (softReference != null) {
                    classInfo = softReference.get();
                }
            }
        } finally {
            lock.unlock();
        }
        
        // 1st checking
        if (classInfo == null) {
            (lock = CACHE_LOCK.writeLock()).lock();
            try {
                //2nd reading in write lock
                map = CACHE.get(clazz);
                if (map != null) {
                    SoftReference<ClassInfo<?>> softReference = map.get(type);
                    if (softReference != null) {
                        classInfo = softReference.get();
                    }
                }
                //2nd checking
                if (classInfo == null) {
                    classInfo = contextMap.get(type);
                    if (classInfo == null) {
                        classInfo = new ClassInfo<Object>(contextMap, declaringClass, genericResolver);
                        contextMap.put(type, classInfo);
                    }
                    if (map == null) {
                        map = new WeakHashMap<>();
                    }
                    map.put(type, new SoftReference<ClassInfo<?>>(classInfo));
                    CACHE.put(clazz, map);
                }
            } finally {
                lock.unlock();
            }
        }
        return (ClassInfo<?>)classInfo;
    }
    
    public static Class<?> forName(String className) {
        Arguments.mustNotBeEmpty("className", Arguments.mustNotBeNull("className", className));
        if (className.charAt(0) == '[') {
            return Array.newInstance(forName(className.substring(1)), 0).getClass();
        }
        if (className.endsWith("[]")) {
            return Array.newInstance(forName(className.substring(0, className.length() - 2)), 0).getClass();
        }
        try {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException ex) {
                return Class.forName(className);
            }
        } catch (ClassNotFoundException ex) {
            throw new NoSuchClassException(ex);
        }
    }
    
    public static Class<?> forInternalName(String internalNameOrDescriptor) {
        Arguments.mustNotBeEmpty("internalName", Arguments.mustNotBeNull("internalName", internalNameOrDescriptor));
        if (internalNameOrDescriptor.charAt(0) == '[') {
            return Array.newInstance(forInternalName(internalNameOrDescriptor.substring(1)), 0).getClass();
        }
        if (internalNameOrDescriptor.endsWith("[]")) {
            return Array.newInstance(forInternalName(internalNameOrDescriptor.substring(0, internalNameOrDescriptor.length() - 2)), 0).getClass();
        }
        if (internalNameOrDescriptor.length() == 1) {
            switch (internalNameOrDescriptor.charAt(0)) {
            case 'Z':
                return boolean.class;
            case 'C':
                return char.class;
            case 'B':
                return byte.class;
            case 'S':
                return short.class;
            case 'I':
                return int.class;
            case 'J':
                return long.class;
            case 'F':
                return float.class;
            case 'D':
                return double.class;
            case 'V':
                return void.class;
            }
        }
        if (internalNameOrDescriptor.charAt(0) == 'L') {
            if(internalNameOrDescriptor.charAt(internalNameOrDescriptor.length() - 1) != ';') {
                throw new IllegalArgumentException(descriptorMustEndWithSemi(internalNameOrDescriptor));
            }
            internalNameOrDescriptor = internalNameOrDescriptor.substring(1, internalNameOrDescriptor.length() - 1);
        }
        internalNameOrDescriptor = internalNameOrDescriptor.replace('/', '.');
        try {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(internalNameOrDescriptor);
            } catch (ClassNotFoundException ex) {
                return Class.forName(internalNameOrDescriptor);
            }
        } catch (ClassNotFoundException ex) {
            throw new NoSuchClassException(ex);
        }
    }
    
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return this.getRawClass().isAnnotationPresent(annotationClass);
    }

    public boolean isAnnotation() {
        return this.getRawClass().isAnnotation();
    }

    public boolean isArray() {
        return this.getRawClass().isArray();
    }

    public boolean isEnum() {
        return this.getRawClass().isEnum();
    }

    public boolean isInterface() {
        return this.getRawClass().isInterface();
    }

    public boolean isMemberClass() {
        return this.getRawClass().isMemberClass();
    }

    public boolean isNonStaticMemberClass() {
        return isNonStaticMemberClass(this.getRawClass());
    }

    public boolean isGenericClass() {
        return isGenericClass(this.getRawClass());
    }

    @SuppressWarnings("unchecked")
    public Class<T> getRawClass() {
        return 
            this.rawType instanceof Class<?> ?
                (Class<T>)this.rawType :
                (Class<T>)((ParameterizedType)this.rawType).getRawType();
    }
    
    public Type getRawType() {
        return this.rawType;
    }
    
    public ModifierSet getModifiers() {
        return ModifierSet.forType(this.getRawClass().getModifiers());
    }
    
    public String getName() {
        return this.getRawClass().getName();
    }
    
    public String getSimpleName() {
        return this.getRawClass().getSimpleName();  
    }
    
    public String getCanonicalName() {
        return this.getRawClass().getCanonicalName();
    }
    
    @SuppressWarnings("unchecked")
    public ClassInfo<? super T> getSuperClass() {
        return (ClassInfo<? super T>)this.superClass;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<ClassInfo<? super T>> getInterfaces() {
        return (List)this.interfaces;
    }
    
    public List<ClassInfo<? super T>> getMinimumInterfaces() {
        return this.minimumInterfaces;
    }
    
    @SuppressWarnings("unchecked")
    public ClassInfo<? super T> getAncestor(Class<?> ancestorRawClass) {
        if (this.getRawClass() == ancestorRawClass) {
            return this;
        }
        ClassInfo<?> superClass = (ClassInfo<? super T>)this.superClass;
        if (superClass != null) {
            ClassInfo<?> ancestor = superClass.getAncestor(ancestorRawClass);
            if (ancestor != null) {
                return (ClassInfo<? super T>)ancestor;
            }
        }
        for (ClassInfo<?> interface_ : this.interfaces) {
            ClassInfo<?> ancestor = interface_.getAncestor(ancestorRawClass);
            if (ancestor != null) {
                return (ClassInfo<? super T>)ancestor;
            }
        }
        return null;
    }
    
    public ClassInfo<?> getDeclaringClass() {
        return this.declaringClass;
    }
    
    public List<TypeVariable<Class<T>>> getTypeParameters() {
        return this.typeParameters;
    }
    
    public Collection<FieldInfo> getDeclaredFields() {
        Collection<FieldInfo> declaredFields = null;
        if (this.declaredFieldsRef != null) {
            declaredFields = this.declaredFieldsRef.get();
        }
        if (declaredFields == null) {
            Field[] fields = this.getRawClass().getDeclaredFields();
            FieldInfo[] fieldInfos = new FieldInfo[fields.length];
            for (int i = fieldInfos.length - 1; i >= 0; i--) {
                fieldInfos[i] = new FieldInfo(fields[i], this);
            }
            declaredFields = MACollections.wrap(fieldInfos);
            this.declaredFieldsRef = new SoftReference<Collection<FieldInfo>>(declaredFields);
        }
        return declaredFields;
    }
    
    public Collection<FieldInfo> getFields() {
        Collection<FieldInfo> fields = null;
        if (this.fieldsRef != null) {
            fields = this.fieldsRef.get();
        }
        if (fields == null) {
            final Set<FieldInfo> fieldInfos = new LinkedHashSet<FieldInfo>();
            this.traveler().depthFirstTravel(
                    this,
                    new GraphTravelAction<ClassInfo<?>>() {
                        @Override
                        public void preTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                            fieldInfos.addAll(ctx.getNode().getDeclaredFields());
                        }
                        @Override
                        public void postTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                            
                        }
                    });
            fields = MACollections.wrap(fieldInfos.toArray(new FieldInfo[fieldInfos.size()]));
            this.fieldsRef = new SoftReference<Collection<FieldInfo>>(fields);
        }
        return fields;
    }

    public Collection<MethodInfo> getDeclaredMethods() {
        Collection<MethodInfo> declaredMethods = null;
        if (this.declaredMethodsRef != null) {
            declaredMethods = this.declaredMethodsRef.get();
        }
        if (declaredMethods == null) {
            Method[] methods = this.getRawClass().getDeclaredMethods();
            MethodInfo[] methodInfos = new MethodInfo[methods.length];
            for (int i = methodInfos.length - 1; i >= 0; i--) {
                methodInfos[i] = new MethodInfo(methods[i], this);
            }
            declaredMethods = MACollections.wrap(methodInfos);
            this.declaredMethodsRef = new SoftReference<Collection<MethodInfo>>(declaredMethods);
        }
        return declaredMethods;
    }

    public Collection<MethodInfo> getMethods() {
        Collection<MethodInfo> methods = null;
        if (this.methodsRef != null) {
            methods = this.methodsRef.get();
        }
        if (methods == null) {
            final Set<MethodInfo> methodInfos = new LinkedHashSet<MethodInfo>();
            this.traveler().depthFirstTravel(
                    this,
                    new GraphTravelAction<ClassInfo<?>>() {
                        @Override
                        public void preTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                            methodInfos.addAll(ctx.getNode().getDeclaredMethods());
                        }
                        @Override
                        public void postTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                            
                        }
                    });
            methods = MACollections.wrap(methodInfos.toArray(new MethodInfo[methodInfos.size()]));
            this.methodsRef = new SoftReference<Collection<MethodInfo>>(methods);
        }
        return methods;
    }

    public Collection<PropertyInfo> getDeclaredProperties() {
        Collection<PropertyInfo> declaredProperties = null;
        if (this.declaredPropertiesRef != null) {
            declaredProperties = this.declaredPropertiesRef.get();
        }
        if (declaredProperties == null) {
            Collection<PropertyInfo> properties = new ArrayList<PropertyInfo>();
            Set<MethodInfo> usedMethodsByProperties = new HashSet<MethodInfo>();
            for (MethodInfo method : this.getDeclaredMethods()) {
                if (!usedMethodsByProperties.contains(method)) {
                String methodName = method.getName();
                    if (methodName.startsWith("is") || 
                            methodName.startsWith("get") || 
                            methodName.startsWith("set")) {
                        try {
                            PropertyInfo property = new PropertyInfo(method);
                            properties.add(property);
                            usedMethodsByProperties.add(property.getGetter());
                            usedMethodsByProperties.add(property.getSetter());
                        } catch (InvalidPropertyException ignore) {
                            // Ignore exception
                        }
                    }
                }
            }
            declaredProperties = MACollections.wrap(
                    properties.toArray(new PropertyInfo[properties.size()]));
            this.declaredPropertiesRef = new SoftReference<Collection<PropertyInfo>>(declaredProperties);
        }
        return declaredProperties;
    }

    public Collection<PropertyInfo> getProperties() {
        Collection<PropertyInfo> properties = null;
        if (this.propertiesRef != null) {
            properties = this.propertiesRef.get();
        }
        if (properties == null) {
            final Set<PropertyInfo> propertyInfos = new LinkedHashSet<PropertyInfo>();
            this.traveler().depthFirstTravel(
                    this,
                    new GraphTravelAction<ClassInfo<?>>() {
                        @Override
                        public void preTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                            propertyInfos.addAll(ctx.getNode().getDeclaredProperties());
                        }
                        @Override
                        public void postTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                            
                        }
                    });
            properties = MACollections.wrap(propertyInfos.toArray(new PropertyInfo[propertyInfos.size()]));
            this.propertiesRef = new SoftReference<Collection<PropertyInfo>>(properties);
        }
        return properties;
    }

    public Collection<ClassInfo<?>> getDeclaredNestedClasses() {
        Collection<ClassInfo<?>> declaredNestedClasses = null;
        if (this.declaredNestedClassesRef != null) {
            declaredNestedClasses = this.declaredNestedClassesRef.get();
        }
        if (declaredNestedClasses == null) {
            Class<?>[] declaredRawClasses = this.getRawClass().getDeclaredClasses();
            ClassInfo<?>[] declaredNestedClassInfos = new ClassInfo[declaredRawClasses.length];
            for (int i = declaredRawClasses.length - 1; i >= 0; i--) {
                declaredNestedClassInfos[i] = of(this, declaredRawClasses[i]);
            }
            declaredNestedClasses = MACollections.wrap(declaredNestedClassInfos);
            this.declaredNestedClassesRef = new SoftReference<Collection<ClassInfo<?>>>(declaredNestedClasses);
        }
        return declaredNestedClasses;
    }

    public Collection<ClassInfo<?>> getNestedClasses() {
        Collection<ClassInfo<?>> nestedClasses = null;
        if (this.nestedClassesRef != null) {
            nestedClasses = this.nestedClassesRef.get();
        }
        if (nestedClasses == null) {
            final Set<ClassInfo<?>> nestedClassInfos = new LinkedHashSet<ClassInfo<?>>();
            this.traveler().depthFirstTravel(
                    this,
                    new GraphTravelAction<ClassInfo<?>>() {
                        @Override
                        public void preTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                            nestedClassInfos.addAll(ctx.getNode().getDeclaredNestedClasses());
                        }
                        @Override
                        public void postTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                            
                        }
                    });
            nestedClasses = MACollections.wrap(nestedClassInfos.toArray(new ClassInfo<?>[nestedClassInfos.size()]));
            this.nestedClassesRef = new SoftReference<Collection<ClassInfo<?>>>(nestedClasses);
        }
        return nestedClasses;
    }
    
    public Map<MethodDescriptor, MethodImplementation> getMethodImplementationMap() {
        Map<MethodDescriptor, MethodImplementation> methodImplementationMap = null;
        if (this.methodImplementationMapRef != null) {
            methodImplementationMap = this.methodImplementationMapRef.get();
        }
        if (methodImplementationMap == null) {
            Package pkg = this.getRawClass().getPackage();
            methodImplementationMap = MethodImplementation.getMethodImplementationMap(
                    pkg != null ? pkg.getName() : null, this);
            this.methodImplementationMapRef = 
                new SoftReference<Map<MethodDescriptor, MethodImplementation>>(methodImplementationMap);
        }
        return methodImplementationMap;
    }

    public FieldInfo getDeclaredField(String name) {
        for (FieldInfo fieldInfo : this.getDeclaredFields()) {
            if (fieldInfo.getName().equals(name)) {
                return fieldInfo;
            }
        }
        throw new NoSuchFieldInfoException(noSuchDeclaredFieldInfo(this, name));
    }

    public FieldInfo getField(String name) {
        for (FieldInfo field : this.getDeclaredFields()) {
            if (field.getModifiers().isPublic() && field.getName().equals(name)) {
                return field;
            }
        }
        if (this.superClass != null) {
            try {
                return this.superClass.getField(name);
            } catch (NoSuchFieldInfoException ignore) {
                //Ignore exception
            }
        }
        for (ClassInfo<?> interface_ : this.interfaces) {
            try {
                return interface_.getField(name);
            } catch (NoSuchFieldInfoException ignore) {
                // Ignore exception
            }
        }
        throw new NoSuchFieldInfoException(noSuchFieldInfo(this, name));
    }

    public MethodInfo tryGetDeclaredErasedMethod(
            MethodDescriptor descriptor) {
        return this.tryGetDeclaredErasedMethod(
                descriptor.getReturnType(), 
                descriptor.getName(), 
                descriptor.getParameterTypes());
    }
    
    public MethodInfo tryGetDeclaredErasedMethod(
            Class<?> returnType, String name, List<Class<?>> erasedParameterTypes) {
        Arguments.mustNotBeNull("name", name);
        if (erasedParameterTypes == null) {
            erasedParameterTypes = MACollections.wrap((Class<?>[])null);
        }
        MethodInfo matchedMethod = null;
        for (MethodInfo method : this.getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getParameterTypes().equals(erasedParameterTypes)) {
                if (returnType == null) {
                    if (matchedMethod == null || matchedMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
                        matchedMethod = method;
                    }
                } else if (returnType == method.getReturnType()) {
                    return method;
                }
            }
        }
        return matchedMethod;
    }
    
    public MethodInfo tryGetDeclaredErasedMethod(
            Class<?> returnType, String name, Class<?> ... erasedParameterTypes) {
        return this.tryGetDeclaredErasedMethod(returnType, name, MACollections.wrap(erasedParameterTypes));
    }
    
    public MethodInfo tryGetDeclaredErasedMethod(
            String name, List<Class<?>> erasedParameterTypes) {
        return this.tryGetDeclaredErasedMethod(null, name, erasedParameterTypes);
    }
    
    public MethodInfo tryGetDeclaredErasedMethod(
            String name, Class<?> ... erasedParameterTypes) {
        return this.tryGetDeclaredErasedMethod(null, name, erasedParameterTypes);
    }
    
    public MethodInfo getDeclaredErasedMethod(MethodDescriptor descriptor) {
        return this.getDeclaredErasedMethod(
                descriptor.getReturnType(), 
                descriptor.getName(), 
                descriptor.getParameterTypes());
    }

    public MethodInfo getDeclaredErasedMethod(
            Class<?> returnType, String name, List<Class<?>> erasedParameterTypes) {
        MethodInfo methodInfo = this.tryGetDeclaredErasedMethod(
                returnType, name, erasedParameterTypes);
        if (methodInfo == null) {
            if (returnType == null) {
                throw new NoSuchMethodInfoException(
                        noSuchDeclaredErasedMethodInfo(
                                this, 
                                name, 
                                erasedParameterTypes
                        )
                );
            } else {
                throw new NoSuchMethodInfoException(
                        noSuchDeclaredErasedMethodInfoWithReturnType(
                                this, 
                                returnType, 
                                name, 
                                erasedParameterTypes
                        )
                );
            }
        }
        return methodInfo;
    }

    public MethodInfo getDeclaredErasedMethod(
            Class<?> returnType, String name, Class<?> ... erasedParameterTypes) {
        return this.getDeclaredErasedMethod(
                returnType, name, MACollections.wrap(erasedParameterTypes));
    }

    public MethodInfo getDeclaredErasedMethod(
            String name, List<Class<?>> erasedParameterTypes) {
        return this.getDeclaredErasedMethod(
                null, name, erasedParameterTypes);
    }

    public MethodInfo getDeclaredErasedMethod(
            String name, Class<?> ... erasedParameterTypes) {
        return this.getDeclaredErasedMethod(
                null, name, MACollections.wrap(erasedParameterTypes));
    }
    
    public MethodInfo tryGetDeclaredResolvedMethod(
            MethodDescriptor descriptor) {
        return this.tryGetDeclaredResolvedMethod(
                descriptor.getReturnType(), 
                descriptor.getName(), 
                descriptor.getParameterTypes());
    }

    public MethodInfo tryGetDeclaredResolvedMethod(
            Class<?> returnType, 
            String name, 
            List<Class<?>> resolvedParameterTypes) {
        Arguments.mustNotBeNull("name", name);
        if (resolvedParameterTypes == null) {
            resolvedParameterTypes = MACollections.wrap((Class<?>[])null);
        }
        MethodInfo matchedMethod = null;
        for (MethodInfo method : this.getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getResolvedParameterTypes().equals(resolvedParameterTypes)) {
                if (returnType == null || returnType == method.getResolvedReturnType()) {
                    if (matchedMethod == null) {
                        matchedMethod = method;
                    } else {
                        Integer cmp = compare(matchedMethod, method);
                        if (cmp == null) {
                            if (returnType == null) {
                                throw new TooManyMethodInfoException(
                                        tooManyDeclaredResolvedMethodInfo(this, name, resolvedParameterTypes)
                                );
                            } else {
                                throw new TooManyMethodInfoException(
                                        tooManyDeclaredResolvedMethodInfoWithReturnType(
                                                this, 
                                                returnType, 
                                                name, 
                                                resolvedParameterTypes
                                        )
                                );
                            }
                        }
                        if (cmp < 0) {
                            matchedMethod = method;
                        }
                    }
                }
            }
        }
        return matchedMethod;
    }
    
    public MethodInfo tryGetDeclaredResolvedMethod(
            Class<?> returnType, 
            String name, 
            Class<?> ... resolvedParameterTypes) {
        return this.tryGetDeclaredResolvedMethod(
                returnType, name, MACollections.wrap(resolvedParameterTypes));
    }
    
    public MethodInfo tryGetDeclaredResolvedMethod(
            String name, 
            List<Class<?>> resolvedParameterTypes) {
        return this.tryGetDeclaredResolvedMethod(null, name, resolvedParameterTypes);
    }
    
    public MethodInfo tryGetDeclaredResolvedMethod(
            String name, 
            Class<?> ... resolvedParameterTypes) {
        return this.tryGetDeclaredResolvedMethod(null, name, resolvedParameterTypes);
    }
    
    public MethodInfo getDeclaredResolvedMethod(MethodDescriptor descriptor) {
        return this.getDeclaredResolvedMethod(
                descriptor.getReturnType(), 
                descriptor.getName(), 
                descriptor.getParameterTypes());
    }

    public MethodInfo getDeclaredResolvedMethod(
            Class<?> returnType, String name, List<Class<?>> resolvedParameterTypes) {
        MethodInfo methodInfo = this.tryGetDeclaredResolvedMethod(
                returnType, name, resolvedParameterTypes);
        if (methodInfo == null) {
            if (returnType == null) {
                throw new NoSuchMethodInfoException(
                        noSuchDeclaredResolvedMethodInfo(this, name, resolvedParameterTypes)
                );
            } else {
                throw new NoSuchMethodInfoException(
                        noSuchDeclaredResolvedMethodInfoWithReturnType(this, returnType, name, resolvedParameterTypes)
                );
            }
        }
        return methodInfo;
    }

    public MethodInfo getDeclaredResolvedMethod(
            Class<?> returnType, String name, Class<?> ... resolvedParameterTypes) {
        return this.getDeclaredResolvedMethod(
                returnType, name, MACollections.wrap(resolvedParameterTypes));
    }

    public MethodInfo getDeclaredResolvedMethod(
            String name, List<Class<?>> resolvedParameterTypes) {
        return this.getDeclaredResolvedMethod(null, name, resolvedParameterTypes);
    }

    public MethodInfo getDeclaredResolvedMethod(
            String name, Class<?> ... resolvedParameterTypes) {
        return this.getDeclaredResolvedMethod(
                null, name, MACollections.wrap(resolvedParameterTypes));
    }

    public MethodInfo tryGetErasedMethod(
            MethodDescriptor descriptor) {
        return this.tryGetErasedMethod(
                descriptor.getReturnType(), 
                descriptor.getName(), 
                descriptor.getParameterTypes());
    }

    public MethodInfo tryGetErasedMethod(
            final Class<?> returnType, final String name, final List<Class<?>> erasedParameterTypes) {
        final Ref<MethodInfo> methodInfoRef = new Ref<MethodInfo>();
        this.traveler().depthFirstTravel(
                this,
                new GraphTravelAction<ClassInfo<?>>() {
                    @Override
                    public void preTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                        MethodInfo methodInfo = ctx.getNode().tryGetDeclaredErasedMethod(
                                returnType, name, erasedParameterTypes);
                        if (methodInfo != null) {
                            methodInfoRef.set(methodInfo);
                            ctx.stopTravel();
                        }
                    }
                    @Override
                    public void postTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                                            
                    }
                });
        return methodInfoRef.get();
    }
    
    public MethodInfo tryGetErasedMethod(
            Class<?> returnType, String name, Class<?> ... erasedParameterTypes) {
        return this.tryGetErasedMethod(returnType, name, MACollections.wrap(erasedParameterTypes));
    }
    
    public MethodInfo tryGetErasedMethod(
            String name, List<Class<?>> erasedParameterTypes) {
        return this.tryGetErasedMethod(null, name, erasedParameterTypes);
    }
    
    public MethodInfo tryGetErasedMethod(
            String name, Class<?> ... erasedParameterTypes) {
        return this.tryGetErasedMethod(null, name, erasedParameterTypes);
    }
    
    public MethodInfo getErasedMethod(MethodDescriptor descriptor) {
        return this.getErasedMethod(
                descriptor.getReturnType(), 
                descriptor.getName(), 
                descriptor.getParameterTypes());
    }

    public MethodInfo getErasedMethod(
            Class<?> returnType, String name, List<Class<?>> erasedParameterTypes) {
        MethodInfo methodInfo = this.tryGetErasedMethod(
                returnType, name, erasedParameterTypes);
        if (methodInfo == null) {
            if (returnType == null) {
                throw new NoSuchMethodInfoException(
                        noSuchErasedMethodInfo(this, name, erasedParameterTypes)
                );
            } else {
                throw new NoSuchMethodInfoException(
                        noSuchErasedMethodInfoWithReturnType(this, returnType, name, erasedParameterTypes)
                );
            }
        }
        return methodInfo;
    }

    public MethodInfo getErasedMethod(
            Class<?> returnType, String name, Class<?> ... erasedParameterTypes) {
        return this.getErasedMethod(returnType, name, MACollections.wrap(erasedParameterTypes));
    }

    public MethodInfo getErasedMethod(
            String name, List<Class<?>> erasedParameterTypes) {
        return this.getErasedMethod(null, name, erasedParameterTypes);
    }

    public MethodInfo getErasedMethod(
            String name, Class<?> ... erasedParameterTypes) {
        return this.getErasedMethod(
                null, name, MACollections.wrap(erasedParameterTypes));
    }
    
    public MethodInfo tryGetResolvedMethod(
            MethodDescriptor descriptor) {
        return this.tryGetResolvedMethod(
                descriptor.getReturnType(), 
                descriptor.getName(), 
                descriptor.getParameterTypes());
    }

    public MethodInfo tryGetResolvedMethod(
            final Class<?> returnType, final String name, final List<Class<?>> resolvedParameterTypes) {
        final Ref<MethodInfo> methodInfoRef = new Ref<MethodInfo>();
        this.traveler().depthFirstTravel(
                this,
                new GraphTravelAction<ClassInfo<?>>() {
                    @Override
                    public void preTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                        MethodInfo methodInfo = ctx.getNode().tryGetDeclaredResolvedMethod(
                                returnType, name, resolvedParameterTypes);
                        if (methodInfo != null) {
                            if (methodInfoRef.get() != null) {
                                if (returnType == null) {
                                    throw new TooManyMethodInfoException(
                                            tooManyResolvedMethodInfo(
                                                    ClassInfo.this, 
                                                    name, 
                                                    resolvedParameterTypes
                                            )
                                    );
                                } else {
                                    throw new TooManyMethodInfoException(
                                            tooManyResolvedMethodInfoWithReturnType(
                                                    ClassInfo.this, 
                                                    returnType,
                                                    name, 
                                                    resolvedParameterTypes
                                            )
                                    );
                                }
                            }
                            methodInfoRef.set(methodInfo);
                            ctx.stopTravelNeighborNodes();
                        }
                    }
                    @Override
                    public void postTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                                            
                    }
                });
        return methodInfoRef.get();
    }
    
    public MethodInfo tryGetResolvedMethod(
            Class<?> returnType, String name, Class<?> ... resolvedParameterTypes) {
        return this.getResolvedMethod(returnType, name, MACollections.wrap(resolvedParameterTypes));
    }
    
    public MethodInfo tryGetResolvedMethod(String name, List<Class<?>> resolvedParameterTypes) {
        return this.getResolvedMethod(null, name, resolvedParameterTypes);
    }
    
    public MethodInfo tryGetResolvedMethod(String name, Class<?> ... resolvedParameterTypes) {
        return this.getResolvedMethod(name, resolvedParameterTypes);
    }
    
    public MethodInfo getResolvedMethod(MethodDescriptor descriptor) {
        return this.getResolvedMethod(
                descriptor.getReturnType(), 
                descriptor.getName(),
                descriptor.getParameterTypes());
    }

    public MethodInfo getResolvedMethod(
            Class<?> returnType, String name, List<Class<?>> resolvedParameterTypes) {
        MethodInfo methodInfo = this.tryGetResolvedMethod(
                returnType, name, resolvedParameterTypes);
        if (methodInfo == null) {
            if (returnType == null) {
                throw new NoSuchMethodInfoException(
                        noSuchResolvedMethodInfo(this, name, resolvedParameterTypes)
                );
            } else {
                throw new NoSuchMethodInfoException(
                        noSuchResolvedMethodInfoWithReturnType(this, returnType, name, resolvedParameterTypes)
                );
            }
        }
        return methodInfo;
    }

    public MethodInfo getResolvedMethod(
            Class<?> returnType, String name, Class<?> ... resolvedParameterTypes) {
        return this.getResolvedMethod(
                returnType, name, MACollections.wrap(resolvedParameterTypes));
    }

    public MethodInfo getResolvedMethod(
            String name, List<Class<?>> resolvedParameterTypes) {
        return this.getResolvedMethod(null, name, resolvedParameterTypes);
    }

    public MethodInfo getResolvedMethod(
            String name, Class<?> ... resolvedParameterTypes) {
        return this.getResolvedMethod(
                null, name, MACollections.wrap(resolvedParameterTypes));
    }

    @SuppressWarnings("unchecked")
    public Collection<ConstructorInfo<T>> getDeclaredConstructors() {
        Collection<ConstructorInfo<T>> declaredConstructors = null;
        if (this.declaredConstructorsRef != null) {
            declaredConstructors = this.declaredConstructorsRef.get();
        }
        if (declaredConstructors == null) {
            Constructor<T>[] constructors = (Constructor<T>[])this.getRawClass().getDeclaredConstructors();
            ConstructorInfo<T>[] constructorInfos = new ConstructorInfo[constructors.length];
            for (int i = constructorInfos.length - 1; i >= 0; i--) {
                constructorInfos[i] = new ConstructorInfo<T>(constructors[i], this);
            }
            declaredConstructors = MACollections.wrap(constructorInfos);
            this.declaredConstructorsRef = 
                new SoftReference<Collection<ConstructorInfo<T>>>(declaredConstructors);
        }
        return declaredConstructors;
    }

    public ConstructorInfo<T> tryGetDeclaredErasedConstructor(
            List<Class<?>> erasedParameterTypes) {
        if (erasedParameterTypes == null) {
            erasedParameterTypes = MACollections.wrap((Class<?>[])null);
        }
        for (ConstructorInfo<T> constructor : this.getDeclaredConstructors()) {
            if (constructor.getParameterTypes().equals(erasedParameterTypes)) {
                return constructor;
            }
        }
        return null;
    }
    
    public ConstructorInfo<T> tryGetDeclaredErasedConstructor(
            Class<?> ... erasedParameterTypes) {
        return this.tryGetDeclaredErasedConstructor(MACollections.wrap(erasedParameterTypes));
    }

    public ConstructorInfo<T> getDeclaredErasedConstructor(
            List<Class<?>> erasedParameterTypes) {
        ConstructorInfo<T> constructorInfo = 
            this.tryGetDeclaredErasedConstructor(erasedParameterTypes);
        if (constructorInfo == null) {
            throw new NoSuchConstructorInfoException(
                    noSuchDeclaredErasedConstructorInfo(this, erasedParameterTypes)
            );
        }
        return constructorInfo;
    }

    public ConstructorInfo<T> getDeclaredErasedConstructor(
            Class<?> ... erasedParameterTypes) {
        return this.getDeclaredErasedConstructor(
                MACollections.wrap(erasedParameterTypes));
    }

    public ConstructorInfo<T> tryGetDeclaredResolvedConstructor(
            List<Class<?>> resolvedParameterTypes) {
        if (resolvedParameterTypes == null) {
            resolvedParameterTypes = MACollections.wrap((Class<?>[])null);
        }
        ConstructorInfo<T> matchedConstructor = null;
        for (ConstructorInfo<T> constructor : this.getDeclaredConstructors()) {
            if (constructor.getResolvedParameterTypes().equals(resolvedParameterTypes)) {
                if (matchedConstructor == null) {
                    matchedConstructor = constructor;
                } else {
                    Integer cmp = compare(matchedConstructor, constructor);
                    if (cmp == null) {
                        throw new TooManyConstructorInfoException(
                                tooManyDeclaredResolvedConstructorInfo(this, resolvedParameterTypes)
                        );
                    }
                    if (cmp < 0) {
                        matchedConstructor = constructor;
                    }
                }
            }
        }
        return matchedConstructor;
    }
    
    public ConstructorInfo<T> tryGetDeclaredResolvedConstructor(
            Class<?> ... resolvedParameterTypes) {
        return this.tryGetDeclaredResolvedConstructor(
                MACollections.wrap(resolvedParameterTypes));
    }

    public ConstructorInfo<T> getDeclaredResolvedConstructor(
            List<Class<?>> resolvedParameterTypes) {
        ConstructorInfo<T> constructorInfo = 
            this.tryGetDeclaredResolvedConstructor(resolvedParameterTypes);
        if (constructorInfo == null) {
            throw new NoSuchConstructorInfoException(
                    noSuchDeclaredResolvedConstructorInfo(this, resolvedParameterTypes)
            );
        }
        return constructorInfo;
    }

    public ConstructorInfo<T> getDeclaredResolvedConstructor(
            Class<?> ... resolvedParameterTypes) {
        return this.getDeclaredResolvedConstructor(
                MACollections.wrap(resolvedParameterTypes));
    }

    public PropertyInfo tryGetDeclaredErasedProperty(String name, List<Class<?>> erasedParameterTypes) {
        Arguments.mustNotBeNull("name", name);
        if (erasedParameterTypes == null) {
            erasedParameterTypes = MACollections.<Class<?>>wrap((Class<?>[])null);
        }
        PropertyInfo matchedProperty = null;
        for (PropertyInfo property : this.getDeclaredProperties()) {
            if (property.getName().equals(name) && 
                    property.getParameterTypes().equals(erasedParameterTypes)) {
                if (matchedProperty == null || 
                        matchedProperty.getReturnType().isAssignableFrom(property.getReturnType())) {
                    matchedProperty = property;
                }
            }
        }
        return matchedProperty;
    }

    public PropertyInfo getDeclaredErasedProperty(
            String name, List<Class<?>> erasedParameterTypes) {
        PropertyInfo propertyInfo = this.tryGetDeclaredErasedProperty(
                name, erasedParameterTypes);
        if (propertyInfo == null) {
            throw new NoSuchPropertyInfoException(
                    noSuchDeclaredErasedPropertyInfo(this, name, erasedParameterTypes)
            );
        }
        return propertyInfo;
    }

    public PropertyInfo getDeclaredErasedProperty(
            String name, Class<?> ... erasedParameterTypes) {
        return this.getDeclaredErasedProperty(
                name, MACollections.wrap(erasedParameterTypes));
    }

    public PropertyInfo tryGetDeclaredResolvedProperty(
            String name, List<Class<?>> resolvedParameterTypes) {
        Arguments.mustNotBeNull("name", name);
        if (resolvedParameterTypes == null) {
            resolvedParameterTypes = MACollections.<Class<?>>wrap((Class<?>[])null);
        }
        PropertyInfo matchedProperty = null;
        for (PropertyInfo property : this.getDeclaredProperties()) {
            if (property.getName().equals(name) && 
                    property.getResolvedParameterTypes().equals(resolvedParameterTypes)) {
                if (matchedProperty == null || 
                        matchedProperty.getReturnType().isAssignableFrom(property.getReturnType())) {
                    if (matchedProperty == null) {
                        matchedProperty = property;
                    } else {
                        Integer cmp = compare(matchedProperty, property);
                        if (cmp == null) {
                            throw new TooManyPropertyInfoException(
                                    tooManyDeclaredResolvedPropertyInfo(this, name, resolvedParameterTypes)
                            );
                        }
                        if (cmp < 0) {
                            matchedProperty = property;
                        }
                    }
                }
            }
        }
        return matchedProperty;
    }

    public PropertyInfo getDeclaredResolvedProperty(
            String name, List<Class<?>> resolvedParameterTypes) {
        PropertyInfo propertyInfo = this.tryGetDeclaredResolvedProperty(
                name, resolvedParameterTypes);
        if (propertyInfo == null) {
            throw new NoSuchPropertyInfoException(
                    noSuchDeclaredResolvedPropertyInfo(this, name, resolvedParameterTypes)
            );
        }
        return propertyInfo;
    }

    public PropertyInfo getDeclaredResolvedProperty(
            String name, Class<?> ... resolvedParameterTypes) {
        return this.getDeclaredResolvedProperty(
                name, MACollections.wrap(resolvedParameterTypes));
    }

    public PropertyInfo tryGetErasedProperty(
            final String name, final List<Class<?>> erasedParameterTypes) {
        final Ref<PropertyInfo> propertyInfoRef = new Ref<PropertyInfo>();
        this.traveler().depthFirstTravel(
                this,
                new GraphTravelAction<ClassInfo<?>>() {
                    @Override
                    public void preTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                        PropertyInfo propertyInfo = 
                            ctx.getNode().getDeclaredErasedProperty(
                                    name, erasedParameterTypes);
                        if (propertyInfo != null) {
                            propertyInfoRef.set(propertyInfo);
                            ctx.stopTravel();
                        }
                    }
                    @Override
                    public void postTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                        
                    }
                });
        return propertyInfoRef.get();
    }

    public PropertyInfo getErasedProperty(
            String name, List<Class<?>> erasedParameterTypes) {
        PropertyInfo propertyInfo = this.tryGetErasedProperty(
                name, 
                erasedParameterTypes);
        if (propertyInfo == null) {
            throw new NoSuchPropertyInfoException(
                    noSuchErasedPropertyInfo(this, name, erasedParameterTypes)
            );
        }
        return propertyInfo;
    }

    public PropertyInfo getErasedProperty(
            String name, Class<?> ... erasedParameterTypes) {
        return this.getErasedProperty(
                name, MACollections.wrap(erasedParameterTypes));
    }

    public PropertyInfo tryGetResolvedProperty(
            final String name, final List<Class<?>> resolvedParameterTypes) {
        final Ref<PropertyInfo> propertyInfoRef = new Ref<PropertyInfo>();
        this.traveler().depthFirstTravel(
                this,
                new GraphTravelAction<ClassInfo<?>>() {
                    @Override
                    public void preTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                        PropertyInfo propertyInfo = 
                            ctx.getNode().getDeclaredResolvedProperty(
                                    name, resolvedParameterTypes);
                        if (propertyInfo != null) {
                            if (propertyInfoRef.get() != null) {
                                throw new TooManyPropertyInfoException(
                                        tooManyResolvedPropertyInfo(ClassInfo.this, name, resolvedParameterTypes)
                                );
                            }
                            propertyInfoRef.set(propertyInfo);
                            ctx.stopTravelNeighborNodes();
                        }
                    }
                    @Override
                    public void postTravelNeighborNodes(GraphTravelContext<ClassInfo<?>> ctx) {
                        
                    }
                });
        return propertyInfoRef.get();
    }

    public PropertyInfo getResolvedProperty(
            String name, List<Class<?>> resolvedParameterTypes) {
        PropertyInfo propertyInfo = this.tryGetResolvedProperty(
                name, 
                resolvedParameterTypes);
        if (propertyInfo == null) {
            throw new NoSuchPropertyInfoException(noSuchResolvedPropertyInfo(this, name, resolvedParameterTypes));
        }
        return propertyInfo;
    }

    public PropertyInfo getResolvedProperty(
            String name, Class<?> ... resolvedParameterTypes) {
        return this.getResolvedProperty(
                name, MACollections.wrap(resolvedParameterTypes));
    }

    public ClassInfo<?> getDeclaredNestedClass(String simpleName, Type ... actualTypeArguments) {
        for (Class<?> nestedClass : this.getRawClass().getDeclaredClasses()) {
            if (nestedClass.getSimpleName().equals(simpleName)) {
                return of(this, nestedClass, actualTypeArguments);
            }
        }
        throw new NoSuchClassInfoException(noSuchDeclaredNestedClassInfo(this, simpleName));
    }
    
    public ClassInfo<?> getNestedClass(String simpleName, Type ... actualTypeArguments) {
        for (Class<?> nestedClass : this.getRawClass().getClasses()) {
            if (nestedClass.getSimpleName().equals(simpleName)) {
                return of(this, nestedClass, actualTypeArguments);
            }
        }
        throw new NoSuchClassInfoException(noSuchNestedClassInfo(this, simpleName));
    }
    
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return this.getRawClass().getDeclaredAnnotations();
    }

    @Override
    public Annotation[] getAnnotations() {
        return this.getRawClass().getAnnotations();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return this.getRawClass().getAnnotation(annotationClass);
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            hash = this.rawType.hashCode();
            if (hash == 0) {
                hash = 835723543;
            }
            this.hash = hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClassInfo<?>)) {
            return false;
        }
        ClassInfo<?> other = (ClassInfo<?>)obj;
        return this.rawType.equals(other.rawType);
    }
    
    public static boolean isNonStaticMemberClass(Class<?> rawClass) {
        return
            rawClass.isMemberClass() &&
            !rawClass.isInterface() &&
            !rawClass.isEnum() &&
            !rawClass.isAnnotation() &&
            !java.lang.reflect.Modifier.isStatic(rawClass.getModifiers());
    }
    
    @Override
    public String toString() {
        return this.getRawClass().getName();
    }
    
    public String toGenericString() {
        if (this.rawType instanceof Class<?>) {
            return this.toString();
        }
        return toGenericString(this.getRawClass());
    }
    
    public String toResolvedString() {
        if (this.rawType instanceof Class<?>) {
            return this.toString();
        }
        return toResolvedString((ParameterizedType)this.rawType);
    }
    
    public String toResolvedGenericString() {
        return toString(this.rawType);
    }
    
    public static String toString(Type type) {
        if (type == null) {
            return "null";
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType)type;
            Type[] lowerBounds = wildcardType.getLowerBounds();
            Type[] upperBounds = wildcardType.getUpperBounds();
            Type[] bounds = lowerBounds;
            StringBuilder localStringBuilder = new StringBuilder();

            if (lowerBounds.length != 0) {
                localStringBuilder.append("? super ");
            } else {
                if ((upperBounds.length > 0) && (!(upperBounds[0].equals(Object.class)))) {
                    bounds = upperBounds;
                    localStringBuilder.append("? extends ");
                } else {
                    return "?";
                }
            }
            assert bounds.length > 0;
            int len = bounds.length; 
            for (int i = 0; i < len; i++) { 
                if (i != 0) {
                    localStringBuilder.append(" & ");
                }
                localStringBuilder.append(toString(bounds[i]));
            }
            return ((String)localStringBuilder.toString());     
        }
        if (type instanceof TypeVariable<?>) {
            return ((TypeVariable<?>)type).getName();
        }
        if (type instanceof GenericArrayType) {
            return toString(((GenericArrayType)type).getGenericComponentType()) + "[]";
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            Type ownerType = parameterizedType.getOwnerType();
            String prefix;
            if (ownerType != null) {
                prefix = 
                    toString(ownerType) + 
                    '.' + 
                    ((Class<?>)parameterizedType.getRawType()).getSimpleName();
            } else {
                prefix = toString(parameterizedType.getRawType());
            }
            StringBuilder builder = new StringBuilder();
            boolean addComma = false;
            for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                if (addComma) {
                    builder.append(", ");
                } else {
                    addComma = true;
                }
                builder.append(toString(actualTypeArgument));
            }
            return prefix + '<' + builder.toString() + '>';
        }
        Class<?> clazz = (Class<?>)type;
        if (clazz.isArray()) {
            return toString(clazz.getComponentType()) + "[]";
        }
        return clazz.getName();
    }
    
    public static boolean isGenericClass(Class<?> rawClass) {
        if (rawClass.getTypeParameters().length != 0) {
            return true;
        }
        if (isNonStaticMemberClass(rawClass)) {
            return isGenericClass(rawClass.getDeclaringClass());
        }
        return false;
    }
    
    public static Class<?> box(Class<?> primitiveType) {
        return box(primitiveType, false);
    }
    
    public static Class<?> unbox(Class<?> primitiveBoxType) {
        return unbox(primitiveBoxType, false);
    }
    
    public static Class<?> box(Class<?> primitiveType, boolean throwException) {
        if (primitiveType == null) {
            if (throwException) {
                Arguments.mustNotBeNull("primitvieType", primitiveType);
            }
        } else if (!primitiveType.isPrimitive()) {
            if (throwException) {
                Arguments.mustBePrimitive("primitiveType", primitiveType);
            }
        } else {
            primitiveType = PRIMITIVE_BOX_MAP.get(primitiveType);
        }
        return primitiveType;
    }
    
    public static Class<?> unbox(Class<?> boxType, boolean throwException) {
        if (boxType == null) {
            if (throwException) {
                Arguments.mustNotBeNull("primitiveBoxType", boxType);
            }
        } else {
            Class<?> newType = BOX_PRIMITIVE_MAP.get(boxType);
            if (newType != null) {
                boxType = newType;
            } else if (throwException) {
                //Arguments.mustBeBox("boxType", boxType);
                throw new AssertionError("TODO:");
            }
        }
        return boxType;
    }
    
    private GraphTraveler<ClassInfo<?>> traveler() {
        return this.isInterface() ? INTERFACE_TRAVELER : CLASS_TRAVELER;
    }

    private void validate() {
        if (this.isGenericClass()) {
            if (this.rawType instanceof Class<?>) {
                throw new AssertionError("internal bug: \"raw type must be parameterizedType.\"");
            }
        } else {
            if (this.rawType instanceof ParameterizedType) {
                throw new AssertionError("internal bug: \"raw type must be class.\"");
            }
        }
        Class<?> rawClass = this.getRawClass();
        if (rawClass.getDeclaringClass() != null) {
            if (this.declaringClass == null) {
                throw new AssertionError("internal bug: \"declaringClass must not be null.\"");
            }
            Type declaringType = this.declaringClass.rawType;
            Class<?> declaringRawClass =
                declaringType instanceof ParameterizedType ?
                (Class<?>)((ParameterizedType)declaringType).getRawType() :
                (Class<?>)declaringType;
            if (declaringRawClass != rawClass.getDeclaringClass()) {
                throw new AssertionError(
                        "internal bug:\"" +
                        declaringRawClass.getName() +
                        "\" is not the declaring class of \"" +
                        rawClass.getName() +
                        "\"");
            }
        } else {
            if (this.declaringClass != null) {
                throw new AssertionError("internal bug: \"declaringClass must be null.\"");
            }
        }
    }

    private void addFields(LinkedHashMap<String, FieldInfo> map) {
        for (ClassInfo<?> interface_ : this.interfaces) {
            interface_.addFields(map);
        }
        if (this.superClass != null) {
            this.superClass.addFields(map);
        }
        for (FieldInfo field : this.getDeclaredFields()) {
            if (field.getModifiers().isPublic()) {
                map.put(field.getName(), field);
            }
        }
    }
    
    private static Integer compare(MethodInfo method1, MethodInfo method2) {
        Integer cmp = compare(method1.getReturnType(), method2.getReturnType());
        if (cmp == null) {
            return null;
        }
        List<Class<?>> parameterTypes1 = method1.getParameterTypes();
        List<Class<?>> parameterTypes2 = method2.getParameterTypes();
        for (int i = parameterTypes1.size() - 1; i >= 0; i--) {
            Integer newCmp = compare(parameterTypes1.get(i), parameterTypes2.get(i));
            if (cmp < 0 && newCmp > 0) {
                return null;
            }
            if (cmp > 0 && newCmp < 0) {
                return null;
            }
            if (cmp == 0) {
                cmp = newCmp;
            }
        }
        return cmp;
    }
    
    private static Integer compare(PropertyInfo property1, PropertyInfo property2) {
        Integer cmp = compare(property1.getReturnType(), property2.getReturnType());
        if (cmp == null) {
            return null;
        }
        List<Class<?>> parameterTypes1 = property1.getParameterTypes();
        List<Class<?>> parameterTypes2 = property2.getParameterTypes();
        for (int i = parameterTypes1.size() - 1; i >= 0; i--) {
            Integer newCmp = compare(parameterTypes1.get(i), parameterTypes2.get(i));
            if (cmp < 0 && newCmp > 0) {
                return null;
            }
            if (cmp > 0 && newCmp < 0) {
                return null;
            }
            if (cmp == 0) {
                cmp = newCmp;
            }
        }
        return cmp;
    }
    
    private static Integer compare(ConstructorInfo<?> constructor1, ConstructorInfo<?> constructor2) {
        Integer cmp = null;
        List<Class<?>> parameterTypes1 = constructor1.getParameterTypes();
        List<Class<?>> parameterTypes2 = constructor2.getParameterTypes();
        for (int i = parameterTypes1.size() - 1; i >= 0; i--) {
            Integer newCmp = compare(parameterTypes1.get(i), parameterTypes2.get(i));
            if (cmp < 0 && newCmp > 0) {
                return null;
            }
            if (cmp > 0 && newCmp < 0) {
                return null;
            }
            if (cmp == 0) {
                cmp = newCmp;
            }
        }
        return cmp;
    }
    
    private static Integer compare(Class<?> type1, Class<?> type2) {
        if (type1 == type2) {
            return 0;
        }
        if (type1.isAssignableFrom(type2)) {
            return -1;
        }
        if (type2.isAssignableFrom(type1)) {
            return +1;
        }
        return null;
    }
    
    private static String toGenericString(Class<?> rawClass) {
        StringBuilder builder = new StringBuilder();
        TypeVariable<?>[] typeParameters = rawClass.getTypeParameters();
        if (typeParameters != null) {
            builder.append('<');
            boolean addComma = false;
            for (TypeVariable<?> typeParameter : typeParameters) {
                if (addComma) {
                    builder.append(", ");
                } else {
                    addComma = true;
                }
                builder.append(typeParameter.getName());
            }
            builder.append('>');
        }
        if (isNonStaticMemberClass(rawClass)) {
            return 
                toGenericString(rawClass.getDeclaringClass()) + 
                '.' +
                rawClass.getSimpleName() +
                builder.toString();
        }
        return rawClass.getName() + builder.toString();
    }

    private static String toResolvedString(Type type) {
        if (type instanceof Class<?>) {
            return ((Class<?>)type).getName();
        }
        ParameterizedType parameterizedType = (ParameterizedType)type;
        Type ownerType = parameterizedType.getOwnerType();
        Class<?> rawClass = (Class<?>)parameterizedType.getRawType();
        StringBuilder builder = new StringBuilder();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length != 0) {
            builder.append('<');
            boolean addComma = false;
            for (Type actualTypeArgument : actualTypeArguments) {
                if (addComma) {
                    builder.append(", ");
                } else {
                    addComma = true;
                }
                builder.append(toString(GenericTypes.eraseGenericType(actualTypeArgument)));
            }
            builder.append('>');
        }
        if (ownerType != null) {
            return toResolvedString(ownerType) + '.' + rawClass.getSimpleName() + builder.toString(); 
        }
        return rawClass.getName() + builder.toString();
    }
    
    @I18N
    private static native String descriptorMustEndWithSemi(String internalName);

    @I18N
    private static native String noSuchDeclaredFieldInfo(ClassInfo<?> classInfo, String name);
    
    @I18N
    private static native String noSuchFieldInfo(ClassInfo<?> classInfo, String name);

    @I18N
    private static native String noSuchDeclaredErasedMethodInfo(ClassInfo<?> classInfo, String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String noSuchDeclaredErasedMethodInfoWithReturnType(ClassInfo<?> classInfo, Class<?> returnType, String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String noSuchErasedMethodInfo(ClassInfo<?> classInfo, String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String noSuchErasedMethodInfoWithReturnType(ClassInfo<?> classInfo, Class<?> returnType,String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String noSuchDeclaredResolvedMethodInfo(ClassInfo<?> classInfo, String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String noSuchDeclaredResolvedMethodInfoWithReturnType(ClassInfo<?> classInfo, Class<?> returnType,String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String noSuchResolvedMethodInfo(ClassInfo<?> classInfo, String name, List<Class<?>> parameterTypes);
    
    @I18N
    private static native String noSuchResolvedMethodInfoWithReturnType(ClassInfo<?> classInfo, Class<?> returnType, String name, List<Class<?>> parameterTypes);
    
    @I18N
    private static native String tooManyDeclaredResolvedMethodInfo(ClassInfo<?> classInfo, String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String tooManyDeclaredResolvedMethodInfoWithReturnType(ClassInfo<?> classInfo, Class<?> returnType, String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String tooManyResolvedMethodInfo(ClassInfo<?> classInfo, String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String tooManyResolvedMethodInfoWithReturnType(ClassInfo<?> classInfo, Class<?> returnType, String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String noSuchDeclaredErasedConstructorInfo(ClassInfo<?> classInfo, List<Class<?>> parameterTypes);
            
    @I18N
    private static native String noSuchDeclaredResolvedConstructorInfo(ClassInfo<?> classInfo, List<Class<?>> parameterTypes);
    
    @I18N
    private static native String tooManyDeclaredResolvedConstructorInfo(ClassInfo<?> classInfo, List<Class<?>> parameterTypes);
            
    @I18N
    private static native String noSuchDeclaredErasedPropertyInfo(ClassInfo<?> classInfo, String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String noSuchErasedPropertyInfo(ClassInfo<?> classInfo, String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String noSuchDeclaredResolvedPropertyInfo(ClassInfo<?> classInfo, String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String noSuchResolvedPropertyInfo(ClassInfo<?> classInfo, String name, List<Class<?>> parameterTypes);
    
    @I18N
    private static native String tooManyDeclaredResolvedPropertyInfo(ClassInfo<?> classInfo, String name, List<Class<?>> parameterTypes);

    @I18N
    private static native String tooManyResolvedPropertyInfo(ClassInfo<?> classInfo, String name, List<Class<?>> parameterTypes);
    
    @I18N
    private static native String noSuchDeclaredNestedClassInfo(ClassInfo<?> classInfo, String name);
    
    @I18N
    private static native String noSuchNestedClassInfo(ClassInfo<?> classInfo, String name);

    static {
        Map<Class<?>, Class<?>> primitiveBoxMap = new HashMap<>(12);
        Map<Class<?>, Class<?>> boxPrimitvieMap = new HashMap<>(12);
        primitiveBoxMap.put(boolean.class, Boolean.class);
        primitiveBoxMap.put(char.class, Character.class);
        primitiveBoxMap.put(byte.class, Byte.class);
        primitiveBoxMap.put(short.class, Short.class);
        primitiveBoxMap.put(int.class, Integer.class);
        primitiveBoxMap.put(long.class, Long.class);
        primitiveBoxMap.put(float.class, Float.class);
        primitiveBoxMap.put(double.class, Double.class);
        for (Entry<Class<?>, Class<?>> entry : primitiveBoxMap.entrySet()) {
            boxPrimitvieMap.put(entry.getValue(), entry.getKey());
        }
        PRIMITIVE_BOX_MAP = primitiveBoxMap;
        BOX_PRIMITIVE_MAP = boxPrimitvieMap;
    }
    
}
