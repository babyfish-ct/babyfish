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
package org.babyfish.lang.i18n.metadata;

import java.util.List;

import org.babyfish.lang.I18N;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.org.objectweb.asm.tree.MethodNode;
import org.babyfish.org.objectweb.asm.tree.ParameterNode;

/**
 * @author Tao Chen
 */
public class MetadataMethod {

    private MetadataClass declaringClass;
    
    private String name;
    
    private int parameterSlotCount;
        
    private String[] parameterDescriptors;
    
    private String[] parameterInternalNames;
    
    private String[] parameterTypeNames;
    
    private String[] parameterNames;
    
    private transient String toString;
    
    MetadataMethod(MetadataClass declaringClass, MethodNode methodNode) {
        this.declaringClass = declaringClass;
        this.name = methodNode.name;
        this.parameterSlotCount = (Type.getArgumentsAndReturnSizes(methodNode.desc) >> 2) - 1;
        Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
        String[] descArr = new String[argumentTypes.length];
        for (int i = descArr.length - 1; i >= 0; i--) {
            descArr[i] = argumentTypes[i].getDescriptor();
        }
        this.parameterDescriptors = descArr;
        
        String[] internalNameArr = new String[argumentTypes.length];
        String[] typeArr = new String[argumentTypes.length];
        for (int i = typeArr.length - 1; i >= 0; i--) {
            internalNameArr[i] = internalName(argumentTypes[i]);
            typeArr[i] = argumentTypes[i].getClassName();
        }
        this.parameterInternalNames = internalNameArr;
        this.parameterTypeNames = typeArr;
        
        List<ParameterNode> parameterNodes = methodNode.parameters;
        if (parameterNodes != null) {
            String[] nameArr = new String[parameterNodes.size()];
            for (int i = nameArr.length - 1; i >= 0; i--) {
                nameArr[i] = parameterNodes.get(i).name;
            }
            this.parameterNames = nameArr;
        }
        
        this.validate(methodNode);
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getParameterCount() {
        return this.parameterTypeNames.length;
    }
    
    public int getParameterSlotCount() {
        return this.parameterSlotCount;
    }
    
    public String getParameterDescriptor(int index) {
        return this.parameterDescriptors[index];
    }
    
    public String getParameterInternalName(int index) {
        return this.parameterInternalNames[index];
    }
    
    public String getParameterTypeName(int index) {
        return this.parameterTypeNames[index];
    }
    
    public String getParameterName(int index) {
        if (this.parameterNames == null) {
            if (index < 0 || index >= this.parameterTypeNames.length) {
                throw new IndexOutOfBoundsException(
                        "index must between 0 and " + 
                        this.parameterTypeNames.length
                );
            }
            return "arg" + index;
        }
        return this.parameterNames[index];
    }
    
    private void validate(MethodNode methodNode) {
        int access = methodNode.access;
        if ((access & Opcodes.ACC_STATIC) == 0) {
            throw new IllegalClassException(
                    "The method \""
                    + this
                    + "\" must be static because it's marked by \"@"
                    + I18N.class.getName()
                    + "\""
            );
        }
        if ((access & Opcodes.ACC_NATIVE) == 0) {
            throw new IllegalClassException(
                    "The method \""
                    + this
                    + "\" must be native because it's marked by \"@"
                    + I18N.class.getName()
                    + "\""
            );
        }
        if (!methodNode.desc.endsWith(")Ljava/lang/String;")) {
            throw new IllegalClassException(
                    "The method \""
                    + this
                    + "\" must return \"java.lang.String\" because it's marked by \"@"
                    + I18N.class.getName()
                    + "\""
            );
        }
        if (methodNode.exceptions != null && !methodNode.exceptions.isEmpty()) {
            throw new IllegalClassException(
                    "The method \""
                    + this
                    + "\"can't throw any exception because it's marked by \"@"
                    + I18N.class.getName()
                    + "\""
            );
        }
        
        // TODO: Complex validation logic to check whether java method can match resource message
        // (1) Whether parameter count can be matched
        // (2) Whether the min parameter index in the resource file is "{0}"
        // (3) Whether the parameters in the resource file are sequential.(Eg: "{0}{1}{3}{4}" is invalid, because "{2}" is missing) 
        // (4) All the properties must be validated: such as "default", "en", "en_US", "fr", "fr_FR", "zh", "zh_CN"
    }

    @Override
    public String toString() {
        String toString = this.toString;
        if (toString == null) {
            this.toString = toString = this.calcString();
        }
        return toString;
    }
    
    private String calcString() {
        StringBuilder builder = new StringBuilder();
        builder
        .append(this.declaringClass.getClassName())
        .append('.')
        .append(this.name)
        .append('(');
        
        String[] typeArr = this.parameterTypeNames;
        String[] nameArr = this.parameterNames;
        int len = typeArr.length;
        boolean addComma = false;
        for (int i = 0; i < len; i++) {
            if (addComma) {
                builder.append(", ");
            } else {
                addComma = true;
            }
            builder.append(typeArr[i]);
            if (nameArr != null) {
                builder.append(' ').append(nameArr[i]);
            }
        }
        
        builder.append(')');
        
        return builder.toString();
    }
    
    private static String internalName(Type type) {
        switch (type.getSort()) {
        case Type.BOOLEAN:
        case Type.CHAR:
        case Type.BYTE:
        case Type.SHORT:
        case Type.INT:
        case Type.LONG:
        case Type.FLOAT:
        case Type.DOUBLE:
            return type.getDescriptor();
        default:
            return type.getInternalName();
        }
    }
}
