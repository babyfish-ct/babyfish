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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import junit.framework.Assert;

import org.babyfish.collection.MACollections;
import org.babyfish.model.jpa.path.GetterType;
import org.babyfishdemo.spring.dal.base.AbstractRepositoryTest;
import org.babyfishdemo.spring.entities.Employee;
import org.babyfishdemo.spring.entities.Employee__;
import org.babyfishdemo.spring.model.EmployeeSpecification;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class DynamicConditionTest extends AbstractRepositoryTest {
    
    @Test
    public void testQueryAll() {
        
        List<Employee> employees = this.employeeRepository.getEmployees(
                null, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "order by employee0_.FIRST_NAME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Artanis, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0893-04-29, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Gabriel, "
                +       "lastName: Tosh "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1980-09-24, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Jim, "
                +       "lastName: Raynor "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1981-01-05, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Karass, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1245-03-27, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Matt, "
                +       "lastName: Horner "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1990-07-14, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Mohandar, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1423-02-17, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Nova, "
                +       "lastName: Terra "
                +     "}, "
                +     "gender: FEMALE, "
                +     "birthday: 1993-05-04, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Selendis, "
                +       "lastName: null "
                +     "}, "
                +     "gender: FEMALE, "
                +     "birthday: 1003-12-11, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Tassadar, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0756-08-24, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Tychus, "
                +       "lastName: Findlay "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1983-04-25, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Urun, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1490-08-25, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
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
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testQueryByFirstName() {
        
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setLikeFirstName("s");
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "where upper(employee0_.FIRST_NAME) like ? "
                + "order by employee0_.FIRST_NAME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Artanis, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0893-04-29, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Karass, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1245-03-27, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Selendis, "
                +       "lastName: null "
                +     "}, "
                +     "gender: FEMALE, "
                +     "birthday: 1003-12-11, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Tassadar, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0756-08-24, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Tychus, "
                +       "lastName: Findlay "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1983-04-25, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testQueryByLastName() {

        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setLikeLastName("s");
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "where upper(employee0_.LAST_NAME) like ? "
                + "order by employee0_.FIRST_NAME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Gabriel, "
                +       "lastName: Tosh "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1980-09-24, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testQueryMaleEmployees() {
        
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setGender(Employee.Gender.MALE);
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "where employee0_.GENDER=? "
                + "order by employee0_.FIRST_NAME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Artanis, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0893-04-29, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Gabriel, "
                +       "lastName: Tosh "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1980-09-24, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Jim, "
                +       "lastName: Raynor "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1981-01-05, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Karass, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1245-03-27, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Matt, "
                +       "lastName: Horner "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1990-07-14, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Mohandar, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1423-02-17, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Tassadar, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0756-08-24, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Tychus, "
                +       "lastName: Findlay "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1983-04-25, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Urun, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1490-08-25, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
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
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testQueryFemaleEmployees() {
        
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setGender(Employee.Gender.FEMALE);
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "where employee0_.GENDER=? "
                + "order by employee0_.FIRST_NAME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Nova, "
                +       "lastName: Terra "
                +     "}, "
                +     "gender: FEMALE, "
                +     "birthday: 1993-05-04, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Selendis, "
                +       "lastName: null "
                +     "}, "
                +     "gender: FEMALE, "
                +     "birthday: 1003-12-11, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testQueryByMinBirthday() throws ParseException {
        
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setMinBirthday(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "where employee0_.BIRTHDAY>=? "
                + "order by employee0_.FIRST_NAME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Matt, "
                +       "lastName: Horner "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1990-07-14, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Nova, "
                +       "lastName: Terra "
                +     "}, "
                +     "gender: FEMALE, "
                +     "birthday: 1993-05-04, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testQueryByMaxBirthday() throws ParseException {
        
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setMaxBirthday(new SimpleDateFormat("yyyy-MM-dd").parse("1000-01-01"));
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "where employee0_.BIRTHDAY<=? "
                + "order by employee0_.FIRST_NAME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Artanis, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0893-04-29, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Tassadar, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0756-08-24, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
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
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testQueryByBirthdayRange() throws ParseException {
        
        EmployeeSpecification specification = new EmployeeSpecification();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        specification.setMinBirthday(dateFormat.parse("1980-01-01"));
        specification.setMaxBirthday(dateFormat.parse("1990-01-01"));
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "where employee0_.BIRTHDAY between ? and ? "
                + "order by employee0_.FIRST_NAME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Gabriel, "
                +       "lastName: Tosh "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1980-09-24, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Jim, "
                +       "lastName: Raynor "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1981-01-05, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Tychus, "
                +       "lastName: Findlay "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1983-04-25, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testQueryByDepartmentName() {
        
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setLikeDepartmentName("temp");
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "inner join DEPARTMENT department1_ "
                +     "on employee0_.DEPARTMENT_ID=department1_.DEPARTMENT_ID "
                + "where upper(department1_.NAME) like ? "
                + "order by employee0_.FIRST_NAME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Karass, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1245-03-27, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Tassadar, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0756-08-24, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testQueryByInclusiveDepartments() {
        
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setIncludedDepartmentNames(
                MACollections.wrap("Ghost Academy", "Templar Archive",  "Dark Shrine")
        );
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "where exists ("
                +     "select 0 "
                +     "from DEPARTMENT department1_ "
                +     "inner join EMPLOYEE employees2_ "
                +         "on department1_.DEPARTMENT_ID=employees2_.DEPARTMENT_ID "
                +     "where "
                +         "employee0_.EMPLOYEE_ID=employees2_.EMPLOYEE_ID "
                +     "and "
                +         "(department1_.NAME in (? , ? , ?))"
                + ") "
                + "order by employee0_.FIRST_NAME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Gabriel, "
                +       "lastName: Tosh "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1980-09-24, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Karass, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1245-03-27, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Nova, "
                +       "lastName: Terra "
                +     "}, "
                +     "gender: FEMALE, "
                +     "birthday: 1993-05-04, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Tassadar, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0756-08-24, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
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
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testQueryByExclusiveDepartments() {
        
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setExcludedDepartmentNames(
                MACollections.wrap("Ghost Academy", "Templar Archive",  "Dark Shrine")
        );
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "where  not ("
                +     "exists ("
                +         "select 0 "
                +         "from DEPARTMENT department1_ "
                +         "inner join EMPLOYEE employees2_ "
                +             "on department1_.DEPARTMENT_ID=employees2_.DEPARTMENT_ID "
                +         "where "
                +             "employee0_.EMPLOYEE_ID=employees2_.EMPLOYEE_ID "
                +         "and "
                +             "(department1_.NAME in (? , ? , ?))"
                +     ")"
                + ") "
                + "order by employee0_.FIRST_NAME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Artanis, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0893-04-29, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Jim, "
                +       "lastName: Raynor "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1981-01-05, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Matt, "
                +       "lastName: Horner "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1990-07-14, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Mohandar, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1423-02-17, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Selendis, "
                +       "lastName: null "
                +     "}, "
                +     "gender: FEMALE, "
                +     "birthday: 1003-12-11, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Tychus, "
                +       "lastName: Findlay "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1983-04-25, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Urun, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1490-08-25, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testQueryEmployeesWithPendingAnnualLeaves() {
        
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setHasPendingAnnualLeaves(true);
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "where exists ("
                +     "select 0 "
                +     "from ANNUAL_LEAVE annualleav1_ "
                +     "where "
                +         "employee0_.EMPLOYEE_ID=annualleav1_.EMPLOYEE_ID "
                +     "and "
                +         "annualleav1_.STATE=?"
                + ") "
                + "order by employee0_.FIRST_NAME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Matt, "
                +       "lastName: Horner "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1990-07-14, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Nova, "
                +       "lastName: Terra "
                +     "}, "
                +     "gender: FEMALE, "
                +     "birthday: 1993-05-04, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Tassadar, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0756-08-24, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Tychus, "
                +       "lastName: Findlay "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1983-04-25, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testQueryEmployeesWithoutPendingAnnualLeaves() {
        
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setHasPendingAnnualLeaves(false);
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "where  not ("
                +     "exists ("
                +         "select 0 "
                +         "from ANNUAL_LEAVE annualleav1_ "
                +         "where "
                +             "employee0_.EMPLOYEE_ID=annualleav1_.EMPLOYEE_ID "
                +         "and "
                +             "annualleav1_.STATE=?"
                +     ")"
                + ") "
                + "order by employee0_.FIRST_NAME asc", 
                this.preparedSqlList.get(0)
        );
        
        
        Assert.assertEquals(
                "[ "
                +   "{ "
                +     "name: { "
                +       "firstName: Artanis, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 0893-04-29, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Gabriel, "
                +       "lastName: Tosh "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1980-09-24, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Jim, "
                +       "lastName: Raynor "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1981-01-05, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Karass, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1245-03-27, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Mohandar, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1423-02-17, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Selendis, "
                +       "lastName: null "
                +     "}, "
                +     "gender: FEMALE, "
                +     "birthday: 1003-12-11, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
                +   "{ "
                +     "name: { "
                +       "firstName: Urun, "
                +       "lastName: null "
                +     "}, "
                +     "gender: MALE, "
                +     "birthday: 1490-08-25, "
                +     "description: @UnloadedClob, "
                +     "image: @UnloadedBlob, "
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "}, "
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
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testQueryByCombinedConditions() throws ParseException {
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.setLikeFirstName("a");
        specification.setGender(Employee.Gender.MALE);
        specification.setMinBirthday(dateFormat.parse("0800-01-01"));
        specification.setMaxBirthday(dateFormat.parse("1200-01-01"));
        specification.setLikeDepartmentName("e");
        specification.setIncludedDepartmentNames(
                MACollections.wrap("Dark Shrine", "Templar Archive", "Ghost Academy", "Barracks")
        );
        specification.setExcludedDepartmentNames(
                MACollections.wrap("Barracks", "Star Port", "StarGate", "Fleet Beacon")
        );
        specification.setHasPendingAnnualLeaves(false);
        List<Employee> employees = this.employeeRepository.getEmployees(
                specification, 
                Employee__.preOrderBy().name().firstName().asc()
        );
        
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of employee0_...> "
                + "from EMPLOYEE employee0_ "
                + "inner join DEPARTMENT department1_ "
                +     "on employee0_.DEPARTMENT_ID=department1_.DEPARTMENT_ID "
                + "where "
                +     "(upper(employee0_.FIRST_NAME) like ?) "
                + "and "
                +     "employee0_.GENDER=? "
                + "and "
                +     "(employee0_.BIRTHDAY between ? and ?) "
                + "and "
                +     "(upper(department1_.NAME) like ?) "
                + "and ("
                +     "exists ("
                +         "select 0 "
                +         "from DEPARTMENT department2_ "
                +         "inner join EMPLOYEE employees3_ "
                +             "on department2_.DEPARTMENT_ID=employees3_.DEPARTMENT_ID "
                +         "where "
                +             "employee0_.EMPLOYEE_ID=employees3_.EMPLOYEE_ID "
                +         "and "
                +             "(department2_.NAME in (? , ? , ? , ?))"
                +     ")"
                + ") "
                + "and  not ("
                +     "exists ("
                +         "select 0 "
                +         "from DEPARTMENT department4_ "
                +         "inner join EMPLOYEE employees5_ "
                +             "on department4_.DEPARTMENT_ID=employees5_.DEPARTMENT_ID "
                +         "where "
                +             "employee0_.EMPLOYEE_ID=employees5_.EMPLOYEE_ID "
                +         "and "
                +             "(department4_.NAME in (? , ? , ? , ?))"
                +     ")"
                + ") "
                + "and  not ("
                +     "exists ("
                +         "select 0 "
                +         "from ANNUAL_LEAVE annualleav6_ "
                +         "where "
                +             "employee0_.EMPLOYEE_ID=annualleav6_.EMPLOYEE_ID "
                +         "and "
                +             "annualleav6_.STATE=?"
                +     ")"
                + ") "
                + "order by employee0_.FIRST_NAME asc", 
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
                +     "department: @UnloadedReference, "
                +     "annualLeaves: @UnloadedCollection "
                +   "} "
                + "]", 
                getEmployeesString(employees)
        );
    }
    
    @Test
    public void testImplictConditionCausedByFetchPathWithInnerJoin() {
        /*
         * FetchPath supports inner join by 
         *      GetterType.REQUIRED,
         * that can cause some implicit conditions to filter the data.
         */
        
        /* 
         * In this demo, totally, there are 12 employees
         */
        
        /* 
         * (1) Two employees does not have supervisor: Jim Raynor and Tassadar.
         * So the query with FetchPath 
         *      Employee__.begin().supervisor(GetterType.REQUIRED).end()
         * only returns 10(12 - 2) employees
         */
        Assert.assertEquals(
                10, 
                this.employeeRepository.getEmployees(
                        null, 
                        Employee__.begin().supervisor(GetterType.REQUIRED).end()
                ).size()
        );
        
        /* 
         * (2) Seven employees does not have subordinates: 
         *      Artanis, Gabriel Tosh, Karass, Monhandar, Matt Horner, Tychus Findlay, Urun.
         * So the query with FetchPath
         *      Employee__.begin().subordinates(GetterType.REQUIRED).end()
         * only returns 5(12 - 7) employees
         */
        Assert.assertEquals(
                5, 
                this.employeeRepository.getEmployees(
                        null, 
                        Employee__.begin().subordinates(GetterType.REQUIRED).end()
                ).size()
        );
        
        /*
         * (3) One employee does not have annualLeaves: Karass
         * So the query with FetchPath
         *      Employee__.begin().annualLeaves(GetterType.REQUIRED).end()
         * only returns 11(12 - 1) employees
         */
        Assert.assertEquals(
                11, 
                this.employeeRepository.getEmployees(
                        null, 
                        Employee__.begin().annualLeaves(GetterType.REQUIRED).end()
                ).size()
        );
    }
}
