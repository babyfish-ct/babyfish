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

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.babyfish.collection.LinkedHashSet;
import org.babyfish.lang.I18N;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.lang.bytecode.ScopedMethodVisitor;
import org.babyfish.lang.bytecode.ScopedMethodVisitorBuilder;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.lang.instrument.bytecode.Replacer;
import org.babyfish.model.ModelType;
import org.babyfish.model.instrument.metadata.MetadataClass;
import org.babyfish.model.instrument.metadata.MetadataComparatorPart;
import org.babyfish.model.instrument.metadata.MetadataProperty;
import org.babyfish.model.metadata.ModelClass;
import org.babyfish.model.spi.ObjectModel;
import org.babyfish.model.spi.ObjectModelProvider;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.FieldVisitor;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;

/**
 * @author Tao Chen
 */
public abstract class AbstractModelReplacer extends Replacer {
    
    private MetadataClass metadataClass;
    
    private String objectModelTargetInternalName;
    
    private String objectModelContractDescriptor;
    
    private String rootObjectModelContractDescriptor;
    
    private boolean proxySupported;
    
    protected AbstractModelReplacer(
            AbstractObjectModelInstrumenter instrumenter, 
            String className, 
            File classFile,
            boolean proxySupported) {
        super(instrumenter, className, classFile);
        this.metadataClass = instrumenter.getMetadataClass(className);
        if (this.metadataClass.getModelType() != ModelType.REFERENCE) {
            proxySupported = false;
        }
        
        if (proxySupported) {
            this.objectModelTargetInternalName = 
                    this.getInternalName() + '$' + Identifiers.OBJECT_MODEL_TARGET_SIMPLE_NAME;
            this.objectModelContractDescriptor = 
                    'L' + this.getInternalName() + '$' + Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME + ';';
        } else {
            this.objectModelTargetInternalName = 
                    this.getInternalName() + '$' + Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME;
            this.objectModelContractDescriptor =
                    'L' + this.objectModelTargetInternalName + ';';
        }
        
        MetadataClass rootClass = this.getMetadataClass();
        while (rootClass.getSuperClass() != null) {
            rootClass = rootClass.getSuperClass();
        }
        this.rootObjectModelContractDescriptor = 
                'L' + 
                rootClass.getInternalName() + 
                '$' + 
                Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME +
                ';';
        
        this.proxySupported = proxySupported;
        
        if (proxySupported) {
            this.new ObjectModelInterfaceGenerator();
            this.createObjectModelTargetGenerator();
            this.createObjectModelProxyGenerator();
        } else {
            this.createObjectModelTargetGenerator();
        }
    }
    
    public final MetadataClass getMetadataClass() {
        return this.metadataClass;
    }
    
    public final boolean isProxySupported() {
        return this.proxySupported;
    }
    
    protected void createObjectModelTargetGenerator() {
        new ObjectModelTargetGenerator(this);
    }
    
    protected void createObjectModelProxyGenerator() {}
    
    protected String[] determineMoreInterfaces() {
        return new String[] { Type.getInternalName(ObjectModelProvider.class) };
    }

    @Override
    protected ClassVisitor onCreateClassAdapter(ClassVisitor cv) {
        return new ClassAdapter(cv);
    }
    
    protected String getRuntimeModelClassImplDescriptor() {
        return ASMConstants.MODEL_CLASS_IMPL_DESCRITOR;
    }
    
    protected String getRuntimeModelPropertyImplDescriptor() {
        return ASMConstants.MODEL_PROPERTY_IMPL_DESCRIPTOR;
    }
    
    protected String[] getAdditionalModelPropertyImplConstructorArgDescriptors() {
        return null;
    }
    
    protected void loadAdditionalModelPropertyImplConstructorArgs(
            MethodVisitor mv, MetadataProperty metadataProperty) {
    }
    
    protected void visitGetObjectModelAnnotations(MethodVisitor mv) {}
    
