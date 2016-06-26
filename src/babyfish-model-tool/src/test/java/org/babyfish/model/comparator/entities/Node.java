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
package org.babyfish.model.comparator.entities;

import java.util.Set;

import org.babyfish.model.Association;
import org.babyfish.model.Model;
import org.babyfish.model.Scalar;

@Model
public class Node {
    
    @Scalar
    private String name;
    
    @Association(opposite = "parent")
    private Set<Node> childNodes;
    
    @Association(opposite = "childNodes")
    private Node parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Node> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(Set<Node> childNodes) {
        this.childNodes = childNodes;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}
