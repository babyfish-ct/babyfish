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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.I18N;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.model.jpa.path.CollectionFetchType;
import org.babyfish.model.jpa.path.spi.EntityManagerFactoryConfigurable;
import org.babyfish.persistence.Constants;
import org.babyfish.persistence.criteria.Assignment;
import org.babyfish.persistence.criteria.JoinMode;
import org.babyfish.persistence.criteria.XAbstractQuery;
import org.babyfish.persistence.criteria.XCommonAbstractCriteria;
import org.babyfish.persistence.criteria.XCriteriaDelete;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XCriteriaUpdate;
import org.babyfish.persistence.criteria.XFetch;
import org.babyfish.persistence.criteria.XFetchParent;
import org.babyfish.persistence.criteria.XFrom;
import org.babyfish.persistence.criteria.XJoin;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.XSubquery;
import org.babyfish.persistence.criteria.expression.ParameterExpressionImpl;

/**
 * @author Tao Chen
 */
public class QueryContext implements AutoCloseable {
    
    private XCommonAbstractCriteria commonAbstractCriteria;
    
    private Map<XCommonAbstractCriteria, List<Entity>> rootEntitiyMap;
    
    private XOrderedMap<XFrom<?, ?>, Entity> fromEntityMap;
    
    private XOrderedMap<XFetch<?, ?>, Entity> fetchEntityMap;
    
    private XOrderedMap<PathId, PathNode> pathNodes;
    
    private XOrderedMap<String, ParameterExpression<?>> namedParameters;
    
    private List<ParameterExpression<?>> unnamedParameters;
    
    private PathId.Allocator pathIdAllocator = PathId.primaryAllocator();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public QueryContext(XCommonAbstractCriteria commonAbstractCriteria) {
        this.commonAbstractCriteria = Arguments.mustNotBeNull("commonAbstractCriteria", commonAbstractCriteria);
        PreVisitorImpl preVisitorImpl = this.new PreVisitorImpl();
        preVisitorImpl.visit(commonAbstractCriteria);
        if (!this.pathIdAllocator.isEmpty()) {
            throw new AssertionError();
        }
        this.rootEntitiyMap = preVisitorImpl.rootEntityMap;
        this.fromEntityMap = (XOrderedMap)preVisitorImpl.fromEntityImplMap;
        this.fetchEntityMap = (XOrderedMap)preVisitorImpl.fetchEntityImplMap;
        this.pathNodes = (XOrderedMap)MACollections.unmodifiable(preVisitorImpl.pathNodeImpls);
        this.namedParameters = MACollections.unmodifiable(preVisitorImpl.namedParameters);
        this.unnamedParameters = MACollections.unmodifiable(preVisitorImpl.unnamedParameters);
    }
    
    @Override
    public void close() {
        XCommonAbstractCriteria commonAbstractCriteria = this.commonAbstractCriteria;
        if (commonAbstractCriteria != null) {
            this.new PostVisitorImpl().visit(commonAbstractCriteria);
            this.commonAbstractCriteria = null;
            this.pathIdAllocator = null;
        }
    }
    
    public PathId.Allocator secondaryPathIdAllocator() {
        if (this.pathIdAllocator == null) {
            throw new IllegalStateException(queryContextIsClosed(QueryContext.class));
        }
        return this.pathIdAllocator.secondaryAllocator();
    }
    
    public XCommonAbstractCriteria getCommonAbstractCriteria() {
        XCommonAbstractCriteria commonAbstractCriteria = this.commonAbstractCriteria;
        if (commonAbstractCriteria == null) {
            throw new IllegalStateException(queryContextIsClosed(QueryContext.class));
        }
        return commonAbstractCriteria;
    }
    
    public List<Entity> getRootEntities(XCommonAbstractCriteria query) {
        if (this.commonAbstractCriteria == null) {
            throw new IllegalStateException(queryContextIsClosed(QueryContext.class));
        }
        return this.rootEntitiyMap.get(query);
    }
    
    public Entity getEntity(XFetchParent<?, ?> fetchParent) {
        if (this.commonAbstractCriteria == null) {
            throw new IllegalStateException(queryContextIsClosed(QueryContext.class));
        }
        if (fetchParent instanceof AbstractFrom<?, ?>) {
            XFrom<?, ?> treatedParent = ((AbstractFrom<?, ?>)fetchParent).getTreatedParent();
            if (treatedParent != null) {
                return getEntity(treatedParent);
            }
        }
        if (fetchParent instanceof XFrom<?, ?>) {
            XFrom<?, ?> from = getEntityMapKey((XFrom<?, ?>)fetchParent);
            return this.fromEntityMap.get(from);
        } else {
            return this.fetchEntityMap.get(fetchParent);
        }
    }
    
