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
package org.babyfish.test.hibernate.model.bagandref.delay;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MASet;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.data.LazinessManageable;
import org.babyfish.data.event.ModificationType;
import org.babyfish.data.event.PropertyVersion;
import org.babyfish.model.spi.reference.Reference;
import org.babyfish.model.spi.reference.ReferenceImpl;
import org.babyfish.test.hibernate.model.AbstractHibernateTest;
import org.babyfish.test.hibernate.model.setandref.Department;
import org.babyfish.test.hibernate.model.setandref.Employee;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.LongType;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class SetAndReferenceTest extends AbstractHibernateTest {

    @BeforeClass
    public static void initClass() {
        initSessionFactory(Department.class, Employee.class);
    }
    
    private void init(final boolean hasRelationship) {
        Consumer<Session> handler = session -> {
            session.createSQLQuery("DELETE FROM sr_EMPLOYEE").executeUpdate();
            session.createSQLQuery("DELETE FROM sr_DEPARTMENT").executeUpdate();
            session
                .createSQLQuery("INSERT INTO sr_DEPARTMENT(DEPARTMENT_ID, NAME) VALUES(?, ?)")
                .setLong(0, 1L)
                .setString(1, "department")
                .executeUpdate();
            Query query =
                session
                .createSQLQuery("INSERT INTO sr_EMPLOYEE(EMPLOYEE_ID, NAME, DEPARTMENT_ID) VALUES(?, ?, ?)")
                .setLong(0, 1L)
                .setString(1, "employee");
            if (hasRelationship) {
                query.setLong(2, 1L);
            } else {
                query.setParameter(2, null, LongType.INSTANCE);
            }
            query.executeUpdate();
        };
        execute(handler);
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertLoadedCollectionReferences(Collection<E> c, E ... references) {
        LazinessManageable lazinessManageable = (LazinessManageable)c;
        Assert.assertTrue(lazinessManageable.isLoaded());
        Assert.assertEquals(references.length, c.size());
        int index = 0;
        for (E e : c) {
            Assert.assertSame(references[index++], e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertUnloadedCollectionReferences(Collection<E> c, E ... references) {
        LazinessManageable lazinessManageable = (LazinessManageable)c;
        Assert.assertFalse(lazinessManageable.isLoaded());
        lazinessManageable.load();
        Assert.assertEquals(references.length, c.size());
        int index = 0;
        for (E e : c) {
            Assert.assertSame(references[index++], e);
        }
    }
    
    @Test
    public void testAddLoadedEmployeeWhenHasNotRelationship() {
        this.init(false);
        Consumer<Session> handler = session -> {
            Department department = (Department)session.load(Department.class, 1L);
            Assert.assertTrue(department instanceof HibernateProxy);
            Department realDepartment =
                (Department)
                ((HibernateProxy)department)
                .getHibernateLazyInitializer()
                .getImplementation();
            Employee employee = (Employee)session.load(Employee.class, 1L);
            Assert.assertTrue(employee instanceof HibernateProxy);
            Employee realEmployee = 
                (Employee)
                ((HibernateProxy)employee)
                .getHibernateLazyInitializer()
                .getImplementation();
            final Map<String, Employee> eventArgs = new LinkedHashMap<String, Employee>();
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.ATTACH, e.getModificationType());
                            eventArgs.put("ai", e.getElement(PropertyVersion.ATTACH));
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.ATTACH, e.getModificationType());
                            eventArgs.put("ae", e.getElement(PropertyVersion.ATTACH));
                        }
                    });
            Assert.assertNull(employee.getDepartment());
            Assert.assertTrue(realDepartment.getEmployees().add(realEmployee));
            Assert.assertSame(realDepartment, employee.getDepartment());
            Assert.assertNotSame(eventArgs.get("ai"), employee);
            Assert.assertNotSame(eventArgs.get("ae"), employee);
            Assert.assertSame(eventArgs.get("ai"), realEmployee);
            Assert.assertSame(eventArgs.get("ae"), realEmployee);
            assertUnloadedCollectionReferences(department.getEmployees(), realEmployee);
        };
        execute(handler);
    }
    
    @Test
    public void testAddGettedEmployeeWhenHasNotRelationship() {
        this.init(false);
        Consumer<Session> handler = session -> {
            Department department = (Department)session.get(Department.class, 1L);
            Employee employee = (Employee)session.get(Employee.class, 1L);
            final Map<String, Employee> eventArgs = new LinkedHashMap<String, Employee>();
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.ATTACH, e.getModificationType());
                            eventArgs.put("ai", e.getElement(PropertyVersion.ATTACH));
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.ATTACH, e.getModificationType());
                            eventArgs.put("ae", e.getElement(PropertyVersion.ATTACH));
                        }
                    });
            Assert.assertNull(employee.getDepartment());
            Assert.assertTrue(department.getEmployees().add(employee));
            Assert.assertSame(department, employee.getDepartment());
            Assert.assertSame(eventArgs.get("ai"), employee);
            Assert.assertSame(eventArgs.get("ae"), employee);
            assertUnloadedCollectionReferences(department.getEmployees(), employee);
        };
        execute(handler);
    }
    
    @Test
    public void testAddOtherEmployeeBoforeQueryOldEmployeeWhenHasNotRelationship() {
        this.init(false);
        Consumer<Session> handler;
        final Reference<Employee> employeeRef = new ReferenceImpl<Employee>();
        final Map<String, Object> eventArgs = new LinkedHashMap<String, Object>();
        handler = session -> {
            employeeRef.set((Employee)session.load(Employee.class, 1L));
            Hibernate.initialize(employeeRef.get().getDepartment());
        };
        execute(handler);
        Assert.assertTrue(employeeRef.get() instanceof HibernateProxy);
        handler = session -> {
            Department department = (Department)session.get(Department.class, 1L);
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.ATTACH, e.getModificationType());
                            eventArgs.put("ai", e.getElement(PropertyVersion.ATTACH));
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.ATTACH, e.getModificationType());
                            eventArgs.put("ae", e.getElement(PropertyVersion.ATTACH));
                        }
                    });
            Assert.assertTrue(department.getEmployees().add(employeeRef.get()));
            Employee employee = (Employee)session.get(Employee.class, 1L);
            Assert.assertNotNull(employee);
            Assert.assertSame(employeeRef.get(), eventArgs.get("ai"));
            Assert.assertSame(employeeRef.get(), eventArgs.get("ae"));
            Assert.assertNull(employee.getDepartment());
            assertUnloadedCollectionReferences(department.getEmployees(), employeeRef.get());
            Assert.assertSame(department, employeeRef.get().getDepartment());
        };
        execute(handler);
    }
    
    @Test
    public void testAddOtherEmployeeAfterQueryOldEmployeeWhenHasNotRelationship() {
        this.init(false);
        Consumer<Session> handler;
        final Reference<Employee> employeeRef = new ReferenceImpl<Employee>();
        final Map<String, Object> eventArgs = new LinkedHashMap<String, Object>();
        handler = session -> {
            employeeRef.set((Employee)session.load(Employee.class, 1L));
            Hibernate.initialize(employeeRef.get().getDepartment());
        };
        execute(handler);
        Assert.assertTrue(employeeRef.get() instanceof HibernateProxy);
        handler = session -> {
            Department department = (Department)session.get(Department.class, 1L);
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.ATTACH, e.getModificationType());
                            eventArgs.put("ai", e.getElement(PropertyVersion.ATTACH));
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.ATTACH, e.getModificationType());
                            eventArgs.put("ae", e.getElement(PropertyVersion.ATTACH));
                        }
                    });
            Employee employee = (Employee)session.get(Employee.class, 1L);
            Assert.assertTrue(department.getEmployees().add(employeeRef.get()));
            Assert.assertSame(employeeRef.get(), eventArgs.get("ai"));
            Assert.assertSame(employeeRef.get(), eventArgs.get("ae"));
            Assert.assertNull(employee.getDepartment());
            Assert.assertSame(department, employeeRef.get().getDepartment());
            assertUnloadedCollectionReferences(department.getEmployees(), employeeRef.get());
        };
        execute(handler);
    }
    
    @Test
    public void testAddLoadedEmployeeWhenHasRelationship() {
        this.init(true);
        final Map<String, Employee> eventArgs = new LinkedHashMap<String, Employee>();
        Consumer<Session> handler = session -> {
            Department department = (Department)session.load(Department.class, 1L);
            Assert.assertTrue(department instanceof HibernateProxy);
            Department realDepartment =
                (Department)
                ((HibernateProxy)department)
                .getHibernateLazyInitializer()
                .getImplementation();
            Employee employee = (Employee)session.load(Employee.class, 1L);
            Assert.assertTrue(employee instanceof HibernateProxy);
            Employee realEmployee = 
                (Employee)
                ((HibernateProxy)employee)
                .getHibernateLazyInitializer()
                .getImplementation();
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.REPLACE, e.getModificationType());
                            eventArgs.put("di", e.getElement(PropertyVersion.DETACH));
                            eventArgs.put("ai", e.getElement(PropertyVersion.ATTACH));
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.REPLACE, e.getModificationType());
                            eventArgs.put("de", e.getElement(PropertyVersion.DETACH));
                            eventArgs.put("ae", e.getElement(PropertyVersion.ATTACH));
                        }
                    });
            Assert.assertSame(department, employee.getDepartment());
            Assert.assertFalse(realDepartment.getEmployees().add(realEmployee));
            Assert.assertSame(realDepartment, employee.getDepartment());
            Assert.assertSame(employee, eventArgs.get("di"));
            Assert.assertSame(employee, eventArgs.get("de"));
            Assert.assertSame(realEmployee, eventArgs.get("ai"));
            Assert.assertSame(realEmployee, eventArgs.get("ae"));
            assertUnloadedCollectionReferences(department.getEmployees(), realEmployee);
        };
        execute(handler);
    }
    
    @Test
    public void testAddGettedEmployeeWhenHasRelationship() {
        this.init(true);
        Consumer<Session> handler = session -> {
            Department department = (Department)session.get(Department.class, 1L);
            Employee employee = (Employee)session.get(Employee.class, 1L);
            Assert.assertFalse(employee instanceof HibernateProxy);
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.fail();
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.fail();
                        }
                    });
            Assert.assertFalse(department.getEmployees().add(employee));
            assertUnloadedCollectionReferences(department.getEmployees(), employee);
        };
        execute(handler);
    }
    
    @Test
    public void testAddOtherEmployeeBoforeQueryOldEmployeeWhenHasRelationship() {
        this.init(true);
        Consumer<Session> handler;
        final Reference<Department> departmentRef = new ReferenceImpl<Department>();
        final Reference<Employee> employeeRef = new ReferenceImpl<Employee>();
        final Map<String, Object> eventArgs = new LinkedHashMap<String, Object>();
        handler = session -> {
            employeeRef.set((Employee)session.load(Employee.class, 1L));
            departmentRef.set(employeeRef.get().getDepartment());
            Hibernate.initialize(employeeRef.get());
            ((LazinessManageable)departmentRef.get().getEmployees()).load();
            Assert.assertEquals(1, departmentRef.get().getEmployees().size());
        };
        execute(handler);
        Assert.assertTrue(employeeRef.get() instanceof HibernateProxy);
        handler = session -> {
            Department department = (Department)session.get(Department.class, 1L);
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.REPLACE, e.getModificationType());
                            eventArgs.put("di", e.getElement(PropertyVersion.DETACH));
                            eventArgs.put("ai", e.getElement(PropertyVersion.ATTACH));
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.REPLACE, e.getModificationType());
                            eventArgs.put("de", e.getElement(PropertyVersion.DETACH));
                            eventArgs.put("ae", e.getElement(PropertyVersion.ATTACH));
                        }
                    });
            Assert.assertFalse(department.getEmployees().add(employeeRef.get()));
            Employee employee = (Employee)session.get(Employee.class, 1L);
            Assert.assertNotNull(employee);
            Assert.assertSame(employeeRef.get(), eventArgs.get("ai"));
            Assert.assertSame(employeeRef.get(), eventArgs.get("ae"));
            Assert.assertSame(employee, eventArgs.get("di"));
            Assert.assertSame(employee, eventArgs.get("de"));
            Assert.assertSame(department, employeeRef.get().getDepartment());
            assertUnloadedCollectionReferences(department.getEmployees(), employeeRef.get());
            Assert.assertNull(employee.getDepartment());
            assertLoadedCollectionReferences(departmentRef.get().getEmployees());
        };
        execute(handler);
    }
    
    @Test
    public void testAddOtherEmployeeAfterQueryOldEmployeeWhenHasRelationship() {
        this.init(true);
        Consumer<Session> handler;
        final Reference<Department> departmentRef = new ReferenceImpl<Department>();
        final Reference<Employee> employeeRef = new ReferenceImpl<Employee>();
        final Map<String, Object> eventArgs = new LinkedHashMap<String, Object>();
        handler = session -> {
            employeeRef.set((Employee)session.load(Employee.class, 1L));
            departmentRef.set(employeeRef.get().getDepartment());
            Hibernate.initialize(employeeRef.get().getDepartment());
            ((LazinessManageable)departmentRef.get().getEmployees()).load();
            Assert.assertEquals(1, departmentRef.get().getEmployees().size());
        };
        execute(handler);
        Assert.assertTrue(employeeRef.get() instanceof HibernateProxy);
        handler = session -> {
            Department department = (Department)session.get(Department.class, 1L);
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.REPLACE, e.getModificationType());
                            eventArgs.put("di", e.getElement(PropertyVersion.DETACH));
                            eventArgs.put("ai", e.getElement(PropertyVersion.ATTACH));
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.REPLACE, e.getModificationType());
                            eventArgs.put("de", e.getElement(PropertyVersion.DETACH));
                            eventArgs.put("ae", e.getElement(PropertyVersion.ATTACH));
                        }
                    });
            Employee employee = (Employee)session.get(Employee.class, 1L);
            Assert.assertSame(department, employee.getDepartment());
            Assert.assertFalse(department.getEmployees().add(employeeRef.get()));
            Assert.assertSame(employeeRef.get(), eventArgs.get("ai"));
            Assert.assertSame(employeeRef.get(), eventArgs.get("ae"));
            Assert.assertSame(employee, eventArgs.get("di"));
            Assert.assertSame(employee, eventArgs.get("de"));
            Assert.assertNull(employee.getDepartment());
            assertLoadedCollectionReferences(departmentRef.get().getEmployees());
            Assert.assertSame(department, employeeRef.get().getDepartment());
            assertUnloadedCollectionReferences(department.getEmployees(), employeeRef.get());
        };
        execute(handler);
    }
    
    @Test
    public void testRemoveLoadedEmployeeWhenHasNotRelationship() {
        this.init(false);
        Consumer<Session> handler = session -> {
            Department department = (Department)session.load(Department.class, 1L);
            Assert.assertTrue(department instanceof HibernateProxy);
            Department realDepartment =
                (Department)
                ((HibernateProxy)department)
                .getHibernateLazyInitializer()
                .getImplementation();
            Employee employee = (Employee)session.load(Employee.class, 1L);
            Assert.assertTrue(employee instanceof HibernateProxy);
            Employee realEmployee = 
                (Employee)
                ((HibernateProxy)employee)
                .getHibernateLazyInitializer()
                .getImplementation();
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.fail();
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.fail();
                        }
                    });
            Assert.assertFalse(realDepartment.getEmployees().remove(realEmployee));
            Assert.assertNull(employee.getDepartment());
            assertUnloadedCollectionReferences(department.getEmployees());
        };
        execute(handler);
    }
    
    @Test
    public void testRemoveGettedEmployeeWhenHasNotRelationship() {
        this.init(false);
        Consumer<Session> handler = session -> {
            Department department = (Department)session.get(Department.class, 1L);
            Employee employee = (Employee)session.load(Employee.class, 1L);
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.fail();
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.fail();
                        }
                    });
            Assert.assertFalse(department.getEmployees().remove(employee));
            Assert.assertNull(employee.getDepartment());
            assertUnloadedCollectionReferences(department.getEmployees());
        };
        execute(handler);
    }
    
    @Test
    public void testRemovedOtherEmployeeBoforeQueryOldEmployeeWhenHasNotRelationship() {
        this.init(false);
        Consumer<Session> handler;
        final Reference<Employee> employeeRef = new ReferenceImpl<Employee>();
        handler = session -> {
            employeeRef.set((Employee)session.load(Employee.class, 1L));
            Hibernate.initialize(employeeRef.get());
            Hibernate.initialize(employeeRef.get().getDepartment());
        };
        execute(handler);
        Assert.assertTrue(employeeRef.get() instanceof HibernateProxy);
        handler = session -> {
            Department department = (Department)session.get(Department.class, 1L);
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.fail();
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.fail();
                        }
                    });
            Assert.assertFalse(department.getEmployees().remove(employeeRef.get()));
            Employee employeeInSession = (Employee)session.get(Employee.class, 1L);
            Assert.assertNotNull(employeeInSession);
            assertUnloadedCollectionReferences(department.getEmployees());
        };
        execute(handler);
    }
    
    @Test
    public void testRemoveOtherEmployeeAfterQueryOldEmployeeWhenHasNotRelationship() {
        this.init(false);
        Consumer<Session> handler;
        final Reference<Employee> employeeRef = new ReferenceImpl<Employee>();
        handler = session -> {
            employeeRef.set((Employee)session.load(Employee.class, 1L));
            Hibernate.initialize(employeeRef.get());
            Hibernate.initialize(employeeRef.get().getDepartment());
        };
        execute(handler);
        Assert.assertTrue(employeeRef.get() instanceof HibernateProxy);
        handler = session -> {
            Department department = (Department)session.get(Department.class, 1L);
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.fail();
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.fail();
                        }
                    });
            Employee employeeInSession = (Employee)session.get(Employee.class, 1L);
            Assert.assertFalse(department.getEmployees().remove(employeeRef.get()));
            Assert.assertNull(employeeInSession.getDepartment());
            assertUnloadedCollectionReferences(department.getEmployees());
        };
        execute(handler);
    }
    
    @Test
    public void testRemoveLoadedEmployeeWhenHasRelationship() {
        this.init(true);
        final Map<String, Employee> eventArgs = new LinkedHashMap<String, Employee>();
        Consumer<Session> handler = session -> {
            Department department = (Department)session.load(Department.class, 1L);
            Assert.assertTrue(department instanceof HibernateProxy);
            Department realDepartment =
                (Department)
                ((HibernateProxy)department)
                .getHibernateLazyInitializer()
                .getImplementation();
            Employee employee = (Employee)session.load(Employee.class, 1L);
            Assert.assertTrue(employee instanceof HibernateProxy);
            Employee realEmployee = 
                (Employee)
                ((HibernateProxy)employee)
                .getHibernateLazyInitializer()
                .getImplementation();
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.DETACH, e.getModificationType());
                            eventArgs.put("di", e.getElement(PropertyVersion.DETACH));
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.DETACH, e.getModificationType());
                            eventArgs.put("de", e.getElement(PropertyVersion.DETACH));
                        }
                    });
            Assert.assertSame(department, employee.getDepartment());
            Assert.assertTrue(realDepartment.getEmployees().remove(realEmployee));
            Assert.assertNull(employee.getDepartment());
            Assert.assertSame(employee, eventArgs.get("di"));
            Assert.assertSame(employee, eventArgs.get("de"));
            assertUnloadedCollectionReferences(department.getEmployees());
        };
        execute(handler);
    }
    
    @Test
    public void testRemoveGettedEmployeeWhenHasRelationship() {
        this.init(true);
        final Map<String, Employee> eventArgs = new LinkedHashMap<String, Employee>();
        Consumer<Session> handler = session -> {
            Department department = (Department)session.get(Department.class, 1L);
            Employee employee = (Employee)session.get(Employee.class, 1L);
            Assert.assertFalse(employee instanceof HibernateProxy);
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.DETACH, e.getModificationType());
                            eventArgs.put("di", e.getElement(PropertyVersion.DETACH));
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.DETACH, e.getModificationType());
                            eventArgs.put("de", e.getElement(PropertyVersion.DETACH));
                        }
                    });
            Assert.assertSame(department, employee.getDepartment());
            Assert.assertTrue(department.getEmployees().remove(employee));
            Assert.assertNull(employee.getDepartment());
            Assert.assertSame(employee, eventArgs.get("di"));
            Assert.assertSame(employee, eventArgs.get("de"));
            assertUnloadedCollectionReferences(department.getEmployees());
        };
        execute(handler);
    }
    
    @Test
    public void testRemoveOtherEmployeeBoforeQueryOldEmployeeWhenHasRelationship() {
        this.init(true);
        Consumer<Session> handler;
        final Reference<Department> departmentRef = new ReferenceImpl<Department>();
        final Reference<Employee> employeeRef = new ReferenceImpl<Employee>();
        final Map<String, Object> eventArgs = new LinkedHashMap<String, Object>();
        handler = session -> {
            employeeRef.set((Employee)session.load(Employee.class, 1L));
            departmentRef.set(employeeRef.get().getDepartment());
            Hibernate.initialize(employeeRef.get().getDepartment());
            ((LazinessManageable)departmentRef.get().getEmployees()).load();
            Assert.assertEquals(1, departmentRef.get().getEmployees().size());
        };
        execute(handler);
        Assert.assertTrue(employeeRef.get() instanceof HibernateProxy);
        handler = session -> {
            Department department = (Department)session.get(Department.class, 1L);
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.DETACH, e.getModificationType());
                            eventArgs.put("di", e.getElement(PropertyVersion.DETACH));
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.DETACH, e.getModificationType());
                            eventArgs.put("de", e.getElement(PropertyVersion.DETACH));
                        }
                    });
            Assert.assertTrue(department.getEmployees().remove(employeeRef.get()));
            Employee employee = (Employee)session.get(Employee.class, 1L);
            Assert.assertNotNull(employee);
            Assert.assertSame(employee, eventArgs.get("di"));
            Assert.assertSame(employee, eventArgs.get("de"));
            Assert.assertSame(departmentRef.get(), employeeRef.get().getDepartment());
            assertLoadedCollectionReferences(departmentRef.get().getEmployees(), employeeRef.get());
            Assert.assertNull(employee.getDepartment());
            assertUnloadedCollectionReferences(department.getEmployees());
        };
        execute(handler);
    }
    
    @Test
    public void testRemoveOtherEmployeeAfterQueryOldEmployeeWhenHasRelationship() {
        this.init(true);
        Consumer<Session> handler;
        final Reference<Department> departmentRef = new ReferenceImpl<Department>();
        final Reference<Employee> employeeRef = new ReferenceImpl<Employee>();
        final Map<String, Object> eventArgs = new LinkedHashMap<String, Object>();
        handler = session -> {
            employeeRef.set((Employee)session.load(Employee.class, 1L));
            departmentRef.set(employeeRef.get().getDepartment());
            Hibernate.initialize(employeeRef.get().getDepartment());
            ((LazinessManageable)departmentRef.get().getEmployees()).load();
            Assert.assertEquals(1, departmentRef.get().getEmployees().size());
        };
        execute(handler);
        Assert.assertTrue(employeeRef.get() instanceof HibernateProxy);
        handler = session -> {
            Department department = (Department)session.get(Department.class, 1L);
            ((MASet<Employee>)department.getEmployees()).addElementListener(
                    new ElementListener<Employee>() {
                        @Override
                        public void modifying(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.DETACH, e.getModificationType());
                            eventArgs.put("di", e.getElement(PropertyVersion.DETACH));
                        }
                        @Override
                        public void modified(ElementEvent<Employee> e) throws Throwable {
                            Assert.assertEquals(ModificationType.DETACH, e.getModificationType());
                            eventArgs.put("de", e.getElement(PropertyVersion.DETACH));
                        }
                    });
            Employee employee = (Employee)session.get(Employee.class, 1L);
            Assert.assertSame(department, employee.getDepartment());
            Assert.assertTrue(department.getEmployees().remove(employeeRef.get()));
            Assert.assertSame(employee, eventArgs.get("di"));
            Assert.assertSame(employee, eventArgs.get("de"));
            Assert.assertSame(departmentRef.get(), employeeRef.get().getDepartment());
            assertLoadedCollectionReferences(departmentRef.get().getEmployees(), employeeRef.get());
            Assert.assertNull(employee.getDepartment());
            assertUnloadedCollectionReferences(department.getEmployees());
        };
        execute(handler);
    }
}
