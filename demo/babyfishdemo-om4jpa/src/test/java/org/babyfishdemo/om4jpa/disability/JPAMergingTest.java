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
package org.babyfishdemo.om4jpa.disability;

import java.math.BigDecimal;
import java.util.Arrays;

import javax.persistence.EntityManager;

import org.babyfish.collection.MACollections;
import org.babyfish.model.jpa.JPAEntities;
import org.babyfishdemo.om4jpa.entities.disability.Author;
import org.babyfishdemo.om4jpa.entities.disability.Book;
import org.babyfishdemo.om4jpa.entities.disability.Book_;
import org.babyfishdemo.om4jpa.entities.disability.Book__;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JPAMergingTest {
    
    private static final String CONNECTION_STRING = "jdbc:hsqldb:mem:org.babyfishdemo.om4jpa.disability.merge_test";

    @Before
    public void initData() {
        Author philip = new Author("Philip A. Bernstein"); // id will be 1 later
        Author eric = new Author("Eric Newcomer"); // id will be 2 later
        Author anand = new Author("Anand Rajaraman"); // id will be 3 later
        Book book = new Book( // id will be 1 later
                "978-7-302-24041-9",
                "Principles of Transaction Processing, Second Edition",
                new BigDecimal(48),
                "Tsinghua University Press",
                MACollections.wrap(philip, eric, anand)
        );
        try (TransactionScope ts = new TransactionScope(CONNECTION_STRING)) {
            EntityManager em = TransactionScope.getEntityManager();
            em.persist(philip);
            em.persist(eric);
            em.persist(anand);
            em.persist(book);
            
            ts.compete();
        }
        
        Assert.assertEquals(1L, philip.getId().longValue());
        Assert.assertEquals(2L, eric.getId().longValue());
        Assert.assertEquals(3L, anand.getId().longValue());
        Assert.assertEquals(1L, book.getId().longValue());
    }
    @Test
    public void test() {
        
        assertBook(
                1L,
                "Principles of Transaction Processing, Second Edition",
                "978-7-302-24041-9",
                new BigDecimal(48),
                "Tsinghua University Press",
                new String[] { "Philip A. Bernstein", "Eric Newcomer", "Anand Rajaraman" }
        );
        
        Book book = new Book();
        book.setId(1L);
        book.setName("RDBMS Transaction");
        book.setIsbn("NEW-ISBN");
        book.setPrice(new BigDecimal(50));
        book.setPublisher("NEW-PUBLISHER");
        book.setAuthors(JPAEntities.createFakeEntities(Author.class, 1L, 3L));
        
        JPAEntities.disable(book, Book_.isbn, Book_.publisher, Book_.price);
        
        try (TransactionScope ts = new TransactionScope(CONNECTION_STRING)) {
            /*
             * (1) "name" and "authors" will be updated because they are enabled
             * (2) "isbn", "price" and "publisher" won't be disabled because they are disabled.
             */
            TransactionScope.getEntityManager().merge(book);
            
            ts.compete();
        }
        
        assertBook(
                1L,
                "RDBMS Transaction", // Updated, because "name" is enabled property
                "978-7-302-24041-9", // Still is old value, because "isbn" is disabled property
                new BigDecimal(48), // Still is old value, because "price" is disabled property
                "Tsinghua University Press", // Still is old value, because "publisher" is disabled property
                new String[] { "Philip A. Bernstein", "Anand Rajaraman" } // Updated, because "authors" is enabed property
        );
    }
    
    private static void assertBook(
            long id,
            String name,
            String isbn,
            BigDecimal price,
            String publisher,
            String[] authorNames) {
        Book book;
        try (TransactionScope ts = new TransactionScope(CONNECTION_STRING, true)) {
            Book__[] queryPaths = new Book__[] {
                    Book__.begin().authors().end() // left fetch join authors
            };
            book = TransactionScope.getEntityManager().find(Book.class, 1L, queryPaths);
        }
        Assert.assertEquals(name, book.getName());
        Assert.assertEquals(isbn, book.getIsbn());
        Assert.assertEquals(price.setScale(10), book.getPrice().setScale(10));
        Assert.assertEquals(publisher, book.getPublisher());
        
        String[] names = book.getAuthors().stream().map(b -> b.getName()).toArray(String[]::new);
        Arrays.sort(names);
        Arrays.sort(authorNames);
        Assert.assertTrue(Arrays.equals(authorNames, names));
    }
}
