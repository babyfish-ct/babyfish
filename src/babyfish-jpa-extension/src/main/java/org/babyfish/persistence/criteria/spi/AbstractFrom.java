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

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.model.jpa.path.CollectionFetchType;
import org.babyfish.persistence.criteria.JoinMode;
import org.babyfish.persistence.criteria.XCollectionJoin;
import org.babyfish.persistence.criteria.XCommonAbstractCriteria;
import org.babyfish.persistence.criteria.XFetch;
import org.babyfish.persistence.criteria.XFetchParent;
import org.babyfish.persistence.criteria.XFrom;
import org.babyfish.persistence.criteria.XJoin;
import org.babyfish.persistence.criteria.XListJoin;
import org.babyfish.persistence.criteria.XMapJoin;
import org.babyfish.persistence.criteria.XSetJoin;

/**
 * @author Tao Chen
 */
public abstract class AbstractFrom<Z, X> extends AbstractPath<X> implements XFrom<Z, X> {

    private static final long serialVersionUID = 5352725681261677772L;
    
    private static final JoinType DEFAULT_JOIN_TYPE = JoinType.INNER;
    
    private static final JoinMode DEFAULT_JOIN_MODE = JoinMode.OPTIONALLY_MERGE_EXISTS;
    
    private XCommonAbstractCriteria commonAbstractCriteria;
    
    private AbstractFrom<Z, X> correlationParent;
    
    private XOrderedSet<XJoin<X, ?>> joins;
    
    private XOrderedSet<XFetch<X, ?>> fetches;
        
    protected AbstractFrom(XCommonAbstractCriteria commonAbstractCriteria, Path<?> parentPath) {
        super(commonAbstractCriteria.getCriteriaBuilder(), parentPath);
        this.commonAbstractCriteria = commonAbstractCriteria;
    }
    
    protected AbstractFrom(AbstractFrom<Z, X> correlationParent) {
        super(correlationParent.getCriteriaBuilder(), correlationParent.getParentPath());
        this.commonAbstractCriteria = correlationParent.getCommonAbstractCriteria();
        this.correlationParent = correlationParent;
    }
    
    @SuppressWarnings("unchecked")
    public final <T extends XCommonAbstractCriteria> T getCommonAbstractCriteria() {
        return (T)this.commonAbstractCriteria;
    }
    
    @Override
    protected final boolean isReferenceable() {
        return true;
    }
    
    protected abstract boolean isJoinSource();
    
    @Override
    protected final Attribute<? super X, ?> onLocateAttribute(String attributeName) {
        return this.onLocateManagedType().getAttribute(attributeName);
    }
    
    @SuppressWarnings("unchecked")
    protected ManagedType<? super X> onLocateManagedType() {
        return (ManagedType<? super X>)this.getModel();
    }
    
    @Override
    public XFrom<? super Z, ? super X> getTreatedParent() {
        return null;
    }
    
    @Override
    public boolean isCorrelated() {
        return this.getCorrelationParent() != null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public XOrderedSet<Fetch<X, ?>> getFetches() {
        return (XOrderedSet)this.getXFetches();
    }
    
    @Override
    public XOrderedSet<XFetch<X, ?>> getXFetches() {
        XOrderedSet<XFetch<X, ?>> fetches = this.fetches;
        if (fetches == null) {
            return MACollections.emptyOrderedSet();
        }
        return MACollections.unmodifiable(fetches);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public XOrderedSet<Join<X, ?>> getJoins() {
        return (XOrderedSet)this.getXJoins();
    }
    
    @Override
    public XOrderedSet<XJoin<X, ?>> getXJoins() {
        XOrderedSet<XJoin<X, ?>> joins = this.joins;
        if (joins == null) {
            return MACollections.emptyOrderedSet();
        }
        return MACollections.unmodifiable(joins);
    }

    @Override
    public XFrom<Z, X> getCorrelationParent() { 
        return this.correlationParent; 
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public XFrom<Z, X> alias(String alias) {
        return (XFrom<Z, X>)super.alias(alias);
    }

    @Override
    public <Y> XFetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute) {
        return this.fetch(attribute, DEFAULT_JOIN_TYPE);
    }

    @Override
    public <Y> XFetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute, JoinType jt) {
        this.checkState();
        return this.fetchImpl(attribute, jt, CollectionFetchType.ALL); 
    }

    @Override
    public <Y> XFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute) { 
        return this.fetch(attribute, DEFAULT_JOIN_TYPE, CollectionFetchType.ALL); 
    }

