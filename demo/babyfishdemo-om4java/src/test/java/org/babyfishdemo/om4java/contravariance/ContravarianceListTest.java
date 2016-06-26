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
package org.babyfishdemo.om4java.contravariance;

import org.babyfish.collection.MACollections;
import org.junit.Assert;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class ContravarianceListTest {
 
    @Test(expected = IllegalArgumentException.class)
    public void testCovarianceListFailed() {
        /*
         * The covariance property "components" requires the element must be instance of TabPage,
         * but here the argument is instance of Button, so the java.lang.IllegalArgumentException will be raised
         */
        new TabControl().getComponents().add(new Button());
    }
    
    @Test
    public void testCovarianceListSucessed() {
    
        TabControl tabControl = new TabControl();
        TabPage tabPage1 = new TabPage();
        TabPage tabPage2 = new TabPage();
        TabPage tabPage3 = new TabPage();
        TabPage tabPage4 = new TabPage();
        
        assertContainer(tabControl);
        assertTabControl(tabControl);
        
        /*
         * Modify the covariance property
         */
        tabControl.getComponents().addAll(MACollections.wrap(tabPage1, tabPage2, tabPage3, tabPage4));
        
        /*
         * The covariance property is modified
         */
        assertContainer(tabControl, tabPage1, tabPage2, tabPage3, tabPage4);
        
        /*
         * The contravariance property is modified too because it shares data with covariance property
         */
        assertTabControl(tabControl, tabPage1, tabPage2, tabPage3, tabPage4);
        
        /*
         * The opposite many-to-one assocation is modified automatically and impplicitly
         */
        Assert.assertSame(tabControl, tabPage1.getParent());
        Assert.assertSame(tabControl, tabPage2.getParent());
        Assert.assertSame(tabControl, tabPage3.getParent());
        Assert.assertSame(tabControl, tabPage4.getParent());
    }
    
    @Test
    public void testContravarianceList() {
    
        TabControl tabControl = new TabControl();
        TabPage tabPage1 = new TabPage();
        TabPage tabPage2 = new TabPage();
        TabPage tabPage3 = new TabPage();
        TabPage tabPage4 = new TabPage();
        
        assertContainer(tabControl);
        assertTabControl(tabControl);
        
        /*
         * Modify the contravariance property
         */
        tabControl.getTabPages().addAll(MACollections.wrap(tabPage1, tabPage2, tabPage3, tabPage4));
        
        /*
         * The contravariance property is modified
         */
        assertTabControl(tabControl, tabPage1, tabPage2, tabPage3, tabPage4);
        
        /*
         * The covariance property is modified too because it shares data with contravariance property
         */
        assertContainer(tabControl, tabPage1, tabPage2, tabPage3, tabPage4);
        
        /*
         * The opposite many-to-one assocation is modified automatically and impplicitly
         */
        Assert.assertSame(tabControl, tabPage1.getParent());
        Assert.assertSame(tabControl, tabPage2.getParent());
        Assert.assertSame(tabControl, tabPage3.getParent());
        Assert.assertSame(tabControl, tabPage4.getParent());
    }
 
    private static void assertContainer(Container container, Component ... components) {
        Assert.assertEquals(components.length, container.getComponents().size());
        int index = 0;
        for (Component component : container.getComponents()) {
            Assert.assertSame(components[index++], component);
        }
    }
    
    private static void assertTabControl(TabControl tabControl, TabPage ... tabPages) {
        Assert.assertEquals(tabPages.length, tabControl.getTabPages().size());
        int index = 0;
        for (TabPage tabPage : tabControl.getTabPages()) {
            Assert.assertSame(tabPages[index++], tabPage);
        }
    }
}
