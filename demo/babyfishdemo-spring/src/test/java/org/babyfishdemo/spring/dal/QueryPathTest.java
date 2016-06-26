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
package org.babyfishdemo.spring.dal;

import java.util.List;

import junit.framework.Assert;

import org.babyfishdemo.spring.dal.base.AbstractRepositoryTest;
import org.babyfishdemo.spring.entities.Employee;
import org.babyfishdemo.spring.entities.Employee__;
import org.babyfishdemo.spring.model.EmployeeSpecification;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class QueryPathTest extends AbstractRepositoryTest {

    @Test
    public void testQueryEmployeeOnly() {
        
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setLikeFirstName("Zeratul");
        List<Employee> employees = this.employeeRepository.getEmployees(specification);
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "where upper(employee0_.FIRST_NAME) like ?", 
                this.preparedSqlList.get(0)
        );
        
        /*
         * No FetchPaths so that all the lazy scalars and lazy associations are unloaded.
         */
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Zeratul, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0976-11-13, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection, "
                +     "supervisor: @UnloadedReference, "
                +     "subordinates: @UnloadedCollection "
                +   "} "
                + "]",
                getEmployeesString(employees, true, true) 
        );
    }
    
    @Test
    public void testQueryEmployeeWithScalars() {
        /*
         * Fetch Plan
         * 
         *      +-<< Root Entity: Employee >>
         *      |
         *      +-----@description
         *      |
         *      \-----@image
         */
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setLikeFirstName("Zeratul");
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification,
                Employee__.begin().description().end(),
                Employee__.begin().image().end()
        );
        
        /*
         * Two SQLs
         * 
         * SQL[0]: Load entity
         * SQL[1]: Load LOBs
         */
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "where upper(employee0_.FIRST_NAME) like ?", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "this_.IMAGE, "
                +     "this_.DESCRIPTION "
                + "from EMPLOYEE this_ "
                + "where this_.EMPLOYEE_ID=?", 
                this.preparedSqlList.get(1)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Zeratul, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0976-11-13, "
                +     "description: clob(The description for Zeratul), "
                +     "image: blob(0x060708090A0B0C0D0E0F000102030405), "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection, "
                +     "supervisor: @UnloadedReference, "
                +     "subordinates: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees, true, true)
        );
    }
    
    @Test
    public void testQueryEmployeeWithAssociations() {
        /*
         * Fetch plan tree:
         * 
         *      +-<< Root Entity: Employee >>
         *      |
         *      +-----department
         *      |
         *      +-----annualLeaves
         *      |
         *      +---+-supervisor
         *      |   |
         *      |   +-----supervisor
         *      |   |
         *      |   +-----department
         *      |   |
         *      |   \-----annualLeaves
         *      |
         *      \---+-subordinates
         *          |
         *          +---+-subordinates
         *          |   |
         *          |   +-----subordinates
         *          |   |
         *          |   +-----department
         *          |   |
         *          |   \-----annualLeaves
         *          |
         *          +-----department
         *          |
         *          \-----annualLeaves
         */
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setLikeFirstName("Zeratul");
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification,
                
                // Fetch Paths
                Employee__.begin().department().end(),
                Employee__.begin().annualLeaves().end(),
                
                Employee__.begin().supervisor().supervisor().end(),
                
                Employee__.begin().supervisor().department().end(),
                Employee__.begin().supervisor().annualLeaves().end(),
                
                Employee__.begin().subordinates().subordinates().subordinates().end(),
                
                Employee__.begin().subordinates().department().end(),
                Employee__.begin().subordinates().annualLeaves().end(),
                
                Employee__.begin().subordinates().subordinates().department().end(),
                Employee__.begin().subordinates().subordinates().annualLeaves().end(),
                
                // SimpleOrderPaths
                Employee__.preOrderBy().annualLeaves().startTime().asc(),
                Employee__.preOrderBy().supervisor().annualLeaves().startTime().asc(),
                Employee__.preOrderBy().subordinates().name().firstName().asc(),
                Employee__.preOrderBy().subordinates().annualLeaves().startTime().asc(),
                Employee__.preOrderBy().subordinates().subordinates().name().firstName().asc(),
                Employee__.preOrderBy().subordinates().subordinates().annualLeaves().startTime().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                + "<...many columns of employee0_...>, "
                + "<...many columns of department1_...>, "
                + "<...many columns of annualleav2_...>, "
                + "<...many columns of employee3_...>, "
                + "<...many columns of employee4_...>, "
                + "<...many columns of department5_...>, "
                + "<...many columns of annualleav6_...>, "
                + "<...many columns of subordinat7_...>, "
                + "<...many columns of subordinat8_...>, "
                + "<...many columns of subordinat9_...>, "
                + "<...many columns of department10_...>, "
                + "<...many columns of annualleav11_...>, "
                + "<...many columns of department12_...>, "
                + "<...many columns of annualleav13_...> "
                + "from EMPLOYEE employee0_ "
                + "left outer join DEPARTMENT department1_ "
                +     "on employee0_.DEPARTMENT_ID=department1_.DEPARTMENT_ID "
                + "left outer join ANNUAL_LEAVE annualleav2_ "
                +     "on employee0_.EMPLOYEE_ID=annualleav2_.EMPLOYEE_ID "
                + "left outer join EMPLOYEE employee3_ on "
                +     "employee0_.SUPERVISOR_ID=employee3_.EMPLOYEE_ID "
                + "left outer join EMPLOYEE employee4_ "
                +     "on employee3_.SUPERVISOR_ID=employee4_.EMPLOYEE_ID "
                + "left outer join DEPARTMENT department5_ "
                +     "on employee3_.DEPARTMENT_ID=department5_.DEPARTMENT_ID "
                + "left outer join ANNUAL_LEAVE annualleav6_ "
                +     "on employee3_.EMPLOYEE_ID=annualleav6_.EMPLOYEE_ID "
                + "left outer join EMPLOYEE subordinat7_ "
                +     "on employee0_.EMPLOYEE_ID=subordinat7_.SUPERVISOR_ID "
                + "left outer join EMPLOYEE subordinat8_ "
                +     "on subordinat7_.EMPLOYEE_ID=subordinat8_.SUPERVISOR_ID "
                + "left outer join EMPLOYEE subordinat9_ "
                +     "on subordinat8_.EMPLOYEE_ID=subordinat9_.SUPERVISOR_ID "
                + "left outer join DEPARTMENT department10_ "
                +     "on subordinat8_.DEPARTMENT_ID=department10_.DEPARTMENT_ID "
                + "left outer join ANNUAL_LEAVE annualleav11_ "
                +     "on subordinat8_.EMPLOYEE_ID=annualleav11_.EMPLOYEE_ID "
                + "left outer join DEPARTMENT department12_ "
                +     "on subordinat7_.DEPARTMENT_ID=department12_.DEPARTMENT_ID "
                + "left outer join ANNUAL_LEAVE annualleav13_ "
                +     "on subordinat7_.EMPLOYEE_ID=annualleav13_.EMPLOYEE_ID "
                + "where "
                +     "upper(employee0_.FIRST_NAME) like ? "
                + "order by "
                +     "annualleav2_.START_TIME asc, "
                +     "annualleav6_.START_TIME asc, "
                +     "subordinat7_.FIRST_NAME asc, "
                +     "annualleav13_.START_TIME asc, "
                +     "subordinat8_.FIRST_NAME asc, "
                +     "annualleav11_.START_TIME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Zeratul, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0976-11-13, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: { "
                +       "name: Dark Shrine, "
                +       "image: @UnloadedBlob "
                +     "}, "
                +     "annualLeaves: [ "
                +       "{ "
                +         "startTime: 2015-01-19 09:00, "
                +         "endTime: 2015-01-19 18:00, "
                +         "state: APPROVED "
                +       "}, "
                +       "{ "
                +         "startTime: 2015-03-13 09:00, "
                +         "endTime: 2015-03-13 18:00, "
                +         "state: APPROVED "
                +       "} "
                +     "], "
                +     "supervisor: { "
                +       "name: { "
                +         "firstName: Tassadar, "
                +         "lastName: null "
                +       "}, "
                +       "gender: MALE, "
                +       "birthday: 0756-08-24, "
                +       "description: @UnloadedClob, "
                +       "image: @UnloadedBlob, "
                +       "department: { "
                +         "name: Templar Archive, "
                +         "image: @UnloadedBlob "
                +       "}, "
                +       "annualLeaves: [ "
                +         "{ "
                +           "startTime: 2015-04-21 09:00, "
                +           "endTime: 2015-04-24 18:00, "
                +           "state: APPROVED "
                +         "}, "
                +         "{ "
                +           "startTime: 2015-09-05 19:00, "
                +           "endTime: 2015-09-06 18:00, "
                +           "state: PENDING "
                +         "} "
                +       "], "
                +       "supervisor: null "
                +     "}, "
                +     "subordinates: [ "
                +       "{ "
                +         "name: { "
                +           "firstName: Karass, "
                +           "lastName: null "
                +         "}, "
                +         "gender: MALE, "
                +         "birthday: 1245-03-27, "
                +         "description: @UnloadedClob, "
                +         "image: @UnloadedBlob, "
                +         "department: { "
                +           "name: Templar Archive, "
                +           "image: @UnloadedBlob "
                +         "}, "
                +         "annualLeaves: [], "
                +         "subordinates: [] "
                +       "}, "
                +       "{ "
                +         "name: { "
                +           "firstName: Selendis, "
                +           "lastName: null "
                +         "}, "
                +         "gender: FEMALE, "
                +         "birthday: 1003-12-11, "
                +         "description: @UnloadedClob, "
                +         "image: @UnloadedBlob, "
                +         "department: { "
                +           "name: Fleet Beacon, "
                +           "image: @UnloadedBlob "
                +         "}, "
                +         "annualLeaves: [ "
                +           "{ "
                +             "startTime: 2015-08-22 09:00, "
                +             "endTime: 2015-08-23 18:00, "
                +             "state: APPROVED "
                +           "} "
                +         "], "
                +         "subordinates: [ "
                +           "{ "
                +             "name: { "
                +               "firstName: Mohandar, "
                +               "lastName: null "
                +             "}, "
                +             "gender: MALE, "
                +             "birthday: 1423-02-17, "
                +             "description: @UnloadedClob, "
                +             "image: @UnloadedBlob, "
                +             "department: { "
                +               "name: Star Gate, "
                +               "image: @UnloadedBlob "
                +             "}, "
                +             "annualLeaves: [ "
                +               "{ "
                +                 "startTime: 2015-01-07 09:00, "
                +                 "endTime: 2015-02-28 18:00, "
                +                 "state: REJECTED "
                +               "}, "
                +               "{ "
                +                 "startTime: 2015-01-07 09:00, "
                +                 "endTime: 2015-02-20 18:00, "
                +                 "state: REJECTED "
                +               "}, "
                +               "{ "
                +                 "startTime: 2015-01-07 09:00, "
                +                 "endTime: 2015-02-07 18:00, "
                +                 "state: APPROVED "
                +               "} "
                +             "], "
                +             "subordinates: [] "
                +           "}, "
                +           "{ "
                +             "name: { "
                +               "firstName: Urun, "
                +               "lastName: null "
                +             "}, "
                +             "gender: MALE, "
                +             "birthday: 1490-08-25, "
                +             "description: @UnloadedClob, "
                +             "image: @UnloadedBlob, "
                +             "department: { "
                +               "name: Star Gate, "
                +               "image: @UnloadedBlob "
                +             "}, "
                +             "annualLeaves: [ "
                +               "{ "
                +                 "startTime: 2015-04-28 09:00, "
                +                 "endTime: 2015-04-29 18:00, "
                +                 "state: APPROVED "
                +               "}, "
                +               "{ "
                +                 "startTime: 2015-08-21 09:00, "
                +                 "endTime: 2015-08-21 18:00, "
                +                 "state: APPROVED "
                +               "} "
                +             "], "
                +             "subordinates: [] "
                +           "} "
                +         "] "
                +       "} "
                +     "] "
                +   "} "
                + "]",
                getEmployeesString(employees, true, true)
        );
    }
    
    @Test
    public void testQueryEmployeeWithBothScalarsAndAssociations() {
        /*
         * Fetch plan tree:
         * 
         *      +-<< Root Entity: Employee >>
         *      |
         *      +-----@description
         *      |
         *      +-----@image
         *      |
         *      +---+-department
         *      |   |
         *      |   \-----@image
         *      |
         *      +-----annualLeaves
         *      |
         *      +---+-supervisor
         *      |   |
         *      |   +-----@descrption
         *      |   |
         *      |   +-----@image
         *      |   |
         *      |   +-----supervisor
         *      |   |
         *      |   +---+-department
         *      |   |   |
         *      |   |   \-----@image
         *      |   |
         *      |   \-----annualLeaves
         *      |
         *      \---+-subordinates
         *          |
         *          +---+-subordinates
         *          |   |
         *          |   +-----@description
         *          |   |
         *          |   +-----@image
         *          |   |
         *          |   +-----subordinates
         *          |   |
         *          |   +---+-department
         *          |   |   |
         *          |   |   \-----@image
         *          |   |
         *          |   \-----annualLeaves
         *          |
         *          +-----@description
         *          |
         *          +-----@image
         *          |
         *          +---+-department
         *          |   |
         *          |   \-----@image
         *          |
         *          \-----annualLeaves
         */
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setLikeFirstName("Zeratul");
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification,
                
                // FetchPaths
                Employee__.begin().description().end(),
                Employee__.begin().image().end(),
                Employee__.begin().department().end(),
                Employee__.begin().department().image().end(),
                Employee__.begin().annualLeaves().end(),
                
                Employee__.begin().supervisor().supervisor().end(),
                
                Employee__.begin().supervisor().description().end(),
                Employee__.begin().supervisor().image().end(),
                Employee__.begin().supervisor().department().end(),
                Employee__.begin().supervisor().department().image().end(),
                Employee__.begin().supervisor().annualLeaves().end(),
                
                Employee__.begin().subordinates().subordinates().subordinates().end(),
                
                Employee__.begin().subordinates().description().end(),
                Employee__.begin().subordinates().image().end(),
                Employee__.begin().subordinates().department().end(),
                Employee__.begin().subordinates().department().image().end(),
                Employee__.begin().subordinates().annualLeaves().end(),
                
                Employee__.begin().subordinates().subordinates().description().end(),
                Employee__.begin().subordinates().subordinates().image().end(),
                Employee__.begin().subordinates().subordinates().department().end(),
                Employee__.begin().subordinates().subordinates().department().image().end(),
                Employee__.begin().subordinates().subordinates().annualLeaves().end(),
                
                // SimpleOrderPaths
                Employee__.preOrderBy().annualLeaves().startTime().asc(),
                Employee__.preOrderBy().supervisor().annualLeaves().startTime().asc(),
                Employee__.preOrderBy().subordinates().name().firstName().asc(),
                Employee__.preOrderBy().subordinates().annualLeaves().startTime().asc(),
                Employee__.preOrderBy().subordinates().subordinates().name().firstName().asc(),
                Employee__.preOrderBy().subordinates().subordinates().annualLeaves().startTime().asc()
        );
        
        
        /*
         * Three SQLs
         * SQL[0]: Load the fetched tree
         * SQL[1]: Load fetched LOBs, for all employees
         * SQL[2]: Load fetched LOBs, for all departments
         */
        Assert.assertEquals(3, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                + "<...many columns of employee0_...>, "
                + "<...many columns of department1_...>, "
                + "<...many columns of annualleav2_...>, "
                + "<...many columns of employee3_...>, "
                + "<...many columns of employee4_...>, "
                + "<...many columns of department5_...>, "
                + "<...many columns of annualleav6_...>, "
                + "<...many columns of subordinat7_...>, "
                + "<...many columns of subordinat8_...>, "
                + "<...many columns of subordinat9_...>, "
                + "<...many columns of department10_...>, "
                + "<...many columns of annualleav11_...>, "
                + "<...many columns of department12_...>, "
                + "<...many columns of annualleav13_...> "
                + "from EMPLOYEE employee0_ "
                + "left outer join DEPARTMENT department1_ "
                +     "on employee0_.DEPARTMENT_ID=department1_.DEPARTMENT_ID "
                + "left outer join ANNUAL_LEAVE annualleav2_ "
                +     "on employee0_.EMPLOYEE_ID=annualleav2_.EMPLOYEE_ID "
                + "left outer join EMPLOYEE employee3_ on "
                +     "employee0_.SUPERVISOR_ID=employee3_.EMPLOYEE_ID "
                + "left outer join EMPLOYEE employee4_ "
                +     "on employee3_.SUPERVISOR_ID=employee4_.EMPLOYEE_ID "
                + "left outer join DEPARTMENT department5_ "
                +     "on employee3_.DEPARTMENT_ID=department5_.DEPARTMENT_ID "
                + "left outer join ANNUAL_LEAVE annualleav6_ "
                +     "on employee3_.EMPLOYEE_ID=annualleav6_.EMPLOYEE_ID "
                + "left outer join EMPLOYEE subordinat7_ "
                +     "on employee0_.EMPLOYEE_ID=subordinat7_.SUPERVISOR_ID "
                + "left outer join EMPLOYEE subordinat8_ "
                +     "on subordinat7_.EMPLOYEE_ID=subordinat8_.SUPERVISOR_ID "
                + "left outer join EMPLOYEE subordinat9_ "
                +     "on subordinat8_.EMPLOYEE_ID=subordinat9_.SUPERVISOR_ID "
                + "left outer join DEPARTMENT department10_ "
                +     "on subordinat8_.DEPARTMENT_ID=department10_.DEPARTMENT_ID "
                + "left outer join ANNUAL_LEAVE annualleav11_ "
                +     "on subordinat8_.EMPLOYEE_ID=annualleav11_.EMPLOYEE_ID "
                + "left outer join DEPARTMENT department12_ "
                +     "on subordinat7_.DEPARTMENT_ID=department12_.DEPARTMENT_ID "
                + "left outer join ANNUAL_LEAVE annualleav13_ "
                +     "on subordinat7_.EMPLOYEE_ID=annualleav13_.EMPLOYEE_ID "
                + "where "
                +     "upper(employee0_.FIRST_NAME) like ? "
                + "order by "
                +     "annualleav2_.START_TIME asc, "
                +     "annualleav6_.START_TIME asc, "
                +     "subordinat7_.FIRST_NAME asc, "
                +     "annualleav13_.START_TIME asc, "
                +     "subordinat8_.FIRST_NAME asc, "
                +     "annualleav11_.START_TIME asc", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "this_.EMPLOYEE_ID, "
                +     "this_.IMAGE, "
                +     "this_.DESCRIPTION "
                + "from EMPLOYEE this_ "
                + "where this_.EMPLOYEE_ID in (?, ?, ?, ?, ?, ?)", 
                this.preparedSqlList.get(1)
        );
        Assert.assertEquals(
                "select "
                +     "this_.DEPARTMENT_ID, "
                +     "this_.IMAGE "
                +     "from DEPARTMENT this_ "
                + "where this_.DEPARTMENT_ID in (?, ?, ?, ?)", 
                this.preparedSqlList.get(2)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Zeratul, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0976-11-13, "
                +     "description: clob(The description for Zeratul), "
                +     "image: blob(0x060708090A0B0C0D0E0F000102030405), "
                +     "department: { "
                +       "name: Dark Shrine, "
                +       "image: blob(0x05050505050505050505) "
                +     "}, "
                +     "annualLeaves: [ "
                +       "{ "
                +         "startTime: 2015-01-19 09:00, "
                +         "endTime: 2015-01-19 18:00, "
                +         "state: APPROVED "
                +       "}, "
                +       "{ "
                +         "startTime: 2015-03-13 09:00, "
                +         "endTime: 2015-03-13 18:00, "
                +         "state: APPROVED "
                +       "} "
                +     "], "
                +     "supervisor: { "
                +       "name: { "
                +         "firstName: Tassadar, "
                +         "lastName: null "
                +       "}, "
                +       "gender: MALE, "
                +       "birthday: 0756-08-24, "
                +       "description: clob(The description for Tassadar), "
                +       "image: blob(0x05060708090A0B0C0D0E0F0001020304), "
                +       "department: { "
                +         "name: Templar Archive, "
                +         "image: blob(0x04040404040404040404) "
                +       "}, "
                +       "annualLeaves: [ "
                +         "{ "
                +           "startTime: 2015-04-21 09:00, "
                +           "endTime: 2015-04-24 18:00, "
                +           "state: APPROVED "
                +         "}, "
                +         "{ "
                +           "startTime: 2015-09-05 19:00, "
                +           "endTime: 2015-09-06 18:00, "
                +           "state: PENDING "
                +         "} "
                +       "], "
                +       "supervisor: null "
                +     "}, "
                +     "subordinates: [ "
                +       "{ "
                +         "name: { "
                +           "firstName: Karass, "
                +           "lastName: null "
                +         "}, "
                +         "gender: MALE, "
                +         "birthday: 1245-03-27, "
                +         "description: clob(The description for Karass), "
                +         "image: blob(0x0B0C0D0E0F000102030405060708090A), "
                +         "department: { "
                +           "name: Templar Archive, "
                +           "image: blob(0x04040404040404040404) "
                +         "}, "
                +         "annualLeaves: [], "
                +         "subordinates: [] "
                +       "}, "
                +       "{ "
                +         "name: { "
                +           "firstName: Selendis, "
                +           "lastName: null "
                +         "}, "
                +         "gender: FEMALE, "
                +         "birthday: 1003-12-11, "
                +         "description: clob(The description for Selendis), "
                +         "image: blob(0x0A0B0C0D0E0F00010203040506070809), "
                +         "department: { "
                +           "name: Fleet Beacon, "
                +           "image: blob(0x07070707070707070707) "
                +         "}, "
                +         "annualLeaves: [ "
                +           "{ "
                +             "startTime: 2015-08-22 09:00, "
                +             "endTime: 2015-08-23 18:00, "
                +             "state: APPROVED "
                +           "} "
                +         "], "
                +         "subordinates: [ "
                +           "{ "
                +             "name: { "
                +               "firstName: Mohandar, "
                +               "lastName: null "
                +             "}, "
                +             "gender: MALE, "
                +             "birthday: 1423-02-17, "
                +             "description: clob(The description for Mohandar), "
                +             "image: blob(0x08090A0B0C0D0E0F0001020304050607), "
                +             "department: { "
                +               "name: Star Gate, "
                +               "image: blob(0x06060606060606060606) "
                +             "}, "
                +             "annualLeaves: [ "
                +               "{ "
                +                 "startTime: 2015-01-07 09:00, "
                +                 "endTime: 2015-02-28 18:00, "
                +                 "state: REJECTED "
                +               "}, "
                +               "{ "
                +                 "startTime: 2015-01-07 09:00, "
                +                 "endTime: 2015-02-20 18:00, "
                +                 "state: REJECTED "
                +               "}, "
                +               "{ "
                +                 "startTime: 2015-01-07 09:00, "
                +                 "endTime: 2015-02-07 18:00, "
                +                 "state: APPROVED "
                +               "} "
                +             "], "
                +             "subordinates: [] "
                +           "}, "
                +           "{ "
                +             "name: { "
                +               "firstName: Urun, "
                +               "lastName: null "
                +             "}, "
                +             "gender: MALE, "
                +             "birthday: 1490-08-25, "
                +             "description: clob(The description for Urun), "
                +             "image: blob(0x090A0B0C0D0E0F000102030405060708), "
                +             "department: { "
                +               "name: Star Gate, "
                +               "image: blob(0x06060606060606060606) "
                +             "}, "
                +             "annualLeaves: [ "
                +               "{ "
                +                 "startTime: 2015-04-28 09:00, "
                +                 "endTime: 2015-04-29 18:00, "
                +                 "state: APPROVED "
                +               "}, "
                +               "{ "
                +                 "startTime: 2015-08-21 09:00, "
                +                 "endTime: 2015-08-21 18:00, "
                +                 "state: APPROVED "
                +               "} "
                +             "], "
                +             "subordinates: [] "
                +           "} "
                +         "] "
                +       "} "
                +     "] "
                +   "} "
                + "]",
                getEmployeesString(employees, true, true)
        );
    }
}