    @Override
    public <Y> XFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt) { 
        return this.fetch(attribute, jt, CollectionFetchType.ALL); 
    }

    @Override
    public <Y> XFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, CollectionFetchType cft) {
        return this.fetch(attribute, DEFAULT_JOIN_TYPE, cft); 
    }

    @Override
    public <Y> XFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt, CollectionFetchType cft) {
        this.checkState();
        return this.fetchImpl(attribute, jt, cft);
    }
    
    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XFetch<X, Y> fetch(String attributeName) { 
        return this.fetch(attributeName, DEFAULT_JOIN_TYPE, CollectionFetchType.ALL); 
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XFetch<X, Y> fetch(String attributeName, JoinType jt) {
        return this.fetch(attributeName, jt, CollectionFetchType.ALL);
    }

    @SuppressWarnings("hiding")
    public <X, Y> XFetch<X, Y> fetch(String attributeName, CollectionFetchType cft) { 
        return this.fetch(attributeName, DEFAULT_JOIN_TYPE, cft);
    }

    @SuppressWarnings({ "unchecked", "rawtypes", "hiding" })
    public <X, Y> XFetch<X, Y> fetch(String attributeName, JoinType jt, CollectionFetchType cft) {
        this.checkState();
        Attribute attribute = (Attribute)this.locateAttribute(attributeName);
        return (XFetch)this.fetchImpl(attribute, jt, cft);
    }
    
    @Override
    public <Y> XJoin<X, Y> join(SingularAttribute<? super X, Y> attribute) { 
        return this.join(attribute, DEFAULT_JOIN_TYPE, DEFAULT_JOIN_MODE); 
    }

    @Override
    public <Y> XJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType jt) { 
        return this.join(attribute, jt, DEFAULT_JOIN_MODE); 
    }

    @Override
    public <Y> XJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinMode jm) {
        return this.join(attribute, DEFAULT_JOIN_TYPE, jm);
    }

    @Override
    public <Y> XJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType jt, JoinMode jm) {
        this.checkState();
        return this.joinImpl(attribute, jt, jm);
    }

    @Override
    public <Y> XCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection) { 
        return this.join(collection, DEFAULT_JOIN_TYPE, DEFAULT_JOIN_MODE); 
    }

    @Override
    public <Y> XCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType jt) {
        return this.join(collection, jt, DEFAULT_JOIN_MODE);
    }

    @Override
    public <Y> XCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinMode jm) {
        return this.join(collection, DEFAULT_JOIN_TYPE, jm);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Y> XCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType jt, JoinMode jm) {
        this.checkState();
        return (XCollectionJoin<X, Y>)this.joinImpl(collection, jt, jm);
    }

    @Override
    public <Y> XSetJoin<X, Y> join(SetAttribute<? super X, Y> set) { 
        return this.join(set, DEFAULT_JOIN_TYPE, DEFAULT_JOIN_MODE); 
    }

    @Override
    public <Y> XSetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt) {
        return this.join(set, jt, DEFAULT_JOIN_MODE);
    }

    @Override
    public <Y> XSetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinMode jm) {
        return this.join(set, DEFAULT_JOIN_TYPE, jm);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Y> XSetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt, JoinMode jm) {
        this.checkState();
        return (XSetJoin<X, Y>)this.joinImpl(set, jt, jm);
    }

    @Override
    public <Y> XListJoin<X, Y> join(ListAttribute<? super X, Y> list) {
        return this.join(list, DEFAULT_JOIN_TYPE, DEFAULT_JOIN_MODE);
    }

    @Override
    public <Y> XListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType jt) {
        return this.join(list, jt, DEFAULT_JOIN_MODE);
    }

    @Override
    public <Y> XListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinMode jm) {
        return this.join(list, DEFAULT_JOIN_TYPE, jm);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Y> XListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType jt, JoinMode jm) {
        this.checkState();
        return (XListJoin<X, Y>)this.joinImpl(list, jt, jm);
    }

    @Override
    public <K, V> XMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map) { 
        return this.join(map, DEFAULT_JOIN_TYPE, DEFAULT_JOIN_MODE); 
    }

    @Override
    public <K, V> XMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType jt) {
        return this.join(map, jt, DEFAULT_JOIN_MODE);
    }

    @Override
    public <K, V> XMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinMode jm) {
        return this.join(map, DEFAULT_JOIN_TYPE, jm);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> XMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType jt, JoinMode jm) {
        this.checkState();
        return (XMapJoin<X, K, V>)this.joinImpl(map, jt, jm);
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XJoin<X, Y> join(String attributeName) {
        return this.join(attributeName, DEFAULT_JOIN_TYPE, DEFAULT_JOIN_MODE);
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XJoin<X, Y> join(String attributeName, JoinType jt) {
        return this.join(attributeName, jt, DEFAULT_JOIN_MODE);
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XJoin<X, Y> join(String attributeName, JoinMode jm) {
        return this.join(attributeName, DEFAULT_JOIN_TYPE, jm);
    } 

    @SuppressWarnings({ "hiding", "unchecked", "rawtypes" })
    @Override
    public <X, Y> XJoin<X, Y> join(String attributeName, JoinType jt, JoinMode jm) {
        this.checkState();
        Attribute attribute = this.locateAttribute(attributeName);
        return (XJoin)this.joinImpl(attribute, jt, jm); 
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XCollectionJoin<X, Y> joinCollection(String attributeName) {
        return this.joinCollection(attributeName, DEFAULT_JOIN_TYPE, DEFAULT_JOIN_MODE);
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XCollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt) {
        return this.joinCollection(attributeName, jt, DEFAULT_JOIN_MODE);
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XCollectionJoin<X, Y> joinCollection(String attributeName, JoinMode jm) {
        return this.joinCollection(attributeName, DEFAULT_JOIN_TYPE, jm);
    } 

    @SuppressWarnings({ "hiding", "unchecked", "rawtypes" })
    @Override
    public <X, Y> XCollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt, JoinMode jm) {
        this.checkState();
        Attribute attribute = this.locateAttribute(attributeName);
        return (XCollectionJoin<X, Y>)this.joinImpl(attribute, jt, jm); 
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XSetJoin<X, Y> joinSet(String attributeName) {
        return this.joinSet(attributeName, DEFAULT_JOIN_TYPE, DEFAULT_JOIN_MODE);
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XSetJoin<X, Y> joinSet(String attributeName, JoinType jt) {
        return this.joinSet(attributeName, jt, DEFAULT_JOIN_MODE);
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XSetJoin<X, Y> joinSet(String attributeName, JoinMode jm) {
        return this.joinSet(attributeName, DEFAULT_JOIN_TYPE, jm);
    }

    @SuppressWarnings({ "hiding", "unchecked", "rawtypes" })
    @Override
    public <X, Y> XSetJoin<X, Y> joinSet(String attributeName, JoinType jt, JoinMode jm) {
        this.checkState();
        Attribute attribute = this.locateAttribute(attributeName);
        return (XSetJoin<X, Y>)this.joinImpl(attribute, jt, jm);
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XListJoin<X, Y> joinList(String attributeName) {
        return this.joinList(attributeName, DEFAULT_JOIN_TYPE, DEFAULT_JOIN_MODE);
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XListJoin<X, Y> joinList(String attributeName, JoinType jt) {
        return this.joinList(attributeName, jt, DEFAULT_JOIN_MODE);
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, Y> XListJoin<X, Y> joinList(String attributeName, JoinMode jm) {
        return this.joinList(attributeName, DEFAULT_JOIN_TYPE, jm);
    }

    @SuppressWarnings({ "hiding", "unchecked", "rawtypes" })
    @Override
    public <X, Y> XListJoin<X, Y> joinList(String attributeName, JoinType jt, JoinMode jm) {
        this.checkState();
        Attribute attribute = this.locateAttribute(attributeName);
        return (XListJoin<X, Y>)this.joinImpl(attribute, jt, jm);
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, K, V> XMapJoin<X, K, V> joinMap(String attributeName) {
        return this.joinMap(attributeName, DEFAULT_JOIN_TYPE, DEFAULT_JOIN_MODE);
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, K, V> XMapJoin<X, K, V> joinMap(String attributeName, JoinType jt) {
        return this.joinMap(attributeName, jt, DEFAULT_JOIN_MODE);
    }

    @SuppressWarnings("hiding")
    @Override
    public <X, K, V> XMapJoin<X, K, V> joinMap(String attributeName, JoinMode jm) {
        return this.joinMap(attributeName, DEFAULT_JOIN_TYPE, jm);
    } 

    @SuppressWarnings({ "hiding", "unchecked", "rawtypes" })
    @Override
    public <X, K, V> XMapJoin<X, K, V> joinMap(String attributeName, JoinType jt, JoinMode jm) {
        this.checkState();
        Attribute attribute = this.locateAttribute(attributeName);
        return (XMapJoin<X, K, V>)this.joinImpl(attribute, jt, jm); 
    }
    
    @SuppressWarnings("unchecked")
    private <Y> XFetch<X, Y> fetchImpl(
            Attribute<? super X, ?> attribute, 
            JoinType jt, 
            CollectionFetchType cft) {
        if (this.correlationParent != null) {
            return this.correlationParent.fetchImpl(attribute, jt, cft);
        }
        XFetch<X, Y> fetch;
        if (attribute instanceof SingularAttribute<?, ?>) {
            fetch = new FetchImpl<X, Y>(this, (SingularAttribute<X, Y>)attribute, jt);
        } else {
            fetch = new FetchImpl<X, Y>(this, (PluralAttribute<X, ?, Y>)attribute, jt, cft);
        }
        XOrderedSet<XFetch<X, ?>> fetches = this.fetches;
        if (fetches == null) {
            this.fetches = fetches = new LinkedHashSet<>();
        }
        fetches.add(fetch);
        return fetch;
    }

    @SuppressWarnings("unchecked")
    private <Y> XJoin<X, Y> joinImpl(
            Attribute<? super X, Y> attribute,
            JoinType jt, 
            JoinMode jm) {
        if (this.correlationParent != null) {
            return this.correlationParent.joinImpl(attribute, jt, jm);
        }
        if (!this.isJoinSource()) {
            throw new IllegalStateException(isNotJoinSource());
        }
        XJoin<X, Y> join;
        if (attribute instanceof SingularAttribute<?, ?>) {
            join = new SingularAttributeJoin<>(this, (SingularAttribute<X, ?>)attribute, jt, jm);
        } else if (attribute instanceof MapAttribute<?, ?, ?>) {
            join = new MapAttributeJoin<>(this, (MapAttribute<X, Object, Y>)attribute, jt, jm);
        } else if (attribute instanceof ListAttribute<?, ?>) {
            join = new ListAttributeJoin<>(this, (ListAttribute<X, Y>)attribute, jt, jm);
        } else if (attribute instanceof SetAttribute<?, ?>) {
            join = new SetAttributeJoin<>(this, (SetAttribute<X, Y>)attribute, jt, jm);
        } else if (attribute instanceof CollectionAttribute<?, ?>) {
            join = new CollectionAttributeJoin<>(this, (CollectionAttribute<X, Y>)attribute, jt, jm);
        } else {
            Arguments.mustBeInstanceOfAnyOfValue(
                    "attribute", 
                    attribute, 
                    SingularAttribute.class,
                    MapAttribute.class,
                    ListAttribute.class,
                    SetAttribute.class,
                    CollectionAttribute.class);
            throw new AssertionError();//Unreachable code, only use to ingnore compile errors
        }
        XOrderedSet<XJoin<X, ?>> joins = this.joins;
        if (joins == null) {
            this.joins = joins = new LinkedHashSet<>();
        }
        joins.add(join);
        return join;
    }

    private static class FetchImpl<Z, X> implements XFetch<Z, X>, Serializable {
        
        private static final long serialVersionUID = 1809724313612744893L;
        
        private AbstractFrom<?, ?> owner;

        private XFetchParent<?, Z> parent;
        
        private Attribute<? super Z, ?> attribute;
        
        private JoinType joinType;
        
        private CollectionFetchType collectionFetchType;
        
        private XOrderedSet<XFetch<X, ?>> fetches;
        
        public FetchImpl(
                XFetchParent<?, Z> parent,
                SingularAttribute<? super Z, ?> attribute, 
                JoinType joinType) {
            this.parent = parent;
            if (parent instanceof AbstractFrom<?, ?>) {
                this.owner = (AbstractFrom<?, ?>)parent;
            } else {
                this.owner = ((FetchImpl<?, ?>)parent).owner;
            }
            this.attribute = attribute;
            this.joinType = joinType;
            this.collectionFetchType = CollectionFetchType.ALL;
        }
        
        public FetchImpl(
                XFetchParent<?, Z> parent,
                PluralAttribute<? super Z, ?, ?> attribute, 
                JoinType joinType,
                CollectionFetchType collectionFetchType) {
            this.parent = parent;
            if (parent instanceof AbstractFrom<?, ?>) {
                this.owner = (AbstractFrom<?, ?>)parent;
            } else {
                this.owner = ((FetchImpl<?, ?>)parent).owner;
            }
            this.attribute = attribute;
            this.joinType = joinType;
            this.collectionFetchType = collectionFetchType;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public XOrderedSet<Fetch<X, ?>> getFetches() {
            return (XOrderedSet)this.getXFetches();
        }

        @Override
        public XOrderedSet<XFetch<X, ?>> getXFetches() {
            XOrderedSet<XFetch<X, ?>> fetches = this.fetches;
            if (fetches == null) {
                return MACollections.emptyOrderedSet();
            }
            return MACollections.unmodifiable(fetches);
        }

        @Override
        public XFetchParent<?, Z> getParent() {
            return this.parent;
        }

        @Override
        public Attribute<? super Z, ?> getAttribute() {
            return this.attribute;
        }

        @Override
        public JoinType getJoinType() {
            return this.joinType;
        }
        
        @Override
        public CollectionFetchType getCollectionFetchType() {
            return this.collectionFetchType;
        }

        @Override
        public <Y> XFetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute) {
            return this.fetch(attribute, DEFAULT_JOIN_TYPE);
        }

        @Override
        public <Y> XFetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute, JoinType jt) {
            this.owner.checkState();
            return this.fetchImpl(attribute, jt, CollectionFetchType.ALL);
        }

        @Override
        public <Y> XFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute) { 
            return this.fetch(attribute, DEFAULT_JOIN_TYPE, CollectionFetchType.ALL);
        }

        @Override
        public <Y> XFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt) { 
            return this.fetch(attribute, jt, CollectionFetchType.ALL); 
        }

        @Override
        public <Y> XFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, CollectionFetchType cft) {
            return this.fetch(attribute, DEFAULT_JOIN_TYPE, cft); 
        }

        @Override
        public <Y> XFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt, CollectionFetchType cft) { 
            this.owner.checkState();
            return this.fetchImpl(attribute, jt, cft);
        }
        
        @SuppressWarnings("hiding")
        @Override
        public <X, Y> XFetch<X, Y> fetch(String attributeName) { 
            return this.fetch(attributeName, DEFAULT_JOIN_TYPE, CollectionFetchType.ALL); 
        }

        @SuppressWarnings("hiding")
        @Override
        public <X, Y> XFetch<X, Y> fetch(String attributeName, JoinType jt) {
            return this.fetch(attributeName, jt, CollectionFetchType.ALL);
        }

        @SuppressWarnings("hiding")
        @Override
        public <X, Y> XFetch<X, Y> fetch(String attributeName, CollectionFetchType cft) { 
            return this.fetch(attributeName, DEFAULT_JOIN_TYPE, cft);
        }

        @SuppressWarnings({ "unchecked", "rawtypes", "hiding" })
        @Override
        public <X, Y> XFetch<X, Y> fetch(String attributeName, JoinType jt, CollectionFetchType cft) {
            this.owner.checkState();
            Attribute<?, ?> attribute = this.locateAttribute(attributeName);
            return (XFetch)this.fetchImpl(attribute, jt, cft);
        }
        
        @SuppressWarnings("unchecked")
        private <Y> XFetch<X, Y> fetchImpl(
                Attribute<?, ?> attribute, 
                JoinType jt, 
                CollectionFetchType cft) {
            XFetch<X, Y> fetch;
            if (attribute instanceof SingularAttribute<?, ?>) {
                fetch = new FetchImpl<X, Y>(this, (SingularAttribute<X, Y>)attribute, jt);
            } else {
                fetch = new FetchImpl<X, Y>(this, (PluralAttribute<X, ?, Y>)attribute, jt, cft);
            }
            XOrderedSet<XFetch<X, ?>> fetches = this.fetches;
            if (fetches == null) {
                this.fetches = fetches = new LinkedHashSet<>();
            }
            fetches.add(fetch);
            return fetch;
        }
        
        @SuppressWarnings("unchecked")
        private <Y> Attribute<X, Y> locateAttribute(String attributeName) {
            ManagedType<X> managedType;
            Attribute<? super Z, ?> parentAttribute = this.attribute;
            if (parentAttribute instanceof SingularAttribute<?, ?> &&
                    Attribute.PersistentAttributeType.EMBEDDED == parentAttribute.getPersistentAttributeType()) {
                managedType = (ManagedType<X>)parentAttribute;
            } else {
                managedType = 
                        (ManagedType<X>)
                        ((AbstractNode)parent)
                        .getCriteriaBuilder()
                        .getEntityManagerFactory()
                        .getMetamodel()
                        .managedType(parentAttribute.getJavaType());
            }
            return (Attribute<X, Y>)managedType.getAttribute(attributeName);
        }

    }
    
    @I18N
    private static native String isNotJoinSource();
}
