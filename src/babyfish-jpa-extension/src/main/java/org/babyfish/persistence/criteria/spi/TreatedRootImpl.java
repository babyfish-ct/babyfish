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
package org.babyfish.persistence.criteria.spi;

import org.babyfish.lang.Arguments;
import org.babyfish.persistence.criteria.XRoot;

/**
 * @author Tao Chen
 */
public class TreatedRootImpl<X> extends RootImpl<X> {

    private static final long serialVersionUID = 8081993778608242059L;
    
    private RootImpl<? super X> root;
    
    private Class<X> type;

    public TreatedRootImpl(RootImpl<? super X> root, Class<X> type) {
        super(root.getCommonAbstractCriteria(), 
                root.getCriteriaBuilder().getEntityManagerFactory().getMetamodel().entity(type));
        Arguments.mustBeCompatibleWithOther("type", type, "root.getJavaType()", root.getJavaType());
        this.root = root;
        this.type = type;
    }
    
    public Class<X> getJavaType() {
        return this.type;
    }
    
    @Override
    public XRoot<? super X> getTreatedParent() {
        return this.root;
    }
}
