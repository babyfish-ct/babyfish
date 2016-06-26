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

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaBuilder.Trimspec;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.babyfish.collection.MACollections;
import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.model.jpa.path.CollectionFetchType;
import org.babyfish.model.jpa.path.spi.EntityManagerFactoryConfigurable;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;
import org.babyfish.persistence.criteria.JoinMode;
import org.babyfish.persistence.criteria.QueryTemplate;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaDelete;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XCriteriaUpdate;
import org.babyfish.persistence.criteria.XJoin;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.XSubquery;
import org.babyfish.persistence.criteria.spi.AbstractQueryTemplate;
import org.babyfish.persistence.criteria.spi.QueryContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class QueryTemplateTest {
    
    private static final Field JPQL_FIELD;
    
    private static final Field IMPLICIT_PARAMTERS_FIELD;
    
    private static final Field NAME_FIELD;
    
    private static final Field VALUE_FIELD;
    
    private static XEntityManagerFactory entityManagerFactory;
    
    private XEntityManager entityManager;
    
    @BeforeClass
    public static void initEntityManagerFactory() {
        entityManagerFactory =
                new HibernatePersistenceProvider(
                        QueryTemplateTest.class.getPackage().getName().replace('.', '/') + 
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
    
    @Before
    public void initEntityManager() {
        this.entityManager = entityManagerFactory.createEntityManager();
    }
    
    @After
    public void disposeEntityManager() {
        EntityManager em = this.entityManager;
        if (em != null) {
            this.entityManager = null;
            em.close();
        }
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
            XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
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
            QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
            Assert.assertEquals(
                    "select " +
                    "babyfish_shared_alias_0, " +
                    "babyfish_not_shared_alias_1, " +
                    "babyfish_shared_alias_2, " +
                    "babyfish_not_shared_alias_3, " +
                    "babyfish_shared_alias_4 " +
                    "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                    "left join babyfish_shared_alias_0.departments babyfish_not_shared_alias_1 " +
                    "inner join babyfish_not_shared_alias_1.employees babyfish_shared_alias_2 " +
                    "right join babyfish_shared_alias_0.departments babyfish_not_shared_alias_3 " +
                    "inner join babyfish_not_shared_alias_3.offices babyfish_shared_alias_4 " +
                    "where " +
                    "babyfish_shared_alias_2.name = :babyfish_literal_0 and " +
                    "babyfish_shared_alias_4.name = :babyfish_literal_1",
                    get(template, JPQL_FIELD));
            assertImplictParameters(
                    template,
                    "babyfish_literal_0", "jim",
                    "babyfish_literal_1", "pandora");
            template.createQuery(this.entityManager).getResultList();
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
            XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
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
            
            QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
            if (i < joinModeMetrix.length / 2) {
                Assert.assertEquals(
                        "select " +
                        "babyfish_shared_alias_0, " +
                        "babyfish_not_shared_alias_1, " +
                        "babyfish_shared_alias_2, " +
                        "babyfish_shared_alias_3, " +
                        "babyfish_shared_alias_4 " +
                        "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                        "left join babyfish_shared_alias_0.departments babyfish_not_shared_alias_1 " +
                        "inner join babyfish_not_shared_alias_1.employees babyfish_shared_alias_2 " +
                        "right join babyfish_shared_alias_0.departments babyfish_shared_alias_3 " +
                        "inner join babyfish_shared_alias_3.offices babyfish_shared_alias_4 " +
                        "where " +
                        "babyfish_shared_alias_2.name = :babyfish_literal_0 and " +
                        "babyfish_shared_alias_4.name = :babyfish_literal_1",
                        get(template, JPQL_FIELD));
            } else {
                Assert.assertEquals(
                        "select " +
                        "babyfish_shared_alias_0, " +
                        "babyfish_shared_alias_1, " +
                        "babyfish_shared_alias_2, " +
                        "babyfish_not_shared_alias_3, " +
                        "babyfish_shared_alias_4 " +
                        "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                        "left join babyfish_shared_alias_0.departments babyfish_shared_alias_1 " +
                        "inner join babyfish_shared_alias_1.employees babyfish_shared_alias_2 " +
                        "right join babyfish_shared_alias_0.departments babyfish_not_shared_alias_3 " +
                        "inner join babyfish_not_shared_alias_3.offices babyfish_shared_alias_4 " +
                        "where " +
                        "babyfish_shared_alias_2.name = :babyfish_literal_0 and " +
                        "babyfish_shared_alias_4.name = :babyfish_literal_1",
                        get(template, JPQL_FIELD));
            }
            assertImplictParameters(
                    template,
                    "babyfish_literal_0", "jim",
                    "babyfish_literal_1", "pandora");
            template.createQuery(this.entityManager).getResultList();
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
            XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
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
            
            QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
            Assert.assertEquals(
                    "select " +
                    "babyfish_shared_alias_0, " +
                    "babyfish_shared_alias_1, " +
                    "babyfish_shared_alias_2, " +
                    "babyfish_shared_alias_3 " +
                    "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                    "inner join babyfish_shared_alias_0.departments babyfish_shared_alias_1 " +
                    "inner join babyfish_shared_alias_1.employees babyfish_shared_alias_2 " +
                    "inner join babyfish_shared_alias_1.offices babyfish_shared_alias_3 " +
                    "where " +
                    "babyfish_shared_alias_2.name = :babyfish_literal_0 and " +
                    "babyfish_shared_alias_3.name = :babyfish_literal_1",
                    get(template, JPQL_FIELD));
            assertImplictParameters(
                    template,
                    "babyfish_literal_0", "jim",
                    "babyfish_literal_1", "pandora");
            template.createQuery(this.entityManager).getResultList();
        }
    }
    
    @Test
    public void testOptionalJoin() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        JoinMode[] joinModes = new JoinMode[] {
                JoinMode.OPTIONALLY_MERGE_EXISTS,
                JoinMode.OPTIONALLY_CREATE_NEW
        };
        for (int i = 0; i < joinModes.length; i++) {
            XCriteriaQuery<Company> cq = cb.createQuery(Company.class);
            XRoot<Company> company = cq.from(Company.class);
            company
            .join(Company_.departments)
            .join(Department_.employees, joinModes[i]);
            QueryTemplate<Company> template = entityManagerFactory.createQueryTemplate(cq);
            Assert.assertEquals(
                    "select " +
                    "babyfish_shared_alias_0 " +
                    "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0",
                    get(template, JPQL_FIELD));
            assertImplictParameters(template);
            template.createQuery(this.entityManager).getResultList();
        }
    }
    
    @Test
    public void testRequiredJoin() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        JoinMode[] joinModes = new JoinMode[] {
                JoinMode.REQUIRED_TO_MERGE_EXISTS,
                JoinMode.REQUIRED_TO_CREATE_NEW
        };
        for (int i = 0; i < joinModes.length; i++) {
            XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
            XRoot<Company> company = cq.from(Company.class);
            company
            .join(Company_.departments)
            .join(Department_.employees, joinModes[i]);
            QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
            if (i == 0) {
                Assert.assertEquals(
                        "select " +
                        "babyfish_shared_alias_0, babyfish_shared_alias_1, babyfish_shared_alias_2 " +
                        "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                        "inner join babyfish_shared_alias_0.departments babyfish_shared_alias_1 " +
                        "inner join babyfish_shared_alias_1.employees babyfish_shared_alias_2",
                        get(template, JPQL_FIELD));
            } else {
                Assert.assertEquals(
                        "select " +
                        "babyfish_shared_alias_0, babyfish_shared_alias_1, babyfish_not_shared_alias_2 " +
                        "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                        "inner join babyfish_shared_alias_0.departments babyfish_shared_alias_1 " +
                        "inner join babyfish_shared_alias_1.employees babyfish_not_shared_alias_2",
                        get(template, JPQL_FIELD));
            }
            assertImplictParameters(template);
            template.createQuery(this.entityManager).getResultList();
        }
    }
    
    @Test
    public void testMergeExistingJoinImplicitly() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
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
        
        QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select " +
                "babyfish_shared_alias_0, " +
                "babyfish_shared_alias_1, " +
                "babyfish_shared_alias_2, " +
                "babyfish_shared_alias_3 " +
                "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                "inner join babyfish_shared_alias_0.departments babyfish_shared_alias_1 " +
                "inner join babyfish_shared_alias_1.employees babyfish_shared_alias_2 " +
                "inner join babyfish_shared_alias_1.offices babyfish_shared_alias_3 " +
                "where " +
                "babyfish_shared_alias_2.name = :babyfish_literal_0 and " +
                "babyfish_shared_alias_3.name = :babyfish_literal_1",
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "jim",
                "babyfish_literal_1", "pandora");
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testMergeSingularFetchFailed() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
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
        
        QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select " +
                "babyfish_shared_alias_0, " +
                "babyfish_not_shared_alias_1, " +
                "babyfish_not_shared_alias_2 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "left join babyfish_shared_alias_0.department babyfish_not_shared_alias_1 " +
                "right join babyfish_not_shared_alias_1.company babyfish_not_shared_alias_2 " +
                "right join fetch babyfish_shared_alias_0.department babyfish_shared_alias_3 " +
                "left join fetch babyfish_shared_alias_3.company babyfish_shared_alias_4 " +
                "where babyfish_not_shared_alias_2.name = :babyfish_literal_0", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template, "babyfish_literal_0", "oracle");
        template.createQuery(this.entityManager).getResultList();
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
        
        QueryTemplate<Employee> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "inner join fetch babyfish_shared_alias_0.department babyfish_shared_alias_1 " +
                "inner join fetch babyfish_shared_alias_1.company babyfish_shared_alias_2 " +
                "where babyfish_shared_alias_2.name = :babyfish_literal_0", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template, "babyfish_literal_0", "oracle");
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testMergePluralFetchFailedBecauseOfJoinMode() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
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
        
        QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select " +
                "babyfish_shared_alias_0, " +
                "babyfish_not_shared_alias_1, " +
                "babyfish_not_shared_alias_2, " +
                "babyfish_not_shared_alias_3, " +
                "babyfish_not_shared_alias_4 " +
                "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                "right join babyfish_shared_alias_0.departments babyfish_not_shared_alias_1 " +
                "right join babyfish_not_shared_alias_1.employees babyfish_not_shared_alias_2 " +
                "right join babyfish_shared_alias_0.departments babyfish_not_shared_alias_3 " +
                "right join babyfish_not_shared_alias_3.offices babyfish_not_shared_alias_4 " +
                "left join fetch babyfish_shared_alias_0.departments babyfish_shared_alias_5 " +
                "left join fetch babyfish_shared_alias_5.employees babyfish_shared_alias_6 " +
                "left join fetch babyfish_shared_alias_5.offices babyfish_shared_alias_7 " +
                "where " +
                "babyfish_not_shared_alias_2.name = :babyfish_literal_0 and " +
                "babyfish_not_shared_alias_4.name = :babyfish_literal_1",
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "jim",
                "babyfish_literal_1", "pandora");
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testMergePluralFetchFailedBecauseOfCollectionFetchType() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
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
        
        QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select " +
                "babyfish_shared_alias_0, " +
                "babyfish_shared_alias_1, " +
                "babyfish_shared_alias_2, " +
                "babyfish_shared_alias_3 " +
                "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                "right join babyfish_shared_alias_0.departments babyfish_shared_alias_1 " +
                "right join babyfish_shared_alias_1.employees babyfish_shared_alias_2 " +
                "right join babyfish_shared_alias_1.offices babyfish_shared_alias_3 " +
                "left join fetch babyfish_shared_alias_0.departments babyfish_shared_alias_4 " +
                "left join fetch babyfish_shared_alias_4.employees babyfish_shared_alias_5 " +
                "left join fetch babyfish_shared_alias_4.offices babyfish_shared_alias_6 " +
                "where " +
                "babyfish_shared_alias_2.name = :babyfish_literal_0 and " +
                "babyfish_shared_alias_3.name = :babyfish_literal_1",
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "jim",
                "babyfish_literal_1", "pandora");
        template.createQuery(this.entityManager).getResultList();
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
        
        QueryTemplate<Company> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                "inner join fetch babyfish_shared_alias_0.departments babyfish_shared_alias_1 " +
                "inner join fetch babyfish_shared_alias_1.employees babyfish_shared_alias_2 " +
                "inner join fetch babyfish_shared_alias_1.offices babyfish_shared_alias_3 " +
                "where " +
                "babyfish_shared_alias_2.name = :babyfish_literal_0 and " +
                "babyfish_shared_alias_3.name = :babyfish_literal_1",
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "jim",
                "babyfish_literal_1", "pandora");
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testMergeAndTheCreateNewByJoinMode() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
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
        
        QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0, " +
                "babyfish_not_shared_alias_2, " +
                "babyfish_not_shared_alias_3 " +
                "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                "inner join fetch babyfish_shared_alias_0.departments babyfish_shared_alias_1 " +
                "right join babyfish_shared_alias_1.employees babyfish_not_shared_alias_2 " +
                "right join babyfish_shared_alias_1.offices babyfish_not_shared_alias_3 " +
                "left join fetch babyfish_shared_alias_1.employees babyfish_shared_alias_4 " +
                "left join fetch babyfish_shared_alias_1.offices babyfish_shared_alias_5 " +
                "where " +
                "babyfish_not_shared_alias_2.name = :babyfish_literal_0 and " +
                "babyfish_not_shared_alias_3.name = :babyfish_literal_1",
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "jim",
                "babyfish_literal_1", "pandora");
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testMergeAndTheCreateNewByCollectionFetchType() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
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
        
        QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select " +
                "babyfish_shared_alias_0, " +
                "babyfish_shared_alias_2, " +
                "babyfish_shared_alias_3 " +
                "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                "inner join fetch babyfish_shared_alias_0.departments babyfish_shared_alias_1 " +
                "right join babyfish_shared_alias_1.employees babyfish_shared_alias_2 " +
                "right join babyfish_shared_alias_1.offices babyfish_shared_alias_3 " +
                "left join fetch babyfish_shared_alias_1.employees babyfish_shared_alias_4 " +
                "left join fetch babyfish_shared_alias_1.offices babyfish_shared_alias_5 " +
                "where " +
                "babyfish_shared_alias_2.name = :babyfish_literal_0 and " +
                "babyfish_shared_alias_3.name = :babyfish_literal_1",
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "jim",
                "babyfish_literal_1", "pandora");
        template.createQuery(this.entityManager).getResultList();
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
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        XRoot<Company> company = cq.from(Company.class);
        XJoin<Company, Department> department = company.join(Company_.departments, JoinMode.OPTIONALLY_CREATE_NEW);
        department.on(cb.like(department.get(Department_.name), "%a%"));
        QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0, babyfish_not_shared_alias_1 " +
                "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                "inner join babyfish_shared_alias_0.departments babyfish_not_shared_alias_1 " +
                "on babyfish_not_shared_alias_1.name like :babyfish_literal_0",
                get(template, JPQL_FIELD));
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testEntityUsed() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        XRoot<Company> company = cq.from(Company.class);
        XJoin<Company, Department> department = company.join(Company_.departments);
        department.join(Department_.employees);
        XJoin<Department, Office> office = department.join(Department_.offices);
        
        QueryTemplate<Tuple> template;
        template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0",
                get(template, JPQL_FIELD));
        assertImplictParameters(template);
        template.createQuery(this.entityManager).getResultList();
        
        cq.where(cb.equal(office.get(Office_.name), "pandora"));
        template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0, babyfish_shared_alias_1, babyfish_shared_alias_2 " +
                "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                "inner join babyfish_shared_alias_0.departments babyfish_shared_alias_1 " +
                "inner join babyfish_shared_alias_1.offices babyfish_shared_alias_2 " +
                "where babyfish_shared_alias_2.name = :babyfish_literal_0",
                get(template, JPQL_FIELD));
        assertImplictParameters(template, "babyfish_literal_0", "pandora");
        template.createQuery(this.entityManager).getResultList();
        
        company
        .fetch(Company_.departments, CollectionFetchType.PARTIAL)
        .fetch(Department_.employees, CollectionFetchType.PARTIAL)
        .fetch(Employee_.annualLeaves, CollectionFetchType.PARTIAL);
        template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select " +
                "babyfish_shared_alias_0, " +
                "babyfish_shared_alias_4 " +
                "from org.babyfish.hibernate.jpacriteria.Company babyfish_shared_alias_0 " +
                "inner join fetch babyfish_shared_alias_0.departments babyfish_shared_alias_1 " +
                "inner join fetch babyfish_shared_alias_1.employees babyfish_shared_alias_2 " +
                "inner join fetch babyfish_shared_alias_2.annualLeaves babyfish_shared_alias_3 " +
                "inner join babyfish_shared_alias_1.offices babyfish_shared_alias_4 " +         
                "where babyfish_shared_alias_4.name = :babyfish_literal_0",
                get(template, JPQL_FIELD));
        assertImplictParameters(template, "babyfish_literal_0", "pandora");
        template.createQuery(this.entityManager).getResultList();
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
        /*
         * Left reference join will always be optimized successfully
         */
        try (DbSchemaStrictScope scope = new DbSchemaStrictScope(entityManagerFactory, false)) {
            XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
            XCriteriaQuery<Long> cq = cb.createQuery(Long.class);
            XRoot<AnnualLeave> annualLeave = cq.from(AnnualLeave.class);
            cq.select(annualLeave
                    .join(AnnualLeave_.employee, JoinType.LEFT)
                    .join(Employee_.department, JoinType.LEFT)
                    .join(Department_.company, JoinType.LEFT)
                    .get(Company_.id));
            
            QueryTemplate<Long> template = entityManagerFactory.createQueryTemplate(cq);
            Assert.assertEquals(
                    "select babyfish_shared_alias_2.company.id " +
                    "from org.babyfish.hibernate.jpacriteria.AnnualLeave babyfish_shared_alias_0 " +
                    "left join babyfish_shared_alias_0.employee babyfish_shared_alias_1 " +
                    "left join babyfish_shared_alias_1.department babyfish_shared_alias_2", 
                    get(template, JPQL_FIELD));
            assertImplictParameters(template);
            template.createQuery(this.entityManager).getResultList();
        }
    }
    
    @Test
    public void testReplaceNonNullInnerJoinToGetBecauseId() {
        /*
         * Optimize failed:
         * 
         * When db schema is NOT strict, NonNull ManyToOne does NOT means NonNull foreign key,
         * last inner join can NOT be replaced to be getter
         */
        try (DbSchemaStrictScope scope = new DbSchemaStrictScope(entityManagerFactory, false)) {
            XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
            XCriteriaQuery<Long> cq = cb.createQuery(Long.class);
            XRoot<AnnualLeave> annualLeave = cq.from(AnnualLeave.class);
            cq.select(annualLeave
                    .join(AnnualLeave_.employee, JoinType.LEFT)
                    .join(Employee_.department, JoinType.LEFT)
                    .join(Department_.company, JoinType.INNER)
                    .get(Company_.id));
            
            QueryTemplate<Long> template = entityManagerFactory.createQueryTemplate(cq);
            Assert.assertEquals(
                    "select babyfish_shared_alias_3.id "
                    + "from org.babyfish.hibernate.jpacriteria.AnnualLeave babyfish_shared_alias_0 "
                    + "left join babyfish_shared_alias_0.employee babyfish_shared_alias_1 "
                    + "left join babyfish_shared_alias_1.department babyfish_shared_alias_2 "
                    + "inner join babyfish_shared_alias_2.company babyfish_shared_alias_3", 
                    get(template, JPQL_FIELD));
            assertImplictParameters(template);
            template.createQuery(this.entityManager).getResultList();
        }
        
        /*
         * Optimize successed:
         * 
         * When db schema is strict, NonNull ManyToOne does means NonNull foreign key,
         * last inner join can be replaced to be getter
         */
        try (DbSchemaStrictScope scope = new DbSchemaStrictScope(entityManagerFactory, true)) {
            XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
            XCriteriaQuery<Long> cq = cb.createQuery(Long.class);
            XRoot<AnnualLeave> annualLeave = cq.from(AnnualLeave.class);
            cq.select(annualLeave
                    .join(AnnualLeave_.employee, JoinType.LEFT)
                    .join(Employee_.department, JoinType.LEFT)
                    .join(Department_.company, JoinType.INNER)
                    .get(Company_.id));
            
            QueryTemplate<Long> template = entityManagerFactory.createQueryTemplate(cq);
            Assert.assertEquals(
                    "select babyfish_shared_alias_2.company.id " +
                    "from org.babyfish.hibernate.jpacriteria.AnnualLeave babyfish_shared_alias_0 " +
                    "left join babyfish_shared_alias_0.employee babyfish_shared_alias_1 " +
                    "left join babyfish_shared_alias_1.department babyfish_shared_alias_2", 
                    get(template, JPQL_FIELD));
            assertImplictParameters(template);
            template.createQuery(this.entityManager).getResultList();
        }
    }
    
    @Test
    public void testReplaceGetToJoinIncludeLeaf() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        XRoot<AnnualLeave> annualLeave = cq.from(AnnualLeave.class);
        cq
        .multiselect(
                annualLeave
                .get(AnnualLeave_.employee)
                .get(Employee_.department)
                .get(Department_.company),
                annualLeave
                .get(AnnualLeave_.employee)
                .get(Employee_.department)
                .get(Department_.company)
        );
        
        QueryTemplate<Object[]> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_3, babyfish_shared_alias_3 " +
                "from org.babyfish.hibernate.jpacriteria.AnnualLeave babyfish_shared_alias_0 " +
                "inner join babyfish_shared_alias_0.employee babyfish_shared_alias_1 " +
                "inner join babyfish_shared_alias_1.department babyfish_shared_alias_2 " +
                "inner join babyfish_shared_alias_2.company babyfish_shared_alias_3",
                get(template, JPQL_FIELD));
        assertImplictParameters(template);
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testReplaceGetToJoinExcludeLeaf() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        XRoot<Department> department = cq.from(Department.class);
        XSubquery<Department> subq = cq.subquery(Department.class);
        XRoot<Employee> subEmployee = subq.from(Employee.class);
        subq
        .select(subEmployee.get(Employee_.department))
        .where(
                cb.like(
                        subEmployee.get(Employee_.name), 
                        "%peter%"
                )
        );
        cq
        .multiselect(
                department.get(Department_.company).get(Company_.id),
                cb.countDistinct(department)
        )
        .groupBy(department.get(Department_.company))
        .where(cb.in(department).value(subq))
        .orderBy(cb.asc(department.get(Department_.company)));
        
        QueryTemplate<Object[]> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0.company.id, count(distinct babyfish_shared_alias_0) " +
                "from org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "where babyfish_shared_alias_0 in(" +
                "select babyfish_shared_alias_1.department " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_1 " +
                "where babyfish_shared_alias_1.name like :babyfish_literal_0" +
                ") " +
                "group by babyfish_shared_alias_0.company " +
                "order by babyfish_shared_alias_0.company asc", 
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "%peter%");
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testReplaceGetToJoinIncludeLeafAndExcludeLear() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        Path<Department> department = employee.get(Employee_.department);
        cq.select(department).orderBy(cb.asc(department));
        
        QueryTemplate<Department> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_1 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "inner join babyfish_shared_alias_0.department babyfish_shared_alias_1 " +
                "order by babyfish_shared_alias_0.department asc", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template);
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testFetchAllOnMergeExistsJoin() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        XRoot<Department> department = cq.from(Department.class);
        department.join(Department_.employees, JoinMode.REQUIRED_TO_MERGE_EXISTS).join(Employee_.annualLeaves, JoinMode.REQUIRED_TO_MERGE_EXISTS);
        department.fetch(Department_.employees, CollectionFetchType.ALL).fetch(Employee_.annualLeaves, CollectionFetchType.ALL);
        QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0, babyfish_shared_alias_1, babyfish_shared_alias_2 " +
                "from org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "inner join babyfish_shared_alias_0.employees babyfish_shared_alias_1 " +
                "inner join babyfish_shared_alias_1.annualLeaves babyfish_shared_alias_2 " +
                "inner join fetch babyfish_shared_alias_0.employees babyfish_shared_alias_3 " +
                "inner join fetch babyfish_shared_alias_3.annualLeaves babyfish_shared_alias_4", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template);
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testFetchAllOnCreateNewJoin() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        XRoot<Department> department = cq.from(Department.class);
        department.join(Department_.employees, JoinMode.REQUIRED_TO_CREATE_NEW).join(Employee_.annualLeaves, JoinMode.REQUIRED_TO_CREATE_NEW);
        department.fetch(Department_.employees, CollectionFetchType.ALL).fetch(Employee_.annualLeaves, CollectionFetchType.ALL);
        QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select " +
                "babyfish_shared_alias_0, " +
                "babyfish_not_shared_alias_1, " +
                "babyfish_not_shared_alias_2 " +
                "from org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "inner join babyfish_shared_alias_0.employees babyfish_not_shared_alias_1 " +
                "inner join babyfish_not_shared_alias_1.annualLeaves babyfish_not_shared_alias_2 " +
                "inner join fetch babyfish_shared_alias_0.employees babyfish_shared_alias_3 " +
                "inner join fetch babyfish_shared_alias_3.annualLeaves babyfish_shared_alias_4", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template);
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testFetchPartialOnMergeExistsJoin() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        department.join(Department_.employees, JoinMode.REQUIRED_TO_MERGE_EXISTS).join(Employee_.annualLeaves, JoinMode.REQUIRED_TO_MERGE_EXISTS);
        department.fetch(Department_.employees, CollectionFetchType.PARTIAL).fetch(Employee_.annualLeaves, CollectionFetchType.PARTIAL);
        QueryTemplate<Department> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "inner join fetch babyfish_shared_alias_0.employees babyfish_shared_alias_1 " +
                "inner join fetch babyfish_shared_alias_1.annualLeaves babyfish_shared_alias_2", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template);
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testFetchPartialOnCreateNewJoin() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        XRoot<Department> department = cq.from(Department.class);
        department.join(Department_.employees, JoinMode.REQUIRED_TO_CREATE_NEW).join(Employee_.annualLeaves, JoinMode.REQUIRED_TO_CREATE_NEW);
        department.fetch(Department_.employees, CollectionFetchType.PARTIAL).fetch(Employee_.annualLeaves, CollectionFetchType.PARTIAL);
        QueryTemplate<Tuple> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select " +
                "babyfish_shared_alias_0, " +
                "babyfish_not_shared_alias_1, " +
                "babyfish_not_shared_alias_2 " +
                "from org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "inner join babyfish_shared_alias_0.employees babyfish_not_shared_alias_1 " +
                "inner join babyfish_not_shared_alias_1.annualLeaves babyfish_not_shared_alias_2 " +
                "inner join fetch babyfish_shared_alias_0.employees babyfish_shared_alias_3 " +
                "inner join fetch babyfish_shared_alias_3.annualLeaves babyfish_shared_alias_4", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template);
        template.createQuery(this.entityManager).getResultList();
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
        
        QueryTemplate<Department> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "where " +
                "length(babyfish_shared_alias_0.name) >= ?1 and " +
                "babyfish_shared_alias_0.name like :departmentName and " +
                "length(babyfish_shared_alias_0.name) <= ?2", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template);
        template
        .createQuery(this.entityManager)
        .setParameter(1, 5)
        .setParameter("departmentName", "%M%")
        .setParameter(2, 10)
        .getResultList();
    }
    
    @Test
    public void testCompoundPredicate() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        Predicate predicate = 
                cb.or(
                        cb.equal(department.get(Department_.name), "pandora"),
                        cb.equal(department.get(Department_.name), "athena"),
                        cb.equal(department.get(Department_.name), "aphrodite")
                );
        cq.where(predicate);
        
        QueryTemplate<Department> template;
        
        template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "where " +
                "babyfish_shared_alias_0.name = :babyfish_literal_0 or " +
                "babyfish_shared_alias_0.name = :babyfish_literal_1 or " +
                "babyfish_shared_alias_0.name = :babyfish_literal_2", 
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "pandora",
                "babyfish_literal_1", "athena",
                "babyfish_literal_2", "aphrodite");
        template.createQuery(this.entityManager).getResultList();
        
        predicate = predicate.not();
        cq.where(predicate);
        
        template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "where " +
                "babyfish_shared_alias_0.name != :babyfish_literal_0 and " +
                "babyfish_shared_alias_0.name != :babyfish_literal_1 and " +
                "babyfish_shared_alias_0.name != :babyfish_literal_2", 
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "pandora",
                "babyfish_literal_1", "athena",
                "babyfish_literal_2", "aphrodite");
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testExistsPredicate() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        XSubquery<Employee> subq = cq.subquery(Employee.class);
        XRoot<Employee> employee = subq.from(Employee.class);
        subq.where(
                cb.equal(employee.get(Employee_.department), department),
                cb.like(employee.get(Employee_.name), "jim%")
        );
        cq.where(cb.exists(subq));
        
        QueryTemplate<Department> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "where exists(" +
                "select babyfish_shared_alias_1 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_1 " +
                "where " +
                "babyfish_shared_alias_1.department = babyfish_shared_alias_0 and " +
                "babyfish_shared_alias_1.name like :babyfish_literal_0" +
                ")",
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "jim%");
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testSubqueryComparisonModifiers() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        
        XSubquery<Integer> subq1 = cq.subquery(Integer.class);
        XRoot<Employee> subEmployee1 = subq1.from(Employee.class);
        subq1.where(
                cb.equal(
                        subEmployee1.get(Employee_.department).get(Department_.name), 
                        "pandora"
                )
        );
        subq1.select(subEmployee1.get(Employee_.salary));
        
        XSubquery<Integer> subq2 = cq.subquery(Integer.class);
        XRoot<Employee> subEmployee2 = subq2.from(Employee.class);
        subq2.where(
                cb.equal(
                        subEmployee2.get(Employee_.department).get(Department_.name), 
                        "athena"
                )
        );
        subq2.select(subEmployee2.get(Employee_.salary));
        
        XSubquery<Integer> subq3 = cq.subquery(Integer.class);
        XRoot<Employee> subEmployee3 = subq3.from(Employee.class);
        subq3.where(
                cb.equal(
                        subEmployee3.get(Employee_.department).get(Department_.name), 
                        "aphrodite"
                )
        );
        subq3.select(subEmployee3.get(Employee_.salary));
        
        cq.where(
                cb.ge(
                        employee.get(Employee_.salary), 
                        cb.all(subq1)
                ),
                cb.ge(
                        employee.get(Employee_.salary), 
                        cb.any(subq2)
                ),
                cb.ge(
                        employee.get(Employee_.salary), 
                        cb.some(subq3)
                )
        );
        
        QueryTemplate<Employee> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "where " +
                "babyfish_shared_alias_0.salary >= all(" +
                "select babyfish_shared_alias_1.salary " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_1 " +
                "inner join babyfish_shared_alias_1.department babyfish_shared_alias_2 " +
                "where babyfish_shared_alias_2.name = :babyfish_literal_0" +
                ") and " +
                "babyfish_shared_alias_0.salary >= any(" +
                "select babyfish_shared_alias_3.salary " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_3 " +
                "inner join babyfish_shared_alias_3.department babyfish_shared_alias_4 " +
                "where babyfish_shared_alias_4.name = :babyfish_literal_1" +
                ") and " +
                "babyfish_shared_alias_0.salary >= some(" +
                "select babyfish_shared_alias_5.salary " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_5 " +
                "inner join babyfish_shared_alias_5.department babyfish_shared_alias_6 " +
                "where babyfish_shared_alias_6.name = :babyfish_literal_2" +
                ")", 
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "pandora",
                "babyfish_literal_1", "athena",
                "babyfish_literal_2", "aphrodite");
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testInPredicateByNothing() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        cq.where(cb.in(employee.get(Employee_.id)));
        
        QueryTemplate<Employee> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "where 1 = 0", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template);
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testInPredicateBySingleSubquery() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        XSubquery<Long> subq = cq.subquery(Long.class);
        XRoot<Employee> subEmployee = subq.from(Employee.class);
        subq.where(
                cb.equal(
                        subEmployee.get(Employee_.department).get(Department_.name), 
                        "pandora"
                )
        );
        subq.select(subEmployee.get(Employee_.id));
        cq.where(cb.in(employee.get(Employee_.id)).value(subq));
        
        QueryTemplate<Employee> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "where babyfish_shared_alias_0.id in(" +
                "select babyfish_shared_alias_1.id " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_1 " +
                "inner join babyfish_shared_alias_1.department babyfish_shared_alias_2 " +
                "where babyfish_shared_alias_2.name = :babyfish_literal_0" +
                ")", 
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "pandora");
        template.createQuery(this.entityManager).getResultList();
    }

    @Test
    public void testInPredicateBySingleValue() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        cq.where(cb.in(employee.get(Employee_.id)).value(4L));
        
        QueryTemplate<Employee> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "where babyfish_shared_alias_0.id = :babyfish_literal_0", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template,
                "babyfish_literal_0", 4L);
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testInPredicateByLiteralExpressions() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        cq.where(cb.in(employee.get(Employee_.id)).value(1L).value(2L).value(3L));
        
        QueryTemplate<Employee> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "where babyfish_shared_alias_0.id in(" +
                ":babyfish_literal_0)", 
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", MACollections.wrapLong(1L, 2L, 3L));
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testInPredicateByMixedExpressions() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        XSubquery<Long> subq = cq.subquery(Long.class);
        XRoot<Employee> subEmployee = subq.from(Employee.class);
        subq.where(
                cb.equal(
                        subEmployee.get(Employee_.department).get(Department_.name), 
                        "pandora"
                )
        );
        subq.select(subEmployee.get(Employee_.id));
        cq.where(
                cb
                .in(employee.get(Employee_.id))
                .value(subq)
                .value(1L)
                .value(2L)
                .value(3L)
        );
        
        QueryTemplate<Employee> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "where babyfish_shared_alias_0.id in(" +
                "(select babyfish_shared_alias_1.id " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_1 " +
                "inner join babyfish_shared_alias_1.department babyfish_shared_alias_2 " +
                "where babyfish_shared_alias_2.name = :babyfish_literal_0), " +
                ":babyfish_literal_1, " +
                ":babyfish_literal_2, " +
                ":babyfish_literal_3)", 
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "pandora",
                "babyfish_literal_1", 1L,
                "babyfish_literal_2", 2L,
                "babyfish_literal_3", 3L);
        
        //TODO: hibernate dosenot support this mode, the last 3 parameters is ignored by hibernate
        //template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testSubqueryWithNothing() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        XSubquery<Integer> subq = cq.subquery(Integer.class);
        XRoot<Employee> subEmployee = subq.from(Employee.class);
        subq.select(cb.max(subEmployee.get(Employee_.salary)));
        cq.where(cb.equal(employee.get(Employee_.salary), subq));
        
        QueryTemplate<Employee> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "where babyfish_shared_alias_0.salary = (" +
                "select max(babyfish_shared_alias_1.salary) " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_1)", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template);
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testSubqueryWithNullif() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        XSubquery<Integer> subq = cq.subquery(Integer.class);
        XRoot<Employee> subEmployee = subq.from(Employee.class);
        subq.select(cb.max(subEmployee.get(Employee_.salary)));
        cq.where(cb.equal(employee.get(Employee_.salary), cb.nullif(subq, 0)));
        
        QueryTemplate<Employee> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "where babyfish_shared_alias_0.salary = " +
                "nullif(" +
                "(select max(babyfish_shared_alias_1.salary) " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_1), " +
                ":babyfish_literal_0)", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template, "babyfish_literal_0", 0);
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testAggregation() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        XRoot<Employee> employee = cq.from(Employee.class);
        cq.multiselect(
                cb.count(employee),
                cb.count(employee.get(Employee_.department)),
                cb.countDistinct(employee.get(Employee_.department)),
                cb.sum(employee.get(Employee_.salary)),
                cb.sumAsLong(employee.get(Employee_.salary)),
                cb.min(employee.get(Employee_.salary)),
                cb.max(employee.get(Employee_.salary)),
                cb.least(employee.get(Employee_.birthday)),
                cb.greatest(employee.get(Employee_.birthday)),
                cb.avg(employee.get(Employee_.salary))
        );
        
        QueryTemplate<Object[]> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select " +
                "count(babyfish_shared_alias_0), " +
                "count(babyfish_shared_alias_0.department), " +
                "count(distinct babyfish_shared_alias_0.department), " +
                "sum(babyfish_shared_alias_0.salary), " +
                "sum(babyfish_shared_alias_0.salary), " +
                "min(babyfish_shared_alias_0.salary), " +
                "max(babyfish_shared_alias_0.salary), " +
                "min(babyfish_shared_alias_0.birthday), " +
                "max(babyfish_shared_alias_0.birthday), " +
                "avg(babyfish_shared_alias_0.salary) " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template);
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testGroupByAndHaving() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        XRoot<Employee> employee = cq.from(Employee.class);
        cq.groupBy(employee.get(Employee_.department));
        cq.having(
                cb.ge(cb.count(employee), 5L),
                cb.le(cb.count(employee), 10L)
        );
        cq.multiselect(
                cb.count(employee),
                cb.sum(employee.get(Employee_.salary)),
                cb.sumAsLong(employee.get(Employee_.salary)),
                cb.min(employee.get(Employee_.salary)),
                cb.max(employee.get(Employee_.salary)),
                cb.least(employee.get(Employee_.birthday)),
                cb.greatest(employee.get(Employee_.birthday)),
                cb.avg(employee.get(Employee_.salary))
        );
        
        QueryTemplate<Object[]> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select " +
                "count(babyfish_shared_alias_0), " +
                "sum(babyfish_shared_alias_0.salary), " +
                "sum(babyfish_shared_alias_0.salary), " +
                "min(babyfish_shared_alias_0.salary), " +
                "max(babyfish_shared_alias_0.salary), " +
                "min(babyfish_shared_alias_0.birthday), " +
                "max(babyfish_shared_alias_0.birthday), " +
                "avg(babyfish_shared_alias_0.salary) " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "group by babyfish_shared_alias_0.department " +
                "having " +
                "count(babyfish_shared_alias_0) >= :babyfish_literal_0 and " +
                "count(babyfish_shared_alias_0) <= :babyfish_literal_1", 
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", 5L,
                "babyfish_literal_1", 10L);
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testTrim() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        XRoot<Employee> employee = cq.from(Employee.class);
        cq.multiselect(
            cb.trim(employee.get(Employee_.name)),
            cb.trim(Trimspec.BOTH, employee.get(Employee_.name)),
            cb.trim(Trimspec.LEADING, employee.get(Employee_.name)),
            cb.trim(Trimspec.TRAILING, '*', employee.get(Employee_.name))
        );
        
        QueryTemplate<Object[]> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select " +
                "trim(both ' ' from babyfish_shared_alias_0.name), " +
                "trim(both ' ' from babyfish_shared_alias_0.name), " +
                "trim(leading ' ' from babyfish_shared_alias_0.name), " +
                "trim(trailing :babyfish_literal_0 from babyfish_shared_alias_0.name) " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0",
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", '*');
        //TODO: HQLDB does not support it?
        //template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testOtherExpressions() throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        XRoot<Employee> employee = cq.from(Employee.class);
        cq.where(
                cb.equal(
                        cb
                        .concat() //the concat for one word
                        .value(employee.get(Employee_.department).get(Department_.name)),
                        "pandora"
                ),
                cb.lessThanOrEqualTo( //not use le, to test lessThanOrEqual to for Number
                        cb.length(
                                cb.concat( //the concat for two words
                                        employee.get(Employee_.department).get(Department_.name), 
                                        employee.get(Employee_.name)
                                )
                        ),
                        30
                ),
                cb.or(
                        cb.lessThanOrEqualTo(
                                employee.get(Employee_.birthday),
                                dateFormat.parse("1980-01-01")
                        ),
                        cb.greaterThanOrEqualTo(
                                employee.get(Employee_.birthday),
                                dateFormat.parse("1990-01-01")
                        )
                )
        );
        cq.multiselect(
                employee,
                cb.concat() //the concat for 3 words
                .value(employee.get(Employee_.department).get(Department_.name))
                .value(" : ")
                .value(employee.get(Employee_.name)),
                cb.prod(
                        cb.toDouble(employee.get(Employee_.salary)),
                        cb.toDouble(
                                cb.sum(
                                        cb.literal(1),
                                        0.1
                                )
                        )
                ),
                employee.get(Employee_.birthday).isNotNull(),
                cb.selectCase()
                .when(
                        cb.lt(
                                employee.get(Employee_.salary),
                                3000
                        ),
                        "Level-1"
                )
                .when(
                        cb.lt(
                                employee.get(Employee_.salary),
                                6000
                        ),
                        "Level-2"
                )
                .when(
                        cb.lt(
                                employee.get(Employee_.salary),
                                9000
                        ),
                        "Level-3"
                )
                .otherwise("Level-Max"),
                cb
                .selectCase(employee.get(Employee_.salary))
                .when(0, cb.constant("is boss"))
                .otherwise(cb.constant("is not boss"))
        );
        
        QueryTemplate<Object[]> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select " +
                "babyfish_shared_alias_0, " +
                "concat(concat(babyfish_shared_alias_1.name, :babyfish_literal_0), babyfish_shared_alias_0.name), " +
                "babyfish_shared_alias_0.salary * (:babyfish_literal_1 + :babyfish_literal_2), " +
                "babyfish_shared_alias_0.birthday is not null, " +
                "case " +
                "when babyfish_shared_alias_0.salary < :babyfish_literal_3 then 'Level-1' " +
                "when babyfish_shared_alias_0.salary < :babyfish_literal_4 then 'Level-2' " +
                "when babyfish_shared_alias_0.salary < :babyfish_literal_5 then 'Level-3' " +
                "else 'Level-Max' " +
                "end, " +
                "case babyfish_shared_alias_0.salary " +
                "when 0 then 'is boss' " +
                "else 'is not boss' " +
                "end " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "inner join babyfish_shared_alias_0.department babyfish_shared_alias_1 " +
                "where " +
                "babyfish_shared_alias_1.name = :babyfish_literal_6 and " +
                "length(concat(babyfish_shared_alias_1.name, babyfish_shared_alias_0.name)) <= " +
                ":babyfish_literal_7 and " +
                "(" +
                "babyfish_shared_alias_0.birthday <= :babyfish_literal_8 or " +
                "babyfish_shared_alias_0.birthday >= :babyfish_literal_9" +
                ")",
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", " : ",
                "babyfish_literal_1", 1,
                "babyfish_literal_2", 0.1,
                "babyfish_literal_3", 3000,
                "babyfish_literal_4", 6000,
                "babyfish_literal_5", 9000,
                "babyfish_literal_6", "pandora",
                "babyfish_literal_7", 30,
                "babyfish_literal_8", dateFormat.parse("1980-01-01"),
                "babyfish_literal_9", dateFormat.parse("1990-01-01"));
        //TODO:
        //hibernate does not supportd:
        //template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testOrderBy() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        cq.orderBy(
                cb.asc(department.get(Department_.company).get(Company_.name)),
                cb.desc(
                        cb.length(department.get(Department_.name))
                )
        );
        
        QueryTemplate<Department> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "inner join babyfish_shared_alias_0.company babyfish_shared_alias_1 " +
                "order by " +
                "babyfish_shared_alias_1.name asc, " +
                "length(babyfish_shared_alias_0.name) desc", 
                get(template, JPQL_FIELD));
        assertImplictParameters(template);
        template.createQuery(this.entityManager).getResultList();
        
        //TODO: It looks like hibernate hql does not support to use subquery in the order list.
    }
    
    @Test
    public void testLargeInPredicate() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        XRoot<Department> department = cq.from(Department.class);
        In<String> inPredicate1 = cb.in(department.get(Department_.name));
        In<Long> inPredicate2 = cb.in(department.get(Department_.id));
        In<Long> inPredicate3 = cb.in(department.get(Department_.company).get(Company_.id));
        for (int i = 0; i < 19; i++) {
            inPredicate2.value((long)i);
            inPredicate3.value(
                    cb.prod(
                            cb.literal((long)i), 
                            cb.constant((long)i)
                    )
            );
        }
        cq.where(inPredicate1, inPredicate2, inPredicate3.not());
        
        QueryTemplate<Department> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "where " +
                "1 = 0 and " +
                "(" +
                "babyfish_shared_alias_0.id in(:babyfish_literal_0) or " +
                "babyfish_shared_alias_0.id in(:babyfish_literal_1)" +
                ")" +
                " and " +
                "(" +
                "babyfish_shared_alias_0.company.id not in(" +
                ":babyfish_literal_2 * 0L, " +
                ":babyfish_literal_3 * 1L, " +
                ":babyfish_literal_4 * 2L, " +
                ":babyfish_literal_5 * 3L, " +
                ":babyfish_literal_6 * 4L, " +
                ":babyfish_literal_7 * 5L, " +
                ":babyfish_literal_8 * 6L, " +
                ":babyfish_literal_9 * 7L, " +
                ":babyfish_literal_10 * 8L, " +
                ":babyfish_literal_11 * 9L) and " +
                "babyfish_shared_alias_0.company.id not in(" +
                ":babyfish_literal_12 * 10L, " +
                ":babyfish_literal_13 * 11L, " +
                ":babyfish_literal_14 * 12L, " +
                ":babyfish_literal_15 * 13L, " +
                ":babyfish_literal_16 * 14L, " +
                ":babyfish_literal_17 * 15L, " +
                ":babyfish_literal_18 * 16L, " +
                ":babyfish_literal_19 * 17L, " +
                ":babyfish_literal_20 * 18L" + 
                ")" +
                ")", 
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", new Long[] { 0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L },
                "babyfish_literal_1", new Long[] { 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L },
                "babyfish_literal_2", 0L,
                "babyfish_literal_3", 1L,
                "babyfish_literal_4", 2L,
                "babyfish_literal_5", 3L,
                "babyfish_literal_6", 4L,
                "babyfish_literal_7", 5L,
                "babyfish_literal_8", 6L,
                "babyfish_literal_9", 7L,
                "babyfish_literal_10", 8L,
                "babyfish_literal_11", 9L,
                "babyfish_literal_12", 10L,
                "babyfish_literal_13", 11L,
                "babyfish_literal_14", 12L,
                "babyfish_literal_15", 13L,
                "babyfish_literal_16", 14L,
                "babyfish_literal_17", 15L,
                "babyfish_literal_18", 16L,
                "babyfish_literal_19", 17L,
                "babyfish_literal_20", 18L);
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testComparison() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        XRoot<Employee> employee = cq.from(Employee.class);
        cq.where(
                cb.equal(employee.get(Employee_.salary), 5000),
                cb.notEqual(employee.get(Employee_.salary), 5000),
                cb.lessThan(employee.get(Employee_.salary), 5000),
                cb.lessThanOrEqualTo(employee.get(Employee_.salary), 5000),
                cb.greaterThan(employee.get(Employee_.salary), 5000),
                cb.greaterThanOrEqualTo(employee.get(Employee_.salary), 5000),
                cb.lt(employee.get(Employee_.salary), 5000),
                cb.le(employee.get(Employee_.salary), 5000),
                cb.gt(employee.get(Employee_.salary), 5000),
                cb.ge(employee.get(Employee_.salary), 5000),
                cb.equal(employee.get(Employee_.salary), 5000).not(),
                cb.notEqual(employee.get(Employee_.salary), 5000).not(),
                cb.lessThan(employee.get(Employee_.salary), 5000).not(),
                cb.lessThanOrEqualTo(employee.get(Employee_.salary), 5000).not(),
                cb.greaterThan(employee.get(Employee_.salary), 5000).not(),
                cb.greaterThanOrEqualTo(employee.get(Employee_.salary), 5000).not(),
                cb.lt(employee.get(Employee_.salary), 5000).not(),
                cb.le(employee.get(Employee_.salary), 5000).not(),
                cb.gt(employee.get(Employee_.salary), 5000).not(),
                cb.ge(employee.get(Employee_.salary), 5000).not()
        );
        QueryTemplate<Employee> template = entityManagerFactory.createQueryTemplate(cq);
        Assert.assertEquals(
                "select babyfish_shared_alias_0 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_0 " +
                "where " +
                "babyfish_shared_alias_0.salary = :babyfish_literal_0 and " +
                "babyfish_shared_alias_0.salary != :babyfish_literal_1 and " +
                "babyfish_shared_alias_0.salary < :babyfish_literal_2 and " +
                "babyfish_shared_alias_0.salary <= :babyfish_literal_3 and " +
                "babyfish_shared_alias_0.salary > :babyfish_literal_4 and " +
                "babyfish_shared_alias_0.salary >= :babyfish_literal_5 and " +
                "babyfish_shared_alias_0.salary < :babyfish_literal_6 and " +
                "babyfish_shared_alias_0.salary <= :babyfish_literal_7 and " +
                "babyfish_shared_alias_0.salary > :babyfish_literal_8 and " +
                "babyfish_shared_alias_0.salary >= :babyfish_literal_9 and " +
                "babyfish_shared_alias_0.salary != :babyfish_literal_10 and " +
                "babyfish_shared_alias_0.salary = :babyfish_literal_11 and " +
                "babyfish_shared_alias_0.salary >= :babyfish_literal_12 and " +
                "babyfish_shared_alias_0.salary > :babyfish_literal_13 and " +
                "babyfish_shared_alias_0.salary <= :babyfish_literal_14 and " +
                "babyfish_shared_alias_0.salary < :babyfish_literal_15 and " +
                "babyfish_shared_alias_0.salary >= :babyfish_literal_16 and " +
                "babyfish_shared_alias_0.salary > :babyfish_literal_17 and " +
                "babyfish_shared_alias_0.salary <= :babyfish_literal_18 and " +
                "babyfish_shared_alias_0.salary < :babyfish_literal_19",
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", 5000,
                "babyfish_literal_1", 5000,
                "babyfish_literal_2", 5000,
                "babyfish_literal_3", 5000,
                "babyfish_literal_4", 5000,
                "babyfish_literal_5", 5000,
                "babyfish_literal_6", 5000,
                "babyfish_literal_7", 5000,
                "babyfish_literal_8", 5000,
                "babyfish_literal_9", 5000,
                "babyfish_literal_10", 5000,
                "babyfish_literal_11", 5000,
                "babyfish_literal_12", 5000,
                "babyfish_literal_13", 5000,
                "babyfish_literal_14", 5000,
                "babyfish_literal_15", 5000,
                "babyfish_literal_16", 5000,
                "babyfish_literal_17", 5000,
                "babyfish_literal_18", 5000,
                "babyfish_literal_19", 5000);
        template.createQuery(this.entityManager).getResultList();
    }
    
    @Test
    public void testUpdateWithoutRestriction() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaUpdate<Department> cu = cb.createCriteriaUpdate(Department.class);
        XRoot<Department> department = cu.from(Department.class);
        cu.set(
                Department_.name, 
                cb
                .concat()
                .value(department.get(Department_.name))
                .value(":")
                .value("+")
        );
        
        QueryTemplate<Department> template = entityManagerFactory.createQueryTemplate(cu);
        Assert.assertEquals(
                "update org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "set babyfish_shared_alias_0.name = concat(concat(babyfish_shared_alias_0.name, :babyfish_literal_0), :babyfish_literal_1)",
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", ":",
                "babyfish_literal_1", "+");
                
        this.entityManager.getTransaction().begin();
        try {
            this.entityManager.createQuery(cu).executeUpdate();
        } catch (RuntimeException | Error ex) {
            this.entityManager.getTransaction().rollback();
            throw ex;
        }
        this.entityManager.getTransaction().commit();
    }
    
    @Test
    public void testUpdateWithRestriction() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaUpdate<Department> cu = cb.createCriteriaUpdate(Department.class);
        XRoot<Department> department = cu.from(Department.class);
        XSubquery<Employee> subCq = cu.subquery(Employee.class);
        XRoot<Employee> employee = subCq.from(Employee.class);
        subCq.where(
                cb.equal(employee.get(Employee_.department), department),
                cb.like(employee.get(Employee_.name), "%Tom%")
        );
        cu
        .set(
                Department_.name, 
                cb
                .concat()
                .value(department.get(Department_.name))
                .value(":")
                .value("+")
        )
        .where(cb.exists(subCq));
        
        QueryTemplate<Department> template = entityManagerFactory.createQueryTemplate(cu);
        Assert.assertEquals(
                "update org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "set babyfish_shared_alias_0.name = concat(concat(babyfish_shared_alias_0.name, :babyfish_literal_0), :babyfish_literal_1) " +
                "where exists(" +
                "select babyfish_shared_alias_1 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_1 " +
                "where " +
                "babyfish_shared_alias_1.department = babyfish_shared_alias_0 and " +
                "babyfish_shared_alias_1.name like :babyfish_literal_2" +
                ")",
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", ":",
                "babyfish_literal_1", "+",
                "babyfish_literal_2", "%Tom%");
                
        this.entityManager.getTransaction().begin();
        try {
            this.entityManager.createQuery(cu).executeUpdate();
        } catch (RuntimeException | Error ex) {
            this.entityManager.getTransaction().rollback();
            throw ex;
        }
        this.entityManager.getTransaction().commit();
    }
    
    @Test
    public void testDeleteWithoutRestriction() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaDelete<Department> cd = cb.createCriteriaDelete(Department.class);
        cd.from(Department.class);
        
        QueryTemplate<Department> template = entityManagerFactory.createQueryTemplate(cd);
        Assert.assertEquals(
                "delete from org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0",
                get(template, JPQL_FIELD));
        assertImplictParameters(template);
                
        this.entityManager.getTransaction().begin();
        try {
            this.entityManager.createQuery(cd).executeUpdate();
        } catch (RuntimeException | Error ex) {
            this.entityManager.getTransaction().rollback();
            throw ex;
        }
        this.entityManager.getTransaction().commit();
    }
    
    @Test
    public void testDeleteWithRestriction() {
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaDelete<Department> cd = cb.createCriteriaDelete(Department.class);
        XRoot<Department> department = cd.from(Department.class);
        XSubquery<Employee> subCq = cd.subquery(Employee.class);
        XRoot<Employee> employee = subCq.from(Employee.class);
        subCq.where(
                cb.equal(employee.get(Employee_.department), department),
                cb.like(employee.get(Employee_.name), "%Tom%")
        );
        cd.where(cb.exists(subCq));
        
        QueryTemplate<Department> template = entityManagerFactory.createQueryTemplate(cd);
        Assert.assertEquals(
                "delete from org.babyfish.hibernate.jpacriteria.Department babyfish_shared_alias_0 " +
                "where exists(" +
                "select babyfish_shared_alias_1 " +
                "from org.babyfish.hibernate.jpacriteria.Employee babyfish_shared_alias_1 " +
                "where " +
                "babyfish_shared_alias_1.department = babyfish_shared_alias_0 and " +
                "babyfish_shared_alias_1.name like :babyfish_literal_0" +
                ")",
                get(template, JPQL_FIELD));
        assertImplictParameters(
                template,
                "babyfish_literal_0", "%Tom%");
                
        this.entityManager.getTransaction().begin();
        try {
            this.entityManager.createQuery(cd).executeUpdate();
        } catch (RuntimeException | Error ex) {
            this.entityManager.getTransaction().rollback();
            throw ex;
        }
        this.entityManager.getTransaction().commit();
    }
    
    private static void assertImplictParameters(
            QueryTemplate<?> template, Object ... values) {
        Collection<?> literalParameters = get(template, IMPLICIT_PARAMTERS_FIELD);
        Assert.assertTrue(values.length % 2 == 0);
        Assert.assertEquals(values.length / 2, literalParameters.size());
        int index = 0;
        for (Object ip : literalParameters) {
            Object name = values[index++];
            Object value = values[index++];
            Assert.assertEquals(name, get(ip, NAME_FIELD));
            if (value.getClass().isArray()) {
                Collection<?> c = (Collection<?>)get(ip, VALUE_FIELD);
                Object[] arr = (Object[])value;
                Assert.assertEquals(arr.length, c.size());
                int nestedIndex = 0;
                for (Object e : c) {
                    Assert.assertEquals(arr[nestedIndex++], e);
                }
            } else {
                Assert.assertEquals(value, get(ip, VALUE_FIELD));
            }
        }
    }
    
    private static Field fieldOf(Class<?> ownerType, String fieldName) {
        Field field;
        try {
            field = ownerType.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            return null;
        }
        field.setAccessible(true);
        return field;
    }
    
    private static Class<?> classOf(Class<?> ownerType, String simpleClassName) {
        for (Class<?> clazz : ownerType.getDeclaredClasses()) {
            if (clazz.getSimpleName().equals(simpleClassName)) {
                return clazz;
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T get(Object owner, Field field) {
        Object o;
        try {
            o = field.get(owner);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
        return (T)o;
    }
    
    static {
        JPQL_FIELD = fieldOf(AbstractQueryTemplate.class, "jpql");
        IMPLICIT_PARAMTERS_FIELD = fieldOf(AbstractQueryTemplate.class, "literalParameters");
        Class<?> literalParameterType = classOf(AbstractQueryTemplate.class, "LiteralParameter");
        NAME_FIELD = fieldOf(literalParameterType, "name");
        VALUE_FIELD = fieldOf(literalParameterType, "value");
    }
}
