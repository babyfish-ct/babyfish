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
package org.babyfish.model.spi.reference.event.modification;

import org.babyfish.data.event.spi.AbstractModification;
import org.babyfish.data.spi.Appender;
import org.babyfish.model.spi.reference.event.ValueEvent.Modification;

/**
 * @author Tao Chen
 */
public class ReferenceModifications {
    
    public static <T> SetByValue<T> set(T value) {
        return new SetByValueImpl<>(value);
    }
    
    public interface SetByValue<T> extends Modification<T> {
        
        T getValue();
    }
    
    private static class SetByValueImpl<T> extends AbstractModification implements SetByValue<T> {
      
        private static final long serialVersionUID = 3935025434520955322L;
        
        private T value;

        public SetByValueImpl(T value) {
            this.value = value;
        }

        @Override
        public T getValue() {
            return this.value;
        }

        @Override
        protected void appendTo(Appender appender) {
            appender.property("value", this.value);
        }
    }
    
    @Deprecated
    protected ReferenceModifications() {
        throw new UnsupportedOperationException();
    }
}
