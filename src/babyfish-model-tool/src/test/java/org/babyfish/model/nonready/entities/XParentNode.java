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

import org.babyfish.model.Contravariance;
import org.babyfish.model.Model;
import org.babyfish.model.metadata.ModelClass;

@Model
public class XParentNode extends ParentNode {
    
    public static final int X_CHILD_NODES_ID = 
            ModelClass.of(XParentNode.class).getProperties().get("xChildNodes").getId();
    
    @Contravariance(from = "childNodes")
    private Set<XChildNode> xChildNodes;

    public Set<XChildNode> getXChildNodes() {
        return xChildNodes;
    }

    public void setXChildNodes(Set<XChildNode> xChildNodes) {
        this.xChildNodes = xChildNodes;
    }
}
