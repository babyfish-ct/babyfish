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

import junit.framework.Assert;

import org.babyfish.persistence.QueryType;
import org.babyfishdemo.pagingquery.entities.Department__;
import org.babyfishdemo.pagingquery.base.AbstractDAOTest;
import org.babyfishdemo.pagingquery.base.LimitedResult;
import org.babyfishdemo.pagingquery.entities.Department;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test case shows the paging query of QueryType.RESULT
 * (The paging query behavior like classic QBC query)
 * 
 * This test case does NOT show the DistinctLimitQuery, 
 * please see DistinctLimitQueryTest to know more.
 *
 * @author Tao Chen
 */
public class ResultLimitQueryTest extends AbstractDAOTest {

    @BeforeClass
    public static void initEntityManagerFactory() {
        initEntityManagerFactory(false);
    }
    
    @Test
    public void test() {
        //        +---+      /------->+Barracks(1)
        //        | 0 +------+        |
        //        +---+      |        +-----Jim Raynor(1, Marine)
        //        | 1 +------/        |
        //        +---+               +-----Tychus Findlay(4, Marine)
        //        | 2 +------\        |    
        //        +---+      +------->+Ghost Academy(2)
        //        | 3 +------/        |
        //        +---+               +-----Nova Terra(2, Ghost)
        // First->| 4 +------\        |                               
        //        +---+      |        +-----Gabriel Tosh(3, Ghost)
        //        |   |      |        |
        //        |   |      \------->+Star Port(3)
        //        |   |               |
        //        | 5 +------\        +-----Matt Horner(5, Battlecruiser)
        //        |   |      |        |
        //        |   |      +------->+Templar Archives(4)
        //        |   |      |        |
        //        +---+      |        +-----Tassadar(6, High Templar)
        //        | 6 +------/        |
        //        +---+               +-----Karass(12, High Templar)
        //  Last->| 7 +------\        |
        //        +---+      \------->+Dark Shrine(5)
        //        |   |               |
        //        | 8 +------\        +-----Zeratul(7, Dark Templar)
        //        |   |      |        |    
        //        +---+      +------->+Star Gate(6)
        //        | 9 +------/        |
        //        +---+               +-----Mohandar(9, Void Ray)
        //        |10 +------\        |
        //        +---+      |        +-----Urun(10, Phoenix)
        //        |11 +------+        |
        //        +---+      \------->+Fleet Beacon(7)
        //                            |
        //                            +-----Artanis(8, Mothership)
        //                            |
        //                            \-----Selendis(11, Carrier)
        LimitedResult<Department> limitedResult = getLimitedDepartments(
                QueryType.RESULT, 
                4,
                4,
                Department__.begin().employees().end(),
                Department__.preOrderBy().id().asc(),
                Department__.preOrderBy().employees().id().asc()
        );
        
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select count(department0_.DEPARTMENT_ID) "
                + "from DEPARTMENT department0_ "
                + "left outer join EMPLOYEE employees1_ "
                +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...>, "
                +     "<...many columns of employees1_...> "
                + "from DEPARTMENT department0_ "
                + "left outer join EMPLOYEE employees1_ "
                +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + "order by "
                +     "department0_.DEPARTMENT_ID asc, "
                +     "employees1_.EMPLOYEE_ID asc "
                + "offset ? "
                + "limit ?", 
                this.preparedSqlList.get(1)
        );
        
        
        Assert.assertEquals(12, limitedResult.getUnlimitedRowCount());
        Assert.assertEquals(4, limitedResult.getLimitedRows().size());
        
        assertDepartment(limitedResult.getLimitedRows().get(0), "Star Port", "Matt Horner");
        
        Assert.assertSame(limitedResult.getLimitedRows().get(1), limitedResult.getLimitedRows().get(2));
        assertDepartment(limitedResult.getLimitedRows().get(1), "Templar Archives", "Tassadar", "Karass");
        
        assertDepartment(limitedResult.getLimitedRows().get(3), "Dark Shrine", "Zeratul");
    }
}
