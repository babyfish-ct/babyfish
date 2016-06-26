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

import java.util.List;

import org.babyfish.collection.XList;
import org.babyfish.model.Association;
import org.babyfish.model.IndexOf;
import org.babyfish.model.Model;
import org.babyfishdemo.om4java.dom.visitor.Visitor;

/**
 * In w3c XML demo structure, Node should have 12 non-abstract derived classes: 
 * Element, Attr, Text, CDATASection, EntityReference, Entity, ProcessingInstruction, 
 * Comment, Document, DocumentType, DocumentFragment, Notation.
 * 
 * But, for this demo, in order to make the code to be more simple, Node ONLY 
 * has 4 non-abstract derived classes: Element, Attribute, Text and Comment
 *
 * @author Tao Chen
 */
@Model // Using ObjectModel4Java, requires compilation-time byte code instrument
public abstract class Node {
    
    @IndexOf("parentNode")
    private int index;
    
    @Association(opposite = "childNodes")
    private Node parentNode;
    
    @Association(opposite = "parentNode")
    private List<Node> childNodes;
    
    protected Node() {
        XList<Node> childNodes = (XList<Node>)this.childNodes;
        childNodes.addValidator(this::validateChildNode);
    }
    
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    public List<Node> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<Node> childNodes) {
        this.childNodes = childNodes;
    }

    public Node getFirstChild() {
        List<Node> childNodes = this.childNodes;
        return childNodes.isEmpty() ? null : childNodes.get(0);
    }
    
    public Node getLastChild() {
        List<Node> childNodes = this.childNodes;
        return childNodes.isEmpty() ? null : childNodes.get(childNodes.size() - 1);
    }
    
    public Node getPreviousSibling() {
        int index = this.index;
        if (index <= 0) {
            return null;
        }
        return this.parentNode.childNodes.get(index - 1);
    }
    
    public Node getNextSibling() {
        int index = this.index;
        if (index == -1) {
            return null;
        }
        List<Node> siblings = this.parentNode.childNodes;
        if (index >= siblings.size() - 1) {
            return null;
        }
        return siblings.get(index + 1);
    }
    
    public abstract NodeType getNodeType();

    public abstract void accept(Visitor visitor);
    
    // Some derived classes should override it.
    protected void validateChildNode(Node childNode) {
        throw new UnsupportedOperationException(
                "The instance of \""
                + this.getClass().getName()
                + "\" does accept any child nodes"
        );
    }
}
