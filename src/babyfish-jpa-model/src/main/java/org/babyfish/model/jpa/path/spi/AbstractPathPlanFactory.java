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
package org.babyfish.model.jpa.path.spi;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.regex.Pattern;

import javax.persistence.criteria.JoinType;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.lang.I18N;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.Strings;
import org.babyfish.model.jpa.path.CollectionFetchType;
import org.babyfish.model.jpa.path.FetchPath;
import org.babyfish.model.jpa.path.GetterType;
import org.babyfish.model.jpa.path.QueryPath;
import org.babyfish.model.jpa.path.SimpleOrderPath;
import org.babyfish.model.jpa.path.spi.PathPlanKey.SubKey;

/**
 * @author Tao Chen
 */
public abstract class AbstractPathPlanFactory {
    
    public PathPlan create(PathPlanKey key) {
        return new PathPlanImpl(key);
    }
    
    protected abstract EntityDelegate getEntityDelegate(String alias);
    
    protected interface EntityDelegate {
        
        String getIdPropertyName();
        
        PropertyDelegate getNonIdProperty(String nonIdPropertyName);

        boolean containsImplicitCollectionJoins();
    }
    
    protected interface PropertyDelegate {
        
        boolean isAssociation();
        
        boolean isCollection();
        
        EntityDelegate getAssociatedEntityDelegate();
    }
    
    private class PathPlanImpl implements PathPlan {
        
        private Map<String, SubPlanImpl> subPlans;
        
        private boolean containsScalarEagerness;
        
        private boolean containsInnerJoins;
        
        private boolean containsCollectionJoins;
        
        private boolean containsCollectionInnerJoins;
        
        private boolean containsNoFetchJoins;
        
        public PathPlanImpl(PathPlanKey key) {
            key = PathPlanKey.nullToNil(key);
            Map<String, SubPlanImpl> map = new LinkedHashMap<>(key.getSubKeys().length);
            for (SubKey subKey : key.getSubKeys()) {
                SubPlanImpl subPlan = AbstractPathPlanFactory.this.new SubPlanImpl(subKey);
                map.put(subKey.getAlias(), subPlan);
                this.containsScalarEagerness |= subPlan.getJoinNode().containsScalarEagerness();
                this.containsInnerJoins |= subPlan.getJoinNode().containsInnerJoins();
                this.containsCollectionJoins |= subPlan.getJoinNode().containsCollectionJoins();
                this.containsCollectionInnerJoins |= subPlan.getJoinNode().containsCollectionInnerJoins();
                this.containsNoFetchJoins |= subPlan.getJoinNode().containsNoFetchJoins();
            }
            this.subPlans = MACollections.unmodifiable(map);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Map<String, SubPlan> getSubPlans() {
            return MACollections.unmodifiable((Map)this.subPlans);
        }

        @Override
        public boolean containsScalarEagerness() {
            return this.containsScalarEagerness;
        }

        @Override
        public boolean containsInnerJoins() {
            return this.containsInnerJoins;
        }

        @Override
        public boolean containsCollectionJoins() {
            return this.containsCollectionJoins;
        }
        
        @Override
        public boolean containsCollectionInnerJoins() {
            return this.containsCollectionInnerJoins;
        }

        @Override
        public boolean containsNoFetchJoins() {
            return this.containsNoFetchJoins;
        }
    }
    
    private class SubPlanImpl implements SubPlan {
        
        private String alias;
        
        private JoinNodeImpl joinNodeImpl;
        
        private List<OrderNode> preOrderNodes;
        
        private List<OrderNode> postOrderNodes;
        
