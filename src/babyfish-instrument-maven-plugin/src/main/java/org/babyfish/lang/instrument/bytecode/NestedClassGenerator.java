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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.ClassWriter;
import org.babyfish.org.objectweb.asm.Opcodes;

/**
 * @author Tao Chen
 */
public abstract class NestedClassGenerator extends AbstractGenerator {
    
    private String simpleName;
    
    protected NestedClassGenerator(AbstractGenerator parent, String simpleName) {
        super(parent, simpleName);
        this.simpleName = simpleName;
    }

    protected final String getSimpleName() {
        return simpleName;
    }
    
    protected int determineAccess() {
        return Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC;
    }

    final void generate() {
        
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        this.generate(this.new InnerClassClassAdapter(classWriter));
        byte[] bytecode = classWriter.toByteArray();
        
        File targetFile = this.targetFile();
        try (OutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(bytecode);
        } catch (IOException ex) {
            throw new GeneratorException(
                    "Cannot write the instrumented bytecode into \"" +
                    targetFile +
                    "\" because of some IO exceptions", 
                    ex
            );
        }
    }
    
    protected abstract void generate(ClassVisitor cv);
    
    @Override
    final void visitInnerClassInSpecifiedClassVisitor(ClassVisitor cv) {
        cv.visitInnerClass(
                this.getInternalName(), 
                this.getParent().getInternalName(), 
                this.simpleName, 
                this.determineAccess() & ~(Opcodes.ACC_INTERFACE | Opcodes.ACC_ENUM | Opcodes.ACC_ANNOTATION)
        );
    }
    
    private File targetFile() {
        Replacer replacer = this.getRoot();
        File targetFile = replacer.getClassFile();
        String postfix = this.getClassName().substring(replacer.getClassName().length());
        String fileName = targetFile.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            fileName += postfix;
        } else {
            fileName = fileName.substring(0, lastDotIndex) + postfix + fileName.substring(lastDotIndex);
        }
        return new File(targetFile.getParentFile(), fileName);
    }
}
