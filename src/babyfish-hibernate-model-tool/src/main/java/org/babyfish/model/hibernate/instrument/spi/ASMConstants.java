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
package org.babyfish.model.hibernate.instrument.spi;

import java.io.Serializable;

import org.babyfish.org.objectweb.asm.Type;
import org.hibernate.bytecode.internal.javassist.FieldHandled;
import org.hibernate.bytecode.internal.javassist.FieldHandler;
import org.hibernate.model.hibernate.metadata.internal.HibernateModelClassImpl;
import org.hibernate.model.hibernate.spi.HibernateObjectModelExceptions;
import org.hibernate.model.hibernate.spi.proxy.AbstractObjectModelProxy;
import org.hibernate.model.hibernate.spi.proxy.FrozenLazyInitializer;
import org.hibernate.model.hibernate.spi.scalar.HibernateScalarLoader;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

/**
 * @author Tao Chen
 */
public class ASMConstants extends org.babyfish.model.jpa.instrument.spi.ASMConstants {

    public static final String ABSTRACT_OBJECT_MODEL_PROXY_INTERNAL_NAME = 
            Type.getInternalName(AbstractObjectModelProxy.class);
    
    public static final String HIBERNATE_PROXY_DESC = 
            Type.getDescriptor(HibernateProxy.class);
    
    public static final String HIBERNATE_RROXY_INTERNAL_NAME = 
            descToInternalName(HIBERNATE_PROXY_DESC);
    
    public static final String FROZEN_LAZY_INITIALIZER_DESC = 
            Type.getDescriptor(FrozenLazyInitializer.class);
    
    public static final String FROZEN_LAZY_INITIALIZER_INTERNAL_NAME = 
            descToInternalName(FROZEN_LAZY_INITIALIZER_DESC);
    
    public static final String LAZY_INITIALIZER_INTERNAL_NAME = 
            Type.getInternalName(LazyInitializer.class);
    
    public static final String HIBERNATE_MODEL_CLASS_IMPL_DESCRIPTOR = 
            Type.getDescriptor(HibernateModelClassImpl.class);
    
    public static final String FIELD_HANDLED_INTERNAL_NAME =
            Type.getInternalName(FieldHandled.class);
    
    public static final String FIELD_HANDLER_DESCRIPTOR =
            Type.getDescriptor(FieldHandler.class);
    
    public static final String FIELD_HANDLER_INTERNAL_NAME =
            descToInternalName(FIELD_HANDLER_DESCRIPTOR);
    
    public static final String HIBERNATE_SCALAR_LOADER_DESCRIPTOR =
            Type.getDescriptor(HibernateScalarLoader.class);
    
    public static final String HIBERNATE_SCALAR_LOADER_INTERNAL_NAME =
            descToInternalName(HIBERNATE_SCALAR_LOADER_DESCRIPTOR);
    
    public static final String HIBERNATE_OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME =
            Type.getInternalName(HibernateObjectModelExceptions.class);
    
    public static final String SERIALIZABLE_DESCRIPTOR =
            Type.getDescriptor(Serializable.class);
    
    @Deprecated
    protected ASMConstants() {}
}
