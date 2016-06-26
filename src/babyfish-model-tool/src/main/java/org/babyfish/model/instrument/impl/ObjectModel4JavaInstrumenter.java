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
package org.babyfish.model.instrument.impl;

import java.io.File;

import org.babyfish.lang.instrument.bytecode.Replacer;
import org.babyfish.model.instrument.metadata.spi.ClassProcessor;
import org.babyfish.model.instrument.metadata.spi.Processor;
import org.babyfish.model.instrument.metadata.spi.PropertyProcessor;
import org.babyfish.model.instrument.spi.AbstractObjectModelInstrumenter;
import org.babyfish.org.objectweb.asm.tree.ClassNode;

/**
 * @author Tao Chen
 */
public class ObjectModel4JavaInstrumenter extends AbstractObjectModelInstrumenter {

    @Override
    public Replacer createReplacer(String className, File classFile) {
        return new ObjectModel4JavaReplacer(this, className, classFile);
    }

    @Override
    protected MetadataClassImpl createMetadataClass(File classFile, ClassNode classNode) {
        return new MetadataClassImpl(classFile, classNode);
    }

    @Override
    protected Processor[] getProcessors() {
        final ObjectModel4JavaInstrumenter ctx = this;
        return new Processor[] {
                new ClassProcessor<MetadataClassImpl>() {
                    @Override
                    public void processClass(MetadataClassImpl c) {
                        c.init();
                    }
                },
                new PropertyProcessor<MetadataPropertyImpl>() {
                    @Override
                    public void processProperty(MetadataPropertyImpl p) {
                        p.init();
                    }
                },
                new ClassProcessor<MetadataClassImpl>() {
                    @Override
                    public void processClass(MetadataClassImpl c) {
                        c.afterInit();
                    }
                },
                new PropertyProcessor<MetadataPropertyImpl>() {
                    @Override
                    public void processProperty(MetadataPropertyImpl p) {
                        p.resolveClass(ctx);
                    }
                },
                new PropertyProcessor<MetadataPropertyImpl>() {
                    @Override
                    public void processProperty(MetadataPropertyImpl p) {
                        p.resolveReferenceProperty();
                    }
                },
                new PropertyProcessor<MetadataPropertyImpl>() {
                    @Override
                    public void processProperty(MetadataPropertyImpl p) {
                        p.resolveAssociationType();
                    }
                },
                new ClassProcessor<MetadataClassImpl>() {
                    @Override
                    public void processClass(MetadataClassImpl c) {
                        c.resolveSuperClass(ctx);
                    }
                },
                new ClassProcessor<MetadataClassImpl>() {
                    @Override
                    public void processClass(MetadataClassImpl c) {
                        c.resolveAncestorClass();
                    }
                },
                new ClassProcessor<MetadataClassImpl>() {
                    @Override
                    public void processClass(MetadataClassImpl c) {
                        c.validateNonReferenceClass();
                    }
                },
                new ClassProcessor<MetadataClassImpl>() {
                    @Override
                    public void processClass(MetadataClassImpl c) {
                        c.resolveProperties();
                    }
                },
                new ClassProcessor<MetadataClassImpl>() {
                    @Override
                    public void processClass(MetadataClassImpl c) {
                        c.resolvePropertyList();
                    }
                },
                new ClassProcessor<MetadataClassImpl>() {
                    @Override
                    public void processClass(MetadataClassImpl c) {
                        c.resolveComparatorParts();
                    }
                },
                new PropertyProcessor<MetadataPropertyImpl>() {
                    @Override
                    public void processProperty(MetadataPropertyImpl p) {
                        p.resolveConvarianceProperty();
                    }
                },
                new PropertyProcessor<MetadataPropertyImpl>() {
                    @Override
                    public void processProperty(MetadataPropertyImpl p) {
                        p.resolveOppositeProperty();
                    }
                },
                new PropertyProcessor<MetadataPropertyImpl>() {
                    @Override
                    public void processProperty(MetadataPropertyImpl p) {
                        p.resolveComparatorParts(ctx);
                    }
                },
        };
    }
}
