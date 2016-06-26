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
package org.babyfishdemo.om4jpa.entities.disability;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.model.jpa.JPAModel;

/**
 * @author Tao Chen
 */
@JPAModel
@Entity
@Table(name = "BOOK")
@SequenceGenerator(
        name = "bookSequence",
        sequenceName = "BOOK_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Book {

    @Id
    @Column(name = "BOOK_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookSequence")
    private Long id;
    
    @Column(name = "ISBN", length =20, nullable = false, unique = true)
    private String isbn;
    
    @Column(name = "NAME", length = 100, nullable = false)
    private String name;
    
    @Column(name = "PRICE", nullable = false)
    private BigDecimal price;
    
    @Column(name = "PUBLISHER", length = 100)
    private String publisher;
    
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "BOOK_AUTHOR_MAPPING",
            joinColumns = @JoinColumn(name = "BOOK_ID"),
            inverseJoinColumns = @JoinColumn(name = "AUTHOR_ID")
    )
    private Set<Author> authors;
    
    public Book() {
        
    }
    
    public Book(String isbn, String name, BigDecimal price, String pulisher, Collection<Author> authors) {
        this.isbn = isbn;
        this.name = name;
        this.price = price;
        this.publisher = pulisher;
        for (Author author : authors) {
            this.authors.add(author);
        }
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }
}
