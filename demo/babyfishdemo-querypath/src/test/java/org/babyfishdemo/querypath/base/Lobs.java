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
package org.babyfishdemo.querypath.base;

import java.util.List;

import org.babyfish.collection.MACollections;

/**
 * In the class, the binary constants are declared as List<Byte> 
 * that is read-only and created by MACollections.wrap(byte[]).
 * 
 * For example, please look at this bad code:
 * 
 *      public static final byte[] CONSTANT = new byte[] { ... };
 * 
 * This code try to declared a binary constants. Yes, the variable itself is immutable,
 * but the binary data is mutable, the elements of this array are still mutable.
 * 
 * Don't worry, MACollections.wrapXXX({{primiveType}}[]) will NOT create box objects for 
 * all the elements, the private data of this list is still a primitive array, 
 * and the other operations such as "List.get(int)" that will create temporary box objects 
 * in young generation(or young region) in GC heap will not used in the test cases of this project.
 * 
 * MACollections.toXXXArray(List<{{primitiveBoxType}}>) can get the cloned primitiveArray 
 * from the read-only list.
 *
 * @author Tao Chen
 */
public class Lobs {

    /*
     * Lobs for department: Templar Archives
     */
    public static final String TEMPLAR_ARCHIVES_DESCRIPTION = "The description of Templar Archives";
    
    public static final List<Byte> TEMPLAR_ARCHIVES_IMAGE = MACollections.wrapByte(new byte[] { 1, 2, 3, 4, 5 });
    
    /*
     * Lobs for employee: Tassdar
     */
    public static final String TASSADAR_RESUME = "The resume of Tassadar";
    
    public static final List<Byte> TASSADAR_PHOTO = MACollections.wrapByte(new byte[] { 2, 3, 4, 5, 6 });
    
    /*
     * Lobs for employee: Karass
     */
    public static final String KARASS_RESUME = "The resume of Karass";
    
    public static final List<Byte> KARASS_PHOTO = MACollections.wrapByte(new byte[] { 3, 4, 5, 6, 7 });
    
    /*
     * Lobs for employee: Zeratul
     */
    public static final String ZERATUL_RESUME = "The resume of zeratul";
    
    public static final List<Byte> ZERATUL_PHOTO = MACollections.wrapByte(new byte[] { 4, 5, 6, 7, 8 });
    
    /*
     * Lobs for employee: Mohandar
     */
    public static final String MOHANDAR_RESUME = "The resume of Mohandar";
    
    public static final List<Byte> MOHANDAR_PHOTO = MACollections.wrapByte(new byte[] { 5, 6, 7, 8, 9 });
    
    /*
     * Lobs for employee: Urun
     */
    public static final String URUN_RESUME = "The resume of Urun";
    
    public static final List<Byte> URUN_PHOTO = MACollections.wrapByte(new byte[] { 6, 7, 8, 9, 1 });
    
    /*
     * Lobs for employee: Selendis
     */
    public static final String SELENDIS_RESUME= "The resume of Selendis";
    
    public static final List<Byte> SELENDIS_PHOTO = MACollections.wrapByte(new byte[] { 7, 8, 9, 1, 2 });
    
    /*
     * Lobs for employee: Artanis
     */
    public static final String ARTANIS_RESUME = "The resume of Artanis";
    
    public static final List<Byte> ARTANIS_PHOTO = MACollections.wrapByte(new byte[] { 8, 9, 1, 2, 3 });
    
    
    private Lobs() {}
}
