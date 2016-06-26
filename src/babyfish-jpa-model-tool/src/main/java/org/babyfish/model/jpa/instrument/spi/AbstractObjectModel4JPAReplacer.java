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
package org.babyfish.model.jpa.instrument.spi;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.I18N;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.model.instrument.metadata.MetadataClass;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.instrument.spi.AbstractModelReplacer;
import org.babyfish.model.instrument.spi.Identifiers;
import org.babyfish.model.jpa.instrument.impl.ObjectModel4JPAInstrumenter;
import org.babyfish.model.jpa.instrument.metadata.JPAMetadataProperty;
import org.babyfish.org.objectweb.asm.AnnotationVisitor;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.tree.AnnotationNode;
import org.babyfish.org.objectweb.asm.tree.ClassNode;
import org.babyfish.org.objectweb.asm.tree.FieldNode;

/**
 * @author Tao Chen
 */
public abstract class AbstractObjectModel4JPAReplacer extends AbstractModelReplacer {

    protected AbstractObjectModel4JPAReplacer(
            ObjectModel4JPAInstrumenter instrumenter, 
            String className, 
            File classFile) {
        super(instrumenter, className, classFile, true);
    }

    @Override
    protected String getRuntimeModelClassImplDescriptor() {
        return ASMConstants.JPA_MODEL_CLASS_IMPL_DESCRIPTOR;
    }

    @Override
    protected String getRuntimeModelPropertyImplDescriptor() {
        return ASMConstants.JPA_MODEL_PROPERTY_IMPL_DESCRIPTOR;
    }

    @Override
    protected String[] getAdditionalModelPropertyImplConstructorArgDescriptors() {
        return new String[] { ASMConstants.JPA_SCALAR_TYPE_DESCRIPTOR, "Z" };
    }

    @Override
    protected void loadAdditionalModelPropertyImplConstructorArgs(
            MethodVisitor mv, 
            MetadataProperty metadataProperty) {
        JPAMetadataProperty jpaMetadataProperty = (JPAMetadataProperty)metadataProperty;
        ASMUtils.visitEnumLdc(mv, jpaMetadataProperty.getScalarType());
        mv.visitInsn(jpaMetadataProperty.isInverse() ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
    }

    @Override
    protected void visitGetObjectModelAnnotations(MethodVisitor mv) {
        mv.visitAnnotation(ASMConstants.TRANSIENT_DESCRIPTOR, true);
    }

    @Override
    protected ClassVisitor onCreateClassAdapter(ClassVisitor cv) {
        return this.new ClassAdapter(super.onCreateClassAdapter(cv));
    }
    
    protected boolean isJPAAnnotation(String annotationDesc) {
        int lastSlashIndex = annotationDesc.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            String packageName = annotationDesc.substring(1, lastSlashIndex);
            return packageName.equals("javax/persistence");
        }
        return false;
    }
    
    private class ClassAdapter extends ClassVisitor {

        private Map<String, FieldNode> declaredFieldNodes;
        
        private Map<String, MetadataProperty> getters;
        
        private Map<String, MetadataProperty> setters;
        
        ClassAdapter(ClassVisitor cv) {
            
            super(Opcodes.ASM5, cv);
        
            MetadataClass metadataClass = AbstractObjectModel4JPAReplacer.this.getMetadataClass();
            
            ObjectModel4JPAInstrumenter instrumenter = 
                    (ObjectModel4JPAInstrumenter)AbstractObjectModel4JPAReplacer.this.getInstrumenter();
            ClassNode classNode = instrumenter.getClassNode(metadataClass.getName());
            if (Nulls.isNullOrEmpty(classNode.fields)) {
                this.declaredFieldNodes = MACollections.emptyMap();
            } else {
                Map<String, FieldNode> fMap = new HashMap<>((classNode.fields.size() * 4 + 2) / 3);
                for (FieldNode fieldNode : classNode.fields) {
                    fMap.put(fieldNode.name, fieldNode);
                }
                this.declaredFieldNodes = fMap;
            }
            
            int capacity = (metadataClass.getDeclaredProperties().size() * 4 + 2) / 3;
            Map<String, MetadataProperty> gMap = new HashMap<>(capacity);
            Map<String, MetadataProperty> sMap = new HashMap<>(capacity);
            for (MetadataProperty metadataProperty : metadataClass.getDeclaredProperties().values()) {
                gMap.put(Identifiers.getterName(metadataProperty), metadataProperty);
                sMap.put(Identifiers.setterName(metadataProperty), metadataProperty);
            }
            this.getters = gMap;
            this.setters = sMap;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (desc.charAt(1) == ')') {
                MetadataProperty metadataProperty = this.getters.get(name);
                if (metadataProperty != null && metadataProperty.getDescriptor().equals(desc.substring(2))) {
                    if ((access & Opcodes.ACC_STATIC) != 0) {
                        throw new IllegalClassException(
                                getterCannotBeStatic(
                                        metadataProperty.getDeclaringClass(), 
                                        ASMUtils.toClassName(metadataProperty.getDescriptor()) + ' ' + name + "()"
                                )
                        );
                    }
                    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                    FieldNode fieldNode = this.declaredFieldNodes.get(metadataProperty.getName());
                    if (fieldNode.visibleAnnotations != null) {
                        for (AnnotationNode annotationNode : fieldNode.visibleAnnotations) {
                            if (AbstractObjectModel4JPAReplacer.this.isJPAAnnotation(annotationNode.desc)) {
                                AnnotationVisitor av = mv.visitAnnotation(annotationNode.desc, true);
                                annotationNode.accept(av);
                                av.visitEnd();
                            }
                        }
                    }
                    this.getters.remove(name);
                    return mv;
                }
            } else if (desc.endsWith(")V")) {
                MetadataProperty metadataProperty = this.setters.get(name);
                if (metadataProperty != null && metadataProperty.getDescriptor().equals(desc.substring(1, desc.length() - 2))) {
                    if ((access & Opcodes.ACC_STATIC) != 0) {
                        throw new IllegalClassException(
                                setterCannotBeStatic(
                                        metadataProperty.getDeclaringClass(), 
                                        "void " + name + '(' + ASMUtils.toClassName(metadataProperty.getDescriptor()) + ')'
                                )
                        );
                    }
                    this.setters.remove(name);
                }
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        @Override
        public void visitEnd() {
            if (!this.getters.isEmpty()) {
                List<String> fieldNames = new ArrayList<>(this.getters.size());
                for (MetadataProperty metadataProperty : this.getters.values()) {
                    fieldNames.add(metadataProperty.getName());
                }
                throw new IllegalClassException(
                        missGetters(
                                AbstractObjectModel4JPAReplacer.this.getMetadataClass(),
                                fieldNames
                        )
                );
            }
            if (!this.setters.isEmpty()) {
                List<String> fieldNames = new ArrayList<>(this.setters.size());
                for (MetadataProperty metadataProperty : this.setters.values()) {
                    fieldNames.add(metadataProperty.getName());
                }
                throw new IllegalClassException(
                        missSetters(
                                AbstractObjectModel4JPAReplacer.this.getMetadataClass(),
                                fieldNames
                        )
                );
            }
            super.visitEnd();
        }
    }
    
    @I18N
    private static native String getterCannotBeStatic(MetadataClass declaringClass, String method);
    
    @I18N
    private static native String setterCannotBeStatic(MetadataClass declaringClass, String method);
    
    @I18N
    private static native String missGetters(MetadataClass declaringClass, Collection<String> fieldNames);
    
    @I18N
    private static native String missSetters(MetadataClass declaringClass, Collection<String> fieldNames);
}
