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
package org.babyfish.model.instrument.metadata.spi;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.I18N;
import org.babyfish.lang.bytecode.ASMTreeUtils;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.model.ComparatorRule;
import org.babyfish.model.ModelType;
import org.babyfish.model.NullComparatorType;
import org.babyfish.model.StringComparatorType;
import org.babyfish.model.instrument.metadata.MetadataClass;
import org.babyfish.model.instrument.metadata.MetadataComparatorPart;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.org.objectweb.asm.tree.AnnotationNode;
import org.babyfish.org.objectweb.asm.tree.ClassNode;

/**
 * @author Tao Chen
 */
public abstract class AbstractMetadataClass implements MetadataClass {
    
    private File classFile;
    
    private String name;
    
    private String internalName;
    
    private String descriptor;
    
    private String superTypeName;
    
    private int bytecodeVersion;
    
    protected AbstractMetadataClass(File classFile, ClassNode classNode) {
        this.classFile = classFile;
        this.name = classNode.name.replace('/', '.');
        this.internalName = classNode.name;
        this.descriptor = 'L' + classNode.name + ';';
        this.superTypeName = classNode.superName.replace('/', '.');
        this.bytecodeVersion = classNode.version;
    }
    
    public abstract void finish();
    
    @Override
    public File getClassFile() {
        return this.classFile;
    }

    @Override
    public int getBytecodeVersion() {
        return this.bytecodeVersion;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public final String getSuperTypeName() {
        return this.superTypeName;
    }

    @Override
    public String toString() {
        return this.name;
    }

    protected Collection<MetadataComparatorPart> determineComparatorParts(ClassNode classNode) {
        AnnotationNode comparatorRuleNode =
                ASMTreeUtils.getAnnotationNode(
                        classNode, 
                        ComparatorRule.class
                );
        if (comparatorRuleNode != null) {
            if (this.getModelType() != ModelType.EMBEDDABLE) {
                throw new IllegalClassException(
                        comparatorRuleCanOnlyBeUsedOnEmbeddedModel(
                                this.name, 
                                ComparatorRule.class,
                                ModelType.EMBEDDABLE
                        )
                );
            }
            List<AnnotationNode> comparatorPropertyNodes =
                    ASMTreeUtils.getAnnotationValue(
                            comparatorRuleNode, 
                            "properties"
                    );
            if (comparatorPropertyNodes.isEmpty()) {
                throw new IllegalClassException(
                        emptyComparatorRuleProperties(
                                this.name, 
                                ComparatorRule.class
                        )
                );
            }
            Map<String, MetadataComparatorPart> map = new LinkedHashMap<>();
            for (AnnotationNode comparatorPropertyNode : comparatorPropertyNodes) {
                String propertyName = ASMTreeUtils.getAnnotationValue(comparatorPropertyNode, "name");
                MetadataProperty comparatorProperty = this.getProperties().get(propertyName);
                if (comparatorProperty == null) {
                    throw new IllegalClassException(
                            noComparatorRuleProperty(
                                    this.name,
                                    ComparatorRule.class,
                                    propertyName
                            )
                    );
                }
                if (comparatorProperty.getDescriptor().charAt(0) == '[') {
                    throw new IllegalClassException(
                            arrayComparatorRuleProperty(
                                    this.name,
                                    ComparatorRule.class,
                                    propertyName
                            )
                    );
                }
                StringComparatorType stringComparatorType = ASMTreeUtils.getAnnotationEnumValue(
                        StringComparatorType.class, 
                        comparatorPropertyNode, 
                        "stringComparatorType"
                );
                if (stringComparatorType == null) {
                    stringComparatorType = StringComparatorType.SENSITIVE;
                } else if (!comparatorProperty.getDescriptor().equals("Ljava/lang/String;")) {
                    throw new IllegalClassException(
                            stringComparatorTypeForNonStringProperty(
                                    this.name,
                                    ComparatorRule.class,
                                    propertyName
                            )
                    );
                }
                NullComparatorType nullComparatorType = ASMTreeUtils.getAnnotationEnumValue(
                        NullComparatorType.class,
                        comparatorPropertyNode,
                        "nullComparatorType"
                );
                if (nullComparatorType == null) {
                    nullComparatorType = NullComparatorType.NULLS_FIRST;
                } else if (comparatorProperty.getDescriptor().charAt(0) != 'L') {
                    throw new IllegalClassException(
                            nullComparatorTypeForPrimitiveProperty(
                                    this.name,
                                    ComparatorRule.class,
                                    propertyName
                            )
                    );
                }
                MetadataComparatorPart comparatorPart = 
                        new MetadataComparatorPart(comparatorProperty, stringComparatorType, nullComparatorType);
                if (map.put(propertyName, comparatorPart) != null) {
                    throw new IllegalClassException(
                            duplicatedComparatorPropertyName(
                                    this.name,
                                    ComparatorRule.class,
                                    propertyName
                            )
                    );
                }
            }
            return MACollections.unmodifiable(map.values());
        }
        
        if (this.getModelType() == ModelType.EMBEDDABLE) {
            List<MetadataComparatorPart> partList = new ArrayList<>(this.getPropertyList().size());
            for (MetadataProperty metadataProperty : this.getPropertyList()) {
                if (metadataProperty.getDescriptor().charAt(0) != '[') {
                    partList.add(
                            new MetadataComparatorPart(
                                    metadataProperty, 
                                    StringComparatorType.SENSITIVE, 
                                    NullComparatorType.NULLS_FIRST
                            )
                    );
                }
            }
            return MACollections.unmodifiable(partList);
        }
        
        return null;
    }
    
    @I18N
    private static native String comparatorRuleCanOnlyBeUsedOnEmbeddedModel(
            String className,
            Class<ComparatorRule> comparatorRuleTypeConstant,
            ModelType embeddedModelTypeConstant);
    
    @I18N
    private static native String emptyComparatorRuleProperties(
            String className,
            Class<ComparatorRule> comparatorRuleTypeConstant);
    
    @I18N
    private static native String noComparatorRuleProperty(
            String className,
            Class<ComparatorRule> comparatorRuleTypeConstant,
            String propertyName);
    
    @I18N
    private static native String arrayComparatorRuleProperty(
            String className,
            Class<ComparatorRule> comparatorRuleTypeConstant,
            String propertyName);
    
    @I18N
    private static native String stringComparatorTypeForNonStringProperty(
            String className,
            Class<ComparatorRule> comparatorRuleTypeConstant,
            String propertyName);
    
    @I18N
    private static native String nullComparatorTypeForPrimitiveProperty(
            String className,
            Class<ComparatorRule> comparatorRuleTypeConstant,
            String propertyName);
    
    @I18N
    private static native String duplicatedComparatorPropertyName(
            String className,
            Class<ComparatorRule> comparatorRuleTypeConstant,
            String propertyName);
}
