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
package org.babyfish.model.instrument.impl;

import java.lang.annotation.Annotation;
import java.util.Collection;

import org.babyfish.lang.I18N;
import org.babyfish.lang.bytecode.ASMTreeUtils;
import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.model.Association;
import org.babyfish.model.Contravariance;
import org.babyfish.model.IndexOf;
import org.babyfish.model.KeyOf;
import org.babyfish.model.ModelType;
import org.babyfish.model.Scalar;
import org.babyfish.model.instrument.metadata.MetadataClass;
import org.babyfish.model.instrument.metadata.MetadataComparatorPart;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.instrument.metadata.spi.AbstractMetadataClass;
import org.babyfish.model.instrument.metadata.spi.AbstractMetadataProperty;
import org.babyfish.model.instrument.spi.AbstractObjectModelInstrumenter;
import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.org.objectweb.asm.tree.AnnotationNode;
import org.babyfish.org.objectweb.asm.tree.FieldNode;

import com.google.common.base.Objects;

/**
 * @author Tao Chen
 */
class MetadataPropertyImpl extends AbstractMetadataProperty {
        
    int id;
    
    PropertyType propertyType;
    
    AssociationType associationType;
    
    boolean deferrable;
    
    boolean mandatory;
    
    boolean absolute;
    
    MetadataClassImpl keyClass;
    
    MetadataClassImpl targetClass;
    
    MetadataPropertyImpl referenceProperty;
    
    MetadataPropertyImpl indexProperty;
    
    MetadataPropertyImpl keyProperty;
    
    MetadataPropertyImpl convarianceProperty;
    
    MetadataPropertyImpl oppositeProperty;
    
    Collection<MetadataComparatorPart> comparatorParts;
    
    Unresolved unresolved;
    
    public MetadataPropertyImpl(AbstractMetadataClass declaringClass, FieldNode fieldNode) {
        super(declaringClass, fieldNode);
        if ((fieldNode.access & Opcodes.ACC_STATIC) != 0) {
            throw new IllegalClassException(
                    illegalModifier(this, "static")
            );
        }
        if ((fieldNode.access & Opcodes.ACC_FINAL) != 0) {
            throw new IllegalClassException(
                    illegalModifier(this, "final")
            );
        }
        if ((fieldNode.access & Opcodes.ACC_PRIVATE) == 0) {
            throw new IllegalClassException(
                    requireModifier(this, "private")
            );
        }
        this.unresolved = new Unresolved();
        this.unresolved.fieldNode = fieldNode;
    }
    
