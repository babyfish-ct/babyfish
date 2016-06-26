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
package org.babyfish.lang.bytecode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;

/**
 * @author Tao Chen
 */
public class ScopedMethodVisitorBuilder {
    
    private int access;
    
    private String name;
    
    private String[] exceptionTypes;
    
    private String typeParameterSignatureClause;
    
    private Parameter selfParameter;
    
    private Parameter outputParameter;
    
    private String exceptionSignatureClause;
    
    private Map<String, Parameter> orderedParameters = new LinkedHashMap<>();
    
    public ScopedMethodVisitorBuilder(int access, String name, String ... exceptionTypes) {
        this.access = access;
        this.name = name;
        this.exceptionTypes = exceptionTypes;
    }
    
    public ScopedMethodVisitorBuilder typeParameterSignatureClause(String typeParameterSignatureClause) {
        if (typeParameterSignatureClause == null || typeParameterSignatureClause.isEmpty()) {
            this.typeParameterSignatureClause = null;
            return this;
        }
        if (typeParameterSignatureClause.charAt(0) != '<' ||
                typeParameterSignatureClause.charAt(typeParameterSignatureClause.length() - 1) != '>') {
            throw new IllegalArgumentException("'typeParameterSignatureClause' must start with '<' and end with '>'");
        }
        this.typeParameterSignatureClause = typeParameterSignatureClause;
        return this;
    }
    
    public ScopedMethodVisitorBuilder exceptionSignatureClause(String exceptionSignatureClause) {
        if (exceptionSignatureClause == null || exceptionSignatureClause.isEmpty()) {
            this.exceptionSignatureClause = null;
            return this;
        }
        if (exceptionSignatureClause.charAt(0) != '^') {
            throw new IllegalArgumentException("'exceptionSignatrueClause' must start with '^'");
        }
        this.exceptionSignatureClause = exceptionSignatureClause;
        return this;
    }
    
    public ScopedMethodVisitorBuilder self(String desc) {
        return this.self(desc, null);
    }
    
    public ScopedMethodVisitorBuilder self(String desc, String signature) {
        boolean isStatic = (this.access & Opcodes.ACC_STATIC) != 0;
        if (desc == null) {
            if (!isStatic) {
                throw new IllegalArgumentException("desc must not be null because current method isn't static");
            }
        } else {
            if (isStatic) {
                throw new IllegalArgumentException("desc must be null because current method is static");
            }
            this.selfParameter = new Parameter("this", desc, signature);
        }
        return this;
    }
    
    public ScopedMethodVisitorBuilder parameter(String name, String desc) {
        return this.parameter(name, desc, null);
    }
    
    public ScopedMethodVisitorBuilder parameter(String name, String desc, String signature) {
        Parameter parameter = new Parameter(name, desc, signature);
        if (this.orderedParameters.put(name, parameter) != null) {
            throw new IllegalArgumentException(
                    "Duplicated parameter name \""
                    + name
                    + "\""
            );
        }
        return this;
    }
    
    public ScopedMethodVisitorBuilder output(String desc) {
        return this.output(desc, null);
    }
    
    public ScopedMethodVisitorBuilder output(String desc, String signature) {
        if (desc.equals("V")) {
            this.outputParameter = null;
        } else {
            this.outputParameter = new Parameter(null, desc, signature);
        }
        return this;
    }
    
    public ScopedMethodVisitor build(ClassVisitor cv) {
        if ((this.access & Opcodes.ACC_STATIC) == 0 && this.selfParameter == null) {
            throw new IllegalStateException("No static requires self");
        }
        
        VariableScopeBuilder scopeBuilder = new VariableScopeBuilder();
        if (this.selfParameter != null) {
            scopeBuilder.parameter(
                    this.selfParameter.name, 
                    this.selfParameter.desc,
                    this.selfParameter.signature
            );
        }
        
        StringBuilder builder = new StringBuilder();
        boolean useSignature = this.selfParameter != null && this.selfParameter.signature != null;
        builder.append('(');
        for (Parameter parameter : this.orderedParameters.values()) {
            builder.append(parameter.desc);
            if (parameter.signature != null) {
                useSignature = true;
            }
            scopeBuilder.parameter(parameter.name, parameter.desc, parameter.signature);
        }
        builder.append(')');
        if (this.outputParameter == null) {
            builder.append('V');
        } else {
            builder.append(this.outputParameter.desc);
            if (this.outputParameter.signature != null) {
                useSignature = true;
            }
        }
        String desc = builder.toString();
        String signature = null;
        if (useSignature) {
            builder = new StringBuilder();
            if (this.typeParameterSignatureClause != null) {
                builder.append(this.typeParameterSignatureClause);
            }
            builder.append('(');
            for (Parameter parameter : this.orderedParameters.values()) {
                builder.append(parameter.signature != null ? parameter.signature : parameter.desc);
            }
            builder.append(')');
            if (this.outputParameter == null) {
                builder.append('V');
            } else {
                builder.append(this.outputParameter.signature != null ? this.outputParameter.signature : this.outputParameter.desc);
            }
            if (this.exceptionSignatureClause != null) {
                builder.append(this.exceptionSignatureClause);
            }
            signature = builder.toString();
        }
        MethodVisitor mv = cv.visitMethod(
                this.access, 
                this.name, 
                desc, 
                signature, 
                this.exceptionTypes
        );
        return new ScopedMethodVisitor(scopeBuilder, mv);
    }
    
