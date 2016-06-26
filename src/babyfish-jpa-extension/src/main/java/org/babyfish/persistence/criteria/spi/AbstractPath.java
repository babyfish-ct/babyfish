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

import java.util.Collection;
import java.util.Map;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.babyfish.collection.HashMap;
import org.babyfish.lang.I18N;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.expression.PathTypeExpression;
import org.babyfish.persistence.criteria.expression.PriorityConstants;

/**
 * @author Tao Chen
 */
public abstract class AbstractPath<X> extends AbstractExpression<X> implements Path<X> {

    private static final long serialVersionUID = 1763608393496615508L;
    
    private Path<?> parentPath;
    
    private Map<String, AbstractPath<?>> pathMap;
    
    private transient Expression<Class<? extends X>> typeExpression;

    protected AbstractPath(XCriteriaBuilder criteriaBuilder, Path<?> parentPath) {
        super(criteriaBuilder);
        this.mustUnderSameCriteriaBuilder("parentPath", parentPath);
        this.parentPath = parentPath;
    }
    
    protected abstract boolean isReferenceable();
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final <Y> Attribute<X, Y> locateAttribute(String attributeName) {
        Attribute<X, Y> attribute = (Attribute)this.onLocateAttribute(attributeName);
        if ( attribute == null ) {
            this.throwUnknowAttributeException(attributeName);
        }
        return attribute;
    }
    
    protected abstract Attribute<? super X, ?> onLocateAttribute(String attributeName);
    
    protected void throwIllegalReferenceException() {
        throw new IllegalArgumentException(illegalReference());
    }
    
    protected void throwUnknowAttributeException(String attributeName) {
        throw new IllegalArgumentException(illegalAttributeName(attributeName));
    }

    @Override
    public <Y> Path<Y> get(SingularAttribute<? super X, Y> attribute) {
        if (!this.isReferenceable()) {
            this.throwIllegalReferenceException();
        }
        Map<String, SingularAttributePath<Y>> pathMap = this.<Y, SingularAttributePath<Y>>getPathMap();
        SingularAttributePath<Y> path = pathMap.get(attribute.getName());
        if (path == null) {
            path = new SingularAttributePath<Y>(this, attribute);
            pathMap.put(attribute.getName(), path);
        }
        return path;
    }

    @Override
    public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<X, C, E> collection) {
        if (!this.isReferenceable()) {
            this.throwIllegalReferenceException();
        }
        Map<String, PluralAttributePath<C>> pathMap = this.<C, PluralAttributePath<C>>getPathMap();
        PluralAttributePath<C> path = pathMap.get(collection.getName());
        if (path == null) {
            path = new PluralAttributePath<C>(this, collection);
            pathMap.put(collection.getName(), path);
        }
        return path;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<X, K, V> map) {
        if (!this.isReferenceable()) {
            this.throwIllegalReferenceException();
        }
        Map<String, PluralAttributePath<M>> pathMap = this.<M, PluralAttributePath<M>>getPathMap();
        PluralAttributePath<M> path = pathMap.get(map.getName());
        if (path == null) {
            path = new PluralAttributePath<M>(this, (PluralAttribute<?, M, ?>)map);
        }
        return path;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <Y> Path<Y> get(String attributeName) {
        if (!this.isReferenceable()) {
            this.throwIllegalReferenceException();
        }
        final Attribute<X, Y> attribute = this.locateAttribute(attributeName);
        if (attribute.isCollection()) {
            if (((PluralAttribute<?, ?, ?>)attribute).getCollectionType() == PluralAttribute.CollectionType.MAP) {
                return (Path)this.get((MapAttribute<X, Map<Object, Object>, Object>)attribute);
            }
            return (Path)this.<Object, Collection<Object>>get((PluralAttribute<X, Collection<Object>, Object>)attribute);
        }
        return this.get((SingularAttribute<X,Y>) attribute);
    }

    @Override
    public Path<?> getParentPath() {
        return this.parentPath;
    }

    @Override
    public Expression<Class<? extends X>> type() {
        Expression<Class<? extends X>> typeExpression = this.typeExpression;
        if (typeExpression == null) {
            this.typeExpression = 
                    typeExpression = 
                    new PathTypeExpression<X>(this.getCriteriaBuilder(), this);
        }
        return typeExpression;
    }
    
    @Override
    public final void accept(Visitor visitor) {
        visitor.visitPath(this);
    }
    
    @Override
    public int getPriority() {
        return PriorityConstants.HIGHEST;
    }
    
    public Path<? super X> getTreatedParent() {
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <Y, P extends AbstractPath<Y>> Map<String, P> getPathMap() {
        Map<String, P> pathMap = (Map)this.pathMap;
        if (pathMap == null) {
            pathMap = new HashMap<String, P>();
            this.pathMap = (Map)pathMap;
        }
        return pathMap;
    }

    @I18N
    private static native String illegalReference();

    @I18N
    private static native String illegalAttributeName(String attributeName);
}
