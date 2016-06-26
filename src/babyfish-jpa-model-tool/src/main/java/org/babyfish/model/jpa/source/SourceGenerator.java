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
package org.babyfish.model.jpa.source;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Generated;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.TreeSet;
import org.babyfish.lang.Nulls;
import org.babyfish.model.jpa.path.CollectionFetchType;
import org.babyfish.model.jpa.path.FetchPath;
import org.babyfish.model.jpa.path.FetchPathWrapper;
import org.babyfish.model.jpa.path.GetterType;
import org.babyfish.model.jpa.path.QueryPath;
import org.babyfish.model.jpa.path.QueryPaths;
import org.babyfish.model.jpa.path.SimpleOrderPath;
import org.babyfish.model.jpa.path.SimpleOrderPathWrapper;
import org.babyfish.model.jpa.path.TypedFetchPath;
import org.babyfish.model.jpa.path.TypedQueryPath;
import org.babyfish.model.jpa.path.TypedSimpleOrderPath;
import org.babyfish.model.jpa.source.metadata.MetadataAssociation;
import org.babyfish.model.jpa.source.metadata.MetadataClass;
import org.babyfish.model.jpa.source.metadata.MetadataProperty;
import org.babyfish.model.jpa.source.metadata.MetadataScalar;

/**
 * @author Tao Chen
 */
class SourceGenerator {
    
    static final String NAME_POSTFIX = "__";
    
    private static final String LINE_SPERATOR = System.getProperty("line.separator", "\n");
    
    private MetadataClass metadataClass;
    
    // Key: SimpleName
    private Map<String, MetadataClass> samePackageMetadataClassMap;
    
    // Key: SimpleName, value: FullName
    private Map<String, String> importMap = new HashMap<>();
    
    SourceGenerator(MetadataClass metadataClass, Collection<MetadataClass> allMetadataClasses) {
        this.metadataClass = metadataClass;
        Map<String, MetadataClass> spMetadataClassMap = new HashMap<>();
        for (MetadataClass mc : allMetadataClasses) {
            if (Nulls.equals(metadataClass.getPackageName(), mc.getPackageName())) {
                spMetadataClassMap.put(mc.getSimpleName(), mc);
            }
        }
        this.samePackageMetadataClassMap = spMetadataClassMap;
        this.importClasses();
    }
    
    public void generate(Writer writer) throws IOException {
        
        writer.write("package ");
        writer.write(metadataClass.getPackageName());
        writer.write(";");
        writer.write(LINE_SPERATOR);
        writer.write(LINE_SPERATOR);
        
        this.generateImports(writer);
        
        writer.write("@Generated(\"");
        writer.write(TypedQueryPathProcessor.class.getName());
        writer.write("\")");
        writer.write(LINE_SPERATOR);
        writer.write("public abstract class ");
        writer.write(metadataClass.getSimpleName());
        writer.write(NAME_POSTFIX);
        if (metadataClass.isEntity()) {
            writer.write(" implements ");
            String typedQueryPathName = this.getRenderName(TypedQueryPath.class);
            writer.write(typedQueryPathName);
            writer.write("<");
            writer.write(metadataClass.getSimpleName());
            writer.write(">");
        }
        writer.write(" {");
        writer.write(LINE_SPERATOR);
        
        if (metadataClass.isEntity()) {
            this.generateSerialVersionUID(writer, 0, 1);
        }
        CodeBuilder builder = new CodeBuilder(1);
        if (this.metadataClass.isEntity()) {
            this.generateMembers(builder);
            this.generateFetchPathImpl(builder);
            this.generateSimpleOrderPathImpl(builder);
            this.generateFetchPathBuilderImpl(builder);
        }
        this.generateSimpleOrderPathBuilderImpl(builder);
        writer.write(builder.toString());
        
        writer.write("}");
        writer.write(LINE_SPERATOR);
    }
    
