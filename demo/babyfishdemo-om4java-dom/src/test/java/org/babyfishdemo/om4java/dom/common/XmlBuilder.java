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

import java.util.Map;
import java.util.Map.Entry;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;
import org.babyfishdemo.om4java.dom.Attribute;
import org.babyfishdemo.om4java.dom.Comment;
import org.babyfishdemo.om4java.dom.Element;
import org.babyfishdemo.om4java.dom.QuanifiedName;
import org.babyfishdemo.om4java.dom.Text;
import org.babyfishdemo.om4java.dom.visitor.ChildScopeAwareVisitor;
import org.babyfishdemo.om4java.dom.visitor.Visitor;

/**
 * @author Tao Chen
 */
public class XmlBuilder {

    private XOrderedMap<String, String> prefixNamespaceURIMap;
    
    public XmlBuilder() {
        this.prefixNamespaceURIMap = new LinkedHashMap<>();
    }
    
    private XmlBuilder(XOrderedMap<String, String> prefixNamespaceURIMap) {
        this.prefixNamespaceURIMap = prefixNamespaceURIMap;
    }
    
    public XmlBuilder addPrefix(String prefix, String namespaceURI) {
        this.prefixNamespaceURIMap.put(
                Arguments.mustNotBeNull("prefix", prefix),
                Arguments.mustNotBeEmpty("namespaceURI", 
                        Arguments.mustNotBeNull("namespaceURI", namespaceURI)
                )
        );
        return this;
    }
    
    public XmlBuilder readOnly() {
        return new XmlBuilder(this.prefixNamespaceURIMap) {

            @Override
            public XmlBuilder addPrefix(String prefix, String namespaceURI) {
                throw new UnsupportedOperationException("The current builder is readonly");
            }

            @Override
            public XmlBuilder readOnly() {
                return this;
            }
        };
    }
    
    public String build(Element element) {
        Arguments.mustNotBeNull("element", element);
        PrefixAllocationVisitor prefixAllocationVisitor = 
                new PrefixAllocationVisitor(this.prefixNamespaceURIMap);
        XMLGeneratorVisitor xmlGeneratorVisitor = 
                new XMLGeneratorVisitor(prefixAllocationVisitor.getNamespaceURIPrefixMap());
        element.accept(prefixAllocationVisitor);
        element.accept(xmlGeneratorVisitor);
        return xmlGeneratorVisitor.toXML();
    }
    
    private static class PrefixAllocationVisitor implements Visitor {

        private Map<String, String> prefixNamespaceURIMap;
        
        private Map<String, String> namespaceURIPrefixMap;
        
        private int sequence = 0;
        
        public PrefixAllocationVisitor(XOrderedMap<String, String> prefixNamespaceURIMap) {
            this.prefixNamespaceURIMap = new LinkedHashMap<>(prefixNamespaceURIMap);
            Map<String, String> reversedMap = new LinkedHashMap<>((prefixNamespaceURIMap.size() * 4 + 2) / 3);
            for (Entry<String, String> e : prefixNamespaceURIMap.entrySet()) {
                reversedMap.put(e.getValue(), e.getKey());
            }
            this.namespaceURIPrefixMap = reversedMap;
        }
        
        public Map<String, String> getNamespaceURIPrefixMap() {
            return namespaceURIPrefixMap;
        }

        @Override
        public void visitElement(Element element) {
            this.processNamespaceURI(element.getQuanifiedName().getNamespaceURI());
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            this.processNamespaceURI(attribute.getQuanifiedName().getNamespaceURI());
        }

        @Override
        public void visitText(Text text) {}

        @Override
        public void visitComment(Comment comment) {}
        
        private void processNamespaceURI(String namespaceURI) {
            if (Nulls.isNullOrEmpty(namespaceURI)) {
                return;
            }
            if (this.namespaceURIPrefixMap.containsKey(namespaceURI)) {
                return;
            }
            String prefix;
            while (true) {
                prefix = "ns" + this.sequence++;
                if (!this.prefixNamespaceURIMap.containsKey(prefix)) {
                    break;
                }
            }
            this.prefixNamespaceURIMap.put(prefix, namespaceURI);
            this.namespaceURIPrefixMap.put(namespaceURI, prefix);
        }
    }
    
    private static class XMLGeneratorVisitor implements ChildScopeAwareVisitor { 
        
        private Map<String, String> namespaceURIPrefixMap;
        
        private StringBuilder builder = new StringBuilder();
        
        private int depth;
        
        public XMLGeneratorVisitor(Map<String, String> namespaceURIPrefixMap) {
            this.namespaceURIPrefixMap = namespaceURIPrefixMap;
        }

        public String toXML() {
            return this.builder.toString();
        }

        @Override
        public void visitElement(Element element) {
            builder
            .append('<')
            .append(quanifiedText(element.getQuanifiedName()));
            if (this.depth == 0) {
                for (Entry<String, String> e : this.namespaceURIPrefixMap.entrySet()) {
                    String prefix = e.getValue();
                    String namespaceURI = e.getKey();
                    builder.append(" xmlns");
                    if (!Nulls.isNullOrEmpty(prefix)) {
                        builder.append(':').append(prefix);
                    }
                    builder.append("=\'").append(namespaceURI).append('\'');
                }
            }
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            this
            .builder
            .append(' ')
            .append(quanifiedText(attribute.getQuanifiedName()))
            .append("=\'")
            .append(escape(attribute.getValue()))
            .append('\'');
        }

        @Override
        public void visitText(Text text) {
            this.builder.append(escape(text.getData()));
        }

        @Override
        public void visitComment(Comment comment) {
            this
            .builder
            .append("<!--")
            .append(escape(comment.getData()))
            .append("-->");
        }
        
        @Override
        public void enterChildScope(Element element) {
            if (element.getChildNodes().isEmpty()) {
                this.builder.append("/>");
            } else {
                this.builder.append('>');
            }
            this.depth++;
        }

        @Override
        public void leaveChildScope(Element element) {
            this.depth--;
            if (!element.getChildNodes().isEmpty()) {
                this
                .builder
                .append("</")
                .append(quanifiedText(element.getQuanifiedName()))
                .append('>');
            }
        }

        private String quanifiedText(QuanifiedName quanifiedName) {
            String namespaceURI = quanifiedName.getNamespaceURI();
            String prefix = null;
            if (!Nulls.isNullOrEmpty(namespaceURI)) {
                prefix = this.namespaceURIPrefixMap.get(namespaceURI);
            }
            if (Nulls.isNullOrEmpty(prefix)) {
                return quanifiedName.getLocalName();
            }
            return prefix + ':' + quanifiedName.getLocalName();
        }
        
        private static String escape(String data) {
            return data
                    .replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;")
                    .replaceAll("&", "&amp;")
                    .replaceAll("\"", "&quot;")
                    .replaceAll("'", "&apos;");
        }
    }
}
