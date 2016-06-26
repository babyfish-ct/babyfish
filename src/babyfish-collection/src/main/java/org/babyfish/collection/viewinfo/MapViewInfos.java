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
package org.babyfish.collection.viewinfo;

import java.util.Objects;

import org.babyfish.data.spi.AbstractSingletonViewInfo;
import org.babyfish.data.spi.AbstractViewInfo;
import org.babyfish.data.spi.Appender;

/**
 * @author Tao Chen
 */
public class MapViewInfos {
    
    public static EntrySet entrySet() {
        return EntrySet.INSTANCE;
    }
    
    public static KeySet keySet() {
        return KeySet.INSTANCE;
    }
    
    public static Values values() {
        return Values.INSTANCE;
    }
    
    public static Entry entry() {
        return Entry.INSTANCE;
    }
    
    public static EntryOfKey entryOfKey(Object key) {
        return new EntryOfKey(key);
    }
    
    public static EntryOfValue entryOfValue(Object value) {
        return new EntryOfValue(value);
    }
    
    public static class EntrySet extends AbstractSingletonViewInfo {
        
        static final EntrySet INSTANCE = getInstance(EntrySet.class);
        
        private EntrySet() {}
    }
    
    public static class KeySet extends AbstractSingletonViewInfo {
        
        static final KeySet INSTANCE = getInstance(KeySet.class);
        
        KeySet() {}
    }
    
    public static class Values extends AbstractSingletonViewInfo {
        
        static final Values INSTANCE = getInstance(Values.class);
        
        private Values() {}
    }
    
    public static class Entry extends AbstractSingletonViewInfo {
        
        static final Entry INSTANCE = getInstance(Entry.class);
        
        private Entry() {}
    }
    
    public static class EntryOfKey extends AbstractViewInfo {
      
        private static final long serialVersionUID = 8148557817488053611L;
        
        private Object key;

        EntryOfKey(Object key) {
            this.key = key;
        }

        public Object getKey() {
            return this.key;
        }
        
        @Override
        public int hashCode() {
            return Objects.hashCode(this.key);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return false;
            }
            EntryOfKey other = (EntryOfKey)obj;
            return Objects.equals(this.key, other.key);
        }

        @Override
        protected void appendTo(Appender appender) {
            appender.property("key", this.key);
        }
    }
    
    public static class EntryOfValue extends AbstractViewInfo {
        
        private static final long serialVersionUID = 8148557817488053611L;
        
        private Object value;

        EntryOfValue(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return this.value;
        }
        
        @Override
        public int hashCode() {
            return Objects.hashCode(this.value);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return false;
            }
            EntryOfValue other = (EntryOfValue)obj;
            return Objects.equals(this.value, other.value);
        }

        @Override
        protected void appendTo(Appender appender) {
            appender.property("value", this.value);
        }
    }
    
    @Deprecated
    protected MapViewInfos() {
        throw new UnsupportedOperationException();
    }
}
