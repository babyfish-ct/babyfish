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
package org.babyfish.test.hibernate.model.osetandnset;

import java.util.NavigableSet;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import junit.framework.Assert;

import org.babyfish.collection.XOrderedSet;
import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class DbTest {

    private static XEntityManagerFactory entityManagerFactory;
    
    @BeforeClass
    public static void initEntityManagerFactory() {
        entityManagerFactory =
                new HibernatePersistenceProvider(
                        DbTest.class.getPackage().getName().replace('.', '/') + 
                        "/persistence.xml")
                .createEntityManagerFactory(null, null);
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            try {
                Student jim = new Student("jim");
                Student kate = new Student("kate");
                Student tom = new Student("tom");
                Student mary = new Student("mary");
                Course history = new Course("history");
                Course math = new Course("math");
                Course english = new Course("english");
                Course physics = new Course("physics");
                
                em.persist(jim);
                em.persist(kate);
                em.persist(tom);
                em.persist(mary);
                em.persist(history);
                em.persist(math);
                em.persist(english);
                em.persist(physics);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    @AfterClass
    public static void disposeEntityManager() {
        XEntityManagerFactory emf = entityManagerFactory;
        if (emf != null) {
            entityManagerFactory = null;
            emf.close();
        }
    }
    
    @Before
    public void init() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            try {
                em.createNativeQuery("DELETE FROM osns_COURSE_STUDENT").executeUpdate();
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    @Test
    public void testModifyStudents() {
        
        XEntityManager em = entityManagerFactory.createEntityManager();
        try {
            Student jim = em.getReference(Student.class, 1L);
            Student kate = em.getReference(Student.class, 2L);
            Student tom = em.getReference(Student.class, 3L);
            Student mary = em.getReference(Student.class, 4L);
            Course history = em.getReference(Course.class, 1L);
            Course math = em.getReference(Course.class, 2L);
            Course english = em.getReference(Course.class, 3L);
            Course physics = em.getReference(Course.class, 4L);
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(jim.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(kate.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(tom.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(mary.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(history.getStudents()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(math.getStudents()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(english.getStudents()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(physics.getStudents()));
            
            em.getTransaction().begin();
            try {
                ((XOrderedSet<Course>)jim.getCourses()).descendingSet().add(english);
                ((XOrderedSet<Course>)jim.getCourses()).descendingSet().add(physics);
                ((XOrderedSet<Course>)kate.getCourses()).descendingSet().add(physics);
                ((XOrderedSet<Course>)kate.getCourses()).descendingSet().add(history);
                ((XOrderedSet<Course>)tom.getCourses()).descendingSet().add(history);
                ((XOrderedSet<Course>)tom.getCourses()).descendingSet().add(math);
                ((XOrderedSet<Course>)mary.getCourses()).descendingSet().add(math);
                ((XOrderedSet<Course>)mary.getCourses()).descendingSet().add(english);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
            
            Assert.assertTrue(jim.getCourses().contains(new Course("english")));
            Assert.assertTrue(jim.getCourses().contains(new Course("physics")));
            Assert.assertTrue(kate.getCourses().contains(new Course("history")));
            Assert.assertTrue(kate.getCourses().contains(new Course("physics")));
            Assert.assertTrue(tom.getCourses().contains(new Course("history")));
            Assert.assertTrue(tom.getCourses().contains(new Course("math")));
            Assert.assertTrue(mary.getCourses().contains(new Course("english")));
            Assert.assertTrue(mary.getCourses().contains(new Course("math")));
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(jim.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(kate.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(tom.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(mary.getCourses()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(history.getStudents()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(math.getStudents()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(english.getStudents()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(physics.getStudents()));
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Student jim = em.getReference(Student.class, 1L);
            Student kate = em.getReference(Student.class, 2L);
            Student tom = em.getReference(Student.class, 3L);
            Student mary = em.getReference(Student.class, 4L);
            Course history = em.getReference(Course.class, 1L);
            Course math = em.getReference(Course.class, 2L);
            Course english = em.getReference(Course.class, 3L);
            Course physics = em.getReference(Course.class, 4L);
            
            assertCourses(jim, "physics", "english");
            assertCourses(kate, "physics", "history");
            assertCourses(tom, "math", "history");
            assertCourses(mary, "english", "math");
            
            assertStudents(history, "kate", "tom");
            assertStudents(math, "mary", "tom");
            assertStudents(english, "jim", "mary");
            assertStudents(physics, "jim", "kate");
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Student jim = em.getReference(Student.class, 1L);
            Student kate = em.getReference(Student.class, 2L);
            Student tom = em.getReference(Student.class, 3L);
            Student mary = em.getReference(Student.class, 4L);
            Course history = em.getReference(Course.class, 1L);
            Course math = em.getReference(Course.class, 2L);
            Course english = em.getReference(Course.class, 3L);
            Course physics = em.getReference(Course.class, 4L);
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(jim.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(kate.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(tom.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(mary.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(history.getStudents()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(math.getStudents()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(english.getStudents()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(physics.getStudents()));
            
            em.getTransaction().begin();
            try {
                ((XOrderedSet<Course>)jim.getCourses()).descendingSet().remove(english);
                ((XOrderedSet<Course>)kate.getCourses()).descendingSet().remove(physics);
                ((XOrderedSet<Course>)tom.getCourses()).descendingSet().remove(history);
                ((XOrderedSet<Course>)mary.getCourses()).descendingSet().remove(math);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
            
            Assert.assertFalse(jim.getCourses().contains(new Course("english")));
            Assert.assertFalse(kate.getCourses().contains(new Course("physics")));
            Assert.assertFalse(tom.getCourses().contains(new Course("history")));
            Assert.assertFalse(mary.getCourses().contains(new Course("math")));
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(jim.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(kate.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(tom.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(mary.getCourses()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(history.getStudents()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(math.getStudents()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(english.getStudents()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(physics.getStudents()));
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Student jim = em.getReference(Student.class, 1L);
            Student kate = em.getReference(Student.class, 2L);
            Student tom = em.getReference(Student.class, 3L);
            Student mary = em.getReference(Student.class, 4L);
            Course history = em.getReference(Course.class, 1L);
            Course math = em.getReference(Course.class, 2L);
            Course english = em.getReference(Course.class, 3L);
            Course physics = em.getReference(Course.class, 4L);
            
            assertCourses(jim, "physics");
            assertCourses(kate, "history");
            assertCourses(tom, "math");
            assertCourses(mary, "english");
            
            assertStudents(history, "kate");
            assertStudents(math, "tom");
            assertStudents(english, "mary");
            assertStudents(physics, "jim");
        } finally {
            em.close();
        }
    }
    
    @Test
    public void testModifyCourses() {
        XEntityManager em = entityManagerFactory.createEntityManager();
        try {
            Student jim = em.getReference(Student.class, 1L);
            Student kate = em.getReference(Student.class, 2L);
            Student tom = em.getReference(Student.class, 3L);
            Student mary = em.getReference(Student.class, 4L);
            Course history = em.getReference(Course.class, 1L);
            Course math = em.getReference(Course.class, 2L);
            Course english = em.getReference(Course.class, 3L);
            Course physics = em.getReference(Course.class, 4L);
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(jim.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(kate.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(tom.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(mary.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(history.getStudents()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(math.getStudents()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(english.getStudents()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(physics.getStudents()));
            
            em.getTransaction().begin();
            try {
                ((NavigableSet<Student>)history.getStudents()).descendingSet().add(tom);
                ((NavigableSet<Student>)history.getStudents()).descendingSet().add(mary);
                ((NavigableSet<Student>)math.getStudents()).descendingSet().add(mary);
                ((NavigableSet<Student>)math.getStudents()).descendingSet().add(jim);
                ((NavigableSet<Student>)english.getStudents()).descendingSet().add(jim);
                ((NavigableSet<Student>)english.getStudents()).descendingSet().add(kate);
                ((NavigableSet<Student>)physics.getStudents()).descendingSet().add(kate);
                ((NavigableSet<Student>)physics.getStudents()).descendingSet().add(tom);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
            
            Assert.assertTrue(jim.getCourses().contains(new Course("english")));
            Assert.assertTrue(jim.getCourses().contains(new Course("math")));
            Assert.assertTrue(kate.getCourses().contains(new Course("english")));
            Assert.assertTrue(kate.getCourses().contains(new Course("physics")));
            Assert.assertTrue(tom.getCourses().contains(new Course("history")));
            Assert.assertTrue(tom.getCourses().contains(new Course("physics")));
            Assert.assertTrue(mary.getCourses().contains(new Course("history")));
            Assert.assertTrue(mary.getCourses().contains(new Course("math")));
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(jim.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(kate.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(tom.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(mary.getCourses()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(history.getStudents()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(math.getStudents()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(english.getStudents()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(physics.getStudents()));
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Student jim = em.getReference(Student.class, 1L);
            Student kate = em.getReference(Student.class, 2L);
            Student tom = em.getReference(Student.class, 3L);
            Student mary = em.getReference(Student.class, 4L);
            Course history = em.getReference(Course.class, 1L);
            Course math = em.getReference(Course.class, 2L);
            Course english = em.getReference(Course.class, 3L);
            Course physics = em.getReference(Course.class, 4L);
            
            assertCourses(jim, "english", "math");
            assertCourses(kate, "physics", "english");
            assertCourses(tom, "physics", "history");
            assertCourses(mary, "math", "history");
            
            assertStudents(history, "mary", "tom");
            assertStudents(math, "jim", "mary");
            assertStudents(english, "jim", "kate");
            assertStudents(physics, "kate", "tom");
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Student jim = em.getReference(Student.class, 1L);
            Student kate = em.getReference(Student.class, 2L);
            Student tom = em.getReference(Student.class, 3L);
            Student mary = em.getReference(Student.class, 4L);
            Course history = em.getReference(Course.class, 1L);
            Course math = em.getReference(Course.class, 2L);
            Course english = em.getReference(Course.class, 3L);
            Course physics = em.getReference(Course.class, 4L);
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(jim.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(kate.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(tom.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(mary.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(history.getStudents()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(math.getStudents()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(english.getStudents()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(physics.getStudents()));
            
            em.getTransaction().begin();
            try {
                ((NavigableSet<Student>)history.getStudents()).descendingSet().remove(tom);
                ((NavigableSet<Student>)math.getStudents()).descendingSet().remove(mary);
                ((NavigableSet<Student>)english.getStudents()).descendingSet().remove(jim);
                ((NavigableSet<Student>)physics.getStudents()).descendingSet().remove(kate);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
            
            Assert.assertFalse(jim.getCourses().contains(new Course("english")));
            Assert.assertFalse(kate.getCourses().contains(new Course("physics")));
            Assert.assertFalse(tom.getCourses().contains(new Course("history")));
            Assert.assertFalse(mary.getCourses().contains(new Course("math")));
            
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(jim.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(kate.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(tom.getCourses()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(mary.getCourses()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(history.getStudents()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(math.getStudents()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(english.getStudents()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(physics.getStudents()));
        } finally {
            em.close();
        }
        
        em = entityManagerFactory.createEntityManager();
        try {
            Student jim = em.getReference(Student.class, 1L);
            Student kate = em.getReference(Student.class, 2L);
            Student tom = em.getReference(Student.class, 3L);
            Student mary = em.getReference(Student.class, 4L);
            Course history = em.getReference(Course.class, 1L);
            Course math = em.getReference(Course.class, 2L);
            Course english = em.getReference(Course.class, 3L);
            Course physics = em.getReference(Course.class, 4L);
            
            em.getTransaction().begin();
            assertCourses(jim, "math");
            assertCourses(kate, "english");
            assertCourses(tom, "physics");
            assertCourses(mary, "history");
            
            assertStudents(history, "mary");
            assertStudents(math, "jim");
            assertStudents(english, "kate");
            assertStudents(physics, "tom");
            
        } finally {
            em.close();
        }
    }
    
    private static void assertCourses(Student student, String ... courseNames) {
        Assert.assertEquals(courseNames.length, student.getCourses().size());
        int index = 0;
        for (Course course : student.getCourses()) {
            Assert.assertEquals(courseNames[index++], course.getName());
        }
    }
    
    private static void assertStudents(Course course, String ... studentNames) {
        Assert.assertEquals(studentNames.length, course.getStudents().size());
        int index = 0;
        for (Student student : course.getStudents()) {
            Assert.assertEquals(studentNames[index++], student.getName());
        }
    }
}
