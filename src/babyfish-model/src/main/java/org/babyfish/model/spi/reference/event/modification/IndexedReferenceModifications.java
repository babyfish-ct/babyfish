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
public class IndexedReferenceModifications extends ReferenceModifications {
    
    public static <T> SetByIndex<T> setIndex(int index) {
        return new SetByIndexImpl<>(index);
    }
    
    public static <T> SetByIndexAndValue<T> set(int index, T value) {
        return new SetByIndexAndValue<>(index, value);
    }
    
    public interface SetByIndex<T> extends Modification<T> {
        int getIndex();
    }

    private static class SetByIndexImpl<T> extends AbstractModification implements SetByIndex<T> {
        
        private static final long serialVersionUID = 6953654963467353162L;
        
        private int index;
        
        public SetByIndexImpl(int index) {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return this.index;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("index", index);
        }
    }
    
    public static class SetByIndexAndValue<T> 
    extends AbstractModification 
    implements SetByIndex<T>, SetByValue<T> {
       
        private static final long serialVersionUID = -7475789612868789010L;

        private int index;
        
        private T value;

        public SetByIndexAndValue(int index, T value) {
            this.index = index;
            this.value = value;
        }

        public int getIndex() {
            return this.index;
        }

        public T getValue() {
            return this.value;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("index", index).property("value", value);
        }
    }
    
    @Deprecated
    protected IndexedReferenceModifications() {}
}
