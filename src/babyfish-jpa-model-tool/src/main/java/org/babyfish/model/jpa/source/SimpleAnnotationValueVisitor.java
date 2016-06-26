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

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Tao Chen
 */
class SimpleAnnotationValueVisitor implements AnnotationValueVisitor<Object, Void> {

    @Override
    public Object visit(AnnotationValue av, Void p) {
        return null;
    }

    @Override
    public Object visit(AnnotationValue av) {
        return null;
    }

    @Override
    public Object visitBoolean(boolean b, Void p) {
        return b;
    }

    @Override
    public Object visitByte(byte b, Void p) {
        return b;
    }

    @Override
    public Object visitChar(char c, Void p) {
        return c;
    }

    @Override
    public Object visitDouble(double d, Void p) {
        return d;
    }

    @Override
    public Object visitFloat(float f, Void p) {
        return f;
    }

    @Override
    public Object visitInt(int i, Void p) {
        return i;
    }

    @Override
    public Object visitLong(long i, Void p) {
        return i;
    }

    @Override
    public Object visitShort(short s, Void p) {
        return s;
    }

    @Override
    public Object visitString(String s, Void p) {
        return s;
    }

    @Override
    public Object visitType(TypeMirror t, Void p) {
        return null;
    }

    @Override
    public Object visitEnumConstant(VariableElement c, Void p) {
        return c.getSimpleName().toString();
    }

    @Override
    public Object visitAnnotation(AnnotationMirror a, Void p) {
        return null;
    }

    @Override
    public Object visitArray(List<? extends AnnotationValue> vals, Void p) {
        Object[] arr = new Object[vals.size()];
        int index = 0;
        for (AnnotationValue annotationValue : vals) {
            arr[index++] = annotationValue.accept(this, null);
        }
        return arr;
    }

    @Override
    public Object visitUnknown(AnnotationValue av, Void p) {
        return null;
    }
}
