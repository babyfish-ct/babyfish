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
package org.babyfish.model.instrument.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenContext;
import org.babyfish.data.event.AttributeScope;
import org.babyfish.data.event.EventAttributeContext;
import org.babyfish.data.event.ModificationEvent;
import org.babyfish.data.event.spi.GlobalAttributeContext;
import org.babyfish.data.event.spi.InAllChainAttributeContext;
import org.babyfish.model.NullComparatorType;
import org.babyfish.model.StringComparatorType;
import org.babyfish.model.event.ScalarEvent;
import org.babyfish.model.event.ScalarListener;
import org.babyfish.model.event.modification.ObjectModelModifications;
import org.babyfish.model.metadata.AssociationType;
import org.babyfish.model.metadata.ComparatorPart;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.metadata.PropertyType;
import org.babyfish.model.metadata.internal.ModelClassImpl;
import org.babyfish.model.metadata.internal.ModelPropertyImpl;
import org.babyfish.model.spi.AbstractObjectModelImpl;
import org.babyfish.model.spi.DirtinessAwareScalarLoader;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelExceptions;
import org.babyfish.model.spi.ObjectModelProvider;
import org.babyfish.model.spi.ScalarLoader;
import org.babyfish.model.spi.association.AssociatedEndpoint;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.validator.Validator;
import org.babyfish.validator.Validators;

/**
 * @author Tao Chen
 */
public class ASMConstants {

    public static final int LDC_CONST_CLEAR_DISABLED = ~1;
    
    public static final int OP_CONST_DISABLED = Opcodes.ICONST_1;
    
    public static final int OP_CONST_UNLOADED = Opcodes.ICONST_2;
    
    public static final String MODEL_CLASS_IMPL_DESCRITOR = Type.getDescriptor(ModelClassImpl.class);
    
    public static final String MODEL_PROPERTY_IMPL_DESCRIPTOR = Type.getDescriptor(ModelPropertyImpl.class);
    
    public static final String PROPERTY_TYPE_DESCRIPTOR = Type.getDescriptor(PropertyType.class);
    
    public static final String ASSOCIATION_TYPE_DESCRIPTOR = Type.getDescriptor(AssociationType.class);
    
    public static final String DEPENDENCY_DESCRIPTOR = Type.getDescriptor(ModelPropertyImpl.Dependency.class);
    
    public static final String DEPENDENCY_INTERNAL_NAME = descToInternalName(DEPENDENCY_DESCRIPTOR);
    
    public static final String COMPARATOR_PART_DESCRIPTOR = Type.getDescriptor(ComparatorPart.class);
    
    public static final String COMPARATOR_PART_INTERNAL_NAME = descToInternalName(COMPARATOR_PART_DESCRIPTOR);
    
    public static final String OBJECT_MODEL_PROVIDER_INTERNAL_NAME = Type.getInternalName(ObjectModelProvider.class);
    
    public static final String RUNTIME_EXCEPTION_DESCRIPTOR = Type.getDescriptor(RuntimeException.class);
    
    public static final String IO_EXCEPTION_INTERNAL_NAME = 
            Type.getInternalName(IOException.class);
    
    public static final String CLASS_NOT_FOUND_EXCEPTION_INTERNAL_NAME = 
            Type.getInternalName(ClassNotFoundException.class);
    
    public static final String OBJECT_OUTPUT_STREAM_DESCRIPTOR = Type.getDescriptor(ObjectOutputStream.class);
    
    public static final String OBJECT_OUTPUT_STREAM_INTERNAL_NAME = 
            descToInternalName(OBJECT_OUTPUT_STREAM_DESCRIPTOR);
    
    public static final String OBJECT_INPUT_STREAM_DESCRIPTOR = Type.getDescriptor(ObjectInputStream.class);
    
    public static final String OBJECT_INPUT_STREAM_INTERNAL_NAME =
            descToInternalName(OBJECT_INPUT_STREAM_DESCRIPTOR);

    public static final String UNSUPPORTED_OPERATION_EXCEPTION_DESCRIPTOR = 
            Type.getDescriptor(UnsupportedOperationException.class);

    public static final String UNSUPPORTED_OPERATION_EXCEPTION_INTERNAL_NAME =
            descToInternalName(UNSUPPORTED_OPERATION_EXCEPTION_DESCRIPTOR);
    
    public static final String VALIDATOR_DESCRIPTOR = Type.getDescriptor(Validator.class);
    
    public static final String VALIDATORS_INTERNAL_NAME = Type.getInternalName(Validators.class);
    
    public static final String COMPARATOR_DESCRIPTOR = Type.getDescriptor(Comparator.class);

