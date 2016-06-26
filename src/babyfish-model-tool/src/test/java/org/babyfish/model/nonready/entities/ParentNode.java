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
package org.babyfish.model.nonready.entities;

import java.util.Set;

import org.babyfish.model.Association;
import org.babyfish.model.Model;
import org.babyfish.model.metadata.ModelClass;

/**
 * 
 * @author Tao Chen
 */
@Model
public class ParentNode {
    
    public static final int CHILD_NODES_ID = 
            ModelClass.of(ParentNode.class).getProperties().get("childNodes").getId();
    
    @Association(opposite = "parentNode")
    private Set<ChildNode> childNodes;

    public Set<ChildNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(Set<ChildNode> childNodes) {
        this.childNodes = childNodes;
    }
}
