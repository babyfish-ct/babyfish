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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollection;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAList;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.MASet;
import org.babyfish.collection.MASortedMap;
import org.babyfish.collection.MASortedSet;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XList;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XNavigableMap;
import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.collection.XSet;
import org.babyfish.collection.XSortedMap;
import org.babyfish.collection.XSortedSet;
import org.babyfish.lang.I18N;
import org.babyfish.lang.bytecode.ASMTreeUtils;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.model.ComparatorRule;
import org.babyfish.model.Immutable;
import org.babyfish.model.ModelType;
import org.babyfish.model.Navigable;
import org.babyfish.model.NullComparatorType;
import org.babyfish.model.StringComparatorType;
import org.babyfish.model.instrument.metadata.MetadataClass;
import org.babyfish.model.instrument.metadata.MetadataComparatorPart;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.instrument.spi.AbstractObjectModelInstrumenter;
import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.org.objectweb.asm.tree.AnnotationNode;
import org.babyfish.org.objectweb.asm.tree.ClassNode;
import org.babyfish.org.objectweb.asm.tree.FieldNode;

/**
 * @author Tao Chen
 */
public abstract class AbstractMetadataProperty implements MetadataProperty {

    private static final Map<String, Class<?>> SIMPLE_TYPE_MAP;
    
    private static final Map<String, Class<?>> STANDARD_COLLECTION_TYPE_MAP;
    
    protected AbstractMetadataClass declaringClass;
    
    protected String name;
    
    protected String descriptor;
    
    protected String signature;
    
    protected Class<?> simpleType;
    
    protected Class<?> standardCollectionType;
    
    protected String keyTypeName;
    
    protected String keyDescriptor;
    
    protected String targetTypeName;
    
    protected String targetDescriptor;
    
    protected AbstractMetadataProperty(AbstractMetadataClass declaringClass, FieldNode fieldNode) {
        this.declaringClass = declaringClass;
        this.name = fieldNode.name;
        this.descriptor = fieldNode.desc;
        this.signature = fieldNode.signature;
        this.simpleType = SIMPLE_TYPE_MAP.get(fieldNode.desc);
        this.standardCollectionType = STANDARD_COLLECTION_TYPE_MAP.get(fieldNode.desc);
        if (ASMTreeUtils.getAnnotationNode(fieldNode, Navigable.class) != null) {
            if (XOrderedSet.class.isAssignableFrom(this.standardCollectionType) ||
                    XOrderedMap.class.isAssignableFrom(this.standardCollectionType)) {
                throw new IllegalClassException(
                        navigableAnnoationRejectOrderedField(
                                this,
                                Navigable.class,
                                this.standardCollectionType
                        )
                );
            }
            if (Set.class.isAssignableFrom(this.standardCollectionType)) {
                this.standardCollectionType = NavigableSet.class;
            } else if (Map.class.isAssignableFrom(this.standardCollectionType)) {
                this.standardCollectionType = NavigableMap.class;
            } else {
                throw new IllegalClassException(
                        navigableAnnoationRequireSetOrMap(
                                this,
                                Navigable.class,
                                Set.class,
                                Map.class
                        )
                );
            }
        }
        this.determineKeyAndTarget(fieldNode);
    }
    
    @Override
    public AbstractMetadataClass getDeclaringClass() {
        return this.declaringClass;
    }
    
    @Override
    public final String getDescriptor() {
        return this.descriptor;
    }
    
    @Override
    public final String getSignature() {
        return this.signature;
    }

    @Override
    public final String getKeyTypeName() {
        return this.keyTypeName;
    }
    
    @Override
    public final String getKeyDescriptor() {
        return this.keyDescriptor;
    }
    
    @Override
    public final String getTargetTypeName() {
        return this.targetTypeName;
    }

    @Override
    public final String getTargetDescriptor() {
        return this.targetDescriptor;
    }
    
    @Override
    public final Class<?> getSimpleType() {
        return this.simpleType;
    }

    @Override
    public final Class<?> getStandardCollectionType() {
        return this.standardCollectionType;
    }

    @Override
    public String toString() {
        return this.declaringClass.getName() + '.' + this.getName();
    }

    public abstract void finish();
    
