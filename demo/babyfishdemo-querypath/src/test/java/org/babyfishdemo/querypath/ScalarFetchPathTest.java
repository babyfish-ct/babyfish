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
package org.babyfishdemo.querypath;

import java.util.Arrays;
import java.util.Iterator;

import javax.persistence.Persistence;

import junit.framework.Assert;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Ref;
import org.babyfish.util.GraphTravelAction;
import org.babyfish.util.GraphTravelContext;
import org.babyfish.util.GraphTraveler;
import org.babyfishdemo.querypath.base.AbstractTest;
import org.babyfishdemo.querypath.base.Lobs;
import org.babyfishdemo.querypath.entities.Department;
import org.babyfishdemo.querypath.entities.Department__;
import org.babyfishdemo.querypath.entities.Employee;
import org.junit.Test;

/** 
 * Hibernate started to support scalar property lazy-loading from version3,
 * this is a cool technology, it is often(not must) be used for lob fields,
 * but it required to use the hibernate compilation-time byte-code instrument.
 * 
 * BabyFish also support Scalar FetchPath to load the lazy scalar fields 
 * before close the session/EntityManager. Be different with Association 
 * FetchPath, Scalar FetchPath required to use ObjectModel4JPA. So in this
 * project, all the entities is marked by 
 *      &#64;org.babyfish.model.jpa.JPAModel
 * and the pom.xml must use byte-code instrument of
 * babyfish-hibernate(not hibernate), please view the "babyfish-hibernate-model-tool"
 * in the pom.xml of current project.
 * 
 * In original hibernate, if an entity class contains several lazy scalar 
 * fields, either all of them or none of them should be loaded, because
 * all the lazy scalar fields will be loaded if you access any one of them
 * before close the session/EntityManager. ObjectModel4JPA change the 
 * mechanism of lazy fields, by using Scalar FetchPath, you can load some of 
 * the lazy scalar fields, not all.
 * 
 * @author Tao Chen
 */
public class ScalarFetchPathTest extends AbstractTest {

    private DepartmentRepository departmentRepository = new DepartmentRepository();
    
    @Test
    public void testLoadNothing() {
        
        Department templarArchives = this.departmentRepository.getDepartmentByName("Templar Archives");
        
        // description is not loaded
        Assert.assertFalse(
                Persistence.getPersistenceUtil().isLoaded(
                        templarArchives, 
                        "description"
                )
        );
        
        // image is not loaded
        Assert.assertFalse(
                Persistence.getPersistenceUtil().isLoaded(
                        templarArchives, 
                        "image"
                )
        );
        
        Assert.assertEquals(1, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                + "<...many columns of department0_...> "
                + "from DEPARTMENT department0_ "
                + "where department0_.NAME=?", 
                this.preparedSqlList.get(0)
        );
    }
    
