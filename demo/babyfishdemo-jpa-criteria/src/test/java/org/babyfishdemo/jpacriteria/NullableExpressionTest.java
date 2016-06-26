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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import junit.framework.Assert;

import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.expression.ComparisonPredicate;
import org.babyfish.persistence.criteria.expression.CompoundPredicate;
import org.babyfishdemo.jpacriteria.base.AbstractTest;
import org.babyfishdemo.jpacriteria.entities.Employee;
import org.babyfishdemo.jpacriteria.entities.Employee_;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class NullableExpressionTest extends AbstractTest {
    
    @Test
    public void testConjunctionAndDisjunction() {
        /*
         * In order to support dynamic query better, babyfish-jpa-criteria supports 
         * nullable expression, so that CriteriaBuilder.conjunction() and
         * CrieriaBuilder.disjunction() is unnecessary. both of them always return null.
         * 
         * Don't use the "conjunction()" and "disjunction()" in real projects, 
         * babyfish-jpa-criteria doesn't need them.
         */
        CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        Assert.assertNull(cb.conjunction());
        Assert.assertNull(cb.disjunction());
    }

    @Test
    public void testAnd() {
        CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        Predicate a = cb.lt(cb.literal(3), cb.literal(4));
        Predicate b = cb.gt(cb.literal(5), cb.literal(4));
        
        Assert.assertNull(cb.and(null, null));
        Assert.assertSame(a, cb.and(a, null));
        Assert.assertSame(b, cb.and(null, b));
        
        /*
         * In real project, please don't use 
         * "org.babyfish.persistence.criteria.expression.CompoundPredicate"
         * because it is internal class of babyfish so that you
         * should not use it directly.
         */
        CompoundPredicate compoundPredicate = (CompoundPredicate)cb.and(a, null, b, null);
        Assert.assertEquals(2, compoundPredicate.getExpressions().size());
        Assert.assertSame(a, compoundPredicate.getExpressions().get(0));
        Assert.assertSame(b, compoundPredicate.getExpressions().get(1));
    }
    
    @Test
    public void testOr() {
        CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        Predicate a = cb.lt(cb.literal(3), cb.literal(4));
        Predicate b = cb.gt(cb.literal(5), cb.literal(4));
        
        Assert.assertNull(cb.or(null, null));
        Assert.assertSame(a, cb.or(a, null));
        Assert.assertSame(b, cb.or(null, b));
        
        /*
         * In real project, please don't use 
         * "org.babyfish.persistence.criteria.expression.CompoundPredicate"
         * because it is internal class of babyfish so that you
         * should not use it directly.
         */
        CompoundPredicate compoundPredicate = (CompoundPredicate)cb.or(a, null, b, null);
        Assert.assertEquals(2, compoundPredicate.getExpressions().size());
        Assert.assertSame(a, compoundPredicate.getExpressions().get(0));
        Assert.assertSame(b, compoundPredicate.getExpressions().get(1));
    }
    
    @Test
    public void testNot() {
        
        CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        Predicate x = cb.lt(cb.literal(3), cb.literal(4));
        
        Assert.assertNull(cb.not(null));
        
        Predicate notX = cb.not(x);
        Assert.assertTrue(x instanceof ComparisonPredicate.LessThan);
        Assert.assertTrue(notX instanceof ComparisonPredicate.GreaterThanOrEqual);
        Assert.assertSame(notX.not(), x);
        /*
         * Important! Notes:
         *    babyfish-jpa-criteria has a big difference with hibernate-jpa-criteria:
         *  (1) In hibernate, Predicate.not()/cb.not(Predicate) 
         *      changes the current predicate and return itself
         *  (2) In babyfish-hibernate, Predicate.not()/cb.not(Predicate)
         *      does NOT change the current predicate and return a new predicate.
         */
    }
    
    @Test
    public void testBetween() {
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        
        /*
         * Test between when both lower bound expression and 
         * upper bound expression are NOT null
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq.where(
                    cb.between(
                            employee.get(Employee_.salary),
                            new BigDecimal(3000),
                            new BigDecimal(4000)
                    )
            );
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where babyfish_shared_alias_0.salary between :babyfish_literal_0 and :babyfish_literal_1", 
                    createQueryTemplate(cq).toString()
            );
        }
        
        /*
         * Test between when lower bound expression is NULL 
         * but upper bound expression is NOT null.
         * 
         * "between" is changed to "<="
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq.where(
                    cb.between(
                            employee.get(Employee_.salary),
                            null,
                            new BigDecimal(4000)
                    )
            );
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where babyfish_shared_alias_0.salary <= :babyfish_literal_0", 
                    createQueryTemplate(cq).toString()
            );
        }
        
        /*
         * Test between when upper bound expression is NULL 
         * but lower bound expression is NOT null.
         * 
         * "between" is changed to ">="
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq.where(
                    cb.between(
                            employee.get(Employee_.salary),
                            new BigDecimal(3000),
                            null
                    )
            );
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                    + "where babyfish_shared_alias_0.salary >= :babyfish_literal_0", 
                    createQueryTemplate(cq).toString()
            );
        }
        
        /*
         * Test between when both lower bound expression and 
         * upper bound expression are null.
         * 
         * The cb.between(...) returns null so that it can be ignored.
         */
        {
            XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
            Root<Employee> employee = cq.from(Employee.class);
            cq.where(
                    cb.between(
                            employee.get(Employee_.salary),
                            (Expression<BigDecimal>)null,
                            (Expression<BigDecimal>)null
                    )
            );
            Assert.assertEquals(
                    "select babyfish_shared_alias_0 "
                    + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0", 
                    createQueryTemplate(cq).toString()
            );
        }
    }
}
