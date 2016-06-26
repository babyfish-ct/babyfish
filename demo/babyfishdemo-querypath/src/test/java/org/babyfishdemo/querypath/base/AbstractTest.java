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
package org.babyfishdemo.querypath.base;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.Persistence;

import junit.framework.Assert;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Strings;
import org.babyfish.persistence.XEntityManager;
import org.babyfishdemo.querypath.JPAContext;
import org.babyfishdemo.querypath.entities.AnnualLeave;
import org.babyfishdemo.querypath.entities.AnnualLeaveState;
import org.babyfishdemo.querypath.entities.Company;
import org.babyfishdemo.querypath.entities.Cost;
import org.babyfishdemo.querypath.entities.Department;
import org.babyfishdemo.querypath.entities.Employee;
import org.babyfishdemo.querypath.jdbc.SqlRecorder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author Tao Chen
 */
public abstract class AbstractTest {
    
    private static final Pattern COMMA_PATTERN = 
            Pattern.compile("\\s*,\\s*");
    
    private static final Pattern SIMPLE_COLUMN_PATTERN = 
            Pattern.compile("\\w+\\.\\w+");
    
    // DateFormat is not thread-safe, fortunately, this class is unit test, it is executed in single thread.
    // (I don't know why the formatter is not designed to be thread-safe)
    private static final DateFormat LONG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    protected List<String> preparedSqlList;
    
    private SqlRecorder sqlRecorder;
    