    public XOrderedMap<PathId, PathNode> getPathNodes() {
        if (this.commonAbstractCriteria == null) {
            throw new IllegalStateException(queryContextIsClosed(QueryContext.class));
        }
        return this.pathNodes;
    }
    
    public XOrderedMap<String, ParameterExpression<?>> getNamedParameters() {
        if (this.commonAbstractCriteria == null) {
            throw new IllegalStateException(queryContextIsClosed(QueryContext.class));
        }
        return this.namedParameters;
    }
    
    public List<ParameterExpression<?>> getUnnamedParameters() {
        if (this.commonAbstractCriteria == null) {
            throw new IllegalStateException(queryContextIsClosed(QueryContext.class));
        }
        return this.unnamedParameters;
    }
    
    private boolean isDbSchemaStrict() {
        EntityManagerFactory entityManagerFactory = this.commonAbstractCriteria.getCriteriaBuilder().getEntityManagerFactory();
        if (entityManagerFactory instanceof EntityManagerFactoryConfigurable) {
            return ((EntityManagerFactoryConfigurable)entityManagerFactory).isDbSchemaStrict();
        }
        return false;
    }
    
    private static XFrom<?, ?> getEntityMapKey(XFrom<?, ?> from) {
        XFrom<?, ?> treatedParent = ((AbstractFrom<?, ?>)from).getTreatedParent();
        if (treatedParent != null) {
            return getEntityMapKey(treatedParent);
        }
        XFrom<?, ?> correlationRoot = from;
        while (true) {
            from = from.getCorrelationParent();
            if (from == null) {
                break;
            }
            correlationRoot = from;
        }
        return correlationRoot;
    }

    public interface Entity {
        
        String getRenderAlias();
        
        ManagedType<?> getManagedType();
        
        Attribute<?, ?> getAttribute();
        
        JoinType getJoinType();
        
        JoinMode getJoinMode();
        
        boolean isFetch();
        
        boolean isUsed();
        
        boolean isExplicit();
        
        Predicate getOn();
        
        Entity getParent();
        
        List<Entity> getEntities();
    }
    
    public interface PathNode {
        
        Class<?> getTreatAsType();
        
        PathNode getParent();
        
        Entity getEntity();
        
        Attribute<?, ?> getAttribute();
    }
    
    private class PreVisitorImpl extends AbstractVisitor {
        
        Map<XCommonAbstractCriteria, List<Entity>> rootEntityMap = 
                new HashMap<>(
                        ReferenceEqualityComparator.getInstance(), 
                        (ReferenceEqualityComparator<List<Entity>>)null);
        
        /*
         * It is very important to keep the order of fromEntityImplMap!
         * because finally, the visitor will allocate the id for each entity, 
         * start the recursion from each XRoot<?> in the fromEntityImplMap
         */
        XOrderedMap<XFrom<?, ?>, EntityImpl> fromEntityImplMap = 
                new LinkedHashMap<>(
                        ReferenceEqualityComparator.getInstance(),
                        (ReferenceEqualityComparator<EntityImpl>)null);
                
        XOrderedMap<XFetch<?, ?>, EntityImpl> fetchEntityImplMap =
                new LinkedHashMap<>(
                        ReferenceEqualityComparator.getInstance(),
                        (ReferenceEqualityComparator<EntityImpl>)null);
        
        //Unit test need the order(QueryContextTest)
        XOrderedMap<PathId, PathNodeImpl> pathNodeImpls = 
                new LinkedHashMap<>();
                
        XOrderedMap<String, ParameterExpression<?>> namedParameters =
                new LinkedHashMap<>();
        
        List<ParameterExpression<?>> unnamedParameters =
                new ArrayList<>();
        
        int entityIdSequence;
        
        int parameterPositionSequence;
        
        @Override
        public void visitCriteriaQuery(XCriteriaQuery<?> query) {
            this.getEntities(query); //Give up return value, but create the objects.
            
            this.visitAbstractQuery(query);
            
            PathId.Allocator pathIdAllocator = QueryContext.this.pathIdAllocator;
            pathIdAllocator.push(PathId.Allocator.ORDER_LIST);
            try {
                for (Order order : query.getOrderList()) {
                    this.visit(order);
                }
            } finally {
                pathIdAllocator.pop();
            }
            
            this.afterVisitCommonAbstractCriteria(query);
        }

