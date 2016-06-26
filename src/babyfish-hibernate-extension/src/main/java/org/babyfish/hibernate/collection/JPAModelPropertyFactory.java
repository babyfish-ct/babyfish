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
package org.babyfish.hibernate.collection;

import org.babyfish.model.jpa.metadata.JPAModelProperty;
import org.babyfish.model.metadata.ModelClass;

class JPAModelPropertyFactory {

    public static JPAModelProperty of(String role) {
        int lastDotIndex = role.lastIndexOf('.');
        String className = role.substring(0, lastDotIndex);
        String propertyName = role.substring(lastDotIndex +1);
        Class<?> javaType;
        try {
            javaType = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new AssertionError("Internal bug", ex);
        }
        return (JPAModelProperty)ModelClass.of(javaType).getProperties().get(propertyName);
    }
    
    private JPAModelPropertyFactory() {
    }
}
