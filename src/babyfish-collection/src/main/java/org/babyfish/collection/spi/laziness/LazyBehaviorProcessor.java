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
package org.babyfish.collection.spi.laziness;

import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public abstract class LazyBehaviorProcessor {
    
    private static final LazyBehaviorProcessor UNLIMITED = new Unlimited();
    
    private static final LazyBehaviorProcessor DISABLED = new Disabled();
    
    private LazyBehaviorProcessor() {
        
    }
    
    public abstract boolean preVisionallyRead(int rowCount);
    
    public abstract boolean preVisionallyReadSize();
    
    public abstract void visionallyRead(int rowCount);
    
    public abstract void visionallyReadSize();
    
    public static LazyBehaviorProcessor of(int rowLimit, int countLimit) {
        Arguments.mustBeGreaterThanOrEqualToValue("rowLimit", rowLimit, 0);
        Arguments.mustBeGreaterThanOrEqualToValue("countLimit", rowLimit, 0);
        Arguments.mustBeLessThanOrEqualToOther("countLimit", countLimit, "rowLimit", rowLimit);
        if (rowLimit == 0 && countLimit == 0) {
            return DISABLED;
        }
        return new Normal(rowLimit, countLimit);
    }
    
    public static LazyBehaviorProcessor unlimited() {
        return UNLIMITED;
    }
    
    private static class Normal extends LazyBehaviorProcessor {

        private float limit;
        
        private float countScale;
        
        private float current;
        
        Normal(int rowLimit, int countLimit) {
            this.limit = rowLimit;
            if (countLimit != 0) {
                this.countScale = (float)rowLimit / countLimit; 
            }
        }
        
        public boolean preVisionallyRead(int rowCount) {
            if (this.limit == 0) {
                return false;
            }
            return this.current + rowCount <= this.limit;
        }
        
        public boolean preVisionallyReadSize() {
            if (this.limit == 0 || this.countScale == 0) {
                return false;
            }
            return this.current + this.countScale <= this.limit;
        }
        
        public void visionallyRead(int rowCount) {
            if (this.limit != 0) {
                if (this.current + rowCount <= this.limit) {
                    this.current += rowCount;
                }
            }
        }
        
        public void visionallyReadSize() {
            if (this.limit == 0 || this.countScale == 0) {
                if (this.current + this.countScale <= this.limit) {
                    this.current += this.countScale;
                }
            }
        }
    }
    
    private static class Unlimited extends LazyBehaviorProcessor {
    
        @Override
        public boolean preVisionallyRead(int rowCount) {
            return true;
        }
    
        @Override
        public boolean preVisionallyReadSize() {
            return true;
        }
    
        @Override
        public void visionallyRead(int rowCount) {
            
        }
    
        @Override
        public void visionallyReadSize() {
            
        }
    }

    private static class Disabled extends LazyBehaviorProcessor {

        @Override
        public boolean preVisionallyRead(int rowCount) {
            return false;
        }

        @Override
        public boolean preVisionallyReadSize() {
            return false;
        }

        @Override
        public void visionallyRead(int rowCount) {
            
        }

        @Override
        public void visionallyReadSize() {
            
        }
        
    }
}

