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
package org.babyfish.util.reflect.runtime;

import java.util.function.Consumer;

import org.babyfish.org.objectweb.asm.MethodVisitor;

/**
 * @author Tao Chen
 */
public abstract class XMethodVisitor extends MethodVisitor implements SlotAllocator {
    
    public static final Object JVM_PRIMTIVIE_DEFAULT_VALUE = new Object();
    
    public XMethodVisitor(int api) {
        super(api);
    }

    public XMethodVisitor(int api, MethodVisitor mv) {
        super(api, mv);
    }
    
    public abstract int paramSlot(int paramIndex);

    public abstract void visitHashCode(Class<?> clazz, boolean nullable);
    
    public abstract void visitEquals(Class<?> clazz, boolean nullable);
    
    public abstract void visitCompare(Class<?> clazz, Boolean nullsLast);
    
    public abstract void visitBox(Class<?> clazz, Consumer<XMethodVisitor> action);
    
    public abstract void visitUnbox(Class<?> clazz, Object nullDefaultValue);
    
    public abstract void visitAStoreInsnIfNull(int varindex);
    
    public abstract void visitTryCatchBlock(
            Consumer<XMethodVisitor> tryBlock,
            Catch ... catches);
    
    public abstract void visitTryFinally(
            Consumer<MethodVisitor> tryAction,
            Consumer<MethodVisitor> finallyAction);
}