        SubPlanImpl(SubKey subKey) {
            EntityDelegate entityDelegate = AbstractPathPlanFactory.this.getEntityDelegate(alias);
            if (entityDelegate == null) {
                throw new IllegalProgramException(
                        methodCanNotReturnNull(
                                AbstractPathPlanFactory.this.getClass(),
                                "getEntityDelegate(String)")
                );
            }
            JoinNodeImpl joinNodeImpl = new JoinNodeImpl(entityDelegate);
            int orderSequence = 0;
            if (subKey.getQueryPaths() != null) {
                for (QueryPath queryPath : subKey.getQueryPaths()) {
                    if (queryPath instanceof FetchPath) {
                        joinNodeImpl.addFetchPath((FetchPath)queryPath);
                    } else {
                        joinNodeImpl.addSimpleOrderPath(orderSequence++, (SimpleOrderPath)queryPath);
                    }
                }
            }
            joinNodeImpl.finalInitialize(); //very important!!!
            this.alias = subKey.getAlias();
            this.joinNodeImpl = joinNodeImpl;
            NavigableMap<Integer, OrderNode> preOrderNodeMap = new TreeMap<>(), postOrderNodeMap = new TreeMap<>();
            joinNodeImpl.collectOrderNodes(preOrderNodeMap, postOrderNodeMap);
            this.preOrderNodes = MACollections.wrap(preOrderNodeMap.values().toArray(new OrderNode[preOrderNodeMap.size()]));
            this.postOrderNodes = MACollections.wrap(postOrderNodeMap.values().toArray(new OrderNode[postOrderNodeMap.size()]));
        }

        @Override
        public String getAlias() {
            return this.alias;
        }

        @Override
        public JoinNode getJoinNode() {
            return this.joinNodeImpl;
        }

        @Override
        public List<OrderNode> getPreOrderNodes() {
            return this.preOrderNodes;
        }

        @Override
        public List<OrderNode> getPostOrderNodes() {
            return this.postOrderNodes;
        }
    }
    
    private static class JoinNodeImpl implements JoinNode {
        
        private String name;
        
        private JoinType joinType;
        
        private CollectionFetchType collectionFetchType;
        
        private JoinNodeImpl parentNode;
        
        private boolean fetch;
        
        private XOrderedSet<String> loadedScalarNames;
            
        private Map<String, JoinNodeImpl> childNodes;
        
        private Map<String, OrderNodeImpl> orderNodes;
        
        private boolean collection;
        
        private boolean containsScalarEagerness;
        
        private boolean containsInnerJoins;
        
        private boolean containsCollectionJoins;
        
        private boolean containsCollectionInnerJoins;
        
        private boolean containsNoFetchJoins;
        
        private transient String path;
        
        private transient String toString;
        
        //temporary data, should be clean at last
        private transient JoinNodeTempCreatingData creatingData = new JoinNodeTempCreatingData();
        
        JoinNodeImpl(EntityDelegate entityDelegate) {
            this.name = "this";
            this.joinType = JoinType.LEFT;
            this.collectionFetchType = CollectionFetchType.ALL;
            this.creatingData.entityDelegate = entityDelegate;
        }
        
        private JoinNodeImpl(
                JoinNodeImpl parentNode, 
                String name, 
                JoinType joinType, 
                boolean fetch, 
                CollectionFetchType collectionFetchType) {
            EntityDelegate parentEntityDelegate = parentNode.creatingData.entityDelegate;
            if (name.equals(parentEntityDelegate.getIdPropertyName())) {
                throw new IllegalArgumentException(
                        joinPropertyMustBeAssociationProperty(parentNode.getPath(), name)
                );
            }
            PropertyDelegate propertyDelegate = parentNode.getPropertyDelegate(name);
            if (!propertyDelegate.isAssociation()) {
                throw new IllegalArgumentException(
                        joinPropertyMustBeAssociationProperty(parentNode.getPath(), name)
                );
            }
            this.parentNode = parentNode;
            this.name = name;
            this.joinType = joinType;
            this.collectionFetchType = collectionFetchType;
            if (fetch) {
                this.setFetch();
            }
            this.collection = propertyDelegate.isCollection();
            this.creatingData.entityDelegate = propertyDelegate.getAssociatedEntityDelegate();
        }
        
        void setFetch() {
            for (JoinNodeImpl joinNodeImpl = this; joinNodeImpl != null; joinNodeImpl = joinNodeImpl.parentNode) {
                if (joinNodeImpl.fetch) {
                    break;
                }
                joinNodeImpl.fetch = true;
            }
            this.setUsed();
        }
        
