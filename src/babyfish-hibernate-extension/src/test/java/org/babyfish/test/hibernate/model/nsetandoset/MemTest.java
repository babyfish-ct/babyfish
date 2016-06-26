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
package org.babyfish.test.hibernate.model.nsetandoset;

import java.util.NavigableSet;

import junit.framework.Assert;

import org.babyfish.collection.XOrderedSet;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class MemTest {

    @Test
    public void testModifyStudent() {
        
        Student jim = new Student();
        Student kate = new Student();
        Student tom = new Student();
        Student mary = new Student();
        Course history = new Course();
        Course math = new Course();
        Course english = new Course();
        Course physics = new Course();
        
        jim.setName("jim");
        kate.setName("kate");
        tom.setName("tom");
        mary.setName("mary");
        history.setName("history");
        math.setName("math");
        english.setName("english");
        physics.setName("physics");
        
        ((NavigableSet<Course>)jim.getCourses()).descendingSet().add(english);
        ((NavigableSet<Course>)jim.getCourses()).descendingSet().add(physics);
        ((NavigableSet<Course>)kate.getCourses()).descendingSet().add(physics);
        ((NavigableSet<Course>)kate.getCourses()).descendingSet().add(history);
        ((NavigableSet<Course>)tom.getCourses()).descendingSet().add(history);
        ((NavigableSet<Course>)tom.getCourses()).descendingSet().add(math);
        ((NavigableSet<Course>)mary.getCourses()).descendingSet().add(math);
        ((NavigableSet<Course>)mary.getCourses()).descendingSet().add(english);
        
        assertCourses(jim, "english", "physics");
        assertCourses(kate, "history", "physics");
        assertCourses(tom, "history", "math");
        assertCourses(mary, "english", "math");
        
        assertStudents(history, "kate", "tom");
        assertStudents(math, "tom", "mary");
        assertStudents(english, "jim", "mary");
        assertStudents(physics, "jim", "kate");
        
        ((NavigableSet<Course>)jim.getCourses()).descendingSet().remove(english);
        ((NavigableSet<Course>)kate.getCourses()).descendingSet().remove(physics);
        ((NavigableSet<Course>)tom.getCourses()).descendingSet().remove(history);
        ((NavigableSet<Course>)mary.getCourses()).descendingSet().remove(math);
        
        assertCourses(jim, "physics");
        assertCourses(kate, "history");
        assertCourses(tom, "math");
        assertCourses(mary, "english");
        
        assertStudents(history, "kate");
        assertStudents(math, "tom");
        assertStudents(english, "mary");
        assertStudents(physics, "jim");
    }
    
    @Test
    public void testModifyCourses() {
        
        Student jim = new Student();
        Student kate = new Student();
        Student tom = new Student();
        Student mary = new Student();
        Course history = new Course();
        Course math = new Course();
        Course english = new Course();
        Course physics = new Course();
        
        jim.setName("jim");
        kate.setName("kate");
        tom.setName("tom");
        mary.setName("mary");
        history.setName("history");
        math.setName("math");
        english.setName("english");
        physics.setName("physics");
        
        ((XOrderedSet<Student>)history.getStudents()).descendingSet().add(tom);
        ((XOrderedSet<Student>)history.getStudents()).descendingSet().add(mary);
        ((XOrderedSet<Student>)math.getStudents()).descendingSet().add(mary);
        ((XOrderedSet<Student>)math.getStudents()).descendingSet().add(jim);
        ((XOrderedSet<Student>)english.getStudents()).descendingSet().add(jim);
        ((XOrderedSet<Student>)english.getStudents()).descendingSet().add(kate);
        ((XOrderedSet<Student>)physics.getStudents()).descendingSet().add(kate);
        ((XOrderedSet<Student>)physics.getStudents()).descendingSet().add(tom);
        
        assertCourses(jim, "english", "math");
        assertCourses(kate, "english", "physics");
        assertCourses(tom, "history", "physics");
        assertCourses(mary, "history", "math");
        assertStudents(history, "tom", "mary");
        assertStudents(math, "mary", "jim");
        assertStudents(english, "jim", "kate");
        assertStudents(physics, "kate", "tom");
        
        ((XOrderedSet<Student>)history.getStudents()).descendingSet().remove(tom);
        ((XOrderedSet<Student>)math.getStudents()).descendingSet().remove(mary);
        ((XOrderedSet<Student>)english.getStudents()).descendingSet().remove(jim);
        ((XOrderedSet<Student>)physics.getStudents()).descendingSet().remove(kate);
        
        assertCourses(jim, "math");
        assertCourses(kate, "english");
        assertCourses(tom, "physics");
        assertCourses(mary, "history");
        assertStudents(history, "mary");
        assertStudents(math, "jim");
        assertStudents(english, "kate");
        assertStudents(physics, "tom");
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
