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

import org.babyfish.lang.Nulls;

/**
 * @author Tao Chen
 */
public enum LikeMode {

    EXACT {
        @Override
        public String pattern(String pattern) {
            return pattern;
        }
    },
    START {
        @Override
        public String pattern(String pattern) {
            if (Nulls.isNullOrEmpty(pattern) || pattern.charAt(0) != '%') {
                return '%' + Nulls.toString(pattern, true);
            }
            return pattern;
        }
    },
    END {
        @Override
        public String pattern(String pattern) {
            if (Nulls.isNullOrEmpty(pattern) || pattern.charAt(pattern.length() - 1) != '%') {
                return Nulls.toString(pattern, true) + '%';
            }
            return pattern;
        }
    },
    ANYWHERE {
        @Override
        public String pattern(String pattern) {
            if (Nulls.isNullOrEmpty(pattern) || pattern.charAt(0) != '%') {
                pattern = '%' + Nulls.toString(pattern, true);
            }
            if (pattern.charAt(pattern.length() - 1) != '%') {
                pattern = pattern + '%';
            }
            return pattern;
        }
    };
    
    public abstract String pattern(String pattern);
}