        void setUsed() {
            for (JoinNodeImpl joinNodeImpl = this; joinNodeImpl != null; joinNodeImpl = joinNodeImpl.parentNode) {
                if (joinNodeImpl.creatingData.used) {
                    break;
                }
                joinNodeImpl.creatingData.used = true;
            }
        }
        
        void addFetchPath(FetchPath fetchPath) {
            JoinNodeImpl joinNodeImpl = this;
            FetchPath.Node node = fetchPath.getFirstNode();
            while (node != null) {
                FetchPath.Node nextNode = node.getNextNode();
                if (node.getName().equals(this.getIdPropertyName())) {
                    throw new IllegalArgumentException(
                            fetchPathCanNotContainId(fetchPath, node.getName())
                    );
                }
                PropertyDelegate propertyDelegate = joinNodeImpl.getPropertyDelegate(node.getName());
                if (propertyDelegate.isAssociation()) {
                    joinNodeImpl = joinNodeImpl.addJoinNode(
                            node.getName(),
                            node.getGetterType(), 
                            true, 
                            node.getCollectionFetchType());
                } else {
                    if (nextNode != null) {
                        throw new IllegalArgumentException(
                                fetchPathConOnlyContainOneScalar(fetchPath, node.getName())
                        );
                    }
                    if (node.getGetterType() == GetterType.REQUIRED) {
                        throw new IllegalArgumentException(
                                scalarPropertyCanNotBeRequired(fetchPath, node.getName())
                        );
                    }
                    if (node.getCollectionFetchType() == CollectionFetchType.PARTIAL) {
                        throw new IllegalArgumentException(
                                scalarPropertyCanNotBePartial(fetchPath, node.getName())
                        );
                    }
                    joinNodeImpl.addLoadedScalarName(node.getName());
                }
                node = nextNode;
            }
        }
        
