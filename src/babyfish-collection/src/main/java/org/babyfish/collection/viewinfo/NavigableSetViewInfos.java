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

import org.babyfish.data.spi.AbstractSingletonViewInfo;
import org.babyfish.data.spi.Appender;
import org.babyfish.lang.Equality;
import org.babyfish.lang.OverrideEquality;

/**
 * @author Tao Chen
 */
public class NavigableSetViewInfos extends SortedSetViewInfos {
    
    public static HeadSet headSet(Object toElement, boolean inclusive) {
        return new HeadSet(toElement, inclusive);
    }
    
    public static TailSet tailSet(Object fromElement, boolean inclusive) {
        return new TailSet(fromElement, inclusive);
    }
    
    public static SubSet subSet(Object fromElement, boolean fromInclusive, Object toElement, boolean toInclusive) {
        return new SubSet(fromElement, fromInclusive, toElement, toInclusive);
    }
    
    public static DescendingSet descendingSet() {
        return DescendingSet.INSTANCE;
    }
    
    public static DescendingIterator descendingIterator() {
        return DescendingIterator.INSTANCE;
    }
    
    public static class HeadSet extends SortedSetViewInfos.HeadSet {
       
        private static final long serialVersionUID = 2909320453953283213L;
        
        private boolean inclusive;

        HeadSet(Object toElement, boolean toInclusive) {
            super(toElement);
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
            Equality<HeadSet> equality = Equality.of(HeadSet.class, this, obj);
            HeadSet other = equality.other();
            if (other == null) {
                return false;
            }
            return super.equals(obj) && this.inclusive == other.inclusive;
        }
        
        @Override
        public void appendTo(Appender appender) {
            appender
            .property("toElement", this.getToElement())
            .property("inclusive", this.inclusive);
        }
    }
    
    public static class TailSet extends SortedSetViewInfos.TailSet {
       
        private static final long serialVersionUID = 5390987428846105934L;
        
        private boolean inclusive;

        TailSet(Object fromElement, boolean fromInclusive) {
            super(fromElement);
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
            Equality<TailSet> equality = Equality.of(TailSet.class, this, obj);
            TailSet other = equality.other();
            if (other == null) {
                return false;
            }
            return super.equals(obj) && 
                    this.inclusive == other.inclusive;
        }
        
        @Override
        public void appendTo(Appender appender) {
            appender
            .property("fromElement", this.getFromElement())
            .property("inclusive", this.inclusive);
        }
    }
    
    public static class SubSet extends SortedSetViewInfos.SubSet {

        private static final long serialVersionUID = 2526039426790619559L;

        private boolean fromInclusive;
        
        private boolean toInclusive;

        SubSet(Object fromElement, boolean fromInclusive, Object toElement, boolean toInclusive) {
            super(fromElement, toElement);
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
            Equality<SubSet> equality = Equality.of(SubSet.class, this, obj);
            SubSet other = equality.other();
            if (other == null) {
                return false;
            }
            return super.equals(obj) && 
                    this.fromInclusive == other.fromInclusive &&
                    this.toInclusive == other.toInclusive;
        }

        @Override
        public void appendTo(Appender appender) {
            appender
            .property("fromElement", this.getFromElement())
            .property("fromInclusive", this.fromInclusive)
            .property("toElement", this.getToElement())
            .property("toInclusive", this.toInclusive);
        }
    }
    
    public static class DescendingSet extends AbstractSingletonViewInfo {
        
        static final DescendingSet INSTANCE = getInstance(DescendingSet.class);
        
        private DescendingSet() {}
    }
    
    public static class DescendingIterator extends AbstractSingletonViewInfo {
        
        static final DescendingIterator INSTANCE = getInstance(DescendingIterator.class);
        
        private DescendingIterator() {}
    }
    
    @Deprecated
    protected NavigableSetViewInfos() {}
}
