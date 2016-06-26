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

import javax.persistence.criteria.Root;

import junit.framework.Assert;

import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfishdemo.jpacriteria.base.AbstractTest;
import org.babyfishdemo.jpacriteria.entities.Employee;
import org.babyfishdemo.jpacriteria.entities.Employee_;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class LiteralAndConstantTest extends AbstractTest {

    @Test
    public void testLiteral() {
        /*
         * In the jpa criteria implementation of original hibernate, 
         * literal expression is rendered as literal expression 
         * when its type is numeric; otherwise, it is rendered as
         * JPQL parameter to avoid SQL injection attacking.
         * 
         * (1) Database supports a cache to optimize the performance
         * about translating SQL to query plan, for example, Oracle
         * supports this cache via SGA.
         * (2) Hibernate supports a cache to optimize the performance
         * about translating JPQL to query plan. For original hibernate,
         * it is org.hibernate.engine.query.spi.QueryPlanCache; for
         * babyfish-hibernate, it is org.babyfish.hibernate.hql.XQueryPlanCache
         * 
         * Hibernate's implementation has a problem. the literal 
         * expression whose type is numeric will not be rendered as
         * JPQL parameter, so the hit rate of those 2 caches will be
         * markedly reduced. Another solution is that we don't use numeric
         * literal expression but declare them as parameter by 
         * "CriteriaBuilder.parameter(...)", but that will make our
         * criteria code become more complex.
         * 
         * babyfish-jpa-criteria supports the "OpenJPA" style literal expressions!
         * any liternal expression is rendered as JPQL parameter whatever its type is.
         * 
         * Please say goodbye to "CriteriaBuilder.parameter"!
         */
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> employee = cq.from(Employee.class);
        cq.where(
                cb.or(
                        cb.le(
                                employee.get(Employee_.salary), 
                                new BigDecimal("3000") // You can also write: cb.literal(new BigDecmial("3000"))
                        ),
                        cb.ge(
                                employee.get(Employee_.salary), 
                                new BigDecimal("7000") // You can also write: cb.literal(new BigDecmial("7000"))
                        )
                )
        );
        Assert.assertEquals(
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                + "where "
                +     "babyfish_shared_alias_0.salary <= :babyfish_literal_0 " // Render as parameter even if it's numeric
                +   "or "
                +     "babyfish_shared_alias_0.salary >= :babyfish_literal_1", // Render as parameter even if it's numeric
                createQueryTemplate(cq).toString()
        );
    }
    
    @Test
    public void testConstant() {
        /*
         * XCriteriaBuilder supports a new method: constant, it looks like literal, but it
         * is NEVER render as JPQL parameter whatever its type is. it can works together with 
         * FUNCTION INDEX!
         * (For example, Oracle supports function index)
         * 
         * As a demo, we suppose if:
         * (1) The base tax rate is 10%
         * (2) If salary > 5000, the tax rate is 20% for the portion that is greater than 5000
         */
        
        BigDecimal minTax = new BigDecimal("1000"); // In real project, its value should be input by UI
        BigDecimal maxTax = new BigDecimal("2000"); // In real project, its value should be input by UI
        
        XCriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        XCriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> employee = cq.from(Employee.class);
        cq.where(
                cb.between(
                        cb.<BigDecimal>selectCase()
                        .when(
                                cb.gt(
                                        employee.get(Employee_.salary), 
                                        cb.constant(new BigDecimal("5000")) // Constant, not literal
                                ), 
                                cb.sum(
                                        cb.prod(
                                                cb.diff(
                                                        employee.get(Employee_.salary), 
                                                        cb.constant(new BigDecimal("5000")) // Constant, not literal
                                                ),
                                                cb.constant(new BigDecimal("0.2")) // Constant, not literal
                                        ), 
                                        cb.constant(new BigDecimal("500")) // Constant, not literal
                                )
                        )
                        .otherwise(
                                cb.prod(
                                        employee.get(Employee_.salary), 
                                        cb.constant(new BigDecimal("0.1")) // Constant, not literal
                                )
                        ), 
                        minTax, // Literal, not constant
                        maxTax // Literal, not constant
                )
        );
        Assert.assertEquals(
                "select babyfish_shared_alias_0 "
                + "from org.babyfishdemo.jpacriteria.entities.Employee babyfish_shared_alias_0 "
                + "where "
                +   "case "
                +     "when babyfish_shared_alias_0.salary > 5000 then "
                +       "(babyfish_shared_alias_0.salary - 5000) * 0.2 + 500 "
                +     "else "
                +       "babyfish_shared_alias_0.salary * 0.1 "
                +   "end "
                +   "between "
                +     ":babyfish_literal_0 "
                +   "and "
                +     ":babyfish_literal_1",
                createQueryTemplate(cq).toString()
        );
        
        /*
         * Why the constant expressions are NOT rendered as JPQL parameter?
         * 
         * The answer is for FUNCTION index of database.
         * 
         * Suppose if the database is Oracle, you can create this function index:
         * 
         *    CREATE INDEX EMPLOYEE_TAX_INDEX
         *      ON EMPLOYEE(
         *          CASE
         *              WHEN SALARY > 500 THEN
         *                  (SALARY - 5000) * 0.2 + 500
         *              ELSE
         *                  SALARY * 0.1
         *          END
         *      );
         */
    }
}
