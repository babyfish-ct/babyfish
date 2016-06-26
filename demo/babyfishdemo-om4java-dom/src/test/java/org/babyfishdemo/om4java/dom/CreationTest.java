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

import junit.framework.Assert;

import org.babyfish.collection.MACollections;
import org.babyfishdemo.om4java.dom.Attribute;
import org.babyfishdemo.om4java.dom.Comment;
import org.babyfishdemo.om4java.dom.Element;
import org.babyfishdemo.om4java.dom.Node;
import org.babyfishdemo.om4java.dom.QuanifiedName;
import org.babyfishdemo.om4java.dom.common.XmlBuilder;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class CreationTest {
    
    private static final String NS_W3C_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    
    private static final String NS_SPRING_BEANS = "http://www.springframework.org/schema/beans";
    
    private static final String NS_ALIBABA_DUBBO = "http://code.alibabatech.com/schema/dubbo";
    
    private static final XmlBuilder XML_BUILDER =
            new XmlBuilder()
            .addPrefix("", NS_SPRING_BEANS)
            .addPrefix("dubbo", NS_ALIBABA_DUBBO)
            .addPrefix("xsi", NS_W3C_XSI)
            .readOnly();

    @Test
    public void testCreateTreeByAddChildToParent() {
        
        Element 
            beans = new Element(NS_SPRING_BEANS, "beans"),
            application = new Element(NS_ALIBABA_DUBBO, "application"),
            registry = new Element(NS_ALIBABA_DUBBO, "registry"),
            protocol = new Element(NS_ALIBABA_DUBBO, "protocol"),
            service = new Element(NS_ALIBABA_DUBBO, "service"),
            bean = new Element(NS_SPRING_BEANS, "bean");
        
        testPutOnAttributeMap(
                beans, 
                NS_W3C_XSI,
                "schemaLocation", 
                NS_SPRING_BEANS
                + " http://www.springframework.org/schema/beans/spring-beans.xsd "
                + NS_ALIBABA_DUBBO
                + " http://code.alibabatech.com/schema/dubbo/dubbo.xsd"
        );
        testPutOnAttributeMap(application, "name", "hello-world-app");
        testPutOnAttributeMap(registry, "address", "multicast://224.5.6.7:1234");
        testPutOnAttributeMap(protocol, "name", "dubbo");
        testPutOnAttributeMap(protocol, "port", "20880");
        testPutOnAttributeMap(service, "interface", "com.alibaba.dubbo.demo.DemoService");
        testPutOnAttributeMap(service, "ref", "demoService");
        testPutOnAttributeMap(bean, "id", "demoService");
        testPutOnAttributeMap(bean, "class", "com.alibaba.dubbo.demo.provider.DemoServiceImpl");
        
        testAddAllOnChildNodeList(
                beans, 
                new Comment("Provide application information to calculate dependencies"),
                application,
                new Comment("Use Multicast broadcast registry center to expose service address"),
                registry,
                new Comment("Apply dubbo protocol on port 20880"),
                protocol,
                new Comment("Declare the interface of service"),
                service,
                new Comment("Implement the service as local bean"),
                bean
        );
        
        Assert.assertEquals(
                "<beans "
                + "xmlns='http://www.springframework.org/schema/beans' "
                + "xmlns:dubbo='http://code.alibabatech.com/schema/dubbo' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://code.alibabatech.com/schema/dubbo "
                + "http://code.alibabatech.com/schema/dubbo/dubbo.xsd'>"
                
                +     "<!--Provide application information to calculate dependencies-->"
                +     "<dubbo:application name='hello-world-app'/>"
                
                +     "<!--Use Multicast broadcast registry center to expose service address-->"
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                
                +     "<!--Apply dubbo protocol on port 20880-->"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                
                +     "<!--Declare the interface of service-->"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                + "</beans>",
                XML_BUILDER.build(beans)
        );
    }
    
    public void testCreateTreeBySetParentOfChild() {
        
        Element 
            beans = new Element(NS_SPRING_BEANS, "beans"),
            application = new Element(NS_ALIBABA_DUBBO, "application"),
            registry = new Element(NS_ALIBABA_DUBBO, "registry"),
            protocol = new Element(NS_ALIBABA_DUBBO, "protocol"),
            service = new Element(NS_ALIBABA_DUBBO, "service"),
            bean = new Element(NS_SPRING_BEANS, "bean");
        
        testSetOwnerElementOnAttribute(
                NS_W3C_XSI, 
                "schemaLocation",
                NS_SPRING_BEANS
                + " http://www.springframework.org/schema/beans/spring-beans.xsd "
                + NS_ALIBABA_DUBBO
                + " http://code.alibabatech.com/schema/dubbo/dubbo.xsd", 
                beans
        );
        testSetOwnerElementOnAttribute("name", "hello-world-app", application);
        testSetOwnerElementOnAttribute("address", "multicast://224.5.6.7:1234", registry);
        testSetOwnerElementOnAttribute("name", "dubbo", protocol);
        testSetOwnerElementOnAttribute("port", "20880", protocol);
        testSetOwnerElementOnAttribute("interface", "com.alibaba.dubbo.demo.DemoService", service);
        testSetOwnerElementOnAttribute("ref", "demoService", service);
        testSetOwnerElementOnAttribute("id", "demoService", bean);
        testSetOwnerElementOnAttribute("class", "com.alibaba.dubbo.demo.provider.DemoServiceImpl", bean);
        
        testSetParentNodeOnChildNode(new Comment("Provide application information to calculate dependencies"), 0, beans);
        testSetParentNodeOnChildNode(application, 1, beans);
        testSetParentNodeOnChildNode(new Comment("Use Multicast broadcast registry center to expose service address"), 2, beans);
        testSetParentNodeOnChildNode(registry, 3, beans);
        testSetParentNodeOnChildNode(new Comment("Apply dubbo protocol on port 20880"), 4, beans);
        testSetParentNodeOnChildNode(protocol, 5, beans);
        testSetParentNodeOnChildNode(new Comment("Declare the interface of service"), 6, beans);
        testSetParentNodeOnChildNode(service, 7, beans);
        testSetParentNodeOnChildNode(new Comment("Implement the service as local bean"), 8, beans);
        testSetParentNodeOnChildNode(bean, 9, beans);
        
        Assert.assertEquals(
                "<beans "
                + "xmlns='http://www.springframework.org/schema/beans' "
                + "xmlns:dubbo='http://code.alibabatech.com/schema/dubbo' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://code.alibabatech.com/schema/dubbo "
                + "http://code.alibabatech.com/schema/dubbo/dubbo.xsd'>"
                
                +     "<!--Provide application information to calculate dependencies-->"
                +     "<dubbo:application name='hello-world-app'/>"
                
                +     "<!--Use Multicast broadcast registry center to expose service address-->"
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                
                +     "<!--Apply dubbo protocol on port 20880-->"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                
                +     "<!--Declare the interface of service-->"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                + "</beans>",
                XML_BUILDER.build(beans)
        );
    }
    
    private static void testPutOnAttributeMap(Element element, String localName, String value) {
        testPutOnAttributeMap(element, null, localName, value);
    }
    
    private static void testPutOnAttributeMap(Element element, String namespaceURI, String localName, String value) {
        Attribute attribute = new Attribute();
        attribute.setValue(value);
        
        /*
         * Before call "element.getAttributes().put(?, ?)",
         * both the "parent" and "quanifiedName" of "attribute" should be null
         */
        Assert.assertNull(attribute.getOwnerElement());
        Assert.assertNull(attribute.getQuanifiedName());
        
        /*
         * This explicit modification will cause two implicit modifications automatically
         * (1) "attribute.ownerElement" will be assigned automatically
         * (2) "attribute.quanifiedName" will be assigned automatically
         */
        element.getAttributes().put(new QuanifiedName(namespaceURI, localName), attribute);
        
        /*
         * After call "element.getAttributes().put(?, ?)"
         * both the "ownerElement" and "quanifiedName" of "attribute" are assigned automatically and implicitly
         */
        Assert.assertSame(element, attribute.getOwnerElement());
        Assert.assertEquals(namespaceURI, attribute.getQuanifiedName().getNamespaceURI());
        Assert.assertEquals(localName, attribute.getQuanifiedName().getLocalName());
    }
    
    private static void testAddAllOnChildNodeList(Element element, Node ... childNodes) {
        /*
         * Before call "element.getAttributes().addAll(?),
         * (1) The "parentNode" of each node must be null
         * (2) The "index" of each node must be -1
         */
        for (Node childNode : childNodes) {
            Assert.assertEquals(null, childNode.getParentNode());
            Assert.assertEquals(-1, childNode.getIndex());
        }
        
        /*
         * This explicit modification will cause two implicit modifications automatically
         * (1) "childNode.parentNode" will be assigned automatically
         * (1) "childNode.index" will be assigned automatically
         */
        element.getChildNodes().addAll(MACollections.wrap(childNodes));
        
        /*
         * After call "element.getAttributes().addAll(?),
         * (1) "childNode.parentNode" is assigned automatically and implicitly
         * (2) "childNode.index" is assigned automatically and implicitly
         */
        int index = 0;
        for (Node childNode : childNodes) {
            Assert.assertSame(element, childNode.getParentNode());
            Assert.assertEquals(index++, childNode.getIndex());
        }
    }
    
    private static void testSetOwnerElementOnAttribute(
            String localName, 
            String value, 
            Element element) {
        testSetOwnerElementOnAttribute(null, localName, value, element);
    }
    
    private static void testSetOwnerElementOnAttribute(
            String namespaceURI, 
            String localName, 
            String value, 
            Element element) {
        
        Attribute attribute = new Attribute();
        attribute.setValue(value);
        
        /*
         * Before call "attribute.setOwnerElement(?)" and "attribute.setQuanifiedName(?)",
         * element does not contain this attribute.
         */
        Assert.assertFalse(element.getAttributes().containsKey(new QuanifiedName(namespaceURI, localName)));
        Assert.assertFalse(element.getAttributes().containsValue(attribute));
        
        /*
         * call "attribute.setOwnerElement(?)", but "attribute.setQuanifiedName(?)" is not called
         * so that element still does not contain this attribute
         */
        attribute.setOwnerElement(element);
        Assert.assertFalse(element.getAttributes().containsKey(new QuanifiedName(namespaceURI, localName)));
        Assert.assertFalse(element.getAttributes().containsValue(attribute));
        
        /*
         * call "attribute.setQuanifiedName(?)", now both the "ownerElement" and "quanifiedName"
         * of attribute have been set, so a key-value pair will be inserted into 
         * "element.getAttributes()" automatically and implicitly.
         */
        attribute.setQuanifiedName(new QuanifiedName(namespaceURI, localName));
        Assert.assertTrue(element.getAttributes().containsKey(new QuanifiedName(namespaceURI, localName)));
        Assert.assertTrue(element.getAttributes().containsValue(attribute));
    }
    
    private static void testSetParentNodeOnChildNode(Node childNode, int index, Element parentElement) {
        
        /*
         * Before call "childNode.setParentNode(?)" and "childNode.setIndex(?)",
         * parentElement does not contain this childNode
         */
        Assert.assertFalse(parentElement.getChildNodes().contains(childNode));
        
        /*
         * call "childNode.setParentNode(?)", but "childNode.setIndex(?)" is not called
         * so that parentElement still does not contain this childNode
         */
        childNode.setParentNode(parentElement);
        Assert.assertFalse(parentElement.getChildNodes().contains(childNode));
        
        /*
         * call "childNode.setIndex(?)", now both the "parentNode" and "index"
         * of childNode have been set, so the childNode will be inserted into 
         * "parentElement.getChildNodes()" automatically and implicitly.
         */
        childNode.setIndex(index);
        Assert.assertTrue(parentElement.getChildNodes().contains(childNode));
    }
}
