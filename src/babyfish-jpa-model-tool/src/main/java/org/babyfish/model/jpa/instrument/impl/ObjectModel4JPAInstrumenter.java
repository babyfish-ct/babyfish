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
package org.babyfish.model.jpa.instrument.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.babyfish.lang.I18N;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.UncheckedException;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.model.instrument.metadata.spi.AbstractMetadataClass;
import org.babyfish.model.instrument.metadata.spi.ClassProcessor;
import org.babyfish.model.instrument.metadata.spi.Processor;
import org.babyfish.model.instrument.metadata.spi.PropertyProcessor;
import org.babyfish.model.instrument.spi.AbstractObjectModelInstrumenter;
import org.babyfish.model.jpa.instrument.spi.AbstractObjectModel4JPAReplacer;
import org.babyfish.org.objectweb.asm.tree.ClassNode;

/**
 * @author Tao Chen
 */
public class ObjectModel4JPAInstrumenter extends AbstractObjectModelInstrumenter {
    
    private static final String CONFIGURATION_LOCATION = "META-INF/ObjectModel4JPA.properties";

    private static final Constructor<?> REPLACER_CONSTRUCTOR;
    
    @Override
    protected AbstractMetadataClass createMetadataClass(File classFile, ClassNode classNode) {
        return new JPAMetadataClassImpl(classFile, classNode);
    }

    @Override
    public AbstractObjectModel4JPAReplacer createReplacer(String className, File classFile) {
        try {
            return (AbstractObjectModel4JPAReplacer)REPLACER_CONSTRUCTOR.newInstance(this, className, classFile);
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new AssertionError("Internal bug", ex);
        } catch (InvocationTargetException ex) {
            throw UncheckedException.rethrow(ex.getTargetException());
        }
    }

    @Override
    protected Processor[] getProcessors() {
        final ObjectModel4JPAInstrumenter that = this;
        return new Processor[] {
                new PropertyProcessor<JPAMetadataPropertyImpl>() {
                    @Override
                    public void processProperty(JPAMetadataPropertyImpl p) {
                        p.resolveClass(that);
                    }
                },
                new ClassProcessor<JPAMetadataClassImpl>() {
                    @Override
                    public void processClass(JPAMetadataClassImpl c) {
                        c.resolveSuperClass(that);
                    }
                },
                new ClassProcessor<JPAMetadataClassImpl>() {
                    @Override
                    public void processClass(JPAMetadataClassImpl c) {
                        c.resolveAncestorClass();
                    }
                },
                new ClassProcessor<JPAMetadataClassImpl>() {
                    @Override
                    public void processClass(JPAMetadataClassImpl c) {
                        c.resolveProperties();
                    }
                },
                new ClassProcessor<JPAMetadataClassImpl>() {
                    @Override
                    public void processClass(JPAMetadataClassImpl c) {
                        c.resolvePropertyList();
                    }
                },
                new ClassProcessor<JPAMetadataClassImpl>() {
                    @Override
                    public void processClass(JPAMetadataClassImpl c) {
                        c.resolveIdProperty();
                    }
                },
                new ClassProcessor<JPAMetadataClassImpl>() {
                    @Override
                    public void processClass(JPAMetadataClassImpl c) {
                        c.resolveVersionProperty();
                    }
                },
                new PropertyProcessor<JPAMetadataPropertyImpl>() {
                    @Override
                    public void processProperty(JPAMetadataPropertyImpl p) {
                        p.resolveConvarianceProperty();
                    }
                },
                new PropertyProcessor<JPAMetadataPropertyImpl>() {
                    @Override
                    public void processProperty(JPAMetadataPropertyImpl p) {
                        p.calculateJoin();
                    }
                },
                new PropertyProcessor<JPAMetadataPropertyImpl>() {
                    @Override
                    public void processProperty(JPAMetadataPropertyImpl p) {
                        p.resolveExplicitBidrectionalAssociations();
                    }
                },
                new PropertyProcessor<JPAMetadataPropertyImpl>() {
                    @Override
                    public void processProperty(JPAMetadataPropertyImpl p) {
                        p.resolveImplicitBidrectionalAssociations();
                    }
                },
                new PropertyProcessor<JPAMetadataPropertyImpl>() {
                    @Override
                    public void processProperty(JPAMetadataPropertyImpl p) {
                        p.resolveIndexPropertyOfOpposite();
                    }
                },
                new PropertyProcessor<JPAMetadataPropertyImpl>() {
                    @Override
                    public void processProperty(JPAMetadataPropertyImpl p) {
                        p.resolveKeyPropertyOfOpposite();
                    }
                },
                new PropertyProcessor<JPAMetadataPropertyImpl>() {
                    @Override
                    public void processProperty(JPAMetadataPropertyImpl p) {
                        p.resolveStateAfterAssociationResolved();
                    }
                },
                new ClassProcessor<JPAMetadataClassImpl>() {
                    @Override
                    public void processClass(JPAMetadataClassImpl c) {
                        c.resolveComparatorParts();
                    }
                },
                new PropertyProcessor<JPAMetadataPropertyImpl>() {
                    @Override
                    public void processProperty(JPAMetadataPropertyImpl p) {
                        p.resolveComparatorParts(that);
                    }
                }
        };
    }
    