    private void generateImports(Writer writer) throws IOException {
        NavigableSet<String> javaClasses = new TreeSet<>();
        NavigableSet<String> javaXClasses = new TreeSet<>();
        NavigableMap<String, NavigableSet<String>> otherClassMap = new TreeMap<>();
        
        for (String importedClass : this.importMap.values()) {
            if (importedClass.startsWith("java.")) {
                javaClasses.add(importedClass);
            } else if (importedClass.startsWith("javax.")) {
                javaXClasses.add(importedClass);
            } else {
                int index = importedClass.indexOf('.');
                String prefix = importedClass.substring(0, index);
                NavigableSet<String> set = otherClassMap.get(prefix);
                if (set == null) {
                    set = new TreeSet<>();
                    otherClassMap.put(prefix, set);
                }
                set.add(importedClass);
            }
        }
        this.generateImports(writer, javaClasses);
        this.generateImports(writer, javaXClasses);
        for (NavigableSet<String> classes : otherClassMap.values()) {
            this.generateImports(writer, classes);
        }
    }
    
    private void generateImports(Writer writer, Set<String> importedClasses) throws IOException {
        if (!importedClasses.isEmpty()) {
            for (String importedClass : importedClasses) {
                writer.write("import ");
                writer.write(importedClass);
                writer.write(';');
                writer.write(LINE_SPERATOR);
            }
            writer.write(LINE_SPERATOR);
        }
    }
    
    private long getSerialVersionUID(String nestedSimpleClassName) throws IOException {
        long serialVersionUID = nestedSimpleClassName != null ? nestedSimpleClassName.hashCode() : 0;
        for (MetadataProperty property : this.metadataClass.getProperties().values()) {
            serialVersionUID += serialVersionUID * 31 + property.getName().hashCode();
        }
        return serialVersionUID;
    }
    
    private void generateSerialVersionUID(Writer writer, int shift, int tabCount) throws IOException {
        long serialVersionUID = 0;
        for (MetadataScalar scalar : this.metadataClass.getScalars().values()) {
            serialVersionUID += scalar.getName().hashCode();
        }
        for (MetadataAssociation association : this.metadataClass.getAssociations().values()) {
            serialVersionUID += ((long)association.getName().hashCode() << 32) | association.getRelatedMetadataClass().getName().hashCode();
        }
        serialVersionUID <<= shift;
        writer.write(LINE_SPERATOR);
        for (int i = tabCount - 1; i >= 0; i--) {
            writer.write('\t');
        }
        writer.write("private static final long serialVersionUID = ");
        writer.write(Long.toString(serialVersionUID));
        writer.write("L;");
        writer.write(LINE_SPERATOR);
    }
    
