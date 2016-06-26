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
package org.babyfishdemo.spring.dal.impl;

import java.util.List;

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.criteria.LikeMode;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.persistence.criteria.ext.SelfGetter;
import org.babyfish.persistence.criteria.ext.SelfGetterFromRestrictedTarget;
import org.babyfishdemo.spring.dal.EmployeeRepository;
import org.babyfishdemo.spring.entities.AnnualLeave;
import org.babyfishdemo.spring.entities.AnnualLeave_;
import org.babyfishdemo.spring.entities.Department;
import org.babyfishdemo.spring.entities.Department_;
import org.babyfishdemo.spring.entities.Employee;
import org.babyfishdemo.spring.entities.Employee_;
import org.babyfishdemo.spring.entities.Employee__;
import org.babyfishdemo.spring.entities.Name_;
import org.babyfishdemo.spring.model.EmployeeSpecification;
import org.babyfishdemo.spring.model.Page;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class EmployeeRepositoryImpl implements EmployeeRepository {

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public List<Employee> getEmployees(
            EmployeeSpecification specification,
            Employee__... queryPaths) {
        return this
                .em
                .createQuery(this.createCriteriaQuery(specification))
                .setQueryPaths(queryPaths)
                .getResultList();
    }

    @Override
    public Page<Employee> getEmployees(
            EmployeeSpecification specification,
            int pageIndex, 
            int pageSize, 
            Employee__... queryPaths) {
        return new PageBuilder<>(
                this
                .em
                .createQuery(this.createCriteriaQuery(specification))
                .setQueryPaths(queryPaths),
                pageIndex,
                pageSize
        ).build();
    }

    @Override
    public Employee mergeEmployee(Employee employee) {
        return this.em.merge(employee);
    }

    @Override
    public int deleteAllEmployees() {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaDelete<Employee> cd = cb.createCriteriaDelete(Employee.class);
        cd.from(Employee.class);
        return this.em.createQuery(cd).executeUpdate();
    }

    private CriteriaQuery<Employee> createCriteriaQuery(EmployeeSpecification specification) {
        
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> employee = cq.from(Employee.class);
        
        if (specification != null) {
            
            Predicate likeFirstNamePredicate = null;
            Predicate likeLastNamePredicate = null;
            Predicate genderPredicate = null;
            Predicate likeDepartmentNamePredicate = null;
            
            if (specification.getLikeFirstName() != null) {
                likeFirstNamePredicate = cb.insensitivelyLike(
                        employee.get(Employee_.name).get(Name_.firstName),
                        specification.getLikeFirstName(), 
                        LikeMode.ANYWHERE
                );
            }
            if (specification.getLikeLastName() != null) {
                likeLastNamePredicate = cb.insensitivelyLike(
                        employee.get(Employee_.name).get(Name_.lastName), 
                        specification.getLikeLastName(),
                        LikeMode.ANYWHERE
                );
            }
            if (specification.getLikeDepartmentName() != null) {
                likeDepartmentNamePredicate = cb.insensitivelyLike(
                        employee.get(Employee_.department).get(Department_.name), 
                        specification.getLikeDepartmentName(), 
                        LikeMode.ANYWHERE
                );
            }
            if (specification.getGender() != null) {
                genderPredicate = cb.equal(
                        employee.get(Employee_.gender), 
                        specification.getGender()
                );
            }
            
            cq.where(
                    likeFirstNamePredicate,
                    likeLastNamePredicate,
                    genderPredicate,
                    
                    cb.between( //return null if both min and max are null
                            employee.get(Employee_.birthday), 
                            specification.getMinBirthday(), 
                            specification.getMaxBirthday()
                    ),
                    
                    likeDepartmentNamePredicate,
                    
                    cb
                    .dependencyPredicateBuilder(employee, Department.class)
                    .addSelfGetter(new SelfGetter<Employee, Department>() { //Change to lambda for Java8
                        @Override
                        public Path<Employee> getSelf(XRoot<Department> target) {
                            return target.join(Department_.employees);
                        }
                    })
                    .includeAny(Department_.name, specification.getIncludedDepartmentNames())
                    .excludeAll(Department_.name, specification.getExcludedDepartmentNames())
                    .build(), // returns null if both includeDepartmentNames and excludeDepartmentNames are null or empty
                    
                    cb
                    .dependencyPredicateBuilder(employee, AnnualLeave.class)
                    .addSelfGetter(new SelfGetterFromRestrictedTarget<Employee, AnnualLeave>() {
                        @Override
                        public Path<Employee> getSelf(XRoot<AnnualLeave> target) {
                            return target.get(AnnualLeave_.employee);
                        }
                        @Override
                        public Predicate restrictTarget(XCriteriaBuilder cb, XRoot<AnnualLeave> target) {
                            return cb.equal(target.get(AnnualLeave_.state), AnnualLeave.State.PENDING);
                        }
                    })
                    .has(specification.getHasPendingAnnualLeaves())
                    .build() // returns null if hasPendingAnnualLeaves is null
            );
        }
        
        return cq;
    }
}
