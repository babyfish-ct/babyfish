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
package org.babyfish.hibernate.jpa.boot.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitInfo;

import org.babyfish.hibernate.internal.XSessionFactoryImplementor;
import org.babyfish.hibernate.jpa.internal.XEntityManagerFactoryImpl;
import org.babyfish.lang.UncheckedException;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.tree.AbstractInsnNode;
import org.babyfish.org.objectweb.asm.tree.InsnList;
import org.babyfish.org.objectweb.asm.tree.MethodInsnNode;
import org.babyfish.org.objectweb.asm.tree.TypeInsnNode;
import org.babyfish.util.reflect.runtime.ASM;
import org.babyfish.util.reflect.runtime.ClassEnhancer;
import org.hibernate.MappingException;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.boot.registry.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.hibernate.jpa.boot.internal.SettingsImpl;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.hibernate.jpa.internal.EntityManagerFactoryImpl;
import org.hibernate.jpa.internal.schemagen.JpaSchemaGenerator;
import org.hibernate.service.ServiceRegistry;

/**
 * @author Tao Chen
 */
public abstract class XEntityManagerFactoryBuilderImpl extends EntityManagerFactoryBuilderImpl {
    
    private static final Constructor<XEntityManagerFactoryBuilderImpl> CONSTRUCTOR;
    
    private static final Field SETTINGS_FIELD;
    
    private static final Field HIBERNATE_CONFIGURATION_FIELD;
    
    private static final Field SUPPLIED_SESSION_FACTORY_OBSERVER_FIELD;
    
    private PersistenceUnitDescriptor persistenceUnit;
    
