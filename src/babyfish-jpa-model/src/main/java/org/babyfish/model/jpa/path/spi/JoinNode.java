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
package org.babyfish.model.jpa.path.spi;

import java.util.Map;

import javax.persistence.criteria.JoinType;

import org.babyfish.collection.XOrderedSet;
import org.babyfish.model.jpa.path.CollectionFetchType;

/**
 * @author Tao Chen
 */
public interface JoinNode {

    String getName();
    
    JoinType getJoinType();
    
    boolean isFetch();
    
    CollectionFetchType getCollectionFetchType();
    
    XOrderedSet<String> getLoadedScalarNames();
    
    JoinNode getParentNode();
    
    Map<String, JoinNode> getChildNodes();
    
    Map<String, OrderNode> getOrderNodes();
    
    String getPath();
    
    boolean isCollection();
    
    boolean containsScalarEagerness();
    
    boolean containsInnerJoins();
    
    boolean containsCollectionJoins();
    
    boolean containsCollectionInnerJoins();
        
    boolean containsNoFetchJoins();
}