        void addSimpleOrderPath(int orderSequence, SimpleOrderPath simpleOrderPath) {
            JoinNodeImpl joinNodeImpl = this;
            OrderNodeImpl orderNodeImpl;
            SimpleOrderPath.Node node = simpleOrderPath.getFirstNode();
            
            if (node == null) {
                //"pre order by this asc" -> "pre order by this.id asc"
                orderNodeImpl = new OrderNodeImpl(
                        orderSequence,
                        joinNodeImpl,
                        joinNodeImpl.getIdPropertyName(), 
                        simpleOrderPath.isPost(), 
                        simpleOrderPath.isDesc());
            } else {
                while (true) {
                    SimpleOrderPath.Node nextNode = node.getNextNode();
                    boolean isId = node.getName().equals(this.getIdPropertyName());
                    PropertyDelegate propertyDelegate = null;
                    if (!isId) {
                        propertyDelegate = joinNodeImpl.getPropertyDelegate(node.getName());
                    }
                    if (propertyDelegate != null && propertyDelegate.isAssociation()) {
                        // "pre order by this.department" need not join
                        if (nextNode == null) {
                            String orderName = node.getName();
                            if (node.getGetterType() == GetterType.REQUIRED || propertyDelegate.isCollection()) {
                                //adjust "pre order by this..department asc" to "pre order by this..department.id asc"(inner join can not be used by order by statement)
                                //adjust "pre order by this.annualLeves asc" to "pre order by this.annualLeaves.id asc"(collection property can not be used by order by statement)
                                joinNodeImpl = joinNodeImpl.addJoinNode(
                                        node.getName(), 
                                        node.getGetterType(), 
                                        false, 
                                        CollectionFetchType.ALL);
                                orderName = joinNodeImpl.getIdPropertyName();
                            }
                            orderNodeImpl = new OrderNodeImpl(
                                    orderSequence,
                                    joinNodeImpl,
                                    orderName, 
                                    simpleOrderPath.isPost(), 
                                    simpleOrderPath.isDesc());
                            break;
                        }
                        joinNodeImpl = joinNodeImpl.addJoinNode(
                                node.getName(), 
                                node.getGetterType(), 
                                false, 
                                CollectionFetchType.ALL);
                    } else if (nextNode != null) { //Multiple scalar properties chain
                        StringBuilder quanifiedNameBuilder = new StringBuilder();
                        for (; node != null; node = node.getNextNode()) {
                            
                            if (node.getGetterType() == GetterType.REQUIRED) {
                                //Scalar property can be be applied inner join
                                throw new IllegalArgumentException(
                                        requiredOrderPropertyMustBeAssociationProperty(
                                                joinNodeImpl.getPath() + "." + quanifiedNameBuilder.toString(),
                                                GetterType.REQUIRED,
                                                node.getName()
                                        )
                                );
                            }
                            if (quanifiedNameBuilder.length() != 0) {
                                quanifiedNameBuilder.append('.');
                            }
                            quanifiedNameBuilder.append(node.getName());
                        }
                        orderNodeImpl = new OrderNodeImpl(
                                orderSequence,
                                joinNodeImpl,
                                quanifiedNameBuilder.toString(), 
                                simpleOrderPath.isPost(), 
                                simpleOrderPath.isDesc());
                        break;
                    } else { //Single scalar property
                        if (node.getGetterType() == GetterType.REQUIRED) {
                            //Scalar property can be be applied inner join
                            throw new IllegalArgumentException(
                                    requiredOrderPropertyMustBeAssociationProperty(
                                            joinNodeImpl.getPath(),
                                            GetterType.REQUIRED,
                                            node.getName()
                                    )
                            );
                        }
                        if (joinNodeImpl.parentNode != null &&
                                !joinNodeImpl.collection &&
                                node.getGetterType() == GetterType.OPTIONAL && 
                                node.getName().equals(joinNodeImpl.getIdPropertyName())) {
                            //optimize "pre order by this.department.id asc" -> "pre order by this.department asc"
                            orderNodeImpl = new OrderNodeImpl(
                                    orderSequence,
                                    joinNodeImpl.parentNode,
                                    joinNodeImpl.name, 
                                    simpleOrderPath.isPost(), 
                                    simpleOrderPath.isDesc());
                            joinNodeImpl = joinNodeImpl.parentNode;
                            // After this statement, though that JoinNodeImpl looks like that is not deleted,
                            // but don't worry because it may be marked as "unused".
                        } else {
                            orderNodeImpl = new OrderNodeImpl(
                                    orderSequence,
                                    joinNodeImpl,
                                    node.getName(), 
                                    simpleOrderPath.isPost(), 
                                    simpleOrderPath.isDesc());
                        }
                        break;
                    }
                    node = nextNode;                    
                }
            }
            
            Map<String, OrderNodeImpl> orderNodes = joinNodeImpl.orderNodes;
            if (orderNodes == null) {
                joinNodeImpl.orderNodes = orderNodes = new LinkedHashMap<>();
            } else {
                OrderNodeImpl existingOrderNode = orderNodes.get(orderNodeImpl.getQuanifiedName());
                if (existingOrderNode != null) {
                    if (existingOrderNode.post != orderNodeImpl.post) {
                        throw new IllegalArgumentException(
                                simpleOrderPathStateAreConflict(
                                        existingOrderNode.getParentNode().getPath() + '.' + existingOrderNode.getQuanifiedName(),
                                        existingOrderNode.isPost() ? "post" : "pre",
                                        orderNodeImpl.getParentNode().getPath() + '.' + orderNodeImpl.getQuanifiedName(),
                                        orderNodeImpl.isPost() ? "post" : "pre"
                                )
                        );
                    }
                    if (existingOrderNode.desc != orderNodeImpl.desc) {
                        throw new IllegalArgumentException(
                                simpleOrderPathStateAreConflict(
                                        existingOrderNode.getParentNode().getPath() + '.' + existingOrderNode.getQuanifiedName(),
                                        existingOrderNode.isPost() ? "desc" : "asc",
                                        orderNodeImpl.getParentNode().getPath() + '.' + orderNodeImpl.getQuanifiedName(),
                                        orderNodeImpl.isPost() ? "desc" : "asc"
                                )
                        );
                    }
                    orderNodeImpl = null; //need not to be added
                }
            }
            if (orderNodeImpl != null) {
                orderNodes.put(orderNodeImpl.getQuanifiedName(), orderNodeImpl);
            }
        }
        
