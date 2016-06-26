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
package org.babyfish.lang.bytecode;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.org.objectweb.asm.tree.AnnotationNode;
import org.babyfish.org.objectweb.asm.tree.ClassNode;
import org.babyfish.org.objectweb.asm.tree.FieldNode;
import org.babyfish.org.objectweb.asm.tree.MethodNode;

/**
 * @author Tao Chen
 */
public class ASMTreeUtils {

    @Deprecated
    protected ASMTreeUtils() {
        throw new UnsupportedOperationException();
    }
    
    public static List<AnnotationNode> getVisibleAnnotations(Object node) {
        List<AnnotationNode> list = null;
        if (node instanceof ClassNode) {
            list = ((ClassNode)node).visibleAnnotations;
        } else if (node instanceof FieldNode) {
            list = ((FieldNode)node).visibleAnnotations;
        } else if (node instanceof MethodNode) {
            list = ((MethodNode)node).visibleAnnotations;
        } else {
            throw new IllegalArgumentException(
                    "node must be instance of \""
                    + ClassNode.class.getName()
                    + "\", \""
                    + MethodNode.class.getName()
                    + "\" or \""
                    + FieldNode.class.getName()
                    + "\""
            );
        }
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }
    
    public static List<AnnotationNode> getInvisibleAnnotations(Object node) {
        List<AnnotationNode> list = null;
        if (node instanceof ClassNode) {
            list = ((ClassNode)node).invisibleAnnotations;
        } else if (node instanceof FieldNode) {
            list = ((FieldNode)node).invisibleAnnotations;
        } else if (node instanceof MethodNode) {
            list = ((MethodNode)node).invisibleAnnotations;
        } else {
            throw new IllegalArgumentException(
                    "node must be instance of \""
                    + ClassNode.class.getName()
                    + "\", \""
                    + MethodNode.class.getName()
                    + "\" or \""
                    + FieldNode.class.getName()
                    + "\""
            );
        }
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }
    
    public static void consumeAnnotationNodes(Object asmTreeNode, Consumer<AnnotationNode> consumer) {
        if (asmTreeNode instanceof ClassNode) {
            consumeAnnotationNodes((ClassNode)asmTreeNode, consumer);
        } else if (asmTreeNode instanceof FieldNode) {
            consumeAnnotationNodes((FieldNode)asmTreeNode, consumer);
        } else if (asmTreeNode instanceof MethodNode) {
            consumeAnnotationNodes((MethodNode)asmTreeNode, consumer);
        } else {
            throw new IllegalArgumentException(
                    "\"asmTreeNode\" must be instance of \""
                    + ClassNode.class.getName()
                    + "\", \""
                    + FieldNode.class.getName()
                    + "\" or \""
                    + MethodNode.class.getName()
                    + "\""
            );
        }
    }
    
    public static void consumeAnnotationNodes(ClassNode classNode, Consumer<AnnotationNode> consumer) {
        if (classNode.visibleAnnotations != null) {
            for (AnnotationNode annotationNode : classNode.visibleAnnotations) {
                consumer.accept(annotationNode);
            }
        }
        if (classNode.invisibleAnnotations != null) {
            for (AnnotationNode annotationNode : classNode.invisibleAnnotations) {
                consumer.accept(annotationNode);
            }
        }
    }
    
    public static void consumeAnnotationNodes(FieldNode fieldNode, Consumer<AnnotationNode> consumer) {
        if (fieldNode.visibleAnnotations != null) {
            for (AnnotationNode annotationNode : fieldNode.visibleAnnotations) {
                consumer.accept(annotationNode);
            }
        }
        if (fieldNode.invisibleAnnotations != null) {
            for (AnnotationNode annotationNode : fieldNode.invisibleAnnotations) {
                consumer.accept(annotationNode);
            }
        }
    }
    
    public static void consumeAnnotationNodes(MethodNode methodNode, Consumer<AnnotationNode> consumer) {
        if (methodNode.visibleAnnotations != null) {
            for (AnnotationNode annotationNode : methodNode.visibleAnnotations) {
                consumer.accept(annotationNode);
            }
        }
        if (methodNode.invisibleAnnotations != null) {
            for (AnnotationNode annotationNode : methodNode.invisibleAnnotations) {
                consumer.accept(annotationNode);
            }
        }
    }
    
    public static AnnotationNode getAnnotationNode(Object node, Class<?> annotationType) {
        String desc = Type.getDescriptor(annotationType);
        for (AnnotationNode annotationNode : getVisibleAnnotations(node)) {
            if (annotationNode.desc.equals(desc)) {
                return annotationNode;
            }
        }
        for (AnnotationNode annotationNode : getInvisibleAnnotations(node)) {
            if (annotationNode.desc.equals(desc)) {
                return annotationNode;
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getAnnotationValue(AnnotationNode annotationNode, String name) {
        if (annotationNode.values != null) {
            Iterator<Object> itr = annotationNode.values.iterator();
            while (itr.hasNext()) {
                String n = (String)itr.next();
                Object v = itr.next();
                if (v != null && n.equals(name)) {
                    return (T)v;
                }
            }
        }
        return null;
    }
    
    public static <T> T getAnnotationValue(AnnotationNode annotationNode, String name, T defaultValue) {
        T value = getAnnotationValue(annotationNode, name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
    
    public static <E extends Enum<E>> E getAnnotationEnumValue(Class<E> enumType, AnnotationNode annotationNode, String name) {
        String[] pair = getAnnotationValue(annotationNode, name);
        if (pair != null) {
            return Enum.valueOf(enumType, pair[1]);
        }
        return null;
    }
    
    public static <E extends Enum<E>> E getAnnotationEnumValue(Class<E> enumType, AnnotationNode annotationNode, String name, E defaultValue) {
        E value = getAnnotationEnumValue(enumType, annotationNode, name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
