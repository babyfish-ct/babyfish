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
package org.babyfish.hibernate.jpa;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.hibernate.ejb.PersistenceUnitReader;
import org.babyfish.hibernate.jpa.boot.internal.XEntityManagerFactoryBuilderImpl;
import org.babyfish.lang.UncheckedException;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.tree.AbstractInsnNode;
import org.babyfish.org.objectweb.asm.tree.FieldInsnNode;
import org.babyfish.org.objectweb.asm.tree.InsnList;
import org.babyfish.org.objectweb.asm.tree.MethodInsnNode;
import org.babyfish.org.objectweb.asm.tree.TypeInsnNode;
import org.babyfish.org.objectweb.asm.tree.VarInsnNode;
import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfish.util.reflect.runtime.ASM;
import org.babyfish.util.reflect.runtime.ClassEnhancer;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.jpa.boot.spi.Bootstrap;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.hibernate.jpa.boot.spi.ProviderChecker;


/**
 * @author Tao Chen
 */
public class HibernatePersistenceProvider implements PersistenceProvider {
    
    private static final String DEFAULT_PERSISTENCE_XML = "META-INF/persistence.xml";
            
    private PersistenceProvider rawProvider;
    
    private ReadWriteLock rawProviderLock = new ReentrantReadWriteLock();
    
    private String[] persistenceXmlLocations;
    
    public HibernatePersistenceProvider() {
        this((String[])null);
    }
    
