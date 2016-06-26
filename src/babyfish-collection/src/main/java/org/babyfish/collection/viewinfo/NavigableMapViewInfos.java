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
import org.babyfish.lang.Equality;
import org.babyfish.lang.OverrideEquality;

/**
 * @author Tao Chen
 */
public class NavigableMapViewInfos extends SortedMapViewInfos {
    
    public static HeadMap headMap(Object toKey, boolean inclusive) {
        return new HeadMap(toKey, inclusive);
    }
    
    public static TailMap tailMap(Object fromKey, boolean inclusive) {
        return new TailMap(fromKey, inclusive);
    }
    
    public static SubMap subMap(Object fromKey, boolean fromInclusive, Object toKey, boolean toInclusive) {
        return new SubMap(fromKey, fromInclusive, toKey, toInclusive);
    }
    
    public static DescendingMap descendingMap() {
        return DescendingMap.INSTANCE;
    }
    
    public static NavigableKeySet navigableKeySet() {
        return NavigableKeySet.INSTANCE;
    }
    
    public static DescendingKeySet descendingKeySet() {
        return DescendingKeySet.INSTANCE;
    }
    
    public static FirstEntry firstEntry() {
        return FirstEntry.INSTANCE;
    }
    
    public static LastEntry lastEntry() {
        return LastEntry.INSTANCE;
    }
    
    public static FloorEntry floorEntry(Object key) {
        return new FloorEntry(key);
    }
    
    public static CeilingEntry ceilingEntry(Object key) {
        return new CeilingEntry(key);
    }
    
    public static LowerEntry lowerEntry(Object key) {
        return new LowerEntry(key);
    }
    
    public static HigherEntry higherEntry(Object key) {
        return new HigherEntry(key);
    }
    
    public static class HeadMap extends SortedMapViewInfos.HeadMap {
       
        private static final long serialVersionUID = 6076735429810170204L;
        
        private boolean inclusive;

        HeadMap(Object toKey, boolean toInclusive) {
            super(toKey);
            this.inclusive = toInclusive;
        }

        public boolean isInclusive() {
            return this.inclusive;
        }

        @Override
        public int hashCode() {
            return super.hashCode() * 31 + Boolean.hashCode(this.inclusive);
        }

        @OverrideEquality
        @Override
        public boolean equals(Object obj) {
            Equality<HeadMap> equality = Equality.of(HeadMap.class, this, obj);
            HeadMap other = equality.other();
            if (other == null) {
                return false;
            }
            return super.equals(obj) && this.inclusive == other.inclusive;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender
            .property("toKey", this.getToKey())
            .property("inclusive", this.inclusive);
        }
    }
    
    public static class TailMap extends SortedMapViewInfos.TailMap {
       
        private static final long serialVersionUID = -6590399712088432078L;
        
        private boolean inclusive;

        TailMap(Object fromKey, boolean fromInclusive) {
            super(fromKey);
            this.inclusive = fromInclusive;
        }

        public boolean isInclusive() {
            return this.inclusive;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = hash * 31 + Boolean.hashCode(this.inclusive);
            return hash;
        }

        @OverrideEquality
        @Override
        public boolean equals(Object obj) {
            Equality<TailMap> equality = Equality.of(TailMap.class, this, obj);
            TailMap other = equality.other();
            if (other == null) {
                return false;
            }
            return super.equals(obj) && 
                    this.inclusive == other.inclusive;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender
            .property("fromKey", this.getFromKey())
            .property("inclusive", this.inclusive);
        }
    }
    
    public static class SubMap extends SortedMapViewInfos.SubMap {

        private static final long serialVersionUID = 5846294853978343920L;

        private boolean fromInclusive;
        
        private boolean toInclusive;

        SubMap(Object fromKey, boolean fromInclusive, Object toKey, boolean toInclusive) {
            super(fromKey, toKey);
            this.fromInclusive = fromInclusive;
            this.toInclusive = toInclusive;
        }

        public boolean isFromInclusive() {
            return this.fromInclusive;
        }

        public boolean isToInclusive() {
            return this.toInclusive;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = hash * 31 + Boolean.hashCode(this.fromInclusive);
            hash = hash * 31 + Boolean.hashCode(this.toInclusive);
            return hash;
        }

        @OverrideEquality
        @Override
        public boolean equals(Object obj) {
            Equality<SubMap> equality = Equality.of(SubMap.class, this, obj);
            SubMap other = equality.other();
            if (other == null) {
                return false;
            }
            return super.equals(obj) && 
                    this.fromInclusive == other.fromInclusive &&
                    this.toInclusive == other.toInclusive;
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender
            .property("fromKey", this.getFromKey())
            .property("fromInclusive", this.fromInclusive)
            .property("toKey", this.getToKey())
            .property("toInclusive", this.toInclusive);
        }
    }
    
    public static class DescendingMap extends AbstractSingletonViewInfo {
        
        static final DescendingMap INSTANCE = getInstance(DescendingMap.class);
        
        private DescendingMap() {}
    }
    
    public static class NavigableKeySet extends KeySet {
        
        static final NavigableKeySet INSTANCE = getInstance(NavigableKeySet.class);
        
        private NavigableKeySet() {}
    }
    
    public static class DescendingKeySet extends AbstractSingletonViewInfo {
        
        static final DescendingKeySet INSTANCE = getInstance(DescendingKeySet.class);
        
        private DescendingKeySet() {}
    }
    
    public static class FirstEntry extends AbstractSingletonViewInfo {
        
        static final FirstEntry INSTANCE = getInstance(FirstEntry.class);
        
        private FirstEntry() {}
    }
    
    public static class LastEntry extends AbstractSingletonViewInfo {
        
        static final LastEntry INSTANCE = getInstance(LastEntry.class);
        
        private LastEntry() {}
    }
    
    public static class FloorEntry extends AbstractViewInfo {
        
        private static final long serialVersionUID = -741878897385999981L;
        
        private Object key;
        
        FloorEntry(Object key) {
            this.key = key;
        }
        
        public Object getKey() {
            return this.key;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(this.key);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            FloorEntry other = (FloorEntry)obj;
            return Objects.equals(this.key, other.key);
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("key", this.key);
        }
    }
    
    public static class CeilingEntry extends AbstractViewInfo {
       
        private static final long serialVersionUID = 4269809245443230582L;
        
        private Object key;
        
        CeilingEntry(Object key) {
            this.key = key;
        }
        
        public Object getKey() {
            return this.key;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(this.key);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            CeilingEntry other = (CeilingEntry)obj;
            return Objects.equals(this.key, other.key);
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("key", this.key);
        }
    }
    
    public static class LowerEntry extends AbstractViewInfo {
        
        private static final long serialVersionUID = 4337670736272859891L;
        
        private Object key;
        
        LowerEntry(Object key) {
            this.key = key;
        }
        
        public Object getKey() {
            return this.key;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(this.key);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            LowerEntry other = (LowerEntry)obj;
            return Objects.equals(this.key, other.key);
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("key", this.key);
        }
    }
    
    public static class HigherEntry extends AbstractViewInfo {
        
        private static final long serialVersionUID = -5219512100219858251L;
        
        private Object key;
        
        HigherEntry(Object key) {
            this.key = key;
        }
        
        public Object getKey() {
            return this.key;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(this.key);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            HigherEntry other = (HigherEntry)obj;
            return Objects.equals(this.key, other.key);
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("key", this.key);
        }
    }
    
    @Deprecated
    protected NavigableMapViewInfos() {}
}
