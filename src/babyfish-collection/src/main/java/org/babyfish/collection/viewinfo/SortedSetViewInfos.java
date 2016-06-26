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
public class SortedSetViewInfos extends CollectionViewInfos {
    
    public static HeadSet headSet(Object toElement) {
        return new HeadSet(toElement);
    }
    
    public static TailSet tailSet(Object fromElement) {
        return new TailSet(fromElement);
    }
    
    public static SubSet headSet(Object fromElement, Object toElement) {
        return new SubSet(fromElement, toElement);
    }

    public static class HeadSet extends AbstractViewInfo {
    
        private static final long serialVersionUID = 797192640309750632L;
        
        private Object toElement;

        HeadSet(Object toElement) {
            this.toElement = toElement;
        }

        public Object getToElement() {
            return this.toElement;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(toElement);
        }

        @OverrideEquality
        @Override
        public boolean equals(Object obj) {
            Equality<HeadSet> equality = Equality.of(HeadSet.class, this, obj);
            HeadSet other = equality.other();
            if (equality.other() == null) {
                return equality.returnValue();
            }
            return Objects.equals(this.toElement, other.toElement);
        }
        
        @Override
        public void appendTo(Appender appender) {
            appender.property("toElement", this.toElement);
        }
    }
    
    public static class TailSet extends AbstractViewInfo {
       
        private static final long serialVersionUID = 3500904622987303865L;
        
        private Object fromElement;
        
        TailSet(Object fromElement) {
            this.fromElement = fromElement;
        }

        public Object getFromElement() {
            return this.fromElement;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.fromElement);
        }

        @OverrideEquality
        @Override
        public boolean equals(Object obj) {
            Equality<TailSet> equality = Equality.of(TailSet.class, this, obj);
            TailSet other = equality.other();
            if (equality.other() == null) {
                return equality.returnValue();
            }
            return Objects.equals(this.fromElement, other.fromElement);
        }
        
        @Override
        public void appendTo(Appender appender) {
            appender.property("fromElement", this.fromElement);
        }
    }
    
    public static class SubSet extends AbstractViewInfo {
       
        private static final long serialVersionUID = 8289562876590317715L;

        private Object fromElement;
        
        private Object toElement;

        SubSet(Object fromElement, Object toElement) {
            this.fromElement = fromElement;
            this.toElement = toElement;
        }

        public Object getFromElement() {
            return this.fromElement;
        }

        public Object getToElement() {
            return this.toElement;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.fromElement) * 31 + Objects.hashCode(toElement);
        }

        @OverrideEquality
        @Override
        public boolean equals(Object obj) {
            Equality<SubSet> equality = Equality.of(SubSet.class, this, obj);
            SubSet other = equality.other();
            if (equality.other() == null) {
                return equality.returnValue();
            }
            return Objects.equals(this.fromElement, other.fromElement) &&
                    Objects.equals(this.toElement, other.toElement);
        }
        
        @Override
        public void appendTo(Appender appender) {
            appender
            .property("fromElement", this.fromElement)
            .property("toElement", this.toElement);
        }
    }
    
    @Deprecated
    protected SortedSetViewInfos() {}
}