    public HibernatePersistenceProvider(String ... persistenceXmlLocations) {
        this.persistenceXmlLocations = persistenceXmlLocations != null ? persistenceXmlLocations.clone() : null;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public XEntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {
        return (XEntityManagerFactory)this.getRawProvider().createEntityManagerFactory(persistenceUnitName, properties);
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public XEntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties) {
        return (XEntityManagerFactory)XEntityManagerFactoryBuilderImpl.of(info, properties).build();
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void generateSchema(PersistenceUnitInfo info, Map properties) {
        XEntityManagerFactoryBuilderImpl.of(info, properties).generateSchema();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean generateSchema(String persistenceUnitName, Map map) {
        return this.getRawProvider().generateSchema(persistenceUnitName, map);
    }

    public ProviderUtil getProviderUtil() {
        return this.getRawProvider().getProviderUtil();
    }
    
    public static boolean isProvider(PersistenceUnitDescriptor persistenceUnit, Map<?, ?> integration) {
        if (ProviderChecker.isProvider(persistenceUnit, integration)) {
            return true;
        }
        String providerClassName = ProviderChecker.extractRequestedProviderName(persistenceUnit, integration);
        return HibernatePersistenceProvider.class.getName().equals(providerClassName);
    }
    
    protected PersistenceProvider getRawProvider() {
        Lock lock;
        PersistenceProvider rawProvider;
        
        (lock = this.rawProviderLock.readLock()).lock(); //1st locking
        try {
            rawProvider = this.rawProvider; //1st reading
        } finally {
            lock.unlock();
        }
        
        if (rawProvider == null) { //1st checking
            (lock = this.rawProviderLock.writeLock()).lock(); //2nd locking
            try {
                rawProvider = this.rawProvider; //2nd reading
                if (rawProvider == null) {
                    this.rawProvider = rawProvider = this.createRawProvider();
                }
            } finally {
                lock.unlock();
            }
        }
        
        return rawProvider;
    }
        
    private PersistenceProvider createRawProvider() {
        XOrderedMap<String, PersistenceUnitInfo> infos;
        String[] persistenceXmlLocations = this.persistenceXmlLocations;
        if (persistenceXmlLocations == null || persistenceXmlLocations.length == 0) {
            infos = new PersistenceUnitReader().read(new String[] { DEFAULT_PERSISTENCE_XML });
        } else {
            infos = new PersistenceUnitReader().read(persistenceXmlLocations);
        }
        List<PersistenceUnitInfoDescriptor> descriptors = new ArrayList<>(infos.size());
        for (PersistenceUnitInfo info : infos.values()) {
            descriptors.add(new PersistenceUnitInfoDescriptor(info));
        }
        return Enhancer.newInstance(descriptors);
    }
    
    @SuppressWarnings("unused")
    private static class RawProvider extends org.hibernate.jpa.HibernatePersistenceProvider {
        
        protected List<PersistenceUnitInfoDescriptor> descriptors;
        
        protected RawProvider(List<PersistenceUnitInfoDescriptor> descriptors) {
            this.descriptors = descriptors;
        }
    }

    private static class Enhancer extends ClassEnhancer {

        private static final Enhancer INSTANCE = getInstance(Enhancer.class);
        
        private static final Constructor<?> CONSTRUCTOR;

        protected Enhancer() {
            super(RawProvider.class);
        }
        
        public static PersistenceProvider newInstance(List<PersistenceUnitInfoDescriptor> descriptors) {
            try {
                return (PersistenceProvider)CONSTRUCTOR.newInstance(descriptors);
            } catch (InstantiationException ex) {
                throw new AssertionError("Internal bug", ex);
            } catch (IllegalAccessException ex) {
                throw new AssertionError("Internal bug", ex);
            } catch (InvocationTargetException ex) {
                throw UncheckedException.rethrow(ex.getTargetException());
            }
        }
        
        @Override
        protected boolean usedEnhancerLoader() {
            return true;
        }

        @Override
        protected void doMethodFilter(MethodSource methodSource) {
            InsnList instructions = methodSource.getInstructions();
            for (AbstractInsnNode abstractInsnNode = instructions.getFirst();
                    abstractInsnNode != null;
                    abstractInsnNode = abstractInsnNode.getNext()) {
                if (abstractInsnNode.getOpcode() == Opcodes.INVOKESTATIC) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;
                    if (methodInsnNode.name.equals("getEntityManagerFactoryBuilder") &&
                            methodInsnNode.owner.equals(ASM.getInternalName(Bootstrap.class))) {
                        methodInsnNode.owner = ASM.getInternalName(XEntityManagerFactoryBuilderImpl.class);
                        methodInsnNode.name = "of";
                        methodInsnNode.desc = 
                                methodInsnNode.desc.substring(0, methodInsnNode.desc.lastIndexOf(')') + 1) + 
                                ASM.getDescriptor(XEntityManagerFactoryBuilderImpl.class);
                    } else if (methodInsnNode.name.equals("locatePersistenceUnits") &&
                            methodInsnNode.owner.equals(ASM.getInternalName(PersistenceXmlParser.class))) {
                        AbstractInsnNode nextInsnNode = abstractInsnNode.getNext();
                        instructions.remove(methodInsnNode.getPrevious());
                        instructions.remove(methodInsnNode);
                        InsnList tmpInstructions = new InsnList();
                        tmpInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        tmpInstructions.add(
                                new FieldInsnNode(
                                        Opcodes.GETFIELD, 
                                        ASM.getInternalName(RawProvider.class), 
                                        "descriptors", 
                                        ASM.getDescriptor(List.class)
                                )
                        );
                        abstractInsnNode = tmpInstructions.getLast();
                        instructions.insertBefore(nextInsnNode, tmpInstructions);
                    } else if (methodInsnNode.name.equals("isProvider") &&
                            methodInsnNode.owner.equals(ASM.getInternalName(ProviderChecker.class))) {
                        methodInsnNode.owner = ASM.getInternalName(HibernatePersistenceProvider.class);
                    }
                } else if (abstractInsnNode.getOpcode() == Opcodes.CHECKCAST) {
                    TypeInsnNode typeInsnNode = (TypeInsnNode)abstractInsnNode;
                    if (typeInsnNode.desc.equals(ASM.getInternalName(ParsedPersistenceXmlDescriptor.class))) {
                        typeInsnNode.desc = ASM.getInternalName(PersistenceUnitInfoDescriptor.class);
                    }
                } else if (abstractInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;
                    if (methodInsnNode.owner.equals(ASM.getInternalName(ParsedPersistenceXmlDescriptor.class))) {
                        methodInsnNode.owner = ASM.getInternalName(PersistenceUnitInfoDescriptor.class);
                    }
                }
            }
        }
        
        static {
            Constructor<?> constructor;
            try {
                constructor = INSTANCE.getResultClass().getDeclaredConstructor(List.class);
            } catch (NoSuchMethodException ex) {
                throw new AssertionError("Internal bug", ex);
            }
            constructor.setAccessible(true);
            CONSTRUCTOR = constructor;
        }
    }
}
