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
package org.babyfishdemo.pagingquery;

import java.util.List;

import org.babyfish.persistence.QueryType;
import org.babyfishdemo.pagingquery.base.AbstractDAOTest;
import org.babyfishdemo.pagingquery.entities.Department;
import org.babyfishdemo.pagingquery.entities.Department__;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

/**
 * This test case shows what the difference between 
 * QueryType.DISTINCT(Classic behavior like HQL query) 
 * and 
 * QueryType.RESULT(Classic behavior like QBC query)
 * 
 * This test case does NOT show the DistinctLimitQuery, 
 * please see DistinctLimitQueryTest to know more.
 * 
 * @author Tao Chen
 */
public class AllRowsTest extends AbstractDAOTest {
    
    private static final String SQL =
            "select "
            +     "<...many columns of department0_...>, "
            +     "<...many columns of employees1_...> "
            + "from DEPARTMENT department0_ "
            + "left outer join EMPLOYEE employees1_ "
            +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
            + "order by "
            +     "department0_.DEPARTMENT_ID asc, "
            +     "employees1_.EMPLOYEE_ID asc";
    
    @BeforeClass
    public static void initEntityManagerFactory() {
        initEntityManagerFactory(false);
    }

    @Test
    public void testResultMode() {
        // +---+      /------->+Barracks(1)
        // | 0 +------+        |
        // +---+      |        +-----Jim Raynor(1, Marine)
        // | 1 +------/        |
        // +---+               +-----Tychus Findlay(4, Marine)
        // | 2 +------\        |    
        // +---+      +------->+Ghost Academy(2)
        // | 3 +------/        |
        // +---+               +-----Nova Terra(2, Ghost)
        // | 4 +------\        |
        // +---+      |        +-----Gabriel Tosh(3, Ghost)
        // |   |      |        |
        // |   |      \------->+Star Port(3)
        // |   |               |
        // | 5 +------\        +-----Matt Horner(5, Battlecruiser)
        // |   |      |        |
        // |   |      +------->+Templar Archives(4)
        // |   |      |        |
        // +---+      |        +-----Tassadar(6, High Templar)
        // | 6 +------/        |
        // +---+               +-----Karass(12, High Templar)
        // | 7 +------\        |
        // +---+      \------->+Dark Shrine(5)
        // |   |               |
        // | 8 +------\        +-----Zeratul(7, Dark Templar)
        // |   |      |        |    
        // +---+      +------->+Star Gate(6)
        // | 9 +------/        |
        // +---+               +-----Mohandar(9, Void Ray)
        // |10 +------\        |
        // +---+      |        +-----Urun(10, Phoenix)
        // |11 +------+        |
        // +---+      \------->+Fleet Beacon(7)
        //                     |
        //                     +-----Artanis(8, Mothership)
        //                     |
        //                     \-----Selendis(11, Carrier)
        List<Department> departments = getAllDepartments(
                QueryType.RESULT, 
                Department__.begin().employees().end(),
                Department__.preOrderBy().id().asc(),
                Department__.preOrderBy().employees().id().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(SQL, this.preparedSqlList.get(0));
        
        
        Assert.assertEquals(12, departments.size());
        
        Assert.assertSame(departments.get(0), departments.get(1));
        assertDepartment(departments.get(0), "Barracks", "Jim Raynor", "Tychus Findlay");
        
        Assert.assertSame(departments.get(2), departments.get(3));
        assertDepartment(departments.get(2), "Ghost Academy", "Nova Terra", "Gabriel Tosh");
        
        assertDepartment(departments.get(4), "Star Port", "Matt Horner");
        
        Assert.assertSame(departments.get(5), departments.get(6));
        assertDepartment(departments.get(5), "Templar Archives", "Tassadar", "Karass");
        
        assertDepartment(departments.get(7), "Dark Shrine", "Zeratul");
        
        Assert.assertSame(departments.get(8), departments.get(9));
        assertDepartment(departments.get(8), "Star Gate", "Mohandar", "Urun");
        
        Assert.assertSame(departments.get(10), departments.get(11));
        assertDepartment(departments.get(10), "Fleet Beacon", "Artanis", "Selendis");
    }
    
    @Test
    public void testDistinctMode() {
        // +---+      /------->+Barracks(1)
        // |   |      |        |
        // | 0 +------/        +-----Jim Raynor(1, Marine)
        // |   |               |
        // |   |               +-----Tychus Findlay(4, Marine)
        // +---+               |    
        // |   |      /------->+Ghost Academy(2)
        // | 1 +------/        |
        // |   |               +-----Nova Terra(2, Ghost)
        // |   |               |
        // +---+               +-----Gabriel Tosh(3, Ghost)
        // |   |               |
        // | 2 +-------------->+Star Port(3)
        // |   |               |
        // |   |               +-----Matt Horner(5, Battlecruiser)
        // +---+               |
        // |   |      /------->+Templar Archives(4)
        // | 3 +------/        |
        // |   |               +-----Tassadar(6, High Templar)
        // |   |               |
        // +---+               +-----Karass(12, High Templar)
        // |   |               |
        // | 4 +-------------->+Dark Shrine(5)
        // |   |               |
        // |   |               +-----Zeratul(7, Dark Templar)
        // +---+               |    
        // |   |      /------->+Star Gate(6)
        // | 5 +------/        |
        // |   |               +-----Mohandar(9, Void Ray)
        // |   |               |
        // +---+               +-----Urun(10, Phoenix)
        // |   |               |
        // | 6 +-------------->+Fleet Beacon(7)
        // |   |               |
        // |   |               +-----Artanis(8, Mothership)
        // +---+               |
        //                     \-----Selendis(11, Carrier)
        List<Department> departments = getAllDepartments(
                QueryType.DISTINCT, 
                Department__.begin().employees().end(),
                Department__.preOrderBy().id().asc(),
                Department__.preOrderBy().employees().id().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(SQL, this.preparedSqlList.get(0));
        
        
        Assert.assertEquals(7, departments.size());
        
        assertDepartment(departments.get(0), "Barracks", "Jim Raynor", "Tychus Findlay");
        assertDepartment(departments.get(1), "Ghost Academy", "Nova Terra", "Gabriel Tosh");
        assertDepartment(departments.get(2), "Star Port", "Matt Horner");
        assertDepartment(departments.get(3), "Templar Archives", "Tassadar", "Karass");
        assertDepartment(departments.get(4), "Dark Shrine", "Zeratul");
        assertDepartment(departments.get(5), "Star Gate", "Mohandar", "Urun");
        assertDepartment(departments.get(6), "Fleet Beacon", "Artanis", "Selendis");
    }
}
