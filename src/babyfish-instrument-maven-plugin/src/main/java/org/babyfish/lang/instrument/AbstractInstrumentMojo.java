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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.babyfish.lang.bytecode.ASMTreeUtils;
import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.lang.internal.Instrumented;
import org.babyfish.lang.spi.UsingInstrumenter;
import org.babyfish.org.objectweb.asm.ClassReader;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.ClassWriter;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.org.objectweb.asm.tree.AnnotationNode;
import org.babyfish.org.objectweb.asm.tree.ClassNode;
import org.babyfish.org.objectweb.asm.tree.FieldNode;
import org.babyfish.org.objectweb.asm.tree.MethodNode;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.components.io.resources.PlexusIoFileResource;
import org.codehaus.plexus.components.io.resources.PlexusIoFileResourceCollection;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;

/**
 * @author Tao Chen
 */
public abstract class AbstractInstrumentMojo extends AbstractMojo {

    private static final int CLASS_MAGIC = 0xCAFEBABE;
    
    private static final String[] DEFAULT_EXCLUDES = new String[]{ "**/package.html" };

    private static final String[] DEFAULT_INCLUDES = new String[]{ "**/**" };
    
    @Parameter
    private String[] includes;

    @Parameter
    private String[] excludes;
    
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    protected File classesDirectory;
    
    @Component
    protected MavenProject project;
    
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        File instrumentDirectory = this.getInstrumentDirectory();
        if (instrumentDirectory == null || !instrumentDirectory.exists()) {
            this.getLog().warn(
                    "Ignore the instrument for the directory \"" +
                    instrumentDirectory +
                    "\""
            );
            return;
        }
        if (!instrumentDirectory.isDirectory()) {
            throw new ArchiverException(
                    "\"" + 
                    instrumentDirectory.getAbsolutePath() + 
                    "\" is not directory"
            );
        }
        
