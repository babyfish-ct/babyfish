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
package org.babyfish.hibernate.internal;

import org.babyfish.hibernate.XSession;
import org.babyfish.hibernate.XSessionBuilder;
import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.lang.Arguments;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.util.reflect.MethodDescriptor;
import org.babyfish.util.reflect.MethodImplementation;
import org.babyfish.util.reflect.runtime.ASM;
import org.babyfish.util.reflect.runtime.ClassWrapper;
import org.babyfish.util.reflect.runtime.XMethodVisitor;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.engine.spi.SessionBuilderImplementor;

/**
 * @author Tao Chen
 */
public final class SessionBuilderWrapper extends ClassWrapper {
    
    private static final SessionBuilderWrapper INSTANCE = getInstance(SessionBuilderWrapper.class);

    private SessionBuilderWrapper() {
        super(SessionBuilder.class, XSessionFactory.class);
    }
    
    public static XSessionBuilderImplementor wrap(SessionBuilder sessionBuilder, XSessionFactory factory) {
        if (sessionBuilder instanceof XSessionBuilderImplementor) {
            return (XSessionBuilderImplementor)sessionBuilder;
        }
        return (XSessionBuilderImplementor)INSTANCE.createProxy(
                Arguments.mustBeInstanceOfValue("sessionBuilder", sessionBuilder, SessionBuilderImplementor.class),
                Arguments.mustNotBeNull("factory", factory)
        );
    }

    @Override
    protected Class<?>[] onGetRawInterfaceTypes() {
        return new Class[] { SessionBuilderImplementor.class };
    }

    @Override
    protected Class<?>[] onGetInterfaceTypes() {
        return new Class[] { XSessionBuilder.class, XSessionBuilderImplementor.class };
    }

    @Override
    protected void generateMethodCode(XMethodVisitor mv,
            MethodImplementation methodImplementation) {
        MethodDescriptor descriptor = methodImplementation.getDescriptor();
        boolean returnSession = Session.class.isAssignableFrom(descriptor.getReturnType());
        boolean returnSessionBuilder = SessionBuilder.class.isAssignableFrom(descriptor.getReturnType());
        if (returnSession || returnSessionBuilder) {
            this.generateInvokeRawMethodCode(mv, methodImplementation);
            this.generateGetArgument(mv, 0);
            if (returnSession) {
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        ASM.getInternalName(SessionImplWrapper.class), 
                        "wrap", 
                        '(' + 
                        ASM.getDescriptor(Session.class) + 
                        ASM.getDescriptor(XSessionFactory.class) + 
                        ')' 
                        + ASM.getDescriptor(XSession.class),
                        false);
            }
            if (returnSessionBuilder) {
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        ASM.getInternalName(SessionBuilderWrapper.class), 
                        "wrap", 
                        '(' + 
                        ASM.getDescriptor(SessionBuilder.class) +
                        ASM.getDescriptor(XSessionFactory.class) + 
                        ')' 
                        + ASM.getDescriptor(XSessionBuilderImplementor.class),
                        false);
            }
            mv.visitInsn(Opcodes.ARETURN);
        } else {
            super.generateMethodCode(mv, methodImplementation);
        }
    }
}