    public void init() {
        AnnotationNode primaryAnnotationNode = this.determinPrimaryAnnotationNode(
                ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, Scalar.class),
                ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, Association.class),
                ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, IndexOf.class),
                ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, KeyOf.class),
                ASMTreeUtils.getAnnotationNode(this.unresolved.fieldNode, Contravariance.class)
        );
        if (primaryAnnotationNode.desc.equals(Type.getDescriptor(Scalar.class))) {
            this.propertyType = PropertyType.SCALAR;
            this.deferrable = ASMTreeUtils.getAnnotationValue(primaryAnnotationNode, "deferrable", false);
            this.mandatory = ASMTreeUtils.getAnnotationValue(primaryAnnotationNode, "mandatory", false);
        } else if (primaryAnnotationNode.desc.equals(Type.getDescriptor(Association.class))) {
            if (this.declaringClass.getModelType() != ModelType.REFERENCE) {
                throw new IllegalClassException(
                        associationMustBeDeclaringInReferenceModelType(
                                this,
                                Association.class,
                                ModelType.REFERENCE
                        )
                );
            }
            this.propertyType = PropertyType.ASSOCIATION;
            this.deferrable = true;
            this.unresolved.opposite = ASMTreeUtils.getAnnotationValue(primaryAnnotationNode, "opposite");
        } else if (primaryAnnotationNode.desc.equals(Type.getDescriptor(IndexOf.class))) {
            if (!this.unresolved.fieldNode.desc.equals("I")) {
                throw new IllegalClassException(
                        indexPropertyMustBeInteger(
                                this,
                                IndexOf.class
                        )
                );
            }
            this.propertyType = PropertyType.INDEX;
            this.absolute = ASMTreeUtils.getAnnotationValue(primaryAnnotationNode, "absolute", false);
            this.deferrable = true;
            this.simpleType = int.class;
            this.unresolved.indexOf = ASMTreeUtils.getAnnotationValue(primaryAnnotationNode, "value");
        } else if (primaryAnnotationNode.desc.equals(Type.getDescriptor(KeyOf.class))) {
            this.propertyType = PropertyType.KEY;
            this.absolute = ASMTreeUtils.getAnnotationValue(primaryAnnotationNode, "absolute", false);
            this.deferrable = true;
            this.unresolved.keyOf = ASMTreeUtils.getAnnotationValue(primaryAnnotationNode, "value");
        } else if (primaryAnnotationNode.desc.equals(Type.getDescriptor(Contravariance.class))) {
            this.propertyType = PropertyType.CONTRAVARIANCE;
            this.deferrable = true;
            this.unresolved.contravarianceFrom = ASMTreeUtils.getAnnotationValue(primaryAnnotationNode, "from", this.name);
        }
    }
    
    private AnnotationNode determinPrimaryAnnotationNode(AnnotationNode ... annotationNodes) {
        AnnotationNode primaryAnnotationNode = null;
        for (AnnotationNode annotationNode : annotationNodes) {
            if (annotationNode != null) {
                if (primaryAnnotationNode != null) {
                    throw new IllegalClassException(
                            conflictPrimaryAnnotation(
                                    this,
                                    ASMUtils.toClassName(primaryAnnotationNode.desc),
                                    ASMUtils.toClassName(annotationNode.desc)
                            )
                    );
                }
                primaryAnnotationNode = annotationNode;
            }
        }
        if (primaryAnnotationNode == null) {
            throw new AssertionError("Internal bug");
        }
        return primaryAnnotationNode;
    }
        
    public void resolveReferenceProperty() {
        MetadataClassImpl declaringClass = (MetadataClassImpl)this.declaringClass;
        MetadataPropertyImpl referenceProperty = null;
        if (this.unresolved.indexOf != null) {
            referenceProperty = declaringClass.declaredProperties.get(this.unresolved.indexOf);
        } else if (this.unresolved.keyOf != null) {
            referenceProperty = declaringClass.declaredProperties.get(this.unresolved.keyOf);
        } else {
            return;
        }
        if (referenceProperty == null) {
            throw new IllegalClassException(
                    noReferencePropertyForKey(
                            this,
                            this.unresolved.indexOf != null ? IndexOf.class : KeyOf.class,
                            this.unresolved.indexOf != null ? 
                                    this.unresolved.indexOf : 
                                    this.unresolved.keyOf,
                            this.declaringClass.getName()
                    )
            );
        }
        if (referenceProperty == this) {
            throw new IllegalClassException(
                    referencePropertyCanNotBeSelf(
                            this,
                            this.unresolved.indexOf != null ? IndexOf.class : KeyOf.class,
                            referenceProperty.name
                    )
            );
        }
        if (referenceProperty.propertyType != PropertyType.ASSOCIATION) {
            throw new IllegalClassException(
                    referencePropertyMustBeAssociation(
                            this,
                            this.unresolved.indexOf != null ? IndexOf.class : KeyOf.class,
                            referenceProperty
                    )
            );
        }
        if (referenceProperty.standardCollectionType != null) {
            throw new IllegalClassException(
                    referencePropertyCanNotBeCollection(
                            this,
                            this.unresolved.indexOf != null ? IndexOf.class : KeyOf.class,
                            referenceProperty
                    )
            );
        }
        if (referenceProperty.unresolved.contravarianceFrom != null) {
            throw new IllegalClassException(
                    referencePropertyCanNotBeContravariance(
                            this,
                            this.unresolved.indexOf != null ? IndexOf.class : KeyOf.class,
                            referenceProperty,
                            Contravariance.class
                    )
            );
        }
        if (referenceProperty.indexProperty != null) {
            throw new IllegalClassException(
                    conflictReferenceProperty(
                            this.declaringClass,
                            this,
                            referenceProperty.indexProperty,
                            referenceProperty
                    )
            );
        }
        if (referenceProperty.keyProperty != null) {
            throw new IllegalClassException(
                    conflictReferenceProperty(
                            this.declaringClass,
                            this,
                            referenceProperty.keyProperty,
                            referenceProperty
                    )
            );
        }
        this.referenceProperty = referenceProperty;
        if (this.unresolved.indexOf != null) {
            referenceProperty.indexProperty = this;
        } else if (this.unresolved.keyOf != null) {
            referenceProperty.keyProperty = this;
        }
    }
    
    public void resolveAssociationType() {
        this.associationType = this.determineAssociationType();
    }
    
    public void resolveConvarianceProperty() {
        if (this.unresolved.contravarianceFrom == null) {
            return;
        }
        MetadataClassImpl superMetadataClass = (MetadataClassImpl)this.declaringClass.getSuperClass();
        if (superMetadataClass == null) {
            throw new IllegalClassException(
                    contravarianceRequireSuperClass(this, Contravariance.class)
            );
        }
        MetadataPropertyImpl superProperty = superMetadataClass.properties.get(this.unresolved.contravarianceFrom);
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
        if (!Objects.equal(this.keyTypeName, superProperty.keyTypeName)) {
            throw new IllegalClassException(
                    diffKeyTypeOfConvariance(
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
                    sameTargetTypeOfConvariance(
                            this, 
                            Contravariance.class,
                            superProperty
                    )
            );
        }
        boolean concurrentConvariance = false;
        for (MetadataClassImpl superClass = this.targetClass.superClass; 
                superClass != null; 
                superClass = superClass.superClass) {
            if (superClass == superProperty.targetClass) {
                concurrentConvariance = true;
                break;
            }
        }
        if (!concurrentConvariance) {
            throw new IllegalClassException(
                    illegalTargetTypeOfConvariance(
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
    
    public void resolveOppositeProperty() {
        if (this.oppositeProperty != null) {
            return;
        }
        String opposite = this.unresolved.opposite;
        if (opposite == null) {
            if (this.indexProperty != null) {
                throw new IllegalClassException(
                        mustBeBidirectional(
                                this,
                                this.indexProperty,
                                IndexOf.class,
                                Association.class
                        )
                );
            }
            if (this.keyProperty != null) {
                throw new IllegalClassException(
                        mustBeBidirectional(
                                this,
                                this.keyProperty,
                                KeyOf.class,
                                Association.class
                        )
                );
            }
            return;
        }
        MetadataPropertyImpl oppositeProperty = this.targetClass.declaredProperties.get(opposite);
        if (oppositeProperty == null) {
            throw new IllegalClassException(
                    noOpposite(
                            this,
                            Association.class,
                            opposite,
                            this.targetClass
                    )
            );
        }
        if (oppositeProperty.getPropertyType() != PropertyType.ASSOCIATION) {
            throw new IllegalClassException(
                    oppositePropertyMustBeAssociation(
                            this,
                            Association.class,
                            oppositeProperty
                    )
            );
        }
        if (!this.name.equals(oppositeProperty.unresolved.opposite)) {
            throw new IllegalClassException(
                oppositeOppositeMustBeSelf(
                        this,
                        Association.class,
                        oppositeProperty,
                        this.name,
                        oppositeProperty.unresolved.opposite
                )
            );
        }
        this.validateOppositeAssociationProperty(oppositeProperty);
        this.oppositeProperty = oppositeProperty;
        oppositeProperty.oppositeProperty = this;
    }
    
    public void resolveComparatorParts(AbstractObjectModelInstrumenter ctx) {
        this.comparatorParts = this.determineComparatorParts(this.unresolved.fieldNode, ctx);
    }

    @Override
    public void finish() {
        this.unresolved = null;
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
    public String getName() {
        return this.name;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public boolean isDeferrable() {
        return this.deferrable;
    }

    @Override
    public boolean isMandatory() {
        return this.mandatory;
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
    protected void setKeyClass(AbstractMetadataClass keyClass) {
        this.keyClass = (MetadataClassImpl)keyClass;
    }

    @Override
    protected void setTargetClass(AbstractMetadataClass targetClass) {
        this.targetClass = (MetadataClassImpl)targetClass;
    }

    static class Unresolved {
        
        FieldNode fieldNode;
        
        String opposite;
        
        String indexOf;
        
        String keyOf;
        
        String contravarianceFrom;
    }
    
    @I18N
    private static native String illegalModifier(
            MetadataPropertyImpl thisProperty,
            String modifier);
    
    @I18N
    private static native String requireModifier(
            MetadataPropertyImpl thisProperty,
            String modifier);
    
    @I18N
    private static native String associationMustBeDeclaringInReferenceModelType(
            MetadataPropertyImpl thisProperty,
            Class<Association> associationTypeConstat,
            ModelType referenceModelTypeConstant);
    
    @I18N
    private static native String indexPropertyMustBeInteger(
            MetadataPropertyImpl thisProperty,
            Class<IndexOf> indexOfTypeConstant);
    
    @I18N
    private static native String conflictPrimaryAnnotation(
            MetadataPropertyImpl thisProperty,
            String annoationTypeName1,
            String annoationTypeName2);
    
    @I18N
    private static native String noReferencePropertyForKey(
            MetadataPropertyImpl thisProperty,
            Class<? extends Annotation> keyAnnotation,
            String keyName,
            String declaringClassName);
    
    @I18N
    private static native String referencePropertyCanNotBeSelf(
            MetadataPropertyImpl thisProperty,
            Class<? extends Annotation> keyAnnotation,
            String keyName);
    
    @I18N
    private static native String referencePropertyCanNotBeCollection(
            MetadataPropertyImpl thisProperty,
            Class<? extends Annotation> keyAnnotation,
            MetadataPropertyImpl referenceProperty);
    
    @I18N
    private static native String referencePropertyMustBeAssociation(
            MetadataPropertyImpl thisProperty,
            Class<? extends Annotation> keyAnnotation,
            MetadataPropertyImpl referenceProperty);
    
    @I18N
    private static native String referencePropertyCanNotBeContravariance(
            MetadataPropertyImpl thisProperty,
            Class<? extends Annotation> keyAnnotation,
            MetadataPropertyImpl referenceProperty,
            Class<Contravariance> contravarianceTypeConstant);
    
    @I18N
    private static native String conflictReferenceProperty(
            MetadataClass metadataClass,
            MetadataPropertyImpl property1,
            MetadataPropertyImpl property2,
            MetadataPropertyImpl referenceProperty);
    
    @I18N
    private static native String contravarianceRequireSuperClass(
            MetadataPropertyImpl thisProperty, 
            Class<Contravariance> contravarianceTypeConstant);
    
    @I18N
    private static native String noConvarianceProperty(
            MetadataPropertyImpl thisProperty,
            Class<Contravariance> contravarianceTypeConstant,
            String convariancePropertyName,
            MetadataClass superMetadataClass);
    
    @I18N
    private static native String diffKeyTypeOfConvariance(
            MetadataPropertyImpl thisProperty, 
            Class<Contravariance> contravarianceTypeConstant,
            MetadataPropertyImpl convarianceProperty,
            String thiskeyTypeName,
            String convariancekeyTypeName);
    
    @I18N
    private static native String sameTargetTypeOfConvariance(
            MetadataPropertyImpl thisProperty, 
            Class<Contravariance> contravarianceTypeConstant,
            MetadataPropertyImpl convarianceProperty);
    
    @I18N
    private static native String illegalTargetTypeOfConvariance(
            MetadataPropertyImpl thisProperty, 
            Class<Contravariance> contravarianceTypeConstant,
            MetadataPropertyImpl convarianceProperty,
            MetadataClass thisTargetClass,
            MetadataClass convarianceTargetClass);
    
    @I18N
    private static native String mustBeBidirectional(
            MetadataPropertyImpl thisProperty,
            MetadataPropertyImpl keyProperty,
            Class<? extends Annotation> keyAnnotation,
            Class<Association> associationTypeConstant);
    
    @I18N
    private static native String noOpposite(
            MetadataPropertyImpl thisProperty,
            Class<Association> associationTypeConstant,
            String oppositeName,
            MetadataClass targetClass);
    
    @I18N
    private static native String oppositePropertyMustBeAssociation(
            MetadataPropertyImpl thisProperty,
            Class<Association> associationTypeConstant,
            MetadataPropertyImpl oppositeProperty);
    
    @I18N
    private static native String oppositeOppositeMustBeSelf(
            MetadataPropertyImpl thisProperty,
            Class<Association> associationTypeConstant,
            MetadataPropertyImpl oppositeProperty,
            String expectedOppositeOppositeName,
            String actualOppositeOppositeName);
}
