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

import org.babyfish.data.spi.AbstractViewInfo;
import org.babyfish.data.spi.Appender;
import org.babyfish.lang.Equality;
import org.babyfish.lang.OverrideEquality;

/**
 * @author Tao Chen
 */
public class SortedMapViewInfos extends MapViewInfos {
    
    public static HeadMap headMap(Object toKey) {
        return new HeadMap(toKey);
    }
    
    public static TailMap tailMap(Object fromKey) {
        return new TailMap(fromKey);
    }
    
    public static SubMap headMap(Object fromKey, Object toKey) {
        return new SubMap(fromKey, toKey);
    }

    public static class HeadMap extends AbstractViewInfo {
    
        private static final long serialVersionUID = 2839189186649479726L;
        
        private Object toKey;

        HeadMap(Object toKey) {
            this.toKey = toKey;
        }

        public Object getToKey() {
            return this.toKey;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(toKey);
        }

        @OverrideEquality
        @Override
        public boolean equals(Object obj) {
            Equality<HeadMap> equality = Equality.of(HeadMap.class, this, obj);
            HeadMap other = equality.other();
            if (equality.other() == null) {
                return equality.returnValue();
            }
            return Objects.equals(this.toKey, other.toKey);
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("toKey", this.toKey);
        }
    }
    
    public static class TailMap extends AbstractViewInfo {
       
        private static final long serialVersionUID = 3742562421590039319L;
        
        private Object fromKey;
        
        TailMap(Object fromKey) {
            this.fromKey = fromKey;
        }

        public Object getFromKey() {
            return this.fromKey;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.fromKey);
        }

        @OverrideEquality
        @Override
        public boolean equals(Object obj) {
            Equality<TailMap> equality = Equality.of(TailMap.class, this, obj);
            TailMap other = equality.other();
            if (equality.other() == null) {
                return equality.returnValue();
            }
            return Objects.equals(this.fromKey, other.fromKey);
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender.property("fromKey", this.fromKey);
        }
    }
    
    public static class SubMap extends AbstractViewInfo {
       
        private static final long serialVersionUID = -2310496506356063400L;

        private Object fromKey;
        
        private Object toKey;

        SubMap(Object fromKey, Object toKey) {
            this.fromKey = fromKey;
            this.toKey = toKey;
        }

        public Object getFromKey() {
            return this.fromKey;
        }

        public Object getToKey() {
            return this.toKey;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.fromKey) * 31 + Objects.hashCode(toKey);
        }

        @OverrideEquality
        @Override
        public boolean equals(Object obj) {
            Equality<SubMap> equality = Equality.of(SubMap.class, this, obj);
            SubMap other = equality.other();
            if (equality.other() == null) {
                return equality.returnValue();
            }
            return Objects.equals(this.fromKey, other.fromKey) &&
                    Objects.equals(this.toKey, other.toKey);
        }
        
        @Override
        protected void appendTo(Appender appender) {
            appender
            .property("fromKey", this.fromKey)
            .property("toKey", this.toKey);
        }
    }
    
    @Deprecated
    protected SortedMapViewInfos() {}
}
