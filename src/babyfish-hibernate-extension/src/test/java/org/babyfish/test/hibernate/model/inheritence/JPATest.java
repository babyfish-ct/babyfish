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
package org.babyfish.test.hibernate.model.inheritence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.function.Function;

import javax.persistence.Persistence;

import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.model.jpa.path.CollectionFetchType;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XJoin;
import org.babyfish.persistence.criteria.XRoot;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class JPATest {

    private static XEntityManagerFactory entityManagerFactory;
    
    @BeforeClass
    public static void initEntityManagerFactory() {
        entityManagerFactory =
                new HibernatePersistenceProvider(
                        JPATest.class.getPackage().getName().replace('.', '/') + 
                        "/persistence.xml")
                .createEntityManagerFactory(null, null);
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            
            TabControl tabControl = new TabControl();
            tabControl.setWidth(400);
            tabControl.setHeight(300);
            
            TabPage tabPage1 = new TabPage();
            tabPage1.setTitle("page1");
            
            TabPage tabPage2 = new TabPage();
            tabPage2.setTitle("page2");
            
            Button button1InPage1 = new Button();
            button1InPage1.setText("page1::button1");
            button1InPage1.setWidth(100);
            button1InPage1.setHeight(30);
            
            Button button2InPage1 = new Button();
            button2InPage1.setText("page1::button2");
            button2InPage1.setWidth(100);
            button2InPage1.setHeight(30);
            
            Button button1InPage2 = new Button();
            button1InPage2.setText("page2::button1");
            button1InPage2.setWidth(120);
            button1InPage2.setHeight(25);
            
            Button button2InPage2 = new Button();
            button2InPage2.setText("page2::button2");
            button2InPage2.setWidth(120);
            button2InPage2.setHeight(25);
            
            tabControl.getPages().add(tabPage1);
            tabControl.getPages().add(tabPage2);
            
            tabPage1.getControls().add(button1InPage1);
            tabPage1.getControls().add(button2InPage1);
            
            tabPage2.getControls().add(button1InPage2);
            tabPage2.getControls().add(button2InPage2);
            
            em.getTransaction().begin();
            try {
                em.persist(tabControl);
                em.persist(tabPage1);
                em.persist(tabPage2);
                em.persist(button1InPage1);
                em.persist(button2InPage1);
                em.persist(button1InPage2);
                em.persist(button2InPage2);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        }
    }
    
    @AfterClass
    public static void disposeEntityManagerFactory() {
        XEntityManagerFactory emf = entityManagerFactory;
        if (emf != null) {
            entityManagerFactory = null;
            emf.close();
        }
    }
    
    @Test
    public void testUnloadedSerializedAssociations() {
        TabPage page;
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            page = em.find(TabPage.class, 2L);
            Assert.assertNotNull(page);
        }
        TabPage deserializedPage = serialzingClone(page);
        Assert.assertTrue(page != deserializedPage);
        Assert.assertEquals("page1", deserializedPage.getTitle());
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(deserializedPage.getParent()));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(deserializedPage.getControls()));
        Assert.assertEquals(1L, deserializedPage.getParent().getId().longValue());
    }
    
    @Test
    public void testSerializeTabControl() {
        testSerialization((TabControl x) -> serialzingClone(x));
    }
    
    @Test
    public void testSerializeTabPage1() {
        testSerialization((TabControl x) -> serialzingClone(x.getPages().get(0)).getTabControl());
    }
    
    @Test
    public void testSerializeTabPage2() {
        testSerialization((TabControl x) -> serialzingClone(x.getPages().get(1)).getTabControl());
    }
    
    @Test
    public void testSerializeButton1InTabPage1() {
        testSerialization((TabControl x) -> (
                (TabPage)
                serialzingClone(x.getPages().get(0).getControls().get(0)).getParent())
                .getTabControl()
        );
    }
    
    @Test
    public void testSerializeButton2InTabPage1() {
        testSerialization((TabControl x) -> (
                (TabPage)serialzingClone(x.getPages().get(0).getControls().get(1))
                .getParent())
                .getTabControl()
        );
    }
    
    @Test
    public void testSerializeButton1InTabPage2() {
        testSerialization((TabControl x) -> 
                ((TabPage)serialzingClone(x.getPages().get(1).getControls().get(0)).getParent())
                .getTabControl()
        );
    }
    
    @Test
    public void testSerializeButton2InTabPage2() {
        testSerialization((TabControl x) -> 
                ((TabPage)serialzingClone(x.getPages().get(1).getControls().get(1))
                .getParent())
                .getTabControl()
        );
    }
    
    private void testSerialization(Function<TabControl, TabControl> serializingCloneAction) {
        TabControl tabControl;
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            XCriteriaQuery<TabControl> cq = em.getCriteriaBuilder().createQuery(TabControl.class);
            cq.from(TabControl.class);
            tabControl = 
                    em
                    .createQuery(cq)
                    .setQueryPaths(TabControl__.begin().controls().controls().controls().end())
                    .getSingleResult();
        }
        TabPage tabPage1 = tabControl.getPages().get(0);
        TabPage tabPage2 = tabControl.getPages().get(1);
        Button button1InPage1 = (Button)tabPage1.getControls().get(0);
        Button button2InPage1 = (Button)tabPage1.getControls().get(1);
        Button button1InPage2 = (Button)tabPage2.getControls().get(0);
        Button button2InPage2 = (Button)tabPage2.getControls().get(1);
        
        TabControl deserializedTabControl = serializingCloneAction.apply(tabControl);
        Assert.assertEquals(2, deserializedTabControl.getPages().size());
        TabPage deserializedTabPage1 = deserializedTabControl.getPages().get(0);
        Assert.assertEquals(2, deserializedTabPage1.getControls().size());
        TabPage deserializedTabPage2 = deserializedTabControl.getPages().get(1);
        Assert.assertEquals(2, deserializedTabPage2.getControls().size());
        Button deserializedButton1InPage1 = (Button)deserializedTabPage1.getControls().get(0);
        Button deserializedButton2InPage1 = (Button)deserializedTabPage1.getControls().get(1);
        Button deserializedButton1InPage2 = (Button)deserializedTabPage2.getControls().get(0);
        Button deserializedButton2InPage2 = (Button)deserializedTabPage2.getControls().get(1);
        
        Assert.assertTrue(tabControl != deserializedTabControl);
        Assert.assertTrue(tabPage1 != deserializedTabPage1);
        Assert.assertTrue(tabPage2 != deserializedTabPage2);
        Assert.assertTrue(button1InPage1 != deserializedButton1InPage1);
        Assert.assertTrue(button2InPage1 != deserializedButton2InPage1);
        Assert.assertTrue(button1InPage2 != deserializedButton1InPage2);
        Assert.assertTrue(button2InPage2 != deserializedButton2InPage2);
        
        Assert.assertEquals(1L, deserializedTabControl.getId().longValue());
        Assert.assertEquals(400, deserializedTabControl.getWidth());
        Assert.assertEquals(300, deserializedTabControl.getHeight());
        assertChildControls(deserializedTabControl, deserializedTabPage1, deserializedTabPage2);
        Assert.assertSame(null, deserializedTabControl.getParent());
        
        Assert.assertEquals(2L, deserializedTabPage1.getId().longValue());
        Assert.assertEquals(0, deserializedTabPage1.getWidth());
        Assert.assertEquals(0, deserializedTabPage1.getHeight());
        assertChildControls(deserializedTabPage1, deserializedButton1InPage1, deserializedButton2InPage1);
        Assert.assertSame(deserializedTabControl, deserializedTabPage1.getParent());
        Assert.assertEquals("page1", deserializedTabPage1.getTitle());
        
        Assert.assertEquals(3L, deserializedTabPage2.getId().longValue());
        Assert.assertEquals(0, deserializedTabPage2.getWidth());
        Assert.assertEquals(0, deserializedTabPage2.getHeight());
        assertChildControls(deserializedTabPage2, deserializedButton1InPage2, deserializedButton2InPage2);
        Assert.assertSame(deserializedTabControl, deserializedTabPage2.getParent());
        Assert.assertEquals("page2", deserializedTabPage2.getTitle());
        
        Assert.assertEquals(4L, deserializedButton1InPage1.getId().longValue());
        Assert.assertEquals(100, deserializedButton1InPage1.getWidth());
        Assert.assertEquals(30, deserializedButton1InPage1.getHeight());
        assertChildControls(deserializedButton1InPage1);
        Assert.assertSame(deserializedTabPage1, deserializedButton1InPage1.getParent());
        Assert.assertEquals("page1::button1", deserializedButton1InPage1.getText());
        
        Assert.assertEquals(5L, deserializedButton2InPage1.getId().longValue());
        Assert.assertEquals(100, deserializedButton2InPage1.getWidth());
        Assert.assertEquals(30, deserializedButton2InPage1.getHeight());
        assertChildControls(deserializedButton2InPage1);
        Assert.assertSame(deserializedTabPage1, deserializedButton2InPage1.getParent());
        Assert.assertEquals("page1::button2", deserializedButton2InPage1.getText());
        
        Assert.assertEquals(6L, deserializedButton1InPage2.getId().longValue());
        Assert.assertEquals(120, deserializedButton1InPage2.getWidth());
        Assert.assertEquals(25, deserializedButton1InPage2.getHeight());
        assertChildControls(deserializedButton1InPage2);
        Assert.assertSame(deserializedTabPage2, deserializedButton1InPage2.getParent());
        Assert.assertEquals("page2::button1", deserializedButton1InPage2.getText());
        
        Assert.assertEquals(7L, deserializedButton2InPage2.getId().longValue());
        Assert.assertEquals(120, deserializedButton2InPage2.getWidth());
        Assert.assertEquals(25, deserializedButton2InPage2.getHeight());
        assertChildControls(deserializedButton2InPage2);
        Assert.assertSame(deserializedTabPage2, deserializedButton2InPage2.getParent());
        Assert.assertEquals("page2::button2", deserializedButton2InPage2.getText());
        
        TabPage newPage = new TabPage();
        deserializedTabControl.getPages().add(deserializedTabPage1);
        deserializedTabControl.getPages().add(newPage);
        assertChildControls(deserializedTabControl, deserializedTabPage2, deserializedTabPage1, newPage);
        Assert.assertSame(deserializedTabControl, newPage.getParent());
        
        {
            Button newButtonInPage1 = new Button();
            deserializedTabPage1.getControls().add(deserializedButton1InPage1);
            deserializedTabPage1.getControls().add(newButtonInPage1);
            assertChildControls(deserializedTabPage1, deserializedButton2InPage1, deserializedButton1InPage1, newButtonInPage1);
            Assert.assertSame(deserializedTabPage1, newButtonInPage1.getParent());
        }
        
        {
            Button newButtonInPage2 = new Button();
            deserializedTabPage2.getControls().add(deserializedButton1InPage2);
            deserializedTabPage2.getControls().add(newButtonInPage2);
            assertChildControls(deserializedTabPage2, deserializedButton2InPage2, deserializedButton1InPage2, newButtonInPage2);
            Assert.assertSame(deserializedTabPage2, newButtonInPage2.getParent());
        }
        
        try {
            deserializedTabControl.getControls().add(new Button());
            Assert.fail("must throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }
    
    private static void assertChildControls(Control thisControl, Control ... childControls) {
        Assert.assertEquals(childControls.length, thisControl.getControls().size());
        int index = 0;
        for (Control actualChildControl : thisControl.getControls()) {
            Assert.assertSame(childControls[index++], actualChildControl);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T serialzingClone(T obj) {
        try {
            byte[] buf;
            try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    ObjectOutputStream oout = new ObjectOutputStream(bout)) {
                oout.writeObject(obj);
                oout.flush();
                buf = bout.toByteArray();
            }
            try (ByteArrayInputStream bin = new ByteArrayInputStream(buf);
                    ObjectInputStream oin = new ObjectInputStream(bin)) {
                return (T)oin.readObject();
            }
        } catch (IOException | ClassNotFoundException ex) {
            throw new AssertionError(ex);
        }
    }
    
    @Test
    public void testTreatCollection() {
        try (XEntityManager entityManager = entityManagerFactory.createEntityManager()) {
            XCriteriaBuilder cb = entityManager.getCriteriaBuilder();
            XCriteriaQuery<Control> cq = cb.createQuery(Control.class);
            XRoot<Control> control = cq.from(Control.class);
            XJoin<Control, TabPage> tabPage = cb.treat(control.join(Control_.controls), TabPage.class);
            cq
            .where(cb.equal(tabPage.get(TabPage_.title), "page1"))
            .select(control);
            List<Control> controls = 
                    entityManager
                    .createQuery(cq)
                    .setQueryPaths(Control__.begin().controls(CollectionFetchType.PARTIAL).end())
                    .getResultList();
            Assert.assertEquals(1, controls.size());
        }
    }
}
