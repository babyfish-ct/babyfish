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
package org.babyfish.lang.delegate.instrument;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.babyfish.lang.delegate.metadata.MetadataClass;
import org.babyfish.lang.instrument.Instrumenter;
import org.babyfish.lang.instrument.Logger;
import org.babyfish.lang.instrument.NoCodeClassNodeLoader;
import org.babyfish.lang.instrument.bytecode.Replacer;
import org.babyfish.org.objectweb.asm.tree.ClassNode;

/**
 * @author Tao Chen
 */
public class DelegateInstrumenter implements Instrumenter {

    private NoCodeClassNodeLoader noCodeClassNodeLoader;
    
    private Map<String, MetadataClass> metadataClasses = new HashMap<>();
    
    @Override
    public void setNoCodeClassNodeLoader(NoCodeClassNodeLoader noCodeClassNodeLoader) {
        this.noCodeClassNodeLoader = noCodeClassNodeLoader;
    }

    @Override
    public void setLogger(Logger logger) {
    }

    @Override
    public void addClassFile(File classFile) {
        ClassNode classNode = this.noCodeClassNodeLoader.load(classFile);
        MetadataClass metadataClass = new MetadataClass(classNode);
        this.metadataClasses.put(metadataClass.getClassName(), metadataClass);
    }

    @Override
    public void initialize() {}

    @Override
    public Replacer createReplacer(String className, File classFile) {
        return new DelegateReplacer(this, className, classFile);
    }
    
    public MetadataClass getMetadataClass(String className) {
        return this.metadataClasses.get(className);
    }
}
