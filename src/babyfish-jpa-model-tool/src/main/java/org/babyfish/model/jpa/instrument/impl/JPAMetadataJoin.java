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
package org.babyfish.model.jpa.instrument.impl;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.I18N;
import org.babyfish.lang.bytecode.ASMTreeUtils;
import org.babyfish.lang.bytecode.ASMUtils;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.model.jpa.instrument.spi.ASMConstants;
import org.babyfish.org.objectweb.asm.tree.AnnotationNode;
import org.babyfish.org.objectweb.asm.tree.FieldNode;

/**
 * @author Tao Chen
 */
final class JPAMetadataJoin {
    
    String tableName;
    
    List<Column> columns;
    
    List<Column> inversedColumns;

    public JPAMetadataJoin(JPAMetadataPropertyImpl property) {
        List<Class<? extends Annotation>> annotationTypes = new ArrayList<>();
        FieldNode fieldNode = property.unresolved.fieldNode;
        AnnotationNode joinColumnNode = ASMTreeUtils.getAnnotationNode(fieldNode, JoinColumn.class);
        AnnotationNode joinColumnsNode = ASMTreeUtils.getAnnotationNode(fieldNode, JoinColumns.class);
        AnnotationNode joinTableNode = ASMTreeUtils.getAnnotationNode(fieldNode, JoinTable.class);
        if (property.unresolved.primaryAnnotationNode.desc.equals(ASMConstants.MANY_TO_MANY_DESCRIPTOR)) {
            if (joinTableNode == null) {
                throw new IllegalClassException(
                        requireAnnoatation(
                                property, 
                                ASMUtils.toClassName(property.unresolved.primaryAnnotationNode.desc),
                                JoinTable.class
                        )
                );
            }
        }
        if (joinColumnNode != null) {
            annotationTypes.add(JoinColumn.class);
        } else if (joinColumnsNode != null) {
            annotationTypes.add(JoinColumns.class);
        } else if (joinTableNode != null) {
            annotationTypes.add(JoinTable.class);
        }
        if (annotationTypes.isEmpty()) {
            throw new IllegalClassException(
                    requireAnyAnnoatation(
                            property, 
                            ASMUtils.toClassName(property.unresolved.primaryAnnotationNode.desc),
                            JoinColumn.class, 
                            JoinColumns.class, 
                            JoinTable.class
                    )
            );
        }
        if (annotationTypes.size() > 1) {
            throw new IllegalClassException(
                    conflictAnnotations(property, annotationTypes)
            );
        }
        
        JPAMetadataClassImpl referencedSingleIdClass;
        JPAMetadataClassImpl inverseReferencedSingleIdClass = null;
        if (joinTableNode != null || 
                property.unresolved.primaryAnnotationNode.desc
                .equals(ASMConstants.ONE_TO_MANY_DESCRIPTOR)) {
            referencedSingleIdClass = singleIdClass((JPAMetadataClassImpl)property.getDeclaringClass());
            inverseReferencedSingleIdClass = singleIdClass(property.targetClass);
        } else {
            referencedSingleIdClass = singleIdClass(property.targetClass);
            inverseReferencedSingleIdClass = singleIdClass((JPAMetadataClassImpl)property.getDeclaringClass());
        }
        
        if (joinColumnNode != null) {
            this.initByJoinColumnNode(property, joinColumnNode, referencedSingleIdClass);
        } else if (joinColumnsNode != null) {
            this.initByJoinColumnsNode(property, joinColumnsNode, referencedSingleIdClass);
        } else {
            this.initByJoinTableNode(property, joinTableNode, referencedSingleIdClass, inverseReferencedSingleIdClass);
        }
    }
    
    private void initByJoinColumnNode(
            JPAMetadataPropertyImpl property, 
            AnnotationNode joinColumnNode,
            JPAMetadataClassImpl referencedSingleIdClass) {
        this.columns = MACollections.wrap(
                new Column(
                        property,
                        joinColumnNode,
                        "joinColumns", 
                        0, 
                        referencedSingleIdClass
                )
        );
    }
    
    private void initByJoinColumnsNode(
            JPAMetadataPropertyImpl property, 
            AnnotationNode joinColumnsNode,
            JPAMetadataClassImpl referencedSingleIdClass) {
        List<AnnotationNode> joinColumnNodes = ASMTreeUtils.getAnnotationValue(joinColumnsNode, "value");
        if (joinColumnNodes == null || joinColumnNodes.isEmpty()) {
            throw new IllegalClassException(
                    emptyJoinColumns(property, JoinColumns.class, JoinColumn.class)
            );
        }
        if (joinColumnNodes.size() == 1) {
            this.columns = MACollections.wrap(
                    new Column(
                            property,
                            joinColumnNodes.get(0),
                            "joinColumns", 
                            0, 
                            referencedSingleIdClass
                    )
            );
        } else {
            Column[] columns = new Column[joinColumnNodes.size()];
            int index = 0;
            for (AnnotationNode joinColumnNode : joinColumnNodes) {
                columns[index++] = new Column(
                        property,
                        joinColumnNode,
                        "joinColumns",
                        index,
                        null
                );
            }
            this.columns = MACollections.wrap(columns);
        }
    }
    
