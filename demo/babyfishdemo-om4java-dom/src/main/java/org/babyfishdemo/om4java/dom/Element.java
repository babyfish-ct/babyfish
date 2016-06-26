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

import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.Arguments;
import org.babyfish.model.Association;
import org.babyfish.model.Model;
import org.babyfish.model.Scalar;
import org.babyfishdemo.om4java.dom.visitor.ChildScopeAwareVisitor;
import org.babyfishdemo.om4java.dom.visitor.Visitor;

/**
 * @author Tao Chen
 */
@Model // Using ObjectModel4Java, requires compilation-time byte code instrument
public class Element extends Node {
    
    @Scalar
    private QuanifiedName quanifiedName;
    
    @Association(opposite = "ownerElement")
    private XOrderedMap<QuanifiedName, Attribute> attributes;
    
    public Element(String localName) {
        this.quanifiedName = new QuanifiedName(localName);
    }
    
    public Element(String namespaceURI, String localName) {
        this.quanifiedName = new QuanifiedName(namespaceURI, localName);
    }
    
    public Element(QuanifiedName quanifiedName) {
        this.quanifiedName = quanifiedName;
    }
    
    public Element(String localName, Node ... childNodes) {
        this(new QuanifiedName(localName), childNodes);
    }
    
    public Element(String namespaceURI, String localName, Node ... childNodes) {
        this(new QuanifiedName(namespaceURI, localName), childNodes);
    }
    
    public Element(QuanifiedName quanifiedName, Node ... childNodes) {
        this.quanifiedName = quanifiedName;
        for (Node childNode : childNodes) {
            if (childNode instanceof Attribute) {
                this.addAttribute((Attribute)childNode);
            } else {
                this.getChildNodes().add(childNode);
            }
        }
    }

    public QuanifiedName getQuanifiedName() {
        return quanifiedName;
    }

    public void setQuanifiedName(QuanifiedName quanifiedName) {
        this.quanifiedName = quanifiedName;
    }

    public XOrderedMap<QuanifiedName, Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(XOrderedMap<QuanifiedName, Attribute> attributes) {
        this.attributes = attributes;
    }

    /*
     * Shortcut method of ".getAttributes.put(quanifiedName, attribute)"
     * when the attribute.quanifiedName is ALREADY assigned
     */
    public void addAttribute(Attribute attribute) {
        if (attribute != null) {
            Arguments.mustNotBeNull("attribute.quanifiedName", attribute.getQuanifiedName());
            this.attributes.put(attribute.getQuanifiedName(), attribute);
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.ELEMENT;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitElement(this);
        for (Attribute attribute : this.attributes.values()) {
            attribute.accept(visitor);
        }
        if (visitor instanceof ChildScopeAwareVisitor) {
            ChildScopeAwareVisitor childScopeAwareVisitor = (ChildScopeAwareVisitor)visitor;
            childScopeAwareVisitor.enterChildScope(this);
            for (Node childNode : this.getChildNodes()) {
                childNode.accept(visitor);
            }
            childScopeAwareVisitor.leaveChildScope(this);
        } else {
            for (Node childNode : this.getChildNodes()) {
                childNode.accept(visitor);
            }
        }
    }

    @Override
    protected void validateChildNode(Node childNode) {
        if (!(childNode instanceof Element) &&
                !(childNode instanceof Text) &&
                !(childNode instanceof Comment)) {
            throw new IllegalArgumentException(
                    "The child node of Element can only be Element, Text or Comment"
            );
        }
    }
}
