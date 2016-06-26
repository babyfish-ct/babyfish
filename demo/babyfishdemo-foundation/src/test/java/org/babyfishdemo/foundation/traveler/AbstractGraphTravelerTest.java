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

import org.babyfish.util.GraphTraveler;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public abstract class AbstractGraphTravelerTest {

    protected final Atom createSodiumBenzoate() {
        /*
         * The molecules of "Sodium Benzoate"
         *    (C6H5-CO2-Na) 
         * have simple structure with only one ring,
         * but it is complex enough for this demo
         * 
         *      H4     H5
         *       \     /
         *       C4---C5      O1
         *       /     \      |
         * H3---C3     C6-----C7---O2-----Na1
         *       \     /
         *       C2---C1
         *       /     \
         *      H2     H1
         *      
         * We use "C7" to be the starting point of the graph travel.
         */
        
        /*
         * Because of ObjectModel4Java,
         * the explicit code 
         *      "a.getNeighbors().add(b);"
         * can do 
         *      "b.getNeighbors().add(a);" 
         * implicitly.
         */
        
        Atom c1 = this.createAtom("C1");
        Atom c2 = this.createAtom("C2");
        Atom c3 = this.createAtom("C3");
        Atom c4 = this.createAtom("C4");
        Atom c5 = this.createAtom("C5");
        Atom c6 = this.createAtom("C6");
        Atom c7 = this.createAtom("C7");
        
        Atom h1 = this.createAtom("H1");
        Atom h2 = this.createAtom("H2");
        Atom h3 = this.createAtom("H3");
        Atom h4 = this.createAtom("H4");
        Atom h5 = this.createAtom("H5");
        
        Atom o1 = this.createAtom("O1");
        Atom o2 = this.createAtom("O2");
        
        Atom na1 = this.createAtom("Na1");
        
        /*
         * Create the base ring of the phenyl.
         */
        c1.getNeighbors().add(c2);
        c2.getNeighbors().add(c3);
        c3.getNeighbors().add(c4);
        c4.getNeighbors().add(c5);
        c5.getNeighbors().add(c6);
        c6.getNeighbors().add(c1);
        
        /*
         * Add the hydrogen atoms to the phenyl ring.
         */
        c1.getNeighbors().add(h1);
        c2.getNeighbors().add(h2);
        c3.getNeighbors().add(h3);
        c4.getNeighbors().add(h4);
        c5.getNeighbors().add(h5);
        
        /*
         * Create the carboxyl sodium
         */
        c7.getNeighbors().add(o1);
        c7.getNeighbors().add(o2);
        o2.getNeighbors().add(na1);
        
        /*
         * connect the phenyl and carboxyl.
         */
        c6.getNeighbors().add(c7);
        
        return c7;
    }
    
    protected Atom createAtom(String name) {
        return new Atom(name);
    }
    
    protected abstract GraphTraveler<Atom> createAtomTraveler(StringBuilder recorderBuilder);

    /*
     *      H4     H5
     *       \     /
     *       C4---C5      O1
     *       /     \      |
     * H3---C3     C6-----C7---O2-----Na1
     *       \     /
     *       C2---C1
     *       /     \
     *      H2     H1
     *      
     * "C7 is the root atom"
     */
    private Atom rootAtom = this.createSodiumBenzoate();
    
    @Test
    public void testDepthFirst() {
        StringBuilder builder = new StringBuilder();
        this.createAtomTraveler(builder).depthFirstTravel(this.rootAtom);
        Assert.assertEquals(
                "C7 (indexPath = 1, namePath = C7)\n" +
                "    O1 (indexPath = 1.1, namePath = C7->O1)\n" +
                "    O2 (indexPath = 1.2, namePath = C7->O2)\n" +
                "        Na1 (indexPath = 1.2.2, namePath = C7->O2->Na1)\n" +
                "    C6 (indexPath = 1.3, namePath = C7->C6)\n" +
                "        C5 (indexPath = 1.3.1, namePath = C7->C6->C5)\n" +
                "            C4 (indexPath = 1.3.1.1, namePath = C7->C6->C5->C4)\n" +
                "                C3 (indexPath = 1.3.1.1.1, namePath = C7->C6->C5->C4->C3)\n" +
                "                    C2 (indexPath = 1.3.1.1.1.1, namePath = C7->C6->C5->C4->C3->C2)\n" +
                "                        C1 (indexPath = 1.3.1.1.1.1.1, namePath = C7->C6->C5->C4->C3->C2->C1)\n" +
                "                            H1 (indexPath = 1.3.1.1.1.1.1.3, namePath = C7->C6->C5->C4->C3->C2->C1->H1)\n" +
                "                        H2 (indexPath = 1.3.1.1.1.1.3, namePath = C7->C6->C5->C4->C3->C2->H2)\n" +
                "                    H3 (indexPath = 1.3.1.1.1.3, namePath = C7->C6->C5->C4->C3->H3)\n" +
                "                H4 (indexPath = 1.3.1.1.3, namePath = C7->C6->C5->C4->H4)\n" +
                "            H5 (indexPath = 1.3.1.3, namePath = C7->C6->C5->H5)\n", 
                builder.toString()
        );
    }
    
    @Test
    public void testBreadthFirst() {
        StringBuilder builder = new StringBuilder();
        this.createAtomTraveler(builder).breadthFirstTravel(this.rootAtom);
        Assert.assertEquals(
                "C7 (indexPath = 1, namePath = C7)\n" +
                "    O1 (indexPath = 1.1, namePath = C7->O1)\n" +
                "    O2 (indexPath = 1.2, namePath = C7->O2)\n" +
                "    C6 (indexPath = 1.3, namePath = C7->C6)\n" +
                "        Na1 (indexPath = 1.2.2, namePath = C7->O2->Na1)\n" +
                "        C5 (indexPath = 1.3.1, namePath = C7->C6->C5)\n" +
                "        C1 (indexPath = 1.3.2, namePath = C7->C6->C1)\n" +
                "            C4 (indexPath = 1.3.1.1, namePath = C7->C6->C5->C4)\n" +
                "            H5 (indexPath = 1.3.1.3, namePath = C7->C6->C5->H5)\n" +
                "            C2 (indexPath = 1.3.2.1, namePath = C7->C6->C1->C2)\n" +
                "            H1 (indexPath = 1.3.2.3, namePath = C7->C6->C1->H1)\n" +
                "                C3 (indexPath = 1.3.1.1.1, namePath = C7->C6->C5->C4->C3)\n" +
                "                H4 (indexPath = 1.3.1.1.3, namePath = C7->C6->C5->C4->H4)\n" +
                "                H2 (indexPath = 1.3.2.1.3, namePath = C7->C6->C1->C2->H2)\n" +
                "                    H3 (indexPath = 1.3.1.1.1.3, namePath = C7->C6->C5->C4->C3->H3)\n", 
                builder.toString()
        );
    }
    
    /*
     * BTW,'
     * 
     * GraphTravelContext.stopTravel(),
     * GraphTravelContext.stopTravelSiblingNodes()
     * and 
     * GraphTravelContext.stopTravelNeighborNodes()
     * 
     * have been demonstrated in TreeTravelerTest, 
     * so it is unnecessary to demonstrate them again in this test class
     */
}
