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
package org.babyfish.model.classic.m2b;

import java.util.ArrayList;
import java.util.List;

import org.babyfish.model.classic.Utils;
import org.babyfish.model.classic.m2b.entities.Course;
import org.babyfish.model.classic.m2b.entities.Student;
import org.junit.Test;

public class ModelTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testAddByCourse() {
        Student student = new Student();
        Course course = new Course();
        course.getStudents().add(student);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllByCourse() {
        Student student = new Student();
        Course course = new Course();
        List<Student> students = new ArrayList<Student>();
        students.add(student);
        course.getStudents().addAll(students);
    }
    
    @Test
    public void testModifyStudentByKey() {
        Student student1 = new Student();
        Course course1 = new Course();
        Student student2 = new Student();
        Course course2 = new Course();
        Utils.assertMap(student1.getCourses(), new String[0], new Course[0]);
        Utils.assertCollection(course1.getStudents());
        Utils.assertMap(student2.getCourses(), new String[0], new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().put("first-course", course1);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(), 
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().put("second-course", course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0],
                new Course[0]);
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().put("first-course", course1);
        Utils.assertMap(
                student1.getCourses(),
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().put("second-course", course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course2.getStudents(), student1, student2);
        
        student2.getCourses().remove("second-course");
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().remove("first-course");
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents(), student1);
        
        student1.getCourses().remove("second-course");
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().remove("first-course");
        Utils.assertMap(
                student1.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course1.getStudents());
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
    }
    
    @Test
    public void testModifyStudentByValue() {
        Student student1 = new Student();
        Course course1 = new Course();
        Student student2 = new Student();
        Course course2 = new Course();
        Utils.assertMap(student1.getCourses(), new String[0], new Course[0]);
        Utils.assertCollection(course1.getStudents());
        Utils.assertMap(student2.getCourses(), new String[0], new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().put("first-course", course1);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(), 
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().put("second-course", course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0],
                new Course[0]);
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().put("first-course", course1);
        Utils.assertMap(
                student1.getCourses(),
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().put("second-course", course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course2.getStudents(), student1, student2);
        
        student2.getCourses().values().remove(course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().values().remove(course1);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents(), student1);
        
        student1.getCourses().values().remove(course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().values().remove(course1);
        Utils.assertMap(
                student1.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course1.getStudents());
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
    }
    
    @Test
    public void testModifyStudentByIterator() {
        Student student1 = new Student();
        Course course1 = new Course();
        Student student2 = new Student();
        Course course2 = new Course();
        Utils.assertMap(student1.getCourses(), new String[0], new Course[0]);
        Utils.assertCollection(course1.getStudents());
        Utils.assertMap(student2.getCourses(), new String[0], new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().put("first-course", course1);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(), 
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().put("second-course", course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0],
                new Course[0]);
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().put("first-course", course1);
        Utils.assertMap(
                student1.getCourses(),
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().put("second-course", course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course2.getStudents(), student1, student2);
        
        Utils.locateIterator(student2.getCourses(), "second-course").remove();
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course2.getStudents(), student1);
        
        Utils.locateIterator(student2.getCourses(), "first-course").remove();
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents(), student1);
        
        Utils.locateIterator(student1.getCourses(), "second-course").remove();
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        Utils.locateIterator(student1.getCourses(), "first-course").remove();
        Utils.assertMap(
                student1.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course1.getStudents());
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
    }
    
    @Test
    public void testModifyStudentByEntry() {
        
        Student student1 = new Student();
        Course course1 = new Course();
        Student student2 = new Student();
        Course course2 = new Course();
        Utils.assertMap(student1.getCourses(), new String[0], new Course[0]);
        Utils.assertCollection(course1.getStudents());
        Utils.assertMap(student2.getCourses(), new String[0], new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().put("first-course", course1);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(), 
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().put("second-course", course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0],
                new Course[0]);
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().put("first-course", course1);
        Utils.assertMap(
                student1.getCourses(),
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().put("second-course", course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course2.getStudents(), student1, student2);
        
        Utils.locateEntry(student2.getCourses(), "first-course").setValue(course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course2 });
        Utils.assertCollection(course2.getStudents(), student1, student2);
        
        Utils.locateEntry(student1.getCourses(), "second-course").setValue(course1);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "second-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course2 });
        Utils.assertCollection(course2.getStudents(), student2);
    }
    
    @Test
    public void testModifyCourse() {
        
        Student student1 = new Student();
        Course course1 = new Course();
        Student student2 = new Student();
        Course course2 = new Course();
        Utils.assertMap(student1.getCourses(), new String[0], new Course[0]);
        Utils.assertCollection(course1.getStudents());
        Utils.assertMap(student2.getCourses(), new String[0], new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().put("first-course", course1);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(), 
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().put("second-course", course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0],
                new Course[0]);
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().put("first-course", course1);
        Utils.assertMap(
                student1.getCourses(),
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().put("second-course", course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course2.getStudents(), student1, student2);
        
        course2.getStudents().remove(student2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course2.getStudents(), student1);
        
        course1.getStudents().remove(student2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents(), student1);
        
        course2.getStudents().remove(student1);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        course1.getStudents().remove(student1);
        Utils.assertMap(
                student1.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course1.getStudents());
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
    }
    
    @Test
    public void testModifyCourseByIterator() {
        
        Student student1 = new Student();
        Course course1 = new Course();
        Student student2 = new Student();
        Course course2 = new Course();
        Utils.assertMap(student1.getCourses(), new String[0], new Course[0]);
        Utils.assertCollection(course1.getStudents());
        Utils.assertMap(student2.getCourses(), new String[0], new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().put("first-course", course1);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(), 
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        student1.getCourses().put("second-course", course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0],
                new Course[0]);
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().put("first-course", course1);
        Utils.assertMap(
                student1.getCourses(),
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course2.getStudents(), student1);
        
        student2.getCourses().put("second-course", course2);
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course2.getStudents(), student1, student2);
        
        Utils.locateIterator(course2.getStudents(), student2).remove();
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1, student2);
        Utils.assertMap(
                student2.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course2.getStudents(), student1);
        
        Utils.locateIterator(course1.getStudents(), student2).remove();
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course", "second-course" }, 
                new Course[] { course1, course2 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents(), student1);
        
        Utils.locateIterator(course2.getStudents(), student1).remove();
        Utils.assertMap(
                student1.getCourses(), 
                new String[] { "first-course" }, 
                new Course[] { course1 });
        Utils.assertCollection(course1.getStudents(), student1);
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
        
        Utils.locateIterator(course1.getStudents(), student1).remove();
        Utils.assertMap(
                student1.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course1.getStudents());
        Utils.assertMap(
                student2.getCourses(),
                new String[0], 
                new Course[0]);
        Utils.assertCollection(course2.getStudents());
    }
}
