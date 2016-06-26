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
package org.babyfish.hibernate.jpacriteria;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import junit.framework.Assert;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.model.jpa.path.CollectionFetchType;
import org.babyfish.model.jpa.path.spi.EntityManagerFactoryConfigurable;
import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfish.persistence.criteria.JoinMode;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XFetch;
import org.babyfish.persistence.criteria.XJoin;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.XSubquery;
import org.babyfish.persistence.criteria.spi.PathId;
import org.babyfish.persistence.criteria.spi.QueryContext;
import org.babyfish.persistence.criteria.spi.QueryContext.Entity;
import org.babyfish.persistence.criteria.spi.QueryContext.PathNode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class QueryContextTest {

    private static XEntityManagerFactory entityManagerFactory;
    
    @BeforeClass
    public static void initEntityManagerFactory() {
        entityManagerFactory =
                new HibernatePersistenceProvider(
                        QueryContextTest.class.getPackage().getName().replace('.', '/') + 
                        "/persistence.xml")
                .createEntityManagerFactory(null, null);
        Assert.assertTrue(
                ((EntityManagerFactoryConfigurable)entityManagerFactory)
                .isDbSchemaStrict()
        );
    }
    
    @AfterClass
    public static void disposeEntityManagerFactory() {
        EntityManagerFactory emf = entityManagerFactory;
        if (emf != null) {
            entityManagerFactory = null;
            emf.close();
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBadJoin() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        department.join("unkown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadFetch() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        department.fetch("unkown");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBadGet() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        department.get("unkown");
    }

    @Test
    public void testCreateNewJoin() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        JoinMode[][] joinModeMetrix = new JoinMode[][] { 
                new JoinMode[] { JoinMode.OPTIONALLY_CREATE_NEW, JoinMode.OPTIONALLY_CREATE_NEW },
                new JoinMode[] { JoinMode.REQUIRED_TO_CREATE_NEW, JoinMode.OPTIONALLY_CREATE_NEW },
                new JoinMode[] { JoinMode.OPTIONALLY_CREATE_NEW, JoinMode.REQUIRED_TO_CREATE_NEW },
                new JoinMode[] { JoinMode.REQUIRED_TO_CREATE_NEW, JoinMode.REQUIRED_TO_CREATE_NEW }
        }; 
        for (int i = 0; i < joinModeMetrix.length; i++) {
            XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
            XRoot<Company> company = cq.from(Company.class);
            cq.where(
                    cb.and(
                            cb.equal(
                                    company
                                    .join(Company_.departments, JoinType.LEFT, joinModeMetrix[i][0]) 
                                    .join(Department_.employees)
                                    .get(Employee_.name),
                                    "jim"
                            ),
                            cb.equal(
                                    company
                                    .join(Company_.departments, JoinType.RIGHT, joinModeMetrix[i][1])
                                    .join(Department_.offices)
                                    .get(Office_.name), 
                                    "pandora"
                            )
                    )
            );
            
            try (QueryContext queryContext = new QueryContext(cq)) {
                Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
                assertEntity(
                        queryContext.getRootEntities(cq).get(0),
                        new EntityDescriptor(
                                Company.class,
                                new EntityDescriptor(
                                        Department.class,
                                        Company_.departments,
                                        JoinType.LEFT,
                                        false,
                                        new EntityDescriptor(
                                                Employee.class,
                                                Department_.employees,
                                                JoinType.INNER,
                                                false
                                        )
                                ),
                                new EntityDescriptor(
                                        Department.class,
                                        Company_.departments,
                                        JoinType.RIGHT,
                                        false,
                                        new EntityDescriptor(
                                                Office.class,
                                                Department_.offices,
                                                JoinType.INNER,
                                                false
                                        )
                                )
                        )
                );
            }
        }
    }
    
    @Test
    public void testMergeExistingJoinFailed() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        JoinMode[][] joinModeMetrix = new JoinMode[][] {
                new JoinMode[] { JoinMode.OPTIONALLY_CREATE_NEW, JoinMode.OPTIONALLY_MERGE_EXISTS },
                new JoinMode[] { JoinMode.REQUIRED_TO_CREATE_NEW, JoinMode.OPTIONALLY_MERGE_EXISTS },
                new JoinMode[] { JoinMode.OPTIONALLY_CREATE_NEW, JoinMode.REQUIRED_TO_MERGE_EXISTS },
                new JoinMode[] { JoinMode.REQUIRED_TO_CREATE_NEW, JoinMode.REQUIRED_TO_MERGE_EXISTS },
                new JoinMode[] { JoinMode.OPTIONALLY_MERGE_EXISTS, JoinMode.OPTIONALLY_CREATE_NEW },
                new JoinMode[] { JoinMode.REQUIRED_TO_MERGE_EXISTS, JoinMode.OPTIONALLY_CREATE_NEW },
                new JoinMode[] { JoinMode.OPTIONALLY_MERGE_EXISTS, JoinMode.REQUIRED_TO_CREATE_NEW },
                new JoinMode[] { JoinMode.REQUIRED_TO_MERGE_EXISTS, JoinMode.REQUIRED_TO_CREATE_NEW },
        };
        for (int i = 0; i < joinModeMetrix.length; i++) {
            XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
            XRoot<Company> company = cq.from(Company.class);
            cq.where(
                    cb.and(
                            cb.equal(
                                    company
                                    .join(Company_.departments, JoinType.LEFT, joinModeMetrix[i][0]) 
                                    .join(Department_.employees)
                                    .get(Employee_.name),
                                    "jim"),
                            cb.equal(
                                    company
                                    .join(Company_.departments, JoinType.RIGHT, joinModeMetrix[i][1])
                                    .join(Department_.offices)
                                    .get(Office_.name), 
                                    "pandora")));
            
            try (QueryContext queryContext = new QueryContext(cq)) {
                Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
                
                assertEntity(
                        queryContext.getRootEntities(cq).get(0),
                        new EntityDescriptor(
                                Company.class,
                                new EntityDescriptor(
                                        Department.class,
                                        Company_.departments,
                                        JoinType.LEFT,
                                        false,
                                        new EntityDescriptor(
                                                Employee.class,
                                                Department_.employees,
                                                JoinType.INNER,
                                                false
                                        )
                                ),
                                new EntityDescriptor(
                                        Department.class,
                                        Company_.departments,
                                        JoinType.RIGHT,
                                        false,
                                        new EntityDescriptor(
                                                Office.class,
                                                Department_.offices,
                                                JoinType.INNER,
                                                false
                                        )
                                )
                        )
                );
            }
        }
    }
    
    @Test
    public void testMergeExistingJoinSuccessed() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        JoinMode[][] joinModeMetrix = new JoinMode[][] {
                new JoinMode[] { JoinMode.OPTIONALLY_MERGE_EXISTS, JoinMode.OPTIONALLY_MERGE_EXISTS },
                new JoinMode[] { JoinMode.REQUIRED_TO_MERGE_EXISTS, JoinMode.OPTIONALLY_MERGE_EXISTS },
                new JoinMode[] { JoinMode.OPTIONALLY_MERGE_EXISTS, JoinMode.REQUIRED_TO_MERGE_EXISTS },
                new JoinMode[] { JoinMode.REQUIRED_TO_MERGE_EXISTS, JoinMode.REQUIRED_TO_MERGE_EXISTS }
        };
        for (int i = 0; i < joinModeMetrix.length; i++) {
            XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
            XRoot<Company> company = cq.from(Company.class);
            cq.where(
                    cb.and(
                            cb.equal(
                                    company
                                    .join(Company_.departments, JoinType.LEFT, joinModeMetrix[i][0]) 
                                    .join(Department_.employees)
                                    .get(Employee_.name),
                                    "jim"),
                            cb.equal(
                                    company
                                    .join(Company_.departments, JoinType.RIGHT, joinModeMetrix[i][1])
                                    .join(Department_.offices)
                                    .get(Office_.name), 
                                    "pandora")));
            
            try (QueryContext queryContext = new QueryContext(cq)) {
                Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
                
                assertEntity(
                        queryContext.getRootEntities(cq).get(0),
                        new EntityDescriptor(
                                Company.class,
                                new EntityDescriptor(
                                        Department.class,
                                        Company_.departments,
                                        JoinType.INNER,
                                        false,
                                        new EntityDescriptor(
                                                Employee.class,
                                                Department_.employees,
                                                JoinType.INNER,
                                                false
                                        ),
                                        new EntityDescriptor(
                                                Office.class,
                                                Department_.offices,
                                                JoinType.INNER,
                                                false
                                        )
                                )
                        )
                );
            }
        }
    }
    
    @Test
    public void testMergeExistingJoinImplicitly() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
        XRoot<Company> company = cq.from(Company.class);
        cq.where(
                cb.and(
                        cb.equal(
                                company
                                .join(Company_.departments, JoinType.LEFT) 
                                .join(Department_.employees)
                                .get(Employee_.name),
                                "jim"),
                        cb.equal(
                                company
                                .join(Company_.departments, JoinType.RIGHT)
                                .join(Department_.offices)
                                .get(Office_.name), 
                                "pandora")));
        
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
            
            assertEntity(
                    queryContext.getRootEntities(cq).get(0),
                    new EntityDescriptor(
                            Company.class,
                            new EntityDescriptor(
                                    Department.class,
                                    Company_.departments,
                                    JoinType.INNER,
                                    false,
                                    new EntityDescriptor(
                                            Employee.class,
                                            Department_.employees,
                                            JoinType.INNER,
                                            false
                                    ),
                                    new EntityDescriptor(
                                            Office.class,
                                            Department_.offices,
                                            JoinType.INNER,
                                            false
                                    )
                            )
                    )
            );
        }
    }
    
    @Test
    public void testMergeSingularFetchFailed() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        cq.where(
                cb.equal(
                        employee
                        .join(Employee_.department, JoinType.LEFT, JoinMode.OPTIONALLY_CREATE_NEW)
                        .join(Department_.company, JoinType.RIGHT, JoinMode.OPTIONALLY_CREATE_NEW)
                        .get(Company_.name), 
                        "oracle"
                )
        );
        
        employee
        .fetch(Employee_.department, JoinType.RIGHT)
        .fetch(Department_.company, JoinType.LEFT);
        
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
            
            assertEntity(
                    queryContext.getRootEntities(cq).get(0),
                    new EntityDescriptor(
                            Employee.class,
                            new EntityDescriptor(
                                    Department.class,
                                    Employee_.department,
                                    JoinType.LEFT,
                                    false,
                                    new EntityDescriptor(
                                            Company.class,
                                            Department_.company,
                                            JoinType.RIGHT,
                                            false
                                    )
                            ),
                            new EntityDescriptor(
                                    Department.class,
                                    Employee_.department,
                                    JoinType.RIGHT,
                                    true,
                                    new EntityDescriptor(
                                            Company.class,
                                            Department_.company,
                                            JoinType.LEFT,
                                            true
                                    )
                            )
                    )
            );
        }
    }
    
    @Test
    public void testMergeSingularFetchSuccessed() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        cq.where(
                cb.equal(
                        employee
                        .join(Employee_.department, JoinType.LEFT)
                        .join(Department_.company, JoinType.RIGHT)
                        .get(Company_.name), 
                        "oracle"
                )
        );
        
        employee
        .fetch(Employee_.department, JoinType.RIGHT)
        .fetch(Department_.company, JoinType.LEFT);
        
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
            
            assertEntity(
                    queryContext.getRootEntities(cq).get(0),
                    new EntityDescriptor(
                            Employee.class,
                            new EntityDescriptor(
                                    Department.class,
                                    Employee_.department,
                                    JoinType.INNER,
                                    true,
                                    new EntityDescriptor(
                                            Company.class,
                                            Department_.company,
                                            JoinType.INNER,
                                            true
                                    )
                            )
                    )
            );
        }
    }
    
    @Test
    public void testMergePluralFetchFailedBecauseOfJoinMode() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
        XRoot<Company> company = cq.from(Company.class);
        cq.where(
                cb.and(
                        cb.equal(
                                company
                                .join(Company_.departments, JoinType.RIGHT, JoinMode.OPTIONALLY_CREATE_NEW)
                                .join(Department_.employees, JoinType.RIGHT, JoinMode.OPTIONALLY_CREATE_NEW)
                                .get(Employee_.name), 
                                "jim"
                        ),
                        cb.equal(
                                company
                                .join(Company_.departments, JoinType.RIGHT, JoinMode.OPTIONALLY_CREATE_NEW)
                                .join(Department_.offices, JoinType.RIGHT, JoinMode.OPTIONALLY_CREATE_NEW)
                                .get(Office_.name), 
                                "pandora"
                        )
                )
        );
        
        company
        .fetch(Company_.departments, JoinType.LEFT, CollectionFetchType.PARTIAL)
        .fetch(Department_.employees, JoinType.LEFT, CollectionFetchType.PARTIAL);
        
        company
        .fetch(Company_.departments, JoinType.LEFT, CollectionFetchType.PARTIAL)
        .fetch(Department_.offices, JoinType.LEFT, CollectionFetchType.PARTIAL);
        
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
            
            assertEntity(
                    queryContext.getRootEntities(cq).get(0),
                    new EntityDescriptor(
                            Company.class,
                            new EntityDescriptor(
                                    Department.class,
                                    Company_.departments,
                                    JoinType.RIGHT,
                                    false,
                                    new EntityDescriptor(
                                            Employee.class,
                                            Department_.employees,
                                            JoinType.RIGHT,
                                            false
                                    )
                            ),
                            new EntityDescriptor(
                                    Department.class,
                                    Company_.departments,
                                    JoinType.RIGHT,
                                    false,
                                    new EntityDescriptor(
                                            Office.class,
                                            Department_.offices,
                                            JoinType.RIGHT,
                                            false
                                    )
                            ),
                            new EntityDescriptor(
                                    Department.class,
                                    Company_.departments,
                                    JoinType.LEFT,
                                    true,
                                    new EntityDescriptor(
                                            Employee.class,
                                            Department_.employees,
                                            JoinType.LEFT,
                                            true
                                    ),
                                    new EntityDescriptor(
                                            Office.class,
                                            Department_.offices,
                                            JoinType.LEFT,
                                            true
                                    )
                            )
                    )
            );
        }
    }
    
    @Test
    public void testMergePluralFetchFailedBecauseOfCollectionFetchType() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
        XRoot<Company> company = cq.from(Company.class);
        cq.where(
                cb.and(
                        cb.equal(
                                company
                                .join(Company_.departments, JoinType.RIGHT)
                                .join(Department_.employees, JoinType.RIGHT)
                                .get(Employee_.name), 
                                "jim"
                        ),
                        cb.equal(
                                company
                                .join(Company_.departments, JoinType.RIGHT)
                                .join(Department_.offices, JoinType.RIGHT)
                                .get(Office_.name), 
                                "pandora"
                        )
                )
        );
        
        company
        .fetch(Company_.departments, JoinType.LEFT)
        .fetch(Department_.employees, JoinType.LEFT);
        
        company
        .fetch(Company_.departments, JoinType.LEFT)
        .fetch(Department_.offices, JoinType.LEFT);
        
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
            
            assertEntity(
                    queryContext.getRootEntities(cq).get(0),
                    new EntityDescriptor(
                            Company.class,
                            new EntityDescriptor(
                                    Department.class,
                                    Company_.departments,
                                    JoinType.RIGHT,
                                    false,
                                    new EntityDescriptor(
                                            Employee.class,
                                            Department_.employees,
                                            JoinType.RIGHT,
                                            false
                                    ),
                                    new EntityDescriptor(
                                            Office.class,
                                            Department_.offices,
                                            JoinType.RIGHT,
                                            false
                                    )
                            ),
                            new EntityDescriptor(
                                    Department.class,
                                    Company_.departments,
                                    JoinType.LEFT,
                                    true,
                                    new EntityDescriptor(
                                            Employee.class,
                                            Department_.employees,
                                            JoinType.LEFT,
                                            true
                                    ),
                                    new EntityDescriptor(
                                            Office.class,
                                            Department_.offices,
                                            JoinType.LEFT,
                                            true
                                    )
                            )
                    )
            );
        }
    }
    
    @Test
    public void testMergePluralFetchSuccessed() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
        XRoot<Company> company = cq.from(Company.class);
        cq.where(
                cb.and(
                        cb.equal(
                                company
                                .join(Company_.departments, JoinType.RIGHT)
                                .join(Department_.employees, JoinType.RIGHT)
                                .get(Employee_.name), 
                                "jim"
                        ),
                        cb.equal(
                                company
                                .join(Company_.departments, JoinType.RIGHT)
                                .join(Department_.offices, JoinType.RIGHT)
                                .get(Office_.name), 
                                "pandora"
                        )
                )
        );
        
        company
        .fetch(Company_.departments, JoinType.LEFT, CollectionFetchType.PARTIAL)
        .fetch(Department_.employees, JoinType.LEFT, CollectionFetchType.PARTIAL);
        
        company
        .fetch(Company_.departments, JoinType.LEFT, CollectionFetchType.PARTIAL)
        .fetch(Department_.offices, JoinType.LEFT, CollectionFetchType.PARTIAL);
        
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
            
            assertEntity(
                    queryContext.getRootEntities(cq).get(0),
                    new EntityDescriptor(
                            Company.class,
                            new EntityDescriptor(
                                    Department.class,
                                    Company_.departments,
                                    JoinType.INNER,
                                    true,
                                    new EntityDescriptor(
                                            Employee.class,
                                            Department_.employees,
                                            JoinType.INNER,
                                            true
                                    ),
                                    new EntityDescriptor(
                                            Office.class,
                                            Department_.offices,
                                            JoinType.INNER,
                                            true
                                    )
                            )
                    )
            );
        }
    }
    
    @Test
    public void testMergeAndTheCreateNewByJoinMode() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
        XRoot<Company> company = cq.from(Company.class);
        cq.where(
                cb.and(
                        cb.equal(
                                company
                                .join(Company_.departments, JoinType.RIGHT)
                                .join(Department_.employees, JoinType.RIGHT, JoinMode.OPTIONALLY_CREATE_NEW)
                                .get(Employee_.name), 
                                "jim"
                        ),
                        cb.equal(
                                company
                                .join(Company_.departments, JoinType.RIGHT)
                                .join(Department_.offices, JoinType.RIGHT, JoinMode.OPTIONALLY_CREATE_NEW)
                                .get(Office_.name), 
                                "pandora"
                        )
                )
        );
        
        company
        .fetch(Company_.departments, JoinType.LEFT, CollectionFetchType.PARTIAL)
        .fetch(Department_.employees, JoinType.LEFT, CollectionFetchType.PARTIAL);
        
        company
        .fetch(Company_.departments, JoinType.LEFT, CollectionFetchType.PARTIAL)
        .fetch(Department_.offices, JoinType.LEFT, CollectionFetchType.PARTIAL);
        
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
            
            assertEntity(
                    queryContext.getRootEntities(cq).get(0),
                    new EntityDescriptor(
                            Company.class,
                            new EntityDescriptor(
                                    Department.class,
                                    Company_.departments,
                                    JoinType.INNER,
                                    true,
                                    new EntityDescriptor(
                                            Employee.class,
                                            Department_.employees,
                                            JoinType.RIGHT,
                                            false
                                    ),
                                    new EntityDescriptor(
                                            Office.class,
                                            Department_.offices,
                                            JoinType.RIGHT,
                                            false
                                    ),
                                    new EntityDescriptor(
                                            Employee.class,
                                            Department_.employees,
                                            JoinType.LEFT,
                                            true
                                    ),
                                    new EntityDescriptor(
                                            Office.class,
                                            Department_.offices,
                                            JoinType.LEFT,
                                            true)
                            )
                    )
            );
        }
    }
    
    @Test
    public void testMergeAndTheCreateNewByCollectionFetchType() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
        XRoot<Company> company = cq.from(Company.class);
        cq.where(
                cb.and(
                        cb.equal(
                                company
                                .join(Company_.departments, JoinType.RIGHT)
                                .join(Department_.employees, JoinType.RIGHT)
                                .get(Employee_.name), 
                                "jim"
                        ),
                        cb.equal(
                                company
                                .join(Company_.departments, JoinType.RIGHT)
                                .join(Department_.offices, JoinType.RIGHT)
                                .get(Office_.name), 
                                "pandora"
                        )
                )
        );
        
        company
        .fetch(Company_.departments, JoinType.LEFT, CollectionFetchType.PARTIAL)
        .fetch(Department_.employees, JoinType.LEFT);
        
        company
        .fetch(Company_.departments, JoinType.LEFT, CollectionFetchType.PARTIAL)
        .fetch(Department_.offices, JoinType.LEFT);
        
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
            
            assertEntity(
                    queryContext.getRootEntities(cq).get(0),
                    new EntityDescriptor(
                            Company.class,
                            new EntityDescriptor(
                                    Department.class,
                                    Company_.departments,
                                    JoinType.INNER,
                                    true,
                                    new EntityDescriptor(
                                            Employee.class,
                                            Department_.employees,
                                            JoinType.RIGHT,
                                            false
                                    ),
                                    new EntityDescriptor(
                                            Office.class,
                                            Department_.offices,
                                            JoinType.RIGHT,
                                            false
                                    ),
                                    new EntityDescriptor(
                                            Employee.class,
                                            Department_.employees,
                                            JoinType.LEFT,
                                            true
                                    ),
                                    new EntityDescriptor(
                                            Office.class,
                                            Department_.offices,
                                            JoinType.LEFT,
                                            true)
                            )
                    )
            );
        }
    }
    
    @Test
    public void testAutoSetCollectionFetchTypeToSingular() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        
        Assert.assertEquals(
                CollectionFetchType.ALL, 
                department
                .fetch("employees", CollectionFetchType.ALL)
                .getCollectionFetchType());
        Assert.assertEquals(
                CollectionFetchType.PARTIAL, 
                department
                .fetch("employees", CollectionFetchType.PARTIAL)
                .getCollectionFetchType());
        Assert.assertEquals(
                CollectionFetchType.ALL, 
                department
                .fetch("company", CollectionFetchType.ALL)
                .getCollectionFetchType());
        Assert.assertEquals( //set partial, but still all
                CollectionFetchType.ALL, 
                department
                .fetch("company", CollectionFetchType.PARTIAL)
                .getCollectionFetchType());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testIllegalOnOperation1() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
        XRoot<Company> company = cq.from(Company.class);
        XJoin<Company, Department> department = company.join(Company_.departments, JoinMode.OPTIONALLY_MERGE_EXISTS);
        department.on(cb.like(department.get(Department_.name), "%a%"));
    }
    
    @Test(expected = IllegalStateException.class)
    public void testIllegalOnOperation2() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
        XRoot<Company> company = cq.from(Company.class);
        XJoin<Company, Department> department = company.join(Company_.departments, JoinMode.REQUIRED_TO_MERGE_EXISTS);
        department.on(cb.like(department.get(Department_.name), "%a%"));
    }
    
    @Test
    public void testOnOperation() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
        XRoot<Company> company = cq.from(Company.class);
        XJoin<Company, Department> department = company.join(Company_.departments, JoinMode.OPTIONALLY_CREATE_NEW);
        department.on(cb.like(department.get(Department_.name), "%a%"));
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
            
            assertEntity(
                    queryContext.getRootEntities(cq).get(0),
                    new EntityDescriptor(
                            Company.class,
                            new EntityDescriptor(
                                    Department.class,
                                    Company_.departments,
                                    JoinType.INNER,
                                    false
                            )
                    )
            );
        }
    }
    
    @Test
    public void testEntityUsed() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
        XRoot<Company> company = cq.from(Company.class);
        XJoin<Company, Department> department = company.join(Company_.departments);
        XJoin<Department, Employee> employee = department.join(Department_.employees);
        XJoin<Department, Office> office = department.join(Department_.offices);
        
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertTrue(queryContext.getEntity(company).isUsed());
            Assert.assertFalse(queryContext.getEntity(department).isUsed());
            Assert.assertFalse(queryContext.getEntity(employee).isUsed());
            Assert.assertFalse(queryContext.getEntity(office).isUsed());
        }
        
        cq.where(cb.equal(office.get(Office_.name), "pandora"));
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertTrue(queryContext.getEntity(company).isUsed());
            Assert.assertTrue(queryContext.getEntity(department).isUsed());
            Assert.assertFalse(queryContext.getEntity(employee).isUsed());
            Assert.assertTrue(queryContext.getEntity(office).isUsed());
        }
        
        XFetch<Employee, AnnualLeave> annualLeave = employee.fetch(Employee_.annualLeaves);
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertTrue(queryContext.getEntity(company).isUsed());
            Assert.assertTrue(queryContext.getEntity(department).isUsed());
            Assert.assertTrue(queryContext.getEntity(employee).isUsed());
            Assert.assertTrue(queryContext.getEntity(office).isUsed());
            Assert.assertTrue(queryContext.getEntity(annualLeave).isUsed());
        }
    }
    
    @Test
    public void testEntityUsedWhenJoinIsReplacedToGet() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Long> cq = cb.createQuery(Long.class);
        XRoot<AnnualLeave> annualLeave = cq.from(AnnualLeave.class);
        XJoin<AnnualLeave, Employee> employee = annualLeave.join(AnnualLeave_.employee);
        XJoin<Employee, Department> department = employee.join(Employee_.department);
        XJoin<Department, Company> company = department.join(Department_.company, JoinType.LEFT);
        cq.select(company.get(Company_.id));
        
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertTrue(queryContext.getEntity(annualLeave).isUsed());
            Assert.assertTrue(queryContext.getEntity(employee).isUsed());
            Assert.assertTrue(queryContext.getEntity(department).isUsed());
            Assert.assertFalse(queryContext.getEntity(company).isUsed());
        }
    }
    
    @Test
    public void testReplaceLeftJoinToGetBecauseId() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Long> cq = cb.createQuery(Long.class);
        XRoot<AnnualLeave> annualLeave = cq.from(AnnualLeave.class);
        Path<Long> companyId = 
                annualLeave
                .join(AnnualLeave_.employee, JoinType.LEFT)
                .join(Employee_.department, JoinType.LEFT)
                .join(Department_.company, JoinType.LEFT)
                .get(Company_.id);
        cq.select(companyId);
        
        /*
         * Left reference join can always be optimized successfully.
         */
        try (DbSchemaStrictScope scope = new DbSchemaStrictScope(entityManagerFactory, false);
                QueryContext queryContext = new QueryContext(cq)) {
            assertPathNode(
                    queryContext.getPathNodes().get(pathId(queryContext, companyId, 0)),
                    new PathNodeDescriptor(AnnualLeave.class)
                    .join(AnnualLeave_.employee, JoinType.LEFT)
                    .join(Employee_.department, JoinType.LEFT)
                    .get(Department_.company)
                    .get(Company_.id)
            );
        }
    }

    @Test
    public void testReplaceNonNullInnerJoinToGetBecauseId() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Long> cq = cb.createQuery(Long.class);
        XRoot<AnnualLeave> annualLeave = cq.from(AnnualLeave.class);
        Path<Long> companyId = 
                annualLeave
                .join(AnnualLeave_.employee, JoinType.LEFT)
                .join(Employee_.department, JoinType.LEFT)
                .join(Department_.company, JoinType.INNER)
                .get(Company_.id);
        cq.select(companyId);
        
        /*
         * Optimize failed:
         * 
         * When db schema is NOT strict, NonNull ManyToOne does NOT means NonNull foreign key,
         * last inner join can NOT be replaced to be getter
         */
        try (DbSchemaStrictScope scope = new DbSchemaStrictScope(entityManagerFactory, false);
                QueryContext queryContext = new QueryContext(cq)) {
            assertPathNode(
                    queryContext.getPathNodes().get(pathId(queryContext, companyId, 0)),
                    new PathNodeDescriptor(AnnualLeave.class)
                    .join(AnnualLeave_.employee, JoinType.LEFT)
                    .join(Employee_.department, JoinType.LEFT)
                    .join(Department_.company, JoinType.INNER)
                    .get(Company_.id)
            );
        }
        
        /*
         * Optimize successed:
         * 
         * When db schema is strict, NonNull ManyToOne means NonNull foreign key,
         * last inner join can be replaced to be getter
         */
        try (DbSchemaStrictScope scope = new DbSchemaStrictScope(entityManagerFactory, true);
                QueryContext queryContext = new QueryContext(cq)) {
            assertPathNode(
                    queryContext.getPathNodes().get(pathId(queryContext, companyId, 0)),
                    new PathNodeDescriptor(AnnualLeave.class)
                    .join(AnnualLeave_.employee, JoinType.LEFT)
                    .join(Employee_.department, JoinType.LEFT)
                    .get(Department_.company)
                    .get(Company_.id)
            );
        }
    }
    
    @Test
    public void testReplaceGetToJoinIncludeLeaf() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        XRoot<AnnualLeave> annualLeave = cq.from(AnnualLeave.class);
        Path<Company> company = annualLeave
        .get(AnnualLeave_.employee)
        .get(Employee_.department)
        .get(Department_.company);
        cq.multiselect(company, company);
        try (QueryContext queryContext = new QueryContext(cq)) {
            assertPathNode(
                    queryContext.getPathNodes().get(pathId(queryContext, company, 0)),
                    new PathNodeDescriptor(AnnualLeave.class)
                    .join(AnnualLeave_.employee, JoinType.INNER)
                    .join(Employee_.department, JoinType.INNER)
                    .join(Department_.company, JoinType.INNER)
            );
            assertPathNode(
                    queryContext.getPathNodes().get(pathId(queryContext, company, 1)),
                    new PathNodeDescriptor(AnnualLeave.class)
                    .join(AnnualLeave_.employee, JoinType.INNER)
                    .join(Employee_.department, JoinType.INNER)
                    .join(Department_.company, JoinType.INNER)
            );
        }
    }
    
    @Test
    public void testReplaceGetToJoinExcludeLeaf() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        XRoot<Department> department = cq.from(Department.class);
        XSubquery<Department> subq = cq.subquery(Department.class);
        XRoot<Employee> subEmployee = subq.from(Employee.class);
        
        Path<Department> subqueryDepartment = subEmployee.get(Employee_.department);
        
        //not department.get(Department_.company).get(Company_.id), test the keep logic in PathNodeImpl.of
        Path<Long> companyId = department.get(Department_.company).get(Company_.id);
        Path<Company> groupByCompany = department.get(Department_.company);
        Path<Company> orderByCompany = department.get(Department_.company);
        
        subq
        .select(subqueryDepartment)
        .where(
                cb.like(
                        subEmployee.get(Employee_.name), 
                        "%peter%"
                )
        );
        
        cq
        .multiselect(
                companyId,
                cb.countDistinct(department)
        )
        .groupBy(groupByCompany)
        .where(cb.in(department).value(subq))
        .orderBy(cb.asc(orderByCompany));
        
        try (QueryContext queryContext = new QueryContext(cq)) {
            assertPathNode(
                    queryContext.getPathNodes().get(pathId(queryContext, subqueryDepartment, 0)),
                    new PathNodeDescriptor(Employee.class)
                    .get(Employee_.department)
            );
            assertPathNode(
                    queryContext.getPathNodes().get(pathId(queryContext, companyId, 0)),
                    new PathNodeDescriptor(Department.class)
                    .get(Department_.company)
                    .get(Company_.id)
            );
            assertPathNode(
                    queryContext.getPathNodes().get(pathId(queryContext, groupByCompany, 0)),
                    new PathNodeDescriptor(Department.class)
                    .get(Department_.company)
            );
            assertPathNode(
                    queryContext.getPathNodes().get(pathId(queryContext, orderByCompany, 0)),
                    new PathNodeDescriptor(Department.class)
                    .get(Department_.company)
            );
        }
    }
    
    @Test
    public void testReplaceGetToJoinIncludeLeafAndExcludeLear() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        Path<Department> department = employee.get(Employee_.department);
        cq.select(department).orderBy(cb.asc(department));
        
        try (QueryContext queryContext = new QueryContext(cq)) {
            assertPathNode(
                    queryContext.getPathNodes().get(pathId(queryContext, department, 0)),
                    new PathNodeDescriptor(Employee.class)
                    .join(Employee_.department, JoinType.INNER)
            );
            assertPathNode(
                    queryContext.getPathNodes().get(pathId(queryContext, department, 1)),
                    new PathNodeDescriptor(Employee.class)
                    .get(Employee_.department)
            );
        }
    }
    
    @Test
    public void testFetchAllOnMergeExistsJoin() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        department.join(Department_.employees, JoinMode.REQUIRED_TO_MERGE_EXISTS).join(Employee_.annualLeaves, JoinMode.REQUIRED_TO_MERGE_EXISTS);
        department.fetch(Department_.employees, CollectionFetchType.ALL).fetch(Employee_.annualLeaves, CollectionFetchType.ALL);
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
            assertEntity(
                    queryContext.getRootEntities(cq).get(0),
                    new EntityDescriptor(
                            Department.class,
                            new EntityDescriptor(
                                    Employee.class,
                                    Department_.employees,
                                    JoinType.INNER,
                                    false,
                                    new EntityDescriptor(
                                            AnnualLeave.class,
                                            Employee_.annualLeaves,
                                            JoinType.INNER,
                                            false
                                    )
                            ),
                            new EntityDescriptor(
                                    Employee.class,
                                    Department_.employees,
                                    JoinType.INNER,
                                    true,
                                    new EntityDescriptor(
                                            AnnualLeave.class,
                                            Employee_.annualLeaves,
                                            JoinType.INNER,
                                            true
                                    )
                            )
                    )
            );
        }
    }
    
    @Test
    public void testFetchAllOnCreateNewJoin() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        department.join(Department_.employees, JoinMode.REQUIRED_TO_CREATE_NEW).join(Employee_.annualLeaves, JoinMode.REQUIRED_TO_CREATE_NEW);
        department.fetch(Department_.employees, CollectionFetchType.ALL).fetch(Employee_.annualLeaves, CollectionFetchType.ALL);
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
            assertEntity(
                    queryContext.getRootEntities(cq).get(0),
                    new EntityDescriptor(
                            Department.class,
                            new EntityDescriptor(
                                    Employee.class,
                                    Department_.employees,
                                    JoinType.INNER,
                                    false,
                                    new EntityDescriptor(
                                            AnnualLeave.class,
                                            Employee_.annualLeaves,
                                            JoinType.INNER,
                                            false
                                    )
                            ),
                            new EntityDescriptor(
                                    Employee.class,
                                    Department_.employees,
                                    JoinType.INNER,
                                    true,
                                    new EntityDescriptor(
                                            AnnualLeave.class,
                                            Employee_.annualLeaves,
                                            JoinType.INNER,
                                            true
                                    )
                            )
                    )
            );
        }
    }
    
    @Test
    public void testFetchPartialOnMergeExistsJoin() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        department.join(Department_.employees, JoinMode.REQUIRED_TO_MERGE_EXISTS).join(Employee_.annualLeaves, JoinMode.REQUIRED_TO_MERGE_EXISTS);
        department.fetch(Department_.employees, CollectionFetchType.PARTIAL).fetch(Employee_.annualLeaves, CollectionFetchType.PARTIAL);
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
            assertEntity(
                    queryContext.getRootEntities(cq).get(0),
                    new EntityDescriptor(
                            Department.class,
                            new EntityDescriptor(
                                    Employee.class,
                                    Department_.employees,
                                    JoinType.INNER,
                                    true,
                                    new EntityDescriptor(
                                            AnnualLeave.class,
                                            Employee_.annualLeaves,
                                            JoinType.INNER,
                                            true
                                    )
                            )
                    )
            );
        }
    }
    
    @Test
    public void testFetchPartialOnCreateNewJoin() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        department.join(Department_.employees, JoinMode.REQUIRED_TO_CREATE_NEW).join(Employee_.annualLeaves, JoinMode.REQUIRED_TO_CREATE_NEW);
        department.fetch(Department_.employees, CollectionFetchType.PARTIAL).fetch(Employee_.annualLeaves, CollectionFetchType.PARTIAL);
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, queryContext.getRootEntities(cq).size());
            assertEntity(
                    queryContext.getRootEntities(cq).get(0),
                    new EntityDescriptor(
                            Department.class,
                            new EntityDescriptor(
                                    Employee.class,
                                    Department_.employees,
                                    JoinType.INNER,
                                    false,
                                    new EntityDescriptor(
                                            AnnualLeave.class,
                                            Employee_.annualLeaves,
                                            JoinType.INNER,
                                            false
                                    )
                            ),
                            new EntityDescriptor(
                                    Employee.class,
                                    Department_.employees,
                                    JoinType.INNER,
                                    true,
                                    new EntityDescriptor(
                                            AnnualLeave.class,
                                            Employee_.annualLeaves,
                                            JoinType.INNER,
                                            true
                                    )
                            )
                    )
            );
        }
    }
    
    @Test
    public void testParameters() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        ParameterExpression<Integer> departmentNameMinLength = cb.parameter(Integer.class);
        ParameterExpression<Integer> departmentNameMaxLength = cb.parameter(Integer.class);
        ParameterExpression<String> departmentName = cb.parameter(String.class, "departmentName");
        cq.where(
                cb.and(
                        cb.ge(
                                cb.length(department.get(Department_.name)),
                                departmentNameMinLength
                        ),
                        cb.like(
                                department.get(Department_.name), 
                                departmentName
                        ),
                        cb.le(
                                cb.length(department.get(Department_.name)),
                                departmentNameMaxLength
                        )
                )
        );
        
        Assert.assertNull(departmentNameMaxLength.getPosition());
        Assert.assertNull(departmentNameMaxLength.getPosition());
        Assert.assertNull(departmentName.getPosition());
        
        try (QueryContext queryContext = new QueryContext(cq)) {
            Assert.assertEquals(1, departmentNameMinLength.getPosition().intValue());
            Assert.assertEquals(2, departmentNameMaxLength.getPosition().intValue());
            Assert.assertNull(departmentName.getPosition());
            
            Assert.assertEquals(2, queryContext.getUnnamedParameters().size());
            Assert.assertSame(
                    departmentNameMinLength, 
                    queryContext.getUnnamedParameters().get(0));
            Assert.assertSame(
                    departmentNameMaxLength, 
                    queryContext.getUnnamedParameters().get(1));
            
            Assert.assertEquals(1, queryContext.getNamedParameters().size());
            Assert.assertSame(
                    departmentName, 
                    queryContext.getNamedParameters().get("departmentName"));
        }
    }
    
    @Test
    public void testShareNamedParameters() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<AnnualLeave> cq = cb.createQuery(AnnualLeave.class);
        XRoot<AnnualLeave> annualLeave = cq.from(AnnualLeave.class);
        ParameterExpression<Date> date = cb.parameter(Date.class, "date");
        cq.where(
                cb.greaterThanOrEqualTo(annualLeave.get(AnnualLeave_.startDate), date),
                cb.greaterThanOrEqualTo(annualLeave.get(AnnualLeave_.endDate), date)
        );
        new QueryContext(cq).close();
    }
    
    @Test(expected = IllegalProgramException.class)
    public void testShareUnnamedParameters() {
        /*
         * TODO: is it right?
         * can JPQL shared unnamed parameters such as
         * "where p.firstName = ?1 or p.lastName = ?1"?
         */
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<AnnualLeave> cq = cb.createQuery(AnnualLeave.class);
        XRoot<AnnualLeave> annualLeave = cq.from(AnnualLeave.class);
        ParameterExpression<Date> date = cb.parameter(Date.class);
        cq.where(
                cb.greaterThanOrEqualTo(annualLeave.get(AnnualLeave_.startDate), date),
                cb.greaterThanOrEqualTo(annualLeave.get(AnnualLeave_.endDate), date)
        );
        new QueryContext(cq).close();
    }
    
    private static void assertEntity(Entity entity, EntityDescriptor entityDescriptor) {
        Assert.assertSame(entityDescriptor.entityClass, entity.getManagedType().getJavaType());
        if (entityDescriptor.attribute != null) {
            Assert.assertSame(entityDescriptor.attribute, entity.getAttribute());
            Assert.assertEquals(entityDescriptor.joinType, entity.getJoinType());
        } else {
            Assert.assertNull(entity.getAttribute());
            Assert.assertNull(entity.getJoinType());
        }
        Assert.assertEquals(entityDescriptor.fetch, entity.isFetch());
        List<EntityDescriptor> childNodes = entityDescriptor.childDescriptors;
        Assert.assertEquals(childNodes.size(), entity.getEntities().size());
        for (int i = 0; i < childNodes.size(); i++) {
            assertEntity(entity.getEntities().get(i), childNodes.get(i));
        }
    }
    
    private PathId pathId(QueryContext queryContext, Path<?> path, int appearancePosition) {
        int num = 0;
        for (PathId pathId : queryContext.getPathNodes().keySet()) {
            if (pathId.getPath() == path && num++ == appearancePosition) {
                return pathId;
            }
        }
        return null;
    }
    
    private static void assertPathNode(PathNode pathNode, PathNodeDescriptor pathNodeDescriptor) {
        while (pathNode != null) {
            Assert.assertNotNull(pathNodeDescriptor);
            Assert.assertSame(
                    pathNodeDescriptor.javaType, 
                    pathNode.getEntity() != null ?
                    pathNode.getEntity().getManagedType().getJavaType() :
                    (
                            pathNode.getAttribute() instanceof PluralAttribute<?, ?, ?> ?
                                    ((PluralAttribute<?, ?, ?>)pathNode.getAttribute()).getElementType().getJavaType() :
                                    ((SingularAttribute<?, ?>)pathNode.getAttribute()).getType().getJavaType()
                    )
            );
            Assert.assertSame(pathNodeDescriptor.attribute, pathNode.getAttribute());
            Assert.assertSame(
                    pathNodeDescriptor.joinType, 
                    pathNode.getEntity() != null ? pathNode.getEntity().getJoinType() : null);
            pathNode = pathNode.getParent();
            pathNodeDescriptor = pathNodeDescriptor.parent;
        }
        Assert.assertNull(pathNodeDescriptor);
    }
    
    private static class EntityDescriptor {
        
        private Class<?> entityClass;
        
        private Attribute<?, ?> attribute;
        
        private JoinType joinType;
        
        private boolean fetch;
        
        private List<EntityDescriptor> childDescriptors;
        
        public EntityDescriptor(
                Class<?> entityClass,
                EntityDescriptor ... childDescriptors) {
            this(entityClass, null, null, false, childDescriptors);
        }
        
        public EntityDescriptor(
                Class<?> entityClass,
                Attribute<?, ?> attribute,
                JoinType joinType,
                boolean fetch,
                EntityDescriptor ... childDescriptors) {
            this.entityClass = entityClass;
            this.attribute = attribute;
            this.joinType = joinType;
            this.fetch = fetch;
            if (childDescriptors.length != 0) {
                List<EntityDescriptor> list = new ArrayList<>(childDescriptors.length);
                for (EntityDescriptor childNode : childDescriptors) {
                    list.add(childNode);
                }
                this.childDescriptors = MACollections.unmodifiable(list);
            } else {
                this.childDescriptors = MACollections.emptyList();
            }
        }
        
    }
    
    private static class PathNodeDescriptor {
        
        private PathNodeDescriptor parent;
        
        private Class<?> javaType;
        
        private Attribute<?, ?> attribute;
        
        private JoinType joinType;
        
        public PathNodeDescriptor(Class<?> javaType) {
            this.javaType = javaType;
        }
        
        public PathNodeDescriptor join(Attribute<?, ?> attribute, JoinType joinType) {
            PathNodeDescriptor join = new PathNodeDescriptor(
                    attribute instanceof PluralAttribute<?, ?, ?> ?
                            ((PluralAttribute<?, ?, ?>)attribute).getElementType().getJavaType() :
                            ((SingularAttribute<?, ?>)attribute).getType().getJavaType());
            join.parent = this;
            join.attribute = attribute;
            join.joinType = joinType;
            return join;
        }
        
        public PathNodeDescriptor get(Attribute<?, ?> attribute) {
            PathNodeDescriptor get = new PathNodeDescriptor(
                    attribute instanceof PluralAttribute<?, ?, ?> ?
                            ((PluralAttribute<?, ?, ?>)attribute).getElementType().getJavaType() :
                            ((SingularAttribute<?, ?>)attribute).getType().getJavaType());
            get.parent = this;
            get.attribute = attribute;
            return get;
        }
    }
}
