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
package org.babyfish.model.jpa;

import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.Nulls;
import org.babyfish.model.ModelType;
import org.babyfish.model.jpa.metadata.JPAModelClass;
import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.metadata.ModelProperty;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelProvider;
import org.babyfish.model.spi.reference.Reference;

/**
 * @author Tao Chen
 */
public class JPAEntities {

    private static final String LINE_PREFIX = System.getProperty("line.separator") + '\t';
    
    private static final int FG_REQURED = 1 << 0;
    
    private static final int FG_NOT_NULL = 1 << 1;
    
    private static final int FG_NOT_EMPTY = 1 << 2 | FG_NOT_NULL;
        
    private static final String SPECIAL_ATTRIBUTE_METHODS = 
            "required, notNull, notEmpty";

    @Deprecated
    protected JPAEntities() {
        throw new UnsupportedOperationException();
    }
    
    public static void disableAll(Object entity) {
        validateArgument("entity", entity);
        ModelClass modelClass = JPAModelClass.of(entity.getClass());
        if (modelClass.getType() != ModelType.REFERENCE) {
            throw new IllegalArgumentException(
                    mustBeReferenceModel(
                            "entity", 
                            ModelType.REFERENCE
                    )
            );
        }
        ObjectModel objectModel = ((ObjectModelProvider)entity).objectModel();
        for (ModelProperty modelProperty : modelClass.getProperties().values()) {
            objectModel.disable(modelProperty.getId());
        }
    }
    
    @SafeVarargs
    public static <E> void enable(E entity, Attribute<? super E, ?> ... attributes) {
        validateArgument("entity", entity);
        ModelClass modelClass = JPAModelClass.of(entity.getClass());
        if (modelClass.getType() != ModelType.REFERENCE) {
            throw new IllegalArgumentException(
                    mustBeReferenceModel(
                            "entity", 
                            ModelType.REFERENCE
                    )
            );
        }
        ObjectModel objectModel = ((ObjectModelProvider)entity).objectModel();
        for (Attribute<? super E, ?> attribute : attributes) {
            if (attribute instanceof SpecialAttribute<?>) {
                throw new IllegalArgumentException(
                        mustNotContainSpecialAttribute(
                                "attributes", 
                                SPECIAL_ATTRIBUTE_METHODS
                        )
                );
            }
            objectModel.enable(getModelProperty(modelClass, attribute).getId());
        }
    }
    
    @SafeVarargs
    public static <E> void disable(E entity, Attribute<? super E, ?> ... attributes) {
        validateArgument("entity", entity);
        ModelClass modelClass = JPAModelClass.of(entity.getClass());
        if (modelClass.getType() !=ModelType.REFERENCE) {
            throw new IllegalArgumentException(
                    mustBeReferenceModel(
                           "entity", 
                           ModelType.REFERENCE
                    )
            );
        }
        ObjectModel objectModel = ((ObjectModelProvider)entity).objectModel();
        for (Attribute<? super E, ?> attribute : attributes) {
            if (attribute instanceof SpecialAttribute<?>) {
                throw new IllegalArgumentException(
                        mustNotContainSpecialAttribute(
                                "attributes", 
                                SPECIAL_ATTRIBUTE_METHODS
                        )
                );
            }
            objectModel.disable(getModelProperty(modelClass, attribute).getId());
        }
    }
    
    @SafeVarargs
    public static <E> Comparator<E> comparator(Class<E> entityType, Attribute<? super E, ?> ... attributes) {
        Arguments.mustNotBeNull("entityType", entityType);
        Arguments.mustNotBeNull("attributes", attributes);
        Arguments.mustNotBeEmpty("attributes", attributes);
        ModelClass modelClass = JPAModelClass.of(entityType);
        Set<Integer> propertyIdSet = new LinkedHashSet<>();
        for (Attribute<? super E, ?> attribute : attributes) {
            Arguments.mustNotBeNull("attributes[?]", attribute);
            if (attribute instanceof SpecialAttribute<?>) {
                throw new IllegalArgumentException(
                        mustNotContainSpecialAttribute(
                                "attributes", 
                                SPECIAL_ATTRIBUTE_METHODS
                        )
                );
            }
            ModelProperty modelProperty = getModelProperty(modelClass, attribute);
            if (modelProperty.getPropertyType() != PropertyType.SCALAR) {
                throw new IllegalArgumentException(
                        mustBeScalarAttribute(
                                "attributes", 
                                attribute.getName(),
                                modelProperty
                        )
                );
            }
            propertyIdSet.add(modelProperty.getId());
        }
        int[] propertyIds = MACollections.toIntArray(propertyIdSet);
        return modelClass.getComparator(propertyIds);
    }
    
