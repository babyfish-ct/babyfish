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
package org.babyfish.hibernate.scalar.laziness;

import java.util.Arrays;

import javax.persistence.Persistence;

import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.spi.ObjectModelProvider;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class MergeTest extends AbstractTest {
    
    private static final int IMAGE_ID =
            ModelClass.of(Department.class).getDeclaredProperties().get("image").getId();
    
    private static final int NAME_ID =
            ModelClass.of(Department.class).getDeclaredProperties().get("name").getId();
    
    private static final long DEPARTMENT_ID = 1L;
    
    private static final String OLD_DEPARTMENT_NAME = "Market";

    private static final byte[] OLD_DEPARTMENT_IMAGE = new byte[] { 99, 88, 77, 66, 55, 44, 33, 22, 11 };
    
    private static final String NEW_DEPARTMENT_NAME = "Sales";
    
    private static final byte[] NEW_DEPARTMENT_IMAGE = new byte[] { 13, 24, 35, 46, 57, 68, 79, 80, 91 };
    
    private static int OLD_VERSION = 10;
    
    private XEntityManagerFactory entityManagerFactory;
    
    @Before
    public void before() {
        this.entityManagerFactory = initEntityManagerFactory();
    }

    @After
    public void after() {
        XEntityManagerFactory emf = this.entityManagerFactory;
        if (emf != null) {
            this.entityManagerFactory = null;
            emf.close();
        }
    }
    
    @Test
    public void testMergeManagedDepartmentWithoutImage() {
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            Department department = getDepartment(em, OLD_DEPARTMENT_NAME, false);
            mergeDepartment(em, department, NEW_DEPARTMENT_NAME, null);
        }
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            getDepartment(em, NEW_DEPARTMENT_NAME, false);
        }
        this.assertImage(OLD_DEPARTMENT_IMAGE);
    }
    
    @Test
    public void testMergeDetachedDepartmentWithoutImage() {
        {
            Department department;
            try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
                department = getDepartment(em, OLD_DEPARTMENT_NAME, false);
            }
            try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
                mergeDepartment(em, department, NEW_DEPARTMENT_NAME, null);
            }
        }
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            getDepartment(em, NEW_DEPARTMENT_NAME, false);
        }
        this.assertImage(OLD_DEPARTMENT_IMAGE);
    }
    
    @Test
    public void testMergeDetached2DepartmentWithoutImage() {
        {
            Department department;
            try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
                department = new Department();
                department.setImage(new byte[] { 111, 122 });
                department.setId(DEPARTMENT_ID);
                department.setVersion(OLD_VERSION);
                department.setName(NEW_DEPARTMENT_NAME);
                ((ObjectModelProvider)department).objectModel().disable(IMAGE_ID);
            }
            try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
                mergeDetached2Department(em, department, NEW_DEPARTMENT_NAME);
            }
        }
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            getDepartment(em, NEW_DEPARTMENT_NAME, false);
        }
        this.assertImage(OLD_DEPARTMENT_IMAGE);
    }
    
    @Test
    public void testMergeManagedDepartmentWithImage() {
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            Department department = getDepartment(em, OLD_DEPARTMENT_NAME, false);
            mergeDepartment(em, department, NEW_DEPARTMENT_NAME, NEW_DEPARTMENT_IMAGE);
        }
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            getDepartment(em, NEW_DEPARTMENT_NAME, false);
        }
        this.assertImage(NEW_DEPARTMENT_IMAGE);
    }
    
    @Test
    public void testMergeDetachedDepartmentWithImage() {
        {
            Department department;
            try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
                department = getDepartment(em, OLD_DEPARTMENT_NAME, false);
            }
            department.setImage(NEW_DEPARTMENT_IMAGE);
            try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
                mergeDepartment(em, department, NEW_DEPARTMENT_NAME, null);
            }
        }
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            getDepartment(em, NEW_DEPARTMENT_NAME, false);
        }
        this.assertImage(NEW_DEPARTMENT_IMAGE);
    }
    
    @Test
    public void testMergeDetached2DepartmentWithImage() {
        {
            Department department = new Department();
            department.setId(DEPARTMENT_ID);
            department.setVersion(OLD_VERSION);
            department.setName(NEW_DEPARTMENT_NAME);
            department.setImage(NEW_DEPARTMENT_IMAGE);
            try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
                mergeDetached2Department(em, department, NEW_DEPARTMENT_NAME);
            }
        }
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            getDepartment(em, NEW_DEPARTMENT_NAME, false);
        }
        this.assertImage(NEW_DEPARTMENT_IMAGE);
    }
    
    @Test
    public void mergeDetachDepartmentWithImageOnly() {
        {
            Department department;
            try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
                department = getDepartment(em, OLD_DEPARTMENT_NAME, false);
            }
            department.setName(NEW_DEPARTMENT_NAME);
            department.setImage(NEW_DEPARTMENT_IMAGE);
            ((ObjectModelProvider)department).objectModel().disable(NAME_ID);
            try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
                mergeDepartment(em, department, null, null);
            }
        }
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            getDepartment(em, OLD_DEPARTMENT_NAME, false);
        }
        assertImage(NEW_DEPARTMENT_IMAGE);
    }
    
    /*@Test
    public void mergeClob() {
        {
            Department department;
            try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
                department = getDepartment(em, OLD_DEPARTMENT_NAME, false);
            }
            department.setDescription(NEW_DEPARTMENT_DESCRIPTION);
            try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
                em.merge(department);
            }
        }
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            Department department = getDepartment(em, OLD_DEPARTMENT_NAME, false);
            System.out.println(department.getDescription());
        }
    }*/
    
    private static Department getDepartment(XEntityManager em, String expectedName, boolean isImageLoaded) {
        Department department = em.find(Department.class, DEPARTMENT_ID);
        Assert.assertEquals(expectedName, department.getName());
        Assert.assertEquals(isImageLoaded, Persistence.getPersistenceUtil().isLoaded(department, "image"));
        return department;
    }
    
    private void assertImage(byte[] image) {
        try (XEntityManager em = this.entityManagerFactory.createEntityManager()) {
            Department department = em.find(Department.class, DEPARTMENT_ID);
            Assert.assertTrue(Arrays.equals(image, department.getImage()));
        }
    }
    
    private static void mergeDepartment(XEntityManager em, Department department, String newName, byte[] nullOrNewImage) {
        boolean isImageLoaded = Persistence.getPersistenceUtil().isLoaded(department, "image");
        em.getTransaction().begin();
        try {
            if (newName != null) {
                department.setName(newName);
            }
            if (nullOrNewImage != null) {
                department.setImage(nullOrNewImage);
            }
            em.merge(department);
            em.flush();
        } catch (RuntimeException | Error ex) {
            em.getTransaction().rollback();
            throw ex;
        }
        em.getTransaction().commit();
        Assert.assertEquals(
                isImageLoaded || nullOrNewImage != null, 
                Persistence.getPersistenceUtil().isLoaded(department, "image")
        );
    }
    
    private static void mergeDetached2Department(XEntityManager em, Department department, String newName) {
        em.getTransaction().begin();
        try {
            department.setName(newName);
            em.merge(department);
            em.flush();
        } catch (RuntimeException | Error ex) {
            em.getTransaction().rollback();
            throw ex;
        }
        em.getTransaction().commit();
    }
}
