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
package org.babyfish.test.hibernate.model.setandset;

import java.util.function.Consumer;

import org.babyfish.data.LazinessManageable;
import org.babyfish.test.hibernate.model.AbstractHibernateTest;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class DbTest extends AbstractHibernateTest {
    
    @BeforeClass
    public static void initClass() {
        initSessionFactory(Student.class, Course.class);
    }
    
    @Before
    public void initDb() {
        Consumer<Session> handler =
            session -> {
                session.createSQLQuery("DELETE FROM ss_STUDENT_COURSE").executeUpdate();
                session.createSQLQuery("DELETE FROM ss_STUDENT").executeUpdate();
                session.createSQLQuery("DELETE FROM ss_COURSE").executeUpdate();
                session
                .createSQLQuery("INSERT INTO ss_STUDENT(STUDENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 1L)
                .setString(1, "student1")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO ss_STUDENT(STUDENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 2L)
                .setString(1, "student2")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO ss_COURSE(COURSE_ID, NAME) VALUES(?, ?)")
                .setLong(0, 1L)
                .setString(1, "course1")
                .executeUpdate();
                session
                .createSQLQuery("INSERT INTO ss_COURSE(COURSE_ID, NAME) VALUES(?, ?)")
                .setLong(0, 2L)
                .setString(1, "course2")
                .executeUpdate();
            };
        execute(handler);
    }
    
    private static Object[] load() {
        return execute((Session session) -> {
            Object[] arr = new Object[4];
            arr[0] = session.get(Student.class, 1L);
            arr[1] = session.get(Student.class, 2L);
            arr[2] = session.get(Course.class, 1L);
            arr[3] = session.get(Course.class, 2L);
            for (Object o : arr) {
                if (o instanceof Student) {
                    ((LazinessManageable)((Student)o).getCourses()).load();
                } else {
                    ((LazinessManageable)((Course)o).getStudents()).load();
                }
            }
            return arr;
        });
    }
    
    @Test
    public void testMergeWithStudentPreLoading() {
        this.testMergeStudent(true);
    }
    
    @Test
    public void testMergeStudentWithoutPreLoading() {
        this.testMergeStudent(false);
    }
    
    private void testMergeStudent(final boolean loadBeforeMerge) {
        Object[] arr = load();
        final Student detachedStudent1 = (Student)arr[0];
        final Student detachedStudent2 = (Student)arr[1];
        final Course detachedCourse1 = (Course)arr[2];
        final Course detachedCourse2 = (Course)arr[3];
        
        Consumer<Session> handler;
        
        handler = session -> {
            session.setFlushMode(FlushMode.COMMIT);
            
            Student student1 = (Student)session.get(Student.class, 1L);
            Student student2 = (Student)session.get(Student.class, 2L);
            Course course1 = (Course)session.get(Course.class, 1L);
            Course course2 = (Course)session.get(Course.class, 2L);
            detachedStudent1.getCourses().add(detachedCourse1);
            
            if (loadBeforeMerge) {
                assertCollection(student1.getCourses());
                assertCollection(student2.getCourses());
                assertCollection(course1.getStudents());
                assertCollection(course2.getStudents());
            }
            
            session.merge(detachedStudent1);
            
            assertCollection(student1.getCourses(), course1);
            assertCollection(student2.getCourses());
            assertCollection(course1.getStudents(), student1);
            assertCollection(course2.getStudents());
        };
        execute(handler);
        
        handler = session -> {
            session.setFlushMode(FlushMode.COMMIT);
            
            Student student1 = (Student)session.get(Student.class, 1L);
            Student student2 = (Student)session.get(Student.class, 2L);
            Course course1 = (Course)session.get(Course.class, 1L);
            Course course2 = (Course)session.get(Course.class, 2L);
            detachedStudent1.getCourses().add(detachedCourse2);
            
            if (loadBeforeMerge) {
                assertCollection(student1.getCourses(), course1);
                assertCollection(student2.getCourses());
                assertCollection(course1.getStudents(), student1);
                assertCollection(course2.getStudents());
            }
            
            session.merge(detachedStudent1);
            
            assertCollection(student1.getCourses(), course1, course2);
            assertCollection(student2.getCourses());
            assertCollection(course1.getStudents(), student1);
            assertCollection(course2.getStudents(), student1);
        };
        execute(handler);
        
        handler = session -> {
            session.setFlushMode(FlushMode.COMMIT);
            
            Student student1 = (Student)session.get(Student.class, 1L);
            Student student2 = (Student)session.get(Student.class, 2L);
            Course course1 = (Course)session.get(Course.class, 1L);
            Course course2 = (Course)session.get(Course.class, 2L);
            detachedStudent2.getCourses().add(detachedCourse1);
            
            if (loadBeforeMerge) {
                assertCollection(student1.getCourses(), course1, course2);
                assertCollection(student2.getCourses());
                assertCollection(course1.getStudents(), student1);
                assertCollection(course2.getStudents(), student1);
            }
            
            session.merge(detachedStudent2);
            
            assertCollection(student1.getCourses(), course1, course2);
            assertCollection(student2.getCourses(), course1);
            assertCollection(course1.getStudents(), student1, student2);
            assertCollection(course2.getStudents(), student1);
        };
        execute(handler);
        
        handler = session -> {
            session.setFlushMode(FlushMode.COMMIT);
            
            Student student1 = (Student)session.get(Student.class, 1L);
            Student student2 = (Student)session.get(Student.class, 2L);
            Course course1 = (Course)session.get(Course.class, 1L);
            Course course2 = (Course)session.get(Course.class, 2L);
            detachedStudent2.getCourses().add(detachedCourse2);
            
            if (loadBeforeMerge) {
                assertCollection(student1.getCourses(), course1, course2);
                assertCollection(student2.getCourses(), course1);
                assertCollection(course1.getStudents(), student1, student2);
                assertCollection(course2.getStudents(), student1);
            }
            
            session.merge(detachedStudent2);
            
            assertCollection(student1.getCourses(), course1, course2);
            assertCollection(student2.getCourses(), course1, course2);
            assertCollection(course1.getStudents(), student1, student2);
            assertCollection(course2.getStudents(), student1, student2);
        };
        execute(handler);
        
        handler = session -> {
            session.setFlushMode(FlushMode.COMMIT);
            
            Student student1 = (Student)session.get(Student.class, 1L);
            Student student2 = (Student)session.get(Student.class, 2L);
            Course course1 = (Course)session.get(Course.class, 1L);
            Course course2 = (Course)session.get(Course.class, 2L);
            detachedStudent1.getCourses().remove(detachedCourse1);
            
            if (loadBeforeMerge) {
                assertCollection(student1.getCourses(), course1, course2);
                assertCollection(student2.getCourses(), course1, course2);
                assertCollection(course1.getStudents(), student1, student2);
                assertCollection(course2.getStudents(), student1, student2);
            }
            
            session.merge(detachedStudent1);
            
            assertCollection(student1.getCourses(), course2);
            assertCollection(student2.getCourses(), course1, course2);
            assertCollection(course1.getStudents(), student2);
            assertCollection(course2.getStudents(), student1, student2);
        };
        execute(handler);
    }
    
}
