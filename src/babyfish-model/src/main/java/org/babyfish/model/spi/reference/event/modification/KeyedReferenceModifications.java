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
import org.babyfish.model.spi.reference.event.KeyedValueEvent.KeyedModification;

/**
 * @author Tao Chen
 */
public class KeyedReferenceModifications extends ReferenceModifications {
    
    public static <T> SetByValue<?, T> set(T value) {
        return new SetByValueImpl<>(value);
    }

    public static <K, T> SetByKey<K, T> setKey(K key) {
        return new SetByKeyImpl<>(key);
    }
    
    public static <K, T> SetByKeyAndValue<K, T> set(K key, T value) {
        return new SetByKeyAndValue<>(key, value);
    }
    
    public interface SetByValue<K, T> 
    extends KeyedModification<K, T>, ReferenceModifications.SetByValue<T> {}
    
    private static class SetByValueImpl<K, T> extends AbstractModification implements SetByValue<K, T> {
    
        private static final long serialVersionUID = -1859660011394458318L;
        
        private T value;
        
        SetByValueImpl(T value) {
            this.value = value;
        }
        
        public T getValue() {
            return this.value;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("value", this.value);
        }
    }
    
    public interface SetByKey<K, T> extends KeyedModification<K, T> {
        
        K getKey();
    }

    private static class SetByKeyImpl<K, T> extends AbstractModification implements SetByKey<K, T> {
        
        private static final long serialVersionUID = 8281458259354986096L;
        
        private K key;
        
        SetByKeyImpl(K key) {
            this.key = key;
        }

        @Override
        public K getKey() {
            return this.key;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("key", key);
        }
    }
    
    public static class SetByKeyAndValue<K, T> 
    extends AbstractModification
    implements SetByKey<K, T>, SetByValue<K, T> {
       
        private static final long serialVersionUID = -7475789612868789010L;

        private K key;
        
        private T value;

        SetByKeyAndValue(K key, T value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return this.key;
        }

        public T getValue() {
            return this.value;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("key", key).property("value", value);
        }
    }
    
    @Deprecated
    protected KeyedReferenceModifications() {}
}
