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

import java.util.List;

import javax.persistence.PersistenceContext;

import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XTypedQuery;
import org.babyfish.persistence.criteria.XCriteriaBuilder;
import org.babyfish.persistence.criteria.XCriteriaQuery;
import org.babyfish.persistence.criteria.XRoot;
import org.babyfish.springframework.test.dal.ProductRepository;
import org.babyfish.springframework.test.entities.Product;
import org.babyfish.springframework.test.entities.Product_;
import org.babyfish.springframework.test.entities.Product__;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tao Chen
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class ProductRepositoryImpl implements ProductRepository {
    
    @PersistenceContext
    private XEntityManager em;

    @Override
    public List<Product> getProductsLikeName(String name, Product__ ... queryPaths) {
        XCriteriaBuilder cb = this.em.getCriteriaBuilder();
        XCriteriaQuery<Product> cq = cb.createQuery(Product.class);
        XRoot<Product> product = cq.from(Product.class);
        cq
        .where(cb.like(product.get(Product_.name), name))
        .orderBy(cb.asc(product.get(Product_.name)));
        XTypedQuery<Product> q = this.em.createQuery(cq);
        return q.setQueryPaths(queryPaths).getResultList();
    }

    @Override
    public void persistProduct(Product product) {
        this.em.persist(product);
    }
}
