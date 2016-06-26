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
package org.hibernate.model.hibernate.spi.association;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;

import org.babyfish.lang.Arguments;
import org.babyfish.model.jpa.metadata.JPAModelProperty;
import org.babyfish.model.jpa.metadata.JPAScalarType;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelProvider;
import org.hibernate.HibernateException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.bytecode.instrumentation.spi.LazyPropertyInitializer;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.Setter;

public class ObjectModel4HibernatePropertyAccessor implements PropertyAccessor {

    @SuppressWarnings("rawtypes") 
    @Override
    public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        ModelClass modelClass= ModelClass.of(theClass);
        JPAModelProperty property = (JPAModelProperty)modelClass.getProperties().get(propertyName);
        switch (property.getPropertyType()) {
        case CONTRAVARIANCE:
            return this.getGetter(
                    property.getConvarianceProperty().getDeclaringlClass().getJavaType(), 
                    property.getConvarianceProperty().getName()
            );
        case SCALAR:
            return new ScalarGetter(property);
        case INDEX:
            return new IndexGetter(property);
        case KEY:
            return new KeyGetter(property);
        case ASSOCIATION:
            switch (property.getAssociationType()) {
            case INDEXED_REFERENCE:
                return new EntityIndexedReferenceGetter(property);
            case KEYED_REFERENCE:
                return new EntityKeyedReferenceGetter(property);
            case REFERENCE:
                return new EntityReferenceGetter(property);
            default:
                Class<?> standardCollectionType = property.getStandardCollectionType();
                if (NavigableMap.class.isAssignableFrom(standardCollectionType)) {
                    return new EntityNavigableMapGetter(property);
                }
                if (Map.class.isAssignableFrom(standardCollectionType)) {
                    return new EntityOrderedMapGetter(property);
                }
                if (List.class.isAssignableFrom(standardCollectionType)) {
                    return new EntityListGetter(property);
                }
                if (NavigableSet.class.isAssignableFrom(standardCollectionType)) {
                    return new EntityNavigableSetGetter(property);
                }
                return new EntityOrderedSetGetter(property);
            }
        default:
            throw new AssertionError("Internal bug");
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        ModelClass modelClass= ModelClass.of(theClass);
        JPAModelProperty property = (JPAModelProperty)modelClass.getProperties().get(propertyName);
        switch (property.getPropertyType()) {
        case CONTRAVARIANCE:
            return this.getSetter(
                    property.getConvarianceProperty().getDeclaringlClass().getJavaType(), 
                    property.getConvarianceProperty().getName()
            );
        case SCALAR:
            return new ScalarSetter(property);
        case INDEX:
            return new IndexSetter(property);
        case KEY:
            return new KeySetter(property);
        case ASSOCIATION:
            switch (property.getAssociationType()) {
            case INDEXED_REFERENCE:
                return new EntityIndexedReferenceSetter(property);
            case KEYED_REFERENCE:
                return new EntityKeyedReferenceSetter(property);
            case REFERENCE:
                return new EntityReferenceSetter(property);
            default:
                Class<?> standardCollectionType = property.getStandardCollectionType();
                if (NavigableMap.class.isAssignableFrom(standardCollectionType)) {
                    return new EntityNavigableMapSetter(property);
                }
                if (Map.class.isAssignableFrom(standardCollectionType)) {
                    return new EntityOrderedMapSetter(property);
                }
                if (List.class.isAssignableFrom(standardCollectionType)) {
                    return new EntityListSetter(property);
                }
                if (NavigableSet.class.isAssignableFrom(standardCollectionType)) {
                    return new EntityNavigableSetSetter(property);
                }
                return new EntityOrderedSetSetter(property);
            }
        default:
            throw new AssertionError("Internal bug");
        }
    }
    
    private static abstract class AbstractOperator {
        
        JPAModelProperty property;
        
        Method method;
        
