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
package org.babyfish.test.util.reflect.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collection;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.org.objectweb.asm.tree.AbstractInsnNode;
import org.babyfish.org.objectweb.asm.tree.FieldInsnNode;
import org.babyfish.org.objectweb.asm.tree.InsnList;
import org.babyfish.org.objectweb.asm.tree.InsnNode;
import org.babyfish.org.objectweb.asm.tree.LdcInsnNode;
import org.babyfish.org.objectweb.asm.tree.LocalVariableNode;
import org.babyfish.org.objectweb.asm.tree.MethodInsnNode;
import org.babyfish.org.objectweb.asm.tree.MethodNode;
import org.babyfish.org.objectweb.asm.tree.TryCatchBlockNode;
import org.babyfish.org.objectweb.asm.tree.VarInsnNode;
import org.babyfish.test.util.reflect.runtime.other.Rectangle;
import org.babyfish.test.util.reflect.runtime.other.Rectangle.Size;
import org.babyfish.util.reflect.runtime.ASM;
import org.babyfish.util.reflect.runtime.ClassEnhancer;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ClassEnhancerTest {

    @Test
    public void testSimple() throws Exception {
        Simple simple1 = new Simple();
        Assert.assertEquals("Hello Simple", simple1.getString());
        Simple simple2 = SimpleEnhancer.getEnhancedClass().newInstance();
        Assert.assertEquals("Hello Simple Enhancer", simple2.getString());
    }
    
    @Test
    public void testInherance() throws Exception {
        InheritanceExtension inheritanceExtension1 = new InheritanceExtension();
        Assert.assertEquals(7, inheritanceExtension1.sum(3, 4));
        Assert.assertEquals(-1, inheritanceExtension1.sub(3, 4));
        InheritanceExtension inheritanceExtension2 = InheritanceExtensionEnhancer.getEnhancedClass().newInstance();
        Assert.assertEquals(-1, inheritanceExtension2.sum(3, 4));
        Assert.assertEquals(12, inheritanceExtension2.sub(3, 4));
    }
    
    @Test
    public void testPoint() throws Exception {
        Point point1 = new Point(new BigDecimal("3.71"), new BigDecimal("4.05"));
        Constructor<Point> constructor = PointEnhancer.getEnhancedClass().getDeclaredConstructor(BigDecimal.class, BigDecimal.class);
        constructor.setAccessible(true);
        Point point2 = constructor.newInstance(new BigDecimal("3.71"), new BigDecimal("4.05"));
        Assert.assertEquals("Point [x = 3.71, y = 4.05]", point1.toString());
        Assert.assertEquals("(3.71, 4.05)", point2.toString());
    }
    
    @Test
    public void testMessage() throws Exception {
        Message message1 = new Message(true, 'M', (byte)1, (short)2, 3, 4L, 5.5F, 6.6D, "babyfish");
        Constructor<Message> constructor = MessageEnhancer.getEnhancedClass().getDeclaredConstructor(
                boolean.class,
                char.class,
                byte.class,
                short.class,
                int.class,
                long.class,
                float.class,
                double.class,
                Object.class);
        Message message2 = constructor.newInstance(true, 'M', (byte)1, (short)2, 3, 4L, 5.5F, 6.6D, "babyfish");
        Assert.assertEquals("MESSAGE: [z = true, c = 'M', b = 1, s = 2, i = 3, j = 4L, f = 5.5F, d = 6.6D, a = {babyfish}]", message1.toString());
        Assert.assertEquals("NEW_MESSAGE: [z = true, c = 'M', b = 1, s = 2, i = 3, j = 4L, f = 5.5F, d = 6.6D, a = {babyfish}]", message2.toString());
    }
    
    @Test
    public void testValueContainer() throws Exception {
        ValueContainer valueContainer1 = new ValueContainerEx();
        ValueContainer valueContainer2 = ValueContainerEnhancer.getEnhancedClass().newInstance();
        Assert.assertEquals("OVERRIDED:VALUE", valueContainer1.getValue());
        Assert.assertEquals("OVERRIDED:VALUE", valueContainer2.getValue());
        ((ValueSetter)valueContainer2).setValue("~~~~VALUE");
        Assert.assertEquals("OVERRIDED:~~~~VALUE", valueContainer2.getValue());
        Assert.assertEquals("OVERRIDED:VALUE1", valueContainer1.getValue1());
        Assert.assertEquals("OVERRIDED:____VALUE1", valueContainer2.getValue1());
        Assert.assertEquals("OVERRIDED:VALUE2", valueContainer1.getValue2());
        Assert.assertEquals("OVERRIDED:____VALUE2", valueContainer2.getValue2());
        Assert.assertEquals("OVERRIDED:VALUE3", valueContainer1.getValue3());
        Assert.assertEquals("OVERRIDED:____VALUE3", valueContainer2.getValue3());
    }
    
    @Test
    public void testRectangle() throws Exception {
        Rectangle rect = new Rectangle();
        rect.setBound(0, 0, 100, 50);
        Assert.assertEquals(5000, rect.getArea());
        Assert.assertEquals(300, rect.getPerimeter());
        Assert.assertTrue(Math.abs(rect.getAspectRatio() - 2.0F) < 1E-10);
        Assert.assertEquals("(100, 50)", rect.getSize().toString());
        rect = RectangleEnhancer.getEnhancedClass().newInstance();
        rect.setBound(0, 0, 100, 50);
        Assert.assertEquals(150, rect.getArea());
        Assert.assertEquals(10000, rect.getPerimeter());
        Assert.assertTrue(Math.abs(rect.getAspectRatio() - 0.5F) < 1E-10);
        Assert.assertEquals("(50, 100)", rect.getSize().toString());
    }
    
    static class Simple {
        String getString() {
            return "Hello Simple";
        }
    }
    
    static class SimpleEnhancer extends ClassEnhancer {
        
        private static final SimpleEnhancer INSTANCE = getInstance(SimpleEnhancer.class);
        
        public static Class<Simple> getEnhancedClass() {
            return INSTANCE.getResultClass();
        }

        private SimpleEnhancer() {
            super(Simple.class);
        }

        @Override
        protected void doMethodFilter(MethodSource methodSource) {
            if (methodSource.getMethod().getDeclaringClass() == Simple.class &&
                    methodSource.getMethod().getName().equals("getString")) {
                for (AbstractInsnNode abstractInsnNode = methodSource.getInstructions().getFirst();
                        abstractInsnNode != null;
                        abstractInsnNode = abstractInsnNode.getNext()) {
                    if (abstractInsnNode instanceof LdcInsnNode) {
                        ((LdcInsnNode)abstractInsnNode).cst = "Hello Simple Enhancer";
                    }
                }
            }
        }
        
    }
    
    static class InheritanceBase {
        
        protected int sum0(int a, int b) {
            return a + b;
        }
        
        protected final int sub0(int a, int b) {
            return a - b;
        }
    }
    
    static class InheritanceExtension extends InheritanceBase {
        
        public final int sum(int a, int b) {
            return this.sum0(a, b);
        }
        
        public int sub(int a, int b) {
            return this.sub0(a, b);
        }
    }
    
    static class InheritanceExtensionEnhancer extends ClassEnhancer {
        
        private final static InheritanceExtensionEnhancer ENHANCER =
                getInstance(InheritanceExtensionEnhancer.class);

        private InheritanceExtensionEnhancer() {
            super(InheritanceExtension.class);
        }
        
        public static Class<InheritanceExtension> getEnhancedClass() {
            return ENHANCER.getResultClass();
        }

        @Override
        protected void doMethodFilter(MethodSource methodSource) {
            if (methodSource.getMethod().getDeclaringClass() == InheritanceBase.class) {
                if (methodSource.getMethod().getName().equals("sum0")) {
                    for (AbstractInsnNode abstractInsnNode = methodSource.getInstructions().getFirst();
                            abstractInsnNode != null;
                            abstractInsnNode = abstractInsnNode.getNext()) {
                        if (abstractInsnNode instanceof InsnNode && abstractInsnNode.getOpcode() == Opcodes.IADD) {
                            InsnNode newInsnNode = new InsnNode(Opcodes.ISUB);
                            methodSource.getInstructions().insert(abstractInsnNode, newInsnNode);
                            methodSource.getInstructions().remove(abstractInsnNode);
                            abstractInsnNode = newInsnNode;
                        }
                    }
                } else if (methodSource.getMethod().getName().equals("sub0")) {
                    for (AbstractInsnNode abstractInsnNode = methodSource.getInstructions().getFirst();
                            abstractInsnNode != null;
                            abstractInsnNode = abstractInsnNode.getNext()) {
                        if (abstractInsnNode instanceof InsnNode && abstractInsnNode.getOpcode() == Opcodes.ISUB) {
                            InsnNode newInsnNode = new InsnNode(Opcodes.IMUL);
                            methodSource.getInstructions().insert(abstractInsnNode, newInsnNode);
                            methodSource.getInstructions().remove(abstractInsnNode);
                            abstractInsnNode = newInsnNode;
                        }
                    }
                }
            }
        }
        
    }
    
    private static class Point {
        
        BigDecimal x;
        
        private BigDecimal y;

        Point(BigDecimal x, BigDecimal y) {
            Arguments.mustNotBeNull("x", x);
            Arguments.mustNotBeNull("y", y);
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Point [x = " + x + ", y = " + y + "]";
        }
        
    }
    
    private static class PointEnhancer extends ClassEnhancer {
        
        private static final PointEnhancer INSTANCE = getInstance(PointEnhancer.class);
        
        private PointEnhancer() {
            super(Point.class);
        }
        
        public static Class<Point> getEnhancedClass() {
            return INSTANCE.getResultClass();
        }

        @Override
        protected void doMethodFilter(MethodSource methodSource) {
            Method method = methodSource.getMethod();
            if (method.getDeclaringClass() == Point.class && method.getName().equals("toString")) {
                InsnList instructions = methodSource.getInstructions();
                instructions.clear();
                instructions.add(new LdcInsnNode("("));
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                instructions.add(
                        new FieldInsnNode(
                                Opcodes.GETFIELD, 
                                Type.getInternalName(Point.class), 
                                "x", 
                                ASM.getDescriptor(BigDecimal.class)));
                instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "java/lang/Object",
                                "toString",
                                "()Ljava/lang/String;",
                                false));
                instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL, 
                                "java/lang/String", 
                                "concat", 
                                "(Ljava/lang/String;)Ljava/lang/String;",
                                false));
                instructions.add(new LdcInsnNode(", "));
                instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL, 
                                "java/lang/String", 
                                "concat", 
                                "(Ljava/lang/String;)Ljava/lang/String;",
                                false));
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                instructions.add(
                        new FieldInsnNode(
                                Opcodes.GETFIELD, 
                                Type.getInternalName(Point.class), 
                                "y", 
                                ASM.getDescriptor(BigDecimal.class)));
                instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "java/lang/Object",
                                "toString",
                                "()Ljava/lang/String;",
                                false));
                instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL, 
                                "java/lang/String", 
                                "concat", 
                                "(Ljava/lang/String;)Ljava/lang/String;",
                                false));
                instructions.add(new LdcInsnNode(")"));
                instructions.add(
                        new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL, 
                                "java/lang/String", 
                                "concat", 
                                "(Ljava/lang/String;)Ljava/lang/String;",
                                false));
                instructions.add(new InsnNode(Opcodes.ARETURN));
            }
        }
        
    }
    
    static class Message {
        
        private boolean z;
        
        private char c;
        
        private byte b;
        
        private short s;
        
        private int i;
        
        private long j;
        
        private float f;
        
        private double d;
        
        private Object a;

        public Message(
                boolean z, 
                char c, 
                byte b, 
                short s, 
                int i, 
                long j,
                float f, 
                double d, 
                Object a) {
            this.z = z;
            this.c = c;
            this.b = b;
            this.s = s;
            this.i = i;
            this.j = j;
            this.f = f;
            this.d = d;
            this.a = a;
        }
        
        private static String toString0(boolean z, char c, byte b, short s, int i, long j, float f, double d, Object a) {
            return "z = " +
                    z +
                    ", c = '" +
                    c +
                    "', b = " +
                    b +
                    ", s = " +
                    s +
                    ", i = " +
                    i +
                    ", j = " +
                    j +
                    "L, f = " +
                    f +
                    "F, d = " +
                    d +
                    "D, a = {" +
                    a +
                    "}";
        }
        
        public String toString() {
            return "MESSAGE: [" + 
                    toString0(this.z, this.c, this.b, this.s, this.i, this.j, this.f, this.d, this.a) +
                    ']';
        }
        
    }
    
    static class MessageEnhancer extends ClassEnhancer {
        
        private static final MessageEnhancer INSTANCE = getInstance(MessageEnhancer.class);

        private MessageEnhancer() {
            super(Message.class);
        }
        
        public static Class<Message> getEnhancedClass() {
            return INSTANCE.getResultClass();
        }

        @Override
        protected void doMethodFilter(MethodSource methodSource) {
            Method method = methodSource.getMethod();
            if (method.getDeclaringClass() == Message.class &&
                    method.getName().equals("toString")) {
                for (AbstractInsnNode abstractInsnNode = methodSource.getInstructions().getFirst();
                        abstractInsnNode != null;
                        abstractInsnNode = abstractInsnNode.getNext()) {
                    if (abstractInsnNode instanceof LdcInsnNode) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode)abstractInsnNode;
                        if ("MESSAGE: [".equals(ldcInsnNode.cst)) {
                            ldcInsnNode.cst = "NEW_MESSAGE: [";
                        }
                    }
                }
            }
        }
        
    }
    
    static abstract class ValueContainer {
        
        private String value;
        
        protected ValueContainer() {
            this.value = "VALUE";
        }
        
        public Object getValue() {
            return this.value;
        }
        
        public Object getValue1() {
            return "VALUE1";
        }
        
        public Object getValue2() {
            return this.getValue2_0();
        }
        
        public abstract Object getValue3();
        
        private Object getValue2_0() {
            return this.getValue2_1();
        }
        
        protected final Object getValue2_1() {
            return this.getValue2_2();
        }
        
        private Object getValue2_2() {
            return this.getValue2_3();
        }
        
        protected final Object getValue2_3() {
            return this.getValue2_4();
        }
        
        private Object getValue2_4() {
            return this.getValue2_5();
        }
        
        protected final Object getValue2_5() {
            return this.getValue2_6();
        }
        
        private Object getValue2_6() {
            return "VALUE2";
        }
    }
    
    static class ValueContainerEx extends ValueContainer {

        @Override
        public Object getValue() {
            return "OVERRIDED:" + super.getValue();
        }

        @Override
        public String getValue1() {
            return "OVERRIDED:" + super.getValue1();
        }

        @Override
        public String getValue2() {
            return "OVERRIDED:" + super.getValue2();
        }

        @Override
        public String getValue3() {
            return "OVERRIDED:" + this.getValue3_0();
        }
        
        private String getValue3_0() {
            return "VALUE3";
        }
        
    }
    
    static class ValueContainerEnhancer extends ClassEnhancer {
        
        private static final ClassEnhancer INSTANCE = getInstance(ValueContainerEnhancer.class);

        private ValueContainerEnhancer() {
            super(ValueContainerEx.class);
        }
        
        public static Class<ValueContainer> getEnhancedClass() {
            return INSTANCE.getResultClass();
        }

        @Override
        protected void doMethodFilter(MethodSource methodSource) {
            Method method = methodSource.getMethod();
            if (method.getDeclaringClass() == ValueContainer.class &&
                    method.getName().equals("getValue1")) {
                for (AbstractInsnNode abstractInsnNode = methodSource.getInstructions().getFirst();
                        abstractInsnNode != null;
                        abstractInsnNode = abstractInsnNode.getNext()) {
                    if (abstractInsnNode instanceof LdcInsnNode) {
                        ((LdcInsnNode)abstractInsnNode).cst = "____" + ((LdcInsnNode)abstractInsnNode).cst;
                    }
                }
            } else if (method.getDeclaringClass() == ValueContainer.class &&
                    method.getName().equals("getValue2_6")) {
                for (AbstractInsnNode abstractInsnNode = methodSource.getInstructions().getFirst();
                        abstractInsnNode != null;
                        abstractInsnNode = abstractInsnNode.getNext()) {
                    if (abstractInsnNode instanceof LdcInsnNode) {
                        ((LdcInsnNode)abstractInsnNode).cst = "____" + ((LdcInsnNode)abstractInsnNode).cst;
                    }
                }
            } else if (method.getDeclaringClass() == ValueContainerEx.class &&
                    method.getName().equals("getValue3_0")) {
                for (AbstractInsnNode abstractInsnNode = methodSource.getInstructions().getFirst();
                        abstractInsnNode != null;
                        abstractInsnNode = abstractInsnNode.getNext()) {
                    if (abstractInsnNode instanceof LdcInsnNode) {
                        ((LdcInsnNode)abstractInsnNode).cst = "____" + ((LdcInsnNode)abstractInsnNode).cst;
                    }
                }
            }
        }

        @Override
        protected Collection<String> getInterfaces() {
            return MACollections.wrap(ASM.getInternalName(ValueSetter.class));
        }

        @Override
        protected Collection<MethodNode> getMoreMethodNodes(MethodSourceFactory methodSourceFactory) {
            MethodNode methodNode = new MethodNode();
            methodNode.access = Opcodes.ACC_PUBLIC;
            methodNode.name = "setValue";
            methodNode.desc = "(Ljava/lang/String;)V";
            methodNode.exceptions = new ArrayList<String>();
            methodNode.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
            methodNode.localVariables = new ArrayList<LocalVariableNode>();
            InsnList instructions = new InsnList();
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            instructions.add(
                    new FieldInsnNode(
                            Opcodes.PUTFIELD,
                            this.getResultInternalName(),
                            "value",
                            "Ljava/lang/String;"));
            instructions.add(new InsnNode(Opcodes.RETURN));
            methodNode.instructions = instructions;
            return MACollections.wrap(methodNode);
        }
    }
    
    interface ValueSetter {
        void setValue(String value);
    }
    
    static class RectangleEnhancer extends ClassEnhancer {

        private static final RectangleEnhancer INSTANCE = getInstance(RectangleEnhancer.class);
        
        protected RectangleEnhancer() {
            super(Rectangle.class);
        }
        
        @Override
        protected void doMethodFilter(MethodSource methodSource) {
            if (methodSource.getMethod().getName().equals("getArea")) {
                for (AbstractInsnNode abstractInsnNode = methodSource.getInstructions().getFirst();
                        abstractInsnNode != null;
                        abstractInsnNode = abstractInsnNode.getNext()) {
                    if (abstractInsnNode.getOpcode() == Opcodes.IMUL) {
                        AbstractInsnNode next = abstractInsnNode.getNext();
                        methodSource.getInstructions().remove(abstractInsnNode);
                        abstractInsnNode = new InsnNode(Opcodes.IADD);
                        methodSource.getInstructions().insertBefore(next, abstractInsnNode);
                    }
                }
            } else if (methodSource.getMethod().getName().equals("getPerimeter")) {
                for (AbstractInsnNode abstractInsnNode = methodSource.getInstructions().getFirst();
                        abstractInsnNode != null;
                        abstractInsnNode = abstractInsnNode.getNext()) {
                    if (abstractInsnNode.getOpcode() == Opcodes.IADD) {
                        AbstractInsnNode next = abstractInsnNode.getNext();
                        methodSource.getInstructions().remove(abstractInsnNode);
                        abstractInsnNode = new InsnNode(Opcodes.IMUL);
                        methodSource.getInstructions().insertBefore(next, abstractInsnNode);
                    }
                }
            } else if (methodSource.getMethod().getName().equals("getAspectRatio")) {
                for (AbstractInsnNode abstractInsnNode = methodSource.getInstructions().getFirst();
                        abstractInsnNode != null;
                        abstractInsnNode = abstractInsnNode.getNext()) {
                    if (abstractInsnNode.getOpcode() == Opcodes.FRETURN) {
                        InsnList tmpInstructions = new InsnList();
                        tmpInstructions.add(new InsnNode(Opcodes.ICONST_1));
                        tmpInstructions.add(new InsnNode(Opcodes.I2F));
                        tmpInstructions.add(new InsnNode(Opcodes.SWAP));
                        tmpInstructions.add(new InsnNode(Opcodes.FDIV));
                        methodSource.getInstructions().insertBefore(abstractInsnNode, tmpInstructions);
                    }
                }
            } else if (methodSource.getMethod().getName().equals("getSize")) {
                for (AbstractInsnNode abstractInsnNode = methodSource.getInstructions().getFirst();
                        abstractInsnNode != null;
                        abstractInsnNode = abstractInsnNode.getNext()) {
                    if (abstractInsnNode.getOpcode() == Opcodes.INVOKESPECIAL) {
                        if (((MethodInsnNode)abstractInsnNode).owner.equals(ASM.getInternalName(Size.class))) {
                            methodSource.getInstructions().insertBefore(abstractInsnNode.getPrevious(), new InsnNode(Opcodes.SWAP));
                        }
                    }
                }
            }
        }

        @Override
        protected boolean usedEnhancerLoader() {
            return true;
        }

        public static Class<Rectangle> getEnhancedClass() {
            return INSTANCE.getResultClass();
        }
    }
}
