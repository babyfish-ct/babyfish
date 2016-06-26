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
package org.babyfishdemo.pagingquery.base;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.Assert;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.lang.Strings;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfishdemo.pagingquery.entities.Company;
import org.babyfishdemo.pagingquery.entities.Department;
import org.babyfishdemo.pagingquery.entities.Employee;
import org.babyfishdemo.pagingquery.jdbc.SqlAwareOracleDriver;
import org.babyfishdemo.pagingquery.jdbc.SqlRecorder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

/**
 * @author Tao Chen
 */
public abstract class AbstractTest {
    
    private static final Pattern COMMA_PATTERN = 
            Pattern.compile("\\s*,\\s*");
    
    private static final Pattern SIMPLE_COLUMN_PATTERN = 
            Pattern.compile("\\w+\\.\\w+");

    private static final Map<String, String> ORACLE_PROPERTY_MAP;
    
    private SqlRecorder sqlRecorder;
    
    protected static XEntityManagerFactory entityManagerFactory;
    
    protected List<String> preparedSqlList;
    
    protected static void initEntityManagerFactory(boolean oracle) {
        entityManagerFactory =
                new HibernatePersistenceProvider("persistence.xml")
                .createEntityManagerFactory(
                        null, 
                        oracle ? ORACLE_PROPERTY_MAP : null
                );
        insertRows();
    }
    
    @AfterClass
    public static void disposeEntityManagerFactory() {
        EntityManagerFactory emf = entityManagerFactory;
        if (emf != null) {
            try {
                clearRows();
            } finally {
                entityManagerFactory = null;
                emf.close();
            }
        }
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
    
    protected static void assertDepartment(Department department, String departmentName, String ... employeeNames) {
        Assert.assertEquals(departmentName, department.getName());
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(department.getEmployees()));
        Assert.assertEquals(employeeNames.length, department.getEmployees().size());
        int index = 0;
        for (Employee employee : department.getEmployees()) {
            Assert.assertEquals(employeeNames[index++], employee.getName());
        }
    }
    