    private void initByJoinTableNode(
            JPAMetadataPropertyImpl property, 
            AnnotationNode joinTableNode,
            JPAMetadataClassImpl referencedSingleIdClass,
            JPAMetadataClassImpl inverseReferencedSingleIdClass) {
        this.tableName = ASMTreeUtils.getAnnotationValue(joinTableNode, "name", "");
        if (this.tableName.isEmpty()) {
            throw new IllegalClassException(
                    emptyJoinTableName(property, JoinTable.class)
            );
        }
        List<AnnotationNode> joinColumnNodes = ASMTreeUtils.getAnnotationValue(joinTableNode, "joinColumns");
        if (joinColumnNodes == null || joinColumnNodes.isEmpty()) {
            throw new IllegalClassException(
                    emptyJoinTableColumns(
                            property, 
                            JoinTable.class, 
                            "joinColumns", 
                            JoinColumn.class)
            );
        }
        List<AnnotationNode> inverseJoinColumnNodes = ASMTreeUtils.getAnnotationValue(joinTableNode, "inverseJoinColumns");
        if (inverseJoinColumnNodes == null || inverseJoinColumnNodes.isEmpty()) {
            throw new IllegalClassException(
                    emptyJoinTableColumns(
                            property, 
                            JoinTable.class, 
                            "inverseJoinColumnNodes", 
                            JoinColumn.class)
            );
        }
        int count = joinColumnNodes.size();
        if (count == 1) {
            this.columns = MACollections.wrap(
                    new Column(
                            property,
                            joinColumnNodes.get(0),
                            "joinColumns",
                            0,
                            referencedSingleIdClass
                    )
            );
            this.inversedColumns = MACollections.wrap(
                    new Column(
                            property,
                            inverseJoinColumnNodes.get(0),
                            "inverseJoinColumns",
                            0,
                            inverseReferencedSingleIdClass
                    )
            );
        } else {
            Column[] columns = new Column[count];
            Column[] inverseColumns = new Column[count];
            int index;
            
            index = 0;
            for (AnnotationNode joinColumnNode : joinColumnNodes) {
                columns[index++] = new Column(
                        property,
                        joinColumnNode,
                        "joinColumns",
                        index,
                        null
                );
            }
            
            index = 0;
            for (AnnotationNode inverseJoinColumnNode : inverseJoinColumnNodes) {
                inverseColumns[index++] = new Column(
                        property,
                        inverseJoinColumnNode,
                        "inverseJoinColumns",
                        index,
                        null
                );
            }
            
            this.columns = MACollections.wrap(columns);
            this.inversedColumns = MACollections.wrap(inverseColumns);
        }
    }
    
    private static JPAMetadataClassImpl singleIdClass(JPAMetadataClassImpl jpaMetadataClassImpl) {
        if (jpaMetadataClassImpl.idProperty.targetClass != null) {
            return null; //Embedded id, not single id
        }
        return jpaMetadataClassImpl;
    }

    public boolean likeBedirectionalAssociation(JPAMetadataJoin join) {
        if (join == null) {
            return false;
        }
        if (this.tableName != null && join.tableName != null) {
            return 
                    DbIdentifiers.laxIdentifier(this.tableName)
                    .equals(DbIdentifiers.laxIdentifier(join.tableName)) &&
                    like(this.columns, join.inversedColumns) && 
                    like(this.inversedColumns, join.columns);
        }
        if (this.tableName == null && join.tableName == null) {
            return like(this.columns, join.columns);
        }
        return false;
    }
    
