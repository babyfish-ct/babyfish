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
package org.babyfish.persistence.criteria.expression;

/**
 * @author Tao Chen
 */
public class PriorityConstants {
    
    public static final int LOWEST = 0;
    
    public static final int OR = 100;
    
    public static final int AND = 200;
    
    public static final int COMPARASION = 300;
    
    public static final int SUM_DIFF = 400;
    
    public static final int PROD_QUOT_MOD = 500;
    
    public static final int UNARY = 600;
    
    public static final int HIGHEST = Integer.MAX_VALUE;

    protected PriorityConstants() {
        throw new UnsupportedOperationException();
    }
    
}