    public void resolveClass(AbstractObjectModelInstrumenter instrumenter) {
        PropertyType propertyType = this.getPropertyType();
        if (propertyType == PropertyType.ASSOCIATION || propertyType == PropertyType.CONTRAVARIANCE) {
            if (this.standardCollectionType != null) {
                AbstractMetadataClass targetClass = instrumenter.getMetadataClass(this.targetTypeName);
                if (targetClass == null) {
                    throw new IllegalClassException(
                            illegalAssociationTarget(
                                    this,
                                    this.targetTypeName
                            )
                    );
                }
                if (targetClass.getModelType() != ModelType.REFERENCE) {
                    throw new IllegalClassException(
                            associationTargetModelTypeMustBeReference(
                                    this,
                                    this.targetTypeName,
                                    ModelType.REFERENCE
                            )
                    );
                }
                this.setTargetClass(targetClass);
                if (Map.class.isAssignableFrom(this.standardCollectionType)) {
                    this.setKeyClass(this.getNonAssociationClass(true, instrumenter));
                }
            } else {
                if (!this.targetDescriptor.startsWith("L")) {
                    throw new IllegalClassException(
                            rerferenceTargetMustNotBePrimitive(this)
                    );
                }
                AbstractMetadataClass targetClass = instrumenter.getMetadataClass(this.targetTypeName);
                if (targetClass == null) {
                    throw new IllegalClassException(
                            illegalAssociationTarget(
                                    this,
                                    this.targetTypeName
                            )
                    );
                }
                if (targetClass.getModelType() != ModelType.REFERENCE) {
                    throw new IllegalClassException(
                            associationTargetModelTypeMustBeReference(
                                    this,
                                    this.targetTypeName,
                                    ModelType.REFERENCE
                            )
                    );
                }
                this.setTargetClass(targetClass);
            }
        } else {
            this.setTargetClass(this.getNonAssociationClass(false, instrumenter));
        }
    }
    
    protected abstract void setKeyClass(AbstractMetadataClass keyClass);
    
    protected abstract void setTargetClass(AbstractMetadataClass targetClass);
    
    private AbstractMetadataClass getNonAssociationClass(boolean mapKey, AbstractObjectModelInstrumenter instrumenter) {
        if (mapKey && SIMPLE_TYPE_MAP.get(this.keyDescriptor) != null) {
            return null;
        }
        if (!mapKey && this.simpleType != null) {
            return null;
        }
        String className = mapKey ? this.keyTypeName : this.targetTypeName;
        AbstractMetadataClass metadataClass = instrumenter.getMetadataClass(className);
        if (metadataClass != null) {
            if (metadataClass.getModelType() != ModelType.EMBEDDABLE) {
                if (mapKey) {
                    throw new IllegalClassException(
                            mapKeyModelTypeMustBeEmbedded(
                                    this,
                                    className,
                                    ModelType.EMBEDDABLE
                            )
                    );
                }
                throw new IllegalClassException(
                        nonAssociationTargetModelTypeMustBeEmbedded(
                                this,
                                className,
                                ModelType.EMBEDDABLE
                        )
                );
            }
            return metadataClass;
        }
        ClassNode classNode = instrumenter.getClassNode(className);
        if (ASMTreeUtils.getAnnotationNode(classNode, ModelType.class) != null) {
            if (mapKey) {
                throw new IllegalClassException(
                        mapKeyModelIsNotInstrumented(this, className)
                );
            }
            throw new IllegalClassException(
                    nonAssociationTargetModelIsNotInstrumented(this, className)
            );
        }
        if (ASMTreeUtils.getAnnotationNode(classNode, Immutable.class) != null) {
            return null;
        }
        if ((classNode.access & Opcodes.ACC_ENUM) != 0) {
            return null;
        }
        if (mapKey) {
            throw new IllegalClassException(
                    illegalMapKey(this, className)
            );
        }
        throw new IllegalClassException(
                illegalNonAssociationTarget(this, className)
        );
    }

