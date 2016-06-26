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
package org.babyfishdemo.spring.dal.impl;

import java.io.Serializable;
import java.util.List;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.persistence.XTypedQuery;
import org.babyfishdemo.spring.model.Page;

/**
 * @author Tao Chen
 */
public class PageBuilder<T> {
    
    private XTypedQuery<T> typedQuery;
    
    private int pageIndex;
    
    private int pageSize;
    
    public PageBuilder(XTypedQuery<T> typedQuery, int pageIndex, int pageSize) {
        this.typedQuery = Arguments.mustNotBeNull("typedQuery", typedQuery);
        this.pageIndex = pageIndex;
        this.pageSize = Arguments.mustBeGreaterThanValue("pageSize", pageSize, 0);
    }
    
    public Page<T> build() {
        List<T> entities;
        int rowCount;
        int pageCount;
        int actualPageIndex;
        if (pageIndex == 0 && pageSize == Integer.MAX_VALUE) {
            entities = this.typedQuery.getResultList();
            rowCount = entities.size();
            pageCount = entities.isEmpty() ? 0 : 1;
            actualPageIndex = 0;
        } else {
            rowCount = (int)this.typedQuery.getUnlimitedCount();
            pageCount = (rowCount + this.pageSize - 1) / this.pageSize;
            actualPageIndex = this.pageIndex;
            if (actualPageIndex >= pageCount) {
                actualPageIndex = pageCount - 1;
            }
            if (actualPageIndex < 0) { //pageCount == 0
                actualPageIndex = 0;
            }
            entities =
                    this
                    .typedQuery
                    .setFirstResult(actualPageIndex * this.pageSize)
                    .setMaxResults(this.pageSize)
                    .getResultList();
        }
        return new PageImpl<>(
                entities,
                this.pageIndex,
                actualPageIndex,
                this.pageSize,
                rowCount,
                pageCount);
    }
    
    private static class PageImpl<T> implements Page<T>, Serializable {
        
        private static final long serialVersionUID = 8746755395411967799L;

        private List<T> entities;
        
        private int expectedPageIndex;
        
        private int actualPageIndex;
        
        private int pageSize;
        
        private int totalRowCount;
        
        private int totalPageCount;

        PageImpl(
                List<T> entities, 
                int expectedPageIndex,
                int actualPageIndex, 
                int pageSize, 
                int totalRowCount,
                int totalPageCount) {
            this.entities = MACollections.unmodifiable(entities);
            this.expectedPageIndex = expectedPageIndex;
            this.actualPageIndex = actualPageIndex;
            this.pageSize = pageSize;
            this.totalRowCount = totalRowCount;
            this.totalPageCount = totalPageCount;
        }

        @Override
        public List<T> getEntities() {
            return this.entities;
        }

        @Override
        public int getExpectedPageIndex() {
            return this.expectedPageIndex;
        }

        @Override
        public int getActualPageIndex() {
            return this.actualPageIndex;
        }
        
        @Override
        public int getPageSize() {
            return this.pageSize;
        }

        @Override
        public int getTotalRowCount() {
            return this.totalRowCount;
        }

        @Override
        public int getTotalPageCount() {
            return this.totalPageCount;
        }
    }
}
