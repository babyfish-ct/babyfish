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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

import javax.persistence.Persistence;

import junit.framework.Assert;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.model.jpa.path.GetterType;
import org.babyfish.persistence.XEntityManager;
import org.babyfishdemo.querypath.base.AbstractTest;
import org.babyfishdemo.querypath.entities.AnnualLeave;
import org.babyfishdemo.querypath.entities.Company;
import org.babyfishdemo.querypath.entities.Company__;
import org.babyfishdemo.querypath.entities.Department;
import org.babyfishdemo.querypath.entities.Department__;
import org.babyfishdemo.querypath.entities.Employee;
import org.babyfishdemo.querypath.entities.Employee__;
import org.junit.Test;

/*
 * Notes: 
 * Before learn this case, you'd better learn AssociationFetchPathTest at first
 */
/**
 * @author Tao Chen
 */
public class SimpleOrderPathTest extends AbstractTest {

    private CompanyRepository companyRepository = new CompanyRepository();
    
    private DepartmentRepository departmentRepository = new DepartmentRepository();
    
    private EmployeeRepository employeeRepository = new EmployeeRepository();
    
    @Test
    public void testOrderSelf() {
        
        List<Department> departments = this.departmentRepository.getDepartments(
                Department__.preOrderBy().name().asc()
        );
        assertDepartmentNames(
                departments,
                "Barracks",
                "Dark Shrine",
                "Fleet Beacon",
                "Ghost Academy",
                "Star Gate",
                "Star Port",
                "Templar Archives",
                "Xel'Naga Temple"
        );
        
        departments = this.departmentRepository.getDepartments(
                Department__.preOrderBy().name().desc()
        );
        assertDepartmentNames(
                departments,
                "Xel'Naga Temple",
                "Templar Archives",
                "Star Port",
                "Star Gate",
                "Ghost Academy",
                "Fleet Beacon",
                "Dark Shrine",
                "Barracks"
        );
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...> "
                + "from DEPARTMENT department0_ "
                + "order by department0_.NAME asc", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...> "
                + "from DEPARTMENT department0_ "
                + "order by department0_.NAME desc", 
                this.preparedSqlList.get(1)
        );
    }
    
    @Test
    public void testOrderBySeveralSimpleOrderPaths() {
        
        /*
         * The order between several FetchPaths is NOT important, no affect.
         * The order between FetchPath and SimpleOrderPath is NOT important too, no affect.
         * 
         * But
         * 
         * The order between several SimpleOrderPaths is VERY important.
         */
        
        /*
         * First, order by "Employee.cost.mineral" desc, 
         * Then, order by "Employee.cost.gas" desc.
         */
        List<Employee> employees = this.employeeRepository.getEmployees(
                Employee__.preOrderBy().cost().mineral().desc(),
                Employee__.preOrderBy().cost().gas().desc()
        );
        Assert.assertEquals(12, employees.size());
        assertEmployee(employees.get(0), "Artanis", 400, 400);
        assertEmployee(employees.get(1), "Matt Horner", 400, 300);
        assertEmployee(employees.get(2), "Selendis", 350, 250);
        assertEmployee(employees.get(3), "Mohandar", 250, 150);
        assertEmployee(employees.get(4), "Nova Terra", 200, 100);
        assertEmployee(employees.get(5), "Gabriel Tosh", 200, 100);
        assertEmployee(employees.get(6), "Urun", 150, 100);
        assertEmployee(employees.get(7), "Zeratul", 125, 125);   // *
        assertEmployee(employees.get(8), "Tassadar", 50, 150);   // *
        assertEmployee(employees.get(9), "Karass", 50, 150);     // *
        assertEmployee(employees.get(10), "Jim Raynor", 50, 0);
        assertEmployee(employees.get(11), "Tychus Findlay", 50, 0);
        
        /*
         * First, order by "Employee.cost.gas" desc, 
         * Then, order by "Employee.cost.mineral" desc.
         */
        employees = this.employeeRepository.getEmployees(
                Employee__.preOrderBy().cost().gas().desc(),
                Employee__.preOrderBy().cost().mineral().desc()
        );
        Assert.assertEquals(12, employees.size());
        assertEmployee(employees.get(0), "Artanis", 400, 400);
        assertEmployee(employees.get(1), "Matt Horner", 400, 300);
        assertEmployee(employees.get(2), "Selendis", 350, 250);
        assertEmployee(employees.get(3), "Mohandar", 250, 150);
        assertEmployee(employees.get(4), "Tassadar", 50, 150);   // *
        assertEmployee(employees.get(5), "Karass", 50, 150);     // *
        assertEmployee(employees.get(6), "Zeratul", 125, 125);   // *
        assertEmployee(employees.get(7), "Nova Terra", 200, 100);
        assertEmployee(employees.get(8), "Gabriel Tosh", 200, 100);
        assertEmployee(employees.get(9), "Urun", 150, 100);
        assertEmployee(employees.get(10), "Jim Raynor", 50, 0);
        assertEmployee(employees.get(11), "Tychus Findlay", 50, 0);
        
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "order by "
                +     "employee0_.MINERAL desc, "
                +     "employee0_.GAS desc", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "order by "
                +     "employee0_.GAS desc, "
                +     "employee0_.MINERAL desc",
                this.preparedSqlList.get(1)
        );
    }
    
    @Test
    public void testPreOrderAndPostOrder() {
        /*
         * There are 2 kinds of SimpleOrderPath: Pre and Post.
         * 
         * Pre-SimpleOrderPath has high priority, if the original
         * query already has "order by" clause, the Pre-SimpleOrderPath
         * will be applied BEFORE it.
         * 
         * Post-SimpleOrderpath has low priority, if the original
         * query already has "order by" clause, the Post-SimplelOrderPath
         * will be applied AFTER it.
         */
        
        List<Employee> employees;
        
        /*
         * (1) Test the Pre-SimpleOrderPath will be applied BEFORE 
         * the "order by" clause of the original query
         */
        try (XEntityManager em = JPAContext.createEntityManager()) {
            employees = 
                    em
                    .createQuery(
                            "select e " +
                            "from Employee e " +
                            "order by e.cost.mineral desc",
                            Employee.class
                    )
                    .setQueryPaths(Employee__.preOrderBy().cost().gas().desc())
                    .getResultList();
            /*
             * The final order by plan is
             *      order by e.cost.gas.desc, e.cost.mineral desc 
             */
        }
        Assert.assertEquals(12, employees.size());
        assertEmployee(employees.get(0), "Artanis", 400, 400);
        assertEmployee(employees.get(1), "Matt Horner", 400, 300);
        assertEmployee(employees.get(2), "Selendis", 350, 250);
        assertEmployee(employees.get(3), "Mohandar", 250, 150);
        assertEmployee(employees.get(4), "Tassadar", 50, 150);   // *
        assertEmployee(employees.get(5), "Karass", 50, 150);     // *
        assertEmployee(employees.get(6), "Zeratul", 125, 125);   // *
        assertEmployee(employees.get(7), "Nova Terra", 200, 100);
        assertEmployee(employees.get(8), "Gabriel Tosh", 200, 100);
        assertEmployee(employees.get(9), "Urun", 150, 100);
        assertEmployee(employees.get(10), "Jim Raynor", 50, 0);
        assertEmployee(employees.get(11), "Tychus Findlay", 50, 0);
        
        /*
         * (2) Test the Post-SimpleOrderPath will be applied AFTER 
         * the "order by" clause of the original query
         */
        try (XEntityManager em = JPAContext.createEntityManager()) {
            employees = 
                    em
                    .createQuery(
                            "select e " +
                            "from Employee e " +
                            "order by e.cost.mineral desc",
                            Employee.class
                    )
                    .setQueryPaths(Employee__.postOrderBy().cost().gas().desc())
                    .getResultList();
            /*
             * The final order by plan is
             *      order by e.cost.mineral desc, e.cost.gas.desc
             */
        }
        Assert.assertEquals(12, employees.size());
        assertEmployee(employees.get(0), "Artanis", 400, 400);
        assertEmployee(employees.get(1), "Matt Horner", 400, 300);
        assertEmployee(employees.get(2), "Selendis", 350, 250);
        assertEmployee(employees.get(3), "Mohandar", 250, 150);
        assertEmployee(employees.get(4), "Nova Terra", 200, 100);
        assertEmployee(employees.get(5), "Gabriel Tosh", 200, 100);
        assertEmployee(employees.get(6), "Urun", 150, 100);
        assertEmployee(employees.get(7), "Zeratul", 125, 125);   // *
        assertEmployee(employees.get(8), "Tassadar", 50, 150);   // *
        assertEmployee(employees.get(9), "Karass", 50, 150);     // *
        assertEmployee(employees.get(10), "Jim Raynor", 50, 0);
        assertEmployee(employees.get(11), "Tychus Findlay", 50, 0);
        
        /*
         * (3) Test that Pre-SimpleOrderPath will be applied before
         * the Post-SimpleOrderPath even if it is writeen after
         * Post-SimpleOrderPath
         */
        employees = this.employeeRepository.getEmployees(
                Employee__.postOrderBy().cost().mineral().desc(), //Low priority, second order
                Employee__.preOrderBy().cost().gas().desc() //High priority, first order
        );
        Assert.assertEquals(12, employees.size());
        assertEmployee(employees.get(0), "Artanis", 400, 400);
        assertEmployee(employees.get(1), "Matt Horner", 400, 300);
        assertEmployee(employees.get(2), "Selendis", 350, 250);
        assertEmployee(employees.get(3), "Mohandar", 250, 150);
        assertEmployee(employees.get(4), "Tassadar", 50, 150);   // *
        assertEmployee(employees.get(5), "Karass", 50, 150);     // *
        assertEmployee(employees.get(6), "Zeratul", 125, 125);   // *
        assertEmployee(employees.get(7), "Nova Terra", 200, 100);
        assertEmployee(employees.get(8), "Gabriel Tosh", 200, 100);
        assertEmployee(employees.get(9), "Urun", 150, 100);
        assertEmployee(employees.get(10), "Jim Raynor", 50, 0);
        assertEmployee(employees.get(11), "Tychus Findlay", 50, 0);
        
        
        Assert.assertEquals(3, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "order by "
                +     "employee0_.GAS desc, "
                +     "employee0_.MINERAL desc", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "order by "
                +     "employee0_.MINERAL desc, "
                +     "employee0_.GAS desc", 
                this.preparedSqlList.get(1)
        );
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "order by "
                +     "employee0_.GAS desc, "
                +     "employee0_.MINERAL desc", 
                this.preparedSqlList.get(2)
        );
    }
    
    @Test
    public void testOrderChildrenByJoinButNotFetch() {
        
        // Notes: The association in SimpleOrderPath means join, not fetch.
        // The other method "testFetchAndOrderChildren()" will teach you how 
        // to fetch associations at first and then sort them.
        List<Department> departments = this.departmentRepository.getDepartments(
                Department__.preOrderBy().employees().name().asc()
        );
        
        /* 
         * This SimpleOrderPath try to join(not fetch) the employees
         * if that join is not existing in original query,
         * and then order by the Employee.name.
         * so the final JPQL is
         * 
         *      select d
         *      from Department d
         *      left join d.employees babyfish_join_node_alias_0
         *      order by babyfish_join_node_alias_0.name asc
         *
         * Suppose there is no "select d", the ResultSet created by "join" and "order by"
         * should be looks like this(only show 2 columns)
         * 
         *                           +-+------------------+----------------------------+
         * +---+                     | | Department Name  | Employee Name              |
         * | 0 +----------------\    | |                  | (Sorted, but not selected) |
         * +---+                |    +-+------------------+----------------------------+
         * | 1 +--------------\ \--->|0| Xel'Naga Temple  | <<NULL>>                   |
         * +---+              +----->|1| Fleet Beacon     | Artanis                    |
         * | 2 +-----------+--|----->|2| Ghost Academy    | Gabriel Tosh     /|\       |
         * +---+        /--|--|----->|3| Barracks         | Jim Raynor        |        |
         * | 3 |--------+  |  | /--->|4| Templar Archives | Karass            |        |
         * +---+  /-----|--|--|-|--->|5| Star Port        | Matt Horner       |        |
         * | 4 +--|--+--|--|--|-/ /->|6| Star Gate        | Mohandar      Ascending    |
         * +---+  |  |  |  \--|---|->|2| Ghost Academy    | Nova Terra        |        |
         * | 5 +--/  |  |     \---|->|1| Fleet Beacon     | Selendis          |        |
         * +---+     \--|---------|->|4| Templar Archives | Tassadar          |        |
         * | 6 +--\     \---------|->|3| Barracks         | Tychus Findlay    |        |
         * +---+  \---------------+->|6| Star Gate        | Urun                       |
         * | 7 +-------------------->|7|                  | Zeratul                    |
         * +---+                     +-+-Dark Shrine------+----------------------------+
         */
        assertDepartmentNames(
                departments, 
                "Xel'Naga Temple",
                "Fleet Beacon",
                "Ghost Academy",
                "Barracks",
                "Templar Archives",
                "Star Port",
                "Star Gate",
                "Dark Shrine"
        );
        
        //Finally, assert that SimpleOrderPath can't fetch the associations
        for (Department department : departments) {
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(department.getEmployees()));
        }
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...> "
                + "from DEPARTMENT department0_ "
                + "left outer join EMPLOYEE employees1_ "
                +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + "order by employees1_.NAME asc", 
                this.preparedSqlList.get(0)
        );
    }
    
    @Test
    public void testOrderChildrenByInnerJoinButNotFetch() {
        
        // Notes: The association in SimpleOrderPath means join, not fetch.
        // The other method "testFetchAndOrderChildren()" will teach you how 
        // to fetch associations at first and then sort them.
        List<Department> departments = this.departmentRepository.getDepartments(
                
                // GetterType.REQUIRED means "inner join"
                Department__.preOrderBy().employees(GetterType.REQUIRED).name().asc()
        );
        
        // This test method is very similar with "testOrderChildrenByJoinButNotFetch()",
        // Only one difference is the SimpleOrderPath of this query uses INNER join on 
        // the association. So the department "Xel'Naga Temple" will NOT be selected and
        // the other query results are same.
        assertDepartmentNames(
                departments, 
                "Fleet Beacon",
                "Ghost Academy",
                "Barracks",
                "Templar Archives",
                "Star Port",
                "Star Gate",
                "Dark Shrine"
        );
        
        //Finally, assert that SimpleOrderPath can't fetch the associations
        for (Department department : departments) {
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(department.getEmployees()));
        }
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...> "
                + "from DEPARTMENT department0_ "
                + "inner join EMPLOYEE employees1_ "
                +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + "order by employees1_.NAME asc", 
                this.preparedSqlList.get(0)
        );
    }
    
    @Test
    public void testFetchAndOrderChildren() {
        
        // This test method shows how to fetch an association at first and then order it.
        // Please apply both FetchPath and SimpleOrderPath on one association, like this
        List<Department> departments = this.departmentRepository.getDepartments(
                Department__.begin().employees().end(), // Load the association Departemnt.employees
                Department__.preOrderBy().employees().name().asc() // Order the association Department.employees
        );
        
        /* You can also specify the SimpleOrderPath before the FetchPath, like this
         * 
         *      List<Department> departments = this.departmentRepository.getDepartments(
         *          Department__.preOrderBy().employees().name().asc(),
         *          Department__.begin().employees().end()
         *      );
         * 
         * It will get the same result.
         */
        
        /* 
         * The final JPQL is
         * 
         *      select d
         *      from Department d
         *      left join fetch d.employees babyfish_join_node_alias_0 // Fetch it
         *      order by babyfish_join_node_alias_0.name asc
         *
         * The ResultSet after "join" and "order by"
         * should be looks like this(only show 2 columns)
         * 
         *                           +-+------------------+----------------------------+
         * +---+                     | | Department Name  | Employee Name              |
         * | 0 +----------------\    | |                  | (Sorted, but not selected) |
         * +---+                |    +-+------------------+----------------------------+
         * | 1 +--------------\ \--->|0| Xel'Naga Temple  | <<NULL>>                   |
         * +---+              +----->|1| Fleet Beacon     | Artanis                    |
         * | 2 +-----------+--|----->|2| Ghost Academy    | Gabriel Tosh     /|\       |
         * +---+        /--|--|----->|3| Barracks         | Jim Raynor        |        |
         * | 3 |--------+  |  | /--->|4| Templar Archives | Karass            |        |
         * +---+  /-----|--|--|-|--->|5| Star Port        | Matt Horner       |        |
         * | 4 +--|--+--|--|--|-/ /->|6| Star Gate        | Mohandar      Ascending    |
         * +---+  |  |  |  \--|---|->|2| Ghost Academy    | Nova Terra        |        |
         * | 5 +--/  |  |     \---|->|1| Fleet Beacon     | Selendis          |        |
         * +---+     \--|---------|->|4| Templar Archives | Tassadar          |        |
         * | 6 +--\     \---------|->|3| Barracks         | Tychus Findlay    |        |
         * +---+  \---------------+->|6| Star Gate        | Urun                       |
         * | 7 +-------------------->|7|                  | Zeratul                    |
         * +---+                     +-+-Dark Shrine------+----------------------------+
         */
        assertDepartmentNames(
                departments, 
                "Xel'Naga Temple",
                "Fleet Beacon",
                "Ghost Academy",
                "Barracks",
                "Templar Archives",
                "Star Port",
                "Star Gate",
                "Dark Shrine"
        );
        
        // Finally, assert the association "Department.employees" is loaded 
        // and the elements of each collection is sorted.
        assertEmployeeNames(departments.get(0));
        assertEmployeeNames(departments.get(1), "Artanis", "Selendis");
        assertEmployeeNames(departments.get(2), "Gabriel Tosh", "Nova Terra");
        assertEmployeeNames(departments.get(3), "Jim Raynor", "Tychus Findlay");
        assertEmployeeNames(departments.get(4), "Karass", "Tassadar");
        assertEmployeeNames(departments.get(5), "Matt Horner");
        assertEmployeeNames(departments.get(6), "Mohandar", "Urun");
        assertEmployeeNames(departments.get(7), "Zeratul");
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...>, "
                +     "<...many columns of employees1_...> "
                + "from DEPARTMENT department0_ "
                + "left outer join EMPLOYEE employees1_ "
                +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + "order by employees1_.NAME asc", 
                this.preparedSqlList.get(0)
        );
    }
    
    @Test
    public void testFetchAndOrderByManyLevels() {
        /*
         * Fetch: <<ROOT>> -> departments -> employees -> annualLeaves
         * Order By: 
         *      Company.departments.name ASC
         *      Department.employees.name ASC
         *      Employee.annualLeaves.startTime DESC
         */
        Company protoss = this.companyRepository.getCompanyByName(
                "Protoss", 
                Company__.begin().departments().end(),
                Company__.preOrderBy().departments().name().asc(),
                Company__.begin().departments().employees().end(),
                Company__.preOrderBy().departments().employees().name().asc(),
                Company__.begin().departments().employees().annualLeaves().end(),
                Company__.preOrderBy().departments().employees().annualLeaves().startTime().desc()
        );
        
        Assert.assertEquals(
                1 + 4 + 7 + 7 * 2, //1 company, 4 departments, 7 employees, each employee has 2 annualLeaves
                new EntityNode(
                        "Protoss",
                        new EntityNode(
                                "Dark Shrine",
                                new EntityNode(
                                        "Zeratul",
                                        new EntityNode("[2015-05-17, 2015-05-19]"),
                                        new EntityNode("[2015-03-04, 2015-03-04]")
                                )
                        ),
                        new EntityNode(
                                "Fleet Beacon",
                                new EntityNode(
                                        "Artanis",
                                        new EntityNode("[2015-07-03, 2015-07-14]"),
                                        new EntityNode("[2015-02-18, 2015-03-09]")
                                ),
                                new EntityNode(
                                        "Selendis",
                                        new EntityNode("[2015-02-25, 2015-02-28]"),
                                        new EntityNode("[2015-01-05, 2015-01-05]")
                                )
                        ),
                        new EntityNode(
                                "Star Gate",
                                new EntityNode(
                                        "Mohandar",
                                        new EntityNode("[2015-05-25, 2015-05-26]"),
                                        new EntityNode("[2015-02-28, 2015-03-02]")
                                ),
                                new EntityNode(
                                        "Urun",
                                        new EntityNode("[2015-04-19, 2015-04-25]"),
                                        new EntityNode("[2015-03-09, 2015-03-14]")
                                )
                        ),
                        new EntityNode(
                                "Templar Archives",
                                new EntityNode(
                                        "Karass",
                                        new EntityNode("[2015-03-10, 2015-03-13]"),
                                        new EntityNode("[2015-02-19, 2015-02-26]")
                                        
                                ),
                                new EntityNode(
                                        "Tassadar",
                                        new EntityNode("[2015-02-26, 2015-02-27]"),
                                        new EntityNode("[2015-01-10, 2015-01-10]")
                                )
                        )
                ).validate(protoss)
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of company0_...>, "
                +     "<...many columns of department1_...>, "
                +     "<...many columns of employees2_...>, "
                +     "<...many columns of annualleav3_...> "
                + "from COMPANY company0_ "
                + "left outer join DEPARTMENT department1_ "
                +     "on company0_.COMPANY_ID=department1_.COMPANY_ID "
                + "left outer join EMPLOYEE employees2_ "
                +     "on department1_.DEPARTMENT_ID=employees2_.DEPARTMENT_ID "
                + "left outer join ANNUAL_LEAVE annualleav3_ "
                +     "on employees2_.EMPLOYEE_ID=annualleav3_.EMPLOYEE_ID "
                + "where "
                +     "company0_.NAME=? "
                + "order by "
                +     "department1_.NAME asc, "
                +     "employees2_.NAME asc, "
                +     "annualleav3_.START_TIME desc", 
                this.preparedSqlList.get(0)
        );
    }
    
    private static void assertDepartmentNames(Collection<Department> departments, String ... expectedNames) {
        Assert.assertEquals(expectedNames.length, departments.size());
        int index = 0;
        for (Department department : departments) {
            Assert.assertEquals(expectedNames[index++], department.getName());
        }
    }
    
    private static void assertEmployeeNames(Department department, String ... expectedNames) {
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(department.getEmployees()));
        int index = 0;
        for (Employee employee : department.getEmployees()) {
            Assert.assertEquals(expectedNames[index++], employee.getName());
        }
    }
    
    private static void assertEmployee(Employee employee, String name, int mineral, int gas) {
        Assert.assertEquals(name, employee.getName());
        Assert.assertEquals(mineral, employee.getCost().getMineral());
        Assert.assertEquals(gas, employee.getCost().getGas());
    }
    
    private static class EntityNode {
        
        private String text;
        
        private EntityNode[] childNodes;
        
        public EntityNode(String text, EntityNode ... childNodes) {
            this.text = text;
            this.childNodes = childNodes;
        }
        
        public int validate(Object o) {
            return validate(o, new SimpleDateFormat("yyyy-MM-dd"));
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private int validate(Object o, DateFormat dateFormat) {
            Arguments.mustBeInstanceOfAnyOfValue(
                    "o", 
                    o, 
                    Company.class,
                    Department.class,
                    Employee.class,
                    AnnualLeave.class
            );
            Collection<Object> childEntities = MACollections.emptySet();
            if (o instanceof Company) {
                Company company = (Company)o;
                Assert.assertEquals(this.text, company.getName());
                childEntities = (Collection)company.getDepartments();
            } else if (o instanceof Department) {
                Department department = (Department)o;
                Assert.assertEquals(this.text, department.getName());
                childEntities = (Collection)department.getEmployees();
            } else if (o instanceof Employee) {
                Employee employee = (Employee)o;
                Assert.assertEquals(this.text, employee.getName());
                childEntities = (Collection)employee.getAnnualLeaves();
            } else {
                AnnualLeave annualLeave = (AnnualLeave)o;
                Assert.assertEquals(
                        this.text, 
                        '[' +
                        dateFormat.format(annualLeave.getStartTime()) +
                        ", " +
                        dateFormat.format(annualLeave.getEndTime()) +
                        ']'
                );
            }
            Assert.assertEquals(this.childNodes.length, childEntities.size());
            int index = 0;
            int validatedEntityCount = 1;
            for (Object childEntity : childEntities) {
                validatedEntityCount += this.childNodes[index++].validate(childEntity, dateFormat);
            }
            return validatedEntityCount;
        }
    }
}
