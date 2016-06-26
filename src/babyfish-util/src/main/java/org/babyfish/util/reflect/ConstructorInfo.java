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
package org.babyfish.util.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.TypeVariable;
import java.util.List;

/**
 * @author Tao Chen
 */
public final class ConstructorInfo<T> extends MethodBase {

    ConstructorInfo(Constructor<T> constructor, ClassInfo<?> declaringType) {
        super (constructor, declaringType);
    }

    @SuppressWarnings("unchecked")
    public Constructor<T> getRawConstructor() {
        return (Constructor<T>)this.member;
    }
    
    @Override
    public String getName() {
        return this.getDeclaringClass().getSimpleName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TypeVariable<Constructor<?>>> getTypeParameters() {
        return super.getTypeParameters();
    }
    
}
