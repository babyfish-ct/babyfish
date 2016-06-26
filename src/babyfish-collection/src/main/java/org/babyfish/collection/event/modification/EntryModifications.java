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
package org.babyfish.collection.event.modification;

import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.data.event.spi.AbstractModification;
import org.babyfish.data.spi.Appender;

/**
 * @author Tao Chen
 */
public class EntryModifications {
   
    public static <V> SetByValue<V> set(V value) {
        return new SetByValue<>(value);
    }
    
    public static class SetByValue<V> extends AbstractModification implements Modification<V> {
        
        private static final long serialVersionUID = -6765578854290007510L;
        
        private V value;
        
        SetByValue(V value) {
            this.value = value;
        }
        
        public V getValue() {
            return this.value;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("value", this.value);
        }
    }
    
    @Deprecated
    protected EntryModifications() {
        throw new UnsupportedOperationException();
    }
}
