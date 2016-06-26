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
package org.babyfishdemo.om4java.dom;

/**
 * @author Tao Chen
 */
public enum NodeType {

    ELEMENT(1, Element.class),
    
    ATTRIBUTE(2, Attribute.class),
    
    TEXT(3, Text.class),
    
    COMMENT(8, Comment.class),
    
    ;
    
    private int w3cType;
    
    private Class<? extends Node> javaType;
    
    private NodeType(int w3cType, Class<? extends Node> javaType) {
        this.w3cType = w3cType;
        this.javaType = javaType;
    }
    
    public int getW3cType() {
        return w3cType;
    }

    public Class<? extends Node> toJavaType() {
        return this.javaType;
    }
    
    public static NodeType fromW3cType(int w3cType) {
        for (NodeType nodeType : values()) {
            if (nodeType.w3cType == w3cType) {
                return nodeType;
            }
        }
        throw new IllegalArgumentException(
                "The w3cType \""
                + w3cType
                + "\"is not accepted by this simplified implementation");
    }
}
