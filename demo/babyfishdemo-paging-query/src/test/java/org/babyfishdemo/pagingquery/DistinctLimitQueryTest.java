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

import org.babyfish.junit.FilterDeclaration;
import org.babyfish.junit.FilterRunner;
import org.babyfish.persistence.QueryType;
import org.babyfishdemo.pagingquery.entities.Department__;
import org.babyfishdemo.pagingquery.base.AbstractDAOTest;
import org.babyfishdemo.pagingquery.base.LimitedResult;
import org.babyfishdemo.pagingquery.base.OracleFilter;
import org.babyfishdemo.pagingquery.entities.Department;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This is the example for DistinctQueryLimit,
 * 
 * It can do paging query for the query that is QueryType.DISTINCT
 * and has collection fetches in database level(Not memory level).
 * Please see the fixed result of
 * <a href="https://hibernate.atlassian.net/browse/HHH-1412">HHH-1412</a>
 * to know more.
 *
 *
 * If the VM argument "-Ddatabase = oracle" is NOT specified
 * this case will be ignored.
 *
 * @author Tao Chen
 */
@RunWith(FilterRunner.class)
@FilterDeclaration(filterClass = OracleFilter.class)
public class DistinctLimitQueryTest extends AbstractDAOTest {

    @BeforeClass
    public static void initEntityManagerFactory() {
        initEntityManagerFactory(true);
    }
    
