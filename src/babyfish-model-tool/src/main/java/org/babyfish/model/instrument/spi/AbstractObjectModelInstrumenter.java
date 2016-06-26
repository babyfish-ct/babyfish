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
package org.babyfish.model.instrument.spi;

import java.io.File;
import java.util.Map;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.I18N;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.lang.instrument.Instrumenter;
import org.babyfish.lang.instrument.Logger;
import org.babyfish.lang.instrument.NoCodeClassNodeLoader;
import org.babyfish.model.Model;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.instrument.metadata.spi.AbstractMetadataClass;
import org.babyfish.model.instrument.metadata.spi.AbstractMetadataProperty;
import org.babyfish.model.instrument.metadata.spi.ClassProcessor;
import org.babyfish.model.instrument.metadata.spi.Processor;
import org.babyfish.model.instrument.metadata.spi.PropertyProcessor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.tree.ClassNode;

/**
 * @author Tao Chen
 */
public abstract class AbstractObjectModelInstrumenter implements Instrumenter {
    
    private static final int ACC_NOT_CLASS = 
            Opcodes.ACC_INTERFACE | Opcodes.ACC_ENUM | Opcodes.ACC_ANNOTATION;
    
    private Map<String, AbstractMetadataClass> metadataClasses = new HashMap<>();
    
    private NoCodeClassNodeLoader noCodeClassNodeLoader;
    
    protected Logger logger;
    
    protected AbstractObjectModelInstrumenter() {}
    
    @Override
    public void setNoCodeClassNodeLoader(NoCodeClassNodeLoader noCodeClassNodeLoader) {
        this.noCodeClassNodeLoader = noCodeClassNodeLoader;
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void addClassFile(File classFile) {
        ClassNode classNode = this.noCodeClassNodeLoader.load(classFile);
        if ((classNode.access & ACC_NOT_CLASS) != 0) {
            throw new IllegalClassException(
                    modelTypeMustBeClass(
                            classNode.name.replace('/', '.'), 
                            Model.class
                    )
            );
        }
        AbstractMetadataClass metadataClass = this.createMetadataClass(classFile, classNode);
        this.metadataClasses.put(metadataClass.getName(), metadataClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        for (Processor processor : this.getProcessors()) {
            if (processor instanceof ClassProcessor) {
                ClassProcessor<AbstractMetadataClass> classProcessor = (ClassProcessor<AbstractMetadataClass>)processor;
                for (AbstractMetadataClass metadataClass : this.metadataClasses.values()) {
                    classProcessor.processClass(metadataClass);
                }
            } else if (processor instanceof PropertyProcessor) {
                PropertyProcessor<AbstractMetadataProperty> propertyProcessor = 
                        (PropertyProcessor<AbstractMetadataProperty>)processor;
                for (AbstractMetadataClass metadataClass : this.metadataClasses.values()) {
                    Map<String, MetadataProperty> declaredProperties = metadataClass.getDeclaredProperties();
                    if (declaredProperties != null) {
                        for (MetadataProperty declaredProperty : declaredProperties.values()) {
                            propertyProcessor.processProperty((AbstractMetadataProperty)declaredProperty);
                        }
                    }
                }
            } else {
                throw new IllegalProgramException(
                        illegalGetProcessors(
                                this.getClass(),
                                ClassProcessor.class,
                                PropertyProcessor.class
                        )
                );
            }
        }
        for (AbstractMetadataClass metadataClass : this.metadataClasses.values()) {
            metadataClass.finish();
            for (MetadataProperty declaredProperty : metadataClass.getDeclaredProperties().values()) {
                ((AbstractMetadataProperty)declaredProperty).finish();
            }
        }
    }

    protected abstract AbstractMetadataClass createMetadataClass(File classFile, ClassNode classNode);
    
    protected abstract Processor[] getProcessors();
    
    public Map<String, AbstractMetadataClass> getMetadataClasses() {
        return MACollections.unmodifiable(this.metadataClasses);
    }
    
    @SuppressWarnings("unchecked")
    public final <C extends AbstractMetadataClass> C getMetadataClass(String className) {
        return (C)this.metadataClasses.get(className);
    }
    
    public final ClassNode getClassNode(String className) {
        return this.noCodeClassNodeLoader.load(className);
    }
    
    @I18N
    private static native String modelTypeMustBeClass(
            String modelTypeName, 
            Class<Model> modeTypeConstant);
    
    @SuppressWarnings("rawtypes") 
    @I18N
    private static native String illegalGetProcessors(
            Class<? extends AbstractObjectModelInstrumenter> thisType,
            Class<ClassProcessor> classProcessorTypeConstant,
            Class<PropertyProcessor> propertyProcessorTypeConstant
    );
}
