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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenComparator;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.FrozenEqualityComparator;
import org.babyfish.collection.HashCalculator;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.collection.TreeMap;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.Nulls;
import org.babyfish.model.ModelType;
import org.babyfish.model.NullComparatorType;
import org.babyfish.model.StringComparatorType;
import org.babyfish.model.metadata.ComparatorPart;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.metadata.ModelProperty;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelProvider;

/**
 * @author Tao Chen
 */
public class ModelClassImpl implements ModelClass, Serializable {
    
    private static final long serialVersionUID = 9120324713008901228L;

    private static final ReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();
    
    private static final Map<Class<?>, ModelClassImpl> CACHE = new HashMap<>();
    
    private static final ModelPropertyImpl[] EMPTY_PROPERTY_ARRAY = new ModelPropertyImpl[0];

    private ModelType type;
    
    private Class<?> javaType;
    
    private ModelClassImpl superClass;
    
    private Map<String, ModelPropertyImpl> declaredProperties;
    
    private Map<String, ModelPropertyImpl> properties;
    
    private ModelPropertyImpl[] propertyArray;
    
    private Comparator<?> defaultComparator;
    
    private EqualityComparator<?> defaultEqualityComparator;
    
    public ModelClassImpl(
            ModelType type,
            Class<?> javaType, 
            Class<?> superJavaType,
            ModelPropertyImpl[] declaredProperties,
            ComparatorPart[] embeddableComparatorParts) {
        
        Lock  lock = CACHE_LOCK.writeLock();
        lock.lock();
        try {
            if (CACHE.containsKey(javaType)) {
                throw new UnsupportedOperationException();
            }
            CACHE.put(javaType, this);
        } finally {
            lock.unlock();
        }
        
        this.type = type;
        this.javaType = javaType;
        ModelClassImpl superClass = null;
        if (superJavaType != null) {
            superClass = findClass(superJavaType);
            this.superClass = superClass;
        }
        if (Nulls.isNullOrEmpty(declaredProperties)) {
            this.declaredProperties = MACollections.emptyMap();
            if (superClass  == null) {
                this.properties = MACollections.emptyMap();
                this.propertyArray = EMPTY_PROPERTY_ARRAY;
            } else {
                this.properties = superClass.properties;
                this.propertyArray = superClass.propertyArray;
            }
        } else {
            Map<String, ModelPropertyImpl> declaredMap = new LinkedHashMap<>();
            Map<String, ModelPropertyImpl> map;
            if (superClass == null || superClass.properties.isEmpty()) {
                map = declaredMap;
            } else {
                map = new LinkedHashMap<>(superClass.properties);
            }
            int maxId = -1;
            for (ModelPropertyImpl declaredProperty : declaredProperties) {
                declaredProperty.declaringClass = this;
                declaredMap.put(declaredProperty.getName(), declaredProperty);
                maxId = declaredProperty.getId() > maxId ? declaredProperty.getId() : maxId;
            }
            if (map != declaredMap) {
                map.putAll(declaredMap);
            }
            this.declaredProperties = MACollections.unmodifiable(declaredMap);
            this.properties = MACollections.unmodifiable(map);
            ModelPropertyImpl[] propertyArray = new ModelPropertyImpl[maxId + 1];
            if (superClass != null) {
                System.arraycopy(superClass.propertyArray, 0, propertyArray, 0, superClass.propertyArray.length);
            }
            for (ModelPropertyImpl declaredProperty : declaredProperties) {
                if (declaredProperty.getId() != -1) {
                    propertyArray[declaredProperty.getId()] = declaredProperty;
                }
            }
            this.propertyArray = propertyArray;
        }
        
        this.afterPropertiesDetermined();
        
        if (embeddableComparatorParts != null) {
            this.defaultComparator = this.getComparator(embeddableComparatorParts);
            this.defaultEqualityComparator = this.getEqualityComparator(embeddableComparatorParts);
        } else {
            this.defaultEqualityComparator = this.createDefaultEqualityComparator();
        }
    }
    
    public static ModelClass getClass(Class<?> javaType) {
        for (Class<?> type = javaType; type != Object.class; type = type.getSuperclass()) {
            ModelClass modelClass = ModelClassImpl.findClass(type);
            if (modelClass != null) {
                return modelClass;
            }
        }
        throw new IllegalArgumentException(isNotModelClass(javaType));
    }
    
    static ModelClassImpl findClass(Class<?> javaType) {
        Arguments.mustNotBeNull("javaType", javaType);
        ModelClassImpl modelClass;
        
        Lock  lock = CACHE_LOCK.readLock();
        lock.lock();
        try {
            modelClass = CACHE.get(javaType);
            if (modelClass != null) {
                return modelClass;
            }
        } finally {
            lock.unlock();
        }
        
        Field modelClassField = null;
        try {
            modelClassField = javaType.getField("{MODEL_CLASS}");
        } catch (NoSuchFieldException ex) {
            // Ingore exception
        }
        if (modelClassField == null || 
                !Modifier.isStatic(modelClassField.getModifiers()) || 
                modelClassField.getType() != ModelClass.class) {
            return null;
        }
        try {
            modelClass = (ModelClassImpl)modelClassField.get(null);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("impossible", ex);
        }
        if (modelClass == null) {
            throw new AssertionError(illegalModelField(modelClassField));
        }
        return modelClass;
    }
    
