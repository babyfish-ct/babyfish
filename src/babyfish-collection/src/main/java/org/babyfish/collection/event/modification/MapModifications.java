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

import java.util.Map;

import org.babyfish.collection.event.MapElementEvent.MapModification;
import org.babyfish.data.event.spi.AbstractModification;
import org.babyfish.data.spi.Appender;

/**
 * @author Tao Chen
 */
public class MapModifications {
    
    public static <K, V> PutByKeyAndValue<K, V> put(K key, V value) {
        return new PutByKeyAndValue<K, V>(key, value);
    }
    
    public static <K, V> PutAllByMap<K, V> putAll(Map<? extends K, ? extends V> m) {
        return new PutAllByMap<>(m);
    }
    
    public static <K, V> Clear<K, V> clear() {
        return new Clear<>();
    }
    
    public static <K, V> RemoveByKey<K, V> remove(K key) {
        return new RemoveByKey<>(key);
    }
    
    public static <K, V> SuspendViaFrozenContext<K, V> suspendViaFrozenContext(K key) {
        return new SuspendViaFrozenContext<>(key);
    }

    public static <K, V> ResumeViaFrozenContext<K, V> resumeViaFrozenContext() {
        return new ResumeViaFrozenContext<>();
    }
    
    public static <K, V> SuspendViaInversedFrozenContext<K, V> suspendViaInversedFrozenContext(V value) {
        return new SuspendViaInversedFrozenContext<>(value);
    }
    
    public static <K, V> ResumeViaInversedFrozenContext<K, V> resumeViaInversedFrozenContext() {
        return new ResumeViaInversedFrozenContext<>();
    }

    public static class PutByKeyAndValue<K, V> extends AbstractModification implements MapModification<K, V> {
        
        private static final long serialVersionUID = 4750481884525524793L;

        private K key;
        
        private V value;

        PutByKeyAndValue(K key, V value) {
            this.key = key;
            this.value = value;
        }
        
        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("key", this.key).property("value", this.value);
        }
    }
    
    public static class PutAllByMap<K, V> extends AbstractModification implements MapModification<K, V> {
      
        private static final long serialVersionUID = 2797811961032887761L;
        
        private Map<? extends K, ? extends V> map;

        public PutAllByMap(Map<? extends K, ? extends V> map) {
            this.map = map;
        }

        public Map<? extends K, ? extends V> getMap() {
            return this.map;
        }

        @Override
        protected void appendTo(Appender appender) {
            appender.property("map", this.map);
        }
    }
    
    public static class Clear<K, V> extends AbstractModification implements MapModification<K, V> {
        
        private static final long serialVersionUID = 1802295263701434172L;

        Clear() {}
    }
    
    public static class RemoveByKey<K, V> extends AbstractModification implements MapModification<K, V> {
      
        private static final long serialVersionUID = 4473101289727926462L;
        
        private K key;
        
        RemoveByKey(K key) {
            this.key = key;
        }
        
        public K getKey() {
            return this.key;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("key", this.key);
        }
    }
    
    public static class SuspendViaFrozenContext<K, V> extends AbstractModification implements MapModification<K, V> {
     
        private static final long serialVersionUID = -6472881120401949625L;
        
        private K key;
        
        SuspendViaFrozenContext(K key) {
            this.key = key;
        }
        
        public K getKey() {
            return this.key;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("key", this.key);
        }
    }
    
    public static class ResumeViaFrozenContext<K, V> extends AbstractModification implements MapModification<K, V> {
    
        private static final long serialVersionUID = 3806094824592186081L;

        ResumeViaFrozenContext() {}
    }
    
    public static class SuspendViaInversedFrozenContext<K, V> 
    extends AbstractModification 
    implements MapModification<K, V> {
        
        private static final long serialVersionUID = -125437503076626422L;
        
        private V value;
        
        SuspendViaInversedFrozenContext(V value) {
            this.value = value;
        }

        @Override
        protected void appendTo(Appender appender) {
            appender.property("value", this.value);
        }
    }
    
    public static class ResumeViaInversedFrozenContext<K, V>
    extends AbstractModification
    implements MapModification<K, V> {
        
        private static final long serialVersionUID = -1505859621146031685L;

        ResumeViaInversedFrozenContext() {}
    }
    
    @Deprecated
    protected MapModifications() {
        throw new UnsupportedOperationException();
    }
}
