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

import org.babyfish.data.spi.AbstractViewInfo;
import org.babyfish.data.spi.Appender;

public class ListViewInfos extends CollectionViewInfos {

    public static ListIterator listIterator(int index) {
        return new ListIterator(index);
    }
    
    public static SubList subList(int fromIndex, int toIndex) {
        return new SubList(fromIndex, toIndex);
    }
    
    public static class ListIterator extends AbstractViewInfo {

        private static final long serialVersionUID = -2591126758944878323L;
        
        private int index;

        ListIterator(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        public int hashCode() {
            return this.index;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            ListIterator other = (ListIterator)obj;
            return this.index == other.index;
        }

        @Override
        protected void appendTo(Appender appender) {
            appender.property("index", this.index);
        }
    }
    
    public static class SubList extends AbstractViewInfo {

        private static final long serialVersionUID = -859883121975827648L;

        private int fromIndex;
        
        private int toIndex;

        SubList(int fromIndex, int toIndex) {
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        public int getFromIndex() {
            return this.fromIndex;
        }

        public int getToIndex() {
            return this.toIndex;
        }
        
        @Override
        public int hashCode() {
            return this.fromIndex * 31 + this.toIndex;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            SubList other = (SubList)obj;
            return this.fromIndex == other.fromIndex &&
                    this.toIndex == other.toIndex;
        }

        @Override
        protected void appendTo(Appender appender) {
            appender
            .property("fromIndex", this.fromIndex)
            .property("toIndex", this.toIndex);
        }
    }
    
    @Deprecated
    protected ListViewInfos() {}
}
