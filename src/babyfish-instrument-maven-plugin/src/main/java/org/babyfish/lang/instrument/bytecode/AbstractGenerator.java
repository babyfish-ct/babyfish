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
package org.babyfish.lang.instrument.bytecode;

import java.util.LinkedHashMap;
import java.util.Map;

import org.babyfish.lang.internal.Instrumented;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;

/**
 * @author Tao Chen
 */
public abstract class AbstractGenerator {
            
    private String className;
    
    private String internalName;
    
    private String descriptor;
    
    private Replacer root;
    
    private AbstractGenerator parentGenerator;
    
    // Key: simpleName
    private Map<String, NestedClassGenerator> childGeneratorMap;
    
    AbstractGenerator(String className) {
        this.root = (Replacer)this;
        this.className = className;
        this.internalName = className.replace('.', '/');
        this.descriptor = 'L' + internalName + ';';
    }
    
    AbstractGenerator(AbstractGenerator parentGenerator, String simpleName) {
        this.parentGenerator = parentGenerator;
        this.root = parentGenerator.root;
        Map<String, NestedClassGenerator> map = parentGenerator.childGeneratorMap;
        if (map == null) {
            parentGenerator.childGeneratorMap = map = new LinkedHashMap<>();
        }
        if (parentGenerator.childGeneratorMap.put(simpleName, (NestedClassGenerator)this) != null) {
            throw new IllegalArgumentException(
                    "Duplicated child generators with the same simple name: " + simpleName
            );
        }
        this.className = parentGenerator.className + '$' + simpleName;
        this.internalName = this.className.replace('.', '/');
        this.descriptor = 'L' + this.internalName + ';';
    }
    
    public final String getClassName() {
        return this.className;
    }

    public final String getInternalName() {
        return this.internalName;
    }

    public final String getDescriptor() {
        return this.descriptor;
    }
    
    @SuppressWarnings("unchecked")
    public final <R extends Replacer> R getRoot() {
        return (R)this.root;
    }
    
    @SuppressWarnings("unchecked")
    public final <G extends AbstractGenerator> G getParent() {
        return (G)this.parentGenerator;
    }
    
    abstract void visitInnerClassInSpecifiedClassVisitor(ClassVisitor cv);

    private void generateInnerClasses(ClassVisitor cv) {
        this.generateInnerClass(cv, true, true);
    }
    
    private void generateInnerClass(ClassVisitor cv, boolean up, boolean down) {
        
        AbstractGenerator parentGenerator = this.parentGenerator;
        if (up && parentGenerator != null) {
            parentGenerator.generateInnerClass(cv, true, false);
        }
        
        this.visitInnerClassInSpecifiedClassVisitor(cv);
        
        if (down && this.childGeneratorMap != null) {
            for (AbstractGenerator childGenerator : this.childGeneratorMap.values()) {
                childGenerator.generateInnerClass(cv, false, true);
            }
        }
    }
    
    final class InnerClassClassAdapter extends ClassVisitor {

        public InnerClassClassAdapter(ClassVisitor cv) {
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
            super.visit(version, access, name, signature, superName, interfaces);
            super.visitAnnotation(Type.getDescriptor(Instrumented.class), false);
            AbstractGenerator.this.generateInnerClasses(this.cv);
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            Map<String, NestedClassGenerator> childGeneratorMap = 
                    AbstractGenerator.this.childGeneratorMap;
            if (childGeneratorMap != null) {
                for (NestedClassGenerator childGenerator : childGeneratorMap.values()) {
                    childGenerator.generate();
                }
            }
        }
    }
}