    public static XEntityManagerFactoryBuilderImpl of(
            PersistenceUnitDescriptor persistenceUnit, 
            Map<?, ?> integrationSettings,
            ClassLoader providedClassLoader) {
        try {
            return CONSTRUCTOR.newInstance(persistenceUnit, integrationSettings, providedClassLoader);
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        } catch (InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex.getTargetException());
        }
    }
    
    public static XEntityManagerFactoryBuilderImpl of(
            PersistenceUnitDescriptor persistenceUnit, 
            Map<?, ?> integrationSettings) {
        return of(persistenceUnit, integrationSettings, null);
    }
    
    public static XEntityManagerFactoryBuilderImpl of(
            PersistenceUnitInfo persistenceUnitInfo, 
            Map<?, ?> integrationSettings,
            ClassLoader providedClassLoader) {
        return of(
                new PersistenceUnitInfoDescriptor(persistenceUnitInfo), 
                integrationSettings, 
                providedClassLoader);
    }
    
    public static XEntityManagerFactoryBuilderImpl of(
            PersistenceUnitInfo persistenceUnitInfo, 
            Map<?, ?> integrationSettings) {
        return of(
                new PersistenceUnitInfoDescriptor(persistenceUnitInfo), 
                integrationSettings);
    }

    protected XEntityManagerFactoryBuilderImpl(
            PersistenceUnitDescriptor persistenceUnit, 
            Map<?, ?> integrationSettings,
            ClassLoader providedClassLoader) {
        super(persistenceUnit, integrationSettings, providedClassLoader);
        this.persistenceUnit = persistenceUnit;
    }
    
    class GenerateSchemaWork implements ClassLoaderServiceImpl.Work<Void> {
        
        private ServiceRegistry serviceRegistry;
        
        public GenerateSchemaWork(ServiceRegistry serviceRegistry) {
            this.serviceRegistry = serviceRegistry;
        }
    
        @Override
        public Void perform() {
            final org.hibernate.cfg.Configuration hibernateConfiguration = 
                    XEntityManagerFactoryBuilderImpl.this.buildHibernateConfiguration(
                            this.serviceRegistry);
            try {
                hibernateConfiguration.buildSessionFactory(this.serviceRegistry);
            }
            catch (MappingException ex) {
                throw XEntityManagerFactoryBuilderImpl.this.persistenceException(
                        "Unable to build Hibernate SessionFactory", 
                        ex
                );
            }
            JpaSchemaGenerator.performGeneration( hibernateConfiguration, this.serviceRegistry);
            return null;
        }
    }

    class BuildWork implements ClassLoaderServiceImpl.Work<XEntityManagerFactoryImpl> {
        
        private ServiceRegistry serviceRegistry;
        
        public BuildWork(ServiceRegistry serviceRegistry) {
            this.serviceRegistry = serviceRegistry;
        }
        
        @Override
        public XEntityManagerFactoryImpl perform() {
            final org.babyfish.hibernate.cfg.Configuration cfg = 
                    (org.babyfish.hibernate.cfg.Configuration)buildHibernateConfiguration(this.serviceRegistry);
            XEntityManagerFactoryBuilderImpl.this.setHibernateConfiguration(cfg);
            XSessionFactoryImplementor sessionFactory;
            try {
                sessionFactory = (XSessionFactoryImplementor)cfg.buildSessionFactory(this.serviceRegistry);
            }
            catch (MappingException ex) {
                throw persistenceException(
                        "Unable to build Hibernate SessionFactory", 
                        ex
                );
            }
            
            JpaSchemaGenerator.performGeneration(cfg, serviceRegistry);
            
            SessionFactoryObserver suppliedSessionFactoryObserver = 
                    XEntityManagerFactoryBuilderImpl.this.getSuppliedSessionFactoryObserver();
            if (suppliedSessionFactoryObserver != null) {
                sessionFactory.addObserver(suppliedSessionFactoryObserver);
            }
            sessionFactory.addObserver( new ServiceRegistryCloser() );
    
            // NOTE : passing cfg is temporary until
            return new XEntityManagerFactoryImpl(
                    XEntityManagerFactoryBuilderImpl.this.persistenceUnit.getName(),
                    sessionFactory,
                    XEntityManagerFactoryBuilderImpl.this.getSettings(),
                    XEntityManagerFactoryBuilderImpl.this.getConfigurationValues(),
                    (org.babyfish.hibernate.cfg.Configuration)XEntityManagerFactoryBuilderImpl.this.getHibernateConfiguration()
            );
        }
    }

    private static class Enhancer extends ClassEnhancer {
        
        private static final Enhancer INSTANCE = getInstance(Enhancer.class);
        
        private Enhancer() {
            super(XEntityManagerFactoryBuilderImpl.class);
        }
        
        public static Class<XEntityManagerFactoryBuilderImpl> getEnhancedClass() {
            return INSTANCE.getResultClass();
        }

        @Override
        protected void doMethodFilter(MethodSource methodSource) {
            InsnList instructions = methodSource.getInstructions();
            for (AbstractInsnNode abstractInsnNode = instructions.getFirst();
                    abstractInsnNode != null;
                    abstractInsnNode = abstractInsnNode.getNext()) {
                if (abstractInsnNode.getOpcode() == Opcodes.NEW) {
                    TypeInsnNode typeInsnNode = (TypeInsnNode)abstractInsnNode;
                    if (typeInsnNode.desc.equals(ASM.getInternalName(EntityManagerFactoryImpl.class))) {
                        typeInsnNode.desc = ASM.getInternalName(XEntityManagerFactoryImpl.class);
                    } else if (typeInsnNode.desc.equals(ASM.getInternalName(org.hibernate.cfg.Configuration.class))) {
                        typeInsnNode.desc = ASM.getInternalName(org.babyfish.hibernate.cfg.Configuration.class);
                    }
                } else if (abstractInsnNode.getOpcode() == Opcodes.INVOKESPECIAL) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;
                    if (methodInsnNode.name.equals("<init>")) {
                        if (methodInsnNode.owner.equals(ASM.getInternalName(EntityManagerFactoryImpl.class))) {
                            methodInsnNode.owner = ASM.getInternalName(XEntityManagerFactoryImpl.class);
                        } else if (methodInsnNode.owner.equals(ASM.getInternalName(org.hibernate.cfg.Configuration.class))) {
                            methodInsnNode.owner = ASM.getInternalName(org.babyfish.hibernate.cfg.Configuration.class);
                        }
                    }
                } else if (abstractInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;
                    if (methodInsnNode.name.equals("withTccl") && 
                            methodInsnNode.owner.equals(ASM.getInternalName(ClassLoaderServiceImpl.class))) {
                        MethodInsnNode initWorkInsnNode = (MethodInsnNode)methodInsnNode.getPrevious();
                        String oldOwnerName = initWorkInsnNode.owner;
                        initWorkInsnNode.owner = 
                                methodSource.getMethod().getName().equals("generateSchema") ?
                                ASM.getInternalName(GenerateSchemaWork.class) :
                                ASM.getInternalName(BuildWork.class);
                        initWorkInsnNode.desc = initWorkInsnNode.desc.replace(
                                ASM.getDescriptor(EntityManagerFactoryBuilderImpl.class), 
                                ASM.getDescriptor(XEntityManagerFactoryBuilderImpl.class));
                        AbstractInsnNode tmpInsnNode = initWorkInsnNode.getPrevious();
                        while (true) {
                            if (tmpInsnNode.getOpcode() == Opcodes.NEW) {
                                TypeInsnNode typeInsnNode = (TypeInsnNode)tmpInsnNode;
                                if (typeInsnNode.desc.equals(oldOwnerName)) {
                                    typeInsnNode.desc = 
                                            methodSource.getMethod().getName().equals("generateSchema") ?
                                            ASM.getInternalName(GenerateSchemaWork.class) :
                                            ASM.getInternalName(BuildWork.class);
                                    break;
                                }
                            }
                            tmpInsnNode = tmpInsnNode.getPrevious();
                        }
                    }
                }
            }
        }
    }
    
    protected PersistenceException persistenceException(String message, Exception cause) {
        return new PersistenceException(
                "[PersistenceUnit: " + persistenceUnit.getName() + "] " + message,
                cause
        );
    }
    
    protected PersistenceUnitDescriptor persistenceUnitInfo() {
        return this.persistenceUnit;
    }
    
    private SettingsImpl getSettings() {
        try {
            return (SettingsImpl)SETTINGS_FIELD.get(this);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        }
    }
    
    private SessionFactoryObserver getSuppliedSessionFactoryObserver() {
        try {
            return (SessionFactoryObserver)SUPPLIED_SESSION_FACTORY_OBSERVER_FIELD.get(this);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        }
    }
    
    private void setHibernateConfiguration(org.babyfish.hibernate.cfg.Configuration cfg) {
        try {
            HIBERNATE_CONFIGURATION_FIELD.set(this, cfg);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        }
    }

    static {
        Constructor<XEntityManagerFactoryBuilderImpl> constructor;
        try {
            constructor = Enhancer.getEnhancedClass().getDeclaredConstructor(
                    PersistenceUnitDescriptor.class, Map.class, ClassLoader.class);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        constructor.setAccessible(true);
        
        Field settingsField;
        try {
            settingsField = EntityManagerFactoryBuilderImpl.class.getDeclaredField("settings");
        } catch (NoSuchFieldException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        settingsField.setAccessible(true);

        Field hibernateConfigurationField;
        try {
            hibernateConfigurationField = EntityManagerFactoryBuilderImpl.class.getDeclaredField("hibernateConfiguration");
        } catch (NoSuchFieldException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        hibernateConfigurationField.setAccessible(true);
        
        Field suppliedSessionFactoryObserverField;
        try {
            suppliedSessionFactoryObserverField = EntityManagerFactoryBuilderImpl.class.getDeclaredField("suppliedSessionFactoryObserver");
        } catch (NoSuchFieldException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        suppliedSessionFactoryObserverField.setAccessible(true);
        
        CONSTRUCTOR = constructor;
        SETTINGS_FIELD = settingsField;
        HIBERNATE_CONFIGURATION_FIELD = hibernateConfigurationField;
        SUPPLIED_SESSION_FACTORY_OBSERVER_FIELD = suppliedSessionFactoryObserverField;
    }
}