        private JoinNodeImpl addJoinNode(
                String name, 
                GetterType getterType, 
                boolean fetch, 
                CollectionFetchType collectionFetchType) {
            JoinNodeImpl childJoinNodeImpl = null;
            Map<String, JoinNodeImpl> childNodes = this.childNodes;
            if (childNodes != null) {
                childJoinNodeImpl = childNodes.get(name);
                if (childJoinNodeImpl != null) {
                    childJoinNodeImpl.setFetch();
                    if (getterType == GetterType.REQUIRED) {
                        childJoinNodeImpl.joinType = JoinType.INNER;
                    }
                    if (collectionFetchType == CollectionFetchType.PARTIAL) {
                        childJoinNodeImpl.collectionFetchType = CollectionFetchType.PARTIAL;
                    }
                }
            }
            if (childJoinNodeImpl == null) {
                childJoinNodeImpl = new JoinNodeImpl(
                        this,
                        name,
                        getterType == GetterType.REQUIRED ? JoinType.INNER : JoinType.LEFT,
                        fetch,
                        collectionFetchType);
                if (childNodes == null) {
                    this.childNodes = childNodes = new LinkedHashMap<>();
                }
                childNodes.put(name, childJoinNodeImpl);
            }
            return childJoinNodeImpl;
        }
        
        private String getIdPropertyName() {
            return this.creatingData.entityDelegate.getIdPropertyName();
        }
        
        private boolean containsImplicitCollectionJoins() {
            return this.creatingData.entityDelegate.containsImplicitCollectionJoins();
        }

        //In this class, never call entityDelegate.getNonIdProperty() except this method, just use this method.
        private PropertyDelegate getPropertyDelegate(String name) {
            PropertyDelegate propertyDelegate = null;
            Map<String, PropertyDelegate> propertyDelegates = this.creatingData.propertyDelegates;
            if (propertyDelegates == null) {
                this.creatingData.propertyDelegates = propertyDelegates = new HashMap<>();
            } else {
                propertyDelegate = propertyDelegates.get(name);
            }
            if (propertyDelegate == null) {
                try {
                    propertyDelegate = this.creatingData.entityDelegate.getNonIdProperty(name);
                } catch (RuntimeException | Error ex) {
                    throw new IllegalArgumentException(ex);
                }
                if (propertyDelegate == null) {
                    throw new IllegalArgumentException(
                            methodCanNotReturnNull(
                                    this.creatingData.entityDelegate.getClass(), 
                                    "getNonIdProperty(String)"
                            )
                    );
                }
            }
            propertyDelegates.put(name, propertyDelegate);
            return propertyDelegate;
        }
        
        void finalInitialize() {
            this.markJoinNodeAsUsedByOrderRequest();
            this.removeUnusedJoinNode();
            this.initializeContainsScalarEagerness();
            this.initializeContainsInnerJoins();
            this.initializeContainsCollectionJoins();
            this.initializeContainsCollectionInnerJoins();
            this.initializeContainsNoFetchJoins();
            this.finalInitializePrivateData();
        }

