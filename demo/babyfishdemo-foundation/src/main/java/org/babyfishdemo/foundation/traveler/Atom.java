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

import java.util.Set;

import org.babyfish.model.Association;
import org.babyfish.model.Model;
import org.babyfish.model.Scalar;

/**
 * @author Tao Chen
 */
/*
 * The annotation @Model is very important, it means
 * this class uses another functionality called "ObjectModel4Java".
 * 
 * You need not to fully know what the "ObjectModel4Java" is when 
 * you are learning this demo because this demo only wants to tell you
 * what the "GraphTraveler" is. But you need to know a little about it.
 *      When you do 
 *          "a.getNeighbors().add(b)"
 *      , The 
 *          "b.getNeighbors().add(a)" 
 *      will be executed automatically and implicitly
 */
@Model
public class Atom {
    
    @Scalar
    private String name;
    
    @Association(opposite = "neighbors")
    private Set<Atom> neighbors;
    
    public Atom(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Atom> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(Set<Atom> neighbors) {
        this.neighbors = neighbors;
    }
}