        PlexusIoFileResourceCollection collection = 
                new PlexusIoFileResourceCollection();
        collection.setIncludes(this.includes());
        collection.setExcludes(this.excludes());
        collection.setBaseDir(instrumentDirectory);
        collection.setIncludingEmptyDirectories(false);
        collection.setPrefix("");
        collection.setUsingDefaultExcludes(true);
        try {
            Iterator<PlexusIoResource> resourceItr = collection.getResources();
            this.execute(resourceItr);
        } catch (Exception ex) {
            throw new MojoExecutionException("Failed to do the instrument", ex);
        }
    }

    protected abstract File getInstrumentDirectory();

    protected abstract String getType();
    
    protected abstract ClassLoader createClassLoader() throws Exception;
    
    private String[] includes() {
        String[] includes = this.includes;
        if (includes == null || includes.length == 0) {
            return DEFAULT_INCLUDES;
        }
        return includes;
    }
    
    private String[] excludes() {
        String[] excludes = this.excludes;
        if (excludes == null || excludes.length == 0) {
            return DEFAULT_EXCLUDES;
        }
        return excludes;
    }
    
    private void execute(Iterator<PlexusIoResource> resourceItr) throws Exception {
        ClassLoader classLoader = this.createClassLoader();
        NoCodeClassNodeLoader noCodeClassNodeLoader = new NoCodeClassNodeLoader(classLoader);
        Map<Class<?>, Instrumenter> instrumenterMap = new LinkedHashMap<>();
        Map<File, Set<Instrumenter>> fileInstrumenterMap = new LinkedHashMap<>(); 
        while (resourceItr.hasNext()) {
            PlexusIoResource resource = resourceItr.next();
            if (!(resource instanceof PlexusIoFileResource) || 
                    !resource.getName().endsWith(".class")) {
                continue;
            }
            File file = ((PlexusIoFileResource)resource).getFile();
            if (!checkMagic(file)) {
                continue;
            }
            ClassNode classNode = noCodeClassNodeLoader.load(file);
            if (ASMTreeUtils.getAnnotationNode(classNode, Instrumented.class) != null) {
                continue;
            }
            Set<Class<?>> instrumenterTypes = this.getInstrumenterTypes(
                    noCodeClassNodeLoader, classNode
            );
            for (Class<?> intrumenterType : instrumenterTypes) {
                Instrumenter instrumenter = instrumenterMap.get(intrumenterType);
                if (instrumenter == null) {
                    instrumenter = (Instrumenter)intrumenterType.newInstance();
                    instrumenter.setNoCodeClassNodeLoader(noCodeClassNodeLoader);
                    //instrumenter.setLogger(this);
                    instrumenterMap.put(intrumenterType, instrumenter);
                }
                instrumenter.addClassFile(file);
                Set<Instrumenter> instrumenters = fileInstrumenterMap.get(file);
                if (instrumenters == null) {
                    instrumenters = new LinkedHashSet<>();
                    fileInstrumenterMap.put(file, instrumenters);
                }
                instrumenters.add(instrumenter);
            }
        }
        for (Instrumenter instrumenter : instrumenterMap.values()) {
            instrumenter.initialize();
        }
        for (Entry<File, Set<Instrumenter>> entry : fileInstrumenterMap.entrySet()) {
            File classFile = entry.getKey();
            Instrumenter[] instrumenters = entry.getValue().toArray(new Instrumenter[entry.getValue().size()]);
            ClassNode noCodeClassNode = noCodeClassNodeLoader.load(classFile);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassVisitor classVisitor = classWriter;
            for (int i = instrumenters.length - 1; i >= 0; i--) {
                classVisitor = 
                        instrumenters[i]
                        .createReplacer(noCodeClassNode.name.replace('/', '.'), classFile)
                        .createClassAdapter(classVisitor);
            }
            try (InputStream inputStream = new FileInputStream(classFile)) {
                ClassReader classReader = new ClassReader(inputStream);
                classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
            }
            try (OutputStream outputStream = new FileOutputStream(classFile)) {
                byte[] bytecode = classWriter.toByteArray();
                outputStream.write(bytecode);
            }
        }
    }
    
    private static boolean checkMagic(File file) throws IOException {
        try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file.getAbsolutePath()))) {
            return dataInputStream.readInt() == CLASS_MAGIC;
        }
    }
    
    private Set<Class<?>> getInstrumenterTypes(
            NoCodeClassNodeLoader noCodeClassNodeLoader, 
            ClassNode classNode) {
        Set<Class<?>> instrumentTypes = new LinkedHashSet<>();
        String className = classNode.name.replace('/', '.');
        Class<?> instrumentType = this.determineIntrumenterType(
                noCodeClassNodeLoader, className, classNode
        );
        if (instrumentType != null) {
            instrumentTypes.add(instrumentType);
        }
        if (classNode.fields != null) {
            for (FieldNode fieldNode : classNode.fields) {
                instrumentType = this.determineIntrumenterType(
                        noCodeClassNodeLoader, className, fieldNode
                );
                if (instrumentType != null) {
                    instrumentTypes.add(instrumentType);
                }
            }
        }
        if (classNode.methods != null) {
            for (MethodNode methodNode : classNode.methods) {
                instrumentType = this.determineIntrumenterType(
                        noCodeClassNodeLoader,
                        className, 
                        methodNode
                );
                if (instrumentType != null) {
                    instrumentTypes.add(instrumentType);
                }
            }
        }
        return instrumentTypes;
    }
    
    private Class<?> determineIntrumenterType(
            NoCodeClassNodeLoader noCodeClassNodeLoader,
            String className, 
            Object asmTreeNode) {
        AnnotationNodeConsumer consumer = new AnnotationNodeConsumer(
                noCodeClassNodeLoader, className, asmTreeNode
        );
        ASMTreeUtils.consumeAnnotationNodes(asmTreeNode, consumer);
        if (consumer.instrumenterTypeName != null) {
            Class<?> instrumenterType;
            try {
                instrumenterType = Class.forName(consumer.instrumenterTypeName);
            } catch (ClassNotFoundException ex) {
                throw new IllegalClassException(
                        "The annotation \""
                        + consumer.annotationTypeName
                        + "\" is illegal, its argument \"typeName\" is specified as \""
                        + consumer.instrumenterTypeName
                        + "\" which is not a class",
                        ex
                );
            }
            if (!Instrumenter.class.isAssignableFrom(instrumenterType)) {
                throw new IllegalClassException(
                        "The annotation \""
                        + consumer.annotationTypeName
                        + "\" is illegal, its argument \"typeName\" is specified as \""
                        + consumer.instrumenterTypeName
                        + "\" which does not implements the interface \""
                        + Instrumenter.class.getName()
                        + "\""
                );
            }
            return instrumenterType;
        }
        return null;
    }
    
    private static IllegalClassException conflictAnnotationException(
            String className,
            Object asmTreeNode,
            String annotationTypeName1,
            String annotationTypeName2) {
        String nodeTypeName;
        String nodeName;
        if (asmTreeNode instanceof FieldNode) {
            nodeTypeName = "field";
            nodeName = className + '.' + ((FieldNode)asmTreeNode).name;
        }
        if (asmTreeNode instanceof MethodNode) {
            nodeTypeName = "method";
            MethodNode methodNode = (MethodNode)asmTreeNode;
            StringBuilder builder = new StringBuilder();
            builder
            .append(className)
            .append('.')
            .append(methodNode.name)
            .append('(');
            boolean addComma = false;
            for (Type type : Type.getArgumentTypes(methodNode.desc)) {
                if (addComma) {
                    builder.append(", ");
                } else {
                    builder.append(type.getClassName());
                }
            }
            builder.append(')');
            nodeName = builder.toString();
        } else {
            nodeTypeName = "class";
            nodeName = className;
        }
        return new IllegalClassException(
                "The "
                + nodeTypeName
                + " \"" +
                nodeName
                + "\" is illegal, it's marked by the annotation \"@"
                + annotationTypeName1
                + "\" and @\""
                + annotationTypeName2
                + "\", both of those two annotations are marked by the annotation \"@"
                + UsingInstrumenter.class
                + "\", this is not allowed"
        );
    }
    
    protected static URL fileNameToURL(String fileName) {
        try {
            return new File(fileName).toURI().toURL();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(
                    "\"" +
                    fileName +
                    "\" cannot be translated to valid URL",
                    ex
            );
        }
    }
    
    private static class AnnotationNodeConsumer implements Consumer<AnnotationNode> {

        private NoCodeClassNodeLoader noCodeClassNodeLoader;
        
        private Object asmTreeNode;
        
        private String className;
        
        String annotationTypeName;
        
        String instrumenterTypeName;
        
        public AnnotationNodeConsumer(
                NoCodeClassNodeLoader noCodeClassNodeLoader, 
                String className, 
                Object asmTreeNode) {
            this.noCodeClassNodeLoader = noCodeClassNodeLoader;
            this.asmTreeNode = asmTreeNode;
            this.className = className;
        }

        @Override
        public void accept(AnnotationNode annotationNode) {
            ClassNode annotationClassNode = this.noCodeClassNodeLoader.load(annotationNode.desc);
            AnnotationNode usingInsrumenterAnnotationNode = 
                    ASMTreeUtils.getAnnotationNode(annotationClassNode, UsingInstrumenter.class);
            if (usingInsrumenterAnnotationNode != null) {
                if (this.annotationTypeName != null) {
                    throw conflictAnnotationException(
                            this.className,
                            this.asmTreeNode, 
                            this.annotationTypeName, 
                            ASMUtils.toClassName(annotationNode.desc)
                    );
                }
                this.annotationTypeName = ASMUtils.toClassName(annotationNode.desc);
                this.instrumenterTypeName = ASMUtils.toClassName(
                        ASMTreeUtils.<String>getAnnotationValue(usingInsrumenterAnnotationNode, "value")
                );
            }
        }
    }
}
