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
package org.babyfish.hibernate.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.TreeMap;
import org.babyfish.hibernate.XSession;
import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.context.internal.JTAXSessionContext;
import org.babyfish.hibernate.context.internal.ManagedXSessionContext;
import org.babyfish.hibernate.context.internal.ThreadLocalXSessionContext;
import org.babyfish.hibernate.context.spi.CurrentXSessionContext;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.SharedSessionBuilder;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.hibernate.engine.transaction.spi.TransactionFactory;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.util.reflect.MethodDescriptor;
import org.babyfish.util.reflect.MethodImplementation;
import org.babyfish.util.reflect.runtime.ASM;
import org.babyfish.util.reflect.runtime.ClassWrapper;
import org.babyfish.util.reflect.runtime.XMethodVisitor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

/**
 * @author Tao Chen
 */
public final class SessionFactoryImplWrapper extends ClassWrapper {
    
    private static final CoreMessageLogger LOG = Logger.getMessageLogger(
            CoreMessageLogger.class, 
            SessionFactoryImplWrapper.class.getName());
    
    private static final SessionFactoryImplWrapper INSTANCE = getInstance(SessionFactoryImplWrapper.class);
    
    static final Object IDK_HBM_REFERENCES_BY_TARGET_CLASS = new Object();

    private SessionFactoryImplWrapper() {
        super(SessionFactoryImpl.class);
    }

    public static XSessionFactory wrap(SessionFactory sessionFactory) {
        if (sessionFactory instanceof XSessionFactory) {
            return (XSessionFactory)sessionFactory;
        }
        return (XSessionFactory)INSTANCE.createProxy(sessionFactory);
    }
    
    @Override
    protected Class<?>[] onGetInterfaceTypes() {
        return new Class[] {
                XSessionFactory.class, 
                XSessionFactoryImplementor.class
        };
    }

    @Override
    protected boolean useWrapperLoader() {
        return true;
    }

