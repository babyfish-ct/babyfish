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
package org.babyfish.model.metadata;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenComparator;
import org.babyfish.collection.FrozenEqualityComparator;
import org.babyfish.model.ModelType;
import org.babyfish.model.metadata.internal.ModelClassImpl;

/**
 * @author Tao Chen
 */
public interface ModelClass {
    
    ModelType getType();
    
    Class<?> getJavaType();
    
    ModelClass getSuperClass();
    
    Map<String, ModelProperty> getDeclaredProperties();
    
    ModelProperty getDeclaredProperty(String name);
    
    ModelProperty getProperty(String name);
        
    Map<String, ModelProperty> getProperties();
    
    ModelProperty getProperty(int id);
    
    List<ModelProperty> getPropertyList();
    
    <T> Comparator<T> getDefaultComparator();
    
    <T> EqualityComparator<T> getDefaultEqualityComparator();
    
    default <T> FrozenComparator<T> getComparator(int ... scalarPropertyIds) {  
        ComparatorPart[] parts = new ComparatorPart[scalarPropertyIds.length];
        for (int i = parts.length - 1; i >= 0; i--) {
            parts[i] = new ComparatorPart(scalarPropertyIds[i]);
        }
        return this.getComparator(parts);
    }
    
    default <T> FrozenComparator<T> getComparator(String ... scalarPropertyNames) {
        ComparatorPart[] parts = new ComparatorPart[scalarPropertyNames.length];
        for (int i = parts.length - 1; i >= 0; i--) {
            parts[i] = new ComparatorPart(this.getProperty(scalarPropertyNames[i]).getId());
        }
        return this.getComparator(parts);
    }
    
    <T> FrozenComparator<T> getComparator(ComparatorPart ... parts);
    
    default <T> FrozenEqualityComparator<T> getEqualityComparator(int ... scalarPropertyIds) {  
        ComparatorPart[] parts = new ComparatorPart[scalarPropertyIds.length];
        for (int i = parts.length - 1; i >= 0; i--) {
            parts[i] = new ComparatorPart(scalarPropertyIds[i]);
        }
        return this.getEqualityComparator(parts);
    }
    
    default <T> FrozenEqualityComparator<T> getEqualityComparator(String ... scalarPropertyNames) {
        ComparatorPart[] parts = new ComparatorPart[scalarPropertyNames.length];
        for (int i = parts.length - 1; i >= 0; i--) {
            parts[i] = new ComparatorPart(this.getProperty(scalarPropertyNames[i]).getId());
        }
        return this.getEqualityComparator(parts);
    }
    
    <T> FrozenEqualityComparator<T> getEqualityComparator(ComparatorPart ... parts);
    
    static ModelClass of(Class<?> javaType) {
        return ModelClassImpl.getClass(javaType);
    }
}
