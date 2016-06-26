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
package org.babyfishdemo.pagingquery.base;

import java.util.List;

/**
 * @author Tao Chen
 */
public class LimitedResult<T> {

    private long unlimitedRowCount;
    
    private List<T> limitedRows;

    public LimitedResult(long unlimitedRowCount, List<T> limitedRows) {
        this.unlimitedRowCount = unlimitedRowCount;
        this.limitedRows = limitedRows;
    }

    public long getUnlimitedRowCount() {
        return this.unlimitedRowCount;
    }

    public List<T> getLimitedRows() {
        return this.limitedRows;
    }
}