    private void generateFields(ClassVisitor cv) {
        cv
        .visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                Identifiers.MODEL_CLASS_FIELD_NAME, 
                ASMConstants.MODEL_CLASS_DESCRIPTOR, 
                null,
                null
        );
        if (this.getMetadataClass().getSuperClass() == null) {
            cv
            .visitField(
                    Opcodes.ACC_PROTECTED, 
                    Identifiers.OBJECT_MODEL_FIELD_NAME, 
                    this.objectModelContractDescriptor, 
                    null, 
                    null
            )
            .visitEnd();
        }
    }

    private void generateObjectModelMethods(ClassVisitor cv) {
        this.generateCreateObjectModel(cv);
        this.generateGetObjectModel(cv);
    }

    private void generatePreClinitInsns(MethodVisitor mv) {
        this.generateInitModelClassInsns(mv);
    }
    
    private void generateInitModelClassInsns(MethodVisitor mv) {
        MetadataClass metadataClass = this.getMetadataClass();
        String runtimeModelClassImplInternalName = descToInternalName(this.getRuntimeModelClassImplDescriptor());
        String runtimeModelPropertyImplDescriptor = this.getRuntimeModelPropertyImplDescriptor();
        String runtimeModelPropertyImplInternalName = descToInternalName(runtimeModelPropertyImplDescriptor);
        
        String comparatorPartInitDescriptor = 
                "(I" 
                + ASMConstants.STRING_COMPARATOR_TYPE_DESCRIPTOR 
                + ASMConstants.NULL_COMPARATOR_TYPE_DESCRIPTOR 
                + ")V";
        StringBuilder runtimeModelPropertyImplInitDescriptorBuilder = new StringBuilder();
        runtimeModelPropertyImplInitDescriptorBuilder
        .append("(ILjava/lang/String;")
        .append(ASMConstants.PROPERTY_TYPE_DESCRIPTOR)
        .append(ASMConstants.ASSOCIATION_TYPE_DESCRIPTOR)
        .append("ZZLjava/lang/Class;Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/Class;")
        .append(ASMConstants.DEPENDENCY_DESCRIPTOR);
        String[] additionalModelPropertyImplConstructorArgDescriptors = 
                this.getAdditionalModelPropertyImplConstructorArgDescriptors();
        if (!Nulls.isNullOrEmpty(additionalModelPropertyImplConstructorArgDescriptors)) {
            for (String desc : additionalModelPropertyImplConstructorArgDescriptors) {
                runtimeModelPropertyImplInitDescriptorBuilder.append(desc);
            }
        }
        runtimeModelPropertyImplInitDescriptorBuilder.append(")V");
        String runtimeModelPropertyImplInitDescriptor = runtimeModelPropertyImplInitDescriptorBuilder.toString();
        
        mv.visitTypeInsn(Opcodes.NEW, runtimeModelClassImplInternalName);
        mv.visitInsn(Opcodes.DUP);
        ASMUtils.visitEnumLdc(mv, metadataClass.getModelType());
        ASMUtils.visitClassLdc(mv, metadataClass.getDescriptor());
        if (metadataClass.getSuperClass() != null) {
            ASMUtils.visitClassLdc(mv, metadataClass.getSuperClass().getDescriptor());
        } else {
            mv.visitInsn(Opcodes.ACONST_NULL);
        }
        mv.visitLdcInsn(metadataClass.getDeclaredProperties().size());
        mv.visitTypeInsn(Opcodes.ANEWARRAY, runtimeModelPropertyImplInternalName);
        int propertyIndex = 0;
        for (MetadataProperty metadataProperty : metadataClass.getDeclaredProperties().values()) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(propertyIndex++);
            { 
                mv.visitTypeInsn(Opcodes.NEW, runtimeModelPropertyImplInternalName);
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn(metadataProperty.getId());
                mv.visitLdcInsn(metadataProperty.getName());
                ASMUtils.visitEnumLdc(mv, metadataProperty.getPropertyType());
                ASMUtils.visitEnumLdc(mv, metadataProperty.getAssociationType());
                mv.visitInsn(metadataProperty.isDeferrable() ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
                mv.visitInsn(metadataProperty.isMandatory() ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
                ASMUtils.visitClassLdc(mv, metadataProperty.getDescriptor());
                ASMUtils.visitClassLdc(mv, metadataProperty.getStandardCollectionType());
                ASMUtils.visitClassLdc(mv, metadataProperty.getKeyDescriptor());
                ASMUtils.visitClassLdc(mv, metadataProperty.getTargetDescriptor());
                {
                    mv.visitTypeInsn(Opcodes.NEW, ASMConstants.DEPENDENCY_INTERNAL_NAME);
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitLdcInsn(propertyId(metadataProperty.getIndexProperty()));
                    mv.visitLdcInsn(propertyId(metadataProperty.getKeyProperty()));
                    mv.visitLdcInsn(propertyId(metadataProperty.getReferenceProperty()));
                    mv.visitLdcInsn(propertyId(metadataProperty.getConvarianceProperty()));
                    mv.visitLdcInsn(propertyId(metadataProperty.getOppositeProperty()));
                    if (metadataProperty.getComparatorParts() == null) {
                        mv.visitInsn(Opcodes.ACONST_NULL);
                    } else {
                        mv.visitLdcInsn(metadataProperty.getComparatorParts().size());
                        mv.visitTypeInsn(Opcodes.ANEWARRAY, ASMConstants.COMPARATOR_PART_INTERNAL_NAME);
                        int partIndex = 0;
                        for (MetadataComparatorPart comparatorPart : metadataProperty.getComparatorParts()) {
                            mv.visitInsn(Opcodes.DUP);
                            mv.visitLdcInsn(partIndex++);
                            {
                                mv.visitTypeInsn(Opcodes.NEW, ASMConstants.COMPARATOR_PART_INTERNAL_NAME);
                                mv.visitInsn(Opcodes.DUP);
                                mv.visitLdcInsn(comparatorPart.getProperty().getId());
                                ASMUtils.visitEnumLdc(mv, comparatorPart.getStringComparatorType());
                                ASMUtils.visitEnumLdc(mv, comparatorPart.getNullComparatorType());
                                mv.visitMethodInsn(
                                        Opcodes.INVOKESPECIAL, 
                                        ASMConstants.COMPARATOR_PART_INTERNAL_NAME, 
                                        "<init>", 
                                        comparatorPartInitDescriptor, 
                                        false
                                );
                            }
                            mv.visitInsn(Opcodes.AASTORE);
                        }
                    }
                    mv.visitMethodInsn(
                            Opcodes.INVOKESPECIAL, 
                            ASMConstants.DEPENDENCY_INTERNAL_NAME, 
                            "<init>", 
                            "(IIIII[" + ASMConstants.COMPARATOR_PART_DESCRIPTOR + ")V", 
                            false
                    );
                }
                this.loadAdditionalModelPropertyImplConstructorArgs(mv, metadataProperty);
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL, 
                        runtimeModelPropertyImplInternalName, 
                        "<init>", 
                        runtimeModelPropertyImplInitDescriptor,
                        false
                );
            }
            mv.visitInsn(Opcodes.AASTORE);
        }
        if (this.getMetadataClass().getModelType() != ModelType.EMBEDDABLE) {
            mv.visitInsn(Opcodes.ACONST_NULL);
        } else {
            Collection<MetadataComparatorPart> parts = 
                    this.getMetadataClass().getComparatorParts();
            mv.visitLdcInsn(parts.size());
            mv.visitTypeInsn(Opcodes.ANEWARRAY, ASMConstants.COMPARATOR_PART_INTERNAL_NAME);
            int index = 0;
            for (MetadataComparatorPart part : parts) {
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn(index++);
                {
                    mv.visitTypeInsn(Opcodes.NEW, ASMConstants.COMPARATOR_PART_INTERNAL_NAME);
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitLdcInsn(part.getProperty().getId());
                    ASMUtils.visitEnumLdc(mv, part.getStringComparatorType());
                    ASMUtils.visitEnumLdc(mv, part.getNullComparatorType());
                    mv.visitMethodInsn(
                            Opcodes.INVOKESPECIAL, 
                            ASMConstants.COMPARATOR_PART_INTERNAL_NAME, 
                            "<init>", 
                            comparatorPartInitDescriptor, 
                            false
                    );
                }
                mv.visitInsn(Opcodes.AASTORE);
            }
        }
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                runtimeModelClassImplInternalName, 
                "<init>", 
                '(' +
                Type.getDescriptor(ModelType.class) +
                "Ljava/lang/Class;" +
                "Ljava/lang/Class;" +
                '[' + runtimeModelPropertyImplDescriptor +
                '[' + ASMConstants.COMPARATOR_PART_DESCRIPTOR +
                ")V", 
                false
        );
        mv.visitFieldInsn(
                Opcodes.PUTSTATIC, 
                this.getInternalName(), 
                Identifiers.MODEL_CLASS_FIELD_NAME, 
                ASMConstants.MODEL_CLASS_DESCRIPTOR
        );
    }
    
    private static int propertyId(MetadataProperty metadataProperty) {
        return metadataProperty != null ? metadataProperty.getId() : -1;
    }

    private void generatePreInitInsns(MethodVisitor mv) {
        
        MetadataClass metadataClass = this.getMetadataClass();
        if (metadataClass.getSuperClass() != null) {
            return;
        }
        
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                metadataClass.getInternalName(), 
                Identifiers.CREATE_OBJECT_MODEL_METHOD_NAME, 
                "()" + this.objectModelContractDescriptor,
                false
        );
        mv.visitFieldInsn(
                Opcodes.PUTFIELD, 
                metadataClass.getInternalName(), 
                Identifiers.OBJECT_MODEL_FIELD_NAME,
                this.objectModelContractDescriptor
        );
    }
    
    private void generateCreateObjectModel(ClassVisitor cv) {
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PROTECTED, 
                Identifiers.CREATE_OBJECT_MODEL_METHOD_NAME, 
                "()" + this.rootObjectModelContractDescriptor, 
                null,
                null
        );
        mv.visitCode();
        
        this.generateCreateObjectModelInsns(mv);
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateHashCodeMethod(ClassVisitor cv) {
        try (ScopedMethodVisitor mv = 
                new ScopedMethodVisitorBuilder(Opcodes.ACC_PUBLIC, "hashCode")
                .self(this.getDescriptor())
                .output("I")
                .build(cv)
        ) {
            mv.visitCode();
            
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.getInternalName(), 
                    Identifiers.MODEL_CLASS_FIELD_NAME, 
                    ASMConstants.MODEL_CLASS_DESCRIPTOR
            );
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASMConstants.MODEL_CLASS_INTERNAL_NAME, 
                    "getDefaultEqualityComparator", 
                    "()" +ASMConstants.EQUALITY_COMPARATOR_DESCRIPTOR, 
                    true
            );
            mv.load("this");
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASMConstants.EQUALITY_COMPARATOR_INTERNAL_NAME, 
                    "hashCode", 
                    "(Ljava/lang/Object;)I", 
                    true
            );
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private void generateEqualsMethod(ClassVisitor cv) {
        try (ScopedMethodVisitor mv = 
                new ScopedMethodVisitorBuilder(Opcodes.ACC_PUBLIC, "equals")
                .self(this.getDescriptor())
                .parameter("obj", "Ljava/lang/Object;")
                .output("Z")
                .build(cv)
        ) {
            mv.visitCode();
            
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.getInternalName(), 
                    Identifiers.MODEL_CLASS_FIELD_NAME, 
                    ASMConstants.MODEL_CLASS_DESCRIPTOR
            );
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASMConstants.MODEL_CLASS_INTERNAL_NAME, 
                    "getDefaultEqualityComparator", 
                    "()" +ASMConstants.EQUALITY_COMPARATOR_DESCRIPTOR, 
                    true
            );
            mv.load("this");
            mv.load("obj");
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASMConstants.EQUALITY_COMPARATOR_INTERNAL_NAME, 
                    "equals", 
                    "(Ljava/lang/Object;Ljava/lang/Object;)Z", 
                    true
            );
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    protected void generateCreateObjectModelInsns(MethodVisitor mv) {
        mv.visitTypeInsn(Opcodes.NEW, this.objectModelTargetInternalName);
        mv.visitInsn(Opcodes.DUP);
        mv.visitFieldInsn(
                Opcodes.GETSTATIC, 
                this.getInternalName(), 
                Identifiers.MODEL_CLASS_FIELD_NAME, 
                Type.getDescriptor(ModelClass.class)
        );
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                this.objectModelTargetInternalName, 
                "<init>", 
                '(' + 
                Type.getDescriptor(ModelClass.class) + 
                this.getMetadataClass().getDescriptor() +
                ")V", 
                false
        );
        mv.visitInsn(Opcodes.ARETURN);
    }
    
    private void generateGetObjectModel(ClassVisitor cv) {
        if (this.getMetadataClass().getSuperClass() != null) {
            return;
        }
        
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, 
                "objectModel", 
                "()" + Type.getDescriptor(ObjectModel.class), 
                null,
                null
        );
        this.visitGetObjectModelAnnotations(mv);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(
                Opcodes.GETFIELD, 
                this.getMetadataClass().getInternalName(), 
                Identifiers.OBJECT_MODEL_FIELD_NAME, 
                this.objectModelContractDescriptor
        );
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static String descToInternalName(String desc) {
        return desc.substring(1, desc.length() - 1);
    }

    private class ClassAdapter extends ClassVisitor {
        
        private boolean hasClinit;
        
        private boolean hasHashCode;
        
        private boolean hasEquals;

        public ClassAdapter(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public void visit(
                int version, 
                int access, 
                String name, 
                String signature, 
                String superName,
                String[] interfaces) {
            
            AbstractModelReplacer that = AbstractModelReplacer.this;
            String[] moreInterfaces = that.determineMoreInterfaces();
            if (!Nulls.isNullOrEmpty(moreInterfaces)) {
                if (Nulls.isNullOrEmpty(interfaces)) {
                    interfaces = moreInterfaces;
                } else {
                    Set<String> set = new LinkedHashSet<String>();
                    for (String interfaze : interfaces) {
                        set.add(interfaze);
                    }
                    for (String moreInterface : moreInterfaces) {
                        set.add(moreInterface);
                    }
                    interfaces = set.toArray(new String[set.size()]);
                }
            }
            
            super.visit(version, access, name, signature, superName, interfaces);
            that.generateFields(this.cv);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if (AbstractModelReplacer.this.metadataClass.getDeclaredProperties().containsKey(name)) {
                return null;
            }
            return super.visitField(access, name, desc, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if ((access & Opcodes.ACC_STATIC) != 0) {
                if (name.equals("<clinit>") && desc.equals("()V")) {
                    this.hasClinit = true;
                }
            } else {
                if (name.equals("hashCode") && desc.equals("()I")) {
                    this.hasHashCode = true;
                } else if (name.equals("equals") && desc.equals("(Ljava/lang/Object;)Z")) {
                    this.hasEquals = true;
                }
            }
            MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
            if (mv == null) {
                return null;
            }
            return new ModelMethodAdapter(
                    AbstractModelReplacer.this.proxySupported,
                    AbstractModelReplacer.this.metadataClass, 
                    access, 
                    name, 
                    desc, 
                    signature,
                    exceptions,
                    mv,
                    clinitMv -> { AbstractModelReplacer.this.generatePreClinitInsns(clinitMv); },
                    initMv -> { AbstractModelReplacer.this.generatePreInitInsns(initMv); }
            );
        }

        @Override
        public void visitEnd() {
            if (this.hasHashCode != this.hasEquals) {
                if (this.hasHashCode) {
                    throw new IllegalClassException(
                            oneIsOverridenButAnotherIsNot(
                                    AbstractModelReplacer.this.getMetadataClass().getName(), 
                                    "public int hashCode()", 
                                    "public boolean equals(Object)"
                            )
                    );
                }
                throw new IllegalClassException(
                        oneIsOverridenButAnotherIsNot(
                                AbstractModelReplacer.this.getMetadataClass().getName(), 
                                "public boolean equals(Object)",
                                "public int hashCode()"
                        )
                );
            }
            AbstractModelReplacer that = AbstractModelReplacer.this;
            that.generateObjectModelMethods(this.cv);
            if (!this.hasHashCode) {
                that.generateHashCodeMethod(cv);
                that.generateEqualsMethod(cv);
            }
            if (!this.hasClinit) {
                MethodVisitor mv = this.cv.visitMethod(
                        Opcodes.ACC_STATIC, 
                        "<clinit>", 
                        "()V", 
                        null, 
                        null
                );
                mv.visitCode();
                
                AbstractModelReplacer.this.generatePreClinitInsns(mv);
                mv.visitInsn(Opcodes.RETURN);
                
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
            super.visitEnd();
        }
    }
    
    private class ObjectModelInterfaceGenerator extends AbstractObjectModelGenerator {

        protected ObjectModelInterfaceGenerator() {
            super(AbstractModelReplacer.this, Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME);
        }
        
        @Override
        protected int determineAccess() {
            return 
                    Opcodes.ACC_PROTECTED | 
                    Opcodes.ACC_STATIC | 
                    Opcodes.ACC_ABSTRACT | 
                    Opcodes.ACC_INTERFACE;
        }
        
        @Override
        protected void generate(ClassVisitor cv) {
            MetadataClass metadataClass = AbstractModelReplacer.this.metadataClass;
            cv.visit(
                    Math.max(Opcodes.V1_8, metadataClass.getBytecodeVersion()), 
                    this.determineAccess(), 
                    this.getInternalName(), 
                    null, 
                    "java/lang/Object", 
                    new String[] { 
                            metadataClass.getSuperClass() != null ?
                                    metadataClass.getSuperClass().getInternalName() + '$' + Identifiers.OBJECT_MODEL_CONTRACT_SIMPLE_NAME :
                                    ASMConstants.OBJECT_MODEL_INTERNAL_NAME 
                    }
            );
            
            this.generateEmbededComparatorStaticFields(cv);
            this.generateHashCodeScalarMethod(cv, true);
            this.generateEqualsScalarMethod(cv, true);
            this.generateCompareScalarMethod(cv, true);
            
            for (MetadataProperty metadataProperty : metadataClass.getDeclaredProperties().values()) {
                cv
                .visitMethod(
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                        Identifiers.getterName(metadataProperty), 
                        "()" + metadataProperty.getDescriptor(), 
                        metadataProperty.getSignature() != null ? "()" + metadataProperty.getSignature() : null, 
                        null
                )
                .visitEnd();
                
                MethodVisitor mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                        Identifiers.setterName(metadataProperty), 
                        '(' + metadataProperty.getDescriptor() + ")V", 
                        metadataProperty.getSignature() != null ? '(' + metadataProperty.getSignature() + ")V" : null, 
                        null
                );
                mv.visitParameter(metadataProperty.getName(), 0);
                mv.visitEnd();
            }
            
            MethodVisitor mv= cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            this.generateInitComparatorStaticFieldInsns(mv);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            
            cv.visitEnd();
        }
    }
    
    @I18N
    private static native String oneIsOverridenButAnotherIsNot(
            String className,
            String overridenMethod,
            String notOverridenMethod);
}
