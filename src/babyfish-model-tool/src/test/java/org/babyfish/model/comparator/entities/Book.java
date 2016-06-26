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
package org.babyfish.model.comparator.entities;

import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.model.Association;
import org.babyfish.model.ComparatorProperty;
import org.babyfish.model.ComparatorRule;
import org.babyfish.model.Model;
import org.babyfish.model.Scalar;

@Model
public class Book {
    
    @Scalar
    private long id;
    
    @Scalar
    private String code;
    
    @Scalar
    private String name;
    
    @Scalar
    private String isbn;
    
    @Association(opposite = "analogousBooks")
    @ComparatorRule(properties = {
            @ComparatorProperty(name = "code"),
            @ComparatorProperty(name = "name"),
            @ComparatorProperty(name = "isbn")
    })
    private XOrderedSet<Book> analogousBooks;
    
    @Association(opposite = "sameSeriesBooks")
    @ComparatorRule(properties = {
            @ComparatorProperty(name = "code"),
            @ComparatorProperty(name = "name"),
            @ComparatorProperty(name = "isbn")
    })
    private XNavigableSet<Book> sameSeriesBooks;

    public Book(long id, String code, String name, String isbn) {
        super();
        this.id = id;
        this.code = code;
        this.name = name;
        this.isbn = isbn;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public XOrderedSet<Book> getAnalogousBooks() {
        return analogousBooks;
    }

    public void setAnalogousBooks(XOrderedSet<Book> analogousBooks) {
        this.analogousBooks = analogousBooks;
    }

    public XNavigableSet<Book> getSameSeriesBooks() {
        return sameSeriesBooks;
    }

    public void setSameSeriesBooks(XNavigableSet<Book> sameSeriesBooks) {
        this.sameSeriesBooks = sameSeriesBooks;
    }
}
