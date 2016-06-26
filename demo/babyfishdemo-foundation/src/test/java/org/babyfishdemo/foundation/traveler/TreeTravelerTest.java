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
package org.babyfishdemo.foundation.traveler;

import java.util.Iterator;

import org.babyfish.lang.Ref;
import org.babyfish.lang.Strings;
import org.babyfish.util.GraphTravelAction;
import org.babyfish.util.GraphTravelActionAdapter;
import org.babyfish.util.GraphTravelContext;
import org.babyfish.util.GraphTraveler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class TreeTravelerTest {

    private static final TreeNode ROOT_TREE_NODE =
            new TreeNode(
                    "Low level SDKs for 3D",
                    new TreeNode(
                            "Graphics",
                            new TreeNode("Direct3D"),
                            new TreeNode("OpenGL"),
                            new TreeNode("OpenGL ES(and WebGL)")
                    ),
                    new TreeNode(
                            "Sound",
                            new TreeNode("DirectSound"),
                            new TreeNode("OpenAL"),
                            new TreeNode("OpenSL ES")
                    ),
                    new TreeNode(
                            "Physics",
                            new TreeNode("Lagoa Multiphysics"),
                            new TreeNode("PhysX"),
                            new TreeNode("Havok"),
                            new TreeNode("Bullet")
                    )
            );
            
    @Test
    public void testDepthFirst() {
        StringBuilder builder = new StringBuilder();
        new TreeNodeTraveler(builder).depthFirstTravel(ROOT_TREE_NODE);
        Assert.assertEquals(
                "1 Low level SDKs for 3D\n" +
                "    1.1 Graphics\n" +
                "        1.1.1 Direct3D\n" +
                "        1.1.2 OpenGL\n" +
                "        1.1.3 OpenGL ES(and WebGL)\n" +
                "    1.2 Sound\n" +
                "        1.2.1 DirectSound\n" +
                "        1.2.2 OpenAL\n" +
                "        1.2.3 OpenSL ES\n" +
                "    1.3 Physics\n" +
                "        1.3.1 Lagoa Multiphysics\n" +
                "        1.3.2 PhysX\n" +
                "        1.3.3 Havok\n" +
                "        1.3.4 Bullet\n",
                builder.toString()
        );
    }
    
    @Test
    public void testBreadthFirst() {
        StringBuilder builder = new StringBuilder();
        new TreeNodeTraveler(builder).breadthFirstTravel(ROOT_TREE_NODE);
        Assert.assertEquals(
                "1 Low level SDKs for 3D\n" +
                "    1.1 Graphics\n" +
                "    1.2 Sound\n" +
                "    1.3 Physics\n" +
                "        1.1.1 Direct3D\n" +
                "        1.1.2 OpenGL\n" +
                "        1.1.3 OpenGL ES(and WebGL)\n" +
                "        1.2.1 DirectSound\n" +
                "        1.2.2 OpenAL\n" +
                "        1.2.3 OpenSL ES\n" +
                "        1.3.1 Lagoa Multiphysics\n" +
                "        1.3.2 PhysX\n" +
                "        1.3.3 Havok\n" +
                "        1.3.4 Bullet\n",
                builder.toString()
        );
    }
    
    @Test
    public void testStopTravelNeighborNodes() {
        
        GraphTravelActionAdapter<TreeNode> stopNeighborAction = new GraphTravelActionAdapter<TreeNode>() {
            @Override
            public void preTravelNeighborNodes(GraphTravelContext<TreeNode> ctx) {
                if (ctx.getNode().getName().equals("Sound")) {
                    ctx.stopTravelNeighborNodes();
                }
            }
        };
        StringBuilder builder = new StringBuilder();
        
        new TreeNodeTraveler(builder).depthFirstTravel(ROOT_TREE_NODE, stopNeighborAction);
        Assert.assertEquals(
                "1 Low level SDKs for 3D\n" +
                "    1.1 Graphics\n" +
                "        1.1.1 Direct3D\n" +
                "        1.1.2 OpenGL\n" +
                "        1.1.3 OpenGL ES(and WebGL)\n" +
                "    1.2 Sound\n" +
                "    1.3 Physics\n" +
                "        1.3.1 Lagoa Multiphysics\n" +
                "        1.3.2 PhysX\n" +
                "        1.3.3 Havok\n" +
                "        1.3.4 Bullet\n", 
                builder.toString()
        );
        
        builder.setLength(0);
        
        new TreeNodeTraveler(builder).breadthFirstTravel(ROOT_TREE_NODE, stopNeighborAction);
        Assert.assertEquals(
                "1 Low level SDKs for 3D\n" +
                "    1.1 Graphics\n" +
                "    1.2 Sound\n" +
                "    1.3 Physics\n" +
                "        1.1.1 Direct3D\n" +
                "        1.1.2 OpenGL\n" +
                "        1.1.3 OpenGL ES(and WebGL)\n" +
                "        1.3.1 Lagoa Multiphysics\n" +
                "        1.3.2 PhysX\n" +
                "        1.3.3 Havok\n" +
                "        1.3.4 Bullet\n", 
                builder.toString()
        );
    }

    @Test
    public void testStopTravelSiblingNodes() {
        
        GraphTravelActionAdapter<TreeNode> stopTravelSiblingAction = new GraphTravelActionAdapter<TreeNode>() {
            @Override
            public void preTravelNeighborNodes(GraphTravelContext<TreeNode> ctx) {
                if (ctx.getNode().getName().equals("Sound")) {
                    ctx.stopTravelSiblingNodes();
                }
            }
        };
        StringBuilder builder = new StringBuilder();
        
        new TreeNodeTraveler(builder).depthFirstTravel(ROOT_TREE_NODE, stopTravelSiblingAction);
        Assert.assertEquals(
                "1 Low level SDKs for 3D\n" +
                "    1.1 Graphics\n" +
                "        1.1.1 Direct3D\n" +
                "        1.1.2 OpenGL\n" +
                "        1.1.3 OpenGL ES(and WebGL)\n" +
                "    1.2 Sound\n" +
                "        1.2.1 DirectSound\n" +
                "        1.2.2 OpenAL\n" +
                "        1.2.3 OpenSL ES\n", 
                builder.toString()
        );
        
        builder.setLength(0);
        
        new TreeNodeTraveler(builder).breadthFirstTravel(ROOT_TREE_NODE, stopTravelSiblingAction);
        Assert.assertEquals(
                "1 Low level SDKs for 3D\n" +
                "    1.1 Graphics\n" +
                "    1.2 Sound\n" +
                "        1.1.1 Direct3D\n" +
                "        1.1.2 OpenGL\n" +
                "        1.1.3 OpenGL ES(and WebGL)\n" +
                "        1.2.1 DirectSound\n" +
                "        1.2.2 OpenAL\n" +
                "        1.2.3 OpenSL ES\n",
                builder.toString()
        );
    }

    @Test
    public void testStopTravel() {
        
        GraphTravelActionAdapter<TreeNode> stopTravelAction = new GraphTravelActionAdapter<TreeNode>() {
            @Override
            public void preTravelNeighborNodes(GraphTravelContext<TreeNode> ctx) {
                if (ctx.getNode().getName().equals("Sound")) {
                    ctx.stopTravel();
                }
            }
        };
        StringBuilder builder = new StringBuilder();
        
        new TreeNodeTraveler(builder).depthFirstTravel(ROOT_TREE_NODE, stopTravelAction);
        Assert.assertEquals(
                "1 Low level SDKs for 3D\n" +
                "    1.1 Graphics\n" +
                "        1.1.1 Direct3D\n" +
                "        1.1.2 OpenGL\n" +
                "        1.1.3 OpenGL ES(and WebGL)\n" +
                "    1.2 Sound\n", 
                builder.toString()
        );
        
        builder.setLength(0);
        
        new TreeNodeTraveler(builder).breadthFirstTravel(ROOT_TREE_NODE, stopTravelAction);
        Assert.assertEquals(
                "1 Low level SDKs for 3D\n" +
                "    1.1 Graphics\n" +
                "    1.2 Sound\n", 
                builder.toString()
        );
    }
    
    @Test
    public void testBadStopping() {
        /*
         * GraphTravelContext.stopTravel(),
         * GraphTravelContext.stopTravelSiblingNodes()
         * and 
         * GraphTravelContext.stopTravelNeighborNodes()
         * can ONLY be called during 
         * GraphTraveler.preTravelNeighborNodes(GraphTravelContext)
         * or 
         * GraphTravelAction.preTravelNeighborNodes(GraphTravelContext)
         */
        final Ref<Integer> exceptionCount = new Ref<Integer>(0);
        GraphTravelActionAdapter<TreeNode> badStopActionInPostHandler = new GraphTravelActionAdapter<TreeNode>() {
            @Override
            public void postTravelNeighborNodes(GraphTravelContext<TreeNode> ctx) {
                try {
                    ctx.stopTravel();
                } catch (UnsupportedOperationException ex) {
                    exceptionCount.set(exceptionCount.get() + 1);
                }
                try {
                    ctx.stopTravelSiblingNodes();
                } catch (UnsupportedOperationException ex) {
                    exceptionCount.set(exceptionCount.get() + 1);
                }
                try {
                    ctx.stopTravelNeighborNodes();
                } catch (UnsupportedOperationException ex) {
                    exceptionCount.set(exceptionCount.get() + 1);
                }
            }
        };
        new TreeNodeTraveler(new StringBuilder()).depthFirstTravel(new TreeNode(""), badStopActionInPostHandler);
        new TreeNodeTraveler(new StringBuilder()).breadthFirstTravel(new TreeNode(""), badStopActionInPostHandler);
        Assert.assertEquals(6, exceptionCount.get().intValue());
    }
    
    @Test
    public void testDepthFirstByPrintingAfterTravelNeighbors() {
        StringBuilder builder = new StringBuilder();
        new TreeNodeTraveler(builder, true).depthFirstTravel(ROOT_TREE_NODE);
        Assert.assertEquals(
                "        1.1.1 Direct3D\n" +
                "        1.1.2 OpenGL\n" +
                "        1.1.3 OpenGL ES(and WebGL)\n" +
                "    1.1 Graphics\n" +
                "        1.2.1 DirectSound\n" +
                "        1.2.2 OpenAL\n" +
                "        1.2.3 OpenSL ES\n" +
                "    1.2 Sound\n" +
                "        1.3.1 Lagoa Multiphysics\n" +
                "        1.3.2 PhysX\n" +
                "        1.3.3 Havok\n" +
                "        1.3.4 Bullet\n" +
                "    1.3 Physics\n" +
                "1 Low level SDKs for 3D\n",
                builder.toString()
        );
    }
    
    @Test
    public void testBreadthFirstByPrintingAfterTravelNeighbors() {
        StringBuilder builder = new StringBuilder();
        new TreeNodeTraveler(builder, true).breadthFirstTravel(ROOT_TREE_NODE);
        Assert.assertEquals(
                "        1.1.1 Direct3D\n" +
                "        1.1.2 OpenGL\n" +
                "        1.1.3 OpenGL ES(and WebGL)\n" +
                "        1.2.1 DirectSound\n" +
                "        1.2.2 OpenAL\n" +
                "        1.2.3 OpenSL ES\n" +
                "        1.3.1 Lagoa Multiphysics\n" +
                "        1.3.2 PhysX\n" +
                "        1.3.3 Havok\n" +
                "        1.3.4 Bullet\n" +
                "    1.1 Graphics\n" +
                "    1.2 Sound\n" +
                "    1.3 Physics\n" +
                "1 Low level SDKs for 3D\n",
                builder.toString()
        );
    }
    
    private static class TreeNodeTraveler extends GraphTraveler<TreeNode> {
        
        private StringBuilder target;
        
        private boolean printAfterTravelNeighbors;
        
        public TreeNodeTraveler(StringBuilder target) {
            this(target, false);
        }
        
        public TreeNodeTraveler(StringBuilder target, boolean printAfterTravelNeighbors) {
            super();
            this.target = target;
            this.printAfterTravelNeighbors = printAfterTravelNeighbors;
        }

        @Override
        protected Iterator<TreeNode> getNeighborNodeIterator(TreeNode node) {
            return node.getChildNodes().iterator();
        }

        @Override
        protected void preTravelNeighborNodes(
                GraphTravelContext<TreeNode> ctx,
                GraphTravelAction<TreeNode> optionalGraphTravelAction) {
            super.preTravelNeighborNodes(ctx, optionalGraphTravelAction);
            if (!this.printAfterTravelNeighbors) {
                this.printCurrentNode(ctx);
            }
        }
        
        @Override
        protected void postTravelNeighborNodes(
                GraphTravelContext<TreeNode> ctx,
                GraphTravelAction<TreeNode> optionalGraphTravelAction) {
            super.postTravelNeighborNodes(ctx, optionalGraphTravelAction);
            if (this.printAfterTravelNeighbors) {
                this.printCurrentNode(ctx);
            }
        }

        private void printCurrentNode(GraphTravelContext<TreeNode> ctx) {
            StringBuilder sb = this.target;
            for (int i = ctx.getDepth(); i > 0; i--) {
                sb.append("    ");
            }
            Strings.join(
                    ctx.getBranchNodeIndexes(), 
                    ".", 
                    (Integer x) -> Integer.toString(x.intValue() + 1),
                    sb
            );
            sb
            .append(' ')
            .append(ctx.getNode().getName())
            .append('\n');
        }
    }
}
