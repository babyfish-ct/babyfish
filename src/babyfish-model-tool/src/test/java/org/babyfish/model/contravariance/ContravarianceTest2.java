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
package org.babyfish.model.contravariance;

import org.babyfish.collection.MACollections;
import org.babyfish.model.contravariance.entities.CloseableTabControl;
import org.babyfish.model.contravariance.entities.CloseableTabPage;
import org.babyfish.model.contravariance.entities.Component;
import org.babyfish.model.contravariance.entities.Container;
import org.babyfish.model.contravariance.entities.TabPage;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ContravarianceTest2 {
    
    @Test(expected = IllegalArgumentException.class)
    public void testCovarianceListFailed() {
        CloseableTabControl tabControl = new CloseableTabControl();
        tabControl.getComponents().add(new TabPage());
    }
    
    @Test
    public void testCovarianceListSucessed() {
    
        CloseableTabControl tabControl = new CloseableTabControl();
        CloseableTabPage closeableTabPage1 = new CloseableTabPage();
        CloseableTabPage closeableTabPage2 = new CloseableTabPage();
        CloseableTabPage closeableTabPage3 = new CloseableTabPage();
        CloseableTabPage closeableTabPage4 = new CloseableTabPage();
        
        assertContainer(tabControl);
        assertCloseableTabControl(tabControl);
        
        tabControl.getComponents().addAll(MACollections.wrap(closeableTabPage1, closeableTabPage2, closeableTabPage3, closeableTabPage4));
        
        assertContainer(tabControl, closeableTabPage1, closeableTabPage2, closeableTabPage3, closeableTabPage4);
        assertCloseableTabControl(tabControl, closeableTabPage1, closeableTabPage2, closeableTabPage3, closeableTabPage4);
    }
    
    @Test
    public void testContravarianceList() {
    
        CloseableTabControl closeableTabControl = new CloseableTabControl();
        CloseableTabPage closeableTabPage1 = new CloseableTabPage();
        CloseableTabPage closeableTabPage2 = new CloseableTabPage();
        CloseableTabPage closeableTabPage3 = new CloseableTabPage();
        CloseableTabPage closeableTabPage4 = new CloseableTabPage();
        
        assertCloseableTabControl(closeableTabControl);
        assertContainer(closeableTabControl);
        
        closeableTabControl.getCloseableTabPages().addAll(MACollections.wrap(closeableTabPage1, closeableTabPage2, closeableTabPage3, closeableTabPage4));
        
        assertCloseableTabControl(closeableTabControl, closeableTabPage1, closeableTabPage2, closeableTabPage3, closeableTabPage4);
        assertContainer(closeableTabControl, closeableTabPage1, closeableTabPage2, closeableTabPage3, closeableTabPage4);
    }
 
    private static void assertContainer(Container container, Component ... components) {
        Assert.assertEquals(components.length, container.getComponents().size());
        int index = 0;
        for (Component component : container.getComponents()) {
            Assert.assertSame(components[index++], component);
        }
    }
    
    private static void assertCloseableTabControl(CloseableTabControl closeableTabControl, CloseableTabPage ... closeableTabPages) {
        Assert.assertEquals(closeableTabPages.length, closeableTabControl.getCloseableTabPages().size());
        int index = 0;
        for (TabPage tabPage : closeableTabControl.getTabPages()) {
            Assert.assertSame(closeableTabPages[index++], tabPage);
        }
    }
}