    private void generateMembers(CodeBuilder builder) throws IOException {
        
        String entityName = this.metadataClass.getSimpleName();
        String fetchPathName = this.getRenderName(FetchPath.class);
        String simpleOrderPathName = this.getRenderName(SimpleOrderPath.class);
        String queryPathsName = this.getRenderName(QueryPaths.class);
        String functionName = this.getRenderName(Function.class);
        String biFunctionName = this.getRenderName(BiFunction.class);
        
        builder
        .appendLine()
        .append("private static final ")
        .append(functionName)
        .append("<")
        .append(fetchPathName)
        .append(".Builder, FetchPathImpl> FETCH_PATH_CREATOR =")
        .appendBeginBlock(null)
        .append("fetchPathBuilder -> new FetchPathImpl(fetchPathBuilder.end());")
        .appendEndBlock(false, null);
        
        builder
        .appendLine()
        .append("private static final ")
        .append(biFunctionName)
        .append("<")
        .append(simpleOrderPathName)
        .append(".Builder, Boolean, SimpleOrderPathImpl> SIMPLE_ORDER_PATH_CREATOR =")
        .appendBeginBlock(null)
        .append("(simpleOrderPathBuilder, desc) ->");
        try (BlockScope methodScope = new BlockScope(builder)) {
            builder.append("if (desc)");
            try (BlockScope ifScope = new BlockScope(builder)) {
                builder.appendLine("return new SimpleOrderPathImpl(simpleOrderPathBuilder.desc());");
            }
            builder.appendLine("return new SimpleOrderPathImpl(simpleOrderPathBuilder.asc());");
        }
        builder.append(";").appendEndBlock(false, null);
        
        builder
        .appendLine()
        .appendLine("@Override")
        .append("public Class<")
        .append(entityName)
        .append("> getRootType()");
        try (BlockScope methodScope = new BlockScope(builder)) {
            builder
            .append("return ")
            .append(entityName)
            .appendLine(".class;");
        }
        
        builder
        .appendLine()
        .append("public static FetchPathBuilder<")
        .append(entityName)
        .append(", FetchPathImpl> begin()");
        
        try (BlockScope methodScope = new BlockScope(builder)) {
            builder
            .append("return new FetchPathBuilder<")
            .append(entityName)
            .append(", ")
            .append("FetchPathImpl>");
            try (BlockScope parameterBlock = new BlockScope(builder, "(", ");")) {
                builder
                .append(queryPathsName)
                .appendLine(".begin(), FETCH_PATH_CREATOR");
            }
        }
        
        builder
        .appendLine()
        .append("public static SimpleOrderPathBuilder<")
        .append(entityName)
        .append(", SimpleOrderPathImpl> preOrderBy()");
        try (BlockScope methodScope = new BlockScope(builder)) {
            builder
            .append("return new SimpleOrderPathBuilder<")
            .append(entityName)
            .append(", ")
            .append("SimpleOrderPathImpl>");
            try (BlockScope parameterBlock = new BlockScope(builder, "(", ");")) {
                builder.appendLine("QueryPaths.preOrderBy(), SIMPLE_ORDER_PATH_CREATOR");
            }
        }
        
        builder
        .appendLine()
        .append("public static SimpleOrderPathBuilder<")
        .append(entityName)
        .append(", SimpleOrderPathImpl> postOrderBy()");
        try (BlockScope methodScope = new BlockScope(builder)) {
            builder
            .append("return new SimpleOrderPathBuilder<")
            .append(entityName)
            .append(", ")
            .append("SimpleOrderPathImpl>");
            try (BlockScope parameterBlock = new BlockScope(builder, "(", ");")) {
                builder.appendLine("QueryPaths.postOrderBy(), SIMPLE_ORDER_PATH_CREATOR");
            }
        }
        
        builder
        .appendLine()
        .append("public static ")
        .append(metadataClass.getSimpleName())
        .append(NAME_POSTFIX)
        .append("[] compile(String queryPath)");
        try (BlockScope methodScope = new BlockScope(builder)) {
            builder.appendLine("return compile(new String[]{ queryPath });");
        }
        
        builder
        .appendLine()
        .append("public static ")
        .append(metadataClass.getSimpleName())
        .append(NAME_POSTFIX)
        .append("[] compile(String[] queryPaths)");
        try (BlockScope methodScope = new BlockScope(builder)) {
            builder
            .append(this.getRenderName(QueryPath.class))
            .append("[] arr = ")
            .append(queryPathsName)
            .append(".compile(queryPaths);")
            .appendLine()
            .append(metadataClass.getSimpleName())
            .append(NAME_POSTFIX)
            .append("[] typedQueryPaths = new ")
            .append(metadataClass.getSimpleName())
            .append(NAME_POSTFIX)       
            .appendLine("[arr.length];")
            .append("for (int i = arr.length - 1; i >= 0; i--)");
            try (BlockScope forScope = new BlockScope(builder)) {
                builder
                .append("if (arr[i] instanceof ")
                .append(fetchPathName)
                .append(")");
                try (BlockScope ifScope = new BlockScope(builder, false)) {
                    builder
                    .appendLine("typedQueryPaths[i] = new FetchPathImpl((")
                    .append(fetchPathName)
                    .appendLine(")arr[i]);");
                }
                builder.append(" else");
                try (BlockScope elseScope = new BlockScope(builder)) {
                    builder
                    .appendLine("typedQueryPaths[i] = new SimpleOrderPathImpl((")
                    .append(simpleOrderPathName)
                    .appendLine(")arr[i]);");
                }
            }
            builder.appendLine("return typedQueryPaths;");
        }
    }
    
