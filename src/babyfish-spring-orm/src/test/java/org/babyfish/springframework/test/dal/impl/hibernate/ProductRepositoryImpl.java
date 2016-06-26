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
package org.babyfish.springframework.test.dal.impl.hibernate;

import java.util.List;

import javax.annotation.Resource;

import org.babyfish.hibernate.XSession;
import org.babyfish.hibernate.XSessionFactory;
import org.babyfish.springframework.test.dal.ProductRepository;
import org.babyfish.springframework.test.entities.Product;
import org.babyfish.springframework.test.entities.Product__;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tao Chen
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class ProductRepositoryImpl implements ProductRepository {

    @Resource
    private XSessionFactory sessionFactory;
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Product> getProductsLikeName(String name, Product__... queryPaths) {
        XSession session = this.sessionFactory.getCurrentSession();
        return 
                session
                .createQuery("from Product p where p.name like :name")
                .setString("name", name)
                .setQueryPaths(queryPaths)
                .list();
    }

    @Override
    public void persistProduct(Product product) {
        XSession session = this.sessionFactory.getCurrentSession();
        session.save(product);
    }
}
