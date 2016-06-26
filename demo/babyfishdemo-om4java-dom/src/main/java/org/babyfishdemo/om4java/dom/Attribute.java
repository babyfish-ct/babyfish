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

import org.babyfish.model.Association;
import org.babyfish.model.KeyOf;
import org.babyfish.model.Model;
import org.babyfish.model.Scalar;
import org.babyfishdemo.om4java.dom.visitor.Visitor;

/**
 * @author Tao Chen
 */
@Model // Using ObjectModel4Java, requires compilation-time byte code instrument
public class Attribute extends Node {

    @KeyOf(value = "ownerElement", absolute = true)
    private QuanifiedName quanifiedName;
    
    @Association(opposite = "attributes")
    private Element ownerElement;
    
    @Scalar
    private String value;
    
    public Attribute() {}
    
    public Attribute(String localName, String value) {
        this(null, localName, value);
    }
    
    public Attribute(String namespaceURI, String localName, String value) {
        this(new QuanifiedName(namespaceURI, localName), value);
    }
    
    public Attribute(QuanifiedName quanifiedName, String value) {
        this.quanifiedName = quanifiedName;
        this.value = value;
    }
        
    public QuanifiedName getQuanifiedName() {
        return quanifiedName;
    }

    public void setQuanifiedName(QuanifiedName quanifiedName) {
        this.quanifiedName = quanifiedName;
    }

    public Element getOwnerElement() {
        return ownerElement;
    }

    public void setOwnerElement(Element ownerElement) {
        this.ownerElement = ownerElement;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.ATTRIBUTE;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAttribute(this);
    }
}
