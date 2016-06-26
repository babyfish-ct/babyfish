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
package org.babyfish.model.metadata.internal;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;

import org.babyfish.collection.UnifiedComparator;
import org.babyfish.lang.Nulls;
import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.metadata.ComparatorPart;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.metadata.ModelProperty;
import org.babyfish.model.metadata.PropertyType;

/**
 * @author Tao Chen
 */
public class ModelPropertyImpl implements ModelProperty, Serializable {
    
    private static final long serialVersionUID = 5127847123643443699L;
    
    private int id;
    
    private String name;
    
    private PropertyType propertyType;
    
    private AssociationType associationType;
    
    private Class<?> type;
    
    private Class<?> standardCollectionType;
    
    private Class<?> keyType;
    
    private Class<?> targetType;
    
    private ModelClassImpl keyClass;
    
    private ModelClassImpl targetClass;
    
    private UnifiedComparator<?> keyUnifiedComparator;
    
    private UnifiedComparator<?> collectionUnifiedComparator;
    
    private ModelProperty indexProperty;
    
    private ModelProperty keyProperty;
    
    private ModelProperty referenceProperty;
    
    private ModelProperty convarianceProperty;
    
    private ModelProperty oppositeProperty;
    
    ModelClassImpl declaringClass;
    
    boolean deferrable;
    
    boolean mandatory;
    
    Dependency dependency;
    
    public ModelPropertyImpl(
            int id,
            String name,
            PropertyType propertyType, 
            AssociationType associationType,
            boolean deferrable,
            boolean mandatory,
            Class<?> type, 
            Class<?> standardCollectionType, 
            Class<?> keyType,
            Class<?> targetType, 
            Dependency dependency) {
        this.id = id;
        this.name = name;
        this.propertyType = propertyType;
        this.associationType = associationType;
        this.deferrable = deferrable;
        this.mandatory = mandatory;
        this.type = type;
        this.standardCollectionType = standardCollectionType;
        this.keyType = keyType;
        this.targetType = targetType;
        this.dependency = dependency;
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
    public ModelClass getDeclaringlClass() {
        return this.declaringClass;
    }

    @Override
    public PropertyType getPropertyType() {
        return this.propertyType;
    }

    @Override
    public Class<?> getType() {
        return this.type;
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
        return this.deferrable;
    }

    @Override
    public Class<?> getStandardCollectionType() {
        return this.standardCollectionType;
    }

    @Override
    public Class<?> getKeyType() {
        return this.keyType;
    }

    @Override
    public ModelClass getKeyClass() {
        this.resolve();
        return this.keyClass;
    }

    @Override
    public Class<?> getTargetType() {
        return this.targetType;
    }

    @Override
    public ModelClass getTargetClass() {
        this.resolve();
        return this.targetClass;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <K> UnifiedComparator<K> getKeyUnifiedComparator() {
        this.resolve();
        return (UnifiedComparator<K>)this.keyUnifiedComparator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> UnifiedComparator<E> getCollectionUnifiedComparator() {
        this.resolve();
        return (UnifiedComparator<E>)this.collectionUnifiedComparator;
    }
    
    @Override
    public ModelProperty getIndexProperty() {
        this.resolve();
        return this.indexProperty;
    }

    @Override
    public ModelProperty getKeyProperty() {
        this.resolve();
        return this.keyProperty;
    }

    @Override
    public ModelProperty getReferenceProperty() {
        this.resolve();
        return this.referenceProperty;
    }

    @Override
    public ModelProperty getConvarianceProperty() {
        this.resolve();
        return this.convarianceProperty;
    }

    @Override
    public ModelProperty getOppositeProperty() {
        this.resolve();
        return this.oppositeProperty;
    }

    @Override
    public String toString() {
        return this.declaringClass.getJavaType().getName() + '.' + this.name;
    }
    
    private void resolve() {
        Dependency dependency = this.dependency;
        if (dependency == null) {
            return;
        }
        
        if (this.keyType != null) {
            this.keyClass = ModelClassImpl.findClass(this.keyType);
            if (this.keyClass == null) {
                this.keyUnifiedComparator = UnifiedComparator.empty();
            } else {
                if (SortedMap.class.isAssignableFrom(this.standardCollectionType)) {
                    this.keyUnifiedComparator = UnifiedComparator.of(this.keyClass.getDefaultComparator());
                } else {
                    this.keyUnifiedComparator = UnifiedComparator.of(this.keyClass.getDefaultEqualityComparator());
                }
            }
        }
        this.targetClass = ModelClassImpl.findClass(this.targetType);
        if (dependency.indexPropertyId != -1) {
            this.indexProperty = this.declaringClass.getProperty(dependency.indexPropertyId);
        }
        if (dependency.keyPropertyId != -1) {
            this.keyProperty = this.declaringClass.getProperty(dependency.keyPropertyId);
        }
        if (dependency.referencePropertyId != -1) {
            this.referenceProperty = this.declaringClass.getProperty(dependency.referencePropertyId);
        }
        if (dependency.convariancePropertyId != -1) {
            this.convarianceProperty = this.declaringClass.getSuperClass().getProperty(dependency.convariancePropertyId);
        }
        if (dependency.oppositePropertyId != -1) {
            this.oppositeProperty = this.targetClass.getProperty(dependency.oppositePropertyId);
        }
        if (this.targetClass != null) {
            if (dependency.comparatorParts == null) {
                this.collectionUnifiedComparator = UnifiedComparator.of(this.targetClass.getDefaultEqualityComparator());
            } else {
                if (SortedSet.class.isAssignableFrom(this.standardCollectionType)) {
                    this.collectionUnifiedComparator = UnifiedComparator.of(
                            this.targetClass.getComparator(dependency.comparatorParts)
                    );
                } else {
                    this.collectionUnifiedComparator = UnifiedComparator.of(
                            this.targetClass.getEqualityComparator(dependency.comparatorParts)
                    );
                }
            }
        }
        this.dependency = null;
    }

    public static class Dependency {
        
        private int indexPropertyId;
        
        private int keyPropertyId;
        
        private int referencePropertyId;
        
        private int convariancePropertyId;
        
        private int oppositePropertyId;
        
        private ComparatorPart[] comparatorParts;

        public Dependency(
                int indexPropertyId, 
                int keyPropertyId, 
                int referencePropertyId, 
                int convariancePropertyId,
                int oppositePropertyId,
                ComparatorPart[] comparatorParts) {
            this.indexPropertyId = indexPropertyId;
            this.keyPropertyId = keyPropertyId;
            this.referencePropertyId = referencePropertyId;
            this.convariancePropertyId = convariancePropertyId;
            this.oppositePropertyId = oppositePropertyId;
            if (!Nulls.isNullOrEmpty(comparatorParts)) {
                this.comparatorParts = comparatorParts.clone();
            }
        }
    }
    
    protected final Object writeReplace() throws ObjectStreamException {
        return new Serialization(this.declaringClass, this.id);
    }
    
    private static class Serialization implements Serializable {
        
        private static final long serialVersionUID = -3451726233183818240L;

        private ModelClassImpl declaringClass;
        
        private int id;
        
        public Serialization(ModelClassImpl declaringClass, int id) {
            this.declaringClass = declaringClass;
            this.id = id;
        }

        Object readResolve() throws ObjectStreamException {
            return this.declaringClass.getProperty(this.id);
        }
    }
}
