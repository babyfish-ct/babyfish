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

import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelProvider;
import org.babyfish.model.spi.reference.Reference;
import org.junit.Assert;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class ContravarianceReferenceTest {
 
    private static final int COVARIANCE_PARENT_REFERENCE_ID;
 
    @Test(expected = IllegalArgumentException.class)
    public void testCovarianceReferenceFailed() {
        /*
         * The covariance property requires the element must be instance of TabControl,
         * but here the argument is instance of Container, so the java.lang.IllegalArgumentException will be raised
         */
        Reference<Container> covarianceReference = covarianceReference(new TabPage());
        covarianceReference.set(new Container());
    }
    
    @Test
    public void testCovarianceReferenceSucessed() {
        TabPage tabPage = new TabPage();
        TabControl tabControl = new TabControl();
        Reference<Container> covarianceReference = covarianceReference(tabPage);
        
        /*
         * Change the covariance property
         */
        covarianceReference.set(tabControl);
        
        /*
         * The covariance property has been modified
         */
        Assert.assertSame(tabControl, covarianceReference.get());
        
        /*
         * The contravariance property has been modifed too because it shares data with covariance property
         */
        Assert.assertSame(tabControl, tabPage.getParent());
        
        /*
         * The opposite one-to-many assocation is modified automatically and impplicitly
         */
        assertTabControl(tabControl, tabPage);
    }
    
    @Test
    public void testContravarianceReference() {
        TabPage tabPage = new TabPage();
        TabControl tabControl = new TabControl();
        Reference<Container> covarianceReference = covarianceReference(tabPage);
        
        /*
         * Change the covariance property
         */
        covarianceReference.set(tabControl);
        
        /*
         * The contravariance property has been modifed
         */
        Assert.assertSame(tabControl, tabPage.getParent());
        
        /*
         * The covariance property has been modified too because it shares data with contravariance property
         */
        Assert.assertSame(tabControl, covarianceReference.get());
        
        /*
         * The opposite one-to-many assocation is modified automatically and impplicitly
         */
        assertTabControl(tabControl, tabPage);
    }
    
    @SuppressWarnings("unchecked")
    private Reference<Container> covarianceReference(TabPage tabPage) {
        ObjectModel om = ((ObjectModelProvider)tabPage).objectModel();
        return (Reference<Container>)om.getAssociatedEndpoint(COVARIANCE_PARENT_REFERENCE_ID);
    }
    
    private static void assertTabControl(TabControl tabControl, TabPage ... tabPages) {
        Assert.assertEquals(tabPages.length, tabControl.getTabPages().size());
        int index = 0;
        for (TabPage tabPage : tabControl.getTabPages()) {
            Assert.assertSame(tabPages[index++], tabPage);
        }
    }
 
    static {
        ModelClass componentModelClass = ModelClass.of(Component.class);
        COVARIANCE_PARENT_REFERENCE_ID = componentModelClass.getDeclaredProperty("parent").getId();
    }
}
