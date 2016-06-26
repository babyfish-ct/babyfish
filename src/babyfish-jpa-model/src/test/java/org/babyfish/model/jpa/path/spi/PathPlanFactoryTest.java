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
package org.babyfish.model.jpa.path.spi;

import org.babyfish.model.jpa.path.QueryPaths;
import org.babyfish.model.jpa.path.spi.AbstractPathPlanFactory;
import org.babyfish.model.jpa.path.spi.PathPlanKeyBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class PathPlanFactoryTest {

    @Test
    public void testAddQueryPath() {
        PathPlanKeyBuilder keyBuilder = new PathPlanKeyBuilder();
        AbstractPathPlanFactory pathPlanFactory = new PathPlanFactoryImpl();
        Assert.assertNull(pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null));
        
        keyBuilder.setQueryPaths(QueryPaths.compile("this.departments"));
        Assert.assertEquals(
                "this\r\n" +
                "\tleft join fetch departments\r\n", 
                pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null).getJoinNode().toString());
        
        keyBuilder.setQueryPaths(QueryPaths.compile("this.departments", "..departments"));
        Assert.assertEquals(
                "this\r\n" +
                "\tinner join fetch departments\r\n", 
                pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null).getJoinNode().toString());
        
        keyBuilder.setQueryPaths(QueryPaths.compile("this.departments", "..departments", "this.departments.employees"));
        Assert.assertEquals(
                "this\r\n" +
                "\tinner join fetch departments\r\n" +
                "\t\tleft join fetch employees\r\n", 
                pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null).getJoinNode().toString());
        
        keyBuilder.setQueryPaths(
                QueryPaths.compile(
                        "this.departments", 
                        "..departments", 
                        "this.departments.employees",
                        ".departments..employees.anualLeaves"
                )
        );
        Assert.assertEquals(
                "this\r\n" +
                "\tinner join fetch departments\r\n" +
                "\t\tinner join fetch employees\r\n" +
                "\t\t\tleft join fetch anualLeaves\r\n", 
                pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null).getJoinNode().toString());
        
        keyBuilder.setQueryPaths(
                QueryPaths.compile(
                        "this.departments", 
                        "..departments", 
                        "this.departments.employees",
                        ".departments..employees.anualLeaves",
                        "departments.offices"
                )
        );
        Assert.assertEquals(
                "this\r\n" +
                "\tinner join fetch departments\r\n" +
                "\t\tinner join fetch employees\r\n" +
                "\t\t\tleft join fetch anualLeaves\r\n" +
                "\t\tleft join fetch offices\r\n", 
                pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null).getJoinNode().toString());
        
        keyBuilder.setQueryPaths(
                QueryPaths.compile(
                        "this.departments", 
                        "..departments", 
                        "this.departments.employees",
                        ".departments..employees.anualLeaves",
                        "departments.offices",
                        "this.inverstors.father"
                )
        );
        Assert.assertEquals(
                "this\r\n" +
                "\tinner join fetch departments\r\n" +
                "\t\tinner join fetch employees\r\n" +
                "\t\t\tleft join fetch anualLeaves\r\n" +
                "\t\tleft join fetch offices\r\n" +
                "\tleft join fetch inverstors\r\n" +
                "\t\tleft join fetch father\r\n", 
                pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null).getJoinNode().toString());
        
        keyBuilder.setQueryPaths(
                QueryPaths.compile(
                        "this.departments", 
                        "..departments", 
                        "this.departments.employees",
                        ".departments..employees.anualLeaves",
                        "departments.offices",
                        "this.inverstors.father",
                        "..partial(inverstors)..partial(mother)"
                )
        );
        Assert.assertEquals(
                "this\r\n" +
                "\tinner join fetch departments\r\n" +
                "\t\tinner join fetch employees\r\n" +
                "\t\t\tleft join fetch anualLeaves\r\n" +
                "\t\tleft join fetch offices\r\n" +
                "\tinner join fetch partial(inverstors)\r\n" +
                "\t\tleft join fetch father\r\n" +
                "\t\tinner join fetch partial(mother)\r\n", 
                pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null).getJoinNode().toString());
        
        keyBuilder.setQueryPaths(
                QueryPaths.compile(
                        "this.departments", 
                        "..departments", 
                        "this.departments.employees",
                        ".departments..employees.anualLeaves",
                        "departments.offices",
                        "this.inverstors.father",
                        "..partial(inverstors)..partial(mother)",
                        ".location.partial(city).all(province)"
                )
        );
        Assert.assertEquals(
                "this\r\n" +
                "\tinner join fetch departments\r\n" +
                "\t\tinner join fetch employees\r\n" +
                "\t\t\tleft join fetch anualLeaves\r\n" +
                "\t\tleft join fetch offices\r\n" +
                "\tinner join fetch partial(inverstors)\r\n" +
                "\t\tleft join fetch father\r\n" +
                "\t\tinner join fetch partial(mother)\r\n" +
                "\tleft join fetch location\r\n" +
                "\t\tleft join fetch partial(city)\r\n" +
                "\t\t\tleft join fetch province\r\n", 
                pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null).getJoinNode().toString());
        
        keyBuilder.setQueryPaths(
                QueryPaths.compile(
                        "this.departments", 
                        "..departments", 
                        "this.departments.employees",
                        ".departments..employees.anualLeaves",
                        "departments.offices",
                        "this.inverstors.father",
                        "..partial(inverstors)..partial(mother)",
                        ".location.partial(city).all(province)",
                        ".all(location).all(city).partial(province)"
                )
        );
        Assert.assertEquals(
                "this\r\n" +
                "\tinner join fetch departments\r\n" +
                "\t\tinner join fetch employees\r\n" +
                "\t\t\tleft join fetch anualLeaves\r\n" +
                "\t\tleft join fetch offices\r\n" +
                "\tinner join fetch partial(inverstors)\r\n" +
                "\t\tleft join fetch father\r\n" +
                "\t\tinner join fetch partial(mother)\r\n" +
                "\tleft join fetch location\r\n" +
                "\t\tleft join fetch partial(city)\r\n" +
                "\t\t\tleft join fetch partial(province)\r\n", 
                pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null).getJoinNode().toString());
    }
    
    @Test
    public void testSetQueryPath() {
        PathPlanKeyBuilder keyBuilder = new PathPlanKeyBuilder();
        AbstractPathPlanFactory pathPlanFactory = new PathPlanFactoryImpl();
        Assert.assertNull(pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null));
        
        keyBuilder.setQueryPaths(QueryPaths.compile("this.departments"));
        Assert.assertEquals(
                "this\r\n" +
                "\tleft join fetch departments\r\n", 
                pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null).getJoinNode().toString());
        
        keyBuilder.setQueryPaths(QueryPaths.compile("this.inverstors"));
        Assert.assertEquals(
                "this\r\n" +
                "\tleft join fetch inverstors\r\n", 
                pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null).getJoinNode().toString());
        
        keyBuilder.setQueryPaths(QueryPaths.compile("this.partial(inverstors).all(mother)"));
        Assert.assertEquals(
                "this\r\n" +
                "\tleft join fetch partial(inverstors)\r\n" +
                "\t\tleft join fetch mother\r\n", 
                pathPlanFactory.create(keyBuilder.build()).getSubPlans().get(null).getJoinNode().toString());
    }
    
    private static class PathPlanFactoryImpl extends AbstractPathPlanFactory {

        @Override
        protected EntityDelegate getEntityDelegate(String alias) {
            return new CompanyDelegate();
        }
        
        private static class ProvinceDelegate implements EntityDelegate {

            @Override
            public String getIdPropertyName() {
                return "id";
            }

            @Override
            public PropertyDelegate getNonIdProperty(String nonIdPropertyName) {
                return null;
            }

            @Override
            public boolean containsImplicitCollectionJoins() {
                return false;
            }
        }
        
        private static class CityDelegate implements EntityDelegate {

            @Override
            public String getIdPropertyName() {
                return "id";
            }

            @Override
            public PropertyDelegate getNonIdProperty(String nonIdPropertyName) {
                if (nonIdPropertyName.equals("province")) {
                    return new PropertyDelegateImpl(PropertyType.REFERENCE, new ProvinceDelegate());
                }
                return null;
            }

            @Override
            public boolean containsImplicitCollectionJoins() {
                return false;
            }
        }
        
        private static class LocationDelegate implements EntityDelegate {

            @Override
            public String getIdPropertyName() {
                return "id";
            }

            @Override
            public PropertyDelegate getNonIdProperty(String nonIdPropertyName) {
                if (nonIdPropertyName.equals("city")) {
                    return new PropertyDelegateImpl(PropertyType.REFERENCE, new CityDelegate());
                }
                return null;
            }

            @Override
            public boolean containsImplicitCollectionJoins() {
                return false;
            }
        }
        
        private static class InverstorDelegate implements EntityDelegate {

            @Override
            public String getIdPropertyName() {
                return "id";
            }

            @Override
            public PropertyDelegate getNonIdProperty(String nonIdPropertyName) {
                if (nonIdPropertyName.equals("father")) {
                    return new PropertyDelegateImpl(PropertyType.REFERENCE, new InverstorDelegate());
                }
                if (nonIdPropertyName.equals("mother")) {
                    return new PropertyDelegateImpl(PropertyType.REFERENCE, new InverstorDelegate());
                }
                return null;
            }

            @Override
            public boolean containsImplicitCollectionJoins() {
                return false;
            }
            
        }
        
        private static class CompanyDelegate implements EntityDelegate {

            @Override
            public String getIdPropertyName() {
                return "id";
            }

            @Override
            public PropertyDelegate getNonIdProperty(String nonIdPropertyName) {
                if (nonIdPropertyName.equals("departments")) {
                    return new PropertyDelegateImpl(PropertyType.COLLECTION, new DepartmentDelegate());
                }
                if (nonIdPropertyName.equals("inverstors")) {
                    return new PropertyDelegateImpl(PropertyType.COLLECTION, new InverstorDelegate());
                }
                if (nonIdPropertyName.equals("location")) {
                    return new PropertyDelegateImpl(PropertyType.REFERENCE, new LocationDelegate());
                }
                return null;
            }

            @Override
            public boolean containsImplicitCollectionJoins() {
                return false;
            }
            
        }
        
        private static class DepartmentDelegate implements EntityDelegate {

            @Override
            public String getIdPropertyName() {
                return "id";
            }

            @Override
            public PropertyDelegate getNonIdProperty(String nonIdPropertyName) {
                if (nonIdPropertyName.equals("employees")) {
                    return new PropertyDelegateImpl(PropertyType.COLLECTION, new EmployeeDelegate());
                }
                if (nonIdPropertyName.equals("offices")) {
                    return new PropertyDelegateImpl(PropertyType.COLLECTION, new OfficeDelegate());
                }
                return null;
            }

            @Override
            public boolean containsImplicitCollectionJoins() {
                return false;
            }
        }
        
        private static class EmployeeDelegate implements EntityDelegate {

            @Override
            public String getIdPropertyName() {
                return "id";
            }

            @Override
            public PropertyDelegate getNonIdProperty(String nonIdPropertyName) {
                if (nonIdPropertyName.equals("anualLeaves")) {
                    return new PropertyDelegateImpl(PropertyType.COLLECTION, new AnualLeaveDelegate());
                }
                return null;
            }

            @Override
            public boolean containsImplicitCollectionJoins() {
                return false;
            }
            
        }
        
        private static class OfficeDelegate implements EntityDelegate {

            @Override
            public String getIdPropertyName() {
                return "id";
            }

            @Override
            public PropertyDelegate getNonIdProperty(String nonIdPropertyName) {
                return null;
            }

            @Override
            public boolean containsImplicitCollectionJoins() {
                return false;
            }
            
        }
        
        private static class AnualLeaveDelegate implements EntityDelegate {

            @Override
            public String getIdPropertyName() {
                return "id";
            }

            @Override
            public PropertyDelegate getNonIdProperty(String nonIdPropertyName) {
                return null;
            }

            @Override
            public boolean containsImplicitCollectionJoins() {
                return false;
            }
            
        }
        
        private static class PropertyDelegateImpl implements PropertyDelegate {
            
            private boolean association;
            
            private boolean collection;
            
            private EntityDelegate associatedEntityDelegate;

            public PropertyDelegateImpl(PropertyType propertyType, EntityDelegate associatedEntityDelegate) {
                this.association = propertyType != PropertyType.SCALAR;
                this.collection = propertyType == PropertyType.COLLECTION;
                this.associatedEntityDelegate = associatedEntityDelegate;
            }

            public boolean isAssociation() {
                return this.association;
            }

            public boolean isCollection() {
                return this.collection;
            }

            public EntityDelegate getAssociatedEntityDelegate() {
                return this.associatedEntityDelegate;
            }
        }
        
        private static enum PropertyType {
            SCALAR,
            REFERENCE,
            COLLECTION
        }
    }
}