    public boolean isReadonly() {
        for (Column column : columns) {
            if (column.insertable || column.updatable) {
                return false;
            }
        }
        if (this.inversedColumns != null) {
            for (Column inversedColumn : this.inversedColumns) {
                if (inversedColumn.insertable || inversedColumn.updatable) {
                    return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        if (this.tableName != null) {
            StringBuilder builder = new StringBuilder();
            builder
            .append("@JoinTable(name = \"")
            .append(this.tableName)
            .append("\", joinColumns = ");
            appendColumns(this.columns, builder);
            builder.append(", inverseJoinColumns = ");
            appendColumns(this.inversedColumns, builder);
            builder.append(')');
            return builder.toString();
        }
        if (this.columns.size() > 1) {
            StringBuilder builder = new StringBuilder();
            builder.append("@JoinColumns(");
            appendColumns(this.columns, builder);
            builder.append(')');
            return builder.toString();
        } 
        return this.columns.get(0).toString();
    }
    
    private static void appendColumns(List<Column> columns, StringBuilder builder) {
        if (columns.size() == 1) {
            builder.append(columns.get(0));
        } else {
            builder.append("{ ");
            boolean addComma = false;
            for (Column column : columns) {
                if (addComma) {
                    builder.append(", ");
                } else {
                    addComma = true;
                }
                builder.append(column);
            }
            builder.append(" }");
        }
    }

    private static boolean like(List<Column> a, List<Column> b) {
        if (a.size() != b.size()) {
            return false;
        }
        a = sort(a);
        b = sort(b);
        for (int i = a.size() - 1; i >= 0; i--) {
            if (!like(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    private static List<Column> sort(List<Column> columns) {
        List<Column> list = new ArrayList<>(columns);
        Collections.sort(list, new Comparator<Column>() {
            @Override
            public int compare(Column c1, Column c2) {
                return DbIdentifiers.laxIdentifier(c1.name).compareTo(
                        DbIdentifiers.laxIdentifier(c2.name)
                );
            }
        });
        return list;
    }
    
    private static boolean like(Column a, Column b) {
        return  
                DbIdentifiers.laxIdentifier(a.name).equals(
                        DbIdentifiers.laxIdentifier(b.name)
                ) 
                &&
                DbIdentifiers.laxIdentifier(a.referencedColumnName).equals(
                        DbIdentifiers.laxIdentifier(b.referencedColumnName)
                );
    }
    
    private static class Column {
        
        String name;
        
        String referencedColumnName;
        
        boolean insertable;
        
        boolean updatable;
        
        Column(
                JPAMetadataPropertyImpl property, 
                AnnotationNode joinColumnNode,
                String joinColumnsKindName,
                int index,
                JPAMetadataClassImpl referencedSingleIdClass) {
            this.name = ASMTreeUtils.getAnnotationValue(joinColumnNode, "name", "");
            if (this.name.isEmpty()) {
                throw new IllegalClassException(
                        requireJoinColumnName(
                                property, 
                                joinColumnsKindName, 
                                index
                        )
                );
            }
            this.referencedColumnName = ASMTreeUtils.getAnnotationValue(joinColumnNode, "referencedColumnName", "");
            if (this.referencedColumnName.isEmpty()) {
                if (referencedSingleIdClass == null) {
                    throw new IllegalClassException(
                            requireReferencedColumnName(
                                    property, 
                                    joinColumnsKindName, 
                                    index
                            )
                    );
                }
                this.referencedColumnName = referencedSingleIdClass.idProperty.unresolved.singleColumnName;
            }
            this.insertable = ASMTreeUtils.getAnnotationValue(joinColumnNode, "insertable", true);
            this.updatable = ASMTreeUtils.getAnnotationValue(joinColumnNode, "updatable", false);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder
            .append("@JoinColumn(name = \"")
            .append(this.name)
            .append("\", referencedColumnName = \"")
            .append(this.referencedColumnName)
            .append('"');
            if (!this.insertable) {
                builder.append(", insertable = false");
            }
            if (!this.updatable) {
                builder.append(", updatable = false");
            }
            builder.append(')');
            return builder.toString();
        }
    }
    
    @I18N    
    private static native String requireAnnoatation(
                JPAMetadataPropertyImpl property,
                String associationAnnotationTypeName,
                Class<? extends JoinTable> joinTableTypeConstant);
        
    @I18N    
    private static native String requireAnyAnnoatation(
                JPAMetadataPropertyImpl property,
                String associationAnnotationTypeName,
                Class<? extends JoinColumn> joinColumnTypeConstant,
                Class<? extends JoinColumns> joinColumnsTypeConstant,
                Class<? extends JoinTable> joinTableTypeConstant);

    @I18N    
    private static native String conflictAnnotations(
                JPAMetadataPropertyImpl property,
                Collection<Class<? extends Annotation>> annotationTypes);
        
    @I18N    
    private static native String emptyJoinColumns(
                JPAMetadataPropertyImpl property, 
                Class<JoinColumns> joinColumnsTypeConstant, 
                Class<JoinColumn> joinColumnTypeConstant);
        
    @I18N    
    private static native String emptyJoinTableName(
                JPAMetadataPropertyImpl property,
                Class<JoinTable> joinTableTypeConstant);
        
    @I18N    
    private static native String emptyJoinTableColumns(
                JPAMetadataPropertyImpl property,
                Class<JoinTable> joinTableTypeConstant,
                String attributeName,
                Class<JoinColumn> joinColumnTypeConstant);
        
    @I18N    
    private static native String requireJoinColumnName(
                JPAMetadataPropertyImpl property,
                String joinColumnsKindName,
                int index);
        
    @I18N    
    private static native String requireReferencedColumnName(
                JPAMetadataPropertyImpl property,
                String joinColumnsKindName,
                int index);
}