    public static final String COMPARATOR_INTERNAL_NAME = descToInternalName(COMPARATOR_DESCRIPTOR);

    public static final String EQUALITY_COMPARATOR_DESCRIPTOR = 
            Type.getDescriptor(EqualityComparator.class);

    public static final String EQUALITY_COMPARATOR_INTERNAL_NAME = 
            descToInternalName(EQUALITY_COMPARATOR_DESCRIPTOR);

    public static final String STRING_COMPARATOR_TYPE_DESCRIPTOR = Type.getDescriptor(StringComparatorType.class);

    public static final String NULL_COMPARATOR_TYPE_DESCRIPTOR = Type.getDescriptor(NullComparatorType.class);

    public static final String MODEL_CLASS_DESCRIPTOR = Type.getDescriptor(ModelClass.class);

    public static final String MODEL_CLASS_INTERNAL_NAME = descToInternalName(MODEL_CLASS_DESCRIPTOR);

    public static final String OBJECT_MODEL_DESCRIPTOR = Type.getDescriptor(ObjectModel.class);
    
    public static final String OBJECT_MODEL_INTERNAL_NAME = descToInternalName(OBJECT_MODEL_DESCRIPTOR);    

    public static final String FROZEN_CONTEXT_DESCRIPTOR = Type.getDescriptor(FrozenContext.class);
    
    public static final String FROZEN_CONTEXT_INTERNAL_NAME = descToInternalName(FROZEN_CONTEXT_DESCRIPTOR);
    
    public static final String SCALAR_LISTENER_DESCRIPTOR = Type.getDescriptor(ScalarListener.class);
    
    public static final String SCALAR_LISTENER_INTERNAL_NAME = descToInternalName(SCALAR_LISTENER_DESCRIPTOR);
    
    public static final String SCALAR_EVENT_DESCRIPTOR = Type.getDescriptor(ScalarEvent.class);
    
    public static final String SCALAR_EVENT_INTERNAL_NAME = descToInternalName(SCALAR_EVENT_DESCRIPTOR);
    
    public static final String SCALAR_LOADER_DESCRIPTOR = Type.getDescriptor(ScalarLoader.class);
    
    public static final String DIRTY_AWARE_LOADER_INTERNAL_NAME = 
            Type.getInternalName(DirtinessAwareScalarLoader.class);

    public static final String ABSTRACT_OBJECT_MODEL_IMPL_DESCRITPOR = 
            Type.getDescriptor(AbstractObjectModelImpl.class);
    
    public static final String ABSTRACT_OBJECT_MODEL_IMPL_INTERNAL_NAME = 
            descToInternalName(ABSTRACT_OBJECT_MODEL_IMPL_DESCRITPOR);

    public static final String EMBEDDED_SCALAR_LISTENER_IMPL_INTERNAL_NAME = 
            ABSTRACT_OBJECT_MODEL_IMPL_INTERNAL_NAME + 
            "$EmbeddedScalarListenerImpl";
    
    public static final String OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME =
            Type.getInternalName(ObjectModelExceptions.class);
    
    public static final String ASSOCIATED_ENDPOINT_DESCRIPTOR =
            Type.getDescriptor(AssociatedEndpoint.class);
    
    public static final String SET_BY_SCALAR_PROPERTY_ID_AND_VALUE_DESCRIPTOR =
            Type.getDescriptor(ObjectModelModifications.SetByScalarPropertyIdAndValue.class);
    
    public static final String OBJECT_MODEL_MODIFICATIONS_INTERNAL_NAME =
            Type.getInternalName(ObjectModelModifications.class);
    
    public static final String ATTRIBUTE_SCOPE_DESCRIPTOR =
            Type.getDescriptor(AttributeScope.class);
    
    public static final String MODIFICATION_EVENT_INTERNAL_NAME =
            Type.getInternalName(ModificationEvent.class);
    
    public static final String EVENT_ATTRIBUTE_CONTEXT_DESCRIPTOR =
            Type.getDescriptor(EventAttributeContext.class);
    
    public static final String IN_ALL_CHAIN_ATTRIBUTE_CONTEXT_INTERNAL_NAME =
            Type.getInternalName(InAllChainAttributeContext.class);
    
    public static final String GLOBAL_ATTRIBUTE_CONTEXT_INTERNAL_NAME =
            Type.getInternalName(GlobalAttributeContext.class);
    
    @Deprecated
    protected ASMConstants() {
        throw new UnsupportedOperationException();
    }
    
    protected static String descToInternalName(String desc) {
        // Small optimization, let internal name share the memory with descriptor.
        return desc.substring(1, desc.length() - 1);
    }
}
