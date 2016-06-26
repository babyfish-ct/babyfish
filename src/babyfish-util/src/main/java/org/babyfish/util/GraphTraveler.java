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
package org.babyfish.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.LinkedList;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.collection.XList;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.lang.I18N;
import org.babyfish.lang.Nulls;

/**
 * @author Tao Chen
 */
public abstract class GraphTraveler<N> {
    
    private static final AtomicLong CONTEXT_ID_SEQUENCE = new AtomicLong();
    
    private boolean allocateContextId;
    
    protected GraphTraveler() {
        
    }
    
    protected GraphTraveler(boolean allocateContextId) {
        this.allocateContextId = allocateContextId;
    }

    public final GraphTraveler<N> depthFirstTravel(N startNode) {
        this.new DepthFirstContext(MACollections.wrap(startNode), this.travelNull(), null).travel();
        return this;
    }
    
    public final GraphTraveler<N> depthFirstTravel(N startNode, GraphTravelAction<N> graphTravelAction) {
        this.new DepthFirstContext(MACollections.wrap(startNode), this.travelNull(), graphTravelAction).travel();
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public final GraphTraveler<N> depthFirstTravel(N startNode, GraphTravelAction<N> ... graphTravelActions) {
        this.new DepthFirstContext(MACollections.wrap(startNode), this.travelNull(), combineActions(graphTravelActions)).travel();
        return this;
    }
    
    public final GraphTraveler<N> breadthFirstTravel(N startNode) {
        this.new BreadthFirstContext(MACollections.wrap(startNode), this.travelNull(), null).travel();
        return this;
    }
    
    public final GraphTraveler<N> breadthFirstTravel(N startNode, GraphTravelAction<N> graphTravelAction) {
        this.new BreadthFirstContext(MACollections.wrap(startNode), this.travelNull(), graphTravelAction).travel();
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public final GraphTraveler<N> breadthFirstTravel(N startNode, GraphTravelAction<N> ... graphTravelActions) {
        this.new BreadthFirstContext(MACollections.wrap(startNode), this.travelNull(), combineActions(graphTravelActions)).travel();
        return this;
    }
    
    public final GraphTraveler<N> depthFirstTravel(Iterable<N> startNodes) {
        if (!Nulls.isNullOrEmpty(startNodes)) {
            this.new DepthFirstContext(startNodes, this.travelNull(), null).travel();
        }
        return this;
    }
    
    public final GraphTraveler<N> depthFirstTravel(Iterable<N> startNodes, GraphTravelAction<N> graphTravelAction) {
        if (!Nulls.isNullOrEmpty(startNodes)) {
            this.new DepthFirstContext(startNodes, this.travelNull(), graphTravelAction).travel();
        }
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public final GraphTraveler<N> depthFirstTravel(Iterable<N> startNodes, GraphTravelAction<N> ... graphTravelActions) {
        if (!Nulls.isNullOrEmpty(startNodes)) {
            this.new DepthFirstContext(startNodes, this.travelNull(), combineActions(graphTravelActions)).travel();
        }
        return this;
    }
    
    public final GraphTraveler<N> breadthFirstTravel(Iterable<N> startNodes) {
        if (!Nulls.isNullOrEmpty(startNodes)) {
            this.new BreadthFirstContext(startNodes, this.travelNull(), null).travel();
        }
        return this;
    }
    
    public final GraphTraveler<N> breadthFirstTravel(Iterable<N> startNodes, GraphTravelAction<N> graphTravelAction) {
        if (!Nulls.isNullOrEmpty(startNodes)) {
            this.new BreadthFirstContext(startNodes, this.travelNull(), graphTravelAction).travel();
        }
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public final GraphTraveler<N> breadthFirstTravel(Iterable<N> startNodes, GraphTravelAction<N> ... graphTravelActions) {
        if (!Nulls.isNullOrEmpty(startNodes)) {
            this.new BreadthFirstContext(startNodes, this.travelNull(), combineActions(graphTravelActions)).travel();
        }
        return this;
    }
    
    public final GraphTraveler<N> depthFirstTravel(N[] startNodes) {
        if (!Nulls.isNullOrEmpty(startNodes)) {
            this.new DepthFirstContext(MACollections.wrap(startNodes), this.travelNull(), null).travel();
        }
        return this;
    }
    
    public final GraphTraveler<N> depthFirstTravel(N[] startNodes, GraphTravelAction<N> graphTravelAction) {
        if (!Nulls.isNullOrEmpty(startNodes)) {
            this.new DepthFirstContext(MACollections.wrap(startNodes), this.travelNull(), graphTravelAction).travel();
        }
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public final GraphTraveler<N> depthFirstTravel(N[] startNodes, GraphTravelAction<N> ... graphTravelActions) {
        if (!Nulls.isNullOrEmpty(startNodes)) {
            this.new DepthFirstContext(MACollections.wrap(startNodes), this.travelNull(), combineActions(graphTravelActions)).travel();
        }
        return this;
    }
    
    public final GraphTraveler<N> breadthFirstTravel(N[] startNodes) {
        if (!Nulls.isNullOrEmpty(startNodes)) {
            this.new BreadthFirstContext(MACollections.wrap(startNodes), this.travelNull(), null).travel();
        }
        return this;
    }
    
    public final GraphTraveler<N> breadthFirstTravel(N[] startNodes, GraphTravelAction<N> graphTravelAction) {
        if (!Nulls.isNullOrEmpty(startNodes)) {
            this.new BreadthFirstContext(MACollections.wrap(startNodes), this.travelNull(), graphTravelAction).travel();
        }
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public final GraphTraveler<N> breadthFirstTravel(N[] startNodes, GraphTravelAction<N> ... graphTravelActions) {
        if (!Nulls.isNullOrEmpty(startNodes)) {
            this.new BreadthFirstContext(MACollections.wrap(startNodes), this.travelNull(), combineActions(graphTravelActions)).travel();
        }
        return this;
    }
    
    protected boolean isVisitable(GraphTravelContext<N> ctx) {
        return true;
    }
    
    protected void visited(GraphTravelContext<N> ctx) {
        
    }
    
    protected boolean travelNull() {
        return false;
    }
    
    protected abstract Iterator<N> getNeighborNodeIterator(N node);
    
    protected void preTravelNeighborNodes(
            GraphTravelContext<N> ctx,
            GraphTravelAction<N> optionalGraphTravelAction) {
        if (optionalGraphTravelAction != null) {
            optionalGraphTravelAction.preTravelNeighborNodes(ctx);
        }
    }
    
    protected void postTravelNeighborNodes(
            GraphTravelContext<N> ctx,
            GraphTravelAction<N> optionalGraphTravelAction) {
        if (optionalGraphTravelAction != null) {
            optionalGraphTravelAction.postTravelNeighborNodes(ctx);
        }
    }
    
    private static <N> GraphTravelAction<N> combineActions(
            GraphTravelAction<N>[] graphTravelActions) {
        GraphTravelAction<N> combinedGraphTravelAction = null;
        for (GraphTravelAction<N> graphTravelAction : graphTravelActions) {
            combinedGraphTravelAction = GraphTravelAction.combine(
                    combinedGraphTravelAction, graphTravelAction);
        }
        return combinedGraphTravelAction;
    }
    
    private abstract class Context implements GraphTravelContext<N> {
        
        private static final int DEPTH_ARR_INITIAL_LENGTH = 8;
        
        long id;
        
        N node;
        
        Collection<N> startNodes;
        
        int depth = -1;
        
        @SuppressWarnings("unchecked")
        N[] branchNodeArr = (N[])new Object[DEPTH_ARR_INITIAL_LENGTH];
        
        //initial length must be same with the initial length of branchNodeArr
        int[] branchNodeIndexArr = new int[DEPTH_ARR_INITIAL_LENGTH];
        
        XList<N> nodes = MACollections.wrap(this.branchNodeArr);
        
        XList<Integer> nodeIndexes = MACollections.wrapInt(this.branchNodeIndexArr);
        
        XList<N> branchNodes;
        
        XList<Integer> branchNodeIndexes;
        
        GraphTravelAction<N> graphTravelAction;
        
        boolean travelNull;
        
        boolean travelSiblingNodesStopped;
        
        boolean travelNeighborNodesStopped;
        
        boolean travelStopped;
        
        boolean duringBeforeTravelNeighborNodes;
        
        Context(Iterable<N> startNodes, boolean travelNull, GraphTravelAction<N> graphTravelAction) {
            if (startNodes instanceof Collection<?>) {
                this.startNodes = new LinkedHashSet<>(
                        ReferenceEqualityComparator.getInstance(),
                        (Collection<N>)startNodes
                );
            } else {
                XOrderedSet<N> c = new LinkedHashSet<>();
                for (N startNode : startNodes) {
                    c.add(startNode);
                }
                this.startNodes = c;
            }
            this.travelNull = travelNull;
            this.graphTravelAction = graphTravelAction;
            if (GraphTraveler.this.allocateContextId) {
                long id = CONTEXT_ID_SEQUENCE.incrementAndGet();
                if (id == 0) {
                    throw new GraphTravelContextIdAllocatingError();
                }
                this.id = id;
            }
        }
        
        @Override
        public N getNode() {
            if (this.depth == -1) {
                throw new IllegalStateException(invokedMethodTooEarly(GraphTravelContext.class, "getNode"));
            }
            return this.node;
        }

        @Override
        public Collection<N> getStartNodes() {
            if (this.depth == -1) {
                throw new IllegalStateException(invokedMethodTooEarly(GraphTravelContext.class, "getStartNode"));
            }
            return MACollections.unmodifiable(this.startNodes);
        }

        @Override
        public int getDepth() {
            if (this.depth == -1) {
                throw new IllegalStateException(invokedMethodTooEarly(GraphTravelContext.class, "getDepth"));
            }
            return this.depth;
        }

        @Override
        public XList<N> getBranchNodes() {
            XList<N> branchNodes = this.branchNodes;
            if (branchNodes == null) {
                if (this.depth == -1) {
                    throw new IllegalStateException(invokedMethodTooEarly(GraphTravelContext.class, "getBranchNodes"));
                }
                int size = this.depth + 1;
                XList<N> nodes = this.nodes;
                if (size == nodes.size()) {
                    branchNodes = nodes;
                }
                branchNodes = nodes.subList(0, size);
            }
            return branchNodes;
        }

        @Override
        public XList<Integer> getBranchNodeIndexes() {
            XList<Integer> branchNodeIndexes = this.branchNodeIndexes;
            if (branchNodeIndexes == null) {
                if (this.depth == -1) {
                    throw new IllegalStateException(invokedMethodTooEarly(GraphTravelContext.class, "getBranchNodeIndexes"));
                }
                int size = this.depth + 1;
                XList<Integer> nodeIndexes = this.nodeIndexes;
                if (size == nodeIndexes.size()) {
                    branchNodeIndexes = nodeIndexes;
                }
                branchNodeIndexes = nodeIndexes.subList(0, size);
            }
            return branchNodeIndexes;
        }

        @Override
        public void stopTravelSiblingNodes() {
            if (this.depth == -1) {
                throw new IllegalStateException(invokedMethodTooEarly(GraphTravelContext.class, "stopTravelSiblingNodes"));
            }
            if (!this.duringBeforeTravelNeighborNodes) {
                throw new UnsupportedOperationException(
                        invocationMustDuringBeforeTravelNeighborNodes(
                                this.getClass(), 
                                "stopTravelSiblingNodes",
                                GraphTraveler.class,
                                GraphTravelAction.class
                        )
                );
            }
            this.travelSiblingNodesStopped = true;
        }

        @Override
        public void stopTravelNeighborNodes() {
            if (this.depth == -1) {
                throw new IllegalStateException(invokedMethodTooEarly(GraphTravelContext.class, "stopTravelNeighborNodes"));
            }
            if (!this.duringBeforeTravelNeighborNodes) {
                throw new UnsupportedOperationException(
                        invocationMustDuringBeforeTravelNeighborNodes(
                                this.getClass(), 
                                "stopTravelNeighborNodes",
                                GraphTraveler.class,
                                GraphTravelAction.class
                        )
                );
            }
            this.travelNeighborNodesStopped = true;
        }

        @Override
        public void stopTravel() {
            if (this.depth == -1) {
                throw new IllegalStateException(invokedMethodTooEarly(GraphTravelContext.class, "stopTravel"));
            }
            if (!this.duringBeforeTravelNeighborNodes) {
                throw new UnsupportedOperationException(
                        invocationMustDuringBeforeTravelNeighborNodes(
                                this.getClass(), 
                                "stopTravel",
                                GraphTraveler.class,
                                GraphTravelAction.class
                        )
                );
            }
            this.travelStopped = true;
        }
        
        @Override
        public long getId() {
            long id = this.id;
            if (id == 0L) {
                throw new UnsupportedOperationException(
                        getIdIsInvalidBecauseContextIdAllocatingIsNotSupported(
                                this.getClass(),
                                GraphTraveler.class
                        )
                );
            }
            return id;
        }

        abstract void travel();
        
        @SuppressWarnings("unchecked")
        void push() {
            int depth = this.depth;
            N[] oldBranchNodeArr = this.branchNodeArr;
            int[] oldBranchNodeIndexArr = this.branchNodeIndexArr;
            N[] branchNodeArr = oldBranchNodeArr;
            int[] branchNodeIndexArr = oldBranchNodeIndexArr;
            if (branchNodeArr.length == depth + 1){
                branchNodeArr = (N[])new Object[branchNodeArr.length << 1];
                branchNodeIndexArr = new int[branchNodeIndexArr.length << 1];
                System.arraycopy(oldBranchNodeArr, 0, branchNodeArr, 0, oldBranchNodeArr.length);
                System.arraycopy(oldBranchNodeIndexArr, 0, branchNodeIndexArr, 0, oldBranchNodeIndexArr.length);
            }
            if (branchNodeArr != oldBranchNodeArr) {
                this.branchNodeArr = branchNodeArr;
                this.branchNodeIndexArr = branchNodeIndexArr;
                this.nodes = MACollections.wrap(branchNodeArr);
                this.nodeIndexes = MACollections.wrapInt(branchNodeIndexArr);
            }
            this.branchNodes = null;
            this.branchNodeIndexes = null;
            this.branchNodeIndexArr[++this.depth] = -1;
        }
        
        void pop() {
            this.branchNodes = null;
            this.branchNodeIndexes = null;
            int depth = --this.depth;
            this.node = depth == -1 ? null : this.branchNodeArr[depth];
        }
        
        boolean setNode(N node) {
            int depth = this.depth;
            this.node = this.branchNodeArr[depth] = node;
            this.branchNodeIndexArr[depth]++;
            return node != null ? true : this.travelNull;
        }
        
    }
    
    private class DepthFirstContext extends Context {

        DepthFirstContext(Iterable<N> startNodes, boolean travelNull, GraphTravelAction<N> graphTravelAction) {
            super(startNodes, travelNull, graphTravelAction);
        }
        
        @Override
        void travel() {
            this.push();
            for (N startNode : this.startNodes) {
                this.visitAndDoRecursion(startNode);
            }
            this.pop();
        }
        
        // return: travelSiblingNodesStopped
        boolean visitAndDoRecursion(N node) {
            GraphTraveler<N> traveler = GraphTraveler.this;
            GraphTravelAction<N> graphTravelAction = this.graphTravelAction;
            boolean ignoreSiblings = false;
            boolean ignoreNeighbors = false;
            if (this.setNode(node)) {
                if (node != null) {
                    if (!traveler.isVisitable(this)) {
                        return false;
                    }
                    traveler.visited(this);
                }
                this.duringBeforeTravelNeighborNodes = true;
                boolean oldTravelSiblingNodesStopped = this.travelSiblingNodesStopped;
                boolean oldTravelNeighborNodesStopped = this.travelNeighborNodesStopped;
                try {
                    traveler.preTravelNeighborNodes(this, graphTravelAction);
                    ignoreSiblings = this.travelSiblingNodesStopped;
                    ignoreNeighbors = this.travelNeighborNodesStopped;
                } finally {
                    this.duringBeforeTravelNeighborNodes = false;
                    this.travelSiblingNodesStopped = oldTravelSiblingNodesStopped;
                    this.travelNeighborNodesStopped = oldTravelNeighborNodesStopped;
                }
                if (node != null && !this.travelStopped && !ignoreNeighbors) {
                    Iterator<N> neighborNodeIterator = traveler.getNeighborNodeIterator(node);
                    if (neighborNodeIterator != null) {
                        this.push();
                        while (!this.travelStopped && neighborNodeIterator.hasNext()) {
                            if (this.visitAndDoRecursion(neighborNodeIterator.next())) {
                                break;
                            }
                        }
                        this.pop();
                    }
                }
                traveler.postTravelNeighborNodes(this, graphTravelAction);
            }
            return ignoreSiblings;
        }
    }
    
    private class BreadthFirstContext extends Context {
        
        BreadthFirstContext(Iterable<N> startNodes, boolean travelNull, GraphTravelAction<N> graphTravelAction) {
            super(startNodes, travelNull, graphTravelAction);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        boolean setNode(N node) {
            int depth = this.depth;
            if (node instanceof BreadthFirstResetNode<?>) {
                N[] branchNodeArr = this.branchNodeArr;
                int[] branchNodeIndexArr = this.branchNodeIndexArr;
                this.node = branchNodeArr[depth] = null;
                branchNodeIndexArr[depth] = -1;
                for (BreadthFirstResetNode<N> resetNode = (BreadthFirstResetNode<N>)node;
                resetNode != null && --depth >= 0 && branchNodeArr[depth] != resetNode.parentNode;
                resetNode = resetNode.parent) {
                    branchNodeArr[depth] = resetNode.parentNode;
                    branchNodeIndexArr[depth] = resetNode.parentIndex;
                }
                return false;
            }
            this.node = this.branchNodeArr[depth] = node;
            this.branchNodeIndexArr[depth]++;
            return node != null ? true : this.travelNull;
        }

        @SuppressWarnings("unchecked")
        @Override
        void travel() {
            this.push();
            for (N startNode : this.startNodes) {
                this.visitAndDoRecursion(
                        MACollections.wrap(
                                (N)new BreadthFirstResetNode<N>(null, -1, null), 
                                startNode
                        )
                );
            }
            this.pop();
        }

        @SuppressWarnings("unchecked")
        void visitAndDoRecursion(List<N> currLevelNodes) {
            GraphTraveler<N> traveler = GraphTraveler.this;
            GraphTravelAction<N> graphTravelAction = this.graphTravelAction;
            List<N> nextLevelNodes = 
                currLevelNodes.size() < 64 ? 
                        new LinkedList<N>() : 
                        new ArrayList<N>(currLevelNodes.size() * 2);
            boolean[] ignoreArr = new boolean[currLevelNodes.size()];
            int indexInLevel = -1;
            int depth = this.depth;
            BreadthFirstResetNode<N> resetNode = null;
            boolean skip = false;
            for (N node : currLevelNodes) {
                indexInLevel++;
                if (node instanceof BreadthFirstResetNode<?>) {
                    skip = this.travelStopped;
                    resetNode = (BreadthFirstResetNode<N>)node;
                }
                if (this.setNode(node)) {
                    if (skip) {
                        ignoreArr[indexInLevel] = true;
                    } else {
                        if (node != null) {
                            if (!traveler.isVisitable(this)) {
                                ignoreArr[indexInLevel] = true;
                                continue;
                            }
                            traveler.visited(this);
                        }
                        int index = this.branchNodeIndexArr[depth];
                        
                        boolean ignoreSiblings;
                        boolean ignoreNeighbors;
                        this.duringBeforeTravelNeighborNodes = true;
                        boolean oldTravelSiblingNodesStopped = this.travelSiblingNodesStopped;
                        boolean oldTravelNeighborNodesStopped = this.travelNeighborNodesStopped;
                        try {
                            traveler.preTravelNeighborNodes(this, graphTravelAction);
                            ignoreSiblings = this.travelSiblingNodesStopped;
                            ignoreNeighbors = this.travelNeighborNodesStopped;
                        } finally {
                            this.duringBeforeTravelNeighborNodes = false;
                            this.travelSiblingNodesStopped = oldTravelSiblingNodesStopped;
                            this.travelNeighborNodesStopped = oldTravelNeighborNodesStopped;
                        }
                        if (this.travelStopped) {
                            skip = true;
                            continue;
                        }
                        if (node != null && !ignoreNeighbors) {
                            Iterator<N> neighborNodeIterator = traveler.getNeighborNodeIterator(node);
                            if (neighborNodeIterator != null) {
                                if (neighborNodeIterator.hasNext()) {
                                    nextLevelNodes.add((N)new BreadthFirstResetNode<N>(node, index, resetNode));
                                    while (neighborNodeIterator.hasNext()) {
                                        nextLevelNodes.add(neighborNodeIterator.next());
                                    }
                                }
                            }
                        }
                        if (ignoreSiblings) {
                            skip = true;
                            continue;
                        }
                    }
                }
            }
            if (!nextLevelNodes.isEmpty()) {
                this.push();
                visitAndDoRecursion(nextLevelNodes);
                this.pop();
            }
            indexInLevel = -1;
            for (N node : currLevelNodes) {
                indexInLevel++;
                if (this.setNode(node) && !ignoreArr[indexInLevel]) {
                    traveler.postTravelNeighborNodes(this, graphTravelAction);
                }
            }
        }
        
    }
    
    private static class BreadthFirstResetNode<N> {

        N parentNode;
        
        int parentIndex;
        
        BreadthFirstResetNode<N> parent;
        
        public BreadthFirstResetNode(N parentNode, int parentIndex, BreadthFirstResetNode<N> parent) {
            this.parentNode = parentNode;
            this.parentIndex = parentIndex;
            this.parent = parent;
        }

        @Override
        public String toString() {
            return "<RESET>";
        }

    }
    
    @I18N
    private static native String invokedMethodTooEarly(Class<?> contextType, String methodName);

    @SuppressWarnings("rawtypes")
    @I18N
    private static native String invocationMustDuringBeforeTravelNeighborNodes(
            Class<?> runtimeType, 
            String methodName,
            Class<GraphTraveler> graphTravelerType,
            Class<GraphTravelAction> graphTravelerActionType);
    
    @SuppressWarnings("rawtypes")
    @I18N
    private static native String getIdIsInvalidBecauseContextIdAllocatingIsNotSupported(
            Class<?> runtimeType, 
            Class<GraphTraveler> graphTravelerType);
}