    @BeforeClass
    public static void setUpClass() {
        
        /*
         * 1) Structure of Company, Department and Employee
         * 
         *  +---+-Terran
         *  |   |
         *  |   +---+-Barracks
         *  |   |   |
         *  |   |   +-----Jim Raynor
         *  |   |   |
         *  |   |   \-----Tychus Findlay
         *  |   |
         *  |   +---+-Ghost Acedemy
         *  |   |   |
         *  |   |   +-----Nova Terra
         *  |   |   |
         *  |   |   \-----Gabriel Tosh
         *  |   |
         *  |   \---+-Star Port
         *  |       |
         *  |       \-----Matt Horner
         *  |
         *  +---+-Protoss
         *  |   |
         *  |   +---+-Templar Archives
         *  |   |   |
         *  |   |   +-----Tassadar
         *  |   |   |
         *  |   |   \-----Karass
         *  |   |
         *  |   +---+-Dark Shrine
         *  |   |   |
         *  |   |   \-----Zeratul
         *  |   |
         *  |   +---+-Star Gate
         *  |   |   |
         *  |   |   +-----Mohandar
         *  |   |   |
         *  |   |   \-----Urun
         *  |   |
         *  |   \---+-Fleet Beacon
         *  |       |
         *  |       +-----Selendis
         *  |       |
         *  |       +-----Artanis
         *  |       
         *  \---+-<<NULL>>
         *      |
         *      \-----Xel'Naga Temple
         *         
         * 2) Structure of Employee tree
         * 
         *  +---+-Jim Raynor
         *  |   |
         *  |   \---+-Matt Horner
         *  |       |
         *  |       +---+-Nova Terra
         *  |       |   |
         *  |       |   \-----Gabriel Tosh
         *  |       |
         *  |       \-----Tychus Findlay
         *  |
         *  +---+-Tassadar
         *      |
         *      +---+-Zeratul
         *      |   |
         *      |   \---+-Artanis
         *      |       |
         *      |       \---+-Selendis
         *      |           |
         *      |           +-----Mohandar
         *      |           |
         *      |           \-----Urun
         *      |
         *      \-----Karass
         */
        Company terran = new Company();
        terran.setName("Terran");
        
        Company protoss = new Company();
        protoss.setName("Protoss");
        
        Department barracks = new Department();
        barracks.setName("Barracks");
        barracks.setCompany(terran);
        
        Department ghostAcademy = new Department();
        ghostAcademy.setName("Ghost Academy");
        ghostAcademy.setCompany(terran);
        
        Department starPort = new Department();
        starPort.setName("Star Port");
        starPort.setCompany(terran);
        
        Department templarArchives = new Department();
        templarArchives.setName("Templar Archives");
        templarArchives.setDescription(Lobs.TEMPLAR_ARCHIVES_DESCRIPTION);
        templarArchives.setImage(MACollections.toByteArray(Lobs.TEMPLAR_ARCHIVES_IMAGE));
        templarArchives.setCompany(protoss);
        
        Department darkShrine = new Department();
        darkShrine.setName("Dark Shrine");
        darkShrine.setCompany(protoss);
        
        Department starGate = new Department();
        starGate.setName("Star Gate");
        starGate.setCompany(protoss);
        
        Department fleetBeacon = new Department();
        fleetBeacon.setName("Fleet Beacon");
        fleetBeacon.setCompany(protoss);
        
        Department xelNagaTemple = new Department();
        xelNagaTemple.setName("Xel'Naga Temple");
        
        Employee jimRaynor = new Employee();
        jimRaynor.setName("Jim Raynor");
        jimRaynor.setCost(new Cost(50, 0));
        jimRaynor.setDepartment(barracks);
        jimRaynor.getAnnualLeaves().add(annualLevel(AnnualLeaveState.APPROVED, "2015-02-08", "2015-02-10"));
        jimRaynor.getAnnualLeaves().add(annualLevel(AnnualLeaveState.PENDING, "2015-03-17", "2015-03-17"));
    
        Employee novaTerra = new Employee();
        novaTerra.setName("Nova Terra");
        novaTerra.setCost(new Cost(200, 100));
        novaTerra.setDepartment(ghostAcademy);
        novaTerra.getAnnualLeaves().add(annualLevel(AnnualLeaveState.APPROVED, "2015-01-23", "2015-01-23"));
        novaTerra.getAnnualLeaves().add(annualLevel(AnnualLeaveState.REJECTED, "2015-04-24", "2015-05-08"));
        
        Employee gabrielTosh = new Employee();
        gabrielTosh.setName("Gabriel Tosh");
        gabrielTosh.setCost(new Cost(200, 100));
        gabrielTosh.setDepartment(ghostAcademy);
        gabrielTosh.getAnnualLeaves().add(annualLevel(AnnualLeaveState.APPROVED, "2015-01-16", "2015-01-21"));
        gabrielTosh.getAnnualLeaves().add(annualLevel(AnnualLeaveState.PENDING, "2015-03-19", "2015-03-20"));
        
        Employee tychusFindlay = new Employee();
        tychusFindlay.setName("Tychus Findlay");
        tychusFindlay.setCost(new Cost(50, 0));
        tychusFindlay.setDepartment(barracks);
        // tychusFindlay does not has annual leave records 
        // so that it can be used to test the difference of inner join and outer join
        
        Employee mattHorner = new Employee();
        mattHorner.setName("Matt Horner");
        mattHorner.setCost(new Cost(400, 300));
        mattHorner.setDepartment(starPort);
        mattHorner.getAnnualLeaves().add(annualLevel(AnnualLeaveState.REJECTED, "2015-03-15", "2015-04-03"));
        mattHorner.getAnnualLeaves().add(annualLevel(AnnualLeaveState.PENDING, "2015-04-23", "2015-04-25"));
        
        Employee tassadar = new Employee();
        tassadar.setName("Tassadar");
        tassadar.setCost(new Cost(50, 150));
        tassadar.setResume(Lobs.TASSADAR_RESUME);
        tassadar.setPhoto(MACollections.toByteArray(Lobs.TASSADAR_PHOTO));
        tassadar.setDepartment(templarArchives);
        tassadar.getAnnualLeaves().add(annualLevel(AnnualLeaveState.APPROVED, "2015-01-10", "2015-01-10"));
        tassadar.getAnnualLeaves().add(annualLevel(AnnualLeaveState.APPROVED, "2015-02-26", "2015-02-27"));
        
        Employee zeratul = new Employee();
        zeratul.setName("Zeratul");
        zeratul.setCost(new Cost(125, 125));
        zeratul.setResume(Lobs.ZERATUL_RESUME);
        zeratul.setPhoto(MACollections.toByteArray(Lobs.ZERATUL_PHOTO));
        zeratul.setDepartment(darkShrine);
        zeratul.getAnnualLeaves().add(annualLevel(AnnualLeaveState.APPROVED, "2015-03-04", "2015-03-04"));
        zeratul.getAnnualLeaves().add(annualLevel(AnnualLeaveState.PENDING, "2015-05-17", "2015-05-19"));
        
        Employee artanis = new Employee();
        artanis.setName("Artanis");
        artanis.setCost(new Cost(400, 400));
        artanis.setResume(Lobs.ARTANIS_RESUME);
        artanis.setPhoto(MACollections.toByteArray(Lobs.ARTANIS_PHOTO));
        artanis.setDepartment(fleetBeacon);
        artanis.getAnnualLeaves().add(annualLevel(AnnualLeaveState.REJECTED, "2015-02-18", "2015-03-09"));
        artanis.getAnnualLeaves().add(annualLevel(AnnualLeaveState.PENDING, "2015-07-03", "2015-07-14"));
        
        Employee mohandar = new Employee();
        mohandar.setName("Mohandar");
        mohandar.setCost(new Cost(250, 150));
        mohandar.setResume(Lobs.MOHANDAR_RESUME);
        mohandar.setPhoto(MACollections.toByteArray(Lobs.MOHANDAR_PHOTO));
        mohandar.setDepartment(starGate);
        mohandar.getAnnualLeaves().add(annualLevel(AnnualLeaveState.APPROVED, "2015-02-28", "2015-03-02"));
        mohandar.getAnnualLeaves().add(annualLevel(AnnualLeaveState.APPROVED, "2015-05-25", "2015-05-26"));
        
        Employee urun = new Employee();
        urun.setName("Urun");
        urun.setCost(new Cost(150, 100));
        urun.setResume(Lobs.URUN_RESUME);
        urun.setPhoto(MACollections.toByteArray(Lobs.URUN_PHOTO));
        urun.setDepartment(starGate);
        urun.getAnnualLeaves().add(annualLevel(AnnualLeaveState.APPROVED, "2015-03-09", "2015-03-14"));
        urun.getAnnualLeaves().add(annualLevel(AnnualLeaveState.PENDING, "2015-04-19", "2015-04-25"));
        
        Employee selendis = new Employee();
        selendis.setName("Selendis");
        selendis.setCost(new Cost(350, 250));
        selendis.setResume(Lobs.SELENDIS_RESUME);
        selendis.setPhoto(MACollections.toByteArray(Lobs.SELENDIS_PHOTO));
        selendis.setDepartment(fleetBeacon);
        selendis.getAnnualLeaves().add(annualLevel(AnnualLeaveState.APPROVED, "2015-01-05", "2015-01-05"));
        selendis.getAnnualLeaves().add(annualLevel(AnnualLeaveState.APPROVED, "2015-02-25", "2015-02-28"));
        
        Employee karass = new Employee();
        karass.setName("Karass");
        karass.setCost(new Cost(50, 150));
        karass.setResume(Lobs.KARASS_RESUME);
        karass.setPhoto(MACollections.toByteArray(Lobs.KARASS_PHOTO));
        karass.setDepartment(templarArchives);
        karass.getAnnualLeaves().add(annualLevel(AnnualLeaveState.REJECTED, "2015-02-19", "2015-02-26"));
        karass.getAnnualLeaves().add(annualLevel(AnnualLeaveState.APPROVED, "2015-03-10", "2015-03-13"));
        
        gabrielTosh.setSupervisor(novaTerra);
        tychusFindlay.setSupervisor(mattHorner);
        novaTerra.setSupervisor(mattHorner);
        mattHorner.setSupervisor(jimRaynor);
        
        urun.setSupervisor(selendis);
        mohandar.setSupervisor(selendis);
        selendis.setSupervisor(artanis);
        artanis.setSupervisor(zeratul);
        zeratul.setSupervisor(tassadar);
        karass.setSupervisor(tassadar);
        
        try (XEntityManager em = JPAContext.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(terran);
                em.persist(protoss);
                
                em.persist(barracks);
                em.persist(ghostAcademy);
                em.persist(starPort);
                em.persist(templarArchives);
                em.persist(darkShrine);
                em.persist(starGate);
                em.persist(fleetBeacon);
                em.persist(xelNagaTemple);
                
                em.persist(jimRaynor);
                em.persist(novaTerra);
                em.persist(gabrielTosh);
                em.persist(tychusFindlay);
                em.persist(mattHorner);
                em.persist(tassadar);
                em.persist(zeratul);
                em.persist(artanis);
                em.persist(mohandar);
                em.persist(urun);
                em.persist(selendis);
                em.persist(karass);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        JPAContext.stop();
    }
    
    @Before
    public void startSqlRecorder() {
        this.preparedSqlList = new ArrayList<>();
        this.sqlRecorder = new SqlRecorder() {
            @Override
            protected void prepareStatement(String sql) {
                
                // Adjust the SQL to avoid the difference of oracle and hsqldb
                sql = processSql(sql);
                
                AbstractTest.this.preparedSqlList.add(sql);
            }
        };
    }
    
    @After
    public void stopSqlRecorder() {
        this.sqlRecorder.close();
    }
    
    protected static Date date(String text) {
        try {
            if (text.indexOf(' ') != -1) {
                return LONG_DATE_FORMAT.parse(text);
            }
            return SHORT_DATE_FORMAT.parse(text);
        } catch (ParseException ex) {
            throw new IllegalArgumentException(
                    "The text \""
                    + text
                    + "\" can not be parsed to date",
                    ex);
        }
    }

    private static AnnualLeave annualLevel(AnnualLeaveState state, String startTime, String endTime) {
        AnnualLeave annualLeave = new AnnualLeave();
        annualLeave.setState(state);
        annualLeave.setStartTime(date(startTime));
        annualLeave.setEndTime(date(endTime));
        return annualLeave;
    }
    
    private static String processSql(String sql) {
        int selectIndex = indexOfSelect(sql);
        if (selectIndex != -1) {
            /*
             * Must ignore some character to ignore the
             * difference between hsqldb and oracle
             */
            int fromIndex = sql.indexOf(" from ");
            String columns = sql.substring(selectIndex + 7, fromIndex);
            if (columns.indexOf(',') == -1) {
                int asIndex = columns.indexOf(" as ");
                if (asIndex != -1) {
                    columns = columns.substring(0, asIndex);
                }
            } else {
                String[] arr = COMMA_PATTERN.split(columns);
                Set<String> tableNames = new HashSet<>();
                List<String> colNameList = new ArrayList<>();
                for (String col : arr) {
                    int asIndex = col.lastIndexOf(" as ");
                    if (asIndex != -1) {
                        col = col.substring(0, asIndex);
                    }
                    if (col.startsWith("this_.")) {   
                        colNameList.add(col);
                    } else if (SIMPLE_COLUMN_PATTERN.matcher(col).matches()) {
                        String tableName = col.substring(0, col.indexOf('.'));
                        if (tableNames.add(tableName)) {
                            colNameList.add(
                                    "<...many columns of "
                                    + tableName
                                    + "...>"
                            );
                        }
                    } else {
                        colNameList.add(col);
                    }
                }
                columns = Strings.join(colNameList);
            }
            sql = 
                    sql.substring(0, selectIndex)
                    + "select " 
                    + columns
                    + " from "
                    + processSql(sql.substring(fromIndex + 6));
        }
        return sql;
    }
    
    private static int indexOfSelect(String sql) {
        if (sql.startsWith("select ")) {
            return 0;
        }
        int index1 = sql.indexOf(" select ");
        if (index1 != -1) {
            index1++; 
        }
        int index2 = sql.indexOf("(select ");
        if (index2 != -1) {
            index2++;
        }
        if (index1 != -1 && index2 != -1) {
            return index1 < index2 ? index1 : index2;
        }
        return index1 != -1 ? index1 : index2;
    }
    
    protected static class EmployeeTreeNode {
        
        public static final EmployeeTreeNode[] UNLOADED_CHILDREN = new EmployeeTreeNode[0];
        
        private String name;
        
        private EmployeeTreeNode[] childNodes;

        public EmployeeTreeNode(String name, EmployeeTreeNode... childNodes) {
            this.name = name;
            this.childNodes = childNodes;
        }
        
        public void validate(Employee employee) {
            Assert.assertEquals(this.name, employee.getName());
            if (this.childNodes == UNLOADED_CHILDREN) {
                Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(employee.getSubordinates()));
            } else {
                Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(employee.getSubordinates()));
                Assert.assertEquals(this.childNodes.length, employee.getSubordinates().size());
                int index = 0;
                EmployeeTreeNode[] arr = this.childNodes;
                for (Employee childEmployee : employee.getSubordinates()) {
                    arr[index++].validate(childEmployee);
                }
            }
        }
    }
}