        @Override
        public void visitSubquery(XSubquery<?> query) {
            this.getEntities(query); //Give up return value, but create the objects.
            this.visitAbstractQuery(query);
        }

        @Override
        public void visitCriteriaUpdate(XCriteriaUpdate<?> update) {
            this.getEntities(update);
            
            PathId.Allocator pathIdAllocator = QueryContext.this.pathIdAllocator;
            
            pathIdAllocator.push(PathId.Allocator.ON_TREE);
            try {
                this.visitOnExpressions(update);
            } finally {
                pathIdAllocator.pop();
            }
            
            pathIdAllocator.push(PathId.Allocator.ASSIGNMENT_LIST);
            try {
                for (Assignment assignment : update.getAssignments()) {
                    this.visit(assignment);
                }
            } finally {
                pathIdAllocator.pop();
            }
            
            pathIdAllocator.push(PathId.Allocator.RESTRICTION);
            try {
                this.visit(update.getRestriction());
            } finally {
                pathIdAllocator.pop();
            }
            
            this.afterVisitCommonAbstractCriteria(update);
        }

        @Override
        public void visitCriteriaDelete(XCriteriaDelete<?> delete) {
            this.getEntities(delete);
            
            PathId.Allocator pathIdAllocator = QueryContext.this.pathIdAllocator;
            
            pathIdAllocator.push(PathId.Allocator.ON_TREE);
            try {
                this.visitOnExpressions(delete);
            } finally {
                pathIdAllocator.pop();
            }
            
            pathIdAllocator.push(PathId.Allocator.RESTRICTION);
            try {
                this.visit(delete.getRestriction());
            } finally {
                pathIdAllocator.pop();
            }
            
            this.afterVisitCommonAbstractCriteria(delete);
        }

        @Override
        public void visitPath(Path<?> path) {
            if (path instanceof MapKeyPath<?>) {
                this.visit(path.getParentPath());
            } else {
                PathId pathId = QueryContext.this.pathIdAllocator.allocate();
                if (pathId.getPath() != path) {
                    throw new AssertionError();
                }
                this.pathNodeImpls.put(pathId, null);
            }
        }
        
        @Override
        public void visitParameterExpression(ParameterExpression<?> parameterExpression) {
            if (parameterExpression.getName() == null) {
                ParameterExpressionImpl<?> parameterExpressionImpl = (ParameterExpressionImpl<?>)parameterExpression;
                if (!parameterExpressionImpl.setPosition(++this.parameterPositionSequence)) {
                    throw new IllegalProgramException(unnamedParameterCanNotBeUsedTwice());
                }
                this.unnamedParameters.add(parameterExpression);
            } else {
                this.namedParameters.put(parameterExpression.getName(), parameterExpression);
            }
        }
        
        @Override
        protected void visit(Object o) {
            if (o != null) {
                PathId.Allocator pathIdAllocator = QueryContext.this.pathIdAllocator;
                pathIdAllocator.push(o);
                try {
                    AbstractNode abstractNode = (AbstractNode)o;
                    abstractNode.freeze(QueryContext.this);
                    abstractNode.accept(this);
                } finally {
                    pathIdAllocator.pop();
                }
            }
        }

        private void visitAbstractQuery(XAbstractQuery<?> query) {
            PathId.Allocator pathIdAllocator = QueryContext.this.pathIdAllocator;
            
            pathIdAllocator.push(PathId.Allocator.SELECTION);
            try {
                this.visit(query.getSelection());
            } finally {
                pathIdAllocator.pop();
            }
            
            pathIdAllocator.push(PathId.Allocator.ON_TREE);
            try {
                this.visitOnExpressions(query);
            } finally {
                pathIdAllocator.pop();
            }
            
            pathIdAllocator.push(PathId.Allocator.RESTRICTION);
            try {
                this.visit(query.getRestriction());
            } finally {
                pathIdAllocator.pop();
            }
            
            pathIdAllocator.push(PathId.Allocator.GROUP_LIST);
            try {
                for (Expression<?> expression : query.getGroupList()) {
                    this.visit(expression);
                }
            } finally {
                pathIdAllocator.pop();
            }
            
            pathIdAllocator.push(PathId.Allocator.GROUP_RESTRICTION);
            try {
                this.visit(query.getGroupRestriction());
            } finally {
                pathIdAllocator.pop();
            }
        }
        
