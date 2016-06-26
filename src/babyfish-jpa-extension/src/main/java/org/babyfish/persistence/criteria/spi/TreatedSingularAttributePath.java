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

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

/**
 * @author Tao Chen
 */
public class TreatedSingularAttributePath<X> extends SingularAttributePath<X> {

    private static final long serialVersionUID = -5823163815302791282L;

    private SingularAttributePath<? super X> path;
    
    private Class<X> type; 
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TreatedSingularAttributePath(SingularAttributePath<? super X> path, Class<X> type) {
        //Don't refactor the parameter type of SingularAttributePath to path
        //This class can not be a wrapper of join absolutely,
        //Please see QueryContext.getEntityMapKey(XFrom<?, ?>) understand why it is so important
        super((AbstractPath)path.getParentPath(), (SingularAttribute)path.getAttribute());
    }

    @Override
    public Class<? extends X> getJavaType() {
        return this.type;
    }
    
    @Override
    public String getAlias() {
        return this.path.getAlias();
    }

    @Override
    public Path<? super X> getTreatedParent() {
        return this.path;
    }
}
