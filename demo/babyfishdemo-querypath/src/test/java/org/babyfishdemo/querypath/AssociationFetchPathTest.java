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

import java.util.Iterator;
import java.util.List;

import javax.persistence.Persistence;

import junit.framework.Assert;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.model.jpa.path.CollectionFetchType;
import org.babyfish.model.jpa.path.GetterType;
import org.babyfish.util.GraphTravelAction;
import org.babyfish.util.GraphTravelContext;
import org.babyfish.util.GraphTraveler;
import org.babyfishdemo.querypath.base.AbstractTest;
import org.babyfishdemo.querypath.entities.AnnualLeave;
import org.babyfishdemo.querypath.entities.AnnualLeaveState;
import org.babyfishdemo.querypath.entities.Department;
import org.babyfishdemo.querypath.entities.Department__;
import org.babyfishdemo.querypath.entities.Employee;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class AssociationFetchPathTest extends AbstractTest {
    
    private DepartmentRepository departmentRepository = new DepartmentRepository();
    
    @Test
    public void testOptionalFetch() {
        
        // First, we assert the association is lazy at first
        {
            List<Department> departments = this.departmentRepository.getDepartments();
            for (Department department : departments) {
                if (department.getCompany() != null) {
                    Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(department.getCompany()));
                }
            }
        }
        
        // Second, assert we can query all departments 
        // and load the many-to-one association by FetchPath with GetterType.OPTINONAL
        {
            List<Department> departments = this.departmentRepository.getDepartments(
                    // GetterType.OPTIONAL is default behavior, you can also write like this:
                    // Department__.begin().company().end()
                    Department__.begin().company(GetterType.OPTIONAL).end()
            );
            
            // "GetterType.OPTINAL" means "left join fetch", 
            // so "Xel'Naga Temple" will be selected though it belongs to no company
            // The total department count should be 8
            Assert.assertEquals(8, departments.size());
            
            for (Department department : departments) {
                Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(department.getCompany()));
            }
        }
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...> "
                + "from DEPARTMENT department0_", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...>, "
                +     "<...many columns of company1_...> "
                + "from DEPARTMENT department0_ "
                + "left outer join COMPANY company1_ "
                +     "on department0_.COMPANY_ID=company1_.COMPANY_ID", 
                this.preparedSqlList.get(1)
        );
    }
    
    @Test
    public void testRequiredFetch() {
        
        // First, we assert the association is lazy at first
        {
            List<Department> departments = this.departmentRepository.getDepartments();
            for (Department department : departments) {
                if (department.getCompany() != null) {
                    Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(department.getCompany()));
                }
            }
        }
        
        // Scond, assert we can ONLY query some departments 
        // and load the many-to-one association by FetchPath with GetterType.REQUIRED
        {
            List<Department> departments = this.departmentRepository.getDepartments(
                    Department__.begin().company(GetterType.REQUIRED).end()
            );
            
            // "GetterType.OPTINAL" means "inner join fetch", 
            // so "Xel'Naga Temple" will NOT be selected though it belongs to no company
            // The total department count should be 7
            Assert.assertEquals(7, departments.size());
            
            for (Department department : departments) {
                Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(department.getCompany()));
            }
        }
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...> "
                + "from DEPARTMENT department0_", 
                this.preparedSqlList.get(0)
        );
        /*
         * FetchPath of required mode will be translated to "INNER JOIN"
         */
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...>, "
                +     "<...many columns of company1_...> "
                + "from DEPARTMENT department0_ "
                + "inner join COMPANY company1_ "
                +     "on department0_.COMPANY_ID=company1_.COMPANY_ID", 
                this.preparedSqlList.get(1)
        );
    }
    
    @Test
    public void testFetchOneToManyByAllMode() {
        
        // First, we assert the association is lazy at first
        {
            List<Department> departments = this.departmentRepository.getDepartmentsByEmployeeName("Urun");
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(departments.get(0).getEmployees()));
        }
        
        // Second, assert we can load the one-to-many association by FetchPath
        {
            List<Department> departments = this.departmentRepository.getDepartmentsByEmployeeName(
                    "Urun", 
                    Department__.begin().employees().end()
                    // or "Department__.begin().employees(CollectionFetchType.ALL).end()"
            );
            Assert.assertEquals(1, departments.size());
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(departments.get(0).getEmployees()));
            Assert.assertEquals(2, departments.get(0).getEmployees().size());
            
            Iterator<Employee> itr = departments.get(0).getEmployees().iterator();
            Assert.assertEquals("Mohandar", itr.next().getName());
            Assert.assertEquals("Urun", itr.next().getName());
            /*
             * This collection is concurrent, it has 2 elements
             */
            Assert.assertEquals(2, departments.get(0).getEmployees().size());
        }
        
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...> "
                + "from DEPARTMENT department0_ "
                +     "inner join EMPLOYEE employees1_ on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + "where employees1_.NAME=?", 
                this.preparedSqlList.get(0)
        );
        /*
         * For one-to-many association, when the CollecctionFetchType of the fetch path is ALL(or default),
         * The join generated by FetchPath will NOT be combined with the join of the original query,
         * the collection will fetch all the elements.
         */
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...>, "
                +     "<...many columns of employees2_...> "
                + "from DEPARTMENT department0_ "
                + "inner join EMPLOYEE employees1_ " // Join of original SQL
                +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + "left outer join EMPLOYEE employees2_ " // Join added by FetchPath
                +     "on department0_.DEPARTMENT_ID=employees2_.DEPARTMENT_ID "
                + "where employees1_.NAME=?", 
                this.preparedSqlList.get(1)
        );
    }
    
    @Test
    public void testFetchOneToManyByPartialMode() {
        // First, we assert the association is lazy at first
        {
            List<Department> departments = this.departmentRepository.getDepartmentsByEmployeeName("Urun");
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(departments.get(0).getEmployees()));
        }
        
        // Second, assert we can load the one-to-many association by FetchPath
        {
            List<Department> departments = this.departmentRepository.getDepartmentsByEmployeeName(
                    "Urun", 
                    Department__.begin().employees(CollectionFetchType.PARTIAL).end()
            );
            Assert.assertEquals(1, departments.size());
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(departments.get(0).getEmployees()));
            
            Assert.assertEquals(1, departments.get(0).getEmployees().size());
            Assert.assertEquals("Urun", departments.get(0).getEmployees().iterator().next().getName());
            
            /* 
             * This collection is broken, it only contains one element,
             * because the join create by the FetchPath whose mode is CollectionFetchType.PARTIAL 
             * can be merged with the existing join of JPQL in DepartmentRepository
             *
             * Notes: 
             *      Generally speaking, CollectionFetchType.PARTIAL is a bad choice because it can creates the broken collections. 
             *      Please ONLY use it to do some special performance optimization
             */
            Assert.assertEquals(1, departments.get(0).getEmployees().size());
        }
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...> "
                + "from DEPARTMENT department0_ "
                +     "inner join EMPLOYEE employees1_ on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + "where employees1_.NAME=?", 
                this.preparedSqlList.get(0)
        );
        
        /*
         * For one-to-many association, when the CollecctionFetchType of the fetch path is PARTIAL,
         * The join generated by FetchPath will be combined with the join of the original query,
         * the collection will fetch some elements, not all elements.
         * 
         * The merged type of DIFFERENT join types is INNER
         */
        Assert.assertEquals(
                "select "
                + "<...many columns of department0_...>, "
                + "<...many columns of employees1_...> "
                + "from DEPARTMENT department0_ "
                + "inner join EMPLOYEE employees1_ " // Join merged from original join and Fetch Path join, "INNER" merge "LEFT" -> "INNER"
                +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + "where employees1_.NAME=?", 
                this.preparedSqlList.get(1)
        );
    }
    
    @Test
    public void testFetchWithDepth() {
        
        // First, we assert the association is lazy at first
        {
            Department department = this.departmentRepository.getDepartmentByName("Templar Archives");
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(department.getEmployees()));
        }
        
        // Second, load employees the department and and load the subordinates 
        // of each employee recursively for 4 layers.
        {
            Department department = this.departmentRepository.getDepartmentByName(
                    "Templar Archives",
                    Department__.begin().employees().subordinates().subordinates().subordinates().subordinates().end()
            );
        
            /*
             * The returned object graph
             * 
             *  department-----> +-Templar Archives
             *                   |
             *                   +---+-Tassadar
             *                   |   |
             *                   |   +---+-Zeratul
             *                   |   |   |
             *                   |   |   \---+-Artanis
             *                   |   |       |
             *                   |   |       \---+-Selendis
             *                   |   |           |
             *                   |   |           +---+-Mohandar
             *                   |   |           |   |
             *                   |   |           |   \-----{Unloaded Children}
             *                   |   |           |
             *                   |   |           \---+-Urun
             *                   |   |               |
             *                   |   |               \-----{Unloaded Children}
             *                   |   |
             *                   |   \-----Karass(A: same reference with B)
             *                   |
             *                   \-----Karass(B: same reference with A)
             */
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(department.getEmployees()));
            Assert.assertEquals(2, department.getEmployees().size());
            
            new EmployeeTreeNode(
                    "Tassadar", 
                    new EmployeeTreeNode(
                            "Zeratul", 
                            new EmployeeTreeNode(
                                    "Artanis", 
                                    new EmployeeTreeNode(
                                            "Selendis",
                                            new EmployeeTreeNode(
                                                    "Mohandar", 
                                                    EmployeeTreeNode.UNLOADED_CHILDREN
                                            ),
                                            new EmployeeTreeNode(
                                                    "Urun", 
                                                    EmployeeTreeNode.UNLOADED_CHILDREN
                                            )
                                    )
                            )
                    ),
                    new EmployeeTreeNode("Karass")
            ).validate(department.getEmployees().iterator().next());
            
            new EmployeeTreeNode("Karass")
            .validate(department.getEmployees().toArray(new Employee[2])[1]);
        }
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...> "
                + "from DEPARTMENT department0_ "
                + "where department0_.NAME=?",
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...>, "
                +     "<...many columns of employees1_...>, "
                +     "<...many columns of subordinat2_...>, "
                +     "<...many columns of subordinat3_...>, "
                +     "<...many columns of subordinat4_...>, "
                +     "<...many columns of subordinat5_...> "
                + "from DEPARTMENT department0_ "
                + "left outer join EMPLOYEE employees1_ "
                +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + "left outer join EMPLOYEE subordinat2_ "
                +     "on employees1_.EMPLOYEE_ID=subordinat2_.SUPERVISOR_ID "
                + "left outer join EMPLOYEE subordinat3_ "
                +     "on subordinat2_.EMPLOYEE_ID=subordinat3_.SUPERVISOR_ID "
                + "left outer join EMPLOYEE subordinat4_ "
                +     "on subordinat3_.EMPLOYEE_ID=subordinat4_.SUPERVISOR_ID "
                + "left outer join EMPLOYEE subordinat5_ "
                +     "on subordinat4_.EMPLOYEE_ID=subordinat5_.SUPERVISOR_ID "
                + "where department0_.NAME=?", 
                this.preparedSqlList.get(1));
    }
    
    @Test
    public void testFetchWithBreadth() {
        
        // First, we assert the associations are lazy at first
        {
            Department department = this.departmentRepository.getDepartmentByName("Templar Archives");
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(department.getCompany()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(department.getEmployees()));
        }
        
        // Second, load the both the company and the employees of a department
        {
            Department department = this.departmentRepository.getDepartmentByName(
                    "Templar Archives",
                    Department__.begin().company().end(),
                    Department__.begin().employees().end()
            );
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(department.getCompany()));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(department.getEmployees()));
            
            Assert.assertEquals("Protoss", department.getCompany().getName());
            Assert.assertEquals(2, department.getEmployees().size());
            Assert.assertEquals("Tassadar", department.getEmployees().iterator().next().getName());
            Assert.assertEquals("Karass", department.getEmployees().toArray(new Employee[2])[1].getName());
        }
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...> "
                + "from DEPARTMENT department0_ "
                + "where department0_.NAME=?", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...>, "
                +     "<...many columns of company1_...>, "
                +     "<...many columns of employees2_...> "
                + "from DEPARTMENT department0_ "
                + "left outer join COMPANY company1_ "
                +     "on department0_.COMPANY_ID=company1_.COMPANY_ID "
                + "left outer join EMPLOYEE employees2_ "
                +     "on department0_.DEPARTMENT_ID=employees2_.DEPARTMENT_ID "
                + "where department0_.NAME=?", 
                this.preparedSqlList.get(1)
        );
    }
    
    @Test
    public void testFetchWithBothDepthAndBreadth() {
        // First, we assert the associations are lazy at first
        {
            Department department = this.departmentRepository.getDepartmentByName("Templar Archives");
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(department.getCompany()));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(department.getEmployees()));
        }
        
        // Second, fetch with both depth and breadth
        {
            /*
             * FetchPaths specified by arguments
             *      (1) .company
             *      (2) .employees.subordinates.subordinates.subordinates.subordinates
             *      (3) .employees.subordinates.annualLeaves
             *      (4) .employees.supervisior.annualLeaves
             * 
             * The tree structure of final, merged FetchPlan
             * 
             * +-<<ROOT>>
             * |
             * \-----+-----company
             *       |
             *       +---+-employees
             *           |
             *           +---+-subordinates
             *           |   |
             *           |   \---+-subordinates
             *           |       |
             *           |       \---+-subordinates
             *           |           |
             *           |           \-----subordinates
             *           |
             *           +---+-subordinates
             *           |   |
             *           |   \-----annualLeaves
             *           |
             *           \---+-supervisor
             *               |
             *               \-----annualLeaves
             */
            Department department = this.departmentRepository.getDepartmentByName(
                    "Templar Archives",
                    Department__.begin().company().end(),
                    Department__.begin().employees().subordinates().subordinates().subordinates().subordinates().end(),
                    Department__.begin().employees().subordinates().annualLeaves().end(),
                    Department__.begin().employees().supervisor().annualLeaves().end()
            );
            
            Assert.assertEquals("Protoss", department.getCompany().getName());
            
            Assert.assertEquals(2, department.getEmployees().size());
            new EmployeeTreeNode(
                    "Tassadar", 
                    new EmployeeTreeNode(
                            "Zeratul", 
                            new EmployeeTreeNode(
                                    "Artanis", 
                                    new EmployeeTreeNode(
                                            "Selendis",
                                            new EmployeeTreeNode(
                                                    "Mohandar", 
                                                    EmployeeTreeNode.UNLOADED_CHILDREN
                                            ),
                                            new EmployeeTreeNode(
                                                    "Urun", 
                                                    EmployeeTreeNode.UNLOADED_CHILDREN
                                            )
                                    )
                            )
                    ),
                    new EmployeeTreeNode("Karass")
            ).validate(department.getEmployees().iterator().next());
            new EmployeeTreeNode("Karass")
            .validate(department.getEmployees().toArray(new Employee[2])[1]);
            
            new GraphTraveler<Employee>() {
    
                @Override
                protected Iterator<Employee> getNeighborNodeIterator(Employee node) {
                    if (Persistence.getPersistenceUtil().isLoaded(node.getSubordinates())) {
                        return node.getSubordinates().iterator();
                    }
                    return null;
                }
                
                @Override
                protected void preTravelNeighborNodes(
                        GraphTravelContext<Employee> ctx,
                        GraphTravelAction<Employee> optionalGraphTravelAction) {
                    Employee employee = ctx.getNode();
                    List<AnnualLeave> annualLeaveList = null;
                    if (Persistence.getPersistenceUtil().isLoaded(employee.getAnnualLeaves())) {
                        annualLeaveList = new ArrayList<>(employee.getAnnualLeaves());
                    }
                    switch (employee.getName()) {
                    case "Tassadar":
                        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(employee.getAnnualLeaves()));
                        Assert.assertEquals(2, annualLeaveList.size());
                        assertAnnualLeave(annualLeaveList.get(0), AnnualLeaveState.APPROVED, "2015-01-10", "2015-01-10");
                        assertAnnualLeave(annualLeaveList.get(1), AnnualLeaveState.APPROVED, "2015-02-26", "2015-02-27");
                        break;
                    case "Karass":
                        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(employee.getAnnualLeaves()));
                        assertAnnualLeave(annualLeaveList.get(0), AnnualLeaveState.REJECTED, "2015-02-19", "2015-02-26");
                        assertAnnualLeave(annualLeaveList.get(1), AnnualLeaveState.APPROVED, "2015-03-10", "2015-03-13");
                        break;
                    case "Zeratul":
                        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(employee.getAnnualLeaves()));
                        assertAnnualLeave(annualLeaveList.get(0), AnnualLeaveState.APPROVED, "2015-03-04", "2015-03-04");
                        assertAnnualLeave(annualLeaveList.get(1), AnnualLeaveState.PENDING, "2015-05-17", "2015-05-19");
                        break;
                    default:
                        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(employee.getAnnualLeaves()));
                        break;
                    }
                }
            }
            .depthFirstTravel(department.getEmployees());
        }
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...> "
                + "from DEPARTMENT department0_ "
                + "where department0_.NAME=?", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...>, "
                +     "<...many columns of company1_...>, "
                +     "<...many columns of employees2_...>, "
                +     "<...many columns of subordinat3_...>, "
                +     "<...many columns of subordinat4_...>, "
                +     "<...many columns of subordinat5_...>, "
                +     "<...many columns of subordinat6_...>, "
                +     "<...many columns of annualleav7_...>, "
                +     "<...many columns of employee8_...>, "
                +     "<...many columns of annualleav9_...> "
                + "from DEPARTMENT department0_ "
                + "left outer join COMPANY company1_ "
                +     "on department0_.COMPANY_ID=company1_.COMPANY_ID "
                + "left outer join EMPLOYEE employees2_ "
                +     "on department0_.DEPARTMENT_ID=employees2_.DEPARTMENT_ID "
                + "left outer join EMPLOYEE subordinat3_ "
                +     "on employees2_.EMPLOYEE_ID=subordinat3_.SUPERVISOR_ID "
                + "left outer join EMPLOYEE subordinat4_ "
                +     "on subordinat3_.EMPLOYEE_ID=subordinat4_.SUPERVISOR_ID "
                + "left outer join EMPLOYEE subordinat5_ "
                +     "on subordinat4_.EMPLOYEE_ID=subordinat5_.SUPERVISOR_ID "
                + "left outer join EMPLOYEE subordinat6_ "
                +     "on subordinat5_.EMPLOYEE_ID=subordinat6_.SUPERVISOR_ID "
                + "left outer join ANNUAL_LEAVE annualleav7_ "
                +     "on subordinat3_.EMPLOYEE_ID=annualleav7_.EMPLOYEE_ID "
                + "left outer join EMPLOYEE employee8_ "
                +     "on employees2_.SUPERVISOR_ID=employee8_.EMPLOYEE_ID "
                + "left outer join ANNUAL_LEAVE annualleav9_ "
                +     "on employee8_.EMPLOYEE_ID=annualleav9_.EMPLOYEE_ID "
                + "where department0_.NAME=?", 
                this.preparedSqlList.get(1));
    }
    
    @Test
    public void testMergeConflictFetchPath() {
        
        List<Department> departments =
                this.departmentRepository.getDepartmentsByEmployeeNames(
                        MACollections.wrap(
                                "Jim Raynor", 
                                "Tychus Findlay", 
                                "Nova Terra"
                        ), 
                        
                        // CollectionFetchType.ALL is default behavior, so you can also write
                        // Department__.begin().employees().annualLeaves(GetterType.REQUIRED).end()
                        Department__.begin().employees(CollectionFetchType.ALL).annualLeaves(GetterType.REQUIRED).end(),
                        
                        // GetterType.OPTIONAL(outer join) is default behavior, so you can also write
                        // Department__.begin().employees(CollectionFetchType.PARTIAL).annualLeaves().end()
                        Department__.begin().employees(CollectionFetchType.PARTIAL).annualLeaves(GetterType.OPTIONAL).end()
                );
        
        /*
         * In this example, two FetchPaths are conflicts:
         *      The CollectionFetchType of Department.employees is ALL in first path, but PARTIAL in second path.
         *      The GetterType of Employee.annualLeaves is REQUIRED(inner join) in first path, but OPTIONAL in second path(outer join).
         * 
         * Merge rules:
         *      (1) When the GetterTypes are not same, merged result is GetterType.REQUIRED
         *      (2) When the CollectionFetchTypes are not same, merged result is CollectionFetchType.PARTIAL
         * 
         * So the merged Fetch Plan is
         * 
         *  +-<<ROOT>>
         *  |
         *  \---+-employees(collectionFetchType = PARTIAL)
         *      |
         *      \-----annualLeaves(getterType = REQUIRED)
         *      
         * The Department.employees is fetched by CollectionFetchType.PARTIAL, so it will not create a new "join fetch"
         * to fetch the collection elements when the original query already has the join of the same association,
         * the existing "join" will be changed to be "fetch join" so that the collection will not be FULLY fetched, 
         * the elements that are missed in SQL ResultSet will be ignored.
         * (
         *      "Gabriel Tosh" will NOT be returned even if its parent department "Ghost Academy" will be returned,
         *      because the "in(...)" predicate of SQL can not select it and the CollectionFetchType is PARTIAL which
         *      does not guarantee the fetch collection is completed.
         * )
         * 
         * The Employee.annualLeaves is fetched by GetterType.REQUIRED, so it will use "inner join" to fetch the
         * collection, the employees that have no annualLeaves will not't be selected in SQL ResultSet.
         * (
         *      Though "Tychus Findlay" can be selected by "in(...)" predicate of SQL, but it has no annualLeaves
         *      and the fetch node "Employee.annualLeaves" uses GettType.REQUIRED(inner join) so that it can be selected,
         *      Unfortunately, the fetch node "Department.employees" uses CollectionFetchType.PARTIAL so that
         *      it can not be returned too even if its parent department "Barracks" will be returned.
         * )
         * 
         * You can consider the final JPQL as
         *      select d from department d
         *      inner join d.employees e
         *      inner join annualLeaves // This is inner join, so "Tychus Findlay" is missed
         *      where e.name in ('Jim Raynor', 'Tychus Findlay', 'Nova Terra') // Gabriel Tosh is missed
         * 
         * Notes: 
         *      Generally speaking, CollectionFetchType.PARTIAL is a bad choice because it can creates the broken collections. 
         *      Please ONLY use it in some special performance optimization
         */
        Assert.assertEquals(2, departments.size());
        
        Assert.assertEquals("Barracks", departments.get(0).getName());
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(departments.get(0).getEmployees()));
        // The collection is broken, it does not contain all elements 
        // because of CollectionFetchType.PARTIAL and the existing join of JPQL in DepartmentRepository
        Assert.assertEquals(1, departments.get(0).getEmployees().size());
        Assert.assertEquals("Jim Raynor", departments.get(0).getEmployees().iterator().next().getName());
        Assert.assertTrue(
                Persistence.getPersistenceUtil().isLoaded(
                        departments.get(0).getEmployees().iterator().next().getAnnualLeaves()
                )
        );
        
        Assert.assertEquals("Ghost Academy", departments.get(1).getName());
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(departments.get(1).getEmployees()));
        // The collection is broken, it does not contain all elements 
        // because of CollectionFetchType.PARTIAL and the existing join of JPQL in DepartmentRepository
        Assert.assertEquals(1, departments.get(1).getEmployees().size());
        Assert.assertEquals("Nova Terra", departments.get(1).getEmployees().iterator().next().getName());
        Assert.assertTrue(
                Persistence.getPersistenceUtil().isLoaded(
                        departments.get(1).getEmployees().iterator().next().getAnnualLeaves()
                )
        );
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...>, "
                +     "<...many columns of employees1_...>, "
                +     "<...many columns of annualleav2_...> "
                + "from DEPARTMENT department0_ "
                + "inner join EMPLOYEE employees1_ "
                +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + "inner join ANNUAL_LEAVE annualleav2_ "
                +     "on employees1_.EMPLOYEE_ID=annualleav2_.EMPLOYEE_ID "
                + "where employees1_.NAME in (? , ? , ?)", 
                this.preparedSqlList.get(0)
        );
    }
    
    private static void assertAnnualLeave(
            AnnualLeave annualLeave, 
            AnnualLeaveState state, 
            String startTime, 
            String endTime) {
        Assert.assertEquals(state, annualLeave.getState());
        Assert.assertEquals(date(startTime), annualLeave.getStartTime());
        Assert.assertEquals(date(endTime), annualLeave.getEndTime());
    }
}