    @SafeVarargs
    public static <E> EqualityComparator<E> equalityComparator(Class<E> entityType, Attribute<? super E, ?> ... attributes) {
        Arguments.mustNotBeNull("entityType", entityType);
        Arguments.mustNotBeNull("attributes", attributes);
        Arguments.mustNotBeEmpty("attributes", attributes);
        ModelClass modelClass = JPAModelClass.of(entityType);
        Set<Integer> propertyIdSet = new LinkedHashSet<>();
        for (Attribute<? super E, ?> attribute : attributes) {
            Arguments.mustNotBeNull("attributes[?]", attribute);
            if (attribute instanceof SpecialAttribute<?>) {
                throw new IllegalArgumentException(
                        mustNotContainSpecialAttribute(
                                "attributes", 
                                SPECIAL_ATTRIBUTE_METHODS
                        )
                );
            }
            ModelProperty modelProperty = getModelProperty(modelClass, attribute);
            if (modelProperty.getPropertyType() != PropertyType.SCALAR) {
                throw new IllegalArgumentException(
                        mustBeScalarAttribute(
                                "attributes", 
                                attribute.getName(),
                                modelProperty
                        )
                );
            }
            propertyIdSet.add(modelProperty.getId());
        }
        int[] propertyIds = MACollections.toIntArray(propertyIdSet);
        return modelClass.getEqualityComparator(propertyIds);
    }
    
    @SafeVarargs
    public static <E> void validateMaxEnabledRange(E entity, Attribute<? super E, ?> ... maxEnabledAttributeRange) {
        validateArgument("entity", entity);
        JPAModelClass modelClass = JPAModelClass.of(entity.getClass());
        ModelProperty idProperty = modelClass.getIdProperty();
        Map<Integer, Integer> map = new HashMap<>((maxEnabledAttributeRange.length * 4 + 2) / 3);
        for (Attribute<? super E, ?> attribute : maxEnabledAttributeRange) {
            int flags = 0;
            if (attribute instanceof SpecialAttribute<?>) {
                SpecialAttribute<?> sa = (SpecialAttribute<?>)attribute;
                if (sa != null) {
                    flags = sa.flags;
                }
            }
            map.put(getModelProperty(modelClass, attribute).getId(), flags);
        }
        ObjectModel objectModel = ((ObjectModelProvider)entity).objectModel();
        StringBuilder builder = null;
        for (ModelProperty modelProperty : modelClass.getProperties().values()) {
            if (modelProperty == idProperty) {
                continue;
            }
            int propertyId = modelProperty.getId();
            Integer flags = map.get(propertyId);
            if (flags == null) {
                if (!objectModel.isDisabled(modelProperty.getId())) {
                    builder = lazyAppend(
                            builder, 
                            modelClass, 
                            mustBeDisabled(modelProperty)
                    );
                }
            } else {
                if (objectModel.isDisabled(propertyId)) {
                    if ((flags & FG_REQURED) != 0) {
                        builder = lazyAppend(
                                builder, 
                                modelClass, 
                                mustBeEnabled(modelProperty)
                        );
                    }
                    continue;
                }
                if ((flags & ~FG_REQURED) == 0) {
                    continue;
                }
                Object propertyValue;
                if (modelProperty.getAssociationType() == AssociationType.NONE) {
                    propertyValue = objectModel.get(propertyId);
                } else {
                    propertyValue = objectModel.getAssociatedEndpoint(propertyId);
                }
                if ((flags & FG_NOT_NULL) != 0) {
                    if (propertyValue == null) {
                        builder = lazyAppend(
                                builder, 
                                modelClass, 
                                mustNotBeNull(modelProperty)
                        );
                    }
                }
                if ((flags & FG_NOT_EMPTY) != 0) {
                    boolean isEmpty = false;
                    if (propertyValue instanceof String) {
                        isEmpty = ((String)propertyValue).isEmpty();
                    } else if (propertyValue instanceof Collection<?>) {
                        isEmpty = ((Collection<?>)propertyValue).isEmpty();
                    } else if (propertyValue instanceof Map<?, ?>) {
                        isEmpty = ((Map<?, ?>)propertyValue).isEmpty();
                    } else if (propertyValue != null && propertyValue.getClass().isArray()) {
                        isEmpty = Array.getLength(propertyValue) == 0;
                    }
                    if (isEmpty) {
                        builder = lazyAppend(
                                builder, 
                                modelClass, 
                                mustNotBeEmpty(modelProperty)
                        );
                    }
                }
            }
        }
        if (builder != null) {
            throw new IllegalArgumentException(builder.toString());
        }
    }
    
