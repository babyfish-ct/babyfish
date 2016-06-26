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
package org.babyfish.model.jpa.path;

import java.lang.reflect.Array;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;

/**
 * @author Tao Chen
 */
public class TypedQueryPaths {

    protected TypedQueryPaths() {
        throw new UnsupportedOperationException();
    }
    
    @SuppressWarnings("unchecked")
    public static <R, P extends TypedQueryPath<R>> P[] combine(P[] pathArr1, P ... pathArr2) {
        if (Nulls.isNullOrEmpty(pathArr1)) {
            return pathArr2;
        }
        if (Nulls.isNullOrEmpty(pathArr2)) {
            return pathArr1;
        }
        P[] arr = (P[])Array.newInstance(
                pathArr1.getClass().getComponentType(), 
                pathArr1.length + pathArr2.length);
        System.arraycopy(pathArr1, 0, arr, 0, pathArr1.length);
        System.arraycopy(pathArr2, 0, arr, pathArr1.length, pathArr2.length);
        return arr;
    }
    
    @SuppressWarnings("unchecked")
    public static <R, P extends TypedQueryPath<R>> P[] combine(P path, P ... pathArr) {
        if (path == null) {
            return pathArr;
        }
        Arguments.mustNotBeNull("pathArr", pathArr);
        P[] arr = (P[])Array.newInstance(pathArr.getClass().getComponentType(), pathArr.length + 1);
        arr[0] = path;
        System.arraycopy(pathArr, 0, arr, 1, pathArr.length);
        return arr;
    }
    
    @SuppressWarnings("unchecked")
    public static <R, P extends TypedQueryPath<R>> P[] fetchPaths(P ... pathArr) {
        if (Nulls.isNullOrEmpty(pathArr)) {
            return pathArr;
        }
        int len = 0;
        for (P queryPath : pathArr) {
            if (queryPath instanceof FetchPath) {
                len++;
            }
        }
        if (len == pathArr.length) {
            return pathArr;
        }
        P[] arr = (P[])Array.newInstance(pathArr.getClass().getComponentType(), len);
        len = 0;
        for (P queryPath : pathArr) {
            if (queryPath instanceof FetchPath) {
                arr[len++] = queryPath;
            }
        }
        return arr;
    }
    
    @SuppressWarnings("unchecked")
    public static <R, P extends TypedQueryPath<R>> P[] simpleOrderPaths(P ... pathArr) {
        if (Nulls.isNullOrEmpty(pathArr)) {
            return pathArr;
        }
        int len = 0;
        for (P queryPath : pathArr) {
            if (queryPath instanceof SimpleOrderPath) {
                len++;
            }
        }
        if (len == pathArr.length) {
            return pathArr;
        }
        P[] arr = (P[])Array.newInstance(pathArr.getClass().getComponentType(), len);
        len = 0;
        for (P queryPath : pathArr) {
            if (queryPath instanceof SimpleOrderPath) {
                arr[len++] = queryPath;
            }
        }
        return arr;
    }
}
