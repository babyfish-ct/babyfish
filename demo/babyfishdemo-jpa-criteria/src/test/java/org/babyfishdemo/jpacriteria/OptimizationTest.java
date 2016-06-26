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
package org.babyfishdemo.jpacriteria;

import java.util.Collections;

import javax.persistence.Tuple;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import junit.framework.Assert;

import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.model.jpa.JPAEntities;
import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfish.persistence.criteria.JoinMode;
import org.babyfish.persistence.criteria.LikeMode;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfishdemo.jpacriteria.base.AbstractTest;
import org.babyfishdemo.jpacriteria.entities.Department;
import org.babyfishdemo.jpacriteria.entities.Department_;
import org.babyfishdemo.jpacriteria.entities.Employee;
import org.babyfishdemo.jpacriteria.entities.Employee_;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class OptimizationTest extends AbstractTest {
    
    private static XEntityManagerFactory strictDbSchemaEntityManagerFactory ;
    
    @BeforeClass
    public static void initStrictDbSchemaEntityManagerFactory() {
        strictDbSchemaEntityManagerFactory =
                new HibernatePersistenceProvider()
                .createEntityManagerFactory(
                        null, 
                        Collections.singletonMap(
                                "babyfish.hibernate.strict_db_schema", 
                                "true"
                        )
                );
    }
    
    @AfterClass
    public static void disposeDbSchemaEntityManagerFactory() {
        XEntityManagerFactory semf = strictDbSchemaEntityManagerFactory;
        if (semf != null) {
            strictDbSchemaEntityManagerFactory = null;
            semf.close();
        }
    }
    
    @Test
    public void testReplaceGetIdPropertyOfLeftJoinReferenceToGet() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> employee = cq.from(Employee.class);
        cq
        .where(
                cb.le(
                        employee
                        .join(Employee_.department, JoinType.LEFT) // left join to parent
                        .get(Department_.id), // get id(primary key)
                        100L
                )
        )
        .select(employee);
        
        /*
         * In this example, first use LEFT join to get the parent table and check its id property.
         * that join is unnecessary, because the foreign key(id of parent object) is maintained 
         * by the child table itself.
         * 
         * "employee.join(Employee_.department, JoinType.LEFT).get(Department_.id)"
         * can be changed to
         * "employee.get(Employee_).get(Department_.id)"
         * which can access the parent object id by the foreign key of the child table itself
         * 
         * So, After optimization, in the generated JPQL, no joins
         */
        Assert.assertEquals(
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                
                // The join behavior is changed to be get behavior.
                // Hibernate will consider "babyfish_shared_alias_0.department.id" 
                // to be the foreign key of the child table itself.
                + "where babyfish_shared_alias_0.department.id <= :babyfish_literal_0", 
                
                createQueryTemplate(cq).toString()
        );
    }
    
    @Test
    public void testReplaceGetIdPropertyOfInnerJoinNonNullReferenceToGet() {
        
        /*
         * This test methods shows 2 cases, one can not optimize and the other one can
         */
        
        /*
         * Case 1, optimize failed.
         */
        {
            XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq
            .where(
                    cb.le(
                            employee
                            .join(Employee_.department, JoinType.INNER) // inner join to parent
                            .get(Department_.id), // get id(primary key)
                            100L
                    )
            )
            .select(employee);
            
            // Optimize failed, "inner join" still exists. 
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "inner join babyfish_shared_alias_0.department babyfish_shared_alias_1 "
                    
                    // The join behavior is changed to be get behavior.
                    // Hibernate will consider "babyfish_shared_alias_0.department.id" 
                    // to be the foreign key of the child table itself.
                    + "where babyfish_shared_alias_1.id <= :babyfish_literal_0", 
                    
                    createQueryTemplate(cq).toString()
            );
        }
        
        /*
         * Case 2, optimize successfully.
         * 
         * Two important prerequisites:
         * 
         * (1) In entity class Employee, the reference association "Employee.department"
         * is declared as non-null property by the "nullable = false" of @JoinColumn,
         * (2) "this.strictDbSchemaEntityManagerFactory" is created by additional
         * JPA property "babyfish.hibernate.strict_db_schema" is "true", That means
         * the user guarantee the database foreign keys for the non-null reference associations
         * are not null too absolutely.
         */
        {
            // Be careful, "strictDbSchemaEntityManagerFactory", not "entityManagerFactory"
            XCriteriaBuilder cb = strictDbSchemaEntityManagerFactory.getCriteriaBuilder();
            
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq
            .where(
                    cb.le(
                            employee
                            .join(Employee_.department, JoinType.INNER) // inner join to parent
                            .get(Department_.id), // get id(primary key)
                            100L
                    )
            )
            .select(employee);
            
            /*
             * Though this join is inner join, but the reference association "Employee.department" 
             * is non-null and the user guarantee the corresponding foreign key in database
             * is not null too, so no difference between inner join and left join for this association,
             * this join is unnecessary, because the foreign key(id of parent object) is maintained 
             * by the child table itself.
             * 
             * "employee.join(Employee_.department, JoinType.INNER).get(Department_.id)"
             * can be changed to
             * "employee.get(Employee_).get(Department_.id)"
             * which can access the parent object id by the foreign key of the child table itself
             * 
             * Finally, After optimization, in the generated JPQL, no joins
             */
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    
                    // The join behavior is changed to be get behavior.
                    // Hibernate will consider "babyfish_shared_alias_0.department.id" 
                    // to be the foreign key of the child table itself.
                    + "where babyfish_shared_alias_0.department.id <= :babyfish_literal_0", 
                    
                    createQueryTemplate(cq).toString()
            );
        }
    }
    
    @Test
    public void testReplaceLeftJoinReferenceToGetter() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        /*
         * Show the case that the left join on reference association can be replace to getter
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq
            .where(
                    cb.equal(
                            employee.join(Employee_.department, JoinType.LEFT), 
                            JPAEntities.createFakeEntity(Department.class, 1L)
                    )
            )
            .select(employee);
            
            /*
             * Becasue this join type is LEFT and "Employee.department" is reference association, not collection
             * 
             * "employee.join(Employee_.department, JoinType.LEFT)"
             * can be changed to 
             * "employee.get(Employee_.department)" 
             * which can access the parent object by the foreign key in the child table itself automatically.
             * 
             * Finally, after optimization, in the generated JPQL, no joins
             */
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where babyfish_shared_alias_0.department = :babyfish_literal_0",
                    createQueryTemplate(cq).toString()
            );
        }
        
        /*
         * But, if the left reference join is used in the selection of topmost query,
         * that fine-tuning optimization is disabled
         */
        {
            XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
            Root<Employee> employee = cq.from(Employee.class);
            Join<Employee, Department> department = employee.join(Employee_.department, JoinType.LEFT);
            cq.multiselect(department, employee);
            // babyfish thinks that join is necessary so it will not ignored
            Assert.assertEquals(
                    "select "
                    +   "babyfish_shared_alias_1, "
                    +   "babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "left join babyfish_shared_alias_0.department babyfish_shared_alias_1", 
                    createQueryTemplate(cq).toString()
            );
        }
    }
    
    @Test
    public void testReplaceInnerJoinNonNullReferenceToGetter() {
        
        /*
         * Case1, optimize failed
         */
        {
            XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
            
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq
            .where(
                    cb.equal(
                            employee.join(Employee_.department, JoinType.INNER), 
                            JPAEntities.createFakeEntity(Department.class, 50L)
                    )
            )
            .select(employee);
            
            /*
             * Though "Employee.department" is reference association, not collection,
             * but the join type is INNER, so babyfish can not optimize it and
             * join still exists in generated JPQL
             */
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "inner join babyfish_shared_alias_0.department babyfish_shared_alias_1 "
                    + "where babyfish_shared_alias_1 = :babyfish_literal_0",
                    createQueryTemplate(cq).toString()
            );
        }
        
        /*
         * Case2, optimize successfully
         * 
         * Two important prerequisites:
         * 
         * (1) In entity class Employee, the reference association "Employee.department"
         * is declared as non-null property by the "nullable = false" of @JoinColumn,
         * (2) "this.strictDbSchemaEntityManagerFactory" is created by additional
         * JPA property "babyfish.hibernate.strict_db_schema" is "true", That means
         * the user guarantee the database foreign keys for the non-null reference associations
         * are not null too absolutely.
         */
        {
            // Be careful, "strictDbSchemaEntityManagerFactory", not "entityManagerFactory"
            XCriteriaBuilder cb = strictDbSchemaEntityManagerFactory.getCriteriaBuilder();
            
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq
            .where(
                    cb.equal(
                            employee.join(Employee_.department, JoinType.INNER), 
                            JPAEntities.createFakeEntity(Department.class, 50L)
                    )
            )
            .select(employee);
            
            /*
             * Though this join is inner join, but the reference association "Employee.department" 
             * is non-null and the user guarantee the corresponding foreign key in database
             * is not null too, so no difference between inner join and left join for this association.
             * 
             * "employee.join(Employee_.department, JoinType.INNER)"
             * can be changed to 
             * "employee.get(Employee_.department)" 
             * which can access the parent object by the foreign key in the child table itself automatically.
             * 
             * Finally, after optimization, in the generated JPQL, no joins
             */
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where babyfish_shared_alias_0.department = :babyfish_literal_0",
                    createQueryTemplate(cq).toString()
            );
        }
    }

    /*
     * Before learn this test method, you must learn JoinModeTest at first.
     */
    @Test
    public void testImplicitJoinBeforeJPQL() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        cq
        .where(
                cb.or(
                        cb.insensitivelyLike(
                            employee
                                // Get parent(not join), will be changed to join implicitly 
                                // because "department.name" is not id property
                                .get(Employee_.department) 
                                .get(Department_.name), // get name property that is not id
                                "A",
                                LikeMode.ANYWHERE
                        ),
                        cb.equal(
                                employee
                                // Explicit join declared by programmer, it can be merged with other joins.
                                // the JoinMode argument can be ignored because "MERGE_EXISTS" is default behavior.
                                .join(Employee_.department, JoinType.LEFT, JoinMode.REQUIRED_TO_MERGE_EXISTS)
                                .get(Department_.city),
                                "Cheng Du"
                        )
                )
        )
        .select(employee);
        
        /*
         * In this query, the first condition under "or" let child table get(not join) the parent 
         * and then get the "name" property that is not id. For this case, hibernate can use its 
         * functionality named "implicit join" to replace that association getter to "cross join", 
         * finally, the "CROSS JOIN" will be generated in the final SQL(at least, HSQLDialect do it like this).
         * 
         * Hibernate "implicit" can cause a big problem: If the query has created the join for
         * the same association(in this demo, it is the second conditional under "or"), the cross join
         * created as implicit joins will not be merged with the explicit joins that is already
         * declared by the programmer.
         * 
         * babyfish-jpa-crieria implements the similar functionality like the hibernate implicit-join,
         * it generate implicit inner join(not cross join) in the JPQL, before hibernate handle the JPQL, 
         * so the have no chance to generate the implicit join
         */
        Assert.assertEquals(
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                        
                // (1) employee.get(Employee_.name) has been replace to this "inner join"
                // so that hibernate has no chance to do its own implicit join.
                // (2) The implicit joins and the explicit joins can be merged to one join!
                // This is very important and this is why babyfish support this functionality 
                // to suppress the "implicit-join" of original hibernate
                //
                // INNER JOIN --\
                //              +--Merge--> INNER JOIN
                // LEFT JOIN  --/
                + "inner join babyfish_shared_alias_0.department babyfish_shared_alias_1 "
                
                + "where "
                +     "upper(babyfish_shared_alias_1.name) like :babyfish_literal_0 "
                +   "or "
                +     "babyfish_shared_alias_1.city = :babyfish_literal_1", 
                createQueryTemplate(cq).toString()
        );
        
        /*
         * If you look the SQL in runtime-log, you will found there is only one join;
         * but if you use original hibernate jpa crieria to do the same thing, you will 
         * find there are two joins in the SQL in runtime-log.
         */
    }
}