    @Override
    public ModelType getType() {
        return this.type;
    }

    @Override
    public Class<?> getJavaType() {
        return this.javaType;
    }

    @Override
    public ModelClass getSuperClass() {
        return this.superClass;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<String, ModelProperty> getDeclaredProperties() {
        return (Map)this.declaredProperties;
    }

    @Override
    public ModelProperty getDeclaredProperty(String name) {
        ModelProperty modelProperty = this.declaredProperties.get(name);
        if (modelProperty == null) {
            throw new IllegalArgumentException(noDeclaredPropertyName(this.javaType, name));
        }
        return modelProperty;
    }
    
    public ModelProperty getProperty(String name) {
        ModelProperty modelProperty = this.properties.get(name);
        if (modelProperty == null) {
            throw new IllegalArgumentException(noPropertyName(this.javaType, name));
        }
        return modelProperty;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<String, ModelProperty> getProperties() {
        return (Map)this.properties;
    }

    @Override
    public ModelProperty getProperty(int id) {
        try {
            return this.propertyArray[id];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException(noPropertyId(this.javaType, id), ex);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<ModelProperty> getPropertyList() {
        return (List)MACollections.wrap(this.propertyArray);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Comparator<T> getDefaultComparator() {
        return (Comparator<T>)this.defaultComparator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> EqualityComparator<T> getDefaultEqualityComparator() {
        return (EqualityComparator<T>)this.defaultEqualityComparator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> FrozenComparator<T> getComparator(ComparatorPart... parts) {
        validateParts(parts);
        return (FrozenComparator<T>)new FrozenComparatorImpl(parts);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> FrozenEqualityComparator<T> getEqualityComparator(ComparatorPart... parts) {
        validateParts(parts);
        return (FrozenEqualityComparator<T>)new FrozenEqualityComparatorImpl(parts);
    }

    @Override
    public String toString() {
        return this.javaType.getName();
    }

    private void validateParts(ComparatorPart[] parts) {
        Arguments.mustNotContainNullElements(
                "parts", 
                Arguments.mustNotBeEmpty(
                        "parts", 
                        Arguments.mustNotBeNull("parts", parts)
                )
        );
        for (int i = parts.length - 1; i >= 0; i--) {
            Arguments.mustBetweenValue(
                    "parts[" + i + "]scalarPropertyId", 
                    parts[i].getScalarPropertyId(), 
                    0, 
                    true, 
                    this.propertyArray.length, 
                    false
            );
        }
    }
    
    protected void afterPropertiesDetermined() {}
    
    protected EqualityComparator<?> createDefaultEqualityComparator() {
        if (this.type == ModelType.REFERENCE) {
            return ReferenceEqualityComparator.getInstance();
        }
        ComparatorPart[] parts = new ComparatorPart[this.propertyArray.length];
        int propertyCount = 0;
        for (ModelProperty modelProperty : this.propertyArray) {
            if (!modelProperty.getTargetType().isArray()) {
                parts[propertyCount++] = new ComparatorPart(modelProperty.getId());
            }
        }
        if (propertyCount == 0) {
            return ReferenceEqualityComparator.getInstance();
        }
        if (propertyCount != parts.length) {
            ComparatorPart[] newParts = new ComparatorPart[propertyCount];
            System.arraycopy(parts, 0, newParts, 0, propertyCount);
            parts = newParts;
        }
        return this.getEqualityComparator(parts);
    }
    
    private static abstract class AbstractComparator implements Serializable {
        
        private static final long serialVersionUID = -6813444275633083502L;
        
        int[] scalarPropertyIds;
        
        StringComparatorType[] stringComparatorTypes;
        
        NullComparatorType[] nullComparatorTypes;
        
        private transient int hash;
        
        AbstractComparator(ComparatorPart[] parts, boolean keepOrder) {
            Map<Integer, ComparatorPart> map;
            if (keepOrder) { 
                map = new LinkedHashMap<>();
            } else { 
                map = new TreeMap<>(); 
            }
            for (ComparatorPart part : parts) {
                if (map.put(part.getScalarPropertyId(), part) != null) {
                    throw new IllegalArgumentException(duplicateComparatorParts(part.getScalarPropertyId()));
                }
            }
            int len = map.size();
            int[] scalarPropertyIds = new int[len];
            StringComparatorType[] stringComparatorTypes = new StringComparatorType[len];
            NullComparatorType[] nullComparatorTypes = new NullComparatorType[len];
            int index = 0;
            for (ComparatorPart part : map.values()) {
                scalarPropertyIds[index] = part.getScalarPropertyId();
                stringComparatorTypes[index] = part.getStringComparatorType();
                nullComparatorTypes[index] = part.getNullComparatorType();
                index++;
            }
            this.scalarPropertyIds = scalarPropertyIds;
            this.stringComparatorTypes = stringComparatorTypes;
            this.nullComparatorTypes = nullComparatorTypes;
        }
        
        public int hashCode(Object o) {
            ObjectModel om = ((ObjectModelProvider)o).objectModel();
            int[] scalarPropertyIds = this.scalarPropertyIds;
            StringComparatorType[] stringComparatorTypes = this.stringComparatorTypes;
            int len = this.scalarPropertyIds.length;
            int hash = 0;
            for (int i = 0; i < len; i++) {
                hash = hash * 31 + om.hashCodeScalar(scalarPropertyIds[i], stringComparatorTypes[i]);
            }
            return hash;
        }
        
        public void freeze(Object obj, FrozenContext<Object> ctx) {
            ObjectModel om = ((ObjectModelProvider)obj).objectModel();
            for (int scalarPropertyId : this.scalarPropertyIds) {
                om.freezeScalar(scalarPropertyId, ctx);
            }
        }

        public void unfreeze(Object obj, FrozenContext<Object> ctx) {
            ObjectModel om = ((ObjectModelProvider)obj).objectModel();
            for (int scalarPropertyId : this.scalarPropertyIds) {
                om.unfreezeScalar(scalarPropertyId, ctx);
            }
        }

        @Override
        public int hashCode() {
            int h = this.hash;
            if (h == 0) {
                h = Arrays.hashCode(this.scalarPropertyIds);
                h = 31 * h + Arrays.hashCode(this.stringComparatorTypes);
                h = 31 * h + Arrays.hashCode(this.nullComparatorTypes);
                if (h == 0) {
                    h = -1;
                }
                this.hash = h;
            }
            return h;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            AbstractComparator other = (AbstractComparator)obj;
            return 
                    Arrays.equals(this.scalarPropertyIds, other.scalarPropertyIds) &&
                    Arrays.equals(this.stringComparatorTypes, other.stringComparatorTypes) &&
                    Arrays.equals(this.nullComparatorTypes, other.nullComparatorTypes);
        }
    }
    
    private static class FrozenComparatorImpl 
    extends AbstractComparator 
    implements FrozenComparator<Object>, HashCalculator<Object> {
    
        private static final long serialVersionUID = 3846624166718386603L;
    
        FrozenComparatorImpl(ComparatorPart[] parts) {
            super(parts, true);
        }
    
        @Override
        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return +1;
            }
            ObjectModel om1 = ((ObjectModelProvider)o1).objectModel();
            ObjectModel om2 = ((ObjectModelProvider)o2).objectModel();
            int[] scalarPropertyIds = this.scalarPropertyIds;
            StringComparatorType[] stringComparatorTypes = this.stringComparatorTypes;
            NullComparatorType[] nullComparatorTypes = this.nullComparatorTypes;
            int len = this.scalarPropertyIds.length;
            for (int i = 0; i < len; i++) {
                int cmp = om1.compareScalar(
                        scalarPropertyIds[i], 
                        stringComparatorTypes[i], 
                        nullComparatorTypes[i], 
                        om2
                );
                if (cmp != 0) {
                    return cmp;
                }
            }
            return 0;
        }
    }

    private static class FrozenEqualityComparatorImpl 
    extends AbstractComparator 
    implements FrozenEqualityComparator<Object> {
    
        private static final long serialVersionUID = -1014078567581660497L;

        FrozenEqualityComparatorImpl(ComparatorPart[] parts) {
            super(parts, false);
        }
        
        @Override
        public boolean equals(Object o1, Object o2) {
            ObjectModel om1 = ((ObjectModelProvider)o1).objectModel();
            ObjectModel om2 = ((ObjectModelProvider)o2).objectModel();
            int[] scalarPropertyIds = this.scalarPropertyIds;
            StringComparatorType[] stringComparatorTypes = this.stringComparatorTypes;
            int len = this.scalarPropertyIds.length;
            for (int i = 0; i < len; i++) {
                if (!om1.equalsScalar(scalarPropertyIds[i], stringComparatorTypes[i], om2)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    protected final Object writeReplace() throws ObjectStreamException {
        return new Serialization(this.javaType);
    }
    
    private static class Serialization implements Serializable {
        
        private static final long serialVersionUID = -3882855915313676507L;
        
        private Class<?> javaType;
        
        Serialization(Class<?> javaType) {
            this.javaType = javaType;
        }
        
        Object readResolve() throws ObjectStreamException {
            return ModelClassImpl.getClass(this.javaType);
        }
    }
    
    @I18N
    private static native String isNotModelClass(Class<?> javaType);
    
    @I18N
    private static native String illegalModelField(Field field);
    
    @I18N
    private static native String noDeclaredPropertyName(Class<?> modelClass, String name);
    
    @I18N
    private static native String noPropertyName(Class<?> modelClass, String name);
    
    @I18N
    private static native String noPropertyId(Class<?> modelClass, long id);
    
    @I18N
    private static native String duplicateComparatorParts(long scalarPropertyId);
}
