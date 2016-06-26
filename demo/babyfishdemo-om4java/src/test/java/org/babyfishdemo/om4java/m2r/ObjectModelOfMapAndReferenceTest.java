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
package org.babyfishdemo.om4java.m2r;

import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class ObjectModelOfMapAndReferenceTest {
 
    /*
     * The functionalities of Map-Reference association 
     * is subset of
     * the functionalities of Map-KeyedReference association
     * that has been demonstrated by the another test class 
     * "org.babyfishdemo.om4java.m2kr.ObjectModelOfMapAndKeyedReferenceTest".
     * 
     * So it's unnecessary to demonstrate all the functionalities again and
     * this test class ONLY demonstrates the difference between them.
     * 
     * This test method throws UnsupportedOperationException,
     * for Map-Reference association, Reference side can only to disconnect the association by setting null,
     * only the changing of Map side can be used to connect two non-null objects.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testDiffWithMapAndKeyedReference() {
        
        Airplane airplane = new Airplane();
        Engine engine = new Engine();
        
        /*
         * Be DIFFERENT with Map-KeyedReference, for Map-Reference association,  
         * You can NOT create the association by changing the Reference of child object to be non-null value,
         * because Reference(NOT KeyedReference) can not specify what the key in the Map of parent object is.
         * So babyfish throws exception when you want to assign the value the Reference to be non-null.
         * 
         * But you can set the reference to null to destroy the association :)
         */
        engine.setAirplane(airplane);
    }
}
