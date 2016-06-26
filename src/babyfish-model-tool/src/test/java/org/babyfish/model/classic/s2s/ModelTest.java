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
package org.babyfish.model.classic.s2s;

import java.util.Iterator;

import org.babyfish.model.classic.Utils;
import org.babyfish.model.classic.s2s.entities.Course;
import org.babyfish.model.classic.s2s.entities.Student;
import org.junit.Test;

public class ModelTest {

    @Test
    public void testModifyStudent() {
        Student student1 = new Student();
        Course course1 = new Course();
        Student student2 = new Student();
        Course course2 = new Course();
        Utils.assertCollection(student1.getCourses());
        Utils.assertCollection(course1.getStudents());
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().add(course1);
        Utils.assertCollection(student1.getCourses(), course1);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().add(course2);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().add(course1);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertCollection(student2.getCourses(), course1);
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().add(course2);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertCollection(student2.getCourses(), course1, course2);
        Utils.assertCollection(course2.getStudents(), student1, student2);
        
        student2.getCourses().remove(course2);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertCollection(student2.getCourses(), course1);
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().remove(course1);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents(), student1);
        
        student1.getCourses().remove(course2);
        Utils.assertCollection(student1.getCourses(), course1);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
        
        Iterator<Course> itr = student1.getCourses().iterator();
        itr.next();
        itr.remove();
        Utils.assertCollection(student1.getCourses());
        Utils.assertCollection(course1.getStudents());
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
    }
    
    @Test
    public void testModifyStudentByIterator() {
        Student student1 = new Student();
        Course course1 = new Course();
        Student student2 = new Student();
        Course course2 = new Course();
        Utils.assertCollection(student1.getCourses());
        Utils.assertCollection(course1.getStudents());
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().add(course1);
        Utils.assertCollection(student1.getCourses(), course1);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().add(course2);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().add(course1);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertCollection(student2.getCourses(), course1);
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().add(course2);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertCollection(student2.getCourses(), course1, course2);
        Utils.assertCollection(course2.getStudents(), student1, student2);
        
        Utils.locateIterator(student2.getCourses(), course2).remove();
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertCollection(student2.getCourses(), course1);
        Utils.assertCollection(course2.getStudents(), student1);
        
        Utils.locateIterator(student2.getCourses(), course1).remove();
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents(), student1);
        
        Utils.locateIterator(student1.getCourses(), course2).remove();
        Utils.assertCollection(student1.getCourses(), course1);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
        
        Utils.locateIterator(student1.getCourses(), course1).remove();
        Utils.assertCollection(student1.getCourses());
        Utils.assertCollection(course1.getStudents());
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
    }
    
    @Test
    public void testModifyCourse() {
        Student student1 = new Student();
        Course course1 = new Course();
        Student student2 = new Student();
        Course course2 = new Course();
        Utils.assertCollection(student1.getCourses());
        Utils.assertCollection(course1.getStudents());
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
        
        course1.getStudents().add(student1);
        Utils.assertCollection(student1.getCourses(), course1);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
        
        course2.getStudents().add(student1);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents(), student1);
        
        course1.getStudents().add(student2);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertCollection(student2.getCourses(), course1);
        Utils.assertCollection(course2.getStudents(), student1);
        
        course2.getStudents().add(student2);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertCollection(student2.getCourses(), course1, course2);
        Utils.assertCollection(course2.getStudents(), student1, student2);
        
        course2.getStudents().remove(student2);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertCollection(student2.getCourses(), course1);
        Utils.assertCollection(course2.getStudents(), student1);
        
        course1.getStudents().remove(student2);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents(), student1);
        
        course2.getStudents().remove(student1);
        Utils.assertCollection(student1.getCourses(), course1);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
        
        Iterator<Student> itr = course1.getStudents().iterator();
        itr.next();
        itr.remove();
        Utils.assertCollection(student1.getCourses());
        Utils.assertCollection(course1.getStudents());
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
    }
    
    @Test
    public void testModifyCourseByIterator() {
        Student student1 = new Student();
        Course course1 = new Course();
        Student student2 = new Student();
        Course course2 = new Course();
        Utils.assertCollection(student1.getCourses());
        Utils.assertCollection(course1.getStudents());
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
        
        course1.getStudents().add(student1);
        Utils.assertCollection(student1.getCourses(), course1);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
        
        course2.getStudents().add(student1);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents(), student1);
        
        course1.getStudents().add(student2);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertCollection(student2.getCourses(), course1);
        Utils.assertCollection(course2.getStudents(), student1);
        
        course2.getStudents().add(student2);
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertCollection(student2.getCourses(), course1, course2);
        Utils.assertCollection(course2.getStudents(), student1, student2);
        
        Utils.locateIterator(course2.getStudents(), student2).remove();
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertCollection(student2.getCourses(), course1);
        Utils.assertCollection(course2.getStudents(), student1);
        
        Utils.locateIterator(course1.getStudents(), student2).remove();
        Utils.assertCollection(student1.getCourses(), course1, course2);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents(), student1);
        
        Utils.locateIterator(course2.getStudents(), student1).remove();
        Utils.assertCollection(student1.getCourses(), course1);
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
        
        Utils.locateIterator(course1.getStudents(), student1).remove();
        Utils.assertCollection(student1.getCourses());
        Utils.assertCollection(course1.getStudents());
        Utils.assertCollection(student2.getCourses());
        Utils.assertCollection(course2.getStudents());
    }
}
