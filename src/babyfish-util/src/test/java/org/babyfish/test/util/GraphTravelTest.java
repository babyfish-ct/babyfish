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
package org.babyfish.test.util;

import java.util.Iterator;
import java.util.List;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.util.GraphTravelAction;
import org.babyfish.util.GraphTravelContext;
import org.babyfish.util.GraphTraveler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class GraphTravelTest {
    
    /*
     * Use static member to save the object graph,
     * to test whether the travelContextId of each object 
     * can work normally for several times. 
     */
    private static Atom SODIUM_BENZOATE_START = createSodiumBenzoate();
    
    @Test
    public void testDepthFirst() {
        final List<String> beforeList = new ArrayList<String>();
        final List<String> afterList = new ArrayList<String>();
        Atom.TRAVELER.depthFirstTravel(
                SODIUM_BENZOATE_START,
                new GraphTravelAction<Atom>() {
                    @Override
                    public void preTravelNeighborNodes(GraphTravelContext<Atom> ctx) {
                        beforeList.add(ctx.getNode().toString());
                    }
                    @Override
                    public void postTravelNeighborNodes(GraphTravelContext<Atom> ctx) {
                        afterList.add(ctx.getNode().toString());
                    }
                });
        Assert.assertEquals(15, beforeList.size());
        Assert.assertEquals(15, afterList.size());
        assertList(
                beforeList,
                "C", "C1", "C2", "H2", "C3", "H3", "C4", "H4", "C5", "H5", "C6", "H6", "O1", "O2", "Na");
        assertList(
                afterList,
                "H2", "H3", "H4", "H5", "H6", "C6", "C5", "C4", "C3", "C2", "C1", "O1", "Na", "O2", "C");
    }
    
    @Test
    public void testBreadthFirst() {
        final List<String> beforeList = new ArrayList<String>();
        final List<String> afterList = new ArrayList<String>();
        Atom.TRAVELER.breadthFirstTravel(
                SODIUM_BENZOATE_START,
                new GraphTravelAction<Atom>() {
                    @Override
                    public void preTravelNeighborNodes(GraphTravelContext<Atom> ctx) {
                        beforeList.add(ctx.getNode().toString());
                    }
                    @Override
                    public void postTravelNeighborNodes(GraphTravelContext<Atom> ctx) {
                        afterList.add(ctx.getNode().toString());
                    }
                });
        Assert.assertEquals(15, beforeList.size());
        Assert.assertEquals(15, afterList.size());
        assertList(
                beforeList,
                "C",
                "C1", "O1", "O2",
                "C2", "C6", "Na",
                "H2", "C3", "H6", "C5",
                "H3", "C4", "H5",
                "H4");
        assertList(
                afterList,
                "H4",
                "H3", "C4", "H5",
                "H2", "C3", "H6", "C5",
                "C2", "C6", "Na",
                "C1", "O1", "O2",
                "C");
    }
    
    private static Atom createSodiumBenzoate() {
        /*
         * C6H5-CO2-Na:
         * 
         *    H3    H2
         *     \    /
         *      C3-C2      O1
         *     /    \      |
         * H4-C4    C1-----C-O2-----Na
         *     \    /
         *      C5-C6
         *     /     \
         *    H5     H6
         */
        Atom h2 = new Atom("H2");
        Atom h3 = new Atom("H3");
        Atom h4 = new Atom("H4");
        Atom h5 = new Atom("H5");
        Atom h6 = new Atom("H6");
        Atom c1 = new Atom("C1");
        Atom c2 = new Atom("C2");
        Atom c3 = new Atom("C3");
        Atom c4 = new Atom("C4");
        Atom c5 = new Atom("C5");
        Atom c6 = new Atom("C6");
        Atom c = new Atom("C");
        Atom o1 = new Atom("O1");
        Atom o2 = new Atom("O2");
        Atom na = new Atom("Na");
        
        c2.connect(h2);
        c3.connect(h3);
        c4.connect(h4);
        c5.connect(h5);
        c6.connect(h6);
        
        c1.connect(c2);
        c2.connect(c3);
        c3.connect(c4);
        c4.connect(c5);
        c5.connect(c6);
        c6.connect(c1);
        
        c1.connect(c);
        
        c.connect(o1);
        c.connect(o2);
        
        o2.connect(na);
        
        return c;
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertList(List<E> list, E ... elements) {
        Assert.assertEquals(elements.length, list.size());
        int index = 0;
        for (E e : list) {
            E element = elements[index++];
            if (element == null) {
                Assert.assertNull(e);
            } else {
                Assert.assertEquals(element, e);
            }
        }
    }

    static class Atom {
        
        static GraphTraveler<Atom> TRAVELER =
            new GraphTraveler<Atom>(true) {
                @Override
                protected Iterator<Atom> getNeighborNodeIterator(Atom node) {
                    return node.getNeighbors().iterator();
                }

                @Override
                protected boolean isVisitable(GraphTravelContext<Atom> ctx) {
                    return ctx.getNode().travelContextId != ctx.getId();
                }

                @Override
                protected void visited(GraphTravelContext<Atom> ctx) {
                    ctx.getNode().travelContextId = ctx.getId();
                }
            };
        
        private String id;
        
        private XOrderedSet<Atom> neighbors;
        
        private transient long travelContextId;
        
        public Atom(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public XOrderedSet<Atom> getNeighbors() {
            XOrderedSet<Atom> neighbors = this.neighbors;
            if (neighbors == null) {
                this.neighbors = neighbors = new LinkedHashSet<>();
            }
            return neighbors;
        }
        
        public void connect(Atom other) {
            if (this.getNeighbors().add(other)) {
                other.getNeighbors().add(this);
            }
        }
        
        @Override
        public String toString() {
            return this.id;
        }
    }
    
}
