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

import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.PluralJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.EntityType;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.persistence.criteria.XAbstractQuery;
import org.babyfish.persistence.criteria.XRoot;

/**
 * @author Tao Chen
 */
class QueryStructure<T> extends CommonCriteriaStructure {
    
    private Class<T> resultType;
    
    private boolean distinct;
    
    private Selection<T> selection;
    
    private XOrderedSet<XRoot<?>> roots;
    
    private List<Expression<?>> groupList;
    
    private Predicate groupRestriction;
    
    QueryStructure(XAbstractQuery<T> owner, Class<T> resultType) {
        super(owner);
        this.resultType = resultType;
        this.roots = new LinkedHashSet<XRoot<?>>();
    }
    
    public Class<T> getResultType() {
        return this.resultType;
    }

    public boolean isDistinct() {
        return this.distinct;
    }
    
    @SuppressWarnings("unchecked")
    public <S extends Selection<T>> S getSelection() {
        return (S)this.selection;
    }
    
    public XOrderedSet<XRoot<?>> getXRoots() {
        return this.roots;
    }

    public List<Expression<?>> getGroupList() {
        List<Expression<?>> groupList = this.groupList;
        if (groupList == null) {
            return MACollections.emptyList();
        }
        return groupList;
    }

    public Predicate getGroupRestriction() {
        return this.groupRestriction;
    }

    public void distinct(boolean distinct) {
        this.distinct = distinct;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void select(Selection<? extends T> selection) {
        if (selection != null) {
            Class<?> selectionType;
            if (selection instanceof PluralJoin<?, ?, ?>) {
                PluralJoin<?, ?, ?> pluralJoin = (PluralJoin<?, ?, ?>)selection;
                selectionType = pluralJoin.getModel().getElementType().getJavaType();
            } else {
                selectionType = selection.getJavaType();
                if (selectionType.isPrimitive()) {
                    if (selectionType == boolean.class) {
                        selectionType = Boolean.class;
                    } else if (selectionType == char.class) {
                        selectionType = Character.class;
                    } else if (selectionType == byte.class) {
                        selectionType = Byte.class;
                    } else if (selectionType == short.class) {
                        selectionType = Short.class;
                    } else if (selectionType == int.class) {
                        selectionType = Integer.class;
                    } else if (selectionType == long.class) {
                        selectionType = Long.class;
                    } else if (selectionType == float.class) {
                        selectionType = Float.class;
                    } else if (selectionType == double.class) {
                        selectionType = Double.class;
                    }
                }
            }
            if (!this.getResultType().isAssignableFrom(selectionType))
            throw new IllegalArgumentException(
                    "Can not set a selection whose java type is \"" +
                    selectionType.getName() +
                    "\" because it is not the query result type \"" +
                    this.getResultType().getName() +
                    "\" or its derived type.");
        }
        this.selection = (Selection)selection;
    }

    public <X> XRoot<X> from(Class<X> entityClass) {
        XRoot<X> root = this.createRoot(entityClass);
        this.roots.add(root);
        return root;
    }

    public <X> XRoot<X> from(EntityType<X> entity) {
        XRoot<X> root = this.createRoot(entity);
        this.roots.add(root);
        return root;
    }

    public void groupBy(Expression<?>... grouping) {
        this.groupBy(MACollections.wrap(grouping));
    }

    public void groupBy(List<Expression<?>> grouping) {
        List<Expression<?>> groupList;
        if (grouping != null) {
            groupList = new ArrayList<Expression<?>>(grouping.size());
            for (Expression<?> expression : grouping) {
                if (expression != null) {
                    groupList.add(expression);
                }
            }
            if (groupList.isEmpty()) {
                groupList = null;
            } else {
                groupList = MACollections.unmodifiable(groupList);
            }
        } else {
            groupList = null;
        }
        this.groupList = groupList;
    }

    public void having(Expression<Boolean> restriction) {
        Predicate predicate = this.getOwner().getCriteriaBuilder().isTrue(restriction);
        this.groupRestriction = AbstractPredicate.isNullOrEmpty(predicate) ? null : predicate;
    }

    public void having(Predicate... restrictions) {
        Predicate predicate = this.getOwner().getCriteriaBuilder().and(restrictions);
        this.groupRestriction = AbstractPredicate.isNullOrEmpty(predicate) ? null : predicate;
    }
}
