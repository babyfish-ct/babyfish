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
package org.babyfish.lang.instrument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.org.objectweb.asm.ClassReader;
import org.babyfish.org.objectweb.asm.tree.ClassNode;

/**
 * @author Tao Chen
 */
public class NoCodeClassNodeLoader {

    private Map<String, ClassNode> nameMap = new HashMap<>();
    
    private Map<File, ClassNode> classFileMap = new HashMap<>();
    
    private ClassLoader classLoader;
    
    public NoCodeClassNodeLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
        
    public ClassNode load(String name) {
        Map<String, ClassNode> map = this.nameMap;
        ClassNode classNode = map.get(name);
        if (classNode == null) {
            String className = ASMUtils.toClassName(name);
            classNode = map.get(className);
            if (classNode == null) {
                classNode = this.create(className);
                map.put(className, classNode);
            }
            if (!name.equals(className)) {
                map.put(name, classNode);
            }
        }
        return classNode;
    }
    
    public ClassNode load(File classFile) {
        ClassNode classNode = this.classFileMap.get(classFile);
        if (classNode == null) {
            classNode = create(classFile);
            this.classFileMap.put(classFile, classNode);
            this.nameMap.put(classNode.name.replace('/', '.'), classNode);
        }
        return classNode;
    }
    
    private ClassNode create(String className) {
        InputStream inputStream = 
                this.classLoader
                .getResourceAsStream(className.replace('.', '/') + ".class");
        if (inputStream == null) {
            throw new IllegalArgumentException(
                    "Can't load ClassNode for \""
                    + className
                    + "\""
            );
        }
        try {
            try {
                ClassReader classReader = new ClassReader(inputStream);
                ClassNode classNode = new ClassNode();
                classReader.accept(classNode, ClassReader.SKIP_CODE);
                return classNode;
            } finally {
                inputStream.close();
            }
        } catch (IOException | RuntimeException | Error ex) {
            throw new IllegalArgumentException(
                    "Can't load ClassNode for \""
                    + className
                    + "\"",
                    ex
            );
        }
    }
    
    private static ClassNode create(File classFile) {
        ClassReader classReader;
        try (InputStream inputStream = new FileInputStream(classFile.getAbsolutePath())) {
            classReader = new ClassReader(inputStream);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, ClassReader.SKIP_CODE);
            return classNode;
        } catch (IOException ex) {
            throw new IllegalArgumentException(
                    "Can't load ClassNode for \""
                    + classFile
                    + "\"",
                    ex
            );
        }
    }
}