        private List<Entity> getEntities(XCommonAbstractCriteria commonAbstractCriteria) {
            List<Entity> entities = this.rootEntityMap.get(commonAbstractCriteria);
            if (entities == null) {
                if (commonAbstractCriteria instanceof XCriteriaUpdate<?>) {
                    XCriteriaUpdate<?> update = (XCriteriaUpdate<?>)commonAbstractCriteria;
                    XRoot<?> root = update.getRoot();
                    if (root == null) {
                        throw new IllegalStateException(noRoots(XCriteriaUpdate.class));    
                    }
                    entities = new ArrayList<>();
                    entities.add(EntityImpl.of(null, root, this.fromEntityImplMap));
                } else if (commonAbstractCriteria instanceof XCriteriaDelete<?>) {
                    XCriteriaDelete<?> delete = (XCriteriaDelete<?>)commonAbstractCriteria;
                    XRoot<?> root = delete.getRoot();
                    if (root == null) {
                        throw new IllegalStateException(noRoots(XCriteriaUpdate.class));    
                    }
                    entities = new ArrayList<>();
                    entities.add(EntityImpl.of(null, root, this.fromEntityImplMap));
                } else {
                    XAbstractQuery<?> query = (XAbstractQuery<?>)commonAbstractCriteria;
                    Collection<XRoot<?>> roots = query.getXRoots();
                    if (roots.isEmpty()) {
                        throw new IllegalStateException(noRoots(XAbstractQuery.class));
                    }
                    entities = new ArrayList<>(roots.size());
                    for (XRoot<?> root : roots) {
                        EntityImpl entityImpl = EntityImpl.of(null, root, this.fromEntityImplMap);
                        entities.add(entityImpl);
                    }
                }
                this.rootEntityMap.put(commonAbstractCriteria, entities);
            }
            return entities;
        }
        
        private void afterVisitCommonAbstractCriteria(XCommonAbstractCriteria commandAbstractCriteria) {
            for (Entry<PathId, PathNodeImpl> entry : this.pathNodeImpls.entrySet()) {
                entry.setValue(
                        PathNodeImpl.of(
                                QueryContext.this.isDbSchemaStrict(),
                                entry.getKey().getPath(), 
                                this.fromEntityImplMap,
                                entry.getKey().isDirectlyReferencedByTopmostSelection()
                        )
                );
            }
            EntityImpl.fetch(this.fromEntityImplMap, this.fetchEntityImplMap);
            for (Entry<XFrom<?, ?>, EntityImpl> entry : this.fromEntityImplMap.entrySet()) {
                if (entry.getKey() instanceof XRoot<?>) {
                    this.allocateEntityId(entry.getValue());
                }
            }
        }

        private void allocateEntityId(EntityImpl entityImpl) {
            if (entityImpl.isUsed()) {
                entityImpl.setIdIfNecessary(this.entityIdSequence++);
            }
            if (!entityImpl.getEntities().isEmpty()) {
                for (Entity childEntity : entityImpl.getEntities()) {
                    this.allocateEntityId((EntityImpl)childEntity);
                }
            }
        }
    }
    
    private class PostVisitorImpl extends AbstractVisitor {

        @Override
        protected void visit(Object o) {
            if (o != null) {
                AbstractNode abstractNode = (AbstractNode)o;
                abstractNode.unfreeze(QueryContext.this);
                abstractNode.accept(this);
            }
        }
        
    }
    
private static class EntityImpl implements Entity {
        
        private int id = -1;
        
        private ManagedType<?> managedType;
        
        private Attribute<?, ?> attribute;
        
        private JoinType joinType;
        
        private JoinMode joinMode;
        
        private boolean fetch;
        
        private boolean explicit;
        
        private boolean used;
        
        private EntityImpl parent;
        
        private List<EntityImpl> entities;
        
        private String sourceAlias;
        
        private Predicate on;
        
        //for explicit root & join
        private EntityImpl(
                EntityImpl parent,
                XFrom<?, ?> from) {
            this.parent = parent;
            this.explicit = true;
            this.sourceAlias = from.getAlias();
            if (from instanceof XRoot<?>) {
                if (parent != null) {
                    throw new AssertionError();
                }
                this.managedType = ((XRoot<?>)from).getModel();
                this.setUsed();
            } else {
                if (parent == null) {
                    throw new AssertionError();
                }
                XJoin<?, ?> join = (XJoin<?, ?>)from;
                Attribute<?, ?> attribute = join.getAttribute();
                if (attribute instanceof PluralAttribute<?, ?, ?>) {
                    this.managedType = (ManagedType<?>)((PluralAttribute<?, ?, ?>)attribute).getElementType();
                } else {
                    this.managedType = (ManagedType<?>)((SingularAttribute<?, ?>)attribute).getType();
                }
                this.attribute = attribute;
                this.joinType = join.getJoinType();
                this.joinMode = join.getJoinMode();
                this.on = join.getOn();
                if (this.joinMode.isRequired() || this.on != null) {
                    this.setUsed();//invoke set this used field of parent implicitly, this is very important
                }
            }
        }
        