    private void generateFetchPathImpl(CodeBuilder builder) throws IOException {
        
        String entityName = metadataClass.getSimpleName();
        String typedFetchPathName = this.getRenderName(TypedFetchPath.class);
        String fetchPathWrapperName = this.getRenderName(FetchPathWrapper.class);
        String fetchPathName = this.getRenderName(FetchPath.class);
        
        builder
        .appendLine()
        .append("public static class FetchPathImpl extends ")
        .append(entityName)
        .append(NAME_POSTFIX)
        .append(" implements ")
        .append(typedFetchPathName)
        .append("<")
        .append(entityName)
        .append(">, ")
        .append(fetchPathWrapperName);
        
        try (BlockScope classScope = new BlockScope(builder)) {
            builder
            .appendLine()
            .append("private static final long serialVersionUID = ")
            .append(Long.toString(getSerialVersionUID("FetchPathImpl")))
            .appendLine("L;")
            .appendLine()
            .append("private ")
            .append(fetchPathName)
            .appendLine(" fetchPath;")
            .appendLine()
            .append("FetchPathImpl(")
            .append(fetchPathName)
            .append(" fetchPath)");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("this.fetchPath = fetchPath;");
            }
            
            builder
            .appendLine()
            .appendLine("@Override")
            .append("public Node getFirstNode()");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("return this.fetchPath.getFirstNode();");
            }
            