    private static StringBuilder lazyAppend(
            StringBuilder builder, 
            ModelClass modelClass,
            String message) { 
        if (builder == null) {
            builder = new StringBuilder(validationErrorMessagePrefix(modelClass));
        }
        builder
        .append(LINE_PREFIX)
        .append(message);
        return builder;
    }
    
    public static <E> boolean isEnabled(E entity, Attribute<? super E, ?> attribute) {
        return !isDisabled(entity, attribute);
    }
    
    public static <E> boolean isDisabled(E entity, Attribute<? super E, ?> attribute) {
        validateArgument("entity", entity);
        ModelClass modelClass = JPAModelClass.of(entity.getClass());
        ObjectModel objectModel = ((ObjectModelProvider)entity).objectModel();
        return objectModel.isDisabled(getModelProperty(modelClass, attribute).getId());
    }
    
    public static <E> boolean isIdEquals(E entity1, E entity2) {
        validateArgument("entity1", entity1);
        validateArgument("entity2", entity2);
        JPAModelClass modelClass1 = JPAModelClass.of(entity1.getClass());
        JPAModelClass modelClass2 = JPAModelClass.of(entity2.getClass());
        if (modelClass1.getIdProperty() != modelClass2.getIdProperty()) {
            return false;
        }
        ObjectModel objectModel1 = ((ObjectModelProvider)entity1).objectModel();
        ObjectModel objectModel2 = ((ObjectModelProvider)entity2).objectModel();
        int entityIdPropertyId = modelClass1.getIdProperty().getId();
        Object id1 = objectModel1.get(entityIdPropertyId);
        Object id2 = objectModel2.get(entityIdPropertyId);
        return Nulls.equals(id1, id2);
    }
    
    public static <E> E createFakeEntity(Class<E> entityType, Object id) {
        Arguments.mustNotBeNull("entityType", entityType);
        Arguments.mustNotBeNull("id", id);
        JPAModelClass modelClass = JPAModelClass.of(entityType);
        E entity;
        try {
            entity  = entityType.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalArgumentException(
                    newObjectFailed(entityType),
                    ex
            );
        }
        disableAll(entity);
        ((ObjectModelProvider)entity).objectModel().set(modelClass.getIdProperty().getId(), id);
        return entity;
    }
    
    public static <E> Set<E> createFakeEntities(Class<E> entityType, Iterable<?> ids) {
        Arguments.mustNotBeNull("entityType", entityType);
        if (Nulls.isNullOrEmpty(ids)) {
            return MACollections.emptySet();
        }
        Set<E> entities;
        if (ids instanceof Iterable<?>) {
            entities = new LinkedHashSet<>((((Collection<?>)ids).size() * 4 + 2) / 3);
        } else {
            entities = new LinkedHashSet<>();
        }
        JPAModelClass modelClass = JPAModelClass.of(entityType);
        int idPropertyId = modelClass.getIdProperty().getId();
        for (Object id : ids) {
            E entity;
            try {
                entity  = entityType.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new IllegalArgumentException(
                        newObjectFailed(entityType),
                        ex
                );
            }
            disableAll(entity);
            ((ObjectModelProvider)entity).objectModel().set(idPropertyId, id);
            entities.add(entity);
        }
        return entities;
    }
    
    public static <E> Set<E> createFakeEntities(Class<E> entityType, Object ... ids) {
        return createFakeEntities(entityType, MACollections.wrap(ids));
    }
    
