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

import java.util.EventObject;

/**
 * @author Tao Chen
 */
public class PropertyChanagedEvent extends EventObject {

    private static final long serialVersionUID = 8323732437521704562L;

    private String propertyName;
    
    private Object oldValue;
    
    private Object newValue;
    
    private transient String toString;

    public PropertyChanagedEvent(
            Object source, 
            String propertyName, 
            Object oldValue,
            Object newValue) {
        super(source);
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public Book getSource() {
        return (Book)super.getSource();
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOldValue() {
        return (T)this.oldValue;
    }

    @SuppressWarnings("unchecked")
    public <T> T getNewValue() {
        return (T)this.newValue;
    }

    @Override
    public String toString() {
        String str = this.toString;
        if (str == null) {
            this.toString = str = 
                    "{ propertyName: "
                    + text(this.propertyName)
                    + ", oldValue: "
                    + text(this.oldValue)
                    + ", newValue: "
                    + text(this.newValue)
                    + " }";
        }
        return str;
    }
    
    private static String text(Object value) {
        if (value == null) {
            return "null";
        }
        return '"' + value.toString() + '"';
    }
}