    @Test
    public void testLoadAllLazyScalars() {
        
        Department templarArchives = this.departmentRepository.getDepartmentByName(
                "Templar Archives",
                Department__.begin().description().end(),
                Department__.begin().image().end()
        );
        
        // description is loaded
        Assert.assertTrue(
                Persistence.getPersistenceUtil().isLoaded(
                        templarArchives, 
                        "description"
                )
        );
        Assert.assertEquals(
                Lobs.TEMPLAR_ARCHIVES_DESCRIPTION, 
                templarArchives.getDescription()
        );
        
        // image is loaded too
        Assert.assertTrue(
                Persistence.getPersistenceUtil().isLoaded(
                        templarArchives, 
                        "image"
                )
        );
        Assert.assertTrue(
                Arrays.equals(
                        MACollections.toByteArray(Lobs.TEMPLAR_ARCHIVES_IMAGE), 
                        templarArchives.getImage()
                )
        );
        
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...> "
                + "from DEPARTMENT department0_ "
                + "where department0_.NAME=?", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "this_.IMAGE, "
                +     "this_.DESCRIPTION "
                + "from DEPARTMENT this_ "
                + "where this_.DEPARTMENT_ID=?",
                this.preparedSqlList.get(1)
        );
    }
    
    @Test
    public void testLoadSomeLazyScalars() {
        
        // This ability is not supported by original hibernate:
        // load some(not all) lazy scalar fields
        
        Department templarArchives = this.departmentRepository.getDepartmentByName(
                "Templar Archives",
                Department__.begin().image().end() // Only load image, don't load description
        );
        
        // description is not loaded
        Assert.assertFalse(
                Persistence.getPersistenceUtil().isLoaded(
                        templarArchives, 
                        "description"
                )
        );
        
        // but image is loaded
        Assert.assertTrue(
                Persistence.getPersistenceUtil().isLoaded(
                        templarArchives, 
                        "image"
                )
        );
        Assert.assertTrue(
                Arrays.equals(
                        MACollections.toByteArray(Lobs.TEMPLAR_ARCHIVES_IMAGE), 
                        templarArchives.getImage()
                )
        );
        
        
        Assert.assertEquals(2, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...> "
                + "from DEPARTMENT department0_ "
                + "where department0_.NAME=?", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "this_.IMAGE "
                + "from DEPARTMENT this_ "
                + "where this_.DEPARTMENT_ID=?",
                this.preparedSqlList.get(1)
        );
    }
    
    /*
     * Notes: 
     * Before learn this method, you'd better learn AssociationFetchPathTest at first.
     */
    @Test
    public void testFetchBothScalarAndAssociation() {
        // Scalar FetchPath can be used with Association FetchPath together
        // In this example, all the lazy scalar fields of both Department and employees are loaded.
        
        /*
         * The final FetchPlan tree("@" means scalar property)
         * 
         *  +-<<ROOT: Department>>
         *  |
         *  +-----@description
         *  |
         *  +-----@image
         *  |
         *  \---+-employees
         *      |
         *      +-----@resume
         *      |
         *      \-----@photo
         */
        Department templarArchives = this.departmentRepository.getDepartmentByName(
                "Templar Archives",
                Department__.begin().description().end(),
                Department__.begin().image().end(),
                Department__.begin().employees().resume().end(),
                Department__.begin().employees().photo().end()
        );
        
        /*
         * All the lazy scalars of Department "Templar Archives" are loaded.
         */
        Assert.assertTrue(
                Persistence.getPersistenceUtil().isLoaded(
                        templarArchives, 
                        "description"
                )
        );
        Assert.assertEquals(
                Lobs.TEMPLAR_ARCHIVES_DESCRIPTION, 
                templarArchives.getDescription()
        );
        Assert.assertTrue(
                Persistence.getPersistenceUtil().isLoaded(
                        templarArchives, 
                        "image"
                )
        );
        Assert.assertTrue(
                Arrays.equals(
                        MACollections.toByteArray(Lobs.TEMPLAR_ARCHIVES_IMAGE), 
                        templarArchives.getImage()
                )
        );
        
        /*
         * Get the fetched child object
         */
        Employee tassadar = templarArchives.getEmployees().iterator().next();
        Employee karass = templarArchives.getEmployees().toArray(new Employee[2])[1];
        Assert.assertEquals("Tassadar", tassadar.getName());
        Assert.assertEquals("Karass", karass.getName());
        
        /*
         * All the lazy scalars of Employee "Tassadar" are loaded.
         */
        Assert.assertTrue(
                Persistence.getPersistenceUtil().isLoaded(
                        tassadar, 
                        "resume"
                )
        );
        Assert.assertEquals(
                Lobs.TASSADAR_RESUME, 
                tassadar.getResume()
        );
        Assert.assertTrue(
                Persistence.getPersistenceUtil().isLoaded(
                        tassadar, 
                        "photo"
                )
        );
        Assert.assertTrue(
                Arrays.equals(
                        MACollections.toByteArray(Lobs.TASSADAR_PHOTO), 
                        tassadar.getPhoto()
                )
        );
        
        /*
         * All the lazy scalars of Employee "Karass" are loaded.
         */
        Assert.assertTrue(
                Persistence.getPersistenceUtil().isLoaded(
                        karass, 
                        "resume"
                )
        );
        Assert.assertEquals(
                Lobs.KARASS_RESUME, 
                karass.getResume()
        );
        Assert.assertTrue(
                Persistence.getPersistenceUtil().isLoaded(
                        karass, 
                        "photo"
                )
        );
        Assert.assertTrue(
                Arrays.equals(
                        MACollections.toByteArray(Lobs.KARASS_PHOTO), 
                        karass.getPhoto()
                )
        );
        
        
        Assert.assertEquals(3, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...>, "
                +     "<...many columns of employees1_...> "
                + "from DEPARTMENT department0_ "
                + "left outer join EMPLOYEE employees1_ "
                +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + "where department0_.NAME=?", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "this_.IMAGE, "
                +     "this_.DESCRIPTION "
                + "from DEPARTMENT this_ "
                + "where this_.DEPARTMENT_ID=?", 
                this.preparedSqlList.get(1)
        );
        Assert.assertEquals(
                "select "
                +     "this_.EMPLOYEE_ID, "
                +     "this_.RESUME, "
                +     "this_.PHOTO "
                + "from EMPLOYEE this_ "
                + "where this_.EMPLOYEE_ID in (?, ?)", 
                this.preparedSqlList.get(2)
        );
    }
    
    /*
     * Notes: 
     * Before learn this method, you'd better learn AssociationFetchPathTest at first.
     */
    @Test
    public void testFetchScalarWithComplexObjectTree() {
        
        /*
         * The final FetchPlan tree("@" means scalar property)
         * 
         *  +-<<ROOT: Department>>
         *  |
         *  +-----@description
         *  |
         *  +-----@image
         *  |
         *  \---+-employees
         *      |
         *      +-----@resume
         *      |
         *      +-----@photo
         *      |
         *      \---+-subordinates
         *          |
         *          +-----@resume
         *          |
         *          +-----@photo
         *          |
         *          \---+-subordinates
         *              |
         *              +-----@resume
         *              |
         *              +-----@photo
         *              |
         *              \---+-subordinates
         *                  |
         *                  +-----@resume
         *                  |
         *                  +-----@photo
         *                  |
         *                  \---+-subordinates
         *                      |
         *                      +-----@resume
         *                      |
         *                      \-----@photo
         */
        Department templarArchives = this.departmentRepository.getDepartmentByName(
                "Templar Archives",
                Department__.begin().description().end(),
                Department__.begin().image().end(),
                Department__.begin().employees().resume().end(),
                Department__.begin().employees().photo().end(),
                Department__.begin().employees().subordinates().resume().end(),
                Department__.begin().employees().subordinates().photo().end(),
                Department__.begin().employees().subordinates().subordinates().resume().end(),
                Department__.begin().employees().subordinates().subordinates().photo().end(),
                Department__.begin().employees().subordinates().subordinates().subordinates().resume().end(),
                Department__.begin().employees().subordinates().subordinates().subordinates().photo().end(),
                Department__.begin().employees().subordinates().subordinates().subordinates().subordinates().resume().end(),
                Department__.begin().employees().subordinates().subordinates().subordinates().subordinates().photo().end()
        );
        
        /*
         * All the lazy scalars of Department "Templar Archives" are loaded.
         */
        Assert.assertTrue(
                Persistence.getPersistenceUtil().isLoaded(
                        templarArchives, 
                        "description"
                )
        );
        Assert.assertEquals(
                Lobs.TEMPLAR_ARCHIVES_DESCRIPTION, 
                templarArchives.getDescription()
        );
        Assert.assertTrue(
                Persistence.getPersistenceUtil().isLoaded(
                        templarArchives, 
                        "image"
                )
        );
        Assert.assertTrue(
                Arrays.equals(
                        MACollections.toByteArray(Lobs.TEMPLAR_ARCHIVES_IMAGE), 
                        templarArchives.getImage()
                )
        );
        
        /*
         * The returned object graph("@" means scalar property)
         * 
         *  templarArchives->+-Templar Archives (@description and @image are loaded)
         *                   |
         *                   +---+-Tassadar (@resume and @photo are loaded)
         *                   |   |
         *                   |   +---+-Zeratul (@resume and @photo are loaded)
         *                   |   |   |
         *                   |   |   \---+-Artanis (@resume and @photo are loaded)
         *                   |   |       |
         *                   |   |       \---+-Selendis (@resume and @photo are loaded)
         *                   |   |           |
         *                   |   |           +---+-Mohandar (@resume and @photo are loaded)
         *                   |   |           |   |
         *                   |   |           |   \-----{Unloaded Children}
         *                   |   |           |
         *                   |   |           \---+-Urun (@resume and @photo are loaded)
         *                   |   |               |
         *                   |   |               \-----{Unloaded Children}
         *                   |   |
         *                   |   \-----Karass(A: same reference with B; @resume and @photo are loaded)
         *                   |
         *                   \-----Karass(B: same reference with A; @resume and @photo are loaded)
         */
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(templarArchives.getEmployees()));
        Assert.assertEquals(2, templarArchives.getEmployees().size());
        
        new EmployeeTreeNode(
                "Tassadar", 
                new EmployeeTreeNode(
                        "Zeratul", 
                        new EmployeeTreeNode(
                                "Artanis", 
                                new EmployeeTreeNode(
                                        "Selendis",
                                        new EmployeeTreeNode(
                                                "Mohandar", 
                                                EmployeeTreeNode.UNLOADED_CHILDREN
                                        ),
                                        new EmployeeTreeNode(
                                                "Urun", 
                                                EmployeeTreeNode.UNLOADED_CHILDREN
                                        )
                                )
                        )
                ),
                new EmployeeTreeNode("Karass")
        ).validate(templarArchives.getEmployees().iterator().next());
        
        new EmployeeTreeNode("Karass")
        .validate(templarArchives.getEmployees().toArray(new Employee[2])[1]);
        
        final Ref<Integer> visitedCountRef = new Ref<>(0);
        new GraphTraveler<Employee>() {
            
            @Override
            protected Iterator<Employee> getNeighborNodeIterator(Employee node) {
                if (Persistence.getPersistenceUtil().isLoaded(node.getSubordinates())) {
                    return node.getSubordinates().iterator();
                }
                return null;
            }

            @Override
            protected void preTravelNeighborNodes(
                    GraphTravelContext<Employee> ctx,
                    GraphTravelAction<Employee> optionalGraphTravelAction) {
                Employee employee = ctx.getNode();
                String expectedResume = null;
                byte[] expectedPhoto = null;
                switch (employee.getName()) {
                case "Tassadar":
                    expectedResume = Lobs.TASSADAR_RESUME;
                    expectedPhoto = MACollections.toByteArray(Lobs.TASSADAR_PHOTO);
                    break;
                case "Zeratul":
                    expectedResume = Lobs.ZERATUL_RESUME;
                    expectedPhoto = MACollections.toByteArray(Lobs.ZERATUL_PHOTO);
                    break;
                case "Artanis":
                    expectedResume = Lobs.ARTANIS_RESUME;
                    expectedPhoto = MACollections.toByteArray(Lobs.ARTANIS_PHOTO);
                    break;
                case "Selendis":
                    expectedResume = Lobs.SELENDIS_RESUME;
                    expectedPhoto = MACollections.toByteArray(Lobs.SELENDIS_PHOTO);
                    break;
                case "Mohandar":
                    expectedResume = Lobs.MOHANDAR_RESUME;
                    expectedPhoto = MACollections.toByteArray(Lobs.MOHANDAR_PHOTO);
                    break;
                case "Urun":
                    expectedResume = Lobs.URUN_RESUME;
                    expectedPhoto = MACollections.toByteArray(Lobs.URUN_PHOTO);
                    break;
                case "Karass":
                    expectedResume = Lobs.KARASS_RESUME;
                    expectedPhoto = MACollections.toByteArray(Lobs.KARASS_PHOTO);
                    break;
                default:
                    Assert.fail("Unexpected employee name in the employee tree");
                    break;
                }
                Assert.assertEquals(expectedResume, employee.getResume());
                Assert.assertTrue(Arrays.equals(expectedPhoto, employee.getPhoto()));
                visitedCountRef.set(visitedCountRef.get() + 1);
            }
            
        }.depthFirstTravel(templarArchives.getEmployees());
        
        // 7 employees, but 8 visited results, because "Karass" has been visited twice!
        // (Another demo of another project can teach you how to use 
        // "org.babfish.util.GraphTravel" to avoid the duplicated visiting. 
        // Here, let's focus on QueryPath)
        Assert.assertEquals(8, visitedCountRef.get().intValue());
        
        
        Assert.assertEquals(3, this.preparedSqlList.size());
        Assert.assertEquals(
                "select "
                +     "<...many columns of department0_...>, "
                +     "<...many columns of employees1_...>, "
                +     "<...many columns of subordinat2_...>, "
                +     "<...many columns of subordinat3_...>, "
                +     "<...many columns of subordinat4_...>, "
                +     "<...many columns of subordinat5_...> "
                + "from DEPARTMENT department0_ "
                + "left outer join EMPLOYEE employees1_ "
                +     "on department0_.DEPARTMENT_ID=employees1_.DEPARTMENT_ID "
                + "left outer join EMPLOYEE subordinat2_ "
                +     "on employees1_.EMPLOYEE_ID=subordinat2_.SUPERVISOR_ID "
                + "left outer join EMPLOYEE subordinat3_ "
                +     "on subordinat2_.EMPLOYEE_ID=subordinat3_.SUPERVISOR_ID "
                + "left outer join EMPLOYEE subordinat4_ "
                +     "on subordinat3_.EMPLOYEE_ID=subordinat4_.SUPERVISOR_ID "
                + "left outer join EMPLOYEE subordinat5_ "
                +     "on subordinat4_.EMPLOYEE_ID=subordinat5_.SUPERVISOR_ID "
                + "where department0_.NAME=?", 
                this.preparedSqlList.get(0)
        );
        Assert.assertEquals(
                "select "
                +     "this_.IMAGE, "
                +     "this_.DESCRIPTION "
                + "from DEPARTMENT this_ "
                + "where this_.DEPARTMENT_ID=?", 
                this.preparedSqlList.get(1)
        );
        Assert.assertEquals(
                "select "
                +     "this_.EMPLOYEE_ID, "
                +     "this_.RESUME, "
                +     "this_.PHOTO "
                + "from EMPLOYEE this_ "
                + "where this_.EMPLOYEE_ID in (?, ?, ?, ?, ?, ?, ?)", 
                this.preparedSqlList.get(2)
        );
    }
}
