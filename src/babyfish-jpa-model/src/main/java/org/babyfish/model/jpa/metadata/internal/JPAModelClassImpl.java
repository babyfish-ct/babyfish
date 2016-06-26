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
package org.babyfish.model.jpa.metadata.internal;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.FrozenEqualityComparator;
import org.babyfish.lang.I18N;
import org.babyfish.model.Model;
import org.babyfish.model.ModelType;
import org.babyfish.model.StringComparatorType;
import org.babyfish.model.jpa.JPAModel;
import org.babyfish.model.jpa.metadata.JPAModelClass;
import org.babyfish.model.jpa.metadata.JPAModelProperty;
import org.babyfish.model.jpa.metadata.JPAScalarType;
import org.babyfish.model.metadata.ComparatorPart;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.metadata.internal.ModelClassImpl;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelProvider;

public class JPAModelClassImpl extends ModelClassImpl implements JPAModelClass {
    
    private JPAModelPropertyImpl idProperty;
    
    private JPAModelPropertyImpl versionProperty;

    public JPAModelClassImpl(
            ModelType type, 
            Class<?> javaType, 
            Class<?> superJavaType,
            JPAModelPropertyImpl[] declaredProperties, 
            ComparatorPart[] embeddableComparatorParts) {
        super(type, javaType, superJavaType, declaredProperties, embeddableComparatorParts);
    }
    
    public static JPAModelClass of(Class<?> javaType) {
        ModelClass modelClass = ModelClass.of(javaType);
        if (!(modelClass instanceof JPAModelClass)) {
            throw new IllegalArgumentException(
                    onlyUsesObjectModel4Java(javaType, Model.class, JPAModel.class)
            );
        }
        return (JPAModelClass)modelClass;
    }

    @Override
    public JPAModelProperty getIdProperty() {
        return this.idProperty;
    }

    @Override
    public JPAModelProperty getVersionProperty() {
        return this.versionProperty;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void afterPropertiesDetermined() {
        JPAModelClassImpl superClass = (JPAModelClassImpl)this.getSuperClass();
        JPAModelPropertyImpl idProperty = null;
        JPAModelPropertyImpl versionProperty = null;
        if (superClass != null) {
            idProperty = superClass.idProperty;
            versionProperty = superClass.versionProperty;
        }
        Collection<JPAModelPropertyImpl> declaredProperties = (Collection)this.getDeclaredProperties().values();
        if (idProperty == null) {
            for (JPAModelPropertyImpl declaredProperty : declaredProperties) {
                if (declaredProperty.getScalarType() == JPAScalarType.ID) {
                    idProperty = declaredProperty;
                    break;
                }
            }
        }
        if (versionProperty == null) {
            for (JPAModelPropertyImpl declaredProperty : declaredProperties) {
                if (declaredProperty.getScalarType() == JPAScalarType.VERSION) {
                    versionProperty = declaredProperty;
                    break;
                }
            }
        }
        this.idProperty = idProperty;
        this.versionProperty = versionProperty;
    }

    @Override
    protected final EqualityComparator<?> createDefaultEqualityComparator() {
        if (this.getType() == ModelType.REFERENCE) {
            return this.createDefaultEntityEqualityComparator();
        }
        return super.createDefaultEqualityComparator();
    }
    
    protected DefaultEntityEqualityComparator createDefaultEntityEqualityComparator() {
        return new DefaultEntityEqualityComparator(this);
    }
    
    protected static class DefaultEntityEqualityComparator implements FrozenEqualityComparator<Object>, Serializable {

        private static final long serialVersionUID = -7744522574068198251L;
        
        private JPAModelClass modelClass;
        
        private Class<?> entityType;
        
        private int idPropertyId;
        
        public DefaultEntityEqualityComparator(JPAModelClass modelClass) {
            this.modelClass = modelClass;
            this.entityType = modelClass.getJavaType();
            this.idPropertyId = modelClass.getIdProperty().getId();
        }

        @Override
        public boolean equals(Object o1, Object o2) {
            if (!this.entityType.isInstance(o1)) {
                throw new ClassCastException();
            }
            if (!this.entityType.isInstance(o2)) {
                throw new ClassCastException();
            }
            ObjectModel om1 = ((ObjectModelProvider)o1).objectModel();
            ObjectModel om2 = ((ObjectModelProvider)o2).objectModel();
            if (om1.get(this.idPropertyId) == null) {
                return this.isSameEntity(o1, o2);
            }
            return om1.equalsScalar(this.idPropertyId, StringComparatorType.SENSITIVE, om2);
        }

        @Override
        public int hashCode(Object o) {
            ObjectModel om = ((ObjectModelProvider)o).objectModel();
            if (om.get(this.idPropertyId) == null) {
                return System.identityHashCode(o);
            }
            return om.hashCodeScalar(this.idPropertyId, StringComparatorType.SENSITIVE);
        }

        @Override
        public void freeze(Object obj, FrozenContext<Object> ctx) {
            ObjectModel om = ((ObjectModelProvider)obj).objectModel();
            om.freezeScalar(this.idPropertyId, ctx);
        }

        @Override
        public void unfreeze(Object obj, FrozenContext<Object> ctx) {
            ObjectModel om = ((ObjectModelProvider)obj).objectModel();
            om.unfreezeScalar(this.idPropertyId, ctx);
        }
        
        protected boolean isSameEntity(Object a, Object b) {
            return a == b;
        }

        protected Object writeReplace() throws ObjectStreamException {
            return new Serialization(this.modelClass);
        }
        
        private static class Serialization implements Serializable {
            
            private static final long serialVersionUID = 2828472628580135041L;
            
            private JPAModelClass modelClass;
            
            private Serialization(JPAModelClass modelClass) {
                this.modelClass = modelClass;
            }
            
            private Object readResolve() throws ObjectStreamException {
                return this.modelClass.getDefaultEqualityComparator();
            }
        }
    }
    
    @I18N
    private static native String onlyUsesObjectModel4Java(
            Class<?> javaType,
            Class<Model> modelTypeConstant,
            Class<JPAModel> JPAmodelTypeConstant);
}
