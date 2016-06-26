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
package org.babyfish.hibernate.collection.type;

import java.util.Properties;

import org.babyfish.lang.Arguments;
import org.babyfish.model.jpa.metadata.JPAModelProperty;

/**
 * @author Tao Chen
 */
public class MACollectionProperties extends Properties {

    private static final long serialVersionUID = 3541994016030119246L;
    
    private transient JPAModelProperty modelProperty;
    
    public MACollectionProperties(JPAModelProperty modelProperty, Properties properties) {
        this.modelProperty = Arguments.mustNotBeNull("modelProperty", modelProperty);
        if (properties != null) {
            this.putAll(properties);
        }
    }
    
    public JPAModelProperty getModelProperty() {
        return this.modelProperty;
    }
}