    public static ScopedMethodVisitor build(
            ClassVisitor cv,
            String selfDescriptor,
            int access,
            String name,
            String desc,
            String signature,
            String[] exceptions,
            Function<Integer, String> parameterNameLabmda) {
        
        if (selfDescriptor != null) {
            access &= ~Opcodes.ACC_STATIC;
        } else {
            access |= Opcodes.ACC_STATIC;
        }
        
        String typeParameterSignatureClause = null;
        String exceptionSignatureClause = null;
        String[] argumentSignatures = null;
        String returnSignatrue = null;
        if (signature != null) {
            int parenthesisIndex = signature.indexOf('(');
            if (parenthesisIndex != 0) {
                typeParameterSignatureClause = signature.substring(0, parenthesisIndex);
            }
            int argEndIndex = signature.indexOf(')');
            argumentSignatures = splitArumentSignature(signature, parenthesisIndex + 1, argEndIndex); 
            int exceptionIndex = signature.indexOf('^', argEndIndex + 1);
            if (exceptionIndex != -1) {
                returnSignatrue = signature.substring(argEndIndex + 1, exceptionIndex);
                exceptionSignatureClause = signature.substring(exceptionIndex);
            } else {
                returnSignatrue = signature.substring(argEndIndex + 1);
            }
        }
        
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        Type returnType = Type.getReturnType(desc);
        
        ScopedMethodVisitorBuilder builder = 
                new ScopedMethodVisitorBuilder(
                        access, 
                        name, 
                        exceptions
                );
        builder
        .typeParameterSignatureClause(typeParameterSignatureClause)
        .exceptionSignatureClause(exceptionSignatureClause)
        .self(selfDescriptor);
        for (int i = 0; i < argumentTypes.length; i++) {
            builder.parameter(
                    parameterNameLabmda.apply(i), 
                    argumentTypes[i].getDescriptor(), 
                    argumentSignatures != null ? argumentSignatures[i] : null
            );
        }
        builder.output(returnType.getDescriptor(), returnSignatrue);
        return builder.build(cv);
    }
    
    private static String[] splitArumentSignature(String signature, int argBeginIndex, int argEndIndex) {
        
        List<String> list = new ArrayList<>();
        int depth = 0;
        int complexTypeStart = -1;
        boolean requiresSemi = false;
        boolean metGeneric = false;
        
        for (int i = argBeginIndex; i < argEndIndex; i++) {
    
            char c = signature.charAt(i);
            if (depth == 0 && !requiresSemi) {
                boolean isPrimitve = false;
                switch (c) {
                case 'Z':
                case 'C':
                case 'B':
                case 'S':
                case 'I':
                case 'J':
                case 'F':
                case 'D':
                    list.add(null);
                    isPrimitve = true;
                    break;
                }
                if (isPrimitve) {
                    complexTypeStart = -1;
                    continue;
                }
            }
            
            switch (c) {
            case '<':
                depth++;
                metGeneric = true;
                break;
            case '>':
                depth--;
                break;
            case ';':
                if (depth == 0) {
                    if (metGeneric) {
                        list.add(signature.substring(complexTypeStart, i + 1));
                    } else {
                        list.add(null);
                    }
                    metGeneric = false;
                    complexTypeStart = -1;
                    requiresSemi = false;
                }
                break;
            case '[':
                if (depth == 0 && complexTypeStart == -1) {
                    complexTypeStart = i;
                }
                break;
            case 'T':
                if (depth == 0 && !requiresSemi) {
                    metGeneric = true;
                    requiresSemi = true;
                    if (complexTypeStart == -1) {
                        complexTypeStart = i;
                    }
                }
                break;
            case 'L':
                if (depth == 0 && !requiresSemi) {
                    requiresSemi = true;
                    if (complexTypeStart == -1) {
                        complexTypeStart = i;
                    }
                }
                break;
            }
        }
        
        if (list.isEmpty()) {
            return null;
        }
        return list.toArray(new String[list.size()]);
    }

    private static class Parameter {
        
        String name;
        
        String desc;
        
        String signature;

        public Parameter(String name, String desc, String signature) {
            this.name = name;
            this.desc = desc;
            this.signature = signature;
        }
    }
}
