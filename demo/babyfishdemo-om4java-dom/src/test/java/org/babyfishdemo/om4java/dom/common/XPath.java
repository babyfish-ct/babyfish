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
package org.babyfishdemo.om4java.dom.common;

import java.util.List;
import java.util.Map;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.HashMap;
import org.babyfish.lang.Arguments;
import org.babyfish.model.metadata.ModelClass;
import org.babyfishdemo.om4java.dom.Attribute;
import org.babyfishdemo.om4java.dom.Element;
import org.babyfishdemo.om4java.dom.Node;
import org.babyfishdemo.om4java.dom.QuanifiedName;

/*
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * 
 * NOTES:
 * 
 * This implementation is WRONG! it does not implement all the functionalities
 * of xpath, only a little of them has been implemented.
 * 
 * But this WRONG implementation is still enough for this demo project,
 * it is unnecessary to implement all the logic of XPath for this demo,
 * keep the simple code and let's focus on ObjectModel4Java :)
 */
/**
 * @author Tao Chen
 */
public class XPath {
    
    // Actual, it's ForzenEqualityComparator.
    private static final EqualityComparator<QuanifiedName> QUANIFIED_NAME_COMPARATOR = 
            ModelClass.of(QuanifiedName.class).getDefaultEqualityComparator();

    private Map<String, String> prefixNamespaceURIMap;
    
    public XPath() {
        this.prefixNamespaceURIMap = new HashMap<>();
    }
    
    private XPath(Map<String, String> prefixNamespaceURIMap) {
        this.prefixNamespaceURIMap = prefixNamespaceURIMap;
    }

    public XPath addPrefix(String prefix, String namespaceURI) {
        this.prefixNamespaceURIMap.put(
                Arguments.mustNotBeNull("prefix", prefix),
                Arguments.mustNotBeEmpty("namespaceURI", 
                        Arguments.mustNotBeNull("namespaceURI", namespaceURI)
                )
        );
        return this;
    }
    
    public XPath readOnly() {
        return new XPath(this.prefixNamespaceURIMap) {

            @Override
            public XPath addPrefix(String prefix, String namespaceURI) {
                throw new UnsupportedOperationException("The current xpath is readonly");
            }

            @Override
            public XPath readOnly() {
                return this;
            }
        };
    }
    
    @SuppressWarnings("unchecked")
    public <N extends Node> N selectSingleNode(Node node, String xpath) {
        List<Node> nodes = this.selectNodes(node, xpath);
        if (nodes.isEmpty()) {
            return null;
        }
        return (N)nodes.get(0);
    }
    
    public List<Node> selectNodes(Node node, String xpath) {
        
        /*
         * As a demo, we need NOT to implement all the functionality of XPath!
         * 
         * We only need to implement the basic functionalities: "/" and "@",
         * need not to implement the basic functionalities: 
         * "//", ".", "..", "[predicate]", "*", "node()", "|"
         */
        if (xpath.indexOf("//") != -1) {
            throw new UnsupportedOperationException("unlimitted level node \"//\" is not implemented temporarily");
        }
        if (xpath.indexOf('.') != -1) {
            throw new UnsupportedOperationException("current node \".\" is not implemented temporarily");
        }
        if (xpath.indexOf('.') != -1) {
            throw new UnsupportedOperationException("current node \".\" is not implemented temporarily");
        }
        if (xpath.indexOf('[') != -1 || xpath.indexOf(']') != -1) {
            throw new UnsupportedOperationException("pridicate \"[]\" is not implemented temporarily");
        }
        if (xpath.indexOf("*") != -1) {
            throw new UnsupportedOperationException("wildcard(\"*\") is not implemented temporarily");
        }
        if (xpath.indexOf('(') != -1 || xpath.indexOf(')') != -1) {
            throw new UnsupportedOperationException("function \"()\" is not implemented temporarily");
        }
        if (xpath.indexOf('|') != -1) {
            throw new UnsupportedOperationException("or \"|\" is not implemented temporarily");
        }
        if (xpath.startsWith("/")) {
            throw new UnsupportedOperationException("xpath starts with \"/\" is not implemented temporarily");
        }
        if (xpath.endsWith("/")) {
            xpath = xpath.substring(0, xpath.length() - 1);
        }
        if (!(node instanceof Element)) {
            throw new UnsupportedOperationException("xpath for node that is not element is not implemented temporarily");
        }
        
        List<Node> foundNodes = new ArrayList<>();
        foundNodes.add(node);
        for (String nodeName : xpath.split("/")) {
            List<Node> deeperFoundNodes = new ArrayList<>();
            for (Node foundNode : foundNodes) {
                if (!(foundNode instanceof Element)) {
                    break;
                }
                Element element = (Element)foundNode;
                boolean isAttribute = nodeName.startsWith("@");
                if (isAttribute) {
                    nodeName = nodeName.substring(1);
                }
                String namespaceURI;
                String localName;
                int index = nodeName.indexOf(':');
                if (index == -1) {
                    namespaceURI = this.prefixNamespaceURIMap.get("");
                    localName = nodeName;
                } else {
                    String prefix = nodeName.substring(0, index);
                    namespaceURI = this.prefixNamespaceURIMap.get(prefix);
                    if (namespaceURI == null) {
                        throw new IllegalArgumentException("Unknown prefix: " + prefix);
                    }
                    localName = nodeName.substring(index + 1);
                }
                QuanifiedName quanifiedName = new QuanifiedName(namespaceURI, localName);
                if (isAttribute) {
                    Attribute foundAttribute = element.getAttributes().get(quanifiedName);
                    if (foundAttribute != null) {
                        deeperFoundNodes.add(foundAttribute);
                    }
                } else {
                    for (Node childNode : element.getChildNodes()) {
                        if (childNode instanceof Element) {
                            Element childElement = (Element)childNode;
                            if (QUANIFIED_NAME_COMPARATOR.equals(quanifiedName, childElement.getQuanifiedName())) {
                                deeperFoundNodes.add(childElement);
                            }
                        }
                    }
                }
            }
            if (deeperFoundNodes.isEmpty()) {
                return deeperFoundNodes;
            }
            foundNodes = deeperFoundNodes;
        }
        return foundNodes;
    }
}
