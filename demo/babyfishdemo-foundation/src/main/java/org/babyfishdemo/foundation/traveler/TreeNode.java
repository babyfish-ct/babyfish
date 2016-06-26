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

import java.util.List;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;

/**
 * @author Tao Chen
 */
public class TreeNode {

    private String name;
    
    private List<TreeNode> childNodes;
    
    public TreeNode(String name) {
        this(name, (TreeNode[])null);
    }
    
    public TreeNode(String name, TreeNode ... childNodes) {
        Arguments.mustNotContainNullElements("childNodes", childNodes);
        this.name = name;
        if (Nulls.isNullOrEmpty(childNodes)) { 
            this.childNodes = MACollections.emptyList();
        } else {
            this.childNodes = MACollections.wrap(childNodes);
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public List<TreeNode> getChildNodes() {
        return this.childNodes;
    }
}
