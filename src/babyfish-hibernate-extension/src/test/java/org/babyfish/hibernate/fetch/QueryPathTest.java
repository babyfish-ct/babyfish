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
package org.babyfish.hibernate.fetch;

import java.util.List;

import junit.framework.Assert;

import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.hibernate.cfg.Configuration;
import org.babyfish.hibernate.internal.AbstractHibernatePathPlanFactory;
import org.babyfish.model.jpa.path.GetterType;
import org.babyfish.model.jpa.path.TypedSimpleOrderPath;
import org.babyfish.model.jpa.path.spi.AbstractPathPlanFactory;
import org.babyfish.model.jpa.path.spi.OrderNode;
import org.babyfish.model.jpa.path.spi.PathPlanKeyBuilder;
import org.babyfish.model.jpa.path.spi.SubPlan;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class QueryPathTest {

    private static final String RESOURCE =
            QueryPathTest.class.getPackage().getName().replace('.', '/') + "/hibernate.cfg.xml";
    
    private static XSessionFactory sessionFactory;
    
    @SuppressWarnings("deprecation")
    @BeforeClass
    public static void initSessionFactory() {
        sessionFactory =
                new Configuration()
                .configure(RESOURCE)
                .addAnnotatedClass(Department.class)
                .addAnnotatedClass(Employee.class)
                .addAnnotatedClass(AnnualLeave.class)
                .buildSessionFactory();
    }
    
    @AfterClass
    public static void closeSessionFactory() {
        XSessionFactory sf = sessionFactory;
        if (sf != null) {
            sessionFactory = null;
            sf.close();
        }
    }
    
    @Test
    public void testOptimizeOrderByIdSuccessful() {
        EntityPersister persister = ((SessionFactoryImplementor)sessionFactory).getEntityPersister(Employee.class.getName());
        AbstractPathPlanFactory pathPlanFactory = new PlanPathFactoryImpl(persister);
        
        TypedSimpleOrderPath<Employee> path = Employee__.preOrderBy().department().id().asc();
        
        Assert.assertEquals(
                "this\r\n" +
                "\tpre order by department asc\r\n",
                pathPlanFactory.create(new PathPlanKeyBuilder().setQueryPaths(path).build()).getSubPlans().get(null).getJoinNode().toString());
    }
    
    @Test
    public void testOptimizeOrderByIdFailedBecauseOfCollectionJoin() {
        EntityPersister persister = ((SessionFactoryImplementor)sessionFactory).getEntityPersister(Department.class.getName());
        AbstractPathPlanFactory pathPlanFactory = new PlanPathFactoryImpl(persister);
        
        TypedSimpleOrderPath<Department> path = Department__.preOrderBy().employees().id().asc();
        
        Assert.assertEquals(
                "this\r\n" +
                "\tleft join employees\r\n" +
                "\t\tpre order by id asc\r\n", 
                pathPlanFactory.create(new PathPlanKeyBuilder().setQueryPaths(path).build()).getSubPlans().get(null).getJoinNode().toString());
    }
    
    @Test
    public void testOptimizeOrderByIdFailedBecauseOfInnerJoin() {
        EntityPersister persister = ((SessionFactoryImplementor)sessionFactory).getEntityPersister(Department.class.getName());
        AbstractPathPlanFactory pathPlanFactory = new PlanPathFactoryImpl(persister);
        
        TypedSimpleOrderPath<Department> path = Department__.preOrderBy().employees(GetterType.REQUIRED).id().asc();
        
        Assert.assertEquals(
                "this\r\n" +
                "\tinner join employees\r\n" +
                "\t\tpre order by id asc\r\n", 
                pathPlanFactory.create(new PathPlanKeyBuilder().setQueryPaths(path).build()).getSubPlans().get(null).getJoinNode().toString());
    }
    
    @Test
    public void testOptimizeOrderByAssociationSuccessfulBecauseOfInnerJoin() {
        EntityPersister persister = ((SessionFactoryImplementor)sessionFactory).getEntityPersister(Employee.class.getName());
        AbstractPathPlanFactory pathPlanFactory = new PlanPathFactoryImpl(persister);
        
        TypedSimpleOrderPath<Employee> path = Employee__.preOrderBy().department(GetterType.REQUIRED).asc();
        
        Assert.assertEquals(
                "this\r\n" +
                "\tinner join department\r\n" +
                "\t\tpre order by id asc\r\n", 
                pathPlanFactory.create(new PathPlanKeyBuilder().setQueryPaths(path).build()).getSubPlans().get(null).getJoinNode().toString());
    }
    
    @Test
    public void testOptimizeOrderByAssociationSuccessfulBecauseOfCollectionJoin() {
        EntityPersister persister = ((SessionFactoryImplementor)sessionFactory).getEntityPersister(Department.class.getName());
        AbstractPathPlanFactory pathPlanFactory = new PlanPathFactoryImpl(persister);
        
        TypedSimpleOrderPath<Department> path = Department__.preOrderBy().employees().asc();
        
        Assert.assertEquals(
                "this\r\n" +
                "\tleft join employees\r\n" +
                "\t\tpre order by id asc\r\n", 
                pathPlanFactory.create(new PathPlanKeyBuilder().setQueryPaths(path).build()).getSubPlans().get(null).getJoinNode().toString());
    }
    
    @Test
    public void testOptimizeOrderByAssociationFailed() {
        EntityPersister persister = ((SessionFactoryImplementor)sessionFactory).getEntityPersister(Employee.class.getName());
        AbstractPathPlanFactory pathPlanFactory = new PlanPathFactoryImpl(persister);
        
        TypedSimpleOrderPath<Employee> path = Employee__.preOrderBy().department().asc();
        Assert.assertEquals(
                "this\r\n" +
                "\tpre order by department asc\r\n", 
                pathPlanFactory.create(new PathPlanKeyBuilder().setQueryPaths(path).build()).getSubPlans().get(null).getJoinNode().toString());
    }
    
    @Test
    public void testKeepOrderSequenceDuringOptimization() {
        EntityPersister persister = ((SessionFactoryImplementor)sessionFactory).getEntityPersister(Employee.class.getName());
        AbstractPathPlanFactory pathPlanFactory = new PlanPathFactoryImpl(persister);
        
        Employee__[] paths = new Employee__[] {
                Employee__.begin().annualLeaves().end(),
                Employee__.begin().department().end(),
                Employee__.begin().supervisor().supervisor().supervisor().supervisor().end(),
                Employee__.begin().subordinates().subordinates().end(),
                Employee__.postOrderBy().subordinates().subordinates().id().desc(),
                Employee__.postOrderBy().supervisor().supervisor().supervisor().supervisor(GetterType.REQUIRED).desc(),
                Employee__.postOrderBy().department().id().desc(),
                Employee__.postOrderBy().annualLeaves(GetterType.REQUIRED).desc()
        };
        
        Assert.assertEquals(
                "this\r\n" +
                "\tinner join fetch annualLeaves\r\n" +
                "\t\tpost order by id desc\r\n" +
                "\tleft join fetch department\r\n" +
                "\tleft join fetch supervisor\r\n" +
                "\t\tleft join fetch supervisor\r\n" +
                "\t\t\tleft join fetch supervisor\r\n" +
                "\t\t\t\tinner join fetch supervisor\r\n" +
                "\t\t\t\t\tpost order by id desc\r\n" +
                "\tleft join fetch subordinates\r\n" +
                "\t\tleft join fetch subordinates\r\n" +
                "\t\t\tpost order by id desc\r\n" +
                "\tpost order by department desc\r\n",
                pathPlanFactory.create(new PathPlanKeyBuilder().setQueryPaths(paths).build()).getSubPlans().get(null).getJoinNode().toString());
        assertPostOrderNodeRecursively(
                pathPlanFactory.create(new PathPlanKeyBuilder().setQueryPaths(paths).build()).getSubPlans().get(null),
                "post order by this/left join fetch subordinates/left join fetch subordinates@id desc",
                "post order by this/left join fetch supervisor/left join fetch supervisor/left join fetch supervisor/inner join fetch supervisor@id desc",
                "post order by this@department desc",
                "post order by this/inner join fetch annualLeaves@id desc");
    }
    
    private static void assertPostOrderNodeRecursively(SubPlan subPlan, String ... orderNodes) {
        List<OrderNode> orderNodesRecursively = subPlan.getPostOrderNodes();
        Assert.assertEquals(orderNodes.length, orderNodesRecursively.size());
        for (int i = 0; i < orderNodes.length; i++) {
            Assert.assertEquals(orderNodes[i], orderNodesRecursively.get(i).toString());
        }
    }
    
    private static class PlanPathFactoryImpl extends AbstractHibernatePathPlanFactory {
        
        private EntityPersister entityPersister;
        
        PlanPathFactoryImpl(EntityPersister entityPersister) {
            this.entityPersister = entityPersister;
        }

        @Override
        protected EntityPersister getEntityPersister(String alias) {
            if (alias == null) {
                return this.entityPersister;
            }
            throw new IllegalArgumentException();
        }
        
    }
}
