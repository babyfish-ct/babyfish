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
import java.util.Set;

import org.babyfish.collection.HashSet;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.lang.Strings;
import org.babyfish.util.GraphTravelAction;
import org.babyfish.util.GraphTravelContext;
import org.babyfish.util.GraphTraveler;

/**
 * @author Tao Chen
 */
public class Style1GraphTravelerTest extends AbstractGraphTravelerTest {
    
    @Override
    protected GraphTraveler<Atom> createAtomTraveler(StringBuilder recorderBuilder) {
        return new AtomTraveler(recorderBuilder);
    }

    private static class AtomTraveler extends GraphTraveler<Atom> {
        
        private StringBuilder target;
        
        private Set<Atom> visitedAtoms = new HashSet<>(ReferenceEqualityComparator.getInstance());
        
        public AtomTraveler(StringBuilder target) {
            this.target = target;
        }

        // For Graph traveler with object circular, "isVistable" and "visited" are required
        @Override
        protected boolean isVisitable(GraphTravelContext<Atom> ctx) {
            return !this.visitedAtoms.contains(ctx.getNode());
        }

        // For Graph traveler with object circular, "isVistable" and "visited" are required
        @Override
        protected void visited(GraphTravelContext<Atom> ctx) {
            this.visitedAtoms.add(ctx.getNode());
        }

        @Override
        protected Iterator<Atom> getNeighborNodeIterator(Atom atom) {
            return atom.getNeighbors().iterator();
        }

        @Override
        protected void preTravelNeighborNodes(
                GraphTravelContext<Atom> ctx,
                GraphTravelAction<Atom> optionalGraphTravelAction) {
            
            super.preTravelNeighborNodes(ctx, optionalGraphTravelAction);
            
            StringBuilder sb = this.target;
            for (int i = ctx.getDepth(); i > 0; i--) {
                sb.append("    ");
            }
            sb.append(ctx.getNode().getName()).append(" (indexPath = ");
            Strings.join(
                    ctx.getBranchNodeIndexes(), 
                    ".", 
                    (Integer x) -> Integer.toString(x.intValue() + 1), 
                    sb
            );
            sb.append(", namePath = ");
            Strings.join(
                    ctx.getBranchNodes(), 
                    "->", 
                    (Atom x) -> x.getName(), 
                    sb
            );
            sb.append(")\n");
        }
    }
}
