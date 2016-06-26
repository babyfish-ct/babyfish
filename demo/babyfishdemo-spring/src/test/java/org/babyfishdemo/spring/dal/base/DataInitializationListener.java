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
package org.babyfishdemo.spring.dal.base;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.babyfish.collection.MACollections;
import org.babyfishdemo.spring.dal.DepartmentRepository;
import org.babyfishdemo.spring.dal.EmployeeRepository;
import org.babyfishdemo.spring.entities.AnnualLeave;
import org.babyfishdemo.spring.entities.Department;
import org.babyfishdemo.spring.entities.Employee;
import org.babyfishdemo.spring.entities.Name;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Tao Chen
 */
public class DataInitializationListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        final ApplicationContext ctx = testContext.getApplicationContext();
        new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class)).execute(
                new TransactionCallback<Void>() {
                    @Override
                    public Void doInTransaction(TransactionStatus ts) {
                        setupData(ctx);
                        return null;
                    }
                }
        );
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        final ApplicationContext ctx = testContext.getApplicationContext();
        new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class)).execute(
                new TransactionCallback<Void>() {
                    @Override
                    public Void doInTransaction(TransactionStatus ts) {
                        teardownData(ctx);
                        return null;
                    }
                }
        );
    }
    
    private static void setupData(ApplicationContext ctx) {
        
        DepartmentRepository departmentRepository = ctx.getBean(DepartmentRepository.class);
        EmployeeRepository employeeRepository = ctx.getBean(EmployeeRepository.class);
        
        Department barracks = createDepartment(
                "Barracks", 
                new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
        );
        Department ghostAcademy = createDepartment(
                "Ghost Academy", 
                new byte[] { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 }
        );
        Department starPort = createDepartment(
                "Star Port", 
                new byte[] { 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 }
        );
        Department templarArchive = createDepartment(
                "Templar Archive", 
                new byte[] { 4, 4, 4, 4, 4, 4, 4, 4, 4, 4 }
        );
        Department darkShrine = createDepartment(
                "Dark Shrine", 
                new byte[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }
        );
        Department starGate = createDepartment(
                "Star Gate", 
                new byte[] { 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 }
        );
        Department fleetBeacon = createDepartment(
                "Fleet Beacon", 
                new byte[] { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 }
        );
        
        Employee jimRaynor = createEmployee(
                "Jim", 
                "Raynor", 
                Employee.Gender.MALE, 
                createDate("1981-01-05"),
                "The description for Jim Raynor",
                new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
                barracks,
                null,
                createAnnualLeave(
                        createDate("2015-08-21 14:00"), 
                        createDate("2015-08-21 18:00"), 
                        AnnualLeave.State.APPROVED
                ),
                createAnnualLeave(
                        createDate("2015-10-13 09:00"), 
                        createDate("2015-11-20 18:00"), 
                        AnnualLeave.State.REJECTED
                )
        );
        Employee novaTerra = createEmployee(
                "Nova", 
                "Terra", 
                Employee.Gender.FEMALE, 
                createDate("1993-05-04"), 
                "The description for Nova Terra",
                new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0 },
                ghostAcademy,
                jimRaynor,
                createAnnualLeave(
                        createDate("2015-07-01 09:00"), 
                        createDate("2015-07-04 18:00"), 
                        AnnualLeave.State.APPROVED
                ),
                createAnnualLeave(
                        createDate("2015-07-10 09:00"), 
                        createDate("2015-07-12 18:00"),
                        AnnualLeave.State.APPROVED
                ),
                createAnnualLeave(
                        createDate("2015-09-15 14:00"), 
                        createDate("2015-09-15 18:00"), 
                        AnnualLeave.State.PENDING
                )
        );
        Employee gabrielTosh = createEmployee(
                "Gabriel",
                "Tosh",
                Employee.Gender.MALE,
                createDate("1980-09-24"),
                "The description for Gabriel Tosh",
                new byte[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0, 1 },
                ghostAcademy,
                novaTerra,
                createAnnualLeave(
                        createDate("2015-03-21 09:00"), 
                        createDate("2015-03-21 14:00"), 
                        AnnualLeave.State.APPROVED
                ),
                createAnnualLeave(
                        createDate("2015-04-23 09:00"), 
                        createDate("2015-05-23 18:00"), 
                        AnnualLeave.State.REJECTED
                )
        );
        Employee tychusFindlay = createEmployee(
                "Tychus",
                "Findlay",
                Employee.Gender.MALE,
                createDate("1983-04-25"),
                "The description for Tychus Findlay",
                new byte[] { 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2 },
                barracks,
                jimRaynor,
                createAnnualLeave(
                        createDate("2015-04-07 09:00"), 
                        createDate("2015-04-07 18:00"), 
                        AnnualLeave.State.APPROVED
                ),
                createAnnualLeave(
                        createDate("2015-10-18 09:00"), 
                        createDate("2015-10-20 18:00"), 
                        AnnualLeave.State.PENDING
                )
        );
        Employee mattHorner = createEmployee(
                "Matt", 
                "Horner", 
                Employee.Gender.MALE, 
                createDate("1990-07-14"),
                "The description for Matt Horner",
                new byte[] { 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3 },
                starPort,
                jimRaynor,
                createAnnualLeave(
                        createDate("2015-10-31 09:00"), 
                        createDate("2015-10-31 18:00"), 
                        AnnualLeave.State.PENDING
                ),
                createAnnualLeave(
                        createDate("2015-11-15 09:00"), 
                        createDate("2015-11-15 11:00"), 
                        AnnualLeave.State.PENDING
                )
        );
        Employee tassadar = createEmployee(
                "Tassadar", 
                null, 
                Employee.Gender.MALE, 
                createDate("756-08-24"),
                "The description for Tassadar",
                new byte[] { 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4 },
                templarArchive, 
                null,
                createAnnualLeave(
                        createDate("2015-04-21 09:00"), 
                        createDate("2015-04-24 18:00"), 
                        AnnualLeave.State.APPROVED
                ),
                createAnnualLeave(
                        createDate("2015-09-5 19:00"), 
                        createDate("2015-09-6 18:00"), 
                        AnnualLeave.State.PENDING
                )
        );
        Employee zeratul = createEmployee(
                "Zeratul", 
                null, 
                Employee.Gender.MALE, 
                createDate("976-11-13"),
                "The description for Zeratul",
                new byte[] { 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4, 5 },
                darkShrine, 
                tassadar,
                createAnnualLeave(
                        createDate("2015-01-19 09:00"), 
                        createDate("2015-01-19 18:00"), 
                        AnnualLeave.State.APPROVED
                ),
                createAnnualLeave(
                        createDate("2015-03-13 09:00"), 
                        createDate("2015-03-13 18:00"), 
                        AnnualLeave.State.APPROVED
                )
        );
        Employee artanis = createEmployee(
                "Artanis", 
                null, 
                Employee.Gender.MALE, 
                createDate("893-04-29"),
                "The description for Artanis",
                new byte[] { 7, 8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6 },
                fleetBeacon, 
                tassadar,
                createAnnualLeave(
                        createDate("2015-02-17 09:00"), 
                        createDate("2015-03-23 18:00"), 
                        AnnualLeave.State.REJECTED
                ),
                createAnnualLeave(
                        createDate("2015-02-17 09:00"), 
                        createDate("2015-02-30 18:00"), 
                        AnnualLeave.State.APPROVED
                ),
                createAnnualLeave(
                        createDate("2015-03-20 09:00"), 
                        createDate("2015-03-23 18:00"), 
                        AnnualLeave.State.APPROVED
                )
        );
        Employee selendis = createEmployee(
                "Selendis",
                null,
                Employee.Gender.FEMALE,
                createDate("1003-12-11"),
                "The description for Selendis",
                new byte[] { 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
                fleetBeacon,
                zeratul,
                createAnnualLeave(
                        createDate("2015-08-22 09:00"), 
                        createDate("2015-08-23 18:00"), 
                        AnnualLeave.State.APPROVED
                )
        );
        Employee mohandar = createEmployee(
                "Mohandar",
                null,
                Employee.Gender.MALE,
                createDate("1423-02-17"),
                "The description for Mohandar",
                new byte[] { 8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7 },
                starGate,
                selendis,
                createAnnualLeave(
                        createDate("2015-01-7 09:00"), 
                        createDate("2015-02-28 18:00"), 
                        AnnualLeave.State.REJECTED
                ),
                createAnnualLeave(
                        createDate("2015-01-7 09:00"), 
                        createDate("2015-02-20 18:00"), 
                        AnnualLeave.State.REJECTED
                ),
                createAnnualLeave(
                        createDate("2015-01-7 09:00"), 
                        createDate("2015-02-7 18:00"), 
                        AnnualLeave.State.APPROVED
                )
        );
        Employee urun = createEmployee(
                "Urun", 
                null, 
                Employee.Gender.MALE, 
                createDate("1490-08-25"),
                "The description for Urun",
                new byte[] { 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7, 8 },
                starGate, 
                selendis,
                createAnnualLeave(
                        createDate("2015-04-28 09:00"), 
                        createDate("2015-04-29 18:00"), 
                        AnnualLeave.State.APPROVED
                ),
                createAnnualLeave(
                        createDate("2015-08-21 09:00"), 
                        createDate("2015-08-21 18:00"), 
                        AnnualLeave.State.APPROVED
                )
        );
        Employee karass = createEmployee(
                "Karass", 
                null, 
                Employee.Gender.MALE, 
                createDate("1245-03-27"),
                "The description for Karass",
                new byte[] { 11, 12, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
                templarArchive,
                zeratul
        );
        
        departmentRepository.mergeDepartment(barracks);
        departmentRepository.mergeDepartment(ghostAcademy);
        departmentRepository.mergeDepartment(starPort);
        departmentRepository.mergeDepartment(templarArchive);
        departmentRepository.mergeDepartment(darkShrine);
        departmentRepository.mergeDepartment(starGate);
        departmentRepository.mergeDepartment(fleetBeacon);
        
        employeeRepository.mergeEmployee(jimRaynor);
        employeeRepository.mergeEmployee(novaTerra);
        employeeRepository.mergeEmployee(gabrielTosh);
        employeeRepository.mergeEmployee(tychusFindlay);
        employeeRepository.mergeEmployee(mattHorner);
        employeeRepository.mergeEmployee(tassadar);
        employeeRepository.mergeEmployee(zeratul);
        employeeRepository.mergeEmployee(artanis);
        employeeRepository.mergeEmployee(selendis);
        employeeRepository.mergeEmployee(mohandar);
        employeeRepository.mergeEmployee(urun);
        employeeRepository.mergeEmployee(karass);
    }
    
    private static void teardownData(ApplicationContext ctx) {
        
        DepartmentRepository departmentRepository = ctx.getBean(DepartmentRepository.class);
        EmployeeRepository employeeRepository = ctx.getBean(EmployeeRepository.class);
        
        /*
         * This demo uses database-level cascade deleting, not only the jpa-level cascade deleting
         * so the table ANNUAL_LEAVE will be clean automatically
         */
        employeeRepository.deleteAllEmployees();
        
        departmentRepository.deleteAllDepartments();
    }
    
    private static Department createDepartment(String name, byte[] image) {
        Department department = new Department();
        department.setName(name);
        department.setImage(image);
        return department;
    }
    
    private static Employee createEmployee(
            String firstName,
            String lastName,
            Employee.Gender gender,
            Date birthday,
            String description,
            byte[] image,
            Department department,
            Employee supervisor,
            AnnualLeave ... annualLeaves) {
        Employee employee = new Employee();
        employee.setName(new Name(firstName, lastName));
        employee.setGender(gender);
        employee.setBirthday(birthday);
        employee.setDescription(description);
        employee.setImage(image);
        employee.setDepartment(department);
        employee.setSupervisor(supervisor);
        
        /*
         * Because of ObjectModel4JPA, "employee.getAnnualLeaves.addAll(?)" will call
         * "annualLeave.setEmployee(?)" of each annualLeaves automatically!
         * So, don't worry, the non-inverse property "AnnualLeave.employee" will be changed.
         */
        employee.getAnnualLeaves().addAll(MACollections.wrap(annualLeaves));
        return employee;
    }
    
    private static AnnualLeave createAnnualLeave(
            Date startTime, 
            Date endTime, 
            AnnualLeave.State state) {
        AnnualLeave annualLeave = new AnnualLeave();
        annualLeave.setStartTime(startTime);
        annualLeave.setEndTime(endTime);
        annualLeave.setState(state);
        return annualLeave;
    }
    
    private static Date createDate(String dateText) {
        try {
            if (dateText.indexOf(' ') != -1) {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dateText);
            }
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateText);
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
