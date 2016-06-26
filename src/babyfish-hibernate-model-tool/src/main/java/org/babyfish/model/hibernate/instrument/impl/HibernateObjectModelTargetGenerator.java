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
package org.babyfish.model.hibernate.instrument.impl;

import java.util.SortedMap;
import java.util.SortedSet;

import org.babyfish.lang.bytecode.ScopedMethodVisitor;
import org.babyfish.lang.bytecode.ScopedMethodVisitorBuilder;
import org.babyfish.model.hibernate.instrument.spi.ASMConstants;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.instrument.spi.ObjectModelTargetGenerator;
import org.babyfish.model.spi.association.AssociatedEndpoint;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.hibernate.model.hibernate.spi.association.EntityIndexedReference;
import org.hibernate.model.hibernate.spi.association.EntityKeyedReference;
import org.hibernate.model.hibernate.spi.association.EntityList;
import org.hibernate.model.hibernate.spi.association.EntityNavigableMap;
import org.hibernate.model.hibernate.spi.association.EntityNavigableSet;
import org.hibernate.model.hibernate.spi.association.EntityOrderedMap;
import org.hibernate.model.hibernate.spi.association.EntityOrderedSet;
import org.hibernate.model.hibernate.spi.association.EntityReference;

/**
 * @author Tao Chen
 */
class HibernateObjectModelTargetGenerator extends ObjectModelTargetGenerator {

    public HibernateObjectModelTargetGenerator(ObjectModel4HibernateReplacer parent) {
        super(parent);
    }

    @Override
    protected Class<? extends AssociatedEndpoint> onDeterminAssociatedEndpointType(
            MetadataProperty metadataProperty) {
        switch (metadataProperty.getAssociationType()) {
        case LIST:
            return EntityList.class;
        case COLLECTION:
            if (SortedSet.class.isAssignableFrom(metadataProperty.getStandardCollectionType())) {
                return EntityNavigableSet.class;
            }
            return EntityOrderedSet.class;
        case MAP:
            if (SortedMap.class.isAssignableFrom(metadataProperty.getStandardCollectionType())) {
                return EntityNavigableMap.class;
            }
            return EntityOrderedMap.class;
        case REFERENCE:
            return EntityReference.class;
        case INDEXED_REFERENCE:
            return EntityIndexedReference.class;
        case KEYED_REFERENCE:
            return EntityKeyedReference.class;
        default:
            return null;
        }
    }

    @Override
    protected void generateMoreMembers(ClassVisitor cv) {
        if (this.getMetadataClass().getSuperClass() == null) {
            this.overrideSetScalarLoaderMethod(cv);
        }
    }
    
    private void overrideSetScalarLoaderMethod(ClassVisitor cv) {
        
        try (ScopedMethodVisitor mv = 
                new ScopedMethodVisitorBuilder(Opcodes.ACC_PUBLIC, "setScalarLoader")
                .self(this.getDescriptor())
                .parameter("scalarLoader", ASMConstants.SCALAR_LOADER_DESCRIPTOR)
                .build(cv)) {
            
            mv.visitCode();
            
            Label validScalarLoaderLabel = new Label();
            mv.load("scalarLoader");
            mv.visitJumpInsn(Opcodes.IFNULL, validScalarLoaderLabel);
            mv.load("scalarLoader");
            mv.visitTypeInsn(Opcodes.INSTANCEOF, ASMConstants.HIBERNATE_SCALAR_LOADER_INTERNAL_NAME);
            mv.visitJumpInsn(Opcodes.IFNE, validScalarLoaderLabel);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASMConstants.HIBERNATE_OBJECT_MODEL_EXCEPTIONS_INTERNAL_NAME, 
                    "onlyAcceptHibernateScalarLoader", 
                    "()" + ASMConstants.RUNTIME_EXCEPTION_DESCRIPTOR, 
                    false
            );
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitLabel(validScalarLoaderLabel);
            
            mv.load("this");
            mv.load("scalarLoader");
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    ASMConstants.ABSTRACT_OBJECT_MODEL_IMPL_INTERNAL_NAME, 
                    "setScalarLoader", 
                    '(' + ASMConstants.SCALAR_LOADER_DESCRIPTOR + ")V", 
                    false
            );
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
}
