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
package org.babyfish.persistence.criteria.spi;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.babyfish.lang.I18N;

/**
 * @author Tao Chen
 */
public class MapKeyPath<K> extends AbstractPath<K> {

    private static final long serialVersionUID = -2255355353123105873L;
    
    MapKeyAttribute<K> mapKeyAttribute;
    
    protected MapKeyPath(MapAttributeJoin<?, K, ?> mapAttributeJoin) {
        super(mapAttributeJoin.getCriteriaBuilder(), mapAttributeJoin);
        this.mapKeyAttribute = new MapKeyAttribute<>(mapAttributeJoin);
    }

    @Override
    public Bindable<K> getModel() {
        return this.mapKeyAttribute;
    }

    @Override
    public Class<? extends K> getJavaType() {
        return this.mapKeyAttribute.getJavaType();
    }

    @Override
    protected boolean isReferenceable() {
        return this.mapKeyAttribute.getPersistentAttributeType() != PersistentAttributeType.BASIC;
    }

    @Override
    protected Attribute<? super K, ?> onLocateAttribute(String attributeName) {
        throw new UnsupportedOperationException(unSupportedByMapKeyPath(MapKeyPath.class)); 
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapAttributeJoin<?, K, ?> getParentPath() {
        return (MapAttributeJoin<?, K, ?>)super.getParentPath();
    }

    private static class MapKeyAttribute<K> implements SingularAttribute<Map<K, ?>, K>, Serializable {
        
        private static final long serialVersionUID = -2988284156889184044L;

        private static final Method ENTRY_GET_KEY_METHOD;
        
        private MapAttributeJoin<?, K, ?> mapAttributeJoin;

        MapKeyAttribute(MapAttributeJoin<?, K, ?> mapAttributeJoin) {
            this.mapAttributeJoin = mapAttributeJoin;
        }

        @Override
        public ManagedType<Map<K, ?>> getDeclaringType() {
            throw new UnsupportedOperationException(unSupportedByMapKeyPath(MapKeyPath.class));
        }

        @Override
        public Member getJavaMember() {
            return ENTRY_GET_KEY_METHOD;
        }

        @Override
        public String getName() {
            return "key";
        }

        @Override
        public Type<K> getType() {
            return this.mapAttributeJoin.getAttribute().getKeyType();
        }

        @Override
        public BindableType getBindableType() {
            return this.isAssociation() ? BindableType.ENTITY_TYPE : BindableType.SINGULAR_ATTRIBUTE;
        }

        @Override
        public Class<K> getJavaType() {
            return this.mapAttributeJoin.getAttribute().getKeyJavaType();
        }

        @Override
        public Class<K> getBindableJavaType() {
            return this.mapAttributeJoin.getAttribute().getKeyJavaType();
        }

        @Override
        public PersistentAttributeType getPersistentAttributeType() {
            Type<K> keyType = this.mapAttributeJoin.getAttribute().getKeyType();
            if (keyType instanceof IdentifiableType<?>) {
                return PersistentAttributeType.MANY_TO_ONE;
            }
            if (keyType instanceof ManagedType<?>) {
                return PersistentAttributeType.EMBEDDED;
            }
            return PersistentAttributeType.BASIC;
        }

        @Override
        public boolean isAssociation() {
            return this.mapAttributeJoin.getAttribute().getKeyType() instanceof IdentifiableType<?>;
        }

        @Override
        public boolean isCollection() {
            return false;
        }

        @Override
        public boolean isId() {
            return false;
        }

        @Override
        public boolean isOptional() {
            return false;
        }

        @Override
        public boolean isVersion() {
            return false;
        }
        
        static {
            Method entryGetKeyMethod;
            try {
                entryGetKeyMethod = Entry.class.getDeclaredMethod("getKey");
            } catch (NoSuchMethodException ex) {
                throw new AssertionError("Internal bug", ex);
            }
            ENTRY_GET_KEY_METHOD = entryGetKeyMethod;
        }
        
    }
    
    @SuppressWarnings("rawtypes")
    @I18N
    private static native String unSupportedByMapKeyPath(Class<MapKeyPath> mapKeyPathType);
}
