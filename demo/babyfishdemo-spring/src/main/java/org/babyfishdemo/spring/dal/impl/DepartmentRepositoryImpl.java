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

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;

import org.babyfish.persistence.XEntityManager;
import org.babyfishdemo.spring.dal.DepartmentRepository;
import org.babyfishdemo.spring.entities.Department;
import org.springframework.stereotype.Repository;

/**
 * @author Tao Chen
 */
@Repository
public class DepartmentRepositoryImpl implements DepartmentRepository {

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public Department mergeDepartment(Department department) {
        return this.em.merge(department);
    }

    @Override
    public int deleteAllDepartments() {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaDelete<Department> cd = cb.createCriteriaDelete(Department.class);
        cd.from(Department.class);
        return this.em.createQuery(cd).executeUpdate();
    }
}