    private void determineKeyAndTarget(FieldNode fieldNode) {
        String desc = fieldNode.desc;
        if (this.standardCollectionType != null) {
            if (fieldNode.signature == null) {
                throw new IllegalClassException(
                        collectionTypeMustBeGenericType(this)
                );
            }
            String[] descriptors = this.getTypeArgumentDescriptors(fieldNode.signature);
            this.targetDescriptor = descriptors[descriptors.length - 1];
            this.targetTypeName = targetDescriptor.substring(1, targetDescriptor.length() - 1).replace('/', '.');
            if (Map.class.isAssignableFrom(this.standardCollectionType)) {
                String keyDescriptor = descriptors[0];
                this.keyDescriptor = keyDescriptor;
                this.keyTypeName = keyDescriptor.substring(1, keyDescriptor.length() - 1).replace('/', '.');
            }
        } else {
            this.targetDescriptor = desc;
            this.targetTypeName = determineTypeName(fieldNode.desc);
        }
    }

    private static String determineTypeName(String desc) {
        int len = desc.length();
        int arrDepth = 0;
        for (int i = 0; i < len; i++) {
            if (desc.charAt(i) != '[') {
                break;
            } 
            arrDepth++;
        }
        if (arrDepth != 0) {
            desc = desc.substring(arrDepth);
        }
        if (desc.startsWith("L") && desc.endsWith(";")) {
            desc = desc.substring(1, desc.length() - 1).replace('/', '.');
        }
        if (desc.length() == 1) {
            switch (desc.charAt(0)) {
            case 'Z':
                desc = "boolean";
                break;
            case 'C':
                desc = "char";
                break;
            case 'B':
                desc = "byte";
                break;
            case 'S':
                desc = "short";
                break;
            case 'I':
                desc = "int";
                break;
            case 'J':
                desc = "long";
                break;
            case 'F':
                desc = "float";
                break;
            case 'D':
                desc = "double";
                break;
            }
        }
        for (int i = arrDepth; i > 0; i--) {
            desc += "[]";
        }
        return desc;
    }
    
