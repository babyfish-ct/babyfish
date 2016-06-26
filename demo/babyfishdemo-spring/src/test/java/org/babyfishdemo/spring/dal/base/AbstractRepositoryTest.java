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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashSet;
import org.babyfish.hibernate.dialect.DistinctLimitDialect;
import org.babyfish.lang.Strings;
import org.babyfishdemo.spring.dal.DepartmentRepository;
import org.babyfishdemo.spring.dal.EmployeeRepository;
import org.babyfishdemo.spring.dal.jdbc.SqlRecorder;
import org.babyfishdemo.spring.entities.AnnualLeave;
import org.babyfishdemo.spring.entities.Department_;
import org.babyfishdemo.spring.entities.Employee;
import org.babyfishdemo.spring.entities.Employee_;
import org.hibernate.Hibernate;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 * @author Tao Chen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfiguration.class)
@TestExecutionListeners(value = { 
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DataInitializationListener.class
})
public abstract class AbstractRepositoryTest {
    
    private static final Pattern COMMA_PATTERN = 
            Pattern.compile("\\s*,\\s*");
    
    private static final Pattern SIMPLE_COLUMN_PATTERN = 
            Pattern.compile("\\w+\\.\\w+");
    
    @Resource
    protected DepartmentRepository departmentRepository;
    
    @Resource
    protected EmployeeRepository employeeRepository;
    
    @Resource
    private EntityManagerFactory entityManagerFactory;
    
    protected List<String> preparedSqlList;
    
    private SqlRecorder sqlRecorder;
    
    @Before
    public void startSqlRecorder() {
        this.preparedSqlList = new ArrayList<>();
        this.sqlRecorder = new SqlRecorder() {
            @Override
            protected void prepareStatement(String sql) {
                
                // Adjust the SQL to avoid the difference of oracle and hsqldb
                sql = processSql(sql);
                
                AbstractRepositoryTest.this.preparedSqlList.add(sql);
            }
        };
    }
    
    @After
    public void stopSqlRecorder() {
        this.sqlRecorder.close();
    }
    
    protected boolean isOracle() {
        Dialect dialect = 
                this
                .entityManagerFactory
                .unwrap(SessionFactoryImplementor.class)
                .getDialect();
        boolean oracle = dialect instanceof org.hibernate.dialect.Oracle8iDialect;
        if (oracle) {
            Assert.assertTrue(dialect instanceof DistinctLimitDialect);
        }
        return oracle;
    }
    
    protected static String getEmployeesString(Collection<Employee> employees) {
        return getEmployeesString(employees, false, false);
    }
    
    protected static String getEmployeesString(
            Collection<Employee> employees,
            boolean collectSupervisor,
            boolean collectSubordinates) {
        if (employees.isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[ ");
        boolean addComma = false;
        for (Employee employee : employees) {
            if (addComma) {
                builder.append(", ");
            } else {
                addComma = true;
            }
            collectString(employee, builder, collectSupervisor, collectSubordinates);
        }
        builder.append(" ]");
        return builder.toString();
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

    private static void collectString(
            Employee employee, 
            StringBuilder builder, 
            boolean collectSupervisor,
            boolean collectSubordinates) {
        
        DateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat longDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        builder
        .append("{ name: { firstName: ")
        .append(employee.getName().getFirstName())
        .append(", lastName: ")
        .append(employee.getName().getLastName())
        .append(" }");
        
        builder
        .append(", gender: ").append(employee.getGender())
        .append(", birthday: ").append(shortDateFormat.format(employee.getBirthday()));
        
        builder.append(", description: ");
        if (Persistence.getPersistenceUtil().isLoaded(employee, Employee_.description.getName())) {
            builder.append("clob(").append(employee.getDescription()).append(')');
        } else {
            builder.append("@UnloadedClob");
        }
        
        builder.append(", image: ");
        if (Persistence.getPersistenceUtil().isLoaded(employee, Employee_.image.getName())) {
            collectBlobString(employee.getImage(), builder);
        } else {
            builder.append("@UnloadedBlob");
        }
        
        builder.append(", department: ");
        /*
         * Here, we muse use A: 
         *      "Hibernate.isInitialized(employee.getDepartment())",
         * Don't use B: 
         *      "Persistence.getPeristenceUtil().isLoaded(employee.getDepartment)).
         * 
         * If the department itself is loaded but it contains some unloaded scalar(lob) fields,
         * A return true and B return false(I think B is a failure of JPA design).
         */
        if (!Hibernate.isInitialized(employee.getDepartment())) {
            builder.append("@UnloadedReference");
        } else if (employee.getDepartment() == null) {
            builder.append("null");
        } else {
            builder.append("{ name: ").append(employee.getDepartment().getName());
            builder.append(", image: ");
            if (Persistence.getPersistenceUtil().isLoaded(
                    employee.getDepartment(), 
                    Department_.image.getName())) {
                collectBlobString(employee.getDepartment().getImage(), builder);
            } else {
                builder.append("@UnloadedBlob");
            }
            builder.append(" }");
        }
        
        builder.append(", annualLeaves: ");
        if (!Persistence.getPersistenceUtil().isLoaded(employee.getAnnualLeaves())) {
            builder.append("@UnloadedCollection");
        } else if(employee.getAnnualLeaves().isEmpty()) {
            builder.append("[]");
        } else {
            builder.append("[ ");
            boolean addComma = false;
            for (AnnualLeave annualLeave : employee.getAnnualLeaves()) {
                if (addComma) {
                    builder.append(", ");
                } else {
                    addComma = true;
                }
                builder
                .append("{ startTime: ").append(longDateFormat.format(annualLeave.getStartTime()))
                .append(", endTime: ").append(longDateFormat.format(annualLeave.getEndTime()))
                .append(", state: ").append(annualLeave.getState())
                .append(" }");
            }
            builder.append(" ]");
        }
        
        if (collectSupervisor) {
            builder.append(", supervisor: ");
            /*
             * Here, we muse use A: 
             *      "Hibernate.isInitialized(employee.getSupervisor())",
             * Don't use B: 
             *      "Persistence.getPeristenceUtil().isLoaded(employee.getSupervisor()).
             * 
             * If the department itself is loaded but it contains some unloaded scalar(lob) fields,
             * A return true and B return false(I think B is a failure of JPA design).
             */
            if (!Hibernate.isInitialized(employee.getSupervisor())) {
                builder.append("@UnloadedReference");
            } else if (employee.getSupervisor() == null) {
                builder.append("null");
            } else {
                collectString(employee.getSupervisor(), builder, true, false);;
            }
        }
        
        if (collectSubordinates) {
            builder.append(", subordinates: ");
            if (!Persistence.getPersistenceUtil().isLoaded(employee.getSubordinates())) {
                builder.append("@UnloadedCollection");
            } else if (employee.getSubordinates().isEmpty()) {
                builder.append("[]");
            } else {
                builder.append("[ ");
                boolean addComma = false;
                for (Employee subordinate : employee.getSubordinates()) {
                    if (addComma) {
                        builder.append(", ");
                    } else {
                        addComma = true;
                    }
                    collectString(subordinate, builder, false, true);
                }
                builder.append(" ]");
            }
        }
        
        builder.append(" }");
    }
    
    private static void collectBlobString(byte[] blob, StringBuilder builder) {
        builder.append("blob(0x");
        for (byte b : blob) {
            Assert.assertTrue(b >= 0 && b < 16);
            builder.append('0');
            if (b < 10) {
                builder.append((char)('0' + b));
            } else {
                builder.append((char)('A' + b - 10));
            }
        }
        builder.append(')');
    }
}
