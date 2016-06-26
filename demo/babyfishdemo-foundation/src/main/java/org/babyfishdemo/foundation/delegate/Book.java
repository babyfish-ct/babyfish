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
package org.babyfishdemo.foundation.delegate;

import java.math.BigDecimal;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;

/**
 * @author Tao Chen
 */
public class Book {
    
    private String name;
    
    private BigDecimal price;
    
    private PropertyChangedListener propertyChangedListener;
    
    public Book(String name, BigDecimal price) {
        this.setName(name);
        this.setPrice(price);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Arguments.mustNotBeEmpty(
                "name", 
                Arguments.mustNotBeNull("name", name)
        );
        String oldName = this.name;
        if (Nulls.equals(oldName, name)) {
            return;
        }
        this.name = name;
        if (this.propertyChangedListener != null) {
            this.propertyChangedListener.propertyChanged(
                    new PropertyChanagedEvent(this, "name", oldName, name)
            );
        }
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        Arguments.mustBeGreaterThanValue(
                "price", 
                Arguments.mustNotBeNull("price", price), 
                BigDecimal.ZERO
        );
        BigDecimal oldPrice = this.price;
        if (Nulls.equals(oldPrice, price)) {
            return;
        }
        this.price = price;
        if (this.propertyChangedListener != null) {
            this.propertyChangedListener.propertyChanged(
                    new PropertyChanagedEvent(this, "price", oldPrice, price)
            );
        }
    }
    
    public void addPropertyChangedListener(PropertyChangedListener listener) {
        /*
         * "PropertyChangedListener.combine" is implemented by the 
         * maven plugin of babyfish during compilation
         */
        this.propertyChangedListener = PropertyChangedListener.combine(
                this.propertyChangedListener, 
                listener
        );
    }
    
    public void removePropertyChangedListener(PropertyChangedListener listener) {
        /*
         * "PropertyChangedListener.remove" is implemented by the 
         * maven plugin of babyfish during compilation
         */
        this.propertyChangedListener = PropertyChangedListener.remove(
                this.propertyChangedListener, 
                listener
        );
    }
}
