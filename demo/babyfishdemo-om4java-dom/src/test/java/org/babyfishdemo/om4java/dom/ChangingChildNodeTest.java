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

import java.util.Iterator;

import org.babyfish.collection.MACollections;
import org.babyfishdemo.om4java.dom.common.XPath;
import org.babyfishdemo.om4java.dom.common.XmlBuilder;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class ChangingChildNodeTest {
    
    private static final String NS_W3C_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    
    private static final String NS_SPRING_BEANS = "http://www.springframework.org/schema/beans";
    
    private static final String NS_ALIBABA_DUBBO = "http://code.alibabatech.com/schema/dubbo";
    
    private static final XmlBuilder XML_BUILDER =
            new XmlBuilder()
            .addPrefix("", NS_SPRING_BEANS)
            .addPrefix("dubbo", NS_ALIBABA_DUBBO)
            .addPrefix("xsi", NS_W3C_XSI)
            .readOnly();
    
    private static final XPath XPATH =
            new XPath()
            .addPrefix("", NS_SPRING_BEANS)
            .addPrefix("dubbo", NS_ALIBABA_DUBBO)
            .addPrefix("xsi", NS_W3C_XSI)
            .readOnly();
    
    private Element beans;
    
    private Comment applicationComment;
    
    private Element application;
    
    private Comment registryComment;
    
    private Element registry;
    
    private Comment protocolComment;
    
    private Element protocol;
    
    private Comment serviceComment;
    
    private Element service;
    
    private Comment beanComment;
    
    private Element bean;
    
    @Before
    public void create() {
        this.beans = new Element(
                NS_SPRING_BEANS, 
                "beans",
                new Attribute(
                        NS_W3C_XSI,
                        "schemaLocation", 
                        NS_SPRING_BEANS
                        + " http://www.springframework.org/schema/beans/spring-beans.xsd "
                        + NS_ALIBABA_DUBBO
                        + " http://code.alibabatech.com/schema/dubbo/dubbo.xsd"
                ),
                new Comment("Provide application information to calculate dependencies"),
                new Element(
                        NS_ALIBABA_DUBBO,
                        "application",
                        new Attribute("name", "hello-world-app")    
                ),
                new Comment("Use Multicast broadcast registry center to expose service address"),
                new Element(
                        NS_ALIBABA_DUBBO,
                        "registry",
                        new Attribute("address", "multicast://224.5.6.7:1234")  
                ),
                new Comment("Apply dubbo protocol on port 20880"),
                new Element(
                        NS_ALIBABA_DUBBO,
                        "protocol",
                        new Attribute("name", "dubbo"),
                        new Attribute("port", "20880")
                ),
                new Comment("Declare the interface of service"),
                new Element(
                        NS_ALIBABA_DUBBO,
                        "service",
                        new Attribute("interface", "com.alibaba.dubbo.demo.DemoService"),
                        new Attribute("ref", "demoService")
                ),
                new Comment("Implement the service as local bean"),
                new Element(
                        NS_SPRING_BEANS,
                        "bean",
                        new Attribute("id", "demoService"),
                        new Attribute("class", "com.alibaba.dubbo.demo.provider.DemoServiceImpl")
                )
        );
        this.application = XPATH.selectSingleNode(this.beans, "dubbo:application");
        this.applicationComment = (Comment)this.application.getPreviousSibling();
        this.registry = XPATH.selectSingleNode(this.beans, "dubbo:registry");
        this.registryComment = (Comment)this.registry.getPreviousSibling();
        this.protocol = XPATH.selectSingleNode(this.beans, "dubbo:protocol");
        this.protocolComment = (Comment)this.protocol.getPreviousSibling();
        this.service = XPATH.selectSingleNode(this.beans, "dubbo:service");
        this.serviceComment = (Comment)this.service.getPreviousSibling();
        this.bean = XPATH.selectSingleNode(this.beans, "bean");
        this.beanComment = (Comment)this.bean.getPreviousSibling();
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
                XML_BUILDER.build(this.beans)
        );
        
        Assert.assertEquals(0, this.applicationComment.getIndex());
        Assert.assertEquals(1, this.application.getIndex());
        Assert.assertEquals(2, this.registryComment.getIndex());
        Assert.assertEquals(3, this.registry.getIndex());
        Assert.assertEquals(4, this.protocolComment.getIndex());
        Assert.assertEquals(5, this.protocol.getIndex());
        Assert.assertEquals(6, this.serviceComment.getIndex());
        Assert.assertEquals(7, this.service.getIndex());
        Assert.assertEquals(8, this.beanComment.getIndex());
        Assert.assertEquals(9, this.bean.getIndex());
        
        Assert.assertSame(this.beans, this.applicationComment.getParentNode());
        Assert.assertSame(this.beans, this.application.getParentNode());
        Assert.assertSame(this.beans, this.registryComment.getParentNode());
        Assert.assertSame(this.beans, this.registry.getParentNode());
        Assert.assertSame(this.beans, this.protocolComment.getParentNode());
        Assert.assertSame(this.beans, this.protocol.getParentNode());
        Assert.assertSame(this.beans, this.serviceComment.getParentNode());
        Assert.assertSame(this.beans, this.service.getParentNode());
        Assert.assertSame(this.beans, this.beanComment.getParentNode());
        Assert.assertSame(this.beans, this.bean.getParentNode());
    }

    @Test
    public void testChangeChildNodeOrderByParent() {
        /*
         * Add the registryComment and registry AGAIN at index 0,
         * they will be moved to the the front of the "beans.getChildNodes()".
         * 
         * The indexes of applicationComment, application, registryComment and
         * registry will be changed automatically and implicitly!
         */
        this.beans.getChildNodes().addAll(0, MACollections.wrap(this.registryComment, this.registry));
        
        Assert.assertEquals(
                "<beans "
                + "xmlns='http://www.springframework.org/schema/beans' "
                + "xmlns:dubbo='http://code.alibabatech.com/schema/dubbo' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://code.alibabatech.com/schema/dubbo "
                + "http://code.alibabatech.com/schema/dubbo/dubbo.xsd'>"
                
                +     "<!--Use Multicast broadcast registry center to expose service address-->"
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                
                +     "<!--Provide application information to calculate dependencies-->"
                +     "<dubbo:application name='hello-world-app'/>"
                
                +     "<!--Apply dubbo protocol on port 20880-->"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                
                +     "<!--Declare the interface of service-->"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                + "</beans>",
                XML_BUILDER.build(beans)
        );
        Assert.assertEquals(2, this.applicationComment.getIndex()); // index changed automatically
        Assert.assertEquals(3, this.application.getIndex()); // index changed automatically
        Assert.assertEquals(0, this.registryComment.getIndex()); // index changed automatically
        Assert.assertEquals(1, this.registry.getIndex()); // index changed automatically
        Assert.assertEquals(4, this.protocolComment.getIndex());
        Assert.assertEquals(5, this.protocol.getIndex());
        Assert.assertEquals(6, this.serviceComment.getIndex());
        Assert.assertEquals(7, this.service.getIndex());
        Assert.assertEquals(8, this.beanComment.getIndex());
        Assert.assertEquals(9, this.bean.getIndex());
        
        /*
         * Add the protocolComment and protocol AGAIN at index 0,
         * they will be moved to the the front of the "beans.getChildNodes()".
         * 
         * The indexes of applicationComment, application, registryComment,
         * registry, protocolComment and protocol will be changed 
         * automatically and implicitly!
         */
        this.beans.getChildNodes().addAll(0, MACollections.wrap(this.protocolComment, this.protocol));
        
        Assert.assertEquals(
                "<beans "
                + "xmlns='http://www.springframework.org/schema/beans' "
                + "xmlns:dubbo='http://code.alibabatech.com/schema/dubbo' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://code.alibabatech.com/schema/dubbo "
                + "http://code.alibabatech.com/schema/dubbo/dubbo.xsd'>"
                
                +     "<!--Apply dubbo protocol on port 20880-->"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                
                +     "<!--Use Multicast broadcast registry center to expose service address-->"
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                
                +     "<!--Provide application information to calculate dependencies-->"
                +     "<dubbo:application name='hello-world-app'/>"
                
                +     "<!--Declare the interface of service-->"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                + "</beans>",
                XML_BUILDER.build(this.beans)
        );
        Assert.assertEquals(4, this.applicationComment.getIndex()); // index changed automatically
        Assert.assertEquals(5, this.application.getIndex()); // index changed automatically
        Assert.assertEquals(2, this.registryComment.getIndex()); // index changed automatically
        Assert.assertEquals(3, this.registry.getIndex()); // index changed automatically
        Assert.assertEquals(0, this.protocolComment.getIndex()); // index changed automatically
        Assert.assertEquals(1, this.protocol.getIndex()); // index changed automatically
        Assert.assertEquals(6, this.serviceComment.getIndex());
        Assert.assertEquals(7, this.service.getIndex());
        Assert.assertEquals(8, this.beanComment.getIndex());
        Assert.assertEquals(9, this.bean.getIndex());
        
        /*
         * Add the serviceComment and service AGAIN at index 0,
         * they will be moved to the the front of the "beans.getChildNodes()".
         * 
         * The indexes of applicationComment, application, registryComment,
         * registry, protocolComment, protocol, serviceComment and service
         * will be changed automatically and implicitly!
         */
        this.beans.getChildNodes().addAll(0, MACollections.wrap(this.serviceComment, this.service));
        
        Assert.assertEquals(
                "<beans "
                + "xmlns='http://www.springframework.org/schema/beans' "
                + "xmlns:dubbo='http://code.alibabatech.com/schema/dubbo' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://code.alibabatech.com/schema/dubbo "
                + "http://code.alibabatech.com/schema/dubbo/dubbo.xsd'>"
                
                +     "<!--Declare the interface of service-->"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Apply dubbo protocol on port 20880-->"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                
                +     "<!--Use Multicast broadcast registry center to expose service address-->"
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                
                +     "<!--Provide application information to calculate dependencies-->"
                +     "<dubbo:application name='hello-world-app'/>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                + "</beans>",
                XML_BUILDER.build(beans)
        );
        Assert.assertEquals(6, this.applicationComment.getIndex()); // index changed automatically
        Assert.assertEquals(7, this.application.getIndex()); // index changed automatically
        Assert.assertEquals(4, this.registryComment.getIndex()); // index changed automatically
        Assert.assertEquals(5, this.registry.getIndex()); // index changed automatically
        Assert.assertEquals(2, this.protocolComment.getIndex()); // index changed automatically
        Assert.assertEquals(3, this.protocol.getIndex()); // index changed automatically
        Assert.assertEquals(0, this.serviceComment.getIndex()); // index changed automatically
        Assert.assertEquals(1, this.service.getIndex()); // index changed automatically
        Assert.assertEquals(8, this.beanComment.getIndex());
        Assert.assertEquals(9, this.bean.getIndex());
        
        /*
         * Add the beanComment and bean AGAIN at index 0,
         * they will be moved to the the front of the "beans.getChildNodes()".
         * 
         * The indexes of applicationComment, application, registryComment,
         * registry, protocolComment, protocol, serviceComment, service
         * beanComment and bean will be changed automatically and implicitly!
         */
        this.beans.getChildNodes().addAll(0, MACollections.wrap(this.beanComment, this.bean));
        
        Assert.assertEquals(
                "<beans "
                + "xmlns='http://www.springframework.org/schema/beans' "
                + "xmlns:dubbo='http://code.alibabatech.com/schema/dubbo' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://code.alibabatech.com/schema/dubbo "
                + "http://code.alibabatech.com/schema/dubbo/dubbo.xsd'>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                
                +     "<!--Declare the interface of service-->"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Apply dubbo protocol on port 20880-->"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                
                +     "<!--Use Multicast broadcast registry center to expose service address-->"
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                
                +     "<!--Provide application information to calculate dependencies-->"
                +     "<dubbo:application name='hello-world-app'/>"
                
                + "</beans>",
                XML_BUILDER.build(this.beans)
        );
        Assert.assertEquals(8, this.applicationComment.getIndex()); // index changed automatically
        Assert.assertEquals(9, this.application.getIndex()); // index changed automatically
        Assert.assertEquals(6, this.registryComment.getIndex()); // index changed automatically
        Assert.assertEquals(7, this.registry.getIndex()); // index changed automatically
        Assert.assertEquals(4, this.protocolComment.getIndex()); // index changed automatically
        Assert.assertEquals(5, this.protocol.getIndex()); // index changed automatically
        Assert.assertEquals(2, this.serviceComment.getIndex()); // index changed automatically
        Assert.assertEquals(3, this.service.getIndex()); // index changed automatically
        Assert.assertEquals(0, this.beanComment.getIndex()); // index changed automatically
        Assert.assertEquals(1, this.bean.getIndex()); // index changed automatically
    }
    
    @Test
    public void testChangeChildNodeOrderByChild() {
        /*
         * Change the index of registryComment and registry.
         * 
         * (1) The "beans.getChildNodes()" will be changed automatically and
         * implicitly! So the element order is changed.
         * (2) The indexes of applicationComment and application will be 
         * changed automatically and implicitly!
         */
        this.registryComment.setIndex(0);
        this.registry.setIndex(1);
        
        Assert.assertEquals(
                "<beans "
                + "xmlns='http://www.springframework.org/schema/beans' "
                + "xmlns:dubbo='http://code.alibabatech.com/schema/dubbo' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://code.alibabatech.com/schema/dubbo "
                + "http://code.alibabatech.com/schema/dubbo/dubbo.xsd'>"
                
                +     "<!--Use Multicast broadcast registry center to expose service address-->"
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                
                +     "<!--Provide application information to calculate dependencies-->"
                +     "<dubbo:application name='hello-world-app'/>"
                
                +     "<!--Apply dubbo protocol on port 20880-->"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                
                +     "<!--Declare the interface of service-->"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                + "</beans>",
                XML_BUILDER.build(beans)
        ); // The elements order of "beans.getChildNodes()" is changed automatically and implicitly.
        Assert.assertEquals(2, this.applicationComment.getIndex()); // index changed automatically
        Assert.assertEquals(3, this.application.getIndex()); // index changed automatically
        Assert.assertEquals(0, this.registryComment.getIndex()); // You changed it
        Assert.assertEquals(1, this.registry.getIndex()); // You changed it
        Assert.assertEquals(4, this.protocolComment.getIndex());
        Assert.assertEquals(5, this.protocol.getIndex());
        Assert.assertEquals(6, this.serviceComment.getIndex());
        Assert.assertEquals(7, this.service.getIndex());
        Assert.assertEquals(8, this.beanComment.getIndex());
        Assert.assertEquals(9, this.bean.getIndex());
        
        /*
         * Change the index of protocolComment and protocol.
         * 
         * (1) The "beans.getChildNodes()" will be changed automatically and
         * implicitly! So the element order is changed.
         * (2) The indexes of applicationComment, application, registryComment
         * and registry will be changed automatically and implicitly!
         */
        this.protocolComment.setIndex(0);
        this.protocol.setIndex(1);
        
        Assert.assertEquals(
                "<beans "
                + "xmlns='http://www.springframework.org/schema/beans' "
                + "xmlns:dubbo='http://code.alibabatech.com/schema/dubbo' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://code.alibabatech.com/schema/dubbo "
                + "http://code.alibabatech.com/schema/dubbo/dubbo.xsd'>"
                
                +     "<!--Apply dubbo protocol on port 20880-->"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                
                +     "<!--Use Multicast broadcast registry center to expose service address-->"
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                
                +     "<!--Provide application information to calculate dependencies-->"
                +     "<dubbo:application name='hello-world-app'/>"
                
                +     "<!--Declare the interface of service-->"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                + "</beans>",
                XML_BUILDER.build(this.beans)
        ); // The elements order of "beans.getChildNodes()" is changed automatically and implicitly.
        Assert.assertEquals(4, this.applicationComment.getIndex()); // index changed automatically
        Assert.assertEquals(5, this.application.getIndex()); // index changed automatically
        Assert.assertEquals(2, this.registryComment.getIndex()); // index changed automatically
        Assert.assertEquals(3, this.registry.getIndex()); // index changed automatically
        Assert.assertEquals(0, this.protocolComment.getIndex()); // You changed it
        Assert.assertEquals(1, this.protocol.getIndex()); // You changed it
        Assert.assertEquals(6, this.serviceComment.getIndex());
        Assert.assertEquals(7, this.service.getIndex());
        Assert.assertEquals(8, this.beanComment.getIndex());
        Assert.assertEquals(9, this.bean.getIndex());
        
        /*
         * Change the index of serviceComment and service.
         * 
         * (1) The "beans.getChildNodes()" will be changed automatically and
         * implicitly! So the element order is changed.
         * (2) The indexes of applicationComment, application, registryComment,
         * registry, protocolComment and protocol will be changed automatically 
         * and implicitly!
         */
        this.serviceComment.setIndex(0);
        this.service.setIndex(1);
        
        Assert.assertEquals(
                "<beans "
                + "xmlns='http://www.springframework.org/schema/beans' "
                + "xmlns:dubbo='http://code.alibabatech.com/schema/dubbo' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://code.alibabatech.com/schema/dubbo "
                + "http://code.alibabatech.com/schema/dubbo/dubbo.xsd'>"
                
                +     "<!--Declare the interface of service-->"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Apply dubbo protocol on port 20880-->"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                
                +     "<!--Use Multicast broadcast registry center to expose service address-->"
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                
                +     "<!--Provide application information to calculate dependencies-->"
                +     "<dubbo:application name='hello-world-app'/>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                + "</beans>",
                XML_BUILDER.build(beans)
        ); // The elements order of "beans.getChildNodes()" is changed automatically and implicitly.
        Assert.assertEquals(6, this.applicationComment.getIndex()); // index changed automatically
        Assert.assertEquals(7, this.application.getIndex()); // index changed automatically
        Assert.assertEquals(4, this.registryComment.getIndex()); // index changed automatically
        Assert.assertEquals(5, this.registry.getIndex()); // index changed automatically
        Assert.assertEquals(2, this.protocolComment.getIndex()); // index changed automatically
        Assert.assertEquals(3, this.protocol.getIndex()); // index changed automatically
        Assert.assertEquals(0, this.serviceComment.getIndex()); // You changed it
        Assert.assertEquals(1, this.service.getIndex()); // You changed it
        Assert.assertEquals(8, this.beanComment.getIndex());
        Assert.assertEquals(9, this.bean.getIndex());
        
        /*
         * Change the index of beanComment and bean.
         * 
         * (1) The "beans.getChildNodes()" will be changed automatically and
         * implicitly! So the element order is changed.
         * (2) The indexes of applicationComment, application, registryComment,
         * registry, protocolComment, protocol, serviceComemnt and service 
         * will be changed automatically and implicitly!
         */
        this.beanComment.setIndex(0);
        this.bean.setIndex(1);
        
        Assert.assertEquals(
                "<beans "
                + "xmlns='http://www.springframework.org/schema/beans' "
                + "xmlns:dubbo='http://code.alibabatech.com/schema/dubbo' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://code.alibabatech.com/schema/dubbo "
                + "http://code.alibabatech.com/schema/dubbo/dubbo.xsd'>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                
                +     "<!--Declare the interface of service-->"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Apply dubbo protocol on port 20880-->"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                
                +     "<!--Use Multicast broadcast registry center to expose service address-->"
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                
                +     "<!--Provide application information to calculate dependencies-->"
                +     "<dubbo:application name='hello-world-app'/>"
                
                + "</beans>",
                XML_BUILDER.build(this.beans)
        ); // The elements order of "beans.getChildNodes()" is changed automatically and implicitly.
        Assert.assertEquals(8, this.applicationComment.getIndex()); // index changed automatically
        Assert.assertEquals(9, this.application.getIndex()); // index changed automatically
        Assert.assertEquals(6, this.registryComment.getIndex()); // index changed automatically
        Assert.assertEquals(7, this.registry.getIndex()); // index changed automatically
        Assert.assertEquals(4, this.protocolComment.getIndex()); // index changed automatically
        Assert.assertEquals(5, this.protocol.getIndex()); // index changed automatically
        Assert.assertEquals(2, this.serviceComment.getIndex()); // index changed automatically
        Assert.assertEquals(3, this.service.getIndex()); // index changed automatically
        Assert.assertEquals(0, this.beanComment.getIndex()); // You changed it
        Assert.assertEquals(1, this.bean.getIndex()); // You changed it
    }
    
    @Test
    public void removeChildByParentIterator() {
        
        /*
         * User iterator to find and remove all the Comment Nodes
         * This will cause
         * (1) The indexes of all the childNodes will be changed
         * (2) The parentNode of all the Comment Nodes will be changed
         */
        Iterator<Node> itr = this.beans.getChildNodes().iterator();
        while (itr.hasNext()) {
            Node node = itr.next();
            if (node instanceof Comment) {
                itr.remove();
            }
        }
        
        Assert.assertEquals(
                "<beans "
                + "xmlns='http://www.springframework.org/schema/beans' "
                + "xmlns:dubbo='http://code.alibabatech.com/schema/dubbo' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://code.alibabatech.com/schema/dubbo "
                + "http://code.alibabatech.com/schema/dubbo/dubbo.xsd'>"
                +     "<dubbo:application name='hello-world-app'/>"
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                + "</beans>",
                XML_BUILDER.build(this.beans)
        );
        
        /*
         * The index of all the childNodes have been changed.
         */
        Assert.assertEquals(-1, this.applicationComment.getIndex()); // index changed automatically
        Assert.assertEquals(0, this.application.getIndex()); // index changed automatically
        Assert.assertEquals(-1, this.registryComment.getIndex()); // index changed automatically
        Assert.assertEquals(1, this.registry.getIndex()); // index changed automatically
        Assert.assertEquals(-1, this.protocolComment.getIndex()); // index changed automatically
        Assert.assertEquals(2, this.protocol.getIndex()); // index changed automatically
        Assert.assertEquals(-1, this.serviceComment.getIndex()); // index changed automatically
        Assert.assertEquals(3, this.service.getIndex()); // index changed automatically
        Assert.assertEquals(-1, this.beanComment.getIndex()); // index changed automatically
        Assert.assertEquals(4, this.bean.getIndex()); // index changed automatically
        
        /*
         * The parentNode of all the Comment Nodes have been set to null
         */
        Assert.assertNull(this.applicationComment.getParentNode()); // parentNode is set to null automatically
        Assert.assertSame(this.beans, this.application.getParentNode());
        Assert.assertNull(this.registryComment.getParentNode()); // parentNode is set to null automatically
        Assert.assertSame(this.beans, this.registry.getParentNode());
        Assert.assertNull(this.protocolComment.getParentNode()); // parentNode is set to null automatically
        Assert.assertSame(this.beans, this.protocol.getParentNode());
        Assert.assertNull(this.serviceComment.getParentNode()); // parentNode is set to null automatically
        Assert.assertSame(this.beans, this.service.getParentNode());
        Assert.assertNull(this.beanComment.getParentNode()); // parentNode is set to null automatically
        Assert.assertSame(this.beans, this.bean.getParentNode());
    }
    
    @Test
    public void removeChildByParentSubListIterator() {
        
        /*
         * User iterator of sub to find and remove some the Comment Nodes
         * This will cause
         * (1) The indexes of registryComment, registry, protocolComment,
         * protocol, serviceComment and service will be changed
         * (2) The parentNode of registryComment, protocolComment and
         * serviceComment will be changed
         */
        Iterator<Node> itr = this.beans.getChildNodes().subList(2, 8).iterator();
        while (itr.hasNext()) {
            Node node = itr.next();
            if (node instanceof Comment) {
                itr.remove();
            }
        }
        
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
                
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                
                + "</beans>",
                XML_BUILDER.build(this.beans)
        );
        
        /*
         * The index of registryComment, registry, protocolComment,
         * protocol, serviceComment and service have been changed.
         */
        Assert.assertEquals(0, this.applicationComment.getIndex());
        Assert.assertEquals(1, this.application.getIndex());
        Assert.assertEquals(-1, this.registryComment.getIndex()); // index changed automatically
        Assert.assertEquals(2, this.registry.getIndex()); // index changed automatically
        Assert.assertEquals(-1, this.protocolComment.getIndex()); // index changed automatically
        Assert.assertEquals(3, this.protocol.getIndex()); // index changed automatically
        Assert.assertEquals(-1, this.serviceComment.getIndex()); // index changed automatically
        Assert.assertEquals(4, this.service.getIndex()); // index changed automatically
        Assert.assertEquals(5, this.beanComment.getIndex());
        Assert.assertEquals(6, this.bean.getIndex());
        
        /*
         * The parentNode of registryComment, protocolComment and serviceComment 
         * and service have been set to null
         */
        Assert.assertSame(this.beans, this.applicationComment.getParentNode()); 
        Assert.assertSame(this.beans, this.application.getParentNode());
        Assert.assertNull(this.registryComment.getParentNode()); // parentNode is set to null automatically
        Assert.assertSame(this.beans, this.registry.getParentNode());
        Assert.assertNull(this.protocolComment.getParentNode()); // parentNode is set to null automatically
        Assert.assertSame(this.beans, this.protocol.getParentNode());
        Assert.assertNull(this.serviceComment.getParentNode()); // parentNode is set to null automatically
        Assert.assertSame(this.beans, this.service.getParentNode());
        Assert.assertSame(this.beans, this.beanComment.getParentNode()); 
        Assert.assertSame(this.beans, this.bean.getParentNode());
    }
    
    @Test
    public void removeChildByChild() {
        
        /* 
         * Remove registryComment by setting its index to -1,
         * it will cause
         * (1) The "beans.getChildNodes()" will be changed automatically
         * and implicitly.
         * (2) The parentNode of registryComment will be HIDDEN(
         * private data is not changed, but getParent() returns null).
         * (3) The index of of other child objects such as registry, protocolComment, protocol, 
         * serviceComment, service, beanComment, bean will be changed
         * automatically and implicitly.
         */
        this.registryComment.setIndex(-1);
        
        /* 
         * Remove serviceComment by setting its parentNode to null,
         * it will cause
         * (1) The "beans.getChildNodes()" will be changed automatically
         * and implicitly.
         * (2) The index of serviceComment will be HIDDEN(
         * private data is not changed, but getParent() returns -1).
         * (3) The index of other child objects such as service, beanComment and bean will be changed
         * automatically and implicitly.
         */
        this.serviceComment.setParentNode(null);
        
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
                
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                
                +     "<!--Apply dubbo protocol on port 20880-->"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                + "</beans>",
                XML_BUILDER.build(this.beans)
        ); // The "beans.getChildNodes()" is changed automatically and implicitly
        
        Assert.assertEquals(0, this.applicationComment.getIndex());
        Assert.assertEquals(1, this.application.getIndex());
        Assert.assertEquals(-1, this.registryComment.getIndex()); // You changed it
        Assert.assertEquals(2, this.registry.getIndex()); // Index is changed automatically
        Assert.assertEquals(3, this.protocolComment.getIndex()); // Index is changed automatically
        Assert.assertEquals(4, this.protocol.getIndex()); // Index is changed automatically
        Assert.assertEquals(-1, this.serviceComment.getIndex()); // Index is changed automatically
        Assert.assertEquals(5, this.service.getIndex()); // Index is HIDDEN automatically
        Assert.assertEquals(6, this.beanComment.getIndex()); // Index is changed automatically
        Assert.assertEquals(7, this.bean.getIndex()); // Index is changed automatically
        
        Assert.assertSame(this.beans, this.applicationComment.getParentNode());
        Assert.assertSame(this.beans, this.application.getParentNode());
        Assert.assertNull(this.registryComment.getParentNode()); // ParentNode is HIDDEN automatically
        Assert.assertSame(this.beans, this.registry.getParentNode());
        Assert.assertSame(this.beans, this.protocolComment.getParentNode());
        Assert.assertSame(this.beans, this.protocol.getParentNode());
        Assert.assertNull(this.serviceComment.getParentNode()); //You changed it
        Assert.assertSame(this.beans, this.service.getParentNode());
        Assert.assertSame(this.beans, this.beanComment.getParentNode());
        Assert.assertSame(this.beans, this.bean.getParentNode());
    }
}
