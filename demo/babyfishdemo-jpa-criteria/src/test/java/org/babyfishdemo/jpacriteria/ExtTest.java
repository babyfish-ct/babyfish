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

import java.util.Collection;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.babyfish.collection.MACollections;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.ext.SelfGetter;
import org.babyfishdemo.jpacriteria.base.AbstractTest;
import org.babyfishdemo.jpacriteria.entities.Department;
import org.babyfishdemo.jpacriteria.entities.Employee;
import org.babyfishdemo.jpacriteria.entities.Employee_;
import org.babyfishdemo.jpacriteria.entities.Privilege;
import org.babyfishdemo.jpacriteria.entities.Privilege_;
import org.babyfishdemo.jpacriteria.entities.Role_;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ExtTest extends AbstractTest {
    
    /*
     * Not enough time, so only 
     * "org.babyfish.persistence.criteria.ext.SelfGetter" is demonstrated by this demo,
     * 
     * please see babyfishdemo-spring to see how to use
     * "org.babyfish.persistence.criteria.ext.SelfGetterFromRestrictedTarget".
     */
    
    

    @Test
    public void testOneSelfGetterForDependencyPredicate() {
        
        /*
         * In real projects, these four variables should be assigned by the input of UI
         */
        Collection<Long> includedEmployeeIds = MACollections.wrapLong(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Collection<Long> excludedEmployeeIds = MACollections.wrapLong(51, 52, 53, 54, 55, 56, 57, 58, 59, 60);
        Collection<String> includedEmployeeNames = MACollections.wrap("Dennis", "Bjarne", "James", "Anders", "Linus");
        Collection<String> excludedEmployeeNames = MACollections.wrap("Larry", "Yukihiro", "Guido", "Brad", "Brendan");
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        XCriteriaQuery<Department> cq = cb.createQuery(Department.class);
        Root<Department> department = cq.from(Department.class);
        cq.where(
                cb
                .dependencyPredicateBuilder(department, Employee.class)
                // If you use Java8, please write ".addSelfGetter(target -> terget.get(Employee_.department))"
                .addSelfGetter(new SelfGetter<Department, Employee>() {
                    @Override
                    public Path<Department> getSelf(XRoot<Employee> target) {
                        return target.get(Employee_.department);
                    }
                })
                .includeAny(Employee_.id, includedEmployeeIds)
                .includeAny(Employee_.name, includedEmployeeNames)
                .excludeAll(Employee_.id, excludedEmployeeIds)
                .excludeAll(Employee_.name, excludedEmployeeNames)
                .build()
        );
        Assert.assertEquals(
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Department babyfish_shared_alias_0 "
                + "where "
                +     "exists("
                +       "select 0 "
                +       "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_1 "
                +       "where "
                +           "babyfish_shared_alias_0 = babyfish_shared_alias_1.department "
                +         "and ("
                +             "babyfish_shared_alias_1.id in(:babyfish_literal_0) "
                +           "or "
                +             "babyfish_shared_alias_1.name in(:babyfish_literal_1)"
                +         ")"
                +     ") "
                +   "and "
                +     "not exists("
                +       "select 0 "
                +       "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_2 "
                +       "where "
                +           "babyfish_shared_alias_0 = babyfish_shared_alias_2.department "
                +         "and ("
                +             "babyfish_shared_alias_2.id in(:babyfish_literal_2) "
                +           "or "
                +             "babyfish_shared_alias_2.name in(:babyfish_literal_3)"
                +         ")"
                +     ")",
                createQueryTemplate(cq).toString()
        );
    }
    
    @Test
    public void testSeveralSelfGettersForDependencyPredicate() {
        
        /*
         *                         +------+
         *                    -----+ Role +
         * +----------+      /     +---+--+
         * | Employee +-----/          |
         * +----+-----+                |
         *      |                  +---+-------+
         *      \------------------+ Privilege |        
         *                         +-----------+
         * 
         * There are two paths from Employee to Privilege:
         * (1) Employee -> Role -> Privilege
         * (2) Employee -> Privilege
         */
        
        /*
         * In real projects, these two variables should be assigned by the input of UI
         */
        Collection<String> includedPrivilegeNames = MACollections.wrap("view-order", "view-customer", "view-product");
        Collection<String> excludedPrivilegeNames = MACollections.wrap("create-order", "edit-product");
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> employee = cq.from(Employee.class);
        cq.where(
                cb
                .dependencyPredicateBuilder(employee, Privilege.class)
                // If you use Java8, please write ".addSelfGetter(target -> target.join(Privilege_.roles).join(Role_.employees))"
                .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                    @Override
                    public Path<Employee> getSelf(XRoot<Privilege> target) {
                        return target.join(Privilege_.roles).join(Role_.employees);
                    }
                })
                // If you use Java8, please write ".addSelfGetter(target -> target.join(Privilege_.employees))"
                .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                    @Override
                    public Path<Employee> getSelf(XRoot<Privilege> target) {
                        return target.join(Privilege_.employees);
                    }
                })
                .includeAny(Privilege_.name, includedPrivilegeNames)
                .excludeAll(Privilege_.name, excludedPrivilegeNames)
                .build()
        );
        Assert.assertEquals(
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                + "where ("
                +     "exists("
                +       "select 0 "
                +       "from org.babyfishdemo.jpacriteria.entities.Privilege babyfish_shared_alias_1 "
                +       "inner join babyfish_shared_alias_1.roles babyfish_shared_alias_2 "
                +       "inner join babyfish_shared_alias_2.employees babyfish_shared_alias_3 "
                +       "where "
                +           "babyfish_shared_alias_0 = babyfish_shared_alias_3 "
                +         "and "
                +          "babyfish_shared_alias_1.name in(:babyfish_literal_0)"
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
                +       ")"
                + ") "
                + "and "
                +   "not exists("
                +     "select 0 "
                +     "from org.babyfishdemo.jpacriteria.entities.Privilege babyfish_shared_alias_6 "
                +     "inner join babyfish_shared_alias_6.roles babyfish_shared_alias_7 "
                +     "inner join babyfish_shared_alias_7.employees babyfish_shared_alias_8 "
                +     "where "
                +         "babyfish_shared_alias_0 = babyfish_shared_alias_8 "
                +       "and "
                +         "babyfish_shared_alias_6.name in(:babyfish_literal_2)"
                +   ") "
                + "and "
                +   "not exists("
                +     "select 0 "
                +     "from org.babyfishdemo.jpacriteria.entities.Privilege babyfish_shared_alias_9 "
                +     "inner join babyfish_shared_alias_9.employees babyfish_shared_alias_10 "
                +     "where "
                +         "babyfish_shared_alias_0 = babyfish_shared_alias_10 "
                +       "and "
                +         "babyfish_shared_alias_9.name in(:babyfish_literal_3)"
                +   ")", 
                createQueryTemplate(cq).toString()
        );
    }
    
    @Test
    public void testHasForDependencyPredicate() {
        
        /*
         *                         +------+
         *                    -----+ Role +
         * +----------+      /     +---+--+
         * | Employee +-----/          |
         * +----+-----+                |
         *      |                  +---+-------+
         *      \------------------+ Privilege |        
         *                         +-----------+
         * 
         * There are two paths from Employee to Privilege:
         * (1) Employee -> Role -> Privilege
         * (2) Employee -> Privilege
         */
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        /*
         * Select all employees that have privileges
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq.where(
                    cb
                    .dependencyPredicateBuilder(employee, Privilege.class)
                    // If you use Java8, please write ".addSelfGetter(target -> target.join(Privilege_.roles).join(Role_.employees))"
                    .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                        @Override
                        public Path<Employee> getSelf(XRoot<Privilege> target) {
                            return target.join(Privilege_.roles).join(Role_.employees);
                        }
                    })
                    // If you use Java8, please write ".addSelfGetter(target -> target.join(Privilege_.employees))"
                    .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                        @Override
                        public Path<Employee> getSelf(XRoot<Privilege> target) {
                            return target.join(Privilege_.employees);
                        }
                    })
                    .has(true) // Has privileges
                    .build()
            );
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where "
                    +     "exists("
                    +       "select 0 "
                    +       "from org.babyfishdemo.jpacriteria.entities.Privilege babyfish_shared_alias_1 "
                    +       "inner join babyfish_shared_alias_1.roles babyfish_shared_alias_2 "
                    +       "inner join babyfish_shared_alias_2.employees babyfish_shared_alias_3 "
                    +       "where babyfish_shared_alias_0 = babyfish_shared_alias_3"
                    +     ") "
                    +   "or "
                    +     "exists("
                    +       "select 0 "
                    +       "from org.babyfishdemo.jpacriteria.entities.Privilege babyfish_shared_alias_4 "
                    +       "inner join babyfish_shared_alias_4.employees babyfish_shared_alias_5 "
                    +       "where babyfish_shared_alias_0 = babyfish_shared_alias_5"
                    +     ")", 
                    createQueryTemplate(cq).toString()
            );
        }
        
        /*
         * Select all employees that do not have privileges
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq.where(
                    cb
                    .dependencyPredicateBuilder(employee, Privilege.class)
                    // If you use Java8, please write ".addSelfGetter(target -> target.join(Privilege_.roles).join(Role_.employees))"
                    .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                        @Override
                        public Path<Employee> getSelf(XRoot<Privilege> target) {
                            return target.join(Privilege_.roles).join(Role_.employees);
                        }
                    })
                    // If you use Java8, please write ".addSelfGetter(target -> target.join(Privilege_.employees))"
                    .addSelfGetter(new SelfGetter<Employee, Privilege>() {
                        @Override
                        public Path<Employee> getSelf(XRoot<Privilege> target) {
                            return target.join(Privilege_.employees);
                        }
                    })
                    .has(false) // Does not have privileges
                    .build()
            );
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where "
                    +     "not exists("
                    +       "select 0 "
                    +       "from org.babyfishdemo.jpacriteria.entities.Privilege babyfish_shared_alias_1 "
                    +       "inner join babyfish_shared_alias_1.roles babyfish_shared_alias_2 "
                    +       "inner join babyfish_shared_alias_2.employees babyfish_shared_alias_3 "
                    +       "where babyfish_shared_alias_0 = babyfish_shared_alias_3"
                    +     ") "
                    +   "and "
                    +     "not exists("
                    +       "select 0 "
                    +       "from org.babyfishdemo.jpacriteria.entities.Privilege babyfish_shared_alias_4 "
                    +       "inner join babyfish_shared_alias_4.employees babyfish_shared_alias_5 "
                    +       "where babyfish_shared_alias_0 = babyfish_shared_alias_5"
                    +     ")", 
                    createQueryTemplate(cq).toString()
            );
        }
    }
}