    /**
     * In this test method, all the SimpleOrderPath are applied on the root queried entity,
     * babyfish-hibernate decides to use the oralce standard analytic function "dense_rank()"
     * to do the paging query.  
     */
    @Test
    public void testLimitQueryByDenseRank() {
        //        +---+      /------->+Barracks(1)
        //        |   |      |        |
        //        | 0 +------/        +-----Jim Raynor(1, Marine)
        //        |   |               |
        //        |   |               +-----Tychus Findlay(4, Marine)
        //        +---+               |    
        //        |   |      /------->+Ghost Academy(2)
        //        | 1 +------/        |
        //        |   |               +-----Nova Terra(2, Ghost)
        //        |   |               |
        //        +---+               +-----Gabriel Tosh(3, Ghost)
        //        |   |               |
        // First->| 2 +-------------->+Star Port(3)
        //        |   |               |
        //        |   |               +-----Matt Horner(5, Battlecruiser)
        //        +---+               |
        //        |   |      /------->+Templar Archives(4)
        //  Last->| 3 +------/        |
        //        |   |               +-----Tassadar(6, High Templar)
        //        |   |               |
        //        +---+               +-----Karass(12, High Templar)
        //        |   |               |
        //        | 4 +-------------->+Dark Shrine(5)
        //        |   |               |
        //        |   |               +-----Zeratul(7, Dark Templar)
        //        +---+               |    
        //        |   |      /------->+Star Gate(6)
        //        | 5 +------/        |
        //        |   |               +-----Mohandar(9, Void Ray)
        //        |   |               |
        //        +---+               +-----Urun(10, Phoenix)
        //        |   |               |
        //        | 6 +-------------->+Fleet Beacon(7)
        //        |   |               |
        //        |   |               +-----Artanis(8, Mothership)
        //        +---+               |
        //                            \-----Selendis(11, Carrier)
        LimitedResult<Department> limitedResult = getLimitedDepartments(
                QueryType.DISTINCT, 
                2,
                2,
                Department__.begin().employees().end(),
                Department__.preOrderBy().id().asc()
        );
        Assert.assertEquals(7, limitedResult.getUnlimitedRowCount());
        Assert.assertEquals(2, limitedResult.getLimitedRows().size());
        
        assertDepartment(
                limitedResult.getLimitedRows().get(0), 
                "Star Port", 
                "Matt Horner"
        );
        assertDepartment(
                limitedResult.getLimitedRows().get(1), 
                "Templar Archives", 
                "Tassadar", 
                "Karass"
        );
        
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select count(department0_.DEPARTMENT_ID) "
                + "from DEPARTMENT department0_", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "* "
                + "from ("
                +     "select "
                +         "<...many columns of department0_...>, "
                +         "<...many columns of employees1_...>, "
                +         "dense_rank() over("
                +             "order by "
                +                 "department0_.DEPARTMENT_ID asc, "
                +                 "department0_.rowid asc"
                +         ") dense_rank____ "
                + "from DEPARTMENT department0_ "
                + "left outer join EMPLOYEE employees1_ "
                +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + ") "
                + "where "
                +     "dense_rank____ <= ? "
                + "and "
                +     "dense_rank____ > ?",
                this.preparedSqlList.get(1)
        );
    }
    
    /**
     * In this test method, not all the SimpleOrderPath are applied on the root queried entity,
     * babyfish-hibernate decides to use the user defined analytic function "distinct_rank(ROWID)"
     * to do the paging query.  
     */
    @Test
    public void testLimitQueryByDistinctRank() {
        //        +---+      /------->+Barracks(1)
        //        |   |      |        |
        //        | 0 +------/        +-----Jim Raynor(1, Marine)
        //        |   |               |
        //        |   |               +-----Tychus Findlay(4, Marine)
        //        +---+               |    
        //        |   |      /------->+Ghost Academy(2)
        //        | 1 +------/        |
        //        |   |               +-----Gabriel Tosh(3, Ghost)
        //        |   |               |
        //        +---+               +-----Nova Terra(2, Ghost)
        //        |   |               |
        // First->| 2 +-------------->+Star Port(3)
        //        |   |               |
        //        |   |               +-----Matt Horner(5, Battlecruiser)
        //        +---+               |
        //        |   |      /------->+Templar Archives(4)
        //  Last->| 3 +------/        |
        //        |   |               +-----Karass(12, High Templar)
        //        |   |               |
        //        +---+               +-----Tassadar(6, High Templar)
        //        |   |               |
        //        | 4 +-------------->+Dark Shrine(5)
        //        |   |               |
        //        |   |               +-----Zeratul(7, Dark Templar)
        //        +---+               |    
        //        |   |      /------->+Star Gate(6)
        //        | 5 +------/        |
        //        |   |               +-----Mohandar(9, Void Ray)
        //        |   |               |
        //        +---+               +-----Urun(10, Phoenix)
        //        |   |               |
        //        | 6 +-------------->+Fleet Beacon(7)
        //        |   |               |
        //        |   |               +-----Artanis(8, Mothership)
        //        +---+               |
        //                            \-----Selendis(11, Carrier)
        LimitedResult<Department> limitedResult = getLimitedDepartments(
                QueryType.DISTINCT, 
                2,
                2,
                Department__.begin().employees().end(),
                Department__.preOrderBy().id().asc(),
                Department__.preOrderBy().employees().name().asc()
        );
        Assert.assertEquals(7, limitedResult.getUnlimitedRowCount());
        Assert.assertEquals(2, limitedResult.getLimitedRows().size());
        
        assertDepartment(limitedResult.getLimitedRows().get(0), "Star Port", "Matt Horner");
        assertDepartment(limitedResult.getLimitedRows().get(1), "Templar Archives", "Karass", "Tassadar");
        
        
        Assert.assertEquals(
                "select count(department0_.DEPARTMENT_ID) "
                + "from DEPARTMENT department0_", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "* "
                + "from ("
                +     "select "
                +         "<...many columns of department0_...>, "
                +         "<...many columns of employees1_...>, "
                +         "distinct_rank(department0_.rowid) over("
                +             "order by "
                +                 "department0_.DEPARTMENT_ID asc, "
                +                 "employees1_.NAME asc"
                +         ") distinct_rank____ "
                + "from DEPARTMENT department0_ "
                + "left outer join EMPLOYEE employees1_ "
                +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + ") "
                + "where "
                +     "distinct_rank____ <= ? "
                + "and "
                +     "distinct_rank____ > ?",
                this.preparedSqlList.get(1)
        );
    }
}