    private String[] getTypeArgumentDescriptors(String signature) {
        // SignatureReader is too complex...
        int depth = 0;
        int sigLen = signature.length();
        int len = 0;
        char[] arr = new char[signature.length()];
        for (int i = 0; i < sigLen; i++) {
            char c = signature.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
            } else if (depth == 1) {
                arr[len++] = c;
            }
        }
        if (arr[0] != 'L') {
            throw new IllegalClassException(
                    genericTypeArgumentMustBeClass(this, 0)
            );
        }
        int nextIndex = -1;
        for (int i = 0; i < len; i++) {
            if (arr[i] == ';') {
                nextIndex = i + 1;
                break;
            }
        }
        if (nextIndex == len) {
            return new String[] { new String(arr, 0, len) };
        }
        String[] pair = new String[2];
        pair[0] = new String(arr, 0, nextIndex);
        if (arr[nextIndex] != 'L') {
            throw new IllegalClassException(
                    genericTypeArgumentMustBeClass(this, 1)
            );
        }
        pair[1] = new String(arr, nextIndex, len - nextIndex);
        return pair;
    }
    
    protected AssociationType determineAssociationType() {
        if (this.getPropertyType() != PropertyType.ASSOCIATION) {
            return AssociationType.NONE;
        }
        Class<?> standardCollectionType = this.getStandardCollectionType();
        if (standardCollectionType != null) {
            if (Map.class.isAssignableFrom(standardCollectionType)) {
                return AssociationType.MAP;
            }
            if (List.class.isAssignableFrom(standardCollectionType)) {
                return AssociationType.LIST;
            }
            return AssociationType.COLLECTION;
        }
        if (this.getKeyProperty() != null) {
            return AssociationType.KEYED_REFERENCE;
        }
        if (this.getIndexProperty() != null) {
            return AssociationType.INDEXED_REFERENCE;
        }
        return AssociationType.REFERENCE;
    }
    
    protected void validateOppositeAssociationProperty(
            AbstractMetadataProperty oppositeAssocationProperty) {
        AssociationType thisType = this.getAssociationType();
        if (thisType == AssociationType.INDEXED_REFERENCE) {
            this.requireOppositeAssociationEndpoints(
                    oppositeAssocationProperty, 
                    AssociationType.LIST
            );
        } else if (thisType == AssociationType.KEYED_REFERENCE) {
            this.requireOppositeAssociationEndpoints(
                    oppositeAssocationProperty, 
                    AssociationType.MAP
            );
        } else if (thisType == AssociationType.LIST) {
            this.requireOppositeAssociationEndpoints(
                    oppositeAssocationProperty, 
                    AssociationType.REFERENCE,
                    AssociationType.INDEXED_REFERENCE,
                    AssociationType.COLLECTION
            );
        } else if (thisType == AssociationType.MAP) {
            this.requireOppositeAssociationEndpoints(
                    oppositeAssocationProperty, 
                    AssociationType.REFERENCE,
                    AssociationType.KEYED_REFERENCE,
                    AssociationType.COLLECTION
            );
        } else {
            this.requireOppositeAssociationEndpoints(
                    oppositeAssocationProperty, 
                    AssociationType.REFERENCE,
                    AssociationType.COLLECTION,
                    AssociationType.LIST,
                    AssociationType.MAP
            );
        }
        
        if (oppositeAssocationProperty.getTargetClass() != this.getDeclaringClass()) {
            throw new IllegalClassException(
                    illegalDeclaringClassOfOppositeProperty(
                            this,
                            oppositeAssocationProperty,
                            this.getDeclaringClass()
                    )
            );
        }
        if (this.getTargetClass() != oppositeAssocationProperty.getDeclaringClass()) {
            throw new IllegalClassException(
                    illegalDeclaringClassOfOppositeProperty(
                            oppositeAssocationProperty,
                            this,
                            oppositeAssocationProperty.getDeclaringClass()
                    )
            );
        }
        
        String thisKeyTypeName = keyTypeName(this);
        String oppositeKeyTypeName = keyTypeName(oppositeAssocationProperty);
        if (thisKeyTypeName != null && 
                oppositeKeyTypeName != null &&
                !thisKeyTypeName.equals(oppositeKeyTypeName)) {
            throw new IllegalClassException(
                    differentKeyTypeName(
                            this,
                            oppositeAssocationProperty,
                            thisKeyTypeName,
                            oppositeKeyTypeName
                    )
            );
        }
        
        if (this.getConvarianceProperty() != null && oppositeAssocationProperty.getConvarianceProperty() == null) {
            throw new IllegalClassException(
                    oppositePropertyMustBeContravariance(
                            this,
                            oppositeAssocationProperty
                    )
            );
        }
        if (this.getConvarianceProperty() == null && oppositeAssocationProperty.getConvarianceProperty() != null) {
            throw new IllegalClassException(
                    oppositePropertyMustBeContravariance(
                            oppositeAssocationProperty,
                            this
                    )
            );
        }
    }
    
    private static String keyTypeName(MetadataProperty metadataProperty) {
        if (metadataProperty.getKeyProperty() != null) {
            return metadataProperty.getKeyProperty().getTargetTypeName();
        }
        return metadataProperty.getKeyTypeName();
    }
    
    protected Collection<MetadataComparatorPart> determineComparatorParts(
            FieldNode fieldNode, 
            AbstractObjectModelInstrumenter instrumenter) {
        
        Class<?> standardCollectionType = this.getStandardCollectionType();
        AnnotationNode comparatorRuleNode = 
                ASMTreeUtils.getAnnotationNode(fieldNode, ComparatorRule.class);
        
        if (comparatorRuleNode == null) {
            if (this.getPropertyType() == PropertyType.ASSOCIATION &&
                    standardCollectionType != null &&
                    SortedSet.class.isAssignableFrom(standardCollectionType)) {
                throw new IllegalClassException(
                        sortedSetRequireComparatorRule(this, ComparatorRule.class)
                );
            }
            return null;
        }
        
        if (this.getPropertyType() != PropertyType.ASSOCIATION) {
            throw new IllegalClassException(
                    comparatorRuleCanOnlyBeUsedByAssociation(this, ComparatorRule.class)
            );
        }
        
        boolean sortedSet = SortedSet.class.isAssignableFrom(standardCollectionType);
        List<AnnotationNode> comparatorPropertyNodes = ASMTreeUtils.getAnnotationValue(
                comparatorRuleNode, 
                "properties");
        if (comparatorPropertyNodes.isEmpty()) {
            throw new IllegalClassException(
                    emptyComparatorRuleProperties(this, ComparatorRule.class)
            );
        }
        MetadataClass targetClass = this.getTargetClass();
        Map<String, MetadataComparatorPart> partMap;
        if (sortedSet) {
            partMap = new LinkedHashMap<>((comparatorPropertyNodes.size() * 4 + 2) /3);
        } else {
            partMap = new TreeMap<>();
        }
        
        for (AnnotationNode comparatorPropertyNode : comparatorPropertyNodes) {
            String propertyName = ASMTreeUtils.getAnnotationValue(comparatorPropertyNode, "name");
            MetadataProperty comparatorProperty = targetClass.getProperties().get(propertyName);
            if (comparatorProperty == null) {
                throw new IllegalClassException(
                        noComparatorRuleProperty(
                                this, 
                                ComparatorRule.class, 
                                propertyName,
                                this.targetTypeName
                        )
                );
            }
            if (comparatorProperty.getPropertyType() != PropertyType.SCALAR) {
                throw new IllegalClassException(
                        nonScalarComparatorRuleProperty(
                                this, 
                                ComparatorRule.class, 
                                comparatorProperty
                        )
                );
            }
            if (comparatorProperty.getDescriptor().charAt(0) == '[') {
                throw new IllegalClassException(
                        arrayComparatorRuleProperty(
                                this, 
                                ComparatorRule.class, 
                                comparatorProperty
                        )
                );
            } else if (sortedSet && 
                    comparatorProperty.getTargetClass() == null && 
                    comparatorProperty.getSimpleType() == null) {
                if (!isComparable(comparatorProperty.getTargetTypeName(), instrumenter)) {
                    throw new IllegalClassException(
                            nonComparableComparatorRuleProperty(
                                    this, 
                                    ComparatorRule.class, 
                                    comparatorProperty,
                                    comparatorProperty.getDeclaringClass(),
                                    ModelType.EMBEDDABLE,
                                    Comparable.class
                            )
                    );
                }
            }
            
            StringComparatorType stringComparatorType = 
                    ASMTreeUtils.getAnnotationEnumValue(
                            StringComparatorType.class,
                            comparatorPropertyNode, 
                            "stringComparatorType"
                    );
            if (stringComparatorType == null) {
                stringComparatorType = StringComparatorType.SENSITIVE;
            } else if (!comparatorProperty.getDescriptor().equals("Ljava/lang/String;")) {
                throw new IllegalClassException(
                        stringComparatorTypeForNonStringProperty(
                                this, 
                                ComparatorRule.class, 
                                comparatorProperty
                        )
                );
            }
            NullComparatorType nullComparatorType = 
                    ASMTreeUtils.getAnnotationEnumValue(
                            NullComparatorType.class,
                            comparatorPropertyNode, 
                            "nullComparatorType"
                    );
            if (nullComparatorType == null) {
                nullComparatorType = NullComparatorType.NULLS_FIRST;
            } else {
                if (comparatorProperty.getDescriptor().charAt(0) != 'L') {
                    throw new IllegalClassException(
                            nullComparatorTypeForNonStringProperty(
                                    this, 
                                    ComparatorRule.class,
                                    comparatorProperty
                            )
                    );
                } 
            }
            MetadataComparatorPart part = new MetadataComparatorPart(comparatorProperty, stringComparatorType, nullComparatorType);
            if (partMap.put(propertyName, part) != null) {
                throw new IllegalClassException(
                        duplicateComparatorRuleProperty(this, ComparatorRule.class, propertyName)
                );
            }
        }
        if (partMap.isEmpty()) {
            return null;
        }
        return partMap.isEmpty() ? null : MACollections.unmodifiable(partMap.values());
    }
    
    private static boolean isComparable(String className, AbstractObjectModelInstrumenter instrumentor) {
        if (className.equals("java.lang.Comparable")) {
            return true;
        }
        ClassNode classNode = instrumentor.getClassNode(className);
        if (classNode.superName != null && 
                classNode.superName.equals("java/lang/Object") &&
                isComparable(classNode.superName.replace('/', '.'), instrumentor)) {
            return true;
        }
        if (classNode.interfaces != null) {
            for (String interfaceName : classNode.interfaces) {
                if (isComparable(interfaceName.replace('/', '.'), instrumentor)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void requireOppositeAssociationEndpoints(
            AbstractMetadataProperty oppositeAssocationProperty,
            AssociationType ... expectedOppositeAssociationTypes) {
        AssociationType actualOppositeAssociationType = oppositeAssocationProperty.getAssociationType();
        for (AssociationType expectedOppositeAssociationType : expectedOppositeAssociationTypes) {
            if (expectedOppositeAssociationType == actualOppositeAssociationType) {
                return;
            }
        }
        throw new IllegalArgumentException(
                illegalOppositeAssociationType(
                        this,
                        oppositeAssocationProperty,
                        this.getAssociationType(),
                        expectedOppositeAssociationTypes,
                        actualOppositeAssociationType
                )
        );
    }
    
    static {
        
        Map<String, Class<?>> simpleMap = new HashMap<>();
        
        simpleMap.put(Type.getDescriptor(boolean.class), boolean.class);
        simpleMap.put(Type.getDescriptor(char.class), char.class);
        simpleMap.put(Type.getDescriptor(byte.class), byte.class);
        simpleMap.put(Type.getDescriptor(short.class), short.class);
        simpleMap.put(Type.getDescriptor(int.class), int.class);
        simpleMap.put(Type.getDescriptor(long.class), long.class);
        simpleMap.put(Type.getDescriptor(float.class), float.class);
        simpleMap.put(Type.getDescriptor(double.class), double.class);
        
        simpleMap.put(Type.getDescriptor(Boolean.class), Boolean.class);
        simpleMap.put(Type.getDescriptor(Character.class), Character.class);
        simpleMap.put(Type.getDescriptor(Byte.class), Byte.class);
        simpleMap.put(Type.getDescriptor(Short.class), Short.class);
        simpleMap.put(Type.getDescriptor(Integer.class), Integer.class);
        simpleMap.put(Type.getDescriptor(Long.class), Long.class);
        simpleMap.put(Type.getDescriptor(Float.class), Float.class);
        simpleMap.put(Type.getDescriptor(Double.class), Double.class);
        
        simpleMap.put(Type.getDescriptor(BigInteger.class), BigInteger.class);
        simpleMap.put(Type.getDescriptor(BigDecimal.class), BigDecimal.class);
        
        simpleMap.put(Type.getDescriptor(String.class), String.class);
        simpleMap.put(Type.getDescriptor(char[].class), char[].class);
        simpleMap.put(Type.getDescriptor(byte[].class), byte[].class);
        
        simpleMap.put(Type.getDescriptor(java.util.Date.class), java.util.Date.class);
        simpleMap.put(Type.getDescriptor(java.sql.Date.class), java.sql.Date.class);
        simpleMap.put(Type.getDescriptor(java.sql.Time.class), java.sql.Time.class);
        simpleMap.put(Type.getDescriptor(java.sql.Timestamp.class), java.sql.Timestamp.class);
        simpleMap.put(Type.getDescriptor(Calendar.class), Calendar.class);
        simpleMap.put(Type.getDescriptor(GregorianCalendar.class), GregorianCalendar.class);
        
        simpleMap.put(Type.getDescriptor(Duration.class), Duration.class);
        simpleMap.put(Type.getDescriptor(Instant.class), Instant.class);
        simpleMap.put(Type.getDescriptor(LocalDate.class), LocalDate.class);
        simpleMap.put(Type.getDescriptor(LocalTime.class), LocalTime.class);
        simpleMap.put(Type.getDescriptor(LocalDateTime.class), LocalDateTime.class);
        simpleMap.put(Type.getDescriptor(MonthDay.class), MonthDay.class);
        simpleMap.put(Type.getDescriptor(OffsetDateTime.class), OffsetDateTime.class);
        simpleMap.put(Type.getDescriptor(Period.class), Period.class);
        simpleMap.put(Type.getDescriptor(Year.class), Year.class);
        simpleMap.put(Type.getDescriptor(YearMonth.class), YearMonth.class);
        simpleMap.put(Type.getDescriptor(MonthDay.class), MonthDay.class);
        simpleMap.put(Type.getDescriptor(ZonedDateTime.class), ZonedDateTime.class);
        simpleMap.put(Type.getDescriptor(ZoneId.class), ZoneId.class);
        simpleMap.put(Type.getDescriptor(ZoneOffset.class), ZoneOffset.class);
        
        SIMPLE_TYPE_MAP = simpleMap;
        
        
        Map<String, Class<?>> collectionMap = new HashMap<>();
        
        collectionMap.put(Type.getDescriptor(Collection.class), Collection.class);
        collectionMap.put(Type.getDescriptor(List.class), List.class);
        collectionMap.put(Type.getDescriptor(Set.class), Set.class);
        collectionMap.put(Type.getDescriptor(SortedSet.class), NavigableSet.class);
        collectionMap.put(Type.getDescriptor(NavigableSet.class), NavigableSet.class);
        collectionMap.put(Type.getDescriptor(Map.class), Map.class);
        collectionMap.put(Type.getDescriptor(SortedMap.class), NavigableMap.class);
        collectionMap.put(Type.getDescriptor(NavigableMap.class), NavigableMap.class);
        
        collectionMap.put(Type.getDescriptor(XCollection.class), Collection.class);
        collectionMap.put(Type.getDescriptor(XList.class), List.class);
        collectionMap.put(Type.getDescriptor(XSet.class), Set.class);
        collectionMap.put(Type.getDescriptor(XOrderedSet.class), XOrderedSet.class);
        collectionMap.put(Type.getDescriptor(XSortedSet.class), NavigableSet.class);
        collectionMap.put(Type.getDescriptor(XNavigableSet.class), NavigableSet.class);
        collectionMap.put(Type.getDescriptor(XMap.class), Map.class);
        collectionMap.put(Type.getDescriptor(XOrderedMap.class), XOrderedMap.class);
        collectionMap.put(Type.getDescriptor(XSortedMap.class), NavigableMap.class);
        collectionMap.put(Type.getDescriptor(XNavigableMap.class), NavigableMap.class);
        
        collectionMap.put(Type.getDescriptor(MACollection.class), Collection.class);
        collectionMap.put(Type.getDescriptor(MAList.class), List.class);
        collectionMap.put(Type.getDescriptor(MASet.class), Set.class);
        collectionMap.put(Type.getDescriptor(MAOrderedSet.class), XOrderedSet.class);
        collectionMap.put(Type.getDescriptor(MASortedSet.class), NavigableSet.class);
        collectionMap.put(Type.getDescriptor(MANavigableSet.class), NavigableSet.class);
        collectionMap.put(Type.getDescriptor(MAMap.class), Map.class);
        collectionMap.put(Type.getDescriptor(MAOrderedMap.class), XOrderedMap.class);
        collectionMap.put(Type.getDescriptor(MASortedMap.class), NavigableMap.class);
        collectionMap.put(Type.getDescriptor(MANavigableMap.class), NavigableMap.class);
        
        STANDARD_COLLECTION_TYPE_MAP = collectionMap;
    }
    
    @I18N
    private static native String navigableAnnoationRejectOrderedField(
            AbstractMetadataProperty thisProperty,
            Class<Navigable> navigableTypeConstant,
            Class<?> propertyType);
    
    @SuppressWarnings("rawtypes")
    @I18N
    private static native String navigableAnnoationRequireSetOrMap(
            AbstractMetadataProperty thisProperty,
            Class<Navigable> navigableTypeConstant,
            Class<Set> setType,
            Class<Map> mapType);
    
    @I18N
    private static native String illegalAssociationTarget(
            AbstractMetadataProperty thisProperty,
            String targetTypeName);
    
    @I18N
    private static native String associationTargetModelTypeMustBeReference(
            AbstractMetadataProperty thisProperty,
            String targetTypeName,
            ModelType referenceModelTypeConstant);
    
    @I18N
    private static native String rerferenceTargetMustNotBePrimitive(
            AbstractMetadataProperty thisProperty);
    
    @I18N
    private static native String mapKeyModelTypeMustBeEmbedded(
            AbstractMetadataProperty thisProperty, 
            String keyTypeName,
            ModelType embededModelTypeConstanst);
    
    @I18N
    private static native String nonAssociationTargetModelTypeMustBeEmbedded(
            AbstractMetadataProperty thisProperty, 
            String targetTypeName,
            ModelType embededModelTypeConstanst);
    
    @I18N
    private static native String mapKeyModelIsNotInstrumented(
            AbstractMetadataProperty thisProperty, 
            String keyTypeName);
    
    @I18N
    private static native String nonAssociationTargetModelIsNotInstrumented(
            AbstractMetadataProperty thisProperty, 
            String targetName);
    
    @I18N
    private static native String illegalMapKey(
            AbstractMetadataProperty thisProperty, 
            String keyTypeName);
    
    @I18N
    private static native String illegalNonAssociationTarget(
            AbstractMetadataProperty thisProperty, 
            String targetTypeName);
    
    @I18N
    private static native String collectionTypeMustBeGenericType(
            AbstractMetadataProperty thisProperty);
    
    @I18N
    private static native String genericTypeArgumentMustBeClass(
            AbstractMetadataProperty thisProperty, 
            int genericTypeParameterIndex);
    
    @I18N
    private static native String illegalOppositeAssociationType(
            AbstractMetadataProperty thisProperty,
            AbstractMetadataProperty oppositeProperty,
            AssociationType thisAssociationType,
            AssociationType[] expectedOppositeAssociationTypes,
            AssociationType actualOppositeAssociationType);
    
    @I18N
    private static native String illegalDeclaringClassOfOppositeProperty(
            AbstractMetadataProperty thisProperty,
            AbstractMetadataProperty oppositeProperty,
            AbstractMetadataClass thisDeclaringClass);
    
    @I18N
    private static native String differentKeyTypeName(
            AbstractMetadataProperty thisProperty,
            AbstractMetadataProperty oppositeProperty,
            String thisKeyTypeName,
            String oppositeKeyTypeName);
    
    @I18N
    private static native String oppositePropertyMustBeContravariance(
            AbstractMetadataProperty thisProperty,
            AbstractMetadataProperty oppositeProperty);
    
    @I18N
    private static native String sortedSetRequireComparatorRule(
            AbstractMetadataProperty thisProperty, 
            Class<ComparatorRule> comparatorRuleTypeConstant);
    
    @I18N
    private static native String comparatorRuleCanOnlyBeUsedByAssociation(
            AbstractMetadataProperty thisProperty, 
            Class<ComparatorRule> comparatorRuleTypeConstant);
    
    @I18N
    private static native String emptyComparatorRuleProperties(
            AbstractMetadataProperty thisProperty, 
            Class<ComparatorRule> comparatorRuleTypeConstant);
    
    @I18N
    private static native String noComparatorRuleProperty(
            AbstractMetadataProperty thisProperty, 
            Class<ComparatorRule> comparatorRuleTypeConstant,
            String propertyName,
            String targetTypeName);
    
    @I18N
    private static native String nonScalarComparatorRuleProperty(
            AbstractMetadataProperty thisProperty, 
            Class<ComparatorRule> comparatorRuleTypeConstant,
            MetadataProperty comparatorProperty);
    
    @I18N
    private static native String arrayComparatorRuleProperty(
            AbstractMetadataProperty thisProperty, 
            Class<ComparatorRule> comparatorRuleTypeConstant,
            MetadataProperty comparatorProperty);
    
    @SuppressWarnings("rawtypes")
    @I18N
    private static native String nonComparableComparatorRuleProperty(
            AbstractMetadataProperty thisProperty, 
            Class<ComparatorRule> comparatorRuleTypeConstant,
            MetadataProperty comparatorProperty,
            MetadataClass comparatorPropertyDeclaringClass,
            ModelType embeddedModelTypeConstant,
            Class<Comparable> comparableTypeConstant);
    
    @I18N
    private static native String stringComparatorTypeForNonStringProperty(
            AbstractMetadataProperty thisProperty, 
            Class<ComparatorRule> comparatorRuleTypeConstant,
            MetadataProperty comparatorProperty);
    
    @I18N
    private static native String nullComparatorTypeForNonStringProperty(
            AbstractMetadataProperty thisProperty,
            Class<ComparatorRule> comparatorRuleTypeConstant,
            MetadataProperty comparatorProperty);
    
    @I18N
    private static native String duplicateComparatorRuleProperty(
            AbstractMetadataProperty thisProperty,
            Class<ComparatorRule> comparatorRuleTypeConstant,
            String propertyName);
}
