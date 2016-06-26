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
package org.babyfish.springframework.test.bll.impl;

import java.util.List;

import javax.annotation.Resource;

import org.babyfish.springframework.test.bll.ProductService;
import org.babyfish.springframework.test.dal.ProductCategoryRepository;
import org.babyfish.springframework.test.dal.ProductRepository;
import org.babyfish.springframework.test.entities.Product;
import org.babyfish.springframework.test.entities.ProductCategory;
import org.babyfish.springframework.test.entities.Product__;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tao Chen
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductCategoryRepository productCategoryRepsoitory;
    
    @Resource
    private ProductRepository productRepository;
    
    @Override
    public void persistProductCategory(ProductCategory productCategory) {
        this.productCategoryRepsoitory.persistProductCategory(productCategory);
    }
    
    @Override
    public void persistProduct(Product product) {
        this.productRepository.persistProduct(product);
    }
    
    @Override
    public List<Product> getProductLikeName(String name, Product__ ... queryPaths) {
        return this.productRepository.getProductsLikeName(name, queryPaths);
    }

}
