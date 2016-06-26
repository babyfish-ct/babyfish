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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.Transient;

import org.babyfish.lang.I18N;
import org.babyfish.lang.bytecode.ASMTreeUtils;
import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.model.Association;
import org.babyfish.model.ComparatorRule;
import org.babyfish.model.Contravariance;
import org.babyfish.model.IndexOf;
import org.babyfish.model.KeyOf;
import org.babyfish.model.instrument.metadata.MetadataClass;
import org.babyfish.model.instrument.metadata.MetadataComparatorPart;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.instrument.metadata.spi.AbstractMetadataClass;
import org.babyfish.model.instrument.metadata.spi.AbstractMetadataProperty;
import org.babyfish.model.jpa.JPAModel;
import org.babyfish.model.jpa.instrument.metadata.JPAMetadataProperty;
import org.babyfish.model.jpa.instrument.spi.ASMConstants;
import org.babyfish.model.jpa.metadata.JPAScalarType;
import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.tree.AnnotationNode;
import org.babyfish.org.objectweb.asm.tree.FieldNode;

/**
 * @author Tao Chen
 */
final class JPAMetadataPropertyImpl extends AbstractMetadataProperty implements JPAMetadataProperty {
    
    private static final AnnotationNode DEFAULT_BASIC_ANNOTATION_NODE =
            new AnnotationNode(Opcodes.ASM5, ASMConstants.BASIC_DESCRIPTOR);
    
    int id;
    
    PropertyType propertyType;
    
    JPAScalarType scalarType;
    
    AssociationType associationType;
    
    JPAMetadataClassImpl keyClass;
    
    JPAMetadataClassImpl targetClass;
    
    JPAMetadataPropertyImpl convarianceProperty;
    
    JPAMetadataPropertyImpl oppositeProperty;
    
    JPAMetadataPropertyImpl indexProperty;
    
    JPAMetadataPropertyImpl keyProperty;
    
    JPAMetadataPropertyImpl referenceProperty;
    
    Collection<MetadataComparatorPart> comparatorParts;
    
    boolean deferrable;
    
    boolean absolute;
    
    boolean inverse;
    
    Unresolved unresolved;

