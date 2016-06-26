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
package org.babyfish.persistence;

/**
 * @author Tao Chen
 */
public class Constants {

    public static final String NOT_SHARED_JOIN_ALIAS_PREFIX = "babyfish_not_shared_alias_";
    
    public static final String LITERAL_PARAMTER_NAME_PREFIX = "babyfish_literal_";
    
    public static final String QUERY_PATH_JOIN_ALIAS_PREFIX = "babyfish_join_node_alias_";
    
    @Deprecated
    protected Constants() {
        throw new UnsupportedOperationException();
    }
}
