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

/**
 * @author Tao Chen
 */
public interface VariableScope extends AutoCloseable {
    
    VariableScope declare(String name, String desc);
    
    VariableScope declare(String name, String desc, String signature);
    
    VariableScope declareImmediately(String name, String desc);
    
    VariableScope declareImmediately(String name, String desc, String signature);
    
    VariableScope store(String name);
    
    VariableScope load(String name);
    
    int slot(String name);
    
    String descriptor(String name);
    
    String allocateHiddenName();
    
    VariableScope createSubScope();
    
    @Override
    void close();
}