            builder
            .appendLine()
            .appendLine("@Override")
            .append("public ")
            .append(fetchPathName)
            .append(" unwrap()");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("return this.fetchPath;");
            }
        }
    }
    
    private void generateFetchPathBuilderImpl(CodeBuilder builder) throws IOException {
        
        String fetchPathName = this.getRenderName(FetchPath.class);
        String functionName = this.getRenderName(Function.class);
        String typedFetchPathName = this.getRenderName(TypedFetchPath.class);
        
        builder
        .appendLine()
        .append("public static class FetchPathBuilder<R, P extends ")
        .append(typedFetchPathName)
        .append("<R>> extends ");
        if (metadataClass.getSuperMetadataClass() != null && metadataClass.getSuperMetadataClass().isEntity()) {
            builder
            .append(this.getTypedQueryPathRenderName(metadataClass.getSuperMetadataClass()))
            .append(".FetchPathBuilder<R, P> ");
        } else {
            builder.append("")
            .append(typedFetchPathName)
            .append(".TypedBuilder<R, P>");
        }
        
        try (BlockScope classScope = new BlockScope(builder)) {
            builder
            .appendLine()
            .append("public FetchPathBuilder")
            .append("(")
            .append(fetchPathName)
            .append(".Builder builder, ")
            .append(functionName)
            .append("<")
            .append(fetchPathName)
            .append(".Builder, P> pathCreator)");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("super(builder, pathCreator);");
            }
            
            String getterTypeName = this.getRenderName(GetterType.class);
            String collectionFetchTypeName = this.getRenderName(CollectionFetchType.class);
            for (MetadataAssociation association : metadataClass.getAssociations().values()) {
                
                String targetEntityTypedQueryPathName = this.getTypedQueryPathRenderName(association.getRelatedMetadataClass());
                
                builder
                .appendLine()
                .append("public ")
                .append(targetEntityTypedQueryPathName)
                .append(".FetchPathBuilder<R, P> ")
                .append(association.getName())
                .append("()");
                try (BlockScope methodScope = new BlockScope(builder)) {
                    builder
                    .append("return new ")
                    .append(targetEntityTypedQueryPathName)
                    .append(".FetchPathBuilder<R, P>(this.builder.get(\"")
                    .append(association.getName())
                    .appendLine("\"), this.pathCreator);");
                }
                
                builder
                .appendLine()
                .append("public ")
                .append(targetEntityTypedQueryPathName)
                .append(".FetchPathBuilder<R, P> ")
                .append(association.getName())
                .append("(")
                .append(getterTypeName)
                .append(" getterType)");
                try (BlockScope methodScope = new BlockScope(builder)) {
                    builder
                    .append("return new ")
                    .append(targetEntityTypedQueryPathName)
                    .append(".FetchPathBuilder<R, P>(this.builder.get(\"")
                    .append(association.getName())
                    .appendLine("\", getterType), this.pathCreator);");
                }
                
                if (association.isCollection()) {
                    
                    builder
                    .appendLine()
                    .append("public ")
                    .append(targetEntityTypedQueryPathName)
                    .append(".FetchPathBuilder<R, P> ")
                    .append(association.getName())
                    .append("(")
                    .append(collectionFetchTypeName)
                    .append(" collectionFetchType)");
                    try (BlockScope methodScope = new BlockScope(builder)) {
                        builder
                        .append("return new ")
                        .append(targetEntityTypedQueryPathName)
                        .append(".FetchPathBuilder<R, P>(this.builder.get(\"")
                        .append(association.getName())
                        .appendLine("\", collectionFetchType), this.pathCreator);");
                    }
                    
                    builder
                    .appendLine()
                    .append("public ")
                    .append(targetEntityTypedQueryPathName)
                    .append(".FetchPathBuilder<R, P> ")
                    .append(association.getName())
                    .append("(")
                    .append(getterTypeName)
                    .append(" getterType, ")
                    .append(collectionFetchTypeName)
                    .append(" collectionFetchType)");
                    try (BlockScope methodScope = new BlockScope(builder)) {
                        builder
                        .append("return new ")
                        .append(targetEntityTypedQueryPathName)
                        .append(".FetchPathBuilder<R, P>(this.builder.get(\"")
                        .append(association.getName())
                        .appendLine("\", getterType, collectionFetchType), this.pathCreator);");
                    }
                }
            }
            
            for (MetadataScalar scalar : this.metadataClass.getScalars().values()) {
                if (scalar.isLazy()) {
                    builder
                    .appendLine()
                    .append("public ")
                    .append("")
                    .append(typedFetchPathName)
                    .append(".TypedBuilder<R, P> ")
                    .append(scalar.getName())
                    .append("()");
                    try (BlockScope methodScope = new BlockScope(builder)) {
                        builder
                        .append("return new ")
                    .append(typedFetchPathName)
                    .append(".TypedBuilder<R, P>(this.builder.get(\"")
                        .append(scalar.getName())
                        .appendLine("\"), this.pathCreator);");
                    }
                }
            }
        }
    }
    
    private void generateSimpleOrderPathImpl(CodeBuilder builder) throws IOException {
        
        String entityName = metadataClass.getSimpleName();
        String typedSimpleOrderPathName = this.getRenderName(TypedSimpleOrderPath.class);
        String simpleOrderPathWrapperName = this.getRenderName(SimpleOrderPathWrapper.class);
        String simpleOrderPathName = this.getRenderName(SimpleOrderPath.class);
        
        builder
        .appendLine()
        .append("public static class SimpleOrderPathImpl extends ")
        .append(entityName)
        .append(NAME_POSTFIX)
        .append(" implements ")
        .append(typedSimpleOrderPathName)
        .append("<")
        .append(entityName)
        .append(">, ")
        .append(simpleOrderPathWrapperName);
        
        try (BlockScope classScope = new BlockScope(builder)) {
            builder
            .appendLine()
            .append("private static final long serialVersionUID = ")
            .append(Long.toString(getSerialVersionUID("SimpleOrderPathImpl")))
            .appendLine("L;")
            .appendLine()
            .append("private ")
            .append(simpleOrderPathName)
            .appendLine(" simpleOrderPath;")
            .appendLine()
            .append("SimpleOrderPathImpl(")
            .append(simpleOrderPathName)
            .append(" simpleOrderPath)");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.append("this.simpleOrderPath = simpleOrderPath;");
            }
            
            builder
            .appendLine()
            .appendLine("@Override")
            .append("public boolean isPost()");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("return this.simpleOrderPath.isPost();");
            }
            
            builder
            .appendLine()
            .appendLine("@Override")
            .append("public boolean isDesc()");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("return this.simpleOrderPath.isDesc();");
            }
            
            builder
            .appendLine()
            .appendLine("@Override")
            .append("public Node getFirstNode()");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("return this.simpleOrderPath.getFirstNode();");
            }
            
            builder
            .appendLine()
            .appendLine("@Override")
            .append("public ")
            .append(simpleOrderPathName)
            .append(" unwrap()");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("return this.simpleOrderPath;");
            }
        }
    }
    
    private void generateSimpleOrderPathBuilderImpl(CodeBuilder builder) throws IOException {
        
        String typedSimpleOrderPathName = this.getRenderName(TypedSimpleOrderPath.class);
        String simpleOrderPathName = this.getRenderName(SimpleOrderPath.class);
        String biFunctionName = this.getRenderName(BiFunction.class);
        
        builder
        .appendLine()
        .append("public static class SimpleOrderPathBuilder<R, P extends ")
        .append(typedSimpleOrderPathName)
        .append("<R>> extends ");
        if (metadataClass.getSuperMetadataClass() != null) {
            builder
            .append(this.getTypedQueryPathRenderName(metadataClass.getSuperMetadataClass()))
            .append(".SimpleOrderPathBuilder<R, P> ");
        } else {
            builder.append("")
            .append(typedSimpleOrderPathName)
            .append(".TypedBuilder<R, P>");
        }
        
        try (BlockScope classScope = new BlockScope(builder)) {
            
            builder
            .appendLine()
            .append("public SimpleOrderPathBuilder")
            .append("(")
            .append(simpleOrderPathName)
            .append(".Builder builder, ")
            .append(biFunctionName)
            .append("<")
            .append(simpleOrderPathName)
            .append(".Builder, Boolean, P> pathCreator)");
            try (BlockScope methodScope = new BlockScope(builder)) {
                builder.appendLine("super(builder, pathCreator);");
            }
            
            String getterTypeName = this.getRenderName(GetterType.class);
            for (MetadataAssociation association : this.metadataClass.getAssociations().values()) {
                
                String targetEntityTypedQueryPathName = this.getTypedQueryPathRenderName(association.getRelatedMetadataClass());
                
                builder
                .appendLine()
                .append("public ")
                .append(targetEntityTypedQueryPathName)
                .append(".SimpleOrderPathBuilder<R, P> ")
                .append(association.getName())
                .append("()");
                try (BlockScope methodScope = new BlockScope(builder)) {
                    builder
                    .append("return new ")
                    .append(targetEntityTypedQueryPathName)
                    .append(".SimpleOrderPathBuilder<R, P>(this.builder.get(\"")
                    .append(association.getName())
                    .appendLine("\"), this.pathCreator);");
                }
                
                builder
                .appendLine()
                .append("public ")
                .append(targetEntityTypedQueryPathName)
                .append(".SimpleOrderPathBuilder<R, P> ")
                .append(association.getName())
                .append("(")
                .append(getterTypeName)
                .append(" getterType)");
                try (BlockScope methodScope = new BlockScope(builder)) {
                    builder
                    .append("return new ")
                    .append(targetEntityTypedQueryPathName)
                    .append(".SimpleOrderPathBuilder<R, P>(this.builder.get(\"")
                    .append(association.getName())
                    .appendLine("\", getterType), this.pathCreator);");
                }
            }
            
            for (MetadataScalar scalar : metadataClass.getScalars().values()) {
                if (scalar.isEmbedded()) {
                    String embeddableTypedQueryPathName = this.getTypedQueryPathRenderName(scalar.getRelatedMetadataClass());
                    builder
                    .appendLine()
                    .append("public ")
                    .append(embeddableTypedQueryPathName)
                    .append(".SimpleOrderPathBuilder<R, P> ")
                    .append(scalar.getName())
                    .append("()");
                    try (BlockScope methodScope = new BlockScope(builder)) {
                        builder
                        .append("return new ")
                        .append(embeddableTypedQueryPathName)
                        .append(".SimpleOrderPathBuilder<R, P>(this.builder.get(\"")
                        .append(scalar.getName())
                        .appendLine("\"), this.pathCreator);");
                    }
                } else {
                    builder
                    .appendLine()
                    .append("public ")
                    .append(typedSimpleOrderPathName)
                    .append(".TypedBuilder<R, P> ")
                    .append(scalar.getName())
                    .append("()");
                    try (BlockScope methodScope = new BlockScope(builder)) {
                        builder
                        .append("return new ")
                        .append(typedSimpleOrderPathName)
                        .append(".TypedBuilder<R, P>(this.builder.get(\"")
                        .append(scalar.getName())
                        .appendLine("\"), this.pathCreator);");
                    }
                }
            }
        }
    }
    
    private void importClasses() {
        this.importClass(Generated.class);
        
        this.importClass(BiFunction.class);
        this.importClass(TypedSimpleOrderPath.class);
        this.importClass(SimpleOrderPath.class);
    
        for (MetadataScalar scalar : this.metadataClass.getScalars().values()) {
            if (scalar.getRelatedMetadataClass() != null) {
                this.importTypedQueryPathClass(scalar.getRelatedMetadataClass());
            }
        }
        if (this.metadataClass.isEntity()) {
            for (MetadataAssociation association : this.metadataClass.getAssociations().values()) {
                if (association.isCollection()) {
                    this.importClass(CollectionFetchType.class);
                    break;
                }
            }
            if (!metadataClass.getAssociations().isEmpty()) {
                this.importClass(GetterType.class);
            }
            
            this.importClass(Function.class);
            this.importClass(FetchPath.class);
            this.importClass(FetchPathWrapper.class);
            this.importClass(QueryPath.class);
            this.importClass(QueryPaths.class);
            this.importClass(SimpleOrderPathWrapper.class);
            this.importClass(TypedQueryPath.class);
            this.importClass(TypedFetchPath.class);
        }
        for (MetadataAssociation association : this.metadataClass.getAssociations().values()) {
            MetadataClass otherClass = (MetadataClass)association.getRelatedMetadataClass();
            this.importTypedQueryPathClass(otherClass);
        }
    }
    
    private void importClass(Class<?> clazz) {
        if (clazz.getPackage() == null) {
            return;
        }
        while (clazz.getDeclaringClass() != null) {
            clazz = clazz.getDeclaringClass();
        }
        this.importClass(clazz.getPackage().getName(), clazz.getSimpleName());
    }
    
    private void importTypedQueryPathClass(MetadataClass metadataClass) {
        this.importClass(metadataClass.getPackageName(), metadataClass.getSimpleName() + NAME_POSTFIX);
    }
    
    private void importClass(String packageName, String simpleName) {
        if (Nulls.isNullOrEmpty(packageName) || Nulls.equals(this.metadataClass.getPackageName(), packageName)) {
            return;
        }
        if (this.samePackageMetadataClassMap.containsKey(simpleName)) {
            return;
        }
        this.importMap.put(simpleName, packageName + '.' + simpleName);
    }

    private String getTypedQueryPathRenderName(MetadataClass clazz) {
        return this.getRenderName(clazz.getPackageName(), clazz.getSimpleName() + NAME_POSTFIX);
    }
    
    private String getRenderName(Class<?> clazz) {
        return this.getRenderName(clazz.getPackage() != null ? clazz.getPackage().getName() : null, clazz.getSimpleName());
    }
    
    private String getRenderName(String packageName, String simpleName) {
        if (Nulls.isNullOrEmpty(packageName) || Nulls.equals(this.metadataClass.getPackageName(), packageName)) {
            return simpleName;
        }
        String className = this.importMap.get(simpleName);
        if (className != null && 
                className.length() == packageName.length() + 1 + simpleName.length() &&
                className.charAt(packageName.length()) == '.' &&
                className.substring(0, packageName.length()).equals(packageName)) {
            return simpleName;
        }
        return packageName + '.' + simpleName;
    }
}
