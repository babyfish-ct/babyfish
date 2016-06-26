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

import java.lang.annotation.Annotation;
import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * @author Tao Chen
 */
public class ElementHelper {

    @SafeVarargs
    public static boolean containsAnyAnnotation(
            Element element, 
            Class<? extends Annotation> ... annotationTypes) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            for (Class<?> annotationType : annotationTypes) {
                if (annotationType != null && 
                        annotationType.getName().equals(mirror.getAnnotationType().toString())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static Object getAnnotationValue(
            Element element, 
            Class<? extends Annotation> annotationType,
            String parameterName) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (annotationType.getName().equals(mirror.getAnnotationType().toString())) {
                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
                    mirror.getElementValues().entrySet()) {
                    if (entry.getKey().getSimpleName().toString().equals(parameterName)) {
                        return entry.getValue().accept(new SimpleAnnotationValueVisitor(), null);
                    }
                }
            }
        }
        return null;
    }
    
    public static <E extends Enum<E>> E getAnnotationEnumValue(
            Element element, 
            Class<? extends Annotation> annotationType,
            String parameterName,
            Class<E> enumType) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (annotationType.getName().equals(mirror.getAnnotationType().toString())) {
                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
                    mirror.getElementValues().entrySet()) {
                    if (entry.getKey().getSimpleName().toString().equals(parameterName)) {
                        return (E)Enum.valueOf(
                                enumType, 
                                (String)entry.getValue().accept(new SimpleAnnotationValueVisitor(), null)
                        );
                    }
                }
            }
        }
        return null;
    }
    
    private ElementHelper() {}
}
