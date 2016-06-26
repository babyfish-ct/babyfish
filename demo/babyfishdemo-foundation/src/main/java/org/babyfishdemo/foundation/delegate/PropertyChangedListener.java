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
package org.babyfishdemo.foundation.delegate;

import java.util.EventListener;

import org.babyfish.lang.Delegate;

/**
 * @author Tao Chen
 */
/*
 * The annotation @Delegate tell the maven plugin of babyfish 
 * that the byte-code of methods "combine" and "remove" of this 
 * interface need to be replaced during compilation
 */
@Delegate
@FunctionalInterface
public interface PropertyChangedListener extends EventListener {

    void propertyChanged(PropertyChanagedEvent e);
    
    /*
     * Unfortunately, Java8 does not allow static method of interface to be native,
     * so you can write any fake code without logic here(suggest throwing an exception) 
     * and let the maven-plugin of babyfish replace its byte-code during compilation 
     */
    static PropertyChangedListener combine(
            PropertyChangedListener a, 
            PropertyChangedListener b) {
        throw new UnsupportedOperationException("Instrument required");
    }
    
    /*
     * Unfortunately, Java8 does not allow static method of interface to be native,
     * so you can write any fake code without logic here(suggest throwing an exception) 
     * and let the maven-plugin of babyfish replace its byte-code during compilation 
     */
    static PropertyChangedListener remove(
            PropertyChangedListener a, 
            PropertyChangedListener b) {
        throw new UnsupportedOperationException("Instrument required");
    }
}
