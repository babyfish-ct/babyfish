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

import org.babyfish.lang.Strings;
import org.babyfish.util.GraphTravelAction;
import org.babyfish.util.GraphTravelContext;
import org.babyfish.util.GraphTraveler;

/**
 * @author Tao Chen
 */
public class Style2GraphTravelerTest extends AbstractGraphTravelerTest {
    
    @Override
    protected Atom2 createAtom(String name) {
        return new Atom2(name);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected GraphTraveler<Atom> createAtomTraveler(StringBuilder recorderBuilder) {
        return (GraphTraveler)new AtomTraveler(recorderBuilder);
    }

    private static class Atom2 extends Atom {
        
        transient long ctxId;

        public Atom2(String name) {
            super(name);
        }
    }

    private static class AtomTraveler extends GraphTraveler<Atom2> {
        
        private StringBuilder target;
        
        public AtomTraveler(StringBuilder target) {
            super(true); // "true" means Supporting the allocation of context id.
            this.target = target;
        }

        // For Graph traveler with object circular, "isVistable" and "visited" are required
        @Override
        protected boolean isVisitable(GraphTravelContext<Atom2> ctx) {
            return ctx.getNode().ctxId != ctx.getId();
        }

        // For Graph traveler with object circular, "isVistable" and "visited" are required
        @Override
        protected void visited(GraphTravelContext<Atom2> ctx) {
            ctx.getNode().ctxId = ctx.getId();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Iterator<Atom2> getNeighborNodeIterator(Atom2 atom) {
            return (Iterator)atom.getNeighbors().iterator();
        }

        @Override
        protected void preTravelNeighborNodes(
                GraphTravelContext<Atom2> ctx,
                GraphTravelAction<Atom2> optionalGraphTravelAction) {
            
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
                    (Atom2 x) -> x.getName(), 
                    sb
            );
            sb.append(")\n");
        }
    }
}