    private static void insertRows() {
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            
            em.getTransaction().begin();
            try {
                
                clearRowsInExistingTransaction(em);
                
                Company terran = new Company();
                terran.setId(1L);
                terran.setName("Terran");
                
                Company protoss = new Company();
                protoss.setId(2L);
                protoss.setName("Protoss");
                
                Department barracks = new Department();
                barracks.setId(1L);
                barracks.setName("Barracks");
                barracks.setCompany(terran);
                
                Department ghostAcademy = new Department();
                ghostAcademy.setId(2L);
                ghostAcademy.setName("Ghost Academy");
                ghostAcademy.setCompany(terran);
                
                Department starPort = new Department();
                starPort.setId(3L);
                starPort.setName("Star Port");
                starPort.setCompany(terran);
                
                Department templarArchives = new Department();
                templarArchives.setId(4L);
                templarArchives.setName("Templar Archives");
                templarArchives.setCompany(protoss);
                
                Department darkShrine = new Department();
                darkShrine.setId(5L);
                darkShrine.setName("Dark Shrine");
                darkShrine.setCompany(protoss);
                
                Department starGate = new Department();
                starGate.setId(6L);
                starGate.setName("Star Gate");
                starGate.setCompany(protoss);
                
                Department fleetBeacon = new Department();
                fleetBeacon.setId(7L);
                fleetBeacon.setName("Fleet Beacon");
                fleetBeacon.setCompany(protoss);
                
                Employee jimRaynor = new Employee();
                jimRaynor.setId(1L);
                jimRaynor.setName("Jim Raynor");
                jimRaynor.setJobTitle("Marine");
                jimRaynor.setDepartment(barracks);
            
                Employee novaTerra = new Employee();
                novaTerra.setId(2L);
                novaTerra.setName("Nova Terra");
                novaTerra.setJobTitle("Ghost");
                novaTerra.setDepartment(ghostAcademy);
                novaTerra.setSupervisor(jimRaynor);
                
                Employee gabrielTosh = new Employee();
                gabrielTosh.setId(3L);
                gabrielTosh.setName("Gabriel Tosh");
                gabrielTosh.setJobTitle("Ghost");
                gabrielTosh.setDepartment(ghostAcademy);
                gabrielTosh.setSupervisor(novaTerra);
                
                Employee tychusFindlay = new Employee();
                tychusFindlay.setId(4L);
                tychusFindlay.setName("Tychus Findlay");
                tychusFindlay.setJobTitle("Marine");
                tychusFindlay.setDepartment(barracks);
                tychusFindlay.setSupervisor(jimRaynor);
                
                Employee mattHorner = new Employee();
                mattHorner.setId(5L);
                mattHorner.setName("Matt Horner");
                mattHorner.setJobTitle("Battlecruiser");
                mattHorner.setDepartment(starPort);
                mattHorner.setSupervisor(jimRaynor);
                
                Employee tassadar = new Employee();
                tassadar.setId(6L);
                tassadar.setName("Tassadar");
                tassadar.setJobTitle("High Templar");
                tassadar.setDepartment(templarArchives);
                
                Employee zeratul = new Employee();
                zeratul.setId(7L);
                zeratul.setName("Zeratul");
                zeratul.setJobTitle("Dark Templar");
                zeratul.setDepartment(darkShrine);
                zeratul.setSupervisor(tassadar);
                
                Employee artanis = new Employee();
                artanis.setId(8L);
                artanis.setName("Artanis");
                artanis.setJobTitle("Mothership");
                artanis.setDepartment(fleetBeacon);
                artanis.setSupervisor(tassadar);
                
                Employee selendis = new Employee();
                selendis.setId(11L);
                selendis.setName("Selendis");
                selendis.setJobTitle("Carrier");
                selendis.setDepartment(fleetBeacon);
                selendis.setSupervisor(zeratul);
                
                Employee mohandar = new Employee();
                mohandar.setId(9L);
                mohandar.setName("Mohandar");
                mohandar.setJobTitle("Void Ray");
                mohandar.setDepartment(starGate);
                mohandar.setSupervisor(selendis);
                
                Employee urun = new Employee();
                urun.setId(10L);
                urun.setName("Urun");
                urun.setJobTitle("Phoenix");
                urun.setDepartment(starGate);
                urun.setSupervisor(selendis);
                
                Employee karass = new Employee();
                karass.setId(12L);
                karass.setName("Karass");
                karass.setJobTitle("High Templar");
                karass.setDepartment(templarArchives);
                karass.setSupervisor(zeratul);
                
                em.persist(terran);
                em.persist(protoss);
                
                em.persist(barracks);
                em.persist(ghostAcademy);
                em.persist(starPort);
                em.persist(templarArchives);
                em.persist(darkShrine);
                em.persist(starGate);
                em.persist(fleetBeacon);
                
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
    
    private static void clearRows() {
        try (XEntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            try {
                clearRowsInExistingTransaction(em);
            } catch (RuntimeException | Error ex) {
                em.getTransaction().rollback();
                throw ex;
            }
            em.getTransaction().commit();
        }
    }
    
    private static void clearRowsInExistingTransaction(EntityManager em) {
        em.createQuery("delete from Employee").executeUpdate();
        em.createQuery("delete from Department").executeUpdate();
        em.createQuery("delete from Company").executeUpdate();
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
    
    static {
        Map<String, String> map = new HashMap<>();
        String oracle = System.getProperty("oracle");
        if (oracle != null) {
            // For eclipse run/debug configuration, "-Doracle" means 
            // empty string, but for shell, it means "-Doracle=true"
            if (!oracle.startsWith("jdbc:oracle:")) { 
                oracle = "jdbc:oracle:thin:@localhost:1521:babyfish";
            }
            map.put(
                    "hibernate.dialect", 
                    // babfish's dialect, not hibernate's dialect
                    "org.babyfish.hibernate.dialect.Oracle10gDialect"
            );
            map.put(
                    "hibernate.connection.driver_class", 
                    SqlAwareOracleDriver.class.getName()
            );
            map.put(
                    "hibernate.connection.url", 
                    oracle
            );
            map.put(
                    "hibernate.connection.username", 
                    System.getProperty("oracle.user", "babyfish_demo")
            );
            map.put(
                    "hibernate.connection.password", 
                    System.getProperty("oracle.password", "123")
            );
        }
        ORACLE_PROPERTY_MAP = MACollections.unmodifiable(map);
    }
}