        AbstractOperator(JPAModelProperty property) {
            this.property = Arguments.mustNotBeNull("property", property);
            Class<?> entityJavaType = property.getDeclaringlClass().getJavaType();
            String methodNamePostfix = property.getName();
            methodNamePostfix = 
                    Character.toUpperCase(methodNamePostfix.charAt(0)) + 
                    methodNamePostfix.substring(1);
            if (this instanceof Getter) {
                if (property.getType() == boolean.class) {
                    try {
                        this.method = entityJavaType.getDeclaredMethod("is" + methodNamePostfix);
                    } catch (NoSuchMethodException ex) {
                    }
                }
                if (this.method == null) {
                    try {
                        this.method = entityJavaType.getDeclaredMethod("get" + methodNamePostfix);
                    } catch (NoSuchMethodException ex) {
                        throw new AssertionError("Internal bug", ex);
                    }
                }
            } else {
                try {
                    this.method = entityJavaType.getDeclaredMethod("set" + methodNamePostfix, property.getType());
                } catch (NoSuchMethodException ex) {
                    throw new AssertionError("Internal bug", ex);
                }
            }
        }
    }
    
    private static abstract class AbstractGetter extends AbstractOperator implements Getter {
    
        private static final long serialVersionUID = 3943081780514720814L;

        AbstractGetter(JPAModelProperty property) {
            super(property);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Object getForInsert(Object owner, Map mergeMap, SessionImplementor session) throws HibernateException {
            return this.get(owner);
        }

        @Override
        public Member getMember() {
            return this.method;
        }

        @Override
        public Class<?> getReturnType() {
            return this.method.getReturnType();
        }

        @Override
        public String getMethodName() {
            return this.method.getName();
        }

        @Override
        public Method getMethod() {
            return this.method;
        }
    }
    
    private static abstract class AbstractSetter extends AbstractOperator implements Setter {

        private static final long serialVersionUID = 4202608591668689267L;

        AbstractSetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public String getMethodName() {
            return this.method.getName();
        }

        @Override
        public Method getMethod() {
            return this.method;
        }
    }
    
    private static class ScalarGetter extends AbstractGetter {

        private static final long serialVersionUID = -1469628342149898059L;

        ScalarGetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public Object get(Object owner) throws HibernateException {
            int propertyId = this.property.getId();
            ObjectModel objectModel = ((ObjectModelProvider)owner).objectModel();
            if (this.property.getScalarType() == JPAScalarType.GENERAL) {
                if (objectModel.isDisabled(propertyId)) {
                    return LazyPropertyInitializer.UNFETCHED_PROPERTY;
                }
                if (objectModel.isUnloaded(propertyId)) {
                    return LazyPropertyInitializer.UNFETCHED_PROPERTY;
                }
            }
            return objectModel.get(propertyId);
        }
    }
    
    private static class ScalarSetter extends AbstractSetter {

        private static final long serialVersionUID = -8604561960293410622L;

        ScalarSetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)target).objectModel();
            objectModel.set(this.property.getId(), value);
        }
    }
    
    private static class IndexGetter extends AbstractGetter {

        private static final long serialVersionUID = 7343297023732352535L;

        IndexGetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public Object get(Object owner) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)owner).objectModel();
            return ((EntityIndexedReference<?>)objectModel.getAssociatedEndpoint(this.property.getReferenceProperty().getId())).hibernateGetIndex();
        }
    }
    
    private static class IndexSetter extends AbstractSetter {

        private static final long serialVersionUID = 9185339675556697773L;

        IndexSetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)target).objectModel();
            ((EntityIndexedReference<?>)objectModel.getAssociatedEndpoint(this.property.getReferenceProperty().getId())).hibernateSetIndex(value);
        }
    }
    
    private static class KeyGetter extends AbstractGetter {

        private static final long serialVersionUID = -1577572560327130864L;

        KeyGetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public Object get(Object owner) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)owner).objectModel();
            return ((EntityKeyedReference<?, ?>)objectModel.getAssociatedEndpoint(this.property.getReferenceProperty().getId())).hibernateGetKey();
        }
    }
    
    private static class KeySetter extends AbstractSetter {

        private static final long serialVersionUID = 1507323690555007124L;

        KeySetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)target).objectModel();
            ((EntityKeyedReference<?, ?>)objectModel.getAssociatedEndpoint(this.property.getReferenceProperty().getId())).hibernateSetKey(value);
        }
    }
    
    private static class EntityReferenceGetter extends AbstractGetter {

        private static final long serialVersionUID = -2569249262828571839L;

        EntityReferenceGetter(JPAModelProperty property) {
            super(property);
        }
        
        @Override
        public Object get(Object owner) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)owner).objectModel();
            return ((EntityReference<?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibernateGet();
        }
    }
    
    private static class EntityReferenceSetter extends AbstractSetter {

        private static final long serialVersionUID = -7975619987204909015L;

        EntityReferenceSetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)target).objectModel();
            ((EntityReference<?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibernateSet(value);
        }
    }
    
    private static class EntityIndexedReferenceGetter extends AbstractGetter {

        private static final long serialVersionUID = 1698789692343294393L;

        EntityIndexedReferenceGetter(JPAModelProperty property) {
            super(property);
        }
        
        @Override
        public Object get(Object owner) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)owner).objectModel();
            return ((EntityIndexedReference<?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibernateGet();
        }
    }
    
    private static class EntityIndexedReferenceSetter extends AbstractSetter {

        private static final long serialVersionUID = 1691769767214825284L;

        EntityIndexedReferenceSetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)target).objectModel();
            ((EntityIndexedReference<?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibernateSet(value);
        }
    }
    
    private static class EntityKeyedReferenceGetter extends AbstractGetter {

        private static final long serialVersionUID = 2856372727750626239L;

        EntityKeyedReferenceGetter(JPAModelProperty property) {
            super(property);
        }
        
        @Override
        public Object get(Object owner) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)owner).objectModel();
            return ((EntityKeyedReference<?, ?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibernateGet();
        }
    }
    
    private static class EntityKeyedReferenceSetter extends AbstractSetter {

        private static final long serialVersionUID = 6649145596047495500L;

        EntityKeyedReferenceSetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)target).objectModel();
            ((EntityKeyedReference<?, ?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibernateSet(value);
        }
    }
    
    private static class EntityListGetter extends AbstractGetter {

        private static final long serialVersionUID = -5907788840565835448L;

        EntityListGetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public Object get(Object owner) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)owner).objectModel();
            return ((EntityList<?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibenrateGet();
        }
    }
    
    private static class EntityListSetter extends AbstractSetter {

        private static final long serialVersionUID = 5337162305664285083L;

        EntityListSetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)target).objectModel();
            ((EntityList<?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibernateSet(value);
        }
    }
    
    private static class EntityOrderedSetGetter extends AbstractGetter {

        private static final long serialVersionUID = -1424796947105432902L;

        EntityOrderedSetGetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public Object get(Object owner) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)owner).objectModel();
            return ((EntityOrderedSet<?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibenrateGet();
        }
    }
    
    private static class EntityOrderedSetSetter extends AbstractSetter {

        private static final long serialVersionUID = -9130187974439145086L;

        EntityOrderedSetSetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)target).objectModel();
            ((EntityOrderedSet<?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibernateSet(value);
        }
    }
    
    private static class EntityNavigableSetGetter extends AbstractGetter {

        private static final long serialVersionUID = 6484985843838141716L;

        EntityNavigableSetGetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public Object get(Object owner) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)owner).objectModel();
            return ((EntityNavigableSet<?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibenrateGet();
        }
    }
    
    private static class EntityNavigableSetSetter extends AbstractSetter {

        private static final long serialVersionUID = -5450781090215155260L;

        EntityNavigableSetSetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)target).objectModel();
            ((EntityNavigableSet<?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibernateSet(value);
        }
    }
    
    private static class EntityOrderedMapGetter extends AbstractGetter {

        private static final long serialVersionUID = 1565494725260825569L;

        EntityOrderedMapGetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public Object get(Object owner) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)owner).objectModel();
            return ((EntityOrderedMap<?, ?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibenrateGet();
        }
    }
    
    private static class EntityOrderedMapSetter extends AbstractSetter {

        private static final long serialVersionUID = -3604550056230593578L;

        EntityOrderedMapSetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)target).objectModel();
            ((EntityOrderedMap<?, ?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibernateSet(value);
        }
    }
    
    private static class EntityNavigableMapGetter extends AbstractGetter {

        private static final long serialVersionUID = 8333885734356229978L;

        EntityNavigableMapGetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public Object get(Object owner) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)owner).objectModel();
            return ((EntityNavigableMap<?, ?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibenrateGet();
        }
    }
    
    private static class EntityNavigableMapSetter extends AbstractSetter {

        private static final long serialVersionUID = 2012968465393676412L;

        EntityNavigableMapSetter(JPAModelProperty property) {
            super(property);
        }

        @Override
        public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
            ObjectModel objectModel = ((ObjectModelProvider)target).objectModel();
            ((EntityNavigableMap<?, ?>)objectModel.getAssociatedEndpoint(this.property.getId())).hibernateSet(value);
        }
    }
}