        //Only for fetch & implicitJoin, not for explictJoin
        private EntityImpl(
                EntityImpl parentNode,
                Attribute<?, ?> attribute, 
                JoinType joinType) {
            this.parent = parentNode;
            if (attribute instanceof PluralAttribute<?, ?, ?>) {
                this.managedType = (ManagedType<?>)((PluralAttribute<?, ?, ?>)attribute).getElementType();
            } else {
                this.managedType = (ManagedType<?>)((SingularAttribute<?, ?>)attribute).getType();
            }
            this.attribute = attribute;
            this.joinType = joinType;
            this.joinMode = JoinMode.OPTIONALLY_MERGE_EXISTS;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        static EntityImpl of(
                EntityImpl parent, 
                XFrom<?, ?> from,
                Map<XFrom<?, ?>, EntityImpl> fromEntityImplMap) {
            EntityImpl entityImpl = null;
            if (parent != null) {
                XJoin<?, ?> join = (XJoin<?, ?>)from;
                if (!join.getJoinMode().isNew()) {
                    for (EntityImpl existingEntityImpl : parent.entities) {
                        if (existingEntityImpl.attribute == join.getAttribute() &&
                                !existingEntityImpl.joinMode.isNew()) {
                            if (existingEntityImpl.joinType != join.getJoinType()) {
                                existingEntityImpl.joinType = JoinType.INNER;
                            }
                            String sourceAlias = join.getAlias();
                            if (sourceAlias != null) {
                                if (existingEntityImpl.sourceAlias != null &&
                                        !existingEntityImpl.sourceAlias.equals(sourceAlias)) {
                                    throw new IllegalArgumentException(
                                            conflictEntityImplAliases(existingEntityImpl, existingEntityImpl.sourceAlias, sourceAlias)
                                    );
                                }
                                existingEntityImpl.sourceAlias = sourceAlias;
                            }
                            entityImpl = existingEntityImpl;
                            break;
                        }
                    }
                }
            }
            if (entityImpl == null) {
                entityImpl = new EntityImpl(parent, from);
                if (parent != null) {
                    parent.entities.add(entityImpl);
                }
            }
            fromEntityImplMap.put(from, entityImpl);
            
            XOrderedSet<XJoin<?, ?>> joins = (XOrderedSet)from.getXJoins();
            if (!from.getXJoins().isEmpty()) {
                List<EntityImpl> entities = entityImpl.entities;
                if (entities == null) {
                    entityImpl.entities = entities = new ArrayList<>();
                }
                for (XJoin<?, ?> join : joins) {
                    of(entityImpl, join, fromEntityImplMap);
                }
            }
            return entityImpl;
        }
        
        EntityImpl implicitlyJoin(Attribute<?, ?> attribute) {
            List<EntityImpl> entities = this.entities;
            if (entities != null) {
                for (EntityImpl existingEntityImpl : entities) {
                    if (existingEntityImpl.attribute == attribute) {
                        if (existingEntityImpl.joinType != joinType) {
                            existingEntityImpl.joinType = JoinType.INNER;
                        }
                        existingEntityImpl.setUsed();
                        return existingEntityImpl;
                    }
                }
            } else {
                this.entities = entities = new ArrayList<EntityImpl>();
            }
            EntityImpl entityImpl = new EntityImpl(this, attribute, JoinType.INNER);
            entities.add(entityImpl);
            return entityImpl;
        }
        
        static void fetch(
                Map<XFrom<?, ?>, EntityImpl> fromEntityImplMap,
                Map<XFetch<?, ?>, EntityImpl> fetchEntityImplMap) {   
            for (Entry<XFrom<?, ?>, EntityImpl> entry : fromEntityImplMap.entrySet()) {
                fetch(
                        entry.getValue(),
                        getEntityMapKey(entry.getKey()), 
                        fetchEntityImplMap);
            }
        }
        
        private static void fetch(
                EntityImpl parentEntityImpl,
                XFetchParent<?, ?> fetchParent, 
                Map<XFetch<?, ?>, EntityImpl> fetchEntityImplMap) {
            List<EntityImpl> entities = parentEntityImpl.entities;
            for (XFetch<?, ?> fetch : fetchParent.getXFetches()) {
                EntityImpl childEntityImpl = null;
                if (entities != null) {
                    for (EntityImpl existingEntityImpl : entities) {
                        if (existingEntityImpl.attribute == fetch.getAttribute() &&
                                !existingEntityImpl.joinMode.isNew()) {
                            if (fetch.getCollectionFetchType() == CollectionFetchType.PARTIAL ||
                                    existingEntityImpl.attribute instanceof SingularAttribute<?, ?> ||
                                    existingEntityImpl.fetch) {
                                if (existingEntityImpl.joinType != fetch.getJoinType()) {
                                    existingEntityImpl.joinType = JoinType.INNER;
                                }
                                childEntityImpl = existingEntityImpl;
                                break;
                            }
                        }
                    }
                }
                if (childEntityImpl == null) {
                    if (entities == null) {
                        parentEntityImpl.entities = entities = new ArrayList<>();
                    }
                    childEntityImpl = new EntityImpl(parentEntityImpl, fetch.getAttribute(), fetch.getJoinType());
                    entities.add(childEntityImpl);
                }
                childEntityImpl.setFetch(); //Very important to setFetch(include setUsed implicitly)
                fetchEntityImplMap.put(fetch, childEntityImpl);
                fetch(childEntityImpl, (XFetch<?, ?>)fetch, fetchEntityImplMap);
            }
        }
        
        void setUsed() {
            for (EntityImpl entityImpl = this; entityImpl != null; entityImpl = entityImpl.parent) {
                entityImpl.used = true;
            }
        }
        
        void setFetch() {
            this.fetch = true;
            this.setUsed();
            for (EntityImpl entityImpl = this; entityImpl != null; entityImpl = entityImpl.parent) {
                entityImpl.explicit = true;
            }
        }
        
        void setIdIfNecessary(int id) {
            this.id = id;
        }
        
        @Override
        public String getRenderAlias() {
            //id must be validated whenever, if sourceAlias is not null or is is null
            int id = this.id;
            if (id == -1) {
                throw new IllegalStateException(entityIdHasNotBeenAllocated(Entity.class));
            }
            String sourceAlias = this.sourceAlias;
            if (this.joinMode == null || !this.joinMode.isNew()) {
                if (sourceAlias == null) {
                    return "babyfish_shared_alias_" + id;
                }
                return sourceAlias;
            }
            if (sourceAlias == null) {
                return Constants.NOT_SHARED_JOIN_ALIAS_PREFIX + id;
            }
            return Constants.NOT_SHARED_JOIN_ALIAS_PREFIX + sourceAlias;
        }

        @Override
        public ManagedType<?> getManagedType() {
            return this.managedType;
        }

        @Override
        public Attribute<?, ?> getAttribute() {
            return this.attribute;
        }

        @Override
        public JoinType getJoinType() {
            return this.joinType;
        }
        
        @Override
        public JoinMode getJoinMode() {
            return this.joinMode;
        }

        @Override
        public boolean isFetch() {
            return this.fetch;
        }

        @Override
        public boolean isExplicit() {
            return this.explicit;
        }

        @Override
        public boolean isUsed() {
            return this.used;
        }
        
        @Override
        public Predicate getOn() {
            return this.on;
        }
        
        @Override
        public Entity getParent() {
            return this.parent;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public List<Entity> getEntities() {
            List<EntityImpl> entities = this.entities;
            if (entities == null) {
                return MACollections.emptyList();
            }
            return (List)MACollections.unmodifiable(entities);
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            this.toString(0, builder);
            return builder.toString();
        }
        
        private void toString(int tab, StringBuilder output) {
            for (int i = tab - 1; i >= 0; i--) {
                output.append('\t');
            }
            if (this.parent == null) {
                output.append(this.managedType.getJavaType().getName());
            } else {
                output
                .append(this.joinType.name().toLowerCase())
                .append(" join ");
                if (this.fetch) {
                    output.append("fetch ");
                }
                output
                .append(this.attribute.getName())
                .append("(joinMode = ")
                .append(this.joinMode.name().toLowerCase())
                .append(", used = ")
                .append(this.used)
                .append(");");
            }
            List<EntityImpl> entities = this.entities;
            if (entities == null) {
                output.append("\r\n");
            } else {
                output.append(" {\r\n");
                for (EntityImpl entity : entities) {
                    entity.toString(tab + 1, output);
                }
                for (int i = tab - 1; i >= 0; i--) {
                    output.append('\t');
                }
                output.append("}\r\n");
            }
        }
    }
    
    private static class PathNodeImpl implements PathNode {
        
        Class<?> treateAsType;
        
        PathNodeImpl parentNode;
        
        Attribute<?, ?> attribute;
    
        EntityImpl entityImpl; //for "join" only
        
        static PathNodeImpl of(
                boolean dbSchemaStrict,
                Path<?> path, 
                Map<XFrom<?, ?>, EntityImpl> fromEntityImplMap,
                boolean directlyReferencedByTopmostSelection) {
            return ofImpl(dbSchemaStrict, true, path, fromEntityImplMap, directlyReferencedByTopmostSelection);
        }
        
        private static PathNodeImpl ofImpl(
                boolean dbSchemaStrict,
                boolean leaf, 
                Path<?> path, 
                Map<XFrom<?, ?>, EntityImpl> fromEntityImplMap,
                boolean directlyReferencedByTopmostSelection) {
            Arguments.mustNotBeInstanceOfValue("path", path, MapKeyPath.class);
            if (path == null) {
                return null;
            }
            if (path instanceof From<?, ?>) {
                if (leaf && path instanceof Join<?, ?>) {
                    Join<?, ?> join = (Join<?, ?>)path;
                    // If the leaf path is reference left join and it is NOT referenced by topmost selection
                    // Change 
                    //      employee.join(Employee_.department, JoinType.LEFT)
                    // to
                    //      employee.get(Employee_.department);
                    if (!directlyReferencedByTopmostSelection && isLeftOrNonNullReferenceJoin(join, dbSchemaStrict)) {
                        XFrom<?, ?> parentFrom = (XFrom<?, ?>)join.getParentPath();
                        EntityImpl parentEntity = fromEntityImplMap.get(getEntityMapKey(parentFrom));
                        return 
                                new PathNodeImpl(
                                        treatedAsType(path),
                                        new PathNodeImpl(
                                                treatedAsType(parentFrom), 
                                                ofImpl(
                                                        dbSchemaStrict,
                                                        false, 
                                                        parentFrom, 
                                                        fromEntityImplMap, 
                                                        directlyReferencedByTopmostSelection
                                                ),
                                                parentEntity
                                        ),
                                        join.getAttribute()
                                );
                    }
                }
                EntityImpl entityImpl = fromEntityImplMap.get(getEntityMapKey((XFrom<?, ?>)path));
                return new PathNodeImpl(
                        treatedAsType(path), 
                        ofImpl(
                                dbSchemaStrict,
                                false, 
                                path.getParentPath(), 
                                fromEntityImplMap, 
                                directlyReferencedByTopmostSelection),
                        entityImpl);
            }
            
            if (path.getModel() instanceof SingularAttribute<?, ?>) {
                SingularAttribute<?, ?> attribute = (SingularAttribute<?, ?>)path.getModel();
                if (attribute.isId()) {
                    if (path.getParentPath() instanceof Join<?, ?>) {
                        /*
                         * When "Employee_.department" can not be null
                         *     Change
                         *         "employee.join(Emloyee_.department, JoinType.<<ANY>>).get(Department_.id)"
                         *     to
                         *         "employee.get(Employee_.department).get(Department_.id)"
                         * Otherwise
                         *     Change
                         *         "employee.join(Emloyee_.department, JoinType.LEFT).get(Department_.id)"
                         *     to
                         *         "employee.get(Employee_.department).get(Department_.id)"
                         * 
                         * because:
                         *    (1) Employee_.department is a singular attribute
                         *  (2) The join type is JoinType.LEFT; or the attribute is NOT optional
                         *  (3) The Department_.id is an Id attribute.
                         */
                        Join<?, ?> join = (Join<?, ?>)path.getParentPath();
                        if (isLeftOrNonNullReferenceJoin(join, dbSchemaStrict)) {
                            From<?, ?> from = (From<?, ?>)join.getParentPath();
                            return 
                                    new PathNodeImpl(
                                            treatedAsType(path),
                                            new PathNodeImpl(
                                                    treatedAsType(join), 
                                                    ofImpl(
                                                            dbSchemaStrict,
                                                            false, 
                                                            from, 
                                                            fromEntityImplMap, 
                                                            directlyReferencedByTopmostSelection
                                                    ),
                                                    join.getAttribute()
                                            ),
                                            attribute
                                    );
                        }
                    } else if (path.getParentPath() instanceof SingularAttributePath<?>) {
                        /*
                         * Keep
                         *      "employee.join(Emloyee_.department, JoinType.LEFT).get(Department_.id)"
                         * because:
                         *    If don't do this, the next recursion will change the get to join
                         */
                        SingularAttributePath<?> parentPath = (SingularAttributePath<?>)path.getParentPath();
                        return 
                                new PathNodeImpl(
                                        treatedAsType(path),
                                        new PathNodeImpl(
                                                treatedAsType(parentPath), 
                                                ofImpl(
                                                        dbSchemaStrict,
                                                        false, 
                                                        parentPath.getParentPath(), 
                                                        fromEntityImplMap, 
                                                        directlyReferencedByTopmostSelection
                                                ),
                                                parentPath.getAttribute()),
                                        attribute);
                    }
                }
            }
            
            Attribute<?, ?> attribute = (Attribute<?, ?>)path.getModel();
            if (attribute != null) {
                if (!leaf || directlyReferencedByTopmostSelection) {
                    /*
                     * In normal mode
                     * Change 
                     *      employee.get(Employee_.department).get(Department_.name)
                     * to
                     *      employee.join(Employee_.department, JoinType.INNER).get(Department_.name)
                     * 
                     * In selection projection of topmost query
                     * Change
                     *      employee.get(Employee_.department).get(Department_.company)
                     * to
                     *      employee.join(Employee_.department, JoinType.INNER).join(Department_.company, JoinType.INNER)
                     */
                    PersistentAttributeType pat = attribute.getPersistentAttributeType();
                    if (pat == PersistentAttributeType.ONE_TO_ONE ||
                            pat == PersistentAttributeType.MANY_TO_ONE) {
                        PathNodeImpl parentNode = ofImpl(
                                dbSchemaStrict,
                                false, 
                                path.getParentPath(), 
                                fromEntityImplMap, 
                                directlyReferencedByTopmostSelection);
                        EntityImpl entityImpl = parentNode.entityImpl.implicitlyJoin(attribute);
                        return new PathNodeImpl(treatedAsType(path), parentNode, entityImpl);
                    }
                }
            } else {
                //PluralAttribute.getModel() returns null, so use this statement
                attribute = ((PluralAttributePath<?>)path).getAttribute();
            }
            return new PathNodeImpl(
                    treatedAsType(path), 
                    ofImpl(
                            dbSchemaStrict,
                            false, 
                            path.getParentPath(), 
                            fromEntityImplMap, 
                            directlyReferencedByTopmostSelection
                    ),
                    attribute);
        }
        
        private PathNodeImpl(Class<?> treatedAsType, PathNodeImpl parentNode, EntityImpl entityImpl) {
            this.treateAsType = treatedAsType;
            this.parentNode = parentNode;
            this.entityImpl = entityImpl;
            this.attribute = entityImpl.getAttribute();
            entityImpl.setUsed();
        }
        
        private PathNodeImpl(Class<?> treatedAsType, PathNodeImpl parentNode, Attribute<?, ?> attribute) {
            this.treateAsType = treatedAsType;
            this.parentNode = parentNode;
            this.attribute = attribute;
        }
        
        private static Class<?> treatedAsType(Path<?> path) {
            Path<?> treatedParent = ((AbstractPath<?>)path).getTreatedParent();
            if (treatedParent != null) {
                return path.getJavaType();
            }
            return null;
        }

        @Override
        public PathNode getParent() {
            return this.parentNode;
        }

        @Override
        public Entity getEntity() {
            return this.entityImpl;
        }
        
        @Override
        public Attribute<?, ?> getAttribute() {
            return this.attribute;
        }

        @Override
        public Class<?> getTreatAsType() {
            return this.treateAsType;
        }
        
        private static boolean isLeftOrNonNullReferenceJoin(Join<?, ?> join, boolean dbSchemaStrict) {
            if (join.getAttribute().isCollection()) {
                return false;
            }
            if (join.getJoinType() == JoinType.LEFT) {
                return true;
            }
            if (!dbSchemaStrict) {
                return false;
            }
            SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>)join.getAttribute();
            return
                    !singularAttribute.isOptional() && 
                    singularAttribute.getPersistentAttributeType() == PersistentAttributeType.MANY_TO_ONE;
        }
    }
    
    @I18N
    private static native String queryContextIsClosed(Class<QueryContext> queryContextType);
        
    @I18N
    private static native String unnamedParameterCanNotBeUsedTwice();
        
    @I18N
    private static native String noRoots(Class<? extends XCommonAbstractCriteria> xAbstractQueryType);
        
    @I18N
    private static native String entityIdHasNotBeenAllocated(Class<Entity> entityType);
    
    @I18N
    private static native String conflictEntityImplAliases(EntityImpl entityImpl, String alias1, String alias2);
}
