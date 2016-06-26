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
package org.babyfish.model.jpa.path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.babyfish.model.jpa.path.QueryPath;
import org.babyfish.model.jpa.path.QueryPathCompilationException;
import org.babyfish.model.jpa.path.QueryPaths;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class PathCompilerTest {

    private static String sourceCode;
    
    @BeforeClass
    public static void initSoruceCode() throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        PathCompilerTest.class.getResourceAsStream("QueryPaths.txt")))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                builder.append(line).append("\r\n");
            }
        }
        sourceCode = builder.toString();
    }
    
    @Test
    public void testCompileEmpty() {
        QueryPath[] queryPaths = QueryPaths.compile("/* empty *///empty");
        Assert.assertEquals(0, queryPaths.length);
    }
    
    @Test(expected = QueryPathCompilationException.class)
    public void testCompileError() {
        QueryPaths.compile("this.employees supervisor");
    }
    
    @Test
    public void testCompile() {
        QueryPath[] queryPaths = QueryPaths.compile(sourceCode);
        Assert.assertEquals(16, queryPaths.length);
        Assert.assertEquals("this.employees.supervisor.supervisor.annualLeaves", queryPaths[0].toString());
        Assert.assertEquals("this..employees..supervisor..supervisor..partial(annualLeaves)", queryPaths[1].toString());
        Assert.assertEquals("this.employees.supervisor.supervisor.annualLeaves", queryPaths[2].toString());
        Assert.assertEquals("this..employees..supervisor.supervisor.partial(annualLeaves)", queryPaths[3].toString());
        Assert.assertEquals("this.employees.supervisor.supervisor.annualLeaves", queryPaths[4].toString());
        Assert.assertEquals("this..employees..supervisor.supervisor.partial(annualLeaves)", queryPaths[5].toString());
        Assert.assertEquals("this..employees..supervisor.supervisor.partial(annualLeaves).reason", queryPaths[6].toString());
        Assert.assertEquals("pre order by this..employees..supervisor.name asc", queryPaths[7].toString());
        Assert.assertEquals("pre order by this.company.inverstors.name asc", queryPaths[8].toString());
        Assert.assertEquals("pre order by this..company.location.name desc", queryPaths[9].toString());
        Assert.assertEquals("pre order by this..employees..supervisor.name asc", queryPaths[10].toString());
        Assert.assertEquals("pre order by this.company.inverstors.name asc", queryPaths[11].toString());
        Assert.assertEquals("pre order by this..company.location.name desc", queryPaths[12].toString());
        Assert.assertEquals("post order by this..employees..supervisor.name asc", queryPaths[13].toString());
        Assert.assertEquals("post order by this.company.inverstors.name asc", queryPaths[14].toString());
        Assert.assertEquals("post order by this..company.location.name desc", queryPaths[15].toString());
    }
}