    public JPAMetadataPropertyImpl(JPAMetadataClassImpl declaringClass, FieldNode fieldNode) {
        super(declaringClass, fieldNode);
        this.unresolved = new Unresolved();
        this.unresolved.fieldNode = fieldNode;
        this.determinePrimaryAnnoataion();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public PropertyType getPropertyType() {
        return this.propertyType;
    }

    @Override
    public AssociationType getAssociationType() {
        return this.associationType;
    }

    @Override
    public boolean isDeferrable() {
        return this.deferrable;
    }

    @Override
    public boolean isMandatory() {
        return this.scalarType == JPAScalarType.ID || this.scalarType == JPAScalarType.VERSION;
    }

    @Override
    public boolean isAbsolute() {
        return this.absolute;
    }

    @Override
    public MetadataClass getKeyClass() {
        return this.keyClass;
    }

    @Override
    public MetadataClass getTargetClass() {
        return this.targetClass;
    }

    @Override
    public MetadataProperty getIndexProperty() {
        return this.indexProperty;
    }

    @Override
    public MetadataProperty getKeyProperty() {
        return this.keyProperty;
    }

    @Override
    public MetadataProperty getReferenceProperty() {
        return this.referenceProperty;
    }

    @Override
    public MetadataProperty getConvarianceProperty() {
        return this.convarianceProperty;
    }

    @Override
    public MetadataProperty getOppositeProperty() {
        return this.oppositeProperty;
    }

    @Override
    public Collection<MetadataComparatorPart> getComparatorParts() {
        return this.comparatorParts;
    }

    @Override
    public JPAScalarType getScalarType() {
        return this.scalarType;
    }

    @Override
    public boolean isInverse() {
        return this.inverse;
    }

    @Override
    public void finish() {
        this.unresolved = null;
    }
    
    @Override
    protected void setKeyClass(AbstractMetadataClass keyClass) {
        this.keyClass = (JPAMetadataClassImpl)keyClass;
    }

    @Override
    protected void setTargetClass(AbstractMetadataClass targetClass) {
        this.targetClass = (JPAMetadataClassImpl)targetClass;
    }
    
    public void resolveConvarianceProperty() {
        if (this.unresolved.contravarianceFrom == null) {
            return;
        }
        JPAMetadataClassImpl superMetadataClass = (JPAMetadataClassImpl)this.declaringClass.getSuperClass();
        if (superMetadataClass == null) {
            throw new IllegalClassException(
                    contraviancePropertyMissSuperClass(this, Contravariance.class)
            );
        }
        JPAMetadataPropertyImpl superProperty = 
                superMetadataClass.properties.get(this.unresolved.contravarianceFrom);
        if (superProperty == null) {
            throw new IllegalClassException(
                    noConvarianceProperty(
                            this,
                            Contravariance.class,
                            this.unresolved.contravarianceFrom, 
                            superMetadataClass
                    )
            );
        }
        if (!Objects.equals(this.keyTypeName, superProperty.keyTypeName)) {
            throw new IllegalClassException(
                    diffKeyTypeNameOfConvarianceProperty(
                            this, 
                            Contravariance.class,
                            superProperty, 
                            this.keyTypeName, 
                            superProperty.keyTypeName
                    )
            );
        }
        if (this.targetClass == superProperty.targetClass) {
            throw new IllegalClassException(
                    sameTargetTypeNameOfConvarianceProperty(
                            this,
                            Contravariance.class,
                            superProperty
                    )
            );
        }
        boolean concurrentConvariance = false;
        for (JPAMetadataClassImpl superClass = this.targetClass.superClass; 
                superClass != null; 
                superClass = superClass.superClass) {
            if (superClass == superProperty.targetClass) {
                concurrentConvariance = true;
                break;
            }
        }
        if (!concurrentConvariance) {
            throw new IllegalClassException(
                    illegalTypeOfContravarianceProperty(
                            this, 
                            Contravariance.class,
                            superProperty, 
                            this.targetClass, 
                            superProperty.targetClass
                    )
            );
        }
        this.convarianceProperty = superProperty;
    }
    
    public void calculateJoin() {
        if (this.propertyType != PropertyType.ASSOCIATION) {
            return;
        }
        String mappedBy = ASMTreeUtils.getAnnotationValue(this.unresolved.primaryAnnotationNode, "mappedBy", "");
        if (mappedBy.isEmpty()) {
            this.unresolved.join = new JPAMetadataJoin(this);
        } else {
            AnnotationNode joinNode = ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, JoinColumn.class);
            if (joinNode == null) {
                joinNode = ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, JoinColumns.class);
                if (joinNode == null) {
                    joinNode = ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, JoinTable.class);
                }
            }
            if (joinNode != null) {
                throw new IllegalClassException(
                        explicitJoinCanNotBeMappedBy(
                            this,
                            ASMUtils.toClassName(this.unresolved.primaryAnnotationNode.desc),
                            ASMUtils.toClassName(joinNode.desc)
                        )
                );
            }
        }
    }
    
    public void resolveExplicitBidrectionalAssociations() {
        
        if (this.oppositeProperty != null || this.propertyType != PropertyType.ASSOCIATION) {
            return;
        }
        String mappedBy = ASMTreeUtils.getAnnotationValue(this.unresolved.primaryAnnotationNode, "mappedBy", "");
        if (mappedBy.isEmpty()) {
            return;
        }
        this.inverse = true;
        
        JPAMetadataPropertyImpl oppositeProperty = this.targetClass.declaredProperties.get(mappedBy);
        if (oppositeProperty == null) {
            throw new IllegalClassException(
                    explicitOppositeProprtyIsNotExisting(
                            this,
                            ASMUtils.toClassName(this.unresolved.primaryAnnotationNode.desc),
                            mappedBy,
                            this.targetClass
                    )
            );
        }
        if (oppositeProperty == this) {
            throw new IllegalClassException(
                    explicitOppositeProprtyCanNotBeSelf(
                            this,
                            ASMUtils.toClassName(this.unresolved.primaryAnnotationNode.desc),
                            mappedBy
                    )
            );
        }
        if (oppositeProperty.declaringClass != this.targetClass) {
            throw new IllegalClassException(
                    declaringClassOfExplicitOppositePropertyShouldBe(
                            this, 
                            ASMUtils.toClassName(this.unresolved.primaryAnnotationNode.desc),
                            oppositeProperty, 
                            this.targetClass
                    )
            );
        }
        if (oppositeProperty.propertyType != PropertyType.ASSOCIATION) {
            throw new IllegalClassException(
                    explicitOppositePropertyIsNotAssociation(
                            this,
                            ASMUtils.toClassName(this.unresolved.primaryAnnotationNode.desc),
                            mappedBy,
                            oppositeProperty
                    )
            );
        }
        if (oppositeProperty.unresolved.join == null) {
            throw new IllegalClassException(
                    explicitOppositePropertySpecifiedMappedBy(
                            this,
                            ASMUtils.toClassName(this.unresolved.primaryAnnotationNode.desc),
                            mappedBy,
                            oppositeProperty,
                            ASMUtils.toClassName((oppositeProperty.unresolved.primaryAnnotationNode.desc))
                    )
            );
        }
        this.setOppositeProperty(oppositeProperty);
        oppositeProperty.setOppositeProperty(this);
    }
    
    public void resolveImplicitBidrectionalAssociations() {
        
        if (this.oppositeProperty != null ||
                this.propertyType != PropertyType.ASSOCIATION ||
                this.unresolved.join == null) {
            return;
        }
        
        JPAMetadataPropertyImpl oppositeProperty = null;
        for (JPAMetadataPropertyImpl mayBeOppositeProperty : this.targetClass.propertyList) {
            if (this != mayBeOppositeProperty && this.unresolved.join.likeBedirectionalAssociation(mayBeOppositeProperty.unresolved.join)) {
                if (mayBeOppositeProperty.getDeclaringClass() != this.targetClass) {
                    throw new IllegalClassException(
                            declaringClassOfImplicitOppositePropertyShouldBe(
                                    this,
                                    mayBeOppositeProperty,
                                    this.targetClass
                            )
                    );
                }
                if (oppositeProperty != null) {
                    throw new IllegalClassException(
                            conflictOppositeProperties(
                                    this,
                                    oppositeProperty,
                                    mayBeOppositeProperty
                            )
                    );
                }
                oppositeProperty = mayBeOppositeProperty;
            }
        }
        if (oppositeProperty == null) {
            return;
        }
        boolean thisReadonly = this.unresolved.join.isReadonly();
        boolean oppositeReadonly = oppositeProperty.unresolved.join.isReadonly();
        if (thisReadonly) {
            this.inverse = true;
        } else if (oppositeReadonly) {
            oppositeProperty.inverse = true;
        }
        this.setOppositeProperty(oppositeProperty);
        oppositeProperty.setOppositeProperty(this);
    }
    
    public void resolveIndexPropertyOfOpposite() {
        if (this.oppositeProperty == null || this.unresolved.orderColumn == null) {
            return;
        }
        JPAMetadataPropertyImpl oppositeIndexProperty = null;
        String laxOrderColumnName = DbIdentifiers.laxIdentifier(this.unresolved.orderColumn);
        for (JPAMetadataPropertyImpl mayBeOppositeIndexProperty : this.targetClass.propertyList) {
            String otherColumnName = mayBeOppositeIndexProperty.unresolved.singleColumnName;
            if (otherColumnName != null) {
                if (DbIdentifiers.laxIdentifier(otherColumnName).equals(laxOrderColumnName)) {
                    if (mayBeOppositeIndexProperty.getDeclaringClass() != this.targetClass) {
                        throw new IllegalClassException(
                                declaringClassOfOppositeIndexPropertyShouldBe(
                                        this,
                                        OrderColumn.class,
                                        mayBeOppositeIndexProperty,
                                        this.targetClass
                                )
                        );
                    }
                    if (oppositeIndexProperty != null) {
                        throw new IllegalClassException(
                                conflictOppositeIndexProperties(
                                        this,
                                        OrderColumn.class,
                                        oppositeIndexProperty,
                                        mayBeOppositeIndexProperty
                                )
                        );
                    }
                    oppositeIndexProperty = mayBeOppositeIndexProperty;
                }
            }
        }
        
        String mappedBy = ASMTreeUtils.getAnnotationValue(this.unresolved.primaryAnnotationNode, "mappedBy", "");
        if (oppositeIndexProperty != null) {
            if (!this.unresolved.primaryAnnotationNode.desc.equals(ASMConstants.ONE_TO_MANY_DESCRIPTOR)) {
                throw new IllegalClassException(
                        oneToManyIsRequiredWhenOppositeIndexPropertyIsFound(
                                this,
                                OrderColumn.class,
                                oppositeIndexProperty,
                                OneToMany.class
                        )
                );
            }
            if (mappedBy.isEmpty()) {
                throw new IllegalClassException(
                        mappedByIsRequiredWhenOppositeIndexPropertyIsFound(
                                this,
                                OrderColumn.class,
                                oppositeIndexProperty,
                                OneToMany.class
                        )
                );
            }
            if (!oppositeIndexProperty.getDescriptor().equals("I")) {
                throw new IllegalClassException(
                        typeOfOppositeIndexPropertyShouldBeInteger(
                                this,
                                OrderColumn.class,
                                oppositeIndexProperty
                        )
                );
            }
            this.oppositeProperty.indexProperty = oppositeIndexProperty;
            oppositeIndexProperty.setReferenceProperty(this.oppositeProperty);
        } else if (!mappedBy.isEmpty()) {
            throw new IllegalClassException(
                    mappedByIsForbiddenWhenOppositeIndexPropertyIsNotFound(
                            this,
                            OrderColumn.class,
                            this.targetClass,
                            ASMUtils.toClassName(this.unresolved.primaryAnnotationNode.desc)
                    )
            );
        }
    }
    
    public void resolveKeyPropertyOfOpposite() {
        if (this.oppositeProperty == null || (this.unresolved.mapKeyName == null && this.unresolved.mapKeyColumnName == null)) {
            return;
        }
        JPAMetadataPropertyImpl oppositeKeyProperty = null;
        if (this.unresolved.mapKeyName != null) {
            oppositeKeyProperty = this.targetClass.declaredProperties.get(this.unresolved.mapKeyName);
            if (oppositeKeyProperty == null) {
                throw new IllegalClassException(
                        canNotFindOppositeKeyPropertyByMapKey(
                                this,
                                MapKey.class,
                                this.unresolved.mapKeyName,
                                this.targetClass
                        )
                );
            }
        } else if (this.unresolved.mapKeyColumnName != null) {
            String laxKeyColumnName = DbIdentifiers.laxIdentifier(this.unresolved.mapKeyColumnName);
            for (JPAMetadataPropertyImpl mayBeOppositeKeyProperty : this.targetClass.propertyList) {
                String otherColumnName = mayBeOppositeKeyProperty.unresolved.singleColumnName;
                if (otherColumnName != null) {
                    if (DbIdentifiers.laxIdentifier(otherColumnName).equals(laxKeyColumnName)) {
                        if (mayBeOppositeKeyProperty.getDeclaringClass() != this.targetClass) {
                            throw new IllegalClassException(
                                    declaringClassOfOppositeKeyPropertyShouldBe(
                                            this, 
                                            MapKeyColumn.class,
                                            mayBeOppositeKeyProperty, 
                                            this.targetClass
                                    )
                            );
                        }
                        if (oppositeKeyProperty != null) {
                            throw new IllegalClassException(
                                    conflictOppositeKeyProperties(
                                            this, 
                                            MapKeyColumn.class,
                                            oppositeKeyProperty, 
                                            mayBeOppositeKeyProperty
                                    )
                            );
                        }
                        oppositeKeyProperty = mayBeOppositeKeyProperty;
                    }
                }
            }
        }
        
        String mappedBy = ASMTreeUtils.getAnnotationValue(this.unresolved.primaryAnnotationNode, "mappedBy", "");
        if (oppositeKeyProperty != null) {
            if (!this.unresolved.primaryAnnotationNode.desc.equals(ASMConstants.ONE_TO_MANY_DESCRIPTOR)) {
                throw new IllegalClassException(
                        oneToManyIsRequiredWhenOppositeKeyPropertyIsFound(
                                this,
                                this.unresolved.mapKeyName != null ? MapKey.class : MapKeyColumn.class,
                                oppositeKeyProperty,
                                OneToMany.class
                        )
                );
            }
            if (mappedBy.isEmpty()) {
                throw new IllegalClassException(
                        mappedByIsRequiredWhenOppositeKeyPropertyIsFound(
                                this,
                                this.unresolved.mapKeyName != null ? MapKey.class : MapKeyColumn.class,
                                oppositeKeyProperty,
                                OneToMany.class
                        )
                );
            }
            if (!this.keyDescriptor.equals(oppositeKeyProperty.getDescriptor())) {
                throw new IllegalClassException(
                        typeOfOppositeKeyPropertyShouldBe(
                                this,
                                this.unresolved.mapKeyName != null ? MapKey.class : MapKeyColumn.class,
                                oppositeKeyProperty,
                                ASMUtils.toClassName(this.keyDescriptor)
                        )
                );
            }
            this.oppositeProperty.keyProperty = oppositeKeyProperty;
            oppositeKeyProperty.setReferenceProperty(this.oppositeProperty);
        } else if (!mappedBy.isEmpty()) {
            throw new IllegalClassException(
                    mappedByIsForbiddenWhenOppositeKeyPropertyIsNotFound(
                            this,
                            MapKeyColumn.class,
                            this.targetClass,
                            ASMUtils.toClassName(this.unresolved.primaryAnnotationNode.desc)
                    )
            );
        }
    }
    
    public void resolveStateAfterAssociationResolved() {
        
        if (this.propertyType == PropertyType.ASSOCIATION) {
            if (this.oppositeProperty != null) {
                if (this.inverse && this.oppositeProperty.inverse) {
                    throw new IllegalClassException(
                            oppositePropertyShouldNotBeInverse(this, Association.class, this.oppositeProperty)
                    );
                }
                if (!this.inverse && !this.oppositeProperty.inverse) {
                    throw new IllegalClassException(
                            oppositePropertyShouldBeInverse(this, Association.class, this.oppositeProperty)
                    );
                }
            }
            if (this.standardCollectionType == null) {
                if (this.indexProperty != null) {
                    this.associationType = AssociationType.INDEXED_REFERENCE;
                } else if (this.keyProperty != null) {
                    this.associationType = AssociationType.KEYED_REFERENCE;
                } else {
                    this.associationType = AssociationType.REFERENCE;
                }
            } else if (this.isCollection(List.class)) {
                this.associationType = AssociationType.LIST;
            } else if (this.isCollection(Map.class)) {
                this.associationType = AssociationType.MAP;
            } else {
                this.associationType = AssociationType.COLLECTION;
            }
        } else if (this.referenceProperty != null) {
            if (this.propertyType != null) {
                throw new AssertionError("Internal bug");
            }
            if (this.referenceProperty.indexProperty == this) {
                this.propertyType = PropertyType.INDEX;
            } else {
                this.propertyType = PropertyType.KEY;
            }
        }
        if (this.propertyType == null) {
            this.propertyType = PropertyType.SCALAR;
        }
        if (this.associationType == null) {
            this.associationType = AssociationType.NONE;
        }
        if (this.scalarType == null) {
            this.scalarType = 
                    this.propertyType == PropertyType.SCALAR ? 
                            JPAScalarType.GENERAL : 
                            JPAScalarType.NONE;
        }
        
        switch (this.unresolved.primaryAnnotationNode.desc) {
        case ASMConstants.MANY_TO_MANY_DESCRIPTOR:
        case ASMConstants.ONE_TO_MANY_DESCRIPTOR:
            if (ASMTreeUtils.getAnnotationEnumValue(FetchType.class, this.unresolved.primaryAnnotationNode, "fetch") != FetchType.EAGER) {
                this.deferrable = true;
            }
            break;
        case ASMConstants.MANY_TO_ONE_DESCRIPTOR:
        case ASMConstants.ONE_TO_ONE_DESCRIPTOR:
        case ASMConstants.BASIC_DESCRIPTOR:
            if (ASMTreeUtils.getAnnotationEnumValue(FetchType.class, this.unresolved.primaryAnnotationNode, "fetch") == FetchType.LAZY) {
                this.deferrable = true;
            }
            break;
        }
        
        AnnotationNode indexOfNode = ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, IndexOf.class);
        if (indexOfNode != null) {
            if (this.propertyType != PropertyType.INDEX) {
                throw new IllegalClassException(
                        requireIndexOrKeyAnnotation(
                                this,
                                this.referenceProperty,
                                IndexOf.class
                        )
                );
            }
            String value = ASMTreeUtils.getAnnotationValue(indexOfNode, "value");
            if (!this.referenceProperty.getName().equals(value)) {
                throw new IllegalClassException(
                        illegalIndexOrKeyAnnotationValue(
                                this,
                                this.referenceProperty,
                                IndexOf.class,
                                this.referenceProperty.getName()
                        )
                );
            }
            this.absolute = ASMTreeUtils.getAnnotationValue(indexOfNode, "absolute", false);
        } else if (this.propertyType == PropertyType.INDEX) {
            throw new IllegalClassException(
                    unexpectedIndexOrKeyAnnotation(
                            this,
                            IndexOf.class
                    )
            );
        }
        
        AnnotationNode keyOfNode = ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, KeyOf.class);
        if (keyOfNode != null) {
            if (this.propertyType != PropertyType.KEY) {
                throw new IllegalClassException(
                        requireIndexOrKeyAnnotation(
                                this,
                                this.referenceProperty,
                                KeyOf.class
                        )
                );
            }
            String value = ASMTreeUtils.getAnnotationValue(keyOfNode, "value");
            if (!this.referenceProperty.getName().equals(value)) {
                throw new IllegalClassException(
                        illegalIndexOrKeyAnnotationValue(
                                this,
                                this.referenceProperty,
                                KeyOf.class,
                                this.referenceProperty.getName()
                        )
                );
            }
            this.absolute = ASMTreeUtils.getAnnotationValue(keyOfNode, "absolute", false);
        } else if (this.propertyType == PropertyType.KEY) {
            System.err.println(">>>> miss @KeyOf: " + this.declaringClass.getName());
            throw new IllegalClassException(
                    unexpectedIndexOrKeyAnnotation(
                            this,
                            KeyOf.class
                    )
            );
        }
    }
    
    public void resolveComparatorParts(ObjectModel4JPAInstrumenter instrumenter) {
        this.comparatorParts = this.determineComparatorParts(this.unresolved.fieldNode, instrumenter);
    }
    
    private void determinePrimaryAnnoataion() {
        FieldNode fieldNode = this.unresolved.fieldNode;
        if (fieldNode.visibleAnnotations != null) {
            for (AnnotationNode annotationNode : fieldNode.visibleAnnotations) {
                switch (annotationNode.desc) {
                case ASMConstants.MANY_TO_MANY_DESCRIPTOR:
                    this.setPrimaryAnnotationNode(annotationNode);
                    this.propertyType = PropertyType.ASSOCIATION;
                    break;
                case ASMConstants.ONE_TO_MANY_DESCRIPTOR:
                    this.setPrimaryAnnotationNode(annotationNode);
                    this.propertyType = PropertyType.ASSOCIATION;
                    break;
                case ASMConstants.MANY_TO_ONE_DESCRIPTOR:
                    this.setPrimaryAnnotationNode(annotationNode);
                    this.propertyType = PropertyType.ASSOCIATION;
                    break;
                case ASMConstants.ONE_TO_ONE_DESCRIPTOR:
                    this.setPrimaryAnnotationNode(annotationNode);
                    this.propertyType = PropertyType.ASSOCIATION;
                    break;
                case ASMConstants.EMBEDED_DESCRIPTOR:
                    this.setPrimaryAnnotationNode(annotationNode);
                    break;
                case ASMConstants.BASIC_DESCRIPTOR:
                    this.setPrimaryAnnotationNode(annotationNode);
                    break;
                case ASMConstants.ID_DESCRIPTOR:
                    this.setPrimaryAnnotationNode(annotationNode);
                    this.propertyType = PropertyType.SCALAR;
                    this.scalarType = JPAScalarType.ID;
                    break;
                case ASMConstants.VERSION_DESCRIPTOR:
                    this.setPrimaryAnnotationNode(annotationNode);
                    this.propertyType = PropertyType.SCALAR;
                    this.scalarType = JPAScalarType.VERSION;
                    break;
                default:
                    // TODO: visible annotation must be under package "javax.persistence"
                }
            }
        }
        
        if (fieldNode.invisibleAnnotations != null) {
            for (AnnotationNode annotationNode : fieldNode.invisibleAnnotations) {
                if (ASMConstants.CONTRAVARIANCE_DESCRIPTOR.equals(annotationNode.desc)) {
                    this.setPrimaryAnnotationNode(annotationNode);
                    this.propertyType = PropertyType.CONTRAVARIANCE;
                    if (ASMTreeUtils.getAnnotationNode(fieldNode, Transient.class) == null) {
                        throw new IllegalClassException(
                                contravariancePropertyMustBeTransient(
                                        this,
                                        Contravariance.class,
                                        Transient.class
                                )
                        );
                    }
                    this.unresolved.contravarianceFrom = 
                            ASMTreeUtils.getAnnotationValue(annotationNode, "from", fieldNode.name);
                    // Don't break
                }
                if (ASMConstants.COMPARATOR_RULE_DESCRIPTOR.equals(annotationNode.desc)) {
                    if (this.unresolved.contravarianceFrom != null) {
                        throw new IllegalClassException(
                                conflictAnnotations(this, Contravariance.class.getName(), ComparatorRule.class.getName())
                        );
                    }
                }
            }
        }
        
        if (this.unresolved.primaryAnnotationNode == null) {
            this.setPrimaryAnnotationNode(DEFAULT_BASIC_ANNOTATION_NODE);
        }
    }
    
    private void setPrimaryAnnotationNode(AnnotationNode primaryAnnotationNode) {
        
        if (this.unresolved.primaryAnnotationNode != null) {
            throw new IllegalClassException(
                    conflictAnnotations(
                            this,
                            ASMUtils.toClassName(this.unresolved.primaryAnnotationNode.desc),
                            ASMUtils.toClassName(primaryAnnotationNode.desc)
                    )
            );
        }
        
        if (this.propertyType == PropertyType.ASSOCIATION &&
                ASMTreeUtils.getAnnotationValue(primaryAnnotationNode, "targetEntity") != null) {
            throw new IllegalClassException(
                    targetEntityCanNotBeSpecified(
                            this,
                            ASMUtils.toClassName(primaryAnnotationNode.desc),
                            this.declaringClass,
                            JPAModel.class
                    )
            );
        }
        
        switch (primaryAnnotationNode.desc) {
        case ASMConstants.ONE_TO_MANY_DESCRIPTOR:
        case ASMConstants.MANY_TO_MANY_DESCRIPTOR:
            if (this.standardCollectionType == null) {
                throw new IllegalClassException(
                        requireCollectionField(
                                this,
                                ASMUtils.toClassName(primaryAnnotationNode.desc)
                        )
                );
            }
            break;
        case ASMConstants.CONTRAVARIANCE_DESCRIPTOR:
            break;
        default:
            if (this.standardCollectionType != null) {
                throw new IllegalClassException(
                        requireNonCollectionField(
                                this, 
                                ASMUtils.toClassName(primaryAnnotationNode.desc)
                        )
                );
            }
        }   
        if (ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, PrimaryKeyJoinColumn.class) != null) {
            throw new IllegalClassException(
                    unsupportedAnnoatation(
                            this,
                            PrimaryKeyJoinColumn.class
                    )
            );
        }
        if (ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, PrimaryKeyJoinColumns.class) != null) {
            throw new IllegalClassException(
                    unsupportedAnnoatation(
                            this,
                            PrimaryKeyJoinColumns.class
                    )
            );
        }
        
        AnnotationNode orderColumnAnnotationNode = ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, OrderColumn.class);
        if (orderColumnAnnotationNode != null) {
            if (primaryAnnotationNode.desc.equals(ASMConstants.CONTRAVARIANCE_DESCRIPTOR)) {
                throw new IllegalClassException(
                        conflictAnnotations(this, OrderColumn.class.getName(), Contravariance.class.getName())
                );
            }
            if (!this.isCollection(List.class)) {
                throw new IllegalClassException(
                        annotationRequireFieldType(
                                this,
                                OrderColumn.class,
                                List.class
                        )
                );
            }
            String orderColumn = ASMTreeUtils.getAnnotationValue(orderColumnAnnotationNode, "name", "");
            if (orderColumn.isEmpty()) {
                throw new IllegalClassException(
                        annotationArgumentCanNotBeEmpty(
                                this,
                                OrderColumn.class,
                                "name"
                        )
                );
            }
            if (primaryAnnotationNode.desc.equals(ASMConstants.ONE_TO_MANY_DESCRIPTOR)) {
                this.unresolved.orderColumn = orderColumn;
            }
        } else if (this.isCollection(List.class) && !primaryAnnotationNode.desc.equals(ASMConstants.CONTRAVARIANCE_DESCRIPTOR)) {
            throw new IllegalClassException(
                    fieldTypeRequireAnnotation(
                            this,
                            List.class,
                            OrderColumn.class,
                            Contravariance.class
                    )
            );
        }
        
        AnnotationNode mapKeyAnnotationNode = ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, MapKey.class);
        AnnotationNode mapKeyColumnAnnotationNode = ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, MapKeyColumn.class);
        if (mapKeyAnnotationNode != null) {
            if (primaryAnnotationNode.desc.equals(ASMConstants.CONTRAVARIANCE_DESCRIPTOR)) {
                throw new IllegalClassException(
                        conflictAnnotations(this, MapKey.class.getName(), Contravariance.class.getName())
                );
            }
            if (!this.isCollection(Map.class)) {
                throw new IllegalClassException(
                        annotationRequireFieldType(
                                this,
                                MapKey.class,
                                Map.class
                        )
                );
            }
            if (!primaryAnnotationNode.desc.equals(ASMConstants.ONE_TO_MANY_DESCRIPTOR)) {
                throw new IllegalClassException(
                        mapKeyRequireOneToMany(
                                this,
                                MapKey.class,
                                OneToMany.class
                        )
                );
            }
            this.unresolved.mapKeyName = ASMTreeUtils.getAnnotationValue(mapKeyAnnotationNode, "name", "");
            if (this.unresolved.mapKeyName.isEmpty()) {
                throw new IllegalClassException(
                        annotationArgumentCanNotBeEmpty(
                                this,
                                MapKey.class,
                                "name"
                        )
                );
            }
        }
        if (mapKeyColumnAnnotationNode != null) {
            if (!this.isCollection(Map.class)) {
                throw new IllegalClassException(
                        annotationRequireFieldType(
                                this,
                                MapKeyColumn.class,
                                Map.class
                        )
                );
            }
            String mapKeyColumnName = ASMTreeUtils.getAnnotationValue(mapKeyColumnAnnotationNode, "name", "");
            if (mapKeyColumnName.isEmpty()) {
                throw new IllegalClassException(
                        annotationArgumentCanNotBeEmpty(
                                this,
                                MapKeyColumn.class,
                                "name"
                        )
                );
            }
            this.unresolved.mapKeyColumnName = mapKeyColumnName;
        }
        if (mapKeyAnnotationNode != null && mapKeyColumnAnnotationNode != null) {
            throw new IllegalClassException(
                    conflictAnnotations(
                            this, 
                            MapKey.class.getName(), 
                            MapKeyColumn.class.getName()
                    )
            );
        } else if (mapKeyAnnotationNode == null && mapKeyColumnAnnotationNode == null && this.isCollection(Map.class)) {
            throw new IllegalClassException(
                    fieldTypeRequireAnnotation(
                            this,
                            Map.class,
                            MapKey.class,
                            MapKeyColumn.class,
                            Contravariance.class
                    )
            );
        }
        
        if (primaryAnnotationNode.desc.equals(ASMConstants.BASIC_DESCRIPTOR) ||
                primaryAnnotationNode.desc.equals(ASMConstants.ID_DESCRIPTOR) ||
                primaryAnnotationNode.desc.equals(ASMConstants.VERSION_DESCRIPTOR)) {
            AnnotationNode columnNode = ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, Column.class);
            String columnName = "";
            if (columnNode != null) {
                columnName = ASMTreeUtils.getAnnotationValue(columnNode, "name", "");
            }
            if (columnName.isEmpty()) {
                columnName = this.name;
            }
            this.unresolved.singleColumnName = columnName;
        }
        
        this.unresolved.primaryAnnotationNode = primaryAnnotationNode;
    }
    
    private void setOppositeProperty(JPAMetadataPropertyImpl oppositeProperty) {
        if (this.oppositeProperty != null) {
            throw new IllegalClassException(
                    conflictOppositeProperties(
                            this,
                            this.oppositeProperty,
                            oppositeProperty
                    )
            );
        }
        this.oppositeProperty = oppositeProperty;
    }
    
    private void setReferenceProperty(JPAMetadataPropertyImpl referenceProperty) {
        if (this.referenceProperty != null) {
            throw new IllegalClassException(
                    referencePropertyHasBeenUsed(
                            this, 
                            this.referenceProperty,
                            this.referenceProperty.indexProperty != null ? this.referenceProperty.indexProperty : this.referenceProperty.keyProperty
                    )
            );
        }
        this.referenceProperty = referenceProperty;
    }
    
    private boolean isCollection(Class<?> collectionType) {
        if (this.standardCollectionType == null) {
            return false;
        }
        return collectionType.isAssignableFrom(this.standardCollectionType);
    }
    
    static class Unresolved {
        
        FieldNode fieldNode;
        
        AnnotationNode primaryAnnotationNode;
        
        String contravarianceFrom;
        
        String singleColumnName;
        
        JPAMetadataJoin join;
        
        String orderColumn;
        
        String mapKeyName;
        
        String mapKeyColumnName;
    }
    
    @I18N    
    private static native String conflictAnnotations(
            JPAMetadataPropertyImpl property,
            String annotationTypeName1, 
            String annotationTypeName2);

    @I18N    
    private static native String contravariancePropertyMustBeTransient(
            JPAMetadataPropertyImpl property,
            Class<Contravariance> contravarianceTypeConstant, 
            Class<Transient> transientTypeConstant);
        
    @I18N    
    private static native String targetEntityCanNotBeSpecified(
            JPAMetadataPropertyImpl property, 
            String annotationTypeName,
            AbstractMetadataClass declaringClass, 
            Class<JPAModel> jpaModelTypeConstant);
        
    @I18N    
    private static native String requireCollectionField(
            JPAMetadataPropertyImpl property,
            String primaryAnnotationTypeName);
        
    @I18N    
    private static native String requireNonCollectionField(
            JPAMetadataPropertyImpl property,
            String primaryAnnotationTypeName);
        
    @I18N    
    private static native String explicitJoinCanNotBeMappedBy(
            JPAMetadataPropertyImpl property, 
            String associationAnnotationTypeName,
            String joinAnnotationTypeName);
        
    @I18N    
    private static native String explicitOppositeProprtyIsNotExisting(
            JPAMetadataPropertyImpl property, 
            String associationAnnotationTypeName,
            String mappedBy, 
            JPAMetadataClassImpl targetClass);
        
    @I18N    
    private static native String explicitOppositeProprtyCanNotBeSelf(
            JPAMetadataPropertyImpl property, 
            String associationAnnotationTypeName,
            String mappedBy);
    
    @I18N
    private static native String declaringClassOfExplicitOppositePropertyShouldBe(
            JPAMetadataPropertyImpl property, 
            String primaryAnnotationNodeClassName,
            JPAMetadataPropertyImpl oppositeproperty, 
            MetadataClass targetClass);
        
    @I18N    
    private static native String explicitOppositePropertyIsNotAssociation(
            JPAMetadataPropertyImpl property, 
            String associationAnnotationTypeName,
            String mappedBy, 
            JPAMetadataPropertyImpl oppositeProperty);
        
    @I18N    
    private static native String explicitOppositePropertySpecifiedMappedBy(
            JPAMetadataPropertyImpl property, 
            String associationAnnotationTypeName,
            String mappedBy, 
            JPAMetadataPropertyImpl oppositeProperty,
            String oppositeAssociationAnnotationTypeName);
    
    @I18N
    private static native String contraviancePropertyMissSuperClass(
            JPAMetadataPropertyImpl property,
            Class<Contravariance> contravarianceTypeConstant);
    
    @I18N
    private static native String noConvarianceProperty(
            JPAMetadataPropertyImpl property,
            Class<Contravariance> contravarianceTypeConstant,
            String contravarianceFrom,
            MetadataClass superClass);
    
    @I18N
    private static native String diffKeyTypeNameOfConvarianceProperty(
            JPAMetadataPropertyImpl property,
            Class<Contravariance> contravarianceTypeConstant,
            JPAMetadataPropertyImpl convarianceProperty,
            String keyTypeName, 
            String convarianceKeyTypeName);
    
    @I18N
    private static native String sameTargetTypeNameOfConvarianceProperty(
            JPAMetadataPropertyImpl property,
            Class<Contravariance> contravarianceTypeConstant,
            JPAMetadataPropertyImpl convarianceProperty);
    
    @I18N
    private static native String illegalTypeOfContravarianceProperty(
            JPAMetadataPropertyImpl property,
            Class<Contravariance> contravarianceTypeConstant,
            JPAMetadataPropertyImpl convarianceProperty,
            MetadataClass targetClass, 
            MetadataClass convarianceTargetClass);
    
    @I18N
    private static native String declaringClassOfImplicitOppositePropertyShouldBe(
            JPAMetadataPropertyImpl property,
            JPAMetadataPropertyImpl mayBeOppositeProperty,
            MetadataClass thisTargetClass);
    
    @I18N
    private static native String conflictOppositeProperties(
            JPAMetadataPropertyImpl property,
            JPAMetadataPropertyImpl oppositeProperty1,
            JPAMetadataPropertyImpl oppositeProperty2);
    
    @I18N
    private static native String declaringClassOfOppositeIndexPropertyShouldBe(
            JPAMetadataPropertyImpl property,
            Class<OrderColumn> orderColumnTypeConstant,
            JPAMetadataPropertyImpl mayBeOppositeIndexProperty,
            MetadataClass thisTargetClass);
    
    @I18N
    private static native String conflictOppositeIndexProperties(
            JPAMetadataPropertyImpl property,
            Class<OrderColumn> orderColumnTypeConstant,
            JPAMetadataPropertyImpl oppositeIndexProperty1,
            JPAMetadataPropertyImpl oppositeIndexProperty2);
    
    @I18N
    private static native String oneToManyIsRequiredWhenOppositeIndexPropertyIsFound(
            JPAMetadataPropertyImpl property,
            Class<OrderColumn> orderColumnTypeConstant,
            JPAMetadataPropertyImpl oppositeIndexProperty,
            Class<OneToMany> oneToManyTypeConstant);
    
    @I18N
    private static native String mappedByIsRequiredWhenOppositeIndexPropertyIsFound(
            JPAMetadataPropertyImpl property,
            Class<OrderColumn> orderColumnTypeConstant,
            JPAMetadataPropertyImpl oppositeIndexProperty,
            Class<OneToMany> oneToManyTypeConstant);
    
    @I18N
    private static native String typeOfOppositeIndexPropertyShouldBeInteger(
            JPAMetadataPropertyImpl property,
            Class<OrderColumn> orderColumnTypeConstant,
            JPAMetadataPropertyImpl oppositeIndexProperty);
    
    @I18N
    private static native String mappedByIsForbiddenWhenOppositeIndexPropertyIsNotFound(
            JPAMetadataPropertyImpl property,
            Class<OrderColumn> orderColumnTypeConstant,
            MetadataClass targetClass,
            String primaryAnnotationClassName);
    
    @I18N
    private static native String canNotFindOppositeKeyPropertyByMapKey(
            JPAMetadataPropertyImpl property,
            Class<MapKey> mapKeyTypeConstant,
            String mapKeyName,
            MetadataClass targetClass
    );
    
    @I18N
    private static native String declaringClassOfOppositeKeyPropertyShouldBe(
            JPAMetadataPropertyImpl property,
            Class<MapKeyColumn> mapKeyColumnTypeConstant,
            JPAMetadataPropertyImpl mayBeOppositeKeyProperty,
            MetadataClass thisTargetClass);
    
    @I18N
    private static native String conflictOppositeKeyProperties(
            JPAMetadataPropertyImpl property,
            Class<MapKeyColumn> mapKeyColumnTypeConstant,
            JPAMetadataPropertyImpl oppositeKeyProperty1,
            JPAMetadataPropertyImpl oppositeKeyProperty2);
    
    @I18N
    private static native String oneToManyIsRequiredWhenOppositeKeyPropertyIsFound(
            JPAMetadataPropertyImpl property,
            Class<? extends Annotation> mapKeyAnnotationType,
            JPAMetadataPropertyImpl oppositeIndexProperty,
            Class<OneToMany> oneToManyTypeConstant);
    
    @I18N
    private static native String mappedByIsRequiredWhenOppositeKeyPropertyIsFound(
            JPAMetadataPropertyImpl property,
            Class<? extends Annotation> mapKeyAnnotationType,
            JPAMetadataPropertyImpl oppositeIndexProperty,
            Class<OneToMany> oneToManyTypeConstant);
    
    @I18N
    private static native String typeOfOppositeKeyPropertyShouldBe(
            JPAMetadataPropertyImpl property,
            Class<? extends Annotation> mapKeyOrMapKeyColumnType,
            JPAMetadataPropertyImpl oppositeKeyProperty,
            String keyClassName);
    
    @I18N
    private static native String mappedByIsForbiddenWhenOppositeKeyPropertyIsNotFound(
            JPAMetadataPropertyImpl property,
            Class<MapKeyColumn> mapKeyColumnTypeConstant,
            MetadataClass targetClass,
            String primaryAnnotationClassName);
    
    @I18N
    private static native String oppositePropertyShouldBeInverse(
            JPAMetadataPropertyImpl property,
            Class<Association> associationTypeConstant,
            JPAMetadataPropertyImpl oppositeProperty);
    
    @I18N
    private static native String oppositePropertyShouldNotBeInverse(
            JPAMetadataPropertyImpl property,
            Class<Association> associationTypeConstant,
            JPAMetadataPropertyImpl oppositeProperty);
    
    @I18N
    private static native String requireIndexOrKeyAnnotation(
            JPAMetadataPropertyImpl property,
            JPAMetadataPropertyImpl referenceProperty,
            Class<? extends Annotation> inexOfKeyAnnotationType);
    
    @I18N
    private static native String illegalIndexOrKeyAnnotationValue(
            JPAMetadataPropertyImpl property,
            JPAMetadataPropertyImpl referenceProperty,
            Class<? extends Annotation> inexOfKeyAnnotationType,
            String referencePropertyName);
    
    @I18N
    private static native String unexpectedIndexOrKeyAnnotation(
            JPAMetadataPropertyImpl property,
            Class<? extends Annotation> inexOfKeyAnnotationType);
    
    @I18N
    private static native String unsupportedAnnoatation(
            JPAMetadataPropertyImpl property,
            Class<? extends Annotation> unsupportedAnnotationType);
    
    @I18N
    private static native String mapKeyRequireOneToMany(
            JPAMetadataPropertyImpl property,
            Class<MapKey> mapKeyConstant,
            Class<OneToMany> oneToManyTypeConstant);
    
    @I18N
    private static native String annotationRequireFieldType(
            JPAMetadataPropertyImpl property,
            Class<? extends Annotation> annotationType,
            Class<?> expectedFieldType);
    
    @SafeVarargs
    @I18N
    private static native String fieldTypeRequireAnnotation(
            JPAMetadataPropertyImpl property,
            Class<?> fieldType,
            Class<? extends Annotation> ... expectedAnnotationTypes);
    
    @I18N
    private static native String annotationArgumentCanNotBeEmpty(
            JPAMetadataPropertyImpl property,
            Class<? extends Annotation> annotationType,
            String parameterName);
    
    @I18N
    private static native String referencePropertyHasBeenUsed(
            JPAMetadataPropertyImpl property,
            JPAMetadataPropertyImpl referenceProperty,
            JPAMetadataPropertyImpl otherIndexOrKeyProperty);
}