    @Override
    protected void generateMethodCode(
            XMethodVisitor mv,
            MethodImplementation methodImplementation) {
        MethodDescriptor descriptor = methodImplementation.getDescriptor();
        if (descriptor.match("getCurrentSession")) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.getResultInternalName(), 
                    "currentXSessionContext", 
                    ASM.getDescriptor(CurrentXSessionContext.class));
            Label endIfNullLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFNONNULL, endIfNullLabel);
            mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(HibernateException.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn("No CurrentSessionContext configured!");
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    ASM.getInternalName(HibernateException.class),
                    "<init>",
                    "(Ljava/lang/String;)V",
                    false);
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitLabel(endIfNullLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.getResultInternalName(), 
                    "currentXSessionContext", 
                    ASM.getDescriptor(CurrentXSessionContext.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(CurrentXSessionContext.class), 
                    "currentSession", 
                    "()" + ASM.getDescriptor(XSession.class),
                    true);
            mv.visitInsn(Opcodes.ARETURN);
        } else if (SharedSessionBuilder.class.isAssignableFrom(descriptor.getReturnType())) {
            this.generateInvokeRawMethodCode(mv, methodImplementation);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(SharedSessionBuilderWrapper.class), 
                    "wrap", 
                    '(' +
                    ASM.getDescriptor(SharedSessionBuilder.class) +
                    ASM.getDescriptor(XSessionFactory.class) +
                    ')' +
                    ASM.getDescriptor(XSharedSessionBuilderImplementor.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        } else if (SessionBuilder.class.isAssignableFrom(descriptor.getReturnType())) {
            this.generateInvokeRawMethodCode(mv, methodImplementation);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(SessionBuilderWrapper.class), 
                    "wrap", 
                    '(' +
                    ASM.getDescriptor(SessionBuilder.class) +
                    ASM.getDescriptor(XSessionFactory.class) +
                    ')' +
                    ASM.getDescriptor(XSessionBuilderImplementor.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        } else if (Session.class.isAssignableFrom(descriptor.getReturnType())) {
            this.generateInvokeRawMethodCode(mv, methodImplementation);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(SessionImplWrapper.class), 
                    "wrap", 
                    '(' +
                    ASM.getDescriptor(Session.class) + 
                    ASM.getDescriptor(XSessionFactory.class) + 
                    ')' 
                    + ASM.getDescriptor(XSession.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        } else if ("getRawSessionFactoryImpl".equals(descriptor.getName())) {
            this.generateGetRaw(mv);
            mv.visitInsn(Opcodes.ARETURN);
        } else if (descriptor.match("getInternalData", Object.class)) {
            Label isNotHbmReferencesByTargetClassLabel = new Label();
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    ASM.getInternalName(SessionFactoryImplWrapper.class), 
                    "IDK_HBM_REFERENCES_BY_TARGET_CLASS", 
                    "Ljava/lang/Object;");
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    "java/lang/Object", 
                    "equals", 
                    "(Ljava/lang/Object;)Z",
                    false);
            mv.visitJumpInsn(Opcodes.IFEQ, isNotHbmReferencesByTargetClassLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.getResultInternalName(), 
                    "hbmReferencesByTargetClass", 
                    ASM.getDescriptor(NavigableMap.class));
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(isNotHbmReferencesByTargetClassLabel);
            mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(IllegalArgumentException.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn("Unknown internal data key");
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    ASM.getInternalName(IllegalArgumentException.class), 
                    "<init>", 
                    "(Ljava/lang/String;)V",
                    false);
            mv.visitInsn(Opcodes.ATHROW);
        } else {
            super.generateMethodCode(mv, methodImplementation);
        }
    }
    
    @Override
    protected void generateInitCodeWithoutReturning(XMethodVisitor mv) {
        super.generateInitCodeWithoutReturning(mv);
        this.generateInitHbmReferencesByTargetClass(mv);
        this.generateInitCurrentXSessionContext(mv);
    }

    @Override
    protected void generateMoreMemebers(ClassVisitor cv) {
        cv
        .visitField(
                Opcodes.ACC_PRIVATE, 
                "hbmReferencesByTargetClass", 
                ASM.getDescriptor(NavigableMap.class), 
                null,
                null
        )
        .visitEnd();
        cv
        .visitField(
                Opcodes.ACC_PRIVATE, 
                "currentXSessionContext", 
                ASM.getDescriptor(CurrentXSessionContext.class), 
                null, 
                null
        )
        .visitEnd();
        this.generateWriteObject(cv);
        this.generateReadObject(cv);
    }
    
    private void generateWriteObject(ClassVisitor cv) {
        XMethodVisitor mv = ASM.visitMethod(
                cv, 
                Opcodes.ACC_PRIVATE, 
                "writeObject", 
                '(' + ASM.getDescriptor(ObjectOutputStream.class) + ")V", 
                null, 
                new String[] { ASM.getInternalName(IOException.class) });
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                ASM.getInternalName(ObjectOutputStream.class), 
                "defaultWriteObject", 
                "()V",
                false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateReadObject(ClassVisitor cv) {
        XMethodVisitor mv = ASM.visitMethod(
                cv, 
                Opcodes.ACC_PRIVATE, 
                "readObject", 
                '(' + ASM.getDescriptor(ObjectInputStream.class) + ")V", 
                null, 
                new String[] { ASM.getInternalName(IOException.class), ASM.getInternalName(ClassNotFoundException.class) });
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                ASM.getInternalName(ObjectInputStream.class), 
                "defaultReadObject", 
                "()V",
                false);
        this.generateInitHbmReferencesByTargetClass(mv);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateInitHbmReferencesByTargetClass(MethodVisitor mv) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        this.generateGetRaw(mv);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                ASM.getInternalName(SessionFactoryImplWrapper.class), 
                "createHbmReferencesByTargetClass", 
                '(' + ASM.getDescriptor(SessionFactoryImpl.class) + ')' + ASM.getDescriptor(NavigableMap.class),
                false);
        mv.visitFieldInsn(
                Opcodes.PUTFIELD, 
                this.getResultInternalName(), 
                "hbmReferencesByTargetClass", 
                ASM.getDescriptor(NavigableMap.class));
    }
    
    private void generateInitCurrentXSessionContext(MethodVisitor mv) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                ASM.getInternalName(SessionFactoryImplWrapper.class), 
                "buildCurrentXSessionContext", 
                '(' + 
                ASM.getDescriptor(XSessionFactoryImplementor.class) + 
                ')' + 
                ASM.getDescriptor(CurrentXSessionContext.class),
                false);
        mv.visitFieldInsn(
                Opcodes.PUTFIELD, 
                this.getResultInternalName(), 
                "currentXSessionContext", 
                ASM.getDescriptor(CurrentXSessionContext.class));
    }

    static NavigableMap<Class<?>, Collection<HbmReference>> createHbmReferencesByTargetClass(SessionFactoryImpl sessionFactoryImpl) {
        NavigableMap<Class<?>, Collection<HbmReference>> hbmReferencesByTargetClass =
            new TreeMap<Class<?>, Collection<HbmReference>>(
                    new Comparator<Class<?>>() {
                        @Override
                        public int compare(Class<?> o1, Class<?> o2) {
                            if (o1 == o2) {
                                return 0;
                            }
                            if (o1.isAssignableFrom(o2)) {
                                return -1;
                            }
                            if (o2.isAssignableFrom(o1)) {
                                return +1;
                            }
                            //TODO: big bug
                            return o1.hashCode() - o2.hashCode();
                        }
                    });
        Map<String, ClassMetadata> classClassMetadatas = sessionFactoryImpl.getAllClassMetadata();
        for (ClassMetadata classMetadata : classClassMetadatas.values()) {
            Type[] propertyTypes = classMetadata.getPropertyTypes(); 
            for (int i = propertyTypes.length - 1; i >= 0; i--) {
                Type propertyType = propertyTypes[i];
                if (propertyType instanceof EntityType) {
                    Class<?> targetClass = propertyType.getReturnedClass();
                    Collection<HbmReference> hbmReferences =
                        hbmReferencesByTargetClass.get(targetClass);
                    if (hbmReferences == null) {
                        hbmReferences = new ArrayList<HbmReference>();
                        hbmReferencesByTargetClass.put(targetClass, hbmReferences);
                    }
                    hbmReferences.add(new HbmReference(classMetadata, i));
                }
            }
        }
        for (Entry<?, Collection<HbmReference>> entry : hbmReferencesByTargetClass.entrySet()) {
            entry.setValue(MACollections.unmodifiable(entry.getValue()));
        }
        return MACollections.unmodifiable(hbmReferencesByTargetClass);
    }
    
    static CurrentXSessionContext buildCurrentXSessionContext(XSessionFactoryImplementor sessionFactory) {
        ServiceRegistry serviceRegistry = sessionFactory.getServiceRegistry();
        TransactionFactory<?> transactionFactory = serviceRegistry.getService(TransactionFactory.class);
        boolean canAccessTransactionManager;
        try {
            canAccessTransactionManager = serviceRegistry.getService(JtaPlatform.class).retrieveTransactionManager() != null;
        }
        catch (Exception ex) {
            LOG.warn("Cannot access transaction manager", ex);
            canAccessTransactionManager = false;
        }
        String impl = sessionFactory.getProperties().getProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS);
        if (impl == null) {
            if (canAccessTransactionManager) {
                impl = "jta";
            }
            else {
                return null;
            }
        }

        if ("jta".equals(impl)) {
            if (!transactionFactory.compatibleWithJtaSynchronization() ) {
                LOG.autoFlushWillNotWork();
            }
            return new JTAXSessionContext(sessionFactory);
        }
        else if ("thread".equals(impl)) {
            return new ThreadLocalXSessionContext(sessionFactory);
        }
        else if ("managed".equals(impl)) {
            return new ManagedXSessionContext(sessionFactory);
        }
        else {
            try {
                Class<?> implClass = serviceRegistry.getService(ClassLoaderService.class).classForName(impl);
                if (!CurrentXSessionContext.class.isAssignableFrom(implClass)) {
                    throw new HibernateException(
                            "The class specified by \"" +
                            Environment.CURRENT_SESSION_CONTEXT_CLASS +
                            "\" must be derived class of \"" +
                            CurrentXSessionContext.class.getName() +
                            "\"");
                }
                return (CurrentXSessionContext)implClass
                        .getConstructor( new Class[] { XSessionFactoryImplementor.class } )
                        .newInstance(sessionFactory);
            }
            catch (Throwable t) {
                LOG.unableToConstructCurrentSessionContext(impl, t);
                return null;
            }
        }
    }
}
