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
package org.babyfish.springframework.test.dal.impl.jpa;

import javax.persistence.PersistenceContext;

import org.babyfish.persistence.XEntityManager;
import org.babyfish.springframework.test.dal.ProductCategoryRepository;
import org.babyfish.springframework.test.entities.ProductCategory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tao Chen
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    @PersistenceContext
    private XEntityManager em;
    
    @Override
    public void persistProductCategory(ProductCategory productCategory) {
        this.em.persist(productCategory);
    }
}
