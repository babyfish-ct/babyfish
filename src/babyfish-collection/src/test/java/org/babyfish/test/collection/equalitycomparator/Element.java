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
package org.babyfish.test.collection.equalitycomparator;


/**
 * @author Tao Chen
 */
public class Element {
    
    private String code;
    
    private String name;
    
    public Element(String code, String name) {
        super();
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "{code:" + this.code + ",name:" + this.name + '}'; 
    }

    /*
     * If the collection is work together with a EqualityComparator,
     * the element's hashCode() will never been invoked.
     * So here, specially, let hashCode() always throws an exception
     * so that the test case can make sure that it has never been invoked.
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }
    
    /*
     * If the collection is work together with a EqualityComparator,
     * the element's equals(Object) will never been invoked.
     * So here, specially, let equals() always throws an exception
     * so that the test case can make sure that it has never been invoked.
     */
    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }

}
