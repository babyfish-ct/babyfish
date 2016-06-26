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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.babyfish.lang.I18N;
import org.babyfish.lang.bytecode.ASMTreeUtils;
import org.babyfish.lang.instrument.IllegalClassException;
import org.babyfish.org.objectweb.asm.tree.ClassNode;
import org.babyfish.org.objectweb.asm.tree.MethodNode;

/**
 * @author Tao Chen
 */
public class MetadataClass {
    
    private static final Pattern LOCALE_PATTERN = Pattern.compile("_[a-z][a-z](_[A-Z][A-Z]){0,2}");
    
    private static final Pattern PARAMTER_PATTERN = Pattern.compile("\\{\\d+\\}");

    private String className;
    
    private Map<String, MetadataMethod> declaredMethods;
    
    public MetadataClass(ClassNode classNode, File classFile) {
        this.className = classNode.name.replace('/', '.');
        if (this.className.equals(MetadataClass.class.getName())) {
            throw new AssertionError("Internal bug");
        }
        if (classNode.methods == null || classNode.methods.isEmpty()) {
            throw new AssertionError("Internal bug");
        }
        Set<String> methodNames = new HashSet<>();
        Map<String, MetadataMethod> declaredMap = new HashMap<>();
        for (MethodNode methodNode : classNode.methods) {
            if (ASMTreeUtils.getAnnotationNode(methodNode, I18N.class) != null) {
                if (!methodNames.add(methodNode.name)) {
                    throw new IllegalClassException(
                            "The class \""
                            + this.className
                            + "\" is illegal, several methods which are marked by \"@"
                            + I18N.class.getName()
                            + "\" have the same name \""
                            + methodNode.name
                            + "\", this isn't allowed because overload of I18N method is forbidden"
                    );
                }
                declaredMap.put(key(methodNode.name, methodNode.desc), new MetadataMethod(this, methodNode));
            }
        }
        if (declaredMap.isEmpty()) {
            throw new AssertionError("Internal bug");
        }
        this.declaredMethods = declaredMap;
        
        this.validateResources(classFile);
    }

    public String getClassName() {
        return this.className;
    }
    
    public MetadataMethod getDeclaredMethod(String name, String desc) {
        return this.declaredMethods.get(key(name, desc));
    }
    
    @Override
    public String toString() {
        return this.className;
    }
    
    private static String key(String name, String desc) {
        return name + '(' + desc + ')';
    }
    
    private void validateResources(File classFile) {
        int lastDotIndex = this.className.lastIndexOf('.');
        String shortName = lastDotIndex == -1 ? this.className : this.className.substring(lastDotIndex + 1);
        File defaultResourceFile = new File(classFile.getParentFile(), shortName + ".properties");
        if (!defaultResourceFile.exists()) {
            throw new IllegalClassException(
                    "Illegal class \"" +
                    this.className +
                    "\", it requires uses @" +
                    I18N.class.getName() +
                    " but the default resource file \"" +
                    defaultResourceFile +
                    "\" does not exists"
            );
        }
        File[] otherResourceFiles = classFile.getParentFile().listFiles((dir, name) -> {
            if (name.endsWith(".properties") && name.startsWith(shortName)) {
                String localeName = name.substring(shortName.length(), name.length() - 11);
                if (localeName.isEmpty()) {
                    return false;
                }
                if (!LOCALE_PATTERN.matcher(localeName).matches()) {
                    throw new IllegalClassException(
                            "The resource file\"" +
                            name +
                            "\" has illegal file name, its locale name \"" +
                            localeName +
                            "\" does not match the pattern \"" +
                            LOCALE_PATTERN.pattern() +
                            "\""
                    );
                }
                return true;
            }
            return false;
        });
        this.validateResource(defaultResourceFile);
        for (File otherResourceFile : otherResourceFiles) {
            this.validateResource(otherResourceFile);
        }
    }
    
    private void validateResource(File resourceFile) {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(resourceFile.getAbsolutePath())) {
            properties.load(inputStream);
        } catch (IOException ex) {
            throw new IllegalClassException(
                    "Failed to validate the resource file \"" +
                    resourceFile +
                    "\" because of some IO exception",
                    ex
            );
        }
        this.validateProperties(resourceFile, properties);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void validateProperties(File resourceFile, Properties properties) {
        Map<String, String> map = new LinkedHashMap<>((Map)properties);
        for (MetadataMethod method : this.declaredMethods.values()) {
            String methodName = method.getName();
            String value = map.remove(methodName);
            if (value == null) {
                throw new IllegalClassException(
                        "The I18N method \"" +
                        methodName +
                        "\" of class \"" +
                        this.className +
                        "\" is not mapped by the resource file \"" +
                        resourceFile +
                        "\""
                );
            }
            this.validateMessage(resourceFile, method, value);
        }
        if (!map.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            boolean addComma = false;
            for (String key : map.keySet()) {
                if (addComma) {
                    builder.append(", ");
                } else {
                    builder.append(key);
                }
            }
            throw new IllegalClassException(
                    "The message(s) \"" +
                    builder.toString() +
                    "\" of the resource \"" +
                    resourceFile +
                    "\" is not mapped by the I18N methods of class \"" +
                    this.className +
                    "\""
            );
        }
    }
    
    private void validateMessage(File resourceFile, MetadataMethod method, String message) {
        NavigableSet<Integer> parameterIndexes = new TreeSet<Integer>();
        Matcher matcher = PARAMTER_PATTERN.matcher(message);
        while (matcher.find()) {
            int parameterIndex = Integer.parseInt(message.substring(matcher.start() + 1, matcher.end() - 1));
            parameterIndexes.add(parameterIndex);
        }
        if (!parameterIndexes.isEmpty()) {
            Integer prev = parameterIndexes.first();
            if (prev.intValue() != 0) {
                throw new IllegalClassException(
                        "The message key \"" +
                        method.getName() +
                        "\" of resource file \"" +
                        resourceFile + 
                        "\" is illegal, its message miss the parameter {0}"
                );
            }
            while (true) {
                Integer cur = parameterIndexes.higher(prev);
                if (cur == null) {
                    break;
                }
                if (cur.intValue() != prev.intValue() + 1) {
                    throw new IllegalClassException(
                            "The message key \"" +
                            method.getName() +
                            "\" of resource file \"" +
                            resourceFile + 
                            "\" is illegal, its message miss the parameter {" +
                            (prev.intValue() + 1) +
                            "}"
                    );
                }
                prev = cur;
            }
        }
        if (parameterIndexes.size() != method.getParameterCount()) {
            throw new IllegalClassException(
                    "The message key \"" +
                    method.getName() +
                    "\" of resource file \"" +
                    resourceFile + 
                    "\" is illegal, its parameter count is " +
                    parameterIndexes.size() +
                    " but the parameter count of the method \"" +
                    method.getName() +
                    "\" the class \"" +
                    this.className +
                    "\" is " +
                    method.getParameterCount()
            );
        }
    }
}