    @SuppressWarnings("unchecked")
    public static <E, A> XOrderedSet<A> extractAttribute(Collection<E> entities, SingularAttribute<? super E, A> attribute) {
        XOrderedSet<A> values;
        if (entities.size() < 1024) {
             values = new LinkedHashSet<>((entities.size() * 4 + 2) / 3);
        } else {
            values = new LinkedHashSet<>();
        }
        for (E entity : entities) {
            validateArgument("entity", entity);
            ModelClass modelClass = JPAModelClass.of(entity.getClass());
            ObjectModel objectModel = ((ObjectModelProvider)entity).objectModel();
            ModelProperty modelProperty = getModelProperty(modelClass, attribute);
            if (modelProperty.getAssociationType() == AssociationType.NONE) {
                values.add((A)objectModel.get(modelProperty.getId()));   
            } else {
                Reference<?> reference = (Reference<?>)objectModel.getAssociatedEndpoint(modelProperty.getId());
                values.add((A)reference.get());
            }
        }
        return values;
    }

    public static <E> Attribute<E, ?> required(Attribute<E, ?> attribute) {
        return specialAttribute(attribute, FG_REQURED);
    }

    public static <E> Attribute<E, ?> notNull(Attribute<E, ?> attribute) {
        return specialAttribute(attribute, FG_NOT_NULL);
    }
    
    public static <E> Attribute<E, ?> notEmpty(Attribute<E, ?> attribute) {
        return specialAttribute(attribute, FG_NOT_EMPTY);
    }
    
    private static <E> Attribute<E, ?> specialAttribute(Attribute<E, ?> attribute, int flags) {
        Arguments.mustNotBeNull("attribute", attribute);
        int oldFlags = 
                attribute instanceof SpecialAttribute<?> ? 
                        ((SpecialAttribute<?>)attribute).flags : 
                        0;
        return new SpecialAttribute<>(attribute, flags | oldFlags);
    }
    
    private static void validateArgument(String name, Object entity) {
        Arguments.mustNotBeNull(name, entity);
        Arguments.mustNotBeArray(name, entity.getClass());
        Arguments.mustNotBePrimitive(name, entity.getClass());
    }
    
    private static ModelProperty getModelProperty(ModelClass modelClass, Attribute<?, ?> attribute) {
        String attributeName = attribute.getName();
        ModelProperty modelProperty = modelClass.getProperties().get(attributeName);
        if (modelProperty == null) {
            throw new IllegalArgumentException(
                    noAttribute(modelClass.getJavaType(), attribute.getName())
            );
        }
        return modelProperty;
    }
    
    private static class SpecialAttribute<E> implements Attribute<E, Object> {
        
        Attribute<E, ?> raw;
        
        int flags;

        @SuppressWarnings("unchecked")
        public SpecialAttribute(Attribute<E, ?> raw, int flags) {
            if (raw instanceof SpecialAttribute<?>) {
                SpecialAttribute<E> sa = (SpecialAttribute<E>)raw;
                this.raw = sa.raw;
                this.flags = sa.flags | flags;
            } else {
                this.raw = raw;
                this.flags = flags;
            }
        }

        public ManagedType<E> getDeclaringType() {
            return this.raw.getDeclaringType();
        }

        public Member getJavaMember() {
            return this.raw.getJavaMember();
        }

        @SuppressWarnings("unchecked")
        public Class<Object> getJavaType() {
            return (Class<Object>)this.raw.getJavaType();
        }

        public String getName() {
            return this.raw.getName();
        }

        public javax.persistence.metamodel.Attribute.PersistentAttributeType getPersistentAttributeType() {
            return this.raw.getPersistentAttributeType();
        }

        public boolean isAssociation() {
            return this.raw.isAssociation();
        }

        public boolean isCollection() {
            return this.raw.isCollection();
        }
    }
    
    @I18N
    private static native String validationErrorMessagePrefix(ModelClass modelClass);
    
    @I18N
    private static native String mustBeDisabled(ModelProperty modelProperty);
    
    @I18N
    private static native String mustBeEnabled(ModelProperty modelProperty);
    
    @I18N
    private static native String mustNotBeNull(ModelProperty modelProperty);
    
    @I18N
    private static native String mustNotBeEmpty(ModelProperty modelProperty);
    
    @I18N
    private static native String mustBeReferenceModel(
            String argumentName, 
            ModelType referenceModelTypeConstant);
    
    @I18N
    private static native String mustNotContainSpecialAttribute(
            String argumentName,
            String specialAttributeMethods);
    
    @I18N
    private static native String mustBeScalarAttribute(
            String argumentName,
            String attributeName,
            ModelProperty modelProperty);
    
    @I18N
    private static native String newObjectFailed(Class<?> type);
    
    @I18N
    private static native String noAttribute(Class<?> ownerType, String attributeName);
}
