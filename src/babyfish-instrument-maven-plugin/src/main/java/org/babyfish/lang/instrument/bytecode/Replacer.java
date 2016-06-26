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

import org.babyfish.lang.instrument.Instrumenter;
import org.babyfish.org.objectweb.asm.ClassVisitor;

/**
 * @author Tao Chen
 */
public abstract class Replacer extends AbstractGenerator {

    private Instrumenter instrumenter;
    
    private File classFile;
    
    protected Replacer(
            Instrumenter instrumenter,
            String className,
            File classFile) {
        super(className);
        this.instrumenter = instrumenter;
        this.classFile = classFile;
    }
    
    @SuppressWarnings("unchecked")
    public final <I extends Instrumenter> I getInstrumenter() {
        return (I)this.instrumenter;
    }
    
    public File getClassFile() {
        return this.classFile;
    }
    
    public final ClassVisitor createClassAdapter(ClassVisitor cv) {
        return this.new InnerClassClassAdapter(this.onCreateClassAdapter(cv));
    }
    
    protected abstract ClassVisitor onCreateClassAdapter(ClassVisitor cv);
    
    @Override
    void visitInnerClassInSpecifiedClassVisitor(ClassVisitor cv) {}
}