    static {
        Enumeration<URL> configurationFiles;
        try {
            configurationFiles = 
                    ObjectModel4JPAInstrumenter.class.getClassLoader().getResources(
                            CONFIGURATION_LOCATION
                    );
        } catch (IOException ex) {
            throw new IllegalClassException(
                    findObjectModel4JPAConfigurationFailed(CONFIGURATION_LOCATION),
                    ex
            );
        }
        URL configurationFile = null;
        while (configurationFiles.hasMoreElements()) {
            URL url = configurationFiles.nextElement();
            if (configurationFile != null) {
                throw new IllegalClassException(
                        conflicitObjectModel4JPAConfigurations(
                                configurationFile,
                                url
                        )
                );
            }
            configurationFile = url;
        }
        if (configurationFile == null) {
            throw new IllegalClassException(
                    noObjectModel4JPAConfiguration(CONFIGURATION_LOCATION)
            );
        }
        
        Properties properties = new Properties();
        try (InputStream inputStream = configurationFile.openStream()) {
            properties.load(inputStream);
        } catch (IOException ex) {
            throw new IllegalClassException(
                    readObjectModel4JPAConfigurationFailed(configurationFile),
                    ex
            );
        }
        
        String className = properties.getProperty("modelReplacer");
        if (Nulls.isNullOrEmpty(className)) {
            throw new IllegalClassException(
                    modelReplacerIsNotConfigured(
                            configurationFile,
                            "modelReplacer"
                    )
            );
        }
        
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new IllegalClassException(
                    modelReplacerClassIsNotFound(configurationFile, className),
                    ex
            );
        }
        if (!Modifier.isPublic(clazz.getModifiers())) {
            throw new IllegalClassException(
                    modelReplacerClassMustBePublic(configurationFile, className)
            );
        }
        if (Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalClassException(
                    modelReplacerClassMustNotBeAbstract(configurationFile, className)
            );
        }
        if (!AbstractObjectModel4JPAReplacer.class.isAssignableFrom(clazz)) {
            throw new IllegalClassException(
                    modelReplacerClassIsNotDerivedTypeOf(configurationFile, className, AbstractObjectModel4JPAReplacer.class)
            );
        }
        
        Constructor<?> constructor;
        try {
            constructor = clazz.getDeclaredConstructor(ObjectModel4JPAInstrumenter.class, String.class, File.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new IllegalClassException(
                    modelReplacerConstructorIsNotFound(
                            configurationFile, 
                            className, 
                            new Class[] { ObjectModel4JPAInstrumenter.class, String.class, File.class }
                    ),
                    ex
            );
        }
        if (!Modifier.isPublic(constructor.getModifiers())) {
            throw new IllegalClassException(
                    modelReplacerConstructorIsNotPublic(
                            configurationFile, 
                            className, 
                            new Class[] { ObjectModel4JPAInstrumenter.class, String.class, File.class }
                    )
            );
        }
        
        REPLACER_CONSTRUCTOR = constructor;
    }
    
    @I18N
    private static native String findObjectModel4JPAConfigurationFailed(String configurationLocation);
    
    @I18N
    private static native String conflicitObjectModel4JPAConfigurations(URL configurationURL1, URL configurationURL2);
    
    @I18N
    private static native String noObjectModel4JPAConfiguration(String configurationLocation);
    
    @I18N
    private static native String readObjectModel4JPAConfigurationFailed(URL configurationURL);
    
    @I18N
    private static native String modelReplacerIsNotConfigured(URL configurationFile, String propertyName);
    
    @I18N
    private static native String modelReplacerClassIsNotFound(URL configurationFile, String modelReplacerClassName);
    
    @I18N
    private static native String modelReplacerClassMustBePublic(URL configurationFile, String className);
    
    @I18N
    private static native String modelReplacerClassMustNotBeAbstract(URL configurationFile, String className);
    
    @I18N
    private static native String modelReplacerClassIsNotDerivedTypeOf(
            URL configurationFile, 
            String className, 
            Class<AbstractObjectModel4JPAReplacer> superType);
    
    @I18N
    private static native String modelReplacerConstructorIsNotFound(
            URL configurationFile, 
            String className, 
            Class<?>[] parameterTypes);
    
    @I18N
    private static native String modelReplacerConstructorIsNotPublic(
            URL configurationFile, 
            String className, 
            Class<?>[] parameterTypes);
}