        void collectOrderNodes(
                NavigableMap<Integer, OrderNode> preOrderNodeMap,
                NavigableMap<Integer, OrderNode> postOrderNodeMap) {
            if (this.orderNodes != null) {
                for (OrderNodeImpl orderNodeImpl : this.orderNodes.values()) {
                    if (orderNodeImpl.post) {
                        postOrderNodeMap.put(orderNodeImpl.sequence, orderNodeImpl);
                    } else {
                        preOrderNodeMap.put(orderNodeImpl.sequence, orderNodeImpl);
                    }
                }
            }
            if (this.childNodes != null) {
                for (JoinNodeImpl childJoinNodeImpl : this.childNodes.values()) {
                    childJoinNodeImpl.collectOrderNodes(preOrderNodeMap, postOrderNodeMap);
                }
            }
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public JoinType getJoinType() {
            return this.joinType;
        }

        @Override
        public boolean isFetch() {
            return this.fetch;
        }

        @Override
        public CollectionFetchType getCollectionFetchType() {
            return this.collectionFetchType;
        }

        @Override
        public XOrderedSet<String> getLoadedScalarNames() {
            return this.loadedScalarNames;
        }

        @Override
        public JoinNode getParentNode() {
            return this.parentNode;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Map<String, JoinNode> getChildNodes() {
            //Don't worry, after finalInitializePrivateData(), it is read-only collection
            return (Map)this.childNodes;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Map<String, OrderNode> getOrderNodes() {
            //Don't worry, after finalInitializePrivateData(), it is read-only collection
            return (Map)this.orderNodes;
        }

        @Override
        public boolean isCollection() {
            return this.collection;
        }
        
        @Override
        public boolean containsScalarEagerness() {
            return this.containsScalarEagerness;
        }

        @Override
        public boolean containsInnerJoins() {
            return this.containsInnerJoins;
        }

        @Override
        public boolean containsCollectionJoins() {
            return this.containsCollectionJoins;
        }
        
        @Override
        public boolean containsCollectionInnerJoins() {
            return this.containsCollectionInnerJoins;
        }
        
        @Override
        public boolean containsNoFetchJoins() {
            return this.containsNoFetchJoins;
        }

        @Override
        public String getPath() {
            String path = this.path;
            if (path == null) {
                StringBuilder builder = new StringBuilder();
                if (this.parentNode == null) {
                    builder.append(this.name);
                } else {
                    builder.append(this.parentNode.getPath());
                    if (this.joinType == JoinType.INNER) {
                        builder.append("/inner join ");
                    } else {
                        builder.append("/left join ");
                    }
                    if (this.fetch) {
                        builder.append("fetch ");
                    }
                    if (this.collectionFetchType == CollectionFetchType.PARTIAL) {
                        builder.append("partial(").append(this.name).append(')');
                    } else {
                        builder.append(this.name);
                    }
                }
                if (!Nulls.isNullOrEmpty(this.loadedScalarNames)) {
                    builder.append('[');
                    Strings.join(this.loadedScalarNames, builder);
                    builder.append(']');
                }
                this.path = path = builder.toString();
            }
            return path;
        }
        
        @Override
        public String toString() {
            String toString = this.toString;
            if (toString == null) {
                StringBuilder builder = new StringBuilder();
                this.toString(builder, 0);
                this.toString = toString = builder.toString();
            }
            return toString;
        }
        
        private void addLoadedScalarName(String loadedScalarName) {
            if (!Nulls.isNullOrEmpty(loadedScalarName)) {
                if (this.loadedScalarNames == null) {
                    this.loadedScalarNames = new LinkedHashSet<>();
                }
                this.loadedScalarNames.add(loadedScalarName);
            }
        }
        
        private void toString(StringBuilder builder, int tabCount) {
            for (int i = tabCount - 1; i >= 0; i--) {
                builder.append('\t');
            }
            if (tabCount == 0) {
                builder.append(this.getPath());
            } else {
                if (this.joinType == JoinType.INNER) {
                    builder.append("inner join ");
                } else {
                    builder.append("left join ");
                }
                if (this.fetch) {
                    builder.append("fetch ");
                }
                if (this.getCollectionFetchType() == CollectionFetchType.PARTIAL) {
                    builder.append("partial(").append(this.name).append(')');
                } else {
                    builder.append(this.name);
                }
                if (!Nulls.isNullOrEmpty(this.loadedScalarNames)) {
                    builder.append('[');
                    Strings.join(this.loadedScalarNames, builder);
                    builder.append(']');
                }
            }
            builder.append("\r\n");
            if (this.childNodes != null) {
                for (JoinNodeImpl childJoinNodeImpl : this.childNodes.values()) {
                    childJoinNodeImpl.toString(builder, tabCount + 1);
                }
            }
            if (this.orderNodes != null) {
                for (OrderNodeImpl orderNode : this.orderNodes.values()) {
                    orderNode.toStringForJoinNode(builder, tabCount + 1);
                }
            }
        }
        
        private void markJoinNodeAsUsedByOrderRequest() {
            Map<String, OrderNodeImpl> orderRequests = this.orderNodes;
            if (orderRequests != null && !orderRequests.isEmpty()) {
                this.setUsed();
            }
            Map<String, JoinNodeImpl> childNodes = this.childNodes;
            if (childNodes != null) {
                for (JoinNodeImpl childJoiNodeImpl : childNodes.values()) {
                    childJoiNodeImpl.markJoinNodeAsUsedByOrderRequest();
                }
            }
        }
        
        private void removeUnusedJoinNode() {
            Map<String, JoinNodeImpl> childNodes = this.childNodes;
            if (childNodes != null) {
                Iterator<JoinNodeImpl> itr = childNodes.values().iterator();
                while (itr.hasNext()) {
                    JoinNodeImpl childNodeImpl = itr.next();
                    if (!childNodeImpl.creatingData.used) {
                        itr.remove();
                    } else {
                        childNodeImpl.removeUnusedJoinNode();
                    }
                }
            }
        }
        
        private boolean initializeContainsScalarEagerness() {
            Map<String, JoinNodeImpl> childNodes = this.childNodes;
            if (childNodes != null) {
                for (JoinNodeImpl childJoiNodeImpl : childNodes.values()) {
                    if (childJoiNodeImpl.initializeContainsScalarEagerness()) {
                        this.containsScalarEagerness = true; 
                        //don't break, all the children must be initialized
                    } 
                }
            }
            if (!Nulls.isNullOrEmpty(this.loadedScalarNames)) {
                this.containsScalarEagerness = true;
            }
            return this.containsScalarEagerness;
        }
        
        private boolean initializeContainsInnerJoins() {
            Map<String, JoinNodeImpl> childNodes = this.childNodes;
            if (childNodes != null) {
                for (JoinNodeImpl childJoiNodeImpl : childNodes.values()) {
                    if (childJoiNodeImpl.initializeContainsInnerJoins()) {
                        this.containsInnerJoins = true; 
                        //don't break, all the children must be initialized
                    } 
                }
            }
            if (this.joinType == JoinType.INNER) {
                this.containsInnerJoins = true;
            }
            return this.containsInnerJoins;
        }
        
        private boolean initializeContainsCollectionJoins() {
            Map<String, JoinNodeImpl> childNodes = this.childNodes;
            if (childNodes != null) {
                for (JoinNodeImpl childJoiNodeImpl : childNodes.values()) {
                    if (childJoiNodeImpl.initializeContainsCollectionJoins()) {
                        this.containsCollectionJoins = true;
                        //don't break, all the children must be initialized
                    }
                }
            }
            if (this.collection || this.containsImplicitCollectionJoins()) {
                this.containsCollectionJoins = true;
            }
            return this.containsCollectionJoins;
        }
        
        private boolean initializeContainsCollectionInnerJoins() {
            Map<String, JoinNodeImpl> childNodes = this.childNodes;
            if (childNodes != null) {
                for (JoinNodeImpl childJoinNodeImpl : childNodes.values()) {
                    if (childJoinNodeImpl.initializeContainsCollectionInnerJoins()) {
                        this.containsCollectionInnerJoins = true;
                        //don't break, all the children must be initialized
                    }
                }
            }
            if (this.collection && this.joinType == JoinType.INNER) {
                this.containsCollectionInnerJoins = true;
            }
            return this.containsCollectionInnerJoins;
        }
        
        private boolean initializeContainsNoFetchJoins() {
            Map<String, JoinNodeImpl> childNodes = this.childNodes;
            if (childNodes != null) {
                for (JoinNodeImpl childJoiNodeImpl : childNodes.values()) {
                    if (childJoiNodeImpl.initializeContainsNoFetchJoins()) {
                        this.containsNoFetchJoins = true;
                        //don't break, all the children must be initialized
                    }
                }
            }
            if (this.parentNode != null && !this.fetch) {
                this.containsNoFetchJoins = true;
            }
            return this.containsNoFetchJoins;
        }
        
        private void finalInitializePrivateData() {
            Map<String, JoinNodeImpl> childNodes = this.childNodes;
            if (childNodes != null) {
                for (JoinNodeImpl childJoiNodeImpl : childNodes.values()) {
                    childJoiNodeImpl.finalInitializePrivateData();
                }
            }
            if (this.loadedScalarNames == null) {
                this.loadedScalarNames = MACollections.emptyOrderedSet();
            } else {
                this.loadedScalarNames = MACollections.unmodifiable(this.loadedScalarNames);
            }
            if (childNodes == null) {
                this.childNodes = MACollections.emptyOrderedMap();
            } else {
                this.childNodes = MACollections.unmodifiable(childNodes);
            }
            if (this.orderNodes == null) {
                this.orderNodes = MACollections.emptyOrderedMap();
            } else {
                this.orderNodes = MACollections.unmodifiable(this.orderNodes);
            }
            this.creatingData = null;
            this.path = null;
            this.toString = null;
        }
    }
    
    private static class OrderNodeImpl implements OrderNode {
        
        private static final Pattern DOT = Pattern.compile("\\.");
        
        int sequence;
        
        JoinNodeImpl parentNode;
        
        private String quanifiedName;
        
        List<String> names;
        
        boolean post;
        
        boolean desc;
        
        public OrderNodeImpl(int sequence, JoinNodeImpl parentNode, String quanifiedName, boolean post, boolean desc) {
            this.names = new ArrayList<>();
            this.quanifiedName = quanifiedName;
            if (quanifiedName.indexOf('.') == -1) {
                this.names.add(quanifiedName);
            } else {
                for (String name : DOT.split(quanifiedName)) {
                    this.names.add(name);
                }
            }
            this.sequence = sequence;
            this.parentNode = parentNode;
            this.post = post;
            this.desc = desc;
        }
        
        @Override
        public int getSequence() {
            return this.sequence;
        }
        
        @Override
        public JoinNode getParentNode() {
            return this.parentNode;
        }

        @Override
        public String getQuanifiedName() {
            return this.quanifiedName;
        }

        @Override
        public List<String> getNames() {
            return MACollections.unmodifiable(this.names);
        }

        @Override
        public boolean isPost() {
            return this.post;
        }

        @Override
        public boolean isDesc() {
            return this.desc;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (this.isPost()) {
                builder.append("post order by ");
            } else {
                builder.append("pre order by ");
            }
            builder.append(this.parentNode.getPath()).append('@');
            this.toNameAndTypeString(builder);
            return builder.toString();
        }
        
        void toStringForJoinNode(StringBuilder builder, int tabCount) {
            for (int i = tabCount - 1; i >= 0; i--) {
                builder.append('\t');
            }
            if (this.isPost()) {
                builder.append("post order by ");
            } else {
                builder.append("pre order by ");
            }
            this.toNameAndTypeString(builder);
            builder.append("\r\n");
        }
        
        private void toNameAndTypeString(StringBuilder builder) {
            builder.append(this.getQuanifiedName());
            if (this.isDesc()) {
                builder.append(" desc");
            } else {
                builder.append(" asc");
            }
        }
    }
    
    private static class JoinNodeTempCreatingData {
        
        boolean used;
        
        EntityDelegate entityDelegate;
        
        Map<String, PropertyDelegate> propertyDelegates;
    }
    

    @I18N    
    private static native String methodCanNotReturnNull(Class<?> ownerType, String methodSignature);
        
    @I18N    
    private static native String joinPropertyMustBeAssociationProperty(String parentPath, String name);
        
    @I18N    
    private static native String fetchPathCanNotContainId(FetchPath fetchPath, String idPropertyName);
        
    @I18N    
    private static native String fetchPathConOnlyContainOneScalar(FetchPath fetchPath, String firstScalarNodeName);
        
    @I18N    
    private static native String scalarPropertyCanNotBeRequired(FetchPath fetchPath, String scalarNodeName);
        
    @I18N    
    private static native String scalarPropertyCanNotBePartial(FetchPath fetchPath, String scalarNodeName);
        
    @I18N    
    private static native String requiredOrderPropertyMustBeAssociationProperty(
                String parentPath,
                GetterType getterType,
                String name);

    @I18N    
    private static native String simpleOrderPathStateAreConflict(
                String parentPath1, 
                String state1,
                String parentPath2, 
                String state2);
}
