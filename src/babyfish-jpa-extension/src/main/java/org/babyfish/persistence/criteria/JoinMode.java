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
package org.babyfish.persistence.criteria;

/**
 * @author Tao Chen
 */
public enum JoinMode {

    OPTIONALLY_MERGE_EXISTS {
        @Override
        public boolean isNew() {
            return false;
        }

        @Override
        public boolean isRequired() {
            return false;
        }
    },
    
    OPTIONALLY_CREATE_NEW {
        @Override
        public boolean isNew() {
            return true;
        }

        @Override
        public boolean isRequired() {
            return false;
        }
    },
    
    REQUIRED_TO_MERGE_EXISTS {
        @Override
        public boolean isNew() {
            return false;
        }

        @Override
        public boolean isRequired() {
            return true;
        }
    },
    
    REQUIRED_TO_CREATE_NEW {
        @Override
        public boolean isNew() {
            return true;
        }

        @Override
        public boolean isRequired() {
            return true;
        }
    },
    
    ;
    
    public abstract boolean isNew();
    
    public abstract boolean isRequired();
}
