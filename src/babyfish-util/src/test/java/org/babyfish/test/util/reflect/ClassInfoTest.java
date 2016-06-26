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
package org.babyfish.test.util.reflect;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.babyfish.util.reflect.ClassInfo;
import org.babyfish.util.reflect.MethodInfo;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ClassInfoTest {
    
    @Test
    public void testTypeOfAWithoutTypeArguments() {
        ClassInfo<?> classInfo = ClassInfo.of(A.class);
        MethodInfo method = classInfo.getDeclaredErasedMethod(
                "f", Runnable.class, Runnable[].class);
        Assert.assertNotNull(method);
        Assert.assertSame(Runnable.class, method.getReturnType());
        Assert.assertSame(Runnable.class, method.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Runnable.class, Runnable[].class }, 
                method.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { Runnable.class, Runnable[].class }, 
                method.getResolvedParameterTypes().toArray()));
    }
    
    @Test
    public void testTypeOfAWithThreadAsItsParameter() {
        ClassInfo<?> classInfo = ClassInfo.of(A.class, Thread.class);
        MethodInfo method = classInfo.getDeclaredErasedMethod(
                "f", Runnable.class, Runnable[].class);
        Assert.assertNotNull(method);
        Assert.assertSame(Runnable.class, method.getReturnType());
        Assert.assertSame(Thread.class, method.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Runnable.class, Runnable[].class }, 
                method.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { Thread.class, Thread[].class }, 
                method.getResolvedParameterTypes().toArray()));
    } 
    
    @Test
    public void testTypeOfBWithoutTypeArguments() {
        ClassInfo<?> classInfo = ClassInfo.of(B.class);
        Assert.assertEquals(1, classInfo.getInterfaces().size());
        MethodInfo methodOfA = classInfo.getInterfaces().get(0).getDeclaredErasedMethod(
                "f", Runnable.class, Runnable[].class);
        MethodInfo methodOfB = classInfo.getDeclaredErasedMethod(
                "f", Thread.class, Thread[].class);
        Assert.assertNotNull(methodOfA);
        Assert.assertSame(Runnable.class, methodOfA.getReturnType());
        Assert.assertSame(Thread.class, methodOfA.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Runnable.class, Runnable[].class }, 
                methodOfA.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { Thread.class, Thread[].class }, 
                methodOfA.getResolvedParameterTypes().toArray()));
        Assert.assertNotNull(methodOfB);
        Assert.assertSame(Thread.class, methodOfB.getReturnType());
        Assert.assertSame(Thread.class, methodOfB.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Thread.class, Thread[].class }, 
                methodOfB.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { Thread.class, Thread[].class }, 
                methodOfB.getResolvedParameterTypes().toArray()));
    }
    
    @Test
    public void testTypeOfBWithThreadImplAsItsParameter() {
        ClassInfo<?> classInfo = ClassInfo.of(B.class, ThreadImpl.class);
        Assert.assertEquals(1, classInfo.getInterfaces().size());
        MethodInfo methodOfA = classInfo.getInterfaces().get(0).getDeclaredErasedMethod(
                "f", Runnable.class, Runnable[].class);
        MethodInfo methodOfB = classInfo.getDeclaredErasedMethod(
                "f", Thread.class, Thread[].class);
        Assert.assertNotNull(methodOfA);
        Assert.assertSame(Runnable.class, methodOfA.getReturnType());
        Assert.assertSame(ThreadImpl.class, methodOfA.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Runnable.class, Runnable[].class }, 
                methodOfA.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { ThreadImpl.class, ThreadImpl[].class }, 
                methodOfA.getResolvedParameterTypes().toArray()));
        Assert.assertNotNull(methodOfB);
        Assert.assertSame(Thread.class, methodOfB.getReturnType());
        Assert.assertSame(ThreadImpl.class, methodOfB.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Thread.class, Thread[].class }, 
                methodOfB.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { ThreadImpl.class, ThreadImpl[].class }, 
                methodOfB.getResolvedParameterTypes().toArray()));
    }
    
    @Test
    public void testTypeOfC() {
        ClassInfo<?> classInfo = ClassInfo.of(C.class);
        Assert.assertEquals(1, classInfo.getInterfaces().size());
        Assert.assertEquals(1, classInfo.getInterfaces().get(0).getInterfaces().size());
        MethodInfo methodOfA = classInfo.getInterfaces().get(0).getInterfaces().get(0)
            .getDeclaredErasedMethod("f", Runnable.class, Runnable[].class);
        MethodInfo methodOfB = classInfo.getInterfaces().get(0)
            .getDeclaredErasedMethod("f", Thread.class, Thread[].class);
        MethodInfo methodOfC = classInfo.getDeclaredErasedMethod(
                "f", ThreadImpl.class, ThreadImpl[].class);
        Assert.assertNotNull(methodOfA);
        Assert.assertSame(Runnable.class, methodOfA.getReturnType());
        Assert.assertSame(ThreadImpl.class, methodOfA.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Runnable.class, Runnable[].class }, 
                methodOfA.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { ThreadImpl.class, ThreadImpl[].class }, 
                methodOfA.getResolvedParameterTypes().toArray()));
        Assert.assertNotNull(methodOfB);
        Assert.assertSame(Thread.class, methodOfB.getReturnType());
        Assert.assertSame(ThreadImpl.class, methodOfB.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Thread.class, Thread[].class }, 
                methodOfB.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { ThreadImpl.class, ThreadImpl[].class }, 
                methodOfB.getResolvedParameterTypes().toArray()));
        Assert.assertNotNull(methodOfC);
        Assert.assertSame(ThreadImpl.class, methodOfC.getReturnType());
        Assert.assertSame(ThreadImpl.class, methodOfC.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { ThreadImpl.class, ThreadImpl[].class }, 
                methodOfC.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { ThreadImpl.class, ThreadImpl[].class }, 
                methodOfC.getResolvedParameterTypes().toArray()));
    }
    
    @Test
    public void testTypeOfHWithoutTypeArguments() {
        ClassInfo<?> classInfo = ClassInfo.of(H.class);
        MethodInfo method = classInfo.getDeclaredErasedMethod(
                "f", Object.class, Object[].class);
        Assert.assertNotNull(method);
        Assert.assertSame(Object[].class, method.getReturnType());
        Assert.assertSame(Object[].class, method.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object.class, Object[].class }, 
                method.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object.class, Object[].class }, 
                method.getResolvedParameterTypes().toArray()));
    }
    
    @Test
    public void testTypeOfHWithStringAsItsParameter() {
        ClassInfo<?> classInfo = ClassInfo.of(H.class, String.class);
        MethodInfo method = classInfo.getDeclaredErasedMethod(
                "f", Object.class, Object[].class);
        Assert.assertNotNull(method);
        Assert.assertSame(Object[].class, method.getReturnType());
        Assert.assertSame(String[].class, method.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object.class, Object[].class }, 
                method.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { String.class, String[].class }, 
                method.getResolvedParameterTypes().toArray()));
    }
    
    @Test
    public void testTypeOfHWithStringArrayAsItsParameter() {
        ClassInfo<?> classInfo = ClassInfo.of(H.class, String[].class);
        MethodInfo method = classInfo.getDeclaredErasedMethod(
                "f", Object.class, Object[].class);
        Assert.assertNotNull(method);
        Assert.assertSame(Object[].class, method.getReturnType());
        Assert.assertSame(String[][].class, method.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object.class, Object[].class }, 
                method.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { String[].class, String[][].class }, 
                method.getResolvedParameterTypes().toArray()));
    }
    
    @Test
    public void testTypeOfIWithoutTypeArguments() {
        ClassInfo<?> classInfo = ClassInfo.of(I.class);
        Assert.assertNotNull(classInfo.getSuperClass());
        MethodInfo methodOfH = classInfo.getSuperClass().getDeclaredErasedMethod(
                "f", Object.class, Object[].class);
        MethodInfo methodOfI = classInfo.getDeclaredErasedMethod(
                "f", Object[].class, Object[][].class);
        Assert.assertNotNull(methodOfH);
        Assert.assertSame(Object[].class, methodOfH.getReturnType());
        Assert.assertSame(Object[][].class, methodOfH.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object.class, Object[].class }, 
                methodOfH.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[].class, Object[][].class }, 
                methodOfH.getResolvedParameterTypes().toArray()));
        Assert.assertNotNull(methodOfI);
        Assert.assertSame(Object[][].class, methodOfI.getReturnType());
        Assert.assertSame(Object[][].class, methodOfI.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[].class, Object[][].class }, 
                methodOfI.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[].class, Object[][].class }, 
                methodOfI.getResolvedParameterTypes().toArray()));
    }
    
    @Test
    public void testTypeOfIWithStringAsItsParameter() {
        ClassInfo<?> classInfo = ClassInfo.of(I.class, String.class);
        Assert.assertNotNull(classInfo.getSuperClass());
        MethodInfo methodOfH = classInfo.getSuperClass().getDeclaredErasedMethod(
                "f", Object.class, Object[].class);
        MethodInfo methodOfI = classInfo.getDeclaredErasedMethod(
                "f", Object[].class, Object[][].class);
        Assert.assertNotNull(methodOfH);
        Assert.assertSame(Object[].class, methodOfH.getReturnType());
        Assert.assertSame(String[][].class, methodOfH.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object.class, Object[].class }, 
                methodOfH.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { String[].class, String[][].class }, 
                methodOfH.getResolvedParameterTypes().toArray()));
        Assert.assertNotNull(methodOfI);
        Assert.assertSame(Object[][].class, methodOfI.getReturnType());
        Assert.assertSame(String[][].class, methodOfI.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[].class, Object[][].class }, 
                methodOfI.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { String[].class, String[][].class }, 
                methodOfI.getResolvedParameterTypes().toArray()));
    }
    
    @Test
    public void testTypeOfIWithStringArrayAsItsParameter() {
        ClassInfo<?> classInfo = ClassInfo.of(I.class, String[].class);
        Assert.assertNotNull(classInfo.getSuperClass());
        MethodInfo methodOfH = classInfo.getSuperClass().getDeclaredErasedMethod(
                "f", Object.class, Object[].class);
        MethodInfo methodOfI = classInfo.getDeclaredErasedMethod(
                "f", Object[].class, Object[][].class);
        Assert.assertNotNull(methodOfH);
        Assert.assertSame(Object[].class, methodOfH.getReturnType());
        Assert.assertSame(String[][][].class, methodOfH.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object.class, Object[].class }, 
                methodOfH.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { String[][].class, String[][][].class }, 
                methodOfH.getResolvedParameterTypes().toArray()));
        Assert.assertNotNull(methodOfI);
        Assert.assertSame(Object[][].class, methodOfI.getReturnType());
        Assert.assertSame(String[][][].class, methodOfI.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[].class, Object[][].class }, 
                methodOfI.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { String[][].class, String[][][].class }, 
                methodOfI.getResolvedParameterTypes().toArray()));
    }
    
    @Test
    public void testTypeOfJWithoutTypeArguments() {
        ClassInfo<?> classInfo = ClassInfo.of(J.class);
        Assert.assertNotNull(classInfo.getSuperClass());
        Assert.assertNotNull(classInfo.getSuperClass().getSuperClass());
        MethodInfo methodOfH = classInfo.getSuperClass().getSuperClass().getDeclaredErasedMethod(
                "f", Object.class, Object[].class);
        MethodInfo methodOfI = classInfo.getSuperClass().getDeclaredErasedMethod(
                "f", Object[].class, Object[][].class);
        MethodInfo methodOfJ = classInfo.getDeclaredErasedMethod(
                "f", Object[][].class, Object[][][].class);
        Assert.assertNotNull(methodOfH);
        Assert.assertSame(Object[].class, methodOfH.getReturnType());
        Assert.assertSame(Object[][][].class, methodOfH.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object.class, Object[].class }, 
                methodOfH.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[][].class, Object[][][].class }, 
                methodOfH.getResolvedParameterTypes().toArray()));
        Assert.assertNotNull(methodOfI);
        Assert.assertSame(Object[][].class, methodOfI.getReturnType());
        Assert.assertSame(Object[][][].class, methodOfI.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[].class, Object[][].class }, 
                methodOfI.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[][].class, Object[][][].class }, 
                methodOfI.getResolvedParameterTypes().toArray()));
        Assert.assertNotNull(methodOfJ);
        Assert.assertSame(Object[][][].class, methodOfJ.getReturnType());
        Assert.assertSame(Object[][][].class, methodOfJ.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[][].class, Object[][][].class }, 
                methodOfJ.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[][].class, Object[][][].class }, 
                methodOfJ.getResolvedParameterTypes().toArray()));
    }
    
    @Test
    public void testTypeOfJWithStringAsItsParameter() {
        ClassInfo<?> classInfo = ClassInfo.of(J.class, String.class);
        Assert.assertNotNull(classInfo.getSuperClass());
        Assert.assertNotNull(classInfo.getSuperClass().getSuperClass());
        MethodInfo methodOfH = classInfo.getSuperClass().getSuperClass().getDeclaredErasedMethod(
                "f", Object.class, Object[].class);
        MethodInfo methodOfI = classInfo.getSuperClass().getDeclaredErasedMethod(
                "f", Object[].class, Object[][].class);
        MethodInfo methodOfJ = classInfo.getDeclaredErasedMethod(
                "f", Object[][].class, Object[][][].class);
        Assert.assertNotNull(methodOfH);
        Assert.assertSame(Object[].class, methodOfH.getReturnType());
        Assert.assertSame(String[][][].class, methodOfH.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object.class, Object[].class }, 
                methodOfH.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { String[][].class, String[][][].class }, 
                methodOfH.getResolvedParameterTypes().toArray()));
        Assert.assertNotNull(methodOfI);
        Assert.assertSame(Object[][].class, methodOfI.getReturnType());
        Assert.assertSame(String[][][].class, methodOfI.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[].class, Object[][].class }, 
                methodOfI.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { String[][].class, String[][][].class }, 
                methodOfI.getResolvedParameterTypes().toArray()));
        Assert.assertNotNull(methodOfJ);
        Assert.assertSame(Object[][][].class, methodOfJ.getReturnType());
        Assert.assertSame(String[][][].class, methodOfJ.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[][].class, Object[][][].class }, 
                methodOfJ.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { String[][].class, String[][][].class }, 
                methodOfJ.getResolvedParameterTypes().toArray()));
    }
    
    @Test
    public void testTypeOfJWithStringArrayAsItsParameter() {
        ClassInfo<?> classInfo = ClassInfo.of(J.class, String[].class);
        Assert.assertNotNull(classInfo.getSuperClass());
        Assert.assertNotNull(classInfo.getSuperClass().getSuperClass());
        MethodInfo methodOfH = classInfo.getSuperClass().getSuperClass().getDeclaredErasedMethod(
                "f", Object.class, Object[].class);
        MethodInfo methodOfI = classInfo.getSuperClass().getDeclaredErasedMethod(
                "f", Object[].class, Object[][].class);
        MethodInfo methodOfJ = classInfo.getDeclaredErasedMethod(
                "f", Object[][].class, Object[][][].class);
        Assert.assertNotNull(methodOfH);
        Assert.assertSame(Object[].class, methodOfH.getReturnType());
        Assert.assertSame(String[][][][].class, methodOfH.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object.class, Object[].class }, 
                methodOfH.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { String[][][].class, String[][][][].class }, 
                methodOfH.getResolvedParameterTypes().toArray()));
        Assert.assertNotNull(methodOfI);
        Assert.assertSame(Object[][].class, methodOfI.getReturnType());
        Assert.assertSame(String[][][][].class, methodOfI.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[].class, Object[][].class }, 
                methodOfI.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { String[][][].class, String[][][][].class }, 
                methodOfI.getResolvedParameterTypes().toArray()));
        Assert.assertNotNull(methodOfJ);
        Assert.assertSame(Object[][][].class, methodOfJ.getReturnType());
        Assert.assertSame(String[][][][].class, methodOfJ.getResolvedReturnType());
        Assert.assertTrue(Arrays.equals(
                new Class[] { Object[][].class, Object[][][].class }, 
                methodOfJ.getParameterTypes().toArray()));
        Assert.assertTrue(Arrays.equals(
                new Class[] { String[][][].class, String[][][][].class }, 
                methodOfJ.getResolvedParameterTypes().toArray()));
    }
    
    @Test
    public void testRuntimeGenericTypeOfQWithoutTypeParameter() {
        ClassInfo<?> classInfo = ClassInfo.of(Q.class)
            .getInterfaces().iterator().next()
            .getInterfaces().iterator().next();
        Assert.assertNotNull(classInfo);
        MethodInfo method = classInfo.getDeclaredErasedMethod(
                "f", Object[].class);
        Assert.assertNotNull(method);
        Assert.assertSame(
                Map[][].class,
                method.getResolvedParameterTypes().iterator().next());
        Assert.assertSame(
                Map[].class,
                method.getResolvedReturnType());
        Assert.assertEquals(
                "java.util.Map<T[], java.util.Collection<? extends T>>[][]",
                method.getResolvedGenericParameterTypes().iterator().next().toString());
        Assert.assertEquals(
                "java.util.Map<T[], java.util.Collection<? extends T>>[]",
                method.getResolvedGenericReturnType().toString());
    }
    
    @Test
    public void testRuntimeGenericTypeOfQWithStringAsItsParameter() {
        ClassInfo<?> classInfo = ClassInfo.of(Q.class, String.class)
            .getInterfaces().iterator().next()
            .getInterfaces().iterator().next();
        Assert.assertNotNull(classInfo);
        MethodInfo method = classInfo.getDeclaredErasedMethod(
                "f", Object[].class);
        Assert.assertNotNull(method);
        Assert.assertSame(
                Map[][].class,
                method.getResolvedParameterTypes().iterator().next());
        Assert.assertSame(
                Map[].class,
                method.getResolvedReturnType());
        Assert.assertEquals(
                "java.util.Map<[Ljava.lang.String;, java.util.Collection<? extends java.lang.String>>[][]",
                method.getResolvedGenericParameterTypes().iterator().next().toString());
        Assert.assertEquals(
                "java.util.Map<[Ljava.lang.String;, java.util.Collection<? extends java.lang.String>>[]",
                method.getResolvedGenericReturnType().toString());
    }
    
    @Test
    public void testRuntimeGenericTypeOfQWithStringArrayAsItsParameter() {
        ClassInfo<?> classInfo = ClassInfo.of(Q.class, String[].class)
            .getInterfaces().iterator().next()
            .getInterfaces().iterator().next();
        Assert.assertNotNull(classInfo);
        MethodInfo method = classInfo.getDeclaredErasedMethod(
                "f", Object[].class);
        Assert.assertNotNull(method);
        Assert.assertSame(
                Map[][].class,
                method.getResolvedParameterTypes().iterator().next());
        Assert.assertSame(
                Map[].class,
                method.getResolvedReturnType());
        Assert.assertEquals(
                "java.util.Map<[[Ljava.lang.String;, java.util.Collection<? extends [Ljava.lang.String;>>[][]",
                method.getResolvedGenericParameterTypes().iterator().next().toString());
        Assert.assertEquals(
                "java.util.Map<[[Ljava.lang.String;, java.util.Collection<? extends [Ljava.lang.String;>>[]",
                method.getResolvedGenericReturnType().toString());
    }
    
    @Test
    public void testNestedClass1WithoutParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_1.Inner_1.class);
        ClassInfo<?> outer = inner.getSuperClass();
        Assert.assertEquals(outer.getRawClass(), inner.getDeclaringClass().getRawClass());
        MethodInfo methodF = outer.getDeclaredErasedMethod("f", Object[].class);
        Assert.assertEquals(Object[][].class, methodF.getResolvedReturnType());
        assertList(methodF.getResolvedParameterTypes(), Object[][].class);
    }
    
    @Test
    public void testNestedClass1WithParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_1.Inner_1.class, String.class);
        ClassInfo<?> outer = inner.getSuperClass();
        Assert.assertEquals(outer.getRawClass(), inner.getDeclaringClass().getRawClass());
        MethodInfo methodF = outer.getDeclaredErasedMethod("f", Object[].class);
        Assert.assertEquals(String[][].class, methodF.getResolvedReturnType());
        assertList(methodF.getResolvedParameterTypes(), String[][].class);
    }
    
    @Test
    public void testNestedClass2WithoutParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_2.Inner_2.class);
        ClassInfo<?> outer = inner.getSuperClass();
        Assert.assertEquals(outer.getRawClass(), inner.getDeclaringClass().getRawClass());
        MethodInfo methodF = outer.getDeclaredErasedMethod("f", Object[].class);
        Assert.assertEquals(Object[][].class, methodF.getResolvedReturnType());
        assertList(methodF.getResolvedParameterTypes(), Object[][].class);
    }
    
    @Test
    public void testNestedClass2WithParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_2.Inner_2.class, String.class);
        ClassInfo<?> outer = inner.getSuperClass();
        Assert.assertEquals(outer.getRawClass(), inner.getDeclaringClass().getRawClass());
        MethodInfo methodF = outer.getDeclaredErasedMethod("f", Object[].class);
        Assert.assertEquals(String[][].class, methodF.getResolvedReturnType());
        assertList(methodF.getResolvedParameterTypes(), String[][].class);
    }
    
    @Test
    public void testNestedClass3WithoutParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_3.Inner_3.class);
        ClassInfo<?> outer = inner.getSuperClass();
        Assert.assertSame(outer.getRawClass(), inner.getDeclaringClass().getRawClass());
        
        MethodInfo methodF = outer.getDeclaredErasedMethod("f", List.class);
        Assert.assertEquals(
                "java.util.List<T>", 
                methodF.getGenericReturnType().toString());
        Assert.assertEquals(
                "java.util.List<T>", 
                methodF.getGenericParameterTypes().get(0).toString());
        Assert.assertEquals(
                "java.util.List<java.util.List<T>>", 
                methodF.getResolvedGenericReturnType().toString());
        Assert.assertEquals(
                "java.util.List<java.util.List<T>>", 
                methodF.getResolvedGenericParameterTypes().get(0).toString());
    }
    
    @Test
    public void testNestedClass3WithParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_3.Inner_3.class, String.class);
        ClassInfo<?> outer = inner.getSuperClass();
        Assert.assertSame(outer.getRawClass(), inner.getDeclaringClass().getRawClass());
        
        MethodInfo methodF = outer.getDeclaredErasedMethod("f", List.class);
        Assert.assertEquals(
                "java.util.List<T>", 
                methodF.getGenericReturnType().toString());
        Assert.assertEquals(
                "java.util.List<T>", 
                methodF.getGenericParameterTypes().get(0).toString());
        Assert.assertEquals(
                "java.util.List<java.util.List<java.lang.String>>", 
                methodF.getResolvedGenericReturnType().toString());
        Assert.assertEquals(
                "java.util.List<java.util.List<java.lang.String>>", 
                methodF.getResolvedGenericParameterTypes().get(0).toString());
    }
    
    @Test
    public void testNestedClass4WithoutParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_4.Inner_4.class);
        ClassInfo<?> outer = inner.getSuperClass();
        Assert.assertEquals(outer.getRawClass(), inner.getDeclaringClass().getRawClass());
        
        MethodInfo methodF = outer.getDeclaredErasedMethod(
                "f", Object[].class, Object[].class);
        Assert.assertEquals(
                "T2[]", methodF.getGenericReturnType().toString());
        Assert.assertEquals(
                "T1[]", methodF.getGenericParameterTypes().get(0).toString());
        Assert.assertEquals(
                "T2[]", methodF.getGenericParameterTypes().get(1).toString());
        
        Assert.assertEquals(
                "T3[][]", 
                methodF.getResolvedGenericReturnType().toString());
        Assert.assertEquals(
                "T1[][]", methodF.getResolvedGenericParameterTypes().get(0).toString());
        Assert.assertEquals(
                "T3[][]", methodF.getResolvedGenericParameterTypes().get(1).toString());
        
        MethodInfo methodG = inner.getDeclaredErasedMethod(
                "g", Object[].class, Object[].class, Object[][][].class);
        Assert.assertEquals(
                Outer_4.class.getName() +
                '.' +
                Outer_4.ReturnTypeOfG.class.getName() +
                "<T3[], T2[][], T1[][][]>", 
                methodG.getResolvedGenericReturnType().toString());
        Assert.assertEquals(
                "T1[]", 
                methodG.getResolvedGenericParameterTypes().get(0).toString());
        Assert.assertEquals(
                "T2[]", 
                methodG.getResolvedGenericParameterTypes().get(1).toString());
        Assert.assertEquals(
                "T3[][][]", 
                methodG.getResolvedGenericParameterTypes().get(2).toString());
    }
    
    @Test
    public void testNestedClass4WithParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_4.Inner_4.class, Integer.class, Long.class, Float.class);
        ClassInfo<?> outer = inner.getSuperClass();
        Assert.assertEquals(outer.getRawClass(), inner.getDeclaringClass().getRawClass());
        
        MethodInfo methodF = outer.getDeclaredErasedMethod(
                "f", Object[].class, Object[].class);
        Assert.assertEquals(
                "T2[]", methodF.getGenericReturnType().toString());
        Assert.assertEquals(
                "T1[]", methodF.getGenericParameterTypes().get(0).toString());
        Assert.assertEquals(
                "T2[]", methodF.getGenericParameterTypes().get(1).toString());
        
        Assert.assertEquals(
                "class [[Ljava.lang.Float;", 
                methodF.getResolvedGenericReturnType().toString());
        Assert.assertEquals(
                "class [[Ljava.lang.Integer;", methodF.getResolvedGenericParameterTypes().get(0).toString());
        Assert.assertEquals(
                "class [[Ljava.lang.Float;", methodF.getResolvedGenericParameterTypes().get(1).toString());
        
        MethodInfo methodG = inner.getDeclaredErasedMethod(
                "g", Object[].class, Object[].class, Object[][][].class);
        Assert.assertEquals(
                Outer_4.class.getName() +
                '.' +
                Outer_4.ReturnTypeOfG.class.getName() +
                "<[Ljava.lang.Float;, [[Ljava.lang.Long;, [[[Ljava.lang.Integer;>", 
                methodG.getResolvedGenericReturnType().toString());
        Assert.assertEquals(
                "class [Ljava.lang.Integer;", 
                methodG.getResolvedGenericParameterTypes().get(0).toString());
        Assert.assertEquals(
                "class [Ljava.lang.Long;", 
                methodG.getResolvedGenericParameterTypes().get(1).toString());
        Assert.assertEquals(
                "class [[[Ljava.lang.Float;", 
                methodG.getResolvedGenericParameterTypes().get(2).toString());
    }
    
    @Test
    public void testNestedClass5WithoutParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_5.Inner_5.class);
        MethodInfo methodReverse = inner.getSuperClass().getDeclaredErasedMethod(
                "reverse", Map.class);
        Assert.assertEquals(
                "java.util.Map<T2, T1>", 
                methodReverse.getGenericReturnType().toString());
        Assert.assertEquals(
                "java.util.Map<T1, T2>", 
                methodReverse.getGenericParameterTypes().get(0).toString());
        Assert.assertEquals(
                "java.util.Map<T1[], T2[]>", 
                methodReverse.getResolvedGenericReturnType().toString());
        Assert.assertEquals(
                "java.util.Map<T2[], T1[]>", 
                methodReverse.getResolvedGenericParameterTypes().get(0).toString());
    }
    
    @Test
    public void testNestedClass5WithParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_5.Inner_5.class, Integer.class, Float.class);
        MethodInfo methodReverse = inner.getSuperClass().getDeclaredErasedMethod(
                "reverse", Map.class);
        Assert.assertEquals(
                "java.util.Map<T2, T1>", 
                methodReverse.getGenericReturnType().toString());
        Assert.assertEquals(
                "java.util.Map<T1, T2>", 
                methodReverse.getGenericParameterTypes().get(0).toString());
        Assert.assertEquals(
                "java.util.Map<[Ljava.lang.Integer;, [Ljava.lang.Float;>", 
                methodReverse.getResolvedGenericReturnType().toString());
        Assert.assertEquals(
                "java.util.Map<[Ljava.lang.Float;, [Ljava.lang.Integer;>", 
                methodReverse.getResolvedGenericParameterTypes().get(0).toString());
    }
    
    @Test
    public void testNestedClass6WithoutParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_6.Inner_6.class);
        MethodInfo methodReverse = inner.getSuperClass().getDeclaredErasedMethod(
                "reverse", Map.class);
        Assert.assertEquals(
                "java.util.Map<T2, T1>", 
                methodReverse.getGenericReturnType().toString());
        Assert.assertEquals(
                "java.util.Map<T1, T2>", 
                methodReverse.getGenericParameterTypes().get(0).toString());
        Assert.assertEquals(
                "java.util.Map<java.util.List<T1>, java.util.List<T2>>", 
                methodReverse.getResolvedGenericReturnType().toString());
        Assert.assertEquals(
                "java.util.Map<java.util.List<T2>, java.util.List<T1>>", 
                methodReverse.getResolvedGenericParameterTypes().get(0).toString());
    }
    
    @Test
    public void testNestedClass6WithParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_6.Inner_6.class, Integer.class, Float.class);
        MethodInfo methodReverse = inner.getSuperClass().getDeclaredErasedMethod(
                "reverse", Map.class);
        Assert.assertEquals(
                "java.util.Map<T2, T1>", 
                methodReverse.getGenericReturnType().toString());
        Assert.assertEquals(
                "java.util.Map<T1, T2>", 
                methodReverse.getGenericParameterTypes().get(0).toString());
        Assert.assertEquals(
                "java.util.Map<java.util.List<java.lang.Integer>, java.util.List<java.lang.Float>>", 
                methodReverse.getResolvedGenericReturnType().toString());
        Assert.assertEquals(
                "java.util.Map<java.util.List<java.lang.Float>, java.util.List<java.lang.Integer>>", 
                methodReverse.getResolvedGenericParameterTypes().get(0).toString());
    }
    
    @Test
    public void testNestedClass7WithoutParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_7.Inner_7_A.Inner_7_C.class);
        MethodInfo methodF = inner.getSuperClass().getSuperClass().getSuperClass().getDeclaredErasedMethod(
                "f", Object[][].class);
        Assert.assertEquals(
                "T[] org.babyfish.test.util.reflect.ClassInfoTest$Outer_7<T>.f(T[][])", 
                methodF.toGenericString());
        Assert.assertEquals(
                "T[][][][] org.babyfish.test.util.reflect.ClassInfoTest.Outer_7<T[][][]>.f(T[][][][][])", 
                methodF.toResolvedGenericString());
    }
    
    @Test
    public void testNestedClass7WithParameter() {
        ClassInfo<?> inner = ClassInfo.of(Outer_7.Inner_7_A.Inner_7_C.class, String.class, Runnable.class);
        MethodInfo methodF = inner.getSuperClass().getSuperClass().getSuperClass().getDeclaredErasedMethod(
                "f", Object[][].class);
        Assert.assertEquals(
                "T[] " +
                "org.babyfish.test.util.reflect.ClassInfoTest$Outer_7<T>" +
                ".f(T[][])", 
                methodF.toGenericString());
        Assert.assertEquals(
                "java.lang.String[][][][] " +
                "org.babyfish.test.util.reflect.ClassInfoTest.Outer_7<java.lang.String[][][]>" +
                ".f(java.lang.String[][][][][])", 
                methodF.toResolvedGenericString());
    }
    
    @Test
    public void testXAndItsNestedClassWithoutParameters() {
        
        ClassInfo<?> classInfo_X = ClassInfo.of(X.class);
        
        MethodInfo method_1_InDerivedClass =
            classInfo_X
            .getDeclaredNestedClass("_A")
            .getDeclaredNestedClass("_D")
            .getDeclaredNestedClass("_E")
            .getDeclaredNestedClass("__C")
            .getDeclaredNestedClass("__D")
            .getDeclaredErasedMethod(
                "method_1", Object[][].class);
        
        MethodInfo method_1 = 
            method_1_InDerivedClass
            .getDeclaringClass()
            .getSuperClass()
            .getDeclaredErasedMethod(
                "method_1", Object[].class);
        
        MethodInfo method_2_InDerivedClass = 
            classInfo_X
            .getDeclaredNestedClass("_A")
            .getDeclaredNestedClass("__E")
            .getDeclaredErasedMethod("method_2", Map[].class);
        
        MethodInfo method_2 = 
            method_2_InDerivedClass
            .getDeclaringClass()
            .getSuperClass()
            .getDeclaredErasedMethod("method_2", Map[].class);
        
        MethodInfo method_3_InDerivedClass =
            classInfo_X
            .getDeclaredNestedClass("A_")
            .getDeclaredNestedClass("B_")
            .getDeclaredNestedClass("C_")
            .getDeclaredNestedClass("D_")
            .getDeclaredNestedClass("E_")
            .getDeclaredNestedClass("F_")
            .getDeclaredNestedClass("G_")
            .getDeclaredErasedMethod(
                    "method_3", 
                    
                            Object[].class, 
                            Object[][].class, 
                            List[].class, 
                            List[].class);
        
        MethodInfo method_3 =
            method_3_InDerivedClass
            .getDeclaringClass()
            .getSuperClass()
            .getDeclaredErasedMethod(
                    "method_3", 
                    
                            Object.class, 
                            Object[].class, 
                            List[].class, 
                            List[].class);
        
        MethodInfo method_4_InDerivedClass =
            classInfo_X
            .getDeclaredNestedClass("_A")
            .getDeclaredNestedClass("_D")
            .getDeclaredNestedClass("_E")
            .getDeclaredNestedClass("__C")
            .getDeclaredNestedClass("__D")
            .getDeclaredErasedMethod("method_4", Map[].class);
        
        MethodInfo method_4 =
            method_4_InDerivedClass
            .getDeclaringClass()
            .getSuperClass()
            .getSuperClass()
            .getSuperClass()
            .getSuperClass()
            .getDeclaredErasedMethod("method_4", Map[].class);
        
        MethodInfo method_5_InDerivedClass = 
            classInfo_X
            .getDeclaredNestedClass("_A")
            .getDeclaredNestedClass("_D")
            .getDeclaredNestedClass("_E")
            .getDeclaredNestedClass("__C")
            .getDeclaredNestedClass("__D")
            .getDeclaredErasedMethod("method_5", Object[][][][][].class);
        
        MethodInfo method_5 =
            method_5_InDerivedClass
            .getDeclaringClass()
            .getSuperClass()
            .getSuperClass()
            .getSuperClass()
            .getSuperClass()
            .getSuperClass()
            .getDeclaredErasedMethod("method_5", Object.class);
        
        Assert.assertFalse(method_1_InDerivedClass.getModifiers().isBridge());
        Assert.assertFalse(method_2_InDerivedClass.getModifiers().isBridge());
        Assert.assertFalse(method_3_InDerivedClass.getModifiers().isBridge());
        Assert.assertFalse(method_4_InDerivedClass.getModifiers().isBridge());
        Assert.assertFalse(method_5_InDerivedClass.getModifiers().isBridge());
        
        Assert.assertFalse(
                method_1.getGenericParameterTypes().equals(
                        method_1_InDerivedClass.getGenericParameterTypes()));
        Assert.assertFalse(
                method_2.getGenericParameterTypes().equals(
                        method_2_InDerivedClass.getGenericParameterTypes()));
        Assert.assertFalse(
                method_3.getGenericParameterTypes().equals(
                        method_3_InDerivedClass.getGenericParameterTypes()));
        Assert.assertFalse(
                method_4.getGenericParameterTypes().equals(
                        method_4_InDerivedClass.getGenericParameterTypes()));
        Assert.assertFalse(
                method_5.getGenericParameterTypes().equals(
                        method_5_InDerivedClass.getGenericParameterTypes()));
        
        Assert.assertEquals(
                method_1.getResolvedGenericParameterTypes(), 
                method_1_InDerivedClass.getResolvedGenericParameterTypes());
        Assert.assertEquals(
                method_2.getResolvedGenericParameterTypes(), 
                method_2_InDerivedClass.getResolvedGenericParameterTypes());
        Assert.assertEquals(
                method_3.getResolvedGenericParameterTypes(), 
                method_3_InDerivedClass.getResolvedGenericParameterTypes());
        Assert.assertEquals(
                method_4.getResolvedGenericParameterTypes(), 
                method_4_InDerivedClass.getResolvedGenericParameterTypes());
        Assert.assertEquals(
                method_5.getResolvedGenericParameterTypes(), 
                method_5_InDerivedClass.getResolvedGenericParameterTypes());
        
        Assert.assertEquals(
                "java.util.Map " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X$_A$_D$_E$__C" +
                ".method_1(java.lang.Object[])",
                method_1.toString());
        Assert.assertEquals(
                "java.util.Map<T6[], T6[][]> " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X$_A$_D$_E<T6>.__C<T9>" +
                ".method_1(T9[])",
                method_1.toGenericString());
        Assert.assertEquals(
                "java.util.Map " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X$_A$_D._E<java.lang.Object[]>.__C<java.lang.Object[]>" +
                ".method_1(java.lang.Object[][])",
                method_1.toResolvedString());
        Assert.assertEquals(
                "java.util.Map<T10[][], T10[][][]> " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X$_A$_D._E<T10[]>.__C<T9[]>" +
                ".method_1(T9[][])",
                method_1.toResolvedGenericString());

        Assert.assertEquals(
                "java.util.Map " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X$A_$B_" +
                ".method_2(java.util.Map[])",
                method_2.toString());
        Assert.assertEquals(
                "java.util.Map<T50, java.util.Map<T50[], java.util.Collection<? extends T51>>> " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X<T1>.A_<T50>.B_<T51>" +
                ".method_2(java.util.Map<? super T51, java.util.Collection<? super T50>[]>[])",
                method_2.toGenericString());
        Assert.assertEquals(
                "java.util.Map " +
                "org.babyfish.test.util.reflect.ClassInfoTest.X<java.lang.Object[]>.A_<java.lang.Object[]>.B_<java.lang.String[]>" +
                ".method_2(java.util.Map[])",
                method_2.toResolvedString());
        Assert.assertEquals(
                "java.util.Map<T11[], java.util.Map<T11[][], java.util.Collection<? extends java.lang.String[]>>> " +
                "org.babyfish.test.util.reflect.ClassInfoTest.X<T2[]>.A_<T11[]>.B_<java.lang.String[]>" +
                ".method_2(java.util.Map<? super java.lang.String[], java.util.Collection<? super T11[]>[]>[])",
                method_2.toResolvedGenericString());

        Assert.assertEquals(
                "java.util.Map " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X$A_$B_$C_$D_$E_" +
                ".method_3(java.lang.Object,java.lang.Object[],java.util.List[],java.util.List[])",
                method_3.toString());
        Assert.assertEquals(
                "java.util.Map<T50[], java.util.Map<T51[], T52[][]>[][][]> " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X<T1>.A_<T50>.B_<T51>.C_<T52>.D_<T53>.E_<T54>" +
                ".method_3(T53, T53[], java.util.List<T53[]>[], java.util.List<? super T54>[])",
                method_3.toGenericString());
        Assert.assertEquals(
                "java.util.Map " +
                "org.babyfish.test.util.reflect.ClassInfoTest.X<java.lang.Object>.A_<java.lang.Object[][]>.B_<java.lang.Object[]>.C_<java.lang.Object>.D_<java.lang.Object[]>.E_<java.lang.Object[]>" +
                ".method_3(java.lang.Object[], java.lang.Object[][], java.util.List[], java.util.List[])",
                method_3.toResolvedString());
        Assert.assertEquals(
                "java.util.Map<T56[][][], java.util.Map<T53[][], T56[][]>[][][]> " +
                "org.babyfish.test.util.reflect.ClassInfoTest.X<T1>.A_<T56[][]>.B_<T53[]>.C_<T56>.D_<T56[]>.E_<T50[]>" +
                ".method_3(T56[], T56[][], java.util.List<T56[][]>[], java.util.List<? super T50[]>[])",
                method_3.toResolvedGenericString());

        Assert.assertEquals(
                "java.lang.Object[] " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X$_A$_B" +
                ".method_4(java.util.Map[])",
                method_4.toString());
        Assert.assertEquals(
                "T3[] " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X$_A$_B<T3>" +
                ".method_4(java.util.Map<? super T3, ? extends T3>[])",
                method_4.toGenericString());
        Assert.assertEquals(
                "java.lang.Object[][][][][] " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X$_A._B<java.lang.Object[][][][]>" +
                ".method_4(java.util.Map[])",
                method_4.toResolvedString());
        Assert.assertEquals(
                "T9[][][][][] " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X$_A._B<T9[][][][]>" +
                ".method_4(java.util.Map<? super T9[][][][], ? extends T9[][][][]>[])",
                method_4.toResolvedGenericString());

        Assert.assertEquals(
                "void " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X" +
                ".method_5(java.lang.Object)",
                method_5.toString());
        Assert.assertEquals(
                "void " +
                "org.babyfish.test.util.reflect.ClassInfoTest$X<T1>" +
                ".method_5(T1)",
                method_5.toGenericString());
        Assert.assertEquals(
                "void " +
                "org.babyfish.test.util.reflect.ClassInfoTest.X<java.lang.Object[][][][][]>" +
                ".method_5(java.lang.Object[][][][][])",
                method_5.toResolvedString());
        Assert.assertEquals(
                "void " +
                "org.babyfish.test.util.reflect.ClassInfoTest.X<T9[][][][][]>" +
                ".method_5(T9[][][][][])",
                method_5.toResolvedGenericString());
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertList(List<E> list, E ... elements) {
        Assert.assertEquals(elements.length, list.size());
        int index = 0;
        for (E e : list) {
            Assert.assertEquals(elements[index++], e);
        }
    }
    
    private static class ThreadImpl extends Thread {
        
    }

    private interface A<T extends Runnable> {
        T f(T a, T[] b);
    }
    
    private interface B<T extends Thread> extends A<T> {
        T f(T a, T[] b);
    }
    
    private interface C extends B<ThreadImpl> {
        ThreadImpl f(ThreadImpl a, ThreadImpl[] b);
    }
    
    private static abstract class H<T> {
        
        abstract T[] f(T a, T[] b);
    } 
    
    private static abstract class I<T> extends H<T[]> {

        @Override
        abstract T[][] f(T[] a, T[][] value);
    }
    
    private static abstract class J<T> extends I<T[]> {

        @Override
        abstract T[][][] f(T[][] a, T[][][] value);
    }
    
    private interface O<T> {
        T f(T[] o);
    }
    
    private interface P<T> extends O<T[]> {
        
    }
    
    private interface Q<T> extends P<Map<T[], Collection<? extends T>>> {
        
    }
    
    static class Outer_1<T> {
        
        T[] f(T[] args) { return null; }
        
        static class Inner_1<T> extends Outer_1<T[]> {
            
        }
        
    }
    
    static class Outer_2<T> {
        
        T[] f(T[] args) { return null; }
        
        class Inner_2 extends Outer_2<T[]> {
            
        }
        
    }
    
    static class Outer_3<T> {
        
        List<T> f(List<T> args) { return null; }
        
        class Inner_3 extends Outer_3<List<T>> {
            
        }
        
    }
    
    static class Outer_4<T1, T2> {
        
        T2[] f(T1[] a, T2[] b) { return null; }
        
        class Inner_4<T3> extends Outer_4<T1[], T3[]> {
            
            ReturnTypeOfG<T3[], T2[][], T1[][][]> g(T1[] a, T2[] b, T3[][][] c) {
                return null;
            }
            
        }
        
        static class ReturnTypeOfG<T1, T2, T3> {
            
        }
        
    }
    
    static class Outer_5<T1, T2> {
        
        Map<T2, T1> reverse(Map<T1, T2> map) { return null; }
        
        class Inner_5 extends Outer_5<T2[], T1[]> {
            
        }
        
    }
    
    static class Outer_6<T1, T2> {
        
        Map<T2, T1> reverse(Map<T1, T2> map) { return null; }
        
        class Inner_6 extends Outer_6<List<T2>, List<T1>> {
            
        }
        
    }
    
    static class Outer_7<T> {
        
        T[] f(T[][] arg) {
            return null;
        }
        
        class Inner_7_A<T2> extends Outer_7<T2[]> {
            
            @Override
            T2[][] f(T2[][][] arg) {
                return super.f(arg);
            }

            class Inner_7_C extends Inner_7_B<T[]> {

                Inner_7_C(Outer_7<T2[]> owner, Inner_7_A<T2> owner2) {
                    owner2.super(owner);
                }

                @Override
                T[][][][] f(T[][][][][] arg) {
                    return super.f(arg);
                }
                
            }
        }
        
        class Inner_7_B<T3> extends Inner_7_A<T3[]> {
            
            Inner_7_B(Outer_7<T> owner) {
                owner.super();
            }

            @Override
            T3[][][] f(T3[][][][] arg) {
                return super.f(arg);
            }
            
        }
    }
    
    static class X<T1> {
        void method_5(T1 a) { }
        static class _A<T2> extends X<T2[]> {
            static class _B<T3> extends X<T3[]> {
                
                T3[] method_4(Map<? super T3, ? extends T3>[] a) {
                    return null;
                }
                
            }
            static class _D<T5> extends _B<T5[]> {
                static class _E<T6> extends _D<T6[]> {
                    
                    class __C<T9> extends _E<T9[]> {
                        
                        Map<T6[], T6[][]> method_1(T9[] a) { 
                            return null; 
                        }
                        
                        class __D<T10> extends _E<T10[]>.__C<T9[]> {
                            __D(_E<T10[]> owner) { owner.super(); }

                            @Override
                            Map<T10[][], T10[][][]> method_1(T9[][] a) {
                                return super.method_1(a);
                            }

                            @Override
                            T9[][][][][] method_4(Map<? super T9[][][][], ? extends T9[][][][]>[] a) {
                                return super.method_4(a);
                            }

                            @Override
                            void method_5(T9[][][][][] a) {
                                super.method_5(a);
                            }
                        } 
                    }
                }
            }
            class __E<T11> extends A_<T11[]>.B_<String[]> {
                __E(A_<T11[]> owner) {
                    owner.super();
                }

                @Override
                Map<T11[], Map<T11[][], Collection<? extends String[]>>> method_2(
                        Map<? super String[], Collection<? super T11[]>[]>[] a) {
                    return super.method_2(a);
                }
                
            }
        }
        
        class A_<T50> {
            class B_<T51> extends A_<T51> {
                
                Map<T50, Map<T50[], Collection<? extends T51>>> method_2(Map<? super T51, Collection<? super T50>[]>[] a) {
                    return null;
                }
                
                class C_<T52> {
                    class D_<T53> {
                        class E_<T54> extends A_<T50> {
                            
                            Map<T50[], Map<T51[], T52[][]>[][][]> method_3(
                                    T53 a, 
                                    T53[] b, 
                                    List<T53[]>[] c, 
                                    List<? super T54>[] d) { 
                                return null; 
                            }
                            
                            class F_<T55> extends A_<T54[][]>.B_<T53[]>.C_<T55>.D_<T51[]>.E_<T50[]> {
                                
                                class G_<T56> extends A_<T56[][]>.B_<T53[]>.C_<T56>.D_<T56[]>.E_<T50[]> {
                                    G_(A_<T56[][]>.B_<T53[]>.C_<T56>.D_<T56[]> owner) { owner.super(); }

                                    @Override
                                    Map<T56[][][], Map<T53[][], T56[][]>[][][]> method_3(
                                            T56[] a, 
                                            T56[][] b,
                                            List<T56[][]>[] c,
                                            List<? super T50[]>[] d) {
                                        return super.method_3(a, b, c, d);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
