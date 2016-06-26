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

import java.math.BigDecimal;
import java.util.Collection;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import junit.framework.Assert;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Nulls;
import org.babyfish.persistence.criteria.LikeMode;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.ext.SelfGetter;
import org.babyfishdemo.jpacriteria.base.AbstractTest;
import org.babyfishdemo.jpacriteria.entities.Department;
import org.babyfishdemo.jpacriteria.entities.Department_;
import org.babyfishdemo.jpacriteria.entities.Employee;
import org.babyfishdemo.jpacriteria.entities.Employee_;
import org.babyfishdemo.jpacriteria.entities.Privilege;
import org.babyfishdemo.jpacriteria.entities.Privilege_;
import org.babyfishdemo.jpacriteria.entities.Role_;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class DynamicQueryTest extends AbstractTest {
    
    private static final DynamicJPQLBuilder[] DYNAMIC_JPQL_BUILDERS = 
            new DynamicJPQLBuilder[] {
                new DynamicJPQLBuilderImpl_Style1(),
                new DynamicJPQLBuilderImpl_Style2(),
                new DynamicJPQLBuilderImpl_Style3(),
                new DynamicJPQLBuilderImpl_Style4()
            };
    
    @Test
    public void testNoJoinAndNoWhere() {
        String expectedJPQL =
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0";
        for (DynamicJPQLBuilder dynamicJPQLBuilder : DYNAMIC_JPQL_BUILDERS) {
            Assert.assertEquals(expectedJPQL, dynamicJPQLBuilder.getDynamicJPQL(null));
        }
    }
    
    @Test
    public void testWhereSalaryLeMaxSalary() {
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.maxSalary = new BigDecimal(5000);
        String expectedJPQL =
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                + "where babyfish_shared_alias_0.salary <= :babyfish_literal_0";
        for (DynamicJPQLBuilder dynamicJPQLBuilder : DYNAMIC_JPQL_BUILDERS) {
            Assert.assertEquals(expectedJPQL, dynamicJPQLBuilder.getDynamicJPQL(specification));
        }
    }
    
    @Test
    public void testWithAllSelfConditionsWithoutJoin() {
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.likeName = "t";
        specification.minSalary = new BigDecimal(4000);
        specification.maxSalary = new BigDecimal(8000);
        String expectedJPQL =
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                + "where "
                +     "upper(babyfish_shared_alias_0.name) like :babyfish_literal_0 "
                +   "and "
                +     "babyfish_shared_alias_0.salary between :babyfish_literal_1 and :babyfish_literal_2";
        for (DynamicJPQLBuilder dynamicJPQLBuilder : DYNAMIC_JPQL_BUILDERS) {
            Assert.assertEquals(expectedJPQL, dynamicJPQLBuilder.getDynamicJPQL(specification));
        }
    }
    
    @Test
    public void testWithAllDepartmentConditionsWithOnlyOneJoin() {
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.departmentLikeName = "x";
        specification.departmentCityNames = MACollections.wrap("ChengDu", "BeiJing", "ShangHai");
        String expectedJPQL =
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                + "inner join babyfish_shared_alias_0.department babyfish_shared_alias_1 "
                + "where "
                +     "upper(babyfish_shared_alias_1.name) like :babyfish_literal_0 "
                +   "and "
                +     "babyfish_shared_alias_1.city in(:babyfish_literal_1)";
        for (DynamicJPQLBuilder dynamicJPQLBuilder : DYNAMIC_JPQL_BUILDERS) {
            Assert.assertEquals(expectedJPQL, dynamicJPQLBuilder.getDynamicJPQL(specification));
        }
    }
    
    @Test
    public void testDependencyWithInclusiveOnly() {
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.includedPrivilegeNames = MACollections.wrap("view-product", "view-order");
        String expectedJPQL =
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                + "where "
                +     "exists("
                +       "select 0 "
                +       "from org.babyfishdemo.jpacriteria.entities.Privilege babyfish_shared_alias_1 "
                +       "inner join babyfish_shared_alias_1.roles babyfish_shared_alias_2 "
                +       "inner join babyfish_shared_alias_2.employees babyfish_shared_alias_3 "
                +       "where "
                +           "babyfish_shared_alias_0 = babyfish_shared_alias_3 "
                +         "and "
                +           "babyfish_shared_alias_1.name in(:babyfish_literal_0)"
                +     ") "
                +   "or "
                +     "exists("
                +       "select 0 "
                +       "from org.babyfishdemo.jpacriteria.entities.Privilege babyfish_shared_alias_4 "
                +       "inner join babyfish_shared_alias_4.employees babyfish_shared_alias_5 "
                +       "where "
                +           "babyfish_shared_alias_0 = babyfish_shared_alias_5 "
                +         "and "
                +           "babyfish_shared_alias_4.name in(:babyfish_literal_1)"
                +     ")";
        for (DynamicJPQLBuilder dynamicJPQLBuilder : DYNAMIC_JPQL_BUILDERS) {
            Assert.assertEquals(expectedJPQL, dynamicJPQLBuilder.getDynamicJPQL(specification));
        }
    }
    
    @Test
    public void testWithAllConditions() {
        EmployeeSpecification specification = new EmployeeSpecification();
        specification.likeName = "t";
        specification.minSalary = new BigDecimal(4000);
        specification.maxSalary = new BigDecimal(8000);
        specification.departmentLikeName = "x";
        specification.departmentCityNames = MACollections.wrap("ChengDu", "BeiJing", "ShangHai");
        specification.includedPrivilegeNames = MACollections.wrap("view-product", "view-order");
        specification.excludePrivilegeNames = MACollections.wrap("edit-product", "dispatch-order");
        String expectedJPQL =
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                + "inner join babyfish_shared_alias_0.department babyfish_shared_alias_1 "
                + "where "
                +     "upper(babyfish_shared_alias_0.name) like :babyfish_literal_0 "
                +   "and "
                +     "babyfish_shared_alias_0.salary between :babyfish_literal_1 and :babyfish_literal_2 "
                +   "and "
                +     "upper(babyfish_shared_alias_1.name) like :babyfish_literal_3 "
                +   "and "
                +     "babyfish_shared_alias_1.city in(:babyfish_literal_4) "
                +   "and ("
                +       "exists("
                +         "select 0 "
                +         "from org.babyfishdemo.jpacriteria.entities.Privilege babyfish_shared_alias_2 "
                +         "inner join babyfish_shared_alias_2.roles babyfish_shared_alias_3 "
                +         "inner join babyfish_shared_alias_3.employees babyfish_shared_alias_4 "
                +         "where "
                +             "babyfish_shared_alias_0 = babyfish_shared_alias_4 "
                +           "and "
                +             "babyfish_shared_alias_2.name in(:babyfish_literal_5)"
                +       ") "
                +     "or "
                +       "exists("
                +         "select 0 "
                +         "from org.babyfishdemo.jpacriteria.entities.Privilege babyfish_shared_alias_5 "
                +         "inner join babyfish_shared_alias_5.employees babyfish_shared_alias_6 "
                +         "where "
                +             "babyfish_shared_alias_0 = babyfish_shared_alias_6 "
                +           "and "
                +             "babyfish_shared_alias_5.name in(:babyfish_literal_6)"
                +       ")"
                +     ") "
                +   "and "
                +     "not exists("
                +       "select 0 "
                +       "from org.babyfishdemo.jpacriteria.entities.Privilege babyfish_shared_alias_7 "
                +       "inner join babyfish_shared_alias_7.roles babyfish_shared_alias_8 "
                +       "inner join babyfish_shared_alias_8.employees babyfish_shared_alias_9 "
                +       "where "
                +           "babyfish_shared_alias_0 = babyfish_shared_alias_9 "
                +         "and "
                +           "babyfish_shared_alias_7.name in(:babyfish_literal_7)"
                +     ") "
                +   "and "
                +     "not exists("
                +       "select 0 "
                +       "from org.babyfishdemo.jpacriteria.entities.Privilege babyfish_shared_alias_10 "
                +       "inner join babyfish_shared_alias_10.employees babyfish_shared_alias_11 "
                +       "where "
                +           "babyfish_shared_alias_0 = babyfish_shared_alias_11 "
                +         "and "
                +           "babyfish_shared_alias_10.name in(:babyfish_literal_8)"
                +     ")";
        for (DynamicJPQLBuilder dynamicJPQLBuilder : DYNAMIC_JPQL_BUILDERS) {
            Assert.assertEquals(expectedJPQL, dynamicJPQLBuilder.getDynamicJPQL(specification));
        }
    }

    private interface DynamicJPQLBuilder {
        
        String getDynamicJPQL(EmployeeSpecification specification);
        
    }
    /*
     * Each field can be null(or empty), that means that field is disabled.
     */
    private static class EmployeeSpecification {
        
        public String likeName;
        
        public BigDecimal minSalary;
        
        public BigDecimal maxSalary;
        
        public String departmentLikeName;
        
        public Collection<String> departmentCityNames;
        
        public Collection<String> includedPrivilegeNames;
        
        public Collection<String> excludePrivilegeNames;
    }
    
    /*
     * This demo shows 4 style implementations for DynamicJPQLBuilder,
     * and the functionalities of them are 100% same.
     * 
     * Because
     * (1) There are 2 ways to create dynamic predicates
     * 
     *      (a) Predicate a = null, b = null, c = null;
     *          if (...) {
     *              a = <<not null>>;
     *          }
     *          if (...) {
     *              b = <<not null>>;
     *          }
     *          if (...) {
     *              c = <<not null>>;
     *          }
     *          cq.where(a, b, c);
     * 
     *      (b) Predicate predicate = null;
     *          if (...) {
     *              predicate = cb.and(predicate, <<not null>>);
     *          }
     *          if (...) {
     *              predicate = cb.and(predicate, <<not null>>);
     *          }
     *          if (...) {
     *              predicate = cb.and(predicate, <<not null>>);
     *          }
     *          cq.where(predicate);
     * 
     * (2) There are 2 ways to create dynamic joins
     * 
     *      (a) Root<Employee> employe = ...;
     *          if (...) {
     *              // Default JoinMode is JoinMode.OPTIONALLY_MERGE_EXISTS, so that the 
     *              // duplicated join for the same association will be merged to one,
     *              // please don't worry :)
     *              ... employee.join(Employee_.department) ...
     *          }
     *          if (...) {
     *              // Default JoinMode is JoinMode.OPTIONALLY_MERGE_EXISTS, so that the 
     *              // duplicated join for the same association will be merged to one,
     *              // please don't worry :)
     *              ... employee.join(Employee_.department) ...
     *          }
     *          // Here are two "if" statement, if no one is matched, no join; 
     *          // if both of them are matched, only one join because those two original join
     *          // will be merged to be one.
     * 
     *      (b) Root<Employee> employe = ...;
     * 
     *          // Default JoinMode is JoinMode.OPTIONALLY_MERGE_EXISTS, so that the 
     *          // the join object "department" will be ignored if it is NOT used
     *          // by any SQL expression,
     *          // please don't worry :)
     *          Join<Employee> department = employee.join(Employee_.department);
     * 
     *          if (...) {
     *              ...department...
     *          }
     *          if (...) {
     *              ...department...
     *          }
     *          // Here are two "if" statement, if no one is matched, no join, because
     *          // that original join is not used any SQL expression so that it will
     *          // ignored.
     */
    
    private static class DynamicJPQLBuilderImpl_Style1 implements DynamicJPQLBuilder {

        @Override
        public String getDynamicJPQL(EmployeeSpecification specification) {
            
            XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            
            Predicate likeNamePredicate = null;
            Predicate salaryPredicate = null;
            Predicate departmentLikeNamePredicate = null;
            Predicate departmentCityNamePredicate = null;
            Predicate privilegeDependecnyPredicate = null;
            
            if (specification != null) {
                
                if (!Nulls.isNullOrEmpty(specification.likeName)) {
                    likeNamePredicate = cb.insensitivelyLike(
                            employee.get(Employee_.name), 
                            specification.likeName,
                            LikeMode.ANYWHERE
                    );
                }
                
                salaryPredicate = cb.between(
                        employee.get(Employee_.salary), 
                        specification.minSalary, 
                        specification.maxSalary
                ); 
                // If both minSalary and maxSalary are null, 
                // cb.between will be ignored, please see NullableExpressionTest to know more
                
                if (!Nulls.isNullOrEmpty(specification.departmentLikeName)) {
                    departmentLikeNamePredicate = cb.insensitivelyLike(
                            // Don't worry, if another join is same with this, they will be merged to one join:)
                            employee.join(Employee_.department).get(Department_.name), 
                            specification.departmentLikeName,
                            LikeMode.ANYWHERE
                    );
                }
                
                if (!Nulls.isNullOrEmpty(specification.departmentCityNames)) {
                    departmentCityNamePredicate = cb.in(
                            // Don't worry, if another join is same with this, they will be merged to one join:)
                            employee.join(Employee_.department).get(Department_.city), 
                            specification.departmentCityNames
                    );
                }
                
                privilegeDependecnyPredicate = 
                        cb
                        .dependencyPredicateBuilder(employee, Privilege.class)
                        .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                            @Override
                            public Path<Employee> getSelf(XRoot<Privilege> target) {
                                return target.join(Privilege_.roles).join(Role_.employees);
                            }
                        })
                        .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                            @Override
                            public Path<Employee> getSelf(XRoot<Privilege> target) {
                                return target.join(Privilege_.employees);
                            }
                        })
                        .includeAny(Privilege_.name, specification.includedPrivilegeNames)
                        .excludeAll(Privilege_.name, specification.excludePrivilegeNames)
                        .build(); 
                        // If both includedPrivilegeNames and excludedPrivilegeNames are null or empty,
                        // DependencyPredicateBuilder.build() will return null
            }
            
            cq
            .where(
                    likeNamePredicate,
                    salaryPredicate,
                    departmentLikeNamePredicate,
                    departmentCityNamePredicate,
                    privilegeDependecnyPredicate
            )
            .select(employee);
            
            return createQueryTemplate(cq).toString();
        }
    }
    
    private static class DynamicJPQLBuilderImpl_Style2 implements DynamicJPQLBuilder {

        @Override
        public String getDynamicJPQL(EmployeeSpecification specification) {
            
            XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            
            Predicate predicate = null;
            
            if (specification != null) {
                
                if (!Nulls.isNullOrEmpty(specification.likeName)) {
                    predicate = 
                            cb.and(
                                    predicate,
                                    cb.insensitivelyLike(
                                            employee.get(Employee_.name), 
                                            specification.likeName,
                                            LikeMode.ANYWHERE
                                    )
                            );
                }
                
                predicate = cb.and(
                        predicate,
                        cb.between(
                                employee.get(Employee_.salary), 
                                specification.minSalary, 
                                specification.maxSalary
                        )
                        // If both minSalary and maxSalary are null, 
                        // cb.between will be ignored, please see NullableExpressionTest to know more
                );
                
                if (!Nulls.isNullOrEmpty(specification.departmentLikeName)) {
                    predicate = cb.and(
                            predicate,
                            cb.insensitivelyLike(
                                    // Don't worry, if another join is same with this, they will be merged to one join:)
                                    employee.join(Employee_.department).get(Department_.name), 
                                    specification.departmentLikeName,
                                    LikeMode.ANYWHERE
                            )
                    );
                }
                
                if (!Nulls.isNullOrEmpty(specification.departmentCityNames)) {
                    predicate = cb.and(
                            predicate,
                            cb.in(
                                    // Don't worry, if another join is same with this, they will be merged to one join:)
                                    employee.join(Employee_.department).get(Department_.city), 
                                    specification.departmentCityNames
                            )
                    );
                }
                
                predicate = cb.and(
                        predicate,
                        cb
                        .dependencyPredicateBuilder(employee, Privilege.class)
                        .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                            @Override
                            public Path<Employee> getSelf(XRoot<Privilege> target) {
                                return target.join(Privilege_.roles).join(Role_.employees);
                            }
                        })
                        .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                            @Override
                            public Path<Employee> getSelf(XRoot<Privilege> target) {
                                return target.join(Privilege_.employees);
                            }
                        })
                        .includeAny(Privilege_.name, specification.includedPrivilegeNames)
                        .excludeAll(Privilege_.name, specification.excludePrivilegeNames)
                        .build()
                        // If both includedPrivilegeNames and excludedPrivilegeNames are null or empty,
                        // DependencyPredicateBuilder.build() will return null
                );
            }
            
            cq
            .where(predicate)
            .select(employee);
            
            return createQueryTemplate(cq).toString();
        }
    }
    
    private static class DynamicJPQLBuilderImpl_Style3 implements DynamicJPQLBuilder {

        @Override
        public String getDynamicJPQL(EmployeeSpecification specification) {
            
            XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            
            // Don't worry, if this join is not used, it will be ignored :)
            Join<Employee, Department> department = employee.join(Employee_.department);
            
            Predicate likeNamePredicate = null;
            Predicate salaryPredicate = null;
            Predicate departmentLikeNamePredicate = null;
            Predicate departmentCityNamePredicate = null;
            Predicate privilegeDependecnyPredicate = null;
            
            if (specification != null) {
                
                if (!Nulls.isNullOrEmpty(specification.likeName)) {
                    likeNamePredicate = cb.insensitivelyLike(
                            employee.get(Employee_.name), 
                            specification.likeName,
                            LikeMode.ANYWHERE
                    );
                }
                
                salaryPredicate = cb.between(
                        employee.get(Employee_.salary), 
                        specification.minSalary, 
                        specification.maxSalary
                ); 
                // If both minSalary and maxSalary are null, 
                // cb.between will be ignored, please see NullableExpressionTest to know more
                
                if (!Nulls.isNullOrEmpty(specification.departmentLikeName)) {
                    departmentLikeNamePredicate = cb.insensitivelyLike(
                            department.get(Department_.name), 
                            specification.departmentLikeName,
                            LikeMode.ANYWHERE
                    );
                }
                
                if (!Nulls.isNullOrEmpty(specification.departmentCityNames)) {
                    departmentCityNamePredicate = cb.in(
                            department.get(Department_.city), 
                            specification.departmentCityNames
                    );
                }
                
                privilegeDependecnyPredicate = 
                        cb
                        .dependencyPredicateBuilder(employee, Privilege.class)
                        .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                            @Override
                            public Path<Employee> getSelf(XRoot<Privilege> target) {
                                return target.join(Privilege_.roles).join(Role_.employees);
                            }
                        })
                        .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                            @Override
                            public Path<Employee> getSelf(XRoot<Privilege> target) {
                                return target.join(Privilege_.employees);
                            }
                        })
                        .includeAny(Privilege_.name, specification.includedPrivilegeNames)
                        .excludeAll(Privilege_.name, specification.excludePrivilegeNames)
                        .build(); 
                        // If both includedPrivilegeNames and excludedPrivilegeNames are null or empty,
                        // DependencyPredicateBuilder.build() will return null
            }
            
            cq
            .where(
                    likeNamePredicate,
                    salaryPredicate,
                    departmentLikeNamePredicate,
                    departmentCityNamePredicate,
                    privilegeDependecnyPredicate
            )
            .select(employee);
            
            return createQueryTemplate(cq).toString();
        }
    }
    
    private static class DynamicJPQLBuilderImpl_Style4 implements DynamicJPQLBuilder {

        @Override
        public String getDynamicJPQL(EmployeeSpecification specification) {
            
            XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            
            // Don't worry, if this join is not used, it will be ignored :)
            Join<Employee, Department> department = employee.join(Employee_.department);
            
            Predicate predicate = null;
            
            if (specification != null) {
                
                if (!Nulls.isNullOrEmpty(specification.likeName)) {
                    predicate = 
                            cb.and(
                                    predicate,
                                    cb.insensitivelyLike(
                                            employee.get(Employee_.name), 
                                            specification.likeName,
                                            LikeMode.ANYWHERE
                                    )
                            );
                }
                
                predicate = cb.and(
                        predicate,
                        cb.between(
                                employee.get(Employee_.salary), 
                                specification.minSalary, 
                                specification.maxSalary
                        )
                        // If both minSalary and maxSalary are null, 
                        // cb.between will be ignored, please see NullableExpressionTest to know more
                );
                
                if (!Nulls.isNullOrEmpty(specification.departmentLikeName)) {
                    predicate = cb.and(
                            predicate,
                            cb.insensitivelyLike(
                                    department.get(Department_.name), 
                                    specification.departmentLikeName,
                                    LikeMode.ANYWHERE
                            )
                    );
                }
                
                if (!Nulls.isNullOrEmpty(specification.departmentCityNames)) {
                    predicate = cb.and(
                            predicate,
                            cb.in(
                                    department.get(Department_.city), 
                                    specification.departmentCityNames
                            )
                    );
                }
                
                predicate = cb.and(
                        predicate,
                        cb
                        .dependencyPredicateBuilder(employee, Privilege.class)
                        .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                            @Override
                            public Path<Employee> getSelf(XRoot<Privilege> target) {
                                return target.join(Privilege_.roles).join(Role_.employees);
                            }
                        })
                        .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                            @Override
                            public Path<Employee> getSelf(XRoot<Privilege> target) {
                                return target.join(Privilege_.employees);
                            }
                        })
                        .includeAny(Privilege_.name, specification.includedPrivilegeNames)
                        .excludeAll(Privilege_.name, specification.excludePrivilegeNames)
                        .build()
                        // If both includedPrivilegeNames and excludedPrivilegeNames are null or empty,
                        // DependencyPredicateBuilder.build() will return null
                );
            }
            
            cq
            .where(predicate)
            .select(employee);
            
            return createQueryTemplate(cq).toString();
        }
    }
}
