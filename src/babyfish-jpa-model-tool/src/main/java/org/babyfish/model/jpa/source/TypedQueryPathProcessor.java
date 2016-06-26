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
package org.babyfish.model.jpa.source;

import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.tools.JavaFileObject;

import org.babyfish.collection.HashMap;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.UncheckedException;
import org.babyfish.model.jpa.source.metadata.MetadataClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tao Chen
 */
@SupportedAnnotationTypes({ "javax.persistence.Entity", "javax.persistence.MappedSuperclass", "javax.persistence.Embeddable" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TypedQueryPathProcessor extends AbstractProcessor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TypedQueryPathProcessor.class);
    
    @Override
    public boolean process(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        try {
            this.processImpl(annotations, roundEnv); 
            return true;
        } catch (RuntimeException | Error ex) {
            LOGGER.error(
                    "Failed to process because an exception raised",
                    ex
            );
            throw ex;
        }
    }
    
    private void processImpl(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        if ((roundEnv.processingOver())) {
            LOGGER.info("Skip the process because the processing is over");
            return;
        }
        if (annotations.isEmpty()) {
            LOGGER.info("Skip the process because there is no annotations");
            return;
        }
        Map<String, MetadataClass> metadataClasses = new HashMap<>();
        for (Element element : roundEnv.getRootElements()) {
            if (ElementHelper.containsAnyAnnotation(element, Entity.class, Embeddable.class, MappedSuperclass.class)) {
                TypeElement typeElement = (TypeElement)element;
                if (typeElement.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                    throw new IllegalProgramException("Entity, Embeddable, MappedSuperClass can not be nested class.");
                }
                MetadataClass entity = new MetadataClass(typeElement);
                metadataClasses.put(entity.getName(), entity);
            }
        }
        if (metadataClasses.isEmpty()) {
            LOGGER.info(
                    "Skip the process because there is no class that is marked by \"@" +
                    Entity.class.getName() +
                    ", " +
                    Embeddable.class.getName() +
                    "\" and \"@" +
                    MappedSuperclass.class.getName() +
                    "\"");
            return;
        }
        for (MetadataClass entity : metadataClasses.values()) {
            entity.secondaryPass(metadataClasses);
        }
        
        this.generate(metadataClasses.values());
    }
    
    private void generate(Collection<MetadataClass> metadataClasses) {
        for (MetadataClass metadataClass : metadataClasses) {
            try {
                JavaFileObject fo =
                        this
                        .processingEnv
                        .getFiler()
                        .createSourceFile(metadataClass.getName() + SourceGenerator.NAME_POSTFIX);
                try (Writer writer = fo.openWriter()) {
                    new SourceGenerator(metadataClass, metadataClasses).generate(writer);
                }
            } catch (Throwable ex) {
                UncheckedException.rethrow(ex);
            }
        }
    }
}
