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
package org.babyfishdemo.querypath;

import org.babyfishdemo.querypath.entities.Department__;
import org.junit.Test;

import junit.framework.Assert;

public class UntypedQueryPathTest {

    @Test
    public void testCompile() {
        
        // Untyped query path grammar
        // 
        // (1) Multiple-line comment:                                           /*  */
        // (2) Single-line comment:                                             //
        // (3) Query path separator:                                            ';'
        // (4) org.babyfish.model.jpa.path.GetterType.OPTINAL(Left join):       '.'
        //
        // FetchPath only 
        //
        // (5) org.babyfish.model.jpa.path.GetterType.REQUIRED(Inner join):     '..'
        // (6) org.babyfish.model.jpa.path.CollectionFetchType.ALL:             all(<collection property>)
        // (7) org.babyfish.model.jpa.path.CollectionFetchType.PARTIAL:         partial(<collection property>)
        
        String sourceCode =
                "/*"
                + " * Part-I: 5 QueryPaths"
                + " */"
                + "this.description; // Use ';' to separate several QueryPaths\n"
                + "this.employees.image;"
                + "this.employees.annualLeaves;"
                + "this.employees.all(sickLeaves);// Collection property can be wraped by 'all()' or 'patial()' \n"
                + "this.company;"
                + "/*"
                + " * Part-II: 4 SimpleOrderPaths"
                + " */"
                + "pre order by this.name asc;"
                + "pre order by this.employees.name asc;"
                + "pre order by this.employees.annualLeaves.startTime desc;"
                + "pre order by this.employees.sickLeaves.startTime desc; // This last ';' is optional\n";
        Department__[] typedQueryPaths = Department__.compile(sourceCode);
        
        Assert.assertEquals(9, typedQueryPaths.length);
        
        Assert.assertTrue(typedQueryPaths[0] instanceof Department__.FetchPathImpl);
        Assert.assertTrue(typedQueryPaths[1] instanceof Department__.FetchPathImpl);
        Assert.assertTrue(typedQueryPaths[2] instanceof Department__.FetchPathImpl);
        Assert.assertTrue(typedQueryPaths[3] instanceof Department__.FetchPathImpl);
        Assert.assertTrue(typedQueryPaths[4] instanceof Department__.FetchPathImpl);
        
        Assert.assertTrue(typedQueryPaths[5] instanceof Department__.SimpleOrderPathImpl);
        Assert.assertTrue(typedQueryPaths[6] instanceof Department__.SimpleOrderPathImpl);
        Assert.assertTrue(typedQueryPaths[7] instanceof Department__.SimpleOrderPathImpl);
        Assert.assertTrue(typedQueryPaths[8] instanceof Department__.SimpleOrderPathImpl);
    }
}
