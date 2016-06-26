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

import junit.framework.Assert;

import org.babyfishdemo.om4java.dom.common.XPath;
import org.babyfishdemo.om4java.dom.common.XmlBuilder;
import org.babyfishdemo.om4java.dom.visitor.Visitor;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ChangingAttributeByIteratorTest {
    
    private static final XmlBuilder XML_BUILDER = new XmlBuilder().readOnly();
    
    private static final XPath XPATH = new XPath().readOnly();

    private Element html;
    
    @Before
    public void create() {
        this.html = new Element(
                "html",
                new Element(
                        "head",
                        new Element(
                                "link",
                                new Attribute("rel", "stylesheet"),
                                new Attribute("type", "text/css"),
                                new Attribute("href", "default_theme.css")
                        ),
                        new Element(
                                "script",
                                new Attribute("type", "text/javascript"),
                                new Attribute("src", "angular.min.js"),
                                new Text("")
                        )
                ),
                new Element(
                        "body",
                        new Attribute("ng-app", ""),
                        new Attribute("ng-controller", "simpleDemoCtrl"),
                        new Element(
                                "p",
                                new Attribute("data-a", "1"),
                                new Attribute("data-b", "4"),
                                new Attribute("data-c", "9"),
                                new Attribute("data-d", "16"),
                                new Attribute("data-e", "25"),
                                new Attribute("data-f", "36"),
                                new Attribute("data-g", "49"),
                                new Element(
                                        "input",
                                        new Attribute("type", "text"),
                                        new Attribute("class", "wide-input"),
                                        new Attribute("ng-model", "keyword")
                                )
                        ),
                        new Element(
                                "div",
                                new Attribute("class", "simple-list"),
                                new Element(
                                        "div", 
                                        new Attribute("ng-repeat", "item in items()"),
                                        new Element(
                                                "span",
                                                new Attribute("ng-bind", "item.name"),
                                                new Text("")
                                        )
                                )
                        )
                )
        );
        Assert.assertEquals(
                "<html>"
                +   "<head>"
                +     "<link rel='stylesheet' type='text/css' href='default_theme.css'/>"
                +     "<script type='text/javascript' src='angular.min.js'></script>"
                +   "</head>"
                +   "<body ng-app='' ng-controller='simpleDemoCtrl'>"
                +     "<p data-a='1' data-b='4' data-c='9' data-d='16' data-e='25' data-f='36' data-g='49'>"
                +       "<input type='text' class='wide-input' ng-model='keyword'/>"
                +     "</p>"
                +     "<div class='simple-list'>"
                +       "<div ng-repeat='item in items()'>"
                +         "<span ng-bind='item.name'></span>"
                +       "</div>"
                +     "</div>"
                +   "</body>"
                + "</html>", 
                XML_BUILDER.build(this.html)
        );
    }
    
    @Test
    public void testRemoveAttributesByMapKeySetViewIterator() {
        
        Element body = XPATH.selectSingleNode(this.html, "body");
        Attribute ngApp = XPATH.selectSingleNode(body, "@ng-app");
        Attribute ngController = XPATH.selectSingleNode(body, "@ng-controller");
        Assert.assertTrue(body.getAttributes().containsValue(ngApp));
        Assert.assertSame(body, ngApp.getOwnerElement());
        Assert.assertTrue(body.getAttributes().containsValue(ngController));
        Assert.assertSame(body, ngController.getOwnerElement());
        
        Element input = XPATH.selectSingleNode(body, "p/input");
        Attribute ngModel = XPATH.selectSingleNode(input, "@ng-model");
        Assert.assertTrue(input.getAttributes().containsValue(ngModel));
        Assert.assertSame(input, ngModel.getOwnerElement());
        
        Element div = XPATH.selectSingleNode(body, "div/div");
        Attribute ngRepeat = XPATH.selectSingleNode(div, "@ng-repeat");
        Assert.assertTrue(div.getAttributes().containsValue(ngRepeat));
        Assert.assertSame(div, ngRepeat.getOwnerElement());
        
        Element span = XPATH.selectSingleNode(div, "span");
        Attribute ngBind = XPATH.selectSingleNode(span, "@ng-bind");
        Assert.assertTrue(span.getAttributes().containsValue(ngBind));
        Assert.assertSame(span, ngBind.getOwnerElement());
        
        this.html.accept(new Visitor() {

            @Override
            public void visitElement(Element element) {
                /*
                 * If we remove the attribute whose localName start with 
                 * "ng-" in the method "visitAttribute", we will get
                 * "java.util.ConcurrentModificationException"!
                 * 
                 * But, this method is "visitElement", we can do it safely here:)
                 */
                Iterator<QuanifiedName> keyItr = element.getAttributes().keySet().iterator();
                while (keyItr.hasNext()) {
                    QuanifiedName attributeName = keyItr.next();
                    if (attributeName.getLocalName().startsWith("ng-")) {
                        keyItr.remove();
                    }
                }
            }
        });
        
        /*
         * The bidirectional associations between the "ng-" attributes and
         * their owner elements are destroyed automatically and implicitly
         */
        Assert.assertFalse(body.getAttributes().containsValue(ngApp)); // Changed by you
        Assert.assertNull(ngApp.getOwnerElement()); // Set to be null automatically and implicitly
        Assert.assertFalse(body.getAttributes().containsValue(ngController));
        Assert.assertNull(ngController.getOwnerElement());
        
        Assert.assertFalse(input.getAttributes().containsValue(ngModel)); // Changed by you
        Assert.assertNull(ngModel.getOwnerElement()); // Set to be null automatically and implicitly
        
        Assert.assertFalse(div.getAttributes().containsValue(ngRepeat)); // Changed by you
        Assert.assertNull(ngRepeat.getOwnerElement()); // Set to be null automatically and implicitly
        
        Assert.assertFalse(span.getAttributes().containsValue(ngBind)); // Changed by you
        Assert.assertNull(ngBind.getOwnerElement()); // Set to be null automatically and implicitly
        
        /*
         * Now the whole html is(all the attributes start with "ng-" are removed)
         */
        Assert.assertEquals(
                "<html>"
                +   "<head>"
                +     "<link rel='stylesheet' type='text/css' href='default_theme.css'/>"
                +     "<script type='text/javascript' src='angular.min.js'></script>"
                +   "</head>"
                +   "<body>"
                +     "<p data-a='1' data-b='4' data-c='9' data-d='16' data-e='25' data-f='36' data-g='49'>"
                +       "<input type='text' class='wide-input'/>"
                +     "</p>"
                +     "<div class='simple-list'>"
                +       "<div>"
                +         "<span></span>"
                +       "</div>"
                +     "</div>"
                +   "</body>"
                + "</html>", 
                XML_BUILDER.build(this.html)
        );
    }
    
    @Test
    public void testRemoveAttributesByMapValuesViewIterator() {
        
        Element p = XPATH.selectSingleNode(this.html, "body/p");
        Attribute dataB = XPATH.selectSingleNode(p, "@data-b");
        Attribute dataD = XPATH.selectSingleNode(p, "@data-d");
        Attribute dataF = XPATH.selectSingleNode(p, "@data-f");
        Assert.assertTrue(p.getAttributes().containsValue(dataB));
        Assert.assertSame(p, dataB.getOwnerElement());
        Assert.assertTrue(p.getAttributes().containsValue(dataD));
        Assert.assertSame(p, dataD.getOwnerElement());
        Assert.assertTrue(p.getAttributes().containsValue(dataF));
        Assert.assertSame(p, dataF.getOwnerElement());
        
        this.html.accept(new Visitor() {

            @Override
            public void visitElement(Element element) {
                /*
                 * If we remove the attribute whose localName start with 
                 * "ng-" in the method "visitAttribute", we will get
                 * "java.util.ConcurrentModificationException"!
                 * 
                 * But, this method is "visitElement", we can do it safely here :)
                 */
                Iterator<Attribute> valueItr = element.getAttributes().values().iterator();
                while (valueItr.hasNext()) {
                    Attribute attribute = valueItr.next();
                    if (attribute.getQuanifiedName().getLocalName().startsWith("data-")) {
                        int number;
                        try {
                            number = Integer.parseInt(attribute.getValue());
                        } catch (NumberFormatException ex) {
                            continue;
                        }
                        if (number % 2 == 0) {
                            /*
                             * If the value of the current attribute is even number, remove it.
                             * so three attributes will be removed: data-b='4' data-d='16' data-f='36'
                             */
                            valueItr.remove(); 
                        }
                    }
                }
            }
        });
        
        /*
         * The bidirectional associations between the "data-b", "data-d", "data-f"
         * and their owner elements are destroyed automatically and implicitly
         */
        Assert.assertFalse(p.getAttributes().containsValue(dataB)); // Changed by you
        Assert.assertNull(dataB.getOwnerElement()); // Set to be null automatically and implicitly
        Assert.assertFalse(p.getAttributes().containsValue(dataD)); // Changed by you
        Assert.assertNull(dataD.getOwnerElement()); // Set to be null automatically and implicitly
        Assert.assertFalse(p.getAttributes().containsValue(dataF)); // Changed by you
        Assert.assertNull(dataF.getOwnerElement()); // Set to be null automatically and implicitly
        
        /*
         * Now the whole html is(data-b, data-d, data-f are removed)
         */
        Assert.assertEquals(
                "<html>"
                +   "<head>"
                +     "<link rel='stylesheet' type='text/css' href='default_theme.css'/>"
                +     "<script type='text/javascript' src='angular.min.js'></script>"
                +   "</head>"
                +   "<body ng-app='' ng-controller='simpleDemoCtrl'>"
                +     "<p data-a='1' data-c='9' data-e='25' data-g='49'>"
                +       "<input type='text' class='wide-input' ng-model='keyword'/>"
                +     "</p>"
                +     "<div class='simple-list'>"
                +       "<div ng-repeat='item in items()'>"
                +         "<span ng-bind='item.name'></span>"
                +       "</div>"
                +     "</div>"
                +   "</body>"
                + "</html>", 
                XML_BUILDER.build(this.html)
        );
    }
    
    /*
     * Of course, there are many other solutions to modify the attributes
     * except using
     * "Element.getAttributes().keySet().iterator()" 
     * and
     * "Element.getAttributes().values().iterator()"
     * like this demo.
     * 
     * But most of those other methods are shown in 
     * "MovingChildBetweenDiffrentParentsTest" and "UnstableMapKeyTest",
     * so it's unnecessary to show all of them again in this test class,
     * please view other test classes to know more.
     */
}
